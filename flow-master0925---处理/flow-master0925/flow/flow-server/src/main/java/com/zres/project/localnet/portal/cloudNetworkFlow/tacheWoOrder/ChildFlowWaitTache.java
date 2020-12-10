package com.zres.project.localnet.portal.cloudNetworkFlow.tacheWoOrder;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.cloudNetworkFlow.dao.TacheWoOrderDao;
import com.zres.project.localnet.portal.common.CommonWoOrderDealServiceIntf;
import com.zres.project.localnet.portal.common.ToOrderDealTacheWoOrderIntf;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.util.OrderTrackOperType;

/**
 * @Classname ChildFlowWaitTache
 * @Description 子流程等待环节处理
 * @Author guanzhao
 * @Date 2020/11/4 17:41
 */
@Service
public class ChildFlowWaitTache implements ToOrderDealTacheWoOrderIntf {

    Logger logger = LoggerFactory.getLogger(ChildFlowWaitTache.class);

    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private TacheWoOrderDao tacheWoOrderDao;
    @Autowired
    private CommonWoOrderDealServiceIntf commonWoOrderDealServiceIntf;

    /*
     * 子流程等待环节到单逻辑处理：
     * 1，查询父定单，网络施工环节所启子流程是否都到子流程等待环节； --这里可能区分az端，也可能不区分
     * 2，如果到了，回单父定单网络施工环节，回单之前需要修改网络施工环节的工单状态；
     *    如果没到，不做操作；
     *
     * @author guanzhao
     * @date 2020/11/4
     *
     */
    @Override
    public Map<String, Object> toOrderTacheDoSomething(Map<String, Object> toOrderTacheDoSomeMap) throws Exception {
        logger.info("-----------到单处理----------子流程等待环节-----------------------");
        String orderId = MapUtils.getString(toOrderTacheDoSomeMap, "orderId");
        String tacheCode = MapUtils.getString(toOrderTacheDoSomeMap, "tacheCode");
        Map parentOrder = orderDealDao.getParentOrder(orderId);
        String parentOrderId = MapUtils.getString(parentOrder, "PARENTORDERID");
        String parentOrderCode = MapUtils.getString(parentOrder, "PARENTORDERCODE");
        Map<String, String> qryMap = new HashMap<>();
        qryMap.put("parentOrderId", parentOrderId);
        qryMap.put("parentOrderCode", parentOrderCode);
        qryMap.put("tacheCode", tacheCode);
        int childLastNum = tacheWoOrderDao.qryChildFlowNumAtLastNew(qryMap);
        int childNum = tacheWoOrderDao.qryChildFlowNumNew(qryMap);
        if (childLastNum == childNum) {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("orderId", parentOrderId);
            paramMap.put("woState", OrderTrackOperType.WO_ORDER_STATE_10);
            paramMap.put("tacheCode", parentOrderCode);
            Map woOrder = orderDealDao.getParentWoOrder(paramMap);
            if (MapUtils.isNotEmpty(woOrder)) {
                String parentWoId = MapUtils.getString(woOrder, "WOID");
                orderDealDao.updateWoStateByWoId(parentWoId, OrderTrackOperType.WO_ORDER_STATE_2);
                Map<String, Object> complateMap = new HashMap<>();
                complateMap.put("operStaffId", "-2000");
                complateMap.put("woId", parentWoId);
                commonWoOrderDealServiceIntf.complateWoService(complateMap);
            }
        }
        return null;
    }
}
