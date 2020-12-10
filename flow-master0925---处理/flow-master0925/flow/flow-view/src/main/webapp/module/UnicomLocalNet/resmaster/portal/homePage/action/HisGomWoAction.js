define(function(){
    return {
        queryProdTypeData:function(param,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.dict.service.UnicomSysDictServiceIntf","querySysDict",
                param,
                success);
        },
        /**
         * 查询当前处理人处理过的工单
         * @param param
         * @param success
         * @returns {*}
         */
        queryWoInfoForHis:function(param ,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.order.service.OrderQueryServiceIntf","queryWoInfoForHis"
                ,param ,success);
        },
        queryStaffInfo:function(){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.until.service.UntilServiceIntf","queryStaffInfo" );
        },

        //单据类型下拉
        queryItemType:function (jsonObject,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.dict.service.UnicomSysDictServiceIntf","querySysDict",
                jsonObject,
                success);
        },

        exportWoOrderInfo: function (_url,params) {
            ngc.downLoad(_url, params);
        },
    }
});
