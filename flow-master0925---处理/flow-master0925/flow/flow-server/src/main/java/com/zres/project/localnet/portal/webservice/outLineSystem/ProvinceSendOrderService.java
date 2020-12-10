package com.zres.project.localnet.portal.webservice.outLineSystem;

import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.logInfo.dao.LoggerInfoDao;
import com.zres.project.localnet.portal.logInfo.entry.ResInterfaceLog;
import com.zres.project.localnet.portal.util.OrderTrackOperType;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import net.sf.json.JSONArray;
import org.apache.commons.collections.MapUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 省份IP智能网管系统派单接口
 *
 * @author wangsen
 * @date 2020/10/16 11:32
 * @return
 */
@Service
public class ProvinceSendOrderService implements ProvinceSendOrderServiceIntf {
    private static final Logger logger = Logger.getLogger(ProvinceSendOrderService.class);

    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private LoggerInfoDao loggerInfoDao;

    @Override
    public Map<String, Object> sendOrder(Map<String, Object> map) {
        String text = "";
        // 解析返回报文
        String respCode = "";
        String respDesc = "";
        String wo_id = MapUtils.getString(map, "woId", ""); //工单
        String srvOrdId = MapUtils.getString(map, "srvOrdId", ""); //工单
        String type = MapUtils.getString(map, "type", ""); //调接口的方式：  listener监听发起， rescive 界面（重新激活）按钮发起
        if ("listener".equals(type)) {
            text = "激活";
        }
        if ("rescive".equals(type)) {
            text = "重新激活";
        }
        String special_type = orderDealDao.querySpecName(wo_id); //工单查询专业
        List<Map<String, Object>> list = orderDealDao.queryWoInfo(wo_id);
        map.putAll(list.get(0));
        Map<String, Object> returnMap = new HashMap<>();
        try {
            String reqJsonStr = this.createSendOrderJsonStr(map); //拼装入参报文
            logger.info("------调用省份IP智能网管系统激活派单接口请求报文---reqJsonStr:" + reqJsonStr);
            //TODO 配置表补充接口地址
            String url = queryUrl(map); // 获取省份IP智能网管系统接口-url
        //    String url = wsd.queryUrl("provinceSendOrder"); // 获取省份IP智能网管系统接口-url
            logger.info("------获取省份IP智能网管系统接口-url:" + url);
            /**
             * 调用省份IP智能网管系统接口，将报文发往省份IP智能网管系统将工单激活派单处理
             * @author wangsen
             * @date 2020/10/21 14:46
             */
            Map<String, Object> resJsonstr = this.sendOrderToProvinceOrderCenter(url, reqJsonStr);
            logger.info("------调用省份IP智能网管系统激活派单接口返回报文---resJsonstr: " + reqJsonStr);
            // 插入接口日志记录
            ResInterfaceLog resInterfaceLog = new ResInterfaceLog();
            resInterfaceLog.setInterfName("省份IP智能网管系统" + text + "派单接口请求json报文-reqJsonStr");
            resInterfaceLog.setUrl(url);
            resInterfaceLog.setContent(reqJsonStr);
            resInterfaceLog.setOrderNo(srvOrdId);
            resInterfaceLog.setReturnContent(resJsonstr.toString());
            resInterfaceLog.setRemark("省份IP智能网管系统" + text + "派单接口返回json报文-resJsonstr");
            loggerInfoDao.saveResInterfaceInfo(resInterfaceLog); //写入操作日志记录
            if ("200".equals(resJsonstr.get("code"))) {
                JSONObject msg = JSONObject.parseObject(resJsonstr.get("msg").toString());
                String uniBssBody = msg.getString("UNI_BSS_BODY");
                JSONObject jsUniBssBody = JSONObject.parseObject(uniBssBody);
                JSONObject provinceActivateOrderRsp = jsUniBssBody.getJSONObject("PROVINCE_ACTIVE_ORDER_RSP");
                if (provinceActivateOrderRsp != null) {
                    // 处理结果 0：成功； 1：失败，报文错误； 2：失败，附件没有
                    respCode = provinceActivateOrderRsp.getString("RESP_CODE");
                    // 处理原因
                    respDesc = provinceActivateOrderRsp.getString("RESP_DESC");
                }
                else {
                    respCode = "-1";
                    respDesc = "返回报文无PROVINCE_ACTIVE_ORDER_RSP，请联系管理员！";
                }
            }
            else {
                respCode = "-1";
                respDesc = "返回报文格式错误，请联系管理员！";
            }
        }
        catch (Exception e) {
            logger.error(e.getMessage());
        }
        try {
            Map<String, Object> actSendMap = new HashMap<>();
            actSendMap.put("specName", special_type);
            actSendMap.put("woId", wo_id);
            actSendMap.put("feedSystem", "省份IP智能网管系统"); //省份IP智能网管系统
            actSendMap.put("activateCode", respCode);
            actSendMap.put("activateDesc", respDesc);
            wsd.saveActivateInfo(actSendMap); //写入激活日志记录，
            if ("0".equals(respCode)) { //派单成功，修改工单状态为待外系统回单，失败，则保持原状态不变，激活回单需要校验工单状态是否为待外系统回单
                orderDealDao.updateWoStateByWoId(wo_id, OrderTrackOperType.WO_ORDER_STATE_18);
            }
            returnMap.put("RESP_CODE", respCode);
            returnMap.put("RESP_DESC", respDesc);
            logger.info("-------省份IP智能网管系统返回激活派单结果---respCode:" + respCode + "--" + respDesc);
        }
        catch (Exception e) {
            logger.error(e.getMessage());
        }
        return returnMap;
    }

