package com.zres.project.localnet.portal.orderAbnormal.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.zres.project.localnet.portal.orderAbnormal.constant.OrderAbnormalConstant;
import com.zres.project.localnet.portal.orderAbnormal.dao.OrderAbnormalDao;
import com.zres.project.localnet.portal.orderAbnormal.service.ExceptionOrderComonService;

import com.ztesoft.res.frame.flow.common.event.WoCreatedEvent;
import com.ztesoft.res.frame.flow.task.service.PubService;
import com.ztesoft.res.frame.flow.task.service.PubVal;

/**
 * 异常单工单创建的监听器
 * 如果是异常单同时又派发给-2000，则将该工单设为自动单
 */
@Component
public class ExceptionOrderListener implements ApplicationListener<WoCreatedEvent> {

    private static Logger log = LoggerFactory.getLogger(ExceptionOrderListener.class);

    @Autowired
    private PubService pubService;

    @Autowired
    private OrderAbnormalDao orderAbnormalDao;

    @Autowired
    private ExceptionOrderComonService exceptionOrderComonService;

   /* @Autowired
    private OrderDealService orderDealService;
*/

    @Override
    public void onApplicationEvent(WoCreatedEvent event) {
        //异常单
        if (OrderAbnormalConstant.EXCEPTION_ORDER_TYPE.equals(event.getOrderType())) {
            setAutoWoWhenDispToSys(event.getWoId(), event.getDispObjId());
            //生成子流程
            genCldOrder(event);

        }

        //
    }


    /**
     * 派发给-2000时设置自动单
     *
     * @param woId
     * @param dispObjId
     * @return
     */
    private boolean setAutoWoWhenDispToSys(String woId, String dispObjId) {
        boolean result = false;
        if (OrderAbnormalConstant.SYS_AUTO_DISP_ID.equals(dispObjId)) {
            //转自动
            log.debug("异常单{}的派发人为-2000，工单设置为自动单", woId);
            pubService.createAsynchronousServiceEvent(
                    PubVal.TASK_TYPE_WO, woId, null, false, PubVal.AUTO_EXECUTE_NULL_SERVICE);
            result = true;
        }
        return result;
    }


    /**
     * 二干电路。待数据制作和本地调度生成子流程
     *
     * @param event
     */
    private void genCldOrder(WoCreatedEvent event) {
        String orderIdP = event.getOrderId();
        String tacheCode = event.getTacheCode();
        String woId = event.getWoId();
        List<String> orderList = Lists.newArrayList();
        orderList.add(orderIdP);
        if (OrderAbnormalConstant.TO_DATA_CREATE_AND_SCHEDULE.equals(tacheCode)
                || OrderAbnormalConstant.TO_DATA_CREATE_AND_SCHEDULE_2.equals(tacheCode)) {
            //待数据制作和本地调度到单启数据制作和本地调度的子流程
            List<Map<String, String>> cldOrderIdAndPsCodes = new ArrayList<>();
            cldOrderIdAndPsCodes.addAll(orderAbnormalDao.qryCldOrderIdAndPsCode(null, woId));
            cldOrderIdAndPsCodes.addAll(orderAbnormalDao.qryLocalOrderIdAndPsCodeErGan(null, woId));

            //根据电路来生成
            for (Map<String, String> cldOrderIdAndPsCode : cldOrderIdAndPsCodes) {
                //String orderType = MapUtils.getString(cldOrderIdAndPsCode, "ORDER_TYPE");
                String objType = MapUtils.getString(cldOrderIdAndPsCode, "OBJ_TYPE");
                String actType = MapUtils.getString(cldOrderIdAndPsCode, "ACT_TYPE");

                if (OrderAbnormalConstant.MAIN_DISPATCH_CUST_CLD.equals(objType)
                        && OrderAbnormalConstant.SOURCE_DISPATCH.equals(actType)) {
                    //跳过资源分配子流程
                    continue;
                }
                //只取数据制作和本地调度流程
                String srcCldOrderId = MapUtils.getString(cldOrderIdAndPsCode, "SRC_CLD_ORDER_ID");
                String chgWoId = MapUtils.getString(cldOrderIdAndPsCode, "CHG_WO_ID");
                if (StringUtils.isEmpty(srcCldOrderId)) {
                    log.debug("异常单{}待数据制作和本地调度没有子流程，工单设置为自动单", chgWoId);
                    pubService.createAsynchronousServiceEvent(
                            PubVal.TASK_TYPE_WO, chgWoId, null, false, PubVal.AUTO_EXECUTE_NULL_SERVICE);
                    break;
                }
                //根据原单子流程来生成对应的追单子流程
                String orderIds = exceptionOrderComonService.genExceptionChildOrder(cldOrderIdAndPsCode, "-1", chgWoId);
                orderList.add(orderIds);

            }
        }
        else if (OrderAbnormalConstant.LOCAL_NETWORK_CHECK.equals(tacheCode)) {
            //本地网核查
            List<Map<String, String>> cldOrderIdAndPsCodes = orderAbnormalDao.qryLocalOrderIdAndPsCodeErGan(null, woId);
            for (Map<String, String> cldOrderIdAndPsCode : cldOrderIdAndPsCodes) {
                String srcCldOrderId = MapUtils.getString(cldOrderIdAndPsCode, "SRC_CLD_ORDER_ID");
                String chgWoId = MapUtils.getString(cldOrderIdAndPsCode, "CHG_WO_ID");
                if (StringUtils.isEmpty(srcCldOrderId)) {
                    log.debug("异常单{}本地核查没有子流程，工单设置为自动单", chgWoId);
                    pubService.createAsynchronousServiceEvent(
                            PubVal.TASK_TYPE_WO, chgWoId, null, false, PubVal.AUTO_EXECUTE_NULL_SERVICE);
                    break;
                }
                //根据原单子流程来生成对应的追单子流程
                String orderIds = exceptionOrderComonService.genExceptionChildOrder(cldOrderIdAndPsCode, "-1", chgWoId);
                orderList.add(orderIds);
            }

        }
        //orderDealService.qryUserObjByExpWoIdsSendMsg(orderList);
    }


}
