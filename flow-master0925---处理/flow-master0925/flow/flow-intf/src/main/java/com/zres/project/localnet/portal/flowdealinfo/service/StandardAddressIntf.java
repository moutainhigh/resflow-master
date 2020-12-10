package com.zres.project.localnet.portal.flowdealinfo.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

@Repository
public interface StandardAddressIntf {
    /**
     * 查询是否商务专线
     * 如果是商务专线查询标准地址id & 装机地址
     * @param map
     * @return
     */
    Map<String, Object> queryIsBussSpecialty(Map<String, Object> map);

    /**
     * 通过设备名称、编码 、机房、局站、区域等查询资源接口反馈的设备信息
     * @param map
     * @return
     */
    List<Map> queryStandardAddressInfo(Map<String, Object> map);
}
