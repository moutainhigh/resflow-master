package com.zres.project.localnet.portal.webservice.product.sdwan;

import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @ClassName ReceiveSDWANServiceIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/3/13 11:05
 */
public interface ReceiveSDWANServiceIntf {
    /**
     * 接收SD-WAN 产品
     * @param request
     * @return
     */
    public Map receiveJson(String request);
}
