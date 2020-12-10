package com.zres.project.localnet.portal.applpage.service;

import java.util.List;
import java.util.Map;

/**
 * @author :ren.jiahang
 * @date:2018/12/22@time:14:56
 */

public interface InsertOrderInfoIntf {
    /**
     * 查询序列
     * @author ren.jiahang
     * @date 2018/12/26 10:40
     * @param tableName   表名
     * @return int
     */
    int querySequence(String tableName);
    /**
     * 插入客户信息 GOM_BDW_CST_ORD
     * @author ren.jiahang
     * @date 2018/12/26 10:41
     * @param map
     * @return java.lang.String
     */
    int  insertCustomerInfo(Map<String, Object> map);
    /**
     * 插入定单信息 GOM_BDW_SRV_ORD_INFO
     * @author ren.jiahang
     * @date 2018/12/26 10:41
     * @param map
     * @return java.lang.String
     */
    int  insertOrderInfo(Map<String, Object> map);
    /**
     * 插入电路信息 gom_BDW_srv_ord_attr_info
     * @author ren.jiahang
     * @date 2018/12/26 10:42
     * @param list
     * @return java.lang.String
     */
    int  insertordAttrInfo(List<Map<String, Object>> list);
    /**
     * 发起申请页面保存草稿
     * @author ren.jiahang
     * @date 2018/12/26 12:36
     * @param map
     * @return java.lang.String
     */
    String orderInfoSave(Map<String, Object> map);
    /*
     * 发起申请页面提交
     * @author ren.jiahang
     * @date 2019/1/5 14:19
     * @param map
     * @return java.lang.String
     */
    String orderInfoSubmit(Map<String, Object> map);


}