    /**
     * 查询接口地址，不同省份对应不同的系统
     * 目前广东对应互联网综合网管系统
     * @param params
     * @return
     */
    private String queryUrl(Map<String,Object> params) {
        String handleDepId = MapUtils.getString(params,"HANDLE_DEP_ID","");
        String url = "";
        if("8".equals(handleDepId)){
            //TODO 广东 接口地址还未提供，需要配置数据
            url = wsd.queryUrl("provinceSendOrderGD");
        }else {
            url = wsd.queryUrl("provinceSendOrder"); // 获取省份IP智能网管系统接口-url
        }

        return url;
    }

    /**
     * 调用省份IP智能网管系统派发工单激活
     * @author wangsen
     * @date 2020/10/21 14:41
     * @param  url 接口地址
     * @param  reqJsonStr 派单报文
     * @return
     */
    private Map<String, Object> sendOrderToProvinceOrderCenter(String url, String reqJsonStr) {
        String responseContent = null;
        HttpClient httpClient = null;
        Map<String, Object> map = new HashMap<>();
        try {
            httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
            httpPost.setEntity(new StringEntity(reqJsonStr, "utf-8"));
            HttpResponse response = httpClient.execute(httpPost);
            logger.info(response.getStatusLine().getStatusCode() + "\n");
            HttpEntity entity = response.getEntity();
            responseContent = EntityUtils.toString(entity, "UTF-8");
            logger.info(responseContent);
            map.put("msg", responseContent);
            map.put("code", "200");
        }
        catch (Exception e) {
            logger.error("接口调用失败，请联系管理员" + e.getMessage(), e);
            map.put("msg", "接口调用失败，请联系管理员！");
            map.put("code", "300");
        }
        finally {
            httpClient.getConnectionManager().shutdown();
        }
        return map;
    }

