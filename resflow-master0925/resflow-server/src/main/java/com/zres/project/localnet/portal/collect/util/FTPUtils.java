package com.zres.project.localnet.portal.collect.util;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ztesoft.res.frame.flow.task.service.PubService;
import com.ztesoft.res.frame.flow.task.service.PubVal;
import com.ztesoft.res.frame.flow.task.service.SysParams;
import com.ztesoft.res.frame.web.util.ResMessageSourceUtil;


public class FTPUtils {

    private static final Logger logger = LoggerFactory.getLogger(FTPUtils.class);

    private static FTPClient ftpClient = new FTPClient();
    private static String encoding = System.getProperty("file.encoding");
    /**
     * FTP服务器hostname
     */
    private static String url;
    /**
     * FTP登录账号
     */
    private static String username;
    /**
     * FTP登录密码
     */
    private static String password;
    /**
     * FTP服务器保存目录,如果是根目录则为“/”
     */
    private static String path = "/";
    /**
     * FTP服务器端口
     */
    private static int port = 21;

    public FTPUtils(String ftpData) {
        init(ftpData);
    }

    private void init(String ftpData) {
        //读取配置文件  文件上传服务器信息
        //Properties prop = new Properties();
        //FTP信息配置格式{url:xxx,port:xx,username:xxx,password:xxx}
        String ftpServerInfo = SysParams.getIns().find(ftpData);
        try {
            JSONObject jsonObject = new JSONObject(ftpServerInfo);
            url = jsonObject.getString("url");
            Object portObj = jsonObject.get("port");
            if (portObj instanceof Integer) {
                port = (Integer) jsonObject.get("port");
            }
            else {
                port = Integer.parseInt(jsonObject.getString("port"));
            }
            username = jsonObject.getString("username");
            password = jsonObject.getString("password");
        }
        catch (JSONException e) {
            // TODO Auto-generated catch block
            logger.error("FTP " + ResMessageSourceUtil.getMessage("flow.conf.excep"), e);
        }
    }

