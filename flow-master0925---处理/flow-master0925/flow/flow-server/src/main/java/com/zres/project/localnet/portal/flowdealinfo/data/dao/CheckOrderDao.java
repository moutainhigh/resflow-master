package com.zres.project.localnet.portal.flowdealinfo.data.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CheckOrderDao {

    /**
     * 查询核查调度环节的工单id
     * @param orderId
     * @return
     */
    Map<String, Object> qryCheckDispatchWoOrder(@Param("orderId") String orderId, @Param("tacheId") String tacheId);

    /**
     * 修改工单状态
     * @param updateMap
     * @return
     */
    int updateWoOrderStateByWoId(Map updateMap);

    /**
     * 查询所有专业核查的工单id
     * @param orderId
     * @return
     */
    List<String> qryALLSpecialCheckWoOrder(@Param("orderId") String orderId);

    /**
     * 查询最大的工单id
     * @param orderId
     * @return
     */
    String qryMaxWoId(String orderId);

    /**
     * 查询二干所有专业核查的工单id
     * @param orderId
     * @return
     */
    List<String> qrySecALLSpecialCheckWoOrder(@Param("orderId") String orderId);

    /**
     * 查询专业核查
     * @param orderId
     * @return
     */
    List<Map<String, Object>> qrySpecialData(@Param("orderId") String orderId);

    /**
     * 获取二干本地关联信息
     * @param orderId
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String, Object>> getSecLocalRelateInfo(@Param("orderId") String orderId);

    /**
     * 查询本地核查流程到核查等待环节的数量
     * @param orderId
     * @return
     */
    int qryLocalCheckAtWaitTacheNum(@Param("orderId") String orderId);

    /**
     * 查询二干本地核查环节
     * @param orderId
     * @return
     */
    Map<String, Object> qrySecLocalCheckTache(@Param("orderId") String orderId);

    /**
     * 查询专业核查环节某个区域有没有执行中的工单
     * @param orderId
     * @param tacheCode
     * @param areaId
     * @return
     */
    int qrySpecialtyCheckDoing(@Param("orderId") String orderId, @Param("tacheCode") String tacheCode, @Param("areaId") String areaId);
    /**
     * 查询已完成的专业核查
     * @param orderId
     * @return
     */
    List<Map<String, Object>> qryCompleteCheckSpecInfo(@Param("orderId") String orderId);

    /**
     * 查询单据归属省分
     * @param param
     * @return
     */
    Map<String, Object> queryProvinceName(Map<String, Object> param);

    /**
     * 查询电路调度环节，电路编号必填的省份配置
     * @return
     */
    Map<String, Object> queryProvinceConf();

    /**
     *根据ATTR_CODE查询电路属性表中是否有记录
     * @param param
     */
    Map<String, Object> queryCircuitCodeInfo(Map<String, Object> param);

    /**
     *根据attr_code更新attr_value
     * @param param
     */
    void updateCircuitCode(@Param("param") Map<String, Object> param);

    void insertCircuitInfo(@Param("param") Map<String, Object> param);

    List<Map<String, Object>> qryCurrentCompleteCheckSpecInfo(@Param("orderId")String orderId, @Param("tacheId")String tacheId);

    Map<String, Object> queryProvinceAutoConf(@Param("areaId")String areaId);
}
