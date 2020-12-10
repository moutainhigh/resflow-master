package com.zres.project.localnet.portal.report.controller;

import com.alibaba.druid.util.StringUtils;
import com.zres.project.localnet.portal.report.dao.ReportDao;
import com.zres.project.localnet.portal.report.service.ReportService;
import com.zres.project.localnet.portal.report.utils.UserUtil;
import com.zres.project.localnet.portal.util.ExcelExporter;
import com.zres.project.localnet.portal.util.ResponseHandler;
import com.ztesoft.res.frame.core.exception.ServiceBuizException;
import com.ztesoft.zsmart.pot.annotation.PublicServ;
import net.sf.json.JSONObject;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 报表系统控制层
 *
 * @author PangHao
 * @date 2019/5/13 : 11:24
 */
@Controller
@RequestMapping("/localScheduleLT/ReportController")
public class ReportController {
    Logger logger = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private ReportService reportService;
    @Autowired
    ReportDao reportDao;

    /**
     * 调度单统计导出
     *
     * @author PangHao
     * @date 2019/5/21 : 9:43
     */
    @RequestMapping("/exportDispatchOrderData.spr")
    @PublicServ
    public void exportDispatchOrderData(@RequestParam("downLoadData") String downLoadData, HttpServletRequest request,
                                        HttpServletResponse response) {
        // 表头
        List<String> heads = Arrays.asList("产品类型", "新开", "拆机", "变更", "停机", "复机", "移机", "总计");
        // 表列
        List<String> colIds = Arrays.asList("TYPE", "ADD", "DEL", "CHA", "STOP", "REP", "MOVE", "COUNT");
        // excel数据
        List<Map<String, Object>> dataExcel = new ArrayList<Map<String, Object>>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            ResponseHandler responseHandler = new ResponseHandler(request, response);
            String decode = URLDecoder.decode(downLoadData, "UTF-8");
            Map<String, Object> params = JSONObject.fromObject(decode);
            params.put("userId", UserUtil.getOperatorId());
            // 获取选中数据
            List<Map<String, Object>> list = reportDao.dispatchOrderStatistics(params);
            if (!CollectionUtils.isEmpty(list)) {
                dataExcel.addAll(list);
            }
            // 文件名
            String dateStr = sdf.format(new Date());
            String fileName = "产品类型统计表" + "-" + dateStr;
            responseHandler.setResponseFile(fileName);

            ExcelExporter exporter = new ExcelExporter(heads, colIds, dataExcel, responseHandler);
            exporter.fillSheet(fileName, null, null);
            exporter.export();
            logger.info("导出成功");

        } catch (Exception e) {
            throw new ServiceBuizException(e);
        }
    }

    /**
     * 开通及时率报表导出
     *
     * @author PangHao
     * @date 2019/5/21 : 9:43
     */
    @RequestMapping("/openTimeRateData.spr")
    @PublicServ
    public void openTimeRateData(@RequestParam("downLoadData") String downLoadData, HttpServletRequest request,
                                 HttpServletResponse response) {
        // 表头
        List<String> heads = Arrays.asList("部门", "环节名称", "电路数量", "已完成数量", "按时完成数量", "超时完成数量", "业务开通及时率", "完成平均周期（小时）", "未完成数量", "未完成正常数量", "未完成超时数量", "跨域范围");
        // 表列
        List<String> colIds = Arrays.asList("ORG_NAME", "TACHE_NAME", "COUNTNUM", "FIN_COUNT", "FIN_NOR", "FIN_OVER", "TIMELINESS", "AVG_HOUR", "UNFIN_COUNT", "UNFIN_NOR", "UNFIN_OVER", "START_TYPE");
        // excel数据
        List<Map<String, Object>> dataExcel = new ArrayList<Map<String, Object>>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            ResponseHandler responseHandler = new ResponseHandler(request, response);
            String decode = URLDecoder.decode(downLoadData, "UTF-8");
            Map<String, Object> params = JSONObject.fromObject(decode);
            // 获取选中数据
            List<Map<String, Object>> list = reportService.openTimeRateStatistics(params);
            // 循环补充平均时间及及时率
            for (Map<String, Object> map : list) {
                //完成数
                Double finCount = MapUtils.getDouble(map, "FIN_COUNT");
                //正常完成数
                Double finNor = MapUtils.getDouble(map, "FIN_NOR");
                //总耗时
                Double disHour = MapUtils.getDouble(map, "DIS_HOUR");
                //两位小数
                DecimalFormat df = new DecimalFormat("#.00");
                String timeliness;
                String avgHour;
                if (finCount != null && finCount > 0) {
                    timeliness = df.format((finNor / finCount) * 100) + "%";
                    avgHour = df.format((disHour / finCount)) + "";
                } else {
                    timeliness = "0%";
                    avgHour = "0";
                }
                map.put("TIMELINESS", timeliness);
                map.put("AVG_HOUR", avgHour);
            }
            if (!CollectionUtils.isEmpty(list)) {
                dataExcel.addAll(list);
            }
            // 文件名
            String dateStr = sdf.format(new Date());
            String fileName = "开通及时率统计报表" + "-" + dateStr;
            responseHandler.setResponseFile(fileName);

            ExcelExporter exporter = new ExcelExporter(heads, colIds, dataExcel, responseHandler);
            exporter.fillSheet(fileName, null, null);
            exporter.export();
            logger.info("导出成功");

        } catch (Exception e) {
            throw new ServiceBuizException(e);
        }
    }

    /**
     * 调度单钻取明细导出导出
     *
     * @author PangHao
     * @date 2019/5/21 : 9:43
     */
    @RequestMapping("/exportDisOrderDetailData.spr")
    @PublicServ
    public void exportDisOrderDetailData(@RequestParam("downLoadData") String downLoadData, HttpServletRequest request,
                                         HttpServletResponse response) {
        // 表头（定单查询未包含人员展示）
        List<String> heads = Arrays.asList("客户订单号", "申请单编号", "申请单标题", "调度单编号", "是否资源补录","业务号码", "电路编号", "产品类型", "动作类型", "当前环节", "创建时间", "当前处理人", "当前处理人所属分公司", "报竣时间",  "A端装机地址",   "Z端装机地址", "A端所属地市","Z端所属地市","A端要求完成时间", "Z端要求完成时间", "全程要求完成时间");
        // 表列
        List<String> colIds = Arrays.asList("SUBSCRIBE_ID", "APPLY_ORD_ID", "APPLY_ORD_NAME", "DISPATCH_ORDER_NO","ISSUPPLEMENT", "SERIAL_NUMBER", "CIRCUITCODE", "SERVICETYPE", "OPERTYPE", "TACHE_NAME", "CREATE_DATE", "USER_REAL_NAME", "ORG_NAME",  "REPORTTIME", "A_INSTALLED_ADD", "Z_INSTALLED_ADD",  "A_CITY","Z_CITY", "A_REQ_FIN_DATE", "Z_REQ_FIN_DATE","REQ_FIN_DATE" );
        // excel数据
        List<Map<String, Object>> dataExcel = new ArrayList<Map<String, Object>>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            ResponseHandler responseHandler = new ResponseHandler(request, response);
            String decode = URLDecoder.decode(downLoadData, "UTF-8");
            Map<String, Object> params = JSONObject.fromObject(decode);
            int pageSize = MapUtils.getIntValue(params,"pageSize", 10);
            int pageIndex = MapUtils.getIntValue(params, "pageIndex", 1);
            int startRow = pageSize * (pageIndex - 1);
            int endRow = pageSize * pageIndex;
            params.put("startRow", startRow);
            params.put("endRow", endRow);
            params.put("userId", UserUtil.getOperatorId());
            // 获取选中数据
            List<Map<String, Object>> list = reportDao.queryCircuitInfoForBusiReport(params);
            if (!CollectionUtils.isEmpty(list)) {
                dataExcel.addAll(list);
            }
            // 文件名
            String dateStr = sdf.format(new Date());
            String fileName = "电路明细表" + "-" + dateStr;
            responseHandler.setResponseFile(fileName);

            ExcelExporter exporter = new ExcelExporter(heads, colIds, dataExcel, responseHandler);
            exporter.fillSheet(fileName, null, null);
            exporter.export();
            logger.info("导出成功");

        } catch (Exception e) {
            throw new ServiceBuizException(e);
        }
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
            } else {
                writer.write(responseContent);
            }
        } catch (IOException var8) {
            throw new RuntimeException(var8);
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }

        }
    }

    /**
     * 导出业务网络核查
     *
     * @author PangHao
     * @date 2019/5/21 : 9:43
     */
    @RequestMapping("/exportBusNetVerData.spr")
    @PublicServ
    public void exportBusNetVerData(@RequestParam("downLoadData") String downLoadData, HttpServletRequest request,
                                    HttpServletResponse response) {
        // 表头
        List<String> heads = Arrays.asList("客户名称 ", "客户订单号", "申请单编号", "业务单编号", "业务号码", "产品类型", "动作类型", "电路编号", "报竣时间", "起租/止租时间", "详细路由", "电路历史调单编号", "带宽速率", "客户经理", "客户经理联系电话", "电路备注", "A端归属省", "A端归属地市", "A端归属分公司", "A端装机地址", "A端联系人", "A端联系电话", "Z端归属省", "Z端归属地市", "Z端归属分公司", "Z端装机地址", "Z端联系人", "Z端联系电话");
        // 表列
        List<String> colIds = Arrays.asList("custName", "subscribeId", "applyOrdId", "tradeId", "serialNumber", "productType", "operateType", "circuitCode", "reportTime", "startTime", "routInfoList", "disOrderNoList", "speedName", "custManager", "custManaPhone", "cir_remark", "A_belong_province", "A_belong_city", "A_belong_region", "A_installed_add", "A_contact_man", "A_contact_tel", "Z_belong_province", "Z_belong_city", "Z_belong_region", "Z_installed_add", "Z_contact_man", "Z_contact_tel");
        // excel数据
        List<Map<String, Object>> dataExcel = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            ResponseHandler responseHandler = new ResponseHandler(request, response);
            String decode = URLDecoder.decode(downLoadData, "UTF-8");
            Map<String, Object> params = JSONObject.fromObject(decode);
            // 获取选中数据
            Map<String, Object> map = new HashMap<>();
            map.put("pageIndex", 1);
            map.put("pageSize", 10000);
            map.put("parameters", params);
            List<Map<String, Object>> list = (List<Map<String, Object>>) reportService.businessNetworkVerification(map).get("data");
            if (!CollectionUtils.isEmpty(list)) {
                dataExcel.addAll(list);
            }
            // 文件名
            String dateStr = sdf.format(new Date());
            String fileName = "客户电路业务网络稽核" + "-" + dateStr;
            responseHandler.setResponseFile(fileName);

            ExcelExporter exporter = new ExcelExporter(heads, colIds, dataExcel, responseHandler);
            exporter.fillSheet(fileName, null, null);
            exporter.export();
            logger.info("导出成功");

        } catch (Exception e) {
            throw new ServiceBuizException(e);
        }
    }


    /**
     * 导出报竣未起租报表
     *
     * @author PangHao
     * @date 2019/5/21 : 9:43
     */
    @RequestMapping("/exportCompletionNotRentedData.spr")
    @PublicServ
    public void exportCompletionNotRentedData(@RequestParam("downLoadData") String downLoadData, HttpServletRequest request,
                                              HttpServletResponse response) {
        // 表头
        List<String> heads = Arrays.asList("客户名称", "客户订单号", "业务订单号", "业务号码(计费ID)", "申请单标题", "调单编号", "电路编号", "受理部门", "报竣时间");
        // 表列
        List<String> colIds = Arrays.asList("custName", "subscribeId", "tradeId", "serialNumber", "applyOrdName", "dispatchOrderNo", "circuitCode", "handelDEep", "reportTime");
        // excel数据
        List<Map<String, Object>> dataExcel = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            ResponseHandler responseHandler = new ResponseHandler(request, response);
            String decode = URLDecoder.decode(downLoadData, "UTF-8");
            Map<String, Object> params = JSONObject.fromObject(decode);
            // 获取选中数据
            Map<String, Object> map = new HashMap<>();
            map.put("pageIndex", 1);
            map.put("pageSize", 10000);
            map.put("parameters", params);
            List<Map<String, Object>> list = (List<Map<String, Object>>) reportService.queryCompletionNotRented(map).get("data");
            if (!CollectionUtils.isEmpty(list)) {
                dataExcel.addAll(list);
            }
            // 文件名
            String dateStr = sdf.format(new Date());
            String fileName = "报竣未起租报表" + "-" + dateStr;
            responseHandler.setResponseFile(fileName);

            ExcelExporter exporter = new ExcelExporter(heads, colIds, dataExcel, responseHandler);
            exporter.fillSheet(fileName, null, null);
            exporter.export();
            logger.info("导出成功");
        } catch (Exception e) {
            throw new ServiceBuizException(e);
        }
    }

    /**
     * 电路汇总导出
     *
     * @author PangHao
     * @date 2019/6/27  17:15
     **/
    @RequestMapping("/exportCircuitSummaryData.spr")
    @PublicServ
    public void exportCircuitSummaryData(@RequestParam("downLoadData") String downLoadData, HttpServletRequest request,
                                         HttpServletResponse response) {
        // 表头
        List<String> heads = Arrays.asList("电路编号", "产品类型", "业务动作", "详细路由", "客户名称", "业务号码", "开通调度单号", "开通时间", "电路状态",
                "带宽速率", "SLA标识", "SLA业务开通", "SLA网络质量保证", /*"SLA网络质量保证",*/ "SLA售后服务", "客户行业", "调整调单编号", "调整时间",
                "关闭调单编号", "关闭时间", "业务订单号", "实际开通时间", "电路租用范围", "客户经理", "客户经理联系电话", "A端联系人", "A端联系电话",
                "A端装机地址", "A端归属分公司", "Z端联系人", "Z端联系电话", "Z端装机地址", "Z端归属分公司", "原电路编号", "备注");
        // 表列
        List<String> colIds = Arrays.asList("CIRCUIT_NO", "PRODUCT_NAME", "OPERATE_NAME", "ROUTE_INFO", "CUST_NAME_CHINESE", "SERIAL_NUMBER", "KT_DIS_NO", "KT_TIME", "OPRSTATE",
                "speedName", "slaFlagName", "slaServOpenName", "slaNetQuAssName", /*"slaNetQuAssName",*/ "slaSaleServName", "CUST_INDUSTRY", "BG_DIS_NO", "BG_TIME",
                "STOP_DIS_NO", "STOP_TIME", "TRADE_ID", "QZ_TIME", "cirLeaseRangeName", "custManager", "custManaPhone", "A_contact_man", "A_contact_tel",
                "A_installed_add", "A_belong_region", "Z_contact_man", "Z_contact_tel", "Z_installed_add", "Z_belong_region", "oldCircuitCode", "cir_remark");
        // excel数据
        List<Map<String, Object>> dataExcel = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            ResponseHandler responseHandler = new ResponseHandler(request, response);
            String decode = URLDecoder.decode(downLoadData, "UTF-8");
            Map<String, Object> params = JSONObject.fromObject(decode);
            params.put("userId", UserUtil.getOperatorId());
            Map<String, Object> map = new HashMap<>();
            map.put("pageIndex", 1);
            map.put("pageSize", 10000);
            map.put("parameters", params);
            List<Map<String, Object>> list = (List<Map<String, Object>>) reportService.queryCircuitSummaryList(map).get("data");
            if (!CollectionUtils.isEmpty(list)) {
                dataExcel.addAll(list);
                // 文件名
                String dateStr = sdf.format(new Date());
                String fileName = "电路汇总明细" + "-" + dateStr;
                responseHandler.setResponseFile(fileName);
                ExcelExporter exporter = new ExcelExporter(heads, colIds, dataExcel, responseHandler);
                exporter.fillSheet(fileName, null, null);
                exporter.export();
                logger.info("导出成功");
            }
        } catch (Exception e) {
            throw new ServiceBuizException(e);
        }
    }

}
