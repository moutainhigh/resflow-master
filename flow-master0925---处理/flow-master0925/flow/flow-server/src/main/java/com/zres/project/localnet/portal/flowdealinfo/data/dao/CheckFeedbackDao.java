package com.zres.project.localnet.portal.flowdealinfo.data.dao;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Created by tang.huili on 2019/3/5.
 */
public interface CheckFeedbackDao {

    public Map<String,Object> queryInfoByWoId(String woId);
    public Map<String,Object> queryCheckFeedBackInfoByWoId(String woId);
    public List<Map<String,Object>> queryCheckFeedBackInfoByWoIdList(@Param("woIdList") List<String> woIdList);
    void deleteInfoByWoId(String woId);

    void insertCheckInfo(Map<String, Object> params);

    Map<String,Object> queryInfoByTacheId(@Param("srvOrdId") String srvOrdId, @Param("tacheId") String tacheId);

    Map<String,Object> queryInfoByTacheIdTwo(@Param("srvOrdId") String srvOrdId, @Param("tacheId") String tacheId);

    void updateStateBytacheId(@Param("srvOrdId") String srvordId, @Param("tacheId") String tacheId);

    Map<String,Object> queryInfoBySrvOrdId(String srvOrdId);

    Map<String,Object> querySchmeBySrvOrdId(String srvOrdId);

    Map<String,Object> queryAccessRoom(String srvOrdId);

    List<Map<String,Object>> qryCheckInfoHis(String srvOrdId);

    int queryNum(String srvOrdId);

    String qryAreaName(String areaId);

    void insertCheckInfoA(Map<String, Object> checkInfoMap);

    void insertCheckInfoZ(Map<String, Object> checkInfoMap);

    /**
     * 查询工建系统反馈的费用信息
     * @param srvOrdId
     * @return
     */
    Map<String,Object> qryEnginInfo(String srvOrdId);

    void deleteInfoBySrvOrdId(String srvOrdId);

    Map<String,Object> qryLastNodeInfo(String orderId);
    List<Map<String,Object>> qryFinishNodeList(String orderId);
    Map<String,Object> qryLastTotalNode(String orderId);

}
