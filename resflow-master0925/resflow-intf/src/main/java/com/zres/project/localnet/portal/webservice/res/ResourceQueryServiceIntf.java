package com.zres.project.localnet.portal.webservice.res;

import java.util.List;
import java.util.Map;

/**
 * 资源接口-客户端
 * 机房查询接口
 * Created by csq on 2019/3/5.
 */
public interface ResourceQueryServiceIntf {
    /**
     * 机房查询接口
     * @param map
     * @return List
     */
    public List<Map<String,Object>> resourceQuery(Map map);
}
