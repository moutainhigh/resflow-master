package com.zres.project.localnet.portal.flowdealinfo.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderQrySecondaryDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderSendMsgDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.util.OrderTrackOperType;
import com.zres.project.localnet.portal.webservice.sms.SendMessageService;

import com.ztesoft.res.frame.core.util.ListUtil;

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
        String orderType = "SEC"; // 正常单 单子类型区分异常单和正常单
        if (params.containsKey("orderType")){ //如果有这个属性，则说明是异常单
            orderType = MapUtils.getString(params, "orderType"); // 异常单SEC_EXCEPTION
        }
        String resources = MapUtils.getString(params, "resources");
        if (params.containsKey("operStaffId")) {
            /**
             * 查询当前用户所在区域是否需要发送短信
             * 顺便查询环境是否要发短信--区分测试和生产
             *
             * 回单以及异常单确认
             *
             */
            String operStaffId = MapUtils.getString(params, "operStaffId");
            Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(operStaffId));
            areaId = MapUtils.getString(operStaffInfoMap,"AREA_ID");
            msmSwitchMap = orderDealDao.qryMsmSwitchByArea(areaId);
        }
        if (BasicCode.ONEDRY.equals(resources)){
            /**
             * 一干通知全程调测处发送短信和一干发起，会传过来区域id
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
            /*params.put("IS_PRODCTION_ENVIRONMENT",MapUtils.getString(msmSwitchMap,"IS_PRODCTION_ENVIRONMENT"));
            this.qryUserObjByWoId(params);*/
            String  orderIdList=MapUtils.getString(params, "orderIdList");
            if(MapUtils.getString(params, "orderIdList")==null){
                orderIdList="";
            }
            Map<String, Object> msgInfoMap = new HashMap<String, Object>();
            msgInfoMap.put("orderIdList", orderIdList.replace("[", "").replace("]","")
                    .replace("\"",""));
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
        logger.info("----------定时任务扫描短信---正常单----------");
        List<Map<String, Object>> needSendMsgData = orderSendMsgDao.getNeedSendMsgData("SEC");
        if (!ListUtil.isEmpty(needSendMsgData)){
            for (Map<String, Object> needSendMsg : needSendMsgData){
                qryUserObjByWoId(needSendMsg);
            }
        }else {
            logger.info("----------未扫描到需要发送短信的单子---正常单----------");
        }
        logger.info("----------定时任务扫描短信--异常单-----------");
        List<Map<String, Object>> needSendMsgDataException = orderSendMsgDao.getNeedSendMsgData("SEC_EXCEPTION");
        if (!ListUtil.isEmpty(needSendMsgDataException)){
            qryUserObjByExpWoIdsSendMsg(needSendMsgDataException);
        }else {
            logger.info("----------未扫描到需要发送短信的单子---异常单----------");
        }
    }

    public Map<String, Object> qryUserObjByWoId(Map<String, Object> params) {
        logger.info(">>>>>>>>>>>>>>>准备发送短信>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Map<String, Object> resMap = new HashMap<String, Object>();
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        List<String> msgOrderIdList = new ArrayList<String>();
        List<Map<String, Object>> sendMsgInfoList = null;
        List<String> orderIdList = new ArrayList<String>();
        String orderIdsStr = MapUtils.getString(params, "ORDER_ID_LIST");
        String[] orderIds = orderIdsStr.split(",");
        for (String orderId : orderIds){
            orderIdList.add(orderId);
        }
        String operAction = MapUtils.getString(params, "OPERACTION");
        boolean ifChildFlowOrderId = false; //是否为子流程的orderid
        boolean ifToBdw = false; //是否下发本地网
        String tacheCode = "";
        String woState = "";
        for (int i = 0; i < orderIdList.size() ; i++) {
            Map<String, Object> currentTacheOrder = orderSendMsgDao.qryCurrentTache(orderIdList.get(i));
            /**
             * 1，查询当前是什么环节  整个流程的主流程和子流程
             * 二干调度-- 资源分配，专业数据制作 环节查询到父流程orderId，用父流程orderid去查询所有要发的短信
             * 2，查询主流程到什么环节  在待数据制作和本地调度  是否下发本地网  是发送短信
             */
            tacheCode = MapUtils.getString(currentTacheOrder, "TACHE_CODE");
            woState = MapUtils.getString(currentTacheOrder, "WO_STATE");
            String psId = MapUtils.getString(currentTacheOrder, "PS_ID");
            if (OrderTrackOperType.WO_ORDER_STATE_2.equals(woState)){
                /**
                 * 当前环节的工单状态为处理中，才能进这个方法
                 * 子流程最后一个环节回单会查不到处理中的工单就不会进这个方法咯
                 */
                if ("10101064,10101043".indexOf(psId) != -1){ //子流程
                    if (!"rollBackOrder".equals(operAction) && ("SEC_SOURCE_DISPATCH_CLD".equals(tacheCode)
                            || "SPECIALTY_DATA_PRODUCTION".equals(tacheCode))){ //到资源分配 数据制作 环节，查询所有子流程orderid
                        String tacheCodeFlag = "";
                        if ("SEC_SOURCE_DISPATCH_CLD".equals(tacheCode)){
                            tacheCodeFlag = BasicCode.SPECIALTY;
                        } else if ("SPECIALTY_DATA_PRODUCTION".equals(tacheCode)){
                            tacheCodeFlag = BasicCode.NETMANAGE;
                        }
                        List<String> orderIdChildList = orderSendMsgDao.qryOrderIdChild(orderIdList.get(i), tacheCodeFlag);
                        msgOrderIdList.addAll(orderIdChildList);
                    }else {
                        msgOrderIdList.add(orderIdList.get(i));
                    }
                    ifChildFlowOrderId = true;
                }else { //主流程
                    if ("NOTICE_OF_RENT_CONFIRMATION_2,NOTICE_OF_RENT_CONFIRMATION,NEW_APPLICATION_FORM".indexOf(tacheCode) == -1){ //起租确认通知,新建申请单环节不用发短信
                        msgOrderIdList.add(orderIdList.get(i));
                    }
                    ifChildFlowOrderId = false;
                }
            }else if (OrderTrackOperType.WO_ORDER_STATE_5.equals(woState)
                    && "SEC_SOURCE_DISPATCH_CLD".equals(tacheCode)){ //子流程第一个环节二干资源分配环节退单
                //查询父流程的orderId
                Map<String, Object> parentOrder = orderDealDao.getParentOrder(orderIdList.get(i));
                msgOrderIdList.add(MapUtils.getString(parentOrder, "PARENTORDERID"));
                operAction = "rollBackOrderChildFristTache";
                tacheCode = "SECONDARY_SCHEDULE,SECONDARY_SCHEDULE_2"; //二干调度
                ifChildFlowOrderId = true;
            }else if (OrderTrackOperType.WO_ORDER_STATE_10.equals(woState)
                    && ("TO_DATA_CREATE_AND_SCHEDULE".equals(tacheCode) || "TO_DATA_CREATE_AND_SCHEDULE_2".equals(tacheCode))){
                //退单到待数据制作与本地调度    查询所选退单子流程orderid
                List<String> orderIdChildList =  orderSendMsgDao.qryOrderIdChild(orderIdList.get(i),BasicCode.NETMANAGE);
                msgOrderIdList.addAll(orderIdChildList);
                tacheCode = "SPECIALTY_DATA_PRODUCTION,LOCAL_TEST,CROSS_WHOLE_COURDER_TEST"; //数据制作，本地测试，跨域全程调测
                operAction = "rollBackOrder";
                ifChildFlowOrderId = true;
            }

            if ("NOTICE_OF_RENT_CONFIRMATION_2,NOTICE_OF_RENT_CONFIRMATION".indexOf(tacheCode) == -1){ //起租确认通知
                //查询主流程是否到待数据制作和本地调度   用户正向和反向
                int isDataAndSchedule = orderSendMsgDao.qryIsDataAndSchedule(orderIdList.get(i));
                if (isDataAndSchedule > 0){
                    Map<String, Object> dispatchDataMap = orderQrySecondaryDao.qryDispatchData(orderIdList.get(i));
                    if("是".equals(MapUtils.getString(dispatchDataMap, "TO_BDW"))){
                        List<String> orderIdToBdwList = orderSendMsgDao.qryOrderIdToBdw(orderIdList.get(i));
                        if (!"specialtyDataFinsh".equals(operAction)){ //专业数据制作环节最后回单完成和二干下发本地网和全程调测或者完工确认退单
                            msgOrderIdList.addAll(orderIdToBdwList);
                        }
                        ifToBdw = true;
                    }
                }
            }
        }
        if (!ListUtil.isEmpty(msgOrderIdList)){
            List<String> tacheCodeList = new ArrayList<String>();
            if(ifChildFlowOrderId){ //是子流程
                if ("rollBackOrder".equals(operAction)){ //全程调测或者完工确认退单
                    String[] tacheCodeStr = tacheCode.split(",");
                    for (String tacheCodes : tacheCodeStr){
                        tacheCodeList.add(tacheCodes);
                    }
                    Map<String, Object> rollBackMap = new HashMap<String, Object>();
                    rollBackMap.put("orderIdList", msgOrderIdList);
                    rollBackMap.put("tacheCodeList", tacheCodeList);
                    sendMsgInfoList = orderSendMsgDao.qryRollBackSendMsgInfoChild(rollBackMap);
                    if (ifToBdw){
                        List<Map<String, Object>> toBdwList = orderSendMsgDao.qryRollBackSendMsgInfoFromSec(rollBackMap);
                        sendMsgInfoList.addAll(toBdwList);
                    }
                }else if ("rollBackOrderChildFristTache".equals(operAction)){ //子流程第一个环节退单
                    String[] tacheCodeStr = tacheCode.split(",");
                    for (String tacheCodes : tacheCodeStr){
                        tacheCodeList.add(tacheCodes);
                    }
                    Map<String, Object> rollBackMap = new HashMap<String, Object>();
                    rollBackMap.put("orderIdList", msgOrderIdList);
                    rollBackMap.put("tacheCodeList", tacheCodeList); //二干调度
                    sendMsgInfoList = orderSendMsgDao.qryRollBackSendMsgInfoChildFristTache(rollBackMap);
                }else {
                    sendMsgInfoList = orderSendMsgDao.qrySendMsgInfoChild(msgOrderIdList);
                    if (ifToBdw){
                        //List<Map<String, Object>> toBdwList = orderSendMsgDao.qrySendMsgInfo(msgOrderIdList);
                        List<Map<String, Object>> toBdwList = orderSendMsgDao.qrySendMsgInfoFromSec(msgOrderIdList);
                        sendMsgInfoList.addAll(toBdwList);
                    }
                }
            }else { //非子流程
                if ("rollBackOrder".equals(operAction)){ //主流程异常节点退单
                    Map<String, Object> rollBackMap = new HashMap<String, Object>();
                    rollBackMap.put("orderIdList", msgOrderIdList);
                    rollBackMap.put("tacheCode", tacheCode);
                    sendMsgInfoList = orderSendMsgDao.qryRollBackSendMsgInfo(rollBackMap);
                }else {
                    sendMsgInfoList = orderSendMsgDao.qrySendMsgInfo(msgOrderIdList);
                    if (ifToBdw){
                        List<Map<String, Object>> toBdwList = orderSendMsgDao.qrySendMsgInfoFromSec(msgOrderIdList);
                        sendMsgInfoList.addAll(toBdwList);
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
            if(!"NEW_APPLICATION_FORM".equals(tacheCode) || "290000114".equals(woState)){ //如果当前环节是新建申请单环节 等下次扫单 || 如果状态是退单草稿箱直接删除
                paramsMap.put("state", "10X");
                paramsMap.put("msgId", MapUtils.getIntValue(params,"MSG_ID"));
                orderSendMsgDao.updateMsgState(paramsMap);
                orderSendMsgDao.copyMsgToHis(MapUtils.getIntValue(params,"MSG_ID"));
                orderSendMsgDao.deleteMsg(MapUtils.getIntValue(params,"MSG_ID"));
            }
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
            if (BasicCode.RENT.equals(MapUtils.getString(sendMsgInfoMap, "TACHE_ID"))){
                resMap.put("success", true);
                resMap.put("ifToSend", false);
                resMap.put("message", "起租环节不需要发短信");
                //起租环节不用发短信
                continue;
            }
            // 申请单编码和申请单标题不为空时
            if (!applyOrdId.equals("") && !applyOrdName.equals("")) {
                try {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat format = new SimpleDateFormat(" yyyy年MM月dd日 ");
                    String reqFinDateStr = format.format(dateFormat.parse(reqFinDate));
                    String smsContent = "【二干调度系统】收到客户【" + custNameChinese + "】一条工单主题为【" + applyOrdName + "】，" + "定单编号为【"
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

    private Map<String, Object> qryUserObjByExpWoIdsSendMsg(List<Map<String, Object>> needSendMsgDataException) {
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
                                String smsContent = "【二干调度系统】收到客户【" + custNameChinese + "】一条异常工单主题为【" + applyOrdName + "】，" + "定单编号为【"
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
