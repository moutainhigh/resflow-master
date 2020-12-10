package com.zres.project.localnet.portal.webservice.res;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderQrySecondaryDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.logInfo.entry.ResCreateInstanceFlag;
import com.zres.project.localnet.portal.logInfo.entry.ResInterfaceLog;
import com.zres.project.localnet.portal.logInfo.entry.ResRollbackLog;
import com.zres.project.localnet.portal.logInfo.service.ResInterfaceLogger;
import com.zres.project.localnet.portal.logInfo.until.LoggerThreadPool;
import com.zres.project.localnet.portal.util.HttpClientJson;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.zres.project.localnet.portal.webservice.dto.RenamCustInfoDTO;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 资源接口-客户端
 * 过户接口实现
 */
@Service
public class TransferUserService implements TransferUserServiceIntf {
    private static Logger logger = LoggerFactory.getLogger(TransferUserService.class);

    @Autowired
    private WebServiceDao webServiceDao; //数据库操作-对象

    @Autowired
    private ResInterfaceLogger resInterfaceLogger;

    public Map transferUser(Map<String, Object> params) {
        String id = MapUtils.getString(params, "SRV_ORD_ID");
        logger.info("过户接口开始！" + id);
        String json = "";
        String url =webServiceDao.queryUrl("TransferUser");
        String zy_response = "";
        Map retmap = new HashMap();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置日期格式
        Map resmap = new HashMap();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("interfname", "资源过户接口");
        map.put("url", url);
        map.put("returncontent", "");
        map.put("createdate", df.format(new Date()));
        map.put("orderno", id); //业务订单id
        try {
            //生成json报文
            Map jsonMap = createJson(params);
            if(MapUtils.getBoolean(jsonMap,"success")){
                json = MapUtils.getString(jsonMap,"json");
                map.put("content", json);
            } else {
                retmap.put("returncode", "失败");
                retmap.put("returndec", MapUtils.getString(jsonMap,"json"));
                map.put("updatedate", df.format(new Date()));
                map.put("content", MapUtils.getString(retmap,"returndec"));
                map.put("remark", "拼接报文异常");
                this.saveEventJson(map);
//                rsd.saveJson(map);
                return retmap;
            }
            logger.info("发送报文：" + json);
        }
        catch (Exception e) {
            logger.error("拼接报文异常！异常信息：" + e.getMessage(),e);
            retmap.put("returncode", "失败");
            retmap.put("returndec", "拼接报文异常：" + e.getMessage());
            map.put("updatedate", df.format(new Date()));
            map.put("content", MapUtils.getString(retmap,"returndec"));
            map.put("remark", "拼接报文异常");
            this.saveEventJson(map);
//            rsd.saveJson(map);
            return retmap;
        }
        try {
            Map respons = HttpClientJson.httpPostRequest(url, json);
            map.put("updatedate", df.format(new Date()));
            //3.调对方接口，发json报文 ，接收返回json报文
            if("200".equals(respons.get("code"))){
                zy_response = respons.get("msg").toString();
                map.put("returncontent", zy_response);
                map.put("remark", "资源过户接口返回成功");
                //4.报文入库，数据入库
                this.saveEventJson(map);
//                rsd.saveJson(map);
            } else {
                map.put("returncontent", respons.get("code"));
                map.put("remark", "");
                //4.报文入库，数据入库
                this.saveEventJson(map);
                retmap.put("returncode", "失败");
                retmap.put("returndec", "资源系统创建接口返回异常！code:" + respons.get("code"));
                return retmap;
            }
            logger.info("资源返回报文：" + zy_response);
        }
        catch (Exception e) {
            logger.error("调资源接口异常！异常信息：" + e.getMessage(),e);
            retmap.put("returncode", "失败");
            retmap.put("returndec", "调资源接口异常：" + e.getMessage());
            map.put("updatedate", df.format(new Date()));
            map.put("returncontent", MapUtils.getString(retmap,"returndec"));
            map.put("remark", "调资源接口异常");
            this.saveEventJson(map);
            return retmap;
        }
        /*try {
            //5.解析json报文
            JSONObject response = JSONObject.fromObject(zy_response);
            String retBssBody = response.getString("response");
            JSONObject js_retBssBody = JSONObject.fromObject(retBssBody);
            String responseBody = js_retBssBody.getString("responseBody");
            JSONObject js_responseBody = JSONObject.fromObject(responseBody);
            String respCode = js_responseBody.getString("respCode");
            String respDesc = js_responseBody.getString("respDesc");
            String resflowWorkId = js_responseBody.getString("resflowWorkId");

            Map<String, Object> rmap = new HashMap<String, Object>();

            rmap.put("srv_ord_id", MapUtils.getString(resmap, "SRV_ORD_ID"));
            rmap.put("attr_value", resflowWorkId);
            rmap.put("attr_action", "TransferUser");
            rmap.put("attr_code", respCode);
            rmap.put("attr_name", "");
            rmap.put("attr_value_name", "资源过户接口返回结果");
            rmap.put("create_date", df.format(new Date()));
            rmap.put("sourse", "");
            //6.返回结果
            if ("1".equals(respCode)) {
                retmap.put("returncode", "成功");
                retmap.put("returndec", respDesc);
                this.saveEventRetInfo(rmap);
            } else if ("0".equals(respCode)) {
                retmap.put("returncode", respCode);
                retmap.put("returndec", respDesc);
            }
            logger.info("过户接口结束---" + respCode + "---" + respDesc);
        }
        catch (Exception e) {
            logger.error("接口返回报文格式异常！异常信息：" + e.getMessage(),e);
            retmap.put("returncode", "失败");
            retmap.put("returndec", "接口返回报文格式异常：" + e.getMessage());
        }*/
        return retmap;
    }

