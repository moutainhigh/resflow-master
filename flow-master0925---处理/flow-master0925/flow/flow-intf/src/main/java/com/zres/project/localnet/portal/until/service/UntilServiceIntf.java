package com.zres.project.localnet.portal.until.service;

import java.util.List;
import java.util.Map;

public interface UntilServiceIntf {
	/**
	 * 获取登陆信息
	 * ma.furong
	 * 2018/01/24
	 * @return Map
	 */
	Map<String, Object> queryStaffInfo();
	/**
	 * 用户是否包含多个权限
	 * @param roles 权限点id，以英文逗号分隔
	 * @return boolean
	 */
	boolean queryIsExistRolesForStaff(String roles);
	/**
	 * 获取配置文件的信息
	 * @return List
	 */
	List<Map<String, Object>> queryConfigParamById();

	List<Map<String, Object>> queryConfigParamByCondition(Map<String, Object> map);

	List<Map<String, Object>> queryViewPathAndChild(String itemId);

	String updateConfigById(List<String> leftList,List<String> rightList, String id);

	Map<String, Object> updateConfigByCondition(Map<String, Object> map);

	/**
	 * 根据条件查询统计表PUB_RES_STATISTICS的数据
	 * @param map
	 * @return
	 */
	List<Map<String, Object>> queryStatisticsByCondition(Map<String, Object> map);

	/**
	 * 根据条件查询统计表PUB_RES_STATISTICS的数据
	 * @param map
	 * @return
	 */
	List<Map<String, Object>> queryLineDataByCondition(Map<String, Object> map);

	/**
	 * 删除资源树缓存内容
	 * @param viewPathId
	 */
	void deleteResTreeCache(String viewPathId);

	/**
	 * 插入数据到表pub_homepage_config
	 * @param map
	 * @return
	 */
	Map<String, Object> insertHomePageByCondition(Map<String, Object> map);

	void deleteConfigById(Map<String, Object> map);

	Map<String, Object> queryAdminInfo(String staffId);

	String  queryUrl(String secondSystem);

	Map<String, Object> queryPurviewBystaffId(Map<String, Object> param);

    /**
     * 获取资源呈现系统信息
     * @param param
     * @return
     */
	List<Map<String, Object>> queryRouteInfoUrl(Map<String, Object> param);


}