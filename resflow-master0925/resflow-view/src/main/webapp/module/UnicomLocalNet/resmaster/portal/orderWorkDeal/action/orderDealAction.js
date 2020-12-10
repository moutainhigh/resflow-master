define(function(){
    return {
        queryOrderInfo:function(success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","queryOrderInfo"
                ,success);
        },
        //下拉数据接口
        queryProdTypeData:function(jsonObject,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.dict.service.UnicomSysDictServiceIntf","querySysDict",
                jsonObject,
                success);

        }
    }
});
