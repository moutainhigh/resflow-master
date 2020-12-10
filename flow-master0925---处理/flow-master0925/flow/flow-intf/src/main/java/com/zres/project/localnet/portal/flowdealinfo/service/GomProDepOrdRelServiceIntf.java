package com.zres.project.localnet.portal.flowdealinfo.service;

import java.util.List;
import java.util.Map;

public interface GomProDepOrdRelServiceIntf {

    /**
     * 保存专业、区域、订单关联表数据
     *
     * @param params orgId 区域id professionId 专业id orderId 订单id subOrderId 子流程id supOrderState 子流程状态
     */
    public void insertGomProDepOrdRel(List<Map<String,Object>> params);

    /**
     * 更新专业、区域、订单关联状态
     * @param params
     */
    public void updateSupOrderState(Map<String,Object> params);

}
