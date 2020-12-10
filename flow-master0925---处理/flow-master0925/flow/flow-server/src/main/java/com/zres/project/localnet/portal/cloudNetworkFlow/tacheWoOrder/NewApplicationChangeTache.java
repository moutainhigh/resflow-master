package com.zres.project.localnet.portal.cloudNetworkFlow.tacheWoOrder;

import com.zres.project.localnet.portal.cloudNetworkFlow.dao.TacheWoOrderDao;
import com.zres.project.localnet.portal.common.CommonWoOrderDealServiceIntf;
import com.zres.project.localnet.portal.common.ToOrderDealTacheWoOrderIntf;
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
 * 核查子流程等待环节
 *
 * @author caomm on 2020/11/9
 */
@Service
public class NewApplicationChangeTache implements ToOrderDealTacheWoOrderIntf {
    private static final Logger logger = LoggerFactory.getLogger(NewApplicationChangeTache.class);
    @Autowired
    private TacheWoOrderDao tacheWoOrderDao;
    @Autowired
    private CommonWoOrderDealServiceIntf commonWoOrderDealServiceIntf;
    @Override
    public Map<String, Object> toOrderTacheDoSomething(Map<String, Object> param) throws Exception {
        Map<String, Object> resMap = new HashMap<>();
        try{
            //监听云组网本地跨域变更-下联端口流程，新建申请单环节到单
            String woId = MapUtils.getString(param, "woId");

            Map<String, Object> operAttrsVal = new HashMap<>();
            //默认是否原业务拆除：是
            operAttrsVal.put("isBusiDismantle",true);
            /* 查询B侧工单是否指定了端口号和VLAN号
            * 10000462	A下联端口; 10000463	Z下联端口
              10000772	A端VLAN号;10000773	Z端VLAN号
            * */
            List<Map<String,Object>> attrList = tacheWoOrderDao.qryAttrInfoByOrderId(param);
            boolean isConfigPort = false;
            if(CollectionUtils.isNotEmpty(attrList)&& attrList.size()==4){
                isConfigPort = true;
            }
            // 是否需要分配端口
            operAttrsVal.put("isConfigPort",isConfigPort);
            Map<String, Object> complateMap = new HashMap<>();
            complateMap.put("operStaffId", MapUtils.getString(param,"dealUserId"));
            complateMap.put("woId", woId);
            complateMap.put("operAttrsVal",operAttrsVal);
            resMap = commonWoOrderDealServiceIntf.complateWoService(complateMap);
        }catch(Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>云组网本地跨域变更-下联端口流程，新建申请单环节监听处理发生异常:{}", e.getMessage());
        }
        return resMap;
    }

    /**
     * 核查单启子流程
     * @param param
     * @return
     */
    public Map<String, Object> createChildFlow(Map<String, Object> param, Map<String, Object> specialParam){
        Map<String, Object> resMap = new HashMap<>();
        try{
            param.put("attr", specialParam);
            commonWoOrderDealServiceIntf.createOrderService(param);
        }catch(Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>核查启子流程发生异常:{}", e.getMessage());
        }
        return resMap;
    }


}