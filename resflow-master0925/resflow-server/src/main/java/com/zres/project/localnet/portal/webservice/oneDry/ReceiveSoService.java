package com.zres.project.localnet.portal.webservice.oneDry;


import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealService;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderSendMsgService;
import com.zres.project.localnet.portal.logInfo.entry.ToKafkaTacheLog;
import com.zres.project.localnet.portal.logInfo.service.ResInterfaceLogger;
import com.zres.project.localnet.portal.logInfo.until.LoggerThreadPool;
import com.zres.project.localnet.portal.resourceInitiate.service.ResSupplementDealServiceIntf;
import com.zres.project.localnet.portal.util.MapConverterUtil;
import com.zres.project.localnet.portal.webservice.data.dao.InterfaceBoDao;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.zres.project.localnet.portal.webservice.dto.CustomerInfoDTO;
import com.zres.project.localnet.portal.webservice.dto.DispatchInfoDTO;
import com.zres.project.localnet.portal.webservice.dto.ProdAttrDTO;
import com.zres.project.localnet.portal.webservice.dto.ProdInfoDTO;
import com.zres.project.localnet.portal.webservice.flow.ExceptionFlowIntf;
import com.zres.project.localnet.portal.webservice.res.BusinessRollbackServiceIntf;
import com.zres.project.localnet.portal.webservice.res.ResSuspendThread;
import com.ztesoft.res.frame.core.util.ListUtil;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * Created by jiangdebing on 2018/12/22.
 */
@Controller
@RequestMapping("/receiveSoServiceIntf")
public class ReceiveSoService implements ReceiveSoServiceIntf {

    Logger logger = LoggerFactory.getLogger(ReceiveSoService.class);

