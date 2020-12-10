package com.zres.project.localnet.portal.flowdealinfo.data.util;

/**
 * Created by tanghuili on 2019/1/7.
 */
public class BasicCode {
    // 环节id uos_tache.id
    /**select * from uos_tache where tache_catalog_id in (500001061,500001062)
     */
    public static final String OUTSIDElINE_CHECK = "500001145";
    public static final String DATA_CHECK = "500001146"; //数据专业核查
    public static final String TRANS_CHECK = "500001147"; //传输专业核查
    public static final String ACCESS_CHECK = "500001148";//接入专业核查
    public static final String OTHER_CHECK = "500001149";//其他专业核查

    public static final String CHECK_TOTAL = "500001150"; //核查汇总
    public static final String DEMAND_COMPLETE_FILE = "500001167";//需求完工归档
    public static final String INVESTMENT_ESTIMATION = "500001151";//投资估算
    public static final String CHANGE_CHECK = "510101020";
    public static final String SEND_ENGINEERING = "510101021";// 下发工建

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
    public static final String CROSS_WHOLE_COURDER_TEST = "500001168"; //跨域全程调测
    public static final String RENT = "500001222"; //起租
    public static final String SUMMARY_OF_COMPLETION = "510101044"; //	510101021	完工汇总
    public static final String DDOS_FLOW_CLEANING = "510101101"; //DDOS激活

    //二干调度的环节
    public static final String FULL_COMMISSIONING = "510101051"; //全程调测 -- [二干电路]客户，局内电路流程
    public static final String CUST_STATION_DATA_MAKE = "510101082"; //专业数据制作-- [二干电路]客户，局内电路流程
    public static final String CUST_STATION_LOCAL_SCHEDULING = "510101083"; //本地调度-- [二干电路]客户，局内电路流程
    public static final String INTER_PROVINCIAL_COMMISSIONING = "510101045"; //省际全程调测 -- [一干电路]客户，局内电路流程
    public static final String SPECIALTY_DATA_EXEC = "510101042"; //专业数据制作 -- [一干电路]客户，局内电路流程
    public static final String SPECIALTY_DATA_EXEC_2 = "510101082"; //专业数据制作 -- [二干电路]客户，局内电路流程
    public static final String TO_DATA_CREATE_AND_SCHEDULE_2 = "510101086"; //待专业数据制作与本地调度 -- [二干电路]客户，局内电路流程
    public static final String SECONDARY_SCHEDULE = "510101040"; //二干调度 -- [一干电路]客户，局内电路流程
    public static final String SECONDARY_SCHEDULE_2 = "510101080"; //二干调度 -- [二干电路]客户，局内电路流程
    public static final String SEC_SOURCE_DISPATCH = "510101041"; //二干资源分配 -- [二干电路]客户，局内电路流程
    public static final String SEC_SOURCE_DISPATCH_2 = "510101081"; //二干资源分配 -- [二干电路]客户，局内电路流程

    /*
     * 二干核查相关环节
     * @author ren.jiahang
     * @date 2019/5/22 11:53
     * @param null
     * @return
     */
    public static final String CHECK_SCHEDULING = "510101052"; //二干核查调度
    public static final String CHECK_SUMMARY = "510101060"; //二干核查汇总
    public static final String DATA_PROFESSIONAL_VERIFICATION = "510101061"; //二干数据专业核查
    public static final String INVESTMENT_ESTIMATION_SCHEDU = "510101066"; //二干投资估算
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
    public static final String LOCAL_PRARLLEL_CHECK_FLOW = "10101342"; // 并行核查流程
    public static final String LOCAL_CUST_NEWOPEN_FLOW = "1000212"; // 本地客户电路新开、变更、移机流程
    public static final String LOCAL_CUST_STOP_FLOW = "1000213"; // 本地客户电路停复机流程
    public static final String LOCAL_CUST_DISMANTLE_FLOW = "1000214"; // 本地客户电路拆机流程
    public static final String LOCAL_INSIDE_NEWOPEN_FLOW = "1000207"; // 本地局内电路新开、变更流程
    public static final String LOCAL_INSIDE_DISMANTLE_FLOW = "1000208"; // 本地局内电路拆机流程
    public static final String CROSS_NEWOPEN_FLOW = "1000209"; // 跨域电路新开、变更流程
    public static final String CROSS_STOP_FLOW = "1000210"; // 跨域电路停闭
    public static final String LOCAL_OPTICAL_SPECIAL_CHILDFLOW = "1000248"; //光纤专业子流程 BasicCode.LOCAL_TEST
    public static final String LOCAL_OTHER_SPECIAL_CHILDFLOW = "1000249"; //其他专业子流程
    public static final String LOCAL_RESOURCE_SUPPLEMENT_CHILDFLOW = "10101283"; //资源补录子流程--本地
    public static final String SDWAN_OPEN_FLOW = "10101303"; //sdwan新开流程
    public static final String SDWAN_DISMANTLE_FLOW = "10101304"; //sdwan拆机流程


