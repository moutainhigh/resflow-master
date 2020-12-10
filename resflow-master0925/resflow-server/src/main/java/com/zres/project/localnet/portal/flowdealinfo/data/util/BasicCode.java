package com.zres.project.localnet.portal.flowdealinfo.data.util;

/**
 * Created by tanghuili on 2019/1/7.
 */
public class BasicCode {
    // 环节id uos_tache.id
    /**select * from uos_tache where tache_catalog_id in (500001061,500001062)
     */
    public static final String OUTSIDElINE_CHECK = "500001145";
    public static final String DATA_CHECK = "500001146";
    public static final String TRANS_CHECK = "500001147";
    public static final String ACCESS_CHECK = "500001148";
    public static final String OTHER_CHECK = "500001149";

    public static final String CHECK_TOTAL = "500001150"; //核查汇总
    public static final String DEMAND_COMPLETE_FILE = "500001167";
    public static final String INVESTMENT_ESTIMATION = "500001151";
    public static final String CHANGE_CHECK = "510101020";

    public static final String CHECK_DISPATCH = "500001144"; //核查调度
    public static final String CIRCUIT_DISPATCH ="500001153"; //电路调度
    public static final String RES_ALLOCATE = "500001157"; //资源分配
    public static final String DATA_MAKE = "500001158"; //数据制作
    public static final String RES_CONSTRUCT = "500001159"; //资源施工
    public static final String FIBER_RES_ALLOCATE = "500001155"; //光纤资源分配
    public static final String CHILDFLOWWAIT = "500001200"; //光纤资源分配
    public static final String COMPLETE_CONFIRM ="500001162"; //完工确认
    public static final String WHOLE_COURSE_TEST = "500001161"; //全程调测
    public static final String UNION_DEBUG_TEST = "500001166"; //联调测试
    public static final String LOCAL_TEST = "500001160"; //本地测试
    public static final String CROSS_WHOLE_COURDER_TEST = "500001168"; //跨越全程调测
    public static final String RENT = "500001222"; //起租

    //二干调度的环节
    public static final String NEW_APPLICATION_FORM = "510101050"; //新建申请单 --[二干电路]客户，局内电路流程
    public static final String LOCAL_SCHEDULE = "510101043"; //本地调度 --[一干电路]客户，局内电路流程
    public static final String LOCAL_SCHEDULE_2 = "510101083"; //本地调度 --[二干电路]客户，局内电路流程
    public static final String TO_DATA_CREATE_AND_SCHEDULE = "510101047"; //本地调度 --[二干电路]客户，局内电路流程
    public static final String SECONDARY_SCHEDULE = "510101040"; //二干调度 -- [一干电路]客户，局内电路流程
    public static final String SECONDARY_SCHEDULE_2 = "510101080"; //二干调度 -- [二干电路]客户，局内电路流程
    public static final String SEC_SOURCE_DISPATCH = "510101041"; //二干资源分配 -- [一干电路]客户，局内电路流程
    public static final String SEC_SOURCE_DISPATCH_2 = "510101081"; //二干资源分配 -- [二干电路]客户，局内电路流程
    public static final String SEC_SOURCE_DISPATCH_CLD = "510101048"; //二干资源分配 -- 子流程
    public static final String FINISH_SOURCES_DISP = "510101049"; //二干资源分配完成 -- 子流程
    public static final String SPECIALTY_DATA_PRODUCTION_FINSH = "1551002630"; //专业数据制作完成 -- 子流程
    public static final String SUMMARY_OF_COMPLETION = "510101044"; //完工汇总 -- [一干电路]客户，局内电路流程
    public static final String SUMMARY_OF_COMPLETION_2 = "510101084"; //完工汇总 -- [二干电路]客户，局内电路流程
    public static final String CONFIRM_THE_END = "1551002649"; //确认结束 -- [二干电路]局内电路流程

    public static final String FULL_COMMISSIONING = "510101051"; //全程调测 -- [二干电路]客户，局内电路流程
    public static final String INTER_PROVINCIAL_COMMISSIONING = "510101045"; //省际全程调测 -- [一干电路]客户，局内电路流程
    public static final String NOTICE_OF_RENT_CONFIRMATION_2 = "510101085"; //起租确认通知 -- [二干电路]客户，局内电路流程
    public static final String NOTICE_OF_RENT_CONFIRMATION = "510101046"; //起租确认通知 -- [一干电路]客户，局内电路流程
    public static final String SPECIALTY_DATA_EXEC = "510101042"; //专业数据制作 -- [一干电路]客户，局内电路流程
    public static final String SPECIALTY_DATA_EXEC_2 = "510101082"; //专业数据制作 -- [二干电路]客户，局内电路流程
    public static final String SPECIALTY_DATA_PRODUCTION = "1551002629"; //专业数据制作 -- 子流程 专业数据制作子流程
    public static final String TO_DATA_CREATE_AND_SCHEDULE_2 = "510101086"; //待数据制作与本地调度 -- [二干电路]客户，局内电路流程
    public static final String SPECIALTY_RES_CONSTRUCTION = "1551002709"; //专业数据制作 -- 子流程 专业数据制作子流程


