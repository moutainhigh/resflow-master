define(["text!module/UnicomLocalNet/resmaster/portal/resourceInitiate/templates/resSupToDoView.html",
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        'module/UnicomLocalNet/resmaster/portal/resourceInitiate/action/resourceInitiateAction',
        'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
        "css!module/UnicomLocalNet/resmaster/portal/orderLocalStandby/styles/localStandby.css"],
    function (template, portalViewi18n, resourceInitiateAction, orderStandbyAction, css) {
    return ngc.View.extend({
        queryTypeRes : "waitSignFor",
        staffId: "",
        queryObj: new Object(),

        userInfo: new Object(),
        resNetworkUrl: '',
        crmRegion: '',
        initTab:this.dialogArguments._src ? this.dialogArguments._src : "",
        standbyType:'',
        template: ngc.compile(template),
        i18nData: ngc.extend({}, portalViewi18n),
        events: {
            'click #queryOrder': 'initQueryBut',
            'click #tabs-a-a-link': 'waitSignForClick',      //待签收
            'click #tabs-a-b-link': 'dealWithClick',      //处理中
            'click #tabs-a-c-link': 'completedClick',      //已完成
            'click #touchGet': 'touchGet',
            'click #releaseTouchGet': 'releaseTouchGet',
            'click #collapsible': 'collapsible',
            'click #exportExcel': 'exportExcel',
            'click #resetSelect': 'resetSelect'
            // 'click #fileupload': 'attachFileupload',

        },
        initialize: function () {
            this.render();
            // this.workOrderState = "";

        },
        render: function () {           //渲染页面
            this.$el.html(this.template(this.i18nData));
        },
        //初始化fish组件
        afterRender: function () {
            /*this.showActionButtons = "";
            // debugger
            this.userInfo = resourceInitiateAction.queryStaffInfo().responseJSON.data;
            crmRegionMap = resourceInitiateAction.qryMsmSwitchByArea(this.userInfo.areaId).responseJSON.data;
            if(crmRegionMap !=''
                && crmRegionMap != undefined){
                this.crmRegion = crmRegionMap.CRM_REGION;
            }
            this.resNetworkUrl = resourceInitiateAction.qryInterfaceUrl('ResourceNetWork').responseJSON.data;*/
            this.staffId = this.userInfo.userId;
            /*$('#srvOrdId').clearinput();*/
            /*this.rUrl = this.getRootPath();
            this.URl = this.getRootPath();*/
            //初始化tab
            $("#orderDeal-tab").tabs();
            $("#tabs").tabs();
            //初始化表格
            this.initorderDealGridGroup();
            //固定tab页
            this.initClick();
            //初始化下拉框
            this.initCombobox();
        },
        initClick : function () {
            if (this.initTab != "" && this.initTab != null) {
                standbyType = [ 'waitSignFor', 'dealWith', 'completed'];
                $('#' + this.initTab).click();
            }else {
                standbyType = [];
                //默认选中待接单区
                $('#tabs-a-a-link').click();
            }
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

        getRootPath: function () {
            //获取当前网址，如： http://localhost:8083/uimcardprj/share/meun.jsp
            var curWwwPath = window.document.location.href;
            //获取主机地址之后的目录，如： uimcardprj/share/meun.jsp
            var pathName = window.document.location.pathname;
            var pos = curWwwPath.indexOf(pathName);
            //获取主机地址，如： http://localhost:8083
            var localhostPaht = curWwwPath.substring(0, pos);
            //获取带"/"的项目名，如：/uimcardprj
            var projectName = pathName.substring(0, pathName.substr(1).indexOf('/') + 1);
            return (localhostPaht + projectName);
        },

        headerField : function () {
            return [
                {name: 'CIRCUIT_CODE', label: '电路编号', width: 120, sortable: false },
                {name: 'SERIAL_NUMBER', label: '业务号码', width: 120, sortable: false },
                {name: 'CODE_CONTENT', label: '产品类型', width: 120, sortable: false },
                {name: 'PUB_DATE_NAME', label: '专业', width: 120, sortable: false },
                {name: 'TACHE_NAME', label: '环节', width: 120, sortable: false },
                {name: 'INSTANCE_ID', label: '实例号', width: 120, sortable: false }
            ];
        },

        initorderDealGridGroup: function () {
            var me = this;
            var queryWorkOrdersGroup = $.proxy(this.queryWorkOrdersGroup, this); //函数作用域改变
            $("#orderDeal-grid").grid({
                datatype: "json",
                colModel: me.headerField(),
                autowidth: true,
                curPageSort: true,
                rowNum: 10,
                rowList: [10, 15, 20, 50, 100, 200, 500],
                pager: true,
                recordtext: "{0}-{1} 共{2}条",
                pgtext: " 第{0}页/共{1}页",
                rowtext: "每页{0}条",
                gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                multiselect: true,
                shrinkToFit: false,
                autoResizable: true,
                cached: true, //把用户自定义的列展示设置缓存在本地
                pageData: queryWorkOrdersGroup,
                onDblClickRow: function (e, rowid, iRow, iCol) {//双击行事件
                    var dataParent = $("#orderDeal-grid").grid('getRowData', rowid);
                    me.orderDetailViewGroup(dataParent);
                }
            });
            this.resize();
        },

        //查询工单方法
        queryWorkOrdersGroup: function (page, rowNum) {
            //获取表单信息
            var formValue = $('#resSupToDo-Qryform').form("value");
            var rowNum = (rowNum != '' && rowNum != undefined) ? rowNum : 10;
            var page = (page != '' && page != undefined) ? page : 1;
            fish.store.set('orderDeal-grid-rowNum', rowNum); //记录用户选择的每页记录数
            var qryParams = {};
            qryParams.pageIndex = page + '';
            qryParams.pageSize = rowNum + '';
            qryParams.queryTypeRes = queryTypeRes;
            qryParams.circuitCode = formValue.circuitCode;
            qryParams.serialNumber = formValue.serialNumber;
            qryParams.productType = formValue.productType;
            //$.blockUI();
            //调用后台方法
            this.resSupOrdCount(qryParams);
            this.standbyOrderInfoGroup(qryParams);
            this.resize();
        },

        resSupOrdCount: function (qryParams) {
            resourceInitiateAction.qryResSupOrdCount(qryParams, function (datas) {
                if (datas.resultFlag) {
                    $('#waitSignFor').text(datas.counts.waitSignFor);
                    $('#dealWith').text(datas.counts.dealWith);
                    $('#completed').text(datas.counts.completed);
                }else{
                    fish.toast("error", "获取数据失败:"+datas.message);
                }
            });
        },

        standbyOrderInfoGroup: function (qryParams) {
            resourceInitiateAction.qryResSupOrdList(qryParams, function (res) {
                if (res.resultFlag) {
                    switch (res.queryTypeRes) {
                        case 'waitSignFor':
                            $('#waitSignFor').html(res.records);
                            break;
                        case 'dealWith':
                            $('#dealWith').html(res.records);
                            break;
                        case 'completed':
                            $('#completed').html(res.records);
                            break;
                    }
                    $("#orderDeal-grid").grid("reloadData", res);
                    $.unblockUI();
                } else {
                    fish.toast("error", "获取数据失败");
                }
            })
        },

        waitSignForClick: function () {   //个人待办
            $("#touchGet").show();
            $("#releaseTouchGet").hide();
            queryTypeRes = 'waitSignFor';
            this.queryWorkOrdersGroup();
        },
        dealWithClick: function () {  //处理中
            $("#touchGet").hide();
            $("#releaseTouchGet").show();
            queryTypeRes = 'dealWith';
            this.queryWorkOrdersGroup();
        },
        completedClick: function () { //已完成
            $("#touchGet").hide();
            $("#releaseTouchGet").hide();
            queryTypeRes = 'completed';
            this.queryWorkOrdersGroup();
        },

        initQueryBut: function () { //查询
            var me = this;
            switch (queryTypeRes) {
                case 'waitSignFor':
                    this.waitSignForClick();
                    break;
                case 'dealWith':
                    this.dealWithClick();
                    break;
                case 'completed':
                    this.completedClick();
                    break;
            }
        },
        orderDetailViewGroup: function (dataParent) { //详情页面
            var me = this;
            if (dataParent.IS_OLDRES == '1'){ //存量数据js
                var param = {};
                param.businessNum = dataParent.SERIAL_NUMBER;
                param.CIRCUIT_NO = dataParent.CIRCUIT_CODE;
                param.regionId = dataParent.REGION_ID; //'000102000000000000005893';
                me.queryRouteInfo(param, dataParent);
            }else { // if(dataParent.IS_OLDRES == '0')
                var toUrl = 'module/UnicomLocalNet/resmaster/portal/resourceInitiate/views/circuitDetailsInfoView';//调度数据js
                var viewOption = {}; //详情传递参数
                var srvOrdId = resourceInitiateAction.qrySrvOrdIdByInstanceId(dataParent.INSTANCE_ID,dataParent.SYSTEM_RESOURCE).responseJSON.data;
                var selrow = {};
                selrow.SRVORDID = srvOrdId;
                selrow.SERVICEID = dataParent.SERVICE_ID;
                selrow.dealFlag = queryTypeRes;
                viewOption.selrow = selrow;
                viewOption.woId = dataParent.WO_ID;
                viewOption.orderId = dataParent.ORDER_ID;
                viewOption.tacheId = dataParent.TACHEID;
                viewOption.id = dataParent.ID;
                var pop = fish.popupView({
                    url: toUrl,
                    width: "99%",
                    height: "95%",
                    title: "工单详情",
                    modal: false,
                    viewOption: viewOption,
                    callback: function (popup, view) {
                        popup.result.then(function (e) {
                            me.queryWorkOrdersGroup();
                        }, function (e) {
                            console.log('关闭了', e);
                        });
                    }
                })
            }
        },

        queryRouteInfo:function(param, dataParent){
            var me = this;
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
                            res : res,
                            dealFlag : queryTypeRes,
                            woId : dataParent.WO_ID,
                            orderId : dataParent.ORDER_ID,
                            tacheId : dataParent.TACHEID,
                            circuitCode : dataParent.CIRCUIT_CODE,
                            productType : dataParent.SERVICE_ID,
                            accNbr : dataParent.SERIAL_NUMBER,
                            id : dataParent.ID
                        },
                        callback: function (popup, view) {
                            popup.result.then(function (res) {
                                me.queryWorkOrdersGroup();
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


        resetSelect: function () {
            $("#orderDeal-grid").grid("setAllCheckRows", false);
        },
        //浏览器窗口大小改变事件
        resize: function () {
            var frameHeight = $(parent.window).height();
            $("#orderDeal-grid").grid("resize", true);
            $("#orderDeal-grid").grid("setGridHeight", frameHeight - 365);
        },

        touchGet: function () {        //签收
            var woOrderIds = new Array()
            var queryParams = new Object();
            var selarrowJson = {};//存在已遍历过的wo_id
            var selarrrow = $("#orderDeal-grid").grid("getCheckRows");
            $.each(selarrrow, function (p) {
                // console.log('关闭了'+p);
                var seP = selarrrow[p];
                if (seP == undefined) {
                    return true;
                }
                var seWoIds = seP.WO_ID;
                if (seWoIds != ''
                    && seWoIds != undefined) {
                    woOrderIds.push(seWoIds);
                }
            });
            // debugger;
            if (woOrderIds.length <= 0) {
                fish.warn("请选择一条或多条数据");
                return;
            }
            var me = this;
            queryParams.woOrderIds = woOrderIds;
            queryParams.actionType = 'get';
            // debugger;
            orderStandbyAction.getFreeWoOrder(queryParams, function (data) {
                if (data.success) {
                    var params = {};
                    var formValue = $('#resSupToDo-Qryform').form("value");
                    params.circuitCode = formValue.circuitCode;
                    params.serialNumber = formValue.serialNumber;
                    params.productType = formValue.productType;
                    params.queryTypeRes = queryTypeRes;
                    me.standbyOrderInfoGroup(params);
                    me.resSupOrdCount(params);
                    fish.toast("warn", data.message);
                } else {
                    fish.toast("warn", data.message);
                }
            });

        },

        releaseTouchGet: function () {//释放签收
            var woOrderIds = new Array()
            var queryParams = new Object();
            var selarrowJson = {};//存在已遍历过的wo_id
            var selarrrow = $("#orderDeal-grid").grid("getCheckRows");
            $.each(selarrrow, function (p) {
                var seP = selarrrow[p];
                if (seP == undefined) {
                    return true;
                }
                var seWoIds = seP.WO_ID;
                if (seWoIds != ''
                    && seWoIds != undefined) {
                    woOrderIds.push(seWoIds);
                }
            });
            // debugger;
            if (woOrderIds.length <= 0) {
                fish.warn("请选择一条或多条数据");
                return;
            }
            var me = this;
            queryParams.woOrderIds = woOrderIds;
            queryParams.actionType = 'free';
            // debugger;
            orderStandbyAction.getFreeWoOrder(queryParams, function (data) {
                if (data.success) {
                    var params = {};
                    var formValue = $('#resSupToDo-Qryform').form("value");
                    params.circuitCode = formValue.circuitCode;
                    params.serialNumber = formValue.serialNumber;
                    params.productType = formValue.productType;
                    params.queryTypeRes = queryTypeRes;
                    me.standbyOrderInfoGroup(params);
                    me.resSupOrdCount(params);
                    fish.toast("warn", data.message);
                } else {
                    fish.toast("warn", data.message);
                }
            });
        },
        standbyOrderInfo: function () {
            resourceInitiateAction.qryCstOrdList(this.queryObj, function (data) {
                if (data.messages = "success") {
                    switch (this.queryObj.queryTypeLocal) {
                        case 'deptStandny':
                            $('#deptStandny').text(data.data.length);
                            break;
                        case 'jobStandby':
                            $('#jobStandby').text(data.data.length);
                            break;
                        case 'staffStandby':
                            $('#staffStandby').text(data.data.length);
                            break;
                        case 'dealOrder':
                            $('#dealOrder').text(data.data.length);
                            break;
                        case 'dispConfirm': //完成确认单
                            $('#dispConfirm').text(data.data.length);
                            break;
                        case 'ccOrder':
                            $('#ccOrder').text(data.data.length);
                            break;
                    }
                } else {
                    fish.toast("error", "获取数据失败");
                }
                $("#orderDeal-grid").grid("reloadData", data.data);
            })
        }

    }); //fish.View.extend END
}); //ALL END