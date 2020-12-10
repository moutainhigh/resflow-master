package com.zres.project.localnet.portal.cloudNetworkFlow.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.zres.project.localnet.portal.cloudNetworkFlow.WoOrderDealServiceIntf;
import com.zres.project.localnet.portal.cloudNetworkFlow.service.CloudNetworkInterfaceService;
import com.zres.project.localnet.portal.cloudNetworkFlow.util.IntfEnumUtil;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.util.OrderTrackOperType;

import com.ztesoft.res.frame.flow.common.exception.FlowException;

import com.alibaba.fastjson.JSONArray;

@Controller
public class WoOrderDealController {

    Logger logger = LoggerFactory.getLogger(WoOrderDealController.class);

    @Autowired
    private WoOrderDealServiceIntf woOrderDealServiceIntf;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private CloudNetworkInterfaceService cloudNetworkInterfaceService;

    /**
     * 提交工单
     *
     * @param params
     * @return
     */
    public Map<String, Object> submitWoOrder(Map<String, Object> params) {
        logger.info("--------------页面提交进入---提交工单---------------------");
        /**
         * 页面传参：
         *      buttonAction  按钮动作
         *      operAttrs  页面传的数据 包含：remark 说明 等等
         *      circuitData  电路
         */
        Map<String, Object> resMap = new HashMap<String, Object>();
        String buttonAction = MapUtils.getString(params, "buttonAction");
        Map<String, String> operAttrs = null;
        if (params.containsKey("operAttrs")) {
            operAttrs = MapUtils.getMap(params, "operAttrs");
        }

        //List<Object> circuitDatalist = JSON.parseArray(MapUtils.getString(params, "circuitData"));
        List<Map<String, Object>> circuitDatalist = JSONArray.parseObject(MapUtils.getString(params, "circuitData"), List.class);
        for (Map<String, Object> circuitDataMap : circuitDatalist) {
            //Map <String,Object> circuitDataMap = (Map<String, Object>) circuitData;//取出list里面的值转为map
            Map<String, Object> submitMap = new HashMap<String, Object>();
            submitMap.put("tacheId", MapUtils.getString(circuitDataMap, "TACHE_ID"));
            submitMap.put("buttonAction", buttonAction);
            submitMap.put("operAttrs", operAttrs);
            submitMap.put("circuitDataMap", circuitDataMap);
            if (params.containsKey("dispatchOrderData")) {
                submitMap.put("dispatchOrderData", MapUtils.getMap(params, "dispatchOrderData"));
            }
            try {
                resMap = woOrderDealServiceIntf.submitWoOrderCloud(submitMap);
                //如果没有上传新附件，而删除了附件，就会走这里
                /*String delFiles = MapUtil.getString(params, "delFiles", "");
                if (!StringUtils.isEmpty(delFiles)){
                    String[] delFilesList = delFiles.split(",");
                    if(delFilesList.length > 0){
                        orderStandbyDao.removeAttach(delFilesList, woId);
                    }
                }*/
            }
            catch (FlowException fe) {
                logger.info("----------------------工单处理失败-----------------------");
                logger.error("派单失败！流程平台报错：", fe);
                resMap.put("success", false);
                resMap.put("message", "派单失败!" + fe);
            }
            catch (Exception e) {
                logger.info("----------------------工单处理失败-----------------------");
                logger.error("派单失败!业务侧报错：", e);
                resMap.put("success", false);
                resMap.put("message", "派单失败!" + e);
            }
        }
        return resMap;
    }

    public Map<String, Object> getCloudTacheButton(Map<String, Object> params) {
        Map<String, Object> resMap = new HashMap<>();
        try {
            resMap = woOrderDealServiceIntf.getCloudTacheButton(params);
        }
        catch (Exception e) {
            logger.error("按钮查询失败：", e);
            resMap.put("success", false);
            resMap.put("message", "按钮查询失败!" + e);
        }
        return resMap;
    }

    public Map<String, Object> qryCompanyAreaId(Map<String, Object> params) {
        Map<String, Object> resMap = new HashMap<>();
        try {
            resMap = woOrderDealServiceIntf.qryCompanyAreaId(params);
        }
        catch (Exception e) {
            logger.error("分公司区域id数据查询失败：", e);
            resMap.put("success", false);
            resMap.put("message", "分公司区域id数据查询失败!" + e);
        }
        return resMap;
    }

    public Map<String, Object> qryMcpeInstallBackOrderData(Map<String, Object> params) {
        Map<String, Object> resMap = new HashMap<>();
        try {
            resMap = woOrderDealServiceIntf.qryMcpeInstallBackOrderData(params);
        }
        catch (Exception e) {
            logger.error("mcpe安装测试退单的专业和区域查询失败：", e);
            resMap.put("success", false);
            resMap.put("message", "mcpe安装测试退单的专业和区域查询失败!" + e);
        }
        return resMap;
    }

    public boolean qryIfConstruct(String orderId){
        boolean flag = false;
        Map<String, String> qryMap = new HashMap<>();
        qryMap.put("orderId", orderId);
        Map<String, Object> dispSpecialtyMap = orderDealDao.getDispSpecialtyObj(qryMap);
        String constructFlag = MapUtils.getString(dispSpecialtyMap, "IF_CONSTRUCT");
        if (OrderTrackOperType.YES_LINE.equals(constructFlag)) {
            flag = true;
        }
        return flag;
    }

    /*
     * 下联端口查询
     * 终端下联端口配置环节自动失败后调用下联端口查询接口查询，展示在页面，用户手动勾选；
     * @author guanzhao
     * @date 2020/11/23
     *
     */
    public Map<String, Object> qryPortListYZW(Map<String, Object> params) {
        Map<String, Object> resMap = new HashMap<>();
        try {
            String orderId = MapUtils.getString(params, "orderId");
            Map<String, Object> intfParamMap = new HashMap<>();
            intfParamMap.put("orderId", orderId);
            intfParamMap.put("intfCode", IntfEnumUtil.YZW_PORTLISTQUERY); //接口名称编码
            resMap = cloudNetworkInterfaceService.callCloudNetworkInterface(intfParamMap);
            //TODO：这里需要看接口返回的数据，将数据处理下返回给页面
        }
        catch (Exception e) {
            logger.error("下联端口查询失败：", e);
            resMap.put("success", false);
            resMap.put("message", "下联端口查询失败!" + e);
        }
        return resMap;
    }

}
