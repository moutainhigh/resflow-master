/**
 * 静态流程图展示页面- 二干客户电路停复机流程
 */
define(['module/UnicomLocalNet/resmaster/portal/flowChart/action/flowChartViewAction',
    'text!module/UnicomLocalNet/resmaster/portal/flowChart/templates/secondCustStopRecovery.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/flowChart/styles/flowChart.css'
], function (flowChartVAction, secondCustomerView, i18n, css) {
    return fish.View.extend({
        orderId: "",
        srvOrdId: "",
        psId: "",
        parentPsId: "",
        woState: "",
        dataSource: "",
        systemSource: "",
        template: fish.compile(secondCustomerView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #TO_DATA_CREATE_AND_SCHEDULE_2': 'secondToSchedule',

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
            debugger
            var _this = this;
            this.orderId = this.options.orderId;
            this.srvOrdId = this.options.srvOrdId;
            this.psId = this.options.psId;
            this.parentPsId = this.options.parentPsId;
            this.woState = this.options.woState;
            this.dataSource = this.options.dataSource,
                this.systemSource = this.options.systemSource,
                this.initFlowCharts(this); //初始化流程跟踪图
            debugger
            if('second-schedule-lt' == this.systemSource){
                if('secondary' == this.dataSource){
                    $("#innerFlow").css('display','block');
                }else if('jike' == this.dataSource){
                    $("#jikeFlow").css('display','block');
                    $('#NOTICE_OF_RENT_CONFIRMATION_2').children('.fish-flowName').text("集客起租");
                }else if('onedry' == this.dataSource){
                    $("#onedryFlow").css('display','block');
                    $('#NOTICE_OF_RENT_CONFIRMATION_2').children('.fish-flowName').text("一干起租");
                }
            }

            var flowStatus = new Object();
            flowStatus.orderId = this.orderId;
            flowChartVAction.qryParentSubStatusByOrderId(flowStatus,function (data) {
                debugger
                if(data != ''
                    && data != undefined
                    && data.length >0){
                    var localFlag = false;
                    fish.each(data, function (dataSub) {
                        if('290000002' == dataSub.WO_STATE
                            || '290000110' == dataSub.WO_STATE
                            || '290000111' == dataSub.WO_STATE
                            || '290000112' == dataSub.WO_STATE){ //执行中
                            $("#"+dataSub.TACHE_CODE).addClass('fish-dispose');

                        }else if('290000004' == dataSub.WO_STATE){ //已完成
                            $("#"+dataSub.TACHE_CODE).addClass('fish-finish');

                        }
                        if (dataSub.TACHE_CODE == 'NOTICE_OF_RENT_CONFIRMATION_2'){
                            if (dataSub.WO_STATE != '290000005'){
                                localFlag = true;
                            }
                        }
                    });
                    if (localFlag){
                        $('#LOCAL_DISPATCH').addClass('fish-finish')
                    } else {
                        //查询关联表
                        flowChartVAction.qryRelateTableByOrderId(flowStatus, function (res) {
                            if (res.success){
                                if (res.data > 0){
                                    $('#LOCAL_DISPATCH').addClass('fish-dispose');
                                }
                            }
                        });
                    }
                }
            });
            //环节图标点击事件
            $(".tachClick").on("click", function () {
                debugger
                $('#customerView').empty();
                _this.initTaskInfoGrid($(this));
                // debugger
                // _this.tachClick($(this));
            });


        },
        initFlowCharts: function(fThis){ //初始化流程跟踪图
            $('.panel').panel({
                collapsible: true,
                beforeClose: function (e) {
                    console.log('beforeClose');
                    return fish.blockedConfirm(e, 'Are you sure to close this panel?');
                    //return false;// 阻止关闭
                }
            });
            debugger
            $("div[id = 'LOCAL_DISPATCH']").click(function () {
                    debugger
                    var popFile = fish.popupView({
                        url: 'module/UnicomLocalNet/resmaster/portal/flowChart/views/flowLocalCusStopReView',
                        width: "99%",
                        height: "95%",
                        title: "跨域电路停复机流程",
                        viewOption: {
                            orderId: fThis.options.orderId,
                            srvOrdId: fThis.options.srvOrdId,
                            dataSource: fThis.options.dataSource,
                            systemSource: fThis.options.systemSource,
                            psId: fThis.options.psId,
                            parentPsId: fThis.options.parentPsId,
                            woState: fThis.options.woState,

                        },
                        callback: function (popup, view) {
                            // debugger
                            popup.result.then(function (e) {

                            }, function (e) {
                                console.log('关闭了', e);
                            });
                        }
                    });
                }
            );


        },
        tachClick :function($this,page, rowNum, sortname, sortorder){
            var tachCode = $this.parent().attr('id');
            var param = {};
            param.srvOrdId = this.options.srvOrdId;
            param.orderId = this.options.orderId;
            param.tachCode = tachCode;

            rowNum = rowNum || this.$("#customerView").grid("getGridParam", "rowNum");
            fish.store.set('resource-grid-rowNum', rowNum); //记录用户选择的每页记录数
            page = page || 1;
            //登陆人信息
            var paramsMap = {};
            //分页信息
            paramsMap.pageIndex = page+'';
            paramsMap.pageSize = rowNum+'';
            //排序信息
            if (sortname && sortorder) {
                paramsMap.sortCol = this.camelToUnderline(sortname);
                paramsMap.sortOrder = sortorder.toUpperCase();
            }
            //调用后台方法
            $("#customerView").blockUI({message: '加载中'}).data('blockui-content', true);
            flowChartVAction.queryTaskInfoByTacheCode(param,function (res) {
                $("#customerView").unblockUI().data('blockui-content', false);
                $("#customerView").grid("reloadData", res.result);
                $(window).trigger("resize");
            });
        },

        //初始化任务信息表格
        initTaskInfoGrid: function($this) {
            var taskInfo = $.proxy(this.tachClick($this),this); //函数作用域改变
            $("#customerView").grid({
                colModel: [
                    //默认展示字段
                    {name: 'TACHE_NAME', label: '工单环节', width: 80, sortable: false},
                    {name: 'DEPT_NAME', label: '所属部门', width: 80, sortable: false},
                    {name: 'USERJOBNAME', label: '执行岗位', width: 130, sortable: false},
                    {name: 'USERNAME', label: '操作人', width: 80, sortable: false},
                    {name: 'ORDERSTATE', label: '工单状态', width: 80, sortable: false},
                    {name: 'TRACKCONTENT', label: '处理结果', width: 80, sortable: false},
                    {name: 'PHONE_NO', label: '联系方式', width: 100, sortable: false},
                    {name: 'CREATE_DATE', label: '到单时间', width: 100, sortable: false},
                    {name: 'DEAL_DATE', label: '签收时间', width: 100, sortable: false},
                    {name: 'STATE_DATE', label: '处理时间', width: 100, sortable: false}
                ],
                rownumbers:true,
                autowidth: true,
                rowNum: 10,
                rowList: [10,15,20,50],
                pager: true,
                gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                cached: true, //把用户自定义的列展示设置缓存在本地
                pageData: taskInfo
            });
            $("#customerView").grid("setGridHeight", 327);
        },



    });
})