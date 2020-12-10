package com.zres.project.localnet.portal.cloudNetworkFlow.tacheWoOrder;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.cloudNetworkFlow.dao.TacheWoOrderDao;
import com.zres.project.localnet.portal.common.CommonWoOrderDealServiceIntf;
import com.zres.project.localnet.portal.common.DealTacheWoOrderIntf;
import com.zres.project.localnet.portal.common.util.TacheIdEnum;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf;
import com.zres.project.localnet.portal.logInfo.service.TacheDealLogIntf;
import com.zres.project.localnet.portal.util.OrderTrackOperType;

import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;

/*
 * 云组网电路调度环节
 * @author guanzhao
 * @date 2020/10/30
 *
 */
@Service
public class CircuitDispatchTache implements DealTacheWoOrderIntf {

    Logger logger = LoggerFactory.getLogger(CircuitDispatchTache.class);

    @Autowired
    private CommonWoOrderDealServiceIntf commonWoOrderDealServiceIntf;
    @Autowired
    private TacheDealLogIntf tacheDealLogIntf;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private TacheWoOrderDao tacheWoOrderDao;
    @Autowired
    private OrderDealServiceIntf orderDealServiceIntf;

    /*
     * 电路调度处理逻辑：
     * 1，根据页面选择的按钮来处理：保存：保存专业配置；提交，电路调度环节提交；
     * 2，保存：这个还是走的之前的保存功能，这里就不做了；
     * 3，提交： 先保存调单，
     *          再查询修改专业配置的数据状态为2，gom_bdw_ord_specialty表字段delete_state
     *          最后回单；
     * @author guanzhao
     * @date 2020/10/30
     *
     */
    @Override
    public Map<String, Object> tacheDoSomething(Map<String, Object> tacheDoSomeMap) throws Exception {
        logger.info("---------------------进入电路调度环节处理-----------------------");
        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(operStaffId));
        Map<String, String> operAttrs = MapUtils.getMap(tacheDoSomeMap, "operAttrs");
        String remark = MapUtils.getString(operAttrs, "remark");
        String newResRadio = MapUtils.getString(operAttrs, "newResRadio");
        String buttonAction = MapUtils.getString(tacheDoSomeMap, "buttonAction"); //按钮动作
        Map<String, Object> circuitDataMap = MapUtils.getMap(tacheDoSomeMap, "circuitDataMap");
        String woId = MapUtils.getString(circuitDataMap, "WO_ID");
        String orderId = MapUtils.getString(circuitDataMap, "ORDER_ID");
        String psId = MapUtils.getString(circuitDataMap, "PS_ID");
        String tacheId = MapUtils.getString(circuitDataMap, "TACHE_ID");
        String srvOrdId = MapUtils.getString(circuitDataMap, "SRV_ORD_ID");
        Map<String, String> operAttrsValMap = new HashMap<>(); //线条参数
        if (OrderTrackOperType.SUBMIT_BUTTON.equals(buttonAction)) {
            tacheDoSomeMap.put("sysResource", BasicCode.LOCAL);
            tacheDoSomeMap.put("woId", woId);
            tacheDoSomeMap.put("remark", remark);
            tacheDoSomeMap.put("srvOrdIdStr", srvOrdId);
            orderDealServiceIntf.insertDispatchOrder(tacheDoSomeMap);
            Map<String, String> updMap = new HashMap<>();
            updMap.put("orderId", orderId);
            updMap.put("newResRadio", newResRadio);
            tacheWoOrderDao.updOrderConfig(updMap);
            operAttrsValMap.put("if_newResource", newResRadio);
            //不需要新建资源并且是本地新开流程，需要传下面的线条参数
            if (OrderTrackOperType.NO_LINE.equals(newResRadio) && TacheIdEnum.YZW_L_NEWOPEN_FLOW.equals(psId)) {
                operAttrsValMap.put("a_config", OrderTrackOperType.YES_LINE);
                operAttrsValMap.put("z_config", OrderTrackOperType.YES_LINE);
            }
        }
        String operType = "rollback".equals(buttonAction) ? OrderTrackOperType.OPER_TYPE_5 : OrderTrackOperType.OPER_TYPE_4;
        String action = "rollback".equals(buttonAction) ? "退单" : "回单";
        Map<String, Object> logDataMap = new HashMap<String, Object>();
        logDataMap.put("woId", woId);
        logDataMap.put("orderId", orderId);
        logDataMap.put("remark", remark == null ? "" : remark);
        logDataMap.put("tacheId", tacheId);
        logDataMap.put("operStaffInfoMap", operStaffInfoMap);
        logDataMap.put("operType", operType);
        logDataMap.put("action", action);
        logDataMap.put("trackMessage", "[ " + action + " ]");
        tacheDealLogIntf.addTrackLog(logDataMap); //入库操作日志
        Map<String, Object> complateMap = new HashMap<>();
        complateMap.put("operAttrsVal", operAttrsValMap);
        complateMap.put("operStaffId", operStaffId);
        complateMap.put("woId", woId);
        return commonWoOrderDealServiceIntf.complateWoService(complateMap);
    }
}
