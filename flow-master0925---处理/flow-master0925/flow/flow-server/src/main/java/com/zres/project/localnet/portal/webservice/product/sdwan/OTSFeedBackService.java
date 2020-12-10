package com.zres.project.localnet.portal.webservice.product.sdwan;

import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.sdwan.dao.SdwanDealDao;
import com.zres.project.localnet.portal.sdwan.service.SdwanDealServiceIntf;
import com.zres.project.localnet.portal.util.XmlUtil;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName OTSFeedBackService
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/3/17 18:43
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@Service
public class OTSFeedBackService implements OTSFeedBackServiceIntf {
    private static Logger logger = LoggerFactory.getLogger(OTSFeedBackService.class);
    @Autowired
    private XmlUtil xmlUtil;
    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private SdwanDealServiceIntf sdwanDealServiceIntf;


    @Override
    public Map feedBackOTS(@RequestParam Map<String,Object> map) {

        //拼接请求报文
        String jsonReqStr = "";
        logger.info("工单反馈发送报文request：" + jsonReqStr);
        Map<String, Object> sdWanResponse = new HashMap<>();
        Map<String, Object> retMap = new HashMap<>();
        String tradeId = "";
        String response = "";
        JSONObject jsonObj = new JSONObject();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置日期格式
        try {
            String srvOrdId = MapUtils.getString(map, "srvOrdId");
            List<Map<String, Object>> circuitAttrInfo = wsd.queryCircuitAttr(srvOrdId);
            tradeId = MapUtils.getString(circuitAttrInfo.get(0), "TRADE_ID");
          //  tradeId =  MapUtils.getString(map,"TRADE_ID");
            String status = MapUtils.getString(map, "STATUS","1");
            String statusDate = df.format(new Date());
            String statusDesc = MapUtils.getString(map, "remark");

            jsonObj.put("TRADE_ID", tradeId);
            jsonObj.put("STATUS", status);
            jsonObj.put("STATUS_DATE", statusDate);
            jsonObj.put("STATUS_DESC", statusDesc);
            if("5".equals(status)){
                List<Map<String, Object>> wanInfoList = sdwanDealServiceIntf.queryWanInfo(map);
                Map<String, Object> wanInfo = wanInfoList.get(0);
                jsonObj.put("WAN_TYPE", MapUtils.getString(wanInfo, "WANTYPE",""));
                jsonObj.put("IP_ADDR", MapUtils.getString(wanInfo, "IPADDR",""));
                jsonObj.put("GW_ADDR", MapUtils.getString(wanInfo, "GATEWAYADDR",""));
                jsonObj.put("USER_ACC", MapUtils.getString(wanInfo, "USERACCT",""));
                jsonObj.put("USER_PWD", MapUtils.getString(wanInfo, "USERPWD",""));
                /*jsonObj.put("WAN_TYPE", MapUtils.getString(map, "WANType",""));
                jsonObj.put("IP_ADDR", MapUtils.getString(map, "IPAddr",""));
                jsonObj.put("GW_ADDR", MapUtils.getString(map, "gatewayAddr",""));
                jsonObj.put("USER_ACC", MapUtils.getString(map, "userAcct",""));
                jsonObj.put("USER_PWD", MapUtils.getString(map, "userPwd",""));*/
            }
            String url = wsd.queryUrl("sdwanfeedBack");
            sdWanResponse = xmlUtil.sendHttpPostOrderCenter(url, jsonObj.toString());
            String code = MapUtils.getString(sdWanResponse,"code","");
            if("200".equals(code)){
                response = MapUtils.getString(sdWanResponse,"msg");
                JSONObject respJson = JSONObject.parseObject(response);
                //0：成功；1：失败，报文错误；
                String respCode = respJson.getString("RESP_CODE");
                String respDesc = respJson.getString("RESP_DESC");
                if("0".equals(respCode)){
                    retMap.put("success",true);
                } else{
                    retMap.put("success",false);
                    retMap.put("message",respDesc);
                }
            } else{
                retMap.put("success",false);
                retMap.put("message","接口交互失败："+code);
            }
        } catch (Exception e) {
            retMap.put("success", false);
            retMap.put("message", "接口交互失败："+e.getMessage());
        } finally {
            insertInterfaceLog(tradeId,jsonObj.toString(),JSONObject.toJSONString(sdWanResponse));
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
        interflog.put("INTERFNAME","OSS To SDWAN 工单返回接口");
        interflog.put("URL","webservice/product/sdwan/OTSFeedBackService");
        interflog.put("CONTENT",request);
        interflog.put("RETURNCONTENT",respone);
        interflog.put("ORDERNO",tradeId);
        interflog.put("REMARK","发送SDWAN json报文");
        wsd.insertInterfLog(interflog);
    }
}
