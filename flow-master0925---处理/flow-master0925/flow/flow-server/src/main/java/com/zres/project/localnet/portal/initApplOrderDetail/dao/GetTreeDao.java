package com.zres.project.localnet.portal.initApplOrderDetail.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

/**
 * @author :ren.jiahang
 * @date:2019/2/15@time:10:20
 */
@Repository
public interface GetTreeDao {
    public List<Map<String, Object>> getProvenceInfoList(Map deMap);
    public List<Map<String, Object>> getCityInfoList(Map deMap);
    public List<Map<String, Object>> getCountyInfoList(Map deMap);
    /*
     * 递归查询人员的省份区域
     * @author ren.jiahang
     * @date 2019/2/20 14:04
     * @param orgId
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    Map<String,Object> getProviceArea(String areaId);

    Map<String, Object> getAreaIdByName(String param);
    /*
     * 递归查询登陆用户归属分公司
     * @author ren.jiahang
     * @date 2019/3/7 20:54
     * @param userId
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    Map<String,Object> getParentDepInfo(Integer userId);


}