    /**
     * 拼接发单请求报文
     *
     * @param map
     * @return
     */
    private String createSendOrderJsonStr(Map<String, Object> map) {
        JSONObject ORDER_INFO = new JSONObject();
        String woId = MapUtils.getString(map, "woId", "");
        String tradeId = MapUtils.getString(map, "TRADE_ID", "");
        String serialNumber = MapUtils.getString(map, "SERIAL_NUMBER", "");
        String attempType = MapUtils.getString(map, "ACTIVE_TYPE", "");
        String subscribeId = MapUtils.getString(map, "SUBSCRIBE_ID", "");
        String srvOrderId = MapUtils.getString(map, "SRV_ORD_ID", "");

        //订单信息参数
        ORDER_INFO.put("WO_ID", woId);
        ORDER_INFO.put("SUBSCRIBE_ID", subscribeId);
        ORDER_INFO.put("TRADE_ID", tradeId);
        ORDER_INFO.put("SERIAL_NUMBER", serialNumber);
        ORDER_INFO.put("ATTEMP_TYPE", attempType);

        //广东特有部分参数，暂不处理，全部送空
        JSONObject gdparam = new JSONObject();
        gdparam.put("CIRCUIT_NUMBER", "");
        gdparam.put("FULL_ROUTE", "");
        gdparam.put("AS_NAME", "");
        gdparam.put("PENETRATION_MODE", "");
        gdparam.put("FREE_IPV4", "");
        gdparam.put("FREE_IPV6", "");
        gdparam.put("CHARGE_IP", "");
        gdparam.put("EIGHT_PORT", "");
        gdparam.put("RECORD_NUMBER", "");
        gdparam.put("ROOM_NAME", "");
        gdparam.put("REMARKS", "");
        gdparam.put("IPV4_CHANGE_TYPE", "");
        gdparam.put("IPV6_CHANGE_TYPE", "");
        gdparam.put("ADD_BUSINESS_IPV4", "");
        gdparam.put("ADD_BUSINESS_IPV6", "");
        gdparam.put("REDUCE_IPV4_NUMBER", "");
        gdparam.put("REDUCE_IPV6_NUMBER", "");
        gdparam.put("REDUCE_IPV4_LIST", "");
        gdparam.put("REDUCE_IPV6_LIST", "");
        gdparam.put("ADD_AFTER_BUSINESS_IPV4", "");
        gdparam.put("ADD_AFTER_BUSINESS_IPV6", "");
        gdparam.put("NODE_ID", "");
        gdparam.put("NODE_NAME", "");
        gdparam.put("SUBINTERFACE_NO", "");
        gdparam.put("VRRP_NO", "");
        gdparam.put("BUSSINESS_IP", "");
        gdparam.put("RELAY_IP", "");
        gdparam.put("ACCESS_SWITCH_DEV_NAME", "");
        gdparam.put("ACCESS_SWITCH_IP", "");
        gdparam.put("ACCESS_SWITCH_UP_PORT", "");
        gdparam.put("ACCESS_SWITCH_DEV_TRUNK", "");
        gdparam.put("ACCESS_SWITCH_PORT_NAME", "");
        gdparam.put("BUSSINESS_GATEWAY_NAME2", "");
        gdparam.put("BUSSINESS_GATEWAY_IP2", "");
        gdparam.put("BUSSINESS_GATEWAY_PORT2", "");
        gdparam.put("BUSSINESS_PORT_NAME2", "");
        gdparam.put("PASS_SWITCH", "");
        gdparam.put("PASS_SWITCH_IP", "");
        gdparam.put("PASS_SWITCH_PORT", "");
        gdparam.put("UNIT_NAME", "");
        gdparam.put("APPLICANT_TYPE", "");
        gdparam.put("UNIT_PROPERTY", "");
        gdparam.put("DOCUMENT_TYPE", "");
        gdparam.put("IDENTIFICATION_NUMBER", "");
        gdparam.put("UNIT_CLASSIFICATION", "");
        gdparam.put("ORGANIZER_TRADE_CLASSIFY", "");
        gdparam.put("APPLICATION_SERVICE_TYPE", "");
        gdparam.put("CONTACT_NAME", "");
        gdparam.put("CONTACT_TEL", "");
        gdparam.put("CONTACT_MAIL", "");
        gdparam.put("UNIT_PROVINCE", "");
        gdparam.put("UNIT_CITY", "");
        gdparam.put("UNIT_AREA", "");
        gdparam.put("UNIT_DETAILED_ADDRESS", "");
        gdparam.put("IP_ADDRESS_USAGE", "");
        ORDER_INFO.putAll(gdparam);

        // TODO 资源返回参数查询，目前很多参数缺失，后续补充
        JSONObject resparam = new JSONObject();
        String[] resCode = {"10001107"}; //code值需要补充，按顺序取code值查询出value
        //resparam.put("PORT_TYPE", orderDealDao.queryResInfo(srvOrderId, resCode[0]) == null ? "" : orderDealDao.queryResInfo(srvOrderId, resCode[0]));
        resparam.put("ACCESS_DEVICE_NAME",  orderDealDao.queryResInfo(srvOrderId, resCode[0]) == null ? "" : orderDealDao.queryResInfo(srvOrderId, resCode[0]));
        resparam.put("ACCESS_DEVICE_IP",  orderDealDao.queryResInfo(srvOrderId, resCode[0]) == null ? "" : orderDealDao.queryResInfo(srvOrderId, resCode[0]));
        resparam.put("ACCESS_DEVICE_PORT",  orderDealDao.queryResInfo(srvOrderId, resCode[0]) == null ? "" : orderDealDao.queryResInfo(srvOrderId, resCode[0]));
        resparam.put("ACCESS_MODE_NAME",  orderDealDao.queryResInfo(srvOrderId, resCode[0]) == null ? "" : orderDealDao.queryResInfo(srvOrderId, resCode[0]));
        resparam.put("BUSSINESS_GATEWAY_NAME1",  orderDealDao.queryResInfo(srvOrderId, resCode[0]) == null ? "" : orderDealDao.queryResInfo(srvOrderId, resCode[0]));
        resparam.put("BUSSINESS_GATEWAY_IP1",  orderDealDao.queryResInfo(srvOrderId, resCode[0]) == null ? "" : orderDealDao.queryResInfo(srvOrderId, resCode[0]));
        resparam.put("BUSSINESS_GATEWAY_PORT1",  orderDealDao.queryResInfo(srvOrderId, resCode[0]) == null ? "" : orderDealDao.queryResInfo(srvOrderId, resCode[0]));
        resparam.put("BUSSINESS_PORT_NAME1", orderDealDao.queryResInfo(srvOrderId, resCode[0]) == null ? "" : orderDealDao.queryResInfo(srvOrderId, resCode[0]));
        resparam.put("V4PortIP",  orderDealDao.queryResInfo(srvOrderId, resCode[0]) == null ? "" : orderDealDao.queryResInfo(srvOrderId, resCode[0]));
        resparam.put("V6PortIP",  orderDealDao.queryResInfo(srvOrderId, resCode[0]) == null ? "" : orderDealDao.queryResInfo(srvOrderId, resCode[0]));
        resparam.put("V4RouteIP", orderDealDao.queryResInfo(srvOrderId, resCode[0]) == null ? "" : orderDealDao.queryResInfo(srvOrderId, resCode[0]));
        resparam.put("V6RouteIP",  orderDealDao.queryResInfo(srvOrderId, resCode[0]) == null ? "" : orderDealDao.queryResInfo(srvOrderId, resCode[0]));
        ORDER_INFO.putAll(resparam);

        // TODO 省份号线返回参数 , sql需要补充
        String[] hxCode = {"10001107"};  //code值需要补充，根据顺序取code查询value ，sql需要补充
        JSONObject hxparam = new JSONObject();
        hxparam.put("OLTCODE", orderDealDao.queryHaoxInfo(srvOrderId, hxCode[0]) == null ? "" : orderDealDao.queryHaoxInfo(srvOrderId, resCode[0]));
        hxparam.put("VLAN_NO",  orderDealDao.queryHaoxInfo(srvOrderId, hxCode[0]) == null ? "" : orderDealDao.queryHaoxInfo(srvOrderId, resCode[0]));
        hxparam.put("CVLAN",  orderDealDao.queryHaoxInfo(srvOrderId, hxCode[0]) == null ? "" : orderDealDao.queryHaoxInfo(srvOrderId, resCode[0]));
        ORDER_INFO.putAll(hxparam);

        // 集客返回参数查询
        JSONObject jkparam = new JSONObject();
        String[] jkCode = {"10000192", "10001110", "10002200", "10000930", "10001107"};
        jkparam.put("ACCESS_TYPE", orderDealDao.queryJikeInfo(srvOrderId, jkCode[0]) == null ? "" : orderDealDao.queryJikeInfo(srvOrderId, jkCode[0]));
        jkparam.put("BIND_WIDTH", orderDealDao.queryJikeInfoEnumValue(srvOrderId, jkCode[1]) == null ? "" : orderDealDao.queryJikeInfoEnumValue(srvOrderId, jkCode[1]));
        jkparam.put("OUT_BIND_WIDTH", orderDealDao.queryJikeInfo(srvOrderId, jkCode[2]) == null ? "" : orderDealDao.queryJikeInfo(srvOrderId, jkCode[2]));
        jkparam.put("IP_PROTOCOL_VERSIONS", orderDealDao.queryJikeInfo(srvOrderId, jkCode[3]) == null ? "" : orderDealDao.queryJikeInfo(srvOrderId, jkCode[3]));
        jkparam.put("PORT_TYPE", orderDealDao.queryJikeInfo(srvOrderId, jkCode[3]) == null ? "" : orderDealDao.queryJikeInfo(srvOrderId, jkCode[3]));


        //查询客户信息
        List<Map<String, Object>> listCust = orderDealDao.queryCustInfo(srvOrderId);
        if (listCust != null && !listCust.isEmpty()) {
            jkparam.put("CUST_NAME", MapUtils.getString(listCust.get(0), "CUST_NAME_CHINESE", ""));
            jkparam.put("CUST_LINKMAN", MapUtils.getString(listCust.get(0), "CUST_CONTACT_MAN_NAME", ""));
            jkparam.put("CUST_LINKMAN_PHONE", MapUtils.getString(listCust.get(0), "CUST_CONTACT_MAN_TEL", ""));
            jkparam.put("CUST_SETUP_ADDR", MapUtils.getString(listCust.get(0), "CUST_ADDRESS", ""));
        }
        else {
            jkparam.put("CUST_NAME", "");
            jkparam.put("CUST_LINKMAN", "");
            jkparam.put("CUST_LINKMAN_PHONE", "");
            jkparam.put("CUST_SETUP_ADDR", "");
        }
        ORDER_INFO.putAll(jkparam);

        // TODO IPLIST参数 查询sql需要补充
        List<Map<String, Object>> ipList = orderDealDao.queryIplistInfo(srvOrderId);
        JSONArray jsonArray = new JSONArray();
        if (ipList != null && !ipList.isEmpty()) {
            for (int i = 0; i < ipList.size(); i++) {
                JSONObject ipJson = new JSONObject();
                Map<String, Object> mapIp = ipList.get(i);
                ipJson.put("UserIP", MapUtils.getString(mapIp, "UserIP", ""));
                ipJson.put("UserGateway", MapUtils.getString(mapIp, "UserGateway", ""));
                ipJson.put("UserIPType", MapUtils.getString(mapIp, "UserIPType", ""));
                ipJson.put("UserIPv6PDprefix", MapUtils.getString(mapIp, "UserIPv6PDprefix", ""));
                ipJson.put("IPv6PDprefixNUM", MapUtils.getString(mapIp, "IPv6PDprefixNUM", ""));
                JSONObject ipInfoJson = new JSONObject();
                ipInfoJson.put("IPINFO", ipJson);
                jsonArray.add(ipInfoJson);
            }
        }
        else {
            JSONObject ipJson = new JSONObject();
            ipJson.put("UserIP", "");
            ipJson.put("UserGateway", "");
            ipJson.put("UserIPType", "");
            ipJson.put("UserIPv6PDprefix", "");
            ipJson.put("IPv6PDprefixNUM", "");
            JSONObject ipInfoJson = new JSONObject();
            ipInfoJson.put("IPINFO", ipJson);
            jsonArray.add(ipInfoJson);
        }
        ORDER_INFO.put("IPLIST", jsonArray);

        //预留参数
        JSONObject param = new JSONObject();
        param.put("PARA_ID", "");
        param.put("PARA_VALUE", "");

        JSONObject PROVINCE_ACTIVE_ORDER_REQ = new JSONObject();
        PROVINCE_ACTIVE_ORDER_REQ.put("PARA", param);
        PROVINCE_ACTIVE_ORDER_REQ.put("ORDER_INFO", ORDER_INFO);

        JSONObject UNI_BSS_BODY = new JSONObject();
        UNI_BSS_BODY.put("PROVINCE_ACTIVE_ORDER_REQ", PROVINCE_ACTIVE_ORDER_REQ);

        JSONObject json = new JSONObject();
        json.put("UNI_BSS_BODY", UNI_BSS_BODY);

        logger.info("拼装完成的参数：" + json.toString());
        return json.toString();
    }
}

