package com.zres.project.localnet.portal.app;

import java.util.Map;

/**
 * @ClassName AppTacheWoQueryIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/6/17 12:56
 */
public interface AppTacheWoQueryIntf {

    /**
     * app 工单详情 流程跟踪 工单日志信息查询
     * @param request
     * @return
     */
    public Map<String,Object> queryTacheWoInfo(String request);
}
