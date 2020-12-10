package com.zres.project.localnet.portal.subIndexPage.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @Classname SubIndexPageDao
 * @Description 首页工作台
 * @Created by CWY
 * @Date 2019-08-09 11:16
 */
@Repository
public interface SubIndexPageDao {
    /**
     * 查询登录信息
     * 
     * @param userId
     * @return
     */
    public Map<String, Object> getOperStaffInfo(@Param("userId") Integer userId);

    /**
     * 查询月绩效工单
     * 
     * @param userId
     * @return
     */
    List<Map<String, Object>> getMonthWorkChartData(@Param("userId") String userId);

    /**
     * 查询工单时间状态分布
     * 
     * @param userId
     * @return
     */
    List<Map<String, Object>> getWorkOrderDistributeChartData(@Param("userId") String userId);
}
