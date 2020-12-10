package com.zres.project.localnet.portal.flowdealinfo.data.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface OrderQrySecondaryDao {

    /**
     * 查询单子所属系统
     *   本地网：flow-schedule-lt
     *   二干：second-schedule-lt
     * @param param
     * @return
     */
    public Map<String, Object> qrySrvOrderBelongSys(Map param);


    public Map<String, Object> qryParentPsIdBySubOrderId(Map param);

    public Map<String, Object> qryParentPsIdByOrderId(Map param);

    /**
     * 查询单子来源 -- 由二干下发的
     *   本地网：localBuild
     *      *   一干：onedry
     *      *   二干：secondary
     * @param orderId
     * @return
     */
    public String qrySrvOrderSourceFromSec(String orderId);

    /**
     * 查询单子来源 -- 本地网
     *   本地网：localBuild
     *      *   一干：onedry
     *      *   二干：secondary
     * @param orderId
     * @return
     */
    public String qrySrvOrderSource(String orderId);

    /**
     * 查询二干启的本地网跨域流程到起租环节的数量
     * @param orderId
     * @return
     */
    public int qryRentTacheNum(String orderId);

    /**
     * 查询二干启的本地网跨域流程的总数量
     * @param orderId
     * @return
     */
    public int qryCrossFlowNum(String orderId);

    /**
     * 查询下发本地网的单子所起子流程的总数
     * @param orderId
     * @return
     */
    public int qryLocalChildFlowAllNum(String orderId);

    /**
     * 查询下发本地网的单子所起子流程完成的数量
     * @param orderId
     * @return
     */
    public int qryLocalChildFlowFinishNum(String orderId);

    /**
     * 查询跨域全程调测环节的工单是否存在
     * @param orderId
     * @return
     */
    public int qryCrossTacheOrderCount(String orderId);

    /**
     * 查询跨域全程调测环节的工单
     * @param orderId
     * @return
     */
    public Map<String, Object> qryTestTacheOrder(String orderId);

    /**
     * 获取二干本地调度的工单
     * @param orderId
     * @return
     */
    //public Map<String, Object> qrySecLocalOrder(String orderId);

    /**
     * 获取二干待数据制作与本地调度执行中的工单
     * @param orderId
     * @return
     */
    public Map<String, Object> qryDataScheduleOrder(String orderId);

    /**
     * 查询该单子是否从二干发过来的
     * @param orderId
     * @return
     */
    public Map<String, Object> qryIfFromSecondary(String orderId);

    /**
     * 查询跨域全程调测环节的工单--二干调度
     * @param orderId
     * @return
     */
    public Map<String, Object> qrySecTestTacheOrder(String orderId);

    /**
     * 查询二干全程调测的单子
     * @param orderId
     * @return
     */
    public Map<String, Object> qrySecAllTestOrder(String orderId);

    /**
     * 查询二干下发本地网关联单关联表id
     * @param srvOrdId
     * @return
     */
    public List<String> qryRelateInfoId(@Param("srvOrdId") String srvOrdId);

    /**
     * 通过业务订单Id查询关联表Id
     * @param srvOrdId
     * @return
     */
    public List<String> qryRelateInfoIdBySrvordId(@Param("srvOrdId") String srvOrdId);

    /**
     * 获取子流程到达最后一个环节的数量---用于二干
     *
     * @param orderId
     * @return
     */
    public Map<String, Object> qryChildFlowNumAtLastSec(@Param("orderId") String orderId,
                                                        @Param("tacheCode") String tacheCode,
                                                        @Param("subName") String subName);

    /**
     * 获取子流程的数量
     *
     * @param orderId
     * @return
     */
    public Map<String, Object> qryChildFlowNum(@Param("orderId") String orderId, @Param("subName") String subName);

    /**
     * 查询集客ftp服务器的信息
     * @param keyValue
     * @return
     */
    public String qryFtpJiKeData(@Param("codeType") String codeType,
                                 @Param("keyValue") String keyValue);

    /**
     * 查询集客ftp服务器上传文件路径
     * @param codeType
     * @param keyValue
     * @return
     */
    public String qryFtpJiKeDirData(@Param("codeType") String codeType,
                                 @Param("keyValue") String keyValue);

    /**
     * 查询本地网起止租环节的工单
     * @param orderId
     * @return
     */
    public List<Map<String, Object>> qryRentTacheOrder(String orderId);


    public List<Map<String, Object>> querySec2LocalInfo(String prentOrderId);

    /**
     * 查询二干下发到本地电路调度环节工单
     * @param srvOrdId
     * @return
     */
    public List<Map<String, Object>> querySecToLocalOrderCircuitDispatch(String srvOrdId);

    /**
     * 查询是否有本地调单
     * @param dispatchOrderId
     * @return
     */
    public int qryIfHasLocalDispatchOrder(String dispatchOrderId);

    /**
     * 修改调单状态
     * @param dispatchOrderId
     * @return
     */
    public int updateDispatchOrder(String dispatchOrderId);

    public Map<String, Object> qrySecDispatchOrder(String orderId);

    /**
     * 入库工单正向反向标识--插入工单操作属性表
     * @param woOrderParams
     * @return
     */
    public int insertWoOrderOper(Map<String, Object> woOrderParams);

    /**
     * 修改电路调度环节工单反向标识  本地单子
     * @param woId
     * @return
     */
    public int updateWoOrderOper(String woId);

    /**
     * 修改二干本地关联表的状态
     * @param srvOrdId
     * @return
     */
    public int updateSecOrderState(String srvOrdId);
    /**
     * 通过业务订单Id查询关联表的instanceId
     * @param srvOrdId
     * @return
     */
    public List<String> qryRelateInstanceIdBySrvordId(@Param("srvOrdId") String srvOrdId);

    /**
     *
     */
    public List<Map<String, Object>> queryCheckOrderStatBySrvOrdId(@Param("srvOrdId") String srvOrdId);

    /*
     * 查询电路状态，srv_ord_stat
     * @Param:
     * @Return:
     */
    public String qryeSrvOrdStatBySrvOrdId(@Param("srvOrdId") String srvOrdId);

    /**
     * 查询调度信息
     * @param orderId
     * @return
     */
    public Map<String, Object> qryDispatchData(String orderId);
}
