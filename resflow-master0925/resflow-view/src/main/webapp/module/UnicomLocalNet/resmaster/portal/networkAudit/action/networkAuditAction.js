define(function(){
    return {

        /**
         * 查询业务稽核
         * @param params
         * @param success
         * @returns {*|{mess, resultStat}}
         */
        queryNetworkAuditData:function(params,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.networkAudit.NetworkAuditIntf","queryNetworkAuditData",
                params,success);
        },
        /**
         * 导出业务稽核
         * @param _url
         * @param params
         */
        exportNetWorkAuditData: function (_url,params) {
            ngc.downLoad(_url, params);
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

    }
});
