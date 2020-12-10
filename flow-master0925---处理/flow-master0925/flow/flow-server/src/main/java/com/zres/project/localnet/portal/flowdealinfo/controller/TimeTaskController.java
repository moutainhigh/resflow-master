package com.zres.project.localnet.portal.flowdealinfo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.flowdealinfo.service.CreateChildFlowService;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderSendMsgService;

@Service
public class TimeTaskController {

    Logger logger = LoggerFactory.getLogger(TimeTaskController.class);

    @Autowired
    private CreateChildFlowService createChildFlowService;
    @Autowired
    private OrderSendMsgService orderSendMsgService;

    /*
     * 启子流程
     * @author guanzhao
     * @date 2020/10/5
     *
     */
    public void createChildOrderController(){
        logger.info(">>>>>>controller>>>>>>定时任务>>>>>创建子流程>>>>>>>>>>>>>>>>>>>>>>>");
        try{
            createChildFlowService.createChildOrder();
        }catch (Exception e){
            logger.error("电路调度启子流程失败！调单发起失败：", e);
        }
    }

    /*
     * 本地发送短信
     * @author guanzhao
     * @date 2020/10/5
     *
     */
    public void scanMsgInfoAndSendController(){
        logger.info(">>>>>>controller>>>>>>定时任务>>>>>发送短信>>>>>>>>>>>>>>>>>>>>>>>");
        try{
            orderSendMsgService.scanMsgInfoAndSend();
        }catch (Exception e){
            logger.error("短信发送失败：", e);
        }
    }


}
