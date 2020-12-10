package com.zres.project.localnet.portal.webservice.construct;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.CheckFeedbackDao;
import com.zres.project.localnet.portal.util.XmlUtil;
import com.zres.project.localnet.portal.webservice.data.dao.InterfaceBoDao;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName ResPreAssessment
 * @Description TODO 核查前期评估
 * @Author wang.g2
 * @Date 2020/4/22 18:39
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@Service
public class ResPreAssessment implements ResPreAssessmentIntf {

    private static Logger logger = LoggerFactory.getLogger(ResPreAssessment.class);
    @Autowired
    private XmlUtil xmlUtil;
    @Autowired
    private InterfaceBoDao interfaceBoDao;
    @Autowired
    private CheckFeedbackDao checkFeedbackDao;
    @Autowired
    private WebServiceDao wsd;
    @Override
    public Map preAssessment(@RequestParam Map<String,Object> map){
        //拼接请求报文
        String jsonReqStr = "";
        logger.info("核查前期评估接口发送报文request：" + jsonReqStr);
        Map<String, Object> constructResponse = new HashMap<>();
        Map<String, Object> retMap = new HashMap<>();
        String sendJson = "";
        String requestJson = "";
        String tradeId = "";
        String response = "";
        List srvOrdsList = new ArrayList();
        try{
//            String srvOrdId = MapUtils.getString(map, "srvOrdId");
            List<Map<String,Object>> circuitData = (List<Map<String,Object>>) MapUtils.getObject(map, "circuitData");
            String cstOrdId = MapUtils.getString(map, "cstOrdId");

            for (int i = 0; i < circuitData.size(); i++) {
                Map<String, Object> circuitInfo = circuitData.get(i);
                String srvOrdId = MapUtils.getString(circuitInfo, "SRV_ORD_ID");
                String woId = MapUtils.getString(circuitInfo, "WO_ID");
                String productType =  MapUtils.getString(circuitInfo, "SERVICE_ID");
                //通过单号查出原报文日志表报文 核查单
                List<Map<String, Object>> logList = wsd.queryInterfaceLog(cstOrdId,srvOrdId);
                for (Map interfaceLog :logList) {
                    String content = MapUtils.getString(interfaceLog, "CONTENT");
                    String returnContent = MapUtils.getString(interfaceLog, "RETURN_CONTENT");
                    JSONObject returnJson = JSONObject.parseObject(returnContent);
                    JSONObject body = JSONObject.parseObject(returnJson.getString("UNI_BSS_BODY"));
                    JSONObject orderRespone = JSONObject.parseObject(body.getString("APPLY_ORDER_RSP"));
                    String  respCode = orderRespone.getString("RESP_CODE");
                    // 解析报文
                    JSONObject jsStr = JSONObject.parseObject(content);
                    String UNI_BSS_BODY = jsStr.getString("UNI_BSS_BODY");
                    JSONObject js_UNI_BSS_BODY = JSONObject.parseObject(UNI_BSS_BODY);
                    JSONObject js_APPLY_ORDER_REQ = JSONObject.parseObject(js_UNI_BSS_BODY.getString("APPLY_ORDER_REQ"));
                    JSONObject js_CST_ORD = JSONObject.parseObject(js_APPLY_ORDER_REQ.getString("CST_ORD"));
                    JSONObject js_CST_ORD_INFO = JSONObject.parseObject(js_CST_ORD.getString("CST_ORD_INFO"));
                    JSONObject js_SRV_ORD_LIST = JSONObject.parseObject(js_CST_ORD_INFO.getString("SRV_ORD_LIST"));
                    JSONArray js_SRV_ORD = JSON.parseArray(js_SRV_ORD_LIST.getString("SRV_ORD"));
                    JSONObject srvOrdJson = js_SRV_ORD.getJSONObject(0);
                    String activeType = srvOrdJson.getString("ACTIVE_TYPE");
                    String tradeTypeCode = srvOrdJson.getString("TRADE_TYPE_CODE");
//                {"UNI_BSS_BODY":{"APPLY_ORDER_RSP":{"RESP_DESC":"发起流程成功","RESP_CODE":"0"}}}

                    //正常新开并且返回成功报文 ACTIVE_TYPE TRADE_TYPE_CODE
                    if("1".equals(activeType) && "2009".equals(tradeTypeCode) && "0".equals(respCode)){
                        requestJson = content;
                        break;
                    }
                }
                if(!StringUtils.isEmpty(requestJson)){
                    //查询电路信息对原报文增加相应得资源信息通过srvOrdId productType
                    JSONObject checkJson = appendJson(srvOrdId,productType);
                    Map jsStr = JSONObject.parseObject(requestJson,Map.class);
                    Map body = MapUtils.getMap(jsStr, "UNI_BSS_BODY");
                    Map applyOrderReq = MapUtils.getMap(body, "APPLY_ORDER_REQ");
                    Map cstOrd = MapUtils.getMap(applyOrderReq, "CST_ORD");
                    Map cstOrdInfo = MapUtils.getMap(cstOrd, "CST_ORD_INFO");
                    Map srvOrdList = MapUtils.getMap(cstOrdInfo, "SRV_ORD_LIST");
                    List<Map<String,Object>> srvOrd = (List<Map<String,Object>>) MapUtils.getObject(srvOrdList, "SRV_ORD");

                    for (Map<String,Object> obj:srvOrd) {
                        tradeId = MapUtils.getString(obj, "TRADE_ID");
                        obj.put("WO_ID", woId);
                        Map srvOrdInfo = MapUtils.getMap(obj, "SRV_ORD_INFO");
                        srvOrdInfo.put("SRV_ATTR_GRP_LIST", checkJson);
                    }
                    //调用工建系统接口
                    sendJson = JSONObject.toJSONString(jsStr);
                    String url = wsd.queryUrl("preAssessment");
                    constructResponse = xmlUtil.sendHttpPostOrderCenter(url,sendJson);
                    String code = MapUtils.getString(constructResponse,"code","");
                    if("200".equals(code)){
                        response = MapUtils.getString(constructResponse,"msg");
                        JSONObject respJson = JSONObject.parseObject(response);
                        //0：成功；1：失败，报文错误；
                        Map respBody = JSONObject.parseObject(respJson.getString("UNI_BSS_BODY"), Map.class);
                        Map<String,Object> respOrder = MapUtils.getMap(respBody,"APPLY_ORDER_RSP");
                        String respCode = MapUtils.getString(respOrder,"RESP_CODE");
                        String respDesc = MapUtils.getString(respOrder,"RESP_DESC");
                        if("0".equals(respCode)){
                            retMap.put("success",true);
                        } else{
                            retMap.put("success",false);
                            retMap.put("message",respDesc);
                        }
                    } else{
                        retMap.put("success",false);
                        retMap.put("message","接口交互失败："+code);
                    }
                }else{
                    retMap.put("success", false);
                    retMap.put("message", "请检查是否有对应的接口记录" );
                }
            }


        } catch (Exception e) {
            retMap.put("success", false);
            retMap.put("message", "接口交互失败："+e.getMessage());
        } finally {
            insertInterfaceLog(tradeId,sendJson,JSONObject.toJSONString(constructResponse));
        }
        return retMap;
    }


