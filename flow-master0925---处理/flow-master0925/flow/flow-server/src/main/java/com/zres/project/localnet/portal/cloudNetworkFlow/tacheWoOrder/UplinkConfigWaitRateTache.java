package com.zres.project.localnet.portal.cloudNetworkFlow.tacheWoOrder;

import com.alibaba.fastjson.JSON;
import com.zres.project.localnet.portal.cloudNetworkFlow.dao.TacheWoOrderDao;
import com.zres.project.localnet.portal.common.CommonWoOrderDealServiceIntf;
import com.zres.project.localnet.portal.common.ToOrderDealTacheWoOrderIntf;
import com.zres.project.localnet.portal.common.util.TacheIdEnum;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.util.OrderTrackOperType;
import org.apache.axis.utils.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Classname UplinkConfigWaitRateTache
 * @Description 上联设备业务配置环节--本地移机
 * @Author tang.hl
 * @Date 2020/11/5 16:42
 */
@Service
public class UplinkConfigWaitRateTache implements ToOrderDealTacheWoOrderIntf {
    Logger logger = LoggerFactory.getLogger(UplinkConfigWaitRateTache.class);

    @Autowired
    private CommonWoOrderDealServiceIntf commonWoOrderDealServiceIntf;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private TacheWoOrderDao tacheWoOrderDao;

    private final String configAZ = "specialtyConfig";
    private final String configA = "specialtyConfig_A";
    private final String configZ = "specialtyConfig_Z";
    private final String YZW_OPTICAL = "YZW_OPTICAL";
    private final String YZW_IPRAN = "YZW_IPRAN";
    private final String YZW_A = "localA";
    private final String YZW_Z = "localZ";
    private final String YZW_B = "B";

