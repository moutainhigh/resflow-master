package com.zres.project.localnet.portal.common.service;

import java.util.*;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.common.CommonWoOrderDealServiceIntf;
import com.zres.project.localnet.portal.common.dao.CommonDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.util.OrderTrackOperType;

import com.ztesoft.res.frame.flow.common.dto.FlowOrderDTO;
import com.ztesoft.res.frame.flow.common.dto.FlowOrderSpecDTO;
import com.ztesoft.res.frame.flow.common.dto.FlowRollBackReasonDTO;
import com.ztesoft.res.frame.flow.common.dto.FlowWoDTO;
import com.ztesoft.res.frame.flow.common.exception.FlowException;
import com.ztesoft.res.frame.flow.common.intf.FlowAction;
import com.ztesoft.res.frame.flow.common.lock.impl.DatabaseLock;
import com.ztesoft.res.frame.flow.common.lock.intf.DistributeLock;
import com.ztesoft.res.frame.flow.common.util.StringUtils;
import com.ztesoft.res.frame.flow.spec.order.dao.FlowOrderSpecDAO;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;

@Service
public class CommonWoOrderDealService implements CommonWoOrderDealServiceIntf {

    Logger logger = LoggerFactory.getLogger(CommonWoOrderDealService.class);
    @Autowired
    private FlowAction flowAction;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private FlowOrderSpecDAO flowOrderSpecDAO;
    @Autowired
    private CommonDealDao commonDealDao;

    @Override
    public Map<String, Object> createOrderService(Map<String, Object> paramsMap) throws FlowException {
        logger.info(">>>>>>>>>>>>>>>>>启流程>>>>>>>>>>>>>>>>>>>>>>>");
        FlowOrderDTO flowOrderDTO = new FlowOrderDTO();
        FlowOrderSpecDTO specDTO = new FlowOrderSpecDTO();
        int ordPsid = MapUtils.getIntValue(paramsMap, "ordPsid");
        Map<String, Object> orderInfoMap = orderDealDao.findOrderCode(ordPsid);
        String orderType = MapUtils.getString(orderInfoMap, "ORDER_TYPE");
        String orderObjType = MapUtils.getString(orderInfoMap, "OBJ_TYPE");
        String orderActType = MapUtils.getString(orderInfoMap, "ACT_TYPE");
        specDTO.setOrderType(orderType);
        specDTO.setObjType(orderObjType);
        specDTO.setActType(orderActType);
        flowOrderDTO.setOrderSpec(specDTO);
        flowOrderDTO.setParentOrderId(MapUtils.getString(paramsMap, "parentOrderId"));
        flowOrderDTO.setParentOrderCode(MapUtils.getString(paramsMap, "parentOrderCode"));
        //封装第一个环节的处理人
        if (paramsMap.containsKey("dispType") && paramsMap.containsKey("staffId")) {
            String dispType = MapUtils.getString(paramsMap, "dispType");
            String staffId = MapUtils.getString(paramsMap, "staffId");
            flowOrderDTO.setNextDispType(dispType);
            flowOrderDTO.setNextDispId(staffId);
        }
        //定单属性--用于启动子流程
        if (paramsMap.containsKey("attr")) {
            Map attrMap = MapUtils.getMap(paramsMap, "attr"); // 存了区域和专业
            List<HashMap<String, String>> orderAttrs = new ArrayList<HashMap<String, String>>();
            Iterator<Map.Entry<String, String>> iter = attrMap.entrySet().iterator();
            while (iter.hasNext()) {
                HashMap<String, String> operAttrsMap = new HashMap<String, String>();
                Map.Entry<String, String> e = iter.next();
                String key = e.getKey();
                String value = String.valueOf(e.getValue());
                operAttrsMap.put("KEY", key);
                operAttrsMap.put("VALUE", value);
                orderAttrs.add(operAttrsMap);
            }
            flowOrderDTO.setOrderAttrs(orderAttrs);
        }
        flowOrderDTO.setOrderTitle(MapUtils.getString(paramsMap, "ORDER_TITLE"));
        //flowOrderDTO.setAreaId(MapUtils.getString(paramsMap, "AREA"));
        flowOrderDTO.setAreaId("350002000000000042766408");
        flowOrderDTO.setRemark(MapUtils.getString(paramsMap, "ORDER_CONTENT"));
        //flowOrderDTO.setReqFinDate(MapUtils.getString(paramsMap, "requFineTime"));
        String orderId = flowOrderSpecDAO.getSeq("GOM_ORDER");
        flowOrderDTO.setOrderId(orderId);
        String operStaffId = "";
        if (ThreadLocalInfoHolder.getLoginUser() == null) {
            operStaffId = "11";
        }
        else {
            //获取用户id
            operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        }
        Map<String, Object> retMap = new HashMap(2);
        try {
            FlowOrderDTO retDTO = flowAction.createOrder(operStaffId, flowOrderDTO);
            logger.info(">>>>>>>>>>>>>>>>>流程启动成功>>>>>>>>>>>>>>>>>>>>>>>");
            retMap.put("orderId", retDTO.getOrderId());
            retMap.put("orderCode", retDTO.getOrderCode());
        }
        catch (FlowException fe) {
            throw fe;
        }
        return retMap;
    }

