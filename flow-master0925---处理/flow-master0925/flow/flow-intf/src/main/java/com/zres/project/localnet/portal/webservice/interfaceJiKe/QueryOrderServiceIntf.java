package com.zres.project.localnet.portal.webservice.interfaceJiKe;

import java.util.Map;

/**
 * Created by tang.huili on 2018/12/27.
 */
public interface QueryOrderServiceIntf {
    public Map queryOrder(String request);

    /**
     * 全程路由查询接口
     * @param request
     * @return
     */
    public Map queryFullRoute(String request);
}
