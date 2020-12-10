package com.zres.project.localnet.portal.cloudNetworkFlow;

import java.util.Map;

public interface CloudNetWorkResCheckServiceIntf {
    /**
     * 云组网核查单查询电路信息
     * @param param
     * @return
     */
    Map<String, Object> qrycircuitInfo(Map<String, Object> param);

    /**
     * 云组网核查单查询配置的专业区域信息
     * @param param
     * @return
     */
    Map<String, Object> querySpecialAreaInfo(Map<String, Object> param);

    /**
     * 云组网核查单保存专业区域信息
     * @param param
     * @return
     */
    Map<String, Object> saveSpecialArea(Map<String, Object> param);

    /**
     * 查询子工单
     * @param orderId
     * @return
     */
    int queryCheckChildWoOrder(String orderId);

    /**
     * 根据子流程的orderId查询主流程的woId
     * @param orderId
     * @return
     */
    String queryMainWoId(String orderId);

    /**
     * 云组网核查流程子流程等待环节回单
     * @param orderId
     * @throws Exception
     */
    void childFlowWaitTacheComplateWo(String orderId) throws Exception;

    /**
     * 核查信息保存
     * @param param
     * @return
     */
    Map<String, Object> saveCheckInfo(Map<String, Object> param);

    /**
     * 查询核查信息
     * @param param
     * @return
     */
    Map<String, Object> queryCheckInfo(Map<String, Object> param);

}
