package com.zres.project.localnet.portal.flowdealinfo.controller;

import com.alibaba.fastjson.JSON;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealService;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf;
import com.zres.project.localnet.portal.flowdealinfo.service.SecDealLocalServiceInf;
import com.zres.project.localnet.portal.localStandbyInfo.service.OrderStandbyServiceIntf;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import com.ztesoft.zsmart.pot.annotation.PublicServ;
import net.sf.json.JSONObject;

import org.apache.axis.utils.StringUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/localScheduleLT/FlieUpdateController")
public class FlieUpdateController extends HttpServlet {

    Logger logger = LoggerFactory.getLogger(FlieUpdateController.class);
    @Autowired
    private OrderStandbyServiceIntf orderStandbyServiceIntf;
    @Autowired
    private OrderDealServiceIntf orderDealServiceIntf;
    @Autowired
    private SecDealLocalServiceInf secDealLocalServiceInf;

    @RequestMapping("/uploadFiles.spr")
    @PublicServ
    public void uploadFiles(HttpServletRequest request, HttpServletResponse response) {
        logger.info("先上传附件。。。。。。。。。。。。。。。。。。。。");
        PrintWriter writer = null;
        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        Map<String, Object> resMap = new HashMap<String, Object>();
        try {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            //Map<String, MultipartFile> fileMap = multipartRequest.getFileMap(); //获取单个文件
            MultiValueMap<String, MultipartFile> multiFileMap = multipartRequest.getMultiFileMap();
            String ctxPath = request.getSession().getServletContext().getRealPath("/") + File.separator + "upload"; //临时存放路径
            String paramStr = request.getParameter("params");
            JSONObject jasonObject = JSONObject.fromObject(paramStr);
            Map params =  (Map)jasonObject;
            Map paramsDeal =  params; //复制一份原来的参数信息
            //String action = MapUtils.getString(params, "action");
            String applyWoId = MapUtils.getString(params, "woId");
            String origin = MapUtils.getString(params, "origin","HJ");
            String delFiles = MapUtils.getString(params, "delFiles");
            String circuitDataStr = MapUtils.getString(params, "circuitData");
            List<Object> circuitDatalist =JSON.parseArray(circuitDataStr);
            List fileIds = new ArrayList();
            for (Object object : circuitDatalist){
                Map<String, Object> uploadMap = new HashMap();
                Map <String,Object> circuitDataMap = (Map<String, Object>) object;//取出list里面的值转为map
                String woId = MapUtils.getString(circuitDataMap, "WO_ID");
                String orderId = MapUtils.getString(circuitDataMap, "ORDER_ID");
                String tacheId = MapUtils.getString(circuitDataMap, "TACHE_ID");
                //String psId = MapUtils.getString(circuitDataMap, "PS_ID");
                String srvOrdId = MapUtils.getString(circuitDataMap, "SRV_ORD_ID");
                //上传附件
                uploadMap.put("woId", woId);
                uploadMap.put("srvOrdId", srvOrdId);
                uploadMap.put("orderId", orderId);
                uploadMap.put("staffId", operStaffId);
                uploadMap.put("ctxPath", ctxPath);
                uploadMap.put("origin", "HJ");
                uploadMap.put("tacheId",tacheId);
                uploadMap.put("delFiles", delFiles);
                Map<String, Object> returnMap = orderStandbyServiceIntf.uploadFiles(uploadMap, multiFileMap);
                Boolean flag = MapUtils.getBoolean(returnMap,"flag");
                fileIds.addAll((List) returnMap.get("fileIds"));
                if (!flag) {
                    resMap.put("success", false);
                    resMap.put("message", MapUtils.getString(returnMap,"message"));
                    break;
                }else {
                    resMap.put("success", true);
                    resMap.put("message", "附件上传成功！！！");
                }
            }
            Boolean success = MapUtils.getBoolean(resMap,"success");
            if(success){
                paramsDeal.put("fileIds", fileIds); //起草调单新增时未生成调单id,等生成后再更新附件表
                orderDealServiceIntf.submitOrder(paramsDeal);
                resMap.put("success", true);
                resMap.put("message", "派单成功！");
            }
        }
        catch (Exception e) {
            logger.info(e.getMessage());
            e.printStackTrace();
            resMap.put("success", false);
            resMap.put("message", "派单失败！");
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
                e.printStackTrace();
                logger.info(e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }
}
