package com.zres.project.localnet.portal.flowdealinfo.tacheWoOrder;

import com.zres.project.localnet.portal.flowdealinfo.service.CommonMethodDealWoOrderServiceInf;
import com.zres.project.localnet.portal.flowdealinfo.service.DealTacheWoOrderIntf;
import com.zres.project.localnet.portal.util.OrderTrackOperType;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 带线条参数的通用环节处理
 */
@Service
public class CommonWithOperAttrTache implements DealTacheWoOrderIntf {

    Logger logger = LoggerFactory.getLogger(CommonWithOperAttrTache.class);

    @Autowired
    private CommonMethodDealWoOrderServiceInf commonMethodDealWoOrderServiceInf;

    @Override
    public Map<String, Object> tacheDoSomething(Map<String, Object> tacheDoSomeMap) throws Exception {
        logger.info(">>>>>>>>>>>>>>>>进入通用环节处理......................");
        String operStaffId = "";
        if(tacheDoSomeMap.keySet().contains("operStaffId")){
            operStaffId = MapUtils.getString(tacheDoSomeMap,"operStaffId");
        }else{
            operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        }

        String woId = MapUtils.getString(tacheDoSomeMap, "woId");
        String orderId = MapUtils.getString(tacheDoSomeMap, "orderId");
        String tacheId = MapUtils.getString(tacheDoSomeMap, "tacheId");
        String remark = MapUtils.getString(tacheDoSomeMap, "remark");

        // 线条判断参数 operAttrs 获取页面传过来的线条参数
        Map<String, String> operAttrsValMap = MapUtils.getMap(tacheDoSomeMap, "operAttrsVal");
        String action = "回单";
        String operType = OrderTrackOperType.OPER_TYPE_4;
        if(tacheDoSomeMap.containsKey("btnFlag")&&"rollBackOrder".equals(MapUtils.getString(tacheDoSomeMap,"btnFlag"))){
            action = "退单";
            operType = OrderTrackOperType.OPER_TYPE_5;
        }

        Map<String, Object> complateMap = new HashMap<>();
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
