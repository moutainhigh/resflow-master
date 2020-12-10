package com.zres.project.localnet.portal.localStanbdyInfo.controller;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.zres.project.localnet.portal.localStandbyInfo.service.OrderStandbyServiceIntf;
import com.zres.project.localnet.portal.util.ExcelExporter;
import com.zres.project.localnet.portal.util.ResponseHandler;
import com.ztesoft.res.frame.core.exception.ServiceBuizException;
import com.ztesoft.zsmart.pot.annotation.PublicServ;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author :wang.g2
 * @description :
 * @date : 2019/1/8
 */
@Controller
@RequestMapping("/localScheduleLT/orderStandbyController")
public class OrderStandbyController {
    Logger logger = LoggerFactory.getLogger(OrderStandbyController.class);
    @Autowired
    private OrderStandbyServiceIntf orderStandbyServiceIntf;

@RequestMapping("/uploadFiles.spr")
@PublicServ
 public void uploadFiles(HttpServletRequest request, HttpServletResponse response) {
    try {
        String resultMsg = null;
        Map<String, Object> uploadMap = new HashMap();
        String woId = request.getParameter("woId");
        String orderId = request.getParameter("orderId");
        String srvOrdId = request.getParameter("srvOrdId");
        String selarrrowStr = request.getParameter("selarrrow"); //选中上传的待办的信息
        List<Map> mapsList = JSON.parseArray(selarrrowStr, Map.class);
        String staffId = request.getParameter("name");
        String remark = request.getParameter("remark");
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultiValueMap<String, MultipartFile> multiFileMap = multipartRequest.getMultiFileMap();
        String ctxPath = request.getSession().getServletContext().getRealPath("/") + File.separator + "upload"; //临时存放路径
        uploadMap.put("staffId", staffId);
        uploadMap.put("ctxPath", ctxPath);
        uploadMap.put("origin", "YJ");
        uploadMap.put("remark", request.getParameter("remark") == null ? "" : request.getParameter("remark"));
        Map<String, Object> returnMap = orderStandbyServiceIntf.updateCollapsible(mapsList, uploadMap, multiFileMap);
        JSONObject jsonObject = JSONObject.fromObject(returnMap);
        flushResponse(response, jsonObject.toString());
    }
  catch (Exception e) {
        logger.info(e.getMessage());
    }
}


@RequestMapping("/abnomrmalInfo.spr")
@PublicServ
/**
 * 异常单上传附件跟说明 ADD by wang.gang2 zmp  1775764
 */
public void abnomrmalInfo(HttpServletRequest request, HttpServletResponse response) {
    try {
        String resultMsg = null;
        Map<String, Object> uploadMap = new HashMap();
        String woId = request.getParameter("woId");
        String orderIds = request.getParameter("orderIds");
        String tacheId = request.getParameter("tacheId");
        String srvOrdId = request.getParameter("srvOrdId");
        String cstOrdId = request.getParameter("cstOrdId");
        String staffId = request.getParameter("staffId");
        String userName = request.getParameter("userName");
        String chgType = request.getParameter("chgType");
        String regionId = request.getParameter("regionId");
//        String remark = request.getParameter("remark");
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultiValueMap<String, MultipartFile> multiFileMap = multipartRequest.getMultiFileMap();
        String ctxPath = request.getSession().getServletContext().getRealPath("/") + File.separator + "upload"; //临时存放路径
        uploadMap.put("woId", woId);
        uploadMap.put("orderId", orderIds);
        uploadMap.put("tacheId", tacheId);
        uploadMap.put("cstOrdId", cstOrdId);
        uploadMap.put("chgType", chgType);
        uploadMap.put("regionId", regionId);

        uploadMap.put("userName", userName);
        uploadMap.put("staffId", staffId);
        uploadMap.put("ctxPath", ctxPath);
        uploadMap.put("origin", "YC");//异常单上传
        uploadMap.put("remark", request.getParameter("remark") == null ? "" : request.getParameter("remark"));

        Map<String, Object> returnMap = orderStandbyServiceIntf.insertAbnomrmalInfo(uploadMap, multiFileMap);
        JSONObject jsonObject = JSONObject.fromObject(returnMap);
        flushResponse(response, jsonObject.toString());
    }
    catch (Exception e) {
        logger.info(e.getMessage());
    }
}


@PublicServ
@RequestMapping(value = "/exportStandbyOrderData.spr")
public void exportStandbyOrderData(@RequestBody String downLoadData, HttpServletRequest request, HttpServletResponse response) {
    String queryTypeName = "";
    //表头
    List<String> heads = new ArrayList<String>();
    //表列
    List<String> colIds = new ArrayList<String>();
    //excel数据 支持泛型为Map<String,Object>
    List<Map<String, Object>> dataExcel = new ArrayList<Map<String, Object>>();
    List<Map<String, Object>> selarrrow = new ArrayList<>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    try {
        ResponseHandler responseHandler = new ResponseHandler(request, response);
        String decode = URLDecoder.decode(downLoadData, "UTF-8").substring(13);
        int csrf = decode.lastIndexOf("&_csrf");
        String csrfStr = decode.substring(0,csrf);
        Map<String, Object> params = JSONObject.fromObject(csrfStr);
        String queryType = (String) params.get("queryTypeLocal"); //查询类型
        //获取选中数据
        selarrrow = (List<Map<String, Object>>)  params.get("selarrrow");
        String dateStr = sdf.format(new Date());
        if ("deptStandny".equals(queryType)) { //草稿单
            queryTypeName = "部门待签收";
            heads.addAll(getStandbyHeadList());
            colIds.addAll(getStandbyColList());
        }
        else if ("jobStandby".equals(queryType)) { //全部申请单
            queryTypeName = "岗位待签收";
            heads.addAll(getStandbyHeadList());
            colIds.addAll(getStandbyColList());
        }
        else if ("staffStandby".equals(queryType)) { //已完成申请单
            queryTypeName = "个人待签收";
            heads.addAll(getStandbyHeadList());
            colIds.addAll(getStandbyColList());
        }
        else if ("dealOrder".equals(queryType)) {
            //已提交申请单
            queryTypeName = "处理中";
            heads.addAll(getOtherHeadList());
            colIds.addAll(getOtherColList());

        }
        else if ("dispConfirm".equals(queryType)) {
            //已提交申请单
            queryTypeName = "已完成";
            heads.addAll(getOtherHeadList());
            colIds.addAll(getOtherColList());
        }
        if (selarrrow.size() <= 0) {
            selarrrow = orderStandbyServiceIntf.exportStandbyOrderData(params);
        }

        if (!CollectionUtils.isEmpty(selarrrow)) {
            dataExcel.addAll(selarrrow);
        }
        //文件名
        String fileName = dateStr + "-" + queryTypeName;
        responseHandler.setResponseFile(fileName);

        ExcelExporter exporter = new ExcelExporter(heads, colIds, dataExcel, responseHandler);
        exporter.fillSheet(fileName, null, null);
        exporter.export();
        logger.info("导出成功");

    }
    catch (Exception e) {
        throw new ServiceBuizException(e);
    }

}

public List<String> getStandbyHeadList() {
    // 待办工单
    List<String> draftHeadList = new ArrayList<String>();
    draftHeadList.add("单据类型");
    draftHeadList.add("客户名称");
    draftHeadList.add("客户订单号");
    draftHeadList.add("业务订单号");
    draftHeadList.add("申请单编码");
    draftHeadList.add("申请单标题");
    draftHeadList.add("调度单编号");
    draftHeadList.add("调度单标题");
    draftHeadList.add("产品类型");
    draftHeadList.add("动作类型");
    draftHeadList.add("电路编码");
    draftHeadList.add("环节名称");
    draftHeadList.add("专业");
    draftHeadList.add("区域");
    draftHeadList.add("环节要求完成时间");
    return draftHeadList;
}
public List<String> getStandbyColList() {

    List<String> draftColList = new ArrayList<String>();
    draftColList.add("WO_COMPLETE_STATE".toUpperCase());
    draftColList.add("CUST_NAME_CHINESE".toUpperCase());
    draftColList.add("SUBSCRIBE_ID".toUpperCase());
    draftColList.add("TRADE_ID".toUpperCase());
    draftColList.add("APPLY_ORD_ID".toUpperCase());
    draftColList.add("APPLY_ORD_NAME".toUpperCase());
    draftColList.add("DISPATCH_ORDER_NO".toUpperCase());
    draftColList.add("DISPATCH_TITLE".toUpperCase());
    draftColList.add("SERVICETYPE".toUpperCase());
    draftColList.add("ACTIVETYPENAME".toUpperCase());
    draftColList.add("ATTR_VALUE".toUpperCase());
    draftColList.add("TACHE_NAME".toUpperCase());
    draftColList.add("PUB_DATE_NAME".toUpperCase());
    draftColList.add("REGION_NAME".toUpperCase());
    draftColList.add("REQ_FIN_DATE".toUpperCase());
    return draftColList;
}
public List<String> getOtherHeadList() {
    //处理中header
    List<String> applyHeadList = new ArrayList<String>();
    applyHeadList.add("客户订单号");
    applyHeadList.add("业务订单号");
    applyHeadList.add("申请单编码");
    applyHeadList.add("申请单标题");
    applyHeadList.add("调度单编号");
    applyHeadList.add("调度单标题");
    applyHeadList.add("客户名称");
    applyHeadList.add("产品类型");
    applyHeadList.add("动作类型");
    applyHeadList.add("电路编码");
    applyHeadList.add("环节名称");
    applyHeadList.add("专业");
    applyHeadList.add("区域");
    applyHeadList.add("环节要求完成时间");
    return applyHeadList;
}
public List<String> getOtherColList() {

    List<String> applyColList = new ArrayList<String>();
    applyColList.add("SUBSCRIBE_ID".toUpperCase());
    applyColList.add("TRADE_ID".toUpperCase());
    applyColList.add("APPLY_ORD_ID".toUpperCase());
    applyColList.add("APPLY_ORD_NAME".toUpperCase());
    applyColList.add("DISPATCH_ORDER_NO".toUpperCase());
    applyColList.add("DISPATCH_TITLE".toUpperCase());
    applyColList.add("CUST_NAME_CHINESE".toUpperCase());
    applyColList.add("SERVICETYPE".toUpperCase());
    applyColList.add("ACTIVETYPENAME".toUpperCase());
    applyColList.add("ATTR_VALUE".toUpperCase());
    applyColList.add("TACHE_NAME".toUpperCase());
    applyColList.add("PUB_DATE_NAME".toUpperCase());
    applyColList.add("REGION_NAME".toUpperCase());
    applyColList.add("REQ_FIN_DATE".toUpperCase());
    return applyColList;
}

protected void flushResponse(HttpServletResponse response, String responseContent) {
        PrintWriter writer = null;

        try {
            response.setCharacterEncoding("GBK");
            response.setHeader("Cache-Control", "no-cache");
            response.setContentType("text/html;charset=UTF-8");
            writer = response.getWriter();
            if (StringUtils.isEmpty(responseContent)) {
                writer.write("");
            }
            else {
                writer.write(responseContent);
            }
        }
        catch (IOException var8) {
            throw new RuntimeException(var8);
        }
         finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }

        }

    }
}
