package com.zres.project.localnet.portal.webservice.provinceRes;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealService;
import com.zres.project.localnet.portal.logInfo.service.ResInterfaceLogger;
import com.zres.project.localnet.portal.util.XmlUtil;
import com.zres.project.localnet.portal.webservice.data.dao.InterfaceBoDao;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.zres.project.localnet.portal.webservice.dto.JiKeProdAttrDTO;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

/**
 * @ClassName ProvinceResOrderService
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/8/11 16:42
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@Service
public class ProvinceResOrderService implements ProvinceResOrderServiceIntf {
    private static Logger logger = LoggerFactory.getLogger(ProvinceResOrderService.class);
    @Autowired
    private XmlUtil xmlUtil;
    @Autowired
    private InterfaceBoDao interfaceBoDao;
    @Autowired
    private ResInterfaceLogger resInterfaceLogger;
    @Autowired
    private OrderDealService orderDealService;
    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private OrderDealDao orderDealDao;

    @Override
    public Map<String, Object> provinceResOrderService(@RequestParam Map<String,Object> params) {
        logger.info("资源配置页面接口开始！" + params);
        String json = "";
        Map retmap = new HashMap();
        try {
            //生成json报文
            json = createJson(params);
            logger.info("发送报文：" + json);
        } catch (Exception e) {
            logger.error("接口交互异常！异常信息：" + e.getMessage(), e);
            retmap.put("success", false);
            retmap.put("message", "接口交互异常: " + e.getMessage());
            return retmap;
        }
        retmap.put("success", true);
        retmap.put("message", "省内DIA自动开通开通单发送成功");
        return retmap;
    }
    private String createJson(Map<String,Object> map){

        //拼接请求报文
        String jsonReqStr = "";
        Map<String, Object> constructResponse = new HashMap<>();
        Map<String, Object> retMap = new HashMap<>();
        Map<String, Object> logInfo = new HashMap<>();
        String url = wsd.queryUrl("provinceResOrder");
        String srvOrdId = MapUtils.getString(map, "SRV_ORD_ID");
        String cstOrdId = MapUtils.getString(map, "CST_ORD_ID");
        String tradeType = MapUtils.getString(map, "TRADE_TYPE_CODE");
        String woId = MapUtils.getString(map, "WO_ID");
        logInfo.put("url", url);
        logInfo.put("srvOrdId",srvOrdId);
        String requestJson = "";
        String sendJson = "";
        String response = "";
        try{
                //通过单号查出原报文日志表报文
                List<Map<String, Object>> logList = wsd.queryInterfaceLog(cstOrdId,srvOrdId);
                for (Map interfaceLog :logList) {
                    String content = MapUtils.getString(interfaceLog, "CONTENT");
                    String returnContent = MapUtils.getString(interfaceLog, "RETURN_CONTENT");
                    JSONObject returnJson = JSONObject.parseObject(returnContent);
                    JSONObject body = JSONObject.parseObject(returnJson.getString("UNI_BSS_BODY"));
                    JSONObject orderRespone = JSONObject.parseObject(body.getString("APPLY_ORDER_RSP"));
                    String  respCode = orderRespone.getString("RESP_CODE");
                    // 解析报文
                    JSONObject jsStr = JSONObject.parseObject(content);
                    String UNI_BSS_BODY = jsStr.getString("UNI_BSS_BODY");
                    JSONObject js_UNI_BSS_BODY = JSONObject.parseObject(UNI_BSS_BODY);
                    JSONObject js_APPLY_ORDER_REQ = JSONObject.parseObject(js_UNI_BSS_BODY.getString("APPLY_ORDER_REQ"));
                    JSONObject js_CST_ORD = JSONObject.parseObject(js_APPLY_ORDER_REQ.getString("CST_ORD"));
                    JSONObject js_CST_ORD_INFO = JSONObject.parseObject(js_CST_ORD.getString("CST_ORD_INFO"));
                    JSONObject js_SRV_ORD_LIST = JSONObject.parseObject(js_CST_ORD_INFO.getString("SRV_ORD_LIST"));
                    JSONArray js_SRV_ORD = JSON.parseArray(js_SRV_ORD_LIST.getString("SRV_ORD"));
                    JSONObject srvOrdJson = js_SRV_ORD.getJSONObject(0);
                    String tradeTypeCode = srvOrdJson.getString("TRADE_TYPE_CODE");
                    String tradeId = srvOrdJson.getString("TRADE_ID") != null? srvOrdJson.getString("TRADE_ID") : srvOrdId;
                    logInfo.put("tradeId", tradeId);
                    //正常新开并且返回成功报文 ACTIVE_TYPE TRADE_TYPE_CODE
                    if(tradeTypeCode.equals(tradeType) && "0".equals(respCode)){
                        requestJson = content;
                        break;
                    }
                }
                if(!StringUtils.isEmpty(requestJson)){
                    //查询电路信息对原报文增加相应得资源信息通过srvOrdId productType
                    Map jsStr = JSONObject.parseObject(requestJson,Map.class);
                    Map body = MapUtils.getMap(jsStr, "UNI_BSS_BODY");
                    Map applyOrderReq = MapUtils.getMap(body, "APPLY_ORDER_REQ");
                    Map cstOrd = MapUtils.getMap(applyOrderReq, "CST_ORD");
                    Map cstOrdInfo = MapUtils.getMap(cstOrd, "CST_ORD_INFO");
                    Map srvOrdList = MapUtils.getMap(cstOrdInfo, "SRV_ORD_LIST");
                    List<Map<String,Object>> srvOrd = (List<Map<String,Object>>) MapUtils.getObject(srvOrdList, "SRV_ORD");
                    for (Map<String,Object> obj:srvOrd) {
                        obj.put("WO_ID", woId);
                    }
                    /*
                     * 标准地址研发，查询每个电路对应的是否传递了标准地址，没有的话取电路属性中的标准地址
                     * 这里采取的操作是先解析进行判断，如果集客没有下发并且我们调度系统录入了标准地址，则重新堆报文进行修改，添加标准地址属性
                     * begin
                     * @Return:
                     */

                    boolean isContainAddress = false;//集客下发的报文是否包含标准地址
                    boolean isUpdate = false;//是否需要对报文进行更新
                    JSONArray js_SRV_ORD_NEW = new JSONArray();
                    JSONArray js_SRV_ORD = (JSONArray)MapUtils.getObject(srvOrdList, "SRV_ORD");
                    Iterator it = js_SRV_ORD.iterator();
                    JSONObject jsonObjectProdInfoNew = new JSONObject();
                    while (it.hasNext()) {
                        JSONObject jsonObjectProdInfo = (JSONObject) it.next();
                        String serialNumber = jsonObjectProdInfo.getString("SERIAL_NUMBER");
                        String tradeIdReal = jsonObjectProdInfo.getString("TRADE_ID");
                        String serviceOfferId = jsonObjectProdInfo.getString("SERVICE_OFFER_ID");
                        List<Map<String, Object>> orderList = orderDealService.queryOrderList(tradeIdReal, serialNumber);
                        if (orderList != null && orderList.size() > 0) {
                            srvOrdId = orderList.get(0).get("SRV_ORD_ID").toString();
                            //判断报文中是否含有标准地址，如果包含直接使用，如果不包含直接从电路属性中获取
                            JSONObject js_SRV_ORD_INFO = JSONObject.parseObject(jsonObjectProdInfo.getString("SRV_ORD_INFO"));
                            JSONArray js_SRV_ATTR_INFO = JSON.parseArray(js_SRV_ORD_INFO.getString("SRV_ATTR_INFO"));
                            Iterator it1 = js_SRV_ATTR_INFO.iterator();
                            List<JiKeProdAttrDTO> jiKeProdAttrDTOList = new ArrayList<>();
                            while (it1.hasNext()) {
                                JSONObject jsonObjectProdAttr = (JSONObject) it1.next();
                                Map prodAttrInfoMapTemp = jsonObjectProdAttr;
                                String attrCodeJson = MapUtils.getString(prodAttrInfoMapTemp,"ATTR_CODE");
                                String attrValueJson = MapUtils.getString(prodAttrInfoMapTemp,"ATTR_VALUE");
                                if ("10002217".equals(attrCodeJson)){
                                    isContainAddress = true;
                                }
                            }
                            if (!isContainAddress){//不包含标准地址，需要获取调度系统录入的标准地址
                                String attrValue = orderDealDao.qryAttrValue(srvOrdId, "标准地址", "10002217");
                                Map<String, Object> tempMap = new HashMap<>();
                                if(attrValue !=null && !"".equals(attrValue)){
                                    isUpdate = true;
                                    tempMap.put("ATTR_VALUE", attrValue);
                                    tempMap.put("ATTR_ACTION", 0);
                                    tempMap.put("ATTR_CODE", "10002217");
                                    js_SRV_ATTR_INFO.add(tempMap);
                                    js_SRV_ORD_INFO.put("SRV_ATTR_INFO",js_SRV_ATTR_INFO);
                                    jsonObjectProdInfoNew.putAll(jsonObjectProdInfo);
                                    jsonObjectProdInfoNew.put("SRV_ORD_INFO",js_SRV_ORD_INFO);
                                    js_SRV_ORD_NEW.add(jsonObjectProdInfoNew);
                                }
                            }
                        } else {
                            retMap.put("success",false);
                            retMap.put("message","电路信息为空！！请排查");
                        }
                    }
                    if (!isContainAddress && isUpdate){
                        srvOrdList.put("SRV_ORD", js_SRV_ORD_NEW);
                        cstOrdInfo.put("SRV_ORD_LIST",srvOrdList);
                        cstOrd.put("CST_ORD_INFO",cstOrdInfo);
                        applyOrderReq.put("CST_ORD",cstOrd);
                        body.put("APPLY_ORDER_REQ", applyOrderReq);
                        jsStr.put("UNI_BSS_BODY", body);
                    }
                    //标准地址 end

                    sendJson = JSONObject.toJSONString(jsStr);
                    constructResponse = xmlUtil.sendHttpPostOrderCenter(url, sendJson);
                    String code = MapUtils.getString(constructResponse,"code","");
                    if("200".equals(code)){
                        response = MapUtils.getString(constructResponse,"msg");
                        JSONObject respJson = JSONObject.parseObject(response);
                        //0：成功；1：失败，报文错误；
                        Map respBody = JSONObject.parseObject(respJson.getString("UNI_BSS_BODY"), Map.class);
                        Map<String,Object> respOrder = MapUtils.getMap(respBody,"APPLY_ORDER_RSP");
                        String respCode = MapUtils.getString(respOrder,"RESP_CODE");
                        String respDesc = MapUtils.getString(respOrder,"RESP_DESC");
                        if("0".equals(respCode)){
                            retMap.put("success",true);
                            retMap.put("message",respDesc);
                        } else{
                            retMap.put("success",false);
                            retMap.put("message",respDesc);
                        }
                    } else{
                        retMap.put("success",false);
                        retMap.put("message","接口交互失败：资源返回 "+code);
                    }
                }else{
                    retMap.put("success", false);
                    retMap.put("message", "请检查是否有对应的接口记录" );
                }
                logInfo.put("request", sendJson);
                logInfo.put("respone", JSONObject.toJSONString(constructResponse));
        } catch (Exception e) {
            retMap.put("success", false);
            retMap.put("message", "接口交互失败："+e.getMessage());
            logInfo.put("request", requestJson);
            logInfo.put("respone", JSONObject.toJSONString(retMap));
            logger.debug("接口交互失败："+e.getMessage());
        }finally{
            saveEventJson(logInfo);
        }
        return JSONObject.toJSONString(retMap);
    }

        /**
         * @Description 功能描述: 记录接口日志
         * @Param: [tradeId, srvOrdId, request, respone]
         * @Return: void
         * @Author: wang.gang2
         * @Date: 2020/9/19 16:04
         */
        private void saveEventJson( Map<String, Object> logInfo){
                //String tradeId,String srvOrdId,String request,String respone){
            Map<String,Object> interflog = new HashMap<String, Object>();
            interflog.put("INTERFNAME","省份dia自动开通资源配置接口开通单");
            interflog.put("URL", MapUtils.getString(logInfo,"url"));
            interflog.put("CONTENT", MapUtils.getString(logInfo,"request"));
            interflog.put("RETURNCONTENT", MapUtils.getString(logInfo,"respone"));
            interflog.put("ORDERNO", MapUtils.getString(logInfo,"tradeId"));
            interflog.put("SRV_ORD_ID", MapUtils.getString(logInfo,"srvOrdId"));
            interflog.put("REMARK","发送省份dia自动开通资源配置开通单 json报文");
            wsd.insertInterfLog(interflog);
        }

        //省分DIA自动开通，直接通过接口交互的方式进行提交
        public Map<String, Object> submitOrderToProvinceAuto(Map<String, Object> params){
            Map<String, Object> resMap = new HashMap<>();
            try{
                logger.info("和省分DIA接口直接交互！" + params);
                String url = wsd.queryUrl("provinceResOrder");
                //处理请求报文
                String json = createJson(params);
                resMap =JSONObject.parseObject(json);
            }catch(Exception e){
                logger.error("拼接报文异常！异常信息：" + e.getMessage(), e);
                resMap.put("success", false);
                resMap.put("message", "拼接报文异常: " + e.getMessage());
            }
            return resMap;
    }

}
