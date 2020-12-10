package com.zres.project.localnet.portal.webservice.interfaceJiKe;

import java.util.Map;

/**
 * @ClassName CustRenameServiceIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/8/4 9:54
 */
public interface CustRenameServiceIntf {
    /**
     * B域订单中心通过此接口完成客户、用户更名信息同步
     * @param request
     * @return
     */
    public Map<String, Object> custInfoRename(String request);
}
