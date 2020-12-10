package com.zres.project.localnet.portal.app.service;

import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.app.AppContactsQueryIntf;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.ws.Action;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName AppContactsQuery
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/6/22 16:26
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@RestController
public class AppContactsQuery implements AppContactsQueryIntf {
    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private OrderDealServiceIntf orderDealServiceIntf;

    @IgnoreSession
    @RequestMapping(value = "/interfaceBDW/queryContactsInfo", produces = "application/json;charset=UTF-8")
    @Override
    public Map<String, Object> queryContactsInfo(@RequestBody String request) {
        Map<String, Object> returnmap = new HashMap();
        Map<String, Object> logInfo = new HashMap<String, Object>();
        logInfo.put("interfname", "queryContactsInfo 常用联系人查询接口");
        logInfo.put("url", "/interfaceBDW/queryContactsInfo");
        logInfo.put("request", request);
        logInfo.put("remark", "接收app 常用联系人查询接口 json报文");
        try {
            List<Map<String, Object>> taskInfo = new ArrayList<>();
            Map params = JSONObject.parseObject(request, Map.class);
            String staffId = MapUtils.getString(params, "staffId");
            if (StringUtils.isEmpty(staffId) ) {
                returnmap.put("code", false);
                returnmap.put("msg", "请检查必填参数不能为空");
                logInfo.put("respone", returnmap);
                insertInterfaceLog(logInfo);
                return returnmap;
            }
            params.put("currentUserId", staffId);
            Map contacts = orderDealServiceIntf.qrySearchContacts(params);
            returnmap.put("code", true);
            returnmap.put("msg", "成功");
            returnmap.put("data", MapUtils.getObject(contacts,"data"));
            logInfo.put("respone",JSONObject.toJSONString(returnmap));
            logInfo.put("tradeId", staffId);
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
