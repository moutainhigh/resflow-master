define(function() {
	return {
		/**查询用户组**/
		selectList:function(appName,jsonObject,success){
			return ngc.callServerFunctionApp(appName,"com.ztesoft.res.frame.user.manager.inf.WorkGroupServiceIntf","selectListEx"
					,jsonObject,success);

		},

        /**根据staffId获取用户所在部门信息**/
        queryUserDeparmentInfo :function(staffId){
            return ngc.callServerFunction("com.ztesoft.res.frame.user.manager.inf.StaffBaseServiceIntf","queryUserDeparmentInfo",staffId);
        },


        /**递归查询区域**/
        selectRegion:function(appName,jsonObject,success){
            return ngc.callServerFunctionApp(appName,"com.ztesoft.res.frame.user.manager.inf.SpcRegionServiceIntf","selectRegion"
                ,jsonObject
                ,success);

        },




        /**查询用户组 查全部**/
		selectListNoPage:function(jsonObject,success){
			return ngc.callServerFunction("com.ztesoft.res.frame.user.manager.inf.WorkGroupServiceIntf","selectListNoPage"
					,jsonObject
					,success);

		},	
		
		
		/**新增用户组**/
		saveWorkGroup:function(appName,jsonObject,success){
			return ngc.callServerFunctionApp(appName,"com.ztesoft.res.frame.user.manager.inf.WorkGroupServiceIntf","save"
					,jsonObject
					,success);

		},
		
		
		/**删除用户组**/
		deleteUserInfo:function(appName,jsonObject,success){
			return ngc.callServerFunctionApp(appName,"com.ztesoft.res.frame.user.manager.inf.WorkGroupServiceIntf","deleteByPrimaryKey"
					,jsonObject
					,success);

		},
		
		
		/**根据Id查询**/
		selectByPrimaryKey:function(appName,jsonObject){
			return ngc.callServerFunctionApp(appName,"com.ztesoft.res.frame.user.manager.inf.WorkGroupServiceIntf","selectByPrimaryKeyEx"
					,jsonObject
					);

		},
		
		
		/**查询区域**/
		selectRegionList:function(appName,jsonObject,success){
			return ngc.callServerFunctionApp(appName,"com.ztesoft.res.frame.user.manager.inf.SpcRegionServiceIntf","selectList"
					,jsonObject
					,success);

		},
		
		
		/**获取主键**/
		getPrimaryKey:function(appName,success){
			return ngc.callServerFunctionApp(appName,"com.ztesoft.res.frame.user.manager.inf.WorkGroupServiceIntf","getPrimaryKey"
					,success);

		},
		
		
		
		/**
		 * 查询在指定用户组的用户
		 * @param workGroupId
		 * @return
		 */
		selectListInWorkGroupId:function(appName,jsonObject,success){
			return ngc.callServerFunctionApp(appName,"com.ztesoft.res.frame.user.manager.inf.StaffBaseServiceIntf","selectListInWorkGroupId"
					,jsonObject
					,success);

		},
        /**
         * 查询在指定用户组的用户不分页
         * @param workGroupId
         * @return
         */
        selectListInWorkGroupIdNoPage:function(appName,jsonObject,success){
            return ngc.callServerFunctionApp(appName,"com.ztesoft.res.frame.user.manager.inf.StaffBaseServiceIntf","selectListInWorkGroupIdNoPage"
                ,jsonObject
                ,success);

        },

		/**
		 * 查询不在指定用户组的用户
		 * @param workGroupId
		 * @return
		 */
		selectListNotInWorkGroupId:function(appName,jsonObject,success){
			return ngc.callServerFunctionApp(appName,"com.ztesoft.res.frame.user.manager.inf.StaffBaseServiceIntf","selectListNotInWorkGroupId"
					,jsonObject
					,success);

		},
		
		
		/**
		 * 用户组和用户关系
		 * @param workGroupId
		 * @return
		 */
		saveUserGroup:function(appName,jsonObject,success){
			return ngc.callServerFunctionApp(appName,"com.ztesoft.res.frame.user.manager.inf.StaffWorkgrpServiceIntf","save"
					,jsonObject
					,success);

		},
	}
});
