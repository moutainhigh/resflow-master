package com.zres.project.localnet.portal.webservice.res;

import com.alibaba.fastjson.JSON;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.util.HttpClientJson;
import com.zres.project.localnet.portal.util.SpringContextHolderUtil;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.res.frame.core.util.MapUtil;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 资源接口-客户端
 * 资源查询接口实现
 * Created by Skyla on 2018/12/25.
 */
@Service
public class BusinessQueryService implements BusinessQueryServiceIntf {
    private static Logger logger = LoggerFactory.getLogger(BusinessQueryService.class);
    @Autowired
    private WebServiceDao webServiceDao;

    @Override
    public Map<String, Object> businessQuery(Map intMap) { // crm产品实例标识
        Map<String, Object> resMap = new HashMap<>();
        OrderDealDao orderDealDao = SpringContextHolderUtil.getBean("orderDealDao");
        WebServiceDao rsd = SpringContextHolderUtil.getBean("webServiceDao"); // 数据库操作-对象

        logger.info("资源查询接口开始！" + intMap);
        String circuitCode = MapUtils.getString(intMap,"circuitCode");
        String productType = MapUtils.getString(intMap,"productType");
        String custId = MapUtils.getString(intMap, "custId");
        String accNbr = MapUtils.getString(intMap,"accNbr");
        String json = "";
        String zyResponse = "";
        List<Map<String, Object>> allMap = new ArrayList<Map<String, Object>>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置日期格式
        String createdate = "";
        String updatedate = "";
        try {
            // 生成json报文
            JSONObject jsonObj = new JSONObject();
            JSONObject request = new JSONObject();
            JSONObject requestBody = new JSONObject();
            JSONObject requireData = new JSONObject();
            // productType= "20181211001";
            requireData.put("circuitCode", circuitCode);
            requireData.put("accNbr", MapUtils.getString(intMap, "accNbr"));
            requireData.put("productId", productType);
            requireData.put("custId", custId);
            /**
             0：是，（单纯的查电路和关联配置信息）
             1：否（作为变更/拆机查老实例使用）
             */
            requireData.put("isToBeQuery", MapUtils.getString(intMap,"isToBeQuery"));
            /**
             * 新增查询必填项， pageNum当前页；pageNum 每页数量
             * 如果传入参数没有值，那么就给默认值
             */
            requireData.put("pageNum", MapUtils.getString(intMap,"pageIndex","1"));
            requireData.put("pageCount", MapUtils.getString(intMap,"pageSize","10000"));
            requestBody.put("requireData", requireData);
            request.put("requestBody", requestBody);
            Map map = new HashMap();

            if(intMap.keySet().contains("HANDLE_DEP_ID")){
                map.put("HANDLE_DEP_ID", MapUtils.getString(intMap,"HANDLE_DEP_ID"));
            } else {
                String userId = ThreadLocalInfoHolder.getLoginUser().getUserId();
                Map<String, Object> staffMap = orderDealDao.getOperStaffInfo(Integer.valueOf(userId));
                String orgId = MapUtil.getString(staffMap, "ORG_ID");
                map.put("HANDLE_DEP_ID", orgId);
            }

            request.put("requestHeader", HttpClientJson.requestHeader(map, "ResBusinessQuery"));

            jsonObj.put("request", request);
            json = jsonObj.toString();
            // createdate = df.format(new Date());
            logger.info("发送报文：" + json);
        } catch (Exception e) {
            logger.error("拼接报文异常！异常信息：" + e.getMessage(), e);
            resMap.put("isExist", false);
            resMap.put("errMsg", "拼接报文异常！异常信息：" + e.getMessage());
            return resMap;
        }
        try {
            // 3.调对方接口，发json报文 ，接收返回json报文
            String url = rsd.queryUrl("BusinessQuery");
            logger.info("资源接口地址：" + url);
            Map respons = HttpClientJson.sendHttpPost(url, json);
            zyResponse = respons.get("msg").toString();
            updatedate = df.format(new Date());
            logger.info("资源返回报文：" + zyResponse);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("interfname", "资源查询接口");
            map.put("url", url);
            map.put("content", json);
            map.put("createdate", updatedate);
            map.put("returncontent", zyResponse);
            map.put("orderno", circuitCode == "" ? accNbr : circuitCode);
            map.put("remark", "接收资源返回报文");
            map.put("updatedate", updatedate);
            // 5.报文入库，数据入库
            rsd.saveJson(map);
        } catch (Exception e) {
            logger.error("调资源接口异常！异常信息：" + e.getMessage(), e);
            resMap.put("isExist", false);
            resMap.put("errMsg", "调用资源接口异常，异常信息：" + e.getMessage());
            return resMap;
        }
        try {
            // 4.解析json报文
            JSONObject retJson = JSONObject.fromObject(zyResponse);
            JSONObject responseBody = retJson.getJSONObject("response");
            JSONObject responseData = responseBody.getJSONObject("responseBody");
            JSONObject response = responseData.getJSONObject("responseData");
            String isExist = response.getString("isExist");
            String errMsg = response.getString("errorMsg");
            // JSONObject circuitList = responseBody.getJSONObject("circuitList");
            if (isExist == "1" || "1".equals(isExist)) {
                resMap.put("isExist", false);
                resMap.put("errMsg", errMsg);
                return resMap;
            } else if (isExist == "0" || "0".equals(isExist)) {
                String circuitList = response.getString("circuitList");
                if (circuitList == null) {
                    resMap.put("isExist", false);
                    resMap.put("errMsg", "未查到电路数据");
                } else {
                    JSONArray array = JSONArray.fromObject(circuitList);
                    JSONObject jsonRow;
                    if (array.size() == 0) {
                        resMap.put("isExist", false);
                        resMap.put("errMsg", "未查到电路数据");
                        return resMap;
                    }
                    for (int i = 0; i < array.size(); i++) {
                        // 遍历JSONArray中的每一个对象
                        jsonRow = array.getJSONObject(i);
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("circuitCode", jsonRow.getString("circuitCode"));
                        map.put("productId", jsonRow.getString("productId"));
                        map.put("prodInstId", jsonRow.getString("prodInstId"));
                        map.put("crmOrderCode", jsonRow.getString("crmOrderCode"));
                        map.put("circuitId", jsonRow.getString("circuitId"));
                        map.put("accNbr", jsonRow.getString("accNbr"));

                        map.put("resTypeId", jsonRow.getString("resTypeId"));
                        map.put("resType", jsonRow.getString("resType"));
                        map.put("sbOprState", jsonRow.getString("sbOprState"));
                        map.put("oprState", jsonRow.getString("oprState"));
                        map.put("oprStateId", jsonRow.getString("oprStateId"));
                        map.put("sbOprStateId", jsonRow.getString("sbOprStateId"));
                        map.put("regionCodeId", jsonRow.getString("regionCodeId"));
                        map.put("regionId", jsonRow.getString("regionId"));
                        map.put("businessId", jsonRow.getString("businessId"));
                        map.put("buizTypeId", jsonRow.getString("buizTypeId"));
                        map.put("productId", jsonRow.getString("productId"));
                        map.put("serviceId", jsonRow.getString("productNo"));
                        logger.info("circuitid" + jsonRow.getString("circuitId"));
                        allMap.add(map);
                    }
                    resMap.put("isExist", true);
                    resMap.put("data", allMap);
                    resMap.put("page", MapUtils.getString(intMap, "pageIndex", "1"));
                    //电路总数
                    resMap.put("circuitTotalCount", response.getString("circuitTotalCount"));
                }
            }
        } catch (Exception e) {
            logger.error("接口返回报文格式异常！异常信息：" + e.getMessage(), e);
            resMap.put("isExist", false);
            resMap.put("errMsg", "接口返回报文格式异常！异常信息：" + zyResponse);
            return resMap;
        }
        return resMap;
    }

