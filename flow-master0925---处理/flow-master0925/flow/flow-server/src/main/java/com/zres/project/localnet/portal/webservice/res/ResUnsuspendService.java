package com.zres.project.localnet.portal.webservice.res;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.resourceInitiate.data.dao.ResourceInitiateDao;
import com.zres.project.localnet.portal.util.HttpClientJson;
import com.zres.project.localnet.portal.util.SpringContextHolderUtil;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.res.frame.core.util.CollectionUtils;
import net.sf.json.JSONObject;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tang.huili on 2020/2/19.
 */
@Service
public class ResUnsuspendService implements  ResUnsuspendServiceIntf {
    private static Logger logger = LoggerFactory.getLogger(ResUnsuspendService.class);

    /**
     * 业务实例解挂接口，资源系统收到请求后会解挂补录实例
     *
     * @param params
     * @return Map
     */
    @Override
    public Map resUnsuspend(Map<String, Object> params) {
        logger.info("资源业务实例解挂接口开始！" + params);
        WebServiceDao rsd = SpringContextHolderUtil.getBean("webServiceDao"); // 数据库操作-对象

        String srvOrdId = MapUtils.getString(params,"srvOrdId");
        Map retmap = new HashMap();
        String json = "";
        String url =rsd.queryUrl("ResUnsuspend");
        String zy_response = "";

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置日期格式
        Map resmap = new HashMap();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("interfname", "资源业务实例解挂接口");
        map.put("url", url);
        map.put("returncontent", "");
        map.put("createdate", df.format(new Date()));
        map.put("orderno", srvOrdId); //业务订单id
        try {
            //生成json报文
            //1.查数据库数据  (用来拼报文)
            resmap = rsd.queryResInfoBySupplement(srvOrdId);
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
                map.put("remark", "资源业务实例解挂接口返回成功");
                rsd.saveJson(map);
            } else {
                map.put("returncontent", respons.get("code"));
                map.put("remark", "");
                rsd.saveJson(map);
                retmap.put("returncode", false);
                retmap.put("returndec", "资源业务实例解挂接口返回异常！code:" + respons.get("code"));
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
            map.put("returncontent", MapUtils.getString(retmap,"returndec"));
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
            rmap.put("srv_ord_id", srvOrdId);
            rmap.put("attr_action", "ResUnsuspend");
            rmap.put("attr_code", respCode);
            rmap.put("attr_name", "");
            rmap.put("attr_value", srvOrdId);
            rmap.put("attr_value_name", "资源业务实例解挂接口返回结果");
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

            }
            else if ("0".equals(respCode)) {
                retmap.put("returncode", false);
                retmap.put("returndec", "资源业务实例解挂接口返回异常，异常信息：" + zy_response);
                retmap.put("resflowWorkId", resflowWorkId);
                retmap.put("url", url);
                retmap.put("json", json);
                return retmap;
            }
            logger.info("资源业务实例解挂接口结束---" + respCode + "---" + respDesc);
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
            requestBody.put("regionCode",MapUtils.getString(map,"REGION_ID","") );
            requestBody.put("crmOrderCode", MapUtils.getString(map,"CRMORDERCODE"));
            requestBody.put("accNbr", HttpClientJson.checkNull2(map, "SERIAL_NUMBER"));
            requestBody.put("prodInstId", MapUtils.getString(map,"PRODINSTID"));
            requestBody.put("remark", HttpClientJson.checkNull(map, "REMARK"));
            requestBody.put("resflowWorkId", MapUtils.getString(map,"SRV_ORD_ID")); //SRV_ORD_ID
            request.put("requestBody", requestBody);
            request.put("requestHeader", HttpClientJson.requestHeader(map, "ResUnsuspend"));

            json.put("request", request);
        }
        catch (Exception e) {
            //e.printStackTrace();
        }

        return json.toString();
    }
    /**
     * 根据instance_id查询是否有补录单被挂起，如果有，则解挂，
     * @param params
     * @return
     */
    @Override
    public Map<String,Object> resUnsuspendSupplement(Map<String, Object> params){
        Map<String,Object> resParam = new HashMap<>();
        ResourceInitiateDao resourceInitiateDao = SpringContextHolderUtil.getBean("resourceInitiateDao"); // 数据库操作-对象
        OrderDealDao orderDealDao = SpringContextHolderUtil.getBean("orderDealDao");
        String instanceId = MapUtils.getString(params,"instanceId","");
        // 先跟据instance_id查询是否有被挂起的补录单
        String id = "";
        List<Map<String, Object>> list = resourceInitiateDao.queryResSuppleByInstanceId(instanceId,"10E");
        if (!CollectionUtils.isEmpty(list)){
            id = list.get(0).get("ID").toString();
        }
        if(!"".equals(id)){
            // 更新补录单状态
            resourceInitiateDao.updateResSupOrderStateById(id,"10N");
            // 如果成功调用过资源挂起接口，
            int suspendNum = orderDealDao.qryInterResult(id, "ResSuspend");
            int unsuspendNum = orderDealDao.qryInterResult(id, "ResUnsuspend");
            if(suspendNum > 0 && unsuspendNum < 1){
                resParam.put("srvOrdId",id);
                // 调用挂起接口
                resUnsuspend(resParam);
            }
        }
        return null;
    }
}
