
define(function(){
    return {
        //资源回滚
        businessRollback:function(params,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.until.service.TestHelpServiceIntf","businessRollback",
                params,
                success);
        },
        delApplicationByApplCode:function(params,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.until.service.TestHelpServiceIntf","delApplicationByApplCode",
                params,
                success);
        },
        queryApplicationLog:function(params,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.until.service.TestHelpServiceIntf","getApplicationLog",
                params,
                success);
        },
    }
});
