define(function(){
    return {
        submitOrder:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","submitOrder"
                ,params
                ,success);
        },


    }
});
