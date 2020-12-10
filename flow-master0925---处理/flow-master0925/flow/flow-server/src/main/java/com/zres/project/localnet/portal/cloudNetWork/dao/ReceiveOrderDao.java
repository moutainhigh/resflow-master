package com.zres.project.localnet.portal.cloudNetWork.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface ReceiveOrderDao {

    int insertIntoCstOrdInfo(Map<String, Object> param);

    int insertSrvOrdInfo(Map<String, Object> param);

    int insertSrvAttrInfo(@Param("params") List<Map<String, Object>> params);

    String queryFlowPsId(Map<String, Object> param);

    void updateOrderId(@Param("orderId") String orderId, @Param("srvOrdId") String srvOrdId);

    int insertAddProdInfo(Map<String, Object> param);

    int insertAddProdAttrInfo(@Param("params") List<Map<String, Object>> params);

    int batchInsertDeviceInfo(@Param("params") List<Map<String, Object>> params);

    int batchInsertPortInfo (@Param("params") List<Map<String, Object>> params);

    int insertAttrGrpInfo(Map<String, Object> param);

    int batchAttrGrpInfo(@Param("params") List<Map<String, Object>> params);
}
