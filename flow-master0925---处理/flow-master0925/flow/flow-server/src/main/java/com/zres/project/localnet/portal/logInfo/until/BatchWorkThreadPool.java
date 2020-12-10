package com.zres.project.localnet.portal.logInfo.until;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.util.SpringContextHolderUtil;
import com.zres.project.localnet.portal.webservice.oneDry.FeedbackInterface;
import org.apache.commons.collections.MapUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: flow
 * @description: 批量处理线程池
 * @author: CWY
 * @create: 2020-11-10 16:28
 **/
public class BatchWorkThreadPool  implements  Runnable {

    private Map<String, Object> params;

    public BatchWorkThreadPool(Map<String, Object> params) {
        this.params = params;
    }

    public void run() {
        FeedbackInterface feedbackInterface = SpringContextHolderUtil.getBean("feedbackInterface");
        OrderDealDao orderDealDao = SpringContextHolderUtil.getBean("orderDealDao"); //数据库操作-对象
        //调用一干接口
        Map<String, Object> result = new HashMap<>();
        result = feedbackInterface.postponementApply(params);
        if (!"success".equals(MapUtils.getString(result, "flag"))) {
            try {
                Thread.sleep(5000);
                Map<String, Object> submitInfo = new HashMap<>();
                String woId = MapUtils.getString(params, "woId");
                String msg = MapUtils.getString(result, "msg");
                submitInfo.put("woId", woId);
                submitInfo.put("cstOrdId", MapUtils.getString(params, "cstOrdId"));
                submitInfo.put("applyState", "290000002");
                submitInfo.put("auditOpinion", msg);
                //更改附件状态、延期申请状态
                orderDealDao.updatePostponementApply(submitInfo);
                //orderDealDao.updateFileState(woId);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }


}
