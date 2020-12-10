package com.zres.project.localnet.portal.webservice.provinceRes;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.flowdealinfo.service.CommonMethodDealWoOrderServiceInf;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf;
import com.zres.project.localnet.portal.listener.util.EnmuValueUtil;
import com.zres.project.localnet.portal.util.OrderTrackOperType;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.res.frame.flow.common.service.FlowActionHandler;
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
 * @ClassName ProvinceFinishOrderService
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/8/11 17:50
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@RestController
@RequestMapping("/ProvinceFinishOrderServiceIntf")
public class ProvinceFinishOrderService implements ProvinceFinishOrderServiceIntf {
    private static Logger logger = LoggerFactory.getLogger(ProvinceFinishOrderService.class);
    @Autowired
    private WebServiceDao webServiceDao;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private FlowActionHandler flowActionHandler;
    @Autowired
    private CommonMethodDealWoOrderServiceInf commonMethodDealWoOrderServiceInf;
    @Autowired
    private OrderDealServiceIntf orderDealServiceIntf;

    @IgnoreSession
    @PostMapping(value = "/interfaceBDW/provinceFinishOrder.spr", produces = "application/json;charset=UTF-8")
    @Override
    public Map<String, Object> provinceFinishOrder(@RequestBody String request) {
        Map<String, Object> constructResponse = new HashMap<>();
        Map<String, Object> retMap = new HashMap<>();
        String tradeId = null;
        String subscribeId = null;
        try {
            // 解析报文
            Map requestObject = JSONObject.parseObject(request,Map.class);
            Map body = MapUtils.getMap(requestObject, "UNI_BSS_BODY");
            Map feedBackOrder = MapUtils.getMap(body, "PROVINCE_FINISH_ORDER_REQ");

           /* Map para = MapUtils.getMap(feedBackOrder, "PARA");
            String paraId = MapUtils.getString(para,"PARA_ID");*/
            //按照接口文档修改为 数组,
            JSONArray parseArray = JSON.parseArray(MapUtils.getString(feedBackOrder, "PARA"));
            String paraId = "";
            if (parseArray != null && parseArray.size() > 0 ){
                paraId = parseArray.getJSONObject(0).getString("PARA_ID");
            }

            Map cstOrd = MapUtils.getMap(feedBackOrder, "CST_ORD");
            subscribeId = MapUtils.getString(cstOrd,"SUBSCRIBE_ID");
            Map srvOrds = MapUtils.getMap(cstOrd, "SRV_ORD_LIST");
            List<Map<String, Object>> srvOrdList = (List<Map<String, Object>>) MapUtils.getObject(srvOrds, "SRV_ORD");
            List srvOrdAttrList = new ArrayList();
            for(Map<String, Object> srvOrd : srvOrdList) {
                Map<String, Object> attrInfo = new HashMap<>();
                String serial_number = MapUtils.getString(srvOrd,"SERIAL_NUMBER");
                tradeId = MapUtils.getString(srvOrd,"TRADE_ID");
                //TODO 激活信息及文本路由信息 看接口协议福建有些不一样
                String activeStatus = MapUtils.getString(srvOrd,"ACTIVE_STATUS","");
                String textRouteInfo = MapUtils.getString(srvOrd,"TEXT_ROUTE_INFO","");//文本路由
                String employeeId = MapUtils.getString(srvOrd,"EMPLOYEE_ID","");//装维人员工号
                String catModelType = MapUtils.getString(srvOrd,"CAT_MODEL_TYPE","");//光猫机型
                String terminalSn = MapUtils.getString(srvOrd,"TERMINAL_SN","");//终端串号

                //1.关联原单
                List<Map<String, Object>> orderInfo = webServiceDao.querySrvOrderList(tradeId, serial_number);
                //2.转换属性（10002221装维人员工号/10002222 光猫机型/10002223  终端串号） 入库暂时不需要转换
                attrInfo.put("EMPLOYEE_ID", employeeId);
                attrInfo.put("CAT_MODEL_TYPE", catModelType);
                attrInfo.put("TERMINAL_SN", terminalSn);
                attrInfo.put("ACTIVE_STATUS", activeStatus);
                //福建新增属性
                attrInfo.put("PVLAN", MapUtils.getString(srvOrd,"PVLAN",""));
                attrInfo.put("CVLAN", MapUtils.getString(srvOrd,"CVLAN",""));
                attrInfo.put("OLTCODE", MapUtils.getString(srvOrd,"OLTCODE",""));
                attrInfo.put("OLTIP", MapUtils.getString(srvOrd,"OLTIP",""));
                String srvOrdId = MapUtils.getString(orderInfo.get(0), "SRV_ORD_ID");
                //2.1激活成功结果入到电路属性表
                for (Map.Entry attr: attrInfo.entrySet()) {
                    Map<String,Object> attrCir = new HashMap<>();
                    attrCir.put("SRV_ORD_ID",srvOrdId);
                    attrCir.put("SOURSE","provinceDIA");
                    attrCir.put("ATTR_ACTION","0");
                    attrCir.put("ATTR_VALUE_NAME","");
                    attrCir.put("ATTR_CODE", attr.getKey());
                    attrCir.put("ATTR_VALUE", attr.getValue());
                    attrCir.put("OLD_ATTR_VALUE", "");
                    srvOrdAttrList.add(attrCir);
                }
                if (srvOrdAttrList.size() > 0) {
                    // 2.2 批量操作，避免主键冲突
                    webServiceDao.bachAddGomIdcSrvOrdAttrInfo(srvOrdAttrList);
                }
                //3.文本路由信息入路由信息表
                if(!StringUtils.isEmpty(textRouteInfo)){
                    //3.1
                    Map map = new HashMap();
                   map.put("srvOrdId",srvOrdId);
                   map.put("rfsId",createData(8));
                   map.put("resources","newResource");
                    webServiceDao.saveRes(map);
                    //3.2
                    map.put("attrValue",textRouteInfo);
                    map.put("attrName","文本路由");
                    map.put("attrCode","TEXT_ROUTE");
                    webServiceDao.saveResRoute(map);
                }
            //    retMap = responseJson("0","省内DIA自动开通系统反馈接口(激活)处理成功");
                Map<String,Object> dealRetMap = dealWoOrder(srvOrdId,activeStatus,paraId);
                if(MapUtils.getBoolean(dealRetMap,"success")){
                    // 解析附件，关联工单
                    String woId = MapUtils.getString(dealRetMap,"woId","");
                    if(srvOrd.keySet().contains("ATTACH_INFO_LIST")){
                        Map attachs = MapUtils.getMap(srvOrd, "ATTACH_INFO_LIST");
                        List<Map<String, Object>> attachInfoList = (List<Map<String, Object>>) MapUtils.getObject(attachs, "ATTACH_INFO");
                        if(attachInfoList != null && attachInfoList.size() > 0){
                            for(Map<String, Object> temp :attachInfoList) {
                                String fileId = MapUtils.getString(temp, "FILE_ID");
                                temp.put("FILE_ID", fileId.substring(0, fileId.lastIndexOf('.')));
                                temp.put("FILE_TYPE", fileId.substring(fileId.lastIndexOf('.')+1,fileId.length()));
                                temp.put("SRV_ORD_ID", srvOrdId);
                                temp.put("WO_ORD_ID", woId);
                                temp.put("ORIGIN", "PROVRES");
                                temp.put("srvOrdId", srvOrdId);
                                webServiceDao.addGomIdcSrvOrdAttachInfo(temp);
                            }
                        }
                    }
                    retMap = responseJson("0",MapUtils.getString(dealRetMap,"message"));
                } else {
                    return responseJson("1",MapUtils.getString(dealRetMap,"message"));
                }
                logger.info("省内DIA自动开通系统反馈接口(激活)处理成功");
            }
        } catch (Exception e) {
            logger.debug(e.getMessage());
            retMap = responseJson("1","省内DIA自动开通系统反馈接口(激活)处理失败");
        } finally {
            insertInterfaceLog(subscribeId,request,JSONObject.toJSONString(retMap));
        }
        return retMap;
    }

