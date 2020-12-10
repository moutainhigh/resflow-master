package com.zres.project.localnet.portal.until.service;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.until.data.dao.UntilDao;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.res.frame.FrameInterfaceUtils;
import com.ztesoft.res.frame.common.ProcessResult;
import com.ztesoft.res.frame.component.FrameComponentConstant;
import com.ztesoft.res.frame.protal.navigate.intf.RouteServiceIntf;
import com.ztesoft.res.frame.tools.cache.CacheComponent;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import com.ztesoft.res.frame.user.inf.UserInfo;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;





@Service
public class UntilService implements UntilServiceIntf {
    Logger logger = LoggerFactory.getLogger(UntilService.class);
    private  CacheComponent cache = null;
    @Autowired
    private UntilDao untilDao;
    @Autowired
    RouteServiceIntf routeServiceIntf;
    @Autowired
    FrameComponentConstant frameComponentConstant;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private WebServiceDao rsd; //数据库操作-对象

    public Map<String, Object> queryStaffInfo() {
        Map<String, Object> map = new  HashMap<String, Object>();
        try {
            UserInfo user = ThreadLocalInfoHolder.getLoginUser();
            String userName = user.getUserName();
            String userId = user.getUserId();
            Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(userId));
            map.put("userName", userName);
            map.put("name", MapUtils.getString(operStaffInfoMap,"USER_NAME"));
            map.put("userId", userId);
            map.put("areaId", MapUtils.getString(operStaffInfoMap,"AREA_ID"));
            map.put("areaName", MapUtils.getString(operStaffInfoMap,"AREANAME"));
            map.put("orgId", MapUtils.getString(operStaffInfoMap,"ORG_ID"));
            map.put("regionId", MapUtils.getString(operStaffInfoMap,"REGIONID"));
            map.put("isShow", MapUtils.getString(operStaffInfoMap,"IS_SHOW"));
            map.put("ifSelect", MapUtils.getString(operStaffInfoMap,"ORDER_NO"));
        }
        catch (Exception e) {
            map.put("userName", "");
            map.put("userId", "");
            map.put("areaId", "");
            map.put("areaName", "");
            map.put("orgId", "");
            map.put("ifSelect", "");
        }

