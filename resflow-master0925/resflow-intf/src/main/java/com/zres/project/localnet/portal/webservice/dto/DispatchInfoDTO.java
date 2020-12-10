package com.zres.project.localnet.portal.webservice.dto;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * @Description:调单信息DTO
 * @Author:zhang.kaigang
 * @Date:2019/5/15 17:23
 * @Version:1.0
 */
public class DispatchInfoDTO {
    @FieldMeta(column = "DISPATCH_ORDER_NO", name = "调单编号/变更单编号")
    private String dispatchOrderNo;

    @FieldMeta(column = "STAFF_NAME", name = "调单拟稿人")
    private String staffName;

    @FieldMeta(column = "STAFF_TEL", name = "拟稿人电话")
    private String staffTel;

    @FieldMeta(column = "STAFF_ORG", name = "拟稿人部门")
    private String staffOrg;

    @FieldMeta(column = "ISSUER", name = "调单签发人")
    private String issuer;

    @FieldMeta(column = "SEND_DATE", name = "调单派发时间/变更单派发时间")
    private String sendDate;

    // 枚举值，原值和描述值分开
    @FieldMeta(column = "DISPATCH_TYPE")
    private String dispatchType;

    @FieldMeta(name = "调单类型", enumColumn = "dispatchType")
    private String dispatchTypeName;

    @FieldMeta(column = "DISPATCH_GRADE", name = "调单等级")
    private String dispatchGrade;

    @FieldMeta(column = "DISPATCH_URGENCY", name = "调单缓急")
    private String dispatchUrgency;

    @FieldMeta(column = "DISPATCH_TITLE", name = "调单标题")
    private String dispatchTitle;

    @FieldMeta(column = "DISPATCH_SEND_ORG", name = "发送单位")
    private String dispatchSendOrg;

    @FieldMeta(column = "DISPATCH_COPY_ORG", name = "抄送单位")
    private String dispatchCopyOrg;

    @FieldMeta(column = "DISPATCH_TEXT", name = "调单正文")
    private String dispatchText;

    @FieldMeta(column = "CHANGE_BEFORE_TEXT", name = "变更前内容")
    private String changeBeforeText;

    @FieldMeta(column = "CHANGE_AFTER_TEXT", name = "变更后内容")
    private String changeAfterText;

    // TODO 需要处理附件类DTO
    private List<AttachDTO> attachDTOList;

    public String getDispatchOrderNo() {
        return dispatchOrderNo;
    }

    public void setDispatchOrderNo(String dispatchOrderNo) {
        this.dispatchOrderNo = dispatchOrderNo;
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

    public String getStaffOrg() {
        return staffOrg;
    }

    public void setStaffOrg(String staffOrg) {
        this.staffOrg = staffOrg;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getSendDate() {
        return sendDate;
    }

    public void setSendDate(String sendDate) {
        this.sendDate = sendDate;
    }

    public String getDispatchType() {
        return dispatchType;
    }

    public void setDispatchType(String dispatchType) {
        this.dispatchType = dispatchType;
        // set方法里处理是为了将查询出来的dispatchType转成描述后赋值给dispatchTypeName变量
        this.dispatchTypeName = ExceptionFlowEnum.getName(dispatchType);
    }

    public String getDispatchTypeName() {
        // get方法里处理是为了将报文转换成对应DTO改变量有值
        if (StringUtils.isNotEmpty(dispatchType)) {
            this.dispatchTypeName = ExceptionFlowEnum.getName(dispatchType);
        }
        return dispatchTypeName;
    }

    public String getDispatchGrade() {
        return dispatchGrade;
    }

    public void setDispatchGrade(String dispatchGrade) {
        this.dispatchGrade = dispatchGrade;
    }

    public String getDispatchUrgency() {
        return dispatchUrgency;
    }

    public void setDispatchUrgency(String dispatchUrgency) {
        this.dispatchUrgency = dispatchUrgency;
    }

    public String getDispatchTitle() {
        return dispatchTitle;
    }

    public void setDispatchTitle(String dispatchTitle) {
        this.dispatchTitle = dispatchTitle;
    }

    public String getDispatchSendOrg() {
        return dispatchSendOrg;
    }

    public void setDispatchSendOrg(String dispatchSendOrg) {
        this.dispatchSendOrg = dispatchSendOrg;
    }

    public String getDispatchCopyOrg() {
        return dispatchCopyOrg;
    }

    public void setDispatchCopyOrg(String dispatchCopyOrg) {
        this.dispatchCopyOrg = dispatchCopyOrg;
    }

    public String getDispatchText() {
        return dispatchText;
    }

    public void setDispatchText(String dispatchText) {
        this.dispatchText = dispatchText;
    }

    public String getChangeBeforeText() {
        return changeBeforeText;
    }

    public void setChangeBeforeText(String changeBeforeText) {
        this.changeBeforeText = changeBeforeText;
    }

    public String getChangeAfterText() {
        return changeAfterText;
    }

    public void setChangeAfterText(String changeAfterText) {
        this.changeAfterText = changeAfterText;
    }

    public List<AttachDTO> getAttachDTOList() {
        return attachDTOList;
    }

    public void setAttachDTOList(List<AttachDTO> attachDTOList) {
        this.attachDTOList = attachDTOList;
    }
}
