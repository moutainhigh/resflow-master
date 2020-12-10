package com.zres.project.localnet.portal.orderAbnormal.service;

import java.util.List;
import java.util.Map;

public interface OrderAbnormalServiceIntf {

    /**
     * 变更信息
     * @param param
     * @return
     */
    Map qryOrdChgLogByCstOrdId(Map<String, Object> param);

    /**
     * 查询订单id
     * @param cstOrdId
     * @return
     */
    Map qrySrvOrdIds(String cstOrdId);

    /**
     * 回单
     * @param param
     * @return
     */
    Map compWo(Map<String, Object> param);

    /**
     * 追单驳回
     * @param param
     * @return
     */
    Map appendReject(Map<String, Object> param);


    /**
     * 追单确认
     * @param param
     * @return
     */
    Map appendConfirm(Map<String, Object> param);

    /**
     * 根据定单ID查询异常单信息
     * @param param
     * @return
     */
    List<Map<String, Object>> queryChangeOrderInfo(Map<String, Object> param);

    /**
     * 资源修改页面入参
     * @param param
     * @return
     */
    Map getResUrlParam(Map<String, Object> param);

    List<String> getPrivVersionState(Map<String, Object> param);

}
