package com.zres.project.localnet.portal.cloudNetWork.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.cloudNetWork.CloudNetQueryOrderIntf;
import com.zres.project.localnet.portal.util.XmlUtil;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 云组网进度查询接口
 *
 * @author caomm on 2020/11/20
 */
@Service
public class CloudNetQueryOrderService implements CloudNetQueryOrderIntf {
    private static final Logger logger = LoggerFactory.getLogger(CloudNetQueryOrderService.class);
    @Autowired
    private CloudNetCommonService cloudNetCommonService;
    @Autowired
    private XmlUtil xmlUtil;
    @Autowired
    private WebServiceDao webServiceDao;

    public Map<String, Object> queryOrder(String request){
        JSONObject retJson = new JSONObject();
        Map<String, Object> logMap = new HashMap<>();
        Map uniBssBodyReturn = new HashMap();
        Map queryOrderRspMap = new HashMap();
        Map srvOrdListMap = new HashMap();
        Map cstOrdMap = new HashMap();
        try{
            logMap.put("interfName", "云组网进度查询接口");
            logMap.put("url", "/cloudNetWork/interfaceBDW/queryOrder.spr");
            logMap.put("content", request);
            logMap.put("remark", "接收云组网进度查询报文");
            JSONObject json = JSON.parseObject(request);
            JSONObject UNI_BSS_BODY = json.getJSONObject("UNI_BSS_BODY");
            JSONObject YZWQUERY_ORDER_REQ = UNI_BSS_BODY.getJSONObject("YZWQUERY_ORDER_REQ");
            JSONObject ROUTING = YZWQUERY_ORDER_REQ.getJSONObject("ROUTING");
            String ROUTE_TYPE = ROUTING.getString("ROUTE_TYPE");
            String ROUTE_VALUE = ROUTING.getString("ROUTE_VALUE");
            JSONObject CST_ORD = YZWQUERY_ORDER_REQ.getJSONObject("CST_ORD");
            String SUBSCRIBE_ID = CST_ORD.getString("SUBSCRIBE_ID");
            String SUBSCRIBE_ID_RELA = CST_ORD.getString("SUBSCRIBE_ID_RELA");
            JSONObject SRV_ORD_LIST = CST_ORD.getJSONObject("SRV_ORD_LIST");
            JSONArray SRV_ORD = SRV_ORD_LIST.getJSONArray("SRV_ORD");
            logMap.put("orderNo", SUBSCRIBE_ID);
            cloudNetCommonService.insertLogInfo(logMap);
            if (SRV_ORD != null && SRV_ORD.size() > 0){
                List<Map<String,Object>> srvList = new ArrayList<>();
                for (int i = 0; i < SRV_ORD.size(); i++){
                    JSONObject srvOrd = SRV_ORD.getJSONObject(i);
                    String SERIAL_NUMBER = srvOrd.getString("SERIAL_NUMBER");
                    String TRADE_ID = srvOrd.getString("TRADE_ID");
                    String FLOW_ID = srvOrd.getString("FLOW_ID");
                    Map retMap = wrapResultMap(TRADE_ID, FLOW_ID, SERIAL_NUMBER);
                    srvList.add(retMap);
                }
                retJson.put("UNI_BSS_HEAD",xmlUtil.requestHeader());

                srvOrdListMap.put("SRV_ORD", srvList);
                cstOrdMap.put("SRV_ORD_LIST", srvOrdListMap);
                cstOrdMap.put("SUBSCRIBE_ID", SUBSCRIBE_ID);
                queryOrderRspMap.put("CST_ORD", cstOrdMap);
                queryOrderRspMap.put("RESP_DESC","查询成功");
                queryOrderRspMap.put("RESP_CODE","0");
            }
        }catch(Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>进度查询发生异常：{}", e.getMessage());
            queryOrderRspMap.put("RESP_DESC","报文不符合规范，请检查或联系管理员");
            queryOrderRspMap.put("RESP_CODE","1");
        }
        uniBssBodyReturn.put("QUERY_ORDER_RSP", queryOrderRspMap);
        retJson.put("UNI_BSS_BODY", uniBssBodyReturn);
        logMap.put("msg",retJson.toString());
        cloudNetCommonService.updateLogInfo(retJson.toString(), MapUtils.getString(logMap, "id"));
        return retJson;
    }

    public Map<String, Object> wrapResultMap(String tradeId,String flowId,String serialNumber) {
        Map  srvOrdMap = new HashMap();
        Map  tacheInfoS = new HashMap();
        srvOrdMap.put("SERIAL_NUMBER",serialNumber);
        srvOrdMap.put("TRADE_ID",tradeId);
        srvOrdMap.put("FLOW_ID",flowId);

        String orderId = webServiceDao.qrySrvOrdIdByTradeId(tradeId,flowId);
        // 查询环节信息，sql与电路性情-任务tab页保持一致
        List tacheInfoList = webServiceDao.qryTacheInfo(orderId);
        if(tacheInfoList!=null){
            List<Map> list=new ArrayList<Map>();
            for(int i=0;i<tacheInfoList.size();i++){
                Map tacheMap=(Map<String, String>)tacheInfoList.get(i);
                Map  TACHE_INFO=new HashMap();
                TACHE_INFO.put("TACHE_NAME",tacheMap.get("TACHE_NAME"));
                TACHE_INFO.put("RECEIVE_TIME",tacheMap.get("CREATE_DATE"));
                TACHE_INFO.put("REPLY_TIME",tacheMap.get("DEAL_DATE"));
                TACHE_INFO.put("LIMIT_TIME",tacheMap.get("LIMIT_TIME"));
                TACHE_INFO.put("OVERTIME",tacheMap.get("OVERTIME"));
                TACHE_INFO.put("PROC_STAFF",tacheMap.get("USERNAME"));
                TACHE_INFO.put("PROC_STAFF_TEL",tacheMap.get("PHONE"));
                TACHE_INFO.put("PROC_STAFF_EMAIL",tacheMap.get("EMAIL"));
                TACHE_INFO.put("PROC_DESC",tacheMap.get("TRACK_CONTENT"));
                TACHE_INFO.put("ORDER_NUM",tacheMap.get("ROWNUM"));
                list.add(TACHE_INFO);
            }
            tacheInfoS.put("TACHE_INFO",list);
            srvOrdMap.put("TACHE_LIST",tacheInfoS);
        }
        return srvOrdMap;
    }
}