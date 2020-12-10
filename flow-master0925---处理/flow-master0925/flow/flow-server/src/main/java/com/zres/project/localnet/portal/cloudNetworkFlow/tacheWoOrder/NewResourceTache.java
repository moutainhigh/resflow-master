package com.zres.project.localnet.portal.cloudNetworkFlow.tacheWoOrder;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.common.CommonWoOrderDealServiceIntf;
import com.zres.project.localnet.portal.common.DealTacheWoOrderIntf;
import com.zres.project.localnet.portal.common.ToOrderDealTacheWoOrderIntf;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.logInfo.service.TacheDealLogIntf;
import com.zres.project.localnet.portal.util.OrderTrackOperType;

import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;

/**
 * @Classname NewResourceTache
 * @Description 新建资源录入
 * @Author guanzhao
 * @Date 2020/10/30 18:03
 */
@Service
public class NewResourceTache implements DealTacheWoOrderIntf {

    Logger logger = LoggerFactory.getLogger(NewResourceTache.class);
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private TacheDealLogIntf tacheDealLogIntf;
    @Autowired
    private CommonWoOrderDealServiceIntf commonWoOrderDealServiceIntf;

    /*
     * 新建资源录入环节逻辑
     *
     * @author guanzhao
     * @date 2020/10/30
     *
     */
    @Override
    public Map<String, Object> tacheDoSomething(Map<String, Object> tacheDoSomeMap) throws Exception {
        logger.info("----------页面提交-----------新建资源录入环节-----------------------");
        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(operStaffId));
        Map<String, String> operAttrs = MapUtils.getMap(tacheDoSomeMap, "operAttrs");
        String remark = MapUtils.getString(operAttrs, "remark");
        //String buttonAction = MapUtils.getString(tacheDoSomeMap, "buttonAction"); //按钮动作
        Map<String, Object> circuitDataMap = MapUtils.getMap(tacheDoSomeMap, "circuitDataMap");
        String woId = MapUtils.getString(circuitDataMap, "WO_ID");
        String orderId = MapUtils.getString(circuitDataMap, "ORDER_ID");
        String tacheId = MapUtils.getString(circuitDataMap, "TACHE_ID");
        Map<String, String> operAttrsValMap = new HashMap<>(); //线条参数
        operAttrsValMap.put("if_circuit_dispatch", "0");
        Map<String, Object> logDataMap = new HashMap<String, Object>();
        logDataMap.put("woId", woId);
        logDataMap.put("orderId", orderId);
        logDataMap.put("remark", remark == null ? "" : remark);
        logDataMap.put("tacheId", tacheId);
        logDataMap.put("operStaffInfoMap", operStaffInfoMap);
        logDataMap.put("operType", OrderTrackOperType.OPER_TYPE_4);
        logDataMap.put("action", "回单");
        logDataMap.put("trackMessage", "[ 回单 ]");
        tacheDealLogIntf.addTrackLog(logDataMap); //入库操作日志
        Map<String, Object> complateMap = new HashMap<>();
        complateMap.put("operAttrsVal", operAttrsValMap);
        complateMap.put("operStaffId", operStaffId);
        complateMap.put("woId", woId);
        return commonWoOrderDealServiceIntf.complateWoService(complateMap);
    }
}
