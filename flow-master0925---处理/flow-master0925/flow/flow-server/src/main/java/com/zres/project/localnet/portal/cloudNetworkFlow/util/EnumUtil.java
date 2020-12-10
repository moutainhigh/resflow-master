package com.zres.project.localnet.portal.cloudNetworkFlow.util;

/**
 * 静态枚举
 *
 * @author caomm on 2020/11/9
 */
public class EnumUtil {
    //云组网核查流程环节枚举
    public static final String YZW_CHECK_DISPATCH = "YZW_CHECK_DISPATCH"; //核查调度
    public static final String YZW_CHECK_WAITING = "YZW_CHECK_WAITING"; //核查流程子流程等待环节

    //云组网核查流程专业
    public static final String IPRAN = "IPRAN"; //IPRAN专业
    public static final String FIBER = "FIBER"; //光纤专业
    public static final String TERMINAL_BOX = "TERMINAL_BOX"; //终端盒

    //云组网核查流程子流程
    public static final String YZW_FIBER_RES_CHECK_CHILD_FLOW = "10101404"; //云组网光纤资源核查子流程
    public static final String YZW_UPLINK_DEVICE_CHILD_FLOW = "10101405"; //云组网IPRAN资源核查子流程
    public static final String YZW_MCPE_CHECK_CHILD_FLOW = "10101403"; //云组网MCPE终端盒资源核查子流程
}