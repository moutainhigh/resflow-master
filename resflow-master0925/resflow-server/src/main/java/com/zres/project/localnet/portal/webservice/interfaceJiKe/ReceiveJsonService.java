package com.zres.project.localnet.portal.webservice.interfaceJiKe;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealService;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderSendMsgService;
import com.zres.project.localnet.portal.resourceInitiate.service.ResSupplementDealServiceIntf;
import com.zres.project.localnet.portal.util.JsonConverterUtil;
import com.zres.project.localnet.portal.util.MapConverterUtil;
import com.zres.project.localnet.portal.util.OrderTrackOperType;
import com.zres.project.localnet.portal.webservice.IInterface;
import com.zres.project.localnet.portal.webservice.data.dao.InterfaceBoDao;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.zres.project.localnet.portal.webservice.dto.*;
import com.zres.project.localnet.portal.webservice.flow.ExceptionChangeService;
import com.zres.project.localnet.portal.webservice.flow.ExceptionFlowService;
import com.zres.project.localnet.portal.webservice.oneDry.HandleAttachment;
import com.zres.project.localnet.portal.webservice.res.BusinessQueryThread;

import com.zres.project.localnet.portal.webservice.res.TransferUserServiceIntf;
import com.zres.project.localnet.portal.webservice.until.InterfaceThreadPool;
import com.ztesoft.res.frame.core.util.ListUtil;
import com.ztesoft.res.frame.flow.task.dao.PubDAO;
import com.ztesoft.res.frame.flow.task.service.PubVal;
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

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by tang.huili on 2018/12/24.
 * <p>
 * update by jiyou.lion 2019/5/12
 */
@RestController
@RequestMapping("/receiveJsonServiceIntf")
public class ReceiveJsonService implements ReceiveJsonServiceIntf {

    private static final Logger logger = LoggerFactory.getLogger(ReceiveJsonService.class);

    private static final String CUSORD = "CERTI_TYPE_CODE,CERTI_CODE,CUST_ID,CUST_TEL,CUST_NAME_CHINESE,CUST_PROVINCE,CUST_CITY,CUST_CONTACT_MAN_NAME,DEAL_AREA_CODE,CONTRACT_ID,CONTRACT_NAME,CUST_ADDRESS,CUST_TYPE,CUST_CONTACT_MAN_TEL,NETWORK_LEVEL,CUST_EMAIL,REMARK,CUST_FAX,INIT_AM_NAME,INIT_AM_TEL,INIT_AM_ID,INIT_AM_EMAIL,CUST_INDUSTRY,DEVELOPER_DEPART_NAME,DEPART_NAME";
    private static final String SRVORDINFO = "SERVICE_ID,TRADE_TYPE_CODE,ACTIVE_TYPE,SERVICE_OFFER_ID,SERIAL_NUMBER,TRADE_ID,USER_ID,FLOW_ID";
    private static final String SRVORD_TO_ATTR="TRADE_ID_SUBLIST,TRADE_ID_SUBNUM";// 列转行
    private static final String SRVORDATTRINFO = "ATTR_ACTION,ATTR_CODE,ATTR_VALUE,ATTR_VALUE_NAME,SOURSE,OLD_ATTR_VALUE";
    private static final String ADDPRODINFO = "B_PRODUCT_ID,START_DATE,END_DATE";
    private static final String ADDPRODATTRINFO = "ATTR_ACTION,ATTR_CODE,ATTR_NAME,ATTR_VALUE,ATTR_VALUE_NAME";

    private static final String[] SERVICE_ID = {"80000014", "80000015", "80000017"}; //数字电路，以太网，互联网
    private static final String[] SERVICE_ID_TRS = {"10000001", "10000002", "10000011"};

    private static final String[] ADDRESS_MACHINE = {"CON0007", "CON0008"};// A端装机地址 Z端装机地址
    private static final String[] ADDRESS_MACHINE_TRS = {"20000082", "20000100"};

    private static final String[] AREA_MACHINE = {"CON0005", "CON0006"};// A端所属区域 Z端所属区域
    private static final String[] AREA_MACHINE_TRS = {"20000234", "20000235"};
    private static final String[] AREA_MACHINE_TRS_NAME = {"20000080", "20000098"};
    // 全程要求完成时间，全程竣工时间,自动止租时间,A端要求完成时间,Z端要求完成时间
    private static final String[] FIN_DATE = {"CON0014", "20000073", "CON0013", "CON3007", "CON0015", "CON0016"};

    // 变更信息code
    public static final String CHANGE_DATA = "changeData";
    // 变更信息描述
    public static final String CHANGE_MESSAGE = "changeMessage";


    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private OrderDealService orderDealService;
    @Autowired
    private InterfaceBoDao interfaceJikeDao;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private ExceptionFlowService exceptionFlowService;
    @Autowired
    private PubDAO pubDAO;
    @Autowired
    private OrderSendMsgService orderSendMsgService;
    @Autowired
    private ResSupplementDealServiceIntf resSupplementDealServiceIntf;
    @Autowired
    private IInterface interfaceIntf;

    @Autowired
    private TransferUserServiceIntf transferUserServiceIntf;

