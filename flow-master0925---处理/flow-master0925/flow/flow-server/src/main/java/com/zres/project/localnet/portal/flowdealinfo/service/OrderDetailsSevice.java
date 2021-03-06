package com.zres.project.localnet.portal.flowdealinfo.service;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDetailsDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderQrySecondaryDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.listener.util.EnmuValueUtil;
import com.zres.project.localnet.portal.util.OrderTrackOperType;
import com.zres.project.localnet.portal.webservice.construct.ProgressQueryOrderIntf;
import com.ztesoft.res.frame.core.util.ListUtil;
import com.ztesoft.res.frame.core.util.MapUtil;
import net.sf.json.JSONArray;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
public class OrderDetailsSevice implements OrderDetailsServiceIntf
{

    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private OrderDetailsDao orderDetailsDao;
    @Autowired
    private OrderQrySecondaryDao orderQrySecondaryDao;
    @Autowired
    private OrderDetailsServiceIntf orderDetailsServiceIntf;
    @Autowired
    private ProgressQueryOrderIntf progressQueryOrderIntf;

    private List<String> rent = Arrays.asList(
            EnmuValueUtil.START_RENT,
            EnmuValueUtil.STOP_RENT,
            EnmuValueUtil.START_STOP_RENT,
            EnmuValueUtil.NOTICE_OF_RENT_CONFIRMATION,
            EnmuValueUtil.NOTICE_OF_RENT_CONFIRMATION_2
    );
    // 核查反馈的列
    private String CHECK_COLMON = "CONSTRUCT_SCHEME,ACCESS_ROOM,INVESTMENT_AMOUNT,CONSTRUCT_PERIOD,RES_SATISFY" +
            ",RES_PROVIDE_STAND,RES_PROVIDE_STAND_NAME" +
            ",BOARD_READY,BOARD_READY_NAME,BOARD_PERIOD,BOARD_AMOUNT,BOARD_TYPE,BOARD_MODEL" +
            ",TRANS_READY,TRANS_READY_NAME,TRANS_PERIOD,TRANS_AMOUNT,TRANS_TYPE,TRANS_TYPE_NAME,OTHER_TYPE,TRANS_MODEL" +
            ",OPTICAL_READY,OPTICAL_READY_NAME,OPTICAL_PERIOD,OPTICAL_AMOUNT" +
            ",CONSTRUCT_PERIOD_STAND,PROJECT_AMOUNT,PROJECT_OVERVIEW" +
            ",MUNICIPAL_APPROVAL,MUNICIPAL_APPROVAL_NAME,APPROVAL_PERIOD" +
            ",RES_DESC,PROPERTY_REDLINE,PROPERTY_REDLINE_NAME,PROPERTY_DESC,CUST_ROOM,CUST_ROOM_NAME,ACCESS_PROJECT_SCHEME" +
            ",RES_EXPLORER,RES_EXPLOR_CONTACT" +
            ",RES_HAVE,RES_HAVE_NAME,TOTAL_AMOUNT,LONGEST_PERIOD,UNABLE_RELOVE";

    /**
     * 定单详情页根据定单ID查询电路信息
     * @return
     */
    @Override
    public Map<String, Object> queryCircuitInfo(String srv_ord_id, String service_id) {
        List<Map<String, Object>> pubList = new ArrayList();
        List<Map<String, Object>> azList = new ArrayList();
        List<Map<String, Object>> peList = new ArrayList();
        Map<String, Object> retMap = new HashMap();
        try{
            List<Map<String, Object>> listPub = orderDetailsDao.queryCircuitInfo(srv_ord_id, service_id);
            List<Map<String, Object>> listA = orderDetailsDao.queryCircuitInfoAZ(srv_ord_id, service_id,"A");
            List<Map<String, Object>> listZ = orderDetailsDao.queryCircuitInfoAZ(srv_ord_id, service_id,"Z");
            List<Map<String, Object>> listPE = orderDetailsDao.queryCircuitInfoPE(srv_ord_id, service_id,"PE");
            List<Map<String, Object>> listCE = orderDetailsDao.queryCircuitInfoPE(srv_ord_id, service_id,"CE");
            if(!CollectionUtils.isEmpty(listPub)){
                int sizePub = listPub.size()%4;
                if(sizePub>0&&sizePub<4){
                    for(int i=0; i<4-sizePub; i++){
                        Map<String, Object> sizeAMap = new HashMap<String, Object>();
                        sizeAMap.put("ATTR_VALUE","");
                        sizeAMap.put("PROPERTY_NAME","");
                        listPub.add(sizeAMap);
                    }
                }
                pubList.addAll(listPub);
            }
            int sizeA = listA.size();
            int sizeZ = listZ.size();
            if(sizeA!=0||sizeZ!=0){
                if(sizeA>sizeZ){
                    if(sizeA%2 == 1){
                        Map<String, Object> sizeAMap = new HashMap<String, Object>();
                        sizeAMap.put("ATTR_VALUE","");
                        sizeAMap.put("PROPERTY_NAME","");
                        listA.add(sizeAMap);
                    }
                    for(int i=0; i<listA.size()-sizeZ; i++){
                        Map<String, Object> sizeAMap = new HashMap<String, Object>();
                        sizeAMap.put("ATTR_VALUE","");
                        sizeAMap.put("PROPERTY_NAME","");
                        listZ.add(sizeAMap);
                    }
                }else{
                    if(sizeZ%2 == 1){
                        Map<String, Object> sizeAMap = new HashMap<String, Object>();
                        sizeAMap.put("ATTR_VALUE","");
                        sizeAMap.put("PROPERTY_NAME","");
                        listZ.add(sizeAMap);
                    }
                    for(int i=0; i<listZ.size()-sizeA; i++){
                        Map<String, Object> sizeAMap = new HashMap<String, Object>();
                        sizeAMap.put("ATTR_VALUE","");
                        sizeAMap.put("PROPERTY_NAME","");
                        listA.add(sizeAMap);
                    }
                }
                for(int y=0; y<listA.size(); y+=2){
                    azList.add(listA.get(y));
                    azList.add(listA.get(y+1));
                    azList.add(listZ.get(y));
                    azList.add(listZ.get(y+1));
                }
            }
            int sizePE = listPE.size();
            int sizeCE = listCE.size();
            if(sizePE!=0||sizeCE!=0){
                if(sizePE>sizeCE){
                    if(sizePE%2 == 1){
                        Map<String, Object> sizeAMap = new HashMap<String, Object>();
                        sizeAMap.put("ATTR_VALUE","");
                        sizeAMap.put("PROPERTY_NAME","");
                        listPE.add(sizeAMap);
                    }
                    for(int i=0; i<listPE.size()-sizeCE; i++){
                        Map<String, Object> sizeAMap = new HashMap<String, Object>();
                        sizeAMap.put("ATTR_VALUE","");
                        sizeAMap.put("PROPERTY_NAME","");
                        listCE.add(sizeAMap);
                    }
                }else{
                    if(sizeCE%2 == 1){
                        Map<String, Object> sizeAMap = new HashMap<String, Object>();
                        sizeAMap.put("ATTR_VALUE","");
                        sizeAMap.put("PROPERTY_NAME","");
                        listCE.add(sizeAMap);
                    }
                    for(int i=0; i<listCE.size()-sizePE; i++){
                        Map<String, Object> sizeAMap = new HashMap<String, Object>();
                        sizeAMap.put("ATTR_VALUE","");
                        sizeAMap.put("PROPERTY_NAME","");
                        listPE.add(sizeAMap);
                    }
                }
                for(int y=0; y<listPE.size(); y+=2){
                    peList.add(listPE.get(y));
                    peList.add(listPE.get(y+1));
                    peList.add(listCE.get(y));
                    peList.add(listCE.get(y+1));
                }
            }
            retMap.put("AZInfo",azList);
            retMap.put("PEInfo",peList);
            retMap.put("otherInfo",pubList);
            retMap.put("success",true);
        }catch(Exception e){
            retMap.put("success",false);
            retMap.put("message",e.getMessage());
        }
        return retMap;
    }

