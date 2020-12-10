package com.zres.project.localnet.portal.app;

import java.util.Map;

/**
 * @ClassName AppContactsQueryIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/6/22 16:25
 */
public interface AppContactsQueryIntf {
    /**
     * 常用联系人查询
     * @param request
     * @return
     */
    public Map<String,Object> queryContactsInfo(String request);
}
