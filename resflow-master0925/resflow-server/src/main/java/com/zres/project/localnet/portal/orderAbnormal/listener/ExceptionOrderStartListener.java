package com.zres.project.localnet.portal.orderAbnormal.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.zres.project.localnet.portal.orderAbnormal.dao.OrderAbnormalDao;

import com.ztesoft.res.frame.flow.common.event.OrderStartEvent;
import com.ztesoft.res.frame.flow.task.service.PubService;

/**
 * 异常单子流程定单创建的监听器
 * 如果是异常单同时又派发给-2000，则将该工单设为自动单
 */
@Component
public class ExceptionOrderStartListener implements ApplicationListener<OrderStartEvent> {
    /**
     * 异常单定单类型
     */
    private static final String EXCEPTION_ORDER_TYPE = "ORDER_EXCEPTION";

    private static final String EXCEPTION_OBJ_TYPE = "LOCAL_CHILDFLOW";

    /**
     * 自动执行人id
     */
    private static final String SYS_AUTO_DISP_ID = "-2000";

    private static Logger log = LoggerFactory.getLogger(ExceptionOrderStartListener.class);


    @Autowired
    PubService pubService;

    @Autowired
    OrderAbnormalDao orderAbnormalDao;

    @Override
    public void onApplicationEvent(OrderStartEvent event) {
        //异常单子流程
        if (EXCEPTION_ORDER_TYPE.equals(event.getOrderType())
                && EXCEPTION_OBJ_TYPE.equals(event.getOrderObjType())) {
            //do nothing
        }
    }


}
