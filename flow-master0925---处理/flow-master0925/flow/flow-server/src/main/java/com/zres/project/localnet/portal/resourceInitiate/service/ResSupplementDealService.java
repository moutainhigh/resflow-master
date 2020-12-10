package com.zres.project.localnet.portal.resourceInitiate.service;

import com.google.common.collect.Lists;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.flowdealinfo.service.CommonMethodDealWoOrderServiceInf;
import com.zres.project.localnet.portal.flowdealinfo.service.DealTacheWoOrderIntf;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf;
import com.zres.project.localnet.portal.flowdealinfo.service.entry.TacheWoOrder;
import com.zres.project.localnet.portal.resourceInitiate.data.dao.ResourceInitiateDao;
import com.zres.project.localnet.portal.util.OrderTrackOperType;
import com.zres.project.localnet.portal.util.SpringContextHolderUtil;
import com.zres.project.localnet.portal.webservice.res.BuizQueryOnTimeServiceIntf;
import com.zres.project.localnet.portal.webservice.res.BusinessArchiveServiceIntf;
import com.zres.project.localnet.portal.webservice.res.BusinessAutoAssignServiceIntf;
import com.zres.project.localnet.portal.webservice.res.ResSuspendThread;
import com.ztesoft.res.frame.core.util.CollectionUtils;
import com.ztesoft.res.frame.flow.common.exception.FlowException;
import com.ztesoft.res.frame.flow.common.service.FlowActionHandler;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResSupplementDealService implements ResSupplementDealServiceIntf {

    Logger logger = LoggerFactory.getLogger(ResSupplementDealService.class);

    @Autowired
    private CommonMethodDealWoOrderServiceInf commonMethodDealWoOrderServiceInf;
    @Autowired
    private ResourceInitiateDao resourceInitiateDao;
    @Autowired
    private OrderDealServiceIntf orderDealServiceIntf;
    @Autowired
    private BusinessAutoAssignServiceIntf businessAutoAssignServiceIntf;
    @Autowired
    private BuizQueryOnTimeServiceIntf buizQueryOnTimeServiceIntf;
    @Autowired
    private BusinessArchiveServiceIntf businessArchiveServiceIntf;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private FlowActionHandler flowActionHandler;

    @Override
    public Map<String, Object> createOrderResSup(Map<String, Object> paramsMap) throws Exception{
        logger.info("------------------------资源补录模块启流程---------------------------");
        return commonMethodDealWoOrderServiceInf.commonCreateOrder(paramsMap);
    }

    @Override
    public Map<String, Object> submitOrderResSup(Map<String, Object> params) throws Exception {
        logger.info("--------------资源补录模块----------进入工单处理方法--------------------");
        Map<String, Object> resMap = new HashMap<String, Object>();
       // 判断是否被挂起
        resMap = isResSuspend(params);
        if(!MapUtils.getBoolean(resMap,"success")){
            return resMap;
        }
        String tacheId = MapUtils.getString(params, "tacheId");
        Map<String, Object> tacheInfo = resourceInitiateDao.qryTacheInfo(tacheId);
        String tacheName = MapUtils.getString(tacheInfo, "TACHE_NAME");
        String tacheCode = MapUtils.getString(tacheInfo, "TACHE_CODE");
        TacheWoOrder tacheWoOrder = new TacheWoOrder(Integer.parseInt(tacheId),tacheCode,tacheName,params);
        String beanName = tacheWoOrder.getBeanNameByTacheCode();
        DealTacheWoOrderIntf dealTacheWoOrder = SpringContextHolderUtil.getBean(beanName);
        try{
            resMap = dealTacheWoOrder.tacheDoSomething(params);
        } catch (FlowException fe){
            fe.printStackTrace();
            logger.error("派单失败：", fe);
            logger.info("--------------资源补录模块------------工单处理失败-----------------------");
            throw fe;
        }catch (Exception e){
            e.printStackTrace();
            logger.error("派单失败：", e);
            logger.info("--------------资源补录模块------------工单处理失败-----------------------");
            throw e;
        }
        return resMap;
    }

    public Map<String,Object> isResSuspend(Map<String, Object> params){
        Map<String, Object> resMap = new HashMap<String, Object>();
        resMap.put("success",true);
        // 根据orderid查询补录单状态
        String orderId = MapUtils.getString(params,"orderId");

        Map<String,Object> map = resourceInitiateDao.qrySuppleStateByOrderId(orderId);
        if("".equals(MapUtils.getString(map,"ORDER_STATE",""))){
            map = resourceInitiateDao.qrySuppleInfoByChildOrderId(orderId);
        }
        String orderState = MapUtils.getString(map,"ORDER_STATE");
        if("10E".equals(orderState)){
            resMap.put("success",false);
            resMap.put("message","电路已被挂起，不允许提交");
        }
        return resMap;
    }

    @Override
    public Map<String, Object> firstTacheAutoSubmit(String orderId) throws Exception {
        logger.info("-------------------资源补录模块---第一个环节自动回单------------------------");
        Map<String, Object> firstTacheData = resourceInitiateDao.qryFirstTacheData(orderId, BasicCode.APPLICATION_INITIATED_LOCAL);
        String woId = MapUtils.getString(firstTacheData, "WO_ID");
        Map<String, Object> complateMap = new HashMap<>();
        complateMap.put("remark", "发起申请环节自动回单");
        complateMap.put("operStaffId", ThreadLocalInfoHolder.getLoginUser().getUserId());
        complateMap.put("orderId", orderId);
        complateMap.put("woId", woId);
        complateMap.put("action", "回单");
        complateMap.put("operType", OrderTrackOperType.OPER_TYPE_4);
        complateMap.put("tacheId", MapUtils.getString(firstTacheData, "ID"));
        return commonMethodDealWoOrderServiceInf.commonComplateWo(complateMap);
    }

    @Override
    public String qrySrvOrdIdByInstanceId(String instanceId,String systemresouce) {
        if("sec-flow-schedule-lt".equals(systemresouce)){
            return resourceInitiateDao.qrySrvOrdIdByParentInstanceId(instanceId);
        }else{
            return resourceInitiateDao.qrySrvOrdIdByInstanceId(instanceId);
        }
    }

    /**
     * 资源配置
     *
     * @param param
     * @return
     */
    @Override
    public Map<String, Object> resConfigSupplement(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap<>();
        Map<String, Object> resParam = new HashMap<>();
        // 根据资源分配子流程id查找资源补录对应version_id
        String childOrderId = MapUtils.getString(param,"orderId");
        Map suppleInfo = resourceInitiateDao.qrySuppleInfoByChildOrderId(childOrderId);
        if(MapUtils.getString(suppleInfo,"PARENT_ID","").equals("")){
            resParam.put("srvOrdId",MapUtils.getString(suppleInfo,"ID"));
        } else{
            resParam.put("srvOrdId",MapUtils.getString(suppleInfo,"PARENT_ID"));
        }
        resParam.put("srvOrderIdRes",MapUtils.getString(suppleInfo,"ID"));
        String orderState = MapUtils.getString(suppleInfo,"ORDER_STATE","");
        if("10E".equals(orderState)){
            resMap.put("success",false);
            resMap.put("message","电路已被挂起，不允许进行资源配置");
            return resMap;
        }
        resParam.put("resSupplement",true);
        resParam.put("orderId",childOrderId);
        resParam.put("tacheId",MapUtils.getString(param,"tacheId"));
        return orderDealServiceIntf.resConfig(resParam);
    }

    /**
     * 汇总归档
     *
     * @param param
     * @return
     */
    @Override
    public Map<String, Object> resArchive(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap<>();
        // 根据woId查找补录的version_id
        String srvOrderIdRes = MapUtils.getString(param,"id");
        Map<String, Object> resConfigParams = new HashMap<String, Object>();
        resConfigParams.put("flag", BasicCode.RES_SUPPLEMENT);
        resConfigParams.put("srvOrdId", srvOrderIdRes);
        // 如果成功调用过资源创建接口，
        int createNum = orderDealDao.qryInterResult(srvOrderIdRes, "ResBusinessCreate");
        if(createNum > 0){
            boolean flag = true;
            Map<String,Object> suppInfo = resourceInitiateDao.qrySuppleInfoById(srvOrderIdRes);
            // 如果是二干下发本地的补录单，不需要汇总直接归档
            if(suppInfo!=null && !"".equals(MapUtils.getString(suppInfo,"PARENT_ID",""))){
                flag = false;
            }
            // 查询上次调度 正常单的srvOrdId
            Map<String,Object> srvInfo = resourceInitiateDao.qryOriginSrvInfo(param);
            if(srvInfo!=null){
                String originSrvOrdId = MapUtils.getString(srvInfo,"SRV_ORD_ID","");
                if(!"".equals(originSrvOrdId)){
                    resConfigParams.put("originSrvOrdId", originSrvOrdId);
                }
            }
            // 查询是否成功调过资源汇总接口
            int num = orderDealDao.qryInterResult(srvOrderIdRes, "BusinessAutoAssign");
            if (flag && num < 1) {
                // 调用业务电路汇总接口
                List<String> relateListT = Lists.newArrayList();
                if(param.containsKey("systemResource") && BasicCode.SECOND.equals(MapUtils.getString(param,"systemResource",""))){
                    List<String> relateListRe = resourceInitiateDao.qryLocalInstanceId(srvOrderIdRes);
                    if(!org.springframework.util.CollectionUtils.isEmpty(relateListRe)){
                        relateListT.addAll(relateListRe);
                    }
                }
                resConfigParams.put("relaCrmOrderCodes",relateListT);
                Map retmap = businessAutoAssignServiceIntf.businessAutoAssign(resConfigParams);
                if (!"成功".equals(MapUtils.getString(retmap, "returncode"))) {
                    resMap.put("success", false);
                    resMap.put("message", "派单失败!调用资源业务电路汇总接口异常，异常原因：" + MapUtils.getString(retmap, "returndec"));
                    return resMap;
                }
            }
            // 调用资源信息实时查询接口，更新路由等信息
            Map qryMap = buizQueryOnTimeServiceIntf.buizQueryOnTime(resConfigParams);
            if (!"成功".equals(MapUtils.getString(qryMap, "returncode"))) {
                resMap.put("success", false);
                resMap.put("message",
                        "派单失败!调用资源信息查询接口异常，异常原因：" + MapUtils.getString(qryMap, "returndec"));
                return resMap;
            }
            // 查询是否成功调过资源归档接口
            int numHole = orderDealDao.qryInterResult(srvOrderIdRes, "BusinessArchive");
            if (numHole < 1) {
                Map map = businessArchiveServiceIntf.businessArchive(resConfigParams);
                if (!"成功".equals(MapUtils.getString(map, "returncode"))) {
                    resMap.put("success", false);
                    resMap.put("message", "派单失败!调用资源归档接口异常，异常原因：" + MapUtils.getString(map, "returndec"));
                    return resMap;
                }
            }
        }
        resMap.put("success", true);
        return  resMap;
    }

    /**
     * 撤销资源补录流程
     *
     * @param param
     */
    @Override
    public Map<String, Object> cancelOrderResSupplement(Map<String, Object> param) {
        String instanceId = MapUtils.getString(param,"instanceId","");
        Map<String,Object> qryparam = new HashMap<>();
        // 先跟据instance_id查询是否有未完成的补录单
        List<Map<String, Object>> list = resourceInitiateDao.queryResSuppleByInstanceId(instanceId,"10N");
        if (CollectionUtils.isEmpty(list)){
            if(param.keySet().contains("serialNumber")){
                String serialNumber= MapUtils.getString(param,"serialNumber","");
                list = resourceInitiateDao.queryResSuppleByInstanceId(serialNumber,"10N");
            }
        }
        if (!CollectionUtils.isEmpty(list)){
            String id = MapUtils.getString(list.get(0),"ID");
            String orderId = MapUtils.getString(list.get(0),"ORDER_ID");
            if(!"".equals(id)){
                qryparam.put("id",id);
                // 归档已配置资源
                resArchive(qryparam);
                // 更新补录单状态id
                resourceInitiateDao.updateResSupOrderStateById(id,"10X");
                // 撤销补录单住流程
                flowActionHandler.cancelOrder("11", orderId);
                // 根据补录单主流程orderId查到所有子流程
                qryparam.put("orderId",orderId);
                List<Map<String,Object>> orderList = resourceInitiateDao.qryChildOrder(qryparam);
                // 查到所有子流程，全部撤销
                if(!CollectionUtils.isEmpty(orderList)){
                    for(Map<String,Object> tmp :orderList){
                        flowActionHandler.cancelOrder("11", MapUtils.getString(tmp,"ORDER_ID"));
                    }
                }
            }
        }
        return null;
    }

    /**
     * 正常单来单时，资源补录单的相应操作
     * 1.如果不是拆机单，那么挂起补录单
     * 2.如果是拆机单，那么归档已配置的补录资源，并且撤销补录流程
     *
     * @param param
     * @return
     */
    @Override
    public Map<String, Object> supplementStop(Map<String, Object> param) {
        logger.info("-------------------正常单来单时，资源补录单的相应操作------------------------");
        String activeType = MapUtils.getString(param,"activeType","");
        if(BasicCode.ACTIVE_TYPE_DISMANTLE.equals(activeType)){
            // 如果是拆机，
            cancelOrderResSupplement(param);
        } else{
            ResSuspendThread resSuspendThread = new ResSuspendThread(param);
            resSuspendThread.start();
        }
        return null;
    }


}
