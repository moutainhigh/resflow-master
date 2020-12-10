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
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 资源接口-客户端
 * 业务实例创建接口实现
 * Created by csq on 2018/12/25.
 */
@Service
public class BusinessCreateService implements BusinessCreateServiceIntf {
    private static Logger logger = LoggerFactory.getLogger(BusinessCreateService.class);

    @Autowired
    private WebServiceDao rsd; //数据库操作-对象

    @Autowired
    private OrderQrySecondaryDao orderQrySecondaryDao;

    @Autowired
    private ResInterfaceLogger resInterfaceLogger;



    public Map businessCreate(Map<String, Object> params) { //gom_bdw_srv_ord_info.SRV_ORD_ID
        String id = MapUtils.getString(params, "srvOrdId");
        Map retmap = new HashMap();
        /**
         * 并行核查流程不调用资源接口
         * 首先根据srvOrdId查询流程id，然后进行判断是否并行核查
         */
        Map<String,Object> psInfo = rsd.qryOrdPsIdBySrvOrdId(id);
        if(!MapUtils.isEmpty(psInfo) && BasicCode.SECONDARY_PARALLEL_CHECK.equals(
                MapUtils.getString(psInfo,"PS_ID",""))){
            retmap.put("returncode", "成功");
            return retmap;
        }
        String flag = MapUtils.getString(params, "flag",BasicCode.LOCALBUILD);
      //  String flag = BasicCode.LOCALBUILD;
        logger.info("业务实例创建接口开始！" + id);
        String json = "";
        String url =rsd.queryUrl("ResBusinessCreate");
        String zy_response = "";
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置日期格式
        Map resmap = new HashMap();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("interfname", "业务实例创建接口");
        map.put("url", url);
        map.put("returncontent", "");
        map.put("createdate", df.format(new Date()));
        map.put("orderno", id); //业务订单id
        try {
            //生成json报文
            //1.查数据库数据  (用来拼报文)0
            if (BasicCode.LOCALBUILD.equals(flag)){
                Map resmapTemp = rsd.queryResInfo(id);
                resmap.putAll(resmapTemp);
            }else if (BasicCode.SECONDARY.equals(flag) || BasicCode.JIKE.equals(flag)) {
                Map resmapTemp = rsd.queryResInfoFromSec(id);
                resmap.putAll(resmapTemp);
            } else if(BasicCode.RES_SUPPLEMENT.equals(flag)){
                Map resmapTemp = rsd.queryResInfoBySupplement(id);
                resmap.putAll(resmapTemp);
            }
            Map jsonMap = createJson(resmap);
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
            Map respons = HttpClientJson.sendHttpPost(url, json);
            map.put("updatedate", df.format(new Date()));
            //3.调对方接口，发json报文 ，接收返回json报文
            if("200".equals(respons.get("code"))){
                zy_response = respons.get("msg").toString();
                map.put("returncontent", zy_response);
                map.put("remark", "资源业务实例创建接口返回成功");
                //4.报文入库，数据入库
                this.saveEventJson(map);
//                rsd.saveJson(map);
            } else {
                map.put("returncontent", respons.get("code"));
                map.put("remark", "");
                //4.报文入库，数据入库
                this.saveEventJson(map);
//                rsd.saveJson(map);
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
//            rsd.saveJson(map);
            return retmap;
        }
        try {
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
            rmap.put("attr_action", "ResBusinessCreate");
            rmap.put("attr_code", respCode);
            rmap.put("attr_name", "");
            rmap.put("attr_value_name", "业务实例创建接口返回结果");
            rmap.put("create_date", df.format(new Date()));
            rmap.put("sourse", "");
            //6.返回结果
            if ("1".equals(respCode)) {
                retmap.put("returncode", "成功");
                retmap.put("returndec", respDesc);
                this.saveEventRetInfo(rmap);
                // 创建接口成功后，删除上次回滚接口成功的记录，方便下次继续调用回滚接口
               /* if(BasicCode.RES_SUPPLEMENT.equals(flag)) {
                    rsd.deleteResAttrCodeSupple(MapUtils.getString(resmap,"SRV_ORD_ID"),"BusinessRollback");
                }else{*/
                    this.deleteEventRetAttrInfo(MapUtils.getString(resmap,"SRV_ORD_ID"),"delete","BusinessRollback",null);
              //  }
            } else if ("0".equals(respCode)) {
                retmap.put("returncode", respCode);
                retmap.put("returndec", respDesc);
            }
            logger.info("业务实例创建接口结束---" + respCode + "---" + respDesc);
        }
        catch (Exception e) {
            logger.error("接口返回报文格式异常！异常信息：" + e.getMessage(),e);
            retmap.put("returncode", "失败");
            retmap.put("returndec", "接口返回报文格式异常：" + e.getMessage());
        }
        return retmap;
    }

    public void saveEventJson(Map<String, Object> map){
        ResInterfaceLog resInterfaceLog = new ResInterfaceLog();
        resInterfaceLog.setInterfName(MapUtils.getString(map ,"interfname"));
        //resInterfaceLog.setCreateDate(new Date());
        resInterfaceLog.setOrderNo(MapUtils.getString(map ,"orderno"));
        resInterfaceLog.setRemark(MapUtils.getString(map ,"remark"));
        resInterfaceLog.setReturnContent(MapUtils.getString(map ,"returncontent"));
        //resInterfaceLog.setUpdateDate(new Date());
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
        //rsd.saveRetInfo(rmap);
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
        List<Map<String, Object>> cirlist = null;
        // 资源补录标志
        boolean resSupplement = BasicCode.ACTIVE_TYPE_SUPPLEMENT.equals(MapUtils.getString(map,"ACTIVE_TYPE"));
        // 资源补录
        if(resSupplement){
            Set<String> set = new HashSet<>();
            set.add(BasicCode.LOCAL);
            set.add(BasicCode.SECOND);
            if(set.contains(MapUtils.getString(map,"RESOURCES",""))){
                // 如果电路在本系统调度过，根据instance_id查询上次调度的内容
                List<Map<String,Object>> srvList = rsd.qrySrvOrdIdLast(MapUtils.getString(map,"PRODINSTID"));
                if(srvList!=null &&srvList.size()>0){
                    cirlist = rsd.queryCirInfo(MapUtils.getString(srvList.get(0),"SRV_ORD_ID"));
                    map.put("RESOURCES", MapUtils.getString(srvList.get(0), "RESOURCES"));
                }
            }
        } else {
            cirlist = rsd.queryCirInfo(MapUtils.getString(map, "SRV_ORD_ID"));
        }
        //2.拼报文
        JSONObject json = new JSONObject();
        JSONObject request = new JSONObject();
        JSONObject requestBody = new JSONObject();
        JSONObject prodInfo = new JSONObject();
        JSONObject prodAttrInfo = new JSONObject();
        JSONArray rows = new JSONArray();
        JSONObject custInfo = new JSONObject();
        Map<String,Object> retmap = new HashMap<>();
        try {
            if (null != check(map)) {
                retmap.put("success",false);
                retmap.put("json",check(map));
                return retmap;
            }
            map = defaultValue(map);
            //添加单据来源
            Map<String, Object> codeInfo = new HashMap<>();
            codeInfo.put("codeType", "RESOURCE_FROM");
            codeInfo.put("codeValue", MapUtils.getString(map, "RESOURCES"));
            List<Map<String, Object>> mapList = rsd.queryCodeInfo2(codeInfo);
            JSONObject resource = new JSONObject();
            resource.put("attrAction", 0);
            resource.put("attrCode", "RESOURCE_FROM");
            resource.put("attrDesc", "单据来源");
            if(mapList.size() > 0 ){
                resource.put("attrValue",MapUtils.getString(mapList.get(0), "CODE_CONTENT","20038"));
            }
            prodInfo.put("custOrderCode", MapUtils.getString(map, "SUBSCRIBE_ID","")); //客户订单编号
            prodInfo.put("crmOrderCode", MapUtils.getString(map,"CRMORDERCODE","")); //业务订单号
            prodInfo.put("serviceOrderCode", "RES_BusinessCreate");
            prodInfo.put("resflowWorkId", MapUtils.getString(map,"SRV_ORD_ID","")); //SRV_ORD_ID
            prodInfo.put("prodInstId",MapUtils.getString(map,"PRODINSTID",""));
            prodInfo.put("productId", MapUtils.getString(map,"SERVICE_ID",""));
            prodInfo.put("accNbr", MapUtils.getString(map,"SERIAL_NUMBER","")); //业务号码

            if(resSupplement){
                prodInfo.put("regionCode",MapUtils.getString(map, "REGION_ID","") );
            } else{
                // 受理区域：白沙县分公司--查找到海南省的org_id
                String regionCodeName = MapUtils.getString(map,"HANDLE_DEP_ID","");
                String regionCode = rsd.qryRegionCode(regionCodeName);
                prodInfo.put("regionCode",regionCode );
            }
            prodInfo.put("acceptTime", MapUtils.getString(map,"CREATE_DATE",""));
            prodInfo.put("needFinishTime", MapUtils.getString(map,"NEED_FINISH_TIME","")); //要求完成时间
            // 特殊：如果是核查单，那么动作类型类型改为核查
            if("102".equals(MapUtils.getString(map,"ORDER_TYPE"))){
                map.put("ACTIVE_TYPE",BasicCode.ACTIVE_TYPE_CHECK);
            }
            prodInfo.put("actionType", MapUtils.getString(map,"ACTIVE_TYPE",""));
            prodInfo.put("addrId", "0"); //受理地址id
            String addrName = MapUtils.getString(map,"ADDR_NAME","0");
            prodInfo.put("addrName", addrName==""?"0":addrName); //受理地址必填

            if(cirlist!=null && cirlist.size()>0){
                for (Map<String, Object> cir : cirlist) {
                    JSONObject row = new JSONObject();
                    row.put("attrAction", cir.get("ATTR_ACTION"));
                    row.put("attrCode", cir.get("ATTR_CODE"));
                    row.put("attrValue", cir.get("ATTR_VALUE"));
                    row.put("attrDesc", cir.get("ATTR_NAME"));
                    rows.add(row);
                }
                rows.add(resource);
            }

            /**
             * 添加电路编码是否可以编写节点 CIRCUIT_NO_EDIT_FLAG
             * 一干来单 CIRCUIT_NO_EDIT_FLAG = 0  不可编辑
             * 二干自启，集客来单 CIRCUIT_NO_EDIT_FLAG = 1 可以编辑
             * 资源补录，允许修改电路编号 CIRCUIT_NO_EDIT_FLAG = 1 可以编辑
             */
            String circuitNoEditFlag = "0";
            if(resSupplement){
                circuitNoEditFlag = "1";
            } else{
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("srvOrdId",MapUtils.getString(map, "SRV_ORD_ID"));
                Map<String, Object> belongSysMap = orderQrySecondaryDao.qrySrvOrderBelongSys(params);
                String RESOURCES = MapUtils.getString(belongSysMap, "RESOURCES");
                if (BasicCode.ONEDRY.equals(RESOURCES)){
                    circuitNoEditFlag = "0";
                }else if (BasicCode.SECONDARY.equals(RESOURCES) || BasicCode.JIKE.equals(RESOURCES)){
                    circuitNoEditFlag = "1";
                }
            }

            JSONObject rowFlag = new JSONObject();
            rowFlag.put("attrAction", "0");
            rowFlag.put("attrCode", "CIRCUIT_NO_EDIT_FLAG");
            rowFlag.put("attrValue", circuitNoEditFlag);
            rowFlag.put("attrDesc", "电路编码是否可以编写");
            rows.add(rowFlag);
            JSONObject circuit_areaFlag = new JSONObject();
            circuit_areaFlag.put("attrAction", "0");
            circuit_areaFlag.put("attrCode", "CIRCUIT_AREA");
            circuit_areaFlag.put("attrValue", MapUtils.getString(map, "REGION_ID"));
            circuit_areaFlag.put("attrDesc", " 电路所属区域");
            rows.add(circuit_areaFlag);
            JSONObject circuit_levelFlag = new JSONObject();
            circuit_levelFlag.put("attrAction", "0");
            circuit_levelFlag.put("attrCode", "CIRCUIT_LEVEL");
            circuit_levelFlag.put("attrValue", "ergan");
            circuit_levelFlag.put("attrDesc", "电路等级");
            rows.add(circuit_levelFlag);

            custInfo.put("custType", "1");
            custInfo.put("custNo", MapUtils.getString(map, "CUST_ID","default"));
            custInfo.put("custName", MapUtils.getString(map, "CUST_NAME_CHINESE","default"));
            custInfo.put("contactName", MapUtils.getString(map, "CUST_CONTACT_MAN_NAME","default"));
            custInfo.put("contactTel", MapUtils.getString(map, "CUST_TEL",""));
            custInfo.put("contactAddr", MapUtils.getString(map, "CUST_ADDRESS",""));
            custInfo.put("custLevel", "客户等级"); //客户等级
            custInfo.put("custLinkMan", MapUtils.getString(map, "CUST_CONTACT_MAN_NAME",""));
            custInfo.put("custLinkTel", MapUtils.getString(map, "CUST_CONTACT_MAN_TEL",""));
            prodAttrInfo.put("row", rows);
            requestBody.put("prodAttrInfo", prodAttrInfo);
            requestBody.put("custInfo", custInfo);
            requestBody.put("prodInfo", prodInfo);

            request.put("requestHeader", HttpClientJson.requestHeader(map, "ResBusinessCreate"));
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

    public String check(Map<String,Object> map) {
        Set<String> paramSet = new HashSet<String>();
        paramSet.add("SERVICE_ID");
        paramSet.add("ACTIVE_TYPE");

        for(String key:paramSet){
            if(null == map.get(key) || "".equals(map.get(key))){
                return "必填项[" + key +"]不能为空";
            }
        }
        return null;
    }


}
