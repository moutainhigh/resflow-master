package com.zres.project.localnet.portal.prodApply.service;

import com.zres.project.localnet.portal.prodApply.dao.ProdRoleApplyDao;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户，岗位 产品分配权限控制
 * @author wangsen
 * @date 2020/10/12 11:19
 * @return
 */
@Service
public class ProdRoleApplyService implements ProdRoleApplyServiceIntf {
    @Autowired
    private ProdRoleApplyDao prodRoleApplyDao;

    /**
     * 查询人员分配的产品信息
     * @author wangsen
     * @date 2020/10/12 18:59
     * @return
     */
    @Override
    public Map<String, Object> queryProdAssingInfo(String staffId, String type) {
        List<Map<String, Object>> mapListT = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        List<Map<String, Object>> mapList = new ArrayList<>();
        if ("1".equals(type)) {
            mapList = prodRoleApplyDao.queryAssingProdInfo(staffId);
        }
        else if ("2".equals(type)) {
            mapList = prodRoleApplyDao.queryUnAssingProdInfo(staffId);
        }
        if (!CollectionUtils.isEmpty(mapList)) {
            for (Map<String, Object> mapl : mapList) {
                Map<String, Object> mapMid = new HashMap<String, Object>();
                mapMid.put("roleId", mapl.get("PROD_ID"));
                mapMid.put("roleName", mapl.get("PROD_NAME"));
                mapListT.add(mapMid);
            }
        }
        map.put("data", mapListT);
        return map;
    }

    /**
     * 查询岗位分配的产品信息
     * @author wangsen
     * @date 2020/10/12 18:59
     * @return
     */
    @Override
    public Map<String, Object> queryProdGroupAssingInfo(String groupId, String type) {
        List<Map<String, Object>> mapListT = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        List<Map<String, Object>> mapList = new ArrayList<>();
        if ("1".equals(type)) {
            mapList = prodRoleApplyDao.queryProdGroupAssingInfo(groupId);
        }
        else if ("2".equals(type)) {
            mapList = prodRoleApplyDao.queryProdGroupUnAssingInfo(groupId);
        }
        if (!CollectionUtils.isEmpty(mapList)) {
            for (Map<String, Object> mapl : mapList) {
                Map<String, Object> mapMid = new HashMap<String, Object>();
                mapMid.put("roleId", mapl.get("PROD_ID"));
                mapMid.put("roleName", mapl.get("PROD_NAME"));
                mapListT.add(mapMid);
            }
        }
        map.put("data", mapListT);
        return map;
    }

    /**
     * 分配产品到用户
     * @author wangsen
     * @date 2020/10/12 15:08
     * @return
     */
    @Override
    public Map<String, Object> saveProdStaff(Map<String, Object> params) {
        Map<String, Object> map = new HashMap();
        boolean flag = false;
        String staffId = params.get("staffId").toString();
        JSONArray jsonArray = JSONArray.fromObject(params.get("object"));
        String prodId = "";
        prodRoleApplyDao.deleteProdStaff(staffId);
        if (jsonArray.size() == 0) {
            flag = true;
        }
        else {
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = JSONObject.fromObject(jsonArray.get(i));
                prodId = jsonObject.getString("prodId");
                flag = saveProdInfo(staffId, prodId, "role");
            }
        }
        map.put("flag", flag);
        return map;
    }

    /**
     * 分配产品到岗位
     * @author wangsen
     * @date 2020/10/12 15:08
     * @return
     */
    @Override
    public Map<String, Object> saveProdGroup(Map<String, Object> params) {
        boolean flag = false;
        Map<String, Object> map = new HashMap();
        JSONArray jsonArray = JSONArray.fromObject(params.get("object"));
        String groupId = params.get("staffId").toString();
        String prodId = "";
        prodRoleApplyDao.deleteProdGroup(groupId);
        if (jsonArray.size() == 0) {
            flag = true;
        }
        else {
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = JSONObject.fromObject(jsonArray.get(i));
                prodId = jsonObject.getString("prodId");
                flag = saveProdInfo(groupId, prodId, "group");
            }
        }
        map.put("flag", flag);
        return map;
    }

    /**
     * 保存人员或岗位分配的产品信息
     * @author wangsen
     * @date 2020/10/12 15:08
     * @return
     */
    public boolean saveProdInfo(String staffId, String prodId, String role_type) {
        int isProdNull = 0;
        int a = 0;
        boolean flag = false;
        if ("role".equals(role_type)) {
            isProdNull = prodRoleApplyDao.isProdSave(staffId, prodId);
        }
        else if ("group".equals(role_type)) {
            isProdNull = prodRoleApplyDao.isProdGroupSave(staffId, prodId);
        }
        if (isProdNull != 0) {
            a = 1;
        }
        else if (isProdNull == 0) {
            if ("role".equals(role_type)) {
                a = prodRoleApplyDao.saveProdStaff(staffId, prodId);
            }
            else if ("group".equals(role_type)) {
                a = prodRoleApplyDao.saveProdGroupStaff(staffId, prodId);
            }
        }
        if (a == 1) {
            flag = true;
        }
        return flag;
    }
}
