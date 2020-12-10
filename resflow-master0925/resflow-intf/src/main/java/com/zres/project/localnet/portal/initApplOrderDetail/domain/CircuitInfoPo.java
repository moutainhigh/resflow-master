package com.zres.project.localnet.portal.initApplOrderDetail.domain;

import java.io.Serializable;
import java.util.Date;


/**
 * 导入的电路信息
 *  author sunlb
 */
public class CircuitInfoPo implements Serializable {

    private String tradeId; //业务订单号
    private String serviceId; //产品类型code
    private String serviceName; //产品类型名称
    private String speed; //带宽code
    private String speedName; //带宽名称
    private String count; //数量
    private String custApplIPV4Count; //客户申请IPv4地址数量
    private String iPv6; //IPv6前缀
    private String slaFlag; //SLA标识Code
    private String slaFlagName; //SLA标识名称
    private String slaServOpen; //SLA业务开通Code
    private String slaServOpenName; //SLA业务开通名称
    private String slaNetQuAss; //SLA网络质量保证Code
    private String slaNetQuAssName; //SLA网络质量保证名称
    private String slaSaleServ; //SLA售后服务Code
    private String slaSaleServName; //SLA售后服务名称
    private String proPriDeg; //工程缓急程度Code
    private String proPriDegName; //工程缓急程度名称
    private String cirLeaseRange; //电路租用范围Code
    private String cirLeaseRangeName; //电路租用范围名称
    private String cirUse; //电路用途Code
    private String cirUseName; //电路用途名称
    private String custManager; //客户经理
    private String custManaPhone; //客户经理联系电话
    private Date requFineTime; //全程要求完成时间
    private String requFineTimeStr; //全程要求完成时间
    private Date accepTime; //受理时间
    private String accepTimeStr; //受理时间
    private String remark; //备注

    private String a_belong_province; //A端归属省
    private String a_belong_city; //A端归属地市
    private String a_belong_county; //A端归属区县
    private String a_belong_region; //A端归属分公司

    private String a_belong_provinceName; //A端归属省
    private String a_belong_cityName; //A端归属地市
    private String a_belong_countyName; //A端归属区县
    private String a_belong_regionName; //A端归属分公司

    private String a_customer_name; //A端客户名称
    private String a_installed_add; //A端装机地址
    private String a_contact_man; //A端联系人
    private String a_contact_tel; //A端联系电话
    private String a_customer_manager; //A端客户经理
    private String a_customer_manager_tel; //A端客户经理联系方式
    private String a_interface_type; //A端接口类型Code
    private String a_interface_type_Name; //A端接口类型名称

    private String z_belong_province; //Z端归属省
    private String z_belong_city; //Z端归属地市
    private String z_belong_county; //Z端归属区县
    private String z_belong_region; //Z端归属分公司

    private String z_belong_provinceName; //Z端归属省
    private String z_belong_cityName; //Z端归属地市
    private String z_belong_countyName; //Z端归属区县
    private String z_belong_regionName; //Z端归属分公司

    private String z_customer_name; //Z端客户名称
    private String z_installed_add; //Z端安装地址
    private String z_contact_man; //Z端联系人
    private String z_contact_tel; //Z端联系电话
    private String z_customer_manager; //Z端客户经理
    private String z_customer_manager_tel; //Z端客户经理联系方式
    private String z_interface_type; //Z端接口类型
    private String z_interface_type_Name; //Z端接口类型名称

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getSpeedName() {
        return speedName;
    }

