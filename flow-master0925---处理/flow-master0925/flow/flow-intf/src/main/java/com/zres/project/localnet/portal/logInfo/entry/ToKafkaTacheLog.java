package com.zres.project.localnet.portal.logInfo.entry;

import java.util.Map;

public class ToKafkaTacheLog extends LoggerObj {

    private Map<String, String> cstOrderDataMap;

    //private String id; //主键
    private String sheet_id; //工单ID
    private String belong; //定单来源，受理渠道
    private String service_id; //定单业务类型ID
    private String order_title; //定单业务名称
    private String order_type; //O域订单类型（101开通单 102 核查单）
    private String group_type; //AZ端类型（A a端 Z z端 B(不区分az端)）
    private String active_type; //O域操作类型（101 新开 103 变更  102 拆机 104 停机 105 复机 106 移机）
    private String sys_resouce; //O域系统所属（1（一干）,2（二干）,0(本地)）
    private String cust_name; //客户名称
    private String contact_name; //负责人
    private String tache_service_id; //业务类型+环节id
    private String org_id; //处理部门ID
    private String org_name; //处理部门
    private String contact_nbr; //负责人联系电话
    private String tache_define_id; //工单环节id
    private String tache_name; //工单环节名称
    private String area_id; //分公司ID
    private String area_name; //分公司名称
    private String character_value; //需求编号
    private String accept_orgname; //受理部门
    private String order_code; //订单编码
    private String acc_nbr; //订单业务号码
    private String accept_date; //订单受理时间
    private String create_date; //环节创建时间
    private String finish_date; //环节结束时间
    private String work_order_state; //工单状态编码
    private String work_order_state_name; //工单状态说明
    private String moniter_work_time; //纯工作时间历时（分钟）
    private String uos_tache_limit; //环节时限（单位：天）
    private String outtime; //超时时间（单位：分钟）
    private String base_order_id; //订单ID
    private String order_state; //订单状态编码
    private String order_state_name; //订单状态名称
    private String pause_begin_date; //挂起开始时间
    private String pause_end_date; //挂起结束时间
    private String work_content; //短信反馈信息
    private String province_inf; //省份信息
    private String province_id; //省份id（B域编码）
    private String city_inf; //市级份信息
    private String city_id; //市id（B域编码）
    private String buss_inf; //业务相关
    private String Retain_1; //预留字段1
    private String Retain_2; //预留字段2
    private String Retain_3; //预留字段3
    private String domain; //1对应B域 2对应O域
    private String source_system; //来源系统
    private String source_system_remark; //来源系统描述

    public ToKafkaTacheLog() {
    }

    public ToKafkaTacheLog(String sheet_id, String belong, String service_id, String order_title, String order_type, String group_type, String active_type, String sys_resouce, String cust_name, String contact_name, String tache_service_id, String org_id, String org_name, String contact_nbr, String tache_define_id, String tache_name, String area_id, String area_name, String character_value, String accept_orgname, String order_code, String acc_nbr, String accept_date, String create_date, String finish_date, String work_order_state, String work_order_state_name, String moniter_work_time, String uos_tache_limit, String outtime, String base_order_id, String order_state, String order_state_name, String pause_begin_date, String pause_end_date, String work_content, String province_inf, String province_id, String city_inf, String city_id, String buss_inf, String retain_1, String retain_2, String retain_3, String domain, String source_system, String source_system_remark) {
        this.sheet_id = sheet_id;
        this.belong = belong;
        this.service_id = service_id;
        this.order_title = order_title;
        this.order_type = order_type;
        this.group_type = group_type;
        this.active_type = active_type;
        this.sys_resouce = sys_resouce;
        this.cust_name = cust_name;
        this.contact_name = contact_name;
        this.tache_service_id = tache_service_id;
        this.org_id = org_id;
        this.org_name = org_name;
        this.contact_nbr = contact_nbr;
        this.tache_define_id = tache_define_id;
        this.tache_name = tache_name;
        this.area_id = area_id;
        this.area_name = area_name;
        this.character_value = character_value;
        this.accept_orgname = accept_orgname;
        this.order_code = order_code;
        this.acc_nbr = acc_nbr;
        this.accept_date = accept_date;
        this.create_date = create_date;
        this.finish_date = finish_date;
        this.work_order_state = work_order_state;
        this.work_order_state_name = work_order_state_name;
        this.moniter_work_time = moniter_work_time;
        this.uos_tache_limit = uos_tache_limit;
        this.outtime = outtime;
        this.base_order_id = base_order_id;
        this.order_state = order_state;
        this.order_state_name = order_state_name;
        this.pause_begin_date = pause_begin_date;
        this.pause_end_date = pause_end_date;
        this.work_content = work_content;
        this.province_inf = province_inf;
        this.province_id = province_id;
        this.city_inf = city_inf;
        this.city_id = city_id;
        this.buss_inf = buss_inf;
        Retain_1 = retain_1;
        Retain_2 = retain_2;
        Retain_3 = retain_3;
        this.domain = domain;
        this.source_system = source_system;
        this.source_system_remark = source_system_remark;
    }

