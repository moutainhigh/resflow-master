package com.zres.project.localnet.portal.flowdealinfo.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ztesoft.res.frame.core.util.MapUtil;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.util.OrderTrackOperType;

@Service
public class FowordResService implements FowordResServiceIntf{

    @Autowired
    private OrderDealDao orderDealDao;

    /**
     * add by ren.jiahang at 20201029 for 增加资源分配前置资源功能
     *  需求：互联网专线产品限制资源分配处理顺序：1、传输》2、数据》3、其他
     *       互联网商务专线产品：限定数据专业资源分配环节等待接入专业数据制作环节工单提交以后才生成工单
     *       定义:优先级高的专业称之为前置资源，前置资源后的专业称之为后置资源
     *       如：数据的前置资源为传输，后置资源为其他
     * 1、监听资源分配环节，修改所有后置资源状态为待前置专业处理。
     * 2、在资源分配工单提交中判断后置资源改为处理中，同时修改工单创建时间为当前时间。
     */
    @Override
    public void modifyResWoState(String orderId, boolean bussSpecialType){
        List<Map<String, Object>> woSpecialtyList = orderDealDao.queryWoAndSpecialtyInfoByOrderId(orderId);
        String transWoId = "";
        String dataWoId = "";
        String accessId = "";
        for(Map<String, Object> woSpcialMap :woSpecialtyList){
            String specialtyCode = MapUtil.getString(woSpcialMap, "SPECIALTY_CODE");
            String woId = MapUtil.getString(woSpcialMap, "WO_ID");
            String tacheIdSon = MapUtil.getString(woSpcialMap, "TACHE_ID");
            if(BasicCode.RES_ALLOCATE.equals(tacheIdSon) && BasicCode.TRANS_3.equals(specialtyCode)){
                transWoId = woId;
            }
            else if(BasicCode.RES_ALLOCATE.equals(tacheIdSon) && BasicCode.DATA_4.equals(specialtyCode)){
                dataWoId = woId;
            }
            else if (BasicCode.RES_ALLOCATE.equals(tacheIdSon) && BasicCode.ACCESS_6.equals(specialtyCode)){
                accessId = woId;
            }
        }
        //判断完是否有传输和数据专业后，对应修改后置工单状态
        if(!StringUtils.isEmpty(transWoId)){
            for(Map<String, Object> woSpcialMap :woSpecialtyList){
                String specialtyCode = MapUtil.getString(woSpcialMap, "SPECIALTY_CODE");
                String woId = MapUtil.getString(woSpcialMap, "WO_ID");
                String woState = MapUtil.getString(woSpcialMap, "WO_STATE");
                String tacheIdSon = MapUtil.getString(woSpcialMap, "TACHE_ID");
                if(BasicCode.RES_ALLOCATE.equals(tacheIdSon) && !BasicCode.TRANS_3.equals(specialtyCode) && OrderTrackOperType.WO_ORDER_STATE_2.equals(woState)){
                    this.updateWoInfo(woId,OrderTrackOperType.WO_ORDER_STATE_19,true);
                }
            }
        }
        else if(bussSpecialType && !StringUtils.isEmpty(dataWoId) && !StringUtils.isEmpty(accessId)){
            for(Map<String, Object> woSpcialMap :woSpecialtyList){
                String specialtyCode = MapUtil.getString(woSpcialMap, "SPECIALTY_CODE");
                String woId = MapUtil.getString(woSpcialMap, "WO_ID");
                String woState = MapUtil.getString(woSpcialMap, "WO_STATE");
                String tacheIdSon = MapUtil.getString(woSpcialMap, "TACHE_ID");
                if(BasicCode.RES_ALLOCATE.equals(tacheIdSon) && !BasicCode.ACCESS_6.equals(specialtyCode) && OrderTrackOperType.WO_ORDER_STATE_2.equals(woState)){
                    this.updateWoInfo(woId,OrderTrackOperType.WO_ORDER_STATE_19,true);
                }
            }
        }
        else if(!StringUtils.isEmpty(dataWoId)){
            for(Map<String, Object> woSpcialMap :woSpecialtyList){
                String specialtyCode = MapUtil.getString(woSpcialMap, "SPECIALTY_CODE");
                String woId = MapUtil.getString(woSpcialMap, "WO_ID");
                String woState = MapUtil.getString(woSpcialMap, "WO_STATE");
                String tacheIdSon = MapUtil.getString(woSpcialMap, "TACHE_ID");
                if(BasicCode.RES_ALLOCATE.equals(tacheIdSon) && !BasicCode.DATA_4.equals(specialtyCode) && OrderTrackOperType.WO_ORDER_STATE_2.equals(woState)){
                    this.updateWoInfo(woId,OrderTrackOperType.WO_ORDER_STATE_19,true);
                }
            }
        }
    }

    /**
     *
     * @param woId
     * @param woState
     * @param isupdateCreateDate 是否修改创建时间
     */
    @Override
    public void updateWoInfo(String woId, String woState, boolean isupdateCreateDate){
        Map<Object, Object> woMap = new HashMap<>();
        woMap.put("woId", woId);
        woMap.put("woState", woState);
        if(isupdateCreateDate)
            woMap.put("createDate", "1");
        orderDealDao.updateWoStateAndDateByWoId(woMap);
    }

