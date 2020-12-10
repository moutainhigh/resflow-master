/**
 * 多选择树JS
 */
define(['module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/operOrderAction',
    'text!module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/templates/userView.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n'
], function (operOrderAction, userView, i18n) {
    var IDMark_Switch = "_switch",
        IDMark_Icon = "_ico",
        IDMark_Span = "_span",
        IDMark_Input = "_input",
        IDMark_Check = "_check",
        IDMark_Edit = "_edit",
        IDMark_Remove = "_remove",
        IDMark_Ul = "_ul",
        IDMark_A = "_a";
    var meT;
    var isExist = {};//部门下是否存在人员
    var isOrgSear = '0'; //组织树、搜索框、常用联系人
    var currentAreaIdT;
    var currentOrgIdT;
    var currentUserIdT;

    return fish.View.extend({
        template: fish.compile(userView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #submitBtn': 'submit',
            'click #searchOrgPerBtn': 'querysearchOrgPerGrid',
        },
        initialize: function () {
            this.render();
        },
        //渲染页面
        render: function () {
            this.$el.html(this.template(this.i18nData));
        },
        //初始化fish组件
        afterRender: function () {
            meT = this;
            isOrgSear = '1'; //默认搜索
            isExist = {};//部门下是否存在人员
            currentAreaIdT = this.options.currentAreaId;
            currentOrgIdT = this.options.currentOrgId;
            currentUserIdT = this.options.currentUserId;
            meT.initFishData(); //初始化组织树
            meT.initnewOrgPer(); //初始化组织树、搜索、常用联系人单选事件
            meT.initorgPerDeType(); //初始化搜索下的派发类型、值以及改变事件
            meT.initsearchContacts(); //初始化常用联系人
            meT.searchInputPullDow();//初始化搜索(人员、岗位、部门)列表
            if (this.options.title) {
                $("#staff-modalTitle").html(this.options.title);
            }


        },
        initsearchContacts: function () { //初始化常用联系人
            this.requireView({
                selector: "#searchContactsView",
                url: "module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/freContactsView",
                viewOption: {
                    currentUserId: currentUserIdT,
                    currentAreaId: currentAreaIdT
                },
                callback: function (view) {
                }
            }).then(function (view) {

            });

        },
        initFishData: function () {
            var options = {
                view: {
                    txtSelectedEnable: true,
                },
                check: {
                    enable: true,
                    chkStyle: "checkbox",
                    chkboxType: {"Y": "ps", "N": "ps"}
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    // 用于捕获节点被展开的事件回调函数
                    onExpand: function onExpand(event, treeNode) {
                        if (!treeNode.isParent) {
                            return;
                        }
                        var tid = treeNode.tId;
                        var idOrg = treeNode.id;
                        isExist[idOrg] = false;
                        if (treeNode.children != undefined && treeNode.children != null) {
                            if (treeNode.children.length == 0) {
                                operOrderAction.getStaffInfoDeptListUnit(treeNode, function (data) {
                                    var nodes = $("#transferObjTree").tree('getNodeByTId', tid);
                                    $("#transferObjTree").tree('removeChildNodes', nodes);//先清空子节点
                                    if (data != undefined && data != null && data.length != 0) {
                                        for (var i = 0; i < data.length; i++) {
                                            var datum = data[i];
                                            if ("260000003" == datum.objType) {
                                                isExist[idOrg] = true;
                                                break;
                                            }
                                        }
                                        $("#transferObjTree").tree('addNodes', nodes, data);
                                    } else {
                                        fish.toast("warn", "当前部门下无其它部门或人员");
                                        return;
                                    }
                                });
                            }
                        } else {
                            operOrderAction.getStaffInfoDeptListUnit(treeNode, function (data) {
                                var nodes = $("#transferObjTree").tree('getNodeByTId', tid);
                                $("#transferObjTree").tree('removeChildNodes', nodes);//先清空子节点
                                if (data != undefined && data != null && data.length != 0) {
                                    for (var i = 0; i < data.length; i++) {
                                        var datum = data[i];
                                        if ("260000003" == datum.objType) {
                                            isExist[idOrg] = true;
                                            break;
                                        }
                                    }
                                    $("#transferObjTree").tree('addNodes', nodes, data);
                                } else {
                                    fish.toast("warn", "当前部门下无其它部门或人员");
                                    return;
                                }
                            });
                        }
                    }
                }
            };
            var params = {};
            params.flag = this.options.flag;
            params.currentAreaId = currentAreaIdT;
            params.currentOrgId = currentOrgIdT;
            operOrderAction.qryDepartParent(params, function (data) {
                options.fNodes = data;
                if (data != undefined && data != null && data.length != 0) {
                    for (var i = 0; i < data.length; i++) {
                        var datum = data[i];
                        var idP = datum.id;
                        isExist[idP] = false;
                    }
                }
                $("#transferObjTree").tree(options);
            });
        },
        initnewOrgPer: function () { //初始化组织树、搜索、常用联系人单选事件
            //组织树、搜索、常用联系人
            $("input[name = 'newOrgPerRadio']").bind('click', function () {
                var newResourceFlag = $("input[name='newOrgPerRadio']:checked").val();
                if ('0' == newResourceFlag) { //组织树
                    $("#transferTreeDiv").show();
                    $("#searchInput").hide();
                    $("#searchContacts").hide();
                    isOrgSear = '0';
                    $("#searchOrgPer").val("");
                    $("#searchOrgPerDepart").val("");
                    $("#searchOrgPerPost").val("");
                    $("#searchId").val("");
                    $("#searchName").val("");
                    $("#searchObjType").val("");
                    $("#searchOrgPerDivDis").hide();
                    $("#searchOrgPerDePartDivDis").hide();
                    $("#searchOrgPerDePostDivDis").hide();
                    $("#waitingListDivDis").hide();
                } else if ('1' == newResourceFlag) { //搜索框
                    $("#searchInput").show();
                    $("#transferTreeDiv").hide();
                    $("#searchContacts").hide();
                    $("#waitingListDivDis").show();
                    isOrgSear = '1';
                    $("#searchId").val("");
                    $("#searchName").val("");
                    $("#searchObjType").val("");
                    var orgPerDeTypeVal = $("#orgPerDeType").combobox('value');
                    meT.searchHide(orgPerDeTypeVal);
                } else if ('2' == newResourceFlag) { //常用联系人
                    $("#transferTreeDiv").hide();
                    $("#searchInput").hide();
                    $("#searchContacts").show();
                    isOrgSear = '1';
                    $("#searchOrgPer").val("");
                    $("#searchOrgPerDepart").val("");
                    $("#searchOrgPerPost").val("");
                    $("#searchId").val("");
                    $("#searchName").val("");
                    $("#searchObjType").val("");
                    $("#searchOrgPerDivDis").hide();
                    $("#searchOrgPerDePartDivDis").hide();
                    $("#searchOrgPerDePostDivDis").hide();
                    $("#waitingListDivDis").show();
                    meT.getView('#searchContactsView').queryContacts();
                    $(window).trigger("resize");
                }

            });

        },
        initorgPerDeType: function () { //初始化搜索下的派发类型、值以及改变事件
            $('#orgPerDeType').combobox({
                placeholder: '--请选择派发类型--',
                dataTextField: 'name',
                dataValueField: 'value',
                dataSource: [
                    {name: '人员', value: '260000003'},
                    {name: '岗位', value: '260000002'},
                    {name: '部门', value: '260000001'}
                ]
            });
            //默认赋值为人员
            $("#orgPerDeType").combobox('value', '260000003');
            //派发类型改变
            $('#orgPerDeType').on('combobox:change', function (e) {
                $("#searchOrgPer").val("");
                $("#searchId").val("");
                $("#searchName").val("");
                $("#searchObjType").val("");
                var orgPerDeTypeVal = $("#orgPerDeType").combobox('value');
                meT.searchHide(orgPerDeTypeVal);
            });
            //默认搜索人员
            meT.searchHide("260000003");
        },
        searchHide: function (orgPerDeTypeVal) {
            if ("260000001" == orgPerDeTypeVal) { //部门
                $("#searchOrgPerDivDis").hide();
                $("#searchOrgPerDePartDivDis").show();
                $("#searchOrgPerDePostDivDis").hide();
            } else if ("260000003" == orgPerDeTypeVal) { //人员
                $("#searchOrgPerDivDis").show();
                $("#searchOrgPerDePartDivDis").hide();
                $("#searchOrgPerDePostDivDis").hide();
            } else if ("260000002" == orgPerDeTypeVal) { //岗位
                $("#searchOrgPerDivDis").hide();
                $("#searchOrgPerDePartDivDis").hide();
                $("#searchOrgPerDePostDivDis").show();
            }
            meT.querysearchOrgPerGrid();
        },
        searchInputPullDow: function () { //初始化搜索(人员、岗位、部门)列表
            meT.initsearchOrgPerGrid();
            meT.initsearchOrgPerDepartGrid();
            meT.initsearchOrgPerPostGrid();
            meT.initwaitingListGrid();
            meT.querysearchOrgPerGrid();
            $("#searchOrgPer").keyup(function (event) {
                if (event.keyCode == 13) {
                    meT.querysearchOrgPerGrid();
                }
            });

        },
        initwaitingListGrid: function () {//初始化候选名单表格
            var me = this;
            $("#waitingListGrid").grid({
                colModel: [
                    //默认展示字段
                    {name: 'ID', label: '派发人员Id', hidden: true},
                    {name: 'ORG_ID', label: '派发人员归属部门Id', hidden: true},
                    {name: 'NAME', label: '派发人员', width: 200, align: 'left'},
                    {name: 'ORG_NAME', label: '归属部门', width: 400, align: 'left'},
                    {
                        name: 'ACTION', label: '操作', width: 80, formatter: 'actions',
                        formatoptions: {
                            editbutton: false,
                            delbutton: true
                        }
                    }
                ],
                height: 'auto',
                autoResizable: true,
            });

        },
        initsearchOrgPerGrid: function () { //初始化人员
            var _this = this;
            var querysearchOrgPerGrid = $.proxy(this.querysearchOrgPerGrid, this);
            $perGrid = $("#searchOrgPerGrid").grid({
                colModel: [
                    //默认展示字段
                    {name: 'ID', label: '派发人员Id', hidden: true, key: true},
                    {name: 'ORG_ID', label: '派发人员归属部门Id', hidden: true},
                    {name: 'USER_REAL_NAME', label: '派发人员', hidden: true},
                    {name: 'OBJTYPE', label: '派单类型', hidden: true},
                    {name: 'NAME', label: '派发人员', width: 200, align: 'left'},
                    {name: 'ORG_NAME', label: '归属部门', width: 470, align: 'left'}
                ],
                autowidth: true,
                height: 170,
                autoResizable: true,
                pageData: querysearchOrgPerGrid,
                onDblClickRow: function (e, rowid, iRow, iCol) {
                    var rowData = $("#searchOrgPerGrid").grid('getRowData', rowid);
                    $("#waitingListGrid").grid("addRowData", rowData);
                    _this.refreshWaitingListGrid();
                    $(window).trigger("resize");
                }

            });

        },
        querysearchOrgPerGrid: function (page, rowNum, sortname, sortorder) { //岗位、人员、部门查询
            rowNum = 1000;
            page = 1;
            //登陆人信息
            var paramsMap = new Object();
            var searchOrgPerName = $("#searchOrgPer").val();//人员、岗位、部门搜索内容
            var orgPerDeTypeVal = $("#orgPerDeType").combobox('value');
            if (searchOrgPerName == "" || searchOrgPerName == undefined) {
                rowNum = 500;
                page = 1;
            }
            //分页信息
            paramsMap.pageIndex = page + '';
            paramsMap.pageSize = rowNum + '';
            paramsMap.searchOrgPerName = searchOrgPerName;
            paramsMap.currentUserId = currentUserIdT;
            paramsMap.currentAreaId = currentAreaIdT;
            paramsMap.orgPerDeTypeVal = orgPerDeTypeVal;
            paramsMap.currentOrgId = currentOrgIdT;
            //调用后台方法
            if ("260000001" == orgPerDeTypeVal) { //部门
                $("#searchOrgPerDepartGrid").blockUI({message: '加载中'}).data('blockui-content', true);
            } else if ("260000003" == orgPerDeTypeVal) { //人员
                $("#searchOrgPerGrid").blockUI({message: '加载中'}).data('blockui-content', true);
            } else if ("260000002" == orgPerDeTypeVal) { //岗位
                $("#searchOrgPerPostGrid").blockUI({message: '加载中'}).data('blockui-content', true);
            }
            operOrderAction.qrySearchOrgPerDepPullDown(paramsMap, function (data) {
                if (data.message == "success") {
                    if ("260000001" == orgPerDeTypeVal) { //部门
                        $("#searchOrgPerDepartGrid").grid("reloadData", data.data);
                        $("#searchOrgPerDepartGrid").unblockUI({message: '加载中'}).data('blockui-content', false);
                    } else if ("260000003" == orgPerDeTypeVal) { //人员
                        $("#searchOrgPerGrid").grid("reloadData", data.data);
                        $("#searchOrgPerGrid").unblockUI({message: '加载中'}).data('blockui-content', false);
                    } else if ("260000002" == orgPerDeTypeVal) { //岗位
                        $("#searchOrgPerPostGrid").grid("reloadData", data.data);
                        $("#searchOrgPerPostGrid").unblockUI({message: '加载中'}).data('blockui-content', false);
                    }
                    $(window).trigger("resize");
                    //刪除已选择列
                    meT.delSelectRow();
                } else {
                    fish.toast("warn", "获取数据失败");
                    if ("260000001" == orgPerDeTypeVal) { //部门
                        $("#searchOrgPerDepartGrid").unblockUI({message: '加载中'}).data('blockui-content', false);
                    } else if ("260000003" == orgPerDeTypeVal) { //人员
                        $("#searchOrgPerGrid").unblockUI({message: '加载中'}).data('blockui-content', false);
                    } else if ("260000002" == orgPerDeTypeVal) { //岗位
                        $("#searchOrgPerPostGrid").unblockUI({message: '加载中'}).data('blockui-content', false);
                    }
                }
            });

        },
        initsearchOrgPerDepartGrid: function () { //初始化部门
            var querysearchOrgPerDepartGrid = $.proxy(this.querysearchOrgPerGrid, this);
            $perGrid = $("#searchOrgPerDepartGrid").grid({
                colModel: [
                    //默认展示字段
                    {name: 'ID', label: '部门Id', hidden: true},
                    {name: 'PARENTORGID', label: '归属部门Id', hidden: true},
                    {name: 'OBJTYPE', label: '派单类型', hidden: true},
                    {name: 'NAME', label: '派发部门', width: 310, align: 'left'},
                    {name: 'PARENTORGNAME', label: '归属部门', width: 330, align: 'left'}
                ],
                autowidth: true,
                height: 170,
                autoResizable: true,
                pageData: querysearchOrgPerDepartGrid,
                subGrid: true,
                subGridOptions: {
                    reloadOnExpand: false
                },
                subGridRowExpanded: function (e, subGridId, parentRowId) {
                    meT.initsearchOrgPerDepartGridSub(subGridId, parentRowId);
                }
            });

        },
        initsearchOrgPerPostGrid: function () { //初始化岗位
            var querysearchOrgPerDepartGrid = $.proxy(this.querysearchOrgPerGrid, this);
            $perGrid = $("#searchOrgPerPostGrid").grid({
                colModel: [
                    //默认展示字段
                    {name: 'ID', label: '派发岗位Id', hidden: true},
                    {name: 'OBJTYPE', label: '派单类型', hidden: true},
                    {name: 'NAME', label: '派发岗位', width: 640, align: 'left'}
                ],
                autowidth: true,
                height: 170,
                autoResizable: true,
                gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                multiselect: false,
                shrinkToFit: false,
                pageData: querysearchOrgPerDepartGrid,
                subGrid: true,
                subGridOptions: {
                    reloadOnExpand: false
                },
                subGridRowExpanded: function (e, subGridId, parentRowId) {
                    meT.initsearchOrgPerPostGridSub(subGridId, parentRowId);
                }

            });

        },
        initsearchOrgPerPostGridSub: function (subGridId, parentRowId) { //岗位下的人员
            var _this = this;
            //获取父节点row data
            var dataSubP = $("#searchOrgPerPostGrid").grid('getRowData', parentRowId);
            var subgrid_table_id = subGridId + '_t';
            $("#" + subGridId).html("<table id='" + subgrid_table_id + "'></table>");
            var $subGrid = $("#" + subgrid_table_id).grid({
                colModel: [
                    //默认展示字段
                    {name: 'ID', label: '派发人员Id', width: 180, hidden: true},
                    {name: 'NAME', label: '派发人员', width: 150, align: 'left'},
                    {name: 'USER_REAL_NAME', label: '派发人员', width: 200, align: 'left',hidden: true},
                    {name: 'ORG_ID', label: '部门ID', width: 560, align: 'left', hidden: true},
                    {name: 'ORG_NAME', label: '部门名称', width: 420, align: 'left'}
                ],
                autowidth: true,
                rowNum: 5000,
                height: 'auto',
                curPageSort: false,
                multiselect: false,
                pager: false,
                gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                shrinkToFit: false,
                autoResizable: true,
                cached: true, //把用户自定义的列展示设置缓存在本地
                onDblClickRow: function (e, rowid, iRow, iCol) {
                    var rowData = $("#" + subgrid_table_id).grid('getRowData', rowid);
                    $("#waitingListGrid").grid("addRowData", rowData);
                    _this.refreshWaitingListGrid();
                    $(window).trigger("resize");
                }
            });
            var collQueryObj = new Object();
            collQueryObj.postId = dataSubP.ID;
            collQueryObj.objType = dataSubP.OBJTYPE
            collQueryObj.currentUserId = currentUserIdT;
            collQueryObj.currentAreaId = currentAreaIdT;
            operOrderAction.qrySearchOrgPerDepPullDownSub(collQueryObj, function (data) {
                if (data.message == "success") {
                    $("#" + subgrid_table_id).grid("reloadData", data.data);
                } else {
                    fish.toast("warn", "获取数据失败");
                }
            });

        },
        initsearchOrgPerDepartGridSub: function (subGridId, parentRowId) {  //部门下的人员
            //获取父节点row data
            var _this = this;
            var dataSubP = $("#searchOrgPerDepartGrid").grid('getRowData', parentRowId);
            var subgrid_table_id = subGridId + '_t';
            $("#" + subGridId).html("<table id='" + subgrid_table_id + "'></table>");
            var $subGrid = $("#" + subgrid_table_id).grid({
                colModel: [
                    //默认展示字段
                    {name: 'ID', label: '派发人员Id', width: 180, hidden: true},
                    {name: 'NAME', label: '派发人员', width: 560, align: 'left'},
                    {name: 'USER_REAL_NAME', label: '派发人员', width: 560, align: 'left', hidden: true},
                    {name: 'ORG_ID', label: '部门ID', width: 560, align: 'left', hidden: true},
                    {name: 'ORG_NAME', label: '部门名称', width: 360, align: 'left', hidden: true}
                ],
                autowidth: true,
                rowNum: 5000,
                height: 'auto',
                curPageSort: false,
                multiselect: false,
                pager: false,
                gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                shrinkToFit: false,
                autoResizable: true,
                cached: true, //把用户自定义的列展示设置缓存在本地
                onDblClickRow: function (e, rowid, iRow, iCol) {
                    var rowData = $("#" + subgrid_table_id).grid('getRowData', rowid);
                    $("#waitingListGrid").grid("addRowData", rowData);
                    _this.refreshWaitingListGrid();
                    $(window).trigger("resize");
                }

            });
            var collQueryObj = new Object();
            collQueryObj.deptId = dataSubP.ID;
            collQueryObj.objType = dataSubP.OBJTYPE
            collQueryObj.currentUserId = currentUserIdT;
            collQueryObj.currentAreaId = currentAreaIdT;
            operOrderAction.qrySearchOrgPerDepPullDownSub(collQueryObj, function (data) {
                if (data.message == "success") {
                    $("#" + subgrid_table_id).grid("reloadData", data.data);
                } else {
                    fish.toast("warn", "获取数据失败");
                }
            });

        },
        searchInput: function (orgPerDeTypeVal) {//搜索框自动查询下拉树数据
            if ("260000001" == orgPerDeTypeVal) { //部门
                if (!isSearchOrgPerDepart) {
                    var searchObj = new Object();
                    searchObj.searchVal = "";
                    searchObj.orgPerDeType = orgPerDeTypeVal;
                    searchObj.currentAreaId = currentAreaIdT;
                    operOrderAction.qrySearchOrgPerDepart(searchObj, function (data) {
                        var options = {
                            placeholder: "请选择派发部门",
                            dropdownWidth: 350,
                            data: {
                                simpleData: {
                                    enable: true
                                }
                            },
                            searchFilter: true,
                            callback: {
                                onClick: meT.onwDeClick
                            },
                            fNodes: data
                        };
                        $('#searchOrgPerDepart').combotree(options);
                        isSearchOrgPerDepart = true;
                    });

                }

            } else if ("260000003" == orgPerDeTypeVal) { //人员
                if (!isSearchOrgPer) {
                    var searchObj = new Object();
                    searchObj.searchVal = "";
                    searchObj.orgPerDeType = orgPerDeTypeVal;
                    searchObj.currentAreaId = currentAreaIdT;
                    operOrderAction.qrySearchOrgPerDepart(searchObj, function (data) {
                        var options = {
                            placeholder: "请选择派发人员",
                            dropdownWidth: 350,
                            data: {
                                simpleData: {
                                    enable: true
                                }
                            },
                            searchFilter: true,
                            callback: {
                                onClick: meT.onwClick
                            },
                            fNodes: data
                        };
                        $('#searchOrgPer').combotree(options);
                        isSearchOrgPer = true;
                    });

                }
            } else if ("260000002" == orgPerDeTypeVal) { //岗位
                if (!isSearchOrgPerPost) {
                    var searchObj = new Object();
                    searchObj.searchVal = "";
                    searchObj.orgPerDeType = orgPerDeTypeVal;
                    searchObj.currentAreaId = currentAreaIdT;
                    operOrderAction.qrySearchOrgPerDepart(searchObj, function (data) {
                        var options = {
                            placeholder: "请选择派发岗位",
                            dropdownWidth: 350,
                            data: {
                                simpleData: {
                                    enable: true
                                }
                            },
                            searchFilter: true,
                            callback: {
                                onClick: meT.onwPostClick
                            },
                            fNodes: data
                        };
                        $('#searchOrgPerPost').combotree(options);
                        isSearchOrgPerPost = true;
                    });
                }

            }
        },
        onwClick: function (e, treeNode) {
            $("#searchId").val(treeNode.id);
            $("#searchName").val(treeNode.name);
            $("#searchObjType").val(treeNode.objType);
        },
        onwDeClick: function (e, treeNode) {
            $("#searchId").val(treeNode.id);
            $("#searchName").val(treeNode.name);
            $("#searchObjType").val(treeNode.objType);
        },
        onwPostClick: function (e, treeNode) {
            $("#searchId").val(treeNode.id);
            $("#searchName").val(treeNode.name);
            $("#searchObjType").val(treeNode.objType);
        },
        freReturnSelect: function (id, name, objtype) {
            $("#searchId").val(id);
            $("#searchName").val(name);
            $("#searchObjType").val(objtype);
        },
        freReturnDblClick: function (ID, USER_REAL_NAME, ORG_ID, ORG_NAME) {
            var obj = {};
            obj.ID = ID;
            obj.USER_REAL_NAME = USER_REAL_NAME;
            obj.ORG_ID = ORG_ID;
            obj.ORG_NAME = ORG_NAME;
            $("#waitingListGrid").grid("addRowData", obj);
            this.refreshWaitingListGrid();
            $(window).trigger("resize");
        },
        // 数组去重
        uniq: function (arr) {
            var hash = [];
            var newArr = [];
            for (var i = 0; i < arr.length; i++) {
                var ID = arr[i].ID;
                if (hash.indexOf(ID) == -1) {
                    hash.push(ID);
                    newArr.push(arr[i]);
                }
            }
            return newArr;
        },
        refreshWaitingListGrid: function () {
            var data = $("#waitingListGrid").grid("getRowData");
            if (data == undefined || data == null || data.length == 0) {
                return;
            }
            data = this.uniq(data);
            $("#waitingListGrid").grid("reloadData", data);
        },
        submit: function () {
            if ("1" == isOrgSear) {
                var data = $("#waitingListGrid").grid("getRowData");
                if (data == undefined || data == null || data.length == 0) {
                    fish.info("候选人员表暂无数据！");
                    return;
                }
                data = this.uniq(data);
                var searchArr = new Array();
                for (var i = 0; i < data.length; i++) {
                    var node = data[i];
                    var obj = {
                        id: node.ID,
                        name: node.NAME,
                        section: node.ORG_NAME
                    };
                    searchArr.push(obj);
                }
                meT.popup.close(searchArr);
            } else if ("0" == isOrgSear) {
                //获取选中的节点
                var nodes = $("#transferObjTree").tree("getCheckedNodes");
                var new_nodes = new Array();
                for (var i = 0; i < nodes.length; i++) {
                    var node = nodes[i];
                    if (node.objType == '260000003') {
                        node.section = node.getParentNode().name;
                        var obj = {
                            id: node.id,
                            name: node.name,
                            section: node.section
                        };
                        new_nodes.push(obj);
                    }
                }
                if (new_nodes.length <= 0) {
                    fish.info("请选择一个或多个人员！");
                    return;
                }
                meT.popup.close(new_nodes);

            } else if ("2" == isOrgSear) {
                var searchId = $("#searchId").val();
                var searchName = $("#searchName").val();
                var searchObjType = $("#searchObjType").val();
                if (searchId == undefined
                    || searchId == null
                    || searchId == "") {
                    fish.info("请选择一个常用联系人！");
                    return;
                }
                var searchArr = new Array();
                var searchObj = new Object();
                searchObj.id = searchId;
                searchObj.name = searchName;
                searchObj.objType = searchObjType;
                searchArr.push(searchObj);
                meT.popup.close(searchArr);

            }
            meT.popup.close(nodes);
        },

        //删除已选择列
        delSelectRow: function () {
            var ids = this.options.ids;
            console.log("ids",ids);
            if (typeof (ids) != undefined && ids && ids.length > 0) {
                $(ids).each(function () {
                    console.log("this",this);
                    console.log($("#searchOrgPerGrid").grid("delRowData", this));
                });
            }
        },

    });
})