/**
 * 订单详情主JS
 */
define([
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/orderDetailsAction',
    'module/UnicomLocalNet/resmaster/portal/orderAbnormal/action/orderAppendAction',
    'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
    'text!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/templates/relateOrderDetailsView.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/orderDetailsView.css'
], function(orderDetailsAction,orderAppendAction,orderStandbyAction,relateOrderDetails,i18n,css) {

    var selectTab = 'circuit';//tabl标识
    var changeOrderLabel = "4B";//异常单子标识
    var sysResource;
    var URl;
    return fish.View.extend({
        resNetworkUrl: '',
        crmRegion: '',
        template: fish.compile(relateOrderDetails),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #tabs-relate-order': 'tabsOrder',//定单详情tab页单击事件
            'click #tabs-relate-circuit': 'tabsCircuit',//电路信息tab页单击事件
            'click #tabs-relate-circuitSub': 'tabsCircuitSub',//电路信息tab页单击事件
            'click #tabs-relate-attach': 'tabsAttach',//附件信息tab页单击事件
            'click #tabs-relate-log': 'tabsLog',//日志信息tab页单击事件
            'click #tabs-relate-changeorder': 'tabsChangeOrder',//异常单信息tab页单击事件
            'click #tabs-relate-task-second': 'tabsSecondTask',//二干任务信息tab页单击事件
            'click #tabs-relate-task-local': 'tabsLocalTask',//本地任务信息tab页单击事件
            'click #tabs-relate-flow': 'tabsFlow',//流程图tab页单击事件
            'click #tabs-relate-idea': 'tabsIdea',//阶段意见tab页单击事件
            // 'click #tabs-relate-relevance': 'tabsRelevance',//关联主/子单tab页单击事件
            'click #tabs-relate-warning': 'tabsWarning',//预警超时tab页单击事件
            'click #tabs-relate-dispatchOrder': 'tabsDispatchOrder',//调单tab页单击事件
            'click #tabs-relate-resourceOrder-w': 'tabsResourceOrderW',//一干资源信息tab页单击事件
            'click #tabs-relate-resourceOrder': 'tabsResourceOrder',//二干资源信息tab页单击事件
            'click #tabs-relate-resourceOrder-y': 'tabsResourceOrderY',//本地资源信息tab页单击事件
            'click #tabs-relate-feedback': 'tabsFeedbackOrder',//反馈信息tab页单击事件
            'click #tabs-relate-product': 'tabsAddProduct',//附加产品tab页单击事件
            'click #tabs-relate-otherInfo': 'tabsOrderOtherInfo',//反馈信息tab页单击事件
            'click .downloadClFile': 'downloadClFile',//反馈信息tab页单击事件
            'click .downloadFile': 'downloadFile',//异常单附件下载
            //'click .close': 'closeClick',//页面关闭事件
            // 'click #tabs-relate-change-a': 'tabsChangeA',//异常单加急点击时间
            // 'click #tabs-relate-change-b': 'tabsChangeB',//异常单延时申请点击时间
            /*'click #tabs-relate-change-c': 'tabsChangeC',//异常单追单点击时间*/
            // 'click #tabs-relate-change-d': 'tabsChangeD',//异常单撤单点击时间
            // 'click #tabs-relate-change-e': 'tabsChangeE',//异常单挂起点击时间
            // 'click .downLoadAttach': 'downLoadAttach',//
            'click #cc_confirm': 'cc_confirm',//抄送单确认
        },
        initialize: function() {
            this.render();
            var FLAG = '';
        },
        //渲染页面
        render: function() {
            this.$el.html(this.template(this.i18nData));
        },
        afterRender: function() {
            URl=this.getRootPath();
            //初始化定单详情、异常单信息tab页
            $("#tabs-relate-pill,#tabsCirNum-pill,#changeOrder-tabs").tabs();
            var productTyep = this.options.serviceId;
            if (productTyep == "20181221006") {
                $('#consumer-info').hide();
                $('.subScribe').hide();
            }
            $("#tabs-relate-order").click();
            var orderId = this.options.orderId +'';
            FLAG = orderDetailsAction.ifFromSecond(orderId).responseJSON.data;
            //初始化按钮
            this.initButton();
            this.initFishInfo();
            this.hideSecTask();
            //初始化异常通知
            this.initExceptionTab();
        },
        initFishInfo : function() {
            var me = this;
            var woOrderBackFlags = me.options.woOrderBackFlags;
            if (woOrderBackFlags != '' && '1'.indexOf(woOrderBackFlags) != -1){
                $('#ifBackOrder').show();
            }
        },
        //隐藏二干调度的任务，以及资源信息展示的显示和隐藏
        hideSecTask: function (){
            if (FLAG || 'second-schedule-lt' == sysResource.SYSTEM_RESOURCE) {
                $("#tabs-relate-task-second").css({display:""}); //二干任务列表
            }else {
                $("#tabs-relate-task-second").css({display:"none"});
            }
            var resourceName = this.options.RESOURCES;
            if('onedry' == resourceName){
                $('#tabs-relate-otherInfo').attr("style","display:block;"); //对端省份信息
                $('#tabs-relate-resourceOrder-w').attr("style","display:block;"); //一干资源信息
            }else if('secondary' == resourceName){
                if('onedry' == sysResource.RESOURCES){ //一干下给二干，二干再下发本地
                    $('#tabs-relate-otherInfo').attr("style","display:block;"); //对端省份信息
                    $('#tabs-relate-resourceOrder-w').attr("style","display:block;"); //一干资源信息
                    $('#tabs-relate-resourceOrder').attr("style","display:block;"); //二干资源信息
                }else {
                    $('#tabs-relate-otherInfo').attr("style","display:none;"); //对端省份信息
                    $('#tabs-relate-resourceOrder-w').attr("style","display:none;"); //一干资源信息
                    $('#tabs-relate-resourceOrder').attr("style","display:block;"); //二干资源信息
                }
            }else if('jike' == resourceName){
                $('#tabs-relate-otherInfo').attr("style","display:none;"); //对端省份信息
                $('#tabs-relate-resourceOrder-w').attr("style","display:none;"); //一干资源信息
                if (FLAG || 'second-schedule-lt' == sysResource.SYSTEM_RESOURCE) {
                    $('#tabs-relate-resourceOrder').attr("style","display:block;"); //二干资源信息
                }else {
                    $('#tabs-relate-resourceOrder').attr("style","display:none;"); //二干资源信息
                }
            }else{
                $('#tabs-relate-otherInfo').attr("style","display:none;"); //对端省份信息
                $('#tabs-relate-resourceOrder-w').attr("style","display:none;"); //一干资源信息
                $('#tabs-relate-resourceOrder').attr("style","display:none;"); //二干资源信息
            };


        },
        //初始化异常通知
        initExceptionTab: function () {
            // 异常的订单显示异常通知tab页
            if ('exceptionOrder' == this.options.buttonState) {
                $("#tabs-relate-exception").show();
                this.initExceptionNoticeGrid();
            } else {
                $("#tabs-relate-exception").remove();
            }
        },
        //初始化反馈信息表格
        initFeedbackGrid: function(){
            var me = this;
            var feedbackInfo = $.proxy(this.queryFeedbackInfo(),this); //函数作用域改变
            $("#relate-feedback-grid").grid({
                colModel: [
                    //默认展示字段
                    // {name: 'SRV_ORD_ID', label: '电路ID', width: 120, sortable: false},
                    {name: 'USER_REAL_NAME', label: '反馈人', width: 80, sortable: false},
                    {name: 'TACHE_NAME', label: '专业', width: 120, sortable: false},
                    {name: 'AREAID', label: '区域', width: 120, sortable: false},
                    {name: 'CONSTRUCT_SCHEME', label: '建设方案', width: 150, sortable: false},
                    {name: 'ACCESS_ROOM', label: '局端接入机房', width: 150, sortable: false},
                    {name: 'INVESTMENT_AMOUNT', label: '投资金额', width: 150, sortable: false},
                    {name: 'CONSTRUCT_PERIOD', label: '建设工期', width: 150, sortable: false},
                    {name: 'RES_SATISFY', label: '资源是否满足', width: 150, sortable: false}

                ],
                autowidth: true,
                rowNum: 10,
                rowList: [10, 15, 20, 50],
                pager: true,
                gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                multiselect: false,
                shrinkToFit: false,
                autoResizable: true,
                cached: true, //把用户自定义的列展示设置缓存在本地
                pageData: feedbackInfo,
                onDblClickRow: function (e, rowid, iRow, iCol) {//双击行事件
                    me.feedbackFormView(e, rowid, iRow, iCol);
                },
            });
            $("#relate-feedback-grid").grid("setGridHeight", 327);
        },
        feedbackFormView: function(e, rowid, iRow, iCol){
            var me = this;
            me.feedbackViewBtn(e, rowid, iRow, iCol);
        },
        //查看
        feedbackViewBtn:function(){
            var selrow = $("#feedback-grid").grid("getSelection"); //获取选中的行数据
            var _this = $(this);
            var options = {
                url: 'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/views/feedBackInfoView',
                height: '60%',
                width: "1000",
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
        //查询反馈信息
        queryFeedbackInfo:function(page, rowNum, sortname, sortorder){
            var dataIdea = $("#orderDetailsTabGrid-grid").grid("getSelection");
            if(dataIdea == ''
                ||dataIdea == undefined
                ||dataIdea.SRV_ORD_ID == ''
                ||dataIdea.SRV_ORD_ID == undefined){
                fish.toast('warn', "请选择一条电路信息");
                return;
            }
            var srvOrdId = dataIdea.SRV_ORD_ID+'';
            rowNum = rowNum || this.$("#relate-feedback-grid").grid("getGridParam", "rowNum");
            fish.store.set('feedback-grid-rowNum', rowNum); //记录用户选择的每页记录数
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
            $("#relate-feedback-grid").blockUI({message: '加载中'}).data('blockui-content', true);
            orderDetailsAction.queryFeedbackInfo(srvOrdId,function (data) {
                $("#relate-feedback-grid").grid("reloadData", data);
                $(window).trigger("resize");
            });
            $("#relate-feedback-grid").unblockUI().data('blockui-content', false);
        },
        //初始化一干资源信息表格
        initResourceGridW: function() {
            var me = this;
            debugger
            var resourceOrderInfoW = $.proxy(this.queryResourceOrderInfoW(),this); //函数作用域改变
            $("#relate-resource-grid-w").grid({
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
            $("#relate-resource-grid-w").grid("setGridHeight", 327);
        },
        //初始化二干资源信息表格
        initResourceGrid: function() {
            var me = this;
            debugger
            var resourceOrderInfo = $.proxy(this.queryResourceOrderInfo(),this); //函数作用域改变
            $("#relate-resource-grid").grid({
                colModel: [
                    //默认展示字段
                    {name: 'HANDLE_DEP', label: '配置分公司', width: 130, sortable: false},
                    {name: 'RESTYPE', label: '资源类型', width: 100, sortable: false},
                    {name: 'RESNAME', label: '资源名称', width: 290, sortable: false},
                    {name: 'BEFORE_ROUTE', label: '调前路由', width: 420, sortable: false},
                    {name: 'AFTER_ROUTE', label: '调后路由', width: 420, sortable: false},
                    // {name: 'ALL_ROUTE', label: '全程路由', width: 350, sortable: false},
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
            $("#relate-resource-grid").grid("setGridHeight", 327);
        },
        //初始化本地资源信息表格
        initResourceGridY: function() {
            var me = this;
            debugger
            var resourceOrderInfoY = $.proxy(this.queryResourceOrderInfoY(),this); //函数作用域改变
            $("#relate-resource-grid-y").grid({
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
            $("#relate-resource-grid-y").grid("setGridHeight", 327);
        },
        orderApplyFormView: function(res){
            var me = this;
            me.completedViewBtn(res);
        },
        completedViewBtn:function(res){
            var selrow;
            debugger
            if('W' == res){
                selrow = $("relate-#resource-grid-w").grid("getSelection"); //获取选中的行数据
            }else if('R' == res){
                selrow = $("#relate-resource-grid").grid("getSelection"); //获取选中的行数据
            }else if('Y' == res){
                selrow = $("#relate-resource-grid-y").grid("getSelection"); //获取选中的行数据
            }
            var circuitData = new Object();
            circuitData =  selrow;
            var _this = $(this);
            var options = {
                url: 'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/views/resourceInfoView',
                height: '400',
                width: "1000",
                modal: false,
                draggable: true,
                resizable: false,
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
            var dataTemp = $("#relateOrderDetailsTabGrid-grid").grid("getSelection");
            if(dataTemp == ''
                ||dataTemp == undefined
                ||dataTemp.SRV_ORD_ID == ''
                ||dataTemp.SRV_ORD_ID == undefined){
                fish.toast('warn', "请选择一条电路信息");
                return;
            }
            var srvordId = dataTemp.SRV_ORD_ID+'';
            rowNum = rowNum || this.$("#relate-resource-grid-w").grid("getGridParam", "rowNum");
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
            $("#relate-resource-grid-w").blockUI({message: '加载中'}).data('blockui-content', true);
            orderDetailsAction.queryResourceOrderInfoW(srvordId,function (data) {
                $("#relate-resource-grid-w").grid("reloadData", data);
                $(window).trigger("resize");
            });
            $("#relate-resource-grid-w").unblockUI().data('blockui-content', false);
        },
        //查询二干资源信息的方法
        queryResourceOrderInfo:function(page, rowNum, sortname, sortorder){
            var dataTemp = $("#relateOrderDetailsTabGrid-grid").grid("getSelection");
            if(dataTemp == ''
                ||dataTemp == undefined
                ||dataTemp.SRV_ORD_ID == ''
                ||dataTemp.SRV_ORD_ID == undefined){
                fish.toast('warn', "请选择一条电路信息");
                return;
            }
            var srvordId = dataTemp.SRV_ORD_ID+'';
            rowNum = rowNum || this.$("#relate-resource-grid").grid("getGridParam", "rowNum");
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
            $("#relate-resource-grid").blockUI({message: '加载中'}).data('blockui-content', true);
            orderDetailsAction.queryResourceOrderInfo(srvordId,function (data) {
                $("#relate-resource-grid").grid("reloadData", data);
                $(window).trigger("resize");
            });
            $("#relate-resource-grid").unblockUI().data('blockui-content', false);
        },
        //查询本地资源信息的方法
        queryResourceOrderInfoY:function(page, rowNum, sortname, sortorder){
            var dataTemp = $("#relateOrderDetailsTabGrid-grid").grid("getSelection");
            if(dataTemp == ''
                ||dataTemp == undefined
                ||dataTemp.SRV_ORD_ID == ''
                ||dataTemp.SRV_ORD_ID == undefined){
                fish.toast('warn', "请选择一条电路信息");
                return;
            }
            var srvordId = dataTemp.SRV_ORD_ID+'';
            rowNum = rowNum || this.$("#relate-resource-grid-y").grid("getGridParam", "rowNum");
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
            $("#relate-resource-grid-y").blockUI({message: '加载中'}).data('blockui-content', true);
            orderDetailsAction.queryResourceOrderInfoY(srvordId,function (data) {
                $("#relate-resource-grid-y").grid("reloadData", data);
                $(window).trigger("resize");
            });
            $("#relate-resource-grid-y").unblockUI().data('blockui-content', false);
        },
        // 初始化调单信息表格
        initDispatchGrid: function() {
            var changeOrderInfo = $.proxy(this.queryDispatchOrderInfo(),this); //函数作用域改变
            $("#relateDispatch-grid").grid({
                colModel: [
                    //默认展示字段
                    {name: 'DISPATCH_SOURCE', label: '调单来源', width: 120, sortable: false},
                    {name: 'DISPATCH_ORDER_NO', label: '调度单号', width: 240, sortable: false},
                    {name: 'DISPATCH_ORDER_ID', label: '调单号', width: 240, sortable: false,hidden:true},
                    {name: 'REMARK', label: '调单说明', width: 240, sortable: false,hidden:true},
                    {name: 'DISPATCH_TITLE', label: '调单标题', width: 450, sortable: false},
                    {name: 'STAFF_NAME', label: '处理人', width: 120, sortable: false},
                    {name: 'STAFF_TEL', label: '处理人电话', width: 120, sortable: false,hidden:true},
                    {name: 'STAFF_ORG', label: '处理人部门', width: 160, sortable: false},
                    {name: 'DISPATCH_TEXT', label: '调单内容', width: 350, sortable: false},
                    {name: 'DISPATCH_TYPE', label: '调单类型', width: 200, sortable: false},
                    // {name: 'STATE', label: '调单状态', width: 100, sortable: false},
                    {name: 'DISPATCH_GRADE', label: '调单等级', width: 100, sortable: false,hidden:true},
                    {name: 'DISPATCH_URGENCY', label: '调单缓急', width: 100, sortable: false,hidden:true},
                    {name: 'DISPATCH_SEND_ORG', label: '发送单位', width: 100, sortable: false,hidden:true},
                    {name: 'DISPATCH_COPY_ORG', label: '抄送单位', width: 100, sortable: false,hidden:true},
                    {name: 'RES_ALLOCATE', label: '是否转资源分配', width: 100, sortable: false,hidden:true},
                    {name: 'SPECIALTY', label: '分配专业', width: 100, sortable: false,hidden:true},
                    {name: 'NETMANAGE', label: '数据制作', width: 100, sortable: false,hidden:true},
                    {name: 'SEND_DATE', label: '签发时间', width: 150, sortable: true}
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
                pageData: changeOrderInfo,
                onSelectRow: function (e, rowid, state, checked) {
                    var dispatchInfo = $("#relateDispatch-grid").grid("getSelection");
                    if(dispatchInfo.DISPATCH_TEXT != null && dispatchInfo.DISPATCH_TEXT != undefined){
                        while(dispatchInfo.DISPATCH_TEXT.indexOf('@enter@@enter@') >= 0) {
                            dispatchInfo.DISPATCH_TEXT = dispatchInfo.DISPATCH_TEXT.replace('@enter@@enter@', '\n');
                        }
                        while(dispatchInfo.DISPATCH_TEXT.indexOf('@enter@ @enter@') >= 0) {
                            dispatchInfo.DISPATCH_TEXT = dispatchInfo.DISPATCH_TEXT.replace('@enter@ @enter@', '\n');
                        }
                        while(dispatchInfo.DISPATCH_TEXT.indexOf('@enter@') >= 0){
                            dispatchInfo.DISPATCH_TEXT = dispatchInfo.DISPATCH_TEXT.replace('@enter@','\n');
                        }

                    }

                    $("#relateDispatch-info").show();
                    document.getElementById('relate_dispatch_order_no').innerHTML=dispatchInfo.DISPATCH_ORDER_NO;
                    document.getElementById('relate_staff_name').innerHTML=dispatchInfo.STAFF_NAME;
                    document.getElementById('relate_staff_tel').innerHTML=dispatchInfo.STAFF_TEL;
                    document.getElementById('relate_staff_org').innerHTML=dispatchInfo.STAFF_ORG;
                    document.getElementById('relate_dispatch_type').innerHTML=dispatchInfo.DISPATCH_TYPE;
                    document.getElementById('relate_state').innerHTML=dispatchInfo.STATE;
                    document.getElementById('relate_dispatch_grade').innerHTML=dispatchInfo.DISPATCH_GRADE;
                    document.getElementById('relate_dispatch_urgency').innerHTML=dispatchInfo.DISPATCH_URGENCY;
                    document.getElementById('relate_send_date').innerHTML=dispatchInfo.SEND_DATE;
                    document.getElementById('relate_dispatch_title').innerHTML=dispatchInfo.DISPATCH_TITLE;
                    document.getElementById('relate_send_org').innerHTML=dispatchInfo.DISPATCH_SEND_ORG;
                    document.getElementById('relate_copy_org').innerHTML=dispatchInfo.DISPATCH_COPY_ORG;
                    document.getElementById('relate_dispatch_text').innerText=dispatchInfo.DISPATCH_TEXT;
                    document.getElementById('relate_dispatch_remark').innerHTML=dispatchInfo.REMARK;
                    // if(dispatchInfo.RES_ALLOCATE == undefined
                    //     ||dispatchInfo.RES_ALLOCATE == ""){
                    //     document.getElementById('draft_istran_resouce').innerHTML= "";
                    // }else{
                    //     document.getElementById('draft_istran_resouce').innerHTML=dispatchInfo.RES_ALLOCATE;
                    // }
                    // debugger
                    // var obj = new Object();
                    // obj.proId = "23";
                    // obj.type = "NETMANAGE_TYPE";
                    // obj.specityNetManage = dispatchInfo.NETMANAGE;
                    // if(dispatchInfo.NETMANAGE == undefined
                    //     || dispatchInfo.NETMANAGE == ""){
                    //     document.getElementById('draft_data_make').innerHTML="";
                    // }else{
                    //     orderDetailsAction.querySpecNetMagPro(obj, function (data) {
                    //         document.getElementById('draft_data_make').innerHTML=data;
                    //     });
                    // }
                    // obj.type = "SPECIALTY_TYPE";
                    // obj.specityNetManage = dispatchInfo.SPECIALTY;
                    // if(dispatchInfo.SPECIALTY == undefined
                    //     || dispatchInfo.SPECIALTY == ""){
                    //     document.getElementById('draft_dis_resouce').innerHTML="";
                    // }else{
                    //     orderDetailsAction.querySpecNetMagPro(obj, function (data) {
                    //         document.getElementById('draft_dis_resouce').innerHTML=data;
                    //     });
                    // }
                    var dispatchOrderId= dispatchInfo.DISPATCH_ORDER_ID
                    var attachs = document.getElementById('relate_attachs');
                    attachs.innerHTML=null;
                    var html=null;
                    orderDetailsAction.queryDispatchAttachInfo(dispatchOrderId.toString(),function (data){
                        for(var i =0;i <data.length; i++){
                            //html = '<div class=\"btn-group\"><button type=\"button\"  class=\"btn btn-link downLoadAttach\"  onclick=\"javascript:downLoadAttach('+fileId+','+fileName+','+fileType+','+filePath+')\">'+data[i].FILE_NAME+'</button></div>';
                            html = '<div class=\"btn-group\"><button id=\"'+ data[i].FILE_ID+ '\"  type=\"button\" value=\"'+ data[i].FILE_PATH+ '\" title=\"'+ data[i].FILE_TYPE+ '\" class=\"btn btn-link downLoadAttach\">'+data[i].FILE_NAME+'</button></div>';
                            $('#relate_attachs').append(html);
                        }
                        $("#relate_attachs").on("click",".downLoadAttach",function () {
                            var id= $(this).attr('id');
                            var param = new Object();
                            param.fileName =document.getElementById(id).innerText;
                            param.filePath = document.getElementById(id).value;
                            param.fileId = id +'.'+ document.getElementById(id).title;
                            orderDetailsAction.downFile("localScheduleLT/orderDetails/fileDownload.spr",param);
                        });
                    });
                }

            });
            $("#relateDispatch-grid").grid("setGridHeight", 160);
        },
        //查询调单信息的方法
        queryDispatchOrderInfo:function(page, rowNum, sortname, sortorder){
            var me = this;
            var cstOrdId = me.options.cstOrdId+'';
            var selectType = me.options.selectType;
            var param = {};
            param.cstOrdId = me.options.cstOrdId;
            param.tacheId = me.options.tacheId;
            param.specialtyCode = me.options.specialtyCode;
            param.woState = me.options.woStateCir;
            param.reginonId = me.options.reginonId;
            param.dealUserId = me.options.dealUserId;
            param.compUserId = me.options.compUserId;
            param.dispType = me.options.dispType;
            param.staffId = me.options.staffId;
            param.dispObjTyeValue = me.options.dispObjTyeValue;
            param.dispObjTye = me.options.dispObjTye;
            param.orderIdSelect = me.options.orderIdSelect;//工单查询orderId
            param.orderId = me.options.orderId;


            var dispatchOrderId; //调单查询传值

            rowNum = rowNum || this.$("#relateDispatch-grid").grid("getGridParam", "rowNum");
            fish.store.set('dispatch-grid-rowNum', rowNum); //记录用户选择的每页记录数
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
            $("#relateDispatch-grid").blockUI({message: '加载中'}).data('blockui-content', true);
            if('localStandy' == selectType){
                orderDetailsAction.queryDispatchOrderInfo(param,function (data) {
                    $("#relateDispatch-grid").grid("reloadData", data);
                    $(window).trigger("resize");
                });
            }else{
                orderDetailsAction.queryDispatchOrderInfoByCstOrdId(param,function (data) {
                    $("#relateDispatch-grid").grid("reloadData", data);
                    $(window).trigger("resize");
                });
            }
            $("#relateDispatch-grid").unblockUI().data('blockui-content', false);
        },
        //初始化异常单信息表格
        initChangeOrderGrid: function() {
            var me = this;
            var changeOrderInfo = $.proxy(this.queryChangeOrderInfo(null, null, null, null),this); //函数作用域改变
            $("#relateChangeOrder-grid").grid({
                colModel: [
                    //默认展示字段
                    {name: 'CHG_VERSION', label: '版本号', width: 100, sortable: false},
                    {name: 'SRV_ORD_ID', label: '订单ID', width: 100, sortable: false, hidden: true},
                    {name: 'CST_ORD_ID', label: '客户ID', width: 100, sortable: false, hidden: true},
                    {name: 'APPLY_ORD_ID', label: '订单申请编码', width: 150, sortable: false, hidden: true},
                    {name: 'CREATE_DATE', label: '创建时间', width: 180, sortable: true},
                    {name: 'REQ_FIN_DATE', label: '要求完成时间', width: 170, sortable: false},
                    // {name: 'CIRCUIT_CODE', label: '电路编码', width: 150, sortable: false},
                    {name: 'CHG_TYPE', label: '异常单类型', width: 110, sortable: false,hidden:true},
                    {name: 'CHG_TYPE_NAME', label: '异常单类型', width: 110, sortable: false},
                    // {name: 'CHG_VERSION', label: '版本', width: 100, sortable: false,hidden: true},
                    {name: 'TACHE_NAME', label: '环节名称', width: 160, sortable: false, formatter:me.formatUserNames},
                    {name: 'UNCONFIRM_USER_ID', label: '待确认用户id', width: 160, sortable: false, formatter:me.formatUserNames, hidden: true},
                    {name: 'CONFIRM_USER_ID', label: '已确认用户id', width: 160, sortable: false, formatter:me.formatUserNames, hidden: true},
                    {name: 'UNCONFIRM_USER_NAME', label: '待确认用户', width: 250, sortable: false, formatter:me.formatUserNames},
                    {name: 'CONFIRM_USER_NAME', label: '已确认用户', width: 250, sortable: false, formatter:me.formatUserNames},
                    {name: 'FILE_INFO', label: '附件', width: 200, formatter: function (value) {
                            var html = "";
                            if(value != null && value != '') {
                                //循环回写附件标签
                                var files ='['+value+']';
                                $.each(JSON.parse(files), function (index, val) {
                                    html += "<a class=\"downloadFile\" name=\"checkFile" +
                                        index +
                                        "\" fileName='" + val.fileName + "' fileId ='" + val.fileId + "' fileType = '" + val.fileType + "'>" +
                                        val.fileName +
                                        "</a><br>";
                                });
                            }
                            return html;
                        }
                    },
                    {name: 'TRACK_MESSAGE', label: '说明', width: 350, sortable: false}
                ],
                autowidth: true,
                rowNum: 10,
                rowList: [10,15,20,50],
                pager: true,
                gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                multiselect: true,
                shrinkToFit: false,
                autoResizable: true,
                cached: true, //把用户自定义的列展示设置缓存在本地
                pageData: changeOrderInfo,
                onDblClickRow: function (e, rowid, iRow, iCol) {//双击行事件
                    me.openChangeOrderDeatail(e, rowid, iRow, iCol);
                }
            });
            $("#relateChangeOrder-grid").grid("setGridHeight", 327);
        },
        downloadFile:function(){
            $('.downloadFile').off('click').on('click', function (e) {
                var fileName = $(this).attr("fileName");
                var fileId = $(this).attr("fileId");
                var fileType = $(this).attr("fileType");
                var param = {
                    fileName: fileName,
                    filePath: 'ftpattach',
                    fileId: fileId + "." + fileType
                };
                orderDetailsAction.downFile("localScheduleLT/orderDetails/fileDownload.spr", param);
            });
        },
        formatUserNames:function(value){//去重
            var ret = '';
            if(value){
                var arr = [];
                var temp = value.split(',');
                for(var k in temp){
                    arr.push(temp[k]);
                }
                var r = arr.filter(function (element, index, self) {
                    return self.indexOf(element) === index;
                });
                ret =  r.join(',');
            }
            return ret;
        },
        openChangeOrderDeatail:function(e, rowid, iRow, iCol){
            debugger;
            var rowData = $("#relateChangeOrder-grid").grid('getRowData', rowid);
            var chgType = rowData.CHG_TYPE;
            if(['104', '108', '109'].indexOf(chgType) > -1) { //追单，加急, 延期
                var pop = fish.popupView({
                    url: 'module/UnicomLocalNet/resmaster/portal/orderAbnormal/view/orderAppendView',
                    width: "99%",
                    height: "100%",
                    title: "定单详情",
                    viewOption: {
                        viewType: 'show',//仅展示
                        chgType: chgType,
                        version: rowData.CHG_VERSION,
                        // cstOrdId: rowData.CST_ORD_ID, //客户订单id
                        cstOrdId: rowData.SRC_CST_ORDER_ID, //客户订单id
                        srvOrdId: rowData.SRV_ORD_ID
                    },
                    callback: function (popup, view) {
                        popup.result.then(function (e) {

                        }, function (e) {
                            console.log('关闭了', e);
                        });
                    }
                });
            }else{
                fish.toast('warn', "仅追单,加急可显示详情");
                return;
            }
        },
        //初始化附件信息表格
        initAttachGrid: function() {
            var attachInfo = $.proxy(this.queryAttachInfo(),this); //函数作用域改变
            $("#relateAttach-grid").grid({
                colModel: [
                    //默认展示字段
                    {name: 'FILE_NAME', label: '附件名称', sortable: false,
                        formatter: function(cellval, opts, rwdat, _act) {
                            return '<div class="btn-group">' +
                                '<button type="button" class="btn btn-link js-delete">'+cellval+'</button>' +
                                '</div>'
                        },
                    },
                    {name: 'FILE_TYPE', label: '附件类型', sortable: false},
                    {name: 'TACHE_NAME', label: '环节名称', sortable: false},
                    {name: 'USER_NAME', label: '环节处理人', sortable: false},
                    {name: 'CREATE_DATE', label: '创建日期', sortable: false},
                    {name: 'FILE_SIZE', label: '附件大小', sortable: false},
                    {name: 'SOURCE', label: '附件来源', sortable: false}
                ],
                rownumbers:true,
                autowidth: true,
                rowNum: 10,
                rowList: [10,15,20,50],
                pager: true,
                gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                cached: true, //把用户自定义的列展示设置缓存在本地
                pageData: attachInfo,
                onCellSelect:function(e, rowid, iCol, cellcontent){
                    if (iCol == 1) {
                        var data = $("#relateAttach-grid").grid("getRowData",rowid);
                        var param = new Object();
                        param.fileName = data.FILE_NAME;
                        param.filePath = data.FILE_PATH;
                        param.fileId = data.FILE_ID +'.'+ data.FILE_TYPE;
                        orderDetailsAction.downFile("localScheduleLT/orderDetails/fileDownload.spr",param);
                    }
                }
            });
            $("#relateAttach-grid").grid("setGridHeight", 327);
        },
        // 初始化阶段性意见信息表格
        initIdeaGrid: function() {
            var ideaInfo = $.proxy(this.queryIdeaInfo(),this); //函数作用域改变
            $("#relate-idea-grid").grid({
                colModel: [
                    //默认展示字段
                    {name: 'SRV_ORD_ID', label: '订单ID', width: 120, sortable: false, hidden: true},
                    {name: 'WO_ID', label: '工单ID', width: 120, sortable: false, hidden: true},
                    {name: 'TACHE_NAME', label: '环节名称', width: 100, sortable: false},
                    {name: 'USER_NAME', label: '填写人', width: 100, sortable: false},
                    {name: 'CREATE_DATE', label: '填写时间', width: 130, sortable: false},
                    {name: 'PUB_DATE_NAME', label: '专业', width: 120, sortable: false,},
                    {name: 'REMARK', label: '阶段性意见', width: 200, sortable: false},
                    {name: 'FILE_NAME', label: '附件名称', width: 400, sortable: false,
                        formatter: function(value) {
                            var seValue = '';
                            if(value != ''
                                && value != undefined){
                                var valueArr = value.split(",");
                                $.each(valueArr,function (v,obj) {
                                    seValue += obj+"<br/>"
                                });
                            }
                            return seValue;
                        }
                    },
                ],
                rownumbers:true,
                autowidth: true,
                rowNum: 10,
                rowList: [10,15,20,50],
                pager: true,
                gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                cached: true, //把用户自定义的列展示设置缓存在本地
                pageData: ideaInfo,
                onCellSelect:function(e, rowid, iCol, cellcontent){
                    // if (iCol == 6) {
                    //     var data = $("#idea-grid").grid("getRowData",rowid);
                    //     if (data.FILE_NAME != '' && data.FILE_PATH != '' && data.FILE_ID !='' && data.FILE_TYPE != ''){
                    //         var param = new Object();
                    //         param.fileName = data.FILE_NAME;
                    //         param.filePath = data.FILE_PATH;
                    //         param.fileId = data.FILE_ID +'.'+ data.FILE_TYPE;
                    //         orderDetailsAction.downFile("localScheduleLT/orderDetails/fileDownload.spr",param);
                    //     }
                    // }
                }
            });
            $("#relate-idea-grid").grid("setGridHeight", 327);
        },
        //查询阶段性意见的方法
        queryIdeaInfo:function(page, rowNum, sortname, sortorder){
            var me = this;
            var dataIdea = $("#relateOrderDetailsTabGrid-grid").grid("getSelection");
            if(dataIdea == ''
                ||dataIdea == undefined
                ||dataIdea.SRV_ORD_ID == ''
                ||dataIdea.SRV_ORD_ID == undefined){
                fish.toast('warn', "请选择一条电路信息");
                return;
            }
            var orderId = dataIdea.ORDER_ID+'';
            var srvordId = dataIdea.SRV_ORD_ID+'';
            rowNum = rowNum || this.$("#relate-idea-grid").grid("getGridParam", "rowNum");
            fish.store.set('idea-grid-rowNum', rowNum); //记录用户选择的每页记录数
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
            //
            //调用后台方法
            $("#relate-idea-grid").blockUI({message: '加载中'}).data('blockui-content', true);
            orderDetailsAction.queryIdeaInfoBySrvOrdId(srvordId,function (data) {
                $("#relate-idea-grid").grid("reloadData", data);
                $(window).trigger("resize");
            });
            $("#relate-idea-grid").unblockUI().data('blockui-content', false);
        },
        // 初始化预警超时信息表格
        initWarningGrid: function() {
            var warningInfo = $.proxy(this.queryWarningInfo(),this); //函数作用域改变
            $("#relate-warning-grid").grid({
                colModel: [
                    //默认展示字段
                    {name: 'ORDER_ID', label: '订单ID', width: 120, sortable: false, hidden: true},
                    {name: 'TACHE_NAME', label: '环节名称', width: 120, sortable: false},
                    {name: 'REQ_FIN_DATE', label: '要求完成日期', width: 120, sortable: false},
                    {name: 'ALARM_DATE', label: '预警日期', width: 120, sortable: false},
                    {name: 'LIMIT_STATE', label: '预警状态', width: 120, sortable: false},
                    {name: 'DEAL_USER_NAME', label: '当前处理人', width: 120, sortable: false},
                    {name: 'WO_STATE', label: '工单状态', width: 120, sortable: false}
                ],
                rownumbers:true,
                autowidth: true,
                rowNum: 10,
                rowList: [10,15,20,50],
                pager: true,
                gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                cached: true, //把用户自定义的列展示设置缓存在本地
                pageData: warningInfo
            });
            $("#relate-warning-grid").grid("setGridHeight", 327);
        },
        //查询预警超时信息
        queryWarningInfo:function(page, rowNum, sortname, sortorder){
            var me = this;
            var dataWarn = $("#relateOrderDetailsTabGrid-grid").grid("getSelection");
            if(dataWarn == ''
                ||dataWarn == undefined
                ||dataWarn.SRV_ORD_ID == ''
                ||dataWarn.SRV_ORD_ID == undefined){
                fish.toast('warn', "请选择一条电路信息");
                return;
            }

            var orderId = dataWarn.ORDER_ID+'';
            rowNum = rowNum || this.$("#relate-warning-grid").grid("getGridParam", "rowNum");
            fish.store.set('warning-grid-rowNum', rowNum); //记录用户选择的每页记录数
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
            $("#relate-warning-grid").blockUI({message: '加载中'}).data('blockui-content', true);
            orderDetailsAction.queryWarningInfo(orderId,function (data) {
                $("#relate-warning-grid").grid("reloadData", data);
                $(window).trigger("resize");
            });
            $("#relate-warning-grid").unblockUI().data('blockui-content', false);
        },
        // 初始化关联主/子单信息表格
        initRelevanceGrid: function() {
            var relevanceInfo = $.proxy(this.queryRelevanceOrderInfo(),this); //函数作用域改变
            $("#relate-relevance-grid").grid({
                colModel: [
                    //默认展示字段
                    {name: 'ORDER_ID', label: '订单ID', width: 120, sortable: false, hidden: true},
                    {name: 'ORDER_TITLE', label: '订单标题', width: 120, sortable: false},
                    // {name: 'SYS_CODE', label: '系统名称', width: 120, sortable: false},
                    {name: 'AREA_NAME', label: '区域名称', width: 120, sortable: false},
                    {name: 'PUB_DATE_NAME', label: '订单状态', width: 120, sortable: false},
                    {name: 'CREATE_DATE', label: '创建时间', width: 120, sortable: false}
                ],
                rownumbers:true,
                autowidth: true,
                rowNum: 10,
                rowList: [10,15,20,50],
                pager: true,
                gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                cached: true, //把用户自定义的列展示设置缓存在本地
                pageData: relevanceInfo
            });
            $("#relevance-grid").grid("setGridHeight", 327);
        },
        //查询关联主/子信息方法
        queryRelevanceOrderInfo:function(page, rowNum, sortname, sortorder){
            var me = this;
            var dataRel = $("#relateOrderDetailsTabGrid-grid").grid("getSelection");
            if(dataRel == ''
                ||dataRel == undefined
                ||dataRel.SRV_ORD_ID == ''
                ||dataRel.SRV_ORD_ID == undefined){
                fish.toast('warn', "请选择一条电路信息");
                return;
            }
            var orderId = dataRel.ORDER_ID+'';
            rowNum = rowNum || this.$("#relate-relevance-grid").grid("getGridParam", "rowNum");
            fish.store.set('relevance-grid-rowNum', rowNum); //记录用户选择的每页记录数
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
            $("#relate-relevance-grid").blockUI({message: '加载中'}).data('blockui-content', true);
            orderDetailsAction.queryRelevanceOrderInfo(orderId,function (data) {
                $("#relate-relevance-grid").grid("reloadData", data);
                $(window).trigger("resize");
            });
            $("#relate-relevance-grid").unblockUI().data('blockui-content', false);
        },
        //二干本地任务列表展示的表头
        initAllTaskGrid : function(){
            return [
                //默认展示字段
                {name: 'TASKNAME', label: '任务名称', width: 100, sortable: false},
                {name: 'ORGNAME', label: '所属部门', width: 100, sortable: false},
                {name: 'USERJOBNAME', label: '执行岗位', width: 100, sortable: false},
                {name: 'USERNAME', label: '处理人', width: 100, sortable: false},
                {name: 'TACHENAME', label: '流程环节', width: 100, sortable: false},
                {name: 'ORDERSTATE', label: '工单状态', width: 100, sortable: false},
                {name: 'TRACKCONTENT', label: '处理结果', width: 100, sortable: false},
                {name: 'DEAL_DATE', label: '签收时间', width: 120, sortable: true},
                {name: 'CREATE_DATE', label: '开始时间', width: 120, sortable: true},
                {name: 'STATE_DATE', label: '完成时间', width: 120, sortable: true},
                {name: 'FINISH_DATE', label: '报竣时间', width: 120, sortable: true},
                {name: 'MINUTE', label: '耗时(分)', width: 100, sortable: true},
                {name: 'REQ_FIN_DATE', label: '超时时间', width: 120, sortable: true},
                {name: 'ALARM_DATE', label: '预警时间', width: 120, sortable: true},
                {name: 'EXCEEDTYPE', label: '超时类型', width: 100, sortable: true}
            ]
        },
        //初始化任务---二干任务列表
        secondTaskGrid: function() {
            var me = this;
            var secondTaskInfo = $.proxy(this.querySecondTaskInfo(),this); //函数作用域改变
            $("#relate-task-grid-second").grid({
                colModel: me.initAllTaskGrid(),
                autowidth: true,
                rowNum: 10,
                rowList: [10,15,20,50],
                pager: true,
                gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                shrinkToFit: false,
                autoResizable: true,
                cached: true, //把用户自定义的列展示设置缓存在本地
                pageData: secondTaskInfo,
                onDblClickRow: function (e, rowid, iRow, iCol) {//双击行事件
                    me.taskSecondFormView(e, rowid, iRow, iCol);
                },
            });
            $("#relate-task-grid-second").grid("setGridHeight", 327);
        },
        taskSecondFormView: function(e, rowid, iRow, iCol){
            var me = this;
            me.taskDetailViewBtn(e, rowid, iRow, iCol,"二干任务列表详情");
        },
        //查看二干任务裂变详情
        taskDetailViewBtn:function(e, rowid, iRow, iCol,titleValue){
            //判断是二干还是本地
            if(titleValue=="二干任务列表详情"){
                var selrow = $("#relate-task-grid-second").grid("getSelection"); //获取选中的行数据
            }else{//本地任务列表详情
                var selectId = e.target.id;
                var selrow = $("#"+selectId).grid("getSelection"); //获取选中的行数据
            }
            selrow.titleValue = titleValue;
            var _this = $(this);
            var options = {
                url: 'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/views/taskDetailView',
                height: '60%',
                width: "1000",
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

                    }, function (e) {
                        console.log('关闭了', e);
                    });
                }
            };
            var popup = fish.popupView(options);
        },
        //初始化任务---本地任务列表
        localTaskGrid : function (){
            var me = this;
            var dataInfo;
            if(FLAG|| 'second-schedule-lt' == sysResource.SYSTEM_RESOURCE){
                dataInfo = me.secToLocalGrid();
            }else {
                dataInfo = me.localGrid();
            }
            $("#relate-task-grid-local").grid(dataInfo);
            $("#relate-task-grid-local").grid("setGridHeight", 327);
        },
        //二干下发本地网的任务列表初始化
        secToLocalGrid : function(){
            var me = this;
            var localTaskInfo = $.proxy(this.querySecToLocalTaskInfo(),this); //函数作用域改变
            return {
                colModel: me.initAllTaskGrid(),
                autowidth: true,
                rowNum: 10,
                rowList: [10, 15, 20, 50],
                pager: true,
                gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                shrinkToFit: false,
                autoResizable: true,
                cached: true, //把用户自定义的列展示设置缓存在本地
                pageData: localTaskInfo,
                //任务在等待本地网处理环节打开子表格，点击+号可以展开看到本地网任务。 by ren.jiahang in 2019 06 11
                subGrid: true,
                subGridOptions: {
                    reloadOnExpand: true
                },
                subGridRowExpanded: function (e, subGridId, parentRowId) {
                    $("#relate-task-grid-local").grid("setSelection",parentRowId);
                    me.initTaskSubGrid(subGridId, parentRowId, me);
                }
            }
        },
        //本地网自启的任务列表初始化
        localGrid : function(){
            var me = this;
            var localTaskInfo = $.proxy(this.querySecToLocalTaskInfo(),this); //函数作用域改变
            return {
                colModel: me.initAllTaskGrid(),
                autowidth: true,
                rowNum: 10,
                rowList: [10, 15, 20, 50],
                pager: true,
                gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                shrinkToFit: false,
                autoResizable: true,
                cached: true, //把用户自定义的列展示设置缓存在本地
                pageData: localTaskInfo,
                onDblClickRow: function (e, rowid, iRow, iCol) {//双击行事件
                    me.tasklocalFormView(e, rowid, iRow, iCol)
                },
            }
        },
        //二干任务列表查询方法
        querySecondTaskInfo:function(page, rowNum, sortname, sortorder){
            var me = this;
            var dataTashInfo = $("#relateOrderDetailsTabGrid-grid").grid("getSelection");
            if(dataTashInfo == ''
                ||dataTashInfo == undefined
                ||dataTashInfo.SRV_ORD_ID == ''
                ||dataTashInfo.SRV_ORD_ID == undefined){
                fish.toast('warn', "请选择一条电路信息");
                return;
            }
            var orderId = dataTashInfo.ORDER_ID+'';
            rowNum = rowNum || this.$("#relate-task-grid-second").grid("getGridParam", "rowNum");
            fish.store.set('task-grid-second-rowNum', rowNum); //记录用户选择的每页记录数
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
            $("#relate-task-grid-second").blockUI({message: '加载中'}).data('blockui-content', true);
            orderDetailsAction.querySecTaskInfo(orderId,function (data) {
                $("#relate-task-grid-second").grid("reloadData", data);
            });
            $("#relate-task-grid-second").unblockUI().data('blockui-content', false);
        },
        //本地任务列表查询方法
        querySecToLocalTaskInfo:function(page, rowNum, sortname, sortorder){
            var me = this;
            var dataTashInfo = $("#relateOrderDetailsTabGrid-grid").grid("getSelection")
            if(dataTashInfo == ''
                ||dataTashInfo == undefined
                ||dataTashInfo.SRV_ORD_ID == ''
                ||dataTashInfo.SRV_ORD_ID == undefined){
                fish.toast('warn', "请选择一条电路信息");
                return;
            }
            var orderId = dataTashInfo.ORDER_ID+'';
            rowNum = rowNum || this.$("#relate-task-grid-local").grid("getGridParam", "rowNum");
            fish.store.set('relate-task-grid-local-rowNum', rowNum); //记录用户选择的每页记录数
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
            $("#relate-task-grid-local").blockUI({message: '加载中'}).data('blockui-content', true);
            orderDetailsAction.queryLocalTaskInfo(orderId,function (data) {
                $("#relate-task-grid-local").grid("reloadData", data);
                //  modify by wang.gang2
                $("#relate-task-grid-local").unblockUI().data('blockui-content', false);
            });
        },
        //查询本地的任务方法
        initTaskSubGrid:function (subGridId, parentRowId, meTemp) {
            var me = this;
            var dataSub = $("#relate-task-grid-local").grid("getSelection");
            var subgrid_table_id = subGridId + '_t';
            $("#" + subGridId).html("<table id='" + subgrid_table_id + "'></table>");
            var $subGrid = $("#" + subgrid_table_id).grid({
                colModel: me.initAllTaskGrid(),
                hidegrid:false,
                height : 200,
                autowidth: true,
                rowNum: 10,
                rowList: [10,15,20,50],
                pager: true,
                gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                shrinkToFit: false,
                autoResizable: true,
                cached: true, //把用户自定义的列展示设置缓存在本地
                onDblClickRow: function (e, rowid, iRow, iCol) {//双击行事件
                    me.tasklocalFormView(e, rowid, iRow, iCol)
                },
            });
            $('.ui-jqgrid-bdiv').find('.ui-jqgrid-htable > thead').hide(); //隐藏子表表头
            var orderId = dataSub.ORDERID+'';
            orderDetailsAction.queryTaskInfo(orderId,function (data) {
                $("#" + subgrid_table_id).grid("reloadData", data);
            });
        },
        tasklocalFormView: function(e, rowid, iRow, iCol){
            var me = this;
            me.taskDetailViewBtn(e, rowid, iRow, iCol,"本地任务列表详情");
        },
        //初始化日志信息表格
        initLogGrid: function() {
            var logInfo = $.proxy(this.queryLogInfo(),this); //函数作用域改变
            $("#relate-log-grid").grid({
                colModel: [
                    //默认展示字段
                    {name: 'ORDER_ID', label: '定单ID', width: 80, sortable: false},
                    {name: 'TRACK_ORG_NAME', label: '操作部门', width: 120, sortable: false},
                    {name: 'TRACK_STAFF_NAME', label: '操作人员', width: 80, sortable: false},
                    {name: 'TRACK_CONTENT', label: '操作内容', width: 150, sortable: false},
                    {name: 'TRACK_STAFF_PHONE', label: '操作人联系方式', width: 120, sortable: false},
                    {name: 'TRACK_DATE', label: '操作日期', width: 150, sortable: false},
                    {name: 'TRACK_MESSAGE', label: '操作信息', width: 340, sortable: false},
                    {name: 'OPER_TYPE_NAME', label: '操作类型', width: 100, sortable: false}
                ],
                autowidth: true,
                rowNum: 10,
                rowList: [10,15,20,50],
                pager: true,
                gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                shrinkToFit: false,
                autoResizable: true,
                cached: true, //把用户自定义的列展示设置缓存在本地
                pageData: logInfo
            });
            $("#relate-log-grid").grid("setGridHeight", 327);
        },
        //查询日志信息的方法
        queryLogInfo:function(page, rowNum, sortname, sortorder){
            var me = this;
            var datalog = $("#relateOrderDetailsTabGrid-grid").grid("getSelection");
            if(datalog == ''
                ||datalog == undefined
                ||datalog.SRV_ORD_ID == ''
                ||datalog.SRV_ORD_ID == undefined){
                fish.toast('warn', "请选择一条电路信息");
                return;
            }
            var orderId = datalog.ORDER_ID + '';
            rowNum = rowNum || this.$("#relate-log-grid").grid("getGridParam", "rowNum");
            fish.store.set('log-grid-rowNum', rowNum); //记录用户选择的每页记录数
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
            $("#log-grid").blockUI({message: '加载中'}).data('blockui-content', true);
            orderDetailsAction.queryLogInfo(orderId,function (data) {
                $("#relate-log-grid").grid("reloadData", data);
                $(window).trigger("resize");
            });
            $("#log-grid").unblockUI().data('blockui-content', false);
        },
        //查询附件信息方法
        queryAttachInfo:function(page, rowNum, sortname, sortorder){
            var me = this;
            var dataAttach = $("#relateOrderDetailsTabGrid-grid").grid("getSelection");
            if(dataAttach == ''
                ||dataAttach == undefined
                ||dataAttach.SRV_ORD_ID == ''
                ||dataAttach.SRV_ORD_ID == undefined){
                fish.toast('warn', "请选择一条电路信息");
                return;
            }
            var params = {};
            params.srvOrdId = dataAttach.SRV_ORD_ID + '';
            params.orderId = dataAttach.ORDER_ID + '';
            rowNum = rowNum || this.$("#relateAttach-grid").grid("getGridParam", "rowNum");
            fish.store.set('attach-grid-rowNum', rowNum); //记录用户选择的每页记录数
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
            $("#relateAttach-grid").blockUI({message: '加载中'}).data('blockui-content', true);
            orderDetailsAction.queryAttachInfo(params,function (data){
                $("#relateAttach-grid").grid("reloadData", data);
                $(window).trigger("resize");
            });
            $("#relateAttach-grid").unblockUI().data('blockui-content', false);
        },
        //查询异常单信息的方法
        queryChangeOrderInfo: function(page, rowNum, sortname, sortorder) {
            var me = this;
            var dataChange = $("#relateOrderDetailsTabGrid-grid").grid("getSelection");
            if(dataChange == ''
                ||dataChange == undefined
                ||dataChange.SRV_ORD_ID == ''
                ||dataChange.SRV_ORD_ID == undefined){
                fish.toast('warn', "请选择一条电路信息");
                return;
            }

            var srvOrdId = dataChange.SRV_ORD_ID+'';
            var cstOrdId = dataChange.CST_ORD_ID + '';
            var orderId = dataChange.ORDER_ID + '';
            var obj = {};
            obj.srvOrdId = srvOrdId;
            obj.cstOrdId = cstOrdId;
            obj.orderId = orderId;
            var rowNum = rowNum || this.$("#relateChangeOrder-grid").grid("getGridParam", "rowNum");
            fish.store.set('changeOrder-grid-rowNum', rowNum); //记录用户选择的每页记录数
            var page = page || 1;
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
            $("#relateChangeOrder-grid").blockUI({message: '加载中'}).data('blockui-content', true);
            orderDetailsAction.queryChangeOrderInfo(obj,function (data) {
                $("#relateChangeOrder-grid").grid("reloadData", data);
                $(window).trigger("resize");
            });
            $("#relateChangeOrder-grid").unblockUI().data('blockui-content', false);
        },
        //初始化对单测试人信息表格
        /*initOrderOtherInfo: function() {
            var me = this;
            var otherSystemInfo = $.proxy(this.queryOtherSystemInfo(),this); //函数作用域改变
            $("#otherInfo-grid").grid({
                colModel: [
                    //默认展示字段
                    {name: 'completeUnit', label: '报竣省份', width: 160, sortable: false},
                    {name: 'completeOrg', label: '报竣部门', width: 200, sortable: false},
                    {name: 'completePeople', label: '报竣人员', width: 100, sortable: false,formatter: me.formatCompletePeople},
                    {name: 'contactInformation', label: '报竣联系方式', width: 360, sortable: false},
                    {name: 'completeState', label: '报竣状态', width: 120, sortable: false},
                    {name: 'completeTime', label: '报竣时间', width: 180, sortable: false,formatter: me.formatCompleteTime},
                    {name: 'attachs', label: '附件', width: 300, formatter: function (value) {
                            var html = "";
                            if(value != null && value != ''){
                                //循环回写附件标签
                                $.each(value, function (index, val) {
                                    var attach=  val.length;
                                    if(val.length > 0){
                                        $.each(val, function (index, vals) {
                                            html += "<a class=\"downloadClFile\" name=\"checkFile" +
                                                index +
                                                "\" fileName='" + vals.name +
                                                "' fileId ='" + vals.FILE_ID +
                                                "' path ='" + vals.path +
                                                "' ip ='" + vals.ip +
                                                "' port ='" + vals.port +
                                                "' username ='" + vals.username +
                                                "' password ='" + vals.password +
                                                "' fileType = '" + vals.type +
                                                "' value = '" + vals.value +
                                                "'>" + vals.value + "</a><br>";
                                        })

                                    }else{
                                        html += "<a class=\"downloadClFile\" name=\"checkFile" +
                                            index +
                                            "\" fileName='" + val.name +
                                            "' fileId ='" + val.FILE_ID +
                                            "' path ='" + val.path +
                                            "' ip ='" + val.ip +
                                            "' port ='" + val.port +
                                            "' username ='" + val.username +
                                            "' password ='" + val.password +
                                            "' fileType = '" + val.type +
                                            "' value = '" + val.value +
                                            "'>" + val.value + "</a><br>";
                                    }

                                });
                            }
                            return html;
                        }},
                    {name: 'completeInfo', label: '报竣信息', width: 280, sortable: false},
                    {name: 'timeouttype', label: '超时类型', width: 160, sortable: false},
                    {name: 'timeouttext', label: '超时内容', width: 280, sortable: false},

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
                pageData: otherSystemInfo,
                /!*onDblClickRow: function (e, rowid, iRow, iCol) {//双击行事件
                    me.orderApplyFormView('W');
                },*!/
            });
            $("#otherInfo-grid").grid("setGridHeight", 327);
        },*/
        formatCompletePeople : function (value) {
            var completePeople;
            if(value== 'null' || value == '' || value==null){
                completePeople = '暂无';
            }else{
                completePeople = value;
            }
            return completePeople;
        },
        formatCompleteTime :function (value) {
            var completeTime;
            if(value=='null' || value == '' || value==null){
                completeTime = '暂无';
            }else{
                completeTime = value;
            }
            return completeTime;
        },

        queryOtherSystemInfo: function(page, rowNum, sortname, sortorder) {
            var me = this;
            var dataChange = $("#relateOrderDetailsTabGrid-grid").grid("getSelection");
            if(dataChange == ''
                ||dataChange == undefined
                ||dataChange.SRV_ORD_ID == ''
                ||dataChange.SRV_ORD_ID == undefined){
                fish.toast('warn', "请选择一条电路信息");
                return;
            }

            var srvOrdId = dataChange.SRV_ORD_ID+'';
            var cstOrdId = dataChange.CST_ORD_ID + '';
            var orderId = dataChange.ORDER_ID + '';
            var obj = {};
            obj.srvOrdId = srvOrdId;
            obj.cstOrdId = cstOrdId;
            obj.orderId = orderId;
            var rowNum = rowNum || this.$("#relate-otherInfo-grid").grid("getGridParam", "rowNum");
            fish.store.set('changeOrder-grid-rowNum', rowNum); //记录用户选择的每页记录数
            var page = page || 1;
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
            $("#otherInfo-grid").blockUI({message: '加载中'}).data('blockui-content', true);
            orderDetailsAction.queryOrderOtherSystemInfo(obj,function (data) {
                $("#otherInfo-grid").grid("reloadData", data);
            });
            $("#otherInfo-grid").unblockUI().data('blockui-content', false);
        },

        downloadClFile: function(){
            //下载监听
            $('.downloadClFile').off('click').on('click', function (e) {
                var fileName = $(this).attr("fileName");
                var fileId = $(this).attr("fileId");
                var ip = $(this).attr("ip");
                var port = $(this).attr("port");
                var path = $(this).attr("path");
                var username = $(this).attr("username");
                var password = $(this).attr("password");
                var fileType = $(this).attr("fileType");
                var value = $(this).attr("value");
                var param = {
                    fileName: value,
                    ip: ip,
                    port: port,
                    filePath: path,
                    userName: username,
                    password: password,
                    fileType: fileType,
                    value: value,
                    fileId: fileName
                };
                orderDetailsAction.downLoadOneDryFile("localScheduleLT/orderDetails/oneDryFileDownload.spr", param);
            });
        },
        //"定单详情"tab页点击事件
        tabsOrder : function(){
            this.queryConsumerInfo();
        },
        //订单详情"电路信息"tab页点击事件
        tabsCircuit:function(){
            this.initcircuitInfoGrid();
        },
        //初始化电路信息Tab页
        initcircuitInfoGrid : function() {
            var me = this;
            //modify by wang.gang2  ZMP 2019286  集客来单展示关联的核查单
            me.hideSecTask();
            if('101'==me.options.orderType && 'jike'== me.options.RESOURCES){
                me.girdMdel =[
                    {name: 'SOURCENETWORK', label: '网络拓扑图', width: 100,formatter: function () { return  '点击查看'}},
                    {name: 'WOORDERBACKFLAG', label: '是否退单', width: 80 , formatter : me.ifBackOrder},
                    {name: 'SERVICENAME', label: '产品类型', width: 100},
                    {name: 'CST_ORD_ID', label: '客户Id', width: 140 ,hidden: true},
                    {name: 'SRV_ORD_ID', label: '业务订单信息Id', width: 140 ,hidden: true},
                    {name: 'DISPATCH__ORDER_ID', label: '调单Id', width: 140 ,hidden: true},
                    {name: 'ORDER_ID', label: '流程订单Id', width: 140 ,hidden: true},
                    {name: 'SERVICE_ID', label: '产品类型Id', width: 140 ,hidden: true},
                    {name: 'ORDER_TYPE', label: '订单类型', width: 140 ,hidden: true},
                    {name: 'SUBSCRIBE_ID', label: '客户订单号', width: 120 , sortable: false },
                    {name: 'SERIAL_NUMBER', label: '业务号码', width: 120 , sortable: false },
                    {name: 'CIRCUITCODE', label: '电路编号', width: 120, sortable: false },
                    {name: 'TRADE_ID', label: '业务订单号', width: 120 },
                    {name: 'TRADE_ID', label: '关联核查单业务订单号', width: 180 ,style:{color: '#6DCC4A'}},
                    {name: 'AREGIONNAME', label: 'A端所属区域', width: 110 , sortable: false },
                    {name: 'ZREGIONNAME', label: 'Z端所属区域', width: 110 , sortable: false },
                    {name: 'A_INSTALLED_ADD', label: 'A端装机地址', width: 110 , sortable: false },
                    {name: 'Z_INSTALLED_ADD', label: 'Z端装机地址', width: 110 , sortable: false },
                    {name: 'A_REQ_FIN_DATE', label: 'A端要求完成时间', width: 135 , sortable: false },
                    {name: 'Z_REQ_FIN_DATE', label: 'Z端要求完成时间', width: 135 , sortable: false }
                ];
            }else{
                me.girdMdel =[
                    {name: 'SOURCENETWORK', label: '网络拓扑图', width: 100,formatter: function () { return  '点击查看'}},
                    {name: 'WOORDERBACKFLAG', label: '是否退单', width: 80 , formatter : me.ifBackOrder},
                    {name: 'SERVICENAME', label: '产品类型', width: 100},
                    {name: 'CST_ORD_ID', label: '客户Id', width: 140 ,hidden: true},
                    {name: 'SRV_ORD_ID', label: '业务订单信息Id', width: 140 ,hidden: true},
                    {name: 'DISPATCH__ORDER_ID', label: '调单Id', width: 140 ,hidden: true},
                    {name: 'ORDER_ID', label: '流程订单Id', width: 140 ,hidden: true},
                    {name: 'SERVICE_ID', label: '产品类型Id', width: 140 ,hidden: true},
                    {name: 'ORDER_TYPE', label: '订单类型', width: 140 ,hidden: true},
                    {name: 'SUBSCRIBE_ID', label: '客户订单号', width: 120 , sortable: false },
                    {name: 'SERIAL_NUMBER', label: '业务号码', width: 120 , sortable: false },
                    {name: 'CIRCUITCODE', label: '电路编号', width: 120, sortable: false },
                    {name: 'TRADE_ID', label: '业务订单号', width: 120 },
                    {name: 'AREGIONNAME', label: 'A端所属区域', width: 110 , sortable: false },
                    {name: 'ZREGIONNAME', label: 'Z端所属区域', width: 110 , sortable: false },
                    {name: 'A_INSTALLED_ADD', label: 'A端装机地址', width: 110 , sortable: false },
                    {name: 'Z_INSTALLED_ADD', label: 'Z端装机地址', width: 110 , sortable: false },
                    {name: 'A_REQ_FIN_DATE', label: 'A端要求完成时间', width: 135 , sortable: false },
                    {name: 'Z_REQ_FIN_DATE', label: 'Z端要求完成时间', width: 135 , sortable: false }];
            }
            $("#relateOrderDetailsTabGrid-grid").grid({
                colModel:me.girdMdel,
                rownumbers: true,
                autowidth: true,
                multiselect: false,
                shrinkToFit: false, //表格列宽是否按比例缩放，默认true
                height: 160,
                pageData: me.qrycircuitInfoGrid(),
                afterInsertRow: function (e, rowid, pageData) {
                    $("#relateOrderDetailsTabGrid-grid").grid('setCell', rowid, 'SOURCENETWORK', '', {color: '#6DCC4A'});
                },
                onSelectRow: function (e, rowid, state, checked) {//选中行事件
                    switch (selectTab) {
                        case 'circuit':
                            me.queryCircuitInfo();
                            break;
                        case 'attach':
                            me.initAttachGrid();
                            break;
                        case 'secondTask':
                            me.secondTaskGrid();
                            break;
                        case 'localTask':
                            me.localTaskGrid();
                            break;
                        case 'flow':
                            me.initFlowInfo(this);
                            break;
                        case 'log':
                            me.initLogGrid();
                            break;
                        case 'changeOrder':
                            me.queryChangeOrderInfo(null, null, null, null,changeOrderLabel);
                            break;
                        case 'idea':
                            me.initIdeaGrid();
                            break;
                        case 'warn':
                            me.initWarningGrid();
                            break;
                        /*case 'relevance':
                            me.initRelevanceGrid();
                            break;*/
                        case 'resourceW':
                            me.initResourceGridW();
                            break;
                        case 'resource':
                            me.initResourceGrid();
                            break;
                        case 'resourceY':
                            me.initResourceGridY();
                            break;
                        case 'checkOrder':
                            me.initFeedbackGrid();
                            break;
                        case 'otherSystemInfo':
                            me.initOrderOtherInfo();
                            break;
                        case 'addProduct':
                            me.initAddProductGrid();
                            break;
                    }
                },
                onCellSelect: function (e, rowid, iCol, cellcontent, colName, cellval) {//选中单元格的事件
                    var dataCell = $("#relateOrderDetailsTabGrid-grid").grid("getRowData",rowid);
                    debugger
                    if(iCol == 1){
                        var paramsRes={
                            objId:'1111111111111111111111111',
                            objType:2559,
                            objParam: {'MULTI_DATA_SOURCE_CONFIG_REGION_CODE_FOR_TOPO':me.crmRegion,
                                'OUTER_SYS_PASS_CIRCUIT_NO_VALUE':dataCell.CIRCUITCODE},
                            objName:dataCell.CIRCUITCODE,
                            topoDefId:230004,
                            isReaderCache:true,
                            viewPathId:'1901201',
                            topoName:dataCell.CIRCUITCODE
                        };
                        window.open(me.resNetworkUrl+"&params="+fish.TripleDES.encrypt(JSON.stringify(paramsRes),'zte-soft'));
                    }

                    //modify by wang.gang2 数据可能有空格
                    if(iCol == 14 && '101' == me.options.orderType && 'jike'== me.options.RESOURCES && 1 < cellcontent.length){
                        var pop = fish.popupView({
                            url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/orderDetailsView',
                            width: "100%",
                            height: "100%",
                            title: "工单详情",
                            modal: false,
                            viewOption: {
                                orderId: me.options.orderId,
                                srvordId: me.options.srvordId, //业务订单id
                                cstOrdId: me.options.cstOrdId, //客户订单id
                                userInfo: me.options.userInfo // 用户信息
                            },
                            callback: function (popup, view) {
                                popup.result.then(function (e) {
                                    meTemp.queryWorkOrdersGroup();
                                }, function (e) {
                                    console.log('关闭了', e);
                                    // $("#touchGet").css("style","display:block; float: left;");
                                    // $("#touchGet").show();
                                });
                            }
                        })

                    }

                },

            });

        },
        // 查询电路信息数据
        qrycircuitInfoGrid : function(param){
            var me = this;
            var param = {};
            param.cstOrdId = me.options.cstOrdId;
            param.tacheId = me.options.tacheId;
            param.specialtyCode = me.options.specialtyCode;
            param.woState = me.options.woStateCir;
            param.reginonId = me.options.reginonId;
            param.dealUserId = me.options.dealUserId;
            param.compUserId = me.options.compUserId;
            param.dispType = me.options.dispType;
            param.staffId = me.options.staffId;
            param.dispObjTyeValue = me.options.dispObjTyeValue;
            param.dispObjTye = me.options.dispObjTye;
            param.orderIdSelect = me.options.orderIdSelect;//工单查询orderId
            //modify by wang.gang2 // 异常单不需要工单id
            if(me.options.chgType == '' && me.options.chgType == null){
                param.woIds=me.options.woIds;
                param.woId=me.options.woId;
            }

            orderDetailsAction.queryCircuitInfoGrid(param,function (res) {
                if(res.flag == 1){
                    $("#relateOrderDetailsTabGrid-grid").grid("reloadData", res.data);
                    var rowdataOne = $("#relateOrderDetailsTabGrid-grid").grid("getRowData")[0];
                    $("#relateOrderDetailsTabGrid-grid").grid("setSelection",rowdataOne,false);
                    //如果是核查单显示"反馈信息"Tab页
                    if (rowdataOne && '102' == rowdataOne.ORDER_TYPE) {
                        $('#tabs-relate-feedback').find("a").show();
                    } else if (rowdataOne && '101' == rowdataOne.ORDER_TYPE) {
                        //如果是开通单，展示上次核查单的"反馈信息"Tab页
                        $('#tabs-relate-feedback').find("a").show();
                    }
                    me.tabsCircuitSub();
                }else {
                    fish.toast('error', res.message);
                }
            });
        },

        ifBackOrder : function (value){
            var ifBackOrderFlag = '否';
            if (value!= '' && '1'.indexOf(value) != -1){
                ifBackOrderFlag = '是';
            }else if (value == '') {
                ifBackOrderFlag = '-1';
                $('#relateOrderDetailsTabGrid-grid').grid('hideCol', 'WOORDERBACKFLAG');
            }
            return ifBackOrderFlag;
        },

        //订单详情"每条电路信息"tab页点击事件
        tabsCircuitSub:function(){
            var me = this;
            me.hideSecTask();
            //每次点击"电路信息"tab页，都要清空<div id='order-circuit-info'></div>节点下的东西
            //不然会有重复数据填充
            selectTab = 'circuit';
            $("#relateOtherInfo").empty();
            this.queryCircuitInfo();
        },
        //定单详情"附件信息"tab页点击事件
        tabsAttach:function(){
            var me = this;
            me.hideSecTask();
            //初始化"附件"tab页的grid
            selectTab = 'attach';
            this.initAttachGrid();
        },
        /*//"任务"tab页点击事件
        tabsTask:function(){
            selectTab = 'task';
            this.initTaskGrid();
        },*/
        //"二干任务"tab页点击事件
        tabsSecondTask:function(){
            selectTab = 'secondTask';
            this.secondTaskGrid();
        },
        // 对端测试人员信息点击事件
        tabsOrderOtherInfo :function(){
            selectTab = 'otherSystemInfo';
            this.initOrderOtherInfo();
        },
        //"本地任务"tab页点击事件
        tabsLocalTask:function(){
            var me = this;
            me.hideSecTask();
            selectTab = 'localTask';
            this.localTaskGrid();
        },
        //"流程"tab页点击事件
        tabsFlow: function () {
            var me = this;
            me.hideSecTask();
            selectTab = 'flow';
            this.initFlowInfo(this);
        },
        //"日志"tab页点击事件
        tabsLog:function(){
            var me = this;
            me.hideSecTask();
            //初始化"日志"tab页的grid
            selectTab = 'log';
            this.initLogGrid();
        },
        //定单详情"异常单信息"tab页点击事件
        tabsChangeOrder : function(){
            var me = this;
            me.hideSecTask();
            //每次点击"异常单信息"tab页，都默认选中"加急"子tab页
            // $("#tabs-relate-change-a").selected();
            //初始化异常单tab页的grid
            //4B：加急；4C：延期；4D：撤业务订单;4E：挂起；4F：解挂;
            selectTab = 'changeOrder';
            this.initChangeOrderGrid();
            // $('#tabs-relate-change-a').click();
        },
        //阶段性意见Tab页点击事件
        tabsIdea:function(){
            var me = this;
            me.hideSecTask();
            selectTab = 'idea';
            this.initIdeaGrid();
        },
        cc_confirm:function () {
            var me =this;
            fish.confirm('确认后此抄送单将不再显示，确定么？').result.then(function() {
                orderDetailsAction.updateCC({'woId':me.options.woId,'state':1,'dispObjType':'260000003','dispObjId':me.options.staffId,'srvOrdIds':me.options.srvOrdIds},function (res) {
                    fish.toast('info','抄送单已确认');
                    me.popup.close();
                })
            });
        },
        /*  //关联主/子单Tab页点击事件
          tabsRelevance:function(){
              selectTab = 'relevance';
              this.initRelevanceGrid();
          },*/
        //预警超时tab页点击事件
        tabsWarning:function(){
            var me = this;
            me.hideSecTask();
            selectTab = 'warn';
            this.initWarningGrid();
        },
        //调单tab页点击事件
        tabsDispatchOrder:function(){
            // selectTab = 'dispatch';
            $("#relateDispatch-info").hide();
            this.initDispatchGrid();
        },
        //一干资源信息tab页点击时间
        tabsResourceOrderW:function(){
            var me = this;
            me.hideSecTask();
            selectTab = 'resourceW';
            this.initResourceGridW();
        },
        //二干资源信息tab页点击时间
        tabsResourceOrder:function(){
            var me = this;
            me.hideSecTask();
            selectTab = 'resource';
            this.initResourceGrid();
        },
        //本地资源信息tab页点击时间
        tabsResourceOrderY:function(){
            var me = this;
            me.hideSecTask();
            selectTab = 'resourceY';
            this.initResourceGridY();
        },
        //反馈信息tab页点击事件
        tabsFeedbackOrder :function(){
            var me = this;
            me.hideSecTask();
            selectTab = 'checkOrder';
            this.initFeedbackGrid();
        },
        //查询客户信息方法
        queryConsumerInfo:function () {
            var me = this;
            var cstOrdId = me.options.cstOrdId+'';
            var srvOrdIds = me.options.srvordIds+'';
            var srvordId = me.options.srvordId+'';
            var selectType = me.options.selectType;
            var productTyep = me.options.serviceId;
            $('#relateOrderDeatail').empty();
            var orderDetailAll = ''; //定单信息
            orderDetailsAction.queryConsumerInfoByCustId(cstOrdId,function (data) {
                if (data != undefined && data != null) {
                    //一干来单显示客户地址字段
                    if (data.RESOURCES == 'onedry') {
                        $('.oneDry').show();
                    }
                    if (data.CUST_NAME_CHINESE == undefined){
                        document.getElementById("relate_cust_name_chinese").innerText="";//客户名称
                    }else{
                        document.getElementById("relate_cust_name_chinese").innerText=data.CUST_NAME_CHINESE;//客户名称
                    }
                    if (data.CUST_ID == undefined){
                        document.getElementById("relate_cust_id").innerText="";//客户编码
                    }else{
                        document.getElementById("relate_cust_id").innerText=data.CUST_ID;//客户编码
                    }
                    if (data.CUST_TYPE == undefined){
                        document.getElementById("relate_cust_type").innerText="";//客户类型
                    }else{
                        document.getElementById("relate_cust_type").innerText=data.CUST_TYPE;//客户类型
                    }
                    if (data.CUST_INDUSTRY == undefined){
                        document.getElementById("relate_cust_industry").innerText="";//客户行业
                    }else{
                        document.getElementById("relate_cust_industry").innerText=data.CUST_INDUSTRY;//客户行业
                    }
                    if (data.IS_GROUP_CUST == undefined){
                        document.getElementById("relate_is_group_cust").innerText="";//是否集团直管
                    }else{
                        document.getElementById("relate_is_group_cust").innerText=data.IS_GROUP_CUST;//是否集团直管
                    }
                    if (data.CUST_CONTACT_MAN_NAME == undefined){
                        document.getElementById("relate_cust_contact_man_name").innerText="";//客户联系人
                    }else{
                        document.getElementById("relate_cust_contact_man_name").innerText=data.CUST_CONTACT_MAN_NAME;//客户联系人
                    }
                    if (data.CUST_CONTACT_MAN_TEL == undefined){
                        document.getElementById("relate_cust_contact_man_tel").innerText="";//客户联系人电话
                    }else{
                        document.getElementById("relate_cust_contact_man_tel").innerText=data.CUST_CONTACT_MAN_TEL;//客户联系人电话
                    }
                    if (data.CUST_CONTACT_MAN_EMAIL == undefined){
                        document.getElementById("relate_cust_contact_man_email").innerText="";//客户联系人邮箱
                    }else{
                        document.getElementById("relate_cust_contact_man_email").innerText=data.CUST_CONTACT_MAN_EMAIL;//客户联系人邮箱
                    }
                    if (data.CUST_ADDRESS == undefined){
                        document.getElementById("relate_cust_addr").innerText="";//客户地址
                    }else{
                        document.getElementById("relate_cust_addr").innerText=data.CUST_ADDRESS;//客户地址
                    }
                    if (data.CUST_TEL == undefined){
                        document.getElementById("relate_cust_tel").innerText="";//客户联系电话
                    }else{
                        document.getElementById("relate_cust_tel").innerText=data.CUST_TEL;//客户联系电话
                    }
                    if (data.CUST_EMAIL == undefined){
                        document.getElementById("relate_cust_email").innerText="";//客户联系邮箱
                    }else{
                        document.getElementById("relate_cust_email").innerText=data.CUST_EMAIL;//客户联系邮箱
                    }
                    var srvOrdIdsT;
                    //订单信息
                    if('gomDispath' == selectType || 'gomOrderQuery' == selectType ){
                        srvOrdIdsT = srvOrdIds;
                    }else{
                        srvOrdIdsT = srvordId;
                    }
                    orderDetailsAction.queryOrderDeatilsInfo(srvOrdIdsT,function (data) {
                        if (data != undefined){
                            orderDetailAll += '<div class="row">';
                            orderDetailAll += ' <div class="col-md-4 form-group">';
                            orderDetailAll += '     <label class="control-label">申请单编号</label>';
                            orderDetailAll += '     <div id="apply_ord_id" class="form-order-info">'+data[0].APPLY_ORD_ID;
                            orderDetailAll += '     </div>';
                            orderDetailAll += ' </div>';
                            orderDetailAll += ' <div class="col-md-4 form-group">';
                            orderDetailAll += '     <label class="control-label">定单主题</label>';
                            orderDetailAll += '     <div id="apply_ord_name" class="form-order-info">'+data[0].APPLY_ORD_NAME;
                            orderDetailAll += '     </div>';
                            orderDetailAll += ' </div>';
                            if (productTyep != '20181221006'){ //局内中继电路不展示这些字段
                                orderDetailAll += ' <div class="col-md-4 subScribe form-group">';
                                orderDetailAll += '     <label class="control-label">客户订单号</label>';
                                orderDetailAll += '     <div id="subscribe_id" class="form-order-info">'+data[0].SUBSCRIBE_ID;
                                orderDetailAll += '     </div>';
                                orderDetailAll += ' </div>';
                                orderDetailAll += ' <div class="col-md-4 form-group">';
                                orderDetailAll += '     <label class="control-label">合同编号</label>';
                                orderDetailAll += '     <div id="contract_id" class="form-order-info">'+data[0].CONTRACT_ID;
                                orderDetailAll += '     </div>';
                                orderDetailAll += ' </div>';
                                orderDetailAll += ' <div class="col-md-4 form-group">';
                                orderDetailAll += '     <label class="control-label">合同名称</label>';
                                orderDetailAll += '     <div id="contract_name" class="form-order-info">'+data[0].CONTRACT_NAME;
                                orderDetailAll += '     </div>';
                                orderDetailAll += ' </div>';
                            }
                            orderDetailAll += ' <div class="col-md-4 form-group">';
                            orderDetailAll += '     <label class="control-label">产品类型</label>';
                            orderDetailAll += '     <div id="product_type" class="form-order-info">'+data[0].PRODUCT_TYPE;
                            orderDetailAll += '     </div>';
                            orderDetailAll += ' </div>';
                            orderDetailAll += ' <div class="col-md-4 form-group">';
                            orderDetailAll += '     <label class="control-label">业务动作</label>';
                            orderDetailAll += '     <div id="operate_type" class="form-order-info">'+data[0].OPERATE_TYPE;
                            orderDetailAll += '     </div>';
                            orderDetailAll += ' </div>';
                            orderDetailAll += ' <div class="col-md-4 form-group">';
                            orderDetailAll += '     <label class="control-label">业务动作明细</label>';
                            orderDetailAll += '     <div id="operate_type" class="form-order-info">'+data[0].CHANGE_FLAG;
                            orderDetailAll += '     </div>';
                            orderDetailAll += ' </div>';
                            orderDetailAll += ' <div class="col-md-4 form-group">';
                            orderDetailAll += '     <label class="control-label">受理人</label>';
                            orderDetailAll += '     <div id="handle_man_name" class="form-order-info">'+data[0].HANDLE_MAN_NAME;
                            orderDetailAll += '     </div>';
                            orderDetailAll += ' </div>';
                            orderDetailAll += ' <div class="col-md-4 form-group">';
                            orderDetailAll += '     <label class="control-label">受理人联系方式</label>';
                            orderDetailAll += '     <div id="handle_man_tel" class="form-order-info">'+data[0].HANDLE_MAN_TEL;
                            orderDetailAll += '     </div>';
                            orderDetailAll += ' </div>';
                            orderDetailAll += ' <div class="col-md-4 form-group">';
                            orderDetailAll += '     <label class="control-label">受理部门</label>';
                            orderDetailAll += '     <div id="handle_dep" class="form-order-info">'+data[0].HANDLE_DEP;
                            orderDetailAll += '     </div>';
                            orderDetailAll += ' </div>';
                            orderDetailAll += ' <div class="col-md-4 form-group">';
                            orderDetailAll += '     <label class="control-label">受理地市</label>';
                            orderDetailAll += '     <div id="handle_city" class="form-order-info">'+data[0].HANDLE_CITY;
                            orderDetailAll += '     </div>';
                            orderDetailAll += ' </div>';
                            orderDetailAll += ' <div class="col-md-4 form-group">';
                            orderDetailAll += '     <label class="control-label">受理时间</label>';
                            orderDetailAll += '     <div id="handle_time" class="form-order-info">'+data[0].HANDLE_TIME;
                            orderDetailAll += '     </div>';
                            orderDetailAll += ' </div>';
                            orderDetailAll += ' <div class="col-md-4 form-group subScribe">';
                            orderDetailAll += '     <label class="control-label">发起方项目经理</label>';
                            orderDetailAll += '     <div id="init_am_name" class="form-order-info">'+data[0].INIT_AM_NAME;
                            orderDetailAll += '     </div>';
                            orderDetailAll += ' </div>';
                            orderDetailAll += ' <div class="col-md-4 form-group subScribe">';
                            orderDetailAll += '     <label class="control-label">客户经理</label>';
                            orderDetailAll += '     <div id="cust_manager" class="form-order-info">'+data[0].CUST_MANAGER;
                            orderDetailAll += '     </div>';
                            orderDetailAll += ' </div>';
                            orderDetailAll += ' <div class="col-md-4 form-group subScribe">';
                            orderDetailAll += '     <label class="control-label">省项目经理</label>';
                            orderDetailAll += '     <div id="province_pm_name" class="form-order-info">'+data[0].PROVINCE_PM_NAME;
                            orderDetailAll += '     </div>';
                            orderDetailAll += ' </div>';
                            orderDetailAll += ' <div class="col-md-4 form-group subScribe">';
                            orderDetailAll += '     <label class="control-label">发展人渠道名称</label>';
                            orderDetailAll += '     <div id="rele_b_inspect_order" class="form-order-info">'+data[0].DEVELOPER_DEPART_NAME;
                            orderDetailAll += '     </div>';
                            orderDetailAll += ' </div>';
                            orderDetailAll += ' <div class="col-md-12 form-group">';
                            orderDetailAll += '     <label class="control-label">备注</label>';
                            orderDetailAll += '     <div id="remark" class="form-order-info">'+data[0].REMARK;
                            orderDetailAll += '     </div>';
                            orderDetailAll += ' </div>';
                            orderDetailAll += ' <div class="col-md-12 form-group">';
                            orderDetailAll += '     <label class="control-label">附件</label>';
                            orderDetailAll += '     <div id="applyAttachInfo" class="form-order-info">';
                            orderDetailAll += '     </div>';
                            orderDetailAll += ' </div>';
                            orderDetailAll += '</div>';
                        }
                        $('#relateOrderDeatail').append(orderDetailAll);
                        //查询申请单附件信息
                        var cstParam = {};
                        cstParam.cstOrdId = cstOrdId;
                        // cstParam.cstOrdId = 6611;
                        orderDetailsAction.queryApplyAttachInfo(cstParam,function(res){
                            if(res != null && res != undefined){
                                var attachHTML = '';
                                for(var i =0;i <res.length; i++){
                                    attachHTML = '<div class=\"btn-group\"><button id=\"'+ res[i].FILE_ID+ '\"  type=\"button\" value=\"'+ res[i].FILE_PATH+ '\" title=\"'+ res[i].FILE_TYPE+ '\" class=\"btn btn-link downLoadAttach\">'+res[i].FILE_NAME+'</button></div>';
                                    $('#relateApplyAttachInfo').append(attachHTML);
                                }
                                $("#relateApplyAttachInfo").on("click",".downLoadAttach",function () {
                                    var id= $(this).attr('id');
                                    var param = new Object();
                                    param.fileName =document.getElementById(id).innerText;
                                    param.filePath = document.getElementById(id).value;
                                    param.fileId = id +'.'+ document.getElementById(id).title;
                                    orderDetailsAction.downFile("localScheduleLT/orderDetails/fileDownload.spr",param);
                                });
                            }
                        });

                    });

                }
            });

        },
        //查询电路信息方法
        queryCircuitInfo:function () {
            var dataTemp = $("#relateOrderDetailsTabGrid-grid").grid("getSelection");
            if(dataTemp == ''
                ||dataTemp == undefined
                ||dataTemp.SRV_ORD_ID == ''
                ||dataTemp.SRV_ORD_ID == undefined){
                fish.toast('warn', "请选择一条电路信息");
                return;
            }
            var srvOrdId = dataTemp.SRV_ORD_ID+'';
            var serviceId = dataTemp.SERVICE_ID+'';
            $('#relateazpeInfo').show();
            $('#relateOtherInfo').empty();
            $('#relateapeInfo').empty();
            $('#relatezceInfo').empty();
            orderDetailsAction.queryCircuitInfo(srvOrdId,serviceId, function (data) {
                if (data.success) {
                    var azInfo = data.AZInfo;
                    var otherInfo = data.otherInfo;
                    var peInfo = data.PEInfo;
                    var _circuitAPE = '';
                    var _circuitZCE = '';
                    if (azInfo.length > 0){
                        for (var i=0; i< azInfo.length; i+=4){
                            _circuitAPE += '<div class="clearfix form-group">';
                            _circuitAPE +='<div class="col-md-6">';
                            _circuitAPE += '<label class="control-label">'+(azInfo[i].PROPERTY_NAME == ""?"":(azInfo[i].PROPERTY_NAME+':'))+'</label>';
                            _circuitAPE += '<div class="form-order-info">';
                            _circuitAPE += ( azInfo[i].OLD_ATTR_VALUE ==undefined|| azInfo[i].OLD_ATTR_VALUE =="")?"":'<font style="font-weight: bolder;color: red">';
                            _circuitAPE += (azInfo[i].ATTR_VALUE ==undefined?"" : azInfo[i].ATTR_VALUE);
                            _circuitAPE += ( azInfo[i].OLD_ATTR_VALUE ==undefined|| azInfo[i].OLD_ATTR_VALUE =="")?"":'</font>';
                            _circuitAPE += ( azInfo[i].OLD_ATTR_VALUE ==undefined|| azInfo[i].OLD_ATTR_VALUE =="")?"":("(原："+ azInfo[i].OLD_ATTR_VALUE+")");
                            _circuitAPE += '</div>';
                            _circuitAPE += '</div>';
                            _circuitAPE += '<div class="col-md-6">';
                            _circuitAPE += '<label class="control-label">'+(azInfo[i+1].PROPERTY_NAME == ""?"":(azInfo[i+1].PROPERTY_NAME+':'))+'</label>';
                            _circuitAPE += '<div class="form-order-info">';
                            _circuitAPE += ( azInfo[i+1].OLD_ATTR_VALUE ==undefined|| azInfo[i+1].OLD_ATTR_VALUE =="")?"":'<font style="font-weight: bolder;color: red">';
                            _circuitAPE += (azInfo[i+1].ATTR_VALUE ==undefined?"" : azInfo[i+1].ATTR_VALUE);
                            _circuitAPE += ( azInfo[i+1].OLD_ATTR_VALUE ==undefined|| azInfo[i+1].OLD_ATTR_VALUE =="")?"":'</font>';
                            _circuitAPE += (azInfo[i+1].OLD_ATTR_VALUE ==undefined||azInfo[i+1].OLD_ATTR_VALUE =="")?"":("(原："+azInfo[i+1].OLD_ATTR_VALUE+")");
                            _circuitAPE += '</div>';
                            _circuitAPE += '</div>';
                            _circuitAPE += '</div>';
                            _circuitZCE += '<div class="clearfix form-group">';
                            _circuitZCE += '<div class="col-md-6">';
                            _circuitZCE += '<label class="control-label">'+(azInfo[i+2].PROPERTY_NAME == ""?"":(azInfo[i+2].PROPERTY_NAME+':'))+'</label>';
                            _circuitZCE += '<div class="form-order-info">';
                            _circuitZCE += ( azInfo[i+2].OLD_ATTR_VALUE ==undefined|| azInfo[i+2].OLD_ATTR_VALUE =="")?"":'<font style="font-weight: bolder;color: red">';
                            _circuitZCE += (azInfo[i+2].ATTR_VALUE ==undefined?"" : azInfo[i+2].ATTR_VALUE);
                            _circuitZCE += ( azInfo[i+2].OLD_ATTR_VALUE ==undefined|| azInfo[i+2].OLD_ATTR_VALUE =="")?"":'</font>';
                            _circuitZCE += (azInfo[i+2].OLD_ATTR_VALUE ==undefined||azInfo[i+2].OLD_ATTR_VALUE =="")?"":("(原："+azInfo[i+2].OLD_ATTR_VALUE+")");
                            _circuitZCE += '</div>';
                            _circuitZCE += '</div>';
                            _circuitZCE += '<div class="col-md-6">';
                            _circuitZCE += '<label class="control-label">'+(azInfo[i+3].PROPERTY_NAME == ""?"":(azInfo[i+3].PROPERTY_NAME+':'))+'</label>';
                            _circuitZCE += '<div class="form-order-info">';
                            _circuitZCE += ( azInfo[i+3].OLD_ATTR_VALUE ==undefined|| azInfo[i+3].OLD_ATTR_VALUE =="")?"":'<font style="font-weight: bolder;color: red">';
                            _circuitZCE += (azInfo[i+3].ATTR_VALUE ==undefined?"" : azInfo[i+3].ATTR_VALUE);
                            _circuitZCE += ( azInfo[i+3].OLD_ATTR_VALUE ==undefined|| azInfo[i+3].OLD_ATTR_VALUE =="")?"":'</font>';
                            _circuitZCE += (azInfo[i+3].OLD_ATTR_VALUE ==undefined||azInfo[i+3].OLD_ATTR_VALUE =="")?"":("(原："+ azInfo[i+3].OLD_ATTR_VALUE+")");
                            _circuitZCE += '</div>';
                            _circuitZCE += '</div>';
                            _circuitZCE += '</div>';

                        }
                        $("#relateapeInfo").append(_circuitAPE);
                        $("#relatezceInfo").append(_circuitZCE);
                    }else if(peInfo.length > 0){
                        for (var i = 0; i< peInfo.length; i+=4){
                            _circuitAPE += '<div class="clearfix form-group">';
                            _circuitAPE += '<div class="col-md-6">';
                            _circuitAPE += '<label class="control-label">'+(peInfo[i].PROPERTY_NAME == ""?"":(peInfo[i].PROPERTY_NAME+':'))+'</label>';
                            _circuitAPE += '<div class="form-order-info">';
                            _circuitAPE += (peInfo[i].OLD_ATTR_VALUE ==undefined||peInfo[i].OLD_ATTR_VALUE =="")?"":'<font style="font-weight: bolder;color: red">';
                            _circuitAPE += (peInfo[i].ATTR_VALUE ==undefined? "" : peInfo[i].ATTR_VALUE);
                            _circuitAPE += (peInfo[i].OLD_ATTR_VALUE ==undefined||peInfo[i].OLD_ATTR_VALUE =="")?"":'</font>';
                            _circuitAPE += (peInfo[i].OLD_ATTR_VALUE ==undefined||peInfo[i].OLD_ATTR_VALUE =="")?"":("(原："+peInfo[i].OLD_ATTR_VALUE+")");
                            _circuitAPE += '</div>';
                            _circuitAPE += '</div>';
                            _circuitAPE += '<div class="col-md-6">';
                            _circuitAPE += '<label class="control-label">'+(peInfo[i+1].PROPERTY_NAME == ""?"":(peInfo[i+1].PROPERTY_NAME+':'))+'</label>';
                            _circuitAPE += '<div class="form-order-info">';
                            _circuitAPE +=(peInfo[i+1].OLD_ATTR_VALUE ==undefined||peInfo[i+1].OLD_ATTR_VALUE =="")?"":'<font style="font-weight: bolder;color: red">';
                            _circuitAPE += (peInfo[i+1].ATTR_VALUE ==undefined?"" : peInfo[i+1].ATTR_VALUE);
                            _circuitAPE +=(peInfo[i+1].OLD_ATTR_VALUE ==undefined||peInfo[i+1].OLD_ATTR_VALUE =="")?"":'</font>';
                            _circuitAPE +=(peInfo[i+1].OLD_ATTR_VALUE ==undefined||peInfo[i+1].OLD_ATTR_VALUE =="")?"":("(原："+peInfo[i+1].OLD_ATTR_VALUE+")");
                            _circuitAPE += '</div>';
                            _circuitAPE += '</div>';
                            _circuitAPE += '</div>';
                            _circuitZCE += '<div class="clearfix form-group">';
                            _circuitZCE += '<div class="col-md-6">';
                            _circuitZCE += '<label class="control-label">'+(peInfo[i+2].PROPERTY_NAME == ""?"":(peInfo[i+2].PROPERTY_NAME+':'))+'</label>';
                            _circuitZCE += '<div class="form-order-info">';
                            _circuitZCE +=  (peInfo[i+2].OLD_ATTR_VALUE ==undefined||peInfo[i+2].OLD_ATTR_VALUE =="")?"":'<font style="font-weight: bolder;color: red">';
                            _circuitZCE += (peInfo[i+2].ATTR_VALUE ==undefined? "" : peInfo[i+2].ATTR_VALUE);
                            _circuitZCE +=  (peInfo[i+2].OLD_ATTR_VALUE ==undefined||peInfo[i+2].OLD_ATTR_VALUE =="")?"":'</font>';
                            _circuitZCE +=  (peInfo[i+2].OLD_ATTR_VALUE ==undefined||peInfo[i+2].OLD_ATTR_VALUE =="")?"":("(原："+peInfo[i+2].OLD_ATTR_VALUE+")");
                            _circuitZCE += '</div>';
                            _circuitZCE += '</div>';
                            _circuitZCE += '<div class="col-md-6">';
                            _circuitZCE += '<label class="control-label">'+(peInfo[i+3].PROPERTY_NAME == ""?"":(peInfo[i+3].PROPERTY_NAME+':'))+'</label>';
                            _circuitZCE += '<div class="form-order-info">';
                            _circuitZCE += (peInfo[i+3].OLD_ATTR_VALUE ==undefined||peInfo[i+3].OLD_ATTR_VALUE =="")?"":'<font style="font-weight: bolder;color: red">';
                            _circuitZCE += (peInfo[i+3].ATTR_VALUE ==undefined?"" : peInfo[i+3].ATTR_VALUE);
                            _circuitZCE += (peInfo[i+3].OLD_ATTR_VALUE ==undefined||peInfo[i+3].OLD_ATTR_VALUE =="")?"":'</font>';
                            _circuitZCE += (peInfo[i+3].OLD_ATTR_VALUE ==undefined||peInfo[i+3].OLD_ATTR_VALUE =="")?"":("(原："+peInfo[i+3].OLD_ATTR_VALUE+")");
                            _circuitZCE += '</div>';
                            _circuitZCE += '</div>';
                            _circuitZCE += '</div>';
                        }
                        $("#relateapeInfo").append(_circuitAPE);
                        $("#relatezceInfo").append(_circuitZCE);
                    }
                    if(_circuitAPE == ''&&_circuitZCE == ''){
                        $('#relateazpeInfo').hide();
                    }
                    var _circuita = '';
                    if (otherInfo.length >0){
                        for (var i = 0; i < otherInfo.length; i+=4){
                            _circuita += '<div class="clearfix">';
                            _circuita += '<div class="col-md-3 pdb-10">'+
                                '<label class="control-label">'+(otherInfo[i].PROPERTY_NAME == ""?"":(otherInfo[i].PROPERTY_NAME+':'))+'</label>'+
                                '<div  class="form-order-info">';
                            _circuita +=  + (otherInfo[i].OLD_ATTR_VALUE ==undefined||otherInfo[i].OLD_ATTR_VALUE =="")?"":'<font style="font-weight: bolder;color: red">';
                            _circuita +=(otherInfo[i].ATTR_VALUE ==undefined?"" : otherInfo[i].ATTR_VALUE);
                            _circuita +=  + (otherInfo[i].OLD_ATTR_VALUE ==undefined||otherInfo[i].OLD_ATTR_VALUE =="")?"":'</font>';
                            _circuita +=  + (otherInfo[i].OLD_ATTR_VALUE ==undefined||otherInfo[i].OLD_ATTR_VALUE =="")?"":('(原：'+otherInfo[i].OLD_ATTR_VALUE+')');
                            _circuita += '</div></div>';
                            _circuita += '<div class="col-md-3 pdb-10">'+
                                '<label class="control-label">'+(otherInfo[i+1].PROPERTY_NAME == ""?"":(otherInfo[i+1].PROPERTY_NAME+':'))+'</label>'+
                                '<div  class="form-order-info">';
                            _circuita +=  (otherInfo[i+1].OLD_ATTR_VALUE ==undefined||otherInfo[i+1].OLD_ATTR_VALUE =='')?'':'<font style="font-weight: bolder;color: red">';
                            _circuita += (otherInfo[i+1].ATTR_VALUE ==undefined?'': otherInfo[i+1].ATTR_VALUE);
                            _circuita +=  (otherInfo[i+1].OLD_ATTR_VALUE ==undefined||otherInfo[i+1].OLD_ATTR_VALUE =='')?'':'</font>';
                            _circuita +=  (otherInfo[i+1].OLD_ATTR_VALUE ==undefined||otherInfo[i+1].OLD_ATTR_VALUE =='')?'':('(原：'+otherInfo[i+1].OLD_ATTR_VALUE+')');
                            _circuita += '</div></div>';
                            _circuita += '<div class="col-md-3 pdb-10">'+
                                '<label class="control-label">'+(otherInfo[i+2].PROPERTY_NAME == ""?"":(otherInfo[i+2].PROPERTY_NAME+':'))+'</label>'+
                                '<div  class="form-order-info">';
                            _circuita += (otherInfo[i+2].OLD_ATTR_VALUE ==undefined||otherInfo[i+2].OLD_ATTR_VALUE =="")?"":'<font style="font-weight: bolder;color: red">';
                            _circuita +=(otherInfo[i+2].ATTR_VALUE ==undefined?"" : otherInfo[i+2].ATTR_VALUE);
                            _circuita += (otherInfo[i+2].OLD_ATTR_VALUE ==undefined||otherInfo[i+2].OLD_ATTR_VALUE =="")?"":'</font>';
                            _circuita += (otherInfo[i+2].OLD_ATTR_VALUE ==undefined||otherInfo[i+2].OLD_ATTR_VALUE =="")?"":("(原："+otherInfo[i+2].OLD_ATTR_VALUE+")");
                            _circuita += '</div></div>';
                            _circuita += '<div class="col-md-3 pdb-10">'+
                                '<label class="control-label">'+(otherInfo[i+3].PROPERTY_NAME == ""?"":(otherInfo[i+3].PROPERTY_NAME+':'))+'</label>'+
                                '<div  class="form-order-info">';
                            _circuita += (otherInfo[i+3].OLD_ATTR_VALUE ==undefined||otherInfo[i+3].OLD_ATTR_VALUE =="")?"":'<font style="font-weight: bolder;color: red">';
                            _circuita +=(otherInfo[i+3].ATTR_VALUE ==undefined?"" : otherInfo[i+3].ATTR_VALUE);
                            _circuita += (otherInfo[i+3].OLD_ATTR_VALUE ==undefined||otherInfo[i+3].OLD_ATTR_VALUE =="")?"":'</font>';
                            _circuita += (otherInfo[i+3].OLD_ATTR_VALUE ==undefined||otherInfo[i+3].OLD_ATTR_VALUE =="")?"":("(原："+otherInfo[i+3].OLD_ATTR_VALUE+")");
                            _circuita += '</div></div>';
                            _circuita += '</div>';
                        }
                        $("#relateOtherInfo").append(_circuita);
                    }
                }else{
                    fish.toast('error', data.message);
                }
            });
            $(window).trigger("resize");
        },
        // 初始化流程信息
        initFlowInfo: function (meFlow) {
            var rowData = $("#relateOrderDetailsTabGrid-grid").grid("getSelection");
            var tacheId = '';
            var psId = '';
            var woState = '';
            if ('' != meFlow.options && meFlow.options != null ){
                tacheId = meFlow.options.tacheId + '';
                psId = meFlow.options.psId + '';
                woState = meFlow.options.psId + '';
            }
            this.requireView({
                selector: "#relate-flow-info-div",
                url: "module/UnicomLocalNet/resmaster/portal/flowChart/views/flowChartView",
                viewOption: {
                    orderId: rowData.ORDER_ID + '',
                    srvOrdId: rowData.SRV_ORD_ID + '',
                    tacheId: tacheId,
                    psId: psId,
                    woState: woState,
                },
                callback: function (view) {
                    // 调整窗口大小
                    $(window).trigger("resize");
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
        // 初始化工具栏按钮
        initButton:function () {
            var me = this;
            var tacheId = me.options.tacheId;
            var woState = me.options.woState;
            var orderId = me.options.orderId;
            var serviceId = me.options.serviceId;
            var orderIds = me.options.orderIds;
            var woId = me.options.woId;
            var woIds = me.options.woIds;
            var psId = me.options.psId;
            var buttonState = me.options.buttonState;
            var srvOrdId = me.options.srvOrdId;
            var cstOrdId = me.options.cstOrdId;
            var specialtyCode = me.options.specialtyCode;
            var regionId = me.options.reginonId;
            var dispObjTyeValue = me.options.dispObjTyeValue;
            var dispObjTye = me.options.dispObjTye;
            var activeType = me.options.activeType;
            var selectType = me.options.selectType;
            var userInfo = me.options.userInfo;
            var srvOrdStat = me.options.srvOrdStat;
            var orderState = me.options.orderState;
            var resources = me.options.RESOURCES;
            var openViewParam = me.generateObjParam();
            var srvBelong = new Object();
            srvBelong.srvOrdId = srvOrdId;
            srvBelong.cstOrdId = cstOrdId;
            sysResource=orderDetailsAction.qrySrvOrderBelongSys(srvBelong).responseJSON.data
            crmRegionMap = orderDetailsAction.qryMsmSwitchByArea(userInfo.areaId).responseJSON.data;
            if(crmRegionMap !=''
                && crmRegionMap != undefined){
                this.crmRegion = crmRegionMap.CRM_REGION;
            }
            if(buttonState=='ccOrder'){ //抄送确认按钮
                $("#cc_confirm").show();
            }
            //海南、重庆暂时屏蔽二干资源配置
            if('350002000000000042766427' == userInfo.areaId
                || '350002000000000042766429' == userInfo.areaId){
                $("#tabs-relate-resourceOrder").remove();
            }
            this.resNetworkUrl = orderDetailsAction.qryInterfaceUrl('ResourceNetWork').responseJSON.data;
            //导出定单电路详情
            var exportOrderInfo = function () {
                var params = new Object();
                params.cstOrdId = cstOrdId;
                params.serviceId = serviceId;
                params.srvOrdId = srvOrdId;
                params.selectType = selectType;
                params.srvOrdIds = me.options.srvordIds+'';
                params.orderIds = me.options.orderIds;
                params.orderId = me.options.orderId;
                params.tacheId = me.options.tacheId;
                params.specialtyCode = me.options.specialtyCode;
                params.woState = me.options.woStateCir;
                params.reginonId = me.options.reginonId;
                params.dealUserId = me.options.dealUserId;
                params.compUserId = me.options.compUserId;
                params.dispType = me.options.dispType;
                params.staffId = me.options.staffId;
                params.dispObjTyeValue = me.options.dispObjTyeValue;
                params.dispObjTye = me.options.dispObjTye;
                params.orderIdSelect = me.options.orderIdSelect;//工单查询orderId

                orderDetailsAction.exportOrderInfo(URl+'/localScheduleLT/orderInfoExportController/exportOrderInfo.spr',params);
            };
            if(woIds != '' && woIds != undefined){
                var woOrderIds = new Array()
                var splitWoIds = woIds.split(",");
                $.each(splitWoIds,function(index,obj){
                    woOrderIds.push(obj);
                });
            }
            //签收
            var getWoOrder = function () {
                var params = new Object();
                params.woOrderIds = woOrderIds;
                params.actionType = "get";
                orderDetailsAction.getFreeWoOrder(params, function (res) {
                    if (res.success) {
                        //$("#btnClassDiv").html('');
                        $("#btnClassDiv").find("button").remove('[type="button"]');
                        me.options.buttonState = "dealOrder";
                        me.options.dealUserId = me.options.staffId;
                        me.options.staffId = "";
                        me.options.dispType = "260000003";
                        me.initButton();
                        fish.toast('warn', res.message);
                        //me.popup.close();
                    } else {
                        fish.toast('warn', res.message);
                    }
                });
            };
            //释放签收
            var freedWoOrder = function () {
                var pop = fish.popupView({
                    url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/releaseSignForView',
                    width: 950,
                    height: 450,
                    title: "释放签收",
                    viewOption: {
                        cstOrdId: me.options.cstOrdId,
                        tacheId: me.options.tacheId,
                        reginonId: me.options.reginonId,
                        specialtyCode: me.options.specialtyCode,
                        dispObjTyeValue: me.options.dispObjTyeValue,
                        dispObjTye: me.options.dispObjTye,
                        staffId: me.options.staffId
                    },
                    callback: function (popup, view) {
                        popup.result.then(function (e) {
                            me.popup.close();
                        }, function (e) {
                            console.log('关闭了', e);
                        });
                    }
                });

            };
            //派单
            var sendWoOrder = function () {
                if(orderState == '200000006'){ //订单状态为200000006 说明单子已挂起；
                    //挂起的不能让提交
                    if (srvOrdStat == '4E') {
                        fish.error({title:'提示',message:'该电路已挂起，不能提交!'});
                    }
                } else if (srvOrdStat == '10N'){ //10N:才让提交
                    if (tacheId == "500001153") {  //电路调度 判断是否有在途核查单
                        var data = orderDetailsAction.queryCheckOrderStatBySrvOrdId(srvOrdId + '').responseJSON.data;
                        if (data.flag == "false") {
                            fish.error({title: '提示', message: data.msg});
                            return;
                        }
                    }
                    if ((activeType == '102' || activeType == '104' || activeType == '105') && (tacheId == '500001155' || tacheId == '500001157') ) { //页面提示，停机复机和拆机判断资源是否提交过
                        fish.confirm('是否在资源配置页面提交过？').result.then(function() { //目前这样做
                            openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/operOrderView';
                            openViewParam.btnFlag = "submit";
                            me.openDealView(openViewParam);
                        });
                    } else if(me.options.serviceId=='10003406'){
                        // sdwan产品
                        if(tacheId=='1551002669'){
                            // 运维派单页面
                            openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/sdwan/views/commonSubmitView';
                            openViewParam.btnFlag = "submit";
                            me.openDealView(openViewParam);
                        } else{
                            openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/sdwan/views/commonSubmitView';
                            openViewParam.btnFlag = "submit";
                            me.openDealView(openViewParam);
                        }
                    }else{
                        openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/operOrderView';
                        openViewParam.btnFlag = "submit";
                        me.openDealView(openViewParam);
                    }
                }
            };
            //转派
            var transferWoOrder = function () {
                openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/operOrderView';
                openViewParam.btnFlag = "trans";
                me.openDealView(openViewParam);
            };
            //回退
            var goBackWoOrder = function () {
                openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/operOrderView';
                openViewParam.btnFlag = "goBackOrder";
                me.openDealView(openViewParam);
            };
            //退单
            var chargeBackWoOrder = function () {
                openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/operOrderView';
                openViewParam.btnFlag = "rollBackOrder";
                me.openDealView(openViewParam);
            };
            // 填写终端信息
            var saveTerminal = function () {
                openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/sdwan/views/terminalSynchView';
                me.openDealViewWithoutCallback(openViewParam);
            };
            // sdwan退单
            var sdwanBackWoOrder = function () {
                openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/sdwan/views/commonSubmitView';
                openViewParam.btnFlag = "rollBackOrder";
                me.openDealView(openViewParam);
            };
            // 资源配置
            var resConfig = function(){
                if (tacheId == "500001153") {  //电路调度 判断是否有在途核查单
                    var data = orderDetailsAction.queryCheckOrderStatBySrvOrdId(srvOrdId + '').responseJSON.data;
                    if (data.flag == "false") {
                        fish.error({title: '提示', message: data.msg});
                        return;
                    }
                }
                var pop = fish.popupView({
                    url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/operOrderView',
                    width: 700,
                    height: 350,
                    position:{at: "center"},
                    title: "工单处理",
                    viewOption: {
                        psId : psId,
                        tacheId : tacheId,
                        woState : woState,
                        orderId : orderId,
                        woId : woId,
                        srvOrdId : srvOrdId,
                        cstOrdId : cstOrdId,
                        specialtyCode : specialtyCode,
                        regionId : regionId,
                        buttonState : buttonState,
                        dispObjTyeValue:dispObjTyeValue,
                        dispObjTye:dispObjTye,
                        btnFlag : "resConfig"
                    },
                    callback: function (popup, view) {
                        popup.result.then(function (e) {
                            me.popup.close();
                        }, function (e) {
                            console.log('关闭了', e);
                        });
                    }
                });
            };

            //作废
            var disableWoOrder = function () {
                var pop = fish.popupView({
                    url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/disableView',
                    width: 700,
                    position: {at: "center"},
                    title: "工单处理",
                    viewOption: {
                        psId : psId,
                        tacheId : tacheId,
                        woState : woState,
                        orderId : orderId,
                        woId : woId,
                        srvOrdId : srvOrdId,
                        cstOrdId : cstOrdId,
                        specialtyCode : specialtyCode,
                        regionId : regionId,
                        buttonState : buttonState,
                        dispObjTyeValue:dispObjTyeValue,
                        dispObjTye:dispObjTye,
                        btnFlag : "disableOrder"
                    },
                    callback: function (popup, view) {
                        popup.result.then(function (e) {
                            me.popup.close();
                        }, function (e) {
                            console.log('关闭了', e);
                        });
                    }
                });
            };
            // 确认异常
            var affirmException = function () {
                var params = new Object();
                params.srvOrdId = srvOrdId;
                orderDetailsAction.affirmException(params, function (res) {
                    if (res.success) {
                        fish.toast('warn', res.message);
                        me.popup.close();
                    } else {
                        fish.toast('warn', res.message);
                    }
                });
            };
            var ccWoOrder = function () {
                var pop = fish.popupView({
                    url: 'module/UnicomLocalNet/resmaster/portal/orderTacheDealView/views/orderCCView',
                    width: 700,
                    height: "80%",
                    title: "工单抄送",
                    viewOption: {
                        psId : psId,
                        tacheId : tacheId,
                        woState : woState,
                        orderId : orderId,
                        woId : woId,
                        srvOrdId : srvOrdId,
                        cstOrdId : cstOrdId,
                        specialtyCode : specialtyCode,
                        regionId : regionId,
                        buttonState : buttonState,
                        dispObjTyeValue:dispObjTyeValue,
                        dispObjTye:dispObjTye,
                        userInfo:userInfo,
                        btnFlag : "trans"
                    },
                    callback: function (popup, view) {
                        popup.result.then(function (e) {
                            // me.popup.close();
                        }, function (e) {
                            console.log('关闭了', e);
                        });
                    }
                });
            };

            //异常单提交
            var compWo = function () {
                debugger;
                var woIds = [];
                var _woIds = me.options.woIds;
                if(_woIds){
                    woIds = _woIds.split(",");
                }
                var param = {};
                param.woIds = woIds;
                param.staffId = userInfo.userId;
                param.chgType = me.options.chgType;
                ngc.progress("加载中...");
                orderAppendAction.compWo(param,function (ret) {
                    ngc.progress();
                    if('SUCCESS' === ret.result){
                        fish.toast('info', '提交成功');
                        me.popup.close();
                    }
                    else if('UNCONFIRM' === ret.result) {
                        fish.toast('info', '请先进行追单确认或者驳回改追单');
                    }
                    else {
                        fish.toast('warn', ret.message);
                    }
                });
            };

            var btnParams = new Object();
            btnParams.tacheId = tacheId;//"500001144";
            btnParams.orderId = orderId;
            btnParams.orderIds = orderIds;
            btnParams.sysResource=sysResource.SYSTEM_RESOURCE;
            btnParams.buttonState=buttonState;
            //btnParams.woState = woState;//"290000002";
            if('gomDispath' == selectType || 'gomQuery' == selectType || 'gomOrderQuery' == selectType){
                btnParams.btnInfo = "110"

            } else if (buttonState == "deptStandny" || buttonState == "jobStandby" || buttonState == "staffStandby") {
                btnParams.btnInfo = "200";
            }else if (buttonState == "dealOrder") {
                btnParams.btnInfo = "100";//处理中
            } else if (buttonState == "exceptionOrder") {
                btnParams.btnInfo = "300";//异常通知
                //}else if (buttonState == "dispConfirm" && woState == "290000110") {
            }else if (buttonState == "dispConfirm") {
                if (tacheId == "500001153"){  //电路调度
                    /**
                     * 改造。。。因为多条电路时，有的单子可以补有的不能补
                     */
                    var ifSupplementOrder = orderDetailsAction.queryIfSupplementOrder(orderIds).responseJSON.data;
                    if (ifSupplementOrder){
                        btnParams.btnInfo = "101";
                    }else {
                        return false;
                    }
                } else if (woState == "290000004"){
                    btnParams.btnInfo = "110";//处理中
                    if (tacheId == "500001144"){  //核查调度
                        btnParams.btnInfo = "103";
                    }else if (tacheId == "500001157" && userInfo.areaId == '350002000000000042766427'){  //资源分配 并且是海南的用户
                        btnParams.btnInfo = "103";
                    }
                } else {
                    return false;
                }
                /*}else if (buttonState == "dispConfirm" && woState == "290000004") {
                    btnParams.btnInfo = "110";//处理中
                    if (tacheId == "500001144"){  //核查调度
                        btnParams.btnInfo = "103";
                    }else if (tacheId == "500001157" && userInfo.areaId == '350002000000000042766427'){  //资源分配 并且是海南的用户
                        btnParams.btnInfo = "103";
                    }*/
            }else if("110,111,112".indexOf(me.options.chgType) != -1) { //挂起解挂撤单
                //提交按钮
                $('<button>')
                    .attr({
                        'id': 'officeDataApply-tacheBtn-compWo',
                        'class': 'btn handle-order-btn',
                        'type': 'button',
                        'resButtons': 'compWo()'
                    })
                    .css('border', '1px solid #fff')
                    .text('提交')
                    .click(compWo)
                    .appendTo($('#btnClassDiv'));
                return false;
            }else{
                return false;
            }

            orderDetailsAction.getTacheButton(btnParams,function (res) {
                if(res.success){
                    for(var i = 0;i < res.resButtons.length;i++){
                        var button = res.resButtons[i];
                        var psIds = '1000209,1000210';
                        if (tacheId == '500001153') { //电路调度环节
                            //if (psIds.indexOf(psId) != -1 && "secondary" != resources) {
                            if (psIds.indexOf(psId) != -1 && "second-schedule-lt" != sysResource.SYSTEM_RESOURCE) {
                                //if (psIds.indexOf(psId) != -1 || (FLAG && "104,105".indexOf(activeType) != -1)) {
                                //跨域流程    //二干下发的停复机流程，电路调度环节不显示退单按钮
                                if(button.ID == '100010'){
                                    continue;
                                }
                            }
                        }
                        var div=$('<button>');         //创建一个div
                        div.attr('id','officeDataApply-tacheBtn' + button.ID);    //给div设置id
                        div.attr('class','btn handle-order-btn');    //给div设置样式
                        div.attr('type','button');
                        div.attr('style','border:1px solid #fff;');
                        div.attr('resButtons',button.BUTTON_CLICK);
                        div.append(button.BUTTON_NAME);
                        $('#btnClassDiv').append(div);
                        div.bind('click',function(){
                            var funcName = $(this).attr("resButtons");
                            eval(funcName);
                        });
                    }
                }else{
                    alert(res.resButtons);
                }
            });
        },
        //生成对象参数
        generateObjParam : function () {
            var me = this ;
            var openViewParam = {};
            openViewParam.tacheId = me.options.tacheId;
            openViewParam.woState = me.options.woState;
            openViewParam.orderId = me.options.orderId;
            openViewParam.serviceId = me.options.serviceId;
            openViewParam.orderIds = me.options.orderIds;
            openViewParam.woId = me.options.woId;
            openViewParam.woIds = me.options.woIds;
            openViewParam.psId = me.options.psId;
            openViewParam.buttonState = me.options.buttonState;
            openViewParam.srvOrdId = me.options.srvOrdId;
            openViewParam.cstOrdId = me.options.cstOrdId;
            openViewParam.specialtyCode = me.options.specialtyCode;
            openViewParam.regionId = me.options.reginonId;
            openViewParam.dispObjTyeValue = me.options.dispObjTyeValue;
            openViewParam.dispObjTye = me.options.dispObjTye;
            openViewParam.activeType = me.options.activeType;
            openViewParam.selectType = me.options.selectType;
            openViewParam.userInfo = me.options.userInfo;
            openViewParam.srvOrdStat = me.options.srvOrdStat;
            openViewParam.orderState = me.options.orderState;
            openViewParam.resources = me.options.RESOURCES;
            return openViewParam;
        },
        //打开弹窗
        openDealView : function (param) {
            var me = this ;
            var pop = fish.popupView({
                url: param.url,
                width: 700,
                height: "100%",
                title: "工单处理",
                resizable:true,
                viewOption: {
                    psId : param.psId,
                    tacheId : param.tacheId,
                    woState : param.woState,
                    orderId : param.orderId,
                    woId : param.woId,
                    serviceId: param.serviceId,
                    srvOrdId : param.srvOrdId,
                    cstOrdId : param.cstOrdId,
                    specialtyCode : param.specialtyCode,
                    regionId : param.regionId,
                    buttonState : param.buttonState,
                    dispObjTyeValue:param.dispObjTyeValue,
                    dispObjTye:param.dispObjTye,
                    activeType:param.activeType,
                    btnFlag : param.btnFlag,
                    resources : param.resources
                },
                callback: function (popup, view) {
                    popup.result.then(function (e) {
                        me.popup.close();
                    }, function (e) {
                        console.log('关闭了', e);
                    });
                }
            });
        },
        //打开弹窗,没有回调方法
        openDealViewWithoutCallback : function (param) {
            var me = this ;
            var pop = fish.popupView({
                url: param.url,
                width: 700,
                height: "100%",
                title: "工单处理",
                resizable:true,
                viewOption: {
                    psId : param.psId,
                    tacheId : param.tacheId,
                    woState : param.woState,
                    orderId : param.orderId,
                    woId : param.woId,
                    serviceId: param.serviceId,
                    srvOrdId : param.srvOrdId,
                    cstOrdId : param.cstOrdId,
                    specialtyCode : param.specialtyCode,
                    regionId : param.regionId,
                    buttonState : param.buttonState,
                    dispObjTyeValue:param.dispObjTyeValue,
                    dispObjTye:param.dispObjTye,
                    activeType:param.activeType,
                    btnFlag : param.btnFlag,
                    resources : param.resources
                }
            });
        },

        // 初始化异常通知表格
        initExceptionNoticeGrid: function () {
            var me = this;
            $("#exception-notice-grid").grid({
                colModel: [
                    {name: 'NOTICE_ID', label: '通知ID', width: 140, hidden: true},
                    {name: 'SRV_ORD_ID', label: '业务订单Id', width: 140, hidden: true},
                    {name: 'TACHE_NAME', label: '环节名称', width: 120},
                    {name: 'WO_STATE', label: '环节状态', width: 120},
                    {name: 'DEAL_USER_ID', label: '处理人ID', width: 140, hidden: true},
                    {name: 'DEAL_USER_NAME', label: '处理人', width: 140},
                    {name: 'NOTICE_CONTENT', label: '通知内容', width: 160, sortable: false},
                    {name: 'CREATE_DATE', label: '通知时间', width: 120, sortable: false},
                    {
                        name: 'STAT', label: '确认状态', width: 120, sortable: false, formatter: function (value) {
                            if (1 == value) {
                                return '已确认';
                            }
                            return '未确认';
                        }
                    }
                ],
                autowidth: true,
                multiselect: false,
                pageData: me.qryExceptionNoticeList()
            });
        },
        // 查询异常通知信息
        qryExceptionNoticeList: function () {
            var me = this;
            var param = {};
            param.srvOrdId = me.options.srvOrdId;

            orderDetailsAction.qryExceptionNoticeList(param, function (res) {
                if (res.flag == 1) {
                    $("#exception-notice-grid").grid("reloadData", res.data);
                    $(window).trigger("resize");
                } else {
                    fish.toast('error', res.message);
                }
            });
        },
        //任务信息子表格双击行弹窗
        openChildWindowFromTaskGrid:function(){
            var datas = $("#task-grid").grid("getSelection"); //获取选中的行数据
            // var circuitData = new Object();
            // circuitData =  datas;
            // var _this = $(this);
            var options = {
                url: 'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/views/taskChildWindowView',
                height: '60%',
                width: "1000",
                modal: false,
                draggable: true,
                resizable:true,
                autoResizable: true,
                viewOption: {
                    flag : "org",
                    gridData : datas
                },
                callback: function (popup, view) {
                    popup.result.then(function (res) {
                    }, function (e) {
                        console.log('关闭了', e);
                    });
                }
            };
            var popup = fish.popupView(options);
        },
        /**
         * 附加产品单击事件方法
         */
        tabsAddProduct : function(){
            selectTab = 'addProduct';
            //初始化附加产品grid
            this.initAddProductGrid();
        },
        initAddProductGrid : function(){
            var addProductInfo = $.proxy(this.queryAddProductInfo(),this); //函数作用域改变
            $("#addProduct-grid").grid({
                colModel: [
                    //默认展示字段
                    {name: 'PRODUCT_NAME', label: '附件产品名称', width: 120, sortable: false},
                    {name: 'START_DATE', label: '开始时间', width: 120, sortable: false},
                    {name: 'END_DATE', label: '到期时间', width: 120, sortable: false}
                ],
                rownumbers:true,
                autowidth: true,
                rowNum: 10,
                rowList: [10,15,20,50],
                pager: true,
                gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                cached: true, //把用户自定义的列展示设置缓存在本地
                pageData: addProductInfo
            });
            $("#addProduct-grid").grid("setGridHeight", 250);
        },
        queryAddProductInfo:function(page, rowNum, sortname, sortorder){
            var rowData = $("#relateOrderDetailsTabGrid-grid").grid("getSelection");
            var param = {};
            param.srvOrdId = rowData.SRV_ORD_ID;
            rowNum = rowNum || this.$("#addProduct-grid").grid("getGridParam", "rowNum");
            fish.store.set('product-grid-rowNum', rowNum); //记录用户选择的每页记录数
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
            $("#addProduct-grid").blockUI({message: '加载中'}).data('blockui-content', true);
            orderDetailsAction.queryAddProductInfoBySrvOrdId(param,function (data) {
                $("#addProduct-grid").grid("reloadData", data);
                $(window).trigger("resize");
            });
            $("#addProduct-grid").unblockUI().data('blockui-content', false);
        },
        //页面关闭事件
        //为了调用代办回调函数，刷新各tab页数据
        /*closeClick : function() {
            this.popup.close();
        }*/

    });
}); //ALL END