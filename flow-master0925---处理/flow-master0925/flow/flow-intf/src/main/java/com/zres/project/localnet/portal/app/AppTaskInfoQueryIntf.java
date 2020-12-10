package com.zres.project.localnet.portal.app;

import java.util.Map;

/**
 * @ClassName AppTaskInfoQueryIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/6/17 11:43
 */
public interface AppTaskInfoQueryIntf {
    /**
     * app 工单待办 -- 二干本地 任务列表查询
     * @param request
     * @return
     */
    public Map<String, Object> queryTaskInfo(String request);
}
