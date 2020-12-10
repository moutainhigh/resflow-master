package com.zres.project.localnet.portal.app;

import java.util.Map;

/**
 * @ClassName AppTransOrgQueryIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/6/22 11:04
 */
public interface AppTransOrgQueryIntf {
    /**
     * 转办对象信息查询--组织树信息查询
     * @param request
     * @return
     */
    public Map<String,Object> queryTransOrgInfo(String request);
}
