package com.zres.project.localnet.portal.webservice.flow;

import com.zres.project.localnet.portal.webservice.dto.*;

import java.util.List;
import java.util.Map;

/**
 * @Description:异常单处理接口
 * @Author:zhang.kaigang
 * @Date:2019/5/16 16:05
 * @Version:1.0
 */
public interface ExceptionFlowIntf {

    /**
     * 异常单接口
     * @param type 异常单类型：追单、挂起、解挂等，在一干用104-追单 108-加急 109-延期 110-挂起 111-解挂
     * @param newCustomerInfoDTO 发起异常单新传递的客户信息
     * @param newDispatchInfoDTO 发起异常单新传递的调单信息
     * @param newProdInfoDTOList 发起异常单新传递的电路信息
     */
    Map<String, Object> exceptionFlowChange(String type, CustomerInfoDTO newCustomerInfoDTO, DispatchInfoDTO newDispatchInfoDTO, List<ProdInfoDTO> newProdInfoDTOList);

    /**
     * 追单确认
     * @param cstOrdId
     */
    Map<String, Object> exceptionFlowSure(String cstOrdId);

    /**
     * 集客异常单接口
     * @param type 异常单类型：4A-追单 4B-加急 4C-延期4D-撤业务订单 4E-挂起4F-解挂;
     * @param newJiKeCustomInfoDTO 从集客过来的json字符串转换成的客户DTO信息
     * @param newJiKeProdInfoDTO 从集客过来的json字符串转换成的电路信息DTO列表，包括基本信息和属性信息
     * @return
     */
    Map<String, Object> jiKeExceptionFlowChange(String type, JiKeCustomInfoDTO newJiKeCustomInfoDTO,
                                                List<JiKeProdInfoDTO> newJiKeProdInfoDTO);



    /**
     * 测试异常单接口
     * http://localhost:8088/resflow/common/service/callServerFunction.spr?_callFunParams=["com.zres.project.localnet.portal.webservice.flow.ExceptionFlowIntf","testExceptionFlowChange"]
     */
    void testExceptionFlowChange(Map<String, String> param);

    /**
     * 测试追单确认接口
     * http://localhost:8088/resflow/common/service/callServerFunction.spr?_callFunParams=["com.zres.project.localnet.portal.webservice.flow.ExceptionFlowIntf","testExceptionFlowSure"]
     */
    void testExceptionFlowSure();

    /**
     * 测试集客异常单接口
     */
    void testJiKeExceptionFlowChange();

    /**
     * 添加日志记录
     * @param srvOrdId
     * @param message
     * @param operType
     */
    void insertLogInfo(String srvOrdId, String message, String operType);
    /**
     * 解析追单数据用于反馈接口或者退单接口
     * @param srvOrdId
     * @param changeData
     * @return
     */
    Map<String,Object> parseChangeData(String srvOrdId, String changeData);
}
