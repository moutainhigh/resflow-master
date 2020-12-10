package com.zres.project.localnet.portal.flowdealinfo.service;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.CheckFeedbackDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDetailsDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import org.apache.commons.collections.MapUtils;
import org.python.antlr.ast.Str;
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
 * Created by tang.huili  on 2019/3/5.
 */
@Service
public class CheckFeedbackService {
    private static final Logger log = LoggerFactory.getLogger(CheckFeedbackService.class);

    @Autowired
    private CheckFeedbackDao checkFeedbackDao;
    @Autowired
    private OrderDetailsDao orderDetailsDao;

    //查询核查反馈结果
    // params :woId,tacheId,srvOrdId
    public Map<String, Object> queryCheckInfo(Map<String,Object> params){
        Map<String,Object> retMap = new HashMap<String,Object>();
        String woId = MapUtils.getString(params,"woId");
        Map<String,Object> checkInfo = new HashMap<String,Object>();
        Map<String,Object> investmentAmountMap = new HashMap<String,Object>();
        Map<String, Object> checkInfoTwo = new HashMap<String, Object>();//核查汇总环节的资源备货情况
        try{
           //1.根据工单id查询核查反馈结果
           checkInfo =checkFeedbackDao.queryInfoByWoId(woId);
            Map<String, Object> localCheckInfo = checkFeedbackDao.queryLocalInfoByWoId(woId);
            if(localCheckInfo!=null){
                checkInfo.putAll(localCheckInfo);
            }
            if(checkInfo==null){
               String tacheId = MapUtils.getString(params,"tacheId");
               String srvOrdId = MapUtils.getString(params,"srvOrdId");
               // 核查汇总环节
               if(tacheId.equals(BasicCode.CHECK_SUMMARY)){
                   //1.首先查投资估算环节的信息
                   checkInfo = checkFeedbackDao.queryInfoByTacheId(srvOrdId,BasicCode.INVESTMENT_ESTIMATION_SCHEDU);
                   if(checkInfo == null) {
                       // 2.其次查询各专业核查的反馈信息
                       //2.1 查询是否存在各专业核查的反馈信息记录
                     /*  Map<String, Object> checkFeedbackMap = new HashMap<>();
                       checkFeedbackMap.put("srvOrdId",srvOrdId);
                       checkFeedbackMap.put("transport",BasicCode.TRANSPORT_PROFESSIONAL_CHECK); //传输专业
                       checkFeedbackMap.put("exchange",BasicCode.EXCHANGE_PROFESSIONAL_CHECK); //交换专业
                       checkFeedbackMap.put("data",BasicCode.DATA_PROFESSIONAL_VERIFICATION); //数据专业
                       checkFeedbackMap.put("other",BasicCode.OTHER_PROFESSIONAL_CHECK); //其他专业
                       int num  = checkFeedbackDao.queryNum(checkFeedbackMap);
                           if(num == 0){
                           //3.最后查核查调度环节的信息
                           checkInfo = checkFeedbackDao.queryInfoByTacheId(srvOrdId,BasicCode.CHECK_SCHEDULING);
                       } else {*/
                       checkInfo = checkFeedbackDao.queryInfoByTacheId(srvOrdId,BasicCode.CHECK_SCHEDULING);
                       if(checkInfo==null){
                       // 2.2查询资源是否满足、投资金额、建设工期
                           checkInfo = checkFeedbackDao.queryInfoBySrvOrdId(srvOrdId);
                           //查询资本地投资金额投资金额
                           investmentAmountMap = checkFeedbackDao.queryInvestmentAmountBySrvOrdId(srvOrdId);
                           checkInfo.put("Z_INVESTMENT_AMOUNT",investmentAmountMap.get("Z_INVESTMENT_AMOUNT"));
                          /* Map<String, Object> LocalInfo = checkFeedbackDao.queryLocalInfoBySrvOrdId(srvOrdId);
                           checkInfo.putAll(LocalInfo);*/
                           // 2.3拼接后的接入建设方案，资源情况
                           Map<String,Object> resMap = checkFeedbackDao.querySchmeBySrvOrdId(srvOrdId);
                           Map<String,Object> localResMap = checkFeedbackDao.queryLocalSchmeBySrvOrdId(srvOrdId);
                           //checkInfo.put("L_CONSTRUCT_SCHEME",MapUtils.getString(localResMap, "RETL"));
                           //checkInfo.put("A_CONSTRUCT_SCHEME",MapUtils.getString(resMap,"RETA"));
                            // 查询派发到本地的部门
                            //String departmet = checkFeedbackDao.qryDepartment(srvOrdId);
                            checkInfo.put("Z_CONSTRUCT_SCHEME","二干资源：\n"+MapUtils.getString(resMap,"RETZ","")+"\n"+MapUtils.getString(localResMap, "RETL"));
                           //2.4 拼接机房信息modify by wang.gang2
                           /*Map<String, Object> accessRoom = checkFeedbackDao.queryAccessRoom(srvOrdId);
                           checkInfo.put("A_ACCESS_ROOM", MapUtils.getString(accessRoom, "ROOMA"));
                           checkInfo.put("Z_ACCESS_ROOM", MapUtils.getString(accessRoom, "ROOMZ"));*/
                       }
                   }
                   checkInfoTwo = checkFeedbackDao.queryInfoByTacheIdTwo(srvOrdId,BasicCode.CHECK_SUMMARY);
                   checkInfo.putAll(checkInfoTwo);
               } else if (tacheId.equals(BasicCode.INVESTMENT_ESTIMATION_SCHEDU)) { //投资估算
                   // 投资估算环节，查询核查汇总环节提交时的信息SCHEME
                   checkInfo = checkFeedbackDao.queryInfoByTacheId(srvOrdId,BasicCode.CHECK_SUMMARY);
               }
           }
        } catch(Exception e) {
           retMap.put("success",false);
           retMap.put("message","查询核查结果异常,异常信息:" + e.getMessage());
        }
        retMap.put("success",true);
        retMap.put("data",checkInfo);
        return retMap;
    }

