package com.zres.project.localnet.portal.flowdealinfo.data.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.zres.project.localnet.portal.flowdealinfo.service.entry.EquipMentEntry;
@Repository
public interface EquipMentRecycleDao {
    /**
     * 增加设备回收信息（四川需求）
     * @param equipMentEntry
     */
    void addEquip(EquipMentEntry equipMentEntry);

    /**
     * 增加设备回收信息（四川需求）
     * @param equipMentEntry
     */
    void updateEquip(EquipMentEntry equipMentEntry);

    /**
     * 按定单查询设备回收信息（四川需求）
     * @param srvOrdId
     * @return
     */
    List<EquipMentEntry> queryEquipBySrvOrdId(@Param("srvOrdId") String srvOrdId);

    /**
     * 按定单id和专业查询记录数量（四川需求）
     * @param srvOrdId
     * @param specialtyCode
     * @return
     */
    int queryEquipCountBySrvOrdIdAndSpecialty(@Param("srvOrdId") String srvOrdId,@Param("specialtyCode") String specialtyCode);

    List<Map<String, Object>> queryAreaList(@Param("areaId") String areaId);

}
