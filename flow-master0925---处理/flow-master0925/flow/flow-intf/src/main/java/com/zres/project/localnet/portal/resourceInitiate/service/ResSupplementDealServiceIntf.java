package com.zres.project.localnet.portal.resourceInitiate.service;

import java.util.Map;

/**
 * 资源补录模块
 */
public interface ResSupplementDealServiceIntf {

    /**
     * 启流程
     * @param paramsMap
     * @return
     */
    public Map<String, Object> createOrderResSup(Map<String, Object> paramsMap) throws Exception;

    /**
     * 环节回单方法
     * @param params
     * @return
     * @throws Exception
     */
    public Map<String, Object> submitOrderResSup(Map<String, Object> params) throws Exception;

    /**
     * 第一个环节自动提交
     * @param orderId
     * @return
     */
    public Map<String, Object> firstTacheAutoSubmit(String orderId) throws Exception;

    /**
     * 根据实例ID查询srvOrdId
     * @param instanceId
     * @return
     */
    public String qrySrvOrdIdByInstanceId(String instanceId, String systemresouce);

    /**
     * 资源配置
     * @param param
     * @return
     */
    public Map<String, Object> resConfigSupplement(Map<String, Object> param);


    /**
     * 汇总归档
     * @param param
     * @return
     */
    public Map<String,Object> resArchive(Map<String,Object> param);

    /**
     * 撤销资源补录流程
     * @param param
     */
    public Map<String,Object> cancelOrderResSupplement(Map<String,Object> param);

    /**
     * 正常单来单时，资源补录单的相应操作
     * 1.如果不是拆机单，那么挂起补录单
     * 2.如果是拆机单，那么归档已配置的补录资源，并且撤销补录流程
     * @param param
     * @return
     */
    public Map<String,Object> supplementStop(Map<String,Object> param);

}
