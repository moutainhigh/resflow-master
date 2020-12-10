package com.zres.project.localnet.portal.flowdealinfo.data.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface OrderDetailsDao {

    Map queryConsumerInfo(String srv_ord_id);

    Map queryConsumerInfoByCustId(String cst_ord_id);

    List<Map<String,Object>> queryOrderDeatilsInfo(Map<String, Object> params);

    List<Map<String, Object>> queryCircuitInfo(@Param("srv_ord_id") String srv_ord_id,@Param("service_id") String service_id);

    List<Map<String, Object>> queryCircuitInfoAZ(@Param("srv_ord_id") String srv_ord_id,@Param("service_id") String service_id,@Param("stateLabel") String stateLabel);

    List<Map<String, Object>> queryCircuitInfoPE(@Param("srv_ord_id") String srv_ord_id,@Param("service_id") String service_id,@Param("stateLabel") String stateLabel);

    List<Map<String, Object>> queryCircuitInfoGrid(Map<String,Object> params);

    List<Map<String, Object>> queryCirDispatchOrderIds(Map<String, Object> params);

    List<Map<String, Object>> queryCircuitInfoBySrvIdY(String SrvOrdId);

    List <Map<String, Object>> queryAttachInfo(Map<String, Object> params);

    List <Map<String, Object>> queryDispatchAttachInfo(@Param("dispatchOrderId") String dispatchOrderId);

    List <Map<String, Object>> queryLogInfo(String orderId);

    List <Map<String, Object>> queryTaskInfo(@Param("orderId") String orderId);

    /**
     * 查询二干下发本地的单子
     * @param orderId
     * @return
     */
    List <Map<String, Object>> querySecToLocalTaskInfo(@Param("orderId") String orderId);

    List<Map<String, Object>> queryIdeaInfoBySrvOrdId(String srvordId);

    List<Map<String, Object>> queryRelevanceOrderInfo(@Param("orderId") String orderId);

    List<Map<String, Object>> queryWarningInfo(String orderId);

    List<Map<String,Object>> queryDispatchOrderInfo(String cstOrdId);

    List<Map<String,Object>> queryDispatchOrderInfoByIds(Map<String,Object> param);

    List<Map<String,Object>> queryDispatchOrderInfoByCstOrdId(String cstOrdId);

    List<Map<String,Object>> queryDispatchOrderInfoByDispatchId(String dispatchId);

    /**
     * 本地调度的单子，查询本地资源信息
     * 二干下发本地的单子，查询二干资源信息
     * @param param
     * @return
     */
    List<Map<String, Object>> queryResourceOrderInfo(Map<String,Object> param);

    /**
     * 查询二干下发本地的单子--本地资源信息
     * @param param
     * @return
     */
    List<Map<String, Object>> queryResourceOrderInfoLocal(Map<String,Object> param);

    List<Map<String, Object>> queryResourceOrderInfoResOne(Map<String,Object> param);

    List<Map<String, Object>> queryResourceOrderInfoBySrvOrdIdResOne(Map<String,Object> param);

    List<Map<String, Object>> queryResourceOrderInfoBySrvOrdId(Map<String,Object> param);

    List<Map<String, Object>> queryFeedbackAInfo(String srvOrDiD);

    List<Map<String, Object>> queryFeedbackZInfo(String srvOrDiD);

    Map<String, Object> queryRelevanceSrvOrdId(@Param("srvOrdId") String srvOrdId, @Param("orderType") String orderType);

    List<Map<String, Object>> qryExceptionNoticeList(Map<String, Object> params);

    List<Map<String, Object>> queryAddProductInfoBySrvOrdId(Map<String, Object> param);

    List<Map<String, Object>> queryApplyAttachInfo(Map<String, Object> param);

    Map<String, Object> querySrvOrdIdsByCstOrdId(Map<String, Object> param);

    /**
     * 根据cst_ord_id查询系统来源
     * @param cstOrdId
     * @return
     */
    List<Map<String, Object>> querySystemSourceByCstOrdId(String cstOrdId);

    /**
     * 根据CST_ORD_ID查询电路信息表中的调单ID
     * @param cstOrdId
     * @return
     */
    List<Map<String, Object>> queryDispatchIdByCstOrdId(String cstOrdId);

    /**
     * 根据cst_ord_id查询一干调单ID
     * @param cstOrdId
     * @return
     */
    List<Map<String, Object>> queryDispatchIdFromOneDryByCstOrdId(String cstOrdId);

    /**
     * 根据order_id查询环节信息
     * @param orderId
     * @return
     */
    Map<String, Object> queryTacheInfoByOrderId(String orderId);

    /**
     * 跨域全程调测环节查询附件
     * @param param
     * @return
     */
    List<Map<String, Object>> queryAttachInfo2(Map<String, Object> param);

    List<Map<String, Object>> queryTaskInfoByTacheCode(Map<String, Object> param);

    Map<String, Object> queryParentOrderIdByOrderId(String orderId);

    /**
     * 查询订单有没有追单信息
     */
    public List<Map<String, Object>> queryIfTrackData(@Param("orderId") String orderId, @Param("cstOrdId") String cstOrdId);

    /**
     * 查询环节工单的状态
     * @param orderId
     * @param tacheCode
     * @return
     */
    Map<String, Object> qryTacheWoOrderState(@Param("orderId")String orderId, @Param("tacheCode")String tacheCode);

    /**
     * 查询异常单下发到本地的单子
     * @param orderIdChg
     * @return
     */
    public Map<String, Object> qryToLocalOrderChg(@Param("orderIdChg") String orderIdChg,
                                                        @Param("regionId") String regionId);
    /**
     * 电路详情-核查信息Tab-查询反馈信息
     * @param srvOrdId
     * @return
     */
    List<Map<String,Object>> queryFeedbackInfo(String srvOrdId);

    /**
     *开通单 ,查找上次调度的核查单信息，
     * @param srvOrdId
     * @return
     */
    Map<String,Object> qryLastCheck(@Param("srvOrdId") String srvOrdId);

    /**
     * 过户信息查询
     * @param params
     * @return
     */
    List<Map<String, Object>> queryRenameLogByCstOrdId(Map<String,Object> params);
}
