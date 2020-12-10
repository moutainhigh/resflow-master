package com.zres.project.localnet.portal.flowdealinfo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.flowdealinfo.service.OrderSendMsgService;

@Service
public class TimeTaskController{

    Logger logger = LoggerFactory.getLogger(TimeTaskController.class);

    @Autowired
    private OrderSendMsgService orderSendMsgService;

    /*
     * 二干发送短信
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