    @IgnoreSession
    @PostMapping(value = "/interfaceBDW/receiveJson.spr", produces = "application/json;charset=UTF-8")
    @Override
    public Map receiveJson(@RequestBody String request) {
        logger.info("-----receiveJson-----" + request);

        Map resultMap = new HashMap();

        Map<String, Object> interflog = new HashMap<String, Object>();
        interflog.put("INTERFNAME", "集客收单接口");
        interflog.put("URL", "/receiveJsonServiceIntf/interfaceBDW/receiveJson.spr");
        interflog.put("CONTENT", request);
        interflog.put("REMARK", "接收集客json报文");
        String srvOrdId = "";

        try {
            // 解析报文
            JSONObject jsStr = JSONObject.parseObject(request);
            String UNI_BSS_BODY = jsStr.getString("UNI_BSS_BODY");
            JSONObject js_UNI_BSS_BODY = JSONObject.parseObject(UNI_BSS_BODY);
            JSONObject js_APPLY_ORDER_REQ = JSONObject.parseObject(js_UNI_BSS_BODY.getString("APPLY_ORDER_REQ"));
            JSONObject js_CST_ORD = JSONObject.parseObject(js_APPLY_ORDER_REQ.getString("CST_ORD"));
            JSONObject js_CST_ORD_INFO = JSONObject.parseObject(js_CST_ORD.getString("CST_ORD_INFO"));
            JSONObject old_CST_ORD_INFO = JSONObject.parseObject(js_CST_ORD_INFO.getString("OLD_CUST"));
            JSONObject js_SRV_ORD_LIST = JSONObject.parseObject(js_CST_ORD_INFO.getString("SRV_ORD_LIST"));
            JSONArray js_SRV_ORD = JSON.parseArray(js_SRV_ORD_LIST.getString("SRV_ORD"));
          //  String subscribeId = js_CST_ORD.getString("SUBSCRIBE_ID");
            JSONObject temp = js_SRV_ORD.getJSONObject(0);
            String tradeId = temp.getString("TRADE_ID");
            String serialNumber = temp.getString("SERIAL_NUMBER");
            interflog.put("ORDERNO", tradeId);
            /**
             * 追单改造
             */
            String activeType = temp.getString("ACTIVE_TYPE");
            String tradeTypeCode = temp.getString("TRADE_TYPE_CODE");
            //过户更名  只改数据不起流程
            if("2029".equals(tradeTypeCode)){
                resultMap = rename(serialNumber,old_CST_ORD_INFO,js_CST_ORD_INFO);
            }
            else if ("4A".equals(activeType)) {
                // 调用异常单流程
                resultMap = exceptionFlowParsejson(request);
            }
            else {
                // 正常收单接口报文解析
                resultMap = parseJson(request);
                if(resultMap.keySet().contains("srvOrdId")){
                    srvOrdId = MapUtils.getString(resultMap,"srvOrdId");
                    resultMap.remove("srvOrdId");
                }
            }
        }
        catch (Exception e) {
            logger.error("订单接收接口异常：" + e.getMessage(), e);
            resultMap = responseJson("1", "报文数据格式不正确，请检查数据");
        }

        try {
            interflog.put("RETURNCONTENT", JSONObject.toJSONString(resultMap));
            interflog.put("srvOrdId", srvOrdId);
            interfaceJikeDao.insertInterfLog(interflog);
        }
        catch (Exception e) {
            logger.error("订单接收接口入库异常：" + e.getMessage(), e);
            resultMap = responseJson("1", "报文格式不正确，请检查数据");
        }
        return resultMap;
    }

    /**
     * 追单场景，调用流程平台接口
     *
     * @param request
     * @return
     */
    private Map<String, Object> exceptionFlowParsejson(String request) {
        Map<String, Object> resultMap = new HashMap<>();
        // 取值 CST_ORD_INFO 下的字段
        // 先统一取出需要的节点，防止后面移除
        JSONObject jsStr = JSONObject.parseObject(request);
        String UNI_BSS_BODY = jsStr.getString("UNI_BSS_BODY");
        JSONObject js_UNI_BSS_BODY = JSONObject.parseObject(UNI_BSS_BODY);
        JSONObject js_APPLY_ORDER_REQ = JSONObject.parseObject(js_UNI_BSS_BODY.getString("APPLY_ORDER_REQ"));
        JSONObject js_CST_ORD = JSONObject.parseObject(js_APPLY_ORDER_REQ.getString("CST_ORD"));
        // 取值 CST_ORD_INFO 下的字段
        JSONObject js_CST_ORD_INFO = JSONObject.parseObject(js_CST_ORD.getString("CST_ORD_INFO"));
        JSONObject js_SRV_ORD_LIST = JSONObject.parseObject(js_CST_ORD_INFO.getString("SRV_ORD_LIST"));
        JSONArray js_SRV_ORD = JSON.parseArray(js_SRV_ORD_LIST.getString("SRV_ORD"));

        // 2. 转换客户信息DTO
        Map customerInfoMapTemp = js_CST_ORD_INFO;
        // 2.1移除电路列表
        customerInfoMapTemp.remove("SRV_ORD_LIST");
        JiKeCustomInfoDTO jiKeCustomInfoDTO = JsonConverterUtil.convertUpperKeyMap2Bean(customerInfoMapTemp, JiKeCustomInfoDTO.class);

        String orderId = "";
        // 3.转换电路信息(包括属性信息)
        List<JiKeProdInfoDTO> jiKeProdInfoDTOList = new ArrayList<>();
        Iterator it = js_SRV_ORD.iterator();
        while (it.hasNext()) {
            JSONObject jsonObjectProdInfo = (JSONObject) it.next();
            String serialNumber = jsonObjectProdInfo.getString("SERIAL_NUMBER");
            String tradeIdReal = jsonObjectProdInfo.getString("TRADE_ID_RELA");
            String serviceOfferId = jsonObjectProdInfo.getString("SERVICE_OFFER_ID");
            String srvOrdId = "";
            List<Map<String, Object>> orderList = orderDealService.queryOrderList2(tradeIdReal, serialNumber,serviceOfferId);
            if (orderList != null && orderList.size() > 0) {
                orderId = orderList.get(0).get("ORDER_ID").toString();
                srvOrdId = orderList.get(0).get("SRV_ORD_ID").toString();
            }
            else {
                resultMap = responseJson("1", "追单[TRADE_ID_RELA : " + tradeIdReal + "]未找到原单信息");
                return resultMap;
            }
            Map orderInfoMap = (HashMap) pubDAO.getOrderStateAndPsId(orderId);
            String orderState = MapUtils.getString(orderInfoMap, "order_state", "");
            if (!PubVal.ORD_STA_STAFLW.equals(orderState)) {
                // 如果不是正常状态200000002，就报错，不允许继续追单
                resultMap = responseJson("1", "追单失败，当前定单状态不支持此操作");
                return resultMap;
            }

            String serviceId = jsonObjectProdInfo.getString("SERVICE_ID");
            //根据SERVICE_ID节点信息查询编码关系表
            Map<String, Object> codeMap = wsd.queryCodeInfoFromRelateTable(serviceId, "product_type");
            if (codeMap == null) {
                resultMap = responseJson("1", "追单失败，找不到产品，请检查SERVICE_ID节点");
                return resultMap;
            }
            else {
                serviceId = MapUtils.getString(codeMap, "SERVICE_ID");
            }
            // 解析附件节点，并下载附件信息
            if(jsonObjectProdInfo.keySet().contains("ATTACH_INFO_LIST")){
                JSONObject attachInfoList = jsonObjectProdInfo.getJSONObject("ATTACH_INFO_LIST");
                addFile(attachInfoList,String.valueOf(srvOrdId),"JIKE_4A");
            }
            // 先取出列表信息再移除对象
            JSONObject js_SRV_ORD_INFO = JSONObject.parseObject(jsonObjectProdInfo.getString("SRV_ORD_INFO"));
            // 移除属性对象
            jsonObjectProdInfo.remove("SRV_ORD_INFO");
            Map<String, Object> prodInfoMapTemp = jsonObjectProdInfo;
            if (prodInfoMapTemp.keySet().contains("FLOW_ID")) {
                prodInfoMapTemp.put("FLOW_ID", jsonObjectProdInfo.getString("FLOW_ID"));
            }
            JiKeProdInfoDTO jiKeProdInfoDTO = JsonConverterUtil.convertUpperKeyMap2Bean(prodInfoMapTemp, JiKeProdInfoDTO.class);
            //
            JSONArray js_SRV_ATTR_INFO = JSON.parseArray(js_SRV_ORD_INFO.getString("SRV_ATTR_INFO"));
            Iterator it1 = js_SRV_ATTR_INFO.iterator();
            List<JiKeProdAttrDTO> jiKeProdAttrDTOList = new ArrayList<>();
            while (it1.hasNext()) {
                JSONObject jsonObjectProdAttr = (JSONObject) it1.next();
                Map prodAttrInfoMapTemp = jsonObjectProdAttr;
                String attrCode = MapUtils.getString(prodAttrInfoMapTemp, "ATTR_CODE");
                String attrValue = MapUtils.getString(prodAttrInfoMapTemp, "ATTR_VALUE");
                List<Map<String, Object>> attrInfoList = interfaceJikeDao.qryAttrInfo(attrCode, attrValue, null, serviceId);
                if (attrInfoList != null && attrInfoList.size() > 0) {
                    for (int i = 0; i < attrInfoList.size(); i++) {
                        Map<String, Object> tmpMap = attrInfoList.get(i);
                        if (i == 0) {
                            prodAttrInfoMapTemp.put("ATTR_CODE", MapUtils.getString(tmpMap, "ATTR_CODE", ""));
                            prodAttrInfoMapTemp.put("ATTR_VALUE", MapUtils.getString(tmpMap, "ATTR_VALUE", ""));
                        }
                        else {
                            JiKeProdAttrDTO tmpDto = new JiKeProdAttrDTO();
                            tmpDto.setAttrAction(MapUtils.getString(prodAttrInfoMapTemp, "ATTR_ACTION", ""));
                            tmpDto.setAttrAction(MapUtils.getString(tmpMap, "ATTR_CODE", ""));
                            tmpDto.setAttrAction(MapUtils.getString(tmpMap, "ATTR_VALUE", ""));
                            tmpDto.setAttrAction(MapUtils.getString(prodAttrInfoMapTemp, "ATTR_NAME", ""));
                            tmpDto.setAttrAction(MapUtils.getString(prodAttrInfoMapTemp, "ATTR_VALUE_NAME", ""));
                            jiKeProdAttrDTOList.add(tmpDto);
                        }
                    }
                }
                JiKeProdAttrDTO jiKeProdAttrDTO = JsonConverterUtil.convertUpperKeyMap2Bean(prodAttrInfoMapTemp, JiKeProdAttrDTO.class);
                jiKeProdAttrDTOList.add(jiKeProdAttrDTO);
            }
            jiKeProdInfoDTO.setJiKeProdAttrDTOList(jiKeProdAttrDTOList);
            jiKeProdInfoDTOList.add(jiKeProdInfoDTO);
        }

        try {
            // 4.调用接口
            // 追单
            Map<String, Object> map = exceptionFlowService.jiKeExceptionFlowChange("4A", jiKeCustomInfoDTO, jiKeProdInfoDTOList);
            if (MapUtils.getBoolean(map, "success")) {
                Map<String, Object> operActTypeMap = orderDealDao.getOperActType(orderId);
                logger.info("开始准备发送短信。。。。。。。。。。。。。。。。。。。。");
                Map<String, Object> sendMsgMap = new HashMap<String, Object>();
                sendMsgMap.put("areaId", MapUtils.getLong(operActTypeMap, "REGION_ID"));
                sendMsgMap.put("orderIdList", MapUtils.getString(map,"chgOrdId"));
                sendMsgMap.put("operAction", "追单_集客发起");
                sendMsgMap.put("orderType", "SEC_EXCEPTION");
                sendMsgMap.put("resources", "jike");
                orderSendMsgService.sendMsgBefore(sendMsgMap);

                resultMap = responseJson("0", "发起流程成功");
            }
            else {
                resultMap = responseJson("1", "调用流程平台异常接口失败");
            }
        }
        catch (Exception e) {
            logger.info("调用流程平台失败，报错信息：" + e.getMessage());
            resultMap = responseJson("1", "调用流程平台失败，报错信息：" + e.getMessage());
        }
        return resultMap;
    }

