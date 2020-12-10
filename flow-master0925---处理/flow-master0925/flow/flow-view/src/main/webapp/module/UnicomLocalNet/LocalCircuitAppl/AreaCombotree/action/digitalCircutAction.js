
define(function(){
    return {
        queryEnum: function (enumCode,success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.GetEnumIntf", "queryEnum"
             ,enumCode,success);
        },
        orderInfoSave: function (orderInfoObj,success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.InsertOrderInfoIntf", "orderInfoSave"
                ,orderInfoObj,success);
        },
        orderInfoSubmit: function (orderInfoObj,success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.InsertOrderInfoIntf", "orderInfoSubmit"
                ,orderInfoObj,success);
        },
        querySelectedInfo:function(CustId,success){
            return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.EditDraftIntf", "querySelectedInfo"
                ,CustId,success);

        },
        orderInfoUpdate : function(orderInfoObj,success){
         return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.UpdateOrderInfoIntf", "orderInfoUpdate"
            ,orderInfoObj,success);
        },
        queryAttachment : function (CustId,success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.QueryAttachmentIntf", "queryAttachment"
                ,CustId,success);
        },
        downLoadAttachMent: function (_url,params) {
            ngc.downLoad(_url, params);
        },
        delFileOnFtp: function (fileInfo,success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.QueryAttachmentIntf", "delFileOnFtpAndDao"
                ,fileInfo,success);
        }
    }
});
