package com.zres.project.localnet.portal.order.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import com.zres.project.localnet.portal.local.domain.PageInfo;
import com.zres.project.localnet.portal.order.data.dao.OrderQueryListDao;

/**
 * @author :wang.g2

 * @description :
 * @date : 2019/4/19
 */
@Service
public class OrderQueryListService implements  OrderQueryListServiceIntf {

    @Autowired
    private OrderQueryListDao orderQueryLListDao;
    @Override
    public Map<String, Object> queryOrderList(Map<String, Object> params) {
            Map<String, Object> map = new HashMap<String, Object>();
            List<Map<String, Object>> mapListT = new ArrayList<Map<String, Object>>();
            PageInfo pageInfo = new PageInfo(); //分页信息
            pageInfo.setIndexSizeData(params.get("pageIndex"), params.get("pageSize"));
            params.put("endRow", pageInfo.getRowEnd()); //分页结束行
            params.put("startRow", pageInfo.getRowStart()); //分页开始行
            params.put("userId", MapUtils.getString(params, "userId")); //当前登录用户id
            int woCount = orderQueryLListDao.countOrderList(params);
            if (woCount != 0) {
                List<Map<String, Object>> maps = orderQueryLListDao.queryOrderList(params);
                if (!CollectionUtils.isEmpty(maps)) {
                    mapListT.addAll(maps);
                }
            }
            pageInfo.setDataCount(woCount);
            map.put("dataLength", woCount);
            map.put("data", mapListT);
            map.put("flag", "1");
            map.put("pageIndex", pageInfo.getCurrentPage());
            map.put("rowNum", pageInfo.getPageSize());
            map.put("total", pageInfo.getPageCount());
            return map;
    }

    @Override
    public List<Map<String, Object>> exportrderList(Map<String, Object> params) {

        return   orderQueryLListDao.exportOrderList(params);
    }

}
