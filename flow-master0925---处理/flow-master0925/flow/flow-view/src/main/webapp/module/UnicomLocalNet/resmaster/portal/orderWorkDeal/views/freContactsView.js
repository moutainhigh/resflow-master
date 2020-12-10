/**
 * 常用联系人查询、删除、新增页面
 */
define(['module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/operOrderAction',
    'text!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/templates/freContactsView.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n'
], function(operOrderAction,freContactsView,i18n) {
    var currentUserIdTcon;
    var currentAreaIdTcon;
    var localContactsNum = 5;
    var localContactsPage = 1;
    var meTcon;

    return fish.View.extend({
        template: fish.compile(freContactsView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #queryContactOrder': 'queryContactsFun',
            'click #addContactOrder': 'addContactOrderFun',
            "click #deleteFormAttr":"_deleteFormAttr",
        },
        initialize: function() {
            this.render();
        },
        //渲染页面
        render: function() {
            this.$el.html(this.template(this.i18nData));
        },
        //初始化fish组件
        afterRender: function() {
            meTcon = this;
            currentUserIdTcon = this.options.currentUserId; //当前用户Id
            currentAreaIdTcon = this.options.currentAreaId; //区域Id
            this.initContactsGrid();
            this.queryContactsFun();
            $("#searchContactsName").keyup(function (event) {
                if(event.keyCode == 13){
                    // debugger
                    meTcon.queryContacts();
                }
            });

        },
        initContactsGrid: function() {
            var me = this;
            var queryContacts = $.proxy(this.queryContacts,this); //函数作用域改变
            $conGrid =$("#freContactGrid").grid({
                colModel: [
                    //默认展示字段
                    {name: 'ID', label: '联系人Id', width: 180, hidden: true},
                    {name: 'ORG_ID', label: '部门Id', width: 180,hidden: true},
                    {name: 'OBJTYPE', label: '派单类型', width: 180,hidden: true},
                    {name: 'PID', label: '归属联系人Id', width: 180,hidden: true},
                    {name: 'USER_REAL_NAME', label: '联系人', width: 170, align: 'left'},
                    {name: 'ORG_NAME', label: '部门名称', width: 310, align: 'left'},
                    {width: 160,name: '操作',
                        formatter: function(cellval, opts, rwdat, _act) {
                            return '<div class="btn-group">' +
                                '<button type="button" class="btn btn-link js-delete " title="删除" id="deleteFormAttr"> <span class="glyphicon glyphicon glyphicon-remove" style="font-size:15px;color: #eb4a4b"></span></button>' +
                                '</div>'
                        },
                    }

                ],
                // datatype: "json",
                autowidth: true,
                height: 220,
                rowNum: 10000,
                curPageSort: true,
                rowList: [5,10,15,20,50],
                pager: false,
                recordtext:"{0}-{1} 共{2}条",
                pgtext: " 第{0}页/共{1}页",
                rowtext: "每页{0}条",
                gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                multiselect: false,
                shrinkToFit: false,
                autoResizable: true,
                showColumnsFeature: false, //允许用户自定义列展示设置
                cached: true, //把用户自定义的列展示设置缓存在本地
                pageData: queryContacts,
                gridComplete: function () {
                    $('.gotext').html("<span>跳转至<input class=\"ui-pagination-input\"></span>");
                },
                onCellSelect:function(e, rowid, iCol, cellcontent){
                    if(iCol != 6){
                        var portalData = $("#freContactGrid").grid('getRowData', rowid);
                        meTcon.parentView.freReturnSelect(portalData.ID,portalData.USER_REAL_NAME,portalData.OBJTYPE);
                    }
                },
                onDblClickRow: function (e, rowid, iRow, iCol) {
                    var rowData = $("#freContactGrid").grid('getRowData', rowid);
                    var allRowData = $("#waitingListGrid").grid("getRowData");
                    if (allRowData.length > 0) {
                        //判断候选人列表是否已存在将要插入的人员
                        var flag = true;
                        for (var i = 0; i < allRowData.length; i++){
                            if (rowData.ID == allRowData[i].ID){
                                flag = false;
                                break;
                            }
                        }
                        if (flag) {
                            $("#waitingListGrid").grid("addRowData", rowData);

                        }else{
                            fish.info("候选人列表已存在该联系人！");
                        }
                    }else{
                        $("#waitingListGrid").grid("addRowData", rowData);
                    }
                }

            });
            this.resize();

        },
        //查询
        queryContactsFun: function(){
            this.queryContacts();
        },
        //新增
        addContactOrderFun: function(){
            fish.popupView({
                url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/freContactsSelectView',
                width: 600,
                height: 460,
                canClose: true,
                autoResizable: true,
                draggable:true,
                resizable:true,
                viewOption:{
                    currentUserId: currentUserIdTcon,
                    currentAreaId: currentAreaIdTcon
                },
                callback:function(popup,view){
                    popup.result.then(function (ret) {
                        meTcon.queryContacts();
                        // if(ret && ret.isSuccess){
                        //     alert(ret.isSuccess);
                        // }
                    },function (ret) {
                    });
                }
            });

        },
        _deleteFormAttr: function(){ //删除当前联系人
            fish.confirm('确认是否删除当前联系人').result.then(function() {
                // debugger
                var selrow = $("#freContactGrid").grid("getSelection");
                var paramsMap = new Object();
                paramsMap.user_id = selrow.ID;
                paramsMap.parent_user_id = selrow.PID;
                    operOrderAction.deleteSearchContacts(paramsMap,function (data) {
                        // debugger
                        if(data.message == 'fail'){
                            fish.toast("warn", "删除常用联系人失败:"+data.errorMessage);
                            return;
                        }else{
                            fish.toast("warn", "删除常用联系人成功");
                            meTcon.queryContacts();
                        }

                    });

            });

        },
        //查询常用联系人
        queryContacts: function(page, rowNum, sortname, sortorder) {
            rowNum = (rowNum!=''&&rowNum!=undefined)?rowNum:localContactsNum;
            page = (page!=''&&page!=undefined)?page:localContactsPage;
            //登陆人信息
            var paramsMap = new Object();
            //分页信息
            paramsMap.pageIndex = page+'';
            paramsMap.pageSize = rowNum+'';

            var searchContactsName = $("#searchContactsName").val();//常用联系人
            paramsMap.searchContactsName = searchContactsName;
            paramsMap.currentUserId = currentUserIdTcon;
            // debugger;
            //调用后台方法
            $("#freContactGrid").blockUI({message: '加载中'}).data('blockui-content', true);
            // debugger
            operOrderAction.qrySearchContacts(paramsMap,function (data) {
                if (data.message == "success") {
                    // debugger;
                    // var gridData = {
                    //     "rows": data.data,
                    //     "page": page,
                    //     "records": data.contactCount,
                    //     "rowNum": data.contactCount,
                    //     "total":data.pageCount
                    // };
                    $("#freContactGrid").grid("reloadData", data.data);
                    $("#freContactGrid").unblockUI().data('blockui-content', false);
                }else{
                    fish.toast("warn", "获取数据失败");
                    $("#freContactGrid").unblockUI().data('blockui-content', false);
                }
            });

        },
        submit:function () {
                var searchArr = new Array();
                var searchObj = new Object();
                searchArr.push(searchObj);
                meTcon.popup.close(searchArr);

        },
        //浏览器窗口大小改变事件
        resize: function() {
            // $("#freContactGrid").grid("setGridHeight", 327);
        }

    });
})