    @Override
    public Map<String, Object> queryCircuitInfoGrid(Map<String, Object> params) {
        Map<String,Object> returnMap = new HashMap<String,Object>();
        Map<String,Object> paramsTemp = new HashMap<String,Object>();

        String srvOrdIds = MapUtils.getString(params, "srvOrdIds");
        String srvordId = MapUtils.getString(params, "srvordId");
        String woIds = MapUtils.getString(params, "woIds");
        String woId = MapUtils.getString(params, "woId");


        String cstOrdId = MapUtils.getString(params, "cstOrdId");//1
        String tacheId = MapUtils.getString(params, "tacheId");//2
        String reginonId = MapUtils.getString(params, "reginonId");//3
        String specialtyCode = MapUtils.getString(params, "specialtyCode");//4
        String dealUserId = MapUtils.getString(params, "dealUserId");//5
        String compUserId = MapUtils.getString(params, "compUserId");//6
        String dispType = MapUtils.getString(params, "dispType");//7
        String staffId = MapUtils.getString(params, "staffId");//8
        String dispTypeDetail = MapUtils.getString(params, "dispTypeDetail");//9
        String woState = MapUtils.getString(params, "woState");//10
        String dispObjTyeValue = MapUtils.getString(params, "dispObjTyeValue");//11
        String dispObjTye = MapUtils.getString(params, "dispObjTye");//12
        String orderIdSelect = MapUtils.getString(params, "orderIdSelect");//13 工单查询主电路orderId

        List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
        try{
            if(StringUtils.hasText(srvordId)){
                if(StringUtils.hasText(srvOrdIds)
                        &&srvOrdIds.indexOf(srvordId)>-1){
                    srvOrdIds += ","+srvordId;
                }else{
                    srvOrdIds = srvordId;
                }
            }
            if(StringUtils.hasText(srvOrdIds)){
                String[] splitStr = srvOrdIds.split(",");
                paramsTemp.put("srvOrdId",splitStr);
            }
            if(StringUtils.hasText(woId)){
                if(StringUtils.hasText(woIds)
                        &&woIds.indexOf(woId)>-1){
                    woIds += ","+woId;
                }else{
                    woIds = woId;
                }
            }
            if(StringUtils.hasText(woIds)){
                String[] splitStr = woIds.split(",");
                paramsTemp.put("woId",splitStr);
            }
            paramsTemp.put("cstOrdId",cstOrdId);
            paramsTemp.put("tacheId",tacheId);
            paramsTemp.put("reginonId",reginonId);
            paramsTemp.put("dealUserId",dealUserId);
            paramsTemp.put("compUserId",compUserId);
            paramsTemp.put("dispType",dispType);
            paramsTemp.put("staffId",staffId);
            paramsTemp.put("specialtyCode",specialtyCode);
            paramsTemp.put("dispTypeDetail",dispTypeDetail);
            paramsTemp.put("woState",woState);
            paramsTemp.put("dispObjTyeValue","personalValue".equals(dispObjTyeValue)?"":dispObjTyeValue);
            paramsTemp.put("dispObjTye",dispObjTye);
            paramsTemp.put("orderIdSelect",orderIdSelect);

            List<Map<String, Object>> list = orderDetailsDao.queryCircuitInfoGrid(paramsTemp);
//            for(Map<String, Object> paramsMap: list){
//                Map<String, Object> reMap = new HashMap<String, Object>();
//                String srv_ord_id = MapUtils.getString(paramsMap, "SRV_ORD_ID");
//                List<Map<String, Object>> mapsCireNum = orderDetailsDao.queryCircuitInfoBySrvIdY(srv_ord_id);
//                Map<String, Object> circuitMap = new HashMap<String, Object>();
//                for (Map<String, Object> circuitMapItem : mapsCireNum) {
//                    circuitMap.put(circuitMapItem.get("ATTR_VALUE_NAME").toString().toUpperCase(), circuitMapItem.get("ATTR_VALUE"));
//                }
//                reMap.putAll(paramsMap);
//                reMap.putAll(circuitMap);
//                returnList.add(reMap);
//            }
            if(!CollectionUtils.isEmpty(list)){
                returnList.addAll(list);
            }
            returnMap.put("data",returnList);
            returnMap.put("flag","1");
            returnMap.put("message","");

        }catch (Exception e){
            returnMap.put("data",returnList);
            returnMap.put("flag","0");
            returnMap.put("message",e.getMessage());
        }
        return returnMap;
    }

    /**
     * 获取异常通知列表
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> qryExceptionNoticeList(Map<String, Object> params) {
        Map<String, Object> returnMap = new HashMap<String, Object>();
        try {
            List<Map<String, Object>> returnList = orderDetailsDao.qryExceptionNoticeList(params);
            returnMap.put("data", returnList);
            returnMap.put("flag", "1");
            returnMap.put("message", "");

        } catch (Exception e) {
            returnMap.put("data", null);
            returnMap.put("flag", "0");
            returnMap.put("message", e.getMessage());
        }
        return returnMap;
    }

    /**
     * 定单详情页根据定单ID查询客户信息
     * @return
     */
    @Override
    public Map queryConsumerInfoByCustId(String cstOrdId) {
        Map map = orderDetailsDao.queryConsumerInfoByCustId(cstOrdId);
        return map;
    }

    /**
     * 定单详情页根据定单ID查询客户信息
     * @return
     */
    @Override
    public Map queryConsumerInfo(String cstOrdId) {
        Map map = orderDetailsDao.queryConsumerInfo(cstOrdId);
        return map;
    }

    /**
     * 定单详情页根据定单ID查询定单信息
     * @return
     */
    @Override
    public List<Map<String,Object>> queryOrderDeatilsInfo(String srv_ord_id) {
        Map<String, Object> params = new HashMap<String, Object>();
        String[] splitSrv = srv_ord_id.split(",");
        params.put("srv_ord_id",splitSrv);
        List<Map<String,Object>> map = orderDetailsDao.queryOrderDeatilsInfo(params);
        return map;
    }


    /**
     * 根据定单Id查询附件信息
     * @param params
     * @return
     */
    @Override
    public List<Map<String, Object>> queryAttachInfo(Map<String, Object> params) {
        //判断根据order_id当前环节
        String orderId = MapUtils.getString(params, "orderId");
        Map<String, Object> tacheMap = orderDetailsDao.queryTacheInfoByOrderId(orderId);
        List<Map<String, Object>> list = new ArrayList();
        if (tacheMap != null ){
            if ("CROSS_WHOLE_COURDER_TEST".equals(MapUtils.getString(tacheMap, "TACHE_CODE"))){ //跨域全程调测
                list = orderDetailsDao.queryAttachInfo2(params);
            }else{
                list = orderDetailsDao.queryAttachInfo(params);
            }
        }
        return list;
    }
    /**
     * 根据调单Id查询附件信息
     * @param dispatchOrderId
     * @return
     */
    @Override
    public List<Map<String, Object>> queryDispatchAttachInfo(String dispatchOrderId) {
        List<Map<String, Object>> list = orderDetailsDao.queryDispatchAttachInfo(dispatchOrderId);
        return list;
    }

    /**
     * 根据定单ID查询日志信息
     * @param orderId
     * @return
     */
    @Override
    public List<Map<String, Object>> queryLogInfo(String orderId) {
        List<Map<String, Object>> list = orderDetailsDao.queryLogInfo(orderId);
        return list;
    }

    @Override
    public List<Map<String, Object>> querySecTaskInfo(String orderId) {
        List<Map<String, Object>> resList = new ArrayList<Map<String, Object>>();
        Map<String, Object> parentOrder = orderDealDao.getParentOrder(orderId);
        if(MapUtils.isNotEmpty(parentOrder) && !"".equals(MapUtils.getString(parentOrder, "PARENTORDERID"))){
            orderId = MapUtils.getString(parentOrder, "PARENTORDERID");
        }
        Map<String, Object> ifFromSecondaryMap = orderQrySecondaryDao.qryIfFromSecondary(orderId);
        if(!MapUtils.isEmpty(ifFromSecondaryMap)){
            String parentOrderId = MapUtils.getString(ifFromSecondaryMap, "PARENT_ORDER_ID");
            resList = orderDetailsServiceIntf.queryTaskInfo(parentOrderId);
        }
        return resList;
    }