    @Override
    public Map<String, Object> queryRouteInfo(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap<>();
        try{
            //调用资源接口
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>调用资源接口查询路由信息！");
            String url = webServiceDao.queryUrl("queryRouteInfo");
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>接口url：" + url);
            String request = JSON.toJSONString(param);
            Map respons = HttpClientJson.httpPostRequest(url, request);
            //插入接口调用日志
            try {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Map<String, Object> map = new HashMap<String, Object>();
                String updatedate = df.format(new Date());
                map.put("interfname", "调用存量接口查询路由信息");
                map.put("url", url);
                map.put("content", request);
                map.put("createdate", updatedate);
                map.put("returncontent", MapUtils.getString(respons, "msg",""));
                map.put("orderno", MapUtils.getString(param, "businessNum"));//电路编号
                map.put("remark", "接收资源返回报文");
                map.put("updatedate", updatedate);
                // 5.报文入库，数据入库
                webServiceDao.saveJson(map);
            }
            catch (Exception e) {
                logger.info(">>>>>>>>>>>>>>>>>>>>插入调用存量接口查询路由信息日志失败！" + e.getMessage());
            }
            if ("200".equals(MapUtils.getString(respons, "code"))){
                JSONObject json = JSONObject.fromObject(MapUtils.getString(respons, "msg"));
                if ("SUCCESS".equals(json.getString("mess"))){
                    JSONArray datas = json.getJSONArray("datas");
                    if (datas.size() > 0){
                        for (int i = 0; i < datas.size(); i++){
                            JSONObject jsonObject = datas.getJSONObject(i);
                            //获取路由信息
                            JSONArray souteArry = jsonObject.getJSONArray("ROUTE");
                            List<Map<String, Object>> souteList = new ArrayList<>();
                            if (souteArry.size() > 0){
                                for (int m = 0; m < souteArry.size(); m++){
                                    Map<String, Object> souteMap = new HashMap<>();
                                    JSONObject souteJson = souteArry.getJSONObject(m);
                                    souteMap.put("ROUTE_NO", souteJson.get("ROUTE_NO"));
                                    souteMap.put("ROUTE_NAME", souteJson.get("ROUTE_NAME"));
                                    souteMap.put("ROUTE_TYPE", souteJson.get("ROUTE_TYPE"));
                                    souteList.add(souteMap);
                                }
                                resMap.put("souteInfo", souteList);
                            }
                            JSONObject PROPERTY = jsonObject.getJSONObject("PROPERTY");
                            Map<String, Object> propertyMap = new HashMap<>();
                            propertyMap.put("CUST_NAME", PROPERTY.get("CUST_NAME"));
                            propertyMap.put("CUST_ID", PROPERTY.get("CUST_ID"));
                            propertyMap.put("LINK_TELE", PROPERTY.get("LINK_TELE"));
                            propertyMap.put("ADDRESS", PROPERTY.get("ADDRESS"));
                            propertyMap.put("LINK_MAN", PROPERTY.get("LINK_MAN"));
                            propertyMap.put("LINK_TELE", PROPERTY.get("LINK_TELE"));
                            propertyMap.put("PRODUCT_NO", PROPERTY.get("PRODUCT_NO"));
                            propertyMap.put("BUSINESS_IDENTITY", PROPERTY.get("BUSINESS_IDENTITY"));
                            propertyMap.put("CIRCUIT_NO", PROPERTY.get("CIRCUIT_NO"));
                            propertyMap.put("CIRCUIT_RATE", PROPERTY.get("CIRCUIT_RATE"));
                            propertyMap.put("A_RESISTANCE", PROPERTY.get("A_RESISTANCE"));
                            propertyMap.put("A_ADDRESS", PROPERTY.get("A_ADDRESS"));
                            propertyMap.put("LONG_INTER_PORT", PROPERTY.get("LONG_INTER_PORT"));
                            propertyMap.put("A_LINK_MAN", PROPERTY.get("A_LINK_MAN"));
                            propertyMap.put("A_LINK_TELE", PROPERTY.get("A_LINK_TELE"));
                            propertyMap.put("SYS_EMS_NAME", PROPERTY.get("SYS_EMS_NAME"));
                            propertyMap.put("Z_ADDRESS", PROPERTY.get("Z_ADDRESS"));
                            propertyMap.put("SYS_NETWORK_GW", PROPERTY.get("SYS_NETWORK_GW"));
                            propertyMap.put("Z_LINK_MAN", PROPERTY.get("Z_LINK_MAN"));
                            propertyMap.put("Z_LINK_TELE", PROPERTY.get("Z_LINK_TELE"));
                            resMap.put("propertyInfo",propertyMap);
                        }
                        resMap.put("success", true);
                    }else{
                        resMap.put("success", false);
                        resMap.put("msg", "没有查到对应的详细信息！");
                    }

                }
            }else{
                resMap.put("success", false);
                resMap.put("msg", MapUtils.getString(respons, "msg"));
            }
        }
        catch(Exception e){
            resMap.put("success", false);
            resMap.put("msg", "调用资源接口查询路由信息发生异常" + e.getMessage());
        }
        return resMap;
    }

