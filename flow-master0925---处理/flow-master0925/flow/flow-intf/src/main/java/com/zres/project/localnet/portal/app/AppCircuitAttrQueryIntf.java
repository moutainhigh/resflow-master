package com.zres.project.localnet.portal.app;

import java.util.Map;

/**
 * @ClassName AppCircuitAttrQueryIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/6/15 15:44
 */
public interface AppCircuitAttrQueryIntf {

    /**
     * app 工单详情--电路属性信息
     * @param request
     * @return
     */
    public Map<String, Object> queryCircuitAttrInfo(String request);
}
