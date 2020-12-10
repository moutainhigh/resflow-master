package com.zres.project.localnet.portal.app;

import java.util.Map;

/**
 * @ClassName AppStandbyIntf
 * @Description TODO app 待办
 * @Author tang.huili
 * @Date 2020/6/3 9:34
 */
public interface AppGetTacheButtonIntf {
    /**
     * 查询当前环节工单的按钮
     * @param request
     * @return
     */
    public Map<String,Object> getTacheButton(String request);
}
