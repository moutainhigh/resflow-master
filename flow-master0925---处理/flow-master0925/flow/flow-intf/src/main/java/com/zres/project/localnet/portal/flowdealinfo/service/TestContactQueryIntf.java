package com.zres.project.localnet.portal.flowdealinfo.service;

import java.util.Map;

/**
 * @ClassName TestContactQueryIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/11/17 17:14
 */
public interface TestContactQueryIntf {
    /**
     * @Description 功能描述: 查询各地市测试联系人
     * @Param: [params]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wang.gang2
     * @Date: 2020/11/17 17:16
     */
    public Map<String, Object> queryTestContact(Map<String, Object> params);
}
