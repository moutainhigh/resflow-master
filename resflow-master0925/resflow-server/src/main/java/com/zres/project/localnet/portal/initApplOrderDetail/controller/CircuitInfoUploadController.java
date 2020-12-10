package com.zres.project.localnet.portal.initApplOrderDetail.controller;


import com.zres.project.localnet.portal.initApplOrderDetail.domain.CircuitInfoPo;
import com.zres.project.localnet.portal.initApplOrderDetail.domain.LocalCircuitInfoPo;
import com.zres.project.localnet.portal.util.ResponseHandler;
import com.ztesoft.res.frame.core.exception.ServiceBuizException;
import com.ztesoft.res.frame.core.util.MapUtil;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import com.ztesoft.zsmart.pot.annotation.PublicServ;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.easy.excel.ExcelContext;
import org.easy.excel.result.ExcelImportResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * 上传电路信息excel、下载电路信息模板Controller
 * author sunlb
 */
@Controller
@RequestMapping("/localScheduleLT/circuitInfoUploadController")
public class CircuitInfoUploadController {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 上传电路信息excel
     * @param request
     * @param response
     */
    @RequestMapping("/upCirInfoExcel.spr")
    @PublicServ
    public void upCirInfoExcel(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>();
        InputStream inputStream = null;
        JSONObject jsonObject = null;
        File newUploadfile = null;
        ResponseHandler responseHandler = new ResponseHandler(request,response);
        try{
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            MultiValueMap<String, MultipartFile> multiFileMap = multipartRequest.getMultiFileMap();
            List<MultipartFile> uploadFiles = multiFileMap.get("uploadFiles");
            if(CollectionUtils.isEmpty(uploadFiles)){
                throw new Exception("请上传电路信息");
            }
            String serviceId = multipartRequest.getParameter("serviceId");
            MultipartFile multipartFile = uploadFiles.get(0);
            String fileNameString = multipartFile.getOriginalFilename();
            String fileType = fileNameString.substring(fileNameString.lastIndexOf('.') + 1).toLowerCase();//excel扩展名
            if(!"xlsx".equals(fileType)){
                throw new Exception("Excel文件格式不正确，请重新上传!");
            }
            String ctxPath = request.getSession().getServletContext().getRealPath("/") + "uploadCircuit"; //临时存放路径
            File fileParent = new File(ctxPath);
            if(!fileParent.exists()){
                fileParent.mkdir();
            }
            long timeDate = new Date().getTime();
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(ctxPath).append(File.separator).append(timeDate).append(".").append(fileType);
            newUploadfile = new File(stringBuffer.toString());
            if(!newUploadfile.exists()){
                newUploadfile.createNewFile();
            }
            FileCopyUtils.copy(multipartFile.getBytes(), newUploadfile);
            String absolutePath = newUploadfile.getAbsolutePath();
            ExcelContext context = new ExcelContext("excelConfig/excel-config.xml");
            inputStream = new FileInputStream(absolutePath);
            ExcelImportResult cirCuitInfo = null ;
            if (StringUtils.isNotEmpty(serviceId) && "20181221006".equals(serviceId)){
                cirCuitInfo = context.readExcel("localCirCuitInfo", 1, inputStream, 0, false, 0);
            }else {
                cirCuitInfo = context.readExcel("cirCuitInfo", 1, inputStream, 0, false, 0);
            }            List<CircuitInfoPo> listBean = cirCuitInfo.getListBean();
            map.put("message", "success");
            map.put("data", listBean);
            map.put("updateCount", 1);
        }catch (Exception e){
            logger.info("导入电路信息失败:"+e.getMessage());
            e.printStackTrace();
            map.put("message", "fail");
            map.put("data", null);
            map.put("error",e.getMessage());
            map.put("updateCount", 0);
        }finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                inputStream = null;
            }
            if(newUploadfile != null){
                newUploadfile.delete();
                newUploadfile = null;
            }
        }
        jsonObject = JSONObject.fromObject(map);
        responseHandler.flushResponse(response,jsonObject.toString());

    }


    /**
     * 下载电路信息模板
     * @param request
     * @param response
     */
    @RequestMapping("/expCirTempExcel.spr")
    @IgnoreSession
    public void expCirTempExcel(HttpServletRequest request, HttpServletResponse response) {

        ResponseHandler responseHandler = new ResponseHandler(request,response);
        try{

            String productType = "";
            Map<String, String[]> parameterMap = request.getParameterMap();
            if(MapUtil.isNotEmpty(parameterMap)) {
                String[] downLoadData = parameterMap.get("downLoadData");
                String downLoadDatum = downLoadData[0];
                com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(downLoadDatum);
                productType = MapUtil.getString(jsonObject, "productType");
                ExcelContext contextExcel = new ExcelContext("excelConfig/excel-config.xml");
                Workbook draftDataWork = null;
                if ("20181221006".equals(productType)) {
                    Date date = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
                    String format = sdf.format(date);
                    responseHandler.setResponseFile2007("局内中继电路信息导入模板_" + format);
                    draftDataWork = contextExcel.createExcel("localCirCuitInfo", new ArrayList<LocalCircuitInfoPo>(), "中继电路信息");
                }
                else {
                    responseHandler.setResponseFile2007("电路信息导入模板");
                    draftDataWork = contextExcel.createExcel("cirCuitInfo", new ArrayList<CircuitInfoPo>(), "电路信息");
                }
                responseHandler.export37(draftDataWork);
                logger.info("导出电路信息模板成功");
            }
        }catch (Exception e) {
            throw new ServiceBuizException(e);
        }

    }


}
