define(["text!module/UnicomLocalNet/resmaster/portal/messageList/templates/messageListView.html",
        'i18n!module/UnicomLocalNet/resmaster/portal/messageList/i18n/messageListView.i18n',
        'module/UnicomLocalNet/resmaster/portal/messageList/action/messageListViewAction',
        "css!module/UnicomLocalNet/resmaster/portal/messageList/css/messageListView.css"],
    function (messageListView, i18n, messageListViewAction, css) {
        var userId;
        var paramsMap = new Object();
        var URl;
        var gomOrderNum = 10;
        var gomOrderPage = 1;

        return fish.View.extend({
            resNetworkUrl: '',
            crmRegion: '',
            userInfo: new Object(),
            template: fish.compile(messageListView),
            i18nData: fish.extend({}, i18n),
            events: {
                'click #queryOrder': 'initQueryBut',
                'click #excelOrder': 'excelOrder',
                'click #comfirmBtn': 'comfirmBtn'
            },
            initialize: function () {
                this.render();
                this.userInfo = messageListViewAction.queryStaffInfo().responseJSON.data;
                userId = this.userInfo.userId;
            },

            //渲染页面
            render: function () {
                this.$el.html(this.template(this.i18nData));
            },

            //初始化fish组件
            afterRender: function () {
                $('#applyOrder').clearinput();
                $('#orderNo').clearinput();
                $('#productType').clearinput();
                URl = this.getRootPath();

                $('.rowtext .ui-pagination').change(function () {
                        var p1 = $(this).children('option:selected').val();//这就是selected的值
                        localGomNum = p1;
                        localGomPage = 1;
                        this.queryOrderList();
                    }
                );
                //初始化表格
                this.initCombobox();
                this.initorderDealGrid();
                this.queryOrderList(); //初始化完成查询数据
            },

            initCombobox: function () {
                this.initProductTypeData();
            },

            //产品类型
            initProductTypeData: function () {
                var productTypeObj = new Object();
                productTypeObj.codeType = 'product_code';
                //产品类型
                messageListViewAction.queryProdTypeData(productTypeObj, function (data) {
                    $('#productType').combobox({
                        placeholder: '--请选择产品类型--',
                        dataTextField: 'name',
                        dataValueField: 'value',
                        dataSource: data
                    });
                });
            },

            initorderDealGrid: function () {
                var me = this;
                var queryOrderList = $.proxy(this.queryOrderList, this); //函数作用域改变
                $("#orderDeal-grid").grid({
                    colModel: [
                        //默认展示字段
                        {name: 'MESSAGE_ID', label: '订单ID', width: 100, hidden: true},
                        {name: 'ORDER_ID', label: '订单ID', width: 100, hidden: true},
                        {name: 'WO_ID', label: '工单ID', width: 100, hidden: true},
                        {name: 'PROD_ID', label: '产品ID', width: 100, hidden: true},
                        {name: 'PROD_TYPE', label: '产品类型', width: 120, align: 'center'},
                        {name: 'APPLY_ORDER', label: '申请单号', width: 200, align: 'center'},
                        {name: 'ORDER_NO', label: '订单编号', width: 120, align: 'center'},
                        {name: 'SERIAL_NUMBER', label: '业务号码', width: 120, align: 'center'},
                        {name: 'SPEC_NAME', label: '专业', width: 120, align: 'center'},
                        {name: 'USER_NAME', label: '岗位/人员', width: 120, align: 'center'},
                        {name: 'MESSAGE_ALIAS', label: '已完成操作', width: 120, align: 'center'}
                    ],
                    datatype: "json",
                    autowidth: true,
                    rowNum: 10,
                    rowList: [10, 15, 20, 50, 100, 200, 500, 1000],
                    pager: true,
                    curPageSort: true,
                    recordtext: "{0}-{1} 共{2}条",
                    pgtext: " 第{0}页/共{1}页",
                    rowtext: "每页{0}条",
                    gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                    multiselect: true,
                    shrinkToFit: false,
                    autoResizable: true,
                    showColumnsFeature: false, //允许用户自定义列展示设置
                    cached: false, //把用户自定义的列展示设置缓存在本地
                    pageData: queryOrderList,
                    onCellSelect: function (e, rowid, iCol, cellcontent, colName, cellval) { //选中单元格的事件
                        var dataCell = $("#orderDeal-grid").grid("getRowData", rowid);
                        if (iCol == 5 || iCol == 6 ||iCol == 7) {
                            debugger;
                            var data = {
                                title: "工单待办",
                                id: "a11",
                                hash: "javascript:;",
                                "toTab": $("#tabs-a-d-link").attr("value"),
                                "icon": "glyphicon glyphicon-edit",
                                "url": "UnicomLocalNetStandby.html?navType=no&srvOrdId=" + dataCell.APPLY_ORDER,
                            };
                            me.openTabView(data);
                        }
                    },
                    onSelectRow:function(e, rowid, state, checked ){ //选中行事件
                    },
                    gridComplete: function () {
                        $('.gotext').html("<span>跳转至<input class=\"ui-pagination-input\"></span>");
                    }
                });
                this.resize();

                $('.rowtext .ui-pagination').change(function () {
                        var p1 = $(this).children('option:selected').val();//这就是selected的值
                        gomOrderNum = p1;
                        gomOrderPage = 1;
                    }
                );
            },

            openTabView: function (data) {
                var pView = parent.window.contentTabView;
                var id = data.id;
                var title = data.title;
                var url = data.url;
                var param = data.toTab;
                var context = ngc.getContext();
                var origin = window.location.origin;
                var _url = origin + context + "/" + url;
                pView.openFrameView(id, title, _url, true, true, param, false);
            },

            //查询工单方法
            queryOrderList: function (page, rowNum, sortname, sortorder) {
                //分页信息
                rowNum = (rowNum != '' && rowNum != undefined) ? rowNum : gomOrderNum;
                page = (page != '' && page != undefined) ? page : gomOrderPage;

                paramsMap.pageIndex = page + '';
                paramsMap.pageSize = rowNum + '';

                var orderNo = $("#orderNo").val();//订单编号
                var productType = $("#productType").val();//产品类型
                var applyOrder = $("#applyOrder").val(); //申请单号
                paramsMap.prodType = productType;
                paramsMap.orderNo = orderNo;
                paramsMap.applyOrder = applyOrder;
                paramsMap.userId = userId;

                debugger;
                //调用后台方法
                $("#orderDeal-grid").blockUI({message: '加载中'}).data('blockui-content', true);
                messageListViewAction.queryMessageList(paramsMap, function (data) {
                    var gridData = {
                        "rows": data.data,
                        "page": page,
                        "records": data.dataLength,
                        "rowNum": rowNum,
                        "total": data.total
                    };
                    $("#orderDeal-grid").grid("reloadData", gridData);
                }).always(function () {
                    $("#orderDeal-grid").unblockUI().data('blockui-content', false);
                });
            },

            initQueryBut: function () { //查询
                this.queryOrderList();
            },

            comfirmBtn:function(){ //批量确认
                var me = this;
                var selrow = $("#orderDeal-grid").grid("getCheckRows"); //获取多选记录
                fish.confirm(i18n.CONFIRM_SUBMIT({item:selrow.length})).result.then(function () {
                    messageListViewAction.deleteMessageList(selrow,function (res) {
                        if(!res.success){
                            fish.toast("error",res.message);
                        }else{
                            fish.toast("success",i18n.MESSAGE_CONFORM_SUCCESS({item:res.message}));
                            me.queryOrderList();
                        }
                    });
                })
            },

            excelOrder: function () {  //导出
                var selarrrow = $("#orderDeal-grid").grid("getCheckRows");
                paramsMap.selarrrow = selarrrow;
                messageListViewAction.exportGomOrderListData(URl + '/messageList/messageListController/exportMessageList.spr', paramsMap);
            },

            resize: function () {
                // $("#orderDeal-grid").grid("resize",true);
                var frameHeight = document.documentElement.scrollHeight;
                $("#orderDeal-grid").grid("setGridHeight", frameHeight - 215);
            },

            getRootPath: function () {
                //获取当前网址，如： http://localhost:8083/uimcardprj/share/meun.jsp
                var curWwwPath = window.document.location.href;
                //获取主机地址之后的目录，如： uimcardprj/share/meun.jsp
                var pathName = window.document.location.pathname;
                var pos = curWwwPath.indexOf(pathName);
                //获取主机地址，如： http://localhost:8083
                var localhostPaht = curWwwPath.substring(0, pos);
                //获取带"/"的项目名，如：/uimcardprj
                var projectName = pathName.substring(0, pathName.substr(1).indexOf('/') + 1);
                return (localhostPaht + projectName);
            },
        }); //fish.View.extend END
    }); //ALL END