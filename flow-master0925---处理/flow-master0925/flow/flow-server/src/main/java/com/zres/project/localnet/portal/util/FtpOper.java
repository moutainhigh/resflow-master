package com.zres.project.localnet.portal.util;

import java.io.*;
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
        from = from.replaceAll("//", "/");
        to = to.replaceAll("//", "/");
        logger.info("FTP======>from:[{}]", from);
        logger.info("FTP======>to:[{}]", to);
        int ret = 0;
        ByteArrayOutputStream download = new ByteArrayOutputStream();
        fromClient.retrieveFile(from, download);
        byte[] buffer = download.toByteArray();
        ret = buffer.length;
        InputStream upload = new ByteArrayInputStream(buffer);
        toClient.storeFile(to, upload);
        return ret;
    }

    /**
     * 下载上传文件
     *
     * @param remotePath
     * @param remoteFileName
     * @param fromClient
     * @param toClient
     * @return
     * @throws IOException
     */
    public static boolean ftpCopyFile(String remotePath, String remoteFileName, String uploadPath,
        String uploadFileName, IomFtpClient fromClient, IomFtpClient toClient) throws Exception {
        boolean bol = true;
        try {
            String folder = System.getProperty("java.io.tmpdir");
            File tempFile = new File(folder + File.separator + uploadFileName);
            File fileParent = tempFile.getParentFile();
            if (!fileParent.exists()) {
                fileParent.mkdirs();
            }
            FtpUtils.downloadFtp(fromClient.getHost(), fromClient.getPort(), fromClient.getName(),
                fromClient.getPassword(), remotePath, remoteFileName, folder, uploadFileName);
            if (tempFile != null) {
                FtpUtils.uploadFtp(toClient.getHost(), toClient.getPort(), toClient.getName(), toClient.getPassword(),
                    tempFile, uploadPath);
            }
            tempFile.delete();
        } catch (Exception e) {
            bol = false;
        }
        return bol;
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