    /**
     * 查询核查汇总选择的信息
     * @param srvOrderId
     * @param type
     * @param productType
     * @return
     */
    private List<JSONObject> qrySrvAttrInfo(String srvOrderId, String type,String productType) {
                // 属性编码信息
                List<JSONObject> srvAttrInfoList = new ArrayList<>();
                // 根据sev_ord_id查询wo_id和需要反馈的大字段
                Map<String,Object> finishInfoMap = interfaceBoDao.queryWoIdBySrvOrdId(srvOrderId,"CHECK_TOTAL");
                String woId = MapUtils.getString(finishInfoMap,"WO_ID","");

                // 查询通用信息 A端、Z端、全程（B）都需要
                if(!"80000466".equals(productType)){
                    List<Map<String,Object>> generalList = interfaceBoDao.queryGeneralInfo(woId);
                    if (generalList != null && generalList.size() > 0) {
                        for(Map<String,Object> tmp : generalList){
                            JSONObject attr =  new JSONObject();
                            attr.put("ATTR_ACTION", "0");
                            attr.put("ATTR_CODE", MapUtils.getString(tmp,"ATTR_CODE",""));
                            attr.put("ATTR_VALUE", MapUtils.getString(tmp,"ATTR_VALUE",""));
                            srvAttrInfoList.add(attr);
                        }
                    }
                }else if("80000466".equals(productType)){
                    Map<String, Object> otnMap = checkFeedbackDao.queryInfoByWoId(woId);
                    String Z_RES_SATISFY = "0".equals(MapUtils.getString(otnMap,"Z_RES_SATISFY",""))?"1":"0";
                    JSONObject REC_100076 =  new JSONObject();
                    REC_100076.put("ATTR_ACTION", "0");
                    REC_100076.put("ATTR_CODE", "REC_100076");
                    REC_100076.put("ATTR_VALUE",Z_RES_SATISFY);
                    srvAttrInfoList.add(REC_100076);
                    JSONObject REC_100077 =  new JSONObject();
                    REC_100077.put("ATTR_ACTION", "0");
                    REC_100077.put("ATTR_CODE", "REC_100077");
                    REC_100077.put("ATTR_VALUE", MapUtils.getString(otnMap,"Z_CONSTRUCT_PERIOD",""));
                    srvAttrInfoList.add(REC_100077);
                    JSONObject REC_100078 =  new JSONObject();
                    REC_100078.put("ATTR_ACTION", "0");
                    REC_100078.put("ATTR_CODE", "REC_100078");
                    REC_100078.put("ATTR_VALUE", MapUtils.getString(otnMap,"Z_CONSTRUCT_SCHEME",""));
                    srvAttrInfoList.add(REC_100078);
                }
                List<Map<String,Object>> attrlist = null;
                if("A".equals(type)){
                    // 查询核查单反馈接口需要的A端信息
                    attrlist = interfaceBoDao.getConstructCheckInfo(woId,"A");
                } else if("Z".equals(type)){
                    // 查询核查单反馈接口需要的A端信息
                    attrlist = interfaceBoDao.getConstructCheckInfo(woId,"Z");
                }

                if (attrlist != null && attrlist.size() > 0) {
                    for(Map<String,Object> tmp : attrlist){
                        JSONObject attr =  new JSONObject();
                        attr.put("ATTR_ACTION", "0");
                        attr.put("ATTR_CODE", MapUtils.getString(tmp,"ATTR_CODE",""));
                        attr.put("ATTR_VALUE", MapUtils.getString(tmp,"ATTR_VALUE",""));
                        if("REC_50062".equals(MapUtils.getString(tmp,"ATTR_CODE",""))){
                            if("A".equals(type)){
                                attr.put("ATTR_VALUE",MapUtils.getString(finishInfoMap,"A_CONSTRUCT_SCHEME",""));
                            } else if("Z".equals(type)){
                                attr.put("ATTR_VALUE",MapUtils.getString(finishInfoMap,"Z_CONSTRUCT_SCHEME",""));
                            }
                        }
                        if("REC_50061".equals(MapUtils.getString(tmp,"ATTR_CODE",""))){
                            String isResSatisfy = MapUtils.getString(tmp,"ATTR_VALUE","");
                            attr.put("ATTR_VALUE", isResSatisfy.equals("0")?"1":"0");
                        }
                        srvAttrInfoList.add(attr);
                    }
                }

                /** 金融精品网特殊处理
                 REC_100076	1	String	V2	资源是否满足
                 REC_100077	?	String	V30	建设工期
                 REC_100078	1	String	V2000	资源情况描述
                 JSONObject resSatisfyAttr =  new JSONObject();
                 resSatisfyAttr.put("ATTR_ACTION", "0");
                 resSatisfyAttr.put("ATTR_CODE", "REC_100078");
                 resSatisfyAttr.put("ATTR_VALUE", MapUtils.getString(finishInfoMap,"REC_50061","")+MapUtils.getString(finishInfoMap,"Z_CONSTRUCT_SCHEME",""));
                 srvAttrInfoList.add(resSatisfyAttr);
                 JSONObject constructSchemeAttr =  new JSONObject();
                 constructSchemeAttr.put("ATTR_ACTION", "0");
                 constructSchemeAttr.put("ATTR_CODE", "REC_100078");
                 constructSchemeAttr.put("ATTR_VALUE", MapUtils.getString(finishInfoMap,"A_CONSTRUCT_SCHEME","")+MapUtils.getString(finishInfoMap,"Z_CONSTRUCT_SCHEME",""));
                 srvAttrInfoList.add(constructSchemeAttr);*/
        return  srvAttrInfoList;
    }

