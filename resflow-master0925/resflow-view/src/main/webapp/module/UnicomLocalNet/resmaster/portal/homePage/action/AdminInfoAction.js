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
        deleteAdminInfo:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.order.service.AdminInfoServiceInf","deleteAdminInfo"
                ,params,success);
        },
        updateAdminInfo:function(jsonObject,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.order.service.AdminInfoServiceInf","updateAdminInfo"
                ,jsonObject,success);
        },
        queryAdminInfo:function(params,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.order.service.AdminInfoServiceInf","queryAdminInfo"
            ,params
            ,success);
        },
        /* 查询管理员信息*/
        queryAdmin:function(staffId ,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.until.service.UntilServiceIntf","queryAdminInfo"
                ,staffId
                ,success);
        },
        queryDisassemble:function(params,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.order.service.AdminInfoServiceInf","queryDisassemble"
                ,params,success);
        },
        deleteDisassemble:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.order.service.AdminInfoServiceInf","deleteDisassemble"
                ,params,success);
        },
        updateDisassemble:function(jsonObject,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.order.service.AdminInfoServiceInf","updateDisassemble"
                ,jsonObject,success);
        },
        addDisassemble:function(jsonObject,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.order.service.AdminInfoServiceInf","addDisassemble"
                ,jsonObject,success);
        },
    }
});
