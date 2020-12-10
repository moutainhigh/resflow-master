package com.zres.project.localnet.portal.app;

import java.util.Map;

/**
 * @ClassName AppChangeOrderQueryInf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/6/16 18:22
 */
public interface AppChangeOrderQueryInf {
    /**
     * app 工单详情---异常单信息
     * @param request
     * @return
     */
    public Map<String, Object>  queryChangeOrderInfo(String request);

}