    /**
     * Description: 向FTP服务器上传文件
     *
     * url      FTP服务器hostname
     * port     FTP服务器端口
     * username FTP登录账号
     * password FTP登录密码
     * path     FTP服务器保存目录,如果是根目录则为“/”
     * filename 上传到FTP服务器上的文件名
     * @param input    本地文件输入流
     * @return 成功返回true，否则返回false
     * @Version1.0
     */
    public static boolean uploadFile(String ftpUploadDir, String filename, InputStream input) {
        boolean result = false;
        try {
            int reply;
            // 如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
            //ftpClient.connect(url);
            // 连接FTP服务器
            ftpClient.connect(url, port);
            logger.debug(ResMessageSourceUtil.getMessage("flow.ftp.addr") + ": " + url + ":" + port);
            // 登录
            ftpClient.login(username, password);
            logger.debug(ResMessageSourceUtil.getMessage("flow.user.pass") + ": " + "username=" + username + "\t password=" + password);
            // 设置PassiveMode传输
            ftpClient.enterLocalPassiveMode();
            // 设置以二进制流的方式传输
            ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            // 检验是否连接成功
            reply = ftpClient.getReplyCode();
            logger.debug("replyCode = " + reply);
            logger.debug("replyString: \n" + ftpClient.getReplyString());
            if (!FTPReply.isPositiveCompletion(reply)) {
                logger.error(ResMessageSourceUtil.getMessage("flow.connect.fail"));
                ftpClient.disconnect();
                return result;
            }
            // 转移工作目录至指定目录下
            //String workDir = path + ftpUploadDir;
            String filename1 = new String(filename.getBytes("GBK"), "ISO-8859-1");
            String workDir = ftpUploadDir;
            String path1 = new String(workDir.getBytes("GBK"), "ISO-8859-1");
            boolean isChangeWork = ftpClient.changeWorkingDirectory(workDir);
            if (!isChangeWork) {
                boolean isMade = ftpClient.makeDirectory(path1);
                if (!isMade) {
                    throw new IOException("ftp上传文件创建目录失败");
                }
                isChangeWork = ftpClient.changeWorkingDirectory(path1);
            }
            ftpClient.storeFile(filename1, input);
            ftpClient.logout();
            logger.debug(isChangeWork ? ResMessageSourceUtil.getMessage("flow.exist") : ResMessageSourceUtil.getMessage("flow.no.exist"));
            logger.debug("workDir=" + workDir);
            result = true;
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                }
                catch (IOException ioe) {
                    logger.error(ioe.getMessage());
                }
            }
        }
        catch (IOException e) {
            logger.error(ResMessageSourceUtil.getMessage("flow.upload.fail"), e);
        }
        return result;
    }

    /**
     * 在ftp服务器上建目录
     *
     * @param dir
     * @return
     * @throws IOException
     */
    public static boolean createDirectory(String dir) throws IOException {
        boolean flag = true;

        ftpClient.changeWorkingDirectory(path);
        if (dir.startsWith("/")) {
            dir = dir.substring(1);
        }
        if (dir.endsWith("/")) {
            dir = dir.substring(0, dir.length() - 1);
        }

        String[] dirNames = dir.split("/");
        for (String dirName : dirNames) {
            if (!ftpClient.changeWorkingDirectory(dirName)) {
                if (ftpClient.makeDirectory(dirName)) {
                    ftpClient.changeWorkingDirectory(dirName);
                }
                else {
                    flag = false;
                    break;
                }
            }
        }

        return flag;
    }

    /**
     * 将本地文件上传到FTP服务器上
     *
     */
    /*
	 * public void testUpLoadFromDisk() { try { FileInputStream in = new
	 * FileInputStream(new File("D:/SVN/SVN.rar")); boolean flag =
	 * uploadFile("10.45.44.197", 21, "res","*383*67", "/toZsj", "SVN2.rar",
	 * in); logger.debug(flag); } catch (FileNotFoundException e) {
	 * e.printStackTrace(); } }
	 */

    /**
     * Description: 从FTP服务器下载文件
     *
     * url        FTP服务器hostname
     * port       FTP服务器端口
     * username   FTP登录账号
     * password   FTP登录密码
     * remotePath FTP服务器上的相对路径
     * @param fileName   要下载的文件名
     * @param localPath  下载后保存到本地的路径
     * @return
     * @Version1.0
     */
    public static boolean downFile(String ftpDir, String fileName, String localPath) {
        boolean result = false;
        try {
            //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
            localPath = "C" + localPath.substring(1);
            int reply;
            ftpClient.setControlEncoding("utf-8");

			/*
			 * 为了上传和下载中文文件，有些地方建议使用以下两句代替 new
			 * String(remotePath.getBytes(encoding),"iso-8859-1")转码。 经过测试，通不过。
			 */
            // FTPClientConfig conf = new
            // FTPClientConfig(FTPClientConfig.SYST_NT);
            // conf.setServerLanguageCode("zh");
            ftpClient.connect(url, port);
            // 如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
            ftpClient.login(username, password); // 登录
            // 设置文件传输类型为二进制
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

            // 获取ftp登录应答代码
            reply = ftpClient.getReplyCode();
            // 验证是否登陆成功
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                //System.err.println("FTP server refused connection.");
                return result;
            }
            ftpClient.enterLocalPassiveMode();
            // 转移到FTP服务器目录至指定的目录下
            String workDir = path + ftpDir;
            ftpClient.changeWorkingDirectory(new String(workDir.getBytes(encoding), "iso-8859-1"));
            // 获取文件列表
            FTPFile[] fs = ftpClient.listFiles();
            /**
             * 修改人 lan.ruxu
             * 修改日期 2015-1-30
             * zmp单号 638835
             * 描述 附件下载报错
             */
            File localDir = new File(localPath);
            if (!localDir.exists()) {
                localDir.mkdirs();
            }
            for (FTPFile ff : fs) {
//				String ftpFileName = new String(ff.getName().getBytes("iso-8859-1"),"utf-8");
//				logger.debug("ftpFileName========"+ftpFileName);
                if (ff.getName().equals(fileName)) {
                    File localFile = new File(localPath + "/" + ff.getName());

                    OutputStream is = new FileOutputStream(localFile);
                    ftpClient.retrieveFile(ff.getName(), is);
                    is.close();
                }
            }

            ftpClient.logout();
            result = true;
        }
        catch (IOException e) {
            logger.error(ResMessageSourceUtil.getMessage("flow.download.fail"), e);
        }
        finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                }
                catch (IOException ioe) {
                    logger.error(ioe.getMessage());
                }
            }
        }
        return result;
    }

    /**
     * 从一个ftp地址复制文件到流程ftp地址
     * ftp地址信息3种情况： 1）ftp://test:test@10.45.53.197/home/test/2016/12/13/1000093.txt
     * 2) ftp://10.45.53.197/home/test/2016/12/13/1000093.txt
     * 3) home/test/2016/12/13/1000093.txt
     *
     * @return 返回一个map里面记录文件的属性信息及复制结果
     */
    public static Map copyFileFromFtp(String outFtpInfos, String ftpDir, String ftpFileName) {
        //先从数据库读取外部ftp地址信息 如果传入的ftp地址里面包含这些信息就从ftp里面获取
        //{"value":[{"file_size":"0","ftp_dir":"home/test/2016/12/14/","store_type":"1","file_name":"测试上传2.txt","ftp_file_name":"1000140.txt"}]}
        Map result = new HashMap<String, String>();
        String outUrl = "";
        int outPrt = 21;
        String outUsername = "";
        String outPassword = "";
        //需要复制的文件信息
        String fileName = "";
        String fileDir = "";
        String ftpServerInfo = PubService.getInstance().getSysParamByKey(PubVal.SYS_P_OUT_FTP_SERVER_INFO);
        try {
            JSONObject jsonObject = new JSONObject(ftpServerInfo);
            outUrl = jsonObject.getString("url");
            outPrt = Integer.parseInt(jsonObject.getString("port"));
            outUsername = jsonObject.getString("username");
            outPassword = jsonObject.getString("password");
        }
        catch (JSONException e) {
            //如果没有获取到配置数据 照样可以从ftp地址中获取相关的信息
            logger.debug(ResMessageSourceUtil.getMessage("flow.getftpsddr.err"));
        }
        //如果ftp地址中带了ip等信息 就从中获取信息
        try {
            if (outFtpInfos.indexOf("ftp://") > -1) {
                if (outFtpInfos.indexOf('@') > -1) {
                    //带用户名及密码
                    outUsername = outFtpInfos.split("//")[1].split(":")[0];
                    outPassword = outFtpInfos.split("//")[1].split(":")[1].split("@")[0];
                    outUrl = outFtpInfos.split("//")[1].split("@")[1].split("/")[0];
                }
                else {
                    outUrl = outFtpInfos.split("//")[1].split("/")[0];
                }
                fileDir = outFtpInfos.split("//")[1].substring(outFtpInfos.split("//")[1].indexOf('/') + 1, outFtpInfos.split("//")[1].length() - fileName.length() - 1);
                fileDir = fileDir.substring(0, fileDir.lastIndexOf('/'));
            }
            else {
                //只有一个绝对路径： home/test/2016/12/13/1000093.txt
                fileDir = outFtpInfos.substring(0, outFtpInfos.lastIndexOf('/'));
            }
            fileName = outFtpInfos.substring(outFtpInfos.lastIndexOf('/') + 1, outFtpInfos.length());
        }
        catch (Exception e) {
            //如果发生异常就是传入的ftp地址格式不对
            logger.error("", e);
            result.put("flag", "0");
            result.put("msg", ResMessageSourceUtil.getMessage("flow.check.ftpstyle"));
            return result;
        }
        FTPClient outFtpClient = new FTPClient();
        int reply;
        try {
            //外系统
            outFtpClient.connect(url);
            // 连接FTP服务器
            outFtpClient.login(username, password);
            reply = outFtpClient.getReplyCode();
            outFtpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            if (!FTPReply.isPositiveCompletion(reply)) {
                logger.debug(ResMessageSourceUtil.getMessage("flow.connect.fail"));
                result.put("flag", "0");
                result.put("msg", ResMessageSourceUtil.getMessage("flow.connect.out.fail"));
                return result;
            }
            //获取下载的流 目录一定存在 不存在就表示文件不存在
            boolean change = outFtpClient.changeWorkingDirectory(path);
            change = outFtpClient.changeWorkingDirectory(fileDir);
            if (!change) {
                logger.debug(ResMessageSourceUtil.getMessage("flow.out.confirm"));
                result.put("flag", "0");
                result.put("msg", ResMessageSourceUtil.getMessage("flow.out.confirm"));
                return result;
            }
            // 获取流程的ftp流信息
            //上传的ftp
            ftpClient.connect(url);
            // 连接FTP服务器
            ftpClient.login(username, password);
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                logger.debug(ResMessageSourceUtil.getMessage("flow.connect.fail"));
                ftpClient.disconnect();
                result.put("flag", "0");
                result.put("msg", ResMessageSourceUtil.getMessage("flow.connect.ftp.fail"));
                return result;
            }
            change = ftpClient.changeWorkingDirectory(path);
            change = ftpClient.changeWorkingDirectory(ftpDir);
            if (!change) {
                //切换失败，目录不存在，新建
                if (createDirectory(ftpDir)) {
                    change = true;
                }
            }
            if (change) {
                OutputStream os = ftpClient.appendFileStream(ftpFileName);
                // 获取文件列表
                FTPFile[] fs = outFtpClient.listFiles();
                for (FTPFile ff : fs) {
                    if (ff.getName().equals(fileName)) {
                        outFtpClient.retrieveFile(ff.getName(), os);
                        os.close();
                        //获取文件信息传给外部
                        result.put(PubVal.MAP_KEY_FILE_NAME, ff.getName());
                        result.put(PubVal.MAP_KEY_FTP_DIR, ftpDir);
                        result.put(PubVal.MAP_KEY_FTP_FILE_NAME, ftpFileName);
                        result.put(PubVal.MAP_KEY_FILE_SIZE, ff.getSize());
                        result.put("flag", "1");
                        result.put("msg", "success");
                    }
                }
                if (result.size() == 0) {
                    //循环结束后如果没有发现文件就返回错误信息
                    result.put("flag", "0");
                    result.put("msg", ResMessageSourceUtil.getMessage("flow.file.noexist"));
                }
            }
            else {
                result.put("flag", "0");
                result.put("msg", ResMessageSourceUtil.getMessage("flow.change.fail"));
            }
            outFtpClient.logout();
            ftpClient.logout();
        }
        catch (Exception e) {
            result.put("flag", "0");
            result.put("msg", e.getMessage());
            logger.error("", e);
        }
        finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                }
                catch (IOException ioe) {
                    logger.error(ioe.getMessage());
                }
                if (outFtpClient.isConnected()) {
                    try {
                        outFtpClient.disconnect();
                    }
                    catch (IOException ioe) {
                        logger.error(ioe.getMessage());
                    }
                }
            }
        }
        return result;
    }
    /**
     * 将FTP服务器上文件下载到本地
     *
     */
	/*
	 * public void testDownFile() { try { boolean flag = downFile("127.0.0.1",
	 * 21, "zlb", "123", "/", "哈哈.txt", "D:/"); logger.debug(flag); }
	 * catch (Exception e) { e.printStackTrace(); } }
	 * 
	 * public static void main(String[] args) { FtpApache fa = new FtpApache();
	 * fa.testUpLoadFromDisk(); }
	 */
}
