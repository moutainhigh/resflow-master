
define(function(){
    return {
        queryStandardAddressInfo:function(jsonObject,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.StandardAddressIntf","queryStandardAddressInfo",
                jsonObject,
                success);
        },
        queryAlongArea:function(success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.EquipMentRecycleServiceIntf","queryAlongArea",
                success);
        }
    }
});
