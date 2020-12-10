package com.zres.project.localnet.portal.app.service;

import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.app.AppTaskInfoQueryIntf;
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
 * @ClassName AppTaskInfoQuery
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/6/17 11:44
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@RestController
public class AppTaskInfoQuery implements AppTaskInfoQueryIntf {
    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private OrderDetailsServiceIntf orderDetailsServiceIntf;


    @IgnoreSession
    @RequestMapping(value = "/interfaceBDW/queryTaskInfo",produces = "application/json;charset=UTF-8")
    @Override
    public Map<String, Object> queryTaskInfo(@RequestBody String request) {

        Map<String, Object> returnmap = new HashMap();
        Map<String, Object> logInfo = new HashMap<String, Object>();
        logInfo.put("interfname", "queryTaskInfo 工单待办--任务列表信息查询接口");
        logInfo.put("url", "/interfaceBDW/queryTaskInfo");
        logInfo.put("request", request);
        logInfo.put("remark", "接收app 工单待办--人物列表信息查询接口 json报文");
        try {
            List<Map<String, Object>> taskInfo = new ArrayList<>();
            Map params = JSONObject.parseObject(request, Map.class);
            String staffId = MapUtils.getString(params, "staffId");
            String flag = MapUtils.getString(params, "flag");
            String orderId = MapUtils.getString(params, "orderId");
            String sysResource = MapUtils.getString(params, "sysResource");
            logInfo.put("tradeId",orderId);
            if (StringUtils.isEmpty(staffId) && StringUtils.isEmpty(orderId) && StringUtils.isEmpty(flag)) {
                returnmap.put("code", false);
                returnmap.put("msg", "请检查必填参数不能为空");
                logInfo.put("respone",returnmap);
                insertInterfaceLog(logInfo);
                return returnmap;
            }

             if("twoDry".equals(flag)){ //二干任务列表
                 taskInfo = orderDetailsServiceIntf.querySecTaskInfo(orderId);
            }else if("local".equals(flag)){  //本地任务列表
                 if("second-schedule-lt".equals(sysResource)){ // 二干下发本地的
                     orderDetailsServiceIntf.querySecToLocalTaskInfo(orderId);
                 }else{
                     taskInfo = orderDetailsServiceIntf.queryLocalTaskInfo(orderId);
                 }
            }else {
                throw new Exception("请检查标识是否正确");
            }
            returnmap.put("code", true);
            returnmap.put("msg", "成功");
            returnmap.put("data", taskInfo);
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