    /*
     * 上联设备业务配置环节到单处理逻辑：需要启上联设备业务配置处理子流程，按照AZ区域派发
     * 功能点：
     * 1，跨域业务，上联设备业务配置只派单端，
     * 2.本地业务，上联设备业务配置需要派单给AZ处理，
     *   关于AZ派单，首先，查询新开单派发的区域，若没有，则查询电路属性对应AZ端的区域
     * 3，修改主流程环节状态；
     *
     * @author tang.hl
     * @date 2020/11/5
     *
     */
    @Override
    public Map<String, Object> toOrderTacheDoSomething(Map<String, Object> toOrderTacheDoSomeMap) throws Exception {
        logger.info("-----------到单处理--------升降速--上联设备业务配置环节-----------------------");
        String orderId = MapUtils.getString(toOrderTacheDoSomeMap, "orderId");
        String woId = MapUtils.getString(toOrderTacheDoSomeMap, "woId");
        String tacheCode = MapUtils.getString(toOrderTacheDoSomeMap, "tacheCode");
        // 区域
        List<String> areaList = new ArrayList<>();
        List<String> areaAList = new ArrayList<>();
        List<String> areaZList = new ArrayList<>();
        Map<String, Object> qryMap = new HashMap<>();
        qryMap.put("orderId", orderId);
        // todo :查询新开单的orderId
        Map<String,Object> orderInfo = tacheWoOrderDao.qryNewOpenOrderInfo(qryMap);
        if(MapUtils.isNotEmpty(orderInfo) && (!"".equals(MapUtils.getString(orderInfo,"ORDER_ID","")))){
            Map<String, String> qryParamMap = new HashMap<>();
            qryParamMap.put("orderId",MapUtils.getString(orderInfo,"ORDER_ID"));
            // 查询新开单配置的ipran专业的对应的AZ区域
            Map<String, Object> dispSpecialtyMap = orderDealDao.getDispSpecialtyObj(qryParamMap);
            Object specialtyInfoObject = MapUtils.getObject(dispSpecialtyMap, "SPECIALTY_INFO");
            Map<String, Object> specialtyInfoMap = JSON.parseObject(specialtyInfoObject.toString(), Map.class);
         /*
          *{"specialtyConfig_A":{"YZW_OPTICAL":86230,"YZW_MCPE":55391,"YZW_IPRAN":55378},
          "specialtyConfig_Z":{"YZW_OPTICAL":55391,"YZW_MCPE":55378,"YZW_IPRAN":55391}}
          */
            if(MapUtils.isNotEmpty(specialtyInfoMap) && specialtyInfoMap.keySet().contains(configA)){
                Map<String, Object> specConfig = MapUtils.getMap(specialtyInfoMap,configA);
                String areaIdValue = MapUtils.getString(specConfig, YZW_IPRAN);
                if (!StringUtils.isEmpty(areaIdValue)){
                    String areaValue = StringUtils.strip(areaIdValue, "[]");
                    areaAList = Arrays.asList(areaValue.split(","));
                }
            }
            if(MapUtils.isNotEmpty(specialtyInfoMap) && specialtyInfoMap.keySet().contains(configZ)){
                Map<String, Object> specConfig = MapUtils.getMap(specialtyInfoMap,configZ);
                String areaIdValue = MapUtils.getString(specConfig, YZW_IPRAN);
                if (!StringUtils.isEmpty(areaIdValue)){
                    String areaValue = StringUtils.strip(areaIdValue, "[]");
                    areaZList = Arrays.asList(areaValue.split(","));
                }
            }
        }
        if(CollectionUtils.isEmpty(areaAList)){
            // 电路属性：A端城市
            qryMap.put("attrCode", "20000234");
            String areaA = tacheWoOrderDao.qryCity(qryMap);
            if(!StringUtils.isEmpty(areaA)){
                areaAList.add(areaA);
            }
        }
        if(CollectionUtils.isEmpty(areaZList)){
            // 电路属性：Z端城市
            qryMap.put("attrCode", "20000235");
            String areaA = tacheWoOrderDao.qryCity(qryMap);
            if(!StringUtils.isEmpty(areaA)){
                areaList.add(areaA);
            }
        }
        Map<String,Object> flagMap = tacheWoOrderDao.qryAZFlag(qryMap);
        String flag = MapUtils.getString(flagMap,"PARALLEL_FLAG","");
        if(YZW_A.equals(flag)){
            areaList.addAll(areaAList);
        } else if(YZW_Z.equals(flag)){
            areaList.addAll(areaZList);
        } else {
            areaList.addAll(areaAList);
            areaList.addAll(areaZList);
        }
        if(CollectionUtils.isNotEmpty(areaList)){
            orderDealDao.updateWoStateByWoId(woId, OrderTrackOperType.WO_ORDER_STATE_10);
            try {
                String ordPsid = TacheIdEnum.YZW_UPEQUIP_BUSICONFIG_CHILDFLOW;
                for(String areaId : areaList) {
                    Map<String, Object> startChildMap = new HashMap<>();
                    startChildMap.put("ordPsid", ordPsid);
                    startChildMap.put("parentOrderId", orderId);
                    startChildMap.put("parentOrderCode", tacheCode);
                    startChildMap.put("ORDER_TITLE", "上联设备业务配置子流程");
                    startChildMap.put("AREA", "350002000000000042766408");
                    startChildMap.put("ORDER_CONTENT", "子流程");
                    //startChildMap.put("requFineTime", requFineTime);
                    Map<String, String> attr = new HashMap<>();
                    attr.put("REGION_ID", areaId); // 区域
                    /*attr.put("SPECIALTY_CODE", YZW_IPRAN); // 专业
                    attr.put("ACT_TYPE", operActType); // 操作+动作
                    attr.put("PRODUCT_TYPE", productType); //产品编码*/
                    startChildMap.put("attr", attr);
                    commonWoOrderDealServiceIntf.createOrderService(startChildMap);
                }
            }
            catch (Exception e) {
                throw e;
            }
        }else {
            throw new Exception("上联设备业务配置启子流程，没有查到区域数据！！");
        }
        return null;
    }
}
