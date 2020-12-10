define(function () {
    return {
        /**
         * 工单提交
         * @param params
         * @param success
         * @returns {*}
         */
        submitWoOrder: function (params, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.cloudNetworkFlow.controller.WoOrderDealController", "submitWoOrder"
                , params
                , success);
        },

        /**
         * 查询分公司区域id--云组网业务产品，电路调度页面
         * @param params
         * @param success
         * @returns {*}
         */
        qryCompanyAreaId: function (params, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.cloudNetworkFlow.controller.WoOrderDealController", "qryCompanyAreaId"
                , params
                , success);
        },
        /**
         * 查询登录人信息
         * @returns {*}
         */
        queryStaffInfo:function(){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.until.service.UntilServiceIntf","queryStaffInfo" );
        },
        /**
         * 查询mcpe安装测试退单的专业和区域
         * @param params
         * @param success
         * @returns {*}
         */
        qryMcpeInstallBackOrderData: function (params, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.cloudNetworkFlow.controller.WoOrderDealController", "qryMcpeInstallBackOrderData"
                , params
                , success);
        },
    }
});
