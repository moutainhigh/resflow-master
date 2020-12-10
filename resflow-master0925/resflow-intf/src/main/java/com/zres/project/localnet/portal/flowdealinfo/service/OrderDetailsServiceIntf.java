package com.zres.project.localnet.portal.flowdealinfo.service;

import java.util.List;
import java.util.Map;

public interface OrderDetailsServiceIntf {
    /**
     * 订单详情页查询电路信息
     * @return
     */
    Map<String, Object> queryCircuitInfo(String cstOrdId, String service_Id);

    /**
     * 查询电路信息列表
     * @return
     */
    Map<String, Object> queryCircuitInfoGrid(Map<String, Object> params);

    /**
     * 起草调单查询电路信息
     * @param params
     * @return
     */
    Map<String, Object> queryCircuitInfoDraftGrid(Map<String, Object> params);

    /**
     * 定单详情页根据定单ID查询客户信息
     * @return
     */
    Map queryConsumerInfoByCustId(String cstOrdId);

    Map queryConsumerInfo(String cstOrdId);

    /**
     * 定单详情页根据定单ID查询定单信息
     * @return
     */
    List<Map<String,Object>> queryOrderDeatilsInfo(String cstOrdId);


    /**
     * 根据定单Id查询附件信息
     * @param orderId
     * @return
     */
    List<Map<String, Object>> queryAttachInfo(String srv_ord_Id, String orderId);

    List<Map<String, Object>> queryDispatchAttachInfo(String dispatchOrderId);

    List<Map<String, Object>> qryDispatchAttachForDraftSchedule(String dispatchOrderId);

    /**
     * 根据定单ID查询日志信息
     * @param orderId
     * @return
     */
    List<Map<String, Object>> queryLogInfo(String orderId);

    /**
     * 查询二干的任务列表
     * @param orderId
     * @return
     */
    List<Map<String, Object>> queryTaskInfo(String orderId);

    /**
     * 查询二干下发本地网的单子
     * @param orderId
     * @return
     */
    List<Map<String, Object>> querySecToLocalTaskInfo(String orderId);

    /**
     * 根据定单Id查询本地网任务信息
     * @param orderId
     * @return
     *//*
    List<Map<String, Object>> querySubTaskInfo(String orderId);*/

    /**
     *  查询阶段性处理意见
     * @param srvordId
     * @return
     */
    List<Map<String, Object>> queryIdeaInfoBySrvOrdId(String srvordId);

    /**
     * 查询关联主/子单信息
     * @param orderId
     * @return
     */
    List<Map<String, Object>> queryRelevanceOrderInfo(String orderId);

    /**
     * 查询预警超时信息
     * @param orderId
     * @return
     */
    List<Map<String, Object>> queryWarningInfo(String orderId);

    /**
     * 查询调单信息
     * @param cstOrdId
     * @return
     */
    List<Map<String, Object>> queryDispatchOrderInfo(String cstOrdId);

    List<Map<String, Object>> queryDispatchOrderInfo(Map<String, Object> params);


    List<Map<String, Object>> queryDispatchOrderInfoById(Map<String, Object> param);

    /**
     * 查询一干资源信息
     * @param srvOrdId
     * @return
     */
    List<Map<String, Object>> queryResourceOrderInfoW(String srvOrdId);

    /**
     * 查询二干资源信息
     * @param srvOrdId
     * @return
     */
    List<Map<String, Object>> queryResourceOrderInfo(String srvOrdId);

    /**
     * 查询本地资源信息
     * @param srvOrdId
     * @return
     */
    List<Map<String, Object>> queryResourceOrderInfoY(String srvOrdId);

    /**
     * 查询反馈信息
     * @param srvOrdId
     * @return
     */
    List<Map<String, Object>> queryFeedbackInfo(String srvOrdId);

    /**
     * 查询反馈信息
     * @param srvOrdId
     * @return
     */
    List<Map<String, Object>> queryLocalFeedbackInfo(String srvOrdId);

    /**
     * 详情页面流程图跟踪，点击环节时查询环节处理信息
     * @param param
     * @return
     */
    Map<String, Object> queryTaskInfoByTacheCode(Map<String, Object> param);

    /**
     * 查询订单有没有被追单
     */
    public boolean queryIfTrack(String orderId, String cstOrdId);

    /**
     * 获取主流程当前环节
     * @param orderIds
     * @return
     */
    List<String> qryTacheByOrderIds(String orderIds);

    /**
     * 查询JIKE、一干、本地发起业务信息附件
     * @param param
     * @return
     */
    List<Map<String, Object>> queryApplyAttachInfo(Map<String, Object> param);
    /**
     * 查询核查直开的单子，如果核查单不处理不能提交
     * 20200429 renll
     *  zmp：2009745
     */
    Map<String, Object>     queryCheckOrderStatBySrvOrdId(String srvOrdId);

    /**
     * 过户信息
     * @param params
     * @return
     */
    Map<String, Object>  queryRenameLogByCstOrdId(Map<String,Object> params);

    /**
     * 查询工单是否报竣
     * auther cwy
     *
     */
    Map<String, Object> querySrvOrdStatBySrvOrdId(String srvOrdId);

}
