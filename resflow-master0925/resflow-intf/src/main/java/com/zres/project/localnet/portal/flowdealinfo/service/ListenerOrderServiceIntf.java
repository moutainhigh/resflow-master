package com.zres.project.localnet.portal.flowdealinfo.service;

import java.util.Map;

public interface ListenerOrderServiceIntf {

    /**
     * 启二干资源分配子流程
     * @param paramsMap
     */
    public Map<String, Object> startResConfig(Map<String, Object> paramsMap);

    /**
     * 启数据制作子流程
     * @param paramsMap
     */
    public Map<String, Object> startData(Map<String, Object> paramsMap);

    /**
     * 启资源施工子流程
     * @param paramsMap
     */
    public Map<String, Object> startResConstruction(Map<String, Object> paramsMap);

    /**
     * 启本地调度流程--下发本地网
     * @param paramsMap
     */
    public Map<String, Object> startSchedule(Map<String, Object> paramsMap);

    /**
     * 起租确认通知环节结束本地网流程
     * @param paramsMap
     */
    public void finshRent(Map<String, Object> paramsMap);

    /**
     * 二干资源分配和数据制作子流程最后一个环节到单处理
     * @param paramsMap
     */
    public void childFlowLastTache(Map<String, Object> paramsMap) ;

    /**
     * 查询是否下发本地网
     * @param orderId
     * @return
     */
    public Map<String, Object> qryIfToLocal(String orderId);

    /**
     * 回单父流程环节
     */
    public void submitParentFlowTache(Map<String, Object> paramsMap);

    /**
     * 二干完工汇总到单调用业务汇总接口
     * @param paramsMap
     */
    public void sumCompleteReceiptTache(Map<String, Object> paramsMap);

    /**
     * 资源补录流程环节到单   各专业资源补录环节和本地调度资源补录环节
     * @param paramsMap
     */
    public void resSupplementToOrder(Map<String, Object> paramsMap);

    /**
     * 二干下发本地时，发送短信
     * @param orderId
     * @param areaId
     */
    void secToLocalMsg(String orderId, String areaId);

}