        return map;
    }
    /**
     * 用户是否包含多个权限
     * @param roles 权限点id，以英文逗号分隔
     * @return
     */
    public boolean queryIsExistRolesForStaff(String roles) {
        if ("".equals(roles)) {
            return true;
        }
        boolean isExist = false;
        try {
            UserInfo user = ThreadLocalInfoHolder.getLoginUser();
            String userId = user.getUserId();
            isExist = FrameInterfaceUtils.getUserAuthInf().isExistRolesForStaff(userId, roles);
        }
        catch (Exception e) {
            //isExist = FrameInterfaceUtils.getUserAuthInf().isExistRolesForStaff("", roles);
        }
        return isExist;
    }
    //查询资源树以及下级所有资源
    public List<Map<String, Object>> queryViewPathAndChild(String viewPathId) {
        UserInfo user = ThreadLocalInfoHolder.getLoginUser();
        String userId = user.getUserId();
        List<Map<String, Object>> resTreeList = new ArrayList<Map<String, Object>>();
        Map<String, Object> object = (Map<String, Object>) getQueryCache(viewPathId);
        if (object != null && !object.isEmpty()) {
            resTreeList = (List<Map<String, Object>>) object.get(viewPathId);
            return resTreeList;
        }
        Object ViewPath = routeServiceIntf.queryViewPathString(viewPathId, true, "GBK");
        StringBuilder errorChildPaths =  new StringBuilder();
        StringBuilder rightChildPath =  new StringBuilder();
        JSONObject resTreeObj = JSONObject.fromObject(((ProcessResult) ViewPath).getData());
        if (resTreeObj.has("resrelationpath") && resTreeObj.getJSONObject("resrelationpath").has("path")) {
            JSONArray resTreeData = resTreeObj.getJSONObject("resrelationpath").getJSONArray("path");
            for (int i = 0; i < resTreeData.size(); i++) {
                if (resTreeData.getJSONObject(i).has("roleId")) { //判断权限 如果没有配置默认不拦截
                    String roleId = resTreeData.getJSONObject(i).get("roleId").toString();
                    if (!("".equals(userId) || "".equals(roleId))) {
                        Boolean isTrue = FrameInterfaceUtils.getUserAuthInf().isExistRolesForStaff(userId, roleId);
                        if (!isTrue) {
                            break;
                        }
                    }
                }
                if (resTreeData.getJSONObject(i).has("restypeid")) {
                    String childPath = resTreeData.getJSONObject(i).get("restypeid").toString();
                    if (!"".equals(childPath)) {
                        Map<String, Object> parentTree = new HashMap<String, Object>();
                        List<Map<String, Object>> childTreeList = new ArrayList<Map<String, Object>>();
                        parentTree.put("resTree", resTreeData.getJSONObject(i));
                        Object childViewPath = routeServiceIntf.queryViewPathString(childPath, true, "GBK");
                        JSONObject childViewObj = JSONObject.fromObject(((ProcessResult) childViewPath).getData());
                        if (!childViewObj.isNullObject() && childViewObj.has("tree") && childViewObj.getJSONObject("tree").has("item")) {
                            JSONArray childViewData = childViewObj.getJSONObject("tree").getJSONArray("item");
                            for (int j = 0; j < childViewData.size(); j++) {
                                if (childViewData.getJSONObject(j).has("item")) {
                                    JSONArray childViewItem = childViewData.getJSONObject(j).getJSONArray("item");
                                    childTreeList.addAll(childViewItem);
                                    if ("".equals(rightChildPath.toString())) {
                                        rightChildPath = rightChildPath.append(childPath);
                                    }
                                }
                                else {
                                    if (errorChildPaths.indexOf(childPath) < 0) {
                                        errorChildPaths = errorChildPaths.append("," + childPath);
                                    }
                                }
                            }
                            List<Map<String, Object>> childTreeListByRole = new ArrayList<Map<String, Object>>();
                            childTreeListByRole = addChildren(childTreeList, childTreeListByRole, userId);
                            parentTree.put("childTree", childTreeListByRole);
                        }
                        else {
                            errorChildPaths = errorChildPaths.append("," + childPath);
                        }
                        resTreeList.add(parentTree);
                    }
                }
            }
        }
        else {
            logger.error("请确保资源树" + viewPathId + "的结构为 resrelationpath -> path");
        }
        if (!"".equals(errorChildPaths.toString())) {
            errorChildPaths = errorChildPaths.append(errorChildPaths.toString().substring(1, errorChildPaths.toString().length()));
            logger.error("请确保资源树" + errorChildPaths + "的结构为 tree -> item -> item，参考" + rightChildPath);
        }
        Map<String, Object> obj = new HashMap<String, Object>();
        obj.put(viewPathId, resTreeList);
        setResTreeCache(viewPathId, obj);
        return resTreeList;
    }

    /**
     *
     * @param childTreeList
     * @return
     */
    private List<Map<String, Object>>  addChildren(List<Map<String, Object>> childTreeList, List<Map<String, Object>> childTreeListByRole, String userId) {
        for (int p = 0; p < childTreeList.size(); p++) {
            List<Map<String, Object>> listJson = JSONArray.fromObject(childTreeList.get(p).get("userdata"));
            String dataInfoString = listJson.get(0).get("data").toString();
            JSONObject dataInfo = JSONObject.fromObject(dataInfoString);
            if (dataInfo.containsKey("roleId")) { //判断权限 如果没有配置默认不拦截
                String roleId = dataInfo.get("roleId").toString();
                if ("".equals(userId) || "".equals(roleId)) {
                    childTreeListByRole.add(childTreeList.get(p));
                }
                else {
                    Boolean isTrue = FrameInterfaceUtils.getUserAuthInf().isExistRolesForStaff(userId, roleId);
                    if (isTrue) {
                        childTreeListByRole.add(childTreeList.get(p));
                    }
                }
            }
            else {
                childTreeListByRole.add(childTreeList.get(p));
            }
        }
        return childTreeListByRole;
    }
    /**
     * 从缓存中获取数据
     * @param resTreeId
     * @return
     */
    public Object getQueryCache(String resTreeId) {
        try {
            getCache();
            if (cache.get(resTreeId) != null) {
                logger.debug("获取了缓存缓存resTreeId=" + cache.get(resTreeId));
                return cache.get(resTreeId);
            }
            logger.error("getQueryCache 未连接到缓存服务器,请检查缓存服务器配置或者服务器状态!");
        }
        catch (Exception e) {
            logger.debug("getQueryCache 获取查询缓存异常, 查询ID:[{}], 异常信息:[{}]", resTreeId, e);
        }
        return null;
    }
    /**
     *加入缓存
     * @param value
     */
    public void setResTreeCache(String resTreeId, Object value) {
        try {
            getCache();
            cache.set(resTreeId, value);
            logger.debug("加入了缓存resTreeId=" + value.toString());
        }
        catch (Exception e) {
            logger.debug("setResTreeCache 设置资源树缓存异常, 异常信息:" + resTreeId + ":" + value, e);
        }
    }
    /**
     * 获取缓存情况
     */
    public void getCache() {
        try {
            if (cache == null) {
                cache = FrameInterfaceUtils.getCache(frameComponentConstant.getComponentCacheKey("tree")
                );
            }
        }
        catch (Exception e) {
            logger.debug("getCache 获取资源树存数据异常; [{}]", e);
        }
    }

    /**
     * 删除资源树缓存内容
     * @param resTreeId
     */
    public void deleteResTreeCache(String resTreeId) {
        try {
            FrameInterfaceUtils.getCache(frameComponentConstant.getComponentCacheKey("tree"))
                    .delete(resTreeId);
            logger.debug("删除了缓存=" + resTreeId);
        }
        catch (Exception e) {
            logger.debug("deleteQueryCache 缓存删除异常, 查询ID[{}], [{}]", resTreeId, e);
        }
    }

    public List<Map<String, Object>> queryConfigParamById() {
        List<Map<String, Object>> list = untilDao.queryConfigParamById();
      // List<Map<String, Object>> ret = hasRole(list);
        return list;
    }
    public List<Map<String, Object>> hasRole(List<Map<String, Object>> list) {
        UserInfo user = ThreadLocalInfoHolder.getLoginUser();
        String userId = user.getUserId();
        List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < list.size(); i++) {
            String roleId = list.get(i).get("ROLE_ID").toString();
            if ("".equals(userId) || "".equals(roleId)) {
                ret.add(list.get(i));
            }
            else {
                Boolean isTrue = FrameInterfaceUtils.getUserAuthInf().isExistRolesForStaff(userId, roleId);
                if (isTrue) {
                    ret.add(list.get(i));
                }
            }
        }
        return ret;
    }
    public List<Map<String, Object>> queryConfigParamByCondition(Map<String, Object> map) {
        List<Map<String, Object>> list = untilDao.queryConfigParamByCondition(map);
        List<Map<String, Object>> ret = hasRole(list);
        return ret;
    }

    public String updateConfigById(List<String> leftList, List<String> rightList, String id) {
        for (int i = 0; i < leftList.size(); i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("isShow", 0);
            map.put("index", i + 1);
            map.put("itemId", leftList.get(i));
            map.put("id", id);
            untilDao.updateConfigById(map);
        }
        for (int i = 0; i < rightList.size(); i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("isShow", 1);
            map.put("index", i + 1);
            map.put("itemId", rightList.get(i));
            map.put("id", id);
            untilDao.updateConfigById(map);
        }
        return "success";
    }

    public Map<String, Object> updateConfigByCondition(Map<String, Object> map) {
        Map<String, Object> ret = new HashMap<String, Object>();
        try {
            untilDao.updateConfigByCondition(map);
        }
        catch (Exception e) {
            ret.put("state", "error");
            ret.put("msg", e);
            return ret;
        }
        ret.put("state", "success");
        return ret;
    }

    /**
     * 根据条件查询统计表PUB_RES_STATISTICS的数据
     * @param map
     * @return
     */
    public List<Map<String, Object>> queryStatisticsByCondition(Map<String, Object> map) {
        return untilDao.queryStatisticsByCondition(map);
    }

    /**
     * 根据条件查询统计表PUB_RES_STATISTICS的数据
     * @param map
     * @return
     */
    public List<Map<String, Object>> queryLineDataByCondition(Map<String, Object> map) {
        List<Map<String, Object>> list;
        String flag = map.get("flag").toString();
        if ("year".equals(flag)) {
            list = untilDao.queryYearLineDataByCondition(map);
        }
        else {
            list = untilDao.queryLineDataByCondition(map);
        }
        return list;
    }
    /**
     * 插入数据到表pub_homepage_config
     * @param map
     * @return
     */
    public Map<String, Object> insertHomePageByCondition(Map<String, Object> map) {
        //先查序列
        String seq = untilDao.queryHomePageSeq();
        map.put("seq", seq);
        try {
            untilDao.insertHomePageByCondition(map);
        }
        catch (Exception e) {
            map.put("state", "error");
            map.put("msg", e);
            return map;
        }
        map.put("state", "success");
        return map;
    }

    public void deleteConfigById(Map<String, Object> map) {
        untilDao.deleteConfigById(map);
    }

    @Override
    public Map<String, Object> queryAdminInfo(String staffId) {
        Map<String, Object> returnMap = new HashMap<>();
        int adminCount = orderDealDao.queryAdminInfo(staffId);
        if(adminCount > 0){
            returnMap.put("result", true);
        }else{
            returnMap.put("result", false);
        }
        return returnMap;
    }

    @Override
    public String queryUrl(String secondSystem) {
        return  rsd.queryUrl(secondSystem);
    }

    @Override
    public Map<String, Object> queryPurviewBystaffId(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap<>();
        List<Map<String, Object>> list = orderDealDao.queryPurviewBystaffId(param);
        if (CollectionUtils.isNotEmpty(list) && list.size() == 1){
            resMap.put("success", true);
        }else{
            resMap.put("success", false);
        }
        return resMap;
    }

    @Override
    public List<Map<String, Object>> queryRouteInfoUrl(Map<String, Object> param) {
        return orderDealDao.queryRouteInfoUrl(param);
    }
}
