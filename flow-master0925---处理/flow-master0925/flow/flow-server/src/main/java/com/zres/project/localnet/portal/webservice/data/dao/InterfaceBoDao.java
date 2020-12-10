package com.zres.project.localnet.portal.webservice.data.dao;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Created by tang.huili on 2019/6/12.
 */
public interface InterfaceBoDao {

    void addGomAddProdInfo(Map<String,Object> param);

    void bachAddGomAddProdAttrInfo(List<Map<String, Object>> addAttrList);

    /**
     * 根据srvOrdId查询最后一个工单id
     * @param srvOrdId tacheCode
     * @return
     */
    Map<String,Object> queryWoIdBySrvOrdId(@Param("srvOrdId") String srvOrdId,@Param("tacheCode") String tacheCode);

    List<Map<String, Object>> getAttrFileMsgInfo(@Param("srvOrdId") String srvOrdId);

    /**
     * 查询核查单反馈接口需要的A、Z端信息
     * @param woId
     * @param type
     * @return
     */
    List<Map<String,Object>> getCheckBackInfo(@Param("woId") String woId, @Param("type") String type);

    /**
     * 查询通用信息 A端、Z端、全程（B）都需要
     * @param woId
     * @return
     */
    List<Map<String,Object>> queryGeneralInfo(@Param("woId") String woId);

    /**
     * 根据tradeid查询srvOrdId
     * @param tradeId
     * @return
     */
    Map<String,Object> querySrvInfoByTradeId(@Param("tradeId") String tradeId);

    /**
     * 查询起止租环节的woId和orderid
     * @param srvOrdId
     * @return
     */
    Map<String,Object> queryWoInfo(@Param("srvOrdId") String srvOrdId);

    /**
     * 集客编码与内部编码转换
     * @param attrCode
     * @param attrValue
     * @param serviceId
     * @return
     */
    List<Map<String,Object>> qryAttrInfo(@Param("attrCode")String attrCode, @Param("attrValue")String attrValue, @Param("oldAttrValue")String oldAttrValue, @Param("serviceId")String serviceId);

    /**
     * 核查单，查询核查汇总环节上传的附件
     * @param srvOrdId
     * @return
     */
    List<Map<String, Object>> getAttrFileMsgInfoCheck(@Param("srvOrdId") String srvOrdId);

    /**
     * 更新客户订单编码
     * @param
     */
    void updateApplyOrdName(@Param("cstOrdId")int cstOrdId, @Param("APPLY_ORD_NAME")String APPLY_ORD_NAME);

    List<String> qryActName(@Param("CODE_TYPE")String CODE_TYPE, @Param("CODE_VALUE")String CODE_VALUE);

    /**
     * 更新到接口日志表
     * @param srvOrdId
     * @param subscribeId
     */
    void updateSrvOrdId(@Param("srvOrdId")int srvOrdId, @Param("subscribeId")String subscribeId);

    Map<String,Object> qryCstOrdId(Map<String, Object> cstOrdInfo);

    /**
     * 更新到电路表
     * @param srvOrdId
     * @param orderId
     */
    void updateOrderIdBySrvOrdId(@Param("srvOrdId")int srvOrdId, @Param("orderId")String orderId);
    /**
     * 更新到电路表
     * @param srvOrdId
     * @param activeType
     */
    void updateActTypeBySrvOrdId(@Param("srvOrdId")int srvOrdId, @Param("activeType")String  activeType);

    void insertInterfLog(Map<String, Object> interflog);

    /**
     * 查询工建核查单反馈接口需要的A、Z端信息
     * @param woId
     * @param type
     * @return
     */
    List<Map<String,Object>> getConstructCheckInfo(@Param("woId") String woId, @Param("type") String type);

    /**
     * 查询AZ端核查标准化信息
     * @param woId
     * @param type
     * @return
     */
    List<Map<String, Object>> queryCheckStandInfo(@Param("woId") String woId, @Param("type") String type);
    /**
     * 查询汇总后的核查标准化信息
     * @param woId
     * @return
     */
    Map<String, Object> queryCheckStandAllInfo(@Param("woId") String woId);
}
