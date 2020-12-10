package com.zres.project.localnet.portal.app.service;


import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.app.AppGetTacheButtonIntf;
import com.zres.project.localnet.portal.app.AppStandbyIntf;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf;
import com.zres.project.localnet.portal.localStanbdyInfo.data.dao.OrderStandbyDao;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @ClassName AppStandby
 * @Description TODO 待办查询
 * @Author wang.g2
 * @Date 2020/6/3 9:36
 */
@RestController
public class AppGetTacheButton implements AppGetTacheButtonIntf {

    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private OrderDealServiceIntf orderDealServiceIntf;

    /**
     * 按钮初始化查询接口  查询当前环节工单的按钮
     * @param request
     * @return
     */
    @IgnoreSession
    @RequestMapping(value="/interfaceBDW/getTacheButton.spr", produces = "application/json;charset=UTF-8")
    @Override
    public Map<String,Object> getTacheButton(@RequestBody String request) {
        Map<String,Object> returnmap = new HashMap();
        Map<String,Object> logInfo = new HashMap<String, Object>();
        logInfo.put("interfname","getTacheButton按钮初始化查询接口");
        logInfo.put("url","/interfaceBDW/getTacheButton.spr");
        logInfo.put("request",request);
        logInfo.put("remark","接收app 按钮初始化查询接口 json报文");
        try {
            Map params = JSONObject.parseObject(request, Map.class);
            String tacheId = MapUtils.getString(params, "tacheId");
            String orderId = MapUtils.getString(params, "orderId");
            String btnInfo = MapUtils.getString(params, "btnInfo");
            logInfo.put("tradeId",orderId);
            for(Object temp : params.keySet()){
                String tempValue = MapUtils.getString(params,temp);
                if(StringUtils.isEmpty(tempValue)){
                    returnmap.put("code", false);
                    returnmap.put("msg", "请检查"+temp+"不能为空");
                    insertInterfaceLog(logInfo);
                    return returnmap;
                }
            }
            List<Map<String, Object>> mapListT = new ArrayList<Map<String, Object>>();
            if("400".equals(btnInfo)){
                Map<String, Object> map = new HashMap<>();
                map.put("BUTTON_NAME","确认");
                mapListT.add(map);
            } else{
                if("100".equals(btnInfo)){
                    params.put("buttonState","dealOrder");
                }
                Map<String,Object> btnMap = orderDealServiceIntf.getTacheButton(params);
                if(!MapUtils.getBoolean(btnMap,"success")){
                    returnmap.put("code", false);
                    returnmap.put("msg", "查询失败:" + MapUtils.getString(btnMap,"resButtons"));
                    logInfo.put("respone",returnmap);
                    return returnmap;
                } else{
                    List<Map<String, Object>> btnList = (List<Map<String, Object>>) MapUtils.getObject(btnMap,"resButtons");
                    for(Map<String, Object> temp : btnList){
                        /**
                         *  电路调度/核查调度 不返回提交按钮，不提供补单按钮
                         *  所有环节不返回资源配置按钮
                         *  核查汇总环节不返回下发工建按钮
                         */
                        Set<String> clickSet = new HashSet<>();
                        clickSet.add("sendEngineering()");
                        clickSet.add("resConfig()");
                        String click = MapUtils.getString(temp,"BUTTON_CLICK");
                        boolean flag = (BasicCode.CHECK_DISPATCH.equals(tacheId) || BasicCode.CHECK_DISPATCH.equals(tacheId) )&&"sendWoOrder()".equals(click);
                        if(!(clickSet.contains(click) || flag)){
                            Map<String, Object> map = new HashMap<>();
                            map.put("BUTTON_NAME",MapUtils.getString(temp,"BUTTON_NAME"));
                            mapListT.add(map);
                        }

                    }
                }
            }

            returnmap.put("code", true);
            returnmap.put("msg", "");
            returnmap.put("data", mapListT);
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
