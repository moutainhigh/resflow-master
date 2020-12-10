package com.zres.project.localnet.portal.order.data.dao;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

/**
 * @author :wang.g2
 * @description :
 * @date : 2019/4/19
 */
@Repository
public interface OrderQueryListDao {

    List<Map<String, Object>> queryOrderList(Map<String, Object> params);

    List<Map<String, Object>> exportOrderList(Map<String, Object> params);

    int countOrderList(Map<String, Object> params);


}
