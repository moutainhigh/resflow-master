package com.zres.project.localnet.portal.util;

import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.LoggerFactory;

/**
 * @author zhang.tiansheng
 *         此抽象类继承了ftp和sftp两种方式的一些基本操作
 */
public abstract class IomFtpClient {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(IomFtpClient.class);
    private String host = "";
    private int port = 21;
    private String name = "";
    private String password = "";

    public IomFtpClient(String host, int port, String username, String password) {
        this.host = host;
        if (port > 0) {
            this.port = port;
        }
        this.name = username;
        this.password = password;
    }

    /**
     * 工厂方法，判断能连上哪种方式就构建哪种方式的子类
     *
     * @param host
     * @param port
     * @param username
     * @param password
     * @return
     * @throws Exception
     */
    public static IomFtpClient getFtpClient(String host, String port, String username, String password) throws Exception {
        IomFtpClient client = null;
        try {
            logger.debug("开始apache ftp方式连接服务器！服务器信息为host=" + host + "---port:" + port + "---username:" + username + "---password:" + password);
            client = new FtpClientApache(host, Integer.parseInt(port), username, password);
            boolean isConnect = client.connect();
            if (isConnect && client.isConnected()) {
                logger.debug("apache ftp方式连接服务器成功！");
                return client;
            }
        }
        catch (Exception e) {
            logger.debug("apache ftp方式连接失败:" + e.getMessage(), e);
        }
        try {
            logger.debug("切换采用jsch sftp方式连接服务器！");
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
        logger.debug("获取ftp连接失败");
        return null;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public abstract boolean connect() throws Exception;

    public abstract boolean changeWorkingDirectory(String path) throws Exception;

    public abstract String[] listNames(String directory) throws Exception;

    public abstract boolean retrieveFile(String path, OutputStream os) throws Exception;

    public abstract boolean rename(String from, String to) throws Exception;

    public abstract void deleteFile(String path) throws Exception;

    public abstract boolean logout() throws Exception;

    public abstract boolean isConnected() throws Exception;

    public abstract void disconnect() throws Exception;

    public abstract boolean makeDirectory(String pathName) throws Exception;

    public abstract boolean storeFile(String path, InputStream is) throws Exception;

}