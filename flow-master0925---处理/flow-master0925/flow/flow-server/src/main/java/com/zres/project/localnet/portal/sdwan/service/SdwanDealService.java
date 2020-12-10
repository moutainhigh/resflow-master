package com.zres.project.localnet.portal.sdwan.service;

import com.alibaba.fastjson.JSON;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.flowdealinfo.service.DealTacheWoOrderIntf;
import com.zres.project.localnet.portal.flowdealinfo.service.entry.TacheWoOrder;
import com.zres.project.localnet.portal.listener.util.EnmuValueUtil;
import com.zres.project.localnet.portal.sdwan.dao.SdwanDealDao;
import com.zres.project.localnet.portal.util.SpringContextHolderUtil;
import com.zres.project.localnet.portal.webservice.product.sdwan.OTSFeedBackServiceIntf;
import com.zres.project.localnet.portal.webservice.product.sdwan.RollBackServiceIntf;
import com.zres.project.localnet.portal.webservice.product.sdwan.TerminalSynchServicesIntf;
import com.ztesoft.res.frame.flow.common.exception.FlowException;
import com.ztesoft.res.frame.flow.common.service.FlowActionHandler;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by tang.huili on 2020/3/20.
 */
@Service
public class SdwanDealService implements SdwanDealServiceIntf {
    @Autowired
    private SdwanDealDao sdwanDealDao;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private TerminalSynchServicesIntf terminalSynchServicesIntf;
    @Autowired
    private OTSFeedBackServiceIntf otsFeedBackServiceIntf;
    @Autowired
    private FlowActionHandler flowActionHandler;
    @Autowired
    private RollBackServiceIntf rollBackServiceIntf;

    Logger logger = LoggerFactory.getLogger(SdwanDealService.class);

    @Override
    public List<Map<String, Object>> queryCircuitInfo(Map<String, Object> params) {
        List<Map<String, Object>> mapList = sdwanDealDao.queryCircuitAttr(MapUtils.getString(params,"srvOrdId"));
        return mapList;
    }

    @Override
    public List<Map<String, Object>> queryEnum(Map<String, Object> params) {
            List<Map<String, Object>> maps = sdwanDealDao.queryEnum(params);
            Map<String, Object> enumCodeMap = new HashMap<String, Object>(); //异步查询后前台需要enumCode返回值，直接回传回去。
            enumCodeMap.put("enumCode", MapUtils.getString(params,"enumType"));
            maps.add(enumCodeMap);
            return maps;
    }

    /**
     * 保存wan信息
     *
     * @param params
     */
    @Override
    public Map<String, Object> saveWanInfo(Map<String, Object> params) throws Exception {
        Map<String,Object> retMap = new HashMap<>();
        retMap.put("success",true);
        /**
         *  查询是否存在attrCode
         *  如果不存在，判断attrValue不为空，然后新增，
         *  如果存在，判断attrValue不为空，，然后修改数据，
         *
         */
        String srvOrdId = MapUtils.getString(params,"srvOrdId");
        Map<String,Object> qryParam = new HashMap<>();
        qryParam.put("srvOrdId",srvOrdId);
        Set<String> set = new HashSet<>();
        set.add("userAcct");
        set.add("userPwd");
        set.add("WANType");
        set.add("IPAddr");
        set.add("gatewayAddr");
        for(String tmp:set){
            qryParam.put("attrCode",tmp);
            Map<String,Object> attrMap = sdwanDealDao.qryAttrInfoId(qryParam);
            String attrValue = MapUtils.getString(params,tmp,"");
            if(MapUtils.isEmpty(attrMap)){
                // 如果数据库没记录，那么新增
                if(!attrValue.equals("")){
                    Map<String,Object> insertMap = new HashMap<>();
                    insertMap.put("srv_ord_id",srvOrdId);
                    insertMap.put("attr_action","0");
                    insertMap.put("attr_code", tmp);
                    insertMap.put("attr_name", "");
                    insertMap.put("attr_value",attrValue);
                    insertMap.put("attr_value_name", "");
                    insertMap.put("sourse", "local");
                    sdwanDealDao.insertSrvAttrInfo(insertMap);
                }
            } else {
                //modify  by wang.gang2 DHCP 除了wan口配置类型其余传空  要把原信息覆盖清空（张涛确认过的）
                qryParam.put("attrId",MapUtils.getString(attrMap,"ATTR_INFO_ID"));
                qryParam.put("attrValue",attrValue);
                sdwanDealDao.updateSrvAttr(qryParam);
            }
        }
        // 调用接口：通过OSS工单反馈给SDWAN平台。
        params.put("STATUS","5"); // 5：失败
        params.put("remark","1"); // wan配置失败
        retMap = otsFeedBackServiceIntf.feedBackOTS(params);
        if(MapUtils.getBoolean(retMap,"success",false)){
            // 挂起电路 srv_ord表状态改为4E
            orderDealDao.updateSrvOrdState(srvOrdId, "4E");
        }
        return retMap;
    }

