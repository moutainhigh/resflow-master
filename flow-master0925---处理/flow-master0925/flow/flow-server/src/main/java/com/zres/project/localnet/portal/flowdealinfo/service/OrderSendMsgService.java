package com.zres.project.localnet.portal.flowdealinfo.service;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderQrySecondaryDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderSendMsgDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.util.OrderTrackOperType;
import com.zres.project.localnet.portal.webservice.sms.SendMessageService;
import com.ztesoft.res.frame.core.util.ListUtil;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工单处理发送短信
 */
@Service
public class OrderSendMsgService {

    Logger logger = LoggerFactory.getLogger(OrderSendMsgService.class);

    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private SendMessageService sendMessageService;
    @Autowired
    private OrderDetailsServiceIntf orderDetailsServiceIntf;
    @Autowired
    private OrderSendMsgDao orderSendMsgDao;
    @Autowired
    private OrderQrySecondaryDao orderQrySecondaryDao;

    public void sendMsgBefore(Map<String, Object> params){
        logger.info("先查询区域和环境是否要发短信。。。。。。。。。。。。。。。。。。。。");
        Map<String,Object> msmSwitchMap = new HashMap<String,Object>();
        String areaId = "";
        String orderType = "LOCAL"; // 正常单 单子类型区分异常单和正常单
        if (params.containsKey("orderType")){ //如果有这个属性，则说明是异常单
            orderType = MapUtils.getString(params, "orderType"); // 异常单LOCAL_EXCEPTION
        }
        String resources = MapUtils.getString(params, "resources");
        if (params.containsKey("operStaffId")) {
            /**
             * 查询当前用户所在区域是否需要发送短信
             * 顺便查询环境是否要发短信--区分测试和生产
             *
             * 回单以及异常单确认
             */
            String operStaffId = MapUtils.getString(params, "operStaffId");
            Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(operStaffId));
            areaId = MapUtils.getString(operStaffInfoMap,"AREA_ID");
            msmSwitchMap = orderDealDao.qryMsmSwitchByArea(areaId);
        }
        if (BasicCode.ONEDRY.equals(resources)){
            /**
             * 一干发起，会传过来区域id
             */
            areaId = MapUtils.getString(params, "areaId");
            msmSwitchMap = orderDealDao.qryMsmSwitchByAreaAtOne(areaId);
        }else if (BasicCode.JIKE.equals(resources)){
            //集客来单以及集客来异常单
            Map<String, Object> provinceMap = orderDealDao.getProviceOrg(MapUtils.getLong(params, "areaId"));
            areaId = MapUtils.getString(provinceMap, "AREA_ID");
            msmSwitchMap = orderDealDao.qryMsmSwitchByArea(areaId);
        }
        if("1".equals(MapUtils.getString(msmSwitchMap,"ISSEND"))){ //ISSEND等于1时，需要发送短信
            //IS_PRODCTION_ENVIRONMENT字段区分环境：1是生产环境；0非生产环境;
            //params.put("IS_PRODCTION_ENVIRONMENT",MapUtils.getString(msmSwitchMap,"IS_PRODCTION_ENVIRONMENT"));
            String  orderIdList=MapUtils.getString(params, "orderIdList");
            if(MapUtils.getString(params, "orderIdList")==null){
                orderIdList="";
            }
            Map<String, Object> msgInfoMap = new HashMap<String, Object>();
            msgInfoMap.put("orderIdList", orderIdList.replace("[", "").replace("]","").replace("\"",""));
            msgInfoMap.put("areaId", areaId);
            msgInfoMap.put("operAction", MapUtils.getString(params, "operAction"));
            msgInfoMap.put("state", "10A");
            msgInfoMap.put("isRelSend", MapUtils.getString(msmSwitchMap,"IS_PRODCTION_ENVIRONMENT") );
            msgInfoMap.put("belongSystem", orderType);
            orderSendMsgDao.insertMsg(msgInfoMap);
        }
    }

    @Transactional
    public void scanMsgInfoAndSend() throws Exception{
        logger.info("----------定时任务扫描短信--正常单-----------");
        List<Map<String, Object>> needSendMsgData = orderSendMsgDao.getNeedSendMsgData("LOCAL");
        if (!ListUtil.isEmpty(needSendMsgData)){
            for (Map<String, Object> needSendMsg : needSendMsgData){
                qryUserObjByWoId(needSendMsg);
            }
        }else {
            logger.info("----------未扫描到需要发送短信的单子---正常单----------");
        }
        logger.info("----------定时任务扫描短信--异常单-----------");
        List<Map<String, Object>> needSendMsgDataException = orderSendMsgDao.getNeedSendMsgData("LOCAL_EXCEPTION");
        if (!ListUtil.isEmpty(needSendMsgDataException)){
            qryUserObjByExpWoIdsSendMsg(needSendMsgDataException);
        }else {
            logger.info("----------未扫描到需要发送短信的单子---异常单----------");
        }
    }

    /**
     * 1,先查询是否由二干下发 放在一个变量里面boolean类型，后面会用到
     * 2,再查询是当前处理中的环节，三种情况： ·正常处理中  又分为主流程到子流程 和 子流程到主流程
     *                                     ·子流程第一个环节退单
     *                                     ·本地测试退单
     * 3,步骤2中获取要短信的orderidlist，区分主流程和子流程   也要区分本地自启和还是二干下发的
     *      主流程分：正常派发               子流程分：正常派发
     *               主流程异常节点退单               子流程内部环节退单 和 本地测试或联调测试退单
     *                                              子流程第一个环节退单
     *   这里的区分主要是为了查询客户订单的信息，因为orderid和业务电路表客户电路表的关联是有区别的
     * 4，步骤3中获取的sendMsgInfoList是分组之后的，直接遍历发送短信就好了
     * 5，这里区分的生产和测试环境，只有生产会调用发送短信的接口会真实发短信  会插入接口日志表gom_bdw_interf_log_info用申请单标题去查询order_no字段
     *                              测试环境只会插入表gom_bdw_send_msg_log 生产也会插这个表
     *
     * 问题出现时，解决思路：· 进入方法的参数传值是否正确params
     *                     · 检查查询当前环节信息
     *                     · 主要检查步骤2是否少考虑了某些情况
     *                     · 再检查步骤3中orderid和业务电路表客户电路表的关联关系是否有问题
     *                     · sendMsgInfoList这个里面是最终发送的短信，插入表gom_bdw_send_msg_log
     * 后面有问题慢慢断点查询吧。。。。。没办法只能帮到这里了
     *
     */
    public Map<String, Object> qryUserObjByWoId(Map<String, Object> params) {
        logger.info(">>>>>>>>>>>>>>>准备发送短信>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Map<String, Object> resMap = new HashMap<String, Object>();
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        List<String> msgOrderIdList = new ArrayList<String>();
        List<Map<String, Object>> sendMsgInfoList = null;
        List<String> orderIdList = new ArrayList<String>();
        //String orderId = MapUtils.getString(params, "orderId");
        String orderIdsStr = MapUtils.getString(params, "ORDER_ID_LIST");
        String[] orderIds = orderIdsStr.split(",");
        for (String orderId : orderIds){
            orderIdList.add(orderId);
        }
        String operAction = MapUtils.getString(params, "OPERACTION");
        boolean ifFromSend = orderDetailsServiceIntf.ifFromSecond(orderIdList.get(0)); //是否二干下发
        boolean ifChildFlowOrderId = false; //是否为子流程的orderid
        boolean sendMsgToSec = false; //是否给二干单子发送短信
        String tacheCode = "";
        for (int i = 0; i < orderIdList.size() ; i++) {
            Map<String, Object> currentTacheOrder = orderSendMsgDao.qryCurrentTache(orderIdList.get(i));
            /**
             * 查询当前是什么环节
             * 本地网-- 资源分配，光纤资源分配 环节查询到父流程orderId，用父流程orderid去查询所有要发的短信
             *         本地测试，联调测试 环节查询
             * 二干调度--还没考虑到。。。。
             */
            tacheCode = MapUtils.getString(currentTacheOrder, "TACHE_CODE");
            String woState = MapUtils.getString(currentTacheOrder, "WO_STATE");
            String psId = MapUtils.getString(currentTacheOrder, "PS_ID");
            if (OrderTrackOperType.WO_ORDER_STATE_2.equals(woState)){
                /**
                 * 当前环节的工单状态为处理中，才能进这个方法
                 * 子流程最后一个环节回单会查不到处理中的工单就不会进这个方法咯
                 */
                if ("1000248,1000249".indexOf(psId) != -1){ //子流程
                    if (!"rollBackOrder".equals(operAction) && ("FIBER_RES_ALLOCATE".equals(tacheCode)
                            || "RES_ALLOCATE".equals(tacheCode))){ //到光纤资源分配，资源分配 环节，查询所有子流程orderid
                        List<String> orderIdChildList = orderSendMsgDao.qryOrderIdChild(orderIdList.get(i));
                        msgOrderIdList.addAll(orderIdChildList);
                    }else { //到外线施工，数据制作，资源施工环节 /*if ("OUTSIDE_CONSTRUCT".equals(tacheCode) || "DATA_MAKE".equals(tacheCode) || "RES_CONSTRUCT".equals(tacheCode))*/
                        msgOrderIdList.add(orderIdList.get(i));
                    }
                    ifChildFlowOrderId = true;
                }else { //主流程
                    if ("RENT,START_RENT,STOP_RENT,START_STOP_RENT".indexOf(tacheCode) != -1){ //起租，起止租，止租环节不用发短信
                        //msgOrderIdList = null;
                        //是否二干下发
                        if (ifFromSend && "RENT".equals(tacheCode)){
                            //二干下发--辅调到起租环节
                            List<String> orderIdsList = orderSendMsgDao.qryAllOrderIdFromSec(orderIdList.get(i));
                            Map<String, String> crossTestTacheInfo = orderSendMsgDao.qryCrossTestWoState(orderIdsList);
                            String crossTestWoState = MapUtils.getString(crossTestTacheInfo, "WO_STATE");
                            if (!StringUtils.isEmpty(crossTestWoState)
                                    && OrderTrackOperType.WO_ORDER_STATE_2.equals(crossTestWoState)){
                                //如果进到这里，给主调跨域全程调测发短信
                                msgOrderIdList.add(MapUtils.getString(crossTestTacheInfo, "ORDER_ID"));
                            }else if (StringUtils.isEmpty(crossTestWoState)
                                    || OrderTrackOperType.WO_ORDER_STATE_4.equals(crossTestWoState)){
                                //如果进到这里，给二干下一个环节发短信
                                Map<String, Object> orderInfoSec = orderQrySecondaryDao.qryIfFromSecondary(orderIdList.get(i));
                                String orderIdSec = MapUtils.getString(orderInfoSec, "PARENT_ORDER_ID");
                                msgOrderIdList.add(orderIdSec);
                                sendMsgToSec = true; //给二干下发短信标识
                            }
                        }
                    }else {
                        msgOrderIdList.add(orderIdList.get(i));
                    }
                    ifChildFlowOrderId = false;
                }

            } else if (OrderTrackOperType.WO_ORDER_STATE_5.equals(woState) && ("FIBER_RES_ALLOCATE".equals(tacheCode)
                    || "RES_ALLOCATE".equals(tacheCode))){ //子流程第一个环节退单  光纤资源分配，资源分配环节
                //查询电路调度环节的orderId
                Map<String, Object> parentOrder = orderDealDao.getParentOrder(orderIdList.get(i));
                msgOrderIdList.add(MapUtils.getString(parentOrder, "PARENTORDERID"));
                operAction = "rollBackOrderChildFristTache";
                ifChildFlowOrderId = true;
            }else if (OrderTrackOperType.WO_ORDER_STATE_10.equals(woState) && "CIRCUIT_DISPATCH".equals(tacheCode)){
                //本地测试退单    查询所有子流程orderid
                List<String> orderIdChildList = orderSendMsgDao.qryOrderIdChild(orderIdList.get(i));
                msgOrderIdList.addAll(orderIdChildList);
                tacheCode = "OUTSIDE_CONSTRUCT,RES_CONSTRUCT"; //外线施工，资源施工
                operAction = "rollBackOrder";
                ifChildFlowOrderId = true;
            }
        }
        if (!ListUtil.isEmpty(msgOrderIdList)){
            List<String> tacheCodeList = new ArrayList<String>();
            if(ifChildFlowOrderId){ //是子流程
                if ("rollBackOrder".equals(operAction)){ //子流程内部环节退单 和 本地测试或联调测试退单
                    String[] tacheCodeStr = tacheCode.split(",");
                    for (String tacheCodes : tacheCodeStr){
                        tacheCodeList.add(tacheCodes);
                    }
                    Map<String, Object> rollBackMap = new HashMap<String, Object>();
                    rollBackMap.put("orderIdList", msgOrderIdList);
                    rollBackMap.put("tacheCodeList", tacheCodeList);
                    if (ifFromSend){
                        sendMsgInfoList = orderSendMsgDao.qryRollBackSendMsgInfoChildFromSec(rollBackMap);
                    }else {
                        sendMsgInfoList = orderSendMsgDao.qryRollBackSendMsgInfoChild(rollBackMap);
                    }
                }else if ("rollBackOrderChildFristTache".equals(operAction)){ //子流程第一个环节退单
                    Map<String, Object> rollBackMap = new HashMap<String, Object>();
                    rollBackMap.put("orderIdList", msgOrderIdList);
                    rollBackMap.put("tacheCode", "CIRCUIT_DISPATCH"); //电路调度
                    if (ifFromSend){
                        sendMsgInfoList = orderSendMsgDao.qryRollBackSendMsgInfoChildFristTacheFromSec(rollBackMap);
                    }else {
                        sendMsgInfoList = orderSendMsgDao.qryRollBackSendMsgInfoChildFristTache(rollBackMap);
                    }
                } else {
                    if (ifFromSend){
                        sendMsgInfoList = orderSendMsgDao.qrySendMsgInfoChildFromSec(msgOrderIdList);
                    }else {
                        sendMsgInfoList = orderSendMsgDao.qrySendMsgInfoChild(msgOrderIdList);
                    }
                }
            }else { //非子流程
                if ("rollBackOrder".equals(operAction)){ //主流程异常节点退单
                    Map<String, Object> rollBackMap = new HashMap<String, Object>();
                    rollBackMap.put("orderIdList", msgOrderIdList);
                    rollBackMap.put("tacheCode", tacheCode);
                    if (ifFromSend){
                        sendMsgInfoList = orderSendMsgDao.qryRollBackSendMsgInfoFromSec(rollBackMap);
                    }else {
                        sendMsgInfoList = orderSendMsgDao.qryRollBackSendMsgInfo(rollBackMap);
                    }
                }else {
                    if (ifFromSend){
                        if (sendMsgToSec){
                            sendMsgInfoList = orderSendMsgDao.qrySendMsgInfo(msgOrderIdList);
                        }else {
                            sendMsgInfoList = orderSendMsgDao.qrySendMsgInfoFromSec(msgOrderIdList);
                        }
                    }else {
                        sendMsgInfoList = orderSendMsgDao.qrySendMsgInfo(msgOrderIdList);
                    }
                }
            }
        }
        if (!ListUtil.isEmpty(sendMsgInfoList)){
            resMap = lastSendMsg(params, msgOrderIdList, sendMsgInfoList, operAction);
            boolean ifToSend = MapUtils.getBooleanValue(resMap, "ifToSend");
            if (!ifToSend){
                paramsMap.put("state", "10X");
                paramsMap.put("msgId", MapUtils.getIntValue(params,"MSG_ID"));
                orderSendMsgDao.updateMsgState(paramsMap);
                orderSendMsgDao.copyMsgToHis(MapUtils.getIntValue(params,"MSG_ID"));
                orderSendMsgDao.deleteMsg(MapUtils.getIntValue(params,"MSG_ID"));
                logger.info(">>>>>>>>>>>>>>>这里不用发送短信！ " + MapUtils.getString(resMap, "message") + " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            }
        }else {
            paramsMap.put("state", "10X");
            paramsMap.put("msgId", MapUtils.getIntValue(params,"MSG_ID"));
            orderSendMsgDao.updateMsgState(paramsMap);
            orderSendMsgDao.copyMsgToHis(MapUtils.getIntValue(params,"MSG_ID"));
            orderSendMsgDao.deleteMsg(MapUtils.getIntValue(params,"MSG_ID"));
            resMap.put("success", true);
            resMap.put("message", "不用发送短信");
            logger.info(">>>>>>>>>>>>>>>这里不用发送短信！>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        }
        return resMap;
    }

    private Map<String, Object> lastSendMsg(Map<String, Object> params, List<String> msgOrderIdList, List<Map<String, Object>> sendMsgInfoList, String operAction) {
        Map<String, Object> resMap = new HashMap<String, Object>();
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        for (Map<String, Object> sendMsgInfoMap : sendMsgInfoList) {
            String reqFinDate = MapUtils.getString(sendMsgInfoMap, "REQ_FIN_DATE");
            String applyOrdId = MapUtils.getString(sendMsgInfoMap, "APPLY_ORD_ID");
            String applyOrdName = MapUtils.getString(sendMsgInfoMap, "APPLY_ORD_NAME");
            String custNameChinese = MapUtils.getString(sendMsgInfoMap, "CUST_NAME_CHINESE");
            String staffLoginName = MapUtils.getString(sendMsgInfoMap, "STAFFLOGINNAME");
            // 申请单编码和申请单标题不为空时
            if (!applyOrdId.equals("") && !applyOrdName.equals("")) {
                try {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat format = new SimpleDateFormat(" yyyy年MM月dd日 ");
                    String reqFinDateStr = format.format(dateFormat.parse(reqFinDate));
                    String smsContent = "【本地调度系统】收到客户【" + custNameChinese + "】一条工单主题为【" + applyOrdName + "】，" + "定单编号为【"
                            + applyOrdId + "】,处于【" + MapUtils.getString(sendMsgInfoMap, "TACHE_NAME") + "】环节,请于" + reqFinDateStr + "处理。";
                    paramsMap.put("userName", staffLoginName);
                    paramsMap.put("tacheId", MapUtils.getString(sendMsgInfoMap, "TACHE_ID"));
                    paramsMap.put("tacheName", MapUtils.getString(sendMsgInfoMap, "TACHE_NAME"));
                    paramsMap.put("dispatchId", 0);
                    paramsMap.put("smsContent", smsContent);
                    paramsMap.put("feedbackTime", reqFinDate);
                    paramsMap.put("applyOrdId", applyOrdId);
                    paramsMap.put("applyOrdName", applyOrdName);
                    paramsMap.put("custNameChinese", custNameChinese);
                    paramsMap.put("orderIdList", msgOrderIdList.toString().replace("[", "").replace("]",""));
                    paramsMap.put("operAction", operAction);
                    logger.info(">>>>>>>>>>>>>>>入库短信日志>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                    orderSendMsgDao.insertSendMsgLog(paramsMap);//入库短信日志
                    if ("1".equals(MapUtils.getString(params,"IS_REL_SEND"))){
                        sendMessageService.sendMsg(paramsMap);
                        logger.info(">>>>>>>>>>>>>>>真实调用短信接口成功！>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                    }
                    orderSendMsgDao.copyMsgToHis(MapUtils.getIntValue(params,"MSG_ID"));
                    orderSendMsgDao.deleteMsg(MapUtils.getIntValue(params,"MSG_ID"));
                    resMap.put("success", true);
                    resMap.put("ifToSend", true);
                    resMap.put("message", "发送短信成功！");
                    logger.info(">>>>>>>>>>>>>>>发送短信成功！>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                } catch (Exception e) {
                    paramsMap.put("state", "10S");
                    paramsMap.put("msgId", MapUtils.getIntValue(params,"MSG_ID"));
                    orderSendMsgDao.updateMsgState(paramsMap);
                    resMap.put("success", false);
                    resMap.put("message", "发送短信失败！");
                    logger.info(">>>>>>>>>>>>>>>发送短信失败！>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                    e.printStackTrace();// ParseException
                    throw new RuntimeException("发送短信失败：" + e);
                }
            }else {
                resMap.put("success", true);
                resMap.put("ifToSend", false);
                resMap.put("message", "申请单编码和申请单标题为空，不发短信");
            }
        }
        return resMap;
    }

    public Map<String, Object> qryUserObjByExpWoIdsSendMsg(List<Map<String, Object>> needSendMsgDataException) {
        logger.info(">>>>>>>>>>>>>>>异常单--调用短信接口>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Map<String, Object> resMap = new HashMap<String, Object>();
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        for (Map<String, Object> needSendMsgExc : needSendMsgDataException){
            String orderListStr = MapUtils.getString(needSendMsgExc, "ORDER_ID_LIST");
            String[] orderIds = orderListStr.split(",");
            for (String orderId : orderIds){
                List<Map<String, Object>> dispObjList = new ArrayList<>();
                if("null".equals(orderId)){
                    dispObjList = null;
                }else {
                    List<Map<String, Object>> isExpWoOrderChild = orderDealDao.gryIsExpWoOrderChild(orderId);
                    if (ListUtil.isEmpty(isExpWoOrderChild)){
                        //如果是空说明是子流程
                        dispObjList = orderDealDao.qryExpWoOrderChildDispObjList(orderId);//子流程
                        if (ListUtil.isEmpty(dispObjList)){
                            dispObjList = orderDealDao.qryExpWoOrderDispObjListByChildOrderId(orderId);//子流程到主流程
                        }

                    }else {
                        //父流程
                        dispObjList = orderDealDao.qryExpWoOrderDispObjList(orderId); //主流程
                        if (ListUtil.isEmpty(dispObjList)){
                            dispObjList = orderDealDao.qryExpWoOrderChildDispObjListByParentOrderId(orderId); //主流程到子流程
                        }
                    }
                }
                if (!ListUtil.isEmpty(dispObjList)){
                    for (Map<String, Object> dataMap : dispObjList) {
                        String reqFinDate = MapUtils.getString(dataMap, "REQ_FIN_DATE");
                        String applyOrdId = MapUtils.getString(dataMap, "APPLY_ORD_ID");
                        String applyOrdName = MapUtils.getString(dataMap, "APPLY_ORD_NAME");
                        String custNameChinese = MapUtils.getString(dataMap, "CUST_NAME_CHINESE");
                        String staffLoginName = MapUtils.getString(dataMap, "STAFFLOGINNAME");
                        // 申请单编码和申请单标题不为空时
                        if (!applyOrdId.equals("") && !applyOrdName.equals("")) {
                            try {
                                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                SimpleDateFormat format = new SimpleDateFormat(" yyyy年MM月dd日 ");
                                String reqFinDateStr = format.format(dateFormat.parse(reqFinDate));
                                String smsContent = "【本地调度系统】收到客户【" + custNameChinese + "】一条异常工单主题为【" + applyOrdName + "】，" + "定单编号为【"
                                        + applyOrdId + "】,请于" + reqFinDateStr + "处理。";
                                paramsMap.put("userName", staffLoginName);
                                paramsMap.put("tacheId", MapUtils.getString(dataMap, "TACHE_ID"));
                                paramsMap.put("tacheName", MapUtils.getString(dataMap, "TACHE_NAME"));
                                paramsMap.put("dispatchId", 0);
                                paramsMap.put("smsContent", smsContent);
                                paramsMap.put("feedbackTime", reqFinDate);
                                paramsMap.put("applyOrdId", applyOrdId);
                                paramsMap.put("applyOrdName", applyOrdName);
                                paramsMap.put("custNameChinese", custNameChinese);
                                paramsMap.put("orderIdList", orderId);
                                paramsMap.put("operAction", MapUtils.getString(needSendMsgExc, "ORDER_ID_LIST"));
                                logger.info(">>>>>>>>>>>>>>>入库短信日志>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                                List sendMsgLogList = orderSendMsgDao.qrySendMsgLog(paramsMap);//查询是否有插入
                                if (ListUtil.isEmpty(sendMsgLogList)){
                                    orderSendMsgDao.insertSendMsgLog(paramsMap);//入库短信日志
                                    if ("1".equals(MapUtils.getString(needSendMsgExc,"IS_REL_SEND"))){
                                        sendMessageService.sendMsg(paramsMap);
                                        logger.info(">>>>>>>>>>>>>>>异常单--真实调用短信接口成功！>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                                    }
                                    //sendMessageService.sendMsg(paramsMap);
                                }
                                orderSendMsgDao.copyMsgToHis(MapUtils.getIntValue(needSendMsgExc,"MSG_ID"));
                                orderSendMsgDao.deleteMsg(MapUtils.getIntValue(needSendMsgExc,"MSG_ID"));
                                resMap.put("success", true);
                                resMap.put("message", "发送短信成功！");
                                logger.info(">>>>>>>>>>>>>>>异常单--发送短信成功！>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                            } catch (Exception e) {
                                paramsMap.put("state", "10S");
                                paramsMap.put("msgId", MapUtils.getIntValue(needSendMsgExc,"MSG_ID"));
                                orderSendMsgDao.updateMsgState(paramsMap);
                                resMap.put("success", false);
                                resMap.put("message", "发送短信失败！");
                                logger.info(">>>>>>>>>>>>>>>异常单--发送短信失败！>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                                e.printStackTrace();// ParseException
                                throw new RuntimeException("发送短信失败：" + e);
                            }
                        }
                    }
                } else {
                    paramsMap.put("state", "10X");
                    paramsMap.put("msgId", MapUtils.getIntValue(needSendMsgExc,"MSG_ID"));
                    orderSendMsgDao.updateMsgState(paramsMap);
                    orderSendMsgDao.copyMsgToHis(MapUtils.getIntValue(needSendMsgExc,"MSG_ID"));
                    orderSendMsgDao.deleteMsg(MapUtils.getIntValue(needSendMsgExc,"MSG_ID"));
                    resMap.put("success", true);
                    resMap.put("message", "不用发送短信");
                    logger.info(">>>>>>>>>>>>>>>异常单--这里不用发送短信！>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                }
            }
        }
        return resMap;
    }

}
