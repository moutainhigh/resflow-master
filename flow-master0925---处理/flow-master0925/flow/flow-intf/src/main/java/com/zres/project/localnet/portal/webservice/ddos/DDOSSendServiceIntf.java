package com.zres.project.localnet.portal.webservice.ddos;

import java.util.Map;

public interface DDOSSendServiceIntf {
    Map sendOrder(Map<String, Object> map) throws Exception;
}
