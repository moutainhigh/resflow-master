package com.zres.project.localnet.portal.cloudNetWork.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.cloudNetWork.CloudNetChangeOrderIntf;
import com.zres.project.localnet.portal.cloudNetWork.util.CreateCloudNetWorkReponseUntil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 云组网异常单处理service
 *
 * @author caomm on 2020/11/19
 */
@Service
public class CloudChangeOrderService implements CloudNetChangeOrderIntf {
    private static final Logger logger = LoggerFactory.getLogger(CloudChangeOrderService.class);
    @Autowired
    private CloudNetCommonService cloudNetCommonService;
    @Override
    public String changeOrder(String request) {
        String ret = "";
        Map<String, Object> logMap = new HashMap<>();
        try{
            logMap.put("interfName", "云组网异常单接口");
            logMap.put("url", "/cloudNetWork/interfaceBDW/changeOrder.spr");
            logMap.put("content", request);
            logMap.put("remark", "接收云组网异常单报文");
            JSONObject json = JSON.parseObject(request);
            JSONObject UNI_BSS_BODY = json.getJSONObject("UNI_BSS_BODY");
            JSONObject YZWCHANGE_ORDER_REQ = UNI_BSS_BODY.getJSONObject("YZWCHANGE_ORDER_REQ");
            JSONObject ROUTING = YZWCHANGE_ORDER_REQ.getJSONObject("ROUTING");
            String ROUTE_TYPE = ROUTING.getString("ROUTE_TYPE");
            String ROUTE_VALUE = ROUTING.getString("ROUTE_VALUE");
            JSONObject CST_ORD = YZWCHANGE_ORDER_REQ.getJSONObject("CST_ORD");
            String SUBSCRIBE_ID = CST_ORD.getString("SUBSCRIBE_ID");
            String SUBSCRIBE_ID_RELA = CST_ORD.getString("SUBSCRIBE_ID_RELA");
            JSONObject SRV_ORD_LIST = CST_ORD.getJSONObject("SRV_ORD_LIST");
            JSONArray SRV_ORD = SRV_ORD_LIST.getJSONArray("SRV_ORD");
            JSONObject srvOrd = SRV_ORD.getJSONObject(0);
            String ACTIVE_TYPE = srvOrd.getString("ACTIVE_TYPE");
            String SERIAL_NUMBER = srvOrd.getString("SERIAL_NUMBER");
            String TRADE_ID = srvOrd.getString("TRADE_ID");
            String TRADE_ID_RELA = srvOrd.getString("TRADE_ID_RELA");
            String FLOW_ID = srvOrd.getString("FLOW_ID");
            logMap.put("orderNo", TRADE_ID);
            //入库日志信息
            cloudNetCommonService.insertLogInfo(logMap);



        }catch(Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>云组网异常单接口发生异常：{}", e.getMessage());
            ret = CreateCloudNetWorkReponseUntil.createRespone("ChangeOrder", "1", "云组网异常单接口发生异常：" + e.getMessage());
        }
        return ret;
    }
}