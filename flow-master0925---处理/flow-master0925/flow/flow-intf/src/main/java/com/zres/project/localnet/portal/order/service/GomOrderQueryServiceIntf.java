package com.zres.project.localnet.portal.order.service;

import com.zres.project.localnet.portal.order.domain.GomDispatcherOrderPo;
import com.zres.project.localnet.portal.order.domain.GomDispatcherOrderVo;

import java.util.List;
import java.util.Map;

public interface GomOrderQueryServiceIntf {
	/**
	 * 工单查询
	 * @author sunlb
	 * 2018/12/24
	 * @return List
	 */
	public GomDispatcherOrderVo queryWo(Map<String, Object> params);

	/**
	 * 工单查询count
	 * 2018/12/25
	 * @author sunlb
	 * @param params
	 * @return	int
	 */
	int countWo(Map<String, Object> params);

	/**
	 * 查询调度单导出数据
	 * @param params
	 * @return
	 */
	public List<GomDispatcherOrderPo> getGomOrderExportData(Map<String,Object> params);

}