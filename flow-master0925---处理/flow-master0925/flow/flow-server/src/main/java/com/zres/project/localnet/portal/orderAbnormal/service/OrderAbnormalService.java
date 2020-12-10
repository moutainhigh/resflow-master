package com.zres.project.localnet.portal.orderAbnormal.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.ztesoft.res.frame.core.util.ListUtil;
import com.ztesoft.res.frame.flow.common.constant.FlowCommonEnum;
import com.ztesoft.res.frame.flow.common.exception.FlowException;

import com.zres.project.localnet.portal.flowdealinfo.service.OrderSendMsgService;
import com.zres.project.localnet.portal.orderAbnormal.constant.OrderAbnormalConstant;
import com.zres.project.localnet.portal.orderAbnormal.dao.OrderAbnormalDao;
import com.zres.project.localnet.portal.webservice.flow.ExceptionFlowIntf;

import net.sf.json.JSONArray;

@Service
public class OrderAbnormalService implements OrderAbnormalServiceIntf {
    @Autowired
    private OrderAbnormalDao orderAbnormalDao;

    @Autowired
    private ExceptionOrderComonService exceptionOrderComonService;

    @Autowired
    private ExceptionFlowIntf exceptionFlowIntf;
    @Autowired
    private OrderSendMsgService orderSendMsgService;

    @Autowired
    private OrderAbnormalFeedbackService orderAbnormalFeedbackService;

    private static final Logger log = LoggerFactory.getLogger(OrderAbnormalService.class);


    /**
     * 追单信息变更
     *
     * @param param
     * @return
     */
    public Map qryOrdChgLogByCstOrdId(Map<String, Object> param) {
        Map retMap = new HashMap();
        try {
            List<Map<String, Object>> list = orderAbnormalDao.qryOrdChgLogByCstOrdId(param);
            List<Map<String, Object>> customerList = new ArrayList<>();
            List<Map<String, Object>> dispatchList = new ArrayList<>();
            List<Map<String, Object>> prodList = new ArrayList<>();
            List<Map<String, Object>> changeTimeList = new ArrayList<>();

            for (int i = 0; i < list.size(); i++) {
                String fieldType = list.get(i).get("FILED_TYPE").toString();
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
                if ("CustomerInfo".equals(fieldType)) {
                    customerList.add(list.get(i));
                }
                else if ("DispatchInfo".equals(fieldType)) {
                    dispatchList.add(list.get(i));
                }
                else if ("ProdInfo".equals(fieldType)) {
                    prodList.add(list.get(i));
                }
                else {
                    changeTimeList.add(list.get(i));
                }
            }
            retMap.put("success", true);
            retMap.put("customerList", customerList);
            retMap.put("dispatchList", dispatchList);
            retMap.put("prodList", prodList);
            retMap.put("changeTimeList", changeTimeList);
        }
        catch (Exception e) {
            log.error(e.getMessage(),e);
            retMap.put("success", false);
            retMap.put("message", e.getMessage());
        }
        return retMap;
    }

    public Map qrySrvOrdIds(String cstOrdId) {
        Map retMap = new HashMap();
        try {
            String srvOrdIds = orderAbnormalDao.qrySrvOrdIds(cstOrdId);
            retMap.put("success", true);
            retMap.put("srvOrdIds", srvOrdIds);
        }
        catch (Exception e) {
            retMap.put("success", false);
            retMap.put("message", e.getMessage());
        }
        return retMap;
    }

