package com.zres.project.localnet.portal.cloudNetworkFlow.service;

import com.zres.project.localnet.portal.cloudNetworkFlow.CloudNetWorkResCheckServiceIntf;
import com.zres.project.localnet.portal.cloudNetworkFlow.dao.CloudNetWorkResCheckDao;
import com.zres.project.localnet.portal.common.CommonWoOrderDealServiceIntf;
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
 * 云组网核查流程处理service
 *
 * @author caomm on 2020/11/4
 */
@Service
public class CloudNetWorkResCheckService implements CloudNetWorkResCheckServiceIntf {

    private static final Logger logger = LoggerFactory.getLogger(CloudNetWorkResCheckService.class);

    @Autowired
    private CloudNetWorkResCheckDao cloudNetWorkResCheckDao;
    @Autowired
    private CommonWoOrderDealServiceIntf commonWoOrderDealServiceIntf;

    @Override
    public Map<String, Object> qrycircuitInfo(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap<>();
        try{
            List<Map<String, Object>> list = cloudNetWorkResCheckDao.qrycircuitInfo(param);
            resMap.put("success", true);
            resMap.put("data", list);
        }catch(Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>云组网核查单查询电路信息发生异常:{}", e.getMessage());
            resMap.put("success", false);
            resMap.put("msg", e.getMessage());
        }
        return resMap;
    }

    @Override
    public Map<String, Object> querySpecialAreaInfo(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap<>();
        try{
            Map<String, Object> map = cloudNetWorkResCheckDao.querySpecialAreaInfo(param);
            //对数据进行封装处理，方便前端使用
            if (MapUtils.isNotEmpty(map)){
                resMap.put("data", map);
            }else{
                resMap.put("data", null);
            }
            resMap.put("success", true);
        }catch(Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>云组网核查单查询专业区域配置信息发生异常：{}", e.getMessage());
            resMap.put("success", false);
            resMap.put("msg", e.getMessage());
        }
        return resMap;
    }

    @Override
    public Map<String, Object> saveSpecialArea(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap<>();
        try{
            //现根据srvOrdId和cstOrdId判断库中是否已经保存的有专业区域信息，有就更新、没有就插入
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("cstOrdId", MapUtils.getString(param, "cstOrdId"));
            paramMap.put("srvOrdId", MapUtils.getString(param, "srvOrdId"));
            Map<String, Object> retMap = cloudNetWorkResCheckDao.querySpecialAreaInfo(paramMap);
            if (MapUtils.isEmpty(retMap)){
                cloudNetWorkResCheckDao.saveSpecialArea(param);
            }else{
                cloudNetWorkResCheckDao.updateSpecialArea(param);
            }
            resMap.put("success", true);
        }catch(Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>云组网核查单保存专业区域信息发生异常:{}", e.getMessage());
            resMap.put("success", false);
            resMap.put("msg", e.getMessage());
        }
        return resMap;
    }

    @Override
    public int queryCheckChildWoOrder(String orderId) {
        return cloudNetWorkResCheckDao.queryCheckChildWoOrder(orderId);
    }

    @Override
    public String queryMainWoId(String orderId) {
        return cloudNetWorkResCheckDao.queryMainWoId(orderId);
    }

    /**
     * 云组网核查单子流程等待环节回单
     * @param orderId
     * @throws Exception
     */
    public void childFlowWaitTacheComplateWo(String orderId) throws Exception{
        try{
            //根据parentOrderId判断专业核查子流程是否都已经结束了
            int num = queryCheckChildWoOrder(orderId);
            if (num < 1){
                //子流程都已经处理完了，主流程要从子流程等待环节走到核查汇总
                //根据子流程orderId查询主流程，子流程等待环节的woId
                String mainWoId = queryMainWoId(orderId);
                //更改主工单状态为处理中
                updateWoStateByWoId(mainWoId, "290000002");
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("operStaffId", "-2000");
                paramMap.put("woId", mainWoId);
                Map<String, Object> resMap = commonWoOrderDealServiceIntf.complateWoService(paramMap);
                if (MapUtils.getBoolean(resMap, "success")){
                    logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>云组网核查单子流程等待环节回单成功:{}", mainWoId);
                }
            }
        }catch(Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>云组网核查流程子流程等待环节回单发生异常:{}", e.getMessage());
            throw new Exception("云组网核查单子流程等待环节回单发生异常：" + e.getMessage());
        }
    }

    /**
     * 根据woId更改woState
     * @param woId
     */
    public void updateWoStateByWoId(String woId, String state){
        cloudNetWorkResCheckDao.updateWoStateByWoId(woId, state);
    }

    @Override
    public Map<String, Object> saveCheckInfo(Map<String, Object> param) {
        Map<String, Object> retMap = new HashMap<>();
        try{
            List<Map<String, Object>> circuitList = (List<Map<String, Object>>) MapUtils.getObject(param, "circuitData");
            if (CollectionUtils.isNotEmpty(circuitList)){
                for (Map<String, Object> circuitMap : circuitList){
                    String srvOrdId = MapUtils.getString(circuitMap, "SRV_ORD_ID");
                    String woId = MapUtils.getString(circuitMap, "WO_ID");
                    String tacheId = MapUtils.getString(circuitMap, "TACHE_ID");
                    Map<String, Object> checkInfo = MapUtils.getMap(param, "checkInfo");
                    checkInfo.put("srvOrdId", srvOrdId);
                    checkInfo.put("woId", woId);
                    checkInfo.put("tacheId", tacheId);
                    //查询反馈表中是否已经有该工单对应环节
                    Map<String, Object> map = cloudNetWorkResCheckDao.queryCheckInfo(srvOrdId, woId, tacheId);
                    if (!MapUtils.isEmpty(map)){
                        cloudNetWorkResCheckDao.updateCheckInfo(checkInfo);
                    }else{
                        cloudNetWorkResCheckDao.insertIntoCheckInfo(checkInfo);
                    }
                }
            }
           retMap.put("success", true);
        }catch (Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>云组网核查信息保存发生异常:{}", e.getMessage());
            retMap.put("success", false);
            retMap.put("msg", "云组网核查信息保存发生异常:" + e.getMessage());
        }
        return retMap;
    }

    @Override
    public Map<String, Object> queryCheckInfo(Map<String, Object> param) {
        Map<String, Object> retMap = new HashMap<>();
        try{
            Map<String, Object> dataMap = cloudNetWorkResCheckDao.queryCheckInfo(MapUtils.getString(param, "srvOrdId"),
                    MapUtils.getString(param, "woId"), MapUtils.getString(param, "tacheId"));
            retMap.put("success", true);
            retMap.put("data", dataMap);
        }catch (Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>云组网查询核查信息发生异常:{}", e.getMessage());
            retMap.put("success", false);
            retMap.put("msg", "云组网查询核查信息发生异常:" + e.getMessage());
        }
        return retMap;
    }
}