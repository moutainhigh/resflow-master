define(["text!module/UnicomLocalNet/resmaster/portal/orderQuery/template/GomOrderListView.html",
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
            resNetworkUrl: '',
            crmRegion: '',
            userInfo: new Object(),
            template: fish.compile(GomOrderListView),
            i18nData: fish.extend({}, portalViewi18n),
            events: {
                'click #queryOrder': 'initQueryBut',
                // 'click #exportExcel': 'exportExcel',
                'click #excelOrder': 'excelOrder',

            },
            initialize: function() {
                this.render();
                this.userInfo = GomOrderListAction.queryStaffInfo().responseJSON.data;
                userId=this.userInfo.userId;
                // crmRegionMap = GomOrderListAction.qryMsmSwitchByArea(this.userInfo.areaId).responseJSON.data;
                // if(crmRegionMap !=''
                //     && crmRegionMap != undefined){
                //     this.crmRegion = crmRegionMap.CRM_REGION;
                // }
                // this.resNetworkUrl = GomOrderListAction.qryInterfaceUrl('ResourceNetWork').responseJSON.data;

            },
            //渲染页面
            render: function() {
                this.$el.html(this.template(this.i18nData));
            },
            //初始化fish组件
            afterRender: function() {
                $('#orderTitle').clearinput();
                $('#custName').clearinput();
                $('#dispatchOrderId').clearinput();
                $('#cirNum').clearinput();
                $('#serialNumber').clearinput();
                $('#subscribeId').clearinput();
                $('#tradeId').clearinput();
                $('#teacheName').clearinput();
                $('#dealJobId').clearinput();
                $('#applyOrdId').clearinput();
                URl=this.getRootPath();


                //初始化时间控件
                $('#startDate,#endDate').datetimepicker({
                    orientation: {y: 'bottom'}
                });
               /* $('#productType').on('combobox:change', function(e) {
                    var productTypeObj = new Object();
                    productTypeObj.codeType = $('#productType').val();
                    GomOrderListAction.queryProdTypeData(productTypeObj,function(data){
                        $('#actType').combobox({
                            dataSource: data,
                            placeholder: '--请选择动作类型--',
                            dataTextField: 'name',
                            dataValueField: 'value'
                        });
                    });
                });
                $('#actType').on('combobox:open',function (e) {
                    if($('#productType').val()=='' || $('#productType').val()=='undefined'){
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
                        return  false;
                    }
                });*/

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
                //modify by wang2 单击查询加载数据
                // this.queryOrderList();
            },
            initCombobox:function(){
                this.initProductTypeData();
                this.initOperationData();
            },
            //产品类型
            initProductTypeData: function(){
                var productTypeObj = new Object();
                productTypeObj.codeType = 'product_code';
                //产品类型
                GomOrderListAction.queryProdTypeData(productTypeObj,function(data) {
                    $('#productType').combobox({
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
                        // {name: 'FLOWTRACE', label: '流程跟踪', width: 100 ,formatter: function () { return  '点击查看'}},
                        // {name: 'SOURCENETWORK', label: '网络拓扑图', width: 100,formatter: function () { return  '点击查看'}},
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
                    multiselect: true,
                    shrinkToFit: false,
                    autoResizable: true,
                    // showColumnsFeature: true, //允许用户自定义列展示设置
                    cached: true, //把用户自定义的列展示设置缓存在本地
                    pageData: queryOrderList,
                    onDblClickRow: function (e, rowid, iRow, iCol) {//双击行事件
                        me.orderDetailView(e, rowid, iRow, iCol);
                    },
                    // afterInsertRow: function (e, rowid, pageData) {
                    //     $("#orderDeal-grid").grid('setCell', rowid, 'FLOWTRACE', '', {color: '#6DCC4A'});
                    //     $("#orderDeal-grid").grid('setCell', rowid, 'SOURCENETWORK', '', {color: '#6DCC4A'});
                    // },
                    // onCellSelect: function (e, rowid, iCol, cellcontent, colName, cellval) {//选中单元格的事件
                    //     // console.log("onCellSelect---->rowid:" + rowid + ", iCol:" + iCol + ", cellcontent:" + cellcontent + "  ....." + cellval + "--" + colName);
                    //     var dataCell = $("#orderDeal-grid").grid("getRowData",rowid);
                    //     if(iCol == 1){
                    //         debugger
                    //         var popFile = fish.popupView({
                    //             url: 'module/UnicomLocalNet/resmaster/portal/flowChart/views/flowChartRepView',
                    //             width: "99%",
                    //             height: "95%",
                    //             title: "二干调度流程跟踪图",
                    //             viewOption: {
                    //                 orderId: dataCell.ORDER_ID,
                    //                 srvOrdId: dataCell.SRV_ORD_ID,
                    //                 psId: dataCell.PS_ID,
                    //                 woState: '',
                    //
                    //             },
                    //             callback: function (popup, view) {
                    //                 // debugger
                    //                 popup.result.then(function (e) {
                    //
                    //                 }, function (e) {
                    //                     console.log('关闭了', e);
                    //                 });
                    //             }
                    //         });
                    //     }
                    //     if(iCol == 2){
                    //         debugger
                    //         var paramsRes={
                    //             objId:'1111111111111111111111111',
                    //             objType:2559,
                    //             objParam: {'MULTI_DATA_SOURCE_CONFIG_REGION_CODE_FOR_TOPO':me.crmRegion,
                    //                 'OUTER_SYS_PASS_CIRCUIT_NO_VALUE':dataCell.ATTR_VALUE},
                    //             objName:dataCell.ATTR_VALUE,
                    //             topoDefId:230004,
                    //             isReaderCache:true,
                    //             viewPathId:'1901201',
                    //             topoName:dataCell.ATTR_VALUE
                    //         };
                    //         window.open(me.resNetworkUrl+"&params="+fish.TripleDES.encrypt(JSON.stringify(paramsRes),'zte-soft'));
                    //     }
                    // },

                    gridComplete: function () {
                        $('.gotext').html("<span>跳转至<input class=\"ui-pagination-input\"></span>");
                    }
                });
                this.resize();
            },

            //查询工单方法
            queryOrderList: function(page, rowNum, sortname, sortorder) {
                // debugger;
                //分页信息
                rowNum = (rowNum!=''&&rowNum!=undefined)?rowNum:gomOrderNum;
                page = (page!=''&&page!=undefined)?page:gomOrderPage;

                paramsMap.pageIndex = page+'';
                paramsMap.pageSize = rowNum+'';

                var applyOrdId = $("#applyOrdId").val();//申请单编码
                var orderTitle = $("#orderTitle").val();//申请单标题
                var custName = $("#custName").val();//客户名称
                var subscribeId = $("#subscribeId").val(); //客户定单号
                var cirNum = $("#cirNum").val();//电路编码
                var serialNumber = $("#serialNumber").val();//业务号码
                var tradeId = $("#tradeId").val();// 业务订单号
                var actType = $("#actType").val();//动作类型
                var productType = $("#productType").val();//产品类型
                var dispatchOrderId = $("#dispOrderId").val();//调单编号
                var startDate = $("#startDate").val();
                var endDate = $("#endDate").val();

                paramsMap.queryTypeLocal='gomList';
                paramsMap.orderTitle=orderTitle;
                paramsMap.custName = custName;
                paramsMap.cirNum = cirNum;
                paramsMap.serialNumber = serialNumber;
                paramsMap.tradeId = tradeId;
                paramsMap.subscribeId = subscribeId;
                paramsMap.actType = actType;
                paramsMap.productType = productType;
                paramsMap.applyOrdId = applyOrdId;
                paramsMap.dispatchOrderId = dispatchOrderId;
                paramsMap.userId = userId;
                paramsMap.startDate = startDate;
                paramsMap.endDate = endDate;

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
                            srvOrdId:data.SRV_ORD_IDS,
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
                            srvOrdIds:data.SRV_ORD_IDS,
                            reginonId:'',
                            specialtyCode:'',
                            selectType:'gomOrderQuery',
                            dispatchOrderId: data.DISPATCH_ORDER_ID,
                            userInfo: me.userInfo,
                            RESOURCES:data.RESOURCES
                        },
                    callback:function(popup,view){
                        popup.result.then(function (e) {
                            me.queryOrderList();
                        },function (e) {
                            console.log('关闭了',e);
                        });
                    }
                })
            },
            initQueryBut:function(){ //查询
                var startDate = $("#startDate").val();//环节要求完成时间
                var endDate = $("#endDate").val();//环节完成时间
                if (startDate && endDate && startDate >= endDate) {
                    fish.warn("开始时间不能大于完成时间！");
                    return;
                }
                this.queryOrderList();
            },
            excelOrder:function(){  //导出
                var selarrrow = $("#orderDeal-grid").grid("getCheckRows");
                paramsMap.selarrrow=selarrrow;
                GomOrderListAction.exportGomOrderListData(URl+'/localScheduleLT/gomOrderQueryController/exportOrderList.spr',paramsMap);
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