    // 保存或者提交时 更新核查反馈结果
    public Map updateCheckInfo(Map<String,Object> params){
        Map<String,Object> retMap = new HashMap<String,Object>();
        try{
            Map<String, Object> checkInfoMap = (Map<String, Object>) MapUtils.getObject(params, "formValue");
            String woId = MapUtils.getString(params,"woId");
            String srvOrdId = MapUtils.getString(params,"srvOrdId");
            String tacheId = MapUtils.getString(params,"tacheId");

            checkInfoMap.put("woId",woId);
            checkInfoMap.put("srvOrdId",srvOrdId);
            checkInfoMap.put("tacheId",tacheId);

            // 如果是核查汇总或者投资估算，那么按照srvOrdId和环节id去删除记录,保证这两个环节最多只有一条记录
       //     if(tacheId.equals(BasicCode.CHECK_TOTAL) || tacheId.equals(BasicCode.INVESTMENT_ESTIMATION)){
       //         checkFeedbackDao.deleteInfoBytacheId(srvOrdId,tacheId);
        //    } else {
                //先根据woId删除信息，再根据woId插入记录
            checkFeedbackDao.deleteInfoByWoId(woId);
            checkFeedbackDao.insertCheckInfo(checkInfoMap);
        }
        catch(Exception e) {
            retMap.put("success",false);
            retMap.put("message","入库核查结果异常,异常信息:" + e.getMessage());
            return retMap;
        }
        retMap.put("success",true);
        return retMap;
    }

