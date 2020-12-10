package com.zres.project.localnet.portal.cloudNetWork.service;

import java.util.Map;

/**
 * @Description 功能描述: 云组网反馈接口
 * @Param:
 * @Return:
 * @Author: wang.gang2
 * @Date: 2020/11/25 15:24
 */
public interface CloudNetworkFinishOrderIntf {
    /**
     * @Description 功能描述: 核查反馈/全程业务报竣
     * @Param: [map]
     * @Return: java.util.Map
     * @Author: wang.gang2
     * @Date: 2020/11/25 15:24
     */
    public Map finishOrder(Map<String, Object> map);
}
