package com.zres.project.localnet.portal.webservice.data.dao;


import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jiangdebing on 2018/12/22.
 */
@Repository
public interface WebServiceDao {

    String selectActType(@Param("typeCode") String typeCode);

    void bachAddGomIdcSrvOrdAttrInfo(@Param("srvOrdAttrList") List<Map> srvOrdAttrList);

    String selectDeptId(@Param("areaCode") String areaCode);
    int selectValeByProvince(@Param("provinceA") String provinceA);
    int selectValeByProvinceForSdwan(@Param("role_province") String role_province);
    int selectValeByServiceId(@Param("service_id_real") String service_id_real);

    int querySequence(@Param("sequences") String sequences);
    void insertInterfLog(Map<String, Object> map);
    void updateInterfLog(Map<String, Object> map);
    void insertClobInfo(Map<String, Object> map);
    void insertInterfLogS(Map<String, Object> map);
    /**
     * 查询资源接口地址
     * Created by csq on 2018/12/25.
     */
    String queryUrl(@Param("type") String type);
    /**
     * 查询接口报文业务信息
     * Created by csq on 2018/12/25.
     */
    Map queryResInfo(@Param("id") String id);
    /**
     * 查询接口报文业务信息二干下发本地单子
     * Created by csq on 2019/05/31.
     */
    Map queryResInfoSecond(@Param("id") String id,@Param("orderId") String orderId);
    /**
     * 查询接口报文业务信息 --从二干下发的单子
     * @param id
     * @return
     */
    Map queryResInfoFromSec(@Param("id") String id);

    /**
     *  二干派往本地单子添加单独的woid 区分
     * @param map
     * @return
     */
    Map queryResInfo2(Map<String, Object> map);
    /**
     * 查询接口报文电路信息
     * Created by csq on 2018/12/27.,
     */
    List<Map<String, Object>> queryCirInfo(@Param("id") String id);
    /**
     * 报文入库
     * Created by csq on 2018/12/25.
     */
    void saveJson(Map<String, Object> map);

     void insertCstOrd(Map<String, Object> cstordMap);
     Map queryCodeInfo(@Param("codeType") String codeType, @Param("codeValue") String codeValue);
     void updateLogOrderNo(Map<String, Object> map);
     void updateOrderReqFinDate(Map<String, Object> map);
     void addGomIdcSrvOrdInfo(Map<String, Object> srvOrdInfo);

     void addGomIdcSrvOrdAttrInfo(Map<String, Object> srvOrdAttr);

     void addGomIdcSrvOrdAttrGrp(Map<String, Object> srvOrdGrp);

     void addGomIdcSrvOrdAttrGrpInfo(Map<String, Object> srvOrdAttrGrpInfo);

     void addGomIdcSrvOrdAttachInfo(Map<String, Object> attachInfo);

     List<Map<String, Object>> qryOrdPsId(Map<String, Object> map);
    /**
     * 报文数据入库
     * Created by csq on 2018/12/25.
     */
    void saveRetInfo(Map<String, Object> map);
    /**
     * 查询资源返回信息
     * Created by csq on 2018/12/27.
     */
    Map queryResRTInfo(@Param("id") String id, @Param("attr_action") String attr_action);

    /**
     * 查询SrvOrdId
     * Created by csq on 2018/1/7.
     */
    String querySrvOrdId(@Param("id") String id);

    public void deleteRes(@Param("srvOrdId") String srvOrdId,@Param("compId") String compId);

    /**
     * 配置资源返回信息入库
     * @param map
     */
    public void saveRes(Map<String, Object> map);
    /**
     * 根据order_id查询流程订单的属性
     * @param orderId
     * @return
     */
    public Map<String,Object> qryGomOrderAttr(@Param("orderId") String orderId,@Param("attrId") String attrId);

    /**
     * 受理区域：白沙县分公司--查找到海南省的org_id
     * @param regionCodeName
     * @return
     */
    String qryRegionCode(String regionCodeName);

    String qryCrmRegion(String id);

    String qryResCircuitCode(String srvOrdId);

    void updateResCircuitCode(@Param("attrInfoId") String attrInfoId, @Param("circuitNo") String circuitNo);

    void deleteResCircuitCode(@Param("srvOrdId") String srvOrdId);

    void deleteResAttrCode(@Param("srvOrdId") String srvOrdId, @Param("interfaceName") String interfaceName);
    void deleteResAttr(@Param("srvOrdId") String srvOrdId, @Param("attrCode") String attrCode);

    void deleteResRoute(@Param("srvOrdId")String srvOrderId, @Param("compId") String compId);

    void saveResRoute(Map<String, Object> map);

    void copyInterfInfo(@Param("srvOrdId") String srvOrdId, @Param("interfaceName") String interfaceName);

    void updateStateBySrvOrdId(String srvOrdId);

    void saveResCircuit(Map<String, Object> paramMap);

    void updateCirCodeBySrvOrdId(@Param("srvOrdId") String srvOrdId, @Param("circuitNo") String circuitNo);

    String qrySrvOrdIdByTradeId(@Param("tradeId") String tradeId,@Param("flowId") String flowId);

    List qryTacheInfo(@Param("orderId") String orderId);

    Map qryFinishOrdInfo(@Param("srvOrdId") String srvOrdId);

    int qryNumByOrderId(@Param("srvOrdId") String srvOrdId,@Param("orderId") String orderId);

    String qryCityName(@Param("cityCode") String cityCode);

    Map<String, Object> queryCodeInfoFromRelateTable(@Param("serviceId") String serviceId, @Param("codeType") String codeType);