    /**
     * 解析集客收单接口，启动流程
     *
     * @param request
     * @return
     */
    private Map<String, Object> parseJson(String request) {
        SimpleDateFormat dfStr = new SimpleDateFormat("yyyyMMdd"); //设置日期格式
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd"); //设置日期格式
        SimpleDateFormat dfStr2 = new SimpleDateFormat("yyyyMMddHHmmss"); //设置日期格式
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置日期格式
        Map resultMap = null;
        String srvOrdIdStr = null;

        try {
            // 解析报文
            JSONObject jsStr = JSONObject.parseObject(request);
            String UNI_BSS_BODY = jsStr.getString("UNI_BSS_BODY");
            JSONObject js_UNI_BSS_BODY = JSONObject.parseObject(UNI_BSS_BODY);
            JSONObject js_APPLY_ORDER_REQ = JSONObject.parseObject(js_UNI_BSS_BODY.getString("APPLY_ORDER_REQ"));
            JSONObject jsRouting = JSONObject.parseObject(js_APPLY_ORDER_REQ.getString("ROUTING"));
            String routeType = jsRouting.getString("ROUTE_TYPE");
            String routeValue = jsRouting.getString("ROUTE_VALUE");

            JSONObject js_CST_ORD = JSONObject.parseObject(js_APPLY_ORDER_REQ.getString("CST_ORD"));
            String subscribeId = js_CST_ORD.getString("SUBSCRIBE_ID");
            /**
             * 集客下发并行核查，订单中心直接拆单给二干和本地，下发本地的并行核查需要区分AZ
             */
            String parallelFlag = "";
            // 保留字段
            if(js_APPLY_ORDER_REQ.keySet().contains("PARA")){
                JSONArray paraList = JSON.parseArray(js_APPLY_ORDER_REQ.getString("PARA"));
                if(paraList.size()>0){
                    for(Object temp : paraList){
                        JSONObject para = (JSONObject)temp;
                        if("FLAG".equals(para.getString("PARA_ID"))){
                            parallelFlag = para.getString("PARA_VALUE");
                        }
                    }
                }
            }

            // 取值 CST_ORD_INFO 下的字段
            JSONObject js_CST_ORD_INFO = JSONObject.parseObject(js_CST_ORD.getString("CST_ORD_INFO"));
            String[] jcoi = CUSORD.split(",");
            Map<String, Object> cstOrdInfo = new HashMap<String, Object>();
            for (int j = 0; j < jcoi.length; j++) {
                String str = js_CST_ORD_INFO.getString(jcoi[j]);
                cstOrdInfo.put(jcoi[j], str != null ? str : "");
            }
            // 受理人名称
            cstOrdInfo.put("HANDLE_MAN_NAME", js_CST_ORD_INFO.getString("TRADE_STAFF_NAME"));
            // 客户行业
            cstOrdInfo.put("CUST_INDUSTRY", js_CST_ORD_INFO.getString("CUST_INDUSTRY"));
            // 发展人渠道名称
            cstOrdInfo.put("DEVELOPER_DEPART_NAME", js_CST_ORD_INFO.getString("DEVELOPER_DEPART_NAME"));
            // 受理人联系电话
            cstOrdInfo.put("HANDLE_MAN_TEL", js_CST_ORD_INFO.getString("TRADE_STAFF_PHONE"));
            // 受理部门
            //cstOrdInfo.put("HANDLE_DEP", js_CST_ORD_INFO.getString("DEPART_NAME"));
            cstOrdInfo.put("HANDLE_CITY_ID", js_CST_ORD_INFO.getString("TRADE_EPARCHY_CODE"));
            cstOrdInfo.put("HANDLE_CITY", wsd.qryCityName(js_CST_ORD_INFO.getString("TRADE_EPARCHY_CODE")));
            cstOrdInfo.put("HANDLE_TIME", js_CST_ORD_INFO.getString("ACCEPT_DATE"));

            // 受理区域
            String dealAreaCode = js_CST_ORD_INFO.getString("DEAL_AREA_CODE");
            cstOrdInfo.put("DEAL_AREA_CODE", dealAreaCode);
            //      String handleDepId = wsd.selectDeptId(dealAreaCode);
            JSONObject js_SRV_ORD_LIST = JSONObject.parseObject(js_CST_ORD_INFO.getString("SRV_ORD_LIST"));
            JSONArray js_SRV_ORD = JSON.parseArray(js_SRV_ORD_LIST.getString("SRV_ORD"));
            JSONObject srvOrdJson = js_SRV_ORD.getJSONObject(0);
            JSONObject srvOrdInfoJson = JSONObject.parseObject(srvOrdJson.getString("SRV_ORD_INFO"));
            JSONArray srvAttrInfo = JSON.parseArray(srvOrdInfoJson.getString("SRV_ATTR_INFO"));
            // 获取A端省/A端地市
            String provinceA = "";
            String orderTitle = "";
            String checkStyle = "";
            for (Object tmp : srvAttrInfo) {
                JSONObject tmpJson = (JSONObject) tmp;
                if ("CON0101".equals(tmpJson.getString("ATTR_CODE"))) {
                    provinceA = tmpJson.getString("ATTR_VALUE");
                }
                if("200003301".equals(tmpJson.getString("ATTR_CODE"))){
                    orderTitle = tmpJson.getString("ATTR_VALUE");
                }
                /**
                 * [2195191]【联通集团OSS2.0_二干调度】政企全在线_二阶段：并行核查流程涉及收单接口修改
                 * 核查方式  1、精准串行核查 2、快速并行核查"
                 */
                if("ORD10222".equals(tmpJson.getString("ATTR_CODE"))){
                    checkStyle = tmpJson.getString("ATTR_VALUE");
                }
            }
            /* 根据CON0101 进行省份转换成 省公司进行调度
             */
            String handleDepId = wsd.selectDeptId(provinceA);
            if (handleDepId != null && !"".equals(handleDepId)) {
                cstOrdInfo.put("HANDLE_DEP_ID", handleDepId);
                // 根据部门id查询部门信息
                Map deptInfo = orderDealDao.queryDeptIdByParentDeptId(handleDepId);
                // 受理部门
                cstOrdInfo.put("HANDLE_DEP", MapUtils.getString(deptInfo, "DEPT_NAME"));
            }
            else {
                Map retMap = responseJson("1", "报文参数无法解析，请检查节点CON0101[A端省]！");
                return retMap;
            }
            cstOrdInfo.put("ROUTE_TYPE", routeType);
            cstOrdInfo.put("ROUTE_VALUE", routeValue);
            cstOrdInfo.put("SUBSCRIBE_ID", subscribeId);
            cstOrdInfo.put("APPLY_ORD_ID", subscribeId);
            cstOrdInfo.put("APPLY_ORD_NAME", subscribeId);

            Iterator it = js_SRV_ORD.iterator();
            String activeType = "";
            int cstOrdId = 0;
            while (it.hasNext()) {
                JSONObject jso = (JSONObject) it.next();
                Map<String, Object> param = new HashMap<String, Object>();
                String serviceId = jso.getString("SERVICE_ID");
                //根据SERVICE_ID节点信息查询编码关系表
                Map<String, Object> codeMap = wsd.queryCodeInfoFromRelateTable(serviceId, "product_type");
                if (codeMap == null) {
                    Map retMap = responseJson("1", "发起流程失败，找不到产品，请检查SERVICE_ID节点");
                    return retMap;
                }
                else {
                    serviceId = MapUtils.getString(codeMap, "SERVICE_ID");
                }

                JSONObject js_SRV_ORD_INFO = JSONObject.parseObject(jso.getString("SRV_ORD_INFO"));
                JSONArray js_SRV_ATTR_INFO = JSON.parseArray(js_SRV_ORD_INFO.getString("SRV_ATTR_INFO"));

                // 获取全称要求完成时间
                String reqFinDate = "";
                for (Object tmp : js_SRV_ATTR_INFO) {
                    JSONObject tmpJson = (JSONObject) tmp;
                    if ("CON0014".equals(tmpJson.getString("ATTR_CODE"))) {
                        if(tmpJson.getString("ATTR_VALUE").length()==8){
                            reqFinDate = df.format(dfStr.parse(tmpJson.getString("ATTR_VALUE")));
                        }else if(tmpJson.getString("ATTR_VALUE").length()>8){
                            reqFinDate = df2.format(dfStr2.parse(tmpJson.getString("ATTR_VALUE")));
                        }else{
                            reqFinDate=tmpJson.getString("ATTR_VALUE");
                        }
                    }
                }
                String[] jdsoi = SRVORDINFO.split(",");
                Map srvOrdInfo = new HashMap();
                for (int k = 0; k < jdsoi.length; k++) {
                    String str = jso.getString(jdsoi[k]);
                    srvOrdInfo.put(jdsoi[k], str != null ? str : "");
                }

                srvOrdInfo.put("RE_ACTIVE_TYPE", srvOrdInfo.get("ACTIVE_TYPE"));
                srvOrdInfo.put("ACTIVE_TYPE", activeType);
                srvOrdInfo.put("SERVICE_ID", serviceId);
                srvOrdInfo.put("CHANGE_FLAG", jso.get("CHANGE_FLAG"));
                srvOrdInfo.put("INSTANCE_ID", jso.get("SERIAL_NUMBER").toString());

                srvOrdInfo.put("ORDER_TYPE", "101");
                if ("2009".equals(jso.get("TRADE_TYPE_CODE").toString())) {
                    srvOrdInfo.put("ORDER_TYPE", "102");
                }
                srvOrdInfo.put("ORDER_ID", "0");
                srvOrdInfo.put("RESOURCES", "jike");
                srvOrdInfo.put("SYSTEM_RESOURCE", "second-schedule-lt");
                srvOrdInfo.put("SUBSCRIBE_ID",MapUtils.getString(cstOrdInfo,"SUBSCRIBE_ID"));
                srvOrdInfo.put("CUST_ID",MapUtils.getString(cstOrdInfo,"CUST_ID"));
                srvOrdInfo.put("PARALLEL_FLAG",parallelFlag);// 并行标志
                // 查询订单是否存在，如果已存在就合单
                Map<String,Object> cstMap = interfaceJikeDao.qryCstOrdId(srvOrdInfo);
                
                if(StringUtils.isEmpty(MapUtils.getString(cstMap,"CST_ORD_ID"))){
                    wsd.insertCstOrd(cstOrdInfo);//返回主键CST_ORD_ID
                    cstOrdId = (int)cstOrdInfo.get("CST_ORD_ID");
                } else {
                    cstOrdId = MapUtils.getIntValue(cstMap,"CST_ORD_ID");
                }
                srvOrdInfo.put("CST_ORD_ID", cstOrdId);
                srvOrdInfo.put("REQ_FIN_TIME", reqFinDate);

                wsd.addGomIdcSrvOrdInfo(srvOrdInfo);//返回主键SRV_ORD_ID

                int srvOrdId = (int) srvOrdInfo.get("SRV_ORD_ID");
                srvOrdIdStr = MapUtils.getString(srvOrdInfo, "SRV_ORD_ID");
                //订单入库启动单子流程
                if (jso.getString("SERVICE_OFFER_ID") != null) {
                    Map map = new HashMap();
                    map.put("code_type_name", serviceId);
                    map.put("code_value", jso.getString("SERVICE_OFFER_ID"));
                    /**
                     * [2195190]【联通集团OSS2.0_本地调度】政企全在线_二阶段：并行核查流程涉及收单接口修改
                     * 如果是核查流程，默认核查方式值为1
                     * 核查方式  1、精准串行核查 2、快速并行核查"
                     * 增加参数方便查询出并行核查流程
                     */
                    if("102".equals(MapUtils.getString(srvOrdInfo,"ORDER_TYPE", ""))){
                        map.put("ORDER_NO","".equals(checkStyle)?"1":checkStyle);
                    }
                    List<Map<String, Object>> qryOrdPsId = wsd.qryOrdPsId(map);
                    if (qryOrdPsId != null && qryOrdPsId.size() > 0 && qryOrdPsId.get(0).get("SORT_NO") != null) {
                        activeType = MapUtils.getString(qryOrdPsId.get(0), "CODE_CONTENT", "");
                        interfaceJikeDao.updateActTypeBySrvOrdId(srvOrdId,activeType);
                        param.put("ordPsid", qryOrdPsId.get(0).get("SORT_NO"));
                        param.put("ORDER_TITLE", js_CST_ORD.getString("SUBSCRIBE_ID"));
                        param.put("ORDER_CONTENT", "");
                        param.put("requFineTime", reqFinDate);
                        param.put("AREA", "350002000000000042766408");

                        Map<String, Object> acceptAreaMap = new HashMap<String, Object>();
                        // acceptAreaMap.put("REGION_ID", orderDealService.queryDeptId(cstOrdInfo.get("CUST_CITY").toString()));
                        acceptAreaMap.put("REGION_ID", handleDepId);
                        acceptAreaMap.put("ACT_TYPE", qryOrdPsId.get(0).get("JIKE_ACT_TYPE"));
                        acceptAreaMap.put("PRODUCT_TYPE", serviceId);
                        param.put("attr", acceptAreaMap);

                      /*  Map retMap = orderDealService.createOrder(param);
                        orderId = (String) retMap.get("orderId");
                        // 添加操作日志
                        insertLog(orderId, jso.getString("ACTIVE_TYPE"));
                        param.put("activeType",jso.getString("ACTIVE_TYPE"));
                        param.put("srvOrdId",srvOrdId);*/
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Map retMap = orderDealService.createOrder(param);
                                String orderId = MapUtils.getString(retMap,"orderId");
                                interfaceJikeDao.updateOrderIdBySrvOrdId(srvOrdId,orderId);
                                // 添加操作日志
                                insertLog(orderId, jso.getString("ACTIVE_TYPE"));
                                logger.info("开始准备发送短信。。。。。。。。。。。。。。。。。。。。");
                                Map<String, Object> sendMsgMap = new HashMap<String, Object>();
                                sendMsgMap.put("areaId", handleDepId);
                                sendMsgMap.put("orderIdList", orderId);
                                sendMsgMap.put("operAction", "集客来单");
                                sendMsgMap.put("resources", "jike");
                                orderSendMsgService.sendMsgBefore(sendMsgMap);
                            }
                        });
                        thread.start();
                    }
                    else {
                        Map retMap = responseJson("1", "发起流程失败，找不到对应的流程，请检查SERVICE_OFFER_ID节点");
                        return retMap;
                    }
                }
                else {
                    Map retMap = responseJson("1", "发起流程失败，找不到对应的流程，请检查SERVICE_OFFER_ID节点");
                    return retMap;
                }
                List<Map> srvOrdAttrList = new ArrayList<Map>();
                String[] srvOrdToAttr = SRVORD_TO_ATTR.split(",");
                for(String key : srvOrdToAttr){
                    if(jso.containsKey(key)){
                        Map srvAttrTemp = new HashMap();
                        srvAttrTemp.put("SRV_ORD_ID",srvOrdId);
                        srvAttrTemp.put("SOURSE","jike");
                        srvAttrTemp.put("ATTR_ACTION","0");
                        srvAttrTemp.put("ATTR_VALUE_NAME","");
                        srvAttrTemp.put("ATTR_CODE", key);
                        srvAttrTemp.put("ATTR_VALUE", jso.getString(key));
                        srvAttrTemp.put("OLD_ATTR_VALUE", "");
                        srvOrdAttrList.add(srvAttrTemp);
                    }
                }
                String custProvince = MapUtils.getString(cstOrdInfo,"CUST_PROVINCE","");
                // 判断是否异地发起省业务
                if(custProvince.equals(provinceA)){
                    Map srvAttrTemp = new HashMap();
                    srvAttrTemp.put("SRV_ORD_ID",srvOrdId);
                    srvAttrTemp.put("SOURSE","jike");
                    srvAttrTemp.put("ATTR_ACTION","0");
                    srvAttrTemp.put("ATTR_VALUE_NAME","");
                    srvAttrTemp.put("ATTR_CODE", "IS_OTHER_PROVINCES");
                    srvAttrTemp.put("ATTR_VALUE", "是");
                    srvAttrTemp.put("OLD_ATTR_VALUE", "");
                    srvOrdAttrList.add(srvAttrTemp);
                }

