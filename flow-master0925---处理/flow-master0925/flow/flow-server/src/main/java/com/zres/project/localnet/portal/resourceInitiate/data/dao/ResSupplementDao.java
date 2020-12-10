package com.zres.project.localnet.portal.resourceInitiate.data.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ResSupplementDao {

    /**
     * 待签收
     * @param params
     * @return
     */
    List<Map<String, Object>> qryWaitSignForOrdList(Map<String, Object> params);

    /**
     * 待签收数量
     * @param params
     * @return
     */
    int qryWaitSignForOrdCount(Map<String, Object> params);

    /**
     * 处理中
     * @param params
     * @return
     */
    List<Map<String, Object>> qryDealWithOrdList(Map<String, Object> params);

    /**
     * 处理中数量
     * @param params
     * @return
     */
    int qryDealWithOrdCount(Map<String, Object> params);

    /**
     * 已完成
     * @param params
     * @return
     */
    List<Map<String, Object>> qryCompletedOrdList(Map<String, Object> params);

    /**
     * 已完成数量
     * @param params
     * @return
     */
    int qryCompletedOrdCount(Map<String, Object> params);



}