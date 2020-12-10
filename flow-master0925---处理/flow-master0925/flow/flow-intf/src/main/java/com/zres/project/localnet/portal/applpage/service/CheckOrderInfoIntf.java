package com.zres.project.localnet.portal.applpage.service;

import java.util.Map;

/**
 * Created by tang.huili on 2019/2/20.
 * 校验申请单内容
 */
public interface CheckOrderInfoIntf {
    /**
     * 校验电路编号
     * @param params
     * @return
     */
    public Map<String, Object> checkCircuitCode(Map<String, Object> params);
    /*
     * 校验业务订单号
     * @author ren.jiahang
     * @date 2019/4/3 17:22
     * @param params
     * @return boolean
     */
    public Map<String, Object> checkTradeId(Map<String, Object> params);

    public String querySrvOrderState(Map<String, Object> params);
    //根据流程实例查询是否拆机 by ren.jiahang at 20190625
    String queryIsTear (Map<String, Object> params);

    //根据流程实例,产品类型和动作判断是否有在途单  by ren.jiahang at 20190625
    String queryisOnWay(Map<String, Object> params);

}
