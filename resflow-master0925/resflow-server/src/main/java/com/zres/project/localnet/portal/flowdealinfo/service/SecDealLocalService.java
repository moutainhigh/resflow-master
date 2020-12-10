package com.zres.project.localnet.portal.flowdealinfo.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.axis.utils.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderQrySecondaryDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.listener.util.EnmuValueUtil;
import com.zres.project.localnet.portal.logInfo.service.TacheDealLogIntf;
import com.zres.project.localnet.portal.util.OrderTrackOperType;

import com.ztesoft.res.frame.flow.common.dto.FlowRollBackReasonDTO;
import com.ztesoft.res.frame.flow.common.dto.FlowTacheDTO;
import com.ztesoft.res.frame.flow.common.dto.FlowWoDTO;
import com.ztesoft.res.frame.flow.common.lock.impl.DatabaseLock;
import com.ztesoft.res.frame.flow.common.lock.intf.DistributeLock;
import com.ztesoft.res.frame.flow.common.service.FlowActionHandler;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;

@Service
public class SecDealLocalService implements SecDealLocalServiceInf
{

    Logger logger = LoggerFactory.getLogger(SecDealLocalService.class);

    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private FlowActionHandler flowActionHandler;
    @Autowired
    private OrderQrySecondaryDao orderQrySecondaryDao;
    @Autowired
    private SecDealLocalServiceInf secDealLocalServiceInf;
    @Autowired
    private OrderDealService orderDealService;
    @Autowired
    private ListenerOrderServiceIntf listenerOrderServiceIntf;
    @Autowired
    private TacheDealLogIntf tacheDealLogIntf;


