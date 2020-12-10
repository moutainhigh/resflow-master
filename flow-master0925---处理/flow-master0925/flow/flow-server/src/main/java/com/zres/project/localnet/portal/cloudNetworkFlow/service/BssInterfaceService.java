package com.zres.project.localnet.portal.cloudNetworkFlow.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.cloudNetworkFlow.BssInterfaceServiceIntf;
import com.zres.project.localnet.portal.cloudNetworkFlow.dao.WoOrderDealDao;
import com.zres.project.localnet.portal.common.CommonWoOrderDealServiceIntf;
import com.zres.project.localnet.portal.common.util.FlowTacheUtil;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.logInfo.service.TacheDealLogIntf;
import com.zres.project.localnet.portal.util.OrderTrackOperType;

/**
 * @Classname BssInterfaceService
 * @Description 云组网业务流程与bss接口交互
 * @Author guanzhao
 * @Date 2020/11/9 10:09
 */
@Service
public class BssInterfaceService implements BssInterfaceServiceIntf {

    Logger logger = LoggerFactory.getLogger(BssInterfaceService.class);
    @Autowired
    private WoOrderDealDao woOrderDealDao;
    @Autowired
    private CommonWoOrderDealServiceIntf commonWoOrderDealServiceIntf;
    @Autowired
    private TacheDealLogIntf tacheDealLogIntf;
    @Autowired
    private OrderDealDao orderDealDao;

    @Override
    public Map<String, Object> callInteractiveInterface(Map<String, Object> intfParamMap) throws Exception {
        logger.info("---------------------与bss交互接口-----" + intfParamMap.toString() + "------------------");
        Map<String, Object> resMap = new HashMap<>();
        try {
            String orderId = MapUtils.getString(intfParamMap, "orderId");
            //String tacheCode = MapUtils.getString(intfParamMap, "tacheCode");
            String intfCode = MapUtils.getString(intfParamMap, "intfCode");
            Map<String, Object> srvOrdData = woOrderDealDao.qrySrvOrdDataByOrderId(orderId);
            if (MapUtils.isEmpty(srvOrdData)){
                //如果是空，说明是子流程环节
                Map<String, Object> parentOrderAndRegion = woOrderDealDao.qryParentOrderAndRegion(orderId);
                String parentOrderId = MapUtils.getString(parentOrderAndRegion, "PARENT_ORDER_ID");
                srvOrdData = woOrderDealDao.qrySrvOrdDataByOrderId(parentOrderId);
            }
            //业务号码 电路明细编号
            intfParamMap.put("serialNumber", MapUtils.getString(srvOrdData, "SERIAL_NUMBER"));
            //业务类型 产品类型 busi_type
            intfParamMap.put("serviceId", MapUtils.getString(srvOrdData, "SERVICE_ID"));
            intfParamMap.put("srvOrdId", MapUtils.getString(srvOrdData, "SRV_ORD_ID"));
            String intfName = "";
            switch (intfCode) {
                /*case CHECK_SUMMARY:  //客响资源核查
                    intfName = "资源核查反馈";
                    resMap = feedbBackServiceIntf.resCheckOrderFeedBack(intfParamMap);
                    break;
                default:
                    break;*/
            }
            if (MapUtils.getBoolean(resMap, "success")) {
                resMap.put("message", "调用与bss的" + intfName + "接口成功！");
            }
            else {
                String message = MapUtils.getString(resMap, "msg");
                resMap.put("message", "调用与bss的" + intfName + "接口失败！");
                throw new RuntimeException("调用与bss的" + intfName + "接口失败！" + message);
            }
        }
        catch (Exception e) {
            logger.error("调用与bss的接口失败：" + e);
            throw e;
        }
        return resMap;
    }

    @Override
    public Map<String, Object> rentFinshOrder(Map<String, Object> startAndStopRentParams) throws Exception {
        logger.info("--------------开通单-------集客下发起租通知-----------------------");
        Map<String, Object> resMap = new HashMap<>();
        try {
            String serialNumber = MapUtils.getString(startAndStopRentParams, "serialNumber");
            Map<String, Object> qryParam = new HashMap<>();
            qryParam.put("serialNumber", serialNumber);
            qryParam.put("tacheCode", FlowTacheUtil.YZW_RENT_FILE);
            //工单状态 执行中
            qryParam.put("woState", OrderTrackOperType.WO_ORDER_STATE_2);
            Map<String, Object> projectFinishMap = woOrderDealDao.qryWoOrderDataByThis(qryParam);
            if (MapUtils.isNotEmpty(projectFinishMap)) {
                String woId = MapUtils.getString(projectFinishMap, "WO_ID");
                String orderId = MapUtils.getString(projectFinishMap, "ORDER_ID");
                String srvOrdId = MapUtils.getString(projectFinishMap, "SRV_ORD_ID");
                //调资源归档接口
                Map<String, Object> rcMap = new HashMap<>();
                rcMap.put("srvOrdIds", srvOrdId);
                logger.info("--------------调用资源归档接口-----------------------");
                /*resMap = callingResInterface.completeBc4Rent(rcMap);
                String returnCode = MapUtils.getString(resMap, "returnCode");
                if ("0".equals(returnCode)) {
                    resMap.put("success", false);
                    resMap.put("message", MapUtils.getString(resMap, "returnMsg"));
                    return resMap;
                }*/
                Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(11);
                Map<String, Object> logDataMap = new HashMap<String, Object>();
                logDataMap.put("woId", woId);
                logDataMap.put("orderId", orderId);
                logDataMap.put("remark", "起租环节回单");
                logDataMap.put("operStaffInfoMap", operStaffInfoMap);
                logDataMap.put("operType", OrderTrackOperType.OPER_TYPE_4);
                logDataMap.put("action", "回单");
                logDataMap.put("trackMessage", "[ 回单 ]");
                tacheDealLogIntf.addTrackLog(logDataMap); //入库操作日志
                //修改电路状态为10F
                orderDealDao.updateSrvOrdState(srvOrdId, OrderTrackOperType.SRV_STATE_10F);
                Map<String, Object> complateMap = new HashMap<>();
                complateMap.put("operStaffId", "-2000");
                complateMap.put("woId", woId);
                resMap = commonWoOrderDealServiceIntf.complateWoService(complateMap);
            }
            else {
                resMap.put("success", true);
                resMap.put("message", "查询不到对应工单数据，请检查数据!");
            }
        }
        catch (Exception e) {
            logger.error("集客下发起租通知失败：" + e);
            throw e;
        }
        return resMap;
    }
}
