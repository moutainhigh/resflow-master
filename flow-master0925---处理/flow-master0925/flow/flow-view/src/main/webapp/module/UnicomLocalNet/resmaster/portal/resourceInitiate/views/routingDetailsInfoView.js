/**
 * 路由信息
 */
define([
    'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/action/localOrderSelectAction',
    'module/UnicomLocalNet/resmaster/portal/resourceInitiate/action/resourceInitiateAction',
    'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/operOrderAction',
    'text!module/UnicomLocalNet/resmaster/portal/resourceInitiate/templates/routingDetailsInfoView.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/resourceInitiate/styles/resourceInitiateView.css'
], function(localOrderSelectAction,resourceInitiateAction,orderStandbyAction,operOrderAction,routingInfoView,i18n,css) {

    var selectTab = 'circuit';//tabl标识
    var id;//资源补录表的主键id
    return fish.View.extend({
        resNetworkUrl: '',
        crmRegion: '',
        template: fish.compile(routingInfoView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #close': 'close',
            'click #tabs-circuitSub': 'tabsCircuitSub',//电路信息tab页单击事件
            'click #tabs-resourceOrder-sec': 'tabsResourceOrderSec',//二干补录资源信息tab页单击事件
            'click #tabs-resourceOrder-local': 'tabsResourceOrderLocal',//本地补录资源信息tab页单击事件
            'click #touchGet_1': 'releaseAndTouchGet', //签收
            'click #releaseTouchGet_1': 'releaseAndTouchGet', //释放签收
            'click #resource_initiate': 'resourceIniaiate',//发起补录
            'click #resConfig': 'resConfig',//资源配置
            'click #resSubmit': 'resSubmit',//提交
            'click #resStartFlow': 'resStartFlow'//发起流程
        },
        initialize: function() {
            this.render();
        },
        //渲染页面
        render: function() {
            this.$el.html(this.template(this.i18nData));
        },
        afterRender: function() {
            id = this.options.id+'';
            //初始化
            $("#tabsCirNum-pill,#tabs-pill").tabs();
            if(id=="undefined"){
                $("#tabs-resourceOrder-local").remove();
                $("#tabs-resourceOrder-sec").remove();
            }
            this.initButton();
            //初始化grid
            this.initGrid();
            //初始化页面信息

            this.tabsCircuitSub();
            //海南、重庆暂时屏蔽二干资源配置
            if('350002000000000042766427' == this.userInfo.areaId
                || '350002000000000042766429' == this.userInfo.areaId){
                // 同时屏蔽二干补录资源信息
                $("#tabs-resourceOrder-sec").remove();
            }
        },

        initButton : function () {
            if (this.options.hasOwnProperty('dealFlag')) {
                if(this.options.dealFlag == 'dealWith'){ //处理中
                    if (this.options.tacheId == '1551002651'){
                        $("#resStartFlow").show();
                        $("#releaseTouchGet_1").show();
                    }else {
                        $("#resConfig").show();
                        $("#resSubmit").show();
                        $("#releaseTouchGet_1").show();
                    }
                }else if(this.options.dealFlag == 'waitSignFor'){ //待签收
                    $("#touchGet_1").show();
                }
            }else {
                $("#resource_initiate").show();
            }
        },

        initResConfigData : function () {
             var me = this;
            if (!me.options.hasOwnProperty('checkData')) {
                var param = {};
                param.circuitCode = me.options.circuitCode;//电路编号
                param.productType = me.options.productType;//产品类型
                param.accNbr = me.options.accNbr;//业务号码
                param.isToBeQuery = '1';// 只查询
                resourceInitiateAction.queyCircuitInfoFromResource(param,function(res){
                    if (res.success){
                        me.options.checkData = res.data.rows[0];
                    }else{
                        console.log(res.msg);
                        fish.toast("warn", "查询不到数据，请确认输入内容是否正确或联系管理员！");
                    }
                });
                /*var resData = resourceInitiateAction.queyCircuitInfoFromResource(param).responseJSON.data;
                me.options.checkData = resData.data.rows[0];*/
            }
        },

        initGrid:function(){
            var me = this;
            $("#routeGrid").grid({
                colModel: [
                    //默认展示字段
                    {name: 'ROUTE_NO',width:300,label:'路由编号' },
                    {name: 'ROUTE_NAME',width:300,label:'路由名称', align: 'left' },
                    {name: 'ROUTE_TYPE',width:300,label:'路由类型', align: 'left'}
                ],
                curPageSort: false,
                height:240,
                // datatype: 'json',
                recordtext:"{0}-{1} 共{2}条",
                pgtext: " 第{0}页/共{1}页",
                rowtext: "每页{0}条",
                rowNum: 10,
                rowList: [10,20,50,100,200,500],
                pager: true,
                gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                multiselect: false,
                shrinkToFit: false,
                autoResizable: true,
                showColumnsFeature: false, //允许用户自定义列展示设置
                cached: true, //把用户自定义的列展示设置缓存在本地
                pageData: function (e, rowid, iRow, iCol) {
                    me.queryRouteInfo(e, rowid, iRow, iCol);
                }.bind(this),
            });
        },
        initRouteInfo:function(){
            //填充grid
            $('#routeGrid').grid("reloadData", this.options.res.routeInfo);
            //填充form
            this.insertPropertyForm(this.options.res.propertyInfo);
        },
        close: function() {
            this.popup.close();
        },
        insertPropertyForm:function(param){
            document.getElementById("CUST_NAME").innerText=param.CUST_NAME;
            document.getElementById("LINK_TELE").innerText=param.LINK_TELE;
            document.getElementById("ADDRESS").innerText=param.ADDRESS;
            document.getElementById("LINK_MAN").innerText=param.LINK_MAN;
            document.getElementById("LINK_TELE2").innerText=param.LINK_TELE;
            document.getElementById("PRODUCT_NO").innerText=param.PRODUCT_NO;
            document.getElementById("BUSINESS_IDENTITY").innerText=param.BUSINESS_IDENTITY;
            document.getElementById("CIRCUIT_NO").innerText=param.CIRCUIT_NO;
            document.getElementById("CIRCUIT_RATE").innerText=param.CIRCUIT_RATE;
            document.getElementById("A_RESISTANCE").innerText=param.A_RESISTANCE;
            document.getElementById("A_ADDRESS").innerText=param.A_ADDRESS;
            document.getElementById("LONG_INTER_PORT").innerText=param.LONG_INTER_PORT;
            document.getElementById("A_LINK_MAN").innerText=param.A_LINK_MAN;
            document.getElementById("A_LINK_TELE").innerText=param.A_LINK_TELE;
            document.getElementById("SYS_EMS_NAME").innerText=param.SYS_EMS_NAME;
            document.getElementById("Z_ADDRESS").innerText=param.Z_ADDRESS;
            document.getElementById("SYS_NETWORK_GW").innerText=param.SYS_NETWORK_GW;
            document.getElementById("Z_LINK_MAN").innerText=param.Z_LINK_MAN;
            document.getElementById("Z_LINK_TELE").innerText=param.Z_LINK_TELE;
        },
        //订单详情"每条电路信息"tab页点击事件
        tabsCircuitSub:function(){
            selectTab = 'circuit';
            this.initResConfigData();
            this.initRouteInfo();
        },
        //二干补录资源信息tab页点击事件
        tabsResourceOrderSec:function(){
            selectTab = 'resourceSec';
            this.initResourceGridSec();
        },
        //本地补录资源信息tab页点击事件
        tabsResourceOrderLocal:function(){
            selectTab = 'resourceLocal';
            this.initResourceGridLocal();
        },
        //初始化二干补录资源信息表格
        initResourceGridSec: function() {
            var me = this;
            var resourceOrderInfoSec = $.proxy(this.queryResourceOrderInfoSec(),this); //函数作用域改变
            $("#resource-grid-sec").grid({
                colModel: [
                    //默认展示字段
                    {name: 'HANDLE_DEP', label: '配置分公司', width: 130, sortable: false},
                    {name: 'RESTYPE', label: '资源类型', width: 100, sortable: false},
                    {name: 'RESNAME', label: '资源名称', width: 150, sortable: false},
                    {name: 'BEFORE_ROUTE', label: '调前路由', width: 420, sortable: false},
                    {name: 'AFTER_ROUTE', label: '调后路由', width: 420, sortable: false},
                ],
                autowidth: true,
                rowNum: 10,
                rowList: [10,15,20,50],
                pager: true,
                gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                multiselect: false,
                shrinkToFit: false,
                autoResizable: true,
                cached: true, //把用户自定义的列展示设置缓存在本地
                pageData: resourceOrderInfoSec,
                onDblClickRow: function (e, rowid, iRow, iCol) {//双击行事件
                    me.orderApplyFormView('R');
                },
            });
            $("#resource-grid-sec").grid("setGridHeight", 327);
        },
        //初始化本地补录资源信息表格
        initResourceGridLocal: function() {
            var me = this;
            debugger
            var resourceOrderInfoLocal = $.proxy(this.queryResourceOrderInfoLocal(),this); //函数作用域改变
            $("#resource-grid-local").grid({
                colModel: [
                    //默认展示字段
                    {name: 'HANDLE_DEP', label: '配置分公司', width: 130, sortable: false},
                    {name: 'RESTYPE', label: '资源类型', width: 100, sortable: false},
                    {name: 'RESNAME', label: '资源名称', width: 150, sortable: false},
                    {name: 'BEFORE_ROUTE', label: '调前路由', width: 420, sortable: false},
                    {name: 'AFTER_ROUTE', label: '调后路由', width: 420, sortable: false},
                ],
                autowidth: true,
                rowNum: 10,
                rowList: [10,15,20,50],
                pager: true,
                gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                multiselect: false,
                shrinkToFit: false,
                autoResizable: true,
                cached: true, //把用户自定义的列展示设置缓存在本地
                pageData: resourceOrderInfoLocal,
                onDblClickRow: function (e, rowid, iRow, iCol) {//双击行事件
                    me.orderApplyFormView('Y');
                },
            });
            $("#resource-grid-local").grid("setGridHeight", 327);
        },
        //查询二干补录资源信息的方法
        queryResourceOrderInfoSec:function(page, rowNum, sortname, sortorder){
            var srvordId = id;
            rowNum = rowNum || this.$("#resource-grid-sec").grid("getGridParam", "rowNum");
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
            $("#resource-grid-sec").blockUI({message: '加载中'}).data('blockui-content', true);
            resourceInitiateAction.queryResourceOrderInfoSec(srvordId,function (data) {
                $("#resource-grid-sec").grid("reloadData", data);
                $("#resource-grid-sec").unblockUI().data('blockui-content', false);
            });
        },
        //查询本地补录资源信息的方法
        queryResourceOrderInfoLocal:function(page, rowNum, sortname, sortorder){
            var srvordId = id;
            rowNum = rowNum || this.$("#resource-grid-local").grid("getGridParam", "rowNum");
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
            $("#resource-grid-local").blockUI({message: '加载中'}).data('blockui-content', true);
            resourceInitiateAction.queryResourceOrderInfoLocal(srvordId,function (data) {
                $("#resource-grid-local").grid("reloadData", data);
                $(window).trigger("resize");
                $("#resource-grid-local").unblockUI().data('blockui-content', false);
            });

        },

        //发起补录
        resourceIniaiate:function(){
            //判断电路状态，状态为已关闭，不允许补录
            var me = this;
            if (this.options.checkData.oprStateId == '170010') {
                fish.info("电路状态已关闭，不允许发起补录！");
                return;
            }else if (this.options.checkData.oprStateId == '170044'){
                fish.info("电路状态已停机，不允许发起补录！")
                return;
            }else{
                //判断该电路是否有未完成资源补录单
                var param = {};
                param.instanceId = this.options.checkData.prodInstId;
                resourceInitiateAction.queryResourceInitiateInfoByInstanceId(param, function(res){
                    if(res.success){
                        if (res.flag){
                            fish.info("该电路有未完成资源补录单，不允许再次发起补录！");
                        }else{
                            fish.popupView({
                                url: 'module/UnicomLocalNet/resmaster/portal/resourceInitiate/views/initiateDealView',
                                height: '90%',
                                width: "80%",
                                modal: false,
                                draggable: true,
                                resizable:true,
                                autoResizable: true,
                                viewOption: {
                                    gridData : me.options.res,
                                    checkData: me.options.checkData,
                                    flag: 'routingFlag',
                                    startOrSupp : 'supp' //发起流程还是补录的标识
                                },
                                callback: function (popup, view) {
                                    popup.result.then(function (res) {
                                        me.popup.close();
                                    }, function (e) {
                                        console.log('关闭了', e);
                                    });
                                }
                            });
                        }
                    }else{
                        fish.info(res.msg);
                    }
                });
            }
        },
        //发起流程
        resStartFlow:function(){
            //判断电路状态，状态为已关闭，不允许补录
            var me = this;
            fish.popupView({
                url: 'module/UnicomLocalNet/resmaster/portal/resourceInitiate/views/initiateDealView',
                height: '90%',
                width: "80%",
                modal: false,
                draggable: true,
                resizable:true,
                autoResizable: true,
                viewOption: {
                    woId : me.options.woId,
                    orderId : me.options.orderId,
                    tacheId : me.options.tacheId,
                    gridData : me.options.res,
                    checkData: me.options.checkData,
                    flag: 'routingFlag',
                    startOrSupp : 'start' //发起流程还是补录的标识
                },
                callback: function (popup, view) {
                    popup.result.then(function (res) {
                        me.popup.close();
                    }, function (e) {
                        console.log('关闭了', e);
                    });
                }
            });
        },
        // 资源配置
        resConfig : function(){
            var me = this;
            var params = new Object();
            params.tacheId = me.options.tacheId;
            params.orderId = me.options.orderId;
            params.woId = me.options.woId;
            resourceInitiateAction.resConfig(params,function(res){
                if(res.success){
                    if ($("#ExportData").size() < 1) {
                        var formHtml = "<form id=\"ExportData\" action=\"http://www.oschina.net\" target=\"_blank\" method=\"post\">"
                            + "<input type=\"hidden\" id=\"params\" name=\"body\"/>"
                            + "</form>";
                        $(document.body).append($(formHtml));
                    }
                    var tempForm = document.getElementById("ExportData");
                    tempForm.action = res.url;
                    var paramsInput = document.getElementById("params");
                    paramsInput.value =  res.json;
                    tempForm.submit();
                } else {
                    fish.info(res.message);
                    return;
                }
            });

        },
        //提交
        resSubmit: function () {
            var me = this;
            var pop = fish.popupView({
                url: "module/UnicomLocalNet/resmaster/portal/resourceInitiate/views/resSubmitView",
                width: "70%",
                height: "50%",
                title: "工单详情",
                modal: false,
                viewOption: {
                    woId : me.options.woId,
                    orderId : me.options.orderId,
                    tacheId : me.options.tacheId
                },
                callback: function (popup, view) {
                    popup.result.then(function (e) {
                        me.popup.close();
                    }, function (e) {
                        console.log('关闭了', e);
                    });
                }
            })
        },

        //签收和释放签收
        releaseAndTouchGet: function (e) {
            var me = this;
            var actionType = e.toElement.value;
            var woOrderIds = new Array();
            woOrderIds.push(me.options.woId);
            var queryParams = new Object();
            queryParams.woOrderIds = woOrderIds;
            queryParams.actionType = actionType;
            if (actionType == 'get'){
                orderStandbyAction.getFreeWoOrder(queryParams, function (data) {
                    if (data.success) {
                        fish.toast("warn", data.message);
                        me.popup.close();
                    } else {
                        fish.toast("warn", data.message);
                    }
                });
            } else if (actionType == 'free'){
                orderStandbyAction.getFreeWoOrder(queryParams, function (data) {
                    if (data.success) {
                        fish.toast("warn", data.message);
                        me.popup.close();
                    } else {
                        fish.toast("warn", data.message);
                    }
                });
            }
        }
    });
});