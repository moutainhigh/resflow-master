package com.zres.project.localnet.portal.local.domain;

import java.io.Serializable;

public class UnicomLocalOrderPo implements Serializable {

    private static final long serialVersionUID = -3983439036982875286L;

    private String srvOrdId; //业务订单ID
    private String srvordIds; //流程订单Id(集合)
    private int circodeCount; //电路数量
    private String orderId; //流程订单Id
    private String cstOrdId;//客户Id
    private String serialNumber;//业务号码
    private String tradeId;//业务订单编号
    private String orderCode; //流程定单编码
    private String applyOrdId;//申请单编号
    private String applyOrdName; //申请单标题
    private String dianlNo; //电路代号
    private String custName; //客户名称
    private String prodBustType; //
    private String itemType; //单据类型
    private String prodBustTypeName; //产品类型名称
    private String actCode; //动作类型
    private String actCodeName; //动作类型名称
    private String actTypeName; //流程类型
    private String actTypeState; //流程状态
    private String dispObjName; //当前执行人
    private String createDateStr;//申请时间
    private String omlParentOrderId;
    private String omlOrderId;
    private String num;

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getApplyOrdId() {
        return applyOrdId;
    }

    public void setApplyOrdId(String applyOrdId) {
        this.applyOrdId = applyOrdId;
    }

    public String getApplyOrdName() {
        return applyOrdName;
    }

    public void setApplyOrdName(String applyOrdName) {
        this.applyOrdName = applyOrdName;
    }

    public String getDianlNo() {
        return dianlNo;
    }

    public void setDianlNo(String dianlNo) {
        this.dianlNo = dianlNo;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public String getProdBustType() {
        return prodBustType;
    }

    public void setProdBustType(String prodBustType) {
        this.prodBustType = prodBustType;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getActCodeName() {
        return actCodeName;
    }

    public void setActCodeName(String actCodeName) {
        this.actCodeName = actCodeName;
    }

    public String getActTypeName() {
        return actTypeName;
    }

    public void setActTypeName(String actTypeName) {
        this.actTypeName = actTypeName;
    }

    public String getActTypeState() {
        return actTypeState;
    }

    public void setActTypeState(String actTypeState) {
        this.actTypeState = actTypeState;
    }

    public String getDispObjName() {
        return dispObjName;
    }

    public void setDispObjName(String dispObjName) {
        this.dispObjName = dispObjName;
    }

    public String getOmlParentOrderId() {
        return omlParentOrderId;
    }

    public void setOmlParentOrderId(String omlParentOrderId) {
        this.omlParentOrderId = omlParentOrderId;
    }

    public String getOmlOrderId() {
        return omlOrderId;
    }

    public void setOmlOrderId(String omlOrderId) {
        this.omlOrderId = omlOrderId;
    }

    public String getProdBustTypeName() {
        return prodBustTypeName;
    }

    public void setProdBustTypeName(String prodBustTypeName) {
        this.prodBustTypeName = prodBustTypeName;
    }

    public String getSrvOrdId() {
        return srvOrdId;
    }

    public void setSrvOrdId(String srvOrdId) {
        this.srvOrdId = srvOrdId;
    }

    public String getSrvordIds() {
        return srvordIds;
    }

    public void setSrvordIds(String srvordIds) {
        this.srvordIds = srvordIds;
    }

    public int getCircodeCount() {
        return circodeCount;
    }

    public void setCircodeCount(int circodeCount) {
        this.circodeCount = circodeCount;
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

    public String getCstOrdId() {
        return cstOrdId;
    }

    public void setCstOrdId(String cstOrdId) {
        this.cstOrdId = cstOrdId;
    }

    public String getActCode() {
        return actCode;
    }

    public void setActCode(String actCode) {
        this.actCode = actCode;
    }

    public String getCreateDateStr() {
        return createDateStr;
    }

    public void setCreateDateStr(String createDateStr) {
        this.createDateStr = createDateStr;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
}
