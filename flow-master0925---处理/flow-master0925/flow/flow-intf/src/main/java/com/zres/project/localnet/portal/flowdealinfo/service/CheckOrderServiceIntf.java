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
     * 退单二干本地网核查
     * @param orderId
     * @throws Exception
     */
    void rollBackSecondCheck(String orderId) throws Exception;

    /**
     * 查询专业核查环节某个区域有没有执行中的工单
     * @param orderId
     * @param tacheCode
     * @param areaId
     * @return
     */
    int qrySpecialtyCheckDoing(String orderId, String tacheCode, String areaId);

    /**
     * 查询单据归属省分
     * @param param
     * @return
     */
    Map<String, Object> queryProvinceName(Map<String, Object> param);

    /**
     * 查询电路调度环节，电路编号必填的省份配置
     * @return
     */
    Map<String, Object> queryProvinceConf(Map<String, Object> param);

    /**
     * 编辑电路编号后，保存电路编号
     * @param param
     * @return
     */
    Map<String, Object> saveCircuitCodeBySrvOrdId(Map<String, Object> param);

    /**
     * 查询省份DIA
     * @return
     */
    Map<String, Object> queryProvinceAutoConf(Map<String, Object> param);

}
