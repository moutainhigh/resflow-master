package com.zres.project.localnet.portal.collect.data;

import java.util.List;
import java.util.Map;

public interface MonitorCollectDao {

    /**
     * 查询采集sql
     * @return
     */
    String qryCollectSql(String sqlSign);

    /**
     * 查询数据
     * @param sql
     * @return
     */
    List<Map<String, Object>> qryData(String sql);

    /**
     * 查询采集sql
     * @return
     */
    String qryResDisassemble(String sqlSign);

}
