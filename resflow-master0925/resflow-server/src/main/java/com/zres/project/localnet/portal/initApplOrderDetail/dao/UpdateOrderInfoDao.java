package com.zres.project.localnet.portal.initApplOrderDetail.dao;

import java.util.Map;

import org.springframework.stereotype.Repository;

/**
 * @author :ren.jiahang
 * @date:2018/12/22@time:15:12
 */
@Repository
public interface UpdateOrderInfoDao {
    /*
     * 根据srvid修改业务信息的order_id字段。起流程
     * @author ren.jiahang
     * @date 2019/1/7 14:56
     * @param srvID
     * @return int
     */
    int updOrderIdBySrvID(Map param);

    /**
     * 更新客户表属性
     *
     * @param param
     * @return
     */
    int UpdateCustomerInfo(Map<String, Object> param);

    /**
     * 更新业务订单属性
     *
     * @param map
     * @return
     */
    int UpdateSrvOrderInfo(Map<String, Object> map);
}
