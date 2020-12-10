package com.zres.project.localnet.portal.app;

import java.util.Map;

/**
 * @ClassName AppGetFreeWoOrderIntf
 * @Description TODO app 环节处理 通用按钮--转办按钮
 * @Author tang.huili
 * @Date 2020/6/3 9:34
 */
public interface AppTransferWoOrderIntf {
    /**
     * 环节处理 通用按钮--转办按钮
     * @param request
     * @return
     */
    public Map<String,Object> transferWoOrder(String request);
}
