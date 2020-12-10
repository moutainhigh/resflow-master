define(function () {
    return{
        qryOrdChgLogByCstOrdId:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.orderAbnormal.service.OrderAbnormalServiceIntf","qryOrdChgLogByCstOrdId"
                ,params
                ,success);
        },
        exceptionFlowSure:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.orderAbnormal.service.OrderAbnormalServiceIntf","appendConfirm"
                ,params
                ,success);
        },
        qrySrvOrdIds:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.orderAbnormal.service.OrderAbnormalServiceIntf","qrySrvOrdIds"
                ,params
                ,success);
        },
        compWo:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.orderAbnormal.service.OrderAbnormalServiceIntf","compWo"
                ,params
                ,success);
        },
        appendReject:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.orderAbnormal.service.OrderAbnormalServiceIntf","appendReject"
                ,params
                ,success);
        },
        getResUrlParam:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.orderAbnormal.service.OrderAbnormalServiceIntf","getResUrlParam"
                ,params
                ,success);
        },
        getPrivVersionState:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.orderAbnormal.service.OrderAbnormalServiceIntf","getPrivVersionState"
                ,params
                ,success);
        },
        testExceptionFlowChange:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.webservice.flow.ExceptionFlowIntf","testExceptionFlowChange"
                ,params
                ,success);
        }
    }
})