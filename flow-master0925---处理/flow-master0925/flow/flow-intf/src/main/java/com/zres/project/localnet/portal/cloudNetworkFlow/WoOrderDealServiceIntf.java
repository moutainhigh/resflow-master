package com.zres.project.localnet.portal.cloudNetworkFlow;

import java.util.Map;

/*
 * 云组网
 * @author guanzhao
 * @date 2020/10/28
 *
 */
public interface WoOrderDealServiceIntf {

    /**
     * 启流程
     * @param createMap
     * @return
     * @throws Exception
     */
    Map<String, Object> createOrderCloud(Map<String, Object> createMap) throws Exception;

    /**
     * 环节回单方法
     * @param submitMap
     * @return
     * @throws Exception
     */
    Map<String, Object> submitWoOrderCloud(Map<String, Object> submitMap) throws Exception;

    /*
     * 云组网环节按钮查询
     * @author guanzhao
     * @date 2020/10/30
     *
     */
    Map<String, Object> getCloudTacheButton(Map<String, Object> params) throws Exception;

    /*
     * 查询分公司区域id--云组网业务产品，电路调度页面
     * @author guanzhao
     * @date 2020/11/6
     *
     */
    Map<String, Object> qryCompanyAreaId(Map<String, Object> params) throws Exception;

    /*
     * 查询mcpe安装测试退单的专业和区域
     * @author guanzhao
     * @date 2020/11/13
     *
     */
    Map<String, Object> qryMcpeInstallBackOrderData(Map<String, Object> params) throws Exception;

}
