package com.zres.project.localnet.portal.order.service;


import java.util.List;
import java.util.Map;

public interface OrderQueryServiceIntf {
	/**
	 * 工单查询
	 * @author huangping
	 * 2018/12/24
	 * @return List
	 */
	Map<String, Object> queryWo(Map<String, Object> params);

	/**
	 * 工单查询导出
	 * @author huangping
	 * 2018/12/24
	 * @return List
	 */
	Map<String, Object> queryExportWo(Map<String, Object> params);

	/**
	 * 工单查询count
	 * 2018/12/25
	 * @author huangping
	 * @param params
	 * @return	Long
	 */
	int countWo(Map params);

	Map<String, Object> countWoList(Map<String, Object> params);

	Map<String, Object> queryWoInfoForHis(Map<String, Object> param);

	List<Map<String, Object>> exportWoOrderInfo(Map<String, Object> param);
}