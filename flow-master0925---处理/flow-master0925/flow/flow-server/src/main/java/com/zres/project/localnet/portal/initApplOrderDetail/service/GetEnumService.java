package com.zres.project.localnet.portal.initApplOrderDetail.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.applpage.service.GetEnumIntf;
import com.zres.project.localnet.portal.initApplOrderDetail.dao.GetEnumDao;
import com.zres.project.localnet.portal.initApplOrderDetail.dao.InsertOrderInfoDao;


/**
 * @author :ren.jiahang
 * @date:2018/12/22@time:14:59
 */
@Service
public class GetEnumService implements GetEnumIntf {
    @Autowired
    GetEnumDao getEnumDao;
    @Autowired
    InsertOrderInfoDao insertOrderInfoDao;

    /*
     * 发起页面查询枚举特殊方法----局内中继电路用途单独查询
     * [1743235]【江苏突击】【联通集团OSS2.0_本地调度】调度流程_局内电路申请，电路用途枚举值修改跟资源传输电路的电路用途统一。
     * @author  ren.leilei
     * @date 2019/10/11
     * @param enumCode
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    public List<Map<String, Object>> queryEnum2(String enumCode) {
        List<Map<String, Object>> maps = getEnumDao.queryEnum2(enumCode);
        Map<String, Object> enumCodeMap = new HashMap<String, Object>(); //异步查询后前台需要enumCode返回值，直接回传回去。
        enumCodeMap.put("enumCode", enumCode);
        maps.add(enumCodeMap);
        return maps;
    }

    /*
     * 发起页面查询枚举特殊方法----除局内中继外其他产品的电路用途单独查询
     * [1743235]【江苏突击】【联通集团OSS2.0_本地调度】调度流程_局内电路申请，电路用途枚举值修改跟资源传输电路的电路用途统一。
     * @author  ren.leilei
     * @date 2019/10/11
     * @param enumCode
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    public List<Map<String, Object>> queryEnum3(String enumCode) {
        List<Map<String, Object>> maps = getEnumDao.queryEnum3(enumCode);
        Map<String, Object> enumCodeMap = new HashMap<String, Object>(); //异步查询后前台需要enumCode返回值，直接回传回去。
        enumCodeMap.put("enumCode", enumCode);
        maps.add(enumCodeMap);
        return maps;
    }

    /*
     * 发起页面查询枚举特殊方法
     * @author ren.jiahang
     * @date 2019/1/5 14:56
     * @param enumCode
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    public List<Map<String, Object>> queryEnum(String enumCode) {
        List<Map<String, Object>> maps = getEnumDao.queryEnum(enumCode);
        Map<String, Object> enumCodeMap = new HashMap<String, Object>(); //异步查询后前台需要enumCode返回值，直接回传回去。
        enumCodeMap.put("enumCode", enumCode);
        maps.add(enumCodeMap);
        return maps;
    }

    /*
     * 查询枚举值通用方法
     * @author ren.jiahang
     * @date 2019/1/5 14:56
     * @param enumCode
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    public List<Map<String, Object>> queryEnumItem(String enumCode) {
        List<Map<String, Object>> maps = getEnumDao.queryEnum(enumCode);
        return maps;
    }

    /*
     * 查询流程实例
     * @author ren.jiahang
     * @date 2019/1/5 14:56
     * @param param codeType 流程类型（flow_local 本地） ；codeTypeName：产品编码；codeValue：操作类型；codeContent：订单类型；
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>流程规格id
     */
    public List<Map<String, Object>> queryProcessInst(Map param) {
        return getEnumDao.queryProcessInst(param);
    }

    @Override
    public String queryTradeId() {
        insertOrderInfoDao.querySequence("");
        //  String scheduNum = String.format("%04d", Integer.parseInt(schedu_num));
        return null;
    }
}
