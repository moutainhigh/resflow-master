package com.zres.project.localnet.portal.logInfo.service;

import java.util.*;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderQrySecondaryDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.logInfo.dao.LoggerInfoDao;
import com.zres.project.localnet.portal.logInfo.entry.ToKafkaTacheLog;
import com.zres.project.localnet.portal.logInfo.entry.ToOrderCenterLog;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;

import com.ztesoft.res.frame.core.util.MapUtil;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;

@Service
public class ToOrderCenterService implements ToOrderCenterIntf {

    Logger logger = LoggerFactory.getLogger(ToOrderCenterService.class);

    @Autowired
    private WebServiceDao webServiceDao;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private OrderQrySecondaryDao orderQrySecondaryDao;
    @Autowired
    private LoggerInfoDao loggerInfoDao;
    @Autowired
    private LogPushService logPushService;

    @Override
    public Map<String, Object> toOrderCenterIntf(String xmlStr) {
        Map<String, Object> resMap = new HashMap<String, Object>();
        boolean result = false;
        //String url = "http://10.245.6.226:8089/server/logService/add";
        String url = webServiceDao.queryUrl("LogPush");
        //创建httpclient工具对象
        HttpClient client = new HttpClient();
        //创建post请求方法
        PostMethod myPost = new PostMethod(url);
        //设置请求超时时间
        //client.getHttpConnectionManager().getParams().setConnectionTimeout(300*1000);
        try {
            /*myPost.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,"utf-8");
            myPost.setParameter("body",xmlStr);*/
            myPost.setRequestHeader("Content-Type", "application/xml");
            myPost.setRequestEntity(new StringRequestEntity(xmlStr, "application/xml", "utf-8"));
            int statusCode = client.executeMethod(myPost);
            if (statusCode == HttpStatus.SC_OK) {
                result = true;
            }
            String resStr = myPost.getResponseBodyAsString();

            resMap.put("sucess", result);
            resMap.put("message", resStr);
        }
        catch (Exception e) {
            e.printStackTrace();
            resMap.put("sucess", result);
            resMap.put("message", "接口调用异常！！！");
        }
        finally {
            myPost.releaseConnection();
        }
        return resMap;
    }

    /**
     * 拼接xml
     *
     * @return
     */
    @Override
    public String appendXml(ToOrderCenterLog toOrderCenterLog) {
        // 1、创建document对象
        Document document = DocumentHelper.createDocument();
        // 2、创建根节点rss
        Element logServiceModel = document.addElement("model.LogServiceModel");
        // 3、生成子节点及子节点内容
        Element circuitId = logServiceModel.addElement("circuitId");
        circuitId.setText(toOrderCenterLog.getCircuitId().toString());
        Element crmProdInstId = logServiceModel.addElement("crmProdInstId");
        crmProdInstId.setText(toOrderCenterLog.getCrmProdInstId());
        Element custOrderNo = logServiceModel.addElement("custOrderNo");
        custOrderNo.setText(toOrderCenterLog.getCustOrderNo());
        Element sysAreaId = logServiceModel.addElement("sysAreaId");
        sysAreaId.setText(toOrderCenterLog.getSysAreaId().toString());
        Element dataSources = logServiceModel.addElement("dataSources");
        dataSources.setText(toOrderCenterLog.getDataSources());
        Element operator = logServiceModel.addElement("operator");
        operator.setText(toOrderCenterLog.getOperator());
        Element operate = logServiceModel.addElement("operate");
        operate.setText(toOrderCenterLog.getOperate());
        Element operationRemark = logServiceModel.addElement("operationRemark");
        if (!StringUtil.isEmpty(toOrderCenterLog.getOperationRemark())) {
            operationRemark.setText(toOrderCenterLog.getOperationRemark());
        }
        Element operationSegment = logServiceModel.addElement("operationSegment");
        operationSegment.setText(toOrderCenterLog.getOperationSegment());
        Element isDel = logServiceModel.addElement("isDel");
        isDel.setText(toOrderCenterLog.getIsDel() + "");
        Element isMerge = logServiceModel.addElement("isMerge");
        isMerge.setText(toOrderCenterLog.getIsMerge() + "");
        Element createBy = logServiceModel.addElement("createBy");
        createBy.setText(toOrderCenterLog.getCreateBy() + "");
        Element updateBy = logServiceModel.addElement("updateBy");
        updateBy.setText(toOrderCenterLog.getUpdateBy() + "");
        Element timeOut = logServiceModel.addElement("timeOut");
        timeOut.setText(toOrderCenterLog.getTimeOut() + "");
        return document.asXML();
    }

