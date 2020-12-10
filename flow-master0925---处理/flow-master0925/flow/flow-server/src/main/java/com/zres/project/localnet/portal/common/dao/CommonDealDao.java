package com.zres.project.localnet.portal.common.dao;

import org.apache.ibatis.annotations.Param;

import com.ztesoft.res.frame.flow.xpdl.model.TacheDto;

public interface CommonDealDao {

    /**
     * 查询环节
     *
     * @param tacheId
     * @return
     */
    TacheDto qryTacheDto(int tacheId);

    /**
     * 查询区域名称
     *
     * @param childConfig
     * @return
     */
    String qryAreaName(String childConfig);

    /**
     * 查询子流程是否存在，以及子流程订单状态
     *
     * @param orderId
     * @param childConfig
     * @param parentOrderCode
     * @return
     */
    String qryChildOrderState(@Param("orderId") String orderId,
                              @Param("childConfig") String childConfig,
                              @Param("parentOrderCode") String parentOrderCode);

}
