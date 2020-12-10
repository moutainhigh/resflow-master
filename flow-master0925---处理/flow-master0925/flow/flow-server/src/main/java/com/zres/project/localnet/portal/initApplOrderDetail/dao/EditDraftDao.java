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
     * 业务订单属性信息历史
     * @param SrvOrdId
     * @return
     */
    List<Map<String, Object>> getAttrMsgInfo(String SrvOrdId);

    List<Map<String, Object>> getAttrFileMsgInfo(String SrvOrdId);

    /**
     * 根据业务订单ID查询互联网报竣信息
     * @param SrvOrdId
     * @return
     */
    Map<String, Object> getInternetAttrUserTime(String SrvOrdId);

    /**
     * 根据业务订单ID查询以太网或SDH报竣信息
     * @param SrvOrdId
     * @return
     */
    Map<String, Object> getEthernetOrSdhAttrUserTime(String SrvOrdId);

    /**
     * 获取业务订单信息
     * @param srvOrdId
     * @return
     */
    Map<String, Object> getSrvOrdInfo(String srvOrdId);

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
    /**
     * 根据申请单编号查询 业务CustOrdId
     *
     * @param applicationCode
     * @return
     */
    List<String> queryCustOrdIdByApplicationCode(@Param("applicationCode") String applicationCode);

}
