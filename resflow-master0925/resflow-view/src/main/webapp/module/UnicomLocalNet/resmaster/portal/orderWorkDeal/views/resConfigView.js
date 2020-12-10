define(['text!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/templates/resConfigView.html',
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/operOrderAction',
    'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/orderDetailsAction',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/operOrderView.css'
], function(operOrderView,operOrderAction,orderStandbyAction,orderDetailsAction,i18n,css) {
    var userInfo;

    return fish.View.extend({
        template: fish.compile(operOrderView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #manualBtn': 'config',
            'click #manualUpdateBtn': 'configUpdate',
        //    'click #resourcesBackBtn': 'configBack', //回滚
         //   'click #closeCircuitBtn': 'closeCircuit' //关闭电路
        },

        initialize: function() {
            this.render();
            qryCircuitParams = {};
            userInfo = orderStandbyAction.queryStaffInfo().responseJSON.data; //用户信息
        },

        //渲染页面
        render: function() {
            var titleObj = new Object();
            var me = this;
            /*var teachIds = '500001155,500001157,510101041,510101048,510101081,1051002591';// 资源分配
            var secTeaIds = '510101040,510101080,1051002590';// 二干调度
            if (teachIds.indexOf(me.options.tacheId) != -1 || secTeaIds.indexOf(me.options.tacheId) != -1)*/
            me.options.tacheId == '500001157' || me.options.tacheId == '500001155'
            if (me.options.psId == '10101042' ){ // 二干核查流程
                titleObj.TITLE = '资源核查';
                titleObj.NOTCONFIGGRID = '未核查电路信息：';
                titleObj.CONFIG = '资源核查';
                titleObj.CLASE = '关闭电路';
                titleObj.CONFIGGRID = '已核查电路信息：';
                titleObj.CONFIGUPDATE = '资源核查修改';
                titleObj.CONFIGBACK = '核查回滚';
            } else {
                titleObj.TITLE = '资源配置';
                titleObj.NOTCONFIGGRID = '未配置电路信息：';
                titleObj.CONFIG = '资源配置';
                titleObj.CLASE = '关闭电路';
                titleObj.CONFIGGRID = '已配置电路信息：';
                titleObj.CONFIGUPDATE = '资源配置修改';
                titleObj.CONFIGBACK = '资源回滚';
            }
            this.$el.html(this.template({
                TITLE : titleObj.TITLE,
                NOTCONFIGGRID : titleObj.NOTCONFIGGRID,
                CONFIG : titleObj.CONFIG,
          //      CLASE : titleObj.CLASE,
                CONFIGGRID : titleObj.CONFIGGRID,
                CONFIGUPDATE : titleObj.CONFIGUPDATE
            //    CONFIGBACK : titleObj.CONFIGBACK
            }));
        },

        //初始化fish组件
        afterRender: function() {
            var me = this;
            //初始化电路信息
            this.circuitInfo();
            //定时刷新
            var refreshCircuit = setInterval(function refresh() {
                me.frush();
            }, 10000); //10s刷新一次
            $(".close").on('click',function () {
                clearInterval(refreshCircuit);
            })
        },
        config:function (e) {
            var circuitData = $("#circuitGrid").grid("getCheckRows");//订单信息
            this.configSubmit(e, circuitData);
        },
        configUpdate:function (e) {
            var circuitData = $("#circuitConfigGrid").grid("getCheckRows");//订单信息
            this.configSubmit(e, circuitData);
        },
        configSubmit:function (e, circuitData) {
            var me = this;
            var params = new Object();
            params.cstOrdId = me.options.cstOrdId;
            if (circuitData.length == 0) {
                fish.warn('请选择一条电路信息！');
                return;
            }
            if (circuitData.length != 1) {
                fish.warn('请选择一条电路信息！');
                return false;
            }
            params.circuitData = circuitData; //订单信息
            params.action = "resConfig";
            $("#resConfigView").blockUI({message: '资源配置...'}).data('blockui-content', true);
            me.submitOrder(params);
        },
        submitOrder : function(params){
            var me = this;
            operOrderAction.submitOrder(params, function (res) {
                if (res.success) {
                    $("#resConfigView").unblockUI().data('blockui-content', false);
                    if (params.action == "resConfig") {
                        if ($("#ExportData").size() < 1) {
                            var formHtml = "<form id=\"ExportData\" action=\"http://www.oschina.net\" target=\"_blank\" method=\"post\">"
                                + "<input type=\"hidden\" id=\"params\" name=\"body\"/>"
                                + "</form>";
                            $(document.body).append($(formHtml));
                        }
                        var tempForm = document.getElementById("ExportData");
                        tempForm.action = res.url;
                        var paramsInput = document.getElementById("params");
                        paramsInput.value = res.json;
                        tempForm.submit();
                    }
                } else {
                    fish.toast('error', res.message);
                }
            });
        },
        frush: function(){
            this.qrycircuitInfo("0");
            this.qrycircuitInfo("1");
        },
        // 初始化电路信息表
        circuitInfo : function() {
            var me = this;
            var type = '0';
            $("#circuitGrid").grid({
                colModel: me.initGridInfo(),
                width: 516,
                multiselect: true,
                shrinkToFit: false,
                pageData: me.qrycircuitInfo(type)
            });
            $("#circuitConfigGrid").grid({
                colModel: me.initGridInfo(),
                width: 516,
                multiselect: true,
                shrinkToFit: false,
                pageData: me.qrycircuitInfo('1')
            });
            // 设置表格高度
            $("#circuitGrid, #circuitConfigGrid").grid("setGridHeight", 150);
        },
         qrycircuitInfo : function(type){
            var me = this;
            var psIds = '10101043'; //子流程
            var psId = me.options.psId;
            var specialtyCode = me.options.specialtyCode;
            var regionId = me.options.regionId;
            var param = {};
            param.cstOrdId = me.options.cstOrdId +'';
            param.orderId = me.options.orderId +'';
            param.woIds = me.options.woIds +'';
            param.srvOrdId = me.options.srvOrdId + '';
            param.woState = me.options.woState +'';
            param.tacheId = me.options.tacheId +'';
            param.dealUserId = userInfo.userId +'';
            param.dispObjTyeValue = me.options.dispObjTyeValue +'';
            param.dispObjTye = me.options.dispObjTye +'';
            param.btnFlag = me.options.btnFlag +'';
            param.type = type +'';
            if (psIds.indexOf(psId) != -1){
                param.specialtyCode = specialtyCode;
                param.regionId = regionId;
                operOrderAction.qrySrvOrdChildList(param,function (res) {
                    if(res.flag == 1){
                        if(type=="0"){
                            $("#circuitGrid").grid("reloadData", res.data);
                        } else if(type=="1"){
                            $("#circuitConfigGrid").grid("reloadData", res.data);
                        }
                    }else {
                        fish.toast('error', res.message);
                    }
                });
            }else {
                operOrderAction.qrySrvOrdList(param,function (res) {
                    if(res.flag == 1){
                        if(type=="0"){
                            $("#circuitGrid").grid("reloadData", res.data);
                        } else if(type=="1"){
                            $("#circuitConfigGrid").grid("reloadData", res.data);
                        }
                    }else {
                        fish.toast('error', res.message);
                    }
                });
            }
            $(window).trigger("resize");
        },

        initGridInfo:function(){
                return [
                    {name: 'CIRCUITCODE', label: '电路编号', width: 95, sortable: false },
                    {name: 'TRADE_ID', label: '业务订单号', width: 100 },
                    {name: 'ORDER_ID', label: '流程订单号', width: 100 , hidden: true },
                    {name: 'SERIAL_NUMBER', label: '业务号码', width: 100 , sortable: false },
                    {name: 'AREGIONNAME', label: 'A端所属区域', width: 110, sortable: false},
                    {name: 'ZREGIONNAME', label: 'Z端所属区域', width: 110, sortable: false},
                    {name: 'A_INSTALLED_ADD', label: 'A端装机地址', width: 100 , sortable: false },
                    {name: 'Z_INSTALLED_ADD', label: 'Z端装机地址', width: 100 , sortable: false }
                ]
        },
    });
});