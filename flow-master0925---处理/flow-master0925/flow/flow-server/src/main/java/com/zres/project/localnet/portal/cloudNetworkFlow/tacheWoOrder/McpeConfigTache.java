package com.zres.project.localnet.portal.cloudNetworkFlow.tacheWoOrder;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.cloudNetworkFlow.service.CloudNetworkInterfaceService;
import com.zres.project.localnet.portal.cloudNetworkFlow.util.IntfEnumUtil;
import com.zres.project.localnet.portal.common.CloudFlowFeedbackIntf;
import com.zres.project.localnet.portal.common.CommonWoOrderDealServiceIntf;
import com.zres.project.localnet.portal.common.ToOrderDealTacheWoOrderIntf;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.logInfo.service.TacheDealLogIntf;
import com.zres.project.localnet.portal.util.OrderTrackOperType;

/**
 * @Classname McpeConfigTache
 * @Description mcpe业务配置环节处理
 * @Author guanzhao
 * @Date 2020/11/24 11:27
 */
@Service
public class McpeConfigTache implements ToOrderDealTacheWoOrderIntf, CloudFlowFeedbackIntf {

    Logger logger = LoggerFactory.getLogger(McpeConfigTache.class);

    @Autowired
    private CloudNetworkInterfaceService cloudNetworkInterfaceService;
    @Autowired
    private CommonWoOrderDealServiceIntf commonWoOrderDealServiceIntf;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private TacheDealLogIntf tacheDealLogIntf;

    /*
     * mcpe业务配置到单逻辑：
     * 调用云网平台--MCPE业务配置下发接口，成功后修改工单状态等待云网平台反馈
     * @author guanzhao
     * @date 2020/11/23
     *
     */
    @Override
    public Map<String, Object> toOrderTacheDoSomething(Map<String, Object> toOrderTacheDoSomeMap) throws Exception {
        logger.info("-----------到单处理----------mcpe业务配置环节-----------------------");
        String orderId = MapUtils.getString(toOrderTacheDoSomeMap, "orderId");
        String woId = MapUtils.getString(toOrderTacheDoSomeMap, "woId");
        Map<String, Object> intfParamMap = new HashMap<>();
        intfParamMap.put("orderId", orderId);
        intfParamMap.put("intfCode", IntfEnumUtil.YZW_CONFIGCALL); //接口名称编码
        Map<String, Object> resMap = cloudNetworkInterfaceService.callCloudNetworkInterface(intfParamMap);
        if (MapUtils.getBoolean(resMap, "success")) {
            orderDealDao.updateWoStateByWoId(woId, OrderTrackOperType.WO_ORDER_STATE_20);
        }else {
            throw new Exception("调用云网平台——MCPE业务配置下发接口失败！");
        }
        return null;
    }

    @Override
    public Map<String, Object> feedbackDoSomething(Map<String, Object> toOrderTacheDoSomeMap) throws Exception {
        logger.info("-----------云网平台调用-------进入流程部分-------mcpe业务配置环节-------------");
        Map<String, Object> resMap = new HashMap<>();
        boolean success = MapUtils.getBoolean(toOrderTacheDoSomeMap, "success");
        String woId = MapUtils.getString(toOrderTacheDoSomeMap, "woId");
        orderDealDao.updateWoStateByWoId(woId, OrderTrackOperType.WO_ORDER_STATE_2);
        if (success){
            Map<String, Object> complateMap = new HashMap<>();
            complateMap.put("operStaffId", "-2000");
            complateMap.put("woId", woId);
            resMap = commonWoOrderDealServiceIntf.complateWoService(complateMap);
        }else {
            resMap.put("success", true);
            resMap.put("message", "处理成功!");
        }
        return resMap;
    }
}
