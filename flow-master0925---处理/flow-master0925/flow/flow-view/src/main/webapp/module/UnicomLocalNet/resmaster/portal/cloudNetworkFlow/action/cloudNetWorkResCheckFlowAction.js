define(function () {
    return {

        /**
         * 查询登录人信息
         * @returns {*}
         */
        queryStaffInfo:function(){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.until.service.UntilServiceIntf","queryStaffInfo" );
        },
        /**
         * 查询云组网核查调度环节的电路信息
         */
        qrycircuitInfo:function(params,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.cloudNetworkFlow.CloudNetWorkResCheckServiceIntf", "qrycircuitInfo",
            params,success);
        },
        /**
         * 查询云组网专业区域信息配置
         * @param param
         * @param success
         * @returns {*}
         */
        querySpecialAreaInfo:function(param,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.cloudNetworkFlow.CloudNetWorkResCheckServiceIntf", "querySpecialAreaInfo",
                param,success);
        },
        /**
         * 云组网核查单保存专业区域信息
         * @param param
         * @param success
         * @returns {*}
         */
        saveSpecialArea:function(param,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.cloudNetworkFlow.CloudNetWorkResCheckServiceIntf", "saveSpecialArea",
                param,success);
        },
        /**
         * 工单提交
         * @param param
         * @param success
         * @returns {*}
         */
        submitWoOrder:function(param, success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.cloudNetworkFlow.WoOrderDealServiceIntf", "submitWoOrderCloud",
                param,success);
        },
        /**
         * 核查信息保存
         * @param param
         * @param success
         * @returns {*}
         */
        saveCheckInfo:function(param, success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.cloudNetworkFlow.CloudNetWorkResCheckServiceIntf", "saveCheckInfo",
                param,success);
        },
        /**
         * 查询电路的核查信息
         * @param param
         * @param success
         * @returns {*}
         */
        queryCheckInfo:function(param, success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.cloudNetworkFlow.CloudNetWorkResCheckServiceIntf", "queryCheckInfo",
                param,success);
        }
    }
});
