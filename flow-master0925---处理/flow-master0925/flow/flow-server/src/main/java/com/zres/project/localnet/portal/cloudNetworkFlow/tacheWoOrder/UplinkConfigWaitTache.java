package com.zres.project.localnet.portal.cloudNetworkFlow.tacheWoOrder;

import java.util.*;

import org.apache.axis.utils.StringUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.common.CommonWoOrderDealServiceIntf;
import com.zres.project.localnet.portal.common.ToOrderDealTacheWoOrderIntf;
import com.zres.project.localnet.portal.common.util.TacheIdEnum;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.util.OrderTrackOperType;

import com.alibaba.fastjson.JSON;

/**
 * @Classname UplinkConfigWaitTache
 * @Description 上联设备业务配置环节
 * @Author guanzhao
 * @Date 2020/11/5 16:42
 */
@Service
public class UplinkConfigWaitTache implements ToOrderDealTacheWoOrderIntf {
    Logger logger = LoggerFactory.getLogger(UplinkConfigWaitTache.class);

    @Autowired
    private CommonWoOrderDealServiceIntf commonWoOrderDealServiceIntf;
    @Autowired
    private OrderDealDao orderDealDao;

    private final String configAZ = "specialtyConfig";
    private final String configA = "specialtyConfig_A";
    private final String configZ = "specialtyConfig_Z";
    private final String YZW_OPTICAL = "YZW_OPTICAL";
    private final String YZW_IPRAN = "YZW_IPRAN";

    /*
     * 上联设备业务配置环节到单处理逻辑：需要启上联设备业务配置处理子流程，按照传输ipran专业选择的区域派发
     * 1，查询电路调度电路调度环节配置的ipran专业的区域；
     * 2，修改主流程环节状态；
     *
     * @author guanzhao
     * @date 2020/11/5
     *
     */
    @Override
    public Map<String, Object> toOrderTacheDoSomething(Map<String, Object> toOrderTacheDoSomeMap) throws Exception {
        logger.info("-----------到单处理----------上联设备业务配置环节-----------------------");
        String orderId = MapUtils.getString(toOrderTacheDoSomeMap, "orderId");
        String woId = MapUtils.getString(toOrderTacheDoSomeMap, "woId");
        String tacheCode = MapUtils.getString(toOrderTacheDoSomeMap, "tacheCode");
        Map<String, String> qryMap = new HashMap<>();
        qryMap.put("orderId", orderId);
        Map<String, Object> dispSpecialtyMap = orderDealDao.getDispSpecialtyObj(qryMap);
        Object specialtyInfoObject = MapUtils.getObject(dispSpecialtyMap, "SPECIALTY_INFO");
        Map<String, Object> specialtyInfoMap = JSON.parseObject(specialtyInfoObject.toString(), Map.class);
        if(MapUtils.isNotEmpty(specialtyInfoMap)){
            orderDealDao.updateWoStateByWoId(woId, OrderTrackOperType.WO_ORDER_STATE_10);
            try {
                String ordPsid = TacheIdEnum.YZW_UPEQUIP_BUSICONFIG_CHILDFLOW;
                Iterator<Map.Entry<String, Object>> dispConfigIter = specialtyInfoMap.entrySet().iterator();
                while (dispConfigIter.hasNext()) {
                    Map.Entry<String, Object> dispConfig = dispConfigIter.next();
                    //String dispConfigKey = dispConfig.getKey();
                    String dispConfigValue = String.valueOf(dispConfig.getValue());
                    Map<String, String> dispConfigData = JSON.parseObject(dispConfigValue, Map.class);
                    //上联设备业务配置处理子流程az端派发--按照传输ipran专业选择的区域派发；
                    String areaIdValue = MapUtils.getString(dispConfigData, YZW_IPRAN);
                    if (StringUtils.isEmpty(areaIdValue)){
                        continue;
                    }
                    Map<String, Object> startChildMap = new HashMap<>();
                    String areaValue = StringUtils.strip(areaIdValue, "[]");
                    List<String> areaValueList = Arrays.asList(areaValue.split(","));
                    for (int i = 0; i < areaValueList.size(); i++) {
                        startChildMap.put("ordPsid", ordPsid);
                        startChildMap.put("parentOrderId", orderId);
                        startChildMap.put("parentOrderCode", tacheCode);
                        startChildMap.put("ORDER_TITLE", "上联设备业务配置子流程");
                        startChildMap.put("AREA", "350002000000000042766408");
                        startChildMap.put("ORDER_CONTENT", "子流程");
                        //startChildMap.put("requFineTime", requFineTime);
                        Map<String, String> attr = new HashMap<>();
                        attr.put("REGION_ID", areaValueList.get(i)); // 区域
                        /*attr.put("SPECIALTY_CODE", YZW_IPRAN); // 专业
                        attr.put("ACT_TYPE", operActType); // 操作+动作
                        attr.put("PRODUCT_TYPE", productType); //产品编码*/
                        startChildMap.put("attr", attr);
                        commonWoOrderDealServiceIntf.createOrderService(startChildMap);
                    }
                }
            }
            catch (Exception e) {
                throw e;
            }
        }else {
            throw new Exception("上联设备业务配置启子流程，没有查到电路调度配置的数据！！");
        }
        return null;
    }
}
