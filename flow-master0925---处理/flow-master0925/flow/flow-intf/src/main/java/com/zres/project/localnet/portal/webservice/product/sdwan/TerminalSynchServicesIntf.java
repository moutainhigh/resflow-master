package com.zres.project.localnet.portal.webservice.product.sdwan;

import java.util.Map;

/**
 * @ClassName TerminalSynchServicesIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/3/19 14:47
 */
public interface TerminalSynchServicesIntf {
    /**
     * oss To sd_wan 终端同步
     * @param params
     * @return
     */
    public Map terminalSynchronization(Map<String,Object> params);
}
