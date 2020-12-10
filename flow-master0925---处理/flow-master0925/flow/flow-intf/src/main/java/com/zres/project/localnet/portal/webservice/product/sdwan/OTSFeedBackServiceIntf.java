package com.zres.project.localnet.portal.webservice.product.sdwan;

import java.util.Map;

/**
 * @ClassName OTSFeedBackServiceIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/3/17 19:26
 */
public interface OTSFeedBackServiceIntf {
    /**
     * OSS TO SDWAN 工单反馈
     * @param map
     * @return
     */
    public Map feedBackOTS(Map<String,Object> map);
}
