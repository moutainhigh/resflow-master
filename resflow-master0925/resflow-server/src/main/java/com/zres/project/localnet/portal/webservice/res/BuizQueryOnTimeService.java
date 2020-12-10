package com.zres.project.localnet.portal.webservice.res;

import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.util.HttpClientJson;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.zres.project.localnet.portal.webservice.disp.ResReturnService;
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
 * Created by tang.huili on 2019/5/7.
 */
@Service
public class BuizQueryOnTimeService implements BuizQueryOnTimeServiceIntf {
    private static Logger logger = LoggerFactory.getLogger(BuizQueryOnTimeService.class);

    @Autowired
    private WebServiceDao rsd; //数据库操作-对象
    @Autowired
    private ResReturnService resReturnService;
    public Map buizQueryOnTime(Map<String,Object> params){

        logger.info("资源信息实时查询接口开始！" );
        String json = "";
        String id  = MapUtils.getString(params,"srvOrdId");
        String url = rsd.queryUrl("ResBuizQueryOnTime");
        String respJson = "";
        Map retmap = new HashMap();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置日期格式
        Map resmap = new HashMap();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("interfname", "资源信息实时查询接口");
        map.put("url", url);
        map.put("returncontent", "");
        map.put("createdate", df.format(new Date()));
        map.put("orderno", id); //业务订单id
        boolean flag = params.containsKey("flag")&& BasicCode.RES_SUPPLEMENT.equals(MapUtils.getString(params,"flag",""));

        try {
            //生成json报文
            //1.查数据库数据  (用来拼报文)
            if(flag){
                resmap = rsd.queryResInfoBySupplement(id);
            } else{
                resmap = rsd.queryResInfo(id);
                if(MapUtil.isEmpty(resmap)){ //二干下发的单子
                    resmap = rsd.queryResInfoFromSec(id);
                }
            }

            //2.拼报文
            json = createJson(resmap);
            map.put("content", json);
            logger.info("发送报文：" + json);
        }
        catch (Exception e) {
            logger.error("拼接报文异常！异常信息：" + e.getMessage(),e);
            retmap.put("returncode", "失败");
            retmap.put("returndec", "拼接报文异常：" + e.getMessage());
            map.put("updatedate", df.format(new Date()));
            map.put("content", MapUtils.getString(retmap,"returndec"));
            map.put("remark", "拼接报文异常");
            rsd.saveJson(map);
            return retmap;
        }

            map.put("url", url == null ? "":url);
            logger.info("资源接口地址：" + url);
        try {
            Map responseMap = HttpClientJson.sendHttpPost(url, json);
            map.put("updatedate", df.format(new Date()));
            //3.调对方接口，发json报文 ，接收返回json报文
            if("200".equals(responseMap.get("code"))){
                respJson = responseMap.get("msg").toString();
                map.put("returncontent", respJson);
                map.put("remark", "资源信息实时查询接口返回成功");
                //4.报文入库，数据入库
                rsd.saveJson(map);
            } else {
                map.put("returncontent", responseMap.get("code"));
                map.put("remark", "");
                //4.报文入库，数据入库
                rsd.saveJson(map);
                retmap.put("returncode", "失败");
                retmap.put("returndec", "资源信息实时查询接口返回异常！code:" + responseMap.get("code"));
                return retmap;
            }
            logger.info("资源返回报文：" + respJson);
        }
        catch (Exception e) {
            logger.error("调资源接口异常！异常信息：" + e.getMessage(),e);
            retmap.put("returncode", "失败");
            retmap.put("returndec", "调资源接口异常：" + e.getMessage());
            map.put("updatedate", df.format(new Date()));
            map.put("returncontent", MapUtils.getString(retmap,"returndec"));
            map.put("remark", "调资源接口异常");
            rsd.saveJson(map);
            return retmap;
        }
        try {
            //5.解析json报文
            String str = resReturnService.resReturn(respJson);
            JSONObject retJson = JSONObject.fromObject(str);
            JSONObject response =retJson.getJSONObject("response");
            JSONObject requestBody = response.getJSONObject("responseBody");
            if(!"1".equals(requestBody.getString("respCode"))){
                retmap.put("returncode", "失败");
                retmap.put("returndec", "接口返回报文格式异常：" + requestBody.getString("respDesc"));
                return retmap;
            }
            if(flag){
                retmap = updateResInfo(respJson,params);
            }
        } catch (Exception e) {
            logger.error("接口返回报文格式异常！异常信息：" + e.getMessage(),e);
            retmap.put("returncode", "失败");
            retmap.put("returndec", "接口返回报文格式异常：" + e.getMessage());
            return retmap;
        }
        retmap.put("returncode", "成功");
        return retmap;
    }
    /**
     *     如果是资源补录，需要更新上次调度的资源信息
     * @param respJson
     */
    private Map<String,Object> updateResInfo(String respJson,Map<String,Object>params) {
        Map<String,Object> retmap = new HashMap<>();
        retmap.put("returncode", "成功");
        //查询上次调度的srvOrdId
        if(params.keySet().contains("originSrvOrdId")){
            JSONObject requests = JSONObject.fromObject(respJson);
            JSONObject requestJson = requests.getJSONObject("request");
            JSONObject requestBody = requestJson.getJSONObject("requestBody");
            JSONObject result = requestBody.getJSONObject("result");
            result.put("resflowWorkId",MapUtils.getString(params,"originSrvOrdId"));
            requestBody.put("result",result);
            requestJson.put("requestBody",requestBody);
            requests.put("request",requestJson);
            respJson = String.valueOf(requests);
            String str = resReturnService.resReturn(respJson);
            JSONObject retJson = JSONObject.fromObject(str);
            JSONObject response =retJson.getJSONObject("response");
            JSONObject requestBody2 = response.getJSONObject("responseBody");
            if(!"1".equals(requestBody2.getString("respCode"))){
                retmap.put("returncode", "失败");
                retmap.put("returndec", "接口返回报文格式异常：" + requestBody2.getString("respDesc"));
              //  return retmap;
            }
        }
        return retmap;
    }
    public String createJson(Map map){
        //拼报文
        JSONObject json = new JSONObject();
        JSONObject request = new JSONObject();
        JSONObject requestBody = new JSONObject();
        requestBody.put("prodInstId",MapUtils.getString(map,"PRODINSTID"));
        requestBody.put("crmOrderCode", MapUtils.getString(map,"CRMORDERCODE")); //业务订单号
        requestBody.put("resflowWorkId", MapUtils.getString(map,"SRV_ORD_ID")); //SRV_ORD_ID
        request.put("requestHeader", HttpClientJson.requestHeader(map, "RES_BuizQueryOnTime"));
        request.put("requestBody", requestBody);
        json.put("request", request);
        System.out.println(json.toString());
        return json.toString();
    }
}
