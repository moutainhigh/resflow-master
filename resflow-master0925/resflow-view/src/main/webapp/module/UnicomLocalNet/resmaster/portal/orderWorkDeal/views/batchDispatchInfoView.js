define(['module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/operOrderAction',
        'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/orderDetailsAction',
        'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
        'text!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/templates/bactchDispatchInfoView.Html',
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/operOrderView.css'
    ],
    function (operOrderAction, orderDetailsAction, orderStandbyAction, bactchDispatchInfoView,  i18n) {
        var userInfo = orderStandbyAction.queryStaffInfo().responseJSON.data;
        return fish.View.extend({
            template: fish.compile(bactchDispatchInfoView),
            i18nData: fish.extend({}, i18n),
            events: {
                'click #saveDraft': 'saveDraft',
                'click #submitBtn': 'submitBtn',
            },
            initialize: function () {
                this.render();
            },
            render: function () {
                this.$el.html(this.template(this.i18nData));
            },
            afterRender: function () {
                //初始化表格
                this.initGrid();
                //初始化主/辅调局
                this.initRegionInfo();
                //初始化数据制作
                this.initNetManage();
                //初始化专业信息
                this.initResourceSpecialty();
                $("#masterComplateTime").datetimepicker({
                    viewType: "datetime",
                    format:'yyyy-mm-dd hh:ii:ss',
                    todayBtn: true
                });
                $("#slaveComplateTime").datetimepicker({
                    viewType: "datetime",
                    format: 'yyyy-mm-dd hh:ii:ss',
                    todayBtn: true
                });
                $('#resAllocateChild').combobox({
                    placeholder: '请选择...',
                    dataTextField: 'name',
                    dataValueField: 'value',
                    dataSource: [
                        {name: '是', value: '是'},
                        {name: '否', value: '否'}
                    ],
                });
                $('#isAssignPersonChild').combobox({
                    placeholder: '请选择...',
                    dataTextField: 'name',
                    dataValueField: 'value',
                    dataSource: [
                        {name: '是', value: '是'},
                        {name: '否', value: '否'}
                    ],
                });
                $('#isResAssChild').combobox({
                    placeholder: '请选择...',
                    dataTextField: 'name',
                    dataValueField: 'value',
                    dataSource: [
                        {name: '是', value: '是'},
                        {name: '否', value: '否'}
                    ],
                });
                $('#isComplatePersonChild').combobox({
                    placeholder: '请选择...',
                    dataTextField: 'name',
                    dataValueField: 'value',
                    dataSource: [
                        {name: '是', value: '是'},
                        {name: '否', value: '否'}
                    ],
                });
                //给是否转资源分配赋默认值
                $('#resAllocateChild').combobox('value', '是');
                //给数据制作是否指定到人赋默认值
                $('#isAssignPersonChild').combobox('value', '否');
                //给资源分配是否指定到人赋默认值
                $('#isResAssChild').combobox('value', '否');
                //给完工汇总是否指定到人赋默认值
                $('#isComplatePersonChild').combobox('value', '是');
                //是否转资源分配下拉框变更事件
                $('#resAllocateChild').on('combobox:change', function () {
                    var resVal = $('#resAllocateChild').val();
                    if (resVal == '是'){
                        $('.resourceSpecialty').show();
                        var resVal = $('#isResAssChild').val();
                        if (resVal == '否'){
                            $('.resAssign').hide();
                        }else if (resVal == "是"){
                            //先将所有的专业指定人隐藏
                            $('.resAssign').hide();
                            //获取资源分配专业
                            var specialtyInfo = $("#resourceSpecialtyChild").val();
                            if (specialtyInfo != null && specialtyInfo != ''){
                                for (var i = 0; i < specialtyInfo.length; i++){
                                    $("." + specialtyInfo[i] + 'PersonChild').show();
                                }
                            }
                        }
                    }else if (resVal == '否'){
                        $('.resourceDiv').hide();
                    }
                });
                //资源分配专业选择事件
                $('#resourceSpecialtyChild').on('multiselect:change', function(){
                    var resVal = $('#isResAssChild').val();
                    if (resVal == '是'){
                        //先将所有的专业指定人隐藏
                        $('.resAssign').hide();
                        //获取资源分配专业
                        var specialtyInfo = $("#resourceSpecialtyChild").val();
                        if (specialtyInfo != null && specialtyInfo != ''){
                            for (var i = 0; i < specialtyInfo.length; i++){
                                $("." + specialtyInfo[i] + 'PersonChild').show();
                            }
                        }
                    }
                });
                //资源分配是否指定到人
                $('#isResAssChild').on('combobox:change', function () {
                    var resVal = $('#isResAssChild').val();
                    if (resVal == '是'){
                        //获取资源分配专业
                        var specialtyInfo = $("#resourceSpecialtyChild").val();
                        if (specialtyInfo != null && specialtyInfo != ''){
                            for (var i = 0; i < specialtyInfo.length; i++){
                                $("." + specialtyInfo[i] + 'PersonChild').show();
                            }
                        }
                    }else if (resVal == '否'){
                        $('.resAssign').hide();
                    }
                });
                //数据制作选择专业事件
                $('#netManageChild').on('multiselect:change',function(e, params){
                    var resVal = $('#isAssignPersonChild').val();
                    if (resVal == '是'){
                        //现将所有的专业指定人隐藏
                        $('.dataMkName').hide();
                        var dataMkInfo = $("#netManageChild").val();
                        if (dataMkInfo != null && dataMkInfo != ''){
                            for (var i = 0; i < dataMkInfo.length; i++){
                                $("." + dataMkInfo[i] + 'NameChild').show();
                            }
                        }
                    }
                });
                //数据制作是否指定到人
                $('#isAssignPersonChild').on('combobox:change', function () {
                    var resVal = $('#isAssignPersonChild').val();
                    if (resVal == '是'){
                        var dataMkInfo = $("#netManageChild").val();
                        if (dataMkInfo != null && dataMkInfo != ''){
                            for (var i = 0; i < dataMkInfo.length; i++){
                                $("." + dataMkInfo[i] + 'NameChild').show();
                            }
                        }
                    }else if (resVal == '否'){
                        $('.dataMkName').hide();
                    }
                });
                //完工汇总是否指定到人
                $('#isComplatePersonChild').on('combobox:change', function () {
                    var resVal = $('#isComplatePersonChild').val();
                    if (resVal == '是'){
                        $('.isComplatePerson').show();
                    }else if (resVal == '否'){
                        $('.isComplatePerson').hide();
                    }
                });
                $(".resRadio").click(function () {
                    var val = $("input:radio[name='resRadio']:checked").val();
                    if (val == '是'){
                        $('.toLocal').show();
                    }else if(val = '否'){
                        //隐藏主辅调局信息
                        $('.toLocal').hide();
                    }
                });
            },
            initGrid: function () {
                var me = this;
                $("#batchDealDispatchInfo-grid").grid({
                    colModel: [
                        {name: 'CST_ORD_ID', label: '客户Id', width: 140, hidden: true},
                        {name: 'SRV_ORD_ID', label: '业务订单信息Id', width: 140, hidden: true},
                        {name: 'ORDER_ID', label: '流程订单Id', width: 140, hidden: true},
                        {name: 'WO_ID', label: '工单Id', width: 140, hidden: true},
                        {name: 'TACHE_ID', label: '环节Id', width: 140, hidden: true},
                        {name: 'TACHE_CODE', label: '环节code', width: 140, hidden: true},
                        {name: 'SERVICE_ID', label: '产品类型Id', width: 140, hidden: true},
                        {name: 'ORDER_TYPE', label: '订单类型', width: 140, hidden: true},
                        {name: 'SUBSCRIBE_ID', label: '客户订单号', width: 140, hidden: true},
                        {name: 'TRADE_ID', label: '业务订单号', width: 140},
                        {name: 'SERIAL_NUMBER', label: '业务号码', width: 140},
                        {name: 'CIRCUITCODE', label: '电路编号', width: 140, sortable: false},
                        {name: 'CIRCUIT_REQ_NAME', label: '电路要求', width: 140, sortable: false},
                        {name: 'SPEED', label: '电路带宽', width: 140, sortable: false},
                        {name: 'SERVICENAME', label: '产品类型', width: 140, hidden: true},
                        {name: 'ONE_GROUP_ROUTE', label: '一干群次路由', align: 'left', width: 140},
                        {name: 'ONE_ALL_ROUTE', label: '一干全程路由', align: 'left', width: 140},
                        {name: 'SECOND_GROUP_ROUTE', label: '二干群次路由', align: 'left', width: 140},
                        {name: 'SECOND_ALL_ROUTE', label: '二干全程路由', align: 'left', width: 140},
                        {name: 'ALL_REQ_TIME', label: '全程要求完成时间', width: 140},
                        {name: 'AREGIONNAME', label: 'A端所属区域', width: 140, hidden: true},
                        {name: 'AREGIONNAME_ID', label: 'A端所属区域', width: 140, hidden: true},
                        {name: 'ZREGIONNAME', label: 'Z端所属区域', width: 140, hidden: true},
                        {name: 'ZREGIONNAME_ID', label: 'Z端所属区域', width: 140, hidden: true},
                        {name: 'A_CITY', label: 'A端城市', width: 140, sortable: false},
                        {name: 'A_CITY_ID', label: 'A端城市', width: 140, hidden:true},
                        {name: 'A_INTERFACE_TYPE', label: 'A端接口类型', width: 140, sortable: false, hidden: true},
                        {name: 'A_INTERFACE_TYPE_NAME', label: 'A端接口类型', width: 140, sortable: false},
                        {name: 'A_CUSTOMER_NAME', label: 'A端客户名称', width: 140, sortable: false},
                        {name: 'A_INSTALLED_ADD', label: 'A端装机地址', width: 140, sortable: false},
                        {name: 'A_REQ_TIME', label: 'A端要求完成时间', width: 140, sortable: false},
                        {name: 'Z_CITY', label: 'Z端城市', width: 140, sortable: false},
                        {name: 'Z_CITY_ID', label: 'Z端城市', width: 140, hidden: true},
                        {name: 'Z_INTERFACE_TYPE', label: 'Z端接口类型', width: 140, sortable: false, hidden: true},
                        {name: 'Z_INTERFACE_TYPE_NAME', label: 'Z端接口类型', width: 140, sortable: false},
                        {name: 'Z_CUSTOMER_NAME', label: 'Z端客户名称', width: 140, sortable: false},
                        {name: 'Z_INSTALLED_ADD', label: 'Z端装机地址', width: 140, sortable: false},
                        {name: 'Z_REQ_TIME', label: 'Z端要求完成时间', width: 140, sortable: false}
                    ],
                    autowidth: true,
                    multiselect: true,
                    shrinkToFit: false, //表格列宽是否按比例缩放，默认true
                    pageData: me.queryData(),
                });
                $("#batchDealDispatchInfo-grid").grid("setGridHeight", 160);
            },
            queryData: function () {
                var me = this;
                var param = {};
                param.onedry = me.options.onedry;
                param.cstOrdId = me.options.cstOrdId;
                param.tacheId = me.options.tacheId;
                param.specialtyCode = me.options.specialtyCode;
                param.woState = me.options.woState;
                param.woIds = me.options.woIds;
                param.reginonId = me.options.reginonId;
                param.dealUserId = me.options.dealUserId;
                param.compUserId = me.options.compUserId;
                param.dispType = me.options.dispType;
                param.staffId = me.options.staffId;
                param.dispObjTyeValue = me.options.dispObjTyeValue;
                param.dispObjTye = me.options.dispObjTye;
                param.orderIdSelect = me.options.orderIdSelect;//工单查询orderId
                $('#batchDealDispatchInfo-grid').blockUI();
                orderDetailsAction.queryCircuitInfoDraftGrid(param, function (res) {
                    $('#batchDealDispatchInfo-grid').unblockUI();
                    if (res.flag == 1) {
                        $("#batchDealDispatchInfo-grid").grid("reloadData", res.data);
                    }
                });
            },
            initRegionInfo : function(){
                var replenish = this.options.replenish;
                var disableAssist = this.options.disableAssist;
                var me = this;
                //初始化主调局
                $("#masterRegionName").popedit({
                    initialData: {
                        'name': '--请选择主调局--',
                        'value': ''
                    },
                    open: function (e) {
                        var _this = $(this);
                        var options = {
                            url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/masterTreeView',
                            height: 350,
                            width: 400,
                            modal: false,
                            draggable: false,
                            autoResizable: true,
                            viewOption: {
                                type: "schedule",
                                orderId: me.options.orderId,
                                key: "MASTER",
                                circuitInfo : me.options.oldOrgInfo //电路信息
                            },
                            callback: function (popup, view) {
                                popup.result.then(function (res) {
                                    var orgNames = '';
                                    var orgIds = '';
                                    var nodeArray = new Array;
                                    res.forEach(function (val, i) {
                                        var nodeSin = new Object();
                                        nodeSin.id = val.id;
                                        nodeArray.push(nodeSin);
                                        if (!val.isParent) {
                                            if (orgIds == '') {
                                                orgNames = val.name;
                                                orgIds = val.id;
                                            } else {
                                                orgNames = orgNames + ',' + val.name;
                                                orgIds = orgIds + ',' + val.id;
                                            }
                                        }
                                    });
                                    _this.popedit('setValue', {name: orgNames, value: orgIds});
                                }, function (e) {
                                    console.log('关闭了', e);
                                });
                            }
                        };
                        var popup = fish.popupView(options);
                    }
                });
                //初始化辅调局
                $("#slaveRegionName").popedit({
                    showClearIcon:false, //不显示x按钮----好像没啥作用，下面手动销毁了
                    initialData: {
                        'name': '--请选择辅调局--',
                        'value': ''
                    },
                    open: function (e) {
                        var masterRegion = $("#masterRegionName").popedit('getValue').value;
                        var areaPro = {};
                        var _this = $(this);
                        var _array = new Array;
                        var _value = $("#slaveRegionName").popedit('getValue').value;
                        if (_value != null) {
                            _array = _value.split(",");
                        }
                        var options = {
                            url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/operTreeOrderView',
                            height: 350,
                            width: 400,
                            modal: false,
                            draggable: false,
                            autoResizable: true,
                            viewOption: {
                                flag: "org",
                                orderId: me.options.orderId,
                                nodeValues: _array,
                                masterRegion:masterRegion,
                                oldRegion: '', //补单，主流程在数据制作环节原辅调不可删
                                circuitInfo : "" //电路信息
                            },
                            callback: function (popup, view) {
                                popup.result.then(function (res) {
                                    var orgNames = '';
                                    var orgIds = '';
                                    var nodeArray = new Array;
                                    res.forEach(function (val, i) {
                                        var nodeSin = new Object();
                                        nodeSin.id = val.id;
                                        nodeArray.push(nodeSin);
                                        if (!val.isParent) {
                                            if (orgIds == '') {
                                                orgNames = val.name;
                                                orgIds = val.id;
                                            } else {
                                                orgNames = orgNames + ',' + val.name;
                                                orgIds = orgIds + ',' + val.id;
                                            }
                                        }
                                    })
                                    _this.popedit('setValue', {name: orgNames, value: orgIds});
                                }, function (e) {
                                    console.log('关闭了', e);
                                });
                            }
                        };
                        var popup = fish.popupView(options);
                    }
                });
                //初始化数据专业的数据制作指定人
                $("#DATA_4NameChild").popedit({
                    initialData: {
                        'name': '--请选择数据专业的数据制作指定人--',
                        'value': ''
                    },
                    open: function(){
                        var _this = $(this);
                        var options = {
                            url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/assignPersonView',
                            height: 450,
                            width: 700,
                            modal: false,
                            draggable: false,
                            autoResizable: true,
                            viewOption: {
                                flag : "transferStaff",
                                currentAreaId : userInfo.areaId,
                                currentOrgId : userInfo.orgId,
                                currentUserId : userInfo.userId
                            },
                            callback: function (popup, view) {
                                popup.result.then(function (res) {
                                    var orgNames = '';
                                    var orgIds = '';
                                    var nodeArray = new Array;
                                    res.forEach(function (val, i) {
                                        var nodeSin = new Object();
                                        nodeSin.id = val.id;
                                        nodeArray.push(nodeSin);
                                        if (!val.isParent) {
                                            if (orgIds == '') {
                                                orgNames = val.name;
                                                orgIds = val.id;
                                            } else {
                                                orgNames = orgNames + ',' + val.name;
                                                orgIds = orgIds + ',' + val.id;
                                            }
                                        }
                                    });
                                    _this.popedit('setValue', {name: orgNames, value: orgIds});
                                }, function (e) {
                                    console.log('关闭了', e);
                                });
                            }
                        };
                        var popup = fish.popupView(options);
                    }
                });
                //初始化传输专业的数据制作指定人
                $("#TRANS_3NameChild").popedit({
                    initialData: {
                        'name': '--请选择传输专业的数据制作指定人--',
                        'value': ''
                    },
                    open: function(){
                        var _this = $(this);
                        var options = {
                            url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/assignPersonView',
                            height: 450,
                            width: 700,
                            modal: false,
                            draggable: false,
                            autoResizable: true,
                            viewOption: {
                                flag : "transferStaff",
                                currentAreaId : userInfo.areaId,
                                currentOrgId : userInfo.orgId,
                                currentUserId : userInfo.userId
                            },
                            callback: function (popup, view) {
                                popup.result.then(function (res) {
                                    var orgNames = '';
                                    var orgIds = '';
                                    var nodeArray = new Array;
                                    res.forEach(function (val, i) {
                                        var nodeSin = new Object();
                                        nodeSin.id = val.id;
                                        nodeArray.push(nodeSin);
                                        if (!val.isParent) {
                                            if (orgIds == '') {
                                                orgNames = val.name;
                                                orgIds = val.id;
                                            } else {
                                                orgNames = orgNames + ',' + val.name;
                                                orgIds = orgIds + ',' + val.id;
                                            }
                                        }
                                    });
                                    _this.popedit('setValue', {name: orgNames, value: orgIds});
                                }, function (e) {
                                    console.log('关闭了', e);
                                });
                            }
                        };
                        var popup = fish.popupView(options);
                    }
                });
                //初始化交换专业的数据制作指定人
                $("#EXCHANGE_5NameChild").popedit({
                    initialData: {
                        'name': '--请选择交换专业的数据制作指定人--',
                        'value': ''
                    },
                    open: function(){
                        var _this = $(this);
                        var options = {
                            url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/assignPersonView',
                            height: 450,
                            width: 700,
                            modal: false,
                            draggable: false,
                            autoResizable: true,
                            viewOption: {
                                flag : "transferStaff",
                                currentAreaId : userInfo.areaId,
                                currentOrgId : userInfo.orgId,
                                currentUserId : userInfo.userId
                            },
                            callback: function (popup, view) {
                                popup.result.then(function (res) {
                                    var orgNames = '';
                                    var orgIds = '';
                                    var nodeArray = new Array;
                                    res.forEach(function (val, i) {
                                        var nodeSin = new Object();
                                        nodeSin.id = val.id;
                                        nodeArray.push(nodeSin);
                                        if (!val.isParent) {
                                            if (orgIds == '') {
                                                orgNames = val.name;
                                                orgIds = val.id;
                                            } else {
                                                orgNames = orgNames + ',' + val.name;
                                                orgIds = orgIds + ',' + val.id;
                                            }
                                        }
                                    });
                                    _this.popedit('setValue', {name: orgNames, value: orgIds});
                                }, function (e) {
                                    console.log('关闭了', e);
                                });
                            }
                        };
                        var popup = fish.popupView(options);
                    }
                });
                //初始化IP地址数据制作指定人
                $("#IP_15NameChild").popedit({
                    initialData: {
                        'name': '--请选择IP地址的数据制作指定人--',
                        'value': ''
                    },
                    open: function(){
                        var _this = $(this);
                        var options = {
                            url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/assignPersonView',
                            height: 450,
                            width: 700,
                            modal: false,
                            draggable: false,
                            autoResizable: true,
                            viewOption: {
                                flag : "transferStaff",
                                currentAreaId : userInfo.areaId,
                                currentOrgId : userInfo.orgId,
                                currentUserId : userInfo.userId
                            },
                            callback: function (popup, view) {
                                popup.result.then(function (res) {
                                    var orgNames = '';
                                    var orgIds = '';
                                    var nodeArray = new Array;
                                    res.forEach(function (val, i) {
                                        var nodeSin = new Object();
                                        nodeSin.id = val.id;
                                        nodeArray.push(nodeSin);
                                        if (!val.isParent) {
                                            if (orgIds == '') {
                                                orgNames = val.name;
                                                orgIds = val.id;
                                            } else {
                                                orgNames = orgNames + ',' + val.name;
                                                orgIds = orgIds + ',' + val.id;
                                            }
                                        }
                                    });
                                    _this.popedit('setValue', {name: orgNames, value: orgIds});
                                }, function (e) {
                                    console.log('关闭了', e);
                                });
                            }
                        };
                        var popup = fish.popupView(options);
                    }
                });
                //初始化其它专业数据制作指定人
                $("#OTHER_11NameChild").popedit({
                    initialData: {
                        'name': '--请选择其它专业的数据制作指定人--',
                        'value': ''
                    },
                    open: function(){
                        var _this = $(this);
                        var options = {
                            url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/assignPersonView',
                            height: 450,
                            width: 700,
                            modal: false,
                            draggable: false,
                            autoResizable: true,
                            viewOption: {
                                flag : "transferStaff",
                                currentAreaId : userInfo.areaId,
                                currentOrgId : userInfo.orgId,
                                currentUserId : userInfo.userId
                            },
                            callback: function (popup, view) {
                                popup.result.then(function (res) {
                                    var orgNames = '';
                                    var orgIds = '';
                                    var nodeArray = new Array;
                                    res.forEach(function (val, i) {
                                        var nodeSin = new Object();
                                        nodeSin.id = val.id;
                                        nodeArray.push(nodeSin);
                                        if (!val.isParent) {
                                            if (orgIds == '') {
                                                orgNames = val.name;
                                                orgIds = val.id;
                                            } else {
                                                orgNames = orgNames + ',' + val.name;
                                                orgIds = orgIds + ',' + val.id;
                                            }
                                        }
                                    });
                                    _this.popedit('setValue', {name: orgNames, value: orgIds});
                                }, function (e) {
                                    console.log('关闭了', e);
                                });
                            }
                        };
                        var popup = fish.popupView(options);
                    }
                });
                //初始化传输专业资源分配指定人
                $("#TRANS_3PersonChild").popedit({
                    initialData: {
                        'name': '--请选择传输专业资源分配指定人--',
                        'value': ''
                    },
                    open: function(){
                        var _this = $(this);
                        var options = {
                            url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/assignPersonView',
                            height: 460,
                            width: 700,
                            modal: false,
                            draggable: false,
                            autoResizable: true,
                            viewOption: {
                                flag : "transferStaff",
                                currentAreaId : userInfo.areaId,
                                currentOrgId : userInfo.orgId,
                                currentUserId : userInfo.userId
                            },
                            callback: function (popup, view) {
                                popup.result.then(function (res) {
                                    var orgNames = '';
                                    var orgIds = '';
                                    var nodeArray = new Array;
                                    res.forEach(function (val, i) {
                                        var nodeSin = new Object();
                                        nodeSin.id = val.id;
                                        nodeArray.push(nodeSin);
                                        if (!val.isParent) {
                                            if (orgIds == '') {
                                                orgNames = val.name;
                                                orgIds = val.id;
                                            } else {
                                                orgNames = orgNames + ',' + val.name;
                                                orgIds = orgIds + ',' + val.id;
                                            }
                                        }
                                    });
                                    _this.popedit('setValue', {name: orgNames, value: orgIds});
                                }, function (e) {
                                    console.log('关闭了', e);
                                });
                            }
                        };
                        var popup = fish.popupView(options);
                    }
                });
                //数据制作资源分配指定人
                $("#DATA_4PersonChild").popedit({
                    initialData: {
                        'name': '--请选择数据专业资源分配指定人--',
                        'value': ''
                    },
                    open: function(){
                        var _this = $(this);
                        var options = {
                            url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/assignPersonView',
                            height: 460,
                            width: 700,
                            modal: false,
                            draggable: false,
                            autoResizable: true,
                            viewOption: {
                                flag : "transferStaff",
                                currentAreaId : userInfo.areaId,
                                currentOrgId : userInfo.orgId,
                                currentUserId : userInfo.userId
                            },
                            callback: function (popup, view) {
                                popup.result.then(function (res) {
                                    var orgNames = '';
                                    var orgIds = '';
                                    var nodeArray = new Array;
                                    res.forEach(function (val, i) {
                                        var nodeSin = new Object();
                                        nodeSin.id = val.id;
                                        nodeArray.push(nodeSin);
                                        if (!val.isParent) {
                                            if (orgIds == '') {
                                                orgNames = val.name;
                                                orgIds = val.id;
                                            } else {
                                                orgNames = orgNames + ',' + val.name;
                                                orgIds = orgIds + ',' + val.id;
                                            }
                                        }
                                    });
                                    _this.popedit('setValue', {name: orgNames, value: orgIds});
                                }, function (e) {
                                    console.log('关闭了', e);
                                });
                            }
                        };
                        var popup = fish.popupView(options);
                    }
                });
                //初始化交换专业资源分配指定人
                $("#EXCHANGE_5PersonChild").popedit({
                    initialData: {
                        'name': '--请选择交换专业资源分配指定人--',
                        'value': ''
                    },
                    open: function(){
                        var _this = $(this);
                        var options = {
                            url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/assignPersonView',
                            height: 460,
                            width: 700,
                            modal: false,
                            draggable: false,
                            autoResizable: true,
                            viewOption: {
                                flag : "transferStaff",
                                currentAreaId : userInfo.areaId,
                                currentOrgId : userInfo.orgId,
                                currentUserId : userInfo.userId
                            },
                            callback: function (popup, view) {
                                popup.result.then(function (res) {
                                    var orgNames = '';
                                    var orgIds = '';
                                    var nodeArray = new Array;
                                    res.forEach(function (val, i) {
                                        var nodeSin = new Object();
                                        nodeSin.id = val.id;
                                        nodeArray.push(nodeSin);
                                        if (!val.isParent) {
                                            if (orgIds == '') {
                                                orgNames = val.name;
                                                orgIds = val.id;
                                            } else {
                                                orgNames = orgNames + ',' + val.name;
                                                orgIds = orgIds + ',' + val.id;
                                            }
                                        }
                                    });
                                    _this.popedit('setValue', {name: orgNames, value: orgIds});
                                }, function (e) {
                                    console.log('关闭了', e);
                                });
                            }
                        };
                        var popup = fish.popupView(options);
                    }
                });
                //初始化光纤专业资源分配指定人
                $("#OPTICAL_2PersonChild").popedit({
                    initialData: {
                        'name': '--请选择光纤专业资源分配指定人--',
                        'value': ''
                    },
                    open: function(){
                        var _this = $(this);
                        var options = {
                            url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/assignPersonView',
                            height: 460,
                            width: 700,
                            modal: false,
                            draggable: false,
                            autoResizable: true,
                            viewOption: {
                                flag : "transferStaff",
                                currentAreaId : userInfo.areaId,
                                currentOrgId : userInfo.orgId,
                                currentUserId : userInfo.userId
                            },
                            callback: function (popup, view) {
                                popup.result.then(function (res) {
                                    var orgNames = '';
                                    var orgIds = '';
                                    var nodeArray = new Array;
                                    res.forEach(function (val, i) {
                                        var nodeSin = new Object();
                                        nodeSin.id = val.id;
                                        nodeArray.push(nodeSin);
                                        if (!val.isParent) {
                                            if (orgIds == '') {
                                                orgNames = val.name;
                                                orgIds = val.id;
                                            } else {
                                                orgNames = orgNames + ',' + val.name;
                                                orgIds = orgIds + ',' + val.id;
                                            }
                                        }
                                    });
                                    _this.popedit('setValue', {name: orgNames, value: orgIds});
                                }, function (e) {
                                    console.log('关闭了', e);
                                });
                            }
                        };
                        var popup = fish.popupView(options);
                    }
                });
                //初始化IP地址资源分配指定人
                $("#IP_15PersonChild").popedit({
                    initialData: {
                        'name': '--请选择IP地址资源分配指定人--',
                        'value': ''
                    },
                    open: function(){
                        var _this = $(this);
                        var options = {
                            url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/assignPersonView',
                            height: 460,
                            width: 700,
                            modal: false,
                            draggable: false,
                            autoResizable: true,
                            viewOption: {
                                flag : "transferStaff",
                                currentAreaId : userInfo.areaId,
                                currentOrgId : userInfo.orgId,
                                currentUserId : userInfo.userId
                            },
                            callback: function (popup, view) {
                                popup.result.then(function (res) {
                                    var orgNames = '';
                                    var orgIds = '';
                                    var nodeArray = new Array;
                                    res.forEach(function (val, i) {
                                        var nodeSin = new Object();
                                        nodeSin.id = val.id;
                                        nodeArray.push(nodeSin);
                                        if (!val.isParent) {
                                            if (orgIds == '') {
                                                orgNames = val.name;
                                                orgIds = val.id;
                                            } else {
                                                orgNames = orgNames + ',' + val.name;
                                                orgIds = orgIds + ',' + val.id;
                                            }
                                        }
                                    });
                                    _this.popedit('setValue', {name: orgNames, value: orgIds});
                                }, function (e) {
                                    console.log('关闭了', e);
                                });
                            }
                        };
                        var popup = fish.popupView(options);
                    }
                });
                //初始化其它专业资源分配指定人
                $("#OTHER_11PersonChild").popedit({
                    initialData: {
                        'name': '--请选择其它专业资源分配指定人--',
                        'value': ''
                    },
                    open: function(){
                        var _this = $(this);
                        var options = {
                            url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/assignPersonView',
                            height: 460,
                            width: 700,
                            modal: false,
                            draggable: false,
                            autoResizable: true,
                            viewOption: {
                                flag : "transferStaff",
                                currentAreaId : userInfo.areaId,
                                currentOrgId : userInfo.orgId,
                                currentUserId : userInfo.userId
                            },
                            callback: function (popup, view) {
                                popup.result.then(function (res) {
                                    var orgNames = '';
                                    var orgIds = '';
                                    var nodeArray = new Array;
                                    res.forEach(function (val, i) {
                                        var nodeSin = new Object();
                                        nodeSin.id = val.id;
                                        nodeArray.push(nodeSin);
                                        if (!val.isParent) {
                                            if (orgIds == '') {
                                                orgNames = val.name;
                                                orgIds = val.id;
                                            } else {
                                                orgNames = orgNames + ',' + val.name;
                                                orgIds = orgIds + ',' + val.id;
                                            }
                                        }
                                    });
                                    _this.popedit('setValue', {name: orgNames, value: orgIds});
                                }, function (e) {
                                    console.log('关闭了', e);
                                });
                            }
                        };
                        var popup = fish.popupView(options);
                    }
                });
                //初始化完工汇总人员选择
                $("#complatePersonChild").popedit({
                    initialData: {
                        'name': '--请选择完工汇总指定人--',
                        'value': ''
                    },
                    open: function(){
                        var _this = $(this);
                        var options = {
                            url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/assignPersonView',
                            height: 460,
                            width: 700,
                            modal: false,
                            draggable: false,
                            autoResizable: true,
                            viewOption: {
                                flag : "transferStaff",
                                currentAreaId : userInfo.areaId,
                                currentOrgId : userInfo.orgId,
                                currentUserId : userInfo.userId
                            },
                            callback: function (popup, view) {
                                popup.result.then(function (res) {
                                    var orgNames = '';
                                    var orgIds = '';
                                    var nodeArray = new Array;
                                    res.forEach(function (val, i) {
                                        var nodeSin = new Object();
                                        nodeSin.id = val.id;
                                        nodeArray.push(nodeSin);
                                        if (!val.isParent) {
                                            if (orgIds == '') {
                                                orgNames = val.name;
                                                orgIds = val.id;
                                            } else {
                                                orgNames = orgNames + ',' + val.name;
                                                orgIds = orgIds + ',' + val.id;
                                            }
                                        }
                                    });
                                    _this.popedit('setValue', {name: orgNames, value: orgIds});
                                }, function (e) {
                                    console.log('关闭了', e);
                                });
                            }
                        };
                        var popup = fish.popupView(options);
                    }
                });

            },
            //初始化专业信息
            initNetManage:function(){
                var param = {};
                param.proId = "23";
                param.type = "NETMANAGE_TYPE";
                operOrderAction.querySpecNetMag(param,function(res){
                    $('#netManageChild').multiselect('option',{
                        dataTextField:'NAME',
                        dataValueField:'ID',
                        dataSource:res
                    });
                });
            },
            //初始化专业信息
            initResourceSpecialty:function(){
                var param = {};
                param.proId = "23";
                param.type = "SPECIALTY_TYPE";
                operOrderAction.querySpecNetMag(param,function(res){
                    $('#resourceSpecialtyChild').multiselect('option',{
                        dataTextField:'NAME',
                        dataValueField:'ID',
                        dataSource:res
                    });
                });
            },
            //保存页面信息
            submitBtn:function(){
                var me = this;
                var val = $("input:radio[name='resRadio']:checked").val();
                var checkData = $("#batchDealDispatchInfo-grid").grid("getCheckRows");
                if (checkData.length <= 0){
                    fish.info("请勾选要批量处理的数据！");
                    return;
                }else{
                    //派发本地网对主调局和辅调局信息进行校验
                    if (val == '是'){
                        //校验主调局
                        var masterRegion = $("#masterRegionName").popedit('getValue').value;
                        if (masterRegion == null || masterRegion == ''){
                            fish.info("主调局不能为空，请选择！");
                            return;
                        }
                        //校验主调局完成时间
                        var masterComTime = $('#masterComplateTime').val();
                        if (masterComTime == null || masterComTime == ''){
                            fish.info("主调局完成时间不能为空，请选择！");
                            return;
                        }
                        //如果辅调局不为空，则辅调局完成时间不能为空
                        var slaveRegion = $("#slaveRegionName").popedit('getValue').value;
                        if (slaveRegion != null && slaveRegion != ''){
                            var slaveComTime = $('#masterComplateTime').val();
                            if (slaveComTime == null || slaveComTime == ''){
                                fish.info("辅调局不为空，辅调局完成时间也不能为空，请检查！");
                                return;
                            }
                        }
                    }
                    //校验数据制作指定人不为空
                    if ($('#isAssignPersonChild').val() == '是'){
                        var dataMkInfo = $("#netManageChild").val();
                        if (dataMkInfo != null && dataMkInfo != ''){
                            for (var i = 0; i < dataMkInfo.length; i++){
                                var dataMkName = $("#" + dataMkInfo[i] + "NameChild").popedit('getValue');
                                if (dataMkName == null ||  dataMkName == '' || dataMkName.value == null || dataMkName.value == ''){
                                    fish.info("请完成数据制作指定人信息！");
                                    return;
                                }
                            }
                        }
                    }
                    //校验数据制作
                    var netManage = $('#netManageChild').val();
                    if (netManage == null || netManage == ''){
                        fish.info("数据制作不能为空，请选择！");
                        return;
                    }
                    //校验专业信息
                    if($('#resAllocateChild').val() == '是'){
                        var specialty = $('#resourceSpecialtyChild').val();
                        if (specialty == null || specialty == ''){
                            fish.info("资源分配不能为空，请选择！");
                            return;
                        }
                    }
                    //校验资源分配指定人不为空
                    if ($('#resAllocateChild').val() == '是'){
                        if ($('#isResAssChild').val() == '是'){
                            var specitaltyInfo = $('#resourceSpecialtyChild').val();
                            if (specitaltyInfo != null && specitaltyInfo != ''){
                                for (var i = 0; i < specitaltyInfo.length; i++){
                                    var resAssPerson = $('#' + specitaltyInfo[i] + "PersonChild").popedit('getValue');
                                    if (resAssPerson == null ||  resAssPerson == '' || resAssPerson.value == null || resAssPerson.value == ''){
                                        fish.info("请完成资源分配指定人信息！！");
                                        return;
                                    }
                                }
                            }
                        }
                    }
                    //校验完工汇总指定人不为空
                    if ($('#isComplatePersonChild').val() == '是'){
                        var complatePerson = $('#complatePersonChild').popedit('getValue').value;
                        if (complatePerson == null ||  complatePerson == ''){
                            fish.info("完工汇总指定人不能为空，请选择！");
                            return;
                        }
                    }
                    var param = {};
                    param.checkData = checkData;
                    var form = {};
                    var toLocal = $("input[name='resRadio']:checked").val();//是否派发本地网
                    form.toLoacl = toLocal
                    if (toLocal == '是'){
                        form.masterRegion = $("#masterRegionName").popedit('getValue').value;
                        form.masterComplateTime = $('#masterComplateTime').val();
                        form.slaveRegion = $('#slaveRegionName').popedit('getValue').value;
                        form.slaveComplateTime = $('#slaveComplateTime').val();
                    }else if (toLocal == '否'){
                        form.masterRegion = '';
                        form.masterComplateTime = '';
                        form.slaveRegion = '';
                        form.slaveComplateTime = '';
                    }
                    form.netManage = $('#netManageChild').val();
                    var isAssPerMkData = $('#isAssignPersonChild').val();
                    form.isAssPerMkData = isAssPerMkData;
                    if (isAssPerMkData == '是'){
                        var netManageInfo = $('#netManageChild').val();
                        if (netManageInfo != null && netManageInfo != ''){
                            var mkDataPerson = {};
                            var mkDataPersonId = {}
                            for(var i = 0; i < netManageInfo.length; i++){
                                var key = netManageInfo[i];
                                mkDataPerson[key] = $("#" + key + "NameChild").val();
                                mkDataPersonId[key] = $("#" + key + "NameChild").popedit('getValue').value;
                            }
                            form.mkDataPerson = JSON.stringify(mkDataPerson);
                            form.mkDataPersonId = JSON.stringify(mkDataPersonId);
                        }
                    }else if (isAssPerMkData == '否'){
                        form.mkDataPerson = '';
                        form.mkDataPersonId = '';
                    }
                    var isResAssign = $('#isResAssChild').val();
                    form.isResAssign = isResAssign;
                    if(isResAssign == '是'){
                        //封装不同专业资源分配指定人信息
                        var specialtyInfo = $("#resourceSpecialtyChild").val();
                        if (specialtyInfo != null){
                            var resAssPerson = {};
                            var resAssPersonId = {};
                            for (var m = 0; m < specialtyInfo.length; m++){
                                var key = specialtyInfo[m];
                                resAssPerson[key] = $("#" + key + "PersonChild").val();
                                resAssPersonId[key] = $("#" + key + "PersonChild").popedit('getValue').value;
                            }
                            form.resAssPerson = JSON.stringify(resAssPerson);
                            form.resAssPersonId = JSON.stringify(resAssPersonId);
                        }
                    }else if (isResAssign == '否'){
                        form.resAssPerson = '';
                        form.resAssPersonId = '';
                    }
                    //完工汇总指定到人
                    var isComplatePerson = $('#isComplatePersonChild').val();
                    form.isComplatePerson = isComplatePerson;
                    if(isComplatePerson == '是'){
                        form.complatePersonId = $('#complatePersonChild').popedit('getValue').value;
                        form.complatePerson = $('#complatePersonChild').val();
                    }else if (isResAssign == '否'){
                        form.complatePerson = '';
                        form.complatePersonId = '';
                    }
                    form.resAllocate = $('#resAllocateChild').val();
                    form.resourceSpecialty = $('#resourceSpecialtyChild').val();
                    param.formData = form;
                    operOrderAction.batchSaveDispatchInfo(param, function(res){
                        if (res.success){
                            fish.info("主辅调局信息保存成功！");
                            me.popup.close();
                        }else{
                            fish.info(res.msg);
                        }
                    });
                }
            },
        });
    });