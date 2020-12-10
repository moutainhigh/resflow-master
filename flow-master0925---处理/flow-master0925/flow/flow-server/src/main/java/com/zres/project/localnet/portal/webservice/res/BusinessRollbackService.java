package com.zres.project.localnet.portal.webservice.res;

import com.zres.project.localnet.portal.logInfo.entry.ResCreateInstanceFlag;
import com.zres.project.localnet.portal.logInfo.entry.ResInterfaceLog;
import com.zres.project.localnet.portal.logInfo.entry.ResRollbackLog;
import com.zres.project.localnet.portal.logInfo.service.ResInterfaceLogger;
import com.zres.project.localnet.portal.logInfo.until.LoggerThreadPool;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 资源接口-客户端
 * 业务实例回滚接口实现
 * Created by Skyla on 2018/12/25.
 */
@Service
public class BusinessRollbackService implements BusinessRollbackServiceIntf {
    private static Logger logger = LoggerFactory.getLogger(BusinessRollbackService.class);

    @Autowired
    private WebServiceDao webServiceDao; //数据库操作-对象
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private BuizQueryOnTimeServiceIntf buizQueryOnTimeServiceIntf;

    @Autowired
    private ResInterfaceLogger resInterfaceLogger;

    @Override
    public Map businessRollback(Map<String,Object> param) { //gom_bdw_srv_ord_info.SRV_ORD_ID
     //   String srvOrdId, List<String> orderIds,String rollbackDesc,tacheId
        logger.info("业务实例回滚接口开始！"+ param);
        String srvOrdId = MapUtils.getString(param,"srvOrdId");
        String json = "";
        String url =  webServiceDao.queryUrl("BusinessRollback");
        String zyResponse = "";
        Map retmap = new HashMap();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置日期格式
        Map resmap = new HashMap();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("interfname", "业务实例回滚接口");
        map.put("url", url);
        map.put("createdate", df.format(new Date()));
        map.put("returncontent", zyResponse);
        map.put("orderno", srvOrdId); //业务订单id
        boolean resSupplement= param.keySet().contains("flag")&&MapUtils.getString(param,"flag","").equals(BasicCode.RES_SUPPLEMENT);
        try {
            //生成json报文
            //1.查数据库数据  (用来拼报文)
            if(resSupplement){
                resmap = webServiceDao.queryResInfoBySupplement(srvOrdId);
            } else{
                resmap = webServiceDao.queryResInfo(srvOrdId);
                if(MapUtil.isEmpty(resmap)){ //二干下发的单子
                    resmap = webServiceDao.queryResInfoFromSec(srvOrdId);
                }
            }

            resmap = webServiceDao.queryResInfo(srvOrdId);
            if(MapUtil.isEmpty(resmap)){ //二干下发的单子
                resmap = webServiceDao.queryResInfoFromSec(srvOrdId);
            }
            logger.info("报文数据resmap：" + resmap);
            json = createJson(resmap,param);
            map.put("content", json);
            logger.info("发送报文：" + json);
        }
        catch (Exception e) {
            logger.error("拼接报文异常！异常信息：" + e.getMessage(),e);
            retmap.put("returncode", "失败");
            retmap.put("returndec", "拼接报文异常！");
            map.put("content", MapUtils.getString(retmap,"returndec"));
            map.put("remark", "拼接报文异常");
            map.put("updatedate", df.format(new Date()));
            //5.报文入库，数据入库
            this.saveEventJson(map);
//            rsd.saveJson(map);
            return retmap;
        }
        map.put("url", url);
        try {
            //3.调对方接口，发json报文 ，接收返回json报文
            Map respons = HttpClientJson.sendHttpPost(url, json);
            zyResponse = respons.get("msg").toString();
            map.put("returncontent", zyResponse);
            map.put("remark", "接收资源返回报文");
            map.put("updatedate", df.format(new Date()));
            logger.info("资源返回报文：" + zyResponse);
            //5.报文入库，数据入库
            this.saveEventJson(map);
//            rsd.saveJson(map);
        }
        catch (Exception e) {
            logger.error("调资源接口异常！异常信息：" + e.getMessage(),e);
            retmap.put("returncode", "失败");
            retmap.put("returndec", "调资源接口异常！");
            map.put("updatedate", df.format(new Date()));
            map.put("remark", "调资源接口异常");
            map.put("returncontent", MapUtils.getString(retmap,"returndec"));
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

            Map<String, Object> rmap = new HashMap<String, Object>();
            /*if(resSupplement) {
                rmap.put("srv_ord_id", "0");
                rmap.put("attr_value", MapUtils.getString(resmap, "SRV_ORD_ID"));
            } else{*/
                rmap.put("srv_ord_id", MapUtils.getString(resmap, "SRV_ORD_ID"));
                rmap.put("attr_value", resflowWorkId);
         //   }
            rmap.put("attr_action", "BusinessRollback");
            rmap.put("attr_code", respCode);
            rmap.put("attr_name", "");

            // 是否子流程回滚
            boolean childRoolBack = param.keySet().contains("orderIds") && param.get("orderIds") != null;
            if(childRoolBack){
                List<String> list  = (List<String>) param.get("orderIds");
                rmap.put("attr_value", list.get(0));
                rmap.put("attr_action", "BusinessRollbackChild");
            }

            rmap.put("attr_value_name", "业务实例回滚接口返回结果");
            rmap.put("create_date", df.format(new Date()));
            rmap.put("sourse", "");

            //6.返回结果
            if ("1".equals(respCode)) {
                retmap.put("returncode", "成功");
                retmap.put("returndec", respDesc);
                retmap.put("resflowWorkId", resflowWorkId);
                retmap.put("url", url);
                retmap.put("json", json);
                this.saveEventRetInfo(rmap);
//            rsd.saveRetInfo(rmap);
                // 如果不是子流程回滚，
                if(!childRoolBack){
                    // 回滚接口成功后，删除上次创建接口成功的记录，方便下次继续调用创建接口
                    this.deleteEventRetAttrInfo(srvOrdId,"delete","ResBusinessCreate",null);

//                    rsd.deleteResAttrCode(srvOrdId,"ResBusinessCreate");
                }
            }
            else if ("0".equals(respCode)) {
                retmap.put("returncode", "失败");
                retmap.put("returndec", respDesc);
                retmap.put("resflowWorkId", resflowWorkId);
                retmap.put("url", url);
                retmap.put("json", json);
            }
            logger.info("业务实例回滚接口结束---" + respCode + "---" + respDesc);
          /*  // 当前单子不是资源补录时
            if(!resSupplement){
                // 解挂资源补录单子
                Map<String,Object> unsuspendParam = new HashMap<>();
                unsuspendParam.put("instanceId",MapUtils.getString(resmap, "PRODINSTID","0"));
                ResUnsuspendThread resUnsuspendThread = new ResUnsuspendThread(unsuspendParam);
            }*/
        }
        catch (Exception e) {
            logger.error("接口报文解析入库异常！异常信息：" + e.getMessage(),e);
            retmap.put("returncode", "失败");
            retmap.put("returndec", "接口报文解析入库异常！异常信息："+ zyResponse);
            return retmap;
        }
        return retmap;
    }

