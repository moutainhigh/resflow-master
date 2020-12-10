package com.zres.project.localnet.portal.common.util;

/*
 * 流程规格id和环节id枚举值类
 * @author guanzhao
 * @date 2020/10/28
 *
 */
public class TacheIdEnum {

    //流程规格
    public static final String YZW_LOCAL_FIBER_CHILDFLOW = "10101384"; //光纤专业子流程
    public static final String YZW_IPRAN_CHILDFLOW = "10101385"; //传输IPRAN专业子流程
    public static final String YZW_UPEQUIP_BUSICONFIG_CHILDFLOW = "10101387"; //上联设备业务配置处理子流程

    public static final String YZW_MOVE_MCPE_CONFIG_CHILD_FLOW = "10101427"; //移机业务MCPE业务配置处理子流程

    public static final String YZW_C_MOVE_FLOW_MIAN = "10101424"; //跨域业务移机主端
    public static final String YZW_C_MOVE_FLOW_COOPERATE = "10101425"; //跨域业务移机配合端
    public static final String YZW_C_MOVE_FLOW_COOPERATE_IPRAN = "10101426"; //跨域业务移机配合端IPRAN释放

    public static final String YZW_L_NEWOPEN_FLOW = "10101391"; //本地业务新开
    public static final String YZW_C_NEWOPEN_FLOW = "10101392"; //跨域业务新开
    public static final String YZW_L_MOVE_FLOW = "10101393"; //本地业务移机

    //环节枚举
    //本地业务新开
    public static final String YZW_L_CIRCUIT_DISPATCH = "1551002760"; //电路调度
    public static final String YZW_A_MCPE_INSTALL = "1551002763"; //A端MCPE安装测试
    public static final String YZW_Z_MCPE_INSTALL = "1551002770"; //Z端MCPE安装测试
    //本地移机
    public static final String YZW_L_MCPE_INSTALL = "1551002774"; //MCPE安装测试

    //跨域新开
    public static final String YZW_C_MCPE_INSTALL = "1551002780"; //MCPE安装测试



    public static final String YZW_UPEQUIP_BUSICONFIG = "1051002741"; //上联设备业务配置处理--子流程环节





}
