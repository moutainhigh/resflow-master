package com.zres.project.localnet.portal.app.service;


import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.app.AppCcWoOrderIntf;
import com.zres.project.localnet.portal.app.AppTransferWoOrderIntf;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealService;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
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
 * @ClassName AppCcWoOrder
 * @Description TODO 环节处理/通用按钮-抄送按钮
 * @Author wang.g2
 * @Date 2020/6/3 9:36
 */
@RestController
public class AppCcWoOrder implements AppCcWoOrderIntf {

    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private OrderDealService orderDealService;

    /**
     * 环节处理/通用按钮-抄送按钮
     * @param request
     * @return
     */
    @IgnoreSession
    @RequestMapping(value="/interfaceBDW/ccWoOrder.spr", produces = "application/json;charset=UTF-8")
    @Override
    public Map<String,Object> ccWoOrder(@RequestBody String request) {
        Map<String,Object> returnmap = new HashMap();
        Map<String,Object> logInfo = new HashMap<String, Object>();
        logInfo.put("interfname","ccWoOrder/通用按钮-抄送按钮");
        logInfo.put("url","/interfaceBDW/ccWoOrder.spr");
        logInfo.put("request",request);
        logInfo.put("remark","接收app 环节处理/通用按钮-抄送按钮 json报文");
        try {
            Map params = JSONObject.parseObject(request, Map.class);
            String paramKeys = "staffId,woId,orderId,tacheId,objId,objType,srvOrdId";
            for(Object temp : paramKeys.split(",")){
                String tempValue = MapUtils.getString(params,temp);
                if(StringUtils.isEmpty(tempValue)){
                    returnmap.put("code", false);
                    returnmap.put("msg", "请检查"+temp+"不能为空");
                    return returnmap;
                }
            }
            Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(MapUtils.getInteger(params,"staffId"));
            params.put("operStaffInfoMap",operStaffInfoMap);
            if(!params.containsKey("opinion")){
                params.put("opinion",MapUtils.getString(params,"remark",""));// 抄送意见
            }
            Map<String,Object> btnMap = orderDealService.ccWoOrder(params);
            if(!MapUtils.getBoolean(btnMap,"success")){
                returnmap.put("code", false);
                returnmap.put("msg", "查询失败:" + MapUtils.getString(btnMap,"message"));
                logInfo.put("respone",returnmap);
                insertInterfaceLog(logInfo);
                return returnmap;
            }
            returnmap.put("code", true);
            returnmap.put("msg", "");
            logInfo.put("respone",JSONObject.toJSONString(returnmap));
            logInfo.put("tradeId",MapUtils.getString(params, "orderId"));
        } catch (Exception e) {
            returnmap.put("code", false);
            returnmap.put("msg", "查询失败");
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
