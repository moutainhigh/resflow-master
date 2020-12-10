package com.zres.project.localnet.portal.order.domain;

import java.io.Serializable;

/**
 * 调单查询Po
 */
public class GomDispatcherOrderPo implements Serializable {

    private static final long serialVersionUID = -5094114129624307232L;

    private String dispatchOrderId; //调单编号
    private String dispatchOrderNo; //调单编号
    private String productCodeContent; //业务类型（产品类型）
    private String activeType; //动作类型
    private String activeTypeName; //动作类型名称
    private String orderId; //流程订单Id
    private String orderIds; //流程订单Id集合
    private String orderType; //单据类型
    private String resources; //来源
    private String cstOrdId; //客户订单Id
    private String srvOrdIds; //业务订单Id集合
    private String serialNumbers; //业务订单Id集合
    private int cirCount; //电路数量
    private String dispatchTitle; //调单标题
    private String orderTitle; //申请单标题
    private String pubDateName; //调单状态
    private String staffName; //拟稿人
    private String staffTel; //联系方式
    private String sendDate; //派发时间
    private String dispatchSendOrg; //发往单位
    private String staffOrg; //申请部门
    private String reqFinDate; //要求完成时间。
    private String custNameChinese; //客户名称
    private String subscribeId; //客户订单号
    private String dispathUrgency; //调单缓急
    private String dispatchSource; //调单来源

    public String getDispatchSource() {
        return dispatchSource;
    }

    public void setDispatchSource(String dispatchSource) {
        this.dispatchSource = dispatchSource;
    }

    public String getDispatchOrderNo() {
        return dispatchOrderNo;
    }

    public void setDispatchOrderNo(String dispatchOrderNo) {
        this.dispatchOrderNo = dispatchOrderNo;
    }

    public String getProductCodeContent() {
        return productCodeContent;
    }

    public void setProductCodeContent(String productCodeContent) {
        this.productCodeContent = productCodeContent;
    }

    public String getDispatchTitle() {
        return dispatchTitle;
    }

    public void setDispatchTitle(String dispatchTitle) {
        this.dispatchTitle = dispatchTitle;
    }

    public String getPubDateName() {
        return pubDateName;
    }

    public void setPubDateName(String pubDateName) {
        this.pubDateName = pubDateName;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getStaffTel() {
        return staffTel;
    }

    public void setStaffTel(String staffTel) {
        this.staffTel = staffTel;
    }

    public String getSendDate() {
        return sendDate;
    }

    public void setSendDate(String sendDate) {
        this.sendDate = sendDate;
    }

    public String getDispatchSendOrg() {
        return dispatchSendOrg;
    }

    public void setDispatchSendOrg(String dispatchSendOrg) {
        this.dispatchSendOrg = dispatchSendOrg;
    }

    public String getStaffOrg() {
        return staffOrg;
    }

    public void setStaffOrg(String staffOrg) {
        this.staffOrg = staffOrg;
    }

    public String getReqFinDate() {
        return reqFinDate;
    }

    public void setReqFinDate(String reqFinDate) {
        this.reqFinDate = reqFinDate;
    }

    public String getCustNameChinese() {
        return custNameChinese;
    }

    public void setCustNameChinese(String custNameChinese) {
        this.custNameChinese = custNameChinese;
    }

    public String getSubscribeId() {
        return subscribeId;
    }

    public void setSubscribeId(String subscribeId) {
        this.subscribeId = subscribeId;
    }

    public String getDispathUrgency() {
        return dispathUrgency;
    }

    public void setDispathUrgency(String dispathUrgency) {
        this.dispathUrgency = dispathUrgency;
    }

    public String getOrderTitle() {
        return orderTitle;
    }

    public void setOrderTitle(String orderTitle) {
        this.orderTitle = orderTitle;
    }

    public String getActiveType() {
        return activeType;
    }

    public void setActiveType(String activeType) {
        this.activeType = activeType;
    }

    public String getActiveTypeName() {
        return activeTypeName;
    }

    public void setActiveTypeName(String activeTypeName) {
        this.activeTypeName = activeTypeName;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCstOrdId() {
        return cstOrdId;
    }

    public void setCstOrdId(String cstOrdId) {
        this.cstOrdId = cstOrdId;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getSrvOrdIds() {
        return srvOrdIds;
    }

    public void setSrvOrdIds(String srvOrdIds) {
        this.srvOrdIds = srvOrdIds;
    }

    public int getCirCount() {
        return cirCount;
    }

    public void setCirCount(int cirCount) {
        this.cirCount = cirCount;
    }

    public String getOrderIds() {
        return orderIds;
    }

    public void setOrderIds(String orderIds) {
        this.orderIds = orderIds;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getResources() {
        return resources;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }

    public String getDispatchOrderId() {
        return dispatchOrderId;
    }

    public void setDispatchOrderId(String dispatchOrderId) {
        this.dispatchOrderId = dispatchOrderId;
    }

    public String getSerialNumbers() {
        return serialNumbers;
    }

    public void setSerialNumbers(String serialNumbers) {
        this.serialNumbers = serialNumbers;
    }



}
