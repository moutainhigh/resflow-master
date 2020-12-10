package com.zres.project.localnet.portal.webservice.oneDry;

import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.util.AnalysisXML;
import com.zres.project.localnet.portal.util.TransferInterfaceUtil;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;

import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import com.ztesoft.res.frame.user.inf.UserInfo;

/**
 * Created by jiangdebing on 2019/1/3.
 */
@Service
public class FeedbackInterface {
    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private JdbcTemplate springJdbcTemplate;
    @Autowired
    private OrderDealDao orderDealDao;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public Map soAccept(Map parameter) { //服务定单签收通知一干电路调度
        Map<String, Object> urn =  new HashMap<String, Object>();
        if (parameter.get("SrvOrdId") != null && !"".equals(parameter.get("SrvOrdId"))) {
            List<Map<String, Object>> bwData = springJdbcTemplate.queryForList("SELECT TRADE_ID,TRADE_TYPE_CODE,FLOW_ID,OMORDERCODE FROM GOM_BDW_SRV_ORD_INFO WHERE SRV_ORD_ID=" + parameter.get("SrvOrdId")); //查询报文
            StringBuilder message = new StringBuilder(); //拼接报竣报文
            int logId = wsd.querySequence("SEQ_GOM_BDW_INTERF_LOG_INFO.NEXTVAL"); //获取接口日志信息表序列
            Map<String, Object> interflog = new HashMap<String, Object>(); //记录接口日志
            message.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            message.append("<root><header>");
            message.append("<eventType>sync</eventType>");
            message.append("<interfaceCode>soAccept</interfaceCode>"); //接口编码（必填）
            message.append("<serialNo>000000</serialNo>");
            message.append("<userId>sysAdmin</userId>");
            message.append("<password>1</password>");
            message.append("<sender>IOM</sender>");
            message.append("<province>BenDiWang</province>");
            message.append("</header><body>");
            message.append("<orderCode>" + bwData.get(0).get("TRADE_TYPE_CODE") + "</orderCode>"); //一干订单ID
            message.append("<acceptPeople>" + parameter.get("username") + "</acceptPeople>"); //本地网和一干共有的用户名
            message.append("<acceptPeoplePhone>" + parameter.get("mobiletel") + "</acceptPeoplePhone>"); //本地网和一干共有的用户名手机号
            message.append("<assignTime>" + sdf.format(new Date()) + "</assignTime>");
            message.append("<comments></comments>"); // 备注
            message.append("<prodsInfo><prodInfo>");
            message.append("<prodInstId>" + bwData.get(0).get("TRADE_ID") + "</prodInstId>"); //一干电路ID
            message.append("<woOrderCode>" + bwData.get(0).get("FLOW_ID") + "</woOrderCode>"); //一干签收工单ID
            message.append("<omOrderCode>" + bwData.get(0).get("OMORDERCODE") + "</omOrderCode>"); //一干的om_order表的ID
            message.append("</prodInfo></prodsInfo></body></root>");
            String wsdl = wsd.queryUrl("OneDry_TwoDry_Message");
            interflog.put("INTERFNAME","服务定单签收通知一干电路调度接口");
            interflog.put("URL", wsdl);
            interflog.put("CONTENT", message.toString());
            interflog.put("ORDERNO", parameter.get("SrvOrdId"));
            interflog.put("ID", logId);
            interflog.put("REMARK", "签收通知报文");
            wsd.insertInterfLogS(interflog); //保存日志报文
            Object ret = TransferInterfaceUtil.sendXmlClient(message.toString(), wsdl, "http://Interface.ws.chinaunicom.cn", "OneDry_TwoDry_Message"); //调用接口
            interflog.put("RETURNCONTENT", ret.toString());
            wsd.updateInterfLog(interflog); //修改日志表返回结果
            Map<String, Object> res = AnalysisXML.analysis(ret.toString());
            if ("0".equals(res.get("resultFlag"))) {
                urn.put("flag", "success");
                urn.put("msg", "签收成功");
            }
            else {
                urn.put("flag", "fail");
                urn.put("msg", res.get("resultContent"));
            }
        }
        else {
            urn.put("flag", "fail");
            urn.put("msg", "签收失败参数中SRV_ORD_ID值为空");
        }
        return urn;
    }

