package com.zres.project.localnet.portal.webservice.data.dao;

import com.zres.project.localnet.portal.webservice.dto.CustomerInfoDTO;
import com.zres.project.localnet.portal.webservice.dto.DispatchInfoDTO;
import com.zres.project.localnet.portal.webservice.dto.JiKeCustomInfoDTO;
import com.zres.project.localnet.portal.webservice.dto.JiKeProdAttrDTO;
import com.zres.project.localnet.portal.webservice.dto.JiKeProdInfoDTO;
import com.zres.project.localnet.portal.webservice.dto.ProdAttrDTO;
import com.zres.project.localnet.portal.webservice.dto.ProdInfoDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @Description:TODO
 * @Author:zhang.kaigang
 * @Date:2019/5/17 15:47
 * @Version:1.0
 */
@Repository
public interface ExceptionFlowDao {

    /**
     * 获取客户信息
     * @param cstOrdId
     * @return
     */
    CustomerInfoDTO queryCustomerInfoDTO(String cstOrdId);

    /**
     * 根据cstOrdId获取业务电路基本信息
     * @param cstOrdId
     * @return
     */
    List<ProdInfoDTO> queryProdInfoDTOListByCstOrdId(String cstOrdId);

    /**
     * 根据crmProdInstId获取电路基本信息
     * @param crmProdInstId
     * @param actType
     * @return
     */
    List<ProdInfoDTO> queryProdInfoDTOListByCrmProdInstId(@Param("crmProdInstId") String crmProdInstId,
                                                          @Param("actType") String actType,
                                                          @Param("onedryAreaCode") String  onedryAreaCode,
                                                          @Param("tradeTypeCode") String  tradeTypeCode);


    /**
     * 获取业务电路属性信息
     * @param srvOrdId
     * @return
     */
    List<ProdAttrDTO> queryProdAttrDTOList(String srvOrdId);

    /**
     * 获取调单信息
     * @param dispatchOrderId
     * @return
     */
    DispatchInfoDTO queryDispatchInfoDTO(String dispatchOrderId);

    /**
     * 查询出异常单变更信息记录的最大版本号
     * @param cstOrdId
     * @return
     */
    Map<String, Object> queryMaxChangeVersion(@Param("type") String type, @Param("cstOrdId") String cstOrdId);


    /**
     * 插入变更信息表
     * @param map
     * @return
     */
    int insertChangeOrderLog(Map<String, Object> map);

    /**
     * 查询历史属性表是否已存在
     * @param srvOrdId
     * @param attrValueName
     * @return
     */
    int queryProdAttrHis(@Param("srvOrdId") String srvOrdId, @Param("attrValueName") String attrValueName);

    /**
     * 将产品属性插入历史表
     * @param srvOrdId
     * @param attrValueName
     * @return
     */
    int insertProdAttrHis(@Param("srvOrdId") String srvOrdId, @Param("attrValueName") String attrValueName);

    /**
     * 更新产品属性表
     * @param srvOrdId
     * @param attrValueName
     * @param attrValue
     * @return
     */
    int updateProdAttr(@Param("srvOrdId") String srvOrdId,
                       @Param("attrValueName") String attrValueName,
                       @Param("attrValue") String attrValue
    );

    /**
     * 得到最新的变更信息集合
     * @param cstOrdId
     * @param maxVersion
     * @return
     */
    List<Map<String, Object>> queryLastChangeOrderLog(@Param("type") String type,
                                                      @Param("cstOrdId") String cstOrdId,
                                                      @Param("version") String maxVersion);


    /**
     * 查询客户历史表是否有数据
     * @param cstOrdId
     * @return
     */
    int queryCustomerInfoHis(@Param("cstOrdId") String cstOrdId);

    /**
     * 将客户定单信息转储到历史表中
     * @param cstOrdId
     * @return
     */
    int insertCustomerInfoHis(@Param("cstOrdId") String cstOrdId);

    /**
     * 更新客户订单信息
     * @param map
     * @return
     */
    int updateCustomerInfo(@Param("params") Map<String, String> map);

    /**
     * 查询调单信息历史表是否有数据
     * @param dispatchOrderId
     * @return
     */
    int queryDispatchInfoHis(@Param("dispatchOrderId") String dispatchOrderId);

    /**
     * 将调单定单信息转储到历史表中
     * @param dispatchOrderId
     * @return
     */
    int insertDispatchInfoHis(@Param("dispatchOrderId") String dispatchOrderId);

    /**
     * 更新调单信息
     * @param map
     * @return
     */
    int updateDispatchInfo(@Param("params") Map<String, String> map);

    /**
     * 查询产品信息历史表是否有数据
     * @param srvOrdId
     * @return
     */
    int queryProdInfoHis(@Param("srvOrdId") String srvOrdId);

    /**
     * 将产品信息转储到历史表中
     * @param srvOrdId
     * @return
     */
    int insertProdInfoHis(@Param("srvOrdId") String srvOrdId);

    /**
     * 更新产品信息
     * @param map
     * @return
     */
    int updateProdInfo(@Param("params") Map<String, String> map);

    /**
     * 根据定单ID得到act_type,obj_type,area_id
     * @param orderId
     * @return
     */
    Map<String, String> getOrderSpec(String orderId);

