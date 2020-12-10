package com.zres.project.localnet.portal.flowdealinfo.tacheWoOrder;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.flowdealinfo.service.CommonMethodDealWoOrderServiceInf;
import com.zres.project.localnet.portal.flowdealinfo.service.DealTacheWoOrderIntf;
import com.zres.project.localnet.portal.util.OrderTrackOperType;

import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;

/**
 * 通用环节处理
 */
@Service
public class CommonTache implements DealTacheWoOrderIntf {

    Logger logger = LoggerFactory.getLogger(CommonTache.class);

    @Autowired
    private CommonMethodDealWoOrderServiceInf commonMethodDealWoOrderServiceInf;

    @Override
    public Map<String, Object> tacheDoSomething(Map<String, Object> tacheDoSomeMap) {
        logger.info(">>>>>>>>>>>>>>>>进入通用环节处理......................");
        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        Map<String, Object> complateMap = new HashMap<>();
        // 线条判断参数 operAttrs 获取页面传过来的线条参数
        Map<String, String> operAttrsValMap = MapUtils.getMap(tacheDoSomeMap, "operAttrsVal");
        String action = "回单";
        String operType = OrderTrackOperType.OPER_TYPE_4;
        // 获取环节操作信息 调单信息
        Map<String, Object> tacheOperInfo = MapUtils.getMap(tacheDoSomeMap, "tacheOperInfo");
        String remark = MapUtils.getString(tacheOperInfo, "remark");
        Map<String, Object> circuitDataMap = MapUtils.getMap(tacheDoSomeMap, "circuitDataMap");
        String woId = MapUtils.getString(circuitDataMap, "WO_ID");
        String orderId = MapUtils.getString(circuitDataMap, "ORDER_ID");
        String tacheId = MapUtils.getString(circuitDataMap, "TACHE_ID");
        //String psId = MapUtils.getString(circuitDataMap, "PS_ID");
        //String srvOrdId = MapUtils.getString(circuitDataMap, "SRV_ORD_ID");
        //String relateInfoId = MapUtils.getString(circuitDataMap, "RELATE_INFO_ID");
        complateMap.put("operAttrsVal", operAttrsValMap);
        complateMap.put("remark",  remark==null?"":remark);
        complateMap.put("operStaffId", operStaffId);
        complateMap.put("orderId", orderId);
        complateMap.put("woId", woId);
        complateMap.put("action", action);
        complateMap.put("operType", operType);
        complateMap.put("tacheId", tacheId);
        return commonMethodDealWoOrderServiceInf.commonComplateWo(complateMap);
    }
}
