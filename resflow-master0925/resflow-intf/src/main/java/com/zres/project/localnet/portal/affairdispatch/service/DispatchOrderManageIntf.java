package com.zres.project.localnet.portal.affairdispatch.service;

import java.util.List;
import java.util.Map;

/**
 * 事务调单管理接口
 *
 * @author PangHao
 * @date 2019/4/22 : 18:46
 */
public interface DispatchOrderManageIntf {

    /**
     * 发起事务调单
     *
     * @param map 前端传来的事务点单信息
     * @return 处理结果描述
     * @throws Exception 事务控制异常
     * @author PangHao
     * @date 2019/4/22 : 18:51
     */
    Map<String, Object> initAffairDispatchOrder(Map<String, Object> map) throws Exception;

    /**
     * 查询事务调单列表
     *
     * @param map
     * @return
     */
    Map<String, Object> queryAffairOrderList(Map<String, Object> map);


    /**
     * 子流程事务处理回单
     *
     * @param map 事物處理參數
     * @return 回單結果
     * @author PangHao
     * @date 2019/4/26 : 10:19
     */
    Map<String, Object> affairProcessComplateWo(Map<String, Object> map);

    /**
     * 事务调单审核
     *
     * @param map 审核工单信息
     * @return 回单结果
     * @author PangHao
     * @date 2019/4/26 : 10:29
     */
    Map<String, Object> affairDispatchOrderCheck(Map<String, Object> map);

    /**
     * 查询各事务单数量
     * 
     * @param map
     * @return
     */
    Map<String, Object> countVariousAffairOrder(Map<String, Object> map);

    /**
     * 查询审核驳回待处理工单数
     * 
     * @param map
     * @return
     */
    Map<String, Object> queryShRejectNum(Map<String, Object> map);

    /**
     * 查询事务处理被驳回待处理工单数
     * 
     * @param map
     * @return
     */
    Map<String, Object> queryClRejectNum(Map<String, Object> map);

    /**
     * 父流程事务确认回单
     *
     * @param map 事务确认工单信息
     * @return 回单结果
     * @author PangHao
     * @date 2019/4/26 : 10:34
     */
    Map<String, Object> affairAffirmComplateWo(Map<String, Object> map);

    /**
     * 子流程事务处理批量退单
     *
     * @param orderList 子流程工单信息集合
     * @return 退单结果
     * @author PangHao
     * @date 2019/4/28 : 14:38
     */
    Map<String, Object> affairProcessRollBackWo(List<Map<String, Object>> orderList);


    //得到事务调单附件(作废)
    //List<Map<String, Object>> getAffairDispatchFile(String affairId);

    /**
     * 得到事务调单抄送人
     *
     * @param affairId 事务调单标识
     * @author PangHao
     * @date 2019/4/29 : 16:15
     */
    List<Map<String, Object>> getAffairNoticeStaffArray(String affairId);

    /**
     * 获取事务调单审核信息
     *
     * @param affairId 事务调单标识
     * @author PangHao
     * @date 2019/4/29 : 16:15
     */
    List<Map<String, Object>> getAffairCheckInfoArray(String affairId);

    /**
     * 获取事务调单处理进度列表
     *
     * @param affairId 事务调单标识
     * @author PangHao
     * @date 2019/4/29 : 16:15
     */
    List<Map<String, Object>> getAffairDisposeList(String affairId);

    /**
     * 获取事务调单子单处理进度
     *
     * @param orderId 定单标识
     * @author PangHao
     * @date 2019/4/29 : 16:15
     */
    List<Map<String, Object>> getChildAffairDisposeList(String orderId);

    /**
     * 获取工单附件
     *
     * @param woId 工单ID
     * @author PangHao
     * @date 2019/4/29 : 16:15
     */
    List<Map<String, Object>> getAffairWoAccessoryFile(String woId);

    /**
     * 关闭事务调单
     * @author  PangHao
     * @date    2019/5/9 : 16:56
     * @param   map 事务调单及定单标识
     * @return  关闭结果
     * @throws  Exception exception异常
     */
    Map<String, Object> closeAffair(Map<String, Object> map);

    /**
     * 事务调单审查环节回单
     * @param map
     * @return
     */
    Map<String, Object> affairDispatchOrderReview(Map<String, Object> map);

    /**
     * 查询事务调单类型枚举值
     * @return
     */
    public List<Map<String, Object>> qryAffairDispatchOrderType();

}
