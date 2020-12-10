package com.zres.project.localnet.portal.affairdispatch.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;

import com.zres.project.localnet.portal.affairdispatch.constants.AffairDispatchOrderConstant;
import com.zres.project.localnet.portal.affairdispatch.dao.DispatchOrderManageDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.local.domain.PageInfo;
import com.zres.project.localnet.portal.util.OrderTrackOperType;
import com.ztesoft.res.frame.core.util.MapUtil;
import com.ztesoft.res.frame.core.util.StringUtils;
import com.ztesoft.res.frame.flow.common.dto.FlowOrderDTO;
import com.ztesoft.res.frame.flow.common.dto.FlowOrderSpecDTO;
import com.ztesoft.res.frame.flow.common.dto.FlowRollBackReasonDTO;
import com.ztesoft.res.frame.flow.common.dto.FlowWoDTO;
import com.ztesoft.res.frame.flow.common.service.FlowActionHandler;
import com.ztesoft.res.frame.flow.spec.order.dao.FlowOrderSpecDAO;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;

/**
 * 事务调单管理接口实现
 *
 * @author PangHao
 * @date 2019/4/22 : 18:46
 */
@Service
public class DispatchOrderManageService implements DispatchOrderManageIntf
{

    private Logger logger = LoggerFactory.getLogger(DispatchOrderManageService.class);

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private DispatchOrderManageDao dispatchOrderManageDao;

    @Autowired
    private FlowOrderSpecDAO flowOrderSpecDAO;

    @Autowired
    private FlowActionHandler flowActionHandler;

    @Autowired
    private OrderDealDao orderDealDao;

