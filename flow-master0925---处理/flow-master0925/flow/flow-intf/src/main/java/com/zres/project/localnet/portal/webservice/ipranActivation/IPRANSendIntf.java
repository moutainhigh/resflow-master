package com.zres.project.localnet.portal.webservice.ipranActivation;

import java.util.Map;

/**
 * ipran工单下发接口
 */
public interface IPRANSendIntf {
    Map<String, Object> send(Map<String, Object> map);
}
