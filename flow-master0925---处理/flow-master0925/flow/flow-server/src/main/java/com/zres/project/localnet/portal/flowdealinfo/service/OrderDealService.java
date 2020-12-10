package com.zres.project.localnet.portal.flowdealinfo.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDetailsDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderQrySecondaryDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.flowdealinfo.service.entry.TacheWoOrder;
import com.zres.project.localnet.portal.initApplOrderDetail.dao.InsertOrderInfoDao;
import com.zres.project.localnet.portal.initApplOrderDetail.service.GetEnumService;
import com.zres.project.localnet.portal.local.SrvOrdAttrServiceIntf;
import com.zres.project.localnet.portal.local.domain.PageInfo;
import com.zres.project.localnet.portal.localStanbdyInfo.data.dao.OrderStandbyDao;
import com.zres.project.localnet.portal.localStandbyInfo.service.OrderStandbyServiceIntf;
import com.zres.project.localnet.portal.logInfo.service.TacheDealLogIntf;
import com.zres.project.localnet.portal.logInfo.until.BatchWorkThreadPool;
import com.zres.project.localnet.portal.sdwan.dao.SdwanDealDao;
import com.zres.project.localnet.portal.util.OrderTrackOperType;
import com.zres.project.localnet.portal.util.SpringContextHolderUtil;
import com.zres.project.localnet.portal.webservice.IInterface;
import com.zres.project.localnet.portal.webservice.construct.ResPreAssessmentIntf;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.zres.project.localnet.portal.webservice.govDouCheck.DualCirResCheckServiceIntf;
import com.zres.project.localnet.portal.webservice.interfaceJiKe.BackOrderServiceIntf;
import com.zres.project.localnet.portal.webservice.interfaceJiKe.FinishOrderServiceIntf;
import com.zres.project.localnet.portal.webservice.oneDry.FeedbackInterface;
import com.zres.project.localnet.portal.webservice.outLineSystem.ProvinceSendOrderServiceIntf;
import com.zres.project.localnet.portal.webservice.res.*;
import com.zres.project.localnet.portal.webservice.sms.SendMessageService;
import com.zres.project.localnet.portal.webservice.until.InterfaceThreadPool;
import com.ztesoft.res.frame.core.util.ListUtil;
import com.ztesoft.res.frame.core.util.MapUtil;
import com.ztesoft.res.frame.flow.common.dto.*;
import com.ztesoft.res.frame.flow.common.exception.FlowException;
import com.ztesoft.res.frame.flow.common.lock.impl.DatabaseLock;
import com.ztesoft.res.frame.flow.common.lock.intf.DistributeLock;
import com.ztesoft.res.frame.flow.common.service.FlowActionHandler;
import com.ztesoft.res.frame.flow.spec.order.dao.FlowOrderSpecDAO;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import com.ztesoft.res.frame.user.inf.UserInfo;
import org.apache.axis.utils.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class OrderDealService implements OrderDealServiceIntf {
    Logger logger = LoggerFactory.getLogger(OrderDealService.class);
    private static final String[] PENDING_STATE = {"290000000", "290000002","290000004"}; //待审核状态 可更新

    @Autowired
    private FlowActionHandler flowActionHandler;

    @Autowired
    private FlowOrderSpecDAO flowOrderSpecDAO;

    @Autowired
    private OrderDealDao orderDealDao;

    @Autowired
    private OrderQrySecondaryDao orderQrySecondaryDao;
    @Autowired
    private InsertOrderInfoDao insertOrderInfoDao;

    @Autowired
    private WebServiceDao webServiceDao;

    @Autowired
    private BusinessCreateServiceIntf businessCreateServiceIntf;

    @Autowired
    private BusinessArchiveServiceIntf businessArchiveServiceIntf;

    @Autowired
    private ResAssignmentServiceIntf resAssignmentServiceIntf;

    @Autowired
    private BusinessAutoAssignServiceIntf businessAutoAssignServiceIntf;

    @Autowired
    private BusinessRollbackServiceIntf businessRollbackServiceIntf;

    @Autowired
    private OrderDetailsDao orderDetailsDao;

    @Autowired
    private FeedbackInterface feedbackInterface;

    @Autowired
    private CheckFeedbackService checkFeedbackService;

    @Autowired
    private SendMessageService sendMessageService;
    @Autowired
    private OrderSendMsgService orderSendMsgService;

    @Autowired
    private BuizQueryOnTimeServiceIntf buizQueryOnTimeServiceIntf;
    @Autowired
    private GetEnumService getEnumService;
    @Autowired
    private FinishOrderServiceIntf finishOrderServiceIntf;
    @Autowired
    private IInterface interfaceIntf;
    @Autowired
    private OrderStandbyServiceIntf orderStandbyService;
    @Autowired
    private BackOrderServiceIntf backOrderServiceIntf;
    @Autowired
    private ListenerOrderServiceIntf listenerOrderServiceIntf;
    @Autowired
    private TacheDealLogIntf tacheDealLogIntf;
    @Autowired
    private OrderStandbyDao orderStandbyDao;
    @Autowired
    private SdwanDealDao sdwanDealDao;
    @Autowired
    private LocalAndSecondMutualServiceInf localAndSecondMutualServiceInf;
    @Autowired
    private ResCfsAttrUpdateServiceIntf resCfsAttrUpdateServiceIntf;
    @Autowired
    private ResPreAssessmentIntf resPreAssessmentIntf;
    @Autowired
    private CheckOrderServiceIntf checkOrderServiceIntf;
    @Autowired
    private DualCirResCheckServiceIntf dualCirResCheckServiceIntf;
    @Autowired
    private ProvinceSendOrderServiceIntf provinceSendOrderServiceIntf;
    @Autowired
    private FowordResServiceIntf fowordResServiceIntf;
    @Autowired
    private SrvOrdAttrServiceIntf srvOrdAttrServiceIntf;
    public OrderDealService() {
    }

    public String findActType(String typeCode) {
        return webServiceDao.selectActType(typeCode);
    }

    public String queryDeptId(String areaCode) {
        return webServiceDao.selectDeptId(areaCode);
    }

    public String selectSrvOrdId(String subscribeId, String serialNumber, String tradeId) {
        return orderDealDao.querySrvOrdId(subscribeId, serialNumber, tradeId);
    }

    public List<Map> getTacheInfo(String tradeId) {
        return orderDealDao.queryTacheInfo(tradeId);
    }

    public List selectStaffInfoList(String userId) {
        return orderDealDao.queryStaffInfoList(userId);
    }

    public List selectOrderAttrInfo(String svrOrderId) {
        return orderDealDao.queryOrderAttrInfoList(svrOrderId);
    }

    public List selectOrderInfoList(String svrOrderId) {
        return orderDealDao.queryOrderInfoList(svrOrderId);
    }

    private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * 通过关联业务订单号查询订单
     *
     * @param tradeIdReal
     * @param serialNumber
     * @return
     */
    public List<Map<String, Object>> queryOrderList(String tradeIdReal, String serialNumber) {
        return orderDealDao.selectOrderList(tradeIdReal, serialNumber);
    }
    /**
     * 通过关联业务订单号以及服务提供标识查询订单
     * @param tradeIdReal
     * @param serialNumber
     * @param serviceOfferId  服务提供标识
     * @return
     */
    public List<Map<String, Object>> queryOrderList2(String tradeIdReal, String serialNumber,String serviceOfferId){
        return orderDealDao.selectOrderList2(tradeIdReal, serialNumber,serviceOfferId);
    }
    /**
     * 更新订单状态
     *
     * @param activeType
     * @param tradeIdReal
     * @param serialNumber
     */
    public void updateOrdInfo(String activeType, String tradeIdReal, String serialNumber) {
        orderDealDao.updateOrdInfo(activeType, tradeIdReal, serialNumber);
    }

    public void updateFinDate(String requireCompleteCate, String orderId) {
        orderDealDao.updateFinDate(requireCompleteCate, orderId);
    }

    public void addOrderNotice(String srvOrdId, String requireCompleteCate, String srvOrdDesc, String activeType,
                               Map<String, String> params) {
        params.put("srvOrdId", srvOrdId);
        params.put("requireCompleteCate", requireCompleteCate);
        params.put("srvOrdDesc", srvOrdDesc);
        params.put("activeType", activeType);
        orderDealDao.addOrderNotice(params);
    }

    public List<Map> queryListDealCurrentUser(String orderId, String activeType) {
        return orderDealDao.queryListDealCurrentUser(orderId, activeType);
    }

    @Override
    public Map<String, Object> qryProvinceName(Map<String, Object> paramsMap) {
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> retmap = new HashMap<String, Object>();
        if (paramsMap.get("type") == "A" || "A".equals(paramsMap.get("type"))) {
            map = orderDealDao.qryProvinceA(paramsMap);
        } else if (paramsMap.get("type") == "Z" || "Z".equals(paramsMap.get("type"))) {
            map = orderDealDao.qryProvinceZ(paramsMap);
        }
        if(null != map){
            retmap.put("ATTR_VALUE", map.get("ATTR_VALUE"));
        }
        return retmap;
    }

    @Override
    public String querySpecNetMagPro(Map<String, Object> param) {
        String specityNetManage = MapUtil.getString(param,"specityNetManage");
        String proId = MapUtil.getString(param,"proId");
        String type = MapUtil.getString(param,"type");
        String[] specityNetManageStr = specityNetManage.split(",");
        Map<String, Object> orderDataMap = new HashMap<String, Object>();
        orderDataMap.put("proId",proId);
        orderDataMap.put("type",type);
        orderDataMap.put("specityNetManageStr",specityNetManageStr);
        return orderDealDao.querySpecNetMagPro(orderDataMap);
    }

    @Override
    public Map<String, Object> queryOrderInfo() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("data", orderDealDao.queryOrderInfo());
        map.put("flag", "1");
        return map;
    }

    @Override
    public Map<String, Object> saveResConstructConfigInfo(Map<String, Object> param) {
        /**
         * 环节为资源分配:500001157 则保存500001158 数据制作 500001159 资源施工
         * 环节为光纤资源分配:500001155 则保存 500001156 外线施工
         */
        Map<String, Object> mapReturn = new HashMap<String, Object>();
        String flag  = MapUtils.getString(param, "flag");
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) param.get("dataInfo");
        if (!CollectionUtils.isEmpty(dataList)) {
            for (Map<String, Object> dataM : dataList) {
                List<Map<String, Object>> dataParamList = new ArrayList<Map<String, Object>>();
                if ("other".equals(flag)){
                    String dataMakeMap = MapUtils.getString(param, "dataMake");// 数据制作 选择岗位
                    String dataMakeUserMap = MapUtils.getString(param, "dataMakeUser");// 数据制作 选择人员
                    String resConstructMap = MapUtils.getString(param, "resConstruct");// 资源施工
                    String resConstructUserMap = MapUtils.getString(param, "resConstructUser");// 资源施工 人员
                    Map<String, Object> dataMake = getListDispObjByString(dataMakeMap,dataMakeUserMap,
                            "500001158",dataM.get("ORDER_ID").toString());
                    dataParamList.add(dataMake);
                    if (!StringUtils.isEmpty(resConstructMap) || !StringUtils.isEmpty(resConstructUserMap)){
                        Map<String, Object> resConstruct = getListDispObjByString(resConstructMap,resConstructUserMap,
                                "500001159",dataM.get("ORDER_ID").toString());
                        dataParamList.add(resConstruct);
                    }
                    mapReturn = saveResConstructConfigInfoTra(dataM.get("ORDER_ID").toString(), dataParamList);
                }else if ("outside".equals(flag)){
                    String outsideMap = MapUtils.getString(param, "outside");// 外线施工
                    String outsideUserMap = MapUtils.getString(param, "outsideUser");// 外线施工 人员
                    Map<String, Object> outside = getListDispObjByString(outsideMap,outsideUserMap,
                            "500001156",dataM.get("ORDER_ID").toString());
                    dataParamList.add(outside);
                    mapReturn = saveResConstructConfigInfoTra(dataM.get("ORDER_ID").toString(), dataParamList);
                }
            }
        }
        return mapReturn;
    }

    @Override
    public Map<String, Object> saveResConstructConfigInfoTra(String orderId, List<Map<String, Object>> dataParamList) {
        Map<String, Object> mapReturn = new HashMap<String, Object>();
        try {
            for (Map<String, Object> mapT : dataParamList) {
                String tacheId = mapT.get("tacheId").toString();
                Map<String, Object> qryMap = new HashMap<>();
                qryMap.put("orderId", orderId);
                qryMap.put("tacheId", tacheId);
                qryMap.put("flag", "CHILD");
                List<Map<String, Object>> dispDateList = orderDealDao.qryDispObjTache(qryMap);
                if (!CollectionUtils.isEmpty(dispDateList)) {
                    orderDealDao.updateDispObjConfig(mapT);
                } else {
                    orderDealDao.insertDispObj(mapT);
                }
            }
            mapReturn.put("success", true);
            mapReturn.put("message", "电路对应的岗位配置信息保存成功");
        } catch (Exception e) {
            mapReturn.put("success", false);
            mapReturn.put("message", e.getMessage());
        }
        return mapReturn;
    }

    @Override
    public Map<String, Object> getListDispObjByString(String jobStr, String userStr, String tacheId, String orderId) {

        Map<String, Object> paramsMap = new HashMap<String, Object>();
        if (!StringUtils.isEmpty(jobStr)) {
            paramsMap.put("tacheId", tacheId);
            paramsMap.put("orderId", orderId);
            paramsMap.put("dispState", "10A");
            StringBuffer jobIdStr = new StringBuffer();
            StringBuffer jobId = new StringBuffer();
            List<Object> dataMakeMapList = JSON.parseArray(jobStr);
            for (Object object : dataMakeMapList) {
                Map<String, Object> jobMap = (Map<String, Object>) object;// 取出list里面的值转为map
                jobIdStr.append("260000002_J!G@F_").append(MapUtils.getString(jobMap, "id")).append(",");
                jobId.append(MapUtils.getString(jobMap, "id")).append(",");
            }
            if (!StringUtils.isEmpty(userStr)) {
                List<Object> dataMakeUserMapList = JSON.parseArray(userStr);
                for (Object object : dataMakeUserMapList) {
                    Map<String, Object> userMap = (Map<String, Object>) object;// 取出list里面的值转为map
                    jobIdStr.append("260000003_J!G@F_").append(MapUtils.getString(userMap, "id"))
                            .append(",");
                    jobId.append(MapUtils.getString(userMap, "id")).append(",");
                }
                dataMakeMapList.addAll(dataMakeUserMapList);
            }
            String jobIdStrS = jobIdStr.substring(0, jobIdStr.length() - 1);
            String jobIdS = jobId.substring(0, jobId.length() - 1);
            paramsMap.put("dispObj", jobIdStrS);
            paramsMap.put("dispObjId", jobIdS);
            paramsMap.put("dispObjList", dataMakeMapList.toString());
        }
        if (!StringUtils.isEmpty(userStr) && StringUtils.isEmpty(jobStr)) {
            paramsMap.put("tacheId", tacheId);
            paramsMap.put("orderId", orderId);
            paramsMap.put("dispState", "10A");
            StringBuffer jobIdStr = new StringBuffer();
            StringBuffer jobId = new StringBuffer();
            List<Object> configUserMapList = JSON.parseArray(userStr);
            for (Object object : configUserMapList) {
                Map<String, Object> userMap = (Map<String, Object>) object;// 取出list里面的值转为map
                jobIdStr.append("260000003_J!G@F_").append(MapUtils.getString(userMap, "id")).append(",");
                jobId.append(MapUtils.getString(userMap, "id")).append(",");
            }
            String jobIdStrS = jobIdStr.substring(0, jobIdStr.length() - 1);
            String jobIdS = jobId.substring(0, jobId.length() - 1);
            paramsMap.put("dispObj", jobIdStrS);
            paramsMap.put("dispObjId", jobIdS);
            paramsMap.put("dispObjList", userStr);
        }
        return paramsMap;
    }

    /**
     * 工单处理
     *
     * @param params
     * @return
     * @throws Exception
     */
    @Override
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED,readOnly = false)
    public Map<String, Object> submitOrder(Map<String, Object> params) throws Exception {
        logger.info("进入工单处理方法。。。。。。。。。。。。。。。。。。。。。。。");
        Map<String, Object> resMap = new HashMap<String, Object>();
        resMap.put("success", true);
        try {
            String action = MapUtils.getString(params, "action");
            String sysResFullCom = MapUtils.getString(params, "sysResFullCom");
            String oneDryValue = MapUtils.getString(params, "oneDryValue");
            // 要处理的电路信息
            String circuitDataStr = MapUtils.getString(params, "circuitData");
            String srvOrdIdStr = "";
            List<Object> circuitDatalist = JSON.parseArray(circuitDataStr);
            List<String> orderIdList = new ArrayList<String>();
            for (Object object : circuitDatalist) {
                Map<String, Object> circuitDataMap = (Map<String, Object>) object;// 取出list里面的值转为map
                String woId = MapUtils.getString(circuitDataMap, "WO_ID");
                String orderId = MapUtils.getString(circuitDataMap, "ORDER_ID");
                String tacheId = MapUtils.getString(circuitDataMap, "TACHE_ID");
                String psId = MapUtils.getString(circuitDataMap, "PS_ID");
                String srvOrdId = MapUtils.getString(circuitDataMap, "SRV_ORD_ID");
                String relateInfoId = MapUtils.getString(circuitDataMap, "RELATE_INFO_ID");
                String finishDateStr = MapUtils.getString(params, "finishDate");
                String opinion = MapUtils.getString(circuitDataMap, "opinion","");
                String activeType = MapUtils.getString(circuitDataMap, "ACTIVE_TYPE");
                String serviceId = MapUtils.getString(circuitDataMap, "SERVICE_ID");

                if(BasicCode.COMPLETE_CONFIRM.equals(tacheId) && !"".equals(finishDateStr) && finishDateStr!=null) {
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置日期格式
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                    Date finishDate = df.parse(finishDateStr);
                    finishDateStr = dateFormat.format(finishDate);
                    params.put("ATTR_CODE", "REC_30004");
                    params.put("SRV_ORD_ID", srvOrdId);
                    params.put("ATTR_ACTION", 0);
                    params.put("ATTR_NAME", "全程竣工时间");
                    params.put("ATTR_VALUE", finishDateStr);
                    params.put("SOURSE", "内部建单");
                    params.put("ATTR_VALUE_NAME", "");
                    params.put("CREATE_DATE", df.format(new Date()));
                }
                if ("".equals(srvOrdIdStr)) {
                    srvOrdIdStr = srvOrdId;
                } else {
                    srvOrdIdStr = srvOrdIdStr + "," + srvOrdId;
                }
                params.put("woId", woId);
                params.put("orderId", orderId);
                params.put("tacheId", tacheId);
                params.put("psId", psId);
                params.put("srvOrdId", srvOrdId);
                params.put("srvOrdIdFullCom", srvOrdId);
                params.put("relateInfoId", relateInfoId);
                params.put("sysResource", sysResFullCom);
                params.put("oneDryValue", oneDryValue);
                params.put("srvOrdIdStr", srvOrdIdStr);
                params.put("opinion", opinion);
                params.put("activeType", activeType);
                params.put("serviceId",serviceId);

                if ((Boolean) resMap.get("success")) {
                    if ("submit".equals(action)) {
                        // 工单正常流转
                        resMap = sendWoOrder(params);
                    } else if ("trans".equals(action)) {
                        // 工单转派
                        resMap = transferWoOrder(params);
                    } else if ("rollBackOrder".equals(action)) {
                        // 退单
                        resMap = rollBackWoOrder(params);
                    } else if ("goBackOrder".equals(action)) {
                        // 回退
                        resMap = goBackOrder(params);
                    } else if ("checkBackOrder".equals(action)) {
                        //核查单--专业核查环节退单
                        resMap = checkOrderServiceIntf.specialCheckBackOrder(params);
                    } else if ("checkTotalBackOrder".equals(action)) {
                        //核查单--核查汇总环节退单
                        resMap = checkOrderServiceIntf.checkTotalBackOrder(params);
                    } else if ("resConfig".equals(action)) {
                        // 资源配置
                        resMap = resConfig(params);// params 需要 woId、orderId、tacheId、srvOrdId
                    } else if("wocc".equals(action)){
                        resMap = ccWoOrder(params); // 抄送
                    } else if("sendEngineering".equals(action)){
                        resMap = sendEngineering(params); // 下发工建系统
                    }
                }
                if ((Boolean) resMap.get("success")) {
                    orderIdList.add(orderId);
                }
            }

            if((Boolean) resMap.get("success")){
                try {
                    if ("submit".equals(action) && params.containsKey("dispatchOrderData") && params.containsKey("disOrdFlag")) {
                        insertDispatchOrder(params);
                    }
                } catch (Exception e) {
                    resMap.put("message", "派单失败!保存调单信息失败：" + e.getMessage());
                }
            }
            //如果没有上传新附件，而删除了附件，就会走这里
            String delFiles = MapUtil.getString(params, "delFiles", "");
            if (!StringUtils.isEmpty(delFiles)){
                String[] delFilesList = delFiles.split(",");
                if(delFilesList.length > 0){
                    orderStandbyDao.removeAttach(delFilesList);
                }
            }
            if (!"resConfig".equals(action)) { //资源配置不发短信，其它动作发送短信
                if (!ListUtil.isEmpty(orderIdList)){
                    logger.info("开始准备发送短信。。。。。。。。。。。。。。。。。。。。");
                    String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
                    Map<String, Object> sendMsgMap = new HashMap<String, Object>();
                    sendMsgMap.put("operStaffId", operStaffId);
                    sendMsgMap.put("orderIdList", orderIdList);
                    sendMsgMap.put("operAction", action);
                    orderSendMsgService.sendMsgBefore(sendMsgMap);
                }
                /*Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(operStaffId));
                String areaId = MapUtils.getString(operStaffInfoMap, "AREA_ID");
                Map<String, Object> msmSwitchMap = orderDealDao.qryMsmSwitchByArea(areaId);
                // ISSEND等于1发送短信
                if ("1".equals(MapUtils.getString(msmSwitchMap, "ISSEND"))) {
                    // 查询工单列表发送短信
                    qryUserObjByWoIdsSendMsg(orderIdList);
                }*/
            }

        } catch (Exception e) {
            e.printStackTrace();
            resMap.put("success", false);
        }
        return resMap;
    }
    /**
     * 直接反馈核查信息
     * @param params
     * @return
     * @throws Exception
     */
    @Override
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED,readOnly = false)
    public Map<String, Object> feedBackDirect(Map<String, Object> params) throws Exception {
        logger.info("直接反馈核查信息开始。。。。。。。。。。。。。。。。。。。。。。。");
        Map<String, Object> resMap = new HashMap<>();
        resMap.put("success", true);
        //2.保存核查标准化信息，
        Map<String, Object> saveMap = saveCheckInfo(params);
        if (!MapUtils.getBoolean(saveMap, "success")) {
            return saveMap;
        }
        //3，调用集客反馈接口，
        Map<String, Object> map = new HashMap<>();
        map.put("srvOrdId",MapUtils.getString(params, "srvOrdId"));
        map.put("feedbackDirect", true);
        //     InterfaceThreadPool.tuneIntfToExecute(interfaceIntf, map);
        Map finMap = finishOrderServiceIntf.finishOrder(map);
        if (!"1".equals(MapUtils.getString(finMap, "RESP_CODE"))) {
            logger.info("调用集客反馈接口异常，异常原因：" + MapUtils.getString(finMap, "RESP_DESC"));
            resMap.put("success", false);
            resMap.put("message", "调用集客反馈接口异常， 请联系相关人员");
            return resMap;
        }
        //4.更改工单表、流程订单表、电路表的状态为已完成
        //更改工单表
        //update gom_wo set  WO_STATE  ='290000004' where  WO_ID=?
        orderDealDao.updateWoStateByWoId(MapUtils.getString(params, "woId"), "290000004");

        //流程订单表
        //update gom_order set  ORDER_STATE  ='200000004' where  ORDER_ID=?
        Map orderMap = new HashMap();
        orderMap.put("orderState", OrderTrackOperType.WO_ORDER_STATE_4);
        orderMap.put("stateDate", new java.sql.Date(new java.util.Date().getTime()));
        orderMap.put("orderId", MapUtils.getString(params, "orderId"));
        orderDealDao.updateOrderStateById(orderMap);

        //电路表的状态为已完成
        //update GOM_BDW_SRV_ORD_INFO set SRV_ORD_STAT  ='10F' where SRV_ORD_ID=
        orderDealDao.updateSrvOrdState(MapUtils.getString(params, "srvOrdId"), "10F");
        return resMap;
    }

    /**
     * 下发工建：先调核查前评估接口，再提交工单
     * @param params
     * @return
     */
    public Map<String,Object> sendEngineering(Map<String, Object> params) throws Exception{
        logger.info("--------------核查汇总 下发工建派单------进入工单处理方法--------------------");

        Map<String, Object> resMap = new HashMap<String, Object>();
        resMap = resPreAssessmentIntf.preAssessment(params);
        if(!MapUtils.getBoolean(resMap,"success")){
            return resMap;
        }
        resMap = complateWoWithAttr(params);
        return resMap;
    }

    /**
     * 有线条参数，提交工单公用方法
     * @param params key:woId  orderId  tacheId remark
     * @return
     * @throws Exception
     */
    public Map<String,Object> complateWoWithAttr(Map<String, Object> params) throws Exception{
        logger.info("-------------进入工单处理方法--------------------");
        Map<String, Object> resMap = new HashMap<String, Object>();
        String tacheId = MapUtils.getString(params, "tacheId");
        Map<String, Object> tacheInfo = sdwanDealDao.qryTacheInfo(tacheId);
        String tacheName = MapUtils.getString(tacheInfo, "TACHE_NAME");
        String tacheCode = MapUtils.getString(tacheInfo, "TACHE_CODE");
        TacheWoOrder tacheWoOrder = new TacheWoOrder(Integer.parseInt(tacheId),tacheCode,tacheName,params);
        String beanName = tacheWoOrder.getBeanNameByTacheCodeWithOperAttr();
        DealTacheWoOrderIntf dealTacheWoOrder = SpringContextHolderUtil.getBean(beanName);
        try{
            resMap = dealTacheWoOrder.tacheDoSomething(params);
        } catch (FlowException fe){
            fe.printStackTrace();
            logger.error("派单失败：", fe);
            logger.info("--------------sdwan模块------------工单处理失败-----------------------");
            throw fe;
        }catch (Exception e){
            e.printStackTrace();
            logger.error("派单失败：", e);
            logger.info("--------------sdwan模块------------工单处理失败-----------------------");
            throw e;
        }
        return resMap;
    }

    /**
     * 查询当前操作人的相关信息
     *
     * @param userId
     * @return
     */
    @Override
    public Map<String, Object> getOperStaffInfo(Integer userId) {
        return orderDealDao.getOperStaffInfo(userId);
    }

    /**
     * 通过区域查询短信是否需要发送
     *
     * @param areaId
     * @return
     */
    @Override
    public Map<String, Object> qryMsmSwitchByArea(String areaId) {
        return orderDealDao.qryMsmSwitchByArea(areaId);
    }

    @Override
    public Map<String, Object> qryProvinceValue(Map<String, Object> paramsMap) {
        Map<String, Object> retmap = new HashMap<String, Object>();
        Map<String, Object> map = orderDealDao.qryProvinceValue(paramsMap);
        retmap.put("ID", map.get("ID"));
        return retmap;
    }

    /*@Override
    public Map<String, Object> sendSecondScheduleLTRes(String flagSys,Map<String, Object> resMapSec) {
        String srvOrderIdRes = MapUtil.getString(resMapSec, "SRV_ORD_ID");
        Map<String, Object> resMap = new HashMap<String, Object>();
        resMap.put("success", true);
        // 查询是否成功调过资源汇总接口
        int num = orderDealDao.qryInterResult(srvOrderIdRes, "BusinessAutoAssign");
        if (num < 1) {
            // 调用业务电路汇总接口(允许多次调用)
            Map<String, Object> resConfigParams = new HashMap<String, Object>();
            resConfigParams.put("flag", flagSys);
            resConfigParams.put("srvOrdId", srvOrderIdRes);
            resConfigParams.put("relaCrmOrderCodes", resMapSec.get("relaCrmOrderCodes"));
            Map retmap = businessAutoAssignServiceIntf.businessAutoAssign(resConfigParams);
            if (!"成功".equals(MapUtils.getString(retmap, "returncode"))) {
                resMap.put("success", false);
                resMap.put("message", "派单失败!调用资源业务电路汇总接口异常，异常原因：" + MapUtils.getString(retmap, "returndec"));
                return resMap;
            }
        }
        return resMap;
    }*/

    @Override
    public Map createOrder(Map<String, Object> params) {
        logger.info(">>>>>>>>>>>>>>>>>启流程>>>>>>>>>>>>>>>>>>>>>>>");
        FlowOrderDTO flowOrderDTO = new FlowOrderDTO();
        FlowOrderSpecDTO specDTO = new FlowOrderSpecDTO();
        int ordPsid = MapUtils.getIntValue(params, "ordPsid");
        Map<String, Object> orderInfoMap = orderDealDao.findOrderCode(ordPsid);

        String orderType = MapUtils.getString(orderInfoMap, "ORDER_TYPE");
        String orderObjType = MapUtils.getString(orderInfoMap, "OBJ_TYPE");
        String orderActType = MapUtils.getString(orderInfoMap, "ACT_TYPE");
        specDTO.setOrderType(orderType);
        specDTO.setObjType(orderObjType);
        specDTO.setActType(orderActType);
        flowOrderDTO.setOrderSpec(specDTO);
        flowOrderDTO.setParentOrderId(MapUtils.getString(params, "parentOrderId"));
        flowOrderDTO.setParentOrderCode(MapUtils.getString(params, "parentOrderCode"));
        /**
         * 局内电路指定需求部门审核的处理对象DEMAND_DEPART_AUDIT_DISP_OBJ
         */
        if (!StringUtils.isEmpty(MapUtils.getString(params, "DEMAND_DEPART_AUDIT_DISP_OBJ"))){
            String[] nextDispObj = MapUtils.getString(params, "DEMAND_DEPART_AUDIT_DISP_OBJ").split("_J!G@F_");
            flowOrderDTO.setNextDispType(nextDispObj[0]);
            flowOrderDTO.setNextDispId(nextDispObj[1]);
        }

        // 定单属性--启动子流程
        if (params.containsKey("attr")) {
            Map attrMap = MapUtils.getMap(params, "attr"); // 存了区域和专业
            List<HashMap<String, String>> orderAttrs = new ArrayList<HashMap<String, String>>();
            Iterator<Map.Entry<String, String>> iter = attrMap.entrySet().iterator();
            while (iter.hasNext()) {
                HashMap<String, String> operAttrsMap = new HashMap<String, String>();
                Map.Entry<String, String> e = iter.next();
                String key = e.getKey();
                String value = String.valueOf(e.getValue());
                operAttrsMap.put("KEY", key);
                operAttrsMap.put("VALUE", value);
                orderAttrs.add(operAttrsMap);
            }
            flowOrderDTO.setOrderAttrs(orderAttrs);
        }
        flowOrderDTO.setOrderTitle(MapUtils.getString(params, "ORDER_TITLE"));
        flowOrderDTO.setAreaId(MapUtils.getString(params, "AREA"));
        flowOrderDTO.setRemark(MapUtils.getString(params, "ORDER_CONTENT"));
        flowOrderDTO.setReqFinDate(MapUtils.getString(params, "requFineTime"));
        String orderId = flowOrderSpecDAO.getSeq("GOM_ORDER");
        flowOrderDTO.setOrderId(orderId);
        String operStaffId = "";
        if (ThreadLocalInfoHolder.getLoginUser() == null) {
            operStaffId = "11";
        } else {
            // 获取用户id
            operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        }
        FlowOrderDTO retDTO = flowActionHandler.createOrder(operStaffId, flowOrderDTO);
        Map<String, Object> retMap = new HashMap(2);
        retMap.put("orderId", retDTO.getOrderId());
        retMap.put("orderCode", retDTO.getOrderCode());
        return retMap;
    }

    @Override
    public Map<String, Object> rollBackWoOrder(Map<String, Object> params) {
        logger.info(">>>>>>>>>>>>>>>>>流程中退单>>>>>>>>>>>>>>>>>>>>>>>");
        Map<String, Object> resMap = new HashMap<String, Object>();
        Map<String, Object> operStaffInfoMap = new HashMap<String, Object>();
        String operStaffId = "-1";
        String operStaffName = "-1";
        if(params.containsKey("operStaffId")&& !"".equals(MapUtils.getString(params,"operStaffId",""))){
            operStaffId = MapUtils.getString(params,"operStaffId");
            operStaffInfoMap = orderDealDao.getOperStaffInfo(MapUtils.getInteger(params,"operStaffId"));
            operStaffName = MapUtils.getString(operStaffInfoMap,"USER_NAME");
        } else if (ThreadLocalInfoHolder.getLoginUser() == null) {
            operStaffId = "-1";
            //operStaffName = "一干";
            operStaffInfoMap.put("ORG_ID", "-1");
            operStaffInfoMap.put("ORG_NAME", "一干");
            operStaffInfoMap.put("USER_PHONE", "-1");
            operStaffInfoMap.put("USER_EMAIL", "-1");
            operStaffInfoMap.put("USER_REAL_NAME", "一干");
            operStaffInfoMap.put("USER_ID", operStaffId);
        } else {
            operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
            operStaffName = ThreadLocalInfoHolder.getLoginUser().getUserName();
            operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(operStaffId));
        }
        String woId = MapUtils.getString(params, "woId");
        String orderId = MapUtils.getString(params, "orderId");
        String tacheId = MapUtils.getString(params, "tacheId");
        String remark = MapUtils.getString(params, "remark");
        String flagAct = MapUtils.getString(params, "flag");
        String srvOrdId = MapUtils.getString(params, "srvOrdIdFullCom");
        //String relateInfoId = MapUtils.getString(params, "relateInfoId");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("srvOrdId", srvOrdId);
        Map<String, Object> belongSysMap = orderQrySecondaryDao.qrySrvOrderBelongSys(map);
        String systemResource = MapUtils.getString(belongSysMap, "SYSTEM_RESOURCE");
        Map<String, String> cstOrderDataMap = new HashMap<String, String>();
        if (BasicCode.SECOND.equals(systemResource)){
            cstOrderDataMap = orderDealDao.qryCstOrderDataFromSec(orderId);
        }else if (BasicCode.LOCAL.equals(systemResource)){
            cstOrderDataMap = orderDealDao.qryCstOrderData(orderId);
        }
        DistributeLock lock = new DatabaseLock(woId);
        try {
            Date reasonStart = new Date();
            logger.info("========流程平台flowActionHandler.queryRollBackReasons-开始:"+reasonStart);
            List<FlowRollBackReasonDTO> flowRollBackReasonDTOs = flowActionHandler.queryRollBackReasons(woId);
            Date reasonEnd = new Date();
            long between=reasonEnd.getTime()-reasonStart.getTime();
            logger.info("========流程平台flowActionHandler.queryRollBackReasons-结束:"+reasonEnd);
            logger.info("========流程平台flowActionHandler.queryRollBackReasons耗时:"+between+"微秒");
            FlowRollBackReasonDTO flowRollBackReasonDTO = null;
            String toTacheId = "";
            if ("一干".equals(flagAct)) {
                if (BasicCode.RENT.equals(tacheId)) {
                    String mainOrg = MapUtils.getString(cstOrderDataMap, "MAINORG");
                    Map<String, Object> areaMap = orderDealDao.qryCircuitAreaInfo(srvOrdId);
                    if (mainOrg.equals(MapUtils.getString(areaMap, "PARENT_ID"))) {
                        toTacheId = BasicCode.CROSS_WHOLE_COURDER_TEST;
                    } else {
                        toTacheId = BasicCode.LOCAL_TEST;
                    }
                } else if (BasicCode.CROSS_WHOLE_COURDER_TEST.equals(tacheId)) {
                    toTacheId = BasicCode.LOCAL_TEST;
                }

                for (int i = 0; i < flowRollBackReasonDTOs.size(); i++) {
                    FlowRollBackReasonDTO _flowRollBackReasonDTO = flowRollBackReasonDTOs.get(i);
                    FlowTacheDTO flowTacheDTO = _flowRollBackReasonDTO.getToTache();
                    String _toTacheId = flowTacheDTO.getId();
                    if (toTacheId.equals(_toTacheId)) {
                        flowRollBackReasonDTO = _flowRollBackReasonDTO;
                    }
                }
            } else if ("LOCAL".equals(flagAct)) {
                if (BasicCode.COMPLETE_CONFIRM.equals(tacheId)) { // 完工确认
                    Map<String, Object> orderDataMap = orderDealDao.qryOrderData(woId);
                    String psId = MapUtils.getString(orderDataMap, "PSID");
                    String productInfoId = MapUtils.getString(orderDataMap, "CODE_INFO_ID");
                    if (BasicCode.LOCAL_CUST_NEWOPEN_FLOW.equals(psId)) { // 本地客户电路新开、变更、移机流程
                        if ("1".equals(productInfoId) || "2".equals(productInfoId) || "8".equals(productInfoId)) {
                            toTacheId = BasicCode.LOCAL_TEST;
                        } else {
                            toTacheId = BasicCode.WHOLE_COURSE_TEST;
                        }
                    } else if (BasicCode.LOCAL_INSIDE_NEWOPEN_FLOW.equals(psId)) { // 本地局内电路新开、变更流程
                        toTacheId = BasicCode.UNION_DEBUG_TEST;
                    }
                    for (int i = 0; i < flowRollBackReasonDTOs.size(); i++) {
                        FlowRollBackReasonDTO _flowRollBackReasonDTO = flowRollBackReasonDTOs.get(i);
                        FlowTacheDTO flowTacheDTO = _flowRollBackReasonDTO.getToTache();
                        String _toTacheId = flowTacheDTO.getId();
                        if (toTacheId.equals(_toTacheId)) {
                            flowRollBackReasonDTO = _flowRollBackReasonDTO;
                        }
                    }
                } else if (BasicCode.CROSS_WHOLE_COURDER_TEST.equals(tacheId)) { // 跨域全程调测环节页面退单
                    String areaName = MapUtils.getString(cstOrderDataMap, "REMARK"); // 受理区域
                    // 判断页面传过来的az端，是不是需要退单本省，先调用一干接口，成功后如果需要退单本省流程退到本地测试，
                    // 如果不需要将工单状态修改为等一干通知，等一干通知跨域全程调测可以回单
                    String provinces = MapUtils.getString(params, "province");
                    String province = "";
                    if (!"".equals(provinces)){
                        province = provinces.replace("[", "")
                                .replace("]", "")
                                .replace("\"","");
                    }
                    Map<String, Object> backMap = new HashMap<String, Object>();
                    backMap.put("SrvOrdId", srvOrdId);
                    backMap.put("provinceA", "无");
                    backMap.put("provinceZ", province);
                    backMap.put("username", MapUtils.getString(operStaffInfoMap, "USER_NAME"));
                    backMap.put("mobiletel", MapUtils.getString(operStaffInfoMap, "USER_PHONE"));
                    Map<String, Object> ifFromSecondaryMap = orderQrySecondaryDao.qryIfFromSecondary(MapUtils.getString(params, "orderId"));
                    if(MapUtils.isEmpty(ifFromSecondaryMap)){
                        //一干直接下发本地网跨域流程
                        Map<String, Object> reportResMap = feedbackInterface.soSendBack(backMap);
                        if ("fail".equals(MapUtils.getString(reportResMap, "flag"))) {
                            resMap.put("success", false);
                            resMap.put("message", "调一干退单接口失败!" + MapUtils.getString(reportResMap, "msg"));
                            return resMap;
                        }else {
                            if (province.indexOf(areaName) != -1) {
                                toTacheId = BasicCode.LOCAL_TEST;
                                for (int i = 0; i < flowRollBackReasonDTOs.size(); i++) {
                                    FlowRollBackReasonDTO _flowRollBackReasonDTO = flowRollBackReasonDTOs.get(i);
                                    FlowTacheDTO flowTacheDTO = _flowRollBackReasonDTO.getToTache();
                                    String _toTacheId = flowTacheDTO.getId();
                                    if (toTacheId.equals(_toTacheId)) {
                                        flowRollBackReasonDTO = _flowRollBackReasonDTO;
                                    }
                                }
                            } else {
                                orderDealDao.updateWoStateByWoId(woId, OrderTrackOperType.WO_ORDER_STATE_11);
                                resMap.put("success", true);
                                resMap.put("message", "通知一干退单成功！" + MapUtils.getString(reportResMap, "msg"));
                                return resMap;
                            }
                        }
                    }else {
                        /**
                         * 如果是二干下发的单子，跨域全程调测退单时，也退单二干的全程调测环节
                         */
                        List<HashMap<String, String>> operAttrsList = new ArrayList<HashMap<String, String>>();
                        HashMap<String, String> operAttrsMap = new HashMap<String, String>();
                        if ("onedry".equals(MapUtils.getString(ifFromSecondaryMap,"RESOURCES"))) {
                            Map<String, Object> secAllTestOrderMap = orderQrySecondaryDao.qrySecAllTestOrder(MapUtils.getString(params, "orderId"));
                            /**
                             *  如果是一干下发二干，二干下发本地网，跨域全程调测退单，需要调用一干退单接口
                             *  1,,先调用一干接口
                             *  2,判断页面传过来的az端，是不是需要退单本省，
                             *    是：退单本省二干流程修改全程调测环节的状态，退单到完工汇总，跨域全程调测等待二干通知
                             *    否：将工单状态修改为等一干通知，等一干通知跨域全程调测可以回单
                             */
                            Map<String, Object> reportResMap = feedbackInterface.soSendBack(backMap);
                            if ("fail".equals(MapUtils.getString(reportResMap, "flag"))) {
                                resMap.put("success", false);
                                resMap.put("message", "调一干退单接口失败!" + MapUtils.getString(reportResMap, "msg"));
                                return resMap;
                            } else {
                                if (province.indexOf(areaName) != -1) {
                                    //orderDealDao.updateWoStateByWoId(MapUtils.getString(secAllTestOrderMap,"WO_ID"),OrderTrackOperType.WO_ORDER_STATE_2);
                                    orderDealDao.updateWoStateAndUserByWoId(MapUtils.getString(secAllTestOrderMap,"WO_ID"),operStaffId,OrderTrackOperType.WO_ORDER_STATE_2);
                                    FlowWoDTO woDTO = new FlowWoDTO();
                                    woDTO.setWoId(MapUtils.getString(secAllTestOrderMap,"WO_ID"));
                                    orderDealDao.updateWoStateByWoId(woId, OrderTrackOperType.WO_ORDER_STATE_13);
                                    operAttrsMap.put("KEY", "COMMISSIONING_RESULT");
                                    operAttrsMap.put("VALUE", "1");
                                    operAttrsList.add(operAttrsMap);
                                    woDTO.setOperAttrs(operAttrsList);
                                    flowActionHandler.complateWo(operStaffId, woDTO);
                                    resMap.put("success", true);
                                    resMap.put("message", "回退成功");
                                    return resMap;
                                } else {
                                    orderDealDao.updateWoStateAndUserByWoId(MapUtils.getString(secAllTestOrderMap,"WO_ID"),operStaffId,OrderTrackOperType.WO_ORDER_STATE_11);
                                    orderDealDao.updateWoStateByWoId(woId, OrderTrackOperType.WO_ORDER_STATE_11);
                                    resMap.put("success", true);
                                    resMap.put("message", "通知一干退单成功！" + MapUtils.getString(reportResMap, "msg"));
                                    return resMap;
                                }
                            }

                        }else if("secondary".equals(MapUtils.getString(ifFromSecondaryMap,"RESOURCES"))
                                ||"jike".equals(MapUtils.getString(ifFromSecondaryMap,"RESOURCES"))){
                            /**
                             *  如果是二干起单下发本地网，跨域全程调测退单，
                             */
                            params.put("operStaffInfoMap", operStaffInfoMap);
                            params.put("operStaffId", operStaffId);
                            params.put("operStaffName", operStaffName);
                            Map<String, Object> crossTestRollBackMap = localAndSecondMutualServiceInf.crossTestRollBack(params);
                            if (MapUtils.getBoolean(crossTestRollBackMap, "success")){
                                crossTestRollBackMap.put("message", "跨域全程调测退单成功!");
                            }
                            return crossTestRollBackMap;
                            /*String dataMakeStr = MapUtils.getString(params, "dataMakeData");
                            String subLocalTestData = MapUtils.getString(params, "subLocalTestData");
                            dataMakeSubProRollBack("rollBackWoOrder", dataMakeStr, remark);
                            fullCommissSecondRollBack("rollBackWoOrder", dataMakeStr, remark, srvOrdId);
                            rentCrossToTestLocalRollBack("rollBackWoOrder", subLocalTestData, remark, woId);
                            lock = null;
                            resMap.put("success", true);
                            resMap.put("message", "回退成功!");*/

                        }
                    }

                } else if (BasicCode.RES_ALLOCATE.equals(tacheId) || BasicCode.FIBER_RES_ALLOCATE.equals(tacheId)) { // 子流程第一个环节回退调撤销
                    params.put("systemResource",systemResource);
                    params.put("operStaffId",operStaffId);
                    resMap = rollBackChildFlow(params);
                    if(!MapUtils.getBoolean(resMap,"success")){
                        return resMap;
                    }
                } else {
                    flowRollBackReasonDTO = flowRollBackReasonDTOs.get(0);
                    // 回退修改之前入库的派单对象的状态
                    //orderDealDao.updateDispObjState(orderId);
                }
            }
            lock.lock();
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>加锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            Map<String, Object> woOrderState = orderDealDao.qryWoOrderState(woId);
            if (OrderTrackOperType.WO_ORDER_STATE_2.equals(MapUtils.getString(woOrderState, "WO_STATE"))) {
                Date rollWoStart = new Date();
                logger.info("========流程平台flowActionHandler.rollBackWo-开始:"+rollWoStart);
                flowActionHandler.rollBackWo(operStaffId, woId, flowRollBackReasonDTO, remark);
                Date rollWoEnd = new Date();
                long be=rollWoEnd.getTime()-rollWoStart.getTime();
                logger.info("========流程平台flowActionHandler.rollBackWo-结束:"+rollWoEnd);
                logger.info("========流程平台flowActionHandler.rollBackWo耗时:"+be+"微秒");
                long a=between + be;
                logger.info("========流程平台退单总耗时:"+a+"微秒");
            }
            Map<String, Object> logDataMap = new HashMap<String, Object>();
            logDataMap.put("woId", woId);
            logDataMap.put("orderId", orderId);
            logDataMap.put("remark", remark);
            logDataMap.put("tacheId", tacheId);
            logDataMap.put("operType", OrderTrackOperType.OPER_TYPE_5);
            logDataMap.put("operStaffInfoMap", operStaffInfoMap);
            logDataMap.put("action", "退单");
            logDataMap.put("trackMessage", "[退单]");
            tacheDealLogIntf.addTrackLog(logDataMap);
            /*Map<String, Object> operLogMap = new HashMap<String, Object>();
            operLogMap.put("orderId", orderId);
            operLogMap.put("woOrdId", woId);
            operLogMap.put("trackOrgId", MapUtils.getString(operStaffInfoMap, "ORG_ID"));
            operLogMap.put("trackOrgName", MapUtils.getString(operStaffInfoMap, "ORG_NAME"));
            operLogMap.put("trackDate", new java.sql.Date(new java.util.Date().getTime()));
            operLogMap.put("createDate", new java.sql.Date(new java.util.Date().getTime()));
            operLogMap.put("trackStaffId", operStaffId);
            operLogMap.put("trackStaffName", operStaffName);
            operLogMap.put("trackStaffPhone", MapUtils.getString(operStaffInfoMap, "USER_PHONE"));
            operLogMap.put("trackStaffEmail", MapUtils.getString(operStaffInfoMap, "USER_EMAIL"));
            String trackMessage = "[" + operStaffName + "将工单单号：" + woId + "][退单]";
            operLogMap.put("trackMessage", trackMessage);
            operLogMap.put("trackContent", "[退单]" + remark);
            operLogMap.put("operType", OrderTrackOperType.OPER_TYPE_5);
            orderDealDao.insertTrackLogInfo(operLogMap);*/
            resMap.put("success", true);
            resMap.put("message", "回退成功!");
            /**
             * 资源配置完提交后将进行流程及子流程消息推送操作 需要工单ID，消息推送人，推送内容
             * @author wangsen
             * @date 2020/9/28 19:50
             * @param params  产品类型，订单ID， 工单ID，环节处理人员、环节处理专业，推送内容
             */
            tacheDealLogIntf.writeOrderMessage(orderId, woId, "退单","childFlow","");
        }
        catch (Exception e) {
            e.printStackTrace();
            resMap.put("success", false);
            resMap.put("message", "回退失败!" + e);
        } finally {
            if(lock!=null){
                lock.unlock();
            }
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>finally主动解锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        }
        return resMap;
    }

    /**
     * 子流程第一个环节回退调撤销
     * @return
     */
    public Map<String,Object> rollBackChildFlow(Map<String,Object> params){
        Map<String, Object> resMap = new HashMap<String, Object>();
        try{
            String woId = MapUtils.getString(params, "woId");
            String orderId = MapUtils.getString(params, "orderId");
            String tacheId = MapUtils.getString(params, "tacheId");
            String remark = MapUtils.getString(params, "remark","");
            String operStaffId = MapUtils.getString(params, "operStaffId");
            String srvOrdId = MapUtils.getString(params, "srvOrdIdFullCom");
            String relateInfoId = MapUtils.getString(params, "relateInfoId");
            Map<String, Object> param = new HashMap<String, Object>();
            // 二干的单子先将订单id转化为关联表gom_bdw_sec_local_relate_info的id
            if (BasicCode.SECOND.equals(MapUtils.getString(params, "systemResource",""))){
                srvOrdId = relateInfoId;
                params.put("srvOrdId",relateInfoId);
            }
            // 调用资源回滚接口
            List orderIdList = new ArrayList();
            orderIdList.add(orderId);
            param.put("srvOrdId", MapUtils.getString(params, "srvOrdId"));// gom_bdw_srv_ord_info.srv_ord_id
            param.put("orderIds", orderIdList);// orderIds是个List, 存放的是 子流程的gom_order.order_id
            param.put("rollbackDesc", "子流程撤单"); // 回滚原因
            Map retmap = businessRollbackServiceIntf.resRollBack(param);
            if (!MapUtils.getBoolean(retmap, "success")) {
                resMap.put("success", false);
                resMap.put("message", "回退失败!" + MapUtils.getString(retmap, "message"));
                return resMap;
            }

            /**
             * 如果资源配置改过电路编号SOURSE='res'
             * 如果资源返回的电路编号是空，并且资源资源配置改过电路编号，那么更新电路编号为空
             */
            orderDealDao.updateCircuitCode(srvOrdId);
            boolean flagCancelOrder = flowActionHandler.cancelOrder(operStaffId, orderId);
            if (flagCancelOrder) {
                // 调用撤单成功，将当前工单状态修改成作废
                Map<String, Object> updateMap = new HashMap<String, Object>();
                updateMap.put("woState", "290000005");
                updateMap.put("stateDate", new java.sql.Date(new java.util.Date().getTime()));
                updateMap.put("woID", woId);
                updateMap.put("staffId", operStaffId);
                orderDealDao.updateWoOrderState(updateMap);
                // 1.查询保存的专业区域信息去掉当前回滚的信息
                Map<String, Object> dispInfo = orderDealDao
                        .qryDispInfoBySrvordInfo(MapUtils.getString(params, "srvOrdIdFullCom"), orderId);
                Map<String,Object> flowSpecialtyData = (Map<String, Object>)JSONObject.parse(MapUtils.getString(dispInfo,"FLOW_SPECIALTY_DATA"));
                Map<String,Object> childFlowSpecial = (Map<String, Object>)JSONObject.parse(MapUtils.getString(flowSpecialtyData,"childFlowSpecial"));
                Map<String,Object> specInfo = MapUtils.getMap(flowSpecialtyData,"childFlowSpecialArea");
                Map<String,Object> specInfoName = MapUtils.getMap(flowSpecialtyData,"childFlowSpecialAreaName");
                // 2.查询当前子流程的区域信息 REGION_ID,SPECIALTY_CODE
                Map<String,Object> childOrdDispInfo = orderDealDao.qryChildOrdDispInfo(woId);
                // 3.去掉当前的子流程区域信息
                String specCode = MapUtils.getString(childOrdDispInfo,"SPECIALTY_CODE","");
                String regionId = MapUtils.getString(childOrdDispInfo,"REGION_ID","");
                if(specInfo.containsKey(specCode)){
                    String areaIdsStr = MapUtils.getString(specInfo,specCode,"");
                    if(regionId.equals(areaIdsStr)){
                        specInfo.remove(specCode);
                        specInfoName.remove(specCode);
                        childFlowSpecial.put(specCode,"NULL");
                    } else{
                        String[] areaIds = areaIdsStr.split(",");
                        String s = regionId;
                        Map<String,Object> deptInfo = orderDealDao.queryDeptIdByParentDeptId(regionId);
                        String deptName = MapUtils.getString(deptInfo,"DEPT_NAME");
                        for ( int i = 0; i < areaIds.length; i ++){
                            if(areaIds[i].equals(regionId)){
                                if(i==0){
                                    s = regionId + ",";
                                    deptName = deptName + ",";

                                } else{
                                    s = "," + regionId;
                                    deptName = "," + deptName;
                                }
                            }
                        }
                        specInfo.put(specCode,areaIdsStr.replace(s,""));
                        specInfoName.put(specCode, MapUtils.getString(specInfoName,specCode).replace(deptName,""));
                        childFlowSpecial.put(specCode,MapUtils.getString(childFlowSpecial,specCode).replace(s,""));
                        flowSpecialtyData.put("childFlowSpecial",childFlowSpecial);
                        flowSpecialtyData.put("childFlowSpecialArea",specInfo);
                        flowSpecialtyData.put("childFlowSpecialAreaName",specInfoName);
                        dispInfo.put("SPECIALTY_INFO",specInfo);
                        dispInfo.put("SPECIALTY_INFO_NAME",specInfoName);
                    }
                }
                boolean flag = specInfo == null || (specInfo!=null && specInfo.keySet().size()<1);
                // 4.更新最新的区域信息入库,如果更新后是空的，那么删除该记录，同时更改电路表状态
                if(flag){
                    // 删除记录
                    orderDealDao.deleteConfigById(MapUtils.getString(dispInfo,"ID",""));
                    // 更新订单业务表中的状态为未配置
                    Map<String,Object> paramMap = new HashMap<String,Object>();
                    // 本地单子改订单表状态
                    paramMap.put("srvOrdId",srvOrdId);
                    paramMap.put("state","10E");// 代表未配置
                    if (BasicCode.SECOND.equals(MapUtils.getString(params, "systemResource", ""))) {
                        orderDealDao.updateSecondSrvStateById(paramMap);
                    } else {
                        orderDealDao.updateSrvStateBySrvOrdId(paramMap);
                    }
                } else {
                    // 更新记录
                    dispInfo.put("SPECIALTY_INFO",JSONObject.toJSON(specInfo).toString());
                    dispInfo.put("SPECIALTY_INFO_NAME",JSONObject.toJSON(specInfoName).toString());
                    dispInfo.put("FLOW_SPECIALTY_DATA",JSONObject.toJSON(flowSpecialtyData).toString());
                    orderDealDao.updateConfigById(dispInfo);
                }
            }
            resMap.put("success", true);
            resMap.put("message", "回退成功!");
            return resMap;
        }catch  (Exception e) {
            e.printStackTrace();
            resMap.put("success", false);
            resMap.put("message", "回退失败!" + e);
            return resMap;
        }
    }

    @Override
    public void dataMakeSubProRollBack(String action, String dataMakeStr, String remark) {
        List<Object> dataMakeList = Lists.newArrayList();
        //二干数据制作子流程退单：从专业数据制作完成退到专业数据制作环节
        if (!StringUtils.isEmpty(dataMakeStr)) {
            dataMakeList = JSON.parseArray(dataMakeStr);
            UserInfo user = ThreadLocalInfoHolder.getLoginUser();
            for(Object obj : dataMakeList){
                Map<String, Object> dataMakeMap = (Map<String, Object>) obj;
                String woId = MapUtils.getString(dataMakeMap, "WO_ID");
                String dealUserId = MapUtils.getString(dataMakeMap, "DEAL_USER_ID");
                String pubDateName = MapUtils.getString(dataMakeMap, "PUB_DATE_NAME");
                String orderId = MapUtils.getString(dataMakeMap, "ORDER_ID");
                Map<String, Object> operStaffInfoMap = new HashMap<String, Object>();
                if(StringUtils.isEmpty(dealUserId)){
                    dealUserId = user.getUserId();
                }
                operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(dealUserId));
                DistributeLock lock = new DatabaseLock(woId);
                try{
                    //回退线退单
                    FlowWoDTO woDTO = new FlowWoDTO();
                    woDTO.setWoId(woId);
                    List<HashMap<String, String>> operAttrsList = new ArrayList<HashMap<String, String>>();
                    HashMap<String, String> operAttrsMap = new HashMap<String, String>();
                    operAttrsMap.put("KEY", "SPECIALTY_DATA_PRODUCTION_CHECK");
                    operAttrsMap.put("VALUE", "1");
                    operAttrsList.add(operAttrsMap);
                    woDTO.setOperAttrs(operAttrsList);
                    flowActionHandler.complateWo(dealUserId, woDTO);
                    lock.lock();
                }catch (Exception e){
                    throw e;
                }finally {
                    lock.unlock();
                }
                /*try{
                    String operStaffName = MapUtils.getString(operStaffInfoMap, "USER_REAL_NAME");
                    String trackMessage = "[" + operStaffName + "将工单单号：" + woId + "][退单]";
                    insertTrackLogInfo(orderId, woId, operStaffInfoMap, dealUserId, operStaffName, trackMessage,
                        remark);
                }catch (Exception e){
                    throw e;
                }*/
            }

        }
    }

    @Override
    public void rentCrossToTestLocalRollBack(String action, String subLocalTestData, String remark,String woIdC) {
        List<Object> subLocalTestDataList = Lists.newArrayList();
        Boolean isFullCrossWholeTest = true;
        if (!StringUtils.isEmpty(subLocalTestData)) {
            subLocalTestDataList = JSON.parseArray(subLocalTestData);
            UserInfo user = ThreadLocalInfoHolder.getLoginUser();
            for(Object obj : subLocalTestDataList){
                Map<String, Object> subLocalMap = (Map<String, Object>) obj;
                String woId = MapUtils.getString(subLocalMap, "WOID");
                String dealUserId = MapUtils.getString(subLocalMap, "DEAL_USER_ID");
                String tacheName = MapUtils.getString(subLocalMap, "TACHENAME");
                String orderId = MapUtils.getString(subLocalMap, "ORDERID");
                String tacheId = MapUtils.getString(subLocalMap, "TACHEID"); //本地网辅调局起租、主调局跨域全程调测环节Id
                if(woIdC.equals(woId)){
                    isFullCrossWholeTest = false;
                }
                Map<String, Object> operStaffInfoMap = new HashMap<String, Object>();
                if(StringUtils.isEmpty(dealUserId)){
                    dealUserId = user.getUserId();
                }
                operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(dealUserId));
                DistributeLock lock = new DatabaseLock(woId);
                try{
                    //异常单退单
                    List<FlowRollBackReasonDTO> flowRollBackReasonDTOs = flowActionHandler.queryRollBackReasons(woId);
                    FlowRollBackReasonDTO flowRollBackReasonDTO = null;
                    for (int i = 0; i < flowRollBackReasonDTOs.size(); i++) {
                        FlowRollBackReasonDTO _flowRollBackReasonDTO = flowRollBackReasonDTOs.get(i);
                        FlowTacheDTO flowTacheDTO = _flowRollBackReasonDTO.getToTache();
                        String _toTacheId = flowTacheDTO.getId();
                        if (BasicCode.LOCAL_TEST.equals(_toTacheId)) {
                            flowRollBackReasonDTO = _flowRollBackReasonDTO;
                        }
                    }
                    flowActionHandler.rollBackWo(dealUserId, woId, flowRollBackReasonDTO, remark);
                    lock.lock();
                }catch (Exception e){
                    throw e;
                }finally {
                    lock.unlock();
                }
                try{
                    /*String operStaffName = MapUtils.getString(operStaffInfoMap, "USER_REAL_NAME");
                    String trackMessage = "[" + operStaffName + "将工单单号：" + woId + "][退单]";
                    insertTrackLogInfo(orderId, woId, operStaffInfoMap, dealUserId, operStaffName, trackMessage,remark);*/
                    Map<String, Object> logDataMap = new HashMap<String, Object>();
                    logDataMap.put("woId", woId);
                    logDataMap.put("orderId", orderId);
                    logDataMap.put("remark", remark);
                    logDataMap.put("tacheId", tacheId);
                    logDataMap.put("operType", OrderTrackOperType.OPER_TYPE_5);
                    logDataMap.put("operStaffInfoMap", operStaffInfoMap);
                    logDataMap.put("action", "退单");
                    logDataMap.put("trackMessage", "[退单]");
                    tacheDealLogIntf.addTrackLog(logDataMap);
                }catch (Exception e){
                    throw e;
                }
            }

        }
        if(isFullCrossWholeTest){
            orderDealDao.updateWoStateByWoId(woIdC, OrderTrackOperType.WO_ORDER_STATE_15);
        }

    }

    /*@Override
    public void insertTrackLogInfo(String orderId, String woId, Map<String, Object> operStaffInfoMap, String dealUserId, String operStaffName, String trackMessage, String remark) {
        Map<String, Object> operLogMap = new HashMap<String, Object>();
        operLogMap.put("orderId", orderId);
        operLogMap.put("woOrdId", woId);
        operLogMap.put("trackOrgId", MapUtils.getString(operStaffInfoMap, "ORG_ID"));
        operLogMap.put("trackOrgName", MapUtils.getString(operStaffInfoMap, "ORG_NAME"));
        operLogMap.put("trackDate", new java.sql.Date(new java.util.Date().getTime()));
        operLogMap.put("createDate", new java.sql.Date(new java.util.Date().getTime()));
        operLogMap.put("trackStaffId", dealUserId);
        operLogMap.put("trackStaffName", operStaffName);
        operLogMap.put("trackStaffPhone", MapUtils.getString(operStaffInfoMap, "USER_PHONE"));
        operLogMap.put("trackStaffEmail", MapUtils.getString(operStaffInfoMap, "USER_EMAIL"));
        operLogMap.put("trackMessage", trackMessage);
        operLogMap.put("trackContent", "[退单]" + remark);
        operLogMap.put("operType", OrderTrackOperType.OPER_TYPE_5);
        orderDealDao.insertTrackLogInfo(operLogMap);

    }*/

    /**
     * 派发工单
     *
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> sendWoOrder(Map<String, Object> params) {
        logger.info(">>>>>>>>>>>>>>>>>提交工单>>>>>>>>>>>>>>>>>>>>>>>");
        Map<String, Object> resMap = new HashMap<String, Object>();
        Map<String, Object> resMapOperArr = new HashMap<String, Object>(); //保存前台传过来的流程参数
        String tacheId = MapUtils.getString(params, "tacheId");
        String orderIdMain = MapUtils.getString(params, "orderId");
        String srvOrdId = MapUtils.getString(params, "srvOrdId");
        String srvOrdState = orderDealDao.qrySrvOrdStat(srvOrdId);
        if(srvOrdState != null &&"4E".equals(srvOrdState)){
            resMap.put("success", false);
            resMap.put("message", "电路已被挂起，不能派单!");
            return resMap;
        }
        //查询单子属于什么系统
        Map<String, Object> belongSysMap = orderQrySecondaryDao.qrySrvOrderBelongSys(params);
        String systemResource = MapUtils.getString(belongSysMap, "SYSTEM_RESOURCE");
        String srvOrderIdRes = "";
        String flagSys = BasicCode.LOCALBUILD;
        if (BasicCode.SECOND.equals(systemResource)){
            srvOrderIdRes = MapUtils.getString(params, "relateInfoId");
            flagSys = BasicCode.SECONDARY;
        }else {
            srvOrderIdRes = srvOrdId;
            flagSys = BasicCode.LOCALBUILD;
        }
        String operStaffId = "";
        try {
            Map<String, Object> operStaffInfoMap = new HashMap<String, Object>();
            if (BasicCode.RENT.equals(tacheId)) { // 起租环节是一干来的
                operStaffId = "-1";
                operStaffInfoMap.put("ORG_ID", "-1");
                operStaffInfoMap.put("ORG_NAME", "一干");
                operStaffInfoMap.put("USER_PHONE", "-1");
                operStaffInfoMap.put("USER_EMAIL", "-1");
                operStaffInfoMap.put("USER_REAL_NAME", "一干");
                operStaffInfoMap.put("USER_ID", operStaffId);
            } else if(params.containsKey("operStaffId")&& !"".equals(MapUtils.getString(params,"operStaffId",""))){
                operStaffId = MapUtils.getString(params,"operStaffId");
                operStaffInfoMap = orderDealDao.getOperStaffInfo(MapUtils.getInteger(params,"operStaffId"));
            }else {
                operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
                operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(operStaffId));
            }
            String woId = MapUtils.getString(params, "woId");
            // 获取环节操作信息 调单信息
            Map<String, Object> tacheOperInfo = MapUtils.getMap(params, "tacheOperInfo");
            String remark = MapUtils.getString(tacheOperInfo, "remark","");
            String opinion = MapUtils.getString(params, "opinion");
            String operFlag = "";
            // 本地测试/联调测试环节退单原因入库日志表
            if (StringUtils.isEmpty(remark)){
                if (BasicCode.LOCAL_TEST.equals(tacheId) || BasicCode.UNION_DEBUG_TEST.equals(tacheId)) { // 本地测试 联调测试
                    operFlag = MapUtils.getString(tacheOperInfo, "operFlag");
                    remark = "环节操作说明信息：测试结果" + operFlag;
                }
            } else{
                remark = "环节操作说明信息：" + remark;
            }
            // 线条判断参数 operAttrs 获取页面传过来的线条参数
            Map<String, String> paramsMap = null;
            if (BasicCode.RENT.equals(tacheId)) { // 起租环节获取一干来的参数
                if (params.containsKey("operAttrsVal")){
                    paramsMap = MapUtils.getMap(params, "operAttrsVal");
                }
            } else if (BasicCode.CHECK_DISPATCH.equals(tacheId)) { // 核查调度 需要动态调度查询入库配置
                // isThoughCheckTatal 核查环节不选择专业
                Map<String, String> temp = MapUtils.getMap(params, "operAttrsVal");
                boolean isThoughCheckTatal = temp != null && temp.keySet().contains("isThoughCheckTatal") && "0".equals(MapUtils.getString(temp, "isThoughCheckTatal"));
                if (!isThoughCheckTatal){
                    // 默认值1，代表电路调度配置的专业区域信息，如果不传值就是默认
                    String newCreateResource = "1";
                    if (params.keySet().contains("newCreateResource")) {
                        // 0代表电路调度环节保存的新建资源录入派发区域信息
                        if ("0".equals(org.apache.commons.collections.MapUtils.getString(params, "newCreateResource"))) {
                            newCreateResource = org.apache.commons.collections.MapUtils.getString(params,
                                    "newCreateResource");
                        }
                    }
                    //String newCreateResource = MapUtils.getString(params, "newCreateResource");
                    Map<String, Object> checkFlowParams = orderDealDao.getDispSpecialtyObjByRes(srvOrdId,orderIdMain,newCreateResource);
                    Object flowDispObject = MapUtils.getObject(checkFlowParams, "FLOW_SPECIALTY_DATA");
                    Map<String, String> flowSpecialtyData = JSON.parseObject(flowDispObject.toString(), Map.class);
                    Map<String, String> flowAreaSpecial = MapUtils.getMap(flowSpecialtyData, "checkAreaSpecialArea");
                    //过滤核查单发过的区域环节工单
                    Iterator<Map.Entry<String, String>> areaSpecial = flowAreaSpecial.entrySet().iterator();
                    while (areaSpecial.hasNext()) {
                        Map.Entry<String, String> entry = areaSpecial.next();
                        String keyName = entry.getKey();
                        String tacheCodeFlag = "";
                        String sendTacheFlag = "";
                        if (keyName.equals("transOrg")){
                            tacheCodeFlag = "TRANS_CHECK";
                            sendTacheFlag = "isTransCheck";
                        } else if (keyName.equals("outsideOrg")){
                            tacheCodeFlag = "OUTSIDElINE_CHECK";
                            sendTacheFlag = "isOutsideCheck";
                        } else if (keyName.equals("dataOrg")){
                            tacheCodeFlag = "DATA_CHECK";
                            sendTacheFlag = "isDataCheck";
                        } else if (keyName.equals("accessOrg")){
                            tacheCodeFlag = "ACCESS_CHECK";
                            sendTacheFlag = "isAccessCheck";
                        } else if (keyName.equals("otherOrg")){
                            tacheCodeFlag = "OTHER_CHECK";
                            sendTacheFlag = "isOtherCheck";
                        } else if (keyName.equals("changeOrg")){
                            tacheCodeFlag = "CHANGE_CHECK";
                            sendTacheFlag = "isChangeCheck";
                        }
                        String areaIdStrs = String.valueOf(entry.getValue());
                        String[] areaIdStr = areaIdStrs.split(",");
                        ArrayList<String> areaIdList = new ArrayList<>(Arrays.asList(areaIdStr));
                        Iterator<String> areaIdObj = areaIdList.iterator();
                        while(areaIdObj.hasNext()){
                            String areaIdFlow = areaIdObj.next();
                            int specialtyCheckDoingNum = checkOrderServiceIntf.qrySpecialtyCheckDoing(orderIdMain, tacheCodeFlag, areaIdFlow);
                            if (specialtyCheckDoingNum > 0){
                                areaIdObj.remove();
                            }
                        }
                        if (ListUtil.isEmpty(areaIdList)){
                            //如果是空，修改线条参数
                            flowSpecialtyData.put(sendTacheFlag, "1");
                        }else {
                            //将删除后的数据重新赋值
                            //areaIdStr = areaIdList.toArray(new String[areaIdList.size()]);
                            flowAreaSpecial.put(keyName, areaIdList.toString().replace("[", "").replace("]", "").replace(" ",""));
                        }
                    }
                    flowSpecialtyData.remove("checkAreaSpecialArea");
                    flowSpecialtyData.remove("checkAreaSpecialAreaName");
                    paramsMap = flowSpecialtyData;
                    paramsMap.putAll(flowAreaSpecial);
                    if (!params.containsKey("checkAddOrder")){ //非补单情况
                        String maxWoId = checkOrderServiceIntf.qryMaxWoId(orderIdMain);
                        if (!woId.equals(maxWoId)){
                            params.put("checkDispatchOrder", true); //控制回单
                        }
                    }
                } else {
                    //不需要专业核查
                    Object operAttrsObject = MapUtils.getObject(params, "operAttrsVal");
                    paramsMap = JSON.parseObject(operAttrsObject.toString(), Map.class);
                }
                //查询是否成功调过资源创建接口
                int num = orderDealDao.qryInterResult(srvOrderIdRes, "ResBusinessCreate");
                if (num < 1) {
                    // 电路调度环节调用资源创建接口
                    Map<String, Object> resConfigParams = new HashMap<String, Object>();
                    resConfigParams.put("flag", flagSys);
                    resConfigParams.put("srvOrdId", srvOrderIdRes);
                    resConfigParams.put("srvOrdIdOld", srvOrdId);
                    // 添加单据来源 modify by wang.g2
                    resConfigParams.put("resFullCom", MapUtils.getString(params,"resFullCom"));
                    Map retmap = businessCreateServiceIntf.businessCreate(resConfigParams);
                    if (!"成功".equals(MapUtils.getString(retmap, "returncode"))) {
                        resMap.put("success", false);
                        resMap.put("message", "派单失败!调用资源创建接口异常，异常原因：" + MapUtils.getString(retmap, "returndec"));
                        return resMap;
                    }
                }
            } else {
                Object operAttrsObject = MapUtils.getObject(params, "operAttrsVal");
                paramsMap = JSON.parseObject(operAttrsObject.toString(), Map.class);
            }
            // 电路调度环节
            if (BasicCode.CIRCUIT_DISPATCH.equals(tacheId)) {
                //电路调度选择新建资源，不要调用创建实例接口
                /*
                 * if (paramsMap.containsKey("isNeedNewRes")){ String isNeedNewRes = MapUtils.getString(paramsMap,
                 * "isNeedNewRes"); if ("1".equals(isNeedNewRes)){ } }
                 */
                // 查询是否成功调过资源创建接口
                int num = orderDealDao.qryInterResult(srvOrderIdRes, "ResBusinessCreate");
                if (num < 1) {
                    // 电路调度环节调用资源创建接口
                    Map<String, Object> resConfigParams = new HashMap<String, Object>();
                    resConfigParams.put("flag", flagSys);
                    resConfigParams.put("srvOrdId", srvOrderIdRes);
                    resConfigParams.put("srvOrdIdOld", srvOrdId);
                    //  添加单据来源2
                    resConfigParams.put("resFullCom", MapUtils.getString(params,"resFullCom"));
                    Map retmap = businessCreateServiceIntf.businessCreate(resConfigParams);
                    if (!"成功".equals(MapUtils.getString(retmap, "returncode"))) {
                        resMap.put("success", false);
                        resMap.put("message", "派单失败!调用资源创建接口异常，异常原因：" + MapUtils.getString(retmap, "returndec"));
                        return resMap;
                    }
                }
                //补充电路信息
                //insertAZResources(params); //A/Z端资源是否具备
                insertAccessType(params); //接入类型
                insertStandardAddress(params); //商务专线补充标准地址
            }
            String childoOrderId = orderDealDao.qryOrderIdByWoId(woId);
            if (BasicCode.RES_ALLOCATE.equals(tacheId)){//资源分配环节
                //保存可选是否资源施工，因为资源施工在数据制作上已经有线条参数了，这里为了区分选择了数据库保存标识符
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置日期格式
                String isNeedResConstructRes = MapUtils.getString(params, "isNeedResConstructRes");
                Map<String, Object> rmap = new HashMap<String, Object>();
                rmap.put("srv_ord_id", srvOrdId);
                rmap.put("attr_code", "isResConstruct");
                rmap.put("attr_name",childoOrderId);
                rmap.put("attr_value", isNeedResConstructRes);//0是 1否
                rmap.put("create_date", df.format(new Date()));
                rmap.put("sourse", "");
                rmap.put("attr_action", "0");
                rmap.put("attr_value_name", "是否需要资源施工");
                webServiceDao.delAttrBySrvOrdIdAndCode(srvOrdId,childoOrderId,"isResConstruct");
                webServiceDao.saveRetInfo(rmap);
            }
            if(BasicCode.FIBER_RES_ALLOCATE.equals(tacheId)){
                String isNeedResConstructResOutside = MapUtils.getString(params, "isNeedResConstructResOutside");
                if("1".equals(isNeedResConstructResOutside)){//不需要外线施工
                    paramsMap.put("isNeedResConstructResOutside", "1");
                }else{
                    paramsMap.put("isNeedResConstructResOutside", "0");
                }
            }
            Map<String, Object> orderDataMap = orderDealDao.qryOrderData(woId);
            String psId = MapUtils.getString(orderDataMap, "PSID");
            // 判断产品是否为 MV/DIA/语音中继
            /*
             * String productId = MapUtils.getString(orderDataMap, "SERVICE_ID"); //产品编码 String productName =
             * MapUtils.getString(orderDataMap, "CODE_CONTENT");
             */
            if (BasicCode.LOCAL_CUST_NEWOPEN_FLOW.equals(psId)) { // 本地客户电路 新开变更移机
                String productInfoId = MapUtils.getString(orderDataMap, "CODE_INFO_ID");
                if ("1".equals(productInfoId) || "2".equals(productInfoId) || "8".equals(productInfoId)) {
                    paramsMap.put("ifAllTestRoll", "1");
                    paramsMap.put("isAllTestRollback", "1");
                } else {
                    paramsMap.put("ifAllTestRoll", "0");
                }
            } else if (BasicCode.LOCAL_OTHER_SPECIAL_CHILDFLOW.equals(psId)) { // 如果是子流程
                String orderId = MapUtils.getString(params, "orderId");
                Map<String, Object> psIdDate = orderDealDao.getMainFlowPsId(orderId);
                String psIdFlow = MapUtils.getString(psIdDate, "PSID");
                if (BasicCode.LOCAL_CUST_STOP_FLOW.equals(psIdFlow)) { // 本地电路的停复机
                    paramsMap.put("isNeedResConstruct", "1");
                    paramsMap.put("isDataMakeBack", "1");
                } else {
                    //取资源分配选择的是否资源施工，如果为否，则直接不要
                    String isNeedResConstructRes = orderDealDao.qryAttrValue(srvOrdId, childoOrderId,"isResConstruct");
                    if ("1".equals(isNeedResConstructRes)){
                        paramsMap.put("isNeedResConstruct", "1");
                    }else {
                        paramsMap.put("isNeedResConstruct", "0");
                    }
                    paramsMap.put("isDataMakeBack", "1");                }
            }
            List<HashMap<String, String>> operAttrsList = new ArrayList<HashMap<String, String>>();
            if (MapUtils.isNotEmpty(paramsMap)){
                Iterator<Map.Entry<String, String>> iter = paramsMap.entrySet().iterator();
                while (iter.hasNext()) {
                    HashMap<String, String> operAttrsMap = new HashMap<String, String>();
                    Map.Entry<String, String> e = iter.next();
                    String key = e.getKey();
                    String value = String.valueOf(e.getValue());
                    operAttrsMap.put("KEY", key);
                    operAttrsMap.put("VALUE", value);
                    operAttrsList.add(operAttrsMap);
                    resMapOperArr.put(key,value);
                }
            }
            boolean flag = false;
            // 判断流程是否结束，如果流程结束，修改srv_ord订单状态
            boolean isFlowEnd = false;
            // 完工确认环节 不回退
            if (BasicCode.COMPLETE_CONFIRM.equals(tacheId)) {
                flag = true;
            }
            // TODO 核查汇总环节不进行投资估算时
            if (BasicCode.CHECK_TOTAL.equals(tacheId)) {
                String IsNeedInvestment = MapUtils.getString(paramsMap, "IsNeedInvestment");
                if ("1".equals(IsNeedInvestment)) {
                    flag = true;
                    //二干来单不需要修改状态为10F
                    if(flagSys!=BasicCode.SECONDARY){
                        isFlowEnd = true;
                    }
                    // 判断是否是集客来单
                    //如果为从二干下发的集客来单，汇总环节本地不需要反馈集客，只需二干的核查汇总反馈即可
                    if ("jike".equals(MapUtils.getString(orderDataMap, "RESOURCES"))&&!"second-schedule-lt".equals(MapUtils.getString(orderDataMap, "SYSTEM_RESOURCE"))) {
                        // 集客来单，完工确认环节调用反馈接口
                        int numFinish = orderDealDao.qryInterResult(srvOrdId, "FinishOrder");
                        if (numFinish < 1) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("srvOrdId",srvOrdId);
                            map.put("psId",psId);
                            InterfaceThreadPool.tuneIntfToExecute(interfaceIntf, map);
                            /*Map finMap = finishOrderServiceIntf.finishOrder(map);
                            if (!"1".equals(MapUtils.getString(finMap, "RESP_CODE"))) {
                                resMap.put("success", false);
                                resMap.put("message",
                                        "派单失败!调用集客反馈接口异常，异常原因：" + MapUtils.getString(finMap, "RESP_DESC"));
                                return resMap;
                            }*/
                        }
                    }
                }
            }
            // 跨域电路新开流程 、接入集客后的本地客户新开流程 起租环节
            if (BasicCode.RENT.equals(tacheId)) {
                flag = true;
                isFlowEnd = true;
            }
            if(BasicCode.LOCAL.equals(systemResource)){
                if (flag) {
                    // 查询是否成功调过资源汇总接口
                    int num = orderDealDao.qryInterResult(srvOrderIdRes, "BusinessAutoAssign");
                    if (num < 1) {
                        // 调用业务电路汇总接口(允许多次调用)
                        Map<String, Object> resConfigParams = new HashMap<String, Object>();
                        resConfigParams.put("flag", flagSys);
                        resConfigParams.put("srvOrdId", srvOrderIdRes);
                        resConfigParams.put("relaCrmOrderCodes",new ArrayList<String>());
                        //resConfigParams.put("srvOrdIdOld", srvOrdId);
                        Map retmap = businessAutoAssignServiceIntf.businessAutoAssign(resConfigParams);
                        if (!"成功".equals(MapUtils.getString(retmap, "returncode"))) {
                            resMap.put("success", false);
                            resMap.put("message", "派单失败!调用资源业务电路汇总接口异常，异常原因：" + MapUtils.getString(retmap, "returndec"));
                            return resMap;
                        }
                        // 调用业务汇总接口后，调用资源信息实时查询接口，更新路由等信息
                        Map qryMap = buizQueryOnTimeServiceIntf.buizQueryOnTime(resConfigParams);
                        if (!"成功".equals(MapUtils.getString(qryMap, "returncode"))) {
                            resMap.put("success", false);
                            resMap.put("message",
                                    "派单失败!调用资源信息查询接口异常，异常原因：" + MapUtils.getString(qryMap, "returndec"));
                            return resMap;
                        }
                    }

                    //完工确认环节加入竣工时间
                    String attrCode = MapUtils.getString(params, "ATTR_CODE");
                    if(BasicCode.COMPLETE_CONFIRM.equals(tacheId) && attrCode!=null&&!"".equals(attrCode)){
                        try{
                            String qryFinishTime = insertOrderInfoDao.queryFinishTime(srvOrdId);
                            if( "".equals(qryFinishTime)||qryFinishTime==null){//表中不存在竣工时间的数据，执行插入操作
                                List<Map<String,Object>> cirInfoList = new ArrayList<>();
                                cirInfoList.add(params);
                                querySequence("gom_BDW_srv_ord_attr_info"); //序列刷掉一个，解决第一次提交报违反唯一约束
                                insertOrderInfoDao.insertordAttrInfo(cirInfoList);
                            }else{//执行更新操作
                                String finishTime =MapUtils.getString(params, "ATTR_VALUE");
                                insertOrderInfoDao.updateFinishTime(finishTime,srvOrdId);
                            }
                        }catch (Exception e) {
                            resMap.put("message", "提交失败!保存电路信息失败：" + e.getMessage());
                        }
                    }

                    boolean isEnd = true;
                    // 本地客户流程 查询集客 是否可以下单
                    if (BasicCode.LOCAL_CUST_NEWOPEN_FLOW.equals(psId)
                            || BasicCode.LOCAL_CUST_STOP_FLOW.equals(psId)
                            || BasicCode.LOCAL_CUST_DISMANTLE_FLOW.equals(psId)) {
                        isEnd = false;
                        if ("jike".equals(MapUtils.getString(orderDataMap, "RESOURCES"))) {
                            // 集客来单，完工确认环节调用反馈接口
                            int numFinish = orderDealDao.qryInterResult(srvOrdId, "FinishOrder");
                            if (numFinish < 1) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("srvOrdId",srvOrdId);
                                InterfaceThreadPool.tuneIntfToExecute(interfaceIntf, map);
                                /*Map finMap = finishOrderServiceIntf.finishOrder(map);
                                if (!"1".equals(MapUtils.getString(finMap, "RESP_CODE"))) {
                                    resMap.put("success", false);
                                    resMap.put("message",
                                            "派单失败!调用集客反馈接口异常，异常原因：" + MapUtils.getString(finMap, "RESP_DESC"));
                                    return resMap;
                                }*/
                            }
                        }
                    }
                    // 本地局内电路新开变更流程 ，提交前需调用资源归档接口
                    if (isEnd) {
                        // 查询是否成功调过资源归档接口
                        int numHole = orderDealDao.qryInterResult(srvOrderIdRes, "BusinessArchive");
                        if (numHole < 1) {
                            params.put("flag", flagSys);
                            params.put("srvOrdId", srvOrderIdRes);
                            Map map = businessArchiveServiceIntf.businessArchive(params);
                            if (!"成功".equals(MapUtils.getString(map, "returncode"))) {
                                resMap.put("success", false);
                                resMap.put("message", "派单失败!调用资源归档接口异常，异常原因：" + MapUtils.getString(map, "returndec"));
                                return resMap;
                            }
                        }
                    }
                }
            }
            // 本地局内电路新开、拆机 需求完工归档环节
            if (BasicCode.DEMAND_COMPLETE_FILE.equals(tacheId)) {
                isFlowEnd = true;
            }
            if (isFlowEnd) {
                // srv_ord表状态改为10F
                orderDealDao.updateSrvOrdState(srvOrdId, "10F");
            }
            String cstOrdId = MapUtils.getString(orderDataMap, "CSTORDID");
            //本地测试环节 跨域电路判断是否为主调局，是否有跨域全程调测环节
            if (BasicCode.LOCAL_TEST.equals(tacheId)) {
                if (BasicCode.CROSS_NEWOPEN_FLOW.equals(psId)) { // 跨域电路 判断是否为主调局 是否有全程调测环节
                    String orderIdKy = MapUtils.getString(params, "orderId");
                    Map<String, String> cstOrderDataMap = new HashMap<String, String>();
                    if (BasicCode.SECOND.equals(systemResource)){
                        /**单子属于二干系统
                         * 1，一干下发给二干，二干下发给本地
                         *    先判断一干指定的主调是不是本省：如果是再判断二干指定的主调是不是本区域，如果是有跨域全程调测，如果不是没有跨域全程调测；
                         * 2，二干直接下发给本地
                         *    直接判断二干指定的主调是不是本区域，如果是有跨域全程调测，如果不是没有跨域全程调测；
                         */
                        cstOrderDataMap = orderDealDao.qryCstOrderDataFromSec(orderIdKy);
                        String mainOrg = MapUtils.getString(cstOrderDataMap, "MAINORG"); // 一干指定的主调
                        //String mainOrg_Eg = MapUtils.getString(cstOrderDataMap, "MAINORG_EG"); // 二干指定的主调
                        String ifMainOrgSec = MapUtils.getString(cstOrderDataMap, "IFMAINORG");
                        String resources = MapUtils.getString(cstOrderDataMap, "RESOURCES"); // 数据来源
                        String areaParentId = MapUtils.getString(cstOrderDataMap, "HANDLE_DEP_ID"); // 受理区域id
                        //String secondRegion = orderDealDao.qryOrderMainEgRegion(orderIdKy, mainOrg_Eg);
                        if (BasicCode.ONEDRY.equals(resources)) {
                            if (mainOrg.equals(areaParentId)) {
                                if ("0".equals(ifMainOrgSec)) {
                                    HashMap<String, String> operAttrsMap = new HashMap<String, String>();
                                    operAttrsMap.put("KEY", "ifMainOffice");
                                    operAttrsMap.put("VALUE", "0");
                                    operAttrsList.add(operAttrsMap);
                                } else {
                                    HashMap<String, String> operAttrsMap = new HashMap<String, String>();
                                    operAttrsMap.put("KEY", "ifMainOffice");
                                    operAttrsMap.put("VALUE", "1");
                                    operAttrsList.add(operAttrsMap);
                                }
                            }else {
                                HashMap<String, String> operAttrsMap = new HashMap<String, String>();
                                operAttrsMap.put("KEY", "ifMainOffice");
                                operAttrsMap.put("VALUE", "1");
                                operAttrsList.add(operAttrsMap);
                            }
                        }else if (BasicCode.SECONDARY.equals(resources)
                                ||BasicCode.JIKE.equals(resources)){
                            if ("0".equals(ifMainOrgSec)) {
                                HashMap<String, String> operAttrsMap = new HashMap<String, String>();
                                operAttrsMap.put("KEY", "ifMainOffice");
                                operAttrsMap.put("VALUE", "0");
                                operAttrsList.add(operAttrsMap);
                            } else {
                                HashMap<String, String> operAttrsMap = new HashMap<String, String>();
                                operAttrsMap.put("KEY", "ifMainOffice");
                                operAttrsMap.put("VALUE", "1");
                                operAttrsList.add(operAttrsMap);
                            }
                        }
                    }else if (BasicCode.LOCAL.equals(systemResource)) {
                        /**单子属于本地网系统
                         * 1，一干下发给本地
                         *    判断是否为主调局,是否有全程调测环节
                         */
                        cstOrderDataMap = orderDealDao.qryCstOrderData(orderIdKy);
                        String mainOrg = MapUtils.getString(cstOrderDataMap, "MAINORG"); // 一干指定的主调
                        String areaParentId = MapUtils.getString(cstOrderDataMap, "PARENT_ID"); // 受理区域的父id
                        String popDeptId = orderDealDao.qryPopDeptId();
                        /*
                         * 查询国际公司pop点的deptid；判断一干指定的主调是否为国际公司的pop点，如果是则需要有跨域全程调测
                         * @author guanzhao
                         * @date 2020/10/22
                         *
                         */
                        if (popDeptId.indexOf(mainOrg) != -1) {
                            HashMap<String, String> operAttrsMap = new HashMap<String, String>();
                            operAttrsMap.put("KEY", "ifMainOffice");
                            operAttrsMap.put("VALUE", "0");
                            operAttrsList.add(operAttrsMap);
                        }else{
                            if (mainOrg.equals(areaParentId)) {
                                HashMap<String, String> operAttrsMap = new HashMap<String, String>();
                                operAttrsMap.put("KEY", "ifMainOffice");
                                operAttrsMap.put("VALUE", "0");
                                operAttrsList.add(operAttrsMap);
                            } else {
                                HashMap<String, String> operAttrsMap = new HashMap<String, String>();
                                operAttrsMap.put("KEY", "ifMainOffice");
                                operAttrsMap.put("VALUE", "1");
                                operAttrsList.add(operAttrsMap);
                            }
                        }
                    }
                }
            }
            // 跨域电路 本地测试和跨域全程调测环节调报竣接口
            if (BasicCode.CROSS_NEWOPEN_FLOW.equals(psId)) {
                Map<String, Object> ifFromSecondaryMap = orderQrySecondaryDao.qryIfFromSecondary(MapUtils.getString(params, "orderId"));
                if(BasicCode.CROSS_WHOLE_COURDER_TEST.equals(tacheId)){
                    if(MapUtils.isEmpty(ifFromSecondaryMap)){
                        Map<String, Object> reportMap = new HashMap<String,Object>();
                        reportMap.put("SrvOrdId",srvOrdId);
                        reportMap.put("tacheId",tacheId);
                        reportMap.put("workId",woId);
                        reportMap.put("username",MapUtils.getString(operStaffInfoMap,"USER_NAME"));
                        reportMap.put("mobiletel",MapUtils.getString(operStaffInfoMap,"USER_PHONE"));
                        reportMap.put("email",MapUtils.getString(operStaffInfoMap,"USER_EMAIL"));
                        reportMap.put("fullname",MapUtils.getString(operStaffInfoMap,"USER_REAL_NAME"));
                        reportMap.put("comments",remark);
                        Thread.sleep(1000);
                        Map<String, Object> reportResMap = feedbackInterface.soComplete(reportMap);
                        if ("fail".equals(MapUtils.getString(reportResMap, "flag"))){
                            resMap.put("success", false);
                            resMap.put("message", "调一干报竣接口失败!" + MapUtils.getString(reportResMap, "msg"));
                            return resMap;
                        }
                    }else {
                        /**
                         * 如果是二干下发的单子，跨域全程调测回单时，也回单二干省际全程调测环节
                         * 1，查询二干系统全程调测的工单；
                         * 2，将工单状态改为处理中；
                         * 3，回单
                         */
                        Map<String, Object> secAllTestOrderMap = orderQrySecondaryDao.qrySecAllTestOrder(MapUtils.getString(params, "orderId"));
                        orderDealDao.updateWoStateAndUserByWoId(MapUtils.getString(secAllTestOrderMap,"WO_ID"),operStaffId,OrderTrackOperType.WO_ORDER_STATE_2);
                        FlowWoDTO woDTOSec = new FlowWoDTO();
                        woDTOSec.setWoId(MapUtils.getString(secAllTestOrderMap,"WO_ID"));
                        HashMap<String, String> operAttrsMap = new HashMap<String, String>();
                        if ("onedry".equals(MapUtils.getString(ifFromSecondaryMap,"RESOURCES"))) {
                            // 如果是一干发给二干，二干发给本地网的单子  跨域全程调测环节回单要给一干报竣
                            Map<String, Object> reportMap = new HashMap<String,Object>();
                            reportMap.put("SrvOrdId",srvOrdId);
                            reportMap.put("tacheId",tacheId);
                            reportMap.put("workId",woId);
                            reportMap.put("username",MapUtils.getString(operStaffInfoMap,"USER_NAME"));
                            reportMap.put("mobiletel",MapUtils.getString(operStaffInfoMap,"USER_PHONE"));
                            reportMap.put("email",MapUtils.getString(operStaffInfoMap,"USER_EMAIL"));
                            reportMap.put("fullname",MapUtils.getString(operStaffInfoMap,"USER_REAL_NAME"));
                            reportMap.put("comments",remark);
                            Thread.sleep(1000);
                            Map<String, Object> reportResMap = feedbackInterface.soComplete(reportMap);
                            if ("fail".equals(MapUtils.getString(reportResMap, "flag"))){
                                resMap.put("success", false);
                                resMap.put("message", "调一干报竣接口失败!" + MapUtils.getString(reportResMap, "msg"));
                                return resMap;
                            }
                            operAttrsMap.put("KEY", "COMMISSIONING_RESULT");
                            operAttrsMap.put("VALUE", "0");
                        }else if("secondary".equals(MapUtils.getString(ifFromSecondaryMap,"RESOURCES"))
                                ||"jike".equals(MapUtils.getString(ifFromSecondaryMap,"RESOURCES"))){
                            // 如果是二干起单直接发给本地网的单子
                            operAttrsMap.put("KEY", "COMMISSIONING_REVIEW");
                            operAttrsMap.put("VALUE", "0");
                        }
                        operAttrsList.add(operAttrsMap);
                        woDTOSec.setOperAttrs(operAttrsList);
                        flowActionHandler.complateWo(operStaffId, woDTOSec);
                    }
                }else if(BasicCode.LOCAL_TEST.equals(tacheId) && MapUtils.getBoolean(tacheOperInfo, "success")){
                    if(MapUtils.isEmpty(ifFromSecondaryMap)){
                        Map<String, Object> reportMap = new HashMap<String, Object>();
                        reportMap.put("SrvOrdId", srvOrdId);
                        reportMap.put("tacheId", tacheId);
                        reportMap.put("workId", woId);
                        reportMap.put("username", MapUtils.getString(operStaffInfoMap, "USER_NAME"));
                        reportMap.put("mobiletel", MapUtils.getString(operStaffInfoMap, "USER_PHONE"));
                        reportMap.put("email",MapUtils.getString(operStaffInfoMap,"USER_EMAIL"));
                        reportMap.put("fullname",MapUtils.getString(operStaffInfoMap,"USER_REAL_NAME"));
                        reportMap.put("comments",remark);
                        Thread.sleep(1000);
                        Map<String, Object> reportResMap = feedbackInterface.soComplete(reportMap);
                        if ("fail".equals(MapUtils.getString(reportResMap, "flag"))) {
                            resMap.put("success", false);
                            resMap.put("message", "调一干报竣接口失败!" + MapUtils.getString(reportResMap, "msg"));
                            return resMap;
                        }
                    }
                }
            }
            // 如果是核查流程，每个环节提交时都需要入库核查反馈信息
          /*  if (BasicCode.LOCAL_RES_CHECK_FLOW.equals(psId)){
                if(!(paramsMap.keySet().contains("isThoughCheckTatal")&&"1".equals(MapUtils.getString(paramsMap,"isThoughCheckTatal")))){
                    Map<String,Object> retMap = checkFeedbackService.updateCheckInfo(params);
                    if(!MapUtils.getBoolean(retMap,"success")){
                        return retMap;
                    }
                }
            }*/

            // 本地客户流程 查询集客  是否可以下单
            /*if(BasicCode.LOCAL_CUST_NEWOPEN_FLOW.equals(psId)){
                String newCreateResource = MapUtils.getString(params,"newCreateResource");
                if(BasicCode.NEW_RESOURCE_YES.equals(newCreateResource)){
                    Map<String, Object> areaMap = orderDealDao.queryConfigInfoBySrvOrdId(srvOrdId, cstOrdId, newCreateResource);
                    JSONObject jsonObject = JSONObject.parseObject(MapUtils.getString(areaMap, "SPECIALTY_INFO"));
                    HashMap<String, String> operAttrsMap = new HashMap<String, String>();
                    operAttrsMap.put("KEY", "areaPopedit");
                    operAttrsMap.put("VALUE", jsonObject.getString("areaPopedit"));
                    operAttrsList.add(operAttrsMap);
                }
            }*/

            // 操作类型：回单：complateWo 启子流程：createChildOrder
            String actionFlag = MapUtils.getString(params, "actionFlag");
            FlowWoDTO woDTO = new FlowWoDTO();
            woDTO.setWoId(woId);
            woDTO.setOperAttrs(operAttrsList);
            if ("complateWo".equals(actionFlag)) {
                if (BasicCode.LOCAL_TEST.equals(tacheId) || BasicCode.UNION_DEBUG_TEST.equals(tacheId)) { // 本地测试 联调测试
                    if (!MapUtils.getBoolean(tacheOperInfo, "success")) {
                        // 本地测试如果有退单，将未回单的兄弟节点修改状态为被动驳回
                        // 查询本地测试未结束的兄弟节点
                        Map testMap = new HashMap();
                        testMap.put("woState", OrderTrackOperType.WO_ORDER_STATE_2);
                        testMap.put("orderId", MapUtils.getString(params, "orderId"));
                        testMap.put("woId", woId);
                        List<Map<String, Object>> localTestList = orderDealDao.qryBrotherOrdId(testMap);
                        for (int i = 0; i < localTestList.size(); i++) {
                            Map<String, Object> updateMap = new HashMap<String, Object>();
                            updateMap.put("woState", OrderTrackOperType.WO_ORDER_STATE_7); // 被动驳回
                            updateMap.put("stateDate", new java.sql.Date(new java.util.Date().getTime()));
                            // updateMap.put("dealDate", new java.sql.Date(new java.util.Date().getTime()));
                            updateMap.put("woID", MapUtils.getString(localTestList.get(i), "WO_ID"));
                            updateMap.put("staffId", "");
                            orderDealDao.updateWoOrderState(updateMap);
                        }
                        // 回退用户选择的子流程
                        String childFlowDataStr = MapUtils.getString(tacheOperInfo, "childFlowData");
                        List<Object> childFlowDataList = JSON.parseArray(childFlowDataStr);
                        for (Object object : childFlowDataList) {
                            Map<String, Object> childFlowDataMap = (Map<String, Object>) object;// 取出list里面的值转为map
                            Map<String, Object> rollBackMap = new HashMap<String, Object>();
                            rollBackMap.put("woId", MapUtils.getString(childFlowDataMap, "WOID"));
                            rollBackMap.put("orderId", MapUtils.getString(childFlowDataMap, "ORDERID"));
                            rollBackMap.put("tacheId", MapUtils.getString(childFlowDataMap, "TACHEID"));
                            rollBackMap.put("remark", "回退用户选择的子流程。。。");
                            rollBackMap.put("flag", "LOCAL");
                            rollBackMap.put("relateInfoId", srvOrderIdRes);
                            rollBackMap.put("srvOrdIdFullCom", srvOrdId);
                            rollBackWoOrder(rollBackMap);
                        }
                        /*
                         * Map<String, Object> updateStateMap = new HashMap<String, Object>();
                         * updateStateMap.put("woState", OrderTrackOperType.WO_ORDER_STATE_10);//子流程回退中290000112
                         * 已启子流程290000110 updateStateMap.put("stateDate", new java.sql.Date(new
                         * java.util.Date().getTime())); updateStateMap.put("woID", woId); updateStateMap.put("staffId",
                         * operStaffId); orderDealDao.updateWoOrderState(updateStateMap);//修改本地测试环节状态为子流程回退中
                         */

                        /*
                         * Map<String, Object> logDataMap = new HashMap<String, Object>(); logDataMap.put("woId",woId);
                         * logDataMap.put("action","本地测试回退"); logDataMap.put("operType",OrderTrackOperType.OPER_TYPE_5);
                         * logDataMap.put("remark",remark); addTrackLog(logDataMap);//入库操作日志
                         */

                        /*
                         * resMap.put("success", true); resMap.put("message", "派单成功!"); return resMap;
                         */
                    }
                    /*
                     * else { //如果本地测试通过，将所有子流程回单 //先查询本地测试的兄弟工单是否还有执行中的 //现在改成监听主订单结束，回单子流程 Map testMap = new
                     * HashMap(); testMap.put("woState",OrderTrackOperType.WO_ORDER_STATE_2);
                     * testMap.put("orderId",MapUtils.getString(params, "orderId")); testMap.put("woId",woId);
                     * List<Map<String, Object>> localTestList = orderDealDao.qryBrotherOrdId(testMap); if
                     * (localTestList.isEmpty()){ Map<String, Object> paramMap = new HashMap<String, Object>();
                     * paramMap.put("orderId", MapUtils.getString(params, "orderId")); paramMap.put("woState",
                     * OrderTrackOperType.WO_ORDER_STATE_2); List<Map<String, Object>> childFlowDataList =
                     * orderDealDao.getAllChildFlowData(paramMap); for (int i = 0; i < childFlowDataList.size(); i++) {
                     * FlowWoDTO woChildDTO = new FlowWoDTO();
                     * woChildDTO.setWoId(MapUtils.getString(childFlowDataList.get(i), "WOID"));
                     * flowActionHandler.complateWo("-2000", woChildDTO); } } }
                     */
                }
                DistributeLock lock = new DatabaseLock(woId);
                try {
                    lock.lock();
                    logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>加锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                    // MultiThread.getInstance().runByMultiThread(woId, 1);
                    // logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>事务>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                    Map<String, Object> logDataMap = new HashMap<String, Object>();
                    logDataMap.put("woId", woId);
                    logDataMap.put("orderId", MapUtils.getString(params, "orderId"));
                    logDataMap.put("remark", remark);
                    logDataMap.put("opinion", MapUtils.getString(params, "opinion"));
                    logDataMap.put("tacheId", tacheId);
                    logDataMap.put("operStaffInfoMap", operStaffInfoMap);
                    logDataMap.put("opinion", MapUtils.getString(params, "opinion"));
                    if (params.containsKey("checkAddOrder")) {
                        flowActionHandler.superaddWo(operStaffId, woDTO);
                        logDataMap.put("operType", OrderTrackOperType.OPER_TYPE_7);
                        logDataMap.put("action", "核查调度补单");
                        logDataMap.put("trackMessage", "[核查调度补单]");
                    }else if (params.containsKey("checkDispatchOrder")) {
                        orderDealDao.updateWoStateByWoId(woId, OrderTrackOperType.WO_ORDER_STATE_4);
                        flowActionHandler.superaddWo(operStaffId, woDTO);
                        logDataMap.put("operType", OrderTrackOperType.OPER_TYPE_4);
                        logDataMap.put("action", "核查调度回单");
                        logDataMap.put("trackMessage", "[核查调度回单]");
                    } else if (params.containsKey("resAllocatAddOrder")) {
                        flowActionHandler.superaddWo(operStaffId, woDTO);
                        logDataMap.put("operType", OrderTrackOperType.OPER_TYPE_7);
                        logDataMap.put("action", "资源分配补单");
                        logDataMap.put("trackMessage", "[资源分配补单]");
                    } else {
                        flowActionHandler.complateWo(operStaffId, woDTO);
                        logDataMap.put("operType", OrderTrackOperType.OPER_TYPE_4);
                        logDataMap.put("action", "回单");
                        logDataMap.put("trackMessage", "[回单]");
                        // 调用资源接口更新全程报竣时间、起止租时间
                        Map<String,Object> resParams = new HashMap<>();
                        resParams.put("psId",psId);
                        resParams.put("tacheId",tacheId);
                        resParams.put("systemResource",systemResource);
                        resParams.put("srvOrdId",srvOrderIdRes);
                        resCfsAttrUpdateServiceIntf.resAttrUpdate(resParams,operAttrsList);

                        String productInfoId = MapUtils.getString(params, "serviceId");
                        if(BasicCode.DIA_SERVICEID.equals(productInfoId)  && (BasicCode.RES_ALLOCATE.equals(tacheId) || BasicCode.DATA_MAKE.equals(tacheId))){
                            fowordResServiceIntf.resSubmitOrder(MapUtils.getString(params, "orderId"),woId);
                        }
                    }
                    tacheDealLogIntf.addTrackLog(logDataMap);// 入库操作日志
                    /**
                     * 资源配置完提交后将进行主流程及子流程工单消息推送操作 需要工单ID，消息推送人，推送内容
                     * @author wangsen
                     * @date 2020/9/28 19:50
                     * 订单ID， 工单ID，下一环节处理人员、下一环节处理专业，推送内容
                     */
                    tacheDealLogIntf.writeOrderMessage(MapUtils.getString(params, "orderId"), MapUtils.getString(params, "woId"), "回单", "childFlow","");

                } catch (Exception e) {
                    logger.error("派单失败：", e);
                    logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>解锁失败捕获异常>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                    resMap.put("success", false);
                    resMap.put("message", "派单失败!" + e);
                    return resMap;
                } finally {
                    lock.unlock();
                    logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>finally主动解锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                }
            } else if ("createChildOrder".equals(actionFlag)) {
                //插入日志
                String orderId = MapUtils.getString(orderDataMap, "ORDERID");
                Map<String, Object> logDataMap = new HashMap<String, Object>();
                logDataMap.put("woId", woId);
                logDataMap.put("orderId", orderId);
                logDataMap.put("remark", remark);
                logDataMap.put("tacheId", MapUtils.getString(params, "tacheId"));
                logDataMap.put("operType", OrderTrackOperType.OPER_TYPE_10);
                logDataMap.put("operStaffInfoMap", operStaffInfoMap);
                logDataMap.put("action", "发起调单");
                logDataMap.put("trackMessage", "[发起调单][调单发起的专业有]");
                tacheDealLogIntf.addTrackLog(logDataMap);
                // 更新电路调度环节状态
                Map<String, Object> updateStateMap = new HashMap<String, Object>();
                updateStateMap.put("woState", OrderTrackOperType.WO_ORDER_STATE_10);// 已启子流程
                updateStateMap.put("stateDate", new java.sql.Date(new java.util.Date().getTime()));
                updateStateMap.put("woID", woId);
                updateStateMap.put("staffId", operStaffId);
                orderDealDao.updateWoOrderState(updateStateMap);
                //入库启流程消息表
                Map<String, String> insertMap = new HashMap<>();
                insertMap.put("woId", woId);
                insertMap.put("orderId", orderId);
                insertMap.put("srvOrdId", srvOrdId);
                orderDealDao.insertCreateChildFlow(insertMap);
            }
            resMap.put("success", true);
            resMap.put("message", "派单成功!");
        } catch (Exception e) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置日期格式
            Map<String, Object> rmap = new HashMap<String, Object>();
            rmap.put("srv_ord_id", srvOrdId);
            rmap.put("attr_code", "1");
            rmap.put("attr_name", "");
            rmap.put("attr_value", srvOrdId);
            rmap.put("create_date", df.format(new Date()));
            rmap.put("sourse", "");
            // 电路调度环节、核查调度环节
            if (BasicCode.CIRCUIT_DISPATCH.equals(tacheId) || BasicCode.CHECK_DISPATCH.equals(tacheId)) {
                // 查询是否成功调过资源创建接口
                int num = orderDealDao.qryInterResult(srvOrderIdRes, "ResBusinessCreate");
                if (num > 0) {
                    rmap.put("attr_action", "ResBusinessCreate");
                    rmap.put("attr_value_name", "业务实例创建接口返回结果");
                    webServiceDao.saveRetInfo(rmap);
                    webServiceDao.copyInterfInfo(srvOrderIdRes, "业务实例创建接口");
                }
            }
            if(BasicCode.LOCALBUILD.equals(flagSys)){
                // 查询是否成功调过资源归档接口
                int numHole = orderDealDao.qryInterResult(srvOrderIdRes, "BusinessArchive");
                if (numHole > 0) {
                    rmap.put("attr_action", "BusinessArchive");
                    rmap.put("attr_value_name", "业务实例归档接口返回结果");
                    webServiceDao.saveRetInfo(rmap);
                    webServiceDao.copyInterfInfo(srvOrderIdRes, "业务实例归档接口");
                }
            }
            resMap.put("success", false);
            resMap.put("message", "派单失败!参数信息:" + params.toString() + e.getMessage());
        }
        return resMap;
    }

    private void insertAccessType(Map<String, Object> params) {
        String srvOrdId = MapUtils.getString(params, "SRV_ORD_ID_ACCESS");
        String accessSources = MapUtils.getString(params, "ACCESS_RESOURCES");
        String attrcodeAccress = MapUtils.getString(params, "ATTR_CODE_ACCRESS");
        String attrCodeNameAccress = MapUtils.getString(params, "ATTR_CODE_NAME_ACCRESS");
        String attrCodeValueAccress = MapUtils.getString(params, "ATTR_CODE_VALUE_ACCRESS","");
        Boolean resultFlagAccess = MapUtils.getBoolean(params, "RESULT_FLAG_ACCESS",false);
        if (resultFlagAccess) {
            if (attrCodeValueAccress!=null && attrCodeValueAccress.length()!=0 ) {
                srvOrdAttrServiceIntf.insertSrvOrdAttr(srvOrdId,attrcodeAccress,attrCodeNameAccress,attrCodeValueAccress,accessSources);
            }
        }
    }

    /*private void insertAZResources(Map<String, Object> params) {
        String srvOrdId = MapUtils.getString(params, "srvOrdId");
        String attrCodeA = MapUtils.getString(params, "ATTR_CODE_A");
        String attrCodeNameA = MapUtils.getString(params, "ATTR_CODE_NAME_A");
        String attrCodeZ = MapUtils.getString(params, "ATTR_CODE_Z");
        String attrCodeNameZ = MapUtils.getString(params, "ATTR_CODE_NAME_Z");
        String attrCodeValueZ = MapUtils.getString(params, "ATTR_CODE_VALUE_Z");
        Boolean resultFlagA = MapUtils.getBoolean(params, "RESULT_FLAG_A",false);
        Boolean resultFlagZ = MapUtils.getBoolean(params, "RESULT_FLAG_Z",false);
        String attrCodeValueA = MapUtils.getString(params, "ATTR_CODE_VALUE_A","");

        if (resultFlagA && attrCodeValueA != null  && attrCodeValueA.length()!=0) {
            srvOrdAttrServiceIntf.insertSrvOrdAttr(srvOrdId,attrCodeA,attrCodeNameA,attrCodeValueA,"local");
        }
        if (resultFlagZ && attrCodeValueZ != null && attrCodeValueA.length()!=0) {
            srvOrdAttrServiceIntf.insertSrvOrdAttr(srvOrdId,attrCodeZ,attrCodeNameZ,attrCodeValueZ,"local");
        }
    }*/


    @Override
    public Map<String, Object> disableOrder(Map<String, Object> params) {
        Map<String, Object> resMap = new HashMap<String, Object>();
        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        String remark = MapUtils.getString(params, "remark");
        String tacheId = MapUtils.getString(params, "tacheId");

        // 把工单ID组成List
        List<String> woIds = new ArrayList<String>();
        String woId = MapUtils.getString(params, "woId");
        woIds.add(woId);

        try {
            Map<String, Object> logDataMap = new HashMap<String, Object>();
            logDataMap.put("woId", woId);
            logDataMap.put("orderId", MapUtils.getString(params, "orderId"));
            logDataMap.put("remark", remark);
            logDataMap.put("tacheId", tacheId);
            flowActionHandler.disableWo(operStaffId, woIds);
            logDataMap.put("operType", OrderTrackOperType.OPER_TYPE_12);
            logDataMap.put("action", "作废");
            logDataMap.put("trackMessage", "[作废]");

            // 查出作废的工单是否是最后一个
            List<Map<String, Object>> list = orderDealDao.queryListByWoId(woId);
            if (list.isEmpty()) { // 最后一个工单作废
                FlowWoDTO woDTO = new FlowWoDTO();
                // 查出该工单兄弟工单中所有已完成的工单ID 倒序
                List<Map<String, Object>> woIdList = orderDealDao.queryWoIdByWoId(woId);
                woDTO.setWoId(MapUtils.getString(woIdList.get(0), "WO_ID"));
                flowActionHandler.superaddWo(operStaffId, woDTO);
            }
            tacheDealLogIntf.addTrackLog(logDataMap);// 入库操作日志

        } catch (Exception e) {
            logger.error("提交失败：", e);
            resMap.put("success", false);
            resMap.put("message", "提交失败!" + e);
            return resMap;
        }
        resMap.put("success", true);
        resMap.put("message", "提交成功!");

        return resMap;
    }

    public Map<String, Object> createChildOrder(Map<String, Object> params) {
        logger.info(">>>>>>>>>>>>>>>>>创建子流程>>>>>>>>>>>>>>>>>>>>>>>");
        Map<String, Object> childResMap = new HashMap<String, Object>();
        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        String operStaffName = ThreadLocalInfoHolder.getLoginUser().getUserName();
        String woId = MapUtils.getString(params, "woId");
        Map<String, Object> tacheOperInfo = MapUtils.getMap(params, "tacheOperInfo");
        String remark = MapUtils.getString(tacheOperInfo, "remark");
        if (remark != null && !"".equals(remark)){
            remark = remark;
        }else {
            remark = "";
        }
        Map<String, String> orderDataMap = MapUtils.getMap(params, "orderDataMap");
        String orderId = MapUtils.getString(orderDataMap, "ORDERID");
        String orderCode = MapUtils.getString(orderDataMap, "ORDERCODE");
        String orderTitle = MapUtils.getString(orderDataMap, "ORDERTITLE");
        String requFineTime = MapUtils.getString(orderDataMap, "REQ_FIN_DATE");//父定单的要求完成时间
        Map<String, String> operStaffInfoMap = MapUtils.getMap(params, "operStaffInfoMap");
        Map<String, Object> operActTypeMap = orderDealDao.getOperActType(orderId);
        String operActType = MapUtils.getString(operActTypeMap, "ACT_TYPE");
        String productType = MapUtils.getString(operActTypeMap, "PRODUCT_TYPE");
        String srvOrdId = MapUtils.getString(params, "srvOrdId");
        // 默认值1，代表电路调度配置的专业区域信息，如果不传值就是默认
        String newCreateResource = "1";
        if (params.keySet().contains("newCreateResource")) {
            // 0代表电路调度环节保存的新建资源录入派发区域信息
            if ("0".equals(org.apache.commons.collections.MapUtils.getString(params, "newCreateResource"))) {
                newCreateResource = org.apache.commons.collections.MapUtils.getString(params, "newCreateResource");
            }
        }
        //  String newCreateResource = MapUtils.getString(params, "newCreateResource");
        Map<String, Object> childFlowParams = orderDealDao.getDispSpecialtyObjByRes(srvOrdId, orderId, newCreateResource);
        // Map<String, Object> childFlowParams = MapUtils.getMap(params, "childFlowSpecialVal");
        // Map<String, String> childFlowSpecial = MapUtils.getMap(childFlowParams, "childFlowSpecial");
        // Map<String, Object> childFlowSpecialArea = MapUtils.getMap(childFlowParams, "childFlowSpecialArea");
        String keyNote = MapUtils.getString(childFlowParams, "KEYNOTE");

        Map<String, Object> sysMap = new HashMap<String, Object>();
        sysMap.put("srvOrdId",srvOrdId);
        sysMap.put("orderId",orderId);
        sysMap.put("keyNote",keyNote);
        Map<String, Object> belongSysMap = orderQrySecondaryDao.qrySrvOrderBelongSys(sysMap); //查询业务订单归属来源
        String systemResource = MapUtils.getString(belongSysMap, "SYSTEM_RESOURCE");
        if(BasicCode.SECOND.equals(systemResource)){
            orderDealDao.updateRelateInfoMaster(sysMap);

        }else if(BasicCode.LOCAL.equals(systemResource)){
            orderDealDao.insertMaster(keyNote, srvOrdId);
        }
        // 入库主调局
        Object flowDispObject = MapUtils.getObject(childFlowParams, "FLOW_SPECIALTY_DATA");
        Map<String, Object> flowSpecialtyData = JSON.parseObject(flowDispObject.toString(), Map.class);
        Map<String, String> childFlowSpecial = MapUtils.getMap(flowSpecialtyData, "childFlowSpecial");
        Map<String, Object> childFlowSpecialArea = MapUtils.getMap(flowSpecialtyData, "childFlowSpecialArea");
        Iterator<Map.Entry<String, String>> childIter = childFlowSpecial.entrySet().iterator();
        List<String> childSpecialList = new ArrayList<String>();
        // List<Map<String, Object>> childFlowList = new ArrayList<Map<String, Object>>(); //存放已启的流程

        // 查询已派发的子流程,流程状态在处理中或者已竣工的不用再次派发
        List<Map<String, Object>> alreadyChildFlowList = orderDealDao.queryAlreadyChildFlowList(orderId);
        try {
            while (childIter.hasNext()) {
                Map.Entry<String, String> e = childIter.next();
                Map<String, Object> startChildMap = new HashMap<String, Object>();
                String key = e.getKey();
                String value = String.valueOf(e.getValue());
                if (!"NULL".equals(value)) {
                    // 是否派发子流程
                    boolean isPayout = false;
                    String ordPsid = "";
                    if ("光纤".equals(value)) {
                        ordPsid = BasicCode.LOCAL_OPTICAL_SPECIAL_CHILDFLOW;
                    } else {
                        ordPsid = BasicCode.LOCAL_OTHER_SPECIAL_CHILDFLOW;
                    }
                    String areaValue = MapUtils.getString(childFlowSpecialArea, key);
                    areaValue = StringUtils.strip(areaValue, "[]");
                    List<String> areaValueList = Arrays.asList(areaValue.split(","));
                    for (int i = 0; i < areaValueList.size(); i++) {
                        // 已派发的区域和专业不再重复派发
                        if (checkoutRepeat(key, areaValueList.get(i), alreadyChildFlowList)) {
                            continue;
                        }
                        startChildMap.put("ordPsid", ordPsid);
                        startChildMap.put("parentOrderId", orderId);
                        startChildMap.put("parentOrderCode", orderCode);
                        startChildMap.put("ORDER_TITLE", orderTitle + "_" + value + "专业子流程");
                        startChildMap.put("AREA", "350002000000000042766408");
                        startChildMap.put("ORDER_CONTENT", "子流程");
                        startChildMap.put("requFineTime", requFineTime);
                        Map<String, String> attr = new HashMap<String, String>();
                        attr.put("REGION_ID", areaValueList.get(i)); // 区域
                        attr.put("SPECIALTY_CODE", key); // 专业
                        attr.put("ACT_TYPE", operActType); // 操作+动作
                        attr.put("PRODUCT_TYPE", productType); //产品编码
                        startChildMap.put("attr", attr);
                        Map<String, Object> childflowData = createOrder(startChildMap);
                        // 区域专业关联已启流程
                        // childflowData.put("orderId",orderId); // 子流程的订单id
                        childflowData.put("orderState", OrderTrackOperType.ORDER_STATE_2); // 子流程的订单状态 200000002:正在执行中。。
                        // 200000004:已完成
                        childflowData.put("orgId", areaValueList.get(i)); // 区域id
                        childflowData.put("professionId", key); // 专业code
                        childflowData.put("parentOrderId", orderId); // 父流程orderId
                        // childFlowList.add(childflowData);
                        isPayout = true;
                    }
                    if (isPayout) {
                        childSpecialList.add(value);
                    }
                }
            }
            //gomProDepOrdRelService.insertGomProDepOrdRel(childFlowList);
            Map<String, Object> logDataMap = new HashMap<String, Object>();
            logDataMap.put("woId", woId);
            logDataMap.put("orderId", orderId);
            logDataMap.put("remark", remark);
            logDataMap.put("tacheId", MapUtils.getString(params, "tacheId"));
            logDataMap.put("operType", OrderTrackOperType.OPER_TYPE_10);
            logDataMap.put("operStaffInfoMap", operStaffInfoMap);
            logDataMap.put("action", "发起调单");
            logDataMap.put("trackMessage", "[发起调单][调单发起的专业有]" + childSpecialList);
            tacheDealLogIntf.addTrackLog(logDataMap);
        } catch (Exception e) {
            logger.error("调单发起失败：", e);
            childResMap.put("success", false);
            childResMap.put("message", "调单发起失败!" + e);
        }
        childResMap.put("success", true);
        childResMap.put("message", "调单发起成功!");
        return childResMap;
    }

    /**
     * 校验是否派发过该区域专业
     *
     * @param key 专业
     * @param areaValue 区域
     * @param alreadyChildFlowList 已派发流程区域、专业
     * @return
     */
    private boolean checkoutRepeat(String key, String areaValue, List<Map<String, Object>> alreadyChildFlowList) {
        boolean temp = false;
        for (Map<String, Object> map : alreadyChildFlowList) {
            String regionId = MapUtil.getString(map, "REGION_ID");
            String specialtyCode = MapUtil.getString(map, "SPECIALTY_CODE");
            if (key.equals(specialtyCode) && areaValue.equals(regionId)) {
                temp = true;
            }
        }
        return temp;
    }

    /**
     * 根据开始日期 ，需要的工作日天数 ，计算工作截止日期，并返回截止日期
     *
     * @param startDate 开始日期
     * @param workDay 工作日天数(周一到周五)
     * @return
     */
    public static String getWorkDay(Date startDate, int workDay) {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(startDate);
        for (int i = 0; i < workDay; i++) {
            c1.set(Calendar.DATE, c1.get(Calendar.DATE) + 1);
            if (Calendar.SATURDAY == c1.get(Calendar.SATURDAY) || Calendar.SUNDAY == c1.get(Calendar.SUNDAY)) {
                workDay = workDay + 1;
                c1.set(Calendar.DATE, c1.get(Calendar.DATE) + 1);
                continue;
            }
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // System.out.println(df.format(c1.getTime()) + " " + getWeekOfDate(c1.getTime()));
        return df.format(c1.getTime());
    }

    /**
     * 工单转派
     *
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> transferWoOrder(Map<String, Object> params) {
        logger.info(">>>>>>>>>>>>>>>>>工单转派>>>>>>>>>>>>>>>>>>>>>>>");
        String operStaffId = "";
        if(params.containsKey("operStaffId")){
            operStaffId = MapUtils.getString(params,"operStaffId");
        } else{
            operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        }
        // String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        String woId = MapUtils.getString(params, "woId");
        String tacheId = MapUtils.getString(params, "tacheId");
        String objId = MapUtils.getString(params, "objId");
        String objType = MapUtils.getString(params, "objType");
        Map<String, Object> logDataMap = new HashMap<String, Object>();
        logDataMap.put("woId", woId);
        logDataMap.put("orderId", MapUtils.getString(params, "orderId"));
        logDataMap.put("remark", MapUtils.getString(params, "remark"));
        logDataMap.put("tacheId", tacheId);
        logDataMap.put("operType", OrderTrackOperType.OPER_TYPE_6);
        logDataMap.put("action", "转派");
        logDataMap.put("trackMessage", "[转派]");
        Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(operStaffId));
        logDataMap.put("operStaffInfoMap",operStaffInfoMap);
        Map<String, Object> resMap = new HashMap<String, Object>();
        DistributeLock lock = new DatabaseLock(woId);
        try {
            lock.lock();
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>加锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            FlowDispObjDTO dispObjDTO = new FlowDispObjDTO();
            dispObjDTO.setDispObjId(objId);
            dispObjDTO.setDispObjType(objType);
            flowActionHandler.reDispWo(operStaffId, woId, dispObjDTO);
            orderDealDao.updateDealUserByWoId(woId);
            tacheDealLogIntf.addTrackLog(logDataMap);
            resMap.put("success", true);
            resMap.put("message", "转派成功!");
        } catch (Exception e) {
            logger.error("转派失败：", e);
            resMap.put("success", false);
            resMap.put("message", "转派失败!" + e);
        } finally {
            lock.unlock();
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>finally主动解锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        }
        return resMap;
    }

    @Override
    public Map<String, Object> getFreeWoOrder(Map<String, Object> params) {
        String operStaffId = "";
        if(params.containsKey("operStaffId")){
            operStaffId = MapUtils.getString(params,"operStaffId");
        } else{
            operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        }
        //String operStaffName = ThreadLocalInfoHolder.getLoginUser().getUserName();
        List woOrderIds = (List) MapUtils.getObject(params, "woOrderIds");
        List srvOrdIds = (List) MapUtils.getObject(params, "srvOrdIds");
        Map<String, Object> resMap = new HashMap<String, Object>();
        Map<String, Object> orderMap = null;
        String actionType = MapUtils.getString(params, "actionType");
        // String woOrderState = "";
        String action = "";
        String operType = "";
        Map<String, Object> updateMap = new HashMap<String, Object>();
        updateMap.put("operStaffId", operStaffId);
        //    updateMap.put("dealDate", new java.sql.Date(new java.util.Date().getTime()));
        if ("get".equals(actionType)) {
            action = "签收";
            operType = OrderTrackOperType.OPER_TYPE_2;
            updateMap.put("operStaffId", operStaffId);
            //    updateMap.put("dealDate", new java.sql.Date(new java.util.Date().getTime()));

        } else if ("free".equals(actionType)) {
            action = "释放签收";
            operType = OrderTrackOperType.OPER_TYPE_3;
            updateMap.put("operStaffId", "");
            updateMap.put("dealDate", "");
        }
        Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(operStaffId));
        try {
            for (int i = 0; i < woOrderIds.size(); i++) {
                Long woOrderId = Long.parseLong(woOrderIds.get(i).toString());
                updateMap.put("woOrderId", woOrderId);
                orderDealDao.updateWoOrderStateById(updateMap);
                orderMap = orderDealDao.qryOrderDetailInfoBywoOrderId(woOrderId);
                Map<String, Object> logDataMap = new HashMap<String, Object>();
                logDataMap.put("woId", woOrderId);
                logDataMap.put("orderId", MapUtils.getString(orderMap, "ORDER_ID"));
                logDataMap.put("remark", "");
                logDataMap.put("tacheId", MapUtils.getString(orderMap, "ID"));
                logDataMap.put("operType", operType);
                logDataMap.put("operStaffInfoMap", operStaffInfoMap);
                logDataMap.put("action", action);
                logDataMap.put("trackMessage", "[" + action + "]");
                SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                logDataMap.put("dealDate", formatter.format(new Date()));
                tacheDealLogIntf.addTrackLog(logDataMap);
                /*Map<String, Object> paramsMap = new HashMap<String, Object>();
                paramsMap.put("orderId", MapUtils.getString(orderMap, "ORDER_ID"));
                paramsMap.put("woOrdId", woOrderId);
                paramsMap.put("trackOrgId", MapUtils.getString(operStaffInfoMap, "ORG_ID"));
                paramsMap.put("trackOrgName", MapUtils.getString(operStaffInfoMap, "ORG_NAME"));
                paramsMap.put("trackDate", new java.sql.Date(new java.util.Date().getTime()));
                paramsMap.put("createDate", new java.sql.Date(new java.util.Date().getTime()));
                paramsMap.put("trackStaffId", operStaffId);
                paramsMap.put("trackStaffName", operStaffName);
                paramsMap.put("trackStaffPhone", MapUtils.getString(operStaffInfoMap, "USER_PHONE"));
                paramsMap.put("trackStaffEmail", MapUtils.getString(operStaffInfoMap, "USER_EMAIL"));
                String trackMessage = "[" + operStaffName + "将工单单号：" + woOrderId + "][" + action + "]";
                paramsMap.put("trackMessage", trackMessage);
                String trackContent = "[" + action + "工单]";
                paramsMap.put("trackContent", trackContent);
                paramsMap.put("operType", operType);
                orderDealDao.insertTrackLogInfo(paramsMap);*/
            }
            resMap.put("success", true);
            resMap.put("message", action + "成功!");

            // add by cwy 签发成功后一干下发的单子需要通知一干电路调度
            try {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置日期格式
                if ("get".equals(actionType) && srvOrdIds != null && srvOrdIds.size() > 0) {
                    for (int i = 0; i < srvOrdIds.size(); i++) {
                        String srvOrdId =srvOrdIds.get(i).toString();
                        Map<String, Object> sendMap = new HashMap<>();
                        sendMap.put("SrvOrdId",srvOrdId);
                        int sendNum = orderDealDao.qryIsSendOneDry(srvOrdId);
                        if (sendNum == 0 ){
                            //通知一干
                            feedbackInterface.soAccept(sendMap);
                            //记录通知，二次签收不通知
                            Map<String, Object> rmap = new HashMap<String, Object>();
                            rmap.put("srv_ord_id", srvOrdId);
                            rmap.put("attr_code", "sendOneDry");
                            rmap.put("attr_name", "");
                            rmap.put("attr_value", "1");//0是 1否
                            rmap.put("create_date", df.format(new Date()));
                            rmap.put("sourse", "");
                            rmap.put("attr_action", "0");
                            rmap.put("attr_value_name", "电路调度签收通知一干调度");
                            webServiceDao.saveRetInfo(rmap);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(action + "签发成功后通知一干电路调度失败：", e);
            }

        } catch (Exception e) {
            logger.error(action + "失败：", e);
            resMap.put("success", false);
            resMap.put("message", action + "失败!" + e);
        }
        return resMap;
    }

    @Override
    public Map<String, Object> affirmException(Map<String, Object> params) {
        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        String operStaffName = ThreadLocalInfoHolder.getLoginUser().getUserName();
        String srvOrdId = MapUtils.getString(params, "srvOrdId");

        Map<String, Object> resMap = new HashMap<String, Object>();
        Map<String, Object> updateMap = new HashMap<String, Object>();

        Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(operStaffId));
        try {
            if (!"".equals(srvOrdId)) {
                updateMap.put("dealUserId", operStaffId);
                updateMap.put("srvOrdId", srvOrdId);
                orderDealDao.affirmExceptionBySrvOrdId(updateMap);
            }
            resMap.put("success", true);
            resMap.put("message", "异常确认成功!");
        } catch (Exception e) {
            logger.error("异常确认失败：", e);
            resMap.put("success", false);
            resMap.put("message", "异常确认失败!" + e);
        }
        return resMap;
    }

    @Override
    public Map<String, Object> getTacheButton(Map<String, Object> params) {
        String tacheId = MapUtils.getString(params, "tacheId");
        String btnInfo = MapUtils.getString(params, "btnInfo");
        String orderId = MapUtils.getString(params, "orderId");
        String sysResource = MapUtils.getString(params, "sysResource");
        String buttonState = MapUtils.getString(params, "buttonState");

        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("tacheId", tacheId == null ? null : tacheId);
        if("dealOrder".equals(buttonState)){
            //抄送单所有环节得  岗位部门个人待办处理中都要有
            paramsMap.put("buttonState", 120);
        }
        if("applyOrder".equals(buttonState)){
            //抄送单所有环节得  岗位部门个人待办处理中都要有
            paramsMap.put("buttonState", 402);
        }
        if ("101".equals(btnInfo)) {
            paramsMap.put("btnInfo", btnInfo);
            paramsMap.put("finishFlag", "101");
        } else if ("103".equals(btnInfo)) {
            Map<String, Object> tacheIdsMap = orderDealDao.getTacheIds(orderId);
            String tacheIds = MapUtils.getString(tacheIdsMap, "TACHEIDS");
            //if (BasicCode.CHECK_DISPATCH.equals(tacheId.toString()) && tacheIds.indexOf(BasicCode.CHECK_TOTAL) == -1) {
            if (BasicCode.CHECK_DISPATCH.equals(tacheId.toString())) {
                paramsMap.put("btnInfo", btnInfo);
                paramsMap.put("finishFlag", "103");
            } else if (tacheIds.indexOf(BasicCode.RES_CONSTRUCT) == -1) {
                paramsMap.put("btnInfo", btnInfo);
                paramsMap.put("finishFlag", "103");
                paramsMap.put("resChangeFlag", null);
            } else if (BasicCode.RES_ALLOCATE.equals(tacheId.toString())
                    || BasicCode.FIBER_RES_ALLOCATE.equals(tacheId)){
                paramsMap.put("btnInfo", btnInfo);
                paramsMap.put("finishFlag", "103");
                paramsMap.put("resChangeFlag", "109");
            } else {
                paramsMap.put("btnInfo", btnInfo);
                paramsMap.put("finishFlag", null);
            }
        } else {
            //资源分配已完成
            if ("110".equals(btnInfo) && (BasicCode.RES_ALLOCATE.equals(tacheId.toString()) || BasicCode.FIBER_RES_ALLOCATE.equals(tacheId))){
                paramsMap.put("btnInfo", btnInfo);
                paramsMap.put("finishFlag", null);
                paramsMap.put("resChangeFlag", "109");
            }else{
                paramsMap.put("btnInfo", btnInfo);
                paramsMap.put("finishFlag", null);
                paramsMap.put("resChangeFlag", null);
            }
        }
        Map<String, Object> resMap = new HashMap<String, Object>();
        try {
            List<Map<String, String>> btnList = orderDealDao.qryTacheButton(paramsMap);
            /*if("second-schedule-lt".equals(sysResource)&& BasicCode.CHECK_DISPATCH.equals(tacheId)){
                for(int i=0;i<btnList.size();i++){
                    if(btnList.get(i).get("BUTTON_NAME").equals("回退")){
                        btnList.remove(i);
                    }
                }
            }*/

            // 核查汇总环节是否展示下发工建按钮
            btnList = checkFlowShowBtn(params,btnList);
            resMap.put("success", true);
            resMap.put("resButtons", btnList);
        } catch (Exception e) {
            logger.error("环节按钮查询失败：", e);
            resMap.put("success", false);
            resMap.put("resButtons", "环节按钮查询失败!" + e);
        }
        return resMap;
    }

    /**
     * 1.资源分配环节,查看专业是否可以进行资源配置
     * 2.核查流程 核查汇总环节是否展示下发工建按钮;
     * 3.并行核查流程不展示资源配置按钮
     * 4.核查调度环节，只有互联网专线(DIA)产品，才展示自动化核查/直接反馈两个按钮
     * @param params
     * @param btnList
     */
    public List<Map<String, String>> checkFlowShowBtn(Map<String, Object> params, List<Map<String, String>> btnList) {
        String tacheId = MapUtils.getString(params, "tacheId");
        String serviceId = MapUtils.getString(params, "serviceId");
        String resources = MapUtils.getString(params, "resources");
        String specialtyCode = MapUtils.getString(params, "specialtyCode");
        String orderId = MapUtils.getString(params, "orderId");
        // 资源分配环节,查看专业是否可以进行资源配置
        if (BasicCode.RES_ALLOCATE.equals(tacheId)) {
            for (int i = 0; i < btnList.size(); i++) {
                Map<String, String> btn = btnList.get(i);
                if ("resConfig()".equals(MapUtils.getString(btn, "BUTTON_CLICK"))) {
                    String[] orderIds = MapUtils.getString(params, "orderIds").split(",");
                    int num = orderDealDao.qryGomOrderAttrByOrderIds(orderIds, BasicCode.SPECIALTY_CODE,BasicCode.RES_CONFIG);
                    if (num < 1) {
                        btnList.remove(btn);
                        --i;
                    }
                }
                if ("diaResConfig()".equals(MapUtils.getString(btn, "BUTTON_CLICK"))) {
                    params.put("codeType", "provinceAuto");
                    if (!isAccessEngineering(params) || !"10000011".equals(serviceId) || !"DATA_4".equals(specialtyCode)) {
                        btnList.remove(btn);
                        --i;
                    }
                }
            }
        }
        // 核查汇总环节
        else if(BasicCode.CHECK_TOTAL.equals(tacheId)){
            params.put("codeType", "provAccessEngiConstruct");
            if(!existTacheSendEngin(params)) {
                for (int i = 0; i < btnList.size(); i++) {
                    Map<String, String> btn = btnList.get(i);
                    if ("sendEngineering()".equals(MapUtils.getString(btn, "BUTTON_CLICK"))) {
                        btnList.remove(btn);
                        --i;
                    }
                }
            }
        } else if(BasicCode.CHECK_DISPATCH.equals(tacheId)){
            if(!"10000011".equals(serviceId)){
                // 核查调度环节，只有互联网专线(DIA)产品，才展示自动化核查/直接反馈两个按钮
                Set<String> clickSet = new HashSet<>();
                clickSet.add("autoCheck()");//自动化核查
                clickSet.add("directFeedback()");// 直接反馈政企中台
                for (int i = 0; i < btnList.size(); i++) {
                    Map<String, String> btn = btnList.get(i);
                    if (clickSet.contains(MapUtils.getString(btn, "BUTTON_CLICK"))) {
                        btnList.remove(btn);
                        --i;
                    }
                }
            }
        }
        // 并行核查流程不展示资源配置按钮
        Map<String,Object> psInfo = orderDealDao.qryOrderInfoByOrderId(orderId);
        if((!MapUtils.isEmpty(psInfo)) && BasicCode.LOCAL_PRARLLEL_CHECK_FLOW.equals(
                MapUtils.getString(psInfo,"PS_ID",""))){
            for (int i = 0; i < btnList.size(); i++) {
                Map<String, String> btn = btnList.get(i);
                if ("resConfig()".equals(MapUtils.getString(btn, "BUTTON_CLICK"))) {
                    btnList.remove(btn);
                    --i;
                }
            }
        }
        return btnList;
    }

    /**
     * 是否已生成下发工建环节
     * @param params
     * @return
     */
    public boolean existTacheSendEngin(Map<String, Object> params) {
        String orderId = MapUtils.getString(params, "orderId");
        String woId = MapUtils.getString(params, "woId");
        String sysResource = MapUtils.getString(params, "sysResource");
        String resource = MapUtils.getString(params, "resource");
        boolean showFlag = false;
        // 判断省份是否对接了工建系统
        if (isAccessEngineering(params)) {
            // 本地网的集客下发的单子，才可以对接工建
            if ("jike".equals(resource)&&"flow-schedule-lt".equals(sysResource)) {
                // 查询orderId下所有环节
                Map<String, Object> tacheIdsMap = orderDealDao.getTacheIds(orderId);
                String tacheIds = MapUtils.getString(tacheIdsMap, "TACHEIDS");
                // 已对接工建，并且未生成下发工建环节，则展示按钮
                showFlag = tacheIds.indexOf(BasicCode.SEND_ENGINEERING) == -1;
            }
        }
        return showFlag;
    }
    /**
     * 核查汇总页面是否展示工建反馈费用信息
     * @param params
     * @return
     */
    @Override
    public boolean showEnginInfo(Map<String, Object> params) {
        String orderId = MapUtils.getString(params, "orderId");
        boolean showFlag = false;
        // 判断省份是否对接了工建系统
        if (isAccessEngineering(params)) {
            // 查询orderId下所有环节
            Map<String, Object> tacheIdsMap = orderDealDao.getTacheIds(orderId);
            String tacheIds = MapUtils.getString(tacheIdsMap, "TACHEIDS");
            // 已对接工建，并且已生成下发工建环节，则展示工建反馈费用信息
            showFlag = tacheIds.indexOf(BasicCode.SEND_ENGINEERING) > -1;
        }
        return showFlag;
    }

    /**
     * 查询资源核查信息
     *
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> qryResCheckInfo(Map<String, Object> params) {
        //文本路由：1023   网元：1053：IP:2101   端口：310  光路路由：1008
        // 路由类型  端口  310，局限光纤  731，纤芯  702，端子 317，文本路由1023，端口310
        Map<String, Object> retMap = new HashMap<>();
        retMap.put("success",true);
        Map<String, Object> data = new HashMap<>();
        List<Map<String,Object>> list = orderDealDao.qryResCheckInfo(params);
        String position = MapUtils.getString(params,"position","");
        boolean devFlag = params.containsKey("hasDevice");
        boolean optFlag = params.containsKey("hasOptical");
        String hasDevice =MapUtils.getString(params,"hasDevice","");
        String hasOptical =MapUtils.getString(params,"hasOptical","");
        if(list!=null && list.size()>0){
            StringBuffer optRoute = new StringBuffer();
            for(Map<String, Object> temp : list ){
                String attrValue = MapUtils.getString(temp,"ATTR_VALUE","");
                String attrCode = MapUtils.getString(temp,"ATTR_CODE","");
                String attrName = MapUtils.getString(temp,"ATTR_NAME","");
                //20000001	传输专业;20000007	光路专业
                String compId = MapUtils.getString(temp,"COMPID","");
                if(position.equals("A")){
                    if(devFlag && "20000001".equals(compId)){
                        if(attrCode.equals("1023") ){
                            data.put("A_EQUIPMENT_MODEL",attrValue);// A端设备型号
                        } else if(attrCode.equals("1053")){
                            data.put("A_TRANS_ELEMENT",attrValue); // A端传输网元
                        } else if(attrCode.equals("1053-2101")){
                            data.put("A_IP_ADDRESS",attrValue); // A端IP地址
                        } else if(attrCode.equals("310")){
                            data.put("A_NETWORK_PORT",attrValue); // A端网元端口
                        }
                    }
                    if("0".equals(hasOptical) && "20000007".equals(compId) && attrCode.equals("1023") ){
                        data.put("A_OPTICALCABLE_TEXT",attrValue);// A端光缆文本备注说明
                    } else if("1".equals(hasOptical) && "20000007".equals(compId) ){
                        optRoute.append(attrName+":"+attrValue+"\n");
                    }
                } else if(position.equals("Z")){
                    if(devFlag && "20000001".equals(compId)){
                        if(attrCode.equals("1023") ){
                            data.put("Z_EQUIPMENT_MODEL",attrValue);// Z端设备型号
                        } else if(attrCode.equals("1053")){
                            data.put("Z_TRANS_ELEMENT",attrValue); // Z端传输网元
                        } else if(attrCode.equals("1053-2101")){
                            data.put("Z_IP_ADDRESS",attrValue); // Z端IP地址
                        } else if(attrCode.equals("310")){
                            data.put("Z_NETWORK_PORT",attrValue); // Z端网元端口
                        }
                    }
                    if("0".equals(hasOptical) && "20000007".equals(compId) && attrCode.equals("1023") ){
                        data.put("Z_OPTICALCABLE_TEXT",attrValue);// Z端光缆文本备注说明
                    } else if("1".equals(hasOptical) && "20000007".equals(compId) ){
                        optRoute.append(attrName+":"+attrValue+"\n");
                    }
                }
            }
            if(position.equals("A")&& "1".equals(hasOptical)){
                data.put("A_OPTICALCABLE_ROUTE",optRoute); // A端光缆路由
            } else if(position.equals("Z") && "1".equals(hasOptical)){
                data.put("Z_OPTICALCABLE_ROUTE",optRoute); // Z端光缆路由
            }
        }
        retMap.put("data",data);
        return retMap;
    }

    /**
     * 是否已对接工建系统
     * @return
     */
    public Boolean isAccessEngineering(Map<String,Object> params) {
        Boolean flag = false;
        // 判断省份是否对接了工建系统
        String operStaffId = "";
        String codeType = MapUtils.getString(params, "codeType");
        if (params.keySet().contains("staffId")) {
            operStaffId = MapUtils.getString(params, "staffId");
        } else {
            operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        }
        int proNum = orderDealDao.qryProNumByUserId(operStaffId, codeType);
        if (proNum > 0) {
            flag = true;
        }
        return flag;
    }

    /**
     * 已对接工建系统是否需要等待
     * @return
     */
    public Boolean waitAccessEngineering(Map<String,Object> params) {
        Boolean flag = false;
        // 判断省份是否对接了工建系统
        String srvOrdId = MapUtils.getString(params,"srvOrdId");
        String cstOrdId = MapUtils.getString(params,"cstOrdId");
        //add by wang.gang2 资源是否具备 （不具备 订单枢纽会直接下发 具备的话需要判断 是否有手动下发工建得记录）
        Map<String,Object> resInfo = orderDealDao.resExistence(srvOrdId);
        if("0".equals(MapUtils.getString(resInfo,"ARESEXIST","1"))
                || "0".equals(MapUtils.getString(resInfo,"ZRESEXIST","1"))
        ){
            flag = true;
        }else{
            //查询开通单下发工建记录
            if ("1".equals(MapUtils.getString(resInfo,"O2CLOG","0"))) {
                flag = true;
            }
        }

        return flag;
    }

    /**
     * 完工汇总环节提交前判断工建是否已完成建设
     * @param params
     * @return
     */
    @Override
    public Map<String,Object> summaryBeforeCommit(Map<String,Object> params){
        Map<String,Object> resMap = new HashMap<>();
        boolean flag = true;
        String psId = MapUtils.getString(params,"psId");
        String tacheId = MapUtils.getString(params,"tacheId");
        // 目前还有本地客户新开流程的完工汇总需要判断
        //modify by wang.gang2  本地只有完工确认 GG
        boolean sign = BasicCode.LOCAL_CUST_NEWOPEN_FLOW.equals(psId) && BasicCode.COMPLETE_CONFIRM.equals(tacheId);

        if(sign && waitAccessEngineering(params)){
            //查询工建系统是否已建设完成(查完工推送)
            String srvOrdId = MapUtils.getString(params,"srvOrdId");
            Map<String,Object> map = webServiceDao.queryProgressFinsh(srvOrdId);
            /*
            工程建设状态：00：不需要建设,01：建设已完成
             */
            if(map != null){
                String stat = MapUtils.getString(map,"PROGRESS_STAT");
                if( "00".equals(stat) || "01".equals(stat) ){
                    flag =  true;
                }
            } else{
                flag = false;
                resMap.put("message","该电路还未完成工建流程，等待工建系统完工！");
            }
        }
        resMap.put("success",flag);
        return resMap;
    }

    /**
     * 回退按钮
     *
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> goBackOrder(Map<String, Object> params) {
        logger.info(">>>>>>>>>>>>>>>>>电路调度,核查调度环节回退工单>>>>>>>>>>>>>>>>>>>>>>>");
        /**
         * 电路调度，核查调度，需求部门审核环节退单
         * 区分核查调度和电路调度
         * 1，电路调度：先判断子流程是否都退单到电路调度；
         * 2，核查调度：先判断是否有专业核查,如果有将所有专业核查环节工单作废，回滚资源；
         *
         *
         */
        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        String woId = MapUtils.getString(params, "woId");
        Long orderId = MapUtils.getLong(params, "orderId");
        String contentInfo = MapUtils.getString(params, "remark");
        String tacheId = MapUtils.getString(params, "tacheId");
        Map<String, Object> resMap = new HashMap<String, Object>();
        Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(operStaffId));
        DistributeLock lock = new DatabaseLock(woId);
        try {
            lock.lock();
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>加锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            String srvOrdId = MapUtils.getString(params, "srvOrdId");
            Map<String, Object> woOrderMap = new HashMap<String, Object>();
            woOrderMap.put("woState", OrderTrackOperType.WO_ORDER_STATE_6);
            woOrderMap.put("stateDate", new java.sql.Date(new java.util.Date().getTime()));
            woOrderMap.put("woID", woId);
            woOrderMap.put("staffId", operStaffId);
            woOrderMap.put("orderState", OrderTrackOperType.ORDER_STATE_3);
            woOrderMap.put("orderId", orderId);
            woOrderMap.put("srvOrdId", srvOrdId);
            if(BasicCode.CIRCUIT_DISPATCH.equals(tacheId)){
                // 先判断所有子流程都已退回至电路调度环节
                int count = orderDealDao.qrySurviveChildFlowNum(woOrderMap);
                if (count > 0) {
                    resMap.put("success", false);
                    resMap.put("message", "还有其它专业及区域的资源处理流程未退单，故不能将整个电路回退!");
                    return resMap;
                }
            }else if(BasicCode.CHECK_DISPATCH.equals(tacheId)){
                //核查调度作废所有专业核查的工单
                Map<String, Object> invalidParam = new HashMap<String, Object>();
                invalidParam.put("orderId", orderId.toString());
                invalidParam.put("operStaffId", operStaffId);
                checkOrderServiceIntf.invalidAllSpecialCheck(invalidParam);
            }

            /**
             * 判断是否二干下发：
             * 是：调用二干环节退单方法，将二干环节退单；
             * 否：判断是否集客来单：是：调用接口退单接口，业务订单状态为作废；
             *                    否：业务订单状态为草稿；
             */
            Map<String, Object> ifFromSecondaryMap = orderQrySecondaryDao.qryIfFromSecondary(orderId.toString());
            if(MapUtils.isNotEmpty(ifFromSecondaryMap)){
                srvOrdId = MapUtils.getString(ifFromSecondaryMap, "RELATE_INFO_ID");//如果是二干下发要将srvOrdId赋值为本地的，再去回滚资源
                contentInfo = "本地网退单二干调度！" + contentInfo;
                if(BasicCode.CIRCUIT_DISPATCH.equals(tacheId)){
                    /**
                     * 查询本地是否有调单，如果有作废调单，删除其关联关系
                     */
                    String dispatchOrderId = MapUtils.getString(ifFromSecondaryMap, "DISPATCH_ORDER_ID");
                    int dispatchOrderNum = orderQrySecondaryDao.qryIfHasLocalDispatchOrder(dispatchOrderId);
                    if(dispatchOrderNum > 0){
                        orderQrySecondaryDao.updateDispatchOrder(dispatchOrderId);//修改调单状态为作废
                        Map<String, Object> secDispatchOrder = orderQrySecondaryDao.qrySecDispatchOrder(orderId.toString());
                        Map<String, Object> paramMap = new HashMap<>();
                        paramMap.put("cstOrdId", MapUtils.getString(secDispatchOrder, "CST_ORD_ID"));
                        paramMap.put("srvOrdId", MapUtils.getString(secDispatchOrder, "SRV_ORD_ID"));
                        paramMap.put("orderId", MapUtils.getString(secDispatchOrder, "ORDER_ID"));
                        paramMap.put("dispatchOrderId", MapUtils.getString(secDispatchOrder, "DISPATCH_ORDER_ID"));
                        orderDealDao.updateDispatchOrderIdToRelateTable(paramMap);
                    }
                    params.put("operStaffId", "-2000");
                    resMap = localAndSecondMutualServiceInf.localBackOrderToSecond(params);
                }else if(BasicCode.CHECK_DISPATCH.equals(tacheId)){
                    //核查调度退单二干。。。。。。
                    checkOrderServiceIntf.rollBackSecondCheck(orderId.toString());
                    resMap.put("success", true);
                    resMap.put("message", "回退成功!");
                }
                orderQrySecondaryDao.updateSecOrderState(srvOrdId);//修改二干本地关联表的状态为10X
            }else {
                String srvOrderState = "10C"; //业务订单状态为草稿
                //调用集客退单接口
                Map<String, Object> orderDataMap = orderDealDao.qryOrderData(woId);
                if ("jike".equals(MapUtils.getString(orderDataMap, "RESOURCES"))) {
                    int numBackOrder = orderDealDao.qryInterResult(srvOrdId, "BackOrder");
                    if (numBackOrder < 1) {
                        Map map = new HashMap();
                        map.put("srvOrdId", srvOrdId);
                        map.put("backExec", contentInfo);
                        map.put("orderId", orderId);
                        map.put("backReasonType", MapUtils.getString(params, "backReasonType", ""));
                        Map backMap = backOrderServiceIntf.backOrder(map);
                        if (!"1".equals(MapUtils.getString(backMap, "RESP_CODE"))) {
                            resMap.put("success", false);
                            resMap.put("message", "派单失败!调用集客退单接口异常，异常原因：" + MapUtils.getString(backMap, "RESP_DESC"));
                            return resMap;
                        }
                    }
                    srvOrderState = "10X"; //业务订单状态为作废
                }
                orderDealDao.updateSrvOrderStateById(srvOrderState, orderId);
                resMap.put("success", true);
                resMap.put("message", "回退成功!");
            }
            //modify by wang.gang2  避免影响集客并行核查退单 将这两个方法放在下面 不删除是怕影响别的功能
            orderDealDao.updateWoOrderState(woOrderMap); // 修改工单状态为主动驳回
            orderDealDao.updateOrderStateById(woOrderMap); // 修改订单状态为回退中
            // 调用资源回滚接口
            Map<String, Object> param = new HashMap<>();
            param.put("srvOrdId", srvOrdId);// gom_bdw_srv_ord_info.srv_ord_id
            param.put("orderIds", null);// orderIds是个List, 存放的是 子流程的gom_order.order_id
            param.put("rollbackDesc", contentInfo); // 回滚原因
            Map retmap = businessRollbackServiceIntf.resRollBack(param);
            if (!MapUtils.getBoolean(retmap, "success")) {
                resMap.put("success", false);
                resMap.put("message", "回退失败!" + MapUtils.getString(retmap, "message"));
                return resMap;
            }
            Map<String, Object> logDataMap = new HashMap<String, Object>();
            logDataMap.put("woId", woId);
            logDataMap.put("orderId", orderId);
            logDataMap.put("remark", "回退原因：" + contentInfo);
            logDataMap.put("tacheId", MapUtils.getLong(params, "tacheId"));
            logDataMap.put("operType", OrderTrackOperType.OPER_TYPE_11);
            logDataMap.put("operStaffInfoMap", operStaffInfoMap);
            logDataMap.put("action", "回退");
            logDataMap.put("trackMessage", "[回退]");
            tacheDealLogIntf.addTrackLog(logDataMap);
        } catch (Exception e) {
            logger.error("回退失败：" + e.getMessage(), e);
            resMap.put("success", false);
            resMap.put("message", "回退失败!" + e.getMessage());
            return resMap;
        } finally {
            lock.unlock();
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>finally主动解锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        }
        return resMap;
    }

    @Override
    public synchronized Map getsequenceNum(Map param) {
        Map<String, Object> resMap = new HashMap<String, Object>();
        String strId = "";
        try {
            String cstOrdId = MapUtils.getString(param, "cstOrdId");
            String srvOrdId = MapUtils.getString(param, "srvOrdId");
            String deptId = "";
            //根据srvOrdId，判断电路是本地起草，还是上级下发
            Map<String, Object> srvMap = orderDealDao.querySrvInfoBySrvOrdId(srvOrdId);
            if(BasicCode.LOCAL.equals(MapUtils.getString(srvMap, "SYSTEM_RESOURCE"))){
                //本地起单，根据CST_ORD_ID查询业务受理区域
                Map<String, Object> cstMap = orderDealDao.queryHandleDeptId(cstOrdId);
                deptId = MapUtils.getString(cstMap, "HANDLE_DEP_ID");
                //根据部门ID查询部门等级和部门区域ID
                Map<String, Object> deptMap = orderDealDao.getDeptInfoByDeptId(deptId);
                String areaId = MapUtils.getString(deptMap,"AREA_ID");
                int currentLevel = MapUtils.getIntValue(deptMap, "DEPT_LEVEL_ID");
                if ("350002000000000042766429".equals(areaId) || "350002000000000042766427".equals(areaId)){ //本地起草，还需要区分是否是海南重庆，如果是海南重庆，根据部门ID迭代部门等级为1的记录
                    deptId = iterativeQueryDeptId(deptId, 1, currentLevel);
                }else if (true){ //如果不是海南重庆，根据部门ID迭代部门等级为2的记录
                    deptId = iterativeQueryDeptId(deptId, 2, currentLevel);
                }
            }else{ //上级下发
                //根据cst_ord_id、srv_ord_id、order_id查询受理部门ID
                String orderId = MapUtils.getString(param, "orderId");
                Map<String, Object> regionMap = orderDealDao.queryRegionId(cstOrdId, srvOrdId, orderId);
                deptId = MapUtils.getString(regionMap, "REGION_ID");
                //根据部门ID查询部门等级和部门区域ID
                Map<String, Object> deptMap = orderDealDao.getDeptInfoByDeptId(deptId);
                int currentLevel = MapUtils.getIntValue(deptMap, "DEPT_LEVEL_ID");
                //迭代查询需要的部门ID
                deptId = iterativeQueryDeptId(deptId, 2, currentLevel);
            }
            //根据部门ID查询拼接调单的信息
            Map<String, Object> disMap = orderDealDao.queryDisInfoByDeptId(deptId);
            String sign = disMap.get("SIGN").toString() + "联通网调字";
            resMap.put("sign", sign);
            // 判断是否需要将schedu_num重置为0
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy");
            String newDate = fmt.format(new Date());
            String updateDate = disMap.get("UPDATE_DATE").toString().substring(0, 4);
            int seqId = 0;
            if (newDate.equals(updateDate)) {
                seqId = Integer.parseInt(disMap.get("SCHEDU_NUM").toString()) + 1;
                // 将当前的序号更新到数据库
                orderDealDao.updateScheduNum(String.valueOf(seqId), disMap.get("ORG_ID").toString());
            } else {
                seqId = seqId + 1;
                // 如果进入下一年则schedu_num重置为1
                orderDealDao.updateScheduNum("1", disMap.get("ORG_ID").toString());
            }
            strId = String.format("%05d", seqId);
        } catch (Exception e) {
            e.printStackTrace();
            resMap.put("success", false);
            resMap.put("message", "调单编号生成失败：" + e);
        }
        resMap.put("success", true);
        resMap.put("message", strId);
        return resMap;
    }

    /**
     * 进行资源配置，返回报文，url等信息
     *
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> resConfig(Map<String, Object> params) {
        String srvOrdId = MapUtils.getString(params, "srvOrdId","");
        Map<String, Object> resMap = new HashMap<>();
        String srvOrderIdRes = "";
        String flagSys = BasicCode.LOCALBUILD;
        boolean resSupplement = params.keySet().contains("resSupplement")&&MapUtils.getBoolean(params,"resSupplement");
        if( resSupplement ){
            flagSys = BasicCode.RES_SUPPLEMENT;
            srvOrderIdRes = MapUtils.getString(params,"srvOrderIdRes");
        } else{
            Map<String, Object> belongSysMap = orderQrySecondaryDao.qrySrvOrderBelongSys(params);
            String systemResource = MapUtils.getString(belongSysMap, "SYSTEM_RESOURCE");
            if (BasicCode.SECOND.equals(systemResource)){
                srvOrderIdRes = MapUtils.getString(params, "relateInfoId");
                flagSys = BasicCode.SECONDARY;
            }else {
                srvOrderIdRes = srvOrdId;
                flagSys = BasicCode.LOCALBUILD;
            }
        }

        Map<String, Object> resConfigParams = new HashMap<String, Object>();
        resConfigParams.put("flag", flagSys);
        resConfigParams.put("srvOrdId", srvOrderIdRes);
        resConfigParams.put("srvOrdIdOld", srvOrdId);
        resConfigParams.put("resFullCom", MapUtils.getString(params,"resFullCom"));

        // 核查调度环节可以直接进行资源配置，所以需要先查询是否成功调过资源创建接口
        int num = orderDealDao.qryInterResult(srvOrderIdRes, "ResBusinessCreate");
        if (num < 1) {
            // 电路调度环节调用资源创建接口
            Map retmap = businessCreateServiceIntf.businessCreate(resConfigParams);
            if (!"成功".equals(MapUtils.getString(retmap, "returncode"))) {
                resMap.put("success", false);
                resMap.put("message", "派单失败!调用资源创建接口异常，异常原因：" + MapUtils.getString(retmap, "returndec"));
                return resMap;
            }
        }
        String operStaffName = ThreadLocalInfoHolder.getLoginUser().getLoginName();
        params.put("userName", operStaffName);
        // 查看需要参数：srvOrdId,isConfig,woId,compIds[List],userName,
        // 子流程配置需要参数：srvOrdId,userName,isConfig,tacheId,orderId:gom_order.order_id
        // 核查预占需要参数:srvOrdId,userName,isConfig,tacheId,woId
        if (params.keySet().contains("isConfig")) {
            if (!"0".equals(MapUtils.getString(params, "isConfig"))) {
                params.put("isConfig", "1");
            }
        } else {
            params.put("isConfig", "1");
        }
        params.put("flag", flagSys);
        params.put("srvOrdId", srvOrderIdRes);
        params.put("srvOrdIdOld", srvOrdId);
        resMap = resAssignmentServiceIntf.resAssignment(params);

        if ("成功".equals(MapUtils.getString(resMap, "returncode"))) {
            resMap.put("success", true);
        } else {
            resMap.put("success", false);
            resMap.put("message", MapUtils.getString(resMap, "returndec"));
        }
        return resMap;
    }

    /**
     * 查询供选择的组织（区域）
     */
    @Override
    public List qryDepart(Map<String, Object> params) {
        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        Map<String, Object> staffMap = orderDealDao.getIsBraFiliAle(Integer.valueOf(operStaffId));
        Long orgId = MapUtil.getLong(staffMap, "ORG_ID");
        String isBraFiliale = MapUtils.getString(staffMap, "ISBRAFILIALE");
        // Map<String, Object> orgMap = orderDealDao.getDeptInfo(orgId);
        String flag = MapUtils.getString(params, "flag");
        Map<String, Object> paramMap = new HashMap<String, Object>();
        if("0".equals(isBraFiliale)){ //是否查询分公司下的部门 1查询市分公司 0查询省分公司下
            // 查询该部门所在省份的org_id
            Map<String, Object> provinceMap = orderDealDao.getProviceOrg(orgId);
            Long parentId = MapUtils.getLong(provinceMap, "ORG_ID");
            paramMap.put("parentId", parentId);
        }else if("1".equals(isBraFiliale)){
            Map<String, Object> stringObjectMap = orderDealDao.getParentDepInfo(Integer.valueOf(operStaffId));
            Long parentId = MapUtils.getLong(stringObjectMap, "ORG_ID");
            paramMap.put("fuzzyRegionName", "分公司");
            paramMap.put("parentId", parentId);
        }
        paramMap.put("isBraFiliale", isBraFiliale);
        paramMap.put("professionId", MapUtils.getString(params, "key"));
        paramMap.put("parentOrderId", MapUtils.getString(params, "orderId"));
        List<Map<String, Object>> deptList = orderDealDao.getDeptInfoListRel(paramMap);
        // 人员转派查询
        if ("transferStaff".equals(flag)) {
            // List<Map<String, Object>> usersList = new ArrayList<Map<String, Object>>();
            if (deptList != null && deptList.size() > 0) {
                for (Map<String, Object> deptMap : deptList) {
                    String parentDeptId = MapUtils.getString(deptMap, "pId");
                    // Long deptId = MapUtils.getLong(deptMap, "id");
                    if (!"1".equals(parentDeptId)) {
                        // 查询同部门下的人员
                        /*
                         * List<Map<String, Object>> staffList = orderDealDao.getStaffInfoList(deptId,operStaffId);
                         * usersList.addAll(staffList);
                         */
                        deptMap.put("isParent", true);
                    } else {
                        deptMap.remove("pId");
                    }
                }
            }
            // deptList.addAll(usersList);
        }
        return deptList;
    }

    @Override
    public List qryDepartParent(Map<String, Object> params) {
        /*String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        String flag = MapUtils.getString(params, "flag");*/
        String currentAreaId = MapUtils.getString(params, "currentAreaId");
        String currentOrgId = MapUtils.getString(params, "currentOrgId");
        List<Map<String, Object>> deptList = orderDealDao.getDeptInfoListRelUnit(currentAreaId, currentOrgId);
        if (deptList != null && deptList.size() > 0) {
            for (Map<String, Object> deptMap : deptList) {
                String parentDeptId = MapUtils.getString(deptMap, "pId");
                if (!"1".equals(parentDeptId)) {
                    deptMap.put("isParent", true);
                } else {
                    deptMap.remove("pId");
                }
            }
        }
        return deptList;
    }

    @Override
    public List qrySearchOrgPerDepart(Map<String, Object> params) {
        // String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        List<Map<String, Object>> deptList = new ArrayList<Map<String, Object>>();
        Map<String, Object> isCunmap = new HashMap<String, Object>(); // 判断组织是否已存在
        String orgPerDeType = MapUtils.getString(params, "orgPerDeType");
        String searchVal = MapUtils.getString(params, "searchVal");
        String currentAreaId = MapUtils.getString(params, "currentAreaId");
        if ("260000001".equals(orgPerDeType)) { // 部门
            List<Map<String, Object>> mapDepart = orderDealDao.qrySearchPartInfoByVal(currentAreaId, searchVal);
            List<Map<String, Object>> mapDepartFilter = new ArrayList<Map<String, Object>>(); //从组织、人员List里过滤出的组织
            if(!CollectionUtils.isEmpty(mapDepart)){
                //添加人员
                for(Map<String, Object> mapS : mapDepart){
                    String id = MapUtils.getString(mapS, "id");
                    String name = MapUtils.getString(mapS, "name");
                    String pId = MapUtils.getString(mapS, "pId");
                    String open = MapUtils.getString(mapS, "open");
                    String objType = MapUtils.getString(mapS, "objType");
                    String isParent = MapUtils.getString(mapS, "isParent");
                    if (isCunmap.get(id + objType) == null) {
                        Map<String, Object> mapO = new HashMap<String, Object>(); // 查询出的组织
                        mapO.put("id", id);
                        mapO.put("name", name);
                        mapO.put("pId", pId);
                        mapO.put("open", open);
                        mapO.put("objType", objType);
                        mapO.put("isParent", isParent);
                        mapDepartFilter.add(mapO);
                        isCunmap.put(id + objType, id);
                    }

                    String perid = MapUtils.getString(mapS, "perid");
                    String pername = MapUtils.getString(mapS, "pername");
                    String perpId = MapUtils.getString(mapS, "perpId");
                    String peropen = MapUtils.getString(mapS, "peropen");
                    String perobjType = MapUtils.getString(mapS, "perobjType");
                    String perisParent = MapUtils.getString(mapS, "perisParent");
                    if (!StringUtils.isEmpty(perid)) {
                        Map<String, Object> mapO = new HashMap<String, Object>(); // 查询出组织下的人员
                        mapO.put("id", perid);
                        mapO.put("name", pername);
                        mapO.put("pId", perpId);
                        mapO.put("open", peropen);
                        mapO.put("objType", perobjType);
                        mapO.put("isParent", perisParent);
                        deptList.add(mapO);
                    }

                    String ppid = MapUtils.getString(mapS, "ppid");
                    String ppname = MapUtils.getString(mapS, "ppname");
                    String pppId = MapUtils.getString(mapS, "pppId");
                    String ppopen = MapUtils.getString(mapS, "ppopen");
                    String ppobjType = MapUtils.getString(mapS, "ppobjType");
                    String ppisParent = MapUtils.getString(mapS, "ppisParent");
                    if (!StringUtils.isEmpty(ppid)) {
                        if (!"中国联通".equals(ppname)) {
                            if (isCunmap.get(ppid + ppobjType) == null) {
                                Map<String, Object> mapO = new HashMap<String, Object>(); // 查询出的组织的上级组织
                                mapO.put("id", ppid);
                                mapO.put("name", ppname);
                                mapO.put("pId", pppId);
                                mapO.put("open", ppopen);
                                mapO.put("objType", ppobjType);
                                mapO.put("isParent", ppisParent);
                                mapDepartFilter.add(mapO);
                                isCunmap.put(ppid + ppobjType, ppid);
                            }
                        }
                    }
                }
                // 添加组织--对组织特殊处理下
                Map<String, Object> isCunmapCu = new HashMap<String, Object>(); // 过滤当前循环已存在的组织
                List<Map<String, Object>> deptListOrg = new ArrayList<Map<String, Object>>();
                if (!CollectionUtils.isEmpty(mapDepartFilter)) {
                    for (Map<String, Object> mapS : mapDepartFilter) {
                        String id = MapUtils.getString(mapS, "id");
                        String name = MapUtils.getString(mapS, "name");
                        String pId = MapUtils.getString(mapS, "pId");
                        String open = MapUtils.getString(mapS, "open");
                        String objType = MapUtils.getString(mapS, "objType");
                        String isParent = MapUtils.getString(mapS, "isParent");
                        if (isCunmapCu.get(id + objType) == null) {
                            mapS.put("pId", isCunmap.get(pId + objType) == null ? "" : pId);
                            deptListOrg.add(mapS);
                            isCunmapCu.put(id + objType, id);
                        }

                    }

                }
                deptList.addAll(deptListOrg);
                // isCunmapCu.clear(); //清空map

            }

        } else if ("260000003".equals(orgPerDeType)) { // 人员
            List<Map<String, Object>> mapsPerson = orderDealDao.qrySearchPerInfoByVal(currentAreaId, searchVal);
            if (!CollectionUtils.isEmpty(mapsPerson)) {
                for (Map<String, Object> mapS : mapsPerson) {
                    String id = MapUtils.getString(mapS, "id");
                    String name = MapUtils.getString(mapS, "name");
                    String pId = MapUtils.getString(mapS, "pId");
                    String open = MapUtils.getString(mapS, "open");
                    String objType = MapUtils.getString(mapS, "objType");
                    String isParent = MapUtils.getString(mapS, "isParent");
                    if (isCunmap.get(id + objType) == null) {
                        Map<String, Object> mapO = new HashMap<String, Object>(); // 查询出的组织
                        mapO.put("id", id);
                        mapO.put("name", name);
                        mapO.put("pId", pId);
                        mapO.put("open", open);
                        mapO.put("objType", objType);
                        mapO.put("isParent", isParent);
                        deptList.add(mapO);
                        isCunmap.put(id + objType, id);
                    }

                    String oid = MapUtils.getString(mapS, "oid");
                    String oname = MapUtils.getString(mapS, "oname");
                    String opId = MapUtils.getString(mapS, "opId");
                    String oopen = MapUtils.getString(mapS, "oopen");
                    String oobjType = MapUtils.getString(mapS, "oobjType");
                    String oisParent = MapUtils.getString(mapS, "oisParent");

                    if (!StringUtils.isEmpty(oid)) {
                        if (isCunmap.get(oid + oobjType) == null) {
                            Map<String, Object> mapO = new HashMap<String, Object>(); // 查询出的组织
                            mapO.put("id", oid);
                            mapO.put("name", oname);
                            mapO.put("pId", opId);
                            mapO.put("open", oopen);
                            mapO.put("objType", oobjType);
                            mapO.put("isParent", oisParent);
                            deptList.add(mapO);
                            isCunmap.put(oid + oobjType, oid);
                        }
                    }

                }
            }
        } else if ("260000002".equals(orgPerDeType)) { // 岗位
            List<Map<String, Object>> mapsPost = orderDealDao.qrySearchPostInfoByVal(currentAreaId, searchVal);
            if (!CollectionUtils.isEmpty(mapsPost)) {
                for (Map<String, Object> mapS : mapsPost) {
                    String id = MapUtils.getString(mapS, "id");
                    String name = MapUtils.getString(mapS, "name");
                    String pId = MapUtils.getString(mapS, "pId");
                    String open = MapUtils.getString(mapS, "open");
                    String objType = MapUtils.getString(mapS, "objType");
                    String isParent = MapUtils.getString(mapS, "isParent");
                    if (isCunmap.get(id + objType) == null) {
                        Map<String, Object> mapO = new HashMap<String, Object>(); // 查询出的组织
                        mapO.put("id", id);
                        mapO.put("name", name);
                        mapO.put("pId", pId);
                        mapO.put("open", open);
                        mapO.put("objType", objType);
                        mapO.put("isParent", isParent);
                        deptList.add(mapO);
                        isCunmap.put(id + objType, id);
                    }

                    String perid = MapUtils.getString(mapS, "perid");
                    String pername = MapUtils.getString(mapS, "pername");
                    String perpId = MapUtils.getString(mapS, "perpId");
                    String peropen = MapUtils.getString(mapS, "peropen");
                    String perobjType = MapUtils.getString(mapS, "perobjType");
                    String perisParent = MapUtils.getString(mapS, "perisParent");
                    if (!StringUtils.isEmpty(perid)) {
                        Map<String, Object> mapO = new HashMap<String, Object>(); // 查询出组织下的人员
                        mapO.put("id", perid);
                        mapO.put("name", pername);
                        mapO.put("pId", perpId);
                        mapO.put("open", peropen);
                        mapO.put("objType", perobjType);
                        mapO.put("isParent", perisParent);
                        deptList.add(mapO);
                    }

                }
            }
        }
        // isCunmap.clear(); //清空数据
        return deptList;
    }

    @Override
    public Map<String, Object> qrySearchOrgPerDepSingleDown(Map<String, Object> params) {
        Map<String, Object> isCunmap = new HashMap<String, Object>();
        List<Map<String, Object>> deptList = new ArrayList<Map<String, Object>>();
        try {
            String orgPerDeTypeVal = MapUtils.getString(params, "orgPerDeTypeVal"); // 派发类型
            String searchOrgPerName = MapUtils.getString(params, "searchOrgPerName");
            String currentUserId = MapUtils.getString(params, "currentUserId");
            String currentAreaId = MapUtils.getString(params, "currentAreaId");
            PageInfo pageInfo = new PageInfo();// 分页信息
            pageInfo.setIndexSizeData(params.get("pageIndex"), params.get("pageSize"));
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("searchOrgPerName", searchOrgPerName);
            paramMap.put("currentUserId", currentUserId);
            paramMap.put("currentAreaId", currentAreaId);
            paramMap.put("startRow", pageInfo.getRowStart());// 分页开始行
            paramMap.put("endRow", pageInfo.getRowEnd());// 分页结束行
            if ("260000003".equals(orgPerDeTypeVal)) { // 人员
                List<Map<String, Object>> mapsPerson = orderDealDao.qrySearchPerSingleDown(paramMap);
                if (!CollectionUtils.isEmpty(mapsPerson)) {
                    deptList.addAll(mapsPerson);
                }
            }
            isCunmap.put("message", "success");
            isCunmap.put("data", deptList);
        } catch (Exception e) {
            isCunmap.put("errormessage", e.getMessage());
            isCunmap.put("message", "fail");
            isCunmap.put("data", Lists.newArrayList());
        }
        return isCunmap;
    }

    @Override
    public Map<String, Object> qrySearchOrgPerDepPullDown(Map<String, Object> params) {
        Map<String, Object> isCunmap = new HashMap<String, Object>();
        List<Map<String, Object>> deptList = new ArrayList<Map<String, Object>>();
        try {
            String orgPerDeTypeVal = MapUtils.getString(params, "orgPerDeTypeVal"); // 派发类型
            String searchOrgPerName = MapUtils.getString(params, "searchOrgPerName");
            String currentUserId = MapUtils.getString(params, "currentUserId");
            String currentAreaId = MapUtils.getString(params, "currentAreaId");
            String currentOrgId = MapUtils.getString(params, "currentOrgId");
            PageInfo pageInfo = new PageInfo();// 分页信息
            pageInfo.setIndexSizeData(params.get("pageIndex"), params.get("pageSize"));
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("searchOrgPerName", searchOrgPerName);
            paramMap.put("currentUserId", currentUserId);
            paramMap.put("currentAreaId", currentAreaId);
            paramMap.put("startRow", pageInfo.getRowStart());// 分页开始行
            paramMap.put("endRow", pageInfo.getRowEnd());// 分页结束行
            if ("260000001".equals(orgPerDeTypeVal)) { // 部门
                List<Map<String, Object>> mapDepart = orderDealDao.qrySearchDepPullDown(paramMap);
                if (!CollectionUtils.isEmpty(mapDepart)) {
                    deptList.addAll(mapDepart);
                }
            } else if ("260000003".equals(orgPerDeTypeVal)) { // 人员
                List<Map<String, Object>> mapsPerson = orderDealDao.qrySearchPerPullDown(paramMap);
                if (!CollectionUtils.isEmpty(mapsPerson)) {
                    deptList.addAll(mapsPerson);
                }
            } else if ("260000002".equals(orgPerDeTypeVal)) { // 岗位
                currentOrgId = orderDealDao.qryBelongCompany(currentOrgId);
                paramMap.put("currentOrgId", currentOrgId);
                List<Map<String, Object>> mapsPost = orderDealDao.qrySearchPostPullDown(paramMap);
                if (!CollectionUtils.isEmpty(mapsPost)) {
                    deptList.addAll(mapsPost);
                }
            }
            isCunmap.put("message", "success");
            isCunmap.put("data", deptList);
        } catch (Exception e) {
            isCunmap.put("errormessage", e.getMessage());
            isCunmap.put("message", "fail");
            isCunmap.put("data", Lists.newArrayList());
        }
        return isCunmap;
    }

    @Override
    public Map<String, Object> qrySearchOrgPerDepPullDownSub(Map<String, Object> params) {
        Map<String, Object> isCunmap = new HashMap<String, Object>();
        List<Map<String, Object>> deptList = new ArrayList<Map<String, Object>>();
        try {
            String postId = MapUtils.getString(params, "postId"); // 岗位Id
            String deptId = MapUtils.getString(params, "deptId"); // 部门Id
            String objType = MapUtils.getString(params, "objType"); // 派发类型
            String currentUserId = MapUtils.getString(params, "currentUserId");
            String currentAreaId = MapUtils.getString(params, "currentAreaId");
            String currentOrgId = MapUtils.getString(params, "currentOrgId");
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("postId", postId);
            paramMap.put("deptId", deptId);
            paramMap.put("currentUserId", currentUserId);
            paramMap.put("currentAreaId", currentAreaId);
            if ("260000001".equals(objType)) { // 部门
                List<Map<String, Object>> mapDepart = orderDealDao.qrySearchDepPullDownSub(paramMap);
                if (!CollectionUtils.isEmpty(mapDepart)) {
                    deptList.addAll(mapDepart);
                }
            } else if ("260000002".equals(objType)) { // 岗位
                currentOrgId = orderDealDao.qryBelongCompany(currentOrgId);
                paramMap.put("currentOrgId", currentOrgId);
                List<Map<String, Object>> mapsPost = orderDealDao.qrySearchPostPullDownSub(paramMap);
                if (!CollectionUtils.isEmpty(mapsPost)) {
                    deptList.addAll(mapsPost);
                }
            }
            isCunmap.put("message", "success");
            isCunmap.put("data", deptList);
        } catch (Exception e) {
            isCunmap.put("errormessage", e.getMessage());
            isCunmap.put("message", "fail");
            isCunmap.put("data", Lists.newArrayList());
        }
        return isCunmap;

    }

    @Override
    public List qrySearchContactsSel(Map<String, Object> params) {
        List<Map<String, Object>> deptList = new ArrayList<Map<String, Object>>();
        Map<String, Object> isCunmap = new HashMap<String, Object>(); // 判断组织是否已存在
        String currentUserId = MapUtils.getString(params, "currentUserId");
        String currentAreaId = MapUtils.getString(params, "currentAreaId");

        List<Map<String, Object>> mapsPerson = orderDealDao.qrySearchContactsSel(params);
        if (!CollectionUtils.isEmpty(mapsPerson)) {
            for (Map<String, Object> mapS : mapsPerson) {
                String id = MapUtils.getString(mapS, "id");
                String name = MapUtils.getString(mapS, "name");
                String pId = MapUtils.getString(mapS, "pId");
                String open = MapUtils.getString(mapS, "open");
                String objType = MapUtils.getString(mapS, "objType");
                String isParent = MapUtils.getString(mapS, "isParent");
                String nocheck = MapUtils.getString(mapS, "nocheck");
                if (isCunmap.get(id + objType) == null) {
                    Map<String, Object> mapO = new HashMap<String, Object>(); // 查询出的组织
                    mapO.put("id", id);
                    mapO.put("name", name);
                    mapO.put("pId", pId);
                    mapO.put("open", open);
                    mapO.put("objType", objType);
                    mapO.put("isParent", isParent);
                    mapO.put("nocheck", nocheck);
                    deptList.add(mapO);
                    isCunmap.put(id + objType, id);
                }

                String oid = MapUtils.getString(mapS, "oid");
                String oname = MapUtils.getString(mapS, "oname");
                String opId = MapUtils.getString(mapS, "opId");
                String oopen = MapUtils.getString(mapS, "oopen");
                String oobjType = MapUtils.getString(mapS, "oobjType");
                String oisParent = MapUtils.getString(mapS, "oisParent");
                String onocheck = MapUtils.getString(mapS, "onocheck");

                if (!StringUtils.isEmpty(oid)) {
                    if (isCunmap.get(oid + oobjType) == null) {
                        Map<String, Object> mapO = new HashMap<String, Object>(); // 查询出的组织
                        mapO.put("id", oid);
                        mapO.put("name", oname);
                        mapO.put("pId", opId);
                        mapO.put("open", oopen);
                        mapO.put("objType", oobjType);
                        mapO.put("isParent", oisParent);
                        mapO.put("nocheck", onocheck);
                        deptList.add(mapO);
                        isCunmap.put(oid + oobjType, oid);
                    }
                }

            }
        }
        return deptList;
    }

    @Override
    public Map<String, Object> addSearchContacts(Map<String, Object> params) {
        Map<String, Object> contactMap = new HashMap<String, Object>();
        List<Map<String, Object>> conTactsList = new ArrayList<Map<String, Object>>();
        try {
            net.sf.json.JSONArray contactData = (net.sf.json.JSONArray) params.get("data");
            if (!CollectionUtils.isEmpty(contactData)) {
                for (int i = 0; i < contactData.size(); i++) {
                    Map<String, Object> contactMapL = new HashMap<String, Object>();
                    net.sf.json.JSONObject oJbject = (net.sf.json.JSONObject) contactData.get(i);
                    contactMapL.put("user_id", oJbject.get("user_id"));
                    contactMapL.put("parent_user_id", oJbject.get("parent_user_id"));
                    contactMapL.put("system_resource", oJbject.get("system_resource"));
                    conTactsList.add(contactMapL);
                }
                if (!CollectionUtils.isEmpty(conTactsList)) {
                    orderDealDao.addSearchContacts(conTactsList);
                }
            }
            contactMap.put("message", "success");
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            contactMap.put("message", "fail");
            contactMap.put("errorMessage", e.getMessage());
        }
        return contactMap;
    }

    @Override
    public Map<String, Object> deleteSearchContacts(Map<String, Object> params) {
        Map<String, Object> contactMap = new HashMap<String, Object>();
        try {
            orderDealDao.deleteSearchContacts(params);
            contactMap.put("message", "success");
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            contactMap.put("message", "fail");
            contactMap.put("errorMessage", e.getMessage());
        }
        return contactMap;
    }

    @Override
    public Map<String, Object> qrySearchContacts(Map<String, Object> params) {
        Map<String, Object> contactMap = new HashMap<String, Object>();
        List<Map<String, Object>> contactMapsList = new ArrayList<Map<String, Object>>();
        String currentUserId = MapUtils.getString(params, "currentUserId"); // 当前Id
        String searchContactsName = MapUtils.getString(params, "searchContactsName"); // 联系人
        PageInfo pageInfo = new PageInfo();// 分页信息
        pageInfo.setIndexSizeData(params.get("pageIndex"), params.get("pageSize"));
        int contactCount = 0;
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("currentUserId", currentUserId);
            paramMap.put("searchContactsName", searchContactsName);
            // paramMap.put("startRow",pageInfo.getRowStart());//分页开始行
            // paramMap.put("endRow",pageInfo.getRowEnd());//分页结束行
            contactCount = orderDealDao.qrySearchContactsCount(paramMap);
            if (contactCount > 0) {
                List<Map<String, Object>> contactMapsListTemp = orderDealDao.qrySearchContacts(paramMap);
                if (!CollectionUtils.isEmpty(contactMapsListTemp)) {
                    contactMapsList.addAll(contactMapsListTemp);
                }
            }
            contactMap.put("data", contactMapsList);
            contactMap.put("message", "success");
        } catch (Exception e) {
            contactMap.put("data", contactMapsList);
            contactMap.put("message", "fail");
        }
        contactMap.put("contactCount", contactCount);
        contactMap.put("pageCount", pageInfo.getPageCount());
        return contactMap;
    }

    @Override
    public List qryChildNodeData(Map<String, Object> params) {
        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        Long deptId = MapUtils.getLong(params, "id");
        List<Map<String, Object>> staffList = orderDealDao.getStaffInfoList(deptId, operStaffId);
        List<Map<String, Object>> childDepartList = orderDealDao.getChildDepartInfoList(deptId);
        childDepartList.addAll(staffList);
        return childDepartList;
    }

    @Override
    public List getStaffInfoDeptListUnit(Map<String, Object> params) {
        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        Long deptId = MapUtils.getLong(params, "id");
        List<Map<String, Object>> staffDeptList = orderDealDao.getStaffInfoDeptListUnit(deptId, operStaffId);
        return staffDeptList;
    }

    @Override
    public List qryChildNodeDataT(Map<String, Object> params) {
        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        Long deptId = MapUtils.getLong(params, "id");
        String searchValue = MapUtils.getString(params, "searchValue");
        List<Map<String, Object>> staffList = orderDealDao.getStaffInfoListT(deptId, operStaffId, searchValue);
        List<Map<String, Object>> childDepartList = orderDealDao.getChildDepartInfoListT(deptId, searchValue);
        childDepartList.addAll(staffList);
        return childDepartList;
    }

    /**
     * 根据区域ID查询对应分公司下的专业
     *
     * @param param
     * @return
     */
    @Override
    public List<Map<String, Object>> querySpecialtyConfig(Map<String, Object> param) {
        // 查询当前用户分公司信息
        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        Map<String, Object> map = orderDealDao.getCurrentUserBranch(operStaffId);
        String area_id = MapUtils.getString(map, "AREA_ID"); //map.get("AREA_ID").toString();
        String serviceId =MapUtils.getString(param, "serviceId"); // param.get("serviceId").toString();
        String psId =MapUtils.getString(param, "psId");
        Map<String, Object> qryMap = new HashMap<>();
        /*
         * 福建--电路调度环节的专业配置需要区分流程
         * @author guanzhao
         * @date 2020/10/19
         *
         */
        if ("350002000000000042766440".equals(area_id)){
            qryMap.put("psId", psId);
        }
        qryMap.put("areaId", area_id);
        qryMap.put("serviceId", serviceId);
        //List<Map<String, Object>> list = orderDealDao.querySpecialtyConfig(area_id, serviceId);
        List<Map<String, Object>> list = orderDealDao.querySpecialtyConfig(qryMap);
        return list;
    }

    /**
     * 补单，查询原调单信息
     *
     * @param param
     * @return
     */
    @Override
    public Map<String, Object> queryDispatchInfoByCstOrdId(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap();
        try {// 查询当前用户所属分公司
            // String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
            // Map<String, Object> map = orderDealDao.getCurrentUserBranch(operStaffId);
            // if(map != null) {
            List<Map<String, Object>> list = orderDealDao.queryDispatchInfoByCstOrdId(param);
            resMap.put("dispatch",list);
//                resMap.put("areaId",map.get("AREA_ID"));
            resMap.put("success",true);
//            }
//            else {
//                throw new Exception("查询用户信息为空！");
//            }
        }
        catch (Exception e){
            resMap.put("success",false);
            resMap.put("message",e.getMessage());
            logger.info(">>>>>>查询原调单信息发生异常" +e);
        }
        return resMap;
    }

    @Override
    public Map<String, Object> qryCircuitAreaInfo(String srvOrdId) {
        Map<String, Object> resMap = new HashMap<>();
        Map<String, Object> areaMap = orderDealDao.qryCircuitAreaInfo(srvOrdId);
        String aAreaId = MapUtils.getString(areaMap, "AREGIONID");
        String zAreaId = MapUtils.getString(areaMap, "ZREGIONID");
        String resources = MapUtils.getString(areaMap, "RESOURCES");
        if (aAreaId.isEmpty()) {
            if (!zAreaId.isEmpty()) {
                resMap.put("PORT", "Z");
                resMap.put("ZREGIONID", MapUtils.getString(areaMap, "ZREGIONID"));
                resMap.put("ZREGIONNAME", MapUtils.getString(areaMap, "ZREGIONNAME"));
                resMap.put("AREAID", MapUtils.getString(areaMap, "AREAID"));
                resMap.put("AREANAME", MapUtils.getString(areaMap, "AREANAME"));
            } else {
                resMap.put("AREAID", MapUtils.getString(areaMap, "AREAID"));
                resMap.put("AREANAME", MapUtils.getString(areaMap, "AREANAME"));
            }
        } else if (zAreaId.isEmpty()) {
            if (!aAreaId.isEmpty()) {
                resMap.put("PORT", "A");
                resMap.put("AREGIONID", MapUtils.getString(areaMap, "AREGIONID"));
                resMap.put("AREGIONNAME", MapUtils.getString(areaMap, "AREGIONNAME"));
                resMap.put("AREAID", MapUtils.getString(areaMap, "AREAID"));
                resMap.put("AREANAME", MapUtils.getString(areaMap, "AREANAME"));

            } else {
                resMap.put("AREAID", MapUtils.getString(areaMap, "AREAID"));
                resMap.put("AREANAME", MapUtils.getString(areaMap, "AREANAME"));
            }
        } else if (!aAreaId.isEmpty() && !zAreaId.isEmpty() && aAreaId.equals(zAreaId)) {
            resMap.put("PORT", "A");
            resMap.put("AREGIONID", MapUtils.getString(areaMap, "AREGIONID"));
            resMap.put("AREGIONNAME", MapUtils.getString(areaMap, "AREGIONNAME"));
            resMap.put("AREAID", MapUtils.getString(areaMap, "AREAID"));
            resMap.put("AREANAME", MapUtils.getString(areaMap, "AREANAME"));
        } else if (!aAreaId.isEmpty() && !zAreaId.isEmpty() && !aAreaId.equals(zAreaId)) {
            resMap.put("PORT", "ALL");
            resMap.put("AREGIONID", MapUtils.getString(areaMap, "AREGIONID"));
            resMap.put("AREGIONNAME", MapUtils.getString(areaMap, "AREGIONNAME"));
            resMap.put("ZREGIONID", MapUtils.getString(areaMap, "ZREGIONID"));
            resMap.put("ZREGIONNAME", MapUtils.getString(areaMap, "ZREGIONNAME"));
            resMap.put("AREAID", MapUtils.getString(areaMap, "AREAID"));
            resMap.put("AREANAME", MapUtils.getString(areaMap, "AREANAME"));
        }
        resMap.put("RESOURCES", resources);
        return resMap;
    }

    @Override
    public Map<String, Object> qrySecondDataMakeList(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap<String, Object>();
        List<Map<String, Object>> secondDataMakeList = Lists.newArrayList();
        try{
            List<Map<String, Object>> secondDataMakeListT = orderDealDao.qrySecondDataMakeList(param);
            if(!CollectionUtils.isEmpty(secondDataMakeListT)){
                secondDataMakeList.addAll(secondDataMakeListT);
            }
            resMap.put("flag", true);
        }catch (Exception e){
            resMap.put("flag", false);
            resMap.put("message",e.getMessage());
        }
        resMap.put("data",secondDataMakeList);
        return resMap;
    }

    @Override
    public Map<String, Object> qrySecondResMakeList(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap<String, Object>();
        List<Map<String, Object>> secondResMakeList = Lists.newArrayList();
        try{
            List<Map<String, Object>> secondResMakeListT = orderDealDao.qrySecondResMakeList(param);
            if(!CollectionUtils.isEmpty(secondResMakeListT)){
                secondResMakeList.addAll(secondResMakeListT);
            }
            resMap.put("flag", true);
        }catch (Exception e){
            resMap.put("flag", false);
            resMap.put("message",e.getMessage());
        }
        resMap.put("data",secondResMakeList);
        return resMap;
    }

    @Override
    public Map<String, Object> qrySubLocalTestDataList(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap<String, Object>();
        List<Map<String, Object>> secondDataMakeList = Lists.newArrayList();
        try{
            List<Map<String, Object>> secondDataMakeListT = orderDealDao.qrySubLocalTestDataList(param);
            if(!CollectionUtils.isEmpty(secondDataMakeListT)){
                secondDataMakeList.addAll(secondDataMakeListT);
            }
            resMap.put("flag", true);
        }catch (Exception e){
            resMap.put("flag", false);
            resMap.put("message",e.getMessage());
        }
        resMap.put("data",secondDataMakeList);
        return resMap;
    }

    /*
     * @Override public List<Map<String,Object>> qryJob(Map<String, Object> params) { String operStaffId =
     * ThreadLocalInfoHolder.getLoginUser().getUserId(); Map<String, Object> staffMap =
     * orderDealDao.getOperStaffInfo(Integer.valueOf(operStaffId)); String areaId = MapUtils.getString(staffMap,
     * "AREA_ID"); String workName = MapUtils.getString(params, "workName"); if ("dataMake".equals(workName)){ workName
     * = "专业配置"; } else if ("resConstruct".equals(workName)) { workName = "资源施工"; } else if ("outside".equals(workName))
     * { workName = "外线施工"; } return orderDealDao.qryJob(areaId,workName); }
     */

    @Override
    public List<Map<String, Object>> qryJob(Map<String, Object> params) {
        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        Map<String, Object> staffMap = orderDealDao.getOperStaffInfo(Integer.valueOf(operStaffId));
        boolean searchFlag = MapUtil.getBoolean(params, "searchFlag");
        String areaId = MapUtils.getString(staffMap, "AREA_ID");
        String orgId = MapUtils.getString(staffMap, "ORG_ID");
        String workNameCode = MapUtils.getString(params, "workName");
        String workName = "";
        if ("dataMake".equals(workNameCode) || "dataMakeUser".equals(workNameCode)) {
            workName = "专业配置";
        } else if ("resConstruct".equals(workNameCode) || "resConstructUser".equals(workNameCode)) {
            workName = "资源施工";
        } else if ("outside".equals(workNameCode) || "outsideUser".equals(workNameCode)) {
            workName = "外线施工";
        }
        Map<String, Object> paramMaps = new HashMap<String, Object>();
        paramMaps.put("areaId", areaId);
        paramMaps.put("orgId", orderDealDao.qryBelongCompany(orgId));
        paramMaps.put("workName", workName);
        String selectTypeFlag = "";
        if (params.containsKey("selectTypeFlag")){
            selectTypeFlag = MapUtils.getString(params, "selectTypeFlag");
            if (searchFlag && "job".equals(selectTypeFlag)){
                String searchDataName = MapUtil.getString(params, "searchDataName");
                paramMaps.put("searchDataName", searchDataName);
            }
        }
        List<Map<String, Object>> resList = orderDealDao.qryJob(paramMaps);
        if ("dataMakeUser".equals(workNameCode) || "resConstructUser".equals(workNameCode) || "outsideUser".equals(workNameCode)) {
            if (searchFlag && "user".equals(selectTypeFlag)){
                String searchDataName = MapUtil.getString(params, "searchDataName");
                paramMaps.put("searchDataName", searchDataName);
            }
            List<Map<String, Object>> userResList = new ArrayList<Map<String, Object>>();
            for (Map<String, Object> map : resList) {
                map.put("isParent", true);
                map.put("pId", "-1");
                map.put("checked", false);
                paramMaps.put("jobId", MapUtils.getString(map, "id"));
                List<Map<String, Object>> userList = orderDealDao.qryUserByJob(paramMaps);
                userResList.addAll(userList);
            }
            if(searchFlag){
                resList.clear();
            }
            resList.addAll(userResList);
        }
        return resList;
    }

    /*
     * @Override public List<Map<String, Object>> qryDispObj(Map<String, Object> params) { String orderId =
     * MapUtils.getString(params, "orderId"); List<Map<String, Object>> dispObj = orderDealDao.qryDispObj(orderId); for
     * (int i = 0; i < dispObj.size() ; i++) { String dispObjId = MapUtils.getString(dispObj.get(i), "DISP_OBJ_ID");
     * String[] jobId = dispObjId.split(","); StringBuffer jobIdStr = new StringBuffer(); for (int j = 0; j <
     * jobId.length ; j++) { Map<String, Object> jobNameMap = orderDealDao.findJobName(jobId[j]); if (j == 0) {
     * jobIdStr.append(MapUtils.getString(jobNameMap, "NAME")); }else { jobIdStr.append("," +
     * MapUtils.getString(jobNameMap, "NAME")); } } dispObj.get(i).put("JOBNAME",jobIdStr.toString()); } return dispObj;
     * }
     */

    @Override
    public List<Map<String, Object>> qryDispObjByOrderId(Map<String, Object> params) {
        String orderId = MapUtils.getString(params, "orderId");
        List<Map<String, Object>> dispObj = orderDealDao.qryDispObjByOrderId(orderId);
        for (int i = 0; i < dispObj.size(); i++) {
            StringBuffer jobIdStr = new StringBuffer();
            StringBuffer userNameStr = new StringBuffer();
            List<Map<String, Object>> jobObj = new ArrayList<Map<String, Object>>();
            List<Map<String, Object>> userObj = new ArrayList<Map<String, Object>>();
            List<Object> dispObjMapList = JSON.parseArray(MapUtils.getString(dispObj.get(i), "DISP_OBJ_LIST"));
            String tacheId = MapUtils.getString(dispObj.get(i), "TACHE_ID");
            for (Object object : dispObjMapList) {
                Map<String, Object> dispObjMap = (Map<String, Object>) object;
                // 数据库查到的对象pId如果是-1说明该对象是岗位 不是-1是人员
                //if (!"-1".equals(MapUtils.getString(dispObjMap, "pId")) && "500001158".equals(tacheId)) {
                if (!"-1".equals(MapUtils.getString(dispObjMap, "pId"))) {
                    userNameStr.append(MapUtils.getString(dispObjMap, "name")).append(",");
                    userObj.add(dispObjMap);
                } else {
                    jobIdStr.append(MapUtils.getString(dispObjMap, "name")).append(",");
                    jobObj.add(dispObjMap);
                }
            }
            if (!StringUtils.isEmpty(userNameStr.toString())) {
                String userNameStrS = userNameStr.substring(0, userNameStr.length() - 1);
                dispObj.get(i).put("USERNAME", userNameStrS);
                dispObj.get(i).put("USEROBJ", userObj);
            }
            if (!StringUtils.isEmpty(jobIdStr.toString())) {
                String jobIdStrS = jobIdStr.substring(0, jobIdStr.length() - 1);
                dispObj.get(i).put("JOBNAME", jobIdStrS);
                dispObj.get(i).put("JOBOBJ", jobObj);
            }
        }
        return dispObj;
    }

    @Override
    public List<Map<String, Object>> qryTacheDealUserByOrderId(Map<String, Object> params) {
        String orderId = MapUtils.getString(params, "orderId");
        List<Map<String, Object>> dispObj = orderDealDao.qryDispObjByOrderId(orderId);
        return dispObj;
    }

    /**
     * 调单入库方法，抽取出来是为了事务控制
     */
    @Override
    public String insertDispatchOrder(Map<String, Object> params) throws Exception {
        String dispatchOrderId = "";
        try {
            //判断数据来源
            if(BasicCode.SECOND.equals(MapUtils.getString(params, "sysResource"))){ //二干下发
                //判断是否复用上级调单
                if (BasicCode.NOT_REUSE.equals(MapUtils.getString(params, "oneDryValue"))){ //不复用
                    //不复用上级调单，提交时插入新的一条数据，然后更新电路信息表，和关系表数据
                    int dispatchId = insertDispatchOrderInfo(params);
                    dispatchOrderId = String.valueOf(dispatchId);
                    logger.info(">>>>>>>>>>>>>>>>>>>>>>dispatchOrderId:" + dispatchOrderId);
                    updateDispatchOrderIdToRelateTable(params, dispatchId);
                }else{ //复用上级调单
                    //复用上级调单号时，判断库里有没有一条调单号一样，调单来源为local的数据，如果没有插入一条新的，如果有则更新
                    String dispatchNum = MapUtils.getMap(params, "dispatchOrderData").get("dispatchOrderNum").toString();
                    List<Map<String, Object>> disList = orderDealDao.queryDispatchInfoByDisNumAndSource(dispatchNum, "local", MapUtils.getString(params,"cstOrdId"));
                    if(disList != null && disList.size() > 0){
                        //复用上级调单，如果库中有一条本地的调单记录，根据调单编号更新调单标题和内容
                        updateDispatchInfoByDispatchNum(params, disList);
                        dispatchOrderId = disList.get(0).get("DISPATCH_ORDER_ID").toString();
                    }else{
                        int dispatchId = insertDispatchOrderInfo(params);
                        dispatchOrderId = String.valueOf(dispatchId);
                        updateDispatchOrderIdToRelateTable(params, dispatchId);
                    }
                }
            }else{ //非二干来单
                //根据调单编号查询是否有调单来源为local的记录
                String dispatchNum = MapUtils.getMap(params, "dispatchOrderData").get("dispatchOrderNum").toString();
                List<Map<String, Object>> disList = orderDealDao.queryDispatchInfoByDisNumAndSource(dispatchNum, "local", MapUtils.getString(params, "cstOrdId"));
                if (disList != null && disList.size() > 0) {
                    //更新调单信息
                    updateDispatchInfoByDispatchNum(params, disList);
                    // 此处未关联调度单关联关系的电路需做关联处理处理
                    Map map = new HashMap();
                    String[] srvOrdArr = params.get("srvOrdIdStr").toString().split(",");
                    dispatchOrderId = disList.get(0).get("DISPATCH_ORDER_ID").toString();
                    map.put("dispatchOrderId", disList.get(0).get("DISPATCH_ORDER_ID").toString());
                    map.put("srvOrdId", srvOrdArr);
                    orderDealDao.updateSrvOrdInfo(map);
                } else {
                    int dispatchId = insertDispatchOrderInfo(params);
                    dispatchOrderId = String.valueOf(dispatchId);
                    // 将调单数据和业务订单数据关联起来
                    Map paramMap = new HashMap();
                    String[] srvOrdArr = params.get("srvOrdIdStr").toString().split(",");
                    logger.info(">>>>>>>>>>>>更新调单ID到业务电路开始了：" + srvOrdArr + ">>>>>>>>>>>>");
                    paramMap.put("dispatchOrderId", dispatchId);
                    paramMap.put("srvOrdId", srvOrdArr);
                    orderDealDao.updateSrvOrdInfo(paramMap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
        return dispatchOrderId;
    }

    /**
     * 二干来单更新调单ID到二干本地关系表中
     * @param params
     */
    public void updateDispatchOrderIdToRelateTable(Map<String, Object> params, int dispatchId) throws Exception{
        logger.info(">>>>>>>>>>>>>>>>>>>>>>>>更新调单ID到二干本地关系表中！");
        List<Object> circuitList = JSON.parseArray(MapUtils.getString(params, "circuitData"));
        Map<String, Object> paramMap = new HashMap();
        paramMap.put("dispatchOrderId", dispatchId);
        if(circuitList != null && circuitList.size() > 0){
            for (Object obj : circuitList){
                Map<String, Object> objMap = (Map<String, Object>) obj;
                paramMap.put("cstOrdId", MapUtils.getString(objMap, "CST_ORD_ID"));
                paramMap.put("srvOrdId", MapUtils.getString(objMap, "SRV_ORD_ID"));
                paramMap.put("orderId", MapUtils.getString(objMap, "ORDER_ID"));
                //根据cst_ord_id、srv_ord_id、order_id更新关联表中的调单数据
                orderDealDao.updateDispatchOrderIdToRelateTable(paramMap);
            }
        }
        logger.info(">>>>>>>>>>>>>>>>>>>>>>>>完成调单ID更新到二干本地关系表中！");
    }

    /**
     * 根据调单编号更新调单信息
     */
    public void updateDispatchInfoByDispatchNum(Map<String, Object> params, List<Map<String, Object>> list) throws Exception{
        // 根据调度编号判断调单表中是否已有调单数据
//        String dispatchNum = MapUtils.getMap(params, "dispatchOrderData").get("dispatchOrderNum").toString();
//        List<Map<String, Object>> list = orderDealDao.queryDispatchInfoByDispatchNum(dispatchNum);
        if (list != null && list.size() > 0) {
            // 将新的调单内容更新到数据中
            Map<String, Object> paramMap = new HashMap();
            paramMap.put("dispatchId", list.get(0).get("DISPATCH_ORDER_ID"));
            paramMap.put("dispatchTitle",
                    MapUtils.getMap(params, "dispatchOrderData").get("dispatchOrderName").toString());
            paramMap.put("dispatchText",
                    MapUtils.getMap(params, "dispatchOrderData").get("dispatchOrderText").toString());
            paramMap.put("remark", MapUtils.getString(params, "remark"));
            orderDealDao.updateDispatchOrderInfo(paramMap);
            logger.info("<<<<<<<<<<<调单信息更新完成<<<<<<<<<<<<");
            // 将历史调单信息更新到历史表中
            Map<String, Object> hisParam = new HashMap();
            hisParam.put("dispatchId", list.get(0).get("DISPATCH_ORDER_ID").toString());
            hisParam.put("dispatchTitle", list.get(0).get("DISPATCH_TITLE").toString());
            hisParam.put("dispatchText", list.get(0).get("DISPATCH_TEXT").toString());
            hisParam.put("dispatchNum", list.get(0).get("DISPATCH_ORDER_NO").toString());
            hisParam.put("remark", list.get(0).get("REMARK").toString());
            orderDealDao.insertDispatchInfo(hisParam);
            logger.info("<<<<<<<<<<<<调单历史数据插入调单历史表成功<<<<<<<<<<<<");
        }
//        return list;
    }
    /**
     * 插入调单数据方法
     * @param params
     */
    public int insertDispatchOrderInfo(Map<String, Object> params) throws Exception{
        // 调单入库
        Map<String, Object> dispatchDataMap = new HashMap<String, Object>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> orderDataMap = orderDealDao.qryOrderData(MapUtils.getString(params, "woId"));
        Map<String, Object> dispatchOrderData = MapUtils.getMap(params, "dispatchOrderData");
        Map<String, String> operStaffInfoMap = MapUtils.getMap(params, "operStaffInfoMap");
        dispatchDataMap.put("cstOrdId", MapUtils.getString(orderDataMap, "CSTORDID"));
        dispatchDataMap.put("dispatchOrderNo", MapUtils.getString(dispatchOrderData, "dispatchOrderNum"));
        dispatchDataMap.put("staffName", ThreadLocalInfoHolder.getLoginUser().getUserName());
        dispatchDataMap.put("staffTel", MapUtils.getString(operStaffInfoMap, "USER_PHONE"));
        dispatchDataMap.put("staffOrg", MapUtils.getString(operStaffInfoMap, "ORG_NAME"));
        dispatchDataMap.put("issuer", null);
        dispatchDataMap.put("sendDate", df.format(new Date()));
        dispatchDataMap.put("dispatchType", null);
        dispatchDataMap.put("dispatchGrade", null);
        dispatchDataMap.put("dispatchUrgency", null);
        dispatchDataMap.put("dispatchTitle", MapUtils.getString(dispatchOrderData, "dispatchOrderName"));
        dispatchDataMap.put("dispatchSendOrg", null);
        dispatchDataMap.put("dispatchCopyOrg", null);
        dispatchDataMap.put("dispatchText", MapUtils.getString(dispatchOrderData, "dispatchOrderText"));
        dispatchDataMap.put("remark", MapUtils.getString(params, "remark"));
        dispatchDataMap.put("changeBeforeText", null);
        dispatchDataMap.put("changeAfterText", null);
        dispatchDataMap.put("dispatchSource", "local");
        // 查询当前序列的值，即为刚插入调单数据的ID
        int dispatchId = insertOrderInfoDao.querySequence("SEQ_GOM_BDW_DISPATCH_ORDER.NEXTVAL");
        dispatchDataMap.put("dispatchOrderId", dispatchId);
        orderDealDao.insertDispatchOrder(dispatchDataMap);
        return dispatchId;
    }

    /**
     * 保存电路对应的专业配置相关信息
     *
     * @param param
     * @return
     */
    @Override
    public Map<String, Object> saveSpecialtyConfigInfo(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap();
        try {
            //电路调度环节指定下游环节处理对象
            List<Map<String, Object>> tacheDealUserDataList = new ArrayList<>();
            if (param.containsKey("tacheDealUserData")){
                tacheDealUserDataList = (List<Map<String, Object>>) param.get("tacheDealUserData");
            }

            String keyNote = MapUtils.getString(param, "masterValue");
            Map<String, Object> configMap = MapUtils.getMap(param, "specialtyConfig", new HashMap());
            Map<String, Object> configNameMap = MapUtils.getMap(param, "specialtyConfigName", new HashMap());
            Map<String, Object> flowSpecialDataMap = MapUtils.getMap(param, "flowSpecialData", new HashMap());
            // 默认值1，代表电路调度配置的专业区域信息，如果不传值就是默认
            String newCreateResource = "1";
            if (param.keySet().contains("newCreateResource")) {
                // 0代表电路调度环节保存的新建资源录入派发区域信息
                if ("0".equals(MapUtils.getString(param, "newCreateResource"))) {
                    newCreateResource = MapUtils.getString(param, "newCreateResource");
                }
            }
            /*
             * 是否需要网络施工标识--云组网业务 0：需要；1：不需要；
             * @author guanzhao
             * @date 2020/11/4
             *
             */
            String constructFlag = "1";
            if (param.keySet().contains("constructFlag")) {
                constructFlag = MapUtils.getString(param, "constructFlag");
            }
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) param.get("dataInfo");
            if (dataList.size() > 0) {
                for (Map<String, Object> map : dataList) {
                    String configInfo = JSONObject.toJSON(configMap).toString();
                    String configInfoName = JSONObject.toJSON(configNameMap).toString();
                    String flowSpecialData = JSONObject.toJSON(flowSpecialDataMap).toString();
                    String cstOrdId = MapUtils.getString(map, "CST_ORD_ID");
                    String srvOrdId = MapUtils.getString(map, "SRV_ORD_ID");
                    String orderId = MapUtils.getString(map, "ORDER_ID");
                    Map<String, Object> paramMap = new HashMap();
                    paramMap.put("srvOrdId", srvOrdId);
                    paramMap.put("orderId", orderId);
                    paramMap.put("keyNote", keyNote);
                    paramMap.put("cstOrdId", cstOrdId);
                    paramMap.put("configInfo", configInfo);
                    paramMap.put("configInfoName", configInfoName);
                    paramMap.put("flowSpecialData", flowSpecialData);
                    paramMap.put("newCreateResource", newCreateResource);
                    paramMap.put("constructFlag", constructFlag);
                    //入库之前先判断下表中是否有记录，如果有记录则更新记录，否则需要插入记录
                    Map<String, Object> returnMap = orderDealDao.queryConfigInfoBySrvOrdIdRes(paramMap);
                    if (returnMap != null){
                        orderDealDao.updateConfigInfoBySrvOrdIdRes(paramMap);
                    } else {
                        orderDealDao.saveSpecialtyConfigInfo(paramMap);
                        /*Map<String, Object> belongMap = new HashMap();
                        belongMap.put("srvOrdId", srvOrdId);
                        belongMap.put("cstOrdId", cstOrdId);*/
                        Map<String, Object> orderBelongSys = orderQrySecondaryDao.qrySrvOrderBelongSys(paramMap);
                        if (BasicCode.LOCAL.equals(MapUtils.getString(orderBelongSys, "SYSTEM_RESOURCE"))){
                            // 更新订单业务表中的状态为已配置
                            orderDealDao.updateStateBySrvOrdId(paramMap);
                        }else if(BasicCode.SECOND.equals(MapUtils.getString(orderBelongSys, "SYSTEM_RESOURCE"))){
                            //二干下发的单子修改关联表的状态
                            orderDealDao.updateStateRelateBySrvOrdId(orderId);
                        }
                    }
                    if(!ListUtil.isEmpty(tacheDealUserDataList)){
                        this.saveTacheDealUser(tacheDealUserDataList, orderId);
                    }
                }
            }
            resMap.put("success", true);
            resMap.put("message", "电路对应的配置信息保存成功");
        } catch (Exception e) {
            resMap.put("success", false);
            resMap.put("message", e.getMessage());
            logger.info(">>>>>>保存专业配置信息出错：" + e);
        }
        return resMap;
    }

    private void saveTacheDealUser(List<Map<String, Object>> tacheDealUserDataList, String orderId){
        for (Map<String, Object> tacheDealUserData : tacheDealUserDataList){
            String tacheIdStr = MapUtils.getString(tacheDealUserData, "tacheId");
            String tacheId = tacheIdStr.substring(0, tacheIdStr.indexOf("_"));
            String tacheName= tacheIdStr.substring(tacheId.length() + 1, tacheIdStr.length());
            Map<String, String> tacheUserObj = MapUtils.getMap(tacheDealUserData, "tacheUserObj");
            String objName = MapUtils.getString(tacheUserObj, "name");
            String objType = MapUtils.getString(tacheUserObj, "objType");
            String objId = MapUtils.getString(tacheUserObj, "value");
            Map<String, Object> qryMap = new HashMap<>();
            qryMap.put("orderId", orderId);
            qryMap.put("tacheId", tacheId);
            qryMap.put("tacheName", tacheName);
            qryMap.put("flag", "AZ"); //电路调度环节配置的下游环节处理人
            List<Map<String, Object>> dispDateList = orderDealDao.qryDispObjTache(qryMap);
            Map<String, Object> paramsMap = new HashMap<>();
            paramsMap.put("tacheId", tacheId);
            paramsMap.put("tacheName", tacheName);
            paramsMap.put("orderId", orderId);
            paramsMap.put("dispState", "10A");
            String dispObj = objType + "_J!G@F_" + objId;
            paramsMap.put("dispObj", dispObj);
            paramsMap.put("dispObjId", objId);
            paramsMap.put("dispObjList", tacheUserObj.toString());
            paramsMap.put("tacheName", tacheName);
            if (!CollectionUtils.isEmpty(dispDateList)) {
                orderDealDao.updateDispObjConfig(paramsMap);
            } else {
                orderDealDao.insertDispObj(paramsMap);
            }
        }

    }

    /**
     * 查询电路的专业配置信息进行回显
     *
     * @param param
     * @return
     */
    @Override
    public Map<String, Object> queryPropertyConfig(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap();
        try{
            String cstOrdId = MapUtils.getString(param,"cstOrdId");
            String srvOrdId = MapUtils.getString(param,"srvOrdId");
            String orderId = MapUtils.getString(param, "orderId");
            String activeType = MapUtils.getString(param,"activeType");
            // 默认值1，代表电路调度配置的专业区域信息，如果不传值就是默认
            String newCreateResource = "1";
            if (param.keySet().contains("newCreateResource")) {
                // 0代表电路调度环节保存的新建资源录入派发区域信息
                if ("0".equals(MapUtils.getString(param, "newCreateResource"))) {
                    newCreateResource = MapUtils.getString(param, "newCreateResource");
                }
            }
            param.put("newCreateResource", newCreateResource);
            //String newCreateResource = MapUtils.getString(param,"newCreateResource");
            Map<String, Object> map = orderDealDao.queryConfigInfoBySrvOrdIdRes(param);
            if(MapUtils.isEmpty(map) && "102,104,105".indexOf(activeType) != -1){
                //获取上一个业务单
                //Map<String, Object> srvOrderMap = orderDealDao.qryLastSrvOrder(srvOrdId, cstOrdId);
                Map<String, Object> srvOrderMap = orderDetailsDao.queryRelevanceSrvOrdId(srvOrdId, "101");
                if(!MapUtils.isEmpty(srvOrderMap)){
                    Map<String, Object> secLocatRelateMap = new HashMap<String, Object>();
                    //先查询是否由二干下发：：：：：有值是二干下发，没值不是二干下发
                    Map<String, Object> ifFromSecondaryMap = orderQrySecondaryDao.qryIfFromSecondary(orderId);
                    if(!MapUtils.isEmpty(ifFromSecondaryMap)){ //二干下发
                        String instanceId = MapUtils.getString(ifFromSecondaryMap, "INSTANCE_ID");
                        secLocatRelateMap = orderDealDao.qrySecLocalRelateInfo(instanceId,MapUtils.getString(srvOrderMap, "ORDER_ID"));
                        srvOrderMap.put("newCreateResource", newCreateResource);
                        srvOrderMap.put("srvOrdId", MapUtils.getString(secLocatRelateMap, "SRV_ORD_ID"));
                        srvOrderMap.put("cstOrdId", MapUtils.getString(secLocatRelateMap, "CST_ORD_ID"));
                        srvOrderMap.put("orderId", MapUtils.getString(secLocatRelateMap, "ORDER_ID"));
                    } else {
                        srvOrderMap.put("newCreateResource", newCreateResource);
                        srvOrderMap.put("srvOrdId", MapUtils.getString(srvOrderMap, "SRV_ORD_ID"));
                        srvOrderMap.put("cstOrdId", MapUtils.getString(srvOrderMap, "CST_ORD_ID"));
                        srvOrderMap.put("orderId", MapUtils.getString(srvOrderMap, "ORDER_ID"));

                    }
                    map = orderDealDao.queryConfigInfoBySrvOrdIdRes(srvOrderMap);
                }
            }
            if(MapUtils.isEmpty(map)){
                resMap.put("success", true);
                resMap.put("message","102,104,105");
            }else {
                String keyNote = MapUtils.getString(map, "KEYNOTE");
                String constructFlag = MapUtils.getString(map, "IF_CONSTRUCT");
                String keyName = MapUtils.getString(map, "ORG_NAME");
                String specialtyInfo = MapUtils.getString(map, "SPECIALTY_INFO");
                String specialtyInfoName = MapUtils.getString(map, "SPECIALTY_INFO_NAME");
                Map<String, Object> configMap = (Map<String, Object>) JSONObject.parseObject(specialtyInfo);
                Map<String, Object> configNameMap = (Map<String, Object>) JSONObject.parseObject(specialtyInfoName);
                resMap.put("success", true);
                resMap.put("message", "查询电路的专业配置信息成功");
                resMap.put("keyNote", keyNote);
                resMap.put("constructFlag", constructFlag);
                resMap.put("keyName", keyName);
                resMap.put("configInfo", configMap);
                resMap.put("configInfoName", configNameMap);
            }
        } catch (Exception e) {
            resMap.put("success", false);
            resMap.put("message", "查询电路的专业配置信息失败：" + e);
            logger.info(">>>>>>查询电路的专业配置信息：" + e);
        }
        return resMap;
    }

    /**
     * 查询拼接调单标题、调单内容的信息
     *
     * @param param
     * @return
     */
    @Override
    public Map<String, Object> getDispatchInfo(Map<String, Object> param) throws Exception {
        Map<String, Object> resMap = new HashMap();
        try {
            //获取勾选电路包含的调单ID
            String dispatchOrderId = MapUtils.getString(param, "dispatchOrderId");
            logger.info(">>>>>>>>>>>>勾选电路包含的调单ID："+ dispatchOrderId +">>>>>>>>>>>>");
            //查询该调单下关联的所有电路的电路ID
            String srvOrdIds = "";
            if (!"".equals(dispatchOrderId)){
                Map<String, Object> srvOrdMap = orderDealDao.querySrvOrdIdsByDispatchOrderId(dispatchOrderId);
                srvOrdIds = MapUtils.getString(srvOrdMap,"SRVORDIDS");
            }
            logger.info(">>>>>>>>>>>>勾选调单关联的定单ID："+srvOrdIds + ">>>>>>>>>>>>");
            //获取勾选电路的电路信息
            List<Map<String, Object>> paramList = (List<Map<String, Object>>) param.get("rowsData");
            //将没有调单关联的电路ID拼接到srvOrdIds
            for(int i = 0; i < paramList.size(); i++){
                if ("".equals(MapUtils.getString(paramList.get(i), "DISPATCH_ORDER_ID"))) {
                    if ("".equals(srvOrdIds)){
                        srvOrdIds = MapUtils.getString(paramList.get(i), "SRV_ORD_ID");
                    }else{
                        srvOrdIds = srvOrdIds +"," + MapUtils.getString(paramList.get(i), "SRV_ORD_ID");
                    }
                }
            }
            logger.info(">>>>>>>>>>>>查询拼接调单信息的定单ID:"+ srvOrdIds + ">>>>>>>>>>>>");
            //根据srvOrdIds查询用来拼接调单内容的信息
            String[] srvOrdIdArr = srvOrdIds.split(",");
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("srvOrdIds", srvOrdIdArr);
            List<Map<String, Object>> dispatchTextList = orderDealDao.getCircuitInfo(paramMap);
            resMap.put("dispatchTextList", dispatchTextList);
            // 获取客户名称、动作类型、产品类型信息用来拼接调单标题
            Map<String, Object> map = orderDealDao.getDispatchTitleInfo(param);
            resMap.put("custName", MapUtils.getString(map, "CUST_NAME_CHINESE"));
            resMap.put("operateType", MapUtils.getString(map, "OPERATE_TYPE"));
            resMap.put("productType", MapUtils.getString(map, "PRODUCT_TYPE"));
            resMap.put("countNum", srvOrdIdArr.length);
            // 默认值1，代表电路调度配置的专业区域信息，如果不传值就是默认
            String newCreateResource = "1";
            if (param.keySet().contains("newCreateResource")) {
                // 0代表电路调度环节保存的新建资源录入派发区域信息
                if ("0".equals(org.apache.commons.collections.MapUtils.getString(param, "newCreateResource"))) {
                    newCreateResource = org.apache.commons.collections.MapUtils.getString(param, "newCreateResource");
                }
            }
            resMap.put("newCreateResource", map.get("newCreateResource"));

            // 查询派发了几条电路用来拼接调单标题
//            Map<String, Object> numMap = orderDealDao.queryNumToAppendTitle(param);
//            resMap.put("num", numMap.get("NUM"));
            // 查询电路信息拼接到调单内容里
//            String[] strArr = numMap.get("IDLIST").toString().split(",");
//            Map<String, Object> paramMap = new HashMap();
//            paramMap.put("srvOrdId", strArr);
//            if (MapUtils.getString(param, "orderIds") != null && !MapUtils.getString(param, "orderIds").equals("")) {
//                String[] orderIds = MapUtils.getString(param, "orderIds").split(",");
//                paramMap.put("orderId", orderIds);
//            } else {
//                paramMap.put("orderId", "");
//            }
//            List<Map<String, Object>> list = orderDealDao.getCircuitInfo(paramMap);
//            resMap.put("textInfo", list);
            resMap.put("success", true);
            resMap.put("message", "获取拼接调单信息成功");
        } catch (Exception e) {
            logger.info("<<<<<<获取拼接调单信息失败");
            resMap.put("success", false);
            resMap.put("message", e.getMessage());
            throw new Exception(e);
        }
        return resMap;
    }

    /**
     * 保存核查反馈信息
     *
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> saveCheckInfo(Map<String, Object> params) {
        Map<String, Object> retMap = new HashMap<>();
        String circuitDataStr = MapUtils.getString(params, "circuitData");
        String formValue = MapUtils.getString(params, "formValue");
        String sysResource = MapUtils.getString(params, "sysResource");
        List<Object> circuitDatalist = JSON.parseArray(circuitDataStr);
        // List<Map<String,Object>> circuitData = new ArrayList<Map<String,Object>>();
        for (Object object : circuitDatalist) {
            Map<String, Object> circuitDataMap = (Map<String, Object>) object;// 取出list里面的值转为map
            String woId = MapUtils.getString(circuitDataMap, "WO_ID");
            String orderId = MapUtils.getString(circuitDataMap, "ORDER_ID");
            String tacheId = MapUtils.getString(circuitDataMap, "TACHE_ID");
            String psId = MapUtils.getString(circuitDataMap, "PS_ID");
            String srvOrdId = MapUtils.getString(circuitDataMap, "SRV_ORD_ID");
            if("second-schedule-lt".equals(sysResource)){
                srvOrdId=MapUtils.getString(circuitDataMap, "RELATE_INFO_ID");
            }
            String areaId = MapUtils.getString(circuitDataMap, "WOAREA");
            String areaA = MapUtils.getString(circuitDataMap, "AREGIONNAME");
            String areaZ = MapUtils.getString(circuitDataMap, "ZREGIONNAME");
            params.put("woId", woId);
            params.put("orderId", orderId);
            params.put("tacheId", tacheId);
            params.put("psId", psId);
            params.put("srvOrdId", srvOrdId);
            params.put("areaId", areaId);
            params.put("areaA", areaA);
            params.put("areaZ", areaZ);
            retMap = checkFeedbackService.updateCheckInfo(params);
            if (!MapUtils.getBoolean(retMap, "success")) {
                return retMap;
            }
        }
        retMap.put("success", true);
        retMap.put("message", "保存反馈信息成功！");
        return retMap;
    }

    @Override
    public Map<String, Object> getListChildFlow(Map<String, String> params) {
        Map<String, Object> childFlowInfo = new HashMap<String, Object>();
        try {
            List<Map<String, Object>> childList = orderDealDao.getListChildFlow(params);
            childFlowInfo.put("flag", true);
            childFlowInfo.put("data", childList);
        } catch (Exception e) {
            childFlowInfo.put("flag", false);
            childFlowInfo.put("data", "查询调单数据失败！" + e);
        }
        return childFlowInfo;
    }

    @Override
    public Map<String, Object> getMainFlowPsId(String orderId) {
        return orderDealDao.getMainFlowPsId(orderId);
    }

    @Override
    public Map<String, Object> queryCheckInfo(Map<String, Object> params) {
        return checkFeedbackService.queryCheckInfo(params);
    }
    @Override
    public Map<String, Object> queryCheckFeedBackInfoByWoId(Map<String, Object> params) {
        return checkFeedbackService.queryCheckFeedBackInfoByWoId(params);
    }

    @Override
    public List<Map<String, Object>> getProvinceName(Map<String, Object> params) {
        List<Map<String, Object>> provinceList = new ArrayList<Map<String, Object>>();
        Map<String, Object> provinceSMap = orderDealDao.getProvinceName(MapUtils.getString(params, "srvOrdId"));
        String sendDepartmentNameStr = MapUtils.getString(provinceSMap, "ATTR_VALUE");
        if (!StringUtils.isEmpty(sendDepartmentNameStr)){
            String[] sendDepartmentName = sendDepartmentNameStr.split(",");
            for (int i = 0; i < sendDepartmentName.length ; i++) {
                Map<String, Object> provinceMap = new HashMap<String, Object>();
                provinceMap.put("province", sendDepartmentName[i]);
                provinceList.add(provinceMap);
            }
        }
        return provinceList;
    }

    @Override
    public Map<String, Object> qrySrvOrderBelongSys(Map<String, Object> params) {
        Map<String, Object> stringObjectMap = orderQrySecondaryDao.qrySrvOrderBelongSys(params);
        return stringObjectMap;
    }

    @Override
    public Map<String, Object> qryParentPsIdBySubOrderId(Map<String, Object> params) {
        Map<String, Object> stringObj = orderQrySecondaryDao.qryParentPsIdBySubOrderId(params);
        if(MapUtil.isEmpty(stringObj)){
            stringObj = orderQrySecondaryDao.qryParentPsIdByOrderId(params);
        }
        return stringObj;
    }

    @Override
    public List<Map<String, Object>> qryParentSubStatusByOrderId(Map<String, Object> params) {
        List<Map<String, Object>> resList = new ArrayList<Map<String, Object>>();
        String orderId = MapUtils.getString(params, "orderId");
        Map<String, Object> parentOrder = orderDealDao.getParentOrder(orderId);
        if(MapUtils.isNotEmpty(parentOrder) && !"".equals(MapUtils.getString(parentOrder, "PARENTORDERID"))){
            orderId = MapUtils.getString(parentOrder, "PARENTORDERID");
        }
        Map<String, Object> provinceMapRe = new HashMap<String, Object>(); //去重
        Map<String, Object> provinceMap = new HashMap<String, Object>();
        provinceMap.put("orderId", orderId);
        provinceMap.put("psId", MapUtils.getString(params, "psId"));
        List<Map<String, Object>> mapsList = orderDealDao.qryParentSubStatusByOrderId(provinceMap);
        if(!CollectionUtils.isEmpty(mapsList)){
            for(Map<String, Object> mapObj : mapsList){
                String tacheCode = MapUtils.getString(mapObj, "TACHE_CODE");
                if(provinceMapRe.get(tacheCode)!=null){
                    if(OrderTrackOperType.WO_ORDER_STATE_2.equals(provinceMapRe.get(tacheCode))){
                        provinceMapRe.put(tacheCode,tacheCode);
                        resList.add(mapObj);
                    }else{
                        continue;
                    }
                }else{
                    provinceMapRe.put(tacheCode,tacheCode);
                    resList.add(mapObj);
                }

            }
        }
        return resList;
    }

    /**
     * 根据工单id或定单id查询工单处理人发送短信
     *
     * @param id
     * @param typeFlag
     * @return
     */
    @Override
    public Map<String, Object> qryUserObjByWoId(String id, String typeFlag) {
        logger.info(">>>>>>>>>>>>>>>调用短信接口>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Map<String, Object> resMap = new HashMap<String, Object>();
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        Map<String, Object> dataMap = orderDealDao.qryWoOrderDispObj(id, typeFlag);
        String dispObjType = MapUtils.getString(dataMap, "DISP_OBJ_TYE");
        String dispObjId = MapUtils.getString(dataMap, "DISP_OBJ_ID");
        String reqFinDate = MapUtils.getString(dataMap, "REQ_FIN_DATE");
        String applyOrdId = MapUtils.getString(dataMap, "APPLY_ORD_ID");
        String applyOrdName = MapUtils.getString(dataMap, "APPLY_ORD_NAME");
        String custNameChinese = MapUtils.getString(dataMap, "CUST_NAME_CHINESE");
        // 申请单编码和申请单标题不为空时
        if (!applyOrdId.equals("") && !applyOrdName.equals("")) {
            List<Map<String, Object>> usersList = new ArrayList<Map<String, Object>>();
            if (BasicCode.DISP_TYPE_ORG.equals(dispObjType)) { // 部门组织
                usersList = orderDealDao.qryUserByDept(dispObjId);
            } else if (BasicCode.DISP_TYPE_JOB.equals(dispObjType)) { // 角色
                usersList = orderDealDao.qryUserByGroup(dispObjId);
            } else if (BasicCode.DISP_TYPE_STAFF.equals(dispObjType)) { // 人员
                usersList = orderDealDao.qryUserById(dispObjId);
            }
            StringBuffer userName = new StringBuffer();
            for (int i = 0; i < usersList.size(); i++) {
                String user = MapUtils.getString(usersList.get(i), "STAFFLOGINNAME");
                if (i == 0) {
                    userName.append(user);
                } else {
                    userName.append("," + user);
                }
            }
            try {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat format = new SimpleDateFormat(" yyyy年MM月dd日 ");
                String reqFinDateStr = format.format(dateFormat.parse(reqFinDate));
                String smsContent = "【本地调度系统】收到客户【" + custNameChinese + "】一条工单主题为【" + applyOrdName + "】，" + "定单编号为【"
                        + applyOrdId + "】,请于" + reqFinDateStr + "处理。";
                paramsMap.put("userName", userName.toString());
                paramsMap.put("tacheId", MapUtils.getString(dataMap, "TACHE_ID"));
                paramsMap.put("tacheName", MapUtils.getString(dataMap, "TACHE_NAME"));
                paramsMap.put("dispatchId", 0);
                paramsMap.put("orderId", MapUtils.getString(dataMap, "ORDER_ID"));
                paramsMap.put("woId", MapUtils.getString(dataMap, "WO_ID"));
                paramsMap.put("smsContent", smsContent);
                paramsMap.put("feedbackTime", reqFinDate);
                sendMessageService.sendMsg(paramsMap);
                resMap.put("success", true);
                resMap.put("message", "调用短信接口成功！");
                logger.info(">>>>>>>>>>>>>>>调用短信接口成功>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            } catch (Exception e) {
                resMap.put("success", false);
                resMap.put("message", "调用短信接口失败！");
                logger.info(">>>>>>>>>>>>>>>调用短信接口失败>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                e.printStackTrace();// ParseException
                throw new RuntimeException("调用短信接口失败：" + e);
            }
        }
        return resMap;
    }

    @Override
    public Map<String, Object> queryDispatchInfoByDispatchIds(Map<String, Object> param) {
        Map<String, Object> returnMap = new HashMap();
        try {
            List<Map<String, Object>> listData = (List<Map<String, Object>>) param.get("rowsData");
            if (listData.size() > 0) {
                Map<String, Object> paramMap = new HashMap();
                // 建一个和listData大小一样的数组
                String[] dispatchIdArr = new String[listData.size()];
                // 循环listData，将每条数据中的SRV_ORD_ID添加到数组srvOrdIdArr中
                for (int i = 0; i < listData.size(); i++) {
                    dispatchIdArr[i] = listData.get(i).get("DISPATCH_ORDER_ID").toString();
                }
                paramMap.put("disPatchIds", dispatchIdArr);
                List<Map<String, Object>> list = orderDealDao.queryDispatchInfoByDispatchIds(paramMap);
                returnMap.put("success", true);
                returnMap.put("dispatchInfo", list);
            } else {
                returnMap.put("success", false);
                returnMap.put("message", "入参不能为空");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info(">>>>>>>>>>>查询调单信息发生异常：" + e.getMessage());
            returnMap.put("success", false);
            returnMap.put("message", e.getMessage());
        }

        return returnMap;
    }

    /**
     * 根据流程定单ID查询流程定单
     *
     * @param params
     * @return
     */
    @Override
    public List<Map<String, Object>> getFlowOrderById(Map<String, Object> params) {
        return orderDealDao.getFlowOrderById(params);
    }

    /**
     * 根据父流程定单ID查询子流程定单
     *
     * @param params
     * @return
     */
    @Override
    public List<Map<String, Object>> getFlowOrderByParentId(Map<String, Object> params) {
        return orderDealDao.getFlowOrderByParentId(params);
    }

    @Override
    public String getFLowTacheCodeById(String paramStr) {
        return orderDealDao.getFLowTacheCodeById(paramStr);
    }

    /**
     * 迭代查询需要的部门ID
     * @param deptId
     * @param deptLevel
     * @param currentLevel
     * @return
     */
    public String iterativeQueryDeptId(String deptId, int deptLevel, int currentLevel){
        logger.info(">>>>>>>>>>>>>>>>>>>>>>deptId:" + deptId +";currentLevel:" + currentLevel + ";deptLevel:" + deptLevel);
        if(deptLevel < currentLevel){
            for(int i = 0; i < (currentLevel - deptLevel); i++){
                Map<String, Object> map = orderDealDao.queryDeptIdByParentDeptId(deptId);
                if (deptLevel != MapUtils.getIntValue(map, "DEPT_LEVEL_ID")){
                    deptId = MapUtils.getString(map, "PARENT_ID");
                }
            }
        }
        logger.info(">>>>>>>>>>>>>>>>>>>>>>>>deptId:" + deptId);
        return deptId;
    }

    /**
     * 查询电路信息
     * @param param
     * @return
     */
    @Override
    public Map<String, Object> querySrvInfoBySrvOrdId(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap();
        try{
            String srvOrdId = MapUtils.getString(param, "srvOrdId");
            Map<String, Object> srvMap = orderDealDao.querySrvInfoBySrvOrdId(srvOrdId);
            resMap.put("success", true);
            resMap.put("result", srvMap);
        }catch(Exception e){
            e.printStackTrace();
            resMap.put("success", false);
            resMap.put("message", e.getMessage());
        }
        return resMap;
    }

    /**
     * 查询关系表中的调单ID
     * @param param
     * @return
     */
    @Override
    public Map<String, Object> queryDispatchOrderIdFromRelateTable(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap();
        try{
            Map<String, Object> map = orderDealDao.queryDispatchOrderIdFromRelateTable(param);
            resMap.put("success", true);
            resMap.put("result", map);
        }catch(Exception e){
            e.printStackTrace();
            resMap.put("success", false);
            resMap.put("message", e.getMessage());
        }
        return resMap;
    }
    /*
     * 发起页面受理区域
     * @author ren.jiahang
     * @date 2019/6/20 15:37
     * @param params
     * @return java.util.List
     */
    @Override
    public List qryCreatePageHandleDep(Map<String, Object> params) {
        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        Map<String, Object> staffMap = orderDealDao.getOperStaffInfo(Integer.valueOf(operStaffId));
        Long orgId = MapUtil.getLong(staffMap, "ORG_ID");
        //Map<String, Object> orgMap = orderDealDao.getDeptInfo(orgId);
        String flag = MapUtils.getString(params, "flag");
        //查询该部门所在省份的org_id
        Map<String, Object> provinceMap = orderDealDao.getProviceOrg(orgId);
        Long parentId = MapUtils.getLong(provinceMap, "ORG_ID");
        Map<String, Object> paramMap =  new HashMap<String, Object>();
        paramMap.put("provienceOrgId",parentId);
        paramMap.put("belongOrgId",orgId);
        List<Map<String, Object>> deptList = new ArrayList<>();
        String [] zhixs ={"4","5","13","27","29"}; //直辖市 (北京，重庆，海南，上海，天津)
        boolean iszhixs=false;
        for(String zhixsStr:zhixs){
            if(zhixsStr.equals(parentId.toString())){
                iszhixs=true;
            }
        }
        if(iszhixs){
            paramMap.put("fuzzyRegionName", "分公司");
            paramMap.put("parentId",parentId);
            deptList = orderDealDao.getDeptInfoListRel(paramMap);
        }else{
            deptList = orderDealDao.qryCreatePageHandleDep(paramMap);
        }
        return deptList;
    }

    @Override
    public Map<String, Object> sendSecondScheduleLTResAssign(String flagSys,Map<String, Object> resMapSec) {
        String srvOrderIdRes = MapUtil.getString(resMapSec, "SRV_ORD_ID");
        Map<String, Object> resMap = new HashMap<String, Object>();
        resMap.put("success", true);
        /*// 查询是否成功调过资源汇总接口
        int num = orderDealDao.qryInterResult(srvOrderIdRes, "BusinessAutoAssign");
        if (num < 1) {

        }*/
        // 调用业务电路汇总接口(允许多次调用)
        Map<String, Object> resConfigParams = new HashMap<String, Object>();
        resConfigParams.put("flag", flagSys);
        resConfigParams.put("srvOrdId", srvOrderIdRes);
        resConfigParams.put("relaCrmOrderCodes", resMapSec.get("relaCrmOrderCodes"));
        Map retmap = businessAutoAssignServiceIntf.businessAutoAssign(resConfigParams);
        if (!"成功".equals(MapUtils.getString(retmap, "returncode"))) {
            resMap.put("success", false);
            resMap.put("message", "派单失败!调用资源业务电路汇总接口异常，异常原因：" + MapUtils.getString(retmap, "returndec"));
            return resMap;
        }
        return resMap;
    }

    /*
     * 查询序列
     * @author ren.jiahang
     * @date 2019/1/5 15:24
     * @param tableName
     * @return int
     */
    @Override
    public int querySequence(String tableName) {
        return insertOrderInfoDao.querySequence("seq_" + tableName + ".nextval");
    }

    @Override
    public Map<String, Object> exceptionOrderNoticeBack(Map<String, Object> params) {
        logger.info(">>>>>>>>>>>>>>>>>一干下发异常单，需要二干报竣，所以这里要退单到本地报竣环节>>>>>>>>>>>>>>>>>>>>>>>");
        Map<String, Object> resMap = new HashMap<String, Object>();
        Map<String, Object> operStaffInfoMap = new HashMap<String, Object>();
        String operStaffId = "-1";
        //String operStaffName = "一干";
        operStaffInfoMap.put("ORG_ID", "-1");
        operStaffInfoMap.put("ORG_NAME", "一干");
        operStaffInfoMap.put("USER_PHONE", "-1");
        operStaffInfoMap.put("USER_EMAIL", "-1");
        operStaffInfoMap.put("USER_REAL_NAME", "一干");
        operStaffInfoMap.put("USER_ID", operStaffId);
        String woId = MapUtils.getString(params, "woId");
        String orderId = MapUtils.getString(params, "orderId");
        String tacheId = MapUtils.getString(params, "tacheId");
        String remark = MapUtils.getString(params, "remark");
        DistributeLock lock = new DatabaseLock(woId);
        try {
            List<FlowRollBackReasonDTO> flowRollBackReasonDTOs = flowActionHandler.queryRollBackReasons(woId);
            FlowRollBackReasonDTO flowRollBackReasonDTO = null;
            String toTacheId = BasicCode.LOCAL_TEST;
            for (int i = 0; i < flowRollBackReasonDTOs.size(); i++) {
                FlowRollBackReasonDTO _flowRollBackReasonDTO = flowRollBackReasonDTOs.get(i);
                FlowTacheDTO flowTacheDTO = _flowRollBackReasonDTO.getToTache();
                String _toTacheId = flowTacheDTO.getId();
                if (toTacheId.equals(_toTacheId)) {
                    flowRollBackReasonDTO = _flowRollBackReasonDTO;
                }
            }
            lock.lock();
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>加锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            orderDealDao.updateWoStateByWoId(woId,OrderTrackOperType.WO_ORDER_STATE_2);
            flowActionHandler.rollBackWo(operStaffId, woId, flowRollBackReasonDTO, remark);
            //作废之前跨域全程调测环节的单子  将工单状态修改为作废
            // 起租和跨域全程调测环节都是异常节点退单，会自动作废工单，这里只有从起租退单到本地测试时需要手动作废跨域全程调测环节工单
            Map<String, Object> crossTestAndTestMap = orderDealDao.qryCrossTest(orderId);
            if (MapUtils.isNotEmpty(crossTestAndTestMap)){
                orderDealDao.updateWoStateByWoId(MapUtil.getString(crossTestAndTestMap, "WOID"),OrderTrackOperType.WO_ORDER_STATE_5);
            }
            Map<String, Object> logDataMap = new HashMap<String, Object>();
            logDataMap.put("woId", woId);
            logDataMap.put("orderId", orderId);
            logDataMap.put("remark", remark);
            logDataMap.put("tacheId", tacheId);
            logDataMap.put("operType", OrderTrackOperType.OPER_TYPE_5);
            logDataMap.put("operStaffInfoMap", operStaffInfoMap);
            logDataMap.put("action", "退单");
            logDataMap.put("trackMessage", "[一干通知要求][退单]");
            tacheDealLogIntf.addTrackLog(logDataMap);
            /*Map<String, Object> operLogMap = new HashMap<String, Object>();
            operLogMap.put("orderId", orderId);
            operLogMap.put("woOrdId", woId);
            operLogMap.put("trackOrgId", MapUtils.getString(operStaffInfoMap, "ORG_ID"));
            operLogMap.put("trackOrgName", MapUtils.getString(operStaffInfoMap, "ORG_NAME"));
            operLogMap.put("trackDate", new java.sql.Date(new java.util.Date().getTime()));
            operLogMap.put("createDate", new java.sql.Date(new java.util.Date().getTime()));
            operLogMap.put("trackStaffId", operStaffId);
            operLogMap.put("trackStaffName", operStaffName);
            operLogMap.put("trackStaffPhone", MapUtils.getString(operStaffInfoMap, "USER_PHONE"));
            operLogMap.put("trackStaffEmail", MapUtils.getString(operStaffInfoMap, "USER_EMAIL"));
            String trackMessage = "[一干通知要求][退单]";
            operLogMap.put("trackMessage", trackMessage);
            operLogMap.put("trackContent", "[退单]" + remark);
            operLogMap.put("operType", OrderTrackOperType.OPER_TYPE_5);
            orderDealDao.insertTrackLogInfo(operLogMap);*/
            resMap.put("success", true);
            resMap.put("message", "回退成功!");
        } catch (Exception e) {
            e.printStackTrace();
            resMap.put("success", false);
            resMap.put("message", "回退失败!" + e);
        } finally {
            if(lock!=null){
                lock.unlock();
            }
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>finally主动解锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        }
        return resMap;
    }

    public Map<String, Object> sendlocalScheduleLTRentRes(Map<String, Object> params) {
        Map<String, Object> resMap = new HashMap<String, Object>();
        String relate_info_id = MapUtil.getString(params, "relate_info_id");
        resMap.put("success", true);
        // 查询是否成功调过资源归档接口
        int num = orderDealDao.qryInterResult(relate_info_id,"BusinessArchive");
        if (num<1){
            Map<String, Object> paramsMap = new HashMap<String, Object>();
            paramsMap.put("flag", BasicCode.SECONDARY);
            paramsMap.put("srvOrdId", relate_info_id);
            Map map =  businessArchiveServiceIntf.businessArchive(paramsMap);
            //Map map =  businessArchiveServiceIntf.businessArchiveLocalSche(relate_info_id);
            if(!"成功".equals(MapUtils.getString(map,"returncode"))){
                resMap.put("success", false);
                resMap.put("message", "派单失败!调用本地调度资源归档接口异常，异常原因：" +MapUtils.getString(map,"returndec") );
                return resMap;
            }
        }
        return resMap;
    }

    @Override
    public Map<String, Object> sendSecondScheduleLTRes(Map<String, Object> params) {
        Map<String, Object> resMap = new HashMap<String, Object>();
        String srvOrdId = MapUtil.getString(params, "srvOrdId");
        resMap.put("success", true);
        // 查询是否成功调过资源归档接口
        int num = orderDealDao.qryInterResult(srvOrdId,"BusinessArchive");
        if (num < 1){
            Map<String, Object> paramsMap = new HashMap<String, Object>();
            paramsMap.put("flag", BasicCode.LOCALBUILD);
            paramsMap.put("srvOrdId", srvOrdId);
            Map map =  businessArchiveServiceIntf.businessArchive(paramsMap);
            if(!"成功".equals(MapUtils.getString(map,"returncode"))){
                resMap.put("success", false);
                resMap.put("message", "派单失败!调用资源归档接口异常，异常原因：" +MapUtils.getString(map,"returndec") );
                return resMap;
            }
        }
        return resMap;
    }

    @Override
    public void insertTacheLog(Map<String, Object> params) {
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        String orderId = MapUtils.getString(params, "orderId");
        String woId = MapUtils.getString(params, "woId");
        String operStaffName = MapUtil.getString(params, "operStaffName");
        String action = MapUtil.getString(params, "action"); //操作
        String remark = MapUtil.getString(params, "remark"); //备注
        String operType = MapUtil.getString(params, "operType"); //操作类型 OrderTrackOperType
        paramsMap.put("orderId",orderId);
        paramsMap.put("woOrdId", woId);
        paramsMap.put("trackOrgId", MapUtil.getString(params, "ORG_ID"));
        paramsMap.put("trackOrgName", MapUtil.getString(params, "ORG_NAME"));
        paramsMap.put("trackDate", new java.sql.Date(new java.util.Date().getTime()));
        paramsMap.put("createDate", new java.sql.Date(new java.util.Date().getTime()));
        paramsMap.put("trackStaffId", MapUtil.getString(params, "operStaffId"));
        paramsMap.put("trackStaffName", operStaffName);
        paramsMap.put("trackStaffPhone", MapUtils.getString(params, "USER_PHONE"));
        paramsMap.put("trackStaffEmail", MapUtils.getString(params, "USER_EMAIL"));
        String trackMessage = "[" + operStaffName + "将工单单号：" + woId + "][" + action + "]";
        paramsMap.put("trackMessage", trackMessage);
        String trackContent = "[" + action + "]";
        if (remark != null ){
            trackContent = trackContent + remark ;
        }
        paramsMap.put("trackContent", trackContent);
        paramsMap.put("operType", operType);
        orderDealDao.insertTrackLogInfo(paramsMap);

    }

    @Override
    public boolean queryIfSupplementOrder(String orderIdStr) {
        boolean ifSupplementOrder = false;
        String[] orderIds = orderIdStr.split(",");
        List<String> orderIdList = Arrays.asList(orderIds);
        List<String> woOrderStateList = orderDealDao.queryIfSupplementOrder(orderIdList);
        for (String woOrderState : woOrderStateList){
            if (OrderTrackOperType.WO_ORDER_STATE_10.equals(woOrderState)){
                ifSupplementOrder = true;
                break;
            }
        }
        return ifSupplementOrder;
    }

    @Override
    public Map<String, Object> qryDealUserTacheConfig(Map<String, Object> params) {
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        try{
            List<Map<String, Object>> dealUserTacheConfigList = orderDealDao.qryDealUserTacheConfig(params);
            String psId = MapUtils.getString(params, "psId");
            if (BasicCode.CROSS_NEWOPEN_FLOW.equals(psId)){
                Iterator<Map<String, Object>> it = dealUserTacheConfigList.iterator();
                while(it.hasNext()){
                    Map<String, Object> dealUserTacheConfig = it.next();
                    String tacheId = MapUtils.getString(dealUserTacheConfig, "TACHE_ID");
                    //如果是跨域全程调测，需要判断是不是主调，决定跨域全程调测框是不是展示
                    if (BasicCode.CROSS_WHOLE_COURDER_TEST.equals(tacheId)){
                        String orderId = MapUtils.getString(params, "orderId");
                        String sysResource = MapUtils.getString(params, "sysResource");//单子属于什么系统
                        Map<String, String> cstOrderDataMap = new HashMap<String, String>();
                        if (BasicCode.SECOND.equals(sysResource)){
                            cstOrderDataMap = orderDealDao.qryCstOrderDataFromSec(orderId);
                            String mainOrg = MapUtils.getString(cstOrderDataMap, "MAINORG"); // 一干指定的主调
                            String ifMainOrgSec = MapUtils.getString(cstOrderDataMap, "IFMAINORG");// 二干指定的主调
                            String resources = MapUtils.getString(cstOrderDataMap, "RESOURCES"); // 数据来源
                            String areaParentId = MapUtils.getString(cstOrderDataMap, "HANDLE_DEP_ID"); // 受理区域id
                            if (BasicCode.ONEDRY.equals(resources)) {
                                if (mainOrg.equals(areaParentId)) {
                                    if (!"0".equals(ifMainOrgSec)) {
                                        it.remove();
                                    }
                                }else {
                                    it.remove();
                                }
                            }else if (BasicCode.SECONDARY.equals(resources)
                                    ||BasicCode.JIKE.equals(resources)){
                                if (!"0".equals(ifMainOrgSec)) {
                                    it.remove();
                                }
                            }
                        }else if (BasicCode.LOCAL.equals(sysResource)) {
                            cstOrderDataMap = orderDealDao.qryCstOrderData(orderId);
                            String mainOrg = MapUtils.getString(cstOrderDataMap, "MAINORG"); // 一干指定的主调
                            String areaParentId = MapUtils.getString(cstOrderDataMap, "PARENT_ID"); // 受理区域的父id
                            if (!mainOrg.equals(areaParentId)) {
                                it.remove();
                            }
                        }
                    }
                }
            }
            paramsMap.put("success", true);
            paramsMap.put("data", dealUserTacheConfigList);
        }catch (Exception e){
            logger.error("查询电路调度环节需要指定处理人的环节失败！" + e);
            paramsMap.put("success", false);
        }
        return paramsMap;
    }

    /*
     * 工单抄送
     * @author ren.jiahang
     * @date 2019/6/1 17:19
     * @param params
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> ccWoOrder(Map<String, Object> params) {
        logger.info(">>>>>>>>>>>>>>>>>工单抄送>>>>>>>>>>>>>>>>>>>>>>>");
        //String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        String woId = MapUtils.getString(params, "woId");
        String tacheId = MapUtils.getString(params, "tacheId");
        //String objId = MapUtils.getString(params, "objId");
        //String objType = MapUtils.getString(params, "objType");
        Map<String, Object> logDataMap = new HashMap<String, Object>();
        logDataMap.put("woId",woId);
        logDataMap.put("orderId",MapUtils.getString(params, "orderId"));
        logDataMap.put("remark",MapUtils.getString(params, "remark"));
        logDataMap.put("tacheId", tacheId);
        logDataMap.put("operType", OrderTrackOperType.OPER_TYPE_15);
        logDataMap.put("action", "抄送");
        logDataMap.put("trackMessage", "[抄送]");
        logDataMap.put("staffId",MapUtils.getString(params, "objId"));
        logDataMap.put("opinion",MapUtils.getString(params, "opinion"));
        if(params.containsKey("operStaffInfoMap")){
            logDataMap.put("operStaffInfoMap", MapUtils.getMap(params, "operStaffInfoMap"));
        }
        Map<String, Object> resMap = new HashMap<String, Object>();
        DistributeLock lock = new DatabaseLock(woId);
        try {
            lock.lock();
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>加锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            orderStandbyService.addCC(params); //抄送
            resMap = tacheDealLogIntf.addTrackLog(logDataMap); //记录日志
            resMap.put("success", true);
            resMap.put("message", "抄送成功!");
        } catch (Exception e) {
            logger.error("抄送失败：", e);
            resMap.put("success", false);
            resMap.put("message", "抄送失败!" + e);
        }finally {
            lock.unlock();
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>finally主动解锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        }
        return resMap;
    }

    /**
     * 保存超时原因
     * 保存超时原因
     * @param params
     */
    @Override
    public void saveOpinionInfo(List<Map<String, Object>> params) {

        for (int i = 0; i < params.size(); i++) {
            Map<String, Object> map = params.get(i);
            Map<String, Object> logDataMap = new HashMap<String, Object>();
            logDataMap.put("woId", MapUtils.getString(map, "woId"));
            logDataMap.put("orderId", MapUtils.getString(map, "orderId"));
            logDataMap.put("remark", MapUtils.getString(map, "remark"));
            logDataMap.put("tacheId", MapUtils.getString(map, "tacheId"));
            logDataMap.put("operType", OrderTrackOperType.OPER_TYPE_1);
            logDataMap.put("operStaffInfoMap", MapUtils.getString(map, "operStaffInfoMap"));
            logDataMap.put("action", "超时");
            logDataMap.put("trackMessage", "[超时]");
            tacheDealLogIntf.addTrackLog(logDataMap);
        }
    }

    @Override
    public Map<String, Object> queryIfMainOrg(Map<String, Object> params) {
        return orderDealDao.queryIfMainOrg(params);
    }

    @Override
    public Map<String, Object> queryConstructConf(Map<String, Object> params) {
        return orderDealDao.queryConstructConf(params);
    }

    @Override
    public boolean qryIfPopConfigView(String orderId) {
        boolean flag = false;
        int ifNum = orderDealDao.qryIfPopConfig(orderId);
        if (ifNum > 0){
            flag = true;
        }
        return flag;
    }
    /**
     * @Description 功能描述: 并行核查单端退单
     * @Param: [srvOrderState],[srvOrdId]
     * @Return: int
     * @Author: wang.gang2
     * @Date: 2020/10/20 16:46
     */
    public int updateOrdInfoById(String srvOrderState,String srvOrdId) {
        return orderDealDao.updateOrdInfoById(srvOrderState,srvOrdId);
    }

    /**
     * @Description 功能描述: 查询单个电路信息属性
     * @Param: [params]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wang.gang2
     * @Date: 2020/10/21 16:16
     */
    public List<Map<String, Object>> queryAttrInfos(Map<String,Object> params){
        String attrCode = MapUtils.getString(params, "attrCode");
        String srvOrdId = MapUtils.getString(params, "srvOrdId");
        return  insertOrderInfoDao.queryAttrInfos(attrCode, srvOrdId);
    }

    /**
     * @Description 功能描述: 并行核查关联所有单子
     * @Param: [srvOrdId]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wang.gang2
     * @Date: 2020/10/23 19:26
     */
    public List<Map<String,Object>> queryParallelRelated(String srvOrdId){
        return orderDealDao.queryParallelRelated(srvOrdId);
    }

    /**
     * 查询自动化核查信息
     *
     * @param params
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> queryAutoCheckInfo(Map<String, Object> params) throws Exception {
        // 根据srvOrdId查询自动化核查结果
        Map<String,Object> retMap = new HashMap<>();
        Map<String,Object> cirResCheckMap = orderDealDao.qryAutoDetails(params);
        if("".equals(MapUtils.getString(cirResCheckMap,"CHK_CODE",""))){
            // 查不到核查结果，调用自动化核查接口 DualCirResCheckService
            retMap =dualCirResCheckServiceIntf.dualCirResCheck(params);
        } else{
            List<Map<String,Object>> autoList = orderDealDao.qryAutoChkList(params);
            cirResCheckMap.put("chkResList",autoList);
            retMap.put("data",cirResCheckMap);
            retMap.put("success",true);
        }
        return retMap;
    }

    /**
     * 激活结果查询
     * @author wangsen
     * @date 2020/10/15 17:12
     * @return
     */

    @Override
    public List<Map<String, Object>> getActivateInfo(Map<String, Object> params) {
        Map<String, Object> orderMap = orderDealDao.qryParentOrdIdByorderId(MapUtils.getString(params,"orderId"));
        String parentOrderId = MapUtils.getString(orderMap,"PARENT_ORDER_ID","");
        if (parentOrderId != null && !"".equals(parentOrderId)){
            params.put("orderId",parentOrderId);
        }
        List<Map<String, Object>> resList = orderDealDao.getActivateInfo(params);
        return resList;
    }

    @Override
    public Map<String, Object> sendOrder(Map<String, Object> map){
        map.put("type", "rescive"); //界面操作
        return provinceSendOrderServiceIntf.sendOrder(map);
    }

    @Override
    public Map<String, Object> postponementApply(Map<String, Object> params) {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> applyInfo = new HashMap<>();
        Map<String, Object> submitInfo = new HashMap<>();

        String applyState = "290000002";
        String cstOrdId = MapUtils.getString(params, "cstOrdId");
        String btnFlg = MapUtils.getString(params, "btnFlg");
        applyInfo.put("cstOrdId", cstOrdId);
        applyInfo.put("btnFlg", btnFlg);
        applyInfo.put("fileState", "0");
        applyInfo.put("origin", "YQ");
        applyInfo.put("reason", MapUtils.getString(params, "reason"));
        applyInfo.put("applyType", MapUtils.getString(params, "applyId"));
        applyInfo.put("newTime",MapUtils.getString(params, "postponementDate"));
        List<Map<String, Object>> fileData = (List<Map<String, Object>>) MapUtils.getObject(params, "fileData"); //附件信息
        List<Map<String, Object>> circuitData = (List<Map<String, Object>>) MapUtils.getObject(params, "circuitData");  //电路信息
        for (Map<String, Object> circuitInfo : circuitData) {
            //1.保存附件信息   插入记录待办添加延期申请审批  查询电路调度处理人 当前环节是电路调度得话无需审批直接调用一干接口
            //1.1 查询是否一干发二干然后发本地得要查询二干调度得处理人 否则是本地调度得处理人
            String srvOrdId = MapUtils.getString(circuitInfo, "SRV_ORD_ID");
            String woId = MapUtils.getString(circuitInfo, "WO_ID");
            Map<String, Object> belongSys = orderQrySecondaryDao.qrySrvOrderBelongSys(applyInfo);
            applyInfo.put("srvOrdId", srvOrdId);
//            applyInfo.put("woId", woId);
            applyInfo.put("fileData", fileData);
            applyInfo.put("oldTime", MapUtils.getString(circuitInfo,"RFSDATE"));
            //2.待办处理信息
            String tacheCode = BasicCode.SECOND.equals(MapUtils.getString(belongSys, "SYSTEM_RESOURCE")) ? "SECONDARY_SCHEDULE" : "CIRCUIT_DISPATCH";
            Map<String, Object> dealUserInfo = orderDealDao.querydealUserInfo(cstOrdId,tacheCode);
            String dealUser = MapUtils.getString(dealUserInfo, "COMP_USER_ID");
            applyInfo.put("dealUser", dealUser);
//            applyInfo.put("saveState", "0");
            //applyInfo.put("applyState", applyState); //申请状态：290000002 未审核 /290000004 同意 /290000006驳回 /290000020一干审核通过 /290000021一干审核不通过
            //3.保存 延期说明以及延期时间 附件信息 处理人信息
            List<Map<String, Object>> postponementApply = orderDealDao.queryPostponementApply(applyInfo);
            applyInfo.put("woId", woId);
//            map.put("CHG_VERSION", ++maxVersion);
            //1. 所有的都要先保存 a.未保存直接提交要报错 b.第一次保存直接插入 c.第二次保存 更新数据
            if (postponementApply.size() < 1 && "submit".equals(btnFlg)) {
                resultMap.put("message", "请先保存再提交");
                resultMap.put("success", false);
                return resultMap;
            }
            //a.第一次保存直接插入 或者一干已经审核过了
            if(("saveBtn".equals(btnFlg) && postponementApply.size() < 1)
                || ("saveBtn".equals(btnFlg) && !Arrays.asList(PENDING_STATE).contains(MapUtils.getString(postponementApply.get(0), "APPLY_STATE")))
               ) {
                applyInfo.put("saveState", "0");
                applyInfo.put("applyState", "290000000");
                orderDealDao.insertPostponementApply(applyInfo);
                //4.保存附件信息 可能要校验附件信息 先删除再新增
                for (Map<String, Object> fileInfo : fileData) {
                    fileInfo.put("srvOrdId", srvOrdId);
                    fileInfo.put("filePath", "createbuss");
                    fileInfo.put("woId", woId);
                    fileInfo.put("cstOrdId", cstOrdId);
                    fileInfo.put("origin", "YQ");
                    fileInfo.put("dispOrdId", MapUtils.getString(applyInfo,"ID"));// 延期申请单
                    orderStandbyDao.upLoadAttach(fileInfo);
                }
                resultMap.put("message", "保存成功");
                resultMap.put("success", true);
                //b.第二次保存 更新数据
            } else if ("saveBtn".equals(btnFlg) && postponementApply.size() > 0
                    && Arrays.asList(PENDING_STATE).contains(MapUtils.getString(postponementApply.get(0), "APPLY_STATE"))
                    && woId.equals(MapUtils.getString(postponementApply.get(0), "WO_ID"))
                ){
                //4.保存附件信息 可能要校验附件信息 先删除再新增 一干审核过的不能删除
                applyInfo.put("origin", "YQ");
                orderDealDao.deleteAttachFile(applyInfo);
                for (Map<String, Object> fileInfo : fileData) {
                    fileInfo.put("srvOrdId", srvOrdId);
                    fileInfo.put("filePath", "createbuss");
                    fileInfo.put("woId", woId);
                    fileInfo.put("cstOrdId",cstOrdId);
                    fileInfo.put("origin", "YQ");
                    fileInfo.put("dispatchOrderId", MapUtils.getString(postponementApply.get(0), "ID"));
                    orderStandbyDao.upLoadAttach(fileInfo);
                }
                //更新 延期申请信息
                applyInfo.put("applyState", "290000000");
                orderDealDao.updatePostponementApply(applyInfo);
                resultMap.put("message", "保存成功");
                resultMap.put("success", true);
                //提交后保存
            } else if (!StringUtils.isEmpty(dealUser) && "submit".equals(btnFlg) && "290000000".equals(MapUtils.getString(postponementApply.get(0), "APPLY_STATE"))) {
                submitInfo.put("cstOrdId", cstOrdId);
                submitInfo.put("srvOrdId", srvOrdId);
                submitInfo.put("woId", woId);
                submitInfo.put("updateState", "1");
                submitInfo.put("saveState", "0");
                submitInfo.put("applyState", "290000002");
                orderDealDao.updatePostponementApply(submitInfo);

                resultMap.put("message", "提交成功");
                resultMap.put("success", true);
            } else if (StringUtils.isEmpty(dealUser) && "submit".equals(btnFlg)) { //当前环无需审核直接调用一干接口
                //二干调度/电路调度没有处理人 无需审核直接调用一干延期接口  先保存在提交 还要根据环节回显
                //查询是否本地调度是否审核 已审核需要新插入数据 未审核直接更新
                Map<String, Object> applySaveInfo = queryApplySaveInfo(applyInfo);
                String postponementState = MapUtils.getString(applySaveInfo, "APPLY_STATE");
                if ("290000004".equals(postponementState)) {
                    resultMap.put("message", "提交失败，存在一干延期申请未审核反馈单子");
                    resultMap.put("success", false);
                    return resultMap;
                }
                Map<String, Object> map = feedbackInterface.postponementApply(applySaveInfo);

                if("success".equals(MapUtils.getString(map, "flag"))){
                    submitInfo.put("woId", woId);
                    submitInfo.put("cstOrdId", cstOrdId);
                    submitInfo.put("applyState", "290000004");
                    submitInfo.put("saveState", "1");
                    submitInfo.put("auditOpinion", MapUtils.getString(applySaveInfo,"REMARK"));
                    //更改附件状态、延期申请状态
                    orderDealDao.updatePostponementApply(submitInfo);

                    resultMap.put("message", "提交申请成功");
                    resultMap.put("success", true);
                }else{
                    resultMap.put("success", false);
                    resultMap.put("message", "失败!" + MapUtils.getString(map, "msg"));
                }
            }else{
                resultMap.put("message", "请检查是否有延期申请待审核");
                resultMap.put("success", false);
            }
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> queryApplySaveInfo(Map<String, Object> params) {
        Map<String, Object> postponementApplyInfo = new HashMap<>();
        List<Map<String, Object>> fileInfo = new ArrayList<>();
        //1. 查询当前环节保存的延期申请信息 未确认 未提交
        List<Map<String, Object>> postponementApply = orderDealDao.queryPostponementApply(params);
        if (postponementApply.size() > 0 && Arrays.asList(PENDING_STATE).contains(MapUtils.getString(postponementApply.get(0),"APPLY_STATE"))) {
            params.put("applyId", MapUtils.getString(postponementApply.get(0), "ID"));
            //2.拼接附件信息
            List<Map<String, Object>> attachList = orderDealDao.getAttachInfo(params);
            for (Map<String, Object> attachInfo : attachList) {
                Map attachs = new HashMap();
                for (Map.Entry<String, Object> attach : attachInfo.entrySet()) {
                    if ("FILE_ID".equals(attach.getKey())) {
                        attachs.put("fileId", attach.getValue());
                    }
                    if ("FILE_SIZE".equals(attach.getKey())) {
                        attachs.put("fileSize", attach.getValue());
                    }
                    if ("FILE_TYPE".equals(attach.getKey())) {
                        attachs.put("fileType", attach.getValue());
                    }
                    if ("FILE_NAME".equals(attach.getKey())) {
                        attachs.put("fileName", attach.getValue());
                    }
                    if ("FILE_PATH".equals(attach.getKey())) {
                        attachs.put("filePath", attach.getValue());
                    }
                }
                fileInfo.add(attachs);
            }
            postponementApplyInfo.put("fileData", fileInfo);

//            postponementApplyInfo.putAll(postponementApply.get(0));
            postponementApplyInfo.put("id", MapUtils.getString(postponementApply.get(0), "ID"));
            postponementApplyInfo.put("srvOrdId", MapUtils.getString(postponementApply.get(0), "SRV_ORD_ID"));
            postponementApplyInfo.put("woId", MapUtils.getString(postponementApply.get(0), "WO_ID"));
            postponementApplyInfo.put("oldTime", MapUtils.getString(postponementApply.get(0), "OLD_DATE"));
            postponementApplyInfo.put("newTime", MapUtils.getString(postponementApply.get(0), "POSTPONEMENT"));
            postponementApplyInfo.put("reason", MapUtils.getString(postponementApply.get(0), "REMARK"));
            postponementApplyInfo.put("applyType", MapUtils.getString(postponementApply.get(0), "APPLY_TYPE"));
            postponementApplyInfo.put("applyState", MapUtils.getString(postponementApply.get(0), "APPLY_STATE"));
        }
        return postponementApplyInfo;
    }

    @Override
    public List<Map<String, Object>> getAttachInfo(Map<String, Object> params) {
        return orderDealDao.getAttachInfo(params);
    }
    /**
     * @Description 功能描述: 调用一干接口
     * @Param: [params]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wang.gang2
     * @Date: 2020/11/10 15:44
     */
    @Override
    public Map<String, Object> feedBackToOneDry(Map<String, Object> params) {
        Map<String, Object> returnMap = new HashMap<>();
        Map<String, Object> applyInfo = new HashMap<>();
        Map<String, Object> result = new HashMap();
        result.put("flag", "success");
//        params.put("applyState","290000002");
        params.put("fileState", "0");
        String agreeOrNot = "0".equals(MapUtils.getString(params, "agreeOrNot")) ? "290000004" : "290000006";
        String cstOrdId = MapUtils.getString(params, "cstOrdId");
        String srvOrdId = MapUtils.getString(params, "srvOrdId");
        String woId = MapUtils.getString(params, "woId");
        params.put("origin", "YQ");
        Map<String, Object> postponementApply = queryApplySaveInfo(params);
        if("0".equals(MapUtils.getString(params, "agreeOrNot"))){
            //调用一干接口
            result = feedbackInterface.postponementApply(postponementApply);
        }

        if("success".equals(MapUtils.getString(result, "flag"))){
            Map<String, Object> submitInfo = new HashMap<>();
            submitInfo.put("woId", woId);
            submitInfo.put("cstOrdId", cstOrdId);
            submitInfo.put("applyState", agreeOrNot);
            submitInfo.put("auditOpinion", MapUtils.getString(params,"remark"));
            List<Map<String, Object>> fileData = (List<Map<String, Object>>) MapUtils.getObject(params, "fileData"); //附件信息
            //更改附件状态、延期申请状态
            orderDealDao.updatePostponementApply(submitInfo);
            for (Map<String, Object> fileInfo : fileData) {
                fileInfo.put("srvOrdId", srvOrdId);
                fileInfo.put("filePath", "createbuss");
                fileInfo.put("woId", woId);
                fileInfo.put("cstOrdId",cstOrdId);
                fileInfo.put("origin", "SH");
                fileInfo.put("dispatchOrderId", MapUtils.getString(postponementApply, "id"));
                orderStandbyDao.upLoadAttach(fileInfo);
            }
            returnMap.put("success", true);
            returnMap.put("message", "成功! " + MapUtils.getString(result, "msg"));
        }else{
            returnMap.put("success", false);
            returnMap.put("message", "失败!" + MapUtils.getString(result, "msg"));
        }
        return returnMap;
    }

    @Override
    public Map<String, Object> feedBackToOneDryBatch(Map<String, Object> params) {
        Map<String, Object> returnMap = new HashMap<>();
        Map<String, Object> applyInfo = new HashMap<>();
        Map<String, Object> result = new HashMap();
        List<Map<String, Object>> circuitData = (List<Map<String, Object>>) MapUtils.getObject(params, "circuitData");  //电路信息
        result.put("flag", "success");
        params.put("origin", "YQ");
        params.put("applyState","290000002");
        params.put("saveState","1");
        params.put("fileState","1");
        try {
            for (Map<String, Object> circuitInfo : circuitData) {
                String agreeOrNot = "0".equals(MapUtils.getString(params, "agreeOrNot")) ? "290000004" : "290000006";
                String cstOrdId = MapUtils.getString(circuitInfo, "CST_ORD_ID");
                String woId = MapUtils.getString(circuitInfo, "WO_ID");
                //首先修改状态，然后异步派发接口。如果接口派发失败，会把状态再次修改回来
                Map<String, Object> submitInfo = new HashMap<>();
                submitInfo.put("woId", woId);
                submitInfo.put("cstOrdId", cstOrdId);
                submitInfo.put("applyState", agreeOrNot);
                submitInfo.put("auditOpinion", MapUtils.getString(params,"remark"));
                params.put("woId", woId);
                params.put("cstOrdId", cstOrdId);
                if("0".equals(MapUtils.getString(params, "agreeOrNot"))){
                    Map<String, Object> postponementApply = queryApplySaveInfo(params);
                    UserInfo user = ThreadLocalInfoHolder.getLoginUser();
                    Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(user.getUserId()));
                    applyInfo.put("operStaffInfoMap", operStaffInfoMap);
                    applyInfo.put("srvOrdId", MapUtils.getString(postponementApply,"SRV_ORD_ID"));
                    applyInfo.put("oldTime", MapUtils.getString(postponementApply,"OLD_DATE"));
                    applyInfo.put("newTime", MapUtils.getString(postponementApply,"POSTPONEMENT"));
                    applyInfo.put("reason", MapUtils.getString(postponementApply,"REMARK"));
                    applyInfo.put("applyType", MapUtils.getString(postponementApply,"APPLY_TYPE"));
                    applyInfo.put("fileData", MapUtils.getObject(postponementApply,"fileInfo"));
                    applyInfo.put("woId", woId);
                    applyInfo.put("cstOrdId", cstOrdId);

                    //异步调用接口，派发一干
                    BatchWorkThreadPool batchWorkThreadPool = new BatchWorkThreadPool(applyInfo);
                    executorService.execute(batchWorkThreadPool);
                }
                //更改附件状态、延期申请状态
                orderDealDao.updatePostponementApply(submitInfo);
            }

            returnMap.put("success", true);
            returnMap.put("message", "批量审核提交成功！派发一干系统中！");
        } catch (NumberFormatException e) {
            logger.error("批量审核失败：", e);
            returnMap.put("success", false);
            returnMap.put("message", "批量审核失败!请联系管理员");
        }
        return returnMap;
    }
    private void insertStandardAddress(Map<String, Object> params) {
        String srvOrdId = MapUtils.getString(params, "srvOrdId");
        String attrCode = "10002217";
        String attrName = "标准地址";
        String attrValue = MapUtils.getString(params, "standardAddress","");
        if(attrValue != ""){
            srvOrdAttrServiceIntf.insertSrvOrdAttr(srvOrdId,attrCode,attrName,attrValue,"local");
        }
    }
}
