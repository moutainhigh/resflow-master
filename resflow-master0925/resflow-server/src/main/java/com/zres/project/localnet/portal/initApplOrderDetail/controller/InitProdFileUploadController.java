package com.zres.project.localnet.portal.initApplOrderDetail.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.zres.project.localnet.portal.localStanbdyInfo.service.OrderStandbyService;
import com.ztesoft.zsmart.pot.annotation.PublicServ;


/*
 * 
 * @author ren.jiahang 
 * @date 2019/1/21 9:30 
 * @param null  
 * @return   
 */
@Controller
@RequestMapping("/localScheduleLT/initProdFileUploadController")
public class InitProdFileUploadController extends HttpServlet {
    Logger logger = LoggerFactory.getLogger(InitProdFileUploadController.class);
    @Autowired
    private OrderStandbyService orderStandbyService;

    @RequestMapping("/uploadFiles.spr")
    @PublicServ
    public void uploadFiles(HttpServletRequest request, HttpServletResponse response) {
        String resultMsg = null;
        File uploadFile = null;
        File newUploadfile = null;
        boolean uploadResult = false;

        List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        try {
            String ctxPath = request.getSession().getServletContext().getRealPath("/") + File.separator + "/createbuss"; //临时存放路径
            Map remoteSavePath = new HashMap();
            remoteSavePath.put("filePath", "createbuss");
            for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
                int fileId = orderStandbyService.getFileId();
                MultipartFile file = entity.getValue();
               // Long fileSize = file.getSize() / 1024;

                float fileNum = (float) file.getSize() / 1024;
                DecimalFormat df = new DecimalFormat("0.00"); //附件大小保留两位小数
                String fileSize = df.format(fileNum) + "KB";

                String fileNameString = file.getOriginalFilename();
                String fileType = fileNameString.substring(fileNameString.lastIndexOf('.') + 1).toLowerCase();
                uploadFile = new File(ctxPath + File.separator + fileNameString);
                newUploadfile = new File(fileId + "." + fileType);
                uploadFile.renameTo(newUploadfile);
                FileCopyUtils.copy(file.getBytes(), newUploadfile);
                Map<String, Object> returnMap = new HashMap<String, Object>();
                returnMap.put("fileId", fileId);
                returnMap.put("fileName", fileNameString);
                returnMap.put("fileSize", fileSize);
                returnMap.put("fileType", fileType);
                returnList.add(returnMap);
                uploadResult = orderStandbyService.uploadFileToFtp(remoteSavePath, newUploadfile);
            }

        }
        catch (Exception e) {
            uploadResult = false;
            logger.error("*******上传附件失败**********" + e.getMessage());
        }
        String returnJson = JSONArray.toJSONString(returnList);
        if (uploadResult) {
            resultMsg = returnJson;
        }
        else {
            JSONArray resultError = new JSONArray();
            resultError.add("error");
            resultMsg = resultError.toString();
        }
        flushResponse(response, resultMsg);
    }

    /**
     * huang.xinfei 事务附件上传
     * 
     * @param request
     * @param response
     */
    @RequestMapping("/affairUploadFiles.spr")
    @PublicServ
    public void affairUploadFiles(HttpServletRequest request, HttpServletResponse response) {
        String resultMsg = null;
        File uploadFile = null;
        File newUploadfile = null;
        boolean uploadResult = false;

        List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        List<MultipartFile> multipartFiles = multipartRequest.getFiles("selectFiles");
        try {
            String ctxPath = request.getSession().getServletContext().getRealPath("/") + File.separator + "/createbuss"; // 临时存放路径
            Map remoteSavePath = new HashMap();
            remoteSavePath.put("filePath", "createbuss");
            for (MultipartFile file : multipartFiles) {
                int fileId = orderStandbyService.getFileId();

                float fileNum = (float) file.getSize() / 1024;
                DecimalFormat df = new DecimalFormat("0.00"); // 附件大小保留两位小数
                String fileSize = df.format(fileNum) + "KB";

                String fileNameString = file.getOriginalFilename();
                String fileType = fileNameString.substring(fileNameString.lastIndexOf('.') + 1).toLowerCase();
                uploadFile = new File(ctxPath + File.separator + fileNameString);
                newUploadfile = new File(fileId + "." + fileType);
                uploadFile.renameTo(newUploadfile);
                FileCopyUtils.copy(file.getBytes(), newUploadfile);
                Map<String, Object> returnMap = new HashMap<String, Object>();
                returnMap.put("fileId", fileId);
                returnMap.put("fileName", fileNameString);
                returnMap.put("fileSize", fileSize);
                returnMap.put("fileType", fileType);
                returnList.add(returnMap);
                uploadResult = orderStandbyService.uploadFileToFtp(remoteSavePath, newUploadfile);
            }

        } catch (Exception e) {
            uploadResult = false;
            logger.error("*******上传附件失败**********" + e.getMessage());
        }
        String returnJson = JSONArray.toJSONString(returnList);
        if (uploadResult) {
            resultMsg = returnJson;
        } else {
            JSONArray resultError = new JSONArray();
            resultError.add("error");
            resultMsg = resultError.toString();
        }
        flushResponse(response, resultMsg);
    }

    protected void flushResponse(HttpServletResponse response, String responseContent) {
        PrintWriter writer = null;

        try {
            response.setCharacterEncoding("GBK");
            response.setHeader("Cache-Control", "no-cache");
            response.setContentType("text/html;charset=UTF-8");
            writer = response.getWriter();
            if (StringUtils.isEmpty(responseContent)) {
                writer.write("");
            }
            else {
                writer.write(responseContent);
            }
        }
        catch (IOException var8) {
            throw new RuntimeException(var8);
        }
        finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }

        }

    }
}
