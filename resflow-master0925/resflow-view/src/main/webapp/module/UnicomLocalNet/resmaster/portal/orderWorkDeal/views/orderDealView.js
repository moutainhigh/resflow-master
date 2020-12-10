define(['module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/orderDealAction',
    'text!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/templates/orderDealView.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/taskmanagement.css'
], function(orderDealAction,orderDealView,i18n,css) {
    return fish.View.extend({
        template: fish.compile(orderDealView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #tabs-a-link': 'aClick',
            'click #tabs-b-link': 'bClick',
            'click #tabs-c-link': 'cClick',
            'click #findBtn':'findClick'

        },

        initialize: function() {
            this.render();
            this.workOrderState = "";
            this.showActionButtons = false;
        },

        //渲染页面
        render: function() {
            this.$el.html(this.template(this.i18nData));
        },

        //初始化fish组件
        afterRender: function() {
            //初始化tab
            $('#orderDeal-tab').tabs();

            //初始化表格
            this.initorderDealGrid();

            //默认选中待接单区
            $('#tabs-a-link').click();

            //初始化clearinput
            $('#userName,#autocomplete,#userName1').clearinput();

            //隐藏高级查询条件
            /*$(".orderDeal-advSearchFields-row").hide();
            $("#orderDeal-panel-body").attr("style","padding-bottom: 0px;");*/

            //初始化时间控件
            $('#finishDate,#endDate').datetimepicker({
                orientation:{y:'bottom'}
            });


            //初始化下拉框
            this.initCombobox();

            //初始化定时查询
            //this.initAutoQry();


        },

        initCombobox:function(){
            $('#productType').combobox({
                dataSource: [
                    {"name": "一月", "value": "01"},
                    {"name": "二月", "value": "02"},
                    {"name": "三月", "value": "03"}
                ],
                value: "01",
                change: function (event) {
                    var val = $('#productType').combobox("value");
                }
            });
        },

        initorderDealGrid: function() {
            var me = this;
            var queryWorkOrders = $.proxy(this.queryWorkOrders,this); //函数作用域改变
            $("#orderDeal-grid").grid({
                colModel: [
                    //默认展示字段
                    {name: 'ORDER_CODE', label: '定单编码', width: 120, sortable: false},
                    {name: 'ORDER_ID', label: '订单id', width: 150, sortable: false},
                    {name: 'ORDER_TITLE', label: '标题', width: 120, sortable: false},
                    {name: 'ORDER_STATE', label: '工单状态', width: 160, sortable: false},
                    {name: 'APPLY_MAN', label: '当前处理人', width: 100, sortable: false},
                    {name: 'APPLY_UNIT', label: '派发人', width: 100, sortable: false},
                    {name: 'AREA_ID', label: '派发人附件', width: 100, sortable: false},
                    {name: 'REQ_FIN_DATE', label: '要求完成时间', width: 150, sortable: true}
                ],
                autowidth: true,
                rowNum: 10,
                rowList: [10,15,20,50],
                pager: true,
                gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                multiselect: true,
                shrinkToFit: false,
                autoResizable: true,
                //showColumnsFeature: true, //允许用户自定义列展示设置
                cached: true, //把用户自定义的列展示设置缓存在本地
                pageData: queryWorkOrders
            });
            this.resize();
        },
        //查询工单方法
        queryWorkOrders: function(page, rowNum, sortname, sortorder) {
            rowNum = rowNum || this.$("#orderDeal-grid").grid("getGridParam", "rowNum");
            fish.store.set('orderDeal-grid-rowNum', rowNum); //记录用户选择的每页记录数
            page = page || 1;

            //登陆人信息
            var paramsMap = {};

            //分页信息
            paramsMap.pageIndex = page+'';
            paramsMap.pageSize = rowNum+'';

            //排序信息
            if (sortname && sortorder) {
                paramsMap.sortCol = this.camelToUnderline(sortname);
                paramsMap.sortOrder = sortorder.toUpperCase();;
            }

            //获取表单信息
            var formValue = $('#orderDeal-form').form("value");

            //任务管理必要查询条件
            paramsMap.workOrderState = this.workOrderState;
            paramsMap.orderClass = '1OA';
            //调用后台方法
            $("#orderDeal-grid").blockUI({message: '加载中'}).data('blockui-content', true);
            orderDealAction.queryOrderInfo(function (data) {
                $("#orderDeal-grid").grid("reloadData", data.data);
            });
            $("#orderDeal-grid").unblockUI().data('blockui-content', false);
        },

        aClick:function () {
            this.queryWorkOrders();
        },
        bClick:function () {
            this.queryWorkOrders();
        },
        cClick:function () {
            this.queryWorkOrders();
        },
        findClick:function(){
            var pop = fish.popupView({
                url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/operOrderView',
                width: "700px",
                hight: "600px",
                title: "工单处理",
                viewOption:{

                },
                callback:function(popup,view){
                    popup.result.then(function (e) {

                    },function (e) {
                        console.log('关闭了',e);
                    });
                }
            });
        },

        //浏览器窗口大小改变事件
        resize: function() {
            //$("#orderDeal-grid").grid("resize",true);
            $("#orderDeal-grid").grid("setGridHeight", 327);
        },

    }); //fish.View.extend END
}); //ALL END