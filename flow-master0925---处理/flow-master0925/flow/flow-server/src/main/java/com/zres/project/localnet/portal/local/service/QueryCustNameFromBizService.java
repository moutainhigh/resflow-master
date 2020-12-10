package com.zres.project.localnet.portal.local.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.local.QueryCustNameFromBizServiceIntf;
import com.zres.project.localnet.portal.webservice.res.BusinessQueryServiceIntf;

/**
 * @author  renll
 *
 */
@Service
public class QueryCustNameFromBizService implements QueryCustNameFromBizServiceIntf {

    Logger logger = LoggerFactory.getLogger(QueryCustNameFromBizService.class);

    @Autowired
    private BusinessQueryServiceIntf service;

    @Override
    public Map<String, Object> queryCustNameFromBizData(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap<>();
        try{
            Map<String, Object> stockMap = service.businessQueryCust(param);
            if (MapUtils.getBoolean(stockMap, "isExist")){
                Map<String, Object> pageMap = new HashMap<>();
                pageMap.put("page", MapUtils.getObject(stockMap,"page"));
                pageMap.put("rows", MapUtils.getObject(stockMap, "data"));
                pageMap.put("records", MapUtils.getObject(stockMap, "custTotalCount"));
                resMap.put("success", true);
                resMap.put("msg", "客户信息查询成功");
                resMap.put("data", pageMap);
            }else{
                resMap.put("success", false);
                resMap.put("msg", MapUtils.getString(stockMap, "errMsg"));
            }
        }
        catch (Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>客户信息查询失败：" + e.getMessage());
            resMap.put("success", false);
            resMap.put("msg", "客户信息查询失败，请联系管理员：" + e.getMessage());
        }
        return resMap;
    }
}
