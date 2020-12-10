package com.zres.project.localnet.portal.initApplOrderDetail.dao;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author :ren.jiahang
 * @date:2018/12/22@time:15:12
 */
@Repository
public interface GetEnumDao {
    /**
     * 查询枚举值
     * @author ren.jiahang
     * @date 2019/1/5 11:32
     * @param enumCode  枚举值编码
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String, Object>> queryEnum(String enumCode);

    /**
     * 查询枚举值---------局内中继 电路用途 特殊处理取code_value 大于10000的
     * @author ren.jiahang
     * @date 2019/1/5 11:32
     * @param enumCode  枚举值编码
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String, Object>> queryEnum2(String enumCode);
    /**
     * 查询枚举值---------除局内中继 电路用途 特殊处理取code_value 小于10000的
     * @author ren.jiahang
     * @date 2019/1/5 11:32
     * @param enumCode  枚举值编码
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String, Object>> queryEnum3(String enumCode);
    /*
     * 查询流程实例
     * @author ren.jiahang
     * @date 2019/1/5 14:53
     * @param param
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String, Object>> queryProcessInst(Map param);

}