    /**
     * 解析xml
     *
     * @return
     */
    @Override
    public String analysisXml(String xmlStr) {
        try {
            Document document = DocumentHelper.parseText(xmlStr);
            Element resultModel = document.getRootElement();
            String code = resultModel.elementTextTrim("code");
            String message = resultModel.elementTextTrim("message");
            for (Iterator i = resultModel.elementIterator("data"); i.hasNext(); ) {
                Element data = (Element) i.next();
                for (Iterator j = data.elementIterator(); j.hasNext(); ) {
                    Element node = (Element) j.next();
                    logger.info(">>>>>>>>>>>>>" + node.getName() + ":" + node.getText());
                }
            }
            logger.info(">>>>日志推送接口调用返回>>>>>" + code + message);
        }
        catch (DocumentException e) {
            System.out.println(e.getMessage());
        }
        return "";
    }

    @Override
    public void pushCenterLog(ToOrderCenterLog toOrderCenterLog) {
        /**
         * 用orderid查询对应的业务电路信息
         *      先判断日志是否要推送给订单中心
         *         只有一干来单和集客来单需要推送给订单中心
         */
        Map<String, Object> params = toOrderCenterLog.getParams();
        String orderId = MapUtils.getString(params, "orderId");
        Map<String, String> cstOrderDataMap = getCstOrdInfo(orderId);
        if (MapUtil.isNotEmpty(cstOrderDataMap)) {
            String resources = MapUtil.getString(cstOrderDataMap, "RESOURCES");
            if ("onedry,jike".indexOf(resources) != -1 && !"".equals(resources)) { //一干和集客来的单子才需要推送日志给订单中心
                //推送日志到订单中心
                toOrderCenterLog(params, cstOrderDataMap);
            }
        }
    }

    private Map<String, String> getCstOrdInfo(String orderId) {
        Map<String, Object> parentOrder = orderDealDao.getParentOrder(orderId); //查询是否存在父订单
        if (MapUtils.isNotEmpty(parentOrder) && !"".equals(MapUtils.getString(parentOrder, "PARENTORDERID"))) {
            orderId = MapUtils.getString(parentOrder, "PARENTORDERID");
        }
        Map<String, String> cstOrderDataMap = new HashMap<String, String>();
        Map<String, Object> ifFromSecondaryMap = orderQrySecondaryDao.qryIfFromSecondary(orderId);
        if (!MapUtils.isEmpty(ifFromSecondaryMap)) {
            //二干下发
            cstOrderDataMap = orderDealDao.qryCstOrderDataFromSec(orderId);
        }
        else {
            cstOrderDataMap = orderDealDao.qryCstOrderData(orderId);
        }
        return cstOrderDataMap;
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
        tacheLogDataMap.put("resources", "LOCAL_NET"); //来源，本地调度就传本地调度
        tacheLogDataMap.put("remark", remark);
        tacheLogDataMap.put("tacheName", orderDealDao.qryTacheName(MapUtils.getString(params, "tacheId")));
        tacheLogDataMap.put("sysAreaId", webServiceDao.qryRegionCode(MapUtil.getString(cstOrderDataMap, "HANDLE_DEP_ID")));
        tacheLogDataMap.put("action", "进行 " + action + " 操作");
        tacheLogDataMap.put("operStaffName", MapUtil.getString(params, "operStaffName"));
        int timeOut = 1; //默认不超时
        Date reqFinDate = orderDealDao.qryReqFinDate(woId);
        Date currentDate = new Date();
        if (reqFinDate.before(currentDate)) {
            timeOut = 0; //超时
        }
        tacheLogDataMap.put("timeOut", timeOut);
        String xmlStr = this.executeToOrderCenter(tacheLogDataMap);
        Map<String, Object> dataInfoMap = this.toOrderCenterIntf(xmlStr);
        if (MapUtils.getBoolean(dataInfoMap, "success")) {
            this.analysisXml(MapUtils.getString(dataInfoMap, "message"));
        }
    }

