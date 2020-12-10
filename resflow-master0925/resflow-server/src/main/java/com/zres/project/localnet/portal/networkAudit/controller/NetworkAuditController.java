package com.zres.project.localnet.portal.networkAudit.controller;

import com.zres.project.localnet.portal.networkAudit.NetworkAuditIntf;
import com.zres.project.localnet.portal.networkAudit.domain.NetworkAuditPo;
import com.zres.project.localnet.portal.util.ResponseHandler;
import com.ztesoft.res.frame.core.exception.ServiceBuizException;
import com.ztesoft.zsmart.pot.annotation.PublicServ;
import net.sf.json.JSONObject;
import org.apache.poi.ss.usermodel.Workbook;
import org.easy.excel.ExcelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/localScheduleLT/networkAuditController")
public class NetworkAuditController {

    Logger logger = LoggerFactory.getLogger(NetworkAuditController.class);

    @Autowired
    private NetworkAuditIntf networkAuditService;


    /**
     * 业务稽核导出
     * @param downLoadData
     * @param request
     * @param response
     */
    @RequestMapping(value = "/exportData.spr")
    @PublicServ
    public void exportNetWordAuditData(@RequestBody String downLoadData, HttpServletRequest request, HttpServletResponse response){

        try{
            ResponseHandler responseHandler = new ResponseHandler(request,response);
            String decode = URLDecoder.decode(downLoadData, "UTF-8").substring(13);
            int csrf = decode.lastIndexOf("&_csrf");
            String csrfStr = decode.substring(0,csrf);
            Map<String, Object> params = JSONObject.fromObject(csrfStr);
            String queryTypeName = "业务网络稽核";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String dateStr = sdf.format(new Date());
            //文件名
            String fileName = dateStr+"-"+queryTypeName;
            responseHandler.setResponseFile2007(fileName);
            ExcelContext contextExcel = new ExcelContext("excelConfig/excel-config.xml");
            Workbook netWorkDataWork = null;
            List<NetworkAuditPo> networkAuditPosList = networkAuditService.queryNetworkAuditExportData(params);
            netWorkDataWork = contextExcel.createExcel("netWorkData", networkAuditPosList, fileName);
            responseHandler.export37(netWorkDataWork);
            logger.info("导出成功");
        }catch (Exception e){
            throw new ServiceBuizException(e);
        }



    }



}
