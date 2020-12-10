package com.zres.project.localnet.portal.webservice.ipranActivation;

import java.util.Map;

/**
 *ipran回单接口
 */
public interface IPRANReturnIntf {
    /**
     * ipran新开业务回单
     * @param jsonStr
     * @return
     */
    Map<String, Object> createBack(String jsonStr);

    /**
     * ipran拆机业务回单
     * @param jsonStr
     * @return
     */
    Map<String, Object> removeBack(String jsonStr);

    /**
     * ipran停复机业务回单
     * @param jsonStr
     * @return
     */
    Map<String, Object> statusChange(String jsonStr);

    /**
     * ipran变更业务回单
     * @param jsonStr
     * @return
     */
    Map<String, Object> modify(String jsonStr);

}
