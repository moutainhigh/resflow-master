package com.zres.project.localnet.portal.order.service;

import java.util.Map;

public interface AdminInfoServiceInf {
    /**
     * 系统管理员设置
     * @param params
     * @return
     */
    public Map<String, Object> addAdminInfo(Map<String, Object> params);

    public Map<String, Object> deleteAdminInfo(Map<String, Object> params);

    public Map<String, Object> updateAdminInfo(Map<String, Object> params);

    public Map<String, Object> queryAdminInfo(Map<String, Object> params);

    /**
     * 资源释放时长设置
     * @param params
     * @return
     */
    public Map<String, Object> addDisassemble(Map<String, Object> params);

    public Map<String, Object> queryDisassemble(Map<String, Object> params);

    public Map<String, Object> updateDisassemble(Map<String, Object> params);

    public Map<String, Object> deleteDisassemble(Map<String, Object> params);


}