    @Override
    public Map<String, Object> createChildOrderService(Map<String, Object> paramsMap) throws FlowException {
        logger.info(">>>>>>>>>>>>>>>>>启子流程>>>>>>>>>>>>>>>>>>>>>>>");
        Map<String, Object> childflowData = new HashMap<>();
        String woId = MapUtils.getString(paramsMap, "woId");
        String orderId = MapUtils.getString(paramsMap, "orderId");
        Map<String, Object> orderDataMap = orderDealDao.qryOrderData(woId);
        String orderTitle = MapUtils.getString(orderDataMap, "ORDERTITLE");
        //String requFineTime = MapUtils.getString(orderDataMap, "REQ_FIN_DATE");//父定单的要求完成时间
        Map<String, Object> operActTypeMap = orderDealDao.getOperActType(orderId);
        String ordPsId = MapUtils.getString(paramsMap, "ordPsId");
        String parentOrderCode = MapUtils.getString(paramsMap, "parentOrderCode");
        String childConfigs = MapUtils.getString(paramsMap, "childConfigs");
        String[] childConfigAry = childConfigs.split(",");
        Map<String, Object> startChildMap = new HashMap<String, Object>();
        for (int i = 0; i < childConfigAry.length; i++) {
            String childConfig = childConfigAry[i];
            //查询该子流程定单是否是处理中，处理中不需要再启流程
            String childOrderState = commonDealDao.qryChildOrderState(orderId, childConfig, parentOrderCode);
            if (!StringUtils.isEmpty(childOrderState) && "200000002".equals(childOrderState)) {
                continue;
            }
            String childConfigName = "";
            childConfigName = commonDealDao.qryAreaName(childConfig);
            Map<String, String> attr = new HashMap<>();
            attr.put("REGION_ID", childConfig); //区域
            attr.put("PRODUCT_TYPE", MapUtils.getString(operActTypeMap, "PRODUCT_TYPE")); //产品编码
            startChildMap.put("attr", attr);
            startChildMap.put("ordPsid", ordPsId);
            startChildMap.put("parentOrderId", orderId);
            startChildMap.put("parentOrderCode", parentOrderCode);
            startChildMap.put("ORDER_TITLE", orderTitle + "_" + childConfigName + "子流程");
            startChildMap.put("ORDER_CONTENT", "子流程");
            //startChildMap.put("requFineTime", requFineTime);
            childflowData = this.createOrderService(startChildMap);
        }
        return childflowData;
    }

