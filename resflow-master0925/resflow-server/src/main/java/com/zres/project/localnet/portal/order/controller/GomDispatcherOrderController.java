package com.zres.project.localnet.portal.order.controller;


import com.zres.project.localnet.portal.local.domain.PageInfo;
import com.zres.project.localnet.portal.order.domain.GomDispatcherOrderPo;
import com.zres.project.localnet.portal.order.service.GomOrderQueryServiceIntf;
import com.zres.project.localnet.portal.util.ExcelExporter;
import com.zres.project.localnet.portal.util.ResponseHandler;
import com.ztesoft.res.frame.core.exception.ServiceBuizException;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import com.ztesoft.zsmart.pot.annotation.PublicServ;
import net.sf.json.JSONObject;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/localScheduleLT/gomDispatcherOrderController")
public class GomDispatcherOrderController {
    Logger logger = LoggerFactory.getLogger(GomDispatcherOrderController.class);

    @Autowired
    private GomOrderQueryServiceIntf gomOrderQueryService;

    @Value("${ftf.sql.limit-result-set}")
    private int ftfsqllimitresultset;

    /**
     * 调度单导出数据
     * @param downLoadData
     * @param request
     * @param response
     */
    @RequestMapping(value = "/exportData.spr")
    @PublicServ
    public void gomDownOrderData(@RequestBody String downLoadData, HttpServletRequest request, HttpServletResponse response){
        try{
            ResponseHandler responseHandler = new ResponseHandler(request,response);
            String decode = URLDecoder.decode(downLoadData, "UTF-8").substring(13);
            int csrf = decode.lastIndexOf("&_csrf");
            String csrfStr = decode.substring(0,csrf);
            Map<String, Object> params = JSONObject.fromObject(csrfStr);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String dateStr = sdf.format(new Date());

            //查询参数
            HashMap<String, Object> daoMap = new HashMap<String, Object>();

            SimpleDateFormat simpleFormate =  new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String finishDateStart = (String)params.get("finishDateStart");
            String finishDateEnd = (String)params.get("finishDateEnd");
            java.sql.Date finishDateStartsql= StringUtils.hasText(finishDateStart)?new java.sql.Date(simpleFormate.parse(finishDateStart).getTime()):null;
            java.sql.Date finishDateEndsql=StringUtils.hasText(finishDateEnd)?new java.sql.Date(simpleFormate.parse(finishDateEnd).getTime()):null;

            daoMap.put("databaseType","oracle"); //数据库类型
            daoMap.put("orderTitle",params.get("orderTitle")); //调单标题
            daoMap.put("custName",params.get("custName")); //客户名称
            daoMap.put("cirNum",params.get("cirNum")); //调单编号
            daoMap.put("serialNumber",params.get("serialNumber"));//业务号码
            daoMap.put("tradeId",params.get("tradeId"));//业务订单号
            daoMap.put("finishDateStart",finishDateStartsql);//完成开始时间
            daoMap.put("finishDateEnd",finishDateEndsql);//完成结束时间
            daoMap.put("subscribeId",params.get("subscribeId"));//客户流水号
            daoMap.put("actType",params.get("actType"));//动作类型
            daoMap.put("productType",params.get("productType"));//产品类型
            String userId = "";
            if (ThreadLocalInfoHolder.getLoginUser()!=null){
                userId = ThreadLocalInfoHolder.getLoginUser().getUserId();
            }
            daoMap.put("userId",userId);//当前登录用户id
            //表头
            List<String> heads = new ArrayList<String>();
            //表列
            List<String> colIds = new ArrayList<String>();
            //excel数据 支持泛型为Map<String,Object>
//            List<Map<String,Object>> dataExcel = new ArrayList<Map<String,Object>>();
            //调度单数量
            int gomCount = gomOrderQueryService.countWo(daoMap);
            PageInfo pageInfo = new PageInfo();
            pageInfo.setIndexSizeData(1,ftfsqllimitresultset-1);
            pageInfo.setDataCount(gomCount);
            //文件名
            String fileName = dateStr+"_"+"调度单";
            responseHandler.setResponseFile(fileName);
            heads.addAll(getGomDispatcherHeadList());
            colIds.addAll(getGomDispatcherColHeadList());
            ExcelExporter excelExporter = new ExcelExporter(heads, colIds, responseHandler);
            XSSFSheet rowsSheet = excelExporter.initSheet(fileName);
            excelExporter.initHeaders(rowsSheet,null,null);

            if(gomCount!=0){
                for(int i=1;i<=pageInfo.getPageCount();i++){
                    pageInfo.setIndexSizeData(i,ftfsqllimitresultset-1);
                    daoMap.put("startRow",pageInfo.getRowStart());//分页开始行
                    daoMap.put("endRow",pageInfo.getRowEnd());//分页结束行
                    List<GomDispatcherOrderPo> gomOrderExportDataList = gomOrderQueryService.getGomOrderExportData(daoMap);
                    if(!CollectionUtils.isEmpty(gomOrderExportDataList)){
                        excelExporter.writeSheet(rowsSheet,null,null,gomOrderExportDataList,pageInfo.getRowStart());
                    }
                }
                excelExporter.export();
            }
            logger.info("导出成功");
        }catch (Exception e) {
            throw new ServiceBuizException(e);
        }

    }

    public List<String> getGomDispatcherHeadList() {
        List<String> gomHeadList = new ArrayList<String>();
        gomHeadList.add("调度单编号");
        gomHeadList.add("调度单标题");
        gomHeadList.add("申请单标题");
        gomHeadList.add("客户订单号");
        gomHeadList.add("客户名称");
        gomHeadList.add("产品类型");
        gomHeadList.add("动作类型");
        gomHeadList.add("电路数量");
        return gomHeadList;
    }

    public List<String> getGomDispatcherColHeadList() {
        List<String> gomColList = new ArrayList<String>();
        gomColList.add("dispatchOrderNo");
        gomColList.add("dispatchTitle");
        gomColList.add("orderTitle");
        gomColList.add("subscribeId");
        gomColList.add("custNameChinese");
        gomColList.add("productCodeContent");
        gomColList.add("activeTypeName");
        gomColList.add("cirCount");
        return gomColList;
    }




}
