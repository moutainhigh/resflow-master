package com.zres.project.localnet.portal.webservice.flow;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zres.project.localnet.portal.webservice.dto.JiKeProdAttrDTO;
import com.zres.project.localnet.portal.webservice.dto.JiKeProdInfoDTO;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.util.MapConverterUtil;
import com.zres.project.localnet.portal.webservice.data.dao.ExceptionFlowDao;
import com.zres.project.localnet.portal.webservice.dto.AttachDTO;
import com.zres.project.localnet.portal.webservice.dto.FieldMeta;
import com.zres.project.localnet.portal.webservice.dto.ProdAttrDTO;
import com.zres.project.localnet.portal.webservice.dto.ProdInfoDTO;

@Service
public class ExceptionChangeService {

    @Autowired
    private ExceptionFlowDao exceptionFlowDao;

    /*------------------一干异常单类型start----------------------------*/
    // 追单
    public static final String EXCEPTION_104 = "104";
    // 加急
    public static final String EXCEPTION_108 = "108";
    // 延期，和加急的逻辑一样
    public static final String EXCEPTION_109 = "109";
    // 挂起
    public static final String EXCEPTION_110 = "110";
    // 解挂
    public static final String EXCEPTION_111 = "111";
    // 撤单
    public static final String EXCEPTION_112 = "112";
    /*------------------一干异常单类型end----------------------------*/

    /*------------------集客异常单类型start----------------------------*/
    // 追单
    public static final String EXCEPTION_4A = "4A";
    // 加急
    public static final String EXCEPTION_4B = "4B";
    // 延期
    public static final String EXCEPTION_4C = "4C";
    // 撤单
    public static final String EXCEPTION_4D = "4D";
    // 挂起
    public static final String EXCEPTION_4E = "4E";
    // 解挂
    public static final String EXCEPTION_4F = "4F";

    public static final String REQUIRE_COMPLETE_DATE_CODE = "CON0014";

    // 全程要求完成时间，全程竣工时间,自动止租时间,A端要求完成时间,Z端要求完成时间
    private static final String[] FIN_DATE = {"CON0014", "20000073","CON0013","CON3007","CON0015","CON0016"};

    private SimpleDateFormat dfStr = new SimpleDateFormat("yyyyMMddHHmmss");
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /*------------------集客异常单类型end----------------------------*/



    // 变更信息code
    public static final String CHANGE_DATA = "changeData";
    // 变更信息描述
    public static final String CHANGE_MESSAGE = "changeMessage";

    /**
     * 封装电路业务信息差异值,追单用
     * @param newProdInfoDTO
     * @param oldProdInfoDTO
     * @return
     */
    public Map<String, String> changeProdInfo(String type, String srvOrdId, ProdInfoDTO newProdInfoDTO, ProdInfoDTO oldProdInfoDTO) {
        StringBuilder changeData = new StringBuilder();
        StringBuilder changeMessage = new StringBuilder();
        // 基本信息
        Map<String, String> prodInfoMap = changeContent(newProdInfoDTO, oldProdInfoDTO);
        changeData.append(prodInfoMap.get(CHANGE_DATA));
        changeMessage.append(prodInfoMap.get(CHANGE_MESSAGE));
        // 产品属性
        List<ProdAttrDTO> oldProdAttrDTOList = oldProdInfoDTO.getProdAttrDTOList();
        List<ProdAttrDTO> newProdAttrDTOList = newProdInfoDTO.getProdAttrDTOList();
        Map<String, String> prodAttrInfoMap = changeProdAttrContent(type, srvOrdId, newProdAttrDTOList);
        changeData.append(prodAttrInfoMap.get(CHANGE_DATA));
        changeMessage.append(prodAttrInfoMap.get(CHANGE_MESSAGE));
        Map<String, String> map = new HashMap<>();
        map.put(CHANGE_DATA, changeData.toString());
        map.put(CHANGE_MESSAGE, changeMessage.toString());
        return map;
    }

