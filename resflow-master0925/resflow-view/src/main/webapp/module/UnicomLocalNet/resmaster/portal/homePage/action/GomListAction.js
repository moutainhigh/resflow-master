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
        countWo:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.order.service.OrderQueryServiceIntf","countWoList"
                ,params,success);
        },
        //下拉数据接口
        queryProdTypeData:function(jsonObject,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.dict.service.UnicomSysDictServiceIntf","querySysDict",
                jsonObject,
                success);

        },
        qryMsmSwitchByArea:function (params,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryMsmSwitchByArea",
                params,
                success);
        },
        qryInterfaceUrl:function (params,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.dict.service.UnicomSysDictServiceIntf","qryInterfaceUrl",
                params,
                success);
        },
        /**
         * 导出工单查询
         */
        exportGomListData: function (_url,params) {
            ngc.downLoad(_url, params);
        },
    }
});
