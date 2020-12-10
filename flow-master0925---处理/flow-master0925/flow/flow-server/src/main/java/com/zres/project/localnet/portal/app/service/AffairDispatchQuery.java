package com.zres.project.localnet.portal.app.service;

import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.affairdispatch.constants.AffairDispatchOrderConstant;
import com.zres.project.localnet.portal.affairdispatch.dao.DispatchOrderManageDao;
import com.zres.project.localnet.portal.app.AffairDispatchQueryIntf;
import com.zres.project.localnet.portal.local.domain.PageInfo;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.res.frame.core.util.MapUtil;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName AffairDispatchQuery
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/6/9 16:38
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@RestController
public class AffairDispatchQuery implements AffairDispatchQueryIntf {
    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private DispatchOrderManageDao dispatchOrderManageDao;


    @IgnoreSession
    @RequestMapping(value = "/interfaceBDW/queryAffairOrderInfo" , produces = "application/json;charset=UTF-8")
    @Override
    public Map<String, Object> queryAffairOrderInfo(@RequestBody String request) {
        Map<String,Object> returnmap = new HashMap();
        Map<String,Object> logInfo = new HashMap<String, Object>();
        logInfo.put("interfname","queryAffairOrderInfo 事务调单信息查询接口");
        logInfo.put("url","/interfaceBDW/queryAffairOrderInfo");
        logInfo.put("request",request);
        logInfo.put("remark","接收app 事务调单信息查询接口 json报文");
        try {
            Map params = JSONObject.parseObject(request, Map.class);
            String staffId = MapUtils.getString(params, "staffId");
            logInfo.put("tradeId", staffId);
            if (StringUtils.isEmpty(staffId)) {
                returnmap.put("code", false);
                returnmap.put("msg", "请检查用户id 不能为空");
                logInfo.put("respone",returnmap);
                insertInterfaceLog(logInfo);
                return returnmap;
            }
            Map affairOrderList = queryAffairOrderList(params);
            returnmap.put("code", true);
            returnmap.put("msg", "成功");
            returnmap.put("data", MapUtils.getObject(affairOrderList,"data"));
            logInfo.put("respone",JSONObject.toJSONString(returnmap));
        } catch (Exception e) {
            returnmap.put("code", false);
            returnmap.put("msg", "查询失败");
            logInfo.put("respone",e.getMessage());
        }finally {
            insertInterfaceLog(logInfo);
        }
        return returnmap;
    }


    /**
     * 查询事务调单列表
     *
     * @param param
     * @return
     */

    private Map<String, Object> queryAffairOrderList(Map<String, Object> param) {
        String staffId = MapUtils.getString(param, "staffId");
        String queryType = MapUtils.getString(param, "flag", "");
        Boolean isGetData = MapUtils.getBoolean(param, "isGetData", true);

        Map<String, Object> rest = new HashMap<String, Object>();
        String isReject = MapUtil.getString(param, "isReject", "false");
        if ("false".equals(isReject)) {
            param.put("isReject", isReject);
        }
        param.put("startRow",MapUtils.getInteger(param,"pageIndex",0)); // 分页开始行
        param.put("endRow",MapUtils.getInteger(param,"pageEnd",10)); // 分页结束行
        param.put("staffId", staffId);
        param.put("queryType", queryType);
        int rowCount = 0;
        switch (queryType) {
            // 发起事务
            case "fqOrder":
                param.put("tacheCode", AffairDispatchOrderConstant.INITIATE_AFFAIR);
                rowCount = dispatchOrderManageDao.countStartAffairOrderList(param);
                if (rowCount != 0 && isGetData) {
                    List<Map<String, Object>> mapList = dispatchOrderManageDao.selectStartAffairOrderList(param);
                    rest.put("data", mapList);
                }
                break;
            // 草稿箱
            case "cgOrder":
                param.put("orderState", "290000112");
                rowCount = dispatchOrderManageDao.countDraftAffairOrderList(param);
                if (rowCount != 0 && isGetData) {
                    List<Map<String, Object>> mapList = dispatchOrderManageDao.selectDraftAffairOrderList(param);
                    rest.put("data", mapList);
                }
                break;
            // 审核事务
            case "shOrder":
                param.put("orderState", "290000115");
                param.put("tacheCode", AffairDispatchOrderConstant.AFFAIR_REVIEW);
                rowCount = dispatchOrderManageDao.countAffairOrderList(param);
                if (rowCount != 0 && isGetData) {
                    List<Map<String, Object>> mapList = dispatchOrderManageDao.selectAffairOrderList(param);
                    rest.put("data", mapList);
                }
                break;
            // 处理事务
            case "clOrder":
                param.put("tacheCode", AffairDispatchOrderConstant.AFFAIR_PROCESS);
                rowCount = dispatchOrderManageDao.countDisponeAffairOrderList(param);
                if (rowCount != 0 && isGetData) {
                    List<Map<String, Object>> mapList = dispatchOrderManageDao.selectDisponseAffairOrderList(param);
                    rest.put("data", mapList);
                }
                break;
            // 确认事务
            case "qrOrder":
                param.put("orderState", "290000119");
                param.put("tacheCode", AffairDispatchOrderConstant.AFFAIR_CONFIROM);
                rowCount = dispatchOrderManageDao.countAffairOrderList(param);
                if (rowCount != 0 && isGetData) {
                    List<Map<String, Object>> mapList = dispatchOrderManageDao.selectAffairOrderList(param);
                    rest.put("data", mapList);
                }
                break;
            // 事务通知
            case "tzOrder":
                rowCount = dispatchOrderManageDao.countCopyAffairOrderList(param);
                if (rowCount != 0 && isGetData) {
                    List<Map<String, Object>> mapList = dispatchOrderManageDao.selectCopyAffairOrderList(param);
                    rest.put("data", mapList);
                }
                break;
            // 历史事务
            case "lsOrder":
                rowCount = dispatchOrderManageDao.countHistoryAffairOrderList(param);
                if (rowCount != 0 && isGetData) {
                    List<Map<String, Object>> mapList = dispatchOrderManageDao.selectHistoryAffairOrderList(param);
                    rest.put("data", mapList);
                }
                break;
            // 事务审查
            case "scOrder":
                param.put("tacheCode", AffairDispatchOrderConstant.AFFAIR_REVIEW_CHECK);
                rowCount = dispatchOrderManageDao.countDisponeAffairOrderList(param);
                if (rowCount != 0 && isGetData) {
                    List<Map<String, Object>> mapList = dispatchOrderManageDao.selectDisponseAffairOrderList(param);
                    rest.put("data", mapList);
                }
                break;
            default:
                break;
        }

        rest.put("dataLength", rowCount);
        rest.put("flag", true);
        return rest;
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
