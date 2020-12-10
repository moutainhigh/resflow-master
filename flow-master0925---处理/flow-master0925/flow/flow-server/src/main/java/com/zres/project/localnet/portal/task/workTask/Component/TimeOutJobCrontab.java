package com.zres.project.localnet.portal.task.workTask.Component;

import java.text.SimpleDateFormat;
import java.util.*;

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

import com.zres.project.localnet.portal.task.workTask.dao.WorkTaskDao;
import com.zres.project.localnet.portal.webservice.sms.SendMessageService;

/**
 * 超时工单短信发送
 */
@Component
@Lazy(false)
public class TimeOutJobCrontab {

    Logger logger = LoggerFactory.getLogger(TimeOutJobCrontab.class);

    @Value("${local.woContab.isStart:false}")
    private boolean alarmExecute;

    @Autowired
    private WorkTaskDao workTaskDao;

    @Autowired
    private SendMessageService sendMessageService;

    @Scheduled(cron = "${local.timeout.contab:0 0 9 * * ?}") // 每天9点执行
    public void timeOutJob() throws Exception {
        if (alarmExecute) {
            logger.info("工单超时定时任务:" + getClass().getName() + "执行开始.........");
            String smsContent = "工单超时提示：【本地调度系统】收到客户【custNameChinese】的一条工单，主题为【applyOrdName】，申请单编号为【applyOrdId】已经超时timeOutHour个小时,请尽快反馈。";
            List<Map<String, Object>> mapsDealTotal = new ArrayList<Map<String, Object>>(); // 超时工单数据
            Map<String, Object> params = new HashMap<String, Object>();
            // params.put("limitState","84000003"); //超时状态
            // 查询超时数据
            List<Map<String, Object>> mapsDealUId = workTaskDao.queryTimeOutWoByDealUIdAndByPId(params);
            List<Map<String, Object>> mapsPost = workTaskDao.queryTimeOutWoByPost(params);
            if (!CollectionUtils.isEmpty(mapsDealUId)) {
                // mapsDealUId.get(0).put("USER_NAME","zx-wu.zhaoting");
                //// mapsDealTotal.add(mapsDealUId.get(0));
                mapsDealTotal.addAll(mapsDealUId);
            }
            if (!CollectionUtils.isEmpty(mapsPost)) {
                // mapsPost.get(0).put("USER_NAME","zx-wu.zhaoting,zx-wu.zhaoting");
                // mapsDealTotal.add(mapsPost.get(0));
                mapsDealTotal.addAll(mapsPost);
            }
            if (!CollectionUtils.isEmpty(mapsDealTotal)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                for (int i = 0; i < mapsDealTotal.size(); i++) {
                    Map<String, Object> stringAlarmMap = mapsDealTotal.get(i);
                    String applyOrdId = MapUtils.getString(stringAlarmMap, "APPLY_ORD_ID");
                    try {
                        String reqFinDate = MapUtils.getString(stringAlarmMap, "REQ_FIN_DATE");
                        String applyOrdName = MapUtils.getString(stringAlarmMap, "APPLY_ORD_NAME");
                        String userName = MapUtils.getString(stringAlarmMap, "USER_NAME");
                        String dispatchOrderId = MapUtils.getString(stringAlarmMap, "DISPATCH_ORDER_ID");
                        String custNameChinese = MapUtils.getString(stringAlarmMap, "CUST_NAME_CHINESE");
                        String tacheId = MapUtils.getString(stringAlarmMap, "TACHE_ID");
                        String tacheName = MapUtils.getString(stringAlarmMap, "TACHE_NAME");

                        String smsContentT = smsContent;
                        double reqFinDateHour = 0;
                        if (StringUtils.hasText(reqFinDate)) {
                            Date reqFinDateCh = dateFormat.parse(reqFinDate);
                            reqFinDateHour = (new Date().getTime() - reqFinDateCh.getTime()) / (1000 * 60 * 60);
                        }
                        smsContentT = smsContentT.replaceAll("applyOrdName", applyOrdName)
                            .replaceAll("applyOrdId", applyOrdId).replaceAll("custNameChinese", custNameChinese)
                            .replaceAll("timeOutHour", Double.toString(reqFinDateHour));
                        Map<String, Object> interSend = new HashMap<String, Object>(); // 短信消息
                        interSend.put("userName", userName);
                        interSend.put("dispatchId", dispatchOrderId);
                        interSend.put("smsContent", smsContentT);
                        interSend.put("feedbackTime", reqFinDate);
                        interSend.put("tacheId", tacheId);
                        interSend.put("tacheName", tacheName);

                        sendMessageService.sendMsg(interSend);

                    } catch (Exception e) {
                        logger.info("申请编号:" + applyOrdId + "发送超时短信失败:" + e.getMessage());
                    }
                }

            }
            logger.info("工单超时定时任务:" + getClass().getName() + "执行结束.........");
        }

    }

}
