package com.zres.project.localnet.portal.webservice.interfaceJiKe;

import com.alibaba.fastjson.JSONObject;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.CheckFeedbackDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCodeEnum;
import com.zres.project.localnet.portal.initApplOrderDetail.dao.InsertOrderInfoDao;
import com.zres.project.localnet.portal.util.XmlUtil;
import com.zres.project.localnet.portal.webservice.data.dao.InterfaceBoDao;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.zres.project.localnet.portal.webservice.flow.ExceptionChangeService;
import org.apache.axis.utils.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by tang.huili on 2019/5/10. Update by huang.xingfei01 on 2019/5/12. 反馈接口
 */
@Service
// @Controller
// @RequestMapping("/finishOrder")
public class FinishOrderService implements FinishOrderServiceIntf {
    private static final Logger logger = Logger.getLogger(FinishOrderService.class);

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
    @Override
    // @IgnoreSession
    // @ResponseBody
    // @RequestMapping(value = "/interfaceBDW/finishOrder.spr", method = RequestMethod.POST, produces =
    // "application/json;charset=UTF-8")
    public Map finishOrder(Map<String, Object> map) {
        //   Map<String, String> map = new HashMap<>();
        //   map.put("srvOrderId", srvOrderId);
        map.put("resp_code", "0");
        String srvOrderId = MapUtils.getString(map,"srvOrdId","");
        map.put("srvOrderId", srvOrderId);
        Map returnMap = new HashMap();
        try {
            Boolean flag= map.keySet().contains("activeType")&&
                    ExceptionChangeService.EXCEPTION_4A.equals(MapUtils.getString(map,"activeType"));
            // 拼报文
            map.put("flag",flag);
            String reqJsonStr = createFinishOrderJsonStr(map);
            logger.info("------反馈发送报文---reqJsonStr:" + reqJsonStr);
            // 获取订单中心 -url
            String url = wsd.queryUrl("finishOrder");
            // 调用rest接口，将报文发往订单中心-集客
            Map<String, Object> jkResponse = xmlUtil.sendHttpPostOrderCenter(url, reqJsonStr);
            logger.info("------集客返回报文---jkResponse: " + jkResponse);

            // 插入接口记录
            Map<String, Object> interflog = new HashMap<String, Object>();
            interflog.put("INTERFNAME", "集客反馈接口finishOrder");
            interflog.put("URL", url);
            interflog.put("CONTENT", reqJsonStr);
            interflog.put("ORDERNO", MapUtils.getString(map,"srvOrdId",""));
            interflog.put("RETURNCONTENT", jkResponse.get("msg"));
            interflog.put("REMARK", "接收集客json报文-backOrder");
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
            /*
               如果是追单，不入库调用反馈接口的成功失败标志
               map.put("activeType","4A");// 代表追单
               只有正常单才会入库反馈接口的成功失败标志
              */
            Map<String, Object> rmap = new HashMap<String, Object>();
            rmap.put("srv_ord_id", srvOrderId);
            rmap.put("attr_code", respCode);
            rmap.put("attr_name", "");
            rmap.put("attr_value", "");
            rmap.put("attr_value_name", "集客反馈接口返回结果");
            rmap.put("create_date", df.format(new Date()));
            rmap.put("sourse", "");
            if(!flag){
                rmap.put("attr_action", "FinishOrder");
            } else{
                rmap.put("attr_action", "FinishOrder_4A");
                rmap.put("attr_value", MapUtils.getString(map,"FLOW_ID",""));
            }
            wsd.saveRetInfo(rmap);

            // 反馈接口处理结果
            returnMap.put("RESP_CODE", respCode);
            // 处理失败原因
            returnMap.put("RESP_DESC", respDesc);
            logger.info("-------集客返回结果---respCode:" + respCode + "--" + respDesc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnMap;
    }

    /**
     * 拼接请求报文
     *
     * @param map
     * @return
     */
    private String createFinishOrderJsonStr(Map map) throws Exception{
        String srvOrderId = MapUtils.getString(map,"srvOrdId","");
    //    String productType = MapUtils.getString(map, "serviceId"); //产品类型
   //     logger.info("*****ren.jiahang***产品编码*********"+productType);
        logger.info("srvOrderId： " + srvOrderId);
        List <Map<String,Object>>  srvlist = orderDealDao.queryOrderInfoList(srvOrderId);
        logger.info("反馈报文信息查询--" + srvlist.toString());
        Map mp = srvlist.get(0);
        String productType = MapUtils.getString(mp,"SERVICE_ID","");
        // 订单类型 101：开通；102：核查
        String orderType = MapUtils.getString(mp,"ORDER_TYPE","");
        // 路由信息
        JSONObject routing = new JSONObject();
        // 路由类型:00:按系统路由
        routing.put("ROUTE_TYPE", MapUtils.getString(mp, "ROUTE_TYPE"));
        // 路由关键值:01:集客综合订单;02:集客订单中心;03:政企订单中心
        routing.put("ROUTE_VALUE", MapUtils.getString(mp, "ROUTE_VALUE"));
        JSONObject finishOrderReq = new JSONObject();
        finishOrderReq.put("ROUTING", routing);
        String respDesc = null;
        if (map.get("resp_code") != null
                && (map.get("resp_code").toString().equals("1") || map.get("resp_code").toString().equals("2"))) {
            respDesc = map.get("resp_desc").toString();
        }

        JSONObject srvOrdList = new JSONObject();
        for (int i = 0; srvlist.size() - i > 0; i++) {
            Map srvmp =  srvlist.get(i);
            JSONObject srvOrd = new JSONObject();
            List<JSONObject> srvOrdArr = new ArrayList<>();
            srvOrd.put("FINISH_TYPE", "1");
            srvOrd.put("OPERATE_TIME", srvmp.get("CREATE_DATE"));
            srvOrd.put("RESP_CODE", map.get("resp_code").toString());
            if (null != respDesc) {
                srvOrd.put("RESP_DESC", respDesc);
            }
            srvOrd.put("SERIAL_NUMBER", srvmp.get("SERIAL_NUMBER"));
            if(MapUtils.getBoolean(map,"flag",false)){
                srvOrd.put("TRADE_ID", map.get("TRADE_ID_RELA"));
                srvOrd.put("FLOW_ID", map.get("FLOW_ID"));
            }else {
                srvOrd.put("TRADE_ID", srvmp.get("TRADE_ID"));
                srvOrd.put("FLOW_ID", srvmp.get("FLOW_ID"));
            }
            // 拼接反馈属性节点
            srvOrd.put("SRV_ORD_INFO", createSrvOrdInfo(srvlist.get(i)));

            List attrFilelist = new ArrayList();
            if(MapUtils.getString(mp,"ORDER_TYPE","").equals("101")){
                // 查询全程调测环节上传的附件
                attrFilelist = interfaceBoDao.getAttrFileMsgInfo(srvOrderId);
            }else if(MapUtils.getString(mp,"ORDER_TYPE","").equals("102")){
                // 核查单查询核查汇总环节上传的附件
                attrFilelist = interfaceBoDao.getAttrFileMsgInfoCheck(srvOrderId);
                //moddify  by wang.gang2 增加核查汇总信息  REC_10025 建设工期 REC_50061 资源是否满足  REC_50062  资源情况描述 映射是否立项（REC_50014） 映射一次性费用（REC_10047）

            }
            if (attrFilelist != null && attrFilelist.size() > 0) {
                List<JSONObject> attachInfoList = new ArrayList<>();
                JSONObject attachInfo = new JSONObject();
                List<Map<String, Object>> attachmentList = new ArrayList<>();

                for (int f = 0; attrFilelist.size() - f > 0; f++) {
                    HashMap attrFilemp = (HashMap) attrFilelist.get(f);
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
        JSONObject cstOrd = new JSONObject();
        cstOrd.put("SUBSCRIBE_ID", mp.get("SUBSCRIBE_ID"));
        //以太网/互联网/数字电路，反馈核查标准化信息
        Set<String> serviceSet = new HashSet<>();
        serviceSet.add("10000001");// 数字电路
        serviceSet.add("10000002");// 以太网专线
        if("102".equals(orderType)&&serviceSet.contains(productType)){
            // 反馈核查标准化时增加字段:  资源核查回执版本号
            cstOrd.put("RES_VER_NO", "1");
        }
        cstOrd.put("SRV_ORD_LIST", srvOrdList);
        finishOrderReq.put("CST_ORD", cstOrd);

        List paraList = orderDealDao.queryOrderAttrInfoList(srvOrderId);
        if (paraList.size() > 0) {
            List<JSONObject> para = new ArrayList<>();
            for (int i = 0; paraList.size() - i > 0; i++) {
                HashMap paramp = (HashMap) paraList.get(i);
                JSONObject paraObj = new JSONObject();
                paraObj.put("PARA_ID", MapUtils.getString(paramp, "PARA_ID"));
                paraObj.put("PARA_VALUE", MapUtils.getString(paramp, "PARA_VALUE"));
                para.add(paraObj);
            }
            finishOrderReq.put("PARA", para);
        }

        JSONObject uniBssBody = new JSONObject();
        uniBssBody.put("FINISH_ORDER_REQ", finishOrderReq);

        JSONObject json = new JSONObject();
        json.put("UNI_BSS_HEAD", xmlUtil.requestHeader());
        json.put("UNI_BSS_BODY", uniBssBody);

        // 政企精品网产品特殊处理
        if("80000466".equals(MapUtils.getString(mp,"SERVICE_ID"))){
            return otnJson(json.toString());
        }
        return json.toString();
    }

    /**
     * 拼接报文节点:SRV_ORD_INFO
     * @param param
     * @return
     */
    public JSONObject createSrvOrdInfo(Map<String,Object> param) {
        String productType = MapUtils.getString(param,"SERVICE_ID");
        String srvOrderId = MapUtils.getString(param,"SRV_ORD_ID");
        // A端属性编码信息
        List<JSONObject> srvAttrInfoAList = new ArrayList<>();
        // B端属性编码信息
        List<JSONObject> srvAttrInfoBList = new ArrayList<>();
        // Z端属性编码信息
        List<JSONObject> srvAttrInfoZList = new ArrayList<>();
        // 开通单拼装反馈报文
        if(MapUtils.getString(param,"ORDER_TYPE","").equals("101")){
            // 互联网、以太网、SDH
            Map attrUserTime = wsd.qryFinishOrdInfo(srvOrderId);
            if (MapUtils.isEmpty(attrUserTime)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                String finishTime = insertOrderInfoDao.queryFinishTime(srvOrderId);
                if(finishTime != null && (!"".equals(finishTime))){
                } else {
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
                srvAttrInfoZList = createAttrList(attrAMap); // Z端本端竣工时间
            }
        } else if(MapUtils.getString(param,"ORDER_TYPE","").equals("102")){
            //以太网/数字电路，反馈核查标准化信息
            Set<String> serviceSet = new HashSet<>();
            serviceSet.add("10000001");// 数字电路
            serviceSet.add("10000002");// 以太网专线
            if(serviceSet.contains(productType)){
                // 根据srv_ord_id查询核查汇总的wo_id
                Map<String,Object> finishInfoMap = interfaceBoDao.queryWoIdBySrvOrdId(srvOrderId);
                String woId = MapUtils.getString(finishInfoMap,"WO_ID","");
                /**
                 * 集客下发并行核查，订单中心直接拆单给二干和本地，下发二干的并行核查需要区分AZ
                 * 集客来单并行核查，核查信息只有B端，并行核查只传一端信息，由订单中心进行合并
                 *
                 * 串行核查分两种情况：1.在二干核查汇总环节录入本地核查信息，直接根据woId查询AZ信息
                 * 2.在二干核查调度下发本地网核查，查询本地网核查的区域，找出AZ两端区域，查出本地两张单的核查汇总的反馈信息
                 */
                // 查询核查标准化全程B端字段
                List<Map<String,Object>> standBList = interfaceBoDao.queryCheckStandInfo(woId,"B");
                srvAttrInfoBList = mapParseJsonObject(standBList);
                Map<String,Object> orderInfo = wsd.qryOrdPsIdBySrvOrdId(srvOrderId);
                if(BasicCode.SECONDARY_CHECK.equals(MapUtils.getString(orderInfo,"PS_ID",""))){
                    List<Map<String,Object>> standAList = interfaceBoDao.queryCheckStandInfo(woId,"A");
                    List<Map<String,Object>> standZList = interfaceBoDao.queryCheckStandInfo(woId,"Z");
                    if(CollectionUtils.isEmpty(standAList)){
                        // todo 如果是空的话，查询本地核查单的核查汇总woId
                        List<Map<String,Object>> localWoList = interfaceBoDao.qryLocalWoInfo(srvOrderId);
                        if(!CollectionUtils.isEmpty(localWoList)){
                            for(Map<String,Object> temp :localWoList){
                                String woIdTemp = MapUtils.getString(temp,"WO_ID","");
                                if("Z".equals(MapUtils.getString(temp,"FLAG_TYPE",""))){
                                    standZList = interfaceBoDao.queryCheckStandInfo(woIdTemp,"Z");
                                } else {
                                    standAList = interfaceBoDao.queryCheckStandInfo(woIdTemp,"Z");
                                }
                            }
                        }
                    }
                    srvAttrInfoAList = mapParseJsonObject(standAList);
                    srvAttrInfoZList = mapParseJsonObject(standZList);
                }
            } else{
                // modify by wang.gang2 工建系统反馈结果
                List<Map<String, Object>> constructResult = insertOrderInfoDao.queryAttrInfo("ASSESS_RESULT", srvOrderId);
                // add by wang.gang2 核查单，拼装核查反馈信息
                srvAttrInfoAList = qrySrvAttrInfo(srvOrderId,"A",productType);
                srvAttrInfoBList= qrySrvAttrInfo(srvOrderId,"C",productType);
                srvAttrInfoZList = qrySrvAttrInfo(srvOrderId,"Z",productType);

                if(constructResult.size()>0 ){
                    String assessResult = MapUtils.getString(constructResult.get(0), "ASSESS_RESULT");
                    if(!StringUtils.isEmpty(assessResult)){
                        JSONObject assessGrpA = new JSONObject();
                        assessGrpA.put("ATTR_ACTION", "0");
                        assessGrpA.put("ATTR_CODE", "REC_50014");
                        assessGrpA.put("ATTR_VALUE", MapUtils.getString(constructResult.get(0), "ASSESS_RESULT"));
                        srvAttrInfoAList.add(assessGrpA);
                        srvAttrInfoZList.add(assessGrpA);
                    }
                }
            }
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
            JSONObject attr = new JSONObject();
            attr.put("ATTR_ACTION", "0");
            attr.put("ATTR_CODE", tmpKey);
            attr.put("ATTR_VALUE", MapUtils.getString(attrMap,tmpKey,""));
            list.add(attr);
        }
        return list;
    }
    /**
     * 政企精品网产品特殊处理
     * @param json
     * @return
     */
    private String otnJson(String json){
        /**
         REC_100079	报竣时间 REC_20012
         REC_100092	全程路由 CIRCUIT_ROUTE
         REC_100089 调单号 REC_20037
         REC_100088	电路代号 REC_20039
         REC_100085	全程竣工时间
         */
        String compDateRepJson = json.replaceAll("REC_20012","REC_100079");
        String cirRouteRepJson = compDateRepJson.replaceAll("CIRCUIT_ROUTE","REC_100092");
        String dispRepJson = cirRouteRepJson.replaceAll("REC_20037","REC_100089");
        String cirCodeRepJson = dispRepJson.replaceAll("REC_20039","REC_100088");
        String competeDateRepJson = cirCodeRepJson.replaceAll("REC_30004","REC_100085");
        return competeDateRepJson;

    }
    private List<JSONObject> qrySrvAttrInfo(String srvOrderId, String type,String productType) {
        // 属性编码信息
        List<JSONObject> srvAttrInfoList = new ArrayList<>();
        // 根据sev_ord_id查询wo_id和需要反馈的大字段
        Map<String,Object> finishInfoMap = interfaceBoDao.queryWoIdBySrvOrdId(srvOrderId);
        String woId = MapUtils.getString(finishInfoMap,"WO_ID","");

        // 查询通用信息 A端、Z端、全程（B）都需要
        if(!"80000466".equals(productType)){
            List<Map<String,Object>> generalList = interfaceBoDao.queryGeneralInfo(woId);
            srvAttrInfoList = mapParseJsonObject(generalList);
        }else if("80000466".equals(productType)){
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
            // 查询核查单反馈接口需要的A端信息
            attrlist = interfaceBoDao.getCheckBackInfo(woId,"Z");
        }
        else if("C".equals(type)){
            // 查询核查单反馈接口需要的A端信息
            attrlist = interfaceBoDao.checkSummary(woId,"C");
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

    // 根据指定长度生成纯数字的随机数
    public String createData(int length) {
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(rand.nextInt(10));
        }
        return sb.toString();
    }

}
