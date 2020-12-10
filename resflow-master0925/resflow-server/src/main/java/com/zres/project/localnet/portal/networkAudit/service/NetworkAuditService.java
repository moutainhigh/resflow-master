package com.zres.project.localnet.portal.networkAudit.service;


import com.zres.project.localnet.portal.local.domain.PageInfo;
import com.zres.project.localnet.portal.networkAudit.NetworkAuditIntf;
import com.zres.project.localnet.portal.networkAudit.dao.NetworkAuditDao;
import com.zres.project.localnet.portal.networkAudit.domain.NetworkAuditPo;
import com.ztesoft.res.frame.core.exception.ServiceBuizException;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class NetworkAuditService implements NetworkAuditIntf {

    @Autowired
    private NetworkAuditDao networkAuditDao;


    @Override
    public Map<String, Object> queryNetworkAuditData(Map<String, Object> params) {
        Map<String, Object> returnMap = new HashMap<String, Object>();
        List<NetworkAuditPo> networkAuditPosList = new ArrayList<NetworkAuditPo>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int iNetWCount = 0;
        String userId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        PageInfo pageInfo= new PageInfo();//分页信息
        try {
            pageInfo.setIndexSizeData(params.get("pageIndex"),params.get("pageSize"));
            //查询参数
            Map<String, Object> daoMap = new HashMap<String, Object>();
            daoMap.put("userId",userId);
            daoMap.put("serviceCode",params.get("serviceCode"));
            daoMap.put("activeCode",params.get("activeCode"));
            daoMap.put("DISPATCH_ORDER_NO",params.get("DISPATCH_ORDER_NO"));
            daoMap.put("SERIAL_NUMBER",params.get("SERIAL_NUMBER"));
            daoMap.put("SERIAL_NUMBER",params.get("SERIAL_NUMBER"));
            daoMap.put("circuitCode",params.get("circuitCode"));
            daoMap.put("CUST_NAME_CHINESE",params.get("CUST_NAME_CHINESE"));
            String sum_completion_start_date = (String)params.get("SUM_COMPLETION_START_DATE");
            String sum_completion_end_date = (String)params.get("SUM_COMPLETION_END_DATE");
            String rent_confirmation_start_date = (String)params.get("RENT_CONFIRMATION_START_DATE");
            String rent_confirmation_end_date = (String)params.get("RENT_CONFIRMATION_END_DATE");
            daoMap.put("SUM_COMPLETION_START_DATE",sum_completion_start_date);
            daoMap.put("SUM_COMPLETION_END_DATE",sum_completion_end_date);
            daoMap.put("RENT_CONFIRMATION_START_DATE",rent_confirmation_start_date);
            daoMap.put("RENT_CONFIRMATION_END_DATE",rent_confirmation_end_date);
            daoMap.put("startRow",pageInfo.getRowStart());//分页开始行
            daoMap.put("endRow",pageInfo.getRowEnd());//分页结束行

            iNetWCount = networkAuditDao.queryNetworkAuditDataCount(daoMap);
            if(iNetWCount > 0){
                List<NetworkAuditPo> networkAuditPos = networkAuditDao.queryNetworkAuditData(daoMap);
                if(!CollectionUtils.isEmpty(networkAuditPos)){
                    networkAuditPosList.addAll(networkAuditPos);
                }
            }
            returnMap.put("data",networkAuditPosList);
            returnMap.put("flag","success");
        }catch (Exception e){
            returnMap.put("data",new ArrayList<NetworkAuditPo>());
            returnMap.put("message",e.getMessage());
            returnMap.put("flag","fail");
        }
        pageInfo.setDataCount(iNetWCount);
        returnMap.put("dataCount",iNetWCount);
        returnMap.put("pageIndex", pageInfo.getCurrentPage());
        returnMap.put("rowNum", pageInfo.getPageSize());
        returnMap.put("total", pageInfo.getPageCount());
        return returnMap;
    }

    @Override
    public int queryNetworkAuditDataCount(Map<String, Object> params) {



        return 0;
    }

    @Override
    public List<NetworkAuditPo> queryNetworkAuditExportData(Map<String, Object> params) {
        List<NetworkAuditPo> networkAuditPosList = new ArrayList<NetworkAuditPo>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            //查询参数
            Map<String, Object> daoMap = new HashMap<String, Object>();
            daoMap.put("serviceCode",params.get("serviceCode"));
            daoMap.put("userId",params.get("userId"));
            daoMap.put("activeCode",params.get("activeCode"));
            daoMap.put("DISPATCH_ORDER_NO",params.get("DISPATCH_ORDER_NO"));
            daoMap.put("SERIAL_NUMBER",params.get("SERIAL_NUMBER"));
            daoMap.put("circuitCode",params.get("circuitCode"));
            daoMap.put("CUST_NAME_CHINESE",params.get("CUST_NAME_CHINESE"));
            String sum_completion_start_date = (String)params.get("SUM_COMPLETION_START_DATE");
            String sum_completion_end_date = (String)params.get("SUM_COMPLETION_END_DATE");
            String rent_confirmation_start_date = (String)params.get("RENT_CONFIRMATION_START_DATE");
            String rent_confirmation_end_date = (String)params.get("RENT_CONFIRMATION_END_DATE");
            daoMap.put("SUM_COMPLETION_START_DATE",sum_completion_start_date);
            daoMap.put("SUM_COMPLETION_END_DATE",sum_completion_end_date);
            daoMap.put("RENT_CONFIRMATION_START_DATE",rent_confirmation_start_date);
            daoMap.put("RENT_CONFIRMATION_END_DATE",rent_confirmation_end_date);
            List<NetworkAuditPo> networkAuditPos = networkAuditDao.queryNetworkAuditData(daoMap);
            if(!CollectionUtils.isEmpty(networkAuditPos)){
                networkAuditPosList.addAll(networkAuditPos);
            }
        }catch (Exception e){
            throw new ServiceBuizException(e);
        }
        return networkAuditPosList;
    }


}
