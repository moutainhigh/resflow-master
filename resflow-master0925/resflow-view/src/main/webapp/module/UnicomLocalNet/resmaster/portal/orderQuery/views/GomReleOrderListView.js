define(["text!module/UnicomLocalNet/resmaster/portal/orderQuery/template/GomReleOrderListView.html",
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        'module/UnicomLocalNet/resmaster/portal/orderQuery/action/GomOrderListAction',
        "css!module/UnicomLocalNet/resmaster/portal/orderLocalStandby/styles/localStandby.css"],
    function(GomOrderListView,portalViewi18n,GomOrderListAction,css) {
        var userId;
        var paramsMap = new Object();
        var queryObj= new Object();
        var rUrl;
        var URl;
        var gomOrderNum = 10;
        var gomOrderPage = 1;

        return fish.View.extend({
            template: fish.compile(GomOrderListView),
            i18nData: fish.extend({}, portalViewi18n),
            events: {
                'click #queryOrder': 'initQueryBut',
                // 'click #exportExcel': 'exportExcel',
                'click #excelOrder': 'excelOrder',

            },
            initialize: function() {
                this.render();
                var userInfo = GomOrderListAction.queryStaffInfo().responseJSON.data;
                userId=userInfo.userId;
            },
            //渲染页面
            render: function() {
                this.$el.html(this.template(this.i18nData));
            },
            //初始化fish组件
            afterRender: function() {
                $('#orderTitle').clearinput();
                $('#custNameCode').clearinput();
                $('#dispatchOrderId').clearinput();
                $('#cirNum').clearinput();
                $('#serialNumberCode').clearinput();
                $('#subscribeId').clearinput();
                $('#tradeId').clearinput();
                $('#teacheName').clearinput();
                $('#dealJobId').clearinput();
                $('#applyOrdCode').clearinput();
                $('#dispOrderCode').clearinput();
                URl=this.getRootPath();
                //初始化时间控件
                $('#finishDate').datetimepicker({
                    orientation:{y:'bottom'}
                });
                $('.rowtext .ui-pagination').change(function () {
                        var p1=$(this).children('option:selected').val();//这就是selected的值
                        localGomNum = p1;
                        localGomPage = 1;
                        this.queryOrderList();
                    }
                );
                //初始化表格
                this.initCombobox();
                this.initorderDealGrid();
                this.queryOrderList();
            },
            initCombobox:function(){
                this.initProductTypeData();
                this.initOperationData();
            },
            //产品类型
            initProductTypeData: function(){
                var me =this;
                var productTypeObj = new Object();
                var orderType = me.options.orderType;
                productTypeObj.codeType = 'product_code';
                //产品类型
                GomOrderListAction.queryProdTypeData(productTypeObj,function(data) {
                    $('#productTypeCode').combobox({
                        placeholder: '--请选择产品类型--',
                        dataTextField: 'name',
                        dataValueField: 'value',
                        dataSource: data
                    });
                    $('#productTypeCode').combobox('value', me.options.productType);
                    $('#productTypeCode').combobox('disable');

                });
                if(orderType instanceof Object){
                    orderType=orderType.value;
                }
                $("#ORDER_TYPE_CODE").combobox({ dataSource: [{name:'开通单', value:'101'},{name:'核查单', value:'102'}]});
                $("#ORDER_TYPE_CODE").combobox("value",orderType);
                $('#ORDER_TYPE_CODE').combobox('disable');

            },
            //动作类型
            initOperationData: function(){
                var operationTypeObj = new Object();
                operationTypeObj.codeType = 'operate_type';
                //动作类型1
                GomOrderListAction.queryProdTypeData(operationTypeObj,function(data){
                    $('#actType').combobox({
                        dataSource: data,
                        placeholder: '--请选择动作类型--',
                        dataTextField: 'name',
                        dataValueField: 'value'
                    });
                });


            },
            initorderDealGrid: function() {
                var me = this;
                var queryOrderList = $.proxy(this.queryOrderList,this); //函数作用域改变
                $("#orderDeal-grid").grid({
                    colModel: [
                        //默认展示字段
                        {name: 'SRV_ORD_ID', label: '业务定单信息ID', width: 120, align: 'center',hidden:true },
                        // {name: 'DISPATCH_ORDER_NO', label: '调单ID', width: 120, align: 'center',hidden:true },
                        {name: 'CST_ORD_ID', label: '客户ID', width: 120, align: 'center',hidden:true },
                        {name: 'ORDER_ID', label: '订单ID', width: 240, hidden:true},
                        {name: 'PS_ID', label: '流程实例', width: 240, hidden:true},
                        {name: 'ORDER_IDS', label: '子流程定单ID合集', width: 240, hidden:true},

                        {name: 'CUST_NAME_CHINESE', label: '客户名称', width: 120, align: 'left'},
                        {name: 'SUBSCRIBE_ID', label: '客户订单号', width: 120, align: 'left'},
                        {name: 'APPLY_ORD_ID', label: '申请单编号', width: 200, align: 'left' },
                        {name: 'APPLY_ORD_NAME', label: '申请单标题', width: 200, align: 'left' },
                        {name: 'DISPATCH_ORDER_NO', label: '调度单编号', width: 200, align: 'left'},
                        {name: 'DISPATCH_TITLE', label: '调度单标题', width: 400, align: 'left'},
                        {name: 'SERIAL_NUMBER', label: '业务号码', width: 120, align: 'left'},
                        {name: 'CIRCODE', label: '电路编号', width: 150, align:'left'},
                        {name: 'SERVICETYPE', label: '产品类型', width: 180, align:'left'},
                        {name: 'ACTIVETYPENAME', label: '动作类型', width: 120, align:'left'},
                        {name: 'TACHE_NAME', label: '当前环节', width: 150, align:'left'},
                        {name: 'REQ_FIN_DATE', label: '定单要求完成时间', width: 160, align:'left'}
                    ],
                    datatype: "json",
                    autowidth: true,
                    rowNum:10,
                    rowList: [10,15,20,50,100,200,500,1000],
                    pager: true,
                    curPageSort: true,
                    recordtext:"{0}-{1} 共{2}条",
                    pgtext: " 第{0}页/共{1}页",
                    rowtext: "每页{0}条",
                    gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                    //multiselect: true,
                    shrinkToFit: false,
                    autoResizable: true,
                    // showColumnsFeature: true, //允许用户自定义列展示设置
                    cached: true, //把用户自定义的列展示设置缓存在本地
                    pageData: queryOrderList,
                    onDblClickRow: function (e, rowid, iRow, iCol) {//双击行事件
                        //me.orderDetailView(e, rowid, iRow, iCol);
                        var selrow = $("#orderDeal-grid").grid("getSelection"); //获取选中的行数据
                        var param = {srvOrdIds:selrow.SRV_ORD_IDS,orderType:me.options.orderType.value,serviceId:me.options.productType}
                        var tearOrderS = GomOrderListAction.queryIsTear(param).responseJSON;
                        if(tearOrderS.resultStat='SUCCESS'&&tearOrderS.data!=null){fish.warn("已拆机：拆机单申请单号：【"+tearOrderS.data+"】"); return;}
                        var onWayOrderS = GomOrderListAction.queryisOnWay(param).responseJSON;
                        if(onWayOrderS.resultStat='SUCCESS'&&onWayOrderS.data!=null){fish.warn("存在在途单：在途单申请单号：【"+onWayOrderS.data+"】"); return;}
                        me.popup.close(selrow);
                    },
                    gridComplete: function () {
                        $('.gotext').html("<span>跳转至<input class=\"ui-pagination-input\"></span>");
                    }
                });
                this.resize();
            },

            //查询工单方法
            queryOrderList: function(page, rowNum, sortname, sortorder) {
                var me=this;
                // debugger;
                //分页信息
                rowNum = (rowNum!=''&&rowNum!=undefined)?rowNum:gomOrderNum;
                page = (page!=''&&page!=undefined)?page:gomOrderPage;

                paramsMap.pageIndex = page+'';
                paramsMap.pageSize = rowNum+'';

                var applyOrdCode = $("#applyOrdCode").val();//申请单编码
                var orderTitle = $("#orderTitle").val();//申请单标题
                var custNameCode = $("#custNameCode").val();//客户名称
                var subscribeId = $("#subscribeId").val(); //客户定单号
                var cirNum = $("#cirNum").val();//电路编码
                var serialNumberCode = $("#serialNumberCode").val();//业务号码
                // var tradeId = $("#tradeId").val();// 业务订单号
                //var actType = $("#actType").val();//动作类型
                var productTypeCode = $("#productTypeCode").val();//产品类型
                var dispOrderCode = $("#dispOrderCode").val();//调单编号

                paramsMap.queryTypeLocal='gomList';
                paramsMap.orderTitle=orderTitle;
                paramsMap.custName = custNameCode;
                paramsMap.cirNum = cirNum;
                paramsMap.serialNumber = serialNumberCode;
                // paramsMap.tradeId = tradeId;
                paramsMap.subscribeId = subscribeId;
                paramsMap.actType = '101'; //只查新开单
                paramsMap.productType = this.options.productType;
                paramsMap.applyOrdId = applyOrdCode;
                paramsMap.dispatchOrderId = dispOrderCode;
                paramsMap.userId = userId;
                paramsMap.orderState='已完成';
                paramsMap.srvOrdStat='10F';
                if(me.options.orderType instanceof Object){
                    paramsMap.orderType=me.options.orderType.value;
                }else{
                    paramsMap.orderType=me.options.orderType;

                }


                // debugger;
                //调用后台方法
                $("#orderDeal-grid").blockUI({message: '加载中'}).data('blockui-content', true);
                GomOrderListAction.queryOrderList(paramsMap,function (data) {
                    var gridData = {
                        "rows": data.data,
                        "page": page,
                        "records": data.dataLength,
                        "rowNum": rowNum,
                        "total":data.total
                    };
                    $("#orderDeal-grid").grid("reloadData", gridData);
                    $("#orderDeal-grid").unblockUI().data('blockui-content', false);
                });
                // $("#orderDeal-grid").unblockUI().data('blockui-content', false);
            },

            orderDetailView:function(){ //详情页面
                var me = this;
                var data = $("#orderDeal-grid").grid("getSelection");
                // debugger
                var pop = fish.popupView({
                    url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/orderDetailsView',
                    width: "99%",
                    height: "100%",
                    title: "工单详情",
                    viewOption:{
                            buttonState:'',
                            psId:'',
                            srvOrdId:'',
                            cstOrdId:data.CST_ORD_ID,
                            orderId:'',
                            orderIdSelect:'',
                            orderIds:data.ORDER_IDS,
                            resources:data.resources,
                            woState:'',
                            woId:'',
                            tacheId:'',
                            compUserId:'',
                            serviceId:data.SERVICE_ID,
                            srvordId:'',
                            srvordIds:data.SRV_ORD_IDS,
                            reginonId:'',
                            specialtyCode:'',
                            selectType:'gomOrderQuery',
                            dispatchOrderId: data.DISPATCH_ORDER_ID

                        },
                    callback:function(popup,view){
                        popup.result.then(function (e) {
                            me.queryOrderList();
                        },function (e) {
                            console.log('关闭了',e);
                            // $("#touchGet").css("style","display:block; float: left;");
                            // $("#touchGet").show();
                        });
                    }
                })
            },

            initQueryBut:function(){ //查询
                this.queryOrderList();
            },

            resize: function() {
                // $("#orderDeal-grid").grid("resize",true);
                $("#orderDeal-grid").grid("setGridHeight", 327);
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
        }); //fish.View.extend END
    }); //ALL END