    @Override
    public Map<String, Object> rollBackWoOrderSec(Map<String, Object> params) {
            logger.info(">>>>>>>>>>>>>>>>>通知本地网退单>>>>>>>>>>>>>>>>>>>>>>>");
            Map<String, Object> resMap = new HashMap<String, Object>();
            Map<String, Object> operStaffInfoMap = new HashMap<String, Object>();
            String operStaffId = "-1";
            String operStaffName = "-1";
            if (ThreadLocalInfoHolder.getLoginUser() == null) {
                operStaffId = "-1";
                operStaffName = "二干";
                operStaffInfoMap.put("ORG_ID", "-1");
                operStaffInfoMap.put("ORG_NAME", "二干");
                operStaffInfoMap.put("USER_PHONE", "-1");
                operStaffInfoMap.put("USER_EMAIL", "-1");
            } else {
                operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
                operStaffName = ThreadLocalInfoHolder.getLoginUser().getUserName();
                operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(operStaffId));
            }
            String woId = MapUtils.getString(params, "WOID");
            String orderId = MapUtils.getString(params, "ORDERID");
            String tacheId = MapUtils.getString(params, "TACHEID");
            String remark = MapUtils.getString(params, "remark");
            String flagAct = MapUtils.getString(params, "flag");
            DistributeLock lock = new DatabaseLock(woId);
            try {
                List<FlowRollBackReasonDTO> flowRollBackReasonDTOs = flowActionHandler.queryRollBackReasons(woId);
                FlowRollBackReasonDTO flowRollBackReasonDTO = null;
                String toTacheId = "";
                if ("二干".equals(flagAct)) {
                    if (BasicCode.RENT.equals(tacheId)) {
                        toTacheId = BasicCode.CROSS_WHOLE_COURDER_TEST;
                    }
                    for (int i = 0; i < flowRollBackReasonDTOs.size(); i++) {
                        FlowRollBackReasonDTO _flowRollBackReasonDTO = flowRollBackReasonDTOs.get(i);
                        FlowTacheDTO flowTacheDTO = _flowRollBackReasonDTO.getToTache();
                        String _toTacheId = flowTacheDTO.getId();
                        if (toTacheId.equals(_toTacheId)) {
                            flowRollBackReasonDTO = _flowRollBackReasonDTO;
                        }
                    }
                }
                lock.lock();
                logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>加锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                Map<String, Object> woOrderState = orderDealDao.qryWoOrderState(woId);
                if (OrderTrackOperType.WO_ORDER_STATE_2.equals(MapUtils.getString(woOrderState, "WO_STATE"))){
                    flowActionHandler.rollBackWo(operStaffId, woId, flowRollBackReasonDTO, remark);
                }
                Map<String, Object> logDataMap = new HashMap<String, Object>();
                logDataMap.put("woId", woId);
                logDataMap.put("orderId", orderId);
                logDataMap.put("remark", remark);
                logDataMap.put("tacheId", tacheId);
                logDataMap.put("operType", OrderTrackOperType.OPER_TYPE_5);
                logDataMap.put("operStaffInfoMap", operStaffInfoMap);
                logDataMap.put("action", "退单");
                logDataMap.put("trackMessage", "[退单]");
                tacheDealLogIntf.addTrackLog(logDataMap);
                /*Map<String, Object> operLogMap = new HashMap<String, Object>();
                operLogMap.put("orderId", orderId);
                operLogMap.put("woOrdId", woId);
                operLogMap.put("trackOrgId", MapUtils.getString(operStaffInfoMap, "ORG_ID"));
                operLogMap.put("trackOrgName", MapUtils.getString(operStaffInfoMap, "ORG_NAME"));
                operLogMap.put("trackDate", new java.sql.Date(new java.util.Date().getTime()));
                operLogMap.put("createDate", new java.sql.Date(new java.util.Date().getTime()));
                operLogMap.put("trackStaffId", operStaffId);
                operLogMap.put("trackStaffName", operStaffName);
                operLogMap.put("trackStaffPhone", MapUtils.getString(operStaffInfoMap, "USER_PHONE"));
                operLogMap.put("trackStaffEmail", MapUtils.getString(operStaffInfoMap, "USER_EMAIL"));
                String trackMessage = "[" + operStaffName + "将工单单号：" + woId + "][退单]";
                operLogMap.put("trackMessage", trackMessage);
                operLogMap.put("trackContent", "[退单]" + remark);
                operLogMap.put("operType", OrderTrackOperType.OPER_TYPE_5);
                orderDealDao.insertTrackLogInfo(operLogMap);*/
                resMap.put("success", true);
                resMap.put("message", "回退成功!");
            } catch (Exception e) {
                e.printStackTrace();
                resMap.put("success", false);
                resMap.put("message", "回退失败!" + e);
            }finally {
                lock.unlock();
                logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>finally主动解锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            }
            return resMap;
    }

    @Override
    public Map<String, Object> qrySecondDataMake(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap<String, Object>();
        List<Map<String, Object>> secondDataMakeList = Lists.newArrayList();
        try{
            List<Map<String, Object>> secondDataMakeListT = orderQrySecondaryDao.qrySecondDataMake(param);
            if(!CollectionUtils.isEmpty(secondDataMakeListT)){
                secondDataMakeList.addAll(secondDataMakeListT);
            }
            resMap.put("flag",true);
        }catch (Exception e){
            resMap.put("flag",false);
            resMap.put("message",e.getMessage());
        }
        resMap.put("data",secondDataMakeList);
        return resMap;
    }

    @Override
    public Map<String, Object> qrySecondResMake(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap<String, Object>();
        List<Map<String, Object>> secondResMakeList = Lists.newArrayList();
        try{
            List<Map<String, Object>> secondResMakeListT = orderQrySecondaryDao.qrySecondResMake(param);
            if(!CollectionUtils.isEmpty(secondResMakeListT)){
                secondResMakeList.addAll(secondResMakeListT);
            }
            resMap.put("flag",true);
        }catch (Exception e){
            resMap.put("flag",false);
            resMap.put("message",e.getMessage());
        }
        resMap.put("data",secondResMakeList);
        return resMap;
    }

    @Override
    public Map<String, Object> qrySecToLocalData(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap<String, Object>();
        List<Map<String, Object>> secondDataMakeList = Lists.newArrayList();
        try{
            List<Map<String, Object>> secondDataMakeListT = orderQrySecondaryDao.qrySecToLocalData(param);
            if(!CollectionUtils.isEmpty(secondDataMakeListT)){
                secondDataMakeList.addAll(secondDataMakeListT);
            }
            resMap.put("flag",true);
        }catch (Exception e){
            resMap.put("flag",false);
            resMap.put("message",e.getMessage());
        }
        resMap.put("data",secondDataMakeList);
        return resMap;
    }

    @Override
    public Map<String, Object> summaryRollBack(Map<String, Object> params) {
        logger.info(">>>>>>>>>>>>>>>>>完工汇总环节退单>>>>>>>>>>>>>>>>>>>>>>>");
        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        String operStaffName = ThreadLocalInfoHolder.getLoginUser().getUserName();
        Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(operStaffId));
        Map<String, Object> resMap = new HashMap<String, Object>();
        String orderId = MapUtils.getString(params, "orderId");
        params.put("operStaffInfoMap", operStaffInfoMap);
        params.put("operStaffId", operStaffId);
        params.put("operStaffName", operStaffName);
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("orderId", orderId);
        /**
         * 数据制作子流程退单专业
         * 1，用户页面选择数据制作退单专业不为空时，先退用户选择的专业子流程退单
         * 2，将二干主流程退单到专业数据制作环节
         */
        String specialtyStr = MapUtils.getString(params, "specialty");
        if (!StringUtils.isEmpty(specialtyStr) && !"[]".equals(specialtyStr)){
            resMap = secDealLocalServiceInf.rollBackData(params);
            if (MapUtils.getBoolean(resMap, "success")){
                try{
                    params.put("tacheFlag","SPECIALTY_DATA_EXEC");
                    resMap = secDealLocalServiceInf.summaryRollBackOrder(params);
                }catch (Exception e) {
                    e.printStackTrace();
                    resMap.put("success", false);
                    resMap.put("message", "完工汇总退专业数据制作失败!" + e);
                }
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
        String localDispatchAreaStr = MapUtils.getString(params, "localDispatchArea");
        if (!StringUtils.isEmpty(localDispatchAreaStr)  && !"[]".equals(localDispatchAreaStr)){
            resMap = secDealLocalServiceInf.rollBackLocal(params);
            if (MapUtils.getBoolean(resMap, "success")){
                try{
                    params.put("tacheFlag","LOCAL_SCHEDULE");
                    resMap = secDealLocalServiceInf.summaryRollBackOrder(params);
                }catch (Exception e) {
                    e.printStackTrace();
                    resMap.put("success", false);
                    resMap.put("message", "完工汇总退本地调度失败!" + e);
                }
            }else {
                return resMap;
            }
        }
        return resMap;
    }

    @Override
    public Map<String, Object> rollBackData(Map<String, Object> params) {
        Map<String, Object> resMap = new HashMap<String, Object>();
        String orderId = MapUtils.getString(params, "orderId");
        String specialtyStr = MapUtils.getString(params, "specialty");
        //String operStaffId = MapUtils.getString(params, "operStaffId");
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("orderId", orderId);
        specialtyStr = StringUtils.strip(specialtyStr, "[]");
        String[] specialty = specialtyStr.split(",");
        for (int i = 0; i < specialty.length ; i++) {
            paramsMap.put("spacialtyCode", StringUtils.strip(specialty[i], "\"\""));
            List<Map<String, Object>> specialtyList = orderQrySecondaryDao.qrySecondDataMake(paramsMap);
            for (int j = 0; j < specialtyList.size() ; j++) {
                String woId = MapUtils.getString(specialtyList.get(j),"WO_ID");
                String childOrderId = MapUtils.getString(specialtyList.get(j),"ORDER_ID");
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
                    Map<String, Object> logMap = new HashMap<>();
                    logMap.putAll(params);
                    logMap.put("woId",woId);
                    logMap.put("orderId",childOrderId);
                    secDealLocalServiceInf.addRollBackLog(logMap);
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

    @Override
    public Map<String, Object> rollBackRes(Map<String, Object> params) {
        Map<String, Object> resMap = new HashMap<String, Object>();
        String orderId = MapUtils.getString(params, "orderId");
        String specialtyResStr = MapUtils.getString(params, "specialtyRes");
        //String operStaffId = MapUtils.getString(params, "operStaffId");
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("orderId", orderId);
        specialtyResStr = StringUtils.strip(specialtyResStr, "[]");
        String[] specialty = specialtyResStr.split(",");
        for (int i = 0; i < specialty.length ; i++) {
            paramsMap.put("spacialtyCode", StringUtils.strip(specialty[i], "\"\""));
            List<Map<String, Object>> specialtyList = orderQrySecondaryDao.qrySecondResMake(paramsMap);
            for (int j = 0; j < specialtyList.size() ; j++) {
                String woId = MapUtils.getString(specialtyList.get(j),"WO_ID");
                String childOrderId = MapUtils.getString(specialtyList.get(j),"ORDER_ID");
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
                        flowActionHandler.complateWo("-2000", woDTO);
                    }
                    Map<String, Object> logMap = new HashMap<>();
                    logMap.putAll(params);
                    logMap.put("woId",woId);
                    logMap.put("orderId",childOrderId);
                    secDealLocalServiceInf.addRollBackLog(logMap);
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

    @Override
    public Map<String, Object> rollBackLocal(Map<String, Object> params) {
        Map<String, Object> resMap = new HashMap<String, Object>();
        String orderId = MapUtils.getString(params, "orderId");
        String localDispatchAreaStr = MapUtils.getString(params, "localDispatchArea");
        String remark = MapUtils.getString(params, "remark");
        String operStaffId = MapUtils.getString(params, "operStaffId");
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("orderId", orderId);
        localDispatchAreaStr = StringUtils.strip(localDispatchAreaStr, "[]");
        String[] localDispatchArea = localDispatchAreaStr.split(",");
        for (int i = 0; i < localDispatchArea.length ; i++) {
            paramsMap.put("regionId", StringUtils.strip(localDispatchArea[i], "\"\""));
            List<Map<String, Object>> localDispatchAreaList = orderQrySecondaryDao.qrySecToLocalData(paramsMap);
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
                    secDealLocalServiceInf.addRollBackLog(params);
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
        return resMap;
    }

    @Override
    public Map<String, Object> summaryRollBackOrder(Map<String, Object> params) {
        Map<String, Object> resMap = new HashMap<String, Object>();
        String woId = MapUtils.getString(params, "woId");
        String remark = "完工汇总退单！";
        String operStaffId = MapUtils.getString(params, "operStaffId");
        String tacheFlag = MapUtils.getString(params, "tacheFlag");
        DistributeLock lock = new DatabaseLock(woId);
        try {
            List<FlowRollBackReasonDTO> flowRollBackReasonDTOs = flowActionHandler.queryRollBackReasons(woId);
            FlowRollBackReasonDTO flowRollBackReasonDTO = null;
            String backReasonCode = "";
            if ("LOCAL_SCHEDULE".equals(tacheFlag)){
                backReasonCode = "510101044_TO_510101043,510101051_TO_510101083";
            }else if ("SPECIALTY_DATA_EXEC".equals(tacheFlag)){
                backReasonCode = "510101044_TO_510101042,510101051_TO_510101082";
            }
            for (int k = 0; k < flowRollBackReasonDTOs.size(); k++) {
                FlowRollBackReasonDTO _flowRollBackReasonDTO = flowRollBackReasonDTOs.get(k);
                String _backReasonCode = _flowRollBackReasonDTO.getCode();
                if (backReasonCode.indexOf(_backReasonCode) > -1) {
                    flowRollBackReasonDTO = _flowRollBackReasonDTO;
                }
            }
            lock.lock();
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>加锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            Map<String, Object> woOrderState = orderDealDao.qryWoOrderState(woId);
            if (OrderTrackOperType.WO_ORDER_STATE_2.equals(MapUtils.getString(woOrderState, "WO_STATE"))){
                flowActionHandler.rollBackWo(operStaffId, woId, flowRollBackReasonDTO, remark);
            }
            params.put("remark",remark);
            secDealLocalServiceInf.addRollBackLog(params);
            resMap.put("success", true);
            resMap.put("message", "完工汇总退单成功!");
        }catch (Exception e) {
            e.printStackTrace();
            resMap.put("success", false);
            resMap.put("message", "完工汇总退单失败!" + e);
        }finally {
            lock.unlock();
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>finally主动解锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        }
        return resMap;
    }

    @Override
    public void addRollBackLog(Map<String, Object> params) {
        String orderId = MapUtils.getString(params, "orderId");
        String woId = MapUtils.getString(params, "woId");
        String remark = MapUtils.getString(params, "remark");
        Map operStaffInfoMap = MapUtils.getMap(params, "operStaffInfoMap");
        /*String operStaffId = MapUtils.getString(params, "operStaffId");
        String operStaffName = MapUtils.getString(params, "operStaffName");
        Map<String, Object> operLogMap = new HashMap<String, Object>();
        operLogMap.put("orderId", orderId);
        operLogMap.put("woOrdId", woId);
        operLogMap.put("trackOrgId", MapUtils.getString(operStaffInfoMap, "ORG_ID"));
        operLogMap.put("trackOrgName", MapUtils.getString(operStaffInfoMap, "ORG_NAME"));
        operLogMap.put("trackDate", new java.sql.Date(new java.util.Date().getTime()));
        operLogMap.put("createDate", new java.sql.Date(new java.util.Date().getTime()));
        operLogMap.put("trackStaffId", operStaffId);
        operLogMap.put("trackStaffName", operStaffName);
        operLogMap.put("trackStaffPhone", MapUtils.getString(operStaffInfoMap, "USER_PHONE"));
        operLogMap.put("trackStaffEmail", MapUtils.getString(operStaffInfoMap, "USER_EMAIL"));
        String trackMessage = "[" + operStaffName + "将工单单号：" + woId + "][退单]";
        operLogMap.put("trackMessage", trackMessage);
        operLogMap.put("trackContent", "[退单]" + remark);
        operLogMap.put("operType", OrderTrackOperType.OPER_TYPE_5);
        orderDealDao.insertTrackLogInfo(operLogMap);*/
        Map<String, Object> logDataMap = new HashMap<String, Object>();
        logDataMap.put("woId", woId);
        logDataMap.put("orderId", orderId);
        logDataMap.put("remark", remark);
        logDataMap.put("tacheId", BasicCode.SUMMARY_OF_COMPLETION);
        logDataMap.put("operType", OrderTrackOperType.OPER_TYPE_5);
        logDataMap.put("operStaffInfoMap", operStaffInfoMap);
        logDataMap.put("action", "退单");
        logDataMap.put("trackMessage", "[退单]");
        tacheDealLogIntf.addTrackLog(logDataMap);
    }

    @Override
    public int qryOrderIfConfigDispatch(Map<String, Object> params) {
        int flag = orderQrySecondaryDao.qryOrderIfConfigDispatch(MapUtils.getString(params, "srvOrdId"));
        return flag;
    }

    @Override
    public Map<String, Object> qryCircuitNum(Map<String, Object> params) {
        Map<String, Object> resMap = new HashMap<String, Object>();
        try{
            int flag = orderQrySecondaryDao.qryOrderIfConfigDispatch(MapUtils.getString(params, "srvOrdId"));
            if (flag == 1){
                resMap.put("circuitNum", MapUtils.getString(orderQrySecondaryDao.qryCircuitNum(params), "ATTR_VALUE"));
                resMap.put("ifConfig", orderQrySecondaryDao.qryCircuitIfConfig(params));
                resMap.put("success", true);
            } else {
                resMap.put("success", false);
                resMap.put("flag", 0);
                resMap.put("message", "该电路未起草调单，请先起草调单。。。");
            }
        } catch (Exception e){
            resMap.put("success", false);
            resMap.put("flag", 1);
            resMap.put("message", "查询是否有电路编号和是否二干资源分配失败！！" + e);
        }
        return resMap;
    }

    @Override
    public int insertCircuitNum(Map<String, Object> params) {
        int res = 1;
        Map<String, Object> circuitMap = orderQrySecondaryDao.qryCircuitNum(params);
        String circuitNum = MapUtils.getString(circuitMap, "ATTR_VALUE");
        String attrInfoId = MapUtils.getString(circuitMap, "ATTR_INFO_ID");
        if (StringUtils.isEmpty(circuitNum) && StringUtils.isEmpty(attrInfoId)){
            //集客来单这两个值都是空的要重新插入
            res = orderQrySecondaryDao.insertCircuitNum(params);
        }else if (StringUtils.isEmpty(circuitNum) && !StringUtils.isEmpty(attrInfoId)){
            res = orderQrySecondaryDao.updateCircuitNum(params);
        }
        return res;
    }

    @Override
    public Map<String, Object> qryDispatchData(String orderId) {
        Map<String, Object> resMap = new HashMap<String, Object>();
        Map<String, Object> dispatchDataMap = orderQrySecondaryDao.qryDispatchData(orderId);
        if("是".equals(MapUtils.getString(dispatchDataMap, "TO_BDW"))){
            resMap.put("ifToLocal",true);
        }else if("否".equals(MapUtils.getString(dispatchDataMap, "TO_BDW"))){
            resMap.put("ifToLocal",false);
        }
        return resMap;
    }

    @Override
    public boolean sendCopyOrder(Map<String, Object> params) {
        boolean ifSuccess = true;
        String srvOrdId = MapUtils.getString(params, "srvOrdId");
        Map<String, Object> copySendObj = orderQrySecondaryDao.qryCopySendObj(srvOrdId);
        String copySendObjStr = MapUtils.getString(copySendObj, "DISPATCH_COPY_ORG");
        if(MapUtils.isNotEmpty(copySendObj) && !"".equals(copySendObjStr)){
            String[] copySendObjs = copySendObjStr.split(",");
            for (int i = 0; i < copySendObjs.length; i++) {
                params.put("objType","260000003");
                params.put("objId",copySendObjs[i]);
                Map<String, Object> ccMap = orderDealService.ccWoOrder(params);
                if (!MapUtils.getBoolean(ccMap, "success")){
                    logger.error("---------二干调度环节抄送失败------" + MapUtils.getString(ccMap, "message"));
                    ifSuccess = false;
                    return ifSuccess;
                }
            }
        }
        return ifSuccess;
    }

    @Override
    public int qryChildOrderDealing(String orderId) {
        /**
         * 二干调度退单时，查询二干启的子流程和下发本地的流程 执行中的数量
         */
        int resNum = 0;
        int secChildFlowNum = orderQrySecondaryDao.qryChildOrderDealing(orderId);
        if (secChildFlowNum > resNum){
            return secChildFlowNum;
        }
        int localChildFlowNum = orderQrySecondaryDao.qryToLocalChildOrderDealing(orderId);
        if (localChildFlowNum > resNum){
            return localChildFlowNum;
        }
        return resNum;
    }

    @Override
    public Map<String, Object> supplementOrder(Map<String, Object> params) {
        Map<String, Object> resMap = new HashMap<>();
        String orderId = MapUtils.getString(params, "orderId");
        params = orderQrySecondaryDao.qryDealingChildFlowTache(orderId, OrderTrackOperType.WO_ORDER_STATE_10);
        if (MapUtils.isNotEmpty(params)){
            String tacheCode = MapUtils.getString(params, "TACHECODE");
            Map<String, Object> paramsMap = new HashMap<String, Object>();
            paramsMap.put("orderId",orderId);
            paramsMap.put("woId",MapUtils.getString(params, "WOID"));
            if (EnmuValueUtil.TO_DATA_CREATE_AND_SCHEDULE.equals(tacheCode)
                    || EnmuValueUtil.TO_DATA_CREATE_AND_SCHEDULE_2.equals(tacheCode)){ //数据制作和本地调度
                params = listenerOrderServiceIntf.startData(paramsMap);
                params = listenerOrderServiceIntf.startSchedule(paramsMap);
            }else if (EnmuValueUtil.SEC_SOURCE_DISPATCH.equals(tacheCode)
                    || EnmuValueUtil.SEC_SOURCE_DISPATCH_2.equals(tacheCode)){ //二干资源分配
                params = listenerOrderServiceIntf.startResConfig(paramsMap);
            }
            if (MapUtils.getBoolean(params, "success")){
                resMap.put("success", true);
                resMap.put("message", "补单成功！");
            } else {
                resMap.put("success", false);
                resMap.put("message", "补单失败！" + MapUtils.getString(params, "message"));
            }
        }else {
            resMap.put("success", true);
            resMap.put("message", "补单成功！");
        }
        return resMap;
    }

}
