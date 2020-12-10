package com.zres.project.localnet.portal.converter;

import com.zres.project.localnet.portal.dict.dao.UnicomSysDictDao;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.easy.excel.config.FieldValue;
import org.easy.excel.parsing.CellDropDownDaConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class DropDownCellValueConverter implements CellDropDownDaConverter {


    @Autowired
    private UnicomSysDictDao unicomSysDictDao;

    @Override
    public void convertDrop(Sheet sheet, FieldValue fieldValue, DropType type, DataValidationHelper dvHelper) throws Exception {
        String cellRowName = fieldValue.getCellRowName();
        if(DropType.EXPORT.equals(type)){
                if("serviceName".equals(fieldValue.getName())){//产品类型
                    List<String> baseObjects = this.queryNameFromDb("product_code",null);
                    if(!CollectionUtils.isEmpty(baseObjects)){
                        String[] strings = new String[baseObjects.size()];
                        baseObjects.toArray(strings);
                        this.setDropDownData(strings,sheet,cellRowName,dvHelper);
                    }

                }else if ("activeType".equals(fieldValue.getName())) {//动作类型
                    String product = "101,102,103";
                    String[] productArr = product.split(",");
                    List<String> baseObjects = this.queryNameFromDb("operate_type", productArr);
                    if (!CollectionUtils.isEmpty(baseObjects)) {
                        String[] strings = new String[baseObjects.size()];
                        baseObjects.toArray(strings);
                        this.setDropDownData(strings,sheet,cellRowName,dvHelper);
                    }

                }else if("speedName".equals(fieldValue.getName())){//带宽
                    List<String> baseObjects = this.queryNameFromDb("10000102",null);
                    if(!CollectionUtils.isEmpty(baseObjects)){
                        String[] strings = new String[baseObjects.size()];
                        baseObjects.toArray(strings);
//                        this.setDropDownData(strings,sheet,cellRowName,dvHelper);
                    }

                }else if("slaFlagName".equals(fieldValue.getName())){//SLA标识
                    List<String> baseObjects = this.queryNameFromDb("10000516",null);
                    if(!CollectionUtils.isEmpty(baseObjects)){
                        String[] strings = new String[baseObjects.size()];
                        baseObjects.toArray(strings);
                        this.setDropDownData(strings,sheet,cellRowName,dvHelper);
                    }

                }else if("slaServOpenName".equals(fieldValue.getName())){//SLA_业务开通
                    List<String> baseObjects = this.queryNameFromDb("10000513",null);
                    if(!CollectionUtils.isEmpty(baseObjects)){
                        String[] strings = new String[baseObjects.size()];
                        baseObjects.toArray(strings);
                        this.setDropDownData(strings,sheet,cellRowName,dvHelper);
                    }

                }else if("slaNetQuAssName".equals(fieldValue.getName())){//SLA_网络质量保证
                    List<String> baseObjects = this.queryNameFromDb("10000514",null);
                    if(!CollectionUtils.isEmpty(baseObjects)){
                        String[] strings = new String[baseObjects.size()];
                        baseObjects.toArray(strings);
                        this.setDropDownData(strings,sheet,cellRowName,dvHelper);
                    }

                }else if("slaSaleServName".equals(fieldValue.getName())){//SLA_售后服务
                    List<String> baseObjects = this.queryNameFromDb("10000515",null);
                    if(!CollectionUtils.isEmpty(baseObjects)){
                        String[] strings = new String[baseObjects.size()];
                        baseObjects.toArray(strings);
                        this.setDropDownData(strings,sheet,cellRowName,dvHelper);
                    }

                }else if("proPriDegName".equals(fieldValue.getName())){//工程缓急程度
                    List<String> baseObjects = this.queryNameFromDb("CON0095",null);
                    if(!CollectionUtils.isEmpty(baseObjects)){
                        String[] strings = new String[baseObjects.size()];
                        baseObjects.toArray(strings);
                        this.setDropDownData(strings,sheet,cellRowName,dvHelper);
                    }

                }else if("cirLeaseRangeName".equals(fieldValue.getName())){//电路租用范围
                    List<String> baseObjects = this.queryNameFromDb("10000101",null);
                    if(!CollectionUtils.isEmpty(baseObjects)){
                        String[] strings = new String[baseObjects.size()];
                        baseObjects.toArray(strings);
                        this.setDropDownData(strings,sheet,cellRowName,dvHelper);
                    }

                }else if("cirUseName".equals(fieldValue.getName())){//电路用途
                    List<String> baseObjects = this.queryNameFromDb("10000506",null);
                    if(!CollectionUtils.isEmpty(baseObjects)){
                        String[] strings = new String[baseObjects.size()];
                        baseObjects.toArray(strings);
 //                       this.setDropDownData(strings,sheet,cellRowName,dvHelper);
                    }

                }else if("count".equals(fieldValue.getName())){//电路数量默认1
                    String[] strings ={"1"};
                    this.setDropDownData(strings,sheet,cellRowName,dvHelper);

                }else if("RelayTypeName".equals(fieldValue.getName())){//中继类型
                    List<String> baseObjects = this.queryNameFromDb("relayType",null);
                    if(!CollectionUtils.isEmpty(baseObjects)){
                        String[] strings = new String[baseObjects.size()];
                        baseObjects.toArray(strings);
 //                       this.setDropDownData(strings,sheet,cellRowName,dvHelper);
                    }

                }else if("a_interface_type_name".equals(fieldValue.getName())){//A端接口类型
                    List<String> baseObjects = this.queryNameFromDb("10000107",null);
                    if(!CollectionUtils.isEmpty(baseObjects)){
                        String[] strings = new String[baseObjects.size()];
                        baseObjects.toArray(strings);
  //                      this.setDropDownData(strings,sheet,cellRowName,dvHelper);
                    }

                }else if("z_interface_type_Name".equals(fieldValue.getName())) {//Z端接口类型
                    List<String> baseObjects = this.queryNameFromDb("10000114",null);
                    if(!CollectionUtils.isEmpty(baseObjects)){
                        String[] strings = new String[baseObjects.size()];
                        baseObjects.toArray(strings);
//                        this.setDropDownData(strings,sheet,cellRowName,dvHelper);
                    }
                }

        }


    }


    //模拟查询数据库
    private List<String> queryNameFromDb(String codeType,  String[] codeValue){
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("codeType",codeType);
        params.put("codeValue", codeValue);
        List<String> baseObjects = unicomSysDictDao.querySysDictDataName(params);
        return baseObjects;
    }

    /**
     *
     * @param textlist 下拉数据
     * @param sheet 表单
     * @param cellRowName 哪行哪列
     */
    private void setDropDownData(String[] textlist,Sheet sheet,String cellRowName, DataValidationHelper dvHelper){
//        String[] textlist = {"B1","D1"};
        XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(textlist);//strs下拉菜单的数据数组例如:String[] strs = {"A","B","C"}
        CellRangeAddressList addressList = new CellRangeAddressList();
        CellRangeAddress cellRangeAddress = CellRangeAddress.valueOf(cellRowName);
        addressList.addCellRangeAddress(cellRangeAddress);
        XSSFDataValidation validation = (XSSFDataValidation)dvHelper.createValidation(dvConstraint, addressList);
        //设置只能选下拉菜单里的值不能随便输入
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);

    }



}