    // 查询核查反馈历史记录
    public List<Map<String,Object>> qryCheckInfoHis (String srvOrdId){
        // 根据srvordId查询全部核查反馈信息
         return checkFeedbackDao.qryCheckInfoHis(srvOrdId);
    }
    public Map<String, Object> queryCheckFeedBackInfoByWoId(Map<String, Object> params) {
        Map<String, Object> retMap = new HashMap<String, Object>();
        String woId = MapUtils.getString(params, "woId");
        Map<String, Object> checkInfo = new HashMap<String, Object>();
//        Map<String, Object> checkInfoTwo = new HashMap<String, Object>();//核查汇总环节的资源备货情况
        boolean isInvestment = false;
        try {
            // 1.根据工单id查询核查反馈结果
            checkInfo = checkFeedbackDao.queryCheckFeedBackInfoByWoId(woId);
            String tacheId = MapUtils.getString(params, "tacheId");
            String orderId = MapUtils.getString(params, "orderId");
            // 核查汇总环节
            if (tacheId.equals(BasicCode.CHECK_SUMMARY)) {
                //  判断是 哪个的汇总  是正向 还是逆向的
                Map<String, Object> lastNode = checkFeedbackDao.qryLastNodeInfo(orderId);
                if (!MapUtils.getString(lastNode,"ID").equals(BasicCode.INVESTMENT_ESTIMATION_SCHEDU)) {
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
                        //checkInfo = setTotalCount(checkInfo,finishNodeFeedBackInfo,"A");
                        checkInfo = setTotalCount(checkInfo,finishNodeFeedBackInfo,tacheId);
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
//                            checkInfo.put("A_RES_HAVE",MapUtils.getString(checkInfoTemp,"A_RES_HAVE"));
//                            checkInfo.put("A_TOTAL_AMOUNT",MapUtils.getString(checkInfoTemp,"A_TOTAL_AMOUNT"));
//                            checkInfo.put("A_LONGEST_PERIOD",MapUtils.getString(checkInfoTemp,"A_LONGEST_PERIOD"));
                            checkInfo.put("COLLECT_RES",MapUtils.getString(checkInfoTemp,"COLLECT_RES"));
                            checkInfo.put("COLLECT_MONEY",MapUtils.getString(checkInfoTemp,"COLLECT_MONEY"));
                            checkInfo.put("COLLECT_DAY",MapUtils.getString(checkInfoTemp,"COLLECT_DAY"));
                            checkInfo.put("COLLECT_DESC",MapUtils.getString(checkInfoTemp,"COLLECT_DESC"));
                            isInvestment = true;
                        }
                    }

                }

            } else if (tacheId.equals(BasicCode.INVESTMENT_ESTIMATION_SCHEDU)) {
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
        if (checkInfo == null) {
            checkInfo = new HashMap<>();
        }

        String collectRes = MapUtils.getString(checkInfo, "COLLECT_RES");
        checkInfo.put("COLLECT_RES", StringUtils.isEmpty(collectRes)?"0":collectRes);
        String collectMoney = MapUtils.getString(checkInfo, "COLLECT_MONEY");
        checkInfo.put("COLLECT_MONEY", StringUtils.isEmpty(collectMoney)?"0":collectMoney);
        String collectDay = MapUtils.getString(checkInfo, "COLLECT_DAY");
        checkInfo.put("COLLECT_DAY", StringUtils.isEmpty(collectDay)?"0":collectDay);
        checkInfo.put("IS_INVESTMENT",isInvestment);
        // 跟本地网进行汇总
        String srvOrdId = MapUtils.getString(params, "srvOrdId");
        if (!StringUtils.isEmpty(srvOrdId)) {
            List<Map<String, Object>> totalInfo = queryLocalFeedbackInfo(srvOrdId);
            if (!CollectionUtils.isEmpty(totalInfo)) {
                checkInfo = setLocalAndSecondTotal(checkInfo,totalInfo);
            }
        }
        retMap.put("success", true);
        retMap.put("data", checkInfo);
        return retMap;
    }
    /**
     * 在汇总的基础上 再跟本地网进行汇总
     * */
    Map<String,Object>setLocalAndSecondTotal(Map<String,Object> checkInfo,List<Map<String,Object>> totalInfo) {
        String resHaveFlag = "1";
        String resHave = "1";
        double totalAmount = 0;
        int longestPeriod = 0;

        String collectRes = MapUtils.getString(checkInfo, "COLLECT_RES");
        String collectMoney = MapUtils.getString(checkInfo, "COLLECT_MONEY");
        if (!StringUtils.isEmpty(collectMoney)) {
            totalAmount = totalAmount+Double.valueOf(collectMoney);
        }
        String collectDay = MapUtils.getString(checkInfo, "COLLECT_DAY");
        if (!StringUtils.isEmpty(collectDay)) {
            if (longestPeriod < Integer.valueOf(collectDay)) {
                longestPeriod = Integer.valueOf(collectDay);
            }
        }
        if (!resHaveFlag.equals(collectRes)) {
            resHave = "0";
        }

        for (Map<String, Object> map :totalInfo) {
            String zResHave = MapUtils.getString(map,"Z_RES_HAVE");
            if (!resHaveFlag.equals(zResHave)) {
                resHave = "0";
            }
            String zTotalAmount = MapUtils.getString(map,"Z_TOTAL_AMOUNT");
            if (!StringUtils.isEmpty(zTotalAmount)) {
                totalAmount = totalAmount+Double.valueOf(zTotalAmount);
            }
            String zLongestPeriod = MapUtils.getString(map,"Z_LONGEST_PERIOD");
            if (!StringUtils.isEmpty(zLongestPeriod)) {
                if (longestPeriod < Integer.valueOf(zLongestPeriod)) {
                    longestPeriod = Integer.valueOf(zLongestPeriod);
                }
            }
        }
        checkInfo.put("COLLECT_RES",resHave);
        checkInfo.put("COLLECT_MONEY",totalAmount);
        checkInfo.put("COLLECT_DAY",longestPeriod);
        return checkInfo;
    }

