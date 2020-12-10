package com.zres.project.localnet.portal.webservice.construct;

import java.util.Map;

/**
 * @ClassName ConstructTachePushIntf
 * @Description TODO 工建 TO Oss 环节推送接口
 * @Author wang.g2
 * @Date 2020/12/2 15:20
 */
public interface ConstructTachePushIntf {
    /**
     * @Description 功能描述: 工建 TO Oss 环节推送接口
     * @Param: [params]
     * @Return: java.lang.String
     * @Author: wang.gang2
     * @Date: 2020/12/2 15:21
     */
    public Map<String, Object> pushTache( String request);
}
