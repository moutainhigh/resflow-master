define(["module/flow/common/ext/gomNgc"], function (ngc) {
    return {
        queryEnum: function (enumCode, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.GetEnumIntf", "queryEnum"
                , enumCode, success);
        },
        queryEnum2: function (enumCode, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.GetEnumIntf", "queryEnum2"
                , enumCode, success);
        },
        queryEnum3: function (enumCode, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.GetEnumIntf", "queryEnum3"
                , enumCode, success);
        },
        orderInfoSave: function (orderInfoObj, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.InsertOrderInfoIntf", "orderInfoSave"
                , orderInfoObj, success);
        },
        orderInfoSubmit: function (orderInfoObj, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.InsertOrderInfoIntf", "orderInfoSubmit"
                , orderInfoObj, success);
        },
        querySelectedInfo: function (param, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.EditDraftIntf", "querySelectedInfo"
                , param, success);

        },
        querySelectInfo: function (SubscribeId, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.EditDraftIntf", "querySelectInfo"
                , SubscribeId, success);

        },
        queryCustInfoByAppId: function (SubscribeId, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.EditDraftIntf", "queryCustInfoByAppId"
                , SubscribeId, success);

        },
        orderInfoUpdate: function (orderInfoObj, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.UpdateOrderInfoIntf", "orderInfoUpdate"
                , orderInfoObj, success);
        },
        queryAttachment: function (CustId, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.QueryAttachmentIntf", "queryAttachment"
                , CustId, success);
        },
        queryDDKAttachment: function (srvID, origin, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.QueryAttachmentIntf", "queryDDKAttachment"
                , srvID, origin, success);
        },
        downLoadAttachMent: function (_url, params) {
            ngc.downLoad(_url, params);
        },
        delFileOnFtp: function (fileInfo, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.QueryAttachmentIntf", "delFileOnFtpAndDao"
                , fileInfo, success);
        },
        checkCircuitCode: function (params, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.CheckOrderInfoIntf", "checkCircuitCode"
                , params, success);
        },
        checkTradeId: function (params, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.CheckOrderInfoIntf", "checkTradeId"
                , params, success);
        },
        queryProvienceTree: function (success) {
            var param = new Object();
            param.flag = "province";
            return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.GetTreeIntf", "queryProvienceTree"
                , param, success);
        },
        queryProvienceTree2: function (param, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.GetTreeIntf", "queryProvienceTree"
                , param, success);
        },
        queryAreaIdByName: function (param, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.GetTreeIntf", "queryAreaIdByName"
                , param, success);
        },
        queryOperStaffInfo: function (success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.GetTreeIntf", "queryOperStaffInfo"
                , success);
        },
        /**
         * 登陆账号
         * @returns {*}
         */
        queryStaffInfo: function () {
            return ngc.callServerFunction("com.zres.project.localnet.portal.until.service.UntilServiceIntf", "queryStaffInfo");
        },
        //时间格式化方法，调用实例：formatDate(new Date().getTime());//2017-05-12 09:09:21
        formatDate: function (time) {
            var date = new Date(time);

            var year = date.getFullYear(),
                month = date.getMonth() + 1,//月份是从0开始的
                day = date.getDate(),
                hour = date.getHours(),
                min = date.getMinutes(),
                sec = date.getSeconds();
            var newTime = year + '-' +
                (month < 10 ? '0' + month : month) + '-' +
                (day < 10 ? '0' + day : day) + ' ' +
                (hour < 10 ? '0' + hour : hour) + ':' +
                (min < 10 ? '0' + min : min) + ':' +
                (sec < 10 ? '0' + sec : sec);

            return newTime;
        },
        synvalidataNewProcessdefine: function (params) {
            return ngc.callProcess("processDesignController", "validataNewProcessdefine", params);
        }
    }
});
