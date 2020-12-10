package com.zres.project.localnet.portal.order.service;

import java.util.List;
import java.util.Map;

public interface AdminInfoServiceInf {

    public Map<String, Object> addAdminInfo(Map<String, Object> params);

    public Map<String, Object> deleteAdminInfo(Map<String, Object> params);

    public Map<String, Object> updateAdminInfo(Map<String, Object> params);

    public Map<String, Object> queryAdminInfo(Map<String, Object> params);

    /**
     *  管理原配置资源预占自动释放时间
     * @param params
     * @return
     */
    public Map<String, Object> addDisassemble(Map<String, Object> params);

    public Map<String, Object> queryDisassemble(Map<String, Object> params);

    public Map<String, Object> updateDisassemble(Map<String, Object> params);

    public Map<String, Object> deleteDisassemble(Map<String, Object> params);


}
