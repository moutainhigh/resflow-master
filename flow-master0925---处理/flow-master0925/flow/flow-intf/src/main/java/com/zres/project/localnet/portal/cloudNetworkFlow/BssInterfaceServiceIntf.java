package com.zres.project.localnet.portal.cloudNetworkFlow;

import java.util.Map;

/**
 * @Classname BssInterfaceServiceIntf
 * @Description 云组网业务流程与bss接口交互接口
 * @Author guanzhao
 * @Date 2020/11/9 10:24
 */
public interface BssInterfaceServiceIntf {

    /**
     * 与bss接口交互
     * @param intfParamMap
     * @return
     * @throws Exception
     */
    Map<String, Object> callInteractiveInterface(Map<String, Object> intfParamMap) throws Exception;

    /**
     * 下发起止租通知接口
     * @param startAndStopRentParams
     * @return
     * @throws Exception
     */
    Map<String, Object> rentFinshOrder(Map<String, Object> startAndStopRentParams) throws Exception;

}