    @Override
    public List<Map<String, Object>> queryLocalTaskInfo(String orderId) {
        List<Map<String, Object>> resList = new ArrayList<Map<String, Object>>();
        Map<String, Object> parentOrder = orderDealDao.getParentOrder(orderId);
        if(MapUtils.isNotEmpty(parentOrder) && !"".equals(MapUtils.getString(parentOrder, "PARENTORDERID"))){
            orderId = MapUtils.getString(parentOrder, "PARENTORDERID");
        }
        Map<String, Object> ifFromSecondaryMap = orderQrySecondaryDao.qryIfFromSecondary(orderId);
        if(MapUtils.isEmpty(ifFromSecondaryMap)){
            resList = orderDetailsServiceIntf.queryTaskInfo(orderId);
        }else {
            String parentOrderId = MapUtils.getString(ifFromSecondaryMap, "PARENT_ORDER_ID");
            resList = orderDetailsServiceIntf.querySecToLocalTaskInfo(parentOrderId);
            for (Map<String, Object> localOrder : resList){
                String ifMainOrg = MapUtils.getString(localOrder, "IFMAINORG");
                String localOrderId = MapUtils.getString(localOrder, "ORDERID");
                //查询当前处理的环节
                String orderState = MapUtils.getString(localOrder, "ORDER_STATE");
                if (OrderTrackOperType.ORDER_STATE_2.equals(orderState)){ //订单未结束时
                    //二干发本地调度单子，本地测试环节走完，非主调，显示成待起租，主调显示成等待全程调测
                    if("1".equals(ifMainOrg)){ //非主调
                        Map<String, Object> localTestTacheWoOrderState = orderDetailsDao.qryTacheWoOrderState(localOrderId, EnmuValueUtil.LOCAL_TEST);
                        if (MapUtils.isNotEmpty(localTestTacheWoOrderState)){
                            if (OrderTrackOperType.WO_ORDER_STATE_4.equals(MapUtils.getString(localTestTacheWoOrderState, "WO_STATE"))){
                                localOrder.put("ORDERSTATE", "待起租");
                            }
                        }
                    }else if("0".equals(ifMainOrg)){ //主调
                        Map<String, Object> localTestTacheWoOrderState = orderDetailsDao.qryTacheWoOrderState(localOrderId, EnmuValueUtil.CROSS_WHOLE_COURDER_TEST);
                        if (MapUtils.isNotEmpty(localTestTacheWoOrderState)){
                            if (OrderTrackOperType.WO_ORDER_STATE_4.equals(MapUtils.getString(localTestTacheWoOrderState, "WO_STATE"))){
                                localOrder.put("ORDERSTATE", "待起租");
                            }else if (OrderTrackOperType.WO_ORDER_STATE_2.equals(MapUtils.getString(localTestTacheWoOrderState, "WO_STATE"))){
                                localOrder.put("ORDERSTATE", "等待全程调测");
                            }
                        }
                    }else if ("-1".equals(ifMainOrg)){ //核查
                        continue;
                    }
                }
            }
        }
        return resList;
    }

    @Override
    public boolean ifFromSecond(String orderId) {
        boolean flag = false;
        Map<String, Object> parentOrder = orderDealDao.getParentOrder(orderId);
        if(MapUtils.isNotEmpty(parentOrder) && !"".equals(MapUtils.getString(parentOrder, "PARENTORDERID"))){
            orderId = MapUtils.getString(parentOrder, "PARENTORDERID");
        }
        Map<String, Object> ifFromSecondaryMap = orderQrySecondaryDao.qryIfFromSecondary(orderId);
        if(!MapUtils.isEmpty(ifFromSecondaryMap)){
            flag = true;
        }
        return flag;
    }

