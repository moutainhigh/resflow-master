package com.zres.project.localnet.portal.flowdealinfo.data.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CheckOrderDao {

    /**
     * 查询核查调度环节的工单id
     * @param orderId
     * @return
     */
    Map<String, Object> qryCheckDispatchWoOrder(@Param("orderId") String orderId, @Param("tacheId") String tacheId);

    /**
     * 修改工单状态
     * @param updateMap
     * @return
     */
    int updateWoOrderStateByWoId(Map updateMap);

    /**
     * 修改定单状态
     * @param updateMap
     * @return
     */
    int updateOrderStateByOrderId(Map updateMap);

    /**
     * 修改下发本地网关闭表状态
     * @param updateMap
     * @return
     */
    int updateToLocalStateById(Map updateMap);

    /**
     * 查询所有专业核查的工单id
     * @param orderId
     * @return
     */
    List<String> qryALLSpecialCheckWoOrder(@Param("orderId") String orderId);

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
    List<Map<String, Object>> qrySpecialData(@Param("orderId") String orderId);

    /**
     * 查询下发本地的核查流程的电路，定单表，和工单数据；
     * @param orderId
     * @return
     */
    List<Map<String, Object>> qryToLocalCheck(@Param("orderId") String orderId);

    /**
     * 查询二干下发本地核查的区域信息
     * @param orderId
     * @return
     */
    List<Map<String, Object>> qryToLocalCheckData(@Param("orderId") String orderId);

    /**
     * 查询本地核查等待环节的工单id
     * @param orderId
     * @return
     */
    String qryLocalCheckWaitTacheId(@Param("orderId") String orderId);

    /**
     * 查询二干下发到本地的所有核查等待环节的工单
     * @param orderId
     * @return
     */
    List<Map<String, Object>> qryAllLocalCheckWaitTacheWoOrder(@Param("orderId") String orderId);
    /**
     * 查询已完成的专业核查
     * @param orderId
     * @return
     */
    List<Map<String, Object>> qryCompleteCheckSpecInfo(String orderId);

    /**
     * 根据电路ID查询系统源信息
     * @param param
     * @return
     */
    Map<String, Object> querySystemInfo(Map<String, Object> param);

    List<Map<String, Object>> qryCurrentCompleteCheckSpecInfo(String orderId, String toSpecialTache);
}
