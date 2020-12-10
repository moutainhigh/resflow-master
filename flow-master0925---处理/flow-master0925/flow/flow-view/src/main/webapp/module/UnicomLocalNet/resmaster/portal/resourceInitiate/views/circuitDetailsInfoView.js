/**
 * 订单详情主JS
 */
define([
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/orderDetailsAction',
    'module/UnicomLocalNet/resmaster/portal/resourceInitiate/action/resourceInitiateAction',
    'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
    'text!module/UnicomLocalNet/resmaster/portal/resourceInitiate/templates/circuitDetailsInfoView.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/orderDetailsView.css'
], function(orderDetailsAction,resourceInitiateAction,orderStandbyAction,orderDetails,i18n,css) {

    var selectTab = 'circuit';//tabl标识
    var changeOrderLabel = "4B";//异常单子标识
    var serviceids ;
    var srvordIds ;
    var URl;
    var id;//资源补录表的主键id
    return fish.View.extend({
        userInfo: new Object(),
        template: fish.compile(orderDetails),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #close': 'close',//电路信息tab页单击事件
            'click #tabs-circuitSub': 'tabsCircuitSub',//电路信息tab页单击事件
            'click #tabs-resourceOrder-w': 'tabsResourceOrderW',//一干资源信息tab页单击事件
            'click #tabs-resourceOrder': 'tabsResourceOrder',//二干资源信息tab页单击事件
            'click #tabs-resourceOrder-y': 'tabsResourceOrderY',//本地资源信息tab页单击事件
            'click #tabs-resourceOrder-sec': 'tabsResourceOrderSec',//二干补录资源信息tab页单击事件
            'click #tabs-resourceOrder-local': 'tabsResourceOrderLocal',//本地补录资源信息tab页单击事件
            'click #resource_initiate': 'resourceInitiate',//发起补录点击事件
            'click #resConfig': 'resConfig',//资源配置
            'click #touchGet_2': 'releaseAndTouchGet', //签收
            'click #releaseTouchGet_2': 'releaseAndTouchGet', //释放签收
            'click #resSubmit': 'resSubmit', //提交
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
            URl=this.getRootPath();
            this.userInfo = orderDetailsAction.queryStaffInfo().responseJSON.data;
            //初始化
            $("#tabsCirNum-pill,#tabs-pill").tabs();
            serviceids = this.options.selrow.SERVICEID+'';
            srvordIds = this.options.selrow.SRVORDID+'';
            id = this.options.id+'';
            this.initButton(this.options.selrow);
            this.tabsCircuitSub();
            //海南、重庆暂时屏蔽二干资源配置
            if('350002000000000042766427' == this.userInfo.areaId
                || '350002000000000042766429' == this.userInfo.areaId){
                $("#tabs-resourceOrder").remove();
                // 同时屏蔽二干补录资源信息
                $("#tabs-resourceOrder-sec").remove();
            }
            if(id=="undefined"){
                $("#tabs-resourceOrder-local").remove();
                $("#tabs-resourceOrder-sec").remove();
            }

        },
        initButton : function (params) {
            if (params.hasOwnProperty('dealFlag')) {
                if(params.dealFlag == 'dealWith'){ //处理中
                    if (this.options.tacheId == '1551002651'){
                        $("#resStartFlow").show();
                        $("#releaseTouchGet_2").show();
                    }else {
                        $("#resConfig").show();
                        $("#resSubmit").show();
                        $("#releaseTouchGet_2").show();
                    }
                }else if(params.dealFlag == 'waitSignFor') { //待签收
                    $("#touchGet_2").show();
                }
            }else {
                $("#resource_initiate").show();
            }
        },
        //初始化一干资源信息表格
        initResourceGridW: function() {
            var me = this;
            debugger
            var resourceOrderInfoW = $.proxy(this.queryResourceOrderInfoW(),this); //函数作用域改变
            $("#resource-grid-w").grid({
                colModel: [
                    //默认展示字段
                    {name: 'HANDLE_DEP', label: '配置分公司', width: 130, sortable: false},
                    {name: 'RESTYPE', label: '资源类型', width: 100, sortable: false},
                    {name: 'RESNAME', label: '资源名称', width: 150, sortable: false},
                    {name: 'BEFORE_ROUTE', label: '调前路由', width: 420, sortable: false},
                    {name: 'AFTER_ROUTE', label: '调后路由', width: 420, sortable: false},
                    //{name: 'ALL_ROUTE', label: '全程路由', width: 350, sortable: false},
                    // {name: 'ROAD_ROUTE', label: '光路路由', width: 150, sortable: false},
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
                pageData: resourceOrderInfoW,
                onDblClickRow: function (e, rowid, iRow, iCol) {//双击行事件
                    me.orderApplyFormView('W');
                },
            });
            $("#resource-grid-w").grid("setGridHeight", 327);
        },
        //初始化二干资源信息表格
        initResourceGrid: function() {
            var me = this;
            var resourceOrderInfo = $.proxy(this.queryResourceOrderInfo(),this); //函数作用域改变
            $("#resource-grid").grid({
                colModel: [
                    //默认展示字段
                    {name: 'HANDLE_DEP', label: '配置分公司', width: 130, sortable: false},
                    {name: 'RESTYPE', label: '资源类型', width: 100, sortable: false},
                    {name: 'RESNAME', label: '资源名称', width: 150, sortable: false},
                    {name: 'BEFORE_ROUTE', label: '调前路由', width: 420, sortable: false},
                    {name: 'AFTER_ROUTE', label: '调后路由', width: 420, sortable: false},
                    //{name: 'ALL_ROUTE', label: '全程路由', width: 350, sortable: false},
                    // {name: 'ROAD_ROUTE', label: '光路路由', width: 150, sortable: false},
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
                pageData: resourceOrderInfo,
                onDblClickRow: function (e, rowid, iRow, iCol) {//双击行事件
                    me.orderApplyFormView('R');
                },
            });
            $("#resource-grid").grid("setGridHeight", 327);
        },
        //初始化本地资源信息表格
        initResourceGridY: function() {
            var me = this;
            debugger
            var resourceOrderInfoY = $.proxy(this.queryResourceOrderInfoY(),this); //函数作用域改变
            $("#resource-grid-y").grid({
                colModel: [
                    //默认展示字段
                    {name: 'HANDLE_DEP', label: '配置分公司', width: 130, sortable: false},
                    {name: 'RESTYPE', label: '资源类型', width: 100, sortable: false},
                    {name: 'RESNAME', label: '资源名称', width: 150, sortable: false},
                    {name: 'BEFORE_ROUTE', label: '调前路由', width: 420, sortable: false},
                    {name: 'AFTER_ROUTE', label: '调后路由', width: 420, sortable: false},
                    //{name: 'ALL_ROUTE', label: '全程路由', width: 350, sortable: false},
                    // {name: 'ROAD_ROUTE', label: '光路路由', width: 150, sortable: false},
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
                pageData: resourceOrderInfoY,
                onDblClickRow: function (e, rowid, iRow, iCol) {//双击行事件
                    me.orderApplyFormView('Y');
                },
            });
            $("#resource-grid-y").grid("setGridHeight", 327);
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
                    me.orderApplyFormView('sec');
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
                    me.orderApplyFormView('local');
                },
            });
            $("#resource-grid-local").grid("setGridHeight", 327);
        },

        orderApplyFormView: function(res){
            var me = this;
            me.completedViewBtn(res);
        },
        completedViewBtn:function(res){
            var selrow;
            if('W' == res){
                selrow = $("#resource-grid-w").grid("getSelection"); //获取选中的行数据
            }else if('R' == res){
                selrow = $("#resource-grid").grid("getSelection"); //获取选中的行数据
            }else if('Y' == res){
                selrow = $("#resource-grid-y").grid("getSelection"); //获取选中的行数据
            }else if('local' == res){
                selrow = $("#resource-grid-local").grid("getSelection"); //获取选中的行数据
            }else if('sec' == res){
                selrow = $("#resource-grid-sec").grid("getSelection"); //获取选中的行数据
            }
            var circuitData = new Object();
            circuitData =  selrow;
            var _this = $(this);
            var options = {
                url: 'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/views/resourceInfoView',
                height: '80%',
                width: "90%",
                modal: false,
                draggable: true,
                resizable:true,
                autoResizable: true,
                viewOption: {
                    flag : "org",
                    selrow : selrow
                },
                callback: function (popup, view) {
                    popup.result.then(function (res) {
                        //_this.popedit('setValue', {name:res.circuitCode, value:res.circuitCode});
                        //$('input[name="serialNumber"]').val(res.serialNumber);
                        //$('#serialNumber').attr('disabled',true);
                        //$('input[name="tradeId"]').val(res.tradeId);
                        //res.serviceName = $("#SERVICE_ID").combobox('text');
                        //$("#gridId").grid("addRowData",1, res, 'last');
                    }, function (e) {
                        console.log('关闭了', e);
                    });
                }
            };
            var popup = fish.popupView(options);
            //circuitData.productType =  this.options.productType;
            //circuitData.circuitCode =  res.productId;
            //this.popup.close(circuitData);
        },
        //查询一干资源信息的方法
        queryResourceOrderInfoW:function(page, rowNum, sortname, sortorder){
            var srvordId = srvordIds;
            rowNum = rowNum || this.$("#resource-grid-w").grid("getGridParam", "rowNum");
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
            $("#resource-grid-w").blockUI({message: '加载中'}).data('blockui-content', true);
            orderDetailsAction.queryResourceOrderInfoW(srvordId,function (data) {
                $("#resource-grid-w").grid("reloadData", data);
                $(window).trigger("resize");
                $("#resource-grid-w").unblockUI().data('blockui-content', false);
            });

        },
        //查询资源信息的方法
        queryResourceOrderInfo:function(page, rowNum, sortname, sortorder){
            var srvordId = srvordIds;
            rowNum = rowNum || this.$("#resource-grid").grid("getGridParam", "rowNum");
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
            $("#resource-grid").blockUI({message: '加载中'}).data('blockui-content', true);
            orderDetailsAction.queryResourceOrderInfo(srvordId,function (data) {
                $("#resource-grid").grid("reloadData", data);
                $("#resource-grid").unblockUI().data('blockui-content', false);
            });
        },
        //查询本地资源信息的方法
        queryResourceOrderInfoY:function(page, rowNum, sortname, sortorder){
            var srvordId = srvordIds;
            rowNum = rowNum || this.$("#resource-grid-y").grid("getGridParam", "rowNum");
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
            $("#resource-grid-y").blockUI({message: '加载中'}).data('blockui-content', true);
            orderDetailsAction.queryResourceOrderInfoY(srvordId,function (data) {
                $("#resource-grid-y").grid("reloadData", data);
                $(window).trigger("resize");
                $("#resource-grid-y").unblockUI().data('blockui-content', false);
            });

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

        //订单详情"每条电路信息"tab页点击事件
        tabsCircuitSub:function(){
            //每次点击"电路信息"tab页，都要清空<div id='order-circuit-info'></div>节点下的东西
            //不然会有重复数据填充
            selectTab = 'circuit';
            $("#otherInfo").empty();
            this.queryCircuitInfo();
        },
        //一干资源信息tab页点击时间
        tabsResourceOrderW:function(){
            selectTab = 'resourceW';
            this.initResourceGridW();
        },
        //二干资源信息tab页点击时间
        tabsResourceOrder:function(){
            selectTab = 'resource';
            this.initResourceGrid();
        },
        //本地资源信息tab页点击时间
        tabsResourceOrderY:function(){
            selectTab = 'resourceY';
            this.initResourceGridY();
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
        //查询电路信息方法
        queryCircuitInfo:function () {
            var srvOrdId = srvordIds;
            var serviceId = serviceids;//this.options.selrow.SRVORDID
            $('#azpeInfo').show();
            $('#otherInfo').empty();
            $('#apeInfo').empty();
            $('#zceInfo').empty();
            orderDetailsAction.queryCircuitInfo(srvOrdId,serviceId, function (data) {
                if (data.success) {
                    var azInfo = data.AZInfo;
                    var otherInfo = data.otherInfo;
                    var peInfo = data.PEInfo;
                    var _circuitAPE = '';
                    var _circuitZCE = '';
                    debugger;
                    if (azInfo.length > 0){
                        for (var i=0; i< azInfo.length; i+=4){
                            _circuitAPE += '<div class="clearfix form-group">';
                            _circuitAPE += '<div class="col-md-6">';
                            _circuitAPE += '<label class="control-label">'+(azInfo[i].PROPERTY_NAME == ""?"":(azInfo[i].PROPERTY_NAME+':'))+'</label>';
                            _circuitAPE += '<div class="form-order-info">';
                            _circuitAPE += (azInfo[i].ATTR_VALUE ==undefined?"" : azInfo[i].ATTR_VALUE);
                            _circuitAPE += '</div>';
                            _circuitAPE += '</div>';
                            _circuitAPE += '<div class="col-md-6">';
                            _circuitAPE += '<label class="control-label">'+(azInfo[i+1].PROPERTY_NAME == ""?"":(azInfo[i+1].PROPERTY_NAME+':'))+'</label>';
                            _circuitAPE += '<div class="form-order-info">';
                            _circuitAPE += (azInfo[i+1].ATTR_VALUE ==undefined?"" : azInfo[i+1].ATTR_VALUE);
                            _circuitAPE += '</div>';
                            _circuitAPE += '</div>';
                            _circuitAPE += '</div>';
                            _circuitZCE += '<div class="clearfix form-group">';
                            _circuitZCE += '<div class="col-md-6">';
                            _circuitZCE += '<label class="control-label">'+(azInfo[i+2].PROPERTY_NAME == ""?"":(azInfo[i+2].PROPERTY_NAME+':'))+'</label>';
                            _circuitZCE += '<div class="form-order-info">';
                            _circuitZCE += (azInfo[i+2].ATTR_VALUE ==undefined?"" : azInfo[i+2].ATTR_VALUE);
                            _circuitZCE += '</div>';
                            _circuitZCE += '</div>';
                            _circuitZCE += '<div class="col-md-6">';
                            _circuitZCE += '<label class="control-label">'+(azInfo[i+3].PROPERTY_NAME == ""?"":(azInfo[i+3].PROPERTY_NAME+':'))+'</label>';
                            _circuitZCE += '<div class="form-order-info">';
                            _circuitZCE += (azInfo[i+3].ATTR_VALUE ==undefined?"" : azInfo[i+3].ATTR_VALUE);
                            _circuitZCE += '</div>';
                            _circuitZCE += '</div>';
                            _circuitZCE += '</div>';

                        }
                        $("#apeInfo").append(_circuitAPE);
                        $("#zceInfo").append(_circuitZCE);
                    }else if(peInfo.length > 0){
                        for (var i = 0; i< peInfo.length; i+=4){
                            _circuitAPE += '<div class="clearfix form-group">';
                            _circuitAPE += '<div class="col-md-6">';
                            _circuitAPE += '<label class="control-label">'+(peInfo[i].PROPERTY_NAME == ""?"":(peInfo[i].PROPERTY_NAME+':'))+'</label>';
                            _circuitAPE += '<div class="form-order-info">';
                            _circuitAPE += (peInfo[i].ATTR_VALUE ==undefined? "" : peInfo[i].ATTR_VALUE);
                            _circuitAPE += '</div>';
                            _circuitAPE += '</div>';
                            _circuitAPE += '<div class="col-md-6">';
                            _circuitAPE += '<label class="control-label">'+(peInfo[i+1].PROPERTY_NAME == ""?"":(peInfo[i+1].PROPERTY_NAME+':'))+'</label>';
                            _circuitAPE += '<div class="form-order-info">';
                            _circuitAPE += (peInfo[i+1].ATTR_VALUE ==undefined?"" : peInfo[i+1].ATTR_VALUE);
                            _circuitAPE += '</div>';
                            _circuitAPE += '</div>';
                            _circuitAPE += '</div>';
                            _circuitZCE += '<div class="clearfix form-group">';
                            _circuitZCE += '<div class="col-md-6">';
                            _circuitZCE += '<label class="control-label">'+(peInfo[i+2].PROPERTY_NAME == ""?"":(peInfo[i+2].PROPERTY_NAME+':'))+'</label>';
                            _circuitZCE += '<div class="form-order-info">';
                            _circuitZCE += (peInfo[i+2].ATTR_VALUE ==undefined? "" : peInfo[i+2].ATTR_VALUE);
                            _circuitZCE += '</div>';
                            _circuitZCE += '</div>';
                            _circuitZCE += '<div class="col-md-6">';
                            _circuitZCE += '<label class="control-label">'+(peInfo[i+3].PROPERTY_NAME == ""?"":(peInfo[i+3].PROPERTY_NAME+':'))+'</label>';
                            _circuitZCE += '<div class="form-order-info">';
                            _circuitZCE += (peInfo[i+3].ATTR_VALUE ==undefined?"" : peInfo[i+3].ATTR_VALUE);
                            _circuitZCE += '</div>';
                            _circuitZCE += '</div>';
                            _circuitZCE += '</div>';
                        }
                        $("#apeInfo").append(_circuitAPE);
                        $("#zceInfo").append(_circuitZCE);
                    }
                    if(_circuitAPE == ''&&_circuitZCE == ''){
                        $('#azpeInfo').hide();
                    }
                    var _circuita = '';
                    if (otherInfo.length >0){
                        for (var i = 0; i < otherInfo.length; i+=4){
                            _circuita += '<div class="clearfix">';
                            _circuita += '<div class="col-md-3 pdb-10">'+
                                '<label class="control-label">'+(otherInfo[i].PROPERTY_NAME == ""?"":(otherInfo[i].PROPERTY_NAME+':'))+'</label>'+
                                '<div  class="form-order-info">'+(otherInfo[i].ATTR_VALUE ==undefined?"" : otherInfo[i].ATTR_VALUE)+
                                '</div></div>';
                            _circuita += '<div class="col-md-3 pdb-10">'+
                                '<label class="control-label">'+(otherInfo[i+1].PROPERTY_NAME == ""?"":(otherInfo[i+1].PROPERTY_NAME+':'))+'</label>'+
                                '<div  class="form-order-info">'+(otherInfo[i+1].ATTR_VALUE ==undefined?"" : otherInfo[i+1].ATTR_VALUE)+
                                '</div></div>';
                            _circuita += '<div class="col-md-3 pdb-10">'+
                                '<label class="control-label">'+(otherInfo[i+2].PROPERTY_NAME == ""?"":(otherInfo[i+2].PROPERTY_NAME+':'))+'</label>'+
                                '<div  class="form-order-info">'+(otherInfo[i+2].ATTR_VALUE ==undefined?"" : otherInfo[i+2].ATTR_VALUE)+
                                '</div></div>';
                            _circuita += '<div class="col-md-3 pdb-10">'+
                                '<label class="control-label">'+(otherInfo[i+3].PROPERTY_NAME == ""?"":(otherInfo[i+3].PROPERTY_NAME+':'))+'</label>'+
                                '<div  class="form-order-info">'+(otherInfo[i+3].ATTR_VALUE ==undefined?"" : otherInfo[i+3].ATTR_VALUE)+
                                '</div></div>';
                            _circuita += '</div>';
                        }
                        $("#otherInfo").append(_circuita);
                    }
                }else{
                    fish.toast('error', data.message);
                }
            });
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

        close: function() {
            this.popup.close();
        },
        //发起补录
        resourceInitiate:function(){
            //判断电路是否有在途单和电路的状态
            var me = this;
            var srvOrdInfoList = this.options.srvOrdInfo;
            var srvOrdFlag = false; //是否有在途单的标识，false:没有，true:有
            var applyOrdId = '';//如果有在途单，在途单的申请编号
            for (var i = 0; i < srvOrdInfoList.length; i++){
                var srvOrdState = srvOrdInfoList[i].SRV_ORD_STAT;
                if (srvOrdState != '10F' && srvOrdState != '10X'){
                    srvOrdFlag = true;
                    if (applyOrdId == ''){
                        applyOrdId =  srvOrdInfoList[i].APPLY_ORD_ID;
                    }else{
                        applyOrdId = applyOrdId + ';' + srvOrdInfoList[i].APPLY_ORD_ID;
                    }
                }
            }
            //如果电路业务状态为关闭，不允许发起补录
            if (this.options.checkData.oprStateId == '170010') {
                fish.info("电路状态已关闭，不允许发起补录！");
                return;
            }else if(this.options.checkData.oprStateId == '170044'){
                fish.info("电路状态已停机，不允许发起补录！");
                return;
            }else if (this.options.systemResource == 'second-schedule-lt') {
                fish.info("该条电路属于二干电路，请在二干调度发起补录！");
                return;
            } else if (srvOrdFlag) {
                fish.info("该条电路有在途单未完成，不允许发起补录，在途单号为：" + applyOrdId);
                return;
            }else{
                //判断该电路是否有未完成的资源补录单
                var param = {};
                param.instanceId = this.options.checkData.prodInstId;
                resourceInitiateAction.queryResourceInitiateInfoByInstanceId(param, function(res){
                    if (res.success){
                        if (res.flag){ //有未完成资源补录单
                            fish.info("该条电路有未完成的资源补录单，不允许再次发起资源补录！");
                            return;
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
                                    srvOrdId : me.options.selrow.SRVORDID,
                                    serviceId : me.options.selrow.SERVICEID,
                                    checkData: me.options.checkData,
                                    custInfo : me.options.selrow,
                                    flag : 'flowFlag',
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
                        return;
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
                    srvOrdId : me.options.selrow.SRVORDID,
                    serviceId : me.options.selrow.SERVICEID,
                    checkData: me.options.checkData,
                    flag : 'flowFlag',
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
}); //ALL END