    /**
     * 回单
     *
     * @param param
     * @return
     */
    public Map compWo(Map param) {
        String retState = "SUCCESS";
        Map<String, String> retMap = new HashMap<>();
        String chgType = MapUtils.getString(param, "chgType");
        String operStaffId = MapUtils.getString(param, "staffId");
        //客户订单id
        String cstOrdId = MapUtils.getString(param, "cstOrdId");
        //异常单工单id
        List<String> woIds = (List<String>) MapUtils.getObject(param, "woIds");
        //异常单的定单id
        List<String> orderIds = (List<String>) MapUtils.getObject(param, "orderIds");
     /*  log.debug("异常单回单， cstOrderId = {}, woIds = {}, orderIds = {}, chgType = {}",
                cstOrdId, woIds.toString(), orderIds.toString(), chgType);*/
        try {
            //获取工单的tacheCode
            Map<String, String> gomWOInfo = orderAbnormalDao.qryGomWOInfo(woIds.get(0));
            String tacheCode = MapUtils.getString(gomWOInfo, "TACHE_CODE");
            switch (chgType) {
                case "104":
                    //追单流程回单
                    retState = compWo4Append(operStaffId, tacheCode, woIds, orderIds, cstOrdId);
                    break;
                default:
                    //其他类型的异常单，直接回单就可以了
                    String remark = chgType + "类型的异常单，用户确认收到";
                    exceptionOrderComonService.compExceptionWoCommon("", woIds, operStaffId, remark);
                    break;
            }
        }
        catch (Exception e) {
            log.error("异常单客服订单id[{}]回单出现错误", cstOrdId, e);
            throw new FlowException(FlowCommonEnum.ErrorCode.SYS_ERROR,
                    "异常单客服订单id[" + cstOrdId + "]回单出现错误,请联系管理员处理！");
        }
        if(!ListUtil.isEmpty(orderIds)){
            log.info("开始准备发送短信。。。。。。。。。。。。。。。。。。。。");
            Map<String, Object> sendMsgMap = new HashMap<String, Object>();
            sendMsgMap.put("operStaffId", operStaffId);
            sendMsgMap.put("orderIdList", orderIds);
            sendMsgMap.put("operAction", "追单");
            sendMsgMap.put("orderType", "LOCAL_EXCEPTION");
            orderSendMsgService.sendMsgBefore(sendMsgMap);
        }
        retMap.put("result", retState);
        return retMap;
    }

    /**
     * 追单流程回单
     */
    private String compWo4Append(String operStaffId, String tacheCode, List<String> woIds, List<String> orderIds,
                                 String cstOrdId) {
        String retState = "SUCCESS";

        if (OrderAbnormalConstant.CIRCUIT_DISPATCH.equals(tacheCode)) {
            //电路调度环节的回单
            retState = compWo4Disp(operStaffId, woIds, orderIds, cstOrdId);
        }
        else if (OrderAbnormalConstant.SECONDARY_SCHEDULE_2.equals(tacheCode)
                || OrderAbnormalConstant.SECONDARY_SCHEDULE.equals(tacheCode)) {
            //二干调度回单
            retState = compWo4SecondDisp(operStaffId, woIds, orderIds);
        }
        else {
            String remark = "用户收单异常单，手动回单";
            exceptionOrderComonService.compExceptionWoCommon("", woIds, operStaffId, remark);
        }
        return retState;
    }

    /**
     * 追单驳回
     *
     * @param param
     * @return
     */
    public Map appendReject(Map<String, Object> param) {
        Map retMap = new HashMap();
        List orderIds = (List) MapUtils.getObject(param, "orderIds");
        //客户订单id
        String cstOrdId = MapUtils.getString(param, "cstOrdId");

        log.info("调度订单用户追单确认, cstOrdId = {}, orderIds = {}", cstOrdId, orderIds.toString());

        String remark = "前台用户驳回";
        String state = OrderAbnormalConstant.EPC_STATE_AUDIT_NO;
        //修改异常单状态
        exceptionOrderComonService.editExceptionOrderState(orderIds, "",
                state, remark);

        //集客反馈
        Map map = orderAbnormalFeedbackService.appendRejectFeedBack(cstOrdId);
        Boolean success = MapUtils.getBoolean(map, "success");
        if (!success) {
            return map;
        }

        //回单
        compWo(param);
        retMap.put("success", true);
        return retMap;
    }

    /**
     * 追单确认
     *
     * @param param
     * @return
     */
    public Map appendConfirm(Map<String, Object> param) {
        Map retMap = new HashMap();
        List<String> orderIds = (List<String>) MapUtils.getObject(param, "orderIds");
        //客户订单id
        String cstOrdId = MapUtils.getString(param, "cstOrdId");

        log.info("调度订单用户追单确认, cstOrdId = {}, orderIds = {}", cstOrdId, orderIds.toString());

        String remark = "调度订单用户追单确认";
        String state = OrderAbnormalConstant.EPC_STATE_AUDIT_PASS;
        //修改异常单状态
        exceptionOrderComonService.editExceptionOrderState(orderIds, "",
                state, remark);

        //将追单信息更新到原单上
        exceptionFlowIntf.exceptionFlowSure(cstOrdId);

        //回单
        compWo(param);

        retMap.put("success", true);
        return retMap;
    }

