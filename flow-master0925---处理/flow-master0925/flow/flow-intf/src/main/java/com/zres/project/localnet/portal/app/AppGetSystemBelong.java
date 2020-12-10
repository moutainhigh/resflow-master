package com.zres.project.localnet.portal.app;

import java.util.Map;

/**
 * @ClassName AppGetSystemBelong
 * @Description TODO
 * @Author  ren.leilei
 * @Date 2020/11/24 21:06
 */
public interface AppGetSystemBelong {
    /**
     * 工单详情--电路信息--获取所属受理系统以及数据来源
     * @param request
     * @return
     */
    public Map<String,Object> AppGetSystemBelong(String request);
}
