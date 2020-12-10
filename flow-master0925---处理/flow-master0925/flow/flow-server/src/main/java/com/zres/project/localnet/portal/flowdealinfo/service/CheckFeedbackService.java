package com.zres.project.localnet.portal.flowdealinfo.service;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.CheckFeedbackDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.ztesoft.res.frame.flow.common.lock.impl.DatabaseLock;
import com.ztesoft.res.frame.flow.common.lock.intf.DistributeLock;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tang.huili on 2019/3/5.
 */
@Service
public class CheckFeedbackService {
    private static final Logger log = LoggerFactory.getLogger(CheckFeedbackService.class);

    @Autowired
    private CheckFeedbackDao checkFeedbackDao;
    @Autowired
    private OrderDealDao orderDealDao;
    //查询核查反馈结果
    // params :woId,tacheId,srvOrdId
    public Map<String, Object> queryCheckInfo(Map<String, Object> params) {
        Map<String, Object> retMap = new HashMap<String, Object>();
        String woId = MapUtils.getString(params, "woId");
        Map<String, Object> checkInfo = new HashMap<String, Object>();
        Map<String, Object> checkInfoTwo = new HashMap<String, Object>();//核查汇总环节的资源备货情况
        try {
            // 1.根据工单id查询核查反馈结果
            checkInfo = checkFeedbackDao.queryInfoByWoId(woId);
            if (checkInfo == null) {
                String tacheId = MapUtils.getString(params, "tacheId");
                String sysResource = MapUtils.getString(params, "sysResource");
                String srvOrdId = MapUtils.getString(params, "srvOrdId");
                if("second-schedule-lt".equals(sysResource)){
                    srvOrdId=MapUtils.getString(params, "RELATE_INFO_ID"); //如果是二干来单srvordId去关联表的id作为srvOrdId
                }
                // 核查汇总环节
                if (tacheId.equals(BasicCode.CHECK_TOTAL)) {
                    // 1.首先查投资估算环节的信息
                    //A_RES_PROVIDE,A_RES_ACCESS,A_EQUIP_READY,Z_RES_PROVIDE,Z_RES_ACCESS,Z_EQUIP_READY
                    checkInfo = checkFeedbackDao.queryInfoByTacheId(srvOrdId, BasicCode.INVESTMENT_ESTIMATION);
                    if (checkInfo == null) {
                        // 2.其次查询各专业核查的反馈信息
                        // 2.1 查询是否存在各专业核查的反馈信息记录
                        int num = checkFeedbackDao.queryNum(srvOrdId);
                        if (num == 0) {
                            // 3.最后查核查调度环节的信息
                            checkInfo = checkFeedbackDao.queryInfoByTacheId(srvOrdId, BasicCode.CHECK_DISPATCH);
                        } else {
                            // 2.2查询资源是否满足、投资金额、建设工期
                            checkInfo = checkFeedbackDao.queryInfoBySrvOrdId(srvOrdId);
                            // 2.3拼接后的接入建设方案
                            Map<String, Object> resMap = checkFeedbackDao.querySchmeBySrvOrdId(srvOrdId);
                            checkInfo.put("A_CONSTRUCT_SCHEME", MapUtils.getString(resMap, "RETA"));
                            checkInfo.put("Z_CONSTRUCT_SCHEME", MapUtils.getString(resMap, "RETZ"));
                            // 2.4 拼接机房信息modify by wang.gang2
                            Map<String, Object> accessRoom = checkFeedbackDao.queryAccessRoom(srvOrdId);
                            checkInfo.put("A_ACCESS_ROOM", MapUtils.getString(accessRoom, "ROOMA"));
                            checkInfo.put("Z_ACCESS_ROOM", MapUtils.getString(accessRoom, "ROOMZ"));
                        }
                    }
                    checkInfoTwo = checkFeedbackDao.queryInfoByTacheIdTwo(srvOrdId,BasicCode.CHECK_TOTAL);
                    if(checkInfoTwo != null){
                        checkInfo.putAll(checkInfoTwo);
                    }
                    // 核查汇总环节查询下发工建系统反馈的费用信息
                    Map<String,Object> engineeringInfo = checkFeedbackDao.qryEnginInfo(srvOrdId);
                    if(engineeringInfo != null){
                        checkInfo.putAll(engineeringInfo);
                    }
                } else if (tacheId.equals(BasicCode.INVESTMENT_ESTIMATION)) {
                    // 投资估算环节，查询核查汇总环节提交时的信息SCHEME
                    checkInfo = checkFeedbackDao.queryInfoByTacheId(srvOrdId, BasicCode.CHECK_TOTAL);
                }
            }
        } catch (Exception e) {
            retMap.put("success", false);
            retMap.put("message", "查询核查结果异常,异常信息:" + e.getMessage());
        }
        retMap.put("success", true);
        retMap.put("data", checkInfo);
        return retMap;
    }

