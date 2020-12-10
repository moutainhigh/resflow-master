package com.zres.project.localnet.portal.orderAbnormal.service;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.orderAbnormal.dao.OrderAbnormalDao;
import com.zres.project.localnet.portal.util.OrderTrackOperType;
import com.zres.project.localnet.portal.webservice.data.dao.ExceptionFlowDao;
import com.zres.project.localnet.portal.webservice.flow.ExceptionChangeService;
import com.zres.project.localnet.portal.webservice.flow.ExceptionFlowIntf;
import com.zres.project.localnet.portal.webservice.interfaceJiKe.BackOrderServiceIntf;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderAbnormalFeedbackService {

    @Autowired
    private OrderAbnormalDao orderAbnormalDao;

    @Autowired
    private ExceptionFlowIntf exceptionFlowIntf;

    @Autowired
    private BackOrderServiceIntf backOrderServiceIntf;

    @Autowired
    private OrderDealDao orderDealDao;

    @Autowired
    private ExceptionFlowDao exceptionFlowDao;

    private static final String MAX_VERSION = "MAX_VERSION";
    private static final String SRV_ORD_ID = "SRV_ORD_ID";
    private static final String SUCCESS = "success";

    public Map appendRejectFeedBack(String cstOrdId) {
        Map retMap = new HashMap();

        String remark = "前台用户驳回";
        // 默认是一干的追单
        String type = ExceptionChangeService.EXCEPTION_104;
        // 根据cstOrdId查询数据来源、srvOrdId
        List<Map<String, Object>> srvInfoList = orderAbnormalDao.qrySrvInfoByCstOrdId(cstOrdId);

        // 1.先得到变更信息的最新版本
        Map<String, Object> tempMap = exceptionFlowDao.queryMaxChangeVersion(type, cstOrdId);
        int maxVersion = MapUtils.getIntValue(tempMap, MAX_VERSION);

        // 2.根据最新版本号得到变更信息集合
        List<Map<String, Object>> mapList = exceptionFlowDao.queryLastChangeOrderLog(type, cstOrdId, String.valueOf(maxVersion));
        if (mapList == null || mapList.isEmpty()) {
            // 查询不出数据，追单确认失败
            retMap.put(SUCCESS, false);
            return retMap;
        }
        // 判断是否是集客来单
        if ("jike".equals(MapUtils.getString(srvInfoList.get(0), "RESOURCES"))) {
            type = ExceptionChangeService.EXCEPTION_4A;
        }
        List<Map<String, Object>> srvList = new ArrayList<>();
        for (Map<String, Object> map : mapList) {
            String srvOrdId = MapUtils.getString(map, SRV_ORD_ID);
            String changeData = MapUtils.getString(map, "CHANGE_DATA");
            if (ExceptionChangeService.EXCEPTION_4A.equals(type)) {
                // 解析出追单信息用于反馈接口
                srvList.add(exceptionFlowIntf.parseChangeData(srvOrdId, changeData));
            }
        }

        // 判断是否是集客来单
        if ("jike".equals(MapUtils.getString(srvInfoList.get(0), "RESOURCES"))) {
            if (srvList != null && srvList.size() > 0) {
                for (Map<String, Object> tmp : srvList) {
                    String srvOrdId = MapUtils.getString(tmp, "SRV_ORD_ID", "");
                    String flowId = MapUtils.getString(tmp, "FLOW_ID");
                    // 集客来单，追单确认调用反馈接口
                    int num = orderDealDao.qryInterResultBak(srvOrdId, "FinishOrder_4A", flowId);
                    if (num < 1) {
                        int numFinish = orderDealDao.qryInterResultBak(srvOrdId, "BackOrder_4A", flowId);
                        if (numFinish < 1) {
                            //                                Map map = new HashMap();
                            tmp.put("srvOrdId", srvOrdId);
                            tmp.put("backExec", remark);
                            tmp.put("activeType", ExceptionChangeService.EXCEPTION_4A);// 代表追单
                            Map finMap = backOrderServiceIntf.backOrder(tmp);
                            if (!"1".equals(MapUtils.getString(finMap, "RESP_CODE"))) {
                                retMap.put("success", false);
                                retMap.put("message", "追单驳回失败!调用集客退单接口异常，srvOrdId:" + srvOrdId + " 异常原因：" + MapUtils.getString(finMap, "RESP_DESC"));
                                return retMap;
                            }
                        }
                    }
                    else {
                        retMap.put("success", false);
                        retMap.put("message", "追单驳回已成功，不能进行追单确认操作！");
                        return retMap;
                    }
                    // 以电路为维度记录追单确认日志
                    String operType = OrderTrackOperType.OPER_TYPE_5;
                    String message = "追单驳回";
                    exceptionFlowIntf.insertLogInfo(srvOrdId, message, operType);
                }
            }
        }
        retMap.put(SUCCESS, true);
        return retMap;
    }
}
