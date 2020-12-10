package com.zres.project.localnet.portal.initApplOrderDetail.domain;

import java.io.Serializable;
import java.util.Date;


/**
 * 导入的电路信息
 *  author sunlb
 */
public class LocalCircuitInfoPo implements Serializable {
    private String circuitCode; //电路编号
    private String serviceId; //产品类型code
    private String serviceName; //产品类型名称
    private String  activeType; //电路要求
    private String  activeTypeName; //电路要求
    private String speed; //带宽code
    private String speedName; //带宽名称
    private String cirUse; //电路用途
    private String cirUseName; //电路用途
    private String count; //数量
    private String relayType; //中继类型
    private String relayTypeName; //中继类型
    private String cirLeaseRange; //电路租用范围
    private String cirLeaseRangeName; //电路租用范围
    private Date requFineTime; //全程要求完成时间
    private String requFineTimeStr; //全程要求完成时间
    private Date createTime; //创建时间
    private String createTimeStr; //创建时间
    private String a_belong_province; //A端归属省
    private String a_belong_city; //A端归属地市
    private String a_belong_county; //A端归属区县
    private String a_belong_region; //A端归属分公司
    private String a_belong_provinceName; //A端归属省
    private String a_belong_cityName; //A端归属地市
    private String a_belong_countyName; //A端归属区县
    private String a_belong_regionName; //A端归属分公司

    private String z_belong_province; //Z端归属省
    private String z_belong_city; //Z端归属地市
    private String z_belong_county; //Z端归属区县
    private String z_belong_region; //Z端归属分公司
    private String z_belong_provinceName; //Z端归属省
    private String z_belong_cityName; //Z端归属地市
    private String z_belong_countyName; //Z端归属区县
    private String z_belong_regionName; //Z端归属分公司

    private String a_interface_type; //A端-接口类型
    private String a_interface_type_name; //A端-接口类型
    private String a_room; //A端机房/放置点
    private String a_contact_man; //A端联系人
    private String a_contact_tel; //A端联系电话

    private String z_interface_type; //Z端-接口类型
    private String z_interface_type_name; //Z端-接口类型
    private String z_room; //Z端机房/放置点
    private String z_contact_man; //Z端联系人
    private String z_contact_tel; //Z端联系电话

    private String remark; //备注


    public String getCircuitCode() {
        return circuitCode;
    }

    public void setCircuitCode(String circuitCode) {
        this.circuitCode = circuitCode;
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

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getRelayType() {
        return relayType;
    }

    public void setRelayType(String relayType) {
        this.relayType = relayType;
    }

    public String getRelayTypeName() {
        return relayTypeName;
    }

    public void setRelayTypeName(String relayTypeName) {
        this.relayTypeName = relayTypeName;
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

    public Date getRequFineTime() {
        return requFineTime;
    }

    public void setRequFineTime(Date requFineTime) {
        this.requFineTime = requFineTime;
    }

    public String getRequFineTimeStr() {
        return requFineTimeStr;
    }

    public void setRequFineTimeStr(String requFineTimeStr) {
        this.requFineTimeStr = requFineTimeStr;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCreateTimeStr() {
        return createTimeStr;
    }

    public void setCreateTimeStr(String createTimeStr) {
        this.createTimeStr = createTimeStr;
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

    public String getA_interface_type() {
        return a_interface_type;
    }

    public void setA_interface_type(String a_interface_type) {
        this.a_interface_type = a_interface_type;
    }

    public String getA_interface_type_name() {
        return a_interface_type_name;
    }

    public void setA_interface_type_name(String a_interface_type_name) {
        this.a_interface_type_name = a_interface_type_name;
    }

    public String getA_room() {
        return a_room;
    }

    public void setA_room(String a_room) {
        this.a_room = a_room;
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

    public String getZ_interface_type() {
        return z_interface_type;
    }

    public void setZ_interface_type(String z_interface_type) {
        this.z_interface_type = z_interface_type;
    }

    public String getZ_interface_type_name() {
        return z_interface_type_name;
    }

    public void setZ_interface_type_name(String z_interface_type_name) {
        this.z_interface_type_name = z_interface_type_name;
    }

    public String getZ_room() {
        return z_room;
    }

    public void setZ_room(String z_room) {
        this.z_room = z_room;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
