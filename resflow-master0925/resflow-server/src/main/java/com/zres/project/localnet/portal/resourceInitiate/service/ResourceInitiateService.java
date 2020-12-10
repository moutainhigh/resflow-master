package com.zres.project.localnet.portal.resourceInitiate.service;

import com.zres.project.localnet.portal.resourceInitiate.data.dao.ResourceInitiateDao;
import com.zres.project.localnet.portal.webservice.res.BusinessQueryServiceIntf;
import com.ztesoft.res.frame.core.util.CollectionUtils;
import com.ztesoft.res.frame.core.util.MapUtil;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import org.apache.commons.collections4.MapUtils;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 资源补录实现类
 *
 * @author caomm on 2020-01-08
 */
@Service
public class ResourceInitiateService implements ResourceInitiateServiceIntf{

    Logger logger = LoggerFactory.getLogger(ResourceInitiateService.class);

    @Autowired
    private BusinessQueryServiceIntf businessQueryServiceIntf;
    @Autowired
    private ResourceInitiateDao resourceInitiateDao;
    @Autowired
    private ResSupplementDealServiceIntf resSupplementDealServiceIntf;

    @Override
    public Map<String, Object> queyCircuitInfoFromResource(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap<>();
        try{
            //查询存量电路
            Map<String, Object> stockMap = businessQueryServiceIntf.businessQuery(param);
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

    @Override
    public Map<String, Object> initCircuitInfo(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap<>();
        try{
            List<Map<String, Object>> list = resourceInitiateDao.initCircuitInfo(param);
            resMap.put("success", true);
            resMap.put("data", list);
        }
        catch(Exception e){
            resMap.put("success", false);
            resMap.put("msg", "初始化电路信息失败！");
        }
        return resMap;
    }

    @Override
    public Map<String, Object> queryDeptInfo() {
        Map<String, Object> resMap = new HashMap<>();
        try {
            //获取当前登录人
            String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
            //查询当前登录人所在省份ID
            Map<String, Object> orgMap = resourceInitiateDao.getOrgInfoByStaffId(operStaffId);
            //查询对应省份地市信息
            List<Map<String, Object>> cityList = resourceInitiateDao.queryDeptInfo(MapUtils.getString(orgMap, "ORG_ID"));
            resMap.put("success", true);
            resMap.put("data", cityList);
        }
        catch(Exception e) {
            e.printStackTrace();
            resMap.put("success", false);
            resMap.put("msg", "查询城市部门信息失败！");
        }
        return resMap;
    }

    @Override
    public Map<String, Object> initSpecialtyInfo(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap<>();
        try{
//            //获取当前登录人
//            String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
//            //查询当前登录人所在省份ID
//            Map<String, Object> orgMap = resourceInitiateDao.getOrgInfoByStaffId(operStaffId);
//            param.put("proIdRef", MapUtils.getString(orgMap, "ORG_ID"));
            List<Map<String, Object>> list = resourceInitiateDao.initSpecialtyInfo(param);
            resMap.put("success", true);
            resMap.put("data", list);
        }
        catch(Exception e){
            resMap.put("success", false);
            resMap.put("msg", "查询专业信息失败！");
        }
        return resMap;
    }

    /*@Override
    public Map<String, Object> startResourceInitiateFlow(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap<>();
        try{
            startFlow(param);
            resMap.put("success", true);
            resMap.put("msg", "资源补录起流程成功！");
        }catch(Exception e){
            e.printStackTrace();
            resMap.put("success", false);
            resMap.put("msg", "资源补录起流程失败！");
        }
        return resMap;
    }*/

    @Transactional(rollbackFor = Exception.class)
    public void startFlow(Map<String, Object> param) throws Exception{
        SimpleDateFormat fmt = new SimpleDateFormat("YYYYMMDD");
        //关键信息入库
        Map<String, Object> params = (Map<String, Object>) MapUtils.getMap(param, "gridData");
        Map<String, Object> formData = (Map<String, Object>) MapUtils.getMap(param, "formData");
        //区域ID
        //获取当前登录人
        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        //查询当前登录人所在省份ID
        Map<String, Object> orgMap = resourceInitiateDao.getOrgInfoByStaffId(operStaffId);
        params.put("regionId", MapUtils.getString(formData, "regionId"));
        params.put("resRegionId", MapUtils.getString(orgMap, "CRM_REGION"));
        params.put("custId", MapUtils.getString(formData, "custId"));
        params.put("linkMan", MapUtils.getString(formData, "linkMan"));
        params.put("address", MapUtils.getString(formData, "address"));
        //是否派发本地网
        params.put("isLocal", MapUtils.getString(formData, "isSendLocalNet"));
        List<String> specialtyList = (List<String>) MapUtils.getObject(formData, "specialtyInfo");
        String specialtyStr = "";
        for (String s:specialtyList
             ) {
            if ("".equals(specialtyStr)){
                specialtyStr = s;
            }else{
                specialtyStr = specialtyStr + "," + s;
            }
        }
        //如果是存量数据，对产品类型进行转换
//        if ("1".equals(MapUtils.getString(params, "isoldres"))){ //存量数据
//            Map<String, Object> serviceMap = resourceInitiateDao.transformServiceId(MapUtils.getString(params, "serviceId"));
//            if (MapUtils.isNotEmpty(serviceMap)){
//                params.put("serviceId", MapUtils.getString(serviceMap, ""));
//            }
//        }
        params.put("specialtyConfig", specialtyStr);
        params.put("remark", MapUtils.getMap(formData, "remark"));
        params.put("localConf", MapUtils.getString(formData, "cityId"));
        params.put("orderState", "10N");//处理中...
        resourceInitiateDao.insertResourceInitiateInfo(params);
        //调用流程接口启流程
        Map<String, Object> flowParam = new HashMap<>();
        //查询流程规格
        String flowId = resourceInitiateDao.queryFlowId();
        flowParam.put("ordPsid", flowId);
        flowParam.put("AREA", "350002000000000042766408");//全国
        flowParam.put("ORDER_TITLE", "ZYBL-" + fmt.format(new Date()) + "-" + MapUtils.getString(params, "accNbr"));
        flowParam.put("ORDER_CONTENT", MapUtils.getString(formData, "remark", ""));
        flowParam.put("PRODUCT_TYPE", MapUtils.getString(params, "serviceId"));
        //flowParam.put("REGION_ID", MapUtils.getString(orgMap, "ORG_ID"));
        Map<String, String> attr = new HashMap<>();
        attr.put("REGION_ID", MapUtils.getString(orgMap, "ORG_ID")); //区域
        flowParam.put("attr", attr);
        Map<String, Object> orderMap = resSupplementDealServiceIntf.createOrderResSup(flowParam);
        //将order_id和versionId更新到表中
        orderMap.put("id", MapUtils.getString(params, "id"));
        //orderMap.put("versionId", MapUtils.getString(params,"prodInstId" + "SupplementVersion" + MapUtils.getString(params, "id")));
        orderMap.put("versionId", "SupV" + MapUtils.getString(params, "id"));
        int  updateFlag = resourceInitiateDao.updateOrderInfoByResourceSupplementId(orderMap);
        if (updateFlag > 0){
            //调用方法第一个环节自动流转
            resSupplementDealServiceIntf.firstTacheAutoSubmit(MapUtils.getString(orderMap, "orderId"));
        }
    }

    public Map<String, Object> querySrvOrdInfoByInstanceId(Map<String, Object> param){
        Map<String, Object> resMap = new HashMap<>();
        try{
            List<Map<String, Object>> list = resourceInitiateDao.querySrvOrdInfoByInstanceId(param);
            if (!com.ztesoft.res.frame.core.util.CollectionUtils.isEmpty(list)){
                resMap.put("data", list);
            }else{
                resMap.put("data", Lists.newArrayList());
            }
            resMap.put("success", true);
        }catch(Exception e){
            e.printStackTrace();
            resMap.put("success", false);
            resMap.put("msg", "判断该电路是否有在途单发生异常！");
        }
        return resMap;
    }

    @Override
    public Map<String, Object> queryResourceInitiateInfoByInstanceId(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap<>();
        try{
            List<Map<String, Object>> list = resourceInitiateDao.queryResourceInitiateInfoByInstanceId(param);
            if (!CollectionUtils.isEmpty(list)){
                resMap.put("flag", false);
                for (Map<String, Object> map : list){
                    if (!"10F".equals(MapUtil.getString(map, "ORDER_STATE"))){
                        resMap.put("flag", true);//有未完成的资源补录单
                    }
                }
            }else{
                resMap.put("flag", false); //没有未完成的资源补录单
            }
            resMap.put("success", true);
        }catch(Exception e){
            e.printStackTrace();
            resMap.put("success", false);
            resMap.put("msg", "查询该电路是否有未完成的资源补录单发生异常！");
        }
        return resMap;
    }
    /**
     * 查询二干补录资源信息
     *
     * @param srvOrdId (资源补录表主键id)
     * @return
     */
    @Override
    public List<Map<String, Object>> queryResourceOrderInfoSec(String srvOrdId) {
        Map<String,Object> paramsMap = new HashMap<>();
        paramsMap.put("srvOrdId",new String[]{srvOrdId});
        List<Map<String,Object>> list = resourceInitiateDao.queryResourceOrderInfoSec(paramsMap);
        return list;
    }

    /**
     * 查询本地补录资源信息
     *
     * @param srvOrdId (资源补录表主键id)
     * @return
     */
    @Override
    public List<Map<String, Object>> queryResourceOrderInfoLocal(String srvOrdId) {
        // 查询versionId用于查询补录资源信息数据
        String[] verStrs = null;
        /*String versionId = "";
        Map suppleInfo = resourceInitiateDao.qrySuppleInfoById(srvOrdId);
        if(MapUtils.getString(suppleInfo,"PARENT_ID","").equals("")){
            versionId = MapUtils.getString(suppleInfo,"ID");*/
            List<String> versionIdList = new ArrayList<String>();
            // 查询所有下发本地网的主键Id
            versionIdList = resourceInitiateDao.qryLocalId(srvOrdId);
            if(!CollectionUtils.isEmpty(versionIdList)){
                int length = versionIdList.size();
                verStrs = new String[length];
                for(int i=0;i<length;i++){
                    verStrs[i] = versionIdList.get(i);
                }
            }
      //  }
        // 根据versionId查询本地资源补录信息
        if(verStrs!=null){
            Map<String,Object> paramsMap = new HashMap<>();
            paramsMap.put("srvOrdId",verStrs);
            List<Map<String,Object>> list = resourceInitiateDao.queryResourceOrderInfoLocal(paramsMap);
            return list;
        }
        return null;
    }
}