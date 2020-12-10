package com.zres.project.localnet.portal.cloudNetWork.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.cloudNetWork.ReceiveOrderServiceIntf;
import com.zres.project.localnet.portal.cloudNetWork.dao.ReceiveOrderDao;
import com.zres.project.localnet.portal.cloudNetWork.util.CreateCloudNetWorkReponseUntil;
import org.apache.axis.utils.StringUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 云组网收单接口处理service 备注：该接口支持一单多路
 *
 * @author caomm on 2020/10/12
 */
@Service
public class ReceiveOrderService implements ReceiveOrderServiceIntf {
    Logger logger = LoggerFactory.getLogger(ReceiveOrderService.class);
    @Autowired
    private DealOrderInfoService insertOrderInfo;
    @Autowired
    private ReceiveOrderDao receiveOrderDao;
    @Autowired
    private CloudNetCommonService commonService;

    @Override
    public String receiveOrder(String request){
        Map<String, Object> logMap = new HashMap<>();
        String ret = "";
        try{
            logMap.put("interfName", "云组网收单接口");
            logMap.put("url", "/cloudNetWork/interfaceBDW/receiveOrder.spr");
            logMap.put("content", request);
            logMap.put("remark", "接收云组网json报文");
            //解析收单报文
            JSONObject json = JSON.parseObject(request);
            JSONObject UNI_BSS_BODY = json.getJSONObject("UNI_BSS_BODY");
            JSONObject YZWAPPLY_ORDER_REQ = UNI_BSS_BODY.getJSONObject("YZWAPPLY_ORDER_REQ");
            JSONObject CST_ORD = YZWAPPLY_ORDER_REQ.getJSONObject("CST_ORD");
            JSONObject CST_ORD_INFO = CST_ORD.getJSONObject("CST_ORD_INFO");
            JSONObject SRV_ORD_LIST = CST_ORD_INFO.getJSONObject("SRV_ORD_LIST");
            JSONArray SRV_ORD = SRV_ORD_LIST.getJSONArray("SRV_ORD");
            JSONObject srvOrd = SRV_ORD.getJSONObject(0);
            String TRADE_ID = srvOrd.getString("TRADE_ID");
            logMap.put("orderNo", TRADE_ID);
            //入库日志信息
            commonService.insertLogInfo(logMap);
            //入库订单信息，并启流程
            Map<String, Object> retMap = insertOrderInfo.insertOrderInfo(request);
            if (MapUtils.getBoolean(retMap, "success")){
                ret = CreateCloudNetWorkReponseUntil.createRespone("ApplyOrder", "0", MapUtils.getString(retMap, "msg"));
            }else{
                ret = CreateCloudNetWorkReponseUntil.createRespone("ApplyOrder", "1", MapUtils.getString(retMap, "msg"));
            }
        }catch(Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>云组网收单接口处理异常:{}", e.getMessage());
            ret = CreateCloudNetWorkReponseUntil.createRespone("ApplyOrder", "1", "收单接口处理失败:" + e.getMessage());
        }
        //更新返回信息到日志记录表
        if (!StringUtils.isEmpty(ret)){
            commonService.updateLogInfo(ret, MapUtils.getString(logMap, "id"));
        }
        return ret;
    }
}