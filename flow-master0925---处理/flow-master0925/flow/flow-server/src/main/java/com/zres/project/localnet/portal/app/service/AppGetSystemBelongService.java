package com.zres.project.localnet.portal.app.service;


import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zres.project.localnet.portal.app.AppGetSystemBelong;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderQrySecondaryDao;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;

import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import com.alibaba.fastjson.JSONObject;

/**
 * @ClassName AppGetSystemBelongService
 * @Description TODO 电路详情下 查找对应的所属系统（本地调度系统、二干调度系统）以及数据来源（一干来源、集客来源、本地起单等）
 * @Author  ren.leilei
 * @Date 2020/11/24 21:06
 */
@RestController
public class AppGetSystemBelongService implements AppGetSystemBelong {


    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private OrderQrySecondaryDao orderQrySecondaryDao;
    /**
     * 电路详情下 查找对应的所属系统（本地调度系统、二干调度系统）以及数据来源（一干来源、集客来源、本地起单等）
     * @param request
     * @return
     */
    @IgnoreSession
    @RequestMapping(value="/interfaceBDW/getSystemBelong", produces = "application/json;charset=UTF-8")
    @Override
    public Map<String,Object> AppGetSystemBelong(@RequestBody String request) {
        Map<String,Object> returnmap = new HashMap();
        Map<String,Object> logInfo = new HashMap<String, Object>();
        logInfo.put("interfname","getSystemBelong电路详情下 查找对应的所属系统");
        logInfo.put("url","/interfaceBDW/getSystemBelong");
        logInfo.put("request",request);
        logInfo.put("remark","接收app 电路详情下 查找对应的所属系统json报文");
        try {
            Map params = JSONObject.parseObject(request, Map.class);
            logInfo.put("tradeId",MapUtils.getString(params,"cstOrdId"));
            Map<String, Object> stringObjectMap = orderQrySecondaryDao.qrySrvOrderBelongSys(params);
            if (stringObjectMap==null){
                returnmap.put("code", false);
                returnmap.put("msg", "请检查客户订单编号是否正确");
                logInfo.put("respone",returnmap);
                insertInterfaceLog(logInfo);
                return returnmap;
            }
            returnmap.put("code", true);
            returnmap.put("msg", "成功");
            returnmap.put("data", stringObjectMap);
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