    // 保存或者提交时 更新核查反馈结果
    public Map updateCheckInfo(Map<String, Object> params) {
        Map<String, Object> retMap = new HashMap<String, Object>();
        String woId = MapUtils.getString(params, "woId");
        DistributeLock lock = new DatabaseLock(woId);
        try {
            lock.lock();
            log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>加锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            // Object formValueObject = MapUtils.getObject(params, "formValue");
            // Map<String, Object> checkInfoMap = JSON.parseObject(formValueObject.toString(), Map.class);
            Map<String, Object> checkInfoMap = (Map<String, Object>) MapUtils.getObject(params, "formValue");
            // 互联网产品三个字段 特殊处理
            checkInfoMap.put("ACCESS_CIR_TYPE", MapUtils.getString(checkInfoMap,"Z_ACCESS_CIR_TYPE",""));
            checkInfoMap.put("OTHER_ACE_CIR_TYPE", MapUtils.getString(checkInfoMap,"Z_OTHER_ACE_CIR_TYPE",""));
            checkInfoMap.put("UPLINK_NODE_PORT", MapUtils.getString(checkInfoMap,"Z_UPLINK_NODE_PORT",""));
            String srvOrdId = MapUtils.getString(params, "srvOrdId");
            String tacheId = MapUtils.getString(params, "tacheId");
            String areaId = MapUtils.getString(params, "areaId");
            String areaZ = MapUtils.getString(params, "areaZ");
            String areaA = MapUtils.getString(params, "areaA");
            checkInfoMap.put("woId", woId);
            checkInfoMap.put("srvOrdId", srvOrdId);
            checkInfoMap.put("tacheId", tacheId);
            checkInfoMap.put("areaZ", areaZ);
            checkInfoMap.put("areaA", areaA);
            checkInfoMap.put("areaId", areaId);
            if(tacheId.equals(BasicCode.CHECK_TOTAL) || tacheId.equals(BasicCode.INVESTMENT_ESTIMATION)){
                checkFeedbackDao.updateStateBytacheId(srvOrdId,tacheId);
           }
            // 如果是核查汇总或者投资估算，那么按照srvOrdId和环节id去删除记录,保证这两个环节最多只有一条记录
       //     if(tacheId.equals(BasicCode.CHECK_TOTAL) || tacheId.equals(BasicCode.INVESTMENT_ESTIMATION)){
       //         checkFeedbackDao.deleteInfoBytacheId(srvOrdId,tacheId);
        //    } else {
                //先根据woId删除信息，再根据woId插入记录
                checkFeedbackDao.deleteInfoByWoId(woId);
        //    }
            checkFeedbackDao.insertCheckInfo(checkInfoMap);
          /*  boolean judgeIsSecondDra = orderDealDao.judgeIsSecondDra(woId); //判断是否二干单子
            String feedBackSign = "";
            String signA = "";
            String signZ = "";
            if(judgeIsSecondDra||(areaId==null||("").equals(areaId))){//二干来单或者区域id为空显示ALL
                feedBackSign = "ALL";
                signA = "A";
                signZ = "Z";
            }else {
                // 查询当前工单页面上展示的是什么信息（A/Z/还是两端都展示）
                String areaName = checkFeedbackDao.qryAreaName(areaId);
                if(areaName.equals(areaA) && areaName.equals(areaZ)){
                    feedBackSign = "ALL";
                    signA = "A";
                    signZ = "Z";
                } else if(areaName.equals(areaA)){
                    feedBackSign = "A";
                    signA = "A";
                } else if(areaName.equals(areaZ)){
                    feedBackSign = "Z";
                    signZ = "Z";
                } else {
                    feedBackSign = "ALL";
                    signA = "A";
                    signZ = "Z";
                }
            }
            checkInfoMap.put("feedBackSign", feedBackSign);
            checkInfoMap.put("signA", signA);
            checkInfoMap.put("signZ", signZ);
            if ("ALL".equals(feedBackSign)) {
                checkFeedbackDao.insertCheckInfo(checkInfoMap);
            } else if ("A".equals(feedBackSign)) {
                checkFeedbackDao.insertCheckInfoA(checkInfoMap);
            } else if ("Z".equals(feedBackSign)) {
                checkFeedbackDao.insertCheckInfoZ(checkInfoMap);
            }*/

        } catch (Exception e) {
            retMap.put("success", false);
            retMap.put("message", "入库核查结果异常,异常信息:" + e.getMessage());
            return retMap;
        }  finally {
            lock.unlock();
            log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>finally主动解锁>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        }
        retMap.put("success", true);
        return retMap;
    }

//
//    回显方法：根据当前工单id查询反馈信息表   GOM_BDW_CHECK_FEEDBACK   where wo_id=?
//    特殊：这次操作不包含汇总字段
//    如果当前环节是核查汇总环节，那么查询上个已完成的即核查汇总环节 的值）
//            --根据工单查全部
//    select ut.id,ut.tache_name,w.WO_STATE,w.disp_obj_tye,w.disp_obj_id,w.*,ut.* from gom_wo w
//    LEFT JOIN            GOM_PS_2_WO_S ws        ON            ws.id = w.ps_id
//    LEFT JOIN            UOS_TACHE ut        ON            ut.id = ws.tache_id
//    where  wo_id=？--order_id in(10168916)
//--WO_STATE=’‘290000004     --工单已完成
//--UOS_TACHE.TACHE_CODE,或者UOS_TACHE.id判断是哪个环节
//    select * from UOS_TACHE
//--UOS_TACHE.id  ==java  BasicCode.java
//
//            汇总的三个字段
//    当前环节是核查汇总环节，需要新增三个汇总的字段，
//    查询上个已完成的环节是否是投资估算，是投资估算环节，直接查询投资估算的汇总字段
//    不是投资估算，则查询出已完成的专业核查环节或者核查调度环节的工单id,找出新增字段，进行汇总，得到汇总字段的值

