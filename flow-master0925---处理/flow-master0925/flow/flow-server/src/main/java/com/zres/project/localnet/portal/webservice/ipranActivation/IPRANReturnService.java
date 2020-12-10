package com.zres.project.localnet.portal.webservice.ipranActivation;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.flowdealinfo.service.CommonMethodDealWoOrderServiceInf;
import com.zres.project.localnet.portal.util.OrderTrackOperType;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/backIPranService")
public class IPRANReturnService implements IPRANReturnIntf {
    private static Logger logger = LoggerFactory.getLogger(IPRANReturnService.class);

    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private CommonMethodDealWoOrderServiceInf commonMethodDealWoOrderServiceInf;

    @Override
    @PostMapping(value = "/interfaceBDW/createBack.spr", produces = "application/json;charset=UTF-8")
    @IgnoreSession
    public Map<String, Object> createBack(@RequestBody String jsonStr) {
        Map<String, Object> returnMap = new HashMap<>();
        //准备报文参数
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        String woId = jsonObject.getString("orderNbr");//工单号
        String orderReplyNo = jsonObject.getString("orderReplyNo");//返单流水号
        String status = jsonObject.getString("status");//状态
        String reason = jsonObject.getString("reason");//失败原因
        String servCode = jsonObject.getString("servCode");//对应调单中的业务编码
        String masterVcid = jsonObject.getString("masterVcid");
        String standbyVcidJson = jsonObject.getString("standbyVcid");
        JSONObject assignedACVcidJson = jsonObject.getJSONObject("AssignedAC");
        JSONObject aSite = jsonObject.getJSONObject("aSite");
        JSONObject aSite2 = jsonObject.getJSONObject("aSite2");
        JSONObject zSite = jsonObject.getJSONObject("zSite ");
        JSONObject zSite2 = jsonObject.getJSONObject("zSite2");
        JSONArray pathInfoList = JSONObject.parseArray("pathInfo");

        return returnMap;
    }

    @Override
    @PostMapping(value = "/interfaceBDW/removeBack.spr", produces = "application/json;charset=UTF-8")
    @IgnoreSession
    public Map<String, Object> removeBack(@RequestBody String jsonStr) {
        Map<String, Object> returnMap = new HashMap<>();
        String reStatus = "";
        String reReason = "";
        //准备报文参数
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        try {
            String woId = jsonObject.getString("orderNbr");//工单号
            String orderReplyNo = jsonObject.getString("orderReplyNo");//返单流水号
            String status = jsonObject.getString("status");//状态
            String reason = jsonObject.getString("reason");//失败原因
            String servCode = jsonObject.getString("servCode");//对应调单中的业务编码
            //回单
            if ("0".equals(status)) {//激活成功
                //TODO 参数校验 没有添加具体流程
                Map<String, Object> commonMap = new HashMap<>();
                Map<String, Object> operAttrsVal = new HashMap<>();  //线条参数 ,工单流转使用
                commonMap.put("orderId", "");
                commonMap.put("woId", woId);
                commonMap.put("operStaffId", "11");
                commonMap.put("tacheId", BasicCode.DATA_MAKE);//500001158
                commonMap.put("remark", "IPRAN回单成功");
                commonMap.put("action", "回单"); //动作
                commonMap.put("operType", OrderTrackOperType.OPER_TYPE_4); //操作类型
                operAttrsVal.put("isNeedResConstruct", "1");
                operAttrsVal.put("isDataMakeBack", "1");
                commonMap.put("operAttrsVal", operAttrsVal);
                commonMethodDealWoOrderServiceInf.commonComplateWo(commonMap);
            } else {
                //失败，修改工单状态为处理中
                orderDealDao.updateWoStateByWoId(woId, OrderTrackOperType.WO_ORDER_STATE_2);
            }

            //记录激活结果
            Map<String, Object> actSendMap = new HashMap<>();
            actSendMap.put("woId", woId);
            actSendMap.put("feedSystem", "省份IP智能网管系统"); // 省份IP智能网管系统
            actSendMap.put("activateCode", "0".equals(status) ? "0" : "1");
            actSendMap.put("activateDesc", reason);
            wsd.saveActivateInfo(actSendMap); //写入激活日志记录，

            //拼装返回报文
            returnMap.put("orderNbr", woId);
            returnMap.put("orderReplyNo", orderReplyNo);
            returnMap.put("status", reStatus);
            returnMap.put("reason", reReason);

            //记录回单日志
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("interfname", "标准地址查询接口");
            map.put("url", "/backIPranService/interfaceBDW/removeBack.spr");
            map.put("content", jsonStr);
            map.put("createdate", new java.sql.Date(new java.util.Date().getTime()));
            map.put("returncontent", returnMap);
            map.put("remark", "接收上海AI中心返回报文");
            map.put("updatedate", new java.sql.Date(new java.util.Date().getTime()));
            //5.报文入库，数据入库
            wsd.saveJson(map);

        } catch (Exception e) {
            logger.error("L2VPN VPWS业务-新开-回单异常" + e.getMessage());
            returnMap.put("status", reStatus);
            returnMap.put("reason", reReason);
            return returnMap;
        }

        return returnMap;
    }

