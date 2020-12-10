package com.zres.project.localnet.portal.flowdealinfo.service;

import java.util.*;

import org.apache.axis.utils.StringUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.logInfo.service.TacheDealLogIntf;
import com.zres.project.localnet.portal.resourceInitiate.data.dao.ResourceInitiateDao;
import com.zres.project.localnet.portal.util.OrderTrackOperType;

import com.ztesoft.res.frame.flow.common.dto.FlowOrderDTO;
import com.ztesoft.res.frame.flow.common.dto.FlowOrderSpecDTO;
import com.ztesoft.res.frame.flow.common.dto.FlowRollBackReasonDTO;
import com.ztesoft.res.frame.flow.common.dto.FlowWoDTO;
import com.ztesoft.res.frame.flow.common.exception.FlowException;
import com.ztesoft.res.frame.flow.common.intf.FlowAction;
import com.ztesoft.res.frame.flow.common.lock.impl.DatabaseLock;
import com.ztesoft.res.frame.flow.common.lock.intf.DistributeLock;
import com.ztesoft.res.frame.flow.spec.order.dao.FlowOrderSpecDAO;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;

import com.alibaba.fastjson.JSON;

@Service
public class CommonMethodDealWoOrderService implements CommonMethodDealWoOrderServiceInf {

    Logger logger = LoggerFactory.getLogger(CommonMethodDealWoOrderService.class);
    @Autowired
    private FlowAction flowAction;
    @Autowired
    private TacheDealLogIntf tacheDealLogIntf;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private FlowOrderSpecDAO flowOrderSpecDAO;
    @Autowired
    private ResourceInitiateDao resourceInitiateDao;

    @Override
    public Map<String, Object> commonCreateOrder(Map<String, Object> paramsMap) throws Exception {
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

        //定单属性--启动子流程
        if (paramsMap.containsKey("attr")){
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
        flowOrderDTO.setAreaId(MapUtils.getString(paramsMap, "AREA"));
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
        FlowOrderDTO retDTO = flowAction.createOrder(operStaffId, flowOrderDTO);
        logger.info(">>>>>>>>>>>>>>>>>流程启动成功>>>>>>>>>>>>>>>>>>>>>>>");
        Map<String,Object> retMap = new HashMap(2);
        retMap.put("orderId", retDTO.getOrderId());
        retMap.put("orderCode", retDTO.getOrderCode());
        return retMap;
    }

    @Override
    public Map<String, Object> commonCreateChildOrder(Map<String, Object> paramsMap) throws Exception {
        logger.info(">>>>>>>>>>>>>>>>>启子流程>>>>>>>>>>>>>>>>>>>>>>>");
        Map<String, Object> childflowData = new HashMap<>();
        String woId = MapUtils.getString(paramsMap, "woId");
        String orderId = MapUtils.getString(paramsMap, "orderId");

        Map<String, Object> orderDataMap = orderDealDao.qryOrderData(woId);
        String orderTitle = MapUtils.getString(orderDataMap, "ORDERTITLE");
        String requFineTime = MapUtils.getString(orderDataMap, "REQ_FIN_DATE");//父定单的要求完成时间
        Map<String, Object> operActTypeMap = orderDealDao.getOperActType(orderId);
        //String regionId = MapUtils.getString(operActTypeMap, "REGION_ID");

        Map<String, Object> startChildMap = new HashMap<String, Object>();
        List<String> configList = new ArrayList<String>(); //存放所启区域和专业
        List<String> orderIdList = new ArrayList<String>(); //存放所启子流程的orderid

        //String startFlag = MapUtils.getString(paramsMap, "startFlag");
        String ordPsId = MapUtils.getString(paramsMap, "ordPsId");
        String parentOrderCode = MapUtils.getString(paramsMap, "parentOrderCode");
        Map<String, String> childConfigs = JSON.parseObject(MapUtils.getString(paramsMap, "childConfigs"), Map.class);
        Iterator<Map.Entry<String, String>> childConfigIter = childConfigs.entrySet().iterator();
        while (childConfigIter.hasNext()) {
            Map.Entry<String, String> e = childConfigIter.next();
            String childConfig = e.getKey();
            String childConfigAreas = String.valueOf(e.getValue());
            childConfigAreas = StringUtils.strip(childConfigAreas, "[]");
            List<String> childConfigAreaList = Arrays.asList(childConfigAreas.split(","));
            for (int i = 0; i < childConfigAreaList.size(); i++) {
                String childConfigArea = childConfigAreaList.get(i);
                //查询该专业的子流程订单是否是处理中，处理中不需要再启流程
                String childOrderState = resourceInitiateDao.qryChildOrderState(orderId, childConfig, childConfigAreas, BasicCode.RESSUP_LOCAL);
                if (!StringUtils.isEmpty(childOrderState) && "200000002".equals(childOrderState)){
                    continue;
                }
                String childConfigName = "";
                Map<String, String> attr = new HashMap<>();
                attr.put("REGION_ID", childConfigArea); //区域
                attr.put("SPECIALTY_CODE", childConfig); //专业
                childConfigName = resourceInitiateDao.qrySpecialtyName(childConfig);
                attr.put("PRODUCT_TYPE", MapUtils.getString(operActTypeMap, "PRODUCT_TYPE")); //产品编码
                startChildMap.put("attr", attr);
                startChildMap.put("ordPsid", ordPsId);
                startChildMap.put("parentOrderId", orderId);
                startChildMap.put("parentOrderCode", parentOrderCode);
                startChildMap.put("ORDER_TITLE", orderTitle + "_" + childConfigName + "子流程");
                startChildMap.put("AREA", "350002000000000042766408");
                startChildMap.put("ORDER_CONTENT", "子流程");
                startChildMap.put("requFineTime", requFineTime);
                childflowData = this.commonCreateOrder(startChildMap);
                orderIdList.add(MapUtils.getString(childflowData, "orderId"));
            }
            configList.add(childConfig);
        }
        childflowData.put("configList", configList.toString());
        childflowData.put("orderIdList", orderIdList);
        return childflowData;
    }

    /**
     * 该方法是抽出来一个通用的回单方法，包含记录日志
     * 最好不要再里面添加任何业务代码
     * @param commonMap
     * @return
     */
    @Override
    public Map<String, Object> commonComplateWo(Map<String, Object> commonMap) throws Exception {
        logger.info(">>>>>>>>>>>>>>>>>进入通用提交工单方法>>>>>>>>>>>>>>>>>>>>>>>");
        Map<String, Object> resMap = new HashMap<String, Object>();
        String orderId = MapUtils.getString(commonMap, "orderId");
        String woId = MapUtils.getString(commonMap, "woId");
        String operStaffId = MapUtils.getString(commonMap, "operStaffId");
        String action = MapUtils.getString(commonMap, "action"); //动作  回单
        String operType = MapUtils.getString(commonMap, "operType"); //操作类型  OrderTrackOperType.OPER_TYPE_4
        Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(operStaffId));
        // 线条判断参数 operAttrs 获取页面传过来的线条参数
        Map<String, String> operAttrsValMap = null;
        if (commonMap.containsKey("operAttrsVal")){
            operAttrsValMap = MapUtils.getMap(commonMap, "operAttrsVal");
        }
        List<HashMap<String, String>> operAttrsList = new ArrayList<HashMap<String, String>>();
        if (MapUtils.isNotEmpty(operAttrsValMap)){
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
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>加锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            Map<String, Object> logDataMap = new HashMap<String, Object>();
            logDataMap.put("woId", woId);
            logDataMap.put("orderId", orderId);
            logDataMap.put("remark", MapUtils.getString(commonMap, "remark"));
            logDataMap.put("tacheId", MapUtils.getString(commonMap, "tacheId"));
            logDataMap.put("operStaffInfoMap", operStaffInfoMap);
            flowAction.complateWo(operStaffId, woDTO);
            logDataMap.put("operType", operType);
            logDataMap.put("action", action);
            logDataMap.put("trackMessage", "[" + action + "]");
            tacheDealLogIntf.addTrackLog(logDataMap);//入库操作日志
        } catch (FlowException fe){
            fe.printStackTrace();
            logger.error("派单失败：", fe);
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>解锁失败捕获异常>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            throw fe;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("派单失败：", e);
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>解锁失败捕获异常>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            throw e;
        }finally {
            if(lock!=null){
                lock.unlock();
            }
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>finally主动解锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        }
        resMap.put("success", true);
        resMap.put("message", "派单成功!");
        return resMap;
    }

