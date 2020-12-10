package com.zres.project.localnet.portal.order.service;

import java.util.List;
import java.util.Map;

/**
 * @author :wang.g2
 * @description :定单查询
 * @date : 2019/4/19
 */
public interface OrderQueryListServiceIntf {
    /**
     * 定单查询
     * @param params
     * @return
     */
    public Map<String, Object> queryOrderList(Map<String, Object> params);

    public List<Map<String, Object>> exportrderList(Map<String, Object> params);



}
