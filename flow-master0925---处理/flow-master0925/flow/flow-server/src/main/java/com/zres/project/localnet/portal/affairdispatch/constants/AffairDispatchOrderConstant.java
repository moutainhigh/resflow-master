package com.zres.project.localnet.portal.affairdispatch.constants;

/**
 * 事务调单环节相关常量
 *
 * @author PangHao
 * @date 2019/4/24 : 15:52
 */
public interface AffairDispatchOrderConstant {

    /**
     * 发起事务（环节编码）
     */
    String INITIATE_AFFAIR = "INITIATE_AFFAIR";
    /**
     * 事务审核（环节编码）
     */
    String AFFAIR_REVIEW = "AFFAIR_REVIEW";
    /**
     * 事务处理（环节编码）
     */
    String AFFAIR_PROCESS = "AFFAIR_PROCESS";
    /**
     * 事务确认（环节编码）
     */
    String AFFAIR_CONFIROM = "AFFAIR_CONFIROM";
    /**
     * 子流程等待环节（环节编码）
     */
    String AFFAIR_CONFIROM_WAIT = "AFFAIR_CONFIROM_WAIT";
    /**
     * 事务审查
     */
    String AFFAIR_REVIEW_CHECK = "AFFAIR_REVIEW_CHECK";


    /**
     * 草稿箱(事务调单状态)
     */
    String AFFAIR_DIS_STATE_1 = "290000112";
    /**
     * 发起事务(事务调单状态)
     */
    String AFFAIR_DIS_STATE_2 = "290000113";
    /**
     * 事务审核驳回(事务调单状态)
     */
    String AFFAIR_DIS_STATE_3 = "290000114";
    /**
     * 事务审核(事务调单状态)
     */
    String AFFAIR_DIS_STATE_4 = "290000115";
    /**
     * 事务处理中(事务调单状态)
     */
    String AFFAIR_DIS_STATE_5 = "290000116";
    /**
     * 事务处理驳回(事务调单状态)
     */
    String AFFAIR_DIS_STATE_6 = "290000117";
    /**
     * 事务已处理
     */
    String AFFAIR_DIS_STATE_7 = "290000118";
    /**
     * 事务确认
     */
    String AFFAIR_DIS_STATE_8 = "290000119";
    /**
     * 已完成
     */
    String AFFAIR_DIS_STATE_9 = "290000120";
    /**
     * 已关闭
     */
    String AFFAIR_DIS_STATE_10 = "290000121";

    /**
     * 生效（抄送人记录状态-STAT）
     */
    String ORDER_NOTICE_STATE_1 = "0";
    /**
     * 作废（抄送人记录状态-STAT）
     */
    String ORDER_NOTICE_STATE_2 = "1";
    /**
     * 事务调单（抄送记录类型-NOTICE_TYPE）
     */
    String ORDER_NOTICE_TYPE_7 = "10G";


    /**
     * 是否审核—是
     */
    String IS_CHECK = "0";
    /**
     * 是否审核-否
     */
    String NO_CHECK = "1";

}
