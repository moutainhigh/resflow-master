package com.zres.project.localnet.portal.webservice.ddos;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.flowdealinfo.service.CommonMethodDealWoOrderServiceInf;
import com.zres.project.localnet.portal.util.OrderTrackOperType;
import com.zres.project.localnet.portal.util.XmlUtil;
import com.zres.project.localnet.portal.webservice.data.dao.DDOSDao;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.zres.project.localnet.portal.webservice.interfaceJiKe.BackOrderServiceIntf;
import com.zres.project.localnet.portal.webservice.interfaceJiKe.FinishOrderService;

import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @description: 工单回单接口
 * @create: 2019-09-17 17:12
 **/
@RestController
@RequestMapping("/returnOrderServiceIntf")
public class ReturnOrderService implements ReturnOrderServiceIntf {
    private static final Logger logger = LoggerFactory.getLogger(ReturnOrderService.class);
    // 校验SRV_ORD下的关键必填字段
    private static final String REQUIRED_SRVORD = "SERIAL_NUMBER,TACHE_CODE,TRADE_ID";

    @Autowired
    private WebServiceDao webServiceDao;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private XmlUtil xmlUtil;
    @Autowired
    private BackOrderServiceIntf backOrderServiceIntf;
    @Autowired
    private CommonMethodDealWoOrderServiceInf commonComplate;
    @Autowired
    private DDOSDao ddosDao;
    @Autowired
    private FinishOrderService finishOrderService;

    @IgnoreSession
    @PostMapping(value="/interfaceBDW/receiveJson.spr", produces = "application/json;charset=UTF-8")
    @Override
    public Map receiveJson(@RequestBody String request) {
        logger.info("-----receiveJson-----" + request);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> interflog = new HashMap<String, Object>();
        interflog.put("URL", "/returnOrderServiceIntf/interfaceBDW/receiveJson.spr");
        interflog.put("CONTENT", request);
        try {
            JSONObject jsStr = JSONObject.parseObject(request);
            String UNI_BSS_BODY = jsStr.getString("UNI_BSS_BODY");
            JSONObject js_UNI_BSS_BODY = JSONObject.parseObject(UNI_BSS_BODY);
            JSONObject js_FINISH_ORDER_REQ = JSONObject.parseObject(js_UNI_BSS_BODY.getString("FINISH_ORDER_REQ"));
            JSONObject js_CST_ORD = JSONObject.parseObject(js_FINISH_ORDER_REQ.getString("CST_ORD"));
            JSONObject js_SRV_ORD_LIST = JSONObject.parseObject(js_CST_ORD.getString("SRV_ORD_LIST"));
            JSONArray js_SRV_ORD = JSON.parseArray(js_SRV_ORD_LIST.getString("SRV_ORD"));
            JSONObject jsRouting = JSONObject.parseObject(js_FINISH_ORDER_REQ.getString("ROUTING"));
            String routeValue = jsRouting.getString("ROUTE_VALUE");
            Iterator it = js_SRV_ORD.iterator();
            String tacheCodeLocal = "tache";
            String woId = "";
            while (it.hasNext()) {
                JSONObject srvOrdJson = (JSONObject) it.next();
                String tacheCode = srvOrdJson.getString("TACHE_CODE");
                tacheCodeLocal = getTacheCodeLocal(tacheCode);
                woId = srvOrdJson.getString("WORK_ID");
            }
            String subscribeId = js_CST_ORD.getString("SUBSCRIBE_ID");
            interflog.put("ORDERNO", subscribeId);
            //回单报文解析入库
            switch (routeValue) {
                case "04"://省内支撑中心
                    interflog.put("REMARK", "接收支撑中心回单接口json报文");
                    break;
                case "05":
                case "06"://省内网管系统
                    // 解析报文
                    interflog.put("INTERFNAME", "网管系统回单-" + tacheCodeLocal);
                    interflog.put("REMARK", "接收网管系统回单json报文");
                    break;
                case "07"://DDOS平台
                    resultMap = parseJsonDdos(request);
                    interflog.put("INTERFNAME", "DDOS平台系统回单");
                    interflog.put("REMARK", "接收DDOS平台系统回单json报文");
                    break;
                default:
                    resultMap = responseJson("1", "路由关键值不正确，请检查数据");
            }
        } catch (Exception e) {
            logger.error("工单回单接口异常：" + e.getMessage(), e);
            interflog.put("ORDERNO", "0000");
            interflog.put("INTERFNAME", "回单接口异常");
            interflog.put("REMARK", "接收系统回单json报文");
            resultMap = responseJson("1", "报文数据格式不正确，请检查数据");

        }finally {
            try {
                interflog.put("RETURNCONTENT", JSONObject.toJSONString(resultMap));
                webServiceDao.insertInterfLog(interflog);
            } catch (Exception e) {
                logger.error("工单回单接口入库异常：" + e.getMessage(), e);
                resultMap = responseJson("1", "接口入库异常，请检查");
            }
        }
        return resultMap;
    }




