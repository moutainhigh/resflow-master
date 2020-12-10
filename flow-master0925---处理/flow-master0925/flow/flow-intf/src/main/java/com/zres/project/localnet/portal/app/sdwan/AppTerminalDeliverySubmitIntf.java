package com.zres.project.localnet.portal.app.sdwan;

import java.util.Map;

/**
 * @ClassName AppTerminalDeliverySubmitIntf
 * @Description TODO app sdwan 终端出库及上门环节提交功能
 * @Author tang.huili
 * @Date 2020/6/3 9:34
 */
public interface AppTerminalDeliverySubmitIntf {
    /**
     * sdwan终端出库及上门环节提交功能
     * @param request
     * @return
     */
    public Map<String,Object> terminalDeliverySubmit(String request);
}
