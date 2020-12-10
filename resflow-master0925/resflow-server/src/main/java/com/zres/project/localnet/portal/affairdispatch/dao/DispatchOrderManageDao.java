package com.zres.project.localnet.portal.affairdispatch.dao;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 事务调单管理Dao
 *
 * @author PangHao
 * @date 2019/4/22 : 18:46
 */
@Repository
public interface DispatchOrderManageDao {

    /**
     * 新增实物调单记录（GOM_BDW_AFFAIR_DISPATCH_ORDER）
     *
     * @param affairOrderInfo 事务调单信息
     * @author PangHao
     * @date 2019/4/24 : 10:14
     */
    void insertAffairDispatchOrder(Map<String, Object> affairOrderInfo);

    /**
     * 批量新增事务调单抄送人记录
     *
     * @param noticeRecordList 抄送人记录集合
     * @author PangHao
     * @date 2019/4/24 : 15:05
     */
    void batchInsertNoticeRecord(ArrayList<Map<String, Object>> noticeRecordList);

    /**
     * 得到工单信息
     *
     * @param orderId   定单标识
     * @param tacheCode 环节编码
     * @return 工单信息
     * @author PangHao
     * @date 2019/4/24 : 16:11
     */
    List<Map<String, Object>> getWorkOrder(@Param("orderId") String orderId, @Param("tacheCode") String tacheCode);


    /**
     * 批量插入订单文件记录
     *
     * @param woId      工单ID
     * @param orderId   定单ID
     * @param fileInfos 文件信息集合
     * @author PangHao
     * @date 2019/4/25 : 9:30
     */
    void batchInsertOrderFileRecord(@Param("woId") String woId,
                                    @Param("orderId") String orderId,
                                    @Param("fileInfos") List fileInfos);

    /**
     * 生成事务调单编码
     *
     * @param ariaCode 地区编码
     * @return 订单编码
     * @author PangHao
     * @date 2019/4/25 : 11:28
     */
    String getAffairDisCode(@Param("ariaCode") String ariaCode);

    /**
     * 查询事务调单条数
     *
     * @param params
     * @return
     */
    int countAffairOrderList(Map<String, Object> params);

    /**
     * 查询事务调单列表
     *
     * @param params
     * @return
     */
    List<Map<String, Object>> selectAffairOrderList(Map<String, Object> params);

    /**
     * 通过id得到事务调单信息
     *
     * @param id 事务调单记录标识
     * @return 单条事务调单记录
     * @author PangHao
     * @date 2019/4/26 : 14:26
     */
    Map<String, Object> getAffairDisOrderById(String id);

    /**
     * 通过子流程订单ID得到父流程工单集合
     *
     * @param orderId 子流程定单Id
     * @return 父流程工单集合
     * @author PangHao
     * @date 2019/4/26 : 15:45
     */
    List<Map<String, Object>> getParentWorkOrderList(String orderId);

    /**
     * 得到所有子流程定单
     *
     * @param parentOrderId 父流程定单ID
     * @return 子流程定单列表
     * @author PangHao
     * @date 2019/4/28 : 11:39
     */
    List<Map<String, Object>> getSonOrderList(String parentOrderId);

    /**
     * 修改事务调单状态
     *
     * @param affairId 事务调单ID
     * @param state    事务调单状态
     * @param staffId  操作人标识
     * @author PangHao
     * @date 2019/4/28 : 17:55
     */
    void modifyAffairDisState(@Param("affairId") String affairId,
                              @Param("state") String state,
                              @Param("staffId") String staffId);

    /**
     * 得到序列
     *
     * @param seqName SEQ_NAME.NEXTVAL
     * @return 序列值
     * @author PangHao
     * @date 2019/4/29 : 10:36
     */
    Integer querySequence(@Param("seqName") String seqName);

    /**
     * 更新事务调单记录定单id
     *
     * @param affairId 事务调单标识
     * @param orderId  定单ID
     * @author PangHao
     * @date 2019/4/29 : 10:44
     */
    void updateAffairOrderId(@Param("affairId") String affairId,
                             @Param("orderId") String orderId);

    /**
     * 更新事务调单信息（全量）
     *
     * @param affairDisOrderInfo 事务调单信息
     * @author PangHao
     * @date 2019/4/29 : 10:48
     */
    void updateAffairDispatchOrder(Map<String, Object> affairDisOrderInfo);

