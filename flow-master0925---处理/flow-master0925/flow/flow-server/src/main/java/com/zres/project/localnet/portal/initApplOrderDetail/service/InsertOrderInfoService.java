package com.zres.project.localnet.portal.initApplOrderDetail.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.applpage.domain.PropertyDto;
import com.zres.project.localnet.portal.applpage.service.InsertOrderInfoIntf;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealService;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderSendMsgService;
import com.zres.project.localnet.portal.initApplOrderDetail.dao.DelOrderInfoDao;
import com.zres.project.localnet.portal.initApplOrderDetail.dao.EditDraftDao;
import com.zres.project.localnet.portal.initApplOrderDetail.dao.InsertOrderInfoDao;
import com.zres.project.localnet.portal.initApplOrderDetail.dao.UpdateOrderInfoDao;
import com.zres.project.localnet.portal.localStanbdyInfo.data.dao.OrderStandbyDao;
import com.zres.project.localnet.portal.localStanbdyInfo.service.OrderStandbyService;
import com.zres.project.localnet.portal.resourceInitiate.service.ResSupplementDealServiceIntf;
import com.zres.project.localnet.portal.util.OrderTrackOperType;
import com.ztesoft.res.frame.core.util.MapUtil;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author :ren.jiahang
 * @date:2018/12/22@time:14:59
 */
@Service
public class InsertOrderInfoService implements InsertOrderInfoIntf {
    Logger logger = LoggerFactory.getLogger(InsertOrderInfoService.class);
    @Autowired
    private InsertOrderInfoDao insertOrderInfoDao;

    @Autowired
    private PropertyCofgService propertyCofgService;

    @Autowired
    private GetEnumService getEnumService;

    @Autowired
    private OrderDealService orderDealService;

    @Autowired
    private OrderDealServiceIntf orderDealServiceIntf;

    @Autowired
    private UpdateOrderInfoService updateOrderInfoService;

    @Autowired
    private OrderStandbyDao orderStandbyDao;

    @Autowired
    private OrderStandbyService orderStandbyService;

    @Autowired
    private EditDraftDao editDraftDao;

    @Autowired
    private UpdateOrderInfoDao updateOrderInfoDao;

    @Autowired
    private DelOrderInfoDao delOrderInfoDao;

    @Autowired
    private OrderDealDao orderDealDao;

    @Autowired
    private OrderSendMsgService orderSendMsgService;
    @Autowired
    private ResSupplementDealServiceIntf resSupplementDealServiceIntf;

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

    /*
     * 插入客户信息
     * @author ren.jiahang
     * @date 2019/1/5 15:24
     * @param map
     * @return int
     */

    @Override
    public int insertCustomerInfo(Map<String, Object> map) {
        return insertOrderInfoDao.insertCustomerInfo(map);
    }

    /*
     * 插入定单信息
     * @author ren.jiahang
     * @date 2019/1/5 15:25
     * @param map
     * @return int
     */

    @Override
    public int insertOrderInfo(Map<String, Object> map) {
        return insertOrderInfoDao.insertOrderInfo(map);
    }

    /*
     * 插入电路信息
     * @author ren.jiahang
     * @date 2019/1/5 15:25
     * @param list
     * @return int
     */

    @Override
    public int insertordAttrInfo(List<Map<String, Object>> list) {
        return insertOrderInfoDao.insertordAttrInfo(list);
    }