    public Map soComplete(Map parameter) { //报竣接口
        Map<String, Object> urn =  new HashMap<String, Object>();
        if (parameter.get("SrvOrdId") != null && !"".equals(parameter.get("SrvOrdId"))) {
            List<Map<String, Object>> bwData = springJdbcTemplate.queryForList("SELECT TRADE_ID,TRADE_TYPE_CODE,FLOW_ID,QCWOORDERCODE FROM GOM_BDW_SRV_ORD_INFO WHERE SRV_ORD_ID=" + parameter.get("SrvOrdId")); //查询报文
            StringBuilder message = new StringBuilder(); //拼接报竣报文
            int logId = wsd.querySequence("SEQ_GOM_BDW_INTERF_LOG_INFO.NEXTVAL"); //获取接口日志信息表序列
            Map<String, Object> interflog = new HashMap<String, Object>(); //记录接口日志
            message.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            message.append("<root><header>");
            message.append("<eventType>sync</eventType>");
            message.append("<interfaceCode>soComplete</interfaceCode>"); //接口编码（必填）
            message.append("<serialNo>000000</serialNo>");
            message.append("<userId>sysAdmin</userId>");
            message.append("<password>1</password>");
            message.append("<sender>IOM</sender>");
            message.append("<province>BenDiWang</province>");
            message.append("</header><body>");
            message.append("<orderCode>" + bwData.get(0).get("TRADE_TYPE_CODE") + "</orderCode>"); //一干订单ID
            message.append("<completePeople>" + parameter.get("username") + "</completePeople>"); //本地网和一干共有的用户名
            message.append("<completePeopleName>" + parameter.get("fullname") + "</completePeopleName>");
            message.append("<completePeoplePhone>" + parameter.get("mobiletel") + "</completePeoplePhone>"); //本地网和一干共有的用户名手机号
            message.append("<completePeopleEmail>" + parameter.get("email") + "</completePeopleEmail>");
            message.append("<completeTime>" + sdf.format(new Date()) + "</completeTime>");
            message.append("<comments>"+parameter.get("comments")+"</comments>"); // 备注
            message.append("<prodsInfo><prodInfo>");
            message.append("<prodInstId>" + bwData.get(0).get("TRADE_ID") + "</prodInstId>"); //一干电路ID
            if ("500001160".equals(parameter.get("tacheId"))||"510101044".equals(parameter.get("tacheId"))) { //二干完工汇总环节
                message.append("<woOrderCode>" + bwData.get(0).get("FLOW_ID") + "</woOrderCode>"); //一干本地报竣工单ID
            }
            else {
                message.append("<woOrderCode>" + bwData.get(0).get("QCWOORDERCODE") + "</woOrderCode>"); //一干全程调测工单ID
            }
            message.append("<omOrderCode>" + parameter.get("workId") + "</omOrderCode>"); //二干工单ID
            if ("500001160".equals(parameter.get("tacheId"))||"510101044".equals(parameter.get("tacheId"))) { //本地报竣
                message.append("<soCompleteType>DLDD-JT-SFBDBJ</soCompleteType>");
                interflog.put("INTERFNAME", "服务定单报竣(本地报竣)");
            }
            else { //全程调测
                message.append("<soCompleteType>DLDD-JT-QCCS</soCompleteType>");
                interflog.put("INTERFNAME", "服务定单报竣(全程调测)");
            }
            message.append("<attachs>");
            List<Map<String, Object>> attachs = springJdbcTemplate.queryForList("SELECT FILE_ID,FILE_NAME,FILE_PATH,FILE_TYPE FROM GOM_BDW_ATTACH_INFO WHERE WO_ORD_ID=" + parameter.get("workId"));
            for (int attach = 0; attach < attachs.size(); attach++) {
                    message.append("<attach path=\"" + attachs.get(attach).get("FILE_PATH") + "\" name=\"" + attachs.get(attach).get("FILE_ID") + "." + attachs.get(attach).get("FILE_TYPE") + "\" type=\"" + attachs.get(attach).get("FILE_TYPE") + "\" value=\"" + attachs.get(attach).get("FILE_NAME") + "\" />");
            }
            // 二干完工汇总环节,给一干将本地测试和二干完工汇总环节分别提交的附件，都能够上传
            if("510101044".equals(parameter.get("tacheId"))){
                List<Map<String, Object>> tahceIdList = springJdbcTemplate.queryForList("SELECT wo.wo_id  FROM gom_wo wosec LEFT JOIN  gom_bdw_sec_local_relate_info rel ON rel.parent_order_id = wosec.order_id LEFT JOIN gom_order ord ON ord.order_id = rel.order_id LEFT JOIN gom_wo wo ON wo.order_id = ord.order_id LEFT JOIN GOM_PS_2_WO_S ws ON ws.id = wo.ps_id LEFT JOIN UOS_TACHE ut ON ut.id = ws.tache_id WHERE wosec.wo_id = " + parameter.get("workId") + " AND ut.tache_code = 'LOCAL_TEST' AND wo.wo_state = '290000004'");
                for(int att = 0; att < tahceIdList.size(); att++){
                    List<Map<String, Object>> attach = springJdbcTemplate.queryForList("SELECT FILE_ID,FILE_NAME,FILE_PATH,FILE_TYPE FROM GOM_BDW_ATTACH_INFO WHERE WO_ORD_ID=" + tahceIdList.get(att).get("wo_id"));
                    for (int ach = 0; ach < attach.size(); ach++) {
                        message.append("<attach path=\"" + attach.get(ach).get("FILE_PATH") + "\" name=\"" + attach.get(ach).get("FILE_ID") + "." + attach.get(ach).get("FILE_TYPE") + "\" type=\"" + attach.get(ach).get("FILE_TYPE") + "\" value=\"" + attach.get(ach).get("FILE_NAME") + "\" />");
                    }
                }
            }
            message.append("</attachs>");
            message.append("</prodInfo></prodsInfo></body></root>");
            String wsdl = wsd.queryUrl("OneDry_TwoDry_Message");
            interflog.put("URL", wsdl);
            interflog.put("CONTENT", message.toString());
            interflog.put("ORDERNO", parameter.get("SrvOrdId"));
            interflog.put("ID", logId);
            interflog.put("REMARK", "反馈一干报竣报文");
            wsd.insertInterfLogS(interflog); //保存日志报文
            Object ret = TransferInterfaceUtil.sendXmlClient(message.toString(), wsdl, "http://Interface.ws.chinaunicom.cn", "OneDry_TwoDry_Message"); //调用接口
            interflog.put("RETURNCONTENT", ret.toString());
            wsd.updateInterfLog(interflog); //修改日志表返回结果
            Map<String, Object> res = AnalysisXML.analysis(ret.toString());
            if ("0".equals(res.get("resultFlag"))) {
                urn.put("flag", "success");
                urn.put("msg", "报竣成功");
            }
            else {
                urn.put("flag", "fail");
                urn.put("msg", res.get("resultContent"));
            }
        }
        else {
            urn.put("flag", "fail");
            urn.put("msg", "报竣失败参数中SRV_ORD_ID值为空");
        }
        return urn;
    }

