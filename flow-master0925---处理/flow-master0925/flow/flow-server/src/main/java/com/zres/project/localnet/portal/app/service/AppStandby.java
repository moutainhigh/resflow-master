package com.zres.project.localnet.portal.app.service;


import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.app.AppStandbyIntf;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.localStanbdyInfo.data.dao.OrderStandbyDao;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @ClassName AppStandby
 * @Description TODO 待办查询
 * @Author wang.g2
 * @Date 2020/6/3 9:36
 */
@RestController
public class AppStandby implements AppStandbyIntf {

    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private OrderStandbyDao orderStandbyDao;

    /**
     * 待办各tab总数
     */
    @IgnoreSession
    @RequestMapping(value="/interfaceBDW/queryStandbyOrderEachCount", produces = "application/json;charset=UTF-8")
    @Override
    public Map<String,Object> queryStandbyOrderEachCount(@RequestBody String request) {
        Map<String,Object> returnmap = new HashMap();
        Map<String,Object> logInfo = new HashMap<String, Object>();
        logInfo.put("interfname","queryStandbyOrderEachCount查询待办各TAB总数量接口");
        logInfo.put("url","/appStandby/interfaceBDW/queryStandbyOrderEachCount.spr");
        logInfo.put("request",request);
        logInfo.put("remark","接收app 查询待办各TAB总数量 json报文");

        try {
            Map params = JSONObject.parseObject(request, Map.class);
            params = getTeacheId(params);
            String staffId = MapUtils.getString(params, "staffId");
            logInfo.put("tradeId",staffId);
            if(StringUtils.isEmpty(staffId)){
                returnmap.put("code", false);
                returnmap.put("msg", "请检查用户id 不能为空");
                logInfo.put("respone",returnmap);
                insertInterfaceLog(logInfo);
                return returnmap;
            }
            Map<String, Object> daoMap = new HashMap<String, Object>();
            params.put("staffId", staffId);
            params.put("dispType", "260000001");
            params.put("woState", "290000002");
            params.put("queryTypeLocal","deptStandny");
            daoMap.put("deptStandny",queryStandbyOrderCount("deptStandny", "deptStandny", params, daoMap))       ;
            params.put("staffId", staffId);
            params.put("dispType", "260000002");
            params.put("woState", "290000002");
            params.put("queryTypeLocal","jobStandby");
            daoMap.put("jobStandby",queryStandbyOrderCount("jobStandby",  "jobStandby", params, daoMap));
            //个人待办
            params.put("staffId", staffId);
            params.put("dispType", "260000003");
            params.put("woState", "290000002"); //处理中
            params.put("queryTypeLocal","staffStandby");
            daoMap.put("staffStandby",queryStandbyOrderCount("staffStandby", "staffStandby", params, daoMap));

            //处理中 dealOrder
            params.put("dealUserId", staffId);
            params.put("compUserId", "");
            params.put("staffId", "");
            params.put("dispType", "260000003");
            params.put("woState", "290000002");
            params.put("queryTypeLocal","dealOrder");
            daoMap.put("dealOrder",queryStandbyOrderCount("dealOrder",  "dealOrder", params, daoMap));

            //确认完成单
            params.put("compUserId", staffId);
            params.put("dispType", "260000004");
            params.put("woState", "290000004"); //处理中
            params.put("queryTypeLocal","dispConfirm");//已完成
            daoMap.put("dispConfirm",queryStandbyOrderCount("dispConfirm", "dispConfirm", params, daoMap));

            //异常单
            params.put("dealUserId", staffId);
            params.put("staffId", staffId);
            params.put("dispType", "260000003");
            params.put("woState", "290000002"); //处理中
            params.put("queryTypeLocal","abnormalOrder");//已完成
            daoMap.put("abnormalOrder", queryStandbyOrderCount("abnormalOrder", "abnormalOrder", params, daoMap));
            //抄送单
            params.put("staffId", staffId);
            params.put("dispType", "260000003");
            params.put("woState", "290000002"); //处理中
            params.put("queryTypeLocal","ccOrder");//已完成
            daoMap.put("ccOrder",queryStandbyOrderCount("ccOrder",  "ccOrder", params, daoMap));

            returnmap.put("code", true);
            returnmap.put("msg", " ");
            returnmap.put("data", daoMap);
            logInfo.put("respone",JSONObject.toJSONString(returnmap));
        } catch (Exception e) {
            returnmap.put("code", false);
            returnmap.put("msg", "查询失败");
            logInfo.put("respone",e.getMessage());
        } finally {
            insertInterfaceLog(logInfo);
        }
        return returnmap;
    }


