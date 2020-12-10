package com.zres.project.localnet.portal.webservice.interfaceJiKe;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealService;
import com.zres.project.localnet.portal.util.XmlUtil;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * Created by tang.huili on 2019/5/10.
 * 进度查询接口
 *
 * Update by jiyou.li on 2019/5/10
 */
@RestController
@RequestMapping("/queryOrderService/interfaceBDW")
public class QueryOrderService implements QueryOrderServiceIntf {

    private static Logger logger = LoggerFactory.getLogger(QueryOrderService.class);

    @Autowired
    private OrderDealService orderDealService;

    @Autowired
    private XmlUtil xmlUtil;
    @Autowired
    private WebServiceDao wsd;

    @IgnoreSession
    @PostMapping(value = "/queryOrder.spr", produces = "application/json;charset=UTF-8")
    @Override
    public Map queryOrder(@RequestBody String request) {
        logger.info("-----进度查询接口queryOrder-----" + request);
        JSONObject retJson = new JSONObject();
        Map uniBssBodyReturn = new HashMap();
        Map queryOrderRspMap = new HashMap();
        Map srvOrdListMap = new HashMap();
        Map cstOrdMap = new HashMap();
        //插入接口记录
        Map interflog = new HashMap();
        interflog.put("INTERFNAME","进度查询接口");
        interflog.put("URL","/queryOrderService/interfaceBDW/queryOrder.spr");
        interflog.put("CONTENT",request);
        try {
            // 解析报文
            JSONObject jsStr = JSONObject.parseObject(request);
            JSONObject uniBssBody = jsStr.getJSONObject("UNI_BSS_BODY");
            JSONObject queryOrderRsp = uniBssBody.getJSONObject("QUERY_ORDER_REQ");
            JSONObject cstOrd = queryOrderRsp.getJSONObject("CST_ORD");
            String subscribeId = cstOrd.getString("SUBSCRIBE_ID");
            interflog.put("ORDERNO",subscribeId);
            JSONObject srvOrdList = cstOrd.getJSONObject("SRV_ORD_LIST");
            JSONArray srvOrd = srvOrdList.getJSONArray("SRV_ORD");
            Iterator<Object> srvIterator = srvOrd.iterator();
            List<Map<String,Object>> srvList = new ArrayList<>();
            for(int i=0;i<srvOrd.size();i++){
                JSONObject temp = (JSONObject) srvOrd.get(i);
                Map retMap = this.wrapResultMap(temp.getString("TRADE_ID"), temp.getString("FLOW_ID"), temp.getString("SERIAL_NUMBER"));
                srvList.add(retMap);
            }

            // 拼报文
            retJson.put("UNI_BSS_HEAD",xmlUtil.requestHeader());

            srvOrdListMap.put("SRV_ORD", srvList);
            cstOrdMap.put("SRV_ORD_LIST", srvOrdListMap);
            cstOrdMap.put("SUBSCRIBE_ID", subscribeId);
            queryOrderRspMap.put("CST_ORD", cstOrdMap);
            queryOrderRspMap.put("RESP_DESC","查询成功");
            queryOrderRspMap.put("RESP_CODE","0");
        }catch (Exception e){
            logger.error("进度查询接口异常，" + e.getMessage(), e);

            queryOrderRspMap.put("RESP_DESC","报文不符合规范，请检查或联系管理员");
            queryOrderRspMap.put("RESP_CODE","1");
        }
        uniBssBodyReturn.put("QUERY_ORDER_RSP", queryOrderRspMap);
        retJson.put("UNI_BSS_BODY", uniBssBodyReturn);
        interflog.put("RETURNCONTENT",retJson.toString());
        wsd.insertInterfLog(interflog);
        return retJson;
    }

