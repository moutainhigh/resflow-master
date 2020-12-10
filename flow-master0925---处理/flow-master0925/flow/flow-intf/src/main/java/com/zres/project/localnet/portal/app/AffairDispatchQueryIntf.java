package com.zres.project.localnet.portal.app;

import java.util.Map;

/**
 * @ClassName AffairDispatchQueryIntf
 * @Description TODO  app 事务调单查询
 * @Author wang.g2
 * @Date 2020/6/9 16:35
 */
public interface AffairDispatchQueryIntf {

    public Map<String, Object> queryAffairOrderInfo(String request);

}
