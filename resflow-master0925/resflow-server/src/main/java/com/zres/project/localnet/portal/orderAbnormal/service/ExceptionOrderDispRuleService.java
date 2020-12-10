package com.zres.project.localnet.portal.orderAbnormal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.zres.project.localnet.portal.orderAbnormal.constant.OrderAbnormalConstant;
import com.zres.project.localnet.portal.orderAbnormal.dao.ExceptionOrderDispRulesDAO;

/**
 * 异常单-追单各个环节的派发服务
 */
@Component
public class ExceptionOrderDispRuleService {

    @Autowired
    private ExceptionOrderDispRulesDAO dispRulesDAO;

    private static Logger log = LoggerFactory.getLogger(ExceptionOrderDispRuleService.class);

    /**
     * 追单派发规则
     * 获取派发对象字符串
     * 派发给原单的处理人或者签收人
     *
     * @param param 包含woPsId 追单的工单规格和定单ID
     * @return 260000003_J!G@F_11,260000003_J!G@F_12,260000003_J!G@F_13
     */
    public String getDispObj(Map<String, String> param) {
        String woPsId = MapUtils.getString(param, "WO_PS_ID");
        String orderId = MapUtils.getString(param, "ORDER_ID");

        String appendOrderState = dispRulesDAO.getAppendOrderState(orderId);
        if (OrderAbnormalConstant.EPC_STATE_AUDIT_NO.equals(appendOrderState)) {
            //先判断是否被用户驳回了，如果驳回了直接设置自动单
            return "260000003_J!G@F_-2000";
        }

        //原单的orderId
        String srcOrderId = dispRulesDAO.getSrcOrderIdFromOrdKeyInfo(orderId);

        return genDispObj(woPsId, srcOrderId);
    }

    private String genDispObj(String woPsId, String srcOrderId) {
        StringBuffer sb = new StringBuffer();
        String tacheCode = dispRulesDAO.getDispTacheCode(woPsId);
        List<String> dispCanditates = new ArrayList<>();

        switch (tacheCode) {
            case OrderAbnormalConstant.SEC_SOURCE_DISPATCH:
            case OrderAbnormalConstant.SEC_SOURCE_DISPATCH_2:
                //二干资源分配
                dispCanditates = getCldDispCanditates(tacheCode, srcOrderId);
                break;
            case OrderAbnormalConstant.TO_DATA_CREATE_AND_SCHEDULE:
            case OrderAbnormalConstant.TO_DATA_CREATE_AND_SCHEDULE_2:
                //待数据制作
            case OrderAbnormalConstant.LOCAL_NETWORK_CHECK:
                //本地网核查
                dispCanditates = getSpecDispCanditates();
                break;
            case OrderAbnormalConstant.ALL_CHECK:
                //本地专业核查
                String[] checkTacheCodes = new String[]{"OUTSIDElINE_CHECK", "DATA_CHECK", "TRANS_CHECK",
                        "ACCESS_CHECK", "OTHER_CHECK", "CHANGE_CHECK"};
                dispCanditates = getDispCanditates(null, checkTacheCodes, srcOrderId);
                break;
            case OrderAbnormalConstant.ALL_CHECK_2:
                //二干专业核查
                String[] checkTacheCodes2 = new String[]{"DATA_PROFESSIONAL_VERIFICATION", "TRANSPORT_PROFESSIONAL_CHECK",
                        "EXCHANGE_PROFESSIONAL_CHECK", "OTHER_PROFESSIONAL_CHECK"};
                dispCanditates = getDispCanditates(null, checkTacheCodes2, srcOrderId);
                break;
            default:
                //其他普通环节
                dispCanditates = getDispCanditates(woPsId, null, srcOrderId);
                break;
        }

        //拼接返回字符串
        if (!CollectionUtils.isEmpty(dispCanditates)) {
            for (String dispCanditate : dispCanditates) {
                //260000003是人员类型，_J!G@F_用于分割
                sb.append("260000003_J!G@F_").append(dispCanditate).append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        else {
            sb.append("260000003_J!G@F_-2000");
        }
        log.debug("异常单获取到的派发对象为{}", sb.toString());
        return sb.toString();
    }

    /**
     * 获取派发候选人
     * 由于多工单的存在,可能获取到原单有多个派发对象，这是同样也需要产生多个
     *
     * @param orderId 定单id
     * @param woPsId  工单规格id
     * @return
     */
    private List<String> getDispCanditates(String woPsId, String[] tacheCodes, String orderId) {
        List<String> resultLst = new ArrayList<>();
        List<Map<String, String>> dispCanditates = dispRulesDAO.getDispCanditates(woPsId, tacheCodes, orderId);
        if (!CollectionUtils.isEmpty(dispCanditates)) {
            log.debug("获取到的异常单派发人为{}", dispCanditates.toArray().toString());
            //去重 去空
            for (Map<String, String> dispCanditate : dispCanditates) {
                String dispId = MapUtils.getString(dispCanditate, "DISP_ID");
                if (!StringUtils.isEmpty(dispId) && !resultLst.contains(dispId)) {
                    resultLst.add(dispId);
                }
            }
        }
        return resultLst;
    }

    /**
     * 获取派发候选人
     * 原单环节上有可能没有签收人，而是发起子流程，要获取子流程的真正签收人
     *
     * @param orderId 原单定单id
     * @param tacheCode  环节编码
     * @return
     */
    private List<String> getCldDispCanditates(String tacheCode, String orderId) {
        List<String> resultLst = new ArrayList<>();
        List<Map<String, String>> dispCanditates = dispRulesDAO.getCldDispCanditates(tacheCode, orderId);
        log.debug("获取到的异常单派发人为{}", dispCanditates.toArray().toString());
        //去重 去空
        for (Map<String, String> dispCanditate : dispCanditates) {
            String dispId = MapUtils.getString(dispCanditate, "DISP_ID");
            if (!StringUtils.isEmpty(dispId) && !resultLst.contains(dispId)) {
                resultLst.add(dispId);
            }
        }
        return resultLst;
    }

    /**
     * 特殊的派发人。派给-2000会被监听。这里派发给-1，由系统自动回单
     *
     * @return
     */
    private List<String> getSpecDispCanditates() {
        List<String> resultLst = new ArrayList<>();
        resultLst.add(OrderAbnormalConstant.SPEC_DISP_ID);
        return resultLst;
    }


    /**
     * 异常单除追单外的派发规则
     *
     * @param param
     * @return
     */
    public String getExceptionDispObjCommon(Map<String, String> param) {
        String orderId = MapUtils.getString(param, "ORDER_ID");
        //原单的orderId
        String srcOrderId = dispRulesDAO.getSrcOrderIdFromOrdKeyInfo(orderId);

        return genExceptionDispObjCommon(srcOrderId);

    }

    /**
     * 获取异常通知派发候选人(挂起，解挂，加急，撤单)
     *
     * @param srcOrderId
     * @return
     */
    private String genExceptionDispObjCommon(String srcOrderId) {
        //这个时候如果一个订单有多个电路，那么电路调度这种父单上的环节处理人就会收到多个通知。
        //所以前台回单需要合并展示和批量处理
        StringBuffer sb = new StringBuffer();
        String[] strings = dispRulesDAO.lstOrderDealUsers(srcOrderId);
        if (!ArrayUtils.isEmpty(strings)) {
            for (String string : strings) {
                //260000003是人员类型，_J!G@F_用于分割
                sb.append("260000003_J!G@F_").append(string).append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        else {
            sb.append("260000003_J!G@F_-2000");
        }
        log.debug("异常单获取到的派发对象为{}", sb.toString());
        return sb.toString();
    }


}
