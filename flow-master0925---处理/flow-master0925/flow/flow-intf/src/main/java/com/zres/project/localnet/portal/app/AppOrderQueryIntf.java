package com.zres.project.localnet.portal.app;

import java.util.Map;

/**
 * @ClassName AppOrderQueryIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/6/10 11:17
 */
public interface AppOrderQueryIntf {

    /**
     * app 订单查询
     * @param request
     * @return
     */
    public Map<String, Object> queryOrderDeatilsInfo(String request);

}