    /**
     * 根据定单ID查询任务信息
     * @param orderId
     * @return
     */
    @Override
    public List<Map<String, Object>> queryTaskInfo(String orderId) {
        List<Map<String, Object>> newList = orderDetailsDao.queryTaskInfo(orderId);
        //查询单子来源
        String orderSource = orderQrySecondaryDao.qrySrvOrderSource(orderId);
        if (StringUtils.isEmpty(orderSource) || "NULL".equals(orderSource)){
            orderSource = orderQrySecondaryDao.qrySrvOrderSourceFromSec(orderId);
        }
        if (BasicCode.JIKE.equals(orderSource)){
            orderSource = "集客";
        }else if (BasicCode.ONEDRY.equals(orderSource)){
            orderSource = "一干";
        }else if(BasicCode.SECONDARY.equals(orderSource) || BasicCode.LOCALBUILD.equals(orderSource)){
            orderSource = "";
        }
        try {
            for(int i = 0; i < newList.size(); i++) {
                String dealDate = newList.get(i).get("STATE_DATE").toString()==null?"":newList.get(i).get("STATE_DATE").toString(); //处理时间
                String createDate = newList.get(i).get("CREATE_DATE").toString()==null?"":newList.get(i).get("CREATE_DATE").toString(); //创建时间
                String alarmDate = newList.get(i).get("ALARM_DATE").toString()==null?"":newList.get(i).get("ALARM_DATE").toString(); //预警时间
                String reqFinDate = newList.get(i).get("REQ_FIN_DATE").toString()==null?"":newList.get(i).get("REQ_FIN_DATE").toString(); //要求完成时间
                String pubDateName = newList.get(i).get("ORDERSTATE").toString()==null?"":newList.get(i).get("ORDERSTATE").toString(); //任务状态
                DateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Long dealTime = 0L;
                Long createTime = 0L;
                if(dealDate!=""){
                    dealTime = sf.parse(dealDate).getTime();
                }
                if(createDate!=""){
                    createTime = sf.parse(createDate).getTime();
                }
                Long reqFinTime = 0L;
                if(reqFinDate!=""){
                    reqFinTime = sf.parse(reqFinDate).getTime();
                }

                long between = (dealTime-createTime)/1000; //除以1000是为了转换成秒
                long minute = between/60 ; //耗时
                if("已完成".equals(pubDateName)){
                    newList.get(i).put("MINUTE", minute);
                    System.out.println("minute---"+minute);
                }else{
                    newList.get(i).put("MINUTE", "0");
                }

                Long alarmTime = 0L;

                if(alarmDate!=""){
                    alarmTime = sf.parse(alarmDate).getTime();
                }

                Date nowTime = new Date();
                long between2 = (nowTime.getTime() - reqFinTime)/1000; //超时
                long between3 = (nowTime.getTime() - alarmTime)/1000; //预警
                //long  hour=(between2/(60*60));
                if(between2 > 0) {
                    //newList.get(i).put("EXCEEDTIME", hour);
                    newList.get(i).put("EXCEEDTYPE", "超时");
                }
                else if (between3 > 0) {
                    //newList.get(i).put("EXCEEDTIME", "0");
                    newList.get(i).put("EXCEEDTYPE", "预警");
                }
                else {
                    //newList.get(i).put("EXCEEDTIME", "0");
                    newList.get(i).put("EXCEEDTYPE", "正常");
                }
                if (!"起租".equals(newList.get(i).get("TACHENAME").toString()) && "290000002".equals(newList.get(i).get("PUB_DATE_ID").toString()) && (null == newList.get(i).get("DEAL_USER_ID").toString() || "".equals(newList.get(i).get("DEAL_USER_ID").toString()))){
                    newList.get(i).put("ORDERSTATE", "待签收");
                }
                if ("290000110".equals(newList.get(i).get("PUB_DATE_ID").toString())){
                    newList.get(i).put("ORDERSTATE", "已派发调单");
                }
                if (OrderTrackOperType.WO_ORDER_STATE_18.equals(newList.get(i).get("PUB_DATE_ID").toString())){
                    newList.get(i).put("ORDERSTATE", "待回单");
                }
                if ("起租".equals(newList.get(i).get("TACHENAME").toString()) && "290000002".equals(newList.get(i).get("PUB_DATE_ID").toString())){
                    newList.get(i).put("ORGNAME", "");
                    newList.get(i).put("USERJOBNAME", "");
                    newList.get(i).put("USERNAME", "");
                    newList.get(i).put("ORDERSTATE", "待起租");
                    newList.get(i).put("TRACKCONTENT", "");
                    newList.get(i).put("DEAL_DATE", "");
                    newList.get(i).put("STATE_DATE", "");
                    newList.get(i).put("MINUTE", "");
                    //newList.get(i).put("EXCEEDTIME", "");
                    newList.get(i).put("ALARM_DATE", "");
                    newList.get(i).put("EXCEEDTYPE", "");
                }

                if(EnmuValueUtil.SUMMARY_OF_COMPLETION.equals(MapUtils.getString(newList.get(i), "TACHE_CODE"))
                        || EnmuValueUtil.SUMMARY_OF_COMPLETION_2.equals(MapUtils.getString(newList.get(i), "TACHE_CODE"))
                        || EnmuValueUtil.COMPLETE_CONFIRM.equals(MapUtils.getString(newList.get(i), "TACHE_CODE"))){
                    /**
                     * 完工确认，完工汇总环节，查询页面中选择的【报竣时间】，返回给页面显示
                     */
                    Map<String, Object> manualFullCompleteTime = orderDealDao.qryManualFullCompleteTime(orderId);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                    if (!StringUtils.isEmpty(MapUtils.getString(manualFullCompleteTime, "ATTR_VALUE"))){
                        Date finishDate = dateFormat.parse(MapUtils.getString(manualFullCompleteTime, "ATTR_VALUE"));
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置日期格式
                        String finishDateStr = df.format(finishDate);
                        newList.get(i).put("FINISH_DATE", finishDateStr);
                    }
                }else if (EnmuValueUtil.RENT.equals(MapUtils.getString(newList.get(i), "TACHE_CODE"))
                        || EnmuValueUtil.START_RENT.equals(MapUtils.getString(newList.get(i), "TACHE_CODE"))
                        || EnmuValueUtil.STOP_RENT.equals(MapUtils.getString(newList.get(i), "TACHE_CODE"))
                        || EnmuValueUtil.START_STOP_RENT.equals(MapUtils.getString(newList.get(i), "TACHE_CODE"))
                        || EnmuValueUtil.NOTICE_OF_RENT_CONFIRMATION.equals(MapUtils.getString(newList.get(i), "TACHE_CODE"))
                        || EnmuValueUtil.NOTICE_OF_RENT_CONFIRMATION_2.equals(MapUtils.getString(newList.get(i), "TACHE_CODE"))){
                    /**
                     * 起租，止租，起止租，起租确认通知环节添加单子来源tacheName
                     */
                    newList.get(i).put("TASKNAME", orderSource + MapUtils.getString(newList.get(i), "TASKNAME"));
                }

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return newList;
    }

    @Override
    public List<Map<String, Object>> querySecToLocalTaskInfo(String orderId) {
        List<Map<String, Object>> newList = orderDetailsDao.querySecToLocalTaskInfo(orderId);
        return newList;
    }

    /**
     * 查询阶段性处理意见信息
     * @param srvordId
     * @return
     */
    @Override
    public List<Map<String, Object>> queryIdeaInfoBySrvOrdId(String srvordId) {
        List<Map<String, Object>> list = orderDetailsDao.queryIdeaInfoBySrvOrdId(srvordId);
        return list;
    }

    /**
     * 查询关联主/子单信息
     * @param orderId
     * @return
     */
    @Override
    public List<Map<String, Object>> queryRelevanceOrderInfo(String orderId) {
        List<Map<String, Object>> list = orderDetailsDao.queryRelevanceOrderInfo(orderId);
        return list;
    }

    /**
     * 查询预警超时信息
     * @param orderId
     * @return
     */
    @Override
    public List<Map<String, Object>> queryWarningInfo(String orderId) {
        List<Map<String, Object>> list = orderDetailsDao.queryWarningInfo(orderId);
        return list;
    }

    /**
     * 查询调单信息
     * @param cstOrdId
     * @return
     */
    @Override
    public List<Map<String, Object>> queryDispatchOrderInfo(String cstOrdId) {
        List<Map<String, Object>> list = orderDetailsDao.queryDispatchOrderInfo(cstOrdId);
        return list;
    }
    /**
     * 查询调单信息
     * @param  
     * @return
     */
    @Override
    public List<Map<String, Object>> queryDispatchOrderInfo(Map<String, Object> params) {
        //根据CST_ORD_ID查询电路表中的所有调单ID
        String cstOrdId = MapUtils.getString(params, "cstOrdId");
        //根据CST_ORD_ID查询系统来源
        List<Map<String, Object>> systemList = orderDetailsDao.querySystemSourceByCstOrdId(cstOrdId);
        int srvListSize = 0;
        List<Map<String, Object>> srvList = null;
        if (BasicCode.SECOND.equals(systemList.get(0).get("SYSTEM_RESOURCE"))){
            //查询电路信息表中的调单ID
            srvList = orderDetailsDao.queryDispatchIdByCstOrdId(cstOrdId);
            if (srvList != null && srvList.size() > 0){
                srvListSize = srvList.size();
            }
        }
        //查询是否有一干调单
        List<Map<String, Object>> oneDryList = orderDetailsDao.queryDispatchIdFromOneDryByCstOrdId(cstOrdId);
        if (oneDryList != null && oneDryList.size() > 0){
            srvListSize = srvListSize + oneDryList.size();
        }


        //Map<String,Object> returnMap = new HashMap<String,Object>();
        Map<String,Object> paramsTemp = new HashMap<String,Object>();

        String srvOrdIds = MapUtils.getString(params, "srvOrdIds");
        String srvordId = MapUtils.getString(params, "srvordId");

//        String cstOrdId = MapUtils.getString(params, "cstOrdId");//1
        String tacheId = MapUtils.getString(params, "tacheId");//2
        String reginonId = MapUtils.getString(params, "reginonId");//3
        String specialtyCode = MapUtils.getString(params, "specialtyCode");//4
        String dealUserId = MapUtils.getString(params, "dealUserId");//5
        String compUserId = MapUtils.getString(params, "compUserId");//6
        String dispType = MapUtils.getString(params, "dispType");//7
        String staffId = MapUtils.getString(params, "staffId");//8
        String dispTypeDetail = MapUtils.getString(params, "dispTypeDetail");//9
        String woState = MapUtils.getString(params, "woState");//10
        String dispObjTyeValue = MapUtils.getString(params, "dispObjTyeValue");//11
        String dispObjTye = MapUtils.getString(params, "dispObjTye");//12
        String orderIdSelect = MapUtils.getString(params, "orderIdSelect");//13 工单查询主电路orderId

        //List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
            if(StringUtils.hasText(srvordId)){
                if (StringUtils.hasText(srvOrdIds)
                        && srvOrdIds.indexOf(srvordId) > -1) {
                    srvOrdIds += "," + srvordId;
                }else{
                    srvOrdIds = srvordId;
                }
            }
            if(StringUtils.hasText(srvOrdIds)){
                String[] splitStr = srvOrdIds.split(",");
                paramsTemp.put("srvOrdId", splitStr);
            }
            paramsTemp.put("cstOrdId", cstOrdId);
            paramsTemp.put("tacheId", tacheId);
            paramsTemp.put("reginonId", reginonId);
            paramsTemp.put("dealUserId", dealUserId);
            paramsTemp.put("compUserId", compUserId);
            paramsTemp.put("dispType", dispType);
            paramsTemp.put("staffId", staffId);
            paramsTemp.put("specialtyCode", specialtyCode);
            paramsTemp.put("dispTypeDetail", dispTypeDetail);
            paramsTemp.put("woState", woState);
            paramsTemp.put("dispObjTyeValue", "personalValue".equals(dispObjTyeValue) ? "" : dispObjTyeValue);
            paramsTemp.put("dispObjTye", dispObjTye);
            paramsTemp.put("orderIdSelect", orderIdSelect);

            List<Map<String, Object>> dispatchOrderIdList = orderDetailsDao.queryCirDispatchOrderIds(paramsTemp);
           // String dispatchOrderIds = MapUtils.getString(dispatchOrderIdMap, "DISPATCH_ORDER_ID");
        String[] strings = new String[dispatchOrderIdList.size() + srvListSize];
        for (int i = 0; i < dispatchOrderIdList.size(); i++) {
            strings[i] =  MapUtils.getString(dispatchOrderIdList.get(i),"DISPATCH_ORDER_ID") ;
//            paramsTemp.put("dispatchOrderIds", strings);
        }
        if(srvList != null && srvList.size() > 0){
            for (int i = dispatchOrderIdList.size(); i < srvList.size() + dispatchOrderIdList.size(); i++){
                strings[i] = MapUtils.getString(srvList.get(i - dispatchOrderIdList.size()), "DISPATCH_ORDER_ID");
            }
            if (oneDryList != null && oneDryList.size() > 0){
                for (int i = srvList.size() + dispatchOrderIdList.size(); i < dispatchOrderIdList.size() + srvListSize; i++){
                    strings[i] = MapUtils.getString(oneDryList.get(i - (srvList.size() + dispatchOrderIdList.size()) ), "DISPATCH_ORDER_ID");
                }
            }
        }else{
            if (oneDryList != null && oneDryList.size() > 0){
                for (int i = dispatchOrderIdList.size(); i < dispatchOrderIdList.size() + srvListSize; i++){
                    strings[i] = MapUtils.getString(oneDryList.get(i - dispatchOrderIdList.size() ), "DISPATCH_ORDER_ID");
                }
            }
        }
        paramsTemp.put("dispatchOrderIds", strings);
        List<Map<String, Object>> list = orderDetailsDao.queryDispatchOrderInfoByIds(paramsTemp);
        return list;
    }

    @Override
    public List<Map<String, Object>> queryDispatchOrderInfoByCstOrdId(Map<String, Object> param) {
        String cstOrdId = MapUtils.getString(param, "cstOrdId");
        List<Map<String, Object>> list = orderDetailsDao.queryDispatchOrderInfoByCstOrdId(cstOrdId);
        return list;
    }

    @Override
    public List<Map<String, Object>> queryDispatchOrderInfoByDispatchId(Map<String, Object> param) {
        String dispatchId = MapUtils.getString(param, "dispatchId");
        List<Map<String, Object>> list = orderDetailsDao.queryDispatchOrderInfoByDispatchId(dispatchId);
        return list;
    }

    /**
     * 查询一干的资源信息
     * @param srvOrdId
     * @return
     */
    @Override
    public List<Map<String, Object>> queryResourceOrderInfoW(String srvOrdId) {
        Map<String, Object> paramMap = new HashMap<>();
        int l = 1;
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        List<String> srvOrdIdList = new ArrayList<String>();
        srvOrdIdList.add(srvOrdId);
        Map<String, Object> param = new HashMap<>();
        param.put("srvOrdId", srvOrdId);
        Map<String, Object> belongSysMap = orderQrySecondaryDao.qrySrvOrderBelongSys(param);
        //String systemResource = MapUtils.getString(belongSysMap, "SYSTEM_RESOURCE");
        String RESOURCES = MapUtils.getString(belongSysMap, "RESOURCES");
        if(BasicCode.ONEDRY.equals(RESOURCES)){ //BasicCode.LOCAL.equals(systemResource) &&
            String[] strArrs = new String[l];
            for (int i = 0; i < srvOrdIdList.size(); i++) {
                strArrs[i] = srvOrdIdList.get(i);
            }
            paramMap.put("srvOrdId",strArrs);
            list = orderDetailsDao.queryResourceOrderInfoResOne(paramMap);
            if (list.size() < 1){
                String[] strArr = new String[2];
                //如果当前单查询不到资源信息，则查询最近与他关联的单子的资源信息
                Map<String, Object> map = orderDetailsDao.queryRelevanceSrvOrdId(srvOrdId,null);
                if (map != null && !map.isEmpty()){
                    //如果最近的关联单为核查单，则查询出核查单的资源信息和他最近新开的单资源信息
                    if ("102".equals(map.get("ORDER_TYPE"))){
                        Map<String, Object> map2 = orderDetailsDao.queryRelevanceSrvOrdId(srvOrdId,"101");
                        if (map2 != null && !map2.isEmpty()){
                            strArr[0] = map.get("SRV_ORD_ID").toString();
                            strArr[1] = map2.get("SRV_ORD_ID").toString();
                            paramMap.put("srvOrdId",strArr);
                            list = orderDetailsDao.queryResourceOrderInfoBySrvOrdIdResOne(paramMap);
                        }else{
                            strArr[0] = map.get("SRV_ORD_ID").toString();
                            paramMap.put("srvOrdId",strArr);
                            list = orderDetailsDao.queryResourceOrderInfoBySrvOrdIdResOne(paramMap);
                        }
                    }else{
                        strArr[0] = map.get("SRV_ORD_ID").toString();
                        paramMap.put("srvOrdId",strArr);
                        list = orderDetailsDao.queryResourceOrderInfoBySrvOrdIdResOne(paramMap);
                    }
                }

            }

        }
        return list;
    }

    /**
     * 查询二干资源信息
     * @param srvOrdId
     * @return
     */
    @Override
    public List<Map<String, Object>> queryResourceOrderInfo(String srvOrdId) {
        Map<String, Object> paramMap = new HashMap<>();
        int l = 1;
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        List<String> srvOrdIdList = new ArrayList<String>();
        srvOrdIdList.add(srvOrdId);
        Map<String, Object> param = new HashMap<>();
        param.put("srvOrdId", srvOrdId);
        Map<String, Object> belongSysMap = orderQrySecondaryDao.qrySrvOrderBelongSys(param);
        String systemResource = MapUtils.getString(belongSysMap, "SYSTEM_RESOURCE");
        if (BasicCode.SECOND.equals(systemResource)){
//            List<String> relateInfoIdList = orderQrySecondaryDao.qryRelateInfoId(srvOrdId);
//            srvOrdIdList.addAll(relateInfoIdList);
            l = srvOrdIdList.size();
            String[] strArrs = new String[l];
            for (int i = 0; i < srvOrdIdList.size(); i++) {
                strArrs[i] = srvOrdIdList.get(i);
            }
            paramMap.put("srvOrdId",strArrs);
            list = orderDetailsDao.queryResourceOrderInfo(paramMap);
            if (list.size() < 1){
                String[] strArr = new String[2];
                //如果当前单查询不到资源信息，则查询最近与他关联的单子的资源信息
                Map<String, Object> map = orderDetailsDao.queryRelevanceSrvOrdId(srvOrdId,null);
                if (map != null && !map.isEmpty()){
                    //如果最近的关联单为核查单，则查询出核查单的资源信息和他最近新开的单资源信息
                    if ("102".equals(map.get("ORDER_TYPE"))){
                        Map<String, Object> map2 = orderDetailsDao.queryRelevanceSrvOrdId(srvOrdId,"101");
                        if (map2 != null && !map2.isEmpty()){
                            strArr[0] = map.get("SRV_ORD_ID").toString();
                            strArr[1] = map2.get("SRV_ORD_ID").toString();
                            paramMap.put("srvOrdId",strArr);
                            list = orderDetailsDao.queryResourceOrderInfoBySrvOrdId(paramMap);
                        }else{
                            strArr[0] = map.get("SRV_ORD_ID").toString();
                            paramMap.put("srvOrdId",strArr);
                            list = orderDetailsDao.queryResourceOrderInfoBySrvOrdId(paramMap);
                        }
                    }else{
                        strArr[0] = map.get("SRV_ORD_ID").toString();
                        paramMap.put("srvOrdId",strArr);
                        list = orderDetailsDao.queryResourceOrderInfoBySrvOrdId(paramMap);
                    }
                }
            }

        }
        return list;
    }

    /**
     * 查询本地资源信息
     * @param srvOrdId
     * @return
     */
    @Override
    public List<Map<String, Object>> queryResourceOrderInfoY(String srvOrdId) {
        /**
         * 这里的srvOrdId是业务电路表中的主键
         */
        Map<String, Object> paramMap = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> param = new HashMap<>();
        param.put("srvOrdId", srvOrdId);
        //判断单子来源和所属系统
        Map<String, Object> belongSysMap = orderQrySecondaryDao.qrySrvOrderBelongSys(param);
        String systemResource = MapUtils.getString(belongSysMap, "SYSTEM_RESOURCE");
        String RESOURCES = MapUtils.getString(belongSysMap, "RESOURCES");
        if(BasicCode.SECOND.equals(systemResource)
                && (BasicCode.ONEDRY.equals(RESOURCES) ||BasicCode.SECONDARY.equals(RESOURCES) ||BasicCode.JIKE.equals(RESOURCES))){
            //二干下发给本地的单子
            List<String> srvOrdIdList = new ArrayList<String>();
            //查询下发给本地网单子的srvordid
            List<String> relateInfoIdList = orderQrySecondaryDao.qryRelateInfoId(srvOrdId);
            if(!CollectionUtils.isEmpty(relateInfoIdList)){
                srvOrdIdList.addAll(relateInfoIdList);
            }
            int l = srvOrdIdList.size();
            String[] strArrs = new String[l];
            for (int i = 0; i < srvOrdIdList.size(); i++) {
                strArrs[i] = srvOrdIdList.get(i);
            }
            paramMap.put("srvOrdId",strArrs);
            list = orderDetailsDao.queryResourceOrderInfoLocal(paramMap);
            if (list.size() < 1){
                //如果当前单查询不到资源信息，则查询最近与他关联的单子的资源信息
                Map<String, Object> map = orderDetailsDao.queryRelevanceSrvOrdId(srvOrdId,null);
                //查询下发本地的srvordid
                List<String> relateInfoIdLastList = orderQrySecondaryDao.qryRelateInfoId(MapUtils.getString(map, "SRV_ORD_ID"));
                if (map != null && !map.isEmpty()){
                    if(!ListUtil.isEmpty(relateInfoIdLastList)){
                        //如果最近的关联单为核查单，则查询出核查单的资源信息和他最近新开的单资源信息
                        if ("102".equals(map.get("ORDER_TYPE"))){
                            Map<String, Object> map2 = orderDetailsDao.queryRelevanceSrvOrdId(srvOrdId,"101");
                            //查询下发本地的srvordid
                            List<String> relateInfoIdLastList_2 = orderQrySecondaryDao.qryRelateInfoId(MapUtils.getString(map2, "SRV_ORD_ID"));
                            List<String> allRelateInfoIdlist = new ArrayList<>();
                            if (map2 != null && !map2.isEmpty()){
                                allRelateInfoIdlist.addAll(relateInfoIdLastList);
                                allRelateInfoIdlist.addAll(relateInfoIdLastList_2);
                                String[] strArr = new String[allRelateInfoIdlist.size()];
                                for (int i = 0; i < allRelateInfoIdlist.size(); i++) {
                                    strArr[i] = allRelateInfoIdlist.get(i);
                                }
                                paramMap.put("srvOrdId",strArr);
                                list = orderDetailsDao.queryResourceOrderInfoBySrvOrdId(paramMap);
                            }else{
                                String[] strArr = new String[relateInfoIdLastList.size()];
                                for (int i = 0; i < relateInfoIdLastList.size(); i++) {
                                    strArr[i] = relateInfoIdLastList.get(i);
                                }
                                paramMap.put("srvOrdId",strArr);
                                list = orderDetailsDao.queryResourceOrderInfoBySrvOrdId(paramMap);
                            }
                        }else{
                            String[] strArr = new String[relateInfoIdLastList.size()];
                            for (int i = 0; i < relateInfoIdLastList.size(); i++) {
                                strArr[i] = relateInfoIdLastList.get(i);
                            }
                            paramMap.put("srvOrdId",strArr);
                            list = orderDetailsDao.queryResourceOrderInfoBySrvOrdId(paramMap);
                        }
                    }
                }
            }
        }else{
            String[] strArrs = new String[1];
            strArrs[0] = srvOrdId;
            paramMap.put("srvOrdId",strArrs);
            /*if(BasicCode.LOCAL.equals(systemResource) && BasicCode.ONEDRY.equals(RESOURCES)){
                //一干直接下给本地
                paramMap.put("onedryResID","onedry");
                //这里不用这个字段取判断了直接不查询出来一干的
            }*/
            list = orderDetailsDao.queryResourceOrderInfo(paramMap);
            if (list.size() < 1){
                String[] strArr = new String[2];
                //如果当前单查询不到资源信息，则查询最近与他关联的单子的资源信息
                Map<String, Object> map = orderDetailsDao.queryRelevanceSrvOrdId(srvOrdId,null);
                if (map != null && !map.isEmpty()){
                    //如果最近的关联单为核查单，则查询出核查单的资源信息和他最近新开的单资源信息
                    if ("102".equals(map.get("ORDER_TYPE"))){
                        Map<String, Object> map2 = orderDetailsDao.queryRelevanceSrvOrdId(srvOrdId,"101");
                        if (map2 != null && !map2.isEmpty()){
                            strArr[0] = map.get("SRV_ORD_ID").toString();
                            strArr[1] = map2.get("SRV_ORD_ID").toString();
                            paramMap.put("srvOrdId",strArr);
                            list = orderDetailsDao.queryResourceOrderInfoBySrvOrdId(paramMap);
                        }else{
                            strArr[0] = map.get("SRV_ORD_ID").toString();
                            paramMap.put("srvOrdId",strArr);
                            list = orderDetailsDao.queryResourceOrderInfoBySrvOrdId(paramMap);
                        }
                    }else{
                        strArr[0] = map.get("SRV_ORD_ID").toString();
                        paramMap.put("srvOrdId",strArr);
                        list = orderDetailsDao.queryResourceOrderInfoBySrvOrdId(paramMap);
                    }
                }
            }
        }
        return list;
    }

    /**
     * 查询反馈信息
     * @param srvOrdId
     * @return
     */
    @Override
    public List<Map<String, Object>> queryFeedbackInfo(String srvOrdId) {
        //判断是否是开通单，如果是开通单，查找上次调度的核查单信息，
        Map<String,Object> checkOrderLast = orderDetailsDao.qryLastCheck(srvOrdId);
        if(checkOrderLast!=null&&checkOrderLast.size()>0){
            String temp = MapUtils.getString(checkOrderLast,"SRV_ORD_ID","");
            if(!"".equals(temp)){
                srvOrdId = temp;
            }
        }
        // 查询全部反馈信息
        List<Map<String, Object>> checkList = orderDetailsDao.queryFeedbackInfo(srvOrdId);
        // 反馈结果
        List<Map<String, Object>> retList = new ArrayList<>();
        if (checkList.size() > 0){
            for (Map map : checkList){
                // 判断AZ端息是否全部为空
                Map<String,Object> flagMap = checkInfoIsNull(map);
                boolean flagA = MapUtils.getBoolean(flagMap,"flagA",false);
                boolean flagZ = MapUtils.getBoolean(flagMap,"flagZ",false);
                boolean flagL = MapUtils.getBoolean(flagMap,"flagL",false);
                String tacheId = MapUtils.getString(map,"TACHE_ID","");
                if (flagA) {// 如果A端信息有值
                    Set<String> tacheLocalSet = new HashSet<>();
                    tacheLocalSet.add(BasicCode.CHECK_TOTAL);
                    tacheLocalSet.add(BasicCode.INVESTMENT_ESTIMATION);
                    tacheLocalSet.add(BasicCode.CHECK_DISPATCH);
                    // 如果是二干下发本地的单子，本地网的核查信息全部存放在Z端,A端信息不需要展示
                    if(!"".equals(MapUtils.getString(map,"CHILD_ORDER_ID")) && tacheLocalSet.contains(tacheId)){
                    } else {
                        Map<String,Object> temp = setMapColum(map,"A");
                        // 如果是核查调度环节，更改对应区域
                        if(BasicCode.CHECK_DISPATCH.equals(tacheId) || BasicCode.CHECK_SUMMARY.equals(tacheId)){
                            temp.put("AREAID",MapUtils.getString(map,"AREAA"));
                        }
                        retList.add(temp);
                    }
                }
                if(flagZ){// 如果Z端信息有值
                     Map<String,Object> temp = setMapColum(map,"Z");
                    // 如果是核查调度环节，更改对应区域
                    if( ("".equals(MapUtils.getString(map,"CHILD_ORDER_ID")) && BasicCode.CHECK_DISPATCH.equals(tacheId))
                            || BasicCode.CHECK_SUMMARY.equals(tacheId)){
                        temp.put("AREAID",MapUtils.getString(map,"AREAZ"));
                    }
                    retList.add(temp);
                }
                if(flagL){
                    Map<String,Object> temp = setMapColum(map,"L");
                    // 如果是二干核查汇总环节，更改对应区域
                    if (BasicCode.CHECK_SUMMARY.equals(tacheId)){
                        temp.put("AREAID","本地网");
                    }
                    retList.add(temp);
                }
            }
        }
        //TODO 把list的元素按照时间排序 ren.jiahang in 20190718
        Collections.sort(retList, new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                String create_date = MapUtil.getString(o1,"CREATE_DATE") ;
                String create_date2 = MapUtil.getString(o2,"CREATE_DATE") ;
                return create_date.compareTo(create_date2);
            }
        });
        return retList;
    }

