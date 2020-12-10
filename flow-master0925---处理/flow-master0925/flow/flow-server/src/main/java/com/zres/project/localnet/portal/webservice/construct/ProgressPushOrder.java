package com.zres.project.localnet.portal.webservice.construct;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName ProgressPushOrder
 * @Description TODO  建设完工推送接口
 * @Author wang.g2
 * @Date 2020/4/23 10:35
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@RestController
@RequestMapping("/ProgressPush")
public class ProgressPushOrder implements ProgressPushOrderIntf{
    private static Logger logger = LoggerFactory.getLogger(ProgressPushOrder.class);

    @Autowired
    private WebServiceDao wsd;
    @IgnoreSession
    @PostMapping(value="/interfaceBDW/progressPushOrder.spr", produces = "application/json;charset=UTF-8")
    @Override
    public Map<String, Object> progressComplPushOrder(@RequestBody String request) {
        Map<String, Object> retMap = new HashMap<>();
        List<Map<String, Object>> pushOrderList = new ArrayList<>();
        String subscribeIdRela = null;
        try {
            // 解析报文
            JSONObject jsonObject = JSONObject.parseObject(request);
            JSONObject body = JSONObject.parseObject(jsonObject.getString("UNI_BSS_BODY"));
            JSONObject progressOrder = JSONObject.parseObject(body.getString("PROGRESS_COMPL_PUSH_ORDER_REQ"));
            JSONObject cstOrd = JSONObject.parseObject(progressOrder.getString("CST_ORD"));
            String subscribeId = cstOrd.getString("SUBSCRIBE_ID");
            subscribeIdRela = cstOrd.getString("SUBSCRIBE_ID_RELA");
            JSONObject srvOrds = JSON.parseObject(cstOrd.getString("SRV_ORD_LIST"));
            JSONArray srvOrdList = JSON.parseArray(srvOrds.getString("SRV_ORD"));
            for(Object srvOrdInfo : srvOrdList) {
                Map<String, Object> pushOrder = new HashMap<>();
                JSONObject srvOrd = (JSONObject)srvOrdInfo;
                String serialNumber = srvOrd.getString("SERIAL_NUMBER");
                String tradeId = srvOrd.getString("TRADE_ID");
                String flowId = srvOrd.getString("FLOW_ID");
                String progressStat = srvOrd.getString("PROGRESS_STAT");
                String desc = srvOrd.getString("DESC");
                pushOrder.put("SERIAL_NUMBER", serialNumber);
                pushOrder.put("SUBSCRIBE_ID", subscribeId);
                pushOrder.put("SUBSCRIBE_ID_RELA", subscribeIdRela);
                pushOrder.put("TRADE_ID", tradeId);
                pushOrder.put("FLOW_ID", flowId);
                pushOrder.put("DESCR", desc);
                pushOrder.put("PROGRESS_STAT", progressStat);
                pushOrder.put("SYS_RESOURCE", "construct");
                pushOrderList.add(pushOrder);
            }
            // TODO 入库新增表 前端需要校验
            wsd.addProgressFinishInfo(pushOrderList);
            retMap = responseJson("0","推送接口处理成功");
            logger.info("推送接口处理成功");

        } catch (Exception e) {
            logger.debug(e.getMessage());
            retMap = responseJson("1","推送接口处理失败");
        } finally {
            insertInterfaceLog(subscribeIdRela,request,JSONObject.toJSONString(retMap));
        }
        return retMap;
    }

    /**
     * 记录接口日志
     * @param request
     * @param respone
     */
    private void insertInterfaceLog(String tradeId,String request,String respone){
        Map<String,Object> interflog = new HashMap<String, Object>();
        interflog.put("INTERFNAME","工建系统 To OSS 建设完工推送接口");
        interflog.put("URL","/ProgressPush/interfaceBDW/progressPushOrder.spr");
        interflog.put("CONTENT",request);
        interflog.put("RETURNCONTENT",respone);
        interflog.put("ORDERNO",tradeId);
        interflog.put("REMARK","工建系统发送 json报文");
        wsd.insertInterfLog(interflog);
    }

    /**
     * 拼装反馈报文
     * @param respCode
     * @param respDesc
     * @return
     */
    private Map responseJson(String respCode,String respDesc){
        Map map = new HashMap();
        Map body = new HashMap();
        Map orderRsp = new HashMap();
        orderRsp.put("RESP_DESC", respDesc);
        orderRsp.put("RESP_CODE", respCode);
        body.put("PROGRESS_COMPL_PUSH_ORDER_RSP", orderRsp);
        map.put("UNI_BSS_BODY", body);
        return map;
    }
}
