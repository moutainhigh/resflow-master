package com.zres.project.localnet.portal.cloudNetWork.service;

import java.util.Map;

/**
 * @ClassName WorkStatusQueryIntf
 * @Description TODO 工单状态查询接口
 * @Author wang.g2
 * @Date 2020/11/23 10:20
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */

public interface WorkStatusQueryIntf {
    /**
     * @Description 功能描述: 工单状态查询
     * @Param: [params]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wang.gang2
     * @Date: 2020/11/23 10:23
     */
    public Map<String, Object> workStatusQuery(Map<String,Object> params);
}
