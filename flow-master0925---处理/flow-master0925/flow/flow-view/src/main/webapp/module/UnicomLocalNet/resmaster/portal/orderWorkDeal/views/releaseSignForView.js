/**释放签收*/
define(['module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
        'text!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/templates/releaseSignForView.html',
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/orderDetailsView.css'],
    function (orderStandbyAction, template, i18n, css) {
        return ngc.View.extend({
            template: ngc.compile(template),
            i18nData: ngc.extend({}, i18n),
            events: {
                'click #child-commit': 'commitFrom',
                'click #child-cancel': 'cancelFrom'
            },
            initialize: function () {
                this.render();
                FILES = null;
            },
            render: function () {           //渲染页面
                this.$el.html(this.template(this.i18nData));
            },
            //初始化fish组件
            afterRender: function () {
                //初始化释放电路表格
                this.initReleaseWorkOrderGrid();
                this.writeBackReleaseWorkOrderGrid();
            },
            // 初始化释放电路表格
            initReleaseWorkOrderGrid: function () {
                var me = this;
                $("#releaseWorkOrderGrid").grid({
                    height: 330,
                    autowidth: true,
                    curPageSort: true,
                    gridview: false,
                    multiselect: true,
                    shrinkToFit: false,
                    autoResizable: true,
                    colModel: [
                        //默认展示字段
                        {name: 'WO_COMPLETE_STATE', label: '单据状态', width: 80, sortable: false, hidden: true},
                        {name: 'CST_ORD_ID', label: '客户Id', width: 80, sortable: false, hidden: true},
                        {name: 'ORDER_CODE', label: '流程订单编码', width: 80, sortable: false, hidden: true},
                        {name: 'SERVICE_ID', label: '业务类型', width: 80, sortable: false, hidden: true},
                        {name: 'PS_ID', label: 'PS_ID', width: 240, sortable: false, hidden: true},
                        {name: 'ORDER_ID', label: '订单ID', width: 240, sortable: false, hidden: true},
                        {name: 'WO_STATE', label: '工单状态', width: 240, sortable: false, hidden: true},
                        {name: 'WO_ID', label: '工单ID', width: 240, sortable: false, hidden: true, align: 'center'},
                        {name: 'TACHE_ID', label: '环节ID', width: 240, sortable: false, hidden: true},
                        {name: 'COMP_USER_ID', label: '处理人', width: 80, sortable: false, hidden: true},
                        {name: 'SRV_ORD_ID', label: '业务订单信息ID', width: 240, sortable: false, hidden: true},
                        {name: 'CUST_NAME_CHINESE', label: '客户名称', width: 200},
                        {name: 'SUBSCRIBE_ID', label: '客户订单号', width: 120, align: 'center'},
                        {name: 'TRADE_ID', label: '业务订单号', width: 120, align: 'center'},
                        {name: 'ATTR_VALUE', label: '电路编码', width: 120, align: 'center'},
                        {name: 'TACHE_NAME', label: '环节名称', width: 120, align: 'left'},
                        {name: 'REGION_NAME', label: '区域', width: 120, align: 'center'},
                        {name: 'PUB_DATE_NAME', label: '专业', width: 120, align: 'center'},
                        {name: 'DISPATCH_ORDER_NO', label: '调度单编号', width: 120, align: 'center'},
                        {name: 'DISPATCH_TITLE', label: '调度单标题', width: 200},
                        {name: 'APPLY_ORD_NAME', label: '申请单标题', width: 200},
                        {name: 'REQ_FIN_DATE', label: '环节要求完成时间', width: 160, align: 'center'},
                    ]
                });
            },
            // 回写电路信息
            writeBackReleaseWorkOrderGrid: function () {
                var _this = this;
                var collQueryObj = new Object();

                collQueryObj.queryTypeLocal = 'dealOrder';
                collQueryObj.cstOrdId = this.options.cstOrdId; // 客户Id
                collQueryObj.tacheId = this.options.tacheId;   // 环节Id
                collQueryObj.regionId = this.options.regionId; // 区域Id
                collQueryObj.specialtyCode = this.options.specialtyCode;  // 专业
                collQueryObj.dispObjTyeValue = this.options.dispObjTyeValue; // 岗位、部门、个人值
                collQueryObj.dispObjTye = this.options.dispObjTye; // 业务订单Id合集

                collQueryObj.compUserId = '';
                collQueryObj.woState = '290000002';
                collQueryObj.dispType = '260000003';
                collQueryObj.staffId = '';
                collQueryObj.dealUserId = this.options.staffId;
                orderStandbyAction.querySubOrderInfoColl(collQueryObj, function (data) {
                    if (data.messages = "success") {
                        $("#releaseWorkOrderGrid").grid("reloadData", data.data);
                    } else {
                        fish.toast("error", "获取数据失败");
                    }
                });
            },
            // 驳回
            commitFrom: function () {
                var _this = this;
                var parameters = {};
                var woOrderIds = new Array()
                var selarrrow = $("#releaseWorkOrderGrid").grid("getCheckRows");

                $.each(selarrrow, function (p) {
                    var sewoId = selarrrow[p].WO_ID;
                    woOrderIds.push(sewoId);
                });
                if (woOrderIds.length <= 0) {
                    ngc.warn("请选择一条或多条数据");
                    return;
                }
                parameters.woOrderIds = woOrderIds;
                parameters.actionType = 'free';

                orderStandbyAction.getFreeWoOrder(parameters, function (data) {
                    if (data.success) {
                        ngc.info(data.message);
                        _this.closeView();
                    } else {
                        ngc.error(data.message);
                    }
                });
            },
            // 取消
            cancelFrom: function () {
                this.closeView(null);
            },
            // 关闭视图
            closeView: function (data) {
                this.trigger('returnData', data);
                this.popup.close();
            }
        })
    });