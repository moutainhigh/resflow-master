package com.zres.project.localnet.portal.flowdealinfo.service;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderQrySecondaryDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.logInfo.service.TacheDealLogIntf;
import com.zres.project.localnet.portal.util.OrderTrackOperType;
import com.ztesoft.res.frame.core.util.MapUtil;
import com.ztesoft.res.frame.flow.common.dto.FlowRollBackReasonDTO;
import com.ztesoft.res.frame.flow.common.dto.FlowTacheDTO;
import com.ztesoft.res.frame.flow.common.dto.FlowWoDTO;
import com.ztesoft.res.frame.flow.common.lock.impl.DatabaseLock;
import com.ztesoft.res.frame.flow.common.lock.intf.DistributeLock;
import com.ztesoft.res.frame.flow.common.service.FlowActionHandler;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import com.ztesoft.res.frame.user.inf.UserInfo;
import org.apache.axis.utils.StringUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LocalAndSecondMutualService implements LocalAndSecondMutualServiceInf {

    Logger logger = LoggerFactory.getLogger(LocalAndSecondMutualService.class);

    @Autowired
    private OrderQrySecondaryDao orderQrySecondaryDao;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private FlowActionHandler flowActionHandler;
    @Autowired
    private CommonMethodDealWoOrderServiceInf commonMethodDealWoOrderServiceInf;
    @Autowired
    private TacheDealLogIntf tacheDealLogIntf;

    @Override
    public Map<String, Object> localBackOrderToSecond(Map<String, Object> backOrderParams) {
        logger.info(">>>>>>>>>>>>>>>>>本地调度退单二干调度>>>>>>>>>>>>>>>>>>>>>>>");
        /**
         * 退单二干环节
         */
        Map<String, Object> rollBackWoResult = new HashMap<String, Object>();
        String orderId = MapUtils.getString(backOrderParams, "orderId");
        //查询二干待数据制作与本地调度环节或者二干资源分配环节的工单
        Map<String,Object> dataScheduleOrderMap = orderQrySecondaryDao.qryDataScheduleOrder(orderId);
        if(MapUtils.isNotEmpty(dataScheduleOrderMap) && !"".equals(MapUtils.getString(dataScheduleOrderMap,"WO_ID"))){
            String woId = MapUtils.getString(dataScheduleOrderMap,"WO_ID");
            //修改待数据制作与本地调度环节的工单状态为处理中
            orderDealDao.updateWoStateByWoId(woId, OrderTrackOperType.WO_ORDER_STATE_2);
            List<FlowRollBackReasonDTO> flowRollBackReasonDTOs = flowActionHandler.queryRollBackReasons(woId);
            FlowRollBackReasonDTO flowRollBackReasonDTO = null;
            for (int i = 0; i < flowRollBackReasonDTOs.size(); i++) {
                FlowRollBackReasonDTO _flowRollBackReasonDTO = flowRollBackReasonDTOs.get(i);
                FlowTacheDTO flowTacheDTO = _flowRollBackReasonDTO.getToTache();
                String _toTacheId = flowTacheDTO.getId();
                if (BasicCode.SECONDARY_SCHEDULE.equals(_toTacheId)
                        || BasicCode.SECONDARY_SCHEDULE_2.equals(_toTacheId)) { //退单到二干调度
                    flowRollBackReasonDTO = _flowRollBackReasonDTO;
                }
            }
            backOrderParams.put("woId", woId);
            backOrderParams.put("flowRollBackReasonDTO", flowRollBackReasonDTO);
            rollBackWoResult = commonMethodDealWoOrderServiceInf.commonRollBackWo(backOrderParams);
        }else {
            //这里有可能之前有本地调度的单子退回二干，二干待数据制作与本地调度环节没查到，所以上层逻辑修改本地网单子的状态就好
            rollBackWoResult.put("success", true);
            rollBackWoResult.put("message", "回退成功!");
        }
        return rollBackWoResult;
    }

    @Override
    public Map<String, Object> crossTestRollBack(Map<String, Object> params) {
        logger.info(">>>>>>>>>>>>>>>>>二干下发---跨域全程调测环节退单>>>>>>>>>>>>>>>>>>>>>>>");
        Map<String, Object> resMap = new HashMap<String, Object>();
        /**
         * 数据制作子流程退单专业
         * 1，用户页面选择数据制作退单专业不为空时，先退用户选择的专业子流程退单
         * 2，将二干主流程退单到专业数据制作环节
         */
        String specialtyStr = MapUtils.getString(params, "dataMakeData");
        if (!StringUtils.isEmpty(specialtyStr) && !"[]".equals(specialtyStr)){
            resMap = this.rollBackData(params);
            if (MapUtils.getBoolean(resMap, "success")){
                this.fullCommissSecondRollBack(params);
            }else {
                return resMap;
            }
        }
        /**
         * 资源施工子流程退单专业
         * 1，用户页面选择资源施工退单专业不为空时，先退用户选择的专业子流程退单
         * 2，将二干主流程退单到专业资源施工子流程环节
         */
        String resSpecialtyStr = MapUtils.getString(params, "resMakeData");
        if (!StringUtils.isEmpty(resSpecialtyStr) && !"[]".equals(resSpecialtyStr)){
            resMap = this.rollBackResMake(params);
            if (MapUtils.getBoolean(resMap, "success")){
                this.fullCommissSecondRollBack(params);
            }else {
                return resMap;
            }
        }
        /**
         * 二干下发本地网区域退单
         * 1，用户页面选择本地网退单区域不为空时，先退用户选择的本地网区域流程到本地测试环节
         *    如果没有退主调局，修改跨域全程调测环节的工单状态
         * 2，将二干主流程退单到本地调度环节
         */
        String localDispatchAreaStr = MapUtils.getString(params, "subLocalTestData");
        if (!StringUtils.isEmpty(localDispatchAreaStr)  && !"[]".equals(localDispatchAreaStr)){
            resMap = this.rollBackLocal(params);
            if (MapUtils.getBoolean(resMap, "success")){
                this.fullCommissSecondRollBack(params);
            }else {
                return resMap;
            }
        }
        //本地跨域全程调测有没有退，如果没有修改状态为等二干通知  --主调的跨域全程调测环节不能处理了
        String srvOrdId = MapUtils.getString(params, "srvOrdId");
        Map<String, Object> crossTestDoingMap = orderDealDao.qryCrossTestDoing(srvOrdId);
        if (MapUtils.isNotEmpty(crossTestDoingMap)){
            orderDealDao.updateWoStateByWoId(MapUtils.getString(crossTestDoingMap, "WO_ID"), OrderTrackOperType.WO_ORDER_STATE_15);
        }
        return resMap;
    }

    private Map<String, Object> rollBackData(Map<String, Object> params) {
        Map<String, Object> resMap = new HashMap<String, Object>();
        //String orderId = MapUtils.getString(params, "orderId");
        String srvOrdId = MapUtils.getString(params, "srvOrdId");
        String specialtyStr = MapUtils.getString(params, "dataMakeData");
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        //paramsMap.put("orderId", orderId);
        paramsMap.put("srvOrdId", srvOrdId);
        specialtyStr = StringUtils.strip(specialtyStr, "[]");
        String[] specialty = specialtyStr.split(",");
        for (int i = 0; i < specialty.length ; i++) {
            paramsMap.put("spacialtyCode", StringUtils.strip(specialty[i], "\"\""));
            List<Map<String, Object>> specialtyList = orderDealDao.qrySecondDataMakeList(paramsMap);
            for (int j = 0; j < specialtyList.size() ; j++) {
                String woId = MapUtils.getString(specialtyList.get(j),"WO_ID");
                DistributeLock lock = new DatabaseLock(woId);
                try {
                    lock.lock();
                    logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>加锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                    List<HashMap<String, String>> operAttrsList = new ArrayList<HashMap<String, String>>();
                    HashMap<String, String> operAttrsMap = new HashMap<String, String>();
                    operAttrsMap.put("KEY", "SPECIALTY_DATA_PRODUCTION_CHECK");
                    operAttrsMap.put("VALUE", "1");
                    operAttrsList.add(operAttrsMap);
                    FlowWoDTO woDTO = new FlowWoDTO();
                    woDTO.setWoId(woId);
                    woDTO.setOperAttrs(operAttrsList);
                    Map<String, Object> woOrderState = orderDealDao.qryWoOrderState(woId);
                    if (OrderTrackOperType.WO_ORDER_STATE_2.equals(MapUtils.getString(woOrderState, "WO_STATE"))){
                        flowActionHandler.complateWo("-2000", woDTO); //operStaffId 退单数据制作，提交人用-2000
                    }
                    this.addRollBackLog(params);
                    resMap.put("success", true);
                    resMap.put("message", "数据制作子流程退单成功!");
                }catch (Exception e) {
                    e.printStackTrace();
                    resMap.put("success", false);
                    resMap.put("message", "数据制作子流程退单失败!" + e);
                    return resMap;
                }finally {
                    lock.unlock();
                    logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>finally主动解锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                }
            }
            paramsMap.remove("spacialtyCode");
        }
        return resMap;
    }


    private Map<String, Object> rollBackResMake(Map<String, Object> params) {
        Map<String, Object> resMap = new HashMap<String, Object>();
        String srvOrdId = MapUtils.getString(params, "srvOrdId");
        String resSpecialtyStr = MapUtils.getString(params, "resMakeData");
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("srvOrdId", srvOrdId);
        resSpecialtyStr = StringUtils.strip(resSpecialtyStr, "[]");
        String[] specialty = resSpecialtyStr.split(",");
        for (int i = 0; i < specialty.length ; i++) {
            paramsMap.put("spacialtyCode", StringUtils.strip(specialty[i], "\"\""));
            List<Map<String, Object>> specialtyList = orderDealDao.qrySecondResMakeList(paramsMap);
            for (int j = 0; j < specialtyList.size() ; j++) {
                String woId = MapUtils.getString(specialtyList.get(j),"WO_ID");
                DistributeLock lock = new DatabaseLock(woId);
                try {
                    lock.lock();
                    logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>加锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                    List<HashMap<String, String>> operAttrsList = new ArrayList<HashMap<String, String>>();
                    HashMap<String, String> operAttrsMap = new HashMap<String, String>();
                    operAttrsMap.put("KEY", "SPECIALTY_RES_CONSTRUCTION_CHECK");
                    operAttrsMap.put("VALUE", "1");
                    operAttrsList.add(operAttrsMap);
                    FlowWoDTO woDTO = new FlowWoDTO();
                    woDTO.setWoId(woId);
                    woDTO.setOperAttrs(operAttrsList);
                    Map<String, Object> woOrderState = orderDealDao.qryWoOrderState(woId);
                    if (OrderTrackOperType.WO_ORDER_STATE_2.equals(MapUtils.getString(woOrderState, "WO_STATE"))){
                        flowActionHandler.complateWo("-2000", woDTO); //operStaffId 退单数据制作，提交人用-2000
                    }
                    this.addRollBackLog(params);
                    resMap.put("success", true);
                    resMap.put("message", "资源施工子流程退单成功!");
                }catch (Exception e) {
                    e.printStackTrace();
                    resMap.put("success", false);
                    resMap.put("message", "资源施工子流程退单失败!" + e);
                    return resMap;
                }finally {
                    lock.unlock();
                    logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>finally主动解锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                }
            }
            paramsMap.remove("spacialtyCode");
        }
        return resMap;
    }


    private Map<String, Object> rollBackLocal(Map<String, Object> params) {
        Map<String, Object> resMap = new HashMap<String, Object>();
        //String orderId = MapUtils.getString(params, "orderId");
        String srvOrdId = MapUtils.getString(params, "srvOrdId");
        String localDispatchAreaStr = MapUtils.getString(params, "subLocalTestData");
        String remark = MapUtils.getString(params, "remark");
        String operStaffId = MapUtils.getString(params, "operStaffId");
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        //paramsMap.put("orderId", orderId);
        paramsMap.put("srvOrdId", srvOrdId);
        localDispatchAreaStr = StringUtils.strip(localDispatchAreaStr, "[]");
        String[] localDispatchArea = localDispatchAreaStr.split(",");
        for (int i = 0; i < localDispatchArea.length ; i++) {
            paramsMap.put("regionId", StringUtils.strip(localDispatchArea[i], "\"\""));
            List<Map<String, Object>> localDispatchAreaList = orderDealDao.qrySubLocalTestDataList(paramsMap);
            for (int j = 0; j < localDispatchAreaList.size() ; j++) {
                String woId = MapUtils.getString(localDispatchAreaList.get(j),"WO_ID");
                DistributeLock lock = new DatabaseLock(woId);
                try {
                    List<FlowRollBackReasonDTO> flowRollBackReasonDTOs = flowActionHandler.queryRollBackReasons(woId);
                    FlowRollBackReasonDTO flowRollBackReasonDTO = null;
                    String toTacheId = "";
                    toTacheId = BasicCode.LOCAL_TEST;
                    for (int k = 0; k < flowRollBackReasonDTOs.size(); k++) {
                        FlowRollBackReasonDTO _flowRollBackReasonDTO = flowRollBackReasonDTOs.get(k);
                        FlowTacheDTO flowTacheDTO = _flowRollBackReasonDTO.getToTache();
                        String _toTacheId = flowTacheDTO.getId();
                        if (toTacheId.equals(_toTacheId)) {
                            flowRollBackReasonDTO = _flowRollBackReasonDTO;
                        }
                    }
                    lock.lock();
                    logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>加锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                    Map<String, Object> woOrderState = orderDealDao.qryWoOrderState(woId);
                    if (!OrderTrackOperType.WO_ORDER_STATE_2.equals(MapUtils.getString(woOrderState, "WO_STATE"))){
                        orderDealDao.updateWoStateByWoId(woId,OrderTrackOperType.WO_ORDER_STATE_2);
                    }
                    flowActionHandler.rollBackWo(operStaffId, woId, flowRollBackReasonDTO, remark);
                    this.addRollBackLog(params);
                    resMap.put("success", true);
                    resMap.put("message", "二干下发本地网区域退单成功!");
                }catch (Exception e) {
                    e.printStackTrace();
                    resMap.put("success", false);
                    resMap.put("message", "二干下发本地网区域退单失败!" + e);
                    return resMap;
                }finally {
                    lock.unlock();
                    logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>finally主动解锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                }
            }
            paramsMap.remove("regionId");
        }
        /*//本地退单完成后，查询本地跨域全程调测有没有退，如果没有修改状态为等二干通知  --主调的跨域全程调测环节不能处理了
        Map<String, Object> crossTestDoingMap = orderDealDao.qryCrossTestDoing(srvOrdId);
        if (MapUtils.isNotEmpty(crossTestDoingMap)){
            orderDealDao.updateWoStateByWoId(MapUtils.getString(crossTestDoingMap, "WO_ID"), OrderTrackOperType.WO_ORDER_STATE_15);
        }*/
        return resMap;
    }

    /**
     * 查询二干客户电路全程调测退到待专业数据制作与本地调度
     */
    private void fullCommissSecondRollBack(Map<String, Object> params) {
        String remark = MapUtils.getString(params, "remark");
        String srvOrdId = MapUtils.getString(params, "srvOrdId");
        Map<String, Object> dataParamMap = new HashMap<String, Object>();
        dataParamMap.put("srvOrdId",srvOrdId);
        dataParamMap.put("tacheId",BasicCode.FULL_COMMISSIONING);
        dataParamMap.put("woState","290000112"); //先查询等待本地网处理中，然后再修改为处理中
        Map<String, Object> fullComMap = orderDealDao.qrySecondDataMakeRollBackList(dataParamMap);
        if(!MapUtil.isEmpty(fullComMap)){
            String woId = MapUtils.getString(fullComMap, "WO_ID");
            //String orderId = MapUtils.getString(fullComMap, "ORDER_ID");
            String dealUserId = MapUtils.getString(fullComMap, "DEAL_USER_ID");
            //Map<String, Object> operStaffInfoMap = new HashMap<String, Object>();
            UserInfo user = ThreadLocalInfoHolder.getLoginUser();
            //orderDealDao.updateWoStateByWoId(woId,OrderTrackOperType.WO_ORDER_STATE_2);
            orderDealDao.updateWoStateAndUserByWoId(woId,user.getUserId(),OrderTrackOperType.WO_ORDER_STATE_2);
            if(StringUtils.isEmpty(dealUserId)){
                dealUserId = user.getUserId();
            }
            //operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(dealUserId));
            FlowRollBackReasonDTO flowRollBackReasonDTO = null; //待专业数据制作与本地调度
            List<FlowRollBackReasonDTO> flowRollBackReasonDTOs = flowActionHandler.queryRollBackReasons(woId);
            for (int i = 0; i < flowRollBackReasonDTOs.size(); i++) {
                FlowRollBackReasonDTO _flowRollBackReasonDTO = flowRollBackReasonDTOs.get(i);
                FlowTacheDTO flowTacheDTO = _flowRollBackReasonDTO.getToTache();
                String _toTacheId = flowTacheDTO.getId();
                if (BasicCode.TO_DATA_CREATE_AND_SCHEDULE_2.equals(_toTacheId)) {
                    flowRollBackReasonDTO = _flowRollBackReasonDTO;
                }
            }
            DistributeLock lock = new DatabaseLock(woId);
            try{
                flowActionHandler.rollBackWo(dealUserId, woId, flowRollBackReasonDTO, remark);
                lock.lock();
                this.addRollBackLog(params);
            }catch (Exception e){
                throw e;
            }finally {
                lock.unlock();
            }
        }
    }

    private void addRollBackLog(Map<String, Object> params) {
        String orderId = MapUtils.getString(params, "orderId");
        String woId = MapUtils.getString(params, "woId");
        String remark = MapUtils.getString(params, "remark");
        Map operStaffInfoMap = MapUtils.getMap(params, "operStaffInfoMap");
        Map<String, Object> logDataMap = new HashMap<String, Object>();
        logDataMap.put("woId", woId);
        logDataMap.put("orderId", orderId);
        logDataMap.put("remark", remark);
        logDataMap.put("tacheId", BasicCode.FULL_COMMISSIONING);
        logDataMap.put("operType", OrderTrackOperType.OPER_TYPE_5);
        logDataMap.put("operStaffInfoMap", operStaffInfoMap);
        logDataMap.put("action", "退单");
        logDataMap.put("trackMessage", "[退单]");
        tacheDealLogIntf.addTrackLog(logDataMap);
    }
}
