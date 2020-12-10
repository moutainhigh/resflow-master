package com.zres.project.localnet.portal.util;

import java.io.*;
import java.net.SocketException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author <a href="xu.wenhui@zte.com.cn">徐文辉</a>
 * @version 1.0
 * @since 2011-6-30 下午08:38:24
 */

public final class FtpUtils {

	private FtpUtils() {
		super();
	}

    private static final Log log = LogFactory.getLog(FtpUtils.class);
    
    private static String errMsg = "ftp连接不上，请检查ftp服务器状态或连接ftp配置";
    private static final String IOSCODE = "ISO-8859-1";

    /**
     * 上传文件至ftp服务器
     *
     * @param ip         IP
     * @param port       端口
     * @param userName   用户名
     * @param password   密码
     * @param localFile  本地文件
     * @param remotePath 远程文件目录
     * @throws IOException 
     * @throws Exception
     */
    public static void uploadFtp(String ip, int port, String userName, String password, File localFile,
                                 String remotePath) throws IOException   {
        InputStream in = null;
        FTPClient ftpClient = null;
        if (localFile != null) {
            try {
                ftpClient = new FTPClient();
                in = new FileInputStream(localFile);
                ftpClient.connect(ip, port);
                if (ftpClient.login(userName, password)) {
                    ftpClient.enterLocalPassiveMode();
                    // 改变工作目录
                    changWorkingDirectory(remotePath, ftpClient, true);

                    // 设置为二进制传输模式
                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                    ftpClient.setControlEncoding("GBK");
                    // 被动模式
                    ftpClient.enterLocalPassiveMode();
                    boolean flag = ftpClient.storeFile(new String(localFile.getName().getBytes("GBK"), IOSCODE), in);
                    if (!flag) {
                    	 //主动模式
                        ftpClient.enterLocalActiveMode();
                        flag = ftpClient.storeFile(new String(localFile.getName().getBytes("GBK"), IOSCODE), in);
                    }
                    if (!flag) {
//                        throw new AuditException("上传文件[" + localFile.getName() + "]失败");
                        log.info("上传文件[" + localFile.getName() + "]失败");
                    }
                }
                else {
//                    throw new AuditException(errMsg);
                    log.info(errMsg);
                }

            }
            catch (SocketException e) {
                log.error(e.getMessage(), e);
                throw e;
            }
            catch (IOException e) {
                log.error(e.getMessage(), e);
                throw e;
            }
            catch (Exception e) {
                log.error(e.getMessage(), e);

//      throw e;
            }

            finally {
                if (in != null) {
                    try {
                        in.close();
                        in = null;
                    }
                    catch (IOException e) {
                        log.error(e.getMessage());
                    }
                }
                if (ftpClient != null) {
                    try {
                        ftpClient.disconnect();
                        ftpClient = null;
                    }
                    catch (IOException e) {
                    	 log.error(e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * 改变ftp的工作目录
     *
     * @param remotePath
     * @param ftpClient
     * @param isMakeDir
     * @throws IOException
     */
    public static void changWorkingDirectory(String remotePath, FTPClient ftpClient, boolean isMakeDir)
            throws IOException {
        if (remotePath != null) {
            String[] rps = remotePath.split("/");
            for (int i = 0; i < rps.length; i++) {
        		if ((!"".equals(rps[i])) && (!ftpClient.changeWorkingDirectory(rps[i]))) {
        			if (isMakeDir) {
        				ftpClient.makeDirectory(rps[i]);
        				ftpClient.changeWorkingDirectory(rps[i]);
        			}
        			else {
                //        				throw new AuditException("找不到该目录:" + rps[i]);
                        log.info("找不到该目录:" + rps[i]);
        			}
        		}
            }
        }
    }

    /**
     * 从ftp下载文件到本地
     *
     * @param ip             IP
     * @param port           端口
     * @param userName       用户名
     * @param password       密码
     * @param remotePath     远程文件目录
     * @param remoteFileName 远程文件
     * @param localFilePath  本地文件目录
     * @param fileName  下载后附件名称
     * @throws IOException 
     * @throws SocketException 
     * @throws Exception
     */
    public static void downloadFtp(String ip, int port, String userName, String password, String remotePath,
                                   String remoteFileName, String localFilePath, String fileName) throws  IOException {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip, port);
            // 登陆
            if (ftpClient.login(userName, password)) {
                ftpClient.enterLocalPassiveMode();

                // 改变工作目录
                changWorkingDirectory(remotePath, ftpClient, false);
                ftpClient.setControlEncoding("GBK");
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                // 获取FTP登陆目录下的所有文件

                Boolean temp = true;
                FTPFile[] files = ftpClient.listFiles();
                for (FTPFile file : files) {
                    if (remoteFileName.equalsIgnoreCase(file.getName())) {
                    	temp = false;
                        BufferedOutputStream out = null;
                        try {
                            // IO流下载文件到本地
                            out = new BufferedOutputStream(
                                    new FileOutputStream(new File(localFilePath, fileName)));
                            // 开始下载

                            // 文件下载时使用的文件名采用ISO-8859-1编码，否则中文下载的内容为空
                            ftpClient.retrieveFile(new String(file.getName().getBytes("GBK"), IOSCODE), out);
                            if (log.isDebugEnabled()) {
                                log.debug("下载文件:" + file.getName() + "到本地路径:" + localFilePath);
                            }
                        }
                        finally {
                            try {
                                if (out != null) {
                                    out.close();
                                }
                            }
                            catch (Exception e) {
                                log.error(e.getMessage(), e);
                            }
                        }
                    }
                }
                if (temp) {
                	//throw new AuditException("ftp服务器上没有该文件："+remoteFileName);
                    log.info("ftp服务器上没有该文件：" + remoteFileName);
                }
            }
            else {
//                throw new AuditException(errMsg);
                log.info(errMsg);
            }
        }
        finally {
            if (ftpClient != null) {
                try {
                    ftpClient.disconnect();
                    ftpClient = null;
                }
                catch (IOException e) {
                   log.error(e.getMessage());
                }
            }
        }
    }

    /**
     * 从ftp上删除文件
     *
     * @param ip             IP
     * @param port           端口
     * @param userName       用户名
     * @param password       密码
     * @param remotePath     远程文件目录
     * @param remoteFileName 远程文件
     * @throws IOException 
     * @throws SocketException 
     * @throws Exception
     */
    public static void deleteFtpFile(String ip, int port, String userName, String password, String remotePath,
                                     String remoteFileName) throws IOException  {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip, port);
            // 登陆
            if (ftpClient.login(userName, password)) {
                ftpClient.enterLocalPassiveMode();
                // 改变工作目录
                changWorkingDirectory(remotePath, ftpClient, false);
                ftpClient.setControlEncoding("GBK");
                // 删除ftp上文件
                ftpClient.deleteFile(remoteFileName);
            }
            else {
//                throw new AuditException(errMsg);
                log.info(errMsg);
            }
        }
        finally {
            try {
                ftpClient.disconnect();
                ftpClient = null;
            }
            catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    /**
     * 从ftp下载文件到本地
     *
     * @param ip         IP
     * @param port       端口
     * @param userName   用户名
     * @param password   密码
     * @param remotePath 远程文件目录
     * @throws IOException 
     * @throws SocketException 
     * @throws Exception
     */
    public static List<String> listFtpFileNames(String ip, int port, String userName, String password, String remotePath) throws  IOException {
        FTPClient ftpClient = null;
        List<String> fileNameList = new ArrayList<String>();
        try {
            ftpClient = new FTPClient();
            ftpClient.connect(ip, port);
            // 登陆
            if (ftpClient.login(userName, password)) {
                ftpClient.enterLocalPassiveMode();
                // 改变工作目录
                changWorkingDirectory(remotePath, ftpClient, false);
                ftpClient.setControlEncoding("GBK");
                // 获取FTP登陆目录下的所有文件

                FTPFile[] files = ftpClient.listFiles();
                for (FTPFile file : files) {
                    if (file.isFile()) {
                        fileNameList.add(file.getName());
                    }
                }
            }
            else {
//                throw new AuditException(errMsg);
                log.info(errMsg);
            }
        }
        finally {
            if (ftpClient != null) {
                try {
                    ftpClient.disconnect();
                    ftpClient = null;
                }
                catch (IOException e) {
                	log.error(e.getMessage());
                }
            }
        }
        return fileNameList;
    }

    /**
     * 从FTP 上下载文件直接返回到浏览器客户端
     * @param ip                  FTP地址
     * @param port                端口号
     * @param userName            用户名
     * @param password            密码
     * @param remotePath          FTP远程文件目录
     * @param remoteFileName      FTP远程文件名称
     * @param fileName            文件真实名称
     * @param response            返回浏览器的响应
     * @param request             返回浏览器的请求
     * @throws IOException
     */
    public static void downloadFtpToBrowserClient(String ip, int port, String userName, String password, String remotePath,
                                                  String remoteFileName, String fileName, HttpServletRequest request, HttpServletResponse response) throws IOException {
        //从ftp上下载附件，输出为流
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip, port);
            if (ftpClient.login(userName, password)) {
                ftpClient.enterLocalPassiveMode();

                // 改变工作目录
                changWorkingDirectory(remotePath, ftpClient, false);
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

                            //判断浏览器类型
                            String userAgent = request.getHeader("USER-AGENT");
                            //IE
                            if (userAgent != null && (userAgent.indexOf("MSIE") > -1 || userAgent.indexOf("TRIDENT") > -1 || userAgent.indexOf("EDGE") > -1)){
                                fileName = URLEncoder.encode(fileName, "UTF-8");
                                //使用URLEncoder编解码 + 号 与 空格替换
                                fileName = fileName.replaceAll("\\+", "%20");
                            }else {
                                //其他浏览器
                                fileName = new String(fileName.getBytes("UTF-8"), "ISO-8859-1");
                            }
                             /* 设置文件头：最后一个参数是设置下载文件名 */
                            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
                            os = response.getOutputStream();
                            byte[] b = new byte[1024];
                            int len;
                            while ((len = is.read(b)) > 0) {
                                os.write(b, 0, len);
                            }
                        }
                        catch (IOException e) {
                            throw new IOException("附件下载失败！");
                        }
                        finally {
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
        }
        finally {
            if (ftpClient != null) {
                try {
                    ftpClient.disconnect();
                    ftpClient = null;
                }
                catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }

    }

}
