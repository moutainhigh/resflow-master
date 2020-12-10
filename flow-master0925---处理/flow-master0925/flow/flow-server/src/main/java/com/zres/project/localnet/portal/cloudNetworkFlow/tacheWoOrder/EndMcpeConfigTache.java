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
import com.zres.project.localnet.portal.common.util.FlowTacheUtil;

/**
 * @Classname EndMcpeConfigTache
 * @Description 完成MCPE业务配置处理环节处理--移机父流程
 * @Author guanzhao
 * @Date 2020/11/11 10:06
 */
@Service
public class EndMcpeConfigTache implements ToOrderDealTacheWoOrderIntf {

    Logger logger = LoggerFactory.getLogger(EndMcpeConfigTache.class);

    @Autowired
    private CommonWoOrderDealServiceIntf commonWoOrderDealServiceIntf;
    @Autowired
    private TacheWoOrderDao tacheWoOrderDao;

    /*
     * 完成MCPE业务配置处理环节到单处理逻辑--移机父流程
     * 1，到单查询MCPE业务配置处理子流程是否结束（是否到最后一个环节），YZW_MCPE_CONFIG_FINISH
     *      如果是，直接回单该环节；
     *      如果否，不处理，等待MCPE业务配置处理子流程到达最后一个环节来回单这个环节；
     *
     * @author guanzhao
     * @date 2020/11/11
     *
     */
    @Override
    public Map<String, Object> toOrderTacheDoSomething(Map<String, Object> toOrderTacheDoSomeMap) throws Exception {
        logger.info("-----------到单处理---------移机父流程--完成MCPE业务配置处理环节-----------------------");
        String orderId = MapUtils.getString(toOrderTacheDoSomeMap, "orderId");
        String woId = MapUtils.getString(toOrderTacheDoSomeMap, "woId");
        Map<String, String> qryMap = new HashMap<>();
        qryMap.put("parentOrderId", orderId);
        qryMap.put("parentOrderCode", FlowTacheUtil.YZW_START_MCPE_CONFIG);
        qryMap.put("tacheCode", FlowTacheUtil.YZW_MCPE_CONFIG_FINISH);
        int childLastNum = tacheWoOrderDao.qryChildFlowNumAtLastNew(qryMap);
        if (childLastNum == 1) {
            Map<String, Object> complateMap = new HashMap<>();
            complateMap.put("operStaffId", "-2000");
            complateMap.put("woId", woId);
            commonWoOrderDealServiceIntf.complateWoService(complateMap);
        }
        return null;
    }

}
