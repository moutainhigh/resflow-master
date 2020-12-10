package com.zres.project.localnet.portal.flowdealinfo.data.dao;


import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface OrderDealDao {

    String querySrvOrdId(@Param("subscribeId") String subscribeId, @Param("serialNumber") String serialNumber, @Param("tradeId") String tradeId);

    List<Map> queryTacheInfo(@Param("tradeId") String tradeId);

    List<Map<String, Object>> selectOrderList(@Param("tradeIdReal") String tradeIdReal,
                                              @Param("serialNumber") String serialNumber);

    List<Map<String, Object>> selectOrderList2(@Param("tradeIdReal") String tradeIdReal,
                                               @Param("serialNumber") String serialNumber,
                                               @Param("serviceOfferId") String serviceOfferId);

    void updateOrdInfo(@Param("activeType") String activeType,
                       @Param("tradeIdReal") String tradeIdReal,
                       @Param("serialNumber") String serialNumber);

    void updateFinDate(@Param("requireCompleteCate") String requireCompleteCate,
                       @Param("orderId") String orderId);

    void addOrderNotice(Map<String, String> params);

    List<Map<String, Object>> queryOrderInfoList(@Param("svrOrderId") String svrOrderId);

    List queryOrderAttrInfoList(@Param("svrOrderId") String svrOrderId);

    List<Map<String, Object>> queryStaffInfoList(@Param("userId") String userId);

    List<Map> queryListDealCurrentUser(@Param("orderId") String orderId, @Param("activeType") String activeType);

    List<Map<String, Object>> queryOrderInfo();

    /**
     * 通过专业code查询名称
     *
     * @param param
     * @return
     */
    String querySpecNetMagPro(Map param);

    /**
     * 根据流程规格id查询 工单类型，对象类型，动作类型
     *
     * @param id
     * @return
     */
    Map<String, Object> findOrderCode(int id);

    /**
     * 根据环节查询按钮
     */
    public List<Map<String, String>> qryTacheButton(Map paramsMap);

    /**
     * 添加操作记录日志
     *
     * @param params
     */
    public void insertTrackLogInfo(Map params);

    /**
     * 查询当前操作人的相关信息
     *
     * @param userId
     * @return
     */
    public Map<String, Object> getOperStaffInfo(Integer userId);

    /**
     * 查询当前操作人的相关信息及是否查询子公司
     *
     * @param userId
     * @return
     */
    public Map<String, Object> getIsBraFiliAle(Integer userId);

    /*
     * 递归查询登陆用户归属分公司
     * @author ren.jiahang
     * @date 2019/3/7 20:54
     * @param userId
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    Map<String, Object> getParentDepInfo(Integer userId);

    /**
     * 更新受理人和时间--签收和释放签收
     */
    public int updateWoOrderStateById(Map updateMap);

    /**
     * 根据工单id查询订单详情
     *
     * @param woOrderId
     * @return
     */
    public Map qryOrderDetailInfoBywoOrderId(Long woOrderId);

    /**
     * 更新流程订单状态
     */
    public int updateOrderStateById(Map orderMap);


    /**
     * 更新业务订单状态
     *
     * @param srvOrderState
     * @param orderId
     * @retur
     */
    public int updateSrvOrderStateById(@Param("srvOrderState") String srvOrderState, @Param("orderId") Long orderId);

    /**
     * 获取定单的父id
     */
    public Map getParentOrder(String orderId);

    /**
     * 获取父订单正在执行的工单id
     */
    public Map getParentWoOrder(Map map);

    /**
     * 修改工单状态
     */
    public int updateWoOrderState(Map map);

    /**
     * 查询工单相关信息
     *
     * @param woId
     * @return
     */
    public Map qryOrderData(String woId);

    /**
     * 调单入库
     *
     * @param map
     * @return
     */
    public int insertDispatchOrder(Map map);

    /**
     * 业务订单信息查询 -- 本地网的单子
     *
     * @param orderId
     * @return
     */
    public Map qryCstOrderData(String orderId);

    /**
     * 业务订单信息查询 -- 二干下发的单子
     *
     * @param orderId
     * @return
     */
    public Map qryCstOrderDataFromSec(String orderId);

    /**
     * 查询二干来单主调据是否存在主调局
     * @param orderId
     * @param regionId
     * @return
     */
    //public String qryOrderMainEgRegion(@Param("orderId") String orderId,@Param("regionId") String regionId);

    /**
     * 查询部门组织信息
     *
     * @param orgId
     * @return
     */
    /*public Map getDeptInfo(Long orgId);*/

    /**
     * 查询父节点和兄弟节点的部门组织信息
     *
     * @param orgId
     * @return
     */
    public List<Map<String, Object>> getDeptInfoList(Long orgId);

    /**
     * 根据组织和岗位查询具体角色
     *
     * @param postId
     * @param ldapId
     * @return
     */
    public Map qryjobId(String postId, String ldapId);

    /**
     * 查询部门下的人员
     *
     * @param deptId
     * @param staffId
     * @return
     */
    public List<Map<String, Object>> getStaffInfoList(@Param("deptId") Long deptId, @Param("staffId") String staffId);

    public List<Map<String, Object>> getStaffInfoDeptListUnit(@Param("deptId") Long deptId, @Param("staffId") String staffId);

    public List<Map<String, Object>> getStaffInfoListT(@Param("deptId") Long deptId, @Param("staffId") String staffId, @Param("searchValue") String searchValue);

    /**
     * 查询部门下的子部门
     *
     * @param deptId
     * @return
     */
    public List<Map<String, Object>> getChildDepartInfoList(@Param("deptId") Long deptId);

    public List<Map<String, Object>> getChildDepartInfoListT(@Param("deptId") Long deptId, @Param("searchValue") String searchValue);

    /**
     * 更新工单状态
     *
     * @param woId
     * @param woState
     */
    public void updateWoStateByWoId(@Param("woId") String woId, @Param("woState") String woState);

    /**
     * 更新工单状态 更新工单创建时间
     */
    public void updateWoStateAndDateByWoId(Map param);

    /**
     * 更新工单状态和处理人
     *
     * @param woId
     * @param userId
     * @param woState
     */
    public void updateWoStateAndUserByWoId(@Param("woId") String woId, @Param("userId") String userId, @Param("woState") String woState);

    /**
     * 监听电路调度退单而来，修改
     *
     * @param woId
     * @param orderId
     * @param woState
     */
    public void updateWoDataByWoId(@Param("woId") String woId, @Param("orderId") String orderId, @Param("woState") String woState);

    /**
     * 转派后更新工单处理人信息
     *
     * @param wo_id
     * @return
     */
    public void updateDealUserByWoId(String wo_id);

    /**
     * 查询是否成功调过资源创建接口
     *
     * @param srvOrdId
     * @return
     */
    public List<Map<String, Object>> qryInterLog(@Param("srvOrdId") String srvOrdId, @Param("interName") String interName);

    /**
     * 入库调接口成功标识
     *
     * @param id
     * @return
     */
    public void updateRemarkByLogId(String id);

    /**
     * 获取当前用户所属分公司
     *
     * @param operStaffId
     * @return
     */
    Map<String, Object> getCurrentUserBranch(String operStaffId);

    /**
     * 根据codeType查询gom_BDW_code_info信息
     *
     * @param codeType
     * @return
     */
    public List<Map<String, Object>> qryCodeInfoBycodeType(String codeType);

    /**
     * 根据订单id查询srvOrderId
     *
     * @param orderId
     * @return
     */
    public Map<String, Object> qrysrvOrdIdByorderId(String orderId);

    /**
     * 二干下发的单子查询业务Id以及动作类型
     *
     * @param orderId
     * @return
     */
    public Map<String, Object> qrysrvOrdIdSecondByorderId(String orderId);

    /**
     * 查询主流程order_id
     * 根据gom_order.order_id查找PARENT_ORDER_ID,
     *
     * @param orderId
     * @return
     */
    public Map<String, Object> qryParentOrdIdByorderId(String orderId);


    /**
     * 查询父节点和兄弟节点的部门组织信息
     *
     * @param deMap
     * @return
     */
    public List<Map<String, Object>> getDeptInfoListRel(Map deMap);

    public List<Map<String, Object>> getDeptInfoListRelUnit(@Param("currentAreaId") String currentAreaId, @Param("currentOrgId") String currentOrgId);

    public List<Map<String, Object>> qrySearchPartInfoByVal(@Param("currentAreaId") String currentAreaId, @Param("searchVal") String searchVal);

    public List<Map<String, Object>> qrySearchPerInfoByVal(@Param("currentAreaId") String currentAreaId, @Param("searchVal") String searchVal);

    public List<Map<String, Object>> qrySearchContactsSel(Map<String, Object> params);

    public List<Map<String, Object>> qrySearchPostInfoByVal(@Param("currentAreaId") String currentAreaId, @Param("searchVal") String searchVal);

    public List<Map<String, Object>> qrySearchContacts(Map<String, Object> paramMap);

    public List<Map<String, Object>> qrySearchDepPullDown(Map<String, Object> paramMap);

    public List<Map<String, Object>> qrySearchDepPullDownSub(Map<String, Object> paramMap);

    public List<Map<String, Object>> qrySearchPostPullDown(Map<String, Object> paramMap);

    public List<Map<String, Object>> qrySearchPostPullDownSub(Map<String, Object> paramMap);

    public List<Map<String, Object>> qrySearchPerPullDown(Map<String, Object> paramMap);

    public List<Map<String, Object>> qrySearchPerSingleDown(Map<String, Object> paramMap);

    public int qrySearchContactsCount(Map<String, Object> paramMap);

    public void addSearchContacts(@Param("contactData") List<Map<String, Object>> contactData);

    public void deleteSearchContacts(Map<String, Object> params);

    /**
     * 更新schedu_num的值
     *
     * @param schedu_num
     * @param deptId
     */
    void updateScheduNum(@Param("schedu_num") String schedu_num, @Param("deptId") String deptId);

    /**
     * 根据区域ID查询对应省份分公司下的专业
     *
     * @param
     * @return
     */
    //List<Map<String, Object>> querySpecialtyConfig(@Param("area_id") String area_id, @Param("serviceId") String serviceId);
    List<Map<String, Object>> querySpecialtyConfig(Map<String, Object> params);

    /**
     * 查询该部门所在省份的org_id
     *
     * @param orgId
     * @return
     */
    Map<String, Object> getProviceOrg(Long orgId);

    Map<String, Object> getProviceOrgByParentId(Long orgId);

    /**
     * 补单，查询原调单信息
     *
     * @param param
     * @return
     */
    List<Map<String, Object>> queryDispatchInfoByCstOrdId(Map<String, Object> param);

    /**
     * 根据调单ID更改调单状态
     *
     * @param dispatchOrderId
     */
    void updateDispatchOrderState(String dispatchOrderId);

    /**
     * 查询电路AZ端信息  本地测试派单
     *
     * @param srvOrdId
     * @return
     */
    public Map<String, Object> qryAZAreaInfo(String srvOrdId);

    /**
     * 查询电路Z端信息   本地测试派单  MV/DIA/语音中继产品本地测试只有一端
     *
     * @param srcOrdId
     * @return
     */
    public Map<String, Object> qryZAreaInfo(String srcOrdId);

    /**
     * 电路主调局入库
     *
     * @param masterName
     * @return
     */
    public int insertMaster(@Param("masterName") String masterName, @Param("srvOrdId") String srvOrdId);

    /**
     * 二干下发的单子关联表主调局更新
     *
     * @param paramMap
     * @return
     */
    public int updateRelateInfoMaster(Map paramMap);

    /**
     * 查询电路受理区域
     *
     * @param srvOrdId
     * @return
     */
    public Map<String, Object> qryCircuitAreaInfo(String srvOrdId);

    public List<Map<String, Object>> qrySecondDataMakeList(Map paramMap);

    public List<Map<String, Object>> qrySecondResMakeList(Map paramMap);

    public Map<String, Object> qrySecondDataMakeRollBackList(Map paramMap);

    public List<Map<String, Object>> qrySubLocalTestDataList(Map paramMap);

    int qryGomOrderAttrByOrderIds(@Param("orderIds") String[] orderIds, @Param("attrId") String attrId, @Param("resConfig") String[] resConfig);

    public int qryInterResult(@Param("srvOrdId") String srvOrdId, @Param("interName") String interName);

    public int qryInterResultBak(@Param("srvOrdId") String srvOrdId, @Param("interName") String interName, @Param("flowId") String flowId);

    void updateSrvOrdState(@Param("srvOrdId") String srvOrdId, @Param("orderState") String orderState);

    void saveSpecialtyConfigInfo(Map map);

    Map<String, Object> queryConfigInfoBySrvOrdIdRes(Map paramMap);

    /*Map<String, Object> queryConfigInfoBySrvOrdId(@Param("srvOrdId") String srvOrdId,
                                                  @Param("cstOrdId") String cstOrdId,
                                                  @Param("orderId") String orderId);*/

    //void updateConfigInfoBySrvOrdId(Map paramMap);

    void updateConfigInfoBySrvOrdIdRes(Map paramMap);

    /**
     * 查询岗位，用于资源分配环节查询数据制作，资源施工和外线施工环节的岗位
     *
     * @param paramMaps
     * @return
     */
    public List<Map<String, Object>> qryJob(Map<String, Object> paramMaps);

    /**
     * 查询人员，用于资源分配环节查询数据制作环节的人员
     *
     * @param paramMaps
     * @return
     */
    public List<Map<String, Object>> qryUserByJob(Map<String, Object> paramMaps);

    /**
     * 数据制作，资源施工，外线施工派单人员入库
     *
     * @param params
     * @return
     */
    public int insertDispObj(Map params);

    /**
     * 数据制作，资源施工，外线施工环节更新岗位入库
     *
     * @param params
     * @return
     */
    public int updateDispObjConfig(Map params);

    /**
     * 数据制作，资源施工，外线施工派单人员状态更新
     *
     * @param orderId
     * @return
     */
    public int updateDispObjState(@Param("orderId") String orderId);

    /**
     * 查询之前入库的数据(已保存的)
     *
     * @param orderId
     * @return
     */
    public List<Map<String, Object>> qryDispObjByOrderId(@Param("orderId") String orderId);

    /**
     * 查询之前入库的数据()
     *
     * @param qryMap
     * @return
     */
    public List<Map<String, Object>> qryDispObjTache(Map<String, Object> qryMap);

    /**
     * 查询对应的岗位名称
     *
     * @param jobId
     * @return
     */
    public Map<String, Object> findJobName(@Param("jobId") String jobId);

    /**
     * 获取拼接调单标题所需的信息
     *
     * @param param
     * @return
     */
    Map<String, Object> getDispatchTitleInfo(Map param);

    /**
     * 查询派发了几条电路
     *
     * @param param
     * @return
     */
    Map<String, Object> queryNumToAppendTitle(Map param);

    /**
     * 根据电路ID查询拼接调单内容所需要的信息
     *
     * @param param
     * @return
     */
    List<Map<String, Object>> getCircuitInfo(Map param);

    /**
     * 专业配置信息保存完成后，更改订单数据的状态
     *
     * @param param
     */
    void updateStateBySrvOrdId(Map param);

    /**
     * 专业配置信息保存完成后，更改订单数据的状态---用户二干下发的单子
     *
     * @param orderId
     */
    void updateStateRelateBySrvOrdId(String orderId);

    /**
     * 查询跨域流程发往单位--用于退单省份查询
     *
     * @param srvOrdId
     * @return
     */
    public Map<String, Object> getProvinceName(@Param("srvOrdId") String srvOrdId);

    /**
     * 动态调度  --  查询之前配置要配发的专业和区域
     *
     * @param
     * @return
     */
    Map<String, Object> getDispSpecialtyObj(Map<String, String> param);

    public Map<String, Object> getDispSpecialtyObjByRes(@Param("srvOrdId") String srvOrdId, @Param("orderId") String orderId, @Param("newCreateResource") String newCreateResource);

    public Map qryProvinceA(Map paramsMap);

    public Map qryProvinceZ(Map paramsMap);

    public Map qryProvinceValue(Map paramsMap);

    /**
     * 获取子流程到达最后一个环节的数量
     *
     * @param orderId
     * @return
     */
    public Map<String, Object> qryChildFlowNumAtLast(@Param("orderId") String orderId);

    /**
     * 获取子流程的数量
     *
     * @param orderId
     * @return
     */
    public Map<String, Object> qryChildFlowNum(@Param("orderId") String orderId);

    /**
     * 获取所有子流程的数据
     *
     * @param map
     * @return
     */
    public List<Map<String, Object>> getAllChildFlowData(Map map);

    /**
     * 查询一个订单下子流程的不是撤单状态的数量
     *
     * @param map
     * @return
     */
    public List<Map<String, Object>> qryChildFlowState(Map map);

    /**
     * 查询是否存在兄弟节点   用于本地测试，联调测试环节
     *
     * @param map
     * @return
     */
    public List<Map<String, Object>> qryBrotherOrdId(Map map);

    /**
     * 查询所有的子流程
     *
     * @param map
     * @return
     */
    public List<Map<String, Object>> getListChildFlow(Map map);

    /**
     * 查询本地测试线条参数
     *
     * @param map
     * @return
     */
    public List<Map<String, Object>> qryAttrParams(Map map);

    /**
     * 根据工单查询是否回退
     *
     * @param woId
     * @return
     */
    public Map<String, Object> qryCrossWholeAttrParams(String woId);

    /**
     * 根据本地网订单id查询二干完工汇总是否退单
     * @param map
     * @return
     */
    //public List<Map<String,Object>> qryAttrSummaryCompleteParams(Map map);

    /**
     * 获取主流程的流程实例id
     *
     * @param orderId
     * @return
     */
    public Map<String, Object> getMainFlowPsId(String orderId);

    /**
     * 获取订单的所有环节id
     *
     * @param orderId
     * @return
     */
    public Map<String, Object> getTacheIds(String orderId);


    /**
     * 调单数据和业务订单数据关联
     *
     * @param paramMap
     */
    void updateSrvOrdInfo(Map paramMap);

    /**
     * 查询当前工单对应的定单下所有执行中的工单列表
     *
     * @param woId
     * @return
     */
    public List<Map<String, Object>> queryListByWoId(String woId);

    /**
     * 查询当前工单的兄弟工单中已完成的工单ID
     *
     * @param woId
     * @return
     */
    public List<Map<String, Object>> queryWoIdByWoId(String woId);

    /**
     * 查询父流程的操作动作类型  GOM_ORD_KEY_INFO表ACT_TYPE字段
     *
     * @param orderId
     * @return
     */
    public Map<String, Object> getOperActType(String orderId);

    /**
     * 如果是拆机查询上一个业务订单
     *
     * @param srvOrdId
     * @param cstOrdId
     * @return
     */
    public Map<String, Object> qryLastSrvOrder(@Param("srvOrdId") String srvOrdId, @Param("cstOrdId") String cstOrdId);

    /**
     * 查询二干本地关联的上一个业务订单
     *
     * @param instanceId
     * @param parentOrderId
     * @return
     */
    public Map<String, Object> qrySecLocalRelateInfo(@Param("instanceId") String instanceId, @Param("parentOrderId") String parentOrderId);

    /**
     * 查询工单的派单对象类型，派单对象，申请单编号
     *
     * @param woId
     * @return
     */
    public Map<String, Object> qryWoOrderDispObj(@Param("woId") String woId, @Param("typeFlag") String typeFlag);

    /**
     * 通过部门组织查询用户
     *
     * @param dispObjId
     * @return
     */
    public List<Map<String, Object>> qryUserByDept(@Param("dispObjId") String dispObjId);

    /**
     * 通过角色查询用户
     *
     * @param dispObjId
     * @return
     */
    public List<Map<String, Object>> qryUserByGroup(@Param("dispObjId") String dispObjId);

    /**
     * 通过id查询用户
     *
     * @param dispObjId
     * @return
     */
    public List<Map<String, Object>> qryUserById(@Param("dispObjId") String dispObjId);

    /**
     * 通过区域查询短信是否需要发送
     *
     * @param areaId
     * @return
     */
    public Map<String, Object> qryMsmSwitchByArea(@Param("areaId") String areaId);

    /**
     * 通过区域查询短信是否需要发送 --用于一干发起
     *
     * @param areaId
     * @return
     */
    public Map<String, Object> qryMsmSwitchByAreaAtOne(@Param("areaId") String areaId);

    /**
     * 查询单子的所属区域
     *
     * @param orderId
     * @return
     */
    public Map<String, Object> qryOrderArea(@Param("orderId") String orderId);

    /**
     * 查询工单状态
     *
     * @param woId
     * @return
     */
    public Map<String, Object> qryWoOrderState(@Param("woId") String woId);

    /**
     * 查询工单状态根据工单ID
     *
     * @param
     * @return
     */
    public Map<String, Object> qryWoStateByOrderId(@Param("orderId") String orderId);

    /**
     * 查询工单Id
     *
     * @param woId
     * @return
     */
    public String qryOrderIdByWoId(@Param("woId") String woId);

    /**
     * 查询调单信息
     *
     * @return
     */
    List<Map<String, Object>> queryDispatchInfoByDispatchIds(Map<String, Object> param);

    /**
     * 根据调单编号查询调单数据
     *
     * @param dispatchNum
     * @return
     */
    List<Map<String, Object>> queryDispatchInfoByDispatchNum(String dispatchNum);

    public int qryRollBackResult(@Param("srvOrdId") String srvOrdId, @Param("interName") String interName, @Param("orderId") String orderId);

    /**
     * 派发规则配置查询区域部门信息
     *
     * @param deptId
     * @return
     */
    public List<Map<String, String>> qryAreaDeptData(@Param("deptId") String deptId);

    public List<Map<String, String>> qryAreaDeptDataParent(@Param("deptId") String deptId);

    /**
     * 通过区域id数组查询所有的区域信息
     *
     * @param param
     * @return
     */
    public List<Map<String, String>> qryAreaDeptListData(Map<String, Object> param);

    /**
     * 根据dispatchId更新调单信息
     *
     * @param param
     */
    int updateDispatchOrderInfo(Map<String, Object> param);

    /**
     * 插入历史调单信息到调单历史表
     *
     * @param param
     * @return
     */
    int insertDispatchInfo(Map<String, Object> param);

    /*
     * 根据工单判断是否二干来单
     * @author ren.jiahang
     * @date 2019/5/31 11:04
     * @param wo_id
     * @return boolean
     */
    public boolean judgeIsSecondDra(String wo_id);

    public String getOrgNameByOrgId(String orgId);

    public String getSecondCheckWoId(Map<String, Object> mapParam);

    /**
     * 查询子流程定单专业和区域
     *
     * @param orderId
     * @return
     */
    List<Map<String, Object>> queryAlreadyChildFlowList(@Param("orderId") String orderId);

    /**
     * 查询保存的区域信息
     *
     * @param orderId
     * @param specialCode
     * @return
     */
    Map qryDispAreaBySpec(@Param("orderId") String orderId, @Param("specialCode") String specialCode);

    /**
     * 根据调单ID查询关联的所有订单ID
     *
     * @param dispatchOrderId
     * @return
     */
    Map<String, Object> querySrvOrdIdsByDispatchOrderId(@Param("dispatchOrderId") String dispatchOrderId);

    /**
     * 查询集客是否下发起止租通知
     *
     * @param srvOrdId
     * @return
     */
    int qryNoRentNum(@Param("srvOrdId") String srvOrdId);

    /**
     * 查询二干下发的单子的区域
     *
     * @param orderId
     * @param specialCode
     * @return
     */
    Map qryDispAreaFromSecondBySpec(@Param("orderId") String orderId, @Param("specialCode") String specialCode);

    /**
     * 确认异常通知
     *
     * @param updateMap
     */
    void affirmExceptionBySrvOrdId(Map<String, Object> updateMap);

    String qrySrvOrdStat(@Param("srvOrdId") String srvOrdId);

    /**
     * 根据流程定单ID查询流程定单
     *
     * @param params
     * @return
     */
    List<Map<String, Object>> getFlowOrderById(Map<String, Object> params);

    /**
     * 根据父流程定单ID查询子流程定单
     *
     * @param params
     * @return
     */
    List<Map<String, Object>> getFlowOrderByParentId(Map<String, Object> params);

    /**
     * 通过环节Id查询环节编码
     *
     * @param tacheId
     * @return
     */
    String getFLowTacheCodeById(String tacheId);

    /**
     * 根据deptId查询parent_id
     *
     * @param deptId
     * @return
     */
    Map<String, Object> queryDeptIdByParentDeptId(@Param("deptId") String deptId);

    /**
     * 根据srvOrdId查询客户信息
     *
     * @param srvOrdId
     * @return
     */
    Map<String, Object> querySrvInfoBySrvOrdId(String srvOrdId);

    /**
     * 根据cstOrdId查询业务受理区域
     *
     * @param cstOrdId
     * @return
     */
    Map<String, Object> queryHandleDeptId(String cstOrdId);

    /**
     * 根据deptId查询部门信息
     *
     * @param deptId
     * @return
     */
    Map<String, Object> getDeptInfoByDeptId(String deptId);

    /**
     * 根据deptId查询拼接调单的信息
     *
     * @param deptId
     * @return
     */
    Map<String, Object> queryDisInfoByDeptId(String deptId);

    /**
     * 查询二干下发的受理区域ID
     *
     * @param cstOrdId
     * @param srvOrdId
     * @param orderId
     * @return
     */
    Map<String, Object> queryRegionId(@Param("cstOrdId") String cstOrdId, @Param("srvOrdId") String srvOrdId, @Param("orderId") String orderId);

    /**
     * 根据cst_ord_id、srv_ord_id、order_id更新调单ID
     *
     * @param params
     */
    void updateDispatchOrderIdToRelateTable(Map<String, Object> params);

    /**
     * 查询关系表调单ID
     *
     * @param params
     * @return
     */
    Map<String, Object> queryDispatchOrderIdFromRelateTable(Map<String, Object> params);

    /**
     * 查询发送短信工单
     *
     * @param list
     * @return
     */
    /*List<Map<String, Object>> qryWoOrderDispObjList(@Param("list") List<String> list);*/

    /**
     * 查询异常单发送短信工单
     *
     * @param orderId
     * @return
     */
    List<Map<String, Object>> qryExpWoOrderDispObjList(String orderId);

    /**
     * 查询异常单发送短信工单---查询异常单子流程
     *
     * @param orderId
     * @return
     */
    List<Map<String, Object>> qryExpWoOrderChildDispObjList(String orderId);

    /**
     * 查询异常单发送短信工单---查询异常单子流程用父流程orderid
     *
     * @param orderId
     * @return
     */
    List<Map<String, Object>> qryExpWoOrderChildDispObjListByParentOrderId(String orderId);

    /**
     * 查询异常单发送短信工单---查询异常单父流程用子流程orderid
     *
     * @param orderId
     * @return
     */
    List<Map<String, Object>> qryExpWoOrderDispObjListByChildOrderId(String orderId);

    /**
     * 查询是不是异常单子流程
     *
     * @param orderId
     */
    public List<Map<String, Object>> gryIsExpWoOrderChild(String orderId);

    /**
     * 更新电路调度环节状态
     *
     * @param updateMap
     */
    void updateWoStateBySrvOrdId(Map<String, Object> updateMap);

    /**
     * 查询电路调度环节保存的专业区域信息
     *
     * @param srvOrdId
     * @return
     */
    Map<String, Object> qryDispInfoBySrvordInfo(@Param("srvOrdId") String srvOrdId, @Param("orderId") String orderId);

    /**
     * 查询子流程的专业、区域
     *
     * @param woId
     * @return
     */
    Map<String, Object> qryChildOrdDispInfo(@Param("woId") String woId);

    /**
     * 删除专业区域信息
     *
     * @param id
     */
    void deleteConfigById(String id);

    /**
     * 更新专业区域信息
     *
     * @param dispInfo
     */
    void updateConfigById(Map<String, Object> dispInfo);

    /**
     * 子流程回退后更新电路编号
     *
     * @param srvOrdId
     */
    void updateCircuitCode(String srvOrdId);

    /**
     * 查询已派发并且没有走到等待环节的子流程数量
     *
     * @param orderId
     * @return
     */
    int qryAllChildIsEnd(String orderId);

    /*
     * 发起页面受理区域
     * @author ren.jiahang
     * @date 2019/6/20 15:51
     * @param deMap
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    public List<Map<String, Object>> qryCreatePageHandleDep(Map deMap);

    /**
     * 更新订单业务表中的状态
     *
     * @param paramMap
     */
    void updateSrvStateBySrvOrdId(Map<String, Object> paramMap);

    /**
     * 根据调单号查询指定调单来源的调单信息
     *
     * @return
     */
    List<Map<String, Object>> queryDispatchInfoByDisNumAndSource(@Param("dispatchNum") String dispatchNum, @Param("dispatchSource") String dispatchSource, @Param("cstOrdId") String cstOrdId);

    /**
     * 更新二干订单业务关联关系表中的状态
     *
     * @param paramMap
     */
    void updateSecondSrvStateById(Map<String, Object> paramMap);

    /**
     * 查询存活的子流程
     *
     * @param woOrderMap
     * @return
     */
    int qrySurviveChildFlowNum(Map<String, Object> woOrderMap);

    /**
     * 通过order_Id查询父流程以及子流程环节和状态
     *
     * @param flowMap
     * @return
     */
    List<Map<String, Object>> qryParentSubStatusByOrderId(Map<String, Object> flowMap);

    /**
     * 二干下发到本地的单子，更新电路调度环节的工单状态为处理中
     *
     * @param updateWoMap
     */
    void updateSecondWoStateBySrvOrdId(Map<String, Object> updateWoMap);

    /**
     * 查询跨域全程调测环节的工单
     *
     * @param orderId
     * @return
     */
    public Map<String, Object> qryCrossTest(String orderId);

    /**
     * 查询环节名称
     *
     * @param tacheId
     * @return
     */
    public String qryTacheName(String tacheId);

    /**
     * 查询管理员
     *
     * @param staffId
     * @return
     */
    int queryAdminInfo(String staffId);

    /**
     * 查询完工汇总环节页面手动输入的全程报竣时间
     *
     * @param orderId
     * @return
     */
    public Map<String, Object> qryManualFullCompleteTime(String orderId);

    /**
     * 查询电路调度环节工单状态
     *
     * @param orderIdList
     * @return
     */
    public List<String> queryIfSupplementOrder(@Param("orderIdList") List<String> orderIdList);

    /**
     * 查询电路调度环节需要指定处理人的环节
     *
     * @param params
     * @return
     */
    List<Map<String, Object>> qryDealUserTacheConfig(Map<String, Object> params);

    List<Map<String, Object>> queryPurviewBystaffId(Map<String, Object> param);

    /**
     * 查询跨域全程调测处理中的工单
     *
     * @param srvOrdId
     * @return
     */
    Map<String, Object> qryCrossTestDoing(String srvOrdId);


    public int qryInterResultByAttrValue(@Param("srvOrdId") String srvOrdId, @Param("interName") String interName);

    public int qryProNumByUserId(@Param("userId") String userId, @Param("codeType") String codeType);

    /**
     * 资源呈现配置查询
     *
     * @param param
     * @return
     */
    List<Map<String, Object>> queryRouteInfoUrl(Map param);

    /**
     * 下发工建 资源核查信息查询
     *
     * @param params
     * @return
     */
    List<Map<String, Object>> qryResCheckInfo(Map<String, Object> params);

    /**
     * 查询要求完成时间
     *
     * @param woId
     * @return
     */
    Date qryReqFinDate(@Param("woId") String woId);

    /**
     * 查询工单的一些属性 --用于推送订单中心
     * @param woId
     * @return
     */
    /*Map<String, Object> qryWoOrderInfo(@Param("woId") String woId);*/

    /**
     * 查询省份信息
     *
     * @param areaId
     * @return
     */
    Map<String, Object> qryProvinceData(@Param("areaId") String areaId);

    /**
     * 查询az端数据
     *
     * @param srvOrdId
     * @param areaId
     * @return
     */
    Map<String, Object> qryAZPortInfo(@Param("srvOrdId") String srvOrdId, @Param("areaId") String areaId);

    /**
     * 查询地市数据
     *
     * @param areaId
     * @return
     */
    Map<String, Object> qryCityData(@Param("areaId") String areaId);

    /**
     * 转化产品编码
     *
     * @param serviceId
     * @return
     */
    Map qryServiceId(@Param("serviceId") String serviceId);

    /**
     * 递归查询所属省份
     *
     * @param orgId
     * @return
     */
    String qryBelongCompany(@Param("orgId") String orgId);

    /**
     * 更新工单状态
     */
    int updWoOrder(Map updateMap);

    /**
     * 插入创建子流程消息表
     *
     * @param insertMap
     * @return
     */
    int insertCreateChildFlow(Map<String, String> insertMap);

    /**
     * 查询要启子流程的订单
     *
     * @return
     */
    List<Map<String, String>> qryCreateChildFlow();

    /**
     * 修改要启子流程的状态
     *
     * @param orderId
     * @param state
     * @return
     */
    int updCreateChildFlowState(@Param("orderId") String orderId, @Param("state") String state);

    /**
     * 将启完流程的订单复制去历史表
     *
     * @param orderId
     * @return
     */
    int copyToCreateChildFlowHis(@Param("orderId") String orderId);

    /**
     * 删除消息表数据
     *
     * @param orderId
     * @return
     */
    int deleteCreateChildFlow(@Param("orderId") String orderId);

    /**
     * 更新日志
     *
     * @param childSpecials
     * @param woId
     * @return
     */
    int updOperLog(@Param("childSpecials") String childSpecials, @Param("woId") String woId);

    Map<String, Object> queryIfMainOrg(Map<String, Object> params);

    /**
     * 根据orderId查询流程订单信息
     *
     * @param orderId
     * @return
     */
    Map<String, Object> qryOrderInfoByOrderId(@Param("orderId") String orderId);

    /**
     * az 资源是否具备（集客字段） 等待工建反馈
     *
     * @param srvOrdId
     * @return
     */
    Map<String, Object> resExistence(@Param("srvOrdId") String srvOrdId);

    /**
     * 工建开通单关联是否有前期评估反馈
     *
     * @param srvOrdId
     * @return
     * @date 2020/09/17 10:40
     */
    List<Map<String, Object>> queryAssessResult(@Param("srvOrdId") String srvOrdId);

    /**
     * @Description 功能描述:
     * @Param: [params]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wang.gang2
     * @Date: 2020/9/30 10:53
     */
    Map<String,Object> queryConstructConf(Map<String,Object> params);
    /**
     * 资源配置完提交后将进行主流程及子流程工单消息推送操作
     *
     * @param params
     * @author wangsen
     */
    void writeOrderMessage(Map<String, String> params);

    /**
     * 查询满足条件的工单发出消息
     *
     * @param orderId 订单ID
     * @param woId    工单ID
     * @author wangsen
     */
    List<Map<String, Object>> queryChildFlow(@Param("orderId") String orderId, @Param("woId") String woId);

    /**
     * 查询满足条件的工单发出消息
     *
     * @param orderId 父ORDERID
     * @param woId    工单ID
     * @author wangsen
     */
    List<Map<String, Object>> queryDealFlow(@Param("orderId") String orderId, @Param("woId") String woId);

    /**
     * 消息列表分页查询
     *
     * @param params
     * @author wangsen
     */
    List<Map<String, Object>> queryMessageList(Map<String, Object> params);

    /**
     * 消息列表分页查询总数
     *
     * @param params
     * @author wangsen
     */
    int countMessageList(Map<String, Object> params);

    /**
     * 是否已发送消息
     *
     * @param orderId 订单ID
     * @param woId    工单ID
     * @author wangsen
     */
    int isSendMessage(@Param("orderId") String orderId, @Param("woId") String woId);

    /**
     * 更新消息
     *
     * @param orderId 订单ID
     * @param woId    工单ID
     * @param isDeal  消息状态
     * @author wangsen
     */
    void updateMessage(@Param("orderId") String orderId, @Param("woId") String woId, @Param("isDeal") String isDeal);
    /*
     * 查询是否为国际公司的配置区域
     * 入参：子流程的orderid
     * @author guanzhao
     * @date 2020/10/4
     *
     */
    int qryIfPopConfig(@Param("orderId") String orderId);

    /*
     * 查询是否为国际公司的配置区域
     * 入参：主流程的orderid
     * @author guanzhao
     * @date 2020/10/5
     *
     */
    int qryIfPopConfigView(@Param("orderId") String orderId);


    int updateOrdInfoById(@Param("srvOrderState") String srvOrderState,@Param("srvOrdId") String srvOrdId);

    /*
     * 查询国际pop的deptId
     * @author guanzhao
     * @date 2020/10/22
     *
     */
    String qryPopDeptId();

    List<Map<String, Object>> queryParallelRelated(@Param("srvOrdId") String srvOrdId);

    /**
     * 查询自动化核查结果详情
     * @param params
     * @return
     */
    Map<String, Object> qryAutoDetails(Map<String, Object> params);

    /***
     * 查询自动化核查列表信息
     * @param params
     * @return
     */
    List<Map<String, Object>> qryAutoChkList(Map<String, Object> params);
    /**
     * 根据orderId查询完工汇总是否指定到人的信息
     * @param orderId
     * @return
     */
    Map<String, Object> queryComplateInfo(@Param("orderId") String orderId);

    /**
     * 激活结果查询
     * @author wangsen
     * @date 2020/10/15 17:24
     * @return
     */
    List<Map<String, Object>> getActivateInfo(Map<String, Object> params);

    /**
     * 根据工单ID查询订单ID
     * @author wangsen
     * @date 2020/10/19 14:53
     * @return
     */
    String queryOrderByWoId(@Param("woId") String woId);

    /**
     * 根据工单ID查询产品ID
     * @author wangsen
     * @date 2020/10/19 14:53
     * @return
     */
    List<Map<String, Object>> queryProdId(@Param("orderId") String orderId);

    /**
     * 根据工单ID查询工单环节
     * @author wangsen
     * @date 2020/10/19 14:53
     * @return
     */
    String queryTacheCode(@Param("orderId") String orderId);

    /**
     * 根据工单重新专业
     * @author wangsen
     * @date 2020/10/19 15:56
     * @return
     */
    String querySpecName(@Param("woId") String woId);

    /**
     * 工单信息查询
     * @author wangsen
     * @date 2020/10/19 16:16
     * @return
     */
    List<Map<String, Object>> queryWoInfo(@Param("woId") String wo_id);

    /**
     * 查询客户信息
     * @author wangsen
     * @date 2020/10/19 17:39
     * @return
     */
    List<Map<String, Object>> queryCustInfo(@Param("srvOrderId") String srvOrderId);

    /**
     * 查询集客返回信息
     * @author wangsen
     * @date 2020/10/19 17:39
     * @return
     */
    String queryJikeInfo(@Param("srvOrderId") String srvOrderId, @Param("attrCode") String attrCode);

    /**
     * 查询集客返回信息 枚举值转化具体的值
     * @author CWY
     * @date 2020/10/19 17:39
     * @return
     */
    String queryJikeInfoEnumValue(@Param("srvOrderId") String srvOrderId, @Param("attrCode") String attrCode);

    /**
     * 查询资源返回信息
     * @author wangsen
     * @date 2020/10/19 17:39
     * @return
     */
    String queryResInfo(@Param("srvOrderId") String srvOrderId, @Param("attrCode") String attrCode);

    /** TODO
     * 查询资源返回IP信息
     * @author wangsen
     * @date 2020/10/19 17:39
     * @return
     */
    List<Map<String, Object>> queryIplistInfo(@Param("srvOrderId") String srvOrderId);

    /** TODO
     * 查询省份号线数据
     * @author wangsen
     * @date 2020/10/20 9:00
     * @return
     */
    List<Map<String, Object>> queryHaoxInfo(@Param("srvOrderId") String srvOrderId, @Param("attrCode") String attrCode);

    /**
     * 查询工单状态
     * @author wangsen
     * @date 2020/10/21 15:36
     * @return
     */
    String queryWoState(@Param("woId") String woId);
    /*
     * 查询业务子类型（商务专线） 和动作类型新开、移机、拆机
     * @Param:
     * @Return:
     */
    String qrySubType(@Param("srvOrdId") String srvOrdId);


    /**
     * 根据woId查询电路信息
     *
     * @param woId
     * @return
     */
    Map<String, Object> querySrvInfoByWoId(@Param("woId") String woId);

    /*
     * 根据主流程的orderId查询接入专业数据制作环节的工单
     * @Param:
     * @Return:
     */
    Map<String, Object> qryDateMakeWoId(@Param("orderId") String orderId);

    /**
     * 更新工单处理人信息
     *
     * @param wo_id
     * @return
     */
    public void updateDealUserIdByWoId(@Param("wo_id") String wo_id, @Param("dealUserId")String dealUserId);

    /*
     * 查询是否需要数据制作自动
     * @Param:
     * @Return:
     */
    int qryIsDataMakeAuto(@Param("orderId") String orderId);

    /*
     * 查询接入网是否自动配置
     * @Param:
     * @Return:
     */
    int qryAccessAutoConfig(@Param("orderId") String orderId);

    /*
     * 根据srvOrdId 和ATTR_code查询属性值，通用
     * @Param:
     * @Return:
     */
    String qryAttrValue(@Param("srvOrdId") String srvOrdId,  @Param("attrName") String attrName,@Param("attrCode") String attrCode);


    /**
     * 根据流程id,环节编码，工单状态查询需要的工单信息
     * @param param
     * @return
     */
    Map<String, Object> queryWoInfoByOrderId(Map<String, Object> param);

    List<Map <String, Object>> queryWoAndSpecialtyInfoByOrderId(String orderId);

    /**
     * @Description 功能描述:  查询二干调度/电路调度处理人
     * @Param: [cstOrdId, tacheCode]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wang.gang2
     * @Date: 2020/11/2 18:25
     */
    Map<String, Object> querydealUserInfo(@Param("cstOrdId")String cstOrdId, @Param("tacheCode")String tacheCode);

    /**
     * @Description 功能描述: 延期申请记录
     * @Param: [params]
     * @Return: void
     * @Author: wang.gang2
     * @Date: 2020/11/2 19:48
     */
    void insertPostponementApply(Map params);
    /**
     * @Description 功能描述: 延期申请记录 更改状态
     * @Param: [params]
     * @Return: void
     * @Author: wang.gang2
     * @Date: 2020/11/3 11:27
     */
    void updatePostponementApply(Map params);

    /**
     * @Description 功能描述: 查询延期申请信息
     * @Param: [params]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wang.gang2
     * @Date: 2020/11/3 11:44
     */
    List<Map <String, Object>> queryPostponementApply(Map params);

    List<Map <String, Object>> getAttachInfo(Map params);

    void deleteAttachFile(Map<String,Object> params);

    void updateFileState(Map<String,Object> fileInfo);

    /*
     * 查询是否派发过一干
     * @Param:
     * @Return:
     */
    public int qryIsSendOneDry(@Param("srvOrdId") String srvOrdId);

    /**
     * @Description 功能描述: 更新电路信息
     * @Param: [params]
     * @Return: void
     * @Author: wang.gang2
     * @Date: 2020/11/13 15:37
     */
    void updataCircuitInfo(Map<String,Object> params);

    /**
     * 跟进定单id查询专业名称
     * @param orderId
     * @return
     */
    String querySpecialtyNameByOrderId(String orderId);

    /**
     * 删除消息列表信息
     * @param messageId
     * @param orderId
     * @param woId
     * @return
     */
    int deleteMessageList(@Param("messageId") String messageId,@Param("orderId") String orderId, @Param("woId") String woId);

    /**
     * 编码转化
     * @param
     * @return
     */
    String querySwitchCode(@Param("code") String code, @Param("type") String type);

}
