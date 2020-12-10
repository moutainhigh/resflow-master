package com.zres.project.localnet.portal.local.dao;

import com.zres.project.localnet.portal.local.domain.UnicomLocalExportData;
import com.zres.project.localnet.portal.local.domain.UnicomLocalOrderPo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;


/**
 * @author sunlb
 */
@Repository
public interface UnicomLocalOrderDao {


    /**
     * 草稿单、申请单查询
     * @param params
     * @return
     */
    public List<UnicomLocalOrderPo> queryLocalApplyAllOrderOracleData(Map params);

    /**
     * 草稿单、申请单数量
     * @param params
     * @return
     */
    public int queryLocalApplyAllOrderOracleCount(Map params);

    /**
     * 申请单查询
     * @param params
     * @return
     */
    public List<UnicomLocalOrderPo> queryLocalApplyOrderData(Map params);

    /**
     * 核查单查询
     * @param params
     * @return
     */
    public List<UnicomLocalOrderPo> queryApplyOrderData(Map params);
    /**
     * 草稿单查询
     * @param params
     * @return
     */
    public List<UnicomLocalOrderPo> queryLocalApplyDraftOrderData(Map params);

    /**
     * 申请单数量
     * @param params
     * @return
     */
    public int queryLocalApplyOrderCount(Map params);

    /**
     * 草稿单数量
     * @param params
     * @return
     */
    public int queryLocalApplyDraftOrderCount(Map params);

    /**
     * 查询非草稿单导出数据
     * @param params
     * @return
     */
    public List<Map<String,Object>> queryLocalExportApplyOrderData(Map params);

    public List<UnicomLocalExportData> queryLocalExportApplyOrderDataPo(Map params);

    /**
     * 查询草稿单导出的数据
     * @param params
     * @return
     */
    public List<Map<String,Object>> queryLocalExportDraftOrderData(Map params);

    public List<Map<String,Object>> queryResData(Map params);

    public int testSrv(Map<String, Object> params);

    int queryCountByInstanceId(Map<String, Object> param);

    Map<String, Object> queryCircuitInfoBySrvOrdId(Map<String, Object> param);

}
