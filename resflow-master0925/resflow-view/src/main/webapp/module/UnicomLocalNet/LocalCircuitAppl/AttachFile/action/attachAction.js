define(function(){
    return {
        queryProvienceTree: function (param,success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.applpage.service.GetTreeIntf", "queryProvienceTree"
             ,param,success);
        },

    }
});
