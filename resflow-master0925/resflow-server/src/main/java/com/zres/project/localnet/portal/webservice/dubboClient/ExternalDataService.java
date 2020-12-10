package com.zres.project.localnet.portal.webservice.dubboClient;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.util.HandyTool;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.zres.project.localnet.portal.webservice.until.Encapsulation;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import net.sf.json.JSONArray;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by jiangdebing on 2020/5/18.
 */
@RestController
@RequestMapping("/externalDataServiceIntf")
public class ExternalDataService implements ExternalDataServiceIntf{
    private static final Logger logger = LoggerFactory.getLogger(ExternalDataService.class);
    private static final String querySQL = "SELECT DISTINCT LISTAGG(GBSOI.SRV_ORD_ID, ',') WITHIN GROUP(ORDER BY GBCO.CST_ORD_ID) AS SRV_ORD_ID,GBCO.CST_ORD_ID FROM GOM_BDW_CST_ORD GBCO JOIN GOM_BDW_SRV_ORD_INFO GBSOI ON GBSOI.CST_ORD_ID=GBCO.CST_ORD_ID JOIN GOM_WO GW ON GW.ORDER_ID=GBSOI.ORDER_ID JOIN GOM_ORG_S GOS ON GOS.ORG_ID||''=GBCO.HANDLE_DEP_ID JOIN GOM_AREA_S GAS ON GAS.ID=GOS.AREA_ID WHERE 1=1";
    String timeFormat = "^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))(\\s(((0?[0-9])|([1][0-9])|([2][0-4]))\\:([0-5]?[0-9])((\\s)|(\\:([0-5]?[0-9])))))?$";//时间格式判断
    @Autowired
    private JdbcTemplate springJdbcTemplate;
    @Autowired
    private Encapsulation encapsulation;
    @Autowired
    private WebServiceDao wsd;
    @IgnoreSession
    @PostMapping(value="/interfaceBDW/externalData.spr", produces = "application/json;charset=UTF-8")
    @Override
    public String externalData(@RequestBody String queryJSON) {
        List<Object> returnList = new ArrayList<Object>();
        String returnValue = "";
        JSONObject queryObj = JSONObject.parseObject(queryJSON);
        StringBuffer queryexternalDataSql = new StringBuffer(querySQL);
        HandyTool.splicingSql(queryObj.get("same_day"),"AND TRUNC(GW.CREATE_DATE)=TRUNC(SYSDATE)", queryexternalDataSql);
        HandyTool.splicingSql(queryObj.get("srvOrdId"),"AND GBSOI.SRV_ORD_ID="+queryObj.get("srvOrdId"), queryexternalDataSql);
        HandyTool.splicingSql(queryObj.get("cstOrdId"),"AND GBCO.CST_ORD_ID="+queryObj.get("cstOrdId"), queryexternalDataSql);
        HandyTool.splicingSql(queryObj.get("provinceId"),"AND GAS.ID="+queryObj.get("provinceId"), queryexternalDataSql);
        if(HandyTool.regularExpression(String.valueOf(queryObj.get("woStartTime")),timeFormat)){
            HandyTool.splicingSql(queryObj.get("woStartTime"), "AND GW.CREATE_DATE>=TO_DATE('"+queryObj.get("woStartTime")+"','yyyy-mm-dd hh24:mi:ss')", queryexternalDataSql);
        }
        if(HandyTool.regularExpression(String.valueOf(queryObj.get("woEndTime")),timeFormat)){
            HandyTool.splicingSql(queryObj.get("woEndTime"), "AND GW.CREATE_DATE<=TO_DATE('"+queryObj.get("woEndTime")+"','yyyy-mm-dd hh24:mi:ss')", queryexternalDataSql);
        }
        if(HandyTool.regularExpression(String.valueOf(queryObj.get("cstStartTime")),timeFormat)){
            HandyTool.splicingSql(queryObj.get("cstStartTime"), "AND GBCO.CREATE_DATE>=TO_DATE('"+queryObj.get("cstStartTime")+"','yyyy-mm-dd hh24:mi:ss')", queryexternalDataSql);
        }
        if(HandyTool.regularExpression(String.valueOf(queryObj.get("cstEndTime")),timeFormat)){
            HandyTool.splicingSql(queryObj.get("cstEndTime"), "AND GBCO.CREATE_DATE<=TO_DATE('"+queryObj.get("cstEndTime")+"','yyyy-mm-dd hh24:mi:ss')", queryexternalDataSql);
        }
        List<Map<String,Object>> externalDataList = springJdbcTemplate.queryForList(queryexternalDataSql+" GROUP BY GBCO.CST_ORD_ID");
        if(HandyTool.judgeNULL(externalDataList)) {
            for (Map<String, Object> externalDataMap : externalDataList) {
                List dlList = HandyTool.convertList(externalDataMap.get("SRV_ORD_ID"));
                Map<String, Object> parameter = new HashMap<String, Object>();
                if(HandyTool.judgeNULL(dlList)) {
                    parameter.put("SRV_ORD_ID",dlList.get(0));
                }
                parameter.put("CST_ORD_ID",HandyTool.judgeNULL(externalDataMap.get("CST_ORD_ID"))?externalDataMap.get("CST_ORD_ID"):"");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
                Date date = new Date();
                UUID uuid = UUID.randomUUID();//生成UUID
                Map<String, Object> jsonMap = new HashMap<String, Object>();//全报文
                //HEAD节点
                Map<String, Object> headMap = new HashMap<String, Object>();
                headMap.put("APP_ID", "RESOURCEMANAGEMENT");
                headMap.put("TIMESTAMP", sdf.format(date));
                headMap.put("UUID", uuid.toString().replaceAll("-", ""));
                jsonMap.put("UNI_OSS_HEAD", headMap);//添加HEAD节点
                //BODY节点
                Map<String, Object> bodyMap = new HashMap<String, Object>();
                //定单信息
                Map<String, Object> orderMap = new HashMap<String, Object>();
                //申请信息
                Map<String, Object> applyMap = getData("APPLY_INF","shenqing",parameter);
                orderMap.put("APPLY_INF", applyMap);//添加申请信息
                //客户信息
                Map<String, Object> customerMap = getData("CUSTOMER_INF","kehu",parameter);
                orderMap.put("CUSTOMER_INF", customerMap);//添加客户信息
                //业务信息列表
                List<Map<String, Object>> businessList = new ArrayList<Map<String, Object>>();
                if(HandyTool.judgeNULL(dlList)) {
                    for (int dl = 0; dl < dlList.size(); dl++) {
                        parameter.put("SRV_ORD_ID",dlList.get(dl));
                        //业务信息
                        Map<String, Object> businessMap = new HashMap<String, Object>();
                        //产品属性
                        Map<String, Object> productMap = getData("PRODUCT_ATTRIBUTE","shuxing",parameter);
                        businessMap.put("PRODUCT_ATTRIBUTE", productMap);//添加产品属性
                        Map dispatchData = springJdbcTemplate.queryForMap("SELECT MAX(NVL(NVL(GBSOI.DISPATCH_ORDER_ID,GBDO.DISPATCH_ORDER_ID),0)) AS DISPATCH_ORDER_ID FROM GOM_BDW_SRV_ORD_INFO GBSOI LEFT JOIN GOM_BDW_DISPATCH_ORDER GBDO ON GBDO.CST_ORD_ID=GBSOI.CST_ORD_ID WHERE GBSOI.SRV_ORD_ID="+dlList.get(dl));
                        parameter.put("DISPATCH_ORDER_ID",dispatchData.get("DISPATCH_ORDER_ID"));
                        //调单信息
                        Map<String, Object> dispatchMap = getData("DISPATCH_INF","diaodan",parameter);
                        businessMap.put("DISPATCH_INF", dispatchMap);//添加调单信息
                        //环节信息
                        Map<String, Object> tacheMap = getData("TACHE_INF","huanjie",parameter);
                        //环节信息列表
                        List<Map<String, Object>> tacheList = new ArrayList<Map<String, Object>>();
                        List<Map<String, Object>> hjList = springJdbcTemplate.queryForList("SELECT GW.WO_ID FROM GOM_BDW_SRV_ORD_INFO GBSOI JOIN GOM_WO GW ON GW.ORDER_ID=GBSOI.ORDER_ID WHERE GW.DISP_OBJ_ID!=-2000 AND GBSOI.SRV_ORD_ID="+dlList.get(dl));
                        if(HandyTool.judgeNULL(hjList)) {
                            for (Map<String, Object> hjMap : hjList) {
                                parameter.put("WO_ID",hjMap.get("WO_ID"));
                                //环节详情
                                Map<String, Object> tacheInfoMap = getData("TACHE_LIST","huanjiexiangqing",parameter);
                                tacheList.add(tacheInfoMap);//添加环节详情
                            }
                        }
                        tacheMap.put("TACHE_LIST", tacheList);//添加环节信息列表
                        businessMap.put("TACHE_INF", tacheMap);//添加环节信息
                        businessList.add(businessMap);//添加业务信息
                    }
                }
                orderMap.put("BUSINESS_INF", businessList);//添加业务信息列表
                bodyMap.put("ORDER_INF", orderMap);//添加定单信息
                jsonMap.put("UNI_OSS_BODY", bodyMap);//添加BODY节点
                returnValue = JSON.toJSONString(jsonMap);
                returnList.add(returnValue);
            }
        }
        String returnJson = JSONArray.fromObject(returnList).toString();
        logger.info("externalData方法生成JSON报文:" + returnJson);
        return returnList.toString();
}
/**
 * 获取报文节点及数据
 * @author jdb
 * @date 2020/11/16 15:15
 * @param elementPath
 * @param specialType
 * @param parameter
 * @return java.util.Map<java.lang.String, java.lang.Object>
 */
private Map<String,Object> getData(String elementPath,String specialType,Map<String,Object> parameter){
    Map<String,Object> returnValue = new HashMap<String,Object>();
    List<Map<String,Object>> tableList = springJdbcTemplate.queryForList("SELECT DISTINCT TABLENAME,GINSENG,CONDITIONS FROM GOM_INNER_ORDER_ELEMENT_RELA WHERE ELEMENT_PATH='"+elementPath+"' AND SPECIAL_TYPE='"+specialType+"' AND CODE='ExternalData' AND TABLENAME IS NOT NULL");
    List<Map<String,Object>> callSqlList = new ArrayList<Map<String,Object>>();
    Map<String,Object> transcodingMap = new HashMap<String,Object>();
    for(Map<String,Object> tableMap:tableList){
        List<Map<String,Object>> splicingList = springJdbcTemplate.queryForList("SELECT 'NVL('||PRARMETER||','||NVL(DEFAULT_VALUE,'null')||') AS '||ELEMENT_CODE AS SPELLSQL,ELEMENT_CODE,DATA_TYPE FROM GOM_INNER_ORDER_ELEMENT_RELA WHERE ELEMENT_PATH='"+elementPath+"' AND SPECIAL_TYPE='"+specialType+"' AND CODE='ExternalData' AND TABLENAME='"+tableMap.get("TABLENAME")+"'");
        Map<String,Object> callMap = new HashMap<String,Object>();
        callMap.put("GINSENG",tableMap.get("GINSENG"));
        StringBuffer splicingSb = new StringBuffer("SELECT null");
        for(Map<String,Object> splicingMap : splicingList){
            splicingSb.append(","+splicingMap.get("SPELLSQL"));
            if(HandyTool.judgeNULL(MapUtils.getString(splicingMap,"DATA_TYPE"))){
                transcodingMap.put(MapUtils.getString(splicingMap,"ELEMENT_CODE"),splicingMap.get("DATA_TYPE"));
            }
        }
        splicingSb.append(" FROM "+tableMap.get("TABLENAME")+" WHERE "+tableMap.get("CONDITIONS"));
        callMap.put("CALLSQL",splicingSb);
        callSqlList.add(callMap);

    }
    String srvAttrCode = "["+wsd.querySrvAttrCode(MapUtils.getString(parameter,"SRV_ORD_ID"))+"]";
    for(Map<String,Object> callSqlMap:callSqlList){
        String callSql = HandyTool.judgeNULL(callSqlMap.get("CALLSQL"))?callSqlMap.get("CALLSQL").toString():null;
        logger.info("父节点为"+elementPath+"的执行SQL是：" + callSql);
        if(HandyTool.judgeNULL(callSql)&&callSql.indexOf("?")>-1&&HandyTool.judgeNULL(callSqlMap.get("GINSENG"))){
            List<Map<String,Object>> callDataList = springJdbcTemplate.queryForList(callSqlMap.get("CALLSQL").toString().replaceAll("\\?",parameter.get(callSqlMap.get("GINSENG").toString()).toString()));
            if(HandyTool.judgeNULL(callDataList)&&HandyTool.judgeNULL(callDataList.get(0))){
                Map<String,Object> callDataMap = callDataList.get(0);
                Iterator iter=callDataMap.entrySet().iterator();
                while(iter.hasNext()){
                    Map.Entry<String,String> entry = (Map.Entry<String,String>) iter.next();
                    if(HandyTool.judgeNULL(MapUtils.getString(transcodingMap,entry.getKey()))){
                        String[] transcodings = MapUtils.getString(transcodingMap,entry.getKey()).split(",");
                        for(int t = 0; t < transcodings.length; t++){
                            if(HandyTool.judgeNULL(encapsulation.getTranscodingContent(transcodings[t],entry.getValue()))&&srvAttrCode.indexOf(transcodings[t])>0){
                                callDataMap.put(entry.getKey(),encapsulation.getTranscodingContent(transcodings[t],entry.getValue()));
                                break;
                            }
                        }
                    }
                }
                returnValue.putAll(callDataMap);
            }
        }
        else if (HandyTool.judgeNULL(callSql)){
            List<Map<String,Object>> callDataList = springJdbcTemplate.queryForList(callSqlMap.get("CALLSQL").toString());
            if(HandyTool.judgeNULL(callDataList)&&HandyTool.judgeNULL(callDataList.get(0))){
                returnValue.putAll(callDataList.get(0));
            }
        }
    }
    List<Map<String,Object>> serviceSqlList = springJdbcTemplate.queryForList("SELECT SERVICE_SQL,GINSENG,ELEMENT_CODE,DEFAULT_VALUE FROM GOM_INNER_ORDER_ELEMENT_RELA WHERE ELEMENT_PATH='"+elementPath+"' AND SPECIAL_TYPE='"+specialType+"' AND CODE='ExternalData' AND SERVICE_SQL IS NOT NULL");
    for(Map<String,Object> serviceSqlMap:serviceSqlList){
        if(!HandyTool.judgeNULL(returnValue.get(serviceSqlMap.get("ELEMENT_CODE").toString()))) {
            List<Map<String, Object>> serviceDataList = springJdbcTemplate.queryForList(serviceSqlMap.get("SERVICE_SQL").toString().replaceAll("\\?", parameter.get(serviceSqlMap.get("GINSENG").toString()).toString()));
            if (HandyTool.judgeNULL(serviceDataList)&&HandyTool.judgeNULL(serviceDataList.get(0))) {
                returnValue.putAll(serviceDataList.get(0));
            }
            else if(HandyTool.judgeNULL(serviceSqlMap.get("DEFAULT_VALUE"))){
                returnValue.put(serviceSqlMap.get("ELEMENT_CODE").toString(),serviceSqlMap.get("DEFAULT_VALUE"));
            }
        }
    }
    return returnValue;
}
}
