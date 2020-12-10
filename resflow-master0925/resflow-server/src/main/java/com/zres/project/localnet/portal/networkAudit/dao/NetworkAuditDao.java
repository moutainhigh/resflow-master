package com.zres.project.localnet.portal.networkAudit.dao;

import com.zres.project.localnet.portal.networkAudit.domain.NetworkAuditPo;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

/**
 * @author :sunlb
 * @description :
 * @date : 2019/5/19
 */
@Repository
public interface NetworkAuditDao {

    /**
     * 业务稽核分页查询
     * @param params
     * @return
     */
    public List<NetworkAuditPo> queryNetworkAuditData(Map params);

    /**
     * 业务稽核数据数量
     * @param params
     * @return
     */
    public int queryNetworkAuditDataCount(Map params);

    /**
     * 查询业务稽核导出数据
     * @param params
     * @return
     */
    public List<NetworkAuditPo> queryNetworkAuditExportData(Map params);

}
