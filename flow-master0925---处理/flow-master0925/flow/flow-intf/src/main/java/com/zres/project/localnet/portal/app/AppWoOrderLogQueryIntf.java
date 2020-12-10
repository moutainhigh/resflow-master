package com.zres.project.localnet.portal.app;

import java.util.Map;

/**
 * @ClassName AppWoOrderLogQueryIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/6/17 12:38
 */
public interface AppWoOrderLogQueryIntf {
    /**
     * app  工单详情  流程跟踪--工单处理日志信息查询
     * @param request
     * @return
     */
    public Map<String,Object> queryWoOrderLogInfo(String request);
}
