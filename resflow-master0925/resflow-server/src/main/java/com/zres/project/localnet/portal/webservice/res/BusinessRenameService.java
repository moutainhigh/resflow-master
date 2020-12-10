package com.zres.project.localnet.portal.webservice.res;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.util.XmlUtil;
import com.zres.project.localnet.portal.webservice.data.dao.InterfaceBoDao;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.zres.project.localnet.portal.webservice.interfaceJiKe.FinishOrderService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @ClassName BusinessRenameService
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/8/11 10:57
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@Service
public class BusinessRenameService  implements BusinessRenameServiceIntf{
    private static final Logger logger = Logger.getLogger(BusinessRenameService.class);

    @Autowired
    private XmlUtil xmlUtil;

    @Autowired
    private WebServiceDao wsd;

    @Autowired
    private InterfaceBoDao interfaceBoDao;

    @Autowired
    private OrderDealDao orderDealDao;
    @Override
    public Map<String, Object> custInfoRename(Map<String, Object> custInfo) {

        String url = wsd.queryUrl("finishOrder");

        // 调用rest接口，将报文发往订单中心-集客
        Map<String, Object> jkResponse = xmlUtil.sendHttpPostOrderCenter(url, "");
        return null;
    }
}
