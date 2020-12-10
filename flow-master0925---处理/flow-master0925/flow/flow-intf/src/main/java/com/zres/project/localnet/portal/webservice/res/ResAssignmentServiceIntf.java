package com.zres.project.localnet.portal.webservice.res;

import java.util.Map;

/**
 * 资源接口-客户端
 * 资源配置页面接口
 * Created by Skyla on 2018/12/25.
 */
public interface ResAssignmentServiceIntf {
    /**
     * 资源配置页面接口
     * @param params
     * @return map
     */
    public Map resAssignment(Map<String,Object> params);
}
