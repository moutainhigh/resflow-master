define(function(){
    return {
        submitOrder:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","submitOrder"
                ,params
                ,success);
        },
        sendWoOrder:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","sendWoOrder"
                ,params
                ,success);
        },

        qryProvinceName:function(params,success){
        return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryProvinceName"
            ,params
            ,success);
        },
        qryProvinceValue:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryProvinceValue"
                ,params
                ,success);
        },
        getFreeWoOrder:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","getFreeWoOrder"
                ,params
                ,success);
        },
        transferWoOrder:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","transferWoOrder"
                ,params
                ,success);
        },
        goBackOrder:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","goBackOrder"
                ,params
                ,success);
        },
        disableOrder:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","disableOrder"
                ,params
                ,success);
        },
        qryDepart:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryDepart"
                ,params,success);
        },
        qryDepartParent:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryDepartParent"
                ,params,success);
        },
        qrySearchOrgPerDepSingleDown:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qrySearchOrgPerDepSingleDown"
                ,params,success);
        },
        qrySearchOrgPerDepPullDown:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qrySearchOrgPerDepPullDown"
                ,params,success);
        },
        qrySearchOrgPerDepPullDownSub:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qrySearchOrgPerDepPullDownSub"
                ,params,success);
        },
        qrySearchOrgPerDepart:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qrySearchOrgPerDepart"
                ,params,success);
        },
        qrySearchContactsSel:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qrySearchContactsSel"
                ,params,success);
        },
        qrySearchContacts:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qrySearchContacts"
                ,params,success);
        },
        addSearchContacts:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","addSearchContacts"
                ,params,success);
        },
        deleteSearchContacts:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","deleteSearchContacts"
                ,params,success);
        },
        qryChildNodeData:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryChildNodeData"
                ,params,success);
        },
        getStaffInfoDeptListUnit:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","getStaffInfoDeptListUnit"
                ,params,success);
        },
        qryChildNodeDataT:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryChildNodeDataT"
                ,params,success);
        },
        getCurrentUserInfo:function(success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","getCurrentUserInfo"
                ,success);
        },
        getsequenceNumForSec:function(param,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","getsequenceNumForSec"
                ,param
                ,success);
        },
        rollBackWoOrder:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","rollBackWoOrder"
                ,params
                ,success);
        },
        querySpecialtyConfig:function(areaId,serviceId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","querySpecialtyConfig"
                ,areaId,
                serviceId,
                success);
        },
        queryDispatchInfoBySrvOrdId:function(srvOrdId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","queryDispatchInfoBySrvOrdId"
                ,srvOrdId,success);
        },
        /**
         * 查询业务订单--电路信息  核查流程时（特殊：核查调度不选专业）
         */
        qrySrvOrdListCheck:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.localStandbyInfo.service.OrderStandbyServiceIntf","qrySrvOrdListCheck",
                params,success);
        },
        /**
         * 查询业务订单--电路信息  主流程
         */
        qrySrvOrdList:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.localStandbyInfo.service.OrderStandbyServiceIntf","qrySrvOrdList",
                params,success);
        },
        /**
         * 查询业务订单--电路信息  子流程
         */
        qrySrvOrdChildList:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.localStandbyInfo.service.OrderStandbyServiceIntf","qrySrvOrdChildList",
                params,success);
        },
        /**
         * 查询资源配置页面报文
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        resConfig :function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","resConfig"
                ,params
                ,success);
        },
        qryCircuitAreaInfo:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryCircuitAreaInfo"
                ,params
                ,success);
        },
        /**
         * 保存电路信息对应的专业配置
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        saveSpecialtyConfigInfo:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","saveSpecialtyConfigInfo"
                ,params
                ,success);
        },
        saveResConstructConfigInfo:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","saveResConstructConfigInfo"
                ,params
                ,success);
        },
        /**
         * 查询电路对应的专业配置信息进行回显
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        queryPropertyConfig:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","queryPropertyConfig"
                ,params
                ,success);
        },

        /**
         * 查询岗位
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        qryJob:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryJob"
                ,params,success);
        },
        /**
         * 查询之前入库的岗位数据
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        /*qryDispObj:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryDispObj"
                ,params,success);
        },*/
        /**
         * 返回已保存的岗位数据
         * @param params
         * @param success
         * @returns {*}
         */
        qryDispObjByOrderId:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryDispObjByOrderId"
                ,params,success);
        },
        /**
         * 查询信息用来刷新调单标题和内容
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        getDispatchInfo:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","getDispatchInfo"
                ,params,success);
        },
        /**
         * 查询核查反馈信息
         * @param paramssaveCheckInfo
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        queryCheckInfo:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","queryCheckInfo"
                ,params,success);
        },
        /**
         * 查询跨域流程发往单位--用于退单省份查询
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        getProvinceName:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","getProvinceName"
                ,params,success);
        },
        /**
         * 查询核查反馈信息
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        saveCheckInfo:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","saveCheckInfo"
                ,params,success);
        },
        /**
         * 查询子流程
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        qrychildFlowInfo:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","getListChildFlow"
                ,params,success);
        },

        /**
         * 子流程环节，查询父流程的psid
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        getMainFlowPsId:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","getMainFlowPsId"
                ,params,success);
        },

        /**
         * 查询附件
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        getAnnex:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.annex.GetAnnexInfoIntf","getAnnexInfo"
                ,params,success);
        },

        //查询专业，网管
        querySpecNetMag: function (param, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","querySpecNetMag", param, success);
        },
        //调单保存
        saveDispatchOrder:function (param, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","saveDispatchOrder", param, success);
        },
        //调单信息
        queryDispatchOrder:function (param, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","queryDispatchOrder", param, success);
        },
        //派单信息
        queryDispatchDept:function (param, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","queryDispatchDept", param, success);
        },
        //是否一干
        queryOneDry:function (param, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","queryOneDry", param, success);
        },
        //签发人
        queryIssuer:function (param, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","queryIssuer", param, success);
        },
        qryChildOrder:function (param, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryChildOrder", param, success);
        },
        getDispatchNumber:function (param, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","getDispatchNumber", param, success);
        },
        getDispatchTitle:function (param, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","getDispatchTitle", param, success);
        },
        querySecondDisNoByCstOrdId:function(param, success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","querySecondDisNoByCstOrdId", param, success);
        },
        linkDisOrdNoToCstOrd:function(param, success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","linkDisOrdNoToCstOrd", param, success);
        },
        /**
         * 查询是否有追单
         * @param orderId
         * @param cstOrdId
         * @returns {*}
         */
        queryIfTrack:function(orderId, cstOrdId){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryIfTrack"
                ,orderId
                ,cstOrdId);
        },
        /**
         * 起草调单--查询主调区域是否可以修改
         * @param param
         * @param success
         * @returns {*}
         */
        ifModifyMainArea:function(param, success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","ifModifyMainArea", param, success);
        },

        batchSaveDispatchInfo : function(param, success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","batchSaveDispatchInfo", param, success);
        },
        /**
         * 查询专业核查的工单
         * @param params
         * @param success
         * @returns {*}
         */
        qryAllSpecialCheckWoOrder:function (orderId,success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.CheckOrderServiceIntf","qryAllSpecialCheckWoOrder"
                ,orderId,success);
        },
        /**
         * 登陆账号
         * @returns {*}
         */
        queryStaffInfo:function(){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.until.service.UntilServiceIntf","queryStaffInfo" );
        },
        /*
         *延期单批量审核
         */
        feedBackToOneDryBatch:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","feedBackToOneDryBatch"
                ,params,success);
        },

        /**
         * 查询业务订单归属哪个系统
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        qrySrvOrderBelongSys:function(params){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qrySrvOrderBelongSys",params);
        },
        /**
         *查询电路属性某属性来做判断
         * @param param
         * @param success
         * @returns {*}
         */
        queryAttrInfos:function(param, success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","queryAttrInfos"
                ,param,success);
        }
    }
});
