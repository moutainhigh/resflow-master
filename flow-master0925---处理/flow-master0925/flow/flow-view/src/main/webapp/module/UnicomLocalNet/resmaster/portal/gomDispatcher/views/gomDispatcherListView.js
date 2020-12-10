define([
    'module/UnicomLocalNet/resmaster/portal/gomDispatcher/action/gomDispatcherListAction',
    'text!module/UnicomLocalNet/resmaster/portal/gomDispatcher/templates/gomDispatcherListView.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/gomDispatcher/i18n/gomDispatcherListView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/taskmanagement.css'
], function(gomListAction,gomListView,i18n,css) {

    var localDispatchNum = 10;
    var localDispatchPage = 1;

    return fish.View.extend({
        userInfo: new Object(),
        template: fish.compile(gomListView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #queryGomDispatcher' : 'queryGomDispatcherFun',
            'click #exportGomDispatcher' : 'exportGomDispatcherFun',
            'click #resetGomDispatcher' : 'resetGomDispatcherFun'
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
            this.userInfo = gomListAction.queryStaffInfo().responseJSON.data;
            //初始化表格
            this.initGomorderDealGrid();
            this.initCombobox();
            $('#orderTitle').clearinput();
            $('#custName').clearinput();
            $('#cirNum').clearinput();
            $('#serialNumber').clearinput();
            $('#subscribeId').clearinput();
            $('#tradeId').clearinput();

            //初始化时间控件
            $('#finishDateStart').datetimepicker({
                orientation:{y:'bottom'}
            });
            $('#finishDateEnd').datetimepicker({
                orientation:{y:'bottom'}
            });
            //初始化查询调单
            // this.queryGomDispatcherFun();
            $('#productType').on('combobox:change', function(e) {
                var productTypeObj = new Object();
                productTypeObj.codeType = $('#productType').val();
                if(productTypeObj.codeType == ""
                    || productTypeObj.codeType == undefined){
                    productTypeObj.codeType = 'operate_type';
                }
                gomListAction.queryProdTypeData(productTypeObj,function(data){
                    $('#actType').combobox({
                        dataSource: data,
                        placeholder: '--请选择动作类型--',
                        dataTextField: 'name',
                        dataValueField: 'value'
                    });
                });
            });

            $('.rowtext .ui-pagination').change(function () {
                    var p1=$(this).children('option:selected').val();//这就是selected的值
                    localDispatchNum = p1;
                    localDispatchPage = 1;
                    this.queryGomWorkOrders();
                }
            );

        },
        initCombobox:function(){
            this.initProductTypeData();
            this.initOperationData();

        },
        //产品类型
        initProductTypeData: function(){
            var productTypeObj = new Object();
            productTypeObj.codeType = 'product_code';
            //产品类型
            gomListAction.queryProdTypeData(productTypeObj,function(data) {
                $('#productType').combobox({
                    placeholder: '--请选择产品类型--',
                    dataTextField: 'name',
                    dataValueField: 'value',
                    dataSource: data
                });
            });
        },
        //动作类型
        initOperationData: function(){
            var operationTypeObj = new Object();
            operationTypeObj.codeType = 'operate_type';
            //动作类型1
            gomListAction.queryProdTypeData(operationTypeObj,function(data){
                $('#actType').combobox({
                    dataSource: data,
                    placeholder: '--请选择动作类型--',
                    dataTextField: 'name',
                    dataValueField: 'value'
                });
            });
        },
        initGomorderDealGrid: function() {
            var me = this;
            var queryGomWorkOrders = $.proxy(this.queryGomWorkOrders,this); //函数作用域改变
            $("#orderDeal-grid").grid({
                colModel: [
                    //默认展示字段
                    {name: 'cstOrdId', label: '客户Id', width: 180, align: 'left',hidden: true},
                    {name: 'dispatchOrderId', label: '调单Id', width: 180, align: 'left',hidden: true},
                    {name: 'serviceId', label: '产品类型Id', width: 180, align: 'left',hidden: true},
                    {name: 'orderIds', label: '流程订单Id', width: 180, align: 'left',hidden: true},
                    {name: 'srvOrdIds', label: '业务订单Id集合', width: 180, align: 'left',hidden: true},
                    {name: 'resources', label: '来源', width: 180, align: 'left',hidden: true},
                    {name: 'orderType', label: '单据类型', width: 180, align: 'left',hidden: true},
                    {name: 'dispatchSource', label: '调单来源', width: 120, align: 'left'},
                    {name: 'dispatchOrderNo', label: '调度单编号', width: 240, align: 'left'},
                    {name: 'dispatchTitle', label: '调度单标题', width: 400, align: 'left'},
                    {name: 'orderTitle', label: '申请单标题', width: 200, align: 'left'},
                    {name: 'custNameChinese', label: '客户名称', width: 200, align: 'left'},
                    {name: 'subscribeId', label: '客户订单号', width: 180, align: 'left'},
                    {name: 'serialNumbers', label: '业务号码', width: 180, align: 'left'},
                    {name: 'productCodeContent', label: '产品类型', width: 200, align: 'left'},
                    {name: 'activeTypeName', label: '动作类型', width: 200,  align: 'left'},
                    {name: 'cirCount', label: '电路数量', width: 180, align: 'center'}
                ],
                datatype: "json",
                autowidth: true,
                rowNum: 10,
                curPageSort: true,
                rowList: [10,15,20,50,100,200,500],
                pager: true,
                recordtext:"{0}-{1} 共{2}条",
                pgtext: " 第{0}页/共{1}页",
                rowtext: "每页{0}条",
                gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                multiselect: false,
                shrinkToFit: false,
                autoResizable: true,
                showColumnsFeature: false, //允许用户自定义列展示设置
                cached: true, //把用户自定义的列展示设置缓存在本地
                pageData: queryGomWorkOrders,
                gridComplete: function () {
                    $('.gotext').html("<span>跳转至<input class=\"ui-pagination-input\"></span>");
                },
                onDblClickRow: function (e, rowid, iRow, iCol) {//双击行事件
                    var dataParent = $("#orderDeal-grid").grid('getRowData', rowid);
                    me.orderDetailViewDisGroup(dataParent,me);
                },

            });
            this.resize();
        },
        //查询工单方法
        queryGomWorkOrders: function(page, rowNum, sortname, sortorder) {
            rowNum = (rowNum!=''&&rowNum!=undefined)?rowNum:localDispatchNum;
            page = (page!=''&&page!=undefined)?page:localDispatchPage;

            //登陆人信息
            var paramsMap = new Object();

            //分页信息
            paramsMap.pageIndex = page+'';
            paramsMap.pageSize = rowNum+'';

            var orderTitle = $("#orderTitle").val();//申请单标题
            var custName = $("#custName").val();//客户名称
            var cirNum = $("#cirNum").val();//调单编号
            var serialNumber = $("#serialNumber").val();//业务号码
            var tradeId = $("#tradeId").val();// 业务订单号
            var finishDateStart = $("#finishDateStart").val();//环节要求完成开始时间
            var finishDateEnd = $("#finishDateEnd").val();//环节要求完成结束时间
            var subscribeId = $("#subscribeId").val();// 客户流水号
            var actType = $("#actType").val();//动作类型
            var productType = $("#productType").val();//产品类型

            paramsMap.orderTitle=orderTitle;
            paramsMap.custName = custName;
            paramsMap.cirNum = cirNum;
            paramsMap.serialNumber = serialNumber;
            paramsMap.tradeId = tradeId;
            paramsMap.finishDateStart = finishDateStart;
            paramsMap.finishDateEnd = finishDateEnd;
            paramsMap.subscribeId = subscribeId;
            paramsMap.actType = actType;
            paramsMap.productType = productType;
            // debugger;
            //调用后台方法
            $("#orderDeal-grid").blockUI({message: '加载中'}).data('blockui-content', true);
            gomListAction.queryOrderInfo(paramsMap,function (data) {
                if (data.message == "success") {
                    // debugger;
                    var gridData = {
                        "rows": data.gomDispatcherOrderPoList,
                        "page": page,
                        "records": data.pageInfo.rowCount,
                        "rowNum": rowNum,
                        "total":data.pageInfo.pageCount
                    };
                    $("#orderDeal-grid").grid("reloadData", gridData);
                }else{
                    fish.toast("warn", "获取数据失败");
                }
                $("#orderDeal-grid").unblockUI().data('blockui-content', false);
            });
        },
        orderDetailViewDisGroup:function(dataView,meTemp){
            debugger
            var pop = fish.popupView({
                url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/orderDetailsView',
                width: "100%",
                height: "100%",
                title: "工单详情",
                modal: false,
                viewOption:{
                    buttonState:'',
                    psId:'',
                    srvOrdId:'',
                    cstOrdId:dataView.cstOrdId,
                    orderId:'',
                    orderIdSelect:'',
                    orderIds:dataView.orderIds,
                    resources:dataView.resources,
                    woState:'',
                    woId:'',
                    tacheId:'',
                    compUserId:'',
                    serviceId:dataView.serviceId,
                    srvOrdId:dataView.srvOrdIds.split(',')[0],
                    srvOrdIds:dataView.srvOrdIds,
                    reginonId:'',
                    specialtyCode:'',
                    selectType:'gomDispath',
                    dispatchOrderId: dataView.dispatchOrderId,
                    userInfo: meTemp.userInfo,
                    RESOURCES:dataView.resources
                },
                callback:function(popup,view){
                    popup.result.then(function (e) {
                        meTemp.queryGomWorkOrders();
                    },function (e) {
                        console.log('关闭了',e);
                        // $("#touchGet").css("style","display:block; float: left;");
                        // $("#touchGet").show();
                    });
                }
            })


        },
        //查询
        queryGomDispatcherFun: function(){
            this.queryGomWorkOrders();
        },
        //导出数据
        exportGomDispatcherFun: function(){
            var me = this;
            var orderTitle = $("#orderTitle").val();//申请单标题
            var custName = $("#custName").val();//客户名称
            var cirNum = $("#cirNum").val();//调单编号
            var serialNumber = $("#serialNumber").val();//业务号码
            var tradeId = $("#tradeId").val();// 业务订单号
            var finishDateStart = $("#finishDateStart").val();//环节要求完成开始时间
            var finishDateEnd = $("#finishDateEnd").val();//环节要求完成结束时间
            var subscribeId = $("#subscribeId").val();// 客户流水号
            var actType = $("#actType").val();//动作类型
            var productType = $("#productType").val();//产品类型

            var paramsMap = new Object();
            paramsMap.orderTitle=orderTitle;
            paramsMap.custName = custName;
            paramsMap.cirNum = cirNum;
            paramsMap.serialNumber = serialNumber;
            paramsMap.tradeId = tradeId;
            paramsMap.finishDateStart = finishDateStart;
            paramsMap.finishDateEnd = finishDateEnd;
            paramsMap.subscribeId = subscribeId;
            paramsMap.actType = actType;
            paramsMap.productType = productType;

            gomListAction.exportPageData('localScheduleLT/gomDispatcherOrderController/exportData.spr',paramsMap);

        },
        //重置
        resetGomDispatcherFun: function(){
            $("#orderTitle").val("");//申请单标题
            $("#custName").val("");//客户名称
            $("#cirNum").val("");//电路编号
            $("#serialNumber").val("");//业务号码
            $("#subscribeId").val("");//客户标识
            $("#tradeId").val("");//业务订单号
            $("#finishDate").val("");//要求完成时间
            $("#productType").combobox('value','');
            $("#actType").combobox('value','');

        },

        //浏览器窗口大小改变事件
        resize: function() {
            //$("#orderDeal-grid").grid("resize",true);
            var frameHeight = document.documentElement.scrollHeight;
            $("#orderDeal-grid").grid("setGridHeight", frameHeight - 215);
        }
    }); //fish.View.extend END
}); //ALL END