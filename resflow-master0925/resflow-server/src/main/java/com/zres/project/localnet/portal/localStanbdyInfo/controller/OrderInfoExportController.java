package com.zres.project.localnet.portal.localStanbdyInfo.controller;

import com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf;
import com.zres.project.localnet.portal.localStandbyInfo.service.OrderStandbyServiceIntf;
import com.zres.project.localnet.portal.util.ExcelExporter;
import com.zres.project.localnet.portal.util.ResponseHandler;
import com.ztesoft.res.frame.core.exception.ServiceBuizException;
import com.ztesoft.zsmart.pot.annotation.PublicServ;
import net.sf.json.JSONObject;
import org.apache.commons.collections.MapUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description :
 * @date : 2019/2/26
 */
@Controller
@RequestMapping("/localScheduleLT/orderInfoExportController")
public class OrderInfoExportController {
    private static final String[] TASKINFOCEL = {"业务号码",  "任务名称",   "所属部门",    "执行岗位",    "处理人", "流程环节",  "工单状态",    "处理结果",    "签收时间",    "开始时间", "完成时间",    "耗时(分)",    "超时时间",  "预警时间", "超时类型"};
    Logger logger = LoggerFactory.getLogger(OrderInfoExportController.class);
    @Autowired
    private OrderStandbyServiceIntf orderStandbyServiceIntf;
    @Autowired
    private OrderDetailsServiceIntf orderDetailsServiceIntf;

