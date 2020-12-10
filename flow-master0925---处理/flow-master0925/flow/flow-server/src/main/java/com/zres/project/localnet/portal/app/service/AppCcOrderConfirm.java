package com.zres.project.localnet.portal.app.service;


import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.app.AppCcOrderConfirmIntf;
import com.zres.project.localnet.portal.app.AppSubmitOrderIntf;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf;
import com.zres.project.localnet.portal.localStandbyInfo.service.OrderStandbyServiceIntf;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName AppStandby
 * @Description TODO 提交按钮
 * @Author wang.g2
 * @Date 2020/6/3 9:36
 */
@RestController
public class AppCcOrderConfirm implements AppCcOrderConfirmIntf {

    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private OrderStandbyServiceIntf orderStandbyServiceIntf;

    /**
     * 抄送工单确认功能
     * @param request
     * @return
     */
    @IgnoreSession
    @RequestMapping(value="/interfaceBDW/ccOrderConfirm.spr", produces = "application/json;charset=UTF-8")
    @Override
    public Map<String,Object> ccOrderConfirm(@RequestBody String request) {
        Map<String,Object> returnmap = new HashMap();
        Map<String,Object> logInfo = new HashMap<String, Object>();
        logInfo.put("interfname","抄送工单确认功能");
        logInfo.put("url","/interfaceBDW/ccOrderConfirm.spr");
        logInfo.put("request",request);
        logInfo.put("remark","接收app 抄送工单确认 json报文");
        try {
            Map params = JSONObject.parseObject(request, Map.class);
            for(Object temp : params.keySet()){
                String tempValue = MapUtils.getString(params,temp);
                if(StringUtils.isEmpty(tempValue)){
                    returnmap.put("code", false);
                    returnmap.put("msg", "请检查"+temp+"不能为空");
                    return returnmap;
                }
            }
            orderStandbyServiceIntf.updateCC(params);
            returnmap.put("code", true);
            returnmap.put("msg", "");
            logInfo.put("respone",JSONObject.toJSONString(returnmap));
            logInfo.put("tradeId","");
        } catch (Exception e) {
            returnmap.put("code", false);
            returnmap.put("msg", "抄送确认失败");
            logInfo.put("respone",e.getMessage());
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
        interflog.put("INTERFNAME",MapUtils.getString(logInfo,"interfname"));
        interflog.put("URL",MapUtils.getString(logInfo,"url"));
        interflog.put("CONTENT", MapUtils.getString(logInfo,"request"));
        interflog.put("RETURNCONTENT",MapUtils.getString(logInfo,"respone"));
        interflog.put("ORDERNO",MapUtils.getString(logInfo,"tradeId"));
        interflog.put("REMARK", MapUtils.getString(logInfo,"remark"));
        wsd.insertInterfLog(interflog);
    }

}
