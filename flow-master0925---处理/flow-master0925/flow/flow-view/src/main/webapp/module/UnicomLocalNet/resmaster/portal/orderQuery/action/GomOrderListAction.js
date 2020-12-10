define(function () {
    return {

        /**
         * 登陆账号
         * @returns {*}
         */
        queryStaffInfo: function () {
            return ngc.callServerFunction("com.zres.project.localnet.portal.until.service.UntilServiceIntf", "queryStaffInfo");
        },
        /**
         * 导出
         * @param _url
         * @param params
         */
        exportGomOrderListData: function (_url, params) {
            ngc.downLoad(_url, params);
        },
        /**
         *
         * @param params
         * @param success
         * @returns {*}
         */

        queryOrderList: function (params, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.order.service.OrderQueryListServiceIntf", "queryOrderList",
                params, success);
        },
        /**
         * 下拉框
         * @param jsonObject
         * @param success
         * @returns {*}
         */
        queryProdTypeData: function (jsonObject, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.dict.service.UnicomSysDictServiceIntf", "querySysDict",
                jsonObject,
                success);
        },
        /*
               *传入订单号数组，根据流程实例查询拆机单
               */
        queryIsTear: function (params) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.CheckOrderInfoIntf", "queryIsTear", params);
        },
        /*
         *传入订单号数组，根据流程实例查询在途单
         */
        queryisOnWay: function (params) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.CheckOrderInfoIntf", "queryisOnWay", params);
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
    }
});