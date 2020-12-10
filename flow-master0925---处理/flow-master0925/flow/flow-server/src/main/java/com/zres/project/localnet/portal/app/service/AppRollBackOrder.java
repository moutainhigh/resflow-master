package com.zres.project.localnet.portal.app.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.app.AppRollBackOrderIntf;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf;
import com.zres.project.localnet.portal.localStanbdyInfo.data.dao.OrderStandbyDao;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;

/**
 * @ClassName AppRollBackOrder
 * @Description TODO 工单退单
 * @Author wang.g2
 * @Date 2020/6/3 9:36
 */
@RestController
public class AppRollBackOrder implements AppRollBackOrderIntf {

    private static final Logger logger = LoggerFactory.getLogger(AppRollBackOrder.class);

    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private OrderStandbyDao orderStandbyDao;
    @Autowired
    private OrderDealServiceIntf orderDealServiceIntf;

    /**
     * rollBackOrder工单退单
     * @param request
     * @return
     */
    @IgnoreSession
    @RequestMapping(value="/interfaceBDW/rollBackOrder.spr", produces = "application/json;charset=UTF-8")
    @Override
    public Map<String,Object> rollBackOrder(@RequestBody String request) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startDate = new Date();
        logger.info("=====APP：rollBackOrder===start:"+ df.format(startDate));

        Map<String,Object> returnmap = new HashMap();
        Map<String,Object> logInfo = new HashMap<String, Object>();
        logInfo.put("interfname","rollBackOrder工单退单");
        logInfo.put("url","/interfaceBDW/rollBackOrder.spr");
        logInfo.put("request",request);
        logInfo.put("remark","接收app 工单退单 json报文");
        try {
            Map params = JSONObject.parseObject(request, Map.class);
            String staffId = MapUtils.getString(params, "operStaffId");
            String srv_ord_id="";
            // 要处理的电路信息
            String circuitDataStr = MapUtils.getString(params, "circuitData");
            List<Object> circuitDatalist = JSON.parseArray(circuitDataStr);
            for (Object object : circuitDatalist) {
                Map<String, Object> circuitDataMap = (Map<String, Object>) object;// 取出list里面的值转为map
                String woId = MapUtils.getString(circuitDataMap, "WO_ID");
                String orderId = MapUtils.getString(circuitDataMap, "ORDER_ID");
                String tacheId = MapUtils.getString(circuitDataMap, "TACHE_ID");
                String srvOrdId = MapUtils.getString(circuitDataMap, "SRV_ORD_ID");
                String relateInfoId = MapUtils.getString(circuitDataMap, "RELATE_INFO_ID");
                srv_ord_id=srvOrdId;
                params.put("woId",woId);
                params.put("orderId",orderId);
                params.put("tacheId",tacheId);
                params.put("relateInfoId",relateInfoId);
                params.put("srvOrdIdFullCom",srvOrdId);
                params.put("flag","LOCAL");
                logInfo.put("tradeId",srvOrdId);
                Map<String,Object> btnMap =  orderDealServiceIntf.rollBackWoOrder(params);
                if(circuitDataMap.containsKey("fileInfo")){
                    List<Map<String,Object>> fileInfoList = (List<Map<String,Object>>) MapUtils.getObject(circuitDataMap, "fileInfo");
                    if(CollectionUtils.isNotEmpty(fileInfoList) && fileInfoList.size()>0){
                        for (Map<String,Object> fileInfo : fileInfoList) {
                            // 订单Id
                            fileInfo.put("orderId", orderId);
                            // 工单Id
                            fileInfo.put("woId", woId);
                            // 调单信息Id
                            fileInfo.put("srvOrdId", srvOrdId);
                            // 上传人
                            fileInfo.put("staffId", staffId);
                            //所属动作 hJ 环节上传
                            fileInfo.put("origin", "HJ");
                            fileInfo.put("dispatchOrderId", "");
                            fileInfo.put("cstOrdId", "");
                            orderStandbyDao.upLoadAttach(fileInfo);
                        }
                    }
                }
                if(!MapUtils.getBoolean(btnMap,"success",false)){
                    returnmap.put("code", false);
                    returnmap.put("msg", "工单退单失败:" + MapUtils.getString(btnMap,"message"));
                    logInfo.put("respone",JSONObject.toJSONString(returnmap));
                    return returnmap;
                }
            }
            returnmap.put("code", true);
            returnmap.put("msg", "");
            logInfo.put("respone",JSONObject.toJSONString(returnmap));
            logInfo.put("tradeId",srv_ord_id);
        } catch (Exception e) {
            returnmap.put("code", false);
            returnmap.put("msg", "工单退单失败");
            logInfo.put("respone",e.getMessage());
        } finally {
            insertInterfaceLog(logInfo);
        }
        Date endDate = new Date();
        // 得到微秒级别的差值
        long between=endDate.getTime()-startDate.getTime();
        logger.info("=====APP：rollBackOrder===end:"+ df.format(endDate));
        logger.info("========接口总耗时:"+between+"微秒");
        System.out.println("========接口总耗时:"+between+"微秒");
        return returnmap;
    }

    /**
     * 记录接口日志
     * @param logInfo
     */
    private void insertInterfaceLog(Map<String,Object> logInfo){
        Map<String,Object> interflog = new HashMap<String, Object>();
        interflog.put("INTERFNAME",MapUtils.getString(logInfo,"interfname"));
        interflog.put("URL",MapUtils.getString(logInfo,"url"));
        interflog.put("CONTENT", MapUtils.getString(logInfo,"request"));
        interflog.put("RETURNCONTENT",MapUtils.getString(logInfo,"respone"));
        interflog.put("ORDERNO",MapUtils.getString(logInfo,"tradeId"));
        interflog.put("REMARK", MapUtils.getString(logInfo,"remark"));
        wsd.insertInterfLog(interflog);
    }

}