    /**
     * 资源分配提交
     * 如果是商务专线传输提交&&包含接入和数据专业 ：修改接入专业为处理中 否则 如果包含数据专业，修改数据专业为处理中，否则修改其他所有专业为处理中
     * 如果是商务专线接入提交：不做任何操作
     *
     * 数据制作提交
     * 如果是商务专线接入提交 && 包含数据专业，修改数据专业资源分配为处理中
     *
     * @param orderId
     * @param currwoId
     */
    @Override
    public void resSubmitOrder(String orderId ,String currwoId) {
        Map<String, Object> srvOrdIndMap = orderDealDao.querySrvInfoByWoId(currwoId);
        //1.准备数据
        String tacheId = MapUtils.getString(srvOrdIndMap, "TACHE_ID");
        String srvOrdId = MapUtils.getString(srvOrdIndMap, "SRV_ORD_ID");
        String currspecialtyCode = MapUtils.getString(srvOrdIndMap, "SPECIALTY_CODE");
        String subType = orderDealDao.qrySubType(srvOrdId);
        List<Map<String, Object>> woSpecialtyList = orderDealDao.queryWoAndSpecialtyInfoByOrderId(orderId);
        String dataWoId = "";
        String accessId = "";

        for(Map<String, Object> woSpcialMap :woSpecialtyList){
            String specialtyCode = MapUtil.getString(woSpcialMap, "SPECIALTY_CODE");
            String woId = MapUtil.getString(woSpcialMap, "WO_ID");
            String tacheIdSon = MapUtil.getString(woSpcialMap, "TACHE_ID");
            if(BasicCode.RES_ALLOCATE.equals(tacheIdSon) && BasicCode.DATA_4.equals(specialtyCode)){
                dataWoId = woId;
            }
            else if (BasicCode.RES_ALLOCATE.equals(tacheIdSon) && BasicCode.ACCESS_6.equals(specialtyCode)){
                accessId = woId;
            }
        }

        if(BasicCode.RES_ALLOCATE.equals(tacheId)){
            if("5".equals(subType)  && BasicCode.TRANS_3.equals(currspecialtyCode) && !StringUtils.isEmpty(accessId) && !StringUtils.isEmpty(dataWoId)){
                for(Map<String, Object> woSpcialMap :woSpecialtyList){
                    String specialtyCode = MapUtil.getString(woSpcialMap, "SPECIALTY_CODE");
                    String woId = MapUtil.getString(woSpcialMap, "WO_ID");
                    String woState = MapUtil.getString(woSpcialMap, "WO_STATE");
                    String tacheIdSon = MapUtil.getString(woSpcialMap, "TACHE_ID");

                    if(BasicCode.RES_ALLOCATE.equals(tacheIdSon) && BasicCode.ACCESS_6.equals(specialtyCode) && OrderTrackOperType.WO_ORDER_STATE_19.equals(woState)){
                        this.updateWoInfo(woId,OrderTrackOperType.WO_ORDER_STATE_2,true);
                    }
                }
            }else if ("5".equals(subType)  && BasicCode.ACCESS_6.equals(currspecialtyCode) && !StringUtils.isEmpty(dataWoId)){

            }
            else if(!StringUtils.isEmpty(dataWoId)){
                for(Map<String, Object> woSpcialMap :woSpecialtyList){
                    String specialtyCode = MapUtil.getString(woSpcialMap, "SPECIALTY_CODE");
                    String woId = MapUtil.getString(woSpcialMap, "WO_ID");
                    String woState = MapUtil.getString(woSpcialMap, "WO_STATE");
                    String tacheIdSon = MapUtil.getString(woSpcialMap, "TACHE_ID");
                    if(BasicCode.RES_ALLOCATE.equals(tacheIdSon) && BasicCode.DATA_4.equals(specialtyCode) && OrderTrackOperType.WO_ORDER_STATE_19.equals(woState)){
                        this.updateWoInfo(woId,OrderTrackOperType.WO_ORDER_STATE_2,true);
                    }
                }
            }
            else{
                for(Map<String, Object> woSpcialMap :woSpecialtyList){
                    String specialtyCode = MapUtil.getString(woSpcialMap, "SPECIALTY_CODE");
                    String woId = MapUtil.getString(woSpcialMap, "WO_ID");
                    String woState = MapUtil.getString(woSpcialMap, "WO_STATE");
                    String tacheIdSon = MapUtil.getString(woSpcialMap, "TACHE_ID");
                    if(BasicCode.RES_ALLOCATE.equals(tacheIdSon) && !BasicCode.TRANS_3.equals(specialtyCode) && OrderTrackOperType.WO_ORDER_STATE_19.equals(woState)){
                        this.updateWoInfo(woId,OrderTrackOperType.WO_ORDER_STATE_2,true);
                    }
                }
            }
        }
        else if(BasicCode.DATA_MAKE.equals(tacheId)){
            if("5".equals(subType) && BasicCode.ACCESS_6.equals(currspecialtyCode) && !StringUtils.isEmpty(dataWoId)){
                for(Map<String, Object> woSpcialMap :woSpecialtyList){
                    String specialtyCode = MapUtil.getString(woSpcialMap, "SPECIALTY_CODE");
                    String woId = MapUtil.getString(woSpcialMap, "WO_ID");
                    String woState = MapUtil.getString(woSpcialMap, "WO_STATE");
                    String tacheIdSon = MapUtil.getString(woSpcialMap, "TACHE_ID");
                    if(BasicCode.RES_ALLOCATE.equals(tacheIdSon) && BasicCode.DATA_4.equals(specialtyCode) && OrderTrackOperType.WO_ORDER_STATE_19.equals(woState)){
                        this.updateWoInfo(woId,OrderTrackOperType.WO_ORDER_STATE_2,true);
                    }
                }
            }
        }
    }
}
