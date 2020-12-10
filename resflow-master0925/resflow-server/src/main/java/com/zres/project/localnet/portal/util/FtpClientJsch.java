package com.zres.project.localnet.portal.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class FtpClientJsch extends IomFtpClient {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(FtpClientJsch.class);
    private Session sshSession = null;
    private Channel channel = null;
    private ChannelSftp client = null;

    public FtpClientJsch(String host, int port, String username, String password) {
        super(host, port, username, password);
    }

    @Override
    public boolean connect() throws Exception {
        boolean isConnect = false;
        try {
            JSch jsch = new JSch();
            sshSession = jsch.getSession(this.getName(), this.getHost(), this.getPort());
            sshSession.setPassword(this.getPassword());
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);
            sshSession.connect();
            channel = sshSession.openChannel("sftp");
            channel.connect();
            client = (ChannelSftp) channel;
            isConnect = true;
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            logger.debug("jsch sftp登录失败:" + e.getMessage(), e);
        }
        return isConnect;
    }

    @Override
    public boolean changeWorkingDirectory(String path) throws Exception {
        boolean flag = true;
        try {
            client.cd(path);
        }
        catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    @Override
    public String[] listNames(String directory) throws Exception {

        List<String> fileList = new ArrayList<String>();
        logger.debug("directory:" + directory);
        @SuppressWarnings("unchecked")
        Vector<LsEntry> sftpFile = client.ls(directory);
        LsEntry isEntity = null;
        String fileName = null;
        Iterator<LsEntry> sftpFileNames = sftpFile.iterator();
        while (sftpFileNames.hasNext()) {
            isEntity = (LsEntry) sftpFileNames.next();
            fileName = isEntity.getFilename();
            logger.debug("=====" + fileName);
            fileList.add(fileName);
        }
        String[] array = new String[fileList.size()];
        for (int i = 0; i < fileList.size(); i++) {
            array[i] = fileList.get(i);
        }
        return array;
    }

    @Override
    public boolean retrieveFile(String path, OutputStream os) throws Exception {
        boolean flag = true;
        try {
            client.get(path, os);
        }
        catch (Exception e) {
            flag = false;
        }
        return flag;
    }


    @Override
    public void deleteFile(String pathName) throws Exception {
        client.rm(pathName);
    }

    @Override
    public boolean rename(String from, String to) throws Exception {
        boolean flag = true;
        try {
            client.rename(from, to);
        }
        catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    @Override
    public boolean logout() throws Exception {
        boolean flag = true;
        try {
            client.exit();
        }
        catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    @Override
    public boolean isConnected() throws Exception {
        return client.isConnected();
    }

    @Override
    public void disconnect() throws Exception {
        if (client != null && client.isConnected()) {
            client.disconnect();
        }
        if (channel != null && channel.isConnected()) {
            channel.disconnect();
        }
        if (sshSession != null && sshSession.isConnected()) {
            sshSession.disconnect();
        }
    }

    @Override
    public boolean makeDirectory(String pathName) throws Exception {
        boolean flag = true;
        try {
            client.mkdir(pathName);
        }
        catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    @Override
    public boolean storeFile(String path, InputStream is) throws Exception {
        boolean flag = true;
        try {
            client.put(is, path);
        }
        catch (Exception e) {
            flag = false;
        }
        return flag;
    }
}