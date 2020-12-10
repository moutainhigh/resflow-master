package com.zres.project.localnet.portal.common.util;

/*
 * 流程，环节code枚举值类
 * @author guanzhao
 * @date 2020/10/28
 *
 */
public class FlowTacheUtil {

    /**
     * 订单类型
     */
    public static final String LOCAL_NETWORK_DISPATCH = "LOCAL_NETWORK_DISPATCH"; //本地调度

    /**
     * 订单对象类型 OBJ_TYPE
     */
    public static final String LOCAL_YZW = "LOCAL_YZW"; //云组网
    public static final String YZW_CHILDFLOW = "YZW_CHILDFLOW"; //云组网子流程

    /**
     * 订单动作类型
     */
    public static final String YZW_UPEQUIP_BUSICONFIG_CHILDFLOW = "YZW_UPEQUIP_BUSICONFIG_CHILDFLOW"; //上联设备业务配置处理子流程
    public static final String YZW_IPRAN_CHILDFLOW = "YZW_IPRAN_CHILDFLOW"; //传输IPRAN专业子流程
    public static final String YZW_LOCAL_FIBER_CHILDFLOW = "YZW_LOCAL_FIBER_CHILDFLOW"; //光纤专业子流程

    /**
     * 环节
     */
    //本地业务新开
    public static final String YZW_L_CIRCUIT_DISPATCH = "YZW_L_CIRCUIT_DISPATCH"; //电路调度
    public static final String YZW_L_NEW_RES_ENTRY = "YZW_L_NEW_RES_ENTRY"; //新建资源录入
    public static final String YZW_A_CONSTRUCT_WAIT = "YZW_A_CONSTRUCT_WAIT"; //A端网络施工
    public static final String YZW_A_MCPE_INSTALL = "YZW_A_MCPE_INSTALL"; //A端MCPE安装测试
    public static final String YZW_A_PORT_CONFIG = "YZW_A_PORT_CONFIG"; //A端终端下联端口配置
    public static final String YZW_A_LOCAL_TEST = "YZW_A_LOCAL_TEST"; //A端本地测试报竣
    public static final String YZW_Z_CONSTRUCT_WAIT = "YZW_Z_CONSTRUCT_WAIT"; //Z端网络施工
    public static final String YZW_Z_MCPE_INSTALL = "YZW_Z_MCPE_INSTALL"; //Z端MCPE安装测试
    public static final String YZW_Z_PORT_CONFIG = "YZW_Z_PORT_CONFIG"; //Z端终端下联端口配置
    public static final String YZW_Z_LOCAL_TEST = "YZW_Z_LOCAL_TEST"; //Z端本地测试报竣
    public static final String YZW_L_UPLINK_CONFIG_WAIT = "YZW_L_UPLINK_CONFIG_WAIT"; //上联设备业务配置
    public static final String YZW_UPEQUIP_BUSICONFIG = "YZW_UPEQUIP_BUSICONFIG"; //上联设备业务配置--子流程
    public static final String YZW_L_MCPE_CONFIG = "YZW_L_MCPE_CONFIG"; //MCPE业务配置
    public static final String YZW_L_WHOLE_TEST = "YZW_L_WHOLE_TEST"; //全程测试报竣

    public static final String YZW_RENT_FILE = "YZW_RENT_FILE"; //起租归档

    //跨域业务新开
    public static final String YZW_C_CIRCUIT_DISPATCH = "YZW_C_CIRCUIT_DISPATCH"; //电路调度
    public static final String YZW_C_NEW_RES_ENTRY = "YZW_C_NEW_RES_ENTRY"; //新建资源录入
    public static final String YZW_C_CONSTRUCT_WAIT = "YZW_C_CONSTRUCT_WAIT"; //网络施工
    public static final String YZW_C_MCPE_INSTALL = "YZW_C_MCPE_INSTALL"; //MCPE安装测试
    public static final String YZW_C_PORT_CONFIG = "YZW_C_PORT_CONFIG"; //终端下联端口配置
    public static final String YZW_C_LOCAL_TEST = "YZW_C_LOCAL_TEST"; //本地测试报竣
    public static final String YZW_C_UPLINK_CONFIG = "YZW_C_UPLINK_CONFIG"; //上联设备业务配置
    public static final String YZW_C_MCPE_CONFIG = "YZW_C_MCPE_CONFIG"; //MCPE业务配置
    public static final String YZW_C_WHOLE_TEST = "YZW_C_WHOLE_TEST"; //全程测试报竣

