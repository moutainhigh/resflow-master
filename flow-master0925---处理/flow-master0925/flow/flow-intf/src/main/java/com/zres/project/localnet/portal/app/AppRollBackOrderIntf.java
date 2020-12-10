package com.zres.project.localnet.portal.app;

import java.util.Map;

/**
 * @ClassName AppQueryCircuitIntf
 * @Description TODO app  提交
 * @Author tang.huili
 * @Date 2020/6/3 9:34
 */
public interface AppRollBackOrderIntf {
    /**
     *  提交按钮
     * @param request
     * @return
     */
    public Map<String,Object> rollBackOrder(String request);
}
