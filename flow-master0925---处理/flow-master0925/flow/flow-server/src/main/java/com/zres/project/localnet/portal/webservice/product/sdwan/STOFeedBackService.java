package com.zres.project.localnet.portal.webservice.product.sdwan;

import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.flowdealinfo.service.CommonMethodDealWoOrderServiceInf;
import com.zres.project.localnet.portal.util.OrderTrackOperType;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName STOFeedBackService
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/3/17 19:31
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@RestController
@RequestMapping("/STOFeedBackService")
public class STOFeedBackService implements STOFeedBackServiceIntf {
    private static Logger logger = LoggerFactory.getLogger(STOFeedBackService.class);
    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private CommonMethodDealWoOrderServiceInf commonMethodDealWoOrderServiceInf;

    @IgnoreSession
    @PostMapping(value="/interfaceBDW/STOFeedBackService.spr", produces = "application/json;charset=UTF-8")
    @Override
    public Map feedBackSTO(@RequestBody String request) {
        logger.info("工单反馈发送报文request：" + request);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置日期格式
        Map<String, Object> retMap = new HashMap<>();
        String tradeId = "";
        try {
            // 解析报文
            JSONObject jsStr = JSONObject.parseObject(request);
            tradeId = jsStr.getString("TRADE_ID");
            String status = jsStr.getString("STATUS");
            String statusDate = jsStr.getString("STATUS_DATE");
            String statusDesc = jsStr.getString("STATUS_DESC");
            if(StringUtils.isEmpty(tradeId) || StringUtils.isEmpty(status) || StringUtils.isEmpty(statusDate)|| StringUtils.isEmpty(statusDesc)){
                throw new Exception("请检查必填！");
            }
            List<Map<String, Object>> srvOrderInfo = wsd.querySrvOrderByTradeId(tradeId);
            String tacheId = MapUtils.getString(srvOrderInfo.get(0),"TACHE_ID","");
            String srvOrdId = MapUtils.getString(srvOrderInfo.get(0),"SRV_ORD_ID","");
            //此接口只允许完成配置一个状态
            if("3".equals(status)){
                if(BasicCode.BUSINESS_TEST.equals(tacheId)){
                    // 业务测试环节，修改电路状态为10N
                    orderDealDao.updateSrvOrdState(srvOrdId, "10N");
                } else{
                    //DOTO 处理工单流程 调用美丽姐方法
                    Map<String, Object> complateMap = new HashMap<>();
                    complateMap.put("remark", "等待SDWAN反馈环节自动回单 " + statusDesc);
                    complateMap.put("operStaffId", "11");
                    complateMap.put("orderId", MapUtils.getString(srvOrderInfo.get(0),"ORDER_ID"));
                    complateMap.put("woId", MapUtils.getString(srvOrderInfo.get(0),"WO_ID"));
                    complateMap.put("action", "回单");
                    complateMap.put("operType", OrderTrackOperType.OPER_TYPE_4);
                    complateMap.put("srvOrdId", srvOrdId);
                    complateMap.put("tacheId", tacheId);
                    commonMethodDealWoOrderServiceInf.commonComplateWo(complateMap);
                }
                retMap = responseJson("0","通知反馈成功，可以进行下一步流程");
            }else {
                retMap = responseJson("1","通知反馈失败，请工单审核检查");
            }
        } catch (Exception e) {
             logger.info("通知反馈失败"+e.getMessage());
             retMap = responseJson("1","通知反馈失败");
        } finally {
            insertInterfaceLog(tradeId,request,JSONObject.toJSONString(retMap));
        }
        return retMap;
    }

    /**
     * 拼装反馈报文
     * @param respCode
     * @param respDesc
     * @return
     */
    private Map responseJson(String respCode,String respDesc){
        Map map = new HashMap();
        map.put("RESP_DESC", respDesc);
        map.put("RESP_CODE", respCode);
        return map;
    }

    /**
     * 记录接口日志
     * @param request
     * @param respone
     */
    private void insertInterfaceLog(String tradeId,String request,String respone){
        Map<String,Object> interflog = new HashMap<String, Object>();
        interflog.put("INTERFNAME","SDWAN To OSS 工单返回接口");
        interflog.put("URL","/STOFeedBackService/interfaceBDW/STOFeedBackService.spr");
        interflog.put("CONTENT",request);
        interflog.put("RETURNCONTENT",respone);
        interflog.put("ORDERNO",tradeId);
        interflog.put("REMARK","接收SDWAN工单反馈 json报文");
        wsd.insertInterfLog(interflog);
    }


}
