package com.zres.project.localnet.portal.cloudNetWork.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.cloudNetWork.CloudRentOrderServiceIntf;
import com.zres.project.localnet.portal.cloudNetWork.util.CreateCloudNetWorkReponseUntil;
import com.zres.project.localnet.portal.cloudNetworkFlow.BssInterfaceServiceIntf;
import org.apache.axis.utils.StringUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Service
public class CloudRentOrderService implements CloudRentOrderServiceIntf {
    private static final Logger logger = LoggerFactory.getLogger(CloudRentOrderService.class);

    @Autowired
    private CloudNetCommonService commonService;
    @Autowired
    private BssInterfaceServiceIntf bssInterfaceServiceIntf;

    /*SimpleDateFormat dfStr = new SimpleDateFormat("yyyyMMddHHmmss"); //设置日期格式
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置日期格式*/

    @Override
    public String startAndStopRentOrder(String request) {
        logger.info("------进入--------------------云组网业务------起止租接口-----------------------" + request);
        String ret = "";
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("interfName", "云组网起止租接口");
        logMap.put("url", "/cloudNetWork/interfaceBDW/rentOrder.spr");
        logMap.put("content", request);
        logMap.put("remark", "接收云组网起止租报文");

        try {
            //报文解析
            JSONObject json = JSON.parseObject(request);
            JSONObject UNI_BSS_BODY = json.getJSONObject("UNI_BSS_BODY");
            JSONObject YZWRENT_ORDER_REQ = UNI_BSS_BODY.getJSONObject("YZWRENT_ORDER_REQ");
            JSONObject ROUTING = YZWRENT_ORDER_REQ.getJSONObject("ROUTING");
            JSONObject ROUTE_TYPE = ROUTING.getJSONObject("ROUTE_TYPE");
            JSONObject ROUTE_VALUE = ROUTING.getJSONObject("ROUTE_VALUE");
            JSONObject PARA = YZWRENT_ORDER_REQ.getJSONObject("PARA");
            JSONObject PARA_ID = PARA.getJSONObject("PARA_ID");
            JSONObject PARA_VALUE = PARA.getJSONObject("PARA_VALUE");

            JSONObject CST_ORD = YZWRENT_ORDER_REQ.getJSONObject("CST_ORD");
            JSONObject SUBSCRIBE_ID = CST_ORD.getJSONObject("SUBSCRIBE_ID");
            JSONObject SRV_ORD_LIST = CST_ORD.getJSONObject("SRV_ORD_LIST");
            JSONArray SRV_ORD = SRV_ORD_LIST.getJSONArray("SRV_ORD");
            Iterator iterator = SRV_ORD.iterator();
            while (iterator.hasNext()) {
                JSONObject srvOrder = (JSONObject) iterator.next();
                String tradeTypeCode = srvOrder.getString("TRADE_TYPE_CODE");
                String rentDate = srvOrder.getString("RENT_DATE");
                String serialNumber = srvOrder.getString("SERIAL_NUMBER");
                String tradeId = srvOrder.getString("TRADE_ID");
                String flowId = srvOrder.getString("FLOW_ID");
                String remark = srvOrder.getString("REMARK");
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("serialNumber", serialNumber);
                paramMap = bssInterfaceServiceIntf.rentFinshOrder(paramMap);
                if (MapUtils.getBoolean(paramMap, "success")){
                    ret = CreateCloudNetWorkReponseUntil.createRespone("RentOrder", "0", MapUtils.getString(paramMap, "message"));
                }else{
                    ret = CreateCloudNetWorkReponseUntil.createRespone("RentOrder", "1", MapUtils.getString(paramMap, "message"));
                }
                logMap.put("orderNo", serialNumber);
                //入库日志信息
                commonService.insertLogInfo(logMap);
            }
        } catch (Exception e) {
            logger.info(">>>>>>>>>>>>>>>>>>>>>云组网业务起止租接口处理异常:{}", e.getMessage());
            ret = CreateCloudNetWorkReponseUntil.createRespone("RentOrder", "1", "起止租接口处理失败:" + e.getMessage());
        }
        //更新返回信息到日志记录表
        if (!StringUtils.isEmpty(ret)){
            commonService.updateLogInfo(ret, MapUtils.getString(logMap, "id"));
        }
        return ret;
    }

