package com.zres.project.localnet.portal.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.slf4j.LoggerFactory;

public class FtpOper {
    private static SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmssSSS");
    private static SimpleDateFormat format1 = new SimpleDateFormat("yyyy/MM");

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(FtpOper.class);

    public static void createFolder(String basePath, IomFtpClient ftpClient) throws Exception {
        try {
            ftpClient.changeWorkingDirectory("/"); // 先退出到根目录
            if (!ftpClient.changeWorkingDirectory(basePath)) { // 进入新文件目录
                String[] nps = basePath.split("/");
                int length = nps.length;
                if (!basePath.endsWith("/")) {
                    length -= 1;
                }
                for (int i = 0; i < length; i++) {
                    if (nps[i].length() > 0) {
                        if (!ftpClient.changeWorkingDirectory(nps[i])) {
                            ftpClient.makeDirectory(nps[i]);
                            ftpClient.changeWorkingDirectory(nps[i]);
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            logger.debug("创建文件夹异常:" + e.getMessage());
        }
    }

    public static String getBaseAttachFilePath(String basePath) {
        String ret = format1.format(Calendar.getInstance().getTime());
        String newPath = basePath;
        if (!basePath.endsWith("/")) {
            newPath = basePath + "/";
        }
        return newPath + "oneDry/" + ret + "/";
    }

    /**
     * 使用内存传递文件，只适用于小文件
     *
     * @param from
     * @param to
     * @param fromClient
     * @param toClient
     * @return
     * @throws IOException
     */
    public static int ftpTransFile(String from, String to, IomFtpClient fromClient, IomFtpClient toClient) throws Exception {
        int ret = 0;
        ByteArrayOutputStream download = new ByteArrayOutputStream();
        fromClient.retrieveFile(from, download);
        byte[] buffer = download.toByteArray();
        ret = buffer.length;
        InputStream upload = new ByteArrayInputStream(buffer);
        boolean uploadFile=toClient.storeFile(to, upload);
        if(uploadFile){
            ret = 1;
        }
        return ret;
    }

    /**
     * 上传文件
     *
     * @param client
     * @param path
     * @param input
     * @return
     */
    public static boolean uploadFile(IomFtpClient client, String path, InputStream input) throws Exception {
        boolean flag = client.changeWorkingDirectory(path);
        if (!flag) {
            createFolder(path, client);
        }
        flag = client.storeFile(path, input);
        return flag;
    }

    public String getSaveFileName(String origName) {
        String ret = format.format(Calendar.getInstance().getTime());
        int num = origName.lastIndexOf('.');
        if (num > -1) {
            ret = ret.concat(".").concat(origName.substring(num + 1));
        }
        return ret;
    }

    public static void disConnect(IomFtpClient ftpClient) {
        try {
            if (ftpClient != null) {
                ftpClient.logout();
                if (ftpClient.isConnected()) {
                    ftpClient.disconnect();
                }
            }
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            logger.debug("" + e);
        }
    }
}
