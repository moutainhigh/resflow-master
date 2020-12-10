package com.zres.project.localnet.portal.app;

import java.util.Map;

/**
 * @ClassName AppGomWoQueryIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/6/4 9:21
 */
public interface AppGomWoQueryIntf {
    /**
     * APP 工单查询
     * @param request
     * @return
     */
    public Map<String,Object> queryWorkOrders(String request);

}
