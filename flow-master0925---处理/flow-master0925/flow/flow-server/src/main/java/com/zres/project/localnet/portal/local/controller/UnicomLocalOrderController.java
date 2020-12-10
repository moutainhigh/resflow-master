package com.zres.project.localnet.portal.local.controller;

import com.zres.project.localnet.portal.local.dao.UnicomLocalOrderDao;
import com.zres.project.localnet.portal.local.domain.PageInfo;
import com.zres.project.localnet.portal.local.domain.UnicomLocalExportData;
import com.zres.project.localnet.portal.util.ExcelExporter;
import com.zres.project.localnet.portal.util.ResponseHandler;
import com.zres.project.localnet.portal.webservice.res.BusinessQueryServiceIntf;
import com.ztesoft.res.frame.core.exception.ServiceBuizException;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import com.ztesoft.zsmart.pot.annotation.PublicServ;
import net.sf.json.JSONObject;
import org.apache.commons.collections4.MapUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.easy.excel.ExcelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 本地业务申请Controller
 */
@Controller
@RequestMapping("/localScheduleLT/unicomLocalOrder")
public class UnicomLocalOrderController {

    Logger logger = LoggerFactory.getLogger(UnicomLocalOrderController.class);

    @Autowired
    private UnicomLocalOrderDao unicomLocalOrderDao;
    @Autowired
    private BusinessQueryServiceIntf businessQueryServiceIntf;

    @Value("${ftf.sql.limit-result-set:6000}")
    private int ftfsqllimitresultset;

    /**
     * 草稿单、申请单导出
     * @param downLoadData
     * @param request
     * @param response
     */
    @RequestMapping(value = "/exportData.spr")
    @PublicServ
    public void exportLocalOrderData(@RequestBody String downLoadData,HttpServletRequest request, HttpServletResponse response){
        try{
            ResponseHandler responseHandler = new ResponseHandler(request,response);
            String decode = URLDecoder.decode(downLoadData, "UTF-8").substring(13);
            int csrf = decode.lastIndexOf("&_csrf");
            String csrfStr = decode.substring(0,csrf);
            Map<String, Object> params = JSONObject.fromObject(csrfStr);
            HashMap<String, Object> daoMap = new HashMap<String, Object>(); //查询参数
            String queryType = (String)params.get("queryType"); //查询类型
            String queryTypeName = "";
            daoMap.put("queryType", queryType);
            daoMap.put("isLocalUnicom",params.get("isLocalUnicom")); //localBuild 本地订单
            daoMap.put("orderState",params.get("orderState")); //业务订单状态10C 草稿单 10F 已完成 10N 已提交
            daoMap.put("applyOrdId",params.get("applyOrdId")); //申请单编号
            daoMap.put("applyOrdName",params.get("applyOrdName")); //申请单标题
            daoMap.put("circuitNo",params.get("circuitNo")); //电路代号
            daoMap.put("custName",params.get("custName")); //客户名称
            daoMap.put("productType",params.get("productType")); //产品类型
            daoMap.put("actType",params.get("actType")); //动作类型
            daoMap.put("orderType",params.get("orderType")); //动作类型
            daoMap.put("databaseType","oracle"); //数据库类型

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String dateStr = sdf.format(new Date());
            //excel数据 支持泛型为Map<String,Object>
            PageInfo pageInfo = new PageInfo();
            pageInfo.setIndexSizeData(1,ftfsqllimitresultset-1);
            //获取用户id
            String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
            daoMap.put("operStaffId",operStaffId);

            if("draftOrder".equals(queryType)) {//草稿单
                queryTypeName = "草稿单";
                daoMap.put("wholeOrderState","");
//                daoMap.put("orderTitle","");//申请单标题
                int draftCount = unicomLocalOrderDao.queryLocalApplyOrderCount(daoMap);
                pageInfo.setDataCount(draftCount);
            }else if("allOrder".equals(queryType)) {//全部申请单
                queryTypeName = "全部申请单";
                daoMap.put("wholeOrderState","10C");
                int allOrderCount = unicomLocalOrderDao.queryLocalApplyOrderCount(daoMap);
                pageInfo.setDataCount(allOrderCount);
            }else if("completeOrder".equals(queryType)) {//已完成申请单
                queryTypeName = "已完成申请单";
                daoMap.put("wholeOrderState","");
                int completeOrderCount = unicomLocalOrderDao.queryLocalApplyOrderCount(daoMap);
                pageInfo.setDataCount(completeOrderCount);
            }else if("submitedOrder".equals(queryType)) {//已提交申请单
                queryTypeName = "已提交申请单";
                daoMap.put("wholeOrderState","");
                int submitOrderCount = unicomLocalOrderDao.queryLocalApplyOrderCount(daoMap);
                pageInfo.setDataCount(submitOrderCount);
            }
            //文件名
            String fileName = dateStr+"-"+queryTypeName;
            responseHandler.setResponseFile2007(fileName);
            ExcelContext contextExcel = new ExcelContext("excelConfig/excel-config.xml");
            Workbook draftDataWork = null;
            List<UnicomLocalExportData> unicomLocalExportDataTemp = new ArrayList<UnicomLocalExportData>();
            if("draftOrder".equals(queryType)) {//草稿单
                if(pageInfo.getRowCount()!=0){
                    for(int i=1;i<=pageInfo.getPageCount();i++){
                        pageInfo.setIndexSizeData(i,ftfsqllimitresultset-1);
                        daoMap.put("startRow",pageInfo.getRowStart());//分页开始行
                        daoMap.put("endRow",pageInfo.getRowEnd());//分页结束行
                        List<UnicomLocalExportData> unicomLocalExportData = unicomLocalOrderDao.queryLocalExportApplyOrderDataPo(daoMap);
                        if(!CollectionUtils.isEmpty(unicomLocalExportData)){
                            unicomLocalExportDataTemp.addAll(unicomLocalExportData);
                        }
                    }
                    draftDataWork = contextExcel.createExcel("draftData", unicomLocalExportDataTemp, fileName);

                }
            }else if("allOrder".equals(queryType)
                    ||"completeOrder".equals(queryType)
                    ||"submitedOrder".equals(queryType)) {//全部申请单、已完成申请单、已提交申请单
                if(pageInfo.getRowCount()!=0){
                for(int i=1;i<=pageInfo.getPageCount();i++){
                    pageInfo.setIndexSizeData(i,ftfsqllimitresultset-1);
                    daoMap.put("startRow",pageInfo.getRowStart());//分页开始行
                    daoMap.put("endRow",pageInfo.getRowEnd());//分页结束行
                    List<UnicomLocalExportData> unicomLocalExportData = unicomLocalOrderDao.queryLocalExportApplyOrderDataPo(daoMap);
                    if(!CollectionUtils.isEmpty(unicomLocalExportData)){
                        unicomLocalExportDataTemp.addAll(unicomLocalExportData);
                    }
                }
                draftDataWork = contextExcel.createExcel("applyColData", unicomLocalExportDataTemp, fileName);
            }
        }
            responseHandler.export37(draftDataWork);
            logger.info("导出成功");

        }catch (Exception e) {
            throw new ServiceBuizException(e);
        }

    }

