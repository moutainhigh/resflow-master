package com.zres.project.localnet.portal.cloudNetworkFlow.tacheWoOrder;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.common.CommonWoOrderDealServiceIntf;
import com.zres.project.localnet.portal.common.ToOrderDealTacheWoOrderIntf;
import com.zres.project.localnet.portal.common.util.FlowTacheUtil;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.util.OrderTrackOperType;

/**
 * @Classname McpeConfigFinishTache
 * @Description 完成MCPE业务配置处理--移机子流程
 * @Author guanzhao
 * @Date 2020/11/11 09:39
 */
@Service
public class McpeConfigFinishTache implements ToOrderDealTacheWoOrderIntf {

    Logger logger = LoggerFactory.getLogger(McpeConfigFinishTache.class);

    @Autowired
    private CommonWoOrderDealServiceIntf commonWoOrderDealServiceIntf;
    @Autowired
    private OrderDealDao orderDealDao;

    /*
     * 完成MCPE业务配置处理环节到单处理逻辑--移机子流程
     * 1，查询父流程的完成MCPE业务配置处理环节，YZW_END_MCPE_CONFIG
     *      如果能查询到则回单该环节；
     *      如果查询不到则不处理；
     * @author guanzhao
     * @date 2020/11/11
     *
     */
    @Override
    public Map<String, Object> toOrderTacheDoSomething(Map<String, Object> toOrderTacheDoSomeMap) throws Exception {
        logger.info("-----------到单处理--------移机子流程--完成MCPE业务配置处理环节-----------------------");
        String orderId = MapUtils.getString(toOrderTacheDoSomeMap, "orderId");
        Map parentOrder = orderDealDao.getParentOrder(orderId);
        String parentOrderId = MapUtils.getString(parentOrder, "PARENTORDERID");
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("orderId", parentOrderId);
        paramMap.put("woState", OrderTrackOperType.WO_ORDER_STATE_2);
        paramMap.put("tacheCode", FlowTacheUtil.YZW_END_MCPE_CONFIG);
        Map woOrder = orderDealDao.getParentWoOrder(paramMap);
        if (MapUtils.isNotEmpty(woOrder)) {
            String parentWoId = MapUtils.getString(woOrder, "WOID");
            Map<String, Object> complateMap = new HashMap<>();
            complateMap.put("operStaffId", "-2000");
            complateMap.put("woId", parentWoId);
            commonWoOrderDealServiceIntf.complateWoService(complateMap);
        }
        return null;
    }
}
