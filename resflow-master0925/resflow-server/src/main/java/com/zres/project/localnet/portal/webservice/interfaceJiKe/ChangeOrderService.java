package com.zres.project.localnet.portal.webservice.interfaceJiKe;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.zres.project.localnet.portal.webservice.data.dao.InterfaceBoDao;
import com.ztesoft.res.frame.core.util.ListUtil;
import com.ztesoft.res.frame.flow.task.dao.PubDAO;
import com.ztesoft.res.frame.flow.task.service.PubVal;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderQrySecondaryDao;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealService;
import com.zres.project.localnet.portal.util.OrderTrackOperType;
import com.zres.project.localnet.portal.util.XmlUtil;
import com.zres.project.localnet.portal.webservice.data.dao.ExceptionFlowDao;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.zres.project.localnet.portal.webservice.dto.JiKeCustomInfoDTO;
import com.zres.project.localnet.portal.webservice.dto.JiKeProdAttrDTO;
import com.zres.project.localnet.portal.webservice.dto.JiKeProdInfoDTO;
import com.zres.project.localnet.portal.webservice.flow.ExceptionFlowIntf;
import com.zres.project.localnet.portal.webservice.res.BuizQueryOnTimeServiceIntf;
import com.zres.project.localnet.portal.webservice.res.BusinessRollbackServiceIntf;

import com.ztesoft.zsmart.pot.annotation.IgnoreSession;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Created by tang.huili on 2019/4/29.
 * 5.2.	异常单处理接口
 * <p>
 * Update by jiyou.li on 2019/5/6
 */
@Controller
@RequestMapping("/changeOrderServiceIntf")
public class ChangeOrderService implements ChangeOrderServiceIntf {
    private static final Logger logger = LoggerFactory.getLogger(ReceiveJsonService.class);
    @Autowired
    PubDAO pubDAO;
    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private OrderDealService orderDealService;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private BuizQueryOnTimeServiceIntf buizQueryOnTimeServiceIntf;
    @Autowired
    private XmlUtil xmlUtil;
    @Autowired
    private BusinessRollbackServiceIntf businessRollbackServiceIntf;
    @Autowired
    private ExceptionFlowIntf exceptionFlowIntf;
    @Autowired
    private ExceptionFlowDao exceptionFlowDao;
    @Autowired
    private OrderQrySecondaryDao orderQrySecondaryDao;
    @Autowired
    private InterfaceBoDao interfaceBoDao;

