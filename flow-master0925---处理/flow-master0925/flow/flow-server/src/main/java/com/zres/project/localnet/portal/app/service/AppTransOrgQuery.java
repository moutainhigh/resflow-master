package com.zres.project.localnet.portal.app.service;

import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.app.AppTransOrgQueryIntf;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf;
import com.zres.project.localnet.portal.until.service.UntilServiceIntf;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName AppTransOrgQuery
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/6/22 11:07
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@RestController
public class AppTransOrgQuery implements AppTransOrgQueryIntf {
    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private OrderDealDao orderDealDao;

    @IgnoreSession
    @RequestMapping(value = "/interfaceBDW/queryTransOrgInfo", produces = "application/json;charset=UTF-8")
    @Override
    public Map<String, Object> queryTransOrgInfo(@RequestBody String request) {

        Map<String, Object> returnmap = new HashMap();
        Map<String, Object> logInfo = new HashMap<String, Object>();
        logInfo.put("interfname", "queryTransOrgInfo 转办对象信息查询--组织树信息查询接口");
        logInfo.put("url", "/interfaceBDW/queryTransOrgInfo");
        logInfo.put("request", request);
        logInfo.put("remark", "接收app 转办对象信息查询--组织树信息查询 json报文");
        try {
            List<Map<String, Object>>  staffDeptList = new ArrayList<>();
            Map params = JSONObject.parseObject(request, Map.class);
            String staffId = MapUtils.getString(params, "staffId");
            logInfo.put("tradeId",staffId);
            Map<String, Object> staffInfo = orderDealDao.getOperStaffInfo(Integer.valueOf(staffId));
            String areaId = MapUtils.getString(staffInfo, "AREA_ID");
            String orgId = MapUtils.getString(staffInfo, "ORG_ID");
            Long deptId = MapUtils.getLong(params, "deptId");
            if (StringUtils.isEmpty(staffId)) {
                returnmap.put("code", false);
                returnmap.put("msg", "请检查必填参数不能为空");
                logInfo.put("respone",returnmap);
                insertInterfaceLog(logInfo);
                return returnmap;
            }
            if(StringUtils.isEmpty(deptId)){
                staffDeptList = qryDepartParent(areaId,orgId);
            }else if(!StringUtils.isEmpty(deptId)){
                 staffDeptList = orderDealDao.getStaffInfoDeptListUnit(deptId, staffId);
            } else{
                throw new Exception("请检查必填参数不能为空");
            }
            returnmap.put("code", true);
            returnmap.put("msg", "成功");
            returnmap.put("data", staffDeptList);
            logInfo.put("respone",JSONObject.toJSONString(returnmap));
        } catch (Exception e) {
            returnmap.put("code", false);
            returnmap.put("msg", "查询失败");
            logInfo.put("respone", e.getMessage());
        } finally {
            insertInterfaceLog(logInfo);
        }
        return returnmap;
    }

    /**
     * 记录接口日志
     * @param logInfo
     */
    private void insertInterfaceLog(Map<String,Object> logInfo){
        Map<String,Object> interflog = new HashMap<String, Object>();
        interflog.put("INTERFNAME", MapUtils.getString(logInfo,"interfname"));
        interflog.put("URL",MapUtils.getString(logInfo,"url"));
        interflog.put("CONTENT", MapUtils.getString(logInfo,"request"));
        interflog.put("RETURNCONTENT",MapUtils.getString(logInfo,"respone"));
        interflog.put("ORDERNO",MapUtils.getString(logInfo,"tradeId"));
        interflog.put("REMARK", MapUtils.getString(logInfo,"remark"));
        wsd.insertInterfLog(interflog);
    }

    /**
     *
     * @param
     * @return
     */
    public List qryDepartParent(String currentAreaId,String currentOrgId) {

        List<Map<String, Object>> deptList = orderDealDao.getDeptInfoListRelUnit(currentAreaId, currentOrgId);
        if (deptList != null && deptList.size() > 0) {
            for (Map<String, Object> deptMap : deptList) {
                String parentDeptId = MapUtils.getString(deptMap, "pId");
                if (!"1".equals(parentDeptId)) {
                    deptMap.put("isParent", true);
                } else {
                    deptMap.remove("pId");
                }
            }
        }
        return deptList;
    }


}
