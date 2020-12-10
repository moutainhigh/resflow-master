package com.zres.project.localnet.portal.app;

import java.util.Map;

/**
 * @ClassName AppStandbyIntf
 * @Description TODO app 待办
 * @Author wang.g2
 * @Date 2020/6/3 9:34
 */
public interface AppStandbyIntf {
    /**
     * 待办各tab总数
     * @param request
     * @return
     */
    public Map<String,Object> queryStandbyOrderEachCount(String request);

    /**
     * 各tab页单据信息查询接口
     * @param request
     * @return
     */
    public Map<String,Object> qryCstOrdList(String request);
}