    /**
     * 删除已配置的资源信息
     * @param srvOrdId
     */
    void deleteResInfoBySrvOrdId(String srvOrdId);

    /**
     * 删除已配置的资源路由信息
     * @param srvOrdId
     */
    void deleteResRouteBySrvOrdId(String srvOrdId);

    /**
     * 更新产品实例id
     * @param srvOrdId
     * @param prodInstId
     */
    void updateInstanceId(@Param("srvOrdId") String srvOrdId, @Param("prodInstId") String prodInstId);

    String qryRentTime(@Param("srvOrdId")String srvOrdId);

    /**
     * 资源补录查询报文参数
     * @param id
     * @return
     */
    Map queryResInfoBySupplement(@Param("id") String id);

    public void deleteResSupple(@Param("srvOrdId") String srvOrdId,@Param("compId") String compId);

    /**
     * 配置资源返回信息入库--资源补录
     * @param map
     */
    public void saveResSupple(Map<String, Object> map);
    void deleteResRouteSupple(@Param("srvOrdId")String srvOrderId, @Param("compId") String compId);

    void saveResRouteSupple(Map<String, Object> map);

    /**
     * 通過 業務號碼和業務訂單號查詢srvOrdId
     * @param tradeId
     * @param serialNumber
     * @return
     */
    List<Map<String,Object>> querySrvOrderList(@Param("tradeId") String tradeId, @Param("serialNumber") String serialNumber);


    /**
     * 查询上次竣工的调度单
     * @param instanceId
     * @return
     */
    List<Map<String,Object>> qrySrvOrdIdLast(@Param("instanceId") String instanceId);

    /**
     * 查询是否是补录单
     * @param srvOrdId
     * @return
     */
    int isResSupplement(@Param("srvOrdId")String srvOrdId);

    /**
     * 查询电路属性
     * @param srvOrdId
     * @return
     */
    List<Map<String,Object>> queryCircuitAttr(@Param("srvOrdId") String srvOrdId);

    /**
     * sd_wan 产品 工单反馈接口只有订单编号关联
     * @param tradeId
     * @return
     */
    List<Map<String,Object>> querySrvOrderByTradeId(@Param("tradeId") String tradeId);


    List<Map<String,Object>> qrySrvOrdIdSecLast(String prodinstid);


    /**
     * ADD by wang.gang2
     * 通过申请单编号查询接口日志需要转发给工建系统
     * @param cstOrdId
     * @return
     */
    List<Map<String,Object>> queryInterfaceLog(@Param("cstOrdId")String cstOrdId,@Param("srvOrdId")String srvOrdId);

    /**
     * ADD by wang.gang2
     * 通过srvOrdId查客户信息
     * @param srvOrdId
     * @return
     */
    Map<String,Object> queryCustInfo(String srvOrdId);

    /**
     * 添加建设完工推送记录
     * @param pushOrderList
     */
    void addProgressFinishInfo(@Param("pushOrderList") List<Map<String,Object>> pushOrderList);

    /**
     * 完工推送记录查询
     * @param srvOrdId
     * @return
     */

    Map<String,Object> queryProgressFinsh(@Param("srvOrdId")String srvOrdId);

    /**
     *
     * @param srvOrdId
     * @return
     */
    List<Map<String,Object>> queryWoInfoBysrvOrdId(@Param("srvOrdId") String srvOrdId,@Param("woId")String woId);

    void deleteResRoutePosion(@Param("srvOrdId")String srvOrderId, @Param("compId") String compId, @Param("position") String position);

    void saveResRoutePosition(Map<String, Object> map);

    /**
     * 根据srvOrdId查询流程id
     * @param srvOrdId
     * @return
     */
    Map<String, Object> qryOrdPsIdBySrvOrdId(@Param("srvOrdId")String srvOrdId);

    String queryResConfigUrl(@Param("type")String type, @Param("value") String value);

    /**
     * @Description 功能描述: 下发工建开通但记录
     * @Param: [cstOrdId, srvOrdId]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wang.gang2
     * @Date: 2020/9/21 10:27
     */
    List<Map<String,Object>> queryInterfaceLogs(@Param("cstOrdId")String cstOrdId,@Param("srvOrdId")String srvOrdId);

    /**
     * 根据userId查询
     * @param userCode
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    Map<String,Object> queryStaffidOrderInfo(@Param("userCode")String userCode);

    /**
     * 查询自动化核查的报文参数
     * @param srvOrdId
     * @return
     */
    Map<String, Object> queryAutoChkInfo(@Param("srvOrdId")String srvOrdId);

    /**
     *  自动化核查结果更新入库
     * @param cirResCheckMap
     */
    void updateChkCode(Map<String, Object> cirResCheckMap);

    /**
     * 保存自动化核查信息
     * @param chkResMap
     */
    void insertAutoChk(Map<String, Object> chkResMap);

    /**
     * 激活发单反馈报文数据入库
     * Created by wangsen on 2020/10/18.
     */
    void saveActivateInfo(Map<String, Object> map);
    /*
     * 删除对应字段属性
     * @Param:
     * @Return:
     */
    void delAttrBySrvOrdIdAndCode(@Param("srvOrdId") String srvOrdId, @Param("attrName") String attrName, @Param("attrCode") String attrCode);

    /**
     * @Description 功能描述: 工建环节推送记录
     * @Param: [tacheList]
     * @Return: void
     * @Author: wang.gang2
     * @Date: 2020/12/4 15:26
     */
    void bachAddConstructTacheInfo(@Param("tacheList") List<Map<String, Object>> tacheList);

}
