package com.zres.project.localnet.portal.webservice.product.sdwan;

import java.util.Map;

/**
 * @ClassName OTSFeedBackServiceIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/3/17 19:26
 */
public interface RollBackServiceIntf {
    /**
     * OSS退单接口
     * @param map
     * @return
     */
    public Map rollBackOTS(Map<String, Object> map);
}
