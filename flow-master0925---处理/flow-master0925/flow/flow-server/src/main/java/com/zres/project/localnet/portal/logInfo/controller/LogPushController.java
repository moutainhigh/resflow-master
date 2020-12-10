package com.zres.project.localnet.portal.logInfo.controller;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class LogPushController {

    private static Logger logger = LoggerFactory.getLogger(LogPushController.class);
    public static final String RESCUE_MSG = "您的车辆当前发生故障，请确认是否进行援助";


//    @Autowired
//    Properties properties;

    @Autowired
    KafkaProducer<String, String> kafkaProducer;   //注入KafkaProducer

    public void collideReminder(String imei, Integer collideLevel) {

        if (collideLevel == 1 || collideLevel == 2) {
            String messageContent = "您的车辆当前发生" + collideLevel + "碰撞等级碰撞，请关注您的爱车状态";
            //2.向kafka推送消息
            logger.debug("kafka send message:" + messageContent + " to topic is collide_Reminder");
            kafkaProducer.send(new ProducerRecord<String, String>("collide_Reminder", messageContent));
            logger.debug("kafka send to topic is collide_Reminder success.");
            //3.向数据库中保存消息内容
            /*Message message = new Message();
            message.setImei(imei);
            message.setMessageType(5);
            message.setMessageContent(messageContent);
            message.setIsRead(0);
            message.setIsUsed(0);
            message.setIsDel(0);
            messageDao.insertMessage(message);*/
        }
        //return Response.success();
    }
}