    //得到事务调单附件(作废)
   /* List<Map<String, Object>> getAffairDispatchFile(@Param("affairId") String affairId,
                                                    @Param("tacheCode") String tacheCode);*/

    /**
     * 作废抄送人记录
     *
     * @param affairId    事务调单标识
     * @param noticeState 通知记录状态 ,1:作废
     * @param noticeType  通知记录类型
     * @author PangHao
     * @date 2019/4/30 : 10:13
     */
    void obsoleteNoticeRecord(@Param("affairId") String affairId,
                              @Param("noticeState") String noticeState,
                              @Param("noticeType") String noticeType);

    /**
     * 更改工单描述信息
     *
     * @param woId   工单id
     * @param remark 工单描述
     * @author PangHao
     * @date 2019/4/30 : 10:58
     */
    void modifyWoOrderRemark(@Param("woId") String woId,
                             @Param("remark") String remark);


    /**
     * 获取事务调单抄送人
     *
     * @param affairId
     * @return
     */
    List<Map<String, Object>> getAffairNoticeStaffArray(String affairId);

    /**
     * 事务处理工单条数
     *
     * @param params
     * @return
     */
    int countDisponeAffairOrderList(Map<String, Object> params);

    /**
     * 查询事务处理工单
     *
     * @param params
     * @return
     */
    List<Map<String, Object>> selectDisponseAffairOrderList(Map<String, Object> params);

    /**
     * 获取事务调单审核信息
     *
     * @param affairId
     * @return
     */
    List<Map<String, Object>> getAffairCheckInfoArray(@Param("affairId") String affairId);

    /**
     * 获取工单附件
     *
     * @param woId
     * @return
     */
    List<Map<String, Object>> getAffairWoAccessoryFile(@Param("woId") String woId);

    /**
     * 获取事务调单处理进度列表
     *
     * @param affairId
     * @return
     */
    List<Map<String, Object>> getAffairDisposeList(@Param("affairId") String affairId);

    /**
     * 获取抄送事务单条数
     *
     * @param params
     * @return
     */
    int countCopyAffairOrderList(Map<String, Object> params);

    /**
     * 获取抄送事务单
     *
     * @param params
     * @return
     */
    List<Map<String, Object>> selectCopyAffairOrderList(Map<String, Object> params);

    /**
     * 获取历史事务单工单数量
     *
     * @param params
     * @return
     */
    int countHistoryAffairOrderList(Map<String, Object> params);

    /**
     * 获取历史事务单工单
     *
     * @param params
     * @return
     */
    List<Map<String, Object>> selectHistoryAffairOrderList(Map<String, Object> params);

    /**
     * 获取子流程工单信息
     *
     * @param affairId
     * @return
     */
    List<Map<String, Object>> getChildWorkOrderList(@Param("affairId") String affairId);

    /**
     * 获取事务调单子单处理进度
     *
     * @param orderId 子流程定单ID
     * @return
     */
    List<Map<String, Object>> getChildAffairDisposeList(@Param("orderId") String orderId);

    /**
     * 草稿箱事务单条数
     *
     * @param params
     * @return
     */
    int countDraftAffairOrderList(Map<String, Object> params);

    /**
     * 草稿箱事务单列表
     *
     * @param params
     * @return
     */
    List<Map<String, Object>> selectDraftAffairOrderList(Map<String, Object> params);

    /**
     * 得到当前登录人地区编码
     *
     * @param operatorId 当前登录人标识
     * @return 地区编码
     * @author PangHao
     * @date 2019/5/22 : 20:30
     */
    String getAriaCode(@Param("operatorId")String operatorId);

    /**
     * 我的发起事务单条数
     * 
     * @param params
     * @return
     */
    int countStartAffairOrderList(Map<String, Object> params);

    /**
     * 我的发起事务单列表
     * 
     * @param params
     * @return
     */
    List<Map<String, Object>> selectStartAffairOrderList(Map<String, Object> params);

    /**
     * 查询事务调单类型枚举值
     * @return
     */
    public List<Map<String, Object>> qryAffairDispatchOrderType();


    /*
     * 通过数据库函数生成事物调单编号
     * @author ren.jiahang
     * @date 2019/3/20 15:38
     * @param orgId
     * @return java.lang.String
     */
    String queryDispatchOrderNumFunction(String orgId);
    /**properties
     * 查询用户所在的组织
     * @param userId
     * @return
     */
    String queryOrgId(String  userId);
}
