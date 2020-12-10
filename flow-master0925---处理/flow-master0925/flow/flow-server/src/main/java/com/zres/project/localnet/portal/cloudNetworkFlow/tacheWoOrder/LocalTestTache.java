package com.zres.project.localnet.portal.cloudNetworkFlow.tacheWoOrder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.cloudNetworkFlow.service.CloudNetworkInterfaceService;
import com.zres.project.localnet.portal.cloudNetworkFlow.util.IntfEnumUtil;
import com.zres.project.localnet.portal.common.CommonWoOrderDealServiceIntf;
import com.zres.project.localnet.portal.common.DealTacheWoOrderIntf;
import com.zres.project.localnet.portal.common.ToOrderDealTacheWoOrderIntf;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.logInfo.service.TacheDealLogIntf;
import com.zres.project.localnet.portal.util.OrderTrackOperType;

import com.ztesoft.res.frame.flow.common.dto.FlowRollBackReasonDTO;
import com.ztesoft.res.frame.flow.common.intf.FlowAction;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;

/**
 * @Classname LocalTestTache
 * @Description az端，本地测试报竣环节处理
 * @Author guanzhao
 * @Date 2020/11/16 14:52
 */
@Service
public class LocalTestTache implements DealTacheWoOrderIntf, ToOrderDealTacheWoOrderIntf {
    Logger logger = LoggerFactory.getLogger(LocalTestTache.class);

    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private FlowAction flowAction;
    @Autowired
    private TacheDealLogIntf tacheDealLogIntf;
    @Autowired
    private CommonWoOrderDealServiceIntf commonWoOrderDealServiceIntf;
    @Autowired
    private CloudNetworkInterfaceService cloudNetworkInterfaceService;

    @Override
    public Map<String, Object> tacheDoSomething(Map<String, Object> tacheDoSomeMap) throws Exception {
        logger.info("-----------提交进入----------az端本地测试报竣环节-----------------------");
        Map<String, Object> resMap = new HashMap<>();
        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(operStaffId));
        Map<String, String> operAttrs = MapUtils.getMap(tacheDoSomeMap, "operAttrs");
        String remark = MapUtils.getString(operAttrs, "remark");
        String buttonAction = MapUtils.getString(tacheDoSomeMap, "buttonAction"); //按钮动作
        Map<String, Object> circuitDataMap = MapUtils.getMap(tacheDoSomeMap, "circuitDataMap");
        String woId = MapUtils.getString(circuitDataMap, "WO_ID");
        String orderId = MapUtils.getString(circuitDataMap, "ORDER_ID");
        String tacheId = MapUtils.getString(circuitDataMap, "TACHE_ID");
        if (OrderTrackOperType.SUBMIT_BUTTON.equals(buttonAction)) {
            //提交
            Map<String, Object> complateMap = new HashMap<>();
            complateMap.put("operStaffId", operStaffId);
            complateMap.put("woId", woId);
            resMap = commonWoOrderDealServiceIntf.complateWoService(complateMap);
        }
        else if (OrderTrackOperType.ROLLBACK_BUTTON.equals(buttonAction)) {
            //退单--调用云网平台终端盒下线通知接口，将盒子下线，成功后流程退单；
            Map<String, Object> intfParamMap = new HashMap<>();
            intfParamMap.put("orderId", orderId);
            intfParamMap.put("intfCode", IntfEnumUtil.YZW_OFFLINECALL); //接口名称编码
            Map<String, Object> intfResMap = cloudNetworkInterfaceService.callCloudNetworkInterface(intfParamMap);
            if (MapUtils.getBoolean(intfResMap, "success")) {
                Map<String, Object> backOrderParams = new HashMap<>();
                List<FlowRollBackReasonDTO> flowRollBackReasonDTOs = flowAction.queryRollBackReasons(woId);
                FlowRollBackReasonDTO flowRollBackReasonDTO = flowRollBackReasonDTOs.get(0);
                backOrderParams.put("woId", woId);
                backOrderParams.put("flowRollBackReasonDTO", flowRollBackReasonDTO);
                backOrderParams.put("operStaffId", operStaffId);
                backOrderParams.put("remark", remark == null ? "" : remark);
                resMap = commonWoOrderDealServiceIntf.rollBackWoService(backOrderParams);
            }else {
                throw new Exception("调用云网平台终端盒下线通知接口失败！");
            }
        }
        String operType = "rollback".equals(buttonAction) ? OrderTrackOperType.OPER_TYPE_5 : OrderTrackOperType.OPER_TYPE_4;
        String action = "rollback".equals(buttonAction) ? "退单" : "回单";
        Map<String, Object> logDataMap = new HashMap<String, Object>();
        logDataMap.put("woId", woId);
        logDataMap.put("orderId", orderId);
        logDataMap.put("remark", remark == null ? "" : remark);
        logDataMap.put("tacheId", tacheId);
        logDataMap.put("operStaffInfoMap", operStaffInfoMap);
        logDataMap.put("operType", operType);
        logDataMap.put("action", action);
        logDataMap.put("trackMessage", "[ " + action + "]");
        tacheDealLogIntf.addTrackLog(logDataMap); //入库操作日志
        return resMap;
    }

    /*
     * az端本地测试报竣环节到单处理逻辑：
     *  到单调接口--本地业务测试接口，测试不通过转手工
     * @author guanzhao
     * @date 2020/11/4
     *
     */
    @Override
    public Map<String, Object> toOrderTacheDoSomething(Map<String, Object> toOrderTacheDoSomeMap) throws Exception {
        logger.info("-----------到单处理------本地业务----az端，本地测试报竣环节-----------------------");
        String orderId = MapUtils.getString(toOrderTacheDoSomeMap, "orderId");
        String woId = MapUtils.getString(toOrderTacheDoSomeMap, "woId");
        Map<String, Object> intfParamMap = new HashMap<>();
        intfParamMap.put("orderId", orderId);
        intfParamMap.put("intfCode", IntfEnumUtil.YZW_LOCALTEST); //接口名称编码
        Map<String, Object> resMap = cloudNetworkInterfaceService.callCloudNetworkInterface(intfParamMap);
        if (MapUtils.getBoolean(resMap, "success")) {
            Map<String, Object> complateMap = new HashMap<>();
            complateMap.put("operStaffId", "-2000");
            complateMap.put("woId", woId);
            resMap = commonWoOrderDealServiceIntf.complateWoService(complateMap);
        }
        return resMap;
    }
}
