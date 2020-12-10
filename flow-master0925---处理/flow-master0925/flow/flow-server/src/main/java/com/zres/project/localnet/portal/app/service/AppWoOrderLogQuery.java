package com.zres.project.localnet.portal.app.service;

import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.app.AppWoOrderLogQueryIntf;
import com.zres.project.localnet.portal.app.data.dao.AppQueryDao;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName AppWoOrderLogQuery
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/6/17 12:40
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@RestController
public class AppWoOrderLogQuery implements AppWoOrderLogQueryIntf {
    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private OrderDealServiceIntf orderDetailsServiceIntf;

    @IgnoreSession
    @RequestMapping(value = "/interfaceBDW/queryWoOrderLogInfo",produces = "application/json;charset=UTF-8")
    @Override
    public Map<String, Object> queryWoOrderLogInfo(@RequestBody String request) {

        Map<String, Object> returnmap = new HashMap();
        Map<String, Object> logInfo = new HashMap<String, Object>();
        logInfo.put("interfname", "queryWoOrderLogInfo 工单待办--流程跟踪--工单处理日志信息查询接口");
        logInfo.put("url", "/interfaceBDW/queryWoOrderLogInfo");
        logInfo.put("request", request);
        logInfo.put("remark", "接收app 工单待办--流程跟踪--工单处理日志信息查询接口 json报文");
        try {
            Map params = JSONObject.parseObject(request, Map.class);
            String staffId = MapUtils.getString(params, "staffId");
            String orderId = MapUtils.getString(params, "orderId");
            logInfo.put("tradeId",orderId);
            if (StringUtils.isEmpty(staffId) && StringUtils.isEmpty(orderId)) {
                returnmap.put("code", false);
                returnmap.put("msg", "请检查必填参数不能为空");
                logInfo.put("respone",returnmap);
                insertInterfaceLog(logInfo);
                return returnmap;
            }
            List<Map<String,Object>> woOrderLog = orderDetailsServiceIntf.qryParentSubStatusByOrderId(params);

            returnmap.put("code", true);
            returnmap.put("msg", "成功");
            returnmap.put("data", woOrderLog);
            logInfo.put("respone",JSONObject.toJSONString(returnmap));
        } catch (Exception e) {
            returnmap.put("code", false);
            returnmap.put("msg", "查询失败");
            logInfo.put("respone", e.getMessage());
        } finally {
            insertInterfaceLog(logInfo);
        }
        return returnmap;
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
