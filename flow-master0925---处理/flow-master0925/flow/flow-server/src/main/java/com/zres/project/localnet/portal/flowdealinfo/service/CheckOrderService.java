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
import org.springframework.util.StringUtils;

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
            if(!MapUtils.getBoolean(resMap,"success")){
                return  resMap;
            }
            // 删除专业核查的反馈信息
            checkFeedbackDao.deleteInfoByWoId(woId);
            List<String> woIds = new ArrayList<String>();
            woIds.add(woId);
            flowAction.disableWo(operStaffId, woIds);
            Map<String, Object> checkDispatchWoOrder = checkOrderDao.qryCheckDispatchWoOrder(orderId, BasicCode.CHECK_DISPATCH);
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
            if (OrderTrackOperType.DISPATCH_CHECK.equals(toTacheflag)){
                checkFeedbackDao.deleteInfoBySrvOrdId(MapUtils.getString(backOrderParam, "srvOrdId"));
                toSpecialTache = BasicCode.CHECK_DISPATCH;
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
            }
            if(!CollectionUtils.isEmpty(list)){
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
    public List<String> qryAllSpecialCheckWoOrder(String orderId) {
        return checkOrderDao.qryALLSpecialCheckWoOrder(orderId);
    }

    @Override
    public String qryMaxWoId(String orderId) {
        return checkOrderDao.qryMaxWoId(orderId);
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
    public void rollBackSecondCheck(String orderId) throws Exception {
        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        Map<String, Object> secLocalCheckTache = checkOrderDao.qrySecLocalCheckTache(orderId);
        if (MapUtils.isNotEmpty(secLocalCheckTache)){
            String woId = MapUtils.getString(secLocalCheckTache, "WO_ID");
            String orderIdSec = MapUtils.getString(secLocalCheckTache, "ORDER_ID");
            List<String> woIds = new ArrayList<String>();
            woIds.add(woId);
            flowAction.disableWo(operStaffId, woIds);
            Map<String, Object> checkDispatchWoOrder = checkOrderDao.qryCheckDispatchWoOrder(orderIdSec, BasicCode.CHECK_SCHEDULING);
            String checkDispatchWoId = MapUtils.getString(checkDispatchWoOrder, "WO_ID");
            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put("woId", checkDispatchWoId);
            updateMap.put("woState", OrderTrackOperType.WO_ORDER_STATE_2);
            checkOrderDao.updateWoOrderStateByWoId(updateMap);
        }
    }

    @Override
    public int qrySpecialtyCheckDoing(String orderId, String tacheCode, String areaId) {
        return checkOrderDao.qrySpecialtyCheckDoing(orderId, tacheCode, areaId);
    }

    @Override
    public Map<String, Object> queryProvinceName(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap<>();
        //如果MV产品只有一端查询A端省份查不到就去查询Z端省份
        if ("10000008".equals(MapUtils.getString(param, "serviceId")) || "10000011".equals(MapUtils.getString(param, "serviceId"))){
            param.put("attrCode", "CON0101");
            resMap = checkOrderDao.queryProvinceName(param);
            if (org.springframework.util.CollectionUtils.isEmpty(resMap)){
                param.put("attrCode", "CON0102");
                resMap = checkOrderDao.queryProvinceName(param);
            }
        }else{
            param.put("attrCode", "CON0101");
            resMap = checkOrderDao.queryProvinceName(param);
        }
        return resMap;
    }

    @Override
    public Map<String, Object> queryProvinceConf(Map<String, Object> param) {
        return checkOrderDao.queryProvinceConf();
    }

    @Override
    public Map<String, Object> saveCircuitCodeBySrvOrdId(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap<>();
        try{
            //先查询电路属性表中是否有电路编号的记录
            Map<String, Object> map = new HashMap<>();
            map.put("srvOrdId", MapUtils.getString(param, "srvOrdId"));
            map.put("attrCode", "20000064");
            Map<String, Object> circuitCodeMap = checkOrderDao.queryCircuitCodeInfo(map);
            if (!MapUtils.isEmpty(circuitCodeMap)){
                //查询电路属性表中是否有原电路编号的记录
                map.put("attrCode", "20000065");
                Map<String, Object> oldCircuitCodeMap = checkOrderDao.queryCircuitCodeInfo(map);
                if (!MapUtils.isEmpty(oldCircuitCodeMap)){
                    //有则更新电路编号到原电路编号中
                    map.put("attrValue", MapUtils.getString(circuitCodeMap, "ATTR_VALUE"));
                    if (!StringUtils.isEmpty(MapUtils.getString(circuitCodeMap, "ATTR_VALUE"))){
                        //如果电路编号的值为空，就不需要更新到原电路编号中
                        checkOrderDao.updateCircuitCode(map);
                    }
                }else{
                    //没有则插入
                    Map<String, Object> paramMap = new HashMap<>();
                    paramMap.put("srvOrdId", MapUtils.getString(param, "srvOrdId"));
                    paramMap.put("attrCode", "20000065");
                    paramMap.put("attrAction", "0");
                    paramMap.put("attrValue", MapUtils.getString(circuitCodeMap, "ATTR_VALUE"));
                    paramMap.put("attrName", "原电路编号");
                    paramMap.put("attrValueName", "oldCircuitCode");
                    checkOrderDao.insertCircuitInfo(paramMap);
                }
                //更新手动编辑的电路编号到电路编号记录中
                param.put("attrCode", "20000064");
                checkOrderDao.updateCircuitCode(param);
            }else {
                //如果电路属性信息表中没有电路编号的记录，怎插入一条记录
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("srvOrdId", MapUtils.getString(param, "srvOrdId"));
                paramMap.put("attrCode", "20000064");
                paramMap.put("attrAction", "0");
                paramMap.put("attrValue", MapUtils.getString(param, "attrValue"));
                paramMap.put("attrName", "电路编号");
                paramMap.put("attrValueName", "circuitCode");
                checkOrderDao.insertCircuitInfo(paramMap);
            }
            resMap.put("success", true);
            resMap.put("msg", "电路编号保存成功！");
        }catch(Exception e){
            e.printStackTrace();
            resMap.put("success", false);
            resMap.put("msg", e.getMessage());
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>保存电路编号发生异常:{}", e.getMessage());
        }
        return resMap;
    }

    @Override
    public Map<String, Object> queryProvinceAutoConf(Map<String, Object> param) {
        return checkOrderDao.queryProvinceAutoConf( MapUtils.getString(param, "areaId"));
    }
}
