package com.zres.project.localnet.portal.report.service;

import com.zres.project.localnet.portal.local.domain.PageInfo;
import com.zres.project.localnet.portal.report.dao.CqReportDao;
import com.zres.project.localnet.portal.report.utils.UserUtil;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.*;

import com.ztesoft.res.frame.core.util.ListUtil;

/**
 * 重庆报表相关接口实现
 *
 * @author PangHao
 * @date 2019/6/4    9:51
 **/
@Service
public class CqReportService implements CqReportIntf {

    private Logger logger = LoggerFactory.getLogger(CqReportService.class);


    @Autowired
    CqReportDao cqReportDao;

    /**
     * 未完成环节工单明细（分页）
     *
     * @param map 查询参数及分页信息
     * @return 分页数据
     * @author PangHao
     * @date 2019/6/4  9:57
     **/
    @Override
    public Map<String, Object> queryUndoneTacheList(Map<String, Object> map) {
        PageInfo pageInfo = new PageInfo(); // 分页信息
        pageInfo.setIndexSizeData(map.get("pageIndex"), map.get("pageSize"));
        Map<String, Object> params = MapUtils.getMap(map, "parameters");
        // 分页开始行
        params.put("startRow", pageInfo.getRowStart());
        // 分页结束行
        params.put("endRow", pageInfo.getRowEnd());
        Map<String, Object> sonCompany = cqReportDao.getSonCompany(UserUtil.getOperatorId());
        params.put("areaId", MapUtils.getString(sonCompany, "AREA_ID"));
        params.put("orgId", MapUtils.getString(sonCompany, "ORG_ID"));
        params.put("userId", UserUtil.getOperatorId());
        Map<String, Object> rest = new HashMap<>(6);
        //得到总数
        int rowCount = cqReportDao.queryUndoneTacheCount(params);
        if (rowCount > 0) {

            List<Map<String, Object>> mapList = cqReportDao.queryUndoneTacheList(params);
            rest.put("data", mapList);
        }
        pageInfo.setDataCount(rowCount);
        rest.put("dataLength", rowCount);
        rest.put("flag", "1");
        rest.put("pageIndex", pageInfo.getCurrentPage());
        rest.put("rowNum", pageInfo.getPageSize());
        rest.put("total", pageInfo.getPageCount());
        return rest;
    }

    /**
     * 未完成环节工单统计
     *
     * @param map 查询参数及分页信息
     * @return 分页数据
     * @author PangHao
     * @date 2019/6/4  9:57
     **/
    @Override
    public Map<String, Object> queryUndoneTacheStatistics(Map<String, Object> map) {
        Map<String, Object> resultMap = new HashMap<>(2);
        //得到报表基础数据
        map.put("userId", UserUtil.getOperatorId());
        Map<String, Object> sonCompany = cqReportDao.getSonCompany(UserUtil.getOperatorId());
        map.put("areaId", MapUtils.getString(sonCompany, "AREA_ID"));
        map.put("orgId", MapUtils.getString(sonCompany, "ORG_ID"));
        List<Map<String, Object>> list = cqReportDao.queryUndoneTacheStatistics(map);
        // ============ 数据处理====================
        //已分公司为单位存放环节及数量
        Map<String, Map<String, Object>> dataMap = new HashMap<>();
        //存放实际存在环节信息
        Map<String, String> titleMap = new HashMap<>();
        for (Map<String, Object> data : list) {
            //通过单条记录的分公司名称得到map
            Map<String, Object> orgMap = dataMap.get(MapUtils.getString(data, "orgName"));
            //如果不存在该公司的map,则创建
            if (null == orgMap) {
                orgMap = new HashMap<String, Object>();
                dataMap.put(MapUtils.getString(data, "orgName"), orgMap);
            }
            //将环节及数量存放到分公司map中
            String tacheCode = MapUtils.getString(data, "tacheCode");
            String tacheName = MapUtils.getString(data, "tacheName");
            String count = MapUtils.getString(data, "num");
            orgMap.put(tacheCode, count);
            //保存环节名称及编码（表头）
            titleMap.put(tacheCode, tacheName);
        }
        //存放表格记录数据
        List<Map<String, Object>> dataList = new ArrayList<>();
        //遍历分公司map
        for (String key : dataMap.keySet()) {
            Map<String, Object> data = dataMap.get(key);
            data.put("orgName", key);
            dataList.add(data);
        }
        resultMap.put("title", titleMap);
        resultMap.put("rowData", dataList);
        return resultMap;
    }

    /**
     * 得到本地网流程环节信息（本地网、核查）
     *
     * @return 环节名称编码列表
     * @author PangHao
     * @date 2019/6/4  15:08
     **/
    @Override
    public List<Map<String, String>> getLocalNetworkTache() {
        return cqReportDao.getLocalNetworkTache();
    }

