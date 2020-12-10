package com.zres.project.localnet.portal.logInfo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.logInfo.entry.ToKafkaTacheLog;

import com.ztesoft.res.frame.flow.task.service.SysParams;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.serializer.ValueFilter;

@Service
public class LogPushService {

    private static Logger logger = LoggerFactory.getLogger(LogPushService.class);

    KafkaProducer<String, String> kafkaProducer;
    public static String servers = "";
    public static String topic = "";

    public void init(String servers){
        Properties props = new Properties();
        props.put("bootstrap.servers", servers );//xxx服务器ip
        props.put("acks", "all");//所有follower都响应了才认为消息提交成功，即"committed"
        props.put("retries", 0);//retries = MAX 无限重试，直到你意识到出现了问题:)
        props.put("batch.size", 16384);//producer将试图批处理消息记录，以减少请求次数.默认的批量处理消息字节数
        //batch.size当批量的数据大小达到设定值后，就会立即发送，不顾下面的linger.ms
        props.put("linger.ms", 1);//延迟1ms发送，这项设置将通过增加小的延迟来完成--即，不是立即发送一条记录，producer将会等待给定的延迟时间以允许其他消息记录发送，这些消息记录可以批量处理
        props.put("buffer.memory", 33554432);//producer可以用来缓存数据的内存大小。
        /*props.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");*/
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());
        kafkaProducer = new KafkaProducer<>(props);
    }

    public void pushLogKafka(ToKafkaTacheLog toKafkaTacheLog) {
        List<ToKafkaTacheLog> toKafkaTacheLogList = new ArrayList<>();
        toKafkaTacheLogList.add(toKafkaTacheLog);
        //String toKafkaTacheLogStr = JSONArray.toJSONString(toKafkaTacheLogList, SerializerFeature.WriteMapNullValue);
        String toKafkaTacheLogStr = JSONArray.toJSONString(toKafkaTacheLogList, (ValueFilter) (object, name, value) -> {
            if(value == null){
                return "";
            }
            return value;
        });
        try {
            topic = SysParams.getIns().find("KAFKA_TOPIC");
            servers = SysParams.getIns().find("KAFKA_SERVERS");
            this.init(servers);
            logger.debug("推送kafka：：" + toKafkaTacheLogStr + ".................");
            kafkaProducer.send(new ProducerRecord<>(topic, toKafkaTacheLogStr));
            logger.debug("推送kafka send to topic is TOPIC_DISPATCH_PRO_OSS2 success.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            kafkaProducer.close();
        }
    }

    /*public static void main(String[] args) {
        LogPushService logPushService = new LogPushService();
        logPushService.pushLogKafka();
    }*/

}
