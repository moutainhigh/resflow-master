/**
 * strategy
 */
define(["text!module/frameComponent/pageconfig/templates/DynamicPage.html",
    "module/UnicomLocalNet/resmaster/portal/bussApply/action/userGroupManageAction",
    'module/frameComponent/pageconfig/tools/CreateHtmlTemp',
    'i18n!module/UnicomLocalNet/resmaster/portal/bussApply/i18n/userManage.i18n',
    "css!module/frameComponent/pageconfig/style/default"
], function(mainTemplate,userGroupManageAction,CreateHtmlTemp,userManage) {

    var treeView = ngc.View.extend({
        operationflag :null,
        el:false,
        template : ngc.compile(mainTemplate),
        page :null,
        domainId :null,
        render : function() {
            this.initPlateData = {
                right_of_condition: false,
                left_of_center: false,
                right_of_center: false};

            this.toolsMenuData = {};
            var _buttons = new Array({id: "query", name: userManage.QUERY, index: null, icon: "fa fa-search fa-lg", functions: this.queryManage, show: true, displayPosition: "left_of_center"},
                /*{id: "queryReset", name: userManage.RESET, index: null, icon: "fa fa-refresh fa-lg", functions: this.result, show: true, displayPosition: "left_of_center"},
                {id: "addAuthManage", name: userManage.ADD, index: null, icon: "fa fa-plus fa-lg", functions: this.addAuthManage, show: true, displayPosition: "left_of_center"},
                {id: "editAuthManage", name: userManage.MODIFY, index: null, icon: "fa fa-edit fa-lg", functions: this.editAuthManage, show: true, displayPosition: "left_of_center"},
                {id: "deltAuthManage", name: userManage.DELETE, index: null, icon: "fa fa-close fa-lg", functions: this.delAuthManage, show: true, displayPosition: "left_of_center"},
                {id: "distributeUser", name: userManage.ASSIGNMENT_USER, index: null, icon: "fa fa-plus fa-lg", functions: this.distributeUser, show: true, displayPosition: "left_of_center"},
                {id: "userAuthDistribute", name: userManage.ASSIGNING_ROLE, index: null, icon: "fa-pencil-square-o fa-lg", functions: this.userAuthDistribute, show: true, displayPosition: "left_of_center"},
                */
                {id: "prodRoleApply", name: userManage.PROD_ROLE_APPLY+"到岗位", index: null, icon: "fa fa-edit fa-lg", functions: this.prodRoleApply, show: true, displayPosition: "left_of_center"}
            );
            CreateHtmlTemp._initButton(this, _buttons);
            this.initPlateData["title"] = this.options.title || userManage.USER_GROUP;
            this.$el.append(this.template(this.initPlateData));
            this.height = this.options.height ? this.options.height : 500;
            this.width = this.options.width ? this.options.width : 1000;
            CreateHtmlTemp._initCreateButton(this);
            this._initGrid();
        },

        //设置查询条件from的高度
        _setFormHeight: function(){
            var head = parseInt(this.$("#head_content").css('height'));
            var rowForm = parseInt(this.$("#row_form_content").css('height'));
            if(!rowForm){
                rowForm = parseInt(this.$("#form_content").css('height'));
            }
            var tool = parseInt(this.$("#left_center_tool_content").css('height'));
            var gridHeight = this.height - head - rowForm - tool - 3;
            this.getView("#grid_content").setGridHeight(gridHeight);
            this.getView("#grid_content").setGridWidth(1700);
        },

        _initGrid:function(){
            var that = this;
            var opt = {
                height: 550,
                sortable:true,
                multiselect:true,
                datatype: "json",
                colModel : [{
                    name: 'workGroupId',
                    index: 'workGroupId',
                    sorttype: "int",
                    label: userManage.USER_GROUP_ID,
                    key: true

                },
                    {
                        name: 'name',
                        index: 'name',
                        label: userManage.USER_GROUP_NAME,
                    },
                    {
                        name: 'regionName',
                        label: userManage.BELONG_REGION,
                        index: 'regionName',

                    }
                ],
                rowNum: 20,
                recordtext:userManage.COMMON_BAR,
                pgtext : userManage.PAGE_ZERO,
                rowList: [20, 30, 40, 50, 80],
                pager: true,
                curPageSort:true,
                pageData: function(page, rowNum, sortname, sortorder) {
                    _.delay(function () {
                        that.getPerData(page, rowNum, sortname, sortorder);
                    }, 100);
                    return false;
                },

            };

            var that = this;
            this.requireView({
                selector:"#grid_content",
                url:"module/component/views/GridView",
                viewOption :{config:{grid:opt}}
            }).then(function(){

            });

        },



        _initSearchForm:function(){
            var that = this;
            var _config = {elements:[
                    {id:'name',type:'text',label:userManage.GROUP_NAME}
                ],column:3,notSuit:true};
            this.requireView({
                selector:"#form_content",
                url:"module/component/views/FormView",
                viewOption :{config:_config}
            }).then(function(){
                that._setFormHeight();
            });

        },





        //数据初始化,数据重新加载,
        getPerData: function(page, rowNum, sortname, sortorder) {
            var grid = this.getView("#grid_content");
            var from = this.getView("#form_content");
            var queryData = from.getValues();
            grid.progress(userManage.DATA_LOADING);
            nowPage = parseInt(page);
            rowNum = (rowNum == undefined) ? 20 : rowNum;
            var param = {
                page:{pageNum:nowPage,pageSize:rowNum},
                entity:queryData
            };
            if(this.options.config && this.options.config.regionId){
                param.entity.regionId = this.options.config.regionId;
            }
            userGroupManageAction.selectList(this.options.appName,param,function(data){
                grid.progress();
                if(data && data.data){
                    for(var i = 0;i<data.data.length;i++){
                        if(data.data[i].expDate){
                            data.data[i].expDate = this.getYear(data.data[i].expDate);
                        }
                    }
                    gridResult = {
                        "rows": data.data,
                        "page": data.startIndex,
                        "records": data.total,
                        "id": "staffId"
                    };
                    grid.reloadData(gridResult);
                }else{
                    grid.reloadData(null);

                }

            }.bind(this));



        },


        /**截取时间**/
        getYear: function(str){
            if(str){
                str = str.substr(0, 10);
            }
            return str;
        },

        afterRender: function () {
            this._initSearchForm();
        },



        queryManage:function(){
            this.getPerData(1);
        },



        /*addAuthManage:function(){
            var that = this;
            var regionId ;
            if(this.options.config && this.options.config.regionId){
                regionId = this.options.config.regionId;
            }
            ngc.openView({
                width:'500',
                url: "module/frameComponent/user/userGroupManage/views/userGroupManageOpenView",
                viewOption :{config:{type: "add",
                        regionId:regionId},appName:this.options.appName},
                callback: function(popup, view) {

                    view.on('optionValueUserGroup', function(data) {//监听popView的返回值
                        that.getPerData(1);
                    }.bind(this))
                }.bind(this)
            }).then(function(view) {

            });
        },*/



        /*editAuthManage:function(){
            var that = this;
            var _grid = that.getView("#grid_content").getSelection();
            if(_grid.workGroupId == null){
                ngc.error(userManage.SELECT_DATA_MODIFY);
                return;
            }
            var regionId ;
            if(this.options.config && this.options.config.regionId){
                regionId = this.options.config.regionId;
            }
            ngc.openView({
                width:'500',
                url: "module/frameComponent/user/userGroupManage/views/userGroupManageOpenView",
                viewOption :{config:{resultData:_grid.workGroupId,
                        regionId:regionId},appName : this.options.appName},
                callback: function(popup, view) {
                    view.on('optionValueUserGroup', function(data) {//监听popView的返回值
                        that.getPerData(1);
                    }.bind(this))
                }.bind(this)
            }).then(function(view) {

            });
        },*/


        /*delAuthManage:function(){
            var that = this;
            var _gridView = that.getView("#grid_content");
            var _grid = _gridView.getSelection();
            if(_grid.workGroupId == null){
                ngc.error(userManage.SELECT_DATA_MODIFY);
                return;
            }
            ngc.confirm(userManage.CONFIRM_DELETE+_grid.name+'?').result.then(function() {
                var jsonObject = {id:_grid.workGroupId};
                var resultState = userGroupManageAction.deleteUserInfo(that.options.appName,jsonObject);
                var flag = resultState.responseJSON.resultStat;
                if(flag == "SUCCESS"){
                    _gridView.delRow(_grid.workGroupId);
                    ngc.success(userManage.DELETE_SUCCEEDED);
                }else{
                    ngc.error(userManage.DELETE_FAILED);
                }
            });
        },*/

        /*distributeUser:function(){
            var that = this;
            var _grid = that.getView("#grid_content").getSelection();
            if(_grid.workGroupId == null){
                ngc.error(userManage.SELECT_DATA_ASSIGNMENT);
                return;
            }
            ngc.openView({
                width:'55%',
                url: "module/frameComponent/user/userGroupManage/views/userGroupOperation",
                viewOption :{config:{resultData:_grid},appName : this.options.appName},
                callback: function(popup, view) {
                    view.on('optionValue', function(data) {//监听popView的返回值
                        // that.getPerData(1);
                    }.bind(this))
                }.bind(this)
            }).then(function(view) {

            });
        },*/


        /*userAuthDistribute:function(){
            var that = this;
            var _grid = that.getView("#grid_content").getSelection();
            if(_grid.workGroupId == null){
                ngc.error(userManage.SELECT_DATA_ASSIGNMENT);
                return;
            }
            var regionId ;
            if(this.options.config && this.options.config.regionId){
                regionId = this.options.config.regionId;
            }
            var resultData = {selId:_grid.workGroupId,typeId:72};
            ngc.openView({
                width:'90%',
                url: "module/frameComponent/user/userManage/views/departmentOpenView",
                viewOption :{config:{resultData:resultData,regionId:regionId},appName:this.options.appName},
                callback: function(popup, view) {
                    view.on('optionValue', function(data) {//监听popView的返回值
                        that.getPerData(that.page);
                    }.bind(this))
                }.bind(this)
            }).then(function(view) {

            });

        },*/

        //产品权限申请 add by wangsen 2020年10月9日 14:50:18
        prodRoleApply:function () {
            var that = this;
            var _grid = that.getView("#grid_content").getSelection();
            if(_grid.workGroupId == null){
                ngc.error(userManage.SELECT_DATA_ASSIGNMENT);
                return;
            }
            var regionId ;
            if(this.options.config && this.options.config.regionId){
                regionId = this.options.config.regionId;
            }
            var resultData = {selId:_grid.workGroupId,typeId:72};
            ngc.openView({
                width:'90%',
                url: "module/UnicomLocalNet/resmaster/portal/bussApply/views/groupApply",
                viewOption :{config:{resultData:resultData,regionId:regionId},appName:this.options.appName},
                callback: function(popup, view) {
                    view.on('optionValue', function(data) {//监听popView的返回值
                        that.getPerData(that.page);
                    }.bind(this))
                }.bind(this)
            }).then(function(view) {

            });
        },

        /**检索用户**/
        queryGroupManage:function(){
            this.getPerData(1);
        },

        result:function(){
            this.$("#name").val("");
            this.getPerData(1);
        }

    });
    return treeView;
});