    /**
     * 得到当前登录人省份/地市下属分公司
     *
     * @return 分公司信息列表
     * @author PangHao
     * @date 2019/6/4  15:19
     **/
    @Override
    public List<Map<String, String>> getCityInfo() {
        return cqReportDao.getCityInfo(UserUtil.getOperatorId());
    }

    /**
     * 超时未报竣电路明细(分页)
     *
     * @param map 分页及查询条件
     * @return 分页数据
     * @author PangHao
     * @date 2019/6/11  14:21
     **/
    @Override
    public Map<String, Object> queryOvertimeUnfinishedList(Map<String, Object> map) {
        PageInfo pageInfo = new PageInfo(); // 分页信息
        pageInfo.setIndexSizeData(map.get("pageIndex"), map.get("pageSize"));
        Map<String, Object> params = MapUtils.getMap(map, "parameters");
        // 分页开始行
        params.put("startRow", pageInfo.getRowStart());
        // 分页结束行
        params.put("endRow", pageInfo.getRowEnd());
        params.put("userId", UserUtil.getOperatorId());
        Map<String, Object> sonCompany = cqReportDao.getSonCompany(UserUtil.getOperatorId());
        params.put("areaId", MapUtils.getString(sonCompany, "AREA_ID"));
        params.put("orgId", MapUtils.getString(sonCompany, "ORG_ID"));
        Map<String, Object> rest = new HashMap<>(6);
        //得到总数
        int rowCount = cqReportDao.queryOvertimeUnfinishedCount(params);
        if (rowCount > 0) {
            List<Map<String, Object>> mapList = cqReportDao.queryOvertimeUnfinishedList(params);
            rest.put("data", mapList);
        }
        pageInfo.setDataCount(rowCount);
        rest.put("dataLength", rowCount);
        rest.put("flag", "1");
        rest.put("pageIndex", pageInfo.getCurrentPage());
        rest.put("rowNum", pageInfo.getPageSize());
        rest.put("total", pageInfo.getPageCount());
        return rest;
    }

