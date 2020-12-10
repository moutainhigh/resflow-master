package com.zres.project.localnet.portal.webservice.res;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
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
 * Created by tang.huili on 2020/2/19.
 */
@Service
public class ResDisassembleService implements  ResDisassembleServiceIntf {
    private static Logger logger = LoggerFactory.getLogger(ResDisassembleService.class);

    @Autowired
    private WebServiceDao rsd; //数据库操作-对象
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private ResInterfaceLogger resInterfaceLogger;
    /**
     * 资源自动拆机接口:自动预释放资源
     *
     * @param params
     * @return Map
     */
    @Override
    public Map resDisassemble(Map<String, Object> params) {
        logger.info("资源自动拆机接口开始！" + params);
        String srvOrdId = MapUtils.getString(params,"srvOrdId");
        String flag = MapUtils.getString(params, "flag");
        Map retmap = new HashMap();
        // 查询是否成功调过资源自动拆机接口
        int num = orderDealDao.qryInterResult(srvOrdId, "ResDisassemble");
        if (num > 0) {
            retmap.put("returncode", true);
            return retmap;
        }

        String json = "";
        String url =rsd.queryUrl("ResDisassemble");
        String zy_response = "";

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置日期格式
        Map resmap = new HashMap();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("interfname", "资源自动拆机接口");
        map.put("url", url);
        map.put("returncontent", "");
        map.put("createdate", df.format(new Date()));
        map.put("orderno", srvOrdId); //业务订单id
        try {
            //生成json报文
            //1.查数据库数据  (用来拼报文)
            if (BasicCode.LOCALBUILD.equals(flag)){
                resmap = rsd.queryResInfo(srvOrdId);
            }else if (BasicCode.SECONDARY.equals(flag)) {
                resmap = rsd.queryResInfoFromSec(srvOrdId);
            }
            logger.info("报文数据map：" + resmap);
            json = createJson(resmap);
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
            this.saveEventJson(map);
            return retmap;
        }
        try {
            //3.调对方接口，发json报文 ，接收返回json报文
            Map respons = HttpClientJson.sendHttpPost(url, json);
            map.put("updatedate", df.format(new Date()));
            if("200".equals(respons.get("code"))){
                zy_response = respons.get("msg").toString();
                map.put("returncontent", zy_response);
                map.put("remark", "资源自动拆机接口返回成功");
                this.saveEventJson(map);
            } else {
                map.put("returncontent", respons.get("code"));
                map.put("remark", "");
                this.saveEventJson(map);
                retmap.put("returncode", false);
                retmap.put("returndec", "资源自动拆机接口返回异常！code:" + respons.get("code"));
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
            this.saveEventJson(map);
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
            rmap.put("attr_action", "ResDisassemble");
            rmap.put("attr_code", respCode);
            rmap.put("attr_name", "");
            rmap.put("attr_value", resflowWorkId);
            rmap.put("attr_value_name", "资源自动拆机接口返回结果");
            rmap.put("create_date", df.format(new Date()));
            rmap.put("sourse", "");

            //6.返回结果
            if ("1".equals(respCode)) {
                retmap.put("returncode", true);
                retmap.put("returndec", respDesc);
                retmap.put("resflowWorkId", resflowWorkId);
                retmap.put("url", url);
                retmap.put("json", json);
                //5.报文入库，数据入库
                this.saveEventRetInfo(rmap);
            }
            else if ("0".equals(respCode)) {
                retmap.put("returncode", false);
                retmap.put("returndec", "资源自动拆机接口返回异常，异常信息：" + zy_response);
                retmap.put("resflowWorkId", resflowWorkId);
                retmap.put("url", url);
                retmap.put("json", json);
                return retmap;
            }
            logger.info("资源自动拆机接口结束---" + respCode + "---" + respDesc);
        }
        catch (Exception e) {
            logger.error("接口报文解析入库异常！异常信息：" + e.getMessage(),e);
            retmap.put("returncode", false);
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
            // ：白沙县分公司--查找到海南省的org_id
            String regionCodeName = map.get("HANDLE_DEP_ID").toString();
            String regionCode = rsd.qryRegionCode(regionCodeName);
            requestBody.put("regionCode",regionCode );//map.get("DEAL_AREA_CODE")
            requestBody.put("crmOrderCode", map.get("CRMORDERCODE"));
            requestBody.put("accNbr", HttpClientJson.checkNull2(map, "SERIAL_NUMBER"));
            requestBody.put("prodInstId", map.get("PRODINSTID"));
            requestBody.put("remark", HttpClientJson.checkNull(map, "REMARK"));
            requestBody.put("resflowWorkId", map.get("SRV_ORD_ID")); //SRV_ORD_ID
            request.put("requestBody", requestBody);
            request.put("requestHeader", HttpClientJson.requestHeader(map, "ResDisassemble"));

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


}
