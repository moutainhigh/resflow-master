
define(function(){
    return {
        queryResEquip:function(jsonObject,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.EquipMentRecycleServiceIntf","queryResEquip",
                jsonObject,
                success);
        },
        queryAlongArea:function(success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.flowdealinfo.service.EquipMentRecycleServiceIntf","queryAlongArea",
                success);
        }
    }
});