    /**
     * 追单调度环节的操作方法
     * 追单没有追电路。即gom_change_order_log_s中没有记录追单。调用关闭接口
     * 追单有追电路。根据查出的电路流程，分别发起子流程
     *
     * @param operStaffId 操作人员
     * @param woIds       追单调度环节的工单id
     * @param orderIds    追单定单id
     * @param cstOrdId    客户订单id
     */
    private String compWo4Disp(String operStaffId, List<String> woIds, List<String> orderIds, String cstOrdId) {
        String ret = "SUCCESS";
        String confirmState = OrderAbnormalConstant.EPC_STATE_AUDIT_PASS;
        List<Map<String, String>> cldOrderIdAndPsCodes = orderAbnormalDao.qryCldOrderIdAndPsCode(woIds, "");
        String parentOrderId = MapUtils.getString(cldOrderIdAndPsCodes.get(0), "PARENT_ORDER_ID");

        if (StringUtils.isEmpty(parentOrderId)) {
            //如果本地网的电路调度没有parentOrderId说明不是二干来单，得还得判断异常单状态
            confirmState = isAppendConfirm(orderIds);
        }

        if (OrderAbnormalConstant.EPC_STATE_PENDING.equals(confirmState)) {
            //用户还未进行确认或者驳回
            ret = "UNCONFIRM";
        }
        else if (OrderAbnormalConstant.EPC_STATE_AUDIT_NO.equals(confirmState)) {
            String remark = "用户驳回后回单";
            exceptionOrderComonService.compExceptionWoCommon("", woIds, operStaffId,
                    remark);
        }
        else {
            //根据电路来生成
            for (Map<String, String> cldOrderIdAndPsCode : cldOrderIdAndPsCodes) {
                String srcCldOrderId = MapUtils.getString(cldOrderIdAndPsCode, "SRC_CLD_ORDER_ID");
                String woId = MapUtils.getString(cldOrderIdAndPsCode, "CHG_WO_ID");
                if (StringUtils.isEmpty(srcCldOrderId)) {
                    //原单没有子流程，说明原单还在停留在该调度调度环节。也直接回单
                    String remark = "原单没有子流程，说明原单还在停留在该调度调度环节。直接自动回单结束异常单";
                    exceptionOrderComonService.compExceptionWoCommon(woId, null, operStaffId,
                            remark);
                }
                else {
                    //根据原单子流程来生成对应的追单子流程
                    exceptionOrderComonService.genExceptionChildOrder(cldOrderIdAndPsCode, operStaffId, woId);
                }
            }
        }
        return ret;
    }

    /**
     * @return
     */
    private String compWo4SecondDisp(String operStaffId, List<String> woIds, List<String> orderIds) {
        String ret = "SUCCESS";
        String confirmState = isAppendConfirm(orderIds);

        if (OrderAbnormalConstant.EPC_STATE_PENDING.equals(confirmState)) {
            //用户还未进行确认或者驳回
            ret = "UNCONFIRM";
        }
        else if (OrderAbnormalConstant.EPC_STATE_AUDIT_NO.equals(confirmState)) {
            String remark = "用户驳回后回单";
            exceptionOrderComonService.compExceptionWoCommon("", woIds, operStaffId, remark);
        }
        else {
            String remark = "用户收单异常单，手动回单";
            exceptionOrderComonService.compExceptionWoCommon("", woIds, operStaffId, remark);

        }
        return ret;
    }


    /**
     * 判断追单是否没有改到电路信息。
     *
     * @return
     */
    private boolean isCircuitChanged(String cstOrdId, List<String> orderIds) {
        boolean isCircuitChanged = false;
        //根据客服订单id查询出是否有记录原单
        Map<String, Object> param = new HashMap<>();
        param.put("srcCstOrderId", cstOrdId);
        param.put("chgOrderIds", orderIds);
        param.put("chgType", "104");
        param.put("needSrcOrderId", "needSrcOrderId");
        List<Map<String, String>> maps = orderAbnormalDao.qryChangeOrderLog(param);
        if (!CollectionUtils.isEmpty(maps)) {
            isCircuitChanged = true;
        }
        return isCircuitChanged;
    }

    /**
     * 是否追单被驳回
     *
     * @param orderIds
     * @return
     */
    private String isAppendConfirm(List<String> orderIds) {
        //查看异常单状态是否被驳回
        Map<String, Object> param = new HashMap<>();
        param.put("chgOrderIds", orderIds);
        param.put("chgType", "104");
        List<Map<String, String>> orderLogLst = orderAbnormalDao.qryChangeOrderLog(param);
        //应该要有记录，且所有数据的状态都一样。否则直接抛错
        String state = MapUtils.getString(orderLogLst.get(0), "STATE");
        return state;
    }

