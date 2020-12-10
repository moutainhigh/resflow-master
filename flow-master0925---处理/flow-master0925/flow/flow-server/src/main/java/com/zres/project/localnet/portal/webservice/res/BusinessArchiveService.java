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
 * 业务实例归档接口实现
 * Created by csq on 2018/12/26.
 */
@Service
public class BusinessArchiveService implements BusinessArchiveServiceIntf {
    private static Logger logger = LoggerFactory.getLogger(BusinessArchiveService.class);

    @Autowired
    private WebServiceDao rsd; //数据库操作-对象

    @Autowired
    private ResInterfaceLogger resInterfaceLogger;

    @Autowired
    private ResDisassembleServiceIntf resDisassembleServiceIntf;

    public Map businessArchive(Map<String, Object> params) { //gom_bdw_srv_ord_info.SRV_ORD_ID
        String id = MapUtils.getString(params, "srvOrdId");
        String flag = MapUtils.getString(params, "flag");
        logger.info("业务实例归档接口开始：" + id);
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
        String url = rsd.queryUrl("BusinessArchive");
        String zy_response = "";
        Map resmap = new HashMap();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置日期格式
        String createdate = df.format(new Date());
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("interfname", "业务实例归档接口");
        map.put("url", url);
        map.put("createdate", df.format(new Date()));
        map.put("returncontent", zy_response);
        map.put("orderno", id); //业务订单id
        map.put("remark", "");
        map.put("updatedate", "");
        try {
            //生成json报文
            //1.查数据库数据  (用来拼报文)
            if (BasicCode.LOCALBUILD.equals(flag)){
                resmap = rsd.queryResInfo(id);
            }else if (BasicCode.SECONDARY.equals(flag)) {
                resmap = rsd.queryResInfoFromSec(id);
            } else if(BasicCode.RES_SUPPLEMENT.equals(flag)){
                Map resmapTemp = rsd.queryResInfoBySupplement(id);
                resmap.putAll(resmapTemp);
            }
            /**
             * 如果是拆机，那么先调用资源自动拆机接口
             */
            if("102".equals(MapUtils.getString(resmap,"ACTIVE_TYPE",""))){
                retmap = resDisassembleServiceIntf.resDisassemble(params);
                if(!MapUtils.getBoolean(retmap,"returncode")){
                    retmap.put("returncode","失败");
                    return retmap;
                }
            }

            logger.info("报文数据map：" + resmap);
            json = createJson(resmap);
            logger.info("发送报文：" + json);
            map.put("content", json);
        }
        catch (Exception e) {
            logger.error("拼接报文异常！异常信息：" + e.getMessage(),e);
            retmap.put("returncode", "失败");
            retmap.put("returndec", "拼接报文异常:"+ e.getMessage());
            map.put("updatedate", df.format(new Date()));
            map.put("content", MapUtils.getString(retmap,"returndec"));
            map.put("remark", "拼接报文异常");
            this.saveEventJson(map);
//            rsd.saveJson(map);
            return retmap;
        }
            map.put("url", url);

        try {
            //3.调对方接口，发json报文 ，接收返回json报文
            Map respons = HttpClientJson.sendHttpPost(url, json);
         //   zy_response = respons.get("msg").toString();
            map.put("updatedate", df.format(new Date()));
            if("200".equals(respons.get("code"))){
                zy_response = respons.get("msg").toString();
                map.put("returncontent", zy_response);
                map.put("remark", "业务实例归档接口返回成功");
                this.saveEventJson(map);
//                rsd.saveJson(map);
            } else {
                map.put("returncontent", respons.get("code"));
                map.put("remark", "");
                this.saveEventJson(map);
//                rsd.saveJson(map);
                retmap.put("returncode", "失败");
                retmap.put("returndec", "业务实例归档接口返回异常！code:" + respons.get("code"));
                return retmap;
            }
            logger.info("资源返回报文：" + zy_response);
        }
        catch (Exception e) {
            logger.error("调资源接口异常！异常信息：" + e.getMessage(),e);
            retmap.put("returncode", "失败");
            retmap.put("returndec", "调资源接口异常：" + e.getMessage());
            map.put("updatedate", df.format(new Date()));
            map.put("remark", "调资源接口异常");
            map.put("returncontent", MapUtils.getString(retmap,"returndec"));
            this.saveEventJson(map);
//            rsd.saveJson(map);
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
            /*if(BasicCode.RES_SUPPLEMENT.equals(flag)) {
                rmap.put("srv_ord_id", "0");
                rmap.put("attr_value", MapUtils.getString(resmap, "SRV_ORD_ID"));
            } else{*/
                rmap.put("srv_ord_id", MapUtils.getString(resmap, "SRV_ORD_ID"));
                rmap.put("attr_value", resflowWorkId);
          //  }
            rmap.put("attr_action", "BusinessArchive");
            rmap.put("attr_code", respCode);
            rmap.put("attr_name", "");
            rmap.put("attr_value_name", "业务实例归档接口返回结果");
            rmap.put("create_date", createdate);
            rmap.put("sourse", "");

            //6.返回结果
            if ("1".equals(respCode)) {
                retmap.put("returncode", "成功");
                retmap.put("returndec", respDesc);
                retmap.put("resflowWorkId", resflowWorkId);
                retmap.put("url", url);
                retmap.put("json", json);
                //5.报文入库，数据入库
                this.saveEventRetInfo(rmap);
//            rsd.saveRetInfo(rmap);
                // 如果不是集客来单或者不是一干来单，都需要入库起租时间

                boolean res =BasicCode.LOCALBUILD.equals(flag) && BasicCode.LOCALBUILD.equals(MapUtils.getString(resmap,"RESOURCES"));
                if(BasicCode.SECONDARY.equals(flag) || res){
                    rmap.put("attr_action", "1");
                    rmap.put("attr_code", "21100001");
                    rmap.put("attr_name", "起租时间");
                    rmap.put("attr_value", df.format(new Date()));
                    rmap.put("attr_value_name", "");
                    rmap.put("sourse", "res");
                    this.saveEventRetInfo(rmap);
                }

            }
            else if ("0".equals(respCode)) {
                retmap.put("returncode", "失败");
                retmap.put("returndec", "业务实例归档接口返回异常，异常信息：" + zy_response);
                retmap.put("resflowWorkId", resflowWorkId);
                retmap.put("url", url);
                retmap.put("json", json);
                return retmap;
            }
            logger.info("业务实例归档接口结束---" + respCode + "---" + respDesc);
            // 当前单子不是资源补录时
            if(!BasicCode.RES_SUPPLEMENT.equals(flag)){
                // 解挂资源补录单子
                Map<String,Object> unsuspendParam = new HashMap<>();
                unsuspendParam.put("instanceId",MapUtils.getString(resmap, "PRODINSTID","0"));
                ResUnsuspendThread resUnsuspendThread = new ResUnsuspendThread(unsuspendParam);
                resUnsuspendThread.start();
            }

        }
        catch (Exception e) {
            logger.error("接口报文解析入库异常！异常信息：" + e.getMessage(),e);
            retmap.put("returncode", "失败");
            retmap.put("returndec", "接口报文解析入库异常: " + e.getMessage());
            return retmap;
        }
        return retmap;
    }

    public String createJson(Map map) {

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
            requestBody.put("resflowWorkId", MapUtils.getString(map,"SRV_ORD_ID"));
            requestBody.put("remark", MapUtils.getString(map, "REMARK",""));

            request.put("requestBody", requestBody);
            request.put("requestHeader", HttpClientJson.requestHeader(map, "BusinessArchive"));

            json.put("request", request);
            //System.out.println(json.toString());
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
        //rsd.saveRetInfo(rmap);
    }


}
