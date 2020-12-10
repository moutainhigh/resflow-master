package com.zres.project.localnet.portal.app.service;

import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.affairdispatch.constants.AffairDispatchOrderConstant;
import com.zres.project.localnet.portal.affairdispatch.dao.DispatchOrderManageDao;
import com.zres.project.localnet.portal.app.AppIndexPageIntf;
import com.zres.project.localnet.portal.app.data.dao.AppQueryDao;
import com.zres.project.localnet.portal.localStanbdyInfo.data.dao.OrderStandbyDao;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName AppIndexPage
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/6/5 10:04
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@RestController
public class AppIndexPage implements AppIndexPageIntf {
    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private DispatchOrderManageDao dispatchOrderManageDao;
    @Autowired
    private OrderStandbyDao orderStandbyDao;
    @Autowired
    private AppQueryDao appQueryDao;

    @IgnoreSession
    @RequestMapping(value = "/interfaceBDW/queryMyOrderInfo", produces = "application/json;charset=UTF-8")
    @Override
    public Map<String, Object> queryMyOrderInfo(@RequestBody String request) {
        Map<String,Object> returnmap = new HashMap();
        Map<String,Object> logInfo = new HashMap<String, Object>();
        logInfo.put("interfname","queryMyOrderInfo首页单据量信息展示查询接口");
        logInfo.put("url","/interfaceBDW/queryMyOrderInfo");
        logInfo.put("request",request);
        logInfo.put("remark","接收app 首页单据量信息展示查询接口 json报文");
        try {
            Map params = JSONObject.parseObject(request, Map.class);
            String staffId = MapUtils.getString(params, "staffId");
            logInfo.put("tradeId",staffId);
            if(StringUtils.isEmpty(staffId)){
                returnmap.put("code", false);
                returnmap.put("msg", "请检查用户id 不能为空");
                logInfo.put("respone",returnmap);
                insertInterfaceLog(logInfo);
                return returnmap;
            }
            Map<String, Object> datas = new HashMap<>();
            //查询事务单   查询条件
            Map<String, Object> affairOrder = queryAffairOrderList(params);
            datas.put("affairOrderInfo", affairOrder);
            //查询待办
            Map<String, Object> daoMap = new HashMap<String, Object>();
            params.put("staffId", staffId);
            params.put("dispType", "260000001");
            params.put("woState", "290000002");
            params.put("queryTypeLocal","deptStandby");
            daoMap.put("deptStandny",queryStandbyOrderCount("deptStandby", "deptStandby", params, daoMap))       ;
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
            params.put("dispType", "260000005");
            params.put("woState", ""); //处理中
            params.put("queryTypeLocal","ccOrder");//已完成
            daoMap.put("ccOrder",queryStandbyOrderCount("ccOrder",  "ccOrder", params, daoMap));
            datas.put("busiOrderInfo", daoMap);
            Map countOrderNum = appQueryDao.countOrderNum(staffId);
            //查询正常超时预警
            datas.put("orderNumInfo", countOrderNum);
            returnmap.put("code", true);
            returnmap.put("msg", "成功");
            returnmap.put("data", datas);
            logInfo.put("respone",returnmap);
        }catch (Exception e) {
            returnmap.put("code", false);
            returnmap.put("msg", "查询失败");
            logInfo.put("respone",e.getMessage());
        } finally {
            insertInterfaceLog(logInfo);
        }
        return returnmap;
    }


    /**
     * 查询事务调单列表
     *
     * @param map
     * @return
     */

    private Map<String, Object> queryAffairOrderList(Map<String, Object> map) {
        Map<String, Object> rest = new HashMap<>();
        map.put("isReject", "false");
        List<String> various = Arrays.asList("cgOrder", "shOrder", "clOrder", "qrOrder", "lsOrder", "tzOrder", "scOrder");
        try {
            for (int i = 0; i < various.size(); i++) {
                map.put("queryType", various.get(i));
                switch (various.get(i)) {
                    case "cgOrder":
                        map.put("orderState", "290000112");
                        map.put("tacheCode", "");
                        int cgOrder = dispatchOrderManageDao.countDraftAffairOrderList(map);
                        rest.put("affairDraftNum", cgOrder);
                        break;
                    case "shOrder":
                        map.put("orderState", "290000115");
                        map.put("tacheCode", AffairDispatchOrderConstant.AFFAIR_REVIEW);
                        int shOrder = dispatchOrderManageDao.countAffairOrderList(map);
                        rest.put("affairAuditNum", shOrder);
                        break;
                    case "clOrder":
                        map.put("orderState", "");
                        map.put("tacheCode", AffairDispatchOrderConstant.AFFAIR_PROCESS);
                        int clOrder = dispatchOrderManageDao.countDisponeAffairOrderList(map);
                        rest.put("affairDealNum", clOrder);
                        break;
                    case "qrOrder":
                        map.put("orderState", "290000119");
                        map.put("tacheCode", AffairDispatchOrderConstant.AFFAIR_CONFIROM);
                        int qrOrder = dispatchOrderManageDao.countAffairOrderList(map);
                        rest.put("affairConfirmNum", qrOrder);
                        break;
                    case "lsOrder":
                        map.put("orderState", "");
                        map.put("tacheCode", "");
                        int lsOrder = dispatchOrderManageDao.countHistoryAffairOrderList(map);
                        rest.put("historyOrderNum", lsOrder);
                        break;
                    case "tzOrder":
                        map.put("orderState", "");
                        map.put("tacheCode", "");
                        int tzOrder = dispatchOrderManageDao.countCopyAffairOrderList(map);
                        rest.put("copyNoticeNum", tzOrder);
                        break;
                    case "scOrder":
                        map.put("orderState", "");
                        map.put("tacheCode", AffairDispatchOrderConstant.AFFAIR_REVIEW_CHECK);
                        int scOrder = dispatchOrderManageDao.countDisponeAffairOrderList(map);
                        rest.put("affairCheckNum", scOrder);
                        break;
                    default:
                        rest.put("historyOrderNum", "");
                        break;
                }
            }
        } catch (Exception e) {
            rest.put("data", "查询各事务单数量错误");
        }
        return rest;
    }


    /**
     * 查询待办
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
        }
        else{
            tmpCount = orderStandbyDao.queryStandbyOrderCount(param);
        }
        daoMap.put(constant, tmpCount);
        return tmpCount;
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
