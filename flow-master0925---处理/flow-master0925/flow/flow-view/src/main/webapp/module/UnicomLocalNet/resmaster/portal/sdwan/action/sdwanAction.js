/**
 * Created by Thinkpad on 2020/3/20.
 */

define(function(){
    return {

        queryCircuitInfo: function (param, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.sdwan.service.SdwanDealServiceIntf",
                "queryCircuitInfo",
                param,
                success);
        },

        /**
         * 工单提交
         * @param param
         * @param success
         * @returns {*}
         */
        submitOrder:function(param,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.sdwan.controller.SdwanDealController",
                "submitOrderSdwan",
                param,
                success);
        },

        queryEnum: function (params, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.sdwan.service.SdwanDealServiceIntf", "queryEnum"
                , params, success);
        },

        /**
         * 登陆账号
         * @returns {*}
         */
        queryStaffInfo:function(){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.until.service.UntilServiceIntf","queryStaffInfo" );
        },
        /**
         * 保存终端信息
         * @param param
         * @param success
         * @returns {*}
         */
        saveDeviceInfo:function(param,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.sdwan.controller.SdwanDealController",
                "saveDeviceInfo",
                param,
                success);
        },
        /**
         * 保存wan配置信息
         * @param param
         * @param success
         * @returns {*}
         */
        saveWanInfo:function(param,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.sdwan.controller.SdwanDealController",
                "saveWanInfo",
                param,
                success);
        },
        /**
         * 查询wan配置信息
         * @param param
         * @param success
         * @returns {*}
         */
        queryWanInfo: function (param, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.sdwan.service.SdwanDealServiceIntf",
                "queryWanInfo",
                param,
                success);
        },

    }
});
