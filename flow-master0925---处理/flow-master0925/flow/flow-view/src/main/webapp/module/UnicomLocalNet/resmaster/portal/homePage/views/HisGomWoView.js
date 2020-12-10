define(['module/UnicomLocalNet/resmaster/portal/homePage/action/HisGomWoAction',
    'text!module/UnicomLocalNet/resmaster/portal/homePage/templates/HisGomWoView.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/taskmanagement.css'
], function(hisGomWoAction,hisGomWoView,i18n,css) {
    var userId;
    var URl;
    return fish.View.extend({
        template: fish.compile(hisGomWoView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #his_queryOrder': 'queryWoOrder',
            'click #his_excelOrder': 'excelOrder',

        },
        initialize: function() {
            fish.setLanguage('zh');
            this.render();
            this.userInfo = hisGomWoAction.queryStaffInfo().responseJSON.data;
            userId=this.userInfo.userId;
        },
        //渲染页面
        render: function() {
            this.$el.html(this.template(this.i18nData));
        },
        //初始化fish组件
        afterRender: function() {
            //初始化查询条件字段
            this.initQryCondition();
            //初始化工单信息grid
            this.initGomWoGrid();
            //查询满足条件的工大你数据
            this.queryWorkOrders();
            URl=this.getRootPath();
            this.resize();

        },
        initQryCondition : function(){
            //初始化要求完成时间
            $('#his_startDate,#his_endDate').datetimepicker({
                orientation: {y: 'bottom'}
            });
            $('#his_month').datetimepicker({
                viewType: "month",
            });
            //是否退单
            $('#his_isBack').combobox({
                placeholder: '--请选择是否退单--',
                dataTextField: 'name',
                dataValueField: 'value',
                dataSource: [
                    {name: '是', value: '1'},
                    {name: '否', value: '0'}
                ]
            });
            //单据来源
            $('#his_resourceType').combobox({
                placeholder: '--请选择单据来源--',
                dataTextField: 'name',
                dataValueField: 'value',
                dataSource: [
                    {name: '政企中台', value: 'jike'},
                    {name: '一干调度', value: 'onedry'},
                    {name: '二干调度', value: 'secondary'},
                    {name: '本地调度', value: 'localBuild'},
                    {name: '云网协同', value: 'cloudNetwork'}
                ]
            });
            //初始化单据类型
            var obj = new Object();
            obj.codeType = "ITEM_TYPE";
            hisGomWoAction.queryItemType(obj, function (data) {
                $('#his_orderType').combobox({
                    placeholder: '--请选择单据类型--',
                    dataTextField: 'name',
                    dataValueField: 'value',
                    dataSource: data
                });
            })
            //初始化环节名称
            // 环节名称 teacheName
            obj.codeType = "TEACH_NAME";
            hisGomWoAction.queryItemType(obj, function (data) {
                $('#his_tacheName').multiselect({
                    containSearch:true,
                    placeholder: '--请选择环节名称--',
                    dataTextField: 'name',
                    dataValueField: 'value',
                    dataSource: data
                });
            })
        },
        //初始化工单信息
        initGomWoGrid : function(){
            var me = this;
            var queryWorkOrders = $.proxy(this.queryWorkOrders,this); //函数作用域改变
            $("#his_gomWOGrid").grid({
                colModel: [
                    {name: 'WOORDERBACKFLAGS', label: '是否退单', width: 100, align: 'left' },
                    {name: 'APPLY_ORD_ID', label: '申请单编号', width: 200, align: 'left' },
                    {name: 'APPLY_ORD_NAME', label: '申请单标题', width: 200, align: 'left' },
                    {name: 'CUST_NAME_CHINESE', label: '客户名称', width: 200, align: 'left'},
                    {name: 'SUBSCRIBE_ID', label: '客户订单号', width: 120, align: 'left'},
                    {name: 'TACHE_NAME', label: '环节名称', width: 150, align:'left'},
                    {name: 'COUNTS', label: '电路数量', width: 150, align:'left'},
                    {name: 'SERVICE_ID', label: '产品类型', width: 150, align:'left'},
                    {name: 'ACTIVE_TYPE', label: '动作类型', width: 150, align:'left'},
                    {name: 'ORDER_TYPE', label: '单据类型', width: 150, align:'left'},
                    {name: 'RESOURCES', label: '单据来源', width: 150, align:'left'},
                    {name: 'DISPATCH_ORDER_NO', label: '调度单编号', width: 150, align:'left'},
                    {name: 'DISPATCH_TITLE', label: '调度单标题', width: 150, align:'left'},
                    {name: 'ALARM_DATE', label: '环节预警时间', width: 150, align:'left'},
                    {name: 'REQ_FIN_DATE', label: '环节要求完成时间', width: 150, align:'left'},
                    {name: 'DEAL_DATE', label: '工单处理时间', width: 150, align:'left'}
                ],
                datatype: "json",
                autowidth: true,
                rowNum:10,
                rowList: [10,15,20,50,100,200,500,1000],
                pager: true,
                curPageSort: true,
                recordtext:"{0}-{1} 共{2}条",
                pgtext: " 第{0}页/共{1}页",
                rowtext: "每页{0}条",
                gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                multiselect: true,
                shrinkToFit: false,
                autoResizable: true,
                cached: true, //把用户自定义的列展示设置缓存在本地
                pageData: queryWorkOrders,
                onDblClickRow: function (e, rowid, iRow, iCol) {//双击行事件
                    me.orderDetailView(e, rowid, iRow, iCol);
                },
                onCellSelect: function (e, rowid, iCol, cellcontent, colName, cellval) {//选中单元格的事件

                }
            });
        },
        queryWorkOrders: function(page, rowNum, sortname, sortorder) {
            var pageSize = (rowNum!=''&&rowNum!=undefined)?rowNum: 10;
            var pageIndex = (page!=''&&page!=undefined)?page: 1;
            var param = {};
            param.applyOrdId = $("#his_applyOrdId").val();
            param.orderTitle = $('#his_orderTitle').val();
            param.custName = $('#his_custName').val();
            param.serialNumber = $("#his_serialNumber").val();
            param.orderType = $('#his_orderType').val();
            param.resourceType = $("#his_resourceType").val();
            param.isBack = $("#his_isBack").val();
            param.tacheIds = $("#his_tacheName").val() != null ? $("#his_tacheName").val().join(',') : '';
            param.month = $("#his_month").val();
            param.userId = userId;
            param.pageSize = pageSize;
            param.pageIndex = pageIndex;
            //调用后台方法
            $("#his_gomWOGrid").blockUI({message: '加载中'}).data('blockui-content', true);
            hisGomWoAction.queryWoInfoForHis(param,function (data) {
                $("#his_gomWOGrid").grid("reloadData", data);
                $("#his_gomWOGrid").unblockUI().data('blockui-content', false);
            });
        },
        formatResourcesName: function(value){
            var resourceName;
            switch (value) {
                case 'localBuild':
                    resourceName = '本地调度';
                    break;
                case 'onedry':
                    resourceName = '一干调度';
                    break;
                case 'secondary':
                    resourceName = '二干调度';
                    break;
                case 'jike':
                    resourceName = '政企中台';
                    break;
                case 'cloudNetwork':
                    resourceName = '云网协同';
                    break;
                default:
                    resourceName = '';
                    break;
            }
            return resourceName;
        },
        excelOrder:function(){  //导出
            var param = {};
            param.applyOrdId = $("#his_applyOrdId").val();
            param.orderTitle = $('#his_orderTitle').val();
            param.custName = $('#his_custName').val();
            param.serialNumber = $("#his_serialNumber").val();
            param.orderType = $('#his_orderType').val();
            param.resourceType = $("#his_resourceType").val();
            param.isBack = $("#his_isBack").val();
            param.tacheIds = $("#his_tacheName").val() != null ? $("#his_tacheName").val().join(',') : '';
            param.month = $("#his_month").val();
            param.userId = userId;
            hisGomWoAction.exportWoOrderInfo(URl+'/localScheduleLT/gomListController/exportWoOrderInfo.spr',param);
        },
        //查询按钮点击方法
        queryWoOrder : function(){
            this.queryWorkOrders();
        },
        //双击打开详情页
        orderDetailView:function(){ //详情页面
            var me = this;
            var data = $("#his_gomWOGrid").grid("getSelection");
            // debugger
            var pop = fish.popupView({
                url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/orderDetailsView',
                width: "100%",
                height: "100%",
                title: "工单详情",
                modal: false,
                viewOption:{
                    buttonState:'',
                    psId:data.PS_ID,
                    srvOrdId:data.SRV_ORD_IDS,
                    cstOrdId:data.CST_ORD_ID,
                    orderId:data.ORDER_IDS,
                    orderIdSelect:data.ORDER_IDS,
                    orderIds:data.ORDER_IDS,
                    woState:'',
                    woId:'',
                    tacheId:'',
                    compUserId:'',
                    serviceId:data.SERVICE_ID,
                    reginonId:'',
                    specialtyCode:data.SPECIALTY_CODE,
                    dispatchOrderId:data.DISPATCH_ORDER_ID,
                    selectType: 'gomQuery',
                    userInfo: me.userInfo,
                    RESOURCES:data.RESOURCES
                },
                callback:function(popup,view){
                    popup.result.then(function (e) {
                        me.queryWorkOrders();
                    },function (e) {
                        console.log('关闭了',e);
                    });
                }
            })
        },
        //浏览器窗口大小改变事件
        resize: function() {
            var frameHeight = document.documentElement.scrollHeight;
            $("#his_gomWOGrid").grid("setGridHeight", frameHeight - 235);
        },
        getRootPath:function (){
            //获取当前网址，如： http://localhost:8083/uimcardprj/share/meun.jsp
            var curWwwPath=window.document.location.href;
            //获取主机地址之后的目录，如： uimcardprj/share/meun.jsp
            var pathName=window.document.location.pathname;
            var pos=curWwwPath.indexOf(pathName);
            //获取主机地址，如： http://localhost:8083
            var localhostPaht=curWwwPath.substring(0,pos);
            //获取带"/"的项目名，如：/uimcardprj
            var projectName=pathName.substring(0,pathName.substr(1).indexOf('/')+1);
            return (localhostPaht+projectName);
        },
    }); //fish.View.extend END
}); //ALL END