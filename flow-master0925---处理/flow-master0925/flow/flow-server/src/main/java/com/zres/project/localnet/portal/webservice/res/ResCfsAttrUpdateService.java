package com.zres.project.localnet.portal.webservice.res;

import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.logInfo.entry.ResCreateInstanceFlag;
import com.zres.project.localnet.portal.logInfo.entry.ResInterfaceLog;
import com.zres.project.localnet.portal.logInfo.service.ResInterfaceLogger;
import com.zres.project.localnet.portal.logInfo.until.LoggerThreadPool;
import com.zres.project.localnet.portal.util.HttpClientJson;
import com.zres.project.localnet.portal.util.SpringContextHolderUtil;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.res.frame.core.util.MapUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by tang.huili on 2020/2/19.
 */
@Service
public class ResCfsAttrUpdateService implements  ResCfsAttrUpdateServiceIntf {
    private static Logger logger = LoggerFactory.getLogger(ResCfsAttrUpdateService.class);

    private ResInterfaceLogger resInterfaceLogger = new ResInterfaceLogger();
    /**
     * 资源配置更新cfs扩展属性更新接口
     *
     * @param params  srvOrdId
     *                flag :工单所属系统标志
     *                finshishTime:true 拓展属性增加全程竣工时间
     *                rentTime:true 拓展属性增加起止租时间
     *
     * @return Map
     */
    @Override
    public Map resCfsAttrUpdate(Map<String, Object> params) {
        logger.info("资源配置更新cfs扩展属性更新接口开始！" + params);
        WebServiceDao rsd = SpringContextHolderUtil.getBean("webServiceDao");
        String srvOrdId = MapUtils.getString(params,"srvOrdId");
        String flag = MapUtils.getString(params, "flag");
        Map retmap = new HashMap();

        String json = "";
        String url =rsd.queryUrl("ResCfsAttrUpdate");
        String zy_response = "";

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置日期格式
        Map resmap = new HashMap();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("interfname", "资源配置更新cfs扩展属性更新接口");
        map.put("url", url);
        map.put("returncontent", "");
        map.put("createdate", df.format(new Date()));
        map.put("orderno", srvOrdId); //业务订单id
        try {
            //生成json报文
            //1.查数据库数据  (用来拼报文)
            if (BasicCode.LOCAL.equals(flag)){
                resmap = rsd.queryResInfo(srvOrdId);
            }else if (BasicCode.SECOND.equals(flag)) {
                resmap = rsd.queryResInfoFromSec(srvOrdId);
            }
            logger.info("报文数据map：" + resmap);
            json = createJson(resmap,params);
            logger.info("发送报文：" + json);
            map.put("content", json);
        }
        catch (Exception e) {
            logger.error("拼接报文异常！异常信息：" + e.getMessage(),e);
            retmap.put("returncode", false);
            retmap.put("returndec", "拼接报文异常:"+ e.getMessage());
            map.put("updatedate", df.format(new Date()));
            map.put("content", MapUtils.getString(retmap,"returndec"));
            map.put("remark", "拼接报文异常");
        //    this.saveEventJson(map);
            rsd.saveJson(map);
            return retmap;
        }
        try {
            //3.调对方接口，发json报文 ，接收返回json报文
            Map respons = HttpClientJson.sendHttpPost(url, json);
            map.put("updatedate", df.format(new Date()));
            if("200".equals(respons.get("code"))){
                zy_response = respons.get("msg").toString();
                map.put("returncontent", zy_response);
                map.put("remark", "返回成功");
             //   this.saveEventJson(map);
                rsd.saveJson(map);
            } else {
                map.put("returncontent", respons.get("code"));
                map.put("remark", "");
            //    this.saveEventJson(map);
                rsd.saveJson(map);
                retmap.put("returncode", false);
                retmap.put("returndec", "资源配置更新cfs扩展属性更新接口返回异常！code:" + respons.get("code"));
                return retmap;
            }
            logger.info("资源返回报文：" + zy_response);
        }
        catch (Exception e) {
            logger.error("调资源接口异常！异常信息：" + e.getMessage(),e);
            retmap.put("returncode", false);
            retmap.put("returndec", "调资源接口异常：" + e.getMessage());
            map.put("updatedate", df.format(new Date()));
            map.put("remark", "调资源接口异常");
            map.put("returncontent", org.apache.commons.collections4.MapUtils.getString(retmap,"returndec"));
          //  this.saveEventJson(map);
            rsd.saveJson(map);
            return retmap;
        }
        try {
            //4.解析json报文
            JSONObject response = JSONObject.fromObject(zy_response);
            String retBssBody = response.getString("response");
            JSONObject js_retBssBody = JSONObject.fromObject(retBssBody);
            String responseBody = js_retBssBody.getString("responseBody");
            JSONObject js_responseBody = JSONObject.fromObject(responseBody);
            String respCode = js_responseBody.getString("respCode");
            String respDesc = js_responseBody.getString("respDesc");
            String resflowWorkId = js_responseBody.getString("resflowWorkId");
            Map<String, Object> rmap = new HashMap<String, Object>();
            rmap.put("srv_ord_id", resmap.get("SRV_ORD_ID"));
            rmap.put("attr_action", "ResCfsAttrUpdate");
            rmap.put("attr_code", respCode);
            rmap.put("attr_name", "");
            rmap.put("attr_value", resflowWorkId);
            rmap.put("attr_value_name", "资源配置更新cfs扩展属性更新接口返回结果");
            rmap.put("create_date", df.format(new Date()));
            rmap.put("sourse", "");
            rsd.saveRetInfo(rmap);
            //6.返回结果
            if ("1".equals(respCode)) {
                retmap.put("returncode", true);
                retmap.put("returndec", respDesc);
                retmap.put("resflowWorkId", resflowWorkId);
                retmap.put("url", url);
                retmap.put("json", json);
                //5.报文入库，数据入库
        //        this.saveEventRetInfo(rmap);
            }
            else if ("0".equals(respCode)) {
                retmap.put("returncode", false);
                retmap.put("returndec", "资源配置更新cfs扩展属性更新接口返回异常，异常信息：" + zy_response);
                retmap.put("resflowWorkId", resflowWorkId);
                retmap.put("url", url);
                retmap.put("json", json);
                return retmap;
            }
            logger.info("资源配置更新cfs扩展属性更新接口结束---" + respCode + "---" + respDesc);
        }
        catch (Exception e) {
            logger.error("接口报文解析入库异常！异常信息：" + e.getMessage(),e);
            retmap.put("returncode", false);
            retmap.put("returndec", "接口报文解析入库异常: " + e.getMessage());
            return retmap;
        }
        return retmap;
    }
    public String createJson(Map map,Map params) {
        WebServiceDao rsd = SpringContextHolderUtil.getBean("webServiceDao");
        String flag = MapUtils.getString(params, "flag");
        JSONObject json = new JSONObject();
        JSONObject request = new JSONObject();
        JSONObject requestBody = new JSONObject();
        JSONObject prodInfo = new JSONObject();
        try {
            // ：白沙县分公司--查找到海南省的org_id
            String regionCodeName = map.get("HANDLE_DEP_ID").toString();
            String regionCode = rsd.qryRegionCode(regionCodeName);
            prodInfo.put("regionCode",regionCode );

            prodInfo.put("custOrderCode", MapUtils.getString(map,"SUBSCRIBE_ID"));
            prodInfo.put("crmOrderCode", MapUtils.getString(map,"CRMORDERCODE"));
            prodInfo.put("serviceOrderCode", "ResCfsAttrUpdate");//服务订单编码
            prodInfo.put("accNbr", HttpClientJson.checkNull2(map, "SERIAL_NUMBER"));
            prodInfo.put("prodInstId", MapUtils.getString(map,"PRODINSTID"));
            prodInfo.put("resflowWorkId", MapUtils.getString(map,"SRV_ORD_ID")); //SRV_ORD_ID
            requestBody.put("prodInfo", prodInfo);
            JSONObject prodAttrInfo = new JSONObject();
            JSONArray rows = new JSONArray();
            Set<String>  actSet = new HashSet<>();
            actSet.add(BasicCode.ACTIVE_TYPE_STOP);
            actSet.add(BasicCode.ACTIVE_TYPE_DISMANTLE);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置日期格式
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // 开通关闭时间
            if(params.keySet().contains("finshishTime")&&MapUtils.getBoolean(params,"finshishTime",false)){
                JSONObject rowDate = new JSONObject();
                rowDate.put("attrAction", "0");
                String attrValue = "";
                if(params.keySet().contains("finshishTimeValue")){
                    attrValue = MapUtils.getString(params,"finshishTimeValue","");
                }
                if("".equals(attrValue)){
                    attrValue = df.format(new Date());
                } else{
                    attrValue = df.format(format.parse(attrValue));
                }
                rowDate.put("attrValue", attrValue.isEmpty()?df.format(new Date()):attrValue);
                if(actSet.contains(MapUtils.getString(map,"ACTIVE_TYPE",""))){
                    // 停机、拆机时，关闭时间
                    rowDate.put("attrCode", "CLOSE_TIME");
                    rowDate.put("attrDesc","关闭时间");
                } else{
                    // 新开、变更、移机、复机时，全程竣工时间
                    rowDate.put("attrCode", "FULL_FINISH_TIME");
                    rowDate.put("attrDesc","全程竣工时间");
                }
                rows.add(rowDate);
            }
            // 如果不是集客来单或者不是一干来单，都需要发送起租时间
            boolean res =BasicCode.LOCAL.equals(flag) && BasicCode.LOCALBUILD.equals(MapUtils.getString(map,"RESOURCES"));
            boolean resource = BasicCode.SECOND.equals(flag) || res;
            boolean rentParam = params.keySet().contains("rentTime")&&MapUtils.getBoolean(params,"rentTime",false);
            // 起止租时间
            if(resource || rentParam){
                JSONObject rowDate = new JSONObject();
                rowDate.put("attrAction", "0");
                String attrValue = "";
                if(params.keySet().contains("rentTimeValue")){
                    attrValue = MapUtils.getString(params,"rentTimeValue","");
                }
                if("".equals(attrValue)){
                    attrValue = rsd.qryRentTime(MapUtils.getString(map,"SRV_ORD_ID"));
                }else{
                    attrValue = df.format(format.parse(attrValue));
                }
                rowDate.put("attrValue", attrValue.isEmpty()?df.format(new Date()):attrValue);
                if(actSet.contains(MapUtils.getString(map,"ACTIVE_TYPE",""))){
                    // 停机、拆机时，止租时间
                    rowDate.put("attrCode", "END_RENT_TIME");
                    rowDate.put("attrDesc","止租时间");
                } else{
                    // 新开、变更、移机、复机时，起租时间
                    rowDate.put("attrCode", "START_RENT_TIME");
                    rowDate.put("attrDesc","起租时间");
                }
                rows.add(rowDate);
            }
            prodAttrInfo.put("row",rows);

            requestBody.put("prodAttrInfo", prodAttrInfo);
            request.put("requestBody", requestBody);
            request.put("requestHeader", HttpClientJson.requestHeader(map, "ResCfsAttrUpdate"));

            json.put("request", request);
        }
        catch (Exception e) {
            //e.printStackTrace();
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
    }

    /**
     * 判断是否需要调用资源配置更新cfs扩展属性更新接口
     * @param resParams
     *
     * 本地网调度调用拓展接口更新全程报竣时间的场景
    跨域电路新开变更流程ordPsid=1000209   本地测试(不进行跨域全程调测)或者跨域全程调测 ifMainOffice=0进入跨域全程调测
    停闭流程 ordPsid=1000210  子流程结束监听事件
    本地客户电路 新开变更ordPsid=1000212 完工确认环节
    停复机流程 /ordPsid=1000213 子流程结束监听事件
    拆机流程  ordPsid=1000214 子流程结束监听事件
    局内电路 新开变更 /ordPsid=1000207 需求完工归档环节
    拆机流程 ordPsid=1000208 需求完工归档环节
     */
    @Override
    public void resAttrUpdate(Map<String, Object> resParams,List<HashMap<String, String>> operAttrsList) {
        Map param = new HashMap();
        /**
         * srvOrdId
         *                flag :工单所属系统标志
         *                finshishTime:true 拓展属性增加全程竣工时间
         *                rentTime:true 拓展属性增加起止租时间
         */
        String psId = MapUtils.getString(resParams,"psId");
        String tacheId = MapUtils.getString(resParams,"tacheId");
        String systemResource = MapUtils.getString(resParams,"systemResource");
        boolean flag = false;
        Set<String> flowSet = new HashSet<>();
        flowSet.add(BasicCode.CROSS_STOP_FLOW);
        flowSet.add(BasicCode.LOCAL_CUST_STOP_FLOW);
        flowSet.add(BasicCode.LOCAL_CUST_DISMANTLE_FLOW);
        // 跨域电路停闭流程、 本地客户电路停复机流程、本地客户电路拆机流程 子流程结束时
        if(BasicCode.CHILDFLOW_END.equals(tacheId)&&flowSet.contains(psId)){
            flag = true;
        }
        //需求完工归档
        if (BasicCode.DEMAND_COMPLETE_FILE.equals(tacheId)) {
            param.put("rentTime",true);
            flag = true;
        }
        // 本地客户电路 新开变更 完工确认
        if (BasicCode.LOCAL_CUST_NEWOPEN_FLOW.equals(psId) && BasicCode.COMPLETE_CONFIRM.equals(tacheId)) {
            flag = true;
        }
        // 跨域电路新开变更流程
        if (BasicCode.CROSS_NEWOPEN_FLOW.equals(psId)) {
            // 跨域全程调测
            if(BasicCode.CROSS_WHOLE_COURDER_TEST.equals(tacheId)){
                flag = true;
            }
            // 本地测试(不进行跨域全程调测)
            if (BasicCode.LOCAL_TEST.equals(tacheId)) {
                if(operAttrsList.size()>0){
                    for(Map<String, String> temp : operAttrsList){
                        if("ifMainOffice".equals(MapUtils.getString(temp,"KEY"))&&"1".equals(MapUtils.getString(temp,"KEY"))){
                            flag = true;
                        }
                    }
                }
            }// 本地测试不进行跨域全程调测时
        }
        if(flag){
            param.put("finshishTime",true);
            param.put("flag",systemResource);
            param.put("srvOrdId",MapUtils.getString(resParams,"srvOrdId"));
            ResCfsAttrUpdateThread thread = new ResCfsAttrUpdateThread(param);
            thread.start();
        }
    }

    @Override
    public void resRentTimeUpdate(String srvOrdId,String rentTime){
        Map<String,Object> param = new HashMap<>();
        param.put("rentTime",true);
        param.put("rentTimeValue",rentTime);
        param.put("flag",BasicCode.LOCAL);
        param.put("srvOrdId",srvOrdId);
        ResCfsAttrUpdateThread thread = new ResCfsAttrUpdateThread(param);
        thread.start();
    }
    /**
     * 更新全程报竣时间、起租时间
     * @param srvOrdId
     * @param rentTime
     * @param finshishTime
     */
    @Override
    public void resTimeUpdate(String srvOrdId,String rentTime,String finshishTime){
        Map<String,Object> param = new HashMap<>();
        if(rentTime!=null&& !"".equals(rentTime)){
            param.put("rentTime",true);
            param.put("rentTimeValue",rentTime);
        }
        if(finshishTime!=null&& !"".equals(finshishTime)){
            param.put("finshishTime",true);
            param.put("finshishTimeValue",finshishTime);
        }
        param.put("srvOrdId",srvOrdId);
        param.put("flag",BasicCode.LOCAL);
        ResCfsAttrUpdateThread thread = new ResCfsAttrUpdateThread(param);
        thread.start();
    }
}
