define([
    'module/UnicomLocalNet/resmaster/portal/local/action/unicomLocalOrderAction',
    'text!module/UnicomLocalNet/resmaster/portal/local/templates/unicomLocalOrderTemp.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/local/i18n/unicomLocalOrderView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/local/css/unicomLocalOrderView.css'],
 function(unicomLocalOrderAction,unicomLocalOrderView,i18n,css) {

     var localConfigNum = 10;
     var localConfigPage = 1;

    return fish.View.extend({
        initTab:this.dialogArguments._src ? this.dialogArguments._src : "",
        template: fish.compile(unicomLocalOrderView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #startLocalApply' : 'startLocalApplyFun',
            "click #queryLocalApply" : 'queryLocalApplyFun',
            'click #resetLocalApply' : 'resetLocalApplyFun',
            // 'click #draftEdit' : 'draftEditBtn',
            'click #draftDelete' : 'draftDeleteBtn',
            'click #draftExport' : 'draftExportBtn',
            // 'click #wholeView' : 'wholeViewBtn',
            'click #wholeExport' : 'wholeExportBtn',               
            // 'click #completedView' : 'completedViewBtn',
            'click #completedExport' : 'completedExportBtn',             
            // 'click #submitView' : 'submitViewBtn',
            'click #submitExport' : 'submitExportBtn',             
            'click #local_apply_tabs_draft_link' : 'local_apply_tabs_draft_linkFun',
            'click #local_apply_tabs_whole_link' : 'local_apply_tabs_whole_linkFun',
            'click #local_apply_tabs_completed_link' : 'local_apply_tabs_completed_linkFun',
            'click #local_apply_tabs_submit_link' : 'local_apply_tabs_submit_linkFun'
        },
        initialize: function() {
            this.render();
            fish.store.set("isLocalUnicom","secondary");//  二干
            fish.store.set("queryTypeLocal","draftOrder");//默认查询类型为草稿
            fish.store.set("orderStateLocal","10C");//默认定单状态草稿

        },
        //渲染页面
        render: function() {
            this.$el.html(this.template(this.i18nData));
            return this;
        },

        //初始化fish组件
        afterRender: function() {
            //初始化tab
            $('#localApplyTabs').tabs();
            //初始化草稿单表格
            this.initorderDraftGrid();
            this.queryLocalApplyFun();
            $('#custName').clearinput();
            $('#applyOrdId').clearinput();
            $('#applyOrdName').clearinput();
            // $('#circuitNo').clearinput();
            //初始化全部申请单
            // this.initorderWholeGrid();
            //初始化已完成申请单
            // this.initorderCompletedGrid();
            //初始化已提交的申请单
            // this.initordersubmitGrid();
            if (this.initTab != "" && this.initTab != null) {
                $('#' + this.initTab).click();
            }else {
                //默认选中草稿
                $('#local_apply_tabs_draft_link').click();
            }
            //初始化下拉框
            this.initCombobox();
            $('#itemTypeStart').combobox('value','101');

            $('#itemTypeStart').on('combobox:change',function () {
                var productTypeObj = new Object();
                productTypeObj.codeType = 'product_code';
                var itemTypeStart =  $("#itemTypeStart").combobox('getSelectedItem');
                //产品类型
                unicomLocalOrderAction.queryProdTypeLocalData(productTypeObj,function(data) {
                    var dataArray = new Array;
                    // debugger;
                    if(data != undefined
                        && data != ''){
                        data.forEach(function(v,i){
                            if(itemTypeStart.value == '102'){
                                if(v.value == '10000001'
                                    ||v.value == '10000002'
                                    ||v.value == '10000011'
                                    ||v.value == '20181221002'
                                    ||v.value == '20181211001'
                                    ||v.value == '10000008'){
                                    dataArray.push(v);
                                }
                            }else{
                                dataArray.push(v);
                            }
                        });
                        // debugger;
                        $('#productTypeStart').combobox({
                            placeholder: '--请选择产品类型--',
                            dataTextField: 'name',
                            dataValueField: 'value',
                            dataSource: dataArray
                        });

                    }
                });
                $("#productTypeStart").combobox('value','');
                $("#actTypeStart").combobox('value','');

            });

            $('#productType').on('combobox:change', function(e) {
                var productTypeObj = new Object();
                productTypeObj.codeType = $('#productType').val();
                // debugger;
                unicomLocalOrderAction.queryProdTypeData(productTypeObj,function(data){
                    $('#actType').combobox({
                        dataSource: data,
                        placeholder: '--请选择动作类型--',
                        dataTextField: 'name',
                        dataValueField: 'value'
                    });
                });
            });
            $('#actType').on('combobox:open',function (e) {
                if($('#productType').val()==''
                    ||$('#productType').val()=='undefined'){
                    fish.toast("warn", "请先选择产品类型");
                    $('#actType').combobox({
                        dataSource: [],
                        placeholder: '--请选择动作类型--',
                        dataTextField: 'name',
                        dataValueField: 'value'
                    });
                }
            });

            $('#actType').on('combobox:change',function () {
                if($('#productType').val()==''
                    ||$('#productType').val()=='undefined'){
                    $("#actType").combobox('value','');
                }
            });

            //本地产品类型、动作类型事件 start
            $('#productTypeStart').on('combobox:change', function(e) {
                var productTypeObj = new Object();
                // debugger;
                if($('#productTypeStart').val()==''
                    ||$('#productTypeStart').val()== undefined) {
                    productTypeObj.codeType = 'operate_type';
                }else{
                    productTypeObj.codeType = $('#productTypeStart').val();
                }
                var itemTypeStart =  $("#itemTypeStart").combobox('getSelectedItem');
                unicomLocalOrderAction.queryProdTypeLocalData(productTypeObj,function(data) {
                    var dataArray = new Array;
                    // debugger;
                    if(itemTypeStart.value == '102'){
                        if(productTypeObj.codeType == '10000001'
                            ||productTypeObj.codeType == '10000002'
                            ||productTypeObj.codeType == '10000011'
                            ||productTypeObj.codeType == '20181221002'
                            ||productTypeObj.codeType == '20181211001'
                            ||productTypeObj.codeType == '10000008'){
                            data.forEach(function(v,i){
                                if(v.value == '101'
                                    ||v.value == '103'
                                    ||v.value == '106'){
                                    dataArray.push(v);
                                }
                            });
                            $('#actTypeStart').combobox({
                                dataSource: dataArray,
                                placeholder: '--请选择动作类型--',
                                dataTextField: 'name',
                                dataValueField: 'value'
                            });
                        }
                    }else{
                        $('#actTypeStart').combobox({
                            dataSource: data,
                            placeholder: '--请选择动作类型--',
                            dataTextField: 'name',
                            dataValueField: 'value'
                        });
                    }

                });
            });
            $('#actTypeStart').on('combobox:open',function (e) {
                if($('#productTypeStart').val()==''
                    ||$('#productTypeStart').val()=='undefined'){
                    fish.toast("warn", "请先本地选择产品类型");
                    $('#actTypeStart').combobox({
                        dataSource: [],
                        placeholder: '--请选择动作类型--',
                        dataTextField: 'name',
                        dataValueField: 'value'
                    });
                }
            });

            $('#actTypeStart').on('combobox:change',function () {
                if($('#productTypeStart').val()==''
                    ||$('#productTypeStart').val()=='undefined'){
                    $("#actTypeStart").combobox('value','');
                }
            });
            //本地产品类型、动作类型事件 end
            // $("#circuitNolabel").css('display','none');//隐藏
            // $("#circuitNodiv").css('display','none');//隐藏
            //初始化其它tab页数量
            this.queryLocalOtherCount();

            $('.rowtext .ui-pagination').change(function () {
                    var p1=$(this).children('option:selected').val();//这就是selected的值
                    localConfigNum = p1;
                    localConfigPage = 1;
                    this.queryLocalApplyFun();
                }
            );

        },

        initCombobox:function(){
            this.initProductTypeData();
            this.initOperationData();
            this.initItemTypeData();

        },
        //单据类型
        initItemTypeData: function() {
            $('#itemTypeStart').combobox({
                placeholder: '--请选择单据类型--',
                dataTextField: 'name',
                dataValueField: 'value',
                dataSource: [
                    {name: '开通单', value: '101'},
                    {name: '核查单', value: '102'}
                ]
            });
            $('#orderType').combobox({
                placeholder: '--请选择单据类型--',
                dataTextField: 'name',
                dataValueField: 'value',
                dataSource: [
                    {name: '开通单', value: '101'},
                    {name: '核查单', value: '102'}
                ]
            });

        },
        //产品类型
        initProductTypeData: function(){
            var productTypeObj = new Object();
            productTypeObj.codeType = 'product_code';
            //产品类型
            unicomLocalOrderAction.queryProdTypeData(productTypeObj,function(data) {
                $('#productType').combobox({
                    placeholder: '--请选择产品类型--',
                    dataTextField: 'name',
                    dataValueField: 'value',
                    dataSource: data
                });
            });
            //本地发起单子的产品类型
            unicomLocalOrderAction.queryProdTypeLocalData(productTypeObj,function(data) {
                $('#productTypeStart').combobox({
                    placeholder: '--请选择产品类型--',
                    dataTextField: 'name',
                    dataValueField: 'value',
                    dataSource: data
                });
            });


        },

        //动作类型
        initOperationData: function(){
            var operationTypeObj = new Object();
            operationTypeObj.codeType = 'operate_type';
            //动作类型1
            unicomLocalOrderAction.queryProdTypeData(operationTypeObj,function(data){
                $('#actType').combobox({
                    dataSource: data,
                    placeholder: '--请选择动作类型--',
                    dataTextField: 'name',
                    dataValueField: 'value'
                });
                $('#actTypeStart').combobox({
                    dataSource: data,
                    placeholder: '--请选择动作类型--',
                    dataTextField: 'name',
                    dataValueField: 'value'
                });

            });
        },
        initorderDraftGrid:function() {
            var me = this;
                var queryLocalDraftApplyList = $.proxy(this.queryLocalDraftApplyList,this); //函数作用域改变
                $("#localConfig-localdraftGrid").grid({
                    colModel: [
                        //默认展示字段
                        {name:'orderId',label:'流程定单ID',width:200,hidden: true },
                        {name:'srvOrdId',label:'业务定单ID',width:200,hidden: true },
                        {name:'srvordIds',label:'业务定单ID集合',width:200,hidden: true },
                        {name: 'cstOrdId',label:'客户Id',width:150,hidden: true },
                        {name:'serialNumber',label:'业务号码',width:200,align: 'left',hidden: true },
                        {name: 'custName',label:'客户名称',width:200, align: 'left' },
                        {name:'applyOrdId',label:'申请单编号',width:200,align: 'left'},
                        {name: 'applyOrdName',label:'申请单标题',width:180, align: 'left' },
                        {name: 'dianlNo',label:'电路编号',width:150, align: 'left',hidden: true },
                        {name: 'prodBustType',label:'产品类型',width:150,hidden: true, align: 'left' },
                        {name: 'prodBustTypeName',label:'产品类型',width:150, align: 'left' },
                        {name: 'actCode',label:'动作类型',width:150,hidden: true },
                        {name: 'actCodeName',label:'动作类型',width:150, align: 'left' },
                        {name: 'circodeCount',label:'电路数量',width:150, align: 'left',},
                        {name: 'itemType',label:'单据类型',width:150, align: 'left',formatter:me.formatItemType},
                        {name: 'actTypeName',label:'流程类型',width:150,hidden: true , align: 'left'},
                        {name: 'actTypeState',label:'流程状态',width:150,hidden: true , align: 'left',formatter:me.formatActTypeState},
                        {name: 'dispObjName',label:'创建人',width:150 },
                        {name: 'createDateStr',label:'申请时间',width:150, align: 'left'},
                        {name: 'omlParentOrderId',label:'OML_PARENT_ORDER_ID',width:150,hidden: true },
                        {name: 'omlOrderId',label:'OML_ORDER_ID',width:150,hidden: true }
                    ],
                    datatype: "json",
                    autowidth: true,
                    curPageSort: true,
                    // height: 300,
                    // maxHeight: 300,
                    recordtext:"{0}-{1} 共{2}条",
                    pgtext: " 第{0}页/共{1}页",
                    rowtext: "每页{0}条",
                    rowNum: 10,
                    rowList: [5,10,20,50,100,200,500],
                    pager: true,
                    gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                    multiselect: false,
                    shrinkToFit: false,
                    autoResizable: true,
                    showColumnsFeature: false, //允许用户自定义列展示设置
                    cached: true, //把用户自定义的列展示设置缓存在本地
                    pageData: queryLocalDraftApplyList,
                    onDblClickRow: function (e, rowid, iRow, iCol) {//双击行事件
                        me.orderApplyFormView(e, rowid, iRow, iCol);
                    },
                    gridComplete: function () {
                        $('.gotext').html("<span>跳转至<input class=\"ui-pagination-input\"></span>");
                    }
                });
                this.resize();
        },
        orderApplyFormView: function(e, rowid, iRow, iCol){
            var me = this;
            var queryTypeLocal = fish.store.get("queryTypeLocal");
            switch(queryTypeLocal){
                case 'draftOrder':
                    me.draftEditBtn(e, rowid, iRow, iCol);
                    break;
                case 'allOrder':
                    me.wholeViewBtn(e, rowid, iRow, iCol);
                    break;
                case 'completeOrder':
                    me.completedViewBtn(e, rowid, iRow, iCol);
                    break;
                case 'submitedOrder':
                    me.submitViewBtn(e, rowid, iRow, iCol);
                    break;
            }
        },
        //查询草稿单、申请单数量
        queryLocalOtherCount: function(){
            var queryTypeLocal=fish.store.get("queryTypeLocal");//查询类型
            var isLocalUnicom=fish.store.get("isLocalUnicom");//本地订单

            var applyOrdId = $("#applyOrdId").val();//申请单号
            var applyOrdName = $("#applyOrdName").val();//申请单标题
            // var circuitNo = $("#circuitNo").val();//电路代号
            var custName = $("#custName").val();//客户名称
            var productType = $("#productType").val();//产品类型
            var actType = $("#actType").val();//动作类型

            var queryObject = new Object();
            // queryObject.orderState = orderStateLocal;
            queryObject.applyOrdId = applyOrdId;
            queryObject.applyOrdName = applyOrdName;
            // queryObject.circuitNo = circuitNo;
            queryObject.custName = custName;
            queryObject.productType = productType;
            queryObject.actType = actType;
            queryObject.isLocalUnicom = isLocalUnicom;

            switch(queryTypeLocal){
                case 'draftOrder':
                    queryObject.draftOrderState = '';
                    queryObject.allOrderState = '';
                    queryObject.completeOrderState = '10F';
                    queryObject.submitedOrderState = '10N';
                    break;
                case 'allOrder':
                    queryObject.draftOrderState = '10C';
                    queryObject.allOrderState = 'true';
                    queryObject.completeOrderState = '10F';
                    queryObject.submitedOrderState = '10N';
                    break;
                case 'completeOrder':
                    queryObject.draftOrderState = '10C';
                    queryObject.allOrderState = '';
                    queryObject.completeOrderState = '';
                    queryObject.submitedOrderState = '10N';
                    break;
                case 'submitedOrder':
                    queryObject.draftOrderState = '10C';
                    queryObject.allOrderState = '';
                    queryObject.completeOrderState = '10F';
                    queryObject.submitedOrderState = '';
                    break;
            }
            unicomLocalOrderAction.queryLocalApplyOrderCount(queryObject,function(data){
                if (data.message == "success") {
                    switch(queryTypeLocal){
                        case 'draftOrder':
                            // $('#draftspan').text(data.draftOrderCount);
                            $('#wholespan').text(data.allOrderCount);
                            $('#completespan').text(data.completeOrderCount);
                            $('#submitspan').text(data.submitedOrderCount);
                            break;
                        case 'allOrder':
                            $('#draftspan').text(data.draftOrderCount);
                            // $('#wholespan').text(data.allOrderCount);
                            $('#completespan').text(data.completeOrderCount);
                            $('#submitspan').text(data.submitedOrderCount);
                            break;
                        case 'completeOrder':
                            $('#draftspan').text(data.draftOrderCount);
                            $('#wholespan').text(data.allOrderCount);
                            // $('#completespan').text(data.completeOrderCount);
                            $('#submitspan').text(data.submitedOrderCount);
                            break;
                        case 'submitedOrder':
                            $('#draftspan').text(data.draftOrderCount);
                            $('#wholespan').text(data.allOrderCount);
                            $('#completespan').text(data.completeOrderCount);
                            // $('#submitspan').text(data.submitedOrderCount);
                            break;
                    }
                }else{
                    fish.toast("warn", "获取数据失败");
                }
            });
        },

        //草稿单、申请单、已完成、已提交申请单查询
        queryLocalDraftApplyList: function(page, rowNum, sortname, sortorder) {
            var queryTypeLocal=fish.store.get("queryTypeLocal");//查询类型
            var isLocalUnicom=fish.store.get("isLocalUnicom");//本地订单
            var orderStateLocal=fish.store.get("orderStateLocal");//订单状态

            var applyOrdId = $("#applyOrdId").val();//申请单编号
            var applyOrdName = $("#applyOrdName").val();//申请单标题
            // var circuitNo = $("#circuitNo").val();//电路代号
            var custName = $("#custName").val();//客户名称
            var productType = $("#productType").val();//产品类型
            var actType = $("#actType").val();//动作类型
            var orderType = $("#orderType").val();//动作类型

            if((productType==''
                ||productType==undefined)
                &&(actType!=''&&actType!=undefined)){
                fish.toast("warn", "请先选择产品类型");
                return;
            }
            // debugger;
            rowNum = (rowNum!=''&&rowNum!=undefined)?rowNum:localConfigNum;
            page = (page!=''&&page!=undefined)?page:localConfigPage;
            // rowNum = rowNum || localConfigNum;
            // page = page || localConfigPage;
            var queryObject = new Object();
            queryObject.queryType = queryTypeLocal;
            queryObject.isLocalUnicom = isLocalUnicom;
            queryObject.orderState = orderStateLocal;
            queryObject.applyOrdId = applyOrdId;
            queryObject.applyOrdName = applyOrdName;
            // queryObject.circuitNo = circuitNo;
            queryObject.custName = custName;
            queryObject.productType = productType;
            queryObject.actType = actType;
            queryObject.orderType = orderType;
            queryObject.pageIndex = page+'';
            queryObject.pageSize = rowNum+'';
            // queryObject.sortname = sortname;
            // queryObject.sortorder = sortorder;
            // console.log($('.rowtext .ui-pagination').val());

            //调用后台方法
            $("#localConfig-localdraftGrid").blockUI({message: '加载中'}).data('blockui-content', true);
            unicomLocalOrderAction.queryLocalApplyOrderData(queryObject,function(data){
                if (data.message == "success") {
                    var gridData = {
                        "rows": data.unicomVoList,
                        "page": page,
                        "records": data.pageInfo.rowCount,
                        "rowNum": rowNum,
                        "total":data.pageInfo.pageCount
                    };
                    //显示订单数量
                    switch(queryTypeLocal){
                        case 'draftOrder':
                            $('#draftspan').text(data.pageInfo.rowCount);
                            break;
                        case 'allOrder':
                            $('#wholespan').text(data.pageInfo.rowCount);
                            break;
                        case 'completeOrder':
                            $('#completespan').text(data.pageInfo.rowCount);
                            break;
                        case 'submitedOrder':
                            $('#submitspan').text(data.pageInfo.rowCount);
                            break;
                    }
                    // debugger;
                    $("#localConfig-localdraftGrid").grid("reloadData", gridData);
                }else{
                    fish.toast("warn", "获取数据失败");
                }
            });
            $("#localConfig-localdraftGrid").unblockUI().data('blockui-content', false);
        },
        //草稿单发起、编辑
        openLocalDraftView: function(urlview,productTypeStart,actTypeStart,queryType,flag,srvOrdId,cstOrdIdCur){
            var me = this;
            fish.popupView({
                url: urlview,
                width: "95%",
                height:"100%",
                canClose: true,
                autoResizable: true,
                draggable:true,
                resizable:true,
                viewOption:{
                    productType:productTypeStart,//产品类型
                    actType:actTypeStart,//动作类型
                    queryType:queryType,//查询类型:发起
                    flag:flag,//操作类型:发起
                    srvOrdId:srvOrdId,//业务订单Id
                    custOrdId:cstOrdIdCur
                },
                callback:function(popup,view){
                    popup.result.then(function (ret) {
                        // debugger;
                        // console.log('关闭了',ret);
                        me.queryLocalApplyFun();
                        if(ret && ret.isSuccess){
                            alert(ret.isSuccess);
                        }
                    },function (ret) {
                        // debugger;
                        // me.queryLocalApplyFun();
                    });
                }
            });
        },

        //草稿单发起、编辑(单据类型)
        openLocalDraftItemView: function(urlview,productTypeStart,actTypeStart,queryType,flag,srvOrdId,cstOrdIdCur,itemTypeStart){
            var me = this;
            // debugger;
            fish.popupView({
                url: urlview,
                width: "95%",
                height:"100%",
                canClose: true,
                autoResizable: true,
                draggable:true,
                resizable:true,
                viewOption:{
                    productType:productTypeStart,//产品类型
                    actType:actTypeStart,//动作类型
                    queryType:queryType,//查询类型:发起
                    flag:flag,//操作类型:发起
                    srvOrdId:srvOrdId,//业务订单Id
                    itemTypeStart:itemTypeStart, //单据类型
                    custOrdId:cstOrdIdCur
                },
                callback:function(popup,view){
                    popup.result.then(function (ret) {
                        // debugger;
                        // console.log('关闭了',ret);
                        me.queryLocalApplyFun();
                        if(ret && ret.isSuccess){
                            alert(ret.isSuccess);
                        }
                    },function (ret) {
                        // debugger;
                        // me.queryLocalApplyFun();
                    });
                }
            });
        },

        //全部申请单、已完成、已提交的查看
        openLocalOtherView: function(urlview,productTypeStart,actTypeStart,queryType,flag,srvOrdId,cstOrdIdCur){
            var me = this;
            fish.popupView({
                url: urlview,
                width: "95%",
                height:"100%",
                canClose: true,
                autoResizable: true,
                draggable:true,
                resizable:true,
                viewOption:{
                    productType:productTypeStart,//产品类型
                    actType:actTypeStart,//动作类型
                    queryType:queryType,//查询类型:发起
                    flag:flag,//操作类型:发起
                    srvOrdId:srvOrdId,//业务订单Id
                    custOrdId:cstOrdIdCur
                },
                callback:function(popup,view){
                    // popup.result.then(function (ret) {
                    //     if(ret && ret.isSuccess){
                    //         alert(ret.isSuccess);
                    //     }
                    // },function (ret) {
                    //     // console.log('关闭了',ret);
                    //     me.queryLocalApplyFun();
                    // });
                }
            });
        },

        //发起
        startLocalApplyFun:function(){
            var itemTypeStart = $("#itemTypeStart").combobox('getSelectedItem');
            var productTypeStart =  $("#productTypeStart").combobox('getSelectedItem');
            var actTypeStart =  $("#actTypeStart").combobox('getSelectedItem');

            if(itemTypeStart==''||itemTypeStart ==undefined){
                fish.toast("warn", "请选择单据类型");
                return;
            }
            // debugger;
            if(productTypeStart==''||productTypeStart ==undefined){
                fish.toast("warn", "请选择本地产品类型");
                return;
            }
            if(actTypeStart==''||actTypeStart ==undefined){
                fish.toast("warn", "请选择本地动作类型");
                return;
            }
            var me = this;

            if(itemTypeStart.value=='102'){
                me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/checkView',
                    productTypeStart,
                    actTypeStart,
                    'startorder',
                    'start',
                    '',
                    '',
                    itemTypeStart);
                return;
            }
            switch(productTypeStart.value){
                case '10000008'://MPLS-VPN
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/mplsVpnView',
                        productTypeStart,
                        actTypeStart,
                        'startorder',
                        'start',
                        '',
                        '',
                        itemTypeStart);
                    break;
                case '10000002'://以太网专线
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/ethernetView',
                        productTypeStart,
                        actTypeStart,
                        'startorder',
                        'start',
                        '',
                        '',
                        itemTypeStart);
                    break;
                case '10000001'://数字电路
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/digitalCircuitView',
                        productTypeStart,
                        actTypeStart,
                        'startorder',
                        'start',
                        '',
                        '',
                        itemTypeStart);
                    break;
                case '20181221002'://裸光纤
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/bareFiberView',
                        productTypeStart,
                        actTypeStart,
                        'startorder',
                        'start',
                        '',
                        '',
                        itemTypeStart);
                    break;
                case '20181221006'://局内中继电路
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/localCircuitView',
                        productTypeStart,
                        actTypeStart,
                        'startorder',
                        'start',
                        '',
                        '',
                        itemTypeStart);
                    break;
                case '20181211001'://语音中继电路
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/voiceRelayCircuitView',
                        productTypeStart,
                        actTypeStart,
                        'startorder',
                        'start',
                        '',
                        '',
                        itemTypeStart);
                    break;
                case '10000011'://互联网专线(DIA)
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/diaView',
                        productTypeStart,
                        actTypeStart,
                        'startorder',
                        'start',
                        '',
                        '',
                        itemTypeStart);
                    break;
                case '20181221003'://基础数据(ATM)
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/atmView',
                        productTypeStart,
                        actTypeStart,
                        'startorder',
                        'start',
                        '',
                        '',
                        itemTypeStart);
                    break;
                case '20181221005'://基础数据(DDN)
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/ddnView',
                        productTypeStart,
                        actTypeStart,
                        'startorder',
                        'start',
                        '',
                        '',
                        itemTypeStart);
                    break;
                case '20181221004'://基础数据(FR)
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/frView',
                        productTypeStart,
                        actTypeStart,
                        'startorder',
                        'start',
                        '',
                        '',
                        itemTypeStart);
                    break;
            }

        },
        //查询
        queryLocalApplyFun:function(){
            this.queryLocalDraftApplyList();
            this.queryLocalOtherCount();
        },
        //重置
        resetLocalApplyFun:function(){
            $("#applyOrdId").val("");//申请单编号
            $("#applyOrdName").val("");//申请单标题
            // $("#circuitNo").val("");//电路代号
            $("#custName").val("");//客户名称
            $("#productType").combobox('value','');
            $("#actType").combobox('value','');
            $("#orderType").combobox('value','');
            // $("#productType").val("");//产品类型
            // $("#actType").val("");//动作类型
        },
        //草稿单导出数据
        draftExportBtn:function(){
            var me = this;
            me.exportMethodFun();
        },
        //全部申请单导出数据
        wholeExportBtn:function(){
            var me = this;
            me.exportMethodFun();
        },
        //已完成申请单导出数据
        completedExportBtn:function(){
            var me = this;
            me.exportMethodFun();
        },
        //已完成申请单导出数据
        submitExportBtn:function(){
            var me = this;
            me.exportMethodFun();
        },
        //草稿单编辑
        draftEditBtn:function(){
            var selrow = $("#localConfig-localdraftGrid").grid("getSelection"); //获取选中的行数据
            if(selrow.srvOrdId==''||selrow.srvOrdId ==undefined){
                fish.toast("warn", "请选择一条草稿单");
                return;
            }
             var productTypeEdit = {
                 value:selrow.prodBustType,
                 name:selrow.prodBustTypeName
             };
             var actTypeEdit = {
                 value:selrow.actCode,
                 name:selrow.actCodeName
             };
            var me = this;
            // debugger
            if('102' == selrow.itemType){
                me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/checkView',
                    productTypeEdit,
                    actTypeEdit,
                    'draftorder',
                    'edit',
                    selrow.srvOrdId,
                    selrow.cstOrdId,
                    selrow.itemType);
                return;
            }
            switch (selrow.prodBustType) {
                case '10000008'://MPLS-VPN
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/mplsVpnView',
                        productTypeEdit,
                        actTypeEdit,
                        'draftorder',
                        'edit',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                        break;
                case '10000002'://以太网专线
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/ethernetView',
                        productTypeEdit,
                        actTypeEdit,
                        'draftorder',
                        'edit',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '10000001'://数字电路
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/digitalCircuitView',
                        productTypeEdit,
                        actTypeEdit,
                        'draftorder',
                        'edit',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '20181221002'://裸光纤
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/bareFiberView',
                        productTypeEdit,
                        actTypeEdit,
                        'draftorder',
                        'edit',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '20181221006'://局部中继电路
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/localCircuitView',
                        productTypeEdit,
                        actTypeEdit,
                        'draftorder',
                        'edit',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '20181211001'://语音中继电路
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/voiceRelayCircuitView',
                        productTypeEdit,
                        actTypeEdit,
                        'draftorder',
                        'edit',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '10000011'://互联网专线(DIA)
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/diaView',
                        productTypeEdit,
                        actTypeEdit,
                        'draftorder',
                        'edit',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '20181221003'://基础数据(ATM)
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/atmView',
                        productTypeEdit,
                        actTypeEdit,
                        'draftorder',
                        'edit',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '20181221005'://基础数据(DDN)
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/ddnView',
                        productTypeEdit,
                        actTypeEdit,
                        'draftorder',
                        'edit',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '20181221004'://基础数据(FR)
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/frView',
                        productTypeEdit,
                        actTypeEdit,
                        'draftorder',
                        'edit',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;

            }

        },
        //草稿单删除
        draftDeleteBtn:function(){
            var selrow = $("#localConfig-localdraftGrid").grid("getSelection"); //获取选中的行数据
            if(selrow.cstOrdId==''||selrow.cstOrdId==undefined){
                fish.toast("warn", "请选择一条草稿单");
                return;
            }
            var me = this;
            fish.confirm("确认删除该草稿单?").result.then(function(){
                unicomLocalOrderAction.delDraftInfo(selrow.cstOrdId,function (data) {
                    //删除后重新查询
                    if (data == "success"){
                        fish.toast("success", "删除成功");
                        //刷新
                        me.queryLocalDraftApplyList();
                    }else{
                        fish.toast("warn", "删除失败");
                    }

                })
            });

        },
        //全部申请单查看
        wholeViewBtn:function(){
            var selrow = $("#localConfig-localdraftGrid").grid("getSelection"); //获取选中的行数据
            if(selrow.srvOrdId==''||selrow.srvOrdId==undefined){
                fish.toast("warn", "请选择一条申请单");
                return;
            }
            var productTypeEdit = {
                value:selrow.prodBustType,
                name:selrow.prodBustTypeName
            };
            var actTypeEdit = {
                value:selrow.actCode,
                name:selrow.actCodeName
            };
            var me = this;
            // alert(selrow.srvOrdId);
            if('102' == selrow.itemType){
                me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/checkView',
                    productTypeEdit,
                    actTypeEdit,
                    'wholeorder',
                    'view',
                    selrow.srvOrdId,
                    selrow.cstOrdId,
                    selrow.itemType);
                return;
            }
            switch (selrow.prodBustType) {
                case '10000008'://MPLS-VPN
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/mplsVpnView',
                        productTypeEdit,
                        actTypeEdit,
                        'wholeorder',
                        'view',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '10000002'://以太网专线
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/ethernetView',
                        productTypeEdit,
                        actTypeEdit,
                        'wholeorder',
                        'view',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '10000001'://数字电路
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/digitalCircuitView',
                        productTypeEdit,
                        actTypeEdit,
                        'wholeorder',
                        'view',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '20181221002'://裸光纤
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/bareFiberView',
                        productTypeEdit,
                        actTypeEdit,
                        'wholeorder',
                        'view',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '20181221006'://局部中继电路
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/localCircuitView',
                        productTypeEdit,
                        actTypeEdit,
                        'wholeorder',
                        'view',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '20181211001'://语音中继电路
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/voiceRelayCircuitView',
                        productTypeEdit,
                        actTypeEdit,
                        'wholeorder',
                        'view',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '10000011'://互联网专线(DIA)
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/diaView',
                        productTypeEdit,
                        actTypeEdit,
                        'wholeorder',
                        'view',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '20181221003'://基础数据(ATM)
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/atmView',
                        productTypeEdit,
                        actTypeEdit,
                        'wholeorder',
                        'view',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '20181221005'://基础数据(DDN)
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/ddnView',
                        productTypeEdit,
                        actTypeEdit,
                        'wholeorder',
                        'view',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '20181221004'://基础数据(FR)
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/frView',
                        productTypeEdit,
                        actTypeEdit,
                        'wholeorder',
                        'view',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;

            }

        },
        //已完成申请单查看
        completedViewBtn:function(){
            var selrow = $("#localConfig-localdraftGrid").grid("getSelection"); //获取选中的行数据
            if(selrow.srvOrdId==''||selrow.srvOrdId==undefined){
                fish.toast("warn", "请选择一条已完成申请单");
                return;
            }
            var productTypeEdit = {
                value:selrow.prodBustType,
                name:selrow.prodBustTypeName
            };
            var actTypeEdit = {
                value:selrow.actCode,
                name:selrow.actCodeName
            };
            var me = this;
            // alert(selrow.srvOrdId);
            if('102' == selrow.itemType){
                me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/checkView',
                    productTypeEdit,
                    actTypeEdit,
                    'completeorder',
                    'view',
                    selrow.srvOrdId,
                    selrow.cstOrdId,
                    selrow.itemType);
                return;
            }
            switch (selrow.prodBustType) {
                case '10000008'://MPLS-VPN
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/mplsVpnView',
                        productTypeEdit,
                        actTypeEdit,
                        'completeorder',
                        'view',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '10000002'://以太网专线
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/ethernetView',
                        productTypeEdit,
                        actTypeEdit,
                        'completeorder',
                        'view',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '10000001'://数字电路
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/digitalCircuitView',
                        productTypeEdit,
                        actTypeEdit,
                        'completeorder',
                        'view',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '20181221002'://裸光纤
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/bareFiberView',
                        productTypeEdit,
                        actTypeEdit,
                        'completeorder',
                        'view',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '20181221006'://局部中继电路
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/localCircuitView',
                        productTypeEdit,
                        actTypeEdit,
                        'completeorder',
                        'view',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '20181211001'://语音中继电路
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/voiceRelayCircuitView',
                        productTypeEdit,
                        actTypeEdit,
                        'completeorder',
                        'view',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '10000011'://互联网专线(DIA)
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/diaView',
                        productTypeEdit,
                        actTypeEdit,
                        'completeorder',
                        'view',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '20181221003'://基础数据(ATM)
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/atmView',
                        productTypeEdit,
                        actTypeEdit,
                        'completeorder',
                        'view',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '20181221005'://基础数据(DDN)
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/ddnView',
                        productTypeEdit,
                        actTypeEdit,
                        'completeorder',
                        'view',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '20181221004'://基础数据(FR)
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/frView',
                        productTypeEdit,
                        actTypeEdit,
                        'completeorder',
                        'view',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;

            }

        },
        //已提交申请单查看
        submitViewBtn:function(){
            var selrow = $("#localConfig-localdraftGrid").grid("getSelection"); //获取选中的行数据
            if(selrow.srvOrdId==''||selrow.srvOrdId==undefined){
                fish.toast("warn", "请选择一条已提交申请单");
                return;
            }
            var productTypeEdit = {
                value:selrow.prodBustType,
                name:selrow.prodBustTypeName
            };
            var actTypeEdit = {
                value:selrow.actCode,
                name:selrow.actCodeName
            };
            var me = this;
            if('102' == selrow.itemType){
                me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/checkView',
                    productTypeEdit,
                    actTypeEdit,
                    'submitorder',
                    'view',
                    selrow.srvOrdId,
                    selrow.cstOrdId,
                    selrow.itemType);
                return;
            }
            switch (selrow.prodBustType) {
                case '10000008'://MPLS-VPN
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/mplsVpnView',
                        productTypeEdit,
                        actTypeEdit,
                        'submitorder',
                        'view',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '10000002'://以太网专线
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/ethernetView',
                        productTypeEdit,
                        actTypeEdit,
                        'submitorder',
                        'view',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '10000001'://数字电路
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/digitalCircuitView',
                        productTypeEdit,
                        actTypeEdit,
                        'submitorder',
                        'view',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '20181221002'://裸光纤
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/bareFiberView',
                        productTypeEdit,
                        actTypeEdit,
                        'submitorder',
                        'view',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '20181221006'://局部中继电路
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/localCircuitView',
                        productTypeEdit,
                        actTypeEdit,
                        'submitorder',
                        'view',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '20181211001'://语音中继电路
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/voiceRelayCircuitView',
                        productTypeEdit,
                        actTypeEdit,
                        'submitorder',
                        'view',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '10000011'://互联网专线(DIA)
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/diaView',
                        productTypeEdit,
                        actTypeEdit,
                        'submitorder',
                        'view',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '20181221003'://基础数据(ATM)
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/atmView',
                        productTypeEdit,
                        actTypeEdit,
                        'submitorder',
                        'view',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);;
                    break;
                case '20181221005'://基础数据(DDN)
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/ddnView',
                        productTypeEdit,
                        actTypeEdit,
                        'submitorder',
                        'view',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
                case '20181221004'://基础数据(FR)
                    me.openLocalDraftItemView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/frView',
                        productTypeEdit,
                        actTypeEdit,
                        'submitorder',
                        'view',
                        selrow.srvOrdId,
                        selrow.cstOrdId,
                        selrow.itemType);
                    break;
            }

        },
        local_apply_tabs_draft_linkFun:function () {
            var queryTypeLocal = fish.store.get("queryTypeLocal");
            if(queryTypeLocal!='draftOrder'){
                localConfigPage = 1;
            }
            fish.store.set("queryTypeLocal","draftOrder");//默认查询类型为草稿
            fish.store.set("orderStateLocal","10C");//默认定单状态草稿
            $("#localConfig-localdraftGrid").grid("hideCol", 'actTypeName');
            $("#localConfig-localdraftGrid").grid("hideCol", 'actTypeState');
            $("#localConfig-localdraftGrid").grid("hideCol", 'dianlNo');
            // $("#circuitNolabel").css('display','none');//隐藏
            // $("#circuitNodiv").css('display','none');//隐藏

            this.queryLocalDraftApplyList();
        },
        local_apply_tabs_whole_linkFun:function () {
            var queryTypeLocal = fish.store.get("queryTypeLocal");
            if(queryTypeLocal!='allOrder'){
                localConfigPage = 1;
            }
            fish.store.set("queryTypeLocal","allOrder");//默认查询类型为草稿
            fish.store.set("orderStateLocal","");//定单状态全部
            $("#localConfig-localdraftGrid").grid("hideCol", 'actTypeName');
            $("#localConfig-localdraftGrid").grid("hideCol", 'actTypeState');
            $("#localConfig-localdraftGrid").grid("hideCol", 'dianlNo');

            // $("#circuitNolabel").css('display','block');//显示
            // $("#circuitNodiv").css('display','block');//显示
            this.queryLocalDraftApplyList();
            
        },
        local_apply_tabs_completed_linkFun:function () {
            var queryTypeLocal = fish.store.get("queryTypeLocal");
            if(queryTypeLocal!='completeOrder'){
                localConfigPage = 1;
            }
            fish.store.set("queryTypeLocal","completeOrder");//默认查询类型为草稿
            fish.store.set("orderStateLocal","10F");//定单状态已完成

            $("#localConfig-localdraftGrid").grid("hideCol", 'actTypeName');
            $("#localConfig-localdraftGrid").grid("hideCol", 'actTypeState');
            $("#localConfig-localdraftGrid").grid("hideCol", 'dianlNo');

            // $("#circuitNolabel").css('display','block');//显示
            // $("#circuitNodiv").css('display','block');//显示
            this.queryLocalDraftApplyList();
            
        },
		local_apply_tabs_submit_linkFun:function () {
            var queryTypeLocal = fish.store.get("queryTypeLocal");
            if(queryTypeLocal!='submitedOrder'){
                localConfigPage = 1;
            }
            fish.store.set("queryTypeLocal","submitedOrder");//默认查询类型为草稿
            fish.store.set("orderStateLocal","10N");//定单状态已提交
            $("#localConfig-localdraftGrid").grid("hideCol", 'actTypeName');
            $("#localConfig-localdraftGrid").grid("hideCol", 'actTypeState');
            $("#localConfig-localdraftGrid").grid("hideCol", 'dianlNo');

            // $("#circuitNolabel").css('display','block');//显示
            // $("#circuitNodiv").css('display','block');//显示
            this.queryLocalDraftApplyList();
        },
        //草稿单、申请单导出数据
        exportMethodFun:function(){
            var queryTypeLocal=fish.store.get("queryTypeLocal");//查询类型
            var isLocalUnicom=fish.store.get("isLocalUnicom");//本地订单
            var orderStateLocal=fish.store.get("orderStateLocal");//订单状态

            var applyOrdId = $("#applyOrdId").val();//申请单号
            var applyOrdName = $("#applyOrdName").val();//申请单标题
            // var circuitNo = $("#circuitNo").val();//电路代号
            var custName = $("#custName").val();//客户名称
            var productType = $("#productType").val();//产品类型
            var actType = $("#actType").val();//动作类型
            var orderType = $("#orderType").val();//动作类型

            var queryObject = new Object();
            queryObject.queryType = queryTypeLocal;
            queryObject.isLocalUnicom = isLocalUnicom;
            queryObject.orderState = orderStateLocal;
            queryObject.applyOrdId = applyOrdId;
            queryObject.applyOrdName = applyOrdName;
            // queryObject.circuitNo = circuitNo;
            queryObject.custName = custName;
            queryObject.productType = productType;
            queryObject.actType = actType;
            queryObject.orderType = orderType;

            unicomLocalOrderAction.exportPageData('localScheduleLT/unicomLocalOrder/exportData.spr',queryObject);

        },
        //单据类型转换
        formatItemType: function(value){
            var itemTypeStr;
            switch (value) {
                case '101':
                    itemTypeStr = '开通单';
                    break;
                case '102':
                    itemTypeStr = '核查单';
                    break;
                default:
                    itemTypeStr = '';
                    break;
            }
            return itemTypeStr;
        },
        //格式化流程状态
        formatActTypeState: function(value){
            var actypeSta;
            switch (value) {
                case '200000001':
                    actypeSta = '未启流程';
                    break;
                case '200000002':
                    actypeSta = '已启流程';
                    break;
                case '200000003':
                    actypeSta = '回退中';
                    break;
                case '200000004':
                    actypeSta = '已结束';
                    break;
                case '200000005':
                    actypeSta = '已撤销';
                    break;
                default:
                    actypeSta = '';
                    break;
            }
            return actypeSta;

        },
        //浏览器窗口大小改变事件
        resize: function() {
            // $("#localConfig-localdraftGrid").grid("resize",true);
            $("#localConfig-localdraftGrid").grid("setGridHeight", 327);
        }

    }); //fish.View.extend END
}); //ALL END