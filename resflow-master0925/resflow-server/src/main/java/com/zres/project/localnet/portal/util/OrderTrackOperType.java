package com.zres.project.localnet.portal.util;

public class OrderTrackOperType {

	//操作类型
	public static final String OPER_TYPE_1 = "1"; //派发
	public static final String OPER_TYPE_2 = "2"; //签收
	public static final String OPER_TYPE_3 = "3"; //释放签收
	public static final String OPER_TYPE_4 = "4"; //回单
	public static final String OPER_TYPE_5 = "5"; //退单
	public static final String OPER_TYPE_6 = "6"; //转派
	public static final String OPER_TYPE_7 = "7"; //加派
	public static final String OPER_TYPE_8 = "8"; //挂起
	public static final String OPER_TYPE_9 = "9"; //解挂
	public static final String OPER_TYPE_10 = "10"; //启子流程
	public static final String OPER_TYPE_11 = "11"; //回退
	public static final String OPER_TYPE_12 = "12"; //作废
	public static final String OPER_TYPE_13 = "13"; //申请单发起
	public static final String OPER_TYPE_14 = "14"; //保存调单
	public static final String OPER_TYPE_15 = "15"; //抄送
	public static final String OPER_TYPE_16 = "16"; //退单重发
	public static final String OPER_TYPE_17 = "17"; //集客下发
	public static final String OPER_TYPE_18 = "18"; //补单




	//工单状态
	public static final String WO_ORDER_STATE_1 = "290000001"; //未派发
	public static final String WO_ORDER_STATE_2 = "290000002"; //执行中
	public static final String WO_ORDER_STATE_3 = "290000003"; //被签出
	public static final String WO_ORDER_STATE_4 = "290000004"; //已提交
	public static final String WO_ORDER_STATE_5 = "290000005"; //已作废
	public static final String WO_ORDER_STATE_6 = "290000006"; //主动驳回
	public static final String WO_ORDER_STATE_7 = "290000007"; //被动驳回
	public static final String WO_ORDER_STATE_8 = "290000008"; //挂起
	public static final String WO_ORDER_STATE_9 = "290000009"; //待解挂
	public static final String WO_ORDER_STATE_10 = "290000110"; //已启子流程
	public static final String WO_ORDER_STATE_11 = "290000111"; //等一干通知 OrderTrackOperType.WO_ORDER_STATE_11
	public static final String WO_ORDER_STATE_12 = "290000112"; //等待本地网处理
    public static final String WO_ORDER_STATE_13 = "290000113"; //等待二干调度处理
    public static final String WO_ORDER_STATE_14 = "290000114"; //草稿箱处理

	//定单状态
	public static final String ORDER_STATE_1 = "200000001"; //未启流程
	public static final String ORDER_STATE_2 = "200000002"; //已启流程
	public static final String ORDER_STATE_3 = "200000003"; //退到服开流程
	public static final String ORDER_STATE_4 = "200000004"; //已结束
	public static final String ORDER_STATE_5 = "200000005"; //已撤销

	/**
	 * 核查单--核查汇总退单标识
	 */
	public static final String DISPATCH_CHECK = "dispatchCheck"; //核查调度
	public static final String SPECIAL_CHECK = "specialCheck"; //专业核查

}