    public Map<String, String> getCstOrderDataMap() {
        return cstOrderDataMap;
    }

    public void setCstOrderDataMap(Map<String, String> cstOrderDataMap) {
        this.cstOrderDataMap = cstOrderDataMap;
    }

    public String getSheet_id() {
        return sheet_id;
    }

    public void setSheet_id(String sheet_id) {
        this.sheet_id = sheet_id;
    }

    public String getBelong() {
        return belong;
    }

    public void setBelong(String belong) {
        this.belong = belong;
    }

    public String getService_id() {
        return service_id;
    }

    public void setService_id(String service_id) {
        this.service_id = service_id;
    }

    public String getOrder_title() {
        return order_title;
    }

    public void setOrder_title(String order_title) {
        this.order_title = order_title;
    }

    public String getOrder_type() {
        return order_type;
    }

    public void setOrder_type(String order_type) {
        this.order_type = order_type;
    }

    public String getGroup_type() {
        return group_type;
    }

    public void setGroup_type(String group_type) {
        this.group_type = group_type;
    }

    public String getActive_type() {
        return active_type;
    }

    public void setActive_type(String active_type) {
        this.active_type = active_type;
    }

    public String getSys_resouce() {
        return sys_resouce;
    }

    public void setSys_resouce(String sys_resouce) {
        this.sys_resouce = sys_resouce;
    }

    public String getCust_name() {
        return cust_name;
    }

    public void setCust_name(String cust_name) {
        this.cust_name = cust_name;
    }

    public String getContact_name() {
        return contact_name;
    }

    public void setContact_name(String contact_name) {
        this.contact_name = contact_name;
    }

    public String getTache_service_id() {
        return tache_service_id;
    }

    public void setTache_service_id(String tache_service_id) {
        this.tache_service_id = tache_service_id;
    }

    public String getOrg_id() {
        return org_id;
    }

    public void setOrg_id(String org_id) {
        this.org_id = org_id;
    }

    public String getOrg_name() {
        return org_name;
    }

    public void setOrg_name(String org_name) {
        this.org_name = org_name;
    }

    public String getContact_nbr() {
        return contact_nbr;
    }

    public void setContact_nbr(String contact_nbr) {
        this.contact_nbr = contact_nbr;
    }

    public String getTache_define_id() {
        return tache_define_id;
    }

    public void setTache_define_id(String tache_define_id) {
        this.tache_define_id = tache_define_id;
    }

    public String getTache_name() {
        return tache_name;
    }

    public void setTache_name(String tache_name) {
        this.tache_name = tache_name;
    }

    public String getArea_id() {
        return area_id;
    }

    public void setArea_id(String area_id) {
        this.area_id = area_id;
    }

    public String getArea_name() {
        return area_name;
    }

    public void setArea_name(String area_name) {
        this.area_name = area_name;
    }

    public String getCharacter_value() {
        return character_value;
    }

    public void setCharacter_value(String character_value) {
        this.character_value = character_value;
    }

    public String getAccept_orgname() {
        return accept_orgname;
    }

