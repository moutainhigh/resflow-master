package com.zres.project.localnet.portal.app.sdwan;

import java.util.Map;

/**
 * @ClassName AppInitDeviceModelIntf
 * @Description TODO app 终端信息查询--设备型号初始化
 * @Author tang.huili
 * @Date 2020/6/3 9:34
 */
public interface AppInitDeviceModelIntf {
    /**
     * sdwan终端信息查询--设备型号初始化
     * @param request
     * @return
     */
    public Map<String,Object> initDeviceModel(String request);
}
