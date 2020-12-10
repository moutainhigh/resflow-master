package com.zres.project.localnet.portal.cloudNetworkFlow.tacheWoOrder;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.cloudNetworkFlow.dao.WoOrderDealDao;
import com.zres.project.localnet.portal.cloudNetworkFlow.service.CloudNetworkInterfaceService;
import com.zres.project.localnet.portal.cloudNetworkFlow.util.IntfEnumUtil;
import com.zres.project.localnet.portal.common.CloudFlowFeedbackIntf;
import com.zres.project.localnet.portal.common.CommonWoOrderDealServiceIntf;
import com.zres.project.localnet.portal.common.ToOrderDealTacheWoOrderIntf;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.logInfo.service.TacheDealLogIntf;
import com.zres.project.localnet.portal.util.OrderTrackOperType;

/**
 * @Classname UpequipBusiConfigTache
 * @Description 上联设备业务配置环节处理--子流程
 * @Author guanzhao
 * @Date 2020/11/24 09:47
 */
@Service
public class UpequipBusiConfigTache implements ToOrderDealTacheWoOrderIntf, CloudFlowFeedbackIntf {

    Logger logger = LoggerFactory.getLogger(UpequipBusiConfigTache.class);

    @Autowired
    private CloudNetworkInterfaceService cloudNetworkInterfaceService;
    @Autowired
    private CommonWoOrderDealServiceIntf commonWoOrderDealServiceIntf;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private TacheDealLogIntf tacheDealLogIntf;
    @Autowired
    private WoOrderDealDao woOrderDealDao;

    /*
     * 上联设备业务配置环节处理--子流程到单处理逻辑
     * 1，先调用--业务路由配置下发通知接口（2.10 IPRAN业务路由配置下发通知接口），成功后等待云网平台调用；
     * @author guanzhao
     * @date 2020/11/24
     *
     */
    @Override
    public Map<String, Object> toOrderTacheDoSomething(Map<String, Object> toOrderTacheDoSomeMap) throws Exception {
        logger.info("-----------到单处理----------上联设备业务配置环节-----------------------");
        String orderId = MapUtils.getString(toOrderTacheDoSomeMap, "orderId");
        String woId = MapUtils.getString(toOrderTacheDoSomeMap, "woId");
        Map<String, Object> intfParamMap = new HashMap<>();
        intfParamMap.put("orderId", orderId);
        intfParamMap.put("intfCode", IntfEnumUtil.YZW_IPRANROUTECALL); //接口名称编码
        Map<String, Object> resMap = cloudNetworkInterfaceService.callCloudNetworkInterface(intfParamMap);
        if (MapUtils.getBoolean(resMap, "success")) {
            orderDealDao.updateWoStateByWoId(woId, OrderTrackOperType.WO_ORDER_STATE_20);
        }else {
            throw new Exception("调用云网平台——业务路由配置下发通知接口失败！");
        }
        return null;
    }

    /*
     * 接受云网平台--上联设备（IPRAN、M3）业务路由配置下发通知接口（3.10 IPRAN业务路由配置下发结果反馈接口）；
     * --->>>成功后调用云网平台上联设备端口限速接口（2.11 IPRAN端口限速下发通知接口）
     * --->>>失败，出人工单；
     *
     * 接受云网平台--IPRAN端口限速配置下发结果反馈接口
     * --->>> 成功：回单；
     * --->>> 失败：出人工单；
     * @author guanzhao
     * @date 2020/11/24
     *
     */
    @Override
    public Map<String, Object> feedbackDoSomething(Map<String, Object> toOrderTacheDoSomeMap) throws Exception {
        logger.info("-----------云网平台调用-------进入流程部分-------上联设备业务配置环节-------------");
        Map<String, Object> resMap = new HashMap<>();
        String intfCode = MapUtils.getString(toOrderTacheDoSomeMap, "intfCode");
        String orderId = MapUtils.getString(toOrderTacheDoSomeMap, "orderId");
        String woId = MapUtils.getString(toOrderTacheDoSomeMap, "woId");
        orderDealDao.updateWoStateByWoId(woId, OrderTrackOperType.WO_ORDER_STATE_2);
        if (IntfEnumUtil.OSS_IPRANROUTEREPLY.equals(intfCode)){
            Map<String, Object> intfParamMap = new HashMap<>();
            intfParamMap.put("orderId", orderId);
            intfParamMap.put("intfCode", IntfEnumUtil.YZW_IPRANQOSCALL); //接口名称编码
            Map<String, Object> intfResMap = cloudNetworkInterfaceService.callCloudNetworkInterface(intfParamMap);
            if (MapUtils.getBoolean(intfResMap, "success")) {
                orderDealDao.updateWoStateByWoId(woId, OrderTrackOperType.WO_ORDER_STATE_20);
            }else {
                resMap.put("success", true);
                resMap.put("message", "处理成功!");
            }
        }else if (IntfEnumUtil.OSS_IPRANROUTEREPLY_0.equals(intfCode)){
            Map<String, Object> complateMap = new HashMap<>();
            complateMap.put("operStaffId", "-2000");
            complateMap.put("woId", woId);
            resMap = commonWoOrderDealServiceIntf.complateWoService(complateMap);
        }
        return resMap;
    }
}