    /**
     * 发起事务调单
     *
     * @param map 前端传来的事务点单信息
     * @return 处理结果描述
     * @author PangHao
     * @date 2019/4/22 : 18:51
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> initAffairDispatchOrder(Map<String, Object> map) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        String result = "success";
        StringBuilder errorInfo = new StringBuilder();
        try {
            logger.info("入参：" + map.toString());
            // 得到事务调单信息
            Map<String, Object> affairDisOrderInfo = JSON.parseObject(map.get("affairDisOrderInfo").toString());
            logger.info("事务调单入参：" + affairDisOrderInfo);
            String affairId = MapUtils.getString(affairDisOrderInfo, "affairId", "");
            String state = MapUtils.getString(affairDisOrderInfo, "state");
            String staffId = getOperatorId();
            // 事务调单编码
            String code;
            // 抄送人信息
            List noticeStaffList = (List) map.get("noticeStaffArray");
            //*****当审核驳回时重新发起时需要工单id和定单id*********
            // 该环节工单ID
            String workOrderId = MapUtils.getString(affairDisOrderInfo, "workOrderId", "");
            //定单ID
            String orderId = MapUtils.getString(affairDisOrderInfo, "orderId", "");
            //***********************************************
            if (Objects.equals(affairId, "")) {
                // 得到事务调单主键
                affairId = dispatchOrderManageDao.querySequence("SEQ_GOM_BDW_AFFAIR_DISP_ORDER.NEXTVAL").toString();
                affairDisOrderInfo.put("affairId", affairId);
                //得到地区编码
                String orgid = dispatchOrderManageDao.queryOrgId(staffId);
                // 得到事务调单编码
                code = getAppliTitle(orgid);
                affairDisOrderInfo.put("code", code);
                affairDisOrderInfo.put("createStaff", staffId);
                // 创建事务调单信息
                dispatchOrderManageDao.insertAffairDispatchOrder(affairDisOrderInfo);
                resultMap.put("code", code);
                resultMap.put("result", result);
                // 抄送人信息入库
                if (null != noticeStaffList && !noticeStaffList.isEmpty()) {
                    batchInsertNoticeRecord(noticeStaffList, affairId);
                }

                // 保存至草稿箱
                if (AffairDispatchOrderConstant.AFFAIR_DIS_STATE_1.equals(state)) {
                    return resultMap;
                }
                // 如果直接发起
                /*
                 * if (AffairDispatchOrderConstant.AFFAIR_DIS_STATE_2.equals(state)) { }
                 */
            } else {
                code = MapUtils.getString(affairDisOrderInfo, "code");
                resultMap.put("code", code);
                resultMap.put("result", result);
                affairDisOrderInfo.put("updateStaff", staffId);
                // 作废抄送人记录
                dispatchOrderManageDao.obsoleteNoticeRecord(affairId, AffairDispatchOrderConstant.ORDER_NOTICE_STATE_2,
                        AffairDispatchOrderConstant.ORDER_NOTICE_TYPE_7);
                // 抄送人信息入库
                if (null != noticeStaffList && !noticeStaffList.isEmpty()) {
                    batchInsertNoticeRecord(noticeStaffList, affairId);
                }
                // 更新事务调单信息
                dispatchOrderManageDao.updateAffairDispatchOrder(affairDisOrderInfo);
                // 草稿箱更新
                if (AffairDispatchOrderConstant.AFFAIR_DIS_STATE_1.equals(state)) {
                    return resultMap;
                }
                // 草稿箱发起
                /*
                 * if (AffairDispatchOrderConstant.AFFAIR_DIS_STATE_2.equals(state)) { }
                 */
            }

            //如果没有工单id,即审核驳回，启动新流程，并得到定单ID
            if (null == workOrderId || Objects.equals("", workOrderId.trim())) {
                // 启动主流程
                orderId = createOrder(affairDisOrderInfo);
                // 得到该环节工单信息
                List<Map<String, Object>> workOrderList = dispatchOrderManageDao.getWorkOrder(orderId,
                        AffairDispatchOrderConstant.INITIATE_AFFAIR);
                if (null != workOrderList && !workOrderList.isEmpty()) {
                    workOrderId = MapUtils.getString(workOrderList.get(0), "WOID");
                } else {
                    throw new Exception("未查询到工单信息");
                }
            }

            // 更新事务调单记录定单id
            dispatchOrderManageDao.updateAffairOrderId(affairId, orderId);
            affairDisOrderInfo.put("orderId", orderId);
            // 业务调单信息入库
            logger.info("业务调单信息：" + affairDisOrderInfo.toString());


            // 如果不审核循环创建子流程
            String isCheck = MapUtils.getString(affairDisOrderInfo, "isCheck");
            // 如果审核，主流程回单
            if (null != isCheck && Objects.equals(AffairDispatchOrderConstant.IS_CHECK, isCheck.trim())) {
                // 回单 ifSubmitReview=0
                // 封装线条参数
                List<HashMap<String, String>> operAttrs = new ArrayList<>();
                HashMap<String, String> operAttrsMap1 = new HashMap<>(2);
                operAttrsMap1.put("KEY", "ifSubmitReview");
                operAttrsMap1.put("VALUE", AffairDispatchOrderConstant.IS_CHECK);
                operAttrs.add(operAttrsMap1);
                String checkStaff = MapUtils.getString(affairDisOrderInfo, "checkStaff");
                //封装处理人参数
                HashMap<String, String> operAttrsMap2 = new HashMap<>(2);
                operAttrsMap2.put("KEY", AffairDispatchOrderConstant.AFFAIR_REVIEW + "_DISP_OBJ");
                operAttrsMap2.put("VALUE", "260000003_J!G@F_" + checkStaff);
                operAttrs.add(operAttrsMap2);
                // 回单
                complateWo(workOrderId, operAttrs);
                // 更改事务状态为审核中
                dispatchOrderManageDao.modifyAffairDisState(affairId, AffairDispatchOrderConstant.AFFAIR_DIS_STATE_4,
                        staffId);
                // 日志入库
                Map<String, Object> logMap = new HashMap<>(12);
                logMap.put("orderId", orderId);
                logMap.put("woOrdId", workOrderId);
                logMap.put("typeName", "发起事务调单回单");
                logMap.put("operType", OrderTrackOperType.OPER_TYPE_13);
                logMap.put("remark", MapUtils.getString(affairDisOrderInfo, "content"));
                insertNoticeLog(logMap);
            } else {
                // 得到处理人id集合，批量启动子流程
                String disposeStaffArrStr = MapUtils.getString(affairDisOrderInfo, "disposeStaffArr");
                List<String> disposeStaffList = JSONArray.parseArray(disposeStaffArrStr, String.class);
                batchCreateSonOrder(orderId, disposeStaffList);
                // 状态改为事务处理中
                dispatchOrderManageDao.modifyAffairDisState(affairId, AffairDispatchOrderConstant.AFFAIR_DIS_STATE_5,
                        staffId);
            }

        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus()
                    .setRollbackOnly();
            result = "error";
            errorInfo.append(",").append(e.getMessage());
            logger.info("报错啦！！！！！！", errorInfo.toString());

        }
        resultMap.put("result", result);
        resultMap.put("errorInfo", errorInfo);

        return resultMap;
    }

    /**
     * 生成事物调单编号 改为通过函数生成
     * Version 2.0
     * @param
     * @return java.lang.String
     * @author ren.jiahang
     * @date 2019/3/20 10:35
     */
    public synchronized String getAppliTitle( String orgId) {
        return  "SWDD-"+dispatchOrderManageDao.queryDispatchOrderNumFunction(orgId);
    }
    /**
     * 子流程事务处理回单
     *
     * @param map 事物處理參數
     * @return 回單結果
     * @author PangHao
     * @date 2019/4/26 : 10:19
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> affairProcessComplateWo(Map<String, Object> map) {
        Map<String, Object> resultMap = new HashMap<>(2);
        String resultType = "SUCCESS";
        String message;
        try {
            String woId = MapUtils.getString(map, "workOrderId");
            String orderId = MapUtils.getString(map, "orderId");
            String remark = MapUtils.getString(map, "remark");
            String affairId = MapUtils.getString(map, "affairId");
            String createStaff = MapUtils.getString(map, "createStaff");
            String reviewStaff = MapUtils.getString(map, "reviewStaff"); //审查人
            String isCheck = MapUtils.getString(map, "isCheck"); //是否审核

            /**
             * 审核处理环节添加条件：是否需要审查
             *  需要：到事务审查环节；
             *  不需要：回单该环节，并且回单父流程；
             */
            String isReview = MapUtils.getString(map, "isReviewCheck"); // 是否审查
            List<HashMap<String, String>> operAttrsIsReview = new ArrayList<>(); //传线条参数
            HashMap<String, String> operAttrsIsReviewMap = new HashMap<>(2);
            operAttrsIsReviewMap.put("KEY", "is_review_check");
            operAttrsIsReviewMap.put("VALUE", isReview);
            operAttrsIsReview.add(operAttrsIsReviewMap);
            if (StringUtil.isNotEmpty(reviewStaff)){
                //下个环节处理人
                HashMap<String, String> operAttrsReviewUserMap = new HashMap<>(2);
                operAttrsReviewUserMap.put("KEY", AffairDispatchOrderConstant.AFFAIR_REVIEW_CHECK + "_DISP_OBJ");
                operAttrsReviewUserMap.put("VALUE", "260000003_J!G@F_" + reviewStaff);
                operAttrsIsReview.add(operAttrsReviewUserMap);
            }

            // 通用回单方法
            complateWo(woId, operAttrsIsReview);
            // 更新工单信息
            dispatchOrderManageDao.modifyWoOrderRemark(woId, remark);
            // 子流程回单日志入库
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("orderId", orderId);
            logMap.put("woOrdId", woId);
            logMap.put("typeName", "事务调单-事务处理");
            logMap.put("operType", OrderTrackOperType.OPER_TYPE_4);
            logMap.put("remark", "事务调单-事务处理回单:" + remark);
            insertNoticeLog(logMap);
            // 事务处理附件入库
            List fileInfos = (List) map.get("fileInfos");
            if (null != fileInfos && !fileInfos.isEmpty()) {
                // 工单id暂存为0用于保存事务调单附件，与其他环节附件区分
                dispatchOrderManageDao.batchInsertOrderFileRecord(woId, orderId, fileInfos);
            }
            if (null != isReview && Objects.equals(AffairDispatchOrderConstant.NO_CHECK, isReview.trim())) {
                //不需要
                // 得到父流程工单信息
                List<Map<String, Object>> fatherWoOrderList = dispatchOrderManageDao.getParentWorkOrderList(orderId);
                Map<String, Object> workOrder = fatherWoOrderList.get(0);
                // 如果父流程最新工单环节不是事务确认
                if (!Objects.equals(AffairDispatchOrderConstant.AFFAIR_CONFIROM,
                        MapUtils.getString(workOrder, "TACHE_CODE"))) {
                    // 得到父流程当前环节工单 ID
                    String parentOrderId = MapUtils.getString(workOrder, "ORDER_ID");
                    String parentWoOrderId = MapUtils.getString(workOrder, "WO_ID");
                    //封装处理人参数
                    ArrayList<HashMap<String, String>> operAttrs = new ArrayList<>();
                    HashMap<String, String> operAttrsMap2 = new HashMap<>(2);
                    operAttrsMap2.put("KEY", AffairDispatchOrderConstant.AFFAIR_CONFIROM + "_DISP_OBJ");
                    operAttrsMap2.put("VALUE", "260000003_J!G@F_" + createStaff);
                    operAttrs.add(operAttrsMap2);
                    if (null != isCheck && Objects.equals(AffairDispatchOrderConstant.NO_CHECK, isCheck.trim())) {
                        //如果不审核传新的线条参数
                        HashMap<String, String> operAttrsMap1 = new HashMap<>(2);
                        operAttrsMap1.put("KEY", "ifSubmitReview");
                        operAttrsMap1.put("VALUE", AffairDispatchOrderConstant.NO_CHECK);
                        operAttrs.add(operAttrsMap1);
                    }

                    // 父流程回单
                    complateWo(parentWoOrderId, operAttrs);
                    // 父流程回单日志入库
                    Map<String, Object> logMap1 = new HashMap<>();
                    logMap1.put("orderId", parentOrderId);
                    logMap1.put("woOrdId", parentWoOrderId);
                    logMap1.put("typeName", "事务调单-事务处理");
                    logMap1.put("operType", OrderTrackOperType.OPER_TYPE_4);
                    logMap1.put("remark", "事务调单事务处理通过");
                    insertNoticeLog(logMap1);
                }
                // 更改事务调单状态为事务确认
                dispatchOrderManageDao.modifyAffairDisState(affairId, AffairDispatchOrderConstant.AFFAIR_DIS_STATE_8,
                        getOperatorId());
            }
            message = "事务处理成功";
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus()
                    .setRollbackOnly();
            resultType = "FAILED";
            message = "事务处理失败，原因：" + e.getMessage();
        }
        resultMap.put("code", resultType);
        resultMap.put("message", message);
        return resultMap;
    }

    /**
     * 判断子流程是否全部结束，更新事务单状态
     *
     * @param affairId
     */
    private void modifyAffairDisState(String affairId) {
        // 得到所有子流程最新工单信息
        List<Map<String, Object>> childWoOrderList = dispatchOrderManageDao.getChildWorkOrderList(affairId);
        int i = 0;
        for (Map<String, Object> map : childWoOrderList) {
            String tacheCode = MapUtil.getString(map, "TACHE_CODE");
            if ("AFFAIR_CONFIROM_WAIT".equals(tacheCode)) {
                i++;
            }
        }
        if (i == childWoOrderList.size()) {
            dispatchOrderManageDao.modifyAffairDisState(affairId, AffairDispatchOrderConstant.AFFAIR_DIS_STATE_8,
                    getOperatorId());
        } else {
            dispatchOrderManageDao.modifyAffairDisState(affairId, AffairDispatchOrderConstant.AFFAIR_DIS_STATE_6,
                    getOperatorId());
        }
    }

    /**
     * 无线条参数回单
     *
     * @param woId      工单ID
     * @param operAttrs 线条参数
     * @throws Exception exception异常
     * @author PangHao
     * @date 2019/4/28 : 11:25
     */
    private void complateWo(String woId, List<HashMap<String, String>> operAttrs) throws Exception {
        FlowWoDTO flowWoDTO = new FlowWoDTO();
        flowWoDTO.setWoId(woId);
        flowWoDTO.setOperAttrs(operAttrs);
        flowWoDTO.setCreateDate(formatter.format(LocalDateTime.now()));
        // 流程回单
        boolean isSuccess = flowActionHandler.complateWo(getOperatorId(), flowWoDTO);
        if (!isSuccess) {
            throw new Exception("子流程回单失败");
        }
    }

    /**
     * 事务调单审核
     *
     * @param map 审核工单信息
     * @return 回单结果
     * @author PangHao
     * @date 2019/4/26 : 10:29
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> affairDispatchOrderCheck(Map<String, Object> map) {

        Map<String, Object> resultMap = new HashMap<>(2);
        String resultType = "SUCCESS";
        String message;
        try {
            // 是否通过
            String staffId = getOperatorId();
            String isPass = "0";
            String woId = MapUtils.getString(map, "workOrderId");
            String id = MapUtils.getString(map, "affairId");
            String orderId = MapUtils.getString(map, "orderId");
            String remark = MapUtils.getString(map, "remark");

            if (Objects.equals(isPass, MapUtils.getString(map, "isPass"))) {
                // 审核通过,更改狀態為事務處理中
                dispatchOrderManageDao.modifyAffairDisState(id, AffairDispatchOrderConstant.AFFAIR_DIS_STATE_5,
                        staffId);
                // 通过id得到事务调单信息
                Map<String, Object> affairDisOrder = dispatchOrderManageDao.getAffairDisOrderById(id);
                String disposeStaffArrStr = MapUtils.getString(affairDisOrder, "DISPOSE_STAFF_ARR");
                // 获得事务处理人员集合
                List<String> disposeStaffList = JSONArray.parseArray(disposeStaffArrStr, String.class);
                // 循环启动子流程
                batchCreateSonOrder(orderId, disposeStaffList);
                // 退单日志入库
                Map<String, Object> logMap = new HashMap<>(12);
                logMap.put("orderId", orderId);
                logMap.put("woOrdId", woId);
                logMap.put("typeName", "事务调单事务审核通过");
                logMap.put("operType", OrderTrackOperType.OPER_TYPE_4);
                logMap.put("remark", remark);
                insertNoticeLog(logMap);
                message = "事务调单审核通过";

            } else {
                // 审核不通过父流程退单
                List<FlowRollBackReasonDTO> flowRollBackReasonDTOS = flowActionHandler.queryRollBackReasons(woId);
                logger.info("退單原因集合：" + flowRollBackReasonDTOS.toString());
                boolean isSuccess = flowActionHandler.rollBackWo(staffId, woId, flowRollBackReasonDTOS.get(0), remark);
                if (!isSuccess) {
                    throw new Exception("事务调单退单失败！");
                }
                // 事务状态改为审核驳回
                dispatchOrderManageDao.modifyAffairDisState(id, AffairDispatchOrderConstant.AFFAIR_DIS_STATE_3,
                        staffId);
                // 退单日志入库
                Map<String, Object> logMap = new HashMap<>(12);
                logMap.put("orderId", orderId);
                logMap.put("woOrdId", woId);
                logMap.put("typeName", "事务调单事务审核驳回");
                logMap.put("operType", OrderTrackOperType.OPER_TYPE_5);
                logMap.put("remark", remark);
                insertNoticeLog(logMap);
                message = "事务调单审核驳回";
            }
            // 更新工单信息
            dispatchOrderManageDao.modifyWoOrderRemark(woId, remark);
            // 审核环节附件入库
            List fileInfos = (List) map.get("fileInfos");
            if (null != fileInfos && !fileInfos.isEmpty()) {
                // 工单id暂存为0用于保存事务调单附件，与其他环节附件区分
                dispatchOrderManageDao.batchInsertOrderFileRecord(woId, orderId, fileInfos);
            }

        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus()
                    .setRollbackOnly();
            resultType = "FAILED";
            message = "事务调单审核失败，原因：" + e.getMessage();
        }
        resultMap.put("code", resultType);
        resultMap.put("message", message);
        return resultMap;
    }

    /**
     * 查询各事务单数量
     *
     * @param map
     * @return
     */
    @Override
    public Map<String, Object> countVariousAffairOrder(Map<String, Object> map) {
        Map<String, Object> resultMap = new HashMap<>(2);
        Map<String, Object> parameters = MapUtils.getMap(map, "parameters");
        String resultType = "SUCCESS";
        String[] various = new String[]{
                "fqOrder", "cgOrder", "shOrder", "clOrder", "qrOrder", "lsOrder", "tzOrder", "scOrder"
        };
        map.put("isGetData", false);
        Map<String, Object> counts = new HashMap<>(7);
        /*parameters.put("affOrderCode", "");
        parameters.put("orderTitle", "");
        parameters.put("orderState", "");
        parameters.put("beginDate", "");
        parameters.put("endDate", "");*/
        try {
            for (int i = 0; i < various.length; i++) {
                map.put("queryType", various[i]);
                switch (various[i]) {
                    case "cgOrder":
                        parameters.put("orderState", "290000112");
                        break;
                    case "shOrder":
                        parameters.put("orderState", "290000115");
                        break;
                    case "qrOrder":
                        parameters.put("orderState", "290000119");
                        break;
                    default:
                        parameters.put("orderState", "");
                        break;
                }
                map.put("parameters", parameters);
                Map<String, Object> result = queryAffairOrderList(map);
                if (!result.isEmpty()) {
                    counts.put(various[i], MapUtils.getIntValue(result, "dataLength", 0));
                }
            }
        } catch (Exception e) {
            logger.error("查询各事务单数量错误：", e);
            resultType = "ERROR";
        }
        resultMap.put("code", resultType);
        resultMap.put("counts", counts);
        return resultMap;
    }

    /**
     * 查询审核驳回待处理工单数
     *
     * @param map
     * @return
     */
    @Override
    public Map<String, Object> queryShRejectNum(Map<String, Object> map) {
        return queryRejectNum(map, AffairDispatchOrderConstant.AFFAIR_DIS_STATE_3);
    }

    /**
     * 查询驳回待处理工单数
     *
     * @param map
     * @return
     */
    public Map<String, Object> queryRejectNum(Map<String, Object> map, String orderState) {
        Map<String, Object> resultMap = new HashMap<>(2);
        Map<String, Object> parameters = MapUtils.getMap(map, "parameters");
        String resultType = "SUCCESS";
        int counts = 0;
        map.put("isGetData", false);
        try {
            parameters.put("orderState", orderState);
            parameters.put("isReject", "true");
            map.put("parameters", parameters);
            Map<String, Object> result = queryAffairOrderList(map);
            if (!result.isEmpty()) {
                counts = MapUtils.getIntValue(result, "dataLength", 0);
            }
        } catch (Exception e) {
            resultType = "ERROR";
        }
        resultMap.put("code", resultType);
        resultMap.put("counts", counts);
        return resultMap;
    }

    /**
     * 查询事务处理被驳回待处理工单数
     *
     * @param map
     * @return
     */
    @Override
    public Map<String, Object> queryClRejectNum(Map<String, Object> map) {
        return queryRejectNum(map, AffairDispatchOrderConstant.AFFAIR_DIS_STATE_8);
    }

    /**
     * 父流程事务确认回单
     *
     * @param map 事务确认工单信息
     * @return 回单结果
     * @author PangHao
     * @date 2019/4/26 : 10:34
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> affairAffirmComplateWo(Map<String, Object> map) {
        Map<String, Object> resultMap = new HashMap<>(2);
        String resultType = "SUCCESS";
        String message;
        try {
            // 判断是否存在事务处理环节子流程，如是，禁止回单
            String orderId = MapUtils.getString(map, "orderId");
            String woId = MapUtils.getString(map, "workOrderId");
            String remark = MapUtils.getString(map, "remark");
            String affairId = MapUtils.getString(map, "affairId");
            // 得到所有子流程定单
            List<Map<String, Object>> orderList = dispatchOrderManageDao.getSonOrderList(orderId);
            // 待统一回单的子流程
            for (Map<String, Object> sonOrder : orderList) {
                String sonOrderId = MapUtils.getString(sonOrder, "ORDER_ID");
                // 得到子流程环节等待信息
                List<Map<String, Object>> workOrderList = dispatchOrderManageDao.getWorkOrder(sonOrderId,
                        AffairDispatchOrderConstant.AFFAIR_CONFIROM_WAIT);
                if (null != workOrderList && !workOrderList.isEmpty()) {
                    //子流程回单
                    complateWo(MapUtils.getString(workOrderList.get(0), "WOID"), null);
                } else {
                    resultType = "FAILED";
                    message = "存在未完成处理的事务调单，无法确认！";
                    resultMap.put("code", resultType);
                    resultMap.put("message", message);
                    return resultMap;
                }
            }
            // 父流程回单
            complateWo(woId, null);
            // 更新工单信息
            dispatchOrderManageDao.modifyWoOrderRemark(woId, remark);
            // 更改事务调单状态为已完成
            dispatchOrderManageDao.modifyAffairDisState(affairId, AffairDispatchOrderConstant.AFFAIR_DIS_STATE_9,
                    getOperatorId());

            // 日志入库
            Map<String, Object> logMap = new HashMap<>(10);
            logMap.put("orderId", orderId);
            logMap.put("woOrdId", woId);
            logMap.put("typeName", "事务确认回单");
            logMap.put("operType", OrderTrackOperType.OPER_TYPE_4);
            logMap.put("remark", "事务确认回单");
            insertNoticeLog(logMap);
            message = "事务调单已确认！";
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus()
                    .setRollbackOnly();
            resultType = "FAILED";
            message = "事务调单确认失败，原因：" + e.getMessage();
        }
        resultMap.put("code", resultType);
        resultMap.put("message", message);
        return resultMap;
    }

    /**
     * 子流程事务处理批量退单
     *
     * @param orderList 子流程工单信息集合
     * @return 退单结果
     * @author PangHao
     * @date 2019/4/28 : 14:38
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> affairProcessRollBackWo(List<Map<String, Object>> orderList) {
        Map<String, Object> resultMap = new HashMap<>(2);
        String resultType = "SUCCESS";
        String message;
        try {
            if (null == orderList || orderList.isEmpty()) {
                throw new Exception("缺失信息！");
            }
            for (Map<String, Object> map : orderList) {
                String orderId = MapUtils.getString(map, "orderId");
                String remark = MapUtils.getString(map, "remark");
                // 得到子流程环节等待信息
                List<Map<String, Object>> workOrderList = dispatchOrderManageDao.getWorkOrder(orderId,
                        AffairDispatchOrderConstant.AFFAIR_CONFIROM_WAIT);
                String woId = MapUtils.getString(workOrderList.get(0), "WOID");
                List<FlowRollBackReasonDTO> flowRollBackReasonDTOS = flowActionHandler.queryRollBackReasons(woId);
                logger.info("退單原因集合：" + flowRollBackReasonDTOS.toString());
                boolean isSuccess = flowActionHandler.rollBackWo(getOperatorId(), woId, flowRollBackReasonDTOS.get(0),
                        remark);
                if (!isSuccess) {
                    throw new Exception("事务确认退单失败！");
                }
                // 更新工单信息
                dispatchOrderManageDao.modifyWoOrderRemark(woId, remark);
                // 退单日志入库
                Map<String, Object> logMap = new HashMap<>();
                logMap.put("orderId", orderId);
                logMap.put("woOrdId", woId);
                logMap.put("typeName", "事务处理退单");
                logMap.put("operType", OrderTrackOperType.OPER_TYPE_5);
                logMap.put("remark", "事务处理退单");
                insertNoticeLog(logMap);
            }
            message = "事务处理退单成功！";
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus()
                    .setRollbackOnly();
            resultType = "FAILED";
            message = "事务处理退单失败，原因：" + e.getMessage();
        }
        resultMap.put("code", resultType);
        resultMap.put("message", message);
        return resultMap;
    }

    // 得到事务调单附件(作废)
    /*
     * @Override public List<Map<String, Object>> getAffairDispatchFile(String affairId) { return
     * dispatchOrderManageDao.getAffairDispatchFile(affairId, AffairDispatchOrderConstant.INITIATE_AFFAIR); }
     */

    /**
     * 得到事务调单抄送人
     *
     * @param affairId 事务调单标识
     * @author PangHao
     * @date 2019/4/29 : 16:15
     */
    @Override
    public List<Map<String, Object>> getAffairNoticeStaffArray(String affairId) {
        return dispatchOrderManageDao.getAffairNoticeStaffArray(affairId);
    }

    /**
     * 获取事务调单审核信息
     *
     * @param affairId 事务调单标识
     * @author PangHao
     * @date 2019/4/29 : 16:15
     */
    @Override
    public List<Map<String, Object>> getAffairCheckInfoArray(String affairId) {
        List<Map<String, Object>> list = dispatchOrderManageDao.getAffairCheckInfoArray(affairId);
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> map = list.get(i);
            String woId = MapUtils.getString(map, "WO_ID");
            List<Map<String, Object>> files = getAffairWoAccessoryFile(woId);
            map.put("FILES", files);
            list.set(i, map);
        }
        return list;
    }

    /**
     * 获取事务调单处理进度列表
     *
     * @param affairId 事务调单标识
     * @author PangHao
     * @date 2019/4/29 : 16:15
     */
    @Override
    public List<Map<String, Object>> getAffairDisposeList(String affairId) {
        List<Map<String, Object>> list = dispatchOrderManageDao.getAffairDisposeList(affairId);
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> map = list.get(i);
            String woId = MapUtils.getString(map, "WO_ID");
            List<Map<String, Object>> files = getAffairWoAccessoryFile(woId);
            map.put("FILES", files);
            list.set(i, map);
        }
        return list;
    }

    /**
     * 获取事务调单子单处理进度
     *
     * @param orderId 定单标识
     * @author PangHao
     * @date 2019/4/29 : 16:15
     */
    @Override
    public List<Map<String, Object>> getChildAffairDisposeList(String orderId) {
        List<Map<String, Object>> list = dispatchOrderManageDao.getChildAffairDisposeList(orderId);
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> map = list.get(i);
            String woId = MapUtils.getString(map, "WO_ID");
            List<Map<String, Object>> files = getAffairWoAccessoryFile(woId);
            map.put("FILES", files);
            list.set(i, map);
        }
        return list;
    }

    /**
     * 获取工单附件
     *
     * @param woId 工单ID
     * @author PangHao
     * @date 2019/4/29 : 16:15
     */
    @Override
    public List<Map<String, Object>> getAffairWoAccessoryFile(String woId) {
        return dispatchOrderManageDao.getAffairWoAccessoryFile(woId);
    }

    /**
     * 关闭事务调单
     *
     * @param map 事务调单及定单标识
     * @return 关闭结果
     * @author PangHao
     * @date 2019/5/9 : 16:56
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> closeAffair(Map<String, Object> map) {
        Map<String, Object> resultMap = new HashMap<>(2);
        String resultType = "SUCCESS";
        String message;
        try {
            String orderId = MapUtils.getString(map, "orderId");
            String affairId = MapUtils.getString(map, "affairId");
            String woId = MapUtils.getString(map, "workOrderId");
            if (null != orderId && !"".equals(orderId.trim())) {
                // 得到所有子流程定单
                List<Map<String, Object>> orderList = dispatchOrderManageDao.getSonOrderList(orderId);
                // 子流程统一作废
                for (Map<String, Object> sonOrder : orderList) {
                    String sonOrderId = MapUtils.getString(sonOrder, "ORDER_ID");
                    flowActionHandler.cancelOrder(getOperatorId(), sonOrderId);
                }
                //作废父流程
                flowActionHandler.cancelOrder(getOperatorId(), orderId);
            }

            // 更改事务调单状态为已完成
            dispatchOrderManageDao.modifyAffairDisState(affairId, AffairDispatchOrderConstant.AFFAIR_DIS_STATE_10,
                    getOperatorId());

            message = "事务调单已关闭！";
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus()
                    .setRollbackOnly();
            resultType = "FAILED";
            message = "事务调单确认失败，原因：" + e.getMessage();
        }
        resultMap.put("code", resultType);
        resultMap.put("message", message);
        return resultMap;
    }

    /**
     * 查询事务调单列表
     *
     * @param map
     * @return
     */
    @Override
    public Map<String, Object> queryAffairOrderList(Map<String, Object> map) {
        String staffId = getOperatorId();
        String queryType = MapUtils.getString(map, "queryType", "");
        Boolean isGetData = MapUtils.getBoolean(map, "isGetData", true);
        Map<String, Object> params = MapUtils.getMap(map, "parameters");
        Map<String, Object> rest = new HashMap<String, Object>();
        PageInfo pageInfo = new PageInfo(); // 分页信息
        pageInfo.setIndexSizeData(map.get("pageIndex"), map.get("pageSize"));
        String isReject = MapUtil.getString(params, "isReject", "false");
        if ("false".equals(isReject)) {
            params.put("isReject", isReject);
        }
        params.put("startRow", pageInfo.getRowStart()); // 分页开始行
        params.put("endRow", pageInfo.getRowEnd()); // 分页结束行
        params.put("staffId", staffId);
        params.put("queryType", queryType);
        int rowCount = 0;

        switch (queryType) {
            // 发起事务
            case "fqOrder":
                params.put("tacheCode", AffairDispatchOrderConstant.INITIATE_AFFAIR);
                rowCount = dispatchOrderManageDao.countStartAffairOrderList(params);
                if (rowCount != 0 && isGetData) {
                    List<Map<String, Object>> mapList = dispatchOrderManageDao.selectStartAffairOrderList(params);
                    rest.put("data", mapList);
                }
                break;
            // 草稿箱
            case "cgOrder":
                rowCount = dispatchOrderManageDao.countDraftAffairOrderList(params);
                if (rowCount != 0 && isGetData) {
                    List<Map<String, Object>> mapList = dispatchOrderManageDao.selectDraftAffairOrderList(params);
                    rest.put("data", mapList);
                }
                break;
            // 审核事务
            case "shOrder":
                params.put("tacheCode", AffairDispatchOrderConstant.AFFAIR_REVIEW);
                rowCount = dispatchOrderManageDao.countAffairOrderList(params);
                if (rowCount != 0 && isGetData) {
                    List<Map<String, Object>> mapList = dispatchOrderManageDao.selectAffairOrderList(params);
                    rest.put("data", mapList);
                }
                break;
            // 处理事务
            case "clOrder":
                params.put("tacheCode", AffairDispatchOrderConstant.AFFAIR_PROCESS);
                rowCount = dispatchOrderManageDao.countDisponeAffairOrderList(params);
                if (rowCount != 0 && isGetData) {
                    List<Map<String, Object>> mapList = dispatchOrderManageDao.selectDisponseAffairOrderList(params);
                    rest.put("data", mapList);
                }
                break;
            // 确认事务
            case "qrOrder":
                params.put("tacheCode", AffairDispatchOrderConstant.AFFAIR_CONFIROM);
                rowCount = dispatchOrderManageDao.countAffairOrderList(params);
                if (rowCount != 0 && isGetData) {
                    List<Map<String, Object>> mapList = dispatchOrderManageDao.selectAffairOrderList(params);
                    rest.put("data", mapList);
                }
                break;
            // 事务通知
            case "tzOrder":
                rowCount = dispatchOrderManageDao.countCopyAffairOrderList(params);
                if (rowCount != 0 && isGetData) {
                    List<Map<String, Object>> mapList = dispatchOrderManageDao.selectCopyAffairOrderList(params);
                    rest.put("data", mapList);
                }
                break;
            // 历史事务
            case "lsOrder":
                rowCount = dispatchOrderManageDao.countHistoryAffairOrderList(params);
                if (rowCount != 0 && isGetData) {
                    List<Map<String, Object>> mapList = dispatchOrderManageDao.selectHistoryAffairOrderList(params);
                    rest.put("data", mapList);
                }
                break;
            // 事务审查
            case "scOrder":
                params.put("tacheCode", AffairDispatchOrderConstant.AFFAIR_REVIEW_CHECK);
                rowCount = dispatchOrderManageDao.countDisponeAffairOrderList(params);
                if (rowCount != 0 && isGetData) {
                    List<Map<String, Object>> mapList = dispatchOrderManageDao.selectDisponseAffairOrderList(params);
                    rest.put("data", mapList);
                }
                break;
            default:
                break;
        }
        pageInfo.setDataCount(rowCount);
        rest.put("dataLength", rowCount);
        rest.put("flag", "1");
        rest.put("pageIndex", pageInfo.getCurrentPage());
        rest.put("rowNum", pageInfo.getPageSize());
        rest.put("total", pageInfo.getPageCount());
        return rest;
    }


    /**
     * 新增日志记录
     *
     * @param operLogMap 日志内容
     * @throws Exception 异常抛出到事务方法
     * @author PangHao
     * @date 2019/4/26 : 9:48
     */
    private void insertNoticeLog(Map<String, Object> operLogMap) throws Exception {
        // 获取人员信息
        Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(getOperatorId()));
        operLogMap.put("trackDate", new java.sql.Date(new java.util.Date().getTime()));
        operLogMap.put("createDate", new java.sql.Date(new java.util.Date().getTime()));
        operLogMap.put("trackStaffId", MapUtils.getString(operStaffInfoMap, "USER_ID"));
        operLogMap.put("trackStaffName", MapUtils.getString(operStaffInfoMap, "USER_NAME"));
        operLogMap.put("trackStaffPhone", MapUtils.getString(operStaffInfoMap, "USER_PHONE"));
        operLogMap.put("trackStaffEmail", MapUtils.getString(operStaffInfoMap, "USER_EMAIL"));
        String trackMessage = "[" + MapUtils.getString(operStaffInfoMap, "USER_NAME") + "将工单单号："
                + MapUtils.getString(operLogMap, "woOrdId") + "][" + MapUtils.getString(operLogMap, "typeName") + "]";
        operLogMap.put("trackMessage", trackMessage);
        String remark = MapUtils.getString(operLogMap, "remark");
        String trackContent = "[" + MapUtils.getString(operLogMap, "typeName") + "]"
                + remark;
        operLogMap.put("trackContent", trackContent);
        orderDealDao.insertTrackLogInfo(operLogMap);
    }

    /**
     * 批量新增抄送人信息记录
     *
     * @param noticeStaffIdList 抄送人标识稽核
     * @param affairId          事务调单ID
     * @throws Exception 异常抛出到事务方法
     * @author PangHao
     * @date 2019/4/25 : 16:26
     */
    private void batchInsertNoticeRecord(List<Map<String, Object>> noticeStaffIdList, String affairId) throws Exception {

        ArrayList<Map<String, Object>> noticeRecordList = new ArrayList<>();
        for (Map<String, Object> noticeStaff : noticeStaffIdList) {
            Map<String, Object> noticeRecord = new HashMap<>(2);
            noticeRecord.put("dealUserId", MapUtils.getString(noticeStaff, "id"));
            noticeRecord.put("noticeType", AffairDispatchOrderConstant.ORDER_NOTICE_TYPE_7);
            noticeRecord.put("noticeContent", "事务调单抄送人记录");
            noticeRecord.put("srvOrdId", affairId);
            noticeRecord.put("state", AffairDispatchOrderConstant.ORDER_NOTICE_STATE_1);
            noticeRecordList.add(noticeRecord);
        }
        // 批量新增事务调单抄送人记录
        dispatchOrderManageDao.batchInsertNoticeRecord(noticeRecordList);

    }

    /**
     * 启动业务调单主流程
     *
     * @param affairDisOrderInfo 业务调单信息
     * @return 定单ID
     * @throws Exception exception异常
     * @author PangHao
     * @date 2019/4/25 : 16:25
     */
    private String createOrder(Map<String, Object> affairDisOrderInfo) throws Exception {
        // 定单对象
        FlowOrderDTO flowOrderDTO = new FlowOrderDTO();
        // 订单规格对象
        FlowOrderSpecDTO specDTO = new FlowOrderSpecDTO();
        // 暂时写死规格配置
        specDTO.setOrderType("LOCAL_NETWORK_DISPATCH");
        specDTO.setObjType("AFFAIR_DISPATCH_ORDER");
        specDTO.setActType("AFFAIR_DISPATCH_ORDER_FLOW");
        flowOrderDTO.setOrderSpec(specDTO);
        // 标题
        flowOrderDTO.setOrderTitle(MapUtils.getString(affairDisOrderInfo, "title"));
        // 区域ID(用全国的)
        flowOrderDTO.setAreaId("350002000000000042766408");
        // 得到定单ID
        String orderId = flowOrderSpecDAO.getSeq("GOM_ORDER");
        flowOrderDTO.setOrderId(orderId);
        // 获得当前操作人信息
        String operatorId = getOperatorId();
        // 创建人
        flowOrderDTO.setCreateOp(operatorId);
        // 下一环节处理人
        flowOrderDTO.setNextDispType("260000003");
        flowOrderDTO.setNextDispId(operatorId);
        // 启动主流程
        FlowOrderDTO retDTO = flowActionHandler.createOrder(operatorId, flowOrderDTO);
        // 日志入库
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("orderId", orderId);
        logMap.put("woOrdId", "0");
        logMap.put("typeName", "发起事务调单");
        logMap.put("operType", OrderTrackOperType.OPER_TYPE_13);
        logMap.put("remark", MapUtils.getString(affairDisOrderInfo, "content"));
        insertNoticeLog(logMap);
        return retDTO.getOrderId();
    }

    /**
     * 批量启动子流程
     *
     * @param parentOrderId     父流程定单ID
     * @param disposeStaffArray 调单处理人集合
     * @throws Exception exception异常
     * @author PangHao
     * @date 2019/4/24 : 17:32
     */
    private void batchCreateSonOrder(String parentOrderId, List<String> disposeStaffArray) throws Exception {
        String operatorId = getOperatorId();
        for (String json : disposeStaffArray) {
            JSONObject disposeStaff = JSONObject.parseObject(json);
            FlowOrderDTO flowOrderDTO = new FlowOrderDTO();
            // 订单规格对象
            FlowOrderSpecDTO specDTO = new FlowOrderSpecDTO();
            // 暂时写死规格配置(子流程)
            specDTO.setOrderType("LOCAL_NETWORK_DISPATCH");
            specDTO.setObjType("LOCAL_CHILDFLOW");
            specDTO.setActType("AFFAIR_DISPATCH_ORDER_CHILDFLOW");
            flowOrderDTO.setOrderSpec(specDTO);
            // 区域ID(用全国的)
            flowOrderDTO.setAreaId("350002000000000042766408");
            // 标题
            flowOrderDTO.setOrderTitle("事务调单处理子流程");
            // 得到定单ID
            String orderId = flowOrderSpecDAO.getSeq("GOM_ORDER");
            flowOrderDTO.setOrderId(orderId);
            flowOrderDTO.setParentOrderId(parentOrderId);
            flowOrderDTO.setCreateOp(operatorId);
            flowOrderDTO.setNextDispType("260000003");
            flowOrderDTO.setNextDispId(disposeStaff.getString("id"));
            FlowOrderDTO retDTO = flowActionHandler.createOrder(operatorId, flowOrderDTO);
            // 日志入库
            // 得到该环节工单信息
            List<Map<String, Object>> workOrderList = dispatchOrderManageDao.getWorkOrder(orderId,
                    AffairDispatchOrderConstant.AFFAIR_PROCESS);
            // 该环节工单ID
            String workOrderId;
            if (null != workOrderList && !workOrderList.isEmpty()) {
                workOrderId = MapUtils.getString(workOrderList.get(0), "WOID");
            } else {
                throw new Exception("未查询到工单信息");
            }
            Map<String, Object> logMap = new HashMap<>(10);
            logMap.put("orderId", orderId);
            logMap.put("woOrdId", workOrderId);
            logMap.put("typeName", "发起事务调单子流程");
            logMap.put("operType", OrderTrackOperType.OPER_TYPE_10);
            logMap.put("remark", "发起事务调单子流程");
            insertNoticeLog(logMap);
        }
    }

    /**
     * 得到当前操作人标识
     *
     * @return 操作人标识
     * @author PangHao
     * @date 2019/4/25 : 16:32
     */
    private String getOperatorId() {
        String operatorId;
        if (ThreadLocalInfoHolder.getLoginUser() == null) {
            operatorId = "11";
        } else {
            // 获取用户id
            operatorId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        }
        return operatorId;
    }

    @Override
    public Map<String, Object> affairDispatchOrderReview(Map<String, Object> map) {
        Map<String, Object> resultMap = new HashMap<>(2);
        String resultType = "SUCCESS";
        String message;
        try {
            // 是否通过
            String staffId = getOperatorId();
            String isPass = "0";
            String woId = MapUtils.getString(map, "workOrderId");
            String id = MapUtils.getString(map, "affairId");
            String orderId = MapUtils.getString(map, "orderId");
            String remark = MapUtils.getString(map, "remark");

            String createStaff = MapUtils.getString(map, "createStaff");
            String isCheck = MapUtils.getString(map, "isCheck"); //是否审核

            if (Objects.equals(isPass, MapUtils.getString(map, "isPass"))) {
                // 通用回单方法
                complateWo(woId, null);
                // 退单日志入库
                Map<String, Object> logMap = new HashMap<>(12);
                logMap.put("orderId", orderId);
                logMap.put("woOrdId", woId);
                logMap.put("typeName", "事务调单事务审查通过");
                logMap.put("operType", OrderTrackOperType.OPER_TYPE_4);
                logMap.put("remark", remark);
                insertNoticeLog(logMap);
                message = "事务调单审查通过";

                // 得到父流程工单信息
                List<Map<String, Object>> fatherWoOrderList = dispatchOrderManageDao.getParentWorkOrderList(orderId);
                Map<String, Object> workOrder = fatherWoOrderList.get(0);
                // 如果父流程最新工单环节不是事务确认
                if (!Objects.equals(AffairDispatchOrderConstant.AFFAIR_CONFIROM,
                        MapUtils.getString(workOrder, "TACHE_CODE"))) {
                    // 得到父流程当前环节工单 ID
                    String parentOrderId = MapUtils.getString(workOrder, "ORDER_ID");
                    String parentWoOrderId = MapUtils.getString(workOrder, "WO_ID");
                    //封装处理人参数
                    ArrayList<HashMap<String, String>> operAttrs = new ArrayList<>();
                    HashMap<String, String> operAttrsMap2 = new HashMap<>(2);
                    operAttrsMap2.put("KEY", AffairDispatchOrderConstant.AFFAIR_CONFIROM + "_DISP_OBJ");
                    operAttrsMap2.put("VALUE", "260000003_J!G@F_" + createStaff);
                    operAttrs.add(operAttrsMap2);
                    if (null != isCheck && Objects.equals(AffairDispatchOrderConstant.NO_CHECK, isCheck.trim())) {
                        //如果不审核传新的线条参数
                        HashMap<String, String> operAttrsMap1 = new HashMap<>(2);
                        operAttrsMap1.put("KEY", "ifSubmitReview");
                        operAttrsMap1.put("VALUE", AffairDispatchOrderConstant.NO_CHECK);
                        operAttrs.add(operAttrsMap1);
                    }

                    // 父流程回单
                    complateWo(parentWoOrderId, operAttrs);
                    // 父流程回单日志入库
                    Map<String, Object> logMap1 = new HashMap<>();
                    logMap1.put("orderId", parentOrderId);
                    logMap1.put("woOrdId", parentWoOrderId);
                    logMap1.put("typeName", "事务调单-事务处理");
                    logMap1.put("operType", OrderTrackOperType.OPER_TYPE_4);
                    logMap1.put("remark", "事务调单事务处理通过");
                    insertNoticeLog(logMap1);
                }
                // 更改事务调单状态为事务确认
                dispatchOrderManageDao.modifyAffairDisState(id, AffairDispatchOrderConstant.AFFAIR_DIS_STATE_8,
                        getOperatorId());
            } else {
                // 审核不通过，退单到事务处理
                List<FlowRollBackReasonDTO> flowRollBackReasonDTOS = flowActionHandler.queryRollBackReasons(woId);
                logger.info("退單原因集合：" + flowRollBackReasonDTOS.toString());
                boolean isSuccess = flowActionHandler.rollBackWo(staffId, woId, flowRollBackReasonDTOS.get(0), remark);
                if (!isSuccess) {
                    throw new Exception("事务调单退单失败！");
                }
                // 事务单状态改为事务处理中
                dispatchOrderManageDao.modifyAffairDisState(id, AffairDispatchOrderConstant.AFFAIR_DIS_STATE_5,
                        staffId);
                // 退单日志入库
                Map<String, Object> logMap = new HashMap<>(12);
                logMap.put("orderId", orderId);
                logMap.put("woOrdId", woId);
                logMap.put("typeName", "事务调单事务审查驳回");
                logMap.put("operType", OrderTrackOperType.OPER_TYPE_5);
                logMap.put("remark", remark);
                insertNoticeLog(logMap);
                message = "事务调单审查驳回";
            }
            // 更新工单信息
            dispatchOrderManageDao.modifyWoOrderRemark(woId, remark);
            // 审核环节附件入库
            List fileInfos = (List) map.get("fileInfos");
            if (null != fileInfos && !fileInfos.isEmpty()) {
                // 工单id暂存为0用于保存事务调单附件，与其他环节附件区分
                dispatchOrderManageDao.batchInsertOrderFileRecord(woId, orderId, fileInfos);
            }

        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus()
                    .setRollbackOnly();
            resultType = "FAILED";
            message = "事务调单审查失败，原因：" + e.getMessage();
        }
        resultMap.put("code", resultType);
        resultMap.put("message", message);
        return resultMap;
    }

    @Override
    public List<Map<String, Object>> qryAffairDispatchOrderType() {
        return dispatchOrderManageDao.qryAffairDispatchOrderType();
    }

}
