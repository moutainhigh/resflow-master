/**
 * 订单详情主JS
 */
define([
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/orderDetailsAction',
    'text!module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/templates/circuitCodeInfoView.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/orderDetailsView.css'
], function(orderDetailsAction,orderDetails,i18n,css) {

    var selectTab = 'circuit';//tabl标识
    var changeOrderLabel = "4B";//异常单子标识
    var serviceids ;
    var srvordIds ;
    var URl;
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
            this.tabsCircuitSub();
            //海南、重庆暂时屏蔽二干资源配置
            if('350002000000000042766427' == this.userInfo.areaId
                || '350002000000000042766429' == this.userInfo.areaId){
                $("#tabs-resourceOrder").remove();
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
        //查询二干资源信息的方法
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
    });
}); //ALL END