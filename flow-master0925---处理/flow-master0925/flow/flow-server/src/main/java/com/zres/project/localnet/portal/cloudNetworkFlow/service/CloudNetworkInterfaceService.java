package com.zres.project.localnet.portal.cloudNetworkFlow.service;

import java.util.HashMap;
import java.util.Map;

import com.zres.project.localnet.portal.cloudNetWork.service.*;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.zres.project.localnet.portal.cloudNetWork.service.TerminalNumReoprtIntf;
import com.zres.project.localnet.portal.cloudNetworkFlow.CloudNetworkInterfaceServiceIntf;
import com.zres.project.localnet.portal.cloudNetworkFlow.dao.WoOrderDealDao;
import com.zres.project.localnet.portal.cloudNetworkFlow.entry.CloudFeedbackIntfWoOrder;
import com.zres.project.localnet.portal.cloudNetworkFlow.util.IntfEnumUtil;
import com.zres.project.localnet.portal.common.CloudFlowFeedbackIntf;
import com.zres.project.localnet.portal.common.dao.CommonDealDao;
import com.zres.project.localnet.portal.common.util.FlowTacheUtil;
import com.zres.project.localnet.portal.common.util.TacheIdEnum;
import com.zres.project.localnet.portal.util.OrderTrackOperType;
import com.zres.project.localnet.portal.util.SpringContextHolderUtil;

import com.ztesoft.res.frame.flow.xpdl.model.TacheDto;

/**
 * @Classname CloudNetworkInterfaceService
 * @Description 云组网业务流程与云网平台的接口交互
 * @Author guanzhao
 * @Date 2020/11/9 10:10
 */
@Service
public class CloudNetworkInterfaceService implements CloudNetworkInterfaceServiceIntf {

    Logger logger = LoggerFactory.getLogger(CloudNetworkInterfaceService.class);

    @Autowired
    private IpranAutoActivationIntf ipranAutoActivationIntf;
    @Autowired
    private IpranRoutConfigIntf ipranRoutConfigIntf;
    @Autowired
    private IpranManagerVerifyIntf ipranManagerVerifyIntf;
    @Autowired
    private IpranCollectionIntf ipranCollectionIntf;
    @Autowired
    private IpranPortSpeedLimitIntf ipranPortSpeedLimitIntf;
    @Autowired
    private PortSelectionIntf portSelectionIntf;
    @Autowired
    private PortAutoAllocationIntf portAutoAllocationIntf;
    @Autowired
    private PortDataDeletionIntf portDataDeletionIntf;
    @Autowired
    private PortInfoQueryIntf portInfoQueryIntf;
    @Autowired
    private TerminalNumReoprtIntf terminalNumReoprtIntf;
    @Autowired
    private LocalTestIntf localTestIntf;
    @Autowired
    private ServiceConfigNotifiIntf serviceConfigNotifiIntf;
    @Autowired
    private FullBusinessTestIntf fullBusinessTestIntf;
    @Autowired
    private WorkStatusQueryIntf workStatusQueryIntf;
    @Autowired
    private TerminalBoxOfflineIntf terminalBoxOfflineIntf;
    @Autowired
    private OrderFeedBackIntf orderFeedBackIntf;
    @Autowired
    private CloudNetworkFinishOrderIntf cloudNetworkFinishOrderIntf;
    @Autowired
    private WoOrderDealDao woOrderDealDao;
    @Autowired
    private CommonDealDao commonDealDao;

