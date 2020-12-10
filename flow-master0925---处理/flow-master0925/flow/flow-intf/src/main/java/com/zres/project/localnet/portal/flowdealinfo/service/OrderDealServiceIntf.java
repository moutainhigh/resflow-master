package com.zres.project.localnet.portal.flowdealinfo.service;

import java.util.List;
import java.util.Map;


public interface OrderDealServiceIntf {
    /**
     *
     * @param
     * @return
     */
    //public Map<String, Object> qryProvince(Map paramsMap);

    /**
     *
     * @param
     * @return
     */
    public Map<String, Object> queryOrderInfo();

    public Map<String, Object> qryProvinceName(Map<String, Object> paramsMap);

    /**
     * 通过专业code查询名称
     * @param param
     * @return
     */
    String querySpecNetMagPro(Map<String, Object> param);

    /**
     *
     * @param
     * @return
     */
    public Map<String, Object> createOrder(Map<String, Object> paramsMap);
    /**
     *环节流转统一提交方法
     * @param params
     * @return
     */
    public Map<String, Object> submitOrder(Map<String, Object> params) throws Exception;

    /**
     * 直接反馈核查信息
     * @param params
     * @return
     * @throws Exception
     */
    public Map<String, Object> feedBackDirect(Map<String, Object> params) throws Exception;
    /**
     * 启流程
     * @param params
     * @return
     */
    //public Map<String, Object> qryProvinceValue(Map paramsMap);
    public Map<String, Object> qryProvinceValue(Map<String, Object> params);

    /**
     * 二干电路完工汇总后，同时本地调度单子起租并汇总、归档
     * @param
     * @return
     */
    /*public Map<String, Object> sendSecondScheduleLTRes(String flagSys,Map<String, Object> resMap);*/

    /**
     * 退单
     * @param params
     * @return
     */
    public Map<String, Object> rollBackWoOrder(Map<String, Object> params);

    /**
     * 二干数据制作子流程退单：从专业数据制作完成退到专业数据制作环节
     */
    public void dataMakeSubProRollBack(String action,String dataMakeStr,String remark);

    /**
     * 1、本地网辅调局从起租环节退到本地测试
     * 2、本地网主调局从跨域全程调测退到本地测试
     */
    public void rentCrossToTestLocalRollBack(String action,String subLocalTestData,String remark,String woIdC);

    /**
     * 保存退单日志
     * @param orderId
     * @param woId
     * @param operStaffInfoMap
     * @param dealUserId
     * @param operStaffName
     * @param trackMessage
     * @param remark
     */
    //public void insertTrackLogInfo(String orderId,String woId,Map<String, Object> operStaffInfoMap,String dealUserId,String operStaffName,String trackMessage,String remark);

    /**
     * 回退工单
     * @param params
     * @return
     */
    public Map<String, Object> goBackOrder(Map<String, Object> params);

    /**
     * 派单
     * @param params
     * @return
     */
    public Map<String, Object> sendWoOrder(Map<String, Object> params);

    /**
     * 作废
     * @param params
     * @return
     */
    public Map<String, Object> disableOrder(Map<String, Object> params);

    /**
     * 转派
     * @param params
     * @return
     */
    public Map<String, Object> transferWoOrder(Map<String, Object> params);
    /**
     * 签收/释放签收工单
     * @param params
     * @return
     */
    public Map<String, Object> getFreeWoOrder(Map<String, Object> params);
    /**
     * 异常确认
     * @param params
     * @return
     */
    public Map<String, Object> affirmException(Map<String, Object> params);
    /**
     * 查询环节按钮
     * @param params
     * @return
     */
    public Map<String, Object> getTacheButton(Map<String, Object> params);

    /**
     * 查询可选区域
     * @return
     */
    public List qryDepart(Map<String, Object> params);

    /**
     * 通过部门查询
     * @param params
     * @return
     */
    public List qryDepartParent(Map<String, Object> params);

    /**
     * 转派人员、岗位、部门
     * @param params
     * @return
     */
    public List qrySearchOrgPerDepart(Map<String, Object> params);

