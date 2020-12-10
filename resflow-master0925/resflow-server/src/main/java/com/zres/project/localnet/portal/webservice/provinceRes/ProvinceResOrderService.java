package com.zres.project.localnet.portal.webservice.provinceRes;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.CheckFeedbackDao;
import com.zres.project.localnet.portal.logInfo.entry.ResInterfaceLog;
import com.zres.project.localnet.portal.logInfo.service.ResInterfaceLogger;
import com.zres.project.localnet.portal.logInfo.until.LoggerThreadPool;
import com.zres.project.localnet.portal.util.XmlUtil;
import com.zres.project.localnet.portal.webservice.data.dao.InterfaceBoDao;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.res.frame.core.util.MapUtil;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.SimpleDateFormat;
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
    private WebServiceDao wsd;

    @Override
    public Map<String, Object> provinceResOrderService(@RequestParam Map<String,Object> params) {


        logger.info("资源配置页面接口开始！" + params);
        String json = "";
        String retStr = "";
        String url = wsd.queryUrl("provinceResOrder");
        params.put("url", url);
        Map retmap = new HashMap();

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("interfname", "省份dia自动开通资源配置页面接口");
        map.put("url", url);
        map.put("returncontent", "");
        map.put("orderno", MapUtils.getString(params, "SRV_ORD_ID")); //业务订单id
        map.put("remark", "");
        map.put("updatedate", "");
        try {
            //生成json报文
            json = createJson(params);
            String retStr1 = json.replace("\\n", "");
            String retStr2 = retStr1.replace("\\t", "");
            String retStr3 = retStr2.replace("\\r", "");
            retStr = retStr3.replace(" ", "");
            map.put("content", retStr);
            logger.info("发送报文：" + json);
        } catch (Exception e) {
            logger.error("拼接报文异常！异常信息：" + e.getMessage(), e);
            retmap.put("returncode", "失败");
            retmap.put("returndec", "拼接报文异常: " + e.getMessage());
            map.put("content", MapUtils.getString(retmap, "returndec"));
            map.put("remark", "拼接报文异常");

            //5.报文入库，数据入库
            this.saveEventJson(map);

            return retmap;
        }
        map.put("url", url);
        logger.info("资源接口地址：" + url);
        retmap.put("returncode", "成功");
        retmap.put("url", url);
        retmap.put("json", retStr);
        logger.info("资源配置页面接口结束---" + params);
        return retmap;
    }
    private String createJson(Map<String,Object> map){

        //拼接请求报文
        String jsonReqStr = "";
        logger.info("核查前期评估接口发送报文request：" + jsonReqStr);
        Map<String, Object> constructResponse = new HashMap<>();
        Map<String, Object> retMap = new HashMap<>();
        String sendJson = "";
        String requestJson = "";
        String tradeId = "";
        String response = "";
        List srvOrdsList = new ArrayList();
        try{
//            String srvOrdId = MapUtils.getString(map, "srvOrdId");
            List<Map<String,Object>> circuitData = (List<Map<String,Object>>) MapUtils.getObject(map, "circuitData");
            String cstOrdId = MapUtils.getString(map, "cstOrdId");

            for (int i = 0; i < circuitData.size(); i++) {
                Map<String, Object> circuitInfo = circuitData.get(i);
                String srvOrdId = MapUtils.getString(circuitInfo, "SRV_ORD_ID");
                String woId = MapUtils.getString(circuitInfo, "WO_ID");
                String productType =  MapUtils.getString(circuitInfo, "SERVICE_ID");
                //通过单号查出原报文日志表报文 核查单
                List<Map<String, Object>> logList = new ArrayList<>();
//                        wsd.queryInterfaceLog(cstOrdId,srvOrdId);
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
                    String activeType = srvOrdJson.getString("ACTIVE_TYPE");
                    String tradeTypeCode = srvOrdJson.getString("TRADE_TYPE_CODE");

                    //正常新开并且返回成功报文 ACTIVE_TYPE TRADE_TYPE_CODE
                    if("1".equals(activeType) && "2009".equals(tradeTypeCode) && "0".equals(respCode)){
                        requestJson = content;
                        break;
                    }
                }
                if(!StringUtils.isEmpty(requestJson)){

                    Map jsStr = JSONObject.parseObject(requestJson,Map.class);
                    Map body = MapUtils.getMap(jsStr, "UNI_BSS_BODY");
                    Map applyOrderReq = MapUtils.getMap(body, "APPLY_ORDER_REQ");
                    Map cstOrd = MapUtils.getMap(applyOrderReq, "CST_ORD");
                    Map cstOrdInfo = MapUtils.getMap(cstOrd, "CST_ORD_INFO");
                    Map srvOrdList = MapUtils.getMap(cstOrdInfo, "SRV_ORD_LIST");
                    List<Map<String,Object>> srvOrd = (List<Map<String,Object>>) MapUtils.getObject(srvOrdList, "SRV_ORD");

                    for (Map<String,Object> obj:srvOrd) {
                        tradeId = MapUtils.getString(obj, "TRADE_ID");
                        obj.put("WO_ID", woId);
                    }
                    //调用工建系统接口
                    sendJson = JSONObject.toJSONString(jsStr);
                    String url = wsd.queryUrl("provinceResOrder");
                    constructResponse = xmlUtil.sendHttpPostOrderCenter(url,sendJson);
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
            }


        } catch (Exception e) {
            retMap.put("success", false);
            retMap.put("message", "接口交互失败："+e.getMessage());
            logger.debug("接口交互失败："+e.getMessage());
        }
        return JSONObject.toJSONString(retMap);
    }


    /**
     * 记录接口日志

     */
    public void saveEventJson(Map<String, Object> map){
        ResInterfaceLog resInterfaceLog = new ResInterfaceLog();
        resInterfaceLog.setInterfName(MapUtil.getString(map ,"interfname"));
        //resInterfaceLog.setCreateDate(new Date());
        resInterfaceLog.setOrderNo(MapUtil.getString(map ,"orderno"));
        resInterfaceLog.setRemark(MapUtil.getString(map ,"remark"));
        resInterfaceLog.setReturnContent(MapUtil.getString(map ,"returncontent"));
        //resInterfaceLog.setUpdateDate(new Date());
        resInterfaceLog.setUrl(MapUtil.getString(map ,"url"));
        resInterfaceLog.setContent(MapUtil.getString(map ,"content"));
        LoggerThreadPool.addLoggerToExecute(resInterfaceLogger,resInterfaceLog);
        //rsd.saveJson(map);
    }


}