                Iterator ti = js_SRV_ATTR_INFO.iterator();
                while (ti.hasNext()) {
                    JSONObject jsoa = (JSONObject) ti.next();
                    String[] jdsoai = SRVORDATTRINFO.split(",");
                    Map srvOrdAttr = new HashMap();
                    for (int k = 0; k < jdsoai.length; k++) {
                        String str = jsoa.getString(jdsoai[k]);
                        srvOrdAttr.put(jdsoai[k], str != null ? str : "");
                    }
                    // 属性转换
                    String attrCode = MapUtils.getString(srvOrdAttr, "ATTR_CODE");
                    String attrValue = MapUtils.getString(srvOrdAttr, "ATTR_VALUE");
                    String oldAttrValue = MapUtils.getString(srvOrdAttr, "OLD_ATTR_VALUE");
                    // 要求完成时间
                    if (Arrays.asList(FIN_DATE).contains(attrCode)) {
                        logger.info(MapUtils.getString(srvOrdAttr, "ATTR_VALUE", ""));
                        if(MapUtils.getString(srvOrdAttr, "ATTR_VALUE", "").length()==8){
                            srvOrdAttr.put("ATTR_VALUE", df.format(dfStr.parse(MapUtils.getString(srvOrdAttr, "ATTR_VALUE", ""))));
                        }else if(MapUtils.getString(srvOrdAttr, "ATTR_VALUE", "").length()>8){
                            srvOrdAttr.put("ATTR_VALUE",df2.format(dfStr2.parse(MapUtils.getString(srvOrdAttr, "ATTR_VALUE", ""))));
                        }else{
                            srvOrdAttr.put("ATTR_VALUE", MapUtils.getString(srvOrdAttr, "ATTR_VALUE", ""));
                        }
                    }
                    List<Map<String, Object>> attrInfoList = interfaceJikeDao.qryAttrInfo(attrCode, attrValue, oldAttrValue, serviceId);
                    if (attrInfoList != null && attrInfoList.size() > 0) {
                        for (int i = 0; i < attrInfoList.size(); i++) {
                            Map<String, Object> tmpMap = attrInfoList.get(i);
                            if (i == 0) {
                                srvOrdAttr.put("ATTR_CODE", MapUtils.getString(tmpMap, "ATTR_CODE", ""));
                                srvOrdAttr.put("ATTR_VALUE", MapUtils.getString(tmpMap, "ATTR_VALUE", ""));
                                //modefy by wang.gang2 ZMP  这个要重新存一下转换后得old_attr_value
                                srvOrdAttr.put("OLD_ATTR_VALUE", MapUtils.getString(tmpMap, "OLD_ATTR_VALUE", ""));
                            }
                            else {
                                Map<String, Object> attrTmp = new HashMap<>();
                                attrTmp.put("SRV_ORD_ID", srvOrdId);
                                attrTmp.put("SOURSE", "jike");
                                attrTmp.put("ATTR_ACTION", "0");
                                attrTmp.put("ATTR_VALUE_NAME", "");
                                attrTmp.put("ATTR_CODE", MapUtils.getString(tmpMap, "ATTR_CODE", ""));
                                attrTmp.put("ATTR_VALUE", MapUtils.getString(tmpMap, "ATTR_VALUE", ""));
                                attrTmp.put("OLD_ATTR_VALUE", MapUtils.getString(tmpMap, "OLD_ATTR_VALUE", ""));
                                srvOrdAttrList.add(attrTmp);
                            }
                        }
                    }

                    srvOrdAttr.put("SRV_ORD_ID", srvOrdId);
                    srvOrdAttr.put("SOURSE", "jike");
                    srvOrdAttrList.add(srvOrdAttr);
                }
                //  批量操作，避免主键冲突
                wsd.bachAddGomIdcSrvOrdAttrInfo(srvOrdAttrList);
                /*
                如果是非新开的正常开通单、核查单，需要调用查询资源接口，
                根据资源返回的产品实例好、电路编号更新数据
                 */
                srvOrdInfo.put("HANDLE_DEP_ID", MapUtils.getString(cstOrdInfo, "HANDLE_DEP_ID"));
                updateCirInfo(srvOrdInfo);

