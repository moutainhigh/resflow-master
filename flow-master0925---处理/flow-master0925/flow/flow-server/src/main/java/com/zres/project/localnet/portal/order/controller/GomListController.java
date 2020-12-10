package com.zres.project.localnet.portal.order.controller;

import com.zres.project.localnet.portal.order.service.OrderQueryServiceIntf;
import com.zres.project.localnet.portal.util.ExcelExporter;
import com.zres.project.localnet.portal.util.ResponseHandler;
import com.ztesoft.res.frame.core.exception.ServiceBuizException;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import com.ztesoft.zsmart.pot.annotation.PublicServ;
import net.sf.json.JSONObject;
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
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author :wang.g2
 * @description :
 * @date : 2019/1/14
 */
@Controller
@RequestMapping("/localScheduleLT/gomListController")
public class GomListController {
    Logger logger = LoggerFactory.getLogger(GomListController.class);

    @Autowired
    private OrderQueryServiceIntf orderQueryServiceIntf;
    @PublicServ
    @RequestMapping("exportGomListData.spr")
    public void exportGomListData(@RequestBody String downLoadData, HttpServletRequest request, HttpServletResponse response) {
        try {
            ResponseHandler responseHandler = new ResponseHandler(request, response);
            String decode = URLDecoder.decode(downLoadData, "UTF-8").substring(13);
            int csrf = decode.lastIndexOf("&_csrf");
            String csrfStr = decode.substring(0,csrf);
            Map<String, Object> params = JSONObject.fromObject(csrfStr);
            String userId = "";
            if (ThreadLocalInfoHolder.getLoginUser()!=null){
                userId = ThreadLocalInfoHolder.getLoginUser().getUserId();
            }
            params.put("userId",userId);//当前登录用户id

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String dateStr = sdf.format(new Date());
            //表头
            List<String> heads = new ArrayList<String>();
            //表列
            List<String> colIds = new ArrayList<String>();
            //excel数据 支持泛型为Map<String,Object>
            List<Map<String, Object>> dataExcel = new ArrayList<Map<String, Object>>();

            heads.addAll(getGomListHeadList());
            colIds.addAll(getGomListColList());

            List<Map<String, Object>> draftOrderList = (List<Map<String, Object>>) orderQueryServiceIntf.queryExportWo(params).get("data");
            if (!CollectionUtils.isEmpty(draftOrderList)) {
                dataExcel.addAll(draftOrderList);
            }
            //文件名
            String fileName = dateStr + "_" + "工单查询";
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
    public List<String> getGomListHeadList() {
        List<String> draftHeadList = new ArrayList<String>();
        draftHeadList.add("申请单编码");
        draftHeadList.add("申请单标题");
        draftHeadList.add("客户名称");
        draftHeadList.add("业务订单号");
        draftHeadList.add("客户订单号");
        draftHeadList.add("业务号码");
        draftHeadList.add("电路编码");draftHeadList.add("单据来源");
        draftHeadList.add("环节名称");
        draftHeadList.add("工单状态");
        draftHeadList.add("当前处理人");
        draftHeadList.add("单据类型");

        draftHeadList.add("调度单编号");
        draftHeadList.add("调度单标题");
        draftHeadList.add("环节要求完成时间");
        draftHeadList.add("资源是否补录");
        return draftHeadList;
    }
    public List<String> getGomListColList() {
        List<String> draftColList = new ArrayList<String>();
        draftColList.add("APPLY_ORD_ID".toUpperCase());
        draftColList.add("ORDER_TITLE".toUpperCase());
        draftColList.add("CUST_NAME_CHINESE".toUpperCase());
        draftColList.add("TRADE_ID".toUpperCase());
        draftColList.add("SUBSCRIBE_ID".toUpperCase());
        draftColList.add("SERIAL_NUMBER".toUpperCase());
        draftColList.add("ATTR_VALUE".toUpperCase());
        draftColList.add("TACHE_NAME".toUpperCase());
        draftColList.add("WO_STATE".toUpperCase());
        draftColList.add("LOGIN_NAME".toUpperCase());
        draftColList.add("ORDER_TYPE_NAME".toUpperCase());
        draftColList.add("RESOURCESNAME".toUpperCase());
        draftColList.add("DISPATCH_ORDER_NO".toUpperCase());
        draftColList.add("DISPATCH_TITLE".toUpperCase());
        draftColList.add("REQ_FIN_DATE".toUpperCase());
        draftColList.add("RECORD_TYPE_NAME".toUpperCase());
        return draftColList;
    }

    @PublicServ
    @RequestMapping("exportWoOrderInfo.spr")
    public void exportWoOrderInfo(@RequestBody String downLoadData, HttpServletRequest request, HttpServletResponse response) {
        try {
            ResponseHandler responseHandler = new ResponseHandler(request, response);
            String decode = URLDecoder.decode(downLoadData, "UTF-8").substring(13);
            int csrf = decode.lastIndexOf("&_csrf");
            String csrfStr = decode.substring(0,csrf);
            Map<String, Object> params = JSONObject.fromObject(csrfStr);
            String userId = "";
            if (ThreadLocalInfoHolder.getLoginUser()!=null){
                userId = ThreadLocalInfoHolder.getLoginUser().getUserId();
            }
            params.put("userId",userId);//当前登录用户id

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String dateStr = sdf.format(new Date());
            //表头
            List<String> heads = new ArrayList<String>();
            //表列
            List<String> colIds = new ArrayList<String>();
            //excel数据 支持泛型为Map<String,Object>
            List<Map<String, Object>> dataExcel = new ArrayList<Map<String, Object>>();

            heads.addAll(getWoOrderHeadList());
            colIds.addAll(getWoOrderColList());

            List<Map<String, Object>> draftOrderList = orderQueryServiceIntf.exportWoOrderInfo(params);
            if (!CollectionUtils.isEmpty(draftOrderList)) {
                dataExcel.addAll(draftOrderList);
            }
            //文件名
            String fileName = dateStr + "_" + "历史工单查询";
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

    public List<String> getWoOrderHeadList() {
        List<String> draftHeadList = new ArrayList<String>();
        draftHeadList.add("是否退单");
        draftHeadList.add("申请单编号");
        draftHeadList.add("申请单标题");
        draftHeadList.add("客户名称");
        draftHeadList.add("客户订单号");
        draftHeadList.add("环节名称");
        draftHeadList.add("电路数量");
        draftHeadList.add("产品类型");
        draftHeadList.add("动作类型");
        draftHeadList.add("单据类型");
        draftHeadList.add("单据来源");
        draftHeadList.add("调度单编号");
        draftHeadList.add("调度单标题");
        draftHeadList.add("环节预警时间");
        draftHeadList.add("环节要求完成时间");
        draftHeadList.add("工单处理时间");
        return draftHeadList;
    }

    public List<String> getWoOrderColList() {
        List<String> draftColList = new ArrayList<String>();
        draftColList.add("WOORDERBACKFLAGS".toUpperCase());
        draftColList.add("APPLY_ORD_ID".toUpperCase());
        draftColList.add("APPLY_ORD_NAME".toUpperCase());
        draftColList.add("CUST_NAME_CHINESE".toUpperCase());
        draftColList.add("SUBSCRIBE_ID".toUpperCase());
        draftColList.add("TACHE_NAME".toUpperCase());
        draftColList.add("COUNTS".toUpperCase());
        draftColList.add("SERVICE_ID".toUpperCase());
        draftColList.add("ACTIVE_TYPE".toUpperCase());
        draftColList.add("ORDER_TYPE".toUpperCase());
        draftColList.add("RESOURCES".toUpperCase());
        draftColList.add("DISPATCH_ORDER_NO".toUpperCase());
        draftColList.add("DISPATCH_TITLE".toUpperCase());
        draftColList.add("ALARM_DATE".toUpperCase());
        draftColList.add("REQ_FIN_DATE".toUpperCase());
        draftColList.add("DEAL_DATE".toUpperCase());
        return draftColList;
    }

}
