package com.zres.project.localnet.portal.applpage.service;

import java.util.List;
import java.util.Map;

/**
 * @author :ren.jiahang
 * @date:2018/12/22@time:14:56
 */

public interface GetEnumIntf {
    List<Map<String, Object>> queryEnum(String enumcode);
    List<Map<String, Object>> queryEnum2(String enumcode);
    List<Map<String, Object>> queryEnum3(String enumcode);
    //通用方法，获取枚举值，入参：枚举值GOM_BDW_CODE_INFO_SECOND.CODE_TYPE
    List<Map<String, Object>> queryEnumItem(String enumcode);
    /*
     * 查询流程实例
     * @author ren.jiahang
     * @date 2019/1/5 14:54
     * @param param
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String, Object>> queryProcessInst(Map param);

    String queryTradeId();
}
