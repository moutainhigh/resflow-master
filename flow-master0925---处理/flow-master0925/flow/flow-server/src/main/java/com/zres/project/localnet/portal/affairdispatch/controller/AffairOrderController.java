package com.zres.project.localnet.portal.affairdispatch.controller;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.zres.project.localnet.portal.affairdispatch.constants.AffairDispatchOrderConstant;
import com.zres.project.localnet.portal.affairdispatch.service.DispatchOrderManageService;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Classname AffairOrderController
 * @Description 事务调单
 * @Created by zou.huaqin
 * @Date 2019-05-08 13:57
 */
@Controller
@RequestMapping("/localScheduleLT/affairOrderController")
public class AffairOrderController {
    Logger logger = LoggerFactory.getLogger(AffairOrderController.class);

    @Autowired
    private DispatchOrderManageService dispatchOrderManageService;

    @RequestMapping("/exportAffairOrderData.spr")
    @PublicServ
    public void uploadFiles(@RequestParam("downLoadData") String downLoadData, HttpServletRequest request,
                            HttpServletResponse response) {
        String queryTypeName = "";
        // 表头
        List<String> heads = new ArrayList<String>();
        // 表列
        List<String> colIds = new ArrayList<String>();
        // excel数据
        List<Map<String, Object>> dataExcel = new ArrayList<Map<String, Object>>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        try {
            ResponseHandler responseHandler = new ResponseHandler(request, response);
            String decode = URLDecoder.decode(downLoadData, "UTF-8");
            Map<String, Object> params = JSONObject.fromObject(decode);
            params.put("pageIndex", 1);
            params.put("pageSize", 100000);
            String queryType = (String) params.get("queryType"); // 查询类型
            // 获取选中数据
            String dateStr = sdf.format(new Date());
            if ("fqOrder".equals(queryType)) {
                queryTypeName = "我的发起";
            } else if ("cgOrder".equals(queryType)) {
                queryTypeName = "草稿箱";
            } else if ("shOrder".equals(queryType)) {
                queryTypeName = "审核事务";
            } else if ("clOrder".equals(queryType)) {
                queryTypeName = "处理事务";
            } else if ("qrOrder".equals(queryType)) {
                queryTypeName = "确认事务";
            } else if ("tzOrder".equals(queryType)) {
                queryTypeName = "事务通知";
            } else if ("lsOrder".equals(queryType)) {
                queryTypeName = "历史工单";
            }
            heads.addAll(getOtherHeadList());
            colIds.addAll(getOtherColList());
            Map<String, Object> map = dispatchOrderManageService.queryAffairOrderList(params);
            List<Map<String, Object>> list = (List<Map<String, Object>>) map.get("data");
            //数据处理
            for (Map<String, Object> data : list) {
                MapUtils.getString(data, "DISPOSE_STAFF_ARR");
                data.put("DISPOSE_STAFF_ARR", translateStaffNameArr(MapUtils.getString(data, "DISPOSE_STAFF_ARR")));
                data.put("STATE", translateState(MapUtils.getString(data, "STATE")));
                data.put("IS_CHECK", "0".equals(MapUtils.getString(data, "IS_CHECK")) ? "审核" : "不审核");
                data.put("WO_STATE", translateWoState(data));
            }

            if (!CollectionUtils.isEmpty(list)) {
                dataExcel.addAll(list);
            }
            // 文件名
            String fileName = dateStr + "-" + queryTypeName;
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
     * 工单状态转义
     *
     * @param data 行数据
     * @return 工单状态名
     * @author PangHao
     * @date 2019/5/24 : 15:29
     */
    private String translateWoState(Map<String, Object> data) {
        String stateValue = MapUtils.getString(data, "WO_STATE");
        String name;
        switch (stateValue) {
            case "290000003":
                name = "被签出";
                break;
            case "290000005":
                name = "已作废";
                break;
            case "290000004":
                name = "已完成";
                break;
            case "290000002":
                if (Objects.equals("", data.get("PRIV_FORWARD_WO_ID"))) {
                    name = "驳回重新执行中";
                    break;
                } else {
                    name = "执行中";
                    break;
                }
            case "290000009":
                name = "待解挂";
                break;
            case "290000006":
                name = "主动驳回";
                break;
            case "290000007":
                name = "被动驳回";
                break;
            case "290000001":
                name = "未派发";
                break;
            case "290000008":
                name = "挂起";
                break;
            case "290000110":
                name = "已启子流程";
                break;
            case "290000111":
                name = "等一干通知";
                break;
            default:
                name = "";
                break;
        }
        return name;
    }

    /**
     * 处理人JSON转义
     *
     * @param disposeStaffArr 处理人JSON
     * @return 处理人字符串
     * @author PangHao
     * @date 2019/5/24 : 14:52
     */
    private Object translateStaffNameArr(String disposeStaffArr) {
        StringBuilder staffArr = new StringBuilder();
        if (null != disposeStaffArr && !"".equals(disposeStaffArr.trim())) {
            List<String> disposeStaffList = JSONArray.parseArray(disposeStaffArr, String.class);
            for (String json : disposeStaffList) {
                JSONObject params = JSONObject.fromObject(json);
                String staffName = MapUtils.getString(params, "name");
                if ("".equals(staffArr.toString())) {
                    staffArr.append(staffName);
                } else {
                    staffArr.append(",").append(staffName);
                }
            }
        }
        return staffArr.toString();
    }


    /**
     * 转义状态值
     *
     * @param state 状态枚举值
     * @return 状态名称
     * @author PangHao
     * @date 2019/5/24 : 14:52
     */
    private String translateState(String state) {
        String stateName = "";
        if (AffairDispatchOrderConstant.AFFAIR_DIS_STATE_1.equals(state)) {
            stateName = "草稿箱";
        }
        if (AffairDispatchOrderConstant.AFFAIR_DIS_STATE_2.equals(state)) {
            stateName = "发起事务";
        }
        if (AffairDispatchOrderConstant.AFFAIR_DIS_STATE_3.equals(state)) {
            stateName = "事务审核驳回";
        }
        if (AffairDispatchOrderConstant.AFFAIR_DIS_STATE_4.equals(state)) {
            stateName = "事务审核";
        }
        if (AffairDispatchOrderConstant.AFFAIR_DIS_STATE_5.equals(state)) {
            stateName = "事务处理中";
        }
        if (AffairDispatchOrderConstant.AFFAIR_DIS_STATE_6.equals(state)) {
            stateName = "事务处理驳回";
        }
        if (AffairDispatchOrderConstant.AFFAIR_DIS_STATE_7.equals(state)) {
            stateName = "事务已处理";
        }
        if (AffairDispatchOrderConstant.AFFAIR_DIS_STATE_8.equals(state)) {
            stateName = "事务确认";
        }
        if (AffairDispatchOrderConstant.AFFAIR_DIS_STATE_9.equals(state)) {
            stateName = "已完成";
        }
        if (AffairDispatchOrderConstant.AFFAIR_DIS_STATE_10.equals(state)) {
            stateName = "已关闭";
        }
        return stateName;
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

    public List<String> getOtherHeadList() {
        // 处理中header
        List<String> applyHeadList = new ArrayList<String>();
        applyHeadList.add("事务调单编码");
        applyHeadList.add("事务调单标题");
        applyHeadList.add("事务调单内容");
        applyHeadList.add("创建人");
        applyHeadList.add("创建时间");
        applyHeadList.add("调单处理人");
        applyHeadList.add("是否审核");
        applyHeadList.add("审核人");
        applyHeadList.add("事务单状态");
        //applyHeadList.add("流程环节");
        applyHeadList.add("工单状态");
        applyHeadList.add("环节要求处理时间");
        return applyHeadList;
    }

    public List<String> getOtherColList() {
        List<String> applyColList = new ArrayList<String>();
        applyColList.add("AFFAIR_DISPATCH_ORDER_CODE".toUpperCase());
        applyColList.add("TITLE".toUpperCase());
        applyColList.add("CONTENT".toUpperCase());
        applyColList.add("CREATE_STAFF_NAME".toUpperCase());
        applyColList.add("CREATE_DATE".toUpperCase());
        applyColList.add("DISPOSE_STAFF_ARR".toUpperCase());
        applyColList.add("IS_CHECK".toUpperCase());
        applyColList.add("CHECK_STAFF_NAME".toUpperCase());
        applyColList.add("STATE".toUpperCase());
        //applyColList.add("TACHE_NAME".toUpperCase());
        applyColList.add("WO_STATE".toUpperCase());
        applyColList.add("REQ_FIN_DATE".toUpperCase());
        return applyColList;
    }
}
