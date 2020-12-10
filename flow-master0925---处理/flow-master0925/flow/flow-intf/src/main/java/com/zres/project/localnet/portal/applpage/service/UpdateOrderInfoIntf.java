package com.zres.project.localnet.portal.applpage.service;

import java.util.List;
import java.util.Map;

/**
 * @author :ren.jiahang
 * @date:2018/12/22@time:15:12
 */
public interface UpdateOrderInfoIntf {
    /**
     * 根据srvid修改业务信息的order_id字段。起流程
     * @author ren.jiahang
     * @date 2019/1/7 14:56
     * @param srvID
     * @return int
     */
   int updOrderIdBySrvID(Map param);

    /**
     * 更新草稿单信息
     * @param map
     * @return
     */
   String  orderInfoUpdate(Map<String, Object> map);

    /**
     * 更新草稿单客户信息
     * @param map
     * @return
     */
    int  UpdateCustomerInfo(Map<String, Object> map);

    /**
     * 更新草稿单业务订单信息
     * @param map
     * @return
     */
    int  UpdateOrderInfo(Map<String, Object> map);

    /**
     * 更新草稿电路信息
     * @param list
     * @return
     */
    int  UpdateordAttrInfo(List<Map<String, Object>> list);

    /**
     * 删除电路信息
     * @return
     */

    int  deleteCircuitInfo(List<String> list);
}
