package com.zres.project.localnet.portal.flowdealinfo.service;

public interface FowordResServiceIntf {

    void modifyResWoState(String orderId, boolean bussSpecialType);

    /**
     * 修改工单状态和时间
     * @param woId
     * @param woState
     * @param isupdateCreateDate
     */
    void updateWoInfo(String woId, String woState, boolean isupdateCreateDate);

    void resSubmitOrder(String orderId,String woId);
}
