package com.zres.project.localnet.portal.logInfo.service;

import java.util.Map;

import com.zres.project.localnet.portal.logInfo.entry.ToKafkaTacheLog;
import com.zres.project.localnet.portal.logInfo.entry.ToOrderCenterLog;
import com.zres.project.localnet.portal.logInfo.entry.ToOrderCenterTacheLog;

public interface ToOrderCenterIntf {

    /**
     * 推送给订单中心
     * @param xmlStr
     * @return
     */
    Map<String, Object> toOrderCenterIntf(String xmlStr);

    /**
     * 拼接报文
     * @param toOrderCenterLog
     * @return
     */
    String appendXml(ToOrderCenterLog toOrderCenterLog);

    /**
     * 解析报文
     * @param xmlStr
     * @return
     */
    String analysisXml(String xmlStr);

    //Map<String, Object> toOrderCenterTacheIntf(ToOrderCenterTacheLog toOrderCenterTacheLog);

    /**
     * 推送日志到订单中心
     * @param toOrderCenterLog
     * @return
     */
    void pushCenterLog(ToOrderCenterLog toOrderCenterLog);

    /**
     * 推送日志到kafka
     * @param toKafkaTacheLog
     */
    void pushKafkaLog(ToKafkaTacheLog toKafkaTacheLog);


}