    @PublicServ
    @RequestMapping(value = "/exportOrderInfo.spr")
    public void exportOrderInfoExcel(@RequestBody String downLoadData, HttpServletRequest request, HttpServletResponse response) {
        try {
            ResponseHandler responseHandler = new ResponseHandler(request, response);
            String decode = URLDecoder.decode(downLoadData, "UTF-8").substring(13);
            int csrf = decode.lastIndexOf("&_csrf");
            String csrfStr = decode.substring(0,csrf);
            Map<String, Object> params = JSONObject.fromObject(csrfStr);
            String cstOrdId = String.valueOf(params.get("cstOrdId"));
            //查询类型
            String selectType = MapUtils.getString(params, "selectType");
            Map<String, Object> stringObjectMap = orderStandbyServiceIntf.qryCustInfo(cstOrdId);
            List<Map<String, Object>> cstInfo = (List<Map<String, Object>>) stringObjectMap.get("cstInfo");
            Map<String, Object> cstInfoMap = cstInfo.get(0);
            StringBuffer fileName = new StringBuffer();
            String custName = MapUtils.getString(cstInfoMap, "CUST_NAME_CHINESE");
            String orderType = MapUtils.getString(cstInfoMap, "ORDER_TYPE");
            String applyOrdId = MapUtils.getString(cstInfoMap, "APPLY_ORD_ID");
            if(!"".equals(custName) && custName != null){
                fileName.append(custName).append("_").append(orderType).append("_").append(applyOrdId);
            }else {
                fileName.append(orderType).append("_").append(applyOrdId);
            }
            responseHandler.setResponseFile(fileName.toString());
            ExcelExporter exporter = new ExcelExporter(responseHandler);
            XSSFCellStyle headerStyle = exporter.createHeaderStyle();
            XSSFCellStyle contextStyle = exporter.createContextStyle();
            XSSFSheet rowsSheet = exporter.initSheet("申请单信息");

            // 创建合并单元格  并在sheet里增加合并单元格 ---begin
            rowsSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 11)); // 下标从0开始 起始行号，终止行号， 起始列号，终止列号      申请单信息
            rowsSheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 2)); // 起始行号，终止行号， 起始列号，终止列号                申请编号
            rowsSheet.addMergedRegion(new CellRangeAddress(1, 1, 4, 5)); // 起始行号，终止行号， 起始列号，终止列号                申请人
            rowsSheet.addMergedRegion(new CellRangeAddress(1, 1, 7, 8)); // 起始行号，终止行号， 起始列号，终止列号                申请部门
            rowsSheet.addMergedRegion(new CellRangeAddress(1, 1, 10, 11)); // 起始行号，终止行号， 起始列号，终止列号                申请部门
            rowsSheet.addMergedRegion(new CellRangeAddress(2, 2, 1, 2)); // 起始行号，终止行号， 起始列号，终止列号                竞合方式
            rowsSheet.addMergedRegion(new CellRangeAddress(2, 2, 4, 5)); // 起始行号，终止行号， 起始列号，终止列号                其他运营商流水号
            rowsSheet.addMergedRegion(new CellRangeAddress(2, 2, 7, 8)); // 起始行号，终止行号， 起始列号，终止列号                业务协调部门
            rowsSheet.addMergedRegion(new CellRangeAddress(2, 2, 10, 11)); // 起始行号，终止行号， 起始列号，终止列号                业务协调部门
            rowsSheet.addMergedRegion(new CellRangeAddress(3, 3, 1, 2)); // 起始行号，终止行号， 起始列号，终止列号                申请时间
            rowsSheet.addMergedRegion(new CellRangeAddress(3, 3, 4, 5)); // 起始行号，终止行号， 起始列号，终止列号                联系带电话
            rowsSheet.addMergedRegion(new CellRangeAddress(3, 3, 7, 8)); // 起始行号，终止行号， 起始列号，终止列号                传真
            rowsSheet.addMergedRegion(new CellRangeAddress(3, 3, 10, 11)); // 起始行号，终止行号， 起始列号，终止列号                传真
            rowsSheet.addMergedRegion(new CellRangeAddress(4, 4, 1, 11)); // 下标从0开始 起始行号，终止行号， 起始列号，终止列号      申请内容
            rowsSheet.addMergedRegion(new CellRangeAddress(5, 5, 1, 2)); // 起始行号，终止行号， 起始列号，终止列号                发起单位
            rowsSheet.addMergedRegion(new CellRangeAddress(5, 5, 4, 5)); // 起始行号，终止行号， 起始列号，终止列号                发起人信息
            rowsSheet.addMergedRegion(new CellRangeAddress(5, 5, 7, 8)); // 起始行号，终止行号， 起始列号，终止列号                批次
            rowsSheet.addMergedRegion(new CellRangeAddress(5, 5, 10, 11)); // 起始行号，终止行号， 起始列号，终止列号                批次
            rowsSheet.addMergedRegion(new CellRangeAddress(6, 6, 1, 2)); // 起始行号，终止行号， 起始列号，终止列号                集团项目经理信息
            rowsSheet.addMergedRegion(new CellRangeAddress(6, 6, 4, 5)); // 起始行号，终止行号， 起始列号，终止列号                省项目经理信息
            rowsSheet.addMergedRegion(new CellRangeAddress(6, 6, 7, 8)); // 起始行号，终止行号， 起始列号，终止列号                省项目经理信息
            rowsSheet.addMergedRegion(new CellRangeAddress(6, 6, 10, 11)); // 起始行号，终止行号， 起始列号，终止列号                客户行业
            rowsSheet.addMergedRegion(new CellRangeAddress(7, 7, 1, 11)); // 下标从0开始 起始行号，终止行号， 起始列号，终止列号      备注
            rowsSheet.addMergedRegion(new CellRangeAddress(8, 8, 1, 2)); // 起始行号，终止行号， 起始列号，终止列号                客户编号
            rowsSheet.addMergedRegion(new CellRangeAddress(8, 8, 4, 5)); // 起始行号，终止行号， 起始列号，终止列号                客户名称
            rowsSheet.addMergedRegion(new CellRangeAddress(8, 8, 7, 8)); // 起始行号，终止行号， 起始列号，终止列号                合同编号
            rowsSheet.addMergedRegion(new CellRangeAddress(8, 8, 10, 11)); // 起始行号，终止行号， 起始列号，终止列号                合同编号

            rowsSheet.addMergedRegion(new CellRangeAddress(9, 9, 1, 2)); // 起始行号，终止行号， 起始列号，终止列号                客户经理
            rowsSheet.addMergedRegion(new CellRangeAddress(9, 9, 4, 5)); // 起始行号，终止行号， 起始列号，终止列号                客户地址
            rowsSheet.addMergedRegion(new CellRangeAddress(9, 9, 7, 8)); // 起始行号，终止行号， 起始列号，终止列号                客户行业
            rowsSheet.addMergedRegion(new CellRangeAddress(9, 9, 10, 11)); // 起始行号，终止行号， 起始列号，终止列号                客户经理电话

            rowsSheet.addMergedRegion(new CellRangeAddress(10, 10, 1, 2)); // 起始行号，终止行号， 起始列号，终止列号                客户联系人
            rowsSheet.addMergedRegion(new CellRangeAddress(10, 10, 4, 5)); // 起始行号，终止行号， 起始列号，终止列号                联系电话
            rowsSheet.addMergedRegion(new CellRangeAddress(10, 10, 7, 8)); // 起始行号，终止行号， 起始列号，终止列号                客户Email
            rowsSheet.addMergedRegion(new CellRangeAddress(10, 10, 10, 11)); // 起始行号，终止行号， 起始列号，终止列号                客户Email
            //设置列宽
            rowsSheet.setColumnWidth(0, 30 * 200);
            rowsSheet.setColumnWidth(1, 30 * 150);
            rowsSheet.setColumnWidth(2, 30 * 150);
            rowsSheet.setColumnWidth(3, 30 * 200);
            rowsSheet.setColumnWidth(5, 30 * 150);
            rowsSheet.setColumnWidth(7, 30 * 150);
            rowsSheet.setColumnWidth(8, 30 * 150);
            rowsSheet.setColumnWidth(9, 30 * 150);
            rowsSheet.setColumnWidth(10, 30 * 150);
            rowsSheet.setColumnWidth(6, 30 * 200);
            rowsSheet.setColumnWidth(4, 30 * 150);
            // -----------填充第一行数据-------------
            Row rowTitle = rowsSheet.createRow(0);
            Cell cell = rowTitle.createCell(0);
            cell.setCellValue("申请单信息"); // 设置标题内容
            cell.setCellStyle(headerStyle);
            rowTitle.setHeight((short) 500);
            // -----------填充第二行数据-------------
            Row rowTitle1 = rowsSheet.createRow(1);
            Cell rows120 = rowTitle1.createCell(0);
            rows120.setCellValue("申请单编号"); //  设置内容
            rows120.setCellStyle(headerStyle);
            Cell rows123 = rowTitle1.createCell(3);
            rows123.setCellValue("受理人");
            rows123.setCellStyle(headerStyle);
            Cell rows126 = rowTitle1.createCell(6);
            rows126.setCellValue("受理部门");
            rows126.setCellStyle(headerStyle);
            // -----------填充第三行数据-------------
            Row rowTitle2 = rowsSheet.createRow(2);
            Cell rows130 = rowTitle2.createCell(0);
            rows130.setCellValue("竞合方式");
            rows130.setCellStyle(headerStyle);
            Cell rows133 = rowTitle2.createCell(3);
            rows133.setCellValue("其他运营商流水号");
            rows133.setCellStyle(headerStyle);
            Cell rows136 = rowTitle2.createCell(6);
            rows136.setCellValue("业务协调部门"); //  设置内容
            rows136.setCellStyle(headerStyle);
            // -----------填充第四行数据-------------
            Row rowTitle3 = rowsSheet.createRow(3);
            Cell rows140 = rowTitle3.createCell(0);
            rows140.setCellValue("申请时间"); //  设置内容
            rows140.setCellStyle(headerStyle);
            Cell rows143 = rowTitle3.createCell(3);
            rows143.setCellValue("联系电话");
            rows143.setCellStyle(headerStyle);
            Cell rows146 = rowTitle3.createCell(6);
            rows146.setCellValue("传真");
            rows146.setCellStyle(headerStyle);
            // -----------填充第五行数据-------------
            Row rowTitle4 = rowsSheet.createRow(4);
            Cell rows150 = rowTitle4.createCell(0);
            rows150.setCellValue("申请内容"); //  设置内容
            rows150.setCellStyle(headerStyle);
            // -----------填充第六行数据-------------
            Row rowTitle5 = rowsSheet.createRow(5);
            Cell rows160 = rowTitle5.createCell(0);
            rows160.setCellValue("发起单位"); //  设置内容
            rows160.setCellStyle(headerStyle);
            Cell rows163 = rowTitle5.createCell(3);
            rows163.setCellValue("发起人信息"); //  设置内容
            rows163.setCellStyle(headerStyle);
            Cell rows166 = rowTitle5.createCell(6);
            rows166.setCellValue("批次"); //  设置内容
            rows166.setCellStyle(headerStyle);
            // -----------填充第七行数据-------------
            Row rowTitle6 = rowsSheet.createRow(6);
            Cell rows170 = rowTitle6.createCell(0);
            rows170.setCellValue("集团项目经理信息"); //  设置内容
            rows170.setCellStyle(headerStyle);
            Cell rows173 = rowTitle6.createCell(3);
            rows173.setCellValue("省项目经理信息");
            rows173.setCellStyle(headerStyle);
            Cell rows176 = rowTitle6.createCell(6);
            rows176.setCellValue("客户行业");
            rows176.setCellStyle(headerStyle);
            // -----------填充第八行数据-------------
            Row rowTitle7 = rowsSheet.createRow(7);
            Cell rows180 = rowTitle7.createCell(0);
            rows180.setCellValue("备注"); //  设置内容
            rows180.setCellStyle(headerStyle);
            // -----------填充第九行数据-------------
            Row rowTitle8 = rowsSheet.createRow(8);
            Cell rows190 = rowTitle8.createCell(0);
            rows190.setCellValue("客户编号");
            rows190.setCellStyle(headerStyle);
            Cell rows193 = rowTitle8.createCell(3);
            rows193.setCellValue("客户名称");
            rows193.setCellStyle(headerStyle);
            Cell rows196 = rowTitle8.createCell(6);
            rows196.setCellValue("合同编号");
            rows196.setCellStyle(headerStyle);
            // -----------填充第十行数据-------------
            Row rowTitle9 = rowsSheet.createRow(9);
            Cell rows100 = rowTitle9.createCell(0);
            rows100.setCellValue("客户经理");
            rows100.setCellStyle(headerStyle);
            Cell rows103 = rowTitle9.createCell(3);
            rows103.setCellValue("客户经理电话");
            rows103.setCellStyle(headerStyle);
            Cell rows106 = rowTitle9.createCell(6);
            rows106.setCellValue("是否集团直管客户");
            rows106.setCellStyle(headerStyle);
            Cell rows109 = rowTitle9.createCell(9);
            rows109.setCellValue("客户地址");
            rows109.setCellStyle(headerStyle);
            // -----------填充第十一行数据-------------
            Row rowTitle10 = rowsSheet.createRow(10);
            Cell rows110 = rowTitle10.createCell(0);
            rows110.setCellValue("客户联系人"); //  设置内容
            rows110.setCellStyle(headerStyle);
            Cell rows113 = rowTitle10.createCell(3);
            rows113.setCellValue("联系电话");
            rows113.setCellStyle(headerStyle);
            Cell rows116 = rowTitle10.createCell(6);
            rows116.setCellValue("客户Email");
            rows116.setCellStyle(headerStyle);
            rowTitle1.createCell(1).setCellValue((String) cstInfoMap.get("APPLY_ORD_ID")); //  设置内容
            rowTitle1.createCell(4).setCellValue((String) cstInfoMap.get("HANDLE_MAN_NAME"));
            rowTitle1.createCell(7).setCellValue((String) cstInfoMap.get("HANDLE_DEP"));
            rowTitle2.createCell(1).setCellValue((String) cstInfoMap.get("COOPERATION_MODE"));
            rowTitle2.createCell(4).setCellValue((String) cstInfoMap.get("OTHER_OPERA_SER_NUM"));
            rowTitle2.createCell(7).setCellValue((String) cstInfoMap.get("BUSINESS_COORD_DEP"));
            rowTitle3.createCell(1).setCellValue(cstInfoMap.get("HANDLE_TIME").toString()); //  设置内容
            rowTitle3.createCell(4).setCellValue((String) cstInfoMap.get("HANDLE_MAN_TEL"));
            rowTitle3.createCell(7).setCellValue("");
            rowTitle4.createCell(1).setCellValue((String) cstInfoMap.get("REMARK")); //  设置内容
            rowTitle5.createCell(1).setCellValue((String) cstInfoMap.get("CUST_ADDRESS")); //  设置内容
            rowTitle5.createCell(7).setCellValue((String) cstInfoMap.get("BATCH")); //  设置内容
            rowTitle6.createCell(1).setCellValue((String) cstInfoMap.get("GROUP_PM_NAME")); //  设置内容
            rowTitle6.createCell(4).setCellValue((String) cstInfoMap.get("PROVINCE_PM_NAME"));
            rowTitle6.createCell(7).setCellValue((String) cstInfoMap.get("CUST_INDUSTRY"));
            rowTitle7.createCell(1).setCellValue((String) cstInfoMap.get("REMARK")); //  设置内容
            rowTitle8.createCell(1).setCellValue((String) cstInfoMap.get("CUST_ID"));
            rowTitle8.createCell(4).setCellValue((String) cstInfoMap.get("CUST_NAME_CHINESE"));
            rowTitle8.createCell(7).setCellValue((String) cstInfoMap.get("CONTRACT_ID"));
            rowTitle9.createCell(1).setCellValue((String) cstInfoMap.get("INIT_AM_NAME"));
            rowTitle9.createCell(4).setCellValue((String) cstInfoMap.get("INIT_AM_TEL"));//客户经理电话
            rowTitle9.createCell(7).setCellValue((String) cstInfoMap.get("IS_GROUP_CUST"));
            rowTitle9.createCell(10).setCellValue((String) cstInfoMap.get("CUST_ADDRESS"));


            rowTitle10.createCell(1).setCellValue((String) cstInfoMap.get("CUST_CONTACT_MAN_NAME")); //  设置内容
            rowTitle10.createCell(4).setCellValue((String) cstInfoMap.get("CUST_CONTACT_MAN_TEL"));
            rowTitle10.createCell(7).setCellValue((String) cstInfoMap.get("CUST_CONTACT_MAN_EMAIL"));
            List<Map<String, Object>> datas = new ArrayList<>();
            Row row12222 = rowsSheet.createRow(11); //创建第十二行，标题

            try {
                Map<String, Object> srvOrderInfo = orderStandbyServiceIntf.qrySrvOrderInfo(cstOrdId);
                List<Map<String, Object>> srvOrderInfoMap = (List<Map<String, Object>>) srvOrderInfo.get("srvOrderInfo");

                int j = 0;
                int o = 2;

                //待办需要导出对应的电路信息调单信息
                if("localStandy".equals(selectType)){
                    List<Map<String, Object>> orderCircuitInfoList = orderStandbyServiceIntf.queryCircuitInfo(params);
                    // queryResourceOrderInfoW' 一干资源信息
                    List<Map<String, Object>> resourceOrderInfoW = new ArrayList<>();
                    // queryResourceOrderInfoY 本地资源信息
                    List<Map<String, Object>> resourceOrderInfoY = new ArrayList<>();
                    //queryResourceOrderInfo  二干资源信息
                    List<Map<String, Object>> resourceOrderInfo = new ArrayList<>();


                    for (int h = 0; h < orderCircuitInfoList.size(); h++) {   //获取电路信息
                        //获取每条电路的电路编号
                        XSSFRow row = rowsSheet.createRow(h + 12);
                        Cell cell001 = row.createCell(0);
                        cell001.setCellValue(j++);

                        Map<String, Object> orderCircuitInfo = orderCircuitInfoList.get(h);
                        List<Map<String, Object>>  circuitList = (List<Map<String, Object>> )orderCircuitInfo.get("circuitInfo");
                        String srvOrdIds = MapUtils.getString(orderCircuitInfo, "srvOrdId");
                        String srvOrdId = MapUtils.getString(params, "srvOrdId");
                        for (int i = 0; i < circuitList.size(); i++) {   //获取电路信息
                            Map<String, Object> custMap = circuitList.get(i);
                            String cellHeads = (String) custMap.get("ATTR_NAME");

                            //创建列名
                            if (h == 0) {
                                //创建列名
                                Cell rows00001 = row12222.createCell(0);
                                rows00001.setCellValue("序号");
                                rows00001.setCellStyle(headerStyle);
                                Cell rows12222 = row12222.createCell(i + 1);
                                rows12222.setCellValue(cellHeads);
                                rows12222.setCellStyle(headerStyle);
                                //填充第一条数据
                                Cell cell1 = row.createCell(i + 1);
                                cell1.setCellStyle(contextStyle);
                                cell1.setCellValue(MapUtils.getString(custMap, "ATTR_VALUE"));
                            }
                            else {
                                Cell cell1 = row.createCell(i + 1);
                                cell1.setCellStyle(contextStyle);
                                cell1.setCellValue((String) custMap.get("ATTR_VALUE"));
                            }
                        }
                        // queryResourceOrderInfoW' 一干资源信息
                        List<Map<String, Object>> resourceOrderInfoOne = orderDetailsServiceIntf.queryResourceOrderInfoW(srvOrdIds);
                        resourceOrderInfoW.addAll(resourceOrderInfoOne);
                        // queryResourceOrderInfoY 本地资源信息
                        List<Map<String, Object>> resourceOrderInfoLocal = orderDetailsServiceIntf.queryResourceOrderInfoY(srvOrdIds);
                        resourceOrderInfoY.addAll(resourceOrderInfoLocal);
                        //queryResourceOrderInfo  二干资源信息
                        List<Map<String, Object>> resourceOrderInfoSec = orderDetailsServiceIntf.queryResourceOrderInfo(srvOrdIds);
                        resourceOrderInfo.addAll(resourceOrderInfoSec);


                    }

                    if (resourceOrderInfo.size() > 0) {
                        this.initSheet(exporter, headerStyle, "二干资源信息", resourceOrderInfo);
                    }
                    if (resourceOrderInfoW.size() > 0) {
                        this.initSheet(exporter, headerStyle, "一干资源信息", resourceOrderInfoW);
                    }
                    if (resourceOrderInfoY.size() > 0) {
                        this.initSheet(exporter, headerStyle, "本地资源信息", resourceOrderInfoY);
                    }


                }
                else if ("gomQuery".equals(selectType)) {

                    XSSFRow row13 = rowsSheet.createRow(13);
                    Cell cell001 = row13.createCell(0);
                    cell001.setCellValue(j++);
                    Map<String, Object> queryCircuitInfo = new HashMap<>();
                    String srvOrdId = MapUtils.getString(params, "srvOrdId");
                    String  serviceId = MapUtils.getString(params, "serviceId");
                    queryCircuitInfo.put("srvOrdId", srvOrdId);
                    queryCircuitInfo.put("serviceId", serviceId);
                    Map<String, Object> orderCircuitInfo = orderStandbyServiceIntf.queryOrderCircuitInfo(queryCircuitInfo);

                    List<Map<String, Object>> orderCircuitInfoList = (List<Map<String, Object>>) orderCircuitInfo.get("orderCircuitInfo");
                    for (int i = 0; i < orderCircuitInfoList.size(); i++) {   //获取电路信息
                        Map<String, Object> custMap = orderCircuitInfoList.get(i);
                        String cellHeads = (String) custMap.get("ATTR_NAME");
                        //创建列名
                        Cell rows00001 = row12222.createCell(0);
                        rows00001.setCellValue("序号");
                        rows00001.setCellStyle(headerStyle);
                        Cell rows12222 = row12222.createCell(i + 1);
                        rows12222.setCellValue(cellHeads);
                        rows12222.setCellStyle(headerStyle);
                        Cell cell1 = row13.createCell(i + 1);
                        cell1.setCellValue((String) custMap.get("ATTR_VALUE"));
                    }
                    // queryResourceOrderInfoW' 一干资源信息
                    List<Map<String, Object>> resourceOrderInfoW = orderDetailsServiceIntf.queryResourceOrderInfoW(srvOrdId);
                    // queryResourceOrderInfoY 本地资源信息
                    List<Map<String, Object>> resourceOrderInfoY = orderDetailsServiceIntf.queryResourceOrderInfoY(srvOrdId);
                    //queryResourceOrderInfo  二干资源信息
                    List<Map<String, Object>> resourceOrderInfo = orderDetailsServiceIntf.queryResourceOrderInfo(srvOrdId);
                    if (resourceOrderInfo.size() > 0) {
                        this.initSheet(exporter, headerStyle, "二干资源信息", resourceOrderInfo);
                    }
                    if (resourceOrderInfoW.size() > 0) {
                        this.initSheet(exporter, headerStyle, "一干资源信息", resourceOrderInfoW);
                    }
                    if (resourceOrderInfoY.size() > 0) {
                        this.initSheet(exporter, headerStyle, "本地资源信息", resourceOrderInfoY);
                    }
                }
                else {
                    // queryResourceOrderInfoW' 一干资源信息
                    List<Map<String, Object>> resourceOrderInfoW = new ArrayList<>();
                    // queryResourceOrderInfoY 本地资源信息
                    List<Map<String, Object>> resourceOrderInfoY = new ArrayList<>();
                    //queryResourceOrderInfo  二干资源信息
                    List<Map<String, Object>> resourceOrderInfo = new ArrayList<>();

                    for (int k = 0; k < srvOrderInfoMap.size(); k++)  { //获取每条电路的 srvOrdId serviceId

                        XSSFRow row = rowsSheet.createRow(k + 12);
                        Cell cell001 = row.createCell(0);
                        cell001.setCellValue(j++);
                        Map<String, Object> queryCircuitInfo = new HashMap<>();
                        Map<String, Object> srvOrder = srvOrderInfoMap.get(k);
                        String srvOrdId = String.valueOf(srvOrder.get("SRV_ORD_ID"));
                        String  serviceId = String.valueOf(srvOrder.get("SERVICE_ID"));
                        queryCircuitInfo.put("srvOrdId", srvOrdId);
                        queryCircuitInfo.put("serviceId", serviceId);
                        Map<String, Object> orderCircuitInfo = orderStandbyServiceIntf.queryOrderCircuitInfo(queryCircuitInfo);
                        List<Map<String, Object>> orderCircuitInfoList = (List<Map<String, Object>>) orderCircuitInfo.get("orderCircuitInfo");
                        for (int i = 0; i < orderCircuitInfoList.size(); i++) {   //获取电路信息
                            Map<String, Object> custMap = orderCircuitInfoList.get(i);
                            String cellHeads = (String) custMap.get("ATTR_NAME");

                            //创建列名
                            if (k == 0) {
                                Cell rows00001 = row12222.createCell(0);
                                rows00001.setCellValue("序号");
                                rows00001.setCellStyle(headerStyle);
                                Cell rows12222 = row12222.createCell(i + 1);
                                rows12222.setCellValue(cellHeads);
                                rows12222.setCellStyle(headerStyle);
                                Cell cell1 = row.createCell(i + 1);
                                cell1.setCellStyle(contextStyle);
                                cell1.setCellValue(MapUtils.getString(custMap, "ATTR_VALUE"));
                            }
                            else {
                                Cell cell1 = row.createCell(i + 1);
                                cell1.setCellStyle(contextStyle);
                                cell1.setCellValue((String) custMap.get("ATTR_VALUE"));
                            }
                        }
                        // queryResourceOrderInfoW' 一干资源信息
                        List<Map<String, Object>> resourceOrderInfoOne = orderDetailsServiceIntf.queryResourceOrderInfoW(srvOrdId);
                        resourceOrderInfoW.addAll(resourceOrderInfoOne);
                        // queryResourceOrderInfoY 本地资源信息
                        List<Map<String, Object>> resourceOrderInfoLocal = orderDetailsServiceIntf.queryResourceOrderInfoY(srvOrdId);
                        resourceOrderInfoY.addAll(resourceOrderInfoLocal);
                        //queryResourceOrderInfo  二干资源信息
                        List<Map<String, Object>> resourceOrderInfoSec = orderDetailsServiceIntf.queryResourceOrderInfo(srvOrdId);
                        resourceOrderInfo.addAll(resourceOrderInfoSec);
                    }

                    if (resourceOrderInfo.size() > 0) {
                        this.initSheet(exporter, headerStyle, "二干资源信息", resourceOrderInfo);
                    }
                    if (resourceOrderInfoW.size() > 0) {
                        this.initSheet(exporter, headerStyle, "一干资源信息", resourceOrderInfoW);
                    }
                    if (resourceOrderInfoY.size() > 0) {
                        this.initSheet(exporter, headerStyle, "本地资源信息", resourceOrderInfoY);
                    }

                }
            }
            catch (Exception e) {
                throw new Exception(e.getMessage());
            }
            //查询调单信息
            List<Map<String, Object>> dispatchOrderList = orderStandbyServiceIntf.queryDispatchOrderInfo(params);
