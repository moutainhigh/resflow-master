package com.zres.project.localnet.portal.cloudNetworkFlow;

import java.util.Map;

public interface CloudListenerOrderServiceIntf {

    /**
     * 环节到单处理
     * @param toOrderMap
     */
    void tacheToWoOrder(Map<String, Object> toOrderMap);
}
