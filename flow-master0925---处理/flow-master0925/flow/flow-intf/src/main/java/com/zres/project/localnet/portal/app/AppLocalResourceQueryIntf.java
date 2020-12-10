package com.zres.project.localnet.portal.app;

import java.util.Map;

/**
 * @ClassName AppLocalResourceQueryIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/6/16 18:38
 */
public interface AppLocalResourceQueryIntf {
    /**
     * app 工单待办详情---本地资源信息tab
     * @return
     */
    public Map<String, Object> queryLocalResourceInfo(String request);

}
