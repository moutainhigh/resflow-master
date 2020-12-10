package com.zres.project.localnet.portal.applpage.service;

import java.util.List;
import java.util.Map;

/**
 * @author :ren.jiahang
 * @date:2019/2/15@time:10:35
 */

public interface GetTreeIntf {
    public List<Map<String, Object>> queryProvienceTree(Map<String, Object> deMap);
    public Map<String, Object> queryAreaIdByName(String param);
    public Map<String, Object> queryOperStaffInfo();
}
