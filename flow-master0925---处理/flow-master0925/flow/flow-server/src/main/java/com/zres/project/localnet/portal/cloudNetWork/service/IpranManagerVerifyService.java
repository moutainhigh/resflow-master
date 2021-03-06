package com.zres.project.localnet.portal.cloudNetWork.service;

import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.cloudNetWork.dao.CloudNetworkInterfaceDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.util.XmlUtil;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import com.ztesoft.res.frame.user.inf.UserInfo;
import javassist.tools.web.Webserver;
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
 * @ClassName IpranManagerVerifyService
 * @Description TODO   IPRAN资源网管验证
 * @Author wang.g2
 * @Date 2020/10/12 20:36
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@Service
public class IpranManagerVerifyService implements IpranManagerVerifyIntf{
    private static Logger logger = LoggerFactory.getLogger(IpranManagerVerifyService.class);

    @Autowired
    private WebServiceDao webServiceDao;
    @Autowired
    private XmlUtil xmlUtil;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private CloudNetworkInterfaceDao cloudNetworkInterfaceDao;

    @Override
    public Map<String,Object> ipranManagerVerify(@RequestParam Map<String,Object> params){
        Map<String, Object> reqMap = new HashMap<>();

        Map<String, Object> param = new HashMap<>();
        Map<String, Object> retMap = new HashMap<>();
        String srvOrdId = MapUtils.getString(params,"srvOrdId");
        String province = MapUtils.getString(params,"province");//省份编码要做转换
        String city = MapUtils.getString(params,"city");//省份编码要做转换
        //TODO 看是不是需要转换
        param.put("province", province);
        param.put("city", city);
        param.put("ipranName", MapUtils.getString(params,"ipranName"));
        param.put("deviceAddress", MapUtils.getString(params,"deviceAddress"));
        param.put("interfaceName", MapUtils.getString(params,"interfaceName"));
        param.put("vlanId", MapUtils.getString(params,"vlanId"));
        reqMap = queryCloudNetworkInfo(params);

        reqMap.put("PARAM", param);
        logger.info("云组网IPRAN资源网管验证接口请求报文 ：" + JSONObject.toJSONString(reqMap));
        //IPRAN资源网管验证接口地址
        String url = webServiceDao.queryUrl("ipranManagerVerify");
        Map constructResponse = xmlUtil.sendHttpPostOrderCenter(url,JSONObject.toJSONString(reqMap));
        logger.info("云组网IPRAN资源网管验证接口反馈报文 ：" + JSONObject.toJSONString(constructResponse));
        String code = MapUtils.getString(constructResponse,"code","");
        //插入接口记录
        Map<String,Object> interflog = new HashMap<String, Object>();
        interflog.put("INTERFNAME","云组网IPRAN资源网管验证接口");
        interflog.put("URL",url);
        interflog.put("CONTENT",JSONObject.toJSONString(reqMap));
        interflog.put("REMARK","云组网IPRAN资源网管验证接口请求报文");
        interflog.put("ORDERNO",srvOrdId);
        interflog.put("RETURNCONTENT", JSONObject.toJSONString(constructResponse));
        webServiceDao.insertInterfLog(interflog);

        if("200".equals(code)){
            String response = MapUtils.getString(constructResponse,"msg");
            Map respJson = JSONObject.parseObject(response,Map.class);
            //0：成功；-1：失败；
            String respDesc = MapUtils.getString(respJson,"MESSAGE","云组网IPRAN资源网管验证接口交互异常");//失败原因 -1时必填
            if("0".equals(MapUtils.getString(respJson,"CODE"))){
                Map<String,Object> data = MapUtils.getMap(respJson,"DATA");

                //TODO  跟需求确认后这个数据是反馈到前端选择之后保存
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
