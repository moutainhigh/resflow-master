package com.zres.project.localnet.portal.cloudNetWork.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.cloudNetWork.dao.ReceiveOrderDao;
import com.zres.project.localnet.portal.cloudNetWork.task.StartFlowTask;
import com.zres.project.localnet.portal.cloudNetWork.util.ThreadUtil;
import com.zres.project.localnet.portal.cloudNetworkFlow.WoOrderDealServiceIntf;
import com.zres.project.localnet.portal.webservice.data.dao.InterfaceBoDao;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.zres.project.localnet.portal.webservice.oneDry.HandleAttachment;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 收单接口入库处理逻辑
 *
 * @author caomm on 2020/10/12
 */
@Service
public class DealOrderInfoService {
    Logger logger = LoggerFactory.getLogger(DealOrderInfoService.class);
    @Autowired
    private CloudNetCommonService commonService;
    @Autowired
    private ReceiveOrderDao receiveOrderDao;
    @Autowired
    private WoOrderDealServiceIntf woOrderDealServiceIntf;
    @Autowired
    private WebServiceDao webServiceDao;
    @Autowired
    private InterfaceBoDao interfaceJikeDao;

    public Map<String, Object> insertOrderInfo(String request) throws Exception{
        Map<String, Object> resMap = new HashMap<>();
        try{
            //解析报文信息
            JSONObject json = JSON.parseObject(request);
            JSONObject UNI_BSS_BODY = json.getJSONObject("UNI_BSS_BODY");
            JSONObject YZWAPPLY_ORDER_REQ = UNI_BSS_BODY.getJSONObject("YZWAPPLY_ORDER_REQ");
            JSONObject ROUTING = YZWAPPLY_ORDER_REQ.getJSONObject("ROUTING");
            JSONObject CST_ORD = YZWAPPLY_ORDER_REQ.getJSONObject("CST_ORD");
            JSONObject CST_ORD_INFO = CST_ORD.getJSONObject("CST_ORD_INFO");
            JSONObject SRV_ORD_LIST = CST_ORD_INFO.getJSONObject("SRV_ORD_LIST");
            JSONArray SRV_ORD = SRV_ORD_LIST.getJSONArray("SRV_ORD");
            String applyOrdId = CST_ORD.getString("SUBSCRIBE_ID");
            //入库客户信息
            int cstOrdId = insertCstOrdInfo(CST_ORD, ROUTING);
            //电路信息入库,包括电路属性信息、附加产品信息、附件等信息的处理
            insertSrvOrdInfo(SRV_ORD, cstOrdId, applyOrdId);
            resMap.put("success", true);
            resMap.put("msg", "收单处理成功！");
        }catch(Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>报文信息入库处理失败:{}", e.getMessage());
            resMap.put("success", false);
            resMap.put("msg", "收单处理失败：" + e.getMessage());
        }
        return resMap;
    }

