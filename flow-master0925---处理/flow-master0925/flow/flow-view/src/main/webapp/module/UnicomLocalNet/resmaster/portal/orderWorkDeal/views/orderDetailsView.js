/**
 * 核查汇总
 */
define([
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/orderDetailsAction',
    'module/UnicomLocalNet/resmaster/portal/orderAbnormal/action/orderAppendAction',
    'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
    'text!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/templates/orderDetailsView.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/orderDetailsView.css'
], function(orderDetailsAction,orderAppendAction,orderStandbyAction,orderDetails,i18n,css) {

        var selectTab = 'circuit';//tabl标识
        var changeOrderLabel = "4B";//异常单子标识
        var sysResource;
        var URl;
        var userInfo;
        return fish.View.extend({
            resNetworkUrl: '',
            crmRegion: '',
            template: fish.compile(orderDetails),
            i18nData: fish.extend({}, i18n),
            events: {
                'click #tabs-order': 'tabsOrder',//定单详情tab页单击事件
                'click #tabs-circuit': 'tabsCircuit',//电路信息tab页单击事件
                'click #tabs-circuitSub': 'tabsCircuitSub',//电路信息tab页单击事件
                'click #tabs-attach': 'tabsAttach',//附件信息tab页单击事件
                'click #tabs-log': 'tabsLog',//日志信息tab页单击事件
                'click #tabs-changeorder': 'tabsChangeOrder',//异常单信息tab页单击事件
                'click #tabs-task-second': 'tabsSecondTask',//二干任务信息tab页单击事件
                'click #tabs-task-local': 'tabsLocalTask',//本地任务信息tab页单击事件
                'click #tabs-flow': 'tabsFlow',//流程图tab页单击事件
                'click #tabs-idea': 'tabsIdea',//阶段意见tab页单击事件
                'click #tabs-warning': 'tabsWarning',//预警超时tab页单击事件
                'click #tabs-dispatchOrder': 'tabsDispatchOrder',//调单tab页单击事件
                'click #tabs-resourceOrder-w': 'tabsResourceOrderW',//一干资源信息tab页单击事件
                'click #tabs-resourceOrder': 'tabsResourceOrder',//二干资源信息tab页单击事件
                'click #tabs-resourceOrder-y': 'tabsResourceOrderY',//本地资源信息tab页单击事件
                'click #tabs-feedback': 'tabsFeedbackOrder',//反馈信息tab页单击事件
                'click #tabs-product': 'tabsAddProduct',//附加产品tab页单击事件
                'click #tabs-otherInfo': 'tabsOrderOtherInfo',//反馈信息tab页单击事件
                'click .downloadClFile': 'downloadClFile',//反馈信息tab页单击事件
                'click .downloadFile': 'downloadFile',//异常单附件下载
                'click #tabs-engineeringTache': 'tabsEngineeringTache',//工建进度tab页单击事件
                'click #oldCustInfo': 'oldCustInfo',//原客户信息
                'click #tabs-provinceAutoTache': 'tabsProvinceAutoTache',//省内dia自动开通
                'click #tabs-activate': 'initActivateTache', //激活结果
                'click #tabs-equip-recycle': 'equipRecycleClick', //设备回收
                'click #tabs-postponement': 'postponementClick', //延期申请
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
                $("#tabs-pill,#tabsCirNum-pill,#changeOrder-tabs").tabs({
                    autoResizable: false,
                });
                var productTyep = this.options.serviceId;
                if (productTyep == "20181221006") {
                    $('#consumer-info').hide();
                    $('.subScribe').hide();
                }
                $("#tabs-order").click();
                var orderId = this.options.orderId +'';
                userInfo = this.options.userInfo;
                FLAG = orderDetailsAction.ifFromSecond(orderId).responseJSON.data;
                this.initFishInfo();

                //初始化异常通知
               // this.initExceptionTab();
            },
            initFishInfo : function() {
                var me = this;
                //初始化按钮
                var serviceId = me.options.serviceId;
                if(serviceId == '80000465'){ //如果产品是云组网，按钮查询分开
                    this.initCloudButton();
                }else {
                    this.initButton();
                }

                var woOrderBackFlags = me.options.woOrderBackFlags;
                if (woOrderBackFlags != '' && '1'.indexOf(woOrderBackFlags) != -1){
                    if('500001150'==me.options.tacheId){
                    }else{
                        $('#ifBackOrder').show();
                    }
                }
            },
            //隐藏二干调度的任务，以及资源信息展示的显示和隐藏
            hideSecTask: function (){
                //$("#tabsCirNum-pill").tabs("hideTab",7);
                $("#tabsCirNum-pill").tabs("hideTab",1);
                $("#tabsCirNum-pill").tabs("hideTab",2);
                $("#tabsCirNum-pill").tabs("hideTab",7);  //隐藏二干任务列表
                $("#tabsCirNum-pill").tabs("hideTab",10);  //隐藏对端省份信息

                if (FLAG || 'second-schedule-lt' == sysResource.SYSTEM_RESOURCE) {
                    //$("#tabs-task-second").css({display:""}); //二干任务列表
                    $("#tabsCirNum-pill").tabs("showTab",7); //展示二干任务列表
                }
                var resource = sysResource.RESOURCES;
                var resourceSys = sysResource.SYSTEM_RESOURCE;
                if('onedry' == resource){
                    $("#tabsCirNum-pill").tabs("showTab",1); //一干资源信息
                    $("#tabsCirNum-pill").tabs("showTab",10); //对端省份信息
                    if('second-schedule-lt' == resourceSys){ //一干下给二干，二干再下发本地
                        $("#tabsCirNum-pill").tabs("showTab",2); //二干资源信息
                    }else if ('flow-schedule-lt' == resourceSys){
                        $("#tabsCirNum-pill").tabs("hideTab",2); //二干资源信息
                    }
                }else if('secondary' == resource){
                    $("#tabsCirNum-pill").tabs("hideTab",1);
                    $("#tabsCirNum-pill").tabs("showTab",2);
                    $("#tabsCirNum-pill").tabs("hideTab",10);
                }else if('jike' == resource){
                    $("#tabsCirNum-pill").tabs("hideTab",10);
                    $("#tabsCirNum-pill").tabs("hideTab",1);
                    if(FLAG || 'second-schedule-lt' == resourceSys){ //集客下给二干，二干再下发本地
                        $("#tabsCirNum-pill").tabs("showTab",2);
                    }else if ('flow-schedule-lt' == resourceSys){
                        $("#tabsCirNum-pill").tabs("hideTab",2);
                    }
                }else{
                    $("#tabsCirNum-pill").tabs("hideTab",10);
                    $("#tabsCirNum-pill").tabs("hideTab",1);
                    $("#tabsCirNum-pill").tabs("hideTab",2);
                };
            },
            initCircuitTab:function(){
                //非局内客户电路拆机流程 展示设备回收列表
                var orderPsId = orderDetailsAction.qryParentPsIdBySubOrderId({orderId:this.options.orderId}).responseJSON.data.PS_ID;
                if(this.options.serviceId != "20181221006" && this.options.activeType == "102" && orderPsId == "1000214") {
                  //  $("#tabs-equip-recycle").show();
                    $("#tabsCirNum-pill").tabs("showTab",18);
                }else{
                   // $("#tabs-equip-recycle").hide();
                    $("#tabsCirNum-pill").tabs("hideTab",18);
                }
            },
            //初始化异常通知
            initExceptionTab: function () {
                // 异常的订单显示异常通知tab页
                if ('exceptionOrder' == this.options.buttonState) {
                    $("#tabs-exception").show();
                    this.initExceptionNoticeGrid();
                } else {
                    $("#tabs-exception").remove();
                }
            },
            //初始化反馈信息表格
            initFeedbackGrid: function(){
                var me = this;
                var feedbackInfo = $.proxy(this.queryFeedbackInfo(),this); //函数作用域改变
                $("#feedback-grid").grid({
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
                        {name: 'RES_SATISFY', label: '资源是否满足', width: 150, sortable: false},
                        {name: 'RES_PROVIDE_STAND_NAME', label: '资源提供方式', width: 150, sortable: false},
                        {name: 'BOARD_READY_NAME', label: '板卡备货情况', width: 150, sortable: false},
                        {name: 'TRANS_READY_NAME', label: '传输设备备货情况', width: 150, sortable: false},
                        {name: 'OPTICAL_READY_NAME', label: '光缆及配件备货情况', width: 150, sortable: false},
                        {name: 'BOARD_PERIOD', label: '板卡采购工期', width: 150, sortable: false},

                        {name: 'BOARD_AMOUNT', label: '板卡设备金额', width: 150, sortable: false},
                        {name: 'BOARD_TYPE', label: '板卡设备类型', width: 120, sortable: false},
                        {name: 'BOARD_MODEL', label: '板卡设备型号', width: 120, sortable: false},
                        {name: 'TRANS_PERIOD', label: '传输设备采购工期', width: 150, sortable: false},
                        {name: 'TRANS_AMOUNT', label: '传输设备金额', width: 150, sortable: false},
                        {name: 'TRANS_TYPE_NAME', label: '传输设备类型', width: 150, sortable: false},
                        {name: 'OTHER_TYPE', label: '其他设备类型', width: 150, sortable: false},
                        {name: 'TRANS_MODEL', label: '传输设备型号', width: 150, sortable: false},

                        {name: 'OPTICAL_PERIOD', label: '光缆及配件采购工期', width: 150, sortable: false},
                        {name: 'OPTICAL_AMOUNT', label: '客户接入工程造价概算', width: 120, sortable: false},
                        {name: 'CONSTRUCT_PERIOD_STAND', label: '建设工期', width: 120, sortable: false},
                        {name: 'PROJECT_AMOUNT', label: '工程造价概算', width: 150, sortable: false},
                        {name: 'PROJECT_OVERVIEW', label: '工程概述', width: 150, sortable: false},
                        {name: 'MUNICIPAL_APPROVAL_NAME', label: '是否需要市政报批', width: 150, sortable: false},
                        {name: 'APPROVAL_PERIOD', label: '报批工作日', width: 150, sortable: false},
                        {name: 'RES_DESC', label: '资源情况描述', width: 150, sortable: false},

                        {name: 'PROPERTY_REDLINE_NAME', label: '物业红线区域内施工是否涉及物业问题', width: 150, sortable: false},
                        {name: 'PROPERTY_DESC', label: '物业问题描述', width: 120, sortable: false},
                        {name: 'CUST_ROOM_NAME', label: '客户机房是否具备接入条件', width: 120, sortable: false},
                        {name: 'ACCESS_PROJECT_SCHEME', label: '接入工程方案', width: 150, sortable: false},
                        {name: 'RES_EXPLORER', label: '勘查人', width: 150, sortable: false},
                        {name: 'RES_EXPLOR_CONTACT', label: '勘查人联系电话', width: 150, sortable: false},
                        {name: 'RES_HAVE_NAME', label: '资源是否具备', width: 150, sortable: false},
                        {name: 'TOTAL_AMOUNT', label: '投资总金额', width: 150, sortable: false},
                        {name: 'LONGEST_PERIOD', label: '最长工期', width: 150, sortable: false},
                        {name: 'UNABLE_RELOVE', label: '备注', width: 150, sortable: false},
                        {name: 'ACCESS_CIR_TYPE_NAME', label: '接入电路类型', width: 150, sortable: false},
                        {name: 'OTHER_ACE_CIR_TYPE', label: '其他接入电路类型', width: 150, sortable: false},
                        {name: 'UPLINK_NODE_PORT', label: '上联节点及端口', width: 150, sortable: false}
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
                $("#feedback-grid").grid("setGridHeight", 327);
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
                rowNum = rowNum || this.$("#feedback-grid").grid("getGridParam", "rowNum");
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
                $("#feedback-grid").blockUI({message: '加载中'}).data('blockui-content', true);
                orderDetailsAction.queryFeedbackInfo(srvOrdId,function (data) {
                    $("#feedback-grid").grid("reloadData", data);
                    $(window).trigger("resize");
                }).always(function () {
                    $("#feedback-grid").unblockUI().data('blockui-content', false);
                });
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
                debugger
                var resourceOrderInfo = $.proxy(this.queryResourceOrderInfo(),this); //函数作用域改变
                $("#resource-grid").grid({
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
            orderApplyFormView: function(res){
                var me = this;
                me.completedViewBtn(res);
            },
            completedViewBtn:function(res){
                var selrow;
                debugger
                if('W' == res){
                    selrow = $("#resource-grid-w").grid("getSelection"); //获取选中的行数据
                }else if('R' == res){
                    selrow = $("#resource-grid").grid("getSelection"); //获取选中的行数据
                }else if('Y' == res){
                    selrow = $("#resource-grid-y").grid("getSelection"); //获取选中的行数据
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
                var dataTemp = $("#orderDetailsTabGrid-grid").grid("getSelection");
                if(dataTemp == ''
                    ||dataTemp == undefined
                    ||dataTemp.SRV_ORD_ID == ''
                    ||dataTemp.SRV_ORD_ID == undefined){
                    fish.toast('warn', "请选择一条电路信息");
                    return;
                }
                var srvordId = dataTemp.SRV_ORD_ID+'';
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
                }).always(function () {
                    $("#resource-grid-w").unblockUI().data('blockui-content', false);
                });
            },
            //查询二干资源信息的方法
            queryResourceOrderInfo:function(page, rowNum, sortname, sortorder){
                var dataTemp = $("#orderDetailsTabGrid-grid").grid("getSelection");
                if(dataTemp == ''
                    ||dataTemp == undefined
                    ||dataTemp.SRV_ORD_ID == ''
                    ||dataTemp.SRV_ORD_ID == undefined){
                    fish.toast('warn', "请选择一条电路信息");
                    return;
                }
                var srvordId = dataTemp.SRV_ORD_ID+'';
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
                    $(window).trigger("resize");
                }).always(function () {
                    $("#resource-grid").unblockUI().data('blockui-content', false);
                });
            },
            //查询本地资源信息的方法
            queryResourceOrderInfoY:function(page, rowNum, sortname, sortorder){
                var dataTemp = $("#orderDetailsTabGrid-grid").grid("getSelection");
                if(dataTemp == ''
                    ||dataTemp == undefined
                    ||dataTemp.SRV_ORD_ID == ''
                    ||dataTemp.SRV_ORD_ID == undefined){
                    fish.toast('warn', "请选择一条电路信息");
                    return;
                }
                var srvordId = dataTemp.SRV_ORD_ID+'';
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
                }).always(function () {
                    $("#resource-grid-y").unblockUI().data('blockui-content', false);
                });
            },
            // 初始化调单信息表格
            initDispatchGrid: function() {
                var changeOrderInfo = $.proxy(this.queryDispatchOrderInfo(),this); //函数作用域改变
                $("#dispatch-grid").grid({
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
                        var dispatchInfo = $("#dispatch-grid").grid("getSelection");
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

                        $("#dispatch-info").show();
                        document.getElementById('dispatch_order_no').innerHTML=dispatchInfo.DISPATCH_ORDER_NO;
                        document.getElementById('staff_name').innerHTML=dispatchInfo.STAFF_NAME;
                        document.getElementById('staff_tel').innerHTML=dispatchInfo.STAFF_TEL;
                        document.getElementById('staff_org').innerHTML=dispatchInfo.STAFF_ORG;
                        document.getElementById('dispatch_type').innerHTML=dispatchInfo.DISPATCH_TYPE;
                        document.getElementById('state').innerHTML=dispatchInfo.STATE;
                        document.getElementById('dispatch_grade').innerHTML=dispatchInfo.DISPATCH_GRADE;
                        document.getElementById('dispatch_urgency').innerHTML=dispatchInfo.DISPATCH_URGENCY;
                        document.getElementById('send_date').innerHTML=dispatchInfo.SEND_DATE;
                        document.getElementById('dispatch_title').innerHTML=dispatchInfo.DISPATCH_TITLE;
                        document.getElementById('send_org').innerHTML=dispatchInfo.DISPATCH_SEND_ORG;
                        document.getElementById('copy_org').innerHTML=dispatchInfo.DISPATCH_COPY_ORG;
                        document.getElementById('dispatch_text').innerText=dispatchInfo.DISPATCH_TEXT;
                        document.getElementById('dispatch_remark').innerHTML=dispatchInfo.REMARK;
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
                        var attachs = document.getElementById('attachs');
                        attachs.innerHTML=null;
                        var html=null;
                        orderDetailsAction.queryDispatchAttachInfo(dispatchOrderId.toString(),function (data){
                            for(var i =0;i <data.length; i++){
                                 //html = '<div class=\"btn-group\"><button type=\"button\"  class=\"btn btn-link downLoadAttach\"  onclick=\"javascript:downLoadAttach('+fileId+','+fileName+','+fileType+','+filePath+')\">'+data[i].FILE_NAME+'</button></div>';
                                html = '<div class=\"btn-group\"><button id=\"'+ data[i].FILE_ID+ '\"  type=\"button\" value=\"'+ data[i].FILE_PATH+ '\" title=\"'+ data[i].FILE_TYPE+ '\" class=\"btn btn-link downLoadAttach\">'+data[i].FILE_NAME+'</button></div>';
                                $('#attachs').append(html);
                            }
                                $("#attachs").on("click",".downLoadAttach",function () {
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
                $("#dispatch-grid").grid("setGridHeight", 160);
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

                rowNum = rowNum || this.$("#dispatch-grid").grid("getGridParam", "rowNum");
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
                $("#dispatch-grid").blockUI({message: '加载中'}).data('blockui-content', true);
                if('localStandy' == selectType){
                    orderDetailsAction.queryDispatchOrderInfo(param,function (data) {
                        $("#dispatch-grid").grid("reloadData", data);
                        $(window).trigger("resize");
                    }).always(function () {
                        $("#dispatch-grid").unblockUI().data('blockui-content', false);
                    });
                }else{
                    orderDetailsAction.queryDispatchOrderInfoByCstOrdId(param,function (data) {
                        $("#dispatch-grid").grid("reloadData", data);
                        $(window).trigger("resize");
                    }).always(function () {
                        $("#dispatch-grid").unblockUI().data('blockui-content', false);
                    });
                }

            },
            //初始化异常单信息表格
            initChangeOrderGrid: function() {
                var me = this;
                var changeOrderInfo = $.proxy(this.queryChangeOrderInfo(null, null, null, null),this); //函数作用域改变
                $("#changeOrder-grid").grid({
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
                $("#changeOrder-grid").grid("setGridHeight", 327);
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
                var rowData = $("#changeOrder-grid").grid('getRowData', rowid);
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
                $("#attach-grid").grid({
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
                            var data = $("#attach-grid").grid("getRowData",rowid);
                            var param = new Object();
                            param.fileName = data.FILE_NAME;
                            param.filePath = data.FILE_PATH;
                            param.fileId = data.FILE_ID +'.'+ data.FILE_TYPE;
                            orderDetailsAction.downFile("localScheduleLT/orderDetails/fileDownload.spr",param);
                        }
                    }
                });
                $("#attach-grid").grid("setGridHeight", 327);
            },
            // 初始化阶段性意见信息表格
            initIdeaGrid: function() {
                var ideaInfo = $.proxy(this.queryIdeaInfo(),this); //函数作用域改变
                $("#idea-grid").grid({
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
                $("#idea-grid").grid("setGridHeight", 327);
            },
            //查询阶段性意见的方法
            queryIdeaInfo:function(page, rowNum, sortname, sortorder){
                var me = this;
                var dataIdea = $("#orderDetailsTabGrid-grid").grid("getSelection");
                if(dataIdea == ''
                    ||dataIdea == undefined
                    ||dataIdea.SRV_ORD_ID == ''
                    ||dataIdea.SRV_ORD_ID == undefined){
                    fish.toast('warn', "请选择一条电路信息");
                    return;
                }
                var orderId = dataIdea.ORDER_ID+'';
                var srvordId = dataIdea.SRV_ORD_ID+'';
                rowNum = rowNum || this.$("#idea-grid").grid("getGridParam", "rowNum");
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
                $("#idea-grid").blockUI({message: '加载中'}).data('blockui-content', true);
                orderDetailsAction.queryIdeaInfoBySrvOrdId(srvordId,function (data) {
                    $("#idea-grid").grid("reloadData", data);
                    $(window).trigger("resize");
                }).always(function () {
                    $("#idea-grid").unblockUI().data('blockui-content', false);
                });
            },
            // 初始化预警超时信息表格
            initWarningGrid: function() {
                var warningInfo = $.proxy(this.queryWarningInfo(),this); //函数作用域改变
                $("#warning-grid").grid({
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
                $("#warning-grid").grid("setGridHeight", 327);
            },
            //查询预警超时信息
            queryWarningInfo:function(page, rowNum, sortname, sortorder){
                var me = this;
                var dataWarn = $("#orderDetailsTabGrid-grid").grid("getSelection");
                if(dataWarn == ''
                    ||dataWarn == undefined
                    ||dataWarn.SRV_ORD_ID == ''
                    ||dataWarn.SRV_ORD_ID == undefined){
                    fish.toast('warn', "请选择一条电路信息");
                    return;
                }

                var orderId = dataWarn.ORDER_ID+'';
                rowNum = rowNum || this.$("#warning-grid").grid("getGridParam", "rowNum");
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
                $("#warning-grid").blockUI({message: '加载中'}).data('blockui-content', true);
                orderDetailsAction.queryWarningInfo(orderId,function (data) {
                    $("#warning-grid").grid("reloadData", data);
                    $(window).trigger("resize");
                }).always(function () {
                    $("#warning-grid").unblockUI().data('blockui-content', false);
                });
            },
            // 初始化关联主/子单信息表格
            initRelevanceGrid: function() {
                var relevanceInfo = $.proxy(this.queryRelevanceOrderInfo(),this); //函数作用域改变
                $("#relevance-grid").grid({
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
                var dataRel = $("#orderDetailsTabGrid-grid").grid("getSelection");
                if(dataRel == ''
                    ||dataRel == undefined
                    ||dataRel.SRV_ORD_ID == ''
                    ||dataRel.SRV_ORD_ID == undefined){
                    fish.toast('warn', "请选择一条电路信息");
                    return;
                }
                var orderId = dataRel.ORDER_ID+'';
                rowNum = rowNum || this.$("#relevance-grid").grid("getGridParam", "rowNum");
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
                $("#relevance-grid").blockUI({message: '加载中'}).data('blockui-content', true);
                orderDetailsAction.queryRelevanceOrderInfo(orderId,function (data) {
                    $("#relevance-grid").grid("reloadData", data);
                    $(window).trigger("resize");
                }).always(function () {
                    $("#relevance-grid").unblockUI().data('blockui-content', false);
                });
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
                $("#task-grid-second").grid({
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
                $("#task-grid-second").grid("setGridHeight", 327);
            },
            taskSecondFormView: function(e, rowid, iRow, iCol){
                var me = this;
                me.taskDetailViewBtn(e, rowid, iRow, iCol,"二干任务列表详情");
            },
            //查看二干任务裂变详情
            taskDetailViewBtn:function(e, rowid, iRow, iCol,titleValue){
                //判断是二干还是本地
                if(titleValue=="二干任务列表详情"){
                    var selrow = $("#task-grid-second").grid("getSelection"); //获取选中的行数据
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
                $("#task-grid-local").grid(dataInfo);
                $("#task-grid-local").grid("setGridHeight", 327);
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
                        $("#task-grid-local").grid("setSelection",parentRowId);
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
                var dataTashInfo = $("#orderDetailsTabGrid-grid").grid("getSelection");
                if(dataTashInfo == ''
                    ||dataTashInfo == undefined
                    ||dataTashInfo.SRV_ORD_ID == ''
                    ||dataTashInfo.SRV_ORD_ID == undefined){
                    fish.toast('warn', "请选择一条电路信息");
                    return;
                }
                var orderId = dataTashInfo.ORDER_ID+'';
                rowNum = rowNum || this.$("#task-grid-second").grid("getGridParam", "rowNum");
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
                $("#task-grid-second").blockUI({message: '加载中'}).data('blockui-content', true);
                orderDetailsAction.querySecTaskInfo(orderId,function (data) {
                    $("#task-grid-second").grid("reloadData", data);
                }).always(function () {
                    $("#task-grid-second").unblockUI().data('blockui-content', false);
                });
            },
            //本地任务列表查询方法
            querySecToLocalTaskInfo:function(page, rowNum, sortname, sortorder){
                var me = this;
                var dataTashInfo = $("#orderDetailsTabGrid-grid").grid("getSelection")
                if(dataTashInfo == ''
                    ||dataTashInfo == undefined
                    ||dataTashInfo.SRV_ORD_ID == ''
                    ||dataTashInfo.SRV_ORD_ID == undefined){
                    fish.toast('warn', "请选择一条电路信息");
                    return;
                }
                var orderId = dataTashInfo.ORDER_ID+'';
                rowNum = rowNum || this.$("#task-grid-local").grid("getGridParam", "rowNum");
                fish.store.set('task-grid-local-rowNum', rowNum); //记录用户选择的每页记录数
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
                $("#task-grid-local").blockUI({message: '加载中'}).data('blockui-content', true);
                orderDetailsAction.queryLocalTaskInfo(orderId,function (data) {
                    $("#task-grid-local").grid("reloadData", data);
                }).always(function () {
                    $("#task-grid-local").unblockUI().data('blockui-content', false);
                });
            },
            //查询本地的任务方法
            initTaskSubGrid:function (subGridId, parentRowId, meTemp) {
                var me = this;
                var dataSub = $("#task-grid-local").grid("getSelection");
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
                $("#log-grid").grid({
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
                $("#log-grid").grid("setGridHeight", 327);
            },
            //查询日志信息的方法
            queryLogInfo:function(page, rowNum, sortname, sortorder){
                var me = this;
                var datalog = $("#orderDetailsTabGrid-grid").grid("getSelection");
                if(datalog == ''
                    ||datalog == undefined
                    ||datalog.SRV_ORD_ID == ''
                    ||datalog.SRV_ORD_ID == undefined){
                    fish.toast('warn', "请选择一条电路信息");
                    return;
                }
                var orderId = datalog.ORDER_ID + '';
                rowNum = rowNum || this.$("#log-grid").grid("getGridParam", "rowNum");
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
                    $("#log-grid").grid("reloadData", data);
                    $(window).trigger("resize");
                }).always(function () {
                    $("#log-grid").unblockUI().data('blockui-content', false);
                });
            },
            //查询附件信息方法
            queryAttachInfo:function(page, rowNum, sortname, sortorder){
                var me = this;
                var dataAttach = $("#orderDetailsTabGrid-grid").grid("getSelection");
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
                rowNum = rowNum || this.$("#attach-grid").grid("getGridParam", "rowNum");
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
                $("#attach-grid").blockUI({message: '加载中'}).data('blockui-content', true);
                orderDetailsAction.queryAttachInfo(params,function (data){
                    $("#attach-grid").grid("reloadData", data);
                    $(window).trigger("resize");
                }).always(function () {
                    $("#attach-grid").unblockUI().data('blockui-content', false);
                });
            },
            //查询异常单信息的方法
            queryChangeOrderInfo: function(page, rowNum, sortname, sortorder) {
                var me = this;
                var dataChange = $("#orderDetailsTabGrid-grid").grid("getSelection");
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
                var rowNum = rowNum || this.$("#changeOrder-grid").grid("getGridParam", "rowNum");
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
                $("#changeOrder-grid").blockUI({message: '加载中'}).data('blockui-content', true);
                orderDetailsAction.queryChangeOrderInfo(obj,function (data) {
                    $("#changeOrder-grid").grid("reloadData", data);
                    $(window).trigger("resize");
                }).always(function () {
                    $("#changeOrder-grid").unblockUI().data('blockui-content', false);
                });
            },
            //初始化对单测试人信息表格
            initOrderOtherInfo: function() {
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
                    /*onDblClickRow: function (e, rowid, iRow, iCol) {//双击行事件
                        me.orderApplyFormView('W');
                    },*/
                });
                $("#otherInfo-grid").grid("setGridHeight", 327);
            },
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
                var dataChange = $("#orderDetailsTabGrid-grid").grid("getSelection");
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
                var rowNum = rowNum || this.$("#otherInfo-grid").grid("getGridParam", "rowNum");
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
                }).always(function () {
                    $("#otherInfo-grid").unblockUI().data('blockui-content', false);
                });
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
                me.initCircuitTab();
                if('101'==me.options.orderType && 'jike'== me.options.RESOURCES){
                    me.girdMdel = [
                            {
                                name: 'SOURCENETWORK', label: '网络拓扑图', width: 100, formatter: function () {
                                    return '点击查看'
                                }
                            },
                            {
                                name: 'ROUTEPRESENTATION',
                                label: '路由呈现',
                                hidden: true,
                                width: 100,
                                formatter: function () {
                                    return '点击查看'
                                }
                            },
                            {name: 'WOORDERBACKFLAG', label: '是否退单', width: 80, formatter: me.ifBackOrder},
                            {name: 'SERVICENAME', label: '产品类型', width: 100},
                            {name: 'A_IF_RES_HAVE', label: 'A端资源是否具备', width: 100, formatter: me.azIfResHaveEnum},
                            {name: 'Z_IF_RES_HAVE', label: 'Z端资源是否具备', width: 100, formatter: me.azIfResHaveEnum},
                            {name: 'CST_ORD_ID', label: '客户Id', width: 140, hidden: true},
                            {name: 'SRV_ORD_ID', label: '业务订单信息Id', width: 140, hidden: true},
                            {name: 'DISPATCH__ORDER_ID', label: '调单Id', width: 140, hidden: true},
                            {name: 'ORDER_ID', label: '流程订单Id', width: 140, hidden: true},
                            {name: 'SERVICE_ID', label: '产品类型Id', width: 140, hidden: true},
                            {name: 'ORDER_TYPE', label: '订单类型', width: 140, hidden: true},
                            {name: 'SUBSCRIBE_ID', label: '客户订单号', width: 120, sortable: false},
                            {name: 'SERIAL_NUMBER', label: '业务号码', width: 120, sortable: false},
                            {name: 'CIRCUITCODE', label: '电路编号', width: 120, sortable: false},
                            {name: 'TRADE_ID', label: '业务订单号', width: 120},
                            {name: 'RELATETRADEID', label: '关联核查单业务订单号', width: 180, style: {color: '#6DCC4A'}},
                            {name: 'AREGIONNAME', label: 'A端所属区域', width: 110, sortable: false},
                            {name: 'ZREGIONNAME', label: 'Z端所属区域', width: 110, sortable: false},
                            {name: 'A_INSTALLED_ADD', label: 'A端装机地址', width: 110, sortable: false},
                            {name: 'Z_INSTALLED_ADD', label: 'Z端装机地址', width: 110, sortable: false},
                            {name: 'A_REQ_FIN_DATE', label: 'A端要求完成时间', width: 135, sortable: false},
                            {name: 'Z_REQ_FIN_DATE', label: 'Z端要求完成时间', width: 135, sortable: false}
                        ];
                }else{
                     me.girdMdel = [
                            {
                                name: 'SOURCENETWORK', label: '网络拓扑图', width: 100, formatter: function () {
                                    return '点击查看'
                                }
                            },
                            {
                                name: 'ROUTEPRESENTATION',
                                label: '路由呈现',
                                hidden: true,
                                width: 100,
                                formatter: function () {
                                    return '点击查看'
                                }
                            },
                            {name: 'WOORDERBACKFLAG', label: '是否退单', width: 80, formatter: me.ifBackOrder},
                            {name: 'SERVICENAME', label: '产品类型', width: 100},
                            {name: 'A_IF_RES_HAVE', label: 'A端资源是否具备', width: 100, formatter: me.azIfResHaveEnum},
                            {name: 'Z_IF_RES_HAVE', label: 'Z端资源是否具备', width: 100, formatter: me.azIfResHaveEnum},
                            {name: 'CST_ORD_ID', label: '客户Id', width: 140, hidden: true},
                            {name: 'SRV_ORD_ID', label: '业务订单信息Id', width: 140, hidden: true},
                            {name: 'DISPATCH__ORDER_ID', label: '调单Id', width: 140, hidden: true},
                            {name: 'ORDER_ID', label: '流程订单Id', width: 140, hidden: true},
                            {name: 'SERVICE_ID', label: '产品类型Id', width: 140, hidden: true},
                            {name: 'ORDER_TYPE', label: '订单类型', width: 140, hidden: true},
                            {name: 'SUBSCRIBE_ID', label: '客户订单号', width: 120, sortable: false},
                            {name: 'SERIAL_NUMBER', label: '业务号码', width: 120, sortable: false},
                            {name: 'CIRCUITCODE', label: '电路编号', width: 120, sortable: false},
                            {name: 'TRADE_ID', label: '业务订单号', width: 120},
                            {name: 'AREGIONNAME', label: 'A端所属区域', width: 110, sortable: false},
                            {name: 'ZREGIONNAME', label: 'Z端所属区域', width: 110, sortable: false},
                            {name: 'A_INSTALLED_ADD', label: 'A端装机地址', width: 110, sortable: false},
                            {name: 'Z_INSTALLED_ADD', label: 'Z端装机地址', width: 110, sortable: false},
                            {name: 'A_REQ_FIN_DATE', label: 'A端要求完成时间', width: 135, sortable: false},
                            {name: 'Z_REQ_FIN_DATE', label: 'Z端要求完成时间', width: 135, sortable: false}];
                }

                $("#orderDetailsTabGrid-grid").grid({
                    colModel:me.girdMdel,
                    rownumbers: true,
                    autowidth: true,
                    multiselect: false,
                    shrinkToFit: false, //表格列宽是否按比例缩放，默认true
                    height: 160,
                    pageData: me.qrycircuitInfoGrid(),
                    afterInsertRow: function (e, rowid, pageData) {
                        $("#orderDetailsTabGrid-grid").grid('setCell', rowid, 'SOURCENETWORK', '', {color: '#6DCC4A'});
                        if('0'===userInfo.isShow){
                            $("#orderDetailsTabGrid-grid").grid('setCell', rowid, 'ROUTEPRESENTATION', '', {color: '#6DCC4A'});
                        }

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
                    reloadGrid:function(e, rowid,pageData) {
                        if ('0' === userInfo.isShow) {
                            $("#orderDetailsTabGrid-grid").grid('showCol', 'ROUTEPRESENTATION');
                        }

                    },
                    onCellSelect: function (e, rowid, iCol, cellcontent, colName, cellval) {//选中单元格的事件
                        var dataCell = $("#orderDetailsTabGrid-grid").grid("getRowData",rowid);
                        debugger
                        crmRegionMap = orderDetailsAction.qryMsmSwitchByArea(userInfo.areaId).responseJSON.data;
                        if(crmRegionMap !=''
                            && crmRegionMap != undefined){
                            this.crmRegion = crmRegionMap.CRM_REGION;
                        }
                        if(iCol == 1){
                            var paramsRes={
                                objId:'1111111111111111111111111',
                                objType:2559,
                                objParam: {'MULTI_DATA_SOURCE_CONFIG_REGION_CODE_FOR_TOPO':this.crmRegion,
                                    'OUTER_SYS_PASS_CIRCUIT_NO_VALUE':dataCell.CIRCUITCODE},
                                objName:dataCell.CIRCUITCODE,
                                topoDefId:230004,
                                isReaderCache:true,
                                viewPathId:'1901201',
                                topoName:dataCell.CIRCUITCODE
                            };
                            this.resNetworkUrl = orderDetailsAction.qryInterfaceUrl('ResourceNetWork').responseJSON.data;
                            window.open(this.resNetworkUrl+"&params="+fish.TripleDES.encrypt(JSON.stringify(paramsRes),'zte-soft'));
                        }
                        if(iCol == 2 && '0'===userInfo.isShow){
                            var routeParam = {};
                            routeParam.codeValue = '/businessAnalysis/route';
                            routeParam.codeType = 'ROUTE_PRESENTATION';
                            var routeInfo = orderDetailsAction.queryRouteInfoUrl(routeParam).responseJSON.data;
                            var routeInfoUrl = routeInfo[0].CODE_CONTENT+'&code='+userInfo.regionId+'&userCode＝'+userInfo.name+'&srvOrdId='+dataCell.SRV_ORD_ID;
                            window.open(routeInfoUrl);
                        }

                        //modify by wang.gang2 数据可能有空格
                        if(iCol == 15 && '101' == me.options.orderType && 'jike'== me.options.RESOURCES && ''!= dataCell.CHECK_CST_ORD_ID && ''!=dataCell.CHECK_ORDER_ID ){
                            var pop = fish.popupView({
                                url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/relateOrderDetailsView',
                                width: "95%",
                                height: "95%",
                                title: "工单详情",
                                modal: false,
                                viewOption: {
                                    orderId: dataCell.CHECK_ORDER_ID,
                                    RESOURCES: me.options.RESOURCES,
                                    srvordId: dataCell.CHECK_SRV_ORD_ID, //业务订单id
                                    cstOrdId: dataCell.CHECK_CST_ORD_ID, //客户订单id
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
                $("#orderDetailsTabGrid-grid").blockUI({message: '查询中'}).data('blockui-content', true);
                orderDetailsAction.queryCircuitInfoGrid(param,function (res) {
                    if(res.flag == 1){
                        $("#orderDetailsTabGrid-grid").grid("reloadData", res.data);
                        var rowdataOne = $("#orderDetailsTabGrid-grid").grid("getRowData")[0];
                        $("#orderDetailsTabGrid-grid").grid("setSelection",rowdataOne,false);
                        if (rowdataOne ) {
                          //  $('#tabs-feedback').find("a").show();
                            $("#tabsCirNum-pill").tabs("showTab",4);
                        }else{
                            $("#tabsCirNum-pill").tabs("hideTab",4);
                        }
                        me.tabsCircuitSub();
                    }else {
                        fish.toast('error', res.message);
                    }
                }).always(function () {
                    $("#orderDetailsTabGrid-grid").unblockUI().data('blockui-content', false);
                });
            },

            ifBackOrder : function (value){
                var ifBackOrderFlag = '否';
                if (value!= '' && '1'.indexOf(value) != -1){
                    ifBackOrderFlag = '是';
                }else if (value == '') {
                    ifBackOrderFlag = '-1';
                    $('#orderDetailsTabGrid-grid').grid('hideCol', 'WOORDERBACKFLAG');
                }
                return ifBackOrderFlag;
            },
            azIfResHaveEnum: function (value) {
                var enumTypeMap = {
                    '0':'否',
                    '1':'是',
                    '':''
                };
                return enumTypeMap[value];
            },

            //订单详情"每条电路信息"tab页点击事件
            tabsCircuitSub:function(){
                //每次点击"电路信息"tab页，都要清空<div id='order-circuit-info'></div>节点下的东西
                //不然会有重复数据填充
                selectTab = 'circuit';
                $("#otherInfo").empty();
                this.queryCircuitInfo();
            },
            //定单详情"附件信息"tab页点击事件
            tabsAttach:function(){
                var me = this;
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
                selectTab = 'localTask';
                this.localTaskGrid();
            },
            //"流程"tab页点击事件
            tabsFlow: function () {
                selectTab = 'flow';
                this.initFlowInfo(this);
            },
            //"日志"tab页点击事件
            tabsLog:function(){
                //初始化"日志"tab页的grid
                selectTab = 'log';
                this.initLogGrid();
            },
            //定单详情"异常单信息"tab页点击事件
            tabsChangeOrder : function(){
                //每次点击"异常单信息"tab页，都默认选中"加急"子tab页
                // $("#tabs-change-a").selected();
                //初始化异常单tab页的grid
                //4B：加急；4C：延期；4D：撤业务订单;4E：挂起；4F：解挂;
                selectTab = 'changeOrder';
                this.initChangeOrderGrid();
                // $('#tabs-change-a').click();
            },
            //阶段性意见Tab页点击事件
            tabsIdea:function(){
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
            //预警超时tab页点击事件
            tabsWarning:function(){
                selectTab = 'warn';
                this.initWarningGrid();
            },
            //调单tab页点击事件
            tabsDispatchOrder:function(){
                // selectTab = 'dispatch';
                $("#dispatch-info").hide();
                this.initDispatchGrid();
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
            //反馈信息tab页点击事件
            tabsFeedbackOrder :function(){
                selectTab = 'checkOrder';
                this.initFeedbackGrid();
            },
            //查询客户信息方法
            queryConsumerInfo:function () {
                var me = this;
                var cstOrdId = me.options.cstOrdId+'';
                var srvOrdIds = me.options.srvOrdIds+'';
                var srvordId = me.options.srvOrdId+'';
                var selectType = me.options.selectType;
                var productTyep = me.options.serviceId;
                $('#orderDeatail').empty();
                var orderDetailAll = ''; //定单信息
                orderDetailsAction.queryConsumerInfoByCustId(cstOrdId,function (data) {
                    if (data != undefined && data != null) {
                        //一干来单显示客户地址字段
                        if (data.RESOURCES == 'onedry') {
                            $('.oneDry').show();
                        }
                        if (data.CUST_NAME_CHINESE == undefined){
                            document.getElementById("cust_name_chinese").innerText="";//客户名称
                        }else{
                            document.getElementById("cust_name_chinese").innerText=data.CUST_NAME_CHINESE;//客户名称
                        }
                        if (data.CUST_ID == undefined){
                            document.getElementById("cust_id").innerText="";//客户编码
                        }else{
                            document.getElementById("cust_id").innerText=data.CUST_ID;//客户编码
                        }
                        if (data.CUST_TYPE == undefined){
                            document.getElementById("cust_type").innerText="";//客户类型
                        }else{
                            document.getElementById("cust_type").innerText=data.CUST_TYPE;//客户类型
                        }
                        if (data.CUST_INDUSTRY == undefined){
                            document.getElementById("cust_industry").innerText="";//客户行业
                        }else{
                            document.getElementById("cust_industry").innerText=data.CUST_INDUSTRY;//客户行业
                        }
                        if (data.IS_GROUP_CUST == undefined){
                            document.getElementById("is_group_cust").innerText="";//是否集团直管
                        }else{
                            document.getElementById("is_group_cust").innerText=data.IS_GROUP_CUST;//是否集团直管
                        }
                        if (data.CUST_CONTACT_MAN_NAME == undefined){
                            document.getElementById("cust_contact_man_name").innerText="";//客户联系人
                        }else{
                            document.getElementById("cust_contact_man_name").innerText=data.CUST_CONTACT_MAN_NAME;//客户联系人
                        }
                        if (data.CUST_CONTACT_MAN_TEL == undefined){
                            document.getElementById("cust_contact_man_tel").innerText="";//客户联系人电话
                        }else{
                            document.getElementById("cust_contact_man_tel").innerText=data.CUST_CONTACT_MAN_TEL;//客户联系人电话
                        }
                        if (data.CUST_CONTACT_MAN_EMAIL == undefined){
                            document.getElementById("cust_contact_man_email").innerText="";//客户联系人邮箱
                        }else{
                            document.getElementById("cust_contact_man_email").innerText=data.CUST_CONTACT_MAN_EMAIL;//客户联系人邮箱
                        }
                        if (data.CUST_ADDRESS == undefined){
                            document.getElementById("cust_addr").innerText="";//客户地址
                        }else{
                            document.getElementById("cust_addr").innerText=data.CUST_ADDRESS;//客户地址
                        }
                        if (data.CUST_TEL == undefined){
                            document.getElementById("cust_tel").innerText="";//客户联系电话
                        }else{
                            document.getElementById("cust_tel").innerText=data.CUST_TEL;//客户联系电话
                        }
                        if (data.CUST_EMAIL == undefined){
                            document.getElementById("cust_email").innerText="";//客户联系邮箱
                        }else{
                            document.getElementById("cust_email").innerText=data.CUST_EMAIL;//客户联系邮箱
                        }
                        if (data.CERTI_TYPE_CODE == undefined){
                            document.getElementById("certi_type_code").innerText="";//客户证件类型
                        }else{
                            document.getElementById("certi_type_code").innerText=data.CERTI_TYPE_CODE;//客户证件类型
                        }
                        if (data.CERTI_CODE == undefined){
                            document.getElementById("certi_code").innerText="";//证件编码
                        }else{
                            document.getElementById("certi_code").innerText=data.CERTI_CODE;//证件编码
                        }
                        if (data.RENAMEORDER != undefined && data.RENAMEORDER != ''){
                            $('.oldCust').show();

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
                            $('#orderDeatail').append(orderDetailAll);
                            //查询申请单附件信息
                            var cstParam = {};
                            cstParam.cstOrdId = cstOrdId;
                            // cstParam.cstOrdId = 6611;
                            orderDetailsAction.queryApplyAttachInfo(cstParam,function(res){
                                if(res != null && res != undefined){
                                    var attachHTML = '';
                                    for(var i =0;i <res.length; i++){
                                        attachHTML = '<div class=\"btn-group\"><button id=\"'+ res[i].FILE_ID+ '\"  type=\"button\" value=\"'+ res[i].FILE_PATH+ '\" title=\"'+ res[i].FILE_TYPE+ '\" class=\"btn btn-link downLoadAttach\">'+res[i].FILE_NAME+'</button></div>';
                                        $('#applyAttachInfo').append(attachHTML);
                                    }
                                    $("#applyAttachInfo").on("click",".downLoadAttach",function () {
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
                var dataTemp = $("#orderDetailsTabGrid-grid").grid("getSelection");
                if(dataTemp == ''
                    ||dataTemp == undefined
                    ||dataTemp.SRV_ORD_ID == ''
                    ||dataTemp.SRV_ORD_ID == undefined){
                    fish.toast('warn', "请选择一条电路信息");
                    return;
                }
                var srvOrdId = dataTemp.SRV_ORD_ID+'';
                var serviceId = dataTemp.SERVICE_ID+'';
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
                            $("#apeInfo").append(_circuitAPE);
                            $("#zceInfo").append(_circuitZCE);
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
                                    '<div  class="form-order-info" style="word-wrap:break-word">';
                                _circuita +=  + (otherInfo[i].OLD_ATTR_VALUE ==undefined||otherInfo[i].OLD_ATTR_VALUE =="")?"":'<font style="font-weight: bolder;color: red">';
                                _circuita +=(otherInfo[i].ATTR_VALUE ==undefined?"" : otherInfo[i].ATTR_VALUE);
                                _circuita +=  + (otherInfo[i].OLD_ATTR_VALUE ==undefined||otherInfo[i].OLD_ATTR_VALUE =="")?"":'</font>';
                                _circuita +=  + (otherInfo[i].OLD_ATTR_VALUE ==undefined||otherInfo[i].OLD_ATTR_VALUE =="")?"":('(原：'+otherInfo[i].OLD_ATTR_VALUE+')');
                                _circuita += '</div></div>';
                                _circuita += '<div class="col-md-3 pdb-10">'+
                                    '<label class="control-label">'+(otherInfo[i+1].PROPERTY_NAME == ""?"":(otherInfo[i+1].PROPERTY_NAME+':'))+'</label>'+
                                    '<div  class="form-order-info" style="word-wrap:break-word">';
                                _circuita +=  (otherInfo[i+1].OLD_ATTR_VALUE ==undefined||otherInfo[i+1].OLD_ATTR_VALUE =='')?'':'<font style="font-weight: bolder;color: red">';
                                _circuita += (otherInfo[i+1].ATTR_VALUE ==undefined?'': otherInfo[i+1].ATTR_VALUE);
                                _circuita +=  (otherInfo[i+1].OLD_ATTR_VALUE ==undefined||otherInfo[i+1].OLD_ATTR_VALUE =='')?'':'</font>';
                                _circuita +=  (otherInfo[i+1].OLD_ATTR_VALUE ==undefined||otherInfo[i+1].OLD_ATTR_VALUE =='')?'':('(原：'+otherInfo[i+1].OLD_ATTR_VALUE+')');
                                _circuita += '</div></div>';
                                _circuita += '<div class="col-md-3 pdb-10">'+
                                    '<label class="control-label">'+(otherInfo[i+2].PROPERTY_NAME == ""?"":(otherInfo[i+2].PROPERTY_NAME+':'))+'</label>'+
                                    '<div  class="form-order-info" style="word-wrap:break-word">';
                                _circuita += (otherInfo[i+2].OLD_ATTR_VALUE ==undefined||otherInfo[i+2].OLD_ATTR_VALUE =="")?"":'<font style="font-weight: bolder;color: red">';
                                _circuita +=(otherInfo[i+2].ATTR_VALUE ==undefined?"" : otherInfo[i+2].ATTR_VALUE);
                                _circuita += (otherInfo[i+2].OLD_ATTR_VALUE ==undefined||otherInfo[i+2].OLD_ATTR_VALUE =="")?"":'</font>';
                                _circuita += (otherInfo[i+2].OLD_ATTR_VALUE ==undefined||otherInfo[i+2].OLD_ATTR_VALUE =="")?"":("(原："+otherInfo[i+2].OLD_ATTR_VALUE+")");
                                _circuita += '</div></div>';
                                _circuita += '<div class="col-md-3 pdb-10">'+
                                    '<label class="control-label">'+(otherInfo[i+3].PROPERTY_NAME == ""?"":(otherInfo[i+3].PROPERTY_NAME+':'))+'</label>'+
                                    '<div  class="form-order-info" style="word-wrap:break-word">';
                                _circuita += (otherInfo[i+3].OLD_ATTR_VALUE ==undefined||otherInfo[i+3].OLD_ATTR_VALUE =="")?"":'<font style="font-weight: bolder;color: red">';
                                _circuita +=(otherInfo[i+3].ATTR_VALUE ==undefined?"" : otherInfo[i+3].ATTR_VALUE);
                                _circuita += (otherInfo[i+3].OLD_ATTR_VALUE ==undefined||otherInfo[i+3].OLD_ATTR_VALUE =="")?"":'</font>';
                                _circuita += (otherInfo[i+3].OLD_ATTR_VALUE ==undefined||otherInfo[i+3].OLD_ATTR_VALUE =="")?"":("(原："+otherInfo[i+3].OLD_ATTR_VALUE+")");
                                _circuita += '</div></div>';
                                _circuita += '</div>';
                            }
                            $("#otherInfo").append(_circuita);
                        }
                    }else{
                        fish.toast('error', data.message);
                    }
                });
                $(window).trigger("resize");
            },
            // 初始化流程信息
            initFlowInfo: function (meFlow) {
                var rowData = $("#orderDetailsTabGrid-grid").grid("getSelection");
                var tacheId = '';
                var psId = '';
                var woState = '';
                if ('' != meFlow.options && meFlow.options != null ){
                    tacheId = meFlow.options.tacheId + '';
                    psId = meFlow.options.psId + '';
                    woState = meFlow.options.psId + '';
                }
                this.requireView({
                    selector: "#flow-info-div",
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
                var sender = me.options.sender;
                var sender = 'builtin';
                var openViewParam = me.generateObjParam();
                var srvBelong = new Object();
                // srvBelong.srvOrdId = srvOrdId;
                srvBelong.cstOrdId = cstOrdId;
                sysResource=orderDetailsAction.qrySrvOrderBelongSys(srvBelong).responseJSON.data

                if(buttonState=='ccOrder'){ //抄送确认按钮
                    $("#cc_confirm").show();
                }
                //海南、重庆暂时屏蔽二干资源配置
                if('350002000000000042766427' == userInfo.areaId
                    || '350002000000000042766429' == userInfo.areaId){
                    //$("#tabs-resourceOrder").remove();
                    $("#tabsCirNum-pill").tabs("hideTab",2);
                }

                //导出定单电路详情
                var exportOrderInfo = function () {
                    var params = new Object();
                    params.cstOrdId = cstOrdId;
                    params.serviceId = serviceId;
                    params.srvOrdId = srvOrdId;
                    params.selectType = selectType;
                    params.srvOrdIds = me.options.srvOrdIds+'';
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
                    var resources = me.options.RESOURCES;
                    var tacheId = me.options.TACHE_ID;
                    var srvOrdIds = me.options.SRV_ORD_IDS;
                    if(tacheId == "500001153" && resources == "onedry"){
                        if(srvOrdId != '' && srvOrdId != undefined){
                            var srvOrdIdArr = new Array()
                            var splitSrvOrdIds = srvOrdId.split(",");
                            $.each(splitSrvOrdIds,function(index,obj){
                                srvOrdIdArr.push(obj);
                            });
                        }
                        params.srvOrdIds = srvOrdIdArr;
                    }
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
                            openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/sdwan/views/commonSubmitView';
                            openViewParam.btnFlag = "submit";
                            me.openDealView(openViewParam);
                        } else if('1000211,10101342'.indexOf(me.options.psId) != -1){ // 核查流程
                            openViewParam.url = checkSendWoOrderUrl();
                            openViewParam.btnFlag = "submit";
                            me.openDealView(openViewParam);
                        } else{
                            openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/operOrderView';
                            openViewParam.btnFlag = "submit";
                            openViewParam.width = "60%";
                            me.openDealView(openViewParam);
                        }
                    }
                };
                /**
                 *  核查流程提交
                 *  互联网专线产品或者二干下发核查单或者集客下发的并行核查，跳转到单端页面
                 *  以太网专线产品    数字电路，跳转到双端页面
                 */
                var checkSendWoOrderUrl = function(){
                    var url = '';
                    // 专业核查环节
                    var checkTacheIds='500001145,500001147,500001146,500001148,500001149,510101020';
                    // 如果是二干下发的核查单，或者 集客下发的并行核查，则展示单端信息
                    var singleFlag = 'second-schedule-lt' == sysResource.SYSTEM_RESOURCE || '10101342'==psId;
                    if(tacheId == "500001150"){ // 核查流程，核查汇总环节
                        if(me.options.serviceId=='10000011' || singleFlag){
                            url = 'module/UnicomLocalNet/resmaster/portal/checkFlow/views/checkTotalViewSingle';
                        } else if(me.options.serviceId=='10000001' || me.options.serviceId=='10000002'){
                            // 以太网专线产品    数字电路
                            url = 'module/UnicomLocalNet/resmaster/portal/checkFlow/views/checkTotalViewDouble';
                        } else{
                            url = 'module/UnicomLocalNet/resmaster/portal/checkFlow/views/checkTotalView';
                        }
                    } else if(tacheId == "500001144"){ // 核查调度环节
                        if(me.options.serviceId=='10000011' || singleFlag){
                            // 互联网专线产品
                            url = 'module/UnicomLocalNet/resmaster/portal/checkFlow/views/checkDispatchViewSingle';
                        } else if(me.options.serviceId=='10000001' || me.options.serviceId=='10000002'){
                            // 以太网专线产品    数字电路
                            url = 'module/UnicomLocalNet/resmaster/portal/checkFlow/views/checkDispatchViewDouble';
                        } else{
                            url = 'module/UnicomLocalNet/resmaster/portal/checkFlow/views/checkDispatchView';
                        }
                    } else if(checkTacheIds.indexOf(tacheId) != -1){ // 专业核查环节
                        if(me.options.serviceId=='10000011' || singleFlag){
                            // 互联网专线产品
                            url = 'module/UnicomLocalNet/resmaster/portal/checkFlow/views/specialtyCheckViewSingle';
                        } else if(me.options.serviceId=='10000001' || me.options.serviceId=='10000002'){
                            // 以太网专线产品    数字电路
                            url = 'module/UnicomLocalNet/resmaster/portal/checkFlow/views/specialtyCheckViewDouble';
                        } else{
                            url = 'module/UnicomLocalNet/resmaster/portal/checkFlow/views/investEstimationView';
                        }
                    } else if(tacheId == "500001151"){ // 投资估算环节
                        if(me.options.serviceId=='10000011' || singleFlag){
                            // 互联网专线产品
                            url = 'module/UnicomLocalNet/resmaster/portal/checkFlow/views/investEstimationViewSingle';
                        } else if(me.options.serviceId=='10000001' || me.options.serviceId=='10000002'){
                            // 以太网专线产品    数字电路
                            url = 'module/UnicomLocalNet/resmaster/portal/checkFlow/views/investEstimationViewDouble';
                        } else{
                            url = 'module/UnicomLocalNet/resmaster/portal/checkFlow/views/investEstimationView';
                        }
                    } else{
                        url = 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/operOrderView';
                    }
                    return url;
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
                //专业核查退单--核查单
                var checkBackOrder = function () {
                    openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/checkFlow/views/checkBackOrderView';
                    me.openDealView(openViewParam);
                };
                //核查汇总退单
                var checkTotalBackOrder = function () {
                    openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/checkFlow/views/checkTotalBackOrderView';
                    me.openDealView(openViewParam);
                };
                /**
                 * 直接反馈政企中心
                 */
                var directFeedback = function() {
                    var pop = fish.popupView({
                        url: 'module/UnicomLocalNet/resmaster/portal/checkFlow/views/pop/checkInfoFeedbackDirect',
                        width: 700,
                        height:"90%",
                        position: {at: "center"},
                        title: "工单处理-直接反馈政企中心",
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
                            btnFlag : "feedback"
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
                //自动化核查
                var autoCheck = function () {
                    openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/checkFlow/views/pop/autoCheckView';
                    me.openDealView(openViewParam);
                };
                //作废
                /*var disableWoOrder = function () {
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
                };*/
                // 填写终端信息
                var saveTerminal = function () {
                    openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/sdwan/views/terminalSynchView';
                    me.openDealViewWithoutCallback(openViewParam);
                };

                //add by wang.gang2 延期申请按钮
                var postponementApplyView = function () {
                    openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/postponementApplyView.js';
                    me.openDealViewWithoutCallback(openViewParam);
                };
                //add by wang.gang2 延期申请审核按钮
                var checkApply = function () {
                    openViewParam.woId = me.options.applyWoId;
                    var pop = fish.popupView({
                        url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/checkApplyView.js',
                        width: 700,
                        height: "100%",
                        title: "工单处理",
                        resizable:true,
                        viewOption: {
                            psId : openViewParam.psId,
                            tacheId : openViewParam.tacheId,
                            woState : openViewParam.woState,
                            orderId : openViewParam.orderId,
                            woId : openViewParam.woId,
                            serviceId: openViewParam.serviceId,
                            srvOrdId : openViewParam.srvOrdId,
                            cstOrdId : openViewParam.cstOrdId,
                            specialtyCode : openViewParam.specialtyCode,
                            regionId : openViewParam.regionId,
                            buttonState : openViewParam.buttonState,
                            dispObjTyeValue:openViewParam.dispObjTyeValue,
                            dispObjTye:openViewParam.dispObjTye,
                            activeType:openViewParam.activeType,
                            btnFlag : openViewParam.btnFlag,
                            resources : openViewParam.resources
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
                // 填写wan配置信息
                var saveWAN = function () {
                    //挂起的不能让提交
                    if (srvOrdStat == '4E') {
                        fish.error({title:'提示',message:'该电路已挂起，不能进行WAN配置!'});
                    } else{
                        openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/sdwan/views/configWANView';
                        me.openDealViewWithoutCallback(openViewParam);
                    }
                };
                // sdwan退单
                var sdwanBackWoOrder = function () {
                    //挂起的不能让提交
                    if (srvOrdStat == '4E') {
                        fish.error({title:'提示',message:'该电路已挂起，不能退单!'});
                    } else{
                        openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/sdwan/views/commonSubmitView';
                        openViewParam.btnFlag = "rollBackOrder";
                        me.openDealView(openViewParam);
                    }
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
                    //资源修改条件校验
                    if (buttonState == 'dispConfirm' && (tacheId == "500001153" || tacheId == "500001155" || tacheId == "500001157")){
                        var data = orderDetailsAction.querySrvOrdStatBySrvOrdId(srvOrdId + '').responseJSON.data;
                        if (data.flag == "false") {
                            fish.error({title: '提示', message: data.msg});
                            return;
                        }
                    }
                    var pop = fish.popupView({
                        url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/resConfigView',
                        width: '90%',
                        height: '100%',
                        position:{at: "center"},
                        title: "资源配置",
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
                // 下发工程建设系统核查单
                var sendEngineering = function () {
                    openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/checkFlow/views/sendEngineeringView';
                    openViewParam.btnFlag = "sendEngineering";
                    me.openDealView(openViewParam);
                };
                // add by wang.gang2 2020/09/19 下发工程建设系统 开通单
                var orderSend = function () {
                    if("jike" != me.options.RESOURCES) {  //电路调度 判断是否有在途核查单
                        fish.error({title: '提示', message: '该电路不是政企中台发起'});
                        return;
                    }
                    var pop = fish.popupView({
                        url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/orderSendConstruct',
                        width: 800,
                        height: 350,
                        position:{at: "center"},
                        title: "工单处理",
                        viewOption: {
                            psId : psId,
                            serviceId : serviceId,
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
                            btnFlag : "sendEngineering"
                        },
                        callback: function (popup, view) {
                            popup.result.then(function (e) {
                                //me.popup.close();
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

                //add by wang.gang2  并行核查流程
                var specislty = function () {
                    var pop = fish.popupView({
                        url: 'module/UnicomLocalNet/resmaster/portal/checkFlow/views/specialtyCheckView',
                        width: "80%",
                        height: "90%",
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

                // add   by wang.gang2省内dia 自动激活资源配置
                var diaResConfig = function(){
                    if (me.options.serviceId != "10000011" || me.options.RESOURCES != 'jike') {  //dia  jike
                        fish.error({title: '提示', message: '非政企中台DIA产品不能做此操作！'});
                        return;
                    }
                    var pop = fish.popupView({
                        url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/provinceAutoOperView',
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

                //重新激活
                var reactivation = function () {
                    debugger;
                    var actype = ['101','103','102','104','105','106'];
                    if (me.options.serviceId != "10000011") {  //互联网专线产品
                        fish.error({title: '提示', message: '非互联网专线(DIA) 产品不能做此操作！'});
                        return;
                    }
                    if (tacheId == "500001158" && actype.indexOf(me.options.activeType) != -1) {  //数据制作环节且满足actype里面的动作
                        var param = new Object();
                        param.woId = woId;
                        param.srvOrdId = srvOrdId;
                        var data = orderDetailsAction.resActivation(param).responseJSON.data;
                        if (data.RESP_CODE == "0") {
                            fish.info({title: '提示', message:"重新激活派单成功，请等待网管系统回单"});
                            return;
                        }
                        if (data.RESP_CODE != "0") {
                            fish.error({title: '提示', message: "重新激活派单失败" + data.RESP_DESC});
                            fish.error({title: '提示', message: "重新激活派单失败" + data.RESP_DESC});
                            return;
                        }
                    }
                    else {
                        fish.error({title: '提示', message: '工单不具备激活条件，请核实'});
                        return;
                    }
                };

                var btnParams = new Object();
                btnParams.tacheId = tacheId;//"500001144";
                btnParams.orderId = orderId;
                btnParams.orderIds = orderIds;
                btnParams.serviceId =serviceId;
                btnParams.specialtyCode =specialtyCode;
                btnParams.sysResource=sysResource.SYSTEM_RESOURCE;
                btnParams.resource=me.options.RESOURCES;
                btnParams.buttonState=buttonState;
                //btnParams.woState = woState;//"290000002";
                if('gomDispath' == selectType || 'gomQuery' == selectType || 'gomOrderQuery' == selectType){
                    btnParams.btnInfo = "110"

                } else if (buttonState == "deptStandny" || buttonState == "jobStandby" || buttonState == "staffStandby") {
                    btnParams.btnInfo = "200";
                }else if (buttonState == "dealOrder") {
                    btnParams.btnInfo = "100";//处理中
                } else if (buttonState == "applyOrder") {
                    btnParams.btnInfo = "402";//处理中
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
                var config = new Object();
                config.CODE_TYPE = 'ORDER_SEND';
                config.CODE_VALUE = userInfo.areaId;
                var constructConfig = orderDetailsAction.queryConstructConf(config).responseJSON.data;


                orderDetailsAction.getTacheButton(btnParams,function (res) {
                    if(res.success){
                        debugger;
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

                                //add by wang.gang2  校验工建对接配置的省份 注：如果是二干下发本地的单子，电路调度环节按钮也不展示
                                if ("second-schedule-lt" == sysResource.SYSTEM_RESOURCE || "jike" != me.options.RESOURCES || constructConfig == '' || constructConfig == null) {
                                    //跨域流程    //二干下发的停复机流程，电路调度环节不显示下发工建开通单按钮
                                    if(button.ID == '1000090' ){
                                        continue;
                                    }
                                }
                            }
                            if (sender != 'builtin' || "onedry" != me.options.RESOURCES) {
                                if (button.BUTTON_CLICK == 'postponementApplyView()') {
                                    continue;
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
            initCloudButton:function () {
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
                srvBelong.cstOrdId = cstOrdId;
                sysResource=orderDetailsAction.qrySrvOrderBelongSys(srvBelong).responseJSON.data


                //导出定单电路详情
                var exportOrderInfo = function () {
                    var params = new Object();
                    params.cstOrdId = cstOrdId;
                    params.serviceId = serviceId;
                    params.srvOrdId = srvOrdId;
                    params.selectType = selectType;
                    params.srvOrdIds = me.options.srvOrdIds+'';
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

                //抄送
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

                //转派
                var cloudTransfer = function (buttonName) {
                    openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/cloudNetworkFlow/views/orderTransferView';
                    openViewParam.width = '75%';
                    openViewParam.title = buttonName;
                    me.openDealView(openViewParam);
                };

                //签收
                var getWoOrder = function () {
                    var params = new Object();
                    if(woIds != '' && woIds != undefined){
                        var woOrderIds = new Array()
                        var splitWoIds = woIds.split(",");
                        $.each(splitWoIds,function(index,obj){
                            woOrderIds.push(obj);
                        });
                    }
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
                            me.initCloudButton();
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
                //云组网核查流程提交按钮处理
                var checkFlowSubmit = function(buttonName){
                    if (psId == '10101402'){
                        //云组网核查/跨域核查流程
                        if (tacheId == "1051002745"){
                            //核查调度环节
                            openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/cloudNetworkFlow/views/cloudNetWorkResCheckFlowView';
                        }else if (tacheId == "1051002749"){
                            //核查汇总
                            openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/cloudNetworkFlow/views/verificationSumView';
                        }else if (tacheId == "1051002750"){
                            //投资估算
                            openViewParam.url = "module/UnicomLocalNet/resmaster/portal/cloudNetworkFlow/views/investmentEstimationView";
                        }
                    }else if (psId == "10101405"){
                        //云组网IPRAN资源核查子流程
                        openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/cloudNetworkFlow/views/ipranCheckChildFlowView';
                    }else if (psId == "10101404"){
                        //云组网光纤专业核查子流程
                        openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/cloudNetworkFlow/views/fiberCheckChildFlowView';
                    }else if (psId == "10101403"){
                        //云组网终端盒核查子流程
                        openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/cloudNetworkFlow/views/terminalBoxCheckChildFlowView';
                    }
                    openViewParam.width = '65%';
                    openViewParam.title = buttonName;
                    me.openDealView(openViewParam);
                };

                //默认提交
                var cloudSendWoOrder = function (buttonName) {
                    openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/cloudNetworkFlow/views/woOrderSubmitCloudView';
                    openViewParam.width = '65%';
                    openViewParam.title = buttonName;
                    me.openDealView(openViewParam);
                };
                //电路调度--本地新开业务
                var circuitDispatchWoOrder = function (buttonName) {
                    if(activeType == '101') {
                        openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/cloudNetworkFlow/views/cloudCircuitDispatchView';
                    }else {
                        openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/cloudNetworkFlow/views/cloudCircuitDispatchCrossView';
                    }
                    openViewParam.width = '75%';
                    openViewParam.title = buttonName;
                    me.openDealView(openViewParam);
                };
                //电路调度--跨域新开业务
                var circuitDispatchCrossWoOrder = function (buttonName) {
                    openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/cloudNetworkFlow/views/cloudCircuitDispatchCrossView';
                    openViewParam.width = '75%';
                    openViewParam.title = buttonName;
                    me.openDealView(openViewParam);
                };
                //资源配置
                var resConfig = function(buttonName){
                    openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/resConfigView';
                    openViewParam.width = '75%';
                    openViewParam.title = buttonName;
                    openViewParam.btnFlag = 'resConfig';
                    me.openDealView(openViewParam);
                };
                //mcpe安装测试环节退单
                var mcpeInstallBackOrder = function (buttonName) {
                    var flagBtn = orderDetailsAction.qryIfConstruct(orderId).responseJSON.data;
                    if (flagBtn) {
                        openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/cloudNetworkFlow/views/mcpeInstallBackOrderView';
                        openViewParam.width = '75%';
                        openViewParam.title = buttonName;
                        me.openDealView(openViewParam);
                    } else {
                        fish.toast('warn', '未施工，不需要退单！');
                    }
                };
                var todoService = function (buttonName) {
                    fish.info('开发创作中。。。敬请期待！！！');
                };

                var btnParams = new Object();
                btnParams.tacheId = tacheId;
                btnParams.orderId = orderId;
                btnParams.orderIds = orderIds;
                btnParams.serviceId =serviceId;
                btnParams.specialtyCode =specialtyCode;
                btnParams.buttonState=buttonState;
                //btnParams.woState = woState;//"290000002";
                if('gomDispath' == selectType || 'gomQuery' == selectType || 'gomOrderQuery' == selectType || buttonState == "dispConfirm"){
                    btnParams.btnInfo = "110"
                } else if (buttonState == "deptStandny" || buttonState == "jobStandby" || buttonState == "staffStandby") {
                    btnParams.btnInfo = "200";
                }else if (buttonState == "dealOrder") {
                    btnParams.btnInfo = "100";//处理中
                }else{
                    return false;
                }
                orderDetailsAction.getCloudTacheButton(btnParams,function (res) {
                    if(res.success){
                        debugger;
                        for(var i = 0;i < res.resButtons.length;i++){
                            var button = res.resButtons[i];
                            var div=$('<button>');         //创建一个div
                            div.attr('id','officeDataApply-tacheBtn' + button.ID);    //给div设置id
                            div.attr('class','btn handle-order-btn');    //给div设置样式
                            div.attr('name', button.BUTTON_NAME);
                            div.attr('type','button');
                            div.attr('style','border:1px solid #fff;');
                            div.attr('resButtons',button.BUTTON_CLICK);
                            div.append(button.BUTTON_NAME);
                            $('#btnClassDiv').append(div);
                            div.bind('click',function(){
                                var funcName = $(this).attr("resButtons");
                                var buttonName =$(this).attr("name");
                                eval(funcName+"(buttonName)");
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
                /*try{ }catch (e) {
                    console.log(e);
                }*/
                var pop = fish.popupView({
                    url: param.url,
                    width: param.width == null ? 700 : param.width,
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
                        resources : param.resources,
                        userInfo : param.userInfo,
                        title: param.title == null ? '' : param.title
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
                    },

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
                var rowData = $("#orderDetailsTabGrid-grid").grid("getSelection");
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
                }).always(function () {
                    $("#addProduct-grid").unblockUI().data('blockui-content', false);
                });
            },
            /**
             * 省内自动开通进度查询方法
             */
            tabsProvinceAutoTache : function(){
                selectTab = 'engineeringTache';
                //初始化工建进度grid
                this.initProvinceAutoTacheGrid();
            },
            initProvinceAutoTacheGrid : function(){
                var enginTacheInfo = $.proxy(this.queryProvinceAutInfo(),this); //函数作用域改变
                $("#provinceAuto-grid").grid({
                    colModel: [
                        //默认展示字段
                        {name: 'TACHE_NAME', label: '环节名称', width: 120, sortable: false},
                        {name: 'RECEIVE_TIME', label: '收单时间', width: 120, sortable: false},
                        {name: 'REPLY_TIME', label: '回单时间', width: 120, sortable: false},
                        {name: 'LIMIT_TIME', label: '处理时限', width: 120, sortable: false},
                        {name: 'PROC_STAFF', label: '处理人', width: 120, sortable: false},
                        {name: 'PROC_STAFF_TEL', label: '处理人电话', width: 120, sortable: false},
                        {name: 'PROC_STAFF_EMAIL', label: '处理人Email', width: 120, sortable: false},
                        {name: 'PROC_DESC', label: '处理说明', width: 120, sortable: false}
                    ],
                    rownumbers:true,
                    autowidth: true,
                    rowNum: 10,
                    rowList: [10,15,20,50],
                    pager: true,
                    gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                    cached: true, //把用户自定义的列展示设置缓存在本地
                    pageData: enginTacheInfo
                });
                $("#provinceAuto-grid").grid("setGridHeight", 250);
            },
            queryProvinceAutInfo:function(page, rowNum, sortname, sortorder){
                var rowData = $("#orderDetailsTabGrid-grid").grid("getSelection");
                var param = {};
                param.srvOrdId = rowData.SRV_ORD_ID;
                rowNum = rowNum || this.$("#enginTache-grid").grid("getGridParam", "rowNum");
                fish.store.set('enginTache-grid-rowNum', rowNum); //记录用户选择的每页记录数
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
                $("#provinceAuto-grid").blockUI({message: '加载中'}).data('blockui-content', true);
                orderDetailsAction.provinceResOrder(param,function (data) {
                    $("#provinceAuto-grid").grid("reloadData", data.message);
                    $(window).trigger("resize");
                }).always(function () {
                    $("#provinceAuto-grid").unblockUI().data('blockui-content', false);
                });

            },
            /**
             * 工建进度单击事件方法
             */
            tabsEngineeringTache : function(){
                selectTab = 'engineeringTache';
                //初始化工建进度grid
                this.initEngineeringTacheGrid();
            },
            initEngineeringTacheGrid : function(){
                var enginTacheInfo = $.proxy(this.queryEnginTacheInfo(),this); //函数作用域改变
                $("#enginTache-grid").grid({
                    colModel: [
                        //默认展示字段
                        {name: 'TACHE_NAME', label: '环节名称', width: 120, sortable: false},
                        {name: 'RECEIVE_TIME', label: '收单时间', width: 120, sortable: false},
                        {name: 'REPLY_TIME', label: '回单时间', width: 120, sortable: false},
                        {name: 'LIMIT_TIME', label: '处理时限', width: 120, sortable: false},
                        {name: 'PROC_STAFF', label: '处理人', width: 120, sortable: false},
                        {name: 'PROC_STAFF_TEL', label: '处理人电话', width: 120, sortable: false},
                        {name: 'PROC_STAFF_EMAIL', label: '处理人Email', width: 120, sortable: false},
                        {name: 'PROC_DESC', label: '处理说明', width: 120, sortable: false}
                    ],
                    rownumbers:true,
                    autowidth: true,
                    rowNum: 10,
                    rowList: [10,15,20,50],
                    pager: true,
                    gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                    cached: true, //把用户自定义的列展示设置缓存在本地
                    pageData: enginTacheInfo
                });
                $("#enginTache-grid").grid("setGridHeight", 250);
            },
            queryEnginTacheInfo:function(page, rowNum, sortname, sortorder){
                var rowData = $("#orderDetailsTabGrid-grid").grid("getSelection");
                var param = {};
                param.srvOrdId = rowData.SRV_ORD_ID;
                rowNum = rowNum || this.$("#provinceAuto-grid").grid("getGridParam", "rowNum");
                fish.store.set('provinceAuto-grid-rowNum', rowNum); //记录用户选择的每页记录数
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
                $("#enginTache-grid").blockUI({message: '加载中'}).data('blockui-content', true);
                orderDetailsAction.progressQueryOrder(param,function (data) {
                    $("#enginTache-grid").grid("reloadData", data.message);
                    $(window).trigger("resize");
                }).always(function () {
                    $("#enginTache-grid").unblockUI().data('blockui-content', false);
                });
            },
            // add by wang.gang2   过户业务需要弹窗展示客户信息变更历史
            oldCustInfo:function(){
                var me = this;
                var options = {
                    url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/custInfoHisView',
                    height: '60%',
                    width: "1000",
                    modal: false,
                    draggable: true,
                    resizable:true,
                    autoResizable: true,
                    viewOption: {
                        flag : "org",
                        cstOrdId : me.options.cstOrdId
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

            //"激活结果"tab页点击事件
            initActivateTache: function () {
                selectTab = 'activate';
                this.initActivateTacheGrid(this);
            },

            initActivateTacheGrid : function(){
                var activateTacheInfo = $.proxy(this.queryActivateInfo(),this); //函数作用域改变
                $("#activate-grid").grid({
                    colModel: [
                        //默认展示字段
                        {name: 'WO_ID', label: 'woId', width: 20, sortable: false, hidden: true},
                        {name: 'SPEC_NAME', label: '专业', width: 120, sortable: false},
                        {name: 'FEED_SYSTEM', label: '反馈外系统', width: 120, sortable: false},
                        {name: 'ACTIVATE_CODE', label: '激活结果', width: 120, sortable: false},
                        {name: 'ACTIVATE_DESC', label: '激活描述说明', width: 120, sortable: false},
                        {name: 'ACTIVATE_DATE', label: '激活时间', width: 120, sortable: false}
                    ],
                    rownumbers:true,
                    autowidth: true,
                    rowNum: 10,
                    rowList: [10,15,20,50],
                    pager: true,
                    gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                    cached: true, //把用户自定义的列展示设置缓存在本地
                    pageData: activateTacheInfo
                });
                $("#activate-grid").grid("setGridHeight", 250);
            },

            equipRecycleClick:function(){
                var equipRecycleInfo = $.proxy(this.queryEquipRecycleInfo(),this); //函数作用域改变
                $("#equip-recycle-grid").grid({
                    colModel: [
                        //默认展示字段
                        {name: 'specialtyCode', label: '专业', width: 120, sortable: false},
                        {name: 'isRecycle', label: '设备是否回收', width: 120, sortable: false},
                        {name: 'recycleCount', label: '设备回收数量', width: 120, sortable: false},
                        {name: 'equipSequence', label: '设备序列号', width: 120, sortable: false},
                        {name: 'removeEquip', label: '拆机回收设备', width: 120, sortable: false},
                        {name: 'createDate', label: '创建时间', width: 120, sortable: false}

                    ],
                    rownumbers:true,
                    autowidth: true,
                    rowNum: 10,
                    rowList: [10,15,20,50],
                    pager: true,
                    gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                    cached: true, //把用户自定义的列展示设置缓存在本地
                    pageData: equipRecycleInfo
                });
                $("#equip-recycle-grid").grid("setGridHeight", 250);
            },
            postponementClick:function(){
                var postponementInfo = $.proxy(this.queryPostponementInfo(),this); //函数作用域改变
                $("#postponement-grid").grid({
                    colModel: [
                        //默认展示字段
                        {name: 'POSTPONEMENT', label: '延期时间', width: 120, sortable: false},
                        {name: 'REMARK', label: '延期说明', width: 120, sortable: false},
                        {name: 'OLD_DATE', label: '原全程要求完成时间', width: 120, sortable: false},
                        {name: 'APPLY_TYPE', label: '审核类型', width: 120, sortable: false},
                        {name: 'DEAL_USER', label: '审核人', width: 120, sortable: false},
                        {name: 'AUDIT_OPINION', label: '审核意见', width: 120, sortable: false},
                        {name: 'APPLY_STATE', label: '申请状态', width: 120, sortable: false},
                        {name: 'CREATE_DATE', label: '创建时间', width: 120, sortable: false},
                        {name: 'FILE_NAME', label: '附件', width: 120, sortable: false}

                    ],
                    rownumbers:true,
                    autowidth: true,
                    rowNum: 10,
                    rowList: [10,15,20,50],
                    pager: true,
                    gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                    cached: true, //把用户自定义的列展示设置缓存在本地
                    pageData: postponementInfo
                });
                $("#postponement-grid").grid("setGridHeight", 250);
            },
            queryActivateInfo:function(page, rowNum, sortname, sortorder){
                var rowData = $("#orderDetailsTabGrid-grid").grid("getSelection");
                if (rowData == ''
                    ||rowData == undefined
                    ||rowData.ORDER_ID == ''
                    ||rowData.ORDER_ID == undefined) {
                    fish.toast('warn', "请选择一条电路信息");
                    return;
                }
                var param = {};
                param.orderId = rowData.ORDER_ID;
                rowNum = rowNum || this.$("#activate-grid").grid("getGridParam", "rowNum");
                fish.store.set('enginTache-grid-rowNum', rowNum); //记录用户选择的每页记录数
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
                $("#activate-grid").blockUI({message: '加载中'}).data('blockui-content', true);
                orderDetailsAction.queryActivateResult(param,function (data) {
                    $("#activate-grid").grid("reloadData", data);
                    $(window).trigger("resize");
                }).always(function () {
                    $("#activate-grid").unblockUI().data('blockui-content', false);
                });
            },
            queryEquipRecycleInfo:function(page, rowNum, sortname, sortorder){
                var rowData = $("#orderDetailsTabGrid-grid").grid("getSelection");
                if (rowData == ''
                    ||rowData == undefined
                    ||rowData.ORDER_ID == ''
                    ||rowData.ORDER_ID == undefined) {
                    fish.toast('warn', "请选择一条电路信息");
                    return;
                }
                rowNum = rowNum || this.$("#equip-recycle-grid").grid("getGridParam", "rowNum");
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
                $("#equip-recycle-grid").blockUI({message: '加载中'}).data('blockui-content', true);
                orderDetailsAction.queryEquipInfo(rowData.SRV_ORD_ID + "",function (data) {
                    $("#equip-recycle-grid").grid("reloadData", data);
                    $(window).trigger("resize");
                }).always(function () {
                    $("#equip-recycle-grid").unblockUI().data('blockui-content', false);
                });
            },
            queryPostponementInfo:function(page, rowNum, sortname, sortorder){
                var rowData = $("#orderDetailsTabGrid-grid").grid("getSelection");
                if (rowData == ''
                    ||rowData == undefined
                    ||rowData.ORDER_ID == ''
                    ||rowData.ORDER_ID == undefined) {
                    fish.toast('warn', "请选择一条电路信息");
                    return;
                }
                rowNum = rowNum || this.$("#postponement-grid").grid("getGridParam", "rowNum");
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
                $("#postponement-grid").blockUI({message: '加载中'}).data('blockui-content', true);
                orderDetailsAction.queryPostPonement(rowData.SRV_ORD_ID + "",function (data) {
                    $("#postponement-grid").grid("reloadData", data);
                    $(window).trigger("resize");
                }).always(function () {
                    $("#postponement-grid").unblockUI().data('blockui-content', false);
                });
            }
        });
}); //ALL END