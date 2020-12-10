package com.zres.project.localnet.portal.cloudNetWork.service;

import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.cloudNetWork.dao.CloudNetworkInterfaceDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.CheckFeedbackDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.initApplOrderDetail.dao.InsertOrderInfoDao;
import com.zres.project.localnet.portal.util.XmlUtil;
import com.zres.project.localnet.portal.webservice.data.dao.InterfaceBoDao;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import com.ztesoft.res.frame.user.inf.UserInfo;
import org.apache.axis.utils.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Description 功能描述: 反馈、报竣接口
 * @Param:
 * @Return:
 * @Author: wang.gang2
 * @Date: 2020/11/25 14:39
 */
@Service
public class CloudNetworkFinishService implements CloudNetworkFinishOrderIntf {
    private static final Logger logger = Logger.getLogger(CloudNetworkFinishService.class);

    @Autowired
    private XmlUtil xmlUtil;
    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private InterfaceBoDao interfaceBoDao;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private InsertOrderInfoDao insertOrderInfoDao;
    @Autowired
    private CheckFeedbackDao checkFeedbackDao;
    @Autowired
    private CloudNetworkInterfaceDao cloudNetworkInterfaceDao;

    @Override
    public Map<String,Object> finishOrder(@RequestParam Map<String, Object> map) {

        map.put("resp_code", "0");
        String srvOrderId = MapUtils.getString(map,"srvOrdId","");
        map.put("srvOrderId", srvOrderId);
        Map<String,Object> returnMap = new HashMap<String,Object>();
        try {
            // 拼报文
            String reqJsonStr = createFinishOrderJsonStr(map);
            logger.info("------反馈发送报文---reqJsonStr:" + reqJsonStr);
            // 获取云组网反馈接口 -url
            String url = wsd.queryUrl("cloudNetworkFinish");
            // 调用rest接口，将报文发往订单中心-集客
            Map<String, Object> jkResponse = xmlUtil.sendHttpPostOrderCenter(url, reqJsonStr);
            logger.info("------云组网返回报文---jkResponse: " + jkResponse);
            // 插入接口记录
            Map<String, Object> interflog = new HashMap<String, Object>();
            interflog.put("INTERFNAME", "云组网反馈接口finishOrder");
            interflog.put("URL", url);
            interflog.put("CONTENT", reqJsonStr);
            interflog.put("ORDERNO", MapUtils.getString(map,"srvOrdId",""));
            interflog.put("RETURNCONTENT", jkResponse.get("msg"));
            interflog.put("REMARK", "接收云组网json报文-backOrder");
            wsd.insertInterfLog(interflog);

            // 解析返回报文
            String respCode = null;
            String respDesc = null;
            if ("200".equals(jkResponse.get("code"))) {
                JSONObject msg = JSONObject.parseObject(jkResponse.get("msg").toString());
                String uniBssBody = msg.getString("UNI_BSS_BODY");
                JSONObject jsUniBssBody = JSONObject.parseObject(uniBssBody);
                JSONObject jsFinishOrderRsp = jsUniBssBody.getJSONObject("FINISH_ORDER_RSP");
                if (jsFinishOrderRsp != null) {
                    // 处理结果0：成功；1：失败，报文错误；2：失败，附件没有
                    respCode = jsFinishOrderRsp.getString("RESP_CODE");
                    // 处理原因
                    respDesc = jsFinishOrderRsp.getString("RESP_DESC");
                } else {
                    respCode = "-1";
                    respDesc = "返回报文无FINISH_ORDER_RSP，请联系管理员！";
                }

            } else {
                respCode = "-1";
                respDesc = "返回报文格式错误，请联系管理员！";
            }

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置日期格式
            // 统一成功失败的code
            if ("0".equals(respCode)) {
                respCode = "1";
            } else if ("1".equals(respCode)) {
                respCode = "0";
            }

            Map<String, Object> rmap = new HashMap<String, Object>();
            rmap.put("srv_ord_id", srvOrderId);
            rmap.put("attr_code", respCode);
            rmap.put("attr_name", "");
            rmap.put("attr_value", "");
            rmap.put("attr_value_name", "云组网反馈接口返回结果");
            rmap.put("create_date", df.format(new Date()));
            rmap.put("sourse", "");
            rmap.put("attr_action", "FinishOrder");
            wsd.saveRetInfo(rmap);

            // 反馈接口处理结果
            returnMap.put("RESP_CODE", respCode);
            // 处理失败原因
            returnMap.put("RESP_DESC", respDesc);
            logger.info("-------云组网返回结果---respCode:" + respCode + "--" + respDesc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnMap;
    }

    /**
     * 拼接请求报文
     * @param map
     * @return
     */
    private String createFinishOrderJsonStr(Map<String,Object> map) throws Exception{
        String srvOrderId = MapUtils.getString(map,"srvOrdId","");
        logger.info("srvOrderId： " + srvOrderId);
        List<Map<String,Object>> srvlist = orderDealDao.queryOrderInfoList(srvOrderId);
        logger.info("反馈信息查询--" + srvlist.toString());

        Map<String,Object> mp = srvlist.get(0);
        // 订单类型 101：开通；102：核查
        String orderType = MapUtils.getString(mp,"ORDER_TYPE","");
        /* 路由信息 */
        Map<String, Object>  routing = new HashMap<>();
        /* 路由类型:00:按系统路由 */
        routing.put("ROUTE_TYPE", MapUtils.getString(mp, "ROUTE_TYPE"));
        /* 路由关键值:01:集客综合订单;02:集客订单中心;03:政企订单中心 */
        routing.put("ROUTE_VALUE", MapUtils.getString(mp, "ROUTE_VALUE"));

        Map<String, Object> finishOrderReq = new HashMap<>();
        finishOrderReq.put("ROUTING", routing);

        // 业务订单信息
        String respDesc = null;
        if ("1".equals(MapUtils.getString(map,"resp_code")) || "2".equals(MapUtils.getString(map,"resp_code")) ) {
            respDesc = map.get("resp_desc").toString();
        }

        JSONObject srvOrdList = new JSONObject();
        for (int i = 0; srvlist.size() - i > 0; i++) {
            Map<String, Object> srvmp = srvlist.get(i);
            Map<String, Object> srvOrd = new HashMap<>();
            List<Map<String, Object>> srvOrdArr = new ArrayList<>();
            srvOrd.put("FINISH_TYPE", "1");
            srvOrd.put("OPERATE_TIME", srvmp.get("CREATE_DATE"));
            srvOrd.put("RESP_CODE", MapUtils.getString(map,"resp_code"));
            if (!StringUtils.isEmpty(respDesc)) {
                srvOrd.put("RESP_DESC", respDesc);
            }
            srvOrd.put("SERIAL_NUMBER", srvmp.get("SERIAL_NUMBER"));
            srvOrd.put("TRADE_ID", srvmp.get("TRADE_ID"));
            srvOrd.put("FLOW_ID", srvmp.get("FLOW_ID"));
            // 拼接反馈属性节点
            srvOrd.put("SRV_ORD_INFO", createSrvOrdInfo(mp));
            List<Map<String, Object>> attrFilelist = new ArrayList();
            if(orderType.equals("101")){
                // 开通单，查询全程调测环节上传的附件
                attrFilelist = interfaceBoDao.getAttrFileMsgInfo(srvOrderId);
            }else if(orderType.equals("102")){
                // 核查单，查询核查汇总环节上传的附件
                attrFilelist = interfaceBoDao.getAttrFileMsgInfoCheck(srvOrderId);
            }
            if (attrFilelist != null && attrFilelist.size() > 0) {
                List<JSONObject> attachInfoList = new ArrayList<>();
                JSONObject attachInfo = new JSONObject();
                List<Map<String, Object>> attachmentList = new ArrayList<>();

                for (int f = 0; attrFilelist.size() - f > 0; f++) {
                    Map<String, Object> attrFilemp = attrFilelist.get(f);
                    JSONObject attch = new JSONObject();
                    String fileId = MapUtils.getString(attrFilemp, "FILE_ID", "") + "."
                        + MapUtils.getString(attrFilemp, "FILE_TYPE", "");
                    String fileName = MapUtils.getString(attrFilemp, "FILE_NAME", "");
                    String filePath = MapUtils.getString(attrFilemp, "FILE_PATH", "");
                    String fileType = MapUtils.getString(attrFilemp, "FILE_TYPE", "");
                    attch.put("FILE_ID", fileId);
                    attch.put("FILE_NAME", fileName);
                    attch.put("FILE_PATH", "/upload/");
                    attch.put("FILE_TYPE", "1");
                    attachInfoList.add(attch);
                    Map<String, Object> attachMap = new HashMap<>();
                    attachMap.put("srv_ord_id", srvOrderId);
                    attachMap.put("path", filePath);
                    attachMap.put("name", fileId);
                    attachMap.put("value", fileName);
                    attachMap.put("type", fileType);
                    attachmentList.add(attachMap);
                }
                if (attachInfoList.size() > 0) {
                    attachInfo.put("ATTACH_INFO", attachInfoList);
                    srvOrd.put("ATTACH_INFO_LIST", attachInfo);
                    // 附件改为在全称调测环节同步上传了
                    /*
                     * // 创建异步处理附件 HandleAttachment ha = new HandleAttachment(attachmentList, "FTP_INFO",
                     * "JIKE_FTP_INFO","uploadJiKe"); // 启动异步处理附件 ha.start();
                     */
                }
            }
            srvOrdArr.add(srvOrd);
            srvOrdList.put("SRV_ORD", srvOrdArr);
        }
        Map<String, Object> cstOrd = new HashMap<>();
        cstOrd.put("SUBSCRIBE_ID", mp.get("SUBSCRIBE_ID"));
        cstOrd.put("SRV_ORD_LIST", srvOrdList);
        finishOrderReq.put("CST_ORD", cstOrd);
        List paraList = orderDealDao.queryOrderAttrInfoList(srvOrderId);
        if (paraList.size() > 0) {
            List< Map<String, Object>> para = new ArrayList<>();
            for (int i = 0; paraList.size() - i > 0; i++) {
                HashMap<String,Object> paramp = (HashMap) paraList.get(i);
                Map<String, Object> paraObj = new HashMap<>();
                paraObj.put("PARA_ID", MapUtils.getString(paramp, "PARA_ID"));
                paraObj.put("PARA_VALUE", MapUtils.getString(paramp, "PARA_VALUE"));
                para.add(paraObj);
            }
            finishOrderReq.put("PARA", para);
        }
        Map<String, Object> uniBssBody = new HashMap<>();
        uniBssBody.put("FINISH_ORDER_REQ", finishOrderReq);
        Map<String, Object> json = new HashMap<>();
        json.put("UNI_BSS_BODY", uniBssBody);
        Map<String, Object> reqMap = queryCloudNetworkInfo(map);
        reqMap.put("param", reqMap);
        return JSONObject.toJSONString(reqMap);
    }

    /**
     * 拼接报文节点:SRV_ORD_INFO
     * @param map
     * @return
     */
    public JSONObject createSrvOrdInfo(Map<String,Object> map) {
        String productType = MapUtils.getString(map,"SERVICE_ID");
        String srvOrderId = MapUtils.getString(map,"SRV_ORD_ID");
        String orderType = MapUtils.getString(map,"ORDER_TYPE","");
        // A端属性编码信息
        List<JSONObject> srvAttrInfoAList = new ArrayList<>();
        // B端属性编码信息
        List<JSONObject> srvAttrInfoBList = new ArrayList<>();
        // Z端属性编码信息
        List<JSONObject> srvAttrInfoZList = new ArrayList<>();

        // 开通单拼装反馈报文
        if(orderType.equals("101")){
            // 互联网、以太网、SDH
            Map attrUserTime = wsd.qryFinishOrdInfo(srvOrderId);
            if (!MapUtils.isEmpty(attrUserTime)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                String finishTime = insertOrderInfoDao.queryFinishTime(srvOrderId);
                if(StringUtils.isEmpty(finishTime)){
                    finishTime = dateFormat.format(new Date());
                }
                Map<String,Object> attrMap = new HashMap<>();
                attrMap.put("REC_30004",finishTime); // B端全程竣工时间--B
                attrMap.put("REC_20011",MapUtils.getString(attrUserTime, "USER_NAME", "")); // B端报竣人--B
                attrMap.put("REC_20012",dateFormat.format(new Date())); // B端报竣时间
                attrMap.put("REC_20039",MapUtils.getString(attrUserTime, "CIRCUITCODE", "")); // 电路代号
                attrMap.put("REC_20037",MapUtils.getString(attrUserTime, "DISPATCH_ORDER_NO", "")); // 调单号

                srvAttrInfoBList = createAttrList(attrMap);
                /*
                 因为目前A端Z端之反馈了一个属性：本端竣工时间，所以共用了一个map
                 */
                Map<String,Object> attrAMap = new HashMap<>();
                attrMap.put("REC_20002",finishTime); // 本端竣工时间
                srvAttrInfoAList = createAttrList(attrAMap);
                // Z端本端竣工时间
                srvAttrInfoZList = createAttrList(attrAMap);
            }
        } else if(orderType.equals("102")){

            // 核查单，拼装核查反馈信息
            srvAttrInfoAList = qrySrvAttrInfo(srvOrderId,"A",productType);
            srvAttrInfoBList = qrySrvAttrInfo(srvOrderId,"B",productType);
            srvAttrInfoZList = qrySrvAttrInfo(srvOrderId,"Z",productType);
        }
        // 属性组列表
        List<JSONObject> srvAttrGrpList = new ArrayList<>();
        JSONObject srvAttrGrp = new JSONObject();
        // A端属性信息
        JSONObject srvAttrGrpA = createAttrGrp(srvAttrInfoAList,"A");
        if (srvAttrInfoAList.size() > 0 && !"80000466".equals(productType)) {
            srvAttrGrpList.add(srvAttrGrpA);
        }
        // B端属性信息
        JSONObject srvAttrGrpB = createAttrGrp(srvAttrInfoBList, "B");
        if (srvAttrInfoBList.size() > 0) {
            srvAttrGrpList.add(srvAttrGrpB);
        }
        // Z端属性信息
        JSONObject srvAttrGrpZ = createAttrGrp(srvAttrInfoZList,"Z");
        if (srvAttrInfoZList.size() > 0 && !"80000466".equals(productType)) { //不是政企精品网
            srvAttrGrpList.add(srvAttrGrpZ);
        }
        srvAttrGrp.put("SRV_ATTR_GRP", srvAttrGrpList);

        // 业务订单信息
        JSONObject srvOrdInfo = new JSONObject();
        srvOrdInfo.put("SRV_ATTR_GRP_LIST", srvAttrGrp);
        return srvOrdInfo;
    }

    /**
     * @Description 功能描述: 拼接固定请求参数
     * @Param: []
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wang.gang2
     * @Date: 2020/11/20 15:05
     *  "ORDER_INFO": {
     *         "FLOW_ID": "497818058",
     *         "SERIAL_NUMBER": "831HLW000418",
     *         "SVR_CODE": "Y0371037100079",
     *         "TRADE_ID": "",
     *         "AZ": "a"
     *     },
     *    "OPERATOR": {
     *         "USER_NAME": "hqzhangs",
     *         "PHONE": "15833164458",
     *         "PROVINCE": "河南省",
     *         "CITY": "郑州市",
     *         "COMPANY": "中讯邮电"
     *     },
     *     "WORK_SHEET_NUMBER": "",
     */
    private Map<String,Object> queryCloudNetworkInfo(Map<String,Object> params){
        String flag = null;
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> orderInfo = new HashMap<>();
        Map<String, Object> operator = new HashMap<>();
        UserInfo user = ThreadLocalInfoHolder.getLoginUser();
        Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(user.getUserId()));

        Map<String, Object> cloudNetworkInfo = cloudNetworkInterfaceDao.queryCloudNetworkInfo(params);
        orderInfo.put("FLOW_ID", MapUtils.getString(cloudNetworkInfo,"FLOW_ID"));
        orderInfo.put("SERIAL_NUMBER",MapUtils.getString(cloudNetworkInfo,"SERIAL_NUMBER"));
        orderInfo.put("SVR_CODE",MapUtils.getString(cloudNetworkInfo,"SVR_CODE"));
        orderInfo.put("TRADE_ID",MapUtils.getString(cloudNetworkInfo,"TRADE_ID"));
        //localA、 localZ、 B PARALLEL_FLAG FROM gom_bdw_srv_ord_info
        String parallelFlag = MapUtils.getString(cloudNetworkInfo, "PARALLEL_FLAG");
        switch(parallelFlag){
            case "localZ" :
                flag = "z";
                break;
            case "localA" :
                flag = "a";
                break;
            case "B" :
                flag = "az";
                break;
            default:
                flag = "";
                break;
        }
        orderInfo.put("AZ",flag);

        operator.put("USER_NAME", user.getUserName());
        operator.put("PHONE", MapUtils.getString(operStaffInfoMap,"USER_PHONE"));
        operator.put("PROVINCE", MapUtils.getString(operStaffInfoMap,"AREANAME"));
        operator.put("CITY", "");
        operator.put("COMPANY", MapUtils.getString(operStaffInfoMap,"ORG_NAME"));

        body.put("ORDER_INFO", orderInfo);
        body.put("OPERATOR", operator);
        body.put("WORK_SHEET_NUMBER", "");
        return body;
    }



    /**
     * list<map>转成list<JSONObject>
     * @param mapList
     * @return
     */
    private List<JSONObject> mapParseJsonObject(List<Map<String,Object>> mapList) {
        List<JSONObject> jsonObjectList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(mapList)) {
            for(Map<String,Object> tmp : mapList){
                String attrValue = MapUtils.getString(tmp,"ATTR_VALUE","");
                if(!"".equals(attrValue)){
                    JSONObject attr =  new JSONObject();
                    attr.put("ATTR_ACTION", "0");
                    attr.put("ATTR_CODE", MapUtils.getString(tmp,"ATTR_CODE",""));
                    attr.put("ATTR_VALUE", attrValue);
                    jsonObjectList.add(attr);
                }
            }
        }
        return jsonObjectList;
    }

