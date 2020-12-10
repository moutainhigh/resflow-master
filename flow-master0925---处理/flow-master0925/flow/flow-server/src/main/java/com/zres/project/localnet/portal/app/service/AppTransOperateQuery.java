package com.zres.project.localnet.portal.app.service;

import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.app.AppTransOperateQueryIntf;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf;
import com.zres.project.localnet.portal.until.service.UntilServiceIntf;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
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
 * @ClassName AppTransOperateQuery
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/6/22 11:00
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@RestController
public class AppTransOperateQuery implements AppTransOperateQueryIntf {
    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private OrderDealServiceIntf orderDealServiceIntf;

    @IgnoreSession
    @RequestMapping(value = "/interfaceBDW/queryTransOperaInfo",produces = "application/json;charset=UTF-8")
    @Override
    public Map<String, Object> queryTransOperaInfo(@RequestBody String request) {

        Map<String, Object> returnmap = new HashMap();
        Map<String, Object> logInfo = new HashMap<String, Object>();
        logInfo.put("interfname", "queryTransOperaInfo 转办对象信息查询--默认转办信息查询接口");
        logInfo.put("url", "/interfaceBDW/queryTransOperaInfo");
        logInfo.put("request", request);
        logInfo.put("remark", "接收app 转办对象信息查询--默认转办信息查询 json报文");
        try {
            Map<String, Object> orgPerDepPullDown = new HashMap<>();
            Map<String, Object> queryParam = new HashMap();
            Map params = JSONObject.parseObject(request, Map.class);
            String staffId = MapUtils.getString(params, "staffId");
            logInfo.put("tradeId",staffId);
            Map<String, Object> staffInfo = orderDealDao.getOperStaffInfo(Integer.valueOf(staffId));
            if(MapUtils.isEmpty(staffInfo)){
                returnmap.put("code", false);
                returnmap.put("msg", "请检查账号[staffId]是否存在");
                logInfo.put("respone",returnmap);
                insertInterfaceLog(logInfo);
                return returnmap;
            }
            String areaId = MapUtils.getString(staffInfo, "AREA_ID");
            String searchOrgPerName = MapUtils.getString(params, "searchOrgPerName");
            String transType = MapUtils.getString(params, "transType");
            String orgId = MapUtils.getString(params, "orgId");
            Long deptId = MapUtils.getLong(params, "deptId");

            if (StringUtils.isEmpty(staffId) && StringUtils.isEmpty(areaId) && StringUtils.isEmpty(transType)) {
                returnmap.put("code", false);
                returnmap.put("msg", "请检查必填参数不能为空");
                logInfo.put("respone",returnmap);
                insertInterfaceLog(logInfo);
                return returnmap;
            }
            queryParam.put("searchOrgPerName",searchOrgPerName); // 模糊查询
            queryParam.put("orgPerDeTypeVal",transType); // 派发类型
            queryParam.put("objType",transType); // 派发类型
            queryParam.put( "currentUserId",staffId);
            queryParam.put( "currentOrgId",MapUtils.getString(staffInfo,"ORG_ID"));
            queryParam.put( "currentAreaId",areaId);
            queryParam.put( "postId",orgId);
            queryParam.put( "deptId",deptId);
            queryParam.put("pageIndex",1);
            queryParam.put("pageSize",500);

            if(StringUtils.isEmpty(orgId) && StringUtils.isEmpty(deptId)){
                // 岗位部门为空得 就是查父级
                 orgPerDepPullDown = orderDealServiceIntf.qrySearchOrgPerDepPullDown(queryParam);
            }else{
                 orgPerDepPullDown = orderDealServiceIntf.qrySearchOrgPerDepPullDownSub(queryParam);
            }

            returnmap.put("code", true);
            returnmap.put("msg", "成功");
            returnmap.put("data", MapUtils.getObject(orgPerDepPullDown,"data"));
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

}
