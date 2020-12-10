package com.zres.project.localnet.portal.resourceInitiate.service;

import java.util.List;
import java.util.Map;

public interface ResourceInitiateServiceIntf {
    /**
     * 调用资源接口查询电路信息
     * @param param
     * @return
     */
    Map<String, Object> queyCircuitInfoFromResource(Map<String, Object> param);

    /**
     * 资源补录页面初始化电路信息
     * @param param
     * @return
     */
    Map<String, Object> initCircuitInfo(Map<String, Object> param);

    /**
     * 初始化部门树
     * @return
     */
    Map<String, Object> queryDeptInfo();

    /**
     * 初始化专业下拉多选框
     * @return
     */
    Map<String, Object> initSpecialtyInfo(Map<String, Object> param);

    /**
     * 资源补录起流程入库
     * @param param
     * @return
     */
    void startFlow(Map<String, Object> param) throws Exception;

    /**
     * 二干下发的资源补录发起流程
     * @param param
     * @throws Exception
     */
    void startFlowFromSec(Map<String, Object> param) throws Exception;

    /**
     * 根据产品实例号查询调度侧所有关于该电路的信息
     * @param param
     * @return
     */
    Map<String, Object> querySrvOrdInfoByInstanceId(Map<String, Object> param);

    /**
     * 根据产品实例ID查询电路是否有未完成的资源补录单
     * @param param
     * @return
     */
    Map<String, Object> queryResourceInitiateInfoByInstanceId(Map<String, Object> param);

    /**
     * 查询二干补录资源信息
     * @param srvOrdId (资源补录表主键id)
     * @return
     */
    List<Map<String,Object>> queryResourceOrderInfoSec(String srvOrdId);
    /**
     * 查询本地补录资源信息
     * @param srvOrdId (资源补录表主键id)
     * @return
     */
    List<Map<String,Object>> queryResourceOrderInfoLocal(String srvOrdId);

}
