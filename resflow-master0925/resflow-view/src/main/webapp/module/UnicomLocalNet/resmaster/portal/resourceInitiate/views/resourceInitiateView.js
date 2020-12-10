define([
        'module/UnicomLocalNet/resmaster/portal/resourceInitiate/action/resourceInitiateAction',
        'text!module/UnicomLocalNet/resmaster/portal/resourceInitiate/templates/resourceInitiateView.html',
        'i18n!module/UnicomLocalNet/resmaster/portal/resourceInitiate/i18n/resourceInitiateView.i18n',
        'css!module/UnicomLocalNet/resmaster/portal/resourceInitiate/styles/resourceInitiateView.css'],
    function(resourceInitiateAction,resourceInitiateView,i18n,css) {
        return fish.View.extend({
            template: fish.compile(resourceInitiateView),
            i18nData: fish.extend({}, i18n),
            events: {
                "click #queyCircuitInfoFromResource" : 'queyCircuitInfo',
                "click #exportExcelResourceView" : 'exportExcel',
            },
            initialize: function() {
                this.render();
            },
            //渲染页面
            render: function() {
                this.$el.html(this.template(this.i18nData));
                return this;
            },

            //初始化fish组件
            afterRender: function() {
                //初始化草稿单表格
                this.initGrid();
                //初始化下拉框
                this.initCombobox();
                //输入框最右边添加X
                $('#circuitCode').clearinput();
                $('#serialNumber').clearinput();
            },
            initCombobox:function(){
                this.initProductTypeData();
            },
            //产品类型
            initProductTypeData: function(){
                var productTypeObj = new Object();
                productTypeObj.codeType = 'product_code';
                //产品类型
                resourceInitiateAction.queryProdTypeData(productTypeObj,function(data) {
                    $('#productType').combobox({
                        placeholder: '--请选择产品类型--',
                        dataTextField: 'name',
                        dataValueField: 'value',
                        dataSource: data
                    });
                });
            },

            initGrid:function() {
                var me = this;
                $("#circuitInfoGrid").grid({
                    colModel: [
                        //默认展示字段
                        {name: 'circuitId',label:'电路id',width:170 },
                        {name: 'prodInstId',label:'产品实例号',width:170, align: 'left' },
                        {name: 'circuitCode',label:'电路编号',width:170, align: 'left'},
                        {name: 'accNbr',label:'业务号码',width:170, align: 'left'},
                        {name: 'resType',label:'资源类型',width:170, align: 'left'},
                        {name: 'oprState',label:'业务状态',width:170, align: 'left'},
                        {name: 'oprStateId',label:'业务状态ID',width:170, align: 'left',hidden:true},
                        {name: 'sbOprState',label:'实例状态',width:170, align: 'left'},
                        {name: 'crmOrderCode',label:'srv_order_id',width:170, align: 'left',hidden:true},
                        {name: 'regionId',label:'regionId',width:170, align: 'left',hidden:true},
                        {name: 'serviceId',label:'产品类型',width:170, align: 'left',hidden:true},
                    ],
                    curPageSort: false,
                    height:240,
                    datatype: 'json',
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
                        me.queyCircuitInfoFromResource(e, rowid, iRow, iCol);
                    }.bind(this),
                    onDblClickRow: function (e, rowid, iRow, iCol) {//双击行事件
                        me.openDetailWindow(e, rowid, iRow, iCol)
                    },
                });
                this.resize();
            },
            queyCircuitInfo:function(){
                this.queyCircuitInfoFromResource();
            },
            //调用资源接口查询电路
            queyCircuitInfoFromResource: function(pageIndex, pageSize, sortname, sortorder) {
                //校验产品类型必填
                var productType = $("#productType").val();
                if (productType == null || productType == ''){
                    fish.info("产品类型为必填项，请选择产品类型！")
                    return;
                }
                if ($("#circuitCode").val() == '' && $("#serialNumber").val() == '') {
                    fish.info("电路编号和业务号码不能同时为空！");
                    return;
                }
                var param = new Object();
                if (pageIndex == undefined){
                    param.pageIndex = 1;
                } else {
                    param.pageIndex = pageIndex;
                }
                if (pageSize == undefined){
                    param.pageSize = 10;
                } else {
                    param.pageSize = pageSize
                }
                param.type = '';
                param.circuitCode = $("#circuitCode").val();//电路编号
                param.productType = $("#productType").val();//产品类型
                param.accNbr = $("#serialNumber").val();//业务号码
                param.isToBeQuery = '0';// 精确查询，只查询已归档
                //每次调用资源接口查询时，都要先将grid清空
                $("#circuitInfoGrid").grid("reloadData", null);
                //调用后台方法
                $("#circuitInfoGrid").blockUI({
                    message: '加载中'
                }).data('blockui-content', true);
                resourceInitiateAction.queyCircuitInfoFromResource(param,function(res){
                    if (res.success){
                        $("#circuitInfoGrid").grid("reloadData", res.data);
                    }else{
                        console.log(res.msg);
                        fish.toast("warn", "查询不到数据，请确认输入内容是否正确或联系管理员！");
                    }
                    $("#circuitInfoGrid").unblockUI().data('blockui-content', false);
                });
            },

            //浏览器窗口大小改变事件
            resize: function() {
                $("#circuitInfoGrid").grid("setGridHeight", 360);
            },

            /**
             * 双击行事件
             */
            openDetailWindow:function () {
                var me = this;
                var checkData = $("#circuitInfoGrid").grid("getSelection");
                if (checkData != null && checkData != "" && checkData != undefined){
                    //根据业务流水号判断电路是否在调度中走过流程
                    var param = {};
                    param.instanceId = checkData.prodInstId;
                    param.srvOrdId = checkData.crmOrderCode;
                    resourceInitiateAction.querySrvOrdInfoByInstanceId(param, function(res){
                        if (res.success){
                            if (res.data.length > 0) { //在调度走过流程
                                //查询详情页需要的数据
                                resourceInitiateAction.queryCircuitInfoBySrvOrdId(param,function(resData){
                                    if (resData.data!=null&&resData.data!=""){
                                        var options = {
                                            url: 'module/UnicomLocalNet/resmaster/portal/resourceInitiate/views/circuitDetailsInfoView',
                                            height: '99%',
                                            width: "95%",
                                            modal: false,
                                            draggable: true,
                                            resizable:true,
                                            autoResizable: true,
                                            viewOption: {
                                                selrow : resData.data,
                                                checkData: checkData,
                                                // circuitState:checkData.oprStateId,
                                                // instanceId:checkData.prodInstId,
                                                srvOrdInfo:res.data,
                                                systemResource:resData.data.SYSTEM_RESOURCE
                                            },
                                            callback: function (popup, view) {
                                                popup.result.then(function (res) {

                                                }, function (e) {
                                                    console.log('关闭了', e);
                                                });
                                            }
                                        };
                                        var popup = fish.popupView(options);
                                    }else{
                                        fish.info("系统有未归档的定单"+res.data[0].APPLY_ORD_ID+"，不能发起补录流程");
                                        return;
                                    }
                                });

                            }else{ //没有在调度走过流程
                                //先查询出数据，有数据弹出详情页，没有给予提示不弹页面
                                me.queryRouteInfo();
                            }
                        }
                    });
                }
            },
            exportExcel :function(){
                var me = this;
                var productType = $("#productType").val();
                if (productType == null || productType == ''){
                    fish.info("产品类型为必填项，请选择产品类型！")
                    return;
                }
                if ($("#circuitCode").val() == '' && $("#serialNumber").val() == '') {
                    fish.info("电路编号和业务号码不能同时为空！");
                    return;
                }
                var param = {};
                param.pageIndex = 1;
                param.pageSize = 10000;
                param.circuitCode = $("#circuitCode").val();//电路编号
                param.productType = $("#productType").val();//产品类型
                param.accNbr = $("#serialNumber").val();//业务号码
                param.isToBeQuery = '0';// 只查询
                resourceInitiateAction.exportCircuitInfoFromResouce(me.getRootPath() + '/localScheduleLT/resourceInitiate/exportCircuitInfo.spr', param);
            },
            getRootPath: function () {
                // 获取当前网址，如： http://localhost:8083/uimcardprj/share/meun.jsp
                var curWwwPath = window.document.location.href;
                // 获取主机地址之后的目录，如： uimcardprj/share/meun.jsp
                var pathName = window.document.location.pathname;
                var pos = curWwwPath.indexOf(pathName);
                // 获取主机地址，如： http://localhost:8083
                var localhostPaht = curWwwPath.substring(0, pos);
                // 获取带"/"的项目名，如：/uimcardprj
                var projectName = pathName.substring(0, pathName.substr(1).indexOf('/') + 1);
                return (localhostPaht + projectName);
            },
            queryRouteInfo:function(){
                var checkData = $("#circuitInfoGrid").grid("getSelection");
                var param = {};
                param.businessNum = checkData.accNbr;
                param.CIRCUIT_NO = checkData.circuitCode;
                param.regionId = checkData.regionId;
                resourceInitiateAction.queryRouteInfo(param,function(res){
                    if (res.success){
                        fish.popupView({
                            url: 'module/UnicomLocalNet/resmaster/portal/resourceInitiate/views/routingDetailsInfoView',
                            height: '99%',
                            width: "95%",
                            modal: false,
                            draggable: true,
                            resizable:true,
                            autoResizable: true,
                            viewOption: {
                                res:res,
                                checkData:checkData
                            },
                            callback: function (popup, view) {
                                popup.result.then(function (res) {

                                }, function (e) {
                                    console.log('关闭了', e);
                                });
                            }
                        });
                    }else{
                        fish.info(res.msg);
                    }
                });
            },

        });
    });