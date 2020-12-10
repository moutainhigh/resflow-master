package com.zres.project.localnet.portal.flowdealinfo.service;

import java.util.List;
import java.util.Map;

import com.zres.project.localnet.portal.flowdealinfo.service.entry.EquipMentEntry;

/**
 * 设备回收信息
 * by ren.jiahang at 20201104
 */
public interface EquipMentRecycleServiceIntf {
    /**
     * 增加设备回收信息（四川需求）
     * @param map
     */
    Map<String, Object> addEquip(Map<String, Object> map);

    /**
     * 按定单查询设备回收信息（四川需求）
     * @param srvOrdId
     * @return
     */
    List<EquipMentEntry> queryEquipBySrvOrdId(String srvOrdId);

    /**
     * 通过设备名称、编码 、机房、局站、区域等查询资源接口反馈的设备信息
     * @param map
     * @return
     */
    List<Map> queryResEquip(Map<String, Object> map);

    /**
     * 查询归属区域
     * @param
     * @return
     */
    List<Map<String,Object>> queryAlongArea();
}
