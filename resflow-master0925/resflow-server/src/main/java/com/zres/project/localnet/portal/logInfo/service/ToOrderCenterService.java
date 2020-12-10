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

import static org.apache.zookeeper.ZooDefs.OpCode.delete;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderQrySecondaryDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.logInfo.dao.LoggerInfoDao;
import com.zres.project.localnet.portal.logInfo.entry.ToKafkaTacheLog;
import com.zres.project.localnet.portal.logInfo.entry.ToOrderCenterLog;
import com.zres.project.localnet.portal.logInfo.entry.ToOrderCenterTacheLog;
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
            myPost.setRequestHeader("Content-Type","application/xml");
            myPost.setRequestEntity(new StringRequestEntity(xmlStr,"application/xml","utf-8"));
            int statusCode = client.executeMethod(myPost);
            if(statusCode == HttpStatus.SC_OK){
                result = true;
            }
            String resStr=myPost.getResponseBodyAsString();

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
     * @return
     */
    @Override
    public String appendXml(ToOrderCenterLog toOrderCenterLog){
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
        if (!StringUtil.isEmpty(toOrderCenterLog.getOperationRemark())){
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
     * @return
     */
    @Override
    public String analysisXml(String xmlStr){
        try {
            Document document = DocumentHelper.parseText(xmlStr);
            Element resultModel = document.getRootElement();
            String code = resultModel.elementTextTrim("code");
            String message = resultModel.elementTextTrim("message");
            for (Iterator i = resultModel.elementIterator("data"); i.hasNext();) {
                Element data = (Element) i.next();
                for (Iterator j = data.elementIterator(); j.hasNext();) {
                    Element node = (Element) j.next();
                    logger.info(">>>>>>>>>>>>>" + node.getName() + ":" + node.getText());
                }
            }
            logger.info(">>>>日志推送接口调用返回>>>>>" + code + message);
        } catch (DocumentException e) {
            System.out.println(e.getMessage());
        }/*finally {
            // 插入接口记录
            Map<String, Object> interflog = new HashMap<String, Object>();
            interflog.put("INTERFNAME", "推送订单中日志接口");
            interflog.put("URL", url);
            interflog.put("CONTENT", xmlStr);
            interflog.put("ORDERNO", "");
            interflog.put("RETURNCONTENT", "");
            interflog.put("REMARK", "");
            webServiceDao.insertInterfLog(interflog);
        }*/
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
        if (MapUtil.isNotEmpty(cstOrderDataMap)){
            String resources = MapUtil.getString(cstOrderDataMap, "RESOURCES");
            if ("onedry,jike".indexOf(resources) != -1 && !"".equals(resources)){ //一干和集客来的单子才需要推送日志给订单中心
                //推送日志到订单中心
                toOrderCenterLog(params, cstOrderDataMap);
            }
        }
    }

    private Map<String, String> getCstOrdInfo(String orderId) {
        Map<String, Object> parentOrder = orderDealDao.getParentOrder(orderId); //查询是否存在父订单
        if(MapUtils.isNotEmpty(parentOrder) && !"".equals(MapUtils.getString(parentOrder, "PARENTORDERID"))){
            orderId = MapUtils.getString(parentOrder, "PARENTORDERID");
        }
        Map<String, String> cstOrderDataMap = orderDealDao.qryCstOrderData(orderId);
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
        String xmlStr = this.executeToOrderCenter(tacheLogDataMap);
        Map<String, Object> dataInfoMap = this.toOrderCenterIntf(xmlStr);
        if(MapUtils.getBoolean(dataInfoMap, "success")){
            this.analysisXml(MapUtils.getString(dataInfoMap, "message"));
        }
    }

    private String executeToOrderCenter(Map<String, Object> logDataMap) {
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
        return this.appendXml(toOrderCenterLog);
    }

    /*
     * 1、如果是一干下发的属于az端的 二干和本地都进行推送， 二干是A，Z,本地是A  ，Z  如果不属于az端的不进行推送
       2、如果是集客的，二干是b 本地是a，z 不属于az端的本地不推送
     * @author guanzhao
     * @date 2020/11/23
     *
     */
    @Override
    public void pushKafkaLog(ToKafkaTacheLog toKafkaTacheLog) {
        String orderId = toKafkaTacheLog.getBase_order_id();
        String woId = toKafkaTacheLog.getSheet_id();
        String sysResouce = toKafkaTacheLog.getSys_resouce();
        Map<String, String> cstOrderDataMap = toKafkaTacheLog.getCstOrderDataMap();
        if (MapUtils.isEmpty(cstOrderDataMap)){
            cstOrderDataMap = getCstOrdInfo(orderId);
            //如果是二干下发本地
            if("0".equals(sysResouce)){
                cstOrderDataMap = orderDealDao.qryCstOrderDataFromSec(orderId);
            }
        }
        String resources = MapUtil.getString(cstOrderDataMap, "RESOURCES");
        if ("onedry,jike".indexOf(resources) != -1 && !"".equals(resources)){ //一干和集客来的单子才需要推送日志到kafka
            Map<String, String> qryParams = new HashMap<>();
            qryParams.put("orderId", orderId);
            if(StringUtils.isNotEmpty(woId)){
                qryParams.put("woId", woId);
            }
            Map<String, Object> provinceData = new HashMap<>();
            Map<String, Object> cityData = new HashMap<>();
            String azFlag = "B";
            String province = "";
            String srvOrdId = MapUtil.getString(cstOrderDataMap, "SRV_ORD_ID");
            if("0".equals(sysResouce)){
                provinceData = orderDealDao.qryProvinceData(MapUtil.getString(cstOrderDataMap, "HANDLE_DEP_ID")); //省份
                cityData = orderDealDao.qryCityData(MapUtil.getString(cstOrderDataMap, "REGION_ID")); //地市
                toKafkaTacheLog = loggerInfoDao.qryOrderInfoSec(qryParams);
                province =  MapUtil.getString(cstOrderDataMap, "AREAID");
            }else {
                provinceData = orderDealDao.qryProvinceData(MapUtil.getString(cstOrderDataMap, "HANDLE_DEP_ID")); //省份 地市
                cityData = provinceData;
                toKafkaTacheLog = loggerInfoDao.qryOrderInfo(qryParams);
                if (BasicCode.ONEDRY.equals(resources)){
                    province = MapUtil.getString(cstOrderDataMap, "REMARK");
                }else if (BasicCode.JIKE.equals(resources)){
                    //province = MapUtil.getString(cstOrderDataMap, "HANDLE_DEP_ID");
                    province = MapUtil.getString(provinceData, "NAME");
                }
            }
            if(StringUtils.isNotEmpty(toKafkaTacheLog.getService_id())){
                if ("onedry".equals(resources) || ("0".equals(sysResouce) && "jike".equals(resources))){
                    Map<String, Object> azPortInfo = orderDealDao.qryAZPortInfo(srvOrdId, province);
                    if (MapUtils.isNotEmpty(azPortInfo)){
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

    /*@Override
    public Map<String, Object> toOrderCenterTacheIntf(ToOrderCenterTacheLog toOrderCenterTacheLog) {
        Map<String,Object> toOrderCenterTacheLogMapNew = new HashMap();
        Map<String, Object> toOrderCenterTacheLogMap = JSONObject.parseObject(JSONObject.toJSONString(toOrderCenterTacheLog), Map.class);
        Set<String> setKey = toOrderCenterTacheLogMap.keySet();
        for (String sk : setKey){ //将map的key转成大写
            toOrderCenterTacheLogMapNew.put(sk.toUpperCase(), MapUtils.getString(toOrderCenterTacheLogMap, sk));
        }
        String toOrderCenterTacheLogStr= JSONObject.toJSONString(toOrderCenterTacheLogMapNew);
        Map<String, Object> resMap = new HashMap<String, Object>();
        boolean result = false;
        //String url = "http://10.245.6.226:8089/server/orderCenter/pushWorkWoInfo";
        String url = webServiceDao.queryUrl("tacheOrderLogPush");
        //创建httpclient工具对象
        HttpClient client = new HttpClient();
        //创建post请求方法
        PostMethod myPost = new PostMethod(url);
        try {
            myPost.setRequestHeader("Content-Type","application/json");
            myPost.setRequestEntity(new StringRequestEntity(toOrderCenterTacheLogStr,"application/json","utf-8"));
            int statusCode = client.executeMethod(myPost);
            if(statusCode == HttpStatus.SC_OK){
                result = true;
            }
            String resStr=myPost.getResponseBodyAsString();

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
    }*/


}
