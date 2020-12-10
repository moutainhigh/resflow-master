package com.zres.project.localnet.portal.until.data.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

/**
 * @author :ren.jiahang
 * @date:2019/8/12@time:16:45
 */

@Repository
public interface TestHelpDao {
    int insertDelApplicationLog(Map<String,Object> param);
    List<Map<String, Object>> getApplicationLog();
}
