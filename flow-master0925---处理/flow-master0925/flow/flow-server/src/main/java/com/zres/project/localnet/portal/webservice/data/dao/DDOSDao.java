package com.zres.project.localnet.portal.webservice.data.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface DDOSDao {

        /*
         * 查询电路的SRV_ORD_ID和SERVICE_ID
         * @Param:
         * @Return:
         */
        List<Map<String,Object>> querySrvOrdId(Map<String, Object> map);
        /*
         * 根据orderId和环节ID查询工单信息
         * @Param:
         * @Return:
         */

        Map<String,Object> queryWoInfoByOrderAndTacheId(@Param("orderId")String orderId , @Param("tacheCode") String tacheCode);
        Map<String,Object> queryWoInfoByDDOS(@Param("orderId")String orderId,@Param("tacheCode") String tacheCode);

}
