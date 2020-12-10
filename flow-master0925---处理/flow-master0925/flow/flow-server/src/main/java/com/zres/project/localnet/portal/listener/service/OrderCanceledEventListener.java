package com.zres.project.localnet.portal.listener.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderQrySecondaryDao;
import com.zres.project.localnet.portal.listener.util.EnmuValueUtil;
import com.zres.project.localnet.portal.util.OrderTrackOperType;

import com.ztesoft.res.frame.flow.common.event.OrderCanceledEvent;

@Component
public class OrderCanceledEventListener implements ApplicationListener<OrderCanceledEvent> {

    private static final Logger logger = LoggerFactory.getLogger(OrderCanceledEventListener.class);

    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private OrderQrySecondaryDao orderQrySecondaryDao;

    @Override
    public void onApplicationEvent(OrderCanceledEvent orderCanceledEvent) {

        //监听本地网正常流程
        if (EnmuValueUtil.LOCAL_NETWORK_DISPATCH.equals(orderCanceledEvent.getOrderType())){
            // 监听子流程是否全部撤单
            if (EnmuValueUtil.LOCAL_CHILDFLOW.equals(orderCanceledEvent.getOrderObjType())) {
                String orderId = orderCanceledEvent.getOrderId();
                Map parentOrder = orderDealDao.getParentOrder(orderId);
                String parentOrderId = MapUtils.getString(parentOrder, "PARENTORDERID");
                /*Map<String, Object> paramMap = new HashMap<String, Object>();
                paramMap.put("orderId", parentOrderId);
                paramMap.put("orderState", OrderTrackOperType.ORDER_STATE_2);
                List<Map<String, Object>> childFlowDataList = orderDealDao.qryChildFlowState(paramMap);
                if (childFlowDataList.isEmpty()) {
                    // 现在是本地测试退单时，直接退回，主流程等在电路调度，监听改状态，等子流程全部撤单，电路调度环节只改一下状态
                    Map<String, Object> parentMap = new HashMap<String, Object>();
                    parentMap.put("orderId", parentOrderId);
                    parentMap.put("woState", OrderTrackOperType.WO_ORDER_STATE_10); // 子流程回退中290000112
                    Map woOrder = orderDealDao.getParentWoOrder(parentMap);
                    String woOrderId = MapUtils.getString(woOrder, "WOID");
                    orderDealDao.updateWoStateByWoId(woOrderId, OrderTrackOperType.WO_ORDER_STATE_2);// 修改电路调度环节状态为执行中
                }*/
                // 子流程只要有一个撤单，电路调度环节改状态，修改电路调度环节工单正反向标识
                Map<String, Object> parentMap = new HashMap<String, Object>();
                parentMap.put("orderId", parentOrderId);
                parentMap.put("woState", OrderTrackOperType.WO_ORDER_STATE_10);
                Map woOrder = orderDealDao.getParentWoOrder(parentMap);
                if (MapUtils.isNotEmpty(woOrder)){
                    String woOrderId = MapUtils.getString(woOrder, "WOID");
                    int updateWoOrderOperNum = orderQrySecondaryDao.updateWoOrderOper(woOrderId);
                    if (updateWoOrderOperNum == 0){
                        Map<String, Object> woOrderParams = new HashMap<>();
                        woOrderParams.put("woId", woOrderId);
                        woOrderParams.put("orderId", parentOrderId);
                        woOrderParams.put("forwardOrReverseFlag", "1");
                        orderQrySecondaryDao.insertWoOrderOper(woOrderParams);
                    }
                    orderDealDao.updateWoStateByWoId(woOrderId, OrderTrackOperType.WO_ORDER_STATE_2);// 修改电路调度环节状态为执行中
                }
            }
        }
    }
}
