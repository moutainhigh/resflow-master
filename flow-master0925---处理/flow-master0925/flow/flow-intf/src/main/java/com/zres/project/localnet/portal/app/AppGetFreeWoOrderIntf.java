package com.zres.project.localnet.portal.app;

import java.util.Map;

/**
 * @ClassName AppGetFreeWoOrderIntf
 * @Description TODO app 环节处理 通用按钮--签收/释放签收按钮
 * @Author tang.huili
 * @Date 2020/6/3 9:34
 */
public interface AppGetFreeWoOrderIntf {
    /**
     * 环节处理 通用按钮--签收/释放签收按钮
     * @param request
     * @return
     */
    public Map<String,Object> getFreeWoOrder(String request);
}
