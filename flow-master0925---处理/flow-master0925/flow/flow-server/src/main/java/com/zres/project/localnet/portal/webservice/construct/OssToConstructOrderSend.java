package com.zres.project.localnet.portal.webservice.construct;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.zres.project.localnet.portal.local.SrvOrdAttrServiceIntf;
import com.zres.project.localnet.portal.util.XmlUtil;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName OssToConstructOrderSend
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/9/19 14:39
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@Service
public class OssToConstructOrderSend  implements OssToConstructOrderSendIntf  {
    private static Logger logger = LoggerFactory.getLogger(OssToConstructOrderSend.class);
    @Autowired
    private XmlUtil xmlUtil;

    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private SrvOrdAttrServiceIntf srvOrdAttrServiceIntf;

    @Override
    public Map orderSend(@RequestParam Map<String,Object> map){
        //拼接请求报文
        String jsonReqStr = "";
        logger.info("下发工建开通单接口发送报文request：" + jsonReqStr);
        Map<String, Object> constructResponse = new HashMap<>();
        Map<String, Object> retMap = new HashMap<>();
        String requestJson = "";
        String tradeId = "";
        String srvOrdId = "";
        String response = "";

        List<Map<String,Object>> circuitData = (List<Map<String,Object>>) MapUtils.getObject(map, "circuitData");
        String cstOrdId = MapUtils.getString(map, "cstOrdId");

        for (int i = 0; i < circuitData.size(); i++) {
            /*
             * az端资源是否具备先入库
             * @author guanzhao
             * @date 2020/12/7
             *
             */
            Map<String, Object> circuitInfo = circuitData.get(i);
            srvOrdId = MapUtils.getString(circuitInfo, "SRV_ORD_ID");
            map.put("srvOrdId", srvOrdId);
            insertAZResources(map); //A/Z端资源是否具备
            String tradeType = MapUtils.getString(circuitInfo, "TRADE_TYPE_CODE");
            //通过单号查出原报文日志表报文 开通单
            try{
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
//                    String activeType = srvOrdJson.getString("ACTIVE_TYPE");
                    String tradeTypeCode = srvOrdJson.getString("TRADE_TYPE_CODE");
                    tradeId = srvOrdJson.getString("TRADE_ID");

                    //开通单并且返回成功报文 ACTIVE_TYPE TRADE_TYPE_CODE
                    if(tradeTypeCode.equals(tradeType) && "0".equals(respCode)){
                        requestJson = content;
                        break;
                    }
                }
                if(!StringUtils.isEmpty(requestJson)){
                    //查询电路信息对原报文增加相应得资源信息通过
                    //调用工建系统接口
                    String url = wsd.queryUrl("ToConstructOrderSend");
                    constructResponse = xmlUtil.sendHttpPostOrderCenter(url,requestJson);
                    String code = MapUtils.getString(constructResponse,"code","");
                    if("200".equals(code)){
                        response = MapUtils.getString(constructResponse,"msg");
                        JSONObject respJson = JSONObject.parseObject(response);
                        //0：成功；1：失败，报文错误；
                        Map respBody = JSONObject.parseObject(respJson.getString("UNI_BSS_BODY"), Map.class);
                        Map<String,Object> respOrder = MapUtils.getMap(respBody,"APPLY_ORDER_RSP");
                        String respCode = MapUtils.getString(respOrder,"RESP_CODE");
                        String respDesc = MapUtils.getString(respOrder,"RESP_DESC","接口交互异常");
                        if("0".equals(respCode)){
                            //add by wang.gang2 调度下发工建记录
                            List srvOrdAttrList = new ArrayList();
                            Map<String,Object> attrCir = new HashMap<>();
                            attrCir.put("SRV_ORD_ID",srvOrdId);
                            attrCir.put("SOURSE","localBuild");
                            attrCir.put("ATTR_ACTION","0");
                            attrCir.put("ATTR_VALUE_NAME","");
                            attrCir.put("ATTR_CODE", "O2CORDERSEND");
                            attrCir.put("ATTR_VALUE", "1"); //默认1
                            attrCir.put("OLD_ATTR_VALUE", "");
                            srvOrdAttrList.add(attrCir);
                            wsd.bachAddGomIdcSrvOrdAttrInfo(srvOrdAttrList);
                            retMap.put("success",true);
                        } else{
                            retMap.put("success",false);
                            retMap.put("message",respDesc);
                        }
                    } else{
                        retMap.put("success",false);
                        retMap.put("message","接口交互失败："+code);
                    }
                }else{
                    retMap.put("success", false);
                    retMap.put("message", "请检查是否有对应的接口记录" );
                }
            } catch (Exception e) {
                retMap.put("success", false);
                retMap.put("message", "接口交互失败："+e.getMessage());
            } finally {
                insertInterfaceLog(tradeId,srvOrdId,requestJson,JSONObject.toJSONString(constructResponse));
            }
        }
        return retMap;
    }
    /**
     * @Description 功能描述: 记录接口日志
     * @Param: [tradeId, srvOrdId, request, respone]
     * @Return: void
     * @Author: wang.gang2
     * @Date: 2020/9/19 16:04
     */
    private void insertInterfaceLog(String tradeId,String srvOrdId,String request,String respone){
        Map<String,Object> interflog = new HashMap<String, Object>();
        interflog.put("INTERFNAME","OSS To 工建系统 开通单");
        interflog.put("URL","webservice/construct/OssToConstructOrderSend");
        interflog.put("CONTENT",request);
        interflog.put("RETURNCONTENT",respone);
        interflog.put("ORDERNO",tradeId);
        interflog.put("SRV_ORD_ID",srvOrdId);
        interflog.put("REMARK","发送工建系统开通单 json报文");
        wsd.insertInterfLog(interflog);
    }

    private void insertAZResources(Map<String, Object> params) {
        String srvOrdId = MapUtils.getString(params, "srvOrdId");
        String attrCodeA = MapUtils.getString(params, "ATTR_CODE_A");
        String attrCodeNameA = MapUtils.getString(params, "ATTR_CODE_NAME_A");
        String attrCodeZ = MapUtils.getString(params, "ATTR_CODE_Z");
        String attrCodeNameZ = MapUtils.getString(params, "ATTR_CODE_NAME_Z");
        String attrCodeValueZ = MapUtils.getString(params, "ATTR_CODE_VALUE_Z");
        Boolean resultFlagA = MapUtils.getBoolean(params, "RESULT_FLAG_A",false);
        Boolean resultFlagZ = MapUtils.getBoolean(params, "RESULT_FLAG_Z",false);
        String attrCodeValueA = MapUtils.getString(params, "ATTR_CODE_VALUE_A","");

        if (resultFlagA && attrCodeValueA != null  && attrCodeValueA.length()!=0) {
            srvOrdAttrServiceIntf.insertSrvOrdAttr(srvOrdId,attrCodeA,attrCodeNameA,attrCodeValueA,"local");
        }
        if (resultFlagZ && attrCodeValueZ != null && attrCodeValueA.length()!=0) {
            srvOrdAttrServiceIntf.insertSrvOrdAttr(srvOrdId,attrCodeZ,attrCodeNameZ,attrCodeValueZ,"local");
        }
    }
}
