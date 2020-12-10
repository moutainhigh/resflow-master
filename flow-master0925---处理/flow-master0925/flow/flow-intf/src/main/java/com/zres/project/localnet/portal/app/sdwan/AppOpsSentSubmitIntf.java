package com.zres.project.localnet.portal.app.sdwan;

import java.util.Map;

/**
 * @ClassName AppQueryCircuitIntf
 * @Description TODO app sdwan运维派单 提交
 * @Author tang.huili
 * @Date 2020/6/3 9:34
 */
public interface AppOpsSentSubmitIntf {
    /**
     * sdwan运维派单 提交按钮
     * @param request
     * @return
     */
    public Map<String,Object> opsSentSubmit(String request);
}
