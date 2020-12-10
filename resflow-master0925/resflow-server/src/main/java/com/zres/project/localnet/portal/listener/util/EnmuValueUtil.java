package com.zres.project.localnet.portal.listener.util;

public class EnmuValueUtil {

    /**
     * 订单类型
     */
    public static final String SECONDARY_TRUNK_DISPATCH = "SECONDARY_TRUNK_DISPATCH"; //二干电路
    public static final String LOCAL_NETWORK_DISPATCH = "LOCAL_NETWORK_DISPATCH"; //本地网电路

    /**
     * 订单对象类型 OBJ_TYPE
     */
    public static final String MAIN_DISPATCH_CUST = "MAIN_DISPATCH_CUST"; //[一干电路]客户、局内电路
    public static final String SECONDARY_CUST = "SECONDARY_CUST"; //[二干电路]客户电路
    public static final String SECONDARY_INSIDE = "SECONDARY_INSIDE"; //[二干电路]局内电路
    public static final String MAIN_DISPATCH_CUST_CLD = "MAIN_DISPATCH_CUST_CLD"; //资源分配子流程
    public static final String DATA_PRODUCTION_CHILD = "DATA_PRODUCTION_CHILD"; //数据制作子流程
    public static final String SECONDARY_SRC_REVIEW = "SECONDARY_SRC_REVIEW"; //核查电路

    /**
     * 订单动作类型
     */
    public static final String SECONDARY_RESOURCE_SUPPLEMENT_FLOW = "SECONDARY_RESOURCE_SUPPLEMENT_FLOW"; //资源补录主流程

    /**
     * 资源补录--环节
     */
    public static final String ALL_SPECIALTY_RESOURCE_SUPPLEMENT_SEC = "ALL_SPECIALTY_RESOURCE_SUPPLEMENT_SEC"; //各专业资源补录
    public static final String LOCAL_SCHEDULE_RESOURCE_SUPPLEMENT_SEC = "LOCAL_SCHEDULE_RESOURCE_SUPPLEMENT_SEC"; //本地调度资源补录


    /**
     * [一干电路]客户、局内电路环节
     */
    public static final String SECONDARY_SCHEDULE = "SECONDARY_SCHEDULE";
    public static final String SEC_SOURCE_DISPATCH = "SEC_SOURCE_DISPATCH"; //二干资源分配
    public static final String TO_DATA_CREATE_AND_SCHEDULE = "TO_DATA_CREATE_AND_SCHEDULE"; //待数据制作与本地调度
    public static final String INTER_PROVINCIAL_COMMISSIONING = "INTER_PROVINCIAL_COMMISSIONING"; //省际全程调测
    public static final String NOTICE_OF_RENT_CONFIRMATION = "NOTICE_OF_RENT_CONFIRMATION"; //起租确认通知
    public static final String SUMMARY_OF_COMPLETION = "SUMMARY_OF_COMPLETION"; //完工汇总环节

    /**
     * [二干电路]客户、局内电路环节
     */
    public static final String SECONDARY_SCHEDULE_2 = "SECONDARY_SCHEDULE_2";
    public static final String NEW_APPLICATION_FORM = "NEW_APPLICATION_FORM"; //新建申请单
    public static final String APPLICATION_REVIEW = "APPLICATION_REVIEW"; //申请单审核
    public static final String SEC_SOURCE_DISPATCH_2 = "SEC_SOURCE_DISPATCH_2"; //二干资源分配
    public static final String TO_DATA_CREATE_AND_SCHEDULE_2 = "TO_DATA_CREATE_AND_SCHEDULE_2"; //待数据制作与本地调度
    public static final String FULL_COMMISSIONING = "FULL_COMMISSIONING"; //全程调测
    public static final String NOTICE_OF_RENT_CONFIRMATION_2 = "NOTICE_OF_RENT_CONFIRMATION_2"; //起租确认通知
    public static final String SUMMARY_OF_COMPLETION_2 = "SUMMARY_OF_COMPLETION_2"; //完工汇总环节

    /**
     * 资源分配子流程
     */
    public static final String FINISH_SOURCES_DISP = "FINISH_SOURCES_DISP"; //二干资源分配完成

    /**
     * 数据制作子流程
     */
    public static final String SPECIALTY_DATA_PRODUCTION_FINSH = "SPECIALTY_DATA_PRODUCTION_FINSH"; //专业数据制作完成

    /**
     * 资源施工子流程
     */
    public static final String SPECIALTY_RES_CONSTRUCTION_FINISH = "SPECIALTY_RES_CONSTRUCTION_FINISH"; //专业资源施工完成

    /**
     * 本地网电路流程环节
     */
    public static final String RENT = "RENT"; //起租
    public static final String START_RENT = "START_RENT"; //起租 -- 本地客户电路
    public static final String START_STOP_RENT = "START_STOP_RENT"; //起止租
    public static final String STOP_RENT = "STOP_RENT"; //止租
    public static final String LOCAL_TEST = "LOCAL_TEST"; //本地测试
    public static final String CROSS_WHOLE_COURDER_TEST = "CROSS_WHOLE_COURDER_TEST"; //跨域全程调测
    /**
     * 完工汇总的环节编码
     */
    public static final String COMPLATE_SUMMARY = "510101044";
    public static final String COMPLATE_SUMMARY_2 = "510101084";

    public static final String SEC_SOURCE_DISPATCH_CLD = "510101048"; //二干资源分配（子流程）
    public static final String SPECIALTY_DATA_PRODUCTION = "1551002629"; //专业数据制作环节

}
