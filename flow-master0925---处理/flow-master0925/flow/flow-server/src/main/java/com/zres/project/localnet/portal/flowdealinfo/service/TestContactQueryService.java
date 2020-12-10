package com.zres.project.localnet.portal.flowdealinfo.service;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.TestContactDao;
import com.zres.project.localnet.portal.local.domain.PageInfo;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName TestContactQueryService
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/11/17 17:13
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@Service
public class TestContactQueryService implements TestContactQueryIntf{
    @Autowired
    private OrderDealDao orderDealDao;
 @Autowired
    private TestContactDao testContactDao;


    @Override
    public Map<String, Object> queryTestContact(Map<String, Object> params) {
        Map<String, Object> testContact = new HashMap<>();
        List<Map> testContactList = new ArrayList<>();
        PageInfo pageInfo = new PageInfo(); //分页信息

        pageInfo.setIndexSizeData(params.get("pageIndex"), params.get("pageSize"));
        params.put("startRow", pageInfo.getRowStart()); //分页开始行
        params.put("endRow", pageInfo.getRowEnd()); //分页结束行
        int rowCount = testContactDao.countTestContactList(params);
        testContactList = testContactDao.queryTestContactInfo(params);
        if((testContactList.size() == 1  && "1".equals(MapUtils.getString(testContactList.get(0),"REGION_LEVEL")))
            || (testContactList.size() == 2  && "1".equals(MapUtils.getString(testContactList.get(0),"REGION_LEVEL")))
        ){
            //省级名称要查询下级地市
            params.put("regionId", MapUtils.getString(testContactList.get(0), "REGION_ID"));
            testContactList =  testContactDao.queryTestContactList(params);
            rowCount = testContactList.size();
        }

        pageInfo.setDataCount(rowCount);
        testContact.put("dataLength", rowCount);
        testContact.put("pageIndex", pageInfo.getCurrentPage());
        testContact.put("rowNum", pageInfo.getPageSize());
        testContact.put("total", pageInfo.getPageCount());
        testContact.put("data",testContactList);

        return testContact;
    }
}