    /**
     * 客户信息入库
     * @param cstOrd
     * @param routInfo
     * @return
     * @throws Exception
     */
    public int insertCstOrdInfo(JSONObject cstOrd, JSONObject routInfo) throws Exception{
        Map<String, Object> cstMap = new HashMap<>();
        String provinceA = "";
        String countyA = "";
        String countyZ = "";
        String cityA = "";
        try{
            JSONObject CST_ORD_INFO = cstOrd.getJSONObject("CST_ORD_INFO");
            JSONObject SRV_ORD_LIST = CST_ORD_INFO.getJSONObject("SRV_ORD_LIST");
            JSONArray SRV_ORD = SRV_ORD_LIST.getJSONArray("SRV_ORD");
            JSONObject jsonObject = SRV_ORD.getJSONObject(0);
            String serviceId = jsonObject.getString("SERVICE_ID");
            JSONObject SRV_ORD_INFO =  jsonObject.getJSONObject("SRV_ORD_INFO");
            JSONArray SRV_ATTR_INFO = SRV_ORD_INFO.getJSONArray("SRV_ATTR_INFO");
            if (SRV_ATTR_INFO != null && SRV_ATTR_INFO.size() > 0){
                for (int i = 0; i < SRV_ATTR_INFO.size(); i++){
                    JSONObject json = SRV_ATTR_INFO.getJSONObject(i);
                    if ("CON0101".equals(json.getString("ATTR_CODE"))){
                        provinceA = json.getString("ATTR_VALUE");
                    } else if("CON0105".equals(json.getString("ATTR_CODE"))){
                        countyA = json.getString("ATTR_VALUE");
                    }else if("CON0106".equals(json.getString("ATTR_CODE"))){
                        countyZ = json.getString("ATTR_VALUE");
                    }
                    if("80000466".equals(serviceId)){
                        if ("CON0005".equals(json.getString("ATTR_CODE"))) {
                            cityA = json.getString("ATTR_VALUE");
                        }
                    }else{
                        if ("CON0103".equals(json.getString("ATTR_CODE"))) {
                            cityA = json.getString("ATTR_VALUE");
                        }
                    }
                }
            }
            cstMap.put("routeType", routInfo.getString("ROUTE_TYPE"));
            cstMap.put("routeValue", routInfo.getString("ROUTE_VALUE"));
            cstMap.put("subscribeId", cstOrd.getString("SUBSCRIBE_ID"));
            cstMap.put("subscribeIdRela", cstOrd.getString("SUBSCRIBE_ID_RELA"));
            cstMap.put("projectType", CST_ORD_INFO.getString("PROJECT_TYPE"));
            cstMap.put("dealAreaCode", CST_ORD_INFO.getString("DEAL_AREA_CODE"));
            cstMap.put("netWorkLevel", CST_ORD_INFO.getString("NETWORK_LEVEL"));
            cstMap.put("developerDepartName", CST_ORD_INFO.getString("DEVELOPER_DEPART_NAME"));
            cstMap.put("groupPmId", CST_ORD_INFO.getString("GROUP_PM_ID"));
            cstMap.put("groupPmName", CST_ORD_INFO.getString("GROUP_PM_NAME"));
            cstMap.put("groupPmTel", CST_ORD_INFO.getString("GROUP_PM_TEL"));
            cstMap.put("groupPmEmail", CST_ORD_INFO.getString("groupPmEmail"));
            cstMap.put("provincePmId", CST_ORD_INFO.getString("PROVINCE_PM_ID"));
            cstMap.put("provincePmName", CST_ORD_INFO.getString("PROVINCE_PM_NAME"));
            cstMap.put("provincePmTel", CST_ORD_INFO.getString("PROVINCE_PM_TEL"));
            cstMap.put("provincePmEmail", CST_ORD_INFO.getString("PROVINCE_PM_EMAIL"));
            cstMap.put("initAmId", CST_ORD_INFO.getString("INIT_AM_ID"));
            cstMap.put("initAmName", CST_ORD_INFO.getString("INIT_AM_NAME"));
            cstMap.put("initAmDepartName", CST_ORD_INFO.getString("INIT_AM_DEPART_NAME"));
            cstMap.put("initAmTel", CST_ORD_INFO.getString("INIT_AM_TEL"));
            cstMap.put("initAmEmail", CST_ORD_INFO.getString("INIT_AM_EMAIL"));
            cstMap.put("acceptDate", CST_ORD_INFO.getString("ACCEPT_DATE"));
            cstMap.put("hanleCity", webServiceDao.qryCityName(CST_ORD_INFO.getString("TRADE_EPARCHY_CODE")));
            int order_rule_province= webServiceDao.selectValeByProvince(provinceA); //配置省份
            int order_rule_product  = webServiceDao.selectValeByServiceId(serviceId); // 配置单端产品
            String handleDepId = "";
            if("83".equals(provinceA)){
                handleDepId = webServiceDao.selectDeptId(CST_ORD_INFO.getString("TRADE_EPARCHY_CODE"));
            } else if (order_rule_province>0){
                if(order_rule_product>0&&!"".equals(countyA)){
                    handleDepId = webServiceDao.selectDeptId(countyA);
                }else if(!"".equals(countyA)&&(countyA.equals(countyZ))){
                    handleDepId = webServiceDao.selectDeptId(countyA);
                } else{
                    handleDepId = webServiceDao.selectDeptId(cityA);
                }

            }else{
                handleDepId = webServiceDao.selectDeptId(cityA);
            }
            if (StringUtils.isEmpty(handleDepId)){
                cstMap.put("handleDepId", handleDepId);
            }
            cstMap.put("hanleCityId", CST_ORD_INFO.getString("TRADE_EPARCHY_CODE"));
            cstMap.put("departName", CST_ORD_INFO.getString("DEPART_NAME"));
            cstMap.put("tradeStaffName", CST_ORD_INFO.getString("TRADE_STAFF_NAME"));
            cstMap.put("tradeStaffPhone", CST_ORD_INFO.getString("TRADE_STAFF_PHONE"));
            cstMap.put("remark", CST_ORD_INFO.getString("REMARK"));
            cstMap.put("custId", CST_ORD_INFO.getString("CUST_ID"));
            cstMap.put("contractId", CST_ORD_INFO.getString("CONTRACT_ID"));
            cstMap.put("contractName", CST_ORD_INFO.getString("CONTRACT_NAME"));
            cstMap.put("custNameChinese", CST_ORD_INFO.getString("CUST_NAME_CHINESE"));
            cstMap.put("custNameEnglish", CST_ORD_INFO.getString("CUST_NAME_ENGLISH"));
            cstMap.put("certiTypeCode", CST_ORD_INFO.getString("CERTI_TYPE_CODE"));
            cstMap.put("certiCode", CST_ORD_INFO.getString("CERTI_CODE"));
            cstMap.put("custAddress", CST_ORD_INFO.getString("CUST_ADDRESS"));
            cstMap.put("custIndustry", commonService.enumTrans(CST_ORD_INFO.getString("CUST_INDUSTRY"), "CUST_INDUSTRY"));
            cstMap.put("custType", commonService.enumTrans(CST_ORD_INFO.getString("CUST_TYPE"), "CUST_TYPE"));
            cstMap.put("custProvince", CST_ORD_INFO.getString("CUST_PROVINCE"));
            cstMap.put("custCity", CST_ORD_INFO.getString("CUST_CITY"));
            cstMap.put("custTel", CST_ORD_INFO.getString("CUST_TEL"));
            cstMap.put("custFax", CST_ORD_INFO.getString("CUST_FAX"));
            cstMap.put("custEmail", CST_ORD_INFO.getString("CUST_EMAIL"));
            cstMap.put("custContactManId", CST_ORD_INFO.getString("CUST_CONTACT_MAN_ID"));
            cstMap.put("custContactManName", CST_ORD_INFO.getString("CUST_CONTACT_MAN_NAME"));
            cstMap.put("custContactManTel", CST_ORD_INFO.getString("CUST_CONTACT_MAN_TEL"));
            cstMap.put("custContactManEmail", CST_ORD_INFO.getString("CUST_CONTACT_MAN_EMAIL"));
            cstMap.put("custOperatorName", CST_ORD_INFO.getString("CUST_OPERATOR_NAME"));
            cstMap.put("custOperatorTel", CST_ORD_INFO.getString("CUST_OPERATOR_TEL"));
            cstMap.put("custOperatorEmail", CST_ORD_INFO.getString("CUST_OPERATOR_EMAIL"));
            receiveOrderDao.insertIntoCstOrdInfo(cstMap);
        }catch(Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>云组网客户信息入库发生异常:{}", e.getMessage());
            throw new Exception("云组网客户信息入库发生异常:" + e.getMessage());
        }
        return MapUtils.getIntValue(cstMap, "id");
    }

