package com.zres.project.localnet.portal.order.data.dao;


import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface OrderQueryDao {
   List<Map<String, Object>> queryWo(Map<String, Object> params);

   List<Map<String, Object>> queryExportWo(Map<String, Object> params);

   int countWo(Map params);

   List<Map<String, Object>> getAnnexInfo(@Param("woIds") List<Long> woIds);

   /**
    * 查询工单信息
    * @param woId
    * @return
    */
   public Map<String, Object> qryWoInfo(String woId);

   /**
    * 查询同一环节的工单id
    * @param orderId
    * @param tacheCode
    * @return
    */
   public List<Long> qryWoIdSameTache(@Param("orderId") String orderId, @Param("tacheCode") String tacheCode);

   /**
    * 查询下发本地单子本地测试环节的工单id
    * @param orderId
    * @param tacheCode
    * @return
    */
   public List<Long> qryToLocalTestWoId(@Param("orderId") String orderId, @Param("tacheCode") String tacheCode);

   /**
    * 查询当前处理已经完成的工单信息
    * @param param
    * @return
    */
   List<Map<String, Object>> queryWoInfoForHis(Map<String, Object> param);

   /**
    * 查询当前处理人历史工单总量
    * @param param
    * @return
    */
   int queryWoInfoCont(Map<String, Object> param);

   List<Map<String, Object>> exportWoOrderInfo(Map<String, Object> parram);

}
