package com.zres.project.localnet.portal.webservice.sms;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.flowdealinfo.service.OrderSendMsgService;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;

/**
 * Created by jiangdebing on 2019/4/10.
 */
@Service
public class SendMessageService {

    Logger logger = LoggerFactory.getLogger(SendMessageService.class);

    @Autowired
    private WebServiceDao wsd;

    /**
     * 短信发送
     *
     * @param sendMap sendMap包含的key userName：用户名字符串（多用户名间用英文逗号隔开） tacheId：环节ID tacheName：环节名称 dispatchId：调单ID
     *            smsContent：短信内容 feedbackTime：反馈时间（此环节的要求完成的时间）
     * @throws SQLException
     */
    public void sendMsg(Map<String, Object> sendMap) {
        logger.info(">>>>>>>>>>>>>>>调用短信接口>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Map<String, Object> interflog = new HashMap<String, Object>(); // 记录接口日志
        SimpleDateFormat dfs = new SimpleDateFormat("yyyyMMddHHmmss");
        String msgIdKey = dfs.format(new Date());
        String returnValue = "";
        int logId = wsd.querySequence("SEQ_GOM_BDW_INTERF_LOG_INFO.NEXTVAL"); // 获取接口日志信息表序列
        interflog.put("ID", logId);
        String userName = sendMap.get("userName") + "";
        if (!"".equals(userName) && userName != null && !"null".equals(userName)) {
            String SMS_ADDRESS = wsd.queryUrl("SMS");
            String url = SMS_ADDRESS + msgIdKey;
            interflog.put("URL", SMS_ADDRESS);
            interflog.put("RETURNCONTENT", userName);
            interflog.put("INTERFNAME", "发送短信通知");
            interflog.put("ORDERNO", sendMap.get("applyOrdId"));
            try {
                String[] msgAddress = userName.split(",");
                if (msgAddress != null) {
                    for (int i = 0; i < msgAddress.length; i++) {
                        sendMap.put("msgAddress",msgAddress[i]);
                        String param = smsJson(sendMap, msgIdKey);
                        interflog.put("CONTENT", param);
                        callSendMsgInterface(url, param); // 暂时屏蔽
                    }
                }
                interflog.put("REMARK", "向门户(沃运维)发送成功");
                wsd.insertInterfLogS(interflog); // 保存日志报文 暂时屏蔽
            } catch (ParseException e) {
                interflog.put("REMARK", "发送失败拼短信内容报错");
                wsd.insertInterfLogS(interflog); // 保存日志报文
            } catch (IOException e) { // 暂时屏蔽
                interflog.put("REMARK", "发送失败调取接口未成功");
                wsd.insertInterfLogS(interflog); // 保存日志报文
            }
        }
    }

    public void callSendMsgInterface(String url, String json) throws IOException {
        URL httpUrl = new URL(url);
        HttpURLConnection httpConn = (HttpURLConnection) httpUrl.openConnection();
        byte[] data = json.getBytes();
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);
        httpConn.setUseCaches(false);
        httpConn.setRequestProperty("Content-type", wsd.queryUrl("SMS-Content-type"));
        httpConn.setRequestMethod("POST");

        OutputStream os = httpConn.getOutputStream();
        os.write(data);
        os.flush();
        os.close();

        httpConn.getInputStream();
    }

    public String smsJson(Map<String, Object> smsMap, String msgIdKey) throws ParseException {
        StringBuffer smsMsg = new StringBuffer();
        smsMsg.append("{\"orgSystem\":\"oss20\",\"appId\":\"IOMPROJ\",");// 模块标识，电路调度为"IOMPROJ"
        smsMsg.append("\"msgKey\":" + msgIdKey + ",");
        smsMsg.append("\"msgAddress\":\"" + smsMap.get("msgAddress") + "\",");
        smsMsg.append("\"msgType\":\"PERSON_SETTINGS\",\"msgLevel\":\"MINOR\",");
        smsMsg.append("\"msgText\":\"" + smsMap.get("smsContent") + "\"}");
        return smsMsg.toString();
    }
}