    @Override
    public Map<String, Object> commonRollBackWo(Map<String, Object> commonMap) {
        logger.info(">>>>>>>>>>>>>>>>>进入通用异常节点退单方法>>>>>>>>>>>>>>>>>>>>>>>");
        Map<String, Object> resMap = new HashMap<String, Object>();
        //String orderId = MapUtils.getString(commonMap, "orderId");
        String woId = MapUtils.getString(commonMap, "woId");
        String operStaffId = MapUtils.getString(commonMap, "operStaffId");
        FlowRollBackReasonDTO flowRollBackReasonDTO =
                JSON.parseObject(JSON.parseObject(commonMap.toString()).getString("flowRollBackReasonDTO"),
                        FlowRollBackReasonDTO.class);
                //(FlowRollBackReasonDTO) MapUtils.getObject(commonMap, "flowRollBackReasonDTO"); //退单原因
        String remark = MapUtils.getString(commonMap, "remark");
        //Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(operStaffId));
        DistributeLock lock = new DatabaseLock(woId);
        try{
            lock.lock();
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>加锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            Map<String, Object> woOrderState = orderDealDao.qryWoOrderState(woId);
            if (OrderTrackOperType.WO_ORDER_STATE_2.equals(MapUtils.getString(woOrderState, "WO_STATE"))) {
                flowAction.rollBackWo(operStaffId, woId, flowRollBackReasonDTO, remark);
            }
            /*Map<String, Object> logDataMap = new HashMap<String, Object>();
            logDataMap.put("woId", woId);
            logDataMap.put("orderId", orderId);
            logDataMap.put("remark", remark);
            logDataMap.put("tacheId", MapUtils.getString(commonMap, "tacheId"));
            logDataMap.put("operStaffInfoMap", operStaffInfoMap);
            logDataMap.put("operType", OrderTrackOperType.OPER_TYPE_5);
            logDataMap.put("action", "退单");
            logDataMap.put("trackMessage", "[退单]");
            tacheDealLogIntf.addTrackLog(logDataMap);//入库操作日志*/
            resMap.put("success", true);
            resMap.put("message", "回退成功!");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("回退失败：", e);
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>解锁失败捕获异常>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            resMap.put("success", false);
            resMap.put("message", "回退失败!" + e);
        } finally {
            if(lock!=null){
                lock.unlock();
            }
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>finally主动解锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        }
        return resMap;
    }

}