    /*
     * 保存草稿
     * @author ren.jiahang
     * @date 2019/1/5 15:25
     * @param map
     * @return java.lang.String
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String orderInfoSave(Map<String, Object> map) {
        String result = "success";
        String appliTitle = "";
        StringBuffer errorInfo = new StringBuffer();
        JSONObject orderInfo = JSON.parseObject(map.get("OrderCustmInfo").toString()); //获取订单和用户信息

        Map orderInfomap = orderInfo;
        logger.info("*定单和客户信息-*" + orderInfomap);

        String service_id = orderInfomap.get("SERVICE_ID").toString(); //获取产品类型
        String orderType = MapUtil.getString(orderInfomap, "ORDER_TYPE"); //定单类型

        int seq_gom_bdw_cst_ord = querySequence("GOM_BDW_CST_ORD"); //获取客户表序列值
        JSONArray cirData1 = JSON.parseArray(map.get("cirData").toString()); //获取电路信息
        String operStaffId = "";
        if (ThreadLocalInfoHolder.getLoginUser() == null) {
            operStaffId = "11";
        }
        else {
            //获取用户id
            operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        }
        Date currdate = new Date();

        orderInfomap.put("CST_ORD_ID", seq_gom_bdw_cst_ord); //客户序列
        orderInfomap.put("USER_ID", operStaffId); //发起人
        orderInfomap.put("SRV_ORD_STAT", "10C");
        try {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currDateStr = sdf.format(currdate);
            orderInfomap.put("CREATE_DATE", currDateStr); //发起时间
            orderInfomap.put("ORDER_ID", 0); //gom_order.id保存草稿默认0 未起流程
            appliTitle = getAppliTitle();
            orderInfomap.put("APPLY_ORD_ID", appliTitle);
            insertCustomerInfo(orderInfomap); //插入客户信息

            //有几条电路就插入几条订单信息
            for (int i = 0; i < cirData1.size(); i++) {
                //因为mybatis缓存的问题，从这里获取定单序列会导致这里每次获取都是同一个序列值，改为xml插入方法中获取序列，这个序列因为插入里边用到一次，会清空缓存，此处序列就没有问题了，会和xml中序列同步。可以正常给其他业务使用
                int seq_gom_bdw_srv_ord_info = querySequence("GOM_BDW_SRV_ORD_INFO"); //获取订单表序列值
                attachSave(map, seq_gom_bdw_srv_ord_info); //保存附件信息
                circuitAttachSave(map, seq_gom_bdw_srv_ord_info); //保存电路信息附件
                orderInfomap.put("SRV_ORD_ID", seq_gom_bdw_srv_ord_info); //定单序列
                JSONObject cirData1Json = cirData1.getJSONObject(i);
                Map<String, Object> cirDatalMap = cirData1Json;
                String tradeId = MapUtil.getString(cirDatalMap, "tradeId"); //业务订单号
                String serialNumber = MapUtil.getString(cirDatalMap, "serialNumber"); //业务号码
                String instance_id = MapUtil.getString(cirDatalMap, "INSTANCE_ID"); //实例id
                if("".equals(instance_id)||instance_id==null){
                    orderInfomap.put("INSTANCE_ID",seq_gom_bdw_srv_ord_info); //实例id
                    cirDatalMap.put("INSTANCE_ID",seq_gom_bdw_srv_ord_info);
                }
                else{
                    orderInfomap.put("INSTANCE_ID",instance_id); //实例id
                    cirDatalMap.put("INSTANCE_ID",instance_id);
                }
                if ("20181221006".equals(service_id)) {
                    if ("101".equals(orderType)) {
                        orderInfomap.put("TRADE_ID", seq_gom_bdw_srv_ord_info);
                        orderInfomap.put("SERIAL_NUMBER", String.valueOf(seq_gom_bdw_srv_ord_info));
                    }
                    else {
                        orderInfomap.put("TRADE_ID", seq_gom_bdw_srv_ord_info);
                        orderInfomap.put("SERIAL_NUMBER", serialNumber);
                    }
                    orderInfomap.put("SERIAL_NUMBER", MapUtils.getString(orderInfomap,"INSTANCE_ID"));
                }
                else {
                    if ("101".equals(orderType)) {
                        orderInfomap.put("TRADE_ID", tradeId);
                        orderInfomap.put("SERIAL_NUMBER", serialNumber);
                    }
                    //核查单没有业务号码，给默认值
                    else{
                        orderInfomap.put("TRADE_ID", tradeId);
                        orderInfomap.put("SERIAL_NUMBER", String.valueOf(seq_gom_bdw_srv_ord_info));
                    }
                }
                /**
                 * modify by ren.jiahang  at 20200801 for 待办查询优化，要求完成时间改从电路纵表存储到横表中（GOM_BDW_SRV_ORD_INFO.req_fin_time）
                 */
                String requFineTime = MapUtil.getString(cirDatalMap, "requFineTime"); //全程要求完成时间
                orderInfomap.put("REQ_FIN_TIME",requFineTime);
                logger.info("*客户信息id&定单信息id-* [" + seq_gom_bdw_cst_ord + " , " + seq_gom_bdw_srv_ord_info + "]");
                insertOrderInfo(orderInfomap); //插入定单信息
                packageCircuitInfo(cirDatalMap, service_id, seq_gom_bdw_srv_ord_info, currDateStr); //包装并插入电路信息
            }
        }
        catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus()
                    .setRollbackOnly();
            logger.error("****保存草稿异常！****", e);
            errorInfo.append(e);
            result = "error";

        }
        logger.info("*电路保存-*" + result);
        return result + "," + appliTitle + "," + errorInfo;
    }

    /*
     * 业务发起提交
     * @author ren.jiahang
     * @date 2019/1/5 15:25
     * @param map
     * @return java.lang.String
     */
    @Override
    public String orderInfoSubmit(Map<String, Object> map) {
        JSONObject orderInfo = JSON.parseObject(map.get("OrderCustmInfo").toString()); //获取订单和用户信息
        String result = "success";
        StringBuffer errorInfo = new StringBuffer();
        try {
            //如果申请单标题为空则为直接提交调度，否则为草稿箱提交
            if (orderInfo.containsKey("APPLY_ORD_ID") && MapUtil.getString(orderInfo, "APPLY_ORD_ID") != null) {
                result = orderInfoSubmInDraft(map); //草稿
            }
            else {
                result = orderInfoSubmDirect(map);
            }
        }
        catch (Exception e) {
            result = "error";
            errorInfo.append(",").append(e);
            logger.error("                *  ");
            logger.error("            *********  ");
            logger.error("      *********190207**********");
            logger.error("********* 提交调度失败 ***********", e);
            logger.error("      **********************");
            logger.error("            *********  ");
            logger.error("                *  ");
        }
        return result + "," + errorInfo;
    }

    /*
     * 在草稿单中提交
     * @author ren.jiahang
     * @date 2019/1/5 15:28
     * @param map
     * @return java.lang.String
     */
    @Transactional(rollbackFor = Exception.class)
    public String orderInfoSubmInDraft(Map<String, Object> map) throws Exception {
        String result = "success";
        List<String> orderIdList = new ArrayList(); //发送短信的orderidlist
        try {
            String userId;
            int sendCount=0;
            if (ThreadLocalInfoHolder.getLoginUser() == null) {
                userId = "11";
            }
            else {
                //获取用户id
                userId = ThreadLocalInfoHolder.getLoginUser().getUserId();
            }
            Date currdate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currDateStr = sdf.format(currdate);
            String ORDER_TITLE = "";
            String ORDER_CONTENT = "";
            JSONObject orderInfo = JSON.parseObject(map.get("OrderCustmInfo").toString()); //获取订单和用户信息
            Map orderInfomap = orderInfo;
            logger.info("*定单和客户信息-*" + orderInfomap);
            String service_id = MapUtil.getString(orderInfo, "SERVICE_ID"); //获取产品类型
            String circuitReq = MapUtil.getString(orderInfo, "ACTIVE_TYPE"); //电路类型
            String orderType = MapUtil.getString(orderInfo, "ORDER_TYPE"); //定单类型
            String autitId = MapUtil.getString(orderInfo,"AUTIT_ID"); //审核人类型和id（260000003_J!G@F_489964）
            String CustID = MapUtil.getString(map, "CUST_ID"); //客户订单id
            String IS_GROUP_CUST = MapUtil.getString(orderInfo, "IS_GROUP_CUST"); //是否集团直管客户
            if (!orderInfo.containsKey("HANDLE_DEP_ID")) {
                throw new NullPointerException("受理区域[HANDLE_DEP_ID] is null");
            }
            String handleDepId = MapUtil.getString(orderInfo, "HANDLE_DEP_ID"); //受理区域
            //将 CustID添加到 数据中
            orderInfomap.put("CST_ORD_ID", CustID);
            orderInfomap.put("IS_GROUP_CUST", IS_GROUP_CUST);
            orderInfomap.put("USER_ID", userId);
            logger.info("*定单和客户信息-*" + orderInfomap);
            //更新客户表
            updateOrderInfoDao.UpdateCustomerInfo(orderInfomap);
            //根据客户ID 查询业务订单ID

            List<String> srvOrdIdList = editDraftDao.querySrvOrdIdByCustId(CustID, "10C");
            // TODO: 2019/3/5 获取流程参数
            String procesParam = getProcesParam(service_id, orderType, circuitReq);
            JSONArray cirData1 = JSON.parseArray(map.get("cirData").toString()); //获取电路信息
            List<String> srvIdList = new ArrayList<String>();
            for (int i = 0; i < cirData1.size(); i++) {
                JSONObject cirData1Json = cirData1.getJSONObject(i);
                Map<String, Object> cirDatalMap = cirData1Json;
                String tradeId = MapUtil.getString(cirDatalMap, "tradeId"); //业务订单号
                String serialNumber = MapUtil.getString(cirDatalMap, "serialNumber"); //业务号码
                String requFineTime = MapUtil.getString(cirDatalMap, "requFineTime"); //全程要求完成时间
                String instance_id = MapUtil.getString(cirDatalMap, "INSTANCE_ID"); //实例id

                //处理草稿单新增的电路
                if (MapUtil.getString(cirDatalMap, "SRV_ORD_ID") == null) {
                    //获取订单表序列值
                    int seq_gom_bdw_srv_ord_info = querySequence("GOM_BDW_SRV_ORD_INFO");
                    if ("20181221006".equals(service_id)) {
                        if ("101".equals(orderType)) {
                            ORDER_TITLE = String.valueOf(seq_gom_bdw_srv_ord_info);
                            ORDER_CONTENT = String.valueOf(seq_gom_bdw_srv_ord_info);
                            orderInfomap.put("TRADE_ID", seq_gom_bdw_srv_ord_info);
                            orderInfomap.put("SERIAL_NUMBER", String.valueOf(seq_gom_bdw_srv_ord_info));
                        }
                        else {
                            ORDER_TITLE = String.valueOf(seq_gom_bdw_srv_ord_info);
                            ORDER_CONTENT = String.valueOf(seq_gom_bdw_srv_ord_info);
                            orderInfomap.put("TRADE_ID", seq_gom_bdw_srv_ord_info);
                            orderInfomap.put("SERIAL_NUMBER", serialNumber);
                        }
                    }
                    else {
                        if ("101".equals(orderType)) {
                            ORDER_TITLE = tradeId;
                            ORDER_CONTENT = serialNumber;
                            orderInfomap.put("TRADE_ID", tradeId);
                            orderInfomap.put("SERIAL_NUMBER", serialNumber);
                        }
                        //核查单没有业务号码，给默认值
                        else{
                            ORDER_TITLE = tradeId;
                            ORDER_CONTENT = String.valueOf(seq_gom_bdw_srv_ord_info);
                            orderInfomap.put("TRADE_ID", tradeId);
                            orderInfomap.put("SERIAL_NUMBER", String.valueOf(seq_gom_bdw_srv_ord_info));
                        }
                    }
                    Map<String, Object> creaOrderMap = new HashMap<String, Object>();
                    creaOrderMap.put("ORDER_CONTENT", ORDER_CONTENT);
                    creaOrderMap.put("AREA", "350002000000000042766408"); //目前配置的是福建
                    creaOrderMap.put("ORDER_TITLE", ORDER_TITLE);
                    creaOrderMap.put("ordPsid", procesParam);
                    Map<String, Object> acceptAreaMap = new HashMap<String, Object>();
                    acceptAreaMap.put("REGION_ID", handleDepId);
                    acceptAreaMap.put("PRODUCT_TYPE",service_id); //add 20190529 起流程传产品编码
                    acceptAreaMap.put("ACT_TYPE",circuitReq+procesParam);
                    creaOrderMap.put("DEMAND_DEPART_AUDIT_DISP_OBJ",autitId); //审核人
                    creaOrderMap.put("attr", acceptAreaMap);
                    creaOrderMap.put("requFineTime", requFineTime);
                    //起流程获取流程id
                    Map retMap = orderDealService.createOrder(creaOrderMap);
                    String orderId = MapUtil.getString(retMap, "orderId");
                    insertTrackLogInfo(orderId); //发起申请日志

                    //客户序列
                    orderInfomap.put("CST_ORD_ID", CustID);
                    orderInfomap.put("SRV_ORD_STAT", "10N");
                    orderInfomap.put("CREATE_DATE", currDateStr);
                    //gom_order.id保存草稿默认0 未起流程
                    orderInfomap.put("ORDER_ID", orderId);
                    orderInfomap.put("SRV_ORD_ID", seq_gom_bdw_srv_ord_info);
                    if("".equals(instance_id)||instance_id==null){
                        orderInfomap.put("INSTANCE_ID",seq_gom_bdw_srv_ord_info); //实例id
                        cirDatalMap.put("INSTANCE_ID",seq_gom_bdw_srv_ord_info);
                    }
                    else{
                        orderInfomap.put("INSTANCE_ID",instance_id); //实例id
                        cirDatalMap.put("INSTANCE_ID",instance_id);
                    }
                    if ("20181221006".equals(service_id)) {
                        orderInfomap.put("SERIAL_NUMBER", MapUtils.getString(orderInfomap,"INSTANCE_ID"));
                    }
                    // 资源补录单子挂起
                    Map<String, Object> suspendParam = new HashMap<>();
                    suspendParam.put("instanceId",MapUtils.getString(orderInfomap,"INSTANCE_ID"));
                    suspendParam.put("activeType",MapUtils.getString(orderInfomap,"ACTIVE_TYPE"));
                    /* ResSuspendThread resSuspendThread = new ResSuspendThread(suspendParam);
                    resSuspendThread.start();*/
                    resSupplementDealServiceIntf.supplementStop(suspendParam);
                    attachSave(map, seq_gom_bdw_srv_ord_info); //插入附件信息
                    circuitAttachSave(map, seq_gom_bdw_srv_ord_info); //保存电路信息附件
                    /**
                     * modify by ren.jiahang  at 20200801 for 待办查询优化，要求完成时间改从电路纵表存储到横表中（GOM_BDW_SRV_ORD_INFO.req_fin_time）
                     */
                    orderInfomap.put("REQ_FIN_TIME",requFineTime);
                    //插入定单信息
                    insertOrderInfo(orderInfomap);

                    //TODO: 2019/3/5 匹配配置表
                    packageCircuitInfo(cirDatalMap, service_id, seq_gom_bdw_srv_ord_info, currDateStr); //包装并插入电路信息
                }
                else {
                    srvIdList.add(cirDatalMap.get("SRV_ORD_ID").toString());
                }
            }

            List<String> NeedDeleteSrvOrdIDList = updateOrderInfoService.takeDifferentElements(srvIdList, srvOrdIdList);
            //如何俩个业务电路的数量不同 则删除不同的业务电路
            if (!NeedDeleteSrvOrdIDList.isEmpty()) {
                delOrderInfoDao.delOrderInfoBySrvId(NeedDeleteSrvOrdIDList);
            }
            //删除对应的已存在电路信息 ，保留ServOrdId 重新插入电路信息
            delOrderInfoDao.delOrdAttrInfoBySrvId(srvOrdIdList);

            /******************************修改*******************************************/
            //插入新的电路属性
            for (int i = 0; i < cirData1.size(); i++) {
                JSONObject cirData1Json = cirData1.getJSONObject(i);
                Map<String, Object> cirDatalMap = cirData1Json;
                Integer servid = MapUtil.getInteger(cirDatalMap, "SRV_ORD_ID");
                //Servid==null 表示此条电路信息为编辑草稿单新增电路信息 ，已经添加到数据库中，所以跳出循环
                if (servid == null || servid == 0) {
                    continue;
                }
                else {
                    String tradeId = MapUtil.getString(cirDatalMap, "tradeId"); //业务订单号
                    String serialNumber = MapUtil.getString(cirDatalMap, "serialNumber"); //业务号码
                    String requFineTime = MapUtil.getString(cirDatalMap, "requFineTime"); //全程要求完成时间
                    String instance_id = MapUtil.getString(cirDatalMap, "INSTANCE_ID"); //全程要求完成时间
                    //String SPECIALTY_CODE = "LOCAL"; //区分是客户还是局内电路

                    if("".equals(instance_id)||instance_id==null){
                        orderInfomap.put("INSTANCE_ID",servid); //实例id
                        cirDatalMap.put("INSTANCE_ID",servid);
                    }
                    else{
                        orderInfomap.put("INSTANCE_ID",instance_id); //实例id
                        cirDatalMap.put("INSTANCE_ID",instance_id);
                    }
                    if ("20181221006".equals(service_id)) {
                        //SPECIALTY_CODE = "INSIDE";
                        if ("101".equals(orderType)) {
                            ORDER_TITLE = String.valueOf(servid);
                            ORDER_CONTENT = String.valueOf(servid);
                            orderInfomap.put("TRADE_ID", servid);
                            orderInfomap.put("SERIAL_NUMBER", String.valueOf(servid));
                        }
                        else {
                            ORDER_TITLE = String.valueOf(servid);
                            ORDER_CONTENT = String.valueOf(servid);
                            orderInfomap.put("TRADE_ID", servid);
                            orderInfomap.put("SERIAL_NUMBER", serialNumber);
                        }
                        orderInfomap.put("SERIAL_NUMBER", MapUtils.getString(orderInfomap,"INSTANCE_ID"));
                    }
                    else {
                        if ("101".equals(orderType)) {
                            ORDER_TITLE = tradeId;
                            ORDER_CONTENT = serialNumber;
                            orderInfomap.put("TRADE_ID", tradeId);
                            orderInfomap.put("SERIAL_NUMBER", serialNumber);
                        }
                        //核查单没有业务号码，给默认值
                        else{
                            ORDER_TITLE = tradeId;
                            ORDER_CONTENT = String.valueOf(servid);
                            orderInfomap.put("TRADE_ID", tradeId);
                            orderInfomap.put("SERIAL_NUMBER", String.valueOf(servid));
                        }
                    }

                    Map<String, Object> creaOrderMap = new HashMap<String, Object>();
                    creaOrderMap.put("ORDER_CONTENT", ORDER_CONTENT);
                    creaOrderMap.put("AREA", "350002000000000042766408"); //目前配置的是福建
                    creaOrderMap.put("ORDER_TITLE", ORDER_TITLE);
                    creaOrderMap.put("ordPsid", procesParam);
                    Map<String, Object> acceptAreaMap = new HashMap<String, Object>();
                    acceptAreaMap.put("REGION_ID", handleDepId);
                    acceptAreaMap.put("PRODUCT_TYPE",service_id); //add 20190529 起流程传产品编码
                    acceptAreaMap.put("ACT_TYPE",circuitReq+procesParam);
                    creaOrderMap.put("DEMAND_DEPART_AUDIT_DISP_OBJ",autitId); //审核人
                    creaOrderMap.put("attr", acceptAreaMap);
                    creaOrderMap.put("requFineTime", requFineTime);
                    //判断是否已经起流程，如果有流程id则说明是退单重提单，此时不再重新起流程。modify 20190322 by ren.jiahang
              /*  String queryOrderId = insertOrderInfoDao.queryOrderId(servid.toString());
                String orderId;
                if(queryOrderId != null) {
                    orderId = queryOrderId;
                }
                else{
                    Map retMap = orderDealService.createOrder(creaOrderMap);
                     orderId = MapUtil.getString(retMap,"orderId");
                }*/
                    Map retMap = orderDealService.createOrder(creaOrderMap);

                    String orderId = MapUtil.getString(retMap,"orderId");
                    insertTrackLogInfo(orderId); //发起申请日志
                    orderInfomap.put("ORDER_ID", orderId);
                    orderInfomap.put("SRV_ORD_ID", servid);
                   // orderInfomap.put("INSTANCE_ID",servid); //实例id
                    if("".equals(instance_id)||instance_id==null){
                        orderInfomap.put("INSTANCE_ID",servid); //实例id
                        cirDatalMap.put("INSTANCE_ID",servid);
                    }
                    else{
                        orderInfomap.put("INSTANCE_ID",instance_id); //实例id
                        cirDatalMap.put("INSTANCE_ID",instance_id);
                    }
                    // 资源补录单子挂起
                    Map<String, Object> suspendParam = new HashMap<>();
                    suspendParam.put("instanceId",MapUtils.getString(orderInfomap,"INSTANCE_ID"));
                    suspendParam.put("activeType",MapUtils.getString(orderInfomap,"ACTIVE_TYPE"));
                    /* ResSuspendThread resSuspendThread = new ResSuspendThread(suspendParam);
                    resSuspendThread.start();*/
                    resSupplementDealServiceIntf.supplementStop(suspendParam);
                    orderInfomap.put("SRV_ORD_STAT", "10N");
                    /**
                     * modify by ren.jiahang  at 20200801 for 待办查询优化，要求完成时间改从电路纵表存储到横表中（GOM_BDW_SRV_ORD_INFO.req_fin_time）
                     */
                    orderInfomap.put("REQ_FIN_TIME",requFineTime);
                    updateOrderInfoDao.UpdateSrvOrderInfo(orderInfomap); //修改定单业务信息
                    attachSave(map, servid); //保存附件信息
                    circuitAttachSave(map, servid); //保存电路信息附件
                    packageCircuitInfo(cirDatalMap, service_id, servid, currDateStr); //包装并插入电路信息
                    /*
                    if(sendCount==0){
                        sendMassage(orderId,userId);
                        sendCount++;
                    }*/
                    orderIdList.add(orderId);

                }
            }
            //发送短信通知
            logger.info("开始准备发送短信。。。。。。。。。。。。。。。。。。。。");
            Map<String, Object> sendMsgMap = new HashMap<String, Object>();
            sendMsgMap.put("operStaffId", userId);
            sendMsgMap.put("orderIdList", orderIdList);
            sendMsgMap.put("operAction", "发起");
            orderSendMsgService.sendMsgBefore(sendMsgMap);
        }catch (Exception e){
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus()
                    .setRollbackOnly();
            throw new Exception(e.getMessage());
        }
        return result;
    }

    /*
     * 发起页面直接提交调度
     * @author ren.jiahang
     * @date 2019/1/5 17:57
     * @param cstId
     * @param map
     * @return java.lang.String
     */
    @Transactional(rollbackFor = Exception.class)
    public String orderInfoSubmDirect(Map<String, Object> map) throws Exception {
        String result = "success";
        String appliTitle = "";
        int sendCount=0;
        List<String> orderIdList = new ArrayList(); //发送短信的orderidlist
        try{
            String ORDER_TITLE = "";
            String ORDER_CONTENT = "";
            JSONObject orderInfo = JSON.parseObject(map.get("OrderCustmInfo").toString()); //获取订单和用户信息
            Map orderInfomap = orderInfo;
            logger.info("*定单和客户信息-*" + orderInfomap);
            String service_id = MapUtil.getString(orderInfo, "SERVICE_ID"); //获取产品类型
            String circuitReq = MapUtil.getString(orderInfo, "ACTIVE_TYPE"); //电路类型
            String orderType = MapUtil.getString(orderInfo, "ORDER_TYPE"); //定单类型
            String handleDepId = MapUtil.getString(orderInfo, "HANDLE_DEP_ID"); //受理区域
            String IS_GROUP_CUST = MapUtil.getString(orderInfo, "IS_GROUP_CUST"); //是否集团直管客户
            String autitId = MapUtil.getString(orderInfo,"AUTIT_ID"); //审核人类型和id（260000003_J!G@F_489964）
            if (!orderInfo.containsKey("HANDLE_DEP_ID")) {
                throw new NullPointerException("受理区域[HANDLE_DEP_ID] is null");
            }
            Map<String, Object> queryProcessMap = new HashMap<String, Object>();
            if ("20181221006".equals(service_id)) {
                queryProcessMap.put("codeType", "flow_cust"); //所有局内流程 写死为内部流程
            }
            else {
                queryProcessMap.put("codeType", "flow_local"); //流程类型 写死为内部流程
            }
            queryProcessMap.put("codeContent", orderType); //订单类型开通单
            queryProcessMap.put("codeTypeName", service_id); //产品编码
            queryProcessMap.put("codeValue", circuitReq); //操作类型
            if ("102".equals(orderType)) { //单据类型102代表核查
                queryProcessMap.put("codeType", "flow_check"); //流程类型 写死为核查流程
                //service_id = "20190220001"; //代表核查单所有产品，用来查询页面展示属
            }

            //查询流程参数
            List<Map<String, Object>> services = getEnumService.queryProcessInst(queryProcessMap);
            String procesParam = "";
            if (services != null && services.size() == 1) {
                procesParam = services.get(0).get("SORT_NO").toString();
            }
            else {
                return "error,," + "查询流程参数为空或查询到多个流程参数，查询到流程数量：[" + services.size() + "]";
            }

            int seq_gom_bdw_cst_ord = querySequence("GOM_BDW_CST_ORD"); //获取客户表序列值
            String operStaffId = "";
            if (ThreadLocalInfoHolder.getLoginUser() == null) {
                operStaffId = "11";
            }
            else {
                //获取用户id
                operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
            }
            Date currdate = new Date();
            orderInfomap.put("CST_ORD_ID", seq_gom_bdw_cst_ord); //客户序列
            orderInfomap.put("USER_ID", operStaffId); //发起人
            orderInfomap.put("IS_GROUP_CUST", IS_GROUP_CUST);
            orderInfomap.put("SRV_ORD_STAT", "10N");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currDateStr = sdf.format(currdate);
            orderInfomap.put("CREATE_DATE", currDateStr); //发起时间
            appliTitle = getAppliTitle();
            orderInfomap.put("APPLY_ORD_ID", appliTitle);
            insertCustomerInfo(orderInfomap); //插入客户信息
            JSONArray cirData1 = JSON.parseArray(map.get("cirData").toString()); //获取电路信息
            for (int i = 0; i < cirData1.size(); i++) {
                //有几条电路就插入几条订单信息
                JSONObject cirData1Json = cirData1.getJSONObject(i);
                Map<String, Object> cirDatalMap = cirData1Json;
                int circuitCount = 1;
                if ("20181221006".equals(service_id)) {
                    circuitCount = Integer.parseInt(cirDatalMap.get("count").toString());
                }
                //电路数量为N，插入N条业务信息和电路信息
                for (int z = 0; z < circuitCount; z++) {
                    int seq_gom_bdw_srv_ord_info = querySequence("GOM_BDW_SRV_ORD_INFO"); //获取订单表序列值
                    attachSave(map, seq_gom_bdw_srv_ord_info); //保存附件信息
                    circuitAttachSave(map, seq_gom_bdw_srv_ord_info); //保存电路信息附件
                    orderInfomap.put("SRV_ORD_ID", seq_gom_bdw_srv_ord_info); //定单序列
                    String instance_id = MapUtil.getString(cirDatalMap, "INSTANCE_ID"); //实例id
                    if("".equals(instance_id)||instance_id==null){
                        orderInfomap.put("INSTANCE_ID",seq_gom_bdw_srv_ord_info);
                        cirDatalMap.put("INSTANCE_ID",seq_gom_bdw_srv_ord_info);
                    }
                    else{
                        orderInfomap.put("INSTANCE_ID",instance_id); //实例id
                        cirDatalMap.put("INSTANCE_ID",instance_id);
                    }
                    logger.info("*客户信息id&定单信息id-* [" + seq_gom_bdw_cst_ord + " , " + seq_gom_bdw_srv_ord_info + "]");
                    cirDatalMap.remove("_id_"); //清除多余字段
                    cirDatalMap.remove("upDDKLoadResult"); //清除多余字段
                    cirDatalMap.remove("DDK"); //清除多余字段
                    cirDatalMap.remove("fileList"); //清除多余字段
                    String tradeId = MapUtil.getString(cirDatalMap, "tradeId"); //业务订单号
                    String serialNumber = MapUtil.getString(cirDatalMap, "serialNumber"); //业务号码
                    String requFineTime = MapUtil.getString(cirDatalMap, "requFineTime"); //全程要求完成时间
                    //String SPECIALTY_CODE = "LOCAL"; //区分是客户还是局内电路
                    if ("20181221006".equals(service_id)) {
                        //SPECIALTY_CODE = "INSIDE";
                        if ("101".equals(orderType)) {
                            ORDER_TITLE = String.valueOf(seq_gom_bdw_srv_ord_info);
                            ORDER_CONTENT = String.valueOf(seq_gom_bdw_srv_ord_info);
                            orderInfomap.put("TRADE_ID", seq_gom_bdw_srv_ord_info);
                            orderInfomap.put("SERIAL_NUMBER", String.valueOf(seq_gom_bdw_srv_ord_info));
                        }
                        else {
                            ORDER_TITLE = String.valueOf(seq_gom_bdw_srv_ord_info);
                            ORDER_CONTENT = String.valueOf(seq_gom_bdw_srv_ord_info);
                            orderInfomap.put("TRADE_ID", seq_gom_bdw_srv_ord_info);
                            orderInfomap.put("SERIAL_NUMBER", serialNumber);
                        }
                        orderInfomap.put("SERIAL_NUMBER", MapUtils.getString(orderInfomap,"INSTANCE_ID"));
                    }
                    else {
                        if ("101".equals(orderType)) {
                            ORDER_TITLE = tradeId;
                            ORDER_CONTENT = serialNumber;
                            orderInfomap.put("TRADE_ID", tradeId);
                            orderInfomap.put("SERIAL_NUMBER", serialNumber);
                        }
                        //核查单没有业务号码，给默认值
                        else{
                            ORDER_TITLE = tradeId;
                            ORDER_CONTENT = String.valueOf(seq_gom_bdw_srv_ord_info);
                            orderInfomap.put("TRADE_ID", tradeId);
                            orderInfomap.put("SERIAL_NUMBER", String.valueOf(seq_gom_bdw_srv_ord_info));
                        }
                    }
                    Map<String, Object> creaOrderMap = new HashMap<String, Object>();
                    creaOrderMap.put("ORDER_CONTENT", ORDER_CONTENT);
                    creaOrderMap.put("AREA", "350002000000000042766408"); //全国
                    creaOrderMap.put("ORDER_TITLE", ORDER_TITLE);
                    creaOrderMap.put("ordPsid", procesParam);
                    //受理区域
                    Map<String, Object> acceptAreaMap = new HashMap<String, Object>();
                    acceptAreaMap.put("REGION_ID", handleDepId);
                    acceptAreaMap.put("PRODUCT_TYPE",service_id); //add 20190529 起流程传产品编码
                    acceptAreaMap.put("ACT_TYPE",circuitReq+procesParam);
                    creaOrderMap.put("attr", acceptAreaMap);
                    creaOrderMap.put("DEMAND_DEPART_AUDIT_DISP_OBJ",autitId); //审核人
                    creaOrderMap.put("requFineTime", requFineTime);
                    Map retMap = orderDealService.createOrder(creaOrderMap);
                    // 资源补录单子挂起
                    Map<String, Object> suspendParam = new HashMap<>();
                    suspendParam.put("instanceId",MapUtils.getString(orderInfomap,"INSTANCE_ID"));
                    suspendParam.put("activeType",MapUtils.getString(orderInfomap,"ACTIVE_TYPE"));
                    /* ResSuspendThread resSuspendThread = new ResSuspendThread(suspendParam);
                    resSuspendThread.start();*/
                    resSupplementDealServiceIntf.supplementStop(suspendParam);

                    String orderId = (String) retMap.get("orderId");
                    insertTrackLogInfo(orderId); //发起申请日志
                    orderInfomap.put("ORDER_ID", orderId);
                    /**
                     * modify by ren.jiahang  at 20200801 for 待办查询优化，要求完成时间改从电路纵表存储到横表中（GOM_BDW_SRV_ORD_INFO.req_fin_time）
                     */
                    orderInfomap.put("REQ_FIN_TIME",requFineTime);
                    insertOrderInfo(orderInfomap); //插入定单信息
                    packageCircuitInfo(cirDatalMap, service_id, seq_gom_bdw_srv_ord_info, currDateStr); //包装并插入电路信息
                    /*//发送短信通知
                    if(sendCount==0){
                        sendMassage(orderId,operStaffId);
                        sendCount++;
                    }*/
                    orderIdList.add(orderId);

                }
            }
            //发送短信通知
            logger.info("开始准备发送短信。。。。。。。。。。。。。。。。。。。。");
            Map<String, Object> sendMsgMap = new HashMap<String, Object>();
            sendMsgMap.put("operStaffId", operStaffId);
            sendMsgMap.put("orderIdList", orderIdList);
            sendMsgMap.put("operAction", "发起");
            orderSendMsgService.sendMsgBefore(sendMsgMap);
        }catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus()
                    .setRollbackOnly();
            throw new Exception(e.getMessage());
        }
        return result + "," + appliTitle;
    }

    /**
     * 生成申请单标题临时
     *Version 1.0
     * @param
     * @return java.lang.String
     * @author ren.jiahang
     * @date 2019/1/8 10:35
     */
    public String getAppliTitle1() {
        int applTitle = querySequence("applTitle");
        String year = new SimpleDateFormat("yyyy").format(new Date()); //方法二   一般为.format(new Date())
        String apptit = "HNGLY-" + year + "-" + String.format("%04d", applTitle);
        return apptit;
    }

    /**
     * 生成申请单标题
     * Version 1.1
     * @param
     * @return java.lang.String
     * @author ren.jiahang
     * @date 2019/1/8 10:35
     */
    public synchronized String getAppliTitle2(){
        String userId = ThreadLocalInfoHolder.getLoginUser().getUserId(); //获取当前用户
        Map<String, Object> scheduNumMap = new HashMap<>();
        StringBuffer applTitle = new StringBuffer();
        String year = new SimpleDateFormat("yyyy").format(new Date());

        scheduNumMap.put("actionScope", "1"); //申请单标识 默认1
        //scheduNumMap.put("parOrgId", "13"); //默认13
//        scheduNumMap.put("userId", userId);
        String orgIdQu = insertOrderInfoDao.queryOrgId(userId); //获取组织ID
        scheduNumMap.put("orgIdQu", orgIdQu);

        Map map = insertOrderInfoDao.queryScheduNum(scheduNumMap);
        String org_id = map.get("ORG_ID").toString();
        String schedu_num = map.get("SCHEDU_NUM").toString();
        //String area_id = map.get("AREA_ID").toString();
        String schedu_code = map.get("SCHEDU_CODE").toString();
        String update_date_Str = map.get("UPDATE_DATE").toString();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        String updDate = null;

        try {
            updDate = sdf.format(sdf.parse(update_date_Str));
        }
        catch (ParseException e) {
            logger.error("获取申请单标题 失败", e.getMessage());
        }

        scheduNumMap.put("org_id", org_id);
        //当前年份不等于修改年份则说明是新的一年，修改scheduNum为初始值
        if (!year.equals(updDate)) {
            insertOrderInfoDao.updateInitScheduNum(scheduNumMap);
        }
        /*Map<String, Object> currentUserBranch = orderDealDao.getCurrentUserBranch(userId); //获取所属省公司
        String orgName = currentUserBranch.get("ORG_NAME").toString();
        String firstLetter = FirstLetterUtil.getFirstLetter(orgName).substring(0, 2); //获取所属省分简称*/
        String scheduNum = String.format("%04d", Integer.parseInt(schedu_num));
        applTitle.append(schedu_code).append("-").append(year).append("-").append(scheduNum);
        insertOrderInfoDao.updateScheduNum(scheduNumMap); //修改序列+1
        return applTitle.toString();
    }
    /**
     * 生成申请单标题 改为通过函数生成
     * Version 2.0
     * @param
     * @return java.lang.String
     * @author ren.jiahang
     * @date 2019/3/20 10:35
     */
    public synchronized String getAppliTitle() {
        String userId = ThreadLocalInfoHolder.getLoginUser().getUserId(); //获取当前用户
        String orgId = insertOrderInfoDao.queryOrgId(userId); //获取组织ID
        return insertOrderInfoDao.queryScheduNumFunction(orgId);
    }


    /*
     * 保存附件信息
     * @author ren.jiahang
     * @date 2019/1/18 11:21
     * @param map
     * @param srvId
     * @return java.lang.String
     */
    public String attachSave(Map<String, Object> map, int srvId) {
        String result = "";
        JSONArray fileJson = JSON.parseArray(map.get("upLoadResult").toString()); //获取附件信息
        try {
            if (fileJson != null && fileJson.size() > 0) {
                for (int j = 0; j < fileJson.size(); j++) {
                    JSONObject fileObj = ((JSONArray) fileJson.get(j)).getJSONObject(0);
                    Map<String, Object> attachMap = fileObj;
                    attachMap.put("srvOrdId", srvId);
                    attachMap.put("filePath", "createbuss");
                    attachMap.put("orderId", 0);
                    attachMap.put("origin", "FQ");
                    List<Map<String, Object>> attachList = orderStandbyDao.queryAttachBySrvId(attachMap);
                    if (!attachList.isEmpty()) {
                        orderStandbyDao.delAttachBuSrvId(attachMap); //如果存在附件删除
                        orderStandbyService.delFileToFtp(attachMap); //删除ftp上附件
                    }
                    result = String.valueOf(orderStandbyDao.upLoadAttach(attachMap)); //记录附件信息
                }
            }
        }
        catch (Exception e) {
            logger.error("****附件上传异常***", e);
        }
        return result;
    }

    public String circuitAttachSave(Map<String, Object> map, int srvId) {
        String result = "";
        //JSONArray fileJson = JSON.parseArray(map.get("cirData").toString()); //获取附件信息
        JSONArray fileJson = ((JSONArray) JSON.parseArray(map.get("cirData").toString()).getJSONObject(0).get("upDDKLoadResult"));
        try {
            if (fileJson != null && fileJson.size() > 0) {
                for (int j = 0; j < fileJson.size(); j++) {
                    JSONObject fileObj = ((JSONArray) fileJson.get(j)).getJSONObject(0);
                    Map<String, Object> attachMap = fileObj;
                    attachMap.put("srvOrdId", srvId);
                    attachMap.put("filePath", "createbuss");
                    attachMap.put("orderId", 0);
                    attachMap.put("origin", "FQ-CIR");
                    List<Map<String, Object>> attachList = orderStandbyDao.queryAttachBySrvId(attachMap);
                    if (!attachList.isEmpty()) {
                        orderStandbyDao.delAttachBuSrvId(attachMap); //如果存在附件删除
                        orderStandbyService.delFileToFtp(attachMap); //删除ftp上附件
                    }
                    result = String.valueOf(orderStandbyDao.upLoadAttach(attachMap)); //记录附件信息
                }
            }
        }
        catch (Exception e) {
            logger.error("****附件上传异常***", e);
        }
        return result;
    }

    // TODO: 2019/3/5  包装电路信息
    /*
     * 包装并插入电路信息
     * @author ren.jiahang
     * @date 2019/3/5 10:52
     * @param cirInfoArray
     * @param cirDatalMap 电路信息
     * @param service_id  产品编码
     * @param srv_ord_id  定单信息主键
     * @param currDateStr  创建世界
     * @return void
     */
    public void packageCircuitInfo(Map<String, Object> cirDatalMap, String service_id, int srv_ord_id, String currDateStr) throws Exception {
        cirDatalMap.remove("_id_"); //清除多余字段
        cirDatalMap.remove("upDDKLoadResult"); //清除多余字段
        cirDatalMap.remove("DDK"); //清除多余字段
        cirDatalMap.remove("fileList"); //清除多余字段
        cirDatalMap.remove("SRV_ORD_ID");
        cirDatalMap.remove("orderType");
        cirDatalMap.remove("业务实例创建接口返回结果");
        cirDatalMap.remove("业务实例回滚接口返回结果");
        cirDatalMap.remove("资源信息返回接口返回的子流程id");
        cirDatalMap.remove("资源配置更新cfs扩展属性更新接口返回结果");
        cirDatalMap.remove("业务电路汇总接口返回结果");
        cirDatalMap.remove("集客反馈接口返回结果");
        cirDatalMap.remove("集客订单回退接口返回结果");
        cirDatalMap.remove("集客起止租接口返回结果");
        cirDatalMap.remove("资源业务实例解挂接口返回结果");
        cirDatalMap.remove("资源业务实例挂起接口返回结果");
        cirDatalMap.remove("是否需要资源施工");
        StringBuffer errorInfo = new StringBuffer();
        ArrayList<Map<String, Object>> cirInfoArray = new ArrayList<Map<String, Object>>();
        List<PropertyDto> propertyDtos = propertyCofgService.qureyPropConfBySrvId(service_id); //查询配置表
        boolean logfirst = true;
        for (Map.Entry<String, Object> object : cirDatalMap.entrySet()) {
            String key = object.getKey();
            String value = String.valueOf(object.getValue()).trim(); //去除首尾空格  String.trim (); modify ren.jiahang 20190422
            if (value != null&&!"".equals(key)) {
                Map<String, Object> cirInfoMap = new HashMap<String, Object>();
                boolean isMatch = false;
                for (PropertyDto prod : propertyDtos) {
                    if (key.equals(prod.getLOCAL_CODE())) {
                        isMatch = true;
                        //  cirInfoMap.put("ATTR_INFO_ID",seq_gom_bdw_srv_ord_attr_info);
                        cirInfoMap.put("SRV_ORD_ID", srv_ord_id);
                        cirInfoMap.put("ATTR_ACTION", 0);
                        cirInfoMap.put("ATTR_CODE", prod.getPROPERTY_ID());
                        cirInfoMap.put("ATTR_NAME", prod.getPROPERTY_NAME());
                        cirInfoMap.put("ATTR_VALUE", value);
                        cirInfoMap.put("ATTR_VALUE_NAME", key);
                        cirInfoMap.put("CREATE_DATE", currDateStr);
                        cirInfoMap.put("SOURSE", "内部建单");
                    }
                }
                if (!isMatch&&!"".equals(key)) {
                    if (logfirst) {
                        logfirst = false;
                        errorInfo.append("产品编号 " + service_id + " 请检查配置表是否已配置以下字段");
                    }
                    errorInfo.append("[" + key + "] ");

                    logger.error("----error----产品：[" + service_id + "]--匹配失败字段---[" + key + "]--请检查产品属性表（gom_BDW_property_info）---");
                }
                else {
                    cirInfoArray.add(cirInfoMap);
                }

            }
        }
        if(errorInfo.length()>0){
            throw new Exception(errorInfo.toString());
        }
        querySequence("gom_BDW_srv_ord_attr_info"); //序列刷掉一个，解决第一次提交报违反唯一约束
        insertordAttrInfo(cirInfoArray); //插入电路信息
    }

    /*
     * 获取流程实例
     * @author ren.jiahang
     * @date 2019/3/6 17:09
     * @param service_id
     * @param orderType
     * @param actType
     * @return java.lang.String
     */
    public String getProcesParam(String service_id, String orderType, String actType) {
        String result;
        Map<String, Object> queryProcessMap = new HashMap<String, Object>();
        if ("20181221006".equals(service_id)) {
            queryProcessMap.put("codeType", "flow_cust"); //所有局内流程 写死为内部流程
        }
        else {
            queryProcessMap.put("codeType", "flow_local"); //流程类型 写死为内部流程
        }
        queryProcessMap.put("codeContent", orderType); //订单类型开通单
        queryProcessMap.put("codeTypeName", service_id); //产品编码
        queryProcessMap.put("codeValue", actType); //操作类型
        if ("102".equals(orderType)) { //单据类型102代表核查
            queryProcessMap.put("codeType", "flow_check"); //流程类型 写死为核查流程
            // service_id = "20190220001"; //代表核查单所有产品，用来查询页面展示属
        }
        //查询流程参数
        List<Map<String, Object>> services = getEnumService.queryProcessInst(queryProcessMap);
        if (services != null && services.size() == 1) {
            result = services.get(0).get("SORT_NO").toString();
        }
        else {
            result = "查询流程参数为空或查询到多个流程参数，查询到流程数量：[" + services.size() + "]";
        }
        return result;
    }

    /*
     * 记录日志
     * @author ren.jiahang
     * @date 2019/4/12 10:16
     * @param params
     * @return java.util.Map
     */
    public void insertTrackLogInfo(String orderId){
        String operStaffId = "";
        if (ThreadLocalInfoHolder.getLoginUser() == null) {
            operStaffId = "11";
        }
        else {
            //获取用户id
            operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        }
        Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(operStaffId));
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        String operStaffName = ThreadLocalInfoHolder.getLoginUser().getUserName();
        paramsMap.put("orderId", orderId);
        // paramsMap.put("woOrdId", woOrderId);
        paramsMap.put("trackOrgId", MapUtils.getString(operStaffInfoMap, "ORG_ID"));
        paramsMap.put("trackOrgName", MapUtils.getString(operStaffInfoMap, "ORG_NAME"));
        paramsMap.put("trackDate", new java.sql.Date(new java.util.Date().getTime()));
        paramsMap.put("createDate", new java.sql.Date(new java.util.Date().getTime()));
        paramsMap.put("trackStaffId", operStaffId);
        paramsMap.put("trackStaffName", operStaffName);
        paramsMap.put("trackStaffPhone", MapUtils.getString(operStaffInfoMap, "USER_PHONE"));
        paramsMap.put("trackStaffEmail", MapUtils.getString(operStaffInfoMap, "USER_EMAIL"));
        String trackMessage = "[" + operStaffName +  "发起申请单]";
        paramsMap.put("trackMessage", trackMessage);
        String trackContent = "[发起申请单]";
        paramsMap.put("trackContent", trackContent);
        paramsMap.put("operType", OrderTrackOperType.OPER_TYPE_13);
        orderDealDao.insertTrackLogInfo(paramsMap);
    }
    /*
     * 发送短信通知
     * @author ren.jiahang
     * @date 2019/4/23 16:25
     * @param orderId
     * @param operStaffId
     * @return void
     */
   /* public void sendMassage(String orderId,String operStaffId){
        //查询当前操作用户的所属区域，并查询其区域需不要发送短信
        Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(operStaffId));
        String areaId = MapUtils.getString(operStaffInfoMap,"AREA_ID");
        Map<String,Object> msmSwitchMap = orderDealDao.qryMsmSwitchByArea(areaId);
        if("1".equals(MapUtils.getString(msmSwitchMap,"ISSEND"))){
            //ISSEND等于1发送短信
            orderDealServiceIntf.qryUserObjByWoId(orderId,"ORDERID");
        }
    }*/
}
