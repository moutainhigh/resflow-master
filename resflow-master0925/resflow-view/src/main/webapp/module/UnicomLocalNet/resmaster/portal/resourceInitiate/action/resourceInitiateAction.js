
define(function(){
    return {

        /**
         * 查询资源补录待办
         * @param param
         * @param success
         * @returns {*}
         */
        qryResSupOrdList:function(param,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.resourceInitiate.controller.ResSupplementDealController",
                "qryResSupOrdList",
                param,
                success);
        },

        /**
         * 资源补录待办各tab页数量
         * @param param
         * @param success
         * @returns {*}
         */
        qryResSupOrdCount:function(qryParams,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.resourceInitiate.controller.ResSupplementDealController",
                "qryResSupOrdCount",
                qryParams,
                success);
        },

        /**
         * 根据实例ID查询srvOrdId
         * @param instanceId
         * @param success
         * @returns {*}
         */
        qrySrvOrdIdByInstanceId:function(instanceId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.resourceInitiate.service.ResSupplementDealServiceIntf",
                "qrySrvOrdIdByInstanceId",
                instanceId,
                success);
        },

        /**
         * 工单提交
         * @param param
         * @param success
         * @returns {*}
         */
        submitOrder:function(param,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.resourceInitiate.controller.ResSupplementDealController",
                "submitOrderResSup",
                param,
                success);
        },

        //下拉数据接口
        queryProdTypeData:function(jsonObject,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.dict.service.UnicomSysDictServiceIntf","querySysDict",
                jsonObject,
                success);

        },

        queyCircuitInfoFromResource:function(param,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.resourceInitiate.service.ResourceInitiateServiceIntf","queyCircuitInfoFromResource",
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

        exportCircuitInfoFromResouce: function (_url,params) {
            ngc.downLoad(_url, params);
        },
        /**
         * 调用存量接口查询路由信息
         * @param param
         * @param success
         * @returns {*}
         */
        queryRouteInfo:function(param, success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.local.UnicomLocalOrderServiceIntf","queryRouteInfo", param, success);
        },
        /**
         * 资源补录初始化电路信息
         * @param param
         * @param success
         */
        initCircuitInfo:function(param, success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.resourceInitiate.service.ResourceInitiateServiceIntf","initCircuitInfo", param, success);
        },
        /**
         * 初始化部门树
         * @param param
         * @param success
         * @returns {*}
         */
        queryDeptInfo:function (success) {
            return  ngc.callServerFunction("com.zres.project.localnet.portal.resourceInitiate.service.ResourceInitiateServiceIntf","queryDeptInfo",
                success);
        },
        //初始化专业下拉多选框
        initSpecialtyInfo:function(param, success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.resourceInitiate.service.ResourceInitiateServiceIntf","initSpecialtyInfo",
                param,
                success);
        },
        /**
         * 资源补录起流程入库
         * @param param
         * @param success
         * @returns {*}
         */
        startResourceInitiateFlow:function(param, success){
            //return  ngc.callServerFunction("com.zres.project.localnet.portal.resourceInitiate.service.ResourceInitiateServiceIntf","startResourceInitiateFlow",
            return  ngc.callServerFunction("com.zres.project.localnet.portal.resourceInitiate.controller.ResourceInitiateController","startResourceInitiateFlow",
                param,
                success);
        },
        /**
         * 根据实例Id查询电路信息
         * @param param
         * @param success
         * @returns {*}
         */
        querySrvOrdInfoByInstanceId:function(param, success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.resourceInitiate.service.ResourceInitiateServiceIntf","querySrvOrdInfoByInstanceId",
                param,
                success);
        },
        /**
         * 根据实例ID判断电路是否有未完成资源补录单
         * @param param
         * @param success
         * @returns {*}
         */
        queryResourceInitiateInfoByInstanceId:function(param, success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.resourceInitiate.service.ResourceInitiateServiceIntf","queryResourceInitiateInfoByInstanceId",
                param,
                success);
        },
        /**
         * 资源配置
         * @param param
         * @param success
         * @returns {*}
         */
        resConfig:function(param,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.resourceInitiate.service.ResSupplementDealServiceIntf",
                "resConfigSupplement",
                param,
                success);
        },
        //二干补录资源信息
        queryResourceOrderInfoSec:function(srvOrdId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.resourceInitiate.service.ResourceInitiateServiceIntf",
                "queryResourceOrderInfoSec"
                ,srvOrdId
                ,success);
        },
        // 本地补录资源信息
        queryResourceOrderInfoLocal:function(srvOrdId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.resourceInitiate.service.ResourceInitiateServiceIntf",
                "queryResourceOrderInfoLocal"
                ,srvOrdId
                ,success);
        },
    }
});
