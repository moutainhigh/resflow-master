package com.zres.project.localnet.portal.app.service;

import com.zres.project.localnet.portal.app.AffairDispatchQueryIntf;
import com.zres.project.localnet.portal.app.AppStaffidQueryIntf;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.res.frame.core.util.MapUtil;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hmef.attribute.MAPIAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @ClassName AffairDispatchQuery
 * @Description TODO 根据user_id查询staffid
 * @Author wang.g2
 * @Date 2020/9/9 16:38
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@RestController
public class AppStaffidQuery implements AppStaffidQueryIntf {

    @Autowired
    private WebServiceDao wsd;

    @IgnoreSession
    @RequestMapping(value = "interfaceBDW/queryStaffidOrderInfo", produces = "application/json;charset=UTF-8")
    @Override
    public Map<String, Object> queryStaffidOrderInfo(@RequestBody String request) {
        HashMap<String, Object> returnmap = new HashMap<>();
        HashMap<String, Object> logInfo = new HashMap<>();
        logInfo.put("interfname", "queryStaffidOrderInfo 根据用户id查询账号id");
        logInfo.put("url", "interfaceBDW/queryStaffidOrderInfo");
        logInfo.put("request", request);
        logInfo.put("remark", "接收app 查询账号id接口");
        try {
            Map params = JSONObject.parseObject(request, Map.class);
            String userCode = MapUtils.getString(params, "user_id");
            if (StringUtils.isEmpty(userCode)) {
                returnmap.put("code", false);
                returnmap.put("msg", "请检查用户id 不能为空");
                logInfo.put("respone",returnmap);
                insertInterfaceLog(logInfo);
                return returnmap;
            }
            Map<String, Object> queryStaffidmap = wsd.queryStaffidOrderInfo(userCode);
            if (queryStaffidmap==null){
                returnmap.put("code", false);
                returnmap.put("msg", "请检查用户:"+userCode+"是否正确");
                logInfo.put("respone",returnmap);
                insertInterfaceLog(logInfo);
                return returnmap;
            }

            returnmap.put("code", true);
            returnmap.put("msg", "成功");
            returnmap.put("data", queryStaffidmap);
            logInfo.put("respone",JSONObject.toJSONString(returnmap));
        } catch (Exception e) {
            returnmap.put("code", false);
            returnmap.put("msg", "查询失败");
            logInfo.put("respone", e.getMessage());
        }finally {
            insertInterfaceLog(logInfo);
        }
        return returnmap;
    }

    private void insertInterfaceLog(HashMap<String, Object> logInfo) {
        Map<String, Object> interflog = new HashMap<String, Object>();
        interflog.put("INTERFNAME", MapUtils.getString(logInfo, "interfname"));
        interflog.put("URL", MapUtils.getString(logInfo, "url"));
        interflog.put("CONTENT", MapUtils.getString(logInfo, "request"));
        interflog.put("RETURNCONTENT", MapUtils.getString(logInfo, "respone"));
        interflog.put("ORDERNO", MapUtils.getString(logInfo, "tradeId"));
        interflog.put("REMARK", MapUtils.getString(logInfo, "remark"));
        wsd.insertInterfLog(interflog);
    }

}


