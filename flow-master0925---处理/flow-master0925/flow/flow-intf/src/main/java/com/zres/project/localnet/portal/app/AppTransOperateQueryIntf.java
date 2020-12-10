package com.zres.project.localnet.portal.app;

import java.util.Map;

/**
 * @ClassName AppTransOperateQueryIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/6/22 10:58
 */
public interface AppTransOperateQueryIntf {

    /**
     * 转办对象信息查询--默认转办信息查询（人员、部门、岗位）
     * @param request
     * @return
     */
    public Map<String,Object> queryTransOperaInfo(String request);
}
