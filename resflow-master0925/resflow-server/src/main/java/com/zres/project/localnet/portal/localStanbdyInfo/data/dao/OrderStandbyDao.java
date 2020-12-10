package com.zres.project.localnet.portal.localStanbdyInfo.data.dao;

import com.zres.project.localnet.portal.local.domain.BaseObject;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author :wang.g2
 * @description :
 * @date : 2018/12/21
 */
@Repository
public interface OrderStandbyDao {

    /**
     * 查询客户订单--主单
     * @param params
     * @return
     */
    List<Map<String, Object>> qryCstOrdList(Map<String, Object> params);

    /**
     * 查询客户已完成订单--主单
     * @param params
     * @return
     */
    List<Map<String, Object>> qryCstOrdCompleteList(Map<String, Object> params);

    /**
     * 查询客户抄送订单
     * @param params
     * @return
     */
    List<Map<String, Object>> qryCstOrdCcList(Map<String, Object> params);

    /**
     * 抄送总数
     * @param params
     * @return
     */
    int queryStandbyCcOrderCount(Map<String, Object> params);

    /**
     * 查询业务订单--电路信息 主流程时
     * @param params
     * @return
     */
    public List<Map<String, Object>> qrySrvOrdList(Map<String, Object> params);

    /**
     * 查询业务订单--电路信息  子流程时
     * @param params
     * @return
     */
    public List<Map<String, Object>> qrySrvOrdChildList(Map<String, Object> params);

    List<Map<String, Object>> queryOrderInfo(Map<String, Object> params);

    List<Map<String, Object>> querySubOrderInfoColl(Map<String, Object> params);

    /**
     * 查询客户订单--主单的总数量
     * @param params
     * @return
     */
    int queryStandbyOrderCount(Map<String, Object> params);

    int queryStandbyOrderCompleteCount(Map<String, Object> params);

    List<BaseObject> querySysDictData(Map params);

    int updateCollapsible(Map params);

    int upLoadAttach(Map params);

    List<Map<String, Object>> queryAttachBySrvId(Map params);

    int delAttachBuSrvId(Map params);

    int getSequence(@Param("sequences") String sequences);

    int qryResConfigOrderNum(@Param("orderId") String orderId, @Param("type") String type);

    List<Map<String, Object>> qryCustInfo(String  cstOrdId);

    List<Map<String, Object>> qrySrvOrderInfo(String  cstOrdId);

    List<Map<String, Object>> qryDispatchOrderInfo(String  cstOrdId);

    List<Map<String, Object>> queryOrderCircuitInfo(Map params);

    List<Map<String,Object>> qrySrvOrdListCheck(Map<String, Object> params);

    List<Map<String, Object>> queryCircuitInfoByIds(Map<String, Object> params);

    /*
     * 操作抄送表
     * @author ren.jiahang
     * @date 2019/6/1 15:20
     * @param params
     * @return int
     */
    int addCC(List<Map<String, Object>> params);
    int delCC(Map<String, Object> params);
    int updateCC(Map<String, Object> params);
    int qryAbnormalOrderCount(Map param);

    List<Map<String, Object>> qryAbnormalOrder(Map param);

    List<Map<String,Object>> queryDispatchOrderInfoByIds(Map<String,Object> param);

    //void removeAttach(@Param("array") String[] delFilesList, @Param("woId") String woId);
    void removeAttach(@Param("arrayList") String[] delFilesList, @Param("woId") String woId);

    //    查询 电路信息 公共信息
    List<Map<String,Object>> queryCircuitInfo(@Param("orderId") String orderId, @Param("serviceId")String serviceId);
    //    查询 电路信息 AZ信息
    List<Map<String, Object>>  queryCircuitInfoAZ(@Param("orderId") String orderId, @Param("serviceId")String serviceId, @Param("stateLabel") String stateLabel);
    //    查询 电路信息 PE、CE信息
    List<Map<String, Object>>  queryCircuitInfoPE(@Param("orderId") String orderId, @Param("serviceId")String serviceId, @Param("stateLabel") String stateLabel);

    /**
     * 异常单流程流转说明 gom_bdw_log_info
     * @param params
     */
    void insertFlowLogInfo(Map params);

    /**
     * 查询对应的专业名称
     * @param codeValue
     * @return
     */
    String qrySpecName(String codeValue);
    /**
     * 查询工单或子流程是否已经配置过资源信息
     * @param qryParamMap
     * @return
     */
    int qryIsResConfigNum(Map<String, Object> qryParamMap);
    int queryPostponementOrderCount(Map<String, Object> qryParamMap);
    List<Map<String, Object>> queryPostponementOrderInfo(Map<String, Object> param);
}
