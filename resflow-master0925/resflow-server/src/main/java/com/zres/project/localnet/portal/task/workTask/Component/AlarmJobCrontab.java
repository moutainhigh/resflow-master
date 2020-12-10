package com.zres.project.localnet.portal.task.workTask.Component;

import com.zres.project.localnet.portal.task.workTask.dao.WorkTaskDao;
import com.zres.project.localnet.portal.webservice.sms.SendMessageService;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预警工单短信发送
 */
@Component
@Lazy(false)
public class AlarmJobCrontab {

    Logger logger = LoggerFactory.getLogger(AlarmJobCrontab.class);

    /**
     * 是否开启工单预警、超时定时任务，集群环境只设置一个服务器为true
     */
    @Value("${local.woContab.isStart:false}")
    private boolean alarmExecute;

    @Autowired
    private WorkTaskDao workTaskDao;

    @Autowired
    private SendMessageService sendMessageService;

    @Scheduled(cron = "${local.alarm.contab:0 * */2 * * ?}") // 每隔2小时执行
    public void alarmJob() throws Exception {
        if (alarmExecute) {
            logger.info("工单预警定时任务:" + getClass().getName() + "执行开始.........");
            String smsContent = "工单预警提示：【二干调度系统】收到客户【custNameChinese】的一条工单，主题为【applyOrdName】，申请单编号为【applyOrdId】即将超时,请于reqFinDateStr前反馈。";
            List<Map<String, Object>> mapsDealTotal = new ArrayList<Map<String, Object>>(); // 预警工单数据
            Map<String, Object> params = new HashMap<String, Object>();
            // params.put("limitState","84000002"); //预警状态
            // 预警(84000002)查询sql(已签收、未签收，派发类型:个人)
            List<Map<String, Object>> mapsDealUId = workTaskDao.queryWarnWoByDealUIdAndByPId(params);
            // 预警(84000002)查询sql(未签收，派发类型:岗位)
            List<Map<String, Object>> mapsPost = workTaskDao.queryWarnWoByPost(params);
            if (!CollectionUtils.isEmpty(mapsDealUId)) {
                // mapsDealUId.get(0).put("USER_NAME","zx-wu.zhaoting");
                // mapsDealTotal.add(mapsDealUId.get(0));
                mapsDealTotal.addAll(mapsDealUId);
            }
            if (!CollectionUtils.isEmpty(mapsPost)) {
                // mapsPost.get(0).put("USER_NAME","zx-wu.zhaoting,zx-wu.zhaoting");
                // mapsDealTotal.add(mapsPost.get(0));
                mapsDealTotal.addAll(mapsPost);
            }
            if (!CollectionUtils.isEmpty(mapsDealTotal)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat formatCh = new SimpleDateFormat("yyyy年MM月dd日 HH点mm分ss秒");

                for (int i = 0; i < mapsDealTotal.size(); i++) {
                    Map<String, Object> stringAlarmMap = mapsDealTotal.get(i);
                    String applyOrdId = MapUtils.getString(stringAlarmMap, "APPLY_ORD_ID");
                    try {
                        String reqFinDate = MapUtils.getString(stringAlarmMap, "REQ_FIN_DATE");
                        String applyOrdName = MapUtils.getString(stringAlarmMap, "APPLY_ORD_NAME");
                        String userName = MapUtils.getString(stringAlarmMap, "USER_NAME");
                        String dispatchOrderId = MapUtils.getString(stringAlarmMap, "DISPATCH_ORDER_ID");
                        String custNameChinese = MapUtils.getString(stringAlarmMap, "CUST_NAME_CHINESE");
                        String smsContentT = smsContent;
                        String reqFinDateCh = "";
                        if (StringUtils.hasText(reqFinDate)) {
                            Date parse24 = dateFormat.parse(reqFinDate);
                            reqFinDateCh = formatCh.format(parse24);
                        }
                        smsContentT = smsContentT.replaceAll("applyOrdName", applyOrdName)
                            .replaceAll("applyOrdId", applyOrdId).replaceAll("reqFinDateStr", reqFinDateCh)
                            .replaceAll("custNameChinese", custNameChinese);
                        Map<String, Object> interSend = new HashMap<String, Object>(); // 短信消息
                        interSend.put("userName", userName);
                        interSend.put("dispatchId", dispatchOrderId);
                        interSend.put("smsContent", smsContentT);
                        interSend.put("feedbackTime", reqFinDate);
                        sendMessageService.sendMsg(interSend);

                    } catch (Exception e) {
                        logger.info("业务申请单编号:" + applyOrdId + "发送预警短信失败:" + e.getMessage());
                    }
                }

            }
            logger.info("工单预警定时任务:" + getClass().getName() + "执行结束.........");
        }

    }

}