    /**
     * 拼接属性组信息
     * @param srvAttrInfoList
     * @param type
     * @return
     */
    private JSONObject createAttrGrp(List<JSONObject> srvAttrInfoList, String type) {
        JSONObject srvAttrGrp = new JSONObject();
        srvAttrGrp.put("ATTR_GRP_ID", System.currentTimeMillis() + createData(3));
        srvAttrGrp.put("ATTR_GRP_CODE", type);
        srvAttrGrp.put("ATTR_GRP_NAME", type);
        JSONObject srvAttrInfo = new JSONObject();
        srvAttrInfo.put("SRV_ATTR_INFO", srvAttrInfoList);
        srvAttrGrp.put("ATTR_INFO_LIST", srvAttrInfo);
        return srvAttrGrp;
    }

    /**
     * 拼接反馈属性 key-value节点
     * @param attrMap
     * @return
     */
    private List<JSONObject> createAttrList(Map<String, Object> attrMap) {
        List<JSONObject>  list=  new ArrayList<>();
        for(String tmpKey : attrMap.keySet()){
            if(!"".equals(MapUtils.getString(attrMap,tmpKey,""))){
                JSONObject attr = new JSONObject();
                attr.put("ATTR_ACTION", "0");
                attr.put("ATTR_CODE", tmpKey);
                attr.put("ATTR_VALUE", MapUtils.getString(attrMap,tmpKey,""));
                list.add(attr);
            }
        }
        return list;
    }

