package com.zres.project.localnet.portal.initApplOrderDetail.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * Created by tang.huili on 2019/2/20.
 */
@Repository
public interface CheckOrderInfoDao {

    /**
     * 查询电路编号的数量
     * @param circuitCode
     * @return
     */
    Map<String, Object> queryCircuitCodeNum(@Param("circuitCode") String circuitCode);

    /**
     * 新开校验电路编号唯一性
     * @param circuitCode
     * @return
     */
    int queryCircuitCount (@Param("circuitCode") String circuitCode);
    /*
     * 业务订单号唯一校验
     * @author ren.jiahang
     * @date 2019/4/10 15:32
     * @param params
     * @return int
     */
    int querySrvOriderCount(Map<String, Object> params);

    int querySrvNumberCount(Map<String, Object> params);

    List<Map<String, Object>> querySrvOrderState (Map<String, Object> params);
    //根据流程实例查询是否已拆机
    String queryIsTear (Map<String, Object> params);
    //根据产品类型和动作判断是否有在途单
    String queryisOnWay(Map<String, Object> params);
    //add by wang 在途单
    List<Map<String, Object>> querySrvOrderInfoState (Map<String, Object> params);
}
