package com.zres.project.localnet.portal.app.sdwan;

import java.util.Map;

/**
 * @ClassName AppInitDeviceLevelIntf
 * @Description TODO app 终端信息查询--设备标准初始化
 * @Author tang.huili
 * @Date 2020/6/3 9:34
 */
public interface AppInitDeviceLevelIntf {
    /**
     * sdwan终端信息查询--设备标准初始化
     * @param request
     * @return
     */
    public Map<String,Object> initDeviceLevel(String request);
}
