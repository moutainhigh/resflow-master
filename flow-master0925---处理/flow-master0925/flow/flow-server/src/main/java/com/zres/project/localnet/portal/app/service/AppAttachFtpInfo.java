package com.zres.project.localnet.portal.app.service;

import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.app.AppAttachFtpIntf;
import com.zres.project.localnet.portal.app.controller.AppFilesController;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.until.service.UntilService;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName AppAttachFtpInfo
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/7/9 15:06
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@RestController
public class AppAttachFtpInfo  implements AppAttachFtpIntf {

    Logger logger = LoggerFactory.getLogger(AppAttachFtpInfo.class);

    @Value("${ftp.address}")
    private String ftpAddress;

    @Value("${ftp.port}")
    private String ftpPort;

    @Value("${ftp.username}")
    private String username;

    @Value("${ftp.password}")
    private String password;

    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private OrderDealDao orderDealDao;
    @IgnoreSession
    @RequestMapping(value="/interfaceBDW/queryAttacheFtpInfo", produces = "application/json;charset=UTF-8")
    @Override
    public Map<String, Object> queryAttacheFtpInfo(@RequestBody String request) {

        Map<String,Object> returnmap = new HashMap();
        Map<String,Object> ftpInfo = new HashMap();
        Map<String,Object> logInfo = new HashMap<String, Object>();
        logInfo.put("interfname","附件ftp 信息查询");
        logInfo.put("url","/interfaceBDW/queryAttacheFtpInfo");
        logInfo.put("request",request);
        logInfo.put("remark","接收app 附件ftp 信息查询 json报文");
        try {
            Map params = JSONObject.parseObject(request, Map.class);
            String staffId = MapUtils.getString(params, "staffId");

            if (StringUtils.isEmpty(staffId)) {
                returnmap.put("code", false);
                returnmap.put("msg", "请检查请检查用户id不能为空");
                return returnmap;
            }
            Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(staffId));
           if(StringUtils.isEmpty(MapUtils.getString(operStaffInfoMap,"USER_NAME"))){
               throw new Exception("请检查用户");
           }
            ftpInfo.put("ftpAddress",ftpAddress);
            ftpInfo.put("ftpPort",ftpPort);
            ftpInfo.put("username",username);
            ftpInfo.put("password",password);
            ftpInfo.put("path","appAtach");
            returnmap.put("code", true);
            returnmap.put("msg", "");
            returnmap.put("ftpInfo", ftpInfo);

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
