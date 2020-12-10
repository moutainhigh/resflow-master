package com.zres.project.localnet.portal.app;

import java.util.Map;

/**
 * @ClassName AppCcOrderConfirmIntf
 * @Description TODO app  抄送工单确认功能
 * @Author tang.huili
 * @Date 2020/6/3 9:34
 */
public interface AppCcOrderConfirmIntf {
    /**
     *  抄送工单确认功能
     * @param request
     * @return
     */
    public Map<String,Object> ccOrderConfirm(String request);
}
