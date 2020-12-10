package com.zres.project.localnet.portal.initApplOrderDetail.controller;


import com.zres.project.localnet.portal.applpage.service.GomPropertiesConfIntf;
import com.zres.project.localnet.portal.util.ResponseHandler;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;


@Controller
@RequestMapping("/localScheduleLT/gomPropertiesConfController")
public class GomPropertiesConfController {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GomPropertiesConfIntf gomPropertiesConfService;

    @RequestMapping("/insertPropertiesConf.spr")
    @IgnoreSession
    public void insertPropertiesConf(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>();
        String sourceDataArray = request.getParameter("sourceDataArray");
        ResponseHandler responseHandler = new ResponseHandler(request,response);
        JSONObject jsonObject = null;
        try {
            gomPropertiesConfService.insertGomPropertiesConfView(sourceDataArray);
            map.put("message", "电路配置信息同步成功");

        }catch (Exception e){
            map.put("message", "电路配置信息同步失败:"+e.getMessage());
            logger.debug(e.getMessage());
            e.printStackTrace();
        }
        jsonObject = JSONObject.fromObject(map);
        responseHandler.flushResponse(response, jsonObject.toString());

    }


}
