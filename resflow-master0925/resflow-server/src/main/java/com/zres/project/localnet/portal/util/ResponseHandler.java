/**
 * FileName: ResponseHandler
 * Author:   li.he
 * Date:     2018/11/8 0:27
 * Description: 根据请求头获取不同浏览器类型，设置response对象的请求头、输出流的编码方式等
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.zres.project.localnet.portal.util;

import com.ztesoft.res.frame.core.exception.ServiceBuizException;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 〈description〉<br>
 * 〈根据请求头获取不同浏览器类型，设置response对象的响应头、输出流的编码方式等〉
 */
public class ResponseHandler {
    private String responseFile;
    private HttpServletRequest request;
    private HttpServletResponse response;

    public ResponseHandler(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    public String getResponseFile() {
        return responseFile;
    }

    public void setResponseFile(String responseFile) {
        this.responseFile = responseFile;
    }

    public void setResponseFile2007(String responseFile) {
        this.setResponseFile(responseFile);
        this.setResponse();
    }

    public void setResponseFile2003(String responseFile) {
        this.setResponseFile(responseFile);
        this.set2003Response();
    }

    /**
     * 功能描述: <br>
     * 〈根据不同浏览器，设置响应头和输出流编码方式〉
     */
    public void setResponse() {
        responseFile = StringUtils.isEmpty(responseFile) ? "new_file" : responseFile;
        response.setContentType("application/vnd.ms-excel");
        /*避免CRLF注入攻击*/
        Pattern crlf = Pattern.compile("(\r\n|\r|\n|\n\r)");
        Matcher m = crlf.matcher(responseFile);
        if (m.find()) {
            throw new ServiceBuizException("文件名包含非法字符！");
        }
        String contentDisposition;
        try {
            // 火狐
            if (request.getHeader("User-Agent").toLowerCase().indexOf("firefox") > 0) {
                contentDisposition = "attachment; filename=\"" + new String(responseFile.getBytes("UTF-8"), "ISO8859-1") + ".xlsx";
            }
            //IE
            else {
                contentDisposition = "attachment; filename=\"" + URLEncoder.encode(responseFile, "UTF-8") + ".xlsx";
            }
        }
        catch (UnsupportedEncodingException e) {
            throw new ServiceBuizException("文件名编码错误，请联系管理员！", e);
        }
        response.setHeader("Content-Disposition", contentDisposition);
        response.setCharacterEncoding("UTF-8");
    }
    /**
     * 功能描述: <br>
     * 〈根据不同浏览器，设置响应头和输出流编码方式〉
     */
    public void set2003Response() {
        responseFile = StringUtils.isEmpty(responseFile) ? "new_file" : responseFile;
        response.setContentType("application/vnd.ms-excel");
        /*避免CRLF注入攻击*/
        Pattern crlf = Pattern.compile("(\r\n|\r|\n|\n\r)");
        Matcher m = crlf.matcher(responseFile);
        if (m.find()) {
            throw new ServiceBuizException("文件名包含非法字符！");
        }
        String contentDisposition;
        try {
            // 火狐
            if (request.getHeader("User-Agent").toLowerCase().indexOf("firefox") > 0) {
                contentDisposition = "attachment; filename=\"" + new String(responseFile.getBytes("UTF-8"), "ISO8859-1") + ".xls";
            }
            //IE
            else {
                contentDisposition = "attachment; filename=\"" + URLEncoder.encode(responseFile, "UTF-8") + ".xls";
            }
        }
        catch (UnsupportedEncodingException e) {
            throw new ServiceBuizException("文件名编码错误，请联系管理员！", e);
        }
        response.setHeader("Content-Disposition", contentDisposition);
        response.setCharacterEncoding("UTF-8");
    }


    /**
     * 功能描述: <br>
     * 〈获取response输出流对象〉
     */
    public OutputStream getOutputstream() {
        try {
            return response.getOutputStream();
        }
        catch (IOException e) {
            return null;
        }
    }

    /**
     * 功能描述: <br>(2007)
     * 〈导出，供客户端调用〉
     */
    public void export37(Workbook workbook) {
        OutputStream out = null;
        try {
            out = this.getOutputstream();
            workbook.write(out);
        }
        catch (IOException e) {
            throw new ServiceBuizException(e.getMessage(), e);
        }
        finally {
            ResEntityUtil.closeSteam(out);
        }
    }


    public void flushResponse(HttpServletResponse response, String responseContent) {
        PrintWriter writer = null;
        try {
            response.setCharacterEncoding("GBK");
            response.setHeader("Cache-Control", "no-cache");
            response.setContentType("text/html;charset=UTF-8");
            writer = response.getWriter();
            if (com.alibaba.druid.util.StringUtils.isEmpty(responseContent)) {
                writer.write("");
            }
            else {
                writer.write(responseContent);
            }
        }
        catch (IOException var8) {
            throw new RuntimeException(var8);
        }
        finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }

        }

    }



}