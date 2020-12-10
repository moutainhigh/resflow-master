package com.zres.project.localnet.portal.order.controller;

import com.zres.project.localnet.portal.order.service.OrderQueryListServiceIntf;
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
 * @date : 2019/4/19
 */
@Controller
@RequestMapping("/localScheduleLT/gomOrderQueryController")
public class GomOrderQueryController {
    Logger logger = LoggerFactory.getLogger(GomOrderQueryController.class);
    @Autowired
    private OrderQueryListServiceIntf orderQueryListServiceIntf;

    @PublicServ
    @RequestMapping("/exportOrderList.spr")
    public void exportOrderList(@RequestBody String downLoadData, HttpServletRequest request, HttpServletResponse response) {
        try {

                //表头
                List<String> heads = new ArrayList<String>();
                //表列
                List<String> colIds = new ArrayList<String>();
                //excel数据 支持泛型为Map<String,Object>
                List<Map<String, Object>> dataExcel = new ArrayList<Map<String, Object>>();
                List<Map<String, Object>> selarrrow = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

                ResponseHandler responseHandler = new ResponseHandler(request, response);
                String decode = URLDecoder.decode(downLoadData, "UTF-8").substring(13);
                int csrf = decode.lastIndexOf("&_csrf");
                String csrfStr = decode.substring(0,csrf);
                Map<String, Object> params = JSONObject.fromObject(csrfStr);
                //获取选中数据
                selarrrow = (List<Map<String, Object>>) params.get("selarrrow");
                String dateStr = sdf.format(new Date());

                if (selarrrow.size() <= 0) {
                    selarrrow = orderQueryListServiceIntf.exportrderList(params);
                }
                if (!CollectionUtils.isEmpty(selarrrow)) {
                    dataExcel.addAll(selarrrow);
                }
                //文件名
//                String fileName = dateStr + "-调单查询" ;
                StringBuffer fileName = new StringBuffer();
                fileName.append(dateStr).append("-调单查询");
                responseHandler.setResponseFile(fileName.toString());
                heads.addAll(getGomOrderHeadList());
                colIds.addAll(getGomOrderColList());
                ExcelExporter exporter = new ExcelExporter(heads, colIds, dataExcel, responseHandler);
                exporter.fillSheet(fileName.toString(), null, null);
                exporter.export();
                logger.info("导出成功");

            }
            catch (Exception e) {
                throw new ServiceBuizException(e);
            }
        }

    public List<String> getGomOrderHeadList() {
        List<String> gomHeadList = new ArrayList<String>();
        gomHeadList.add("客户名称");
        gomHeadList.add("客户订单号");
        gomHeadList.add("申请单编号");
        gomHeadList.add("申请单标题");
        gomHeadList.add("调度单编号");
        gomHeadList.add("调度单标题");
        gomHeadList.add("业务号码");
        gomHeadList.add("电路编号");
        gomHeadList.add("产品类型");
        gomHeadList.add("动作类型");
        gomHeadList.add("当前环节");
        gomHeadList.add("要求完成时间");
        return gomHeadList;
    }

    public List<String> getGomOrderColList() {
        List<String> gomColList = new ArrayList<String>();
        gomColList.add("CUST_NAME_CHINESE");
        gomColList.add("SUBSCRIBE_ID");
        gomColList.add("APPLY_ORD_ID");
        gomColList.add("APPLY_ORD_NAME");
        gomColList.add("DISPATCH_ORDER_NO");
        gomColList.add("DISPATCH_TITLE");
        gomColList.add("SERIAL_NUMBER");
        gomColList.add("CIRCODE");
        gomColList.add("SERVICETYPE");
        gomColList.add("ACTIVETYPENAME");
        gomColList.add("TACHE_NAME");
        gomColList.add("REQ_FIN_DATE");
        return gomColList;
    }


}
