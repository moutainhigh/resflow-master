package com.zres.project.localnet.portal.app;

import java.util.Map;

/**
 * @ClassName AppAttachQueryIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/6/23 16:06
 */
public interface AppAttachQueryIntf {
    /**
     * 工单详情--电路信息 --附件信息
     * @param request
     * @return
     */
    public Map<String,Object> queryAttachInfo(String request);
}
