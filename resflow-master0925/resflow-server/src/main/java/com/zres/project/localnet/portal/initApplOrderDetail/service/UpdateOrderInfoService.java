package com.zres.project.localnet.portal.initApplOrderDetail.service;

import java.text.SimpleDateFormat;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.zres.project.localnet.portal.initApplOrderDetail.dao.DelOrderInfoDao;
import com.zres.project.localnet.portal.initApplOrderDetail.dao.EditDraftDao;
import com.zres.project.localnet.portal.initApplOrderDetail.dao.InsertOrderInfoDao;

import com.ztesoft.res.frame.core.util.MapUtil;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.applpage.service.UpdateOrderInfoIntf;
import com.zres.project.localnet.portal.initApplOrderDetail.dao.UpdateOrderInfoDao;
import com.zres.project.localnet.portal.util.SystemResouceType;

/**
 * @author :ren.jiahang
 * @date:2019/1/7@time:15:00
 */
@Service
public class UpdateOrderInfoService implements UpdateOrderInfoIntf {
    Logger logger = LoggerFactory.getLogger(InsertOrderInfoService.class);
    @Autowired
    private UpdateOrderInfoDao updateOrderInfoDao;
    @Autowired
    EditDraftDao editDraftDao;
    @Autowired
    private DelOrderInfoDao delOrderInfoDao;
    @Autowired
    private InsertOrderInfoDao insertOrderInfoDao;
    @Autowired
    private InsertOrderInfoService insertOrderInfoService;

    public int updOrderIdBySrvID(Map param) {
        return updateOrderInfoDao.updOrderIdBySrvID(param);
    }

    //更新草稿单数据
    public String orderInfoUpdate(Map<String, Object> map) {
        String operStaffId = "";
        if (ThreadLocalInfoHolder.getLoginUser() == null) {
            operStaffId = "11";
        }
        else {
            //获取用户id
            operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        }
        //创建当前时间
        Date currdate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currDateStr = sdf.format(currdate);
        //定义返回值
        String result = "success";
        //获取订单和用户信息
        JSONObject orderInfo = JSON.parseObject(map.get("OrderCustmInfo").toString());
        Map orderInfomap = orderInfo;
        //获取产品类型
        String service_id = orderInfomap.get("SERVICE_ID").toString();
        //获取客户ID
        String CustID = map.get("CUST_ID").toString();
        //将 CustID添加到 数据中
        orderInfomap.put("CST_ORD_ID", CustID);
        logger.info("*定单和客户信息-*" + orderInfomap);
        //更新客户表
        this.UpdateCustomerInfo(orderInfomap);
        //根据客户ID 查询业务订单ID
        List<String> srvOrdIdList = editDraftDao.querySrvOrdIdByCustId(CustID, "10C");

        //获取电路信息
        JSONArray cirData1 = JSON.parseArray(map.get("cirData").toString());


        //根据传递过来的电路信息遍历查出业务订单ID
        List<String> srvIdList = new ArrayList<String>();
        //修改草稿单中新增的电路信息
        try {
            for (int k = 0; k < cirData1.size(); k++) {

                JSONObject cirItemJson = cirData1.getJSONObject(k);
                Map<String, Object> ciritemMap = cirItemJson;
                //判断如果电路信息中的 业务订单ID 为空则表示 有新增的电路信息 需要插叙新的业务订单
                //String test = ciritemMap.get("SRV_ORD_ID").toString();
                if (ciritemMap.get("SRV_ORD_ID") == null) {
                    //客户序列
                    orderInfomap.put("CST_ORD_ID", CustID);
                    orderInfomap.put("SRV_ORD_STAT", "10C");
                    orderInfomap.put("SYSTEM_RESOURCE", SystemResouceType.SECONDARY_RESOURCE);
                    //发起时间
                    orderInfomap.put("CREATE_DATE", currDateStr);
                    //gom_order.id保存草稿默认0 未起流程
                    orderInfomap.put("ORDER_ID", 0);
                    orderInfomap.put("USER_ID", operStaffId); //发起人
                    //获取订单表序列值
                    int seq_gom_bdw_srv_ord_info = this.querySequence("GOM_BDW_SRV_ORD_INFO");
                    String tradeId = MapUtil.getString(ciritemMap, "tradeId"); //业务订单号
                    String serialNumber = MapUtil.getString(ciritemMap, "serialNumber"); //业务号码
                    if ("20181221006".equals(service_id)) {
                        orderInfomap.put("TRADE_ID", seq_gom_bdw_srv_ord_info);
                        orderInfomap.put("SERIAL_NUMBER", String.valueOf(seq_gom_bdw_srv_ord_info));
                    }
                    else {
                        orderInfomap.put("TRADE_ID", tradeId);
                        orderInfomap.put("SERIAL_NUMBER", serialNumber);
                    }
                    orderInfomap.put("SRV_ORD_ID", seq_gom_bdw_srv_ord_info);
                    String instance_id = MapUtil.getString(ciritemMap, "INSTANCE_ID"); //实例id
                    if("".equals(instance_id)){
                        orderInfomap.put("INSTANCE_ID",seq_gom_bdw_srv_ord_info);
                    }
                    else{
                        orderInfomap.put("INSTANCE_ID",instance_id); //实例id
                    }
                    insertOrderInfoService.attachSave(map, seq_gom_bdw_srv_ord_info); //插入附件信息
                    insertOrderInfoService.circuitAttachSave(map, seq_gom_bdw_srv_ord_info); //插入电路信息附件
                    /**
                     * modify by ren.jiahang  at 20200801 for 待办查询优化，要求完成时间改从电路纵表存储到横表中（GOM_BDW_SRV_ORD_INFO.req_fin_time）
                     */
                    String requFineTime = MapUtil.getString(ciritemMap, "requFineTime"); //全程要求完成时间
                    orderInfomap.put("REQ_FIN_TIME",requFineTime);
                    //插入定单信息
                    this.insertOrderInfo(orderInfomap);
                    insertOrderInfoService.packageCircuitInfo(ciritemMap, service_id, seq_gom_bdw_srv_ord_info, currDateStr); //包装并插入电路信息
                }
                else {
                    srvIdList.add(ciritemMap.get("SRV_ORD_ID").toString());
                }

            }
        }
        catch (Exception e) {
            result = "error";
            logger.info(e.getMessage());
            logger.error("***************error***************", e);
        }
        try {
            List<String> NeedDeleteSrvOrdIDList = this.takeDifferentElements(srvIdList, srvOrdIdList);
            //如何俩个业务电路的数量不同 则删除不同的业务电路
            if (!NeedDeleteSrvOrdIDList.isEmpty()) {
                delOrderInfoDao.delOrderInfoBySrvId(NeedDeleteSrvOrdIDList);
            }
            //删除对应的已存在电路信息 ，保留ServOrdId 重新插入电路信息
            delOrderInfoDao.delOrdAttrInfoBySrvId(srvOrdIdList);
            //插入新的电路属性
            for (int i = 0; i < cirData1.size(); i++) {
                JSONObject cirData1Json = cirData1.getJSONObject(i);
                Map<String, Object> cirDatalMap = cirData1Json;
                Integer servid = MapUtil.getInteger(cirDatalMap, "SRV_ORD_ID");
                //Servid==null 表示此条电路信息为编辑草稿单新增的的 电路信息 ，已经添加到数据库中，所以跳出循环
                if (servid == null || servid == 0) {
                    continue;
                }
                /*更新定单信息 start*/
                orderInfomap.put("SRV_ORD_ID", servid);
                String tradeId = MapUtil.getString(cirDatalMap, "tradeId"); //业务订单号
                String serialNumber = MapUtil.getString(cirDatalMap, "serialNumber"); //业务号码
                if ("20181221006".equals(service_id)) {
                    orderInfomap.put("TRADE_ID", servid);
                    orderInfomap.put("SERIAL_NUMBER", String.valueOf(servid));
                }
                else {
                    orderInfomap.put("TRADE_ID", tradeId);
                    orderInfomap.put("SERIAL_NUMBER", serialNumber);
                }
                /**
                 * modify by ren.jiahang  at 20200801 for 待办查询优化，要求完成时间改从电路纵表存储到横表中（GOM_BDW_SRV_ORD_INFO.req_fin_time）
                 */
                String requFineTime = MapUtil.getString(cirDatalMap, "requFineTime"); //全程要求完成时间
                orderInfomap.put("REQ_FIN_TIME",requFineTime);
                //更新业务订单表
                this.UpdateOrderInfo(orderInfomap);
                /*更新定单信息 end*/

                insertOrderInfoService.attachSave(map, servid); //插入附件信息
                insertOrderInfoService.circuitAttachSave(map, servid); //插入电路信息附件
                insertOrderInfoService.packageCircuitInfo(cirDatalMap, service_id, servid, currDateStr); //包装并插入电路信息
            }
        }
        catch (Exception e) {
            result = "error";
            logger.info(e.getMessage());
            logger.error("***************error***************", e);
        }
        return result;
    }

