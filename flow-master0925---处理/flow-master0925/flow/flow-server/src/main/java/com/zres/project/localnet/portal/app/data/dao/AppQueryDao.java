package com.zres.project.localnet.portal.app.data.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @ClassName AppQueryDao
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/6/8 17:00
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@Repository
public interface AppQueryDao {
    /**
     * app 查正常 超时预警单子数量
     * @param staffId
     * @return
     */
    public  Map<String, Object>  countOrderNum(@Param("staffId")String staffId);

    /**
     *  app 工单查询
     * @param params
     * @return
     */
    public List<Map<String, Object>> queryWorkOrders(Map<String,Object> params);


    /**
     * app 流程跟踪--工单处理日志信息查询（主要用来点亮流程图标）
     * @param params
     * @return
     */
    public List<Map<String, Object>> queryWoOrderLog(Map<String,Object> params);

}
