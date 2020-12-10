package com.zres.project.localnet.portal.webservice.interfaceJiKe;

import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealService;
import com.zres.project.localnet.portal.util.XmlUtil;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.zres.project.localnet.portal.webservice.flow.ExceptionChangeService;
import com.ztesoft.res.frame.flow.common.service.FlowActionHandler;

import com.ztesoft.res.frame.core.util.MapUtil;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import com.ztesoft.res.frame.user.login.DefaultLoginUserInfo;
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
 * Created by tang.huili on 2019/5/7.
 *
 *  订单回退接口
 *  update by jiyou.li on 2019/5/7.
 */
@Service
//    @RestController
//    @RequestMapping("/backOrderServiceIntf")
public class BackOrderService implements BackOrderServiceIntf {
    private static Logger logger = LoggerFactory.getLogger(BackOrderService.class);
    private static final String[] BACK_TYPE = {"17", "18","06","07","08"};

    @Autowired
    private XmlUtil xmlUtil;
    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private OrderDealService orderDealService;
    @Autowired
    private FlowActionHandler flowActionHandler;

    @Autowired
    private OrderDealDao orderDealDao;

    //    @GetMapping(value = "/interfaceBDW/backOrder.spr")
    public Map backOrder(@RequestParam Map map) {

        Boolean flag= map.keySet().contains("activeType")&&
                ExceptionChangeService.EXCEPTION_4A.equals(MapUtils.getString(map,"activeType"));

        //客户流水号
        String srvOrdId = map.get("srvOrdId").toString();
        String backExec = map.get("backExec").toString();
        Boolean ddosBack = MapUtil.getBoolean(map, "DDOS_BACK"); //DDOS退单
        srvOrdId = map.get("srvOrdId").toString();
        String jsonReqStr = "";
        map.put("flag",flag);
        if(null != ddosBack && ddosBack){
            jsonReqStr = ddosJsonStr(map);
        }else{
            jsonReqStr = wrapJsonStr(map);
        }
        logger.info("退单发送报文request：" + jsonReqStr);

        String url = wsd.queryUrl("backOrder");
        Map<String, Object> jkResponse = xmlUtil.sendHttpPostOrderCenter(url, jsonReqStr);
        logger.info("-------集客返回报文------" + jkResponse);

        //插入接口记录
        Map<String,Object> interflog = new HashMap<String, Object>();
        interflog.put("INTERFNAME","集客退单接口backOrder");
        interflog.put("URL",url);
        interflog.put("CONTENT",jsonReqStr);
        interflog.put("REMARK","接收集客json报文-backOrder");
        interflog.put("ORDERNO",srvOrdId);
        interflog.put("RETURNCONTENT", jkResponse.toString());
        wsd.insertInterfLog(interflog);

        //解析返回报文
        String respCode = "";
        String respDesc = "";
        if("200".equals(jkResponse.get("code"))){
//            JSONObject json = JSONObject.parseObject(jkResponse.get("msg").toString());
//            JSONObject uniBssHead = json.getJSONObject("UNI_BSS_HEAD");
//            respCode = uniBssHead.getString("RESP_CODE");
//            respDesc = uniBssHead.getString("RESP_DESC");
//        } else {
//            respCode = jkResponse.get("code").toString();
//            respDesc = jkResponse.get("msg").toString();
//        }
//        if (jkResponse.get("msg") != null) {
            JSONObject msg = JSONObject.parseObject(jkResponse.get("msg").toString());
            String uniBssBody = msg.getString("UNI_BSS_BODY");
            JSONObject jsUniBssBody = JSONObject.parseObject(uniBssBody);
            JSONObject jsFinishOrderRsp = JSONObject.parseObject(jsUniBssBody.getString("BACK_ORDER_RSP"));
            respCode = jsFinishOrderRsp.getString("RESP_CODE");//处理结果0：成功；1：失败，报文错误；2：失败，附件没有
            respDesc = jsFinishOrderRsp.getString("RESP_DESC");//处理原因
        } else {
            respCode = "-1";
            respDesc = "返回报文格式错误，请联系管理员！";
        }

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置日期格式
        //统一成功失败的code
        if ("0".equals(respCode)) {
            respCode = "1";
        } else if ("1".equals(respCode)) {
            respCode = "0";
        }
        //报文入库
        Map<String, Object> rmap = new HashMap<String, Object>();
        rmap.put("srv_ord_id", srvOrdId);
        rmap.put("attr_code", respCode);
        rmap.put("attr_name", "");
        rmap.put("attr_value_name", "集客订单回退接口返回结果");
        rmap.put("create_date", df.format(new Date()));
        rmap.put("sourse", "");
        if(!flag){
            rmap.put("attr_value", "");
            rmap.put("attr_action", "BackOrder");
        } else{
            rmap.put("attr_action", "BackOrder_4A");
            rmap.put("attr_value", MapUtils.getString(map,"FLOW_ID",""));
        }
        wsd.saveRetInfo(rmap);

        Map returnMap =new HashMap();
        returnMap.put("RESP_CODE",respCode);
        returnMap.put("RESP_DESC",respDesc);
        return returnMap;
    }