    @Override
    public Map<String, Object> callCloudNetworkInterface(Map<String, Object> intfParamMap) throws Exception {
        logger.info("-----流程调用----------与云网平台交互接口-----" + intfParamMap.toString() + "------------------");
        Map<String, Object> resMap = new HashMap<>();
        try {
            String orderId = MapUtils.getString(intfParamMap, "orderId");
            //String tacheCode = MapUtils.getString(intfParamMap, "tacheCode");
            String intfCode = MapUtils.getString(intfParamMap, "intfCode");
            Map<String, Object> srvOrdData = woOrderDealDao.qrySrvOrdDataByOrderId(orderId);
            if (MapUtils.isEmpty(srvOrdData)){
                //如果是空，说明是子流程环节
                Map<String, Object> parentOrderAndRegion = woOrderDealDao.qryParentOrderAndRegion(orderId);
                String parentOrderId = MapUtils.getString(parentOrderAndRegion, "PARENT_ORDER_ID");
                srvOrdData = woOrderDealDao.qrySrvOrdDataByOrderId(parentOrderId);
            }
            //业务号码 电路明细编号
            intfParamMap.put("serialNumber", MapUtils.getString(srvOrdData, "SERIAL_NUMBER"));
            //业务类型 产品类型 busi_type
            intfParamMap.put("serviceId", MapUtils.getString(srvOrdData, "SERVICE_ID"));
            intfParamMap.put("srvOrdId", MapUtils.getString(srvOrdData, "SRV_ORD_ID"));
            String intfName = "";
            switch (intfCode) {

                case IntfEnumUtil.YZW_IPRANINACTIVECALL:  //2.4 IPRAN端口数据删除接口
                    intfName = "IPRAN端口数据删除";
                    resMap = portDataDeletionIntf.portDataDeletion(intfParamMap);
                    break;
                case IntfEnumUtil.YZW_ONLINECALL:  //2.5 终端盒序列号上报接口
                    intfName = "终端盒序列号上报";
                    resMap = terminalNumReoprtIntf.terminalNumReport(intfParamMap);
                    break;
                case IntfEnumUtil.YZW_PORTALLOCATION:  //2.6 下联端口自动分配接口
                    intfName = "下联端口自动分配";
                    resMap = portAutoAllocationIntf.portAutoAllocation(intfParamMap);
                    break;
                case IntfEnumUtil.YZW_PORTLISTQUERY:  //2.7	下联端口查询
                    intfName = "下联端口查询";
                    resMap = portInfoQueryIntf.portInfoQuery(intfParamMap);
                    break;
                case IntfEnumUtil.YZW_PORTCONFIRM:  //2.8 下联端口选择接口
                    intfName = "下联端口选择";
                    resMap = portSelectionIntf.portSelection(intfParamMap);
                    break;
                case IntfEnumUtil.YZW_LOCALTEST:  //2.9 本地业务测试接口
                    intfName = "本地业务测试";
                    resMap = localTestIntf.localBusinessTest(intfParamMap);
                    break;
                case IntfEnumUtil.YZW_IPRANROUTECALL:  //2.10 IPRAN业务路由配置下发通知接口
                    intfName = "IPRAN业务路由配置下发通知";
                    resMap = ipranRoutConfigIntf.routConfigNotifi(intfParamMap);
                    break;
                case IntfEnumUtil.YZW_IPRANQOSCALL:  //2.11 IPRAN端口限速下发通知接口
                    intfName = "IPRAN端口限速下发通知";
                    resMap = ipranPortSpeedLimitIntf.portSpeedLimit(intfParamMap);
                    break;
                case IntfEnumUtil.YZW_CONFIGCALL:  //2.12 业务配置下发通知接口
                    intfName = "业务配置下发通知";
                    resMap = serviceConfigNotifiIntf.serviceConfigNotifi(intfParamMap);
                    break;
                case IntfEnumUtil.YZW_WHOLETESTCALL:  //2.13 全程业务测试通知接口
                    intfName = "全程业务测试通知";
                    resMap = fullBusinessTestIntf.fullBusinessTest(intfParamMap);
                    break;
                //TODO：IntfEnumUtil.YZW_FINISHORDER--反馈接口用于全程测试报竣反馈
                case IntfEnumUtil.YZW_OFFLINECALL:  //2.14	终端盒下线通知接口
                    intfName = "终端盒下线通知";
                    resMap = terminalBoxOfflineIntf.terminalBoxOffline(intfParamMap);
                    break;
                case IntfEnumUtil.YZW_FINISHORDER:  //反馈接口用于全程测试报竣反馈
                    intfName = "反馈接口";
                    resMap = cloudNetworkFinishOrderIntf.finishOrder(intfParamMap);
                    break;
                case "ipranManagerVerify":  //
                    intfName = "IPRAN资源网管验证";
                    resMap = ipranManagerVerifyIntf.ipranManagerVerify(intfParamMap);
                    break;
                case "ipranCollection":  //
                    intfName = "IPRAN资源网管采集";
                    resMap = ipranCollectionIntf.ipranCollection(intfParamMap);
                    break;
                case "ipranAutoActivation":  //
                    intfName = "IPRAN资源网管自动激活";
                    resMap = ipranAutoActivationIntf.ipranAutoActivation(intfParamMap);
                    break;


                case "orderFeedBack":  //
                    intfName = "退单通知";
                    resMap = orderFeedBackIntf.orderFeedBack(intfParamMap);
                    break;
                case "workStatusQuery":  //
                    intfName = "工单状态查询";
                    resMap = workStatusQueryIntf.workStatusQuery(intfParamMap);
                default:
                    break;
            }
            if (MapUtils.getBoolean(resMap, "success")) {
                resMap.put("message", "调用与云网平台的" + intfName + "接口成功！");
            }
            else {
                String message = MapUtils.getString(resMap, "msg");
                resMap.put("message", "调用与云网平台的" + intfName + "接口失败！");
                throw new RuntimeException("调用与云网平台的" + intfName + "接口失败！" + message);
            }
        }
        catch (Exception e) {
            logger.error("调用与云网平台的接口失败：" + e);
            throw e;
        }
        return resMap;
    }




