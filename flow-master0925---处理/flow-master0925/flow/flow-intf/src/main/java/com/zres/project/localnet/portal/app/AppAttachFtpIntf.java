package com.zres.project.localnet.portal.app;

import java.util.Map;

/**
 * @ClassName AppAttachFtpIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/7/9 15:04
 */
public interface AppAttachFtpIntf {
    /**
     * app 附件信息
     * @param request
     * @return
     */
    public Map<String,Object> queryAttacheFtpInfo(String request);
}
