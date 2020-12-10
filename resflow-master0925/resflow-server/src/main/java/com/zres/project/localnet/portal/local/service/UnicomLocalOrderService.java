package com.zres.project.localnet.portal.local.service;

import com.zres.project.localnet.portal.dict.dao.UnicomSysDictDao;
import com.zres.project.localnet.portal.local.UnicomLocalOrderServiceIntf;
import com.zres.project.localnet.portal.local.dao.UnicomLocalOrderDao;
import com.zres.project.localnet.portal.local.domain.PageInfo;
import com.zres.project.localnet.portal.local.domain.UnicomLocalCount;
import com.zres.project.localnet.portal.local.domain.UnicomLocalOrderPo;
import com.zres.project.localnet.portal.local.domain.UnicomLocalVo;
import com.zres.project.localnet.portal.order.domain.ResourceInfoVo;
import com.zres.project.localnet.portal.webservice.res.BusinessQueryServiceIntf;
import com.zres.project.localnet.portal.webservice.res.ResourceQueryServiceIntf;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UnicomLocalOrderService implements UnicomLocalOrderServiceIntf {

    Logger logger = LoggerFactory.getLogger(UnicomLocalOrderService.class);

    @Autowired
    private UnicomLocalOrderDao unicomLocalOrderDao;

    @Autowired
    private UnicomSysDictDao unicomSysDictDao;
    @Autowired
    private BusinessQueryServiceIntf service;
    @Autowired
    private ResourceQueryServiceIntf rService;
    /**
     * 资源电路信息查询-（调资源查询接口）
     * @param params
     * @return
     */
    @Override
    public ResourceInfoVo queryResourceData(Map<String,Object> params) {
        List<Map<String, Object>> listMap = new ArrayList<>();
        ResourceInfoVo gomOrderVo = new ResourceInfoVo();
        List<Map<String, Object>> mapArrayList = new ArrayList<Map<String, Object>>();//返回的订单数据
        PageInfo pageInfo= new PageInfo();//分页信息
        pageInfo.setIndexSizeData(params.get("beginNum"),params.get("endNum"));
        int localCount = 0;
        try{
            //查询参数
            HashMap<String, Object> daoMap = new HashMap<String, Object>();
            daoMap.put("sortCol",params.get("sortCol"));//排序列
            daoMap.put("sortOrder",params.get("sortOrder")); //排序列顺序
            daoMap.put("startRow",pageInfo.getRowStart());//分页开始行
            daoMap.put("endRow",pageInfo.getRowEnd());//分页结束行

            listMap = rService.resourceQuery(params);
            localCount = Integer.parseInt(listMap.get(0).get("macroomCount").toString());

            if(localCount!=0){
                if(!CollectionUtils.isEmpty(listMap)){
                    mapArrayList.addAll(listMap);
                }
            }
            gomOrderVo.setResorceInfoList(mapArrayList);
            gomOrderVo.setMessage("success");

        }catch(Exception e){
            logger.error(e.getMessage());
            gomOrderVo.setMessage("fail");
            gomOrderVo.setResorceInfoList(mapArrayList);
        }
        pageInfo.setDataCount(localCount);
        gomOrderVo.setPageInfo(pageInfo);
        return gomOrderVo;

    }
    /**
     * 资源电路信息查询-（调资源查询接口）
     * @param params
     * @return
     */
    public Map<String,Object> queryResData(Map<String,Object> params) {
        Map<String, Object> resMap = new HashMap<>();
        try{
            String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
            if(params.get("type")=="res"||"res".equals(params.get("type"))){
                params.put("user_id",operStaffId);
                List<Map<String, Object>> listMap = unicomLocalOrderDao.queryResData(params);
                resMap.put("success", true);
                resMap.put("data", listMap);
            }else{
                Map<String, Object> map = service.businessQuery(params);
                if (MapUtils.getBoolean(map, "isExist")){
                    resMap.put("success", true);
                    resMap.put("data", MapUtils.getObject(map, "data"));
                    resMap.put("page", MapUtils.getString(map, "page", "1"));
                    //电路总数
                    resMap.put("circuitTotalCount", MapUtils.getString(map, "circuitTotalCount"));
                }else{
                    resMap.put("success", false);
                    resMap.put("msg", MapUtils.getString(map, "errMsg"));
                }
            }
        }catch(Exception e){
            resMap.put("success", false);
        }
        return resMap;

    }
    @Override
    public UnicomLocalVo queryLocalApplyOrderData(Map<String, Object> params) {
        //返回前台显示的所有的信息
        UnicomLocalVo unicomLocalVo = new UnicomLocalVo();
        List<UnicomLocalOrderPo> mapArrayList = new ArrayList<UnicomLocalOrderPo>();//返回的订单数据
        PageInfo pageInfo= new PageInfo();//分页信息
        pageInfo.setIndexSizeData(params.get("pageIndex"),params.get("pageSize"));
        int localCount = 0;
        try{
            //查询类型
            String queryType = (String)params.get("queryType");
            //查询参数
            HashMap<String, Object> daoMap = new HashMap<String, Object>();
            daoMap.put("queryType", queryType);
            daoMap.put("startRow",pageInfo.getRowStart());//分页开始行
            daoMap.put("endRow",pageInfo.getRowEnd());//分页结束行

            daoMap.put("orderState",params.get("orderState"));//业务订单状态10C 草稿单 10F 已完成 10N 已提交
            daoMap.put("applyOrdId",params.get("applyOrdId"));//申请单编号
            daoMap.put("applyOrdName",params.get("applyOrdName"));//申请单标题
//            daoMap.put("circuitNo",params.get("circuitNo"));//电路编号
            daoMap.put("custName",params.get("custName"));//客户名称
            daoMap.put("productType",params.get("productType"));//产品类型
            daoMap.put("actType",params.get("actType"));//动作类型
            daoMap.put("databaseType","oracle");//数据库类型
            daoMap.put("orderType",params.get("orderType"));//单据类型
            //获取用户id
            String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
            daoMap.put("operStaffId",operStaffId);
            daoMap.put("isLocalUnicom",params.get("isLocalUnicom"));//localBuild 本地订单
            //查询总记录
            if("draftOrder".equals(queryType)){//草稿单
                daoMap.put("wholeOrderState","");
//                daoMap.put("applyOrdName","");//申请单标题
                localCount = unicomLocalOrderDao.queryLocalApplyAllOrderOracleCount(daoMap);
                if(localCount!=0){
                    List<UnicomLocalOrderPo> unicomLocalOrderPosList = unicomLocalOrderDao.queryLocalApplyAllOrderOracleData(daoMap);
                    if(!CollectionUtils.isEmpty(unicomLocalOrderPosList)){
                        mapArrayList.addAll(unicomLocalOrderPosList);
                    }
                }
            }else if("allOrder".equals(queryType)){//全部申请单
                daoMap.put("wholeOrderState","10C");
                localCount = unicomLocalOrderDao.queryLocalApplyAllOrderOracleCount(daoMap);
                if(localCount!=0){
                    List<UnicomLocalOrderPo> unicomLocalOrderPosList = unicomLocalOrderDao.queryLocalApplyAllOrderOracleData(daoMap);
                    if(!CollectionUtils.isEmpty(unicomLocalOrderPosList)){
                        mapArrayList.addAll(unicomLocalOrderPosList);
                    }
                }
            }else if("completeOrder".equals(queryType)||"submitedOrder".equals(queryType)){//已提交、已完成申请单
                daoMap.put("wholeOrderState","");
                localCount = unicomLocalOrderDao.queryLocalApplyAllOrderOracleCount(daoMap);
                if(localCount!=0){
                    List<UnicomLocalOrderPo> unicomLocalOrderPosList = unicomLocalOrderDao.queryLocalApplyAllOrderOracleData(daoMap);
                    if(!CollectionUtils.isEmpty(unicomLocalOrderPosList)){
                        mapArrayList.addAll(unicomLocalOrderPosList);
                    }
                }
            }
            unicomLocalVo.setUnicomVoList(mapArrayList);
            unicomLocalVo.setMessage("success");
        }catch (Exception e){
            logger.error(e.getMessage());
            unicomLocalVo.setMessage("fail");
            unicomLocalVo.setUnicomVoList(mapArrayList);
        }
        pageInfo.setDataCount(localCount);
        unicomLocalVo.setPageInfo(pageInfo);
        return unicomLocalVo;
    }

    @Override
    public UnicomLocalVo queryLocalApplyOrderCheckData(Map<String, Object> params) {
        //返回前台显示的所有的信息
        UnicomLocalVo unicomLocalVo = new UnicomLocalVo();
        List<UnicomLocalOrderPo> mapArrayList = new ArrayList<UnicomLocalOrderPo>();//返回的订单数据
        PageInfo pageInfo= new PageInfo();//分页信息
//        pageInfo.setIndexSizeData(params.get("pageIndex"),params.get("pageSize"));
        int localCount = 0;
        try{
            //查询类型
            String queryType = (String)params.get("queryType");
            //查询参数
            HashMap<String, Object> daoMap = new HashMap<String, Object>();
            daoMap.put("queryType", queryType);
//            daoMap.put("startRow",pageInfo.getRowStart());//分页开始行
//            daoMap.put("endRow",pageInfo.getRowEnd());//分页结束行
            daoMap.put("sortname",params.get("sortname"));//排序字段
            daoMap.put("sortorder",params.get("sortorder"));//排序顺序

            daoMap.put("orderState",params.get("orderState"));//业务订单状态10C 草稿单 10F 已完成 10N 已提交
            daoMap.put("applyOrdId",params.get("applyOrdId"));//申请单编号
            daoMap.put("applyOrdName",params.get("applyOrdName"));//申请单标题
//            daoMap.put("circuitNo",params.get("circuitNo"));//电路编号
            daoMap.put("custName",params.get("custName"));//客户名称
            daoMap.put("productType",params.get("productType"));//产品类型
            daoMap.put("actType",params.get("actType"));//动作类型
            daoMap.put("databaseType","oracle");//数据库类型
            daoMap.put("orderType",params.get("orderType"));//单据类型
            //获取用户id
            String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
            if(params.get("selectType") == "1" || "1".equals(params.get("selectType"))) {
                //selectType = 1 关联核查单查询不需要对应的用户
                daoMap.put("operStaffId",operStaffId);
            }
            else {
                daoMap.put("operStaffId",operStaffId);
                daoMap.put("isLocalUnicom",params.get("isLocalUnicom"));//localBuild 本地订单
            }

            //查询总记录
            if("draftOrder".equals(queryType)){//草稿单
                daoMap.put("wholeOrderState","");
//                daoMap.put("applyOrdName","");//申请单标题
                localCount = unicomLocalOrderDao.queryLocalApplyDraftOrderCount(daoMap);
                if(localCount!=0){
                    List<UnicomLocalOrderPo> unicomLocalOrderPosList = unicomLocalOrderDao.queryLocalApplyDraftOrderData(daoMap);
                    if(!CollectionUtils.isEmpty(unicomLocalOrderPosList)){
                        mapArrayList.addAll(unicomLocalOrderPosList);
                    }
                }

            }else if("allOrder".equals(queryType)){//全部申请单
                daoMap.put("wholeOrderState","10C");
                localCount = unicomLocalOrderDao.queryLocalApplyOrderCount(daoMap);
                if(localCount!=0){
                    List<UnicomLocalOrderPo> unicomLocalOrderPosList = unicomLocalOrderDao.queryLocalApplyOrderData(daoMap);
                    if(!CollectionUtils.isEmpty(unicomLocalOrderPosList)){
                        mapArrayList.addAll(unicomLocalOrderPosList);
                    }
                }
            }else if("completeOrder".equals(queryType)||"submitedOrder".equals(queryType)){//已提交、已完成申请单
                daoMap.put("wholeOrderState","");
                //查询核查单
                if(params.get("selectType") == "1" || "1".equals(params.get("selectType"))){
                    List<UnicomLocalOrderPo> unicomLocalOrderPosList = unicomLocalOrderDao.queryApplyOrderData(daoMap);
                    localCount = unicomLocalOrderPosList.size();
                    if(!CollectionUtils.isEmpty(unicomLocalOrderPosList)){
                        mapArrayList.addAll(unicomLocalOrderPosList);
                    }
                }else{
                    localCount = unicomLocalOrderDao.queryLocalApplyOrderCount(daoMap);
                    if(localCount!=0){
                        List<UnicomLocalOrderPo> unicomLocalOrderPosList = unicomLocalOrderDao.queryLocalApplyOrderData(daoMap);
                        if(!CollectionUtils.isEmpty(unicomLocalOrderPosList)){
                            mapArrayList.addAll(unicomLocalOrderPosList);
                        }
                    }
                }
            }
            unicomLocalVo.setUnicomVoList(mapArrayList);
            unicomLocalVo.setMessage("success");
        }catch (Exception e){
            logger.error(e.getMessage());
            unicomLocalVo.setMessage("fail");
            unicomLocalVo.setUnicomVoList(mapArrayList);
        }
        pageInfo.setRowCount(localCount);
        unicomLocalVo.setPageInfo(pageInfo);
        return unicomLocalVo;
    }

    public UnicomLocalCount queryLocalApplyOrderCount(Map<String, Object> params) {
        UnicomLocalCount localCount = new UnicomLocalCount();
        try{
            //业务订单状态
            String draftOrderState = (String)params.get("draftOrderState");
            String allOrderState = (String)params.get("allOrderState");
            String completeOrderState = (String)params.get("completeOrderState");
            String submitedOrderState = (String)params.get("submitedOrderState");

            Map<String, Object> daoMap = new HashMap<String, Object>();
            daoMap.put("isLocalUnicom",params.get("isLocalUnicom"));//localBuild 本地订单
            daoMap.put("applyOrdId",params.get("applyOrdId"));//申请单号
            daoMap.put("applyOrdName",params.get("applyOrdName"));//申请单标题
//            daoMap.put("circuitNo",params.get("circuitNo"));//电路代号
            daoMap.put("custName",params.get("custName"));//客户名称
            daoMap.put("productType",params.get("productType"));//产品类型
            daoMap.put("actType",params.get("actType"));//动作类型
            daoMap.put("orderType",params.get("orderType"));//单据类型
            daoMap.put("databaseType","oracle");//数据库类型
            //获取用户id
            String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
            daoMap.put("operStaffId",operStaffId);

            //全部申请单
            if(!StringUtils.hasText(allOrderState)){//由于查询全部是不需要设置状态，故前端传空值才查询
                daoMap.put("orderState","");
                daoMap.put("wholeOrderState","10C");
                int allCount = unicomLocalOrderDao.queryLocalApplyAllOrderOracleCount(daoMap);
                localCount.setAllOrderCount(allCount);
            }
            //已完成申请单
            if(StringUtils.hasText(completeOrderState)){
                daoMap.put("orderState",completeOrderState);
                daoMap.put("wholeOrderState","");
                int allCount = unicomLocalOrderDao.queryLocalApplyAllOrderOracleCount(daoMap);
                localCount.setCompleteOrderCount(allCount);
            }
            //已提交申请单
            if(StringUtils.hasText(submitedOrderState)){
                daoMap.put("orderState",submitedOrderState);
                daoMap.put("wholeOrderState","");
                int subCount = unicomLocalOrderDao.queryLocalApplyAllOrderOracleCount(daoMap);
                localCount.setSubmitedOrderCount(subCount);
            }
            //草稿单
            if(StringUtils.hasText(draftOrderState)){
                daoMap.put("orderState",draftOrderState);
                daoMap.put("wholeOrderState","");
                int draCount = unicomLocalOrderDao.queryLocalApplyAllOrderOracleCount(daoMap);
                localCount.setDraftOrderCount(draCount);
            }

            localCount.setMessage("success");
        }catch(Exception e){
            localCount.setMessage("fail");
            logger.error(e.getMessage());
        }
        return localCount;

    }


    public List<Map<String,Object>> queryLocalExportApplyOrderData(Map params) {
        //获取用户id
//        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
//        params.put("operStaffId",operStaffId);
        List<Map<String,Object>> unicomLocalOrderPos = unicomLocalOrderDao.queryLocalExportApplyOrderData(params);
        return unicomLocalOrderPos;
    }

    public List<Map<String,Object>> queryLocalExportDraftOrderData(Map params) {
        //获取用户id
//        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
//        params.put("operStaffId",operStaffId);
        List<Map<String,Object>> unicomLocalOrderPos = unicomLocalOrderDao.queryLocalExportDraftOrderData(params);
        return unicomLocalOrderPos;
    }

    @Override
    public Map<String, Object> queryStockCircuitInfo(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap<>();
        try{
            //查询存量电路
            Map<String, Object> stockMap = service.businessQuery(param);
            if (MapUtils.getBoolean(stockMap, "isExist")){
                //封装一个page对象，用来前端grid数据填充
                Map<String, Object> pageMap = new HashMap<>();
                pageMap.put("page", MapUtils.getObject(stockMap,"page"));
                pageMap.put("rows", MapUtils.getObject(stockMap, "data"));
                pageMap.put("records", MapUtils.getObject(stockMap, "circuitTotalCount"));
                resMap.put("success", true);
                resMap.put("msg", "电路查询成功");
                resMap.put("data", pageMap);
            }else{
                resMap.put("success", false);
                resMap.put("msg", MapUtils.getString(stockMap, "errMsg"));
            }
        }
        catch (Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>电路查询失败：" + e.getMessage());
            resMap.put("success", false);
            resMap.put("msg", "电路查询失败，请联系管理员：" + e.getMessage());
        }
        return resMap;
    }

    public Map<String, Object> queryCountByInstanceId(Map<String, Object> param){
        Map<String, Object> resMap = new HashMap<>();
        try{
           int num = unicomLocalOrderDao.queryCountByInstanceId(param);
           resMap.put("success", true);
           resMap.put("data", num);
        }catch(Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>根据业务号码查询电路信息表发生异常：" + e.getMessage());
            e.printStackTrace();
            resMap.put("success", false);
        }
        return resMap;
    }

    public Map<String, Object> queryCircuitInfoBySrvOrdId(Map<String, Object> param){
        Map<String, Object> resMap = new HashMap<>();
        try{
            Map<String, Object> map = unicomLocalOrderDao.queryCircuitInfoBySrvOrdId(param);
            resMap.put("success", true);
            resMap.put("data", map);
        }catch(Exception e){
            e.printStackTrace();
            resMap.put("success", false);
        }
        return resMap;
    }

    public Map<String, Object> queryRouteInfo(Map<String, Object> param){
        return service.queryRouteInfo(param);
    }

}
