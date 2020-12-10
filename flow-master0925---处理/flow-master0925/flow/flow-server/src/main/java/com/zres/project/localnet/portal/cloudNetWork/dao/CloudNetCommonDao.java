package com.zres.project.localnet.portal.cloudNetWork.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface CloudNetCommonDao {
    /**
     * 枚举值转换实际意义信息
     * @param value
     * @param type
     * @return
     */
    String enumTrans(@Param("value") String value, @Param("type") String type);

    /**
     * 日志信息入库
     * @param param
     * @return
     */
    int insertLogInfo(Map<String, Object> param);

    /**
     * 更新返回信息到日志表
     * @param msg
     * @param id
     */
    void updateLogInfo(@Param("msg") String msg, @Param("id") String id);

    /**
     * 查询配置的URL
     * @param codeType
     * @return
     */
    String queryUrlInfo(@Param("codeType") String codeType);
}
