define(function() {
	return {
		/**查询用户管理**/
		selectList:function(appName,domainId,jsonObject,success){
			return ngc.callDynDataServerFunctionApp(appName,domainId,"com.ztesoft.res.frame.user.manager.inf.StaffBaseServiceIntf","queryUser"
					,jsonObject,success
					);

		},

        queryUserContainWorkGroup:function (domainId,jsonObject,success) {
            return ngc.callDynDataServerFunction(domainId,"com.ztesoft.res.frame.user.manager.inf.StaffBaseServiceIntf","queryUserContainWorkGroup"
                ,jsonObject,success
            );
        },
		
		
		/**新增用户**/
		saveUserInfo:function(appName,domainId,jsonObject){
			return ngc.callDynDataServerFunctionApp(appName,domainId,"com.ztesoft.res.frame.user.manager.inf.StaffBaseServiceIntf","saveUserInfo"
					,jsonObject
					);

		},
		
		
		/**删除用户**/
		deleteUserInfo:function(appName,domainId,jsonObject){
			return ngc.callDynDataServerFunctionApp(appName,domainId,"com.ztesoft.res.frame.user.manager.inf.StaffBaseServiceIntf","deleteUserInfo"
					,jsonObject
					);

		},

        /**删除用户**/
        unlock: function(appName,domainId,staffId,callback){
            return ngc.callDynDataServerFunctionApp(appName,domainId,"com.ztesoft.res.frame.user.manager.inf.StaffBaseServiceIntf","unlock"
                ,staffId,callback);
        },
		
		/**根据Id查询**/
		queryUserInfo:function(appName,domainId,jsonObject){
			return ngc.callDynDataServerFunctionApp(appName,domainId,"com.ztesoft.res.frame.user.manager.inf.StaffBaseServiceIntf","queryUserInfo"
					,jsonObject
					);

		},

        /**根据Id查询**/
        selectExByPrimaryKey:function(appName,domainId,staffId){
            return ngc.callDynDataServerFunctionApp(appName,domainId,"com.ztesoft.res.frame.user.manager.inf.StaffBaseServiceIntf","selectExByPrimaryKey"
                ,staffId);

        },
		
		
		/**获取主键**/
		getPrimaryKey:function(appName,domainId,jsonObject){
			return ngc.callDynDataServerFunctionApp(appName,domainId,"com.ztesoft.res.frame.user.manager.inf.StaffBaseServiceIntf","getPrimaryKey"
					);

		},
		
		/**工作状态**/
		workState:function(appName,domainId,jsonObject,success){
			return ngc.callDynDataServerFunctionApp(appName,domainId,"com.ztesoft.res.frame.user.manager.inf.StaffStateServiceIntf","selectList"
					,jsonObject,success);

		},
		
		/**职务**/
		selectListByCurrendUser:function(appName,domainId){
			return ngc.callDynDataServerFunctionApp(appName,domainId,"com.ztesoft.res.frame.user.manager.inf.TitleServiceIntf","selectListByCurrendUser"
					);

		},

		
		/**所属机构组织**/
		selectDepartmentServiceIntf :function(appName,domainId,jsonObject){
			return ngc.callDynDataServerFunctionApp(appName,domainId,"com.ztesoft.res.frame.user.manager.inf.DepartmentServiceIntf","selectList"
					,jsonObject);

		},
		
		
		/**安全策略**/
		selectSafeStrategyList :function(appName,domainId,jsonObject){
			return ngc.callDynDataServerFunctionApp(appName,domainId,"com.ztesoft.res.frame.user.password.inf.SafeStrategyServiceIntf","selectList"
					,jsonObject);

		},
		
		
		
		/**查询登录用户可分配，且被分配用户不具备的权限组**/
		selectListDistributableByStaff :function(appName,domainId,jsonObject){
			return ngc.callDynDataServerFunctionApp(appName,domainId,"com.ztesoft.res.frame.user.authority.inf.RoleDistributionIntf","selectListRolegrp"
					,jsonObject);

		},


		/**查询用户具备的权限组**/
		selectListInStaffId :function(appName,domainId,jsonObject){
			return ngc.callDynDataServerFunctionApp(appName,domainId,"com.ztesoft.res.frame.user.authority.inf.RoleDistributionIntf","selectListRolegrpHave"
					,jsonObject);

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



		/**根据权限组查询维度**/
		selectListDimensionForStaffRolegrp :function(appName,domainId,jsonObject){
			return ngc.callDynDataServerFunctionApp(appName,domainId,"com.ztesoft.res.frame.user.authority.inf.RoleDistributionIntf","selectListDimensionRolegrp"
					,jsonObject);

		},


		/**根据权限点查询维度**/
		selectListDimensionForStaffRole :function(appName,domainId,jsonObject){
			return ngc.callDynDataServerFunctionApp(appName,domainId,"com.ztesoft.res.frame.user.authority.inf.RoleDistributionIntf","selectListDimensionRole"
					,jsonObject);

		},



		saveRoleDistribution :function(appName,jsonObject,success){
			return ngc.callDynDataServerFunctionApp(appName,"default","com.ztesoft.res.frame.user.authority.inf.RoleDistributionIntf","saveRoleDistribution"
					,jsonObject,success);

		},


		/**获取与权限点有关的专业**/
		selectListByRoles:function(appName,domainId){
			return ngc.callDynDataServerFunctionApp(appName,domainId,"com.ztesoft.res.frame.database.operation.inf.DatabaseQueryServiceInf","selectListByRoles"
					);

		},



		/**用户资源树**/
		getStaffTree:function(appName,domainId,staffTreeResId,roleId,dimensionTypeId,id,success){
			return ngc.callDynDataServerFunctionApp(appName,domainId,"com.ztesoft.res.frame.user.manager.inf.StaffBaseServiceIntf","getStaffTree"
					,staffTreeResId,roleId,dimensionTypeId,id,success);

		},



		/**初始化右键菜单**/
		selectListForLoginStaffByStaffTreeId:function(appName,domainId,treeId,success){
			return ngc.callDynDataServerFunctionApp(appName,domainId,"com.ztesoft.res.frame.database.operation.inf.PubFunctionMenuServiceIntf","selectListForLoginStaffByStaffTreeId"
					,treeId, null,success);

		},


		/**初始化右键菜单**/
		selectListForLoginStaff:function(appName,domainId,resTypeId,menuSpeciality,envDomainId,success){
			return ngc.callDynDataServerFunctionApp(appName,domainId,"com.ztesoft.res.frame.database.operation.inf.PubFunctionMenuServiceIntf","selectListForLoginStaff"
					,resTypeId,menuSpeciality,null,success);

		},


		/**初始化右键菜单**/
		selectListNoPage:function(appName,domainId,jsonObject){
			return ngc.callDynDataServerFunctionApp(appName,domainId,"com.ztesoft.res.frame.user.manager.inf.WorkGroupServiceIntf","selectListNoPage"
					,jsonObject);

		},

		/**按照主键查询**/
		selectSafeStrategyList:function(domainId,jsonObject){
			return ngc.callDynDataServerFunction(domainId,"com.ztesoft.res.frame.user.password.inf.SafeStrategyServiceIntf","selectList"
					,jsonObject
				);

		},



		/**获取所有维度类型**/
		selectListDimensionType:function(appName,domainId){
			return ngc.callDynDataServerFunctionApp(appName,domainId,"com.ztesoft.res.frame.user.authority.inf.RoleDistributionQueryIntf","selectListDimensionType"
				);

		},
		
		
		/**查询TypeId**/
		getEntityType:function(domainId,success){
			return ngc.callDynDataServerFunction(domainId,"com.ztesoft.res.frame.user.manager.inf.StaffBaseServiceIntf","getEntityType"
					,success
					);

		},
		
		
	}
});
