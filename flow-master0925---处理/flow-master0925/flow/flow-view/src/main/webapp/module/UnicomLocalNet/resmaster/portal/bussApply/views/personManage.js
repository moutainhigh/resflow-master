
define(['module/component/views/TreeView',
    "module/UnicomLocalNet/resmaster/portal/bussApply/action/userManageAction",
    "module/UnicomLocalNet/resmaster/portal/bussApply/action/userGroupManageAction",
    "text!module/frameComponent/pageconfig/templates/DynamicPage.html",
    'module/frameComponent/pageconfig/tools/CreateHtmlTemp',
    'i18n!module/UnicomLocalNet/resmaster/portal/bussApply/i18n/userManage.i18n',
    "css!module/frameComponent/pageconfig/style/default"
], function(TreeView,userManageAction,userGroupManageAction,mainTemplate,CreateHtmlTemp,userManage) {

    var treeView = ngc.View.extend({
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
                //{id: "localProdApply", name: userManage.PROD_APPLY_LOCAL, index: null, icon: "fa fa-edit fa-lg", functions: this.localProdApply, show: true, displayPosition: "left_of_center"},
                {id: "enableStateAuthManage", name: userManage.BTN_LABLE_IN_OR_ACTIVE, index: null, icon: "glyphicon glyphicon-ban-circle", functions: this.enableStateAuthManage, show: true, displayPosition: "left_of_center"},
                {id: "userAuthDistribute", name: userManage.ROLE_ASSIGNING, index: null, icon: "fa-pencil-square-o fa-lg", functions: this.userAuthDistribute, show: true, displayPosition: "left_of_center"},
                {id: "unlock", name: userManage.UNLOCK, index: null, icon: "fa-pencil-square-o fa-lg", functions: this.unlock, show: true, displayPosition: "left_of_center"},
                {id: "exportData", name: userManage.EXPORT_DATA, index: null, icon: "glyphicon glyphicon-download-alt", functions: this._exportPageClick, show: true, displayPosition: "left_of_center"}
                */
                {id: "prodRoleApply", name: userManage.PROD_ROLE_APPLY+"到个人", index: null, icon: "fa fa-edit fa-lg", functions: this.prodRoleApply, show: true, displayPosition: "left_of_center"}
                /*{id: "exportAllData", name: userManage.EXPORT_ALL, index: null, icon: "glyphicon glyphicon-download-alt", functions: this.exportAllData, show: true, displayPosition: "left_of_center"}*/


            );
            CreateHtmlTemp._initButton(this, _buttons);
            this.initPlateData["title"] = this.options.title || userManage.USER_MANAGEMENT;
            this.$el.append(this.template(this.initPlateData));
            this.height = this.options.height ? this.options.height : 500;
            CreateHtmlTemp._initCreateButton(this);
            this._initGrid();
            ngc.on('refreshTreeNode', function(node) {
                if(node.treeFlag == "treeAdd"){
                    /**取上个节点更新**/
                    var parentNode = node.getParentNode();
                    this._onExpand(this.getView("#tree_content"),parentNode);
                }else if(node.treeFlag == "delete"){
                    /**删除当前节点**/
                    this.getView("#tree_content").removeNode(node);
                }else{
                    this.getView("#tree_content").updateNode(node);
                }

            }.bind(this));

        },

        afterRender: function () {
            //初始化域Id
            this.initDomainId();

            //检索
            this._initSearchForm();
            //资源树
            // this._initTree();
            // this.$("#top_node_"+this.newEle[i].id).find("label").attr('title',this.newEle[i].title);
            // this.$("#top_node_"+this.newEle[i].id).find("label").css('white-space','nowrap');
            // this.$("#top_node_"+this.newEle[i].id).find("label").css('overflow','hidden');
            // this.$("#top_node_"+this.newEle[i].id).find("label").css('text-overflow','ellipsis');
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
                height: 480,
                sortable:true,
                multiselect:true,
                datatype: "json",
                colModel : [{
                    name: 'staffId',
                    index: 'id',
                    label: userManage.USER_ID,
                    sorttype: "int",
                    key: true

                },
                    {
                        name: 'loginName',
                        index: 'loginName',
                        label: userManage.LOGIN_NAME
                    },
                    {
                        name: 'userName',
                        label: userManage.USER_NAME,
                        index: 'userName'
                    },
                    {
                        name: 'siteName',
                        label: userManage.ORGANIZATIONAL_STRUCTURE,
                        index: 'siteName'
                    },
                    {
                        name: 'phoneNo',
                        label: userManage.PHONE,
                        index: 'phoneNo'
                    },{
                        name: 'state',
                        label : userManage.USER_ACCOUNT_STATUS,
                        formatter: function(cellval){
                            if("A" == cellval){
                                return userManage.USER_ACCOUNT_STATUS_ACTIVE;
                            }else if("X" == cellval){
                                return userManage.USER_ACCOUNT_STATUS_INACTIVE;
                            }else{
                                return "";
                            }

                        }
                    },{
                        name: 'expDate',
                        type : 'date',
                        label : userManage.PASSWORD_VALIDITY_PERIOD,
                        format :'yyyy-mm-dd',
                        hidden: true
                    }],
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
            var _config = {column:3,editable:true,elements:[
                    {id:'loginName',type:'text',label:userManage.LOGIN_NAME},
                    {id:'userName',type:'text',label:userManage.PERSON_NAME},
                    {id:'phoneNo',type:'text',label:userManage.PHONE},
                    {id:'state',type:'select',label:userManage.USER_ACCOUNT_STATUS, options:[{NAME:userManage.USER_ACCOUNT_STATUS_ACTIVE,VALUE:'A'},{NAME:userManage.USER_ACCOUNT_STATUS_INACTIVE,VALUE:'X'}],placeholder:'--' + userManage.TIP_MSG + " " + userManage.USER_ACCOUNT_STATUS + '--',defaultValue:{value :'A'},editable:true}
                ]};
            this.requireView({
                selector:"#form_content",
                url:"module/component/views/FormView",
                viewOption :{config:_config}
            }).then(function(){
                that.$("#phoneNo").attr("maxlength","11");
                that.$("#phoneNo").attr("onkeyup","this.value=this.value.replace(/[^0-9]/g,'')");
                that.$("#phoneNo").attr("onafterpaste","this.value=this.value.replace(/[^0-9]/g,'')");
                that._setFormHeight();
                that.$("#top_node_userName").find("label").attr('title','person name');
                that.$("#top_node_userName").find("label").css('white-space','nowrap');
                that.$("#top_node_userName").find("label").css('overflow','hidden');
                that.$("#top_node_userName").find("label").css('text-overflow','ellipsis');
            });
        },

        //数据初始化,数据重新加载,
        getPerData: function(page, rowNum, sortname, sortorder) {

            var grid = this.getView("#grid_content"),that = this;
            grid.progress(userManage.USER_DATA_LOADING);
            var from = this.getView("#form_content");
            var queryData = from.getValues();
            nowPage = parseInt(page);
            this.page = nowPage
            rowNum = (rowNum == undefined) ? 20 : rowNum;
            var param = {
                page:{pageNum:nowPage,pageSize:rowNum},
                entity:queryData
            };

            userManageAction.selectList(this.options.appName,this.domainId,param,function(data){
                if(data && data.data){
                    for(var i = 0;i<data.data.length;i++){
                        if(data.data[i].expDate){
                            data.data[i].expDate = that.getYear(data.data[i].expDate);
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
                grid.progress();
            });

        },

        _initTree: function(){
            var _that = this;
            this.setView("#tree_content", new TreeView({config:{
                    tree:{
                        idKey : "id",
                        pIdKey : "pId",
                        view: {
                            showLine: true
                        },
                        data : {
                            keep : {
                                parent : true
                            },
                            simpleData : {
                                enable : true
                            }
                        }
                    },
                    callback:{
                        onExpand : function(e, treeNode, clickFlag) {
                            _that._onExpand(this, treeNode);
                        }
                    }
                }}));
            this.renderViews("#tree_content");
            this.getView("#tree_content").on("viewRenderAfter", function() {
                /**初始化树**/
                userManageAction.getStaffTree(_that.options.appName,_that.domainId,"190096","270","200","", function(data){
                    if(data.length>0){
                        for(var i = 0;i<data.length;i++){
                            if (data[i].isChild == 1){
                                data[i].isParent = true;
                            }
                        }
                    }
                    this.reloadData(data);


                }.bind(this));
                /**初始化右键菜单**/
                var jsonObj = {
                    id:190096,entity:null
                };
                userManageAction.selectListForLoginStaffByStaffTreeId(_that.options.appName,_that.domainId,'190096',function(data){
                    if(data.length>0){
                        for(var i = 0;i<data.length;i++){
                            data[i].id = data[i].menuId;
                            data[i].label = data[i].menuName;
                            data[i].icon = data[i].menuImage;
                        }
                    }

                    this.attachMenu(data,function(tree){
                        var nodes = tree.getSelectedNodes();
                        if(nodes==0)
                            return false;
                        this.hideMenu();
                        var node = nodes[0];
                        _that._assertMenuDisplay(node,this);
                        return true;
                    });
                }.bind(this));

                //菜单按钮的ID，表格操作时候右键点击的行ID，menu对象，事件对象，一般只使用前两个
                this.on("onClickMenu",function(menuId,selNode,menu,e){
                    var node = selNode[0];
                    switch(menuId){
                        case '000117160010000000058461':
                            ngc.openView({
                                width:'1400',
                                url: "module/frameComponent/user/userGroupManage/views/userGroupManageOpenView",
                                viewOption: {
                                    config:{node:node,type:"treeadd"},
                                    appName : _that.options.appName
                                },
                                callback: function(popup, view) {

                                }.bind(this)
                            }).then(function(view) {

                            });
                            break;

                        case '000117160010000000057821':
                            ngc.openView({
                                width:'1400',
                                url: "module/frameComponent/user/userGroupManage/views/userGroupManageOpenView",
                                viewOption :{config:{resultData:node.id,type:"treeedit",node:node},appName : _that.options.appName},
                                callback: function(popup, view) {

                                }.bind(this)
                            }).then(function(view) {

                            });
                            break;

                        case '000117160010000000057823':
                            var id=node.id;
                            var name=node.name;

                            ngc.confirm(userManage.CONFIRM_DELETE+name+'?').result.then(function() {
                                var jsonObject = {id:id};
                                var resultState = userGroupManageAction.deleteUserInfo(_that.options.appName,jsonObject);
                                var flag = resultState.responseJSON.resultStat;
                                if(flag == "SUCCESS"){
                                    node.treeFlag = 'delete';
                                    ngc.trigger("refreshTreeNode",node);
                                    ngc.success(userManage.DELETE_SUCCEEDED);
                                }else{
                                    ngc.error(userManage.DELETE_FAILED);
                                }
                            });
                            break;

                        case '000117160010000000057824':
                            var resultData = {selId:node.id,typeId:72};
                            ngc.openView({
                                width:'1400',
                                url: "module/frameComponent/user/staffManage/views/departmentOpenView",
                                viewOption :{config:{resultData:resultData,domainId:_that.domainId},appName : _that.options.appName},
                                callback: function(popup, view) {
//				 				   view.on('optionValue', function(data) {//监听popView的返回值
//				 					  that.getPerData(that.page);
//				        		   }.bind(this))
                                }.bind(this)
                            }).then(function(view) {

                            });
                            break;

                        case '000117160010000000057820':
                            ngc.openView({
                                width:'1400',
                                url: "module/frameComponent/user/userManage/views/userManageOpenView",
                                viewOption: {
                                    config:{node : node,type:"treeadd",domainId:_that.domainId},
                                    appName : _that.options.appName
                                },
                                callback: function(popup, view) {
                                }.bind(this)
                            }).then(function(view) {

                            });
                            break;
                        case '000117160010000000047801':
                            var jsonObject = {id:node.id};
                            var resultState = userManageAction.queryUserInfo(this.options.appName,this.domainId,jsonObject);
                            var resultData = resultState.responseJSON.data;
                            ngc.openView({
                                width:'1400',
                                url: "module/frameComponent/user/userManage/views/userManageOpenView",
                                viewOption :{config:{resultData:resultData,node:node,type:"treeedit",domainId:this.domainId},appName : _that.options.appName},
                                callback: function(popup, view) {
                                    view.on('optionValue', function(data) {//监听popView的返回值
                                        that.getPerData(that.page);
                                    }.bind(this))
                                }.bind(this)
                            }).then(function(view) {

                            });
                            break;

                        case '000117160010000000047802':
                            var id=node.id;
                            var name=node.name;
                            ngc.confirm(userManage.CONFIRM_DELETE+name+'?').result.then(function() {
                                var jsonObject = {id:id};
                                var resultState = userManageAction.deleteUserInfo(this.options.appName,this.domainId,jsonObject);
                                var flag = resultState.responseJSON.resultStat;
                                if(flag == "SUCCESS"){
                                    node.treeFlag = 'delete';
                                    ngc.trigger("refreshTreeNode",node);
                                    ngc.success(userManage.DELETE_SUCCEEDED);
                                }else{
                                    ngc.error(userManage.DELETE_FAILED);
                                }
                            });
                            break;

                        case '000117160010000000047803':
                            var resultData = {selId:node.id,typeId:72};
                            ngc.openView({
                                width:'1400',
                                url: "module/frameComponent/user/userManage/views/departmentOpenView",
                                viewOption :{config:{resultData:resultData,domainId:this.domainId},appName : _that.options.appName},
                                callback: function(popup, view) {

                                }.bind(this)
                            }).then(function(view) {

                            });
                            break;

                        case '000117160010000000057825':
                            var workGroupId = [{"workGroupId":workGroupId},{"name": node.name}];
                            var workGroupId = {};
                            workGroupId.workGroupId = node.id;
                            workGroupId.name =  node.name;
                            ngc.openView({
                                width:'90%',
                                url: "module/frameComponent/user/userGroupManage/views/userGroupOperation",
                                viewOption :{config:{resultData:workGroupId,type:"treeadd",node:node},appName : _that.options.appName},
                                callback: function(popup, view) {
                                    view.on('optionValue', function(data) {//监听popView的返回值
                                        // that.getPerData(1);
                                    }.bind(this))
                                }.bind(this)
                            }).then(function(view) {
                                });
                            break;
                    }
                });
            });
        },


        _onExpand: function(_tree, _treeNode){
            var node = _tree.getNodeByTId(_treeNode.tId);
            var treeConten = this.getView("#tree_content");
            treeConten.removeChildNodes(_treeNode);
            //var node = treeConten.getNodeByTId(_treeNode.tId);
            //if(node.children == undefined){
            userManageAction.getStaffTree(this.options.appName,this.domainId,"190096","270" ,_treeNode.dimensionTypeId, _treeNode.id, function(data){
                if(data.length>0){
                    for(var i = 0;i<data.length;i++){
                        if (data[i].isChild == 1){
                            data[i].isParent = true;
                        }
                    }
                }
                _treeNode = _tree.addNodes(node, data)
            });
        },

        /**
         * 判断右键菜单展示
         */
        _assertMenuDisplay:function(node, menu){
            switch(node.dimensionTypeId){
                /**管理区域**/
                case "200":
                    menu.showMenu("000117160010000000058461");
                    break;

                /**用户组**/
                case "72":
                    menu.showMenu("000117160010000000057823");
                    menu.showMenu("000117160010000000057821");
                    menu.showMenu("000117160010000000057820");
                    menu.showMenu("000117160010000000057824");
                    menu.showMenu("000117160010000000057825");

                    break;

                /**用户**/
                case "151":
                    menu.showMenu("000117160010000000047801");
                    menu.showMenu("000117160010000000047802");
                    menu.showMenu("000117160010000000047803");
                    menu.showMenu("000117160010000000047804");
                    break;
            }
        },

        /**删除安全策略**/
        delStrategy:function (selId, data, that, form, grid){
            ngc.confirm(userManage.CONFIRM_DELETE).result.then(function() {
                var jsonObject = {id:selId};
                var resultState = strategyAction.deleteByPrimaryKey(jsonObject);
                var flag = resultState.responseJSON.resultStat;
                if(flag == "SUCCESS"){
                    ngc.success(userManage.DELETE_FAILED);
                    grid.delRow(selId);
                }
            });
        },

        /**截取时间**/
        getYear: function(str){
            if(str){
                str = str.substr(0, 10);
            }
            return str;

        },

        //补0操作
        getzf:function(num){
            if(parseInt(num) < 10){
                num = '0'+num;
            }
            return num;
        },

        result:function(){
            this.$("#loginName").val("");
            this.$("#userName").val("");
            this.$("#phoneNo").val("");
            this.getView("#form_content").initValue("state",{value:"A",text:userManage.USER_ACCOUNT_STATUS_ACTIVE});
            this.getPerData(1);
        },

        queryManage:function(){
            this.getPerData(1);

        },

        addAuthManage:function(){
            var that = this;
            ngc.openView({
                width:'1100',
                url: "module/frameComponent/user/staffManage/views/userManageOpenView",
                viewOption :{config:{type: "add",deptIdParam:this.options.deptIdParam,deptNameParam:this.options.deptNameParam},
                    appName : this.options.appName
                },
                callback: function(popup, view) {
                    view.on('optionValue', function(data) {//监听popView的返回值
                        var pageS = 1;
                        if(that.page){
                            pageS = that.page;
                        }
                        that.getPerData(pageS);
                    }.bind(this))
                }.bind(this)
            }).then(function(view) {

            });
        },

        editAuthManage:function(){
            var that  = this;
            var rowData = this.getView("#grid_content").getSelection();
            if(rowData.staffId == null){
                ngc.error(userManage.SELECT_DATA_OPERATION);
                return;
            }
            var jsonObject = {id:rowData.staffId};
            var resultState = userManageAction.selectExByPrimaryKey(that.options.appName,that.domainId,rowData.staffId);
            var resultData = resultState.responseJSON.data;
            ngc.openView({
                width:'1100',
                url: "module/frameComponent/user/staffManage/views/userManageOpenView",
                viewOption :{config:{resultData:resultData,hiddenPws:true,domainId:that.domainId}},
                callback: function(popup, view) {
                    view.on('optionValue', function(data) {//监听popView的返回值
                        that.getPerData(that.page);
                    }.bind(this))
                }.bind(this)
            }).then(function(view) {

            });
        },

        delAuthManage:function(){
            var that  = this;
            var _grid = that.getView("#grid_content");
            var rowData =_grid.getSelection();
            if(rowData.staffId == null){
                ngc.error(userManage.SELECT_DATA_DELETE);
                return;
            }
            ngc.confirm(userManage.CONFIRM_DELETE+rowData.userName+'?').result.then(function() {
                var jsonObject = {id:rowData.staffId};
                var resultState = userManageAction.deleteUserInfo(that.options.appName,that.domainId,jsonObject);
                var flag = resultState.responseJSON.resultStat;
                if(flag == "SUCCESS"){
                    _grid.delRow(rowData.staffId);
                    ngc.success(userManage.DELETE_SUCCEEDED);
                }
            });
        },

        enableStateAuthManage: function(){
            var that  = this;
            var _grid = that.getView("#grid_content");
            var rowData =_grid.getCheckRows();
            if(rowData.length <=0){
                ngc.warn(userManage.SELECT_DATA_ACTIVE);
                return;
            }

            var stateData = [];
            $.each(rowData,function(i,row){
                if(row.staffId != null && row.staffId != undefined){
                    stateData.push({
                        staffId:row.staffId,
                        state: row.state
                    });
                }
            });

            ngc.confirm(userManage.CONFIRM_IN_OR_ACTIVE +' ?').result.then(function() {
                var jsonObject = {data:stateData};
                var resultState = userManageAction.activeOrInactiveUser(that.options.appName,that.domainId,jsonObject);
                var flag = resultState.responseJSON.resultStat;
                that.getPerData(1);
                if(flag == "SUCCESS"){
                    ngc.success(userManage.ACTIVE_OR_INACTIVE_MSG_SUC);
                }else{
                    ngc.error(userManage.ACTIVE_OR_INACTIVE_MSG_FAIL);
                }
            });

        },

        userAuthDistribute:function(){
            //查询用户登录的信息
            var loginUserResult = ngc.queryStaffInfo();
            var loginUser = loginUserResult.responseJSON.data;
            var loginUserTitle;
            var loginUserStaffId;
            if(loginUser){
                loginUserTitle = loginUser.title;
                loginUserStaffId = loginUser.staffId;
            }

            var that  = this;
            var rowData = this.getView("#grid_content").getSelection();
            if(rowData.staffId == null){
                ngc.error(userManage.SELECT_DATA_OPERATION);
                return;
            }
            //二级管理员无法给自己分配权限
            if(loginUserTitle == 30 && rowData.title == 30 && rowData.staffId == loginUserStaffId){
                ngc.error(userManage.ADMIN_CANNOT_UPDATE_SELFROLE);
                return;
            }

            var resultData = {selId:rowData.staffId,typeId:151};
            ngc.openView({
                width:'90%',
                title:userManage.ROLE_ASSIGNING,
                url: "module/frameComponent/user/userManage/views/departmentOpenView",
                viewOption :{config:{resultData:resultData,domainId:that.domainId},appName : this.options.appName},
                callback: function(popup, view) {
                    view.on('optionValue', function(data) {//监听popView的返回值
                        that.getPerData(that.page);
                    }.bind(this))
                }.bind(this)
            }).then(function(view) {

            });
        },

        unlock: function () {
            var that  = this;
            var _grid = that.getView("#grid_content");
            var rowData =_grid.getSelection();
            if(rowData.staffId == null){
                ngc.error(userManage.SELECT_DATA_OPERATION);
                return;
            }else if(rowData.staffState == 1){
                ngc.info(userManage.USER_UNLOCKED);
                return;
            }

            userManageAction.unlock(that.options.appName,that.domainId,rowData.staffId,function (data) {
                if (data){
                    if (data == true){
                        //  _grid.setCell(rowData.staffId, "stateDesc", userManage.EFFECTIVE);
                        ngc.success(userManage.UNLOCK_SUCCESS);
                        return;
                    }
                }
                ngc.error(userManage.UNLOCK_FAILURE);
            });
        },

        _exportPageClick: function(){
            var grid = this.getView("#grid_content");
            var _gridSelect = grid.getCheckRows();
            //没有勾选数据  就获取当页数据
            if(_gridSelect.length <=0){
                _gridSelect = grid.getRowData(null,false);
            }
            var _labelArray = new Array();
            var _GridDataArray = new Array();
            var lable = grid.options.config.grid.colModel;
            for(var i=0; i<lable.length; i++){
                _labelArray.push(lable[i]["label"]);
            }
            for(var i=0; i<_gridSelect.length; i++){
                var cellArray = new Array();
                cellArray.push(_gridSelect[i]["staffId"] == null || _gridSelect[i]["staffId"] == "&nbsp;"  ? "" : _gridSelect[i]["staffId"]);
                cellArray.push(_gridSelect[i]["loginName"] == null || _gridSelect[i]["loginName"] == "&nbsp;" ? "" : _gridSelect[i]["loginName"]);
                cellArray.push(_gridSelect[i]["userName"] == null || _gridSelect[i]["userName"] == "&nbsp;" ? "" : _gridSelect[i]["userName"]);
                cellArray.push(_gridSelect[i]["siteName"] == null || _gridSelect[i]["siteName"] == "&nbsp;" ? "" : _gridSelect[i]["siteName"]);
                cellArray.push(_gridSelect[i]["phoneNo"] == null || _gridSelect[i]["phoneNo"] == "&nbsp;" ? "" : _gridSelect[i]["phoneNo"]);
                cellArray.push(_gridSelect[i]["state"] == null || _gridSelect[i]["state"] == "&nbsp;" ? "" : (_gridSelect[i]["state"] == 'A' ? userManage.USER_ACCOUNT_STATUS_ACTIVE : userManage.USER_ACCOUNT_STATUS_INACTIVE));
                // cellArray.push(_gridSelect[i]["expDate"] == null || _gridSelect[i]["expDate"] == "&nbsp;" ? "" : _gridSelect[i]["expDate"]);
                // cellArray.push(_gridSelect[i]["stateDesc"] == null || _gridSelect[i]["stateDesc"] == "&nbsp;" ? "" : _gridSelect[i]["stateDesc"]);
                _GridDataArray.push(cellArray);
            }
            this.exportData(_GridDataArray,_labelArray);
        },

        exportData:function(_gridSelect,_labelArray){
            var _url = "dynamicQuery.spr?method=exportDynamicQueryData&queryId="+1;
            var _params = { labelParams: _labelArray, dataParams: _gridSelect, queryDesc: userManage.USER_MANAGEMENT}
            ngc.downLoad(_url, _params);
        },

        exportAllData: function () {
            var _url = "userManager.spr?method=exportAllUser";
            var from = this.getView("#form_content");
            var queryData = from.getValues();
            var _params = {
                page:{pageNum:1,pageSize:500000},
                entity:queryData
            };
            ngc.downLoad(_url, _params);
        },

        initDomainId:function(){
            if(this.options.config == undefined || this.options.config.domainId == null || this.options.config.domainId == "" && this.options.config.domainId == undefined){
                this.domainId = "";
            }else{
                this.domainId =  this.options.config.domainId;
            }
        },

        //产品权限申请 add by wangsen 2020年10月9日 14:50:18
        prodRoleApply:function () {
            //查询用户登录的信息
            var loginUserResult = ngc.queryStaffInfo();
            var loginUser = loginUserResult.responseJSON.data;
            var loginUserTitle;
            var loginUserStaffId;
            if(loginUser){
                loginUserTitle = loginUser.title;
                loginUserStaffId = loginUser.staffId;
            }

            var that  = this;
            var rowData = this.getView("#grid_content").getSelection();
            if(rowData.staffId == null){
                ngc.error(userManage.SELECT_DATA_OPERATION);
                return;
            }
            //二级管理员无法给自己分配权限
            if(loginUserTitle == 30 && rowData.title == 30 && rowData.staffId == loginUserStaffId){
                ngc.error(userManage.ADMIN_CANNOT_UPDATE_SELFROLE);
                return;
            }

            var resultData = {selId:rowData.staffId,typeId:151};
            ngc.openView({
                width:'90%',
                title:userManage.PROD_ROLE_APPLY,
                url: "module/UnicomLocalNet/resmaster/portal/bussApply/views/personApply",
                viewOption :{config:{resultData:resultData,domainId:that.domainId},appName : this.options.appName},
                callback: function(popup, view) {
                    view.on('optionValue', function(data) {//监听popView的返回值
                        that.getPerData(that.page);
                    }.bind(this))
                }.bind(this)
            }).then(function(view) {

            });
        },

    });

    return treeView;
});
