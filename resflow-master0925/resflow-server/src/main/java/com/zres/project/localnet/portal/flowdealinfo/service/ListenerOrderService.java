package com.zres.project.localnet.portal.flowdealinfo.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.common.collect.Lists;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderSendMsgDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCodeEnum;
import com.zres.project.localnet.portal.flowdealinfo.service.entry.TacheWoOrder;
import com.zres.project.localnet.portal.logInfo.entry.ToKafkaTacheLog;
import com.zres.project.localnet.portal.logInfo.service.ResInterfaceLogger;
import com.zres.project.localnet.portal.logInfo.until.LoggerThreadPool;
import com.zres.project.localnet.portal.resourceInitiate.data.dao.ResourceInitiateDao;
import com.zres.project.localnet.portal.util.SpringContextHolderUtil;
import com.zres.project.localnet.portal.webservice.interfaceJiKe.FinishOrderServiceIntf;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderQrySecondaryDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.initApplOrderDetail.service.GetEnumService;
import com.zres.project.localnet.portal.listener.util.EnmuValueUtil;
import com.zres.project.localnet.portal.logInfo.service.TacheDealLogIntf;
import com.zres.project.localnet.portal.util.OrderTrackOperType;

import com.ztesoft.res.frame.core.util.ListUtil;
import com.ztesoft.res.frame.core.util.MapUtil;
import com.ztesoft.res.frame.flow.common.dto.FlowWoDTO;
import com.ztesoft.res.frame.flow.common.intf.FlowAction;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;

@Service
public class ListenerOrderService implements ListenerOrderServiceIntf {

    Logger logger = LoggerFactory.getLogger(ListenerOrderService.class);

    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private OrderQrySecondaryDao orderQrySecondaryDao;
    @Autowired
    private OrderDealServiceIntf orderDealServiceIntf;
    @Autowired
    private GetEnumService getEnumService;
    @Autowired
    private FlowAction flowAction;
    @Autowired
    private ListenerOrderServiceIntf listenerOrderServiceIntf;
    @Autowired
    private OrderSendMsgService orderSendMsgService;
    @Autowired
    private TacheDealLogIntf tacheDealLogIntf;
    @Autowired
    private FinishOrderServiceIntf finishOrderServiceIntf;
    @Autowired
    private ResourceInitiateDao resourceInitiateDao;
    @Autowired
    private OrderSendMsgDao orderSendMsgDao;
    @Autowired
    private ResInterfaceLogger resInterfaceLogger;

    @Override
    public Map<String, Object> startResConfig(Map<String, Object> paramsMap) {
        String woId = MapUtils.getString(paramsMap, "woId");
        String orderId = MapUtils.getString(paramsMap, "orderId");
        paramsMap = orderDealServiceIntf.createChildOrder(orderId, woId, BasicCode.MAIN_DISPATCH_CUST_CLD, BasicCode.SPECIALTY);
        return paramsMap;
    }

    @Override
    public Map<String, Object> startData(Map<String, Object> paramsMap) {
        String woId = MapUtils.getString(paramsMap, "woId");
        String orderId = MapUtils.getString(paramsMap, "orderId");
        paramsMap = orderDealServiceIntf.createChildOrder(orderId, woId, BasicCode.DATA_PRODUCTION_CHILD, BasicCode.NETMANAGE);
        return paramsMap;
    }

    @Override
    public Map<String, Object> startResConstruction(Map<String, Object> paramsMap) {
        String woId = MapUtils.getString(paramsMap, "woId");
        String orderId = MapUtils.getString(paramsMap, "orderId");
        paramsMap = orderDealServiceIntf.createChildOrder(orderId, woId, BasicCode.RES_CONSTRUCTION_CHILD, BasicCode.RESCONSTRUCTION);
        return paramsMap;
    }

