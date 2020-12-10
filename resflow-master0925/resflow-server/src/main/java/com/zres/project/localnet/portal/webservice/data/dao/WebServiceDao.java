package com.zres.project.localnet.portal.webservice.data.dao;


import com.zres.project.localnet.portal.webservice.dto.RenamCustInfoDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by jiangdebing on 2018/12/22.
 */
@Repository
public interface WebServiceDao {

    String selectActType(@Param("typeCode") String typeCode);

    void bachAddGomIdcSrvOrdAttrInfo(@Param("srvOrdAttrList") List<Map> srvOrdAttrList);

    String selectDeptId(@Param("areaCode") String areaCode);

    int querySequence(@Param("sequences") String sequences);
    void insertInterfLog(Map<String, Object> map);
    void updateInterfLog(Map<String, Object> map);
    void insertClobInfo(Map<String, Object> map);
    void insertInterfLogS(Map<String, Object> map);
    /**
     * 查询资源接口地址
     * Created by csq on 2018/12/25.
     */
    String queryUrl(@Param("type") String type);
    /**
     * 查询接口报文业务信息
     * Created by csq on 2018/12/25.
     */
    Map queryResInfo(@Param("id") String id);
    /**
     * 查询接口报文业务信息 --从二干下发的单子
     * @param id
     * @return
     */
    Map queryResInfoFromSec(@Param("id") String id);

    /**
     * 查询接口报文电路信息
     * Created by csq on 2018/12/27.,
     */
    List<Map<String, Object>> queryCirInfo(@Param("id") String id);
    /**
     * 报文入库
     * Created by csq on 2018/12/25.
     */
    void saveJson(Map<String, Object> map);

     void insertCstOrd(Map<String, Object> cstordMap);
     Map queryCodeInfo(@Param("codeType") String codeType, @Param("codeValue") String codeValue);
     void updateLogOrderNo(Map<String, Object> map);
     void updateOrderReqFinDate(Map<String, Object> map);
    void updateOrderReqFinDate2(Map<String, Object> map);

    void addGomIdcSrvOrdInfo(Map<String, Object> srvOrdInfo);

     void addGomIdcSrvOrdAttrInfo(Map<String, Object> srvOrdAttr);

     void addGomIdcSrvOrdAttrGrp(Map<String, Object> srvOrdGrp);

     void addGomIdcSrvOrdAttrGrpInfo(Map<String, Object> srvOrdAttrGrpInfo);

     void addGomIdcSrvOrdAttachInfo(Map<String, Object> attachInfo);

     List<Map<String, Object>> qryOrdPsId(Map<String, Object> map);
    /**
     * 报文数据入库
     * Created by csq on 2018/12/25.
     */
    void saveRetInfo(Map<String, Object> map);
    /**
     * 查询资源返回信息
     * Created by csq on 2018/12/27.
     */
    Map queryResRTInfo(@Param("id") String id, @Param("attr_action") String attr_action);

    /**
     * 查询SrvOrdId
     * Created by csq on 2018/1/7.
     */
    String querySrvOrdId(@Param("id") String id);

    public void deleteRes(@Param("srvOrdId") String srvOrdId,@Param("compId") String compId);

    /**
     * 配置资源返回信息入库
     * @param map
     */
    public void saveRes(Map<String, Object> map);
    /**
     * 根据order_id查询流程订单的属性
     * @param orderId
     * @return
     */
    public Map<String,Object> qryGomOrderAttr(@Param("orderId") String orderId,@Param("attrId") String attrId);

    /**
     * 根据子流程order_id 查询专业
     * @param orderId
     * @return
     */
    public Map<String,Object> qrypSecialtyCode(@Param("orderId") String orderId);

    /**
     * 受理区域：白沙县分公司--查找到海南省的org_id
     * @param regionCodeName
     * @return
     */
    String qryRegionCode(String regionCodeName);

    String qryCrmRegion(String id);

    String qryResCircuitCode(String srvOrdId);

