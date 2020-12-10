package com.zres.project.localnet.portal.cloudNetWork.service;

import java.util.Map;

/**
 * @ClassName TerminalBoxOfflineIntf
 * @Description TODO 终端盒下线通知接口
 * @Author wang.g2
 * @Date 2020/11/22 10:12
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */

public interface TerminalBoxOfflineIntf {
    /**
     * @Description 功能描述: 终端盒下线通知
     * @Param: [params]
     * @Return: Map<String,Object>
     * @Author: wang.gang2
     * @Date: 2020/11/23 10:14
     */
    public Map<String, Object> terminalBoxOffline(Map<String, Object> params);

}
