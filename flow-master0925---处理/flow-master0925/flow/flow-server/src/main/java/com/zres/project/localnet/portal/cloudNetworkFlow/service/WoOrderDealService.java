package com.zres.project.localnet.portal.cloudNetworkFlow.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.cloudNetworkFlow.WoOrderDealServiceIntf;
import com.zres.project.localnet.portal.cloudNetworkFlow.dao.WoOrderDealDao;
import com.zres.project.localnet.portal.cloudNetworkFlow.entry.CloudTacheWoOrder;
import com.zres.project.localnet.portal.common.CommonWoOrderDealServiceIntf;
import com.zres.project.localnet.portal.common.DealTacheWoOrderIntf;
import com.zres.project.localnet.portal.common.dao.CommonDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.util.SpringContextHolderUtil;

import com.ztesoft.res.frame.flow.xpdl.model.TacheDto;

@Service
public class WoOrderDealService implements WoOrderDealServiceIntf {

    Logger logger = LoggerFactory.getLogger(WoOrderDealService.class);

    @Autowired
    private CommonWoOrderDealServiceIntf commonWoOrderDealServiceIntf;
    @Autowired
    private CommonDealDao commonDealDao;
    @Autowired
    private WoOrderDealDao woOrderDealDao;
    @Autowired
    private OrderDealDao orderDealDao;

    @Override
    public Map<String, Object> createOrderCloud(Map<String, Object> createMap) throws Exception {
        logger.info("------------------------启流程---------------------------");
        return commonWoOrderDealServiceIntf.createOrderService(createMap);
    }

    @Override
    public Map<String, Object> submitWoOrderCloud(Map<String, Object> submitMap) throws Exception {
        logger.info("---------云组网---------进入工单处理方法--------------------");
        int tacheId = MapUtils.getIntValue(submitMap, "tacheId");
        TacheDto tacheDto = commonDealDao.qryTacheDto(tacheId);
        CloudTacheWoOrder cloudTacheWoOrder = new CloudTacheWoOrder(tacheDto, submitMap);
        String beanName = cloudTacheWoOrder.getBeanNameByTacheCode();
        DealTacheWoOrderIntf dealTacheWoOrder = SpringContextHolderUtil.getBean(beanName);
        submitMap.put("tacheCode", tacheDto.getTacheCode());
        return dealTacheWoOrder.tacheDoSomething(submitMap);
    }

    @Override
    public Map<String, Object> getCloudTacheButton(Map<String, Object> params) throws Exception {
        String tacheId = MapUtils.getString(params, "tacheId");
        String btnInfo = MapUtils.getString(params, "btnInfo");
        String psId = MapUtils.getString(params, "psId");
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("tacheId", tacheId == null ? null : tacheId);
        paramsMap.put("psId", psId == null ? null : psId);
        paramsMap.put("btnInfo", btnInfo);
        Map<String, Object> resMap = new HashMap<>();
        try {
            List<Map<String, String>> btnList = woOrderDealDao.getCloudTacheButton(paramsMap);
            resMap.put("success", true);
            resMap.put("resButtons", btnList);
        }
        catch (Exception e) {
            throw e;
        }
        return resMap;
    }

    @Override
    public Map<String, Object> qryCompanyAreaId(Map<String, Object> params) throws Exception {
        Map<String, Object> resMap = new HashMap<>();
        try {
            params.put("parentId", "1");
            Map<String, String> provinceParentMap = woOrderDealDao.qryCityAreaId(params);
            String parentdeptId = MapUtils.getString(provinceParentMap, "ORG_ID");
            params.put("parentId", parentdeptId);
            Map<String, String> provinceMap = woOrderDealDao.qryCityAreaId(params);
            String deptId = MapUtils.getString(provinceMap, "ORG_ID");
            Map<String, Object> qryMap = new HashMap<>();
            qryMap.put("deptId", deptId);
            List<Map<String, String>> areaIdList = woOrderDealDao.qryCompanyAreaId(qryMap);
            resMap.put("success", true);
            resMap.put("areaIdList", areaIdList);
        }
        catch (Exception e) {
            throw e;
        }
        return resMap;
    }

    @Override
    public Map<String, Object> qryMcpeInstallBackOrderData(Map<String, Object> params) throws Exception {
        Map<String, Object> resMap = new HashMap<>();
        try{
            String orderId = MapUtils.getString(params, "orderId");
            String woId = MapUtils.getString(params, "woId");
            String privTacheCode = woOrderDealDao.qryPrivTacheCode(woId);
            Map<String, String> qryParams = new HashMap<>();
            qryParams.put("tacheCode", privTacheCode);
            qryParams.put("orderId", orderId);
            List<Map<String, String>> mcpeInstallBackOrderData = woOrderDealDao.qryMcpeInstallBackOrderData(qryParams);
            resMap.put("success", true);
            resMap.put("dataList", mcpeInstallBackOrderData);
        } catch (Exception e) {
            throw e;
        }
        return resMap;
    }

}
