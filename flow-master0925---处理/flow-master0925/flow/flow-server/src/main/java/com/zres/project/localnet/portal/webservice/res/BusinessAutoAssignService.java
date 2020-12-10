package com.zres.project.localnet.portal.webservice.res;

import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.logInfo.entry.ResCreateInstanceFlag;
import com.zres.project.localnet.portal.logInfo.entry.ResInterfaceLog;
import com.zres.project.localnet.portal.logInfo.service.ResInterfaceLogger;
import com.zres.project.localnet.portal.logInfo.until.LoggerThreadPool;
import com.zres.project.localnet.portal.util.HttpClientJson;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.res.frame.core.util.MapUtil;
import net.sf.json.JSONObject;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 资源接口-客户端
 * 业务电路汇总接口实现
 * Created by Skyla on 2018/12/25.
 */
@Service
public class BusinessAutoAssignService implements BusinessAutoAssignServiceIntf {
    private static Logger logger = LoggerFactory.getLogger(BusinessAutoAssignService.class);

    @Autowired
    private WebServiceDao rsd; //数据库操作-对象

    @Autowired
    private ResInterfaceLogger resInterfaceLogger;

    @Override
    public Map businessAutoAssign(Map<String, Object> params) { //gom_bdw_srv_ord_info.SRV_ORD_ID
        String id = MapUtils.getString(params, "srvOrdId");
        String flag = MapUtils.getString(params, "flag");
        logger.info("业务电路汇总接口开始！" + id);
        Map retmap = new HashMap();
        /**
         * 并行核查流程不调用资源接口
         * 首先根据srvOrdId查询流程id，然后进行判断是否并行核查
         */
        Map<String,Object> psInfo = rsd.qryOrdPsIdBySrvOrdId(id);
        if(!MapUtils.isEmpty(psInfo) && BasicCode.LOCAL_PRARLLEL_CHECK_FLOW.equals(
                MapUtils.getString(psInfo,"PS_ID",""))){
            retmap.put("returncode", "成功");
            return retmap;
        }
        String json = "";
        String url =rsd.queryUrl("BusinessAutoAssign");
        String zyResponse = "";
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置日期格式
        Map resmap = new HashMap();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("interfname", "业务电路汇总接口");
        map.put("url", url);
        map.put("createdate", df.format(new Date()));
        map.put("returncontent", zyResponse);
        map.put("orderno", id); //业务订单id
        try {
            //生成json报文
            //1.查数据库数据  (用来拼报文)
            if (BasicCode.LOCALBUILD.equals(flag)){
                Map resmapCode = rsd.queryResInfo(id);
                resmap.putAll(resmapCode);
            }else if (BasicCode.SECONDARY.equals(flag)) {
                Map resmapCode = rsd.queryResInfoFromSec(id);
                resmap.putAll(resmapCode);
            } else if(BasicCode.RES_SUPPLEMENT.equals(flag)){
                Map resmapCode = rsd.queryResInfoBySupplement(id);
                resmap.putAll(resmapCode);
            }
            // 特殊：如果是核查单，则不调汇总接口，[1978802]
            if("102".equals(MapUtils.getString(resmap,"ORDER_TYPE"))){
                retmap.put("returncode", "成功");
                retmap.put("returndec", "核查单不调汇总接口");
                return retmap;
            }
            resmap.put("relaCrmOrderCodes",params.get("relaCrmOrderCodes"));
            logger.info("报文数据resmap：" + resmap);
            json = createJson(resmap);
            map.put("content", json);
            logger.info("发送报文：" + json);
        }
        catch (Exception e) {
            logger.error("拼接报文异常！异常信息：" + e.getMessage(),e);
            retmap.put("returncode", "失败");
            retmap.put("returndec", "拼接报文异常！");
            map.put("remark", "拼接报文异常");
            map.put("content", MapUtils.getString(retmap,"returndec"));
            map.put("updatedate", df.format(new Date()));
            this.saveEventJson(map);
//            rsd.saveJson(map);
            return retmap;
        }

        map.put("url", url);
        try {
            //3.调对方接口，发json报文 ，接收返回json报文
            Map respons = HttpClientJson.sendHttpPost(url, json);
      //      zyResponse = respons.get("msg").toString();
            map.put("updatedate", df.format(new Date()));
            //3.调对方接口，发json报文 ，接收返回json报文
            if("200".equals(respons.get("code"))){
                zyResponse = respons.get("msg").toString();
                map.put("returncontent", zyResponse);
                map.put("remark", "资源业务电路汇总接口返回成功");
                this.saveEventJson(map);
//                rsd.saveJson(map);
            } else {
                retmap.put("returncode", "失败");
                retmap.put("returndec", "资源业务电路汇总接口返回异常！code:" + respons.get("code"));
                map.put("returncontent", MapUtils.getString(retmap,"returndec"));
                map.put("remark", "");
                this.saveEventJson(map);
//                rsd.saveJson(map);
                return retmap;
            }
            logger.info("资源返回报文：" + zyResponse);
        }
        catch (Exception e) {
            logger.error("调资源接口异常！异常信息：" + e.getMessage(),e);
            retmap.put("returncode", "失败");
            retmap.put("returndec", "调资源接口异常：" + e.getMessage());
            map.put("remark", "调资源接口异常");
            map.put("returncontent", MapUtils.getString(retmap,"returndec"));
            map.put("updatedate", df.format(new Date()));
            this.saveEventJson(map);
//            rsd.saveJson(map);
            return retmap;
        }
        try {
            //4.解析json报文
            JSONObject response = JSONObject.fromObject(zyResponse);
            String retBssBody = response.getString("response");
            JSONObject jsRetBssBody = JSONObject.fromObject(retBssBody);
            String responseBody = jsRetBssBody.getString("responseBody");
            JSONObject jsResponseBody = JSONObject.fromObject(responseBody);
            String respCode = jsResponseBody.getString("respCode");
            String respDesc = jsResponseBody.getString("respDesc");
            String resflowWorkId = jsResponseBody.getString("resflowWorkId");
            // 1.6版本接口协议新增业务电路状态和业务路由

            Map<String, Object> rmap = new HashMap<String, Object>();
           /* if(BasicCode.RES_SUPPLEMENT.equals(flag)) {
                rmap.put("srv_ord_id", "0");
                rmap.put("attr_value", MapUtils.getString(resmap, "SRV_ORD_ID"));
            } else{*/
                rmap.put("srv_ord_id", MapUtils.getString(resmap, "SRV_ORD_ID"));
                rmap.put("attr_value", resflowWorkId);
         //   }

            rmap.put("attr_action", "BusinessAutoAssign");
            rmap.put("attr_code", respCode);
            rmap.put("attr_name", "");
            rmap.put("attr_value_name", "业务电路汇总接口返回结果");
            rmap.put("create_date", df.format(new Date()));
            rmap.put("sourse", "res");

            //6.返回结果
            if ("1".equals(respCode)) {
                retmap.put("returncode", "成功");
                retmap.put("returndec", respDesc);
                retmap.put("resflowWorkId", resflowWorkId);
                retmap.put("url", url);
                retmap.put("json", json);
                //5.报文入库，数据入库
                this.saveEventRetInfo(rmap);
                afterAssign(jsResponseBody,MapUtils.getString(resmap, "SRV_ORD_ID"),flag);
                if(params.containsKey("originSrvOrdId")){
                    afterAssign(jsResponseBody,MapUtils.getString(params, "originSrvOrdId"),"");
                }
            }
            else if ("0".equals(respCode)) {
                retmap.put("returncode", "失败");
                retmap.put("returndec", respDesc);
                return retmap;
            }
            logger.info("业务电路汇总接口结束---" + respCode + "---" + respDesc);
        }
        catch (Exception e) {
            logger.error("接口返回报文格式异常！异常信息：" + e.getMessage(),e);
            retmap.put("returncode", "失败");
            retmap.put("returndec", "接口返回报文格式异常！返回报文：" + zyResponse );
            return retmap;
        }
        return retmap;
    }

