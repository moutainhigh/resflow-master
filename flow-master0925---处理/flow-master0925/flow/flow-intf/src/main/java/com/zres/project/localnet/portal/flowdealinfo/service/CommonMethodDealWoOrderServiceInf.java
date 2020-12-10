package com.zres.project.localnet.portal.flowdealinfo.service;

import java.util.Map;

/**
 * 工单处理的通用方法
 * 这里写了回单接口和异常节点退单接口，后面环节统一的方法也可以抽象成一个接口写这里，比如转派，资源配置等等
 */
public interface CommonMethodDealWoOrderServiceInf {

    /**
     * 启流程
     * @param
     * @return
     */
    public Map<String, Object> commonCreateOrder(Map<String, Object> paramsMap) throws Exception;

    /**
     * 启子流程
     * @param paramsMap
     * @return
     */
    public Map<String, Object> commonCreateChildOrder(Map<String, Object> paramsMap) throws Exception;

    /**
     * 业务上通用回单接口
     * @return
     */
    public Map<String, Object> commonComplateWo(Map<String, Object> commonMap) throws Exception;

    /**
     * 通用异常节点调用退单接口
     * @param commonMap
     * @return
     */
    public Map<String, Object> commonRollBackWo(Map<String, Object> commonMap);

}
