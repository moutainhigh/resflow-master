define(function(){
    return {
        /**
         * 登陆账号
         * @returns {*}
         */
        queryStaffInfo:function(){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.until.service.UntilServiceIntf","queryStaffInfo" );
        },
        qryMsmSwitchByArea:function (params,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryMsmSwitchByArea",
                params,
                success);
        },
        addAdminInfo:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.order.service.AdminInfoServiceInf","addAdminInfo"
                ,params,success);
        },

        addDisassemble:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.order.service.AdminInfoServiceInf","addDisassemble"
                ,params,success);
        },
        queryDisassemble:function(params,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.order.service.AdminInfoServiceInf","queryDisassemble"
                ,params,success);
        },
    }
});
