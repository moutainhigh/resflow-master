define(function () {
    return {
        /**
         * 登陆账号
         * @returns {*}
         */
        queryStaffInfo:function(){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.until.service.UntilServiceIntf","queryStaffInfo" );
        },

        /**
         * 导出
         * @param _url
         * @param params
         */
        exportGomOrderListData: function (_url, params) {
            ngc.downLoad(_url, params);
        },

        /**
         * 分页查询消息
         * @param params
         * @param success
         * @returns {*}
         */

        queryMessageList: function (params, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.logInfo.service.TacheDealLogIntf", "queryMessageList",
                params, success);
        },

        /**
         * 下拉框
         * @param jsonObject
         * @param success
         * @returns {*}
         */
        queryProdTypeData: function (jsonObject, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.dict.service.UnicomSysDictServiceIntf", "querySysDict",
                jsonObject,
                success);
        },

        deleteMessageList: function (params, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.logInfo.service.TacheDealLogIntf", "deleteMessageList",
                params, success);
        }
    }
});