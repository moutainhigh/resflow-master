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
 * IPRAN端口限速配置下发结果反馈接口
 *
 * @author caomm on 2020/11/24
 */
@Service
public class CloudNetIpranPortReplyService {
    private static final Logger logger = LoggerFactory.getLogger(CloudNetIpranPortReplyService.class);

    @Autowired
    private CloudNetCommonService cloudNetCommonService;

    public Map<String, Object> ipranPortReply(String request){
        Map<String, Object> retMap = new HashMap<>();
        Map<String, Object> logMap = new HashMap<>();
        try{
            logMap.put("interfName", "IPRAN端口限速配置下发结果反馈接口");
            logMap.put("url", "/cloudNetWork/interfaceBDW/ipranPortReply.spr");
            logMap.put("content", request);
            logMap.put("remark", "接收云组网IPRAN端口限速配置下发结果反馈报文");
            JSONObject json = JSON.parseObject(request);
            JSONObject ORDER_INFO = json.getJSONObject("ORDER_INFO");
            String FLOW_ID = ORDER_INFO.getString("FLOW_ID");
            String SERIAL_NUMBER = ORDER_INFO.getString("SERIAL_NUMBER");
            String SVR_CODE = ORDER_INFO.getString("SVR_CODE");
            String TRADE_ID = ORDER_INFO.getString("TRADE_ID");
            logMap.put("orderNo", TRADE_ID);
            cloudNetCommonService.insertLogInfo(logMap);

            retMap.put("CODE", "0");
            retMap.put("MESSAGE", "IPRAN端口限速配置下发结果反馈成功！");
        }catch (Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>IPRAN端口限速配置下发结果反馈接口发生异常:{}", e.getMessage());
            retMap.put("CODE", "1");
            retMap.put("MESSAGE", "IPRAN端口限速配置下发结果反馈接口发生异常:" + e.getMessage());
        }
        return retMap;
    }
}