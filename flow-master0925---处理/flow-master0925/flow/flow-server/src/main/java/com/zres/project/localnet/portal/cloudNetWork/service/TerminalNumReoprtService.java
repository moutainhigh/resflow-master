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
 * @ClassName TerminalNumReoprtService
 * @Description TODO    终端盒序列号上报接口
 * @Author wang.g2
 * @Date 2020/10/12 19:11
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@Service
public class TerminalNumReoprtService implements TerminalNumReoprtIntf {
    private static Logger logger = LoggerFactory.getLogger(TerminalNumReoprtService.class);
    @Autowired
    private XmlUtil xmlUtil;
    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private CloudNetworkInterfaceDao cloudNetworkInterfaceDao;

    @Override
    public Map<String,Object> terminalNumReport(@RequestParam Map<String,Object> map){
        //拼接请求报文
        String jsonReqStr = "";
        logger.info("发送云组网终端盒序列号上报接口 request：" + jsonReqStr);
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> retMap = new HashMap<>();
        Map<String, Object> param = new HashMap<>();
        String requestJson = "";
        String tradeId = "";

        List<Map<String,Object>> circuitData = (List<Map<String,Object>>) MapUtils.getObject(map, "circuitData");
        String srvOrdId = MapUtils.getString(map, "SRV_ORD_ID");
        param.put("DEVICE_MAC", MapUtils.getString(map,"DEVICE_MAC"));
        param.put("ADDRESS", MapUtils.getString(map,"ADDRESS"));
        param.put("BUILDING_NAME", MapUtils.getString(map,"BUILDING_NAME"));
        param.put("LONGITUDE", MapUtils.getString(map,"LONGITUDE"));
        param.put("LATITUDE", MapUtils.getString(map,"LATITUDE"));
        param.put("UPLINK_MODE", MapUtils.getString(map,"UPLINK_MODE"));  //上联模式  single：单上联，dual：双上联

        try{
                //查询终端盒子序列号信息
                if(!MapUtils.isEmpty(param)){
                    //调用云组网 终端盒序列号上报接口
                    retMap = queryCloudNetworkInfo(map);
                    String url = wsd.queryUrl("terminalNumReport");
                    response = xmlUtil.sendHttpPostOrderCenter(url,requestJson);
                    String code = MapUtils.getString(response,"code","");
                    if("200".equals(code)){
                        String responseInfo = MapUtils.getString(response,"msg");
                        Map respJson = JSONObject.parseObject(responseInfo,Map.class);
                        //0：成功；-1：失败；
                        String respDesc = MapUtils.getString(respJson,"MESSAGE","云组网终端盒序列号上报接口交互异常");//失败原因 -1时必填
                        if("0".equals(MapUtils.getString(respJson,"CODE"))){
                            Map<String,Object> workSheetNumber = MapUtils.getMap(respJson,"WORK_SHEET_NUMBER");
                            Map<String,Object> data = MapUtils.getMap(respJson,"DATA");
                            String status = MapUtils.getString(data,"status");
                            String reason = MapUtils.getString(respJson,"reason","云组网终端盒序列号上报接口交互异常");//失败原因 -1时必填
                            //TODO  暂时没有反馈需要待确认
                            retMap.put("success",true);
                            retMap.put("message",reason);
                        } else{
                            retMap.put("success",false);
                            retMap.put("message",respDesc);
                        }
                    }
                    else{
                        retMap.put("success",false);
                        retMap.put("message","接口交互失败："+code);
                    }
                }
            } catch (Exception e) {
                retMap.put("success", false);
                retMap.put("message", "接口交互失败："+e.getMessage());
            } finally {
                //记接口日志
                insertInterfaceLog(tradeId,srvOrdId,requestJson,JSONObject.toJSONString(response));
                logger.info("发送云组网终端盒序列号上报接口 request：" + JSONObject.toJSONString(response));
            }

        return retMap;
    }
    /**
     * @Description 功能描述: 记录接口日志
     * @Param: [tradeId, srvOrdId, request, respone]
     * @Return: void
     * @Author: wang.gang2
     * @Date: 2020/10/12 19:14
     */
    private void insertInterfaceLog(String tradeId,String srvOrdId,String request,String respone){
        Map<String,Object> interflog = new HashMap<String, Object>();
        interflog.put("INTERFNAME","云组网终端盒序列号上报接口");
        interflog.put("URL","cloudNetWork/service/TerminalNumReoprtService");
        interflog.put("CONTENT",request);
        interflog.put("RETURNCONTENT",respone);
        interflog.put("ORDERNO",tradeId);
        interflog.put("SRV_ORD_ID",srvOrdId);
        interflog.put("REMARK","发送云组网终端盒序列号上报接口 json报文");
        wsd.insertInterfLog(interflog);
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