    /**
     * 特殊处理产品属性字段
     * @param type
     * @param srvOrdId
     * @param newProdAttrDTOList
     * @return
     */
    public Map<String, String> changeProdAttrContent(String type, String srvOrdId,
                                                     List<ProdAttrDTO> newProdAttrDTOList) {

        Map<String, String> resultMap = new HashMap<>();
        // 特殊化越来越多，追单 加急|延期 分开来处理
        if(EXCEPTION_104.equals(type)){
            resultMap = changeJiKeProdAttrContent104(srvOrdId, newProdAttrDTOList);
        }
        if(EXCEPTION_108.equals(type) || EXCEPTION_109.equals(type)){
            resultMap = changeJiKeProdAttrContent108109(srvOrdId, newProdAttrDTOList);
        }
        return resultMap;


    }

    /**
     * 处理一干追单属性
     * @param srvOrdId
     * @param newProdAttrDTOList
     * @return
     */
    private Map<String, String> changeJiKeProdAttrContent104(String srvOrdId, List<ProdAttrDTO> newProdAttrDTOList){
        // 变更信息code
        StringBuilder changeData = new StringBuilder();
        // 变更信息message
        StringBuilder changeMessage = new StringBuilder();
        for (ProdAttrDTO newProdAttrDTO : newProdAttrDTOList) {
            String newAttrValueName = newProdAttrDTO.getCode();
            String newAttrName = newProdAttrDTO.getName();
            String newValue = newProdAttrDTO.getValue() == null ? "" : newProdAttrDTO.getValue();
            // 根据newAttrValueName和srvOrdId去查出旧的属性对象，如果没有则需要new一个，追单确认的时候更新就要用merge
            List<ProdAttrDTO> oldProdAttrDTOList = exceptionFlowDao.queryProdAttrDTOListByAttrValueName(srvOrdId, newAttrValueName);
            if(oldProdAttrDTOList != null && !oldProdAttrDTOList.isEmpty()){
                ProdAttrDTO oldProdAttrDTO = oldProdAttrDTOList.get(0);
                String oldAttrValueName = oldProdAttrDTO.getCode();
                // 同一个属性才做比较
                if(newAttrValueName.equals(oldAttrValueName)){
                    String oldValue = oldProdAttrDTO.getValue() == null ? "" : oldProdAttrDTO.getValue();
                    if(!oldValue.equals(newValue)){
                        changeData.append("{\"key\":\"" + newProdAttrDTO.getCode() + "\",\"oldValue\":\"" + oldValue + "\",\"newValue\":\"" + newValue + "\"},");
                        changeMessage.append("{\"key\":\"" + newAttrName + "\",\"oldValue\":\"" + oldValue + "\",\"newValue\":\"" + newValue + "\"},");
                    }
                }
            } else {
                // 旧属性对象为空而新传递过来属性对象有值的情况
                newProdAttrDTOContent(newValue, newAttrValueName, newAttrName, changeData, changeMessage);
            }
        }
        Map<String, String> map = new HashMap<>();
        map.put(CHANGE_DATA, changeData.toString());
        map.put(CHANGE_MESSAGE, changeMessage.toString());
        return map;
    }

