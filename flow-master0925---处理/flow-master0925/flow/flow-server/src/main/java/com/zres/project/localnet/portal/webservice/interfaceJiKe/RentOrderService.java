package com.zres.project.localnet.portal.webservice.interfaceJiKe;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.service.ListenerOrderServiceIntf;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealService;
import com.zres.project.localnet.portal.webservice.data.dao.InterfaceBoDao;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.zres.project.localnet.portal.webservice.res.BusinessArchiveServiceIntf;
import com.zres.project.localnet.portal.webservice.res.ResCfsAttrUpdateServiceIntf;
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

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by tang.huili on 2019/5/10.
 * 起止租接口
 *
 * update by jiyou.lion 2019/5/12
 */
@RestController
@RequestMapping("/rentOrderServiceIntf")
public class RentOrderService implements RentOrderServiceIntf {
    private static final Logger logger = LoggerFactory.getLogger(ReceiveJsonService.class);

    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private BusinessArchiveServiceIntf businessArchiveServiceIntf;
    @Autowired
    private OrderDealService orderDealService;
    @Autowired
    private ListenerOrderServiceIntf listenerOrderServiceIntf;
    @Autowired
    private InterfaceBoDao interfaceBoDao;
    @Autowired
    private ResCfsAttrUpdateServiceIntf resCfsAttrUpdateServiceIntf;

    @IgnoreSession
    @PostMapping(value="/interfaceBDW/rentOrder.spr", produces = "application/json;charset=UTF-8")
    @Override
    public Map rentOrder(@RequestBody String reqStr) {
        logger.info("-----起止租接口RentOrder-----" + reqStr);

        Map<String,String> retMap=new HashMap<String, String>();

        //插入接口记录
        Map interflog = new HashMap();
        interflog.put("INTERFNAME","起止租接口RentOrder");
        interflog.put("URL","/rentOrderServiceIntf/interfaceBDW/rentOrder.spr");
        interflog.put("CONTENT",reqStr);

        boolean flag = true; // 返回结果状态标识
        SimpleDateFormat dfStr = new SimpleDateFormat("yyyyMMddHHmmss"); //设置日期格式
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置日期格式

        try {
            // 解析报文
            JSONObject jsStr = JSONObject.parseObject(reqStr);
            String uniBssBodyJsonStr = jsStr.getString("UNI_BSS_BODY");
            JSONObject uniBssBodyJsonObj = JSONObject.parseObject(uniBssBodyJsonStr);
            JSONObject rentOrderReqJsonObj = JSONObject.parseObject(uniBssBodyJsonObj.getString("RENT_ORDER_REQ"));
            JSONObject cstOrdJsonObj = JSONObject.parseObject(rentOrderReqJsonObj.getString("CST_ORD"));
            String subscribeId = cstOrdJsonObj.getString("SUBSCRIBE_ID");

//            String SrvOrdInfoId = "";
            JSONObject srvOrdListJsonObj = JSONObject.parseObject(cstOrdJsonObj.getString("SRV_ORD_LIST"));
            JSONArray srvOrdArr = JSONObject.parseArray(srvOrdListJsonObj.getString("SRV_ORD"));
            Iterator it = srvOrdArr.iterator();

            while (it.hasNext()) {
                JSONObject jso = (JSONObject) it.next();

                String tradeTypeCode = jso.getString("TRADE_TYPE_CODE");
                // 起止租时间
                String rentDate = jso.getString("RENT_DATE");
                String serialNumber = jso.getString("SERIAL_NUMBER");
                String tradeId=jso.getString("TRADE_ID");
                String flowId=jso.getString("FLOW_ID");


                String srvOrdId = orderDealService.selectSrvOrdId(subscribeId,serialNumber,tradeId);
                interflog.put("ORDERNO",srvOrdId);
                if(StringUtils.isEmpty(srvOrdId)){
                    flag = false;
                    retMap = wrapRespMap("1","无效的srvOrdId");
                    break;
                }
                Thread.sleep(1000*60); //设置推迟时间
                Map<String,Object> woInfoMap =interfaceBoDao.queryWoInfo(srvOrdId);
                Map<String,Object> param = new HashMap<>();
                param.put("woId", MapUtils.getString(woInfoMap,"WO_ID"));
                param.put("orderId", MapUtils.getString(woInfoMap,"ORDER_ID"));
                listenerOrderServiceIntf.finshStartStopRent(param);
               /* // 查询是否成功调过资源创建接口
                int num = orderDealDao.qryInterResult(srvOrdId,"ResBusinessCreate");
                // 查询是否成功调过资源归档接口
                int buizNum = orderDealDao.qryInterResult(srvOrdId,"BusinessArchive");
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("flag", BasicCode.LOCALBUILD);
                params.put("srvOrdId", srvOrdId);
                if ( num > 0 && buizNum < 1 ){
                    Map map =  businessArchiveServiceIntf.businessArchive(params);
                    logger.info("-----起止租接口返回报文-----" + map.toString());
                    if(!"成功".equals(MapUtils.getString(map,"returncode"))){
                        flag = false;
                        retMap = wrapRespMap("1","起止租异常，原因："+ MapUtils.getString(map,"returndec"));
                        break;
                    }
                }*/
                //报文入库
                Map<String, Object> rmap = new HashMap<String, Object>();
                rmap.put("srv_ord_id", srvOrdId);
                rmap.put("attr_action", "RentOrder");
                rmap.put("attr_code", "1");
                rmap.put("attr_name", "");
                rmap.put("attr_value", "");
                rmap.put("attr_value_name", "集客起止租接口返回结果");
                rmap.put("sourse", "jike");
                wsd.saveRetInfo(rmap);
                // 入库起租时间
                rmap.put("attr_action", "1");
                rmap.put("attr_code", "21100001");
                rmap.put("attr_name", "起租时间");
                rmap.put("attr_value",  df.format(dfStr.parse(rentDate)));
                rmap.put("attr_value_name", "");
                rmap.put("sourse", "jike");
                wsd.saveRetInfo(rmap);
                // 调用资源扩展接口更新起止租时间
                resCfsAttrUpdateServiceIntf.resRentTimeUpdate(srvOrdId,df.format(dfStr.parse(rentDate)));
            }
        } catch (Exception e) {
            flag = false;
            logger.info("起止租接口系统繁忙，" + e.getMessage(), e);
            retMap = wrapRespMap("1" ,"系统繁忙");
        }
        if(flag){
            retMap = wrapRespMap("0","起止租成功");
            interflog.put("REMARK","success");
        }
        interflog.put("RETURNCONTENT",retMap.toString());
        wsd.insertInterfLog(interflog);

        return retMap;
    }

    private Map wrapRespMap(String respCode,String respDesc) {
        Map map = new HashMap();
        map.put("RESP_DESC", respDesc);
        map.put("RESP_CODE", respCode);
        Map rentMap = new HashMap();
        rentMap.put("RENT_ORDER_RSP", map);
        Map bodyMap = new HashMap();
        bodyMap.put("UNI_BSS_BODY", rentMap);
        return bodyMap;
    }

}
