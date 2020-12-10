package com.zres.project.localnet.portal.networkAudit;

import com.zres.project.localnet.portal.networkAudit.domain.NetworkAuditPo;

import java.util.List;
import java.util.Map;


/**
 * 业务稽核Service
 */
public interface NetworkAuditIntf {

    public Map<String, Object> queryNetworkAuditData(Map<String, Object> params);


    public int queryNetworkAuditDataCount(Map<String, Object> params);

    public List<NetworkAuditPo> queryNetworkAuditExportData(Map<String, Object> params);

}
