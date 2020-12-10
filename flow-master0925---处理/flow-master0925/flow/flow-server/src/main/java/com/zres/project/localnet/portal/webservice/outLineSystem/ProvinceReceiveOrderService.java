package com.zres.project.localnet.portal.webservice.outLineSystem;

import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.service.CommonMethodDealWoOrderServiceInf;
import com.zres.project.localnet.portal.logInfo.dao.LoggerInfoDao;
import com.zres.project.localnet.portal.logInfo.entry.ResInterfaceLog;
import com.zres.project.localnet.portal.util.OrderTrackOperType;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.HashMap;

/**
 * 省分IP智能网管系统回单接口
 *
 * @author wangsen
 * @date 2020/10/16 11:33
 * @return
 */
@RestController
@RequestMapping("/provinceReceiveOrderServiceIntf")
public class ProvinceReceiveOrderService implements ProvinceReceiveOrderServiceIntf {

    private static final Logger logger = LoggerFactory.getLogger(ProvinceReceiveOrderService.class);

    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private WebServiceDao wsd;

    @Autowired
    private LoggerInfoDao loggerInfoDao;

    @Autowired
    private CommonMethodDealWoOrderServiceInf commonMethodDealWoOrderServiceInf;

    @IgnoreSession
    @PostMapping(value = "/interfaceBDW/receiveOrder.spr", produces = "application/json;charset=UTF-8")
    @Override
    public Map receiveOrder(@RequestBody String request) {
        logger.info("-----receiveOrder-----" + request);
        Map resultMap = null;
        // 插入接口日志记录
        ResInterfaceLog resInterfaceLog = new ResInterfaceLog();
        resInterfaceLog.setInterfName("省份IP智能网管系统回单请求json报文-request");
        resInterfaceLog.setUrl("/provinceReceiveOrderServiceIntf/interfaceBDW/receiveOrder.spr");
        resInterfaceLog.setContent(request);
        String woId = "";
        String activeStatus = "";
        String remark = "";
        String special_type = "";
        String tradeId = "";
        String subscribeId = "";
        try {
            // 解析报文
            JSONObject jsStr = JSONObject.parseObject(request);
            String UNI_BSS_BODY = jsStr.getString("UNI_BSS_BODY");
            JSONObject js_UNI_BSS_BODY = JSONObject.parseObject(UNI_BSS_BODY);
            JSONObject js_PROVINCE_COMPLETE_ORDER_REQ = JSONObject.parseObject(js_UNI_BSS_BODY.getString("PROVINCE_COMPLETE_ORDER_REQ"));
            JSONObject js_ORDER_INFO = JSONObject.parseObject(js_PROVINCE_COMPLETE_ORDER_REQ.getString("ORDER_INFO"));
            woId = js_ORDER_INFO.getString("WO_ID"); //工单ID
            tradeId = js_ORDER_INFO.getString("TRADE_ID"); //业务订单编号
            activeStatus = js_ORDER_INFO.getString("ACTIVE_STATUS"); //激活状态 C：成功；  F：失败 ； OVT：设备连接超时状态
            remark = js_ORDER_INFO.getString("REMARK"); //激活描述说明
            special_type = orderDealDao.querySpecName(woId); //工单查询专业
            subscribeId = js_ORDER_INFO.getString("SUBSCRIBE_ID"); //激活描述说明

            if ("C".equals(activeStatus)) { //激活成功，工单正常流转到下一环节 ,提交工单
                String wo_state = orderDealDao.queryWoState(woId);
                if (wo_state.equals(OrderTrackOperType.WO_ORDER_STATE_18)) { //工单状态必须是待外系统回单290000118才可以进行激活回单
                    Map<String, Object> commonMap = new HashMap<>();
                    commonMap.put("orderId", orderDealDao.queryOrderByWoId(woId));
                    commonMap.put("woId", woId);
                    commonMap.put("operStaffId", "622594"); //TODO 先固定写一个处理人，后续修改
                    commonMap.put("remark", "省份IP智能网管系统激活成功");
                    commonMap.put("tacheId", tradeId);
                    commonMap.put("action", "回单"); //动作
                    commonMap.put("operType", OrderTrackOperType.OPER_TYPE_4); //操作类型
                    Map<String, Object> operAttrsVal = new HashMap<>();  //线条参数 ,工单流转使用
                    operAttrsVal.put("operAttrsVal", 0);
                    commonMap.put("operAttrsVal", operAttrsVal);
                    orderDealDao.updateWoStateByWoId(woId, OrderTrackOperType.WO_ORDER_STATE_2); //工单状态的待外系统回单290000118先修改为执行中290000002，否则在工单提交时异常
                    commonMethodDealWoOrderServiceInf.commonComplateWo(commonMap);
                    resultMap = responseJson("0", "成功");
                }
                else {
                    activeStatus = "1";
                    remark = "失败";
                    resultMap = responseJson("1", "失败，激活工单未成功派单到省份IP智能网管系统，请核实");
                }
            }
            if ("F".equals(activeStatus)) { //激活失败，工单可继续等待激活，
                orderDealDao.updateWoStateByWoId(woId, OrderTrackOperType.WO_ORDER_STATE_2);
            }
            if ("OVT".equals(activeStatus)) { //激活失败，设备连接超时状态，工单可继续等待激活，
                orderDealDao.updateWoStateByWoId(woId, OrderTrackOperType.WO_ORDER_STATE_2);
            }
            resInterfaceLog.setOrderNo(subscribeId);
            resInterfaceLog.setReturnContent(JSONObject.toJSONString(resultMap));
            resInterfaceLog.setRemark("省份IP智能网管系统回单返回json报文-resultMap");
            loggerInfoDao.saveResInterfaceInfo(resInterfaceLog); //写入操作日志记录
            Map<String, Object> actSendMap = new HashMap<>();
            actSendMap.put("woId", woId);
            actSendMap.put("specName", special_type);
            actSendMap.put("feedSystem", "省份IP智能网管系统"); // 省份IP智能网管系统
            actSendMap.put("activateCode", activeStatus);
            actSendMap.put("activateDesc", remark);
            wsd.saveActivateInfo(actSendMap); //写入激活日志记录，
        }
        catch (Exception e) {
            logger.error("省分IP智能网管系统激活回单接口异常：" + e.getMessage(), e);
            //TODO 异常后是否回滚？
            resultMap = responseJson("1", "失败，报文数据格式不正确，请检查数据");
        }
        return resultMap;
    }

    /**
     * 拼装反馈报文
     * @param respCode
     * @param respDesc
     * @return
     */
    public Map responseJson(String respCode, String respDesc) {
        Map pmap = new HashMap();
        pmap.put("PARA_ID", "");
        pmap.put("PARA_VALUE", "");
        Map map = new HashMap();
        map.put("RESP_DESC", respDesc);
        map.put("RESP_CODE", respCode);
        map.put("PARA", pmap);
        Map applyMap = new HashMap();
        applyMap.put("PROVINCE_COMPLETE_ORDER_RSP", map);
        Map bodyMap = new HashMap();
        bodyMap.put("UNI_BSS_BODY", applyMap);
        return bodyMap;
    }

}
