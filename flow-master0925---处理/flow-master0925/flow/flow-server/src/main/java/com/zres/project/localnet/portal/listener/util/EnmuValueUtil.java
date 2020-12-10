package com.zres.project.localnet.portal.listener.util;

public class EnmuValueUtil {

    /**
     * 订单类型 ORDER_TYPE
     */
    public static final String LOCAL_NETWORK_DISPATCH = "LOCAL_NETWORK_DISPATCH"; //本地网电路
    public static final String SECONDARY_TRUNK_DISPATCH = "SECONDARY_TRUNK_DISPATCH"; //二干电路

    /**
     * 订单对象类型 OBJ_TYPE
     */
    //本地网调度
    public static final String LOCAL_CUST = "LOCAL_CUST"; //客户电路
    public static final String LOCAL_INSIDE = "LOCAL_INSIDE"; //局内电路
    public static final String CROSS_DOMAIN = "CROSS_DOMAIN"; //跨域电路
    public static final String LOCAL_CHILDFLOW = "LOCAL_CHILDFLOW"; //子流程
    public static final String LOCAL_RES_CHECK = "LOCAL_RES_CHECK"; //核查电路
    //二干调度
    public static final String MAIN_DISPATCH_CUST = "MAIN_DISPATCH_CUST"; //[一干电路]客户、局内电路
    public static final String SECONDARY_CUST = "SECONDARY_CUST"; //[二干电路]客户电路
    public static final String SECONDARY_INSIDE = "SECONDARY_INSIDE"; //[二干电路]局内电路
    public static final String MAIN_DISPATCH_CUST_CLD = "MAIN_DISPATCH_CUST_CLD"; //资源分配子流程
    public static final String DATA_PRODUCTION_CHILD = "DATA_PRODUCTION_CHILD"; //数据制作子流程
    public static final String SECONDARY_SRC_REVIEW = "SECONDARY_SRC_REVIEW"; //核查电路

    /**
     * 流程环节
     */
    public static final String CIRCUIT_DISPATCH = "CIRCUIT_DISPATCH"; //电路调度
    public static final String COMPLETE_CONFIRM = "COMPLETE_CONFIRM"; //完工确认
    public static final String LOCAL_TEST = "LOCAL_TEST"; //本地测试
    public static final String CROSS_WHOLE_COURDER_TEST = "CROSS_WHOLE_COURDER_TEST"; //跨域全程调测
    public static final String RENT = "RENT"; //起租
    public static final String START_RENT = "START_RENT"; //起租 -- 本地客户电路
    public static final String START_STOP_RENT = "START_STOP_RENT"; //起止租
    public static final String STOP_RENT = "STOP_RENT"; //止租

    //核查流程新增环节
    public static final String CHECK_WAIT = "CHECK_WAIT"; //核查等待

    /**
     * 子流程
     */
    public static final String OUTSIDE_CONSTRUCT = "OUTSIDE_CONSTRUCT"; //外线施工
    public static final String RES_ALLOCATE = "RES_ALLOCATE"; //资源分配
    public static final String DATA_MAKE = "DATA_MAKE"; //数据制作
    public static final String RES_CONSTRUCT = "RES_CONSTRUCT"; //资源施工
    public static final String CHILDFLOWWAIT = "CHILDFLOWWAIT"; //子流程等待环节

    /**
     * [一干电路]客户、局内电路环节
     */
    public static final String SUMMARY_OF_COMPLETION = "SUMMARY_OF_COMPLETION"; //完工汇总环节
    public static final String NOTICE_OF_RENT_CONFIRMATION = "NOTICE_OF_RENT_CONFIRMATION"; //起租确认通知

    /**
     * [二干电路]客户、局内电路环节
     */
    public static final String FULL_COMMISSIONING = "FULL_COMMISSIONING"; //全程调测环节
    public static final String SUMMARY_OF_COMPLETION_2 = "SUMMARY_OF_COMPLETION_2"; //完工汇总
    public static final String NOTICE_OF_RENT_CONFIRMATION_2 = "NOTICE_OF_RENT_CONFIRMATION_2"; //起租确认通知

    /**
     * 数据制作子流程 --二干
     */
    public static final String SPECIALTY_DATA_PRODUCTION_FINSH = "SPECIALTY_DATA_PRODUCTION_FINSH"; //专业数据制作完成环节


    //二干调度查询字段
    public static final String SPECIALTY = "SPECIALTY"; //二干资源分配子流程派发专业查询字段
    public static final String NETMANAGE = "NETMANAGE"; //数据制作子流程派发专业查询字段

    /**
     * 资源补录
     */
    public static final String LOCAL_RESOURCE_SUPPLEMENT_FLOW = "LOCAL_RESOURCE_SUPPLEMENT_FLOW"; //资源补录主流程--本地
    public static final String SECONDARY_RESOURCE_SUPPLEMENT_FLOW = "SECONDARY_RESOURCE_SUPPLEMENT_FLOW"; //资源补录主流程--二干

    /**
     * 资源补录--环节
     */
    public static final String ALL_SPECIALTY_RESOURCE_SUPPLEMENT_LOCAL = "ALL_SPECIALTY_RESOURCE_SUPPLEMENT_LOCAL"; //各专业资源补录
    public static final String LOCAL_SCHEDULE_RESOURCE_SUPPLEMENT_SEC = "LOCAL_SCHEDULE_RESOURCE_SUPPLEMENT_SEC"; //本地调度资源补录--二干
    public static final String ALL_SPECIALTY_RESOURCE_SUPPLEMENT_SEC = "ALL_SPECIALTY_RESOURCE_SUPPLEMENT_SEC"; //各专业资源补录--二干

    // sdwan环节流程
    public static final String OPS_SENT = "OPS_SENT"; // 运维派单
    public static final String WAIT_SDWAN_FEEDBACK = "WAIT_SDWAN_FEEDBACK"; // 等待sdwan平台反馈
    public static final String TERNIMAL_DELIVERY = "TERNIMAL_DELIVERY"; // 终端出库及上门安装
    public static final String PICK_UP_TERMINAL_BOX = "PICK_UP_TERMINAL_BOX"; // 上门回收终端盒
    public static final String BUSINESS_TEST = "BUSINESS_TEST"; // 业务测试

    /**
     * 完工汇总的环节编码
     */
    public static final String COMPLATE_SUMMARY = "510101044";
    public static final String COMPLATE_SUMMARY_2 = "510101084";

    public static final String  DDOS_FLOW_CLEANING = "DDOS_FLOW_CLEANING"; //方案设计环节
    public static final String DIA_DDOS_FLOW = "DIA_DDOS_FLOW"; //互联网专线附加产品DDOS （对象类型）
    public static final String DIA_DDOS_CHANGE_FLOW = "DIA_DDOS_CHANGE_FLOW"; //DDOS变更流程（动作类型）

    public static final String SEC_SOURCE_DISPATCH_CLD = "510101048"; //二干资源分配（子流程）
    public static final String SPECIALTY_DATA_PRODUCTION = "1551002629"; //专业数据制作环节

}
