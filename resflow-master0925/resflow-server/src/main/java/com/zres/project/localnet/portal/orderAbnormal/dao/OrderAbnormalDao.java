package com.zres.project.localnet.portal.orderAbnormal.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderAbnormalDao {
    List<Map<String, Object>> qryOrdChgLogByCstOrdId(Map<String, Object> param);

    String qrySrvOrdIds(String cstOrdId);

    List<HashMap<String, String>> qryWoOperAttrs(String woId);

    int updateOrderState(List<String> orderIds);

    int updateChangeOrderState(@Param("orderIds") List<String> orderIds, @Param("orderId") String orderId, @Param("state") String state, @Param("remark") String remark);

    int updateChangeOrderState4Listener(@Param("orderId") String orderId, @Param("remark") String remark);

    int updateWoState(Map<String, Object> param);

    List<Map<String, String>> qryChangeOrderLog(Map<String, Object> param);

    /**
     * 获取原单order_id，再根据order_id获取原单下的所有子流程
     *
     * @param chgWoIds 异常单父工单id
     * @return
     */
    List<Map<String, String>> qryCldOrderIdAndPsCode(@Param("chgWoIds") List<String> chgWoIds, @Param("chgWoId") String chgWoId);

    /**
     * 二干下发本地子单
     *
     * @param chgWoIds
     * @param chgWoId
     * @return
     */
    List<Map<String, String>> qryLocalOrderIdAndPsCodeErGan(@Param("chgWoIds") List<String> chgWoIds, @Param("chgWoId") String chgWoId);

    int updateSrcOrderId(@Param("orderId") String orderId, @Param("srcOrderId") String srcOrderId);

    Map<String, String> qryGomWOInfo(@Param("woId") String woId);

    Map<String, String> qryOrderInfo(@Param("orderId") String orderId);

    List<Map<String, String>> qryWoInfo(@Param("orderIds") List<String> orderIds);

    List<Map<String, String>> qrySiblingOrder(@Param("orderId") String orderId, @Param("state") String state);

    /**
     * 查出父单中处于工作环节工单
     *
     * @param orderId
     * @return
     */
    Map<String, String> qryParentWorkingWoId(@Param("orderId") String orderId, @Param("woState") String woState);

    List<Map<String, Object>> queryChangeOrderInfo(Map<String, Object> param);

    List<Map<String, String>> lstGomUserS(@Param("userIds") List<String> userIds);

    List<Map<String, String>> countUnDispatchToSysExceptionOrd(@Param("tacheCodes") List<String> tacheCodes, @Param("chgVersion") String chgVersion, @Param("chgType") String chgType, @Param("srcCstOrderId") String srcCstOrderId);

    /**
     * 查询数据来源
     *
     * @param cstOrdId
     * @return
     */
    List<Map<String, Object>> qrySrvInfoByCstOrdId(@Param("cstOrdId") String cstOrdId);

    List<Map<String, Object>> getResUrlParam(Map<String, Object> param);

}
