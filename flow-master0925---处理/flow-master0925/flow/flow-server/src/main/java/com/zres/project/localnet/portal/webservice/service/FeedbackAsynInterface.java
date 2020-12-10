package com.zres.project.localnet.portal.webservice.service;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.zres.project.localnet.portal.webservice.IInterface;
import com.zres.project.localnet.portal.webservice.interfaceJiKe.FinishOrderServiceIntf;

@Repository("feedbackAsynInterface")
public class FeedbackAsynInterface implements IInterface {

    Logger logger = LoggerFactory.getLogger(FeedbackAsynInterface.class);

    @Autowired
    private FinishOrderServiceIntf finishOrderServiceIntf;

    @Override
    public void intfTuneMethod(Map<String, Object> intfMap) {
        // 休眠10s
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Map finMap = finishOrderServiceIntf.finishOrder(intfMap);
        if (!"1".equals(MapUtils.getString(finMap, "RESP_CODE"))) {
            logger.info("调用集客反馈接口异常，异常原因：" + MapUtils.getString(finMap, "RESP_DESC"));
        }
    }
}