    /**
     * 电路信息入库
     * @param cstOrdId
     * @param applyOrdId
     * @return
     * @throws Exception
     */
    public void insertSrvOrdInfo(JSONArray srvOrd, int cstOrdId, String applyOrdId) throws Exception{
        if (srvOrd != null && srvOrd.size() > 0){
            //遍历JSON数组
            for (int i = 0; i < srvOrd.size(); i++ ){
                Map<String, Object> keyInfo = new HashMap<>();
                Map<String, Object> srvMap = new HashMap<>();
                JSONObject json = srvOrd.getJSONObject(i);
                srvMap.put("cstOrdId", cstOrdId);
                srvMap.put("serviceId", json.getString("SERVICE_ID"));
                srvMap.put("tradeTypeCode", json.getString("TRADE_TYPE_CODE"));
                srvMap.put("reActiveType", json.getString("ACTIVE_TYPE"));
                srvMap.put("serviceOfferId", json.getString("SERVICE_OFFER_ID"));
                srvMap.put("serialNumber", json.getString("SERIAL_NUMBER"));
                srvMap.put("instanceId", json.getString("SERIAL_NUMBER"));
                srvMap.put("srvCode", json.getString("SVR_CODE"));
                srvMap.put("tradeId", json.getString("TRADE_ID"));
                srvMap.put("tradeIdRela", json.getString("TRADE_ID_RELA"));
                srvMap.put("userId", json.getString("USER_ID"));
                srvMap.put("flowId", json.getString("FLOW_ID"));
                srvMap.put("changeFlag", json.getString("CHANGE_FLAG"));
                keyInfo.put("changeFlag", json.getString("CHANGE_FLAG"));
                if("2009".equals(json.getString("TRADE_TYPE_CODE"))){
                    //核查
                    srvMap.put("orderType", "102");
                }else{
                    //新开
                    srvMap.put("orderType", "101");
                }
                //获取全程要求完成时间
                JSONObject SRV_ORD_INFO = json.getJSONObject("SRV_ORD_INFO");
                JSONArray SRV_ATTR_INFO = SRV_ORD_INFO.getJSONArray("SRV_ATTR_INFO");
                if (SRV_ATTR_INFO != null){
                    for (int m = 0; m < SRV_ATTR_INFO.size(); m++){
                        JSONObject attrJson = SRV_ATTR_INFO.getJSONObject(m);
                        if ("CON0014".equals(attrJson.getString("ATTR_CODE"))){
                            srvMap.put("reqFinTime", attrJson.getString("ATTR_VALUE"));
                        }else if ("CON0005".equals(attrJson.getString("ATTR_CODE"))){
                            keyInfo.put("cityA", attrJson.getString("ATTR_VALUE"));
                        }else if ("CON0006".equals(attrJson.getString("ATTR_CODE"))){
                            keyInfo.put("cityZ", attrJson.getString("ATTR_VALUE"));
                        }
                    }
                }
                srvMap.put("activeType", commonService.enumTrans(json.getString("TRADE_TYPE_CODE"), "ACTIVE_TYPE"));
                //电路信息入库
                receiveOrderDao.insertSrvOrdInfo(srvMap);
                //入库电路属性信息
                logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>开始处理云组网电路属性信息！");
                inserSrvAttrInfo(SRV_ATTR_INFO, MapUtils.getIntValue(srvMap, "id"), json.getString("SERVICE_ID"));
                //入库属性组信息
                JSONObject SRV_ATTR_GRP_LIST = SRV_ORD_INFO.getJSONObject("SRV_ATTR_GRP_LIST");
                if (SRV_ATTR_GRP_LIST != null){
                    logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>开始处理云组网属性组信息！");
                    insertAttrGrpInfo(SRV_ATTR_GRP_LIST, MapUtils.getIntValue(srvMap, "id"));
                }
                //入库附加产品信息
                JSONObject ADD_PROD_LIST = json.getJSONObject("ADD_PROD_LIST");
                if (ADD_PROD_LIST != null){
                    logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>开始处理云组网附加产品信息！");
                    insertAddProdInfo(ADD_PROD_LIST, MapUtils.getIntValue(srvMap, "id"));
                }
                //处理附件信息
                JSONObject ATTACH_INFO_LIST = json.getJSONObject("ATTACH_INFO_LIST");
                if (ATTACH_INFO_LIST != null){
                    logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>开始处理云组网附件信息！");
                    insertAttachInfo(ATTACH_INFO_LIST, MapUtils.getIntValue(srvMap, "id"));
                }
                //启异步线程进行起流程处理
                keyInfo.put("serviceId", json.getString("SERVICE_ID"));
                keyInfo.put("activeType", json.getString("ACTIVE_TYPE"));
                keyInfo.put("tradeTypeCode", json.getString("TRADE_TYPE_CODE"));
                keyInfo.put("serviceOfferId", json.getString("SERVICE_OFFER_ID"));
                keyInfo.put("applyOrdId", applyOrdId);
                StartFlowTask task = new StartFlowTask();
                task.setParam(keyInfo, receiveOrderDao, MapUtils.getIntValue(srvMap, "id"), woOrderDealServiceIntf);
                ThreadUtil.getsThreadInstance().submit(task);
            }
        }
    }

