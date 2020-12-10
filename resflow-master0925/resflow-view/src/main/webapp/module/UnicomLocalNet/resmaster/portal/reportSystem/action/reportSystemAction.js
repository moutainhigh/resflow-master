define(function () {
    return {

        /**
         * 调单统计报表查询
         * @param map 开始及结束时间
         * @param success 成功方法回调
         * @author PangHao
         * @date 2019/5/10 : 18:56
         */
        dispatchOrderStatistics: function (map, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.report.service.ReportIntf", "dispatchOrderStatistics",
                map, success);
        },

        /**
         * 导出excel文件
         * @param map
         * @param success
         */
        export: function (_url, params) {
            ngc.downLoad(_url, params);
        },

        /**
         * 通过字典类型和名称得到编码
         * @param map
         * @param success
         */
        getDicCodeByContent:function (map, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.report.service.ReportIntf", "getDicCodeByContent",
                map, success);
        },

        /**
         * 查询调单列表
         * @param map
         * @param success
         */
        queryDisOrderList:function (map, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.report.service.ReportIntf", "queryDisOrderList",
                map, success);
        },

        /**
         * 开通及时率统计报表
         * @param map
         * @param success
         */
        openTimeRateStatistics:function (map, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.report.service.ReportIntf", "openTimeRateStatistics",
                map, success);
        },
        /**
         * 查询枚举字典
         * @param map
         * @param success
         */
        queryEnum:function (map, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.report.service.ReportIntf", "queryEnum",
                map, success);
        },
        /**
         * 业务网络核查统计列表
         * @param map
         * @param success
         */
        businessNetworkVerification:function (map, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.report.service.ReportIntf", "businessNetworkVerification",
                map, success);
        },

        /**
         * 报竣未起租统计
         * @param map
         * @param success
         */
        queryCompletionNotRented:function (map, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.report.service.ReportIntf", "queryCompletionNotRented",
                map, success);
        },
	 /**
         * 电路汇总查询
         *
         * @param map 查询条件及分页信息
         * @return 分页数据
         * @author PangHao
         * @date 2019/6/26  20:02
         **/
        queryCircuitSummaryList:function (map, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.report.service.ReportIntf", "queryCircuitSummaryList",
                map, success);
        },
        /**
         * 报表统计电路信息明细
         * @param param
         * @param success
         * @returns {*}
         */
        queryCircuitInfoForBusiReport:function(param, success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.report.service.ReportIntf", "queryCircuitInfoForBusiReport",
                param, success);
        }


    }

})