define(function(){
    return {
        /*queryCircuitInfo:function(data,serviceId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryCircuitInfo",data
                ,success);
        },*/
        exportOrderInfo: function (_url,params) {
            ngc.downLoad(_url, params);
        },
        queryConsumerInfo:function(cstOrdId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryConsumerInfo"
                ,cstOrdId
                ,success);
        },
        queryConsumerInfoByCustId:function(cstOrdId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryConsumerInfoByCustId"
                ,cstOrdId
                ,success);
        },
        queryCheckOrderStatBySrvOrdId:function(srvOrdId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryCheckOrderStatBySrvOrdId"
                ,srvOrdId
                ,success);
        },
        queryOrderDeatilsInfo:function(cstOrdId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryOrderDeatilsInfo"
                ,cstOrdId
                ,success);
        },
        queryChangeOrderInfo:function(object,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.orderAbnormal.service.OrderAbnormalServiceIntf","queryChangeOrderInfo"
                ,object
                ,success);
        },
        queryCircuitInfo:function(srv_ord_id,serviceId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryCircuitInfo"
                ,srv_ord_id
                ,serviceId
                ,success);
        },
        queryCircuitInfoGrid:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryCircuitInfoGrid"
                ,params
                ,success);
        },
        qryExceptionNoticeList: function (params, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf", "qryExceptionNoticeList"
                , params
                , success);
        },
        getTacheButton:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","getTacheButton"
                ,params
                ,success);
        },
        getCloudTacheButton:function(params, success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.cloudNetworkFlow.controller.WoOrderDealController","getCloudTacheButton"
                ,params
                ,success);
        },
        getFreeWoOrder:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","getFreeWoOrder"
                ,params
                ,success);
        },
        affirmException: function (params, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf", "affirmException"
                , params
                , success);
        },
        queryAttachInfo:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryAttachInfo"
                ,params
                ,success);
        },
        queryDispatchAttachInfo:function(dispatchOrderId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryDispatchAttachInfo"
                ,dispatchOrderId
                ,success);
        },
        queryIdeaInfo:function(orderId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryIdeaInfo"
                ,orderId
                ,success);
        },
        queryIdeaInfoBySrvOrdId:function(srvordId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryIdeaInfoBySrvOrdId"
                ,srvordId
                ,success);
        },
        queryLogInfo:function(orderId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryLogInfo"
                ,orderId
                ,success);
        },
        /**
         * 查询二干任务列表
         * @param orderId
         * @param success
         * @returns {*}
         */
        querySecTaskInfo:function(orderId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","querySecTaskInfo"
                ,orderId
                ,success);
        },
        /**
         * 查询本地任务列表
         * @param orderId
         * @param success
         * @returns {*}
         */
        queryLocalTaskInfo:function(orderId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryLocalTaskInfo"
                ,orderId
                ,success);
        },
        /**
         * 查询任务列表
         * @param orderId
         * @param success
         * @returns {*}
         */
        queryTaskInfo:function(orderId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryTaskInfo"
                ,orderId
                ,success);
        },
        /**
         * 查询判断单子是否由二干下发
         * @param orderId
         * @returns {*}
         */
        ifFromSecond:function(orderId){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","ifFromSecond"
                ,orderId);
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
        /*querySecToLocalTaskInfo:function(orderId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","querySecToLocalTaskInfo"
                ,orderId
                ,success);
        },
        querySubTaskInfo:function(orderId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","querySubTaskInfo"
                ,orderId
                ,success);
        },*/
        queryRelevanceOrderInfo:function(orderId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryRelevanceOrderInfo"
                ,orderId
                ,success);
        },
        queryWarningInfo:function(orderId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryWarningInfo"
                ,orderId
                ,success);
        },
        downFile:function(_url,param){
            ngc.downLoad(_url, param);
        },
        resConfig :function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","resConfig"
                ,params
                ,success);
        },
        //通过专业code查询名称
        querySpecNetMagPro: function (param, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","querySpecNetMagPro", param, success);
        },
        queryDispatchOrderInfo:function(param,success){

            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryDispatchOrderInfo"
                ,param
                ,success);
        },
        queryDispatchOrderInfoByCstOrdId:function(param,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryDispatchOrderInfoByCstOrdId"
                ,param
                ,success);
        },
        queryDispatchOrderInfoByDispatchId:function(param,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryDispatchOrderInfoByDispatchId"
                ,param
                ,success);
        },
        queryResourceOrderInfoW:function(srvOrdId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryResourceOrderInfoW"
                ,srvOrdId
                ,success);
        },
        queryResourceOrderInfo:function(srvOrdId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryResourceOrderInfo"
                ,srvOrdId
                ,success);
        },
        queryResourceOrderInfoY:function(srvOrdId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryResourceOrderInfoY"
                ,srvOrdId
                ,success);
        },
        queryAddProductInfoBySrvOrdId : function(param,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryAddProductInfoBySrvOrdId"
                ,param
                ,success);
        },
        queryApplyAttachInfo : function(param,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryApplyAttachInfo"
                ,param
                ,success);
        },
        queryTaskInfoByTacheCode : function(param,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryTaskInfoByTacheCode"
                ,param
                ,success);
        },
        qryMsmSwitchByArea:function (params,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryMsmSwitchByArea",
                params,
                success);
        },
        qryInterfaceUrl:function (params,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.dict.service.UnicomSysDictServiceIntf","qryInterfaceUrl",
                params,
                success);
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
         * 查询对端联系人
         * @returns {*}
         */
        queryOrderOtherSystemInfo:function (params,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.webservice.oneDry.FeedbackInterface","completeInformationQueryReport",
                params,
                success);
        },
        updateCC:function(param,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.localStandbyInfo.service.OrderStandbyServiceIntf","updateCC"
                ,param
                ,success);
        },
	 /**
         * 文件下载
         * */
        downLoadOneDryFile: function (_url, params) {
         ngc.downLoad(_url, params);
        },
        /**
         * 登陆账号
         * @returns {*}
         */
        queryStaffInfo:function(){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.until.service.UntilServiceIntf","queryStaffInfo" );
        },
        queryIfSupplementOrder:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","queryIfSupplementOrder"
                ,params
                ,success);
        },
        /**
         * 完工汇总环节提交前判断工建是否已完成建设
         * @param orderId
         * @param cstOrdId
         * @returns {*}
         */
        summaryBeforeCommit:function(params, success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","summaryBeforeCommit"
                ,params
                ,success);
        },
        showEnginInfo:function(params){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","showEnginInfo"
                ,params);
        },
	    /**
         * 过户信息
         * @param param
         * @param success
         * @returns {*}
         */
        queryRenameLogByCstOrdId : function(params,success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf", "queryRenameLogByCstOrdId"
                , params
                , success);
        },

        progressQueryOrder : function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.webservice.construct.ProgressQueryOrderIntf","progressQueryOrder"
                ,params
                ,success);
        },
        /**
         * 省内dia 自动开通进度查询
         * @param params
         * @param success
         * @returns {*}
         */
        provinceResOrder : function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.webservice.provinceRes.ProvinceResOrderService","provinceResOrderService"
                ,params
                ,success);
        },
        queryStaffInfo:function(){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.until.service.UntilServiceIntf","queryStaffInfo" );
        },
        queryRouteInfoUrl : function (param, success) {
            return  ngc.callServerFunction("com.zres.project.localnet.portal.until.service.UntilServiceIntf","queryRouteInfoUrl"
                ,param
                ,success);
        },

        submitOrderToProvinceAuto : function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.webservice.provinceRes.ProvinceResOrderService","submitOrderToProvinceAuto"
                ,params
                ,success);
        },
        queryFeedbackInfo:function(srvOrdId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryFeedbackInfo"
                ,srvOrdId
                ,success);
        },
        queryCheckInfoBack:function(params, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","queryCheckFeedBackInfoByWoId"
                , params
                ,success);
        },
        /**
         * add by wang.gang2
         * 查询对接工建得省份 跟配置得核查下发工建得按钮配置区分开
         * @param param
         * @param success
         * @returns {*}
         */
        queryConstructConf:function(param, success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","queryConstructConf"
                ,param,success);
        },

        /**
         * 同步获取附件信息
         * @param s
         * @param param2
         * @returns {*}
         */
        getFileBySync:function(params, success) {
            return ngc.callDynDataServerFunction("com.zres.project.localnet.portal.annex.GetAnnexInfoIntf","getAnnexInfo"
                ,params,success);
        },

        querySrvOrdStatBySrvOrdId:function(srvOrdId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","querySrvOrdStatBySrvOrdId"
                ,srvOrdId
                ,success);
        },

        /**
         * 获取激活结果信息
         * @param s
         * @param params
         * @returns {*}
         */
        queryActivateResult:function(params, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","getActivateInfo"
                ,params,success);
        },

        /**
         * 调用省份IP智能网管系统重新激活工单
         * @param s
         * @param params
         * @returns {*}
         */
        resActivation:function(params, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","sendOrder"
                ,params,success);

        },


        /**
         * 直接反馈核查信息
         * @param params
         * @param success
         * @returns {*}
         */
        feedBackDirect:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","feedBackDirect"
                ,params
                ,success);
        },
        /**
         * 查询自动化核查信息
         * @param params
         * @param success
         * @returns {*}
         */
        queryAutoCheckInfo:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","queryAutoCheckInfo"
                ,params
                ,success);
        },
        /**
         * 获取设备回收信息
         * @param s
         * @param params
         * @returns {*}
         */
        queryEquipInfo:function(srvOrdId, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.EquipMentRecycleServiceIntf","queryEquipBySrvOrdId"
                ,srvOrdId
                ,success);
        },
        /**
         * 通过order_Id查询父流程psId
         */
        qryParentPsIdBySubOrderId: function (params){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf", "qryParentPsIdBySubOrderId",params);
        },
        queryAttachment: function (CustId, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.QueryAttachmentIntf", "queryAttachment"
                , CustId, success);
        },
        delFileOnFtp: function (fileInfo, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.QueryAttachmentIntf", "delFileOnFtpAndDao"
                , fileInfo, success);
        },
        /**
         * 延期申请保存
         * @param params
         * @param success
         * @returns {*}
         */
        postponementApply :function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","postponementApply"
                ,params
                ,success);
        },
        /**
         * 延期申请提交
         * @param params
         * @param success
         * @returns {*}
         */
        postponementApplyCommit :function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","postponementApplyCommit"
                ,params
                ,success);
        },
        queryApplySaveInfo :function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","queryApplySaveInfo"
                ,params
                ,success);
        },
        /**
         * 查询延期申请信息
         * @param s
         * @param params
         * @returns {*}
         */
        queryPostPonement:function(srvOrdId, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.local.PostponementServiceIntf","queryPostponementInfoBySrvId"
                ,srvOrdId
                ,success);
        },
        /**
         * 查询是否施工
         * @param params
         * @param success
         * @returns {*}
         */
        qryIfConstruct:function(params, success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.cloudNetworkFlow.controller.WoOrderDealController","qryIfConstruct"
                ,params
                ,success);
        },
    }
});
