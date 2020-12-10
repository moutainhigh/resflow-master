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
        qryCreatePageHandleDep:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryCreatePageHandleDep"
                ,params,success);
        },
        qryDepartParent:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryDepartParent"
                ,params,success);
        },
        qrySearchOrgPerDepPullDown:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qrySearchOrgPerDepPullDown"
                ,params,success);
        },
        qrySearchOrgPerDepSingleDown:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qrySearchOrgPerDepSingleDown"
                ,params,success);
        },
        qrySearchOrgPerDepPullDownSub:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qrySearchOrgPerDepPullDownSub"
                ,params,success);
        },
        qrySearchOrgPerDepart:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qrySearchOrgPerDepart"
                ,params,success);
        },
        qrySearchContactsSel:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qrySearchContactsSel"
                ,params,success);
        },
        qrySearchContacts:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qrySearchContacts"
                ,params,success);
        },
        addSearchContacts:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","addSearchContacts"
                ,params,success);
        },
        deleteSearchContacts:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","deleteSearchContacts"
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
        querySpecialtyConfig:function(param,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","querySpecialtyConfig"
                ,param
                ,success);
        },
        queryDispatchInfoByCstOrdId:function(param,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","queryDispatchInfoByCstOrdId"
                ,param,success);
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
         * 跨域全程调测退单查询二干数据制作数据
         */
        qrySecondDataMakeList:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qrySecondDataMakeList",
                params,success);
        },
        /**
         * 跨域全程调测退单查询二干资源施工数据
         */
        qrySecondResMakeLists:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qrySecondResMakeList",
                params,success);
        },
        /**
         * 跨域全程调测退单查询二干数据制作数据
         */
        qrySubLocalTestDataList:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qrySubLocalTestDataList",
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
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        queryCheckInfo:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","queryCheckInfo"
                ,params,success);
        },
        /**
         * 查询资源核查信息
         * @param qryResCheckInfo
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        qryResCheckInfo:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryResCheckInfo"
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
         * 查询业务订单归属哪个系统
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        qrySrvOrderBelongSys:function(params){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qrySrvOrderBelongSys",params);
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
         * 根据srvOrdIds查询调单信息
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        queryDispatchInfoByDispatchIds:function(param,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","queryDispatchInfoByDispatchIds"
                ,param,success);
        },
        /**
         * 根据流程定单ID查询流程定单
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        getFlowOrderById: function (param, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf", "getFlowOrderById"
                , param, success);
        },
        /**
         * 根据流程定单ID查询流程定单
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        getFlowOrderByParentId: function (param, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf", "getFlowOrderByParentId"
                , param, success);

        },

        /**
         * 根据srvOrdId查询电路信息
         * @param param
         * @param success
         * @returns {*}
         */
        querySrvInfoBySrvOrdId : function(param,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","querySrvInfoBySrvOrdId"
                ,param,success);
        },
        /**
         *
         * @param param
         * @param success
         * @returns {*}
         */
        queryDispatchOrderIdFromRelateTable : function(param, success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","queryDispatchOrderIdFromRelateTable"
                ,param,success);
        },
        /**
         *
         * @param param
         * @param success
         * @returns {*}
         */
        queryItemType:function (jsonObject,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.dict.service.UnicomSysDictServiceIntf","querySysDict",
                jsonObject,
                success);
        },
        /**
         * 查询电路调度环节需要指定处理人的环节
         * @param param
         * @param success
         * @returns {*}
         */
        qryDealUserTacheConfig : function(param,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryDealUserTacheConfig"
                ,param,success);
        },
        /**
         * 查询电路调度保存的下游环节处理对象
         * @param params
         * @param success
         * @returns {*}
         */
        qryTacheDealUserByOrderId:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryTacheDealUserByOrderId"
                ,params,success);
        },
        /**
         * 查询专业核查的工单
         * @param params
         * @param success
         * @returns {*}
         */
        qryAllSpecialCheckWoOrder:function (orderId,success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.CheckOrderServiceIntf","qryAllSpecialCheckWoOrder"
                ,orderId,success);
        },
        /**
         * 查询单据归属省分
         * @param orderId
         * @param success
         * @returns {*}
         */
        queryProvinceName:function (param,success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.CheckOrderServiceIntf","queryProvinceName"
                ,param, success);
        },
        /**
         * 查询电路调度环节，电路编号必填的省份配置
         * @param success
         */
        queryProvinceConf:function(param, success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.CheckOrderServiceIntf","queryProvinceConf"
                ,param, success);
        },
        saveOpinionInfo:function(param, success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","saveOpinionInfo"
                ,param, success);
        },
        queryIfMainOrg:function(param, success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","queryIfMainOrg"
                ,param, success);
        },
        saveCircuitCodeBySrvOrdId:function(param, success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.CheckOrderServiceIntf","saveCircuitCodeBySrvOrdId"
                ,param, success);
        },
        /**
         * 查询省份DIA直接调用接口的省份配置
         * @param param
         * @param success
         * @returns {*}
         */
        queryProvinceAutoConf:function(param, success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.CheckOrderServiceIntf","queryProvinceAutoConf"
                ,param, success);
        },
        /**
         * 开通单下发工建系统
         * add by wang.gang2
         * @param param
         * @param success
         * @returns {*}
         */
        orderSendConstruct:function(param, success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.webservice.construct.OssToConstructOrderSendIntf","orderSend"
                ,param, success);
        },
        /**
         * add by wang.gang2
         * 查询对接工建得省份 跟配置得核查下发工建得按钮配置区分开
         * @param param
         * @param success
         * @returns {*}
         */
        queryConstructConf:function(param, success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","queryConstructConf"
                ,param,success);
        },
        /**
         * 查询是否为国际公司的标识
         * @param param
         * @param success
         * @returns {*}
         */
        qryIfPopConfigView:function(param, success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","qryIfPopConfigView"
                ,param, success);
        },
        /**
         *查询电路属性某属性来做判断
         * @param param
         * @param success
         * @returns {*}
         */
        queryAttrInfos:function(param, success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","queryAttrInfos"
                ,param,success);
        },
        getAttachInfo:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","getAttachInfo"
                ,params,success);
        },
        feedBackToOneDry:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf","feedBackToOneDry"
                ,params,success);
        },
        /**
         * 通过order_Id查询父流程psId
         */
        qryParentPsIdBySubOrderId: function (params){
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.OrderDealServiceIntf", "qryParentPsIdBySubOrderId",params);
        },
        equipSubmit:function (param, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.EquipMentRecycleServiceIntf","addEquip"
                ,param,success);
        },
        queryIsBussSpecialty:function (param, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.StandardAddressIntf","queryIsBussSpecialty"
                ,param,success);
        }
    }
});
