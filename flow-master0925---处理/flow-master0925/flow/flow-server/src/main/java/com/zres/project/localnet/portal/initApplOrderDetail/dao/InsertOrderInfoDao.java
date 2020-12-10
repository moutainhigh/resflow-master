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
     *
     * @param tableName 表名
     * @return int
     * @author ren.jiahang
     * @date 2018/12/26 10:40
     */
    int querySequence(@Param("tableName") String tableName);

    /**
     * 插入客户信息 GOM_BDW_CST_ORD
     *
     * @param map
     * @return java.lang.String
     * @author ren.jiahang
     * @date 2018/12/26 10:41
     */
    int insertCustomerInfo(Map<String, Object> map);

    /**
     * 插入定单信息 GOM_BDW_SRV_ORD_INFO
     *
     * @param map
     * @return java.lang.String
     * @author ren.jiahang
     * @date 2018/12/26 10:41
     */
    int insertOrderInfo(Map<String, Object> map);

    /**
     * 插入电路信息 gom_BDW_srv_ord_attr_info
     *
     * @param list
     * @return java.lang.String
     * @author ren.jiahang
     * @date 2018/12/26 10:42
     */
    int insertordAttrInfo(List<Map<String, Object>> list);

    /*
     * 查询申请单标题的拼接数据
     * @author ren.jiahang
     * @date 2019/1/28 14:19
     * @param map
     * @return java.util.Map
     */
    Map queryScheduNum(Map<String, Object> map);

    /*
     * 通过数据库函数生成申请单编号
     * @author ren.jiahang
     * @date 2019/3/20 15:38
     * @param orgId
     * @return java.lang.String
     */
    String queryScheduNumFunction(String orgId);

    /**
     * properties
     * 查询用户所在的组织
     *
     * @param userId
     * @return
     */
    String queryOrgId(String userId);

    Map<String, Object> queryOrgIdSecond(String userId);

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

    /*
     * 查询竣工时间
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
    int updateFinishTime(@Param("finishTime") String finishTime, @Param("srvOrderId") String srvOrderId);

    List<Map<String, Object>> queryAttrInfo(@Param("attrCode") String attrCode, @Param("srvOrdId") String srvOrdId);

    /**
     * @Description 功能描述: 工建az端金额要反馈到B域
     * @Param: [srvOrdId]
     * @Return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     * @Author: wang.gang2
     * @Date: 2020/9/18 11:36
     */
    List<Map<String, Object>> queryAmount(@Param("srvOrdId") String srvOrdId);

    /**
     * @Description 功能描述: 电路属性单个信息查询
     * @Param: [attrCode, srvOrdId]
     * @Return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     * @Author: wang.gang2
     * @Date: 2020/9/18 14:54
     */
    List<Map<String, Object>> queryAttrInfos(@Param("attrCode") String attrCode, @Param("srvOrdId") String srvOrdId);

    /**
     * @Description 功能描述: 省内dia自动开通激活反馈信息  装维人员工号、光猫机型、终端串号 等信息
     * @Param: [srvOrdId]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wang.gang2
     * @Date: 2020/10/10 15:50
     */
    Map<String, Object> queryActiveAttrInfo(@Param("srvOrdId") String srvOrdId);

}
