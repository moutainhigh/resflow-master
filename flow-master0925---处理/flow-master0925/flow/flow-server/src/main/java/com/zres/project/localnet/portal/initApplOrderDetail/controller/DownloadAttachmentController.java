package com.zres.project.localnet.portal.initApplOrderDetail.controller;


import com.zres.project.localnet.portal.util.FtpUtils;
import com.ztesoft.zsmart.pot.annotation.PublicServ;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.util.Map;

/**
 * 下载附件的Controller
 * @author lu.haoyang
 * Created by lu on 2019/1/16 0016.
 */
@Controller
@RequestMapping("/localScheduleLT/ApplOrder")
public class DownloadAttachmentController {
    Logger logger = LoggerFactory.getLogger(DownloadAttachmentController.class);

    @Value("${ftp.address}")
    private String ftpAddress;

    @Value("${ftp.port}")
    private String ftpPort;
    @Value("${ftp.username}")
    private String username;

    @Value("${ftp.password}")
    private String password;
    @RequestMapping(value = "/attachmentDownload.spr")
    @PublicServ
    public  void downloadAttachmentMethod(@RequestBody String downLoadData,HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        //根据前台传递的参数获取对应的需要在的ftp的下载的问题文件的URLpath 等参数
        String str = new String(downLoadData.getBytes("ISO-8859-1"), "UTF-8");
        String decode = URLDecoder.decode(str, "UTF-8").substring(13);
        int csrf = decode.lastIndexOf("&_csrf");
        String csrfStr = decode.substring(0,csrf);
        Map<String, Object> params = JSONObject.fromObject(csrfStr);
        //文件的真实名称
        String fileName = params.get("FILE_NAME").toString();
        //ftp路径
        String remotePath = params.get("FILE_PATH").toString();
        //ftp服务器上文件名称
        String remoteFileName = params.get("FILE_ID").toString();
        //ftp文件类型
        String ftpType = params.get("FILE_TYPE").toString();
        //ftp服务器上文件名称全程(带文件类型)
        String ftpFileName = remoteFileName+"."+ftpType;
        try {
            FtpUtils.downloadFtpToBrowserClient(ftpAddress,Integer.parseInt(ftpPort),username,password,remotePath,ftpFileName,fileName, request,response);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("********下载附件报错*****报错内容为："+e+"*********");
            logger.info(e.getMessage());
        }


    }
}
