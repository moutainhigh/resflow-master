package com.zres.project.localnet.portal.app.sdwan;

import java.util.Map;

/**
 * @ClassName AppGoBackIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/7/8 9:04
 */
public interface AppGoBackIntf {
    /**
     * sdwan  工单回退功能
     * @param request
     * @return
     */
    public Map<String,Object> goBack(String request);
}