    /**
     *
     * @author thl
     * @date 2020/11/11 11:28
     * @param paramMap
     * @return java.util.Map<java.lang.String, java.lang.Object>
     */

    private Map<String, Object> checkInfoIsNull(Map paramMap) {
        Map<String, Object> retMap = new HashMap<>();
        String [] keyList = CHECK_COLMON.split(",");
        for(String key : keyList){
            if(!retMap.containsKey("flagA")){
                if(!"".equals(MapUtils.getString(paramMap,"A_"+key,""))){
                    retMap.put("flagA",true);
                    //      break;
                }
            }
            if(!retMap.containsKey("flagZ")){
            if(!"".equals(MapUtils.getString(paramMap,"Z_"+key,""))){
                retMap.put("flagZ",true);
        //        break;
            }}
            if(!retMap.containsKey("flagL")){
            if(!"".equals(MapUtils.getString(paramMap,"L_"+key,""))){
                retMap.put("flagL",true);
         //       break;
            }}
        }
        return retMap;
    }

    /**
     * 整理页面展示需要的Map
     * @author thl
     * @date 2020/11/11 14:53
     * @param paramMap
     * @param type
     * @return java.util.Map<java.lang.String, java.lang.Object>
     */

    Map<String,Object>setMapColum(Map paramMap,String type) {
        Map<String,Object> temp = new HashMap<>();
        temp.putAll(paramMap);
        String [] keyList = CHECK_COLMON.split(",");
        for(String key : keyList){
            temp.put(key,MapUtils.getString(paramMap,type+"_"+key,""));
        }
        String checkColAmount = "INVESTMENT_AMOUNT,BOARD_AMOUNT,TRANS_AMOUNT,OPTICAL_AMOUNT,PROJECT_AMOUNT,TOTAL_AMOUNT" ;
        String[] amount = checkColAmount.split(",");
        for(String tempKey : amount){
            String value = MapUtils.getString(temp,tempKey,"");
            temp.put(tempKey,"".equals(value) ? "" : value + "万元");
        }
        String checkColPeriod = "CONSTRUCT_PERIOD,BOARD_PERIOD,TRANS_PERIOD,OPTICAL_PERIOD,CONSTRUCT_PERIOD_STAND,APPROVAL_PERIOD,LONGEST_PERIOD";
        String[] period = checkColPeriod.split(",");
        for(String tempKey : period){
            String value = MapUtils.getString(temp,tempKey,"");
            temp.put(tempKey,"".equals(value) ? "" : value + "天");
        }
        if (!"".equals(MapUtils.getString(paramMap,type+"_RES_SATISFY",""))) {
            temp.put("RES_SATISFY", ("0".equals(paramMap.get(type+"_RES_SATISFY"))? "满足":"不满足"));
        } else {
            temp.put("RES_SATISFY","");
        }
        return temp;
    }
    /**
     * 查询反馈信息
     * @param srvOrdId  
     * @return
     */
    public List<Map<String, Object>> queryFeedbackInfoOld(String srvOrdId) {
        //查询A端信息
        List<Map<String, Object>> checkListA = new ArrayList<>();
        List<Map<String, Object>> checkListZ = new ArrayList<>();
        List<Map<String, Object>> investListA = new ArrayList<>();
        List<Map<String, Object>> investListZ = new ArrayList<>();
        List<Map<String, Object>> finallyCheckList = new ArrayList<>();
        List<Map<String, Object>> finallyInvestList = new ArrayList<>();
        List<Map<String, Object>> removeList = new ArrayList();
        List<Map<String, Object>> aList = orderDetailsDao.queryFeedbackAInfo(srvOrdId);
        if (aList.size() > 0){
            for (Map map : aList){
                if ("".equals(map.get("CONSTRUCT_SCHEME")) && "".equals(map.get("ACCESS_ROOM")) && "".equals(map.get("INVESTMENT_AMOUNT")) && "".equals(map.get("CONSTRUCT_PERIOD")) && "".equals(map.get("RES_SATISFY"))) {
                    removeList.add(map);
                }else {
                    if (map.get("INVESTMENT_AMOUNT") != null && !"".equals(map.get("INVESTMENT_AMOUNT"))) {
                        map.put("INVESTMENT_AMOUNT", map.get("INVESTMENT_AMOUNT") + "万元");
                    }
                    if (map.get("CONSTRUCT_PERIOD") != null && !"".equals(map.get("CONSTRUCT_PERIOD"))) {
                        map.put("CONSTRUCT_PERIOD", map.get("CONSTRUCT_PERIOD") + "天");
                    }
                    if (map.get("RES_SATISFY") != null && !"".equals(map.get("RES_SATISFY"))) {
                        if ("1".equals(map.get("RES_SATISFY"))) {
                            map.put("RES_SATISFY", "不满足");
                        }
                        else if ("0".equals(map.get("RES_SATISFY"))) {
                            map.put("RES_SATISFY", "满足");
                        }
                    }
                }
                if ("核查汇总".equals(map.get("TACHE_NAME"))){
                    checkListA.add(map);
                    removeList.add(map);
                }
                if ("投资估算".equals(map.get("TACHE_NAME"))){
                    investListA.add(map);
                    removeList.add(map);
                }
            }
        }
        //查询Z端信息
        List<Map<String, Object>> zList = orderDetailsDao.queryFeedbackZInfo(srvOrdId);
        if (zList.size()>0){
            for (Map map : zList){
                if ("".equals(map.get("CONSTRUCT_SCHEME")) && "".equals(map.get("ACCESS_ROOM")) && "".equals(map.get("INVESTMENT_AMOUNT")) && "".equals(map.get("CONSTRUCT_PERIOD")) && "".equals(map.get("RES_SATISFY"))) {
                    removeList.add(map);
                }else {
                    if (map.get("INVESTMENT_AMOUNT") != null && !"".equals(map.get("INVESTMENT_AMOUNT"))) {
                        map.put("INVESTMENT_AMOUNT", map.get("INVESTMENT_AMOUNT") + "万元");
                    }
                    if (map.get("CONSTRUCT_PERIOD") != null && !"".equals(map.get("CONSTRUCT_PERIOD"))) {
                        map.put("CONSTRUCT_PERIOD", map.get("CONSTRUCT_PERIOD") + "天");
                    }
                    if (map.get("RES_SATISFY") != null && !"".equals(map.get("RES_SATISFY"))) {
                        if ("1".equals(map.get("RES_SATISFY"))) {
                            map.put("RES_SATISFY", "不满足");
                        }
                        else if ("0".equals(map.get("RES_SATISFY"))) {
                            map.put("RES_SATISFY", "满足");
                        }
                    }
                }
                if ("核查汇总".equals(map.get("TACHE_NAME"))){
                    checkListZ.add(map);
                    removeList.add(map);
                }
                if ("投资估算".equals(map.get("TACHE_NAME"))){
                    investListZ.add(map);
                    removeList.add(map);
                }
            }
        }
        //合并两个List
        //modified by caowenyang at 2019-07-11
        aList.addAll(zList);
        Map<String, Object> mapA = new HashMap<>();
        Map<String, Object> mapZ = new HashMap<>();
        if (checkListA.size()>0 && checkListZ.size()>0){
            for (int i=0;i<checkListA.size();i++){
                mapA = checkListA.get(i);
                mapZ = checkListZ.get(i);
                if(mapA != null && mapA.size()>0 || mapZ != null && mapZ.size()>0){
                    mapA=this.mergeList(mapA,mapZ);
                }
                finallyCheckList.add(mapA);
            }
        }
        if (investListA.size()>0 && investListZ.size()>0){
            for (int i=0;i<investListA.size();i++){
                mapA = investListA.get(i);
                mapZ = investListZ.get(i);
                if(mapA != null && mapA.size()>0 || mapZ != null && mapZ.size()>0){
                    mapA=this.mergeList(mapA,mapZ);
                }
                finallyInvestList.add(mapA);
            }
        }
        if (removeList.size()>0){
            for (Map map : removeList){
                aList.remove(map);
            }
        }
        for (int i = 0; i< finallyCheckList.size();i++){
            aList.add(finallyCheckList.get(i));
            if (i < finallyInvestList.size()){
                aList.add(finallyInvestList.get(i));
            }
        }
        return aList;
    }

