package com.zres.project.localnet.portal.webservice.product.sdwan;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealService;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderSendMsgService;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @ClassName ReceiveService
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/3/13 10:56
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@RestController
@RequestMapping("/receiveSDWANServiceIntf")
public class ReceiveService implements ReceiveSDWANServiceIntf{

    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private OrderDealService orderDealService;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private OrderSendMsgService orderSendMsgService;
    @IgnoreSession
    @PostMapping(value="/interfaceBDW/receiveSDWAN.spr", produces = "application/json;charset=UTF-8")
    @Override
    public Map receiveJson(@RequestBody String request) {

        Map resultMap = new HashMap();
        Map<String, Object> cstOrdInfo = new HashMap<String, Object>();
        Map<String, Object> srvOrdInfo = new HashMap<String, Object>();
        String subscribeId = "";
        try {
            // 解析报文
            JSONObject jsStr = JSONObject.parseObject(request);
            String tradeTypeCode = jsStr.getString("TRADE_TYPE_CODE");
            JSONObject goodsInfo = JSONObject.parseObject(jsStr.getString("GOODS_INFO"));
            String productId = goodsInfo.getString("PRODUCT_ID");
            subscribeId = jsStr.getString("SUBSCRIBE_ID");
            // TODO  目前只对接新开
            String orderType = "101";

            //modify by wang.gang2   动作类型
            Map codeInfo = wsd.queryCodeInfo("TRADE_TYPE_CODE",tradeTypeCode);
            String activeType = MapUtils.getString(codeInfo, "CODE_CONTENT");
            if(StringUtils.isEmpty(activeType)){
                resultMap =responseJson("0","发起流程失败，请检查订单业务受理编码 TRADE_TYPE_CODE");
                return resultMap;
           }
            cstOrdInfo.put("CUST_ID", jsStr.getString("CUST_ID") != null ? jsStr.getString("CUST_ID") : "");
            cstOrdInfo.put("CUST_NAME_CHINESE", jsStr.getString("CUST_NAME_CHINESE") != null ? jsStr.getString("CUST_NAME_CHINESE") : "");
            cstOrdInfo.put("CUST_NAME", jsStr.getString("CUST_NAME_CHINESE") != null ? jsStr.getString("CUST_NAME_CHINESE") : "");
            cstOrdInfo.put("CUST_CONTACT_MAN_NAME", jsStr.getString("CUST_CONTACT_MAN_NAME") != null ? jsStr.getString("CUST_CONTACT_MAN_NAME") : "");
            cstOrdInfo.put("CUST_TEL", jsStr.getString("CUST_TEL") != null ? jsStr.getString("CUST_TEL") : "");
            cstOrdInfo.put("CUST_FAX", jsStr.getString("CUST_FAX") != null ? jsStr.getString("CUST_FAX") : "");
            cstOrdInfo.put("CUST_ADDRESS", jsStr.getString("CUST_ADDRESS") != null ? jsStr.getString("CUST_ADDRESS") : "");
            cstOrdInfo.put("CUST_TYPE", jsStr.getString("CUST_TYPE") != null ? jsStr.getString("CUST_TYPE") : "");

            cstOrdInfo.put("CUST_STATUS", jsStr.getString("CUST_STATUS") != null ? jsStr.getString("CUST_STATUS") : "");
            cstOrdInfo.put("CUST_ZIPCODE", jsStr.getString("CUST_ZIPCODE") != null ? jsStr.getString("CUST_ZIPCODE") : "");
            cstOrdInfo.put("SUBSCRIBE_ID", subscribeId+"");
            cstOrdInfo.put("APPLY_ORD_ID", subscribeId+"");
            cstOrdInfo.put("APPLY_ORD_NAME", cstOrdInfo.get("CUST_NAME")+"");
            // 取值 SRV_ORD 子订单 下的字段
            JSONArray srvOrdList = JSON.parseArray(jsStr.getString("SRV_ORD"));
            Iterator srvOrdInfos = srvOrdList.iterator();

            String province  ="";
            String role_province  ="";
            String city  ="";
            String countyA="";
            while (srvOrdInfos.hasNext()) {
                String orderId = "0";
                Map<String, Object> param = new HashMap<String, Object>();
                JSONObject jso = (JSONObject) srvOrdInfos.next();
                String tradeId = jso.getString("TRADE_ID");
                String serialNumber = jso.getString("SERIAL_NUMBER");
                JSONArray srvAttrInfoList = JSONObject.parseArray(jso.getString("SRV_ATTR_INFO"));
                Iterator srvAttrInfo = srvAttrInfoList.iterator();

                while (srvAttrInfo.hasNext()) {
                    JSONObject srvAttr = (JSONObject) srvAttrInfo.next();
                    String attrCode = srvAttr.getString("ATTR_CODE");
                    String attrValue = srvAttr.getString("ATTR_VALUE");
                    if("siteProvince".equals(attrCode)){
                        province = wsd.selectDeptId(attrValue);
                        role_province = attrValue;
                    }
                    if("siteCity".equals(attrCode)){
                        city = wsd.selectDeptId(attrValue);
                    }
                    if("siteDistrict".equals(attrCode)){
                        countyA = wsd.selectDeptId(attrValue);
                    }
                }
                if(city == null || "".equals(city)){
                    Map retMap = responseJson("1","报文参数无法解析，请检查siteCity");
                    return  retMap;
                }

                int  order_rule_province_sdwan= wsd.selectValeByProvinceForSdwan(role_province); //配置省份
                if (order_rule_province_sdwan>0){
                    cstOrdInfo.put("HANDLE_DEP_ID", countyA);
                    // 根据部门id查询部门信息
                    Map deptInfo = orderDealDao.queryDeptIdByParentDeptId(countyA);
                    // 受理部门
                    cstOrdInfo.put("HANDLE_DEP", MapUtils.getString(deptInfo, "DEPT_NAME"));
                }else {
                    cstOrdInfo.put("HANDLE_DEP_ID", city);
                    // 根据部门id查询部门信息
                    Map deptInfo = orderDealDao.queryDeptIdByParentDeptId(city);
                    // 受理部门
                    cstOrdInfo.put("HANDLE_DEP", MapUtils.getString(deptInfo, "DEPT_NAME"));
                }
                Map map = new HashMap();
                map.put("code_type_name", productId);
                map.put("code_value",orderType);
                map.put("code_content",activeType);
                List<Map<String, Object>> qryOrdPsId = wsd.qryOrdPsId(map);
                if(qryOrdPsId != null && qryOrdPsId.size() >0 && qryOrdPsId.get(0).get("SORT_NO") != null){
                    param.put("ordPsid",qryOrdPsId.get(0).get("SORT_NO"));
                    param.put("ORDER_TITLE", jsStr.getString("SUBSCRIBE_ID"));
                    param.put("ORDER_CONTENT", "");
                    param.put("AREA", "350002000000000042766408");

                    Map<String, Object> acceptAreaMap = new HashMap<String, Object>();
                    acceptAreaMap.put("REGION_ID", city);
                    acceptAreaMap.put("ACT_TYPE", qryOrdPsId.get(0).get("JIKE_ACT_TYPE"));
                    acceptAreaMap.put("PRODUCT_TYPE", productId);
                    param.put("attr", acceptAreaMap);

                    Map retMap = orderDealService.createOrder(param);
                    orderId = (String) retMap.get("orderId");
                }else{
                    Map retMap = responseJson("1","发起流程失败，找不到对应的流程");
                    return  retMap;
                }

                // 入客户信息表并返回主键CST_ORD_ID
                wsd.insertCstOrd(cstOrdInfo);
                int cstOrdId = (int)cstOrdInfo.get("CST_ORD_ID");
                // 入业务定单表
                srvOrdInfo.put("RE_ACTIVE_TYPE",activeType);
                srvOrdInfo.put("ACTIVE_TYPE",activeType);
                srvOrdInfo.put("SERVICE_ID", productId);
                srvOrdInfo.put("INSTANCE_ID",serialNumber);
                srvOrdInfo.put("TRADE_ID",tradeId);
                srvOrdInfo.put("SERIAL_NUMBER",serialNumber);
                srvOrdInfo.put("CST_ORD_ID",cstOrdId);
                srvOrdInfo.put("ORDER_TYPE", orderType);
                srvOrdInfo.put("ORDER_ID",orderId);
                //TODO 云网协同  需要修改
                srvOrdInfo.put("RESOURCES","cloudNetwork");
                srvOrdInfo.put("SYSTEM_RESOURCE","flow-schedule-lt");
                wsd.addGomIdcSrvOrdInfo(srvOrdInfo);//返回主键SRV_ORD_ID
                int srvOrdId = (int)srvOrdInfo.get("SRV_ORD_ID");

                //获取电路属性 入电路信息表
                List<Map> srvOrdAttrList = new ArrayList<Map>();
                for (int i = 0; i < srvAttrInfoList.size(); i++) {
                    Map<String, Object> srvOrdAttr = new HashMap<String, Object>();
                    JSONObject attrInfo = (JSONObject) srvAttrInfoList.get(i);
                    String attrCode = attrInfo.getString("ATTR_CODE");
                    String attrValue = attrInfo.getString("ATTR_VALUE");
                    if(!StringUtils.isEmpty(attrValue)){
                        srvOrdAttr.put("SRV_ORD_ID", srvOrdId);
                        srvOrdAttr.put("SOURSE","sd_wan");
                        srvOrdAttr.put("ATTR_ACTION","0");
                        srvOrdAttr.put("ATTR_VALUE_NAME","");
                        srvOrdAttr.put("OLD_ATTR_VALUE", "");
                        srvOrdAttr.put("ATTR_CODE", attrCode);
                        srvOrdAttr.put("ATTR_VALUE", attrValue);
                        srvOrdAttrList.add(srvOrdAttr);
                    }
                }
                //  批量操作，避免主键冲突
                wsd.bachAddGomIdcSrvOrdAttrInfo(srvOrdAttrList);

                //"开始准备发送短信。。。。。。。。。。。。。。。。。。。。");
                Map<String, Object> sendMsgMap = new HashMap<String, Object>();
                sendMsgMap.put("areaId", province);
                sendMsgMap.put("orderIdList", orderId);
                sendMsgMap.put("operAction", "SD_WAN 产品");
                sendMsgMap.put("resources", "sd_wan");
                orderSendMsgService.sendMsgBefore(sendMsgMap);
            }
            resultMap =responseJson("0","发起流程成功");
        } catch (Exception e) {
            e.printStackTrace();
            resultMap = responseJson("1","报文格式不正确，请检查数据");
        } finally {
              insertInterfaceLog(subscribeId,request,JSONObject.toJSONString(resultMap));
        }
        return resultMap;
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
    private void insertInterfaceLog(String subscribeId,String request,String respone){
        Map<String,Object> interflog = new HashMap<String, Object>();
        interflog.put("INTERFNAME","SD_WAN 收单接口");
        interflog.put("URL","/receiveSDWANServiceIntf/interfaceBDW/receiveSDWAN.spr");
        interflog.put("CONTENT",request);
        interflog.put("RETURNCONTENT",respone);
        interflog.put("ORDERNO",subscribeId);
        interflog.put("REMARK","发送SDWAN json报文");
        wsd.insertInterfLog(interflog);
    }
}