    public  String createJson(Map map) {
        //2.拼报文
        JSONObject json = new JSONObject();
        JSONObject request = new JSONObject();
        JSONObject requestBody = new JSONObject();
        try {
            // 资源补录标志
            boolean resSupplement = BasicCode.ACTIVE_TYPE_SUPPLEMENT.equals(MapUtils.getString(map,"ACTIVE_TYPE"));
            if(resSupplement){
                requestBody.put("regionCode",MapUtils.getString(map, "REGION_ID","") );
            } else{
                // 受理区域：白沙县分公司--查找到海南省的org_id
                String regionCodeName = MapUtils.getString(map,"HANDLE_DEP_ID","");
                String regionCode = rsd.qryRegionCode(regionCodeName);
                requestBody.put("regionCode",regionCode );
            }
            requestBody.put("crmOrderCode", MapUtils.getString(map,"CRMORDERCODE"));
            requestBody.put("accNbr", MapUtils.getString(map, "SERIAL_NUMBER","0"));
            requestBody.put("prodInstId", MapUtils.getString(map,"PRODINSTID"));
            // 特殊：如果是核查单，那么动作类型类型改为核查
            if("102".equals(MapUtils.getString(map,"ORDER_TYPE"))){
                map.put("ACTIVE_TYPE","107");
            }
            requestBody.put("actionType", MapUtils.getString(map, "ACTIVE_TYPE"));
            requestBody.put("resflowWorkId", MapUtils.getString(map,"SRV_ORD_ID"));
            requestBody.put("relaProdInstIds", MapUtils.getString(map,"relaCrmOrderCodes"));
            requestBody.put("remark", MapUtils.getString(map, "REMARK",""));

            request.put("requestHeader", HttpClientJson.requestHeader(map, "BusinessAutoAssign"));
            request.put("requestBody", requestBody);
            json.put("request", request);
        }
        catch (Exception e) {
            logger.error("接口报文拼接异常！异常信息：" + e.getMessage(),e);
        }
        return json.toString();
    }

