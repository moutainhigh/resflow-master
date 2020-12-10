package com.zres.project.localnet.portal.logInfo.service;

import java.util.*;

import com.github.pagehelper.util.StringUtil;
import com.zres.project.localnet.portal.local.domain.PageInfo;
import com.ztesoft.res.frame.user.inf.UserInfo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.logInfo.entry.ToKafkaTacheLog;
import com.zres.project.localnet.portal.logInfo.entry.ToOrderCenterLog;
import com.zres.project.localnet.portal.logInfo.until.LoggerThreadPool;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.res.frame.core.util.MapUtil;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import org.springframework.util.CollectionUtils;

@Service
public class TacheDealLogService implements TacheDealLogIntf {

    Logger logger = LoggerFactory.getLogger(TacheDealLogService.class);

    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private WebServiceDao webServiceDao;
    @Autowired
    private ResInterfaceLogger resInterfaceLogger;

    @Override
    public Map<String, Object> addTrackLog(Map<String, Object> params) {
        /*String operStaffId = "";
        String operStaffName = "";
        if (BasicCode.RENT.equals(MapUtils.getString(params, "tacheId"))) { // 起租环节是一干来的
            operStaffId = "-1";
            operStaffName = "一干";
        } else {
            operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
            operStaffName = ThreadLocalInfoHolder.getLoginUser().getUserName();
        }*/
        Map<String, Object> operStaffInfoMap = null;
        if(params.containsKey("operStaffInfoMap")){
            operStaffInfoMap = MapUtils.getMap(params, "operStaffInfoMap");
        }else {
            operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(ThreadLocalInfoHolder.getLoginUser().getUserId()));
        }
        String operStaffId = MapUtil.getString(operStaffInfoMap, "STAFF_ID");
        String operStaffName = MapUtil.getString(operStaffInfoMap, "USER_REAL_NAME");
        Map<String, Object> resMap = new HashMap<String, Object>();
        String woId = MapUtils.getString(params, "woId");
        String action = MapUtils.getString(params, "action");
        String operType = MapUtils.getString(params, "operType");
        String remark = MapUtils.getString(params, "remark");
        try {
            Map<String, Object> paramsMap = new HashMap<String, Object>();
            paramsMap.put("orderId", MapUtils.getString(params, "orderId"));
            paramsMap.put("woOrdId", woId);
            paramsMap.put("trackOrgId", MapUtil.getString(operStaffInfoMap, "ORG_ID"));
            paramsMap.put("trackOrgName", MapUtil.getString(operStaffInfoMap, "ORG_NAME"));
            paramsMap.put("trackDate", new java.sql.Date(new java.util.Date().getTime()));
            paramsMap.put("createDate", new java.sql.Date(new java.util.Date().getTime()));
            paramsMap.put("trackStaffId", operStaffId);
            paramsMap.put("trackStaffName", operStaffName);
            paramsMap.put("trackStaffPhone", MapUtils.getString(operStaffInfoMap, "USER_PHONE"));
            paramsMap.put("trackStaffEmail", MapUtils.getString(operStaffInfoMap, "USER_EMAIL"));
            //modify by wang.gang2 抄送 mmp  非要加到这
            StringBuffer trackMessage = new StringBuffer();
            trackMessage.append("[" ).append( operStaffName)
                    .append("将工单单号：")
                    .append(woId).append("]")
                    .append(MapUtils.getString(params, "trackMessage"));
            if("抄送".equals(MapUtils.getString(params, "action"))){
                String staffId = MapUtils.getString(params, "staffId","0");
                String[] orjIdsplit = staffId.split(",");
                for(String objIdItem : orjIdsplit) {
                    Map<String, Object> operStaff  = orderDealDao.getOperStaffInfo(Integer.valueOf(objIdItem));
                    if(operStaff != null && operStaff.size() > 0){
                        trackMessage.append( MapUtils.getString(operStaff, "USER_REAL_NAME")).append(":")
                                .append(MapUtils.getString(operStaff, "ORG_NAME")).append(",");
                    }
                }
                trackMessage.append("[抄送意见]").append(MapUtils.getString(params, "opinion"," "));
            }
            if(StringUtil.isNotEmpty(MapUtils.getString(params, "opinion",""))
                    && !"抄送".equals(MapUtils.getString(params, "action"))){
                String staffId = MapUtils.getString(params, "staffId","0");
                Map<String, Object> operStaff  = orderDealDao.getOperStaffInfo(Integer.valueOf(staffId));
                if(operStaff != null && operStaff.size() > 0){
                    trackMessage.append( MapUtils.getString(operStaff, "USER_REAL_NAME")).append(":")
                            .append(MapUtils.getString(operStaff, "ORG_NAME")).append(",");
                }
                trackMessage.append("[超时原因]").append(MapUtils.getString(params, "opinion"," "));
            }
            paramsMap.put("trackMessage", trackMessage.toString());
            String trackContent = "[" + action + "]";
            if (remark != null) {
                trackContent = trackContent + remark ;
            }
            paramsMap.put("trackContent", trackContent);
            paramsMap.put("operType", operType);
            orderDealDao.insertTrackLogInfo(paramsMap);
            /*resMap.put("success", true);
            params.put("operStaffId", operStaffId);
            params.put("operStaffName", operStaffName);
            this.pushOrderCenter(params, operStaffInfoMap);*/
            //推送订单中心
            this.pushOrderLogToCenter(params);
            //推送kafka
            if ("4,5,11".indexOf(operType) != -1){ // 回单 退单 启子流程 回退
                this.pushOrderLogToKafka(MapUtils.getString(params, "orderId"), woId);
            }
        } catch (Exception e) {
            logger.error(action + "失败：", e);
            resMap.put("success", false);
            resMap.put("message", action + "失败!" + e);
        }
        return resMap;
    }

    @Override
    public void pushOrderLogToCenter(Map<String, Object> params) {
        ToOrderCenterLog toOrderCenterLog =new ToOrderCenterLog();
        toOrderCenterLog.setParams(params);
        LoggerThreadPool.addLoggerToExecute(resInterfaceLogger, toOrderCenterLog);
    }

    @Override
    public void pushOrderLogToKafka(String orderId, String woId) {
        ToKafkaTacheLog toKafkaTacheLog =new ToKafkaTacheLog();
        toKafkaTacheLog.setSheet_id(woId);
        toKafkaTacheLog.setBase_order_id(orderId);
        LoggerThreadPool.addLoggerToExecute(resInterfaceLogger, toKafkaTacheLog);
    }

    /**
     * 资源配置完提交后将进行主流程及子流程工单消息推送操作
     *
     * @param orderId     订单ID
     * @param messageType 消息类型 （回单或者退单）
     * @param woId        工单ID
     * @author wangsen
     */
    @Override
    public void writeOrderMessage(String orderId, String woId, String messageType, String messageFlag, String remark) {
        logger.info("......消息推送......");
        Map<String, String> params = new HashMap<String, String>();
        UserInfo userInfo = ThreadLocalInfoHolder.getLoginUser(); //环节环节提交处理人ID
        String staffId = "";
        String user_name= "";
        if (userInfo != null){
            staffId = userInfo.getUserId(); //环节环节提交处理人ID
            user_name = userInfo.getUserName(); //环节环节提交处理人
        }

        String prodType = ""; // 产品类型
        String prodId = ""; //产品ID
        String spec_name = ""; //专业
        String allpyOrderId = ""; //申请单号
        String orderNo = ""; //订单编号
        String serialNumber = ""; //业务号码
        String dispObjTye = ""; //处理专业
        String dispObjId = ""; //处理人员
        String messageAlias = ""; //消息描述
        try {
            /**
             * add by CWY
             * 这里判单通知单类别，messagFlag:childFlow(资源配置完成派发同级子环节)
             *                  messagFlag:dealFlow(派发订单当前所在环节)
             *
             */
            List<Map<String, Object>> childFlow = new ArrayList<>();
            spec_name = orderDealDao.querySpecialtyNameByOrderId(orderId); //当前提交专业
            Map orderMap = orderDealDao.qryParentOrdIdByorderId(orderId);
            String parentOrderId = MapUtils.getString(orderMap, "PARENT_ORDER_ID", "");
            if ("dealFlow".equals(messageFlag)) {
                childFlow = orderDealDao.queryDealFlow(parentOrderId, woId);
            }
            else {
                childFlow = orderDealDao.queryChildFlow(orderId, woId);
            }

            Set<String> dispObjSet = new HashSet<>();
            if (childFlow != null && !childFlow.isEmpty()) {
                for (int i = 0; i < childFlow.size(); i++) {
                    Map<String, Object> map = childFlow.get(i);
                    prodType = MapUtils.getString(map, "PROD_TYPE");
                    prodId = MapUtils.getString(map, "PROD_ID");
                    allpyOrderId = MapUtils.getString(map, "APPLY_ORD_ID");
                    orderNo = MapUtils.getString(map, "TRADE_ID");
                    serialNumber = MapUtils.getString(map, "SERIAL_NUMBER");
                    dispObjTye = MapUtils.getString(map, "DISP_OBJ_TYE");
                    dispObjId = MapUtils.getString(map, "DISP_OBJ_ID");

                    //如果多个专业都在同一个岗位下或者同一个人员账号下，只需要发送一条即可
                    if(dispObjSet.contains(dispObjTye + dispObjId)){
                        continue;
                    }else{
                        dispObjSet.add(dispObjTye + dispObjId);
                    }

                    if ("退单".equals(messageType)) {
                        messageAlias = "已完成退单操作";
                    }
                    else if ("回单".equals(messageType)) {
                        messageAlias = "已完成资源配置";
                    }
                    else if ("资源修改".equals(messageType)) {
                        messageAlias = "已进行资源修改";
                    }
                    params.put("prodType", prodType);
                    params.put("applyOrder", allpyOrderId);
                    params.put("oedreNo", orderNo);
                    params.put("dispObjTye", dispObjTye);
                    params.put("dispObjId", dispObjId);
                    params.put("messageState", messageType);
                    params.put("messageAlias", messageAlias);
                    params.put("orderId", orderId);
                    params.put("woId", woId);
                    params.put("userName", user_name);
                    params.put("userId", staffId);
                    params.put("serialNumber", serialNumber);
                    params.put("prodId", prodId);
                    params.put("specName", spec_name);
                    orderDealDao.writeOrderMessage(params);
                }
            }
            else {
                int send = orderDealDao.isSendMessage(orderId, woId);
                if (send != 0) {
                    orderDealDao.updateMessage(orderId, woId, "已处理");
                }
            }
        }
        catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * 消息列表分页查询
     *5
     * @param params
     * @author wangsen
     */
    @Override
    public Map<String, Object> queryMessageList(Map<String, Object> params) {
        List<Map<String, Object>> mapListT = new ArrayList<Map<String, Object>>();
        PageInfo pageInfo = new PageInfo(); //分页信息
        pageInfo.setIndexSizeData(params.get("pageIndex"), params.get("pageSize"));
        params.put("startRow", pageInfo.getRowStart()); //分页开始行
        params.put("endRow", pageInfo.getRowEnd()); //分页结束行
        Map<String, Object> map = new HashMap<String, Object>();
        pageInfo.setIndexSizeData(params.get("pageIndex"), params.get("pageSize"));
        params.put("userId", MapUtils.getString(params, "userId")); //当前登录用户id
        int woCount = orderDealDao.countMessageList(params);
        if (woCount != 0) {
            List<Map<String, Object>> maps = orderDealDao.queryMessageList(params);
            if (!CollectionUtils.isEmpty(maps)) {
                mapListT.addAll(maps);
            }
        }
        pageInfo.setDataCount(woCount);
        map.put("dataLength", woCount);
        map.put("data", mapListT);
        map.put("flag", "1");
        map.put("pageIndex", pageInfo.getCurrentPage());
        map.put("rowNum", pageInfo.getPageSize());
        map.put("total", pageInfo.getPageCount());
        return map;
    }

    /*@Override
    public void pushOrderCenter(Map<String, Object> params, Map<String, Object> operStaffInfoMap) {
        *//**
         * 1，用orderid查询对应的业务电路信息
         *      先判断日志是否要推送给订单中心
         *         只有一干来单和集客来单需要推送给订单中心
         * 2，需要推送的话，调用ToOrderCenterIntf接口executeToOrderCenter方法
         *
         *//*
        String operType = MapUtils.getString(params, "operType");
        String orderId = MapUtils.getString(params, "orderId");
        Map<String, Object> parentOrder = orderDealDao.getParentOrder(orderId); //查询是否存在父订单
        if(MapUtils.isNotEmpty(parentOrder) && !"".equals(MapUtils.getString(parentOrder, "PARENTORDERID"))){
            orderId = MapUtils.getString(parentOrder, "PARENTORDERID");
        }
        Map<String, String> cstOrderDataMap = orderDealDao.qryCstOrderData(orderId);
        if (MapUtil.isNotEmpty(cstOrderDataMap)){
            String resources = MapUtil.getString(cstOrderDataMap, "RESOURCES");
            if ("onedry,jike".indexOf(resources) != -1 && !"".equals(resources)){ //一干和集客来的单子才需要推送日志给订单中心
                toOrderCenterLog(params, cstOrderDataMap);
                if ("2,3,4,5,10,11".indexOf(operType) != -1){ //签收 释放签收 回单 退单 启子流程 回退
                    toOrderCenterTacheLog(params, cstOrderDataMap, operStaffInfoMap);
                }
            }
        }
    }

    private void toOrderCenterLog(Map<String, Object> params, Map<String, String> cstOrderDataMap) {
        Map<String, Object> tacheLogDataMap = new HashMap();
        String action = MapUtils.getString(params, "action");
        String remark = MapUtils.getString(params, "remark");
        String woId = MapUtils.getString(params, "woId");
        tacheLogDataMap.put("srvOrdId", MapUtil.getString(cstOrderDataMap, "SRV_ORD_ID"));
        tacheLogDataMap.put("operStaffId", MapUtil.getString(params, "operStaffId"));
        tacheLogDataMap.put("serialNumber", MapUtil.getString(cstOrderDataMap, "SERIAL_NUMBER"));
        tacheLogDataMap.put("applyOrdId", MapUtil.getString(cstOrderDataMap, "APPLY_ORD_ID"));
        tacheLogDataMap.put("resources", "TWO_DRY"); //来源，二干调度就传二干调度
        tacheLogDataMap.put("remark", remark);
        tacheLogDataMap.put("tacheName", orderDealDao.qryTacheName(MapUtils.getString(params, "tacheId")));
        tacheLogDataMap.put("sysAreaId", webServiceDao.qryRegionCode(MapUtil.getString(cstOrderDataMap, "HANDLE_DEP_ID")));
        tacheLogDataMap.put("action", "进行 " + action + " 操作");
        tacheLogDataMap.put("operStaffName", MapUtil.getString(params, "operStaffName"));
        int timeOut = 1; //默认不超时
        Date reqFinDate = orderDealDao.qryReqFinDate(woId);
        Date currentDate =new Date();
        if (reqFinDate.before(currentDate)){
            timeOut = 0; //超时
        }
        tacheLogDataMap.put("timeOut", timeOut);
        this.executeToOrderCenter(tacheLogDataMap);
    }

    @Override
    public void executeToOrderCenter(Map<String, Object> logDataMap) {
        ToOrderCenterLog toOrderCenterLog =new ToOrderCenterLog();
        toOrderCenterLog.setCircuitId(Long.parseLong(MapUtil.getString(logDataMap,"srvOrdId")));
        toOrderCenterLog.setCreateBy(Integer.parseInt(MapUtil.getString(logDataMap,"operStaffId")));
        toOrderCenterLog.setCrmProdInstId(MapUtil.getString(logDataMap,"serialNumber"));
        toOrderCenterLog.setCustOrderNo(MapUtil.getString(logDataMap,"applyOrdId"));
        toOrderCenterLog.setDataSources(MapUtil.getString(logDataMap,"resources"));
        toOrderCenterLog.setIsDel(0);
        toOrderCenterLog.setIsMerge(0);
        toOrderCenterLog.setOperationRemark(MapUtil.getString(logDataMap,"remark")); //操作备注
        toOrderCenterLog.setOperationSegment(MapUtil.getString(logDataMap,"tacheName")); //环节
        toOrderCenterLog.setSysAreaId(Long.parseLong(MapUtil.getString(logDataMap,"sysAreaId")));
        toOrderCenterLog.setUpdateBy(Integer.parseInt(MapUtil.getString(logDataMap,"operStaffId")));
        toOrderCenterLog.setOperate(MapUtil.getString(logDataMap,"action"));
        toOrderCenterLog.setOperator(MapUtil.getString(logDataMap,"operStaffName"));
        toOrderCenterLog.setTimeOut(MapUtil.getIntValue(logDataMap,"timeOut"));
        LoggerThreadPool.addLoggerToExecute(resInterfaceLogger,toOrderCenterLog);
    }

    private void toOrderCenterTacheLog(Map<String, Object> params, Map<String, String> cstOrderDataMap, Map<String, Object> operStaffInfoMap) {
        String woId = MapUtils.getString(params, "woId");
        String remark = MapUtils.getString(params, "remark");
        String action = MapUtils.getString(params, "action");
        String resources = MapUtil.getString(cstOrderDataMap, "RESOURCES");
        String srvOrdId = MapUtil.getString(cstOrderDataMap, "SRV_ORD_ID");
        String azFlag = "";
        Map<String, Object> provinceData = new HashMap<>();
        if ("onedry".equals(resources)){
            resources = "YZS";
            String province = MapUtil.getString(cstOrderDataMap, "REMARK");
            Map<String, Object> azPortInfo = orderDealDao.qryAZPortInfo(srvOrdId, province);
            if (MapUtils.isNotEmpty(azPortInfo)){
                azFlag = MapUtils.getString(azPortInfo, "AZPORT");
            }
        }else if ("jike".equals(resources)){
            resources = "JIKE";
        }
        provinceData = orderDealDao.qryProvinceData(MapUtil.getString(cstOrderDataMap, "HANDLE_DEP_ID")); //省份 地市

        Map<String, Object> woOrderInfoMap = orderDealDao.qryWoOrderInfo(woId);
        int timeOut = 1; //默认不超时
        Date reqFinDate = (Date) MapUtil.getObject(woOrderInfoMap, "REQ_FIN_DATE");
        Date currentDate =new Date();
        if (reqFinDate.before(currentDate)){
            timeOut = 0; //超时
        }
        String operaTime = MapUtil.getString(woOrderInfoMap, "DEAL_DATE");
        String operType = MapUtils.getString(params, "operType");
        if ("3".equals(operType)){ //释放签收
            operaTime = MapUtils.getString(params, "dealDate");
        }
        Map<String, Object> tacheLogDataMap = new HashMap();
        tacheLogDataMap.put("woId", woId);
        tacheLogDataMap.put("custOrderNo", MapUtil.getString(cstOrderDataMap, "APPLY_ORD_ID"));
        tacheLogDataMap.put("serialNumber", MapUtil.getString(cstOrderDataMap, "SERIAL_NUMBER"));
        tacheLogDataMap.put("acticeType", MapUtil.getString(cstOrderDataMap, "ACTIVENAME"));
        tacheLogDataMap.put("prodType", orderDealDao.qryServiceId(MapUtil.getString(cstOrderDataMap, "SERVICE_ID")));
        tacheLogDataMap.put("tacheName", MapUtil.getString(woOrderInfoMap, "TACHE_NAME"));
        tacheLogDataMap.put("tacheCode", MapUtil.getString(woOrderInfoMap, "TACHE_CODE"));
        tacheLogDataMap.put("provinceId", MapUtil.getString(provinceData, "CRM_REGION"));
        tacheLogDataMap.put("provinceName", MapUtil.getString(provinceData, "NAME"));
        tacheLogDataMap.put("areaId", MapUtil.getString(provinceData, "CRM_REGION"));
        tacheLogDataMap.put("areaName", MapUtil.getString(provinceData, "NAME"));
        tacheLogDataMap.put("dept", MapUtil.getString(operStaffInfoMap, "ORG_NAME"));
        tacheLogDataMap.put("role", MapUtil.getString(woOrderInfoMap, "ROLENAME"));
        tacheLogDataMap.put("dealPerson", MapUtil.getString(params, "operStaffName"));
        tacheLogDataMap.put("phone", MapUtils.getString(operStaffInfoMap, "USER_PHONE"));
        tacheLogDataMap.put("operaContext", action + remark);
        tacheLogDataMap.put("operaTime", operaTime);
        tacheLogDataMap.put("complateTime", MapUtil.getString(woOrderInfoMap, "STATE_DATE"));
        tacheLogDataMap.put("dataSources", resources);
        tacheLogDataMap.put("sysSources", "twoDry");
        tacheLogDataMap.put("isTimeOut", timeOut);
        tacheLogDataMap.put("woState",  MapUtil.getString(woOrderInfoMap, "WO_STATE"));
        tacheLogDataMap.put("flag", azFlag);
        tacheLogDataMap.put("orderType", MapUtil.getString(cstOrderDataMap, "ORDER_TYPE"));
        this.executeToOrderCenterTacheOper(tacheLogDataMap);
    }

    private void executeToOrderCenterTacheOper(Map<String, Object> logDataMap) {
        ToOrderCenterTacheLog toOrderCenterTacheLog =new ToOrderCenterTacheLog();
        toOrderCenterTacheLog.setWo_id(MapUtil.getString(logDataMap, "woId"));
        toOrderCenterTacheLog.setCust_order_no(MapUtil.getString(logDataMap, "custOrderNo"));
        toOrderCenterTacheLog.setSerial_number(MapUtil.getString(logDataMap, "serialNumber"));
        toOrderCenterTacheLog.setActice_type(MapUtil.getString(logDataMap, "acticeType"));
        toOrderCenterTacheLog.setProd_type(MapUtil.getString(logDataMap, "prodType"));
        toOrderCenterTacheLog.setTache_name(MapUtil.getString(logDataMap, "tacheName"));
        toOrderCenterTacheLog.setTache_code(MapUtil.getString(logDataMap, "tacheCode"));
        toOrderCenterTacheLog.setProvince_id(MapUtil.getString(logDataMap, "provinceId"));
        toOrderCenterTacheLog.setProvince_name(MapUtil.getString(logDataMap, "provinceName"));
        toOrderCenterTacheLog.setArea_id(MapUtil.getString(logDataMap, "areaId"));
        toOrderCenterTacheLog.setArea_name(MapUtil.getString(logDataMap, "areaName"));
        toOrderCenterTacheLog.setDept(MapUtil.getString(logDataMap, "dept"));
        toOrderCenterTacheLog.setRole(MapUtil.getString(logDataMap, "role"));
        toOrderCenterTacheLog.setDeal_person(MapUtil.getString(logDataMap, "dealPerson"));
        toOrderCenterTacheLog.setPhone(MapUtil.getString(logDataMap, "phone"));
        toOrderCenterTacheLog.setOpera_context(MapUtil.getString(logDataMap, "operaContext"));
        toOrderCenterTacheLog.setOpera_time(MapUtil.getString(logDataMap, "operaTime"));
        toOrderCenterTacheLog.setComplate_time(MapUtil.getString(logDataMap, "complateTime"));
        toOrderCenterTacheLog.setData_sources(MapUtil.getString(logDataMap, "dataSources"));
        toOrderCenterTacheLog.setSys_sources(MapUtil.getString(logDataMap, "sysSources"));
        toOrderCenterTacheLog.setIs_timeout(MapUtil.getString(logDataMap, "isTimeOut"));
        toOrderCenterTacheLog.setWo_state(MapUtil.getString(logDataMap, "woState"));
        toOrderCenterTacheLog.setFlag(MapUtil.getString(logDataMap, "flag"));
        toOrderCenterTacheLog.setOrder_type(MapUtil.getString(logDataMap, "orderType"));
        LoggerThreadPool.addLoggerToExecute(resInterfaceLogger,toOrderCenterTacheLog);
    }*/
    @Override
    public Map<String, Object> deleteMessageList(List<Map<String, Object>> paramList) {
        Map<String, Object> resMap = new HashMap<>();
        resMap.put("success", true);
        int i = 0; //记录删除成功数据条数
        try {
            for(Map<String, Object> paramMap : paramList){
                String messageId = MapUtil.getString(paramMap, "MESSAGE_ID");
                String orderId = MapUtil.getString(paramMap, "ORDER_ID");
                String woId = MapUtil.getString(paramMap, "WO_ID");
                i = i + orderDealDao.deleteMessageList(messageId,orderId, woId);
            }
            resMap.put("message", i);
        }
        catch (Exception e) {
            resMap.put("success", false);
            resMap.put("message", "消息确认发生异常！ "+e.getMessage());
            logger.error("消息确认发生异常！ "+e.getMessage());
        }
        return resMap;
    }
}