    //二干核查相关环节
    public static final String CHECK_SCHEDULING = "510101052"; //二干核查调度
    public static final String CHECK_SUMMARY = "510101060"; //二干核查汇总
    public static final String INVESTMENT_ESTIMATION_SCHEDU = "510101066"; //二干投资估算
    public static final String DATA_PROFESSIONAL_VERIFICATION = "510101061"; //二干数据专业核查
    public static final String TRANSPORT_PROFESSIONAL_CHECK = "510101062"; //二干传输专业核查
    public static final String EXCHANGE_PROFESSIONAL_CHECK = "510101063"; //二干交换专业核查
    public static final String OTHER_PROFESSIONAL_CHECK = "510101064"; //二干其他专业核查
    public static final String LOCAL_NETWORK_CHECK = "510101065"; //二干本地网核查


    // 岗位id  uos_post.post_id
    /**
     * 101	外线专业
     102	数据专业
     103	传输专业
     104	接入专业
     105	其他专业
     */
    public static final String OUTSIDElINE_POST_ID = "101";
    public static final String DATA_POST_ID = "102";
    public static final String TRANS_POST_ID = "103";
    public static final String ACCESS_POST_ID = "104";
    public static final String OTHER_POST_ID = "105";
    /*
    派发对象类型：260000001  组织；260000002  角色；260000003  人员；260000004  虚拟职位
     */
    public static final String DISP_TYPE_ORG = "260000001";
    public static final String DISP_TYPE_JOB = "260000002";
    public static final String DISP_TYPE_STAFF = "260000003";

    // 流程id SELECT * FROM GOM_PS_2_ORD_S where order_type='LOCAL_NETWORK_DISPATCH'
    public static final String LOCAL_RES_CHECK_FLOW = "1000211"; // 核查流程
    public static final String LOCAL_CUST_NEWOPEN_FLOW = "1000212"; // 本地客户电路新开、变更、移机流程
    public static final String LOCAL_CUST_STOP_FLOW = "1000213"; // 本地客户电路停复机流程
    public static final String LOCAL_CUST_DISMANTLE_FLOW = "1000214"; // 本地客户电路拆机流程
    public static final String LOCAL_INSIDE_NEWOPEN_FLOW = "1000207"; // 本地局内电路新开、变更流程
    public static final String LOCAL_INSIDE_DISMANTLE_FLOW = "1000208"; // 本地局内电路拆机流程
    public static final String CROSS_NEWOPEN_FLOW = "1000209"; // 跨域电路新开、变更流程
    public static final String CROSS_STOP_FLOW = "1000210"; // 跨域电路停闭
    public static final String LOCAL_OPTICAL_SPECIAL_CHILDFLOW = "1000248"; //光纤专业子流程 BasicCode.LOCAL_TEST
    public static final String LOCAL_OTHER_SPECIAL_CHILDFLOW = "1000249"; //其他专业子流程

    //二干主流程
    public static final String CUST_SECONDARY_NEW_ACT = "10101060"; //二干调度-[二干电路]客户电路流程-新开
    public static final String CUST_SECONDARY_STOP_PROCESS_ACT = "10101065"; //二干调度-[二干电路]客户电路流程-停复机
    public static final String INSIDE_SECONDARY_NEW_ACT = "10101061"; //二干调度-[二干电路]局内电路流程-新开
    public static final String SECONDARY_NEW_ACT = "10101061"; //二干调度-[一干电路]客户、局内电路流程-新开
    public static final String SECONDARY_PARALLEL_CHECK = "10101322"; //二干调度-并行核查流程
    public static final String SECONDARY_CHECK = "10101042"; //二干调度-串行核查流程

    //二干流程
    public static final String DATA_PRODUCTION_CHILD = "10101064"; //数据制作子流程
    public static final String RES_CONSTRUCTION_CHILD = "10101363"; //资源施工子流程
    public static final String MAIN_DISPATCH_CUST_CLD = "10101043"; //二干资源分配子流程
    public static final String SECONDARY_RESOURCE_SUPPLEMENT_CHILDFLOW = "10101285"; //资源补录子流程--二干

