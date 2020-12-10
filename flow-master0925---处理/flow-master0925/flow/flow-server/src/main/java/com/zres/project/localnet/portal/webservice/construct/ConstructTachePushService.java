package com.zres.project.localnet.portal.webservice.construct;

import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.localStanbdyInfo.data.dao.OrderStandbyDao;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import org.apache.commons.collections.MapUtils;
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
 * @ClassName ConstructTachePushService
 * @Description TODO zmp [2308350]
 * @Author wang.g2
 * @Date 2020/12/2 15:18
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@RestController
@RequestMapping("/construct")
public class ConstructTachePushService implements ConstructTachePushIntf{
    private static Logger logger = LoggerFactory.getLogger(ConstructTachePushService.class);

    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private OrderStandbyDao orderStandbyDao;

    @IgnoreSession
    @PostMapping(value="/interfaceBDW/pushTache.spr", produces = "application/json;charset=UTF-8")

    @Override
    public Map<String, Object> pushTache(@RequestBody String request) {
        Map<String, Object> retMap = new HashMap<>();
        List<Map<String, Object>> pushOrderList = new ArrayList<>();

        String tradeId = null;
        try {
            // 解析报文
            Map<String, Object> requestInfo = JSONObject.parseObject(request,Map.class);
            Map<String, Object> body = MapUtils.getMap(requestInfo, "UNI_BSS_BODY");
            Map<String, Object> pushTacheReq = MapUtils.getMap(body, "PUSH_TACHE_REQ");
            Map<String, Object> cstOrd = MapUtils.getMap(pushTacheReq, "CST_ORD");

            String subscribeId = MapUtils.getString(cstOrd,"SUBSCRIBE_ID");
            Map<String, Object> srvOrds = MapUtils.getMap(cstOrd,"SRV_ORD_LIST");
            List<Map<String, Object>> srvOrdList = (List<Map<String, Object>>) MapUtils.getObject(srvOrds, "SRV_ORD");
            for(Map<String, Object> srvOrdInfo : srvOrdList) {

                tradeId = MapUtils.getString(srvOrdInfo,"TRADE_ID");
                String serialNumber =  MapUtils.getString(srvOrdInfo,"SERIAL_NUMBER");
                String flowId =  MapUtils.getString(srvOrdInfo,"FLOW_ID");
                List<Map<String, Object>> srvOrderList = wsd.querySrvOrderList(tradeId, serialNumber);
                String srvOrdId = MapUtils.getString(srvOrderList.get(0), "SRV_ORD_ID");

                Map<String, Object> tacheList = MapUtils.getMap(srvOrdInfo,"TACHE_LIST");
                List<Map<String, Object>> tacheInfoList = (List<Map<String, Object>>) MapUtils.getObject(tacheList, "TACHE_INFO");
                for(Map<String, Object> tacheInfo : tacheInfoList) {
                    Map<String, Object> pushOrderInfo = new HashMap<>();
                    pushOrderInfo.put("serialNumber", serialNumber);
                    pushOrderInfo.put("tradeId", tradeId);
                    pushOrderInfo.put("srvOrdId", srvOrdId);
                    String tacheName =  MapUtils.getString(tacheInfo,"TACHE_NAME");
                    pushOrderInfo.put("tacheName", tacheName);
                    pushOrderInfo.put("receiveTime", MapUtils.getString(tacheInfo,"RECEIVE_TIME"));
                    pushOrderInfo.put("replyTime", MapUtils.getString(tacheInfo,"REPLY_TIME"));
                    pushOrderInfo.put("limitTime", MapUtils.getString(tacheInfo,"LIMIT_TIME"));
                    pushOrderInfo.put("procStaff", MapUtils.getString(tacheInfo,"PROC_STAFF"));
                    pushOrderInfo.put("procStaffTel", MapUtils.getString(tacheInfo,"PROC_STAFF_TEL"));
                    pushOrderInfo.put("procStaffEmail", MapUtils.getString(tacheInfo,"PROC_STAFF_EMAIL"));
                    pushOrderInfo.put("procDesc", MapUtils.getString(tacheInfo,"PROC_DESC"));
                    Map<String, Object> attachInfos =  MapUtils.getMap(tacheInfo,"ATTACH_INFO_LIST");
                    List<Map<String, Object>> attachInfoList = (List<Map<String, Object>>) MapUtils.getObject(attachInfos,"ATTACH_INFO");
                    for(Map<String, Object> attachInfo : attachInfoList) {
                        String fileId =  MapUtils.getString(attachInfo,"FILE_ID");
                        String fileName =  MapUtils.getString(attachInfo,"FILE_NAME");
                        String filePath =  MapUtils.getString(attachInfo,"FILE_PATH");
                        String fileType =  MapUtils.getString(attachInfo,"FILE_TYPE");
                        Map<String, Object> fileInfo = new HashMap<>();
                        fileInfo.put("srvOrdId", srvOrdId);
                        fileInfo.put("filePath", filePath);
                        fileInfo.put("fileName", fileName);
                        fileInfo.put("fileId", fileId);
                        fileInfo.put("woId", tacheName);
                        fileInfo.put("fileType", fileType);
                        fileInfo.put("cstOrdId", "");
                        fileInfo.put("origin", "CONSTRUCT");
                        fileInfo.put("dispOrdId","");// 延期申请单
                        orderStandbyDao.upLoadAttach(fileInfo);
                    }
                    pushOrderInfo.put("SYS_RESOURCE", "construct");
                    pushOrderList.add(pushOrderInfo);
                }
            }
            if (pushOrderList.size() > 0) {
                // TODO 入库新增表  1.保存环节信息 2.保存附件信息 批量操作，避免主键冲突
                wsd.bachAddConstructTacheInfo(pushOrderList);
                retMap = responseJson("0","环节推送接口处理成功");
                logger.info("环节推送接口处理成功");
            } else{
                retMap = responseJson("1","环节推送接口处理失败，请检查数据");
            }

        } catch (Exception e) {
            logger.info(e.getMessage());
            retMap = responseJson("1","环节推送接口处理失败，请检查数据");
        } finally {
            insertInterfaceLog(tradeId,request,JSONObject.toJSONString(retMap));
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
        interflog.put("INTERFNAME","工建系统 To OSS 环节推送接口");
        interflog.put("URL","/construct/interfaceBDW/pushTache.spr");
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
        body.put("PUSH_TACHE_RSP", orderRsp);
        map.put("UNI_BSS_BODY", body);
        return map;
    }
}
