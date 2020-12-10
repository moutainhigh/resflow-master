package com.zres.project.localnet.portal.cloudNetWork.task;

import com.zres.project.localnet.portal.cloudNetWork.dao.ReceiveOrderDao;
import com.zres.project.localnet.portal.cloudNetworkFlow.WoOrderDealServiceIntf;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * 异步起流程任务处理
 *
 * @author caomm on 2020/10/29
 */
public class StartFlowTask implements Callable {

    private static final Logger logger = LoggerFactory.getLogger(StartFlowTask.class);

    private ReceiveOrderDao receiveOrderDao;
    private int srvOrdId;
    private Map<String, Object> param;
    private WoOrderDealServiceIntf woOrderDealServiceIntf;
    public void setParam(Map<String, Object> param, ReceiveOrderDao receiveOrderDao, int srvOrdId,
                         WoOrderDealServiceIntf woOrderDealServiceIntf){
        this.param = param;
        this.srvOrdId = srvOrdId;
        this.receiveOrderDao = receiveOrderDao;
        this.woOrderDealServiceIntf = woOrderDealServiceIntf;
    }

    @Override
    public Object call() throws Exception {
        try {
            if (MapUtils.isNotEmpty(param)){
                //起流程，查询流程psId
                Map<String, Object> psMap = new HashMap<>();
                psMap.put("serviceId", MapUtils.getString(param, "serviceId"));
                psMap.put("serviceOfferId", MapUtils.getString(param, "serviceOfferId"));
                psMap.put("changeFlag", MapUtils.getString(param, "changeFlag"));
                if (MapUtils.getString(param, "cityA").equals(MapUtils.getString(param, "cityZ"))){
                    psMap.put("codeType", "cloud_flow_local");
                }else{
                    //跨域
                    psMap.put("codeType", "cloud_flow_cross");
                }
                String psId = receiveOrderDao.queryFlowPsId(psMap);
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("ordPsid", psId);
                paramMap.put("ORDER_TITLE", MapUtils.getString(param, "applyOrdId"));
                paramMap.put("ORDER_CONTENT", "起流程");
                Map<String, String> attr = new HashMap<>();
                //电路id
                attr.put("SRV_ORD_ID", String.valueOf(srvOrdId));
                paramMap.put("attr", attr);
                Map<String, Object> orderMap = woOrderDealServiceIntf.createOrderCloud(paramMap);
                //起流程成功后将orderId更新到对应的电路表中
                receiveOrderDao.updateOrderId(MapUtils.getString(orderMap,"orderId"), String.valueOf(srvOrdId));
            }
        }
        catch (Exception e) {
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>异步启云组网流程发生异常:{}", e.getMessage());
        }
        return null;
    }
}