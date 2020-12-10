package com.zres.project.localnet.portal.cloudNetWork.service;

import java.util.Map;

/**
 * @ClassName OrderFeedBackIntf
 * @Description TODO  退单接口
 * @Author wang.g2
 * @Date 2020/11/23 10:43
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */

public interface OrderFeedBackIntf {
    /**
     * @Description 功能描述: 退单
     * @Param: [params]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wang.gang2
     * @Date: 2020/11/23 10:45
     */
    public Map<String, Object> orderFeedBack(Map<String, Object> params);

}
