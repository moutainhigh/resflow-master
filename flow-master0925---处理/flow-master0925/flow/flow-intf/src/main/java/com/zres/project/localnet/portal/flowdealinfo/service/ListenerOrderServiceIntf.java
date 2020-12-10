package com.zres.project.localnet.portal.flowdealinfo.service;

import java.util.Map;

public interface ListenerOrderServiceIntf {

    /**
     * 子流程最后一个环节到单处理
     */
    public void childFlowLastTache(Map<String, Object> param);

    /**
     * 跨域全程调测到单的相关处理
     * @param param
     */
    public void crassWholeTest(Map<String, Object> param);

    /**
     * 二干下发单子--起租环节到单相关处理
     * @param param
     */
    public void rentTache(Map<String, Object> param);

    /**
     * 查询二干数据制作子流程是否完全走完
     * @param param
     */
    public boolean qryDataChildFlowSec(Map<String, Object> param);

    /**
     * 回单二干待数据制作与本地调度环节
     * @param param
     */
    public void finshDataAndSchedule(Map<String, Object> param);

    /**
     * 电路调度环节提交工单
     * @param param
     */
    void circuitDispatchCompWoOrder(Map<String, Object> param);

    /**
     * 二干完工汇总到单调用业务汇总接口
     * @param paramsMap
     */
    public void sumCompleteReceiptTache(Map<String, Object> paramsMap);

    /**
     * 回单起止租
     * @param paramsMap
     */
    public void finshStartStopRent(Map<String, Object> paramsMap) throws Exception;

    /**
     * 环节日志
     * @param params
     */
    //public void insertTacheLog(Map<String, Object> params);

    /**
     * 本地客户电路起租止租起止租环节操作
     * @param orderId
     */
    public void startStopRent(String orderId, String woId);

    /**
     * 起租确认通知环节结束本地网流程
     * @param paramsMap
     */
    public void finshRent(Map<String, Object> paramsMap);

    /**
     * 资源补录流程环节到单   各专业资源补录环节和本地调度资源补录环节
     * @param paramsMap
     */
    public void resSupplementToOrder(Map<String, Object> paramsMap);

    /**
     * 商务专线 接入专业资源分配环节自动走单
     * @Param:
     * @Return:
     */
    public void resAllocateAuto(Map<String, Object> paramsMap);


    /**
     * 商务专线 接入专业数据制作环节修改状态为待回单
     * @Param:
     * @Return:
     */
    public void updateWoState(Map<String, Object> paramsMap);

    /**
     * 工单激活派单接口，发给省份IP 智能网管系统
     * @author wangsen
     * @param paramsMap
     */
    public void sendOrder(Map<String, Object> paramsMap);

    /*
     * 数据专业数据制作环节自动提交
     * @Param:
     * @Return:
     */
    public void dataMakeAuto(Map<String, Object> paramsMap);

    /**
     * 互联网专线修改后置资源工单状态
     */
    void modifyResWoState(Map<String, Object> paramsMap);
}
