package com.zres.project.localnet.portal.flowdealinfo.service;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * 流程上传、下载文件
 * author sunlb
 */
public interface FileUpdateLocalServiceInf {

    public void uploadLocalFile(PrintWriter writer,
                                String operStaffId,
                                HttpServletRequest request,
                                HttpServletResponse response) throws Exception;


}
