package com.zres.project.localnet.portal.cloudNetWork.service;

import java.util.Map;

/**
 * @Classname CircuitActionNoticeIntf
 * @Description 停机复机拆机接口
 * @Author guanzhao
 * @Date 2020/11/20 10:33
 */
public interface CircuitActionNoticeIntf {

    /*
     * 2.17-2.19 停机复机拆机接口
     * @author guanzhao
     * @date 2020/11/20
     *
     */
    Map<String,Object> circuitActionNotice(Map<String, Object> intfMap);
}