    @IgnoreSession
    @ResponseBody
    @RequestMapping(value = "/interfaceBDW/changeOrder.spr", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Map changeOrder(@RequestBody String request) {
        logger.info("-----changeOrder-----" + request);

        Map<String, Object> resultMap = new HashMap<>();
        Map retMap = new HashMap<>();
        //插入接口记录
        Map<String, Object> interflog = new HashMap<String, Object>();
        interflog.put("INTERFNAME", "异常单接口changeOrder");
        interflog.put("URL", "/changeOrderServiceIntf/interfaceBDW/changeOrder.spr");
        interflog.put("CONTENT", request);
        interflog.put("REMARK", "接收集客json报文-changeOrder");

        try {
            List<JiKeProdInfoDTO> jiKeProdInfoDTOList = new ArrayList<>();
            List<JiKeProdAttrDTO> jiKeProdAttrDTOList = new ArrayList<>();
            JiKeCustomInfoDTO newJikeCustomInfoDTO = new JiKeCustomInfoDTO();
            JiKeProdInfoDTO newJiKeProdInfoDTO = new JiKeProdInfoDTO();
            String activeType = "";
            // 解析报文
            JSONObject jsStr = JSONObject.parseObject(request);
            String UNI_BSS_BODY = jsStr.getString("UNI_BSS_BODY");
            JSONObject js_UNI_BSS_BODY = JSONObject.parseObject(UNI_BSS_BODY);
            JSONObject js_RENT_ORDER_REQ = JSONObject.parseObject(js_UNI_BSS_BODY.getString("CHANGE_ORDER_REQ"));

            JSONObject routingJsonObj = JSONObject.parseObject(js_RENT_ORDER_REQ.getString("ROUTING"));
         /*   String routeType = routingJsonObj.getString("ROUTE_TYPE");
            String routeValue = routingJsonObj.getString("ROUTE_VALUE");
            JiKeProdAttrDTO newJiKeProdAttrDTO1 = new JiKeProdAttrDTO();
            newJiKeProdAttrDTO1.setAttrCode("ROUTE_TYPE");
            newJiKeProdAttrDTO1.setAttrValue(routeType);
            jiKeProdAttrDTOList.add(newJiKeProdAttrDTO1);
            JiKeProdAttrDTO newJiKeProdAttrDTO2 = new JiKeProdAttrDTO();
            newJiKeProdAttrDTO2.setAttrCode("ROUTE_VALUE");
            newJiKeProdAttrDTO2.setAttrValue(routeValue);
            jiKeProdAttrDTOList.add(newJiKeProdAttrDTO2);*/

            JSONObject js_CST_ORD = JSONObject.parseObject(js_RENT_ORDER_REQ.getString("CST_ORD"));
            //申请单号
            String subscribeId = js_CST_ORD.getString("SUBSCRIBE_ID") == null ? "" : js_CST_ORD.getString("SUBSCRIBE_ID");
            interflog.put("ORDERNO", subscribeId);

            //取值 SRV_ORD_LIST 下的字段
            String SrvOrdInfoId = "";
            JSONObject js_SRV_ORD_LIST = JSONObject.parseObject(js_CST_ORD.getString("SRV_ORD_LIST"));
            JSONArray js_SRV_ORD = JSONObject.parseArray(js_SRV_ORD_LIST.getString("SRV_ORD"));
            Iterator it = js_SRV_ORD.iterator();
            while (it.hasNext()) {
                JSONObject jso = (JSONObject) it.next();
                activeType = jso.getString("ACTIVE_TYPE"); //异常类型
                String serialNumber = jso.getString("SERIAL_NUMBER"); //业务号码
                String tradeId = jso.getString("TRADE_ID"); //业务订单号
                String tradeIdReal = jso.getString("TRADE_ID_RELA"); //原业务订单号
                String flowId = jso.getString("FLOW_ID");
                String srvOrdDesc = jso.getString("SRV_ORD_DESC");
                String requireCompleteDate = jso.getString("REQUIRE_COMPLETE_DATE") == null ? "" : jso.getString("REQUIRE_COMPLETE_DATE");
                String aRequireDate  = jso.getString("REQUIRE_DATE_A") == null ? "" : jso.getString("REQUIRE_DATE_A");
                String zRequireDate = jso.getString("REQUIRE_DATE_Z") == null ? "" : jso.getString("REQUIRE_DATE_Z");

                if (!StringUtils.isEmpty(requireCompleteDate)) {
                    JiKeProdAttrDTO newJiKeProdAttrDTO = new JiKeProdAttrDTO();
                    newJiKeProdAttrDTO.setAttrCode("CON0014");
                    newJiKeProdAttrDTO.setAttrValueName("REQUIRE_COMPLETE_DATE");
                    newJiKeProdAttrDTO.setAttrValue(dateFormat(requireCompleteDate));
                    jiKeProdAttrDTOList.add(newJiKeProdAttrDTO);

                }
                if (!StringUtils.isEmpty(aRequireDate)) {
                    JiKeProdAttrDTO newJiKeProdAttrDTO = new JiKeProdAttrDTO();
                    newJiKeProdAttrDTO.setAttrCode("CON0015");
                    newJiKeProdAttrDTO.setAttrValueName("REQUIRE_DATE_A");
                    newJiKeProdAttrDTO.setAttrValue(dateFormat(aRequireDate));
                    jiKeProdAttrDTOList.add(newJiKeProdAttrDTO);
                }

                if (!StringUtils.isEmpty(zRequireDate)) {
                    JiKeProdAttrDTO newJiKeProdAttrDTO = new JiKeProdAttrDTO();
                    newJiKeProdAttrDTO.setAttrCode("CON0016");
                    newJiKeProdAttrDTO.setAttrValueName("REQUIRE_DATE_Z");
                    newJiKeProdAttrDTO.setAttrValue(dateFormat(zRequireDate));
                    jiKeProdAttrDTOList.add(newJiKeProdAttrDTO);
                }
                if (!StringUtils.isEmpty(srvOrdDesc)) {
                    JiKeProdAttrDTO newJiKeProdAttrDTO = new JiKeProdAttrDTO();
                    newJiKeProdAttrDTO.setAttrCode("SRV_ORD_DESC");
                    newJiKeProdAttrDTO.setAttrValue(srvOrdDesc);
                    jiKeProdAttrDTOList.add(newJiKeProdAttrDTO);
                }


                //String orderId = this.upSrvOrdInfoByChangeOrder(tradeIdReal, activeType, serialNumber, requireCompleteDate, srvOrdDesc);
                newJiKeProdInfoDTO.setFlowId(flowId);
                newJiKeProdInfoDTO.setActiveType(activeType);
                newJiKeProdInfoDTO.setTradeId(tradeId);
                newJiKeProdInfoDTO.setTradeIdRela(tradeIdReal);
                newJiKeProdInfoDTO.setSerialNumber(serialNumber);
                newJiKeProdInfoDTO.setJiKeProdAttrDTOList(jiKeProdAttrDTOList);
                jiKeProdInfoDTOList.add(newJiKeProdInfoDTO);
                // 添加操作记录
                Map paramsMap = new HashMap();
                String orderId = "";
                String srvOrdId = "";
                String srvOrdState = "";
                List<Map<String, Object>> orderList = orderDealService.queryOrderList(tradeIdReal, serialNumber);
                if (orderList != null && orderList.size() > 0) {
                    orderId = orderList.get(0).get("ORDER_ID").toString();
                    srvOrdId = orderList.get(0).get("SRV_ORD_ID").toString();
                    srvOrdState = MapUtils.getString(orderList.get(0),"SRV_ORD_STAT");
                    resultMap.put("orderId", orderId);
                }
                else {
                    resultMap.put("respDesc", "异常单通知调度成功");
                    resultMap.put("respCode", "0");
                    resultMap.put("orderId", orderId);
                    retMap = wrapResInfo(resultMap);
                    return retMap;
                }
                // 当前处理人信息
             /*   List<Map> list = orderDealService.queryListDealCurrentUser(orderId, activeType);

                if(list != null && list.size() > 0) {
                    Map<String, String> map = list.get(0);
                    orderDealService.addOrderNotice(srvOrdId, requireCompleteDate, srvOrdDesc, activeType, map);
                }*/
                Map orderInfoMap = (HashMap) pubDAO.getOrderStateAndPsId(orderId);
                String orderState = MapUtils.getString(orderInfoMap, "order_state");

                switch (activeType) {
                    case "4A": {
                        paramsMap.put("trackContent", "追单");
                        paramsMap.put("operType", OrderTrackOperType.OPER_TYPE_5);
                        break;
                    }
                    case "4E": {
                        paramsMap.put("trackContent", "挂起订单");
                        paramsMap.put("operType", OrderTrackOperType.OPER_TYPE_8);
                        break;
                    }
                    case "4F": {
                        paramsMap.put("trackContent", "解挂订单");
                        paramsMap.put("operType", OrderTrackOperType.OPER_TYPE_9);
                        break;
                    }
                    case "4B": {
                        paramsMap.put("trackContent", "订单加急");
                        paramsMap.put("operType", OrderTrackOperType.OPER_TYPE_10);
                        break;
                    }
                    case "4C": {
                        paramsMap.put("trackContent", "订单延期");
                        paramsMap.put("operType", OrderTrackOperType.OPER_TYPE_11);
                        break;
                    }
                    case "4D": {
                        paramsMap.put("trackContent", "订单撤销");
                        paramsMap.put("operType", OrderTrackOperType.OPER_TYPE_12);
                        break;
                    }
                    default: {
                        logger.error("未知 activeTpe：" + activeType);
                        break;
                    }
                }
                paramsMap.put("orderId", orderId);
                paramsMap.put("woOrdId", null);
                paramsMap.put("trackOrgId", null);
                paramsMap.put("trackOrgName", "集客系统");
                paramsMap.put("trackDate", new java.sql.Date(new Date().getTime()));
                paramsMap.put("createDate", new java.sql.Date(new Date().getTime()));
                paramsMap.put("trackStaffId", null);
                paramsMap.put("trackStaffName", null);
                paramsMap.put("trackStaffPhone", null);
                paramsMap.put("trackStaffEmail", null);
                paramsMap.put("trackMessage", srvOrdDesc);
                orderDealDao.insertTrackLogInfo(paramsMap);

                //挂起和撤单  200000002
            //    if ((PubVal.ORD_STA_STAFLW.equals(orderState) && "4D".equals(activeType)) || (PubVal.ORD_STA_STAFLW.equals(orderState) && "4E".equals(activeType))) {

                // 订单交互类型 4B：加急；4C：延期；4D：撤业务订单;4E：挂起；4F：解挂;
                Set<String> activeTypeSet = new HashSet<String>();
                activeTypeSet.add("4B");
                activeTypeSet.add("4C");
                activeTypeSet.add("4D");
                activeTypeSet.add("4E");
                /**
                 * 状态是正常[200000002]，允许进行加急延期挂起撤单
                 */
                if(PubVal.ORD_STA_STAFLW.equals(orderState)&& activeTypeSet.contains(activeType)){
                    if ("4E".equals(activeType)) {
                        if (!StringUtils.isEmpty(requireCompleteDate)) {
                            exceptionFlowDao.updateOrderReqFinDate(srvOrdId,  requireCompleteDate);
                        }
                        updateJikeAttr(srvOrdId, jiKeProdAttrDTOList);
                    }
                    if ("4D".equals(activeType)) {
                        /*1.查询是否下发本地网；
                          2.如果下发本地网，需要先回滚本地网的资源信息
                          3.回滚二干系统的资源信息
                          */
                        List<Map<String,Object>> localList = interfaceBoDao.qryLocalInfo(srvOrdId);
                        if(localList!=null && localList.size()>0){
                            // 本地网回滚
                            for(Map<String,Object> temp : localList){
                                resultMap = resRollBack(MapUtils.getString(temp,"RELATE_INFO_ID",""));
                                if("1".equals(MapUtils.getString(resultMap,"respCode"))){
                                    retMap = wrapResInfo(resultMap);
                                    interflog.put("RETURNCONTENT", new JSONObject(retMap).toJSONString());
                                    return retMap;
                                }
                            }
                        }
                        // 集客下发撤单，调用资源回滚接口
                        resultMap = resRollBack(srvOrdId);
                        if("1".equals(MapUtils.getString(resultMap,"respCode"))){
                            retMap = wrapResInfo(resultMap);
                            interflog.put("RETURNCONTENT", new JSONObject(retMap).toJSONString());
                            return retMap;
                        }
                    }
                    Map<String, Object> returnMap = exceptionFlowIntf.jiKeExceptionFlowChange(activeType, null, jiKeProdInfoDTOList);
                    // 修改业务订单的状态 挂起要先起流程再改状态
                    if ("4E".equals(activeType) || "4D".equals(activeType)){
                        this.updateSrvOrdState(tradeId, tradeIdReal, activeType, serialNumber, requireCompleteDate);
                    }
                    if (MapUtils.getBoolean(returnMap, "success")) {
                        resultMap.put("respDesc", "异常单通知调度成功");
                        resultMap.put("respCode", "0");
                    }
                    else {
                        resultMap.put("respDesc",  MapUtils.getString(returnMap, "message"));
                        resultMap.put("respCode", "1");
                    }
                    retMap = wrapResInfo(resultMap);
                    interflog.put("RETURNCONTENT", new JSONObject(retMap).toJSONString());
                }
                //解挂 200000006
                else if (PubVal.ORD_STA_SUSPEND.equals(orderState) && "4F".equals(activeType) && "4E".equals(srvOrdState)) {
                        if (!StringUtils.isEmpty(requireCompleteDate)) {
                            exceptionFlowDao.updateOrderReqFinDate(srvOrdId,  requireCompleteDate);
                        }
                        updateJikeAttr(srvOrdId, jiKeProdAttrDTOList);
                        // 解挂要先改状态再起流程
                        this.updateSrvOrdState(tradeId, tradeIdReal, activeType, serialNumber, requireCompleteDate);
                        Map<String, Object> returnMap = exceptionFlowIntf.jiKeExceptionFlowChange(activeType, null, jiKeProdInfoDTOList);
                        if (MapUtils.getBoolean(returnMap, "success")) {
                            resultMap.put("respDesc", "异常单通知调度成功");
                            resultMap.put("respCode", "0");
                        }
                        else {
                            resultMap.put("respDesc",  MapUtils.getString(returnMap, "message"));
                            resultMap.put("respCode", "1");
                        }
                        retMap = wrapResInfo(resultMap);
                        interflog.put("RETURNCONTENT", new JSONObject(retMap).toJSONString());
                }
                else {
                    resultMap.put("respCode", "0");
                    resultMap.put("respDesc", "异常单通知调度成功");
                    retMap = wrapResInfo(resultMap);
                    interflog.put("RETURNCONTENT", new JSONObject(retMap).toJSONString());
                    return   retMap;
                }
            }
        }
        catch (Exception e) {
		    logger.error("异常单处理接口：" + e.getMessage(), e);
            resultMap.put("respDesc", e.getMessage());
            resultMap.put("respCode", "1");
            retMap = wrapResInfo(resultMap);
            interflog.put("RETURNCONTENT", new JSONObject(retMap).toJSONString());
            logger.error("异常单处理接口：" + e.getMessage(), e);
        }
        finally {
            wsd.insertInterfLog(interflog);
        }
        //拼返回报文
        logger.info("-----异常单接口返回报文-----" + retMap.toString());
        return retMap;

    }

    /**
     * 调用资源回滚接口逻辑
     * @param srvOrdId
     * @return
     */
    private Map<String,Object> resRollBack(String srvOrdId) {
        Map<String,Object> retMap = new HashMap<>();
        Map<String, Object> param = new HashMap<>();
        param.put("srvOrdId", srvOrdId);// gom_bdw_srv_ord_info.srv_ord_id
        param.put("orderIds", null);// orderIds是个List, 存放的是 子流程的gom_order.order_id
        param.put("rollbackDesc", "集客下发异常单撤单"); // 回滚原因
        Map<String,Object> resultMap = businessRollbackServiceIntf.resRollBack(param);
        if (!MapUtils.getBoolean(resultMap, "success")) {
            resultMap.put("respCode","0");
            resultMap.put("respDesc", "异常单下发失败!" + MapUtils.getString(resultMap, "message"));
          //  retMap = wrapResInfo(resultMap);
        }
        resultMap.put("respCode","0");
        return resultMap;
    }

    /**
     * 封装返回数据
     *
     * @param tempMap
     * @return
     */
    public Map wrapResInfo(Map<String, Object> tempMap) {
        logger.info("开始拼装异常单返回报文--");
        Map retMap = new HashMap();
//        retMap.put("UNI_BSS_HEAD",xmlUtil.requestHeader());
        Map uniBssBody = new HashMap();
        Map changeOrderRsp = new HashMap();
        Map para = new HashMap();
        para.put("PARA_ID", "");
        para.put("PARA_VALUE", "");
        changeOrderRsp.put("PARA", para);
        changeOrderRsp.put("RESP_DESC", MapUtils.getString(tempMap, "respDesc"));
        changeOrderRsp.put("RESP_CODE", MapUtils.getString(tempMap, "respCode"));
        uniBssBody.put("CHANGE_ORDER_RSP", changeOrderRsp);
        retMap.put("UNI_BSS_BODY", uniBssBody);
        return retMap;
    }

    /**
     * 异常单处理  更新订单信息表状态，写进通知单里面
     * 暂不调用该方法
     */
    public Map upSrvOrdInfoByChangeOrder(String tradeId, String tradeIdReal, String activeType, String serialNumber, String requireCompleteCate, String srvOrdDesc) {

        Map<String, Object> resultMap = new HashMap<>();
        String orderId = "";
        String srvOrdId = "";
        List<Map<String, Object>> orderList = orderDealService.queryOrderList(tradeIdReal, serialNumber);
        if (orderList != null && orderList.size() > 0) {
            orderId = orderList.get(0).get("ORDER_ID").toString();
            srvOrdId = orderList.get(0).get("SRV_ORD_ID").toString();
            resultMap.put("orderId", orderId);
        }
        else {
            resultMap.put("respDesc", "异常单[tradeId : " + tradeId + "]未找到原单信息");
            resultMap.put("respCode", "1");
            resultMap.put("orderId", orderId);
            return resultMap;
        }
        // 当前处理人信息
        List<Map> list = orderDealService.queryListDealCurrentUser(orderId, activeType);

        // 订单交互类型 4B：加急；4C：延期；4D：撤业务订单;4E：挂起；4F：解挂;
        Set<String> activeTypeSet = new HashSet<String>();
        activeTypeSet.add("4B");
        activeTypeSet.add("4C");
        activeTypeSet.add("4D");
        activeTypeSet.add("4E");
        activeTypeSet.add("4F");
        if (activeTypeSet.contains(activeType)) {
            orderDealService.updateOrdInfo(activeType, tradeIdReal, serialNumber);
            if (!"".equals(requireCompleteCate)) {
                orderDealService.updateFinDate(requireCompleteCate, orderId);
            }
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    Map<String, String> map = list.get(i);
                    orderDealService.addOrderNotice(srvOrdId, requireCompleteCate, srvOrdDesc, activeType, map);
                }
            }
        } else {
            // 插入通知单，然后根据环节和订单状态进行提示
            if (list != null && list.size() > 0) {
                Map<String, String> map = list.get(0);
                orderDealService.addOrderNotice(srvOrdId, requireCompleteCate, srvOrdDesc, activeType, map);
            }
        }
        if ("4D".equals(activeType)) {
            Map<String, Object> param = new HashMap<>();
            param.put("srvOrdId", srvOrdId);// gom_bdw_srv_ord_info.srv_ord_id
            param.put("orderIds", null);// orderIds是个List, 存放的是 子流程的gom_order.order_id
            param.put("rollbackDesc", "集客下发异常单撤单"); // 回滚原因
            param.put("tacheId", ""); // 环节id
            Map retmap =  businessRollbackServiceIntf.resRollBack(param);
            if(!MapUtils.getBoolean(retmap,"success")){
                resultMap.put("respCode", "1");
                resultMap.put("respDesc", "异常单下发失败!调用资源回滚接口异常，异常原因：" + MapUtils.getString(retmap, "message"));
                return resultMap;
            }
        }
        resultMap.put("respDesc", "异常单通知调度成功");
        resultMap.put("respCode", "0");
        return resultMap;
    }


    /**
     *
     */
    public void updateJikeAttr(String srvOrdId, List<JiKeProdAttrDTO> jiKeProdAttrDTOList) {
        for (int i = 0; i < jiKeProdAttrDTOList.size(); i++) {
            JiKeProdAttrDTO jProdAttrDTO = jiKeProdAttrDTOList.get(i);
            if (!StringUtils.isEmpty(jProdAttrDTO.getAttrValue())) {
                exceptionFlowDao.updateJiKeProdAttr(srvOrdId, jProdAttrDTO.getAttrCode(), jProdAttrDTO.getAttrValue());
            }

        }
    }


    public String dateFormat(String date) {
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss"); //设置日期格式
            SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置日期格式
            if (StringUtils.isEmpty(date)) {
                return null;
            }
            else {
                Date oldDate = df.parse(date);
                return newFormat.format(oldDate);
            }
        }
        catch (ParseException e) {
            throw new RuntimeException("请检查时间格式");
        }
    }

    public Map updateSrvOrdState(String tradeId, String tradeIdReal, String activeType,
                                 String serialNumber, String requireCompleteCate) {
        Map<String, Object> resultMap = new HashMap<>();
        String orderId = "";
        String srvOrdId = "";
        String srvOrdState = "";
        List<Map<String, Object>> orderList = orderDealService.queryOrderList(tradeIdReal, serialNumber);
        if (orderList != null && orderList.size() > 0) {
            orderId = orderList.get(0).get("ORDER_ID").toString();
            srvOrdId = orderList.get(0).get("SRV_ORD_ID").toString();
            srvOrdState = MapUtils.getString(orderList.get(0),"SRV_ORD_STAT");
        }
        else {
            resultMap.put("respDesc", "异常单[tradeId : " + tradeId + "]未找到原单信息");
            resultMap.put("respCode", "1");
            resultMap.put("orderId", orderId);
            return resultMap;
        }
        // 订单交互类型 4B：加急；4C：延期；4D：撤业务订单;4E：挂起；4F：解挂;
        /*Set<String> activeTypeSet = new HashSet<String>();
        activeTypeSet.add("4B");
        activeTypeSet.add("4C");
        activeTypeSet.add("4D");
        activeTypeSet.add("4E");
        activeTypeSet.add("4F");*/
        if ("4E".equals(activeType) && "10N".equals(srvOrdState)) { //挂起  正常单才能挂起
         //   orderDealService.updateOrdInfo(activeType, tradeIdReal, serialNumber);
            // 异常单挂起解挂撤单，更新对应原单的电路表状态
            orderDealDao.updateSrvOrderStateById(activeType, Long.parseLong(orderId));
        }else if ("4F".equals(activeType) && "4E".equals(srvOrdState)){ //解挂  挂起单才能解挂
         //   orderDealService.updateOrdInfo("10N", tradeIdReal, serialNumber);
            orderDealDao.updateSrvOrderStateById("10N", Long.parseLong(orderId));
        }else if ("4D".equals(activeType)){ //撤单
        //    orderDealService.updateOrdInfo("10X", tradeIdReal, serialNumber);
            orderDealDao.updateSrvOrderStateById("10X", Long.parseLong(orderId));
        }
        else{
            resultMap.put("respDesc", "请核查[tradeId : " + tradeId + "]订单状态是否符合挂起解挂操作！");
            resultMap.put("respCode", "1");
            resultMap.put("orderId", orderId);
            return resultMap;
        }

        //修改订单的要求完成时间
        if (!"".equals(requireCompleteCate)) {
            orderDealService.updateFinDate(requireCompleteCate, orderId);
        }
        resultMap.put("respDesc", "异常单通知调度成功");
        resultMap.put("respCode", "0");
        return resultMap;
    }
}
