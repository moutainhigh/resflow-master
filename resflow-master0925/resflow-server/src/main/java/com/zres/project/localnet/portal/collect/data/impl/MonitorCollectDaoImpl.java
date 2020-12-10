package com.zres.project.localnet.portal.collect.data.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.collect.data.MonitorCollectDao;

import com.ztesoft.res.frame.flow.common.util.DAOUtils;

@Service
public class MonitorCollectDaoImpl implements MonitorCollectDao {
    @Override
    public String qryCollectSql(String sqlSign) {
        StringBuffer sb = new StringBuffer("SELECT R.REMARK\n" +
                      " FROM GOM_BDW_CODE_INFO_SECOND R\n" +
                      " WHERE R.CODE_TYPE = '");
        sb.append(sqlSign).append("'");
        return DAOUtils.executeQueryToString(sb.toString(),new Object[]{});
    }

    @Override
    public List<Map<String, Object>> qryData(String sql) {
        return DAOUtils.executeQueryToList(sql, new Object[]{});
    }

    @Override
    public String qryResDisassemble(String sqlSign) {
        StringBuffer sb = new StringBuffer("SELECT R.REMARK\n" +
                " FROM GOM_BDW_CODE_INFO_SECOND R\n" +
                " WHERE R.CODE_TYPE = 'RES_JOB' " +
                "   AND R.CODE_VALUE = '");
        sb.append(sqlSign).append("'");
        return DAOUtils.executeQueryToString(sb.toString(),new Object[]{});
    }


}