    public static final String CHILDFLOW_END = "CHILDFLOW_END"; //子流程结束标志

    //二干电路
    public static final String SECONDARY_CUST = "10101060"; //客户电路
    public static final String SECONDARY_INSIDE = "10101061"; //局内电路

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
    public static final String WIRELESS_7 = "WIRELESS_7"; // 无线网,
    public static final String MOBILE_8 = "MOBILE_8"; // 'MOBILE_8' 移动核心
    public static final String SYN_9 = "SYN_9"; // 'SYN_9' 同步网
    public static final String IMS_10 = "IMS_10"; // 'IMS_10' IMS网
    public static final String P_DATA_4 = "P_DATA_4"; // '省数据
    public static final String P_EXCHANGE_5 = "P_EXCHANGE_5"; // '省核心网

    // 流程订单属性
    public static final String REGION_ID = "REGION_ID"; // 区域id
    public static final String SPECIALTY_CODE = "SPECIALTY_CODE"; // 专业类型

    public static final String[] RES_CONFIG ={"COMPLEX_1","OPTICAL_2","TRANS_3","DATA_4","EXCHANGE_5","TRANS_IPRAN_13","TRANS_MSAP_14","OTHER_11", "ACCESS_6","WIRELESS_7","MOBILE_8","SYN_9","IMS_10","P_EXCHANGE_5","P_DATA_4"}; // 专业类型

    //业务电路的来源
    public static final String LOCALBUILD = "localBuild"; // 本地网
    public static final String ONEDRY = "onedry"; // 一干
    public static final String SECONDARY = "secondary"; // 二干
    public static final String JIKE = "jike"; // 集客

    public static final String RES_SUPPLEMENT = "RES_SUPPLEMENT"; // 资源补录


    //工单所属系统
    public static final String LOCAL = "flow-schedule-lt"; // 本地网
    public static final String SECOND = "second-schedule-lt"; // 二干
    public static final String SEC_LOCAL = "sec-flow-schedule-lt"; // 二干下发本地

    public static final String NEW_RESOURCE_YES = "0"; // 电路专业配置新建资源-是
    public static final String NEW_RESOURCE_NO = "1"; // 电路专业配置新建资源-否

    //是否复用上级调单标识
    public static final String REUSE_ONEDRY = "0"; //复用
    public static final String NOT_REUSE = "1"; //不复用

    // 工单状态
    public static final String wo_state = "1"; // 处理中

    // 单子动作类型
    public static final String ACTIVE_TYPE_NEWOPEN = "101"; // 新开
    public static final String ACTIVE_TYPE_STOP = "104"; // 停机
    public static final String ACTIVE_TYPE_DISMANTLE = "102"; // 拆机
    public static final String ACTIVE_TYPE_BACK = "105";
    public static final String ACTIVE_TYPE_REMOVE = "106"; //移机
    public static final String ACTIVE_TYPE_CHECK = "107"; // 核查
    public static final String ACTIVE_TYPE_SUPPLEMENT = "108"; // 补录


    public static final String RESSUP = "RESSUP"; //资源补录子流程派发专业查询字段
    public static final String RESSUP_LOCAL = "RESSUP_LOCAL"; //资源补录子流程派发本地调度查询字段

    //资源补录环节流程
    public static final String APPLICATION_INITIATED_LOCAL = "1551002651"; // 申请发起
    public static final String SPECIALTY_RESOURCE_SUPPLEMENT_LOCAL = "1551002653"; // 专业资源补录

    // sdwan环节流程
    public static final String WAIT_SDWAN_FEEDBACK = "1551002671"; // 等待sdwan平台反馈
    public static final String TERNIMAL_DELIVERY = "1551002670"; // 终端出库及上门安装
    public static final String PICK_UP_TERMINAL_BOX = "1551002674"; // 上门回收终端盒
    public static final String BUSINESS_TEST = "1551002672"; // 业务测试
    public static final String BUSINESS_SENT = "1551002673"; // 业务派单

    //业务类型
    public static final String DIA_SERVICEID = "10000011"; //互联网专线业务

}
