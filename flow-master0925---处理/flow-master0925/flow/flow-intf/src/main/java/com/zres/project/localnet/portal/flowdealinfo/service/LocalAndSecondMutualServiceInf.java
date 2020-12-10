package com.zres.project.localnet.portal.flowdealinfo.service;

import java.util.Map;

public interface LocalAndSecondMutualServiceInf {

    /**
     * 二干下发电路，本地退单二干；
     * @param backOrderParams
     * @return
     */
    public Map<String, Object> localBackOrderToSecond(Map<String, Object> backOrderParams);

    /**
     * 跨域全程调测退单
     * @param params
     * @return
     */
    public Map<String, Object> crossTestRollBack(Map<String, Object> params);
}
