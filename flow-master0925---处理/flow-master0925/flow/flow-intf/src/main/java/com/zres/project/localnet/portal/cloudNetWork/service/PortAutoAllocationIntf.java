package com.zres.project.localnet.portal.cloudNetWork.service;

import java.util.Map;

/**
 * @ClassName PortAutoAllocationIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/10/16 11:20
 */
public interface PortAutoAllocationIntf {
    /**
     * @Description 功能描述: portAutoAllocation
     * @Param: [params]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wang.gang2
     * @Date: 2020/10/16 11:21
     */
    Map<String,Object> portAutoAllocation(Map<String, Object> params);
}
