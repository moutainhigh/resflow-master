package com.zres.project.localnet.portal.webservice.dto;

/**
 * @Description:TODO
 * @Author:zhang.kaigang
 * @Date:2019/6/29
 * @Version:1.0
 */
public class JiKeCustomInfoDTO {

    @FieldMeta(column = "PROJECT_TYPE", name = "项目类型")
    private String projectType;

    @FieldMeta(column = "NETWORK_LEVEL", name = "网络层次")
    private String networkLevel;

    @FieldMeta(column = "DEAL_AREA_CODE", name = "业务处理区域")
    private String dealAreaCode;

    @FieldMeta(column = "GROUP_PM_ID", name = "集团项目经理ID")
    private String groupPmId;

    @FieldMeta(column = "GROUP_PM_NAME", name = "集团项目经理名称")
    private String groupPmName;

    @FieldMeta(column = "GROUP_PM_TEL", name = "集团项目经理电话")
    private String groupPmTel;

    @FieldMeta(column = "GROUP_PM_EMAIL", name = "集团项目经理Email")
    private String groupPmEmail;

    @FieldMeta(column = "PROVINCE_PM_ID", name = "省项目经理ID")
    private String provincePmId;

    @FieldMeta(column = "PROVINCE_PM_NAME", name = "省项目经理名称")
    private String provincePmName;

    @FieldMeta(column = "PROVINCE_PM_TEL", name = "省项目经理电话")
    private String provincePmTel;

    @FieldMeta(column = "PROVINCE_PM_EMAIL", name = "省项目经理Email")
    private String provincePmEmail;

    @FieldMeta(column = "INIT_AM_ID", name = "发起方客户经理ID")
    private String initAmId;

    @FieldMeta(column = "INIT_AM_NAME", name = "发起方客户经理名称")
    private String initAmName;

    @FieldMeta(column = "INIT_AM_TEL", name = "发起方客户经理电话")
    private String initAmTel;

    @FieldMeta(column = "INIT_AM_EMAIL", name = "发起方客户经理Email")
    private String initAmEmail;

    //@FieldMeta(column = "HANDLE_TIME", name = "受理时间")
    private String acceptDate;

    @FieldMeta(column = "HANDLE_CITY_ID", name = "受理地市编码")
    private String tradeEparchyCode;

    @FieldMeta(column = "HANDLE_DEP", name = "受理部门")
    private String departName;

    @FieldMeta(column = "HANDLE_MAN_NAME", name = "受理人名称")
    private String tradeStaffName;

    @FieldMeta(column = "HANDLE_MAN_TEL", name = "受理人联系电话")
    private String tradeStaffPhone;

    @FieldMeta(column = "REMARK", name = "备注")
    private String remark;

    @FieldMeta(column = "CUST_ID", name = "客户编号")
    private String custId;

    @FieldMeta(column = "CONTRACT_ID", name = "客户合同号")
    private String contractId;

    @FieldMeta(column = "CONTRACT_NAME", name = "合同名称")
    private String contractName;

    @FieldMeta(column = "CUST_NAME_CHINESE", name = "客户中文名")
    private String custNameChinese;

    @FieldMeta(column = "CUST_NAME_ENGLISH", name = "客户英文名")
    private String custNameEnglish;

    @FieldMeta(column = "CUST_ADDRESS", name = "客户地址")
    private String custAddress;

    @FieldMeta(column = "CUST_INDUSTRY", name = "客户行业")
    private String custIndustry;

    @FieldMeta(column = "CUST_TYPE", name = "客户类型")
    private String custType;

    @FieldMeta(column = "CUST_PROVINCE", name = "客户归属省分")
    private String custProvince;

    @FieldMeta(column = "CUST_CITY", name = "客户归属地市")
    private String custCity;

    @FieldMeta(column = "CUST_TEL", name = "客户联系电话")
    private String custTel;

    @FieldMeta(column = "CUST_FAX", name = "客户联系传真")
    private String custFax;

    @FieldMeta(column = "CUST_EMAIL", name = "客户联系Email")
    private String custEmail;

    @FieldMeta(column = "CUST_CONTACT_MAN_ID", name = "客户联系人ID")
    private String custContactManId;

    @FieldMeta(column = "CUST_CONTACT_MAN_NAME", name = "客户联系人名称")
    private String custContactManName;

    @FieldMeta(column = "CUST_CONTACT_MAN_TEL", name = "客户联系人电话")
    private String custContactManTel;

    @FieldMeta(column = "CUST_CONTACT_MAN_EMAIL", name = "客户联系人Email")
    private String custContactManEmail;

    @FieldMeta(column = "CUST_OPERATOR_NAME", name = "经办人名称")
    private String custOperatorName;

    @FieldMeta(column = "CUST_OPERATOR_TEL", name = "经办人电话")
    private String custOperatorTel;

    @FieldMeta(column = "CUST_OPERATOR_EMAIL", name = "经办人Email")
    private String custOperatorEmail;


    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public String getNetworkLevel() {
        return networkLevel;
    }

    public void setNetworkLevel(String networkLevel) {
        this.networkLevel = networkLevel;
    }

    public String getDealAreaCode() {
        return dealAreaCode;
    }

    public void setDealAreaCode(String dealAreaCode) {
        this.dealAreaCode = dealAreaCode;
    }

    public String getGroupPmId() {
        return groupPmId;
    }

    public void setGroupPmId(String groupPmId) {
        this.groupPmId = groupPmId;
    }

