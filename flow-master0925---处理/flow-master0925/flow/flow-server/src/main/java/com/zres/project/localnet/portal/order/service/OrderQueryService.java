package com.zres.project.localnet.portal.order.service;

import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.local.domain.PageInfo;
import com.zres.project.localnet.portal.localStanbdyInfo.data.dao.OrderStandbyDao;
import com.zres.project.localnet.portal.order.data.dao.OrderQueryDao;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class OrderQueryService implements OrderQueryServiceIntf {
    Logger logger = LoggerFactory.getLogger(OrderQueryService.class);

    @Autowired
    private OrderQueryDao orderQueryDao;
    @Autowired
    private OrderStandbyDao orderStandbyDao;

    public Map<String, Object> queryWo(Map<String, Object> params) {
        Map<String, Object> map = new HashMap<String, Object>();
        List<Map<String, Object>> mapListT = new ArrayList<Map<String, Object>>();
        PageInfo pageInfo = new PageInfo(); //分页信息
        pageInfo.setIndexSizeData(params.get("pageIndex"), params.get("pageSize"));
        params.put("endRow", pageInfo.getRowEnd()); //分页结束行
        params.put("startRow", pageInfo.getRowStart()); //分页开始行
        params.put("userId", MapUtils.getString(params, "userId")); //当前登录用户id

        int woCount = orderQueryDao.countWo(params);
        if (woCount != 0) {
            List<Map<String, Object>> maps = orderQueryDao.queryWo(params);
            if (!CollectionUtils.isEmpty(maps)) {
                mapListT.addAll(maps);
            }
        }
        pageInfo.setDataCount(woCount);
        map.put("dataLength", woCount);
        map.put("data", mapListT);
        map.put("flag", "1");
        map.put("pageIndex", pageInfo.getCurrentPage());
        map.put("rowNum", pageInfo.getPageSize());
        map.put("total", pageInfo.getPageCount());
        return map;
    }

    @Override
    public Map<String, Object> queryExportWo(Map<String, Object> params) {
        Map<String, Object> map = new HashMap<String, Object>();
        List<Map<String, Object>> mapsList = orderQueryDao.queryExportWo(params);
        map.put("data", mapsList);
        return map;
    }

    public int countWo(Map params) {
        return orderQueryDao.countWo(params);
    }

    @Override
    public Map<String, Object> countWoList(Map<String, Object> params) {
        Map<String, Object> map = new HashMap<String, Object>();
        PageInfo pageInfo = new PageInfo(); //分页信息
        pageInfo.setIndexSizeData(params.get("pageIndex"), params.get("pageSize"));
//        params.put("userId", MapUtils.getString(params, "userId")); //当前登录用户id

        int woCount = orderQueryDao.countWo(params);
        pageInfo.setDataCount(woCount);
        map.put("dataLength", woCount);
        map.put("pageIndex", pageInfo.getCurrentPage());
        map.put("rowNum", pageInfo.getPageSize());
        map.put("total", pageInfo.getPageCount());
        return map;
    }

    @Override
    public Map<String, Object> queryWoInfoForHis(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap<>();
        try{
            param = getTeacheId(param);
            if (param.containsKey("pageIndex") && param.containsKey("pageSize")){
                param.put("startRow", (MapUtils.getIntValue(param, "pageIndex") - 1) * MapUtils.getIntValue(param, "pageSize"));
                param.put("endRow", MapUtils.getIntValue(param, "pageIndex") * MapUtils.getIntValue(param, "pageSize"));
            }
            //根据历史月份获取当月的第一天和最后一天
            if (param.containsKey("month") && !StringUtils.isEmpty(MapUtils.getString(param, "month"))){
                SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM");
                String month = MapUtils.getString(param, "month") + "-01";

                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate monthDate = LocalDate.parse(month, fmt);
                LocalDate firstday = LocalDate.of(monthDate.getYear(), monthDate.getMonthValue(), 1);
                LocalDate lastDay = monthDate.with(TemporalAdjusters.lastDayOfMonth());
                //将日期转成字符串
                param.put("firstDay", firstday.format(fmt));
                param.put("lastDay", lastDay.format(fmt));
            }
            //查数据总量
            int num = orderQueryDao.queryWoInfoCont(param);
            if (num > 0){
                List<Map<String, Object>> list = orderQueryDao.queryWoInfoForHis(param);
                resMap.put("rows", list);
            }
            resMap.put("page", MapUtils.getString(param, "pageIndex"));
            resMap.put("rowNum", MapUtils.getString(param, "pageSize"));
            resMap.put("records", num);
        }catch (Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>查询当前处理人历史工单发生异常:{}", e.getMessage());
        }
        return resMap;
    }

    public Map<String,Object> getTeacheId(Map<String, Object> params){
        if(params.containsKey("tacheIds")){
            String tn = org.apache.commons.collections4.MapUtils.getString(params,"tacheIds");
            if(StringUtils.hasText(tn)){
                String[] splitStr = tn.split(",");
                Set<String> list = new HashSet<>();
                List<String> resList = new ArrayList<>();
                List<String> dataMakeList = new ArrayList<>();
                List<String> constructList = new ArrayList<>();
                for(String tmp :splitStr){
                    if(tmp.contains(BasicCode.RES_ALLOCATE)){
                        list.add("0");
                        resList.add(orderStandbyDao.qrySpecName(tmp));
                    } else if(tmp.contains(BasicCode.DATA_MAKE)){
                        list.add("0");
                        dataMakeList.add(orderStandbyDao.qrySpecName(tmp));
                    } else if(tmp.contains(BasicCode.RES_CONSTRUCT)){
                        list.add("0");
                        constructList.add(orderStandbyDao.qrySpecName(tmp));
                    } else{
                        list.add(tmp);
                    }
                }
                params.put("tacheName",list);
                if (!CollectionUtils.isEmpty(resList)) {
                    params.put("resList",resList);
                }
                if (!CollectionUtils.isEmpty(dataMakeList)) {
                    params.put("dataMakeList",dataMakeList);
                }
                if (!CollectionUtils.isEmpty(constructList)) {
                    params.put("constructList",constructList);
                }
            }
        }
        return params;
    }

    @Override
    public List<Map<String, Object>> exportWoOrderInfo(Map<String, Object> param) {
        param = getTeacheId(param);
        //根据历史月份获取当月的第一天和最后一天
        if (param.containsKey("month") && !StringUtils.isEmpty(MapUtils.getString(param, "month"))){
            SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM");
            String month = MapUtils.getString(param, "month") + "-01";

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate monthDate = LocalDate.parse(month, fmt);
            LocalDate firstday = LocalDate.of(monthDate.getYear(), monthDate.getMonthValue(), 1);
            LocalDate lastDay = monthDate.with(TemporalAdjusters.lastDayOfMonth());
            //将日期转成字符串
            param.put("firstDay", firstday.format(fmt));
            param.put("lastDay", lastDay.format(fmt));
        }

        List<Map<String, Object>> list = orderQueryDao.exportWoOrderInfo(param);
        return list;
    }
}
