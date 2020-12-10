package com.zres.project.localnet.portal.app;

import java.util.Map;

/**
 * @ClassName AppDispatchQueryIntf
 * @Description TODO app 调单信息查询
 * @Author wang.g2
 * @Date 2020/6/8 18:34
 */
public interface AppDispatchQueryIntf {

    /**
     * 调单查询
     * @param request
     * @return
     */
    public Map<String, Object> queryDispatchOrderInfo(String request);


}
