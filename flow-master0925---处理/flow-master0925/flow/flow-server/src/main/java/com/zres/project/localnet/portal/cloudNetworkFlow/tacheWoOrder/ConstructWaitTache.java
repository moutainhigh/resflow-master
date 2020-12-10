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
import com.zres.project.localnet.portal.common.util.FlowTacheUtil;
import com.zres.project.localnet.portal.common.util.TacheIdEnum;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.util.OrderTrackOperType;

import com.alibaba.fastjson.JSON;

/**
 * @Classname ConstructWaitTache
 * @Description AZ端网络施工--主流程上环节到单处理逻辑
 * @Author guanzhao
 * @Date 2020/11/2 14:51
 */
@Service
public class ConstructWaitTache implements ToOrderDealTacheWoOrderIntf {

    Logger logger = LoggerFactory.getLogger(ConstructWaitTache.class);

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
     * az端网络施工环节到单处理逻辑：
     * 1，先查询电路调度环节的配置；
     * 2，如果有主调选择先入库主调；
     * 3，根据配置信息确定是否需要施工：需要，则派发施工子流程；
     *                              不需要，则回单该环节；
     * 4，步骤3中需要：修改该环节的状态为已启子流程，并根据配置取派发施工区域；
     * @author guanzhao
     * @date 2020/11/5
     *
     */
    @Override
    public Map<String, Object> toOrderTacheDoSomething(Map<String, Object> toOrderTacheDoSomeMap) throws Exception {
        logger.info("-----------到单处理----------AZ端--网络施工环节-----------------------");
        String forwardOrReverseFlag = MapUtils.getString(toOrderTacheDoSomeMap, "forwardOrReverseFlag");
        if (OrderTrackOperType.YES_LINE.equals(forwardOrReverseFlag)){
            String orderId = MapUtils.getString(toOrderTacheDoSomeMap, "orderId");
            String woId = MapUtils.getString(toOrderTacheDoSomeMap, "woId");
            String tacheCode = MapUtils.getString(toOrderTacheDoSomeMap, "tacheCode");
            String qryKey = configAZ;
            if (FlowTacheUtil.YZW_A_CONSTRUCT_WAIT.equals(tacheCode)) {
                qryKey = configA;
            }
            else if (FlowTacheUtil.YZW_Z_CONSTRUCT_WAIT.equals(tacheCode)) {
                qryKey = configZ;
            }
            Map<String, String> qryMap = new HashMap<>();
            qryMap.put("orderId", orderId);
            Map<String, Object> dispSpecialtyMap = orderDealDao.getDispSpecialtyObj(qryMap);
            String srvOrdId = MapUtils.getString(dispSpecialtyMap, "SRV_ORD_ID");
            String keyNote = MapUtils.getString(dispSpecialtyMap, "KEYNOTE", null);
            if (!StringUtils.isEmpty(keyNote)) {
                //先入库主调
                orderDealDao.insertMaster(keyNote, srvOrdId);
            }
            String constructFlag = MapUtils.getString(dispSpecialtyMap, "IF_CONSTRUCT");
            if (OrderTrackOperType.YES_LINE.equals(constructFlag)) {
                orderDealDao.updateWoStateByWoId(woId, OrderTrackOperType.WO_ORDER_STATE_10);
                try {
                    String ordPsid = "";
                    String specialName = "";
                    Object specialtyInfoObject = MapUtils.getObject(dispSpecialtyMap, "SPECIALTY_INFO");
                    Map<String, Object> specialtyData = JSON.parseObject(specialtyInfoObject.toString(), Map.class);
                    Map<String, String> azData = MapUtils.getMap(specialtyData, qryKey);
                    Iterator<Map.Entry<String, String>> azDataIter = azData.entrySet().iterator();
                    while (azDataIter.hasNext()) {
                        boolean ifStart = true; //是否需要启子流程  这里有mcpe的区域，但是这个不启子流程，这里只启光纤和传输ipran，所以添加这个标识
                        Map.Entry<String, String> e = azDataIter.next();
                        String key = e.getKey();
                        String value = String.valueOf(e.getValue());
                        switch (key) {
                            case YZW_OPTICAL:
                                ordPsid = TacheIdEnum.YZW_LOCAL_FIBER_CHILDFLOW;
                                specialName = "光纤";
                                break;
                            case YZW_IPRAN:
                                ordPsid = TacheIdEnum.YZW_IPRAN_CHILDFLOW;
                                specialName = "传输ipran";
                                break;
                            default:
                                ifStart = false;
                                break;

                        }
                        if (ifStart){
                            Map<String, Object> startChildMap = new HashMap<>();
                            String areaValue = StringUtils.strip(value, "[]");
                            List<String> areaValueList = Arrays.asList(areaValue.split(","));
                            for (int i = 0; i < areaValueList.size(); i++) {
                                startChildMap.put("ordPsid", ordPsid);
                                startChildMap.put("parentOrderId", orderId);
                                startChildMap.put("parentOrderCode", tacheCode);
                                startChildMap.put("ORDER_TITLE", specialName + "专业子流程");
                                startChildMap.put("AREA", "350002000000000042766408");
                                startChildMap.put("ORDER_CONTENT", "子流程");
                                //startChildMap.put("requFineTime", requFineTime);
                                Map<String, String> attr = new HashMap<>();
                                attr.put("REGION_ID", areaValueList.get(i)); // 区域
                                attr.put("SPECIALTY_CODE", key); // 专业
                            /*attr.put("ACT_TYPE", operActType); // 操作+动作
                            attr.put("PRODUCT_TYPE", productType); //产品编码*/
                                startChildMap.put("attr", attr);
                                commonWoOrderDealServiceIntf.createOrderService(startChildMap);
                            }
                        }
                    }
                }
                catch (Exception e) {
                    throw e;
                }
            }
            else {
                Map<String, Object> complateMap = new HashMap<>();
                complateMap.put("operStaffId", "-2000");
                complateMap.put("woId", woId);
                return commonWoOrderDealServiceIntf.complateWoService(complateMap);
            }
        }
        return null;
    }
}