    public void saveEventJson(Map<String, Object> map){
        ResInterfaceLog resInterfaceLog = new ResInterfaceLog();
        resInterfaceLog.setInterfName(MapUtil.getString(map ,"interfname"));
        //resInterfaceLog.setCreateDate(new Date());
        resInterfaceLog.setOrderNo(MapUtil.getString(map ,"orderno"));
        resInterfaceLog.setRemark(MapUtil.getString(map ,"remark"));
        resInterfaceLog.setReturnContent(MapUtil.getString(map ,"returncontent"));
        //resInterfaceLog.setUpdateDate(new Date());
        resInterfaceLog.setUrl(MapUtil.getString(map ,"url"));
        resInterfaceLog.setContent(MapUtil.getString(map ,"content"));
        LoggerThreadPool.addLoggerToExecute(resInterfaceLogger,resInterfaceLog);
        //rsd.saveJson(map);
    }

    public void saveEventRetInfo(Map<String, Object> rmap){
        ResCreateInstanceFlag resCreateInstanceFlag = new ResCreateInstanceFlag();
        resCreateInstanceFlag.setSrvOrdId(MapUtil.getString(rmap ,"srv_ord_id"));
        resCreateInstanceFlag.setAttrAction(MapUtil.getString(rmap ,"attr_action"));
        resCreateInstanceFlag.setAttrCode(MapUtil.getString(rmap ,"attr_code"));
        resCreateInstanceFlag.setAttrValue(MapUtil.getString(rmap ,"attr_value"));
        resCreateInstanceFlag.setAttrName(MapUtil.getString(rmap ,"attr_name"));
        resCreateInstanceFlag.setAttrValueName(MapUtil.getString(rmap ,"attr_value_name"));
        resCreateInstanceFlag.setSourse(MapUtil.getString(rmap ,"sourse"));
        LoggerThreadPool.addLoggerToExecute(resInterfaceLogger,resCreateInstanceFlag);
        //rsd.saveRetInfo(rmap);
    }

    /**
     * 汇总接口成功之后的操作：更新电路编号，入库业务路由
     */
    public void afterAssign(JSONObject jsResponseBody,String srvOrdId,String flag){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置日期格式
        String circuitNo = jsResponseBody.getString("circuitNo");
        JSONObject circuitInfo = jsResponseBody.getJSONObject("circuitInfo");
        String oprStateId = circuitInfo.getString("oprStateId");
        String oprState = circuitInfo.getString("oprState");
        String routeInfo = circuitInfo.getString("routeInfo");
        if(BasicCode.RES_SUPPLEMENT.equals(flag)){
            // todo 不确定资源补录汇总后是否需要修改电路名称
        }else{
            // 查询电路编号，如果已存在，就更改信息，如果不存在就新增
            String attrInfoId = rsd.qryResCircuitCode(srvOrdId);
            if (attrInfoId != null && !("null".equals(attrInfoId))){
                rsd.updateResCircuitCode(attrInfoId,circuitNo);
            } else {
                Map<String, Object> tempMap = new HashMap<String, Object>();
                tempMap.put("srv_ord_id", srvOrdId);
                tempMap.put("attr_action", "0");
                tempMap.put("attr_code", "20000064");
                tempMap.put("attr_name", "电路编号");
                tempMap.put("attr_value", circuitNo);
                tempMap.put("attr_value_name", "circuitCode");
                tempMap.put("create_date", df.format(new Date()));
                tempMap.put("sourse", "res");
                this.saveEventRetInfo(tempMap);
            }
        }
        // 入库业务电路的业务状态和业务路由,并把之前的记录state改为10X
        rsd.updateStateBySrvOrdId(srvOrdId);
        Map<String,Object> paramMap = new HashMap<String,Object>();
        paramMap.put("srvOrdId",srvOrdId);
        paramMap.put("circuitNo",circuitNo);
        paramMap.put("oprStateId",oprStateId==null?"":oprStateId);
        paramMap.put("oprState",oprState==null?"":oprState);
        paramMap.put("routeInfo",routeInfo==null?"":routeInfo);
        rsd.saveResCircuit(paramMap);
    }

}
