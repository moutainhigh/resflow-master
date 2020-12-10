package com.zres.project.localnet.portal.local;

import java.util.Map;

/**
 * @author  renll
 * 电路查询下的客户名称通过客户名称或者客户编码调用资源接口进行查询
 */
public interface QueryCustNameFromBizServiceIntf {

    /**
     * 根据业务号码、电路编号调用资源接口查询路由信息
     * @param param
     * @return
     */
    Map<String, Object> queryCustNameFromBizData(Map<String, Object> param);

}
