package com.zres.project.localnet.portal.app;

import java.util.Map;

/**
 * @ClassName AppCheckOrderQueryIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/6/17 11:29
 */
public interface AppCheckOrderQueryIntf {
    /**
     * app  工单详情-- 核查单信息查询
     * @param request
     * @return
     */
    public Map<String, Object> queryCheckOrderInfo(String request);
}
