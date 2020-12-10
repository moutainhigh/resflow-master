package com.zres.project.localnet.portal.webservice.res;

import java.util.Map;



/**
 * 资源接口-客户端
 * 业务实例回滚接口
 * Created by Skyla on 2018/12/25.
 */
public interface BusinessRollbackServiceIntf {
    /**
     * 业务实例回滚接口
     *
     * @param param
     * @return Map
     */
    public Map businessRollback(Map<String,Object> param);

    /**
     * 调用资源业务实例回滚接口的业务逻辑
     * @param params
     * @return
     */
    public Map resRollBack(Map<String,Object> params);
}
