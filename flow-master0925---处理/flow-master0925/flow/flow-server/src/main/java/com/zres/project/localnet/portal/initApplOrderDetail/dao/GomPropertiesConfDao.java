package com.zres.project.localnet.portal.initApplOrderDetail.dao;


import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public interface GomPropertiesConfDao {

    /**
     * 保存电路属性配置信息
     * @param proPertiesConflist
     */
    public void insertGomPropertiesConfView(@Param("proPertiesConflist") List<Map<String,Object>> proPertiesConflist);

    /**
     * 产品类型分组
     * @return
     */
    public List<Map<String, Object>> srvIdGroup();

    /**
     * 查询公共信息(非PE端、CE端、A端、Z端)
     * @return
     */
    public List<Map<String, Object>> selectProperConfPuItem(Map deMap);

    /**
     * 查询PE端、CE端、A端、Z端
     * @return
     */
    public List<Map<String, Object>> selectProperConfItem(Map deMap);

    /**
     * 查询PE端、CE端、A端、Z端最大排序Id
     * @param deMap
     * @return
     */
    public int selectPCAZColumnSortMax(Map deMap);


    /**
     * 查询公共信息(非PE端、CE端、A端、Z端)最大排序Id
     * @return
     */
    public int selectProperConfPuItemMax(Map deMap);





}
