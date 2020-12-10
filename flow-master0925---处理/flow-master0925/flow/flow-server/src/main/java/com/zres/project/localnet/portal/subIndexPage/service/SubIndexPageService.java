package com.zres.project.localnet.portal.subIndexPage.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.subIndexPage.dao.SubIndexPageDao;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import com.ztesoft.res.frame.user.inf.UserInfo;

/**
 * @Classname SubIndexPageService
 * @Description 首页工作台
 * @Created by zou.huaqin
 * @Date 2019-05-31 11:14
 */
@Service
public class SubIndexPageService implements SubIndexPageServiceIntf {

    Logger logger = LoggerFactory.getLogger(SubIndexPageService.class);

    @Autowired
    private SubIndexPageDao subIndexPageDao;

    /**
     * 获取登陆信息 ma.furong 2018/01/24
     *
     * @return Map
     */
    @Override
    public Map<String, Object> queryStaffInfo(Map<String, Object> parameter) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            UserInfo user = ThreadLocalInfoHolder.getLoginUser();
            String userId = user.getUserId();
            Map<String, Object> operStaffInfoMap = subIndexPageDao.getOperStaffInfo(Integer.valueOf(userId));
            map.put("userRealName", MapUtils.getString(operStaffInfoMap, "USER_REAL_NAME"));
            map.put("orgName", MapUtils.getString(operStaffInfoMap, "ORG_NAME"));
        } catch (Exception e) {
            map.put("userRealName", "");
            map.put("orgName", "");
        }

        return map;
    }

    /**
     * 查询月绩效工单量
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> queryMonthWorkChart(Map<String, Object> parameter) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            UserInfo user = ThreadLocalInfoHolder.getLoginUser();
            String userId = user.getUserId();
            List<Map<String, Object>> monthWorkChartData = subIndexPageDao.getMonthWorkChartData(userId);
            Integer[] chartData = new Integer[12];
            for (int i = 1; i <= 12; i++) {
                String month = String.valueOf(i);
                if (i < 10) {
                    month = '0' + month;
                }
                chartData[i - 1] = 0;
                for (Map<String, Object> result : monthWorkChartData) {
                    if (month.equals(MapUtils.getString(result, "DEAL_MONTH", ""))) {
                        chartData[i - 1] = MapUtils.getInteger(result, "COUNTS", 0);
                    }
                }
            }
            map.put("code", "success");
            map.put("chartData", chartData);
        } catch (Exception e) {
            map.put("code", "error");
            logger.error("查询月绩效工单量异常:", e);
        }
        return map;
    }

    /**
     * 查询工单时间状态分布
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> queryWorkOrderDistributeChart(Map<String, Object> parameter) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            UserInfo user = ThreadLocalInfoHolder.getLoginUser();
            String userId = user.getUserId();
            List<Map<String, Object>> chartData = subIndexPageDao.getWorkOrderDistributeChartData(userId);
            map.put("code", "success");
            map.put("chartData", chartData);
        } catch (Exception e) {
            map.put("code", "error");
            logger.error("查询月绩效工单量异常:", e);
        }
        return map;
    }
}
