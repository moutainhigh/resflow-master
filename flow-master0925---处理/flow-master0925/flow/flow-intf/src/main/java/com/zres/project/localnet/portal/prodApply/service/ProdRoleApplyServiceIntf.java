package com.zres.project.localnet.portal.prodApply.service;

import java.util.Map;

/**
 * 人员或岗位产品分配权限控制
 * @author wangsen
 * @date 2020/10/12 11:19
 * @return
 */
public interface ProdRoleApplyServiceIntf {

    /**
     * 查询用户已分配产品信息
     * @author wangsen
     * @return
     */
    Map<String, Object> queryProdAssingInfo(String staffId, String type);

    /**
     * 查询岗位已分配产品信息
     * @author wangsen
     * @return
     */
    Map<String, Object> queryProdGroupAssingInfo(String groupId, String type);

    /**
     * 保存产品到用户下
     * @returns {*}
     */
    Map<String, Object> saveProdStaff(Map<String, Object> params);

    /**
     * 保存产品到岗位下
     * @returns {*}
     */
    Map<String, Object> saveProdGroup(Map<String, Object> params);
}