    private String executeToOrderCenter(Map<String, Object> logDataMap) {
        ToOrderCenterLog toOrderCenterLog = new ToOrderCenterLog();
        toOrderCenterLog.setCircuitId(Long.parseLong(MapUtil.getString(logDataMap, "srvOrdId")));
        toOrderCenterLog.setCreateBy(Integer.parseInt(MapUtil.getString(logDataMap, "operStaffId")));
        toOrderCenterLog.setCrmProdInstId(MapUtil.getString(logDataMap, "serialNumber"));
        toOrderCenterLog.setCustOrderNo(MapUtil.getString(logDataMap, "applyOrdId"));
        toOrderCenterLog.setDataSources(MapUtil.getString(logDataMap, "resources"));
        toOrderCenterLog.setIsDel(0);
        toOrderCenterLog.setIsMerge(0);
        toOrderCenterLog.setOperationRemark(MapUtil.getString(logDataMap, "remark")); //操作备注
        toOrderCenterLog.setOperationSegment(MapUtil.getString(logDataMap, "tacheName")); //环节
        toOrderCenterLog.setSysAreaId(Long.parseLong(MapUtil.getString(logDataMap, "sysAreaId")));
        toOrderCenterLog.setUpdateBy(Integer.parseInt(MapUtil.getString(logDataMap, "operStaffId")));
        toOrderCenterLog.setOperate(MapUtil.getString(logDataMap, "action"));
        toOrderCenterLog.setOperator(MapUtil.getString(logDataMap, "operStaffName"));
        toOrderCenterLog.setTimeOut(MapUtil.getIntValue(logDataMap, "timeOut"));
        return this.appendXml(toOrderCenterLog);
    }

    @Override
    public void pushKafkaLog(ToKafkaTacheLog toKafkaTacheLog) {
        String orderId = toKafkaTacheLog.getBase_order_id();
        String woId = toKafkaTacheLog.getSheet_id();
        String sysResouce = toKafkaTacheLog.getSys_resouce();
        Map<String, String> cstOrderDataMap = toKafkaTacheLog.getCstOrderDataMap(); //发起子流程
        if (MapUtils.isEmpty(cstOrderDataMap)) {
            cstOrderDataMap = getCstOrdInfo(orderId);
        }
        String resources = MapUtil.getString(cstOrderDataMap, "RESOURCES");
        if ("onedry,jike".indexOf(resources) != -1 && !"".equals(resources)) { //一干和集客来的单子才需要推送日志到kafka
            Map<String, String> qryParams = new HashMap<>();
            qryParams.put("orderId", orderId);
            if (StringUtils.isNotEmpty(woId)) { //一干来单没woid
                qryParams.put("woId", woId);
            }
            Map<String, Object> provinceData = new HashMap<>();
            Map<String, Object> cityData = new HashMap<>();
            String azFlag = "B";
            String province = "";
            String srvOrdId = MapUtil.getString(cstOrderDataMap, "SRV_ORD_ID");
            String belongSystem = MapUtil.getString(cstOrderDataMap, "SYSTEM_RESOURCE");
            if (BasicCode.SECOND.equals(belongSystem)) {
                provinceData = orderDealDao.qryProvinceData(MapUtil.getString(cstOrderDataMap, "HANDLE_DEP_ID")); //省份
                if ("2".equals(sysResouce)) {
                    cityData = provinceData;
                    toKafkaTacheLog = loggerInfoDao.qryOrderInfo(qryParams);
                    if (BasicCode.ONEDRY.equals(resources)) {
                        province = MapUtil.getString(cstOrderDataMap, "REMARK");
                    }
                    else if (BasicCode.JIKE.equals(resources)) {
                        //province = MapUtil.getString(cstOrderDataMap, "HANDLE_DEP_ID");
                        province = MapUtil.getString(provinceData, "NAME");
                    }
                }
                else {
                    cityData = orderDealDao.qryCityData(MapUtil.getString(cstOrderDataMap, "REGION_ID")); //地市
                    toKafkaTacheLog = loggerInfoDao.qryOrderInfoSec(qryParams);
                    province = MapUtil.getString(cstOrderDataMap, "AREAID");
                }
            }
            else if (BasicCode.LOCAL.equals(belongSystem)) {
                provinceData = orderDealDao.qryProvinceData(MapUtil.getString(cstOrderDataMap, "PARENT_ID")); //省份
                cityData = orderDealDao.qryCityData(MapUtil.getString(cstOrderDataMap, "HANDLE_DEP_ID")); //地市
                toKafkaTacheLog = loggerInfoDao.qryOrderInfoLocal(qryParams);
                if (BasicCode.ONEDRY.equals(resources)) {
                    province = MapUtil.getString(cstOrderDataMap, "REMARK");
                }
                else if (BasicCode.JIKE.equals(resources)) {
                    province = MapUtil.getString(cstOrderDataMap, "HANDLE_DEP_ID");
                }
            }
            /**
             * 不需要推送的环节：
             *      投资估算  新建资源录入
             *     这个在sql中限制了
             */
            if (StringUtils.isNotEmpty(toKafkaTacheLog.getService_id())) {
                //如果是集客直接下给本地的，或者集客下二干，二干下本地，本地回单二干单子的时候都推b，其他情况需要查询
                if("jike".equals(resources) && (BasicCode.LOCAL.equals(belongSystem) || "2".equals(sysResouce))){
                    azFlag = "B";
                }else {
                    Map<String, Object> azPortInfo = orderDealDao.qryAZPortInfo(srvOrdId, province);
                    if (MapUtils.isNotEmpty(azPortInfo)) {
                        azFlag = MapUtils.getString(azPortInfo, "AZPORT");
                    }
                    if ("B".equals(azFlag)) {
                        return;
                    }
                }
                toKafkaTacheLog.setProvince_inf(MapUtils.getString(provinceData, "NAME"));
                toKafkaTacheLog.setProvince_id(MapUtils.getString(provinceData, "CRM_REGION"));
                toKafkaTacheLog.setCity_inf(MapUtils.getString(cityData, "NAME"));
                toKafkaTacheLog.setCity_id(MapUtils.getString(cityData, "CRM_REGION"));
                toKafkaTacheLog.setGroup_type(azFlag);
                logPushService.pushLogKafka(toKafkaTacheLog);
            }
        }
    }

