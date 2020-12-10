package com.zres.project.localnet.portal.cloudNetworkFlow.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface WoOrderDealDao {

    /*
     * 云组网产品按钮查询
     * @author guanzhao
     * @date 2020/10/30
     *
     */
    List<Map<String, String>> getCloudTacheButton(Map paramsMap);

    /*
     * 查询分公司区域id--云组网业务产品，电路调度页面
     * @author guanzhao
     * @date 2020/11/6
     *
     */
    List<Map<String, String>> qryCompanyAreaId(Map qryMap);

    /*
     * 查询省市公司id
     * @author guanzhao
     * @date 2020/11/6
     *
     */
    Map<String, String> qryCityAreaId(Map qryMap);

    /**
     * 查询业务数据--用于反馈接口
     *
     * @param orderId
     * @return
     */
    Map<String, Object> qrySrvOrdDataByOrderId(String orderId);


    /**
     * 查询父流程id以及regionid
     *
     * @param orderId
     * @return
     */
    Map<String, Object> qryParentOrderAndRegion(String orderId);

    /*
     * 查询上一个工单的环节编码
     * @author guanzhao
     * @date 2020/11/13
     *
     */
    String qryPrivTacheCode(@Param("woId") String woId);

    /*
     * 查询mcpe安装测试退单的专业和区域
     * @author guanzhao
     * @date 2020/11/13
     *
     */
    List<Map<String, String>> qryMcpeInstallBackOrderData(Map<String, String> qryMap);

    /**
     * 根据业务号码，环节编码，工单状态查询工单数据
     *
     * @param qryParam
     * @return
     */
    Map<String, Object> qryWoOrderDataByThis(Map<String, Object> qryParam);

}