    @Override
    public List<Map<String, Object>> queryAddProductInfoBySrvOrdId(Map<String, Object> param) {

        // 调用接口
        Map<String, Object> orderProgress = progressQueryOrderIntf.progressQueryOrder(param);
        List<Map<String, Object>> message = (List) orderProgress.get("message");
        return message;

    }

    @Override
    public List<Map<String, Object>> queryApplyAttachInfo(Map<String, Object> param) {
        //先根据CST_ORD_ID查询出所有的SRV_ORD_ID
        Map<String, Object> paramMap = new HashMap();
        Map<String, Object> srvMap = orderDetailsDao.querySrvOrdIdsByCstOrdId(param);
        String cstOrdId = MapUtils.getString(param, "cstOrdId");
        String[] srvOrdIds = srvMap.get("SRVORDIDS").toString().split(",");
        paramMap.put("cstOrdId", cstOrdId);
        paramMap.put("srvOrdIds", srvOrdIds);
        List<Map<String, Object>> list = orderDetailsDao.queryApplyAttachInfo(paramMap);
        return list;
    }

    @Override
    public Map<String, Object> queryTaskInfoByTacheCode(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap();
        try{
            String orderId = MapUtils.getString(param, "orderId");
            //根据order_id判断是否为主流程的order_id
            Map<String, Object> orderMap = orderQrySecondaryDao.qryParentPsIdByOrderId(param);
            //orderDetailsDao.queryParentOrderIdByOrderId(orderId);
            List<Map<String, Object>> list = null;
            if (orderMap != null && orderMap.size() > 0){
                String parentOrderId = MapUtils.getString(orderMap, "PARENT_ORDER_ID");
                if (parentOrderId != null && !"".equals(parentOrderId)){
                    param.put("orderId", parentOrderId);
                    orderId = parentOrderId;
                }
                list = orderDetailsDao.queryTaskInfoByTacheCode(param);
            }
            //查询单子来源
            String orderSource = orderQrySecondaryDao.qrySrvOrderSource(orderId);
            if (StringUtils.isEmpty(orderSource) || "NULL".equals(orderSource)){
                orderSource = orderQrySecondaryDao.qrySrvOrderSourceFromSec(orderId);
            }
            if (BasicCode.JIKE.equals(orderSource)){
                orderSource = "集客";
            }else if (BasicCode.ONEDRY.equals(orderSource)){
                orderSource = "一干";
            }else if(BasicCode.SECONDARY.equals(orderSource) || BasicCode.LOCALBUILD.equals(orderSource)){
                orderSource = "";
            }
            if (list != null && list.size() >0){
                for (Map<String, Object> map : list){
                    if(rent.contains(MapUtils.getString(map, "TACHE_CODE"))){
                        /**
                         * 起租，止租，起止租，起租确认通知环节添加单子来源tacheName
                         */
                        map.put("TACHE_NAME", orderSource + MapUtils.getString(map, "TACHE_NAME"));
                    }else {
                        if ("290000002".equals(map.get("PUB_DATE_ID").toString())
                                && (null == map.get("DEAL_USER_ID").toString() || "".equals(map.get("DEAL_USER_ID").toString()))){
                            map.put("ORDERSTATE", "待签收");
                        }
                    }
                }
            }
            resMap.put("success", true);
            resMap.put("result", list);
        }catch(Exception e){
           e.printStackTrace();
           resMap.put("success",false);
           resMap.put("message",e.getMessage());
        }
        return resMap;
    }