    /*public Map rentOrder(String request) {
        logger.info("------进入--------------------云组网业务------起止租接口-----------------------" + request);
        String ret = "";
        Map<String, Object> retMap = new HashMap();

        Map<String, Object> logMap = new HashMap<>();
        logMap.put("interfName", "云组网收单接口");
        logMap.put("url", "/cloudNetWork/interfaceBDW/receiveOrder.spr");
        logMap.put("content", request);
        logMap.put("remark", "接收云组网json报文");

        try {
            //报文解析
            JSONObject json = JSON.parseObject(request);
            JSONObject UNI_BSS_BODY = json.getJSONObject("UNI_BSS_BODY");
            JSONObject YZWRENT_ORDER_REQ = UNI_BSS_BODY.getJSONObject("YZWRENT_ORDER_REQ");
            JSONObject ROUTING = YZWRENT_ORDER_REQ.getJSONObject("ROUTING");
            JSONObject ROUTE_TYPE = ROUTING.getJSONObject("ROUTE_TYPE");
            JSONObject ROUTE_VALUE = ROUTING.getJSONObject("ROUTE_VALUE");
            JSONObject PARA = YZWRENT_ORDER_REQ.getJSONObject("PARA");
            JSONObject PARA_ID = PARA.getJSONObject("PARA_ID");
            JSONObject PARA_VALUE = PARA.getJSONObject("PARA_VALUE");

            JSONObject CST_ORD = YZWRENT_ORDER_REQ.getJSONObject("CST_ORD");
            JSONObject SUBSCRIBE_ID = CST_ORD.getJSONObject("SUBSCRIBE_ID");
            JSONObject SRV_ORD_LIST = CST_ORD.getJSONObject("SRV_ORD_LIST");
            JSONArray SRV_ORD = SRV_ORD_LIST.getJSONArray("SRV_ORD");
            Iterator iterator = SRV_ORD.iterator();
            while (iterator.hasNext()) {
                JSONObject srvOrder = (JSONObject) iterator.next();
                String tradeTypeCode = srvOrder.getString("TRADE_TYPE_CODE");
                String rentDate = srvOrder.getString("RENT_DATE");
                String serialNumber = srvOrder.getString("SERIAL_NUMBER");
                String tradeId = srvOrder.getString("TRADE_ID");
                String flowId = srvOrder.getString("FLOW_ID");
                String remark = srvOrder.getString("REMARK");
                //TODO：调接口

                if (MapUtils.getBoolean(retMap, "success")){
                    ret = CreateResponeUtil.createRespone("RentOrder", "0", MapUtils.getString(retMap, "msg"));
                }else{
                    ret = CreateResponeUtil.createRespone("RentOrder", "1", MapUtils.getString(retMap, "msg"));
                }
                logMap.put("orderNo", serialNumber);
                //入库日志信息
                commonService.insertLogInfo(logMap);
                *//*String srvOrdId = orderDealService.selectSrvOrdId(subscribeId,serialNumber,tradeId);
                interflog.put("ORDERNO",srvOrdId);
                if(StringUtils.isEmpty(srvOrdId)){
                    flag = false;
                    retMap = wrapRespMap("1","无效的srvOrdId");
                    break;
                }
                Thread.sleep(1000*60); //设置推迟时间
                Map<String,Object> woInfoMap =interfaceBoDao.queryWoInfo(srvOrdId);
                Map<String,Object> param = new HashMap<>();
                param.put("woId", MapUtils.getString(woInfoMap,"WO_ID"));
                param.put("orderId", MapUtils.getString(woInfoMap,"ORDER_ID"));
                listenerOrderServiceIntf.finshStartStopRent(param);*//*

                //报文入库
                *//*Map<String, Object> rmap = new HashMap<String, Object>();
                rmap.put("srv_ord_id", srvOrdId);
                rmap.put("attr_action", "RentOrder");
                rmap.put("attr_code", "1");
                rmap.put("attr_name", "");
                rmap.put("attr_value", "");
                rmap.put("attr_value_name", "集客起止租接口返回结果");
                rmap.put("sourse", "jike");
                wsd.saveRetInfo(rmap);
                // 入库起租时间
                rmap.put("attr_action", "1");
                rmap.put("attr_code", "21100001");
                rmap.put("attr_name", "起租时间");
                rmap.put("attr_value",  df.format(dfStr.parse(rentDate)));
                rmap.put("attr_value_name", "");
                rmap.put("sourse", "jike");
                wsd.saveRetInfo(rmap);
                // 调用资源扩展接口更新起止租时间
                resCfsAttrUpdateServiceIntf.resRentTimeUpdate(srvOrdId,df.format(dfStr.parse(rentDate)));*//*
            }
        } catch (Exception e) {
            logger.info(">>>>>>>>>>>>>>>>>>>>>云组网业务起止租接口处理异常:{}", e.getMessage());
            ret = CreateResponeUtil.createRespone("RentOrder", "1", "起止租接口处理失败:" + e.getMessage());
        }
        //更新返回信息到日志记录表
        if (!StringUtils.isEmpty(ret)){
            commonService.updateLogInfo(ret, MapUtils.getString(logMap, "id"));
        }
        JSONObject jasonObject = JSONObject.parseObject(ret);
        retMap = (Map<String, Object>) jasonObject;

        return retMap;
    }*/
}
