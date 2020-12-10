package com.zres.project.localnet.portal.webservice.dto;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * @Description:集客的异常单产品基本信息
 * @Author:zhang.kaigang
 * @Date:2019/6/19
 * @Version:1.0
 */
public class JiKeProdInfoDTO {
    // 枚举值，原值和描述值分开，原值在set方法处理，描述值不需要set方法，在get方法处理
    @FieldMeta(column = "SERVICE_ID")
    private String serviceId;

    private String serviceIdName;

    @FieldMeta(column = "TRADE_TYPE_CODE")
    private String tradeTypeCode;

    private String tradeTypeCodeName;

    // 该字段不需要比较，不然的话每次追单确认都会改成4A，追单主表不在电路表，在变更信息表
    private String activeType;

    @FieldMeta(column = "SERVICE_OFFER_ID")
    private String serviceOfferId;

    private String serviceOfferIdName;

    @FieldMeta(column = "SERIAL_NUMBER")
    private String serialNumber;

    @FieldMeta(column = "TRADE_ID")
    private String tradeId;

    @FieldMeta(column = "TRADE_ID_RELA")
    private String tradeIdRela;

    @FieldMeta(column = "USER_ID")
    private String userId;

    @FieldMeta(column = "FLOW_ID")
    private String flowId;

    List<JiKeProdAttrDTO> jiKeProdAttrDTOList;

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
        // set方法里处理是为了将查询出来的serviceId转成描述后赋值给serviceName变量
        this.serviceIdName = ExceptionFlowEnum.getName(serviceId);

    }

    public String getServiceIdName() {
        // get方法里处理是为了将报文转换成对应DTO改变量有值
        if (StringUtils.isNotEmpty(serviceId)) {
            this.serviceIdName = ExceptionFlowEnum.getName(serviceId);
        }
        return serviceIdName;
    }

    public String getTradeTypeCode() {
        return tradeTypeCode;
    }

    public void setTradeTypeCode(String tradeTypeCode) {
        this.tradeTypeCode = tradeTypeCode;
        // set方法里处理是为了将查询出来的枚举值转成描述后赋值给变量
        this.tradeTypeCodeName = ExceptionFlowEnum.getName(tradeTypeCode);

    }
    public String getTradeTypeCodeName() {
        // get方法里处理是为了将报文转换成对应DTO改变量有值
        if (StringUtils.isNotEmpty(tradeTypeCode)) {
            this.tradeTypeCodeName = ExceptionFlowEnum.getName(tradeTypeCode);
        }
        return tradeTypeCodeName;
    }
    public String getActiveType() {
        return activeType;
    }

    public void setActiveType(String activeType) {
        this.activeType = activeType;
    }

    public String getServiceOfferId() {
        return serviceOfferId;
    }

    public void setServiceOfferId(String serviceOfferId) {
        this.serviceOfferId = serviceOfferId;
        // set方法里处理是为了将查询出来的serviceId转成描述后赋值给serviceName变量
        this.serviceOfferIdName = ExceptionFlowEnum.getName(serviceOfferId);

    }

    public String getServiceOfferIdName() {
        // get方法里处理是为了将报文转换成对应DTO改变量有值
        if (StringUtils.isNotEmpty(serviceOfferId)) {
            this.serviceOfferIdName = ExceptionFlowEnum.getName(serviceOfferId);
        }
        return serviceOfferIdName;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getTradeIdRela() {
        return tradeIdRela;
    }

    public void setTradeIdRela(String tradeIdRela) {
        this.tradeIdRela = tradeIdRela;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public List<JiKeProdAttrDTO> getJiKeProdAttrDTOList() {
        return jiKeProdAttrDTOList;
    }

    public void setJiKeProdAttrDTOList(List<JiKeProdAttrDTO> jiKeProdAttrDTOList) {
        this.jiKeProdAttrDTOList = jiKeProdAttrDTOList;
    }

    //----
    //---------
    private String cstOrdId;

    private String srvOrdId;

    private String orderId;

    private String chgOrderId;

    public String getCstOrdId() {
        return cstOrdId;
    }

    public void setCstOrdId(String cstOrdId) {
        this.cstOrdId = cstOrdId;
    }

    public String getSrvOrdId() {
        return srvOrdId;
    }

    public void setSrvOrdId(String srvOrdId) {
        this.srvOrdId = srvOrdId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getChgOrderId() {
        return chgOrderId;
    }

    public void setChgOrderId(String chgOrderId) {
        this.chgOrderId = chgOrderId;
    }

    private String orderType; //订单类型

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }
}
