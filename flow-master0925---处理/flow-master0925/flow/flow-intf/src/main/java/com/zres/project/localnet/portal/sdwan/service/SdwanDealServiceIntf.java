package com.zres.project.localnet.portal.sdwan.service;

import java.util.Map;
import java.util.List;

/**
 * sdwan模块
 */
public interface SdwanDealServiceIntf {

    /**
     * 环节回单方法
     * @param params
     * @return
     * @throws Exception
     */
    public Map<String, Object> submitOrderSdwan(Map<String, Object> params) throws Exception;

    /**
     * 等待sdwan反馈环节自动提交
     * @param orderId
     * @return
     */
    public Map<String, Object> tacheAutoSubmit(String orderId) throws Exception;

    /**
     * 保存终端信息
     */
    public Map<String, Object> saveDeviceInfo(Map<String, Object> params) throws Exception;

    /**
     * 查询电路的设备标准型号厂家
     * @return
     */
    public List<Map<String, Object>> queryCircuitInfo(Map<String, Object> params);

    /**
     * 查询电路的设备标准型号厂家  枚举
     * @param params
     * @return
     */
    List<Map<String, Object>> queryEnum(Map<String,Object> params);
    /**
     * 保存wan信息
     */
    public Map<String, Object> saveWanInfo(Map<String, Object> params) throws Exception;

    /**
     * 查询电路的wan配置信息
     * @return
     */
    public List<Map<String, Object>> queryWanInfo(Map<String, Object> params);
}