    /**
     * 根据定单ID查询异常单信息
     *
     * @param param
     * @return
     */
    public List<Map<String, Object>> queryChangeOrderInfo(Map<String, Object> param) {
        List<Map<String, Object>> chgOrdInfos = orderAbnormalDao.queryChangeOrderInfo(param);
        if (CollectionUtils.isEmpty(chgOrdInfos)) {
            return chgOrdInfos;
        }

        List<String> candidateIds = new ArrayList<>();
        //查询除人员名称。因为人员视图没有索引，查出的数据量也比较少，放在这里拼接
        for (Map<String, Object> chgOrdInfo : chgOrdInfos) {
            String unconfirmUserId = MapUtils.getString(chgOrdInfo, "UNCONFIRM_USER_ID");
            String confirmUserId = MapUtils.getString(chgOrdInfo, "CONFIRM_USER_ID");

            if (!StringUtils.isEmpty(unconfirmUserId)) {
                String[] s = unconfirmUserId.split(",");
                List<String> strings = Arrays.asList(s);
                candidateIds.addAll(strings);
            }

            if (!StringUtils.isEmpty(confirmUserId)) {
                String[] s = confirmUserId.split(",");
                List<String> strings = Arrays.asList(s);
                candidateIds.addAll(strings);
            }
        }

        List<Map<String, String>> userLsts = orderAbnormalDao.lstGomUserS(candidateIds);

        Map<String, String> users = new HashMap<>();
        for (Map<String, String> userLst : userLsts) {
            String userId = MapUtils.getString(userLst, "USER_ID");
            String userName = MapUtils.getString(userLst, "USER_REAL_NAME");
            users.put(userId, userName);
        }

        for (Map<String, Object> chgOrdInfo : chgOrdInfos) {
            String unconfirmUserId = MapUtils.getString(chgOrdInfo, "UNCONFIRM_USER_ID");
            String confirmUserId = MapUtils.getString(chgOrdInfo, "CONFIRM_USER_ID");
            String tacheName = MapUtils.getString(chgOrdInfo, "TACHE_NAME");

            String unconfirmUserName = genUserNames(unconfirmUserId, users);
            String confirmUserName = genUserNames(confirmUserId, users);

            chgOrdInfo.put("UNCONFIRM_USER_NAME", unconfirmUserName);
            chgOrdInfo.put("CONFIRM_USER_NAME", confirmUserName);

            if (StringUtils.isEmpty(tacheName)) {
                chgOrdInfo.put("TACHE_NAME", "已确认");
            }

        }
        return chgOrdInfos;
    }

    /**
     * 获取资源修改页面的入参
     *
     * @param param
     * @return
     */
    @Override
    public Map getResUrlParam(Map<String, Object> param) {
        log.debug("获取资源配置页面的入参，{}", param.toString());
        List<Map<String, Object>> resUrlParam = orderAbnormalDao.getResUrlParam(param);
        log.debug("resUrlParam = {}",resUrlParam.toString());

        Map result = new HashMap();
        if (!CollectionUtils.isEmpty(resUrlParam)) {
            result = resUrlParam.get(0);
        }
        return result;
    }

    @Override
    public List<String> getPrivVersionState(Map<String, Object> param) {
        Set<String> version = new HashSet<>();
        List<Map<String, String>> maps = orderAbnormalDao.qryChangeOrderLog(param);
        for (Map<String, String> map : maps) {
            String state = MapUtils.getString(map, "STATE");
            String ver = MapUtils.getString(map, "CHG_VERSION");
            if (OrderAbnormalConstant.EPC_STATE_PENDING.equals(state)) {
                version.add(ver);
            }
        }
        List<String> list = new ArrayList<>();
        list.addAll(version);
        return list;
    }

    private String genUserNames(String userIdsStr, Map<String, String> userMap) {
        String ret = "";
        StringBuffer userNameSb = new StringBuffer();
        if (!StringUtils.isEmpty(userIdsStr)) {
            String[] sArr = userIdsStr.split(",");
            for (String s : sArr) {
                if (!"-2000".equals(s)) {
                    userNameSb.append(StringUtils.defaultString(userMap.get(s))).append(",");
                }
            }
            if (userNameSb.length() > 0) {
                userNameSb.deleteCharAt(userNameSb.length() - 1);
            }
            ret = userNameSb.toString();
        }

        return ret;
    }
}
