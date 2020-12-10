package com.zres.project.localnet.portal.cloudNetWork.service;

import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.cloudNetWork.dao.CloudNetworkInterfaceDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.util.XmlUtil;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import com.ztesoft.res.frame.user.inf.UserInfo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName OrderFeedBackService
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/11/23 10:46
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@Service
public class OrderFeedBackService implements OrderFeedBackIntf {
    private static Logger logger = LoggerFactory.getLogger(OrderFeedBackService.class);

    @Autowired
    private WebServiceDao webServiceDao;
    @Autowired
    private XmlUtil xmlUtil;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private CloudNetworkInterfaceDao cloudNetworkInterfaceDao;

    @Override
    public Map<String, Object> orderFeedBack(@RequestParam Map<String, Object> params) {
        Map<String, Object> reqMap = new HashMap<>();
        Map<String, Object> retMap = new HashMap<>();
        String srvOrdId = MapUtils.getString(params, "srvOrdId");
        //拼接请求报文
        reqMap= queryCloudNetworkInfo(params);

        logger.info("云组网退单接口请求报文 ：" + JSONObject.toJSONString(reqMap));

        String url = webServiceDao.queryUrl("orderFeedBack");
        Map constructResponse = xmlUtil.sendHttpPostOrderCenter(url, JSONObject.toJSONString(reqMap));
        logger.info("云组网退单接口反馈报文 ：" + JSONObject.toJSONString(constructResponse));
        //插入接口记录
        Map<String, Object> interflog = new HashMap<String, Object>();
        interflog.put("INTERFNAME", "云组网退单接口");
        interflog.put("URL", url);
        interflog.put("CONTENT", JSONObject.toJSONString(reqMap));
        interflog.put("REMARK", "云组网退单接口请求报文");
        interflog.put("ORDERNO", srvOrdId);
        interflog.put("RETURNCONTENT", JSONObject.toJSONString(constructResponse));
        webServiceDao.insertInterfLog(interflog);

        String code = MapUtils.getString(constructResponse, "code", "");
        if ("200".equals(code)) {
            String response = MapUtils.getString(constructResponse, "msg");
            Map respJson = JSONObject.parseObject(response, Map.class);
            //0：成功；-1：失败；
            String respDesc = MapUtils.getString(respJson, "MESSAGE", "云组网退单接口交互异常");//失败原因 -1时必填
            if ("0".equals(MapUtils.getString(respJson, "CODE"))) {
                Map<String, Object> data = MapUtils.getMap(respJson, "DATA");
                Map<String, Object> body = MapUtils.getMap(data, "UNI_BSS_BODY");
                Map<String, Object> orderRep = MapUtils.getMap(body, "BACK_ORDER_RSP");
                String desc = MapUtils.getString(orderRep, "RESP_DESC","退单成功");

                //TODO 反馈前端
                retMap.put("success", true);
                retMap.put("message", desc);
            } else {
                retMap.put("success", false);
                retMap.put("message", respDesc);
            }
        } else {
            retMap.put("success", false);
            retMap.put("message", "接口交互失败：" + code);
        }
        return retMap;
    }
    /**
     * @Description 功能描述: 拼接请求报文
     * @Param: [params]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wang.gang2
     * @Date: 2020/11/23 14:39
     */
    private Map<String,Object> queryCloudNetworkInfo(Map<String,Object> params){
        String flag = null;
        List<Map<String, Object>> srvOrdList = new ArrayList<>();
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> orderInfo = new HashMap<>();
        Map<String, Object> operator = new HashMap<>();
        Map<String, Object> param = new HashMap<>();
        Map<String, Object> reqBody = new HashMap<>(); //
        Map<String, Object> backOrder = new HashMap<>(); //
        Map<String, Object> cstOrd  = new HashMap<>(); //
        Map<String, Object> routing  = new HashMap<>(); //
        Map<String, Object> srvOrd  = new HashMap<>(); //
        Map<String, Object> dealInfo  = new HashMap<>(); //
        Map<String, Object> backOrderDesc  = new HashMap<>(); //
        Map<String, Object> para  = new HashMap<>(); //

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        String operDate = df.format(new Date());
        para.put("PARA_ID", "");
        para.put("PARA_VALUE", "1");
        backOrderDesc.put("PARA", para);
        UserInfo user = ThreadLocalInfoHolder.getLoginUser();
        Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(user.getUserId()));

        Map<String, Object> cloudNetworkInfo = cloudNetworkInterfaceDao.queryCloudNetworkInfo(params);
        orderInfo.put("SVR_CODE",MapUtils.getString(cloudNetworkInfo,"SVR_CODE"));
        orderInfo.put("FLOW_ID", MapUtils.getString(cloudNetworkInfo,"FLOW_ID"));
        orderInfo.put("SERIAL_NUMBER",MapUtils.getString(cloudNetworkInfo,"SERIAL_NUMBER"));
        orderInfo.put("TRADE_ID",MapUtils.getString(cloudNetworkInfo,"TRADE_ID"));

        srvOrd.put("FLOW_ID", MapUtils.getString(cloudNetworkInfo,"FLOW_ID"));
        srvOrd.put("SERIAL_NUMBER",MapUtils.getString(cloudNetworkInfo,"SERIAL_NUMBER"));
        srvOrd.put("TRADE_ID",MapUtils.getString(cloudNetworkInfo,"TRADE_ID"));
        //localA、 localZ、 B PARALLEL_FLAG FROM gom_bdw_srv_ord_info
        String parallelFlag = MapUtils.getString(cloudNetworkInfo, "PARALLEL_FLAG");
        switch(parallelFlag){
            case "localZ" :
                flag = "z";
                break;
            case "localA" :
                flag = "a";
                break;
            case "B" :
                flag = "az";
                break;
            default:
                flag = "";
                break;
        }
        orderInfo.put("AZ",flag);

        operator.put("USER_NAME", user.getUserName());
        operator.put("PHONE", MapUtils.getString(operStaffInfoMap,"USER_PHONE"));

        dealInfo.put("OPERATOR_NAME", user.getUserName());
        dealInfo.put("OPERATOR_TEL", MapUtils.getString(operStaffInfoMap,"USER_PHONE"));
        dealInfo.put("OPERATE_TIME", operDate);
        operator.put("PROVINCE", MapUtils.getString(operStaffInfoMap,"AREANAME"));
        operator.put("CITY", "");
        operator.put("COMPANY", MapUtils.getString(operStaffInfoMap,"ORG_NAME"));

        srvOrd.put("DEAL_INFO", dealInfo);
        srvOrd.put("BACK_ORDER_DESC", backOrderDesc);
        srvOrdList.add(srvOrd);
        cstOrd.put("SUBSCRIBE_ID", MapUtils.getString(cloudNetworkInfo,"SUBSCRIBE_ID"));
        cstOrd.put("SRV_ORD_LIST", srvOrdList);
        routing.put("ROUTE_TYPE", "01");
        routing.put("ROUTE_VALUE", "0");
        backOrder.put("ROUTING", routing);
        backOrder.put("CST_ORD", cstOrd);
        reqBody.put("BACK_ORDER_REQ", backOrder);
        param.put("UNI_BSS_BODY", reqBody);


        body.put("ORDER_INFO", orderInfo);
        body.put("OPERATOR", operator);
        body.put("WORK_SHEET_NUMBER", "");
        body.put("param", param);
        return body;
    }
}
