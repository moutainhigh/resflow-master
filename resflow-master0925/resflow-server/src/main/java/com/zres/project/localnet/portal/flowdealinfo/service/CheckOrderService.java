package com.zres.project.localnet.portal.flowdealinfo.service;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.CheckFeedbackDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.CheckOrderDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.logInfo.service.TacheDealLogIntf;
import com.zres.project.localnet.portal.util.OrderTrackOperType;
import com.zres.project.localnet.portal.webservice.res.BusinessRollbackServiceIntf;
import com.ztesoft.res.frame.core.util.ListUtil;
import com.ztesoft.res.frame.flow.common.dto.FlowRollBackReasonDTO;
import com.ztesoft.res.frame.flow.common.dto.FlowTacheDTO;
import com.ztesoft.res.frame.flow.common.intf.FlowAction;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CheckOrderService implements CheckOrderServiceIntf {

    Logger logger = LoggerFactory.getLogger(CheckOrderService.class);

    @Autowired
    FlowAction flowAction;
    @Autowired
    CheckOrderDao checkOrderDao;
    @Autowired
    OrderDealDao orderDealDao;
    @Autowired
    private CheckFeedbackDao checkFeedbackDao;
    @Autowired
    TacheDealLogIntf tacheDealLogIntf;
    @Autowired
    BusinessRollbackServiceIntf businessRollbackServiceIntf;
    @Override
    public Map<String, Object> specialCheckBackOrder(Map<String, Object> backOrderParam) {
        logger.info("-------------------专业核查退单------------------------");
        Map<String, Object> resMap = new HashMap<String, Object>();
        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        /**
         * 专业核查环节退单逻辑：
         *   先查询专业核查的数量；--查询非作废的数量
         *      如果数量大于1，则作废当前工单，查询当前定单的核查调度环节，并修改核查调度环节的工单状态；
         *      如果数量小于等于1，则调退单接口，从专业核查退单到核查调度；
         */
        try {
            String woId = MapUtils.getString(backOrderParam, "woId");
            String orderId = MapUtils.getString(backOrderParam, "orderId");
            String remark = MapUtils.getString(backOrderParam, "remark");
            // 先调资源回滚接口
            Map<String,Object> resParams = new HashMap<>();
            resParams.put("tacheId",MapUtils.getString(backOrderParam,"tacheId"));
            List orderIdList = new ArrayList();
            orderIdList.add(woId);
            resParams.put("srvOrdId", MapUtils.getString(backOrderParam, "srvOrdId"));// gom_bdw_srv_ord_info.srv_ord_id
            resParams.put("orderIds", orderIdList);// orderIds是个List, 存放的是 专业核查的工单id
            resParams.put("rollbackDesc", remark); // 回滚原因
            resMap = businessRollbackServiceIntf.resRollBack(resParams);

            // 删除专业核查的反馈信息
            checkFeedbackDao.deleteInfoByWoId(woId);

            List<String> woIds = new ArrayList<String>();
            woIds.add(woId);
            flowAction.disableWo(operStaffId, woIds);
            Map<String, Object> checkDispatchWoOrder = checkOrderDao.qryCheckDispatchWoOrder(orderId, BasicCode.CHECK_SCHEDULING);
            String checkDispatchWoId = MapUtils.getString(checkDispatchWoOrder, "WO_ID");
            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put("woId", checkDispatchWoId);
            updateMap.put("woState", OrderTrackOperType.WO_ORDER_STATE_2);
            checkOrderDao.updateWoOrderStateByWoId(updateMap);
            Map<String, Object> logDataMap = new HashMap<String, Object>();
            logDataMap.put("woId", woId);
            logDataMap.put("orderId", orderId);
            logDataMap.put("remark", remark);
            logDataMap.put("tacheId", MapUtils.getString(backOrderParam, "tacheId"));
            logDataMap.put("operType", OrderTrackOperType.OPER_TYPE_5);
            logDataMap.put("action", "退单");
            logDataMap.put("trackMessage", "[退单]" );
            tacheDealLogIntf.addTrackLog(logDataMap);
            resMap.put("success", true);
            resMap.put("message", "退单成功！");
        }catch (Exception e){
            e.printStackTrace();
            resMap.put("success", false);
            resMap.put("message", "退单失败！");
        }
        return resMap;
    }

    @Override
    public Map<String, Object> checkTotalBackOrder(Map<String, Object> backOrderParam){
        logger.info("-------------------核查汇总退单------------------------");
        Map<String, Object> resMap = new HashMap<String, Object>();
        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        /**
         * 核查汇总环节退单：
         * 1，退单到核查调度：
         *      作废所有专业核查--流程平台会自动作废；回滚资源--目前没有按专业回滚先不做；
         * 2，退单到专业核查：
         *      退单所选专业；
         */
        try{
            String woId = MapUtils.getString(backOrderParam, "woId");
            String orderId = MapUtils.getString(backOrderParam, "orderId");
            String remark = MapUtils.getString(backOrderParam, "remark");
            String tacheId = MapUtils.getString(backOrderParam, "tacheId");
            String toTacheflag = MapUtils.getString(backOrderParam, "toTacheFlag");
            String toSpecialTache = "";
            List<Map<String,Object>> list = new ArrayList<>();
            boolean isLocalCheck = false;
            if (OrderTrackOperType.DISPATCH_CHECK.equals(toTacheflag)){
                checkFeedbackDao.deleteInfoBySrvOrdId(MapUtils.getString(backOrderParam, "srvOrdId"));
                toSpecialTache = BasicCode.CHECK_SCHEDULING;
                // 查询所有专业核查环节工单
                list = checkOrderDao.qryCompleteCheckSpecInfo(orderId);
            }else if (OrderTrackOperType.SPECIAL_CHECK.equals(toTacheflag)){
                toSpecialTache = MapUtils.getString(backOrderParam, "toSpecialTache");
                // 删除核查汇总的反馈信息
                checkFeedbackDao.deleteInfoByWoId(woId);
                // 查询当前专业核查环节工单
                list = checkOrderDao.qryCurrentCompleteCheckSpecInfo(orderId,toSpecialTache);
                if(!CollectionUtils.isEmpty(list)){
                    for(Map<String,Object> tmp : list){
                        // 删除专业核查的反馈信息
                        checkFeedbackDao.deleteInfoByWoId(MapUtils.getString(tmp,"WO_ID",""));
                    }
                }
                isLocalCheck = BasicCode.LOCAL_NETWORK_CHECK.equals(toSpecialTache);
                if (isLocalCheck){ //如果退单本地核查
                    String toLocalAreaOrderIdStr = MapUtils.getString(backOrderParam, "areaArray");
                    toLocalAreaOrderIdStr = toLocalAreaOrderIdStr.replace("[", "").replace("]", "");
                    String[] toLocalAreaOrderIdArray = toLocalAreaOrderIdStr.split(",");
                    for (int i = 0; i < toLocalAreaOrderIdArray.length; i++) {
                        String toLocalWoId = checkOrderDao.qryLocalCheckWaitTacheId(toLocalAreaOrderIdArray[i].replaceAll("\"", ""));
                        List<FlowRollBackReasonDTO> flowRollBackReasonDTOs = flowAction.queryRollBackReasons(toLocalWoId);
                        FlowRollBackReasonDTO flowRollBackReasonDTO = flowRollBackReasonDTOs.get(0);
                        flowAction.rollBackWo(operStaffId, toLocalWoId, flowRollBackReasonDTO, "二干退本地：" + remark);
                    }
                }
            }
            if(!CollectionUtils.isEmpty(list)&& (!isLocalCheck)){
                for(Map<String,Object> tmp : list){
                    // 调资源回滚接口srvOrdId
                    Map<String,Object> resParams = new HashMap<>();
                    List orderIdList = new ArrayList();
                    orderIdList.add(MapUtils.getString(tmp,"WO_ID"));
                    resParams.put("srvOrdId", MapUtils.getString(backOrderParam, "srvOrdId"));// gom_bdw_srv_ord_info.srv_ord_id
                    resParams.put("orderIds", orderIdList);// orderIds是个List, 存放的是 专业核查的工单id
                    resParams.put("rollbackDesc", remark); // 回滚原因
                    resParams.put("tacheId",MapUtils.getString(tmp,"ID"));
                    resMap = businessRollbackServiceIntf.resRollBack(resParams);
                    if(!MapUtils.getBoolean(resMap,"success")){
                        return  resMap;
                    }
                }
            }
            List<FlowRollBackReasonDTO> flowRollBackReasonDTOs = flowAction.queryRollBackReasons(woId);
            FlowRollBackReasonDTO flowRollBackReasonDTO = null;
            for (int i = 0; i < flowRollBackReasonDTOs.size(); i++) {
                FlowRollBackReasonDTO _flowRollBackReasonDTO = flowRollBackReasonDTOs.get(i);
                FlowTacheDTO flowTacheDTO = _flowRollBackReasonDTO.getToTache();
                String _toTacheId = flowTacheDTO.getId();
                if (toSpecialTache.equals(_toTacheId)) {
                    flowRollBackReasonDTO = _flowRollBackReasonDTO;
                }
            }
            flowAction.rollBackWo(operStaffId, woId, flowRollBackReasonDTO, remark);
            Map<String, Object> logDataMap = new HashMap<String, Object>();
            logDataMap.put("woId", woId);
            logDataMap.put("orderId", orderId);
            logDataMap.put("remark", remark);
            logDataMap.put("tacheId", tacheId);
            logDataMap.put("operType", OrderTrackOperType.OPER_TYPE_5);
            logDataMap.put("action", "退单");
            logDataMap.put("trackMessage", "[退单]");
            tacheDealLogIntf.addTrackLog(logDataMap);
            resMap.put("success", true);
            resMap.put("message", "退单成功！");
        }catch (Exception e){
            e.printStackTrace();
            resMap.put("success", false);
            resMap.put("message", "退单失败！");
        }
        return resMap;
    }

    @Override
    public void invalidAllSpecialCheck(Map<String, Object> invalidParam) throws Exception {
        String orderId = MapUtils.getString(invalidParam, "orderId");
        String operStaffId = MapUtils.getString(invalidParam, "operStaffId");
        List<String> allSpecialCheckWoOrderIds = checkOrderDao.qryALLSpecialCheckWoOrder(orderId);
        if(!ListUtil.isEmpty(allSpecialCheckWoOrderIds)){
            flowAction.disableWo(operStaffId, allSpecialCheckWoOrderIds);
        }

    }

    @Override
    public void invalidLocalCheck(Map<String, Object> invalidParam) throws Exception {
        String orderId = MapUtils.getString(invalidParam, "orderId");
        List<Map<String, Object>> toLocalCheckOrderList = checkOrderDao.qryToLocalCheck(orderId);
        if (!ListUtil.isEmpty(toLocalCheckOrderList)){
            for (Map<String, Object> toLocalCheckOrder : toLocalCheckOrderList){
                toLocalCheckOrder.put("woState", OrderTrackOperType.WO_ORDER_STATE_5);
                toLocalCheckOrder.put("orderState", OrderTrackOperType.ORDER_STATE_5);
                toLocalCheckOrder.put("state", "10X");
                checkOrderDao.updateWoOrderStateByWoId(toLocalCheckOrder);
                checkOrderDao.updateOrderStateByOrderId(toLocalCheckOrder);
                checkOrderDao.updateToLocalStateById(toLocalCheckOrder);
            }
        }
    }

    @Override
    public List<String> qryAllSpecialCheckWoOrder(String orderId) {
        return checkOrderDao.qryALLSpecialCheckWoOrder(orderId);
    }

    @Override
    public Map<String, Object> qrySpecialData(String orderId) {
        Map<String, Object> resMap = new HashMap<>();
        try{
            resMap.put("specialData", checkOrderDao.qrySpecialData(orderId));
            resMap.put("success", true);
        }catch (Exception e){
            resMap.put("success", false);
            resMap.put("message", "核查专业查询失败！");
        }
        return resMap;
    }
    @Override
    public String qryMaxWoId(String orderId) {
        return checkOrderDao.qryMaxWoId(orderId);
    }

    @Override
    public Map<String, Object> qryToLocalCheckData(String orderId) {
        Map<String, Object> resMap = new HashMap<>();
        try{
            resMap.put("specialData", checkOrderDao.qryToLocalCheckData(orderId));
            resMap.put("success", true);
        }catch (Exception e){
            resMap.put("success", false);
            resMap.put("message", "下发本地核查区域查询失败！");
        }
        return resMap;
    }

    @Override
    public Map<String, Object> querySystemInfo(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap<>();
        try{
            Map<String, Object> map = checkOrderDao.querySystemInfo(param);
            resMap.put("success", true);
            resMap.put("data", map);
        }catch(Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>根据电路ID查询系统源信息发生异常:{}", e.getMessage());
            resMap.put("success", false);
            resMap.put("msg", "查询系统源信息发生异常:" + e.getMessage());
        }
        return resMap;
    }
}