    /**
     * 福建集约化需求：互联网专线（商务专线）新开移机拆机
     * 1.接入专业资源自动分配，若省内自建系统反馈成功 接入专业子流程，接入专业数据制作环节回单，
     *                      若失败，则退单，自动回退到接入专业资源分配环节
     * 2.本地测试下发业务报竣通知，若省内自建系统反馈成功，若失败，则转为人工处理
     *
     * @param srvOrdId
     * @param activeStatus
     * @param paraId 反馈类型（福建省分）： Finish   激活后反馈； Test 科大施工完成反馈
     * @return
     */
    private Map<String,Object> dealWoOrder(String srvOrdId,String activeStatus,String paraId) throws Exception {
        Map<String,Object> retMap = new HashMap<>();
        //判断是否符合条件派发省内号线---互联网专线（商务专线）--新开、移机、拆机
        Map<String, Object> srvMap = orderDealDao.querySrvInfoBySrvOrdId(srvOrdId);
        String serviceId = MapUtils.getString(srvMap, "SERVICE_ID");
        String activeType = MapUtils.getString(srvMap, "ACTIVE_TYPE");
        String orderId = MapUtils.getString(srvMap, "ORDER_ID");
        String subType = orderDealDao.qrySubType(srvOrdId);
        String woId = "";
        if (BasicCode.DIA_SERVICEID.equals(serviceId) && ("101,102,106".indexOf(activeType)!= -1) && "5".equals(subType)){
            //接入专业资源自动分配 查询当前待处理工单 接入专业数据制作环节，进行自动回单操作
            if("Finish".equals(paraId)){
                Map<String, Object> woInfo = orderDealDao.qryDateMakeWoId(orderId);
                woId = MapUtils.getString(woInfo, "WO_ID","");
                String childOrderId = MapUtils.getString(woInfo, "ORDER_ID");
                String woState =  MapUtils.getString(woInfo, "WO_STATE","");
                if (OrderTrackOperType.WO_ORDER_STATE_18.equals(woState)) {
                    if (!"".equals(woId)){
                        orderDealDao.updateWoStateByWoId(woId, OrderTrackOperType.WO_ORDER_STATE_2);
                        Map<String, Object> commonMap = new HashMap<>();
                        Map<String, Object> operAttrsVal = new HashMap<>();  //线条参数 ,工单流转使用
                        commonMap.put("orderId", childOrderId);
                        commonMap.put("woId", woId);
                        commonMap.put("operStaffId", "11");
                        commonMap.put("tacheId", BasicCode.DATA_MAKE);//500001158
                        if ("是".equals(activeStatus)){  //激活成功以后，数据制作环节工单自动流转下去
                            commonMap.put("remark", "省内号线资源系统激活成功");
                            commonMap.put("action", "回单"); //动作
                            commonMap.put("operType", OrderTrackOperType.OPER_TYPE_4); //操作类型
                            operAttrsVal.put("isNeedResConstruct", "1");
                            operAttrsVal.put("isDataMakeBack", "1");
                            commonMap.put("operAttrsVal", operAttrsVal);
                        }else{  //激活失败，自动回退到接入专业资源分配环节
                            commonMap.put("remark", "省内号线资源系统激活失败");
                            commonMap.put("action", "退单"); //动作
                            commonMap.put("operType", OrderTrackOperType.OPER_TYPE_5); //操作类型
                            operAttrsVal.put("isNeedResConstruct", "1");
                            operAttrsVal.put("isDataMakeBack", "0");
                            commonMap.put("operAttrsVal", operAttrsVal);
                        }
                        commonMethodDealWoOrderServiceInf.commonComplateWo(commonMap);
                        //TODO 激活结果入库
                        String respDesc = "是".equals(activeStatus)? "省内号线系统激活成功":"省内号线系统激活失败";
                        Map<String, Object> actSendMap = new HashMap<>();
                        actSendMap.put("specName", "接入");
                        actSendMap.put("woId", woId);
                        actSendMap.put("feedSystem", "省内号线资源系统"); //省内号线资源系统
                        actSendMap.put("activateCode", activeStatus);
                        actSendMap.put("activateDesc", respDesc);
                        webServiceDao.saveActivateInfo(actSendMap); //写入激活日志记录，
                    }
                }else {
                    retMap.put("success",false);
                    retMap.put("message","省内DIA自动开通系统反馈接口(激活)处理失败；工单状态不是待回单");
                    return retMap;
                }
            } else if("Test".equals(paraId)){
                // 本地测试下发业务报竣通知，若省内自建系统反馈成功，若失败，则转为人工处理
                // 根据srvOrdId查询本地测试环节的工单，
                Map<String,Object> param = new HashMap<>();
                param.put("orderId",orderId);
                param.put("tacheCode", EnmuValueUtil.LOCAL_TEST);
                param.put("woState", OrderTrackOperType.WO_ORDER_STATE_18);
                Map<String,Object> localTestWoInfo = orderDealDao.queryWoInfoByOrderId(param);
                woId = MapUtils.getString(localTestWoInfo,"WO_ID","");
                if(!"".equals(woId)){
                    // 工单状态改为处理中
                    orderDealDao.updateWoStateByWoId(woId, OrderTrackOperType.WO_ORDER_STATE_2);
                    Map<String, Object> commonMap = new HashMap<>();
                    Map<String, Object> operAttrsVal = new HashMap<>();  //线条参数 ,工单流转使用
                    commonMap.put("orderId", orderId);
                    commonMap.put("woId", woId);
                    commonMap.put("operStaffId", "-2000");
                    commonMap.put("tacheId", BasicCode.LOCAL_TEST);
                    //if ("是".equals(activeStatus)){  //反馈成功以后，本地测试环节工单自动流转下去
                    commonMap.put("remark", "省内号线资源系统反馈成功");
                    commonMap.put("action", "回单"); //动作
                    commonMap.put("operType", OrderTrackOperType.OPER_TYPE_4); //操作类型
                    operAttrsVal.put("isLocaltestRollback", "1");
                    //全程调测相关线条参数
                    operAttrsVal.put("ifAllTestRoll", "1");
                    operAttrsVal.put("isAllTestRollback", "1");
                    commonMap.put("operAttrsVal", operAttrsVal);
                    commonMethodDealWoOrderServiceInf.commonComplateWo(commonMap);
                    //}
                } else{
                    retMap.put("success",false);
                    retMap.put("message","省内自建系统反馈接口处理失败；工单状态不是待回单");
                    return retMap;
                }
            }
        }
        retMap.put("woId",woId);
        retMap.put("success",true);
        retMap.put("message","反馈接口处理成功");
        return retMap;
    }

