package com.zres.project.localnet.portal.converter;

import com.zres.project.localnet.portal.dict.dao.UnicomSysDictDao;
import com.zres.project.localnet.portal.initApplOrderDetail.domain.CircuitInfoPo;
import com.zres.project.localnet.portal.local.domain.BaseObject;
import org.easy.excel.config.FieldValue;
import org.easy.excel.exception.ExcelException;
import org.easy.excel.parsing.CellValueConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通过产品类型名称查询产品类型Code
 */
@Component
public class DictCodeCellValueConverter implements CellValueConverter {

    @Autowired
    private UnicomSysDictDao unicomSysDictDao;

    @Override
    public Object convert(Object bean, Object value, FieldValue fieldValue, Type type, int rowNum) throws Exception {
        //如果是导入
        if(type==Type.IMPORT){
            if(value != null){
                if("serviceName".equals(fieldValue.getName())){//产品类型
                    List<BaseObject> baseObjects = this.queryCodeForDb("product_code",(String) value);
                    if(!CollectionUtils.isEmpty(baseObjects)){
                        ((CircuitInfoPo) bean).setServiceId(baseObjects.get(0).getValue());
                    }else{
                        StringBuilder err = new StringBuilder()
                                .append("第[").append(rowNum).append("行],[")
                                .append(fieldValue.getTitle()).append("]")
                                .append("在数据库中没有找到["+value.toString()+"]的产品类型信息");
                        throw new ExcelException(err.toString());
                    }

                }else if("speedName".equals(fieldValue.getName())){//带宽
                    List<BaseObject> baseObjects = this.queryCodeForDb("10000102",(String) value);
                    if(!CollectionUtils.isEmpty(baseObjects)){
                        ((CircuitInfoPo) bean).setSpeed(baseObjects.get(0).getValue());
                    }else{
                        StringBuilder err = new StringBuilder()
                                .append("第[").append(rowNum).append("行],[")
                                .append(fieldValue.getTitle()).append("]")
                                .append("在数据库中没有找到["+value.toString()+"]的带宽信息");
                        throw new ExcelException(err.toString());
                    }
                }else if("slaFlagName".equals(fieldValue.getName())){//SLA标识
                    List<BaseObject> baseObjects = this.queryCodeForDb("10000516",(String) value);
                    if(!CollectionUtils.isEmpty(baseObjects)){
                        ((CircuitInfoPo) bean).setSlaFlag(baseObjects.get(0).getValue());
                    }else{
                        StringBuilder err = new StringBuilder()
                                .append("第[").append(rowNum).append("行],[")
                                .append(fieldValue.getTitle()).append("]")
                                .append("在数据库中没有找到["+value.toString()+"]的SLA标识信息");
                        throw new ExcelException(err.toString());
                    }
                }else if("slaServOpenName".equals(fieldValue.getName())){//SLA_业务开通
                    List<BaseObject> baseObjects = this.queryCodeForDb("10000513",(String) value);
                    if(!CollectionUtils.isEmpty(baseObjects)){
                        ((CircuitInfoPo) bean).setSlaServOpen(baseObjects.get(0).getValue());
                    }else{
                        StringBuilder err = new StringBuilder()
                                .append("第[").append(rowNum).append("行],[")
                                .append(fieldValue.getTitle()).append("]")
                                .append("在数据库中没有找到["+value.toString()+"]的SLA_业务开通信息");
                        throw new ExcelException(err.toString());
                    }
                }else if("slaNetQuAssName".equals(fieldValue.getName())){//SLA_网络质量保证
                    List<BaseObject> baseObjects = this.queryCodeForDb("10000514",(String) value);
                    if(!CollectionUtils.isEmpty(baseObjects)){
                        ((CircuitInfoPo) bean).setSlaNetQuAss(baseObjects.get(0).getValue());
                    }else{
                        StringBuilder err = new StringBuilder()
                                .append("第[").append(rowNum).append("行],[")
                                .append(fieldValue.getTitle()).append("]")
                                .append("在数据库中没有找到["+value.toString()+"]的SLA_网络质量保证信息");
                        throw new ExcelException(err.toString());
                    }
                }else if("slaSaleServName".equals(fieldValue.getName())){//SLA_售后服务
                    List<BaseObject> baseObjects = this.queryCodeForDb("10000515",(String) value);
                    if(!CollectionUtils.isEmpty(baseObjects)){
                        ((CircuitInfoPo) bean).setSlaSaleServ(baseObjects.get(0).getValue());
                    }else{
                        StringBuilder err = new StringBuilder()
                                .append("第[").append(rowNum).append("行],[")
                                .append(fieldValue.getTitle()).append("]")
                                .append("在数据库中没有找到["+value.toString()+"]的SLA_售后服务信息");
                        throw new ExcelException(err.toString());
                    }
                }else if("proPriDegName".equals(fieldValue.getName())){//工程缓急程度
                    List<BaseObject> baseObjects = this.queryCodeForDb("CON0095",(String) value);
                    if(!CollectionUtils.isEmpty(baseObjects)){
                        ((CircuitInfoPo) bean).setProPriDeg(baseObjects.get(0).getValue());
                    }else{
                        StringBuilder err = new StringBuilder()
                                .append("第[").append(rowNum).append("行],[")
                                .append(fieldValue.getTitle()).append("]")
                                .append("在数据库中没有找到["+value.toString()+"]的工程缓急程度信息");
                        throw new ExcelException(err.toString());
                    }
                }else if("cirLeaseRangeName".equals(fieldValue.getName())){//电路租用范围
                    List<BaseObject> baseObjects = this.queryCodeForDb("10000101",(String) value);
                    if(!CollectionUtils.isEmpty(baseObjects)){
                        ((CircuitInfoPo) bean).setCirLeaseRange(baseObjects.get(0).getValue());
                    }else{
                        StringBuilder err = new StringBuilder()
                                .append("第[").append(rowNum).append("行],[")
                                .append(fieldValue.getTitle()).append("]")
                                .append("在数据库中没有找到["+value.toString()+"]的电路租用范围信息");
                        throw new ExcelException(err.toString());
                    }
                }else if("cirUseName".equals(fieldValue.getName())){//电路用途
                    List<BaseObject> baseObjects = this.queryCodeForDb("10000506",(String) value);
                    if(!CollectionUtils.isEmpty(baseObjects)){
                        ((CircuitInfoPo) bean).setCirUse(baseObjects.get(0).getValue());
                    }else{
                        StringBuilder err = new StringBuilder()
                                .append("第[").append(rowNum).append("行],[")
                                .append(fieldValue.getTitle()).append("]")
                                .append("在数据库中没有找到["+value.toString()+"]的电路用途信息");
                        throw new ExcelException(err.toString());
                    }
                }else if("a_interface_type_Name".equals(fieldValue.getName())){//A端接口类型
                    List<BaseObject> baseObjects = this.queryCodeForDb("10000107",(String) value);
                    if(!CollectionUtils.isEmpty(baseObjects)){
                        ((CircuitInfoPo) bean).setA_interface_type(baseObjects.get(0).getValue());
                    }else{
                        StringBuilder err = new StringBuilder()
                                .append("第[").append(rowNum).append("行],[")
                                .append(fieldValue.getTitle()).append("]")
                                .append("在数据库中没有找到["+value.toString()+"]的A端接口类型信息");
                        throw new ExcelException(err.toString());
                    }
                }else if("z_interface_type_Name".equals(fieldValue.getName())){//Z端接口类型
                    List<BaseObject> baseObjects = this.queryCodeForDb("10000114",(String) value);
                    if(!CollectionUtils.isEmpty(baseObjects)){
                        ((CircuitInfoPo) bean).setZ_interface_type(baseObjects.get(0).getValue());
                    }else{
                        StringBuilder err = new StringBuilder()
                                .append("第[").append(rowNum).append("行],[")
                                .append(fieldValue.getTitle()).append("]")
                                .append("在数据库中没有找到["+value.toString()+"]的Z端接口类型信息");
                        throw new ExcelException(err.toString());
                    }
                }else if("a_belong_provinceName".equals(fieldValue.getName())){ //A端归属省
                    if(value != null){
                        List<Map<String, Object>> mapsArea = this.queryAreaIdByName((String) value);
                        if(!CollectionUtils.isEmpty(mapsArea)){
                            Map<String, Object> stringObjectMap = mapsArea.get(0);
                            ((CircuitInfoPo) bean).setA_belong_province((String)stringObjectMap.get("ID"));
                        }else{
                            StringBuilder err = new StringBuilder()
                                    .append("第[").append(rowNum).append("行],[")
                                    .append(fieldValue.getTitle()).append("]")
                                    .append("在数据库中没有找到["+value.toString()+"]的A端归属省信息");
                            throw new ExcelException(err.toString());
                        }
                    }

                }else if("a_belong_cityName".equals(fieldValue.getName())){ //A端归属地市
                    if(value != null){
                        List<Map<String, Object>> mapsArea = this.queryAreaIdByName((String) value);
                        if(!CollectionUtils.isEmpty(mapsArea)){
                            Map<String, Object> stringObjectMap = mapsArea.get(0);
                            ((CircuitInfoPo) bean).setA_belong_city((String)stringObjectMap.get("ID"));
                        }else{
                            StringBuilder err = new StringBuilder()
                                    .append("第[").append(rowNum).append("行],[")
                                    .append(fieldValue.getTitle()).append("]")
                                    .append("在数据库中没有找到["+value.toString()+"]的A端归属地市信息");
                            throw new ExcelException(err.toString());
                        }
                    }

                }else if("a_belong_countyName".equals(fieldValue.getName())){ //A端归属区县
                    if(value != null){
                        List<Map<String, Object>> mapsArea = this.queryAreaIdByName((String) value);
                        if(!CollectionUtils.isEmpty(mapsArea)){
                            Map<String, Object> stringObjectMap = mapsArea.get(0);
                            ((CircuitInfoPo) bean).setA_belong_county((String)stringObjectMap.get("ID"));
                        }else{
                            StringBuilder err = new StringBuilder()
                                    .append("第[").append(rowNum).append("行],[")
                                    .append(fieldValue.getTitle()).append("]")
                                    .append("在数据库中没有找到["+value.toString()+"]的A端归属区县信息");
                            throw new ExcelException(err.toString());
                        }
                    }

                }else if("a_belong_regionName".equals(fieldValue.getName())){ //A端归属分公司
                    if(value != null){
                        List<Map<String, Object>> mapsArea = this.queryDeptIdByName((String) value);
                        if(!CollectionUtils.isEmpty(mapsArea)){
                            Map<String, Object> stringObjectMap = mapsArea.get(0);
                            ((CircuitInfoPo) bean).setA_belong_region((String)stringObjectMap.get("ID"));
                        }else{
                            StringBuilder err = new StringBuilder()
                                    .append("第[").append(rowNum).append("行],[")
                                    .append(fieldValue.getTitle()).append("]")
                                    .append("在数据库中没有找到["+value.toString()+"]的A端归属分公司信息");
                            throw new ExcelException(err.toString());
                        }
                    }

                }else if("z_belong_provinceName".equals(fieldValue.getName())){
                    if(value != null){
                        List<Map<String, Object>> mapsArea = this.queryAreaIdByName((String) value);
                        if(!CollectionUtils.isEmpty(mapsArea)){
                            Map<String, Object> stringObjectMap = mapsArea.get(0);
                            ((CircuitInfoPo) bean).setZ_belong_province((String)stringObjectMap.get("ID"));
                        }else{
                            StringBuilder err = new StringBuilder()
                                    .append("第[").append(rowNum).append("行],[")
                                    .append(fieldValue.getTitle()).append("]")
                                    .append("在数据库中没有找到["+value.toString()+"]的Z端归属省信息");
                            throw new ExcelException(err.toString());
                        }
                    }

                }else if("z_belong_cityName".equals(fieldValue.getName())){
                    if(value != null){
                        List<Map<String, Object>> mapsArea = this.queryAreaIdByName((String) value);
                        if(!CollectionUtils.isEmpty(mapsArea)){
                            Map<String, Object> stringObjectMap = mapsArea.get(0);
                            ((CircuitInfoPo) bean).setZ_belong_city((String)stringObjectMap.get("ID"));
                        }else{
                            StringBuilder err = new StringBuilder()
                                    .append("第[").append(rowNum).append("行],[")
                                    .append(fieldValue.getTitle()).append("]")
                                    .append("在数据库中没有找到["+value.toString()+"]的Z端归属地市信息");
                            throw new ExcelException(err.toString());
                        }
                    }

                }else if("z_belong_countyName".equals(fieldValue.getName())){
                    if(value != null){
                        List<Map<String, Object>> mapsArea = this.queryAreaIdByName((String) value);
                        if(!CollectionUtils.isEmpty(mapsArea)){
                            Map<String, Object> stringObjectMap = mapsArea.get(0);
                            ((CircuitInfoPo) bean).setZ_belong_county((String)stringObjectMap.get("ID"));
                        }else{
                            StringBuilder err = new StringBuilder()
                                    .append("第[").append(rowNum).append("行],[")
                                    .append(fieldValue.getTitle()).append("]")
                                    .append("在数据库中没有找到["+value.toString()+"]的Z端归属区县信息");
                            throw new ExcelException(err.toString());
                        }
                    }

                }else if("z_belong_regionName".equals(fieldValue.getName())){
                    if(value != null){
                        List<Map<String, Object>> mapsArea = this.queryDeptIdByName((String) value);
                        if(!CollectionUtils.isEmpty(mapsArea)){
                            Map<String, Object> stringObjectMap = mapsArea.get(0);
                            ((CircuitInfoPo) bean).setZ_belong_region((String)stringObjectMap.get("ID"));
                        }else{
                            StringBuilder err = new StringBuilder()
                                    .append("第[").append(rowNum).append("行],[")
                                    .append(fieldValue.getTitle()).append("]")
                                    .append("在数据库中没有找到["+value.toString()+"]的Z端归属分公司信息");
                            throw new ExcelException(err.toString());
                        }
                    }

                }else if("requFineTime".equals(fieldValue.getName())){
                    if(value != null){
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        String formatDateStr = sdf.format((Date) value);
                        ((CircuitInfoPo) bean).setRequFineTimeStr(formatDateStr);
                    }

                }else if("accepTime".equals(fieldValue.getName())){
                    if(value != null){
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        String formatDateStr = sdf.format((Date) value);
                        ((CircuitInfoPo) bean).setAccepTimeStr(formatDateStr);
                    }

                }

            }
        }
        return value;
    }

    //模拟查询数据库
    private List<BaseObject> queryCodeForDb(String codeType,String codeName){
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("codeType",codeType);
        params.put("codeName",codeName);
        List<BaseObject> baseObjects = unicomSysDictDao.querySysDictDataByName(params);
        return baseObjects;
    }

    private List<Map<String, Object>> queryAreaIdByName(String areaName){
        List<Map<String, Object>> mapsArea = unicomSysDictDao.queryAreaIdByName(areaName);
        return mapsArea;
    }
    private List<Map<String, Object>> queryDeptIdByName(String areaName){
        List<Map<String, Object>> mapsArea = unicomSysDictDao.queryDeptIdByName(areaName);
        return mapsArea;
    }




}
