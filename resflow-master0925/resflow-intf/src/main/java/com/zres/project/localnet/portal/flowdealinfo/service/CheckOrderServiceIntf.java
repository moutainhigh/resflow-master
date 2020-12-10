package com.zres.project.localnet.portal.flowdealinfo.service;

import java.util.List;
import java.util.Map;

/**
 * 核查单退单业务逻辑接口
 */

public interface CheckOrderServiceIntf {

    /**
     * 专业核查环节退单核查调度
     * @param backOrderParam
     * @return
     */
    Map<String, Object> specialCheckBackOrder(Map<String, Object> backOrderParam);

    /**
     * 核查汇总环节退单
     * @param backOrderParam
     * @return
     */
    Map<String, Object> checkTotalBackOrder(Map<String, Object> backOrderParam);

    /**
     * 作废所有的专业核查的工单
     * @param invalidParam
     * @return
     */
    void invalidAllSpecialCheck(Map<String, Object> invalidParam) throws Exception;

    /**
     * 作废本地核查流程
     * @param invalidParam
     * @throws Exception
     */
    void invalidLocalCheck(Map<String, Object> invalidParam) throws Exception;

    /**
     * 查询所有专业核查的工单
     * @param orderId
     * @return
     */
    List<String> qryAllSpecialCheckWoOrder(String orderId);

    /**
     * 查询最大的工单id
     * @param orderId
     * @return
     */
    String qryMaxWoId(String orderId);

    /**
     * 查询专业核查
     * @param orderId
     * @return
     */
    Map<String, Object> qrySpecialData(String orderId);

    /**
     * 查询下发到本地的核查区域
     * @param orderId
     * @return
     */
    Map<String, Object> qryToLocalCheckData(String orderId);

    /**
     * 根据电路ID查询系统源信息
     * @param param
     * @return
     */
    Map<String, Object> querySystemInfo(Map<String, Object> param);

}