    List<Map<String,Object>> queryLocalFeedbackInfo (String srvOrdId) {
        //判断是否是开通单，如果是开通单，查找上次调度的核查单信息，
        Map<String,Object> checkOrderLast = orderDetailsDao.qryLastCheck(srvOrdId);
        if(checkOrderLast!=null&&checkOrderLast.size()>0){
            String temp = MapUtils.getString(checkOrderLast,"SRV_ORD_ID","");
            if(!"".equals(temp)) {
                srvOrdId = temp;
            }
        }
        List<Map<String, Object>> retList = new ArrayList<>();
        // 根据 srvOrdId 查出 本地的 核查 orderId 集合 ，然后 查出核查汇总的列表
        List<String> orderIdList = orderDetailsDao.qryLocalOrderIdListBySrvOrderId(srvOrdId);
        List<String> woIdList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(orderIdList)) {

            for (String orderId : orderIdList) {
                // 查本地最新的汇总环节
                Map<String, Object> lastTotalNodeMap = checkFeedbackDao.qryLastTotalNode(orderId);
                if (null != lastTotalNodeMap) {
                    String woId = MapUtils.getString(lastTotalNodeMap, "WO_ID");
                    if (!StringUtils.isEmpty(woId)) {
                        woIdList.add(woId);
                    }
                }
            }
        }
//        woIdList.add("10194476");
        if (!CollectionUtils.isEmpty(woIdList)) {
            retList = orderDetailsDao.qryLocalFeedBcakInfo(woIdList);
        }
        return retList;
    }

    /*
     * 计算汇总字段值
     * */
    Map<String, Object>  setTotalCount(Map<String, Object> checkInfo,List<Map<String, Object>>finishNodeList,String teachId) {
        if (!CollectionUtils.isEmpty(finishNodeList)) {
            String resHave="1";
            String resHaveResult="1";

            double totalAmount = 0;
            int longestPeriod = 0;
            StringBuffer sb = new StringBuffer();
            for (Map<String, Object> map : finishNodeList) {
                if (!resHave.equals(MapUtils.getString(map,"COLLECT_RES"))) {
                    resHaveResult = "0";
                }
                String nodeTacheId = MapUtils.getString(map,"TACHE_ID");
                String projectOverView = MapUtils.getString(map,"COLLECT_DESC");
                String nodeName = getTacheName(nodeTacheId);
                if (!StringUtils.isEmpty(projectOverView)) {
                    sb.append(nodeName+":"+projectOverView+"\n");
                }
                totalAmount = getAllTotalAmount(map,totalAmount);
                longestPeriod = getLongestPeriod(map,longestPeriod);
            }
            if (checkInfo == null) {
                checkInfo = new HashMap<>();
            }
            checkInfo.put("COLLECT_RES",resHaveResult);
            checkInfo.put("COLLECT_MONEY",totalAmount);
            checkInfo.put("COLLECT_DAY",longestPeriod);
            checkInfo.put("COLLECT_DESC",sb);
        }
        return checkInfo;
    }
    /*
     * 根据tacheId转化名称
     * */
    String getTacheName(String tacheId) {
        if (StringUtils.isEmpty(tacheId)) {
            return "";
        }
        String tacheName = "";
        switch(tacheId){
            case BasicCode.CHECK_SCHEDULING:
                tacheName = "核查调度";
                break;
            case BasicCode.CHECK_SUMMARY:
                tacheName = "核查汇总";
                break;
            case BasicCode.INVESTMENT_ESTIMATION_SCHEDU:
                tacheName = "投资估算";
                break;
            case BasicCode.DATA_PROFESSIONAL_VERIFICATION:
                tacheName = "数据专业核查";
                break;
            case BasicCode.TRANSPORT_PROFESSIONAL_CHECK:
                tacheName = "传输专业核查";
                break;
            case BasicCode.EXCHANGE_PROFESSIONAL_CHECK:
                tacheName = "交换专业核查";
                break;
            case BasicCode.OTHER_PROFESSIONAL_CHECK:
                tacheName = "其他专业核查";
                break;
            case BasicCode.LOCAL_NETWORK_CHECK:
                tacheName = "本地网核查";
                break;
            default:
                tacheName = "其他";
        }
        return tacheName;
    }
    /*
     * 汇总金额 规则累计
     * */
    double getAllTotalAmount(Map<String, Object> map,double allTotalAmount) {
        //  测试
        double totalAmount = 0;
        String projectAmount = MapUtils.getString(map,"COLLECT_MONEY");
        if (!StringUtils.isEmpty(projectAmount)) {
            totalAmount = totalAmount+Double.valueOf(projectAmount);
        }
        totalAmount = totalAmount+allTotalAmount;
        return totalAmount;
    }
    /*
     * 汇总工期 规则 取天数最大值
     * */
    int getLongestPeriod(Map<String, Object> map,int longestPeriodMax) {
        int longestPeriod=0;
        String constructPeriodStand = MapUtils.getString(map,"COLLECT_DAY");
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
