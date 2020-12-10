package com.zres.project.localnet.portal.flowdealinfo.service;

import java.util.Map;

public interface DataMakeTacheOperServiceInf {

    /**
     * 二干下发电路，本地退单二干；
     * @param backOrderParams
     * @return
     */
    public Map<String, Object> dataMakeTacheBackOrder(Map<String, Object> backOrderParams);

    /**
     * 查询是否有二干资源分配以及二干资源分配专业
     * @param qryParams
     * @return
     */
    public Map<String, Object> qryIfHasSourceDispatch(Map<String, Object> qryParams);

}