    /**
     * 一干处理加急和延期
     * @param srvOrdId
     * @param newProdAttrDTOList
     * @return
     */
    private Map<String, String> changeJiKeProdAttrContent108109(String srvOrdId, List<ProdAttrDTO> newProdAttrDTOList){
        // 变更信息code
        StringBuilder changeData = new StringBuilder();
        // 变更信息message
        StringBuilder changeMessage = new StringBuilder();
        for (ProdAttrDTO newProdAttrDTO : newProdAttrDTOList) {
            String newAttrValueName = newProdAttrDTO.getCode();
            String newAttrName = newProdAttrDTO.getName();
            String newValue = newProdAttrDTO.getValue() == null ? "" : newProdAttrDTO.getValue();
            // 根据newAttrValueName和srvOrdId去查出旧的属性对象，如果没有则需要new一个，追单确认的时候更新就要用merge
            List<ProdAttrDTO> oldProdAttrDTOList = exceptionFlowDao.queryProdAttrDTOListByAttrValueName(srvOrdId, newAttrValueName);
            if(oldProdAttrDTOList != null && !oldProdAttrDTOList.isEmpty()){
                ProdAttrDTO oldProdAttrDTO = oldProdAttrDTOList.get(0);
                String oldAttrValueName = oldProdAttrDTO.getCode();
                // 同一个属性才做比较
                if(newAttrValueName.equals(oldAttrValueName)){
                    String oldValue = oldProdAttrDTO.getValue() == null ? "" : oldProdAttrDTO.getValue();
                    if(!oldValue.equals(newValue)){
                        changeData.append("{\"key\":\"" + newAttrValueName + "\",\"oldValue\":\"" + oldValue + "\",\"newValue\":\"" + newValue + "\"},");
                        changeMessage.append("{\"key\":\"" + newAttrName + "\",\"oldValue\":\"" + oldValue + "\",\"newValue\":\"" + newValue + "\"},");
                        // 查询历史表有数据就不做操作，没数据则插入
                        int count = exceptionFlowDao.queryProdAttrHis(srvOrdId, newAttrValueName);
                        if (count == 0) {
                            exceptionFlowDao.insertProdAttrHis(srvOrdId, newAttrValueName);
                        }
                        // 加急延期-查看是否有传递要求完成时间，如果有则更新gom_order表
                        if("rfsdate".equals(newAttrValueName) && StringUtils.isNotEmpty(newValue)){
                            updateOrderReqFinDate(srvOrdId,  newValue);
                        }
                        exceptionFlowDao.updateProdAttr(srvOrdId, newAttrValueName, newValue);
                    }
                }
            } else {
                // 旧属性对象为空而新传递过来属性对象有值的情况
                newProdAttrDTOContent(newValue, newAttrValueName, newAttrName, changeData, changeMessage);
                // 需要属性做插入
                if(StringUtils.isNotEmpty(newValue)){
                    exceptionFlowDao.insertProdAttr(srvOrdId, newAttrValueName, newAttrName, newValue);
                }

            }
        }


        Map<String, String> map = new HashMap<>();
        map.put(CHANGE_DATA, changeData.toString());
        map.put(CHANGE_MESSAGE, changeMessage.toString());
        return map;
    }


    private void newProdAttrDTOContent(String newValue, String newAttrValueName, String newAttrName,
                                       StringBuilder changeData, StringBuilder changeMessage){
        ProdAttrDTO oldProdAttrDTO = new ProdAttrDTO();
        String oldValue = oldProdAttrDTO.getValue() == null ? "" : oldProdAttrDTO.getValue();
        if (!oldValue.equals(newValue)) {
            changeData.append("{\"key\":\"" + newAttrValueName + "\",\"oldValue\":\"" + oldValue + "\",\"newValue\":\"" + newValue + "\"},");
            changeMessage.append("{\"key\":\"" + newAttrName + "\",\"oldValue\":\"" + oldValue + "\",\"newValue\":\"" + newValue + "\"},");
        }
    }


