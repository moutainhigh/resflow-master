package com.zres.project.localnet.portal.report.service;

import java.util.List;
import java.util.Map;

/**
 * 报表系统相关接口
 *
 * @author PangHao
 * @date 2019/5/10 : 18:54
 */
public interface ReportIntf {

    /**
     * 调单统计报表查询
     *
     * @param map 开始及结束时间
     * @return 查询结果
     * @author PangHao
     * @date 2019/5/10 : 18:56
     */
    Map<String, Object> dispatchOrderStatistics(Map<String, Object> map);

    /**
     * 通过类型及描述得到字典编码
     *
     * @param map 类型编码和描述
     * @return 类型编码
     * @author PangHao
     * @date 2019/5/13 : 16:15
     */
    String getDicCodeByContent(Map<String, Object> map);

    /**
     * 查询调单列表
     *
     * @param map 类型及分页信息
     * @return 调单列表
     * @author PangHao
     * @date 2019/5/13 : 16:42
     */
    Map<String, Object> queryDisOrderList(Map<String, Object> map);

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
     * 业务网络核查统计列表
     *
     * @param map 查询条件及分页信息
     * @return 分页数据
     * @author PangHao
     * @date 2019/5/20 : 17:11
     */
    Map<String, Object> businessNetworkVerification(Map<String, Object> map);

    /**
     * 报竣未起租电路统计
     *
     * @param map 查询条件及分页信息
     * @return 分页数据
     * @author PangHao
     * @date 2019/5/20 : 17:11
     */
    Map<String, Object> queryCompletionNotRented(Map<String, Object> map);


    /**
     * 电路汇总查询
     *
     * @param map 查询条件及分页信息
     * @return 分页数据
     * @author PangHao
     * @date 2019/6/26  20:02
     **/
    Map<String, Object> queryCircuitSummaryList(Map<String, Object> map);


}
