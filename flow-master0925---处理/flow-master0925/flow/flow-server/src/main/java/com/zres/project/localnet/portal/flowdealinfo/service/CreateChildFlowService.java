package com.zres.project.localnet.portal.flowdealinfo.service;

import java.util.*;

import org.apache.axis.utils.StringUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderQrySecondaryDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.resourceInitiate.data.dao.ResourceInitiateDao;

import com.ztesoft.res.frame.core.util.ListUtil;
import com.ztesoft.res.frame.core.util.MapUtil;

import com.alibaba.fastjson.JSON;

/**
 * 启子流程
 */
@Service
public class CreateChildFlowService {

    Logger logger = LoggerFactory.getLogger(CreateChildFlowService.class);

    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private ResourceInitiateDao resourceInitiateDao;
    @Autowired
    private OrderQrySecondaryDao orderQrySecondaryDao;
    @Autowired
    private OrderDealServiceIntf orderDealServiceIntf;
    @Autowired
    private ListenerOrderServiceIntf listenerOrderServiceIntf;
    @Autowired
    private CreateChildFlowDealService createChildFlowDealService;

    @Transactional
    public void createChildOrder() throws Exception {
        logger.info(">>>>>>service>>>>>>定时任务>>>>>创建子流程>>>>>>>>>>>>>>>>>>>>>>>");
        List<Map<String, String>> createChildFlowList = orderDealDao.qryCreateChildFlow();
        if (!ListUtil.isEmpty(createChildFlowList)){
            for (Map<String, String> createChildFlow : createChildFlowList){
                logger.info(">>>>>>>>>>>>定时启子流程>>>>>>>>>>>>>>>>>>>>>>>");
                Boolean ifSuccess = true;
                String srvOrdId = MapUtils.getString(createChildFlow, "SRV_ORD_ID");
                String woId = MapUtils.getString(createChildFlow, "WO_ID");
                logger.info(">>>>>>>>>>>srvOrdId>>"+srvOrdId+">>>>>>>>>>>>>woId>>>"+woId+">>>>>>>>>>");
                Map<String, Object> orderDataMap = orderDealDao.qryOrderData(woId);
                String orderId = MapUtils.getString(orderDataMap, "ORDERID");
                String orderCode = MapUtils.getString(orderDataMap, "ORDERCODE");
                String orderTitle = MapUtils.getString(orderDataMap, "ORDERTITLE");
                String requFineTime = MapUtils.getString(orderDataMap, "REQ_FIN_DATE");//父定单的要求完成时间
                Map<String, Object> operActTypeMap = orderDealDao.getOperActType(orderId);
                String operActType = MapUtils.getString(operActTypeMap, "ACT_TYPE");
                String productType = MapUtils.getString(operActTypeMap, "PRODUCT_TYPE");
                Map<String, Object> childFlowParams = orderDealDao.getDispSpecialtyObjByRes(srvOrdId, orderId, "1");
                String keyNote = MapUtils.getString(childFlowParams, "KEYNOTE");
                logger.info(">>>>>>>>>>>orderId>>"+orderId+">>>>>>>>>>>>>childFlowParams>>>"+childFlowParams+">>>>>>>>>>");
                try {
                    Map<String, Object> sysMap = new HashMap<>();
                    sysMap.put("srvOrdId",srvOrdId);
                    sysMap.put("orderId",orderId);
                    sysMap.put("keyNote",keyNote);
                    Map<String, Object> belongSysMap = orderQrySecondaryDao.qrySrvOrderBelongSys(sysMap); //查询业务订单归属来源
                    String systemResource = MapUtils.getString(belongSysMap, "SYSTEM_RESOURCE");
                    if(BasicCode.SECOND.equals(systemResource)){
                        orderDealDao.updateRelateInfoMaster(sysMap);

                    }else if(BasicCode.LOCAL.equals(systemResource)){
                        orderDealDao.insertMaster(keyNote, srvOrdId);
                    }
                    // 入库主调局
                    Object flowDispObject = MapUtils.getObject(childFlowParams, "FLOW_SPECIALTY_DATA");
                    Map<String, Object> flowSpecialtyData = JSON.parseObject(flowDispObject.toString(), Map.class);
                    Map<String, String> childFlowSpecialArea = MapUtils.getMap(flowSpecialtyData, "childFlowSpecialArea");
                    Iterator<Map.Entry<String, String>> childIter = childFlowSpecialArea.entrySet().iterator();
                    List<String> childSpecialList = new ArrayList<>();
                    // 查询已派发的子流程,流程状态在处理中或者已竣工的不用再次派发
                    List<Map<String, Object>> alreadyChildFlowList = orderDealDao.queryAlreadyChildFlowList(orderId);
                    boolean isPayout = false;
                    while (childIter.hasNext()) {
                        Map.Entry<String, String> e = childIter.next();
                        Map<String, Object> startChildMap = new HashMap<String, Object>();
                        String key = e.getKey();
                        String value = String.valueOf(e.getValue());
                        String ordPsid = "";
                        if ("OPTICAL_2".equals(key)) {
                            ordPsid = BasicCode.LOCAL_OPTICAL_SPECIAL_CHILDFLOW;
                        } else {
                            ordPsid = BasicCode.LOCAL_OTHER_SPECIAL_CHILDFLOW;
                        }
                        String specialName = resourceInitiateDao.qrySpecialtyName(key); //专业名称查询
                        String areaValue = StringUtils.strip(value, "[]");
                        List<String> areaValueList = Arrays.asList(areaValue.split(","));
                        for (int i = 0; i < areaValueList.size(); i++) {
                            // 已派发的区域和专业不再重复派发
                            if (checkoutRepeat(key, areaValueList.get(i), alreadyChildFlowList)) {
                                isPayout = true;
                                continue;
                            }
                            startChildMap.put("ordPsid", ordPsid);
                            startChildMap.put("parentOrderId", orderId);
                            startChildMap.put("parentOrderCode", orderCode);
                            startChildMap.put("ORDER_TITLE", orderTitle + "_" + specialName + "专业子流程");
                            startChildMap.put("AREA", "350002000000000042766408");
                            startChildMap.put("ORDER_CONTENT", "子流程");
                            startChildMap.put("requFineTime", requFineTime);
                            Map<String, String> attr = new HashMap<>();
                            attr.put("REGION_ID", areaValueList.get(i)); // 区域
                            attr.put("SPECIALTY_CODE", key); // 专业
                            attr.put("ACT_TYPE", operActType); // 操作+动作
                            attr.put("PRODUCT_TYPE", productType); //产品编码
                            startChildMap.put("attr", attr);
                            orderDealServiceIntf.createOrder(startChildMap);
                            isPayout = true;
                            childSpecialList.add(specialName);
                        }
                    }
                    if (isPayout) {
                        orderDealDao.updOperLog(woId, childSpecialList.toString());
                        orderDealDao.updCreateChildFlowState(orderId, "10F");
                        orderDealDao.copyToCreateChildFlowHis(orderId);
                        orderDealDao.deleteCreateChildFlow(orderId);
                        /**
                         * 如果已派发的子流程已全部结束，那么电路调度环节提交工单，
                         *
                         * num = orderDealDao.qryAllChildIsEnd(orderId)  已派发并且没有走到等待环节的子流程数量
                         * num<1 代表已派发的子流程已全部结束
                         */
                        if(orderDealDao.qryAllChildIsEnd(orderId)<1){
                            Map<String,Object> param = new HashMap<>();
                            param.put("parentOrderId",orderId);
                            // 调用电路调度回单方法
                            listenerOrderServiceIntf.circuitDispatchCompWoOrder(param);
                        }
                    }
                } catch (Exception e) {
                    ifSuccess = false;
                    logger.info(">>>>>>>>>>>ifSuccess>>" + ifSuccess );
                    throw e;
                } finally {
                    if (!ifSuccess) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    createChildFlowDealService.updCreateChildFlowStateService(orderId);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                }
            }
        }
    }

    /**
     * 校验是否派发过该区域专业
     *
     * @param key 专业
     * @param areaValue 区域
     * @param alreadyChildFlowList 已派发流程区域、专业
     * @return
     */
    private boolean checkoutRepeat(String key, String areaValue, List<Map<String, Object>> alreadyChildFlowList) {
        boolean temp = false;
        for (Map<String, Object> map : alreadyChildFlowList) {
            String regionId = MapUtil.getString(map, "REGION_ID");
            String specialtyCode = MapUtil.getString(map, "SPECIALTY_CODE");
            if (key.equals(specialtyCode) && areaValue.equals(regionId)) {
                temp = true;
            }
        }
        return temp;
    }

}
