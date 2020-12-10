package com.zres.project.localnet.portal.cloudNetworkFlow.tacheWoOrder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.cloudNetworkFlow.dao.TacheWoOrderDao;
import com.zres.project.localnet.portal.cloudNetworkFlow.dao.WoOrderDealDao;
import com.zres.project.localnet.portal.cloudNetworkFlow.service.CloudNetworkInterfaceService;
import com.zres.project.localnet.portal.cloudNetworkFlow.util.IntfEnumUtil;
import com.zres.project.localnet.portal.common.CloudFlowFeedbackIntf;
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
 * @Classname McpeInstallTestTache
 * @Description az端，MCPE安装测试环节处理
 * @Author guanzhao
 * @Date 2020/11/5 14:52
 */
@Service
public class McpeInstallTestTache implements DealTacheWoOrderIntf, ToOrderDealTacheWoOrderIntf, CloudFlowFeedbackIntf {
    Logger logger = LoggerFactory.getLogger(McpeInstallTestTache.class);

    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private FlowAction flowAction;
    @Autowired
    private TacheDealLogIntf tacheDealLogIntf;
    @Autowired
    private TacheWoOrderDao tacheWoOrderDao;
    @Autowired
    private WoOrderDealDao woOrderDealDao;
    @Autowired
    private CommonWoOrderDealServiceIntf commonWoOrderDealServiceIntf;
    @Autowired
    private CloudNetworkInterfaceService cloudNetworkInterfaceService;

