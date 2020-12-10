package com.zres.project.localnet.portal.cloudNetworkFlow.tacheWoOrder;

import com.zres.project.localnet.portal.common.CommonWoOrderDealServiceIntf;
import com.zres.project.localnet.portal.common.DealTacheWoOrderIntf;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.logInfo.service.TacheDealLogIntf;
import com.zres.project.localnet.portal.util.OrderTrackOperType;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 核查汇总环节处理
 *
 * @author caomm on 2020/11/11
 */
@Service
public class VerificationSumTache implements DealTacheWoOrderIntf {
    private static final Logger logger = LoggerFactory.getLogger(VerificationSumTache.class);
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private TacheDealLogIntf tacheDealLogIntf;
    @Autowired
    private CommonWoOrderDealServiceIntf commonWoOrderDealServiceIntf;
    @Override
    public Map<String, Object> tacheDoSomething(Map<String, Object> param) throws Exception {
        Map<String, Object> resMap = new HashMap<>();
        try{
            List<Map<String, Object>> circuitLiist = (List<Map<String, Object>>) MapUtils.getObject(param, "circuitData");
            if (CollectionUtils.isNotEmpty(circuitLiist)){
                String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
                Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(operStaffId));
                for (Map<String, Object> map : circuitLiist){
                    String woId = MapUtils.getString(map, "WO_ID");
                    String orderId = MapUtils.getString(map, "ORDER_ID");
                    String tacheId = MapUtils.getString(map, "TACHE_ID");
                    //先进行回单
                    Map<String, Object> logDataMap = new HashMap<String, Object>();
                    logDataMap.put("woId", woId);
                    logDataMap.put("orderId", orderId);
                    logDataMap.put("remark", MapUtils.getString(param, "remark", ""));
                    logDataMap.put("tacheId", tacheId);
                    logDataMap.put("operStaffInfoMap", operStaffInfoMap);
                    logDataMap.put("operType", OrderTrackOperType.OPER_TYPE_4);
                    logDataMap.put("action", "回单");
                    logDataMap.put("trackMessage", "[ 回单 ]");
                    tacheDealLogIntf.addTrackLog(logDataMap);
                    Map<String, Object> complateMap = new HashMap<>();
                    complateMap.put("operStaffId", operStaffId);
                    complateMap.put("woId", woId);
                    //线条参数
                    Map<String, Object> operAttrsValMap = new HashMap<>();
                    operAttrsValMap.put("if_investReckon", MapUtils.getString(param, "isInvestEst"));
                    complateMap.put("operAttrsVal", operAttrsValMap);
                    Map<String, Object> retMap = commonWoOrderDealServiceIntf.complateWoService(complateMap);
                    //TODO
                    //向B域进行核查反馈
                    if (MapUtils.getBoolean(retMap, "success")){
                        logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>云组网核查汇总回单成功:{}", woId);
                    }
                }
                resMap.put("success", true);
                resMap.put("message", "回单成功！");
            }
        }catch(Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>核查汇总回单发生异常:{}", e.getMessage());
            resMap.put("success", false);
            resMap.put("message", "核查汇总回单发生异常:" + e.getMessage());
        }
        return resMap;
    }
}