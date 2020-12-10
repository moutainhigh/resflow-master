package com.zres.project.localnet.portal.webservice.res;

import java.util.Map;

/**
 * Created by tang.huili on 2020/2/19.
 */
public interface ResSuspendServiceIntf {
    /**
     * 业务实例挂起接口，资源系统收到请求后会挂起补录实例。
     *
     * @param param
     * @return Map
     */
    public Map resSuspend(Map<String, Object> param);
    /**
     * 根据instance_id查询是否有补录单，如果有补录单，则挂起，
     * @param params
     * @return
     */
    public Map<String,Object> resSuspendSupplement(Map<String, Object> params);
}
