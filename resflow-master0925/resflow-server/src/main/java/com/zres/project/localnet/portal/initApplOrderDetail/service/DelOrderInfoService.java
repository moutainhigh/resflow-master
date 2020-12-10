package com.zres.project.localnet.portal.initApplOrderDetail.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.zres.project.localnet.portal.applpage.service.DelOrderInfoIntf;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.initApplOrderDetail.dao.DelOrderInfoDao;
import com.zres.project.localnet.portal.initApplOrderDetail.dao.EditDraftDao;
import com.zres.project.localnet.portal.webservice.res.BusinessRollbackServiceIntf;

/**
 * @author :ren.jiahang
 * @date:2018/12/22@time:15:12
 */
@Repository

public class DelOrderInfoService implements DelOrderInfoIntf {
    Logger logger = LoggerFactory.getLogger(InsertOrderInfoService.class);
    @Autowired
    private DelOrderInfoDao delOrderInfoDao;
    @Autowired
    EditDraftDao editDraftDao;
    @Autowired
    OrderDealDao orderDealDao;
    @Autowired
    BusinessRollbackServiceIntf businessRollbackServiceIntf;

    /**
     * 按客户id删除客户信息
     *
     * @param cstId
     * @return int
     * @author ren.jiahang
     * @date 2019/1/5 12:57
     */
    public int delCustomerInfo(String cstId) {
        return delOrderInfoDao.delCustomerInfo(cstId);
    }

    /**
     * 按订单id删除订单信息
     *
     * @param srvId
     * @return int
     * @author ren.jiahang
     * @date 2019/1/5 12:58
     */
    public int delOrderInfoBySrvId(List<String> srvId) {
        return delOrderInfoDao.delOrderInfoBySrvId(srvId);
    }

    /**
     * 按订单id删除电路信息
     *
     * @param srvId
     * @return int
     * @author ren.jiahang
     * @date 2019/1/5 12:58
     */
    public int delOrdAttrInfoBySrvId(List<String> srvId) {
        return delOrderInfoDao.delOrdAttrInfoBySrvId(srvId);
    }

    /**
     * 删除草稿单
     *
     * @param CustID
     * @return
     */
    public String draftDeleteByCustID(String CustID) {
        String result = "success";
        //根据客户ID 查询业务订单ID
        List<String> srvOrdIdList = editDraftDao.querySrvOrdIdByCustId(CustID, "10C");
        if (!srvOrdIdList.isEmpty()) {
            try {
                /**
                 * 如果是退单过来的单子，草稿箱删除要回滚实例
                 * 1,先查询是否调用过创建实例接口
                 * 2，再查询是否成功调用多回滚接口
                 */
                for (int i = 0; i < srvOrdIdList.size(); i++) {
                    Map<String, Object> param = new HashMap<String, Object>();
                    // 调用资源回滚接口
                    param.put("srvOrdId", srvOrdIdList.get(i));// gom_bdw_srv_ord_info.srv_ord_id
                    param.put("rollbackDesc", "草稿箱删除。。。"); // 回滚原因
                    Map retmap =  businessRollbackServiceIntf.resRollBack(param);
                    if(!MapUtils.getBoolean(retmap,"success")){
                        result = "error";
                        logger.error("调用资源回滚接口失败！！！" + MapUtils.getString(retmap, "message"));
                    }
                }
                //删除业务订单对应的电路信息
                // this.delOrdAttrInfoBySrvId(srvOrdIdList);
                delOrderInfoDao.delOrdAttrInfoAll(srvOrdIdList);
                //删除业务订单信息
                this.delOrderInfoBySrvId(srvOrdIdList);
                //删除客户信息
                this.delCustomerInfo(CustID);
            }
            catch (Exception e) {
                result = "error";
                logger.info(e.getMessage());
                logger.error("***************error***************", e);
            }
        }
        else {
            result = "error";
        }

        return result;
    }
}
