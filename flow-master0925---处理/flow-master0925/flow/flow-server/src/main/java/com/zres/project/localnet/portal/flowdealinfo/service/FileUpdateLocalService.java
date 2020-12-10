package com.zres.project.localnet.portal.flowdealinfo.service;

import com.alibaba.druid.util.StringUtils;
import com.zres.project.localnet.portal.localStandbyInfo.service.OrderStandbyServiceIntf;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@Service
public class FileUpdateLocalService implements FileUpdateLocalServiceInf {

    @Autowired
    private OrderStandbyServiceIntf orderStandbyServiceIntf;

    @Override
    public void uploadLocalFile(PrintWriter writer,
                                String operStaffId,
                                HttpServletRequest request,
                                HttpServletResponse response) throws Exception{

        Map<String, Object> uploadMap = new HashMap();
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        //Map<String, MultipartFile> fileMap = multipartRequest.getFileMap(); //获取单个文件
        MultiValueMap<String, MultipartFile> multiFileMap = multipartRequest.getMultiFileMap();
        String ctxPath = request.getSession().getServletContext().getRealPath("/") + File.separator + "upload"; //临时存放路径
        uploadMap.put("woId", request.getParameter("woId"));
        uploadMap.put("srvOrdId", request.getParameter("srvOrdId"));
        uploadMap.put("orderId", request.getParameter("orderId"));
        uploadMap.put("staffId", operStaffId);
        uploadMap.put("ctxPath", ctxPath);
        uploadMap.put("origin", "HJ");
        Map<String, Object> returnMap = orderStandbyServiceIntf.uploadFiles(uploadMap, multiFileMap);
        JSONObject jsonObject = JSONObject.fromObject(returnMap);
        response.setCharacterEncoding("GBK");
        response.setHeader("Cache-Control", "no-cache");
        response.setContentType("text/html;charset=UTF-8");
        writer = response.getWriter();
        if (StringUtils.isEmpty(jsonObject.toString())) {
            writer.write("");
        }
        else {
            writer.write(jsonObject.toString());
        }

    }



}