    public void setSpeedName(String speedName) {
        this.speedName = speedName;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getSlaFlag() {
        return slaFlag;
    }

    public void setSlaFlag(String slaFlag) {
        this.slaFlag = slaFlag;
    }

    public String getSlaFlagName() {
        return slaFlagName;
    }

    public void setSlaFlagName(String slaFlagName) {
        this.slaFlagName = slaFlagName;
    }

    public String getSlaServOpen() {
        return slaServOpen;
    }

    public void setSlaServOpen(String slaServOpen) {
        this.slaServOpen = slaServOpen;
    }

    public String getSlaServOpenName() {
        return slaServOpenName;
    }

    public void setSlaServOpenName(String slaServOpenName) {
        this.slaServOpenName = slaServOpenName;
    }

    public String getSlaNetQuAss() {
        return slaNetQuAss;
    }

    public void setSlaNetQuAss(String slaNetQuAss) {
        this.slaNetQuAss = slaNetQuAss;
    }

    public String getSlaNetQuAssName() {
        return slaNetQuAssName;
    }

    public void setSlaNetQuAssName(String slaNetQuAssName) {
        this.slaNetQuAssName = slaNetQuAssName;
    }

    public String getSlaSaleServ() {
        return slaSaleServ;
    }

    public void setSlaSaleServ(String slaSaleServ) {
        this.slaSaleServ = slaSaleServ;
    }

    public String getSlaSaleServName() {
        return slaSaleServName;
    }

    public void setSlaSaleServName(String slaSaleServName) {
        this.slaSaleServName = slaSaleServName;
    }

    public String getProPriDeg() {
        return proPriDeg;
    }

    public void setProPriDeg(String proPriDeg) {
        this.proPriDeg = proPriDeg;
    }

    public String getProPriDegName() {
        return proPriDegName;
    }

    public void setProPriDegName(String proPriDegName) {
        this.proPriDegName = proPriDegName;
    }

    public String getCirLeaseRange() {
        return cirLeaseRange;
    }

    public void setCirLeaseRange(String cirLeaseRange) {
        this.cirLeaseRange = cirLeaseRange;
    }

    public String getCirLeaseRangeName() {
        return cirLeaseRangeName;
    }

    public void setCirLeaseRangeName(String cirLeaseRangeName) {
        this.cirLeaseRangeName = cirLeaseRangeName;
    }

    public String getCirUse() {
        return cirUse;
    }

    public void setCirUse(String cirUse) {
        this.cirUse = cirUse;
    }

    public String getCirUseName() {
        return cirUseName;
    }

    public void setCirUseName(String cirUseName) {
        this.cirUseName = cirUseName;
    }

    public String getCustManager() {
        return custManager;
    }

    public void setCustManager(String custManager) {
        this.custManager = custManager;
    }

    public String getCustManaPhone() {
        return custManaPhone;
    }

    public void setCustManaPhone(String custManaPhone) {
        this.custManaPhone = custManaPhone;
    }

    public Date getRequFineTime() {
        return requFineTime;
    }

    public void setRequFineTime(Date requFineTime) {
        this.requFineTime = requFineTime;
    }

    public Date getAccepTime() {
        return accepTime;
    }

    public void setAccepTime(Date accepTime) {
        this.accepTime = accepTime;
    }

    public String getA_belong_province() {
        return a_belong_province;
    }

    public void setA_belong_province(String a_belong_province) {
        this.a_belong_province = a_belong_province;
    }

    public String getA_belong_city() {
        return a_belong_city;
    }

    public void setA_belong_city(String a_belong_city) {
        this.a_belong_city = a_belong_city;
    }

    public String getA_belong_county() {
        return a_belong_county;
    }

    public void setA_belong_county(String a_belong_county) {
        this.a_belong_county = a_belong_county;
    }

    public String getA_belong_region() {
        return a_belong_region;
    }

    public void setA_belong_region(String a_belong_region) {
        this.a_belong_region = a_belong_region;
    }

    public String getA_customer_name() {
        return a_customer_name;
    }

    public void setA_customer_name(String a_customer_name) {
        this.a_customer_name = a_customer_name;
    }

    public String getA_installed_add() {
        return a_installed_add;
    }

    public void setA_installed_add(String a_installed_add) {
        this.a_installed_add = a_installed_add;
    }

    public String getA_contact_man() {
        return a_contact_man;
    }

    public void setA_contact_man(String a_contact_man) {
        this.a_contact_man = a_contact_man;
    }

    public String getA_contact_tel() {
        return a_contact_tel;
    }

    public void setA_contact_tel(String a_contact_tel) {
        this.a_contact_tel = a_contact_tel;
    }

    public String getA_customer_manager() {
        return a_customer_manager;
    }

    public void setA_customer_manager(String a_customer_manager) {
        this.a_customer_manager = a_customer_manager;
    }

    public String getA_customer_manager_tel() {
        return a_customer_manager_tel;
    }

    public void setA_customer_manager_tel(String a_customer_manager_tel) {
        this.a_customer_manager_tel = a_customer_manager_tel;
    }

    public String getA_interface_type() {
        return a_interface_type;
    }

    public void setA_interface_type(String a_interface_type) {
        this.a_interface_type = a_interface_type;
    }

    public String getA_interface_type_Name() {
        return a_interface_type_Name;
    }

    public void setA_interface_type_Name(String a_interface_type_Name) {
        this.a_interface_type_Name = a_interface_type_Name;
    }

    public String getZ_belong_province() {
        return z_belong_province;
    }

    public void setZ_belong_province(String z_belong_province) {
        this.z_belong_province = z_belong_province;
    }

    public String getZ_belong_city() {
        return z_belong_city;
    }

    public void setZ_belong_city(String z_belong_city) {
        this.z_belong_city = z_belong_city;
    }

    public String getZ_belong_county() {
        return z_belong_county;
    }

    public void setZ_belong_county(String z_belong_county) {
        this.z_belong_county = z_belong_county;
    }

    public String getZ_belong_region() {
        return z_belong_region;
    }

    public void setZ_belong_region(String z_belong_region) {
        this.z_belong_region = z_belong_region;
    }

    public String getZ_customer_name() {
        return z_customer_name;
    }

    public void setZ_customer_name(String z_customer_name) {
        this.z_customer_name = z_customer_name;
    }

    public String getZ_installed_add() {
        return z_installed_add;
    }

    public void setZ_installed_add(String z_installed_add) {
        this.z_installed_add = z_installed_add;
    }

    public String getZ_contact_man() {
        return z_contact_man;
    }

    public void setZ_contact_man(String z_contact_man) {
        this.z_contact_man = z_contact_man;
    }

    public String getZ_contact_tel() {
        return z_contact_tel;
    }

    public void setZ_contact_tel(String z_contact_tel) {
        this.z_contact_tel = z_contact_tel;
    }

    public String getZ_customer_manager() {
        return z_customer_manager;
    }

    public void setZ_customer_manager(String z_customer_manager) {
        this.z_customer_manager = z_customer_manager;
    }

    public String getZ_customer_manager_tel() {
        return z_customer_manager_tel;
    }

    public void setZ_customer_manager_tel(String z_customer_manager_tel) {
        this.z_customer_manager_tel = z_customer_manager_tel;
    }

    public String getZ_interface_type() {
        return z_interface_type;
    }

    public void setZ_interface_type(String z_interface_type) {
        this.z_interface_type = z_interface_type;
    }

    public String getZ_interface_type_Name() {
        return z_interface_type_Name;
    }

    public void setZ_interface_type_Name(String z_interface_type_Name) {
        this.z_interface_type_Name = z_interface_type_Name;
    }

    public String getCustApplIPV4Count() {
        return custApplIPV4Count;
    }

    public void setCustApplIPV4Count(String custApplIPV4Count) {
        this.custApplIPV4Count = custApplIPV4Count;
    }

    public String getiPv6() {
        return iPv6;
    }

    public void setiPv6(String iPv6) {
        this.iPv6 = iPv6;
    }

    public String getA_belong_provinceName() {
        return a_belong_provinceName;
    }

    public void setA_belong_provinceName(String a_belong_provinceName) {
        this.a_belong_provinceName = a_belong_provinceName;
    }

    public String getA_belong_cityName() {
        return a_belong_cityName;
    }

    public void setA_belong_cityName(String a_belong_cityName) {
        this.a_belong_cityName = a_belong_cityName;
    }

    public String getA_belong_countyName() {
        return a_belong_countyName;
    }

    public void setA_belong_countyName(String a_belong_countyName) {
        this.a_belong_countyName = a_belong_countyName;
    }

    public String getA_belong_regionName() {
        return a_belong_regionName;
    }

    public void setA_belong_regionName(String a_belong_regionName) {
        this.a_belong_regionName = a_belong_regionName;
    }

    public String getZ_belong_provinceName() {
        return z_belong_provinceName;
    }

    public void setZ_belong_provinceName(String z_belong_provinceName) {
        this.z_belong_provinceName = z_belong_provinceName;
    }

    public String getZ_belong_cityName() {
        return z_belong_cityName;
    }

    public void setZ_belong_cityName(String z_belong_cityName) {
        this.z_belong_cityName = z_belong_cityName;
    }

    public String getZ_belong_countyName() {
        return z_belong_countyName;
    }

    public void setZ_belong_countyName(String z_belong_countyName) {
        this.z_belong_countyName = z_belong_countyName;
    }

    public String getZ_belong_regionName() {
        return z_belong_regionName;
    }

    public void setZ_belong_regionName(String z_belong_regionName) {
        this.z_belong_regionName = z_belong_regionName;
    }

    public String getRequFineTimeStr() {
        return requFineTimeStr;
    }

    public void setRequFineTimeStr(String requFineTimeStr) {
        this.requFineTimeStr = requFineTimeStr;
    }

    public String getAccepTimeStr() {
        return accepTimeStr;
    }

    public void setAccepTimeStr(String accepTimeStr) {
        this.accepTimeStr = accepTimeStr;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

}
