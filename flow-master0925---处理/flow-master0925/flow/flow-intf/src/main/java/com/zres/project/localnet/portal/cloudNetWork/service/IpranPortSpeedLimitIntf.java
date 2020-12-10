package com.zres.project.localnet.portal.cloudNetWork.service;

import java.util.Map;

/**
 * @ClassName IpranPortSpeedLimitIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/11/20 18:06
 */
public interface IpranPortSpeedLimitIntf {
    /**
     * @Description 功能描述: -2.11【暂缓】IPRAN端口限速下发通知接口
     * @Param: []
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wang.gang2
     * @Date: 2020/11/20 18:07
     */
    public Map<String, Object> portSpeedLimit(Map<String,Object> params);
}
