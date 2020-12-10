define(function () {
    return {

        /**
         * 得到本地网流程环节信息（本地网、中继、核查）
         *
         * @return 环节名称编码列表
         * @author PangHao
         * @date 2019/6/4  15:08
         **/
        getLocalNetworkTache: function (success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.report.service.CqReportIntf", "getLocalNetworkTache", success);
        },

        /**
         * 得到当前登录人省份/地市下属分公司
         *
         * @return 分公司信息列表
         * @author PangHao
         * @date 2019/6/4  15:19
         **/
        getCityInfo: function (success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.report.service.CqReportIntf", "getCityInfo", success);
        },
        /**
         * 得到当前登录人地市下属分公司
         *
         * @return 分公司信息列表
         * @author wang.gang2
         * @date 2019/6/4  15:19
         **/
        getOrgName: function (success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.report.service.CqReportIntf", "getOrgName", success);
        },

        /**
         * 未完成环节工单明细（分页）
         *
         * @param map 查询参数及分页信息
         * @return 分页数据
         * @author PangHao
         * @date 2019/6/4  9:57
         **/
        queryUndoneTacheList: function (map, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.report.service.CqReportIntf", "queryUndoneTacheList",
                map, success);
        },
        /**
         * 未完成环节工单统计
         *
         * @param map 查询参数及分页信息
         * @return 分页数据
         * @author PangHao
         * @date 2019/6/4  9:57
         **/
        queryUndoneTacheStatistics: function (map, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.report.service.CqReportIntf", "queryUndoneTacheStatistics",
                map, success);
        },

        /**
         * 超时未报竣电路明细(分页)
         * @author PangHao
         * @date   2019/6/11  14:21
         * @param  map 分页及查询条件
         * @return 分页数据
         **/
        queryOvertimeUnfinishedList: function (map, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.report.service.CqReportIntf", "queryOvertimeUnfinishedList",
                map, success);
        },

        /**
         * 电路完工及时率统计
         * @author PangHao
         * @date   2019/6/13  11:36
         * @param  map 查询条件
         * @return 表头及表格数据
         **/
        queryFinishTimeRateStatistics: function (map, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.report.service.CqReportIntf", "queryFinishTimeRateStatistics",
                map, success);
        },
        /**
         * 资源分配未入库查询（分页）
         *
         * @param map 分页及查询条件
         * @return 分页数据
         * @author PangHao
         * @date 2019/6/17  17:15
         **/
        queryResAllocateUnStorageList: function (map, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.report.service.CqReportIntf", "queryResAllocateUnStorageList",
                map, success);
        },

        /**
         * 资源分配未入库查询（汇总）
         * @author PangHao
         * @date   2019/6/18  15:56
         * @param map 查询条件
         * @return 表头及汇总数据
         **/
        queryResAllocateUnStorageStatistics: function (map, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.report.service.CqReportIntf", "queryResAllocateUnStorageStatistics",
                map, success);
        },

        /**
         * 得到专业字典
         * @author PangHao
         * @date   2019/6/19  15:42
         * @return 专业字典列表
         **/
        getSpecialtyType: function (success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.report.service.CqReportIntf", "getSpecialtyType", success);
        },
    }

});