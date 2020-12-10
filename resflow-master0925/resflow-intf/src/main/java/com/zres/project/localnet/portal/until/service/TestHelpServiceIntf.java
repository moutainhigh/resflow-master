package com.zres.project.localnet.portal.until.service;

import java.util.List;
import java.util.Map;

/**
 * @author :ren.jiahang
 * @date:2019/8/8@time:14:46
 */
//资源回滚接口
public interface TestHelpServiceIntf {
    public String businessRollback(Map<String ,Object> map);
    public String delApplicationByApplCode(String param);
    public List<Map<String, Object>> getApplicationLog();
}
