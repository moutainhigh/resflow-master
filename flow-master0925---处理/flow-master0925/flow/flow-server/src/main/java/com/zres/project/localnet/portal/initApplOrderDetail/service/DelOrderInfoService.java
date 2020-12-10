package com.zres.project.localnet.portal.initApplOrderDetail.service;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.zres.project.localnet.portal.applpage.service.DelOrderInfoIntf;
import com.zres.project.localnet.portal.initApplOrderDetail.dao.DelOrderInfoDao;
import com.zres.project.localnet.portal.initApplOrderDetail.dao.EditDraftDao;

import bsh.StringUtil;

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
    public String draftDeleteByCustID(String CustID,String srvordIds) { //@1  modify 前台页面改为传入srvorderId ,因为退单时不能按照custId删除，不然会导致正常单子也被删除 by ren.jiahang at 20190524
        String result = "success";
        //根据客户ID 查询业务订单ID
        List<String> srvOrdIdAllList = editDraftDao.querySrvOrdIdByCustId(CustID, "");

        //add @1 增加两行
        String[] srvOrdIdSplit = StringUtil.split(srvordIds, ",");
        List<String> srvOrdIdList = Arrays.asList(srvOrdIdSplit);

        if (!srvOrdIdList.isEmpty()) {
            try {
                //删除业务订单对应的电路信息
                this.delOrdAttrInfoBySrvId(srvOrdIdList);
                //删除业务订单信息
                this.delOrderInfoBySrvId(srvOrdIdList);
                //删除客户信息
                if(srvOrdIdAllList.size()==srvOrdIdList.size()){ //需要删除的电路数量等于总数量则删除客户信息
                    this.delCustomerInfo(CustID);
                }
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
