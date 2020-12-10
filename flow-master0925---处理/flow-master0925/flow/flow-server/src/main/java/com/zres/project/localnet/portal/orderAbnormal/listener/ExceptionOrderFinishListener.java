package com.zres.project.localnet.portal.orderAbnormal.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.zres.project.localnet.portal.orderAbnormal.constant.OrderAbnormalConstant;
import com.zres.project.localnet.portal.orderAbnormal.dao.OrderAbnormalDao;
import com.zres.project.localnet.portal.orderAbnormal.service.ExceptionOrderComonService;
import com.zres.project.localnet.portal.webservice.flow.ExceptionFlowService;

import com.ztesoft.res.frame.flow.common.event.OrderFinishedEvent;
import com.ztesoft.res.frame.flow.task.service.PubVal;

/**
 * 异常单子流程定单结束的监听器
 */
@Component
public class ExceptionOrderFinishListener implements ApplicationListener<OrderFinishedEvent> {

    private static Logger log = LoggerFactory.getLogger(ExceptionOrderFinishListener.class);


    @Autowired
    private OrderAbnormalDao orderAbnormalDao;

    @Autowired
    private ExceptionOrderComonService exceptionOrderComonService;

    @Autowired
    private ExceptionFlowService exceptionFlowService;

    @Override
    public void onApplicationEvent(OrderFinishedEvent event) {
        //只监听异常单
        if (!OrderAbnormalConstant.EXCEPTION_ORDER_TYPE.equals(event.getOrderType())) {
            return;
        }

        Map<String, String> orderInfo = orderAbnormalDao.qryOrderInfo(event.getOrderId());
        String pOrder = MapUtils.getString(orderInfo, "PARENT_ORDER_ID");
        //追单子流程，有可能是二干发本地的
        if (!StringUtils.isEmpty(pOrder)) {
            //子流程都走完之后将父流程回单
            List<Map<String, String>> siblingOrders = orderAbnormalDao.qrySiblingOrder(event.getOrderId(), PubVal.ORD_STA_STAFLW);
            if (CollectionUtils.isEmpty(siblingOrders)) {
                String orderId = event.getOrderId();
                compParentWo(orderId);
            }
        }

        //异常单结束
        else {

            //追单的流程结束要将原单解挂。这里只找追单父单
            if (!OrderAbnormalConstant.EXCEPTION_ORDER_OBJ.equals(event.getOrderObjType())) {


                /* 追单状态还是待处理中的则说明调度环节都没有签收人。
                 * 这张追单该由系统直接追单确认和修改状态。
                 * 这个操作要早于其他修改状态的方法
                 * */
                confirmExceptionInfo(event.getOrderId());


            }

            /*
             * 追单：
             * 审核通过 改成 审核通过结束
             * 审核不通过 改成 审核不通过结束
             * 其他不需要审核的：
             * 待处理 改成 仅保存记录
             * */
            exceptionOrderComonService.editExceptionOrderState(null, event.getOrderId(),
                    "", "异常单结束,状态更改");
        }
    }


    /**
     * 将父单走下去！！
     *
     * @param orderId
     */
    private void compParentWo(String orderId) {
        Map<String, String> wrkWoInfo = orderAbnormalDao.qryParentWorkingWoId(orderId, "290000110");
        if (CollectionUtils.isEmpty(wrkWoInfo)) {
            wrkWoInfo = orderAbnormalDao.qryParentWorkingWoId(orderId, "290000002");
        }

        //修改工单状态为290000002
        //将异常单电路调度环节工单状态改变
        String operStaffId = MapUtils.getString(wrkWoInfo, "DISP_OBJ_ID");
        String woId = MapUtils.getString(wrkWoInfo, "WO_ID");
        String woState = MapUtils.getString(wrkWoInfo, "WO_STATE");
        log.debug("所有子流程都走完了，开始将父单的工作环节走下去。woId={}", woId);
        if (!"290000002".equals(woState)) {
            //更新父单状态
            exceptionOrderComonService.updateExceptionWoStateCommon(woId, null, operStaffId, "290000002");
        }

        //回单
        exceptionOrderComonService.compExceptionWoCommon(woId, null, operStaffId,
                "ExceptionOrderFinishLister子流程全部执行完成，自动回单");
    }


    /**
     * 调单环节没有签收人，自动追单确认
     *
     * @param chgOrderId
     */
    private void confirmExceptionInfo(String chgOrderId) {

        //获取客户定单id
        Map<String, Object> param = new HashMap<>();
        param.put("chgOrderId", chgOrderId);
        List<Map<String, String>> changeOrderLog = orderAbnormalDao.qryChangeOrderLog(param);


        //不能直接更新。如果用户只签收了部分电路。其中还驳回了，那就尴尬了。
        //所以这边还得判断该客户定单下是不是都没有被签收。即该客户订单下所有电路调度派发人都是-2000

        //肯定要有数据，没有数据的话直接报错
        String srcCstOrderId = MapUtils.getString(changeOrderLog.get(0), "SRC_CST_ORDER_ID");
        String version = MapUtils.getString(changeOrderLog.get(0), "CHG_VERSION");


        isAllDispatchToSys(version, srcCstOrderId);
    }

    private boolean isAllDispatchToSys(String chgVersion, String cstOrderId) {
        boolean isAllDispatchToSys = true;

        //调度环节tacheCode
        List<String> tacheCodes = new ArrayList<>();
        tacheCodes.add(OrderAbnormalConstant.CIRCUIT_DISPATCH);
        tacheCodes.add(OrderAbnormalConstant.SECONDARY_SCHEDULE);
        tacheCodes.add(OrderAbnormalConstant.SECONDARY_SCHEDULE_2);

        //非派发给-2000的单子
        List<Map<String, String>> maps = orderAbnormalDao.countUnDispatchToSysExceptionOrd(tacheCodes, chgVersion, "104", cstOrderId);
        for (Map<String, String> map : maps) {
            String dispObjId = MapUtils.getString(map, "DISP_OBJ_ID");
            String orderState = MapUtils.getString(map, "ORDER_STATE");
            if (!OrderAbnormalConstant.SYS_AUTO_DISP_ID.equals(dispObjId)) {
                isAllDispatchToSys = false;
                break;
            }
            if (!"200000004".equals(orderState)) {
                isAllDispatchToSys = false;
            }
        }

        if (isAllDispatchToSys) {
            exceptionFlowService.exceptionFlowSure(cstOrderId);
        }

        return isAllDispatchToSys;
    }
}
