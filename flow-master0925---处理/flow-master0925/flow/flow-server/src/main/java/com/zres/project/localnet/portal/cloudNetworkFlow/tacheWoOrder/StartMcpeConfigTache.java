package com.zres.project.localnet.portal.cloudNetworkFlow.tacheWoOrder;

import java.util.*;

import org.apache.axis.utils.StringUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.common.CommonWoOrderDealServiceIntf;
import com.zres.project.localnet.portal.common.ToOrderDealTacheWoOrderIntf;
import com.zres.project.localnet.portal.common.util.TacheIdEnum;


/**
 * @Classname StartMcpeConfigTache
 * @Description 启动MCPE业务配置处理环节处理逻辑
 * @Author guanzhao
 * @Date 2020/11/10 14:42
 */
@Service
public class StartMcpeConfigTache implements ToOrderDealTacheWoOrderIntf {

    Logger logger = LoggerFactory.getLogger(StartMcpeConfigTache.class);

    @Autowired
    private CommonWoOrderDealServiceIntf commonWoOrderDealServiceIntf;

    /*
     * 启动MCPE业务配置处理--环节到单处理逻辑
     * 1，移机该环节到单后启MCPE业务配置处理子流程；
     * 2，启动成功后直接回单该环节
     * @author guanzhao
     * @date 2020/11/10
     *
     */
    @Override
    public Map<String, Object> toOrderTacheDoSomething(Map<String, Object> toOrderTacheDoSomeMap) throws Exception {
        logger.info("-----------到单处理----------启动MCPE业务配置处理环节-----------------------");
        String orderId = MapUtils.getString(toOrderTacheDoSomeMap, "orderId");
        String woId = MapUtils.getString(toOrderTacheDoSomeMap, "woId");
        String tacheCode = MapUtils.getString(toOrderTacheDoSomeMap, "tacheCode");
        Map<String, Object> startChildMap = new HashMap<>();
        startChildMap.put("ordPsid", TacheIdEnum.YZW_MOVE_MCPE_CONFIG_CHILD_FLOW);
        startChildMap.put("parentOrderId", orderId);
        startChildMap.put("parentOrderCode", tacheCode);
        startChildMap.put("ORDER_TITLE", "MCPE业务配置处理专业子流程");
        startChildMap.put("AREA", "350002000000000042766408");
        startChildMap.put("ORDER_CONTENT", "子流程");
        try{
            commonWoOrderDealServiceIntf.createOrderService(startChildMap);
        } catch (Exception e) {
            throw e;
        }
        Map<String, Object> complateMap = new HashMap<>();
        complateMap.put("operStaffId", "-2000");
        complateMap.put("woId", woId);
        return commonWoOrderDealServiceIntf.complateWoService(complateMap);
    }
}