    public Map<String, Object> wrapResultMap(String tradeId,String flowId,String serialNumber) {
        Map  srvOrdMap = new HashMap();
        Map  tacheInfoS = new HashMap();
        srvOrdMap.put("SERIAL_NUMBER",serialNumber);
        srvOrdMap.put("TRADE_ID",tradeId);
        srvOrdMap.put("FLOW_ID",flowId);

        String orderId = wsd.qrySrvOrdIdByTradeId(tradeId,flowId);
        // 查询环节信息，sql与电路性情-任务tab页保持一致
        //List <Map<String, Object>> queryTaskInfo(@Param("orderId") String orderId);
        List tacheInfoList = wsd.qryTacheInfo(orderId);//orderDealService.getTacheInfo(tradeId);
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

    /**
     * add by wang.gang2 全程路由查询接口 ZMP 1923018
     * @param request
     * @return
     */
    @IgnoreSession
    @PostMapping(value = "/queryFullRoute.spr", produces = "application/json;charset=UTF-8")
    @Override
    public Map queryFullRoute(@RequestBody String request) {
        logger.info("-----全程路由查询接口queryFullRoute-----" + request);
        JSONObject retJson = new JSONObject();
        Map uniBssBodyReturn = new HashMap();
        Map queryOrderRspMap = new HashMap();
        Map srvOrdListMap = new HashMap();
        Map cstOrdMap = new HashMap();
        //插入接口记录
        Map interflog = new HashMap();
        interflog.put("INTERFNAME","全程路由查询接口");
        interflog.put("URL","/queryOrderService/interfaceBDW/queryFullRoute.spr");
        interflog.put("CONTENT",request);
        try {
            // 解析报文
            JSONObject jsStr = JSONObject.parseObject(request);
            JSONObject uniBssBody = jsStr.getJSONObject("UNI_BSS_BODY");
            JSONObject fullRoute = uniBssBody.getJSONObject("FULLROUTE_API_REQ");
            JSONObject routing = fullRoute.getJSONObject("ROUTING");
            String serialNumber = fullRoute.getString("SERIAL_NUMBER");
            String tradeId = fullRoute.getString("TRADE_ID");
            interflog.put("ORDERNO",serialNumber);
            //查询路由信息并且拼接报文
            List<Map<String, Object>> srvOrdInfo = wsd.querySrvOrderList(tradeId, serialNumber);
            if(srvOrdInfo.size()<0){
                throw new Exception("未找到对应的单子请检查：SERIAL_NUMBER、TRADE_ID 是否正确");
            }
            String srvOrderId = MapUtils.getString(srvOrdInfo.get(0), "SRV_ORD_ID");
            // 拼报文
            retJson.put("UNI_BSS_HEAD",xmlUtil.requestHeader());
            Map fullRouteInfo = wsd.qryFinishOrdInfo(srvOrderId);
            if(fullRouteInfo.size() < 0 || StringUtils.isEmpty(MapUtils.getString(fullRouteInfo,"ROUTE_INFO"))){
//                throw new Exception("未查到相关路由信息");
                queryOrderRspMap.put("FULL_ROUTE", "未查到相关路由信息");
                queryOrderRspMap.put("RESP_DESC","查询成功");
                queryOrderRspMap.put("RESP_CODE","0000");
            }else {
                queryOrderRspMap.put("FULL_ROUTE", MapUtils.getString(fullRouteInfo, "ROUTE_INFO"));
                queryOrderRspMap.put("RESP_DESC", "查询成功");
                queryOrderRspMap.put("RESP_CODE", "0000");
            }
        }catch (Exception e){
            logger.error("全程路由查询接口异常，" + e.getMessage(), e);
            queryOrderRspMap.put("RESP_DESC","报文不符合规范，请检查或联系管理员");
            queryOrderRspMap.put("RESP_CODE","8888");
            queryOrderRspMap.put("FULL_ROUTE", null);
        }
        uniBssBodyReturn.put("FULLROUTE_API_RSP", queryOrderRspMap);

        retJson.put("UNI_BSS_BODY", uniBssBodyReturn);
        interflog.put("RETURNCONTENT",retJson.toString());
        wsd.insertInterfLog(interflog);
        return retJson;
    }
}
