package com.zres.project.localnet.portal.initApplOrderDetail.service;

import com.zres.project.localnet.portal.applpage.service.CheckOrderInfoIntf;
import com.zres.project.localnet.portal.initApplOrderDetail.dao.CheckOrderInfoDao;
import com.zres.project.localnet.portal.local.UnicomLocalOrderServiceIntf;
import com.ztesoft.res.frame.core.util.MapUtil;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tang.huili on 2019/2/20.
 */
@Service
public class CheckOrderInfoService implements CheckOrderInfoIntf {
    @Autowired
    private CheckOrderInfoDao checkOrderInfoDao;    
    @Autowired
    private UnicomLocalOrderServiceIntf unicomLocalOrderServiceIntf;

    /**
     * 校验电路编号
     *
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> checkCircuitCode(Map<String, Object> params) {
        String actType = MapUtils.getString(params, "actType");
        String circuitCode = MapUtils.getString(params, "circuitCode");
        Map<String, Object> retMap = new HashMap<>();
        retMap.put("choice", false);
        if ("101".equals(actType)) {
            // 新开
            if (!"".equals(circuitCode)) { // 电路编号不为空时校验唯一性
                int num = checkOrderInfoDao.queryCircuitCount(circuitCode);
                if (num > 0) {
                    retMap.put("success", true);
                    retMap.put("message", "电路编号不唯一！");
                } else {
                    retMap.put("success", true);
                    retMap.put("message", "电路编号校验通过！");
                }
            } else {
                retMap.put("success", true);
                retMap.put("message", "电路编号为空！");
            }
        }
        retMap.put("success", true);
        return retMap;
    }

    @Override
    public Map<String, Object> checkTradeId(Map<String, Object> params) {
        Map<String, Object> retMap = new HashMap<>();
        int srvOriderCount = 0;
        int srvNumberCount = 0;
        String actType = MapUtil.getString(params, "actType");
        String orderType = MapUtil.getString(params, "orderType");
        String tradeId = MapUtil.getString(params, "tradeId");
        String serialNumber = MapUtil.getString(params, "serialNumber");
        params.put("accNbr", serialNumber);
        params.put("isToBeQuery", 1);

        // 新开单要查询所有数据没有重复的,非新开只要查询除了新开单的号码数量为0则验证通过；新开单删除actType
        if ("101".equals(actType)) {
            Boolean flag = true;
            //新开需要查询资源是否有业务号码
            Map<String, Object> resData = unicomLocalOrderServiceIntf.queryResData(params);
            if (!MapUtils.getBoolean(resData, "success")){
                flag = false;
            }
            //如果都没有的话 会返回只有一条 存有“不存在”的 list
//            String isExist = MapUtils.getString(resData.get(0), "isExist");
//            if (null != isExist && !"".equals(isExist) && "不存在".equals(isExist)){
//                flag = false;
//            }
            /*for (Map resDataMap : resData) {
                String isExist = MapUtils.getString(resDataMap, "isExist");
            }*/
            params.remove("actType");
            //modify by wang ZMP [1694323]   不论正常核查单  新开都不能重复
            params.remove("orderType");
            if (params.containsKey("tradeId") && (!"".equals(tradeId))) {
                srvOriderCount = checkOrderInfoDao.querySrvOriderCount(params);
            }
            if (params.containsKey("serialNumber") && (!"".equals(serialNumber))) {
                srvNumberCount = checkOrderInfoDao.querySrvNumberCount(params);
            }
            if (!flag && srvOriderCount == 0 && srvNumberCount == 0) {
                retMap.put("result", "success");
            }
            if (!flag && srvOriderCount > 0 && srvNumberCount == 0) {
                retMap.put("result", "业务订单号已存在，请检查并修改！");
            }
            if (!flag && srvNumberCount > 0 && srvOriderCount == 0) {
                retMap.put("result", "业务号码已存在，请检查并修改！");
            }
            if (srvOriderCount == 0 && srvNumberCount == 0 && flag) {
                retMap.put("result", "存量已存在该业务号码，请检查并修改！");
            }
            if (srvOriderCount > 0 && srvNumberCount == 0 && flag) {
                retMap.put("result", "调度已存在该业务定单号、存量已存在该业务号码，请检查并修改！");
            }
            if (srvOriderCount == 0 && srvNumberCount > 0 && flag) {
                retMap.put("result", "调度跟存量已存在该业务号码，请检查并修改！");
            }
            if (!flag && srvOriderCount > 0 && srvNumberCount > 0) {
                retMap.put("result", "调度已存在该业务订单号和业务号码，请检查并修改！");
            }
            if (srvOriderCount > 0 && srvNumberCount > 0 && flag) {
                retMap.put("result", "存量已存在该业务号码、调度已存在该业务订单号和业务号码，请检查并修改！");
            }
        }
        // modify 2019/04/15 非新开暂时不校验 ren.jiahang
        //非新开要验证是否在途单
        else {
            Boolean flag = true;
            List<Map<String, Object>> stateList = checkOrderInfoDao.querySrvOrderInfoState(params);
            for (Map stateMap : stateList) {
                String srv_ord_stat = MapUtil.getString(stateMap, "SRV_ORD_STAT", "");
                String activeType = MapUtil.getString(stateMap, "ACTIVE_TYPE", "");
                //作废或者完成才允许
                if (srv_ord_stat != null && ("10F".equals(srv_ord_stat) || "10X".equals(srv_ord_stat) || "4D".equals(srv_ord_stat))) {
                    continue;
                }
                else {
                      flag = false;
                }
             }
            if (flag) {
                retMap.put("result", "success");
            }
            else {
                retMap.put("result", "该电路已在调度中，在途电路");
            }
        }

        return retMap;
    }

    @Override
    public String querySrvOrderState(Map<String, Object> params) {
        StringBuffer result = new StringBuffer();
        List<Map<String, Object>> stateList = checkOrderInfoDao.querySrvOrderState(params);
        for (Map stateMap : stateList) {
            String srv_ord_stat = MapUtil.getString(stateMap, "SRV_ORD_STAT", "");
            String apply_ord_id = MapUtil.getString(stateMap, "APPLY_ORD_ID", "");
            String activeType = MapUtil.getString(stateMap, "ACTIVE_TYPE", "");

            if (srv_ord_stat != null && "10C".equals(srv_ord_stat)) {
                result.append("该电路已在草稿单，申请单编号：【").append(apply_ord_id).append("】");
                return result.toString();
            } else if ("10F".equals(srv_ord_stat) && "102".equals(activeType)) {
                // 代表已拆机成功
                result.append("该电路已拆机归档，申请单编号：【").append(apply_ord_id).append("】");
                return result.toString();
            } else if (!("10C".equals(srv_ord_stat) || "10F".equals(srv_ord_stat) || "4D".equals(srv_ord_stat) || "10X".equals(srv_ord_stat) )) {
                result.append("该电路已在调度中，申请单编号：【").append(apply_ord_id).append("】");
                return result.toString();
            }
        }

        return result.toString();
    }
    @Override
    public String queryIsTear(Map<String, Object> params) {
        return checkOrderInfoDao.queryIsTear(params);
    }
    @Override
    public String queryisOnWay(Map<String, Object> params) {
        return checkOrderInfoDao.queryisOnWay(params);
    }
}
