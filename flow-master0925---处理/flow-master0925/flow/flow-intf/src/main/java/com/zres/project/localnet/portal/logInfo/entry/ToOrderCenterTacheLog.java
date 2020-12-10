package com.zres.project.localnet.portal.logInfo.entry;

public class ToOrderCenterTacheLog extends LoggerObj {

    private String wo_id; //工单ID
    private String cust_order_no; //大客户流水号/订单编号
    private String serial_number; //电路明细编号/业务号码
    private String actice_type; //业务动作：核查、新开、变更
    private String prod_type; //产品类型
    private String tache_name; //环节名称
    private String tache_code; //环节code
    private String province_id; //省份id
    private String province_name; //省份编码
    private String area_id; //区域id
    private String area_name; //区域名称
    private String dept; //部门名称
    private String role; //角色
    private String deal_person; //处理人
    private String phone; //处理人电话号码
    private String opera_context; //处理内容
    private String opera_time; //处理时间
    private String complate_time; //完成时间
    private String data_sources; //数据源：JIKE:集中集客；YZS：一站式系统
    private String sys_sources; //系统源：oneDry:一干调度；twoDry:二干调度；Local:本地调度
    private String is_timeout; //是否超时 0:是；1：否
    private String wo_state; //工单状态：290000002：处理中；290000004：已完成
    private String flag; //对端标识：A：A端/PE端；Z：Z端/CE端
    private String order_type; //订单类型：开通：101；核查：102；

    public ToOrderCenterTacheLog() {
    }

    public ToOrderCenterTacheLog(String wo_id, String cust_order_no, String serial_number, String actice_type, String prod_type, String tache_name, String tache_code, String province_id, String province_name, String area_id, String area_name, String dept, String role, String deal_person, String phone, String opera_context, String opera_time, String complate_time, String data_sources, String sys_sources, String is_timeout, String wo_state, String flag, String order_type) {
        this.wo_id = wo_id;
        this.cust_order_no = cust_order_no;
        this.serial_number = serial_number;
        this.actice_type = actice_type;
        this.prod_type = prod_type;
        this.tache_name = tache_name;
        this.tache_code = tache_code;
        this.province_id = province_id;
        this.province_name = province_name;
        this.area_id = area_id;
        this.area_name = area_name;
        this.dept = dept;
        this.role = role;
        this.deal_person = deal_person;
        this.phone = phone;
        this.opera_context = opera_context;
        this.opera_time = opera_time;
        this.complate_time = complate_time;
        this.data_sources = data_sources;
        this.sys_sources = sys_sources;
        this.is_timeout = is_timeout;
        this.wo_state = wo_state;
        this.flag = flag;
        this.order_type = order_type;
    }

    public String getWo_id() {
        return wo_id;
    }

    public void setWo_id(String wo_id) {
        this.wo_id = wo_id;
    }

    public String getCust_order_no() {
        return cust_order_no;
    }

    public void setCust_order_no(String cust_order_no) {
        this.cust_order_no = cust_order_no;
    }

    public String getSerial_number() {
        return serial_number;
    }

    public void setSerial_number(String serial_number) {
        this.serial_number = serial_number;
    }

    public String getActice_type() {
        return actice_type;
    }

    public void setActice_type(String actice_type) {
        this.actice_type = actice_type;
    }

    public String getProd_type() {
        return prod_type;
    }

    public void setProd_type(String prod_type) {
        this.prod_type = prod_type;
    }

    public String getTache_name() {
        return tache_name;
    }

    public void setTache_name(String tache_name) {
        this.tache_name = tache_name;
    }

    public String getTache_code() {
        return tache_code;
    }

    public void setTache_code(String tache_code) {
        this.tache_code = tache_code;
    }

    public String getProvince_id() {
        return province_id;
    }

    public void setProvince_id(String province_id) {
        this.province_id = province_id;
    }

    public String getProvince_name() {
        return province_name;
    }

    public void setProvince_name(String province_name) {
        this.province_name = province_name;
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

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDeal_person() {
        return deal_person;
    }

    public void setDeal_person(String deal_person) {
        this.deal_person = deal_person;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOpera_context() {
        return opera_context;
    }

    public void setOpera_context(String opera_context) {
        this.opera_context = opera_context;
    }

    public String getOpera_time() {
        return opera_time;
    }

    public void setOpera_time(String opera_time) {
        this.opera_time = opera_time;
    }

    public String getComplate_time() {
        return complate_time;
    }

    public void setComplate_time(String complate_time) {
        this.complate_time = complate_time;
    }

    public String getData_sources() {
        return data_sources;
    }

    public void setData_sources(String data_sources) {
        this.data_sources = data_sources;
    }

    public String getSys_sources() {
        return sys_sources;
    }

    public void setSys_sources(String sys_sources) {
        this.sys_sources = sys_sources;
    }

    public String getIs_timeout() {
        return is_timeout;
    }

    public void setIs_timeout(String is_timeout) {
        this.is_timeout = is_timeout;
    }

    public String getWo_state() {
        return wo_state;
    }

    public void setWo_state(String wo_state) {
        this.wo_state = wo_state;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getOrder_type() {
        return order_type;
    }

    public void setOrder_type(String order_type) {
        this.order_type = order_type;
    }
}
