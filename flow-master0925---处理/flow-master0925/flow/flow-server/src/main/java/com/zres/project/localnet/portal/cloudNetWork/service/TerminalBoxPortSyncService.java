package com.zres.project.localnet.portal.cloudNetWork.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.cloudNetWork.TerminalBoxPortSyncServiceIntf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 终端盒端口业务占用同步接口
 *
 * @author caomm on 2020/10/23
 */
@Service
public class TerminalBoxPortSyncService implements TerminalBoxPortSyncServiceIntf {

    Logger logger = LoggerFactory.getLogger(TerminalBoxPortSyncService.class);
    @Autowired
    private CloudNetCommonService cloudNetCommonService;

    @Override
    public String terminalBoxPortSync(String request){
        Map<String, Object> logMap = new HashMap<>();
        String ret = "";
        try{
            logMap.put("INTERFNAME", "终端盒端口业务同步接口");
            logMap.put("URL", "/terminal//interfaceBDW/terminalBoxPortSync.spr");
            logMap.put("CONTENT", request);
            logMap.put("REMARK", "接收终端盒上线反馈报文");
            JSONObject jsonObject = JSON.parseObject(request);
            JSONObject ORDER_INFO = jsonObject.getJSONObject("ORDER_INFO");
            String FLOW_ID = ORDER_INFO.getString("FLOW_ID");
            String SERIAL_NUMBER = ORDER_INFO.getString("SERIAL_NUMBER");
            String SVR_CODE = ORDER_INFO.getString("SVR_CODE");
            String TRADE_ID = ORDER_INFO.getString("TRADE_ID");
            logMap.put("ORDERNO", TRADE_ID);
            //入库日志信息
            cloudNetCommonService.insertLogInfo(logMap);



        }catch (Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>终端盒上线反馈处理发生异常:{}", e.getMessage());
        }
        return ret;
    }

}