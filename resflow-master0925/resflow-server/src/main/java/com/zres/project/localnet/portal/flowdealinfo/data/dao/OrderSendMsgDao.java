package com.zres.project.localnet.portal.flowdealinfo.data.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderSendMsgDao {

    /**
     * 查询短信消息表
     * @param map
     * @return
     */
    public int insertMsg (Map<String, Object> map);

    /**
     * 从短信消息表中赋值到历史表中----发送成功
     * @param msgId
     * @return
     */
    public int copyMsgToHis (int msgId);

    /**
     * 删除短信消息表数据
     * @param msgId
     * @return
     */
    public int deleteMsg (int msgId);

    /**
     * 发送失败--修改短信消息表的状态
     * @param map
     * @return
     */
    public int updateMsgState (Map<String, Object> map);

    /**
     * 查询短信消息表中需要发送短信的数据
     * @return
     */
    public List<Map<String, Object>> getNeedSendMsgData(String orderType);

    /**
     * 查询工单的派单对象类型，派单对象，申请单编号等信息 -- 主单
     * @param orderIdList
     * @return
     */
    public List<Map<String, Object>> qrySendMsgInfo(@Param("orderIdList") List<String> orderIdList);

    /**
     * 查询工单的派单对象类型，派单对象，申请单编号等信息 -- 子单
     * @param orderIdList
     * @return
     */
    public List<Map<String, Object>> qrySendMsgInfoChild(@Param("orderIdList") List<String> orderIdList);

    /**
     * 查询工单的派单对象类型，派单对象，申请单编号等信息 -- 子单内部环节退单
     * @param params
     * @return
     */
    public List<Map<String, Object>> qryRollBackSendMsgInfoChild(Map params);

    /**
     * 查询工单的派单对象类型，派单对象，申请单编号等信息 -- 子单资源分配环节退单
     * @param params
     * @return
     */
    public List<Map<String, Object>> qryRollBackSendMsgInfoChildFristTache(Map params);

    /**
     * 查询工单的派单对象类型，派单对象，申请单编号等信息 -- 主单二干下发
     * @param orderIdList
     * @return
     */
    public List<Map<String, Object>> qrySendMsgInfoFromSec(@Param("orderIdList") List<String> orderIdList);

    /**
     * 查询工单的派单对象类型，派单对象，申请单编号等信息 -- 子单二干下发
     * @param orderIdList
     * @return
     */
    public List<Map<String, Object>> qrySendMsgInfoChildFromSec(@Param("orderIdList") List<String> orderIdList);

    /**
     * 查询工单的派单对象类型，派单对象，申请单编号等信息 -- 二干下发  子单内部环节退单
     * @param params
     * @return
     */
    public List<Map<String, Object>> qryRollBackSendMsgInfoChildFromSec(Map params);

    /**
     * 查询工单的派单对象类型，派单对象，申请单编号等信息 -- 二干下发  子单资源分配环节退单
     * @param params
     * @return
     */
    public List<Map<String, Object>> qryRollBackSendMsgInfoChildFristTacheFromSec(Map params);

    /**
     * 查询工单的派单对象类型，派单对象，申请单编号等信息 -- 主单异常节点退单
     * @param params
     * @return
     */
    public List<Map<String, Object>> qryRollBackSendMsgInfo(Map params);

    /**
     * 查询工单的派单对象类型，派单对象，申请单编号等信息 -- 二干下发  主单异常节点退单
     * @param params
     * @return
     */
    public List<Map<String, Object>> qryRollBackSendMsgInfoFromSec(Map params);

    /**
     * 查询当前执行中的环节   过滤掉子流程等待环节
     * @param orderId
     * @return
     */
    public Map<String, Object> qryCurrentTache(@Param("orderId") String orderId);

    /**
     * 查询所有子流程的orderId
     * @param orderId
     * @return
     */
    public List<String> qryOrderIdChild(@Param("orderId") String orderId, @Param("tacheCodeFlag") String tacheCodeFlag);

    /**
     * 插入短信日志表
     * @param params
     * @return
     */
    public int insertSendMsgLog(Map params);

    /**
     * 查询父流程的环节是否为待数据制作和本地调度环节
     * @param orderId
     * @return
     */
    public int qryIsDataAndSchedule(@Param("orderId") String orderId);

    /**
     * 查询二干下发本地网所有orderid
     * @param orderId
     * @return
     */
    public List<String> qryOrderIdToBdw(@Param("orderId") String orderId);

    /**
     * 查询之前是否插入发送短信
     * @param params
     * @return
     */
    public List<Map<String, Object>> qrySendMsgLog(Map params);

}
