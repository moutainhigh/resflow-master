package com.zres.project.localnet.portal.webservice.ddos;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.listener.util.EnmuValueUtil;
import com.zres.project.localnet.portal.logInfo.entry.ResInterfaceLog;
import com.zres.project.localnet.portal.logInfo.service.ResInterfaceLogger;
import com.zres.project.localnet.portal.logInfo.until.LoggerThreadPool;
import com.zres.project.localnet.portal.util.HttpClientJson;
import com.zres.project.localnet.portal.util.OrderTrackOperType;
import com.zres.project.localnet.portal.util.XmlUtil;
import com.zres.project.localnet.portal.webservice.data.dao.DDOSDao;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.zres.project.localnet.portal.webservice.interfaceJiKe.BackOrderServiceIntf;
import com.ztesoft.res.frame.core.util.MapUtil;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import net.sf.json.JSONObject;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * DDOS（流量清洗）附加产品派单接口
 */
@Service
public class DDOSSendService implements DDOSSendServiceIntf {
    private static final Logger logger = Logger.getLogger(DDOSSendService.class);
    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private ResInterfaceLogger resInterfaceLogger;
    @Autowired
    private BackOrderServiceIntf backOrderServiceIntf;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private DDOSDao ddosDao;
    @Autowired
    private XmlUtil xmlUtil;
    @Override
    public Map sendOrder(Map<String, Object> map) throws Exception {
        String URL = wsd.queryUrl("ddos");
        //插入接口记录map
        Map<String, Object> interflog = new HashMap<String, Object>();
        Map<String, Object> returnMap = new HashMap<String, Object>();
        try {
            String subscribeId = MapUtils.getString(map, "SUBSCRIBE_ID");
            String reqJsonStr = MapUtils.getString(map, "jikeStr");
            String orderId = MapUtils.getString(map, "ORDER_ID");
            Map<String, Object> response = null;
            try {
               // response = HttpClientJson.sendHttpPost(URL, reqJsonStr);
                response = xmlUtil.sendHttpPostOrderCenter(URL, reqJsonStr);

                interflog.put("RETURNCONTENT", response.get("msg"));
            }
            catch (Exception e) {
                interflog.put("RETURNCONTENT", e.getMessage());
            }

            // 插入接口记录
            interflog.put("INTERFNAME", "DDOS派单接口");
            interflog.put("URL", URL);
            interflog.put("CONTENT", reqJsonStr);
            interflog.put("ORDERNO", subscribeId);
            interflog.put("REMARK", "接口状态status:" + MapUtil.getString(response, "status"));
            this.saveEventJson(interflog);
            // 解析返回报文
            if ("200".equals(MapUtil.getString(response, "code"))) {
                //解析返回报文
                String body = MapUtil.getString(response, "msg").toUpperCase();
                JSONObject requests = JSONObject.fromObject(body);
                JSONObject uniBssBody = requests.getJSONObject("UNI_BSS_BODY");
                JSONObject applyOrderRsp = uniBssBody.getJSONObject("APPLY_ORDER_RSP");
                String resp_code = applyOrderRsp.getString("RESP_CODE");
                String resp_desc = applyOrderRsp.getString("RESP_DESC");
                if ("0".equals(resp_code)) {
                    returnMap.put("RESP_CODE", true);
                }
                else {
                    returnMap.put("RESP_CODE", false);
                    returnMap.put("RESP_DESC", "DDOS派单失败！异常原因：" + resp_desc);
                    this.backOrderJike(map, resp_desc);

                    Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(ThreadLocalInfoHolder.getLoginUser().getUserId()));
                    Map<String, Object> logParamMap = new HashMap<String, Object>();
                    logParamMap.put("orderId", orderId);
                    logParamMap.put("trackOrgId", MapUtil.getString(operStaffInfoMap, "ORG_ID"));
                    logParamMap.put("trackOrgName", MapUtil.getString(operStaffInfoMap, "ORG_NAME"));
                    logParamMap.put("trackDate", new java.sql.Date(new java.util.Date().getTime()));
                    logParamMap.put("createDate", new java.sql.Date(new java.util.Date().getTime()));
                    logParamMap.put("trackStaffId", MapUtil.getString(operStaffInfoMap, "USER_ID"));
                    logParamMap.put("trackStaffName", MapUtil.getString(operStaffInfoMap, "USER_REAL_NAME"));
                    logParamMap.put("trackStaffPhone", MapUtils.getString(operStaffInfoMap, "USER_PHONE"));
                    logParamMap.put("trackStaffEmail", MapUtils.getString(operStaffInfoMap, "USER_EMAIL"));
                    String trackMessage = "DDOS派单失败：" + resp_desc;
                    logParamMap.put("trackMessage", trackMessage);
                    String trackContent = "[调用DDOS派单接口]";
                    logParamMap.put("trackContent", trackContent);
                    logParamMap.put("operType", OrderTrackOperType.OPER_TYPE_1);
                    orderDealDao.insertTrackLogInfo(logParamMap);
                }
            }
            else {
                returnMap.put("RESP_CODE", false);
                returnMap.put("RESP_DESC", "DDOS派单失败！异常原因：" + MapUtil.getString(response, "msg"));
                this.backOrderJike(map, MapUtil.getString(response, "msg"));
            }
        }
        catch (Exception e) {
            returnMap.put("RESP_CODE", false);
            returnMap.put("RESP_DESC", "DDOS派单失败！异常原因：" + e.getMessage());
            logger.error(e.getMessage());
        }
        return returnMap;
    }

    public void backOrderJike(Map map, String errorInfo) throws Exception {
        Map backMap = new HashMap();
        backMap.put("SUBSCRIBE_ID", MapUtil.getString(map, "SUBSCRIBE_ID"));
        backMap.put("SERIAL_NUMBER", MapUtil.getString(map, "SERIAL_NUMBER"));
        backMap.put("FLOW_ID", MapUtil.getString(map, "FLOW_ID"));
        backMap.put("TRADE_ID", MapUtil.getString(map, "TRADE_ID"));
        backMap.put("ROUTE_TYPE", MapUtil.getString(map, "ROUTE_TYPE"));
        backMap.put("ROUTE_VALUE", MapUtil.getString(map, "ROUTE_VALUE"));
        backMap.put("backExec", "派发DDOS失败！原因： " + errorInfo);
        backMap.put("DDOS_BACK", true);
        String srvOrdId = MapUtil.getString(map, "srvOrdId");
        if (srvOrdId != null && !"".equals(srvOrdId)){
            backMap.put("srvOrdId",srvOrdId );
        }else{
            backMap.put("srvOrdId",MapUtil.getString(map, "SUBSCRIBE_ID"));
        }
        backOrderServiceIntf.backOrder(backMap);
        String orderId = MapUtils.getString(map, "ORDER_ID");
        // 修改业务订单状态为作废
        Long orderIdL = Long.valueOf(orderId);
        orderDealDao.updateSrvOrderStateById("10X", orderIdL);
        //查询派发的DDOS环节
        Map <String, Object> ret = ddosDao.queryWoInfoByDDOS(orderId, EnmuValueUtil.DDOS_FLOW_CLEANING);
        if (ret != null && ret.size()>0){
            orderDealDao.updateWoStateByWoId(MapUtils.getString(ret,"WO_ID"), OrderTrackOperType.WO_ORDER_STATE_7);
        }

    }

    public void saveEventJson(Map<String, Object> map){
        ResInterfaceLog resInterfaceLog = new ResInterfaceLog();
        resInterfaceLog.setInterfName(MapUtil.getString(map ,"INTERFNAME"));
        resInterfaceLog.setOrderNo(MapUtil.getString(map ,"ORDERNO"));
        resInterfaceLog.setRemark(MapUtil.getString(map ,"REMARK"));
        resInterfaceLog.setReturnContent(MapUtil.getString(map ,"RETURNCONTENT"));
        resInterfaceLog.setUrl(MapUtil.getString(map ,"URL"));
        resInterfaceLog.setContent(MapUtil.getString(map ,"CONTENT"));
        LoggerThreadPool.addLoggerToExecute(resInterfaceLogger,resInterfaceLog);
    }
}
