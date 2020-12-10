package com.zres.project.localnet.portal.app.sdwan;

import java.util.Map;

/**
 * @ClassName AppInitDeviceFactoryIntf
 * @Description TODO app 终端信息查询--设备厂商初始化
 * @Author tang.huili
 * @Date 2020/6/3 9:34
 */
public interface AppInitDeviceFactoryIntf {
    /**
     * sdwan终端信息查询--设备厂商初始化
     * @param request
     * @return
     */
    public Map<String,Object> initDeviceFactory(String request);
}
