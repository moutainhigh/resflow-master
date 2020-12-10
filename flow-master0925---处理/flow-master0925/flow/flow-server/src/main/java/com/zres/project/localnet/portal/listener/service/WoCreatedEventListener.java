package com.zres.project.localnet.portal.listener.service;

import com.zres.project.localnet.portal.cloudNetworkFlow.CloudListenerOrderServiceIntf;
import com.zres.project.localnet.portal.common.util.FlowTacheUtil;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.CheckOrderDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.EventListenerDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderQrySecondaryDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.flowdealinfo.service.ListenerOrderServiceIntf;
import com.zres.project.localnet.portal.listener.util.EnmuValueUtil;
import com.zres.project.localnet.portal.logInfo.entry.ToKafkaTacheLog;
import com.zres.project.localnet.portal.logInfo.service.ResInterfaceLogger;
import com.zres.project.localnet.portal.logInfo.until.LoggerThreadPool;
import com.zres.project.localnet.portal.order.data.dao.OrderQueryDao;
import com.zres.project.localnet.portal.util.OrderTrackOperType;

import com.zres.project.localnet.portal.webservice.provinceRes.OssToProvFinishOrderServiceIntf;
import com.ztesoft.res.frame.flow.common.dto.FlowWoDTO;
import com.ztesoft.res.frame.flow.common.event.WoCreatedEvent;
import com.ztesoft.res.frame.flow.common.intf.FlowAction;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;

