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
    public Map<String,Object> queryLocalInfoByWoId(String woId);

    void deleteInfoByWoId(@Param("woId")String woId);

    void insertCheckInfo(Map<String, Object> params);

    Map<String,Object> queryInfoByTacheId(@Param("srvOrdId") String srvOrdId, @Param("tacheId") String tacheId);

    void deleteInfoBytacheId(@Param("srvOrdId") String srvordId, @Param("tacheId") String tacheId);
    //汇总本地各区域的核查汇总信息
    Map<String,Object> queryInfoBySrvOrdId(String srvOrdId);
    //查询二干发往本地核查单子汇总的金额
    Map<String,Object> queryInvestmentAmountBySrvOrdId (String srvOrdId);

    Map<String,Object> queryLocalInfoBySrvOrdId(String srvOrdId);

    Map<String,Object> querySchmeBySrvOrdId(String srvOrdId);

    Map<String,Object> queryLocalSchmeBySrvOrdId(String srvOrdId);

    Map<String,Object> queryAccessRoom(String srvOrdId);

    List<Map<String,Object>> qryCheckInfoHis(String srvOrdId);

    int queryNum(Map<String,Object> param);

    String qryAreaName(String areaId);

    void insertCheckInfoA(Map<String, Object> checkInfoMap);

    void insertCheckInfoZ(Map<String, Object> checkInfoMap);

    String qryDepartment(String srvOrdId);

    Map<String,Object> queryInfoByTacheIdTwo(@Param("srvOrdId") String srvOrdId, @Param("tacheId") String tacheId);

    //根据电路主键删除反馈信息
    void deleteInfoBySrvOrdId(@Param("srvOrdId") String srvordId);
    Map<String,Object> qryLastNodeInfo(String orderId);
    List<Map<String,Object>> qryFinishNodeList(String orderId);
    Map<String,Object> qryLastTotalNode(String orderId);
}
