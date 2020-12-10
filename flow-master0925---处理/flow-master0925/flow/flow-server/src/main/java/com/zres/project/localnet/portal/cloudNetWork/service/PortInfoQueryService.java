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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName PortInfoQueryService
 * @Description TODO 下联端口查询接口
 * @Author wang.g2
 * @Date 2020/10/12 19:50
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@Service
public class PortInfoQueryService implements PortInfoQueryIntf{
    private static Logger logger = LoggerFactory.getLogger(PortInfoQueryService.class);

    @Autowired
    private WebServiceDao webServiceDao;
    @Autowired
    private XmlUtil xmlUtil;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private CloudNetworkInterfaceDao cloudNetworkInterfaceDao;

    @Override
    public Map<String,Object> portInfoQuery(@RequestParam Map<String,Object> params){
        Map<String, Object> requestMap = new HashMap<>();
        Map<String, Object> retMap = new HashMap<>();

        Map<String, Object> param = new HashMap<>();
        String srvOrdId = MapUtils.getString(params, "srvOrdId");
        //拼接请求报文

        param.put("NETWORK_TYPE", MapUtils.getString(params, "NETWORK_TYPE"));
        param.put("NETWORK_MODE", MapUtils.getString(params, "NETWORK_MODE"));
        param.put("IS_VLAN", MapUtils.getString(params, "IS_VLAN"));
        param.put("AGGSERV_CODE", MapUtils.getString(params, "AGGSERV_CODE"));
        param.put("PORT_TYPE", MapUtils.getString(params, "PORT_TYPE"));
        param.put("PORT_TYPE", MapUtils.getString(params, "PORT_TYPE"));

        requestMap = queryCloudNetworkInfo(params);
        requestMap.put("PARAM", param);
        logger.info("云组网下联端口查询接口请求报文 ：" + JSONObject.toJSONString(requestMap));
        String url = webServiceDao.queryUrl("portInfoQuery");
        Map response = xmlUtil.sendHttpPostOrderCenter(url, JSONObject.toJSONString(requestMap));
        logger.info("云组网云组网下联端口查询接口请求报文 ：" + JSONObject.toJSONString(response));
        //插入接口记录
        Map<String,Object> interflog = new HashMap<String, Object>();
        interflog.put("INTERFNAME","云组网下联端口查询接口");
        interflog.put("URL",url);
        interflog.put("CONTENT",JSONObject.toJSONString(requestMap));
        interflog.put("REMARK","云组网下联端口查询接口请求报文");
        interflog.put("ORDERNO",srvOrdId);
        interflog.put("RETURNCONTENT", JSONObject.toJSONString(response));
        webServiceDao.insertInterfLog(interflog);

        String code = MapUtils.getString(response,"code","");
        if("200".equals(code)){
            String responses = MapUtils.getString(response,"msg");
            Map respJson = JSONObject.parseObject(responses,Map.class);
            //0：成功；-1：失败；
            String respDesc = MapUtils.getString(respJson,"MESSAGE","云组网下联端口查询接口交互异常");//失败原因 -1时必填
            if("0".equals(MapUtils.getString(respJson,"CODE"))){
                Map<String,Object> data = MapUtils.getMap(respJson,"DATA");
                String status = MapUtils.getString(data,"status");
                String reason = MapUtils.getString(respJson,"reason","云组网下联端口查询成功");//失败原因 -1时必

                //TODO 需求确认反馈前端保存
                retMap.put("success",true);
                retMap.put("message",data);
            } else{
                retMap.put("success",false);
                retMap.put("message",respDesc);
            }
        } else{
            retMap.put("success",false);
            retMap.put("message","接口交互失败："+code);
        }

        return retMap;
    }

    /**
     * @Description 功能描述: 拼接固定请求参数
     * @Param: []
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wang.gang2
     * @Date: 2020/11/20 15:05
     *  "ORDER_INFO": {
     *         "FLOW_ID": "497818058",
     *         "SERIAL_NUMBER": "831HLW000418",
     *         "SVR_CODE": "Y0371037100079",
     *         "TRADE_ID": "",
     *         "AZ": "a"
     *     },
     *    "OPERATOR": {
     *         "USER_NAME": "hqzhangs",
     *         "PHONE": "15833164458",
     *         "PROVINCE": "河南省",
     *         "CITY": "郑州市",
     *         "COMPANY": "中讯邮电"
     *     },
     *     "WORK_SHEET_NUMBER": "",
     */
    private Map<String,Object> queryCloudNetworkInfo(Map<String,Object> params){
        String flag = null;
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> orderInfo = new HashMap<>();
        Map<String, Object> operator = new HashMap<>();
        UserInfo user = ThreadLocalInfoHolder.getLoginUser();
        Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(user.getUserId()));

        Map<String, Object> cloudNetworkInfo = cloudNetworkInterfaceDao.queryCloudNetworkInfo(params);
        orderInfo.put("FLOW_ID", MapUtils.getString(cloudNetworkInfo,"FLOW_ID"));
        orderInfo.put("SERIAL_NUMBER",MapUtils.getString(cloudNetworkInfo,"SERIAL_NUMBER"));
        orderInfo.put("SVR_CODE",MapUtils.getString(cloudNetworkInfo,"SVR_CODE"));
        orderInfo.put("TRADE_ID",MapUtils.getString(cloudNetworkInfo,"TRADE_ID"));
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
        operator.put("PROVINCE", MapUtils.getString(operStaffInfoMap,"AREANAME"));
        operator.put("CITY", "");
        operator.put("COMPANY", MapUtils.getString(operStaffInfoMap,"ORG_NAME"));

        body.put("ORDER_INFO", orderInfo);
        body.put("OPERATOR", operator);
        body.put("WORK_SHEET_NUMBER", "");
        return body;
    }

}