    @Override
    public Map<String, Object> businessQueryCust(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap<>();
        OrderDealDao orderDealDao = SpringContextHolderUtil.getBean("orderDealDao");
        WebServiceDao rsd = SpringContextHolderUtil.getBean("webServiceDao"); // 数据库操作-对象

        logger.info("查询资源客户信息接口开始！" + param);
        String custName = MapUtils.getString(param, "custName");
        String custNo = MapUtils.getString(param, "custNo");
        String json = "";
        String zyResponse = "";
        List<Map<String, Object>> allMap = new ArrayList<Map<String, Object>>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置日期格式
        String updatedate = "";
        try {
            // 生成json报文
            JSONObject jsonObj = new JSONObject();
            JSONObject request = new JSONObject();
            JSONObject requireData = new JSONObject();
            requireData.put("custName", custName);
            requireData.put("custNo", custNo);
            /**
             * 新增查询必填项， pageNum当前页；pageNum 每页数量
             * 如果传入参数没有值，那么就给默认值
             */
            requireData.put("page", MapUtils.getString(param, "page", "1"));
            requireData.put("rowNum", MapUtils.getString(param, "rowNum", "10000"));
            request.put("requestBody", requireData);
            Map map = new HashMap();
            String userId = ThreadLocalInfoHolder.getLoginUser().getUserId();
            Map<String, Object> staffMap = orderDealDao.getOperStaffInfo(Integer.valueOf(userId));
            String orgId = MapUtil.getString(staffMap, "ORG_ID");
            map.put("HANDLE_DEP_ID", orgId);
            request.put("requestHeader", HttpClientJson.requestHeader(map, "RES_QueryCustInfo"));

            jsonObj.put("request", request);
            json = jsonObj.toString();
            // createdate = df.format(new Date());
            logger.info("发送报文：" + json);
        }
        catch (Exception e) {
            logger.error("拼接报文异常！异常信息：" + e.getMessage(), e);
            resMap.put("isExist", false);
            resMap.put("errMsg", "拼接报文异常！异常信息：" + e.getMessage());
            return resMap;
        }
        try {
            // 3.调对方接口，发json报文 ，接收返回json报文
            String url = rsd.queryUrl("TransferUser");
            logger.info("查询客户信息接口地址：" + url);
            Map respons = HttpClientJson.httpPostRequest(url, json);
            zyResponse = respons.get("msg").toString();
            updatedate = df.format(new Date());
            logger.info("查询客户信息返回报文：" + zyResponse);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("interfname", "查询资源配置客户信息接口");
            map.put("url", url);
            map.put("content", json);
            map.put("createdate", updatedate);
            map.put("returncontent", zyResponse);
            map.put("orderno", custName == "" ? custNo : custName);
            map.put("remark", "接收资源返回报文");
            map.put("updatedate", updatedate);
            // 5.报文入库，数据入库
            rsd.saveJson(map);
        }
        catch (Exception e) {
            logger.error("调资源配置客户信息接口异常！异常信息：" + e.getMessage(), e);
            resMap.put("isExist", false);
            resMap.put("errMsg", "调资源配置客户信息接口异常！异常信息：" + e.getMessage());
            return resMap;
        }
        try {
            JSONObject retJson = JSONObject.fromObject(zyResponse);
            JSONObject response= retJson.getJSONObject("response");
            JSONObject responseBody = response.getJSONObject("responseBody");
            String isExist = responseBody.getString("respCode");
            String errMsg = responseBody.getString("respDesc");
            if (isExist == "0" || "0".equals(isExist)) {
                resMap.put("isExist", false);
                resMap.put("errMsg", errMsg);
                return resMap;
            }  else {
                JSONObject custInfoList = responseBody.getJSONObject("custInfoList");

                JSONArray array = JSONArray.fromObject(custInfoList.getString("data"));
                JSONObject jsonRow;
                if (array.size() == 0) {
                    resMap.put("isExist", false);
                    resMap.put("errMsg", "未查到对应的客户信息，请重新查询");
                    return resMap;
                }

                for (int i = 0; i < array.size(); i++) {
                    //遍历JSONArray中的每一个对象
                    jsonRow = array.getJSONObject(i);
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("custId", jsonRow.getString("custId"));
                    map.put("custName", jsonRow.getString("custName"));
                    map.put("crmCustCode", jsonRow.getString("crmCustCode"));
                    allMap.add(map);
                }
                resMap.put("isExist", true);
                resMap.put("data", allMap);
                //  客户信息总数
                resMap.put("custTotalCount", custInfoList.getString("total"));
                resMap.put("page", custInfoList.getString("pageCount"));
            }
        }
        catch (Exception e) {
            logger.error("接口返回报文格式异常！异常信息：" + e.getMessage(), e);
            resMap.put("isExist", false);
            resMap.put("errMsg", "接口返回报文格式异常！异常信息：" + zyResponse);
            return resMap;
        }
        return resMap;
    }
}
