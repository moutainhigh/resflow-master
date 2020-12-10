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
        queryCircuitInfoDraftGrid:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryCircuitInfoDraftGrid"
                ,params
                ,success);
        },
        getTacheButton:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","getTacheButton"
                ,params
                ,success);
        },
        getFreeWoOrder:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","getFreeWoOrder"
                ,params
                ,success);
        },
        queryAttachInfo:function(srvOrdId,orderId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryAttachInfo"
                ,srvOrdId
                ,orderId
                ,success);
        },
        queryDispatchAttachInfo:function(dispatchOrderId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryDispatchAttachInfo"
                ,dispatchOrderId
                ,success);
        },
        queryCheckOrderStatBySrvOrdId:function(srvOrdId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryCheckOrderStatBySrvOrdId"
                ,srvOrdId
                ,success);
        },
        qryDispatchAttachForDraftSchedule:function(dispatchOrderId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","qryDispatchAttachForDraftSchedule"
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
        queryTaskInfo:function(orderId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryTaskInfo"
                ,orderId
                ,success);
        },
        querySecToLocalTaskInfo:function(orderId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","querySecToLocalTaskInfo"
                ,orderId
                ,success);
        },
        querySubTaskInfo:function(orderId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","querySubTaskInfo"
                ,orderId
                ,success);
        },
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
        queryDispatchOrderInfo:function(param,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryDispatchOrderInfo"
                ,param
                ,success);
        },
        //通过专业code查询名称
        querySpecNetMagPro: function (param, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","querySpecNetMagPro", param, success);
        },
        queryDispatchOrderInfoById:function(param,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryDispatchOrderInfoById"
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
        queryFeedbackInfo:function(srvOrdId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryFeedbackInfo"
                ,srvOrdId
                ,success);
        },
        queryLocalFeedbackInfo:function(srvOrdId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryLocalFeedbackInfo"
                ,srvOrdId
                ,success);
        },
        updateCC:function(param,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.localStandbyInfo.service.OrderStandbyServiceIntf","updateCC"
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
        qryTacheByOrderIds:function (params,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","qryTacheByOrderIds",
                params,
                success);
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
        /**
         * 查询申请单
         * @param params
         * @param success
         * @returns {*}
         */
        queryApplyAttachInfo : function(param,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryApplyAttachInfo"
                ,param
                ,success);
        },
        /**
         * 过户信息
         * @param param
         * @param success
         * @returns {*}
         */
        queryRenameLogByCstOrdId : function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryRenameLogByCstOrdId"
                ,params
                ,success);
        },
        /**
         * 省内dia 自动开通进度查询
         * @param params
         * @param success
         * @returns {*}
         */
        progressQueryOrder : function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.webservice.construct.ProgressQueryOrderIntf","progressQueryOrder"
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
        queryCheckInfoBack:function(params, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","queryCheckFeedBackInfoByWoId"
                , params
                ,success);
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
        getAttachInfo:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","getAttachInfo"
                ,params,success);
        },
        feedBackToOneDry:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","feedBackToOneDry"
                ,params,success);
        },
        querySrvOrdStatBySrvOrdId:function(srvOrdId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","querySrvOrdStatBySrvOrdId"
                ,srvOrdId
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
         * 开通单下发工建系统
         * add by wang.gang2
         * @param param
         * @param success
         * @returns {*}
         */
        orderSendConstruct:function(param, success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.webservice.construct.OssToConstructOrderSendIntf","orderSend"
                ,param, success);
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
        }

    }
});
