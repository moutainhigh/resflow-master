/**
 * 静态流程图展示页面
 */
define(['module/UnicomLocalNet/resmaster/portal/flowChart/action/flowChartViewAction',
    'text!module/UnicomLocalNet/resmaster/portal/flowChart/templates/flowChartView.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/flowChart/styles/flowChart.css'
], function (flowChartVAction, flowChartView, i18n, css) {
    return fish.View.extend({
        orderId: "",
        srvOrdId: "",
        psId: "", //子流程psId
        parentPsId: "", //父流程psId
        woState: "",
        dataSource: "",
        systemSource: "",
        template: fish.compile(flowChartView),
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
            debugger
            this.orderId = this.options.orderId;
            this.srvOrdId = this.options.srvOrdId;
            this.woState = this.options.woState;
            var srvBelong = new Object();
            srvBelong.srvOrdId = this.srvOrdId;
            sysResourceData=flowChartVAction.qrySrvOrderBelongSysFlow(srvBelong).responseJSON.data;
            this.dataSource = sysResourceData.RESOURCES;
            this.systemSource = sysResourceData.SYSTEM_RESOURCE;
            debugger
            var srvBelongOrder = new Object();
            srvBelongOrder.orderId = this.orderId;
            parentPsIdTemp = flowChartVAction.qryParentPsIdBySubOrderId(srvBelongOrder).responseJSON.data;
            if(parentPsIdTemp != ''
                && parentPsIdTemp != undefined){
                this.parentPsId = parentPsIdTemp.PS_ID;
                this.psId = this.options.psId;
            }else{
                this.parentPsId = this.options.psId;
                this.psId = '';
            }

            this.initFlowCharts(this); //初始化流程跟踪图

        },
        initFlowCharts: function(fThis){ //初始化流程跟踪图
            var flowUrl = "";
            debugger
            if('1000212' == this.parentPsId){ //本地客户电路新开、变更、移机流程
                flowUrl = "module/UnicomLocalNet/resmaster/portal/flowChart/views/flowChartLocalCustomView";
            }else if("1000207" == this.parentPsId){ //本地局内电路新开、变更流程
                flowUrl = "module/UnicomLocalNet/resmaster/portal/flowChart/views/flowViewRelay";
            }else if("1000211" == this.parentPsId){ //本地核查流程
                flowUrl = "module/UnicomLocalNet/resmaster/portal/flowChart/views/localVerificationView";
            }else if('1000213' == this.parentPsId){ //本地客户电路停复机流程
                flowUrl = "module/UnicomLocalNet/resmaster/portal/flowChart/views/flowLocalCusStopReView";
            }else if('1000214' == this.parentPsId){ //本地客户电路拆机流程
                flowUrl = "module/UnicomLocalNet/resmaster/portal/flowChart/views/flowLocalCusUnpackView";
            }else if('1000208' == this.parentPsId){ //本地局内电路拆机流程
                flowUrl = "module/UnicomLocalNet/resmaster/portal/flowChart/views/flowStationUnpackView";
            }else if('1000209' == this.parentPsId){ //跨域电路新开、变更流程
                flowUrl = "module/UnicomLocalNet/resmaster/portal/flowChart/views/flowCrossMainView";
            }else if('1000210' == this.parentPsId){ //跨域电路停闭流程
                flowUrl = "module/UnicomLocalNet/resmaster/portal/flowChart/views/flowCrossMainCloseView";
            }else if('10101303' == this.parentPsId){ //sdwan  新开流程
                flowUrl = "module/UnicomLocalNet/resmaster/portal/flowChart/views/flowSdwanCrossView";
            }else if('10101304' == this.parentPsId){ //sdwan 拆机流程
                flowUrl = "module/UnicomLocalNet/resmaster/portal/flowChart/views/flowSdwanChangeView";
            }


            if(flowUrl != ""){
                this.requireView({selector:"#flowChartIndexView",
                    url:flowUrl,
                    viewOption:{
                        orderId: fThis.orderId,
                        srvOrdId: fThis.srvOrdId,
                        psId: fThis.psId,
                        parentPsId: fThis.parentPsId,
                        woState: fThis.options.woState,
                        dataSource: fThis.dataSource,
                        systemSource: fThis.systemSource,
                    },
                    callback: function (view) {
                        // debugger
                    }}).then(function (view) {
                    // debugger
                });
            }

        },
       
    });
})