package com.zres.project.localnet.portal.collect.util;

import java.io.*;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ztesoft.res.frame.core.util.MapUtil;
import com.ztesoft.res.frame.flow.task.service.GomException;
import com.ztesoft.res.frame.flow.task.service.SysParams;

public class TxtExport {

    private static final Logger logger = Logger.getLogger(TxtExport.class);
    private static String pathName = SysParams.getIns().find("SERVER_FILE_PATH");//我们自己服务器上文件生成的地址
    private static String filenameTemp;
    private static String ftpFileName;


    /**
     * 创建目录
     *
     * @return
     */
    public static boolean createDir() {
        File dir = new File(pathName);
        if (dir.exists()) {
            System.out.println("创建目录" + pathName + "失败，目标目录已经存在");
            return false;
        }
        if (!pathName.endsWith(File.separator)) {
            pathName = pathName + File.separator;
        }
        //创建目录
        if (dir.mkdirs()) {
            System.out.println("创建目录" + pathName + "成功！");
            return true;
        }
        else {
            System.out.println("创建目录" + pathName + "失败！");
            return false;
        }
    }

    /**
     * 创建文件
     *
     * @throws IOException
     */
    public static boolean creatTxtFile(String name) throws IOException {
        createDir();
        boolean flag = false;
        filenameTemp = pathName + name + ".txt";
        ftpFileName = name + ".txt";
        File filename = new File(filenameTemp);
        if (!filename.exists()) {
            filename.createNewFile();
            flag = true;
        }
        return flag;
    }

    /**
     * 写文件
     *
     * @param newStr 新内容
     * @throws IOException
     */
    public static boolean writeTxtFile(String newStr) throws IOException {
        // 先读取原有文件内容，然后进行写入操作
        boolean flag = false;
        String filein = newStr + "\r\n";
        String temp = "";

        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        PrintWriter pw = null;
        try {
            // 文件路径
            File file = new File(filenameTemp);
            // 将文件读入输入流
            fis = new FileInputStream(file);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            StringBuffer buf = new StringBuffer();

            // 保存该文件原有的内容
            for (int j = 1; (temp = br.readLine()) != null; j++) {
                buf = buf.append(temp);
                // System.getProperty("line.separator")
                // 行与行之间的分隔符 相当于“\n”
                buf = buf.append(System.getProperty("line.separator"));
            }
            buf.append(filein);

            fos = new FileOutputStream(file);
            osw = new OutputStreamWriter(fos, "UTF-8");
            pw = new PrintWriter(osw);
            pw.write(buf.toString().toCharArray());
            pw.flush();
            flag = true;
        }
        catch (IOException e1) {
            // TODO 自动生成 catch 块
            throw e1;
        }
        finally {
            if (pw != null) {
                pw.close();
            }
            if (fos != null) {
                fos.close();
            }
            if (br != null) {
                br.close();
            }
            if (isr != null) {
                isr.close();
            }
            if (fis != null) {
                fis.close();
            }
        }
        return flag;
    }

    public static boolean updateTxtFile(Map<String, Object> paramMap) throws IOException {
        // ftp上传目录
        String ftpBasePath = MapUtil.getString(paramMap, "ftpBasePath");
        //上传服务器ip 端口
        String ftpServerInfo = MapUtil.getString(paramMap, "ftpServerInfo");
        byte[] buffer;
        File file = new File(filenameTemp);
        FileInputStream inputStream = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
        byte[] b = new byte[1000];
        int n;
        while ((n = inputStream.read(b)) != -1) {
            bos.write(b, 0, n);
        }
        inputStream.close();
        bos.close();
        buffer = bos.toByteArray();
        InputStream input = new ByteArrayInputStream(buffer);
        FTPUtils ftpUtils = new FTPUtils(ftpServerInfo);
        boolean uploadFlag = ftpUtils.uploadFile(ftpBasePath, ftpFileName, input);
        // 上传成功删除本机文件 需要本机保存备份
        //file.delete();
        if (!uploadFlag) {
            throw new GomException("upload fail");
        }
        return false;
    }


}