    /**
     * 查询可新增的常用联系人
     * @param params
     * @return
     */
    public Map<String, Object> qrySearchOrgPerDepSingleDown(Map<String, Object> params);

    /**
     * 转办人员、岗位、部门查询
     * @param params
     * @return
     */
    public Map<String, Object> qrySearchOrgPerDepPullDown(Map<String, Object> params);


    /**
     * 转办岗位、部门查询人员
     * @param params
     * @return
     */
    public Map<String, Object> qrySearchOrgPerDepPullDownSub(Map<String, Object> params);

    /**
     * 添加联系人查询
     * @param params
     * @return
     */
    public List qrySearchContactsSel(Map<String, Object> params);

    /**
     * 搜索常用联系人
     * @param params
     * @return
     */
    public Map<String, Object> qrySearchContacts(Map<String, Object> params);

    /**
     * 新增常用联系人
     * @param params
     * @return
     */
    public Map<String, Object> addSearchContacts(Map<String, Object> params);

    /**
     * 删除常用联系人
     * @param params
     * @return
     */
    public Map<String, Object> deleteSearchContacts(Map<String, Object> params);

    /**
     * 转派查询组织树和人员
     * @return
     */
    public List qryChildNodeData(Map<String, Object> params);

    public List getStaffInfoDeptListUnit(Map<String, Object> params);

    public List qryChildNodeDataT(Map<String, Object> params);

    /**
     * 获取序列号
     * @return
     */
    public Map getsequenceNum(Map<String, Object> param);

    /**
     * 进行资源配置，返回报文，url等信息
     * @param params
     * @return
     */
    public Map<String,Object> resConfig(Map<String, Object> params);

    /**
     * 根据区域ID查询对应省份分公司下的专业
     * @param param
     * @return
     */
    List<Map<String, Object>> querySpecialtyConfig(Map<String, Object> param);

    /**
     * 补单，查询原调单信息
     * @param param
     * @return
     */
    Map<String, Object> queryDispatchInfoByCstOrdId(Map<String, Object> param);

    /**
     * 查询电路的主调选择时，az端和受理区域信息
     * @param srvOrdId
     * @return
     */
    public Map<String,Object> qryCircuitAreaInfo(String srvOrdId);

    /**
     * 跨域全程调测退单查询二干数据制作数据
     * @param param   * @return
     */
    public Map<String,Object> qrySecondDataMakeList(Map<String, Object> param);
    /**
     * 跨域全程调测退单查询二干数据制作数据
     * @param param
     * @return
     */
    public Map<String,Object> qrySecondResMakeList(Map<String, Object> param);

    /**
     * 跨域全程调测退单查询本地网起租环节数据
     * @param param   * @return
     */
    public Map<String,Object> qrySubLocalTestDataList(Map<String, Object> param);

    /**
     * 保存电路配置的专业信息
     * @param param
     * @return
     */
    public Map<String, Object> saveSpecialtyConfigInfo(Map<String, Object> param);

    /**
     * 光线资源分配、资源分配环节保存岗位
     * @param param
     * @return
     */
    public Map<String, Object> saveResConstructConfigInfo(Map<String, Object> param);

    /**
     * 光线资源分配、资源分配环节保存岗位
     */
    public Map<String, Object> saveResConstructConfigInfoTra(String orderId, List<Map<String, Object>> dataParamList);

    /**
     * 获取页面上传过来的派发对象
     * @param jobStr
     * @param userStr
     * @param tacheId
     * @param orderId
     * @return
     */
    public Map<String, Object> getListDispObjByString(String jobStr, String userStr, String tacheId, String orderId);

    /**
     * 查询电路的专业配置信息进行回显
     * @param param
     * @return
     */
    Map<String, Object> queryPropertyConfig(Map<String, Object> param);

    /**
     * 获取派发岗位
     * @return
     */
    public List<Map<String,Object>> qryJob(Map<String, Object> params);

    /**
     * 查询之前入库的数据
     * @return
     */
    /*public List<Map<String, Object>> qryDispObj(Map<String, Object> params);*/

    /**
     * 查询已保存的岗位数据
     * @param params
     * @return
     */
    public List<Map<String, Object>> qryDispObjByOrderId(Map<String, Object> params);

