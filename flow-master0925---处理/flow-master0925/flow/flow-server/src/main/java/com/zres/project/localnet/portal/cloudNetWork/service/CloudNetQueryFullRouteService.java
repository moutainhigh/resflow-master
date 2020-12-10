package com.zres.project.localnet.portal.cloudNetWork.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.cloudNetWork.CloudNetQueryFullRouteIntf;
import com.zres.project.localnet.portal.util.XmlUtil;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全程路由处理service
 *
 * @author caomm on 2020/11/20
 */
@Service
public class CloudNetQueryFullRouteService implements CloudNetQueryFullRouteIntf {
    private static final Logger logger = LoggerFactory.getLogger(CloudNetQueryFullRouteService.class);
    @Autowired
    private CloudNetCommonService commonService;
    @Autowired
    private WebServiceDao webServiceDao;
    @Autowired
    private XmlUtil xmlUtil;
    @Override
    public String queryFullRoute(String request) {
        Map<String, Object> logMap = new HashMap<>();
        JSONObject retJson = new JSONObject();
        Map uniBssBodyReturn = new HashMap();
        Map queryOrderRspMap = new HashMap();
        try{
            logMap.put("interfName", "云组网全程路由查询接口");
            logMap.put("url", "/cloudNetWork/interfaceBDW/queryFullRoute.spr");
            logMap.put("content", request);
            logMap.put("remark", "接收云组网全程路由查询报文");
            JSONObject json = JSON.parseObject(request);
            JSONObject UNI_BSS_BODY = json.getJSONObject("UNI_BSS_BODY");
            JSONObject YZWFULLROUTE_API_REQ = UNI_BSS_BODY.getJSONObject("YZWFULLROUTE_API_REQ");
            JSONObject ROUTING = YZWFULLROUTE_API_REQ.getJSONObject("ROUTING");
            String ROUTE_TYPE = ROUTING.getString("ROUTE_TYPE");
            String ROUTE_VALUE = ROUTING.getString("ROUTE_VALUE");
            String SERIAL_NUMBER = YZWFULLROUTE_API_REQ.getString("SERIAL_NUMBER");
            String TRADE_ID = YZWFULLROUTE_API_REQ.getString("TRADE_ID");
            logMap.put("orderNo", TRADE_ID);
            commonService.insertLogInfo(logMap);
            //查询srvOrdId
            List<Map<String, Object>> srvOrdList = webServiceDao.querySrvOrderList(TRADE_ID, SERIAL_NUMBER);
            if (CollectionUtils.isNotEmpty(srvOrdList)){
                String srvOrderId = MapUtils.getString(srvOrdList.get(0), "SRV_ORD_ID");
                retJson.put("UNI_BSS_HEAD", xmlUtil.requestHeader());
                Map fullRouteInfo = webServiceDao.qryFinishOrdInfo(srvOrderId);
                if(fullRouteInfo.size() < 0 || StringUtils.isEmpty(MapUtils.getString(fullRouteInfo,"ROUTE_INFO"))){
                    queryOrderRspMap.put("FULL_ROUTE", "未查到相关路由信息");
                    queryOrderRspMap.put("RESP_DESC","查询成功");
                    queryOrderRspMap.put("RESP_CODE","0000");
                }else {
                    queryOrderRspMap.put("FULL_ROUTE", MapUtils.getString(fullRouteInfo, "ROUTE_INFO"));
                    queryOrderRspMap.put("RESP_DESC", "查询成功");
                    queryOrderRspMap.put("RESP_CODE", "0000");
                }
            }else{
                throw new Exception("未找到对应的单子请检查：SERIAL_NUMBER、TRADE_ID 是否正确");
            }

        }catch(Exception e){
            logger.error("全程路由查询接口异常，" + e.getMessage(), e);
            queryOrderRspMap.put("RESP_DESC","报文不符合规范，请检查或联系管理员");
            queryOrderRspMap.put("RESP_CODE","8888");
            queryOrderRspMap.put("FULL_ROUTE", null);
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>全程路由查询发生异常：{}", e.getMessage());
        }
        uniBssBodyReturn.put("YZWFULLROUTE_API_RSP", queryOrderRspMap);
        retJson.put("UNI_BSS_BODY", uniBssBodyReturn);
        commonService.updateLogInfo(retJson.toString(), MapUtils.getString(logMap, "id"));
        return retJson.toString();
    }
}