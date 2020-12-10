package com.zres.project.localnet.portal.webservice.dto;

import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * @Description:产品信息DTO
 * @Author:zhang.kaigang
 * @Date:2019/5/15 18:00
 * @Version:1.0
 */
public class ProdInfoDTO {

//    @FieldMeta(column = "SERVICE_ID")
//    private String prodType;

//    @FieldMeta(name = "产品名称")
//    private String prodName;

 /*   @FieldMeta(column = "INSTANCE_ID", name = "产品实例")
    private String prodInstId;*/

/*    @FieldMeta(column = "QCWOORDERCODE", name = "工单编码")
    private String woOrderCode;

    @FieldMeta(column = "OMORDERCODE", name = "一干系统内部定单编码")*/
    private String omOrderCode;

    // 枚举值，原值和描述值分开
    @FieldMeta(column = "ACTIVE_TYPE")
    private String actType;

    @FieldMeta(name = "业务动作类型", enumColumn = "actType")
    private String actTypeName;

    private String onedryAreaCode;

    private String tradeTypeCode;

    public String getTradeTypeCode() {
        return tradeTypeCode;
    }

    public void setTradeTypeCode(String tradeTypeCode) {
        this.tradeTypeCode = tradeTypeCode;
    }

    public String getOnedryAreaCode() {
        return onedryAreaCode;
    }

    public void setOnedryAreaCode(String onedryAreaCode) {
        this.onedryAreaCode = onedryAreaCode;
    }

    // TODO 附件信息DTO
    List<AttachDTO> attachDTOList;

    List<ProdAttrDTO> prodAttrDTOList;

//    public String getProdType() {
//        return prodType;
//    }

//    public void setProdType(String prodType) {
//        this.prodType = prodType;
//    }

//    public String getProdName() {
//        return prodName;
//    }
//
//    public void setProdName(String prodName) {
//        this.prodName = prodName;
//    }

   /* public String getProdInstId() {
        return prodInstId;
    }

    public void setProdInstId(String prodInstId) {
        this.prodInstId = prodInstId;
    }*/

 /*   public String getWoOrderCode() {
        return woOrderCode;
    }

    public void setWoOrderCode(String woOrderCode) {
        this.woOrderCode = woOrderCode;
    }

    public String getOmOrderCode() {
        return omOrderCode;
    }

    public void setOmOrderCode(String omOrderCode) {
        this.omOrderCode = omOrderCode;
    }

*/
    public String getActType() {
        return actType;
    }

    public void setActType(String actType) {
        this.actType = actType;
        // set方法里处理是为了将查询出来的dispatchType转成描述后赋值给该变量
        this.actTypeName = ExceptionFlowEnum.getName(actType);
    }

    public String getActTypeName() {
        // get方法里处理是为了将报文转换成对应DTO改变量有值
        if (StringUtils.isNotEmpty(actType)) {
            this.actTypeName = ExceptionFlowEnum.getName(actType);
        }
        return actTypeName;
    }

    public List<AttachDTO> getAttachDTOList() {
        return attachDTOList;
    }

    public void setAttachDTOList(List<AttachDTO> attachDTOList) {
        this.attachDTOList = attachDTOList;
    }

    public List<ProdAttrDTO> getProdAttrDTOList() {
        return prodAttrDTOList;
    }

    public void setProdAttrDTOList(List<ProdAttrDTO> prodAttrDTOList) {
        this.prodAttrDTOList = prodAttrDTOList;
    }

    //---------
    private String cstOrdId;

    private String srvOrdId;

    private String orderId;

    private String dispatchOrderId;

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

    public String getDispatchOrderId() {
        return dispatchOrderId;
    }

    public void setDispatchOrderId(String dispatchOrderId) {
        this.dispatchOrderId = dispatchOrderId;
    }

    public String getChgOrderId() {
        return chgOrderId;
    }

    public void setChgOrderId(String chgOrderId) {
        this.chgOrderId = chgOrderId;
    }

    private String orderType;

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

}
