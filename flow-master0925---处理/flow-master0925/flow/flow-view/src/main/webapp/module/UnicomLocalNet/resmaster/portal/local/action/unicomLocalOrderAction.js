
define(function(){
    return {
        //下拉数据接口
        queryProdTypeData:function(jsonObject,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.dict.service.UnicomSysDictServiceIntf","querySysDict",
                jsonObject,
                success);

        },
        //查询草稿单、全部申请单、已完成、已提交申请单
        queryLocalApplyOrderData:function(jsonObject,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.local.UnicomLocalOrderServiceIntf","queryLocalApplyOrderData",
                jsonObject,
                success);

        },
        //查询全部申请单、已完成、已提交申请单数量
        queryLocalApplyOrderCount:function(jsonObject,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.local.UnicomLocalOrderServiceIntf","queryLocalApplyOrderCount",
                jsonObject,
                success);

        },
        //删除草稿单
        delDraftInfo:function(cstOrdId,srvordIds,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.DelOrderInfoIntf","draftDeleteByCustID",
                cstOrdId,srvordIds,
                success);

        },
        //判断数组是否为空
        isArrNull: function (arr) {
            if (JSON.stringify(arr) == "[]") {
                return true;
            }
            return false;
        },
        exportPageData: function (_url,params) {
            ngc.downLoad(_url, params);
        },
        //本地申请页面产品类型下拉数据接口
        queryProdTypeLocal:function(jsonObject,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.dict.service.UnicomSysDictServiceIntf","queryProdTypeLocal",
                jsonObject,
                success);

        },

    }
});