    /**
     * 电路属性信息批量入库
     * @param srvAttrInfo
     * @param srvOrdId
     * @throws Exception
     */
    public void inserSrvAttrInfo(JSONArray srvAttrInfo, int srvOrdId, String serviceId) throws Exception{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try{
            if (srvAttrInfo != null && srvAttrInfo.size() > 0){
                List<Map<String, Object>> attrList = new ArrayList<>();
                for (int i = 0; i < srvAttrInfo.size(); i++){
                    JSONObject json = srvAttrInfo.getJSONObject(i);
                    logger.info(">>>>>>>>>>>>>>>>attrCode:{}", json.getString("ATTR_CODE") );
                    Map<String, Object> attrMap = new HashMap<>();
                    attrMap.put("srvOrdId", srvOrdId);
                    attrMap.put("attrCode", json.getString("ATTR_CODE"));
                    attrMap.put("attrAction", json.getString("ATTR_ACTION"));
                    attrMap.put("attrName", json.getString("ATTR_NAME"));
                    attrMap.put("attrValue", json.getString("ATTR_VALUE"));
                    attrMap.put("attrValueName", json.getString("ATTR_VALUE_NAME"));
                    attrMap.put("oldAttrValue", json.getString("OLD_ATTR_VALUE"));
                    //时间节点，对时间节点进行格式转换
                    String attrCode = json.getString("ATTR_CODE");
                    if ("CON0013".equals(attrCode) || "CON0014".equals(attrCode)){
                        if (!StringUtils.isEmpty(json.getString("ATTR_VALUE"))){
                            Date date = sdf.parse(json.getString("ATTR_VALUE"));
                            String attrValue = ft.format(date);
                            attrMap.put("attrValue", attrValue);
                        }
                        if (!StringUtils.isEmpty(json.getString("OLD_ATTR_VALUE"))){
                            Date date = sdf.parse(json.getString("OLD_ATTR_VALUE"));
                            String attrValue = ft.format(date);
                            attrMap.put("oldAttrValue", attrValue);
                        }
                        attrList.add(attrMap);
                    }else {
                        List<Map<String,Object>> attrInfoList = interfaceJikeDao.qryAttrInfo(attrCode, json.getString("ATTR_VALUE"), json.getString("OLD_ATTR_VALUE"), serviceId);
                        if(!CollectionUtils.isEmpty(attrInfoList)){
                            for(int m = 0; m < attrInfoList.size(); m++){
                                Map<String,Object> tmpMap = attrInfoList.get(m);
                                Map<String, Object> attrTmp = new HashMap<>();
                                attrTmp.put("srvOrdId",srvOrdId);
                                attrTmp.put("attrAction", json.getString("ATTR_ACTION"));
                                attrTmp.put("attrValueName", json.getString("ATTR_VALUE_NAME"));
                                attrTmp.put("attrName", json.getString("ATTR_NAME"));
                                attrTmp.put("attrCode", MapUtils.getString(tmpMap,"ATTR_CODE",""));
                                attrTmp.put("attrValue", MapUtils.getString(tmpMap,"ATTR_VALUE",""));
                                attrTmp.put("oldAttrValue", MapUtils.getString(tmpMap,"OLD_ATTR_VALUE",""));
                                attrList.add(attrTmp);
                            }
                        }else{
                            attrList.add(attrMap);
                        }
                    }
                }
                //批量进行电路属性信息入库
                receiveOrderDao.insertSrvAttrInfo(attrList);
            }
        }catch(Exception e){
            e.printStackTrace();
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>云组网电路属性信息批量入库失败：{}", e.getMessage());
            throw new Exception("云组网电路属性信息批量入库失败:" + e.getMessage());
        }
    }

