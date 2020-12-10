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
import com.zres.project.localnet.portal.common.DealTacheWoOrderIntf;
import com.zres.project.localnet.portal.common.ToOrderDealTacheWoOrderIntf;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.logInfo.service.TacheDealLogIntf;
import com.zres.project.localnet.portal.util.OrderTrackOperType;

import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;

/**
 * @Classname SingleMcpeOfflineTache
 * @Description 单端MCPE终端盒下线环节处理
 * @Author guanzhao
 * @Date 2020/11/25 10:54
 */
@Service
public class SingleMcpeOfflineTache implements ToOrderDealTacheWoOrderIntf, CloudFlowFeedbackIntf, DealTacheWoOrderIntf {

    Logger logger = LoggerFactory.getLogger(SingleMcpeOfflineTache.class);

    @Autowired
    private CommonWoOrderDealServiceIntf commonWoOrderDealServiceIntf;
    @Autowired
    private TacheDealLogIntf tacheDealLogIntf;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private CloudNetworkInterfaceService cloudNetworkInterfaceService;

    /*
     * 单端MCPE终端盒下线环节到单，调用云网平台--终端盒下线通知接口
     * @author guanzhao
     * @date 2020/11/25
     *
     */
    @Override
    public Map<String, Object> toOrderTacheDoSomething(Map<String, Object> toOrderTacheDoSomeMap) throws Exception {
        logger.info("-----------到单处理----------单端MCPE终端盒下线环节-----------------------");
        String orderId = MapUtils.getString(toOrderTacheDoSomeMap, "orderId");
        String woId = MapUtils.getString(toOrderTacheDoSomeMap, "woId");
        Map<String, Object> intfParamMap = new HashMap<>();
        intfParamMap.put("orderId", orderId);
        intfParamMap.put("intfCode", IntfEnumUtil.YZW_OFFLINECALL); //接口名称编码
        Map<String, Object> resMap = cloudNetworkInterfaceService.callCloudNetworkInterface(intfParamMap);
        if (MapUtils.getBoolean(resMap, "success")) {
            orderDealDao.updateWoStateByWoId(woId, OrderTrackOperType.WO_ORDER_STATE_20);
        }else {
            throw new Exception("调用云网平台——终端盒下线通知接口失败！");
        }
        return null;
    }

    /*
     * 接受云网平台--终端盒下线结果反馈接口
     * --->>> 成功：回单改环节；
     * --->>> 失败：出人工单；
     * @author guanzhao
     * @date 2020/11/25
     *
     */
    @Override
    public Map<String, Object> feedbackDoSomething(Map<String, Object> toOrderTacheDoSomeMap) throws Exception {
        logger.info("-----------云网平台调用-------进入流程部分-------单端MCPE终端盒下线环节-------------");
        Map<String, Object> resMap = new HashMap<>();
        boolean success = MapUtils.getBoolean(toOrderTacheDoSomeMap, "success");
        String woId = MapUtils.getString(toOrderTacheDoSomeMap, "woId");
        //String orderId = MapUtils.getString(toOrderTacheDoSomeMap, "orderId");
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

    @Override
    public Map<String, Object> tacheDoSomething(Map<String, Object> tacheDoSomeMap) throws Exception {
        logger.info("---------------------进入单端MCPE终端盒下线环节处理-----------------------");
        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(operStaffId));
        Map<String, String> operAttrs = MapUtils.getMap(tacheDoSomeMap, "operAttrs");
        String remark = MapUtils.getString(operAttrs, "remark");
        Map<String, Object> circuitDataMap = MapUtils.getMap(tacheDoSomeMap, "circuitDataMap");
        String woId = MapUtils.getString(circuitDataMap, "WO_ID");
        String orderId = MapUtils.getString(circuitDataMap, "ORDER_ID");
        String tacheId = MapUtils.getString(circuitDataMap, "TACHE_ID");
        Map<String, Object> logDataMap = new HashMap<String, Object>();
        logDataMap.put("woId", woId);
        logDataMap.put("orderId", orderId);
        logDataMap.put("remark", remark == null ? "" : remark);
        logDataMap.put("tacheId", tacheId);
        logDataMap.put("operStaffInfoMap", operStaffInfoMap);
        logDataMap.put("operType", OrderTrackOperType.OPER_TYPE_4);
        logDataMap.put("action", "回单");
        logDataMap.put("trackMessage", "[ 回单 ]");
        tacheDealLogIntf.addTrackLog(logDataMap); //入库操作日志
        Map<String, Object> complateMap = new HashMap<>();
        complateMap.put("operStaffId", operStaffId);
        complateMap.put("woId", woId);
        return commonWoOrderDealServiceIntf.complateWoService(complateMap);
    }
}