    @Override
    public Map<String, Object> startSchedule(Map<String, Object> paramsMap) {
        String orderId = MapUtils.getString(paramsMap, "orderId");
        Map<String, Object> srvOrderData = orderDealDao.qrySrvOrdData(orderId);
        String activeType = MapUtils.getString(srvOrderData, "ACTIVE_TYPE");
        String codeType = "";
        if ("104,105".indexOf(activeType) != -1){ //停机复机
            codeType = "flow_local";
        }else if ("101,103,102".indexOf(activeType) != -1){ //新开变更拆机
            codeType = "flow_cross";
        }
        Map<String, Object> queryProcessMap = new HashMap<String, Object>();
        queryProcessMap.put("codeType", codeType); //流程类型 写死为内部流程
        queryProcessMap.put("codeContent", MapUtils.getString(srvOrderData, "ORDER_TYPE")); //订单类型开通单
        queryProcessMap.put("codeTypeName", MapUtils.getString(srvOrderData, "SERVICE_ID")); //产品编码
        queryProcessMap.put("codeValue", activeType); //操作类型
        try{
            //查询流程参数
            List<Map<String, Object>> services = getEnumService.queryProcessInst(queryProcessMap);
            if (CollectionUtils.isEmpty(services)) {
                logger.error("二干调度监听本地调度环节到单监听器--->流程参数为空。");
                paramsMap.put("success", false);
                paramsMap.put("message", "二干调度监听本地调度环节到单监听器--->流程参数为空。");
                return paramsMap;
            }
            String procesParam = services.get(0).get("SORT_NO").toString();
            Map<String, Object> creaOrderMap = new HashMap<String, Object>();
            creaOrderMap.put("ORDER_CONTENT", MapUtils.getString(srvOrderData, "SERIAL_NUMBER"));
            creaOrderMap.put("AREA", "350002000000000042766408"); //全国
            creaOrderMap.put("ORDER_TITLE", MapUtils.getString(srvOrderData, "TRADE_ID"));
            creaOrderMap.put("ordPsid", procesParam); //流程id
            //受理区域
            Map<String, Object> acceptAreaMap = new HashMap<String, Object>();
            acceptAreaMap.put("ACT_TYPE",MapUtils.getString(srvOrderData, "ACTIVE_TYPE") + procesParam); //动作类型+流程规格
            acceptAreaMap.put("PRODUCT_TYPE",MapUtils.getString(srvOrderData, "SERVICE_ID")); //产品编码
            //creaOrderMap.put("parentOrderId", orderId);
            if (!"".equals(MapUtils.getString(srvOrderData, "MASTER_REGION")) && MapUtils.getString(srvOrderData, "MASTER_REGION") != null){
                //先判断二干下发的区域在本地网有没有执行中的订单
                String toLocalchildOrderState = orderQrySecondaryDao.qryToLocalChildOrderState(orderId,MapUtils.getString(srvOrderData, "MASTER_REGION"));
                if (StringUtils.isEmpty(toLocalchildOrderState) || !"200000002".equals(toLocalchildOrderState)){
                    acceptAreaMap.put("REGION_ID", MapUtils.getString(srvOrderData, "MASTER_REGION")); //受理区域
                    acceptAreaMap.put("ORDER_ID", orderId); //二干orderid
                    creaOrderMap.put("attr", acceptAreaMap);
                    creaOrderMap.put("requFineTime", MapUtils.getString(srvOrderData, "MASTER_REQ_TIME")); //要求完成时间
                    Map orderData = orderDealServiceIntf.createOrder(creaOrderMap);
                    srvOrderData.put("ORDER_ID", MapUtils.getString(orderData, "orderId"));
                    srvOrderData.put("REGION_ID", MapUtils.getString(srvOrderData, "MASTER_REGION"));
                    srvOrderData.put("INSTANCE_ID_EG", MapUtils.getString(srvOrderData, "INSTANCE_ID") + MapUtils.getString(srvOrderData, "MASTER_REGION"));
                    srvOrderData.put("PARENT_ORDER_ID", orderId);
                    srvOrderData.put("RESOURCES", MapUtils.getString(srvOrderData, "RESOURCES"));
                    srvOrderData.put("IFMAINORG", 0); //主调
                    srvOrderData.put("CONFIGSTATE", "10E");
                    /**
                     * 二干退本地  关联表的关联关系要先查询如果有更新，如果没有插入
                     */
                    int secLocalRelateNum = orderDealServiceIntf.selectSecLocalRelate(srvOrderData);
                    if (secLocalRelateNum > 0){
                        orderDealServiceIntf.updateSecLocalRelate(srvOrderData);
                    }else {
                        orderDealServiceIntf.insertSecLocalRelate(srvOrderData);
                    }
                    this.secToLocalMsg(MapUtils.getString(orderData, "orderId"),
                            MapUtils.getString(srvOrderData, "MASTER_REGION"));
                    //下发本地的单子推送日志
                    ToKafkaTacheLog toKafkaWoCreateTacheLog = new ToKafkaTacheLog();
                    toKafkaWoCreateTacheLog.setBase_order_id(MapUtils.getString(orderData, "orderId"));
                    toKafkaWoCreateTacheLog.setSys_resouce("0");
                    LoggerThreadPool.addLoggerToExecute(resInterfaceLogger, toKafkaWoCreateTacheLog);
                }
            }
            if (!"".equals(MapUtils.getString(srvOrderData, "ASSIST_REGION")) && MapUtils.getString(srvOrderData, "ASSIST_REGION") != null){
                String assistRegion = MapUtils.getString(srvOrderData, "ASSIST_REGION");
                if(!StringUtils.isEmpty(assistRegion)){
                    String[] regionIdAry = assistRegion.split(",");
                    for (int i = 0; i < regionIdAry.length; i++) {
                        //先判断二干下发的区域在本地网有没有执行中的订单
                        String toLocalchildOrderState = orderQrySecondaryDao.qryToLocalChildOrderState(orderId,regionIdAry[i]);
                        if (!StringUtils.isEmpty(toLocalchildOrderState) && "200000002".equals(toLocalchildOrderState)){
                            continue;
                        }
                        acceptAreaMap.put("REGION_ID", regionIdAry[i]); //受理区域
                        acceptAreaMap.put("ORDER_ID", orderId); //二干orderid
                        creaOrderMap.put("attr", acceptAreaMap);
                        creaOrderMap.put("requFineTime", MapUtils.getString(srvOrderData, "ASSIST_REQ_TIME")); //要求完成时间
                        Map orderData = orderDealServiceIntf.createOrder(creaOrderMap);
                        srvOrderData.put("ORDER_ID", MapUtils.getString(orderData, "orderId"));
                        srvOrderData.put("REGION_ID", regionIdAry[i]);
                        srvOrderData.put("INSTANCE_ID_EG", MapUtils.getString(srvOrderData, "INSTANCE_ID") + regionIdAry[i]);
                        srvOrderData.put("PARENT_ORDER_ID", orderId);
                        srvOrderData.put("RESOURCES", MapUtils.getString(srvOrderData, "RESOURCES"));
                        srvOrderData.put("IFMAINORG", 1); //辅调
                        srvOrderData.put("CONFIGSTATE", "10E");
                        /**
                         * 二干退本地  关联表的关联关系要先查询如果有更新，如果没有插入
                         */
                        int secLocalRelateNum = orderDealServiceIntf.selectSecLocalRelate(srvOrderData);
                        if (secLocalRelateNum > 0){
                            orderDealServiceIntf.updateSecLocalRelate(srvOrderData);
                        }else {
                            orderDealServiceIntf.insertSecLocalRelate(srvOrderData);
                        }
                        this.secToLocalMsg(MapUtils.getString(orderData, "orderId"), regionIdAry[i]);
                        //下发本地的单子推送日志
                        ToKafkaTacheLog toKafkaWoCreateTacheLog = new ToKafkaTacheLog();
                        toKafkaWoCreateTacheLog.setBase_order_id(MapUtils.getString(orderData, "orderId"));
                        toKafkaWoCreateTacheLog.setSys_resouce("0");
                        LoggerThreadPool.addLoggerToExecute(resInterfaceLogger, toKafkaWoCreateTacheLog);
                    }
                }
            }
        }catch (Exception e) {
            paramsMap.put("success", false);
            paramsMap.put("message", "二干调度下发本地调度失败！");
            return paramsMap;
        }
        paramsMap.put("success", true);
        paramsMap.put("message", "二干调度下发本地调度成功！");
        return paramsMap;
    }

