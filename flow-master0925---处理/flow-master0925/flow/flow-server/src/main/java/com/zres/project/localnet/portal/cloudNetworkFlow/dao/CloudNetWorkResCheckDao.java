package com.zres.project.localnet.portal.cloudNetworkFlow.dao;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface CloudNetWorkResCheckDao {

    List<Map<String, Object>> qrycircuitInfo(Map<String, Object> param);

    Map<String, Object> querySpecialAreaInfo(Map<String, Object> param);

    void saveSpecialArea(Map<String, Object> param);

    void updateWoState(@Param("woId") String woId, @Param("state") String state);

    Map<String, Object> querySrvOrdInfo(String orderId);

    int queryCheckChildWoOrder(String orderId);

    String queryMainWoId(String orderId);

    void updateWoStateByWoId(@Param("woId") String woId, @Param("state") String state);

    void updateSpecialArea(Map<String, Object> param);

    Map<String, Object> queryCheckInfo(@Param("srvOrdId") String srvOrdId, @Param("woId") String woId, @Param("tacheId") String tacheId);

    void updateCheckInfo(Map<String, Object> param);

    void insertIntoCheckInfo(Map<String, Object> param);
}
