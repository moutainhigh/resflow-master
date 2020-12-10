package com.zres.project.localnet.portal.app;

import java.util.Map;

/**
 * @ClassName AppCcWoOrderIntf
 * @Description TODO app 环节处理 通用按钮--抄送按钮
 * @Author tang.huili
 * @Date 2020/6/3 9:34
 */
public interface AppCcWoOrderIntf {
    /**
     * 环节处理 通用按钮--抄送按钮
     * @param request
     * @return
     */
    public Map<String,Object> ccWoOrder(String request);
}