    @Override
    public Map<String, Object> cloudNetworkFeedbackInterface(Map<String, Object> intfParamMap) throws Exception {
        logger.info("------------------云网平台反馈接口--------------------");
        Map<String, Object> resMap = new HashMap<>();
        String serialNumber = MapUtils.getString(intfParamMap, "serialNumber");
        Map<String, Object> qryParam = new HashMap<>();
        qryParam.put("serialNumber", serialNumber);
        qryParam.put("woState", OrderTrackOperType.WO_ORDER_STATE_20);
        String tacheCode = "";
        String intfCode = MapUtils.getString(intfParamMap, "intfCode");
        String azFlag = MapUtils.getString(intfParamMap, "AZ");
        switch (intfCode) {
            case IntfEnumUtil.OSS_YZWONLINEREPLY:  //3.6 终端盒上线结果反馈接口
                //TODO：这个地方环节code有问题后面修改
                if("A".equals(azFlag)){
                    tacheCode = FlowTacheUtil.YZW_A_MCPE_INSTALL;
                }else if("Z".equals(azFlag)){
                    tacheCode = FlowTacheUtil.YZW_Z_MCPE_INSTALL;
                }/*else {
                    tacheCode = FlowTacheUtil.YZW_L_MCPE_INSTALL;
                    tacheCode = FlowTacheUtil.YZW_C_MCPE_INSTALL;
                }*/
                break;
            case IntfEnumUtil.OSS_YZWOFFLINEREPLY:  //3.7 终端盒下线结果反馈接口
                tacheCode = FlowTacheUtil.YZW_SINGLE_MCPE_OFFLINE;
                break;
            case IntfEnumUtil.OSS_YZWCONFIGREPLY:  //3.8 业务配置下发结果通知接口
                //MCPE业务配置环节
                /*tacheCode = FlowTacheUtil.YZW_L_MCPE_CONFIG;
                tacheCode = FlowTacheUtil.YZW_C_MANUAL_CONFIG_COOPERATE;*/
                break;
            case IntfEnumUtil.OSS_YZWWHOLETESTREPLY:  //3.9 全程业务测试反馈接口
                /*tacheCode = FlowTacheUtil.YZW_L_WHOLE_TEST;
                tacheCode = FlowTacheUtil.YZW_C_WHOLE_TEST;*/
                break;
            case IntfEnumUtil.OSS_IPRANROUTEREPLY: //3.10 IPRAN业务路由配置下发结果反馈接口
                /*
                 * 上联设备业务配置
                 * 目前的思路是有az标识就是az端上联设备业务配置环节；
                 *            没有az标识去查询
                 * @author guanzhao
                 * @date 2020/11/24
                 *
                 */
                // TODO：上联设备业务配置环节时，这个地方az的区分后面还得优化，现在还没思路
                if("A".equals(azFlag)){
                    tacheCode = FlowTacheUtil.YZW_UPEQUIP_BUSICONFIG;
                }else if("Z".equals(azFlag)){
                    tacheCode = FlowTacheUtil.YZW_UPEQUIP_BUSICONFIG;
                }else {
                    tacheCode = FlowTacheUtil.YZW_UPLINK_CONFIG;
                }
                break;
            case IntfEnumUtil.OSS_IPRANROUTEREPLY_0: //3.11 IPRAN端口限速配置下发结果反馈接口
               /* tacheCode = FlowTacheUtil.YZW_UPEQUIP_BUSICONFIG; //上联设备业务配置
                tacheCode = FlowTacheUtil.YZW_C_UPLINK_CONFIG;
                tacheCode = FlowTacheUtil.YZW_UPLINK_CONFIG;
                tacheCode = FlowTacheUtil.YZW_C_UPLINK_CONFIG_COOPERATE;
                tacheCode = FlowTacheUtil.YZW_C_MANUAL_CONFIG_IPRAN;*/
                break;
            case IntfEnumUtil.OSS_IPRANINACTIVEREPLY:  //3.13 IPRan端口数据删除结果反馈接口
                /*tacheCode = FlowTacheUtil.YZW_SINGLE_MCPE_CONFIG;
                tacheCode = FlowTacheUtil.YZW_UPLINK_DATA_DETELE;*/
                break;
            default:
                break;
        }
        if (StringUtils.isEmpty(tacheCode)) {
            //因为接口是一样，环节不一样，所以有的地方不能指定环节去查询，所以只能根据工单状态去查
            qryParam.put("tacheCode", tacheCode);
        }
        Map<String, Object> woOrderDataMap = woOrderDealDao.qryWoOrderDataByThis(qryParam);
        if (MapUtils.isNotEmpty(woOrderDataMap)) {
            String tacheId = MapUtils.getString(woOrderDataMap, "TACHE_ID");
            String woId = MapUtils.getString(woOrderDataMap, "WO_ID");
            String orderId = MapUtils.getString(woOrderDataMap, "ORDER_ID");
            TacheDto tacheDto = commonDealDao.qryTacheDto(Integer.parseInt(tacheId));
            CloudFeedbackIntfWoOrder cloudTacheInftWoOrder = new CloudFeedbackIntfWoOrder(tacheDto, intfParamMap);
            String beanName = cloudTacheInftWoOrder.getBeanNameByTacheCode();
            CloudFlowFeedbackIntf cloudFlowFeedbackIntf = SpringContextHolderUtil.getBean(beanName);
            intfParamMap.put("orderId", orderId);
            intfParamMap.put("woId", woId);
            resMap = cloudFlowFeedbackIntf.feedbackDoSomething(intfParamMap);
        }else {
            resMap.put("success", false);
            resMap.put("message", "没有找到对应等待环节，请核实业务号码以及接口是否正确!");
        }
        return resMap;
    }
}