    //本地业务移机
    public static final String YZW_L_CONSTRUCT_WAIT = "YZW_L_CONSTRUCT_WAIT"; //网络施工
    public static final String YZW_L_MCPE_INSTALL = "YZW_L_MCPE_INSTALL"; //MCPE安装测试
    public static final String YZW_L_PORT_CONFIG = "YZW_L_PORT_CONFIG"; //终端下联端口配置
    public static final String YZW_L_LOCAL_TEST = "YZW_L_LOCAL_TEST"; //本地测试报竣
    public static final String YZW_LY_UPLINK_CONFIG_WAIT = "YZW_LY_UPLINK_CONFIG_WAIT"; //上联设备业务配置

    //移机主流程启MCPE设备业务配置子流程环节
    public static final String YZW_START_MCPE_CONFIG = "YZW_START_MCPE_CONFIG"; //启动MCPE业务配置处理
    public static final String YZW_END_MCPE_CONFIG = "YZW_END_MCPE_CONFIG"; //完成MCPE业务配置处理

    //移机MCPE设备业务配置
    public static final String YZW_OLD_MCPE_REMOVE = "YZW_OLD_MCPE_REMOVE"; //原MCPE设备拆除
    public static final String YZW_SINGLE_MCPE_CONFIG = "YZW_SINGLE_MCPE_CONFIG"; //单端MCPE业务配置
    public static final String YZW_SINGLE_MCPE_OFFLINE = "YZW_SINGLE_MCPE_OFFLINE"; //单端MCPE终端盒下线
    public static final String YZW_RECOVER = "YZW_RECOVER"; //终端回收
    public static final String YZW_UPLINK_DATA_DETELE = "YZW_UPLINK_DATA_DETELE"; //上联设备数据删除
    public static final String YZW_UPLINK_CONFIG = "YZW_UPLINK_CONFIG"; //上联设备业务配置
    public static final String YZW_MCPE_CONFIG_FINISH = "YZW_MCPE_CONFIG_FINISH"; //完成MCPE业务配置处理

    //跨域移机配合端
    public static final String YZW_C_UPLINK_CONFIG_WAIT = "YZW_C_UPLINK_CONFIG_WAIT"; //上联设备业务配置等待下发
    public static final String YZW_C_UPLINK_CONFIG_COOPERATE = "YZW_C_UPLINK_CONFIG_COOPERATE"; //上联设备业务配置
    public static final String YZW_C_PORT_CONFIG_COOPERATE = "YZW_C_PORT_CONFIG_COOPERATE"; //终端下联端口配置
    public static final String YZW_C_MANUAL_CONFIG_COOPERATE = "YZW_C_MANUAL_CONFIG_COOPERATE"; //人工业务配置

    //跨域移机配合端ipran释放
    public static final String YZW_C_OLD_UPLINK_REMOVE = "YZW_C_OLD_UPLINK_REMOVE"; //原上联设备拆除
    public static final String YZW_C_MANUAL_CONFIG_IPRAN = "YZW_C_MANUAL_CONFIG_IPRAN"; //人工业务配置
    public static final String YZW_C_FEEDBACK_MAIN = "YZW_C_FEEDBACK_MAIN"; //反馈主端

    //子流程
    public static final String YZW_CHILDFLOWWAIT = "YZW_CHILDFLOWWAIT"; //子流程等待环节
    public static final String YZW_CHECK_WAITING = "YZW_CHECK_WAITING"; //云组网核查流程的子流程等待环节

    // 变更升降速
    public static final String YZW_RATE_UPLINK_CONFIG_WAIT = "YZW_RATE_UPLINK_CONFIG_WAIT"; //上联设备业务配置
    // 变更下联端口
    public static final String YZW_CHG_NEW_APPLICATION = "YZW_CHG_NEW_APPLICATION"; //新建申请单
    public static final String YZW_LC_PORT_CONFIG = "YZW_LC_PORT_CONFIG"; //终端下联端口配置

}
