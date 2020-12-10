package com.zres.project.localnet.portal.initApplOrderDetail.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;


/**
 * Created by lu on 2019/1/4 0004.
 */
@Repository
public interface EditDraftDao {
    /**
     * 查询被选中行的业务信息、客户信息
     */

    Map<String, Object> querySelectedServInfo(String CustId);


    /**
     * 根据客户ID 查询 业务iD
     *
     * @param CustId
     * @return
     */
    List<String> querySrvOrdIdByCustId(@Param("CustId") String CustId,@Param("OrderState") String OrderState);
    /**
     * 根据申请单编号查询 业务CustOrdId
     *
     * @param applicationCode
     * @return
     */
    List<String> queryCustOrdIdByApplicationCode(@Param("applicationCode") String applicationCode);

    /**
     * 根据业务订单ID  查询 电路信息
     *
     * @param SrvOrdId
     * @return
     */
    List<Map<String, Object>> queryCircuitInfoBySrvId(@Param("SrvOrdId") String SrvOrdId,@Param("CgFlag") String CgFlag);

    /**
     * 根据业务订单ID查询申请单ID  Trade_Id
     * @param SrvOrdId
     * @return
     */
    Map<String, Object> queryTradeIdBySrvOrdId(String SrvOrdId);
    /**
     * 根据电路id  查询 电路信息
     *
     * @param SubscribeId
     * @return
     */
    List<Map<String,Object>> queryCircuitInfoBySubscribeId(String SubscribeId);
    /**
     * 根据电路id  查询 电路信息
     *
     * @param SubscribeId
     * @return
     */
    List<Map<String,Object>> queryCircuitById(String SubscribeId);
    /**
     * 根据定单编号  查询 电路信息
     *
     * @param srvOrdId
     * @return
     */
    List<Map<String,Object>> queryCircuitInfoById(String srvOrdId);

    Map<String,Object> queryCustInfoByAppId(String appId);
}
