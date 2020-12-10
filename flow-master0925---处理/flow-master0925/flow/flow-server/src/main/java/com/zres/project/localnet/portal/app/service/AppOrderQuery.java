package com.zres.project.localnet.portal.app.service;

import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.app.AppOrderQueryIntf;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName AppOrderQuery
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/6/10 11:18
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@RestController
public class AppOrderQuery implements AppOrderQueryIntf {
    @Autowired
    OrderDetailsServiceIntf orderDetailsServiceIntf;
    @Autowired
    private WebServiceDao wsd;
    @IgnoreSession
    @RequestMapping(value = "/interfaceBDW/queryOrderDeatilsInfo", produces = "application/json;charset=UTF-8")
    @Override
    public Map<String, Object> queryOrderDeatilsInfo(@RequestBody String request) {

        Map<String,Object> returnMap = new HashMap();
        Map<String,Object> orderDeatilsInfo = new HashMap();
        Map<String,Object> logInfo = new HashMap<String, Object>();
        logInfo.put("interfname","queryOrderDeatilsInfo工单详情---定单信息查询");
        logInfo.put("url","/interfaceBDW/queryOrderDeatilsInfo");
        logInfo.put("request",request);
        logInfo.put("remark","接收app 工单详情---定单信息查询接口 json报文");

        try {
            Map params = JSONObject.parseObject(request, Map.class);
            logInfo.put("tradeId",MapUtils.getString(params, "cstOrdId"));
            String staffId = MapUtils.getString(params, "staffId");
            if(StringUtils.isEmpty(staffId)){
                returnMap.put("code", false);
                returnMap.put("msg", "请检查用户id 不能为空");
                logInfo.put("respone",returnMap);
                insertInterfaceLog(logInfo);
                return returnMap;
            }
            boolean ifFromSecond = false;
            if(!StringUtils.isEmpty(MapUtils.getString(params, "orderId"))){
                // 是否是二干来单
                 ifFromSecond = orderDetailsServiceIntf.ifFromSecond(MapUtils.getString(params, "orderId"));
            }
            //客户信息
            Map<String, Object> custInfo = orderDetailsServiceIntf.queryConsumerInfoByCustId(MapUtils.getString(params, "cstOrdId"));
            custInfo.put("IFFROMSECOND",ifFromSecond);
            orderDeatilsInfo.put("custInfo", custInfo);
            //订单信息
            List<Map<String, Object>> orderInfo = orderDetailsServiceIntf.queryOrderDeatilsInfo(MapUtils.getString(params, "SRVORDIDS"));
            orderDeatilsInfo.put("orderInfo", orderInfo);
            //附件信息
            List<Map<String, Object>> attachInfo = orderDetailsServiceIntf.queryApplyAttachInfo(params);
            orderDeatilsInfo.put("attachInfo", attachInfo);

            returnMap.put("code", true);
            returnMap.put("msg", "成功");
            returnMap.put("data", orderDeatilsInfo);
            logInfo.put("respone",JSONObject.toJSONString(returnMap));
        } catch (Exception e) {
            returnMap.put("code", false);
            returnMap.put("msg", "查询失败");
            logInfo.put("respone",e.getMessage());
        } finally {
            insertInterfaceLog(logInfo);
        }
        return returnMap;
    }


    /**
     * 记录接口日志
     * @param logInfo
     */
    private void insertInterfaceLog(Map<String,Object> logInfo){
        Map<String,Object> interflog = new HashMap<String, Object>();
        interflog.put("INTERFNAME", MapUtils.getString(logInfo,"interfname"));
        interflog.put("URL",MapUtils.getString(logInfo,"url"));
        interflog.put("CONTENT", MapUtils.getString(logInfo,"request"));
        interflog.put("RETURNCONTENT",MapUtils.getString(logInfo,"respone"));
        interflog.put("ORDERNO",MapUtils.getString(logInfo,"tradeId"));
        interflog.put("REMARK", MapUtils.getString(logInfo,"remark"));
        wsd.insertInterfLog(interflog);
    }
}
