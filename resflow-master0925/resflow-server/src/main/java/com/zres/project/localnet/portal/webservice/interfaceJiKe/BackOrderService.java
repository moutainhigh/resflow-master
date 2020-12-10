package com.zres.project.localnet.portal.webservice.interfaceJiKe;

import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealService;
import com.zres.project.localnet.portal.util.XmlUtil;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.zres.project.localnet.portal.webservice.flow.ExceptionChangeService;

import com.ztesoft.res.frame.core.util.MapUtil;
import com.ztesoft.res.frame.flow.common.service.FlowActionHandler;
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
    @Autowired
    private XmlUtil xmlUtil;
    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private OrderDealService orderDealService;
    @Autowired
    private FlowActionHandler flowActionHandler;

    //    @GetMapping(value = "/interfaceBDW/backOrder.spr")
    public Map backOrder(@RequestParam Map map) {

        Map returnMap =new HashMap();
        Boolean flag= map.keySet().contains("activeType")&&
                ExceptionChangeService.EXCEPTION_4A.equals(MapUtils.getString(map,"activeType"));

        //客户流水号
        String srvOrdId = map.get("srvOrdId").toString();
        String backExec = map.get("backExec").toString();

        map.put("flag",flag);
        String jsonReqStr = wrapJsonStr(map);
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
            try{
                JSONObject msg = JSONObject.parseObject(jkResponse.get("msg").toString());
                String uniBssBody = msg.getString("UNI_BSS_BODY");
                JSONObject jsUniBssBody = JSONObject.parseObject(uniBssBody);
                JSONObject jsFinishOrderRsp = JSONObject.parseObject(jsUniBssBody.getString("BACK_ORDER_RSP"));
                respCode = jsFinishOrderRsp.getString("RESP_CODE");//处理结果0：成功；1：失败，报文错误；2：失败，附件没有
                respDesc = jsFinishOrderRsp.getString("RESP_DESC");//处理原因
            } catch (Exception e){
                respCode = "-1";
                respDesc = "返回报文格式错误，请联系管理员！";
                returnMap.put("RESP_CODE",respCode);
                returnMap.put("RESP_DESC",respDesc);
                return returnMap;
            }
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
        String backOrderDesc = MapUtils.getString(map, "backExec"); //退单原因
        String productType = MapUtils.getString(map, "serviceId"); //产品类型
        logger.info("*****ren.jiahang***产品编码*********"+productType);
        List<Map<String, Object>> srvList = orderDealService.selectOrderInfoList(svrOrderId);
        Map mp=srvList.get(0);
        DefaultLoginUserInfo userInfo = (DefaultLoginUserInfo)ThreadLocalInfoHolder.getLoginUser();
        List<Map<String, Object>> staff = orderDealService.selectStaffInfoList(userInfo.getStaffId().toString());
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
                Map srvmp= srvList.get(i);
                JSONObject srvOrdObj = new JSONObject();
                srvOrdObj.put("SERIAL_NUMBER",MapUtils.getString(srvmp,"SERIAL_NUMBER"));
                String parallelFlag = MapUtils.getString(srvmp, "PARALLEL_FLAG");
                if(MapUtils.getBoolean(map,"flag",false)){
                    srvOrdObj.put("TRADE_ID", map.get("TRADE_ID"));
                    srvOrdObj.put("FLOW_ID", map.get("FLOW_ID"));
                }else {
                    srvOrdObj.put("TRADE_ID",MapUtils.getString(srvmp,"TRADE_ID"));
                    srvOrdObj.put("FLOW_ID",MapUtils.getString(srvmp,"FLOW_ID"));
                }

                SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
                JSONObject dealInfo = new JSONObject();
                dealInfo.put("OPERATOR_NAME",(MapUtil.getString(staff.get(0),"NAME")));
                dealInfo.put("OPERATOR_TEL",(MapUtil.getString(staff.get(0),"PHONE_NO")));
                dealInfo.put("OPERATE_TIME",timeFormatter.format(Calendar.getInstance().getTime()));
                srvOrdObj.put("DEAL_INFO",dealInfo);

                //modify by wang.gang2  并行核查 退单 新增退单范围 退单类型 以及退单说明
                if("B".equals(parallelFlag)){
                    //1.跟据选择的退单类型 反馈退单范围
                    String backReasonType = MapUtils.getString(map, "backReasonType");
                    srvOrdObj.put("BACK_REASON_TYPE", backReasonType);
                    srvOrdObj.put("OSS_TASK_ID", svrOrderId);
                    if(!StringUtils.isEmpty(backReasonType)){
                        srvOrdObj.put("BACK_SCOPE", "0");
                        List<Map<String, Object>> parallelRelated = orderDealService.queryParallelRelated(svrOrderId);
                        for (Map<String,  Object> relatedInfo: parallelRelated) {
                            flowActionHandler.cancelOrder(userInfo.getStaffId().toString(), MapUtils.getString(relatedInfo,"ORDER_ID"));
                        }
                        //2.根据单子所在端反馈退单范围 并作废相关单子 单端退 只作废单端
                        orderDealService.updateOrdInfo("10X", MapUtils.getString(srvmp,"TRADE_ID"), MapUtils.getString(srvmp,"SERIAL_NUMBER"));
                    }
                }

                srvOrdObj.put("BACK_ORDER_DESC",backOrderDesc);
                srvOrd.add(srvOrdObj);
            }
            srvOrdList.put("SRV_ORD",srvOrd);
        }
        cstOrd.put("SRV_ORD_LIST",srvOrdList);
        backOrderReq.put("CST_ORD",cstOrd);
        //政企精品网
        if ("80000466".equals(productType)) {
            List<JSONObject> para = new ArrayList<>();
            JSONObject paraObj = new JSONObject();
            paraObj.put("PARA_ID","back_type"); //默认back_type"
            paraObj.put("PARA_VALUE", "0"); //默认0
            para.add(paraObj);
            backOrderReq.put("PARA",para);
        }
        body.put("BACK_ORDER_REQ",backOrderReq);
        json.put("UNI_BSS_BODY",body);
        return json.toString();
    }

}
