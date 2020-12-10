package com.zres.project.localnet.portal.app.sdwan;

import java.util.Map;

/**
 * @ClassName AppSaveDeviceInfoIntf
 * @Description TODO app 终端信息查询--填写终端功能
 * @Author tang.huili
 * @Date 2020/6/3 9:34
 */
public interface AppSaveDeviceInfoIntf {
    /**
     * sdwan终端信息查询--填写终端功能
     * @param request
     * @return
     */
    public Map<String,Object> saveDeviceInfo(String request);
}
