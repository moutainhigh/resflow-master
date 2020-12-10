package com.zres.project.localnet.portal.resourceInitiate.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;

import com.zres.project.localnet.portal.local.domain.PageInfo;
import com.zres.project.localnet.portal.resourceInitiate.data.dao.ResSupplementDao;
import com.zres.project.localnet.portal.resourceInitiate.service.ResSupplementDealServiceIntf;

import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;

@Controller
public class ResSupplementDealController {

    Logger logger = LoggerFactory.getLogger(ResSupplementDealController.class);

    @Autowired
    private ResSupplementDealServiceIntf resSupplementDealServiceIntf;
    @Autowired
    private ResSupplementDao resSupplementDao;

    public Map<String, Object> submitOrderResSup(Map<String, Object> params){
        Map<String, Object> resMap = new HashMap<String, Object>();
        try{
            resMap = resSupplementDealServiceIntf.submitOrderResSup(params);
        }catch (Exception e){
            logger.info("--------------资源补录模块------------工单处理失败-----------------------");
            logger.error("派单失败：", e);
            resMap.put("success", false);
            resMap.put("message", "派单失败!" + e);
        }
        return resMap;
    }


    public Map<String, Object> qryResSupOrdList(Map<String, Object> params) {
        List<Map<String, Object>> mapListT = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();

        PageInfo pageInfo = new PageInfo(); //分页信息
        pageInfo.setIndexSizeData(params.get("pageIndex"), params.get("pageSize"));
        params.put("startRow", pageInfo.getRowStart()); //分页开始行
        params.put("endRow", pageInfo.getRowEnd()); //分页结束行
        params.put("staffId", ThreadLocalInfoHolder.getLoginUser().getUserId());

        int rowCount = 0;
        String queryTypeRes = MapUtils.getString(params, "queryTypeRes");
        if ("waitSignFor".equals(queryTypeRes)) {
            logger.info("-----------------查询待签收数量----------------------");
            rowCount = resSupplementDao.qryWaitSignForOrdCount(params);
            logger.info("-------待签收数量:" + rowCount + "----------------------");
            if (rowCount > 0) {
                List<Map<String, Object>> mapList = resSupplementDao.qryWaitSignForOrdList(params);
                if (!CollectionUtils.isEmpty(mapList)) {
                    mapListT.addAll(mapList);
                }
            }
        }else if("dealWith".equals(queryTypeRes)) {
            logger.info("-----------------查询处理中数量----------------------");
            rowCount = resSupplementDao.qryDealWithOrdCount(params);
            logger.info("-------处理中数量:" + rowCount + "----------------------");
            if (rowCount != 0) {
                List<Map<String, Object>> mapList = resSupplementDao.qryDealWithOrdList(params);
                if (!CollectionUtils.isEmpty(mapList)) {
                    mapListT.addAll(mapList);
                }
            }
        }else if("completed".equals(queryTypeRes)) {
            logger.info("-----------------查询已完成数量----------------------");
            rowCount = resSupplementDao.qryCompletedOrdCount(params);
            logger.info("-------已完成数量:" + rowCount + "----------------------");
            if (rowCount != 0) {
                List<Map<String, Object>> mapList = resSupplementDao.qryCompletedOrdList(params);
                if (!CollectionUtils.isEmpty(mapList)) {
                    mapListT.addAll(mapList);
                }
            }
        }
        pageInfo.setDataCount(rowCount);

        map.put("resultFlag", true);
        map.put("queryTypeRes", queryTypeRes);
        map.put("records", rowCount);
        map.put("rows", mapListT);
        map.put("page", pageInfo.getCurrentPage());
        map.put("rowNum", pageInfo.getPageSize());
        map.put("total", pageInfo.getPageCount());
        return map;
    }


    public Map<String, Object> qryResSupOrdCount(Map<String, Object> qryParams) {
        Map<String, Object> resultMap = new HashMap<>(2);
        String[] various = new String[]{"waitSignFor", "dealWith", "completed"};
        qryParams.put("staffId", ThreadLocalInfoHolder.getLoginUser().getUserId());
        int rowCount = 0;
        Map<String, Object> counts = new HashMap<>();
        try {
            for (int i = 0; i < various.length; i++) {
                switch (various[i]) {
                    case "waitSignFor":
                        rowCount = resSupplementDao.qryWaitSignForOrdCount(qryParams);
                        break;
                    case "dealWith":
                        rowCount = resSupplementDao.qryDealWithOrdCount(qryParams);
                        break;
                    case "completed":
                        rowCount = resSupplementDao.qryCompletedOrdCount(qryParams);
                        break;
                    default:
                        break;
                }
                counts.put(various[i], rowCount);
            }
        } catch (Exception e) {
            logger.error("查询数量错误：", e);
            resultMap.put("resultFlag", false);
            resultMap.put("counts", "资源补录数量查询出错：" + e );
            return resultMap;
        }
        resultMap.put("resultFlag", true);
        resultMap.put("counts", counts);
        return resultMap;
    }

}