    /**
     * 记录接口日志
     * @param request
     * @param respone
     */
    private void insertInterfaceLog(String tradeId,String request,String respone){
        Map<String,Object> interflog = new HashMap<String, Object>();
        interflog.put("INTERFNAME","OSS To 工建系统 核查前期评估");
        interflog.put("URL","webservice/construct/ResPreAssessment");
        interflog.put("CONTENT",request);
        interflog.put("RETURNCONTENT",respone);
        interflog.put("ORDERNO",tradeId);
        interflog.put("REMARK","发送工建系统 json报文");
        wsd.insertInterfLog(interflog);
    }

    // 根据指定长度生成纯数字的随机数
    public String createData(int length) {
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(rand.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 拼接核查汇总AZ 端核查信息给工建
     * @param srvOrderId
     * @param productType
     * @return
     */
    private JSONObject appendJson(String srvOrderId,String productType){
        // 核查单，拼装核查反馈信息
        List srvAttrInfoAList = new ArrayList();
        List srvAttrInfoBList = new ArrayList();
        List srvAttrInfoZList = new ArrayList();
        // 属性组列表
        List<JSONObject> srvAttrGrpList = new ArrayList<>();
        JSONObject srvAttrGrp = new JSONObject();
        // A端属性信息
        JSONObject srvAttrGrpA = new JSONObject();
        // B端属性信息
        JSONObject srvAttrGrpB = new JSONObject();
        // Z端属性信息
        JSONObject srvAttrGrpZ = new JSONObject();

        //互联网专线只有一端
        if("10000011".equals(productType)){
            // 核查单，拼装核查反馈信息
            srvAttrInfoZList = qrySrvAttrInfo(srvOrderId,"Z",productType);

            srvAttrGrpA.put("ATTR_GRP_CODE", "A");
            srvAttrGrpA.put("ATTR_GRP_NAME", "A");
            srvAttrGrpA.put("ATTR_GRP_ID", "100");
            JSONObject srvAttrInfoA = new JSONObject();
            srvAttrInfoA.put("SRV_ATTR_INFO", srvAttrInfoZList);
            srvAttrGrpA.put("ATTR_INFO_LIST", srvAttrInfoA);
            if (srvAttrInfoZList.size() > 0) {
                srvAttrGrpList.add(srvAttrGrpA);
            }
        }else{
            // 核查单，拼装核查反馈信息
            srvAttrInfoAList = qrySrvAttrInfo(srvOrderId,"A",productType);
            srvAttrInfoBList = qrySrvAttrInfo(srvOrderId,"B",productType);
            srvAttrInfoZList = qrySrvAttrInfo(srvOrderId,"Z",productType);

            srvAttrGrpA.put("ATTR_GRP_CODE", "A");
            srvAttrGrpA.put("ATTR_GRP_NAME", "A");
            srvAttrGrpA.put("ATTR_GRP_ID", "100");
            JSONObject srvAttrInfoA = new JSONObject();
            srvAttrInfoA.put("SRV_ATTR_INFO", srvAttrInfoAList);
            srvAttrGrpA.put("ATTR_INFO_LIST", srvAttrInfoA);
            if (srvAttrInfoAList.size() > 0 && !"80000466".equals(productType)) {
                srvAttrGrpList.add(srvAttrGrpA);
            }
            srvAttrGrpZ.put("ATTR_GRP_CODE", "Z");
            srvAttrGrpZ.put("ATTR_GRP_NAME", "Z");
            srvAttrGrpZ.put("ATTR_GRP_ID", "120");
            JSONObject srvAttrInfoZ = new JSONObject();
            srvAttrInfoZ.put("SRV_ATTR_INFO", srvAttrInfoZList);
            srvAttrGrpZ.put("ATTR_INFO_LIST", srvAttrInfoZ);
            if (srvAttrInfoZList.size() > 0 && !"80000466".equals(productType)) { //不是政企精品网
                srvAttrGrpList.add(srvAttrGrpZ);
            }
            srvAttrGrpB.put("ATTR_GRP_CODE", "B");
            srvAttrGrpB.put("ATTR_GRP_NAME", "B");
            srvAttrGrpB.put("ATTR_GRP_ID", "110");
            JSONObject srvAttrInfoB = new JSONObject();
            srvAttrInfoB.put("SRV_ATTR_INFO", srvAttrInfoBList);
            srvAttrGrpB.put("ATTR_INFO_LIST", srvAttrInfoB);
            if (srvAttrInfoBList.size() > 0) {
                srvAttrGrpList.add(srvAttrGrpB);
            }
        }

        srvAttrGrp.put("SRV_ATTR_GRP", srvAttrGrpList);
        return srvAttrGrp;
    }
}