    @Override
    public boolean queryIfTrack(String orderId, String cstOrdId) {
        /**
         *
         * 查询到主流程orderid，去判断是二干下发还是本地的；
         *
         * 1,二干下发的：查询二干原单orderid和下发本地网的区域，
         *              查询二干的追单orderid
         *              再查询二干追单下发本地对应区域的流程订单状态
         *              通过判断订单状态确定有没有结束
         * 2,非二干下发：拿到主流程的orderid，去查有没有异常单，以及异常单的状态'760000001','760000002'，这两个状态不允许提交；
         */
        boolean flag = false;
        Map<String, Object> flowOrderIdMap = orderDealDao.getParentOrder(orderId);
        if(MapUtils.isNotEmpty(flowOrderIdMap) && !StringUtils.isEmpty(MapUtils.getString(flowOrderIdMap, "PARENTORDERID"))){
            orderId = MapUtils.getString(flowOrderIdMap, "PARENTORDERID");
        }
        Map<String, Object> ifFromSecondaryMap = orderQrySecondaryDao.qryIfFromSecondary(orderId);
        if(MapUtils.isNotEmpty(ifFromSecondaryMap)){ //二干下发
            String orderIdSec = MapUtils.getString(ifFromSecondaryMap, "PARENT_ORDER_ID");
            String regionId = MapUtils.getString(ifFromSecondaryMap, "REGION_ID");
            List<Map<String, Object>> trackDataList = orderDetailsDao.queryIfTrackData(orderIdSec, cstOrdId);
            if (!ListUtil.isEmpty(trackDataList)) {
                for (Map<String, Object> trackData : trackDataList){
                    String orderIdChg = MapUtils.getString(trackData, "CHG_ORDER_ID");
                    Map<String, Object> toLocalOrderChgMap = orderDetailsDao.qryToLocalOrderChg(orderIdChg, regionId);
                    if (MapUtils.isNotEmpty(toLocalOrderChgMap)){
                        String orderStateChg = MapUtils.getString(toLocalOrderChgMap, "ORDER_STATE");
                        if (OrderTrackOperType.ORDER_STATE_2.equals(orderStateChg)){
                            flag = true;
                        }
                    }
                }
            }
        }else { //本地
            List<Map<String, Object>> trackDataList = orderDetailsDao.queryIfTrackData(orderId, cstOrdId);
            if (!ListUtil.isEmpty(trackDataList)) {
                flag = true;
            }
        }
        return flag;
    }

