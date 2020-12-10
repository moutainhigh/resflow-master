package com.zres.project.localnet.portal.webservice.res;

import java.util.Map;

/**
 * Created by tang.huili on 2020/2/19.
 */
public interface ResUnsuspendServiceIntf {
    /**
     * 业务实例解挂接口，资源系统收到请求后会解挂补录实例
     *
     * @param param
     * @return Map
     */
    public Map resUnsuspend(Map<String, Object> param);
    /**
     * 根据instance_id查询是否有补录单被挂起，如果有，则解挂，
     * @param params
     * @return
     */
    public Map<String,Object> resUnsuspendSupplement(Map<String, Object> params);
}