    /**
     * 记录接口日志
     * @param request
     * @param respone
     */
    private void insertInterfaceLog(String tradeId,String request,String respone){
        Map<String,Object> interflog = new HashMap<String, Object>();
        interflog.put("INTERFNAME","省内DIA自动开通系统反馈接口(激活)");
        interflog.put("URL","/ProvinceFinishOrderServiceIntf/interfaceBDW/provinceFinishOrder.spr");
        interflog.put("CONTENT",request);
        interflog.put("RETURNCONTENT",respone);
        interflog.put("ORDERNO",tradeId);
        interflog.put("REMARK","省内DIA自动开通系统反馈接口(激活) json报文");
        webServiceDao.insertInterfLog(interflog);
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
        body.put("PROVINCE_FINISH_ORDER_RSP", orderRsp);
        map.put("UNI_BSS_BODY", body);
        return map;
    }
    // 根据指定长度生成纯数字的随机数
    public String createData(int length) {
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(rand.nextInt(10));
        }
        return sb.toString();
    }

    //记录日志
    public void addLog(Map<String, Object> map){
        String operType =  MapUtils.getString(map,"operType");
        String action = OrderTrackOperType.OPER_TYPE_4.equals(operType) ? "回单":"退单";
        Map<String, Object> paramsMapRentInner = new HashMap<String, Object>();
        paramsMapRentInner.put("orderId",MapUtils.getString(map,"ORDER_ID"));
        paramsMapRentInner.put("woId",MapUtils.getString(map,"WO_ID"));
        paramsMapRentInner.put("action",action);
        paramsMapRentInner.put("operType",operType);
        paramsMapRentInner.put("operStaffName","省内号线资源系统");
        paramsMapRentInner.put("remark",MapUtils.getString(map,"REMARK"));
        orderDealServiceIntf.insertTacheLog(paramsMapRentInner);
    }

}
