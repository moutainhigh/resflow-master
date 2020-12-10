package com.zres.project.localnet.portal.order.data.dao;


import com.zres.project.localnet.portal.order.domain.GomDispatcherOrderPo;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public interface GomOrderQueryDao {

   /**
    * 调单查询
    * @param params
    * @return
    */
   public List<GomDispatcherOrderPo> queryGomDispatcherOrderPage(Map<String,Object> params);

   /**
    * 查询导出的数据
    * @param params
    * @return
    */
   public List<GomDispatcherOrderPo> queryGomDispatcherExportOrderData(Map<String,Object> params);

   /**
    * 调单查询数量
    * @param params
    * @return
    */
   public int queryGomDispatcherOrderCount(Map<String,Object> params);
}
