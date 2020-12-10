package com.zres.project.localnet.portal.webservice.construct;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.flowdealinfo.service.CommonMethodDealWoOrderServiceInf;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealService;
import com.zres.project.localnet.portal.util.OrderTrackOperType;
import com.zres.project.localnet.portal.util.XmlUtil;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.zres.project.localnet.portal.webservice.oneDry.HandleAttachment;
import com.zres.project.localnet.portal.webservice.product.sdwan.OTSFeedBackService;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @ClassName InvestFeedbackOrder
 * @Description TODO    投资估算反馈接口   工建 TO oss
 * @Author wang.g2
 * @Date 2020/4/23 10:30
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@RestController
@RequestMapping("/InvestFeedback")
public class InvestFeedbackOrder implements InvestFeedbackOrderIntf{

    private static Logger logger = LoggerFactory.getLogger(InvestFeedbackOrder.class);
    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private OrderDealService orderDealService   ;

    @IgnoreSession
    @PostMapping(value="/interfaceBDW/investFeedBackService.spr", produces = "application/json;charset=UTF-8")
    @Override
    public Map<String, Object> investFeedbackOrder(@RequestBody String request) {
        Map<String, Object> constructResponse = new HashMap<>();
        Map<String, Object> retMap = new HashMap<>();
        String tradeId = null;
        String woId = null;
        String subscribeId = null;
        try {
            // 解析报文
            JSONObject jsonObject = JSONObject.parseObject(request);
            JSONObject body = JSONObject.parseObject(jsonObject.getString("UNI_BSS_BODY"));
            JSONObject feedBackOrder = JSONObject.parseObject(body.getString("INVEST_FEEDBACK_ORDER_REQ"));
            JSONObject cstOrd = JSONObject.parseObject(feedBackOrder.getString("CST_ORD"));
            subscribeId = cstOrd.getString("SUBSCRIBE_ID");
            JSONObject srvOrds = JSON.parseObject(cstOrd.getString("SRV_ORD_LIST"));
            JSONArray srvOrdList = JSON.parseArray(srvOrds.getString("SRV_ORD"));
            List srvOrdAttrList = new ArrayList();
            for(Object srvOrdInfo : srvOrdList) {
                JSONObject srvOrd = (JSONObject)srvOrdInfo;
                String serial_number = srvOrd.getString("SERIAL_NUMBER");
                tradeId = srvOrd.getString("TRADE_ID");
                woId = srvOrd.getString("WO_ID");
                //TODO 返回信息以及附件下载可能需要修改
                String assessResult = srvOrd.getString("ASSESS_RESULT");
                String operateTime = srvOrd.getString("OPERATE_TIME");
                String remark = srvOrd.getString("REMARK");

                //关联原单
                List<Map<String, Object>> orderInfo = wsd.querySrvOrderList(tradeId, serial_number);
                String srvOrdId = MapUtils.getString(orderInfo.get(0), "SRV_ORD_ID");
                // 评估接口反馈
                Map<String,Object> attrCir = new HashMap<>();
                attrCir.put("SRV_ORD_ID",srvOrdId);
                attrCir.put("SOURSE","construct");
                attrCir.put("ATTR_ACTION","0");
                attrCir.put("ATTR_VALUE_NAME","");
                attrCir.put("ATTR_CODE", "ASSESS_RESULT");
                attrCir.put("ATTR_VALUE", assessResult);
                attrCir.put("OLD_ATTR_VALUE", "");
                srvOrdAttrList.add(attrCir);
                //评估结果说明
                Map<String,Object> attrCir2 = new HashMap<>();
                attrCir2.put("SRV_ORD_ID",srvOrdId);
                attrCir2.put("SOURSE","construct");
                attrCir2.put("ATTR_ACTION","0");
                attrCir2.put("ATTR_VALUE_NAME","");
                attrCir2.put("ATTR_CODE", "REMARK");
                attrCir2.put("ATTR_VALUE", remark);
                attrCir2.put("OLD_ATTR_VALUE", "");
                srvOrdAttrList.add(attrCir2);

                if(srvOrd.keySet().contains("SRV_ORD_INFO")){
                    JSONObject jsSrvOrdInfo = JSONObject.parseObject(srvOrd.getString("SRV_ORD_INFO"));
                    JSONArray js_SRV_ATTR_INFO = JSON.parseArray(jsSrvOrdInfo.getString("SRV_ATTR_INFO"));
                    // 获取返回的属性
                    for(Object srvAttr : js_SRV_ATTR_INFO){
                        JSONObject attrJson = (JSONObject) srvAttr;
                        String attrCode = attrJson.getString("ATTR_CODE");
                        String attrValue = attrJson.getString("ATTR_VALUE");
                        Map<String,Object> attrTmp = new HashMap<>();
                        attrTmp.put("SRV_ORD_ID",srvOrdId);
                        attrTmp.put("SOURSE","construct");
                        attrTmp.put("ATTR_ACTION","0");
                        attrTmp.put("ATTR_VALUE_NAME","");
                        attrTmp.put("ATTR_CODE", attrCode);
                        attrTmp.put("ATTR_VALUE", attrValue);
                        attrTmp.put("OLD_ATTR_VALUE", "");
                        srvOrdAttrList.add(attrTmp);
                    }
                }

                // 解析附件节点，并下载附件信息
                if(srvOrd.keySet().contains("ATTACH_INFO_LIST")){
                    JSONObject attachInfoList = srvOrd.getJSONObject("ATTACH_INFO_LIST");
                    addFile(attachInfoList,String.valueOf(srvOrdId),"JIKE_4A");
                }
                //  批量操作，避免主键冲突
                wsd.bachAddGomIdcSrvOrdAttrInfo(srvOrdAttrList);
                List<Map<String, Object>> srvOrderInfo = wsd.queryWoInfoBysrvOrdId(srvOrdId,woId);
                //DOTO 处理工单流程 调用美丽姐方法
                Map<String, Object> complateMap = new HashMap<>();
                complateMap.put("remark", "投资估算反馈 " + remark);
                complateMap.put("operStaffId", "11");
                complateMap.put("orderId", MapUtils.getString(srvOrderInfo.get(0),"ORDER_ID"));
                complateMap.put("woId", MapUtils.getString(srvOrderInfo.get(0),"WO_ID"));
                complateMap.put("action", "回单");
                complateMap.put("operType", OrderTrackOperType.OPER_TYPE_4);
                complateMap.put("srvOrdId", MapUtils.getString(srvOrderInfo.get(0),"SRV_ORD_ID"));
                complateMap.put("tacheId", MapUtils.getString(srvOrderInfo.get(0),"TACHE_ID"));

                orderDealService.complateWoWithAttr(complateMap);
                retMap = responseJson("0","投资估算反馈接口处理成功");
                logger.info("投资估算反馈接口处理成功");
            }
        } catch (Exception e) {
            logger.debug(e.getMessage());
            retMap = responseJson("1","投资估算反馈接口处理失败");
        } finally {
            insertInterfaceLog(subscribeId,request,JSONObject.toJSONString(retMap));
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
        interflog.put("INTERFNAME","工建 TO oss，投资估算反馈接口");
        interflog.put("URL","/InvestFeedback/interfaceBDW/investFeedBackService.spr");
        interflog.put("CONTENT",request);
        interflog.put("RETURNCONTENT",respone);
        interflog.put("ORDERNO",tradeId);
        interflog.put("REMARK","工建 TO OSS json报文");
        wsd.insertInterfLog(interflog);
    }

    /**
     * 下载附件，入库附件信息
     */
    private void addFile(JSONObject attachInfoList,String srvOrdId,String origin){
        List<Map<String,Object>> attachmentList = new ArrayList<>();
        JSONArray attachInfo = attachInfoList.getJSONArray("ATTACH_INFO");
        for(int i=0;i< attachInfo.size();i++){
            Map<String,Object> attachMap = new HashMap<>();
            JSONObject temp = attachInfo.getJSONObject(i);
            String fileId = temp.getString("FILE_ID");
            String fileName = temp.getString("FILE_NAME");
            String filePath = temp.getString("FILE_PATH");
            attachMap.put("srv_ord_id",srvOrdId);
            attachMap.put("path",filePath);
            attachMap.put("name",fileId);
            attachMap.put("value",fileName);
            attachMap.put("type",fileId.substring(fileId.lastIndexOf('.')+1,fileId.length()));
            attachmentList.add(attachMap);
        }
        // 创建异步处理附件
        HandleAttachment ha = new HandleAttachment(attachmentList,"JIKE_FTP_INFO","FTP_INFO",origin);
        // 启动异步处理附件
        ha.start();
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
        body.put("INVEST_FEEDBACK_ORDER_RSP", orderRsp);
        map.put("UNI_BSS_BODY", body);
        return map;
    }
}
