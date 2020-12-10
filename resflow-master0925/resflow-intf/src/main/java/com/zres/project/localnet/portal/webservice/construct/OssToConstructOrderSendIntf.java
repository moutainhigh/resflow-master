package com.zres.project.localnet.portal.webservice.construct;

import java.util.Map;

/**
 * @ClassName OssToConstructOrderSendIntf
 * @Description TODO  下发工建开通单
 * @Author wang.g2
 * @Date 2020/9/19 14:41
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */

public interface OssToConstructOrderSendIntf {

    /**
     * @Description 功能描述:  oss to  工建开通单
     * @Param: [request]
     * @Return: java.util.Map
     * @Author: wang.gang2
     * @Date: 2020/9/19 14:45
     */
    public Map orderSend(Map<String, Object> request);
}
