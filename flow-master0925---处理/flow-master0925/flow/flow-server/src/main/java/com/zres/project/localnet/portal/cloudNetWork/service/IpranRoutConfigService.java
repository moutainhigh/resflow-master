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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName IpranRoutConfigService
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/10/27 17:02
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@Service
public class IpranRoutConfigService implements IpranRoutConfigIntf{
    private static Logger logger = LoggerFactory.getLogger(IpranRoutConfigService.class);

    @Autowired
    private WebServiceDao webServiceDao;
    @Autowired
    private XmlUtil xmlUtil;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private CloudNetworkInterfaceDao cloudNetworkInterfaceDao;

    @Override
    public Map<String, Object> routConfigNotifi(@RequestParam Map<String, Object> params) {
        Map<String, Object> reqMap = new HashMap<>();
        Map<String, Object> param = new HashMap<>();
        Map<String, Object> servRoute = new HashMap<>();
        Map<String, Object> operInfo = new HashMap<>();
        Map<String, Object> retMap = new HashMap<>();

        String srvOrdId = MapUtils.getString(params, "srvOrdId");
        // 业务路由相关信息
        servRoute.put("sourceIp", MapUtils.getString(params, "sourceIp")); //源ip
        servRoute.put("sourceMask", MapUtils.getString(params, "sourceMask")); //源ip掩码
        servRoute.put("destIp", MapUtils.getString(params, "destIp")); //目的ip
        servRoute.put("destMask", MapUtils.getString(params, "destMask")); //目的ip掩码
        servRoute.put("plane", MapUtils.getString(params, "plane")); //平面属性
        //配置相关信息
        operInfo.put("deviceAddress", MapUtils.getString(params, "deviceAddress")); //设备loopback地址
        operInfo.put("interfaceName", MapUtils.getString(params, "interfaceName")); //设备物理端口名称
        operInfo.put("vlanId", MapUtils.getString(params, "vlanId")); //vlan号
        operInfo.put("ipAddress", MapUtils.getString(params, "ipAddress")); //接口ip地址
        operInfo.put("mask", MapUtils.getString(params, "mask")); //子网掩码
        operInfo.put("l3vpn", MapUtils.getString(params, "l3vpn")); //绑定的三层vpn名称

        String province = MapUtils.getString(params,"province");//省份编码要做转换
        String city = MapUtils.getString(params,"city");//省份编码要做转换
        //TODO 看是不是需要转换
        param.put("province", province);
        param.put("city", city);
        param.put("deviceType", MapUtils.getString(params, "deviceType")); //设备类型  ipran｜m3
        param.put("operType", MapUtils.getString(params, "operType")); //操作类型  add：开通，del：关闭

        param.put("servRoute",servRoute);//业务路由相关信息
        param.put("operInfo",operInfo);//配置相关信息

        //拼接请求报文
        reqMap = queryCloudNetworkInfo(params);
        reqMap.put("PARAM", param);
        logger.info("云组网下联端口选择接口请求报文 ：" + JSONObject.toJSONString(reqMap));

        String url = webServiceDao.queryUrl("routConfigNotifi");
        Map constructResponse = xmlUtil.sendHttpPostOrderCenter(url, JSONObject.toJSONString(reqMap));
        logger.info("云组网IPRAN业务路由配置下发通知接口反馈报文 ：" + JSONObject.toJSONString(constructResponse));
        //插入接口记录
        Map<String, Object> interflog = new HashMap<String, Object>();
        interflog.put("INTERFNAME", "云组网IPRAN业务路由配置下发通知接口");
        interflog.put("URL", url);
        interflog.put("CONTENT", JSONObject.toJSONString(reqMap));
        interflog.put("REMARK", "云组网IPRAN业务路由配置下发通知接口请求报文");
        interflog.put("ORDERNO", srvOrdId);
        interflog.put("RETURNCONTENT", JSONObject.toJSONString(constructResponse));
        webServiceDao.insertInterfLog(interflog);

        String code = MapUtils.getString(constructResponse, "code", "");
        if ("200".equals(code)) {
            String response = MapUtils.getString(constructResponse, "msg");
            Map respJson = JSONObject.parseObject(response, Map.class);
            //0：成功；-1：失败；
            String respDesc = MapUtils.getString(respJson, "MESSAGE", "云组网IPRAN业务路由配置下发通知接口交互异常");//失败原因 -1时必填
            if ("0".equals(MapUtils.getString(respJson, "CODE"))) {
                Map<String, Object> data = MapUtils.getMap(respJson, "DATA");
                String status = MapUtils.getString(data, "status");
                String reason = MapUtils.getString(respJson, "reason", "云组网IPRAN业务路由配置下发通知接口成功");//失败原因 -1时必填
                //TODO 反馈前端
                retMap.put("success", true);
                retMap.put("message", reason);
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
