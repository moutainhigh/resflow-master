package com.zres.project.localnet.portal.cloudNetworkFlow.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.zres.project.localnet.portal.localStandbyInfo.service.OrderStandbyServiceIntf;
import net.sf.json.JSONObject;

import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import com.ztesoft.zsmart.pot.annotation.PublicServ;

import com.alibaba.fastjson.JSON;

/**
 * 工单提交时附件处理
 */
@Controller
@RequestMapping("/localScheduleLT/FlieWoOrderDealController")
public class FlieWoOrderDealController extends HttpServlet {

    Logger logger = LoggerFactory.getLogger(FlieWoOrderDealController.class);

    @Autowired
    private OrderStandbyServiceIntf orderStandbyServiceIntf;

    /**
     * 环节上上传附件
     *
     * @param request
     * @param response
     */
    @RequestMapping("/uploadFiles.spr")
    @PublicServ
    public void uploadFiles(HttpServletRequest request, HttpServletResponse response) {
        logger.info("------------页面提交进入---上传附件-------------------");
        PrintWriter writer = null;
        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        Map<String, Object> resMap = new HashMap<String, Object>();
        try {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            MultiValueMap<String, MultipartFile> multiFileMap = multipartRequest.getMultiFileMap();
            String ctxPath = request.getSession().getServletContext().getRealPath("/") + File.separator + "upload"; //临时存放路径
            Map params = JSON.parseObject(request.getParameter("params"), Map.class);
            String action = MapUtils.getString(params, "action");
            String delFiles = MapUtils.getString(params, "delFiles");
            String circuitDataStr = MapUtils.getString(params, "circuitData");
            String dispatchOrderId = MapUtils.getString(params, "dispatchOrderId");
            List<Object> circuitDatalist = JSON.parseArray(circuitDataStr);
            List fileIds = new ArrayList();
            for (Object object : circuitDatalist) {
                Map<String, Object> uploadMap = new HashMap();
                Map<String, Object> circuitDataMap = (Map<String, Object>) object; //取出list里面的值转为map
                String woId = MapUtils.getString(circuitDataMap, "WO_ID");
                String orderId = MapUtils.getString(circuitDataMap, "ORDER_ID");
                String tacheId = MapUtils.getString(circuitDataMap, "TACHE_ID");
                String srvOrdId = MapUtils.getString(circuitDataMap, "SRV_ORD_ID");
                String origin = MapUtils.getString(circuitDataMap, "ORIGIN", "HJ");
                String mappingId = MapUtils.getString(circuitDataMap, "mappingId");
                //上传附件
                uploadMap.put("woId", woId);
                uploadMap.put("srvOrdId", srvOrdId);
                uploadMap.put("orderId", orderId);
                uploadMap.put("staffId", operStaffId);
                uploadMap.put("ctxPath", ctxPath);
                uploadMap.put("origin", origin);
                uploadMap.put("tacheId", tacheId);
                uploadMap.put("delFiles", delFiles);
                uploadMap.put("mappingId", mappingId);
                Map<String, Object> returnMap = orderStandbyServiceIntf.uploadFiles(uploadMap, multiFileMap);
                Boolean flag = MapUtils.getBoolean(returnMap, "flag");
                fileIds.addAll((List) returnMap.get("fileIds"));
                if (!flag) {
                    resMap.put("success", false);
                    resMap.put("message", MapUtils.getString(returnMap, "message"));
                    break;
                }
                else {
                    logger.info("------------附件上传成功！---------------------");
                    resMap.put("success", true);
                    resMap.put("message", "附件上传成功！！！");
                }
            }
        }
        catch (Exception e) {
            logger.info(e.getMessage());
            resMap.put("success", false);
            resMap.put("message", "附件上传失败！");
        }
        finally {
            JSONObject jsonObject = JSONObject.fromObject(resMap);
            response.setCharacterEncoding("GBK");
            response.setHeader("Cache-Control", "no-cache");
            response.setContentType("text/html;charset=UTF-8");
            try {
                writer = response.getWriter();
                writer.write(jsonObject.toString());
                writer.flush();
                writer.close();
            }
            catch (IOException e) {
                //e.printStackTrace();
                logger.info(e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

}
