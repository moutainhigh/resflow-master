package com.zres.project.localnet.portal.cloudNetworkFlow.service;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.cloudNetworkFlow.CloudListenerOrderServiceIntf;
import com.zres.project.localnet.portal.cloudNetworkFlow.entry.CloudTacheWoOrderListener;
import com.zres.project.localnet.portal.common.ToOrderDealTacheWoOrderIntf;
import com.zres.project.localnet.portal.common.dao.CommonDealDao;
import com.zres.project.localnet.portal.util.SpringContextHolderUtil;

import com.ztesoft.res.frame.flow.xpdl.model.TacheDto;

@Service
public class CloudListenerOrderService implements CloudListenerOrderServiceIntf {

    Logger logger = LoggerFactory.getLogger(CloudListenerOrderService.class);

    @Autowired
    private CommonDealDao commonDealDao;

    @Override
    public void tacheToWoOrder(Map<String, Object> toOrderMap) {
        try {
            int tacheId = MapUtils.getIntValue(toOrderMap, "tacheId");
            TacheDto tacheDto = commonDealDao.qryTacheDto(tacheId);
            CloudTacheWoOrderListener cloudTacheWoOrderListener = new CloudTacheWoOrderListener(tacheDto, toOrderMap);
            String beanName = cloudTacheWoOrderListener.getBeanNameByTacheCode();
            ToOrderDealTacheWoOrderIntf toOrderDealTacheWoOrder = SpringContextHolderUtil.getBean(beanName);
            toOrderDealTacheWoOrder.toOrderTacheDoSomething(toOrderMap);
//            DealTacheWoOrderIntf dealTacheWoOrder = SpringContextHolderUtil.getBean(beanName);
//            dealTacheWoOrder.tacheDoSomething(toOrderMap);
        }
        catch (Exception e) {
            logger.info("--------环节id：" + MapUtils.getIntValue(toOrderMap, "tacheId") + "监听报错------");
            throw new RuntimeException("环节id：" + MapUtils.getIntValue(toOrderMap, "tacheId") + "监听报错:" + e);
        }
    }

}