    public String getGroupPmName() {
        return groupPmName;
    }

    public void setGroupPmName(String groupPmName) {
        this.groupPmName = groupPmName;
    }

    public String getGroupPmTel() {
        return groupPmTel;
    }

    public void setGroupPmTel(String groupPmTel) {
        this.groupPmTel = groupPmTel;
    }

    public String getGroupPmEmail() {
        return groupPmEmail;
    }

    public void setGroupPmEmail(String groupPmEmail) {
        this.groupPmEmail = groupPmEmail;
    }

    public String getProvincePmId() {
        return provincePmId;
    }

    public void setProvincePmId(String provincePmId) {
        this.provincePmId = provincePmId;
    }

    public String getProvincePmName() {
        return provincePmName;
    }

    public void setProvincePmName(String provincePmName) {
        this.provincePmName = provincePmName;
    }

    public String getProvincePmTel() {
        return provincePmTel;
    }

    public void setProvincePmTel(String provincePmTel) {
        this.provincePmTel = provincePmTel;
    }

    public String getProvincePmEmail() {
        return provincePmEmail;
    }

    public void setProvincePmEmail(String provincePmEmail) {
        this.provincePmEmail = provincePmEmail;
    }

    public String getInitAmId() {
        return initAmId;
    }

    public void setInitAmId(String initAmId) {
        this.initAmId = initAmId;
    }

    public String getInitAmName() {
        return initAmName;
    }

    public void setInitAmName(String initAmName) {
        this.initAmName = initAmName;
    }

    public String getInitAmTel() {
        return initAmTel;
    }

    public void setInitAmTel(String initAmTel) {
        this.initAmTel = initAmTel;
    }

    public String getInitAmEmail() {
        return initAmEmail;
    }

    public void setInitAmEmail(String initAmEmail) {
        this.initAmEmail = initAmEmail;
    }

    public String getAcceptDate() {
        return acceptDate;
    }

    public void setAcceptDate(String acceptDate) {
        this.acceptDate = acceptDate;
    }

    public String getTradeEparchyCode() {
        return tradeEparchyCode;
    }

    public void setTradeEparchyCode(String tradeEparchyCode) {
        this.tradeEparchyCode = tradeEparchyCode;
    }

    public String getDepartName() {
        return departName;
    }

    public void setDepartName(String departName) {
        this.departName = departName;
    }

    public String getTradeStaffName() {
        return tradeStaffName;
    }

    public void setTradeStaffName(String tradeStaffName) {
        this.tradeStaffName = tradeStaffName;
    }

    public String getTradeStaffPhone() {
        return tradeStaffPhone;
    }

    public void setTradeStaffPhone(String tradeStaffPhone) {
        this.tradeStaffPhone = tradeStaffPhone;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public String getContractName() {
        return contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
    }

    public String getCustNameChinese() {
        return custNameChinese;
    }

    public void setCustNameChinese(String custNameChinese) {
        this.custNameChinese = custNameChinese;
    }

    public String getCustNameEnglish() {
        return custNameEnglish;
    }

    public void setCustNameEnglish(String custNameEnglish) {
        this.custNameEnglish = custNameEnglish;
    }

    public String getCustAddress() {
        return custAddress;
    }

    public void setCustAddress(String custAddress) {
        this.custAddress = custAddress;
    }

    public String getCustIndustry() {
        return custIndustry;
    }

    public void setCustIndustry(String custIndustry) {
        this.custIndustry = custIndustry;
    }

    public String getCustType() {
        return custType;
    }

    public void setCustType(String custType) {
        this.custType = custType;
    }

    public String getCustProvince() {
        return custProvince;
    }

    public void setCustProvince(String custProvince) {
        this.custProvince = custProvince;
    }

    public String getCustCity() {
        return custCity;
    }

    public void setCustCity(String custCity) {
        this.custCity = custCity;
    }

    public String getCustTel() {
        return custTel;
    }

    public void setCustTel(String custTel) {
        this.custTel = custTel;
    }

    public String getCustFax() {
        return custFax;
    }

    public void setCustFax(String custFax) {
        this.custFax = custFax;
    }

    public String getCustEmail() {
        return custEmail;
    }

    public void setCustEmail(String custEmail) {
        this.custEmail = custEmail;
    }

    public String getCustContactManId() {
        return custContactManId;
    }

    public void setCustContactManId(String custContactManId) {
        this.custContactManId = custContactManId;
    }

    public String getCustContactManName() {
        return custContactManName;
    }

    public void setCustContactManName(String custContactManName) {
        this.custContactManName = custContactManName;
    }

    public String getCustContactManTel() {
        return custContactManTel;
    }

    public void setCustContactManTel(String custContactManTel) {
        this.custContactManTel = custContactManTel;
    }

    public String getCustContactManEmail() {
        return custContactManEmail;
    }

    public void setCustContactManEmail(String custContactManEmail) {
        this.custContactManEmail = custContactManEmail;
    }

    public String getCustOperatorName() {
        return custOperatorName;
    }

    public void setCustOperatorName(String custOperatorName) {
        this.custOperatorName = custOperatorName;
    }

    public String getCustOperatorTel() {
        return custOperatorTel;
    }

    public void setCustOperatorTel(String custOperatorTel) {
        this.custOperatorTel = custOperatorTel;
    }

    public String getCustOperatorEmail() {
        return custOperatorEmail;
    }

    public void setCustOperatorEmail(String custOperatorEmail) {
        this.custOperatorEmail = custOperatorEmail;
    }
}
