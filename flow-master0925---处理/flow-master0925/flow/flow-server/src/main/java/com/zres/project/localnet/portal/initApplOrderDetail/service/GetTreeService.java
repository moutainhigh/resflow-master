package com.zres.project.localnet.portal.initApplOrderDetail.service;

import com.zres.project.localnet.portal.applpage.service.GetTreeIntf;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.initApplOrderDetail.dao.GetTreeDao;

import com.ztesoft.res.frame.core.util.MapUtil;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author :ren.jiahang
 * @date:2019/2/15@time:10:34
 */
@Service
public class GetTreeService implements GetTreeIntf {
    @Autowired
    private GetTreeDao getTreeDao;
    @Autowired
    private OrderDealDao orderDealDao;

    /*
     * 查询地市信息
     * @author ren.jiahang
     * @date 2019/3/7 21:04
     * @param deMap
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    @Override
    public List<Map<String, Object>> queryProvienceTree(Map deMap) {
        List<Map<String, Object>> provenceInfoList = null;
        if ("province".equals(MapUtils.getString(deMap, "flag"))) {
            String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
            Map<String, Object> staffMap = orderDealDao.getOperStaffInfo(Integer.valueOf(operStaffId));
            String orgId = MapUtil.getString(staffMap, "ORG_ID");
            //查询登陆人所在省份信息
            Map<String, Object> proviceOrgMap = orderDealDao.getProviceOrg(Long.parseLong(orgId));
            String areaId = MapUtils.getString(proviceOrgMap, "AREA_ID");
           /* Map<String, Object> proviceArea = getTreeDao.getProviceArea(areaId);
            String proviceAreaId = MapUtil.getString(proviceArea, "ID");*/
            deMap.put("areaId", areaId);
            provenceInfoList = getTreeDao.getProvenceInfoList(deMap);
            if (provenceInfoList != null && !provenceInfoList.isEmpty()) {
                for (Map<String, Object> provenceMap : provenceInfoList) {
                    if (areaId.equals(MapUtils.getString(provenceMap, "id"))) {
                        provenceMap.put("open", true);//展开一级列表
                    }
                }
            }
        }

        else if ("city".equals(MapUtils.getString(deMap, "flag"))) {
            String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
            Map<String, Object> staffMap = orderDealDao.getOperStaffInfo(Integer.valueOf(operStaffId));
            String orgId = MapUtil.getString(staffMap, "ORG_ID");
            //查询登陆人所在省份信息
            Map<String, Object> proviceOrgMap = orderDealDao.getProviceOrg(Long.parseLong(orgId));
            String proviceAreaId = MapUtils.getString(deMap, "areaId");
            if(!deMap.containsKey("areaId")){
                proviceAreaId = MapUtils.getString(proviceOrgMap, "AREA_ID");
                deMap.put("areaId", proviceAreaId);
            }
            provenceInfoList = getTreeDao.getCityInfoList(deMap);
            if (provenceInfoList != null && !provenceInfoList.isEmpty()) {
                //如果返回子节点为1，只返回子节点默认展示
                if(provenceInfoList.size()==2){
                    for (Map<String, Object> provenceMap : provenceInfoList) {
                        if (!proviceAreaId.equals(MapUtils.getString(provenceMap, "id"))) {
                            provenceInfoList.clear();
                            provenceInfoList.add(provenceMap);
                        }
                    }
                }
                else{
                    for (Map<String, Object> provenceMap : provenceInfoList) {
                        if (proviceAreaId.equals(MapUtils.getString(provenceMap, "id"))) {
                            provenceMap.put("open", true);//展开一级列表
                        }
                    }
                }
            }
        }
        else if ("county".equals(MapUtils.getString(deMap, "flag"))) {
            String proviceAreaId = MapUtil.getString(deMap, "areaId");
            provenceInfoList = getTreeDao.getCountyInfoList(deMap);
            if (provenceInfoList != null && !provenceInfoList.isEmpty()) {
                for (Map<String, Object> provenceMap : provenceInfoList) {
                    if (proviceAreaId.equals(MapUtils.getString(provenceMap, "id"))) {
                        provenceMap.put("open", true);//展开一级列表
                    }
                }
            }
        }


        return provenceInfoList;
    }

    @Override
    public Map<String, Object> queryAreaIdByName(String param) {
        return getTreeDao.getAreaIdByName(param);
    }

    @Override
    public Map<String, Object> queryOperStaffInfo() {
        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        return getTreeDao.getParentDepInfo(Integer.parseInt(operStaffId));
    }
}