    public void setAccept_orgname(String accept_orgname) {
        this.accept_orgname = accept_orgname;
    }

    public String getOrder_code() {
        return order_code;
    }

    public void setOrder_code(String order_code) {
        this.order_code = order_code;
    }

    public String getAcc_nbr() {
        return acc_nbr;
    }

    public void setAcc_nbr(String acc_nbr) {
        this.acc_nbr = acc_nbr;
    }

    public String getAccept_date() {
        return accept_date;
    }

    public void setAccept_date(String accept_date) {
        this.accept_date = accept_date;
    }

    public String getCreate_date() {
        return create_date;
    }

    public void setCreate_date(String create_date) {
        this.create_date = create_date;
    }

    public String getFinish_date() {
        return finish_date;
    }

    public void setFinish_date(String finish_date) {
        this.finish_date = finish_date;
    }

    public String getWork_order_state() {
        return work_order_state;
    }

    public void setWork_order_state(String work_order_state) {
        this.work_order_state = work_order_state;
    }

    public String getWork_order_state_name() {
        return work_order_state_name;
    }

    public void setWork_order_state_name(String work_order_state_name) {
        this.work_order_state_name = work_order_state_name;
    }

    public String getMoniter_work_time() {
        return moniter_work_time;
    }

    public void setMoniter_work_time(String moniter_work_time) {
        this.moniter_work_time = moniter_work_time;
    }

    public String getUos_tache_limit() {
        return uos_tache_limit;
    }

    public void setUos_tache_limit(String uos_tache_limit) {
        this.uos_tache_limit = uos_tache_limit;
    }

    public String getOuttime() {
        return outtime;
    }

    public void setOuttime(String outtime) {
        this.outtime = outtime;
    }

    public String getBase_order_id() {
        return base_order_id;
    }

    public void setBase_order_id(String base_order_id) {
        this.base_order_id = base_order_id;
    }

    public String getOrder_state() {
        return order_state;
    }

    public void setOrder_state(String order_state) {
        this.order_state = order_state;
    }

    public String getOrder_state_name() {
        return order_state_name;
    }

    public void setOrder_state_name(String order_state_name) {
        this.order_state_name = order_state_name;
    }

    public String getPause_begin_date() {
        return pause_begin_date;
    }

    public void setPause_begin_date(String pause_begin_date) {
        this.pause_begin_date = pause_begin_date;
    }

    public String getPause_end_date() {
        return pause_end_date;
    }

    public void setPause_end_date(String pause_end_date) {
        this.pause_end_date = pause_end_date;
    }

    public String getWork_content() {
        return work_content;
    }

    public void setWork_content(String work_content) {
        this.work_content = work_content;
    }

    public String getProvince_inf() {
        return province_inf;
    }

    public void setProvince_inf(String province_inf) {
        this.province_inf = province_inf;
    }

    public String getProvince_id() {
        return province_id;
    }

    public void setProvince_id(String province_id) {
        this.province_id = province_id;
    }

    public String getCity_inf() {
        return city_inf;
    }

    public void setCity_inf(String city_inf) {
        this.city_inf = city_inf;
    }

    public String getCity_id() {
        return city_id;
    }

    public void setCity_id(String city_id) {
        this.city_id = city_id;
    }

    public String getBuss_inf() {
        return buss_inf;
    }

    public void setBuss_inf(String buss_inf) {
        this.buss_inf = buss_inf;
    }

    public String getRetain_1() {
        return Retain_1;
    }

    public void setRetain_1(String retain_1) {
        Retain_1 = retain_1;
    }

    public String getRetain_2() {
        return Retain_2;
    }

    public void setRetain_2(String retain_2) {
        Retain_2 = retain_2;
    }

    public String getRetain_3() {
        return Retain_3;
    }

    public void setRetain_3(String retain_3) {
        Retain_3 = retain_3;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getSource_system() {
        return source_system;
    }

    public void setSource_system(String source_system) {
        this.source_system = source_system;
    }

    public String getSource_system_remark() {
        return source_system_remark;
    }

    public void setSource_system_remark(String source_system_remark) {
        this.source_system_remark = source_system_remark;
    }
}