//            List<Map<String, Object>> dispatchOrderList = (List<Map<String, Object>>) dispatchOrderInfo.get("dispatchOrderInfo");
            if (dispatchOrderList.size() > 0) {
                for (int i = 0; i < dispatchOrderList.size(); i++) {
                    Map<String, Object> dispatchOrderMap = dispatchOrderList.get(i);
                    StringBuffer sheetTittle = new StringBuffer();
                    sheetTittle.append(MapUtils.getString(dispatchOrderMap, "DISPATCH_SOURCE")).append("-").append(MapUtils.getString(dispatchOrderMap, "DISPATCH_ORDER_ID")).append("_").append(i);
                    XSSFSheet rowsSheet2 = exporter.initSheet(sheetTittle.toString()); // 创建第二个sheet
                    //------------------------   创建第二个sheet页信息 -------------------------
                    // 创建合并单元格

                    XSSFRow rows0 = rowsSheet2.createRow(0);
                    XSSFCell cells0 = rows0.createCell(0);
                    rowsSheet2.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
                    cells0.setCellValue("网络资源调度单");
                    cells0.setCellStyle(headerStyle);
                    rows0.setHeight((short) 500);
//                    rowsSheet2.addMergedRegion(new CellRangeAddress(4, 4, 1, 5));
                    rowsSheet2.addMergedRegion(new CellRangeAddress(5, 5, 1, 5));
                    rowsSheet2.addMergedRegion(new CellRangeAddress(6, 6, 1, 5));
                    rowsSheet2.addMergedRegion(new CellRangeAddress(7, 7, 1, 5));
                    rowsSheet2.addMergedRegion(new CellRangeAddress(8, 8, 1, 5));
                    rowsSheet2.addMergedRegion(new CellRangeAddress(9, 9, 1, 5));

                    rowsSheet2.setColumnWidth(0, 30 * 200);
                    rowsSheet2.setColumnWidth(1, 30 * 300);
                    rowsSheet2.setColumnWidth(2, 30 * 200);
                    rowsSheet2.setColumnWidth(3, 30 * 300);
                    rowsSheet2.setColumnWidth(4, 30 * 200);
                    rowsSheet2.setColumnWidth(5, 30 * 300);
                    // -----------填充第二行数据-------------
                    Row rowTitles1 = rowsSheet2.createRow(1);
                    Cell rows220 = rowTitles1.createCell(0);
                    rows220.setCellValue("业务类型");
                    rows220.setCellStyle(headerStyle);
                    rowTitles1.createCell(1).setCellValue((String) dispatchOrderMap.get("DISPATCH_TYPE"));
                    Cell rows222 = rowTitles1.createCell(2);
                    rows222.setCellValue("拟稿人");
                    rows222.setCellStyle(headerStyle);
                    rowTitles1.createCell(3).setCellValue((String) dispatchOrderMap.get("STAFF_NAME"));
                    Cell rows224 = rowTitles1.createCell(4);
                    rows224.setCellValue("联系电话");
                    rows224.setCellStyle(headerStyle);
                    rowTitles1.createCell(5).setCellValue((String) dispatchOrderMap.get("STAFF_TEL"));
                    // -----------填充第三行数据-------------
                    Row rowTitles2 = rowsSheet2.createRow(2);
                    Cell rows230 = rowTitles2.createCell(0);
                    rows230.setCellValue("单位（盖章）");
                    rows230.setCellStyle(headerStyle);
                    rowTitles2.createCell(1).setCellValue((String) dispatchOrderMap.get("STAFF_ORG"));
                    Cell rows232 = rowTitles2.createCell(2);
                    rows232.setCellValue("签发人");
                    rows232.setCellStyle(headerStyle);
                    rowTitles2.createCell(3).setCellValue((String) dispatchOrderMap.get("ISSUER"));
                    Cell rows234 = rowTitles2.createCell(4);
                    rows234.setCellValue("发文时间");
                    rows234.setCellStyle(headerStyle);
                    rowTitles2.createCell(5).setCellValue((String) dispatchOrderMap.get("SEND_DATE"));
                    // -----------填充第四行数据-------------
                    Row rowTitles3 = rowsSheet2.createRow(3);
                    Cell rows240 = rowTitles3.createCell(0);
                    rows240.setCellValue("调单编号");
                    rows240.setCellStyle(headerStyle);
                    rowTitles3.createCell(1).setCellValue((String) dispatchOrderMap.get("DISPATCH_ORDER_NO"));
                    Cell rows242 = rowTitles3.createCell(2);
                    rows242.setCellValue("调单等级");
                    rows242.setCellStyle(headerStyle);
                    rowTitles3.createCell(3).setCellValue((String) dispatchOrderMap.get("DISPATCH_GRADE"));
                    Cell rows244 = rowTitles3.createCell(4);
                    rows244.setCellValue("调单缓急");
                    rows244.setCellStyle(headerStyle);
                    rowTitles3.createCell(5).setCellValue((String) dispatchOrderMap.get("DISPATCH_URGENCY"));

                    // -----------填充第五行数据-------------
                    Row rowTitles04 = rowsSheet2.createRow(4);
                    Cell rows2040 = rowTitles04.createCell(0);
                    rows2040.setCellValue("是否转资源分配");
                    rows2040.setCellStyle(headerStyle);
                    rowTitles04.createCell(1).setCellValue((String) dispatchOrderMap.get("RES_ALLOCATE"));
                    Cell rows2042 = rowTitles04.createCell(2);
                    rows2042.setCellValue("分配专业");
                    rows2042.setCellStyle(headerStyle);
                    rowTitles04.createCell(3).setCellValue((String) dispatchOrderMap.get("SPECIALTY"));
                    Cell rows2044 = rowTitles04.createCell(4);
                    rows2044.setCellValue("数据制作");
                    rows2044.setCellStyle(headerStyle);
                    rowTitles04.createCell(5).setCellValue((String) dispatchOrderMap.get("NETMANAGE"));


                    // -----------填充第五行数据-------------
                    Row rowTitles4 = rowsSheet2.createRow(5);
                    Cell rows250 = rowTitles4.createCell(0);
                    rows250.setCellValue("发往单位");
                    rows250.setCellStyle(headerStyle);
                    rowTitles4.createCell(1).setCellValue((String) dispatchOrderMap.get("DISPATCH_SEND_ORG"));
                    // -----------填充第六行数据-------------
                    Row rowTitles5 = rowsSheet2.createRow(6);
                    Cell rows260 = rowTitles5.createCell(0);
                    rows260.setCellValue("抄送单位");
                    rows260.setCellStyle(headerStyle);
                    rowTitles5.createCell(1).setCellValue((String) dispatchOrderMap.get("DISPATCH_COPY_ORG"));
                    // -----------填充第七行数据-------------
                    Row rowTitles6 = rowsSheet2.createRow(7);
                    Cell rows270 = rowTitles6.createCell(0);
                    rows270.setCellValue("附件");
                    rows270.setCellStyle(headerStyle);
                    rowTitles6.createCell(1).setCellValue("");
                    // -----------填充第八行数据-------------
                    Row rowTitles7 = rowsSheet2.createRow(8);
                    Cell rows280 = rowTitles7.createCell(0);
                    rows280.setCellValue("调单标题");
                    rows280.setCellStyle(headerStyle);
                    rowTitles7.createCell(1).setCellValue((String) dispatchOrderMap.get("DISPATCH_TITLE"));
                    // -----------填充第九行数据-------------
                    Row rowTitles8 = rowsSheet2.createRow(9);
                    rowTitles8.setHeight((short) 8000);
                    Cell rows290 = rowTitles8.createCell(0);
                    rows290.setCellValue("调单正文");
                    rows290.setCellStyle(headerStyle);
                    Cell cell21 = rowTitles8.createCell(1);
                    cell21.setCellValue((String) dispatchOrderMap.get("DISPATCH_TEXT"));
                    cell21.setCellStyle(contextStyle);
                }
            }
            //queryTaskInfo  二干任务列表信息
            List<Map<String, Object>> taskInfo = new ArrayList<>();
            //queryResourceOrderInfo  本地任务列表信息
            List<Map<String, Object>> secToLocalTaskInfo = new ArrayList<>();
            //查询二干任务列表
            List<Map<String, Object>> queryTaskInfo = orderStandbyServiceIntf.queryTaskInfo(params);
            taskInfo.addAll(queryTaskInfo);
            //查询本地任务列表
            List<Map<String, Object>> querySecToLocalTaskInfo = orderStandbyServiceIntf.querySecToLocalTaskInfo(params);
            secToLocalTaskInfo.addAll(querySecToLocalTaskInfo);
            if(taskInfo.size()>0){
                this.initTaskSheet(exporter,headerStyle, "二干任务列表", taskInfo);
            }
            if(secToLocalTaskInfo.size()>0){
                this.initTaskSheet(exporter,headerStyle, "本地任务列表", secToLocalTaskInfo);
            }

            if (!CollectionUtils.isEmpty(datas)) {
                exporter.writeSheet(rowsSheet, null, null, datas, 11);
            }
            exporter.export();
            logger.info("导出成功");
        }
        catch (Exception e) {
            throw new ServiceBuizException(e);
        }
    }

    //资源信息sheet页
    private  void initSheet(ExcelExporter exporter, XSSFCellStyle headerStyle, String name, List<Map<String, Object>> resourceOrderInfo) {
        XSSFSheet rowsSheet3 = exporter.initSheet(name);
        rowsSheet3.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
        XSSFRow rows30 = rowsSheet3.createRow(0);
        XSSFCell cells30 = rows30.createCell(0);
        cells30.setCellValue("资源信息");
        cells30.setCellStyle(headerStyle);
        rows30.setHeight((short) 500);
        String[] collTitles = {
                "业务单号", "资源名称", "资源类型", "调前路由", "调后路由", "创建时间"
        };
        XSSFRow rowTitles33 = rowsSheet3.createRow(1); // 创建标题
        rowsSheet3.setColumnWidth(0, 30 * 200);
        rowsSheet3.setColumnWidth(1, 30 * 300);
        rowsSheet3.setColumnWidth(2, 30 * 200);
        rowsSheet3.setColumnWidth(3, 30 * 300);
        rowsSheet3.setColumnWidth(4, 30 * 200);
        rowsSheet3.setColumnWidth(5, 30 * 300);

        for (int i = 0; i < collTitles.length; i++) {
            Cell rows320 = rowTitles33.createCell(i);
            rows320.setCellValue(collTitles[i]);
            rows320.setCellStyle(headerStyle);
        }
        for (int m = 0; m < resourceOrderInfo.size(); m++) {
            Map<String, Object> resourceInfo = resourceOrderInfo.get(m);
            //多级表头 第二行开始填充数据
            XSSFRow rowTitles32 = rowsSheet3.createRow(m + 2);
            String createDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(resourceInfo.get("CREATE_DATE"));
            rowTitles32.createCell(0).setCellValue(MapUtils.getString(resourceInfo, "TRADE_ID"));
            rowTitles32.createCell(1).setCellValue((String) resourceInfo.get("RESNAME"));
            rowTitles32.createCell(2).setCellValue((String) resourceInfo.get("RESTYPE"));
            rowTitles32.createCell(3).setCellValue((String) resourceInfo.get("BEFORE_ROUTE"));
            rowTitles32.createCell(4).setCellValue((String) resourceInfo.get("AFTER_ROUTE"));
            //           rowTitles32.createCell(5).setCellValue((String) resourceInfo.get("ALL_ROUTE"));
            rowTitles32.createCell(5).setCellValue(createDate);
        }
    };

    /**
     * 任务信息sheet页
     * @param exporter
     * @param headerStyle
     * @param name
     * @param taskInfo
     */
    private void initTaskSheet(ExcelExporter exporter, XSSFCellStyle headerStyle, String name, List<Map<String, Object>> taskInfo){
        XSSFSheet rowsSheet3 = exporter.initSheet(name);
        XSSFRow rowTitles33 = rowsSheet3.createRow(0); // 创建标题
        for (int i = 0; i < TASKINFOCEL.length; i++) {
            rowsSheet3.setColumnWidth(i, 30 * 200);
            Cell rows320 = rowTitles33.createCell(i);
            rows320.setCellValue(TASKINFOCEL[i]);
            rows320.setCellStyle(headerStyle);
        }
        for (int m = 0; m < taskInfo.size(); m++) {
            Map<String, Object> resourceInfo = taskInfo.get(m);
            //多级表头 第二行开始填充数据
            XSSFRow rowTitles32 = rowsSheet3.createRow(m + 1);
            rowTitles32.createCell(0).setCellValue(MapUtils.getString(resourceInfo, "SERIAL_NUMBER"));
            rowTitles32.createCell(1).setCellValue(MapUtils.getString(resourceInfo, "TASKNAME"));
            rowTitles32.createCell(2).setCellValue(MapUtils.getString(resourceInfo, "ORGNAME"));
            rowTitles32.createCell(3).setCellValue(MapUtils.getString(resourceInfo, "USERJOBNAME"));
            rowTitles32.createCell(4).setCellValue(MapUtils.getString(resourceInfo, "USERNAME"));
            rowTitles32.createCell(5).setCellValue(MapUtils.getString(resourceInfo, "TACHENAME"));
            rowTitles32.createCell(6).setCellValue(MapUtils.getString(resourceInfo, "ORDERSTATE"));
            rowTitles32.createCell(7).setCellValue(MapUtils.getString(resourceInfo, "TRACKCONTENT"));
            rowTitles32.createCell(8).setCellValue(MapUtils.getString(resourceInfo, "DEAL_DATE"));
            rowTitles32.createCell(9).setCellValue(MapUtils.getString(resourceInfo, "CREATE_DATE"));
            rowTitles32.createCell(10).setCellValue(MapUtils.getString(resourceInfo, "STATE_DATE"));
            rowTitles32.createCell(11).setCellValue(MapUtils.getLongValue(resourceInfo,  "MINUTE"));
            rowTitles32.createCell(12).setCellValue(MapUtils.getString(resourceInfo, "REQ_FIN_DATE"));
            rowTitles32.createCell(13).setCellValue(MapUtils.getString(resourceInfo, "ALARM_DATE"));
            rowTitles32.createCell(14).setCellValue(MapUtils.getString(resourceInfo, "EXCEEDTYPE"));
/*            { "TASKNAME", "任务名称",                    "ORGNAME","所属部门",                    "USERJOBNAME", "执行岗位",                    "USERNAME", "处理人",
                "TACHENAME'", "流程环节",                  "ORDERSTATE", "工单状态",                "TRACKCONTENT", "处理结果",
                "DEAL_DATE", "签收时间",                   "CREATE_DATE", "开始时间",               "STATE_DATE", "完成时间",
                "MINUTE", "耗时(分)'",                    "REQ_FIN_DATE'", "超时时间",              "ALARM_DATE'", "预警时间",                    "EXCEEDTYPE", "超时类型"};*/
            //           rowTitles32.createCell(5).setCellValue((String) resourceInfo.get("ALL_ROUTE"));

        }
    }

}