    /**
     * 查询电路调度保存的下游环节处理对象
     * @param params
     * @return
     */
    public List<Map<String, Object>> qryTacheDealUserByOrderId(Map<String, Object> params);

    /**
     * 查询核查反馈信息
     * @return
     */
    public Map<String, Object> queryCheckInfo(Map<String,Object> params);
    /**
     * 新查询核查反馈信息
     * @return
     */
    public Map<String, Object> queryCheckFeedBackInfoByWoId(Map<String,Object> params);

    /**
     * 查询跨域流程发往单位--用于退单省份查询
     * @param params
     * @return
     */
    public List<Map<String, Object>> getProvinceName(Map<String,Object> params);

    /**
     * 查询业务订单归属哪个系统
     * @param params
     * @return
     */
    public Map<String, Object> qrySrvOrderBelongSys(Map<String,Object> params);

    /**
     * 通过order_Id查询父流程psId
     * @param params
     * @return
     */
    public Map<String, Object> qryParentPsIdBySubOrderId(Map<String,Object> params);

    /**
     * 通过order_Id查询父流程以及子流程环节和状态
     * @param params
     * @return
     */
    public List<Map<String, Object>> qryParentSubStatusByOrderId(Map<String,Object> params);

    /**
     * 查询信息用来刷新调单标题和内容
     * @param param
     * @return
     */
    Map<String, Object> getDispatchInfo(Map<String, Object> param) throws Exception;

    /**
     * 保存核查反馈信息
     * @return
     */
    public Map<String, Object> saveCheckInfo(Map<String,Object> params);

    /**
     * 查询所有的子流程
     * @param params
     * @return
     */
    public Map<String, Object> getListChildFlow(Map<String,String> params);

    /**
     * 获取主流程的流程实例id
     * @param orderId
     * @return
     */
    public Map<String, Object> getMainFlowPsId(String orderId);

    /**
     * 调单入库
     * @param params
     */
    public String insertDispatchOrder(Map<String, Object> params)  throws Exception;

    /**
     * 短信发送
     * @param woId
     * @return
     */
    public Map<String, Object> qryUserObjByWoId(String woId,String typeFlag);

    /**
     * 查询调单信息
     * @param param
     * @return
     */
    Map<String, Object> queryDispatchInfoByDispatchIds(Map<String, Object> param);
    /**
     * 根据流程定单ID查询流程定单
     * @return
     */
    public List<Map<String, Object>> getFlowOrderById(Map<String, Object> params);

    /**
     * 根据父流程定单ID查询子流程定单
     * @return
     */
    public List<Map<String, Object>> getFlowOrderByParentId(Map<String, Object> params);

    /**
     * 通过环节Id查询环节编码
     * @param paramStr
     * @return
     */
    public String getFLowTacheCodeById(String paramStr);

    /**
     * 根据srvOrdId查询订单信息
     * @param param
     * @return
     */
    Map<String, Object> querySrvInfoBySrvOrdId(Map<String, Object> param);

    /**
     * 二干下发本地，从关系表中查询调单ID
     * @param param
     * @return
     */
    Map<String, Object> queryDispatchOrderIdFromRelateTable(Map<String, Object> param);

    /**
     * 查询工单列表发送短信
     *
     * @param woIdList
     * @return
     */
    //Map<String, Object> qryUserObjByWoIdsSendMsg(List<String> woIdList);

    /**
     * 查询当前操作人的相关信息
     * @param userId
     * @return
     */
    Map<String, Object> getOperStaffInfo(Integer userId);

    /**
     * 通过区域查询短信是否需要发送
     * @param areaId
     * @return
     */
    Map<String, Object> qryMsmSwitchByArea(String areaId);

    //发起页面受理区域查询
    public List qryCreatePageHandleDep(Map<String, Object> params);


    /**
     * 二干电路完工汇总到单执行业务汇总
     * @param
     * @return
     */
    public Map<String, Object> sendSecondScheduleLTResAssign(String flagSys,Map<String, Object> resMapSec);

    /**
     * 查询序列
     * @author cao.wenyang
     * @date 2019/7/15
     * @param tableName   表名
     * @return int
     */
    int querySequence(String tableName);

