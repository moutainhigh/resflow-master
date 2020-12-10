package com.zres.project.localnet.portal.prodApply.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

/**
 * 产品权限分配
 * @author wangsen
 * @date 2020/10/10 9:59
 * @return
 */
@Repository
public interface ProdRoleApplyDao {

    /**
     * 查询人员可分配产品信息
     * @author wangsen
     * @date 2020/10/12 19:01
     * @return
     */
    List<Map<String, Object>> queryAssingProdInfo(@Param("staffId") String staffId);

    /**
     * 查询人员已分配产品信息
     * @author wangsen
     * @date 2020/10/12 19:01
     * @return
     */
    List<Map<String, Object>> queryUnAssingProdInfo(@Param("staffId") String staffId);

    /**
     * 查询岗位可分配产品信息
     * @author wangsen
     * @date 2020/10/12 19:01
     * @return
     */
    List<Map<String, Object>> queryProdGroupAssingInfo(@Param("groupId") String groupId);

    /**
     * 查询岗位已分配产品信息
     * @author wangsen
     * @date 2020/10/12 19:01
     * @return
     */
    List<Map<String, Object>> queryProdGroupUnAssingInfo(@Param("groupId") String groupId);

    /**
     * 保存人员分配的产品信息
     * @author wangsen
     * @date 2020/10/12 19:01
     * @return
     */
    int saveProdStaff(@Param("staffId") String staffId, @Param("prodId") String prodId);

    /**
     * 保存岗位分配的产品信息
     * @author wangsen
     * @date 2020/10/12 19:01
     * @return
     */
    int saveProdGroupStaff(@Param("groupId") String groupId, @Param("prodId") String prodId);

    /**
     * 人员是否存在分配的产品
     * @author wangsen
     * @date 2020/10/12 19:01
     * @return
     */
    int isProdSave(@Param("staffId") String staffId, @Param("prodId") String prodId);

    /**
     * 岗位是否存在分配的产品
     * @author wangsen
     * @date 2020/10/12 19:01
     * @return
     */
    int isProdGroupSave(@Param("groupId") String groupId, @Param("prodId") String prodId);

    /**
     * 删除人员分配的产品
     * @author wangsen
     * @date 2020/10/12 19:01
     * @return
     */
    void deleteProdStaff(@Param("staffId") String staffId);

    /**
     * 删除岗位分配的产品
     * @author wangsen
     * @date 2020/10/12 19:01
     * @return
     */
    void deleteProdGroup(@Param("groupId") String groupId);
}
