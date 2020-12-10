package com.zres.project.localnet.portal.webservice.product.sdwan;

import java.util.Map;

/**
 * @ClassName STOFeedBackServiceIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/3/17 19:29
 */
public interface STOFeedBackServiceIntf {
    /**
     * SDWAN TO OSS 工单反馈
     * @param request
     * @return
     */
    public Map feedBackSTO(String request);
}
