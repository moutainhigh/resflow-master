package com.zres.project.localnet.portal.webservice.provinceRes;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.service.CommonMethodDealWoOrderServiceInf;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealService;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderSendMsgService;
import com.zres.project.localnet.portal.resourceInitiate.service.ResSupplementDealServiceIntf;
import com.zres.project.localnet.portal.util.OrderTrackOperType;
import com.zres.project.localnet.portal.webservice.IInterface;
import com.zres.project.localnet.portal.webservice.data.dao.InterfaceBoDao;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.zres.project.localnet.portal.webservice.flow.ExceptionFlowService;
import com.zres.project.localnet.portal.webservice.oneDry.HandleAttachment;
import com.ztesoft.res.frame.flow.task.dao.PubDAO;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName ProvinceSubmitOrderService
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/8/11 17:57
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */

@RestController
@RequestMapping("/ProvinceResSubmitOrderServiceIntf")
public class ProvinceSubmitOrderService implements ProvinceSubmitOrderServiceIntf {


    private static Logger logger = LoggerFactory.getLogger(ProvinceSubmitOrderService.class);
    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private CommonMethodDealWoOrderServiceInf commonMethodDealWoOrderServiceInf;

    @IgnoreSession
    @PostMapping(value = "/interfaceBDW/provinceResSubmitOrder.spr", produces = "application/json;charset=UTF-8")

    @Override
    public Map<String, Object> provinceResSubmitOrder(@RequestBody String  request) {
        Map<String, Object> constructResponse = new HashMap<>();
        Map<String, Object> retMap = new HashMap<>();
        String tradeId = null;
        String subscribeId = null;
        try {
            // 解析报文
            JSONObject jsonObject = JSONObject.parseObject(request);
            JSONObject body = JSONObject.parseObject(jsonObject.getString("UNI_BSS_BODY"));
            JSONObject feedBackOrder = JSONObject.parseObject(body.getString("PROVINCE_RES_SUBMIT_ORDER_REQ"));
            JSONObject cstOrd = JSONObject.parseObject(feedBackOrder.getString("CST_ORD"));
            subscribeId = cstOrd.getString("SUBSCRIBE_ID");
            JSONObject srvOrds = JSON.parseObject(cstOrd.getString("SRV_ORD_LIST"));
            JSONArray srvOrdList = JSON.parseArray(srvOrds.getString("SRV_ORD"));
            List srvOrdAttrList = new ArrayList();
            for(Object srvOrdInfo : srvOrdList) {
                JSONObject srvOrd = (JSONObject)srvOrdInfo;
                String serial_number = srvOrd.getString("SERIAL_NUMBER");
                tradeId = srvOrd.getString("TRADE_ID");
                //TODO 返回信息以及附件下载可能需要修改
//                String assessResult = srvOrd.getString("ASSESS_RESULT");
                String woId = srvOrd.getString("WO_ID");
                String networkMode = srvOrd.getString("NETWORK_MODE");//接入方式
                String addrId = srvOrd.getString("ADDR_ID");//标准地址
                String standAddr = srvOrd.getString("STAND_ADDR");//标准地址名称
                String operateTime = srvOrd.getString("OPERATE_TIME");


                //关联原单
                List<Map<String, Object>> orderInfo = wsd.querySrvOrderList(tradeId, serial_number);
                String srvOrdId = MapUtils.getString(orderInfo.get(0), "SRV_ORD_ID");

                Map<String,Object> attrCir = new HashMap<>();
                attrCir.put("SRV_ORD_ID",srvOrdId);
                attrCir.put("SOURSE","provinceDIA");
                attrCir.put("ATTR_ACTION","0");
                attrCir.put("ATTR_VALUE_NAME","");
                attrCir.put("ATTR_CODE", "NETWORK_MODE");
                attrCir.put("ATTR_VALUE", networkMode);
                attrCir.put("OLD_ATTR_VALUE", "");
                srvOrdAttrList.add(attrCir);

                Map<String,Object> attrCir1 = new HashMap<>();
                attrCir1.put("SRV_ORD_ID",srvOrdId);
                attrCir1.put("SOURSE","provinceDIA");
                attrCir1.put("ATTR_ACTION","0");
                attrCir1.put("ATTR_VALUE_NAME","");
                attrCir1.put("ATTR_CODE", "ADDR_ID");
                attrCir1.put("ATTR_VALUE", addrId);
                attrCir1.put("OLD_ATTR_VALUE", "");
                srvOrdAttrList.add(attrCir1);

                Map<String,Object> attrCir2 = new HashMap<>();
                attrCir2.put("SRV_ORD_ID",srvOrdId);
                attrCir2.put("SOURSE","provinceDIA");
                attrCir2.put("ATTR_ACTION","0");
                attrCir2.put("ATTR_VALUE_NAME","");
                attrCir2.put("ATTR_CODE", "STAND_ADDR");
                attrCir2.put("ATTR_VALUE", standAddr);
                attrCir2.put("OLD_ATTR_VALUE", "");
                srvOrdAttrList.add(attrCir2);

                // 解析附件节点，并下载附件信息
                if(srvOrd.keySet().contains("ATTACH_INFO_LIST")){
                    JSONObject attachInfoList = srvOrd.getJSONObject("ATTACH_INFO_LIST");
                    addFile(attachInfoList,String.valueOf(srvOrdId),"JIKE_4A");
                }
                //  批量操作，避免主键冲突
                wsd.bachAddGomIdcSrvOrdAttrInfo(srvOrdAttrList);
                compWo(srvOrdId, "自动开通资源配置提交", woId);
                retMap = responseJson("0","省份资源配置提交接口处理成功");
                logger.info("省份资源配置提交接口处理成功");
            }
        } catch (Exception e) {
            logger.debug(e.getMessage());
            retMap = responseJson("1","省份资源配置提交接口处理失败");
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
        interflog.put("INTERFNAME","省份资源配置提交接口");
        interflog.put("URL","/ProvinceResSubmitOrderServiceIntf//interfaceBDW/provinceResSubmitOrder.spr");
        interflog.put("CONTENT",request);
        interflog.put("RETURNCONTENT",respone);
        interflog.put("ORDERNO",tradeId);
        interflog.put("REMARK","省份资源配置 TO OSS json报文");
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
        body.put("PROVINCE_RES_SUBMIT_ORDER_RSP", orderRsp);
        map.put("UNI_BSS_BODY", body);
        return map;
    }


    private void compWo(String srvOrdId,String remark,String woId){
        try {
            List<Map<String, Object>> srvOrderInfo = wsd.queryWoInfoBysrvOrdId(srvOrdId,woId);
            //TODO 处理工单流程 调用美丽姐方法
            Map<String, Object> complateMap = new HashMap<>();
            complateMap.put("remark", "省份资源配置提交接口 " + remark);
            complateMap.put("operStaffId", "11");
            complateMap.put("orderId", MapUtils.getString(srvOrderInfo.get(0),"ORDER_ID"));
            complateMap.put("woId", woId);
            complateMap.put("action", "回单");
            complateMap.put("operType", OrderTrackOperType.OPER_TYPE_4);
            complateMap.put("srvOrdId", MapUtils.getString(srvOrderInfo.get(0),"SRV_ORD_ID"));
            complateMap.put("tacheId", MapUtils.getString(srvOrderInfo.get(0),"TACHE_ID"));
            commonMethodDealWoOrderServiceInf.commonComplateWo(complateMap);
        } catch (Exception e) {
            logger.debug(e.getMessage());

        }

    }
}

