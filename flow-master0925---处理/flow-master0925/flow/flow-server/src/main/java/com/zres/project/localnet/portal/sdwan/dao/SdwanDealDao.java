package com.zres.project.localnet.portal.sdwan.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @ClassName SdwanDealDao
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/3/22 11:27
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@Repository
public interface SdwanDealDao {
    /**
     * 查询电路属性
     * @param srvOrdId
     * @return
     */
    List<Map<String,Object>> queryCircuitAttr(@Param("srvOrdId") String srvOrdId);

    /**
     * 查询设备厂商、设备型号 下拉属性
     * @param params
     * @return
     */
    List<Map<String, Object>> queryEnum(Map params);

    /**
     * 查询环节数据
     * @param tacheId
     * @return
     */
    Map<String, Object> qryTacheInfo(@Param("tacheId") String tacheId);

    Map<String, Object> qryAttrInfoId(Map<String, Object> srvOrdAttr);
    void insertSrvAttrInfo(Map<String, Object> srvOrdAttr);
    void updateSrvAttr(Map<String, Object> srvOrdAttr);
}