    /**
     * 拼接请求报文
     * @return
     */
    public  String wrapJsonStr(Map map) {
        //订单业务信息
        String svrOrderId = MapUtils.getString(map,"srvOrdId");
        String orderId = MapUtils.getString(map,"orderId");
        String backOrderDesc = MapUtils.getString(map, "backExec"); //退单原因
        List srvList = orderDealService.selectOrderInfoList(svrOrderId);
        HashMap mp=(HashMap)srvList.get(0);

        DefaultLoginUserInfo userInfo = (DefaultLoginUserInfo)ThreadLocalInfoHolder.getLoginUser();
        List staff = orderDealService.selectStaffInfoList(userInfo.getStaffId().toString());
        List paraList = orderDealService.selectOrderAttrInfo(svrOrderId);

        JSONObject json = new JSONObject();
        //HEAD
        json.put("UNI_BSS_HEAD",xmlUtil.requestHeader());

        //body
        JSONObject body = new JSONObject();
        JSONObject backOrderReq = new JSONObject();

        JSONObject routing = new JSONObject();
        // 路由类型:00:按系统路由
//        routing.put("ROUTE_TYPE","00");
//        // 路由关键值:01:集客综合订单;02:集客订单中心;03:政企订单中心
//        routing.put("ROUTE_VALUE","01");
        routing.put("ROUTE_TYPE", MapUtils.getString(mp,"ROUTE_TYPE"));
        // 路由关键值:01:集客综合订单;02:集客订单中心;03:政企订单中心
        routing.put("ROUTE_VALUE", MapUtils.getString(mp,"ROUTE_VALUE"));
        backOrderReq.put("ROUTING",routing);

        JSONObject cstOrd = new JSONObject();
        cstOrd.put("SUBSCRIBE_ID", MapUtils.getString(mp,"SUBSCRIBE_ID"));
    //    cstOrd.put("SUBSCRIBE_ID_RELA",MapUtils.getString(mp,"SUBSCRIBE_ID_RELA"));

        JSONObject srvOrdList = new JSONObject();
        if (srvList.size()>0){
            List<JSONObject> srvOrd = new ArrayList<>();
            for(int i=0;srvList.size()-i>0;i++){
                HashMap srvmp=(HashMap)srvList.get(i);
                JSONObject srvOrdObj = new JSONObject();
                String parallelFlag = MapUtils.getString(srvmp, "PARALLEL_FLAG");
                srvOrdObj.put("SERIAL_NUMBER",MapUtils.getString(srvmp,"SERIAL_NUMBER"));
                if(MapUtils.getBoolean(map,"flag",false)){
                    srvOrdObj.put("TRADE_ID", map.get("TRADE_ID"));
                    srvOrdObj.put("FLOW_ID", map.get("FLOW_ID"));
                }else {
                    srvOrdObj.put("TRADE_ID",MapUtils.getString(srvmp,"TRADE_ID"));
                    srvOrdObj.put("FLOW_ID",MapUtils.getString(srvmp,"FLOW_ID"));
                }

                SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
                if(srvmp.get("cust_operator_name") !=null){
                    JSONObject dealInfo = new JSONObject();
                    dealInfo.put("OPERATOR_NAME",((Map)staff.get(0)).get("NAME"));
                    dealInfo.put("OPERATOR_TEL",((Map)staff.get(0)).get("PHONE_NO"));
                    dealInfo.put("OPERATE_TIME",timeFormatter.format(Calendar.getInstance().getTime()));
                    srvOrdObj.put("DEAL_INFO",dealInfo);
                }

                //modify by wang.gang2  并行核查 退单 新增退单范围 退单类型 以及退单说明
                if(!StringUtils.isEmpty(parallelFlag)){
                    //1.跟据选择的退单类型 反馈退单范围
                    String backReasonType = MapUtils.getString(map, "backReasonType");
                    srvOrdObj.put("BACK_REASON_TYPE", backReasonType);
                    srvOrdObj.put("OSS_TASK_ID", svrOrderId);
                    //2.1  查询所有相关数据
                    List<Map<String, Object>> parallelRelated = orderDealService.queryParallelRelated(svrOrderId);
                    if(Arrays.asList(BACK_TYPE).contains(backReasonType)){
                        String backScope = "localA".equals(parallelFlag) ? "1":"2";
                        srvOrdObj.put("BACK_SCOPE", backScope);
                        //2.2根据单子所在端反馈退单范围 并作废相关单子 单端退 只作废单端
                        flowActionHandler.cancelOrder(userInfo.getStaffId().toString(), orderId);
                        orderDealService.updateOrdInfoById("10X",svrOrderId);
                     }else {
                        //2.3. 整体退单 二干本地AZ端都要退
                        srvOrdObj.put("BACK_SCOPE", "0");
                        orderDealService.updateOrdInfo("10X", MapUtils.getString(srvmp,"TRADE_ID"), MapUtils.getString(srvmp,"SERIAL_NUMBER"));
                        for (Map<String,  Object> relatedInfo: parallelRelated) {
                            flowActionHandler.cancelOrder(userInfo.getUserId(), MapUtils.getString(relatedInfo,"ORDER_ID"));
                        }
                    }
                }
                srvOrdObj.put("BACK_ORDER_DESC",backOrderDesc);
                srvOrd.add(srvOrdObj);
            }
            srvOrdList.put("SRV_ORD",srvOrd);
        }
        cstOrd.put("SRV_ORD_LIST",srvOrdList);
        backOrderReq.put("CST_ORD",cstOrd);
        if (paraList.size()>0) {
            List<JSONObject> para = new ArrayList<>();
            for (int i = 0; paraList.size() - i > 0; i++) {
                HashMap paramp = (HashMap) paraList.get(i);
                JSONObject paraObj = new JSONObject();
                paraObj.put("PARA_ID", MapUtils.getString(paramp, "PARA_ID"));
                paraObj.put("PARA_VALUE", MapUtils.getString(paramp, "PARA_VALUE"));
                para.add(paraObj);
            }
            backOrderReq.put("PARA",para);
        }
        body.put("BACK_ORDER_REQ",backOrderReq);
        json.put("UNI_BSS_BODY",body);
        return json.toString();
    }

