package com.zres.project.localnet.portal.cloudNetWork.service;

import java.util.Map;

/**
 * @ClassName PortSelectionIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/10/26 20:02
 */
public interface PortSelectionIntf {
    /**
     * @Description 功能描述: 下联端口选择接口
     * @Param: [params]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wang.gang2
     * @Date: 2020/10/26 20:03
     */
    public Map<String, Object> portSelection(Map<String,Object> params);
}
