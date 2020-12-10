/**
 * 流程定单展示页面
 */
define(['module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/operOrderAction',
    'text!module/UnicomLocalNet/resmaster/portal/flowChart/templates/flowChartTable.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/flowChart/styles/flowChart.css'
], function (operOrderAction, flowChartTable, i18n, css) {
    return fish.View.extend({
        template: fish.compile(flowChartTable),
        i18nData: fish.extend({}, i18n),
        events: {},
        initialize: function () {
            this.render();
        },
        //渲染页面
        render: function () {
            this.$el.html(this.template(this.i18nData));
        },
        //初始化fish组件
        afterRender: function () {
            $('.panel').panel({
                collapsible: true
            });
            this.initMainFlowGrid();
            this.initChildFlowGrid();
            if (this.options.srvOrdId) {
                this.writeBackMainFlowData();
                this.writeBackChildFlowData();
            }
        },
        //初始化主流程表格
        initMainFlowGrid: function () {
            var me = this;
            $("#main-flow-grid").grid({
                height: 'auto',
                colModel: [
                    {name: 'ORDER_ID', label: '流程定单ID', width: 30, sorttype: "int", key: true},
                    {name: 'ORDER_CODE', label: '定单编码', width: 40},
                    {name: 'ORDER_TITLE', label: '定单标题', width: 55},
                    {name: 'CREATE_DATE', label: '创建日期', width: 30},
                    {name: 'ORDER_STATE_VALUE', label: '定单状态', width: 30},
                    {
                        name: 'action', label: '操作', width: 20, formatter: function (cellval, opts, rwdat, _act) {
                            return '<div class="btn-group">' +
                                '<button type="button" name="flowChartBtn" class="btn btn-link js-delete"  orderId="' + rwdat.ORDER_ID + '" style="color: orangered">流程跟踪</button>' +
                                '</div>';
                        }
                    }
                ],
                autoResizable: true
            });
        },
        // 回写主流程数据
        writeBackMainFlowData: function () {
            var _this = this;
            var obj = {
                orderId: _this.options.orderId,
                srvOrdId: _this.options.srvOrdId
            };
            operOrderAction.getFlowOrderById(obj, function (data) {
                if (data && data.length > 0) {
                    $("#main-flow-grid").grid("reloadData", data);
                    _this.flowChartBtn();
                }
            });
        },
        initChildFlowGrid: function () {
            var me = this;
            $("#child-flow-grid").grid({
                height: 'auto',
                colModel: [
                    {name: 'ORDER_ID', label: '流程定单ID', width: 30, sorttype: "int", key: true},
                    {name: 'ORDER_CODE', label: '定单编码', width: 40},
                    {name: 'ORDER_TITLE', label: '定单标题', width: 55},
                    {name: 'CREATE_DATE', label: '创建日期', width: 30},
                    {name: 'ORDER_STATE_VALUE', label: '定单状态', width: 30},
                    {
                        name: 'action', label: '操作', width: 20, formatter: function (cellval, opts, rwdat, _act) {
                            return '<div class="btn-group">' +
                                '<button type="button" name="flowChartBtn" class="btn btn-link js-delete"  orderId="' + rwdat.ORDER_ID + '" style="color: orangered">流程跟踪</button>' +
                                '</div>';
                        }
                    }
                ],
                autoResizable: true
            });
        },
        writeBackChildFlowData: function () {
            var _this = this;
            var obj = {
                orderId: _this.options.orderId,
                srvOrdId: _this.options.srvOrdId
            };
            operOrderAction.getFlowOrderByParentId(obj, function (data) {
                if (data && data.length > 0) {
                    $("#child-flow-grid").grid("reloadData", data);
                    _this.flowChartBtn();
                }
            });
        },
        // 流程跟踪监听
        flowChartBtn: function () {
            var me = this;
            //驳回监听
            $("button[name='flowChartBtn']").off('click').on('click', function (e) {
                var orderId = $(this).attr("orderId");
                parent.window.ngc.openView({
                    url: "module/gom/page/flow/views/flowDiagram",
                    title: "流程查看",
                    height: "100%",
                    width: "100%",
                    viewOption: {
                        ORDER_ID: orderId + '',
                        orderId: orderId + ''
                    }
                });
            });
        },
    });
})