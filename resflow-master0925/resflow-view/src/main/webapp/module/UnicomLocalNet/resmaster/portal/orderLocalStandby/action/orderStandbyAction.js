define(function(){
    return {

        /**
         * 查询客户订单
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        qryCstOrdList:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.localStandbyInfo.service.OrderStandbyServiceIntf","qryCstOrdList",
                params,success);
        },
        /**
         * 导出待办
         * @param _url
         * @param params
         */
        exportStandbyOrderData: function (_url,params) {
            ngc.downLoad(_url, params);
        },
        /**
         * 填写阶段性意见(有附件)
         * @param params
         * @param success
         * @returns {*}
         */

        updateCollapsible:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.localStandbyInfo.service.OrderStandbyServiceIntf","updateCollapsible",
                params,success);
        },
        /**
         * 填写阶段性意见(无附件)
         * @param params
         * @param success
         * @returns {*}
         */
        updateCollapsibleSingle:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.localStandbyInfo.service.OrderStandbyServiceIntf","updateCollapsibleSingle",
                params,success);
        },
        /**
         * 查询数据
         * @param params
         * @param success
         * @returns {*}
         */
        queryOrderInfo:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.localStandbyInfo.service.OrderStandbyServiceIntf","queryOrderInfo",
                params,success);
        },
        /**
         * 查询数据(填写阶段性意见)
         * @param params
         * @param success
         * @returns {*}
         */
        querySubOrderInfoColl:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.localStandbyInfo.service.OrderStandbyServiceIntf","querySubOrderInfoColl",
                params,success);
        },
        /**
         * 查询数据客户订单
         * @param params
         * @param success
         * @returns {*}
         */
        qryCstOrdList:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.localStandbyInfo.service.OrderStandbyServiceIntf","qryCstOrdList",
                params,success);
        },
        /**
         * 查询数据客户订单
         * @param params
         * @param success
         * @returns {*}
         */
        qrySrvOrdList:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.localStandbyInfo.service.OrderStandbyServiceIntf","qrySrvOrdList",
                params,success);
        },
        /**
         * 查询数据总数
         * @param params
         * @param success
         * @returns {*}
         */
        queryStandbyOrderCount:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.localStandbyInfo.service.OrderStandbyServiceIntf","queryStandbyOrderCount",
                params,success);
        },
        /**
         * 查询每个tab页数据总数
         * @param params
         * @param success
         * @returns {*}
         */
        queryStandbyOrderEachCount:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.localStandbyInfo.service.OrderStandbyServiceIntf","queryStandbyOrderEachCount",
                params,success);
        },
        /**
         * 下拉框
         * @param jsonObject
         * @param success
         * @returns {*}
         */
        queryProdTypeData:function(jsonObject,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.dict.service.UnicomSysDictServiceIntf","querySysDict",
                jsonObject,
                success);

        },
        /**
         * 登陆账号
         * @returns {*}
         */
        queryStaffInfo:function(){
                return  ngc.callServerFunction("com.zres.project.localnet.portal.until.service.UntilServiceIntf","queryStaffInfo" );
            },
        /**
         * 签收释放
         * @param params
         * @param success
         * @returns {*}
         */
        getFreeWoOrder:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","getFreeWoOrder"
                ,params
                ,success);
        },
        qryInterfaceUrl:function (params,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.dict.service.UnicomSysDictServiceIntf","qryInterfaceUrl",
                params,
                success);
        },
        qryMsmSwitchByArea:function (params,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryMsmSwitchByArea",
                params,
                success);
        },
        queryConfigParamById:function(success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.until.service.UntilServiceIntf","queryConfigParamById" ,success);
        },
        queryNowAffiche: function(count, success){
            ngc.callServerFunction("com.ztesoft.res.frame.affiche.inf.AfficheSerivceIntf","queryNowAffiche"
                ,parseInt(count)
                ,success);
        },
        //下拉数据接口
        queryProdTypeData:function(jsonObject,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.localStandbyInfo.service.OrderStandbyServiceIntf","querySysDict",
                jsonObject,
                success);

        },
        //单据类型下拉
        queryItemType:function (jsonObject,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.dict.service.UnicomSysDictServiceIntf","querySysDict",
                jsonObject,
                success);
        },
        qrySrvOrdIds:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.orderAbnormal.service.OrderAbnormalServiceIntf","qrySrvOrdIds"
                ,params
                ,success);
        },
        /**
         * 查询所有情况数据总数
         * @param params
         * @param success
         * @returns {*}
         */
        queryAllStandbyOrderCount: function (params, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.localStandbyInfo.service.OrderStandbyServiceIntf", "queryAllStandbyOrderCount",
                params, success);
        },

        updateCC:function(param,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.localStandbyInfo.service.OrderStandbyServiceIntf","updateCC"
                ,param
                ,success);
        },

    }
});
