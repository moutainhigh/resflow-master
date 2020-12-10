define(function(){
    return {
        queryOrderInfo:function(jsonObject,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.order.service.GomOrderQueryServiceIntf","queryWo"
                ,jsonObject,
                success);
        },
        //下拉数据接口
        queryProdTypeData:function(jsonObject,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.dict.service.UnicomSysDictServiceIntf","querySysDict",
                jsonObject,
                success);

        },
        /**
         * 登陆账号
         * @returns {*}
         */
        queryStaffInfo:function(){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.until.service.UntilServiceIntf","queryStaffInfo" );
        },
        //导出数据
        exportPageData: function (_url,params) {
            ngc.downLoad(_url, params);
        }

    }
});