    public Map soSendBack(Map parameter) { //全程调测退单一干
        Map<String, Object> urn =  new HashMap<String, Object>();
        if (parameter.get("SrvOrdId") != null && !"".equals(parameter.get("SrvOrdId"))) {
            List<Map<String, Object>> bwData = springJdbcTemplate.queryForList("SELECT TRADE_ID,TRADE_TYPE_CODE,OMORDERCODE,QCWOORDERCODE FROM GOM_BDW_SRV_ORD_INFO WHERE SRV_ORD_ID=" + parameter.get("SrvOrdId")); //查询报文
            StringBuilder message = new StringBuilder(); //拼接报竣报文
            int logId = wsd.querySequence("SEQ_GOM_BDW_INTERF_LOG_INFO.NEXTVAL"); //获取接口日志信息表序列
            Map<String, Object> interflog = new HashMap<String, Object>(); //记录接口日志
            message.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            message.append("<root><header>");
            message.append("<eventType>sync</eventType>");
            message.append("<interfaceCode>soSendBack</interfaceCode>"); //接口编码（必填）
            message.append("<serialNo>000000</serialNo>");
            message.append("<userId>sysAdmin</userId>");
            message.append("<password>1</password>");
            message.append("<sender>IOM</sender>");
            message.append("<province>BenDiWang</province>");
            message.append("</header><body>");
            message.append("<orderCode>" + bwData.get(0).get("TRADE_TYPE_CODE") + "</orderCode>"); //一干订单ID
            message.append("<backPeople>" + parameter.get("username") + "</backPeople>"); //本地网和一干共有的用户名
            message.append("<backPeoplePhone>" + parameter.get("mobiletel") + "</backPeoplePhone>"); //本地网和一干共有的用户名手机号
            message.append("<backTime>" + sdf.format(new Date()) + "</backTime>");
            message.append("<reason>主调局退单</reason>"); // 退单原因
            message.append("<prodsInfo><prodInfo>");
            message.append("<prodInstId>" + bwData.get(0).get("TRADE_ID") + "</prodInstId>"); //一干电路ID
            message.append("<woOrderCode>" + bwData.get(0).get("QCWOORDERCODE") + "</woOrderCode>"); //一干全程调测工单ID
            message.append("<omOrderCode>" + bwData.get(0).get("OMORDERCODE") + "</omOrderCode>"); //一干的om_order表的ID
            if (parameter.get("provinceA") != null && !"".equals(parameter.get("provinceA")) && !"null".equals(parameter.get("provinceA"))) {
                message.append("<provinceA>" + parameter.get("provinceA") + "</provinceA>");
            }
            else {
                message.append("<provinceA></provinceA>");
            }
            if (parameter.get("provinceZ") != null && !"".equals(parameter.get("provinceZ")) && !"null".equals(parameter.get("provinceZ"))) {
                message.append("<provinceZ>" + parameter.get("provinceZ") + "</provinceZ>");
            }
            else {
                message.append("<provinceZ></provinceZ>");
            }
            message.append("<attachs>");
            //message.append("<attach path=\"" + attachs.get(attach).get("FILE_PATH") + "\" name=\"" + attachs.get(attach).get("FILE_ID") + "." + attachs.get(attach).get("FILE_TYPE") + "\" type=\"" + attachs.get(attach).get("FILE_TYPE") + "\" value=\"" + attachs.get(attach).get("FILE_NAME") + "\" />");
            message.append("</attachs>");
            message.append("</prodInfo></prodsInfo></body></root>");
            String wsdl = wsd.queryUrl("OneDry_TwoDry_Message");
            interflog.put("INTERFNAME", "全程调测退单一干");
            interflog.put("URL", wsdl);
            interflog.put("CONTENT", message.toString());
            interflog.put("ORDERNO", parameter.get("SrvOrdId"));
            interflog.put("ID", logId);
            interflog.put("REMARK", "全程调测退单一干");
            wsd.insertInterfLogS(interflog); //保存日志报文
            Object ret = TransferInterfaceUtil.sendXmlClient(message.toString(), wsdl, "http://Interface.ws.chinaunicom.cn", "OneDry_TwoDry_Message"); //调用接口
            interflog.put("RETURNCONTENT", ret.toString());
            wsd.updateInterfLog(interflog); //修改日志表返回结果
            Map<String, Object> res = AnalysisXML.analysis(ret.toString());
            if ("0".equals(res.get("resultFlag"))) {
                springJdbcTemplate.update("UPDATE GOM_BDW_SRV_ORD_INFO SET QCWOORDERCODE = NULL WHERE SRV_ORD_ID IN (SELECT JDB.SRV_ORD_ID FROM GOM_BDW_SRV_ORD_INFO JDB JOIN GOM_BDW_SRV_ORD_INFO JDY ON JDY.TRADE_TYPE_CODE=JDB.TRADE_TYPE_CODE AND JDY.TRADE_ID=JDY.TRADE_ID AND JDY.SRV_ORD_ID=" + parameter.get("SrvOrdId") + ")");
                urn.put("flag", "success");
                urn.put("msg", "全程调测退单一干成功");
            }
            else {
                urn.put("flag", "fail");
                urn.put("msg", res.get("resultContent"));
            }

        }
        else {
        urn.put("flag", "fail");
        urn.put("msg", "全程调测退单一干失败参数中SRV_ORD_ID值为空");
        }
        return urn;
    }

