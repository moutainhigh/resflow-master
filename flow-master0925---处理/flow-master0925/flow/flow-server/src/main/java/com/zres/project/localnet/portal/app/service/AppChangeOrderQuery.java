package com.zres.project.localnet.portal.app.service;

import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.app.AppChangeOrderQueryInf;
import com.zres.project.localnet.portal.orderAbnormal.service.OrderAbnormalServiceIntf;
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
 * @ClassName AppqueryChangeOrderQuery
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/6/16 18:27
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@RestController
public class AppChangeOrderQuery implements AppChangeOrderQueryInf {
    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private OrderAbnormalServiceIntf orderAbnormalServiceIntf;

    @IgnoreSession
    @RequestMapping(value = "/interfaceBDW/queryChangeOrderInfo",produces = "application/json;charset=UTF-8")
    @Override
    public Map<String, Object> queryChangeOrderInfo(@RequestBody String request) {
        Map<String,Object> returnmap = new HashMap();
        Map<String,Object> logInfo = new HashMap<String, Object>();
        logInfo.put("interfname","queryChangeOrderInfo 工单待办--异常单信息查询接口");
        logInfo.put("url","/interfaceBDW/queryChangeOrderInfo");
        logInfo.put("request",request);
        logInfo.put("remark","接收app 工单待办--异常单信息查询接口 json报文");
        try {
            Map params = JSONObject.parseObject(request, Map.class);
            String staffId = MapUtils.getString(params, "staffId");
            String cstOrdId = MapUtils.getString(params, "cstOrdId");
            String srvOrdId = MapUtils.getString(params, "srvOrdId");
            logInfo.put("tradeId", srvOrdId);
            if (StringUtils.isEmpty(staffId) && StringUtils.isEmpty(srvOrdId) && StringUtils.isEmpty(cstOrdId)) {
                returnmap.put("code", false);
                returnmap.put("msg", "请检查必填参数不能为空");
                logInfo.put("respone",returnmap);
                insertInterfaceLog(logInfo);
                return returnmap;
            }
            List changeOrderInfo = orderAbnormalServiceIntf.queryChangeOrderInfo(params);
            returnmap.put("code", true);
            returnmap.put("msg", "成功");
            returnmap.put("data", changeOrderInfo);
            logInfo.put("respone",JSONObject.toJSONString(returnmap));
        } catch (Exception e) {
            returnmap.put("code", false);
            returnmap.put("msg", "查询失败");
            logInfo.put("respone",e.getMessage());
        }finally {
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
