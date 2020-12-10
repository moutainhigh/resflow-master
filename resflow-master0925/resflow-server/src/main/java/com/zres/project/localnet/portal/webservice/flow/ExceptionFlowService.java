package com.zres.project.localnet.portal.webservice.flow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.orderAbnormal.constant.OrderAbnormalConstant;
import com.zres.project.localnet.portal.orderAbnormal.dao.OrderAbnormalDao;
import com.zres.project.localnet.portal.util.MapConverterUtil;
import com.zres.project.localnet.portal.util.OrderTrackOperType;
import com.zres.project.localnet.portal.webservice.data.dao.ExceptionFlowDao;
import com.zres.project.localnet.portal.webservice.dto.CustomerInfoDTO;
import com.zres.project.localnet.portal.webservice.dto.DispatchInfoDTO;
import com.zres.project.localnet.portal.webservice.dto.JiKeCustomInfoDTO;
import com.zres.project.localnet.portal.webservice.dto.JiKeProdAttrDTO;
import com.zres.project.localnet.portal.webservice.dto.JiKeProdInfoDTO;
import com.zres.project.localnet.portal.webservice.dto.ProdAttrDTO;
import com.zres.project.localnet.portal.webservice.dto.ProdInfoDTO;
import com.zres.project.localnet.portal.webservice.interfaceJiKe.FinishOrderServiceIntf;
import com.zres.project.localnet.portal.webservice.oneDry.GetContentDto;

import com.ztesoft.res.frame.core.util.MapUtil;
import com.ztesoft.res.frame.flow.common.dto.FlowOrderDTO;
import com.ztesoft.res.frame.flow.common.dto.FlowOrderSpecDTO;
import com.ztesoft.res.frame.flow.common.service.FlowActionHandler;
import com.ztesoft.res.frame.flow.task.service.PubVal;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import com.ztesoft.res.frame.user.inf.UserInfo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * @Description:异常单流程接口实现类
 * @Author:zhang.kaigang
 * @Date:2019/5/16 16:08
 * @Version:1.0
 */
@Service
public class ExceptionFlowService implements ExceptionFlowIntf {

    private static Logger logger = LoggerFactory.getLogger(ExceptionFlowService.class);

    @Autowired
    private FlowActionHandler flowActionHandler;

    @Autowired
    private ExceptionFlowDao exceptionFlowDao;

    @Autowired
    private ExceptionChangeService exceptionChangeService;

    @Autowired
    private FinishOrderServiceIntf finishOrderServiceIntf;

    @Autowired
    private OrderDealDao orderDealDao;

    @Autowired
    private OrderAbnormalDao orderAbnormalDao;


    private static final String CRM_PROD_INST_ID = "CRM_PROD_INST_ID";
    private static final String CST_ORD_ID = "CST_ORD_ID";
    private static final String OPER_STAFF_ID = "11";
    private static final String MAX_VERSION = "MAX_VERSION";
    private static final String FILED_TYPE = "FILED_TYPE";
    private static final String SRV_ORD_ID = "SRV_ORD_ID";
    private static final String CUSTOMER_INFO = "CustomerInfo";
    private static final String DISPATCH_INFO = "DispatchInfo";
    private static final String PROD_INFO = "ProdInfo";
    private static final String SUCCESS = "success";
    private static final String MESSAGE = "message";
    private static final String ATTRCODE = "applydatea,applydatez,rfsdate";

    private static Pattern upperPattern = Pattern.compile("[A-Z_]*");

    // 返回错误消息
    String message = "调用异常单接口失败";



