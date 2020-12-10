package com.zres.project.localnet.portal.localStandbyInfo.service;

import com.zres.project.localnet.portal.local.domain.BaseObject;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;


/**
 * @author :wang.g2
 * @description :
 * @date : 2018/12/21
 */
public interface OrderStandbyServiceIntf {

    /**
     * 查询客户订单--主单
     * @param params
     * @return
     */
    public Map<String, Object> qryCstOrdList(Map<String, Object> params);

    /**
     * 查询业务电路单 核查流程时（特殊：核查调度不选专业）
     * @param params
     * @return
     */
    public Map<String, Object> qrySrvOrdListCheck(Map<String, Object> params);
    /**
     * 查询业务电路单--流程订单  主流程时
     * @param params
     * @return
     */
    public Map<String, Object> qrySrvOrdList(Map<String, Object> params);

    /**
     * 查询业务电路单--流程订单  子流程时
     * @param params
     * @return
     */
    public Map<String, Object> qrySrvOrdChildList(Map<String, Object> params);

    public Map<String, Object> queryOrderInfo(Map<String, Object> params);

    public Map<String, Object> querySubOrderInfoColl(Map<String, Object> params);

    /**
     * 查询客户订单--主单的总数量
     * @param params
     * @return
     */
    /*public Map<String, Object> queryStandbyOrderCount(Map<String, Object> params);*/

    /**
     * 查询待办每个tab数量
     * @param params
     * @return
     */
    public Map<String, Object> queryStandbyOrderEachCount(Map<String, Object> params);

    public List<BaseObject> querySysDict(Map<String, Object> mapJson);

    public Map<String, Object> updateCollapsible(List<Map> blockInfo, Map<String, Object> updloadMap, MultiValueMap<String, MultipartFile> multiFileMap);

    public void updateCollapsibleTransactional(List<Map> blockInfo, Map<String, Object> updloadMap, MultiValueMap<String, MultipartFile> multiFileMap) throws Exception;

    public Map<String, Object> updateCollapsibleSingle(Map<String, Object> updloadMap);

    public Map<String, Object> uploadFiles(Map<String, Object> updloadMap, MultiValueMap<String, MultipartFile> multiFileMap);

    public List<Map<String, Object>> exportStandbyOrderData(Map<String, Object> excelData);

    /**
     * 查询导出的申请单信息
     * @param
     * @return
     */
    public Map<String, Object> qryCustInfo(String cstOrdId);

    public Map<String, Object> qrySrvOrderInfo(String cstOrdId);

    public Map<String, Object> qryDispatchOrderInfo(String cstOrdId);

    /**
     * 查询调单信息
     * @return
     */
    public List<Map<String, Object>> queryDispatchOrderInfo(Map<String, Object> params);

    public Map<String, Object> queryOrderCircuitInfo(Map<String, Object> params);

    public List<Map<String, Object>> queryCircuitInfo(Map<String, Object> orders);


    public int addCC(Map<String, Object> params);
    public int updateCC(Map<String, Object> params);
    public int delCC(Map<String, Object> params);
    /**
     * 跨域全程调测环节上传附件给集客
     * @param params
     * @return
     */
    public boolean upJiKeFtp(Map<String, Object> params);

    public Map<String, Object> queryAllStandbyOrderCount(Map<String, Object> params);

    /**
     * 查询二干的任务列表
     * @param params
     * @return
     */
    List<Map<String, Object>> queryTaskInfo(Map<String, Object> params);

    /**
     * 查询二干下发本地网的单子
     * @param params
     * @return
     */
    List<Map<String, Object>> querySecToLocalTaskInfo(Map<String, Object> params);

    /**
     * 异常单提交 保存说明以及附件
     * @param updloadMap
     * @param multiFileMap
     * @return
     */
    public Map<String, Object> insertAbnomrmalInfo(Map<String, Object> updloadMap, MultiValueMap<String, MultipartFile> multiFileMap);

}
