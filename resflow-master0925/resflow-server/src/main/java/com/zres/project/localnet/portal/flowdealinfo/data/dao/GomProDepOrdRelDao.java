package com.zres.project.localnet.portal.flowdealinfo.data.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;


@Repository
public interface GomProDepOrdRelDao {

    /**
     * 保存专业、区域、订单关联表数据
     * @param proDepOrdlist
     */
    public void insertGomProDepOrdRel(@Param("proDepOrdlist") List<Map<String,Object>> proDepOrdlist);

    /**
     * 更新专业、区域、订单关联状态
     * @param params
     */
    public void updateSupOrderState(Map<String,Object> params);


}
