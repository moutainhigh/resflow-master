
define(function(){
    return {
        queryEnum: function (enumCode,success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.GetEnumIntf", "queryEnum"
                ,enumCode,success);
        },
    }
});