    /**
     * 入库属性组信息
     * @param grpInfo
     * @param srvOrdId
     * @throws Exception
     */
    public void insertAttrGrpInfo(JSONObject grpInfo, int srvOrdId) throws Exception{
        try{
            JSONArray SRV_ATTR_GRP = grpInfo.getJSONArray("SRV_ATTR_GRP");
            if (SRV_ATTR_GRP != null && SRV_ATTR_GRP.size() > 0){
                for (int n = 0; n < SRV_ATTR_GRP.size(); n++){
                    Map<String, Object> attrGrpMap = new HashMap<>();
                    JSONObject json = SRV_ATTR_GRP.getJSONObject(n);
                    attrGrpMap.put("attrGrpId", json.getString("ATTR_GRP_ID"));
                    attrGrpMap.put("attrGrpCode", json.getString("ATTR_GRP_CODE"));
                    attrGrpMap.put("attrGrpName", json.getString("ATTR_GRP_NAME"));
                    attrGrpMap.put("srvOrdId", srvOrdId);
                    //入库属性组ID等关键信息
                    receiveOrderDao.insertAttrGrpInfo(attrGrpMap);
                    JSONObject ATTR_INFO_LIST = json.getJSONObject("ATTR_INFO_LIST");
                    JSONArray SRV_ATTR_INFO = ATTR_INFO_LIST.getJSONArray("SRV_ATTR_INFO");
                    if (SRV_ATTR_INFO != null && SRV_ATTR_INFO.size() > 0){
                        //入库属性组的属性信息
                        insertAttrInfo(SRV_ATTR_INFO, MapUtils.getString(attrGrpMap, "id"));
                    }
                }
            }
        }catch(Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>云组网属性组信息批量入库失败:{}", e.getMessage());
            throw new Exception("云组网属性组信息批量入库失败:" + e.getMessage());
        }
    }

