package com.zres.project.localnet.portal.util;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderQrySecondaryDao;

/**
 * 从自己的服务器上传附件给集客能力平台服务器
 */
@Service
public class UpdateToJiKe {

    Logger logger = LoggerFactory.getLogger(UpdateToJiKe.class);

    @Autowired
    private OrderQrySecondaryDao orderQrySecondaryDao;

    public boolean updateFileToFtp(Map<String, Object> attachment,String origin){

        boolean flag = false;
        IomFtpClient FtpClient = null;
        IomFtpClient JTFtpClient = null;
        try {
            // 需要下载的服务器信息
            FtpConfigDto JTFtpConfig = getFtpConfigInfo("FTP_INFO");
            // 需要上传的服务器信息
            FtpConfigDto FtpConfig = getFtpConfigInfo("JIKE_FTP_INFO");
            JTFtpClient = getFtpConfig(JTFtpConfig.getIp(), JTFtpConfig.getPort(), JTFtpConfig.getUsername(),
                    JTFtpConfig.getPassword());
            FtpClient = getSftpConfig(FtpConfig.getIp(), FtpConfig.getPort(), FtpConfig.getUsername(),
                    FtpConfig.getPassword());
            if (attachment != null && attachment.size() > 0) {
                String file_path = MapUtils.getString(attachment,"path",""); //集团附件FILE_PATH
                String file_id = MapUtils.getString(attachment,"name",""); //集团附件FILE_ID带后缀
                if ("uploadJiKe".equals(origin)) {
                    // 上传集客时，把附件表记录的filePath改成服务器根目录下的路径
                    String rootpath = JTFtpConfig.getLocaldir();
                    String remotePath = JTFtpConfig.getRemotedir();
                    if (rootpath.contains(remotePath)) {
                        file_path = rootpath.replace(remotePath, file_path);
                    }
                    String newPath = "/upload/";
                    int mun = FtpOper.ftpTransFile(file_path + "/" + file_id, newPath + file_id, JTFtpClient, FtpClient);
                    if(mun > 0){
                        flag = true;
                    }else {
                        flag = false;
                    }
                }
            }
        }
        catch (Exception e) {
            flag = false;
            logger.debug("附件处理异常" + e);
        }
        finally {
            FtpOper.disConnect(JTFtpClient);
            FtpOper.disConnect(FtpClient);
        }
        return flag;
    }

    public FtpConfigDto getFtpConfigInfo(String codeType) throws Exception { // 获取FTP连接
        FtpConfigDto configInfo = null;
        String ip = orderQrySecondaryDao.qryFtpJiKeData(codeType,"ip");
        String port = orderQrySecondaryDao.qryFtpJiKeData(codeType,"port");
        String name = orderQrySecondaryDao.qryFtpJiKeData(codeType,"username");
        String pwd = orderQrySecondaryDao.qryFtpJiKeData(codeType,"password");
        String path = orderQrySecondaryDao.qryFtpJiKeData(codeType,"directory");
        String holdpath = orderQrySecondaryDao.qryFtpJiKeDirData(codeType,"directory"); // 保存目录
        configInfo = new FtpConfigDto(ip, port, name, pwd, path, holdpath);
        return configInfo;

    }

    //连接自己的服务器
    public IomFtpClient getFtpConfig(String ip, String port, String name, String pwd) throws Exception { //获取FTP Client对象
        IomFtpClient ftpClient = IomFtpClient.getFtpClient(ip, port, name, pwd);
        return ftpClient;
    }

    //连接集客能力服务平台
    public IomFtpClient getSftpConfig(String host, String port, String username, String password) { //获取FTP Client对象
        IomFtpClient client = null;
        try {
            logger.debug("采用jsch sftp方式连接服务器！");
            client = new FtpClientJsch(host, Integer.parseInt(port), username, password);
            boolean isConnect = client.connect();
            if (isConnect && client.isConnected()) {
                logger.debug("jsch ftp方式连接服务器成功！服务器信息为host=" + host + "---port:" + port + "---username:" + username + "---password:" + password);
                return client;
            }
        }
        catch (Exception e) {
            logger.debug("jsch ftp方式连接失败:" + e.getMessage(), e);
        }
        return client;
    }
}