    public String createJson(Map map,Map<String,Object> param) {
        // List<String> orderIds,String rollbackDesc,tacheId

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
                String regionCode = webServiceDao.qryRegionCode(regionCodeName);
                requestBody.put("regionCode",regionCode );
            }
            requestBody.put("crmOrderCode", MapUtils.getString(map,"CRMORDERCODE"));
            requestBody.put("accNbr", MapUtils.getString(map, "SERIAL_NUMBER","0"));

        //    String tacheId = MapUtils.getString(param,"tacheId");
            // 电路调度、核查调度环节 全部会滚，暂时不按专业、子流程做回滚
           /* if(tacheId.equals(BasicCode.CHECK_DISPATCH) || tacheId.equals(BasicCode.CIRCUIT_DISPATCH)){
                requestBody.put("rfsId", new ArrayList<String>()); //rfs实例
                requestBody.put("woId", new ArrayList<String>()); //gom_order.order_id
            }*/
            if(param.keySet().contains("orderIds") && param.get("orderIds") != null){
                //子流程回滚
                requestBody.put("rfsId", new ArrayList<String>()); //rfs实例
                List<String> list  = (List<String>) param.get("orderIds");
                requestBody.put("woId", list); //gom_order.order_id
            } else{
                //按电路回滚
                requestBody.put("rfsId", new ArrayList<String>()); //rfs实例
                requestBody.put("woId", new ArrayList<String>()); //gom_order.order_id
            }

            requestBody.put("prodInstId", MapUtils.getString(map,"PRODINSTID"));
            requestBody.put("rollbackCode", ""); //回滚原因编码
            String rollbackDesc= MapUtils.getString(param,"rollbackDesc")==null?"":MapUtils.getString(param,"rollbackDesc");
            requestBody.put("rollbackDesc", rollbackDesc); //回滚原因描述
            requestBody.put("remark", HttpClientJson.checkNull(map, "REMARK"));
            // 特殊：如果是核查单，那么动作类型类型改为核查
            if("102".equals(MapUtils.getString(map,"ORDER_TYPE"))){
                map.put("ACTIVE_TYPE","107");
            }
            requestBody.put("actionType", MapUtils.getString(map, "ACTIVE_TYPE"));