    /**
     * 更新客户表数据
     *
     * @param map
     * @return
     */
    public int UpdateCustomerInfo(Map<String, Object> map) {
        return updateOrderInfoDao.UpdateCustomerInfo(map);

    }

    /**
     * 更新业务订单表
     *
     * @param map
     * @return
     */
    public int UpdateOrderInfo(Map<String, Object> map) {

        return updateOrderInfoDao.UpdateSrvOrderInfo(map);
    }

    /**
     * 更新电路信息
     *
     * @param list
     * @return
     */
    public int UpdateordAttrInfo(List<Map<String, Object>> list) {
        return 0;
    }

    /**
     * 删除电路信息
     *
     * @param list
     * @return
     */
    public int deleteCircuitInfo(List<String> list) {
        return 0;
    }

    /**
     * 取出俩个list集合中 不同的元素
     *
     * @param small
     * @param big
     * @return
     */
    public List<String> takeDifferentElements(List<String> small, List<String> big) {
        List<String> exists = new ArrayList<String>(big);
        exists.removeAll(small);
        return exists;
    }

    /**
     * 查询序列
     *
     * @param tableName
     * @return int
     * @author ren.jiahang
     * @date 2019/1/5 15:24
     */
    public int querySequence(String tableName) {
        return insertOrderInfoDao.querySequence("seq_" + tableName + ".nextval");
    }

    /**
     * 插入定单信息
     *
     * @param map
     * @return int
     * @author ren.jiahang
     * @date 2019/1/5 15:25
     */
    public int insertOrderInfo(Map<String, Object> map) {
        return insertOrderInfoDao.insertOrderInfo(map);
    }

}
