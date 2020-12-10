package com.zres.project.localnet.portal.subIndexPage.service;

import java.util.Map;

/**
 * @Classname SubIndexPageServiceIntf
 * @Description 首页工作台页面接口
 * @Created by cwy
 * @Date 2019-08-09 11:09
 */
public interface SubIndexPageServiceIntf {
    /**
     * 获取登陆信息
     *
     * @return Map
     */
    Map<String, Object> queryStaffInfo(Map<String, Object> map);

    /**
     * 查询月绩效工单量
     *
     * @param map
     * @return
     */
    Map<String, Object> queryMonthWorkChart(Map<String, Object> map);

    /**
     * 查询工单时间状态分布
     *
     * @param map
     * @return
     */
    Map<String, Object> queryWorkOrderDistributeChart(Map<String, Object> map);

}

