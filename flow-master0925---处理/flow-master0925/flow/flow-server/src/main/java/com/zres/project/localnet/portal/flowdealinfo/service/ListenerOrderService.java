package com.zres.project.localnet.portal.flowdealinfo.service;

import com.google.common.collect.Lists;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderQrySecondaryDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCodeEnum;
import com.zres.project.localnet.portal.flowdealinfo.service.entry.TacheWoOrder;
import com.zres.project.localnet.portal.initApplOrderDetail.dao.EditDraftDao;
import com.zres.project.localnet.portal.listener.util.EnmuValueUtil;
import com.zres.project.localnet.portal.logInfo.service.TacheDealLogIntf;
import com.zres.project.localnet.portal.order.data.dao.OrderQueryDao;
import com.zres.project.localnet.portal.resourceInitiate.data.dao.ResourceInitiateDao;
import com.zres.project.localnet.portal.util.OrderTrackOperType;
import com.zres.project.localnet.portal.util.SpringContextHolderUtil;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.zres.project.localnet.portal.webservice.interfaceJiKe.FinishOrderServiceIntf;
import com.zres.project.localnet.portal.webservice.oneDry.FeedbackInterface;
import com.zres.project.localnet.portal.webservice.outLineSystem.ProvinceSendOrderServiceIntf;
import com.zres.project.localnet.portal.webservice.provinceRes.ProvinceResOrderService;
import com.zres.project.localnet.portal.webservice.res.BusinessArchiveServiceIntf;
import com.zres.project.localnet.portal.webservice.res.ResCfsAttrUpdateServiceIntf;
import com.ztesoft.res.frame.core.util.ListUtil;
import com.ztesoft.res.frame.core.util.MapUtil;
import com.ztesoft.res.frame.flow.common.dto.FlowWoDTO;
import com.ztesoft.res.frame.flow.common.intf.FlowAction;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ListenerOrderService implements ListenerOrderServiceIntf {

    Logger logger = LoggerFactory.getLogger(ListenerOrderService.class);

    @Autowired
    private FlowAction flowAction;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private OrderQrySecondaryDao orderQrySecondaryDao;
    @Autowired
    private BusinessArchiveServiceIntf businessArchiveServiceIntf;
    @Autowired
    private FeedbackInterface feedbackInterface;
    @Autowired
    private OrderDealServiceIntf orderDealServiceIntf;
    @Autowired
    private FinishOrderServiceIntf finishOrderServiceIntf;
    @Autowired
    private EditDraftDao editDraftDao;
    @Autowired
    private ListenerOrderServiceIntf listenerOrderServiceIntf;
    @Autowired
    private WebServiceDao webServiceDao;
    @Autowired
    private OrderSendMsgService orderSendMsgService;
    @Autowired
    private TacheDealLogIntf tacheDealLogIntf;
    @Autowired
    private ResourceInitiateDao resourceInitiateDao;
    @Autowired
    private ResCfsAttrUpdateServiceIntf resCfsAttrUpdateServiceIntf;
    @Autowired
    private ProvinceSendOrderServiceIntf provinceSendOrderServiceIntf;
    @Autowired
    private ProvinceResOrderService provinceResOrderService;

    @Autowired
    private OrderQueryDao orderQueryDao;

    @Autowired
    private FowordResService fowordResService;

    @Override
    public void childFlowLastTache(Map<String, Object> param) {
        /*
         判断是否为子流程的最后一个环节，并且电路调度环节为"290000110" 已启子流程
         因为如果子流程退单，那么电路调度环节状态会改成处理中，允许重新派单
          */
        String orderId = MapUtils.getString(param, "orderId");
        Map parentOrder = orderDealDao.getParentOrder(orderId);
        String parentOrderId = MapUtils.getString(parentOrder, "PARENTORDERID");
        Map childLastNum = orderDealDao.qryChildFlowNumAtLast(parentOrderId);
        Map childNum = orderDealDao.qryChildFlowNum(parentOrderId);
        try {
            if (MapUtils.getString(childLastNum, "CHILDLASTNUM")
                    .equals(MapUtils.getString(childNum, "CHILDNUM"))) {
                Map<String, Object> paramMap = new HashMap<String, Object>();
                paramMap.put("orderId", parentOrderId);
                paramMap.put("woState", OrderTrackOperType.WO_ORDER_STATE_10);
                Map woOrder = orderDealDao.getParentWoOrder(paramMap);
                String woOrderId = MapUtils.getString(woOrder, "WOID","");
                if(!"".equals(woOrderId)){
                    // 电路调度环节提交工单
                    param.put("parentOrderId",parentOrderId);
                    this.circuitDispatchCompWoOrder(param);
                }
            }
        } catch (Exception e) {
            logger.error("OrderFinishedEvent updateOrderAttr failed" + e.getMessage(), e);
            throw new RuntimeException("OrderFinishedEvent updateOrderAttr failed" + e.getMessage());
        }
    }

    @Override
    public void crassWholeTest(Map<String, Object> param) {
        /**
         * 跨域全程调测到单
         * 1，先判断是一干来单还是二干来单；
         * 2，一干直接下发本地网；
         * 3，二干下发本地网：二干直接下发 -- 这里非拆机 线条参数ifMainOffice=1
         *                  一干下发二干，二干下发；
         */
        String woId = MapUtils.getString(param, "woId");
        String orderId = MapUtils.getString(param, "orderId");
        Map<String, Object> ifFromSecondaryMap = orderQrySecondaryDao.qryIfFromSecondary(orderId);
        if(MapUtils.isNotEmpty(ifFromSecondaryMap)){
            Map<String, Object> paramsMap = new HashMap<String, Object>();
            //查询是否由退单而来的
            Map<String, Object> ifBack = orderDealDao.qryCrossWholeAttrParams(woId);
            String oldWoId = MapUtils.getString(ifBack, "OLD_WO_ID");
            //String privForwardWoId= MapUtils.getString(ifBack, "PRIV_FORWARD_WO_ID");
            String isZeFan= MapUtils.getString(ifBack, "IS_ZE_FAN");
            Map<String, String> cstOrderDataSecMap = orderDealDao.qryCstOrderDataFromSec(orderId);
            if (BasicCode.ONEDRY.equals(MapUtils.getString(cstOrderDataSecMap, "RESOURCES"))){
                //一干下发二干，二干下发；
                String qcwoorderCode = MapUtils.getString(cstOrderDataSecMap, "QCWOORDERCODE");
                if (StringUtils.isEmpty(qcwoorderCode)) {
                    // 监听跨域全程调测环节产生修改状态
                    logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>跨域全程调测环节状态修改为等一干通知。。。。。。。。。。。");
                    orderDealDao.updateWoStateByWoId(woId, OrderTrackOperType.WO_ORDER_STATE_11);
                }
            }else if(BasicCode.SECONDARY.equals(MapUtils.getString(cstOrderDataSecMap, "RESOURCES"))
                    ||BasicCode.JIKE.equals(MapUtils.getString(cstOrderDataSecMap, "RESOURCES"))){
                /**
                 * 二干直接下发
                 * 二干系统完工汇总环节退单，会退本地的主调到跨域全程调测，这里的判断条件不能修改。。。。
                 */
                //if (StringUtils.isEmpty(oldWoId) && StringUtils.isEmpty(privForwardWoId)){
                if (StringUtils.isEmpty(oldWoId) && OrderTrackOperType.FORWARD.equals(isZeFan)){ //正向
                    orderDealDao.updateWoStateByWoId(woId, OrderTrackOperType.WO_ORDER_STATE_15);
                }
                List<HashMap<String, String>> operAttrsList = new ArrayList<HashMap<String, String>>();
                HashMap<String, String> operAttrsMap = new HashMap<String, String>();
                operAttrsMap.put("KEY", "ifMainOffice");
                operAttrsMap.put("VALUE", "1");
                operAttrsList.add(operAttrsMap);
                paramsMap.put("operAttrsList", operAttrsList);
            }
            if(StringUtils.isEmpty(oldWoId)){
                int rentTacheNum = orderQrySecondaryDao.qryRentTacheNum(MapUtils.getString(ifFromSecondaryMap,"PARENT_ORDER_ID"));
                int allNum = orderQrySecondaryDao.qryCrossFlowNum(MapUtils.getString(ifFromSecondaryMap,"PARENT_ORDER_ID"));
                if(allNum - rentTacheNum == 1){
                    paramsMap.put("parentOrderId", MapUtils.getString(ifFromSecondaryMap,"PARENT_ORDER_ID"));
                    boolean flag = listenerOrderServiceIntf.qryDataChildFlowSec(paramsMap);
                    if (flag){
                        paramsMap.put("orderId", orderId);
                        listenerOrderServiceIntf.finshDataAndSchedule(paramsMap);
                    }
                }
            }
        }else {
            //一干直接下发本地网
            Map<String, String> cstOrderDataMap = orderDealDao.qryCstOrderData(orderId);
            String qcwoorderCode = MapUtils.getString(cstOrderDataMap, "QCWOORDERCODE");
            if (StringUtils.isEmpty(qcwoorderCode)) {
                // 监听跨域全程调测环节产生修改状态
                orderDealDao.updateWoStateByWoId(woId, OrderTrackOperType.WO_ORDER_STATE_11);
            }
        }
    }

    @Override
    public void rentTache(Map<String, Object> param) {
        /**
         * 只监听二干下发的单子
         * 1，二干直接下发
         * 2，一干下发二干，二干下发本地网
         */
        String orderId = MapUtils.getString(param, "orderId");
        Map<String, Object> ifFromSecondaryMap = orderQrySecondaryDao.qryIfFromSecondary(orderId);
        if(MapUtils.isNotEmpty(ifFromSecondaryMap)){
            int rentTacheNum = orderQrySecondaryDao.qryRentTacheNum(MapUtils.getString(ifFromSecondaryMap,"PARENT_ORDER_ID"));
            int allNum = orderQrySecondaryDao.qryCrossFlowNum(MapUtils.getString(ifFromSecondaryMap,"PARENT_ORDER_ID"));
            Map<String, String> cstOrderDataMap = orderDealDao.qryCstOrderDataFromSec(orderId);
            String mainOrg = MapUtils.getString(cstOrderDataMap, "MAINORG"); // 一干指定的主调
            String resources = MapUtils.getString(cstOrderDataMap, "RESOURCES"); // 数据来源
            String areaParentId = MapUtils.getString(cstOrderDataMap, "HANDLE_DEP_ID"); // 受理区域id
            if (BasicCode.ONEDRY.equals(resources)) {
                //一干下发二干，二干下发本地网
                if (mainOrg.equals(areaParentId)) {
                    //是主调
                    if(allNum - rentTacheNum == 1){
                        Map<String,Object> testOrderMap = orderQrySecondaryDao.qryTestTacheOrder(MapUtils.getString(ifFromSecondaryMap,"PARENT_ORDER_ID"));
                        if (MapUtils.isNotEmpty(testOrderMap)){
                            Map<String, Object> paramsMap = new HashMap<String, Object>();
                            paramsMap.put("parentOrderId", MapUtils.getString(ifFromSecondaryMap,"PARENT_ORDER_ID"));
                            boolean flag = listenerOrderServiceIntf.qryDataChildFlowSec(paramsMap);
                            if (flag){
                                paramsMap.put("orderId", orderId);
                                listenerOrderServiceIntf.finshDataAndSchedule(paramsMap);
                            }
                        }
                    }
                }else {
                    //不是主调
                    if(allNum == rentTacheNum && "onedry".equals(MapUtils.getString(ifFromSecondaryMap,"RESOURCES"))){ //二干不是主调局 一干下发的单子
                        Map<String, Object> paramsMap = new HashMap<String, Object>();
                        paramsMap.put("parentOrderId", MapUtils.getString(ifFromSecondaryMap,"PARENT_ORDER_ID"));
                        boolean flag = listenerOrderServiceIntf.qryDataChildFlowSec(paramsMap);
                        if (flag){
                            paramsMap.put("orderId", orderId);
                            listenerOrderServiceIntf.finshDataAndSchedule(paramsMap);
                        }
                    }
                }
            }else if (BasicCode.SECONDARY.equals(resources)
                    ||BasicCode.JIKE.equals(resources)){
                //二干直接下发
                if(allNum - rentTacheNum == 1){
                    Map<String,Object> testOrderMap = orderQrySecondaryDao.qryTestTacheOrder(MapUtils.getString(ifFromSecondaryMap,"PARENT_ORDER_ID"));
                    if (MapUtils.isNotEmpty(testOrderMap)){
                        Map<String, Object> paramsMap = new HashMap<String, Object>();
                        paramsMap.put("parentOrderId", MapUtils.getString(ifFromSecondaryMap,"PARENT_ORDER_ID"));
                        boolean flag = listenerOrderServiceIntf.qryDataChildFlowSec(paramsMap);
                        if (flag){
                            paramsMap.put("orderId", orderId);
                            //添加线条参数
                            List<HashMap<String, String>> operAttrsList = new ArrayList<HashMap<String, String>>();
                            HashMap<String, String> operAttrsMap = new HashMap<String, String>();
                            operAttrsMap.put("KEY", "ifMainOffice");
                            operAttrsMap.put("VALUE", "1");
                            operAttrsList.add(operAttrsMap);
                            paramsMap.put("operAttrsList", operAttrsList);
                            listenerOrderServiceIntf.finshDataAndSchedule(paramsMap);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean qryDataChildFlowSec(Map<String, Object> param) {
        boolean flag = false;
        String parentOrderId = MapUtils.getString(param, "parentOrderId");
        String tacheCode = EnmuValueUtil.SPECIALTY_DATA_PRODUCTION_FINSH;
        String subName = EnmuValueUtil.NETMANAGE;
        Map childLastNum = orderQrySecondaryDao.qryChildFlowNumAtLastSec(parentOrderId,tacheCode,subName);
        Map childNum = orderQrySecondaryDao.qryChildFlowNum(parentOrderId, subName);
        if (MapUtils.getString(childLastNum, "CHILDLASTNUM").equals(MapUtils.getString(childNum, "CHILDNUM"))) {
            flag = true;
        }
        return flag;
    }

    @Override
    public void finshDataAndSchedule(Map<String, Object> param) {
        String orderId = MapUtils.getString(param, "orderId");
        //将二干待数据制作与本地调度环节回单
        Map<String,Object> dataScheduleOrderMap = orderQrySecondaryDao.qryDataScheduleOrder(orderId);
        if(MapUtils.isNotEmpty(dataScheduleOrderMap) && !"".equals(MapUtils.getString(dataScheduleOrderMap,"WO_ID"))){
            //修改待数据制作与本地调度环节的工单状态为处理中
            orderDealDao.updateWoStateByWoId(MapUtils.getString(dataScheduleOrderMap,"WO_ID"),OrderTrackOperType.WO_ORDER_STATE_2);
            FlowWoDTO woDTO = new FlowWoDTO();
            woDTO.setWoId(MapUtils.getString(dataScheduleOrderMap,"WO_ID"));
            if (!ListUtil.isEmpty((List<HashMap<String, String>>) MapUtils.getObject(param, "operAttrsList"))){
                woDTO.setOperAttrs((List<HashMap<String, String>>) MapUtils.getObject(param, "operAttrsList"));
            }
            flowAction.complateWo("-2000", woDTO);
        }
    }

    /**
     * 电路调度环节提交工单
     *
     * @param param
     */
    @Override
    public void circuitDispatchCompWoOrder(Map<String, Object> param) {
        List<String> orderIdList = new ArrayList<String>();
        String parentOrderId = MapUtils.getString(param,"parentOrderId");
        try{
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("orderId", parentOrderId);
            paramMap.put("woState", OrderTrackOperType.WO_ORDER_STATE_10);
            Map woOrder = orderDealDao.getParentWoOrder(paramMap);
            String woOrderId = MapUtils.getString(woOrder, "WOID");
            String psId = MapUtils.getString(woOrder, "PSID");
            Map<String, Object> srvMap = orderDealDao.qrysrvOrdIdByorderId(parentOrderId);
            String srvOrdId = MapUtils.getString(srvMap, "SRV_ORD_ID");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("srvOrdId", srvOrdId);
            Map<String, Object> belongSysMap = orderQrySecondaryDao.qrySrvOrderBelongSys(params);
            String systemResource = MapUtils.getString(belongSysMap, "SYSTEM_RESOURCE");
            String srvOrderIdRes = "";
            String flagSys = BasicCode.LOCALBUILD;
            if (BasicCode.SECOND.equals(systemResource)){
                Map<String, Object> srvRelateMap = orderQrySecondaryDao.qryIfFromSecondary(parentOrderId);
                srvOrderIdRes = MapUtils.getString(srvRelateMap, "RELATE_INFO_ID");
                flagSys = BasicCode.SECONDARY;
            }else {
                srvOrderIdRes = srvOrdId;
                flagSys = BasicCode.LOCALBUILD;
            }
            if (BasicCode.LOCAL_INSIDE_DISMANTLE_FLOW.equals(psId)) {  //本地局内电路拆机,在子流程结束时调用资源归档
                // 查询是否成功调过资源归档接口
                int num = orderDealDao.qryInterResult(srvOrderIdRes, "BusinessArchive");
                if (num < 1) {
                    params.put("flag", flagSys);
                    params.put("srvOrdId", srvOrderIdRes);
                    Map mapInner = businessArchiveServiceIntf.businessArchive(params);
                    if (!"成功".equals(MapUtils.getString(mapInner, "returncode"))) {
                        throw new Exception("派单失败!调用资源归档接口异常，异常原因：" + MapUtils.getString(mapInner, "returndec"));
                    }
                }
            }
            // 本地客户电路停复机流程 本地客户电路拆机流程 调用集客反馈接口，
            if (BasicCode.LOCAL_CUST_STOP_FLOW.equals(psId)
                    || BasicCode.LOCAL_CUST_DISMANTLE_FLOW.equals(psId)) {
                Map<String, Object> ifFromSecondaryMap = orderQrySecondaryDao.qryIfFromSecondary(parentOrderId);
                if(MapUtils.isEmpty(ifFromSecondaryMap)){ //非二干下发
                    Map<String, Object> orderDataMap = editDraftDao.getSrvOrdInfo(srvOrdId);
                    if ("jike".equals(MapUtils.getString(orderDataMap, "RESOURCES"))) {
                        // 集客来单，完工确认环节调用反馈接口
                        int numFinish = orderDealDao.qryInterResult(srvOrdId, "FinishOrder");
                        if (numFinish < 1) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("srvOrdId",srvOrdId);
                            Map finMap = finishOrderServiceIntf.finishOrder(map);
                            if (!"1".equals(MapUtils.getString(finMap, "RESP_CODE"))) {
                                throw new Exception("派单失败!调用集客反馈接口异常，异常原因：" + MapUtils.getString(finMap, "RESP_DESC"));
                            }
                        }
                    }
                }
            }
            FlowWoDTO woDTO = new FlowWoDTO();
            woDTO.setWoId(woOrderId);
            if (BasicCode.LOCAL_CUST_NEWOPEN_FLOW.equals(psId)
                    || BasicCode.CROSS_NEWOPEN_FLOW.equals(psId)
                    || BasicCode.LOCAL_INSIDE_NEWOPEN_FLOW.equals(psId)) {
                Map<String, String> paramsMap = new HashMap<String, String>();
                if (BasicCode.LOCAL_CUST_NEWOPEN_FLOW.equals(psId)) { // 本地电路新开变更移机 本地测试环节要多工单派发
                    paramsMap.put("isNeedNewRes", "1");
                    paramsMap.put("isCircuitDispatch", "1");
                    Map map = orderDealDao.qryOrderData(woOrderId);
                    String srcOrdId = MapUtils.getString(map, "SRV_ORD_ID");
                    String productInfoId = MapUtils.getString(map, "CODE_INFO_ID");
                    Map<String, Object> azAreaInfo = new HashMap<String, Object>();
                    if ("1".equals(productInfoId) || "2".equals(productInfoId)
                            || "8".equals(productInfoId)) {
                        // MV/DIA/语音中继产品本地测试只派Z
                        azAreaInfo = orderDealDao.qryZAreaInfo(srcOrdId);
                    } else {
                        azAreaInfo = orderDealDao.qryAZAreaInfo(srcOrdId);
                        // 如果az端受理区域一样，就只派一端
                        String azAreaStr = MapUtils.getString(azAreaInfo, "AZINFO");
                        String[] azArea = azAreaStr.split(",");
                        for (int i = 0; i < azArea.length - 1; i++) {
                            if (azArea[i].equals(azArea[i + 1])) {
                                azAreaInfo.put("AZINFO", azArea[i]);
                            }
                        }
                    }
                    paramsMap.put("AZINFO", MapUtils.getString(azAreaInfo, "AZINFO"));
                } else if (BasicCode.CROSS_NEWOPEN_FLOW.equals(psId)) { // 跨域电路
                    paramsMap.put("isNeedNewRes", "1");
                    paramsMap.put("isCircuitDispatch", "1");
                }
                List<HashMap<String, String>> operAttrsList = new ArrayList<HashMap<String, String>>();
                Iterator<String> iter = paramsMap.keySet().iterator();
                while (iter.hasNext()) {
                    HashMap<String, String> operAttrsMap = new HashMap<String, String>();
                    String key = iter.next();
                    String value = String.valueOf(paramsMap.get(key));
                    operAttrsMap.put("KEY", key);
                    operAttrsMap.put("VALUE", value);
                    operAttrsList.add(operAttrsMap);
                }
                woDTO.setOperAttrs(operAttrsList);
            }else if (BasicCode.CROSS_STOP_FLOW.equals(psId)){
                /**
                 * 跨域停闭电路
                 * 1，一干直接下发本地---子流程结束给一干报竣---调用资源归档接口
                 * 2，二干下发本地
                 *    判断数据来源：一干  二干
                 *    二干直接下发--这里是拆机 线条参数ifMainOffice=0
                 *    查询二干数据制作环节是否完成：
                 *      是：回单二干待数据制作和本地调度环节；同时调用集客反馈接口
                 *      否：不做处理
                 */
                Map<String, Object> ifFromSecondaryMap = orderQrySecondaryDao.qryIfFromSecondary(parentOrderId);
                if(MapUtils.isEmpty(ifFromSecondaryMap)){
                    Map<String, Object> reportMap = new HashMap<String, Object>();
                    reportMap.put("SrvOrdId", srvOrdId);
                    reportMap.put("tacheId", BasicCode.LOCAL_TEST);
                    reportMap.put("workId", woOrderId);
                    reportMap.put("username", MapUtils.getString(woOrder, "LOGIN_NAME"));
                    reportMap.put("mobiletel", MapUtils.getString(woOrder, "PHONE_NO"));
                    reportMap.put("email",MapUtils.getString(woOrder,"EMAIL"));
                    reportMap.put("fullname",MapUtils.getString(woOrder,"USER_NAME"));
                    reportMap.put("comments","");
                    Thread.sleep(1000);
                    Map<String, Object> reportResMap = feedbackInterface.soComplete(reportMap);
                    if ("fail".equals(MapUtils.getString(reportResMap, "flag"))) {
                        throw new Exception(MapUtils.getString(reportResMap, "msg"));
                    }
                    // 查询是否成功调过资源归档接口
                    int num = orderDealDao.qryInterResult(srvOrderIdRes, "BusinessArchive");
                    if (num < 1) {
                        params.put("flag", flagSys);
                        params.put("srvOrdId", srvOrderIdRes);
                        Map mapInner = businessArchiveServiceIntf.businessArchive(params);
                        if (!"成功".equals(MapUtils.getString(mapInner, "returncode"))) {
                            throw new Exception("派单失败!调用资源归档接口异常，异常原因：" + MapUtils.getString(mapInner, "returndec"));
                        }
                    }
                    // srv_ord表状态改为10F
                    orderDealDao.updateSrvOrdState(srvOrdId, "10F");
                } else {
                    /**
                     * 本地子流程结束回单二干调度判断条件修改：
                     * 1，先查询二干下发本地的电路，电路调度环节的状态；
                     * 2，再查询二干下发电路所启子流程，以及完成数量；
                     */
                    boolean ifSubmitSecOrder = true; //是否回单二干工单标识
                    List<Map<String, Object>> secToLocalOrderCircuitDispatchList = orderQrySecondaryDao.querySecToLocalOrderCircuitDispatch(MapUtils.getString(ifFromSecondaryMap,"SRV_ORD_ID"));
                    for (Map<String, Object> secToLocalOrderCircuitDispatch : secToLocalOrderCircuitDispatchList){
                        if (OrderTrackOperType.WO_ORDER_STATE_2.equals(MapUtils.getString(secToLocalOrderCircuitDispatch, "WO_STATE"))){
                            ifSubmitSecOrder = false; //如果电路调度环节工单状态还有处理中，不回单二干工单
                        }
                    }
                    if (ifSubmitSecOrder){
                        int localChildFlowAllNum = orderQrySecondaryDao.qryLocalChildFlowAllNum(MapUtils.getString(ifFromSecondaryMap,"PARENT_ORDER_ID"));
                        int localChildFlowFinishNum = orderQrySecondaryDao.qryLocalChildFlowFinishNum(MapUtils.getString(ifFromSecondaryMap,"PARENT_ORDER_ID"));
                        if(localChildFlowAllNum == localChildFlowFinishNum){
                            Map<String, Object> paramsMap = new HashMap<String, Object>();
                            paramsMap.put("parentOrderId", MapUtils.getString(ifFromSecondaryMap,"PARENT_ORDER_ID"));
                            boolean flag = listenerOrderServiceIntf.qryDataChildFlowSec(paramsMap);
                            //if (BasicCode.ONEDRY.equals(MapUtils.getString(ifFromSecondaryMap, ""))){}else
                            if (BasicCode.SECONDARY.equals(MapUtils.getString(ifFromSecondaryMap, "RESOURCES"))
                                    || BasicCode.JIKE.equals(MapUtils.getString(ifFromSecondaryMap, "RESOURCES"))){
                                List<HashMap<String, String>> operAttrsList = new ArrayList<HashMap<String, String>>();
                                HashMap<String, String> operAttrsMap = new HashMap<String, String>();
                                operAttrsMap.put("KEY", "ifMainOffice");
                                operAttrsMap.put("VALUE", "0");
                                operAttrsList.add(operAttrsMap);
                                paramsMap.put("operAttrsList", operAttrsList);
                            }
                            if (flag){
                                paramsMap.put("orderId", parentOrderId);
                                listenerOrderServiceIntf.finshDataAndSchedule(paramsMap);
                            }
                        }
                    }
                }
            }
            Map<String, Object> updateStateMap = new HashMap<String, Object>();
            updateStateMap.put("woState", OrderTrackOperType.WO_ORDER_STATE_2);
            updateStateMap.put("stateDate", new java.sql.Date(new java.util.Date().getTime()));
            // updateStateMap.put("dealDate", new java.sql.Date(new java.util.Date().getTime()));
            updateStateMap.put("woID", woOrderId);
            updateStateMap.put("staffId", "");
            orderDealDao.updateWoOrderState(updateStateMap);
            // 电路调度环节回单人修改
            String compUserId = MapUtils.getString(woOrder, "COMP_USER_ID");
            flowAction.complateWo(compUserId, woDTO);

            /**
             * 必须要电路调度回单完成之后才能调用回单二干待专业数据制作环节
             * 因为这里涉及本地单子的归档  千万不能改
             * 改了也是自己坑自己，自闭了。。。。。。。。。。。。
             */
            if (BasicCode.LOCAL_CUST_STOP_FLOW.equals(psId)){ //本地客户停复机电路
                Map<String, Object> ifFromSecondaryMap = orderQrySecondaryDao.qryIfFromSecondary(parentOrderId);
                if(MapUtils.isNotEmpty(ifFromSecondaryMap)){
                    /**
                     * 本地子流程结束回单二干调度判断条件修改：
                     * 1，先查询二干下发本地的电路，电路调度环节的状态；
                     * 2，再查询二干下发电路所启子流程，以及完成数量；
                     */
                    boolean ifSubmitSecOrder = true; //是否回单二干工单标识
                    List<Map<String, Object>> secToLocalOrderCircuitDispatchList = orderQrySecondaryDao.querySecToLocalOrderCircuitDispatch(MapUtils.getString(ifFromSecondaryMap,"SRV_ORD_ID"));
                    for (Map<String, Object> secToLocalOrderCircuitDispatch : secToLocalOrderCircuitDispatchList){
                        if (OrderTrackOperType.WO_ORDER_STATE_2.equals(MapUtils.getString(secToLocalOrderCircuitDispatch, "WO_STATE"))){
                            ifSubmitSecOrder = false; //如果电路调度环节工单状态还有处理中，不回单二干工单
                        }
                    }
                    if (ifSubmitSecOrder){
                        int localChildFlowAllNum = orderQrySecondaryDao.qryLocalChildFlowAllNum(MapUtils.getString(ifFromSecondaryMap,"PARENT_ORDER_ID"));
                        int localChildFlowFinishNum = orderQrySecondaryDao.qryLocalChildFlowFinishNum(MapUtils.getString(ifFromSecondaryMap,"PARENT_ORDER_ID"));
                        if(localChildFlowAllNum == localChildFlowFinishNum){
                            Map<String, Object> secMap = new HashMap<String, Object>();
                            secMap.put("parentOrderId", MapUtils.getString(ifFromSecondaryMap,"PARENT_ORDER_ID"));
                            boolean flag = listenerOrderServiceIntf.qryDataChildFlowSec(secMap);
                            if (flag){
                                secMap.put("orderId", parentOrderId);
                                listenerOrderServiceIntf.finshDataAndSchedule(secMap);
                                if ("jike".equals(MapUtils.getString(ifFromSecondaryMap, "RESOURCES"))){
                                    // 如果是集客来单，回单后调用反馈接口
                                    int numFinish = orderDealDao.qryInterResult(srvOrdId, "FinishOrder");
                                    if (numFinish < 1) {
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("srvOrdId",srvOrdId);
                                        Map finMap = finishOrderServiceIntf.finishOrder(map);
                                        if (!"1".equals(MapUtils.getString(finMap, "RESP_CODE"))) {
                                            throw new Exception("派单失败!调用集客反馈接口异常，异常原因：" + MapUtils.getString(finMap, "RESP_DESC"));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            /**
             * 这里用于子流程结束主流程本地测试发送短信通知
             */
            orderIdList.add(parentOrderId);
            logger.info("开始准备发送短信。。。。。。。。。。。。。。。。。。。。");
            if(null != ThreadLocalInfoHolder.getLoginUser()){
                String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
                Map<String, Object> sendMsgMap = new HashMap<String, Object>();
                sendMsgMap.put("operStaffId", operStaffId);
                sendMsgMap.put("orderIdList", orderIdList);
                sendMsgMap.put("operAction", "submit");
                orderSendMsgService.sendMsgBefore(sendMsgMap);
            }
            // 起线程调用资源接口更新全程报竣时间、起止租时间
            Map<String,Object> resParams = new HashMap<>();
            resParams.put("psId",psId);
            resParams.put("tacheId",BasicCode.CHILDFLOW_END);
            resParams.put("systemResource",systemResource);
            resParams.put("srvOrdId",srvOrderIdRes);
            resCfsAttrUpdateServiceIntf.resAttrUpdate(resParams,null);
        } catch (Exception e) {
            Map<String, Object> srvMap = orderDealDao.qrysrvOrdIdByorderId(parentOrderId);
            String srvOrdId = MapUtils.getString(srvMap, "SRV_ORD_ID");
            // 查询是否成功调过资源归档接口
            int numHole = orderDealDao.qryInterResult(srvOrdId, "BusinessArchive");
            if (numHole > 0) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置日期格式
                Map<String, Object> rmap = new HashMap<String, Object>();
                rmap.put("srv_ord_id", srvOrdId);
                rmap.put("attr_code", "1");
                rmap.put("attr_name", "");
                rmap.put("attr_value", srvOrdId);
                rmap.put("create_date", df.format(new Date()));
                rmap.put("sourse", "");
                rmap.put("attr_action", "BusinessArchive");
                rmap.put("attr_value_name", "业务实例归档接口返回结果");
                webServiceDao.saveRetInfo(rmap);
                webServiceDao.copyInterfInfo(srvOrdId, "业务实例归档接口");
            }

            logger.error("OrderFinishedEvent updateOrderAttr failed" + e.getMessage(), e);
            throw new RuntimeException("OrderFinishedEvent updateOrderAttr failed" + e.getMessage());
        }
    }

    @Override
    public void sumCompleteReceiptTache(Map<String, Object> paramsMap) {
        Map<String, Object> resMapSec = new HashMap<String, Object>();
        String orderId = MapUtils.getString(paramsMap, "orderId");
        Map<String, Object> srvOrdIdMap = orderDealDao.qrysrvOrdIdSecondByorderId(orderId);
        String srvOrdId = MapUtils.getString(srvOrdIdMap, "SRV_ORD_ID");
        String activeType = MapUtils.getString(srvOrdIdMap, "ACTIVE_TYPE");
        if(!BasicCodeEnum.UNPACK.getValue().equals(activeType)){
            resMapSec.put("SRV_ORD_ID",srvOrdId);
            List<String> relateListT = Lists.newArrayList();
//            int num = orderDealDao.qryInterResult(srvOrdId, "ResBusinessCreate");
//            if (num < 1) {
//                // 电路调度环节调用资源创建接口
//                resMapSec.put("flag", "localBuild");
//                resMapSec.put("srvOrdId", srvOrdId);
//                resMapSec.put("srvOrdIdOld", srvOrdId);
//                Map retmap = businessCreateServiceIntf.businessCreate(resMapSec);
//                if (!"成功".equals(MapUtils.getString(retmap, "returncode"))) {
//                    throw new RuntimeException(MapUtil.getString(retmap,"message"));
//                }
//            }
        //    List<String> relateListRe = orderQrySecondaryDao.qryRelateInfoIdBySrvordId(srvOrdId);
            List<String> relateListRe = orderQrySecondaryDao.qryRelateInstanceIdBySrvordId(srvOrdId);
            if(!CollectionUtils.isEmpty(relateListRe)){
                relateListT.addAll(relateListRe);
            }
            resMapSec.put("relaCrmOrderCodes",relateListT);
            Map<String, Object> secondaryMap = orderDealServiceIntf.sendSecondScheduleLTResAssign("localBuild",resMapSec);
            if(!MapUtil.getBoolean(secondaryMap,"success")){
                throw new RuntimeException(MapUtil.getString(secondaryMap,"message"));
            }

        }

    }

    @Override
    public void finshStartStopRent(Map<String, Object> paramsMap) throws Exception {
        String operStaffId = "-1";
        String woId = MapUtils.getString(paramsMap, "woId");
        String orderId = MapUtils.getString(paramsMap, "orderId");
        Map<String, String> cstOrderDataMap = orderDealDao.qryCstOrderData(orderId);
        String srvOrdId = MapUtils.getString(cstOrderDataMap, "SRV_ORD_ID");
        Map<String, Object> params = new HashMap<String, Object>();
        // 查询是否成功调过资源归档接口
        int num = orderDealDao.qryInterResult(srvOrdId, "BusinessArchive");
        if (num < 1) {
            params.put("flag", BasicCode.LOCALBUILD);
            params.put("srvOrdId", srvOrdId);
            Map mapInner = businessArchiveServiceIntf.businessArchive(params);
            if (!"成功".equals(MapUtils.getString(mapInner, "returncode"))) {
                logger.info(">>>>>>>>>>>>>>>调用资源归档接口失败>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                throw new Exception("派单失败!调用资源归档接口异常，异常原因：" + MapUtils.getString(mapInner, "returndec"));
            }
        }
        //修改业务订单状态为10F
        orderDealDao.updateSrvOrdState(srvOrdId, "10F");
        FlowWoDTO woDTO = new FlowWoDTO();
        woDTO.setWoId(woId);
        flowAction.complateWo(operStaffId, woDTO);
        //更新业务订单状态
        orderDealDao.updateSrvOrdState(srvOrdId,"10F");
       /* Map<String, Object> paramsMapRent = new HashMap<String, Object>();
        paramsMapRent.put("orderId",orderId);
        paramsMapRent.put("woId",woId);
        paramsMapRent.put("action","回单");
        paramsMapRent.put("remark","起租环节回单");
        paramsMapRent.put("operType",OrderTrackOperType.OPER_TYPE_4);
        listenerOrderServiceIntf.insertTacheLog(paramsMapRent);*/
        Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(11);
        Map<String, Object> logDataMap = new HashMap<String, Object>();
        logDataMap.put("woId", woId);
        logDataMap.put("orderId", orderId);
        logDataMap.put("remark", "起租环节回单");
        logDataMap.put("tacheId", MapUtils.getString(params, "tacheId"));
        logDataMap.put("operType", OrderTrackOperType.OPER_TYPE_4);
        logDataMap.put("operStaffInfoMap", operStaffInfoMap);
        logDataMap.put("action", "回单");
        logDataMap.put("trackMessage", "[回单]");
        tacheDealLogIntf.addTrackLog(logDataMap);
    }

    /*@Override
    public void insertTacheLog(Map<String, Object> params) {
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        String orderId = MapUtils.getString(params, "orderId");
        String woId = MapUtils.getString(params, "woId");
        String operStaffName = MapUtil.getString(params, "operStaffName");
        String action = MapUtil.getString(params, "action"); //操作
        String remark = MapUtil.getString(params, "remark"); //备注
        String operType = MapUtil.getString(params, "operType"); //操作类型 OrderTrackOperType
        paramsMap.put("orderId",orderId);
        paramsMap.put("woOrdId", woId);
        paramsMap.put("trackOrgId", MapUtil.getString(params, "ORG_ID"));
        paramsMap.put("trackOrgName", MapUtil.getString(params, "ORG_NAME"));
        paramsMap.put("trackDate", new java.sql.Date(new java.util.Date().getTime()));
        paramsMap.put("createDate", new java.sql.Date(new java.util.Date().getTime()));
        paramsMap.put("trackStaffId", MapUtil.getString(params, "operStaffId"));
        paramsMap.put("trackStaffName", "系统管理员");
        paramsMap.put("trackStaffPhone", MapUtils.getString(params, "USER_PHONE"));
        paramsMap.put("trackStaffEmail", MapUtils.getString(params, "USER_EMAIL"));
        String trackMessage = "[" + operStaffName + "将工单单号：" + woId + "][" + action + "]";
        paramsMap.put("trackMessage", trackMessage);
        String trackContent = "[" + action + "]";
        if (remark != null ){
            trackContent = trackContent + remark ;
        }
        paramsMap.put("trackContent", trackContent);
        paramsMap.put("operType", operType);
        orderDealDao.insertTrackLogInfo(paramsMap);

    }*/

    @Override
    public void startStopRent(String orderId, String woId) {
        Map<String, String> cstOrderDataMap = orderDealDao.qryCstOrderData(orderId);
        String resources = MapUtils.getString(cstOrderDataMap, "RESOURCES"); //单子来源
        String srvOrdId = MapUtils.getString(cstOrderDataMap, "SRV_ORD_ID");
        if (!BasicCode.JIKE.equals(resources)){
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("orderId",orderId);
            params.put("woId",woId);
            try {
                listenerOrderServiceIntf.finshStartStopRent(params);
            }catch(Exception e){
                logger.error("=====调用资源归档接口异常=====" + e.getMessage(), e);
                throw new RuntimeException("派单失败!调用资源归档接口异常，异常原因：" + e.getMessage());
            }
        }else {
            // 查询集客是否会下发起止租通知 如果没有值的话直接回单起租环节
            int noRentNum = orderDealDao.qryNoRentNum(srvOrdId);
            if (noRentNum > 0){
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("orderId",orderId);
                params.put("woId",woId);
                try {
                    listenerOrderServiceIntf.finshStartStopRent(params);
                }catch(Exception e){
                    logger.error("=====调用资源归档接口异常=====" + e.getMessage(), e);
                    throw new RuntimeException("派单失败!调用资源归档接口异常，异常原因：" + e.getMessage());
                }
            }
        }
    }

    @Override
    public void finshRent(Map<String, Object> paramsMap) {
        String woId = MapUtils.getString(paramsMap, "woId");
        String orderId = MapUtils.getString(paramsMap, "orderId");
        Map<String, Object> srvOrdIndMap = orderDealDao.qrysrvOrdIdByorderId(orderId);
        String srvOrdId = MapUtil.getString(srvOrdIndMap, "SRV_ORD_ID");
        //Map<String, String> cstOrderDataMap = orderDealDao.qryCstOrderData(orderId);
        String operStaffId = "-1";
        List<Map<String, Object>> localOrderList = new ArrayList<Map<String, Object>>();
        /*//先判断是否为拆机电路
        if("102".equals(MapUtils.getString(cstOrderDataMap, "ACTIVE_TYPE"))){
            localOrderList = orderQrySecondaryDao.qrySecToLocalChildFlowDate(orderId);
            operStaffId = "-2000";
        }else {
            localOrderList =  orderQrySecondaryDao.qryRentTacheOrder(orderId);
            operStaffId = "-1";
        }*/
        //这里只会有停复机
        localOrderList =  orderQrySecondaryDao.qryRentTacheOrder(orderId);
        for (int i = 0; i < localOrderList.size() ; i++) {
            String relate_info_id = MapUtils.getString(localOrderList.get(i), "RELATE_INFO_ID");
            Map<String, Object> resLocalMap = new HashMap<String, Object>();
            resLocalMap.put("relate_info_id",relate_info_id);
            Map<String, Object> stringLocalSchMap = orderDealServiceIntf.sendlocalScheduleLTRentRes(resLocalMap);
            if(!MapUtil.getBoolean(stringLocalSchMap,"success")){
                throw new RuntimeException(MapUtil.getString(stringLocalSchMap,"message"));
            }
            FlowWoDTO woDTO = new FlowWoDTO();
            woDTO.setWoId(MapUtils.getString(localOrderList.get(i),"WO_ID"));
            flowAction.complateWo(operStaffId, woDTO);
            Map<String, Object> paramsMapRentInner = new HashMap<String, Object>();
            paramsMapRentInner.put("orderId",MapUtils.getString(localOrderList.get(i),"ORDER_ID"));
            paramsMapRentInner.put("woId",MapUtils.getString(localOrderList.get(i),"WO_ID"));
            paramsMapRentInner.put("action","回单");
//          paramsMapRentInner.put("remark","起租确认回单");
            paramsMapRentInner.put("operType",OrderTrackOperType.OPER_TYPE_4);
            orderDealServiceIntf.insertTacheLog(paramsMapRentInner);
        }
        Map<String, Object> resFfirMap = new HashMap<String, Object>();
        resFfirMap.put("srvOrdId",srvOrdId);
        Map<String, Object> secondScheduleLMap = orderDealServiceIntf.sendSecondScheduleLTRes(resFfirMap);
        if(!MapUtil.getBoolean(secondScheduleLMap,"success")){
            throw new RuntimeException(MapUtil.getString(secondScheduleLMap,"message"));
        }
        FlowWoDTO woDTO = new FlowWoDTO();
        woDTO.setWoId(woId);
        flowAction.complateWo(operStaffId, woDTO);
        //更新业务订单状态
        orderDealDao.updateSrvOrdState(srvOrdId,"10F");
        Map<String, Object> paramsMapRent = new HashMap<String, Object>();
        paramsMapRent.put("orderId",orderId);
        paramsMapRent.put("woId",woId);
        paramsMapRent.put("action","回单");
//      paramsMapRent.put("remark","起租确认回单");
        paramsMapRent.put("operType",OrderTrackOperType.OPER_TYPE_4);
        orderDealServiceIntf.insertTacheLog(paramsMapRent);
    }

    @Override
    public void resSupplementToOrder(Map<String, Object> paramsMap) {
        Map<String, Object> resMap = new HashMap<String, Object>();
        String tacheId = MapUtils.getString(paramsMap, "tacheId");
        String tacheCode = MapUtils.getString(paramsMap, "tacheCode");
        Map<String, Object> tacheInfo = resourceInitiateDao.qryTacheInfo(tacheId);
        String tacheName = MapUtils.getString(tacheInfo, "TACHE_NAME");
        TacheWoOrder tacheWoOrder = new TacheWoOrder(Integer.parseInt(tacheId), tacheCode, tacheName, paramsMap);
        String beanName = tacheWoOrder.getBeanNameByTacheCode();
        DealTacheWoOrderIntf dealTacheWoOrder = SpringContextHolderUtil.getBean(beanName);
        try{
            resMap = dealTacheWoOrder.tacheDoSomething(paramsMap);
        }catch (Exception e){
            logger.error("派单失败：", e);
            logger.info("--------------资源补录模块------------子流程启动失败-----------------------");
            throw new RuntimeException("----------资源补录模块----子流程启动失败:" + MapUtil.getString(resMap,"message"));
        }
    }

    @Override
    public void resAllocateAuto(Map<String, Object> paramsMap){
        String orderId = MapUtils.getString(paramsMap, "orderId");
        String woId = MapUtils.getString(paramsMap, "woId");
        Map<String, Object> srvOrdIndMap = orderDealDao.querySrvInfoByWoId(woId);
        //1.准备数据
        String activeType = MapUtils.getString(srvOrdIndMap, "ACTIVE_TYPE");
        String serviceId = MapUtils.getString(srvOrdIndMap, "SERVICE_ID");
        String srvOrdId = MapUtils.getString(srvOrdIndMap, "SRV_ORD_ID");
        String cstOrdID = MapUtils.getString(srvOrdIndMap, "CST_ORD_ID");
        String specialtyCode = MapUtils.getString(srvOrdIndMap, "SPECIALTY_CODE");
        String tardeType = MapUtils.getString(srvOrdIndMap, "TRADE_TYPE_CODE");
        String subType = orderDealDao.qrySubType(srvOrdId);
        boolean bussSpecialtyLine = false;
        if(subType != null && "5".equals(subType)){
            bussSpecialtyLine = true; //商务专线
        }

        //2.判断是否符合条件派发省内号线---互联网专线（商务专线）接入专业资源自动分配--新开、移机、拆机
        if (BasicCode.DIA_SERVICEID.equals(serviceId)){
            if (bussSpecialtyLine  && "101,102,106".indexOf(activeType)!= -1 && BasicCode.ACCESS_6.equals(specialtyCode) ){
                Map<String, Object> logMap = new HashMap<>();
                logMap.put("WO_ID",woId);
                logMap.put("ORDER_ID",orderId);
                logMap.put("operType", OrderTrackOperType.OPER_TYPE_4);
                Map<String, Object> params = new HashMap<>();
                Map<String, Object> circuitDataMap = new HashMap<>();
                params.put("SRV_ORD_ID",srvOrdId);
                params.put("WO_ID",woId);
                params.put("CST_ORD_ID",cstOrdID);
                params.put("TRADE_TYPE_CODE",tardeType);
                Map<String, Object> retMap = provinceResOrderService.submitOrderToProvinceAuto(params);
                if ((Boolean)MapUtils.getObject(retMap, "success")){//派单成功，进行自动回单，
                    FlowWoDTO woChildDTO = new FlowWoDTO();
                    List<HashMap<String, String>> operAttrsList = new ArrayList<>();
                    woChildDTO.setOperAttrs(operAttrsList);
                    woChildDTO.setWoId(woId);
                    flowAction.complateWo("13", woChildDTO);
                    logMap.put("remark",MapUtils.getObject(retMap, "message"));
                    //修改工单处理人为省内号线资源系统
                    orderDealDao.updateDealUserIdByWoId(woId, "13");
                    addLog(logMap);
                }
            }
        }
    }

    /**
     * 调用省份IP智能网管系统派单接口
     * @author wangsen
     * @date 2020/10/16 11:32
     * @return
     */
    public void sendOrder(Map<String, Object> map) {
        map.put("type", "listener"); //监听激活
        provinceSendOrderServiceIntf.sendOrder(map);
    }

    @Override
    public void dataMakeAuto(Map<String, Object> paramsMap) {
        String orderId = MapUtils.getString(paramsMap, "orderId");
        String woId = MapUtils.getString(paramsMap, "woId");
        Map<String, Object> woMap = orderQueryDao.qryWoInfo(woId);
        String specialtyCode = MapUtils.getString(woMap, "SPECIALTY_CODE");
        // List<Map<String, Object>> list = orderDealDao.queryProdId(orderId);
        Map<String, Object> parentOrder = orderDealDao.getParentOrder(orderId); //查询是否存在父订单
        if (MapUtils.isNotEmpty(parentOrder) && !"".equals(MapUtils.getString(parentOrder, "PARENTORDERID"))) {
            orderId = MapUtils.getString(parentOrder, "PARENTORDERID");
        }
        Map<String, String> cstOrderDataMap = new HashMap<String, String>();
        Map<String, Object> ifFromSecondaryMap = orderQrySecondaryDao.qryIfFromSecondary(orderId);
        if (!MapUtils.isEmpty(ifFromSecondaryMap)) {
            //二干下发
            cstOrderDataMap = orderDealDao.qryCstOrderDataFromSec(orderId);
        } else {
            cstOrderDataMap = orderDealDao.qryCstOrderData(orderId);
        }
        if (MapUtils.isNotEmpty(cstOrderDataMap)) {
            String srvOrdId = MapUtils.getString(cstOrderDataMap, "SRV_ORD_ID");
            String prod_id =  MapUtils.getString(cstOrderDataMap, "SERVICE_ID");
            String change_flag = MapUtils.getString(cstOrderDataMap, "CHANGE_FLAG");
            String active_type = MapUtils.getString(cstOrderDataMap, "ACTIVE_TYPE");
            if (BasicCode.DIA_SERVICEID.equals(prod_id) && BasicCode.DATA_4.equals(specialtyCode)) {
                Set<String> actTypeSet = new HashSet<>(); //动作，变更除外
                actTypeSet.add(BasicCode.ACTIVE_TYPE_NEWOPEN); // 101 新开
                actTypeSet.add(BasicCode.ACTIVE_TYPE_DISMANTLE); //102 拆机
                actTypeSet.add(BasicCode.ACTIVE_TYPE_STOP); //104 停机
                actTypeSet.add(BasicCode.ACTIVE_TYPE_BACK); //105 复机
                actTypeSet.add(BasicCode.ACTIVE_TYPE_REMOVE); //106 移机
                if ( actTypeSet.contains(active_type)) { //互联网专线(DIA) 非变更动作时
                    paramsMap.put("woId", woId);
                    paramsMap.put("srvOrdId", srvOrdId);
                    listenerOrderServiceIntf.sendOrder(paramsMap);
                }
                if ("103".equals(active_type)) { //TODO 互联网专线(DIA) 变更动作时
                    Set<String> changeSet = new HashSet<>(); // 变更明细
                    changeSet.add("1020"); //升速
                    changeSet.add("1021"); //降速
                    changeSet.add("1022"); //IP地址变更
                    changeSet.add("1025"); //其他业务属性变更开通80端口
                    if (changeSet.contains(change_flag)) {
                        paramsMap.put("woId", woId);
                        paramsMap.put("srvOrdId", srvOrdId);
                        listenerOrderServiceIntf.sendOrder(paramsMap);
                    }
                }
            }
        }
    }



    @Override
    /**
     * 互联网专线商务专线，数据专业子流程
     * 工单流转到数据专业数据制作环节 ，修改工单状态为待回单
     */
    public void updateWoState(Map<String, Object> params){
        String orderId = MapUtils.getString(params, "orderId");
        String woId = MapUtils.getString(params, "woId");
        Map<String, Object> srvOrdIndMap = orderDealDao.querySrvInfoByWoId(woId);
        String tacheId = MapUtils.getString(srvOrdIndMap, "TACHE_ID");
        String activeType = MapUtils.getString(srvOrdIndMap, "ACTIVE_TYPE");
        String serviceId = MapUtils.getString(srvOrdIndMap, "SERVICE_ID");
        String srvOrdId = MapUtils.getString(srvOrdIndMap, "SRV_ORD_ID");
        String specialtyCode = MapUtils.getString(srvOrdIndMap, "SPECIALTY_CODE");
        Set<String> specSet = new HashSet<>();
        specSet.add(BasicCode.ACCESS_6);
        if (BasicCode.DIA_SERVICEID.equals(serviceId) && BasicCode.DATA_MAKE.equals(tacheId)
                && ("101,102,106".indexOf(activeType)!= -1) && specSet.contains(specialtyCode)){
            String subType = orderDealDao.qrySubType(srvOrdId);
            // 判断产品子类型是否是商务专线
            if (subType != null && "5".equals(subType)){
                    orderDealDao.updateWoStateByWoId(woId, OrderTrackOperType.WO_ORDER_STATE_18);
            }
        }
    }

    //记录日志
    public void addLog(Map<String, Object> map){
        String operType =  MapUtils.getString(map,"operType");
        String action = OrderTrackOperType.OPER_TYPE_4.equals(operType) ? "回单":"退单";
        Map<String, Object> paramsMapRentInner = new HashMap<String, Object>();
        paramsMapRentInner.put("orderId",MapUtils.getString(map,"ORDER_ID"));
        paramsMapRentInner.put("woId",MapUtils.getString(map,"WO_ID"));
        paramsMapRentInner.put("action",action);
        paramsMapRentInner.put("operType",operType);
        paramsMapRentInner.put("operStaffName","省内号线资源系统");
        paramsMapRentInner.put("remark",MapUtils.getString(map,"REMARK"));
        orderDealServiceIntf.insertTacheLog(paramsMapRentInner);
    }

    @Override
    public void modifyResWoState(Map<String, Object> paramsMap) {
        String orderId = MapUtils.getString(paramsMap, "orderId");
        String woId = MapUtils.getString(paramsMap, "woId");
        Map<String, Object> srvOrdIndMap = orderDealDao.querySrvInfoByWoId(woId);
        //1.准备数据
        String serviceId = MapUtils.getString(srvOrdIndMap, "SERVICE_ID");
        String srvOrdId = MapUtils.getString(srvOrdIndMap, "SRV_ORD_ID");
        String subType = orderDealDao.qrySubType(srvOrdId);
        boolean bussSpecialtyLine = false;
        if(subType != null && "5".equals(subType)){
            bussSpecialtyLine = true; //商务专线
        }
        //2.判断是否符合条件派发省内号线---互联网专线（商务专线）接入专业资源自动分配--新开、移机、拆机
        if (BasicCode.DIA_SERVICEID.equals(serviceId)){
            //修改后置资源状态
            fowordResService.modifyResWoState(orderId, bussSpecialtyLine);
        }
    }
}