    @Override
    @PostMapping(value = "/interfaceBDW/statusChangeBack.spr", produces = "application/json;charset=UTF-8")
    @IgnoreSession
    public Map<String, Object> statusChange(@RequestBody String jsonStr) {
        Map<String, Object> returnMap = new HashMap<>();
        String reStatus = "";
        String reReason = "";
        try {
            //准备报文参数
            JSONObject jsonObject = JSONObject.parseObject(jsonStr);
            String woId = jsonObject.getString("orderNbr");//工单号
            String orderReplyNo = jsonObject.getString("orderReplyNo");//返单流水号
            String status = jsonObject.getString("status");//状态
            String reason = jsonObject.getString("reason");//失败原因
            String servCode = jsonObject.getString("servCode");//对应调单中的业务编码

            //记录激活结果
            Map<String, Object> actSendMap = new HashMap<>();
            actSendMap.put("woId", woId);
            actSendMap.put("feedSystem", "省份IP智能网管系统"); // 省份IP智能网管系统
            actSendMap.put("activateCode", "0".equals(status) ? "0" : "1");
            actSendMap.put("activateDesc", reason);
            wsd.saveActivateInfo(actSendMap); //写入激活日志记录，

            //拼装返回报文
            returnMap.put("orderNbr", woId);
            returnMap.put("orderReplyNo", orderReplyNo);
            returnMap.put("status", reStatus);
            returnMap.put("reason", reReason);

            //记录回单日志
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("interfname", "标准地址查询接口");
            map.put("url", "/backIPranService/interfaceBDW/removeBack.spr");
            map.put("content", jsonStr);
            map.put("createdate", new java.sql.Date(new java.util.Date().getTime()));
            map.put("returncontent", returnMap);
            map.put("remark", "接收上海AI中心返回报文");
            map.put("updatedate", new java.sql.Date(new java.util.Date().getTime()));
            //5.报文入库，数据入库
            wsd.saveJson(map);
        } catch (Exception e) {
            logger.error("L2VPN VPWS业务-新开-回单异常" + e.getMessage());
            returnMap.put("status", reStatus);
            returnMap.put("reason", reReason);
            return returnMap;
        }

        return returnMap;
    }

    @Override
    @PostMapping(value = "/interfaceBDW/modifyBack.spr", produces = "application/json;charset=UTF-8")
    @IgnoreSession
    public Map<String, Object> modify(@RequestBody String jsonStr) {
        Map<String, Object> returnMap = new HashMap<>();
        //准备报文参数
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        String orderNbr = jsonObject.getString("orderNbr");//工单号
        String orderReplyNo = jsonObject.getString("orderReplyNo");//返单流水号
        String status = jsonObject.getString("status");//状态
        String reason = jsonObject.getString("reason");//失败原因
        String servCode = jsonObject.getString("servCode");//对应调单中的业务编码]

        return returnMap;
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

}
