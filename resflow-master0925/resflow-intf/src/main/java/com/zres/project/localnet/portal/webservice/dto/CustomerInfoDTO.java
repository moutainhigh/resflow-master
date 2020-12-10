package com.zres.project.localnet.portal.webservice.dto;

/**
 * @Description:一干客户信息DTO
 * @Author:zhang.kaigang
 * @Date:2019/5/15 17:11
 * @Version:1.0
 */
public class CustomerInfoDTO {

    @FieldMeta(column = "CUST_NAME_CHINESE", name = "客户中文名")
    private String cnName;

    @FieldMeta(column = "CUST_NAME_ENGLISH", name = "客户英文名")
    private String enName;

    @FieldMeta(column = "CUST_ID", name = "客户编码")
    private String code;

    // TODO 需要处理枚举
    @FieldMeta(column = "SERVICELEVEL", name = "客户服务等级")
    private String serviceLevel;

    @FieldMeta(column = "CUST_ADDRESS", name = "客户地址")
    private String address;

    @FieldMeta(column = "CUST_EMAIL", name = "客户email")
    private String email;

    @FieldMeta(column = "CUST_TEL", name = "客户电话")
    private String phone;

    @FieldMeta(column = "CUST_CONTACT_MAN_NAME", name = "客户联系人")
    private String contact;

    @FieldMeta(column = "CUST_INDUSTRY", name = "客户行业")
    private String industry;

    @FieldMeta(column = "UPPERCODE", name = "上级客户编码")
    private String upperCode;


    @FieldMeta(column = "UPPERNAME", name = "上级客户名称")
    private String upperName;

    public String getCnName() {
        return cnName;
    }

    public void setCnName(String cnName) {
        this.cnName = cnName;
    }

    public String getEnName() {
        return enName;
    }

    public void setEnName(String enName) {
        this.enName = enName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getServiceLevel() {
        return serviceLevel;
    }

    public void setServiceLevel(String serviceLevel) {
        this.serviceLevel = serviceLevel;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getIndustry() {
        return industry;
    }


    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getUpperCode() {
        return upperCode;
    }

    public void setUpperCode(String upperCode) {
        this.upperCode = upperCode;
    }

    public String getUpperName() {
        return upperName;
    }

    public void setUpperName(String upperName) {
        this.upperName = upperName;
    }


    // ------------------
    private String cstOrdId;

    public String getCstOrdId() {
        return cstOrdId;
    }

    public void setCstOrdId(String cstOrdId) {
        this.cstOrdId = cstOrdId;
    }
}
