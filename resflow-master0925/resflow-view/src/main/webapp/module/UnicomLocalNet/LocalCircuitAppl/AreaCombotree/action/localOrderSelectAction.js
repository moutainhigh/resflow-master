
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
        queryLocalApplyOrderCheckData:function(jsonObject,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.local.UnicomLocalOrderServiceIntf","queryLocalApplyOrderCheckData",
                jsonObject,
                success);

        },
        //查询资源电路信息
        queryResData:function(jsonObject,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.local.UnicomLocalOrderServiceIntf","queryResData",
                jsonObject,
                success);

        },
        //查询机房信息
        queryResourceData:function(jsonObject,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.local.UnicomLocalOrderServiceIntf","queryResourceData",
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
        delDraftInfo:function(cstOrdId,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.DelOrderInfoIntf","draftDeleteByCustID",
                cstOrdId,
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

        //查询电路状态判断是否在途
        querySrvOrderState:function(param,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.CheckOrderInfoIntf","querySrvOrderState",
                param,
                success);
        },
        /**
         * 调用资源接口查询电路信息
         * @param param
         * @param success
         *
         * @returns {*}
         */
        queryStockCircuitInfo:function(param, success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.local.UnicomLocalOrderServiceIntf","queryStockCircuitInfo",
                param,
                success);
        },
        queryCountByInstanceId:function(param, success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.local.UnicomLocalOrderServiceIntf","queryCountByInstanceId",
                param,
                success);
        },
        queryCircuitInfoBySrvOrdId:function(param, success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.local.UnicomLocalOrderServiceIntf","queryCircuitInfoBySrvOrdId", param, success);
        },
        /**
         * 存量电路信息导出
         * @param _url
         * @param params
         */
        exportStockCircuitInfo: function (_url,params) {
            ngc.downLoad(_url, params);
        },
        /**
         * 调用资源接口查询路由信息
         * @param param
         * @param success
         */
        queryRouteInfo:function(param, success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.local.UnicomLocalOrderServiceIntf","queryRouteInfo", param, success);
        }

    }
});
