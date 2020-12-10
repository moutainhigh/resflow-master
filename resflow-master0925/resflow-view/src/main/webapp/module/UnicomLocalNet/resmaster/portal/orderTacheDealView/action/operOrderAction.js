define(function(){
    return {
        submitOrder:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","submitOrder"
                ,params
                ,success);
        },
        sendWoOrder:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","sendWoOrder"
                ,params
                ,success);
        },
        /*qrySecondDataMakeList:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qrySecondDataMakeList",
                params,success);
        },*/
        qryProvinceName:function(params,success){
        return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryProvinceName"
            ,params
            ,success);
        },
        qryProvinceValue:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryProvinceValue"
                ,params
                ,success);
        },
        getFreeWoOrder:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","getFreeWoOrder"
                ,params
                ,success);
        },
        transferWoOrder:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","transferWoOrder"
                ,params
                ,success);
        },
        goBackOrder:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","goBackOrder"
                ,params
                ,success);
        },
        disableOrder:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","disableOrder"
                ,params
                ,success);
        },
        qryDepart:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryDepart"
                ,params,success);
        },
        qryDepartParent:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryDepartParent"
                ,params,success);
        },
        qryChildNodeData:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryChildNodeData"
                ,params,success);
        },
        getStaffInfoDeptListUnit:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","getStaffInfoDeptListUnit"
                ,params,success);
        },
        qryChildNodeDataT:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryChildNodeDataT"
                ,params,success);
        },
        getCurrentUserInfo:function(success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","getCurrentUserInfo"
                ,success);
        },
        getsequenceNum:function(param,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","getsequenceNum"
                ,param
                ,success);
        },
        rollBackWoOrder:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","rollBackWoOrder"
                ,params
                ,success);
        },
        querySpecialtyConfig:function(areaId,serviceId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","querySpecialtyConfig"
                ,areaId,
                serviceId,
                success);
        },
        queryDispatchInfoBySrvOrdId:function(srvOrdId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","queryDispatchInfoBySrvOrdId"
                ,srvOrdId,success);
        },
        /**
         * 查询业务订单--电路信息  核查流程时（特殊：核查调度不选专业）
         */
        qrySrvOrdListCheck:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.localStandbyInfo.service.OrderStandbyServiceIntf","qrySrvOrdListCheck",
                params,success);
        },
        /**
         * 查询业务订单--电路信息  主流程
         */
        qrySrvOrdList:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.localStandbyInfo.service.OrderStandbyServiceIntf","qrySrvOrdList",
                params,success);
        },
        /**
         * 查询业务订单--电路信息  子流程
         */
        qrySrvOrdChildList:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.localStandbyInfo.service.OrderStandbyServiceIntf","qrySrvOrdChildList",
                params,success);
        },
        /**
         * 查询资源配置页面报文
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        resConfig :function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","resConfig"
                ,params
                ,success);
        },
        qryCircuitAreaInfo:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryCircuitAreaInfo"
                ,params
                ,success);
        },
        /**
         * 保存电路信息对应的专业配置
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        saveSpecialtyConfigInfo:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","saveSpecialtyConfigInfo"
                ,params
                ,success);
        },
        saveResConstructConfigInfo:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","saveResConstructConfigInfo"
                ,params
                ,success);
        },
        /**
         * 查询电路对应的专业配置信息进行回显
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        queryPropertyConfig:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","queryPropertyConfig"
                ,params
                ,success);
        },
        queryOpinionInfo:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","queryOpinionInfo"
                ,params
                ,success);
        },

        /**
         * 查询岗位
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        qryJob:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryJob"
                ,params,success);
        },
        /**
         * 查询之前入库的岗位数据
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        /*qryDispObj:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryDispObj"
                ,params,success);
        },*/
        /**
         * 返回已保存的岗位数据
         * @param params
         * @param success
         * @returns {*}
         */
        qryDispObjByOrderId:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryDispObjByOrderId"
                ,params,success);
        },
        /**
         * 查询信息用来刷新调单标题和内容
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        getDispatchInfo:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","getDispatchInfo"
                ,params,success);
        },
        /**
         * 查询核查反馈信息
         * @param paramssaveCheckInfo
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        queryCheckInfo:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","queryCheckInfo"
                ,params,success);
        },
        /**
         * 查询跨域流程发往单位--用于退单省份查询
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        getProvinceName:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","getProvinceName"
                ,params,success);
        },
        /**
         * 查询核查反馈信息
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        saveCheckInfo:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","saveCheckInfo"
                ,params,success);
        },
        //同步
        saveCheckInfo2:function(params){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","saveCheckInfo"
                ,params);
        },
        /**
         * 查询子流程
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        qrychildFlowInfo:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","getListChildFlow"
                ,params,success);
        },

        /**
         * 子流程环节，查询父流程的psid
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        getMainFlowPsId:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","getMainFlowPsId"
                ,params,success);
        },
        qryOrderIfConfigDispatch:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.SecDealLocalServiceInf","qryOrderIfConfigDispatch"
                ,params,success);
        },
        /**
         * 查询电路编号
         * @param params
         * @param success
         * @returns {*}
         */
        qryCircuitNum:function(params){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.SecDealLocalServiceInf","qryCircuitNum"
                ,params);
        },
        queryisSendSpeciallocal:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","queryisSendSpeciallocal"
                ,params,success);
        },
        /**
         * 查询业务订单归属哪个系统
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        qrySrvOrderBelongSys:function(params){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qrySrvOrderBelongSys",params);
        },

        /**
         * 查询父订单正在执行中的子流程
         * @param orderId
         * @returns {*}
         */
        qryChildOrderDealing:function(orderId){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.SecDealLocalServiceInf","qryChildOrderDealing"
                ,orderId);
        },
        /**
         * 查询附件
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        getAnnex:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.annex.GetAnnexInfoIntf","getAnnexInfo"
                ,params,success);
        },
        /**
         * 查询是否有追单
         * @param orderId
         * @param cstOrdId
         * @returns {*}
         */
        queryIfTrack:function(orderId, cstOrdId){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDetailsServiceIntf","queryIfTrack"
                ,orderId
                ,cstOrdId);
        },
        /**
         * 查询专业核查
         * @param orderId
         * @param success
         * @returns {*}
         */
        qrySpecialData:function(orderId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.CheckOrderServiceIntf","qrySpecialData"
                ,orderId,success);
        },

        /**
         * 查询下发本地核查的区域
         * @param orderId
         * @param success
         * @returns {*}
         */
        qryToLocalCheckData:function(orderId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.CheckOrderServiceIntf","qryToLocalCheckData"
                ,orderId,success);
        },
        /**
         * 根据电路ID查询系统源、数据源信息
         * @param param
         * @param success
         * @returns {*}
         */
        querySystemInfo:function(param, success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.CheckOrderServiceIntf","querySystemInfo"
                ,param,success);
        },
        /**
         *查询电路属性某属性来做判断
         * @param param
         * @param success
         * @returns {*}
         */
        /*queryAttrInfos:function(param, success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","queryAttrInfos"
                ,param,success);
        },*/
    }
});
