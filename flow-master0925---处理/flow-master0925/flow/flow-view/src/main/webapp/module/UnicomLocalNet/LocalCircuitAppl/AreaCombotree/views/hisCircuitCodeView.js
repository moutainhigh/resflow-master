define([
        'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/action/localOrderSelectAction',
        'text!module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/templates/hisCircuitCodeView.html',
        'i18n!module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/i18n/gomCircuitCodeListView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/taskmanagement.css'],
    function(localOrderSelectAction,hisCircuitCodeView,i18n,css) {

        return fish.View.extend({
            template: fish.compile(hisCircuitCodeView),
            i18nData: fish.extend({}, i18n),
            events: {
                "click #queryLocalApply" : 'queryLocalApplyFun',
                "click #exportExcel" : 'exportExcel',
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
                //初始grid
                this.initorderDraftGrid();
                //初始化下拉框
                this.initCombobox();
                $('#circuitCode').clearinput();
                $('#accNbr').clearinput();
            },
            initCombobox:function(){
                this.initProductTypeData();
            },
            //产品类型
            initProductTypeData: function(){
                var productTypeObj = new Object();
                productTypeObj.codeType = 'product_code';
                //产品类型
                localOrderSelectAction.queryProdTypeData(productTypeObj,function(data) {
                    $('#productType').combobox({
                        placeholder: '--请选择产品类型--',
                        dataTextField: 'name',
                        dataValueField: 'value',
                        dataSource: data
                    });
                });
                $("input[name$='custId']").popedit({
                    open:function(e) {
                        var _this = $(this);
                        var options = {
                            url: 'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/views/custnameSelectView',
                            height: '80%',
                            width: '80%',
                            modal: false,
                            draggable: true,
                            resizable:true,
                            autoResizable: true,
                            viewOption: {
                                flag:'queryCustName'
                            },
                            callback: function (popup, view) {
                                popup.result.then(function (data) {
                                    _this.popedit('setValue', {name:data.custName, value:data.custId});
                                }, function (e) {
                                    console.log('关闭了', e);
                                });
                            }
                        };
                        var popup = fish.popupView(options);
                    }
                });
            },

            initorderDraftGrid:function() {
                var me = this;
                $("#Config-draftG").grid({
                    colModel: [
                        //默认展示字段
                        {name: 'circuitId',label:'电路id',width:170 },
                        {name: 'prodInstId',label:'产品实例号',width:170, align: 'left' },
                        {name: 'circuitCode',label:'电路编号',width:170, align: 'left'},
                        {name: 'accNbr',label:'业务号码',width:170, align: 'left'},
                        {name: 'resType',label:'资源类型',width:170, align: 'left'},
                        {name: 'oprState',label:'业务状态',width:170, align: 'left'},
                        {name: 'sbOprState',label:'实例状态',width:170, align: 'left'},
                        {name: 'crmOrderCode',label:'srv_order_id',width:170, align: 'left',hidden:true},
                    ],
                    curPageSort: false,
                    recordtext:"{0}-{1} 共{2}条",
                    pgtext: " 第{0}页/共{1}页",
                    rowtext: "每页{0}条",
                    datatype: 'json',
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
                        me.queryLocalDraftApplyList(e, rowid, iRow, iCol);
                    }.bind(this),
                    onDblClickRow: function (e, rowid, iRow, iCol) {//双击行事件
                        me.openDetailWindow(e, rowid, iRow, iCol)
                    },
                });
                this.resize();
            },
            //电路查询
            queryLocalDraftApplyList: function(page, rowNum, sortname, sortorder) {

                var productType = $("#productType").val();
                var custId =  $("#custId").val();
                if ((productType == null || productType == '')&&(custId == null || custId == '')){
                    fish.info("产品类型、客户名称其中一个为必填，请选择");
                    return;
                }
                if(productType!=""){
                    if ($("#circuitCode").val() == '' && $("#accNbr").val() == '') {
                        fish.info("产品类型不为空时，电路编号和业务号码不能同时为空！");
                        return;
                    }
                }
                var param = new Object();
                param.type = '';
                if (page == undefined) {
                    param.pageIndex = 1;
                }else{
                    param.pageIndex = page;
                }
                if (rowNum == undefined) {
                    param.pageSize = 10;
                }else{
                    param.pageSize = rowNum;
                }
                param.circuitCode = $("#circuitCode").val();
                param.productType = $("#productType").val();;
                param.accNbr = $("#accNbr").val();
                param.isToBeQuery = '0';// 只查询
                if($("#custId").val()!=''){
                    param.custId =  $("#custId").popedit("getValue").value;//客户id
                }else{
                    param.custId ='';
                }
                //调用后台方法
                $("#Config-draftG").blockUI({message: '加载中'}).data('blockui-content', true);
                localOrderSelectAction.queryStockCircuitInfo(param,function(res){
                    if (res.success) {
                        $("#Config-draftG").grid("reloadData", res.data);
                    }else{
                        console.log(res.msg);
                        fish.toast("warn", "查询不到数据，请确认输入内容是否正确或联系管理员！");
                    }
                    $("#Config-draftG").unblockUI().data('blockui-content', false);
                });
            },

            //查询
            queryLocalApplyFun:function(){
                this.queryLocalDraftApplyList();
            },
            //浏览器窗口大小改变事件
            resize: function() {
                $("#Config-draftG").grid("resize", true);
                var frameHeight = document.documentElement.scrollHeight;
                $("#Config-draftG").grid("setGridHeight", frameHeight - 120);
            },
            openDetailWindow:function () {
                var me = this;
                var checkData = $("#Config-draftG").grid("getSelection");
                if (checkData != null && checkData != "" && checkData != undefined){
                    //根据业务流水号判断电路是否在调度中走过流程
                    var param = {};
                    param.instanceId = checkData.prodInstId;
                    param.srvOrdId = checkData.crmOrderCode;
                    localOrderSelectAction.queryCountByInstanceId(param, function(res){
                        if (res.success){
                            if (res.data > 0) { //在调度走过流程
                                //查询详情页需要的数据
                                localOrderSelectAction.queryCircuitInfoBySrvOrdId(param,function(resData){
                                    if (resData.success){
                                        var options = {
                                            url: 'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/views/circuitCodeInfoView',
                                            height: '99%',
                                            width: "95%",
                                            modal: false,
                                            draggable: true,
                                            resizable:true,
                                            autoResizable: true,
                                            viewOption: {
                                                // flag : "org",
                                                selrow : resData.data
                                            },
                                            callback: function (popup, view) {
                                                popup.result.then(function (res) {

                                                }, function (e) {
                                                    console.log('关闭了', e);
                                                });
                                            }
                                        };
                                        var popup = fish.popupView(options);
                                    }
                                });

                            }else{ //没有在调度走过流程
                                //先查询存量资源接口，有数据弹出页面，没有数据给予提示，不弹新的页面
                                me.queryRouteInfo();
                            }
                        }
                    });
                }
            },
            exportExcel :function(){
                var me = this;
                if ($("#circuitCode").val() == '' && $("#accNbr").val() == '') {
                    fish.info("电路编号和业务号码不能同时为空！");
                    return;
                }
                if ($("#productType").val() == '' ) {
                    fish.info("产品类型为必填项，不能为空！");
                    return;
                }
                var param = {};
                param.pageIndex = 1;
                param.pageSize = 10000;
                param.circuitCode = $("#circuitCode").val();//电路编号
                param.productType = $("#productType").val();//产品类型
                param.accNbr = $("#accNbr").val();//业务号码
                param.isToBeQuery = '0';// 只查询
                localOrderSelectAction.exportStockCircuitInfo(me.getRootPath() + '/localScheduleLT/unicomLocalOrder/exportStockCircuitInfo.spr', param);
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
                var checkData = $("#Config-draftG").grid("getSelection");
                var param = {};
                param.businessNum = checkData.accNbr;
                param.CIRCUIT_NO = checkData.circuitCode;
                param.regionId = checkData.regionId;
                localOrderSelectAction.queryRouteInfo(param,function(res){
                    if (res.success){
                        fish.popupView({
                            url: 'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/views/routingInfoView',
                            height: '99%',
                            width: "95%",
                            modal: false,
                            draggable: true,
                            resizable:true,
                            autoResizable: true,
                            viewOption: {
                                res:res
                            },
                            callback: function (popup, view) {
                                popup.result.then(function (res) {

                                }, function (e) {
                                    console.log('关闭了', e);
                                });
                            }
                        });
                        // //填充grid
                        // $('#routeGrid').grid("reloadData", res.souteInfo);
                        // //填充form
                        // me.insertPropertyForm(res.propertyInfo);
                    }else{
                        fish.info(res.msg);
                    }
                });
            },

        });
    });