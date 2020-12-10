package com.zres.project.localnet.portal.webservice.res;

import java.util.Map;



/**
 * 资源接口-客户端
 * 业务电路汇总接口
 * Created by Skyla on 2018/12/25.
 */
public interface BusinessAutoAssignServiceIntf {
    /**
     * 业务电路汇总接口
     * @param params
     * @return Map
     */
    public Map businessAutoAssign(Map<String, Object> params);
}
