package com.zres.project.localnet.portal.cloudNetWork.service;

import java.util.Map;

/**
 * @ClassName TerminalNumReoprtIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/10/16 11:13
 */
public interface TerminalNumReoprtIntf {
    /**
     * @Description 功能描述: 终端盒序列号上报接口
     * @Param: [map]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wang.gang2
     * @Date: 2020/10/16 11:14
     */
    Map<String,Object> terminalNumReport(Map<String, Object> map);
}
