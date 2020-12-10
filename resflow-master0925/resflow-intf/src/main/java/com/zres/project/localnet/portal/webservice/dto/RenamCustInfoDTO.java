package com.zres.project.localnet.portal.webservice.dto;

/**
 * @ClassName RenamCustInfoDTO
 * @Description TODO   订单中心通过此接口完成客户、用户更名信息同步
 * @Author wang.g2
 * @Date 2020/8/4 10:12
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */

public class RenamCustInfoDTO {

    public String getCstOrdId() {
        return cstOrdId;
    }

        @FieldMeta(column = "CUST_ORDER_ID", name = "客户编号")
        private String cstOrdId;

        @FieldMeta(column = "CUST_ID", name = "客户编号")
        private String custId;

        @FieldMeta(column = "CUST_TYPE", name = "客户类型" ,value = "CustType")
        private String custType;

        @FieldMeta(column = "CUST_NAME_CHINESE", name = "客户中文名")
        private String custNameChinese;

        @FieldMeta(column = "CUST_NAME_ENGLISH", name = "客户英文名")
        private String custNameEnglish;

        @FieldMeta(column = "CUST_PROVINCE", name = "客户归属省份" ,value ="PROVINCE_CODE")
        private String custProvince;

        @FieldMeta(column = "CUST_CITY", name = "客户归属地市" ,value ="CITY_CODE")
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

        @FieldMeta(column = "CERTI_TYPE_CODE", name = "客户证件类型" ,value ="CERTI_TYPE_CODE")
        private String certiTypeCode;

        @FieldMeta(column = "CERTI_CODE", name = "证件编码")
        private String certiCode;

        @FieldMeta(column = "CUST_ADDRESS", name = "客户地址")
        private String custAddress;

        public String getCustId() {
            return custId;
        }

        public void setCustId(String custId) {
            this.custId = custId;
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

        public void setCertiTypeCode(String certiTypeCode) {
        this.certiTypeCode = certiTypeCode;
    }

        public String getCertiTypeCode() {
            return certiTypeCode;
        }

        public String getCustType() {
            return custType;
        }

        public void setCustType(String custType) {
            this.custType = custType;
        }

        public String getCertiCode() {
            return certiCode;
        }

        public void setCertiCode(String certiCode) {
            this.certiCode = certiCode;
        }

        public String getCustAddress() {
            return custAddress;
        }

        public void setCustAddress(String custAddress) {
            this.custAddress = custAddress;
        }

}
