package com.zres.project.localnet.portal.listener.service;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderQrySecondaryDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.listener.util.EnmuValueUtil;
import com.zres.project.localnet.portal.resourceInitiate.data.dao.ResourceInitiateDao;
import com.zres.project.localnet.portal.util.OrderTrackOperType;
import com.ztesoft.res.frame.core.util.MapUtil;
import com.ztesoft.res.frame.flow.common.dto.FlowWoDTO;
import com.ztesoft.res.frame.flow.common.event.OrderFinishedEvent;
import com.ztesoft.res.frame.flow.common.intf.FlowAction;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OrderFinishedEventListener implements ApplicationListener<OrderFinishedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(OrderFinishedEventListener.class);

    @Autowired
    private FlowAction flowAction;

    @Autowired
    private OrderDealDao orderDealDao;

    @Autowired
    private OrderQrySecondaryDao orderQrySecondaryDao;

    @Autowired
    private ResourceInitiateDao resourceInitiateDao;

    @Override
    public void onApplicationEvent(OrderFinishedEvent orderFinishedEvent) {

        //监听本地网调度流程
        if (EnmuValueUtil.LOCAL_NETWORK_DISPATCH.equals(orderFinishedEvent.getOrderType())){
            // 监听主订单结束，回单所有子流程
            String orderId = orderFinishedEvent.getOrderId();
            Map<String, Object> paramsMap = new HashMap<String, Object>();
            paramsMap.put("orderId", orderId);
            if (!EnmuValueUtil.LOCAL_RES_CHECK.equals(orderFinishedEvent.getOrderObjType())){
                /**
                 * 主流程结束回单子流程
                 * 二干下发的拆机流程不用回单所有子流程，等二干起租通知时回单所有子流程
                 */
                Map<String, Object> ifFromSecondaryMap = orderQrySecondaryDao.qryIfFromSecondary(orderId);
                if (MapUtils.isNotEmpty(ifFromSecondaryMap)){
                    Map<String, Object> map = orderDealDao.qryCstOrderDataFromSec(orderId);
                    if("102".equals(MapUtils.getString(map, "ACTIVE_TYPE"))){
                        return;
                    }
                }
                paramsMap.put("woState", OrderTrackOperType.WO_ORDER_STATE_2);
                List<Map<String, Object>> childFlowDataList = orderDealDao.getAllChildFlowData(paramsMap);
                for (int i = 0; i < childFlowDataList.size(); i++) {
                    FlowWoDTO woChildDTO = new FlowWoDTO();
                    woChildDTO.setWoId(MapUtils.getString(childFlowDataList.get(i), "WOID"));
                    flowAction.complateWo("-2000", woChildDTO);
                }
            }
            //资源补录
            if (EnmuValueUtil.LOCAL_RESOURCE_SUPPLEMENT_FLOW.equals(orderFinishedEvent.getOrderActType())){
                resourceInitiateDao.updateResSupOrderState(orderFinishedEvent.getOrderId());
            }
        }

        //监听二干调度流程
        if (EnmuValueUtil.SECONDARY_TRUNK_DISPATCH.equals(orderFinishedEvent.getOrderType())){
            if (EnmuValueUtil.SECONDARY_RESOURCE_SUPPLEMENT_FLOW.equals(orderFinishedEvent.getOrderActType())){
                resourceInitiateDao.updateResSupOrderState(orderFinishedEvent.getOrderId());
            }
        }

    }
}
