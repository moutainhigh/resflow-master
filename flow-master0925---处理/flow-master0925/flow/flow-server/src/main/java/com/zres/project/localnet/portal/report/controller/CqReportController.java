package com.zres.project.localnet.portal.report.controller;

import com.zres.project.localnet.portal.report.dao.CqReportDao;
import com.zres.project.localnet.portal.report.service.CqReportService;
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
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 报表系统控制层
 *
 * @author PangHao
 * @date 2019/5/13 : 11:24
 */
@Controller
@RequestMapping("/localScheduleLT/CqReportController")
public class CqReportController {
    Logger logger = LoggerFactory.getLogger(CqReportController.class);

    @Autowired
    CqReportDao cqReportDao;
    @Autowired
    CqReportService cqReportService;


    /**
     * 未完成环节工单明细导出
     *
     * @param downLoadData 筛选条件
     * @author PangHao
     * @date 2019/6/5  11:33
     **/
    @RequestMapping("/exportUndoneTacheDate.spr")
    @PublicServ
    public void exportUndoneTacheData(@RequestParam("downLoadData") String downLoadData, HttpServletRequest request,
                                      HttpServletResponse response) {
        // 表头
        List<String> heads = Arrays.asList("申请单号","业务订单号","申请单标题","当前环节名称","电路编号","电路调度环节处理人所属分公司","电路调度环节处理人员/角色","当前环节所属分公司","申请单创建时间","客户名称","调度单号","派发类型","当前环节单位","当前处理人","当前处理人电话","发起单位（分公司）","动作类型","单据类型","到单时间","产品类型","是否退单","单据来源","派发岗位","业务号码","客户订单号","环节历时","环节超时时长","是否超时"); // 表列
        List<String> colIds = Arrays.asList("APPLY_ORD_ID","TRADE_ID","APPLY_ORD_NAME","TACHE_NAME","ATTR_VALUE","CD_ORG","CD_STAFF","DISP_DEP","CREATE_DATE","CUST_NAME_CHINESE","DISPATCH_ORDER_NO","DISP_OBJ_TYE","DISP_ORG_NAME","DU_STAFF_NAME","SU_STAFF_PHONE","HANDLE_DEP","OPERATE_NAME","ORDER_TYEP","ARRIVAL_TIME","PRODUCT_NAME","ISRETURN","RESOURCES_NAME","SEND_ORG_NAME","SERIAL_NUMBER","SUBSCRIBE_ID","TACHE_DURATION","TACHETIMEOUT","ISTIMEOUT");
        // excel数据
        List<Map<String, Object>> dataExcel = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            ResponseHandler responseHandler = new ResponseHandler(request, response);
            String decode = URLDecoder.decode(downLoadData, "UTF-8");
            Map<String, Object> params = JSONObject.fromObject(decode);
            Map<String, Object> sonCompany = cqReportDao.getSonCompany(UserUtil.getOperatorId());
            params.put("areaId", MapUtils.getString(sonCompany, "AREA_ID"));
            params.put("orgId", MapUtils.getString(sonCompany, "ORG_ID"));
            params.put("userId", UserUtil.getOperatorId());
            List<Map<String, Object>> list = cqReportDao.queryUndoneTacheList(params);
            if (!CollectionUtils.isEmpty(list)) {
                dataExcel.addAll(list);
                // 文件名
                String dateStr = sdf.format(new Date());
                String fileName = "未完成环节工单明细" + "-" + dateStr;
                responseHandler.setResponseFile(fileName);

                String sheetName = fileName;
                if (!"".equals(MapUtils.getString(params, "beginDate")) || !"".equals(MapUtils.getString(params, "endDate"))) {
                    sheetName = MapUtils.getString(params, "endDate") + "至" + MapUtils.getString(params, "endDate");
                }
                ExcelExporter exporter = new ExcelExporter(heads, colIds, dataExcel, responseHandler);
                exporter.fillSheet(sheetName, null, null);
                exporter.export();
                logger.info("导出成功");
            }
        } catch (Exception e) {
            throw new ServiceBuizException(e);
        }
    }

    @RequestMapping("/exportUndoneTacheStaData.spr")
    @PublicServ
    public void exportUndoneTacheStaData(@RequestParam("downLoadData") String downLoadData, HttpServletRequest request,
                                         HttpServletResponse response) {
        // 表头
        List<String> heads = new ArrayList<>();
        // 表列
        List<String> colIds = new ArrayList<>();
        // excel数据
        List<Map<String, Object>> dataExcel = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            ResponseHandler responseHandler = new ResponseHandler(request, response);
            String decode = URLDecoder.decode(downLoadData, "UTF-8");
            Map<String, Object> params = JSONObject.fromObject(decode);
            List dataList = (List) MapUtils.getObject(params, "data");
            List titleList = (List) MapUtils.getObject(params, "title");
            Map queryMap = MapUtils.getMap(params, "queryData");

            if (!CollectionUtils.isEmpty(dataList)) {
                for (Object titleMap : titleList) {
                    heads.add(MapUtils.getString((Map) titleMap, "label"));
                    colIds.add(MapUtils.getString((Map) titleMap, "name"));
                }
                heads.set(heads.size() - 1, "合计");
                dataExcel.addAll(dataList);
                // 文件名
                String dateStr = sdf.format(new Date());
                String fileName = "未完成环节工单统计" + "-" + dateStr;
                responseHandler.setResponseFile(fileName);

                String sheetName = fileName;
                if (!"".equals(MapUtils.getString(params, "beginDate")) || !"".equals(MapUtils.getString(params, "endDate"))) {
                    sheetName = MapUtils.getString(params, "endDate") + "至" + MapUtils.getString(params, "endDate");
                }
                ExcelExporter exporter = new ExcelExporter(heads, colIds, dataExcel, responseHandler);
                exporter.fillSheet(sheetName, null, null);
                exporter.export();
                logger.info("导出成功");
            }
        } catch (Exception e) {
            throw new ServiceBuizException(e);
        }
    }


    /**
     * 超时未报竣电路明细导出
     *
     * @author PangHao
     * @date 2019/6/12  10:20
     **/
    @RequestMapping("/exportOvertimeUnfinishedData.spr")
    @PublicServ
    public void exportOvertimeUnfinishedData(@RequestParam("downLoadData") String downLoadData, HttpServletRequest request,
                                             HttpServletResponse response) {
        // 表头
        List<String> heads = Arrays.asList("单据来源", "发起单位（分公司）", "申请单号", "申请单创建时间", "申请单标题", "客户名称", "产品类型", "业务号码", "电路编号", "动作类型", "全程要求完成时间", "电路调度环节处理人所属分公司", "电路调度环节处理人员/角色", "调度单号", "客户订单号", "业务订单号", "超时天数");
        // 表列
        List<String> colIds = Arrays.asList("RESOURCES_NAME", "HANDLE_DEP", "APPLY_ORD_ID", "CREATE_DATE", "APPLY_ORD_NAME", "CUST_NAME_CHINESE", "PRODUCT_NAME", "SERIAL_NUMBER", "ATTR_VALUE", "OPERATE_NAME", "REQ_FIN_DATE", "CD_ORG", "CD_STAFF", "DISPATCH_ORDER_NO", "TRADE_ID", "SUBSCRIBE_ID", "OVER_TIME");
        // excel数据
        List<Map<String, Object>> dataExcel = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            ResponseHandler responseHandler = new ResponseHandler(request, response);
            String decode = URLDecoder.decode(downLoadData, "UTF-8");
            Map<String, Object> params = JSONObject.fromObject(decode);
            params.put("userId", UserUtil.getOperatorId());
            Map<String, Object> sonCompany = cqReportDao.getSonCompany(UserUtil.getOperatorId());
            params.put("areaId", MapUtils.getString(sonCompany, "AREA_ID"));
            params.put("orgId", MapUtils.getString(sonCompany, "ORG_ID"));
            List<Map<String, Object>> list = cqReportDao.queryOvertimeUnfinishedList(params);
            if (!CollectionUtils.isEmpty(list)) {
                dataExcel.addAll(list);
                // 文件名
                String dateStr = sdf.format(new Date());
                String fileName = "超时未报竣电路明细" + "-" + dateStr;
                responseHandler.setResponseFile(fileName);
                String sheetName = fileName;
                if (!"".equals(MapUtils.getString(params, "beginDate")) || !"".equals(MapUtils.getString(params, "endDate"))) {
                    sheetName = MapUtils.getString(params, "endDate") + "至" + MapUtils.getString(params, "endDate");
                }

                ExcelExporter exporter = new ExcelExporter(heads, colIds, dataExcel, responseHandler);
                exporter.fillSheet(sheetName, null, null);
                exporter.export();
                logger.info("导出成功");
            }
        } catch (Exception e) {
            throw new ServiceBuizException(e);
        }
    }


    /**
     * 导出电路完工及时率汇总表
     *
     * @author PangHao
     * @date 2019/6/17  10:55
     **/
    @RequestMapping("/exportFinishTimeRareData.spr")
    @PublicServ
    public void exportFinishTimeRareData(@RequestParam("downLoadData") String downLoadData, HttpServletRequest request,
                                         HttpServletResponse response) {
        // 表头
        List<String> heads = new ArrayList<>();
        // 表列
        List<String> colIds = new ArrayList<>();
        // excel数据
        List<Map<String, Object>> dataExcel = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            ResponseHandler responseHandler = new ResponseHandler(request, response);
            downLoadData = downLoadData.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
            downLoadData = downLoadData.replaceAll("\\+", "%2B");
            String decode = URLDecoder.decode(downLoadData, "UTF-8");
            Map<String, Object> params = JSONObject.fromObject(decode);
            List dataList = (List) MapUtils.getObject(params, "data");
            List titleList = (List) MapUtils.getObject(params, "title");

            if (!CollectionUtils.isEmpty(dataList)) {
                for (Object titleMap : titleList) {
                    heads.add(MapUtils.getString((Map) titleMap, "label"));
                    colIds.add(MapUtils.getString((Map) titleMap, "name"));
                }
                dataExcel.addAll(dataList);
                // 文件名
                String dateStr = sdf.format(new Date());
                String fileName = "电路完工及时率汇总" + "-" + dateStr;
                responseHandler.setResponseFile(fileName);
                String sheetName = fileName;
                if (!"".equals(MapUtils.getString(params, "beginDate")) || !"".equals(MapUtils.getString(params, "endDate"))) {
                    sheetName = MapUtils.getString(params, "endDate") + "至" + MapUtils.getString(params, "endDate");
                }

                ExcelExporter exporter = new ExcelExporter(heads, colIds, dataExcel, responseHandler);
                exporter.fillSheet(sheetName, null, null);
                exporter.export();
                logger.info("导出成功");
            }
        } catch (Exception e) {
            throw new ServiceBuizException(e);
        }
    }


    /**
     * 资源分配未入库明细导出
     *
     * @author PangHao
     * @date 2019/6/20  9:57
     **/
    @RequestMapping("/exportResAllocateUnStorageData.spr")
    @PublicServ
    public void exportResAllocateUnStorageData(@RequestParam("downLoadData") String downLoadData, HttpServletRequest request,
                                               HttpServletResponse response) {
        // 表头
        List<String> heads = Arrays.asList("单据来源", "发起单位（分公司）", "申请单号", "申请单创建时间", "申请单标题", "客户名称", "产品类型", "业务号码", "电路编号", "动作类型", "调度单号", "客户订单号", "资源分配未入库专业", "资源分配处理人员所属区域", "资源分配处理人员", "是否报竣", "业务订单号");
        // 表列
        List<String> colIds = Arrays.asList("RESOURCES_NAME", "HANDLE_DEP", "APPLY_ORD_ID", "CREATE_DATE", "APPLY_ORD_NAME", "CUST_NAME_CHINESE", "PRODUCT_NAME", "SERIAL_NUMBER", "ATTR_VALUE", "OPERATE_NAME", "DISPATCH_ORDER_NO", "SUBSCRIBE_ID", "UN_SAVE_SPECIALTY", "FP_ORG", "FP_STAFF", "IS_FINISH", "TRADE_ID");
        // excel数据
        List<Map<String, Object>> dataExcel = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            ResponseHandler responseHandler = new ResponseHandler(request, response);
            String decode = URLDecoder.decode(downLoadData, "UTF-8");
            Map<String, Object> params = JSONObject.fromObject(decode);
            params.put("userId", UserUtil.getOperatorId());
            List<Map<String, Object>> list = cqReportDao.queryResAllocateUnStorageList(params);
            if (!CollectionUtils.isEmpty(list)) {
                dataExcel.addAll(list);
                // 文件名
                String dateStr = sdf.format(new Date());
                String fileName = "资源分配未入库明细" + "-" + dateStr;
                responseHandler.setResponseFile(fileName);
                String sheetName = fileName;
                if (!"".equals(MapUtils.getString(params, "beginDate")) || !"".equals(MapUtils.getString(params, "endDate"))) {
                    sheetName = MapUtils.getString(params, "endDate") + "至" + MapUtils.getString(params, "endDate");
                }
                ExcelExporter exporter = new ExcelExporter(heads, colIds, dataExcel, responseHandler);
                exporter.fillSheet(sheetName, null, null);
                exporter.export();
                logger.info("导出成功");
            }
        } catch (Exception e) {
            throw new ServiceBuizException(e);
        }
    }


    /**
      * 资源分配未入库汇总导出
      * @author PangHao
      * @date   2019/6/20  10:10
     **/
    @RequestMapping("/exportResAllocateUnStorageStatisticsData.spr")
    @PublicServ
    public void exportResAllocateUnStorageStatisticsData(@RequestParam("downLoadData") String downLoadData, HttpServletRequest request,
                                         HttpServletResponse response) {
        // 表头
        List<String> heads = new ArrayList<>();
        // 表列
        List<String> colIds = new ArrayList<>();
        // excel数据
        List<Map<String, Object>> dataExcel = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            ResponseHandler responseHandler = new ResponseHandler(request, response);
            downLoadData = downLoadData.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
            downLoadData = downLoadData.replaceAll("\\+", "%2B");
            String decode = URLDecoder.decode(downLoadData, "UTF-8");
            Map<String, Object> params = JSONObject.fromObject(decode);
            List dataList = (List) MapUtils.getObject(params, "data");
            List titleList = (List) MapUtils.getObject(params, "title");
            if (!CollectionUtils.isEmpty(dataList)) {
                for (Object titleMap : titleList) {
                    heads.add(MapUtils.getString((Map) titleMap, "label"));
                    colIds.add(MapUtils.getString((Map) titleMap, "name"));
                }
                dataExcel.addAll(dataList);
                // 文件名
                String dateStr = sdf.format(new Date());
                String fileName = "资源分配未入库汇总" + "-" + dateStr;
                responseHandler.setResponseFile(fileName);
                String sheetName = fileName;
                if (!"".equals(MapUtils.getString(params, "beginDate")) || !"".equals(MapUtils.getString(params, "endDate"))) {
                    sheetName = MapUtils.getString(params, "endDate") + "至" + MapUtils.getString(params, "endDate");
                }
                ExcelExporter exporter = new ExcelExporter(heads, colIds, dataExcel, responseHandler);
                exporter.fillSheet(sheetName, null, null);
                exporter.export();
                logger.info("导出成功");
            }
        } catch (Exception e) {
            throw new ServiceBuizException(e);
        }
    }

}
