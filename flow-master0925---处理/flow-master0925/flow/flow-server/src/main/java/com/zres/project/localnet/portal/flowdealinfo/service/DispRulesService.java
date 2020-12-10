package com.zres.project.localnet.portal.flowdealinfo.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.DispObjDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderQrySecondaryDao;


import net.sf.json.JSONArray;

import com.ztesoft.res.frame.core.util.ListUtil;
import com.ztesoft.res.frame.flow.spec.tache.service.TacheService;
import com.ztesoft.res.frame.flow.spec.tache.vo.Tache;

@Service
public class DispRulesService {

    private static final Logger log = LoggerFactory.getLogger(DispRulesService.class);

    @Autowired
    private DispObjDao dispObjDao;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private OrderQrySecondaryDao orderQrySecondaryDao;
    @Autowired
    private OrderDetailsServiceIntf orderDetailsServiceIntf;
    @Autowired
    private TacheService tacheService;

    private enum SrcTacheStaff {DISP_OBJ_ID, DEAL_USER_ID, COMP_USER_ID}

    private enum TacheCode {SEC_SOURCE_DISPATCH, SEC_SOURCE_DISPATCH_2}

    /**
     * 根据配置的环节和环节的人员类型，获取派发对象
     * 派发人，签收人，提交人
     * @param orderId
     * @param tacheCode
     * @param tacheStaffType
     * @return
     */
    public String getDispObjByPrevTacheInfo(String orderId, String tacheCode, String tacheStaffType){
        String dispObjInfo = "";
        Map<String, Object> operActTypeMap = orderDealDao.getOperActType(orderId);
        String productType = MapUtils.getString(operActTypeMap, "PRODUCT_TYPE");
        List<String> allOrderIdList = this.getCircuitAllOrderId(orderId);
        List<Tache> sameNameTache = tacheService.qrySameNameByCode(tacheCode);
        for (int i = 0; i < sameNameTache.size(); i++) {
            //去除查到的二干资源分配环节主流程code
            Tache tacheInfo = sameNameTache.get(i);
            if (TacheCode.SEC_SOURCE_DISPATCH.toString().equals(tacheInfo.getTacheCode())
                    || TacheCode.SEC_SOURCE_DISPATCH_2.toString().equals(tacheInfo.getTacheCode())) {
                sameNameTache.remove(tacheInfo);
            }
        }
        Map<String, Object> qryTacheWoOrderParams = new HashMap<>();
        qryTacheWoOrderParams.put("allOrderIdList", allOrderIdList);
        qryTacheWoOrderParams.put("sameNameTache", sameNameTache);
        qryTacheWoOrderParams.put("productType", productType);
        log.info("---allOrderIdList:" + allOrderIdList.toString()
                + "---sameNameTache:" + JSONArray.fromObject(sameNameTache).toString() + "---productType:" + productType);
        List<Map<String, Object>> tacheWoOrderList = dispObjDao.qryCircuitTacheWoOrder(qryTacheWoOrderParams);
        log.info("---tacheWoOrderList:" + tacheWoOrderList.toString());
        if (!ListUtil.isEmpty(tacheWoOrderList)){
            if (tacheWoOrderList.size() > 1){
                tacheWoOrderList = this.getCorrectTacheWoOrderInfo(orderId, allOrderIdList, sameNameTache, tacheWoOrderList);
                log.info("---tacheWoOrderList:" + tacheWoOrderList.toString());
            }
            if (!ListUtil.isEmpty(tacheWoOrderList)){
                dispObjInfo = this.getDispObj(tacheStaffType, tacheWoOrderList);
            }
        }
        return dispObjInfo;
    }

    /**
     * 查询电路所有的orderid
     * @param orderId
     * @return
     */
    private List<String> getCircuitAllOrderId(String orderId){
        /**
         * 1，查询是否有父流程；
         * 2，查询是否由二干下发；
         *      是：查询二干主流程orderid，查询二干子流程orderid
         */
        List<String> allOrderIdList = new ArrayList<>();
        allOrderIdList.add(orderId);
        Map parentOrder = orderDealDao.getParentOrder(orderId);
        String parentOrderId = MapUtils.getString(parentOrder, "PARENTORDERID");
        if (!StringUtils.isEmpty(parentOrderId)){
            allOrderIdList.add(parentOrderId);
            orderId = parentOrderId;
        }
        Map<String, Object> ifFromSecondaryMap = orderQrySecondaryDao.qryIfFromSecondary(orderId);
        if(MapUtils.isNotEmpty(ifFromSecondaryMap)){
            String orderIdSec = MapUtils.getString(ifFromSecondaryMap,"PARENT_ORDER_ID");
            allOrderIdList.add(orderIdSec);
            List<Map<String, Object>> childFlowList = orderDealDao.queryAlreadyChildFlowList(orderIdSec);
            for (Map<String, Object> childFlow : childFlowList){
                allOrderIdList.add(MapUtils.getString(childFlow, "ORDER_ID"));
            }
        }
        return allOrderIdList;
    }

