package com.zres.project.localnet.portal.cloudNetWork.service;

import java.util.Map;

/**
 * @ClassName ServiceConfigNotifiIntf
 * @Description TODO 2.12	业务配置下发通知接口
 * @Author wang.g2
 * @Date 2020/11/22 9:55
 */
public interface ServiceConfigNotifiIntf {
    /**
     * @Description 功能描述: 业务配置下发通知
     * @Param: [params]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wang.gang2
     * @Date: 2020/11/22 9:56
     */
    public Map<String, Object> serviceConfigNotifi(Map<String,Object> params);
}