    /**
     * 普通的两个对象比较，得到修改的内容
     * @param newSource 新的对象
     * @param oldSource 旧对象
     * @return 返回差异内容
     */
    public Map<String, String> changeContent(Object newSource, Object oldSource) {
        // 变更信息code
        StringBuilder changeData = new StringBuilder();
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
            if (typeStr.contains("com") || typeStr.contains("List")) {
                continue;
            }
            // 获取新的Field值
            String newValue = MapConverterUtil.getFieldValue(newSource, fieldName) == null ? "" : MapConverterUtil.getFieldValue(newSource, fieldName).toString().replaceAll("@enter@","\n");
            // 获取对应的旧的targetField值
            String oldValue = MapConverterUtil.getFieldValue(oldSource, fieldName) == null ? "" : MapConverterUtil.getFieldValue(oldSource, fieldName).toString();
            if (StringUtils.isEmpty(newValue) && StringUtils.isEmpty(oldValue)) {
                continue;
            }
            FieldMeta fieldMeta = newField.getAnnotation(FieldMeta.class);
            if (fieldMeta != null && !newValue.equals(oldValue)) {
                if (StringUtils.isNotEmpty(fieldMeta.column())) {
                    changeData.append("{\"key\":\"" + fieldMeta.column() + "\",\"oldValue\":\"" + oldValue + "\",\"newValue\":\"" + newValue + "\"},");
                }
                if (StringUtils.isNotEmpty(fieldMeta.name())) {
                    changeMessage.append("{\"key\":\"" + fieldMeta.name() + "\",\"oldValue\":\"" + oldValue + "\",\"newValue\":\"" + newValue + "\"},");
                }
            }
        }
        Map<String, String> map = new HashMap<>();
        map.put(CHANGE_DATA, changeData.toString());
        map.put(CHANGE_MESSAGE, changeMessage.toString());
        return map;

    }


    //------集客异常单start--------

    /**
     * 封装集客电路信息差异值
     * @param type
     * @param newJiKeProdInfoDTO
     * @param oldJiKeProdInfoDTO
     * @return
     */
    public Map<String, String> changeJiKeProdInfo(String type, String srvOrdId, JiKeProdInfoDTO newJiKeProdInfoDTO, JiKeProdInfoDTO oldJiKeProdInfoDTO) {
        StringBuilder changeData = new StringBuilder();
        StringBuilder changeMessage = new StringBuilder();
        // 基本信息
        Map<String, String> prodInfoMap = changeContent(newJiKeProdInfoDTO, oldJiKeProdInfoDTO);
        changeData.append(prodInfoMap.get(CHANGE_DATA));
        changeMessage.append(prodInfoMap.get(CHANGE_MESSAGE));
        // 产品属性
        List<JiKeProdAttrDTO> oldJiKeProdAttrDTOList = oldJiKeProdInfoDTO.getJiKeProdAttrDTOList();
        List<JiKeProdAttrDTO> newJiKeProdAttrDTOList = newJiKeProdInfoDTO.getJiKeProdAttrDTOList();
        Map<String, String> prodAttrInfoMap = changeJiKeProdAttrContent(type, srvOrdId, newJiKeProdAttrDTOList);
        changeData.append(prodAttrInfoMap.get(CHANGE_DATA));
        changeMessage.append(prodAttrInfoMap.get(CHANGE_MESSAGE));
        Map<String, String> map = new HashMap<>();
        map.put(CHANGE_DATA, changeData.toString());
        map.put(CHANGE_MESSAGE, changeMessage.toString());
        return map;
    }

    public Map<String, String> changeJiKeProdAttrContent(String type, String srvOrdId,
                                                         List<JiKeProdAttrDTO> newJiKeProdAttrDTOList) {
        Map<String, String> resultMap = new HashMap<>();
        // 特殊化越来越多，追单 加急|延期 分开来处理
        if(EXCEPTION_4A.equals(type)){
            resultMap = changeJiKeProdAttrContent4A(srvOrdId, newJiKeProdAttrDTOList);
        }
        if(EXCEPTION_4B.equals(type) || EXCEPTION_4C.equals(type)){
            resultMap = changeJiKeProdAttrContent4B4C(srvOrdId, newJiKeProdAttrDTOList);
        }
        return resultMap;

    }

    /**
     * 处理追单比较属性
     * @param newJiKeProdAttrDTOList
     * @return
     */
    private Map<String, String> changeJiKeProdAttrContent4A(String srvOrdId, List<JiKeProdAttrDTO> newJiKeProdAttrDTOList) {
        StringBuilder changeData = new StringBuilder();
        StringBuilder changeMessage = new StringBuilder();
        for (JiKeProdAttrDTO newJiKeProdAttrDTO : newJiKeProdAttrDTOList) {
            String newAttrCode = newJiKeProdAttrDTO.getAttrCode();
            // 根据newAttrCode和srvOrdId去查出旧的属性对象，如果没有则需要new一个，追单确认的时候更新就要用merge
            List<JiKeProdAttrDTO> oldJiKeProdAttrDTOList = exceptionFlowDao.queryJiKeProdAttrDTOListByAttrCode(srvOrdId, newAttrCode);
            if(oldJiKeProdAttrDTOList !=null &&  !oldJiKeProdAttrDTOList.isEmpty()) {
                JiKeProdAttrDTO oldJiKeProdAttrDTO = oldJiKeProdAttrDTOList.get(0);
                String oldAttrCode = oldJiKeProdAttrDTO.getAttrCode();
                // 同一个属性才做比较
                if (newAttrCode.equals(oldAttrCode)) {
                    dealJiKeProdAttrDTO(newJiKeProdAttrDTO, oldJiKeProdAttrDTO, changeData, changeMessage);
                }
            }else{
                // 旧属性对象为空而新传递过来属性对象有值的情况
                JiKeProdAttrDTO oldJiKeProdAttrDTO = new JiKeProdAttrDTO();
                dealJiKeProdAttrDTO(newJiKeProdAttrDTO, oldJiKeProdAttrDTO, changeData, changeMessage);
            }
        }
        Map<String, String> map = new HashMap<>();
        map.put(CHANGE_DATA, changeData.toString());
        map.put(CHANGE_MESSAGE, changeMessage.toString());
        return map;
    }

    private void dealJiKeProdAttrDTO(JiKeProdAttrDTO newJiKeProdAttrDTO, JiKeProdAttrDTO oldJiKeProdAttrDTO,
                                     StringBuilder changeData, StringBuilder changeMessage){
        String oldValue = oldJiKeProdAttrDTO.getAttrValue() == null ? "" : oldJiKeProdAttrDTO.getAttrValue();
        String newValue = newJiKeProdAttrDTO.getAttrValue() == null ? "" : newJiKeProdAttrDTO.getAttrValue();
        String newAttrCode = newJiKeProdAttrDTO.getAttrCode();
        // 针对追单时间需要格式化之后再比较
        if(Arrays.asList(FIN_DATE).contains(newJiKeProdAttrDTO.getAttrCode())){
            try{
                // 时间只比较年月日
                if(StringUtils.isNotEmpty(oldValue)){
                    oldValue = oldValue.substring(0, 10);
                }
                if(StringUtils.isNotEmpty(newValue)){
                    newValue = df.format(dfStr.parse(newValue)).substring(0,10);
                }
            }catch (ParseException e){

            }
        }
        // 值有变化，则将变更信息组装
        if (!oldValue.equals(newValue)) {
            changeData.append("{\"key\":\"" + newAttrCode + "\",\"oldValue\":\"" + oldValue + "\",\"newValue\":\"" + newValue + "\"},");
            // 集客属性中文描述额外定义值
            String name = getPropertyNameByAttrCode(newAttrCode);
            String oldMsgValue = oldValue;
            String newMsgValue = newValue;
            // 先判断属性值是否是枚举值，如果是需要做转换
            List<Map<String, String>> enumMapList = exceptionFlowDao.getPropertyById(newAttrCode);
            String propertyId = "";
            if(enumMapList != null && !enumMapList.isEmpty()){
                Map<String, String> enumMap = enumMapList.get(0);
                propertyId = MapUtils.getString(enumMap, "PROPERTY_ID", "");
            }
            if(newAttrCode.equals(propertyId)){
                // 证明是枚举值，需要对值进行转换
                oldMsgValue = getCodeContent(newAttrCode, oldValue);
                newMsgValue = getCodeContent(newAttrCode, newValue);
            }
            changeMessage.append("{\"key\":\"" + name + "\",\"oldValue\":\"" + oldMsgValue + "\",\"newValue\":\"" + newMsgValue + "\"},");
        }
    }

    /**
     * 处理加急和延期比较属性
     * @param srvOrdId
     * @param newJiKeProdAttrDTOList
     * @return
     */
    private Map<String, String> changeJiKeProdAttrContent4B4C(String srvOrdId,
                                                              List<JiKeProdAttrDTO> newJiKeProdAttrDTOList){
        StringBuilder changeData = new StringBuilder();
        StringBuilder changeMessage = new StringBuilder();
        for (JiKeProdAttrDTO newJiKeProdAttrDTO : newJiKeProdAttrDTOList) {
            String newAttrCode = newJiKeProdAttrDTO.getAttrCode();
            String newValue = newJiKeProdAttrDTO.getAttrValue();
            // 根据newAttrCode和srvOrdId去查出旧的属性对象，如果没有则需要new一个，如果是new则需要插入而不是更新
            List<JiKeProdAttrDTO> oldJiKeProdAttrDTOList = exceptionFlowDao.queryJiKeProdAttrDTOListByAttrCode(srvOrdId, newAttrCode);
            if(oldJiKeProdAttrDTOList !=null &&  !oldJiKeProdAttrDTOList.isEmpty()){
                JiKeProdAttrDTO oldJiKeProdAttrDTO = oldJiKeProdAttrDTOList.get(0);
                String oldAttrCode = oldJiKeProdAttrDTO.getAttrCode();
                // 同一个属性才做比较
                if (newAttrCode.equals(oldAttrCode)) {
                    String oldValue = oldJiKeProdAttrDTO.getAttrValue() == null ? "" : oldJiKeProdAttrDTO.getAttrValue();
                    if (!oldValue.equals(newValue)) {
                        // 值有变化，则将变更信息组装
                        changeData.append("{\"key\":\"" + newAttrCode + "\",\"oldValue\":\"" + oldValue + "\",\"newValue\":\"" + newValue + "\"},");
                        String name = getPropertyNameByAttrCode(newAttrCode);
                        changeMessage.append("{\"key\":\"" + name + "\",\"oldValue\":\"" + oldValue + "\",\"newValue\":\"" + newValue + "\"},");
                        // 加急或者延期，需要进行更新数据：先转储到属性历史表，然后再更新属性信息
                        // 查询历史表有数据就不做操作，没数据则插入
                        int count = exceptionFlowDao.queryJiKeProdAttrHis(srvOrdId, newAttrCode);
                        if (count == 0) {
                            exceptionFlowDao.insertJiKeProdAttrHis(srvOrdId, newAttrCode);
                        }
                        if(REQUIRE_COMPLETE_DATE_CODE.equals(newAttrCode) && StringUtils.isNotEmpty(newValue)){
                            // 查看是否有传递要求完成时间，如果有则更新gom_order表
                            updateOrderReqFinDate(srvOrdId,  newValue);
                        }
                        exceptionFlowDao.updateJiKeProdAttr(srvOrdId, newAttrCode, newValue);
                    }
                }
            }else{
                // 旧属性对象为空而新传递过来属性对象有值的情况
                Map<String, String> newOldJiKeProdAttrDTOChangeContent = newOldJiKeProdAttrDTOChange(newJiKeProdAttrDTO);
                changeData.append(newOldJiKeProdAttrDTOChangeContent.get(CHANGE_DATA));
                changeMessage.append(newOldJiKeProdAttrDTOChangeContent.get(CHANGE_MESSAGE));
                // 需要属性做插入
                if(StringUtils.isNotEmpty(newValue)){
                    exceptionFlowDao.insertJiKeProdAttr(srvOrdId, newAttrCode, newValue);
                }
            }
        }
        Map<String, String> map = new HashMap<>();
        map.put(CHANGE_DATA, changeData.toString());
        map.put(CHANGE_MESSAGE, changeMessage.toString());
        return map;
    }

    private Map<String, String> newOldJiKeProdAttrDTOChange(JiKeProdAttrDTO newJiKeProdAttrDTO){
        StringBuilder changeData = new StringBuilder();
        StringBuilder changeMessage = new StringBuilder();
        JiKeProdAttrDTO oldJiKeProdAttrDTO = new JiKeProdAttrDTO();
        String oldValue = oldJiKeProdAttrDTO.getAttrValue() == null ? "" : oldJiKeProdAttrDTO.getAttrValue();
        String newValue = newJiKeProdAttrDTO.getAttrValue() == null ? "" : newJiKeProdAttrDTO.getAttrValue();
        if (!oldValue.equals(newValue)) {
            changeData.append("{\"key\":\"" + newJiKeProdAttrDTO.getAttrCode() + "\",\"oldValue\":\"" + oldValue + "\",\"newValue\":\"" + newValue + "\"},");
            // 集客属性中文名称需要转换
            String name = getPropertyNameByAttrCode(newJiKeProdAttrDTO.getAttrCode());
            changeMessage.append("{\"key\":\"" + name + "\",\"oldValue\":\"" + oldValue + "\",\"newValue\":\"" + newValue + "\"},");
        }
        Map<String, String> map = new HashMap<>();
        map.put(CHANGE_DATA, changeData.toString());
        map.put(CHANGE_MESSAGE, changeMessage.toString());
        return map;
    }

    private String getPropertyNameByAttrCode(String attrCode){
        List<String> nameList = exceptionFlowDao.getPropertyNameById(attrCode);
        String name;
        if(nameList != null && !nameList.isEmpty()){
            name = nameList.get(0);
        }else{
            name = attrCode;
        }
        return name;
    }

    private String getCodeContent(String codeType, String codeValue){
        List<String> codeContentList = exceptionFlowDao.getCodeContent(codeType, codeValue);
        String codeContent;
        if(codeContentList != null && !codeContentList.isEmpty()){
            codeContent = codeContentList.get(0);
        }else{
            codeContent = codeValue;
        }
        return codeContent;
    }
    //------集客异常单end---------------

    public void updateOrderReqFinDate(String srvOrdId,  String reqFinDate){
        exceptionFlowDao.updateOrderReqFinDate(srvOrdId,  reqFinDate);
    }
}
