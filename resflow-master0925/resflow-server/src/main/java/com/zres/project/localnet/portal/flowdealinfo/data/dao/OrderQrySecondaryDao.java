package com.zres.project.localnet.portal.flowdealinfo.data.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.poi.hmef.attribute.MAPIAttribute;
import org.springframework.stereotype.Repository;

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
     * 查询单子来源 -- 二干子流程
     *   本地网：localBuild
     *      *   一干：onedry
     *      *   二干：secondary
     * @param orderId
     * @return
     */
    public Map<String, Object> qrySrvOrderSourceSec(String orderId);

    /**
     * 查询二干启的本地网跨域流程到起租环节数量
     * @param orderId
     * @param tacheId
     * @return
     */
    public int qryRentOrTestTacheNum(@Param("orderId") String orderId, @Param("tacheId") String tacheId);

    /**
     * 查询二干启的本地网跨域全程调测环节的数量
     * @param orderId
     * @param tacheId
     * @return
     */
    public int qryRentOrTestCrossTacheNum(@Param("orderId") String orderId, @Param("tacheId") String tacheId);

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
     * 获取二干本地调度的工单
     * @param orderId
     * @return
     */
    public Map<String, Object> qrySecLocalOrder(String orderId);

    /**
     * 查询跨域全程调测环节的工单--二干调度
     * @param orderId
     * @return
     */
    public Map<String, Object> qrySecTestTacheOrder(String orderId);

    /**
     * 查询调度信息
     * @param orderId
     * @return
     */
    public Map<String, Object> qryDispatchData(String orderId);

    /**
     * 查询二干调度环节处理人
     * @param orderId
     * @return
     */
    public Map<String, Object> qryDispatchTacheCompUser(String orderId);

    /**
     * 查询二干调度环节的工单id
     * @param orderId
     * @return
     */
    public Map<String, Object> qryDispatchTacheWoId(String orderId);

    /**
     * 二干发往本地的单子，关联业务信息入库
     * @param param
     * @return
     */
    public int insertSecLocalRelate(Map<String, Object> param);

    /**
     * 二干发往本地的单子，查询之前是否有数据
     * @param param
     * @return
     */
    public int selectSecLocalRelate(Map<String, Object> param);

    /**
     * 二干发往本地的单子,更新关联业务信息
     * @param param
     * @return
     */
    public int updateSecLocalRelate(Map<String, Object> param);

    /**
     * 查询本地网起租环节的工单
     * @param orderId
     * @return
     */
    public List<Map<String, Object>> qryRentTacheOrder(String orderId);

    /**
     * 查询本地网主调方的起租环节的工单数据
     * @param orderId
     * @return
     */
    public Map<String, Object> qryRentTacheLocal(String orderId);

    /**
     * 查询数据制作所起子流程
     * @param param
     */
    public List<Map<String,Object>> qrySecondDataMake(Map<String, Object> param);

    /**
     * 查询资源施工所起子流程
     * @param param
     */
    public List<Map<String,Object>> qrySecondResMake(Map<String, Object> param);

    /**
     * 查询二干下发本地网流程
     * @param param
     */
    public List<Map<String,Object>> qrySecToLocalData(Map<String, Object> param);

    /**
     * 查询专业数据制作和本地调度环节的数量
     * @param param
     * @return
     */
    //public int qryTacheNum(Map<String, Object> param);

    /**
     * 查询工单是否为回退工单
     * @param woId
     * @return
     */
    public Map<String, Object> qryWoOrderIfBack(String woId);

    /**
     * 查询电路是否起草过调单
     * @param srvOrdId
     * @return
     */
    public int qryOrderIfConfigDispatch(@Param("srvOrdId") String srvOrdId);

    /**
     * 查询表gom_ord_key_info---用户派发规则
     * @param orderId
     * @return
     */
    //public Map<String, Object> qryOrderKeyInfo(@Param("orderId") String orderId); 迁移至DispObjDao

    /**
     * 查询线条参数--新建申请单环节到单
     * @param orderId
     * @return
     */
    public String qryAttrParams(@Param("orderId") String orderId, @Param("flagKey") String flagKey);

    /**
     * 查询派发对象
     * @param param
     * @return
     */
    //public String qryDispObj(Map<String, Object> param); 迁移至DispObjDao

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
     * 查询审核对象--二干调度局内电路
     * @param orderId
     * @return
     */
    public String qryAutitObj(@Param("orderId") String orderId);

    /**
     * 查询电路编号
     * @param params
     * @return
     */
    public Map<String, Object> qryCircuitNum(Map<String, Object> params);

    /**
     * 查询电路编号
     * @param params
     * @return
     */
    public String qryCircuitIfConfig(Map<String, Object> params);

    /**
     * 插入电路编号   ---集客来单
     * @param params
     * @return
     */
    public int insertCircuitNum(Map<String, Object> params);

    /**
     * 插入电路编号  ---本地发起
     * @param params
     * @return
     */
    public int updateCircuitNum(Map<String, Object> params);

    /**
     * 查询二干下发本地的单子 所有子流程到等待环节的工单id
     * @param orderId
     * @return
     */
    public List<Map<String, Object>> qrySecToLocalChildFlowDate(String orderId);

    /**
     * 查询需要抄送对象
     * @param srvOrdId
     * @return
     */
    public Map<String, Object> qryCopySendObj(String srvOrdId);
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
     * 查询单子来源 -- 二干主流程
     *   本地网：localBuild
     *      *   一干：onedry
     *      *   二干：secondary
     * @param orderId
     * @return
     */
    public String qrySrvOrderSource(@Param("orderId") String orderId);

    /**
     * 查询单子来源 -- 由二干下发的
     *   本地网：localBuild
     *      *   一干：onedry
     *      *   二干：secondary
     * @param orderId
     * @return
     */
    public String qrySrvOrderSourceFromSec(@Param("orderId") String orderId);

    /**
     * 查询子流程是否存在，以及子流程订单状态
     * @param orderId
     * @param specialtyCode
     * @return
     */
    public String qryChildOrderState(@Param("orderId") String orderId,
                                     @Param("specialtyCode") String specialtyCode,
                                     @Param("subName") String subName);

    /**
     * 查询父订单正在执行中的子流程
     * @param orderId
     * @return
     */
    public int qryChildOrderDealing(@Param("orderId") String orderId);

    /**
     * 查询下发本地正在执行中的子流程
     * @param orderId
     * @return
     */
    public int qryToLocalChildOrderDealing(@Param("orderId") String orderId);

    /**
     * 查询二干下发到本地网的子流程是否存在，以及其订单状态
     * @param orderId
     * @param regionId
     * @return
     */
    public String qryToLocalChildOrderState(@Param("orderId") String orderId,
                                            @Param("regionId") String regionId);

    /**
     * 查询正在执行中的子流程是什么环节的
     * @param orderId
     * @param woState
     * @return
     */
    public Map<String, Object> qryDealingChildFlowTache(@Param("orderId") String orderId,
                                                        @Param("woState") String woState);

    /**
     * 二干下发到本地网得order_id
     * @param
     * @return
     */
    public List<Map<String, Object>> querySec2LocalInfo(String orderId);

    /**
     * 二干下发到本地网得order_id
     * @param
     * @return
     */
    public List<Map<String, Object>> queryOrderIdList(String orderId);

    /**
     * 查询二干下发到本地电路调度环节工单
     * @param srvOrdId
     * @return
     */
    public List<Map<String, Object>> querySecToLocalOrderCircuitDispatch(String srvOrdId);

    /**
     * 查询该单子是否从二干发过来的
     * @param orderId
     * @return
     */
    public Map<String, Object> qryIfFromSecondary(String orderId);

    /**
     * 查询二干资源分配环节
     * @param orderId
     * @return
     */
    public List<Map<String, Object>> qrySourceDispatch(String orderId);

    /**
     * 查询二干资源分配下发的专业
     * @param orderId
     * @return
     */
    public List<Map<String, Object>> qrySourceDispatchSpecialty(String orderId);

    /**
     * 查询对应专业资源分配完成环节工单
     * @param qryParams
     * @return
     */
    public Map<String, Object> qrySourceDispatchFinishWoOrder(Map<String, Object> qryParams);

    /**
     * 起草调单--查询主调区域下发本地网定单状态
     * @param qryParams
     * @return
     */
    public Map<String, Object> qryMainAreaOrderState(Map<String, Object> qryParams);

    /**
     * 查询二干资源分配处理中的工单数量
     * @param orderId
     * @return
     */
    public int qrySourceDispatchDealingWoOrderNum(String orderId);

    /**
     * 入库工单正向反向标识--插入工单操作属性表
     * @param woOrderParams
     * @return
     */
    public int insertWoOrderOper(Map<String, Object> woOrderParams);
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
}
