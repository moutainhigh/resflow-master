package com.zres.project.localnet.portal.app.sdwan;

import java.util.Map;

/**
 * @ClassName AppQueryCircuitIntf
 * @Description TODO app sdwan运维派单 提交页面电路信息查询
 * @Author tang.huili
 * @Date 2020/6/3 9:34
 */
public interface AppQueryCircuitIntf {
    /**
     * sdwan运维派单 提交页面电路信息查询
     * @param request
     * @return
     */
    public Map<String,Object> queryCircuit(String request);
}