    /**
     * 根据srvOrdId和attrValueName得到属性对象
     * @param srvOrdId
     * @param attrValueName
     * @return
     */
    List<ProdAttrDTO> queryProdAttrDTOListByAttrValueName(@Param("srvOrdId") String srvOrdId,
                                                             @Param("attrValueName") String attrValueName);

    /**
     * 一干插入属性表--加急延期用
     * @param srvOrdId
     * @param attrValueName
     * @param attrName
     * @param attrValue
     * @return
     */
    int insertProdAttr(@Param("srvOrdId") String srvOrdId,
                         @Param("attrValueName") String attrValueName,
                         @Param("attrName") String attrName,
                         @Param("attrValue") String attrValue
                        );

    /**
     * 一干插入属性表--追单确认用
     * @param srvOrdId
     * @param attrValueName
     * @param attrValue
     * @return
     */
    int insertProdAttrForSure(@Param("srvOrdId") String srvOrdId,
                              @Param("attrValueName") String attrValueName,
                              @Param("attrValue") String attrValue
    );
    //-----------集客需要的方法

    /**
     * 根据tradeIdRela关联业务订单号获取电路基本信息
     * @param tradeIdRela
     * @return
     */
    List<JiKeProdInfoDTO> queryProdInfoDTOListByTradeId(@Param("tradeIdRela")String tradeIdRela,
                                                        @Param("serviceOfferId")String serviceOfferId);
    /**
     * 获取业务电路属性信息
     * @param srvOrdId
     * @return
     */
    List<JiKeProdAttrDTO> queryJiKeProdAttrDTOList(String srvOrdId);

    /**
     * 获取客户信息
     * @param cstOrdId
     * @return
     */
    JiKeCustomInfoDTO queryJiKeCustomerInfoDTO(String cstOrdId);

    /**
     * 根据propertyId得到中文描述propertyName
     * @param propertyId
     * @return
     */
    List<String> getPropertyNameById(String propertyId);

    /**
     * 根据cstOrdId获取业务电路基本信息
     * @param cstOrdId
     * @return
     */
    List<JiKeProdInfoDTO> queryJiKeProdInfoDTOListByCstOrdId(String cstOrdId);

    /**
     * 将客户定单信息转储到历史表中
     * @param cstOrdId
     * @return
     */
    int insertJiKeCustomerInfoHis(@Param("cstOrdId") String cstOrdId);

    /**
     * 将产品信息转储到历史表中
     * @param srvOrdId
     * @return
     */
    int insertJiKeProdInfoHis(@Param("srvOrdId") String srvOrdId);

    /**
     * 更新定单表的完成时间
     * @param srvOrdId
     * @param reqFinDate
     * @return
     */
    int updateOrderReqFinDate(@Param("srvOrdId") String srvOrdId,
                              @Param("reqFinDate") String reqFinDate
    );

    /**
     * 集客查询历史属性表是否已存在，只有追单是根据attrCode这样查
     *
     * @param srvOrdId
     * @param attrCode
     * @return
     */
    int queryJiKeProdAttrHis(@Param("srvOrdId") String srvOrdId, @Param("attrCode") String attrCode);

    /**
     * 集客将产品属性插入历史表
     *
     * @param srvOrdId
     * @param attrCode
     * @return
     */
    int insertJiKeProdAttrHis(@Param("srvOrdId") String srvOrdId, @Param("attrCode") String attrCode);

    /**
     * 集客更新产品属性表
     *
     * @param srvOrdId
     * @param attrCode
     * @param attrValue
     * @return
     */
    int updateJiKeProdAttr(@Param("srvOrdId") String srvOrdId,
                           @Param("attrCode") String attrCode,
                           @Param("attrValue") String attrValue
    );

    /**
     * 查询是枚举值的属性集合
     * @param propertyId
     * @return
     */
    List<Map<String, String>> getPropertyById(String propertyId);

    /**
     * 查询属性值的中文描述
     * @param codeType
     * @param codeValue
     * @return
     */
    List<String> getCodeContent(@Param("codeType") String codeType, @Param("codeValue") String codeValue);

    /**
     * 根据attrCode和srvOrdId获取业务电路属性信息
     * @param srvOrdId
     * @return
     */
    List<JiKeProdAttrDTO> queryJiKeProdAttrDTOListByAttrCode(@Param("srvOrdId") String srvOrdId,
                                                             @Param("attrCode") String attrCode);

    /**
     * 插入集客产品属性表
     * @param srvOrdId
     * @param attrCode
     * @param attrValue
     * @return
     */
    int insertJiKeProdAttr(@Param("srvOrdId") String srvOrdId,
                           @Param("attrCode") String attrCode,
                           @Param("attrValue") String attrValue
    );

    /**
     * 获取子流程定单id列表
     * @param pOrderId
     * @param orderState
     * @return
     */
    List<String> lstCldOrderIds(@Param("pOrderId") String pOrderId, @Param("orderState") String orderState);

    /**
     * 修改srv_ord_stat   状态
     * @param srvOrdId
     * @param srvOrdStat
     * @return
     */
    int updateSrvOrdState(@Param("srvOrdId")String srvOrdId ,@Param("srvOrdStat")String srvOrdStat);

    /**
     * 修改定单和工单状态
     * @param orderId
     * @return
     */
    int updStateCancelOrder(@Param("orderId")String orderId);
}