    // 查询核查反馈历史记录
    public List<Map<String, Object>> qryCheckInfoHis(String srvOrdId) {
        // 根据srvordId查询全部核查反馈信息
        return checkFeedbackDao.qryCheckInfoHis(srvOrdId);
    }
    public Map<String, Object> queryCheckFeedBackInfoByWoId(Map<String, Object> params) {
        Map<String, Object> retMap = new HashMap<String, Object>();
        String woId = MapUtils.getString(params, "woId");
        Map<String, Object> checkInfo = new HashMap<String, Object>();
//        Map<String, Object> checkInfoTwo = new HashMap<String, Object>();//核查汇总环节的资源备货情况
        try {
            // 1.根据工单id查询核查反馈结果
            checkInfo = checkFeedbackDao.queryCheckFeedBackInfoByWoId(woId);
            String tacheId = MapUtils.getString(params, "tacheId");
            String orderId = MapUtils.getString(params, "orderId");
            // 核查汇总环节
            if (tacheId.equals(BasicCode.CHECK_TOTAL)) {
                //  判断是 哪个的汇总  是正向 还是逆向的
                Map<String, Object> lastNode = checkFeedbackDao.qryLastNodeInfo(orderId);
                 if (!MapUtils.getString(lastNode,"ID").equals(BasicCode.INVESTMENT_ESTIMATION)) {
                     // 判断三个汇总字段是否为空
                     if (null == checkInfo) {
                         List<Map<String, Object>>finishNodeList = checkFeedbackDao.qryFinishNodeList(orderId);
                         List<String> woIdList = new ArrayList<>();
                         if (!CollectionUtils.isEmpty(finishNodeList)) {
                             for (Map<String, Object> m : finishNodeList) {
                                 woIdList.add(MapUtils.getString(m,"WO_ID"));
                             }
                         }
                         List<Map<String, Object>> finishNodeFeedBackInfo = checkFeedbackDao.queryCheckFeedBackInfoByWoIdList(woIdList);
                         checkInfo = setTotalCount(checkInfo,finishNodeFeedBackInfo,"A");
                         checkInfo = setTotalCount(checkInfo,finishNodeFeedBackInfo,"Z");
                     }
                 } else {
                     //上一个环节是估投资环节, 查上一次的AZ端字段，并且查投资估算的汇总字段
                     if (null == checkInfo) {
                         Map<String, Object> lastTotalNodeMap = checkFeedbackDao.qryLastTotalNode(orderId);
                         if (null != lastTotalNodeMap) {
                              woId = MapUtils.getString(lastTotalNodeMap,"WO_ID");
                             checkInfo = checkFeedbackDao.queryCheckFeedBackInfoByWoId(woId);
                             // 这边查投资估算的汇总字段
                             woId = MapUtils.getString(lastNode,"WO_ID");
                            Map<String,Object> checkInfoTemp = checkFeedbackDao.queryCheckFeedBackInfoByWoId(woId);
                             checkInfo.put("A_RES_HAVE",MapUtils.getString(checkInfoTemp,"A_RES_HAVE"));
                             checkInfo.put("A_TOTAL_AMOUNT",MapUtils.getString(checkInfoTemp,"A_TOTAL_AMOUNT"));
                             checkInfo.put("A_LONGEST_PERIOD",MapUtils.getString(checkInfoTemp,"A_LONGEST_PERIOD"));
                             checkInfo.put("Z_RES_HAVE",MapUtils.getString(checkInfoTemp,"Z_RES_HAVE"));
                             checkInfo.put("Z_TOTAL_AMOUNT",MapUtils.getString(checkInfoTemp,"Z_TOTAL_AMOUNT"));
                             checkInfo.put("Z_LONGEST_PERIOD",MapUtils.getString(checkInfoTemp,"Z_LONGEST_PERIOD"));
                         }
                     }

                 }

            } else if (tacheId.equals(BasicCode.INVESTMENT_ESTIMATION)) {
                // 投资估算
                // 查上一次的汇总数据
                if (null == checkInfo) {
                    Map<String, Object> lastTotalNodeMap = checkFeedbackDao.qryLastTotalNode(orderId);
                    if (null != lastTotalNodeMap) {
                        woId = String.valueOf(lastTotalNodeMap.get("WO_ID"));
                        checkInfo = checkFeedbackDao.queryCheckFeedBackInfoByWoId(woId);
                    }
                }
            }
        } catch (Exception e) {
            retMap.put("success", false);
            retMap.put("message", "查询核查结果异常,异常信息:" + e.getMessage());
        }
        retMap.put("success", true);
        retMap.put("data", checkInfo);
        return retMap;
    }
    /*
     * 计算汇总字段值
     * */
    Map<String, Object>  setTotalCount(Map<String, Object> checkInfo,List<Map<String, Object>>finishNodeList,String type) {
        if (!CollectionUtils.isEmpty(finishNodeList)) {
            String resHave="1";
            double totalAmount = 0;
            int longestPeriod = 0;
            for (Map<String, Object> map : finishNodeList) {
                if (!resHave.equals(MapUtils.getString(map,type+"_RES_PROVIDE_STAND"))) {
                    resHave = "0";
                }
                totalAmount = getAllTotalAmount(map,type,totalAmount);
                longestPeriod = getLongestPeriod(map,type,longestPeriod);
            }
            if (checkInfo == null) {
                checkInfo = new HashMap<>();
            }
            checkInfo.put(type+"_RES_HAVE",resHave);
            checkInfo.put(type+"_TOTAL_AMOUNT",totalAmount);
            checkInfo.put(type+"_LONGEST_PERIOD",longestPeriod);
        }
        return checkInfo;
    }
    /*
    * 汇总金额 规则累计
    * */
    double getAllTotalAmount(Map<String, Object> map,String type,double allTotalAmount) {
        //  测试
        double totalAmount = 0;
        String boardAmount = MapUtils.getString(map,type+"_BOARD_AMOUNT");
        if (!StringUtils.isEmpty(boardAmount)) {
            totalAmount = totalAmount+Integer.valueOf(boardAmount);
        }
        String transAmount = MapUtils.getString(map,type+"_TRANS_AMOUNT");
        if (!StringUtils.isEmpty(transAmount)) {
            totalAmount = totalAmount+Integer.valueOf(transAmount);
        }
        String opticalAmount = MapUtils.getString(map,type+"_OPTICAL_AMOUNT");
        if (!StringUtils.isEmpty(opticalAmount)) {
            totalAmount = totalAmount+Integer.valueOf(opticalAmount);
        }
        String projectAmount = MapUtils.getString(map,type+"_PROJECT_AMOUNT");
        if (!StringUtils.isEmpty(projectAmount)) {
            totalAmount = totalAmount+Integer.valueOf(projectAmount);
        }
        totalAmount = totalAmount+allTotalAmount;
        return totalAmount;
    }
    /*
     * 汇总工期 规则 取天数最大值
     * */
    int getLongestPeriod(Map<String, Object> map,String type,int longestPeriodMax) {
        int longestPeriod=0;
        String boardPeriod = MapUtils.getString(map,type+"_BOARD_PERIOD");
        if (!StringUtils.isEmpty(boardPeriod)) {
            longestPeriod = Integer.valueOf(boardPeriod);
        }
        String transPeriod = MapUtils.getString(map,type+"_TRANS_PERIOD");
        if (!StringUtils.isEmpty(transPeriod)) {
            if (longestPeriod < Integer.valueOf(transPeriod)) {
                longestPeriod = Integer.valueOf(transPeriod);
            }
        }
        String opticalPeriod = MapUtils.getString(map,type+"_OPTICAL_PERIOD");
        if (!StringUtils.isEmpty(opticalPeriod)) {
            if (longestPeriod < Integer.valueOf(opticalPeriod)) {
                longestPeriod = Integer.valueOf(opticalPeriod);
            }
        }
        String constructPeriodStand = MapUtils.getString(map,type+"_CONSTRUCT_PERIOD_STAND");
        if (!StringUtils.isEmpty(constructPeriodStand)) {
            if (longestPeriod < Integer.valueOf(constructPeriodStand)) {
                longestPeriod = Integer.valueOf(constructPeriodStand);
            }
        }
        if (longestPeriod < longestPeriodMax) {
            return longestPeriodMax;
        }
        return longestPeriod;
    }

}