    /*private void toKafkaTacheLog(Map<String, Object> params, Map<String, String> cstOrderDataMap, Map<String, Object> operStaffInfoMap) {
        String woId = MapUtils.getString(params, "woId");
        String remark = MapUtils.getString(params, "remark");
        String action = MapUtils.getString(params, "action");
        String resources = MapUtil.getString(cstOrderDataMap, "RESOURCES");
        String srvOrdId = MapUtil.getString(cstOrderDataMap, "SRV_ORD_ID");
        String belongSystem = MapUtil.getString(cstOrderDataMap, "SYSTEM_RESOURCE");
        String azFlag = "";
        Map<String, Object> provinceData = new HashMap<>();
        Map<String, Object> cityData = new HashMap<>();
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
        if (BasicCode.SECOND.equals(belongSystem)){
            provinceData = orderDealDao.qryProvinceData(MapUtil.getString(cstOrderDataMap, "HANDLE_DEP_ID")); //省份
            cityData = orderDealDao.qryCityData(MapUtil.getString(cstOrderDataMap, "REGION_ID")); //地市
        }if (BasicCode.LOCAL.equals(belongSystem)){
            provinceData = orderDealDao.qryProvinceData(MapUtil.getString(cstOrderDataMap, "PARENT_ID")); //省份
            cityData = orderDealDao.qryCityData(MapUtil.getString(cstOrderDataMap, "HANDLE_DEP_ID")); //地市
        }
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
        Map serviceMap = orderDealDao.qryServiceId(MapUtil.getString(cstOrderDataMap, "SERVICE_ID"));
        Map<String, Object> tacheLogDataMap = new HashMap();
        tacheLogDataMap.put("id", MapUtil.getString(cstOrderDataMap, "CST_ORD_ID"));
        tacheLogDataMap.put("sheet_id", woId);
        tacheLogDataMap.put("belong", "订单中心");
        tacheLogDataMap.put("service_id", MapUtil.getString(serviceMap, "CODE_INFO"));
        tacheLogDataMap.put("order_title", MapUtil.getString(serviceMap, "CODE_NAME"));
        tacheLogDataMap.put("order_type", MapUtil.getString(cstOrderDataMap, "ORDER_TYPE"));
        tacheLogDataMap.put("group_type", azFlag);
        tacheLogDataMap.put("active_type", MapUtil.getString(cstOrderDataMap, "ACTIVE_TYPE"));
        tacheLogDataMap.put("sys_resouce", "0");
        tacheLogDataMap.put("cust_name", MapUtil.getString(cstOrderDataMap, "CUST_NAME_CHINESE"));
        tacheLogDataMap.put("contact_name", MapUtil.getString(params, "operStaffName"));
        tacheLogDataMap.put("tache_service_id", MapUtil.getString(serviceMap, "CODE_INFO") + MapUtil.getString(woOrderInfoMap, "ID"));
        tacheLogDataMap.put("org_id", MapUtil.getString(operStaffInfoMap, "ORG_ID"));
        tacheLogDataMap.put("org_name", MapUtil.getString(operStaffInfoMap, "ORG_NAME"));
        tacheLogDataMap.put("contact_nbr", MapUtils.getString(operStaffInfoMap, "USER_PHONE"));
        tacheLogDataMap.put("tache_define_id", MapUtil.getString(woOrderInfoMap, "ID"));
        tacheLogDataMap.put("tache_name", MapUtil.getString(woOrderInfoMap, "TACHE_NAME"));
        tacheLogDataMap.put("area_id", MapUtil.getString(woOrderInfoMap, "REGION_ID"));
        tacheLogDataMap.put("area_name", MapUtil.getString(woOrderInfoMap, "DEPT_NAME"));
        tacheLogDataMap.put("character_value", MapUtil.getString(cstOrderDataMap, "TRADE_TYPE_CODE"));
        tacheLogDataMap.put("accept_orgname", MapUtil.getString(cstOrderDataMap, "HANDLE_DEP"));
        tacheLogDataMap.put("order_code", MapUtil.getString(cstOrderDataMap, "TRADE_ID"));
        tacheLogDataMap.put("acc_nbr", MapUtil.getString(cstOrderDataMap, "SERIAL_NUMBER"));
        tacheLogDataMap.put("accept_date", MapUtil.getString(cstOrderDataMap, "CREATE_DATE"));

        tacheLogDataMap.put("create_date", MapUtil.getString(woOrderInfoMap, "CREATE_DATE"));
        tacheLogDataMap.put("finish_date", MapUtil.getString(woOrderInfoMap, "STATE_DATE"));
        tacheLogDataMap.put("work_order_state", MapUtil.getString(woOrderInfoMap, "WO_STATE"));
        tacheLogDataMap.put("work_order_state_name", MapUtil.getString(woOrderInfoMap, "WO_STATE_NAME"));
        tacheLogDataMap.put("moniter_work_time", MapUtil.getString(cstOrderDataMap, "SERIAL_NUMBER"));
        tacheLogDataMap.put("uos_tache_limit", MapUtil.getString(cstOrderDataMap, "SERIAL_NUMBER"));
        tacheLogDataMap.put("outtime", MapUtil.getString(cstOrderDataMap, "SERIAL_NUMBER"));




        tacheLogDataMap.put("custOrderNo", MapUtil.getString(cstOrderDataMap, "APPLY_ORD_ID"));
        tacheLogDataMap.put("serialNumber", MapUtil.getString(cstOrderDataMap, "SERIAL_NUMBER"));
        //tacheLogDataMap.put("acticeType", MapUtil.getString(cstOrderDataMap, "ACTIVENAME"));

        tacheLogDataMap.put("tacheName", MapUtil.getString(woOrderInfoMap, "TACHE_NAME"));
        tacheLogDataMap.put("tacheCode", MapUtil.getString(woOrderInfoMap, "TACHE_CODE"));
        tacheLogDataMap.put("provinceId", MapUtil.getString(provinceData, "CRM_REGION"));
        tacheLogDataMap.put("provinceName", MapUtil.getString(provinceData, "NAME"));
        tacheLogDataMap.put("areaId", MapUtil.getString(cityData, "CRM_REGION"));
        tacheLogDataMap.put("areaName", MapUtil.getString(cityData, "NAME"));
        tacheLogDataMap.put("dept", MapUtil.getString(operStaffInfoMap, "ORG_NAME"));
        tacheLogDataMap.put("role", MapUtil.getString(woOrderInfoMap, "ROLENAME"));
        tacheLogDataMap.put("dealPerson", MapUtil.getString(params, "operStaffName"));
        tacheLogDataMap.put("phone", MapUtils.getString(operStaffInfoMap, "USER_PHONE"));
        tacheLogDataMap.put("operaContext", action + remark);
        tacheLogDataMap.put("operaTime", operaTime);
        tacheLogDataMap.put("complateTime", MapUtil.getString(woOrderInfoMap, "STATE_DATE"));
        tacheLogDataMap.put("dataSources", resources);
        tacheLogDataMap.put("sysSources", "Local");
        tacheLogDataMap.put("isTimeOut", timeOut);
        tacheLogDataMap.put("woState",  MapUtil.getString(woOrderInfoMap, "WO_STATE"));

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
        //LoggerThreadPool.addLoggerToExecute(resInterfaceLogger,toOrderCenterTacheLog);
    }*/
}