    /*
     * DDOS平台回单解析报文
     * @Param:
     * @Return:
     */
    public Map<String, Object> parseJsonDdos(String request) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            JSONObject jsStr = JSONObject.parseObject(request);
            String UNI_BSS_BODY = jsStr.getString("UNI_BSS_BODY");
            JSONObject js_UNI_BSS_BODY = JSONObject.parseObject(UNI_BSS_BODY);
            JSONObject js_FINISH_ORDER_REQ = JSONObject.parseObject(js_UNI_BSS_BODY.getString("FINISH_ORDER_REQ"));
            JSONObject jsRouting = JSONObject.parseObject(js_FINISH_ORDER_REQ.getString("ROUTING"));
            JSONObject js_CST_ORD = JSONObject.parseObject(js_FINISH_ORDER_REQ.getString("CST_ORD"));
            JSONObject js_SRV_ORD_LIST = JSONObject.parseObject(js_CST_ORD.getString("SRV_ORD_LIST"));
            // 取值 CST_ORD下的字段
            JSONArray srvOrdArray = JSON.parseArray(js_SRV_ORD_LIST.getString("SRV_ORD"));
            String subscribeId = js_CST_ORD.getString("SUBSCRIBE_ID");
            //遍历多个电路
            Iterator it = srvOrdArray.iterator();
            while (it.hasNext()) {
                JSONObject srvOrdJson = (JSONObject) it.next();
                //begin校验必填项
                Map<String, Object> checkCstDDOS = xmlUtil.checkJson(REQUIRED_SRVORD, srvOrdJson);
                if (!(Boolean) checkCstDDOS.get("checkFlag")) {
                    return responseJson("1", "请检查报文必填字段：" + checkCstDDOS.get("lackFiled"));
                }

                //获取处理人信息
                JSONObject operator = JSONObject.parseObject(srvOrdJson.getString("DEAL_INFO"));
                Map<String, Object> operatorMap = new HashMap<String, Object>();
                //根据支撑中心操作name查询调度系统对应的Id
                operatorMap.put("OPERATOR_NAME", operator.getString("OPERATOR_NAME"));
                operatorMap.put("OPERATOR_TEL", operator.getString("OPERATOR_TEL"));
                operatorMap.put("OPERATE_TIME", operator.getString("OPERATE_TIME"));

                //获取SRV_ORD_ID
                Map<String, Object> srvOrdParam = new HashMap<String, Object>();
                srvOrdParam.put("SERIAL_NUMBER", srvOrdJson.getString("SERIAL_NUMBER"));
                srvOrdParam.put("FLOW_ID", srvOrdJson.getString("FLOW_ID"));
                srvOrdParam.put("TRADE_ID", srvOrdJson.getString("TRADE_ID"));
                srvOrdParam.put("APPLY_ORD_ID", subscribeId);
                //查询电路信息srvOrdId,默认查出一个，用list有利于后期排查问题
                List<Map<String, Object>> srvOrdIdList = new ArrayList<>();
                srvOrdIdList = ddosDao.querySrvOrdId(srvOrdParam);
                if (srvOrdIdList.isEmpty()) {
                    return responseJson("1", "没有找到相应的电路信息。请检查");
                }
                String srvOrdId = "";
                String orderId = "";
                for (Map srvOrdIdMap : srvOrdIdList) {
                    if (srvOrdIdMap.containsKey("SRV_ORD_ID")) {
                        srvOrdId = MapUtils.getString(srvOrdIdMap, "SRV_ORD_ID", "");
                        orderId = MapUtils.getString(srvOrdIdMap, "ORDER_ID", "");
                    } else {
                        return responseJson("1", "没有找到相应的电路信息。请检查");
                    }
                }

                String result = srvOrdJson.getString("RESULT");
                //判断工单状态是不是待回单
                Map<String, Object> woInfo = ddosDao.queryWoInfoByOrderAndTacheId(orderId,"DDOS_FLOW_CLEANING");
                if (woInfo != null) {
                    String woState = MapUtils.getString(woInfo, "WO_STATE", "");
                    String woId = MapUtils.getString(woInfo, "WO_ID", "");
                    String backOrderDesc = srvOrdJson.getString("BACK_ORDER_DESC");
                    //工单状态为待回单才可以进行回单操作
                    if (!OrderTrackOperType.WO_ORDER_STATE_18.equals(woState)) {
                        return responseJson("1", "工单状态不是待回单，回单失败");
                    }
                    if ("0".equals(result)) {
                        // 专业施工数据制作环节回单操作
                        //更改状态为处理中
                        HashMap<String, String> operAttrsValMap = new HashMap<String, String>();
                        operAttrsValMap.put("is_backOrder", "1"); //是否退单
                        String action = "回单";
                        String operType = OrderTrackOperType.OPER_TYPE_4;
                        orderDealDao.updateWoStateByWoId(woId,OrderTrackOperType.WO_ORDER_STATE_2);
                        Map<String, Object> commonMap = new HashMap<String, Object>();
                        commonMap.put("operAttrsVal", operAttrsValMap);
                        commonMap.put("remark", backOrderDesc);
                        commonMap.put("operStaffId", "15");
                        commonMap.put("woId", woId);
                        commonMap.put("action", action);
                        commonMap.put("operType", operType);
                        commonMap.put("tacheId", BasicCode.DDOS_FLOW_CLEANING);
                        commonComplate.commonComplateWo(commonMap);
                        insertLog(orderId, operatorMap,"DDOS平台",OrderTrackOperType.OPER_TYPE_4);
                        //激活成功结果保存 0--成功  1--失败
                        resultMap = responseJson("0", "回单成功");
                        //反馈集客
                        Map<String, Object> param = new HashMap<>();
                        param.put("srvOrdId", srvOrdId);
                        int numFinish = orderDealDao.qryInterResult(srvOrdId, "FinishOrder");
                        if (numFinish < 1) {
                            Map finishOrderMap = finishOrderService.finishOrder(param);
                            if (!"1".equals(MapUtils.getString(finishOrderMap, "RESP_CODE"))) {
                                return responseJson("1", "工单回单异常:用集客反馈接口单接口异常，请检查");
                            }
                        }

                    } else if ("1".equals(result)) {
                        // 调用集客退单接口
                        Map<String, Object> orderDataMap = orderDealDao.qryOrderData(woId);
                        int numBackOrder = orderDealDao.qryInterResult(srvOrdId, "BackOrder");
                        if (numBackOrder < 1) {
                            Map map = new HashMap();
                            map.put("srvOrdId", srvOrdId);
                            map.put("backExec", backOrderDesc);
                            Map backMap = backOrderServiceIntf.backOrder(map);
                            if (!"1".equals(MapUtils.getString(backMap, "RESP_CODE"))) {
                                logger.error("工单回单异常：" + "派单失败!调用集客退单接口异常，异常原因"+MapUtils.getString(backMap, "RESP_DESC"));
                                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                                return responseJson("1", "工单回单异常:用集客退单接口异常，请检查");
                            }
                        }
                        // 修改业务订单状态为作废
                        Long orderIdL = Long.valueOf(orderId);
                        orderDealDao.updateSrvOrderStateById("10X", orderIdL);
                        insertLog(orderId, operatorMap,"DDOS平台",OrderTrackOperType.OPER_TYPE_5);
                        resultMap =  responseJson("0", "回单成功！！激活结果未通过，退单操作");
                    } else {
                        return responseJson("1", "回单失败！！请检查开通激活结果字段是否符合要求");
                    }
                } else {
                    return responseJson("1", "没有找到相应的工单");
                }
            }
        } catch (Exception e) {
            logger.error("DDOS平台回单接口异常：" + e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            resultMap = responseJson("1", "报文数据格式不正确，请检查数据");
        }
        return resultMap;
    }

    /**
     * 拼装反馈报文
     *
     * @param respCode
     * @param respDesc
     * @return
     */
    public Map responseJson(String respCode, String respDesc) {
        logger.info("开始拼装回单返回报文--");
        Map retMap = new HashMap();
        Map uniBssBody = new HashMap();
        Map changeOrderRsp = new HashMap();
        Map para = new HashMap();
        para.put("PARA_ID", "");
        para.put("PARA_VALUE", "");
        changeOrderRsp.put("PARA", para);
        changeOrderRsp.put("RESP_DESC", respDesc);
        changeOrderRsp.put("RESP_CODE", respCode);
        uniBssBody.put("CHANGE_ORDER_RSP", changeOrderRsp);
        retMap.put("UNI_BSS_BODY", uniBssBody);
        return retMap;
    }

    /**
     * 添加操作记录
     */
    public void insertLog(String orderId, Map staffMap, String source,String operType) {
        // 添加操作记录
        Map paramsMap = new HashMap();
        paramsMap.put("trackContent", source+"回单");
        paramsMap.put("operType", operType);
        paramsMap.put("trackMessage", source+"回单成功");
        paramsMap.put("orderId", orderId);
        paramsMap.put("woOrdId", null);
        paramsMap.put("trackOrgId", null);
        paramsMap.put("trackOrgName", source+"系统");
        paramsMap.put("trackDate", new java.sql.Date(new java.util.Date().getTime()));
        paramsMap.put("createDate", new java.sql.Date(new java.util.Date().getTime()));
        paramsMap.put("trackStaffId", null);
        paramsMap.put("trackStaffName", MapUtils.getString(staffMap, "OPERATOR_NAME"));
        paramsMap.put("trackStaffPhone", MapUtils.getString(staffMap, "OPERATOR_TEL"));
        paramsMap.put("trackStaffEmail", null);
        orderDealDao.insertTrackLogInfo(paramsMap);
    }

    /*
     * 转化环节 1-方案制定；2-支撑中心施工；3-支撑中心本地报竣；4-支撑中心全程报竣
     * @Param:
     * @Return:
     */
    private String getTacheCodeLocal(String tacheCode) {
        String tacheCodeLocal = "";
        switch (tacheCode) {
          /*  case "1": //方案制定环节--改电路调度环节状态为处理中
                tacheCodeLocal = BasicCode.PLAN_FORMULATION_S;
                break;
            case "2": //支撑中心施工环节--工单提交
                tacheCodeLocal = BasicCode.SUPPORT_CENTER_CONSTRUCT_S;
                break;
            case "3"://支撑中心本地报竣环节--工单提交
                tacheCodeLocal = BasicCode.LOCAL_TEST_S;
                break;
            case "4"://支撑中心全程报竣环节--工单提交
                tacheCodeLocal = BasicCode.CROSS_WHOLE_COURDER_TEST_S;
                break;
            case "5":
                tacheCodeLocal = EnmuValueUtil.IPRAN_DATA_MAKE;
                break;
            case "6":
                tacheCodeLocal = EnmuValueUtil.DIA_DATA_MAKE;
                break;*/
            default:
                tacheCodeLocal = "error";
                break;
        }
        return tacheCodeLocal;
    }
}
