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

    List<Map<String, Object>> queryCircuitDraftInfoGrid(Map<String,Object> params);

    List<Map<String, Object>>   queryCirDispatchOrderIds(Map<String, Object> params);

    List<Map<String, Object>> queryCircuitInfoBySrvIdY(String SrvOrdId);

    List <Map<String, Object>> queryAttachInfo(@Param("srv_ord_id") String srv_ord_id, @Param("orderId") String orderId);

    List <Map<String, Object>> queryDispatchAttachInfo(@Param("dispatchOrderId") String dispatchOrderId);

    List <Map<String, Object>> qryDispatchAttachForDraftSchedule(@Param("dispatchOrderId") String dispatchOrderId);

    List <Map<String, Object>> queryLogInfo(String orderId);

    /**
     * 查询任务列表
     * @param orderId
     * @return
     */
    List <Map<String, Object>> queryTaskInfo(@Param("orderId") String orderId);

    /**
     * 查询二干下发本地的单子
     * @param orderId
     * @return
     */
    List <Map<String, Object>> querySecToLocalTaskInfo(@Param("orderId") String orderId);

   /* List <Map<String, Object>> querySubTaskInfo(@Param("orderId") String orderId);

    List <Map<String, Object>> queryLocalScheuleTaskInfo(@Param("orderId") String orderId);

    Map<String, Object> qryCircuitOrderInfo(@Param("orderId") String orderId);*/

    List<Map<String, Object>> queryIdeaInfoBySrvOrdId(String srvordId);

    List<Map<String, Object>> queryRelevanceOrderInfo(@Param("orderId") String orderId);

    List<Map<String, Object>> queryWarningInfo(String orderId);

    List<Map<String,Object>> queryDispatchOrderInfo(String cstOrdId);

    List<Map<String,Object>> queryDispatchOrderInfoByIds(Map<String,Object> param);

    List<Map<String,Object>> queryDispatchOrderInfoById(String dispatchOrderId);

    List<Map<String, Object>> queryResourceOrderInfo(Map<String,Object> param);

    List<Map<String, Object>> queryResourceOrderInfoResOne(Map<String,Object> param);

    List<Map<String, Object>> queryResourceOrderInfoBySrvOrdIdResOne(Map<String,Object> param);

    List<Map<String, Object>> queryResourceOrderInfoBySrvOrdId(Map<String,Object> param);

    List<Map<String, Object>> queryFeedbackAInfo(String srvOrDiD);

    List<Map<String, Object>> queryFeedbackZInfo(String srvOrDiD);

    Map<String, Object> queryRelevanceSrvOrdId(@Param("srvOrdId") String srvOrdId, @Param("orderType") String orderType);
    /*--and exists(select b.SRV_ORD_ID from GOM_BDW_SRV_ORD_RES_INFO info where info.SRV_ORD_ID = b.SRV_ORD_ID)*/

    List<Map<String, Object>> queryDispatchOrderIdFromRelateTable(String cstOrdId);

    List<Map<String, Object>> queryDispatchOrderIdFromOneDry(String cstOrdId);

    List<Map<String, Object>> queryTaskInfoByTacheCode(Map<String, Object> param);

    Map<String, Object> queryParentOrderIdByOrderId(String orderId);

    List<Map<String, Object>> queryOrderIdFromRelateTalbe(String srvOrdId);

    /**
     * 查询订单有没有追单信息
     */
    public int queryIfTrackData(@Param("orderId") String orderId, @Param("cstOrdId") String cstOrdId);

    List<String> qryTacheByOrderIds(@Param("orderIds") List<String> orderIds);

    List<Map<String, Object>> queryResourceOrderInfoLocal(Map<String,Object> param);

    /**
     * 查询环节工单的状态
     * @param orderId
     * @param tacheCode
     * @return
     */
    Map<String, Object> qryTacheWoOrderState(@Param("orderId")String orderId, @Param("tacheCode")String tacheCode);

    /**
     * 电路详情-核查信息Tab-查询反馈信息
     * @param srvOrdId
     * @return
     */
    List<Map<String,Object>> queryFeedbackInfo(String srvOrdId);

    Map<String, Object> querySrvOrdIdsByCstOrdId(Map<String, Object> param);

    List<Map<String, Object>> queryApplyAttachInfo(Map<String, Object> param);
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

    /**
     *获取本地的orderId
     * @param srvOrdId
     * @return
     */
    List<String> qryLocalOrderIdListBySrvOrderId(@Param("srvOrdId") String srvOrdId);

    /**
     * 电路详情-本地核查信息Tab-查询反馈信息
     * @param woIdList
     * @return
     */
    List<Map<String,Object>> qryLocalFeedBcakInfo(@Param("woIdList")List<String> woIdList);

}
