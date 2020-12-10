package com.zres.project.localnet.portal.messageList.controller;

import com.zres.project.localnet.portal.messageList.service.MessageListServiceIntf;
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
 * 消息列表记录
 * @author wangsen
 * @date 2020/9/28 10:29
 */
@Controller
@RequestMapping("/messageList/messageListController")
public class MessageListController {
    Logger logger = LoggerFactory.getLogger(MessageListController.class);

    @Autowired
    private MessageListServiceIntf messageListServiceIntf;

    @PublicServ
    @RequestMapping("/exportMessageList.spr")
    public void exportOrderList(@RequestBody String downLoadData, HttpServletRequest request, HttpServletResponse response) {
        try {
            //表头
            List<String> heads = new ArrayList<String>();
            //表列
            List<String> colIds = new ArrayList<String>();
            List<Map<String, Object>> dataExcel = new ArrayList<Map<String, Object>>();
            List<Map<String, Object>> selarrrow = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

            ResponseHandler responseHandler = new ResponseHandler(request, response);
            String decode = URLDecoder.decode(downLoadData, "UTF-8").substring(13);
            int csrf = decode.lastIndexOf("&_csrf");
            String csrfStr = decode.substring(0, csrf);
            Map<String, Object> params = JSONObject.fromObject(csrfStr);
            //获取选中数据
            selarrrow = (List<Map<String, Object>>) params.get("selarrrow");
            String dateStr = sdf.format(new Date());

            if (selarrrow.size() <= 0) {
                selarrrow = messageListServiceIntf.messageList(params);
            }
            if (!CollectionUtils.isEmpty(selarrrow)) {
                dataExcel.addAll(selarrrow);
            }
            StringBuffer fileName = new StringBuffer();
            fileName.append(dateStr).append("-消息列表");
            responseHandler.setResponseFile(fileName.toString());
            heads.addAll(getMessageHeadList());
            colIds.addAll(getMessageColList());
            ExcelExporter exporter = new ExcelExporter(heads, colIds, dataExcel, responseHandler);
            exporter.fillSheet(fileName.toString(), null, null);
            exporter.export();
            logger.info("导出成功");

        }
        catch (Exception e) {
            throw new ServiceBuizException(e);
        }
    }

    public List<String> getMessageHeadList() {
        List<String> gomHeadList = new ArrayList<String>();
        gomHeadList.add("产品类型");
        gomHeadList.add("订单编号");
        gomHeadList.add("申请单编号");
        gomHeadList.add("业务号码");
        gomHeadList.add("专业");
        gomHeadList.add("人员");
        gomHeadList.add("已完成操作");
        return gomHeadList;
    }

    public List<String> getMessageColList() {
        List<String> gomColList = new ArrayList<String>();
        gomColList.add("PROD_TYPE");
        gomColList.add("ORDER_NO");
        gomColList.add("APPLY_ORDER");
        gomColList.add("SERIAL_NUMBER");
        gomColList.add("SPEC_NAME");
        gomColList.add("USER_NAME");
        gomColList.add("MESSAGE_ALIAS");
        return gomColList;
    }
}
