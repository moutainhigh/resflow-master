define(function () {
    return {
        /**
         * 登陆账号
         * @returns {*}
         */
        queryStaffInfo: function (param, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.subIndexPage.service.SubIndexPageServiceIntf", "queryStaffInfo", param, success);
        },
        /**
         * 查询月工单绩效量
         * @returns {*}
         */
        queryMonthWorkChart: function (param, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.subIndexPage.service.SubIndexPageServiceIntf", "queryMonthWorkChart", param, success);
        },
        /**
         * 查询月工单时间状态分布
         * @returns {*}
         */
        queryWorkOrderDistributeChart: function (param, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.subIndexPage.service.SubIndexPageServiceIntf", "queryWorkOrderDistributeChart", param, success);
        },
    }
})