    public void saveEventJson(Map<String, Object> map){
        ResInterfaceLog resInterfaceLog = new ResInterfaceLog();
        resInterfaceLog.setInterfName(MapUtils.getString(map ,"interfname"));
        resInterfaceLog.setOrderNo(MapUtils.getString(map ,"orderno"));
        resInterfaceLog.setRemark(MapUtils.getString(map ,"remark"));
        resInterfaceLog.setReturnContent(MapUtils.getString(map ,"returncontent"));
        resInterfaceLog.setUrl(MapUtils.getString(map ,"url"));
        resInterfaceLog.setContent(MapUtils.getString(map ,"content"));
        LoggerThreadPool.addLoggerToExecute(resInterfaceLogger,resInterfaceLog);
        //rsd.saveJson(map);
    }

    public void saveEventRetInfo(Map<String, Object> rmap){
        ResCreateInstanceFlag resCreateInstanceFlag = new ResCreateInstanceFlag();
        resCreateInstanceFlag.setSrvOrdId(MapUtils.getString(rmap ,"srv_ord_id"));
        resCreateInstanceFlag.setAttrAction(MapUtils.getString(rmap ,"attr_action"));
        resCreateInstanceFlag.setAttrCode(MapUtils.getString(rmap ,"attr_code"));
        resCreateInstanceFlag.setAttrValue(MapUtils.getString(rmap ,"attr_value"));
        resCreateInstanceFlag.setAttrName(MapUtils.getString(rmap ,"attr_name"));
        resCreateInstanceFlag.setAttrValueName(MapUtils.getString(rmap ,"attr_value_name"));
        resCreateInstanceFlag.setSourse(MapUtils.getString(rmap ,"sourse"));
        LoggerThreadPool.addLoggerToExecute(resInterfaceLogger,resCreateInstanceFlag);
    }

    public void deleteEventRetAttrInfo(String srvOrdId,String flag,String interfaceName,String orderId){
        ResRollbackLog resRollbackLog = new ResRollbackLog();
        resRollbackLog.setSrvOrdId(srvOrdId);
        resRollbackLog.setOrderId(orderId);
        resRollbackLog.setFlag(flag);
        resRollbackLog.setInterfaceName(interfaceName);
        LoggerThreadPool.addLoggerToExecute(resInterfaceLogger,resRollbackLog);
    }
    public Map createJson(Map map) {

        //2.拼报文
        JSONObject json = new JSONObject();
        JSONObject request = new JSONObject();
        JSONObject requestBody = new JSONObject();
        JSONObject prodInfo = new JSONObject();
        JSONObject custInfo = new JSONObject();
        Map<String,Object> retmap = new HashMap<>();
        try {

            RenamCustInfoDTO newCustInfo = (RenamCustInfoDTO) map.get("custInfo");
            Map<String,Object> resmap = webServiceDao.queryResInfo(MapUtils.getString(map,"SRV_ORD_ID",""));
            map = defaultValue(map);
            prodInfo.put("custOrderCode", MapUtils.getString(map, "SUBSCRIBE_ID","")); //客户订单编号
            prodInfo.put("crmOrderCode", MapUtils.getString(map,"SRV_ORD_ID","")); //业务订单号 SRV_ORD_ID
            prodInfo.put("resflowWorkId", MapUtils.getString(map,"SRV_ORD_ID","")); //SRV_ORD_ID
            prodInfo.put("prodInstId",MapUtils.getString(map,"INSTANCE_ID",""));

            custInfo.put("custType", newCustInfo.getCustType());
            custInfo.put("custNo", newCustInfo.getCustId());
            custInfo.put("custName", newCustInfo.getCustNameChinese());
            custInfo.put("contactName", newCustInfo.getCustContactManName());
            custInfo.put("contactTel", newCustInfo.getCustTel());
            custInfo.put("contactAddr", newCustInfo.getCustAddress());
            custInfo.put("custLevel", "客户等级"); //客户等级
            requestBody.put("custInfo", custInfo);
            requestBody.put("prodInfo", prodInfo);

            request.put("requestHeader", HttpClientJson.requestHeader(resmap, "Transfer_User"));
            request.put("requestBody", requestBody);
            json.put("request", request);
            System.out.println(json.toString());
        }
        catch (Exception e) {
            logger.error("接口报文拼接异常！异常信息：" + e.getMessage() ,e);
        }
        retmap.put("success",true);
        retmap.put("json",json);
        return retmap;
    }

    public Map defaultValue(Map<String,Object> map){
        for(String key : map.keySet()){
            if(null==map.get(key)){
                map.put(key,"");
            }
        }
        return map;
    }



}
