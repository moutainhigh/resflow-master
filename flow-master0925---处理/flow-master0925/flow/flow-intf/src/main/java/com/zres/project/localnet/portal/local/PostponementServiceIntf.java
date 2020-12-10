package com.zres.project.localnet.portal.local;

import java.util.List;
import java.util.Map;

/**
 * 电路信息--延期申请tab页面后台逻辑
 */
public interface PostponementServiceIntf {
    List<Map<String, Object>> queryPostponementInfoBySrvId(String srvOrdId);
}