    //本地流程
    public static final String LOCAL_RESOURCE_SUPPLEMENT_FLOW = "10101282"; //资源补录--本地流程


    //二干调度查询字段
    public static final String SPECIALTY = "SPECIALTY"; //二干资源分配子流程派发专业查询字段
    public static final String NETMANAGE = "NETMANAGE"; //数据制作子流程派发专业查询字段
    public static final String RESSUP = "RESSUP"; //资源补录子流程派发专业查询字段
    public static final String RESSUP_LOCAL = "RESSUP_LOCAL"; //资源补录子流程派发本地调度查询字段
    public static final String RESCONSTRUCTION = "RESCONSTRUCTION"; //资源施工子流程派发专业查询字段


    // 专业 SELECT * FROM GOM_PUB_DATE_S WHERE DF_TYPE = 'SPECIALTY_TYPE'
    public static final String COMPLEX_1 = "COMPLEX_1"; // 设备综合专业
    public static final String OPTICAL_2 = "OPTICAL_2"; // 光纤专业
    public static final String TRANS_3 = "TRANS_3"; // 传输专业
    public static final String DATA_4 = "DATA_4"; // 数据专业
    public static final String EXCHANGE_5 = "EXCHANGE_5"; // 交换专业
    public static final String TRANS_IPRAN_13 = "TRANS_IPRAN_13"; // 传输-IPRAN
    public static final String TRANS_MSAP_14 = "TRANS_MSAP_14"; // 传输-MSAP
    public static final String OTHER_11 = "OTHER_11"; // 其他专业
    public static final String IP_15 = "IP_15"; // ip地址
    public static final String ACCESS_6 = "ACCESS_6"; // 接入地址

    // 流程订单属性
    public static final String REGION_ID = "REGION_ID"; // 区域id
    public static final String SPECIALTY_CODE = "SPECIALTY_CODE"; // 专业类型


    /**
     * 二干 资源配置环节相关
     */
    public static final String SECOND_CUST_FLOW = "510101080"; //二干调度环节资源配置 本地发起
    public static final String SECOND_CROSS_FLOW = "510101040"; //（一干） 二干调度环节资源配置
    public static final String SECOND_CUST_CHILDFLOW = "510101081"; //二干调度环节资源配置
    public static final String SECOND_CROSS_CHILDFLOW = "510101048"; //二干 资源分配环节资源配置  本地发起



    public static final String SECOND_CHECK_FLOW = "510101052"; //二干  核查调度
    public static final String SECOND_CHECK_DATA = "510101061"; //二干 数据核查
    public static final String SECOND_CHECK_TRAN = "510101062"; //二干 传输专业核查
    public static final String SECOND_CHECK_EXCHANGE = "510101063"; //二干  交换专业核查
    public static final String SECOND_CHECK_OTHER = "510101064"; //二干   其他专业核查

    //业务电路的来源
    public static final String LOCALBUILD = "localBuild"; // 本地网
    public static final String ONEDRY = "onedry"; // 一干
    public static final String SECONDARY = "secondary"; // 二干
    public static final String JIKE = "jike"; // 集客

    public static final String RES_SUPPLEMENT = "RES_SUPPLEMENT"; // 资源补录

    //工单所属系统
    public static final String LOCAL = "flow-schedule-lt"; // 本地网
    public static final String SECOND = "second-schedule-lt"; // 二干

    public static final String LOCALFLOW = "local-flow-schedule"; // 本地网拆
    public static final String SECONDTOLOCAL = "second-local-flow"; // 二干TO本地



    // 单子动作类型
    public static final String ACTIVE_TYPE_NEWOPEN = "101"; // 新开
    public static final String ACTIVE_TYPE_CHANGE = "103"; // 变更
    public static final String ACTIVE_TYPE_MOVE = "106"; // 移机
    public static final String ACTIVE_TYPE_STOP = "104"; // 停机
    public static final String ACTIVE_TYPE_DISMANTLE = "102"; // 拆机
    public static final String ACTIVE_TYPE_CHECK = "107"; // 核查
    public static final String ACTIVE_TYPE_SUPPLEMENT = "108"; // 补录

    //异常节点退单流程正向反向
    public static final String FORWARD = "210000002"; // 正向
    public static final String RECERSE = "210000001"; // 反向

    //资源补录环节流程
    public static final String APPLICATION_INITIATED_SEC = "1551002654"; // 申请发起

}
