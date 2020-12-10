package com.zres.project.localnet.portal.listener.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.CheckOrderDao;
import com.zres.project.localnet.portal.resourceInitiate.data.dao.ResourceInitiateDao;
import com.zres.project.localnet.portal.resourceInitiate.service.ResSupplementDealServiceIntf;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.listener.util.EnmuValueUtil;
import com.zres.project.localnet.portal.util.OrderTrackOperType;

import com.ztesoft.res.frame.core.util.ListUtil;
import com.ztesoft.res.frame.flow.common.dto.FlowWoDTO;
import com.ztesoft.res.frame.flow.common.event.OrderFinishedEvent;
import com.ztesoft.res.frame.flow.common.intf.FlowAction;

@Component
public class OrderFinishedEventListener implements ApplicationListener<OrderFinishedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(OrderFinishedEventListener.class);

    @Autowired
    private FlowAction flowAction;

    @Autowired
    private OrderDealDao orderDealDao;

    @Autowired
    private CheckOrderDao checkOrderDao;

    @Autowired
    private ResourceInitiateDao resourceInitiateDao;

    @Override
    public void onApplicationEvent(OrderFinishedEvent orderFinishedEvent) {

        //监听二干调度流程结束，回单所有子流程
        if (EnmuValueUtil.SECONDARY_TRUNK_DISPATCH.equals(orderFinishedEvent.getOrderType())){
            // 监听主订单结束，回单所有子流程
            String orderId = orderFinishedEvent.getOrderId();
            Map<String, Object> paramsMap = new HashMap<String, Object>();
            paramsMap.put("orderId", orderId);
            //开通电路
            paramsMap.put("woState", OrderTrackOperType.WO_ORDER_STATE_2);
            List<Map<String, Object>> childFlowDataList = orderDealDao.getAllChildFlowData(paramsMap);
            for (int i = 0; i < childFlowDataList.size(); i++) {
                FlowWoDTO woChildDTO = new FlowWoDTO();
                woChildDTO.setWoId(MapUtils.getString(childFlowDataList.get(i), "WOID"));
                List<HashMap<String, String>> operAttrsList = new ArrayList<HashMap<String, String>>();
                //二干资源分配完成
                HashMap<String, String> operAttrsAllocationMap = new HashMap<String, String>();
                operAttrsAllocationMap.put("KEY", "DATA_PRODUCTION_REVIEW");
                operAttrsAllocationMap.put("VALUE", "0");
                operAttrsList.add(operAttrsAllocationMap);
                //二干专业数据制作完成
                HashMap<String, String> operAttrsDataMakeMap = new HashMap<String, String>();
                operAttrsDataMakeMap.put("KEY", "SPECIALTY_DATA_PRODUCTION_CHECK");
                operAttrsDataMakeMap.put("VALUE", "0");
                operAttrsList.add(operAttrsDataMakeMap);

                //二干专业资源施工完成
                HashMap<String, String> operAttrsResMakeMap = new HashMap<String, String>();
                operAttrsResMakeMap.put("KEY", "SPECIALTY_RES_CONSTRUCTION_CHECK");
                operAttrsResMakeMap.put("VALUE", "0");
                operAttrsList.add(operAttrsResMakeMap);

                woChildDTO.setOperAttrs(operAttrsList);
                flowAction.complateWo("-2000", woChildDTO);
            }

            //资源补录流程
            if (EnmuValueUtil.SECONDARY_RESOURCE_SUPPLEMENT_FLOW.equals(orderFinishedEvent.getOrderActType())){
                resourceInitiateDao.updateResSupOrderState(orderFinishedEvent.getOrderId());
            }

            //核查流程
            if(EnmuValueUtil.SECONDARY_SRC_REVIEW.equals(orderFinishedEvent.getOrderObjType())){
                //二干核查流程结束，如果有下发本地将本地的流程也结束
                List<Map<String, Object>> allLocalCheckWaitTacheWoOrderList = checkOrderDao.qryAllLocalCheckWaitTacheWoOrder(orderFinishedEvent.getOrderId());
                if (!ListUtil.isEmpty(allLocalCheckWaitTacheWoOrderList)){
                    for(Map<String, Object> allLocalCheckWaitTacheWoOrder : allLocalCheckWaitTacheWoOrderList){
                        String woId = MapUtils.getString(allLocalCheckWaitTacheWoOrder, "WO_ID");
                        FlowWoDTO woChildDTO = new FlowWoDTO();
                        woChildDTO.setWoId(woId);
                        flowAction.complateWo("-2000", woChildDTO);
                    }
                }
            }
        }
        //监听本地网流程结束，回单所有子流程
        if (EnmuValueUtil.LOCAL_NETWORK_DISPATCH.equals(orderFinishedEvent.getOrderType())){
            String orderId = orderFinishedEvent.getOrderId();
            Map<String, Object> paramsMap = new HashMap<String, Object>();
            paramsMap.put("orderId", orderId);
            paramsMap.put("woState", OrderTrackOperType.WO_ORDER_STATE_2);
            List<Map<String, Object>> childFlowDataList = orderDealDao.getAllChildFlowData(paramsMap);
            for (int i = 0; i < childFlowDataList.size(); i++) {
                FlowWoDTO woChildDTO = new FlowWoDTO();
                woChildDTO.setWoId(MapUtils.getString(childFlowDataList.get(i), "WOID"));
                flowAction.complateWo("-2000", woChildDTO);
            }
        }

    }
}
