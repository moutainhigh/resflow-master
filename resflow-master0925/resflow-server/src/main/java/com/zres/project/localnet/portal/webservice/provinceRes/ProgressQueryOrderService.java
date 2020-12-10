package com.zres.project.localnet.portal.webservice.provinceRes;

import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.util.XmlUtil;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName ProgressQueryOrderService
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/8/11 17:47
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@Service
public class ProgressQueryOrderService implements ProgressQueryOrderServiceIntf{
    private static Logger logger = LoggerFactory.getLogger(ProgressQueryOrderService.class);

    @Autowired
    private XmlUtil xmlUtil;
    @Autowired
    private WebServiceDao wsd;

    @Override
    public List<Map<String, Object>> progressQueryOrder(@RequestParam Map<String,Object> params) {

        //拼接请求报文
        String jsonReqStr = "";
        logger.info("进度查询接口发送报文request：" + jsonReqStr);
        Map<String, Object> constructResponse = new HashMap<>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置日期格式
        Map<String, Object> retMap = new HashMap<>();
        JSONObject jsonObj = new JSONObject();
        String tradeId = "";
        String response = "";
        List<Map<String, Object>> tacheInfo = new ArrayList<>();
        // 要传过来 srv_ord_id 过来
        try{
            Map<String, Object> custInfo = wsd.queryCustInfo(MapUtils.getString(params,"srvOrdId"));
            List<JSONObject>  srvOrdInfoList = new ArrayList<>();
            JSONObject progressQuery = new JSONObject();
            JSONObject cstOrd = new JSONObject();
            JSONObject srvOrd = new JSONObject();
            JSONObject srvOrdInfo = new JSONObject();
            JSONObject srvOrdList = new JSONObject();
            tradeId = MapUtils.getString(custInfo, "TRADE_ID");
            srvOrdInfo.put("SERIAL_NUMBER", MapUtils.getString(custInfo,"SERIAL_NUMBER"));
            srvOrdInfo.put("TRADE_ID",MapUtils.getString(custInfo, "TRADE_ID"));
            srvOrdInfo.put("TRADE_ID_RELA",MapUtils.getString(custInfo, "TRADE_ID_RELA"));
            srvOrdInfo.put("FLOW_ID",MapUtils.getString(custInfo, "FLOW_ID"));
            srvOrdInfoList.add(srvOrdInfo);
            srvOrd.put("SRV_ORD", srvOrdInfoList);

            srvOrdList.put("SUBSCRIBE_ID", MapUtils.getString(custInfo,"SUBSCRIBE_ID"));
            srvOrdList.put("SUBSCRIBE_ID_RELA", MapUtils.getString(custInfo,"SUBSCRIBE_ID_RELA"));
            srvOrdList.put("SRV_ORD_LIST", srvOrd);

            cstOrd.put("CST_ORD", srvOrdList);
            progressQuery.put("PROGRESS_QUERY_ORDER_REQ", cstOrd);
            jsonObj.put("UNI_BSS_BODY", progressQuery);

            String url = wsd.queryUrl("progressQuery");
            constructResponse = xmlUtil.sendHttpPostOrderCenter(url, jsonObj.toString());
            String code = MapUtils.getString(constructResponse,"code","");
            if("200".equals(code)){
                response = MapUtils.getString(constructResponse,"msg");
                Map<String,Object> respJson = JSONObject.parseObject(response,Map.class);
                //返回编码 00成功，01 失败
                Map<String,Object>  body = MapUtils.getMap(respJson,"UNI_BSS_BODY");
                Map<String,Object>  queryOrderRsp = MapUtils.getMap(body,"PROGRESS_QUERY_ORDER_RSP");

                String respCode = MapUtils.getString(queryOrderRsp,"RESP_CODE");
                String respDesc = MapUtils.getString(queryOrderRsp,"RESP_DESC");
                if("00".equals(respCode)){
                    Map<String,Object>  cstOrdRsp = MapUtils.getMap(queryOrderRsp,"CST_ORD");
                    Map<String,Object>  srvOrdListRsp = MapUtils.getMap(cstOrdRsp,"SRV_ORD_LIST");
                    List< Map<String,Object>> srvOrdInfos = (List< Map<String,Object>>)MapUtils.getObject(srvOrdListRsp,"SRV_ORD");

                    Map tacheListRsp = MapUtils.getMap(srvOrdInfos.get(0),"TACHE_LIST");
                    tacheInfo = (List<Map<String,Object>>)MapUtils.getObject(tacheListRsp,"TACHE_INFO");
                    retMap.put("success",true);
                    retMap.put("message",tacheInfo);
                } else{
                    retMap.put("success",false);
                    retMap.put("message",tacheInfo);
                }
            } else{
                retMap.put("success",false);
                retMap.put("message",tacheInfo);
            }
        } catch (Exception e) {
            retMap.put("success", false);
            retMap.put("message", tacheInfo);
        } finally {
            insertInterfaceLog(tradeId,jsonObj.toString(),JSONObject.toJSONString(constructResponse));
        }
        return  tacheInfo;

    }

    /**
     * 记录接口日志
     * @param request
     * @param respone
     */
    private void insertInterfaceLog(String tradeId,String request,String respone){
        Map<String,Object> interflog = new HashMap<String, Object>();
        interflog.put("INTERFNAME","OSS To 省内资源dia 自动开通 进度查询接口");
        interflog.put("URL","webservice/construct/progressQueryOrder");
        interflog.put("CONTENT",request);
        interflog.put("RETURNCONTENT",respone);
        interflog.put("ORDERNO",tradeId);
        interflog.put("REMARK","发送工建系统 json报文");
        wsd.insertInterfLog(interflog);
    }


}