    /**
     * 添加过滤条件过滤 专业和区域  查询出来的工单
     * @param orderId
     * @param allOrderIdList
     * @param sameNameTache
     * @param tacheWoOrderList
     * @return
     */
    private List<Map<String, Object>> getCorrectTacheWoOrderInfo(String orderId,
                                                                 List<String> allOrderIdList,
                                                                 List<Tache> sameNameTache,
                                                                 List<Map<String, Object>> tacheWoOrderList){
        Map parentOrder = orderDealDao.getParentOrder(orderId);
        String parentOrderId = MapUtils.getString(parentOrder, "PARENTORDERID");
        Map<String, Object> operActTypeMap = orderDealDao.getOperActType(orderId);
        String regionId = MapUtils.getString(operActTypeMap, "REGION_ID");
        String productType = MapUtils.getString(operActTypeMap, "PRODUCT_TYPE");
        String actType = MapUtils.getString(operActTypeMap, "ACT_TYPE");
        String specialtyCode = MapUtils.getString(operActTypeMap, "SPECIALTY_CODE");
        Map<String, Object> qryTacheWoOrderParams = new HashMap<String, Object>();
        qryTacheWoOrderParams.put("allOrderIdList", allOrderIdList);
        qryTacheWoOrderParams.put("sameNameTache", sameNameTache);
        qryTacheWoOrderParams.put("productType", productType);
        qryTacheWoOrderParams.put("actType", actType);
        Map<String, Object> tacheWoOrder = tacheWoOrderList.get(0);
        String orderIdDisp = MapUtils.getString(tacheWoOrder, "ORDER_ID");
        String parentOrderIdDisp = MapUtils.getString(tacheWoOrder, "PARENT_ORDER_ID");
        if (orderId.equals(orderIdDisp)){ //同一个流程
            qryTacheWoOrderParams.put("regionId", regionId);
            qryTacheWoOrderParams.put("specialtyCode", specialtyCode);
        }else {
            boolean ifFromSend = orderDetailsServiceIntf.ifFromSecond(orderId); //是否二干下发
            if (ifFromSend){
                //是二干下发
                if (!StringUtils.isEmpty(parentOrderId) && !StringUtils.isEmpty(parentOrderIdDisp)){
                    qryTacheWoOrderParams.put("specialtyCode", specialtyCode);
                }
            }else {
                //非二干下发
                if (!StringUtils.isEmpty(parentOrderId)){
                    if (!parentOrderId.equals(orderIdDisp)) {
                        qryTacheWoOrderParams.put("specialtyCode", specialtyCode);
                    }
                }
                if (!StringUtils.isEmpty(parentOrderIdDisp)){
                    if (!parentOrderIdDisp.equals(orderId)) {
                        qryTacheWoOrderParams.put("specialtyCode", specialtyCode);
                    }
                }
            }
        }
        List<Map<String, Object>> tacheWoOrderListNew = dispObjDao.qryCircuitTacheWoOrder(qryTacheWoOrderParams);
        return tacheWoOrderListNew;
    }

    /**
     * 获取派发对象
     * @param tacheStaffType
     * @param tacheWoOrderList
     * @return
     */
    public String getDispObj(String tacheStaffType, List<Map<String, Object>> tacheWoOrderList) {
        String dispObjInfo;
        Map<String, Object> tacheWoOrder = tacheWoOrderList.get(0);
        String dispObjId = MapUtils.getString(tacheWoOrder, tacheStaffType);
        log.info("---tacheStaffType:" + tacheStaffType + "---dispObjId:" + dispObjId);
        if (SrcTacheStaff.DISP_OBJ_ID.toString().equals(tacheStaffType)){
            String dispObjType = MapUtils.getString(tacheWoOrder, "DISP_OBJ_TYE");
            dispObjInfo = dispObjType + "_J!G@F_" + dispObjId ;
        }else {
            dispObjInfo = "260000003_J!G@F_" + dispObjId ;
        }
        log.info("---dispObjInfo:" + dispObjInfo);
        return dispObjInfo;
    }

}