package com.zres.project.localnet.portal.report.dao;


import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 重庆报表系统相关Dao
 *
 * @author PangHao
 * @date 2019/6/4  9:59
 **/
@Repository
public interface CqReportDao {


    /**
     * 未完成环节工单明细（总数）
     *
     * @param params 筛选条件
     * @return 总数
     * @author PangHao
     * @date 2019/6/4  10:03
     **/
    int queryUndoneTacheCount(Map<String, Object> params);

    /**
     * 未完成环节工单明细（分页）
     *
     * @param params 筛选条件
     * @return 分页数据
     * @author PangHao
     * @date 2019/6/4  10:03
     **/
    List<Map<String, Object>> queryUndoneTacheList(Map<String, Object> params);

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
     * @param userId 当前登录人表示
     * @return 分公司信息列表
     * @author PangHao
     * @date 2019/6/4  15:19
     **/
    List<Map<String, String>> getCityInfo(@Param("userId") String userId);

    /**
     * 未完成环节汇总源数据查询
     *
     * @param map 筛选条件
     * @return 统计数据
     * @author PangHao
     * @date 2019/6/4  10:03
     **/
    List<Map<String, Object>> queryUndoneTacheStatistics(Map<String, Object> map);

    /**
     * 超时未报竣电路明细(总数)
     *
     * @param params 查询条件
     * @return 总数
     * @author PangHao
     * @date 2019/6/11  14:21
     **/
    int queryOvertimeUnfinishedCount(Map<String, Object> params);

    /**
     * 超时未报竣电路明细(分页)
     *
     * @param params 查询条件及分页数据
     * @return 分页数据
     * @author PangHao
     * @date 2019/6/11  14:21
     **/
    List<Map<String, Object>> queryOvertimeUnfinishedList(Map<String, Object> params);

    /**
     * 电路完工及时率统计
     *
     * @param map 查询条件
     * @return 表头及表格数据
     * @author PangHao
     * @date 2019/6/13  11:36
     **/
    List<Map<String, Object>> queryFinishTimeRateStatistics(Map<String, Object> map);

    /**
     * 资源分配未入库查询（总数）
     *
     * @param params 查询条件
     * @return 总数
     * @author PangHao
     * @date 2019/6/17  17:19
     **/
    int queryResAllocateUnStorageCount(Map<String, Object> params);

    /**
     * 资源分配未入库查询（分页）
     *
     * @param params 分页及查询条件
     * @return 分页数据
     * @author PangHao
     * @date 2019/6/17  17:19
     **/
    List<Map<String, Object>> queryResAllocateUnStorageList(Map<String, Object> params);

    /**
     * 资源分配未入库基础数据查询(汇总)
     *
     * @param map 查询参数
     * @return 专业、部分分组数据
     * @author PangHao
     * @date 2019/6/18  16:42
     **/
    List<Map<String, Object>> queryResAllocateUnStorageStatistics(Map<String, Object> map);

    /**
     * 得到分公司信息
     *
     * @param userId 操作人表示
     * @return 所属分公司信息
     * @author PangHao
     * @date 2019/6/19  11:21
     **/
    Map<String, Object> getSonCompany(@Param("userId") String userId);

    /**
     * 得到专业字典
     *
     * @return 专业字典列表
     * @author PangHao
     * @date 2019/6/19  15:42
     **/
    List<Map<String, Object>> getSpecialtyType();

    /**
     * 得到分公司信息
     * @param userId 操作人表示
     * @return 所属分公司信息
     * @author wang.gang
     * @date 2020/4/22  11:21
     **/
    List<Map<String, String>>  getOrgName(@Param("userId") String userId);
}
