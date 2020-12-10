package com.zres.project.localnet.portal.report.service;

import java.util.List;
import java.util.Map;

/**
 * 重庆报表接口
 *
 * @author PangHao
 * @date 2019/6/4  9:54
 **/
public interface CqReportIntf {

    /**
     * 未完成环节工单明细（分页）
     *
     * @param map 查询参数及分页信息
     * @return 分页数据
     * @author PangHao
     * @date 2019/6/4  9:57
     **/
    Map<String, Object> queryUndoneTacheList(Map<String, Object> map);

    /**
     * 未完成环节工单统计
     *
     * @param map 查询参数
     * @return 表结构及数据
     * @author PangHao
     * @date 2019/6/4  9:57
     **/
    Map<String, Object> queryUndoneTacheStatistics(Map<String, Object> map);


    /**
     * 得到本地网流程环节信息（本地网、中继、核查）
     *
     * @return 环节名称编码列表
     * @author PangHao
     * @date 2019/6/4  15:08
     **/
    List<Map<String, String>> getLocalNetworkTache();

    /**
     * 得到当前登录人省份/地市下属分公司
     *
     * @return 分公司信息列表
     * @author PangHao
     * @date 2019/6/4  15:19
     **/
    List<Map<String, String>> getCityInfo();

    /**
     * 超时未报竣电路明细(分页)
     *
     * @param map 分页及查询条件
     * @return 分页数据
     * @author PangHao
     * @date 2019/6/11  14:21
     **/
    Map<String, Object> queryOvertimeUnfinishedList(Map<String, Object> map);


    /**
     * 电路完工及时率统计
     *
     * @param map 查询条件
     * @return 表头及表格数据
     * @author PangHao
     * @date 2019/6/13  11:36
     **/
    Map<String, Object> queryFinishTimeRateStatistics(Map<String, Object> map);

    /**
     * 资源分配未入库查询（分页）
     *
     * @param map 分页及查询条件
     * @return 分页数据
     * @author PangHao
     * @date 2019/6/17  17:15
     **/
    Map<String, Object> queryResAllocateUnStorageList(Map<String, Object> map);

    /**
     * 资源分配未入库查询（汇总）
     *
     * @param map 查询条件
     * @return 表头及汇总数据
     * @author PangHao
     * @date 2019/6/18  15:56
     **/
    Map<String, Object> queryResAllocateUnStorageStatistics(Map<String, Object> map);

    /**
     * 得到专业字典
     *
     * @return 专业字典列表
     * @author PangHao
     * @date 2019/6/19  15:42
     **/
    List<Map<String, Object>> getSpecialtyType();


    /**
     * 得到当前登录人省份/地市下属分公司
     *
     * @return 分公司信息列表
     * @author wang.gang2
     * @date 2020/4/24  15:19
     **/
    List<Map<String, String>> getOrgName();
}
