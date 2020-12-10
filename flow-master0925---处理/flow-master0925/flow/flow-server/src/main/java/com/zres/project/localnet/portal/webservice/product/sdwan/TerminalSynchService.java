package com.zres.project.localnet.portal.webservice.product.sdwan;

import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealService;
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
 * @ClassName TerminalSynchService
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/3/19 14:50
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@Service
public class TerminalSynchService implements TerminalSynchServicesIntf{
    private static Logger logger = LoggerFactory.getLogger(TerminalSynchService.class);
    @Autowired
    private XmlUtil xmlUtil;
    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private OrderDealService orderDealService;
    @Override
    public Map terminalSynchronization(@RequestParam Map<String, Object> params) {
        //拼接请求报文
        String jsonReqStr = "";
        logger.info("终端同步发送报文request：" + jsonReqStr);
        Map<String, Object> sdWanResponse = new HashMap<>();
        Map<String, Object> retMap = new HashMap<>();
        String tradeId = "";
        String response = "";
        JSONObject jsonObj = new JSONObject();
        try {
            String srvOrdId = MapUtils.getString(params, "srvOrdId");
            List<Map<String, Object>> circuitAttrInfo = wsd.queryCircuitAttr(srvOrdId);
            tradeId = MapUtils.getString(circuitAttrInfo.get(0), "TRADE_ID");
            jsonObj.put("TRADE_ID", tradeId);
            jsonObj.put("DEVICE_FACTORY", MapUtils.getString(circuitAttrInfo.get(0),"DEVICEFACTORY"));
            jsonObj.put("DEVICE_MODEL", MapUtils.getString(circuitAttrInfo.get(0),"DEVICEMODEL"));
            jsonObj.put("BSN", MapUtils.getString(circuitAttrInfo.get(0),"BSN"));
            String url = wsd.queryUrl("syncTerminalInfo");
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
                    retMap.put("message","终端信息同步成功！");
                    //先删除之前成功记录
                    wsd.deleteResAttr(srvOrdId,"terminalSync");
                    // 如果接口返回成功，入库attr表，用来判断是否成功调过终端同步接口
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置日期格式
                    Map<String, Object> rmap = new HashMap<String, Object>();
                    rmap.put("srv_ord_id", srvOrdId);
                    rmap.put("attr_value", respCode);
                    rmap.put("attr_action", "0");
                    rmap.put("attr_code", "terminalSync");
                    rmap.put("attr_name", "");
                    rmap.put("attr_value_name", "终端同步接口返回结果");
                    rmap.put("create_date", df.format(new Date()));
                    rmap.put("sourse", "local");
                    wsd.saveRetInfo(rmap);
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
        interflog.put("INTERFNAME","终端同步接口");
        interflog.put("URL","webservice/product/sdwan/OTSFeedBackService");
        interflog.put("CONTENT",request);
        interflog.put("RETURNCONTENT",respone);
        interflog.put("ORDERNO",tradeId);
        interflog.put("REMARK","发送SDWAN json报文");
        wsd.insertInterfLog(interflog);
    }
}