    /**
     * 属性组属性信息批量入库
     * @param SRV_ATTR_INFO
     * @param attrGrpId
     * @throws Exception
     */
    public void insertAttrInfo(JSONArray SRV_ATTR_INFO, String attrGrpId) throws Exception{
        try{
            if (SRV_ATTR_INFO != null && SRV_ATTR_INFO.size() > 0){
                List<Map<String, Object>> attrList = new ArrayList<>();
                for (int i = 0; i < SRV_ATTR_INFO.size(); i++){
                    Map<String, Object> attrMap = new HashMap<>();
                    JSONObject json = SRV_ATTR_INFO.getJSONObject(i);
                    attrMap.put("attrAction", json.getString("ATTR_ACTION"));
                    attrMap.put("attrCode", json.getString("ATTR_CODE"));
                    attrMap.put("attrName", json.getString("ATTR_NAME"));
                    attrMap.put("attrValue", json.getString("ATTR_VALUE"));
                    attrMap.put("attrValueName", json.getString("ATTR_VALUE_NAME"));
                    attrMap.put("attrGrpId", attrGrpId);
                    attrList.add(attrMap);
                }
                //批量入库
                receiveOrderDao.batchAttrGrpInfo(attrList);
            }
        }catch (Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>云组网的属性组的属性信息批量入库失败:{}", e.getMessage());
            throw new Exception("云组网的属性组的属性信息批量入库失败:" + e.getMessage());
        }
    }