    //拼接显示字符串。A端与Z端地区以及信息的显示拼接。
    public  String spliceStr(String aArea, String zArea,String aVal, String zVal){
        if(!"".equals(aArea)&&!"".equals(zArea)){
            if(!"".equals(aVal) && !"".equals(zVal)){
                return  aArea + ":"+aVal +"\n"+ zArea+":"+ zVal;
            }
            else if(!"".equals(aVal) && "".equals(zVal)){
                return aArea + ":"+aVal;
            }
            else if("".equals(aVal) && !"".equals(zVal)){
                return zArea + ":"+zVal;
            }
            else{
                return "";
            }
        }
        else if("".equals(aArea)&&!"".equals(zArea)){
            if(!"".equals(aVal) && !"".equals(zVal)){
                return  aVal +"\n"+ zArea+":"+ zVal;
            }
            else if(!"".equals(aVal) && "".equals(zVal)){
                return aVal;
            }
            else if("".equals(aVal) && !"".equals(zVal)){
                return zArea + ":"+zVal;
            }
            else{
                return "";
            }
        }
        else if(!"".equals(aArea)&&"".equals(zArea)){
            if(!"".equals(aVal) && !"".equals(zVal)){
                return  aArea + ":"+aVal +"\n"+ zVal;
            }
            else if(!"".equals(aVal) && "".equals(zVal)){
                return aArea+":"+aVal;
            }
            else if("".equals(aVal) && !"".equals(zVal)){
                return zVal;
            }
            else{
                return "";
            }
        }
        else {
            if(!"".equals(aVal) && !"".equals(zVal)){
                return  aVal +"\n"+ zVal;
            }
            else if(!"".equals(aVal) && "".equals(zVal)){
                return aVal;
            }
            else if("".equals(aVal) && !"".equals(zVal)){
                return zVal;
            }
            else{
                return "";
            }
        }

    }

    //合并两个list,用于A端与B端合并
    public Map<String, Object> mergeList(Map<String, Object> mapA,Map<String, Object> mapZ){
        mapA.put("CONSTRUCT_SCHEME",this.spliceStr(mapA.get("AREA").toString(),mapZ.get("AREA").toString(),mapA.get("CONSTRUCT_SCHEME").toString(),mapZ.get("CONSTRUCT_SCHEME").toString()));
        mapA.put("ACCESS_ROOM",this.spliceStr(mapA.get("AREA").toString(),mapZ.get("AREA").toString(),mapA.get("ACCESS_ROOM").toString(),mapZ.get("ACCESS_ROOM").toString()));
        mapA.put("INVESTMENT_AMOUNT",this.spliceStr(mapA.get("AREA").toString(),mapZ.get("AREA").toString(),mapA.get("INVESTMENT_AMOUNT").toString(),mapZ.get("INVESTMENT_AMOUNT").toString()));
        mapA.put("CONSTRUCT_PERIOD",this.spliceStr(mapA.get("AREA").toString(),mapZ.get("AREA").toString(),mapA.get("CONSTRUCT_PERIOD").toString(),mapZ.get("CONSTRUCT_PERIOD").toString()));
        mapA.put("RES_SATISFY",this.spliceStr(mapA.get("AREA").toString(),mapZ.get("AREA").toString(),mapA.get("RES_SATISFY").toString(),mapZ.get("RES_SATISFY").toString()));

        if (!"".equals(mapA.get("AREA")) && !"".equals(mapZ.get("AREA"))){
            mapA.put("AREA",mapA.get("AREA").toString() +';'+ mapZ.get("AREA"));
        }
        else if("".equals(mapA.get("AREA")) && !"".equals(mapZ.get("AREA"))){
            mapA.put("AREA",mapZ.get("AREA"));
        }
        else if(!"".equals(mapA.get("AREA")) && "".equals(mapZ.get("AREA"))){
            mapA.put("AREA",mapA.get("AREA").toString());
        }
        else{
            mapA.put("AREA","");
        }
        return mapA;
    }

    /**
     * 查询二干下发到本地得order_id
     * @return
     */
    public List<Map<String, Object>> querySec2LocalInfo(String orderId){

      return  orderQrySecondaryDao.querySec2LocalInfo(orderId);
    }
    /**
     *查询核查直开的单子，如果核查单不处理不能提交
     * 20200429 renll
     *  zmp：2009745
     */
    @Override
    public  Map<String, Object>     queryCheckOrderStatBySrvOrdId(String srvOrdId){
        Map<String, Object>  map=new HashMap<String, Object>();
        List<Map<String, Object>> list=orderQrySecondaryDao.queryCheckOrderStatBySrvOrdId(srvOrdId);
        StringBuilder sb=new StringBuilder("该电路有未完工的电路或者核查单，不能提交!对应的工单如下-->");
        if(list.size()>0){
            for (Map<String, Object> trackData : list){
                sb.append(MapUtils.getString(trackData, "ORDER_TYPE")+":"+MapUtils.getString(trackData, "APPLY_ORD_ID")+",");
            }
            map.put("flag","false");
            map.put("msg",sb.toString());
        }else{
            map.put("flag","true");
        }
        return  map;
    }

    @Override
    public Map<String, Object> queryRenameLogByCstOrdId(Map<String,Object> params) {
        Map retMap = new HashMap();
        try {
            List<Map<String, Object>> list = orderDetailsDao.queryRenameLogByCstOrdId(params);
            List<Map<String, Object>> customerList = new ArrayList<>();


            for (int i = 0; i < list.size(); i++) {

                String jsonStr = list.get(i).get("CHANGE_MESSAGE").toString();
                jsonStr = jsonStr.replaceAll("\r|\n|\\s", " ");
                JSONArray array = JSONArray.fromObject(jsonStr);
                List<Map<String, Object>> lst = new ArrayList<>();
                for (int j = 0; j < array.size(); j++) {
                    Map<String, Object> temp = new HashMap<>();
                    temp.put("key", array.getJSONObject(j).get("key"));
                    temp.put("oldValue", array.getJSONObject(j).get("oldValue"));
                    temp.put("newValue", array.getJSONObject(j).get("newValue"));
                    lst.add(temp);
                }

                list.get(i).put("CHANGE_MESSAGE", lst);
                customerList.add(list.get(i));
            }
            retMap.put("success", true);
            retMap.put("customerList", customerList);

        }
        catch (Exception e) {
            retMap.put("success", false);
            retMap.put("message", e.getMessage());
        }
        return retMap;
    }


    @Override
    public  Map<String, Object> querySrvOrdStatBySrvOrdId(String srvOrdId) {
        Map<String, Object> map = new HashMap<String, Object>();
        String srvOrdStat = orderQrySecondaryDao.qryeSrvOrdStatBySrvOrdId(srvOrdId);
        if ("10F".equals(srvOrdStat)){
            map.put("flag","false");
            map.put("msg","该电路已进行报竣，无法发起资源修改！！！");
        }else {
            map.put("flag","true");
        }
        return map;
    }

}