    public void secToLocalMsg(String orderId, String areaId) {
        logger.info("开始准备发送短信。。。。。。。。。。。。。。。。。。。。");
        Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(ThreadLocalInfoHolder.getLoginUser().getUserId()));
        areaId = MapUtils.getString(operStaffInfoMap,"AREA_ID");
        Map<String,Object> msmSwitchMap = orderDealDao.qryMsmSwitchByArea(areaId);
        if("1".equals(MapUtils.getString(msmSwitchMap,"ISSEND"))){ //ISSEND等于1时，需要发送短信
            Map<String, Object> msgInfoMap = new HashMap<>();
            msgInfoMap.put("orderIdList", orderId);
            msgInfoMap.put("areaId", areaId);
            msgInfoMap.put("operAction", "二干下发本地");
            msgInfoMap.put("state", "10A");
            msgInfoMap.put("isRelSend", MapUtils.getString(msmSwitchMap,"IS_PRODCTION_ENVIRONMENT") );
            msgInfoMap.put("belongSystem", "LOCAL");
            orderSendMsgDao.insertMsg(msgInfoMap);
        }
    }

    @Override
    public void finshRent(Map<String, Object> paramsMap) {
        String woId = MapUtils.getString(paramsMap, "woId");
        String orderId = MapUtils.getString(paramsMap, "orderId");
        Map<String, Object> srvOrdIndMap = orderDealDao.qrysrvOrdIdByorderId(orderId);
        String srvOrdId = MapUtil.getString(srvOrdIndMap, "SRV_ORD_ID");
        Map<String, String> cstOrderDataMap = orderDealDao.qryCstOrderData(orderId);
        String operStaffId = "-1";
        List<Map<String, Object>> localOrderList = new ArrayList<Map<String, Object>>();
        //先判断是否为拆机电路
        if("102".equals(MapUtils.getString(cstOrderDataMap, "ACTIVE_TYPE"))){
            localOrderList = orderQrySecondaryDao.qrySecToLocalChildFlowDate(orderId);
            operStaffId = "-2000";
        }else {
            localOrderList =  orderQrySecondaryDao.qryRentTacheOrder(orderId);
            operStaffId = "-1";
        }
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
            /*Map<String, Object> paramsMapRentInner = new HashMap<String, Object>();
            paramsMapRentInner.put("orderId",MapUtils.getString(localOrderList.get(i),"ORDER_ID"));
            paramsMapRentInner.put("woId",MapUtils.getString(localOrderList.get(i),"WO_ID"));
            paramsMapRentInner.put("action","回单");
//          paramsMapRentInner.put("remark","起租确认回单");
            paramsMapRentInner.put("operType",OrderTrackOperType.OPER_TYPE_4);*/
            Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(11);
            Map<String, Object> logDataMap = new HashMap<String, Object>();
            logDataMap.put("woId", MapUtils.getString(localOrderList.get(i),"WO_ID"));
            logDataMap.put("orderId", MapUtils.getString(localOrderList.get(i),"ORDER_ID"));
            logDataMap.put("remark", "起租环节回单");
            logDataMap.put("tacheId", MapUtils.getString(localOrderList.get(i),"ID"));
            logDataMap.put("operType", OrderTrackOperType.OPER_TYPE_4);
            logDataMap.put("operStaffInfoMap", operStaffInfoMap);
            logDataMap.put("action", "回单");
            logDataMap.put("trackMessage", "[回单]");
            tacheDealLogIntf.addTrackLog(logDataMap);
            //orderDealServiceIntf.insertTacheLog(paramsMapRentInner);
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
        /*Map<String, Object> paramsMapRent = new HashMap<String, Object>();
        paramsMapRent.put("orderId",orderId);
        paramsMapRent.put("woId",woId);
        paramsMapRent.put("action","回单");
//      paramsMapRent.put("remark","起租确认回单");
        paramsMapRent.put("operType",OrderTrackOperType.OPER_TYPE_4);
        orderDealServiceIntf.insertTacheLog(paramsMapRent);*/
        Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(11);
        Map<String, Object> logDataMap = new HashMap<String, Object>();
        logDataMap.put("woId", woId);
        logDataMap.put("orderId", orderId);
        logDataMap.put("remark", "起租环节回单");
        logDataMap.put("tacheId", BasicCode.NOTICE_OF_RENT_CONFIRMATION_2);
        logDataMap.put("operType", OrderTrackOperType.OPER_TYPE_4);
        logDataMap.put("operStaffInfoMap", operStaffInfoMap);
        logDataMap.put("action", "回单");
        logDataMap.put("trackMessage", "[回单]");
        tacheDealLogIntf.addTrackLog(logDataMap);
    }
    @Override
    public void childFlowLastTache(Map<String, Object> paramsMap){
        List<String> orderIdList = new ArrayList<String>();  //用于发送短信
        String orderId = MapUtils.getString(paramsMap, "orderId");
        String subName = MapUtils.getString(paramsMap, "subName");
        String tacheCode = MapUtils.getString(paramsMap, "tacheCode");
        Map parentOrder = orderDealDao.getParentOrder(orderId);
        String parentOrderId= MapUtils.getString(parentOrder, "PARENTORDERID");
        paramsMap.put("parentOrderId", parentOrderId);
        Map childLastNum = new HashMap();
        Map childNum = new HashMap();

        List<String> subNameList = new ArrayList<>();
        List<String> tacheCodeList = new ArrayList<>();
        //环节为数据制作和资源施工要查询对方
        if (BasicCode.RESCONSTRUCTION.equals(subName) || BasicCode.NETMANAGE.equals(subName)){
            //资源施工环节
            subNameList.add(BasicCode.RESCONSTRUCTION);
            subNameList.add(BasicCode.NETMANAGE);
            tacheCodeList.add( EnmuValueUtil.SPECIALTY_DATA_PRODUCTION_FINSH);
            tacheCodeList.add(EnmuValueUtil.SPECIALTY_RES_CONSTRUCTION_FINISH);
            childLastNum = orderDealDao.qryChildFlowNumAtLastSec(parentOrderId,tacheCodeList,subNameList);
            childNum = orderDealDao.qryChildFlowNum(parentOrderId, subNameList);
        }else {
            //其他环节
            subNameList.add(subName);
            tacheCodeList.add(tacheCode);
            childLastNum = orderDealDao.qryChildFlowNumAtLastSec(parentOrderId,tacheCodeList,subNameList);
            childNum = orderDealDao.qryChildFlowNum(parentOrderId, subNameList);
        }
        if (MapUtils.getString(childLastNum, "CHILDLASTNUM").equals(MapUtils.getString(childNum, "CHILDNUM"))) {
            List<HashMap<String, String>> operAttrsList = new ArrayList<HashMap<String, String>>();
            String operAction = "specialtyDataFinsh";
            if (EnmuValueUtil.SPECIALTY_DATA_PRODUCTION_FINSH.equals(tacheCode)
                    || EnmuValueUtil.SPECIALTY_RES_CONSTRUCTION_FINISH.equals(tacheCode)){
                /**
                 * 专业数据制作完成
                 * 1，查询是否下发本地网  查询是否拆机单
                 * 2，下发，不是拆机单：
                 *      查询数量 1条，查询是否到跨域全程调测；
                 *              2条以上，做判断
                 *    下发，是拆机单：
                 *      查询本地网的子流程是否到最后一个等待环节；
                 *    最后回单父流程待数据制作与本地调度
                 * 3，如果没下发，是拆机单：
                 *      不需要全程调测回单父流程待数据制作与本地调度
                 *    没下发，不是拆机单
                 *      需要全程调测回单父流程待数据制作与本地调度
                 */
                Map<String, String> cstOrderDataMap = orderDealDao.qryCstOrderData(parentOrderId);
                Map sourceMap = orderQrySecondaryDao.qrySrvOrderSourceSec(orderId);
                String source = MapUtils.getString(sourceMap, "RESOURCES");
                Map<String, Object> toLocalData = listenerOrderServiceIntf.qryIfToLocal(parentOrderId);

                if (MapUtils.getBoolean(toLocalData, "ifToLocal")){
                    //下发本地网
                    //查询二干下发本地的电路，电路调度环节的状态；
                    boolean ifSubmitSecOrder = true; //是否回单二干工单标识
                    List<Map<String, Object>> secToLocalOrderCircuitDispatchList = orderQrySecondaryDao.querySecToLocalOrderCircuitDispatch(MapUtils.getString(cstOrderDataMap,"SRV_ORD_ID"));
                    for (Map<String, Object> secToLocalOrderCircuitDispatch : secToLocalOrderCircuitDispatchList){
                        if (OrderTrackOperType.WO_ORDER_STATE_2.equals(MapUtils.getString(secToLocalOrderCircuitDispatch, "WO_STATE"))){
                            ifSubmitSecOrder = false; //如果电路调度环节工单状态还有处理中，不回单二干工单
                        }
                    }
                    if (ifSubmitSecOrder){
                        int rentTacheNum = orderQrySecondaryDao.qryRentOrTestTacheNum(parentOrderId,BasicCode.RENT);
                        int allNum = orderQrySecondaryDao.qryCrossFlowNum(parentOrderId);
                        int crossWholeTestNum = orderQrySecondaryDao.qryRentOrTestCrossTacheNum(parentOrderId,BasicCode.CROSS_WHOLE_COURDER_TEST);
                        int localChildFlowAllNum = orderQrySecondaryDao.qryLocalChildFlowAllNum(parentOrderId);
                        int localChildFlowFinishNum = orderQrySecondaryDao.qryLocalChildFlowFinishNum(parentOrderId);
                        if (BasicCode.ONEDRY.equals(source)){
                            if("102".equals(MapUtils.getString(cstOrderDataMap, "ACTIVE_TYPE"))){
                                if(localChildFlowAllNum == localChildFlowFinishNum){
                                    operAction = "specialtyDataFinshLast";
                                    listenerOrderServiceIntf.submitParentFlowTache(paramsMap);
                                }
                            }else {
                                //Map<String, String> cstOrderDataMap = orderDealDao.qryCstOrderData(parentOrderId);
                                String mainOrg = MapUtils.getString(cstOrderDataMap, "MAINORG"); //一干指定的主调
                                String areaParentId = MapUtils.getString(cstOrderDataMap, "HANDLE_DEP_ID"); //二干直接查询受理区域；本地网是受理区域的父idPARENT_ID
                                if (mainOrg.equals(areaParentId)) {
                                    //是主调
                                    if (allNum == 1){
                                        //判断下发本地的单子是否到跨域全程调测
                                        if (crossWholeTestNum == allNum){
                                            operAction = "specialtyDataFinshLast";
                                            listenerOrderServiceIntf.submitParentFlowTache(paramsMap);
                                        }
                                    }else if (allNum > 1){
                                        if(allNum - rentTacheNum == 1){
                                            //判断下发本地的单子是否有到跨域全程调测
                                            if (crossWholeTestNum == 1){
                                                operAction = "specialtyDataFinshLast";
                                                listenerOrderServiceIntf.submitParentFlowTache(paramsMap);
                                            }
                                        }
                                    }
                                }else {
                                    //非主调
                                    if(allNum == rentTacheNum){
                                        operAction = "specialtyDataFinshLast";
                                        listenerOrderServiceIntf.submitParentFlowTache(paramsMap);
                                    }
                                }
                            }
                        }else if (BasicCode.SECONDARY.equals(source)
                                ||BasicCode.JIKE.equals(source)){
                            if("102".equals(MapUtils.getString(cstOrderDataMap, "ACTIVE_TYPE"))){ //拆机
                                if(localChildFlowAllNum == localChildFlowFinishNum){
                                    operAction = "specialtyDataFinshLast";
                                    HashMap<String, String> operAttrsMap = new HashMap<String, String>();
                                    operAttrsMap.put("KEY", "ifMainOffice");
                                    operAttrsMap.put("VALUE", "0");
                                    operAttrsList.add(operAttrsMap);
                                    paramsMap.put("operAttrsList", operAttrsList);
                                    listenerOrderServiceIntf.submitParentFlowTache(paramsMap);
                                }
                            }else if("104,105".indexOf(MapUtils.getString(cstOrderDataMap, "ACTIVE_TYPE")) != -1){ //停机复机
                                if(localChildFlowAllNum == localChildFlowFinishNum){
                                    operAction = "specialtyDataFinshLast";
                                    listenerOrderServiceIntf.submitParentFlowTache(paramsMap);
                                    // 如果是停复机，判断数据来源是否是集客，如果是集客就调用反馈接口
                                    if (BasicCode.JIKE.equals(source)) {
                                        // 集客来单，调用反馈接口
                                        Map<String, Object> srvInfo = orderDealDao.qrySrvOrdData(orderId);
                                        String srvOrdId = MapUtils.getString(srvInfo,"SRV_ORD_ID");
                                        int numFinish = orderDealDao.qryInterResult(srvOrdId, "FinishOrder");
                                        if (numFinish < 1) {
                                            Map<String, Object> map = new HashMap<String, Object>();
                                            map.put("srvOrdId",srvOrdId);
                                            map.put("serviceId",MapUtils.getString(srvInfo,"SERVICE_ID"));
                                            Map finMap = finishOrderServiceIntf.finishOrder(map);
//                                    if (!"1".equals(MapUtils.getString(finMap, "RESP_CODE")))
//                                        throw new Exception("派单失败!调用集客反馈接口异常，异常原因：" + MapUtils.getString(finMap, "RESP_DESC"));
                                        }

                                    }
                                }
                            }else {
                                HashMap<String, String> operAttrsMap = new HashMap<String, String>();
                                operAttrsMap.put("KEY", "ifMainOffice");
                                operAttrsMap.put("VALUE", "1");
                                operAttrsList.add(operAttrsMap);
                                paramsMap.put("operAttrsList", operAttrsList);
                                if (allNum == 1){
                                    //判断下发本地的单子是否到跨域全程调测
                                    if (crossWholeTestNum == allNum){
                                        operAction = "specialtyDataFinshLast";
                                        listenerOrderServiceIntf.submitParentFlowTache(paramsMap);
                                    }
                                }else if (allNum > 1){
                                    if(allNum - rentTacheNum == 1){
                                        //判断下发本地的单子是否有到跨域全程调测
                                        if (crossWholeTestNum == 1){
                                            operAction = "specialtyDataFinshLast";
                                            listenerOrderServiceIntf.submitParentFlowTache(paramsMap);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }else {
                    //没下发本地网
                    /*if (BasicCode.ONEDRY.equals(source)){} else */
                    if (BasicCode.SECONDARY.equals(source)
                            ||BasicCode.JIKE.equals(source)){
                        if("102".equals(MapUtils.getString(cstOrderDataMap, "ACTIVE_TYPE"))){
                            //拆机电路
                            HashMap<String, String> operAttrsMap = new HashMap<String, String>();
                            operAttrsMap.put("KEY", "ifMainOffice");
                            operAttrsMap.put("VALUE", "0");
                            operAttrsList.add(operAttrsMap);
                            paramsMap.put("operAttrsList", operAttrsList);
                        } else if ("101,103".indexOf(MapUtils.getString(cstOrderDataMap, "ACTIVE_TYPE")) != -1) {
                            //新开，变更
                            HashMap<String, String> operAttrsMap = new HashMap<String, String>();
                            operAttrsMap.put("KEY", "ifMainOffice");
                            operAttrsMap.put("VALUE", "1");
                            operAttrsList.add(operAttrsMap);
                            paramsMap.put("operAttrsList", operAttrsList);
                        } else if("104,105".indexOf(MapUtils.getString(cstOrderDataMap, "ACTIVE_TYPE")) != -1){ //停机复机
                            // 如果是停复机，判断数据来源是否是集客，如果是集客就调用反馈接口
                            if (BasicCode.JIKE.equals(source)) {
                                // 集客来单，调用反馈接口
                                Map<String, Object> srvInfo = orderDealDao.qrySrvOrdData(orderId);
                                String srvOrdId = MapUtils.getString(srvInfo,"SRV_ORD_ID");
                                int numFinish = orderDealDao.qryInterResult(srvOrdId, "FinishOrder");
                                if (numFinish < 1) {
                                    Map<String, Object> map = new HashMap<String, Object>();
                                    map.put("srvOrdId",srvOrdId);
                                    map.put("serviceId",MapUtils.getString(srvInfo,"SERVICE_ID"));
                                    Map finMap = finishOrderServiceIntf.finishOrder(map);
//                                    if (!"1".equals(MapUtils.getString(finMap, "RESP_CODE")))
//                                        throw new Exception("派单失败!调用集客反馈接口异常，异常原因：" + MapUtils.getString(finMap, "RESP_DESC"));
                                }

                            }

                        }                    }
                    operAction = "specialtyDataFinshLast";
                    listenerOrderServiceIntf.submitParentFlowTache(paramsMap);
                }
            }else {
                //二干资源分配完成，直接回单父流程
                operAction = "submit_res_dispatch";
                listenerOrderServiceIntf.submitParentFlowTache(paramsMap);
            }
            /**
             * 这里用于子流程结束--主流程发送短信通知
             * --资源分配完成，给数据制作和本地调度发短信
             * --专业数据制作子流程结束，给本地跨域全程调测发送短信通知
             */
            orderIdList.add(parentOrderId);
            logger.info("开始准备发送短信。。。。。。。。。。。。。。。。。。。。");
            String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
            Map<String, Object> sendMsgMap = new HashMap<String, Object>();
            sendMsgMap.put("operStaffId", operStaffId);
            sendMsgMap.put("orderIdList", orderIdList);
            sendMsgMap.put("operAction", operAction);
            orderSendMsgService.sendMsgBefore(sendMsgMap);
        }
    }

    @Override
    public Map<String, Object> qryIfToLocal(String orderId) {
        Map<String,Object> resMap = new HashMap<String, Object>();
        Map<String, Object> dispatchDataMap = orderQrySecondaryDao.qryDispatchData(orderId);
        if("是".equals(MapUtils.getString(dispatchDataMap, "TO_BDW"))){
            resMap.put("ifToLocal",true);
        }else if("否".equals(MapUtils.getString(dispatchDataMap, "TO_BDW"))){
            resMap.put("ifToLocal",false);
        }
        return resMap;
    }

    @Override
    public void submitParentFlowTache(Map<String, Object> paramsMap) {
        paramsMap.put("woState", OrderTrackOperType.WO_ORDER_STATE_10);
        Map woOrder = orderDealDao.getParentWoOrder(paramsMap);
        if(MapUtils.isNotEmpty(woOrder)){
            String woOrderId = MapUtils.getString(woOrder, "WOID");
            orderDealDao.updateWoStateByWoId(woOrderId, OrderTrackOperType.WO_ORDER_STATE_2);
            FlowWoDTO woDTO = new FlowWoDTO();
            woDTO.setWoId(woOrderId);
            if (!ListUtil.isEmpty((List<HashMap<String, String>>) MapUtils.getObject(paramsMap, "operAttrsList"))){
                woDTO.setOperAttrs((List<HashMap<String, String>>) MapUtils.getObject(paramsMap, "operAttrsList"));
            }
            flowAction.complateWo("-2000", woDTO);
        }
    }

    @Override
    public void sumCompleteReceiptTache(Map<String, Object> paramsMap) {
        Map<String, Object> resMapSec = new HashMap<String, Object>();
        String orderId = MapUtils.getString(paramsMap, "orderId");
        Map<String, Object> srvOrdIdMap = orderDealDao.qrysrvOrdIdByorderId(orderId);
        String srvOrdId = MapUtils.getString(srvOrdIdMap, "SRV_ORD_ID");
        String activeType = MapUtils.getString(srvOrdIdMap, "ACTIVE_TYPE");
        if(!BasicCodeEnum.UNPACK.getValue().equals(activeType)){
            resMapSec.put("SRV_ORD_ID",srvOrdId);
            List<String> relateListT = Lists.newArrayList();
       //     List<String> relateListRe = orderQrySecondaryDao.qryRelateInfoIdBySrvordId(srvOrdId);
            List<String> relateListRe = orderQrySecondaryDao.qryRelateInstanceIdBySrvordId(srvOrdId);
            if(!CollectionUtils.isEmpty(relateListRe)){
                relateListT.addAll(relateListRe);
            }
            resMapSec.put("relaCrmOrderCodes",relateListT);
            Map<String, Object> secondaryMap = orderDealServiceIntf.sendSecondScheduleLTResAssign(resMapSec);
            if(!MapUtil.getBoolean(secondaryMap,"success")){
                throw new RuntimeException(MapUtil.getString(secondaryMap,"message"));
            }
        }


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

}
