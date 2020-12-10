define(function() {
	return {

        /**查询用户分配的产品**/
        queryProdAssingInfo: function (userId, type, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.prodApply.service.ProdRoleApplyServiceIntf","queryProdAssingInfo",
                userId, type, success);
        },

        /**查询岗位分配的产品**/
        queryProdGroupAssingInfo: function (userId, type, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.prodApply.service.ProdRoleApplyServiceIntf","queryProdGroupAssingInfo",
                userId, type, success);
        },

        /**
         * 登陆账号
         * @returns {*}
         */
        queryStaffInfo:function(){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.until.service.UntilServiceIntf","queryStaffInfo" );
        },

        /**
         * 保存产品到用户下
         * @returns {*}
         */
        saveProdStaff:function(params, success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.prodApply.service.ProdRoleApplyServiceIntf","saveProdStaff",
                params,
                success);
        },

        /**
         * 保存产品到岗位下
         * @returns {*}
         */
        saveProdGroup:function(params, success){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.prodApply.service.ProdRoleApplyServiceIntf","saveProdGroup",
                params,
                success);
        },

        /**查询登录用户可分配，且被分配用户不具备的权限 **/
        selectRolesListDistributableByStaff :function(appName,domainId,jsonObject){
            return ngc.callDynDataServerFunctionApp(appName,domainId,"com.ztesoft.res.frame.user.authority.inf.RoleDistributionIntf","selectListRoles"
                ,jsonObject);
        },

        /**查询用户具备的权限 **/
        selectRolesListInStaffId :function(appName,domainId,jsonObject){
            return ngc.callDynDataServerFunctionApp(appName,domainId,"com.ztesoft.res.frame.user.authority.inf.RoleDistributionIntf","selectListRolesHave"
                ,jsonObject);
        },

        /**权限组下所有维度**/
        selectListDimensionChilds :function(appName,domainId,dimensionTypeId,pid,envDomainId){
            return ngc.callDynDataServerFunctionApp(appName,domainId,"com.ztesoft.res.frame.user.authority.inf.RoleDistributionIntf","selectListDimensionChildsSupportUserManagerRole"
                ,dimensionTypeId,pid,envDomainId);
        },

        /**二级管理员所属部门区域下，权限组下的所有维度**/
        selectListDimensionChildsSupportRegionId :function(appName,domainId,dimensionTypeId,pid,envDomainId,regionId){
            return ngc.callDynDataServerFunctionApp(appName,domainId,"com.ztesoft.res.frame.user.authority.inf.RoleDistributionIntf","selectListDimensionChildsSupportRegionId"
                ,dimensionTypeId,pid,envDomainId,regionId);
        },

        /**根据权限点查询维度**/
        selectListDimensionForStaffRole :function(appName,domainId,jsonObject){
            return ngc.callDynDataServerFunctionApp(appName,domainId,"com.ztesoft.res.frame.user.authority.inf.RoleDistributionIntf","selectListDimensionRole"
                ,jsonObject);
        },

        /**获取与权限点有关的专业**/
        selectListByRoles:function(appName,domainId){
            return ngc.callDynDataServerFunctionApp(appName,domainId,"com.ztesoft.res.frame.database.operation.inf.DatabaseQueryServiceInf","selectListByRoles"
            );
        },

        /**获取所有维度类型**/
        selectListDimensionType:function(appName,domainId){
            return ngc.callDynDataServerFunctionApp(appName,domainId,"com.ztesoft.res.frame.user.authority.inf.RoleDistributionQueryIntf","selectListDimensionType"
            );
        },
	}
});