@Component
public class WoCreatedEventListener implements ApplicationListener<WoCreatedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(WoCreatedEventListener.class);

    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private CheckOrderDao checkOrderDao;
    @Autowired
    private OrderQrySecondaryDao orderQrySecondaryDao;
    @Autowired
    private ListenerOrderServiceIntf listenerOrderServiceIntf;
    @Autowired
    private CloudListenerOrderServiceIntf cloudListenerOrderServiceIntf;
    @Autowired
    private EventListenerDao eventListenerDao;
    @Autowired
    private FlowAction flowAction;
    @Autowired
    private ResInterfaceLogger resInterfaceLogger;
    @Autowired
    private OssToProvFinishOrderServiceIntf ossToProvFinishOrderServiceIntf;

    @Autowired
    private OrderQueryDao orderQueryDao;


    private String tacheId = "500001155,500001157";

    private String forwardOrReverseFlag = "";
    /*
     * 国际公司流程优化：
     *  跨域流程只需要 电路调度，资源分配，本地测试，全程调测，起租这几个环节，别的环节自动回单；
     * @author guanzhao
     * @date 2020/10/4
     * 下面这几个环节监听到单，如果是国际公司的单子则自动回单
     */
    private List<String> tacheCodes = Arrays.asList(
            EnmuValueUtil.OUTSIDE_CONSTRUCT,
            EnmuValueUtil.DATA_MAKE,
            EnmuValueUtil.RES_CONSTRUCT
    );

    /*
     * 云组网
     * 需要监听处理的环节
     * @author guanzhao
     * @date 2020/10/29
     *
     */
    private List<String> tacheCodeListener = Arrays.asList(
            //子流程
            FlowTacheUtil.YZW_CHILDFLOWWAIT,
            FlowTacheUtil.YZW_MCPE_CONFIG_FINISH,
            //本地业务新开
            FlowTacheUtil.YZW_A_CONSTRUCT_WAIT,
            FlowTacheUtil.YZW_Z_CONSTRUCT_WAIT,
            FlowTacheUtil.YZW_A_MCPE_INSTALL,
            FlowTacheUtil.YZW_Z_MCPE_INSTALL,
            FlowTacheUtil.YZW_L_UPLINK_CONFIG_WAIT,
            //本地移机
            FlowTacheUtil.YZW_L_CONSTRUCT_WAIT,
            FlowTacheUtil.YZW_LY_UPLINK_CONFIG_WAIT,

            //跨域业务
            FlowTacheUtil.YZW_C_CONSTRUCT_WAIT,
            FlowTacheUtil.YZW_C_MCPE_INSTALL,
            //移机
            FlowTacheUtil.YZW_START_MCPE_CONFIG,
            FlowTacheUtil.YZW_END_MCPE_CONFIG,
            FlowTacheUtil.YZW_L_UPLINK_CONFIG_WAIT,
            FlowTacheUtil.YZW_CHECK_WAITING,

            // 变更升降速
            FlowTacheUtil.YZW_RATE_UPLINK_CONFIG_WAIT,
            // 变更下联端口
            FlowTacheUtil.YZW_CHG_NEW_APPLICATION,
            FlowTacheUtil.YZW_LC_PORT_CONFIG

    );

    @Override
    public void onApplicationEvent(WoCreatedEvent woCreatedEvent) {

        /**
         * 监听--本地网所有流程
         */
        if(EnmuValueUtil.LOCAL_NETWORK_DISPATCH.equals(woCreatedEvent.getOrderType())){

            /**
             * 入库工单正向反向标识 ”0”--正向；”1”--反向
             */
            Map<String, Object> ifWoBack = orderDealDao.qryCrossWholeAttrParams(woCreatedEvent.getWoId());
            String oldWoIdBack = MapUtils.getString(ifWoBack, "OLD_WO_ID");
            if (StringUtils.isEmpty(oldWoIdBack)) {
                forwardOrReverseFlag = flowAction.forwardOrReverse(woCreatedEvent.getWoId());
                Map<String, Object> woOrderParams = new HashMap<String, Object>();
                woOrderParams.put("woId", woCreatedEvent.getWoId());
                woOrderParams.put("orderId", woCreatedEvent.getOrderId());
                woOrderParams.put("forwardOrReverseFlag", forwardOrReverseFlag);
                orderQrySecondaryDao.insertWoOrderOper(woOrderParams);
            }

            /*
             * 云组网-监听环节处理
             * @author guanzhao
             * @date 2020/10/29
             *
             */
            if (FlowTacheUtil.LOCAL_YZW.equals(woCreatedEvent.getOrderObjType())
                    || FlowTacheUtil.YZW_CHILDFLOW.equals(woCreatedEvent.getOrderObjType())) {
                if (tacheCodeListener.contains(woCreatedEvent.getTacheCode())) {
                    Map<String, Object> toOrderMap = new HashMap<>();
                    toOrderMap.put("orderId", woCreatedEvent.getOrderId());
                    toOrderMap.put("woId", woCreatedEvent.getWoId());
                    toOrderMap.put("tacheId", woCreatedEvent.getTacheId());
                    toOrderMap.put("dealUserId", woCreatedEvent.getDispObjId());
                    toOrderMap.put("tacheCode", woCreatedEvent.getTacheCode());
                    toOrderMap.put("actType", woCreatedEvent.getOrderActType());
                    toOrderMap.put("forwardOrReverseFlag", forwardOrReverseFlag);
                    cloudListenerOrderServiceIntf.tacheToWoOrder(toOrderMap);
                }
            }


            /**
             * 监听--电路调度环节到单
             */
            if (EnmuValueUtil.CIRCUIT_DISPATCH.equals(woCreatedEvent.getTacheCode())){
                /*
                 * 电路调度到单业务：
                 * 先判断是否本地测试退单：如果是，再判断是否有执行中的子流程，如果有改电路调度的状态为已启子流程，如果没有，则不操作；
                 *                                      如果不是，不用操作；
                 * @author guanzhao
                 * @date 2020/11/6
                 *
                 */
                Map<String, String> testMap = new HashMap<String, String>();
                // testMap.put("attrId","isLocaltestRollback,isTestRollback");
                testMap.put("orderId", woCreatedEvent.getOrderId());
                List<Map<String, Object>> attrParamsList = orderDealDao.qryAttrParams(testMap);
                for (int i = 0; i < attrParamsList.size(); i++) {
                    String attrValue = MapUtils.getString(attrParamsList.get(i), "ATTR_VAL");
                    if ("0".equals(attrValue)) {
                        Map childNum = orderDealDao.qryChildFlowNum(woCreatedEvent.getOrderId());
                        if(MapUtils.getIntValue(childNum, "CHILDNUM") > 0){
                            orderDealDao.updateWoDataByWoId(woCreatedEvent.getWoId(), woCreatedEvent.getOrderId(),
                                    OrderTrackOperType.WO_ORDER_STATE_10); // 修改电路调度环节状态为已启子流程290000110
                        }
                    }
                }
            }
            // 监听到单  监听多工单环节(外线专业核查、传输专业核查、数据专业核查、交换、其他、接入)到单
         //   wosCreatedEventListener(woCreatedEvent);

            /**
             * 监听--本地客户电路
             */
            if(EnmuValueUtil.LOCAL_CUST.equals(woCreatedEvent.getOrderObjType())){
                /**
                 * 监听起止租环节
                 * 1，如果是集客来单，等待集客下发起止租通知回单环节调用资源归档接口
                 *                  移机也是直接回单环节调用资源归档接口
                 * 2，如果是本地启单，直接回单环节调用资源归档接口
                 */
                //起租和止租
                if (EnmuValueUtil.START_RENT.equals(woCreatedEvent.getTacheCode())
                        || EnmuValueUtil.STOP_RENT.equals(woCreatedEvent.getTacheCode())){
                        listenerOrderServiceIntf.startStopRent(woCreatedEvent.getOrderId(), woCreatedEvent.getWoId());
                }
                /**
                 * 先判断是否二干下发--二干停复机对接本地客户电路的停复机流程
                 * 二干下发：要等二干通知起租
                 * 非二干下发：同上
                 */
                //起止租
                if ( EnmuValueUtil.START_STOP_RENT.equals(woCreatedEvent.getTacheCode())){
                    Map<String, Object> ifFromSecondaryMap = orderQrySecondaryDao.qryIfFromSecondary(woCreatedEvent.getOrderId());
                    if(!MapUtils.isNotEmpty(ifFromSecondaryMap)){
                        listenerOrderServiceIntf.startStopRent(woCreatedEvent.getOrderId(), woCreatedEvent.getWoId());
                    }
                }
                /**
                 * 本地客户电路新开变更流程-本地测试环节
                 * 福建集约化需求： 本地客户电路新开变更流程-本地测试环节
                 * 1.到单后，修改工单状态为290000118	待回单  (等待号线反馈回单)
                 * 2.调用接口，自动将工单发送给省内号线资源管理系统。
                 *
                 * 4.1.	省内自建系统反馈接口，收到报文后，查询工单，改工单状态为处理中
                 * 如果反馈成功，直接提交工单，
                 * 需要将省内号线资源系统反馈的测试报告附件信息和工单关联。
                 */
                if (EnmuValueUtil.LOCAL_TEST.equals(woCreatedEvent.getTacheCode())){
                    Map<String, Object> srvOrdIdMap = orderDealDao.qrysrvOrdIdByorderId(woCreatedEvent.getOrderId());
                    String srvOrdId = MapUtils.getString(srvOrdIdMap, "SRV_ORD_ID");
                    //判断是否符合条件派发省内号线---互联网专线（商务专线）--新开、移机、拆机
                 //   String srvOrdId = MapUtils.getString(map,"srvOrdId","");
                    Map<String, Object> srvMap = orderDealDao.querySrvInfoBySrvOrdId(srvOrdId);
                    String serviceId = MapUtils.getString(srvMap, "SERVICE_ID","");
                    String activeType = MapUtils.getString(srvMap, "ACTIVE_TYPE","");
                    String orderType = MapUtils.getString(srvMap, "ORDER_TYPE","");
                    String subType = orderDealDao.qrySubType(srvOrdId);
                    if (BasicCode.DIA_SERVICEID.equals(serviceId) && ("101,102,106".indexOf(activeType)!= -1) && "5".equals(subType)){
                        orderDealDao.updateWoStateByWoId(woCreatedEvent.getWoId(),OrderTrackOperType.WO_ORDER_STATE_18);
                        // todo 调接口
                        Map<String, Object> map = new HashMap<>();
                        map.put("srvOrdId",srvOrdId);
                        ossToProvFinishOrderServiceIntf.ossToProvFinishOrder(map);
                    }
                }
            }

            /**
             * 监听--子流程
             */
            if (EnmuValueUtil.LOCAL_CHILDFLOW.equals(woCreatedEvent.getOrderObjType())){
                /**
                 * 子流程的最后一个环节
                 * 子流程是否全部走完，全部走完回单电路调度
                 */
                if(EnmuValueUtil.CHILDFLOWWAIT.equals(woCreatedEvent.getTacheCode())){
                    Map<String, Object> paramsMap = new HashMap<String, Object>();
                    paramsMap.put("orderId", woCreatedEvent.getOrderId());
                    listenerOrderServiceIntf.childFlowLastTache(paramsMap);
                }

                if (EnmuValueUtil.RES_ALLOCATE.equals(woCreatedEvent.getTacheCode())){
                   // forwardOrReverseFlag = flowAction.forwardOrReverse(woCreatedEvent.getWoId());
                    Map<String, Object> paramsMap = new HashMap<String, Object>();
                    paramsMap.put("woId", woCreatedEvent.getWoId());
                    paramsMap.put("orderId", woCreatedEvent.getOrderId());

                    if ("0".equals(forwardOrReverseFlag)){
                        int ifNum = orderDealDao.qryAccessAutoConfig(woCreatedEvent.getOrderId());
                        if (ifNum > 0){
                            listenerOrderServiceIntf.resAllocateAuto(paramsMap);
                        }
                        //修改资源分配前置资源工单状态
                        listenerOrderServiceIntf.modifyResWoState(paramsMap);
                    }
                }


                if (EnmuValueUtil.DATA_MAKE.equals(woCreatedEvent.getTacheCode())){
                    //数据制作环节
                    int ifNum = orderDealDao.qryAccessAutoConfig(woCreatedEvent.getOrderId());
                    if (ifNum > 0){
                        Map<String, Object> paramsMap = new HashMap<String, Object>();
                        paramsMap.put("woId", woCreatedEvent.getWoId());
                        paramsMap.put("orderId", woCreatedEvent.getOrderId());
                        listenerOrderServiceIntf.updateWoState(paramsMap);
                    }
                }

                /*
                 * 这几个环节，如果是国际公司的单子则自动回单
                 * @author guanzhao
                 * @date 2020/10/4
                 *
                 */
                if (tacheCodes.contains(woCreatedEvent.getTacheCode())) {
                    int ifNum = orderDealDao.qryIfPopConfig(woCreatedEvent.getOrderId());
                    if (ifNum > 0){
                        FlowWoDTO woChildDTO = new FlowWoDTO();
                        if (EnmuValueUtil.DATA_MAKE.equals(woCreatedEvent.getTacheCode())) {
                            Map<String, String> paramsMap = new HashMap<>();
                            paramsMap.put("isNeedResConstruct", "1");
                            paramsMap.put("isDataMakeBack", "1");
                            List<HashMap<String, String>> operAttrsList = new ArrayList<>();
                            Iterator<String> iter = paramsMap.keySet().iterator();
                            while (iter.hasNext()) {
                                HashMap<String, String> operAttrsMap = new HashMap<>();
                                String key = iter.next();
                                String value = String.valueOf(paramsMap.get(key));
                                operAttrsMap.put("KEY", key);
                                operAttrsMap.put("VALUE", value);
                                operAttrsList.add(operAttrsMap);
                            }
                            woChildDTO.setOperAttrs(operAttrsList);
                        }
                        woChildDTO.setWoId(woCreatedEvent.getWoId());
                        flowAction.complateWo("-2000", woChildDTO);
                    }
                }

                /**
                 * 互联网专线产品 10000011 、 子流程、 数据制作环节、
                 * 业务动作：
                 *      2001：产品订购（指集团用户开户即新开）;  --> 101
                 *      2010：集团产品变更； --> 103
                 *      2011：移机； --> 106
                 *      2013：停机； --> 104
                 *      2014：复机 ； --> 105
                 *      2019：拆机；  -->102
                 * 变更明细：
                 *      1020 升速
                 *      1021 降速
                 *      1022 IP地址变更
                 *      1025 其他业务属性变更开通80端口
                 * @author wangsen
                 * @date 2020/10/19 14:35
                 */
                if (EnmuValueUtil.DATA_MAKE.equals(woCreatedEvent.getTacheCode())) {
                    //数据制作环节
                    int ifNum = orderDealDao.qryIsDataMakeAuto(woCreatedEvent.getOrderId());
                    if (ifNum > 0){
                        Map<String, Object> paramsMap = new HashMap<String, Object>();
                        paramsMap.put("woId", woCreatedEvent.getWoId());
                        paramsMap.put("orderId", woCreatedEvent.getOrderId());
                        listenerOrderServiceIntf.dataMakeAuto(paramsMap);
                    }
                }
            }

            /**
             * 监听--跨域电路
             */
            if(EnmuValueUtil.CROSS_DOMAIN.equals(woCreatedEvent.getOrderObjType())){
                /**
                 * 跨域全程调测到单
                 * 1，先判断是一干来单还是二干来单；
                 * 2，一干直接下发本地网；
                 * 3，二干下发本地网：二干直接下发
                 *                  一干下发二干，二干下发；
                 */
                if(EnmuValueUtil.CROSS_WHOLE_COURDER_TEST.equals(woCreatedEvent.getTacheCode())){
                    Map<String, Object> paramsMap = new HashMap<String, Object>();
                    paramsMap.put("woId", woCreatedEvent.getWoId());
                    paramsMap.put("orderId", woCreatedEvent.getOrderId());
                    listenerOrderServiceIntf.crassWholeTest(paramsMap);
                }
                /**
                 * 起租到单
                 * 监听二干下发的单子
                 * 1，二干直接下发
                 * 2，一干下发二干，二干下发本地网
                 */
                if (EnmuValueUtil.RENT.equals(woCreatedEvent.getTacheCode())){
                    Map<String, Object> paramsMap = new HashMap<String, Object>();
                    paramsMap.put("orderId", woCreatedEvent.getOrderId());
                    listenerOrderServiceIntf.rentTache(paramsMap);
                }
            }

            //核查流程
            if (EnmuValueUtil.LOCAL_RES_CHECK.equals(woCreatedEvent.getOrderObjType())){
                if (EnmuValueUtil.CHECK_WAIT.equals(woCreatedEvent.getTacheCode())){
                    String orderId = woCreatedEvent.getOrderId();
                    Map<String, Object> paramsMap = new HashMap<String, Object>();
                    paramsMap.put("orderId", orderId);
                    boolean judjeComplate = false; //判断是否所有子流程已完成。
                    List<Map<String, Object>> secLocalRelateInfo = checkOrderDao.getSecLocalRelateInfo(orderId); //查询二干本地关联信息表
                    if(secLocalRelateInfo.size() > 0){
                        int localCheckAtWaitTacheNum = checkOrderDao.qryLocalCheckAtWaitTacheNum(orderId);
                        if (localCheckAtWaitTacheNum == secLocalRelateInfo.size()){
                            judjeComplate = true;
                        }
                        paramsMap.put("tacheId", BasicCode.LOCAL_NETWORK_CHECK);
                        paramsMap.put("woState", OrderTrackOperType.WO_ORDER_STATE_12);
                        String secondCheckWoId = orderDealDao.getSecondCheckWoId(paramsMap); //查询二干核查调度下发本地环节工单
                        if(judjeComplate && StringUtils.hasText(secondCheckWoId)){
                            orderDealDao.updateWoStateByWoId(secondCheckWoId,OrderTrackOperType.WO_ORDER_STATE_2);
                            FlowWoDTO woChildDTO = new FlowWoDTO();
                            woChildDTO.setWoId(secondCheckWoId);
                            flowAction.complateWo("-2000", woChildDTO);
                        }
                    }else {
                        //本地的核查单
                        FlowWoDTO woDTO = new FlowWoDTO();
                        woDTO.setWoId(woCreatedEvent.getWoId());
                        flowAction.complateWo("-2000", woDTO);
                    }
                }

                //核查汇总环节
                if(BasicCode.CHECK_SUMMARY.equals(woCreatedEvent.getTacheId()) || BasicCode.CHECK_TOTAL.equals(woCreatedEvent.getTacheId())){
                    Map<String, Object> checkDispatchWoOrder = checkOrderDao.qryCheckDispatchWoOrder(woCreatedEvent.getOrderId(), BasicCode.CHECK_DISPATCH);
                    String checkDispatchWoId = MapUtils.getString(checkDispatchWoOrder, "WO_ID");
                    Map<String, Object> updateMap = new HashMap<>();
                    updateMap.put("woId", checkDispatchWoId);
                    updateMap.put("woState", OrderTrackOperType.WO_ORDER_STATE_4);
                    //checkOrderDao.updateWoOrderStateByWoId(updateMap);
                    orderDealDao.updWoOrder(updateMap);
                }
            }

            /**
             * 资源补录模块
             * 各专业资源补录环节和本地调度资源补录环节到单
             */
            if(EnmuValueUtil.LOCAL_RESOURCE_SUPPLEMENT_FLOW.equals(woCreatedEvent.getOrderActType())){
                if (EnmuValueUtil.ALL_SPECIALTY_RESOURCE_SUPPLEMENT_LOCAL.equals(woCreatedEvent.getTacheCode())){
                    orderDealDao.updateWoStateByWoId(woCreatedEvent.getWoId(),OrderTrackOperType.WO_ORDER_STATE_10);
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("orderId", woCreatedEvent.getOrderId());
                    params.put("woId", woCreatedEvent.getWoId());
                    params.put("tacheId", woCreatedEvent.getTacheId());
                    params.put("tacheCode", woCreatedEvent.getTacheCode());
                    listenerOrderServiceIntf.resSupplementToOrder(params);
                }
            }

            //到单kafka推日志
            String orderId = woCreatedEvent.getOrderId();
            ToKafkaTacheLog toKafkaWoCreateTacheLog = new ToKafkaTacheLog();
            toKafkaWoCreateTacheLog.setSheet_id(woCreatedEvent.getWoId());
            toKafkaWoCreateTacheLog.setBase_order_id(woCreatedEvent.getOrderId());
            LoggerThreadPool.addLoggerToExecute(resInterfaceLogger, toKafkaWoCreateTacheLog);
            if(tacheId.indexOf(woCreatedEvent.getTacheId()) != -1){
                Map<String, Object> parentOrder = orderDealDao.getParentOrder(orderId); //查询是否存在父订单
                if(MapUtils.isNotEmpty(parentOrder) && !"".equals(MapUtils.getString(parentOrder, "PARENTORDERID"))){
                    orderId = MapUtils.getString(parentOrder, "PARENTORDERID");
                }
                Map<String, String> cstOrderDataMap = orderDealDao.qryCstOrderData(orderId);
                toKafkaWoCreateTacheLog.setCstOrderDataMap(cstOrderDataMap);
            }
            LoggerThreadPool.addLoggerToExecute(resInterfaceLogger, toKafkaWoCreateTacheLog);

            if(EnmuValueUtil.DIA_DDOS_FLOW.equals(woCreatedEvent.getOrderObjType())){
                if(EnmuValueUtil.DIA_DDOS_CHANGE_FLOW.equals(woCreatedEvent.getOrderActType())){
                    if(EnmuValueUtil.DDOS_FLOW_CLEANING.equals(woCreatedEvent.getTacheCode())){
                        //DDOS激活环节改为带回单
                        orderDealDao.updateWoStateByWoId(woCreatedEvent.getWoId(), OrderTrackOperType.WO_ORDER_STATE_18);
                    }
                }
            }
        }

        /**
         * 监听--二干流程电路
         */
        if (EnmuValueUtil.SECONDARY_TRUNK_DISPATCH.equals(woCreatedEvent.getOrderType())){

            /**
             * 入库工单正向反向标识 ”0”--正向；”1”--反向
             */
            Map<String, Object> ifWoBack = orderDealDao.qryCrossWholeAttrParams(woCreatedEvent.getWoId());
            String oldWoIdBack = MapUtils.getString(ifWoBack, "OLD_WO_ID");
            if (StringUtils.isEmpty(oldWoIdBack)) {
                forwardOrReverseFlag = flowAction.forwardOrReverse(woCreatedEvent.getWoId());
                Map<String, Object> woOrderParams = new HashMap<String, Object>();
                woOrderParams.put("woId", woCreatedEvent.getWoId());
                woOrderParams.put("orderId", woCreatedEvent.getOrderId());
                woOrderParams.put("forwardOrReverseFlag", forwardOrReverseFlag);
                orderQrySecondaryDao.insertWoOrderOper(woOrderParams);
            }

            /**
             * 监听[一干电路]客户、局内电路
             */
            if (EnmuValueUtil.MAIN_DISPATCH_CUST.equals(woCreatedEvent.getOrderObjType())){

                /**
                 * 监听一干客户、局内电路完工汇总环节
                 */
                if(EnmuValueUtil.SUMMARY_OF_COMPLETION.equals(woCreatedEvent.getTacheCode())){
                    Map<String, Object> paramsMap = new HashMap<String, Object>();
                    paramsMap.put("woId", woCreatedEvent.getWoId());
                    paramsMap.put("orderId", woCreatedEvent.getOrderId());
                    listenerOrderServiceIntf.sumCompleteReceiptTache(paramsMap);
                }
            }

            /**
             * 监听--[二干电路]客户、局内电路
             */
            if (EnmuValueUtil.SECONDARY_CUST.equals(woCreatedEvent.getOrderObjType())
                    || EnmuValueUtil.SECONDARY_INSIDE.equals(woCreatedEvent.getOrderObjType())){

                /**
                 * 全程调测到单
                 * 修改全程调测环节状态为等待本地网调度处理
                 * 修改本地网对应跨域流程跨域全程调测环节的工单状态为处理中
                 */
                if(EnmuValueUtil.FULL_COMMISSIONING.equals(woCreatedEvent.getTacheCode())){
                    orderDealDao.updateWoStateByWoId(woCreatedEvent.getWoId(),OrderTrackOperType.WO_ORDER_STATE_12);
                    Map<String,Object> secTestOrderMap = orderQrySecondaryDao.qrySecTestTacheOrder(woCreatedEvent.getOrderId());
                    //修改本地网跨域流程---全程调测环节的工单状态为处理中
                    orderDealDao.updateWoStateByWoId(MapUtils.getString(secTestOrderMap,"WO_ID"),OrderTrackOperType.WO_ORDER_STATE_2);
                }

                /**
                 * 监听二干客户、局内电路完工汇总环节
                 */
                if(EnmuValueUtil.SUMMARY_OF_COMPLETION_2.equals(woCreatedEvent.getTacheCode())){
                    Map<String, Object> paramsMap = new HashMap<String, Object>();
                    paramsMap.put("woId", woCreatedEvent.getWoId());
                    paramsMap.put("orderId", woCreatedEvent.getOrderId());
                    listenerOrderServiceIntf.sumCompleteReceiptTache(paramsMap);
                }

                /**
                 * 二干客户电路起租确认通知环节到单监听
                 *      只有二干停复机电路才会走这里，本地调度流程对接本地客户电路停复机
                 *
                 *      确认通知环节到单：
                 *      要判断单子来源： 如果是集客来的，需要等集客通知才能起止租
                 *                     如果不是集客来单，回单确认通知环节，同时也本地调度起止租并归档
                 *
                 */
                if(EnmuValueUtil.NOTICE_OF_RENT_CONFIRMATION_2.equals(woCreatedEvent.getTacheCode())){
                    Map<String, Object> paramsMap = new HashMap<String, Object>();
                    paramsMap.put("woId", woCreatedEvent.getWoId());
                    paramsMap.put("orderId", woCreatedEvent.getOrderId());
                    String source = orderQrySecondaryDao.qrySrvOrderSource(woCreatedEvent.getOrderId());
                    if (!BasicCode.JIKE.equals(source)){
                        listenerOrderServiceIntf.finshRent(paramsMap);
                    }
                }
                //二干的单子推送日志
                ToKafkaTacheLog toKafkaWoCreateTacheLog = new ToKafkaTacheLog();
                toKafkaWoCreateTacheLog.setSheet_id(woCreatedEvent.getWoId());
                toKafkaWoCreateTacheLog.setBase_order_id(woCreatedEvent.getOrderId());
                toKafkaWoCreateTacheLog.setSys_resouce("2");
                LoggerThreadPool.addLoggerToExecute(resInterfaceLogger, toKafkaWoCreateTacheLog);

            }
        }

    }

    /**
     * 监听核查流程多工单环节到单
     * (外线专业核查、传输专业核查、数据专业核查、交换、其他、接入)
     * @param woCreatedEvent
     */
    private void wosCreatedEventListener(WoCreatedEvent woCreatedEvent) {
        Set<String> tacheSet = new HashSet<>();
        tacheSet.add(BasicCode.OUTSIDElINE_CHECK);
        tacheSet.add(BasicCode.TRANS_CHECK);
        tacheSet.add(BasicCode.DATA_CHECK);
        tacheSet.add(BasicCode.CHANGE_CHECK);
        tacheSet.add(BasicCode.OTHER_CHECK);
        tacheSet.add(BasicCode.ACCESS_CHECK);
        if(tacheSet.contains(woCreatedEvent.getTacheId())){
            // 获取工单派发对象，根据工单对象找到所属区域(部门)
            String woId = woCreatedEvent.getWoId();
            Map<String,Object> param = new HashMap<>();
            param.put("dispObjId",woCreatedEvent.getDispObjId());
            param.put("dispObjTye",woCreatedEvent.getDispObjTye());
            param.put("tacheCode",woCreatedEvent.getTacheCode());

            List<Map<String,Object>> dispList = eventListenerDao.queryDispInfo(param);
            // 入库工单区域(部门)表 dispObjTye dispObjId tacheCode
            Map<String,Object> dispMap = dispList.get(0);
            dispMap.put("WO_ID",woId);
            eventListenerDao.insertWoDept(dispMap);
        }
    }
}
