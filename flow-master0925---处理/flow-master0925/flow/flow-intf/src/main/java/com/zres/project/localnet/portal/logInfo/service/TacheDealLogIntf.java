package com.zres.project.localnet.portal.logInfo.service;

import java.util.List;
import java.util.Map;

public interface TacheDealLogIntf {

    /**
     * 入库操作日志
     *
     * @param params
     * @return
     */
    Map<String, Object> addTrackLog(Map<String, Object> params);

    /**
     * 推送日志给订单中心
     *
     * @param params
     */
    void pushOrderLogToCenter(Map<String, Object> params);

    /**
     * 推送日志到kafka
     *
     * @param orderId
     * @param woId
     */
    void pushOrderLogToKafka(String orderId, String woId);

    /**
     * 资源配置完提交后将进行主流程及子流程工单消息推送操作
     *
     * @param orderId     订单ID
     * @param messageType 消息类型 （回单或者退单）
     * @param woId        工单ID
     * @author wangsen
     */
    void writeOrderMessage(String orderId, String woId, String messageType, String messageFlag, String remark);

    /**
     * 消息列表分页查询
     *
     * @param params
     * @author wangsen
     */
    Map<String, Object> queryMessageList(Map<String, Object> params);

    /**
     * 删除消息列表信息
     * @param
     * @return
     */
    Map<String, Object> deleteMessageList(List<Map<String, Object>> listParam);

}