    /**
     * 查询电路的wan配置信息
     *
     * @param params
     * @return
     */
    @Override
    public List<Map<String, Object>> queryWanInfo(Map<String, Object> params) {
        List<Map<String, Object>> mapList = sdwanDealDao.queryCircuitAttr(MapUtils.getString(params,"srvOrdId"));
        return mapList;
    }


    /**
     * 环节回单方法
     *
     * @param params
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> submitOrderSdwan(Map<String, Object> params) throws Exception {
        /**
         *          String woId = MapUtils.getString(tacheDoSomeMap, "woId");
         String orderId = MapUtils.getString(tacheDoSomeMap, "orderId");
         String tacheId = MapUtils.getString(tacheDoSomeMap, "tacheId");
         String remark = MapUtils.getString(tacheDoSomeMap, "remark");
         */
        logger.info("--------------sdwan模块----------进入工单处理方法--------------------");
        Map<String, Object> resMap = new HashMap<String, Object>();
        try{
            // 要处理的电路信息
            String circuitDataStr = MapUtils.getString(params, "circuitData");
            String srvOrdIdStr = "";
            String remark = MapUtils.getString(params,"remark","");
            String btnFlag = MapUtils.getString(params,"btnFlag","");
            String psId = MapUtils.getString(params,"psId","");
            List<Object> circuitDatalist = JSON.parseArray(circuitDataStr);
            List<String> orderIdList = new ArrayList<String>();
            for (Object object : circuitDatalist) {
                Map<String, Object> circuitDataMap = (Map<String, Object>) object;// 取出list里面的值转为map
                String woId = MapUtils.getString(circuitDataMap, "WO_ID");
                String orderId = MapUtils.getString(circuitDataMap, "ORDER_ID");
                String tacheId = MapUtils.getString(circuitDataMap, "TACHE_ID");
                Map<String,Object> subMap = new HashMap<>();
                subMap.putAll(params);
                subMap.put("woId",woId);
                subMap.put("orderId",orderId);
                subMap.put("tacheId",tacheId);
                subMap.put("remark",remark);
                subMap.put("psId",psId);
                subMap.put("srvOrdId",MapUtils.getString(circuitDataMap, "SRV_ORD_ID"));
                // 只有拆机流程的第一个环节业务派单才能退单到sdwan平台
                if("rollBackOrder".equals(btnFlag)&&BasicCode.BUSINESS_SENT.equals(tacheId)){
                    resMap = rollBackOrder(subMap);
                } else {
                    resMap = submitOrder(subMap);
                }

                if(!MapUtils.getBoolean(resMap,"success")){
                    return resMap;
                }
            }
        }catch (Exception e){
             e.printStackTrace();
             logger.error("派单失败：", e);
             logger.info("--------------sdwan模块------------工单处理失败-----------------------");
             throw e;
        }
        return resMap;

    }

    /**
     * 退单到sdwan平台
     * @param params
     * @return
     */
    public Map<String, Object> rollBackOrder(Map<String, Object> params) {
        Map<String, Object> resMap = new HashMap<String, Object>();
        try{
            // 调用退单接口
            String psId = MapUtils.getString(params, "psId");
            String type = "";
            if(BasicCode.SDWAN_OPEN_FLOW.equals(psId)){
                type = "0";
            } else if(BasicCode.SDWAN_DISMANTLE_FLOW.equals(psId)){
                type = "1";
            }
            params.put("type",type);
            resMap = rollBackServiceIntf.rollBackOTS(params);
            if(!MapUtils.getBoolean(resMap,"success")){
                return resMap;
            }
            String operStaffId = "-1";
            if (ThreadLocalInfoHolder.getLoginUser() != null) {
                operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
            }
            String orderId = MapUtils.getString(params, "orderId","");
            boolean flagCancelOrder = flowActionHandler.cancelOrder(operStaffId, orderId);
            if (flagCancelOrder) {
                // 调用撤单成功，将当前工单状态修改成作废
                Map<String, Object> updateMap = new HashMap<String, Object>();
                updateMap.put("woState", "290000005");
                updateMap.put("stateDate", new java.sql.Date(new java.util.Date().getTime()));
                updateMap.put("woID", MapUtils.getString(params, "woId",""));
                updateMap.put("staffId", operStaffId);
                orderDealDao.updateWoOrderState(updateMap);
            }
            resMap.put("success",true);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("派单失败：", e);
            logger.info("--------------sdwan模块------------工单处理失败-----------------------");
            throw e;
        }
        return resMap;
    }

    /**
     * 单条电路提交
     */
    public Map<String, Object> submitOrder(Map<String, Object> params) throws Exception {
        /**
         *          String woId = MapUtils.getString(tacheDoSomeMap, "woId");
         String orderId = MapUtils.getString(tacheDoSomeMap, "orderId");
         String tacheId = MapUtils.getString(tacheDoSomeMap, "tacheId");
         String remark = MapUtils.getString(tacheDoSomeMap, "remark");
         */
        logger.info("--------------sdwan模块----------进入工单处理方法--------------------");
        Map<String, Object> resMap = new HashMap<String, Object>();
        String tacheId = MapUtils.getString(params, "tacheId");
        // 提交工单前的操作
        resMap = beforeSubmit(params);
        if(!MapUtils.getBoolean(resMap,"success")){
            return resMap;
        }
        Map<String, Object> tacheInfo = sdwanDealDao.qryTacheInfo(tacheId);
        String tacheName = MapUtils.getString(tacheInfo, "TACHE_NAME");
        String tacheCode = MapUtils.getString(tacheInfo, "TACHE_CODE");
        // 处理线条参数
        params = dealOperAttr(params,tacheInfo);
        TacheWoOrder tacheWoOrder = new TacheWoOrder(Integer.parseInt(tacheId),tacheCode,tacheName,params);
        String beanName = tacheWoOrder.getBeanNameByTacheCodeWithOperAttr();
        DealTacheWoOrderIntf dealTacheWoOrder = SpringContextHolderUtil.getBean(beanName);
        try{
            resMap = dealTacheWoOrder.tacheDoSomething(params);
        } catch (FlowException fe){
            fe.printStackTrace();
            logger.error("派单失败：", fe);
            logger.info("--------------sdwan模块------------工单处理失败-----------------------");
            throw fe;
        }catch (Exception e){
            e.printStackTrace();
            logger.error("派单失败：", e);
            logger.info("--------------sdwan模块------------工单处理失败-----------------------");
            throw e;
        }
        return resMap;
    }

    /**
     * 处理线条参数
     * @param params
     * @param tacheInfo
     * @return
     */
    public Map<String,Object> dealOperAttr(Map<String,Object> params,Map<String, Object> tacheInfo){
        Map<String,Object> operAttrsVal = new HashMap<>();
    //    String tacheName = MapUtils.getString(tacheInfo, "TACHE_NAME");
        String tacheCode = MapUtils.getString(tacheInfo, "TACHE_CODE");
        String btnFlag = MapUtils.getString(params,"btnFlag","");
        String psId = MapUtils.getString(params,"psId","");
        Set<String> tacheIdSet = new HashSet<>();
        tacheIdSet.add(EnmuValueUtil.TERNIMAL_DELIVERY);
        tacheIdSet.add(EnmuValueUtil.PICK_UP_TERMINAL_BOX);
        // 环节退单处理
        if(tacheIdSet.contains(tacheCode)){
            if("submit".equals(btnFlag)){
                operAttrsVal.put("isReturn",false);
            }else if("rollBackOrder".equals(btnFlag)){
                operAttrsVal.put("isReturn",true);
            }
        }
        if("submit".equals(btnFlag)){
            String dispObjSign = "_DISP_OBJ";
            // 运维派单环节需要指定派发人
            if( EnmuValueUtil.OPS_SENT.equals(tacheCode)){
                String objType = MapUtils.getString(params,"objType","");
                String objId = MapUtils.getString(params,"objId","");
                String value = objType +"_J!G@F_" + objId;
                if(BasicCode.SDWAN_OPEN_FLOW.equals(psId)){
                    operAttrsVal.put(EnmuValueUtil.TERNIMAL_DELIVERY+dispObjSign,value);
                } else if(BasicCode.SDWAN_DISMANTLE_FLOW.equals(psId)){
                    operAttrsVal.put(EnmuValueUtil.PICK_UP_TERMINAL_BOX+dispObjSign,value);
                }
            }
            // 等待sdwan平台反馈 环节需要指定派发人 -2000
            if(EnmuValueUtil.TERNIMAL_DELIVERY.equals(tacheCode)){
                operAttrsVal.put(EnmuValueUtil.WAIT_SDWAN_FEEDBACK+dispObjSign,"260000003_J!G@F_-2000");
            }
        }
        params.put("operAttrsVal",operAttrsVal);
        return params;
    }
    /**
     * 提交工单前的操作
     * @param params
     */
    private Map<String,Object> beforeSubmit(Map<String, Object> params) {
        Map<String, Object> resMap = new HashMap<String, Object>();
        resMap.put("success",true);
        String tacheId = MapUtils.getString(params, "tacheId");
     //   String woId = MapUtils.getString(params, "woId");
        String srvOrdId = MapUtils.getString(params, "srvOrdId");
        String btnFlag = MapUtils.getString(params, "btnFlag");

        Map<String,Object> resParam = new HashMap<>();
        resParam.put("srvOrdId",srvOrdId);
        resParam.put("remark",MapUtils.getString(params, "remark"));
        if("submit".equals(btnFlag)){
            // 终端出库及上门安装
            if(BasicCode.TERNIMAL_DELIVERY.equals(tacheId)){
                // 终端信息同步接口调用超过十分钟才可以提交工单
                resMap = terminalLimit(srvOrdId);
                if(!MapUtils.getBoolean(resMap,"success")){
                    return resMap;
                }
                // 调用接口
                resParam.put("STATUS","2"); // 2：上门安装完成（O域通知SDWAN）
                resMap = otsFeedBackServiceIntf.feedBackOTS(resParam);
            } else if(BasicCode.PICK_UP_TERMINAL_BOX.equals(tacheId) || BasicCode.BUSINESS_TEST.equals(tacheId)){
                // 调用接口
                resParam.put("STATUS","4"); // 4：工单完成(O域通知SDWAN)
                resMap = otsFeedBackServiceIntf.feedBackOTS(resParam);
                if(!MapUtils.getBoolean(resMap,"success")){
                    return resMap;
                }
                // srv_ord表状态改为10F
                orderDealDao.updateSrvOrdState(srvOrdId, "10F");
            }
        }
        return resMap;
    }

    /**
     *  终端信息同步接口调用超过十分钟才可以提交工单
      */
    public Map terminalLimit(String srvOrdId){
        Map<String, Object> resMap = new HashMap<String, Object>();

        Map<String,Object> qryParam = new HashMap<>();
        qryParam.put("srvOrdId",srvOrdId);
        qryParam.put("attrCode","terminalSync");
        Map<String,Object> attrMap = sdwanDealDao.qryAttrInfoId(qryParam);
        if(attrMap!=null && !attrMap.isEmpty()){
            String dateStr = MapUtils.getString(attrMap,"CREATE_DATE");
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Date dbDate = format.parse(dateStr);
                long min = getDatePoor(new Date(), dbDate);
                if(min <10){
                    resMap.put("success",false);
                    resMap.put("message","终端信息同步保存超过十分钟才可以提交工单");
                    return resMap;
                }else{
                    resMap.put("success",true);
                    resMap.put("message","成功");
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }else{
            resMap.put("success",false);
            resMap.put("message","请先填写终端信息");
        }
        return resMap;
    }

    /**
     * 计算时间差
     * @param endDate
     * @param nowDate
     * @return
     */
    public long getDatePoor(Date endDate, Date nowDate) {

        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少天
     //   long day = diff / nd;
        // 计算差多少小时
     //   long hour = diff / nh;
        // 计算差多少分钟
        long min = diff / nm;

        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return min;
    }

    /**
     * 等待sdwan反馈环节自动提交
     *
     * @param orderId
     * @return
     */
    @Override
    public Map<String, Object> tacheAutoSubmit(String orderId) throws Exception {
        return null;
    }

    /**
     * 保存终端信息
     *
     * @param params
     */
    @Override
    public Map<String, Object> saveDeviceInfo(Map<String, Object> params) throws Exception {
       Map<String,Object> retMap = new HashMap<>();
        retMap.put("success",true);
        /**
         *  查询是否存在attrCode
         *  如果不存在，判断attrValue不为空，然后新增，
         *  如果存在，判断数据来源，如果是local,判断attrValue不为空，，然后修改数据，
          */
        String srvOrdId = MapUtils.getString(params,"srvOrdId");
        Map<String,Object> qryParam = new HashMap<>();
        qryParam.put("srvOrdId",srvOrdId);
        Set<String> set = new HashSet<>();
        set.add("deviceModel");
        set.add("deviceFactory");
        set.add("BSN");
        for(String tmp:set){
            qryParam.put("attrCode",tmp);
            Map<String,Object> attrMap = sdwanDealDao.qryAttrInfoId(qryParam);
            String attrValue = MapUtils.getString(params,tmp,"");
            if(attrMap == null || attrMap.isEmpty()){
                // 如果数据库没记录，那么新增
                if(!attrValue.equals("")){
                    Map<String,Object> insertMap = new HashMap<>();
                    insertMap.put("srv_ord_id",srvOrdId);
                    insertMap.put("attr_action","0");
                    insertMap.put("attr_code", tmp);
                    insertMap.put("attr_name", "");
                    insertMap.put("attr_value",attrValue);
                    insertMap.put("attr_value_name", "");
                    insertMap.put("sourse", "local");
                    sdwanDealDao.insertSrvAttrInfo(insertMap);
                }
            } else if(MapUtils.getString(attrMap,"SOURSE","").equals("local")
                    || MapUtils.getString(attrMap,"ATTR_VALUE","").equals("")){
                // 如果是本地存入的，或者值为空的，就更新最新值入库
                if(!attrValue.equals("")){
                    qryParam.put("attrId",MapUtils.getString(attrMap,"ATTR_INFO_ID"));
                    qryParam.put("attrValue",attrValue);
                    sdwanDealDao.updateSrvAttr(qryParam);
                }
            }
        }
        // 调用接口：通过OSS工单反馈给SDWAN平台。
        Map<String,Object> interfaceParam = new HashMap<>();
        interfaceParam.put("srvOrdId",srvOrdId);
        retMap = terminalSynchServicesIntf.terminalSynchronization(interfaceParam);
        return retMap;
    }


}
