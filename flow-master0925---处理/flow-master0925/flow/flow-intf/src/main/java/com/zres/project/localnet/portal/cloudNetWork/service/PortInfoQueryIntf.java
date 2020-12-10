package com.zres.project.localnet.portal.cloudNetWork.service;

import java.util.Map;

/**
 * @ClassName PortInfoQueryIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/10/16 10:53
 */
public interface PortInfoQueryIntf {
/**
 * @Description 功能描述: 下联端口查询接口
 * @Param: [params]
 * @Return: java.util.Map<java.lang.String,java.lang.Object>
 * @Author: wang.gang2
 * @Date: 2020/10/16 10:56
 */
    public Map<String, Object> portInfoQuery(Map<String,Object> params);
}
