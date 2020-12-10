
define(function(){
    return {
        //查询存量客户列表，通过客户名称或者客户编码
        queryCustNameFromBizData:function(jsonObject,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.local.QueryCustNameFromBizServiceIntf","queryCustNameFromBizData",
                jsonObject,
                success);
        }
    }
});
