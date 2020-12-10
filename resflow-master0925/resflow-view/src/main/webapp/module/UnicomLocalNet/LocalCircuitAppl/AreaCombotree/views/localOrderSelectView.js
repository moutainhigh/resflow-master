define([
        'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/action/localOrderSelectAction',
        'text!module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/templates/localOrderSelectView.html',
        'i18n!module/UnicomLocalNet/resmaster/portal/local/i18n/unicomLocalOrderView.i18n',
        'css!module/UnicomLocalNet/resmaster/portal/local/css/unicomLocalOrderView.css'],
    function(localOrderSelectAction,localOrderSelectView,i18n,css) {

        var localConfigNum = 10;
        var localConfigPage = 1;
        var productType ;

        return fish.View.extend({
            template: fish.compile(localOrderSelectView),
            i18nData: fish.extend({}, i18n),
            events: {
                'click #startLocalApply' : 'startLocalApplyFun',
                "click #queryLocalApply" : 'queryLocalApplyFun',
                'click #resetLocalApply' : 'resetLocalApplyFun',

                // 'click #completedView' : 'completedViewBtn',
                'click #completedExport' : 'completedExportBtn',

                'click #local_apply_tabs_completed_link' : 'local_apply_tabs_completed_linkFun'
            },
            initialize: function() {
                this.render();
                fish.store.set("isLocalUnicom","secondary");//默认二干
                fish.store.set("queryTypeLocal","completeOrder");//默认查询类型为草稿
                fish.store.set("orderStateLocal","10F");//默认定单状态草稿

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
                $('#orderCode').clearinput();
                $('#orderTitle').clearinput();
                $('#circuitNo').clearinput();
                //初始化全部申请单
                // this.initorderWholeGrid();
                //初始化已完成申请单
                // this.initorderCompletedGrid();
                //初始化已提交的申请单
                // this.initordersubmitGrid();

                //默认选中待接单区
                $('#local_apply_tabs_draft_link').click();
                //初始化下拉框
                this.initCombobox();

                $('#produc').on('combobox:change', function(e) {
                    var productTypeObj = new Object();
                    productTypeObj.codeType = $('#produc').val();
                    localOrderSelectAction.queryProdTypeData(productTypeObj,function(data){
                        $('#actT').combobox({
                            dataSource: data,
                            placeholder: '--请选择动作类型--',
                            dataTextField: 'name',
                            dataValueField: 'value'
                        });
                    });
                });
                $('#actT').on('combobox:open',function (e) {
                    if($('#produc').val()==''
                        ||$('#produc').val()=='undefined'){

                    }
                });

                $('#actT').on('combobox:change',function () {
                    if($('#produc').val()==''
                        ||$('#produc').val()=='undefined'){
                        $("#actT").combobox('value','');
                    }
                });

                //本地产品类型、动作类型事件 start
                $('#productTypeStart').on('combobox:change', function(e) {
                    var productTypeObj = new Object();
                    // debugger;
                    if($('#productTypeStart').val()==''
                        ||$('#productTypeStart').val()== undefined) {
                        operationTypeObj.codeType = 'operate_type';
                    }else{
                        productTypeObj.codeType = $('#productTypeStart').val();
                    }
                    localOrderSelectAction.queryProdTypeData(productTypeObj,function(data) {
                        $('#actTypeStart').combobox({
                            dataSource: data,
                            placeholder: '--请选择动作类型--',
                            dataTextField: 'name',
                            dataValueField: 'value'
                        });
                    });


                });
                //初始化其它tab页数量
                this.queryLocalOtherCount();

                $('.rowtext .ui-pagination').change(function () {
                        var p1=$(this).children('option:selected').val();//这就是selected的值
                        localConfigNum = p1;
                    }
                );

            },

            initCombobox:function(){
                productType=this.options.productType;
                this.initProductTypeData();
                this.initOperationData();

            },
            //产品类型
            initProductTypeData: function(){
                var productTypeObj = new Object();
                productTypeObj.codeType = 'product_code';
                //产品类型
                localOrderSelectAction.queryProdTypeData(productTypeObj,function(data) {
                    $('#produc').combobox({
                        placeholder: '--请选择产品类型--',
                        dataTextField: 'name',
                        dataValueField: 'value',
                        dataSource: data
                    });
                    $('#produc').combobox("value",productType);
                    $("#produc").combobox("disable");
                });
            },

            //动作类型
            initOperationData: function(){
                var operationTypeObj = new Object();
                operationTypeObj.codeType = 'operate_type';
                //动作类型1
                localOrderSelectAction.queryProdTypeData(operationTypeObj,function(data){
                    $('#actT').combobox({
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
                $("#Config-draftGrid").grid({
                    colModel: [
                        //默认展示字段
                        {name:'orderId',label:'流程定单ID',width:200,hidden: true },
                        {name:'srvOrdId',label:'业务定单ID',width:200,hidden: true },
                        {name:'applyOrdId',label:'核查单编号',width:200,align: 'left'},
                        {name:'serialNumber',label:'业务号码',width:200,align: 'left',hidden: true },
                        {name: 'orderCode',label:'定单编码',width:170,hidden: true},
                        {name: 'applyOrdName',label:'核查单标题',width:180, align: 'left' },
                        {name: 'dianlNo',label:'电路编号',width:150, align: 'left',hidden: true },
                        {name: 'cstOrdId',label:'客户Id',width:150,hidden: true },
                        {name: 'custName',label:'客户名称',width:200, align: 'left' },
                        {name: 'prodBustType',label:'产品类型',width:150,hidden: true, align: 'left' },
                        {name: 'prodBustTypeName',label:'产品类型',width:150, align: 'left' },
                        {name: 'actCode',label:'动作类型',width:150,hidden: true },
                        {name: 'num',label:'电路数量',width:150 },
                        {name: 'actCodeName',label:'动作类型',width:150, align: 'left' },
                        {name: 'actTypeName',label:'流程类型',width:150,hidden: true , align: 'left'},
                        {name: 'actTypeState',label:'流程状态',width:150,hidden: true , align: 'left',formatter:me.formatActTypeState},
                        {name: 'dispObjName',label:'当前执行人',width:150,hidden: true  },
                        {name: 'createDateStr',label:'申请时间',width:150, align: 'left',hidden: true  },
                        {name: 'omlParentOrderId',label:'OML_PARENT_ORDER_ID',width:150,hidden: true },
                        {name: 'omlOrderId',label:'OML_ORDER_ID',width:150,hidden: true }
                    ],
                    autowidth: true,
                    curPageSort: false,
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
                    pageData: null,
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
                queryTypeLocal = 'completeOrder';
                switch(queryTypeLocal){

                    case 'completeOrder':
                        me.completedViewBtn(e, rowid, iRow, iCol);
                        break;
                }
            },
            //查询草稿单、申请单数量
            queryLocalOtherCount: function(){
                var queryTypeLocal=fish.store.get("queryTypeLocal");//查询类型
                var isLocalUnicom=fish.store.get("isLocalUnicom");//本地订单 localBuild

                var orderCode = $("#orderCode").val();//申请单号
                var orderTitle = $("#orderTitle").val();//申请单标题
                var circuitNo = $("#circuitNo").val();//电路代号
                var custName = $("#custName").val();//客户名称
                var productType = $("#produc").val();//产品类型
                var actType = $("#actT").val();//动作类型
                //actType='107';
                var queryObject = new Object();
                // queryObject.orderState = orderStateLocal;
                queryObject.orderCode = orderCode;
                queryObject.orderTitle = orderTitle;
                queryObject.circuitNo = circuitNo;
                queryObject.custName = custName;
                queryObject.productType = productType;
                queryObject.actType = actType;
                queryObject.isLocalUnicom = isLocalUnicom;
                switch(queryTypeLocal){

                    case 'completeOrder':
                        queryObject.draftOrderState = '10C';
                        queryObject.allOrderState = '';
                        queryObject.completeOrderState = '';
                        queryObject.submitedOrderState = '10N';
                        break;

                }
                localOrderSelectAction.queryLocalApplyOrderCount(queryObject,function(data){
                    if (data.message == "success") {
                        switch(queryTypeLocal){

                            case 'completeOrder':
                                //$('#completespans').text(data.completeOrderCount);
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

                var orderCode = $("#orderCode").val();//申请单号
                var orderTitle = $("#orderTitle").val();//申请单标题
                var circuitNo = $("#circuitNo").val();//电路代号
                var custName = $("#custNam").val();//客户名称
                var productType = $("#produc").val();//产品类型
                if(productType==''
                    ||productType==undefined){
                    productType=this.options.productType;
                }
                var actType = $("#actT").val();//动作类型
                //actType='107';
                if((productType==''
                    ||productType==undefined)
                    &&(actType!=''&&actType!=undefined)){
                    // fish.toast("warn", "请先选择产品类型");
                    // return;
                }
                // debugger;
                rowNum = (rowNum!=''&&rowNum!=undefined)?rowNum:localConfigNum;
                page = (page!=''&&page!=undefined)?page:localConfigPage;
                // rowNum = rowNum || localConfigNum;
                // page = page || localConfigPage;
                if (page == undefined) {
                    page = 1;
                }
                if (rowNum == undefined) {
                    rowNum = 10;
                }
                var queryObject = new Object();
                queryObject.queryType = queryTypeLocal;
                queryObject.isLocalUnicom = isLocalUnicom;
                queryObject.orderState = orderStateLocal;
                queryObject.applyOrdId = orderCode;
                queryObject.applyOrdName = orderTitle;
                queryObject.circuitNo = circuitNo;
                queryObject.custName = custName;
                queryObject.productType = productType;
                queryObject.actType = actType;
                queryObject.pageIndex = page+'';
                queryObject.pageSize = rowNum+'';
                queryObject.sortname = sortname;
                queryObject.sortorder = sortorder;
                queryObject.orderType = '102';
                queryObject.selectType = '1';
                // console.log($('.rowtext .ui-pagination').val());

                //调用后台方法
                $("#Config-draftGrid").blockUI({message: '加载中'}).data('blockui-content', true);
                localOrderSelectAction.queryLocalApplyOrderCheckData(queryObject,function(data){
                    if (data.message == "success") {
                        // var gridData = {
                        //     "rows": data.unicomVoList,
                        //     "page": page,
                        //     "records": data.unicomVoList.length,
                        //     "rowNum": rowNum,
                        //     "total":data.pageInfo.pageCount
                        // };
                        //显示订单数量
                        switch(queryTypeLocal){

                            case 'completeOrder':
                                
                                $('#completespans').text(data.pageInfo.rowCount);
                                break;

                        }
                        // debugger;
                        $("#Config-draftGrid").grid("reloadData", data.unicomVoList);
                    }else{
                        fish.toast("warn", "获取数据失败");
                    }
                });
                localConfigNum = $("#Config-draftGrid").grid("getGridParam","rowNum");
                localConfigPage = $("#Config-draftGrid").grid("getGridParam","page");
                $("#Config-draftGrid").unblockUI().data('blockui-content', false);
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
                var productTypeStart =  $("#productTypeStart").combobox('getSelectedItem');
                var actTypeStart =  $("#actTypeStart").combobox('getSelectedItem');
                // debugger;
                if(productTypeStart==''||productTypeStart ==undefined){

                }
                if(actTypeStart==''||actTypeStart ==undefined){

                }
                var me = this;

                if('107' == actTypeStart.value){
                    me.openLocalDraftView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/checkView',
                        productTypeStart,
                        actTypeStart,
                        'startorder',
                        'start',
                        '',
                        '');
                    return;
                }
                switch(productTypeStart.value){
                    case '10000008'://MPLS-VPN
                        me.openLocalDraftView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/mplsVpnView',
                            productTypeStart,
                            actTypeStart,
                            'startorder',
                            'start',
                            '',
                            '');
                        break;
                    case '10000002'://以太网专线
                        me.openLocalDraftView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/ethernetView',
                            productTypeStart,
                            actTypeStart,
                            'startorder',
                            'start',
                            '',
                            '');
                        break;
                    case '10000001'://数字电路
                        var me =this;
                        //获取选中的节点
                        var nodes;
                        alert("--"+selrow.tradeId);

                        me.popup.close(nodes);
                        break;
                    case '20181221002'://裸光纤
                        me.openLocalDraftView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/bareFiberView',
                            productTypeStart,
                            actTypeStart,
                            'startorder',
                            'start',
                            '',
                            '');
                        break;
                    case '20181221006'://局内中继电路
                        me.openLocalDraftView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/localCircuitView',
                            productTypeStart,
                            actTypeStart,
                            'startorder',
                            'start',
                            '',
                            '');
                        break;
                    case '20181211001'://语音中继电路
                        me.openLocalDraftView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/voiceRelayCircuitView',
                            productTypeStart,
                            actTypeStart,
                            'startorder',
                            'start',
                            '',
                            '');
                        break;
                    case '10000011'://互联网专线(DIA)
                        me.openLocalDraftView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/diaView',
                            productTypeStart,
                            actTypeStart,
                            'startorder',
                            'start',
                            '',
                            '');
                        break;
                    case '20181221003'://基础数据(ATM)
                        me.openLocalDraftView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/atmView',
                            productTypeStart,
                            actTypeStart,
                            'startorder',
                            'start',
                            '',
                            '');
                        break;
                    case '20181221005'://基础数据(DDN)
                        me.openLocalDraftView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/ddnView',
                            productTypeStart,
                            actTypeStart,
                            'startorder',
                            'start',
                            '',
                            '');
                        break;
                    case '20181221004'://基础数据(FR)
                        me.openLocalDraftView('module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/frView',
                            productTypeStart,
                            actTypeStart,
                            'startorder',
                            'start',
                            '',
                            '');
                        break;
                }

            },
            //查询
            queryLocalApplyFun:function(){
                var queryTypeLocal = fish.store.get("queryTypeLocal");
                if(queryTypeLocal!='completeOrder'){
                    localConfigPage = 1;
                }
                fish.store.set("queryTypeLocal","completeOrder");//默认查询类型为草稿
                fish.store.set("orderStateLocal","10F");//定单状态已完成

                $("#Config-draftGrid").grid("hideCol", 'actTypeName');
                $("#Config-draftGrid").grid("showCol", 'orderTitle');
                $("#orderTitlediv").css('display','block');//显示
                $("#orderTitlelabel").css('display','block');//显示
                $("#circuitNolabel").css('display','block');//显示
                $("#circuitNodiv").css('display','block');//显示
                this.queryLocalDraftApplyList();
                this.queryLocalOtherCount();
            },
            //重置
            resetLocalApplyFun:function(){
                $("#orderCode").val("");//申请单号
                $("#orderTitle").val("");//申请单标题
                $("#circuitNo").val("");//电路代号
                $("#custNam").val("");//客户名称
                $("#actT").combobox('value','');
                // $("#productType").val("");//产品类型
                // $("#actType").val("");//动作类型
            },




            //已完成申请单查看
            completedViewBtn:function(){
                var selrow = $("#Config-draftGrid").grid("getSelection"); //获取选中的行数据
                var productTypeEdit = {
                    value:selrow.prodBustType,
                    name:selrow.prodBustTypeName
                };
                var actTypeEdit = {
                    value:selrow.actCode,
                    name:selrow.actCodeName
                };
                this.popup.close(selrow);
            },


            local_apply_tabs_completed_linkFun:function () {
                var queryTypeLocal = fish.store.get("queryTypeLocal");
                if(queryTypeLocal!='completeOrder'){
                    localConfigPage = 1;
                }
                fish.store.set("queryTypeLocal","completeOrder");//默认查询类型为草稿
                fish.store.set("orderStateLocal","10F");//定单状态已完成

                $("#Config-draftGrid").grid("hideCol", 'actTypeName');
                $("#Config-draftGrid").grid("showCol", 'orderTitle');
                $("#orderTitlediv").css('display','block');//显示
                $("#orderTitlelabel").css('display','block');//显示
                $("#circuitNolabel").css('display','block');//显示
                $("#circuitNodiv").css('display','block');//显示
                this.queryLocalDraftApplyList();

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
                // $("#Config-draftGrid").grid("resize",true);
                $("#Config-draftGrid").grid("setGridHeight", 327);
            }

        }); //fish.View.extend END
    }); //ALL END