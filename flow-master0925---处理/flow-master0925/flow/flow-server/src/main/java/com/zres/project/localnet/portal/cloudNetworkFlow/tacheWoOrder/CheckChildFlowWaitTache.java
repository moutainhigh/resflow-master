package com.zres.project.localnet.portal.cloudNetworkFlow.tacheWoOrder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.cloudNetworkFlow.dao.CloudNetWorkResCheckDao;
import com.zres.project.localnet.portal.cloudNetworkFlow.util.EnumUtil;
import com.zres.project.localnet.portal.common.CommonWoOrderDealServiceIntf;
import com.zres.project.localnet.portal.common.ToOrderDealTacheWoOrderIntf;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 核查子流程等待环节
 *
 * @author caomm on 2020/11/9
 */
@Service
public class CheckChildFlowWaitTache implements ToOrderDealTacheWoOrderIntf {
    private static final Logger logger = LoggerFactory.getLogger(CheckChildFlowWaitTache.class);
    @Autowired
    private CloudNetWorkResCheckDao cloudNetWorkResCheckDao;
    @Autowired
    private CommonWoOrderDealServiceIntf commonWoOrderDealServiceIntf;
    @Override
    public Map<String, Object> toOrderTacheDoSomething(Map<String, Object> param) throws Exception {
        Map<String, Object> resMap = new HashMap<>();
        try{
            //监听云组网核查流程，子流程等待环节到单
            if (EnumUtil.YZW_CHECK_WAITING.equals(MapUtils.getString(param, "tacheCode"))){
                //根据工单ID更改子流程等待环节的状态
                cloudNetWorkResCheckDao.updateWoState(MapUtils.getString(param, "woId"), "290000110");
                //根据orderId查询srvOrdId和cstOrdId,然后去查询该条电路的专业区域配置信息
                Map<String, Object> map = cloudNetWorkResCheckDao.querySrvOrdInfo(MapUtils.getString(param, "orderId"));
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("srvOrdId", MapUtils.getString(map, "SRV_ORD_ID"));
                paramMap.put("cstOrdId", MapUtils.getString(map, "CST_ORD_ID"));
                Map<String, Object> specialMap = cloudNetWorkResCheckDao.querySpecialAreaInfo(paramMap);
                //如果不是直接到完工汇总环节，则根据专业区域信息进行启子流程
                if ("1".equals(MapUtils.getString(specialMap, "NEW_CREATE_RESOURCE"))){
                    Map<String, Object> childParam = new HashMap<>();
                    childParam.put("parentOrderId", MapUtils.getString(param, "orderId"));
                    childParam.put("parentOrderCode", MapUtils.getString(param, "tacheCode"));
                    childParam.put("AREA", "350002000000000042766408");
                    childParam.put("ORDER_CONTENT", "子流程");
                    //启IPRAN专业子流程
                    if (!StringUtils.isEmpty(MapUtils.getString(specialMap, "SPECIALTY_INFO"))){
                        JSONObject ipranInfo = JSON.parseObject(MapUtils.getString(specialMap, "SPECIALTY_INFO"));
                        childParam.put("ordPsid", EnumUtil.YZW_UPLINK_DEVICE_CHILD_FLOW);
                        childParam.put("ORDER_TITLE", EnumUtil.IPRAN + "专业子流程");
                        childParam.put("ORDER_CONTENT", "子流程");
                        Map<String, Object> specialParam = new HashMap<>();
                        if (!StringUtils.isEmpty(ipranInfo.getString("areaId"))){
                            specialParam.put("specail", EnumUtil.IPRAN);
                            specialParam.put("areaId", ipranInfo.getString("areaId"));
                            createChildFlow(childParam, specialParam);
                        }
                    }
                    //启光纤专业子流程
                    if (!StringUtils.isEmpty(MapUtils.getString(specialMap, "SPECIALTY_INFO_NAME"))){
                        childParam.put("ordPsid", EnumUtil.YZW_FIBER_RES_CHECK_CHILD_FLOW);
                        childParam.put("ORDER_CONTENT", "子流程");
                        JSONObject fiberInfo = JSON.parseObject(MapUtils.getString(specialMap, "SPECIALTY_INFO_NAME"));
                        Map<String, Object> specialParam = new HashMap<>();
                        specialParam.put("specail", EnumUtil.FIBER);
                        //Z端
                        if (!StringUtils.isEmpty(fiberInfo.getString("areaIdA"))){
                            childParam.put("ORDER_TITLE", EnumUtil.FIBER + "A端专业子流程");
                            specialParam.put("areaId", fiberInfo.getString("areaIdA"));
                            createChildFlow(childParam, specialParam);
                        }
                        //A端
                        if (!StringUtils.isEmpty(fiberInfo.getString("areaIdZ"))){
                            childParam.put("ORDER_TITLE", EnumUtil.FIBER + "Z端专业子流程");
                            specialParam.put("areaId", fiberInfo.getString("areaIdA"));
                            createChildFlow(childParam, specialParam);
                        }

                    }
                    //启终端盒子流程
                    if (!StringUtils.isEmpty(MapUtils.getString(specialMap, "FLOW_SPECIALTY_DATA"))){
                        JSONObject terminalBox = JSON.parseObject(MapUtils.getString(specialMap, "FLOW_SPECIALTY_DATA"));
                        childParam.put("ordPsid", EnumUtil.YZW_MCPE_CHECK_CHILD_FLOW);
                        childParam.put("ORDER_CONTENT", "子流程");
                        Map<String, Object> specialParam = new HashMap<>();
                        specialParam.put("special", EnumUtil.TERMINAL_BOX);
                        //A端
                        if (!StringUtils.isEmpty(terminalBox.getString("areaIdA"))){
                            childParam.put("ORDER_TITLE", EnumUtil.TERMINAL_BOX + "A端专业子流程");
                            specialParam.put("areaId", terminalBox.getString("areaIdA"));
                            createChildFlow(childParam, specialParam);
                        }
                        //Z端
                        if (!StringUtils.isEmpty(terminalBox.getString("areaIdZ"))){
                            childParam.put("ORDER_TITLE", EnumUtil.TERMINAL_BOX + "Z端专业子流程");
                            specialParam.put("areaId", terminalBox.getString("areaIdZ"));
                            createChildFlow(childParam, specialParam);
                        }
                    }
                }
            }
        }catch(Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>核查子流程等待环节监听处理发生异常:{}", e.getMessage());
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