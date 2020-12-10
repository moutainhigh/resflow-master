define(['module/component/views/FormView',
        'module/component/views/GridView',
        'module/component/views/TreeView',
        'module/UnicomLocalNet/resmaster/portal/bussApply/action/prodApplyAction',
        'i18n!module/UnicomLocalNet/resmaster/portal/bussApply/i18n/userManage.i18n']
    ,function (FormView, GridView,TreeView,prodApplyErganAction,userManage) {
        var templatehtml = '<div class="ui-dialog dialog-md" id="dialogMd" style="height: 540px;">'+
            '<div class="modal-header"><h4 class="modal-title">'+userManage.PROD_ROLE_APPLY+'</h4></div>' +
            '<div class="col-sm-10" style="width: 100%;">'+

            //待分配的产品
            '<div class="panel panel-default" style="width:100%">'+
            '<div>'+
            '<div id="tabs" class="ui-tabs">'+
            '<ul class="ui-tabs-nav">'+
            '<li id="preciseCond"><a>'+userManage.TWODRY_PROD_ASSIGNMENT+'</a></li>'+
            /*'<li id="complexCond"><a>'+userManage.ROLE_POINT_ASSIGNMENT+'</a></li>'+*/
            '</ul>'+
            '<div id="tabs-a" class="ui-tabs-panel">'+
            //已分配的产品
            '<div class="row">'+
            '<div class="col-md-3 ">'+
            '<div class="panel panel-primary ">'+
            '<div class="panel-heading">'+
            ' <label>'+userManage.PROD_AVAILABLE_ASSIGNMENT+'</label>'+
            '</div>'+
            '<div class="panel-body" id="nonexixtRoleGroupGrid"></div>'+
            '</div>'+
            '</div>'+



            /*        '<div class="col-md-1" style="width: 50px;padding-top: 100px;">' +
                       '<div class="btn-separate-group-vertical">'+
                           '<button type="button" class="btn btn-default menuLeft">'+
                              ' <span class="glyphicon glyphicon-menu-left"></span>'+
                           '</button>'+
                          '<button type="button" class="btn btn-default menuRight">'+
                               '<span class="glyphicon glyphicon-menu-right"></span>'+
                           '</button>'+

                           '<button type="button" class="btn btn-default addRoleGroup">'+
                           '<span class="glyphicon glyphicon-plus"></span>'+
                           '</button>'+
                       '</div>'+
                   '</div>'+
                       */
            '<div class="col-md-3 ">'+
            '<div class="panel panel-primary ">'+
            '<div class="panel-heading">'+
            ' <label>'+userManage.ASSIGNED_PROD+'</label>'+
            '</div>'+
            '<div class="panel-body" id="exixtRoleGroupGrid"></div>'+
            '</div>'+
            '</div>'+

            /*'<div class="col-md-3" >'+
            '<div class="panel panel-primary ">'+
            '<div class="panel-heading">'+
            '<label>'+userManage.ASSIGNED_DIMENSION+'</label>'+
            '</div>'+
            '<div class="panel-body" id="roleGroupTree"  style="height:328px; overflow:auto"></div>'+
            '<div class="btn-group ui-nav-btn-group" style="text-align: right; "><button class="ui-nav-button menuRightTree text-left" type="button" title="右移" id="glyphicon glyphicon-menu-right"><span class="glyphicon glyphicon-menu-right"></span></button></div>'+
            '</div>'+
            '</div>'+*/

            /*	         '<div class="col-md-1" style="width: 50px;padding-top: 150px;">' +
                            '<div class="btn-separate-group-vertical">'+
                                '<button type="button" class="btn btn-default menuLeftTree">'+
                                   ' <span class="glyphicon glyphicon-menu-left"></span>'+
                                '</button>'+
                               '<button type="button" class="btn btn-default menuRightTree">'+
                                    '<span class="glyphicon glyphicon-menu-right"></span>'+
                                '</button>'+
                            '</div>'+
                        '</div>'+*/


            /*'<div class="col-md-3" style="height:30%;" >'+
            '<div class="panel panel-primary ">'+
            '<div class="panel-heading">'+
            '<label>'+userManage.DIMENSIONS_AVAILABLE_ASSIGNMENT+'</label>'+
            '</div>'+
            '<div class="panel-body" id="roleGroupTreeAll"  style="height:328px; overflow:auto"></div>'+
            '<div class="btn-group ui-nav-btn-group" style="text-align: left; "> <button class="ui-nav-button menuLeftTree text-left" type="button" title="右移" id="glyphicon glyphicon-menu-left"><span class="glyphicon glyphicon-menu-left"></span></button></div>'+
            '</div>'+
            '</div>'+
            '</div>'+*/

            '</div>'+
           /* '<div id="grid_content" class="ui-tabs-panel">'+
            //权限点
            '<div class="row">'+
            '<div class="col-md-3 ">'+
            '<div class="panel panel-primary " style="height: 377px;">'+
            '<div class="panel-heading">'+
            ' <label>'+userManage.ROLE_POINTS_AVAILABLE_ASSIGNMENT+'</label>'+
            '</div>'+
            '<div class="panel-body searchdistribution">'+
            '</div>'+

            '<div id="distributionRoleGrid"></div>'+
            '</div>'+
            '</div>'+


            /!*		         '<div class="col-md-1" style="width: 50px;padding-top: 100px;">' +
                                '<div class="btn-separate-group-vertical">'+
                                    '<button type="button" class="btn btn-default distributionRoleGridLeft">'+
                                       ' <span class="glyphicon glyphicon-menu-left"></span>'+
                                    '</button>'+
                                   '<button type="button" class="btn btn-default distributionRoleGridRight">'+
                                        '<span class="glyphicon glyphicon-menu-right"></span>'+
                                    '</button>'+


                                    '<button type="button" class="btn btn-default  adddistributionRole">'+
                                        '<span class="glyphicon glyphicon-plus"></span>'+
                                    '</button>'+

                                '</div>'+
                            '</div>'+*!/


            '<div class="col-md-3 ">'+
            '<div class="panel panel-primary ">'+
            '<div class="panel-heading">'+
            ' <label>'+userManage.ASSIGNED_ROLE_POINT+'</label>'+
            '</div>'+
            '<div class="panel-body" id="existDistributionRoleGrid"></div>'+
            '</div>'+
            '</div>'+


            '<div class="col-md-3" >'+
            '<div class="panel panel-primary ">'+
            '<div class="panel-heading">'+
            '<label>'+userManage.ASSIGNED_DIMENSION+'</label>'+
            '</div>'+
            '<div class="panel-body" id="existDistributionRoleTree"  style="height:328px; overflow:auto"></div>'+
            '<div class="btn-group ui-nav-btn-group" style="text-align: right; "><button class="ui-nav-button existRightTree text-left" type="button" title="右移" id="glyphicon glyphicon-menu-right"><span class="glyphicon glyphicon-menu-right"></span></button></div>'+
            '</div>'+
            '</div>'+

            /!*	         '<div class="col-md-1" style="width: 50px;padding-top: 150px;">' +
                            '<div class="btn-separate-group-vertical">'+
                                '<button type="button" class="btn btn-default existLeftTree">'+
                                   ' <span class="glyphicon glyphicon-menu-left"></span>'+
                                '</button>'+
                               '<button type="button" class="btn btn-default  existRightTree">'+
                                    '<span class="glyphicon glyphicon-menu-right"></span>'+
                                '</button>'+
                            '</div>'+
                        '</div>'+*!/



            '<div class="col-md-3 ">'+
            '<div class="panel panel-primary ">'+
            '<div class="panel-heading">'+
            '<label>'+userManage.DIMENSIONS_AVAILABLE_ASSIGNMENT+'</label>'+
            '</div>'+

            '<div class="panel-body" id="existDistributionRoleTreeAll"  style="height:328px; overflow:auto"></div>'+
            '<div class="btn-group ui-nav-btn-group" style="text-align: left; "> <button class="ui-nav-button existLeftTree text-left" type="button" title="右移" id="glyphicon glyphicon-menu-left"><span class="glyphicon glyphicon-menu-left"></span></button></div>'+
            '</div>'+
            '</div>'+
            '</div>'+
            '</div>'+
            '</div>'+
            '</div>'+
*/
            '</div>'+

            '<div class="text-center">'+
            '<button type="button" id="saveRoleGroup" class="btn btn-info">'+userManage.ASSIGNING_PROD+'</button>&nbsp&nbsp'+
            '<button type="button" id="btnSub" class="btn btn-info">'+userManage.CLOSE_PAGE+'</button> </div>' +
            '</div>'+
            '</div>'+
            '</div>';
        var popView = ngc.View.extend({
            el:false,
            /**保存权限组维度数据**/
            roleGroupTreeData:[],

            /**保存权限组点维度数据**/
            roleDistributionData:[],

            /**保存权限组维度删除数据**/
            delroleGroupData:[],

            /**保存权限组点移动的维度数据**/
            delroleDistributionData:[],

            roleIdArray:[],

            staffIdArray:[],

            /**维度数据**/
            dimensionTypeArray:[],

            /**点击权限组记录维度数据**/
            checkRepeatGroupData:[],

            /**点击权限点记录维度数据**/
            checkRepeatDistributionData:[],

            template: ngc.compile(templatehtml),
            events: {
                "click .menuLeft": 'menuGridLeft',
                "click .menuRight": 'menuGridRight',
                "click .distributionRoleGridLeft": 'distributionRoleGridLeft',
                "click .distributionRoleGridRight": 'distributionRoleGridRight',
                "click .menuLeftTree": 'menuLeftTree',
                "click .menuRightTree": 'menuRightTree',
                "click .existLeftTree": 'existLeftTree',
                "click .existRightTree": 'existRightTree',
                "click .addRoleGroup": 'addRoleGroup',
                "click .adddistributionRole": 'adddistributionRole',
                "click #saveRoleGroup": 'saveRoleGroup',
                "click #btnSub": 'btnSub',
            },

            initialize : function() {
                this.roleGroupTreeAll();
                this.roleGroupTree();
                this.existDistributionRoleTree();
                this.existDistributionRoleTreeAll();
            },

            afterRender: function () {
                $("#tabs").tabs({
                    canClose:true,
                    paging: true,
                    autoResizable:true
                });

                if (!!window.ActiveXObject || "ActiveXObject" in window){
                    $("#dialogMd").css("margin-top","50px");
                }else{
                    $("#dialogMd").css("margin-top","20px");
                }

                var userId = this.options.config.resultData.selId,domainId = this.options.config.domainId,that = this;
                var param = { entity:{id:userId,
                        typeId:this.options.config.resultData.typeId }
                };

                $.when(userId?prodApplyErganAction.queryProdAssingInfo(userId.toString(), '1', $.noop):$.noop,
                    userId?prodApplyErganAction.queryProdAssingInfo(userId.toString(), '2', $.noop):$.noop,
                    userId?prodApplyErganAction.selectRolesListInStaffId(this.options.appName,domainId,param,$.noop):$.noop

                ).then(function(rule,detail,staff){
                    /**可供分配的产品**/
                    that.nonexixtRoleGroupGrid(rule[0].data);

                    /**已分配的产品***/
                    that.exixtRoleGroupGrid(detail[0].data);

                    /**查询登录用户可分配，且被分配用户不具备的权限点 **/
                    that.distributionRoleGrid("");

                    /**查询用户具备的权限点 **/
                    that.existDistributionRoleGrid(staff[0].data);

                    /**初始化顶级节点维度数据 **/
                    var retult = prodApplyErganAction.selectListDimensionType(that.options.appName,domainId);
                    that.dimensionTypeArray = retult.responseJSON.data;
                });
                that._initDistributionSel();


            },


            roleGroupTreeAll:function(){

                var that = this;
                var callbackEvent = {
                    onExpand : function(e, treeNode, clickFlag) {
                        var node = this.getNodeByTId(treeNode.tId);
                        try {
                            node.children.length;
                            var id = node["id"];
                            var treeLife = that.getView("#roleGroupTreeAll");
                            var treeNodeLeft = treeLife.getNodes();
                            var nodeLeft1;
                            for(var i=0; i<treeNodeLeft.length; i++){
                                if(id == treeNodeLeft[i]["id"]){
                                    nodeLeft1 = treeNodeLeft[i];
                                    break;
                                }
                                nodeLeft1 = that.getElements(id,treeNodeLeft[i],node.getParentNode().id);
                            }

                            var nodeLeft = that._getLeftTree().getNodeByTId(nodeLeft1.tId);
                            that._getLeftTree().expandNode(nodeLeft,true,false,true,false);
                        } catch (e) {
                            that.loadResTypeCompareTree(node);
                        }

                    },
                }

                // 树的使用
                this.setView("#roleGroupTreeAll", new TreeView({config:{
                        tree:{
                            idKey:"id",
                            data : {
                                simpleData : {
                                    enable : true
                                }
                            },
                            view: {
                                showIcon: this.showIconForTreeRight,
                                fontCss: this.getNodeFont
                            },
                        },
                        callback:callbackEvent

                    }}));


                this.getView("#roleGroupTreeAll").on("viewRenderAfter", function() {
                    var result = prodApplyErganAction.selectListDimensionChilds(that.options.appName,"default","","-3","");
                    var resultData = result.responseJSON.data;
                    for(var i = 0;i<resultData.length;i++){
                        if (resultData[i].isChild == 1){
                            resultData[i].isParent = true;
                        }
                    }
                    this.reloadData(resultData);
                    /*   $($(".ztree")[3]).css("overflow-x","auto");
                       $($(".ztree")[3]).css("height","310px");*/

                });

            },





            roleGroupTree :function(){
                var that = this;
                this.setView("#roleGroupTree", new TreeView({config:{
                        tree:{
                            idKey:"id",
                            data : {
                                simpleData : {
                                    enable : true
                                }
                            },
                            view: {
                                showIcon: this.showIconForTreeRight,
                                fontCss: this.getNodeFont
                            }
                        },

                    }}));

                this.getView("#roleGroupTree").on("viewRenderAfter", function() {
                    $($(".ztree")[1]).css({"overflow-x":"auto","height":"315px"});
                });


            },



            //是否显示图标
            showIconForTreeRight :function (treeNode){
                if(treeNode.id == 1){
                    return false;
                }
                if(treeNode.isSame == 'DIFF' || treeNode.isSame == 'SOURCE_NOT_EXIST'|| treeNode.isSame == 'COMPARE_NOT_EXIST'){
                    return true;
                }else{
                    return false;
                }
            },



            //加载所有的维度
            loadResTypeCompareTree:function(node){
                var result ;
                var roleGroupTreeAll = this.getView("#roleGroupTreeAll");
                roleGroupTreeAll.removeChildNodes(node);
                if(!this.firstQueryRes){//第一次会根据当前登陆人的所属部门区域进行过滤
                    if(this.options.config.regionId){
                        node.id = this.options.config.regionId;
                        result = prodApplyErganAction.selectListDimensionChildsSupportRegionId(this.options.appName,"default",node.dimensionTypeId,node.id,"");
                    }else{
                        result = prodApplyErganAction.selectListDimensionChilds(this.options.appName,"default",node.dimensionTypeId,node.id,"");
                    }
                    this.firstQueryRes = true;
                }else{
                    result = prodApplyErganAction.selectListDimensionChilds(this.options.appName,"default",node.dimensionTypeId,node.id,"");
                }

                var data = result.responseJSON.data;
                for(var i = 0;i<data.length;i++){
                    if (data[i].isChild == 1){
                        data[i].isParent = true;
                    }
                }
                roleGroupTreeAll.addNodes(node,data);

            },



            //加载所有权限点维度
            loadexistDistributionRoleTree:function(node){
                var roleGroupTreeAll = this.getView("#existDistributionRoleTreeAll");
                roleGroupTreeAll.removeChildNodes(node);
                var result ;
                if(!this.firstQueryResRole){//第一次会根据当前登陆人的所属部门区域进行过滤
                    if(this.options.config.regionId){
                        node.id = this.options.config.regionId;
                        result = prodApplyErganAction.selectListDimensionChildsSupportRegionId(this.options.appName,"default",node.dimensionTypeId,node.id,"");
                    }else{
                        result = prodApplyErganAction.selectListDimensionChilds(this.options.appName,"default",node.dimensionTypeId,node.id,"");
                    }
                    this.firstQueryResRole = true;
                }else{
                    result = prodApplyErganAction.selectListDimensionChilds(this.options.appName,"default",node.dimensionTypeId,node.id,"");
                }

                var data = result.responseJSON.data;
                for(var i = 0;i<data.length;i++){
                    if (data[i].isChild == 1){
                        data[i].isParent = true;
                    }
                }
                roleGroupTreeAll.addNodes(node,data);

            },


            /**权限点所有的维度**/
            existDistributionRoleTreeAll:function(){
                var that = this;
                var callbackEvent = {
                    onExpand : function(e, treeNode, clickFlag) {
                        var node = this.getNodeByTId(treeNode.tId);
                        try {
                            node.children.length;
                            var id = node["id"];
                            var treeLife = that.getView("#existDistributionRoleTreeAll");
                            var treeNodeLeft = treeLife.getNodes();
                            var nodeLeft1;
                            for(var i=0; i<treeNodeLeft.length; i++){
                                if(id == treeNodeLeft[i]["id"]){
                                    nodeLeft1 = treeNodeLeft[i];
                                    break;
                                }
                                nodeLeft1 = that.getElements(id,treeNodeLeft[i],node.getParentNode().id);
                            }

                            var nodeLeft = that._getLeftTree().getNodeByTId(nodeLeft1.tId);
                            that._getLeftTree().expandNode(nodeLeft,true,false,true,false);
                        } catch (e) {
                            that.loadexistDistributionRoleTree(node);
                        }

                    },
                }

                // 树的使用
                this.setView("#existDistributionRoleTreeAll", new TreeView({config:{
                        tree:{
                            idKey:"id",
                            data : {
                                simpleData : {
                                    enable : true
                                }
                            },
                            view: {
                                showIcon: this.showIconForTreeRight,
                                fontCss: this.getNodeFont
                            },
                        },
                        callback:callbackEvent

                    }}));


                this.getView("#existDistributionRoleTreeAll").on("viewRenderAfter", function() {
                    var result = prodApplyErganAction.selectListDimensionChilds(that.options.appName,"default","","-3","");
                    var resultData = result.responseJSON.data;
                    for(var i = 0;i<resultData.length;i++){
                        if (resultData[i].isChild == 1){
                            resultData[i].isParent = true;
                        }
                    }
                    this.reloadData(resultData);
                    $($(".ztree")[2]).css({"overflow-x":"auto","height":"315px"});

                });

            },






            existDistributionRoleTree :function(){
                var that = this;
                this.setView("#existDistributionRoleTree", new TreeView({config:{
                        tree:{
                            idKey:"id",
                            data : {
                                simpleData : {
                                    enable : true
                                }
                            },
                            view: {
                                showIcon: this.showIconForTreeRight,
                                fontCss: this.getNodeFont
                            },
                        },

                    }}));

            },

            /**可供分配的权限组**/
            nonexixtRoleGroupGrid:function(grid_datas){
                var that = this;
                var opt = {
                    height: 333,
                    multiselect:true,
                    colModel : [
                        {
                            name: 'roleId',
                            index: 'roleId',
                            label: userManage.PROD_ID,
                            width:'120',
                            key: true
                        },
                        {
                            name: 'roleName',
                            label: userManage.PROD_NAME,
                            index: 'roleName',
                            width:'120'
                        }
                    ],

                    onSelectRow: function (e, rowid, state, checked) {//选中行事件
                        that.getView("#nonexixtRoleGroupGrid").setCheckRows([rowid], true);
                    },

                    onDblClickRow: function (e, rowid, state, checked) {
                        that.menuRight("nonexixtRoleGroupGrid","exixtRoleGroupGrid");
                    },
                    toolbar: [true, "bottom"]

                };
                this.requireView({
                    selector:"#nonexixtRoleGroupGrid",
                    url:"module/component/views/GridView",
                    viewOption :{config:{grid:opt}}
                }).then(function(){
                    var _grid = that.getView("#nonexixtRoleGroupGrid");
                    /**右键菜单**/
                    _grid.on("onClickMenu",function(menuId,selNode,menu,e){
                        switch (menuId) {
                            case 'delete':
                                ngc.confirm(userManage.CONFIRM_DELETE+selNode+'?').result.then(function() {
                                    _grid.delRow(selNode);
                                });
                                break;
                        }
                    });
                    _grid.navButtonAdd([{
                        title:userManage.ADD,
                        id:"glyphicon glyphicon-menu-right",
                        buttonicon:"glyphicon glyphicon-menu-right",
                        navpos:"bottombar",
                        cssprop:"menuRight text-left"

                    }]);

                    if(grid_datas){
                        _grid.reloadData(grid_datas.data);
                    }
                    that.$("#nonexixtRoleGroupGrid .ui-userdata-tb").css("text-align", "right");
                });

            },

            /**已分配的产品***/
            exixtRoleGroupGrid:function(grid_datas){
                var that = this;
                var opt = {
                    height: 333,
                    multiselect:true,
                    colModel : [{
                        name: 'roleId',
                        index: 'roleId',
                        label: userManage.PROD_ID,
                        key: true
                    },

                        {
                            name: 'roleName',
                            label:userManage.PROD_NAME,
                            index: 'roleName',

                        }
                        /*,{
                            name: 'roleType',
                            label: userManage.CAN_BE_LOWERED,
                            index: 'roleType',
                            sorttype:'float',
                            edittype: "select",
                            formatter: "select",
                            editoptions:{value:"2:"+userManage.CAN_BE_LOWERED+";0:"+userManage.CANNOT_BE_LOWERED+""}
                        }*/
                    ],
                    /*onSelectRow: function (e, rowid, state, checked) {//选中行事件
                        if(checked == false){
                            that.getView("#exixtRoleGroupGrid").setCheckRows([rowid], true);
                        }else{
                            that.getView("#exixtRoleGroupGrid").setCheckRows([rowid], false);
                        }
                        if(e.toElement != null && e.toElement.type != undefined && e.toElement.type =="checkbox"){
                            if(e.toElement.checked == true){
                                that.getView("#exixtRoleGroupGrid").setCheckRows([rowid], false);
                            }else{
                                that.getView("#exixtRoleGroupGrid").setCheckRows([rowid], true);
                            }
                        }

                        var staffId = that.options.config.resultData.selId;
                        var param={
                            entity:{id:staffId,
                                typeId:that.options.config.resultData.typeId},id:rowid
                        };
                        var resultState = prodApplyErganAction.selectListDimensionForStaffRole(that.options.appName,"default",param);
                        //var resultState = prodApplyErganAction.selectListDimensionForStaffRolegrp(that.options.appName,"default",param);
                        var resultData = resultState.responseJSON.data;
                        var roleGroupTree = that.getView("#roleGroupTree");
                        roleGroupTree.reloadData(null);
                        if(resultData){
                            for(var i = 0;i<resultData.length;i++){
                                var data = resultData[i];
                                if (data.isChild == 1){
                                    data.isParent = true;
                                }
                                //判断重复数据的时候会用到
                                data = $.extend({},data,{roleId:rowid});
                                that.checkRepeatGroupData.push(data);
                            }
                            roleGroupTree.reloadData(resultData);
                        }

                        /!**获取上次移动的数据**!/
                        var groupTreeData= that.roleGroupTreeData;
                        if(groupTreeData.length>0){
                            var treeLifeNode = roleGroupTree.getNodes();
                            /!**没有节点就默认从移动数据里面获取节点**!/
                            if(treeLifeNode.length !=0){
                                for(var i = 0;i<groupTreeData.length;i++){
                                    //判断重复数据的时候会用到
                                    that.checkRepeatGroupData.push(groupTreeData[i]);
                                    if(rowid == groupTreeData[i].roleId && groupTreeData[i].name !=undefined){
                                        for(var j = 0;j<treeLifeNode.length;j++){
                                            if(groupTreeData[i].dimensionTypeId ==treeLifeNode[j].dimensionTypeId){
                                                roleGroupTree.addNodes(treeLifeNode[j],groupTreeData[i]
                                                    ,true);
                                            }
                                        }

                                    }

                                }
                            }else{
                                for(var i = 0;i<groupTreeData.length;i++){
                                    that.checkRepeatGroupData.push(groupTreeData[i]);
                                    if(rowid == groupTreeData[i].roleId && groupTreeData[i].name !=undefined){
                                        roleGroupTree.addNodes(null,groupTreeData[i]
                                            ,true);

                                    }
                                }
                            }

                        }
                        roleGroupTree.expandAll(true);
                        $(roleGroupTree.$el).find(".ztree").css({"overflow-x":"auto","height":"315px"});

                    },*/

                    toolbar: [true, "bottom"]
                };

                this.requireView({
                    selector:"#exixtRoleGroupGrid",
                    url:"module/component/views/GridView",
                    viewOption :{config:{grid:opt}}
                }).then(function(){
                    var _grid = that.getView("#exixtRoleGroupGrid");
                    _grid.attachMenu([{id:'detrion',label:userManage.CAN_BE_LOWERED,icon:'glyphicon glyphicon-retweet',index:0},{id:'notdetrion',label:userManage.CANNOT_BE_LOWERED,icon:'glyphicon glyphicon-retweet',index:0}]);
                    _grid.on("onClickMenu",function(menuId,selNode,menu,e){
                        switch (menuId) {
                            case 'detrion':
                                that.detrion(selNode,"exixtRoleGroupGrid");
                                break;
                            case 'notdetrion':
                                that.notdetrion(selNode,"exixtRoleGroupGrid");
                                break;
                        }
                    });

                    _grid.navButtonAdd([{
                        title:userManage.MOVE_LEFT,
                        id:"glyphicon glyphicon-menu-left",
                        buttonicon:"glyphicon glyphicon-menu-left",
                        navpos:"bottombar",
                        cssprop:"menuLeft"

                    }
                    /*,{
                        title:userManage.ADD,
                        id:"grid_content_addIcon",
                        buttonicon:"glyphicon glyphicon-plus",
                        navpos:"bottombar",
                        cssprop:"addRoleGroup",
                        onClick:function(){
                            that.gridAdd();
                        }
                    }*/
                    ]);

                    if(grid_datas){
                        _grid.reloadData(grid_datas.data);
                    }
                });

            },



            /**可供分配的权限点**/
            distributionRoleGrid:function(grid_datas){
                var that = this;
                var opt = {
                    height: 282,
                    multiselect:true,
                    colModel : [{
                        name: 'PROD_ID',
                        index: 'PROD_ID',
                        label: userManage.PROD_ID,
                        width:'80',
                        key: true
                    },

                        {
                            name: 'PROD_NAME',
                            label: userManage.PROD_NAME,
                            index: 'PROD_NAME',

                        }
                    ],
                    onSelectRow: function (e, rowid, state, checked) {//选中行事件
                        that.getView("#distributionRoleGrid").setCheckRows([rowid], true);
                    },
                    onDblClickRow: function (e, rowid, state, checked) {
                        that.distributionmenuRight("distributionRoleGrid","existDistributionRoleGrid");
                    },
                    toolbar: [true, "bottom"]
                };
                this.requireView({
                    selector:"#distributionRoleGrid",
                    url:"module/component/views/GridView",
                    viewOption :{config:{grid:opt}}
                }).then(function(){
                    var _grid = that.getView("#distributionRoleGrid");
                    _grid.navButtonAdd([{
                        title:userManage.MOVE_RIGHT,
                        id:"glyphicon glyphicon-menu-right",
                        buttonicon:"glyphicon glyphicon-menu-right",
                        navpos:"bottombar",
                        cssprop:"distributionRoleGridRight",
                        height:"240px"
                    }])

                    if(grid_datas){
                        _grid.reloadData(grid_datas);
                    }

                    that.$("#distributionRoleGrid .ui-userdata-tb").css({"text-align":"right","margin-top":"5px"});

                });
            },


            /**已分配的权限点**/
            existDistributionRoleGrid:function(grid_datas){
                var that = this;
                var opt = {
                    height: 333,
                    multiselect:true,
                    colModel : [{
                        name: 'roleId',
                        index: 'roleId',
                        label: userManage.SPECIFICATION,
                        key: true
                    },
                        {
                            name: 'roleName',
                            label: userManage.NAME,
                            index: 'roleName',

                        },
                        {
                            name: 'roleType',
                            label:  userManage.CAN_BE_LOWERED,
                            index: 'roleType',
                            sorttype:'float',
                            edittype: "select",
                            formatter: "select",
                            editoptions:{value:"2:"+userManage.CAN_BE_LOWERED+";0:"+userManage.CANNOT_BE_LOWERED+""}
                        }

                    ],

                    /*onSelectRow: function (e, rowid, state, checked) {//选中行事件
                        var grid = that.getView("#existDistributionRoleGrid");
                        if(checked == false){
                            grid.setCheckRows([rowid], true);
                        }else{
                            grid.setCheckRows([rowid], false);
                        }
                        if(e.toElement != null && e.toElement.type != undefined && e.toElement.type =="checkbox"){
                            if(e.toElement.checked == true){
                                grid.setCheckRows([rowid], false);
                            }else{
                                grid.setCheckRows([rowid], true);
                            }
                        }

                        var staffId = that.options.config.resultData.selId;
                        var param={
                            entity:{id:staffId,
                                typeId:that.options.config.resultData.typeId},id:rowid
                        }
                        var resultState = prodApplyErganAction.selectListDimensionForStaffRole(that.options.appName,"default",param);
                        var resultData = resultState.responseJSON.data;
                        var roleGroupTree = that.getView("#existDistributionRoleTree");
                        roleGroupTree.reloadData(null);

                        if(resultData){
                            for(var i = 0;i<resultData.length;i++){
                                if (resultData[i].isChild == 1){
                                    resultData[i].isParent = true;
                                }
                                //判断重复数据的时候会用到
                                that.checkRepeatDistributionData.push(resultData[i]);

                            }
                            roleGroupTree.reloadData(resultData);
                        }

                        /!**获取上次移动的数据**!/
                        var groupTreeData= that.roleDistributionData;
                        if(groupTreeData.length>0){
                            var treeLifeNode = roleGroupTree.getNodes();

                            if(treeLifeNode.length !=0){
                                for(var i = 0;i<groupTreeData.length;i++){
                                    that.checkRepeatDistributionData.push(groupTreeData[i]);
                                    if(rowid == groupTreeData[i].roleId && groupTreeData[i].name !=undefined){
                                        for(var j = 0;j<treeLifeNode.length;j++){
                                            if(groupTreeData[i].dimensionTypeId ==treeLifeNode[j].dimensionTypeId){
                                                roleGroupTree.addNodes(treeLifeNode[j],groupTreeData[i]
                                                    ,true);
                                            }
                                        }

                                    }
                                }
                            }else{
                                for(var i = 0;i<groupTreeData.length;i++){
                                    that.checkRepeatDistributionData.push(groupTreeData[i]);
                                    if(rowid == groupTreeData[i].roleId && groupTreeData[i].name !=undefined){
                                        roleGroupTree.addNodes(null,groupTreeData[i]
                                            ,true);

                                    }
                                }
                            }
                        }
                        roleGroupTree.expandAll(true);
                        $(roleGroupTree.$el).find(".ztree").css({"overflow-x":"auto","height":"315px"});

                    },*/

                    toolbar: [true, "bottom"]
                };

                this.requireView({
                    selector:"#existDistributionRoleGrid",
                    url:"module/component/views/GridView",
                    viewOption :{config:{grid:opt}}
                }).then(function(){
                    var _grid = that.getView("#existDistributionRoleGrid");
                    _grid.attachMenu([{id:'detrion',label:userManage.CAN_BE_LOWERED,icon:'glyphicon glyphicon-retweet',index:0},{id:'notdetrion',label:userManage.CANNOT_BE_LOWERED,icon:'glyphicon glyphicon-retweet',index:0}]);
                    _grid.on("onClickMenu",function(menuId,selNode,menu,e){
                        switch (menuId) {
                            case 'detrion':
                                that.detrion(selNode,"existDistributionRoleGrid");
                                break;
                            case 'notdetrion':
                                that.notdetrion(selNode,"existDistributionRoleGrid");
                                break;
                        }
                    });

                    _grid.navButtonAdd([{
                        title:userManage.MOVE_LEFT,
                        id:"glyphicon glyphicon-menu-left",
                        buttonicon:"glyphicon glyphicon-menu-left",
                        navpos:"bottombar",
                        cssprop:"distributionRoleGridLeft"

                    },{
                        title:userManage.ADD,
                        id:"grid_content_addIcon",
                        buttonicon:"glyphicon glyphicon-plus",
                        navpos:"bottombar",
                        cssprop:"adddistributionRole"

                    }]);

                    if(grid_datas){
                        _grid.reloadData(grid_datas);
                    }
                });
            },







            initSearchFrom: function(){
                var _config = {elements:[
                        {id:'nameBegin',type:'text',label:userManage.DEPARTMENT_NAME},
                        {id:'nameStep',type:'text',label:userManage.MANAGEMENT_AREA}
                    ]};
                this.requireView({
                    selector:".search-form",
                    url:"module/component/views/FormView",
                    viewOption :{config:_config}
                }).then(function(){


                });
            },



            /**权限组**/
            menuGridLeft:function(){
                this.menuLeft("exixtRoleGroupGrid","nonexixtRoleGroupGrid");
            },


            menuGridRight:function(){
                this.menuRight("nonexixtRoleGroupGrid","exixtRoleGroupGrid");
            },

            /**权限点**/
            distributionRoleGridLeft:function(){
                this.distributionmenuLeft("existDistributionRoleGrid","distributionRoleGrid");
            },

            distributionRoleGridRight:function(){
                this.distributionmenuRight("distributionRoleGrid","existDistributionRoleGrid");
            },

            /**产品右移**/
            menuRight:function(delGrid,addGrid){
                var rowData = this.getView("#"+delGrid+"").getCheckRows();
                for(var i = 0;i<rowData.length;i++){
                    rowData[i].roleId = rowData[i].roleId;
                    rowData[i].roleName = rowData[i].roleName;
                    this.getView("#"+delGrid+"").delRow(rowData[i].roleId);
                }
                this.getView("#"+addGrid+"").addRowData("1",rowData,"first");
            },

            /**产品左移**/
            menuLeft:function(delGrid,addGrid){
                var rowData = this.getView("#"+delGrid+"").getCheckRows();
                for(var i = 0;i<rowData.length;i++){
                    rowData[i].roleId = rowData[i].roleId;
                    rowData[i].roleName = rowData[i].roleName;
                    this.getView("#"+delGrid+"").delRow(rowData[i].roleId);
                }
                this.getView("#"+addGrid+"").addRowData("1",rowData,"first");

            },




            /**权限点数据转换**/
            distributionmenuRight:function(delGrid,addGrid){
                var rowData = this.getView("#"+delGrid+"").getCheckRows();
                for(var i = 0;i<rowData.length;i++){
                    rowData[i].roleId = rowData[i].roleId;
                    rowData[i].roleName = rowData[i].name;
                    rowData[i].roleType = 2;
                    this.getView("#"+delGrid+"").delRow(rowData[i].roleId);
                }
                this.getView("#"+addGrid+"").addRowData("1",rowData,"first");
            },

            distributionmenuLeft:function(delGrid,addGrid){
                var rowData = this.getView("#"+delGrid+"").getCheckRows();
                for(var i = 0;i<rowData.length;i++){
                    rowData[i].prodId = rowData[i].PROD_ID;
                    rowData[i].prodName = rowData[i].PROD_NAME;
                    delete rowData[i].roleType;
                    this.getView("#"+delGrid+"").delRow(rowData[i].prodId);
                }
                this.getView("#"+addGrid+"").addRowData("1",rowData,"first");

            },






            /**权限组**/
            menuLeftTree:function(){
                var rowData = this.getView("#exixtRoleGroupGrid").getCheckRows();
                if(rowData.length == 0){
                    ngc.error(userManage.SELECT_ROLE_DATA_DIMENSION);
                    return;
                }
                this.LeftTree("roleGroupTreeAll","roleGroupTree");
            },

            menuRightTree:function(){
                this.RightTree("roleGroupTree");
            },



            /**权限点**/
            existLeftTree:function(){
                var rowData = this.getView("#existDistributionRoleGrid").getCheckRows();
                if(rowData.length == 0){
                    ngc.error(userManage.SELECT_ROLE_POINT_DATA_DIMENSION);
                    return;
                }
                this.LeftTree("existDistributionRoleTreeAll","existDistributionRoleTree");
            },


            existRightTree:function(){
                this.RightTree("existDistributionRoleTree");
            },


            LeftTree:function(leftTree,rightTree){
                var node = this.getView("#"+leftTree+"").getSelectedNodes()[0],that = this;
                delete node.isParent;
                delete node.children;
                var treeLife = this.getView("#"+rightTree+"");
                /**无限制**/
                if(node.dimensionTypeId == '0' && node.id == '-1' && node.canDistribution != "0"){
                    if(leftTree == "existDistributionRoleTreeAll"){
                        var rowData = that.getView("#existDistributionRoleGrid").getSelection();
                        //记录权限点id
                        node = $.extend({},node,{pId:node.pId,roleId:rowData.roleId,roleType:rowData.roleType});
                        that.roleDistributionData.push(node);
                    }else{
                        /**保存权限组移动数据**/
                        var rowData = that.getView("#exixtRoleGroupGrid").getSelection();
                        //记录权限点id
                        node = $.extend({},node,{pId:node.pId,roleId:rowData.roleId,roleType:rowData.roleType});
                        that.roleGroupTreeData.push(node);
                    }

                    if(that.contains(treeLife.getNodes(),node.id)){
                        ngc.error(userManage.NODE_DUPLICATE_DATA);
                        return;
                    }else{
                        treeLife.addNodes(null,node
                            ,true);
                    }

                    return;
                }

                if(node.canDistribution != "0"){
                    var nodes = treeLife.getNodes();
                    if(nodes.length ==0){
                        var newNodes = [];
                        var parentNode = node.getParentNode();
                        delete parentNode.children;
                        if(leftTree == "existDistributionRoleTreeAll"){
                            // 批量分配权限点
                            var _checkRowArray = that.getView("#existDistributionRoleGrid").getCheckRows();
                            var selRow = that.getView("#existDistributionRoleGrid").getSelection();
                            for(var i = _checkRowArray.length;i--;) {
                                var  rowData= _checkRowArray[i];
                                if(!that.containsByRoleId(that.checkRepeatDistributionData,rowData.roleId,node.id)){
                                    //记录权限点id
                                    parentNode = this.getParentNode(node.dimensionTypeId);
                                    if(!that.containsByRoleId(that.checkRepeatDistributionData,rowData.roleId,parentNode.id)){
                                        parentNode = $.extend({},parentNode,{isParent:true,roleType:rowData.roleType,roleId:rowData.roleId,pId:""});
                                        that.roleDistributionData.push(parentNode);
                                        newNodes.push(parentNode);
                                    }
                                    node = $.extend({},node,{pId:parentNode.id,roleId:rowData.roleId,roleType:rowData.roleType});
                                    that.roleDistributionData.push(node);
                                    that.checkRepeatDistributionData.push(node);
                                    newNodes.push(node);
                                }else{
                                    ngc.error(rowData.roleName+userManage.DUPLICATE_DIMENSION_DATA);
                                    return;

                                }
                                newNodes = that.restulArrayByRoleId(newNodes,selRow.roleId);

                            }
                            //}

                        }else{
                            /**保存权限组移动数据 批量分配权限组**/
                            var _checkRowArray = that.getView("#exixtRoleGroupGrid").getCheckRows();
                            var selRow = that.getView("#exixtRoleGroupGrid").getSelection();
                            for(var i = _checkRowArray.length;i--;) {
                                //记录权限点id
                                var  rowData= _checkRowArray[i];
                                if(!that.containsByRoleId(that.checkRepeatGroupData,rowData.roleId,node.id)){
                                    parentNode = this.getParentNode(node.dimensionTypeId);
                                    if(!that.containsByRoleId(that.checkRepeatGroupData,rowData.roleId)){
                                        parentNode = $.extend({},parentNode,{isParent:true,roleId:rowData.roleId,roleType:rowData.roleType,pId:""});
                                        that.roleGroupTreeData.push(parentNode);
                                        newNodes.push(parentNode);

                                    }
                                    node = $.extend({},node,{pId:parentNode.id,roleId:rowData.roleId,roleType:rowData.roleType});
                                    that.roleGroupTreeData.push(node);
                                    that.checkRepeatGroupData.push(node);
                                    newNodes.push(node);

                                }else{
                                    ngc.error(rowData.roleName+userManage.DUPLICATE_DIMENSION_DATA);
                                    return;

                                }

                                newNodes = that.restulArrayByRoleId(newNodes,selRow.roleId);
                            }
                        }

                        treeLife.addNodes(null,newNodes
                            ,true);
                        $(treeLife.$el).find(".ztree").css({"overflow-x":"auto","height":"315px"});
                        that.cleanArray(that.newArray);
                    }else{
                        //获取左边已有的父节点。
                        var conNodes = that.containsByDtId(nodes,node.dimensionTypeId);
                        if(conNodes != null){
                            // 存在父节点 直接往父节点中 添加子节点
                            if(conNodes.dimensionTypeId == node.dimensionTypeId){
                                /**保存权限点移动数据**/
                                if(leftTree == "existDistributionRoleTreeAll"){
                                    var _checkRowArray = that.getView("#existDistributionRoleGrid").getCheckRows();
                                    var selRow = that.getView("#existDistributionRoleGrid").getSelection();

                                    //等于空addNode  不等于check重复节点
                                    var nodeArray = conNodes.children;
                                    if(nodeArray == undefined){
                                        for(var j = _checkRowArray.length;j--;) {
                                            var  rowData= _checkRowArray[j];
                                            if(!that.containsByRoleId(that.checkRepeatDistributionData,rowData.roleId, node.id) && that.containsTreeNode(rowData,node.id,node.dimensionTypeId)){
                                                newNode = $.extend({},node,{pId:node.pId,roleId:rowData.roleId,roleType:rowData.roleType});
                                                that.roleDistributionData.push(newNode);
                                                that.checkRepeatDistributionData.push(newNode);
                                                if(selRow.roleId == newNode.roleId){
                                                    treeLife.addNodes(conNodes,newNode
                                                        ,true);
                                                }
                                            }else{
                                                ngc.error(rowData.roleName+userManage.ROLE_GROUP_DUPLICATE_DIMENSION_DATA);
                                                return;
                                            }

                                        }

                                    }else{
                                        // roleDistributionData 权限点缓存数据
                                        for(var j = _checkRowArray.length;j--;) {
                                            var  rowData= _checkRowArray[j];
                                            if(that.containsByNodeId(that.roleDistributionData,rowData.roleId,node.id) && that.containsTreeNode(rowData,node.id,node.dimensionTypeId)){
                                                newNode = $.extend({},node,{pId:node.pId,roleId:rowData.roleId,roleType:rowData.roleType});
                                                that.roleDistributionData.push(newNode);
                                                that.checkRepeatDistributionData.push(newNode);
                                                if(selRow.roleId == newNode.roleId){
                                                    treeLife.addNodes(conNodes,newNode
                                                        ,true);
                                                }

                                            }else{
                                                ngc.error(rowData.roleName+userManage.ROLE_POINT_DUPLICATE_DIMENSION_DATA);
                                                return;
                                            }

                                        }


                                    }
                                    return;

                                }else{
                                    /**保存权限组移动数据**/
                                    var _checkRowArray = that.getView("#exixtRoleGroupGrid").getCheckRows();
                                    var selRow = that.getView("#exixtRoleGroupGrid").getSelection();

                                    //等于空addNode  不等于check重复节点
                                    var nodeArray = conNodes.children;
                                    if(nodeArray == undefined){
                                        for(var j = _checkRowArray.length;j--;) {
                                            var  rowData= _checkRowArray[j];
                                            if(that.containsByNodeId(that.checkRepeatGroupData,rowData.roleId,node.id) && that.containsTreeNode(nodes,node.id,node.dimensionTypeId)){
                                                newNode = $.extend({},node,{pId:node.pId,roleId:rowData.roleId,roleType:rowData.roleType});
                                                that.roleGroupTreeData.push(newNode);
                                                that.checkRepeatGroupData.push(newNode);
                                                if(selRow.roleId == newNode.roleId){
                                                    treeLife.addNodes(conNodes,newNode
                                                        ,true);
                                                }
                                            }else{
                                                ngc.error(userManage.ASSIGNED_PROD_DUPLICATE_DIMENSION_DATA);
                                                return;
                                            }
                                        }
                                    }else{
                                        for(var j = _checkRowArray.length;j--;) {
                                            var  rowData= _checkRowArray[j];
                                            if(that.containsByNodeId(that.checkRepeatGroupData,rowData.roleId,node.id) && that.containsTreeNode(nodes,node.id,node.dimensionTypeId)){
                                                newNode = $.extend({},node,{pId:node.pId,roleId:rowData.roleId,roleType:rowData.roleType});
                                                that.roleGroupTreeData.push(newNode)
                                                that.checkRepeatGroupData.push(newNode);
                                                if(selRow.roleId == newNode.roleId){
                                                    treeLife.addNodes(conNodes,newNode
                                                        ,true);
                                                }
                                            }else{
                                                ngc.error(userManage.ASSIGNED_PROD_DUPLICATE_DIMENSION_DATA);
                                                return;
                                            }

                                        }

                                    }
                                    return;

                                }
                            }
                        }else{
                            // 左侧没有父节点的
                            var parentNode = node.getParentNode();
                            if(that.contains(treeLife.getNodes(),parentNode.id)){
                                ngc.error(userManage.NODE_DUPLICATE_DATA);
                                return;
                            }else{
                                var newNodes = [];
                                delete parentNode.children;
                                newNodes.push(node);
                                newNodes.push(parentNode);
                                if(leftTree == "existDistributionRoleTreeAll"){
                                    /**保存权限点移动数据**/
                                        //var rowData = that.getView("#existDistributionRoleGrid").getSelection();
                                    var _checkRowArray = that.getView("#existDistributionRoleGrid").getCheckRows();
                                    for(var i = _checkRowArray.length;i--;) {
                                        var rowData= _checkRowArray[i];
                                        //记录权限点id
                                        parentNode = $.extend({},parentNode,{pId:parentNode.pId,roleId:rowData.roleId,roleType:rowData.roleType});
                                        that.roleDistributionData.push(parentNode);
                                        node = $.extend({},node,{pId:node.pId,roleId:rowData.roleId,roleType:rowData.roleType});
                                        that.roleDistributionData.push(node);
                                    }
                                }else{
                                    /**保存权限组移动数据**/
                                        //var rowData = that.getView("#exixtRoleGroupGrid").getSelection();
                                    var _checkRowArray = that.getView("#exixtRoleGroupGrid").getCheckRows();
                                    for(var i = _checkRowArray.length;i--;) {
                                        var rowData= _checkRowArray[i];
                                        //记录权限点id
                                        parentNode = $.extend({},parentNode,{pId:parentNode.pId,roleId:rowData.roleId,roleType:rowData.roleType});
                                        that.roleGroupTreeData.push(parentNode);
                                        node = $.extend({},node,{pId:node.pId,roleId:rowData.roleId,roleType:rowData.roleType});
                                        that.roleGroupTreeData.push(node);

                                    }

                                }
                                treeLife.addNodes(null,newNodes
                                    ,true);
                                treeLife.refresh();
                                return;
                            }

                        }
                    }


                }else{
                    ngc.error(userManage.NODE_CANNOT_MOVED);
                }
                treeLife.expandAll(true);
            },


            RightTree:function(rightTree){
                var that  = this,aiObjDt = {};
                var roleGroupTree = this.getView("#"+rightTree+"");
                var node = roleGroupTree.getSelectedNodes()[0];
                if(rightTree == "existDistributionRoleTree"){
                    //权限点
                    var rowData = that.getView("#existDistributionRoleGrid").getSelection();
                    //记录权限点id
                    if(node.children != undefined){
                        /**子节点也需要做移动数据保存**/
                        var nodeChildren =node.children;
                        for(var i = 0;i<nodeChildren.length;i++){
                            aiObjDt = $.extend({},aiObjDt,{id:nodeChildren[i].id,roleId:rowData.roleId,roleType:rowData.roleType,pId:nodeChildren[i].pId});
                            that.delroleGroupData.push(aiObjDt);
                            that.delArrayByRoleId(that.roleDistributionData,that.checkRepeatDistributionData,nodeChildren[i].id,rowData.roleId);
                        }
                    }else{
                        aiObjDt = $.extend({},aiObjDt,{id:node.id,roleId:rowData.roleId,roleType:rowData.roleType,pId:node.pId});
                        that.delroleGroupData.push(aiObjDt);
                        that.delArrayByRoleId(that.roleDistributionData,that.checkRepeatDistributionData,node.id,rowData.roleId);
                    }

                }else{
                    //权限组
                    var rowData = that.getView("#exixtRoleGroupGrid").getSelection();
                    if(node.children != undefined){
                        /**子节点也需要做移动数据保存**/
                        var nodeChildren =node.children;
                        for(var i = 0;i<nodeChildren.length;i++){
                            aiObjDt = $.extend({},aiObjDt,{id:nodeChildren[i].id,roleId:rowData.roleId,roleType:rowData.roleType,pId:nodeChildren[i].pId});
                            that.delroleDistributionData.push(aiObjDt);
                            that.delArrayByRoleId(that.roleGroupTreeData,that.checkRepeatGroupData,nodeChildren[i].id,rowData.roleId);

                        }
                    }else{
                        aiObjDt = $.extend({},aiObjDt,{id:node.id,roleId:rowData.roleId,roleType:rowData.roleType,pId:node.pId});
                        that.delroleDistributionData.push(aiObjDt);
                        that.delArrayByRoleId(that.roleGroupTreeData,that.checkRepeatGroupData,node.id,rowData.roleId);

                    }

                }
                roleGroupTree.removeNode(node);
            },


            /**分配产品给用户**/
            saveRoleGroup:function(){
                var that  = this;
                this.progressOn(userManage.ROLE_DATA_SUBMIT);
                var staffId = this.options.config.resultData.selId;
                /**获取分配的产品数据**/
                var girdData = this.getView("#exixtRoleGroupGrid").getRowData(null,false);
                var object = new Array();
                if(girdData.length>0){
                    for(var i = 0;i<girdData.length;i++){
                        var params = {
                            prodId:girdData[i].roleId
                        };
                        object.push(params);
                    }
                    var param = {
                        staffId:staffId,
                        object:object
                    };
                    prodApplyErganAction.saveProdStaff(param, function(resultData){
                        that.progressOff();
                        if(resultData.flag == true){
                            that.trigger("rolesChanged",true);
                            ngc.success(userManage.ASSIGNING_ROLE_SUCCEEDED);
                            that.btnSub();
                        }else{
                            that.trigger("rolesChanged",false);
                        }
                    });
                }
                else {
                    var param = {
                        staffId:staffId,
                        object:[]
                    };
                    prodApplyErganAction.saveProdStaff(param, function(resultData){
                        that.progressOff();
                        if(resultData.flag == true){
                            that.trigger("rolesChanged",true);
                            ngc.success(userManage.ASSIGNING_ROLE_SUCCEEDED);
                            that.btnSub();
                        }else{
                            that.trigger("rolesChanged",false);
                        }
                    });
                }
            },

            /*saveRoleGroup:function(){
                var that  = this;
                this.progressOn(userManage.PROD_DATA_SUBMIT);
                var staffId = this.options.config.resultData.selId;
                /!**获取权限组维度数据  循环获取权限组和维度数据**!/
                var girdData = this.getView("#exixtRoleGroupGrid").getRowData(null,false);
                if(girdData.length>0){
                    for(var i = 0;i<girdData.length;i++){
                        var param={
                            entity:{id:staffId,
                                typeId:this.options.config.resultData.typeId},id:girdData[i].roleId
                        }

                        var ids = {};
                        ids.roleId = girdData[i].roleId;
                        //ids.roleType = girdData[i].roleType;
                        this.staffIdArray.push(ids);
                        //var resultState = prodApplyErganAction.selectListDimensionForStaffRolegrp(this.options.appName,this.options.config.domainId,param);
                        var resultState = prodApplyErganAction.selectListDimensionForStaffRole(this.options.appName,this.options.config.domainId,param);
                        var resultData = resultState.responseJSON.data;
                        if(resultData){
                            for(var j = 0;j<resultData.length;j++){
                                if(resultData[j].dimensionTypeId != resultData[j].id){
                                    var aiObjDt = {};
                                    aiObjDt.pId = resultData[j].pId;
                                    aiObjDt.dimensionTypeId = resultData[j].dimensionTypeId;
                                    aiObjDt.id = resultData[j].id;
                                    aiObjDt.roleId = girdData[i].roleId;
                                    //aiObjDt.roleType = girdData[i].roleType;
                                    this.roleGroupTreeData.push(aiObjDt);
                                }
                            }
                        }

                    }
                }



                //权限组对应的维度
                var listRoleGeneral  = this.roleGroupTreeData;

                //删除数据中的 用户删除记录
                var delroleDistributionData = this.delroleDistributionData;
                for(var j = 0;j<listRoleGeneral.length;j++){
                    for(var z = 0;z<delroleDistributionData.length;z++){
                        if(listRoleGeneral[j].id == delroleDistributionData[z].id && listRoleGeneral[j].roleId == delroleDistributionData[z].roleId ){
                            listRoleGeneral.splice(j, 1);
                        }
                    }
                }

                /!**根据Id分组**!/
                var map = {},
                    dest = [];
                for(var i = 0; i < listRoleGeneral.length; i++){
                    var ai = listRoleGeneral[i];
                    var aiObj = {};
                    aiObj.pId = ai.pId;
                    aiObj.dimensionTypeId = ai.dimensionTypeId;
                    aiObj.qyid = ai.id;
                    aiObj.roleId = ai.roleId;
                    //aiObj.roleType = ai.roleType;
                    if(!map[aiObj.roleId]){
                        dest.push({
                            roleId: aiObj.roleId,
                            //roleType: aiObj.roleType,
                            listRoleQuyu: [aiObj]
                        });
                        map[aiObj.roleId] = aiObj;
                    }else{
                        for(var j = 0; j < dest.length; j++){
                            var dj = dest[j];
                            if(dj.roleId == aiObj.roleId){
                                dj.listRoleQuyu.push(aiObj);
                                break;
                            }
                        }
                    }
                }



                /!**已分配的权限点  循环获取权限点和维度数据**!/
                var girdData = this.getView("#existDistributionRoleGrid").getRowData(null,false);
                if(girdData.length>0){
                    for(var i = 0;i<girdData.length;i++){
                        var param={
                            entity:{id:staffId,
                                typeId:this.options.config.resultData.typeId},id:girdData[i].roleId
                        }
                        var ids = {};
                        ids.roleId = girdData[i].roleId;
                        //ids.roleType = girdData[i].roleType;
                        this.roleIdArray.push(ids);
                        var resultState = userManageAction.selectListDimensionForStaffRole(this.options.appName,this.options.config.domainId,param);
                        var resultData = resultState.responseJSON.data;
                        if(resultData){
                            for(var j = 0;j<resultData.length;j++){
                                if(resultData[j].dimensionTypeId != resultData[j].id){
                                    var aiObjDt = {};
                                    aiObjDt.pId = resultData[j].pId;
                                    aiObjDt.dimensionTypeId = resultData[j].dimensionTypeId;
                                    aiObjDt.id = resultData[j].id;
                                    aiObjDt.roleId = girdData[i].roleId;
                                    //aiObjDt.roleType = girdData[i].roleType;
                                    this.roleDistributionData.push(aiObjDt);
                                }
                            }
                        }
                    }
                }



                //权限点对应的维度
                var roleDistributionData  = this.roleDistributionData;
                //用户删除记录
                var delroleGroupData = this.delroleGroupData;
                for(var j = 0;j<roleDistributionData.length;j++){
                    for(var z = 0;z<delroleGroupData.length;z++){
                        if(roleDistributionData[j].id == delroleGroupData[z].id && roleDistributionData[j].roleId == delroleGroupData[z].roleId){
                            roleDistributionData.splice(j, 1);
                        }
                    }
                }

                var map2 = {},
                    dest2 = [];
                if(roleDistributionData.length>0){
                    for(var i = 0; i < roleDistributionData.length; i++){
                        var ai = roleDistributionData[i];
                        var aiObjDt = {};
                        aiObjDt.dimensionTypeId = ai.dimensionTypeId;
                        aiObjDt.pId = ai.pId;
                        aiObjDt.qyid = ai.id;
                        aiObjDt.roleId = ai.roleId;
                        //aiObjDt.roleType = ai.roleType;
                        if(!map2[aiObjDt.roleId]){
                            dest2.push({
                                //pId:aiObjDt.pId,
                                roleId: aiObjDt.roleId,
                                //roleType: aiObjDt.roleType,
                                listRoleQuyu: [aiObjDt]
                            });
                            map2[aiObjDt.roleId] = aiObjDt;
                        }else{
                            for(var j = 0; j < dest2.length; j++){
                                var dj = dest2[j];
                                if(dj.roleId == aiObjDt.roleId){
                                    dj.listRoleQuyu.push(aiObjDt);
                                    break;
                                }
                            }
                        }
                    }

                }





                //权限组
                var resultObj = [];
                var roleIdArray = this.staffIdArray;
                for(var i = 0; i < roleIdArray.length; i++){
                    var obj = roleIdArray[i];
                    var num = obj.roleId;
                    var isExist = false;
                    for(var j = 0; j < listRoleGeneral.length; j++){
                        var aj = listRoleGeneral[j];
                        var n = aj.roleId;
                        if(n == num){
                            isExist = true;
                            break;
                        }
                    }
                    if(!isExist){
                        resultObj.push(obj);
                    }
                }


                if(resultObj.length>0){
                    for(var i = 0; i < resultObj.length; i++){
                        dest.push({
                            roleId: resultObj[i].roleId,
                            //roleType: resultObj[i].roleType,
                            listRoleQuyu: []
                        });
                    }
                }


                //权限点
                var result = [];
                var roleIdArray = this.roleIdArray;
                for(var i = 0; i < roleIdArray.length; i++){
                    var obj = roleIdArray[i];
                    var num = obj.roleId;
                    var isExist = false;
                    for(var j = 0; j < roleDistributionData.length; j++){
                        var aj = roleDistributionData[j];
                        var n = aj.roleId;
                        if(n == num){
                            isExist = true;
                            break;
                        }
                    }
                    if(!isExist){
                        result.push(obj);
                    }
                }
                if(result.length>0){
                    for(var i = 0; i < result.length; i++){
                        dest2.push({
                            roleId: result[i].roleId,
                            //roleType: result[i].roleType,
                            listRoleQuyu: []
                        });
                    }
                }


                var jsonObj = {
                    roleDistributionEntity:{id:staffId,
                        typeId:this.options.config.resultData.typeId},
                    listRoleGeneralGrp:dest,
                    listRoleGeneral:dest2
                }
                /!*	        var resultState = userManageAction.saveRoleDistribution("default",jsonObj);
                            var flag = resultState.responseJSON.resultStat;
                            this.progressOff();
                            if(flag == "SUCCESS"){
                                this.trigger("rolesChanged",true);
                                ngc.success("分配权限成功");
                            }else{
                                this.trigger("rolesChanged",false);
                                ngc.error("分配权限失败");
                            }*!/

                prodApplyErganAction.saveProdStaff(jsonObj,function(data){
                    that.progressOff();
                    debugger;
                    if(data.flag == true){
                        that.trigger("rolesChanged",true);
                        ngc.success(userManage.ASSIGNING_PROD_SUCCEEDED);
                    }else{
                        that.trigger("rolesChanged",false);
                        ngc.error(userManage.ASSIGNING_PROD_FAILED);
                    }
                    dest.splice(0,dest.length);
                    dest2.splice(0,dest2.length);
                    resultObj.splice(0,resultObj.length);
                    result.splice(0,result.length);
                    that.cleanArray([that.roleIdArray,that.staffIdArray,that.roleDistributionData,that.roleGroupTreeData,that.delroleGroupData,that.delroleDistributionData,that.checkRepeatGroupData,that.checkRepeatDistributionData]);

                });

            },*/


            /**权限组数据**/
            addRoleGroup:function(){
                ngc.openView({
                    url: "module/frameComponent/user/userManage/views/authGroupManageOpenView",
                    viewOption :{appName : this.options.appName},
                    callback: function(popup, view) {
                    }.bind(this)
                }).then(function(view) {
                    view.on('authGroupManage', function(data) {//监听popView的返回值
                        if(data.length > 0){
                            //已分配的权限组
                            var girdData = this.getView("#exixtRoleGroupGrid").getRowData(null,false);
                            if(girdData.length>0){
                                var result = [];
                                for(var i = 0; i < girdData.length; i++){
                                    var obj = girdData[i];
                                    var num = obj.roleId;
                                    var isExist = false;
                                    for(var j = 0; j < data.length; j++){
                                        var aj = data[j];
                                        var n = aj.roleGroupId;
                                        if(n == num){
                                            isExist = true;
                                            break;
                                        }
                                    }
                                    if(isExist){
                                        result.push(obj);
                                    }
                                }
                                if(result.length>0){
                                    var repeat ="";
                                    for(var j = 0; j < result.length; j++){
                                        repeat+= result[j].roleName+",";
                                    }
                                    ngc.error(repeat+userManage.DUPLICATE_ROLE_GROUP);
                                }else{
                                    this.addRowData(data,"exixtRoleGroupGrid");

                                }
                            }else{
                                this.addRowData(data,"exixtRoleGroupGrid");

                            }
                        }
                    }.bind(this))
                }.bind(this));
            },



            /**权限点数据**/
            adddistributionRole:function(){
                ngc.openView({
                    url: "module/frameComponent/user/userManage/views/authmanageOpenView",
                    viewOption :{config:{domainId:this.options.config.domainId},appName : this.options.appName},
                    callback: function(popup, view) {
                    }.bind(this)
                }).then(function(view) {
                    view.on('authManage', function(data) {//监听popView的返回值
                        if(data.length > 0){
                            //this.addRowData(data,"existDistributionRoleGrid");
                            //已分配的权限组
                            var girdData = this.getView("#existDistributionRoleGrid").getRowData(null,false);
                            if(girdData.length>0){
                                var result = [];
                                for(var i = 0; i < girdData.length; i++){
                                    var obj = girdData[i];
                                    var num = obj.roleId;
                                    var isExist = false;
                                    for(var j = 0; j < data.length; j++){
                                        var aj = data[j];
                                        var n = aj.roleId;
                                        if(n == num){
                                            isExist = true;
                                            break;
                                        }
                                    }
                                    if(isExist){
                                        result.push(obj);
                                    }
                                }
                                if(result.length>0){
                                    var repeat ="";
                                    for(var j = 0; j < result.length; j++){
                                        repeat+= result[j].roleName+",";
                                    }
                                    ngc.error(repeat+userManage.DUPLICATE_ROLE_POINT);
                                }else{
                                    this.addRowData(data,"existDistributionRoleGrid");

                                }
                            }else{
                                this.addRowData(data,"existDistributionRoleGrid");

                            }



                        }
                    }.bind(this))
                }.bind(this));
            },





            addRowData: function (rowData,gridId){
                if(gridId == "existDistributionRoleGrid"){
                    for(var i = 0;i<rowData.length;i++){
                        rowData[i].roleName =  rowData[i].name;
                        rowData[i].roleType = 2;
                    }
                }else{
                    for(var i = 0;i<rowData.length;i++){
                        rowData[i].roleId =  rowData[i].roleGroupId;
                        rowData[i].roleName =  rowData[i].groupName;
                        rowData[i].roleType = 2;
                    }
                }

                this.getView("#"+gridId+"").addRowData("1",rowData);

            },



            /**权限点模块下拉框**/
            _initDistributionSel:function(){
                var that = this;
                var _config = {column:1,editable:true,elements:[{id:'specialityId',type:'select',label:userManage.SPECIALITY,options:this._getSelectListByRoles()}]};
                this.requireView({
                    selector:".searchdistribution",
                    url:"module/component/views/FormView",
                    viewOption :{config:_config}
                }).then(function(){
                    $("#top_node_specialityId").css("width","80%")
                    var form_view = that.getView(".searchdistribution");
                    form_view.on("inputChange", function(event){
                        if(event =="specialityId"){
                            /**资源对象类型属性**/
                            var specialityId = form_view.getValue("specialityId").value;
                            var userId = that.options.config.resultData.selId;
                            var param1 = {
                                id:specialityId,entity:{id:userId,typeId:that.options.config.resultData.typeId}
                            };
                            $.when( userId?prodApplyErganAction.selectRolesListDistributableByStaff(that.options.appName,that.options.config.domainId,param1,$.noop):$.noop
                            ).then(function(rule){
                                /**查询登录用户可分配，且被分配用户不具备的权限点 **/
                                that.distributionRoleGrid(rule.data);

                            });
                        }
                    })


                });
            },

            /**获取与权限点有关的专业**/
            _getSelectListByRoles:function(){
                var roles =  prodApplyErganAction.selectListByRoles(this.options.appName,this.options.config.domainId);
                var rolesData = roles.responseJSON.data,property = [];
                $.each(rolesData, function(i) {
                    property.push({VALUE:this["specialityId"],NAME:this["speciality"]});
                });
                return property;

            },


            /**是否可下放**/
            detrion:function(selNode,girdId){
                var girdData = this.getView("#"+girdId+"").getRowData(null,false);
                for(var i = 0;i<girdData.length;i++){
                    if(selNode == girdData[i].roleId ){
                        girdData[i].roleType = 2;
                    }
                }
                this.getView("#"+girdId+"").reloadData(girdData);
            },

            notdetrion:function(selNode,girdId){
                var girdData = this.getView("#"+girdId+"").getRowData(null,false);
                for(var i = 0;i<girdData.length;i++){
                    if(selNode == girdData[i].roleId ){
                        girdData[i].roleType = 0;
                    }
                }
                this.getView("#"+girdId+"").reloadData(girdData);

            },

            btnSub:function(){
                this.cleanArray([this.roleIdArray,this.staffIdArray,this.roleDistributionData,this.roleGroupTreeData,this.delroleGroupData,this.delroleDistributionData]);
                this.popup.close();
            },

            cleanArray:function(array){
                if(array !=null){
                    for(var i = 0;i<array.length;i++){
                        if(array[i].splice == undefined){
                            array.splice(0,array[i].length);
                        }else{
                            array[i].splice(0,array[i].length);
                        }
                    }(this);
                }
            },

            contains:function(arr, obj) {
                var i = arr.length;
                while (i--) {
                    if (arr[i].id === obj) {
                        return true;
                    }
                }
                return false;
            },


            //判断移过去的 左边资源树是否有重复数据
            containsTreeNode:function(nodes,id,dimensionTypeId){
                if(nodes.length >0){
                    for(var i = 0;i<nodes.length;i++){
                        if(nodes[i].id == id && nodes[i].dimensionTypeId == dimensionTypeId ){
                            return false;
                        }else if(nodes[i].children !=undefined){
                            if(nodes[i].id != nodes[i].dimensionTypeId){
                                var _node = nodes[i].children;
                                for(var j = 0;j<_node.length;j++){
                                    if(_node[j].id == id && _node[j].dimensionTypeId == dimensionTypeId ){
                                        return false;
                                    }
                                }
                            }

                        }
                    }
                }
                return true;
            },

            containsByDtId:function(arr, obj) {
                var i = arr.length;
                while (i--) {
                    if (arr[i].id ==  obj ) {
                        return arr[i];
                    }
                }
                return null;
            },

            containsByRoleId:function(arr, obj, nodeId) {
                var i = arr.length;
                while (i--) {
                    if(obj == undefined){
                        if (arr[i].id == nodeId ) {
                            return true;
                        }
                    }else{
                        if (arr[i].roleId === obj && arr[i].id == nodeId ) {
                            return true;
                        }
                    }

                }
                return false;
            },

            /***判断移动的数据中 是否包含重复数据***/
            containsByNodeId:function(arr,roleId, nodeId) {
                var i = arr.length;
                while (i--) {
                    if (arr[i].roleId === roleId && arr[i].id == nodeId ) {
                        return false;
                    }
                }
                return true;
            },

            /**根据子维度查顶级节点的父维度**/
            getParentNode:function (dimensionTypeId){
                var dimensionType= this.dimensionTypeArray;
                for(var i=0;i<dimensionType.length;i++ ){
                    if(dimensionTypeId == dimensionType[i].id){
                        return dimensionType[i];
                    }
                }
            },



            delArrayByRoleId:function(Array,_Array,nodeId,roleId){
                for(var j = 0;j<Array.length;j++){
                    if(Array[j].roleId == roleId && Array[j].id == nodeId){
                        Array.splice(j, 1);
                    }

                }

                for(var i = 0;i<_Array.length;i++){
                    if(_Array[i].roleId == roleId){
                        _Array.splice(i, 1);
                    }

                }
            },


            restulArrayByRoleId:function(Array,roleId){
                this.newArray=[];
                for(var i=0;i<Array.length;i++ ){
                    if(Array[i].roleId == roleId){
                        this.newArray.push(Array[i]);
                    }
                }
                return this.newArray;
            }






        });
        return popView;
    });
