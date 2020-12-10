package com.zres.project.localnet.portal.orderAbnormal.constant;

public interface OrderAbnormalConstant {
    //异常日志状态
    String EPC_STATE_PENDING = "760000001";  //待处理
    String EPC_STATE_AUDIT_PASS = "760000002";//审核通过
    String EPC_STATE_AUDIT_NO = "760000003"; //审核不通过
    String EPC_STATE_AUDIT_END_PASS = "760000004"; //审核结束_通过
    String EPC_STATE_AUDIT_END_NO = "760000005"; //审核结束_不通过
    String EPC_STATE_ONLY_SAVE = "760000006"; //仅保存记录


    /**
     * 本地电路调度环节tache_code
     */
    String CIRCUIT_DISPATCH = "CIRCUIT_DISPATCH";

    /**
     * 二干调度一干电路tache_code
     */
    String SECONDARY_SCHEDULE = "SECONDARY_SCHEDULE";

    /**
     * 二干调度二干电路tache_code
     */
    String SECONDARY_SCHEDULE_2 = "SECONDARY_SCHEDULE_2";


    /**
     * 【一干电路】 二干资源分配
     */
    String SEC_SOURCE_DISPATCH = "SEC_SOURCE_DISPATCH";

    /**
     * [二干电路] 二干资源分配
     */
    String SEC_SOURCE_DISPATCH_2 = "SEC_SOURCE_DISPATCH_2";


    /**
     * [一干电路] 待数据制作与本地调度
     */
    String TO_DATA_CREATE_AND_SCHEDULE = "TO_DATA_CREATE_AND_SCHEDULE";

    /**
     * [二干电路] 待数据制作与本地调度
     */
    String TO_DATA_CREATE_AND_SCHEDULE_2 = "TO_DATA_CREATE_AND_SCHEDULE_2";


    /**
     * 本地专业核查
     */
    String ALL_CHECK = "ALL_CHECK";

    /**
     * 二干专业核查
     */
    String ALL_CHECK_2 = "ALL_CHECK_2";

    /**
     * 本地网核查
     */
    String LOCAL_NETWORK_CHECK = "LOCAL_NETWORK_CHECK";


    /**
     * 异常流程的定单规格
     */
    String EXCEPTION_ORDER_TYPE = "ORDER_EXCEPTION";


    /**
     * 资源分配子流程obj_type
     */
    String MAIN_DISPATCH_CUST_CLD = "MAIN_DISPATCH_CUST_CLD";


    /**
     * 资源分配子流程act_type
     */
    String SOURCE_DISPATCH = "SOURCE_DISPATCH";


    /**
     * 异常单子流程
     */
    String EXCEPTION_OBJ_TYPE = "LOCAL_CHILDFLOW";

    /**
     * 异常单通用异常流程
     */
    String EXCEPTION_ORDER_OBJ = "EXCEPTION_ORDER_OBJ";

    /**
     * 异常单区域id，全国
     */
    String EXCEPTION_ORDER_AREA_ID = "350002000000000042766408";

    /**
     * 自动执行人id
     */
    String SYS_AUTO_DISP_ID = "-2000";

    /**
     * 特殊的执行人id
     */
    String SPEC_DISP_ID = "-1";

}
