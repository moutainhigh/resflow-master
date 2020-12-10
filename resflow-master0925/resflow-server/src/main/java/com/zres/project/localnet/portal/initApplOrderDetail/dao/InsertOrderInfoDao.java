package com.zres.project.localnet.portal.initApplOrderDetail.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author :ren.jiahang
 * @date:2018/12/22@time:15:12
 */
@Repository
public interface InsertOrderInfoDao {
    /**
     * 查询序列
     * @author ren.jiahang
     * @date 2018/12/26 10:40
     * @param tableName   表名
     * @return int
     */
    int querySequence(@Param("tableName") String tableName);
    /**
     * 插入客户信息 GOM_BDW_CST_ORD
     * @author ren.jiahang
     * @date 2018/12/26 10:41
     * @param map
     * @return java.lang.String
     */
    int  insertCustomerInfo(Map<String, Object> map);
    /**
     * 插入定单信息 GOM_BDW_SRV_ORD_INFO
     * @author ren.jiahang
     * @date 2018/12/26 10:41
     * @param map
     * @return java.lang.String
     */
    int insertOrderInfo(Map<String, Object> map);
    /**
     * 插入电路信息 gom_BDW_srv_ord_attr_info
     * @author ren.jiahang 
     * @date 2018/12/26 10:42
     * @param list  
     * @return java.lang.String  
     */
    int insertordAttrInfo(List<Map<String, Object>> list);
    /*
     * 查询申请单标题的拼接数据
     * @author ren.jiahang
     * @date 2019/1/28 14:19
     * @param map
     * @return java.util.Map
     */
    Map queryScheduNum(Map<String, Object>  map);
    /*
     * 通过数据库函数生成申请单编号
     * @author ren.jiahang
     * @date 2019/3/20 15:38
     * @param orgId
     * @return java.lang.String
     */
    String queryScheduNumFunction(String orgId);
    /**properties
     * 查询用户所在的组织
     * @param userId
     * @return
     */
    String queryOrgId(String  userId);
    Map<String,Object> queryOrgIdSecond(String  userId);


    int updateScheduNum(Map<String, Object> map);

    int updateInitScheduNum(Map<String, Object> map);

    String querySrvOrdId(String circuitCode);
    /*
     * 通过业务查询流程id
     * @author ren.jiahang
     * @date 2019/3/22 10:55
     * @param serId
     * @return java.lang.String
     */
    String queryOrderId(String serId);

    String queryWoIdByOrderId(String orderId);

    /*
     * 通过业务查询流程id
     * @author ren.jiahang
     * @date 2019/3/22 10:55
     * @param serId
     * @return java.lang.String
     */
    String queryFinishTime(String serId);

    /*
     * 更新竣工时间
     * @author cao.wenyang
     * @date 2019/7/15
     * @param finishTime
     * @return int
     */
    void updateFinishTime(@Param("finishTime")String finishTime,@Param("srd")String srvOrderId);

    /**
     * @Description 功能描述: 工建系统反馈结果
     * @Param: [attrCode, srvOrdId]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wang.gang2
     * @Date: 2020/9/17 20:28
     */
    List<Map<String,Object>> queryAttrInfo(@Param("attrCode")String attrCode,@Param("srvOrdId")String srvOrdId);
}