    /**
     * 异常单接口
     * @param type 异常单类型：追单、挂起、解挂等，在一干用104-追单 108-加急 109-延期 110-挂起 111-解挂 112撤单作废
     * @param newCustomerInfoDTO 发起异常单新传递的客户信息
     * @param newDispatchInfoDTO 发起异常单新传递的调单信息
     * @param newProdInfoDTOList 发起异常单新传递的电路信息
     */
    @Override
    public Map<String, Object> exceptionFlowChange(String type, CustomerInfoDTO newCustomerInfoDTO, DispatchInfoDTO newDispatchInfoDTO, List<ProdInfoDTO> newProdInfoDTOList) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            String cstOrdId = "";
            String chgOrdId = "";
            Map<String, String> srcOrdIdsMap = new HashMap<>();
            Map<String, String> chgOrdIdsMap = new HashMap<>();
            if (ExceptionChangeService.EXCEPTION_104.equals(type)) {
                // 追单 1.将选择的电路定单挂起 2.启动异常单  3.将变更差异值存入数据表
                // 1.挂起定单  电路维度查询出电路信息得到定单id进行挂起
                dealOrder(type, newProdInfoDTOList, newCustomerInfoDTO, srcOrdIdsMap, chgOrdIdsMap);
                cstOrdId = newProdInfoDTOList.get(0).getCstOrdId();
                // 随便取一条异常单的orderId，给客户信息和调单信息变更值赋值
                chgOrdId =  newProdInfoDTOList.get(0).getChgOrderId();

                message = "变更差异值存入数据库失败";
                // 3.将变更差异值存入数据库
                // 3.1先查询版本号是否存在，如果不存在则默认1， 存在则加1
                Map<String, Object> tempMap = exceptionFlowDao.queryMaxChangeVersion(type, cstOrdId);
                int maxVersion = MapUtils.getIntValue(tempMap, MAX_VERSION, 0);
                message = "电路信息变更差异值存入数据库失败";
                // 3.3 得到原有电路信息， 比较电路信息差异值，存入数据表
                List<ProdInfoDTO> oldProdInfoDTOListBasic =  exceptionFlowDao.queryProdInfoDTOListByCstOrdId(cstOrdId);
                List<ProdInfoDTO> oldProdInfoDTOList = queryProdInfoDTOList(oldProdInfoDTOListBasic);
                boolean typeFlag = insertChangeProdInfoMap(newProdInfoDTOList, oldProdInfoDTOList, cstOrdId, srcOrdIdsMap,  chgOrdIdsMap, type, maxVersion);
                if (typeFlag){ //如果这里返回true就说明是追单延期，修改类型为延期
                    type = "109";
                }
                message = "客户信息变更差异值存入数据库失败";
                // 3.2得到原有客户订单信息， 比较客户信息差异值，存入数据表
                Map<String, String> changeCustomerInfoMap = changeCustomerInfoMap(newCustomerInfoDTO, cstOrdId);
                if(!"".equals(MapUtils.getString(changeCustomerInfoMap,"changeMessage"))&& MapUtils.getString(changeCustomerInfoMap,"changeMessage")!= null){
                    insertChangeInfo(cstOrdId, "", chgOrdId, type, CUSTOMER_INFO, maxVersion, changeCustomerInfoMap);
                }
                // 3.4单条调单信息差异值，如果是多条则需要写在3.3电路里面
                Map<String, String>  changeDispatchInfoMap  = changeDispatchInfoMap(newDispatchInfoDTO, oldProdInfoDTOList);
                if(!"".equals(MapUtils.getString(changeDispatchInfoMap,"changeMessage"))&& MapUtils.getString(changeDispatchInfoMap,"changeMessage")!= null){
                    insertChangeInfo(cstOrdId, "", chgOrdId, type, DISPATCH_INFO, maxVersion, changeDispatchInfoMap);
                }
            }
            else if (ExceptionChangeService.EXCEPTION_108.equals(type) || ExceptionChangeService.EXCEPTION_109.equals(type)) {
                // 加急或者延期
                // 处理一些信息,比如cstOrdId或者newProdInfoDTOList加入srvOrdId
                dealOrder(type, newProdInfoDTOList, null, srcOrdIdsMap, chgOrdIdsMap);
                cstOrdId = newProdInfoDTOList.get(0).getCstOrdId();

                // 2.存入变更电路的属性信息，主要是涉及到的时间如完成时间等字段
                urgentOrder(cstOrdId, newProdInfoDTOList, type, chgOrdIdsMap);

            }
            else if (ExceptionChangeService.EXCEPTION_110.equals(type)) {
                // 挂起
                dealOrder(type, newProdInfoDTOList, null, srcOrdIdsMap, chgOrdIdsMap);
            }
            else if (ExceptionChangeService.EXCEPTION_111.equals(type)) {
                // 解挂
                dealOrder(type, newProdInfoDTOList, null,  srcOrdIdsMap, chgOrdIdsMap);
            }
            else if (ExceptionChangeService.EXCEPTION_112.equals(type)) {
                // 撤单
                dealOrder(type, newProdInfoDTOList, null,  srcOrdIdsMap, chgOrdIdsMap);
            }
            resultMap.put(SUCCESS, true);
        }
        catch (Exception e) {
            resultMap.put(SUCCESS, false);
            resultMap.put(MESSAGE, message);
            logger.error(e.getMessage(), e);
            // 手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return resultMap;
    }


    /**
     * 处理定单操作
     * @param type
     * @param newProdInfoDTOList
     * @param newCustomerInfoDTO
     * @param srcOrdIdsMap
     */
    private void dealOrder(String type, List<ProdInfoDTO> newProdInfoDTOList,
                           CustomerInfoDTO newCustomerInfoDTO, Map<String, String> srcOrdIdsMap,
                           Map<String, String> chgOrdIdsMap) {
        for (ProdInfoDTO newProdInfoDTO : newProdInfoDTOList) {
            List<ProdAttrDTO> newProdAttrDTOList = newProdInfoDTO.getProdAttrDTOList();
            String crmProdInstId = "";
            String actType = newProdInfoDTO.getActType();
            String onedryAreaCode = newProdInfoDTO.getOnedryAreaCode();
            String tradeTypeCode = newProdInfoDTO.getTradeTypeCode();
            for (ProdAttrDTO newProdAttrDTO : newProdAttrDTOList) {
                if (CRM_PROD_INST_ID.equalsIgnoreCase(newProdAttrDTO.getCode())) {
                    crmProdInstId = newProdAttrDTO.getValue();
                    break;
                }
            }
            // 正式数据以下根据CRM_PROD_INST_ID只能得到1条数据
            message = "根据入参crmProdInstId" +crmProdInstId+"没有查询到电路信息";
            List<ProdInfoDTO> oldProdInfoDTOListBasic =  exceptionFlowDao.queryProdInfoDTOListByCrmProdInstId(crmProdInstId, actType, onedryAreaCode, tradeTypeCode);
            List<ProdInfoDTO> oldProdInfoDTOList = queryProdInfoDTOList(oldProdInfoDTOListBasic);
            if (oldProdInfoDTOList != null && !oldProdInfoDTOList.isEmpty()) {
                ProdInfoDTO oldProdInfoDTO = oldProdInfoDTOList.get(0);
                // 将原有的值如srvOrdId赋值给新的电路信息，用于后续比较差异值
                String cstOrdId = oldProdInfoDTO.getCstOrdId();
                newProdInfoDTO.setCstOrdId(cstOrdId);
                newProdInfoDTO.setSrvOrdId(oldProdInfoDTO.getSrvOrdId());
                newProdInfoDTO.setDispatchOrderId(oldProdInfoDTO.getDispatchOrderId());

                if (ExceptionChangeService.EXCEPTION_104.equals(type)) {
                    /**
                     * 这里修改了顺序：之前是先对比客户信息后对比电路信息，现在反过来这个顺序
                     */
                    // 追单挂起
                    // 2. 如果客户信息没有改变那么则比较电路信息是否有差异值，有差异值证明该电路被修改，挂起相应的定单
                    Map<String, String> changeProdInfoMapTemp = exceptionChangeService.changeProdInfo(type, oldProdInfoDTO.getSrvOrdId(),
                            newProdInfoDTO, oldProdInfoDTO);
                    String changeDataStr = MapUtils.getString(changeProdInfoMapTemp, ExceptionChangeService.CHANGE_DATA);
                    if (!changeDataStr.isEmpty()) {
                        /**
                         * 对比的差异数据changeProdInfoMapTemp.get(ExceptionChangeService.CHANGE_DATA)
                         *
                         * 如果电路信息只改变了全程要求完成时间，AZ端要求完成时间这三个值中的任何一个就启延期；
                         * 这里string转list，如果list长度大于3就说明改了上述值之外的数据
                         * 小于等于3的时候再遍历，是否含有除上述code之外的code，如果有启追单，如果没有启延期；
                         * 	A端要求完成时间		applydatea
                         * 	Z端要求完成时间		applydatez
                         * 	全程要求完成时间		rfsdate
                         */
                        boolean flag = true; //是否启延期标识
                        changeDataStr = JSON.toJSON("["+ changeDataStr.substring(0,changeDataStr.lastIndexOf(",")) + "]").toString();
                        List<Map> changeDataList =  JSONObject.parseArray(changeDataStr, Map.class);
                        if (changeDataList.size() <= 3){
                            for (Map object : changeDataList){
                                Map<String,Object> changeDataMap =  object;//取出list里面的值转为map
                                String attrCode = MapUtils.getString(changeDataMap, "key");
                                if (ATTRCODE.indexOf(attrCode) == -1){ // 差异key的code如果不包含在ATTRCODE中，则是启追单
                                    flag = false;
                                    break;
                                }
                            }
                        }else {
                            flag = false;
                        }
                        if (flag){
                            //追单延期
                            otherExceptionOrder(srcOrdIdsMap, oldProdInfoDTO.getSrvOrdId(), oldProdInfoDTO.getOrderId(),
                                    chgOrdIdsMap, newProdInfoDTO, null);
                        }else {
                            // 追单的挂起和发起异常单
                            chaseOrder(srcOrdIdsMap, chgOrdIdsMap, oldProdInfoDTO.getSrvOrdId(), oldProdInfoDTO.getOrderId(),
                                    newProdInfoDTO, null);
                        }
                        continue;
                    }
                    /*if (!changeProdInfoMapTemp.get(ExceptionChangeService.CHANGE_DATA).isEmpty()) {
                        // 追单的挂起和发起异常单
                        chaseOrder(srcOrdIdsMap, chgOrdIdsMap, oldProdInfoDTO.getSrvOrdId(), oldProdInfoDTO.getOrderId(),
                                newProdInfoDTO, null);
                    }*/
                    // 1. 先判断客户信息是否被修改，如果客户信息被修改，那么所属电路都进行挂起
                    Map<String, String> changeCustomerInfoMapTemp = changeCustomerInfoMap(newCustomerInfoDTO, cstOrdId);
                    if (!changeCustomerInfoMapTemp.get(ExceptionChangeService.CHANGE_DATA).isEmpty()) {
                        // 追单的挂起和发起异常单
                        chaseOrder(srcOrdIdsMap, chgOrdIdsMap, oldProdInfoDTO.getSrvOrdId(), oldProdInfoDTO.getOrderId(),
                                newProdInfoDTO, null);
                        //continue;
                    }
                }
                else if(ExceptionChangeService.EXCEPTION_108.equals(type) || ExceptionChangeService.EXCEPTION_109.equals(type)){
                    // 加急、延期
                        // 加急、延期发起异常单
                    otherExceptionOrder(srcOrdIdsMap, oldProdInfoDTO.getSrvOrdId(), oldProdInfoDTO.getOrderId(),
                            chgOrdIdsMap, newProdInfoDTO, null);

                }
                else if (ExceptionChangeService.EXCEPTION_110.equals(type)) {
                    // 普通挂起
                    suspendOrder(oldProdInfoDTO.getOrderId(), "一干下发挂起异常单");
                    // 发起异常通知单
                    FlowOrderDTO flowOrderDTO = otherExceptionOrder(srcOrdIdsMap, oldProdInfoDTO.getSrvOrdId(), oldProdInfoDTO.getOrderId(),
                            chgOrdIdsMap, newProdInfoDTO, null);
                    // 存入异常单变更信息表
                    insertChangeInfo(cstOrdId, oldProdInfoDTO.getOrderId(), flowOrderDTO.getOrderId(), type, "", 0, null);
                }
                else if (ExceptionChangeService.EXCEPTION_111.equals(type)) {
                    // 解挂
                    unSuspendOrder(oldProdInfoDTO.getOrderId(), "一干下发解挂异常单");
                    // 发起异常通知单
                    FlowOrderDTO flowOrderDTO = otherExceptionOrder(srcOrdIdsMap, oldProdInfoDTO.getSrvOrdId(), oldProdInfoDTO.getOrderId(),
                            chgOrdIdsMap, newProdInfoDTO, null);
                    // 存入异常单变更信息表
                    insertChangeInfo(cstOrdId, oldProdInfoDTO.getOrderId(), flowOrderDTO.getOrderId(), type, "", 0, null);
                }
                else if (ExceptionChangeService.EXCEPTION_112.equals(type)) {
                    boolean flag = true;
                    // 撤单
                    String orderType = oldProdInfoDTO.getOrderType();
                    if ("102".equals(orderType)){ //核查单撤单手动修改定单和工单状态
                        exceptionFlowDao.updStateCancelOrder(oldProdInfoDTO.getOrderId());
                        flag = false;
                    }
                    cancelOrder(oldProdInfoDTO.getOrderId(), flag);
                    /*// 撤单
                    cancelOrder(oldProdInfoDTO.getOrderId());*/
                    // 发起异常通知单
                    FlowOrderDTO flowOrderDTO = otherExceptionOrder(srcOrdIdsMap, oldProdInfoDTO.getSrvOrdId(), oldProdInfoDTO.getOrderId(),
                            chgOrdIdsMap, newProdInfoDTO, null);
                    // 存入异常单变更信息表
                    insertChangeInfo(cstOrdId, oldProdInfoDTO.getOrderId(), flowOrderDTO.getOrderId(), type, "", 0, null);
                    //modify by wang.gang2   需要先生成异常单再修改srv_ord_sta
                    exceptionFlowDao.updateSrvOrdState(oldProdInfoDTO.getSrvOrdId(), "10X");

                }
            }
        }
    }

    /**
     * 追单的挂起和发起异常单
     * @param srcOrdIdsMap
     * @param chgOrdIdsMap
     * @param srvOrdId
     * @param srcOrdId
     * @param newProdInfoDTO 一干用
     * @param newJiKeProdInfoDTO 集客用
     */
    private void chaseOrder(Map<String, String> srcOrdIdsMap, Map<String, String> chgOrdIdsMap,
                            String srvOrdId, String srcOrdId, ProdInfoDTO newProdInfoDTO,
                            JiKeProdInfoDTO newJiKeProdInfoDTO){
        srcOrdIdsMap.put(srvOrdId, srcOrdId);
        FlowOrderDTO flowOrderDTOTemp = getFlowOrderDTO(srcOrdId);
        message = "启动异常单失败";
        FlowOrderDTO flowOrderDTO = flowActionHandler.createOrder(OPER_STAFF_ID, flowOrderDTOTemp);
        /*FlowOrderDTO flowOrderDTO = new FlowOrderDTO();
        flowOrderDTO.setOrderId("1000");*/
        chgOrdIdsMap.put(srvOrdId, flowOrderDTO.getOrderId());
        if(newProdInfoDTO != null){
            newProdInfoDTO.setChgOrderId(flowOrderDTO.getOrderId());
        }
        if(newJiKeProdInfoDTO != null){
            newJiKeProdInfoDTO.setChgOrderId(flowOrderDTO.getOrderId());
        }
    }

    /**
     * 除追单外的接口发起异常单
     * @param srcOrdIdsMap
     * @param srvOrdId
     * @param srcOrdId
     * @param chgOrdIdsMap
     * @param newProdInfoDTO 一干
     * @param newJiKeProdInfoDTO 集客
     * @return
     */
    private FlowOrderDTO otherExceptionOrder(Map<String, String> srcOrdIdsMap, String srvOrdId, String srcOrdId,
                                             Map<String, String> chgOrdIdsMap, ProdInfoDTO newProdInfoDTO, JiKeProdInfoDTO newJiKeProdInfoDTO){
        srcOrdIdsMap.put(srvOrdId, srcOrdId);
        //  启动异常单
        FlowOrderDTO flowOrderDTOTemp = getFlowOrderDTO2(srcOrdId);
        // TODO
        message = "启动异常单失败";
        FlowOrderDTO flowOrderDTO = flowActionHandler.createOrder(OPER_STAFF_ID, flowOrderDTOTemp);
        /*FlowOrderDTO flowOrderDTO = new FlowOrderDTO();
        flowOrderDTO.setOrderId("2000");*/
        chgOrdIdsMap.put(srvOrdId, flowOrderDTO.getOrderId());
        if(newProdInfoDTO != null){
            newProdInfoDTO.setChgOrderId(flowOrderDTO.getOrderId());
        }
        if(newJiKeProdInfoDTO != null){
            newJiKeProdInfoDTO.setChgOrderId(flowOrderDTO.getOrderId());
        }
        return flowOrderDTO;
    }

    /**
     * 通过基础信息得到属性信息
     * @param oldProdInfoDTOListBasic
     * @return
     */
    private List<ProdInfoDTO> queryProdInfoDTOList(List<ProdInfoDTO> oldProdInfoDTOListBasic) {
        List<ProdInfoDTO> oldProdInfoDTOList = new ArrayList<>();
        if (oldProdInfoDTOListBasic != null && !oldProdInfoDTOListBasic.isEmpty()) {
            for (ProdInfoDTO oldProdInfoDTO : oldProdInfoDTOListBasic) {
                List<ProdAttrDTO> prodAttrDTOList = exceptionFlowDao.queryProdAttrDTOList(oldProdInfoDTO.getSrvOrdId());
                oldProdInfoDTO.setProdAttrDTOList(prodAttrDTOList);
                oldProdInfoDTOList.add(oldProdInfoDTO);
            }
        }
        return oldProdInfoDTOList;
    }

    /**
     * 追单异常单FlowOrderDTO对象追单得根据原单流程走
     * @return
     */
    private FlowOrderDTO getFlowOrderDTO(String srcOrdId) {
        Map<String, String> specMap = exceptionFlowDao.getOrderSpec(srcOrdId);
        FlowOrderDTO flowOrderDTO = new FlowOrderDTO();
        FlowOrderSpecDTO specDTO = new FlowOrderSpecDTO();
        // 异常单定单规格
        specDTO.setActType(specMap.get("ACT_TYPE"));
        specDTO.setObjType(specMap.get("OBJ_TYPE"));
        specDTO.setOrderType(OrderAbnormalConstant.EXCEPTION_ORDER_TYPE);
        flowOrderDTO.setOrderSpec(specDTO);
        flowOrderDTO.setAreaId(specMap.get("AREA_ID"));
        // 原单的定单id放入异常的定单属性里，不然派发规则取不到映射
        List<HashMap<String, String>> orderAttrs = getOrderAttrs(srcOrdId);
        flowOrderDTO.setOrderAttrs(orderAttrs);
        return flowOrderDTO;
    }

    /**
     * 追单外的异常单FlowOrderDTO对象
     * @return
     */
    private FlowOrderDTO getFlowOrderDTO2(String srcOrdId) {
        FlowOrderDTO flowOrderDTO = new FlowOrderDTO();
        FlowOrderSpecDTO specDTO = new FlowOrderSpecDTO();
        // 异常单定单规格
        specDTO.setActType("EXCEPTION_ORDER_ACT");
        specDTO.setObjType("EXCEPTION_ORDER_OBJ");
        specDTO.setOrderType("ORDER_EXCEPTION");
        flowOrderDTO.setOrderSpec(specDTO);
        // 目前配置的是全国
        flowOrderDTO.setAreaId("350002000000000042766408");
        // 原单的定单id放入异常的定单属性里，不然派发规则取不到映射
        List<HashMap<String, String>> orderAttrs = getOrderAttrs(srcOrdId);
        flowOrderDTO.setOrderAttrs(orderAttrs);
        return flowOrderDTO;
    }

    /**
     * //原单的定单id放入异常的定单属性里，不然派发规则取不到映射
     * @param srcOrdId
     * @return
     */
    private List<HashMap<String, String>> getOrderAttrs(String srcOrdId){
        List<HashMap<String, String>> orderAttrs = new ArrayList<>();
        HashMap<String, String> orderAttr = new HashMap<>();
        orderAttr.put("KEY", "src_order_id");
        orderAttr.put("VALUE", srcOrdId);
        orderAttrs.add(orderAttr);
        return orderAttrs;
    }

    /**
     * 处理加急或者延期变更差异值
     * @param cstOrdId
     * @param newProdInfoDTOList
     * @param type
     * @param chgOrdIdsMap
     */
    private void urgentOrder(String cstOrdId, List<ProdInfoDTO> newProdInfoDTOList, String type, Map<String, String> chgOrdIdsMap) {
        List<ProdInfoDTO> oldProdInfoDTOListBasic =  exceptionFlowDao.queryProdInfoDTOListByCstOrdId(cstOrdId);
        List<ProdInfoDTO> oldProdInfoDTOList = queryProdInfoDTOList(oldProdInfoDTOListBasic);
        for (ProdInfoDTO newProdInfoDTO : newProdInfoDTOList) {
            if (oldProdInfoDTOList != null && !oldProdInfoDTOList.isEmpty()) {
                for (ProdInfoDTO oldProdInfoDTO : oldProdInfoDTOList) {
                    // 判断是同一条电路，才进行属性比较
                    if (oldProdInfoDTO.getSrvOrdId().equals(newProdInfoDTO.getSrvOrdId())) {
                        String chgOrderId = chgOrdIdsMap.get(oldProdInfoDTO.getSrvOrdId());
                        List<ProdAttrDTO> newProdAttrDTOList = newProdInfoDTO.getProdAttrDTOList();
                        // 此处处理了属性信息的转储和更新，并且得到差异值集合
                        Map<String, String> changeProdAttrMap = exceptionChangeService.changeProdAttrContent(type, oldProdInfoDTO.getSrvOrdId(), newProdAttrDTOList);
                        insertChangeInfo(cstOrdId, oldProdInfoDTO.getOrderId(), chgOrderId, type, "", 0, changeProdAttrMap);
                    }
                }
            }
        }
    }

    /**
     * 比较客户订单信息
     * @param newCustomerInfoDTO
     * @param cstOrdId
     * @return
     */
    private Map<String, String> changeCustomerInfoMap(CustomerInfoDTO newCustomerInfoDTO, String cstOrdId) {
        CustomerInfoDTO oldCustomerInfoDTO = exceptionFlowDao.queryCustomerInfoDTO(cstOrdId);
        if (oldCustomerInfoDTO == null) {
            oldCustomerInfoDTO = new CustomerInfoDTO();
        }
        Map<String, String> changeCustomerInfoMap = exceptionChangeService.changeContent(newCustomerInfoDTO, oldCustomerInfoDTO);
        return changeCustomerInfoMap;
    }

    /**
     * 比较电路信息差异值并存入数据表
     * @param newProdInfoDTOList
     * @param oldProdInfoDTOList
     * @param cstOrdId
     * @param srcOrdIdsMap
     * @param chgOrdIdsMap
     * @param type
     * @param maxVersion
     */
    private boolean insertChangeProdInfoMap(List<ProdInfoDTO> newProdInfoDTOList, List<ProdInfoDTO> oldProdInfoDTOList,
                                         String cstOrdId, Map<String, String> srcOrdIdsMap,
                                         Map<String, String> chgOrdIdsMap,
                                         String type, int maxVersion) {
        boolean flag = true; //是否启延期标识
        for (ProdInfoDTO newProdInfoDTO : newProdInfoDTOList) {
            if (oldProdInfoDTOList != null && !oldProdInfoDTOList.isEmpty()) {
                for (ProdInfoDTO oldProdInfoDTO : oldProdInfoDTOList) {
                    // 如果是电路列表则需要保证newProdInfoDTO有srvOrdId值
                    if (oldProdInfoDTO.getSrvOrdId().equals(newProdInfoDTO.getSrvOrdId())) {
                        Map<String, String> changeProdInfoMap = exceptionChangeService.changeProdInfo(type, oldProdInfoDTO.getSrvOrdId(),
                                newProdInfoDTO, oldProdInfoDTO);
                        String srcOrdId = srcOrdIdsMap.get(oldProdInfoDTO.getSrvOrdId());
                        String chgOrderId = chgOrdIdsMap.get(oldProdInfoDTO.getSrvOrdId());
                        /**
                         * 解析遍历电路属性：
                         * 如果电路属性只修改了全程要求完成时间，az端要求完成时间则流程为延期单，直接将数据覆盖到原单
                         */
                        String changeDataStr = MapUtils.getString(changeProdInfoMap, ExceptionChangeService.CHANGE_DATA);
                        if (!changeDataStr.isEmpty()) {
                            changeDataStr = JSON.toJSON("["+ changeDataStr.substring(0,changeDataStr.lastIndexOf(",")) + "]").toString();
                            List<Map> changeDataList =  JSONObject.parseArray(changeDataStr, Map.class);
                            if (changeDataList.size() <= 3){
                                for (Map object : changeDataList){
                                    Map<String,Object> changeDataMap =  object;//取出list里面的值转为map
                                    String attrCode = MapUtils.getString(changeDataMap, "key");
                                    if (ATTRCODE.indexOf(attrCode) == -1){ // 差异key的code如果不包含在ATTRCODE中，则是启追单
                                        flag = false;
                                        break;
                                    }
                                }
                            }else {
                                flag = false;
                            }
                            if (flag){
                                for (Map object : changeDataList){
                                    Map<String,Object> changeDataMap =  object;//取出list里面的值转为map
                                    String newAttrValueName = MapUtils.getString(changeDataMap, "key");
                                    String newValue = MapUtils.getString(changeDataMap, "newValue");
                                    // 查询历史表有数据就不做操作，没数据则插入
                                    int count = exceptionFlowDao.queryProdAttrHis(oldProdInfoDTO.getSrvOrdId(), newAttrValueName);
                                    if (count == 0) {
                                        exceptionFlowDao.insertProdAttrHis(oldProdInfoDTO.getSrvOrdId(), newAttrValueName);
                                    }
                                    // 加急延期-查看是否有传递要求完成时间，如果有则更新gom_order表
                                    if("rfsdate".equals(newAttrValueName) && StringUtils.isNotEmpty(newValue)){
                                        exceptionFlowDao.updateOrderReqFinDate(oldProdInfoDTO.getSrvOrdId(),  newValue);
                                    }
                                    exceptionFlowDao.updateProdAttr(oldProdInfoDTO.getSrvOrdId(), newAttrValueName, newValue);
                                }

                            }
                        }
                        if (flag){
                            type = "109";
                            insertChangeInfo(cstOrdId, srcOrdId, chgOrderId, type, "", maxVersion, changeProdInfoMap);
                        }else {
                            insertChangeInfo(cstOrdId, srcOrdId, chgOrderId, type, PROD_INFO, maxVersion, changeProdInfoMap);
                        }
                    }
                    // TODO 如果调单信息是列表的话需要保证newProdInfoDTO有dispatchOrderId值
                }
            }
            else {
                flag = false;
                ProdInfoDTO oldProdInfoDTO = new ProdInfoDTO();
                Map<String, String> changeProdInfoMap = exceptionChangeService.changeProdInfo(type, newProdInfoDTO.getSrvOrdId(),
                        newProdInfoDTO, oldProdInfoDTO);
                insertChangeInfo(cstOrdId, "", "", type, PROD_INFO, maxVersion, changeProdInfoMap);
            }
        }
        return flag;
    }

    /**
     * 比较调单信息的差异
     * @param newDispatchInfoDTO
     * @param oldProdInfoDTOList
     * @return
     */
    private Map<String, String> changeDispatchInfoMap(DispatchInfoDTO newDispatchInfoDTO, List<ProdInfoDTO> oldProdInfoDTOList) {
        Map<String, String>  changeDispatchInfoMap;
        if (oldProdInfoDTOList != null && !oldProdInfoDTOList.isEmpty()) {
            String dispatchOrderId = oldProdInfoDTOList.get(0).getDispatchOrderId();
            DispatchInfoDTO oldDispatchInfoDTO = exceptionFlowDao.queryDispatchInfoDTO(dispatchOrderId);
            if (oldDispatchInfoDTO == null) {
                oldDispatchInfoDTO = new DispatchInfoDTO();
            }
            changeDispatchInfoMap = exceptionChangeService.changeContent(newDispatchInfoDTO, oldDispatchInfoDTO);
        }
        else {
            DispatchInfoDTO oldDispatchInfoDTO = new DispatchInfoDTO();
            changeDispatchInfoMap = exceptionChangeService.changeContent(newDispatchInfoDTO, oldDispatchInfoDTO);
        }
        return changeDispatchInfoMap;
    }

    /**
     * 存入变更信息表
     * @param cstOrdId
     *  @param srcOrdId
     * @param orderId
     * @param chgTypeTemp
     * @param filedType
     * @param changeInfoMap
     */
    private void insertChangeInfo(String cstOrdId, String srcOrdId, String orderId, String chgTypeTemp, String filedType, int maxVersion, Map<String, String> changeInfoMap) {
        // 将集客的类型进行转换成通用的
        String chgType = chgTypeTemp;
        if(ExceptionChangeService.EXCEPTION_4A.equals(chgTypeTemp)){
            // 追单
            chgType = ExceptionChangeService.EXCEPTION_104;
        }
        if(ExceptionChangeService.EXCEPTION_4B.equals(chgTypeTemp)){
            // 加急
            chgType = ExceptionChangeService.EXCEPTION_108;
        }
        if(ExceptionChangeService.EXCEPTION_4C.equals(chgTypeTemp)){
            // 延期
            chgType = ExceptionChangeService.EXCEPTION_109;
        }
        if(ExceptionChangeService.EXCEPTION_4E.equals(chgTypeTemp)){
            // 挂起
            chgType = ExceptionChangeService.EXCEPTION_110;
        }
        if(ExceptionChangeService.EXCEPTION_4F.equals(chgTypeTemp)){
            // 解挂
            chgType = ExceptionChangeService.EXCEPTION_111;
        }
        if(ExceptionChangeService.EXCEPTION_4D.equals(chgTypeTemp)){
            // 撤单
            chgType = ExceptionChangeService.EXCEPTION_112;
        }
        Map<String, Object> map  = new HashMap();
        map.put("SRC_CST_ORDER_ID", cstOrdId);
        map.put("SRC_ORDER_ID", srcOrdId);
        map.put("CHG_ORDER_ID", orderId);
        map.put("CHG_TYPE", chgType);
        map.put("FILED_TYPE", filedType);
        map.put("CHG_VERSION", ++maxVersion);
        if(ExceptionChangeService.EXCEPTION_104.equals(chgType) ||
                ExceptionChangeService.EXCEPTION_108.equals(chgType) ||
                ExceptionChangeService.EXCEPTION_109.equals(chgType) ) {
            // 追单、加急、延期
            // 有差异值才存入数据库，没差异值不存入
            if (!changeInfoMap.get(ExceptionChangeService.CHANGE_DATA).isEmpty()) {
                map.put("CHANGE_DATA", "[" + changeInfoMap.get(ExceptionChangeService.CHANGE_DATA) + "]");
                map.put("CHANGE_MESSAGE", "[" + changeInfoMap.get(ExceptionChangeService.CHANGE_MESSAGE) + "]");
                exceptionFlowDao.insertChangeOrderLog(map);
            }
        }
        else {
            // 挂起、解挂、撤单
            exceptionFlowDao.insertChangeOrderLog(map);
        }

    }


    /**
     * 追单确认:1.转储历史表：客户信息、调单信息、产品信息 2.更新正式表
     * @param cstOrdId
     */
    @Override
    public Map<String, Object> exceptionFlowSure(String cstOrdId) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            // 追单
            String type = ExceptionChangeService.EXCEPTION_104;
            // 1.先得到变更信息的最新版本
            Map<String, Object> tempMap = exceptionFlowDao.queryMaxChangeVersion(type, cstOrdId);
            int maxVersion = MapUtils.getIntValue(tempMap, MAX_VERSION);
            // 2.根据最新版本号得到变更信息集合
            List<Map<String, Object>> mapList = exceptionFlowDao.queryLastChangeOrderLog(type, cstOrdId, String.valueOf(maxVersion));
            if(mapList == null || mapList.isEmpty()){
                // 查询不出数据，追单确认失败
                resultMap.put(SUCCESS, false);
                return resultMap;
            }

            // 根据cstOrdId查询数据来源、srvOrdId
            List<Map<String,Object>> srvInfoList = orderAbnormalDao.qrySrvInfoByCstOrdId(cstOrdId);
            String serviceId= MapUtil.getString(srvInfoList.get(0),"SERVICE_ID");
            // 判断是否是集客来单，注意这个type只作为判断条件不能作为操作数据库所以位置要放正确
            if ("jike".equals(MapUtils.getString(srvInfoList.get(0), "RESOURCES"))) {
                type = ExceptionChangeService.EXCEPTION_4A;
            }

            List<Map<String,Object>> srvList = new ArrayList<>();
            for (Map<String, Object> map : mapList) {
                String filedType = MapUtils.getString(map, FILED_TYPE);
                String srvOrdId = MapUtils.getString(map, SRV_ORD_ID);
                String changeData = MapUtils.getString(map, "CHANGE_DATA");
                if (CUSTOMER_INFO.equals(filedType)) {
                    // 客户信息确认
                    customerInfoSure(type, cstOrdId, changeData);
                }
                if (DISPATCH_INFO.equals(filedType)) {
                    // 调单信息确认
                    dispatchInfoSure(cstOrdId, changeData);
                }
                if (PROD_INFO.equals(filedType)) {
                    // 产品信息确认
                    if(ExceptionChangeService.EXCEPTION_104.equals(type)){
                        prodInfoSure(srvOrdId, changeData);
                    }
                    if(ExceptionChangeService.EXCEPTION_4A.equals(type)){
                        jiKeProdInfoSure(srvOrdId, changeData);
                        // 解析出追单信息用于反馈接口
                        srvList.add(parseChangeData(srvOrdId, changeData));
                    }
                }
            }
            logger.info("----追单确认，调用集客接口start");
            // 判断是否是集客来单
            if ("jike".equals(MapUtils.getString(srvInfoList.get(0), "RESOURCES"))) {
                if(srvList!=null && srvList.size()>0){
                    for(Map<String,Object> tmp : srvList){
                        String srvOrdId = MapUtils.getString(tmp,SRV_ORD_ID,"");
                        String flowId = MapUtils.getString(tmp, "FLOW_ID", "");
                        // 集客来单，追单确认调用反馈接口
                        int num = orderDealDao.qryInterResultBak(srvOrdId, "BackOrder_4A", flowId);
                        if (num < 1){
                            int numFinish = orderDealDao.qryInterResultBak(srvOrdId, "FinishOrder_4A", flowId);
                            if (numFinish < 1) {
//                                Map<String, Object> map = new HashMap<>();
                                tmp.put("srvOrdId",srvOrdId);
                                tmp.put("activeType",ExceptionChangeService.EXCEPTION_4A);// 代表追单
                                tmp.put("serviceId",serviceId);
                                Map finMap = finishOrderServiceIntf.finishOrder(tmp);
                                if (!"1".equals(MapUtils.getString(finMap, "RESP_CODE"))) {
                                    resultMap.put("success", false);
                                    resultMap.put("message","追单确认失败!调用集客反馈接口异常，srvOrdId:" + srvOrdId + " 异常原因：" + MapUtils.getString(finMap, "RESP_DESC"));
                                    return resultMap;
                                }
                            }
                        }else{
                            resultMap.put("success", false);
                            resultMap.put("message","追单驳回已成功，不允许进行追单确认操作！");
                            return resultMap;
                        }
                        // 以电路为维度记录追单确认日志
                        String operType = OrderTrackOperType.OPER_TYPE_1;
                        String message = "追单确认";
                        insertLogInfo(srvOrdId,message,operType);
                    }
                }
            }
            resultMap.put(SUCCESS, true);
            logger.info("----追单确认，调用集客接口end");
        }
        catch (Exception e) {
            resultMap.put(SUCCESS, false);
            logger.error(e.getMessage(), e);
            // 手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return resultMap;
    }
    public Map<String,Object> parseChangeData(String srvOrdId, String changeData){
        Map<String, String> columnParam = MapConverterUtil.convertJsonArrayToMap(changeData);
        Map<String, Object> prodInfoMap = new HashMap<>();
        // 特定的KEY作为基本信息，其他的作为属性信息
        String[] prodInfoArray = {"SERVICE_ID", "TRADE_TYPE_CODE", "ACTIVE_TYPE", "SERVICE_OFFER_ID", "SERIAL_NUMBER",
                "TRADE_ID", "TRADE_ID_RELA", "USER_ID", "FLOW_ID"};
        for (Map.Entry<String, String> entry : columnParam.entrySet()) {
            String mapKey = entry.getKey();
            String mapValue = entry.getValue();
            boolean res = Arrays.asList(prodInfoArray).contains(mapKey);
            if (res) {
                // 结果为true，则表示该值是基本信息
                prodInfoMap.put(mapKey, mapValue);
            }
        }
        prodInfoMap.put(SRV_ORD_ID, srvOrdId);
        return prodInfoMap;
    }

    /**
     *  以电路为维度记录日志
     * @param srvOrdId
     */
    public void insertLogInfo(String srvOrdId,String message,String operType){
        // 根据srvOrdId查询orderid
        Integer userId = 11;
        Map<String,Object>srvInfo = orderDealDao.querySrvInfoBySrvOrdId(srvOrdId);
        UserInfo user = ThreadLocalInfoHolder.getLoginUser();
        if(null != user){
             userId = Integer.valueOf(user.getUserId());
        }
        Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(userId);
        Map<String,Object> operLogMap = new HashMap<>();
        operLogMap.put("orderId", MapUtils.getString(srvInfo,"ORDER_ID",""));
        operLogMap.put("woOrdId", "");
        operLogMap.put("trackOrgId", MapUtils.getString(operStaffInfoMap, "ORG_ID"));
        operLogMap.put("trackOrgName", MapUtils.getString(operStaffInfoMap, "ORG_NAME"));
        operLogMap.put("trackDate", new java.sql.Date(new java.util.Date().getTime()));
        operLogMap.put("createDate", new java.sql.Date(new java.util.Date().getTime()));
        operLogMap.put("trackStaffId", userId);
        String staffName = MapUtils.getString(operStaffInfoMap, "USER_REAL_NAME");
        operLogMap.put("trackStaffName", staffName);
        operLogMap.put("trackStaffPhone", MapUtils.getString(operStaffInfoMap, "USER_PHONE"));
        operLogMap.put("trackStaffEmail", MapUtils.getString(operStaffInfoMap, "USER_EMAIL"));
        operLogMap.put("trackMessage", "[" + staffName + "将电路：" + srvOrdId + "][" + message + "]");
        operLogMap.put("trackContent", "[" + message + "]" );
        operLogMap.put("operType", operType);
        orderDealDao.insertTrackLogInfo(operLogMap);
    }

    /**
     * 客户信息确认
     * @param type 判断是一干还是集客的追单
     * @param cstOrdId
     * @param changeData
     */
    private void customerInfoSure(String type, String cstOrdId, String changeData) {
        //  客户信息 :1、转储(只存初次) 2、更新
        int count = exceptionFlowDao.queryCustomerInfoHis(cstOrdId);
        if (count == 0) {
            if(ExceptionChangeService.EXCEPTION_104.equals(type)){
                // 一干
                exceptionFlowDao.insertCustomerInfoHis(cstOrdId);
            }
            if(ExceptionChangeService.EXCEPTION_4A.equals(type)){
                // 集客
                exceptionFlowDao.insertJiKeCustomerInfoHis(cstOrdId);
            }
        }
        Map<String, String> columnParam = MapConverterUtil.convertJsonArrayToMap(changeData);
        columnParam.put(CST_ORD_ID, cstOrdId);
        exceptionFlowDao.updateCustomerInfo(columnParam);
    }

    /**
     * 调单信息确认
     * @param cstOrdId
     * @param changeData
     */
    private void dispatchInfoSure(String cstOrdId, String changeData) {
        // 调单信息:1、转储(只存初次) 2、更新
        List<ProdInfoDTO> prodInfoDTOList = exceptionFlowDao.queryProdInfoDTOListByCstOrdId(cstOrdId);
        ProdInfoDTO prodInfoDTO = prodInfoDTOList.get(0);
        String dispatchOrderId = prodInfoDTO.getDispatchOrderId();
        int count = exceptionFlowDao.queryDispatchInfoHis(dispatchOrderId);
        if (count == 0) {
            exceptionFlowDao.insertDispatchInfoHis(dispatchOrderId);
        }
        Map<String, String> columnParam = MapConverterUtil.convertJsonArrayToMap(changeData);
        columnParam.put("DISPATCH_ORDER_ID", dispatchOrderId);
        exceptionFlowDao.updateDispatchInfo(columnParam);
    }

    /**
     * 产品信息确认
     * @param srvOrdId
     * @param changeData
     */
    private void prodInfoSure(String srvOrdId, String changeData) {
        // 产品信息: 分为产品基本信息和产品属性信息  1.转储 2.更新
        int count = exceptionFlowDao.queryProdInfoHis(srvOrdId);
        if (count == 0) {
            exceptionFlowDao.insertProdInfoHis(srvOrdId);
        }
        Map<String, String> columnParam = MapConverterUtil.convertJsonArrayToMap(changeData);
        Map<String, String> prodInfoMap = new HashMap<>();
        Map<String, String> prodAttrMap = new HashMap<>();
        // 通过key的大写和下划线来区分是产品信息还是产品属性信息
        for (Map.Entry<String, String> entry : columnParam.entrySet()) {
            String mapKey = entry.getKey();
            String mapValue = entry.getValue();
            Matcher matcher = upperPattern.matcher(mapKey);
            if (matcher.matches()) {
                prodInfoMap.put(mapKey, mapValue);
            }
            else {
                prodAttrMap.put(mapKey, mapValue);
            }
        }
        prodInfoMap.put(SRV_ORD_ID, srvOrdId);
        // 更新产品信息

        if (prodInfoMap.size() > 1) {
            exceptionFlowDao.updateProdInfo(prodInfoMap);
        }

        // 转储产品属性信息，更新或者新增
        for (Map.Entry<String, String> entry : prodAttrMap.entrySet()) {
            String mapKey = entry.getKey();
            String mapValue = entry.getValue();
            int attrCount  =  exceptionFlowDao.queryProdAttrHis(srvOrdId, mapKey);
            if (attrCount == 0) {
                exceptionFlowDao.insertProdAttrHis(srvOrdId, mapKey);
            }
            // 如果是有传递要求完成时间，则更新定单要求完成时间
            if("rfsdate".equals(mapKey) && StringUtils.isNotEmpty(mapValue)){
                exceptionChangeService.updateOrderReqFinDate(srvOrdId, mapValue);
            }
            // 先判断是否有旧的，如果有则更新没有则插入
            // 根据newAttrCode和srvOrdId去查出旧的属性对象，如果没有则需要插入
            List<ProdAttrDTO> oldProdAttrDTOList = exceptionFlowDao.queryProdAttrDTOListByAttrValueName(srvOrdId, mapKey);
            if(oldProdAttrDTOList != null && !oldProdAttrDTOList.isEmpty()){
                exceptionFlowDao.updateProdAttr(srvOrdId, mapKey, mapValue);
            }else{
                // 需要属性做插入
                exceptionFlowDao.insertProdAttrForSure(srvOrdId, mapKey, mapValue);
            }
        }
    }


    /**------------------------------------集客异常单start-------------------------------------------------------------*/

    @Override
    public Map<String, Object> jiKeExceptionFlowChange(String type, JiKeCustomInfoDTO newJiKeCustomInfoDTO,
                                                       List<JiKeProdInfoDTO> newJiKeProdInfoDTOList) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            String cstOrdId = "";
            String chgOrdId = "";
            Map<String, String> srcOrdIdsMap = new HashMap<>();
            Map<String, String> chgOrdIdsMap = new HashMap<>();
            if (ExceptionChangeService.EXCEPTION_4A.equals(type)) {
                // 1.追单:1.将选择的电路定单挂起 2.启动异常单  3.将变更差异值存入数据表
                jiKeDealOrder(type, newJiKeProdInfoDTOList, newJiKeCustomInfoDTO, srcOrdIdsMap, chgOrdIdsMap);
                cstOrdId = newJiKeProdInfoDTOList.get(0).getCstOrdId();
                // 随便取一条异常单的orderId，给客户信息异常记录赋值
                chgOrdId = newJiKeProdInfoDTOList.get(0).getChgOrderId();

                message = "变更差异值存入数据库失败";
                // 3.将变更差异值存入数据库
                // 3.1先查询版本号是否存在，如果不存在则默认1， 存在则加1
                Map<String, Object> tempMap = exceptionFlowDao.queryMaxChangeVersion(ExceptionChangeService.EXCEPTION_104, cstOrdId);
                int maxVersion = MapUtils.getIntValue(tempMap, MAX_VERSION, 0);

                // 3.2得到原有客户订单信息， 比较客户信息差异值，存入数据表
                Map<String, String> changeCustomerInfoMap = changeJiKeCustomerInfoMap(newJiKeCustomInfoDTO, cstOrdId);
                insertChangeInfo(cstOrdId, "", chgOrdId, type, CUSTOMER_INFO, maxVersion, changeCustomerInfoMap);

                // 3.3 得到原有电路信息， 比较电路信息差异值，存入数据表
                List<JiKeProdInfoDTO> oldJiKeProdInfoDTOListBasic =  exceptionFlowDao.queryJiKeProdInfoDTOListByCstOrdId(cstOrdId);
                List<JiKeProdInfoDTO> oldJiKeProdInfoDTOList = queryJiKeProdInfoDTOList(oldJiKeProdInfoDTOListBasic);
                insertChangeJiKeProdInfoMap(newJiKeProdInfoDTOList, oldJiKeProdInfoDTOList, cstOrdId, srcOrdIdsMap,  chgOrdIdsMap, type, maxVersion);
            }
            else if (ExceptionChangeService.EXCEPTION_4B.equals(type) || ExceptionChangeService.EXCEPTION_4C.equals(type)){
                // 加急或者延期
                // 处理一些信息,比如cstOrdId或者newJiKeProdInfoDTOList加入srvOrdId
                jiKeDealOrder(type, newJiKeProdInfoDTOList, null, srcOrdIdsMap, chgOrdIdsMap);
                cstOrdId = newJiKeProdInfoDTOList.get(0).getCstOrdId();
                // 2.存入变更电路的属性信息，主要是涉及到的时间如完成时间等字段
                jiKeUrgentOrder(cstOrdId, newJiKeProdInfoDTOList, type, chgOrdIdsMap);
            }
            else if (ExceptionChangeService.EXCEPTION_4E.equals(type)) {
                // 挂起
                jiKeDealOrder(type, newJiKeProdInfoDTOList, null, srcOrdIdsMap, chgOrdIdsMap);
            }
            else if (ExceptionChangeService.EXCEPTION_4F.equals(type)) {
                // 解挂
                jiKeDealOrder(type, newJiKeProdInfoDTOList, null,  srcOrdIdsMap, chgOrdIdsMap);
            }
            else if (ExceptionChangeService.EXCEPTION_4D.equals(type)) {
                // 撤单
                jiKeDealOrder(type, newJiKeProdInfoDTOList, null,  srcOrdIdsMap, chgOrdIdsMap);
            }

            resultMap.put(SUCCESS, true);
            resultMap.put("chgOrdId", chgOrdId);
        }
        catch (Exception e) {
                resultMap.put(SUCCESS, false);
                resultMap.put(MESSAGE, message);
                logger.error(e.getMessage(), e);
                // 手动回滚
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
        return resultMap;

    }

    /**
     * 集客异常单挂起原单并启动异常单
     * @param type 异常单类型
     * @param newJiKeProdInfoDTOList 传递的电路信息列表
     * @param newJiKeCustomInfoDTO 传递的客户信息
     * @param srcOrdIdsMap 电路主键SRV_ORD_ID与定单ORDER_ID关系
     * @param chgOrdIdsMap 电路主键SRV_ORD_ID与异常单ORDER_ID关系
     */
    private void jiKeDealOrder(String type, List<JiKeProdInfoDTO> newJiKeProdInfoDTOList,
                          JiKeCustomInfoDTO newJiKeCustomInfoDTO, Map<String, String> srcOrdIdsMap,
                          Map<String, String> chgOrdIdsMap){
        for(JiKeProdInfoDTO newJiKeProdInfoDTO : newJiKeProdInfoDTOList){
            String tradeIdRela = newJiKeProdInfoDTO.getTradeIdRela();
            String serviceOfferId = newJiKeProdInfoDTO.getServiceOfferId();
            // 得到电路信息基本信息，取时间最近的一条
            message = "根据入参TRADE_ID_RELA" +tradeIdRela+"没有查询到电路信息";
            List<JiKeProdInfoDTO> oldJiKeProdInfoDTOListBasic =  exceptionFlowDao.queryProdInfoDTOListByTradeId(tradeIdRela,serviceOfferId);
            JiKeProdInfoDTO oldJiKeProdInfoDTO = oldJiKeProdInfoDTOListBasic.get(0);
            // 得到电路完整信息
            List<JiKeProdAttrDTO> jiKeProdAttrDTOList = exceptionFlowDao.queryJiKeProdAttrDTOList(oldJiKeProdInfoDTO.getSrvOrdId());
            oldJiKeProdInfoDTO.setJiKeProdAttrDTOList(jiKeProdAttrDTOList);

            // 将原有的值如srvOrdId赋值给新传递过来的电路信息，用于后续比较差异值
            String cstOrdId = oldJiKeProdInfoDTO.getCstOrdId();
            newJiKeProdInfoDTO.setCstOrdId(cstOrdId);
            newJiKeProdInfoDTO.setSrvOrdId(oldJiKeProdInfoDTO.getSrvOrdId());

            if(ExceptionChangeService.EXCEPTION_4A.equals(type)){
                // 追单挂起
                // 1. 先判断客户信息是否被修改，如果客户信息被修改，那么所属电路都进行挂起
                Map<String, String> changeCustomerInfoMapTemp = changeJiKeCustomerInfoMap(newJiKeCustomInfoDTO, cstOrdId);
                if (!changeCustomerInfoMapTemp.get(ExceptionChangeService.CHANGE_DATA).isEmpty()) {
                    // 追单的挂起和发起异常单
                    chaseOrder(srcOrdIdsMap,  chgOrdIdsMap, oldJiKeProdInfoDTO.getSrvOrdId(),
                            oldJiKeProdInfoDTO.getOrderId(), null, newJiKeProdInfoDTO);
                    continue;
                }
                // 2. 如果客户信息没有改变那么则比较电路信息是否有差异值，有差异值证明该电路被修改，挂起相应的定单
                Map<String, String> changeProdInfoMapTemp = exceptionChangeService.changeJiKeProdInfo(type, oldJiKeProdInfoDTO.getSrvOrdId(),
                        newJiKeProdInfoDTO, oldJiKeProdInfoDTO);
                if (!changeProdInfoMapTemp.get(ExceptionChangeService.CHANGE_DATA).isEmpty()) {
                    // 追单的挂起和发起异常单
                    chaseOrder(srcOrdIdsMap,  chgOrdIdsMap, oldJiKeProdInfoDTO.getSrvOrdId(),
                            oldJiKeProdInfoDTO.getOrderId(), null, newJiKeProdInfoDTO);
                }
            }
            else if (ExceptionChangeService.EXCEPTION_4B.equals(type) || ExceptionChangeService.EXCEPTION_4C.equals(type)){
                // 加急或者延期
                // 加急、延期发起异常单
                otherExceptionOrder(srcOrdIdsMap, oldJiKeProdInfoDTO.getSrvOrdId(), oldJiKeProdInfoDTO.getOrderId(),
                        chgOrdIdsMap, null, newJiKeProdInfoDTO);
            }
            else if(ExceptionChangeService.EXCEPTION_4E.equals(type)){
                // 挂起
                // 普通挂起
                suspendOrder(oldJiKeProdInfoDTO.getOrderId(), "集客下发挂起异常单");
                // 发起异常通知单
                FlowOrderDTO flowOrderDTO = otherExceptionOrder(srcOrdIdsMap, oldJiKeProdInfoDTO.getSrvOrdId(),
                        oldJiKeProdInfoDTO.getOrderId(), chgOrdIdsMap, null, newJiKeProdInfoDTO);
                // 存入异常单变更信息表
                insertChangeInfo(cstOrdId, oldJiKeProdInfoDTO.getOrderId(), flowOrderDTO.getOrderId(), type, "", 0, null);
            }
            else if (ExceptionChangeService.EXCEPTION_4F.equals(type)) {
                // 解挂
                unSuspendOrder(oldJiKeProdInfoDTO.getOrderId(), "集客下发解挂异常单");
                // 发起异常通知单
                FlowOrderDTO flowOrderDTO = otherExceptionOrder(srcOrdIdsMap, oldJiKeProdInfoDTO.getSrvOrdId(),
                        oldJiKeProdInfoDTO.getOrderId(), chgOrdIdsMap, null, newJiKeProdInfoDTO);
                // 存入异常单变更信息表
                insertChangeInfo(cstOrdId, oldJiKeProdInfoDTO.getOrderId(), flowOrderDTO.getOrderId(), type, "", 0, null);
            }
            else if (ExceptionChangeService.EXCEPTION_4D.equals(type)) {
                boolean flag = true;
                // 撤单
                String orderType = oldJiKeProdInfoDTO.getOrderType();
                if ("102".equals(orderType)){ //核查单撤单手动修改定单和工单状态
                    exceptionFlowDao.updStateCancelOrder(oldJiKeProdInfoDTO.getOrderId());
                    flag = false;
                }
                cancelOrder(oldJiKeProdInfoDTO.getOrderId(), flag);
                // 发起异常通知单
                FlowOrderDTO flowOrderDTO = otherExceptionOrder(srcOrdIdsMap, oldJiKeProdInfoDTO.getSrvOrdId(),
                        oldJiKeProdInfoDTO.getOrderId(), chgOrdIdsMap, null, newJiKeProdInfoDTO);
                // 存入异常单变更信息表
                insertChangeInfo(cstOrdId, oldJiKeProdInfoDTO.getOrderId(), flowOrderDTO.getOrderId(), type, "", 0, null);
            }

        }
    }

    /**
     * 根据电路基本信息得到电路完整信息
     * @param oldJiKeProdInfoDTOListBasic
     * @return
     */
    private List<JiKeProdInfoDTO> queryJiKeProdInfoDTOList(List<JiKeProdInfoDTO> oldJiKeProdInfoDTOListBasic){
        List<JiKeProdInfoDTO> oldJiKeProdInfoDTOList = new ArrayList<>();
        if(oldJiKeProdInfoDTOListBasic != null && !oldJiKeProdInfoDTOListBasic.isEmpty()){
            for(JiKeProdInfoDTO oldJiKeProdInfoDTOBasic : oldJiKeProdInfoDTOListBasic){
                List<JiKeProdAttrDTO> jiKeProdAttrDTOList = exceptionFlowDao.queryJiKeProdAttrDTOList(oldJiKeProdInfoDTOBasic.getSrvOrdId());
                oldJiKeProdInfoDTOBasic.setJiKeProdAttrDTOList(jiKeProdAttrDTOList);
                oldJiKeProdInfoDTOList.add(oldJiKeProdInfoDTOBasic);
            }
        }
        return oldJiKeProdInfoDTOList;
    }

    /**
     * 比较客户订单信息
     * @param newJiKeCustomInfoDTO
     * @param cstOrdId
     * @return
     */
    private Map<String, String> changeJiKeCustomerInfoMap(JiKeCustomInfoDTO newJiKeCustomInfoDTO, String cstOrdId) {
        JiKeCustomInfoDTO oldJiKeCustomerInfoDTO = exceptionFlowDao.queryJiKeCustomerInfoDTO(cstOrdId);
        if (oldJiKeCustomerInfoDTO == null) {
            oldJiKeCustomerInfoDTO = new JiKeCustomInfoDTO();
        }
        return exceptionChangeService.changeContent(newJiKeCustomInfoDTO, oldJiKeCustomerInfoDTO);
    }

    /**
     * 集客追单将电路信息差异值存入数据库
     * @param newJiKeProdInfoDTOList
     * @param oldJiKeProdInfoDTOList
     * @param cstOrdId
     * @param srcOrdIdsMap
     * @param chgOrdIdsMap
     * @param type
     * @param maxVersion
     */
    private void insertChangeJiKeProdInfoMap(List<JiKeProdInfoDTO> newJiKeProdInfoDTOList, List<JiKeProdInfoDTO> oldJiKeProdInfoDTOList,
                                             String cstOrdId, Map<String, String> srcOrdIdsMap, Map<String, String> chgOrdIdsMap,
                                             String type, int maxVersion){
        for (JiKeProdInfoDTO newJiKeProdInfoDTO : newJiKeProdInfoDTOList){
            if (oldJiKeProdInfoDTOList != null && !oldJiKeProdInfoDTOList.isEmpty()) {
                for (JiKeProdInfoDTO oldJiKeProdInfoDTO : oldJiKeProdInfoDTOList){
                    // 电路列表保证newJiKeProdInfoDTO有srvOrdId值，用于和旧电路匹配
                    if (oldJiKeProdInfoDTO.getSrvOrdId().equals(newJiKeProdInfoDTO.getSrvOrdId())) {
                        Map<String, String> changeProdInfoMap = exceptionChangeService.changeJiKeProdInfo(type, newJiKeProdInfoDTO.getSrvOrdId(),
                                newJiKeProdInfoDTO, oldJiKeProdInfoDTO);
                        String srcOrdId = srcOrdIdsMap.get(oldJiKeProdInfoDTO.getSrvOrdId());
                        String chgOrderId = chgOrdIdsMap.get(oldJiKeProdInfoDTO.getSrvOrdId());
                        insertChangeInfo(cstOrdId, srcOrdId, chgOrderId, type, PROD_INFO, maxVersion, changeProdInfoMap);
                    }
                }
            }
            else {
                JiKeProdInfoDTO oldJiKeProdInfoDTO = new JiKeProdInfoDTO();
                Map<String, String> changeProdInfoMap = exceptionChangeService.changeJiKeProdInfo(type, newJiKeProdInfoDTO.getSrvOrdId(),
                        newJiKeProdInfoDTO, oldJiKeProdInfoDTO);
                insertChangeInfo(cstOrdId, "", "", type, PROD_INFO, maxVersion, changeProdInfoMap);
            }
        }
    }

    /**
     * 集客产品信息确认，用于追单
     * @param srvOrdId
     * @param changeData
     */
    private void jiKeProdInfoSure(String srvOrdId, String changeData) {
        // 产品信息: 分为产品基本信息和产品属性信息  1.转储 2.更新
        int count = exceptionFlowDao.queryProdInfoHis(srvOrdId);
        if (count == 0) {
            exceptionFlowDao.insertJiKeProdInfoHis(srvOrdId);
        }
        Map<String, String> columnParam = MapConverterUtil.convertJsonArrayToMap(changeData);
        Map<String, String> prodInfoMap = new HashMap<>();
        Map<String, String> prodAttrMap = new HashMap<>();
        // 特定的KEY作为要更新的基本信息
        String[] prodInfoArray = {"SERVICE_ID", "TRADE_TYPE_CODE", "ACTIVE_TYPE", "SERVICE_OFFER_ID", "SERIAL_NUMBER", "USER_ID" };
        // 特定的KEY作为不更新的字段
        String[] invalidProdInfoArray = {"TRADE_ID", "TRADE_ID_RELA", "FLOW_ID"};
        for (Map.Entry<String, String> entry : columnParam.entrySet()) {
            String mapKey = entry.getKey();
            String mapValue = entry.getValue();
            boolean res = Arrays.asList(prodInfoArray).contains(mapKey);
            if (res) {
                // 结果为true，则表示该值是基本信息
                prodInfoMap.put(mapKey, mapValue);
            } else {
                // 先排除无效KEY，剩下的才作为属性信息
                boolean resInvalid = Arrays.asList(invalidProdInfoArray).contains(mapKey);
                if(!resInvalid){
                    prodAttrMap.put(mapKey, mapValue);
                }
            }
        }
        prodInfoMap.put(SRV_ORD_ID, srvOrdId);
        // 更新产品信息
        // 不更新信息表
        if (prodInfoMap.size() > 1) {
            //exceptionFlowDao.updateProdInfo(prodInfoMap);
        }

        // 转储产品属性信息，更新
        for (Map.Entry<String, String> entry : prodAttrMap.entrySet()) {
            String mapKey = entry.getKey();
            String mapValue = entry.getValue();
            int attrCount  =  exceptionFlowDao.queryJiKeProdAttrHis(srvOrdId, mapKey);
            if (attrCount == 0) {
                exceptionFlowDao.insertJiKeProdAttrHis(srvOrdId, mapKey);
            }
            // 如果是有传递要求完成时间，则更新定单要求完成时间
            if(ExceptionChangeService.REQUIRE_COMPLETE_DATE_CODE.equals(mapKey) && StringUtils.isNotEmpty(mapValue)){
                exceptionChangeService.updateOrderReqFinDate(srvOrdId, mapValue);
            }
            // 先判断是否有旧的，如果有则更新没有则插入
            // 根据newAttrCode和srvOrdId去查出旧的属性对象，如果没有则需要new一个，追单确认的时候更新就要用merge
            List<JiKeProdAttrDTO> oldJiKeProdAttrDTOList = exceptionFlowDao.queryJiKeProdAttrDTOListByAttrCode(srvOrdId, mapKey);
            if(oldJiKeProdAttrDTOList != null && !oldJiKeProdAttrDTOList.isEmpty()){
                exceptionFlowDao.updateJiKeProdAttr(srvOrdId, mapKey, mapValue);
            }else{
                // 需要属性做插入
                exceptionFlowDao.insertJiKeProdAttr(srvOrdId, mapKey, mapValue);
            }
        }
    }

    /**
     * 处理集客加急或者延期变更差异值
     * @param cstOrdId
     * @param newJiKeProdInfoDTOList
     * @param type
     * @param chgOrdIdsMap
     */
    private void jiKeUrgentOrder(String cstOrdId, List<JiKeProdInfoDTO> newJiKeProdInfoDTOList, String type, Map<String, String> chgOrdIdsMap) {
        // 得到电路完整信息
        List<JiKeProdInfoDTO> oldJiKeProdInfoDTOListBasic =  exceptionFlowDao.queryJiKeProdInfoDTOListByCstOrdId(cstOrdId);
        List<JiKeProdInfoDTO> oldJiKeProdInfoDTOList = queryJiKeProdInfoDTOList(oldJiKeProdInfoDTOListBasic);
        for (JiKeProdInfoDTO newJiKeProdInfoDTO : newJiKeProdInfoDTOList) {
            if (oldJiKeProdInfoDTOList != null && !oldJiKeProdInfoDTOList.isEmpty()) {
                for (JiKeProdInfoDTO oldJiKeProdInfoDTO : oldJiKeProdInfoDTOList) {
                    // 判断是同一条电路才进行属性比较
                    if (oldJiKeProdInfoDTO.getSrvOrdId().equals(newJiKeProdInfoDTO.getSrvOrdId())) {
                        String chgOrderId = chgOrdIdsMap.get(oldJiKeProdInfoDTO.getSrvOrdId());
                        List<JiKeProdAttrDTO> newJiKeProdAttrDTOList = newJiKeProdInfoDTO.getJiKeProdAttrDTOList();
                        // 此处处理了属性信息的转储和更新，并且得到差异值集合
                        Map<String, String> changeProdAttrMap = exceptionChangeService.changeJiKeProdAttrContent(type, oldJiKeProdInfoDTO.getSrvOrdId(),
                                newJiKeProdAttrDTOList);
                        insertChangeInfo(cstOrdId, oldJiKeProdInfoDTO.getOrderId(), chgOrderId, type, "", 0, changeProdAttrMap);
                    }
                }
            }

        }

    }
    /**
     * ------------------------------------集客异常单end-------------------------------------------------------------
     */

    /**
     * 撤单(包括原单子流程)
     * @param orderId
     */
    private void cancelOrder(String orderId, boolean flag) {
        if (flag){
            //撤单
            flowActionHandler.cancelOrder(OPER_STAFF_ID, orderId);
        }
        //遍历子流程
        List<String> cldOrderIds = exceptionFlowDao.lstCldOrderIds(orderId, PubVal.ORD_STA_STAFLW);
        for (String cldOrderId : cldOrderIds) {
            cancelOrder(cldOrderId, true);
        }
    }

    /**
     * 挂起(包括子流程)
     * @param orderId
     * @param remark
     */
    private void suspendOrder(String orderId, String remark) {
        //挂起
        flowActionHandler.suspendOrder(orderId, OPER_STAFF_ID, remark);

        //遍历子流程
        List<String> cldOrderIds = exceptionFlowDao.lstCldOrderIds(orderId, PubVal.ORD_STA_STAFLW);
        for (String cldOrderId : cldOrderIds) {
            suspendOrder(cldOrderId, remark);
        }
    }

    /**
     * 解挂(包括子流程)
     * @param orderId
     * @param remark
     */
    private void unSuspendOrder(String orderId, String remark) {
        //解挂
        flowActionHandler.cancelSuspendOrder(orderId, OPER_STAFF_ID, remark);

        //遍历子流程
        List<String> cldOrderIds = exceptionFlowDao.lstCldOrderIds(orderId, PubVal.ORD_STA_SUSPEND);
        for (String cldOrderId : cldOrderIds) {
            unSuspendOrder(cldOrderId, remark);
        }
    }



    //-------------------------------------------------测试--------------------------------------------------------------------
    @Override
    public void testExceptionFlowChange(Map<String, String> param) {
        String orderType = "104";
        // 追单请求报文
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF8\"?>\n" +
                "<root>\n" +
                "  <header>\n" +
                "    <eventType>\n" +
                "    </eventType>\n" +
                "    <interfaceCode>\n" +
                "    </interfaceCode>\n" +
                "    <serialNo>\n" +
                "    </serialNo>\n" +
                "    <userId>\n" +
                "    </userId>\n" +
                "    <password>\n" +
                "    </password>\n" +
                "    <sender>\n" +
                "    </sender>\n" +
                "    <province>\n" +
                "    </province>\n" +
                "  </header>\n" +
                "  <body>\n" +
                "    <serviceOrderInfo>\n" +
                "      <orderCode>200116603</orderCode>\n" +
                "      <orderType>" + orderType +
                "      </orderType>\n" +
                "      <orderTitle>2017-07675-k01B</orderTitle>\n" +
                "      <orderContent>450YTW031741</orderContent>\n" +
                "      <orderFlag>\n" +
                "      </orderFlag>\n" +
                "      <orderLevel>\n" +
                "      </orderLevel>\n" +
                "      <handledept>江苏省分公司/苏州市分公司</handledept>\n" +
                "      <custmanagertel>朱剑波,15651101203</custmanagertel>\n" +
                "      <groupOrderCode>200116603</groupOrderCode>\n" +
                "      <projectFlag>\n" +
                "      </projectFlag>\n" +
                "      <contract>2017-002105</contract>\n" +
                "      <originatorDepartment>集团大客户部</originatorDepartment>\n" +
                "      <originator>朱剑波</originator>\n" +
                "      <originatorPhone>\n" +
                "      </originatorPhone>\n" +
                "      <provinceManager>李林夕,15651600335</provinceManager>\n" +
                "      <provinceManagerPhone>李林夕,15651600335</provinceManagerPhone>\n" +
                "      <groupManager>任磊,66258217</groupManager>\n" +
                "      <groupManagerPhone>任磊,66258217</groupManagerPhone>\n" +
                "      <comment>\n" +
                "      </comment>\n" +
                "    </serviceOrderInfo>\n" +
                "    <customerInfo>\n" +
                "      <cnName>苏州工业园区计算机信息中心2333</cnName>\n" +
                "      <enName>\n" +
                "      </enName>\n" +
                "      <code>\n" +
                "      </code>\n" +
                "      <serviceLevel>\n" +
                "      </serviceLevel>\n" +
                "      <address>苏州工业园区现代大道999号现代大厦4楼机房</address>\n" +
                "      <email>\n" +
                "      </email>\n" +
                "      <phone>/18068009588</phone>\n" +
                "      <contact>杨科</contact>\n" +
                "      <industry>其他行业</industry>\n" +
                "      <upperCode>\n" +
                "      </upperCode>\n" +
                "      <upperName>\n" +
                "      </upperName>\n" +
                "    </customerInfo>\n" +
                "    <dispatchInfo>\n" +
                "      <dispatchOrderNo>[中国联通网调字[2017]07520号]总部加急1号</dispatchOrderNo>\n" +
                "      <staffName>吴京</staffName>\n" +
                "      <staffTel>\n" +
                "      </staffTel>\n" +
                "      <staffOrg>综合调度处</staffOrg>\n" +
                "      <issuer>\n" +
                "      </issuer>\n" +
                "      <sendDate>\n" +
                "      </sendDate>\n" +
                "      <dispatchType>11</dispatchType>\n" +
                "      <dispatchGrade>蓝色</dispatchGrade>\n" +
                "      <dispatchUrgency>普通</dispatchUrgency>\n" +
                "      <dispatchTitle>\n" +
                "      </dispatchTitle>\n" +
                "      <dispatchSendOrg>传送处传输专业,广东,江苏</dispatchSendOrg>\n" +
                "      <dispatchCopyOrg>\n" +
                "      </dispatchCopyOrg>\n" +
                "      <dispatchText>客服通知，电路加急请尽快完成。</dispatchText>\n" +
                "      <changeBeforeText>电路加急</changeBeforeText>\n" +
                "      <changeAfterText>客服通知，电路加急请尽快完成。</changeAfterText>\n" +
                "      <attachs>\n" +
                "      </attachs>\n" +
                "    </dispatchInfo>\n" +
                "    <prodsInfo>\n" +
                "      <prodInfo>\n" +
                "        <prodType>10000002</prodType>\n" +
                "        <prodName>以太网专线</prodName>\n" +
                "        <prodInstId>1201296691</prodInstId>\n" +
                "        <woOrderCode>106254498</woOrderCode>\n" +
                "        <omOrderCode>100935314</omOrderCode>\n" +
                "        <mainOrg>江苏</mainOrg>\n" +
                "        <secondaryOrg>\n" +
                "        </secondaryOrg>\n" +
                "        <actType>101</actType>\n" +
                "        <attachs>\n" +
                "        </attachs>\n" +
                "        <prodAttrs>\n" +
                "          <prodAttr code=\"crm_prod_inst_id\" name=\"电路明细编号\" value=\"2019053110\" />\n" +
                "          <prodAttr code=\"operatetype\" name=\"电路要求\" value=\"新开\" />\n" +
                "          <prodAttr code=\"citya\" name=\"A端城市\" value=\"苏州市\" />\n" +
                "          <prodAttr code=\"cityz\" name=\"Z端城市\" value=\"广州市\" />\n" +
                "          <prodAttr code=\"circuitcode\" name=\"电路编号\" value=\"广州苏州ANE0022NP\" />\n" +
                "          <prodAttr code=\"rfsdate\" name=\"要求完成时间\" value=\"2019-01-26\" />\n" +
                "          <prodAttr code=\"coooperator\" name=\"业务模式\" value=\"20151002005\" />\n" +
                "          <prodAttr code=\"rentfactor\" name=\"租用范围\" value=\"国内长途\" />\n" +
                "          <prodAttr code=\"resourcecategory\" name=\"资源组织方式\" value=\"\" />\n" +
                "          <prodAttr code=\"equipmentsourcea\" name=\"A(CE)端客户设备情况\" value=\"客户设备局方投资个屁\" />\n" +
                "          <prodAttr code=\"old_charge_id\" name=\"原二期计费ID\" value=\"\" />\n" +
                "          <prodAttr code=\"serv_factory_b\" name=\"销售模式\" value=\"\" />\n" +
                "          <prodAttr code=\"aoperatorc\" name=\"大带宽小带宽\" value=\"\" />\n" +
                "          <prodAttr code=\"equipmentsourceb\" name=\"Z端客户设备情况\" value=\"客户设备局方投资\" />\n" +
                "          <prodAttr code=\"speed\" name=\"电路带宽\" value=\"500M\" />\n" +
                "          <prodAttr code=\"completiondate\" name=\"预约服务开始时间\" value=\"\" />\n" +
                "          <prodAttr code=\"foreignopercompletedate\" name=\"预约服务结束时间\" value=\"\" />\n" +
                "          <prodAttr code=\"oldcircuitcode\" name=\"原电路代号\" value=\"\" />\n" +
                "          <prodAttr code=\"orderbureau\" name=\"电路归属\" value=\"\" />\n" +
                "          <prodAttr code=\"applydatea\" name=\"A端要求完成时间\" value=\"2019-01-20\" />\n" +
                "          <prodAttr code=\"applydatez\" name=\"Z端要求完成时间\" value=\"2019-01-20\" />\n" +
                "          <prodAttr code=\"rentdate\" name=\"租期\" value=\"\" />\n" +
                "          <prodAttr code=\"busiflaga\" name=\"竞合方式\" value=\"\" />\n" +
                "          <prodAttr code=\"remark\" name=\"备注\" value=\"\" />\n" +
                "          <prodAttr code=\"custreqdelay\" name=\"客户名称\" value=\"苏州工业园区计算机信息中心\" />\n" +
                "          <prodAttr code=\"custdesiroute\" name=\"客户联系人\" value=\"杨科\" />\n" +
                "          <prodAttr code=\"broad_band_list\" name=\"大带宽电路列表\" value=\"\" />\n" +
                "          <prodAttr code=\"broad_band_cir\" name=\"大带宽电路\" value=\"\" />\n" +
                "          <prodAttr code=\"operatora\" name=\"A端运营商\" value=\"CU\" />\n" +
                "          <prodAttr code=\"porttypea\" name=\"A端接口类型\" value=\"GE以太口\" />\n" +
                "          <prodAttr code=\"usernamea\" name=\"A端客户名称\" value=\"苏州工业园区计算机信息中心\" />\n" +
                "          <prodAttr code=\"cust_address_a\" name=\"A端客户地址\" value=\"\" />\n" +
                "          <prodAttr code=\"constructaddressa\" name=\"A端装机地址\" value=\"苏州工业园区现代大道999号现代大厦4楼机房/\" />\n" +
                "          <prodAttr code=\"contactpersona\" name=\"A端联系人\" value=\"杨科//18068009588\" />\n" +
                "          <prodAttr code=\"operatorz\" name=\"Z端运营商\" value=\"CU\" />\n" +
                "          <prodAttr code=\"porttypez\" name=\"Z端接口类型\" value=\"GE以太口\" />\n" +
                "          <prodAttr code=\"usernamez\" name=\"Z端客户名称\" value=\"苏州工业园区计算机信息中心\" />\n" +
                "          <prodAttr code=\"cust_address_z\" name=\"Z端客户地址\" value=\"\" />\n" +
                "          <prodAttr code=\"constructaddressz\" name=\"Z端装机地址\" value=\"广州市黄埔区科丰路31号金发科技园G25-9栋5楼4号机房B12-B13机柜\" />\n" +
                "          <prodAttr code=\"contactpersonz\" name=\"Z端联系人\" value=\"吴伟林/18620013847\" />\n" +
                "          <prodAttr code=\"inneritmc\" name=\"国内出口局\" value=\"\" />\n" +
                "          <prodAttr code=\"busitype\" name=\"国内段电路类型\" value=\"\" />\n" +
                "          <prodAttr code=\"pop\" name=\"经转POP点\" value=\"\" />\n" +
                "          <prodAttr code=\"spare_flag\" name=\"主备电路\" value=\"主用\" />\n" +
                "          <prodAttr code=\"reqclosetime\" name=\"要求关闭时间\" value=\"\" />\n" +
                "          <prodAttr code=\"maintenancegrade\" name=\"业务开通服务\" value=\"标准\" />\n" +
                "          <prodAttr code=\"circuitnetgrade\" name=\"网络运行维护服务\" value=\"标准\" />\n" +
                "          <prodAttr code=\"custservgrade\" name=\"售后附加服务\" value=\"标准\" />\n" +
                "          <prodAttr code=\"apply_operator\" name=\"发起运营商\" value=\"20151224001\" />\n" +
                "          <prodAttr code=\"changetype\" name=\"电路变更明细\" value=\"\" />\n" +
                "          <prodAttr code=\"portspeeda\" name=\"A端限速速率\" value=\"\" />\n" +
                "          <prodAttr code=\"portspeedz\" name=\"Z端限速速率\" value=\"\" />\n" +
                "          <prodAttr code=\"operate_a\" name=\"A端电路要求\" value=\"\" />\n" +
                "          <prodAttr code=\"operate_z\" name=\"Z端电路要求\" value=\"\" />\n" +
                "          <prodAttr code=\"oldSimpleRoute\" name=\"调前群次路由\" value=\"\" />\n" +
                "          <prodAttr code=\"simpleRoute\" name=\"群次路由\" value=\"苏州《省内资源》南京丹凤街4层综合（9－2798-1.2-JH41.3）~~广州科学城3层综合（9－2769-1.18-JH41.14）\" />\n" +
                "          <prodAttr code=\"oldFullRoute\" name=\"调前全程路由\" value=\"\" />\n" +
                "          <prodAttr code=\"fullRoute\" name=\"全程路由\" value=\"苏州《省内资源》南京丹凤街4层综合（9－2798-1.2-JH41.3）~~广州科学城3层综合（9－2769-1.18-JH41.14）\" />\n" +
                "        </prodAttrs>\n" +
                "      </prodInfo>\n" +
                "    </prodsInfo>\n" +
                "  </body>\n" +
                "</root>";

        GetContentDto getContentDto = new GetContentDto(requestXml);
        // 报文客户信息
        Map<String, Object> customerInfo = getContentDto.getCustomerInfo();
        CustomerInfoDTO newCustomerInfoDTO = MapConverterUtil.convertMap2Bean(customerInfo, CustomerInfoDTO.class);

        // 调单信息
        Map<String, Object> dispatchInfo = getContentDto.getDispatchInfo();
        DispatchInfoDTO newDispatchInfoDTO = MapConverterUtil.convertMap2Bean(dispatchInfo, DispatchInfoDTO.class);

        // 获取电路信息
        List<Map<String, Object>> newProdInfoList = getContentDto.getProdInfo();
        List<ProdInfoDTO> newProdInfoDTOList = new ArrayList<>();
        for(Map<String, Object> prodInfoMap : newProdInfoList){
            ProdInfoDTO prodInfoDTO = MapConverterUtil.convertMap2Bean(prodInfoMap, ProdInfoDTO.class);
            Map<String, Object> prodAttrs = (Map<String, Object>) prodInfoMap.get("prodAttrs");
            List<Map<String, Object>> prodAttr = (List<Map<String, Object>>) prodAttrs.get("prodAttr");
            List<ProdAttrDTO> prodAttrDTOList = MapConverterUtil.convertListMap2ListBean(prodAttr, ProdAttrDTO.class);
            prodInfoDTO.setProdAttrDTOList(prodAttrDTOList);
            newProdInfoDTOList.add(prodInfoDTO);
        }
        // 以下方法测试，需要报文中的crm_prod_inst_id电路明细编号能在数据库中查到
        // SELECT * FROM GOM_BDW_SRV_ORD_INFO t where t.crm_prod_inst_id='201903122230451';
        // 追单
        exceptionFlowChange(orderType, newCustomerInfoDTO, newDispatchInfoDTO, newProdInfoDTOList);
        // 加急
        //exceptionFlowChange("108", null, null, newProdInfoDTOList);
        // 挂起
        //exceptionFlowChange("110", null, null, newProdInfoDTOList);
        // 解挂
        //exceptionFlowChange("111", null, null, newProdInfoDTOList);
        // 撤单
//        exceptionFlowChange("114", null, null, newProdInfoDTOList);
        System.out.println("发起异常单结束");

    }


    @Override
    public void testExceptionFlowSure() {
//        exceptionFlowSure("3414");
        exceptionFlowSure("3390");
    }

    @Override
    public void testJiKeExceptionFlowChange() {
        String jsonStr = "{\n" +
                "\t\"UNI_BSS_ATTACHED\": {\n" +
                "\t\t\"MEDIA_INFO\": \"\"\n" +
                "\t},\n" +
                "\t\"UNI_BSS_BODY\": {\n" +
                "\t\t\"APPLY_ORDER_REQ\": {\n" +
                "\t\t\t\"ROUTING\": {\n" +
                "\t\t\t\t\"ROUTE_VALUE\": \"01\",\n" +
                "\t\t\t\t\"ROUTE_TYPE\": \"00\"\n" +
                "\t\t\t},\n" +
                "\t\t\t\"CST_ORD\": {\n" +
                "\t\t\t\t\"CST_ORD_INFO\": {\n" +
                "\t\t\t\t\t\"CUST_ID\": \"5050150252170002809\",\n" +
                "\t\t\t\t\t\"CUST_TEL\": \"\",\n" +
                "\t\t\t\t\t\"CUST_CONTACT_MAN_NAME\": \"赵迪\",\n" +
                "\t\t\t\t\t\"DEAL_AREA_CODE\": \"501\",\n" +
                "\t\t\t\t\t\"TRADE_STAFF_PHONE\": \"18601221333\",\n" +
                "\t\t\t\t\t\"CONTRACT_ID\": \"CU15-5001-2013-000003\",\n" +
                "\t\t\t\t\t\"SRV_ORD_LIST\": {\n" +
                "\t\t\t\t\t\t\"SRV_ORD\": [{\n" +
                "\t\t\t\t\t\t\t\"SERVICE_OFFER_ID\": \"100000177\",\n" +
                "\t\t\t\t\t\t\t\"SERIAL_NUMBER\": \"501HLW012730\",\n" +
                "\t\t\t\t\t\t\t\"SERVICE_ID\": \"80000017\",\n" +
                "\t\t\t\t\t\t\t\"USER_ID\": \"1392000003453074\",\n" +
                "\t\t\t\t\t\t\t\"SRV_ORD_INFO\": {\n" +
                "\t\t\t\t\t\t\t\t\"SRV_ATTR_INFO\": [{\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_VALUE\": \"1\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_ACTION\": \"0\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_CODE\": \"10000193\"\n" +
                "\t\t\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_VALUE\": \"2\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_ACTION\": \"0\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_CODE\": \"10000199\"\n" +
                "\t\t\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_VALUE\": \"11\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_ACTION\": \"0\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_CODE\": \"10000192\"\n" +
                "\t\t\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_VALUE\": \"1\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_ACTION\": \"0\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_CODE\": \"10000194\"\n" +
                "\t\t\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_VALUE\": \"1\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_ACTION\": \"0\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_CODE\": \"10000198\"\n" +
                "\t\t\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_VALUE\": \"2\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_ACTION\": \"0\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_CODE\": \"10001104\"\n" +
                "\t\t\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_VALUE\": \"29\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_ACTION\": \"0\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_CODE\": \"10001110\"\n" +
                "\t\t\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_VALUE\": \"1\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_ACTION\": \"0\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_CODE\": \"10001113\"\n" +
                "\t\t\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_VALUE\": \"2\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_ACTION\": \"0\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_CODE\": \"10001117\"\n" +
                "\t\t\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_VALUE\": \"0\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_ACTION\": \"0\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_CODE\": \"10001121\"\n" +
                "\t\t\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_VALUE\": \"0\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_ACTION\": \"0\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_CODE\": \"10001102\"\n" +
                "\t\t\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_VALUE\": \"1\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_ACTION\": \"0\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_CODE\": \"10001111\"\n" +
                "\t\t\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_VALUE\": \"1\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_ACTION\": \"0\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_CODE\": \"10000100\"\n" +
                "\t\t\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_VALUE\": \"0\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_ACTION\": \"0\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_CODE\": \"10000930\"\n" +
                "\t\t\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_VALUE\": \"0\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_ACTION\": \"0\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_CODE\": \"CON0001\"\n" +
                "\t\t\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_VALUE\": \"0\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_ACTION\": \"0\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_CODE\": \"CON0002\"\n" +
                "\t\t\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_VALUE\": \"501\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_ACTION\": \"0\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_CODE\": \"CON0005\"\n" +
                "\t\t\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_VALUE\": \"海南市经济技术开发区\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_ACTION\": \"0\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_CODE\": \"CON0007\"\n" +
                "\t\t\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_VALUE\": \"50\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_ACTION\": \"0\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_CODE\": \"CON0101\"\n" +
                "\t\t\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_VALUE\": \"501\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_ACTION\": \"0\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_CODE\": \"CON0103\"\n" +
                "\t\t\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_VALUE\": \"20190510000000\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_ACTION\": \"0\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_CODE\": \"CON0014\"\n" +
                "\t\t\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_VALUE\": \"1\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_ACTION\": \"0\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_CODE\": \"CON0095\"\n" +
                "\t\t\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_VALUE\": \"0\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_ACTION\": \"0\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_CODE\": \"CON0093\"\n" +
                "\t\t\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_VALUE\": \"1\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_ACTION\": \"0\",\n" +
                "\t\t\t\t\t\t\t\t\t\"ATTR_CODE\": \"CON0094\"\n" +
                "\t\t\t\t\t\t\t\t}]\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t\"TRADE_ID_RELA\": \"16000000424161\",\n" +
                "\t\t\t\t\t\t\t\"FLOW_ID\": \"440207212\",\n" +
                "\t\t\t\t\t\t\t\"TRADE_TYPE_CODE\": \"2001\",\n" +
                "\t\t\t\t\t\t\t\"TRADE_ID\": \"16000000424284\",\n" +
                "\t\t\t\t\t\t\t\"ACTIVE_TYPE\": \"4A\"\n" +
                "\t\t\t\t\t\t}]\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t\"CONTRACT_NAME\": \"张慧42合同\",\n" +
                "\t\t\t\t\t\"CUST_ADDRESS\": \"辽宁省大连出口加工区A区气体工业园F1-1栋厂房\",\n" +
                "\t\t\t\t\t\"ACCEPT_DATE\": \"20190509114415\",\n" +
                "\t\t\t\t\t\"CUST_CITY\": \"501\",\n" +
                "\t\t\t\t\t\"CUST_CONTACT_MAN_TEL\": \"13411111111\",\n" +
                "\t\t\t\t\t\"NETWORK_LEVEL\": \"\",\n" +
                "\t\t\t\t\t\"DEPART_NAME\": \"海南省分公司海口市分公司\",\n" +
                "\t\t\t\t\t\"CUST_NAME_CHINESE\": \"阿波罗(大连)照明制品有限公司\",\n" +
                "\t\t\t\t\t\"CUST_EMAIL\": \"\",\n" +
                "\t\t\t\t\t\"TRADE_EPARCHY_CODE\": \"501\",\n" +
                "\t\t\t\t\t\"TRADE_STAFF_NAME\": \"海南计费\",\n" +
                "\t\t\t\t\t\"REMARK\": \"工号：hnbill对客户阿波罗(大连)照明制品有限公司进行产品订购操作！\",\n" +
                "\t\t\t\t\t\"CUST_FAX\": \"\"\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t\"SUBSCRIBE_ID\": \"2019-501HLW-G0064-040721\"\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t},\n" +
                "\t\"UNI_BSS_HEAD\": {\n" +
                "\t\t\"APP_ID\": \"nG1SraNX9Q\",\n" +
                "\t\t\"TIMESTAMP\": \"2019-05-09 14:27:30 424\",\n" +
                "\t\t\"TRANS_ID\": \"20190509142730424889792\",\n" +
                "\t\t\"TOKEN\": \"cca8c0402677263fb47d2893b05155d0\"\n" +
                "\t}\n" +
                "}";
        // 取值 CST_ORD_INFO 下的字段
        // 先统一取出需要的节点，防止后面移除
        /*JSONObject jsStr = JSONObject.parseObject(jsonStr);
        String UNI_BSS_BODY = jsStr.getString("UNI_BSS_BODY");
        JSONObject js_UNI_BSS_BODY = JSONObject.parseObject(UNI_BSS_BODY);
        JSONObject js_APPLY_ORDER_REQ = JSONObject.parseObject(js_UNI_BSS_BODY.getString("APPLY_ORDER_REQ"));
        JSONObject js_CST_ORD = JSONObject.parseObject(js_APPLY_ORDER_REQ.getString("CST_ORD"));
        // 取值 CST_ORD_INFO 下的字段
        JSONObject js_CST_ORD_INFO = JSONObject.parseObject(js_CST_ORD.getString("CST_ORD_INFO"));
        JSONObject js_SRV_ORD_LIST = JSONObject.parseObject(js_CST_ORD_INFO.getString("SRV_ORD_LIST"));
        JSONArray js_SRV_ORD = JSON.parseArray(js_SRV_ORD_LIST.getString("SRV_ORD"));

        // 2. 转换客户信息DTO
        Map customerInfoMapTemp = js_CST_ORD_INFO;
        // 2.1移除电路列表
        customerInfoMapTemp.remove("SRV_ORD_LIST");
        JiKeCustomInfoDTO jiKeCustomInfoDTO = JsonConverterUtil.convertUpperKeyMap2Bean(customerInfoMapTemp, JiKeCustomInfoDTO.class);

        // 3.转换电路信息(包括属性信息)
        List<JiKeProdInfoDTO> jiKeProdInfoDTOList = new ArrayList<>();
        Iterator it = js_SRV_ORD.iterator();
        while (it.hasNext()) {
            JSONObject jsonObjectProdInfo = (JSONObject) it.next();
            // 先取出列表信息再移除对象
            JSONObject js_SRV_ORD_INFO = JSONObject.parseObject(jsonObjectProdInfo.getString("SRV_ORD_INFO"));
            // 移除属性对象
            jsonObjectProdInfo.remove("SRV_ORD_INFO");
            Map prodInfoMapTemp = jsonObjectProdInfo;
            JiKeProdInfoDTO jiKeProdInfoDTO = JsonConverterUtil.convertUpperKeyMap2Bean(prodInfoMapTemp, JiKeProdInfoDTO.class);

            //
            JSONArray js_SRV_ATTR_INFO = JSON.parseArray(js_SRV_ORD_INFO.getString("SRV_ATTR_INFO"));
            Iterator it1 = js_SRV_ATTR_INFO.iterator();
            List<JiKeProdAttrDTO> jiKeProdAttrDTOList = new ArrayList<>();
            while (it1.hasNext()){
                JSONObject jsonObjectProdAttr = (JSONObject) it1.next();
                Map prodAttrInfoMapTemp = jsonObjectProdAttr;
                JiKeProdAttrDTO jiKeProdAttrDTO = JsonConverterUtil.convertUpperKeyMap2Bean(prodAttrInfoMapTemp, JiKeProdAttrDTO.class);
                jiKeProdAttrDTOList.add(jiKeProdAttrDTO);
            }
            jiKeProdInfoDTO.setJiKeProdAttrDTOList(jiKeProdAttrDTOList);
            jiKeProdInfoDTOList.add(jiKeProdInfoDTO);
        }

        // 4.调用接口
        // 追单
        jiKeExceptionFlowChange("4A", jiKeCustomInfoDTO, jiKeProdInfoDTOList);*/

        List<JiKeProdInfoDTO> jiKeProdInfoDTOList = new ArrayList<>();
        JiKeProdInfoDTO jiKeProdInfoDTO = new JiKeProdInfoDTO();
        jiKeProdInfoDTO.setTradeIdRela("16000000424161");
        List<JiKeProdAttrDTO> jiKeProdAttrDTOList = new ArrayList<>();
        JiKeProdAttrDTO jiKeProdAttrDTO = new JiKeProdAttrDTO();
        jiKeProdAttrDTO.setAttrCode("CON0007");
        jiKeProdAttrDTO.setAttrValue("测试延期");
        jiKeProdAttrDTOList.add(jiKeProdAttrDTO);
        jiKeProdInfoDTO.setJiKeProdAttrDTOList(jiKeProdAttrDTOList);
        jiKeProdInfoDTOList.add(jiKeProdInfoDTO);
        // 加急
//        jiKeExceptionFlowChange("4B", null, jiKeProdInfoDTOList);
        // 延期
        jiKeExceptionFlowChange("4C", null, jiKeProdInfoDTOList);


    }
}
