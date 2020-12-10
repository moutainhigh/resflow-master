package com.zres.project.localnet.portal.initApplOrderDetail.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

/**
 * @author :ren.jiahang
 * @date:2018/12/22@time:15:12
 */
@Repository
public interface DelOrderInfoDao {
    int delCustomerInfo(String cstId);
    int delOrderInfoBySrvId(List<String> srvId);
    int delOrdAttrInfoBySrvId(List<String> srvId);
    int delOrdAttrInfoAll(List<String> srvId);
    int delOrdAttrInfoBySrvIdItem(String srvId);

}