            request.put("requestHeader", HttpClientJson.requestHeader(map, "BusinessRollback"));
            request.put("requestBody", requestBody);
            json.put("request", request);

        }
        catch (Exception e) {
            //e.printStackTrace();
        }

        return json.toString();
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

    public void deleteEventRetAttrInfo(String srvOrdId,String flag,String interfaceName,String orderId){
        ResRollbackLog resRollbackLog = new ResRollbackLog();
        resRollbackLog.setSrvOrdId(srvOrdId);
        resRollbackLog.setOrderId(orderId);
        resRollbackLog.setFlag(flag);
        resRollbackLog.setInterfaceName(interfaceName);
        LoggerThreadPool.addLoggerToExecute(resInterfaceLogger,resRollbackLog);
    }



    /**
     * 调用资源回滚接口业务逻辑
     * 1.不是子流程，调用资源回滚接口后，回滚成功后，删除路由表信息，删除电路编码
     * 2.子流程，调用资源回滚接口，回滚成功后，再调用实时查询接口，更新路由信息
     * params.keySet():
     *  srvOrdId // gom_bdw_srv_ord_info.srv_ord_id
     *  orderIds // 是个List, 存放的是 子流程的gom_order.order_id
     *  rollbackDesc // 回滚原因
     */
    @Override
    public Map resRollBack(Map<String,Object> params){
        Map<String,Object> resultMap = new HashMap<>();
        String srvOrdId = MapUtils.getString(params,"srvOrdId");
        /**
         * 并行核查流程不调用资源接口
         * 首先根据srvOrdId查询流程id，然后进行判断是否并行核查
         */
        Map<String,Object> psInfo = webServiceDao.qryOrdPsIdBySrvOrdId(srvOrdId);
        if(MapUtils.isEmpty(psInfo) && BasicCode.LOCAL_PRARLLEL_CHECK_FLOW.equals(
                MapUtils.getString(psInfo,"PS_ID",""))){
            resultMap.put("returncode", "成功");
            return resultMap;
        }
        boolean resSupplement= params.keySet().contains("flag")&&MapUtils.getString(params,"flag","").equals(BasicCode.RES_SUPPLEMENT);
        Map<String,Object> srvInfo = new HashMap<>();
        if(resSupplement){
            srvInfo = webServiceDao.queryResInfoBySupplement(srvOrdId);
        } else{
             srvInfo = webServiceDao.queryResInfo(srvOrdId);
            if(MapUtil.isEmpty(srvInfo)){ //二干下发的单子
                srvInfo = webServiceDao.queryResInfoFromSec(srvOrdId);
            }
        }
        // 当前查询需要的srvOrdId
        String currentSrvOrdId = MapUtils.getString(srvInfo,"SRV_ORD_ID");
        // 是否子流程回滚
        boolean childRoolBack = params.keySet().contains("orderIds") && params.get("orderIds") != null;
        //是否是新开单
        boolean newOpenOrder = BasicCode.ACTIVE_TYPE_NEWOPEN.equals(MapUtils.getString(srvInfo,"ACTIVE_TYPE",""));
        // 如果成功调用过资源创建接口，这里需要调用回滚接口
        int num = orderDealDao.qryInterResult(currentSrvOrdId, "ResBusinessCreate");
        int numRollBack = 1;
        if(childRoolBack){
            // 查询该子流程是否调过回滚接口，若没回滚先回滚,如果已经回滚就跳过
            List<String> orderIdlist = (List<String>) params.get("orderIds");
            numRollBack = orderDealDao.qryRollBackResult(srvOrdId, "BusinessRollbackChild", orderIdlist.get(0));
        } else {
            // 查询该电路是否成功调过回滚接口，若没回滚先回滚,如果已经回滚就跳过
            numRollBack = orderDealDao.qryInterResult(currentSrvOrdId, "BusinessRollback");
            // 当前单子不是资源补录，并且不是子流程回滚时
            if(!resSupplement){
                // 解挂资源补录单子
                Map<String,Object> unsuspendParam = new HashMap<>();
                unsuspendParam.put("instanceId",MapUtils.getString(srvInfo, "PRODINSTID","0"));
                ResUnsuspendThread resUnsuspendThread = new ResUnsuspendThread(unsuspendParam);
                resUnsuspendThread.start();
            }
        }
        if (num > 0 && numRollBack < 1) {
            Map retmap = businessRollback(params);
            if (!"成功".equals(MapUtils.getString(retmap, "returncode"))) {
                resultMap.put("success",false);
                resultMap.put("respCode", "1");
                resultMap.put("message", "电路ID["+srvOrdId+"]调用资源回滚接口异常，异常原因：" + org.apache.commons.collections.MapUtils.getString(retmap, "returndec"));
                return   resultMap;
            }else{
                // 不是子流程回滚
                if(!childRoolBack){
                    // 删除电路编号，这个功能暂时不做
                    // 删除配置的资源信息
                    webServiceDao.deleteResInfoBySrvOrdId(currentSrvOrdId);
                    webServiceDao.deleteResRouteBySrvOrdId(currentSrvOrdId);
                } else {
                    //回滚成功后，调用资源信息实时查询接口，更新路由等信息
                    Map qryMap = buizQueryOnTimeServiceIntf.buizQueryOnTime(params);
                    if (!"成功".equals(MapUtils.getString(qryMap, "returncode"))) {
                        resultMap.put("success",false);
                        resultMap.put("respCode", "1");
                        resultMap.put("message", "电路ID["+srvOrdId+"]调用资源信息查询接口异常，异常原因：" + MapUtils.getString(qryMap, "returndec"));
                        return resultMap;
                    }
                }
            }
        }
        resultMap.put("respCode","0");
        resultMap.put("success",true);
        return resultMap;
    }
}