                // 解析附加产品
                if (jso.keySet().contains("ADD_PROD_LIST")) {
                    JSONObject ADD_PROD_LIST = jso.getJSONObject("ADD_PROD_LIST");
                    JSONArray addProdInfo = ADD_PROD_LIST.getJSONArray("ADD_PROD_INFO");
                    for (int i = 0; i < addProdInfo.size(); i++) {
                        Map<String, Object> addProdMap = new HashMap<>();
                        String[] addProdStrs = ADDPRODINFO.split(",");
                        JSONObject temp = addProdInfo.getJSONObject(i);
                        temp.put("SRV_ORD_ID", srvOrdId);
                        for (int s = 0; s < addProdStrs.length; s++) {
                            String str = temp.getString(addProdStrs[s]);
                            addProdMap.put(addProdStrs[s], str != null ? str : "");
                        }
                        // 插入库表，返回主键id
                        interfaceJikeDao.addGomAddProdInfo(temp);
                        String addProdInfoId = MapUtils.getString(temp, "ADD_PROD_INFO_ID");
                        // 解析附加产品属性信息
                        if (temp.keySet().contains("ADD_ATTR_INFO")) {
                            JSONArray addAttrInfo = ADD_PROD_LIST.getJSONArray("ADD_ATTR_INFO");
                            if (addAttrInfo != null && addAttrInfo.size() > 0) {
                                Iterator addAttrTemp = addAttrInfo.iterator();
                                List<Map<String, Object>> addAttrList = new ArrayList<>();
                                while (addAttrTemp.hasNext()) {
                                    Map<String, Object> addAttrMap = new HashMap<>();
                                    String[] addAttrStrs = ADDPRODATTRINFO.split(",");
                                    temp.put("ADD_PROD_INFO_ID", addProdInfoId);
                                    for (int s = 0; s < addAttrStrs.length; s++) {
                                        String str = temp.getString(addProdStrs[s]);
                                        addProdMap.put(addProdStrs[s], str != null ? str : "");
                                    }
                                    addAttrList.add(addAttrMap);
                                }
                                // 插入库表, 批量操作，避免主键冲突
                                interfaceJikeDao.bachAddGomAddProdAttrInfo(addAttrList);
                            }
                        }
                    }
                }
                // 解析附件信息,下载并入库
                if(jso.keySet().contains("ATTACH_INFO_LIST")){
                    JSONObject attachInfoList = jso.getJSONObject("ATTACH_INFO_LIST");
                    addFile(attachInfoList,String.valueOf(srvOrdId),"JIKE");
                }
                // 补录单操作
                Map<String,Object> suspendParam = new HashMap<>();
                suspendParam.put("activeType",activeType);
                suspendParam.put("instanceId",MapUtils.getString(srvOrdInfo,"INSTANCE_ID"));
                resSupplementDealServiceIntf.supplementStop(suspendParam);
            }
            if(!"".equals(activeType)){
                List<String> actList = interfaceJikeDao.qryActName("operate_type", activeType);
                if(actList!=null &&actList.size()>0){
                    String activeTypeName = actList.get(0);
                    if(!"".equals(activeTypeName)){
                        if(!"".equals(orderTitle)){
                            interfaceJikeDao.updateApplyOrdName(cstOrdId,orderTitle);
                        }else {
                            interfaceJikeDao.updateApplyOrdName(cstOrdId, MapUtils.getString(cstOrdInfo, "CUST_NAME_CHINESE") + "-" + activeTypeName);
                        }
                    }
                }
            }
            resultMap = responseJson("0", "发起流程成功");
            resultMap.put("srvOrdId",srvOrdIdStr);
        }
        catch (Exception e) {
            logger.error("订单接收接口异常：" + e.getMessage(), e);
            resultMap = responseJson("1", "报文数据格式不正确，请检查数据");
        }
        return resultMap;
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
     * 如果是非新开单，起线程调用查询资源接口，
     * 根据资源返回的产品实例好、电路编号更新数据
     *
     * @param srvOrdInfo
     */
    private void updateCirInfo(Map srvOrdInfo) {
        logger.info("--updateCirInfo--srvordIndo.ACTIVE_TYPE:" + MapUtils.getString(srvOrdInfo, "ACTIVE_TYPE"));
        if (!"101".equals(MapUtils.getString(srvOrdInfo, "ACTIVE_TYPE", ""))) {
            Map<String, Object> params = new HashMap<>();
            params.put("productType", MapUtils.getString(srvOrdInfo, "SERVICE_ID"));
            params.put("accNbr", MapUtils.getString(srvOrdInfo, "SERIAL_NUMBER"));
            /**
             0：是，（单纯的查电路和关联配置信息）
             1：否（作为变更/拆机查老实例使用）
             按道理这里应该是1 ，需要精确查找，由于只是通过业务号码去查，取值为0或者1 都无所谓，资源配置都会通过业务号码做精确查找
             */
            params.put("isToBeQuery", "0");
            params.put("HANDLE_DEP_ID", MapUtils.getString(srvOrdInfo, "HANDLE_DEP_ID"));
            params.put("SRV_ORD_ID", MapUtils.getString(srvOrdInfo, "SRV_ORD_ID"));
            BusinessQueryThread resQryThread = new BusinessQueryThread(params);
            // 启动线程异步处理
            resQryThread.start();
        }

    }

    /**
     * 拼装反馈报文
     *
     * @param respCode
     * @param respDesc
     * @return
     */
    public Map responseJson(String respCode, String respDesc) {
        Map map = new HashMap();
        map.put("RESP_DESC", respDesc);
        map.put("RESP_CODE", respCode);
        Map applyMap = new HashMap();
        applyMap.put("APPLY_ORDER_RSP", map);
        Map bodyMap = new HashMap();
        bodyMap.put("UNI_BSS_BODY", applyMap);
        return bodyMap;
    }

    /**
     * 添加操作记录
     */
    public void insertLog(String orderId, String activeType) {
        // 添加操作记录
        Map paramsMap = new HashMap();
        switch (activeType) {
            case "4A": {
                paramsMap.put("trackContent", "追单");
                paramsMap.put("operType", OrderTrackOperType.OPER_TYPE_5);
                paramsMap.put("trackMessage", "集客下发追单成功");
                break;
            }
            default: {
                paramsMap.put("trackContent", "集客下发");
                paramsMap.put("operType", OrderTrackOperType.OPER_TYPE_17);
                paramsMap.put("trackMessage", "集客下发正常单成功");
                break;
            }
        }
        paramsMap.put("orderId", orderId);
        paramsMap.put("woOrdId", null);
        paramsMap.put("trackOrgId", null);
        paramsMap.put("trackOrgName", "集客系统");
        paramsMap.put("trackDate", new java.sql.Date(new java.util.Date().getTime()));
        paramsMap.put("createDate", new java.sql.Date(new java.util.Date().getTime()));
        paramsMap.put("trackStaffId", null);
        paramsMap.put("trackStaffName", null);
        paramsMap.put("trackStaffPhone", null);
        paramsMap.put("trackStaffEmail", null);

        orderDealDao.insertTrackLogInfo(paramsMap);
    }
    public Map rename(String serialNumber,Map oldCustomerInfo,Map newCustomerInfo){

        Map resultMap = new HashMap();
        try{
                //1.获取变更前数据  modify by wang.gang2 2020/09/23 需求变更 要求根据电路做变更 根据业务号码做关联 已合单不允许操作

                List < RenamCustInfoDTO > oldCustomerInfoDTO = wsd.queryCustRenameList(serialNumber);
                if (oldCustomerInfoDTO.size() != 1) {
                    //resultMap = responseJson("1", "该客户订单号不存在或者存在多条电路，不能过户");
                    resultMap = responseJson("0", "过户更名处理成功");
                }else{
                    //2.获取变更后数据
                    String cstOrdId = oldCustomerInfoDTO.get(0).getCstOrdId();
                    RenamCustInfoDTO oldCustInfoDTO = JsonConverterUtil.convertUpperKeyMap2Bean(oldCustomerInfo, RenamCustInfoDTO.class);
                    RenamCustInfoDTO newCustInfoDTO = JsonConverterUtil.convertUpperKeyMap2Bean(newCustomerInfo, RenamCustInfoDTO.class);
                    Map<String, Object> changeContent = changeContent(newCustInfoDTO, oldCustInfoDTO);
                    // 3.将变更差异值存入数据库
                    if(!"".equals(MapUtils.getString(changeContent,CHANGE_MESSAGE))
                            && MapUtils.getString(changeContent,CHANGE_MESSAGE)!= null){
                        // 3.1先查询版本号是否存在，如果不存在则默认1， 存在则加1
                        for (RenamCustInfoDTO customerInfoDTO : oldCustomerInfoDTO) {
                            Map<String, Object> tempMap = wsd.queryMaxCustInfoRenameVersion(customerInfoDTO.getCstOrdId());
                            int maxVersion = MapUtils.getIntValue(tempMap, "MAX_VERSION", 0);
                            insertChangeInfo(customerInfoDTO.getCstOrdId(), "","", maxVersion, changeContent);
                        }
                        //4.更新客户信息
                        Map changeData = MapUtils.getMap(changeContent, "custInfo");
                        changeData.put("CST_ORD_ID", cstOrdId);
                        wsd.updateCustInfoRename( changeData);
                        // 查询该客户订单下所有电路
                        List<Map<String,Object>> list = wsd.querySrvOrdList(serialNumber);
                        if(!ListUtil.isEmpty(list)){
                            //起线程调用资源过户接口
                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    for(Map<String,Object> param : list){
                                        param.put("custInfo",newCustInfoDTO);
                                        transferUserServiceIntf.transferUser(param);
                                    }
                                }
                            });
                            thread.start();
                        }
                    }
                    resultMap = responseJson("0", "过户更名处理成功");
                }
            } catch (Exception e) {
                resultMap = responseJson("1", "过户更名处理失败");
                logger.debug(e.getMessage());
        }
        return resultMap;
    }

    /**
     * 记录接口日志
     * @param logInfo
     */
    private void insertInterfaceLog(Map<String,Object> logInfo){
        Map<String,Object> interflog = new HashMap<String, Object>();
        interflog.put("INTERFNAME",MapUtils.getString(logInfo,"interfname"));
        interflog.put("URL",MapUtils.getString(logInfo,"url"));
        interflog.put("CONTENT", MapUtils.getString(logInfo,"request"));
        interflog.put("RETURNCONTENT",MapUtils.getString(logInfo,"respone"));
        interflog.put("ORDERNO",MapUtils.getString(logInfo,"tradeId"));
        interflog.put("REMARK", MapUtils.getString(logInfo,"remark"));
        wsd.insertInterfLog(interflog);
    }



    /**
     * 普通的两个对象比较，得到修改的内容
     * @param newSource 新的对象
     * @param oldSource 旧对象
     * @return 返回差异内容
     */
    public Map<String, Object> changeContent(Object newSource, Object oldSource) {
        // 变更信息code
        StringBuilder changeData = new StringBuilder();
        // 更新的过户信息
        Map<String, String> custInfo = new HashMap();
        // 变更信息message
        StringBuilder changeMessage = new StringBuilder();
        if (null == newSource || null == oldSource) {
            return null;
        }
        // 取出新的class类
        Class<?> newSourceClass = newSource.getClass();
        // 类的所有声明的字段
        Field[] newSourceFields = newSourceClass.getDeclaredFields();
        for (Field newField : newSourceFields) {
            String fieldName = newField.getName();

            // 如果是引用对象，则退出本次循环。继续下次
            String typeStr = String.valueOf(newField.getType());
            if (typeStr.contains("com") || typeStr.contains("List") || "cstOrdId".equals(fieldName)) {
                continue;
            }
            // 获取新的Field值
            String newValue = MapConverterUtil.getFieldValue(newSource, fieldName) == null ? "" : MapConverterUtil.getFieldValue(newSource, fieldName).toString().replaceAll("@enter@","\n");
            // 获取对应的旧的targetField值
            String oldValue = MapConverterUtil.getFieldValue(oldSource, fieldName) == null ? "" : MapConverterUtil.getFieldValue(oldSource, fieldName).toString();
            if (org.apache.commons.lang3.StringUtils.isEmpty(newValue) && org.apache.commons.lang3.StringUtils.isEmpty(oldValue)) {
                continue;
            }
            FieldMeta fieldMeta = newField.getAnnotation(FieldMeta.class);
            if (fieldMeta != null && !newValue.equals(oldValue)) {
                if (org.apache.commons.lang3.StringUtils.isNotEmpty(fieldMeta.column())) {
                    changeData.append("{\"key\":\"" + fieldMeta.column() + "\",\"oldValue\":\"" + oldValue + "\",\"newValue\":\"" + newValue + "\"},");
                    custInfo.put(fieldMeta.column(),newValue);
                }
                if (org.apache.commons.lang3.StringUtils.isNotEmpty(fieldMeta.name())) {
                    String enumType= fieldMeta.value();
                    //需要翻译枚举值
                    if ("CITY_CODE".equals(enumType)||"PROVINCE_CODE".equals(enumType)){ //地市 、省份
                        Map<String,Object> areaInfo = wsd.conversionAreas(enumType,oldValue,newValue);
                        oldValue=  MapUtils.getString(areaInfo, "OLDVALUE", "");
                        newValue=  MapUtils.getString(areaInfo, "NEWVALUE", "");
                    }

                    if ("CustType".equals(enumType) || "CERTI_TYPE_CODE".equals(enumType)){ //客户类型、证件类型
                        Map<String,Object> enumInfo = wsd.conversionEnum(enumType,oldValue,newValue);
                        oldValue=  MapUtils.getString(enumInfo, "OLDVALUE", "");
                        newValue=  MapUtils.getString(enumInfo, "NEWVALUE", "");
                    }

                    changeMessage.append("{\"key\":\"" + fieldMeta.name() + "\",\"oldValue\":\"" + oldValue + "\",\"newValue\":\"" + newValue + "\"},");
                }
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("custInfo", custInfo);
        map.put(CHANGE_DATA, changeData.toString());
        map.put(CHANGE_MESSAGE, changeMessage.toString());
        return map;
    }



    /**
     * 存入变更信息表
     * @param cstOrdId

     * @param userId
     * @param userName
     * @param changeInfoMap
     */
    private void insertChangeInfo(String cstOrdId, String userId,String userName, int maxVersion, Map<String, Object> changeInfoMap) {

        Map<String, Object> map = new HashMap();
        map.put("CST_ORD_ID", cstOrdId);
        map.put("CHG_VERSION", ++maxVersion);
        map.put("USER_ID", userId);
        map.put("USER_ID", userName);
        // 有差异值才存入数据库，没差异值不存入
        if (!StringUtils.isEmpty(MapUtils.getString(changeInfoMap,(ExceptionChangeService.CHANGE_DATA)))) {
            map.put("CHANGE_DATA", "[" + changeInfoMap.get(ExceptionChangeService.CHANGE_DATA) + "]");
            map.put("CHANGE_MESSAGE", "[" + changeInfoMap.get(ExceptionChangeService.CHANGE_MESSAGE) + "]");
            wsd.insertCustInfoRenameLog(map);
        }
    }

}