    @Override
    public Map<String, Object> tacheDoSomething(Map<String, Object> tacheDoSomeMap) throws Exception {
        logger.info("-----------提交进入----------az端MCPE安装测试环节-----------------------");
        Map<String, Object> resMap = new HashMap<>();
        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(operStaffId));
        String remark = "";
        String buttonAction = MapUtils.getString(tacheDoSomeMap, "buttonAction"); //按钮动作
        Map<String, Object> circuitDataMap = MapUtils.getMap(tacheDoSomeMap, "circuitDataMap");
        String woId = MapUtils.getString(circuitDataMap, "WO_ID");
        String orderId = MapUtils.getString(circuitDataMap, "ORDER_ID");
        String tacheId = MapUtils.getString(circuitDataMap, "TACHE_ID");
        if (OrderTrackOperType.SUBMIT_BUTTON.equals(buttonAction)) {
            Map<String, String> operAttrs = MapUtils.getMap(tacheDoSomeMap, "operAttrs");
            remark = MapUtils.getString(operAttrs, "remark");
            Map<String, Object> complateMap = new HashMap<>();
            complateMap.put("operStaffId", operStaffId);
            complateMap.put("woId", woId);
            resMap = commonWoOrderDealServiceIntf.complateWoService(complateMap);
        }
        else if (OrderTrackOperType.ROLLBACK_BUTTON.equals(buttonAction)) {
            /*
             * MCPE安装测试环节退单：
             * 1，退单页面上传过来的专业区域子流程；
             * 2，将主流程从MCPE安装测试环节退单到网络施工环节
             * @author guanzhao
             * @date 2020/11/16
             *
             */
            remark = MapUtils.getString(circuitDataMap, "remark");
            String privTacheCode = woOrderDealDao.qryPrivTacheCode(woId);
            String specialtyStr = MapUtils.getString(circuitDataMap, "specialty");
            if (!StringUtils.isEmpty(specialtyStr) && !"[]".equals(specialtyStr)) {
                specialtyStr = StringUtils.strip(specialtyStr, "[]");
                String[] specialty = specialtyStr.split("\",\"");
                for (int i = 0; i < specialty.length ; i++) {
                    String[] specialtyAndDeptId = specialty[i].split(",");
                    Map<String, String> qryMap = new HashMap<>();
                    qryMap.put("orderId", orderId);
                    qryMap.put("tacheCode", privTacheCode);
                    qryMap.put("specialtyCode", StringUtils.strip(specialtyAndDeptId[0], "\"\""));
                    qryMap.put("regionId", StringUtils.strip(specialtyAndDeptId[1], "\"\""));
                    Map<String, String> backOrderData = tacheWoOrderDao.qryBackOrderWoId(qryMap);
                    String woIdChild = MapUtils.getString(backOrderData, "WO_ID");
                    Map<String, Object> backOrderChildParams = new HashMap<>();
                    List<FlowRollBackReasonDTO> flowRollBackReasonDTOChilds = flowAction.queryRollBackReasons(woIdChild);
                    FlowRollBackReasonDTO flowRollBackReasonChildDTO = flowRollBackReasonDTOChilds.get(0);
                    backOrderChildParams.put("woId", woIdChild);
                    backOrderChildParams.put("flowRollBackReasonDTO", flowRollBackReasonChildDTO);
                    backOrderChildParams.put("operStaffId", operStaffId);
                    backOrderChildParams.put("remark", remark == null ? "" : remark);
                    commonWoOrderDealServiceIntf.rollBackWoService(backOrderChildParams);
                }
                Map<String, Object> backOrderParams = new HashMap<>();
                List<FlowRollBackReasonDTO> flowRollBackReasonDTOs = flowAction.queryRollBackReasons(woId);
                FlowRollBackReasonDTO flowRollBackReasonDTO = flowRollBackReasonDTOs.get(0);
                backOrderParams.put("woId", woId);
                backOrderParams.put("flowRollBackReasonDTO", flowRollBackReasonDTO);
                backOrderParams.put("operStaffId", operStaffId);
                backOrderParams.put("remark", remark == null ? "" : remark);
                resMap = commonWoOrderDealServiceIntf.rollBackWoService(backOrderParams);
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
     * az端MCPE安装测试环节到单处理逻辑：
     * 1，查询az端是否有终端盒，有则不需要安装终端，直接回单该环节；
     *                       没有则需要安装终端，先调用--终端盒序列号上报接口，调用成功修改工单状态等待云网平台反馈；
     * @author guanzhao
     * @date 2020/11/4
     *
     */
    @Override
    public Map<String, Object> toOrderTacheDoSomething(Map<String, Object> toOrderTacheDoSomeMap) throws Exception {
        logger.info("-----------到单处理----------az端MCPE安装测试环节-----------------------");
        String orderId = MapUtils.getString(toOrderTacheDoSomeMap, "orderId");
        String woId = MapUtils.getString(toOrderTacheDoSomeMap, "woId");
        //todo:是否需要安装终端这个条件需要查询，目前还不知道怎么查，就先默认需要，不做回单操作；
        if(false){
            Map<String, Object> complateMap = new HashMap<>();
            complateMap.put("operStaffId", "-2000");
            complateMap.put("woId", woId);
            commonWoOrderDealServiceIntf.complateWoService(complateMap);
        }else {
            Map<String, Object> intfParamMap = new HashMap<>();
            intfParamMap.put("orderId", orderId);
            intfParamMap.put("intfCode", IntfEnumUtil.YZW_ONLINECALL); //接口名称编码
            Map<String, Object> resMap = cloudNetworkInterfaceService.callCloudNetworkInterface(intfParamMap);
            if (MapUtils.getBoolean(resMap, "success")) {
                orderDealDao.updateWoStateByWoId(woId, OrderTrackOperType.WO_ORDER_STATE_20);
            }else {
                throw new Exception("调用云网平台——终端盒序列号上报接口失败！");
            }
        }
        return null;
    }

    /*
     * 云网平台--3.6	终端盒上线结果反馈接口--调用该方法：
     * 1, 反馈上线成功，则回单该环节；
     *        上线失败，则修改工单状态处理中，需要用户手动处理，并支持退单；
     * @author guanzhao
     * @date 2020/11/23
     *
     */
    @Override
    public Map<String, Object> feedbackDoSomething(Map<String, Object> toOrderTacheDoSomeMap) throws Exception {
        logger.info("-----------云网平台调用-------进入流程部分-------az端，MCPE安装测试环节-------------");
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