    /**
     * 附加产品信息入库
     * @param addProdInfo
     * @param srvOrdId
     */
    public void insertAddProdInfo(JSONObject addProdInfo, int srvOrdId) throws Exception{
        try{
            JSONArray addProd = addProdInfo.getJSONArray("ADD_PROD_INFO");
            if (addProd != null && addProd.size() > 0){
                List<Map<String, Object>> prodList = new ArrayList<>();
                for (int i = 0; i < addProd.size(); i++){
                    JSONObject json = addProd.getJSONObject(i);
                    Map<String, Object> prodMap = new HashMap<>();
                    prodMap.put("srvOrdId", srvOrdId);
                    prodMap.put("productId", json.getString("B_PRODUCT_ID"));
                    prodMap.put("startDate", json.getString("START_DATE"));
                    prodMap.put("endDate", json.getString("END_DATE"));
                    receiveOrderDao.insertAddProdInfo(prodMap);
                    //附加产品信息入库完成后，继续入库附加产品的属性信息
                    JSONArray ADD_ATTR_INFO = json.getJSONArray("ADD_ATTR_INFO");
                    if (ADD_ATTR_INFO != null && ADD_ATTR_INFO.size() > 0){
                        insertProdAttrInfo(ADD_ATTR_INFO, MapUtils.getIntValue(prodMap, "id"));
                    }
                }
            }
        }catch(Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>云组网附加产品信息入库失败:{}", e.getMessage());
            throw new Exception("云组网附加产品信息入库失败:" + e.getMessage());
        }
    }

    /**
     * 附加从产品属性信息入库
     * @param addAttrInfo
     * @param addProdId
     * @throws Exception
     */
    public void insertProdAttrInfo(JSONArray addAttrInfo, int addProdId) throws Exception{
        try{
            List<Map<String, Object>> attrList = new ArrayList<>();
            for (int i = 0; i < addAttrInfo.size(); i++){
                JSONObject json = addAttrInfo.getJSONObject(i);
                Map<String, Object> attrMap = new HashMap<>();
                attrMap.put("addProdInfoId", addProdId);
                attrMap.put("attrAction", json.getString("ATTR_ACTION"));
                attrMap.put("attrCode", json.getString("ATTR_CODE"));
                attrMap.put("attrValue", json.getString("ATTR_VALUE"));
                attrMap.put("attrName", json.getString("ATTR_Name"));
                attrMap.put("attrValueName", json.getString("ATTR_VALUE_NAME"));
                attrList.add(attrMap);
            }
            //批量入库福建产品属性信息
            receiveOrderDao.insertAddProdAttrInfo(attrList);
        }catch (Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>云组网附加产品属性信息入库失败：{}", e.getMessage());
            throw new Exception("云组网附加产品属性信息入库失败:" +e.getMessage());
        }
    }

    /**
     * 附件信息入库
     * @param attachInfo
     * @param srvOrdId
     * @throws Exception
     */
    public void insertAttachInfo(JSONObject attachInfo, int srvOrdId) throws Exception{
        JSONArray ATTACH_INFO = attachInfo.getJSONArray("ATTACH_INFO");
        if (ATTACH_INFO != null && ATTACH_INFO.size() > 0){
            List<Map<String, Object>> attachList = new ArrayList<>();
            for (int i = 0; i < ATTACH_INFO.size(); i++){
                JSONObject json = ATTACH_INFO.getJSONObject(i);
                Map<String, Object> attachMap = new HashMap<>();
                String fileId = json.getString("FILE_ID");
                attachMap.put("srv_ord_id",srvOrdId);
                attachMap.put("path", json.getString("FILE_PATH"));
                attachMap.put("name", fileId);
                attachMap.put("value", json.getString("FILE_NAME"));
                attachMap.put("type", fileId.substring(fileId.lastIndexOf('.')+1, fileId.length()));
                attachList.add(attachMap);
            }
            //创建异步任务，下载附件并入库相关信息
            HandleAttachment ha = new HandleAttachment(attachList,"JIKE_FTP_INFO","FTP_INFO", "JIKE_4A");
            ha.start();
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>云组网附件信息异步处理任务提交完成！");
        }
    }
}