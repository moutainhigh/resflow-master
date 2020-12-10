package com.zres.project.localnet.portal.flowdealinfo.controller;

import com.alibaba.fastjson.JSON;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf;
import com.zres.project.localnet.portal.flowdealinfo.service.OrderSendMsgService;
import com.zres.project.localnet.portal.localStandbyInfo.service.OrderStandbyServiceIntf;
import com.ztesoft.res.frame.core.util.ListUtil;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import com.ztesoft.zsmart.pot.annotation.PublicServ;
import net.sf.json.JSONObject;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/localScheduleLT/FlieUpdateController")
public class FlieUpdateController extends HttpServlet {

    Logger logger = LoggerFactory.getLogger(FlieUpdateController.class);

    @Autowired
    private OrderStandbyServiceIntf orderStandbyServiceIntf;

    @Autowired
    private OrderDealServiceIntf orderDealServiceIntf;
    @Autowired
    private OrderSendMsgService orderSendMsgService;

    @RequestMapping("/uploadFiles.spr")
    @PublicServ
    public void uploadFiles(HttpServletRequest request, HttpServletResponse response) {
        /**
         * 这个地方必须先入库调单，上传附件，再回单，
         * 因为回单中会用到上传的附件，请各位大侠手下留情，这里的顺序不要改，如果有需要要谨慎哦。。
         */
        logger.info("工单流转。。含附件。。。。。。。。。。。。。。。。。。。。");
        PrintWriter writer = null;
        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        Map<String, Object> resMap = new HashMap<String, Object>();
        try {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            MultiValueMap<String, MultipartFile> multiFileMap = multipartRequest.getMultiFileMap();
            String ctxPath = request.getSession().getServletContext().getRealPath("/") + File.separator + "upload";
            String paramStr = request.getParameter("params");
            JSONObject jasonObject = JSONObject.fromObject(paramStr);
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>接收数据参数：" + jasonObject);
            Map params = (Map) jasonObject;
            String action = MapUtils.getString(params, "action");
            String applyWoId = MapUtils.getString(params, "woId");
            String origin = MapUtils.getString(params, "origin","HJ");
            String delFiles = MapUtils.getString(params, "delFiles");
            String sysResFullCom = MapUtils.getString(params, "sysResFullCom");
            String oneDryValue = MapUtils.getString(params, "oneDryValue");
            String circuitDataStr = MapUtils.getString(params, "circuitData");

            String srvOrdIdStr = "";
            List<Object> circuitDatalist = JSON.parseArray(circuitDataStr);
            List<String> orderIdList = new ArrayList();
            for (Object object : circuitDatalist) {
                Map<String, Object> uploadMap = new HashMap();
                Map<String, Object> circuitDataMap = (Map<String, Object>) object;
                String woId = MapUtils.getString(circuitDataMap, "WO_ID");
                String orderId = MapUtils.getString(circuitDataMap, "ORDER_ID");
                String tacheId = MapUtils.getString(circuitDataMap, "TACHE_ID");
                String psId = MapUtils.getString(circuitDataMap, "PS_ID");
                String srvOrdId = MapUtils.getString(circuitDataMap, "SRV_ORD_ID");
                String relateInfoId = MapUtils.getString(circuitDataMap, "RELATE_INFO_ID");
                String finishDateStr = MapUtils.getString(params, "finishDate");

                if(BasicCode.COMPLETE_CONFIRM.equals(tacheId) && !"".equals(finishDateStr) && finishDateStr!=null) {
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置日期格式
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                    Date finishDate = df.parse(finishDateStr);
                    finishDateStr = dateFormat.format(finishDate);
                    params.put("ATTR_CODE", "REC_30004");
                    params.put("SRV_ORD_ID", srvOrdId);
                    params.put("ATTR_ACTION", 0);
                    params.put("ATTR_NAME", "全程竣工时间");
                    params.put("ATTR_VALUE", finishDateStr);
                    params.put("SOURSE", "内部建单");
                    params.put("ATTR_VALUE_NAME", "");
                    params.put("CREATE_DATE", df.format(new Date()));
                }

                if ("".equals(srvOrdIdStr)) {
                    srvOrdIdStr = srvOrdId;
                } else {
                    srvOrdIdStr = srvOrdIdStr + "," + srvOrdId;
                }
                params.put("woId", woId);
                params.put("orderId", orderId);
                params.put("tacheId", tacheId);
                params.put("psId", psId);
                params.put("srvOrdId", srvOrdId);
                params.put("srvOrdIdFullCom", srvOrdId);
                params.put("sysResource", sysResFullCom);
                params.put("oneDryValue", oneDryValue);
                params.put("srvOrdIdStr", srvOrdIdStr);
                params.put("relateInfoId", relateInfoId);

                //调单入库
                try {
                    if ("submit".equals(action) && params.containsKey("dispatchOrderData")
                            && params.containsKey("disOrdFlag")) {
                        logger.info("1.先调单入库。。。。。。。。。。。。。。。。。。。。");
                        String dispatchOrderId = orderDealServiceIntf.insertDispatchOrder(params);
                        logger.info(">>>>>调单id>>>>>>>>>>>>>>>>>>dispatchOrderId:" + dispatchOrderId);
                        uploadMap.put("dispatchOrderId", dispatchOrderId);
                    } else {
                        uploadMap.put("dispatchOrderId", "");
                    }
                } catch (Exception e) {
                    resMap.put("success", false);
                    resMap.put("message", "派单失败!保存调单信息失败：" + e.getMessage());
                    throw new Exception(MapUtils.getString(resMap, "message"));
                }
                // 派单成功后进行附件上传
                logger.info("2.再上传附件。。。。。。。。。。。。。。。。。。。。");
                uploadMap.put("woId", woId);
                uploadMap.put("srvOrdId", srvOrdId);
                uploadMap.put("orderId", orderId);
                uploadMap.put("tacheId", tacheId);
                uploadMap.put("staffId", operStaffId);
                uploadMap.put("ctxPath", ctxPath);
                uploadMap.put("origin", origin);
                uploadMap.put("delFiles", delFiles);
                Map<String, Object> returnMap = orderStandbyServiceIntf.uploadFiles(uploadMap, multiFileMap);
                if (MapUtils.getBoolean(returnMap, "flag")) {
                    logger.info("附件上传成功。。。。。。。。。。");
                } else {
                    logger.info("附件上传失败。。。。。。。。。。");
                    resMap.put("success", false);
                    resMap.put("message", "派单失败!附件上传失败：");
                    throw new Exception(MapUtils.getString(resMap, "message"));
                }
                logger.info("3.最后回单。。。。。。。。。。。。。。。。。。。。");
                if ("submit".equals(action)) {
                    resMap = orderDealServiceIntf.sendWoOrder(params);
                } else if ("trans".equals(action)) {
                    resMap = orderDealServiceIntf.transferWoOrder(params);
                } else if ("rollBackOrder".equals(action)) {
                    resMap = orderDealServiceIntf.rollBackWoOrder(params);
                } else if ("goBackOrder".equals(action)) {
                    resMap = orderDealServiceIntf.goBackOrder(params);
                } else if ("sendEngineering".equals(action)) {
                    // 下发工建系统,调用前评估接口
                   // resMap = orderDealServiceIntf.goBackOrder(params);
                }else if ("postponementApply".equals(action)) {
                    // 延期申请 调用一干接口 修改附件以及申请状态
                     params.put("woId", applyWoId);
                     resMap = orderDealServiceIntf.feedBackToOneDry(params);
                }else if("postponementApplyBatch".equals(action)){
                    //批量延期申请
                    params.put("woId", applyWoId);
                    resMap = orderDealServiceIntf.feedBackToOneDryBatch(params);
                }

                Boolean success = MapUtils.getBoolean(resMap, "success");
                if (success) {
                    resMap.put("success", true);
                    orderIdList.add(orderId);
                } else {
                    throw new Exception(MapUtils.getString(resMap, "message"));
                }
                logger.info(">>>>>>>>>>>>>>>>>>>>>>>工单流转结束");
            }
            if (!ListUtil.isEmpty(orderIdList)){
                // 查询当前操作用户的所属区域，并查询其区域需不要发送短信
                logger.info("开始准备发送短信。。。。。。。。。。。。。。。。。。。。");
                Map<String, Object> sendMsgMap = new HashMap<String, Object>();
                sendMsgMap.put("operStaffId", operStaffId);
                sendMsgMap.put("orderIdList", orderIdList);
                sendMsgMap.put("operAction", action);
                orderSendMsgService.sendMsgBefore(sendMsgMap);
            }
            /*String areaId = MapUtils.getString(operStaffInfoMap, "AREA_ID");
            Map<String, Object> msmSwitchMap = orderDealServiceIntf.qryMsmSwitchByArea(areaId);
            // ISSEND等于1发送短信
            if ("1".equals(MapUtils.getString(msmSwitchMap, "ISSEND"))) {
                // 查询工单列表发送短信
                logger.info("发送短信。。。。。。。。。。。。。。。。。。。。");
                orderDealServiceIntf.qryUserObjByWoIdsSendMsg(woIdList);
            }*/

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("OrderFinishedEvent updateOrderAttr failed" + e.getMessage());
            //TransactionAspectSupport.currentTransactionStatus().setRollbackOnly(); // 手动开启事务回滚
        } finally {
            JSONObject jsonObject = JSONObject.fromObject(resMap);
            response.setCharacterEncoding("GBK");
            response.setHeader("Cache-Control", "no-cache");
            response.setContentType("text/html;charset=UTF-8");
            try {
                writer = response.getWriter();
                writer.write(jsonObject.toString());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                logger.error("{}", e);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 只上传附件，不提交工单,
     * 核查流程需要保存AZ端接入方案描述
     * @param request
     * @param response
     */
    @RequestMapping("/uploadFilesNoSubmit.spr")
    @PublicServ
    public void uploadFilesNoSubmit(HttpServletRequest request, HttpServletResponse response) {

        logger.info("上传附件。。。。。。。。。。。。。。。。。。。。");
        PrintWriter writer = null;
        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        Map<String, Object> resMap = new HashMap<String, Object>();
        try {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            MultiValueMap<String, MultipartFile> multiFileMap = multipartRequest.getMultiFileMap();
            String ctxPath = request.getSession().getServletContext().getRealPath("/") + File.separator + "upload";
            String paramStr = request.getParameter("params");
            JSONObject jasonObject = JSONObject.fromObject(paramStr);
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>接收数据参数：" + jasonObject);
            Map params = (Map) jasonObject;
            String delFiles = MapUtils.getString(params, "delFiles");
            String circuitDataStr = MapUtils.getString(params, "circuitData");

            List<Object> circuitDatalist = JSON.parseArray(circuitDataStr);
            for (Object object : circuitDatalist) {
                Map<String, Object> uploadMap = new HashMap();
                Map<String, Object> circuitDataMap = (Map<String, Object>) object;
                String woId = MapUtils.getString(circuitDataMap, "WO_ID");
                String orderId = MapUtils.getString(circuitDataMap, "ORDER_ID");
                String tacheId = MapUtils.getString(circuitDataMap, "TACHE_ID");
                String srvOrdId = MapUtils.getString(circuitDataMap, "SRV_ORD_ID");

                // 进行附件上传
                logger.info("上传附件。。。。。。。。。。。。。。。。。。。。");
                uploadMap.put("woId", woId);
                uploadMap.put("srvOrdId", srvOrdId);
                uploadMap.put("orderId", orderId);
                uploadMap.put("tacheId", tacheId);
                uploadMap.put("staffId", operStaffId);
                uploadMap.put("ctxPath", ctxPath);
                uploadMap.put("origin", "HJ-CHECK");//-核查流程的环节，核查标准化需要
                uploadMap.put("delFiles", delFiles);
                Map<String, Object> returnMap = orderStandbyServiceIntf.uploadFiles(uploadMap, multiFileMap);
                if (MapUtils.getBoolean(returnMap, "flag")) {
                    logger.info("附件上传成功。。。。。。。。。。");
                } else {
                    logger.info("附件上传失败。。。。。。。。。。");
                    resMap.put("success", false);
                    resMap.put("message", "派单失败!附件上传失败：");
                    throw new Exception(MapUtils.getString(resMap, "message"));
                }

            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("OrderFinishedEvent updateOrderAttr failed" + e.getMessage());
            //TransactionAspectSupport.currentTransactionStatus().setRollbackOnly(); // 手动开启事务回滚
        } finally {
            JSONObject jsonObject = JSONObject.fromObject(resMap);
            response.setCharacterEncoding("GBK");
            response.setHeader("Cache-Control", "no-cache");
            response.setContentType("text/html;charset=UTF-8");
            try {
                writer = response.getWriter();
                writer.write(jsonObject.toString());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                logger.error("{}", e);
                throw new RuntimeException(e);
            }
        }
    }
}
