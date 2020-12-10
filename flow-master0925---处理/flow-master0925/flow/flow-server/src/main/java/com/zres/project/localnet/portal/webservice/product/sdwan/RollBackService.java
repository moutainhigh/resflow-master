package com.zres.project.localnet.portal.webservice.product.sdwan;

import com.alibaba.fastjson.JSONObject;
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
public class RollBackService implements RollBackServiceIntf {
    private static Logger logger = LoggerFactory.getLogger(RollBackService.class);
    @Autowired
    private XmlUtil xmlUtil;
    @Autowired
    private WebServiceDao wsd;

    @Override
    public Map rollBackOTS(@RequestParam Map<String,Object> map) {

        //拼接请求报文
        String jsonReqStr = "";
        logger.info("param:" + map);
        Map<String, Object> sdWanResponse = new HashMap<>();
        Map<String, Object> retMap = new HashMap<>();
        String tradeId = "";
        String response = "";
        JSONObject jsonObj = new JSONObject();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置日期格式
        try {
            String srvOrdId = MapUtils.getString(map, "srvOrdId");
            Map<String, Object> circuitInfo = wsd.queryCustInfo(srvOrdId);
            tradeId = MapUtils.getString(circuitInfo, "TRADE_ID","");
            jsonObj.put("TRADE_TYPE_CODE", MapUtils.getString(map,"type",""));
            jsonObj.put("SUBSCRIBE_ID", MapUtils.getString(circuitInfo, "SUBSCRIBE_ID"));
            JSONObject srvOrd = new JSONObject();
            srvOrd.put("TRADE_ID", tradeId);
            srvOrd.put("SERIAL_NUMBER", MapUtils.getString(circuitInfo, "SERIAL_NUMBER"));
            jsonObj.put("SRV_ORD", srvOrd);
            jsonObj.put("STATUS_DESC", MapUtils.getString(map,"remark",""));

            String url = wsd.queryUrl("sdwanRollBack");
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
        interflog.put("INTERFNAME","OSS退单接口");
        interflog.put("URL","webservice/product/sdwan/OTSFeedBackService");
        interflog.put("CONTENT",request);
        interflog.put("RETURNCONTENT",respone);
        interflog.put("ORDERNO",tradeId);
        interflog.put("REMARK","发送SDWAN json报文");
        wsd.insertInterfLog(interflog);
    }
}
