package com.zres.project.localnet.portal.app;

import java.util.Map;

/**
 * @ClassName AppStaffidQueryIntf
 * @Description TODO  app 根据user_id查询staffid
 * @Author wang.g2
 * @Date 2020/6/9 16:35
 */
public interface AppStaffidQueryIntf {


    public Map<String, Object> queryStaffidOrderInfo(String request);
}