    public Map postponementApply(Map parameter) { //延期申请
        Map<String, Object> urn =  new HashMap<String, Object>();
        Map<String, Object> operStaffInfoMap = new HashMap<>();
        operStaffInfoMap = (Map)MapUtils.getObject(parameter, "operStaffInfoMap");
        UserInfo user = ThreadLocalInfoHolder.getLoginUser();
        if (operStaffInfoMap == null){
            operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(user.getUserId()));
        }
        if (parameter.get("srvOrdId") != null && !"".equals(parameter.get("srvOrdId"))) {
            List<Map<String, Object>> bwData = springJdbcTemplate.queryForList("SELECT TRADE_ID,TRADE_TYPE_CODE,OMORDERCODE,QCWOORDERCODE,FLOW_ID FROM GOM_BDW_SRV_ORD_INFO WHERE SRV_ORD_ID=" + parameter.get("srvOrdId")); //查询报文
            StringBuilder message = new StringBuilder(); //拼接报竣报文
            int logId = wsd.querySequence("SEQ_GOM_BDW_INTERF_LOG_INFO.NEXTVAL"); //获取接口日志信息表序列
            Map<String, Object> interflog = new HashMap<String, Object>(); //记录接口日志
            message.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            message.append("<root><header>");
            message.append("<eventType>sync</eventType>");
            message.append("<interfaceCode>postponementApply</interfaceCode>"); //接口编码（必填）
            message.append("<serialNo>000000</serialNo>");
            message.append("<userId>sysAdmin</userId>");
            message.append("<password>1</password>");
            message.append("<sender>IOM</sender>");
            message.append("<province>BenDiWang</province>");
            message.append("</header><body>");
            message.append("<orderCode>" + bwData.get(0).get("TRADE_TYPE_CODE") + "</orderCode>"); //一干订单ID
            message.append("<applyPeople>"+MapUtils.getString(operStaffInfoMap,"USER_REAL_NAME")+"</applyPeople>"); //本地网和一干共有的用户名
            message.append("<applyPeoplePhone>"+MapUtils.getString(operStaffInfoMap,"USER_PHONE")+"</applyPeoplePhone>"); //本地网和一干共有的用户名手机号
            message.append("<actType>201</actType>"); //延期申请业务动作
            message.append("<prodsInfo><prodInfo>");
            message.append("<prodInstId>" + bwData.get(0).get("TRADE_ID") + "</prodInstId>"); //一干电路ID
            message.append("<oldTime>" + parameter.get("oldTime") + "</oldTime>"); //电路原要求完成时间
            message.append("<newTime>" + parameter.get("newTime") + "</newTime>"); //电路申请完成时间
            message.append("<reason>" + parameter.get("reason") + "</reason>"); //申请原因说明
            message.append("<reasonCode>" + parameter.get("applyType") + "</reasonCode>"); //申请原因编码
            if (bwData.get(0).get("QCWOORDERCODE") != null && "".equals(bwData.get(0).get("QCWOORDERCODE"))) {
                message.append("<woOrderCode>" + bwData.get(0).get("QCWOORDERCODE") + "</woOrderCode>"); //一干全程调测工单ID
            }
            else {
                message.append("<woOrderCode>" + bwData.get(0).get("FLOW_ID") + "</woOrderCode>"); //一干本地报竣工单ID
            }
            message.append("<omOrderCode>" + bwData.get(0).get("OMORDERCODE") + "</omOrderCode>"); //一干的om_order表的ID
            message.append("<woOrderId>" + parameter.get("SrvOrdId") + "</woOrderId>"); //本地网工单ID（这里上传的是SRV_ORD_ID用于延期反馈时使用）
            message.append("<attachs>");
            List<Map<String, Object>> fileData = (List<Map<String, Object>>) MapUtils.getObject(parameter, "fileData");

            for (Map<String, Object> fileInfo : fileData) {
                message.append("<attach path=\"").append(MapUtils.getString(fileInfo,"","ftpattach"))
                        .append("\" name=\"").append(MapUtils.getString(fileInfo,"fileId")).append("." + MapUtils.getString(fileInfo,"fileType"))
                        .append("\" type=\"").append( MapUtils.getString(fileInfo,"fileType"))
                        .append("\" value=\"").append( MapUtils.getString(fileInfo,"fileName")).append( "\" />");
            }
            message.append("</attachs>");
            message.append("</prodInfo></prodsInfo></body></root>");
            String wsdl = wsd.queryUrl("OneDry_TwoDry_Message");
            interflog.put("URL", wsdl);
            interflog.put("INTERFNAME", "延期申请");
            interflog.put("CONTENT", message.toString());
            interflog.put("ORDERNO", parameter.get("srvOrdId"));
            interflog.put("ID", logId);
            interflog.put("REMARK", "延期申请");
            wsd.insertInterfLogS(interflog); //保存日志报文
            Object ret = TransferInterfaceUtil.sendXmlClient(message.toString(), wsdl, "http://Interface.ws.chinaunicom.cn", "OneDry_TwoDry_Message"); //调用接口
            interflog.put("RETURNCONTENT", ret.toString());
            wsd.updateInterfLog(interflog); //修改日志表返回结果
            Map<String, Object> res = AnalysisXML.analysis(ret.toString());
            if ("0".equals(res.get("resultFlag"))) {
                springJdbcTemplate.update("UPDATE GOM_BDW_SRV_ORD_INFO SET QCWOORDERCODE = NULL WHERE SRV_ORD_ID=" + parameter.get("srvOrdId"));
                urn.put("flag", "success");
                urn.put("msg", "延期申请成功");
            }
            else {
                urn.put("flag", "fail");
                urn.put("msg", res.get("resultContent"));
            }

        }
        else {
            urn.put("flag", "fail");
            urn.put("msg", "延期申请失败参数中SRV_ORD_ID值为空");
        }
        return urn;
    }

    public List<Map<String, Object>> completeInformationQueryReport(Map<String,Object> params) { //报竣信息查询
        Map<String, Object> urn =  new HashMap<String, Object>();
        List<Map<String, Object>> completeInfoList = new ArrayList<>();
        String srvOrdId = MapUtils.getString(params, "srvOrdId");
        if (srvOrdId != null && !"".equals(srvOrdId)) {
            List<Map<String, Object>> bwData = springJdbcTemplate.queryForList("SELECT SO.TRADE_ID, CO.ONEDRY_AREA_CODE  FROM GOM_BDW_SRV_ORD_INFO SO JOIN GOM_BDW_CST_ORD CO ON CO.CST_ORD_ID=SO.CST_ORD_ID  WHERE SO.SRV_ORD_ID = " + srvOrdId); //查询报文
            StringBuilder message = new StringBuilder(); //拼接报竣报文
            int logId = wsd.querySequence("SEQ_GOM_BDW_INTERF_LOG_INFO.NEXTVAL"); //获取接口日志信息表序列
            Map<String, Object> interflog = new HashMap<String, Object>(); //记录接口日志
            message.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            message.append("<root><header><eventType>sync</eventType><interfaceCode>completeInformationQueryReport</interfaceCode>"); //接口编码（必填）
            message.append("<serialNo>000000</serialNo><userId>sysAdmin</userId><password>1</password><sender>IOM</sender><province>");
            message.append(MapUtils.getString(bwData.get(0),"ONEDRY_AREA_CODE"));
            message.append("</province></header><body>");
            message.append("<prodInstId>" + bwData.get(0).get("TRADE_ID") + "</prodInstId>"); //一干电路ID
            message.append("</body></root>");
            String wsdl = wsd.queryUrl("OneDry_TwoDry_Message");
            interflog.put("URL", wsdl);
            interflog.put("CONTENT", message.toString());
            interflog.put("ORDERNO", srvOrdId);
            interflog.put("INTERFNAME", "对端联系人信息接口");
            interflog.put("ID", logId);
            interflog.put("REMARK", "报竣信息查询");
            wsd.insertInterfLogS(interflog); //保存日志报文
            Object ret = TransferInterfaceUtil.sendXmlClient(message.toString(), wsdl, "http://Interface.ws.chinaunicom.cn", "OneDry_TwoDry_Message"); //调用接口
            interflog.put("RETURNCONTENT", ret.toString());
            wsd.updateInterfLog(interflog); //修改日志表返回结果
            urn = AnalysisXML.analysis(ret.toString());
            Map<String, Object> body = MapUtils.getMap(urn, "body");
            if(MapUtils.getInteger(body,"resultFlag") == 0){
                Map<String, Object> completeList = MapUtils.getMap(body,"completeList");
                try {
                    completeInfoList = (List<Map<String, Object>>) completeList.get("completeInfo");
                } catch (Exception e) {
                    completeInfoList.add(MapUtils.getMap(completeList,"completeInfo"));
                }

            }
        }
        else {
            urn.put("flag", "fail");
            urn.put("msg", "报竣信息查询失败参数中SRV_ORD_ID值为空");
        }
        return completeInfoList;
    }
}
