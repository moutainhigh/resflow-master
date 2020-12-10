package com.zres.project.localnet.portal.flowdealinfo.data.dao;

import java.util.List;
import java.util.Map;

/**
 * Created by tang.huili on 2019/3/5.
 */
public interface EventListenerDao {

    // 根据工单id查询派发对象,及所属区域
    List<Map<String,Object>> queryDispInfo(Map<String, Object> param);

    // 入库工单区域表
    void insertWoDept(Map<String, Object> dispMap);
}
