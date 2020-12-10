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
 * 全程业务测试反馈接口
 *
 * @author caomm on 2020/11/24
 */
@Service
public class CloudNetWholeTestReplyService {
    private static final Logger logger = LoggerFactory.getLogger(CloudNetWholeTestReplyService.class);
    @Autowired
    private CloudNetCommonService commonService;

    public Map<String, Object> wholeTestReply(String request){
        Map<String, Object> logMap = new HashMap<>();
        Map<String, Object> retMap = new HashMap<>();
        try{
            logMap.put("interfName", "云组网全程业务测试反馈接口");
            logMap.put("url", "/cloudNetWork/interfaceBDW/wholeTestReply.spr");
            logMap.put("content", request);
            logMap.put("remark", "接收云组网全程业务测试反馈报文");
            JSONObject json = JSON.parseObject(request);
            JSONObject ORDER_INFO = json.getJSONObject("ORDER_INFO");
            String FLOW_ID = ORDER_INFO.getString("FLOW_ID");
            String SERIAL_NUMBER = ORDER_INFO.getString("SERIAL_NUMBER");
            String SVR_CODE = ORDER_INFO.getString("SVR_CODE");
            String TRADE_ID = ORDER_INFO.getString("TRADE_ID");

            retMap.put("CODE", "0");
            retMap.put("MESSAGE", "全程业务测试反馈成功！");
        }catch(Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>云组网全程业务测试反馈接口发生异常:{}", e.getMessage());
            retMap.put("MESSAGE", "云组网全程业务测试反馈接口发生异常:" + e.getMessage());
            retMap.put("CODE", "1");
        }
        return retMap;
    }
}