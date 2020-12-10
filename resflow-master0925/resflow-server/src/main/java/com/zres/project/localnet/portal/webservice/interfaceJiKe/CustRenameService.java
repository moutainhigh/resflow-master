package com.zres.project.localnet.portal.webservice.interfaceJiKe;

import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.util.JsonConverterUtil;
import com.zres.project.localnet.portal.util.MapConverterUtil;
import com.zres.project.localnet.portal.webservice.IInterface;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.zres.project.localnet.portal.webservice.dto.FieldMeta;
import com.zres.project.localnet.portal.webservice.dto.RenamCustInfoDTO;
import com.zres.project.localnet.portal.webservice.flow.ExceptionChangeService;
import com.zres.project.localnet.portal.webservice.until.InterfaceThreadPool;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName CustRenameService
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/8/4 9:54
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@RestController
@RequestMapping("/CustRenameServiceIntf")
public class CustRenameService implements CustRenameServiceIntf {
    private static final Logger logger = LoggerFactory.getLogger(CustRenameService.class);
    // 变更信息code
    public static final String CHANGE_DATA = "changeData";
    // 变更信息描述
    public static final String CHANGE_MESSAGE = "changeMessage";

    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private IInterface interfaceIntf;

    @IgnoreSession
    @ResponseBody
    @RequestMapping(value = "/interfaceBDW/RenameAPI.spr", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public Map<String, Object> custInfoRename(@RequestBody String request) {
        Map<String,Object> resultMap = new HashMap();
        Map<String,Object> returnMap = new HashMap();
        Map<String,Object> logInfo = new HashMap<String, Object>();
        logInfo.put("interfname"," 查询待办各tab页单据信息查询接口");
        logInfo.put("url","/CustRenameServiceIntf/interfaceBDW/RenameAPI.spr");
        logInfo.put("request",request);
        logInfo.put("remark","接收app 查询待办各tab页单据信息查询接口 json报文");
        try {
            Map<String,Object> params = JSONObject.parseObject(request, Map.class);
            Map<String,Object> body = MapUtils.getMap(params, "UNI_BSS_BODY");
            Map<String,Object> renameReq = MapUtils.getMap(body, "RENAME_API_REQ");
            Map<String,Object> customerInfo = MapUtils.getMap(renameReq, "CST_ORD_INFO");
            Map<String,Object> userInfo = MapUtils.getMap(renameReq, "USER_INFO");
            String subscribeId = MapUtils.getString(renameReq, "SUBSCRIBE_ID");
            String userId = MapUtils.getString(userInfo, "USER_ID");
            String userName = MapUtils.getString(userInfo, "USER_NAME");
            if(StringUtils.isEmpty(subscribeId)){
                resultMap.put("respCode", "8888");
                resultMap.put("respDesc", "请检查业务号码不能为空");
                returnMap = wrapResInfo(resultMap);
                logInfo.put("respone",JSONObject.toJSONString(returnMap));
                return resultMap;
            }else{
//                message = "根据入参客户订单号 SUBSCRIBE_ID" +subscribeId+"没有查询到电路信息";
                //1.获取变更前数据
                List<RenamCustInfoDTO> oldCustomerInfoDTO = wsd.queryCustRenameList(subscribeId);
                //2.获取变更后数据
                RenamCustInfoDTO newCustomerInfoDTO = JsonConverterUtil.convertUpperKeyMap2Bean(customerInfo, RenamCustInfoDTO.class);
                Map<String, Object> changeContent = changeContent(newCustomerInfoDTO, oldCustomerInfoDTO.get(0));
                // 3.将变更差异值存入数据库

                if(!"".equals(MapUtils.getString(changeContent,CHANGE_MESSAGE))
                        && MapUtils.getString(changeContent,CHANGE_MESSAGE)!= null){
                    // 3.1先查询版本号是否存在，如果不存在则默认1， 存在则加1
                    for (RenamCustInfoDTO customerInfoDTO : oldCustomerInfoDTO) {
                        Map<String, Object> tempMap = wsd.queryMaxCustInfoRenameVersion(customerInfoDTO.getCstOrdId());
                        int maxVersion = MapUtils.getIntValue(tempMap, "MAX_VERSION", 0);
                        insertChangeInfo(customerInfoDTO.getCstOrdId(), userId,userName, maxVersion, changeContent);
                        //TODO  异步调用资源接口
                        Map<String, Object> map = new HashMap<>();
                        map.put("type","custInfoRename");
                        map.put("cstOrdId",customerInfoDTO.getCstOrdId());
                        map.put("cstInfo",customerInfoDTO);
                        InterfaceThreadPool.tuneIntfToExecute(interfaceIntf, map);
                    }
                    //4.更新客户信息
                    Map changeData = MapUtils.getMap(changeContent, "custInfo");
                    changeData.put("SUBSCRIBE_ID", subscribeId);
                    wsd.updateCustInfoRename( changeData);
                }
            }
            resultMap.put("respCode", "0000");
            resultMap.put("respDesc", "");
            returnMap = wrapResInfo(resultMap);
            logInfo.put("respone",JSONObject.toJSONString(returnMap));

        } catch (Exception e) {
            resultMap.put("respCode", "8888");
            resultMap.put("respDesc", "更新失败");
            returnMap = wrapResInfo(resultMap);
            logInfo.put("respone",e.getMessage());
            logger.debug(e.getMessage());
        } finally {
            insertInterfaceLog(logInfo);
        }
        return returnMap;
    }


    /**
     * 封装返回数据
     *
     * @param tempMap
     * @return
     */
    public Map wrapResInfo(Map<String, Object> tempMap) {
        logger.info("开始拼装返回报文--");
        Map retMap = new HashMap();
        Map uniBssBody = new HashMap();
        Map renameRsp = new HashMap();
        Map para = new HashMap();
        para.put("PARA_ID", "");
        para.put("PARA_VALUE", "");
        renameRsp.put("PARA", para);
        renameRsp.put("RESP_DESC", MapUtils.getString(tempMap, "respDesc"));
        renameRsp.put("RESP_CODE", MapUtils.getString(tempMap, "respCode"));
        uniBssBody.put("RENAME_API_RSP", renameRsp);
        retMap.put("UNI_BSS_BODY", uniBssBody);
        return retMap;
    }


    /**
     * 记录接口日志
     * @param logInfo
     */
    private void insertInterfaceLog(Map<String,Object> logInfo){
        Map<String,Object> interflog = new HashMap<String, Object>();
        interflog.put("INTERFNAME",MapUtils.getString(logInfo,"interfname"));
        interflog.put("URL",MapUtils.getString(logInfo,"url"));
        interflog.put("CONTENT", MapUtils.getString(logInfo,"request"));
        interflog.put("RETURNCONTENT",MapUtils.getString(logInfo,"respone"));
        interflog.put("ORDERNO",MapUtils.getString(logInfo,"tradeId"));
        interflog.put("REMARK", MapUtils.getString(logInfo,"remark"));
        wsd.insertInterfLog(interflog);
    }



    /**
     * 普通的两个对象比较，得到修改的内容
     * @param newSource 新的对象
     * @param oldSource 旧对象
     * @return 返回差异内容
     */
    public Map<String, Object> changeContent(Object newSource, Object oldSource) {
        // 变更信息code
        StringBuilder changeData = new StringBuilder();
        // 更新的过户信息
        Map<String, String> custInfo = new HashMap();
        // 变更信息message
        StringBuilder changeMessage = new StringBuilder();
        if (null == newSource || null == oldSource) {
            return null;
        }
        // 取出新的class类
        Class<?> newSourceClass = newSource.getClass();
        // 类的所有声明的字段
        Field[] newSourceFields = newSourceClass.getDeclaredFields();
        for (Field newField : newSourceFields) {
            String fieldName = newField.getName();

            // 如果是引用对象，则退出本次循环。继续下次
            String typeStr = String.valueOf(newField.getType());
            if (typeStr.contains("com") || typeStr.contains("List") || "cstOrdId".equals(fieldName)) {
                continue;
            }
            // 获取新的Field值
            String newValue = MapConverterUtil.getFieldValue(newSource, fieldName) == null ? "" : MapConverterUtil.getFieldValue(newSource, fieldName).toString().replaceAll("@enter@","\n");
            // 获取对应的旧的targetField值
            String oldValue = MapConverterUtil.getFieldValue(oldSource, fieldName) == null ? "" : MapConverterUtil.getFieldValue(oldSource, fieldName).toString();
            if (org.apache.commons.lang3.StringUtils.isEmpty(newValue) && org.apache.commons.lang3.StringUtils.isEmpty(oldValue)) {
                continue;
            }
            FieldMeta fieldMeta = newField.getAnnotation(FieldMeta.class);
            if (fieldMeta != null && !newValue.equals(oldValue)) {
                if (org.apache.commons.lang3.StringUtils.isNotEmpty(fieldMeta.column())) {
                    changeData.append("{\"key\":\"" + fieldMeta.column() + "\",\"oldValue\":\"" + oldValue + "\",\"newValue\":\"" + newValue + "\"},");
                    custInfo.put(fieldMeta.column(),newValue);
                }
                if (org.apache.commons.lang3.StringUtils.isNotEmpty(fieldMeta.name())) {
                    String enumType= fieldMeta.value();
                    //需要翻译枚举值
                    if ("CITY_CODE".equals(enumType)||"PROVINCE_CODE".equals(enumType)){ //地市 、省份
                       Map<String,Object> areaInfo = wsd.conversionAreas(enumType,oldValue,newValue);
                       oldValue=  MapUtils.getString(areaInfo, "OLDVALUE", "");
                       newValue=  MapUtils.getString(areaInfo, "NEWVALUE", "");
                    }

                    if ("CustType".equals(enumType) || "CERTI_TYPE_CODE".equals(enumType)){ //客户类型、证件类型
                        Map<String,Object> enumInfo = wsd.conversionEnum(enumType,oldValue,newValue);
                        oldValue=  MapUtils.getString(enumInfo, "OLDVALUE", "");
                        newValue=  MapUtils.getString(enumInfo, "NEWVALUE", "");
                    }

                    changeMessage.append("{\"key\":\"" + fieldMeta.name() + "\",\"oldValue\":\"" + oldValue + "\",\"newValue\":\"" + newValue + "\"},");
                }
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("custInfo", custInfo);
        map.put(CHANGE_DATA, changeData.toString());
        map.put(CHANGE_MESSAGE, changeMessage.toString());
        return map;
    }



    /**
     * 存入变更信息表
     * @param cstOrdId

     * @param userId
     * @param userName
     * @param changeInfoMap
     */
    private void insertChangeInfo(String cstOrdId, String userId,String userName, int maxVersion, Map<String, Object> changeInfoMap) {

        Map<String, Object> map = new HashMap();
        map.put("CST_ORD_ID", cstOrdId);
        map.put("CHG_VERSION", ++maxVersion);
        map.put("USER_ID", userId);
        map.put("USER_ID", userName);
        // 有差异值才存入数据库，没差异值不存入
        if (!StringUtils.isEmpty(MapUtils.getString(changeInfoMap,(ExceptionChangeService.CHANGE_DATA)))) {
            map.put("CHANGE_DATA", "[" + changeInfoMap.get(ExceptionChangeService.CHANGE_DATA) + "]");
            map.put("CHANGE_MESSAGE", "[" + changeInfoMap.get(ExceptionChangeService.CHANGE_MESSAGE) + "]");
                wsd.insertCustInfoRenameLog(map);
        }
    }

}
