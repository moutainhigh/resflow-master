package com.zres.project.localnet.portal.flowdealinfo.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import com.zres.project.localnet.portal.util.SftpUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.zres.project.localnet.portal.util.FtpUtils;
import com.ztesoft.zsmart.pot.annotation.PublicServ;

import net.sf.json.JSONObject;

@Controller
@RequestMapping("/localScheduleLT/orderDetails")
public class FileDownloadController {

    Logger log = LoggerFactory.getLogger(FileDownloadController.class);

    @Value("${ftp.address}")
    private String ftpAddress;

    @Value("${ftp.port}")
    private String ftpPort;

    @Value("${ftp.username}")
    private String username;

    @Value("${ftp.password}")
    private String password;

    /**
     * 工单详情页下载附件
     * 
     * @param param
     */
    @RequestMapping(value = "/fileDownload.spr")
    @PublicServ
    public void downFile(@RequestBody String param, HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        String decode = URLDecoder.decode(param, "UTF-8").substring(13);
        int csrf = decode.lastIndexOf("&_csrf");
        if (csrf == -1) {
            log.error("下载参数错误：" + decode);
            return;
        }
        String csrfStr = decode.substring(0, csrf);
        Map<String, Object> params = JSONObject.fromObject(csrfStr);
        String fileName = params.get("fileName").toString(); // 文件的真实名称
        // 判断浏览器类型
        String agent = request.getHeader("USER-AGENT").toLowerCase();
        if (agent != null
            && (agent.indexOf("msie") > -1 || agent.indexOf("trident") > -1 || agent.indexOf("edge") > -1)) { // IE
            fileName = URLEncoder.encode(fileName, "UTF-8");
            fileName = fileName.replaceAll("\\+", "%20");
        } else {
            fileName = new String(fileName.getBytes("UTF-8"), "ISO-8859-1");
        }
        String remotePath = params.get("filePath").toString();
        String remoteFileName = params.get("fileId").toString(); // ftp服务器上文件名称
        // 从ftp上下载附件，输出为流
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(ftpAddress, Integer.parseInt(ftpPort));
            if (ftpClient.login(username, password)) {
                ftpClient.enterLocalPassiveMode();

                // 改变工作目录
                FtpUtils.changWorkingDirectory(remotePath, ftpClient, false);
                ftpClient.setControlEncoding("GBK");
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                // 获取FTP登陆目录下的所有文件

                Boolean temp = true;
                FTPFile[] files = ftpClient.listFiles();
                for (FTPFile file : files) {
                    if (remoteFileName.equalsIgnoreCase(file.getName())) {
                        temp = false;
                        OutputStream os = null;
                        InputStream is = null;
                        try {
                            is = ftpClient.retrieveFileStream(file.getName());
                            response.setContentType("multipart/form-data");
                            /* 设置文件头：最后一个参数是设置下载文件名 */
                            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
                            os = response.getOutputStream();
                            byte[] b = new byte[1024];
                            int len;
                            while ((len = is.read(b)) > 0) {
                                os.write(b, 0, len);
                            }
                        } catch (IOException e) {
                            throw new IOException("附件下载失败！");
                        } finally {
                            os.flush();
                            os.close();
                            is.close();
                        }
                    }
                }
                if (temp) {
                    log.info("ftp服务器上没有该文件：" + remoteFileName);
                }
            }
        } catch (Exception e) {
            log.error("附件下载异常", e);
        } finally {
            if (ftpClient != null) {
                try {
                    ftpClient.disconnect();
                    ftpClient = null;
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }


    @RequestMapping(value = "/oneDryFileDownload.spr")
    @PublicServ
    public void downOneDryFile(@RequestBody String param, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String decode = URLDecoder.decode(param, "UTF-8").substring(13);
        int csrf = decode.lastIndexOf("&_csrf");
        String csrfStr = decode.substring(0,csrf);
        Map<String, Object> params = JSONObject.fromObject(csrfStr);
        String oneDryFileName = MapUtils.getString(params,"fileName"); //文件的真实名称
        String oneDryIp = MapUtils.getString(params,"ip"); ; //ip
        String oneDryPort = MapUtils.getString(params,"port"); ; //端口
        String oneDryPath = MapUtils.getString(params,"filePath"); ; //一干路径
        String oneDryUserName = MapUtils.getString(params,"userName"); ; //一干用户名
        String oneDryPassword = MapUtils.getString(params,"password"); ; //一干密码
        String oneDryFileId = MapUtils.getString(params,"fileId"); ; //一干密码
        //判断浏览器类型
        String agent = request.getHeader("USER-AGENT").toLowerCase();
        if (agent != null && (agent.indexOf("msie") > -1 || agent.indexOf("trident") > -1 || agent.indexOf("edge") > -1)) { //IE
            oneDryFileName = URLEncoder.encode(oneDryFileName, "UTF-8");
            oneDryFileName = oneDryFileName.replaceAll("\\+", "%20");
        }
        else {
            oneDryFileName = new String(oneDryFileName.getBytes("UTF-8"), "ISO-8859-1");
        }
        SftpUtils sftpUtils = new SftpUtils(oneDryIp,Integer.parseInt(oneDryPort),oneDryUserName,oneDryPassword);
        ChannelSftp sftp = sftpUtils.connect();
        try {
            Vector<?> ftpFiles = sftp.ls(oneDryPath);
            Boolean temp = true;
            for (Object ftpFile : ftpFiles){

                    ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) ftpFile;
                    if(oneDryFileId.equalsIgnoreCase(entry.getFilename())){
                        OutputStream os = null;
                        InputStream is = null;
                        temp = false;
                        sftp.cd(oneDryPath);
                        is=sftp.get(oneDryFileId);
                        response.setContentType("multipart/form-data");
                        /* 设置文件头：最后一个参数是设置下载文件名 */
                        response.setHeader("Content-Disposition", "attachment;filename=" + oneDryFileName);
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
                log.info("ftp服务器上没有该文件：" + oneDryFileName);
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
                    log.error(e.getMessage());
                }
            }
        }

    }


}
