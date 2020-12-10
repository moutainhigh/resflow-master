package com.zres.project.localnet.portal.flowdealinfo.service;

import java.util.Map;

public interface SecDealLocalServiceInf {

    /**
     * 二干在起租通知本地网退单
     * @param params
     * @return
     */
    public Map<String, Object> rollBackWoOrderSec(Map<String, Object> params);

    /**
     * 查询数据制作所起子流程
     * @param param
     */
    public Map<String,Object> qrySecondDataMake(Map<String, Object> param);

    /**
     * 查询资源施工所起子流程
     * @param param
     */
    public Map<String,Object> qrySecondResMake(Map<String, Object> param);

    /**
     * 查询二干下发本地网流程
     * @param param
     */
    public Map<String,Object> qrySecToLocalData(Map<String, Object> param);

    /**
     * 完工汇总环节退单--二干跨域流程
     * @param params
     * @return
     */
    public Map<String, Object> summaryRollBack(Map<String, Object> params);

    /**
     * 退单数据制作子流程
     * @param params
     * @return
     */
    public Map<String, Object> rollBackData(Map<String, Object> params);

    /**
     * 退单资源施工子流程
     * @param params
     * @return
     */
    public Map<String, Object> rollBackRes(Map<String, Object> params);

    /**
     * 退单本地调度流程
     * @param params
     * @return
     */
    public Map<String, Object> rollBackLocal(Map<String, Object> params);

    /**
     * 完工汇总主流程退单
     * @param params
     * @return
     */
    public Map<String, Object> summaryRollBackOrder(Map<String, Object> params);

    /**
     * 添加退单日志
     * @param params
     * @return
     */
    public void addRollBackLog(Map<String, Object> params);

    /**
     * 查询电路是否起草过调单
     * @param params
     * @return
     */
    public int qryOrderIfConfigDispatch(Map<String, Object> params);

    /**
     * 查询是否有电路编号和是否二干资源分配
     * @param params
     * @return
     */
    public Map<String, Object> qryCircuitNum(Map<String, Object> params);

    /**
     * 插入电路编号
     * @param params
     * @return
     */
    public int insertCircuitNum(Map<String, Object> params);

    /**
     * 查询调度信息
     * @param orderId
     * @return
     */
    public Map<String, Object> qryDispatchData(String orderId);

    /**
     * 抄送----用于二干调度环节
     * @param params
     * @return
     */
    public boolean sendCopyOrder(Map<String, Object> params);

    /**
     * 查询父订单正在执行中的子流程
     * @param orderId
     * @return
     */
    public int qryChildOrderDealing(String orderId);

    /**
     * 补单处理--二干调度环节补单二干资源分配，数据制作和本地调度辅调局
     * @param params
     * @return
     */
    public Map<String, Object> supplementOrder(Map<String, Object> params);

}
