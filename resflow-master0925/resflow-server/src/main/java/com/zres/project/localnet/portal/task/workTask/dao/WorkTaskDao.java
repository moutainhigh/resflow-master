package com.zres.project.localnet.portal.task.workTask.dao;


import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

/**
 * @author sunlb
 */
@Repository
public interface WorkTaskDao {

    /**
     * 预警(84000002)查询sql(已签收、未签收，派发类型:个人)
     * @param params
     * @return
     */
   public List<Map<String,Object>> queryWarnWoByDealUIdAndByPId(Map<String, Object> params);

    /**
     * 超时(84000003)查询sql(已签收、未签收，派发类型:个人)
     * @param params
     * @return
     */
   public List<Map<String,Object>> queryTimeOutWoByDealUIdAndByPId(Map<String, Object> params);

    /**
     * 预警(84000002)查询sql(未签收，派发类型:岗位)
     * @param params
     * @return
     */
   public List<Map<String,Object>> queryWarnWoByPost(Map<String, Object> params);

    /**
     * 超时(84000003)查询sql(未签收，派发类型:岗位)
     * @param params
     * @return
     */
   public List<Map<String,Object>> queryTimeOutWoByPost(Map<String, Object> params);




}
