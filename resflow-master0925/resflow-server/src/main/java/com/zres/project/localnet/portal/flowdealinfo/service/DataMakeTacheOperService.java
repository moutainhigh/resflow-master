package com.zres.project.localnet.portal.flowdealinfo.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderQrySecondaryDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.listener.util.EnmuValueUtil;
import com.zres.project.localnet.portal.logInfo.service.TacheDealLogIntf;
import com.zres.project.localnet.portal.util.OrderTrackOperType;

import net.sf.json.JSONArray;

import com.ztesoft.res.frame.core.util.ListUtil;
import com.ztesoft.res.frame.core.util.StringUtils;
import com.ztesoft.res.frame.flow.common.dto.FlowRollBackReasonDTO;
import com.ztesoft.res.frame.flow.common.dto.FlowTacheDTO;
import com.ztesoft.res.frame.flow.common.service.FlowActionHandler;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;

import com.github.pagehelper.util.StringUtil;

@Service
public class DataMakeTacheOperService implements DataMakeTacheOperServiceInf {

    Logger logger = LoggerFactory.getLogger(DataMakeTacheOperService.class);

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
    public Map<String, Object> dataMakeTacheBackOrder(Map<String, Object> backOrderParams) {
        logger.info(">>>>>>>>>>>>>>>>>专业数据制作退单>>>>>>>>>>>>>>>>>>>>>>>");
        Map<String, Object> rollBackWoResult = new HashMap<String, Object>();
        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        //Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(operStaffId));
        String orderId = MapUtils.getString(backOrderParams, "orderId"); //数据制作子流程的orderid
        Map parentOrder = orderDealDao.getParentOrder(orderId); //查询父订单
        String remark = MapUtils.getString(backOrderParams, "remark");
        String tacheId = MapUtils.getString(backOrderParams, "tacheId");
        String woId = MapUtils.getString(backOrderParams, "woId");
        /**
         * 撤销当前流程，作废工单
         */
        boolean flagCancelOrder = flowActionHandler.cancelOrder(operStaffId, orderId);
        if (flagCancelOrder) {
            //调用撤单成功，将当前工单状态修改成主动驳回
            Map<String, Object> updateMap = new HashMap<String, Object>();
            updateMap.put("woState", "290000006");
            updateMap.put("stateDate", new java.sql.Date(new java.util.Date().getTime()));
            updateMap.put("woID", woId);
            updateMap.put("staffId", operStaffId);
            orderDealDao.updateWoOrderState(updateMap);
            //入日志
            Map<String, Object> logDataMap = new HashMap<String, Object>();
            logDataMap.put("woId", woId);
            logDataMap.put("orderId", orderId);
            logDataMap.put("remark", remark);
            logDataMap.put("tacheId", tacheId);
            logDataMap.put("operType", OrderTrackOperType.OPER_TYPE_5);
            logDataMap.put("action", "退单");
            logDataMap.put("trackMessage", "[退单]");
            tacheDealLogIntf.addTrackLog(logDataMap);//入库操作日志
        }
        //获取页面上需要退单的专业
        if (StringUtil.isNotEmpty(MapUtils.getObject(backOrderParams, "specialty").toString())){
            //List<String> specialtyList = (List<String>) MapUtils.getObject(backOrderParams, "specialty"); //需要退单资源分配专业
            List<String> specialtyList = JSONArray.fromObject(MapUtils.getObject(backOrderParams, "specialty")); //需要退单资源分配专业
            /**
             * 退单资源分配专业子流程
             */
            Map<String, Object> qrySourceDispatchMap = new HashMap<String, Object>();
            qrySourceDispatchMap.put("orderId", MapUtils.getString(parentOrder, "PARENTORDERID"));
            for(String specialty :  specialtyList){
                qrySourceDispatchMap.put("specialtyCode", specialty);
                Map<String, Object> sourceDispatchFinishWoOrder = orderQrySecondaryDao.qrySourceDispatchFinishWoOrder(qrySourceDispatchMap);
                Map<String, Object> backOrderspecialtyParams = new HashMap<>();
                backOrderspecialtyParams.put("orderId", MapUtils.getString(sourceDispatchFinishWoOrder, "ORDER_ID"));
                backOrderspecialtyParams.put("woId", MapUtils.getString(sourceDispatchFinishWoOrder, "WO_ID"));
                backOrderspecialtyParams.put("operStaffId", "-2000");//operStaffId 退单数据制作，提交人用-2000
                backOrderspecialtyParams.put("action", "退单");
                backOrderspecialtyParams.put("operType", OrderTrackOperType.OPER_TYPE_5);
                backOrderspecialtyParams.put("remark", remark);
                backOrderspecialtyParams.put("tacheId", MapUtils.getString(sourceDispatchFinishWoOrder, "ID"));
                Map<String, Object> operAttrsVal = new HashMap<String, Object>();
                operAttrsVal.put("DATA_PRODUCTION_REVIEW", "1"); //线条回退
                backOrderspecialtyParams.put("operAttrsVal", operAttrsVal);
                rollBackWoResult = commonMethodDealWoOrderServiceInf.commonComplateWo(backOrderspecialtyParams);
            }
        }
        /**
         * 退单主流程
         */
        String toTaches = MapUtils.getString(backOrderParams, "toTaches"); //主流程要退单到的环节
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("parentOrderId", MapUtils.getString(parentOrder, "PARENTORDERID"));
        paramsMap.put("woState", OrderTrackOperType.WO_ORDER_STATE_10);
        paramsMap.put("tacheCodePar", EnmuValueUtil.TO_DATA_CREATE_AND_SCHEDULE);
        paramsMap.put("tacheCodeParSec", EnmuValueUtil.TO_DATA_CREATE_AND_SCHEDULE_2);
        Map woOrder = orderDealDao.getParentWoOrder(paramsMap);
        if(MapUtils.isNotEmpty(woOrder) && !"".equals(MapUtils.getString(woOrder,"WOID"))){
            String woIdPar = MapUtils.getString(woOrder,"WOID");
            //修改待数据制作与本地调度环节的工单状态为处理中
            orderDealDao.updateWoStateByWoId(woIdPar, OrderTrackOperType.WO_ORDER_STATE_2);
            List<FlowRollBackReasonDTO> flowRollBackReasonDTOs = flowActionHandler.queryRollBackReasons(woIdPar);
            FlowRollBackReasonDTO flowRollBackReasonDTO = null;
            for (int i = 0; i < flowRollBackReasonDTOs.size(); i++) {
                FlowRollBackReasonDTO _flowRollBackReasonDTO = flowRollBackReasonDTOs.get(i);
                FlowTacheDTO flowTacheDTO = _flowRollBackReasonDTO.getToTache();
                String _toTacheId = flowTacheDTO.getId();
                if (toTaches.indexOf(_toTacheId) != -1) {
                    flowRollBackReasonDTO = _flowRollBackReasonDTO;
                }
            }
            backOrderParams.put("woId", woIdPar);
            backOrderParams.put("flowRollBackReasonDTO", flowRollBackReasonDTO);
            backOrderParams.put("operStaffId", "-2000");
            rollBackWoResult = commonMethodDealWoOrderServiceInf.commonRollBackWo(backOrderParams);
        }else {
            rollBackWoResult.put("success", true);
            rollBackWoResult.put("message", "回退成功!");
        }
        return rollBackWoResult;
    }

    @Override
    public Map<String, Object> qryIfHasSourceDispatch(Map<String, Object> qryParams) {
        Map<String, Object> sourceDispatchResult = new HashMap<String, Object>();
        String orderId = MapUtils.getString(qryParams, "orderId");
        //查询主流程是否有未作废的资源分配
        List<Map<String, Object>> sourceDispatch = orderQrySecondaryDao.qrySourceDispatch(orderId);
        if (!ListUtil.isEmpty(sourceDispatch)){
            String parentOrderId = MapUtils.getString(sourceDispatch.get(0), "PARENT_ORDER_ID");
            List<Map<String, Object>> sourceDispatchSpecialty = orderQrySecondaryDao.qrySourceDispatchSpecialty(parentOrderId);
            if (!ListUtil.isEmpty(sourceDispatchSpecialty)){
                sourceDispatchResult.put("ifHas", true);
                sourceDispatchResult.put("data", sourceDispatchSpecialty);
            }else {
                //如果资源分配专业没有，则直接退单到二干调度
                sourceDispatchResult.put("ifHas", false);
            }
        }else {
            sourceDispatchResult.put("ifHas", false);
        }
        return sourceDispatchResult;
    }
}
