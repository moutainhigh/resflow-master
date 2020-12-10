package com.zres.project.localnet.portal.app;

import java.util.Map;

/**
 * @ClassName AppIndexPageIntf
 * @Description TODO app 首页单据量信息展示查询接口
 * @Author wang.g2
 * @Date 2020/6/5 10:05
 */
public interface AppIndexPageIntf {
    /**
     * 首页单据量信息展示查询接口
     * @param request
     * @return
     */
    public Map<String, Object> queryMyOrderInfo(String request);


}
