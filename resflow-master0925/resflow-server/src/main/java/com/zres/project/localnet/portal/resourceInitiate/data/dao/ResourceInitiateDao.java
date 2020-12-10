package com.zres.project.localnet.portal.resourceInitiate.data.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ResourceInitiateDao {

    /**
     * 查询环节数据
     * @param tacheId
     * @return
     */
    Map<String, Object> qryTacheInfo(String tacheId);

    /**
     * 查询资源补录配置的数据
     * @param orderId
     * @return
     */
    Map<String, Object> qryResSupplementConfigData(String orderId);

    /**
     * 查询流程第一个环节的工单数据
     * @param orderId
     * @param tacheId
     * @return
     */
    Map<String, Object> qryFirstTacheData(@Param("orderId")String orderId, @Param("tacheId")String tacheId);

    /**
     * 二干下发本地资源补录入库
     * @param params
     * @return
     */
    int insertResSupToLocal(Map<String, Object> params);

    /**
     * 二干下发本地资源补录--更新instanceId 和 versionId
     * @param instanceId
     * @param versionId
     * @param rsiId
     * @return
     */
    int updateResSupToLocal(@Param("instanceId") String instanceId,
                            @Param("versionId") String versionId,
                            @Param("rsiId") String rsiId);

    /**
     * 查询子流程是否存在，以及子流程订单状态
     * @param orderId
     * @param startFlag
     * @param childConfig
     * @param subName
     * @return
     */
    String qryChildOrderState(@Param("orderId") String orderId,
                              @Param("startFlag") String startFlag,
                              @Param("childConfig") String childConfig,
                              @Param("subName") String subName);

    /**
     * 查询是否含有其他执行中的子流程
     * @param params
     * @return
     */
    int qryIfHasChildOrder(Map<String, Object> params);

    /**
     * 查询父订单等待子流程执行环节工单
     * @param params
     * @return
     */
    Map<String, Object> qryParentOrderWoId(Map<String, Object> params);

    /**
     * 查询区域名称
     * @param childConfig
     * @return
     */
    String qryAreaName(String childConfig);

    /**
     * 查询专业名称
     * @param childConfig
     * @return
     */
    String qrySpecialtyName(String childConfig);

    /**
     * 根据实例ID查询srvOrdId
     * @param instanceId
     * @return
     */
    String qrySrvOrdIdByInstanceId(String instanceId);

    /**
     * 修改资源补录订单状态
     * @param orderId
     * @return
     */
    int updateResSupOrderState(String orderId);

    List<Map<String, Object>> initCircuitInfo(Map<String, Object> param);

    Map<String, Object> getOrgInfoByStaffId(String staffId);

    List<Map<String, Object>> queryDeptInfo(String orgId);

    List<Map<String, Object>> initSpecialtyInfo(Map<String, Object> param);

    int insertResourceInitiateInfo(Map<String, Object> params);

    /**
     * 查询资源补录流程规格
     * @return
     */
    String queryFlowId();

    int updateOrderInfoByResourceSupplementId(@Param("params") Map<String, Object> param);

    List<Map<String, Object>> querySrvOrdInfoByInstanceId(Map<String, Object> param);

    List<Map<String, Object>> queryResourceInitiateInfoByInstanceId(Map<String, Object> param);

    /**
     * 查询补录信息
     * @param orderId
     * @return
     */
    Map<String, Object> qrySuppleInfoByChildOrderId(@Param("orderId") String orderId);

    /**
     * 根据工单id查询补录信息
     * @param woId
     * @return
     */
    Map<String, Object> qrySuppleInfoByWoId(@Param("woId") String woId);

    /**
     * 根据id查询补录信息
     * @param id
     * @return
     */
    Map<String, Object> qrySuppleInfoById(@Param("id") String id);
    /**
     * 根据instace_id查询是否有未完成的补录单
     * @param instanceId
     * @return
     */
    List<Map<String, Object>> queryResSuppleByInstanceId(@Param("instanceId") String instanceId,@Param("orderState") String orderState);

    /**
     * 修改资源补录订单状态
     * @param id
     * @return
     */
    void updateResSupOrderStateById(@Param("id") String id,@Param("orderState") String orderState);

    Map<String,Object> qrySuppleStateByOrderId(@Param("orderId") String orderId);

    String qryVersionId(@Param("id") String id);

    /**
     * 根据versionId查询二干资源补录信息
     * @param paramsMap
     * @return
     */
    List<Map<String,Object>> queryResourceOrderInfoSec(Map<String, Object> paramsMap);
    /**
     * 根据versionId查询本地资源补录信息
     * @param paramsMap
     * @return
     */
    List<Map<String,Object>> queryResourceOrderInfoLocal(Map<String, Object> paramsMap);

    /**
     * 查询所有下发本地网的versionId
     * @param id
     * @return
     */
    List<String> qryLocalId(@Param("id") String id);

    /**
     * 查询是否存在其他处理中的专业补录子流程
     * @param paramsMap
     * @return
     */
    int qryIfHasOtherWoOrder(Map<String, Object> paramsMap);
    /**
     * 查询二干的各专业资源补录环节，是否已完成
     * @param paramsMap
     * @return
     */
    int qryIfHasSpecialWoOrder(Map<String, Object> paramsMap);
    /**
     * 查询执行中的子流程
     * @param qryparam
     * @return
     */
    List<Map<String,Object>> qryChildOrder(Map<String, Object> qryparam);

    /**
     * 查询上次调度 正常单的srvOrdId
     * @param param
     * @return
     */
    Map<String,Object> qryOriginSrvInfo(Map<String, Object> param);
    /**
     * 查询所有下发本地网的instanceId
     * @param id
     * @return
     */
    List<String> qryLocalInstanceId(@Param("id") String id);

}