    @RequestMapping(value = "/exportStockCircuitInfo.spr")
    public void exportStockCircuitInfo(@RequestParam("downLoadData") String downLoadData, HttpServletRequest request, HttpServletResponse response){
        try{
            String decode = URLDecoder.decode(downLoadData, "UTF-8");
            Map<String, Object> param = JSONObject.fromObject(decode);
            //调用资源接口查询满足条件的电路信息
            Map<String, Object> map = businessQueryServiceIntf.businessQuery(param);
            if (MapUtils.getBoolean(map, "isExist")){
                List<Map<String, Object>> exportData = (List<Map<String, Object>>) MapUtils.getObject(map, "data");
                // 表头
                List<String> heads = Arrays.asList("电路ID", "产品实例号", "电路编号", "业务号码", "资源类型", "业务状态", "实例状态");
                // 表列
                List<String> colIds = Arrays.asList("circuitId", "prodInstId", "circuitCode", "accNbr", "resType", "oprState", "sbOprStateId");
                ResponseHandler responseHandler = new ResponseHandler(request, response);
                responseHandler.setResponseFile("存量电路信息导出");
                ExcelExporter exporter = new ExcelExporter(heads, colIds, exportData, responseHandler);
                exporter.fillSheet("存量电路信息导出", null, null);
                exporter.export();
                logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>存量电路信息导出成功");
            }else {
                logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>所填条件查询不到存量电路信息");
            }
        }catch(Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>存量电路信息导出异常：" + e.getMessage());
        }
    }





}
