package com.zres.project.localnet.portal.flowdealinfo.service;

import com.alibaba.fastjson.JSON;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.DispObjDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderQrySecondaryDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.flowdealinfo.entry.TacheWoOrderEntry;
import com.zres.project.localnet.portal.listener.util.EnmuValueUtil;
import com.ztesoft.res.frame.flow.common.util.DAOUtils;
import com.ztesoft.res.frame.flow.common.util.StringUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResCheckDisp {

    private static final Logger log = LoggerFactory.getLogger(ResCheckDisp.class);

    @Autowired
    private DispObjDao dispObjDao;
    @Autowired
    private DispRulesService dispRulesService;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private OrderQrySecondaryDao orderQrySecondaryDao;

    public ResCheckDisp() {
    }

    public String transDispObj (Map paramsMap) {
        return specialtyCommon(paramsMap, "transOrg");
    }

    public String outsideDispObj (Map paramsMap) {
        return specialtyCommon(paramsMap, "outsideOrg");
    }

    public String dataDispObj (Map paramsMap) {
        return specialtyCommon(paramsMap, "dataOrg");
    }

    public String accessDispObj (Map paramsMap) {
        return specialtyCommon(paramsMap, "accessOrg");
    }

    public String otherDispObj (Map paramsMap) {
        return specialtyCommon(paramsMap, "otherOrg");
    }

    public String changeDispObj (Map paramsMap) {
        return specialtyCommon(paramsMap, "changeOrg");
    }

    private String specialtyCommon(Map paramsMap, String specialtyOrgType) {
        String orderId = MapUtils.getString(paramsMap, "ORDER_ID");
        String psId = MapUtils.getString(paramsMap, "WO_PS_ID");
        Map area = qryDispAreaStr(orderId, specialtyOrgType);
        String areaStr = MapUtils.getString(area, "attr_val");
        List<String> areaValueList = Arrays.asList(areaStr.split(","));
        StringBuffer dispObj = new StringBuffer();
        for (int i = 0; i < areaValueList.size(); i++) {
            String dispObjStr = qryDispObjs(psId, areaValueList.get(i));
            if (i == 0) {
                dispObj.append(dispObjStr);
            }
            else {
                dispObj.append(",");
                dispObj.append(dispObjStr);
            }
        }
        return dispObj.toString();
    }

    /**
     * 流程多工单，获取派发区域
     * @return
     */
    public Map qryDispAreaStr(String orderId, String specialCode){
        String sql = "SELECT ATTR_VAL FROM gom_wo_oper_attr WHERE order_id = ? AND ATTR_ID = ? ";
        List<Map<String,String>> list = DAOUtils.executeQueryToList(sql, new String[]{orderId,specialCode});
        Map<String,String> map = new HashMap<String,String>();
        for (int i = 0; i < list.size(); i++) {
            map = list.get(i);
        }
        return map;
    }

    /**
     * 核查流程多工单，获取派发对象
     * @param areaId
     * @return
     */
    public String qryDispObjs(String psId, String areaId){
        String sql = "SELECT NVL2(D.DISP_OBJ_ID,\n" +
                "D.DISP_OBJ_TYPE || '_J!G@F_' || D.DISP_OBJ_ID, " +
                "B.DISP_OBJ_TYPE || '_J!G@F_' || B.DISP_OBJ_ID)" +
                "|| '_J!G@F_' || 'areaId' || '|' || A.AREA_ID " +
                "from gom_disp_srv_s        A, " +
                "GOM_PS_2_WO_S         B, " +
                "UOS_TACHE             C, " +
                "GOM_DISP_DETAIL_SRV_S D " +
                "where B.id = ? " +
                "AND B.tache_id = C.id\n" +
                "and A.tache_code = C.tache_code " +
                "AND D.DISP_SRV_ID = A.ID " +
                "AND A.AREA_ID = ? " +
                "AND D.IS_AUTO = '210000002' " +
                "AND A.REC_STATE = '170000001' ";
        String dispObjStr = DAOUtils.executeQueryToString(sql, new String[]{psId, areaId});
        return dispObjStr;
    }

    public String commonUseDispObj (Map paramsMap) {
        String dispObjStr = "";
        boolean flag = true;
        String orderId = MapUtils.getString(paramsMap, "ORDER_ID");
        String psId = MapUtils.getString(paramsMap, "WO_PS_ID");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("orderId", orderId);
        params.put("psId", psId);
        //根据psId查询环节ID
        String tacheId = dispObjDao.queryTacheIdByPsId(psId);
        //如果是完工汇总环节，判断完工汇总是否指定到人了
        if (EnmuValueUtil.COMPLATE_SUMMARY.equals(tacheId) || EnmuValueUtil.COMPLATE_SUMMARY_2.equals(tacheId)){
            Map<String, Object> map = orderDealDao.queryComplateInfo(orderId);
            if (MapUtils.isNotEmpty(map)){
                //完工汇总指定到人
                if ("是".equals(MapUtils.getString(map, "IS_COMPLATE_PERSON"))){
                    dispObjStr = "260000003_J!G@F_" + MapUtils.getString(map, "COMPLATE_PERSON_ID");
                    return dispObjStr;
                }
            }
        }
        //如果是资源分配或者是数据制作子流程这里查询对应的专业是否指定了对应的处理人，如果指定了处理人获取到该专业对应的处理人
        //二干资源分配子流程环节
        if(EnmuValueUtil.SEC_SOURCE_DISPATCH_CLD.equals(tacheId)){
            //根据order_id查询gom_ord_key_info表中的专业信息
            Map<String, Object> specialtyMap = dispObjDao.queryKeyInfoByOrderId(orderId);
            //根据orderId查询主流程的orderId即parent_order_id
            String parentOrderId = dispObjDao.queryParentOrderId(orderId);
            //根据主流程order_id查询专业、指定人等信息
            Map<String, Object> paramMap = orderQrySecondaryDao.qryDispatchData(parentOrderId);
            if (MapUtils.isNotEmpty(paramMap)){
                if ("是".equals(MapUtils.getString(paramMap, "IS_ASSIGN_PERSON", ""))){
                    Map<String, Object> personMap = (Map<String, Object>) JSON.parse(MapUtils.getString(paramMap,"RES_ASS_PERSION_ID")) ;
                    //根据专业获取到对应的处理人
                    dispObjStr = "260000003_J!G@F_" + MapUtils.getString(personMap, MapUtils.getString(specialtyMap, "SPECIALTY_CODE"));
                    return dispObjStr;
                }
            }
        }
        //专业数据制作
        if (EnmuValueUtil.SPECIALTY_DATA_PRODUCTION.equals(tacheId)){
            //根据order_id查询gom_ord_key_info表中的专业信息
            Map<String, Object> specialtyMap = dispObjDao.queryKeyInfoByOrderId(orderId);
            //根据orderId查询主流程的orderId即parent_order_id
            String parentOrderId = dispObjDao.queryParentOrderId(orderId);
            //根据主流程order_id查询专业、指定人等信息
            Map<String, Object> paramMap = orderQrySecondaryDao.qryDispatchData(parentOrderId);
            if (MapUtils.isNotEmpty(paramMap)){
                if ("是".equals(MapUtils.getString(paramMap, "ASS_MKDATA_PERSON", ""))){
                    Map<String, Object> personMap = (Map<String, Object>) JSON.parse(MapUtils.getString(paramMap,"MKDATA_PERSON_ID")) ;
                    //根据专业获取到对应的处理人
                    dispObjStr = "260000003_J!G@F_" + MapUtils.getString(personMap, MapUtils.getString(specialtyMap, "SPECIALTY_CODE"));
                    return dispObjStr;
                }
            }
        }
        //专业资源施工
        if (BasicCode.SPECIALTY_RES_CONSTRUCTION.equals(tacheId)){
            //根据order_id查询gom_ord_key_info表中的专业信息
            Map<String, Object> specialtyMap = dispObjDao.queryKeyInfoByOrderId(orderId);
            //根据orderId查询主流程的orderId即parent_order_id
            String parentOrderId = dispObjDao.queryParentOrderId(orderId);
            //根据主流程order_id查询专业、指定人等信息
            Map<String, Object> paramMap = orderQrySecondaryDao.qryDispatchData(parentOrderId);
            if (MapUtils.isNotEmpty(paramMap)){
                if ("是".equals(MapUtils.getString(paramMap, "IS_RES_MAKE", ""))){
                    Map<String, Object> personMap = (Map<String, Object>) JSON.parse(MapUtils.getString(paramMap,"RES_MAKE_PERSION_ID")) ;
                    //根据专业获取到对应的处理人
                    dispObjStr = "260000003_J!G@F_" + MapUtils.getString(personMap, MapUtils.getString(specialtyMap, "SPECIALTY_CODE"));
                    return dispObjStr;
                }
            }
        }
        params = relatedDimensionParams(params);
        Map<String, Object> dispObjPreviousOneMap = dispObjDao.qryDispObjPreviousOne(params);
        if (MapUtils.isNotEmpty(dispObjPreviousOneMap)){
            String tacheCode = MapUtils.getString(dispObjPreviousOneMap, "SRC_TACHE_CODE");
            String tacheStaffType = MapUtils.getString(dispObjPreviousOneMap, "SRC_TACHE_STAFF") ;
            String isAuto = MapUtils.getString(dispObjPreviousOneMap, "IS_AUTO") ;
            log.info("---tacheCode:" + tacheCode + "---tacheStaffType:" + tacheStaffType + "---isAuto:" + isAuto);
            if (StringUtils.isNotEmpty(tacheCode)
                    && StringUtils.isNotEmpty(tacheStaffType)){
                dispObjStr = dispRulesService.getDispObjByPrevTacheInfo(orderId, tacheCode, tacheStaffType);
                if (!StringUtils.isEmpty(dispObjStr)){
                    dispObjStr = dispObjStr + "_J!G@F_" + isAuto;
                    flag = false;
                }
            }
        }
        if (flag){
            dispObjStr = dispObjDao.qryDispObj(params);
            if (StringUtils.isEmpty(dispObjStr)){
                dispObjStr = "";
            }
        }

        return dispObjStr;
    }

    /**
     * 派发服务 汇总成一个通用javabean
     * 本来是想写的好看一点结果以失败而告终
     *    将dispObjDao方法抽成一个接口，然后每个不同的环节来实现接口
     *    在枚举里面返回实现类，获取javabean来调用，这样会更简单
     *    但是不同的环节过来只用调一个dao的方法这样抽成接口来实现太繁琐
     *    就在枚里面返回了方法名，又用ifelse来调用了
     *    这只是一个小例子，后面可以在别的地方运用这个思想来简化ifelse
     * @param paramsMap
     * @return
     */
    public String getTacheDispObj (Map paramsMap) {
        String dispObj = "";
        boolean flag = true;
        String orderId = MapUtils.getString(paramsMap, "ORDER_ID");
        String psId = MapUtils.getString(paramsMap, "WO_PS_ID");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("orderId", orderId);
        params.put("psId", psId);
        params = relatedDimensionParams(params);
        Map<String, Object> tacheInfoMap = dispObjDao.qryTacheInfo(psId);//查询环节信息
        TacheWoOrderEntry tacheWoOrderEntry = new TacheWoOrderEntry();
        tacheWoOrderEntry.setTacheCode(MapUtils.getString(tacheInfoMap, "TACHE_CODE"));
        String method = tacheWoOrderEntry.getMethodNameByTacheCode();//根据环节获取要调用的方法
        if ("mainDispService".equals(method)){
            dispObj = dispObjDao.mainDispService(params);
        }else if ("dataMakeDispService".equals(method)){
            Map<String, Object> dispObjMap = this.getConfigTacheObj(params);
            flag = MapUtils.getBoolean(dispObjMap, "flag");
            if (flag){
                dispObj = dispObjDao.dataMakeDispService(params);
            }else {
                dispObj = MapUtils.getString(dispObjMap, "dispObj");
            }
        }else if ("resourceConstructionDispService".equals(method)){
            Map<String, Object> dispObjMap = this.getConfigTacheObj(params);
            flag = MapUtils.getBoolean(dispObjMap, "flag");
            if (flag){
                dispObj = dispObjDao.resourceConstructionDispService(params);
            }else {
                dispObj = MapUtils.getString(dispObjMap, "dispObj");
            }
        }else if ("adjustTestDispService".equals(method)){
            dispObj = dispObjDao.adjustTestDispService(params);
        }else if ("fullCommissioningDispService".equals(method)){
            Map<String, Object> dispObjMap = this.getConfigTacheObj(params);
            flag = MapUtils.getBoolean(dispObjMap, "flag");
            if (flag){
                dispObj = dispObjDao.fullCommissioningDispService(params);
            }else {
                dispObj = MapUtils.getString(dispObjMap, "dispObj");
            }
        }else if ("provincialCommissioningDispService".equals(method)){
            Map<String, Object> dispObjMap = this.getConfigTacheObj(params);
            flag = MapUtils.getBoolean(dispObjMap, "flag");
            if (flag){
                dispObj = dispObjDao.provincialCommissioningDispService(params);
            }else {
                dispObj = MapUtils.getString(dispObjMap, "dispObj");
            }
        }else {
            throw new RuntimeException(dispObj);
        }
        if (StringUtils.isEmpty(dispObj)){
            dispObj = "";
        }
        return dispObj;
    }

    private Map<String, Object> relatedDimensionParams(Map<String, Object> params) {
        Map<String, Object> keyInfoMap = dispObjDao.qryOrderKeyInfo(params);//查询动作类型，专业编码，产品类型
        log.info("---actType:" + MapUtils.getString(keyInfoMap, "ACTTYPE")
                + "---specialtyCode:" + MapUtils.getString(keyInfoMap, "SPECIALTYCODE")
                + "---productType:" + MapUtils.getString(keyInfoMap, "PRODUCTTYPE"));
        params.put("actType", MapUtils.getString(keyInfoMap, "ACTTYPE"));
        params.put("specialtyCode", MapUtils.getString(keyInfoMap, "SPECIALTYCODE"));
        params.put("productType", MapUtils.getString(keyInfoMap, "PRODUCTTYPE"));
        return params;
    }

    private Map<String, Object> getConfigTacheObj(Map<String, Object> params){
        Map<String, Object> dispObjMap = new HashMap<String, Object>();
        String dispObj = "";
        boolean flag = true;
        String orderId = MapUtils.getString(params, "orderId");
        Map<String, Object> dispObjPreviousOneMap = dispObjDao.qryDispObjPreviousOne(params);
        if (MapUtils.isNotEmpty(dispObjPreviousOneMap)){
            String tacheCode = MapUtils.getString(dispObjPreviousOneMap, "SRC_TACHE_CODE");
            String tacheStaffType = MapUtils.getString(dispObjPreviousOneMap, "SRC_TACHE_STAFF") ;
            String isAuto = MapUtils.getString(dispObjPreviousOneMap, "IS_AUTO") ;
            log.info("---tacheCode:" + tacheCode + "---tacheStaffType:" + tacheStaffType + "---isAuto:" + isAuto);
            if (StringUtils.isNotEmpty(tacheCode)
                    && StringUtils.isNotEmpty(tacheStaffType)) {
                dispObj = dispRulesService.getDispObjByPrevTacheInfo(orderId, tacheCode, tacheStaffType);
                if (!StringUtils.isEmpty(dispObj)){
                    dispObj = dispObj + "_J!G@F_" + isAuto;
                    flag = false;
                }
            }
        }
        dispObjMap.put("flag", flag);
        dispObjMap.put("dispObj", dispObj);
        return dispObjMap;
    }

}