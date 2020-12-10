package com.zres.project.localnet.portal.cloudNetWork.service;

import java.util.Map;

/**
 * @ClassName IpranRoutConfigIhtf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/10/27 16:59
 */
public interface IpranRoutConfigIntf {
    /**
     * @Description 功能描述: IPRAN业务路由配置下发通知接口
     * @Param: [params]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wang.gang2
     * @Date: 2020/10/27 17:01
     */
    public Map<String, Object> routConfigNotifi(Map<String, Object> params);
}
