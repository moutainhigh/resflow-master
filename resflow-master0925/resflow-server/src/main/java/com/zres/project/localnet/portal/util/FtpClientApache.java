package com.zres.project.localnet.portal.util;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.slf4j.LoggerFactory;

public class FtpClientApache extends IomFtpClient {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(FtpClientApache.class);
    private FTPClient client = null;

    public FtpClientApache(String host, int port, String username, String password) {
        super(host, port, username, password);
    }

    @Override
    public boolean connect() throws Exception {
        boolean isConnect = false;
        try {
            client = new FTPClient();
            client.connect(this.getHost(), this.getPort());
            client.login(this.getName(), this.getPassword());
            client.setControlEncoding("GBK");
            FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_NT);
            conf.setServerLanguageCode("zh");
            client.configure(conf);
            client.setDataTimeout(60000);
            client.setFileType(FTP.BINARY_FILE_TYPE);
            client.enterLocalPassiveMode(); // 设置为被动模式登录
            isConnect = true;
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            logger.debug("apache ftp登录失败:" + e.getMessage(), e);
        }
        return isConnect;
    }

    @Override
    public boolean changeWorkingDirectory(String path) throws Exception {
        return client.changeWorkingDirectory(path);
    }

    @Override
    public String[] listNames(String directory) throws Exception {
        return client.listNames(directory);
    }

    @Override
    public boolean retrieveFile(String path, OutputStream os) throws Exception {
        return client.retrieveFile(path, os);
    }


    @Override
    public void deleteFile(String pathName) throws Exception {
        client.deleteFile(pathName);
    }

    @Override
    public boolean rename(String from, String to) throws Exception {
        return client.rename(from, to);
    }

    @Override
    public boolean logout() throws Exception {
        return client.logout();
    }

    @Override
    public boolean isConnected() throws Exception {
        return client.isConnected();
    }

    @Override
    public void disconnect() throws Exception {
        client.disconnect();
    }

    @Override
    public boolean makeDirectory(String pathName) throws Exception {
        return client.makeDirectory(pathName);
    }

    @Override
    public boolean storeFile(String path, InputStream is) throws Exception {
        return client.storeFile(path, is);
    }
}