    @Override
    public Map<String, Object> complateWoService(Map<String, Object> commonMap) throws FlowException {
        logger.info(">>>>>>>>>>>>>>>>>进入通用提交工单方法>>>>>>>>>>>>>>>>>>>>>>>");
        Map<String, Object> resMap = new HashMap<String, Object>();
        String woId = MapUtils.getString(commonMap, "woId");
        String operStaffId = MapUtils.getString(commonMap, "operStaffId");
        // 线条判断参数 operAttrs 获取页面传过来的线条参数
        Map<String, String> operAttrsValMap = null;
        if (commonMap.containsKey("operAttrsVal")) {
            operAttrsValMap = MapUtils.getMap(commonMap, "operAttrsVal");
        }
        List<HashMap<String, String>> operAttrsList = new ArrayList<HashMap<String, String>>();
        if (MapUtils.isNotEmpty(operAttrsValMap)) {
            Iterator<Map.Entry<String, String>> iter = operAttrsValMap.entrySet().iterator();
            while (iter.hasNext()) {
                HashMap<String, String> operAttrsMap = new HashMap<String, String>();
                Map.Entry<String, String> e = iter.next();
                String key = e.getKey();
                String value = String.valueOf(e.getValue());
                operAttrsMap.put("KEY", key);
                operAttrsMap.put("VALUE", value);
                operAttrsList.add(operAttrsMap);
            }
        }
        FlowWoDTO woDTO = new FlowWoDTO();
        woDTO.setWoId(woId);
        woDTO.setOperAttrs(operAttrsList);
        DistributeLock lock = new DatabaseLock(woId);
        try {
            lock.lock();
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>加锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            flowAction.complateWo(operStaffId, woDTO);
            resMap.put("success", true);
            resMap.put("message", "派单成功!");
        }
        catch (FlowException fe) {
            throw fe;
        }
        finally {
            if (lock != null) {
                lock.unlock();
            }
            logger.info(">>>>>>>>>>>>>>>>>>>finally主动解锁>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        }

        return resMap;
    }

    @Override
    public Map<String, Object> rollBackWoService(Map<String, Object> commonMap) throws FlowException {
        logger.info(">>>>>>>>>>>>>>>>>进入通用异常节点退单方法>>>>>>>>>>>>>>>>>>>>>>>");
        Map<String, Object> resMap = new HashMap<String, Object>();
        String woId = MapUtils.getString(commonMap, "woId");
        String operStaffId = MapUtils.getString(commonMap, "operStaffId");
        FlowRollBackReasonDTO flowRollBackReasonDTO = (FlowRollBackReasonDTO) MapUtils.getObject(commonMap, "flowRollBackReasonDTO");
        //JSON.parseObject(JSON.parseObject(commonMap.toString()).getString("flowRollBackReasonDTO"), FlowRollBackReasonDTO.class);
        String remark = MapUtils.getString(commonMap, "remark");
        DistributeLock lock = new DatabaseLock(woId);
        try {
            lock.lock();
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>加锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            Map<String, Object> woOrderState = orderDealDao.qryWoOrderState(woId);
            if (OrderTrackOperType.WO_ORDER_STATE_2.equals(MapUtils.getString(woOrderState, "WO_STATE"))) {
                flowAction.rollBackWo(operStaffId, woId, flowRollBackReasonDTO, remark);
            }
            resMap.put("success", true);
            resMap.put("message", "回退成功!");
        }
        catch (FlowException fe) {
            throw fe;
        }
        finally {
            if (lock != null) {
                lock.unlock();
            }
            logger.info(">>>>>>>>>>>>>>>>>>>>finally主动解锁>>>>>>>>>>>>>>>>>>>>");
        }
        return resMap;
    }

    @Override
    public Map<String, Object> cancelOrderService(Map<String, Object> commonMap) throws FlowException {
        logger.info(">>>>>>>>>>>>>>>>>进入通用作废方法>>>>>>>>>>>>>>>>>>>>>>>");
        Map<String, Object> resMap = new HashMap<String, Object>();
        String orderId = MapUtils.getString(commonMap, "orderId");
        String operStaffId = MapUtils.getString(commonMap, "operStaffId");
        DistributeLock lock = new DatabaseLock(orderId);
        try {
            lock.lock();
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>加锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            flowAction.cancelOrder(operStaffId, orderId);
            resMap.put("success", true);
            resMap.put("message", "作废成功!");
        }
        catch (FlowException fe) {
            throw fe;
        }
        finally {
            if (lock != null) {
                lock.unlock();
            }
            logger.info(">>>>>>>>>>>>>>>>>>>>finally主动解锁>>>>>>>>>>>>>>>>>>>>");
        }
        return resMap;
    }

    @Override
    public Map<String, Object> createSerialChildOrderService(Map<String, Object> paramsMap) throws FlowException {
        logger.info("启串行子流程入参:" + paramsMap.toString());
        Map<String, Object> returnMap = new HashMap<>(2);
        String woId = MapUtils.getString(paramsMap, "woId");
        String orderId = MapUtils.getString(paramsMap, "orderId");
        Map<String, Object> orderDataMap = orderDealDao.qryOrderData(woId);
        String ordPsId = MapUtils.getString(paramsMap, "ordPsId");
        String orderTitle = MapUtils.getString(orderDataMap, "ORDERTITLE");
        String parentOrderCode = MapUtils.getString(paramsMap, "parentOrderCode");
        String childConfig = MapUtils.getString(paramsMap, "childConfig");
        Map<String, Object> startChildMap = new HashMap<>(10);
        String staffId = MapUtils.getString(paramsMap, "staffId");
        String dispType = MapUtils.getString(paramsMap, "dispType");
        //查询该子流程定单是否是处理中，处理中不需要再启流程
        String childOrderState = commonDealDao.qryChildOrderState(orderId, null, parentOrderCode);
        if (!StringUtils.isEmpty(childOrderState) && "200000002".equals(childOrderState)) {
            return null;
        }
        // 封装第一个环节的处理人
        if (StringUtils.isNotEmpty(staffId) && StringUtils.isNotEmpty(dispType)) {
            startChildMap.put("staffId", staffId);
            startChildMap.put("dispType", dispType);
        }
        //上一环节的订单id
        Map<String, String> attr = new HashMap<>(2);
        attr.put("PARENT_ORDER_ID", orderId);
        startChildMap.put("attr", attr);
        startChildMap.put("ordPsid", ordPsId);
        startChildMap.put("ORDER_CONTENT", "子流程");
        startChildMap.put("parentOrderCode", parentOrderCode);
        startChildMap.put("ORDER_TITLE", childConfig + "子流程");
        startChildMap.put("parentOrderId", orderId);
        this.createOrderService(startChildMap);
        returnMap.put("success", true);
        returnMap.put("message", "提交成功");
        logger.info("启动子流程出参:" + returnMap.toString());
        return returnMap;
    }

}