    void updateResCircuitCode(@Param("attrInfoId") String attrInfoId, @Param("circuitNo") String circuitNo);

    void deleteResCircuitCode(@Param("srvOrdId") String srvOrdId);

    void deleteResAttrCode(@Param("srvOrdId") String srvOrdId, @Param("interfaceName") String interfaceName);
    void deleteResAttrCodeSupple(@Param("srvOrdId") String srvOrdId, @Param("interfaceName") String interfaceName);

    void deleteResRoute(@Param("srvOrdId")String srvOrderId, @Param("compId") String compId);

    void saveResRoute(Map<String, Object> map);

    void copyInterfInfo(@Param("srvOrdId") String srvOrdId, @Param("interfaceName") String interfaceName);

    void updateStateBySrvOrdId(String srvOrdId);

    void saveResCircuit(Map<String, Object> paramMap);

    void updateCirCodeBySrvOrdId(@Param("srvOrdId") String srvOrdId, @Param("circuitNo") String circuitNo);

    List<Map<String, Object>> qrySrvOrdIdByTradeId(@Param("tradeId") String tradeId,@Param("flowId") String flowId);

    List<Map<String, Object>> qryTacheInfo(@Param("orderId") String orderId);

    Map qryFinishOrdInfo(@Param("srvOrdId") String srvOrdId);

    int qryNumByOrderId(@Param("srvOrdId") String srvOrdId,@Param("orderId") String orderId);

    String qryCityName(@Param("cityCode") String cityCode);

    Map<String, Object> queryCodeInfoFromRelateTable(@Param("serviceId") String serviceId, @Param("codeType") String codeType);
    /**
     * 删除已配置的资源信息
     * @param srvOrdId
     */
    void deleteResInfoBySrvOrdId(String srvOrdId);

    /**
     * 删除已配置的资源路由信息
     * @param srvOrdId
     */
    void deleteResRouteBySrvOrdId(String srvOrdId);

    /**
     * 更新产品实例id
     * @param srvOrdId
     * @param prodInstId
     */
    void updateInstanceId(@Param("srvOrdId") String srvOrdId, @Param("prodInstId") String prodInstId);

    String qryRentTime(@Param("srvOrdId")String srvOrdId);
    /**
     * 资源补录查询报文参数
     * @param id
     * @return
     */
    Map queryResInfoBySupplement(@Param("id") String id);
    public void deleteResSupple(@Param("srvOrdId") String srvOrdId,@Param("compId") String compId);

    /**
     * 配置资源返回信息入库--资源补录
     * @param map
     */
    public void saveResSupple(Map<String, Object> map);
    void deleteResRouteSupple(@Param("srvOrdId")String srvOrderId, @Param("compId") String compId);

    void saveResRouteSupple(Map<String, Object> map);

    /**
     * 通過 業務號碼和業務訂單號查詢srvOrdId
     * @param tradeId
     * @param serialNumber
     * @return
     */
    List<Map<String,Object>> querySrvOrderList(@Param("tradeId") String tradeId, @Param("serialNumber") String serialNumber);
    /**
     * 查询上次竣工的调度单
     * @param instanceId
     * @return
     */
    List<Map<String,Object>> qrySrvOrdIdLast(@Param("instanceId") String instanceId);
    /**
     * 查询是否是补录单
     * @param srvOrdId
     * @return
     */
    int isResSupplement(@Param("srvOrdId")String srvOrdId);


    /**
     *配置查询
     * @param param
     * @return
     */
    List<Map<String, Object>> queryCodeInfo2(Map<String,Object> param);

    /**
     * 过户业务查询
     * @param serialNumber
     * @return
     */
    List<RenamCustInfoDTO> queryCustRenameList(@Param("serialNumber")String serialNumber);

    /**
     * 查询出变更信息记录的最大版本号
     * @param cstOrdId
     * @return
     */
    Map<String, Object> queryMaxCustInfoRenameVersion(@Param("cstOrdId") String cstOrdId);
    /**
     * 插入变更信息表
     * @param map
     * @return
     */
    int insertCustInfoRenameLog(Map<String, Object> map);

