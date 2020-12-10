package com.zres.project.localnet.portal.cloudNetworkFlow;

import java.util.Map;

/**
 * @Classname CloudNetworkInterfaceServiceIntf
 * @Description 云组网业务流程与云网平台的接口交互接口
 * @Author guanzhao
 * @Date 2020/11/9 10:24
 */
public interface CloudNetworkInterfaceServiceIntf {

    /*
     * 与云网平台接口交互接口
     * 入参：orderId 订单id
     *      intfCode 接口编码
     * @author guanzhao
     * @date 2020/11/9
     *
     */
    Map<String, Object> callCloudNetworkInterface(Map<String, Object> intfParamMap) throws Exception;

    /*
     * 云网平台反馈接口
     * 入参： 业务号码  serialNumber
     *       反馈标识  success (可无)
     *       接口编码  intfCode
     * @author guanzhao
     * @date 2020/11/23
     *
     */
    Map<String, Object> cloudNetworkFeedbackInterface(Map<String, Object> intfParamMap) throws Exception;
}
