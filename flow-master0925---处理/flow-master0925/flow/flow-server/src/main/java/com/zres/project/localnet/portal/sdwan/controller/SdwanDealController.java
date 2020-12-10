package com.zres.project.localnet.portal.sdwan.controller;

import com.zres.project.localnet.portal.sdwan.service.SdwanDealServiceIntf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;


@Controller
public class SdwanDealController {
    Logger logger = LoggerFactory.getLogger(SdwanDealController.class);

    @Autowired
    private SdwanDealServiceIntf sdwanDealServiceIntf;

    /**
     * 工单提交
     * @param params
     * @return
     */
    public Map<String, Object> submitOrderSdwan(Map<String, Object> params){
        Map<String, Object> resMap = new HashMap<String, Object>();
        try{
            resMap = sdwanDealServiceIntf.submitOrderSdwan(params);
        }catch (Exception e){
            logger.info("--------------sdwan工单处理失败-----------------------");
            logger.error("派单失败：", e);
            resMap.put("success", false);
            resMap.put("message", "派单失败!" + e);
        }
        return resMap;
    }
    /**
     * 保存终端信息
     */
    public Map<String, Object> saveDeviceInfo(Map<String, Object> params) {
        Map<String, Object> resMap = new HashMap<String, Object>();
        try{
            resMap = sdwanDealServiceIntf.saveDeviceInfo(params);
        }catch (Exception e){
            logger.info("--------------sdwan保存终端信息失败-----------------------");
            logger.error("保存失败：", e);
            resMap.put("success", false);
            resMap.put("message", "保存失败!" + e);
        }
        return resMap;
    }
    /**
     * 保存wan信息
     */
    public Map<String, Object> saveWanInfo(Map<String, Object> params) {
        Map<String, Object> resMap = new HashMap<String, Object>();
        try{
            resMap = sdwanDealServiceIntf.saveWanInfo(params);
        }catch (Exception e){
            logger.info("--------------sdwan保存wan信息失败-----------------------");
            logger.error("保存失败：", e);
            resMap.put("success", false);
            resMap.put("message", "保存失败!" + e);
        }
        return resMap;
    }

}
