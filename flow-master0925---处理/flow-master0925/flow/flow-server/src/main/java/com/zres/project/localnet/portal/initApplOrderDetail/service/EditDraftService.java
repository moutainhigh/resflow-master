package com.zres.project.localnet.portal.initApplOrderDetail.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.applpage.service.EditDraftIntf;
import com.zres.project.localnet.portal.initApplOrderDetail.dao.EditDraftDao;

import com.ztesoft.res.frame.core.util.MapUtil;

/**
 * Created by lu on 2019/1/4 0004.
 */
@Service
public class EditDraftService implements EditDraftIntf {
    @Autowired
    EditDraftDao editDraftDao;

    public Map<String, Object> querySelectedInfo(Map<String, Object> param) {
        String CustId = MapUtil.getString(param, "CustId");
        String cgFlag = MapUtil.getString(param, "cgFlag");

        //  System.out.println("查询被选中行的 业务信息客户信息电路信息" + CustId);
        //根绝客户ID 查询业务订单ID
        List<String> srvOrdIdList = editDraftDao.querySrvOrdIdByCustId(CustId, cgFlag);
        //将业务信息客户信息 电路信息 放入到 一个map中传递到前台
        Map<String, Object> allInfoMap = new HashMap<String, Object>();
        //业务信息客户信息
        Map<String, Object> servCustInfo = editDraftDao.querySelectedServInfo(srvOrdIdList.get(0));
        //电路信息

        List<Map<String, Object>> circuitList = new ArrayList<Map<String, Object>>();
        for (String srvOrdItem : srvOrdIdList) {
            List<Map<String, Object>> circuitInfoList = editDraftDao.queryCircuitInfoBySrvId(srvOrdItem, cgFlag);
            if (!circuitInfoList.isEmpty()) {
                Map<String, Object> circuitMap = new HashMap<String, Object>();
                circuitMap.put("SRV_ORD_ID", srvOrdItem);
                for (Map<String, Object> circuitMapItem : circuitInfoList) {
                    circuitMap.put(circuitMapItem.get("ATTR_VALUE_NAME").toString(), circuitMapItem.get("ATTR_VALUE"));
                }
                circuitList.add(circuitMap);
            }
        }

        allInfoMap.put("servCustInfo", servCustInfo);
        allInfoMap.put("circuitInfo", circuitList);
        return allInfoMap;
    }

    public Map<String, Object> querySelectInfo(String SubscribeId) {
        //  System.out.println("查询被选中行的 业务信息客户信息电路信息" + CustId);
        //将业务信息客户信息 电路信息 放入到 一个map中传递到前台
        Map<String, Object> allInfoMap = new HashMap<String, Object>();
        List<Map<String, Object>> circuitList = new ArrayList<Map<String, Object>>();
        String order_type = "";

        //电路信息
        List<Map<String, Object>> circuitInfoList = editDraftDao.queryCircuitById(SubscribeId);

        for (Map<String, Object> circuitItem : circuitInfoList) {
            String srvOrdId = circuitItem.get("SRV_ORD_ID").toString();
            String instance_id = MapUtil.getString(circuitItem, "INSTANCE_ID");
            //电路详情
            List<Map<String, Object>> circuits = editDraftDao.queryCircuitInfoById(srvOrdId);

            Map<String, Object> circuitMap = new HashMap<String, Object>();
          //  circuitMap.put("SUBSCRIBE_ID", SubscribeId);
            for (Map<String, Object> circuitMapItem : circuits) {
                String attr_value_name = MapUtil.getString(circuitMapItem, "ATTR_VALUE_NAME");
                if(attr_value_name.contains("返回结果")){
                    continue;
                }
                circuitMap.put(circuitMapItem.get("ATTR_VALUE_NAME").toString(), circuitMapItem.get("ATTR_VALUE"));
                order_type = circuitMapItem.get("ORDER_TYPE").toString();
            }
            circuitMap.put("orderType", order_type);
            circuitMap.put("INSTANCE_ID",instance_id);
            circuitList.add(circuitMap);
        }
        allInfoMap.put("circuitInfo", circuitList);
        return allInfoMap;
    }

    @Override
    public Map<String, Object> queryCustInfoByAppId(String appId) {
        return editDraftDao.queryCustInfoByAppId(appId);
    }
}
