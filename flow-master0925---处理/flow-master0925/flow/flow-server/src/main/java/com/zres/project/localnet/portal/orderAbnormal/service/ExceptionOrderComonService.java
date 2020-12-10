package com.zres.project.localnet.portal.orderAbnormal.service;

import com.zres.project.localnet.portal.orderAbnormal.constant.OrderAbnormalConstant;
import com.zres.project.localnet.portal.orderAbnormal.dao.OrderAbnormalDao;
import com.ztesoft.res.frame.flow.common.dto.FlowOrderDTO;
import com.ztesoft.res.frame.flow.common.dto.FlowOrderSpecDTO;
import com.ztesoft.res.frame.flow.common.dto.FlowWoDTO;
import com.ztesoft.res.frame.flow.common.intf.FlowAction;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExceptionOrderComonService {

    @Autowired
    OrderAbnormalDao orderAbnormalDao;

    @Autowired
    FlowAction flowAction;


    /**
     * 修改异常单状态
     * 追单：
     * 追单确认 待处理 改成 审核通过
     * 追单驳回 待处理 改成 审核不通过
     * <p>
     * 异常单结束
     * 追单：
     * 审核通过 改成 审核通过结束
     * 审核不通过 改成 审核不通过结束
     * 其他不需要审核的：
     * 待处理 改成 仅保存记录
     *
     * @param chgorderIds 异常单id队列，和chgOrderId二选一
     * @param chgOrderId  异常单id，和chgorderIds二选一
     * @param state       异常单状态
     * @param remark      备注
     */
    public void editExceptionOrderState(List<String> chgorderIds, String chgOrderId,
                                        String state, String remark) {
        if (StringUtils.isEmpty(state)) {
            orderAbnormalDao.updateChangeOrderState4Listener(chgOrderId, remark);
        }
        else {
            orderAbnormalDao.updateChangeOrderState(chgorderIds, chgOrderId, state, remark);
        }
    }

    /**
     * 异常单修改状态公共方法
     *
     * @param woId
     * @param woIdLst
     * @param state
     */
    public void updateExceptionWoStateCommon(String woId, List<String> woIdLst, String operStaffId, String state) {
        if (CollectionUtils.isEmpty(woIdLst)) {
            woIdLst = new ArrayList<String>();
            woIdLst.add(woId);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("dealUserId", operStaffId);
        map.put("woIds", woIdLst);
        map.put("woState", state);
        orderAbnormalDao.updateWoState(map);
    }

    /**
     * 异常单回单公共方法
     *
     * @param woId
     * @param woIds
     * @param operStaffId
     * @param remark
     */
    public void compExceptionWoCommon(String woId, List<String> woIds, String operStaffId, String remark) {
        if (CollectionUtils.isEmpty(woIds)) {
            woIds = new ArrayList<String>();
            woIds.add(woId);
        }
        for (String id : woIds) {
            FlowWoDTO flowWoDTO = new FlowWoDTO();
            flowWoDTO.setWoId(id);
            flowWoDTO.setRemark(remark);
            flowAction.complateWo(operStaffId, flowWoDTO);
        }
    }


    public String genExceptionChildOrder(Map<String, String> orderIdAndPsCode,
                                       String operStaffId, String woId) {
        //异常单的orderType固定为ORDER_EXCEPTION
        String objType = MapUtils.getString(orderIdAndPsCode, "OBJ_TYPE");
        String actType = MapUtils.getString(orderIdAndPsCode, "ACT_TYPE");
        String chgOrderId = MapUtils.getString(orderIdAndPsCode, "CHG_ORDER_ID"); //异常单父定单id
        String srcCldOrderId = MapUtils.getString(orderIdAndPsCode, "SRC_CLD_ORDER_ID"); //原单子流程定单id
        String srcAreaId = MapUtils.getString(orderIdAndPsCode, "AREA_ID"); //原单子流程区域id

        //创建异常单子流程
        FlowOrderSpecDTO flowOrderSpecDTO = new FlowOrderSpecDTO();
        flowOrderSpecDTO.setOrderType(OrderAbnormalConstant.EXCEPTION_ORDER_TYPE);
        flowOrderSpecDTO.setObjType(objType);
        flowOrderSpecDTO.setActType(actType);

        FlowOrderDTO flowOrderDTO = new FlowOrderDTO();
        flowOrderDTO.setOrderSpec(flowOrderSpecDTO);
        flowOrderDTO.setParentOrderId(chgOrderId);
        flowOrderDTO.setOrderTitle("追单子流程");
        flowOrderDTO.setAreaId(srcAreaId);


        //添加原单子流程对应的定单id。
        List<HashMap<String, String>> orderAttrs = new ArrayList<>();
        HashMap<String, String> orderAttr = new HashMap<>();
        orderAttr.put("KEY", "src_order_id");
        orderAttr.put("VALUE", srcCldOrderId);
        orderAttrs.add(orderAttr);
        flowOrderDTO.setOrderAttrs(orderAttrs);

        FlowOrderDTO orderDTO = flowAction.createOrder(operStaffId, flowOrderDTO);
        //将异常单电路调度环节工单状态改变
        updateExceptionWoStateCommon(woId, null,
                operStaffId, "290000110");
        return orderDTO.getOrderId();
    }
}
