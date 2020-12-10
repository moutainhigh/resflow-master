package com.zres.project.localnet.portal.local;

import com.zres.project.localnet.portal.local.domain.UnicomLocalCount;
import com.zres.project.localnet.portal.local.domain.UnicomLocalVo;
import com.zres.project.localnet.portal.order.domain.ResourceInfoVo;

import java.util.List;
import java.util.Map;

public interface UnicomLocalOrderServiceIntf {


    /**
     * 调资源查询接口
     * @param params
     * @return
     */
    public Map<String,Object> queryResData(Map<String,Object> params);
    /**
     * 调资源机房查询接口
     * @param params
     * @return
     */
    public ResourceInfoVo queryResourceData(Map<String,Object> params);
    /**
     * 业务申请-申请单、全部、已完成、已提交订单查询
     * @param params
     * @return
     */
    public UnicomLocalVo queryLocalApplyOrderData(Map<String,Object> params);

    /**
     * 业务申请-申请单、全部、已完成、已提交订单查询(核查单)
     * @param params
     * @return
     */
    public UnicomLocalVo queryLocalApplyOrderCheckData(Map<String,Object> params);
    /**
     * 业务申请-申请单、全部、已完成、已提交订单数量查询
     * @param params
     * @return
     */
    public UnicomLocalCount queryLocalApplyOrderCount(Map<String,Object> params);


    /**
     * 查询非草稿单导出数据
     * @param params
     * @return
     */
    public List<Map<String,Object>> queryLocalExportApplyOrderData(Map params);

    /**
     * 查询草稿单导出的数据
     * @param params
     * @return
     */
    public List<Map<String,Object>> queryLocalExportDraftOrderData(Map params);

    /**
     * 调用资源接口查询电路信息
     * @param param
     * @return
     */
    Map<String, Object> queryStockCircuitInfo(Map<String, Object> param);

    /**
     * 根绝业务号码查询gom_bdw_srv_ord_info表中是否有对应的数据
     * @param param
     * @return
     */
    Map<String, Object> queryCountByInstanceId(Map<String, Object> param);

    /**
     * 根绝instance_id和circuit_code查询电路信息
     * @param param
     * @return
     */
    Map<String, Object> queryCircuitInfoBySrvOrdId(Map<String, Object> param);

    /**
     * 根据业务号码、电路编号调用资源接口查询路由信息
     * @param param
     * @return
     */
    Map<String, Object> queryRouteInfo(Map<String, Object> param);




}
