package com.zres.project.localnet.portal.app.controller;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import com.zres.project.localnet.portal.util.SftpUtils;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import net.sf.json.JSONObject;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.zres.project.localnet.portal.localStanbdyInfo.service.OrderStandbyService;
import com.ztesoft.zsmart.pot.annotation.PublicServ;



@Controller
public class AppFilesController extends HttpServlet {
    Logger logger = LoggerFactory.getLogger(AppFilesController.class);

    @Value("${ftp.address}")
    private String ftpAddress;

    @Value("${ftp.port}")
    private String ftpPort;

    @Value("${ftp.username}")
    private String username;

    @Value("${ftp.password}")
    private String password;

    @Autowired
    private OrderStandbyService orderStandbyService;

    @RequestMapping("/interfaceBDW/uploadFiles.spr")
    @IgnoreSession
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
            remoteSavePath.put("filePath", "appAtach");
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
                returnMap.put("filePath", "appAtach");
                returnList.add(returnMap);
                uploadResult = orderStandbyService.uploadFileToFtp(remoteSavePath, newUploadfile);
                newUploadfile.delete();
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


    @RequestMapping(value = "/interfaceBDW/Download.spr")
    @IgnoreSession
    public void downOneDryFile(@RequestBody String param, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String decode = URLDecoder.decode(param, "UTF-8").substring(13);
        int csrf = decode.lastIndexOf("&_csrf");
        String csrfStr = decode.substring(0,csrf);
        Map<String, Object> params = JSONObject.fromObject(csrfStr);
        String fileName = MapUtils.getString(params,"fileName"); //文件的真实名称
        String fileId = MapUtils.getString(params,"fileId"); //文件id
        String filePath = MapUtils.getString(params,"filePath"); //文件的路径

        //判断浏览器类型
        String agent = request.getHeader("USER-AGENT").toLowerCase();
        if (agent != null && (agent.indexOf("msie") > -1 || agent.indexOf("trident") > -1 || agent.indexOf("edge") > -1)) { //IE
            fileName = URLEncoder.encode(fileName, "UTF-8");
            fileName = fileName.replaceAll("\\+", "%20");
        }
        else {
            fileName = new String(fileName.getBytes("UTF-8"), "ISO-8859-1");
        }
        SftpUtils sftpUtils = new SftpUtils(ftpAddress,Integer.parseInt(ftpPort),username,password);
        ChannelSftp sftp = sftpUtils.connect();
        try {
            Vector<?> ftpFiles = sftp.ls(filePath);
            Boolean temp = true;
            for (Object ftpFile : ftpFiles){

                ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) ftpFile;
                if(fileId.equalsIgnoreCase(entry.getFilename())){
                    OutputStream os = null;
                    InputStream is = null;
                    temp = false;
                    sftp.cd(filePath);
                    is=sftp.get(fileId);
                    response.setContentType("multipart/form-data");
                    /* 设置文件头：最后一个参数是设置下载文件名 */
                    response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
                    os = response.getOutputStream();
                    byte[] b = new byte[1024];
                    int len;
                    while ((len = is.read(b)) > 0) {
                        os.write(b, 0, len);
                    }
                    os.flush();
                    os.close();
                    is.close();
                }

            }
            if (temp) {
                logger.info("ftp服务器上没有该文件：" + fileName);
            }
        } catch (SftpException e) {
            throw new IOException("附件下载失败！");
        } finally {

            if (sftp != null) {
                try {
                    sftpUtils.disconnect();
                    sftp = null;
                }
                catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        }

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

    /**
     * 删除单个文件
     *
     * @param sPath 被删除文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public boolean deleteFile(String sPath) {
        Boolean flag = false;
        File file = new File(sPath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }
}

