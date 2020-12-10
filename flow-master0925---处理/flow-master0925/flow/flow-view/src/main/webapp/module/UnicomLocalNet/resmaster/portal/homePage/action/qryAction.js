
define(function(){
    return {
        queryStaffInfo:function(){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.until.service.UntilServiceIntf","queryStaffInfo" );
        },
        downLoadAttachMent: function (_url,params) {
            ngc.downLoad(_url, params);
        },
       /* 查询管理员信息*/
        queryAdminInfo:function(staffId ,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.until.service.UntilServiceIntf","queryAdminInfo"
                ,staffId
                ,success);
        },
        /* 配置的二干信息*/
        queryUrl:function(systemSetup ,success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.until.service.UntilServiceIntf","queryUrl"
                ,systemSetup
                ,success);
        },
        queryPurviewBystaffId : function (param, success) {
            return  ngc.callServerFunction("com.zres.project.localnet.portal.until.service.UntilServiceIntf","queryPurviewBystaffId"
                ,param
                ,success);
        },
        queryRouteInfoUrl : function (param, success) {
            return  ngc.callServerFunction("com.zres.project.localnet.portal.until.service.UntilServiceIntf","queryRouteInfoUrl"
                ,param
                ,success);
        }
        /*,
        queryNowAffiche: function(count, success){
            ngc.callServerFunction("com.ztesoft.res.frame.affiche.inf.AfficheSerivceIntf","queryNowAffiche"
                ,parseInt(count)
                ,success);
        },
        getEqpCount: function(success){
            ngc.callServerFunction("com.zres.product.resmaster.device.common.intf.DeviceServiceForIdcIntf","getEqpCount" //数据设备
                ,success);
        },
        getRackOccupyInfo: function(region,time,success){
            ngc.callServerFunction("com.zres.product.resmaster.device.common.intf.DeviceServiceForIdcIntf","getRackOccupyInfo"//机架
                ,region
                ,time
                ,success);
        },
        getRackUOccupyInfo: function(time,success){
            ngc.callServerFunction("com.zres.product.resmaster.device.common.intf.DeviceServiceForIdcIntf","getRackUOccupyInfo"//机架U
                ,time
                ,success);
        },
        getRegionRoomsCollectInfos: function(map,success){
            ngc.callServerFunction("com.zres.product.resmaster.space.intf.common.SpaceResBasicServiceIntf","getRegionRoomsCollectInfos"//机架
                ,map
                ,success);
        }*/
    }
});
