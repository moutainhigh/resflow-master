define(function(){
    return {
        qrySecondDataMake:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.SecDealLocalServiceInf","qrySecondDataMake"
                ,params
                ,success);
        },
        qrySecondResMake:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.SecDealLocalServiceInf","qrySecondResMake"
                ,params
                ,success);
        },
        qrySecToLocalData:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.SecDealLocalServiceInf","qrySecToLocalData"
                ,params
                ,success);
        },
        qryDispatchData:function(orderId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.SecDealLocalServiceInf","qryDispatchData"
                ,orderId
                ,success);
        },
        qrySecondSourceDispatch:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.DataMakeTacheOperServiceInf","qryIfHasSourceDispatch"
                ,params
                ,success);
        },
    }
});
