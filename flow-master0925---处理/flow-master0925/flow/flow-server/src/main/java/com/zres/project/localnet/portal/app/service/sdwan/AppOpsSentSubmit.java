package com.zres.project.localnet.portal.app.service.sdwan;


import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.app.sdwan.AppOpsSentSubmitIntf;
import com.zres.project.localnet.portal.sdwan.service.SdwanDealServiceIntf;
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
 * @Description TODO sdwan运维派单 提交页面电路信息查询
 * @Author wang.g2
 * @Date 2020/6/3 9:36
 */
@RestController
public class AppOpsSentSubmit implements AppOpsSentSubmitIntf {

    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private SdwanDealServiceIntf sdwanDealServiceIntf;

    /**
     * sdwan运维派单 提交页面电路信息查询
     * @param request
     * @return
     */
    @IgnoreSession
    @RequestMapping(value="/interfaceBDW/opsSentSubmit.spr", produces = "application/json;charset=UTF-8")
    @Override
    public Map<String,Object> opsSentSubmit(@RequestBody String request) {
        Map<String,Object> returnmap = new HashMap();
        Map<String,Object> logInfo = new HashMap<String, Object>();
        logInfo.put("interfname","opsSentSubmit运维派单提交");
        logInfo.put("url","/interfaceBDW/opsSentSubmit.spr");
        logInfo.put("request",request);
        logInfo.put("remark","接收app sdwan运维派单提交 json报文");
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
            params.put("btnFlag","submit");
            Map<String,Object> retMap = sdwanDealServiceIntf.submitOrderSdwan(params);
            returnmap.put("code", MapUtils.getString(retMap,"success"));
            returnmap.put("msg", MapUtils.getString(retMap,"message"));
            logInfo.put("respone",JSONObject.toJSONString(returnmap));
            logInfo.put("tradeId","");
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
