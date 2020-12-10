package com.zres.project.localnet.portal.applpage.service;

import java.util.List;


/**
 * @author :ren.jiahang
 * @date:2018/12/22@time:15:12
 */
public interface DelOrderInfoIntf {
    int delCustomerInfo(String cstId);
    int delOrderInfoBySrvId(List<String> srvId);
    int delOrdAttrInfoBySrvId(List<String> srvId);

    String  draftDeleteByCustID(String CustID);
}