    /**
     * ddos退集客拼接报文。【此时方法用于集客收单接口中还未完成起流程和集客数据入库时直接退单集客系统使用】
     * @param map
     * @return
     */
    public String ddosJsonStr(Map map){
        String subscribeId = MapUtils.getString(map, "SUBSCRIBE_ID");
        String serialNumber = MapUtils.getString(map, "SERIAL_NUMBER");
        String flowId = MapUtils.getString(map, "FLOW_ID");
        String tradeId = MapUtils.getString(map, "TRADE_ID");
        String backOrderDesc = MapUtils.getString(map, "backExec"); //退单原因
        String routeType = MapUtils.getString(map, "ROUTE_TYPE");
        String routeValue = MapUtils.getString(map, "ROUTE_VALUE");
        JSONObject json = new JSONObject();
        //HEAD
        json.put("UNI_BSS_HEAD",xmlUtil.requestHeader());

        //body
        JSONObject body = new JSONObject();
        JSONObject backOrderReq = new JSONObject();

        JSONObject routing = new JSONObject();
        routing.put("ROUTE_TYPE", routeType);
        // 路由关键值:01:集客综合订单;02:集客订单中心;03:政企订单中心
        routing.put("ROUTE_VALUE", routeValue);
        backOrderReq.put("ROUTING",routing);
        JSONObject cstOrd = new JSONObject();
        cstOrd.put("SUBSCRIBE_ID", subscribeId);
        JSONObject srvOrdList = new JSONObject();

            List<JSONObject> srvOrd = new ArrayList<>();
        JSONObject srvOrdObj = new JSONObject();
        srvOrdObj.put("SERIAL_NUMBER",serialNumber);
        srvOrdObj.put("FLOW_ID",flowId);
        srvOrdObj.put("TRADE_ID",tradeId);
        srvOrdObj.put("BACK_ORDER_DESC",backOrderDesc);

        srvOrd.add(srvOrdObj);
        srvOrdList.put("SRV_ORD",srvOrd);
        cstOrd.put("SRV_ORD_LIST",srvOrdList);
        backOrderReq.put("CST_ORD",cstOrd);
        body.put("BACK_ORDER_REQ",backOrderReq);
        json.put("UNI_BSS_BODY",body);
        return json.toString();
    }
}
