/**
 * 订单详情主JS
 */
define([
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/orderDetailsAction',
    'module/UnicomLocalNet/resmaster/portal/orderAbnormal/action/orderAppendAction',
    'text!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/templates/orderDetailsView.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/orderDetailsView.css'
], function(orderDetailsAction,orderAppendAction,orderDetails,i18n,css) {

        var selectTab = 'circuit';//tabl标识
        var changeOrderLabel = "4B";//异常单子标识
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
                'click #tabs-task-second': 'tabsSecondTask',//二干任务信息tab页单击事件
                'click #tabs-task-local': 'tabsLocalTask',//本地任务信息tab页单击事件
                'click #tabs-flow': 'tabsFlow',//流程图tab页单击事件
                'click #tabs-log': 'tabsLog',//日志信息tab页单击事件
                'click #tabs-changeorder': 'tabsChangeOrder',//异常单信息tab页单击事件
                'click #tabs-idea': 'tabsIdea',//阶段意见tab页单击事件
                // 'click #tabs-relevance': 'tabsRelevance',//关联主/子单tab页单击事件
                'click #tabs-warning': 'tabsWarning',//预警超时tab页单击事件
                'click #tabs-dispatchOrder': 'tabsDispatchOrder',//调单tab页单击事件
                'click #tabs-resourceOrder-w': 'tabsResourceOrderW',//一干资源信息tab页单击事件
                'click #tabs-resourceOrder': 'tabsResourceOrder',//二干资源信息tab页单击事件
                'click #tabs-resourceOrder-y': 'tabsResourceOrderY',//本地资源信息tab页单击事件
                'click #tabs-feedback': 'tabsFeedbackOrder',//反馈信息tab页单击事件
                'click #tabs-otherInfo': 'tabsOrderOtherInfo',//反馈信息tab页单击事件
                'click .downloadClFile': 'downloadClFile',//反馈信息tab页单击事件
                'click .downloadFile': 'downloadFile',//异常单附件下载
                'click #oldCustInfo': 'oldCustInfo',//原客户信息
                'click #tabs-engineeringTache': 'tabsEngineeringTache',//工建进度tab页单击事件
                'click #tabs-postponement': 'postponementClick', //延期申请

                //'click .close': 'closeClick',//页面关闭事件

                // 'click #tabs-change-a': 'tabsChangeA',//异常单加急点击时间
                // 'click #tabs-change-b': 'tabsChangeB',//异常单延时申请点击时间
                /*'click #tabs-change-c': 'tabsChangeC',//异常单追单点击时间*/
                // 'click #tabs-change-d': 'tabsChangeD',//异常单撤单点击时间
                // 'click #tabs-change-e': 'tabsChangeE',//异常单挂起点击时间
                // 'click .downLoadAttach': 'downLoadAttach',//
                'click #cc_confirm': 'cc_confirm',//抄送单确认
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
                //初始化定单详情、异常单信息tab页
                $("#tabs-pill,#tabsCirNum-pill,#changeOrder-tabs").tabs();
                userInfo = this.options.userInfo;
                var productTyep = this.options.serviceId;
                if (productTyep == "20181221006") {
                    $('#consumer-info').hide();
                    $('.subScribe').hide();
                }
                $("#tabs-order").click();
                //初始化按钮
                this.initButton();
                this.initFishInfo();
                this.controlTabShowAndHidden();
            },
            initFishInfo : function() {
                var me = this;
                var woOrderBackFlags = me.options.woOrderBackFlags;
                if (woOrderBackFlags != '' && '1'.indexOf(woOrderBackFlags) != -1){
                    if('510101060'==me.options.tacheId){
                    }else{
                        $('#ifBackOrder').show();
                    }
                }
            },
            //控制tab的显示隐藏
            controlTabShowAndHidden : function() {
                var resource = sysResource.RESOURCES;
                if('onedry' == resource){
                    $('#tabs-otherInfo').attr("style","display:block;");
                    $('#tabs-resourceOrder-w').attr("style","display:block;");
                }else{
                    $('#tabs-otherInfo').attr("style","display:none;");
                    $('#tabs-resourceOrder-w').attr("style","display:none;");
                };
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
                     /*   {name: 'CONSTRUCT_SCHEME', label: '建设方案', width: 150, sortable: false},
                        {name: 'ACCESS_ROOM', label: '局端接入机房', width: 150, sortable: false},*/
                        {name: 'INVESTMENT_AMOUNT', label: '投资金额', width: 150, sortable: false},
                        {name: 'CONSTRUCT_PERIOD', label: '建设工期', width: 150, sortable: false},
                        {name: 'RES_SATISFY', label: '资源是否满足', width: 150, sortable: false},
                        {name: 'RES_PROVIDE_STAND_NAME', label: '资源提供方式', width: 150, sortable: false},
                        {name: 'COLLECT_RES_NAME', label: '资源是否具备', width: 150, sortable: false},
                        {name: 'COLLECT_DAY', label: '扩容工期', width: 120, sortable: false},
                        {name: 'COLLECT_MONEY', label: '工程造价概算', width: 150, sortable: false},
                        {name: 'COLLECT_DESC', label: '工程概述', width: 150, sortable: false},



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
            }, //查看

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
                });
                $("#feedback-grid").unblockUI().data('blockui-content', false);
            },

            //初始化一干资源信息表格
            initResourceGridW: function() {
                var me = this;
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
                });
                $("#resource-grid-w").unblockUI().data('blockui-content', false);
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
                });
                $("#resource-grid").unblockUI().data('blockui-content', false);
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
                });
                $("#resource-grid-y").unblockUI().data('blockui-content', false);
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
                        document.getElementById('issuer').innerHTML=dispatchInfo.ISSUER;
                        document.getElementById('dispatch_text').innerText=dispatchInfo.DISPATCH_TEXT;
                        document.getElementById('dispatch_remark').innerHTML=dispatchInfo.REMARK;
                        // if(dispatchInfo.RES_ALLOCATE == undefined
                        //     ||dispatchInfo.RES_ALLOCATE == ""){
                        //     document.getElementById('draft_istran_resouce').innerHTML= "";
                        // }else{
                        //     document.getElementById('draft_istran_resouce').innerHTML=dispatchInfo.RES_ALLOCATE;
                        // }
                        // 
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
                                    // ;
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
                if(dispatchOrderId != undefined
                    && dispatchOrderId != null){
                    orderDetailsAction.queryDispatchOrderInfoById(dispatchOrderId,function (data) {
                        $("#dispatch-grid").grid("reloadData", data);
                    });
                }else{
                    orderDetailsAction.queryDispatchOrderInfo(param,function (data) {
                        $("#dispatch-grid").grid("reloadData", data);
                    });
                }
                if('localStandy' == selectType){
                    orderDetailsAction.queryDispatchOrderInfo(param,function (data) {
                        $("#dispatch-grid").grid("reloadData", data);
                        $(window).trigger("resize");
                    });
                }else{
                    orderDetailsAction.queryDispatchOrderInfoById(param,function (data) {
                        $("#dispatch-grid").grid("reloadData", data);
                        $(window).trigger("resize");
                    });
                }
                $("#dispatch-grid").unblockUI().data('blockui-content', false);
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
                        {name: 'WO_STATE', label: '异常单类型', width: 110, sortable: false},
                        // {name: 'CHG_VERSION', label: '版本', width: 100, sortable: false,hidden: true},
                        {name: 'TACHE_NAME', label: '环节名称', width: 160, sortable: false, formatter:me.formatUserNames},
                        {name: 'UNCONFIRM_USER_ID', label: '待确认用户id', width: 160, sortable: false, formatter:me.formatUserNames, hidden: true},
                        {name: 'CONFIRM_USER_ID', label: '已确认用户id', width: 160, sortable: false, formatter:me.formatUserNames, hidden: true},
                        {name: 'UNCONFIRM_USER_NAME', label: '待确认用户', width: 250, sortable: false, formatter:me.formatUserNames},
                        {name: 'CONFIRM_USER_NAME', label: '已确认用户', width: 250, sortable: false, formatter:me.formatUserNames},
                        {
                            name: 'FILE_INFO', label: '附件', width: 200, formatter: function (value) {
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
                ;
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
                            cstOrdId: rowData.CST_ORD_ID, //客户订单id
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
                });
                $("#idea-grid").unblockUI().data('blockui-content', false);
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
                });
                $("#warning-grid").unblockUI().data('blockui-content', false);
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
                });
                $("#relevance-grid").unblockUI().data('blockui-content', false);
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
                    var selrow = $("#"+selectId).grid("getSelection"); //获取子列表选中的行数据
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
                //circuitData.productType =  this.options.productType;
                //circuitData.circuitCode =  res.productId;
                //this.popup.close(circuitData);
            },
            //初始化任务---本地任务列表
            localTaskGrid : function (){
                var me = this;
                var localTaskInfo = $.proxy(this.querySecToLocalTaskInfo(),this); //函数作用域改变
                $("#task-grid-local").grid({
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
                });
                $("#task-grid-local").grid("setGridHeight", 327);
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
                var paramsMap = {}
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
                orderDetailsAction.queryTaskInfo(orderId,function (data) {
                    $("#task-grid-second").grid("reloadData", data);
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
                orderDetailsAction.querySecToLocalTaskInfo(orderId,function (data) {
                    $("#task-grid-local").grid("reloadData", data);
                    $("#task-grid-local").unblockUI().data('blockui-content', false);
                });

            },

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
                        {name: 'TRACK_ORG_NAME', label: '操作部门', width: 80, sortable: false},
                        {name: 'TRACK_STAFF_NAME', label: '操作人员', width: 80, sortable: false},
                        {name: 'TRACK_CONTENT', label: '操作内容', width: 100, sortable: false},
                        {name: 'TRACK_STAFF_PHONE', label: '操作人联系方式', width: 120, sortable: false},
                        {name: 'TRACK_DATE', label: '操作日期', width: 150, sortable: false},
                        {name: 'TRACK_MESSAGE', label: '操作信息', width: 300, sortable: false},
                        {name: 'OPER_TYPE_NAME', label: '操作类型', width: 80, sortable: false}
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
                });
                $("#log-grid").unblockUI().data('blockui-content', false);
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
                var srvOrdId = dataAttach.SRV_ORD_ID+"";
                var orderId = dataAttach.ORDER_ID+"";
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
                orderDetailsAction.queryAttachInfo(srvOrdId,orderId,function (data){
                    $("#attach-grid").grid("reloadData", data);
                });
                $("#attach-grid").unblockUI().data('blockui-content', false);
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
                });
                $("#changeOrder-grid").unblockUI().data('blockui-content', false);
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
                ;
                this.queryConsumerInfo();
            },
            //订单详情"电路信息"tab页点击事件
            tabsCircuit:function(){
                //每次点击"电路信息"tab页，都要清空<div id='order-circuit-info'></div>节点下的东西
                //不然会有重复数据填充
                // $("#order-circuit-info").empty();
                this.initcircuitInfoGrid();
            },
            //初始化电路信息Tab页
            initcircuitInfoGrid : function() {
                var me = this;
                me.controlTabShowAndHidden();
                //modify by wang.gang2  ZMP 2019286  集客来单展示关联的核查单
                if('101'==me.options.orderType && 'jike'== me.options.RESOURCES){
                    me.girdMdel =[
                        {name: 'SOURCENETWORK', label: '网络拓扑图', width: 100,formatter: function () { return  '点击查看'}},
                        {name: 'ROUTEPRESENTATION', label: '路由呈现',hidden: true, width: 100,formatter: function () { return  '点击查看'}},
                        {name: 'WOORDERBACKFLAG', label: '是否退单', width: 80 , formatter : me.ifBackOrder},
                        {name: 'CST_ORD_ID', label: '客户Id', width: 140 ,hidden: true},
                        {name: 'SRV_ORD_ID', label: '业务订单信息Id', width: 140 ,hidden: true},
                        {name: 'DISPATCH__ORDER_ID', label: '调单Id', width: 140 ,hidden: true},
                        {name: 'ORDER_ID', label: '流程订单Id', width: 140 ,hidden: true},
                        {name: 'SERVICE_ID', label: '产品类型Id', width: 140 ,hidden: true},
                        {name: 'ORDER_TYPE', label: '订单类型', width: 140 ,hidden: true},
                        {name: 'SUBSCRIBE_ID', label: '客户订单号', width: 120 , sortable: false },
                        {name: 'SERIAL_NUMBER', label: '业务号码', width: 120 , sortable: false },
                        {name: 'CIRCUITCODE', label: '电路编号', width: 120, sortable: false },
                        {name: 'A_IF_RES_HAVE', label: 'A端资源是否具备', width: 100, formatter: me.azIfResHaveEnum},
                        {name: 'Z_IF_RES_HAVE', label: 'Z端资源是否具备', width: 100, formatter: me.azIfResHaveEnum},
                        {name: 'TRADE_ID', label: '业务订单号', width: 120 },
                        {name: 'RELATETRADEID', label: '关联核查单业务订单号', width: 180 ,style:{color: '#6DCC4A'}},
                        {name: 'SERVICENAME', label: '产品类型', width: 100 },
                        {name: 'A_CITY', label: 'A端地市', width: 100 , sortable: false },
                        {name: 'Z_CITY', label: 'Z端地市', width: 100 , sortable: false },
                        {name: 'A_INSTALLED_ADD', label: 'A端装机地址', width: 100 , sortable: false },
                        {name: 'Z_INSTALLED_ADD', label: 'Z端装机地址', width: 100 , sortable: false },
                        {name: 'A_REQ_TIME', label: 'A端要求完成时间', width: 135 , sortable: false },
                        {name: 'Z_REQ_TIME', label: 'Z端要求完成时间', width: 135 , sortable: false }
                    ];
                }else{
                    me.girdMdel =[
                        {name: 'SOURCENETWORK', label: '网络拓扑图', width: 100,formatter: function () { return  '点击查看'}},
                        {name: 'ROUTEPRESENTATION', label: '路由呈现',hidden: true, width: 100,formatter: function () { return  '点击查看'}},
                        {name: 'WOORDERBACKFLAG', label: '是否退单', width: 80 , formatter : me.ifBackOrder},
                        {name: 'CST_ORD_ID', label: '客户Id', width: 140 ,hidden: true},
                        {name: 'SRV_ORD_ID', label: '业务订单信息Id', width: 140 ,hidden: true},
                        {name: 'DISPATCH__ORDER_ID', label: '调单Id', width: 140 ,hidden: true},
                        {name: 'ORDER_ID', label: '流程订单Id', width: 140 ,hidden: true},
                        {name: 'SERVICE_ID', label: '产品类型Id', width: 140 ,hidden: true},
                        {name: 'ORDER_TYPE', label: '订单类型', width: 140 ,hidden: true},
                        {name: 'SUBSCRIBE_ID', label: '客户订单号', width: 120 , sortable: false },
                        {name: 'SERIAL_NUMBER', label: '业务号码', width: 120 , sortable: false },
                        {name: 'CIRCUITCODE', label: '电路编号', width: 120, sortable: false },
                        {name: 'A_IF_RES_HAVE', label: 'A端资源是否具备', width: 100, formatter: me.azIfResHaveEnum},
                        {name: 'Z_IF_RES_HAVE', label: 'Z端资源是否具备', width: 100, formatter: me.azIfResHaveEnum},
                        {name: 'TRADE_ID', label: '业务订单号', width: 120 },
                        {name: 'SERVICENAME', label: '产品类型', width: 100 },
                        {name: 'A_CITY', label: 'A端地市', width: 100 , sortable: false },
                        {name: 'Z_CITY', label: 'Z端地市', width: 100 , sortable: false },
                        {name: 'A_INSTALLED_ADD', label: 'A端装机地址', width: 100 , sortable: false },
                        {name: 'Z_INSTALLED_ADD', label: 'Z端装机地址', width: 100 , sortable: false },
                        {name: 'A_REQ_TIME', label: 'A端要求完成时间', width: 135 , sortable: false },
                        {name: 'Z_REQ_TIME', label: 'Z端要求完成时间', width: 135 , sortable: false }
                    ]
                }

                $("#orderDetailsTabGrid-grid").grid({
                    colModel:me.girdMdel,
                    autowidth: true,
                    multiselect: false,
                    shrinkToFit: false, //表格列宽是否按比例缩放，默认true
                    pageData: me.qrycircuitInfoGrid(),
                    afterInsertRow: function (e, rowid, pageData) {
                        $("#orderDetailsTabGrid-grid").grid('setCell', rowid, 'SOURCENETWORK', '', {color: '#6DCC4A'});
                    },
                    onSelectRow: function (e, rowid, state, checked) {//选中行事件
                        // console.log("onSelectRow ......");
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
                            case 'otherSystemInfo':
                                me.initOrderOtherInfo();
                                break;
                            case 'checkOrder':
                                me.initFeedbackGrid();
                                break;
                        }
                    },
                    onCellSelect: function (e, rowid, iCol, cellcontent, colName, cellval) {//选中单元格的事件
                        var dataCell = $("#orderDetailsTabGrid-grid").grid("getRowData",rowid);
                        if(iCol == 0){
                            
                            crmRegionMap = orderDetailsAction.qryMsmSwitchByArea(userInfo.areaId).responseJSON.data;
                            if(crmRegionMap !=''
                                && crmRegionMap != undefined){
                                this.crmRegion = crmRegionMap.CRM_REGION;
                            }
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
                        if(iCol == 1 && '0'===userInfo.isShow){
                            var routeParam = {};
                            routeParam.codeValue = '/businessAnalysis/route';
                            routeParam.codeType = 'ROUTE_PRESENTATION';
                            var routeInfo = orderDetailsAction.queryRouteInfoUrl(routeParam).responseJSON.data;
                            var routeInfoUrl = routeInfo[0].CODE_CONTENT+'&code='+userInfo.regionId+'&userCode＝'+userInfo.name+'&srvOrdId='+dataCell.SRV_ORD_ID;
                            window.open(routeInfoUrl);
                        }

                        //modify by wang.gang2 数据可能有空格
                        if(iCol == 12 && '101' == me.options.orderType
                            && 'jike'== me.options.RESOURCES
                            && ''!= dataCell.CHECK_CST_ORD_ID
                            && ''!=dataCell.CHECK_ORDER_ID
                        ){
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
                    reloadGrid:function(e, rowid,pageData){
                        if('0'===userInfo.isShow){
                            $("#orderDetailsTabGrid-grid").grid('showCol', 'ROUTEPRESENTATION');
                        }
                    }
                });


                $("#orderDetailsTabGrid-grid").grid("setGridHeight", 160);
            },
            azIfResHaveEnum: function (value) {
                var enumTypeMap = {
                    '0':'否',
                    '1':'是',
                    '':''
                };
                return enumTypeMap[value];
            },
            qrycircuitInfoGrid : function(param){
                var me = this;
                var param = {};
                param.cstOrdId = me.options.cstOrdId;
                param.chgType = me.options.chgType;
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
                // modify ren.jiahang at 20190605 15:09 for 按照srvOrdIds显示详情,在xml中拼接srvOrdId。
                param.srvOrdIds=me.options.srvOrdIds;
                //modify by wang.gang2 // 异常单不需要工单id
                if(me.options.chgType == '' && me.options.chgType == null){
                    param.woIds=me.options.woIds;
                    param.woId=me.options.woId;
                }
                // param.dispTypeDetail = me.options.dispTypeDetail;
                $("#orderDetailsTabGrid-grid").blockUI({message: '查询中'}).data('blockui-content', true);
                orderDetailsAction.queryCircuitInfoGrid(param,function (res) {
                    if(res.flag == 1){
                        //
                        $("#orderDetailsTabGrid-grid").grid("reloadData", res.data);
                        var rowdataOne = $("#orderDetailsTabGrid-grid").grid("getRowData")[0];
                        $("#orderDetailsTabGrid-grid").grid("setSelection",rowdataOne,false);

                        $('#tabs-feedback').find("a").show();
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

            //订单详情"每条电路信息"tab页点击事件
            tabsCircuitSub:function(){
                var me = this;
                me.controlTabShowAndHidden();
                //每次点击"电路信息"tab页，都要清空<div id='order-circuit-info'></div>节点下的东西
                //不然会有重复数据填充
                selectTab = 'circuit';
                $("#otherInfo").empty();
                this.queryCircuitInfo();
            },
            //定单详情"附件信息"tab页点击事件
            tabsAttach:function(){
                var me = this;
                me.controlTabShowAndHidden();
                //初始化"附件"tab页的grid
                selectTab = 'attach';
                this.initAttachGrid();
            },
            //"二干任务"tab页点击事件
            tabsSecondTask:function(){
                var me = this;
                me.controlTabShowAndHidden();
                selectTab = 'secondTask';
                this.secondTaskGrid();
            },
            //"本地任务"tab页点击事件
            tabsLocalTask:function(){
                var me = this;
                me.controlTabShowAndHidden();
                selectTab = 'localTask';
                this.localTaskGrid();
            },
            //"流程"tab页点击事件
            tabsFlow: function () {
                var me = this;
                me.controlTabShowAndHidden();
                selectTab = 'flow';
                this.initFlowInfo(this);
            },
            //"日志"tab页点击事件
            tabsLog:function(){
                var me = this;
                me.controlTabShowAndHidden();
                //初始化"日志"tab页的grid
                selectTab = 'log';
                this.initLogGrid();
            },
            //定单详情"异常单信息"tab页点击事件
            tabsChangeOrder : function(){
                var me = this;
                me.controlTabShowAndHidden();
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
                var me = this;
                me.controlTabShowAndHidden();
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
                me.controlTabShowAndHidden();
                selectTab = 'warn';
                this.initWarningGrid();
            },
            //调单tab页点击事件
            tabsDispatchOrder:function(){
                var me = this;
                me.controlTabShowAndHidden();
                // selectTab = 'dispatch';
                $("#dispatch-info").hide();
                this.initDispatchGrid();
            },
            //一干资源信息tab页点击时间
            tabsResourceOrderW:function(){
                var me = this;
                me.controlTabShowAndHidden();
                selectTab = 'resourceW';
                this.initResourceGridW();
            },
            //二干资源信息tab页点击时间
            tabsResourceOrder:function(){
                var me = this;
                me.controlTabShowAndHidden();
                selectTab = 'resource';
                this.initResourceGrid();
            },
            //本地资源信息tab页点击时间
            tabsResourceOrderY:function(){
                var me = this;
                me.controlTabShowAndHidden();
                selectTab = 'resourceY';
                this.initResourceGridY();
            },
            //反馈信息tab页点击事件
            tabsFeedbackOrder :function(){
                var me = this;
                me.controlTabShowAndHidden();
                selectTab = 'checkOrder';
                this.initFeedbackGrid();
            },
            // 对端测试人员信息点击事件
            tabsOrderOtherInfo :function(){
                var me = this;
                me.controlTabShowAndHidden();
                selectTab = 'otherSystemInfo';
                this.initOrderOtherInfo();
            },
            //查询客户信息方法
            queryConsumerInfo:function () {
                var me = this;
                var cstOrdId = me.options.cstOrdId+'';
                var srvOrdIds = me.options.srvOrdIds+'';
                var srvordId = me.options.srvOrdId+'';
                var selectType = me.options.selectType;
                var productTyep = this.options.serviceId;
                $('#orderDeatail').empty();
                var orderDetailAll = ''; //定单信息
                // 
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
                                for(var i = 0; i < data.length; i++) {
                                    if(data.length > 1){
                                        orderDetailAll += '<div>定单信息'+ (i+1)+':</div>';
                                    }
                                    orderDetailAll += '<div class="row">';
                                    orderDetailAll += ' <div class="col-md-4 form-group">';
                                    orderDetailAll += '     <label class="control-label">申请单编号</label>';
                                    orderDetailAll += '     <div id="apply_ord_id" class="form-order-info">' + data[i].APPLY_ORD_ID;
                                    orderDetailAll += '     </div>';
                                    orderDetailAll += ' </div>';
                                    orderDetailAll += ' <div class="col-md-4 form-group">';
                                    orderDetailAll += '     <label class="control-label">定单主题</label>';
                                    orderDetailAll += '     <div id="apply_ord_name" class="form-order-info">' + data[i].APPLY_ORD_NAME;
                                    orderDetailAll += '     </div>';
                                    orderDetailAll += ' </div>';
                                    if (productTyep != '20181221006'){ //局内中继隐藏字段
                                        orderDetailAll += ' <div class="col-md-4 subScribe form-group">';
                                        orderDetailAll += '     <label class="control-label">客户订单号</label>';
                                        orderDetailAll += '     <div id="subscribe_id" class="form-order-info">' + data[i].SUBSCRIBE_ID;
                                        orderDetailAll += '     </div>';
                                        orderDetailAll += ' </div>';
                                        orderDetailAll += ' <div class="col-md-4 form-group">';
                                        orderDetailAll += '     <label class="control-label">合同编号</label>';
                                        orderDetailAll += '     <div id="contract_id" class="form-order-info">' + data[i].CONTRACT_ID;
                                        orderDetailAll += '     </div>';
                                        orderDetailAll += ' </div>';
                                        orderDetailAll += ' <div class="col-md-4 form-group">';
                                        orderDetailAll += '     <label class="control-label">合同名称</label>';
                                        orderDetailAll += '     <div id="contract_name" class="form-order-info">' + data[i].CONTRACT_NAME;
                                        orderDetailAll += '     </div>';
                                        orderDetailAll += ' </div>';
                                    }
                                    orderDetailAll += ' <div class="col-md-4 form-group">';
                                    orderDetailAll += '     <label class="control-label">产品类型</label>';
                                    orderDetailAll += '     <div id="product_type" class="form-order-info">' + data[i].PRODUCT_TYPE;
                                    orderDetailAll += '     </div>';
                                    orderDetailAll += ' </div>';
                                    orderDetailAll += ' <div class="col-md-4 form-group">';
                                    orderDetailAll += '     <label class="control-label">业务动作</label>';
                                    orderDetailAll += '     <div id="operate_type" class="form-order-info">' + data[i].OPERATE_TYPE;
                                    orderDetailAll += '     </div>';
                                    orderDetailAll += ' </div>';
                                    orderDetailAll += ' <div class="col-md-4 form-group">';
                                    orderDetailAll += '     <label class="control-label">业务动作明细</label>';
                                    orderDetailAll += '     <div id="operate_type" class="form-order-info">' + data[i].CHANGE_FLAG;
                                    orderDetailAll += '     </div>';
                                    orderDetailAll += ' </div>';
                                    orderDetailAll += ' <div class="col-md-4 form-group">';
                                    orderDetailAll += '     <label class="control-label">受理人</label>';
                                    orderDetailAll += '     <div id="handle_man_name" class="form-order-info">' + data[i].HANDLE_MAN_NAME;
                                    orderDetailAll += '     </div>';
                                    orderDetailAll += ' </div>';
                                    orderDetailAll += ' <div class="col-md-4 form-group">';
                                    orderDetailAll += '     <label class="control-label">受理人联系方式</label>';
                                    orderDetailAll += '     <div id="handle_man_tel" class="form-order-info">' + data[i].HANDLE_MAN_TEL;
                                    orderDetailAll += '     </div>';
                                    orderDetailAll += ' </div>';
                                    orderDetailAll += ' <div class="col-md-4 form-group">';
                                    orderDetailAll += '     <label class="control-label">受理部门</label>';
                                    orderDetailAll += '     <div id="handle_dep" class="form-order-info">' + data[i].HANDLE_DEP;
                                    orderDetailAll += '     </div>';
                                    orderDetailAll += ' </div>';
                                    orderDetailAll += ' <div class="col-md-4 form-group">';
                                    orderDetailAll += '     <label class="control-label">受理地市</label>';
                                    orderDetailAll += '     <div id="handle_city" class="form-order-info">' + data[i].HANDLE_CITY;
                                    orderDetailAll += '     </div>';
                                    orderDetailAll += ' </div>';
                                    orderDetailAll += ' <div class="col-md-4 form-group">';
                                    orderDetailAll += '     <label class="control-label">受理时间</label>';
                                    orderDetailAll += '     <div id="handle_time" class="form-order-info">' + data[i].HANDLE_TIME;
                                    orderDetailAll += '     </div>';
                                    orderDetailAll += ' </div>';
                                    orderDetailAll += ' <div class="col-md-4 form-group subScribe">';
                                    orderDetailAll += '     <label class="control-label">发起方项目经理</label>';
                                    orderDetailAll += '     <div id="init_am_name" class="form-order-info">' + data[i].INIT_AM_NAME;
                                    orderDetailAll += '     </div>';
                                    orderDetailAll += ' </div>';
                                    if('20181221006' != data[i].SERVICE_ID ){

                                        orderDetailAll += ' <div class="col-md-4 form-group subScribe">';
                                        orderDetailAll += '     <label class="control-label">省项目经理</label>';
                                        orderDetailAll += '     <div id="province_pm_name" class="form-order-info">' + data[i].PROVINCE_PM_NAME;
                                        orderDetailAll += '     </div>';
                                        orderDetailAll += ' </div>';
                                        orderDetailAll += ' <div class="col-md-4 form-group subScribe">';
                                        orderDetailAll += '     <label class="control-label">客户经理</label>';
                                        orderDetailAll += '     <div id="cust_manager" class="form-order-info">' + data[i].CUST_MANAGER;
                                        orderDetailAll += '     </div>';
                                        orderDetailAll += ' </div>';
                                        orderDetailAll += ' <div class="col-md-4 form-group subScribe">';
                                        orderDetailAll += '     <label class="control-label">客户经理联系电话</label>';
                                        orderDetailAll += '     <div id="cust_manager_tel" class="form-order-info">' + data[i].CUST_MANAGER_TEL;
                                        orderDetailAll += '     </div>';
                                        orderDetailAll += ' </div>';
                                    }
                                    orderDetailAll += ' <div class="col-md-4 form-group subScribe">';
                                    orderDetailAll += '     <label class="control-label">发展人渠道名称</label>';
                                    orderDetailAll += '     <div id="rele_b_inspect_order" class="form-order-info">' + data[i].DEVELOPER_DEPART_NAME;
                                    orderDetailAll += '     </div>';
                                    orderDetailAll += ' </div>';
                                    orderDetailAll += ' <div class="col-md-4 form-group">';
                                    orderDetailAll += '     <label class="control-label">备注</label>';
                                    orderDetailAll += '     <div id="remark" class="form-order-info">' + data[i].REMARK;
                                    orderDetailAll += '     </div>';
                                    orderDetailAll += ' </div>';
                                    orderDetailAll += ' <div class="col-md-12 form-group">';
                                    orderDetailAll += '     <label class="control-label">附件</label>';
                                    orderDetailAll += '     <div id="applyAttachInfo" class="form-order-info">';
                                    orderDetailAll += '     </div>';
                                    orderDetailAll += ' </div>';
                                    orderDetailAll += '</div>';

                                    if(data.length > 1 && i <data.length - 1){
                                        orderDetailAll += '<hr style="border-top:1px solid #d5d5d5; margin-top:0px;">';
                                    }
                                }
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
                        // ;
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
                            $("#otherInfo").append(_circuita);
                        }
                    }else{
                        fish.toast('error', data.message);
                    }
                });
            },
            // 初始化流程信息
            initFlowInfo: function (meFlow) {
                var rowData = $("#orderDetailsTabGrid-grid").grid("getSelection");
                this.requireView({
                    selector: "#flow-info-div",
                    url: "module/UnicomLocalNet/resmaster/portal/flowChart/views/flowChartView",
                    viewOption: {
                        orderId: rowData.ORDER_ID + '',
                        srvOrdId: rowData.SRV_ORD_ID + '',
                        psId: meFlow.options.psId + '',
                        woState: meFlow.options.woState,
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
                // var sender = 'builtin';
                var openViewParam = me.generateObjParam();
                var srvBelong = new Object();
                // srvBelong.srvOrdId = srvOrdId;
                srvBelong.cstOrdId = cstOrdId;
                sysResource=orderDetailsAction.qrySrvOrderBelongSys(srvBelong).responseJSON.data

                if(buttonState=='ccOrder'){ //抄送确认按钮
                    $("#cc_confirm").show();
                }
                //导出定单电路详情
                var exportOrderInfo = function () {
                    var params = new Object();
                    params.selectType = me.options.selectType;
                    params.srvOrdId = me.options.srvOrdId;
                    params.srvOrdIds = me.options.srvOrdIds+'';
                    params.orderIds = me.options.orderIds;
                    params.orderId = me.options.orderId;
                    params.serviceId = me.options.serviceId;
                    params.cstOrdId = me.options.cstOrdId;
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
                    var dispatchOrderId; //调单查询传值
                    orderDetailsAction.exportOrderInfo(URl+'/localScheduleLT/orderInfoExportController/exportOrderInfo.spr',params);
                };
                //签收
                var getWoOrder = function () {
                    var woOrderIds = new Array()
                    if(woIds != '' && woIds != undefined){
                        var splitWoIds = woIds.split(",");
                        $.each(splitWoIds,function(index,obj){
                            woOrderIds.push(obj);
                        });
                    }
                    var params = new Object();
                    var resources = me.options.RESOURCES;
                    var tacheId = me.options.tacheId;
                    var srvOrdIds = me.options.SRV_ORD_IDS;
                    if(tacheId == "510101040" && resources == "onedry"){
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
                    var woOrderIds = new Array();
                    if("110,111,112".indexOf(me.options.chgType) != -1){ //挂起解挂撤单
                        if(woIds != '' && woIds != undefined){
                            var splitWoIds = woIds.split(",");
                            $.each(splitWoIds,function(index,obj){
                                woOrderIds.push(obj);
                            });
                        }
                    }else {
                        woOrderIds.push(woId);
                    }
                    var params = new Object();
                    params.woOrderIds = woOrderIds;
                    params.actionType = "free";
                    orderDetailsAction.getFreeWoOrder(params, function (res) {
                        if (res.success) {
                            fish.toast('warn', res.message);
                            me.popup.close();
                        } else {
                            fish.toast('warn', res.message);
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
                        if (tacheId == "510101080") {  //电路调度 判断是否有在途核查单
                            var data = orderDetailsAction.queryCheckOrderStatBySrvOrdId(srvOrdId + '').responseJSON.data;
                            if (data.flag == "false") {
                                fish.error({title: '提示', message: data.msg});
                                return;
                            }
                        }
                        if ((activeType == '102'||activeType == '104'||activeType == '105') && tacheId == '510101048') { //页面提示，拆机--二干资源分配环节判断资源是否提交过
                            fish.confirm('是否在资源配置页面提交过？').result.then(function() { //目前这样做
                                openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/orderTacheDealView/views/orderSubmitView';
                                openViewParam.btnFlag = "submit";
                                me.openDealView(openViewParam);
                            })
                        }else{
                            var url = '';
                            if (('510101080,510101040').indexOf(tacheId) != -1 ) { //二干调度环节
                                url = 'module/UnicomLocalNet/resmaster/portal/orderTacheDealView/views/orderDispatchView';
                            } else if (('510101051,510101045').indexOf(tacheId) != -1 ) { //全程调测环节 省际全程调测
                                url = 'module/UnicomLocalNet/resmaster/portal/orderTacheDealView/views/orderTestView';
                            } else if (('510101084,510101087').indexOf(tacheId) != -1 ) { //二干电路--完工汇总环节  申请单审核环节
                                url = 'module/UnicomLocalNet/resmaster/portal/orderTacheDealView/views/orderCheckView';
                            } else if('10101042,10101322'.indexOf(psId) != -1){ // 核查流程
                                url = checkSendWoOrderUrl();
                            } else {
                                url = 'module/UnicomLocalNet/resmaster/portal/orderTacheDealView/views/orderSubmitView';
                            }
                            openViewParam.url = url;
                            openViewParam.btnFlag = "submit";
                            me.openDealView(openViewParam);
                        }
                    }
                };
                // 核查流程提交
                var checkSendWoOrderUrl = function(){
                    var url = '';
                    // 专业核查环节
                    var checkTacheIds='510101061,510101062,510101063,510101064,510101065';
                    if(tacheId == "510101060"){ // 核查流程，核查汇总环节
                        if(me.options.serviceId=='10000001' || me.options.serviceId=='10000002'){
                            // 以太网专线产品    数字电路
                            url = 'module/UnicomLocalNet/resmaster/portal/checkFlow/views/checkTotalViewDouble';
                        } else{
                            url = 'module/UnicomLocalNet/resmaster/portal/checkOrderTacheDealView/views/orderCollectView';
                        }
                    } else if(tacheId == "510101052"){ // 核查调度环节
                        if(me.options.serviceId=='10000001' || me.options.serviceId=='10000002'){
                            // 以太网专线产品    数字电路
                            url = 'module/UnicomLocalNet/resmaster/portal/checkFlow/views/checkDispatchViewDouble';
                        } else{
                            url = 'module/UnicomLocalNet/resmaster/portal/checkOrderTacheDealView/views/orderCheckView';
                        }
                    } else if(checkTacheIds.indexOf(tacheId) != -1){ // 专业核查环节
                        if(me.options.serviceId=='10000001' || me.options.serviceId=='10000002'){
                            // 以太网专线产品    数字电路
                            url = 'module/UnicomLocalNet/resmaster/portal/checkFlow/views/specialtyCheckViewDouble';
                        } else{
                            url = 'module/UnicomLocalNet/resmaster/portal/checkOrderTacheDealView/views/orderSubmitView';
                        }
                    } else if(tacheId == "510101066"){ // 投资估算环节
                        if(me.options.serviceId=='10000001' || me.options.serviceId=='10000002'){
                            // 以太网专线产品    数字电路
                            url = 'module/UnicomLocalNet/resmaster/portal/checkFlow/views/investEstimationViewDouble';
                        } else{
                            url = 'module/UnicomLocalNet/resmaster/portal/checkOrderTacheDealView/views/orderSubmitView';
                        }
                    } else{
                        url = 'module/UnicomLocalNet/resmaster/portal/checkOrderTacheDealView/views/orderSubmitView';
                    }
                    return url;
                }
                //转派
                var transferWoOrder = function () {
                    openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/orderTacheDealView/views/orderTransferView';
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
                    openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/orderTacheDealView/views/orderChargeBackView';
                    openViewParam.btnFlag = "rollBackOrder";
                    me.openDealView(openViewParam);
                };
                //专业核查退单--核查单
                var checkBackOrder = function () {
                    openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/checkOrderTacheDealView/views/checkBackOrderView';
                    me.openDealView(openViewParam);
                };
                //核查汇总退单
                var checkTotalBackOrder = function () {
                    openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/checkOrderTacheDealView/views/checkTotalBackOrderView';
                    me.openDealView(openViewParam);
                };
		        //add by wang.gang2 延期申请按钮
                var postponementApplyView = function () {
                    openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/postponementApplyView.js';
                    me.openDealView(openViewParam);
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
                //专业数据制作退单
                var dataMakeRollBackOrder = function () {
                    openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/orderTacheDealView/views/dataMakeRollBackView';
                    openViewParam.btnFlag = "dataMakeRollBackOrder";
                    me.openDealView(openViewParam);
                };
                //二干完工汇总退单
                var rollBackWoOrder = function () {
                    openViewParam.url = 'module/UnicomLocalNet/resmaster/portal/orderTacheDealView/views/orderRollBackView';
                    openViewParam.btnFlag = "summaryRollBackOrder";
                    me.openDealView(openViewParam);
                };

                // 资源配置
                var resConfig = function(){
                    if (tacheId == "510101080") {  //电路调度 判断是否有在途核查单
                        var data = orderDetailsAction.queryCheckOrderStatBySrvOrdId(srvOrdId + '').responseJSON.data;
                        if (data.flag == "false") {
                            fish.error({title: '提示', message: data.msg});
                            return;
                        }
                    }

                    //资源修改条件校验
                    if (buttonState == 'dispConfirm' && (tacheId == "510101080" || tacheId == "510101040" || tacheId == "510101048")){
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

                //作废
                var disableWoOrder = function () {
                    var pop = fish.popupView({
                        url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/disableView',
                        width: 700,
                        position: {at: "center"},
                        title: "工单作废",
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

                //起草调单
                var goDraftScheduling = function () {
                    var pop = fish.popupView({
                        url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/draftSchedulView',
                        width: '95%',
                        height: '100%',
                        position: {at: "center"},
                        title: "起草调单",
                        viewOption: {
                            parentOption : me.options,
                            psId : psId,
                            tacheId : tacheId,
                            woState : woState,
                            orderId : orderId,
                            woId : woId,
                            woIds : woIds,
                            srvOrdId : srvOrdId,
                            cstOrdId : cstOrdId,
                            specialtyCode : specialtyCode,
                            regionId : regionId,
                            buttonState : buttonState,
                            dispObjTyeValue:dispObjTyeValue,
                            dispObjTye:dispObjTye,
                            woIds: woIds,
                            btnFlag : "goDraftScheduling",
                            replenish: buttonState == 'dispConfirm'? true:false
                        },callback: function (popup, view) {
                            popup.result.then(function (e) {
                                me.popup.close();
                            }, function (e) {
                                console.log('关闭了', e);
                            });
                        }
                    });
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

                //异常单提交
                var compWo = function () {
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

                // add by wang.gang2  下发工程建设系统 开通单
                var orderSend = function () {
                    if("jike" != me.options.RESOURCES) {  //电路调度 判断是否有在途核查单
                        fish.error({title: '提示', message: '该电路不是政企中台发起'});
                        return;
                    }
                    var pop = fish.popupView({
                        url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/orderSendConstruct',
                        width: 700,
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

                var btnParams = new Object();
                btnParams.tacheId = tacheId;//"500001144";
                btnParams.orderId = orderId;
                btnParams.orderIds = orderIds;
                //btnParams.woState = woState;//"290000002";
                if('gomDispath' == selectType || 'gomQuery' == selectType || 'gomOrderQuery' == selectType){
                    btnParams.btnInfo = "110"

                } else if (buttonState == "applyOrder") {
                    btnParams.btnInfo = "402";//处理中
                } else if (buttonState == "deptStandny" || buttonState == "jobStandby" || buttonState == "staffStandby") {
                    btnParams.btnInfo = "200";
                }else if (buttonState == "dealOrder") {
                    btnParams.btnInfo = "100";//处理中
                }else if (buttonState == "dispConfirm" && woState == "290000110") {
                    if (tacheId == "500001153"){  //电路调度
                        btnParams.btnInfo = "101";
                    }else {
                        return false;
                    }
                //}else if (buttonState == "dispConfirm" && (woState == "290000004" || woState == '290000110')) {
                }else if (buttonState == "dispConfirm") {
                    btnParams.btnInfo = "110";//处理中
                    if(woState == "290000004"){
                        if (tacheId == "510101052"){  //核查调度--二干
                            btnParams.btnInfo = "104";
                        }
                    }
                    if((woState == '290000004' || woState == '290000110') && '510101040,510101080'.indexOf(tacheId) > -1){ //已完成的二干调度环节，补单 再次起草调度
                        //主流程当前环节
                        var tacheCodes = orderDetailsAction.qryTacheByOrderIds(orderIds).responseJSON.data;
                        var beforeDataArr = [/*'SECONDARY_SCHEDULE','SECONDARY_SCHEDULE_2',*/'SEC_SOURCE_DISPATCH_2',
                            'SEC_SOURCE_DISPATCH','TO_DATA_CREATE_AND_SCHEDULE', 'TO_DATA_CREATE_AND_SCHEDULE_2'];
                        if(tacheCodes != undefined) {
                            for(var i = 0; i < tacheCodes.length > 0; i++) {
                                var tacheCode = tacheCodes[i];
                                if (beforeDataArr.indexOf(tacheCode) > -1) {
                                    btnParams.draftSchedule = true;
                                }
                            }
                        }
                    }
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
                        for(var i = 0;i < res.resButtons.length;i++){
                            var button = res.resButtons[i];
                            var psIds = '1000209,1000210';
                            if (tacheId == '500001153') { //电路调度环节
                                if (psIds.indexOf(psId) != -1) {
                                    if(button.ID == '100010'){
                                        continue;
                                    }
                                }
                            } else if (tacheId == '510101044'){//一干跨域拆机电路--完工汇总不显示退单按钮
                                if(activeType == '102'){
                                    if(button.BUTTON_NAME == '退单'){
                                        continue;
                                    }
                                }
                            }
			                if (sender != 'builtin' || "onedry" != me.options.RESOURCES) {
                                if (button.BUTTON_CLICK == 'postponementApplyView()') {
                                    continue;
                                }
                            }

                            //add by wang.gang2  校验工建对接配置的省份
                            if ('10101060' != psId || "jike" != me.options.RESOURCES || constructConfig == '' || constructConfig == null) {
                                //跨域流程  集客下发跨域电路   显示下发工建开通单按钮
                                if(button.ID == '1000390' ){
                                    continue;
                                }
                            }

                            var div=$('<button>');         //创建一个div
                            div.attr('id','officeDataApply-tacheBtn' + button.ID);    //给div设置id
                            div.attr('class','btn handle-order-btn');    //给div设置样式
                            div.attr('type','button');
                            div.attr('style','border:1px solid #fff;');
                            div.attr('resButtons',button.BUTTON_CLICK);
                            // div.attr('onclick',button.BUTTON_CLICK);
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
                $("#enginTache-grid").blockUI({message: '加载中'}).data('blockui-content', true);
                orderDetailsAction.progressQueryOrder(param,function (data) {
                    $("#enginTache-grid").grid("reloadData", data.message);
                    $(window).trigger("resize");
                });
                $("#enginTache-grid").unblockUI().data('blockui-content', false);
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
                openViewParam.resources = me.options.RESOURCES
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
                        resources:param.resources,
                        btnFlag : param.btnFlag
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