    void updateCustInfoRename(Map<String, Object> changeInfo);

    /**
     * ADD by wang.gang2
     * 通过srvOrdId查客户信息
     * @param srvOrdId
     * @return
     */
    Map<String,Object> queryCustInfo(String srvOrdId);

    Map<String, Object> conversionAreas(@Param("enumType") String enumType,@Param("oldValue") String oldValue,@Param("newValue") String newValue);

    Map<String, Object> conversionEnum(@Param("enumType") String enumType,@Param("oldValue") String oldValue,@Param("newValue") String newValue);
    /**
     * 通过业务申请单号查询所有srvOrdId
     * @param serialNumber
     * @return
     */
    List<Map<String,Object>> querySrvOrdList(@Param("serialNumber") String serialNumber);

    /**
     * 资源自动释放记录
     * @param disassemble
     */
    void insertDisassembleInfo(Map<String,Object> disassemble);

    /**
     * jike 是否存在开通单
     * @param
     */
    int existOpenOrder(@Param("serialNumber")String serialNumber,@Param("tradeId")String  tradeId);

    /**
     * 本地发起是否存在开通单
     * @param instanceId
     * @return
     */
    int existOpenOrderLocal(String instanceId);

    Map<String, Object> queryDisassembleInfo(@Param("serviceId") String serviceId,@Param("areaId") String areaId);

    /**
     * 是否是二干下发本地的单子
     * @param srvOrdId
     * @return
     */
    List<Map<String, Object>> queryBelongSystem(@Param("srvOrdId") String srvOrdId);

    /**
     * 核查单自动拆机 拆本地单子
     * 查询接口报文业务信息
     * @Date 2020/09/17
     */
    Map queryResInfoLocal(@Param("id") String id);

    /**
     * 核查但自动拆机 从二干下发本地的单子
     * 查询接口报文业务信息 --从二干下发的单子
     * @Date 2020/09/17
     * @param id
     * @return
     */
    Map querySecToLocalResInfo(@Param("id") String id);
    /**
     * 根据srvOrdId查询流程id
     * @param srvOrdId
     * @return
     */
    Map<String, Object> qryOrdPsIdBySrvOrdId(@Param("srvOrdId")String srvOrdId);
    /**
     * 查询电路属性code码以，分隔开
     * @author jdb
     * @date 2020/11/17 11:22
     * @param srvOrdId
     * @return java.lang.String
     */
    String querySrvAttrCode(@Param("srvOrdId")String srvOrdId);
    /**
     * 查询GOM_BDW_CODE_INFO_SECOND表和GOM_BDW_CODE_INFO表特定CODE_TYPE值的CODE_VALUE和CODE_CONTENT
     * @author jdb
     * @date 2020/11/17 11:25
     * @param param
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     */
    List<Map<String, Object>> queryTranscodingInfo(Map<String,Object> param);
    /**
     * 查询GOM_BDW_CODE_INFO_SECOND表和GOM_BDW_CODE_INFO表的CODE_TYPE值
     * @author jdb
     * @date 2020/11/17 11:23
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     */
    List<Map<String, Object>> queryTranscoding();
  /**
     * 添加建设完工推送记录
     * @param pushOrderList
     */
    void addProgressFinishInfo(@Param("pushOrderList") List<Map<String,Object>> pushOrderList);

    /**
     * 完工推送记录查询
     * @param srvOrdId
     * @return
     */

    Map<String,Object> queryProgressFinsh(@Param("srvOrdId")String srvOrdId);
     /**
     * ADD by wang.gang2
     * 通过申请单编号查询接口日志需要转发给工建系统
     * @param cstOrdId
     * @return
     */
    List<Map<String,Object>> queryInterfaceLog(@Param("cstOrdId")String cstOrdId,@Param("srvOrdId")String srvOrdId);

}

