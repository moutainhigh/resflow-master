package com.zres.project.localnet.portal.flowdealinfo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;

/**
 * @Classname CreateChildFlowDealService
 * @Description 启子流程失败报错后修改数据状态
 * @Author guanzhao
 * @Date 2020/11/26 14:22
 */
@Service
public class CreateChildFlowDealService {

    Logger logger = LoggerFactory.getLogger(CreateChildFlowDealService.class);
    @Autowired
    private OrderDealDao orderDealDao;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void updCreateChildFlowStateService(String orderId) throws Exception {
        logger.info(">>>>>>>>>>>>>>>>>启子流程失败报错后修改数据状态>>>>>>>>>>>>>>>>>>>>>>>");
        orderDealDao.updCreateChildFlowState(orderId, "10E");
    }
}
