package com.zres.project.localnet.portal.webservice.oneDry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zres.project.localnet.portal.webservice.res.ResCfsAttrUpdateServiceIntf;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealService;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf;
import com.zres.project.localnet.portal.util.AnalysisXML;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;

import com.ztesoft.zsmart.pot.annotation.IgnoreSession;

/**
 * Created by jiangdebing on 2019/1/4.
 */
@Controller
@RequestMapping("/startRentServiceIntf")
public class StartRentService implements StartRentServiceIntf {
    @Autowired
    private OrderDealService orderDealService;
    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private JdbcTemplate springJdbcTemplate;
    @Autowired
    private OrderDealServiceIntf orderDealServiceIntf;
    @Autowired
    private ResCfsAttrUpdateServiceIntf resCfsAttrUpdateServiceIntf;

    @IgnoreSession
    @ResponseBody
    @RequestMapping(value = "/interfaceBDW/startRent.spr", method = RequestMethod.POST, produces = "application/xml;charset=UTF-8")
    public String startRent(@RequestBody String xml) { //起止租接口和全程调测通知接口
        Map<String, Object> interflog = new HashMap<String, Object>(); //记录接口日志
        String returnValue = "";
        int logId = wsd.querySequence("SEQ_GOM_BDW_INTERF_LOG_INFO.NEXTVAL"); //获取接口日志信息表序列
        interflog.put("URL", "/startRentServiceIntf/interfaceBDW/startRent.spr");
        interflog.put("CONTENT", xml);
        interflog.put("ID", logId);
        interflog.put("REMARK", "接收一干通知报文");
        Map<String, Object> rentdto = AnalysisXML.analysis(xml);
        Map<String, Object> prodsInfo = new HashMap<String, Object>();
        Map<String, Object> prodInfo = new HashMap<String, Object>();
        Map<String, Object> header = (Map<String, Object>) rentdto.get("header"); //获取header节点信息
        prodsInfo = (Map<String, Object>) ((Map<String, Object>) rentdto.get("body")).get("prodsInfo");
        if (prodsInfo != null && prodsInfo.size() > 0) {
            prodInfo = (Map<String, Object>) prodsInfo.get("prodInfo");
        }
        Object orderCode = ((Map<String, Object>) rentdto.get("body")).get("orderCode");
        Object prodInstId = prodInfo.get("prodInstId");
        Object qcWoOrderCode = prodInfo.get("qcWoOrderCode");
        List<Map<String, Object>> SrvOrdList = springJdbcTemplate.queryForList("SELECT G.SRV_ORD_ID,G.QCWOORDERCODE FROM GOM_BDW_SRV_ORD_INFO G JOIN GOM_BDW_CST_ORD C ON C.CST_ORD_ID=G.CST_ORD_ID WHERE G.TRADE_TYPE_CODE = '" + orderCode + "' AND G.TRADE_ID = '" + prodInstId + "' AND C.ONEDRY_AREA_CODE='" + header.get("province") + "'");
        if (SrvOrdList != null && SrvOrdList.size() > 0) {
            if (qcWoOrderCode != null) { //全程调测通知
                if (SrvOrdList.get(0).get("QCWOORDERCODE") != null && !"".equals(SrvOrdList.get(0).get("QCWOORDERCODE")) && !"null".equals(SrvOrdList.get(0).get("QCWOORDERCODE"))) { //一干回退全程调测
                    interflog.put("INTERFNAME", "回退全程调测通知");
                    interflog.put("ORDERNO", SrvOrdList.get(0).get("SRV_ORD_ID"));
                    wsd.insertInterfLogS(interflog); //保存日志报文
                    List<Map<String, Object>> returnList = springJdbcTemplate.queryForList("SELECT W.WO_ID,O.ORDER_ID,WS.ORD_PS_ID,UT.ID,UT.TACHE_NAME FROM GOM_ORDER O JOIN GOM_BDW_SRV_ORD_INFO G ON G.ORDER_ID = O.ORDER_ID JOIN GOM_WO W ON W.ORDER_ID = O.ORDER_ID JOIN GOM_BDW_SRV_ORD_INFO I ON I.ORDER_ID = O.ORDER_ID JOIN GOM_PS_2_WO_S WS ON WS.ID = W.PS_ID JOIN UOS_TACHE UT ON UT.ID = WS.TACHE_ID JOIN GOM_BDW_CST_ORD C ON C.CST_ORD_ID=G.CST_ORD_ID WHERE G.TRADE_ID = '" + prodInstId + "' AND  G.TRADE_TYPE_CODE = '" + orderCode + "' AND  W.WO_STATE = '290000002' AND UT.ID = 510101046 AND C.ONEDRY_AREA_CODE='" + header.get("province") + "'");
                    if (returnList != null && returnList.size() > 0) {
                        for (int td = 0; td < returnList.size(); td++) {
                            springJdbcTemplate.update("UPDATE GOM_BDW_SRV_ORD_INFO SET QCWOORDERCODE = '" + qcWoOrderCode + "' WHERE TRADE_TYPE_CODE = '" + orderCode + "' AND TRADE_ID = '" + prodInstId + "'");
                            Map<String, Object> rentMap = new HashMap<String, Object>();
                            rentMap.put("tacheId", returnList.get(td).get("ID"));
                            rentMap.put("woId", returnList.get(td).get("WO_ID"));
                            rentMap.put("orderId", returnList.get(td).get("ORDER_ID"));
                            rentMap.put("flag", "一干");
                            rentMap.put("remark", "一干下发了退单通知");
                            orderDealService.rollBackWoOrder(rentMap);
                        }
                        returnValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><resultFlag>0</resultFlag><resultContent>回退全程调测通知成功</resultContent></root>";
                        interflog.put("RETURNCONTENT", returnValue);
                        wsd.updateInterfLog(interflog); //修改日志表返回结果
                    }
                    else {
                        List<Map<String, Object>> kyqctcWorkId = springJdbcTemplate.queryForList("SELECT O.ORDER_ID,W.WO_ID FROM GOM_ORDER O JOIN GOM_BDW_SRV_ORD_INFO G ON G.ORDER_ID = O.ORDER_ID JOIN GOM_WO W ON W.ORDER_ID = O.ORDER_ID JOIN GOM_BDW_SRV_ORD_INFO I ON I.ORDER_ID = O.ORDER_ID JOIN GOM_PS_2_WO_S WS ON WS.ID = W.PS_ID JOIN UOS_TACHE UT ON UT.ID = WS.TACHE_ID WHERE G.TRADE_TYPE_CODE = '" + orderCode + "' AND  G.TRADE_ID = '" + prodInstId + "' AND  W.WO_STATE = '290000111' AND UT.ID = '510101045'");
                        if (kyqctcWorkId != null && kyqctcWorkId.size() > 0) {
                            for (int qc = 0; qc < kyqctcWorkId.size(); qc++) {
                                orderDealServiceIntf.oneDryNotice(kyqctcWorkId.get(qc).get("ORDER_ID")+"", kyqctcWorkId.get(qc).get("WO_ID")+"", header.get("province")+"");
                            }
                            springJdbcTemplate.update("UPDATE GOM_BDW_SRV_ORD_INFO SET QCWOORDERCODE = '" + qcWoOrderCode + "' WHERE TRADE_TYPE_CODE = '" + orderCode + "' AND TRADE_ID = '" + prodInstId + "'");
                            returnValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><resultFlag>0</resultFlag><resultContent>全程调测通知成功</resultContent></root>";
                            interflog.put("RETURNCONTENT", returnValue);
                            wsd.updateInterfLog(interflog); //修改日志表返回结果

                        }
                    }
                    return returnValue;
                }
                else {
                    interflog.put("INTERFNAME", "全程调测通知");
                    interflog.put("ORDERNO", SrvOrdList.get(0).get("SRV_ORD_ID"));
                    wsd.insertInterfLogS(interflog); //保存日志报文
                    List<Map<String, Object>> kyqctcWorkId = springJdbcTemplate.queryForList("SELECT O.ORDER_ID,W.WO_ID FROM GOM_ORDER O JOIN GOM_BDW_SRV_ORD_INFO G ON G.ORDER_ID = O.ORDER_ID JOIN GOM_WO W ON W.ORDER_ID = O.ORDER_ID JOIN GOM_BDW_SRV_ORD_INFO I ON I.ORDER_ID = O.ORDER_ID JOIN GOM_PS_2_WO_S WS ON WS.ID = W.PS_ID JOIN UOS_TACHE UT ON UT.ID = WS.TACHE_ID WHERE G.TRADE_TYPE_CODE = '" + orderCode + "' AND  G.TRADE_ID = '" + prodInstId + "' AND  W.WO_STATE = '290000111' AND UT.ID = '510101045'");
                    if (kyqctcWorkId != null && kyqctcWorkId.size() > 0) {
                        for (int qc = 0; qc < kyqctcWorkId.size(); qc++) {
                            orderDealServiceIntf.oneDryNotice(kyqctcWorkId.get(qc).get("ORDER_ID")+"", kyqctcWorkId.get(qc).get("WO_ID")+"", header.get("province")+"");
                        }
                        springJdbcTemplate.update("UPDATE GOM_BDW_SRV_ORD_INFO SET QCWOORDERCODE = '" + qcWoOrderCode + "' WHERE TRADE_TYPE_CODE = '" + orderCode + "' AND TRADE_ID = '" + prodInstId + "'");
                        returnValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><resultFlag>0</resultFlag><resultContent>全程调测通知成功</resultContent></root>";
                        interflog.put("RETURNCONTENT", returnValue);
                        wsd.updateInterfLog(interflog); //修改日志表返回结果
                        return returnValue;
                    }
                    else {
                        springJdbcTemplate.update("UPDATE GOM_BDW_SRV_ORD_INFO SET QCWOORDERCODE = '" + qcWoOrderCode + "' WHERE TRADE_TYPE_CODE = '" + orderCode + "' AND TRADE_ID = '" + prodInstId + "'");
                        returnValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><resultFlag>0</resultFlag><resultContent>全程调测通知成功</resultContent></root>";
                        interflog.put("RETURNCONTENT", returnValue);
                        wsd.updateInterfLog(interflog); //修改日志表返回结果
                        return returnValue;
                    }
                }
            }
            else { //起租通知
                interflog.put("INTERFNAME", "起止租通知");
                List<Map<String, Object>> rentList = springJdbcTemplate.queryForList("SELECT W.WO_ID,O.ORDER_ID,WS.ORD_PS_ID,UT.ID,UT.TACHE_NAME,G.SRV_ORD_ID FROM GOM_ORDER O JOIN GOM_BDW_SRV_ORD_INFO G ON G.ORDER_ID = O.ORDER_ID JOIN GOM_BDW_CST_ORD C ON C.CST_ORD_ID=G.CST_ORD_ID AND C.ONEDRY_AREA_CODE='" + header.get("province") + "' JOIN GOM_WO W ON W.ORDER_ID = O.ORDER_ID JOIN GOM_BDW_SRV_ORD_INFO I ON I.ORDER_ID = O.ORDER_ID JOIN GOM_PS_2_WO_S WS ON WS.ID = W.PS_ID JOIN UOS_TACHE UT ON UT.ID = WS.TACHE_ID WHERE G.TRADE_TYPE_CODE = '" + orderCode + "' AND  G.TRADE_ID = '" + prodInstId + "' AND  W.WO_STATE = '290000002' AND UT.ID = 510101046");
                interflog.put("ORDERNO", rentList.get(0).get("SRV_ORD_ID"));
                wsd.insertInterfLogS(interflog); //保存日志报文
                String startTime = MapUtils.getString(prodInfo,"startTime");
                String qcFinishTime = MapUtils.getString(prodInfo,"qcFinishTime");//全程报浚时间
                if (rentList != null && rentList.size() > 0) {
                    Map<String, Object> res = new HashMap<String, Object>();
                    for (int qz = 0; qz < rentList.size(); qz++) {
                        Map<String, Object> rentMap = new HashMap<String, Object>();
                        rentMap.put("tacheId", rentList.get(qz).get("ID"));
                        rentMap.put("woId", rentList.get(qz).get("WO_ID"));
                        rentMap.put("orderId", rentList.get(qz).get("ORDER_ID"));
                        /*Map<String, String> operAttrsVal = new HashMap<String, String>();
                        operAttrsVal.put("ifRentBack ", "1");
                        rentMap.put("operAttrsVal", operAttrsVal);*/
                        Map<String, Object> tacheOperInfo = new HashMap<String, Object>();
                        tacheOperInfo.put("operFlag", "起租成功");
                        tacheOperInfo.put("remark", "一干下发了起租通知");
                        rentMap.put("tacheOperInfo", tacheOperInfo);
                        rentMap.put("actionFlag", "complateWo");
                        rentMap.put("srvOrdId", rentList.get(qz).get("SRV_ORD_ID"));
                        rentMap.put("startTime", startTime);
                        res = orderDealService.sendWoOrder(rentMap);
                        // 入库起租时间
                        String srvOrdId = rentList.get(qz).get("SRV_ORD_ID").toString();
                        Map<String, Object> rmap = new HashMap<String, Object>();
                        rmap.put("srv_ord_id", srvOrdId);
                        rmap.put("attr_action", "1");
                        rmap.put("attr_code", "21100001");
                        rmap.put("attr_name", "起租时间");
                        //rmap.put("attr_value", df.format(startTime)); 这是什么扎心操作，，，，，，
                        rmap.put("attr_value", startTime);
                        rmap.put("attr_value_name", "");
                        rmap.put("sourse", "onedry");
                        wsd.saveRetInfo(rmap);
                        // 调用资源扩展接口更新起止租时间
                        resCfsAttrUpdateServiceIntf.resTimeUpdate(srvOrdId,startTime,qcFinishTime);
                    }
                    if((boolean)res.get("success")==true) {
                        returnValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><resultFlag>0</resultFlag><resultContent>起租成功</resultContent></root>";
                    }
                    else {
                        returnValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><resultFlag>1</resultFlag><resultContent>" + res.get("message") + "</resultContent></root>";
                    }
                    interflog.put("RETURNCONTENT", returnValue);
                    wsd.updateInterfLog(interflog); //修改日志表返回结果
                    return returnValue;
                }
                else {
                    returnValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><resultFlag>1</resultFlag><resultContent>起租失败，没有找到对应在起租环节的工单</resultContent></root>";
                    interflog.put("RETURNCONTENT", returnValue);
                    wsd.updateInterfLog(interflog); //修改日志表返回结果
                    return returnValue;
                }
            }
        }
        else {
            interflog.put("ORDERNO", prodInstId);
            wsd.insertInterfLogS(interflog); //保存日志报文
            returnValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><resultFlag>1</resultFlag><resultContent>起租或全程调测通知失败，系统中没有此单信息。</resultContent></root>";
            interflog.put("RETURNCONTENT", returnValue);
            wsd.updateInterfLog(interflog); //修改日志表返回结果
            return returnValue;
        }
    }
}
