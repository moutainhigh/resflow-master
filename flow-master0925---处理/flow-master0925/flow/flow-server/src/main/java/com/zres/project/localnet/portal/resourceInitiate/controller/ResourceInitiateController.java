package com.zres.project.localnet.portal.resourceInitiate.controller;

import com.zres.project.localnet.portal.resourceInitiate.service.ResourceInitiateServiceIntf;
import com.zres.project.localnet.portal.util.ExcelExporter;
import com.zres.project.localnet.portal.util.ResponseHandler;
import com.zres.project.localnet.portal.webservice.res.BusinessQueryServiceIntf;
import com.ztesoft.res.frame.flow.common.exception.FlowException;
import net.sf.json.JSONObject;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * controller
 *
 * @author caomm on 2020-01-08
 */
@Controller
@RequestMapping("/localScheduleLT/resourceInitiate")
public class ResourceInitiateController {

    Logger logger = LoggerFactory.getLogger(ResourceInitiateController.class);

    @Autowired
    private BusinessQueryServiceIntf businessQueryServiceIntf;
    @Autowired
    private ResourceInitiateServiceIntf resourceInitiateServiceIntf;

    @RequestMapping(value = "/exportCircuitInfo.spr")
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

    public Map<String, Object> startResourceInitiateFlow(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap<>();
        String startOrSupp = MapUtils.getString(param, "startOrSupp");
        try{
            if ("supp".equals(startOrSupp)){
                resourceInitiateServiceIntf.startFlow(param);
            } else if("start".equals(startOrSupp)){
                resourceInitiateServiceIntf.startFlowFromSec(param);
            }
            resMap.put("success", true);
            resMap.put("msg", "资源补录起流程成功！");
        }catch (FlowException fe){
            fe.printStackTrace();
            resMap.put("success", false);
            resMap.put("msg", "资源补录起流程失败！" + fe);
        }catch(Exception e){
            e.printStackTrace();
            resMap.put("success", false);
            resMap.put("msg", "资源补录起流程失败！" + e);
        }
        return resMap;
    }

}