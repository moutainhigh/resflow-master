package com.zres.project.localnet.portal.webservice.res;

import java.util.List;
import java.util.Map;

/**
 * 资源接口-客户端
 * 资源查询接口
 * Created by Skyla on 2018/12/25.
 */
public interface BusinessQueryServiceIntf {
    /**
     * 资源查询接口
     * @param map
     * @return List
     */
    public Map<String,Object> businessQuery(Map map);

    Map<String, Object> queryRouteInfo(Map<String, Object> param);

    Map<String, Object> businessQueryCust(Map<String, Object> param);
}