    @Autowired
    private OrderDealService orderDealService;
    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private JdbcTemplate springJdbcTemplate;
    @Autowired
    private ExceptionFlowIntf exceptionFlowIntf;
    @Autowired
    private BusinessRollbackServiceIntf businessRollbackServiceIntf;
    @Autowired
    private OrderSendMsgService orderSendMsgService;
    @Autowired
    private InterfaceBoDao interfaceBoDao;
    @Autowired
    private ResSupplementDealServiceIntf resSupplementDealServiceIntf;
    @Autowired
    private ResInterfaceLogger resInterfaceLogger;
    @IgnoreSession
    @ResponseBody
    @RequestMapping(value = "/interfaceBDW/receiveSo.spr", method = RequestMethod.POST, produces = "application/xml;charset=UTF-8")
    public String receiveSo(@RequestBody String xml) {
        String returnValue = "";
        int gbcoId = wsd.querySequence("SEQ_GOM_BDW_CST_ORD.NEXTVAL"); //获取客户表序列
        GetContentDto gcdto = new GetContentDto(xml);
        Map<String, Object> header = gcdto.getHeader(); //获取header节点信息
        Map<String, Object> serviceOrderInfo = gcdto.getServiceOrderInfo(); //获取服务定单信息
        String tradeTypeCode = MapUtils.getString(serviceOrderInfo, "groupOrderCode");
        int gbdoId = wsd.querySequence("SEQ_GOM_BDW_DISPATCH_ORDER.NEXTVAL"); //获取调单表序列
        Map<String, Object> customerInfo = gcdto.getCustomerInfo(); //获取报文客户信息
        Map<String, Object> dispatchInfo = gcdto.getDispatchInfo(); //获取调单信息
        List<Map<String, Object>> prodInfo = gcdto.getProdInfo(); //获取电路信息
        List<Map<String, Object>> attachment = new ArrayList<Map<String, Object>>(); //报文中附件信息
        String crmProdInstId = ""; //电路明细编号
        String circuitcode = ""; //电路编号
        String provincea = ""; //A端城市
        String provincez = ""; //Z端城市
        String citya = ""; //A端城市
        String cityz = ""; //Z端城市
        String receptionAreaId = ""; //受理区域ID
        String receptionAreaName = ""; //受理区域名称
        List<Map<String, Object>> repeatList = new ArrayList<Map<String, Object>>(); //重单数据
        String onedryproducttype = ""; //一干产品类型（中文名称）
        List<String> orderIdList = new ArrayList();//发送短信入参
        int logId = wsd.querySequence("SEQ_GOM_BDW_INTERF_LOG_INFO.NEXTVAL"); //获取接口日志信息表序列
        Map<String, Object> interflog = new HashMap<String, Object>();
        interflog.put("INTERFNAME", "服务定单处理（包含正常单和所有异常单的发送）");
        interflog.put("URL", "/receiveSoServiceIntf/interfaceBDW/receiveSo.spr");
        interflog.put("CONTENT", xml);
        interflog.put("ORDERNO", serviceOrderInfo.get("orderTitle"));
        interflog.put("ID", logId);
        interflog.put("REMARK", "接收一干xml报文");
        wsd.insertInterfLogS(interflog);//保存日志报文
        if ("114".equals(serviceOrderInfo.get("orderType"))) { //全程调测退单
            for (int tddl = 0; tddl < prodInfo.size(); tddl++) {
                List<Map<String, Object>> returnList = springJdbcTemplate.queryForList("SELECT * FROM (SELECT W.WO_ID,O.ORDER_ID,WS.ORD_PS_ID,UT.ID,UT.TACHE_NAME,G.MAINORG,ROWNUM rn FROM GOM_ORDER O JOIN GOM_BDW_SRV_ORD_INFO G ON G.ORDER_ID = O.ORDER_ID JOIN GOM_WO W ON W.ORDER_ID = O.ORDER_ID JOIN GOM_BDW_SRV_ORD_INFO I ON I.ORDER_ID = O.ORDER_ID JOIN GOM_PS_2_WO_S WS ON WS.ID = W.PS_ID JOIN UOS_TACHE UT ON UT.ID = WS.TACHE_ID WHERE G.TRADE_ID = '" + prodInfo.get(tddl).get("prodInstId") + "' AND  G.TRADE_TYPE_CODE = '" + serviceOrderInfo.get("orderCode") + "' AND  W.WO_STATE = '290000002' AND UT.ID = 510101046 ORDER BY G.CREATE_DATE,W.CREATE_DATE DESC) WHERE RN = 1");
                List<Map<String, Object>> mainOrg = springJdbcTemplate.queryForList("SELECT SORT_NO AS AREA_ID FROM GOM_BDW_CODE_INFO_SECOND WHERE CODE_TYPE='PROVINCE' AND CODE_TYPE_NAME='" + header.get("province") + "' AND REMARK = '" + prodInfo.get(tddl).get("mainOrg") + "'"); //查询主调局编码
                if (judgeNULL(returnList)) {
                    if (judgeNULL(mainOrg) && returnList.get(0).get("MAINORG").equals(mainOrg.get(0).get("AREA_ID"))) { //如果此单主调局是本省只改电路表的FLOW_ID（给一干回单的工单ID）
                        springJdbcTemplate.update("UPDATE GOM_BDW_SRV_ORD_INFO SET FLOW_ID = " + prodInfo.get(tddl).get("woOrderCode") + " WHERE ORDER_ID = " + returnList.get(0).get("ORDER_ID"));
                        continue;
                    }
                    else {
                        springJdbcTemplate.update("UPDATE GOM_BDW_SRV_ORD_INFO SET FLOW_ID = " + prodInfo.get(tddl).get("woOrderCode") + " WHERE ORDER_ID = " + returnList.get(0).get("ORDER_ID"));
                        Map<String, Object> rentMap = new HashMap<String, Object>();
                        rentMap.put("tacheId", returnList.get(0).get("ID"));
                        rentMap.put("woId", returnList.get(0).get("WO_ID"));
                        rentMap.put("orderId", returnList.get(0).get("ORDER_ID"));
                        rentMap.put("flag", "一干");
                        rentMap.put("remark", "一干下发了退单通知");
                       /* Map<String, Object> operAttrsVal = new HashMap<String, Object>();
                        operAttrsVal.put("ifRentBack ", "0");
                        rentMap.put("operAttrsVal", operAttrsVal);
                        Map<String, Object> tacheOperInfo = new HashMap<String, Object>();
                        tacheOperInfo.put("operFlag", "退单成功");
                        tacheOperInfo.put("remark", "一干下发了退单通知");
                        rentMap.put("tacheOperInfo", tacheOperInfo);
                        rentMap.put("actionFlag", "complateWo");*/
                        orderDealService.rollBackWoOrder(rentMap);
                    }
                }
                else {
                    returnValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><resultFlag>1</resultFlag><resultContent>全程调测退单失败，没有此单的信息</resultContent></root>";
                    interflog.put("RETURNCONTENT", returnValue);
                    wsd.updateInterfLog(interflog);
                    return returnValue;
                }
            }
            returnValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><resultFlag>0</resultFlag><resultContent>全程调测退单成功</resultContent></root>";
            interflog.put("RETURNCONTENT", returnValue);
            wsd.updateInterfLog(interflog);
            return returnValue;
        }
        else if (!"101".equals("" + serviceOrderInfo.get("orderType"))) { //异常单流程
            CustomerInfoDTO newCustomerInfoDTO = MapConverterUtil.convertMap2Bean(customerInfo, CustomerInfoDTO.class);
            DispatchInfoDTO newDispatchInfoDTO = MapConverterUtil.convertMap2Bean(dispatchInfo, DispatchInfoDTO.class);
            List<ProdInfoDTO> newProdInfoDTOList = new ArrayList<ProdInfoDTO>();
            String provinceId = MapUtils.getString(header, "province");
            for(Map<String, Object> prodInfoMap : prodInfo){
                ProdInfoDTO prodInfoDTO = MapConverterUtil.convertMap2Bean(prodInfoMap,ProdInfoDTO.class);
                Map<String, Object> attrs = (Map<String, Object>)prodInfoMap.get("prodAttrs");
                List<Map<String, Object>> attr = (List<Map<String, Object>>)attrs.get("prodAttr");
                StringBuilder crm_prod_inst_id = new StringBuilder();
                int yc = 0;
                for (int ycxx = 0; ycxx < attr.size(); ycxx++) {
                    Map<String, Object> prods = (Map<String, Object>) attr.get(ycxx);
                    if ("crm_prod_inst_id".equals(prods.get("code"))) {
                        if(yc==0){
                            crm_prod_inst_id.append("'" + prods.get("value") + "'");
                        }else {
                            crm_prod_inst_id.append(",'" + prods.get("value") + "'");
                        }
                        Map<String, Object> resultMap = new HashMap<>();
                        List<Map<String, Object>> originalList = springJdbcTemplate.queryForList("SELECT GBSOI.SRV_ORD_ID, GBSOI.ORDER_ID, GBCO.CST_ORD_ID FROM GOM_BDW_CST_ORD GBCO JOIN GOM_BDW_SRV_ORD_INFO GBSOI ON GBSOI.CST_ORD_ID=GBCO.CST_ORD_ID AND GBSOI.ORDER_TYPE='101' AND GBCO.ONEDRY_AREA_CODE='" + header.get("province") + "' AND GBSOI.CRM_PROD_INST_ID='" + prods.get("value") + "' AND GBSOI.TRADE_TYPE_CODE = '" + serviceOrderInfo.get("groupOrderCode") + "'");
                        if(judgeNULL(originalList) && judgeNULL( originalList.get(0))) {
                            String srvOrdStat = "10N";
                            List<Map<String, Object>> dispatchList = springJdbcTemplate.queryForList("SELECT DISPATCH_ORDER_ID FROM GOM_BDW_DISPATCH_ORDER WHERE DISPATCH_SOURCE='onedry' AND CST_ORD_ID=" +  originalList.get(0).get("CST_ORD_ID"));
                            if (judgeNULL("" + serviceOrderInfo.get("attachs"))&&ycxx==0) {
                                Map<String, Object> serviceOrderInfoAttachs = (Map<String, Object>) serviceOrderInfo.get("attachs"); //异常单serviceOrderInfo节点下的附件
                                if (judgeNULL(serviceOrderInfoAttachs)) { //异常单循环处理serviceOrderInfo节点下的附件
                                    try {
                                        List<Map<String, Object>> serviceOrderInfoAttach = (List<Map<String, Object>>) serviceOrderInfoAttachs.get("attach");
                                        for (int i = 0; i < serviceOrderInfoAttach.size(); i++) {
                                            serviceOrderInfoAttach.get(i).put("srv_ord_id", 0);
                                            serviceOrderInfoAttach.get(i).put("dispatch_order_id", 0);
                                            serviceOrderInfoAttach.get(i).put("cst_ord_id", originalList.get(0).get("CST_ORD_ID"));
                                            serviceOrderInfoAttach.get(i).put("origin", "CUSTOMER");
                                            attachment.add(serviceOrderInfoAttach.get(i));
                                        }
                                    }
                                    catch (Exception e) {
                                        Map<String, Object> serviceOrderInfoAttach = (Map<String, Object>) serviceOrderInfoAttachs.get("attach");
                                        serviceOrderInfoAttach.put("srv_ord_id", 0);
                                        serviceOrderInfoAttach.put("dispatch_order_id", 0);
                                        serviceOrderInfoAttach.put("cst_ord_id", originalList.get(0).get("CST_ORD_ID"));
                                        serviceOrderInfoAttach.put("origin", "CUSTOMER");
                                        attachment.add(serviceOrderInfoAttach);
                                    }
                                }
                            }
                            if (judgeNULL("" + dispatchInfo.get("attachs"))&&ycxx==0) {
                                Map<String, Object> dispatchInfoAttachs = (Map<String, Object>) dispatchInfo.get("attachs"); //异常单dispatchInfo节点下的附件
                                if (judgeNULL(dispatchInfoAttachs)) { //异常单循环处理dispatchInfo节点下的附件
                                    try {
                                        List<Map<String, Object>> dispatchInfoAttachsAttach = (List<Map<String, Object>>) dispatchInfoAttachs.get("attach");
                                        for (int i = 0; i < dispatchInfoAttachsAttach.size(); i++) {
                                            dispatchInfoAttachsAttach.get(i).put("srv_ord_id", 0);
                                            dispatchInfoAttachsAttach.get(i).put("dispatch_order_id", dispatchList.get(0).get("DISPATCH_ORDER_ID"));
                                            dispatchInfoAttachsAttach.get(i).put("cst_ord_id", 0);
                                            dispatchInfoAttachsAttach.get(i).put("origin", "DISPATCH");
                                            attachment.add(dispatchInfoAttachsAttach.get(i));
                                        }
                                    }
                                    catch (Exception e) {
                                        Map<String, Object> dispatchInfoAttachsAttach = (Map<String, Object>) dispatchInfoAttachs.get("attach");
                                        dispatchInfoAttachsAttach.put("srv_ord_id", 0);
                                        dispatchInfoAttachsAttach.put("dispatch_order_id", dispatchList.get(0).get("DISPATCH_ORDER_ID"));
                                        dispatchInfoAttachsAttach.put("cst_ord_id", 0);
                                        dispatchInfoAttachsAttach.put("origin", "DISPATCH");
                                        attachment.add(dispatchInfoAttachsAttach);
                                    }
                                }
                            }
                            if (judgeNULL(""+prodInfoMap.get("attachs"))) {
                                Map<String, Object> prodInfoAttachs = (Map<String, Object>) prodInfoMap.get("attachs"); //异常单prodInfo节点下的附件
                                if (judgeNULL(prodInfoAttachs)) { //异常单循环处理prodInfoInfo节点下的附件
                                    try {
                                        List<Map<String, Object>> prodInfoAttach = (List<Map<String, Object>>) prodInfoAttachs.get("attach");
                                        for (int i = 0; i < prodInfoAttach.size(); i++) {
                                            prodInfoAttach.get(i).put("srv_ord_id", originalList.get(0).get("SRV_ORD_ID"));
                                            prodInfoAttach.get(i).put("dispatch_order_id", 0);
                                            prodInfoAttach.get(i).put("cst_ord_id", 0);
                                            prodInfoAttach.get(i).put("origin", "FQ");
                                            attachment.add(prodInfoAttach.get(i));
                                        }
                                    }
                                    catch (Exception e) {
                                        Map<String, Object> prodInfoAttach = (Map<String, Object>) prodInfoAttachs.get("attach");
                                        prodInfoAttach.put("srv_ord_id", originalList.get(0).get("SRV_ORD_ID"));
                                        prodInfoAttach.put("dispatch_order_id", 0);
                                        prodInfoAttach.put("cst_ord_id", 0);
                                        prodInfoAttach.put("origin", "FQ");
                                        attachment.add(prodInfoAttach);
                                    }
                                }
                            }
                            String orderId =  MapUtils.getString(originalList.get(0), "ORDER_ID");
                            if ("108".equals("" + serviceOrderInfo.get("orderType")) || "109".equals("" + serviceOrderInfo.get("orderType"))) { //加急或延期
                                List<Map<String, Object>> resultList = springJdbcTemplate.queryForList("SELECT  WS.TACHE_ID,WO.WO_ID FROM GOM_WO WO LEFT JOIN GOM_PS_2_WO_S WS ON WS.ID = WO.PS_ID LEFT JOIN UOS_TACHE UT ON UT.ID = WS.TACHE_ID WHERE WO.WO_ID=(SELECT MAX(WO_ID) FROM GOM_WO WHERE ORDER_ID =" + orderId + ")");
                                Map<String, Object> rentMap = new HashMap<String, Object>();
                                rentMap.put("tacheId", resultList.get(0).get("TACHE_ID"));
                                rentMap.put("woId", resultList.get(0).get("WO_ID"));
                                rentMap.put("orderId", orderId);
                                rentMap.put("srvOrdId", MapUtils.getString(originalList.get(0), "SRV_ORD_ID"));
                                rentMap.put("remark", "一干下发本地加急延期需要回退到完工汇总环节进行二次报竣");

                                try {
                                    Map<String, Object> rollBackMap = orderDealService.exceptionOrderNoticeBack(rentMap);
                                    if(MapUtils.getBoolean(rollBackMap, "success")){

                                    }
                                    else{
                                        returnValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><resultFlag>1</resultFlag><resultContent>加急延期流程回退失败 : "+ MapUtils.getString(rollBackMap, "message") +"</resultContent></root>";
                                        interflog.put("RETURNCONTENT", returnValue);
                                    }
                                } catch (Exception e) {
                                    returnValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><resultFlag>1</resultFlag><resultContent>加急延期流程回退失败"+e.getMessage()+"</resultContent></root>";
                                    interflog.put("RETURNCONTENT", returnValue);
                                }
                            }
                            else if ("110".equals("" + serviceOrderInfo.get("orderType"))) { //挂起
                                srvOrdStat="4E";
                            }
                            else if ("111".equals("" + serviceOrderInfo.get("orderType"))) { //解挂

                                for (int jgxx = 0; jgxx < attr.size(); jgxx++) {
                                    Map<String, Object> jgprods = (Map<String, Object>) attr.get(jgxx);
                                    if ("rfsdate".equals("" + jgprods.get("code"))) {
                                        springJdbcTemplate.update("UPDATE GOM_BDW_SRV_ORD_ATTR_INFO SET ATTR_VALUE='" + jgprods.get("value") + "' WHERE SOURSE='onedry' AND ATTR_VALUE_NAME='rfsdate' AND SRV_ORD_ID=" + originalList.get(0).get("SRV_ORD_ID"));
                                    }
                                    else if ("applydatea".equals("" + jgprods.get("code"))) {
                                        springJdbcTemplate.update("UPDATE GOM_BDW_SRV_ORD_ATTR_INFO SET ATTR_VALUE='" + jgprods.get("value") + "' WHERE SOURSE='onedry' AND ATTR_VALUE_NAME='applydatea' AND SRV_ORD_ID=" + originalList.get(0).get("SRV_ORD_ID"));
                                    }
                                    else if ("applydatez".equals("" + jgprods.get("code"))) {
                                        springJdbcTemplate.update("UPDATE GOM_BDW_SRV_ORD_ATTR_INFO SET ATTR_VALUE='" + jgprods.get("value") + "' WHERE SOURSE='onedry' AND ATTR_VALUE_NAME='applydatez' AND SRV_ORD_ID=" + originalList.get(0).get("SRV_ORD_ID"));
                                    }
                                }
                            }
                            else if ("112".equals("" + serviceOrderInfo.get("orderType"))) { //作废
//                                srvOrdStat="10X";
                                Map<String, Object> param = new HashMap<>();
                                param.put("srvOrdId", originalList.get(0).get("SRV_ORD_ID"));// gom_bdw_srv_ord_info.srv_ord_id
                                param.put("orderIds", null);// orderIds是个List, 存放的是 子流程的gom_order.order_id
                                param.put("rollbackDesc", "一干下发作废单"); // 回滚原因
                                /*1.查询是否下发本地网；2.如果下发本地网，需要先回滚本地网的资源信息；3.回滚二干系统的资源信息；*/
                                List<Map<String,Object>> localList = interfaceBoDao.qryLocalInfo(originalList.get(0).get("SRV_ORD_ID") + "");
                                if(judgeNULL(localList)){
                                    // 本地网回滚
                                    for(Map<String,Object> temp : localList){
                                        Map<String, Object> params = new HashMap<>();
                                        params.put("srvOrdId", MapUtils.getString(temp,"RELATE_INFO_ID",""));// gom_bdw_srv_ord_info.srv_ord_id
                                        params.put("orderIds", null);// orderIds是个List, 存放的是 子流程的gom_order.order_id
                                        params.put("rollbackDesc", "一干下发作废单"); // 回滚原因
                                        resultMap = businessRollbackServiceIntf.resRollBack(params);
                                        if("1".equals(MapUtils.getString(resultMap,"respCode"))){
                                            resultMap.put("respCode", "1");
                                            resultMap.put("respDesc", "异常单下发失败!" + MapUtils.getString(resultMap, "message"));
                                        }
                                    }
                                }
                                resultMap = businessRollbackServiceIntf.resRollBack(param);
                                if (!MapUtils.getBoolean(resultMap, "success")) {
                                    resultMap.put("respCode", "1");
                                    resultMap.put("respDesc", "异常单下发失败!" + MapUtils.getString(resultMap, "message"));
                                    interflog.put("RETURNCONTENT", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><resultFlag>0</resultFlag><resultContent>作废单调取资源回滚接口失败：" + MapUtils.getString(resultMap, "message") + "</resultContent></root>");
                                    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><resultFlag>0</resultFlag><resultContent>作废单调取资源回滚接口失败：" + MapUtils.getString(resultMap, "message") + "</resultContent></root>";
                                }
                            }
                            //追单
                            else if ("104".equals("" + serviceOrderInfo.get("orderType"))) {
                                if(judgeNULL("" + dispatchInfo.get("changeBeforeText")) || judgeNULL("" + dispatchInfo.get("changeAfterText"))) { //追加原单调单信息
                                    if (judgeNULL(dispatchList) && judgeNULL(dispatchList.get(0))) {
                                        StringBuilder dispatchText = new StringBuilder("@enter@追单调单内容：");
                                        if (judgeNULL(dispatchInfo.get("changeBeforeText"))){
                                            dispatchText.append("@enter@变更前：" + dispatchInfo.get("changeBeforeText"));
                                        }
                                        if(judgeNULL(dispatchInfo.get("changeAfterText"))){
                                            dispatchText.append("@enter@变更后：" + dispatchInfo.get("changeAfterText"));
                                        }
                                        springJdbcTemplate.update("UPDATE GOM_BDW_DISPATCH_ORDER SET DISPATCH_TEXT = DISPATCH_TEXT||'" + dispatchText.toString() + "' WHERE DISPATCH_ORDER_ID=" + dispatchList.get(0).get("DISPATCH_ORDER_ID"));
                                    }
                                }
                                for (int zdxx = 0; zdxx < attr.size(); zdxx++) { //覆盖原单路由信息
                                    Map<String, Object> zdprods = (Map<String, Object>) attr.get(zdxx);
                                    if ("simpleRoute".equals("" + zdprods.get("code")) || "ordsimpleRoute".equals("" + zdprods.get("code")) || "oldSimpleRoute".equals("" + zdprods.get("code"))) {
                                        if (judgeNULL("" + zdprods.get("value"))) {
                                            if ("ordsimpleRoute".equals("" + zdprods.get("code")) || "oldSimpleRoute".equals("" + zdprods.get("code"))) {
                                                springJdbcTemplate.update("UPDATE GOM_BDW_SRV_ORD_RES_ROUTE SET ATTR_VALUE='" + zdprods.get("value") + "' WHERE ATTR_CODE='onedrySimpleRoute' AND RESOURCES='oldResource' AND SRV_ORD_ID=" + originalList.get(0).get("SRV_ORD_ID"));
                                            } else {
                                                springJdbcTemplate.update("UPDATE GOM_BDW_SRV_ORD_RES_ROUTE SET ATTR_VALUE='" + zdprods.get("value") + "' WHERE ATTR_CODE='onedrySimpleRoute' AND RESOURCES='newResource' AND SRV_ORD_ID=" + originalList.get(0).get("SRV_ORD_ID"));
                                            }
                                        }
                                        continue;
                                    }
                                    if ("fullRoute".equals("" + zdprods.get("code")) || "oldfullRoute".equals("" + zdprods.get("code")) || "oldFullRoute".equals("" + zdprods.get("code"))) {
                                        if (judgeNULL("" + zdprods.get("value"))) {
                                            if ("oldfullRoute".equals("" + zdprods.get("code")) || "oldFullRoute".equals("" + zdprods.get("code"))) {
                                                springJdbcTemplate.update("UPDATE GOM_BDW_SRV_ORD_RES_ROUTE SET ATTR_VALUE='" + zdprods.get("value") + "' WHERE ATTR_CODE='onedryFullRoute' AND RESOURCES='oldResource' AND SRV_ORD_ID=" + originalList.get(0).get("SRV_ORD_ID"));
                                            } else {
                                                springJdbcTemplate.update("UPDATE GOM_BDW_SRV_ORD_RES_ROUTE SET ATTR_VALUE='" + zdprods.get("value") + "' WHERE ATTR_CODE='onedryFullRoute' AND RESOURCES='newResource' AND SRV_ORD_ID=" + originalList.get(0).get("SRV_ORD_ID"));
                                            }
                                        }
                                        continue;
                                    }
                                    if ("route".equals("" + zdprods.get("code"))) {
                                        if (judgeNULL("" + zdprods.get("value"))) {
                                            springJdbcTemplate.update("UPDATE GOM_BDW_SRV_ORD_RES_ROUTE SET ATTR_VALUE='" + zdprods.get("value") + "' WHERE ATTR_CODE='onedryRoute' AND RESOURCES='newResource' AND SRV_ORD_ID=" + originalList.get(0).get("SRV_ORD_ID"));
                                        }
                                    }
                                }
                                orderId =  MapUtils.getString(originalList.get(0), "ORDER_ID");
                                List<Map<String, Object>> countWoList = springJdbcTemplate.queryForList("SELECT WO.WO_ID FROM GOM_WO WO LEFT JOIN GOM_PS_2_WO_S WS  ON WS.ID = WO.PS_ID LEFT JOIN UOS_TACHE UT ON UT.ID = WS.TACHE_ID WHERE WS.TACHE_ID = '510101044' AND ORDER_ID =" + orderId);
                                if (judgeNULL(countWoList)) {
                                    List<Map<String, Object>> woList = springJdbcTemplate.queryForList("SELECT WO.WO_ID FROM GOM_WO WO LEFT JOIN GOM_PS_2_WO_S WS  ON WS.ID = WO.PS_ID LEFT JOIN UOS_TACHE UT ON UT.ID = WS.TACHE_ID WHERE WS.TACHE_ID = '510101044' AND WO.WO_STATE = '290000002'  AND ORDER_ID =" + orderId);
                                    if (!judgeNULL(woList)) {
                                        List<Map<String, Object>> resultList = springJdbcTemplate.queryForList("SELECT  WS.TACHE_ID,WO.WO_ID FROM GOM_WO WO LEFT JOIN GOM_PS_2_WO_S WS ON WS.ID = WO.PS_ID LEFT JOIN UOS_TACHE UT ON UT.ID = WS.TACHE_ID WHERE WO.WO_ID=(SELECT MAX(WO_ID) FROM GOM_WO WHERE ORDER_ID =" + orderId + ")");
                                        Map<String, Object> rentMap = new HashMap<String, Object>();
                                        rentMap.put("tacheId", resultList.get(0).get("TACHE_ID"));
                                        rentMap.put("woId", resultList.get(0).get("WO_ID"));
                                        rentMap.put("orderId", orderId);
                                        rentMap.put("srvOrdId", MapUtils.getString(originalList.get(0), "SRV_ORD_ID"));
                                        rentMap.put("remark", "一干下发本地追单需要回退到完工汇总环节进行二次报竣");

                                        try {
                                            Map<String, Object> rollBackMap = orderDealService.exceptionOrderNoticeBack(rentMap);
                                            if(MapUtils.getBoolean(rollBackMap, "success")){

                                            }
                                            else{
                                                returnValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><resultFlag>1</resultFlag><resultContent>追单流程回退失败 : "+ MapUtils.getString(rollBackMap, "message") +"</resultContent></root>";
                                                interflog.put("RETURNCONTENT", returnValue);
                                            }
                                        } catch (Exception e) {
                                            returnValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><resultFlag>1</resultFlag><resultContent>追单流程回退失败"+e.getMessage()+"</resultContent></root>";
                                            interflog.put("RETURNCONTENT", returnValue);
                                        }
                                    }
                                }
                            }
                            springJdbcTemplate.update("UPDATE GOM_BDW_SRV_ORD_INFO SET SRV_ORD_STAT='" + srvOrdStat + "' WHERE SRV_ORD_ID=" + originalList.get(0).get("SRV_ORD_ID"));
                        }
                        yc++;
                    }
                }
                if(yc>0) {
                    Map ydxxMap = springJdbcTemplate.queryForMap("SELECT COUNT(SRV_ORD_ID) AS YCSL FROM GOM_BDW_SRV_ORD_INFO WHERE CRM_PROD_INST_ID IN (" + crm_prod_inst_id.toString() + ") AND ORDER_TYPE='101'");
                    if("0".equals(ydxxMap.get("YCSL") + "")){
                        returnValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><resultFlag>0</resultFlag><resultContent>异常单电路信息原单未系统中</resultContent></root>";
                        interflog.put("RETURNCONTENT", returnValue);
                        wsd.updateInterfLog(interflog);
                        return returnValue;
                    }
                }
                else
                {
                    returnValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><resultFlag>0</resultFlag><resultContent>异常单没有任何电路信息</resultContent></root>";
                    interflog.put("RETURNCONTENT", returnValue);
                    wsd.updateInterfLog(interflog);
                    return returnValue;
                }
                List<ProdAttrDTO> prodAttrDTOList = MapConverterUtil.convertListMap2ListBean(attr, ProdAttrDTO.class);
                prodInfoDTO.setProdAttrDTOList(prodAttrDTOList);
                prodInfoDTO.setOnedryAreaCode(provinceId);
                prodInfoDTO.setTradeTypeCode(tradeTypeCode);
                newProdInfoDTOList.add(prodInfoDTO);
            }
            if(judgeNULL(attachment)) {
                HandleAttachment yccircuit = new HandleAttachment(attachment, "JT_FTP_INFO", "FTP_INFO", "ONEDRY"); //创建异步处理附件
                yccircuit.start(); //启动异步处理附件
            }
            exceptionFlowIntf.exceptionFlowChange(serviceOrderInfo.get("orderType") + "", newCustomerInfoDTO, newDispatchInfoDTO, newProdInfoDTOList);

            for(ProdInfoDTO prodInfoDTO : newProdInfoDTOList){
                String chgOrderId =  prodInfoDTO.getChgOrderId();
                if (!StringUtils.isEmpty(chgOrderId) && !"null".equals(chgOrderId)){
                    orderIdList.add(chgOrderId);
                }
            }
            if(!ListUtil.isEmpty(orderIdList)){
                logger.info("开始准备发送短信。。。。。。。。。。。。。。。。。。。。");
                Map<String, Object> sendMsgMap = new HashMap<String, Object>();
                sendMsgMap.put("areaId", header.get("province"));
                sendMsgMap.put("orderIdList", orderIdList);
                sendMsgMap.put("operAction", "一干_追单");
                sendMsgMap.put("orderType", "SEC_EXCEPTION");
                sendMsgMap.put("resources", "onedry");
                orderSendMsgService.sendMsgBefore(sendMsgMap);
            }
            returnValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><resultFlag>0</resultFlag><resultContent>异常定单接收处理成功</resultContent></root>";
            interflog.put("RETURNCONTENT", returnValue);
            wsd.updateInterfLog(interflog);
            return returnValue;
        }
        else {
            for (int dl = 0; dl < prodInfo.size(); dl++) {
                String gbsoiId = springJdbcTemplate.queryForMap(" SELECT SEQ_GOM_BDW_SRV_ORD_INFO.NEXTVAL FROM DUAL").get("NEXTVAL") + ""; //获取电路表序列
                onedryproducttype = ""+prodInfo.get(dl).get("prodName");
                String prodType = prodInfo.get(dl).get("prodType") + "";
                if ("10000014".equals(prodInfo.get(dl).get("prodType"))) { //10000014一干中继对应20181221006局内中继电路
                    prodType = "20181221006";
                }
                else if ("10000007".equals(prodInfo.get(dl).get("prodType"))) { //10000007一干ATM对应20181221003基础数据(ATM)
                    prodType = "20181221003";
                }
                else if ("10000005".equals(prodInfo.get(dl).get("prodType"))) { //10000005一干ATM对应20181221004基础数据(FR)
                    prodType = "20181221004";
                }
                else if ("10000003".equals(prodInfo.get(dl).get("prodType"))) { //10000003一干ATM对应20181221005基础数据(DDN)
                    prodType = "20181221005";
                }
                Map<String, Object> param = new HashMap<String, Object>();
                Map<String, Object> prodAttrs = (Map<String, Object>) prodInfo.get(dl).get("prodAttrs");
                List<Map<String, Object>> prodAttr = (List<Map<String, Object>>) prodAttrs.get("prodAttr");
                List<Map<String, Object>> services = springJdbcTemplate.queryForList("SELECT SORT_NO FROM GOM_BDW_CODE_INFO_SECOND WHERE CODE_TYPE='flow_secondary_cross' AND CODE_TYPE_NAME='" + prodType + "' AND CODE_VALUE='" + prodInfo.get(dl).get("actType") + "' AND CODE_CONTENT='" + serviceOrderInfo.get("orderType") + "'");
                if (judgeNULL(services)) {
                    param.put("ordPsid", services.get(0).get("SORT_NO"));
                }
                else {
                    returnValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><resultFlag>1</resultFlag>此单没查到相应流程<resultContent></resultContent></root>";
                    interflog.put("RETURNCONTENT", returnValue);
                    wsd.updateInterfLog(interflog);
                    return returnValue;
                }
                String OrderId = "0";
                String requFineTime = "";
                if (!judgeNULL(""+serviceOrderInfo.get("orderTitle"))) {
                        returnValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><resultFlag>1</resultFlag>orderTitle节点为空<resultContent></resultContent></root>";
                        interflog.put("RETURNCONTENT", returnValue);
                        wsd.updateInterfLog(interflog);
                        return returnValue;
                    }
                    else {
                        param.put("ORDER_CONTENT", serviceOrderInfo.get("orderContent"));
                        param.put("AREA", "350002000000000042766408"); //目前配置的是全国
                        Map<String, Object> province = springJdbcTemplate.queryForMap("SELECT CODE_VALUE AS REGION_ID FROM GOM_BDW_CODE_INFO_SECOND WHERE CODE_TYPE='PROVINCE' AND CODE_TYPE_NAME='" + header.get("province") + "'");
                        province.put("PRODUCT_TYPE", prodType);
                        province.put("ACT_TYPE",  prodInfo.get(dl).get("actType").toString() + services.get(0).get("SORT_NO"));
                        param.put("attr", province);
                        int bk = 0;
                        for (int dlbh = 0; dlbh < prodAttr.size(); dlbh++) {
                            Map<String, Object> prods = (Map<String, Object>) prodAttr.get(dlbh);
                            if ("crm_prod_inst_id".equals(prods.get("code"))) {
                                param.put("ORDER_TITLE", prods.get("value"));
                                crmProdInstId = prods.get("value") + "";
                                bk++;
                            }
                            else if ("circuitcode".equals(prods.get("code"))) {
                                circuitcode = prods.get("value") + "";
                                bk++;
                            }
                            else if ("provincea".equals(prods.get("code"))) {
                                provincea = prods.get("value") + "";
                                bk++;
                            }
                            else if ("provincez".equals(prods.get("code"))) {
                                provincez = prods.get("value") + "";
                                bk++;
                            }
                            else if ("citya".equals(prods.get("code"))) {
                                citya = prods.get("value") + "";
                                bk++;
                            }
                            else if ("cityz".equals(prods.get("code"))) {
                                cityz = prods.get("value") + "";
                                bk++;
                            }
                            else if ("rfsdate".equals(prods.get("code"))) {
                                requFineTime = prods.get("value") + " 23:59:59";
                                param.put("requFineTime", requFineTime);
                                bk++;
                            }
                            if (bk == 7) {
                                break;
                            }
                        }
                            repeatList = springJdbcTemplate.queryForList("SELECT SRV_ORD_ID FROM GOM_BDW_SRV_ORD_INFO S JOIN GOM_BDW_CST_ORD C ON C.CST_ORD_ID=S.CST_ORD_ID WHERE S.TRADE_ID = '" + prodInfo.get(dl).get("prodInstId") + "' AND  S.TRADE_TYPE_CODE = '" + serviceOrderInfo.get("orderCode") + "' AND S.CRM_PROD_INST_ID='" + crmProdInstId + "' AND C.ONEDRY_AREA_CODE='" + header.get("province") + "'");
                            if (judgeNULL(repeatList)) { //重单修改数据非增加数据
                                springJdbcTemplate.update("UPDATE GOM_BDW_SRV_ORD_INFO SET QCWOORDERCODE = NULL ,FLOW_ID='" + prodInfo.get(dl).get("woOrderCode") + "' ，OMORDERCODE='" + prodInfo.get(dl).get("omOrderCode") + "' WHERE SRV_ORD_ID=" + repeatList.get(0).get("SRV_ORD_ID"));
                                continue;
                            }
                            Map retMap = orderDealService.createOrder(param); //启动流程方法
                            OrderId = (String) retMap.get("orderId");
                            orderIdList.add(OrderId);
                    // 资源补录单子挂起
                    Map<String, Object> suspendParam = new HashMap<>();
                    suspendParam.put("instanceId",crmProdInstId);
                    suspendParam.put("activeType",MapUtils.getString(prodInfo.get(dl),"actType"));
                    /* ResSuspendThread resSuspendThread = new ResSuspendThread(suspendParam);
                    resSuspendThread.start();*/
                    resSupplementDealServiceIntf.supplementStop(suspendParam);
                }
                if (judgeNULL("" + serviceOrderInfo.get("attachs"))&&dl==0) {
                    Map<String, Object> serviceOrderInfoAttachs = (Map<String, Object>) serviceOrderInfo.get("attachs"); //serviceOrderInfo节点下的附件
                    if (judgeNULL(serviceOrderInfoAttachs)) { //循环处理serviceOrderInfo节点下的附件
                        try {
                            List<Map<String, Object>> serviceOrderInfoAttach = (List<Map<String, Object>>) serviceOrderInfoAttachs.get("attach");
                            for (int i = 0; i < serviceOrderInfoAttach.size(); i++) {
                                serviceOrderInfoAttach.get(i).put("srv_ord_id", 0);
                                serviceOrderInfoAttach.get(i).put("dispatch_order_id", 0);
                                serviceOrderInfoAttach.get(i).put("cst_ord_id", gbcoId);
                                serviceOrderInfoAttach.get(i).put("origin", "CUSTOMER");
                                attachment.add(serviceOrderInfoAttach.get(i));
                            }
                        }
                        catch (Exception e) {
                            Map<String, Object> serviceOrderInfoAttach = (Map<String, Object>) serviceOrderInfoAttachs.get("attach");
                            serviceOrderInfoAttach.put("srv_ord_id", 0);
                            serviceOrderInfoAttach.put("dispatch_order_id", 0);
                            serviceOrderInfoAttach.put("cst_ord_id", gbcoId);
                            serviceOrderInfoAttach.put("origin", "CUSTOMER");
                            attachment.add(serviceOrderInfoAttach);
                        }
                    }
                }
                if (judgeNULL("" + dispatchInfo.get("attachs"))&&dl==0) {
                    Map<String, Object> dispatchInfoAttachs = (Map<String, Object>) dispatchInfo.get("attachs"); //dispatchInfo节点下的附件
                    if (judgeNULL(dispatchInfoAttachs)) { //循环处理dispatchInfo节点下的附件
                        try {
                            List<Map<String, Object>> dispatchInfoAttachsAttach = (List<Map<String, Object>>) dispatchInfoAttachs.get("attach");
                            for (int i = 0; i < dispatchInfoAttachsAttach.size(); i++) {
                                dispatchInfoAttachsAttach.get(i).put("srv_ord_id", 0);
                                dispatchInfoAttachsAttach.get(i).put("dispatch_order_id", gbdoId);
                                dispatchInfoAttachsAttach.get(i).put("cst_ord_id", 0);
                                dispatchInfoAttachsAttach.get(i).put("origin", "DISPATCH");
                                attachment.add(dispatchInfoAttachsAttach.get(i));
                            }
                        }
                        catch (Exception e) {
                            Map<String, Object> dispatchInfoAttachsAttach = (Map<String, Object>) dispatchInfoAttachs.get("attach");
                            dispatchInfoAttachsAttach.put("srv_ord_id", 0);
                            dispatchInfoAttachsAttach.put("dispatch_order_id", gbdoId);
                            dispatchInfoAttachsAttach.put("cst_ord_id", 0);
                            dispatchInfoAttachsAttach.put("origin", "DISPATCH");
                            attachment.add(dispatchInfoAttachsAttach);
                        }
                    }
                }
                if (judgeNULL(""+prodInfo.get(dl).get("attachs"))) {
                    Map<String, Object> prodInfoAttachs = (Map<String, Object>) prodInfo.get(dl).get("attachs"); //prodInfo节点下的附件
                    if (judgeNULL(prodInfoAttachs)) { //循环处理prodInfoInfo节点下的附件
                        try {
                            List<Map<String, Object>> prodInfoAttach = (List<Map<String, Object>>) prodInfoAttachs.get("attach");
                            for (int i = 0; i < prodInfoAttach.size(); i++) {
                                prodInfoAttach.get(i).put("srv_ord_id", gbsoiId);
                                prodInfoAttach.get(i).put("dispatch_order_id", 0);
                                prodInfoAttach.get(i).put("cst_ord_id", 0);
                                prodInfoAttach.get(i).put("origin", "FQ");
                                attachment.add(prodInfoAttach.get(i));
                            }
                        }
                        catch (Exception e) {
                            Map<String, Object> prodInfoAttach = (Map<String, Object>) prodInfoAttachs.get("attach");
                            prodInfoAttach.put("srv_ord_id", gbsoiId);
                            prodInfoAttach.put("dispatch_order_id", 0);
                            prodInfoAttach.put("cst_ord_id", 0);
                            prodInfoAttach.put("origin", "FQ");
                            attachment.add(prodInfoAttach);
                        }
                    }
                }
                String mainOrg = prodInfo.get(dl).get("mainOrg") + "";
                List<Map<String, Object>> zdj = springJdbcTemplate.queryForList("SELECT SORT_NO AS AREA_ID FROM GOM_BDW_CODE_INFO_SECOND WHERE CODE_TYPE='PROVINCE' AND CODE_TYPE_NAME='" + header.get("province") + "' AND REMARK = '" + prodInfo.get(dl).get("mainOrg") + "'");
                if (judgeNULL(zdj)&&judgeNULL(zdj.get(0))&&!"IP中继上联电路".equals(onedryproducttype)) {
                    mainOrg = zdj.get(0).get("AREA_ID") + "";
                }

                    StringBuffer addgbsoiSql = new StringBuffer(); //拼接电路表信息sql(GOM_BDW_SRV_ORD_INFO)
                    addgbsoiSql.append("insert into GOM_BDW_SRV_ORD_INFO (SRV_ORD_ID, CST_ORD_ID, ORDER_ID,  DISPATCH_ORDER_ID, SERVICE_ID, TRADE_TYPE_CODE, ACTIVE_TYPE, SERVICE_OFFER_ID, SERIAL_NUMBER, TRADE_ID, TRADE_ID_RELA, USER_ID, FLOW_ID, MAINORG, ORDER_TYPE, SRV_ORD_STAT, RESOURCES, OMORDERCODE，CRM_PROD_INST_ID, INSTANCE_ID, SYSTEM_RESOURCE, CREATE_DATE, REQ_FIN_TIME) values (");
                    addgbsoiSql.append(gbsoiId + "," + gbcoId + "," + OrderId + "," + gbdoId + ",'" + prodType + "','" + serviceOrderInfo.get("orderCode") + "',");
                    addgbsoiSql.append("'" + prodInfo.get(dl).get("actType") + "','" + services.get(0).get("SORT_NO") + "','" + crmProdInstId + "',");
                    addgbsoiSql.append("'" + prodInfo.get(dl).get("prodInstId") + "','" + circuitcode + "','" + dispatchInfo.get("staffName") + "','" + prodInfo.get(dl).get("woOrderCode") + "','" + mainOrg + "',");
                    addgbsoiSql.append("'" + serviceOrderInfo.get("orderType") + "','10N','onedry','" + prodInfo.get(dl).get("omOrderCode") + "','" + crmProdInstId + "','" + crmProdInstId + "','second-schedule-lt',sysdate,'"+requFineTime+"')");
                    springJdbcTemplate.update(addgbsoiSql.toString()); //添加电路表信息
                    StringBuffer addgbsoaiSql = new StringBuffer(); //拼接属性表信息sql(GOM_BDW_SRV_ORD_ATTR_INFO)
                    addgbsoaiSql.append("INSERT INTO GOM_BDW_SRV_ORD_ATTR_INFO (ATTR_INFO_ID, SRV_ORD_ID, ATTR_ACTION, ATTR_CODE, ATTR_NAME, ATTR_VALUE, ATTR_VALUE_NAME, CREATE_DATE, SOURSE) SELECT SEQ_GOM_BDW_SRV_ORD_ATTR_INFO.NEXTVAL,SRV_ORD_ID, ATTR_ACTION, ATTR_CODE, ATTR_NAME, ATTR_VALUE, ATTR_VALUE_NAME, CREATE_DATE, SOURSE FROM (");
                        for (int sx = 0; sx < prodAttr.size(); sx++) {
                            Map<String, Object> prod = (Map<String, Object>) prodAttr.get(sx);
                            String attrValue = prod.get("value") + "";
                            if ("simpleRoute".equals("" + prod.get("code")) || "ordsimpleRoute".equals("" + prod.get("code")) || "oldSimpleRoute".equals("" + prod.get("code"))) {
                                if (judgeNULL("" + prod.get("value"))) {
                                    Map<String, Object> simpleRoute = new HashMap<String, Object>();
                                    simpleRoute.put("srvOrdId", gbsoiId);
                                    simpleRoute.put("resId", prodInfo.get(dl).get("actType") + "simpleRoute" + prodInfo.get(dl).get("prodInstId"));
                                    simpleRoute.put("resSpecId", "onedry");
                                    simpleRoute.put("resName", circuitcode);
                                    simpleRoute.put("aggrResId", prodInfo.get(dl).get("woOrderCode"));
                                    simpleRoute.put("aggrResSpec", prodInfo.get(dl).get("omOrderCode"));
                                    simpleRoute.put("seq", 0);
                                    simpleRoute.put("actionType", "QunCi");
                                    simpleRoute.put("rfsId", serviceOrderInfo.get("orderType") + "S" + prodInfo.get(dl).get("woOrderCode") + "R" + serviceOrderInfo.get("orderCode"));
                                    simpleRoute.put("compId", "19921201");
                                    simpleRoute.put("resType", "一干群次路由");
                                    if ("ordsimpleRoute".equals(prod.get("code")) || "oldSimpleRoute".equals(prod.get("code"))) {
                                        simpleRoute.put("attrCode", "onedrySimpleRoute");
                                        simpleRoute.put("resources", "oldResource");
                                        simpleRoute.put("attrName", "一干调前群次路由");
                                    }
                                    else {
                                        simpleRoute.put("attrCode", "onedrySimpleRoute");
                                        simpleRoute.put("resources", "newResource");
                                        simpleRoute.put("attrName", "一干调后群次路由");
                                    }
                                    simpleRoute.put("attrValue", prod.get("value"));
                                    wsd.saveRes(simpleRoute);
                                    wsd.saveResRoute(simpleRoute);
                                }
                                continue;
                            }
                            if ("fullRoute".equals("" + prod.get("code")) || "oldfullRoute".equals("" + prod.get("code")) || "oldFullRoute".equals("" + prod.get("code"))) {
                                if (judgeNULL("" + prod.get("value"))) {
                                    Map<String, Object> fullRoute = new HashMap<String, Object>();
                                    fullRoute.put("srvOrdId", gbsoiId);
                                    fullRoute.put("resId", prodInfo.get(dl).get("actType") + "fullRoute" + prodInfo.get(dl).get("prodInstId"));
                                    fullRoute.put("resSpecId", "onedry");
                                    fullRoute.put("resName", circuitcode);
                                    fullRoute.put("aggrResId", prodInfo.get(dl).get("woOrderCode"));
                                    fullRoute.put("aggrResSpec", prodInfo.get(dl).get("omOrderCode"));
                                    fullRoute.put("seq", 0);
                                    fullRoute.put("actionType", "QuanCheng");
                                    fullRoute.put("rfsId", serviceOrderInfo.get("orderType") + "F" + prodInfo.get(dl).get("omOrderCode") + "R" + serviceOrderInfo.get("orderCode"));
                                    fullRoute.put("compId", "20110718");
                                    fullRoute.put("resType", "一干全程路由");
                                    if ("oldfullRoute".equals(prod.get("code")) || "oldFullRoute".equals(prod.get("code"))) {
                                        fullRoute.put("attrCode", "onedryFullRoute");
                                        fullRoute.put("resources", "oldResource");
                                        fullRoute.put("attrName", "一干调前全程路由");
                                    }
                                    else {
                                        fullRoute.put("attrCode", "onedryFullRoute");
                                        fullRoute.put("resources", "newResource");
                                        fullRoute.put("attrName", "一干调后全程路由");
                                    }
                                    fullRoute.put("attrValue", prod.get("value"));
                                    wsd.saveRes(fullRoute);
                                    wsd.saveResRoute(fullRoute);
                                }
                                continue;
                            }
                            if ("route".equals("" + prod.get("code"))) {
                                if (judgeNULL("" + prod.get("value"))) {
                                    Map<String, Object> route = new HashMap<String, Object>();
                                    route.put("srvOrdId", gbsoiId);
                                    route.put("resId", prodInfo.get(dl).get("actType") + "fullRoute" + prodInfo.get(dl).get("prodInstId"));
                                    route.put("resSpecId", "onedry");
                                    route.put("resName", circuitcode);
                                    route.put("aggrResId", prodInfo.get(dl).get("woOrderCode"));
                                    route.put("aggrResSpec", prodInfo.get(dl).get("omOrderCode"));
                                    route.put("seq", 0);
                                    route.put("actionType", "QuanCheng");
                                    route.put("rfsId", serviceOrderInfo.get("orderType") + "F" + prodInfo.get(dl).get("omOrderCode") + "R" + serviceOrderInfo.get("orderCode"));
                                    route.put("compId", "12010817");
                                    route.put("resType", "一干路由");
                                    route.put("attrCode", "onedryRoute");
                                    route.put("resources", "newResource");
                                    route.put("attrName", "一干路由");
                                    route.put("attrValue", prod.get("value"));
                                    wsd.saveRes(route);
                                    wsd.saveResRoute(route);
                                }
                                continue;
                            }
                            List<Map<String, Object>> attr = springJdbcTemplate.queryForList("SELECT PROPERTY_GRP_ID,PROPERTY_ID FROM GOM_BDW_PROPERTY_INFO WHERE SRV_ID='" + prodType + "' AND ONEDRY_CODE='" + prod.get("code") + "'");
                            if (judgeNULL(attr)&& judgeNULL(attr.get(0))) {
                                addgbsoaiSql.append("SELECT " + gbsoiId + " AS SRV_ORD_ID, '" + attr.get(0).get("PROPERTY_GRP_ID") + "' AS ATTR_ACTION, '" + attr.get(0).get("PROPERTY_ID") + "' AS ATTR_CODE, '" + prod.get("name") + "' AS ATTR_NAME, '" + attrValue + "' AS ATTR_VALUE, '" + prod.get("code") + "' AS ATTR_VALUE_NAME, SYSDATE AS CREATE_DATE, 'onedry' AS SOURSE FROM DUAL UNION  ALL ");
                            }
                        }
                        Map<String, Object> slAREA = springJdbcTemplate.queryForMap("SELECT SORT_NO AS SLAREA FROM GOM_BDW_CODE_INFO_SECOND WHERE CODE_TYPE='PROVINCE' AND CODE_TYPE_NAME='" + header.get("province") + "'"); //查询受理区域
                        List<Map<String, Object>> aAREA = springJdbcTemplate.queryForList("SELECT CODE_VALUE AS AAREA FROM GOM_BDW_CODE_INFO_SECOND WHERE CODE_TYPE='CITY' AND CODE_TYPE_NAME='" + citya + "' AND SORT_NO='" + slAREA.get("SLAREA") + "'"); //查询A端所属区域
                        List<Map<String, Object>> zAREA = springJdbcTemplate.queryForList("SELECT CODE_VALUE AS ZAREA FROM GOM_BDW_CODE_INFO_SECOND WHERE CODE_TYPE='CITY' AND CODE_TYPE_NAME='" + cityz + "' AND SORT_NO='" + slAREA.get("SLAREA") + "'"); //查询Z端所属区域
                        if (judgeNULL(aAREA)) {
                            addgbsoaiSql.append("SELECT " + gbsoiId + " AS SRV_ORD_ID, '0' AS ATTR_ACTION, '20000234' AS ATTR_CODE, 'A端或PE端所属区域ID' AS ATTR_NAME, '" + aAREA.get(0).get("AAREA") + "' AS ATTR_VALUE, 'AreceptionArea' AS ATTR_VALUE_NAME, SYSDATE AS CREATE_DATE, 'onedry' AS SOURSE FROM DUAL UNION  ALL ");
                        }
                        addgbsoaiSql.append("SELECT " + gbsoiId + " AS SRV_ORD_ID, '0' AS ATTR_ACTION, '20000080' AS ATTR_CODE, 'A端或PE端所属区域NAME' AS ATTR_NAME, '" + provincea + " " + citya + "' AS ATTR_VALUE, 'AreceptionArea' AS ATTR_VALUE_NAME, SYSDATE AS CREATE_DATE, 'onedry' AS SOURSE FROM DUAL UNION  ALL ");
                        addgbsoaiSql.append("SELECT " + gbsoiId + " AS SRV_ORD_ID, '0' AS ATTR_ACTION, '20000078' AS ATTR_CODE, 'A端或PE端所属地市NAME' AS ATTR_NAME, '" + provincea + " " + citya + "' AS ATTR_VALUE, 'AreceptionArea' AS ATTR_VALUE_NAME, SYSDATE AS CREATE_DATE, 'onedry' AS SOURSE FROM DUAL UNION  ALL ");
                        if (judgeNULL(zAREA)) {
                            addgbsoaiSql.append("SELECT " + gbsoiId + " AS SRV_ORD_ID, '0' AS ATTR_ACTION, '20000235' AS ATTR_CODE, 'Z端或CE端所属区域ID' AS ATTR_NAME, '" + zAREA.get(0).get("ZAREA") + "' AS ATTR_VALUE, 'ZreceptionArea' AS ATTR_VALUE_NAME, SYSDATE AS CREATE_DATE, 'onedry' AS SOURSE FROM DUAL UNION  ALL ");
                        }
                        addgbsoaiSql.append("SELECT " + gbsoiId + " AS SRV_ORD_ID, '0' AS ATTR_ACTION, '20000098' AS ATTR_CODE, 'Z端或CE端所属区域NAME' AS ATTR_NAME, '" + provincez + " " + cityz + "' AS ATTR_VALUE, 'AreceptionArea' AS ATTR_VALUE_NAME, SYSDATE AS CREATE_DATE, 'onedry' AS SOURSE FROM DUAL UNION  ALL ");
                        addgbsoaiSql.append("SELECT " + gbsoiId + " AS SRV_ORD_ID, '0' AS ATTR_ACTION, '20000096' AS ATTR_CODE, 'Z端或CE端所属地市NAME' AS ATTR_NAME, '" + provincez + " " + cityz + "' AS ATTR_VALUE, 'AreceptionArea' AS ATTR_VALUE_NAME, SYSDATE AS CREATE_DATE, 'onedry' AS SOURSE FROM DUAL UNION  ALL ");
                        addgbsoaiSql.append("SELECT " + gbsoiId + " AS SRV_ORD_ID, '0' AS ATTR_ACTION, 'mainOrg' AS ATTR_CODE, '主调局' AS ATTR_NAME, '" + prodInfo.get(dl).get("mainOrg") + "' AS ATTR_VALUE, 'mainOrg' AS ATTR_VALUE_NAME, SYSDATE AS CREATE_DATE, 'onedry' AS SOURSE FROM DUAL UNION  ALL ");
                        if (judgeNULL("" + prodInfo.get(dl).get("secondaryOrg"))) {
                            addgbsoaiSql.append("SELECT " + gbsoiId + " AS SRV_ORD_ID, '0' AS ATTR_ACTION, 'sendDepartment' AS ATTR_CODE, '发往单位' AS ATTR_NAME, '" + prodInfo.get(dl).get("mainOrg") + "," + prodInfo.get(dl).get("secondaryOrg") + "' AS ATTR_VALUE, 'sendDepartment' AS ATTR_VALUE_NAME, SYSDATE AS CREATE_DATE, 'onedry' AS SOURSE FROM DUAL)");
                        }
                        else {
                            addgbsoaiSql.append("SELECT " + gbsoiId + " AS SRV_ORD_ID, '0' AS ATTR_ACTION, 'sendDepartment' AS ATTR_CODE, '发往单位' AS ATTR_NAME, '" + prodInfo.get(dl).get("mainOrg") + "' AS ATTR_VALUE, 'sendDepartment' AS ATTR_VALUE_NAME, SYSDATE AS CREATE_DATE, 'onedry' AS SOURSE FROM DUAL)");
                        }
                        springJdbcTemplate.queryForMap(" SELECT SEQ_GOM_BDW_SRV_ORD_ATTR_INFO.NEXTVAL FROM DUAL"); //属性表入库执行前刷掉属性表一个序列值
                        springJdbcTemplate.update(addgbsoaiSql.toString()); //添加属性表信息
                    if(!"0".equals(OrderId)){
                        //到单kafka推日志
                        ToKafkaTacheLog toKafkaWoCreateTacheLog = new ToKafkaTacheLog();
                        toKafkaWoCreateTacheLog.setBase_order_id(OrderId);
                        LoggerThreadPool.addLoggerToExecute(resInterfaceLogger, toKafkaWoCreateTacheLog);
                    }
                }
            }
            if (judgeNULL(repeatList)) {
                returnValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><resultFlag>0</resultFlag><resultContent>服务定单修改处理成功</resultContent></root>";
                interflog.put("RETURNCONTENT", returnValue);
                wsd.updateInterfLog(interflog);
                return returnValue;
            }
            else {
                Map<String, Object> receptionArea = springJdbcTemplate.queryForMap("SELECT CODE_VALUE,CODE_CONTENT FROM GOM_BDW_CODE_INFO_SECOND WHERE CODE_TYPE='PROVINCE' AND CODE_TYPE_NAME='" + header.get("province") + "'");
                receptionAreaId = receptionArea.get("CODE_VALUE") + "";
                receptionAreaName = receptionArea.get("CODE_CONTENT") + "";
                StringBuffer addgbcoSql = new StringBuffer(); //拼接客户表信息sql（GOM_BDW_CST_ORD）
                addgbcoSql.append("insert into GOM_BDW_CST_ORD (CST_ORD_ID,CUST_MANAGER_TEL, SENDER,");
                addgbcoSql.append("GROUP_PM_NAME,GROUP_PM_TEL,PROVINCE_PM_TEL,PROVINCE_PM_NAME,INIT_AM_TEL,INIT_AM_NAME,SUBSCRIBE_ID,CUST_NAME_ENGLISH,INIT_AM_ORG,REMARK,APPLY_ORD_ID,");
                addgbcoSql.append("CUST_PROVINCE,CUST_CONTACT_MAN_NAME,CUST_CONTACT_MAN_TEL,CONTRACT_ID,CUST_NAME_CHINESE,CUST_ADDRESS,CUST_INDUSTRY,CUST_EMAIL,CUST_TEL,CUST_ID,UPPERCODE,UPPERNAME,APPLY_ORD_NAME,HANDLE_DEP_ID,HANDLE_DEP,IS_GROUP_CUST,ONEDRY_AREA_CODE,CREATE_DATE");
                addgbcoSql.append(") values (" + gbcoId + ",'"+serviceOrderInfo.get("custmanagertel") + "','").append(MapUtils.getString(header,"sender")).append("','");
                addgbcoSql.append(serviceOrderInfo.get("groupManager") + "','" + serviceOrderInfo.get("groupManagerPhone") + "','" + serviceOrderInfo.get("provinceManagerPhone") + "','" + serviceOrderInfo.get("provinceManager") + "','" + serviceOrderInfo.get("originatorPhone") + "','" + serviceOrderInfo.get("originator") + "','" + serviceOrderInfo.get("orderTitle") + "','" + customerInfo.get("enName") + "','" + serviceOrderInfo.get("originatorDepartment") + "','" + serviceOrderInfo.get("orderContent") + "','" + serviceOrderInfo.get("orderTitle"));
                addgbcoSql.append("','" + customerInfo.get("industry") + "','" + customerInfo.get("contact") + "','" + customerInfo.get("phone") + "','" + customerInfo.get("code") + "','" + customerInfo.get("cnName") + "','" + customerInfo.get("address") + "','" + customerInfo.get("industry") + "','" + customerInfo.get("email") + "','" + customerInfo.get("phone") + "','" + customerInfo.get("serviceLevel") + "','" + customerInfo.get("upperCode") + "','" + customerInfo.get("upperName") + "'");
                addgbcoSql.append(",'" + serviceOrderInfo.get("orderTitle") + "','" + receptionAreaId + "','" + receptionAreaName + "','" + serviceOrderInfo.get("isGroupCust") + "','" + header.get("province") + "',sysdate)");
                springJdbcTemplate.update(addgbcoSql.toString()); //添加客户表信息
                StringBuffer addgbdoSql = new StringBuffer(); //拼接调单表信息sql
                addgbdoSql.append("insert into GOM_BDW_DISPATCH_ORDER (DISPATCH_ORDER_ID, CST_ORD_ID, DISPATCH_ORDER_NO, STAFF_NAME, STAFF_TEL, STAFF_ORG, ISSUER, SEND_DATE, DISPATCH_TYPE, DISPATCH_GRADE, DISPATCH_URGENCY, DISPATCH_TITLE, DISPATCH_SEND_ORG, DISPATCH_COPY_ORG, DISPATCH_TEXT, CHANGE_BEFORE_TEXT, CHANGE_AFTER_TEXT ,DISPATCH_SOURCE) values (");
                addgbdoSql.append(gbdoId + "," + gbcoId);
                addgbdoSql.append(",'" + dispatchInfo.get("dispatchOrderNo") + "','" + dispatchInfo.get("staffName") + "','" + dispatchInfo.get("staffTel") + "','" + dispatchInfo.get("staffOrg") + "','" + dispatchInfo.get("issuer") + "','" + dispatchInfo.get("sendDate") + "','" + dispatchInfo.get("dispatchType") + "','" + dispatchInfo.get("dispatchGrade"));
                addgbdoSql.append("','" + dispatchInfo.get("dispatchUrgency") + "','" + dispatchInfo.get("dispatchTitle") + "','" + dispatchInfo.get("dispatchSendOrg") + "','" + dispatchInfo.get("dispatchCopyOrg") + "','" + (dispatchInfo.get("dispatchText") + "").replaceAll("@enter@","\n") + "','" + dispatchInfo.get("changeBeforeText") + "','" + dispatchInfo.get("changeAfterText") + "'");
                addgbdoSql.append(",'onedry')");
                springJdbcTemplate.update(addgbdoSql.toString()); //添加调单表信息
                returnValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><resultFlag>0</resultFlag><resultContent>服务定单接收处理成功</resultContent></root>";
                interflog.put("RETURNCONTENT", returnValue);
                wsd.updateInterfLog(interflog);
                if(judgeNULL(attachment)) {
                    HandleAttachment circuit = new HandleAttachment(attachment, "JT_FTP_INFO", "FTP_INFO", "ONEDRY"); //创建异步处理附件
                    circuit.start(); //启动异步处理附件
                }
                if (!ListUtil.isEmpty(orderIdList)){
                    logger.info("开始准备发送短信。。。。。。。。。。。。。。。。。。。。");
                    Map<String, Object> sendMsgMap = new HashMap<String, Object>();
                    sendMsgMap.put("areaId", header.get("province"));
                    sendMsgMap.put("orderIdList", orderIdList);
                    sendMsgMap.put("operAction", "一干发起");
                    sendMsgMap.put("resources", "onedry");
                    orderSendMsgService.sendMsgBefore(sendMsgMap);
                }
                return returnValue;
            }
        }
        public Boolean judgeNULL(Object judge){
            if(judge==null){
                return false;
            }else if(judge instanceof String){
                if ("".equals(judge.toString())){
                    return false;
                }else if ("null".equals(judge.toString())){
                    return false;
                }else if ("NULL".equals(judge.toString())){
                    return false;
                }
            }else if(judge instanceof Map){
                if(((Map)judge).size()==0){
                    return false;
                }
            }else if(judge instanceof List){
                if(((List)judge).size()==0){
                    return false;
                }
            }
           return true;
        }
    }