    /**
     * 电路完工及时率统计
     *
     * @param map 查询条件
     * @return 表头及表格数据
     * @author PangHao
     * @date 2019/6/13  11:36
     **/
    @Override
    public Map<String, Object> queryFinishTimeRateStatistics(Map<String, Object> map) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(2);
        Map<String, Object> resultMap = new HashMap<>(2);
        //得到报表基础数据
        map.put("userId", UserUtil.getOperatorId());
        List<Map<String, Object>> list = cqReportDao.queryFinishTimeRateStatistics(map);
        //已分公司为单位存放环节及数量
        Map<String, Map<String, Object>> dataMap = new HashMap<>();
        Map<String, String> titleMap = new LinkedHashMap<>();
        titleMap.put("CD_ORG", "开通责任单位");
        //汇总行
        Map<String, Object> countRow = new HashMap<>();
        countRow.put("CD_ORG", "共计");
        //存放表格记录数据
        List<Map<String, Object>> dataList = new ArrayList<>();
        if (!ListUtil.isEmpty(list)){
            for (Map<String, Object> data : list) {
                Map<String, Object> orgMap = dataMap.get(MapUtils.getString(data, "CD_ORG"));
                //如果不存在该公司的map,则创建
                if (null == orgMap) {
                    orgMap = new HashMap<String, Object>();
                    dataMap.put(MapUtils.getString(data, "CD_ORG"), orgMap);
                }
                String resourceName = MapUtils.getString(data, "RESOURCES_NAME");
                switch (resourceName) {
                    case "一干调度（客户电路、局内电路）":
                        resourceName = "一干";
                        break;
                    case "二干客户电路":
                        resourceName = "二干客户";
                        break;
                    case "本地客户电路":
                        resourceName = "本地客户";
                        break;
                    case "本地局内电路":
                        resourceName = "本地局内";
                        break;
                    default:
                        resourceName = "";
                        break;
                }
                Integer unOvertimeCount = MapUtils.getInteger(data, "UN_ORVERTIME");
                Integer isFinishCount = MapUtils.getInteger(data, "IS_FINISH");
                String finishPercent = nf.format((unOvertimeCount.doubleValue() / isFinishCount.doubleValue()) * 100) + "%";
                int finishSumCount = MapUtils.getIntValue(orgMap, "FINISH_SUM_COUNT") + isFinishCount;
                int unOvertimeSumCount = MapUtils.getIntValue(orgMap, "UN_OVERTIME_SUM_COUNT") + unOvertimeCount;
                orgMap.put(resourceName + "按时已完成", unOvertimeCount);
                orgMap.put(resourceName + "应完成", isFinishCount);
                orgMap.put(resourceName + "完工及时率", finishPercent);
                orgMap.put("FINISH_SUM_COUNT", finishSumCount);
                orgMap.put("UN_OVERTIME_SUM_COUNT", unOvertimeSumCount);
                titleMap.put(resourceName + "按时已完成", resourceName + "按时已完成");
                titleMap.put(resourceName + "应完成", resourceName + "应完成");
                titleMap.put(resourceName + "完工及时率", resourceName + "完工及时率");
                //总计行
                Integer countRowUnOvertime = MapUtils.getIntValue(countRow, resourceName + "按时已完成") + unOvertimeCount;
                Integer countRowFinish = MapUtils.getIntValue(countRow, resourceName + "应完成") + isFinishCount;
                countRow.put(resourceName + "按时已完成", countRowUnOvertime);
                countRow.put(resourceName + "应完成", countRowFinish);
                countRow.put(resourceName + "完工及时率", nf.format((countRowUnOvertime.doubleValue() / countRowFinish.doubleValue()) * 100) + "%");
            }

            //遍历分公司map
            for (String key : dataMap.keySet()) {
                Map<String, Object> data = dataMap.get(key);
                data.put("CD_ORG", key);
                Integer finishSumCount = MapUtils.getInteger(data, "FINISH_SUM_COUNT");
                Integer unOvertimeSumCount = MapUtils.getInteger(data, "UN_OVERTIME_SUM_COUNT");
                data.put("SUM_PERCENT", nf.format((unOvertimeSumCount.doubleValue() / finishSumCount.doubleValue()) * 100) + "%");
                dataList.add(data);
                //放入总计行
                countRow.put("FINISH_SUM_COUNT", MapUtils.getIntValue(countRow, "FINISH_SUM_COUNT") + finishSumCount);
                countRow.put("UN_OVERTIME_SUM_COUNT", MapUtils.getIntValue(countRow, "UN_OVERTIME_SUM_COUNT") + unOvertimeSumCount);
            }
            countRow.put("SUM_PERCENT", nf.format((MapUtils.getInteger(countRow, "UN_OVERTIME_SUM_COUNT").doubleValue() / MapUtils.getInteger(countRow, "FINISH_SUM_COUNT").doubleValue()) * 100) + "%");

        }else {
            titleMap.put( "按时已完成",  "按时已完成");
            titleMap.put( "应完成",  "应完成");
            titleMap.put( "完工及时率",  "完工及时率");
        }
        dataList.add(countRow);
        titleMap.put("FINISH_SUM_COUNT", "总按时已完成数");
        titleMap.put("UN_OVERTIME_SUM_COUNT", "总应完成数");
        titleMap.put("SUM_PERCENT", "总完工及时率");
        resultMap.put("title", titleMap);
        resultMap.put("rowData", dataList);
        return resultMap;
    }

    /**
     * 资源分配未入库查询（分页）
     *
     * @param map 分页及查询条件
     * @return 分页数据
     * @author PangHao
     * @date 2019/6/17  17:15
     **/
    @Override
    public Map<String, Object> queryResAllocateUnStorageList(Map<String, Object> map) {
        PageInfo pageInfo = new PageInfo(); // 分页信息
        pageInfo.setIndexSizeData(map.get("pageIndex"), map.get("pageSize"));
        Map<String, Object> params = MapUtils.getMap(map, "parameters");
        // 分页开始行
        params.put("startRow", pageInfo.getRowStart());
        // 分页结束行
        params.put("endRow", pageInfo.getRowEnd());
        params.put("userId", UserUtil.getOperatorId());
        Map<String, Object> rest = new HashMap<>(6);
        //得到总数
        int rowCount = cqReportDao.queryResAllocateUnStorageCount(params);
        if (rowCount > 0) {
            List<Map<String, Object>> mapList = cqReportDao.queryResAllocateUnStorageList(params);
            rest.put("data", mapList);
        }
        pageInfo.setDataCount(rowCount);
        rest.put("dataLength", rowCount);
        rest.put("flag", "1");
        rest.put("pageIndex", pageInfo.getCurrentPage());
        rest.put("rowNum", pageInfo.getPageSize());
        rest.put("total", pageInfo.getPageCount());
        return rest;
    }

    /**
     * 资源分配未入库查询（汇总）
     *
     * @param map 查询条件
     * @return 表头及汇总数据
     * @author PangHao
     * @date 2019/6/18  15:56
     **/
    @Override
    public Map<String, Object> queryResAllocateUnStorageStatistics(Map<String, Object> map) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(2);
        Map<String, Object> resultMap = new HashMap<>(2);
        //得到报表基础数据
        map.put("userId", UserUtil.getOperatorId());
        List<Map<String, Object>> list = cqReportDao.queryResAllocateUnStorageStatistics(map);
        //已分公司为单位存放环节及数量
        Map<String, Map<String, Object>> dataMap = new HashMap<>();
        Map<String, String> titleMap = new LinkedHashMap<>();
        titleMap.put("FP_ORG", "资源分配所属分公司");
        //汇总行
        Map<String, Object> countRow = new HashMap<>();
        countRow.put("FP_ORG", "共计");
        for (Map<String, Object> data : list) {
            Map<String, Object> orgMap = dataMap.get(MapUtils.getString(data, "FP_ORG"));
            //如果不存在该公司的map,则创建
            if (null == orgMap) {
                orgMap = new HashMap<String, Object>();
                dataMap.put(MapUtils.getString(data, "FP_ORG"), orgMap);
            }
            String specialtyName = MapUtils.getString(data, "SPECIALTY");
            Integer unSaveSpecialtyCount = MapUtils.getInteger(data, "UN_SAVE_SPECIALTY_COUNT");
            Integer shouldSaveSpecialtyCount = MapUtils.getInteger(data, "SHOULD_SAVE_SPECIALTY_COUNT");
            String finishPercent = nf.format((unSaveSpecialtyCount.doubleValue() / shouldSaveSpecialtyCount.doubleValue()) * 100) + "%";
            //  int finishSumCount = MapUtils.getIntValue(orgMap, "SHOULD_SAVE_SPECIALTY_SUM_COUNT") + shouldSaveSpecialtyCount;
            // int unOvertimeSumCount = MapUtils.getIntValue(orgMap, "UN_SAVE_SPECIALTY") + unSaveSpecialtyCount;
            orgMap.put(specialtyName + "实际入库", unSaveSpecialtyCount);
            orgMap.put(specialtyName + "应入库", shouldSaveSpecialtyCount);
            orgMap.put(specialtyName + "入库率", finishPercent);
            //  orgMap.put("SHOULD_SAVE_SPECIALTY_SUM_COUNT", finishSumCount);
            // orgMap.put("UN_SAVE_SPECIALTY", unOvertimeSumCount);

            titleMap.put(specialtyName + "实际入库", specialtyName + "实际入库");
            titleMap.put(specialtyName + "应入库", specialtyName + "应入库");
            titleMap.put(specialtyName + "入库率", specialtyName + "入库率");
            //总计行
            Integer countRowUnOvertime = MapUtils.getIntValue(countRow, specialtyName + "实际入库") + unSaveSpecialtyCount;
            Integer countRowFinish = MapUtils.getIntValue(countRow, specialtyName + "应入库") + shouldSaveSpecialtyCount;
            countRow.put(specialtyName + "实际入库", countRowUnOvertime);
            countRow.put(specialtyName + "应入库", countRowFinish);
            countRow.put(specialtyName + "入库率", nf.format((countRowUnOvertime.doubleValue() / countRowFinish.doubleValue()) * 100) + "%");
        }
        //存放表格记录数据
        List<Map<String, Object>> dataList = new ArrayList<>();
        //遍历分公司map
        for (String key : dataMap.keySet()) {
            Map<String, Object> data = dataMap.get(key);
            data.put("FP_ORG", key);
           /* Integer finishSumCount = MapUtils.getInteger(data, "SHOULD_SAVE_SPECIALTY_SUM_COUNT");
            Integer unOvertimeSumCount = MapUtils.getInteger(data, "UN_SAVE_SPECIALTY");
            data.put("SUM_PERCENT", nf.format((unOvertimeSumCount.doubleValue() / finishSumCount.doubleValue()) * 100) + "%");*/
            dataList.add(data);
            //放入总计行
            //countRow.put("SHOULD_SAVE_SPECIALTY_SUM_COUNT", MapUtils.getIntValue(countRow, "SHOULD_SAVE_SPECIALTY_SUM_COUNT") + finishSumCount);
            //countRow.put("UN_SAVE_SPECIALTY", MapUtils.getIntValue(countRow, "UN_SAVE_SPECIALTY") + unOvertimeSumCount);
        }
        //countRow.put("SUM_PERCENT", nf.format((MapUtils.getInteger(countRow, "UN_SAVE_SPECIALTY").doubleValue() / MapUtils.getInteger(countRow, "SHOULD_SAVE_SPECIALTY_SUM_COUNT").doubleValue()) * 100) + "%");
        dataList.add(countRow);

        resultMap.put("title", titleMap);
        resultMap.put("rowData", dataList);
        return resultMap;
    }

    /**
     * 得到专业字典
     *
     * @return 专业字典列表
     * @author PangHao
     * @date 2019/6/19  15:42
     **/
    @Override
    public List<Map<String, Object>> getSpecialtyType() {
        return cqReportDao.getSpecialtyType();
    }


    /**
     * 获取的士下分公司
     * @return
     */
    @Override
    public List<Map<String, String>> getOrgName() {
        return cqReportDao.getOrgName(UserUtil.getOperatorId());
    }


}
