package com.zres.project.localnet.portal.flowdealinfo.service;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.ztesoft.res.frame.flow.common.intf.spec.DispatchRulesIntf;
import com.ztesoft.res.frame.flow.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DispatchRulesImpl implements DispatchRulesIntf {

    private static final Logger log = LoggerFactory.getLogger(DispatchRulesImpl.class);

    @Autowired
    private OrderDealDao orderDealDao;

    public List<Map<String, String>> lstCldSpecArea(String parentId) {
        String areaId = parentId;
        if (StringUtils.isEmpty(areaId)){
            areaId = "1";
        }
        return orderDealDao.qryAreaDeptData(areaId);
    }

    public List<Map<String, String>> lstSpceArea(String[] ids){
        List<Map<String, String>> deptList = new ArrayList<Map<String, String>>();
        Map<String, Object> mapParam = new HashMap<String, Object>();
        if(ids!=null && ids.length >0){
            if(StringUtils.isEmpty(ids[0])){
                mapParam.put("regionIds",null);
            }else{
                mapParam.put("regionIds",ids);
            }
        }else{
            mapParam.put("regionIds",null);
        }
        List<Map<String, String>> mapsList = orderDealDao.qryAreaDeptListData(mapParam);
        if(!CollectionUtils.isEmpty(mapsList)){
            deptList.addAll(mapsList);
        }
        return deptList;
    }

    public List<Map<String, String>> lstParentSpecArea(String childrenId) {
        String areaId = childrenId;
        return orderDealDao.qryAreaDeptDataParent(areaId);
    }



}