    /**
     * 各tab页单据信息查询接口
     * @param request
     * @return
     */
    @IgnoreSession
    @RequestMapping(value="/interfaceBDW/qryCstOrdList", produces = "application/json;charset=UTF-8")
    @Override
    public Map<String,Object> qryCstOrdList(@RequestBody String request) {
        Map<String,Object> returnmap = new HashMap();
        Map<String,Object> logInfo = new HashMap<String, Object>();
        logInfo.put("interfname","qryCstOrdList查询待办各tab页单据信息查询接口");
        logInfo.put("url","/appStandby/interfaceBDW/qryCstOrdList.spr");
        logInfo.put("request",request);
        logInfo.put("remark","接收app 查询待办各tab页单据信息查询接口 json报文");
        try {
            Map params = JSONObject.parseObject(request, Map.class);
            params = getTeacheId(params);
            String staffId = MapUtils.getString(params, "staffId");
            String queryTypeLocal = MapUtils.getString(params, "tabFlag");
            if(StringUtils.isEmpty(staffId) || StringUtils.isEmpty(queryTypeLocal)){
                returnmap.put("code", false);
                returnmap.put("msg", "请检查用户id不能为空");
                logInfo.put("respone",returnmap);
                insertInterfaceLog(logInfo);
                return returnmap;
            }
            logInfo.put("tradeId",staffId);
            List<Map<String, Object>> mapListT = new ArrayList<Map<String, Object>>();
            Map<String, Object> map = new HashMap<String, Object>();
            params.put("ycStaffId", staffId);
            params.put("startRow", MapUtils.getString(params,"pageIndex")); //分页开始行
            params.put("endRow", MapUtils.getString(params,"pageEnd")); //分页结束行
            int rowCount = 0;
            if ("abnormalOrder".equals(queryTypeLocal)) {
                params.put("dealUserId", staffId);
                params.put("staffId", staffId);
                params.put("dispType", "260000003");
                params.put("woState", "290000002"); //处理中
                params.put("queryTypeLocal","abnormalOrder");//已完成
                rowCount = orderStandbyDao.qryAbnormalOrderCount(params);
                if (rowCount > 0) {
                    List<Map<String, Object>> mapList = orderStandbyDao.qryAbnormalOrder(params);
                    if (!CollectionUtils.isEmpty(mapList)) {
                        mapListT.addAll(mapList);
                    }
                }
            }else if("dispConfirm".equals(queryTypeLocal)){
                params.put("compUserId", staffId);
                params.put("dispType", "260000004");
                params.put("woState", "290000004"); //处理中
                params.put("queryTypeLocal","dispConfirm");//已完成
                rowCount = orderStandbyDao.queryStandbyOrderCompleteCount(params);
                if (rowCount != 0) {
                    List<Map<String, Object>> mapList = orderStandbyDao.qryCstOrdCompleteList(params);
                    if (!CollectionUtils.isEmpty(mapList)) {
                        mapListT.addAll(mapList);
                    }
                }
            }else if("ccOrder".equals(queryTypeLocal)){
                params.put("staffId", staffId);
                params.put("dispType", "260000003");
                params.put("woState", "290000002"); //处理中
                params.put("queryTypeLocal","ccOrder");//已完成
                rowCount = orderStandbyDao.queryStandbyCcOrderCount(params);
                if (rowCount != 0) {
                    List<Map<String, Object>> mapList = orderStandbyDao.qryCstOrdCcList(params);
                    if (!CollectionUtils.isEmpty(mapList)) {
                        mapListT.addAll(mapList);
                    }
                }
            }else {
                switch(queryTypeLocal){
                    case "deptStandny":
                        params.put("staffId", staffId);
                        params.put("dispType", "260000001");
                        params.put("woState", "290000002");
                        params.put("queryTypeLocal","deptStandny");
                        break;
                    case "jobStandby":
                        params.put("staffId", staffId);
                        params.put("dispType", "260000002");
                        params.put("woState", "290000002");
                        params.put("queryTypeLocal","jobStandby");
                        break;
                    case "staffStandby":
                        //个人待办
                        params.put("staffId", staffId);
                        params.put("dispType", "260000003");
                        params.put("woState", "290000002"); //处理中
                        params.put("queryTypeLocal","staffStandby");
                        break;
                    case "dealOrder":
                        //处理中 dealOrder
                        params.put("dealUserId", staffId);
                        params.put("compUserId", "");
                        params.put("staffId", "");
                        params.put("dispType", "260000003");
                        params.put("woState", "290000002");
                        params.put("queryTypeLocal","dealOrder");
                        break;
                    default:
                        returnmap.put("code", false);
                        returnmap.put("msg", "请检查待办类型");
                        return returnmap;
                }
                rowCount = orderStandbyDao.queryStandbyOrderCount(params);
                if (rowCount != 0) {
                    List<Map<String, Object>> mapList = orderStandbyDao.qryCstOrdList(params);
                    if (!CollectionUtils.isEmpty(mapList)) {
                        mapListT.addAll(mapList);
                    }
                }
            }
            returnmap.put("code", true);
            returnmap.put("msg", "");
            returnmap.put("data", mapListT);
            logInfo.put("respone",JSONObject.toJSONString(returnmap));
        } catch (Exception e) {
            returnmap.put("code", false);
            returnmap.put("msg", "查询失败");
            logInfo.put("respone",e.getMessage());
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
        interflog.put("INTERFNAME",MapUtils.getString(logInfo,"interfname"));
        interflog.put("URL",MapUtils.getString(logInfo,"url"));
        interflog.put("CONTENT", MapUtils.getString(logInfo,"request"));
        interflog.put("RETURNCONTENT",MapUtils.getString(logInfo,"respone"));
        interflog.put("ORDERNO",MapUtils.getString(logInfo,"tradeId"));
        interflog.put("REMARK", MapUtils.getString(logInfo,"remark"));
        wsd.insertInterfLog(interflog);
    }
    /**
     * 代办页面环节名称查询 整理环节id
     * @param params
     * @return
     */
    public Map<String,Object> getTeacheId(Map<String, Object> params){
        if(params.containsKey("tacheIds")){
            String tn = org.apache.commons.collections4.MapUtils.getString(params,"tacheIds");
            if(StringUtils.hasText(tn)){
                String[] splitStr = tn.split(",");
                Set<String> list = new HashSet<>();
                List<String> resList = new ArrayList<>();
                List<String> dataMakeList = new ArrayList<>();
                for(String tmp :splitStr){
                    if(tmp.contains(BasicCode.RES_ALLOCATE)){
                        list.add("0");
                        resList.add(orderStandbyDao.qrySpecName(tmp));
                    } else if(tmp.contains(BasicCode.DATA_MAKE)){
                        list.add("0");
                        dataMakeList.add(orderStandbyDao.qrySpecName(tmp));
                    } else{
                        list.add(tmp);
                    }
                }
                params.put("teacheName",list);
                if (!CollectionUtils.isEmpty(resList)) {
                    params.put("resList",resList);
                }
                if (!CollectionUtils.isEmpty(dataMakeList)) {
                    params.put("dataMakeList",dataMakeList);
                }
            }
        }
        return params;
    }


    /**
     * 总数
     * @param constant
     * @param type
     * @param param
     * @param daoMap
     * @return
     */
    private int queryStandbyOrderCount(String constant, String type, Map<String, Object> param, Map<String, Object> daoMap) {
        int tmpCount;
        if ("abnormalOrder".equals(constant)) {
            tmpCount = orderStandbyDao.qryAbnormalOrderCount(param);
        } //已完成单独查询
        else if ("dispConfirm".equals(constant)) {
            tmpCount = orderStandbyDao.queryStandbyOrderCompleteCount(param);
        } //抄送单
        else if ("ccOrder".equals(constant)) {
            tmpCount = orderStandbyDao.queryStandbyCcOrderCount(param);
        }
        else{
            tmpCount = orderStandbyDao.queryStandbyOrderCount(param);
        }
        daoMap.put(constant, tmpCount);
        return tmpCount;
    }

}
