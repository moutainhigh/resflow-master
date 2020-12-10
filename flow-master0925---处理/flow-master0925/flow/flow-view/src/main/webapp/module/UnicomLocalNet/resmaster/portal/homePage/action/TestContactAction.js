define(function(){
    return {
        /**
         * 登陆账号
         * @returns {*}
         */
        queryStaffInfo:function(){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.until.service.UntilServiceIntf","queryStaffInfo" );
        },

        queryOrderInfo:function(jsonObject,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.order.service.OrderQueryServiceIntf","queryWo"
                ,jsonObject,success);
        },
        /**
         * 各地测试联系人员名单
         * @param jsonObject
         * @param success
         * @returns {*}
         */
        queryTestContact:function(jsonObject,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.TestContactQueryIntf","queryTestContact"
                ,jsonObject,success);
        },


        qryInterfaceUrl:function (params,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.dict.service.UnicomSysDictServiceIntf","qryInterfaceUrl",
                params,
                success);
        }
    }
});
