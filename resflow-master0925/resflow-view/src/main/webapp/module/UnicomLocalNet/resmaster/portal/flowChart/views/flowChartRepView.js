/**
 * 静态流程图展示页面
 */
define(['module/UnicomLocalNet/resmaster/portal/flowChart/action/flowChartViewAction',
    'text!module/UnicomLocalNet/resmaster/portal/flowChart/templates/flowChartRepView.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/flowChart/styles/flowChart.css'
], function (flowChartVAction, flowChartRepView, i18n, css) {
    return fish.View.extend({
        orderId: "",
        srvOrdId: "",
        psId: "", //子流程psId
        parentPsId: "", //父流程psId
        woState: "",
        dataSource: "",
        systemSource: "",
        template: fish.compile(flowChartRepView),
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
            if('10101060' == this.parentPsId){ //二干客户电路流程-新开、变更、拆机流程
                flowUrl = "module/UnicomLocalNet/resmaster/portal/flowChart/views/secondCustomerView";
            }else if("10101061" == this.parentPsId){ //二干局内电路流程-新开、变更、拆机流程
                flowUrl = "module/UnicomLocalNet/resmaster/portal/flowChart/views/secondStationRelayView";
            }else if("10101042" == this.parentPsId){ //二干资源核查-核查流程
                flowUrl = "module/UnicomLocalNet/resmaster/portal/flowChart/views/secondVerCheckView";
            }else if("10101020" == this.parentPsId){ //一干客户、局内电路新开、变更、拆机流程
                flowUrl = "module/UnicomLocalNet/resmaster/portal/flowChart/views/flowFstationView";
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