    /**
     * 一干异常单通知退单
     * @return
     */
    public Map<String, Object> exceptionOrderNoticeBack(Map<String, Object> params);

    /**
     * 二干电路完工汇总后，同时本地调度单子起租并汇总、归档
     * @param params
     * @return
     */
    public Map<String, Object> sendlocalScheduleLTRentRes(Map<String, Object> params);

    /**
     * 二干资源归档
     * @param params
     * @return
     */
    public Map<String, Object> sendSecondScheduleLTRes(Map<String, Object> params);

    /**
     * 环节日志
     * @param params
     */
    public void insertTacheLog(Map<String, Object> params);

    /**
     * 查询是否补单
     * @param orderIdStr
     * @return
     */
    public boolean queryIfSupplementOrder(String orderIdStr);
    /**
     * 查询电路调度环节需要指定处理人的环节
     * @param params
     * @return
     */
    public Map<String, Object> qryDealUserTacheConfig(Map<String, Object> params);

    /**
     * 有线条参数，提交工单公用方法
     * @param params key:woId  orderId  tacheId remark
     * @return
     * @throws Exception
     */
    public Map<String,Object> complateWoWithAttr(Map<String, Object> params) throws Exception;

    /**
     * 完工汇总环节提交前判断工建是否已完成建设
     * @return
     */
    public Map<String,Object> summaryBeforeCommit(Map<String, Object> params);
    /**
     * 核查汇总页面是否展示工建反馈费用信息
     * @param params
     * @return
     */
    public boolean showEnginInfo(Map<String, Object> params);
    /**
     * 查询资源核查信息
     * @return
     */
    public Map<String, Object> qryResCheckInfo(Map<String,Object> params);

    public void saveOpinionInfo(List<Map<String, Object>> params);

    public Map<String, Object> queryIfMainOrg(Map<String,Object> params);
    /**
     * @Description 功能描述:查询对接工建得省份 跟配置得核查下发工建得按钮配置区分开
     * @Param: [params]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wang.gang2
     * @Date: 2020/9/30 10:52
     */
    public Map<String, Object> queryConstructConf(Map<String,Object> params);

    /*
     * 查询是否为国际公司--页面上本地测试环节，国际公司默认通过，不能修改
     * @author guanzhao
     * @date 2020/10/5
     *
     */
    boolean qryIfPopConfigView(String orderId);
    /**
     * @Description 功能描述:  查询单个电路信息属性
     * @Param: [params]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wang.gang2
     * @Date: 2020/10/21 16:07
     */
    List<Map<String, Object>> queryAttrInfos(Map<String,Object> params);
    /**
     * 查询自动化核查信息
     * @param params
     * @return
     * @throws Exception
     */
    public Map<String, Object> queryAutoCheckInfo(Map<String, Object> params) throws Exception;

    public List<Map<String, Object>> getActivateInfo(Map<String, Object> params);

    Map<String, Object> sendOrder(Map<String, Object> map);

    /**
     * @Description 功能描述: 延期申请
     * @Param: [params]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wang.gang2
     * @Date: 2020/11/2 14:57
     */
    public Map<String, Object> postponementApply(Map<String, Object> params);
    /**
     * @Description 功能描述: 保存信息回显
     * @Param: [params]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wang.gang2
     * @Date: 2020/11/4 10:22
     */
    public Map<String, Object> queryApplySaveInfo(Map<String, Object> params);
    /**
     * @Description 功能描述:附件信息
     * @Param: [params]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wang.gang2
     * @Date: 2020/11/4 10:22
     */
    public List<Map<String, Object>> getAttachInfo(Map<String, Object> params);
    /**
     * @Description 功能描述:
     * @Param: [params]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wang.gang2
     * @Date: 2020/11/5 15:31
     */
    public Map<String, Object> feedBackToOneDry(Map<String, Object> params);

    /**
     * @Description 功能描述:批量延期申请
     * @Param: [params]
     * @Return: Map
     * @Author: cwy
     * @Date: 2020/11/10 15:31
     */
    public Map<String, Object> feedBackToOneDryBatch(Map<String, Object> params);

}
