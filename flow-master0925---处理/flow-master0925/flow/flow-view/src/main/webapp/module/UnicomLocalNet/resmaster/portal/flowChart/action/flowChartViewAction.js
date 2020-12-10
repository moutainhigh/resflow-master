define(function(){
    return {
        /**
         * 通过环节Id查询环节编码
         */
        getFLowTacheCodeById: function (params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf", "getFLowTacheCodeById",params,success);
        },
        /**
         * 通过业务订单Id查询系统来源、数据来源
         */
        qrySrvOrderBelongSysFlow: function (params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf", "qrySrvOrderBelongSys",params,success);
        },
        /**
         * 通过order_Id查询父流程psId
         */
        qryParentPsIdBySubOrderId: function (params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf", "qryParentPsIdBySubOrderId",params,success);
        },
        /**
         * 通过order_Id查询父流程以及子流程环节和状态
         */
        qryParentSubStatusByOrderId: function (params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf", "qryParentSubStatusByOrderId",params,success);
        },
        queryTaskInfoByTacheCode : function(param,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryTaskInfoByTacheCode"
                ,param
                ,success);
        },

    }
});
