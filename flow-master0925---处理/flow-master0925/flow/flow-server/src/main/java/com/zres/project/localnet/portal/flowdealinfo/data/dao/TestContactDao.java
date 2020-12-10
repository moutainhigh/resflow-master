package com.zres.project.localnet.portal.flowdealinfo.data.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @ClassName TestContactDao
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/11/17 17:31
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@Repository
public interface TestContactDao {

    int countTestContactList(Map<String,Object> params);

    /**
     * @Description 功能描述: 查询带查询条件数据
     * @Param: [params]
     * @Return: java.util.List<java.util.Map>
     * @Author: wang.gang2
     * @Date: 2020/11/17 19:53
     */
    List<Map> queryTestContactInfo(Map<String,Object> params);

    /**
     * @Description 功能描述: 查询2级地市
     * @Param: [params]
     * @Return: java.util.List<java.util.Map>
     * @Author: wang.gang2
     * @Date: 2020/11/17 19:52
     */
    List<Map> queryTestContactList(Map<String,Object> params);

}
