package com.zres.project.localnet.portal.cloudNetWork.service;

import java.util.Map;

/**
 * @ClassName IpranManagerVerifyIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/10/16 11:00
 */
public interface IpranManagerVerifyIntf {
    /**
     * @Description 功能描述: IPRAN资源网管验证
     * @Param: [params]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wang.gang2
     * @Date: 2020/10/16 11:03
     */
    public Map<String, Object> ipranManagerVerify(Map<String, Object> params);
}
