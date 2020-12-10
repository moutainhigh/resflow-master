package com.zres.project.localnet.portal.app.service;


import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.app.AppGetFreeWoOrderIntf;
import com.zres.project.localnet.portal.app.AppGetTacheButtonIntf;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf;
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
 * @ClassName AppGetFreeWoOrder
 * @Description TODO 环节处理/通用按钮-签收/释放签收按钮
 * @Author wang.g2
 * @Date 2020/6/3 9:36
 */
@RestController
public class AppGetFreeWoOrder implements AppGetFreeWoOrderIntf {

    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private OrderDealServiceIntf orderDealServiceIntf;

    /**
     * 环节处理/通用按钮-签收/释放签收按钮
     * @param request
     * @return
     */
    @IgnoreSession
    @RequestMapping(value="/interfaceBDW/getFreeWoOrder.spr", produces = "application/json;charset=UTF-8")
    @Override
    public Map<String,Object> getFreeWoOrder(@RequestBody String request) {
        Map<String,Object> returnmap = new HashMap();
        Map<String,Object> logInfo = new HashMap<String, Object>();
        logInfo.put("interfname","getFreeWoOrder环节处理/通用按钮-签收/释放签收按钮");
        logInfo.put("url","/interfaceBDW/getFreeWoOrder.spr");
        logInfo.put("request",request);
        logInfo.put("remark","接收app 环节处理/通用按钮-签收/释放签收按钮 json报文");
        try {
            Map params = JSONObject.parseObject(request, Map.class);
            String staffId = MapUtils.getString(params, "staffId");
            String woOrderIds = MapUtils.getString(params, "woOrderIds");
            String actionType = MapUtils.getString(params, "actionType");
            if(StringUtils.isEmpty(staffId) || StringUtils.isEmpty(woOrderIds) || StringUtils.isEmpty(actionType)){
                returnmap.put("code", false);
                returnmap.put("msg", "请检查staffId,woOrderIds,actionType不能为空");
                logInfo.put("respone", returnmap);
                insertInterfaceLog(logInfo);
                return returnmap;
            }
            Map<String, Object> param = new HashMap<>();
            // sign:签收；releaseSign:释放签收
            if("sign".equals(actionType)){
                param.put("actionType","get");
            } else if("releaseSign".equals(actionType)){
                param.put("actionType","free");
            }
            param.put("operStaffId",staffId);
            String[] woIdStrs = woOrderIds.split(",");
            List woIdList =java.util.Arrays.asList(woIdStrs);
            param.put("woOrderIds",woIdList);
            Map<String,Object> btnMap = orderDealServiceIntf.getFreeWoOrder(param);
            logInfo.put("tradeId",MapUtils.getString(params, "srvOrdIds"));
            if(!MapUtils.getBoolean(btnMap,"success")){
                returnmap.put("code", false);
                returnmap.put("msg", "查询失败:" + MapUtils.getString(btnMap,"resButtons"));
                logInfo.put("respone", returnmap);
                insertInterfaceLog(logInfo);
                return returnmap;
            }
            returnmap.put("code", true);
            returnmap.put("msg", "");
            logInfo.put("respone",JSONObject.toJSONString(returnmap));

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
