package com.zres.project.localnet.portal.flowdealinfo.tacheWoOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.axis.utils.StringUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.flowdealinfo.service.CommonMethodDealWoOrderServiceInf;
import com.zres.project.localnet.portal.flowdealinfo.service.DealTacheWoOrderIntf;
import com.zres.project.localnet.portal.logInfo.service.TacheDealLogIntf;
import com.zres.project.localnet.portal.resourceInitiate.data.dao.ResourceInitiateDao;
import com.zres.project.localnet.portal.util.OrderTrackOperType;

import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;

/**
 * 各专业资源补录环节--主流程
 */
@Service
public class AllSpecialtyResourceSuppementTache implements DealTacheWoOrderIntf {

    Logger logger = LoggerFactory.getLogger(AllSpecialtyResourceSuppementTache.class);

    @Autowired
    private CommonMethodDealWoOrderServiceInf commonMethodDealWoOrderServiceInf;
    @Autowired
    private ResourceInitiateDao resourceInitiateDao;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private TacheDealLogIntf tacheDealLogIntf;

    @Override
    public Map<String, Object> tacheDoSomething(Map<String, Object> tacheDoSomeMap) throws Exception {
        logger.info("-------------到单------各专业资源补录环节----------------------------------");
        String orderId = MapUtils.getString(tacheDoSomeMap, "orderId");
        String woId = MapUtils.getString(tacheDoSomeMap, "woId");
        String tacheId = MapUtils.getString(tacheDoSomeMap, "tacheId");
        //String tacheCode = MapUtils.getString(tacheDoSomeMap, "tacheCode");
        Map<String, Object> resSupplementConfigDataMap = resourceInitiateDao.qryResSupplementConfigData(orderId);
        //String specialtyStr = MapUtils.getString(resSupplementConfigDataMap, "SPECIALTY_CONFIG");
        String specialtyAreaStr = MapUtils.getString(resSupplementConfigDataMap, "LOCAL_CONFIG");
        if (!StringUtils.isEmpty(specialtyAreaStr)){
            tacheDoSomeMap.put("childConfigs", specialtyAreaStr);
            //tacheDoSomeMap.put("startFlag", tacheCode);
            tacheDoSomeMap.put("ordPsId", BasicCode.LOCAL_RESOURCE_SUPPLEMENT_CHILDFLOW);
            tacheDoSomeMap.put("parentOrderCode", BasicCode.RESSUP);
            Map<String, Object> childResMap = commonMethodDealWoOrderServiceInf.commonCreateChildOrder(tacheDoSomeMap);
            Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.parseInt(ThreadLocalInfoHolder.getLoginUser().getUserId()));
            Map<String, Object> logDataMap = new HashMap<String, Object>();
            logDataMap.put("woId", woId);
            logDataMap.put("orderId", orderId);
            logDataMap.put("remark", "发起各专业资源补录");
            logDataMap.put("tacheId", tacheId);
            logDataMap.put("operType", OrderTrackOperType.OPER_TYPE_10);
            logDataMap.put("operStaffInfoMap", operStaffInfoMap);
            logDataMap.put("action", "[发起各专业资源补录]");
            logDataMap.put("trackMessage", "[发起各专业资源补录][各专业有]" + MapUtils.getString(childResMap, "configList"));
            tacheDealLogIntf.addTrackLog(logDataMap);
        }
        return null;
    }

}
