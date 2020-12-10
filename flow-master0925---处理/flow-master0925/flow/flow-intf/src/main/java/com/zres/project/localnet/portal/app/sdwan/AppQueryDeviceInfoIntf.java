package com.zres.project.localnet.portal.app.sdwan;

import java.util.Map;

/**
 * @ClassName AppQueryDeviceInfoIntf
 * @Description TODO app 查询终端信息-终端信息页面回填功能
 * @Author tang.huili
 * @Date 2020/6/3 9:34
 */
public interface AppQueryDeviceInfoIntf {
    /**
     * sdwan终端信息查询--终端信息页面回填功能
     * @param request
     * @return
     */
    public Map<String,Object> queryDeviceInfo(String request);
}
