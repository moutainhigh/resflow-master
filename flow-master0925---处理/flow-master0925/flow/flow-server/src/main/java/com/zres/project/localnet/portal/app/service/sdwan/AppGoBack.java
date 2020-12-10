package com.zres.project.localnet.portal.app.service.sdwan;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.app.sdwan.AppGoBackIntf;
import com.zres.project.localnet.portal.sdwan.service.SdwanDealService;
import com.zres.project.localnet.portal.sdwan.service.SdwanDealServiceIntf;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName AppGoBack
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/7/8 9:04
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@RestController
public class AppGoBack implements AppGoBackIntf {
    Logger logger = LoggerFactory.getLogger(AppGoBack.class);
    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private SdwanDealServiceIntf sdwanDealServiceIntf;
    @IgnoreSession
    @RequestMapping(value="/interfaceBDW/goBack", produces = "application/json;charset=UTF-8")

    @Override
    public Map<String, Object> goBack(@RequestBody String request) {
        Map<String,Object> returnmap = new HashMap();
        Map<String,Object> logInfo = new HashMap<String, Object>();
        logInfo.put("interfname","sdwan 回退工单功能");
        logInfo.put("url","/interfaceBDW/goBack");
        logInfo.put("request",request);
        logInfo.put("remark","接收app sdwan 回退工单功能 json报文");
        try {
            Map params = JSONObject.parseObject(request, Map.class);
            if(params.containsKey("circuitData")){
                String circuitDataStr = MapUtils.getString(params, "circuitData");
                List<Object> circuitDatalist = JSON.parseArray(circuitDataStr);
                for (Object object : circuitDatalist) {
                    Map<String, Object> circuitDataMap = (Map<String, Object>) object;// 取出list里面的值转为map
                    String orderId = MapUtils.getString(circuitDataMap, "ORDER_ID");
                    logInfo.put("tradeId",orderId);
                }
            }else{
                returnmap.put("code", false);
                returnmap.put("msg", "请检查[circuitData]不能为空");
                logInfo.put("respone",returnmap);
                insertInterfaceLog(logInfo);
                return returnmap;
            }
            params.put("btnFlag","rollBackOrder");
            Map<String,Object> retMap = sdwanDealServiceIntf.submitOrderSdwan(params);
            returnmap.put("code", MapUtils.getString(retMap,"success"));
            returnmap.put("msg", MapUtils.getString(retMap,"message"));
            logInfo.put("respone",JSONObject.toJSONString(returnmap));
        } catch (Exception e) {
            returnmap.put("code", false);
            returnmap.put("msg", "查询失败");
            logInfo.put("respone",e.getMessage());
            logger.info(e.getMessage());
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