    private List<JSONObject> qrySrvAttrInfo(String srvOrderId, String type,String productType) {
        // 属性编码信息
        List<JSONObject> srvAttrInfoList = new ArrayList<>();
        // 根据srv_ord_id查询wo_id和需要反馈的大字段
        Map<String,Object> finishInfoMap = interfaceBoDao.queryWoIdBySrvOrdId(srvOrderId,"CHECK_TOTAL");
        String woId = MapUtils.getString(finishInfoMap,"WO_ID","");
        // 查询通用信息 A端、Z端、全程（B）都需要
        if(!"80000466".equals(productType)){
            List<Map<String,Object>> generalList = interfaceBoDao.queryGeneralInfo(woId);
            srvAttrInfoList = mapParseJsonObject(generalList);
        }else {
            Map<String, Object> otnMap = checkFeedbackDao.queryInfoByWoId(woId);
            String Z_RES_SATISFY = "0".equals(MapUtils.getString(otnMap,"Z_RES_SATISFY",""))?"1":"0";
            Map<String,Object> tmpMap = new HashMap<>();
            tmpMap.put("REC_100076",Z_RES_SATISFY);
            tmpMap.put("REC_100077",MapUtils.getString(otnMap,"Z_CONSTRUCT_PERIOD",""));
            tmpMap.put("REC_100078",MapUtils.getString(otnMap,"Z_CONSTRUCT_SCHEME",""));
            srvAttrInfoList = this.createAttrList(tmpMap);
        }
        List<Map<String,Object>> attrlist = null;
        if("A".equals(type)){
            // 查询核查单反馈接口需要的A端信息
            attrlist = interfaceBoDao.getCheckBackInfo(woId,"A");
        } else if("Z".equals(type)){
            // 查询核查单反馈接口需要的Z端信息
            attrlist = interfaceBoDao.getCheckBackInfo(woId,"Z");
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
        return  srvAttrInfoList;
    }

    /**
     * 根据指定长度生成纯数字的随机数
      */
    public String createData(int length) {
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(rand.nextInt(10));
        }
        return sb.toString();
    }
}
