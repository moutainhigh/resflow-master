package com.zres.project.localnet.portal.cloudNetWork.service;

import java.util.Map;

/**
 * @ClassName IpranCollectionIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/10/16 10:56
 */
public interface IpranCollectionIntf {
    /**
     * @Description 功能描述: IPRAN资源网管采集接口
     * @Param: [params]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wang.gang2
     * @Date: 2020/10/16 10:57
     */
    public Map<String, Object> ipranCollection(Map<String,Object> params);
}
