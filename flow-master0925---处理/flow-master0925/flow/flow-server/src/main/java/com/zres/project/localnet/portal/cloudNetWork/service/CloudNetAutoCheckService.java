package com.zres.project.localnet.portal.cloudNetWork.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * MCPE设备资源自动核查接口
 *
 * @author caomm on 2020/11/25
 */
@Service
public class CloudNetAutoCheckService {
    private static final Logger logger = LoggerFactory.getLogger(CloudNetAutoCheckService.class);
    @Autowired
    private CloudNetCommonService cloudNetCommonService;

    public Map<String, Object> autoCheck(String request){
        Map<String, Object> retMap = new HashMap<>();
        Map<String, Object> logMap = new HashMap<>();
        try{
            logMap.put("interfName", "MCPE设备资源自动核查接口");
            logMap.put("url", "/cloudNetWork/interfaceBDW/autoCheck.spr");
            logMap.put("content", request);
            logMap.put("remark", "接收MCPE设备资源自动核查报文");
            JSONObject json = JSON.parseObject(request);
            String PROVINCE = json.getString("PROVINCE");
            String CITY = json.getString("CITY");
            String ADDRESS = json.getString("ADDRESS");
            String CUST_ID = json.getString("CUST_ID");
            logMap.put("orderNo", CUST_ID);
            cloudNetCommonService.insertLogInfo(logMap);

            retMap.put("CODE", "0");
            retMap.put("MESSAGE", "MCPE设备资源自动核查成功！");
        }catch (Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>MCPE设备资源自动核查发生异常:{}", e.getMessage());
            retMap.put("CODE", "1");
            retMap.put("MESSAGE", "MCPE设备资源自动核查发生异常:" + e.getMessage());
        }
        return retMap;
    }
}