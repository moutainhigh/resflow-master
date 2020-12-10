package com.zres.project.localnet.portal.cloudNetWork.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @ClassName CloudNetworkInterfaceDao
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/11/20 14:44
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@Repository
public interface CloudNetworkInterfaceDao {
    /**
     * @Description 功能描述:  查询调用云组网接口的请求参数
     * @Param: [params]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wang.gang2
     * @Date: 2020/11/20 14:50
     */
    Map<String,Object> queryCloudNetworkInfo(Map<String, Object> params);
}
