package com.zres.project.localnet.portal.common;

import java.util.Map;

import com.ztesoft.res.frame.flow.common.exception.FlowException;

/**
 * 工单处理的通用方法
 * 这里写了回单接口和异常节点退单接口，后面环节统一的方法也可以抽象成一个接口写这里，比如转派，资源配置等等
 */
public interface CommonWoOrderDealServiceIntf {

    /**
     * 启流程
     *
     * @param
     * @return
     */
    Map<String, Object> createOrderService(Map<String, Object> paramsMap) throws FlowException;

    /**
     * 启子流程
     *
     * @param paramsMap
     * @return
     */
    Map<String, Object> createChildOrderService(Map<String, Object> paramsMap) throws FlowException;

    /**
     * 业务上通用回单接口
     *
     * @return
     */
    Map<String, Object> complateWoService(Map<String, Object> commonMap) throws FlowException;

    /**
     * 通用异常节点调用退单接口
     *
     * @param commonMap
     * @return
     */
    Map<String, Object> rollBackWoService(Map<String, Object> commonMap) throws FlowException;

    /**
     * 启子流程
     * 适配串行发起子流程，如：子流程A->子流程B->子流程C，
     * 子流程通过parentOrderId查询上一环节orderId进行退单
     *
     * @param paramsMap
     * @return
     */
    Map<String, Object> createSerialChildOrderService(Map<String, Object> paramsMap) throws FlowException;

    /**
     * 作废接口
     *
     * @param commonMap
     * @return java.util.Map<java.lang.String   ,       java.lang.Object>
     * @author xsc
     * @date 2020/9/15 19:59
     */
    Map<String, Object> cancelOrderService(Map<String, Object> commonMap) throws FlowException;

}
