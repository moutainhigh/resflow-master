package com.zres.project.localnet.portal.report.dao;


import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 报表系统相关Dao
 *
 * @author PangHao
 * @date 2019/5/10 : 18:54
 */
@Repository
public interface ReportDao {


    /**
     * 调单统计报表查询
     *
     * @param map 开始及结束时间
     * @return 查询结果
     * @author PangHao
     * @date 2019/5/10 : 18:56
     */
    List<Map<String, Object>> dispatchOrderStatistics(Map<String, Object> map);

    /**
     * 通过类型及描述得到字典编码
     *
     * @param map 类型编码和描述
     * @return 类型编码
     * @author PangHao
     * @date 2019/5/13 : 16:15
     */
    List<String> getDicCodeByContent(Map<String, Object> map);

    /**
     * 查询调单总数
     *
     * @param map 查询参数
     * @return 调单总数
     * @author PangHao
     * @date 2019/5/14 : 9:57
     */
    int countDisOrder(Map<String, Object> map);

    /**
     * 查询调单数据
     *
     * @param map 分页及查询参数
     * @return 调单分页数据
     * @author PangHao
     * @date 2019/5/14 : 9:57
     */
    List<Map<String, Object>> queryDisOrderList(Map<String, Object> map);

    /**
     * 开通及时率统计报表
     *
     * @param map 查询条件
     * @return 报表数据
     * @author PangHao
     * @date 2019/5/17 : 10:15
     */
    List<Map<String, Object>> openTimeRateStatistics(Map<String, Object> map);

    /**
     * 枚举值字典查询
     *
     * @param map CODE_TYPE编码类型
     * @return 枚举值列表
     * @author PangHao
     * @date 2019/5/20 : 17:11
     */
    List<Map<String, Object>> queryEnum(Map<String, Object> map);

    /**
     * 业务电路信息查询（分页）
     *
     * @param params 分页及查询参数
     * @return 列表信息
     * @author PangHao
     * @date 2019/5/20 : 17:11
     */
    List<Map<String, Object>> querySrvOrdList(Map<String, Object> params);

    /**
     * 业务电路信息查询（总数）
     *
     * @param params 分页及查询参数
     * @return 列表信息
     * @author PangHao
     * @date 2019/5/20 : 17:11
     */
    Integer querySrvOrdCount(Map<String, Object> params);

    /**
     * 查询电路属性信息列表
     *
     * @param srvOrdId 业务订单id
     * @return 列表信息
     * @author PangHao
     * @date 2019/5/20 : 17:11
     */
    Map<String, Object> getAttrInfoBySrvId(@Param("srvOrdId") String srvOrdId);

    /**
     * 查询历史路由信息
     *
     * @param srvOrdId 业务订单id
     * @return 列表信息
     * @author PangHao
     * @date 2019/5/20 : 17:11
     */
    List<String> getRouteInfoBySrvId(@Param("srvOrdId") String srvOrdId);

    /**
     * 查询调单编码
     *
     * @param srvOrdId 业务订单id
     * @return 列表信息
     * @author PangHao
     * @date 2019/5/20 : 17:11
     */
    List<String> getDisOrderBySrvId(@Param("srvOrdId") String srvOrdId);


    /**
     * 报竣未起租电路统计（总数）
     *
     * @param params 查询条件及分页信息
     * @return 分页数据
     * @author PangHao
     * @date 2019/5/20 : 17:11
     */
    Integer queryCompletionNotRentedCount(Map<String, Object> params);

    /**
     * 报竣未起租电路统计（分页）
     *
     * @param params 查询条件及分页信息
     * @return 分页数据
     * @author PangHao
     * @date 2019/5/20 : 17:11
     */
    List<Map<String, Object>> queryCompletionNotRentedList(Map<String, Object> params);

    /**
     * 电路汇总查询（总数）
     *
     * @param params 查询条件及分页信息
     * @return 分页数据
     * @author PangHao
     * @date 2019/6/26  20:02
     **/
    Integer queryCircuitSummaryCount(Map<String, Object> params);

    /**
     * 电路汇总查询（分页）
     *
     * @param params 查询条件及分页信息
     * @return 分页数据
     * @author PangHao
     * @date 2019/6/26  20:02
     **/
    List<Map<String, Object>> queryCircuitSummaryList(Map<String, Object> params);

    /**
     * 通过电路编号得到对应开通流程信息(开通时间，调单号)
     *
     * @param circuitNo 电路编号
     * @return 开通电路信息
     * @author PangHao
     * @date 2019/6/27  10:25
     **/
    Map<String, Object> getDredgeDataByCircuitNo(@Param("circuitNo")String circuitNo);

    /**
     * 通过电路编号得到对应变更流程信息(开通时间（最新），调单号（拼接）)
     *
     * @param circuitNo 电路编号
     * @return 变更流程信息
     * @author PangHao
     * @date 2019/6/27  10:29
     **/
    Map<String, Object> getChangeDataByCircuitNo(@Param("circuitNo")String circuitNo);
}
