package com.zres.project.localnet.portal.resourceInitiate.service;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
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
    @Autowired
    private OrderDealDao orderDealDao;

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
        List<Map<String, Object>> cityList = null;
        try {
            //获取当前登录人
            String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
            Map<String, Object> staffMap = orderDealDao.getIsBraFiliAle(Integer.valueOf(operStaffId));
            Long orgId = MapUtil.getLong(staffMap, "ORG_ID");
            String isBraFiliale = org.apache.commons.collections.MapUtils.getString(staffMap, "ISBRAFILIALE");
            if("0".equals(isBraFiliale)){ //是否查询分公司下的部门 1查询市分公司 0查询省分公司下
                // 查询该部门所在省份的org_id
                Map<String, Object> provinceMap = orderDealDao.getProviceOrg(orgId);
                cityList = resourceInitiateDao.queryDeptInfo(MapUtils.getString(provinceMap, "ORG_ID"));
            } else if("1".equals(isBraFiliale)){
//                //查询当前登录人所在省份ID
                Map<String, Object> orgMap = resourceInitiateDao.queryBranchAreaInfo(operStaffId);
//                //查询对应省份区县信息
//                Map<String, Object> paramMap = new HashMap<>();
//                paramMap.put("parentId", MapUtils.getString(orgMap, "ORG_ID"));
//            paramMap.put("",);
                cityList = resourceInitiateDao.queryDeptInfo(MapUtils.getString(orgMap, "ORG_ID"));
            }

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
            //获取当前登录人
            String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
            //查询当前登录人所在省份ID
            Map<String, Object> orgMap = resourceInitiateDao.getOrgInfoByStaffId(operStaffId);
            param.put("areaId", MapUtils.getString(orgMap, "AREA_ID"));
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

    @Transactional(rollbackFor = Exception.class)
    public void startFlow(Map<String, Object> param) throws Exception{
        SimpleDateFormat fmt = new SimpleDateFormat("YYYYMMDD");
        //关键信息入库
        Map<String, Object> gridData = (Map<String, Object>) MapUtils.getMap(param, "gridData");
        Map<String, Object> formData = (Map<String, Object>) MapUtils.getMap(param, "formData");
        //区域ID
        //获取当前登录人
        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        //查询当前登录人所在省份ID
        Map<String, Object> orgMap = resourceInitiateDao.getOrgInfoByStaffId(operStaffId);
        //重新封装参数入库
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("regionId", MapUtils.getString(formData, "regionId"));
        paramMap.put("resRegionId", MapUtils.getString(orgMap, "CRM_REGION"));
        paramMap.put("custId", MapUtils.getString(formData, "custId"));
        paramMap.put("linkMan", MapUtils.getString(formData, "linkMan"));
        paramMap.put("address", MapUtils.getString(formData, "address"));

        paramMap.put("prodInstId", MapUtils.getString(gridData, "prodInstId"));
        paramMap.put("accNbr", MapUtils.getString(gridData, "accNbr"));
        paramMap.put("circuitCode", MapUtils.getString(gridData, "circuitCode"));
        paramMap.put("serviceId", MapUtils.getString(gridData, "serviceId"));
        paramMap.put("systemResource", MapUtils.getString(gridData, "systemResource"));
        paramMap.put("isoldres", MapUtils.getString(gridData, "isoldres"));
        paramMap.put("specialtyConfig", MapUtils.getString(formData, "specialtyInfo"));
        paramMap.put("remark", MapUtils.getString(formData, "remark"));
        paramMap.put("localConf", MapUtils.getString(formData, "cityId").toString());
        paramMap.put("orderState", "10N");//处理中...
        resourceInitiateDao.insertResourceInitiateInfo(paramMap);
        //调用流程接口启流程
        Map<String, Object> flowParam = new HashMap<>();
        //查询流程规格
        String flowId = resourceInitiateDao.queryFlowId();
        flowParam.put("ordPsid", "10101282"); //10101282
        flowParam.put("AREA", "350002000000000042766408");//全国
        flowParam.put("ORDER_TITLE", "ZYBL-" + fmt.format(new Date()) + "-" + MapUtils.getString(paramMap, "accNbr"));
        flowParam.put("ORDER_CONTENT", MapUtils.getString(formData, "remark", ""));
        flowParam.put("PRODUCT_TYPE", MapUtils.getString(paramMap, "serviceId"));
        //flowParam.put("REGION_ID", MapUtils.getString(orgMap, "ORG_ID"));
        Map<String, String> attr = new HashMap<>();
        attr.put("REGION_ID", MapUtils.getString(orgMap, "ORG_ID")); //区域
        flowParam.put("attr", attr);
        Map<String, Object> orderMap = resSupplementDealServiceIntf.createOrderResSup(flowParam);
        //将order_id更新到表中
        orderMap.put("id", MapUtils.getString(paramMap, "id"));
        orderMap.put("versionId",  "SupV" + MapUtils.getString(paramMap, "id"));
        int  updateFlag = resourceInitiateDao.updateOrderInfoByResourceSupplementId(orderMap);
        if (updateFlag > 0){
            //调用方法第一个环节自动流转
            resSupplementDealServiceIntf.firstTacheAutoSubmit(MapUtils.getString(orderMap, "orderId"));
            if("0".equals(MapUtils.getString(param, "isoldres"))){
                resourceInitiateDao.UpdateRecordingStatusByProdInstId(MapUtils.getString(param, "srvordId"));
            }
        }
    }

    public void startFlowFromSec(Map<String, Object> param) throws Exception{
        Map<String, Object> formData = (Map<String, Object>) MapUtils.getMap(param, "formData");
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("orderId", MapUtils.getString(param, "orderId"));
        paramMap.put("specialtyConfig", MapUtils.getString(formData, "specialtyInfo"));
        paramMap.put("remark", MapUtils.getString(formData, "remark"));
        paramMap.put("localConf", MapUtils.getString(formData, "cityId").toString());
        resourceInitiateDao.updateResSupSpecialtyAndArea(paramMap);
        resSupplementDealServiceIntf.submitOrderResSup(param);
    }

    public Map<String, Object> querySrvOrdInfoByInstanceId(Map<String, Object> param){
        Map<String, Object> resMap = new HashMap<>();
        try{
            List<Map<String, Object>> list = resourceInitiateDao.querySrvOrdInfoByInstanceId(param);
            if (!CollectionUtils.isEmpty(list)){
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
        // 查询versionId用于查询补录资源信息数据
        String versionId = "";
        Map suppleInfo = resourceInitiateDao.qrySuppleInfoById(srvOrdId);
        if(MapUtils.getString(suppleInfo,"PARENT_ID","").equals("")){
            versionId = MapUtils.getString(suppleInfo,"ID");
        } else{
            versionId = MapUtils.getString(suppleInfo,"PARENT_ID");
        }
        // 根据versionId查询二干资源补录信息
        Map<String,Object> paramsMap = new HashMap<>();
        paramsMap.put("srvOrdId",new String[]{versionId});
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
        String versionId = "";
        Map<String,Object> paramsMap = new HashMap<>();
        List<Map<String,Object>> list = null;
        Map suppleInfo = resourceInitiateDao.qrySuppleInfoById(srvOrdId);
        if(MapUtils.getString(suppleInfo,"PARENT_ID","").equals("")){
            versionId = MapUtils.getString(suppleInfo,"ID");
            verStrs = new String[]{versionId};
            paramsMap.put("srvOrdId",verStrs);
            list = resourceInitiateDao.queryResourceOrderInfoSec(paramsMap);
        } else{
            versionId = MapUtils.getString(suppleInfo,"PARENT_ID");
            List<String> versionIdList = new ArrayList<String>();
            // 查询所有下发本地网的versionId
            versionIdList = resourceInitiateDao.qryLocalId(versionId);
            if(!CollectionUtils.isEmpty(versionIdList)){
                int length = versionIdList.size();
                verStrs = new String[length];
                for(int i=0;i<length;i++){
                    verStrs[i] = versionIdList.get(i);
                }
            }
            paramsMap.put("srvOrdId",verStrs);
            list = resourceInitiateDao.queryResourceOrderInfoLocal(paramsMap);
        }
        // 根据资源补录表主键Id查询本地资源补录信息


        return list;
    }
}