define(["module/component/views/FormView",
        "module/component/views/GridView",
        'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/operOrderAction',
        'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/orderDetailsAction',
        'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
        'text!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/templates/draftSchedulView.html',
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/operOrderView.css'
    ],
    function (FormView, GridView, operOrderAction, orderDetailsAction, orderStandbyAction, draftTemplate, i18n) {
        var userInfo = orderStandbyAction.queryStaffInfo().responseJSON.data;
        var URL = "";
        var FILES = null;
        var actionType = '';
        var dispatchOrderNo = "";
        var N = 0; //几条电路信息
        var circuitInfo;
        var oneDry;
        var groupNo;
        var oldOrgInfo;
        var dispatchTitle = "";
        var replenish = false;//补单
        var priOrgInfo;//补单，存一下原调单的派单信息
        var priDispOrdId;
        var disableAssist = false;
        var me;
        return fish.View.extend({
            template: fish.compile(draftTemplate),
            i18nData: fish.extend({}, i18n),
            events: {
                'click #saveDraft': 'saveDraft',
                'click #submit': 'submit',
                'click #batchSave': 'batchSave'//批量处理
            },
            initialize: function () {
                this.render();
            },
            render: function () {
                this.$el.html(this.template(this.i18nData));
                replenish = this.options.replenish;
                disableAssist = false;
                me = this;
                this.attachs = [];
                this.delAttach = [];
                this.isOnedry();
                this.initGrid();
                this.initDispDetailForm();
                this.initFileUpdate();

            },
            afterRender: function () {
                URL = this.getRootPath();
                if (!replenish){
                    $("#batchSaveDiv").show();
                }
            },
            isOnedry:function(){
                var obj = new Object();
                obj.cstOrdId = this.options.cstOrdId;
                oneDry = operOrderAction.queryOneDry(obj).responseJSON.data;
            },
            initGrid: function () {
                var that = this;
                var colModel = [
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
                ];
                var opt = {
                    height: '150px',
                    width: '100%',
                    curPageSort: true,
                    cellEdit: false,
                    shrinkToFit: false,
                    datatype: "json",
                    autowidth: true,
                    pageData: function (page, rowNum, sortname, sortorder) {
                        _.delay(function () {
                            that.queryData(page, rowNum, sortname, sortorder);
                        }, 100);
                        return false;
                    },
                    colModel: colModel
                };
                this.setView("#dispGrid", new GridView({
                    config: {
                        grid: opt
                    }
                }));
                this.getView("#dispGrid").on("viewRenderAfter", function () {
                    if (!oneDry) {//判断是否一干
                        this.hideCol(['ONE_GROUP_ROUTE','ONE_ALL_ROUTE']);
                    }
                    that.queryData();
                });

            },
            queryData: function () {
                var me = this;
                var param = {};
                param.onedry = oneDry;
                param.cstOrdId = me.options.parentOption.cstOrdId;
                param.tacheId = me.options.parentOption.tacheId;
                param.specialtyCode = me.options.parentOption.specialtyCode;
                param.woState = me.options.parentOption.woStateCir;
                param.woIds = me.options.parentOption.woIds;
                param.reginonId = me.options.parentOption.reginonId;
                param.dealUserId = me.options.parentOption.dealUserId;
                param.compUserId = me.options.parentOption.compUserId;
                param.dispType = me.options.parentOption.dispType;
                param.staffId = me.options.parentOption.staffId;
                param.dispObjTyeValue = me.options.parentOption.dispObjTyeValue;
                param.dispObjTye = me.options.parentOption.dispObjTye;
                param.orderIdSelect = me.options.parentOption.orderIdSelect;//工单查询orderId
                orderDetailsAction.queryCircuitInfoDraftGrid(param, function (res) {
                    if (res.flag == 1) {
                        var obj = {};
                        obj.rows = res.data;
                        me.getView("#dispGrid").reloadData(obj);
                        circuitInfo = res.data;
                        oldOrgInfo = null;
                        FILES = null;
                        groupNo = null;
                        N = res.data.length;
                        me.initOrgForm(N, null);
                        me.initdispOrdInfo();
                    }
                });
            },
            initOrgForm: function (n, batchSaveFlag) {
                var html = "";
                for (var i = 0; i < n; i++) {
                    var id = 'orgForm' + i;
                    html += "<div style='border: 2px;height:180px;' id='orgFormDiv"+i+"'><div>" + (i + 1) + '. ' + circuitInfo[i].CIRCUITCODE + "</div>";
                    html += "<div style='width:100%;border:1px;' id=" + id + "></div></div>";
                }
                $("#dispOrg").append(html);
                for (var i = 0; i < n; i++) {
                    this.initDispOrgForm('#orgForm' + i, i, batchSaveFlag);
                }
            },
            initDispOrgForm: function (id, i, batchSaveFlag) {
                var that = this;
                var config_elements = [
                    {
                        id: 'srvOrdId' + i,
                        type: 'text',
                        label: 'srvOrdId',
                        hidden: true
                    },
                    {
                        id: 'masterRegionName' + i,
                        type: 'text',
                        label: '主调局',
                        required: true
                    },
                    {
                        id: 'masterRegion' + i,
                        type: 'text',
                        label: '主调局',
                        hidden: true
                    },
                    {
                        id: 'masterReqTime' + i,
                        type: 'date',
                        required: true,
                        label: '完成时间'
                    },
                    {
                        id: 'assistRegion' + i,
                        type: 'text',
                        label: '辅调局',
                        hidden: true
                    },
                    {
                        id: 'assistRegionName' + i,
                        type: 'text',
                        label: '辅调局'
                    },
                    {
                        id: 'assistReqTime' + i,
                        type: 'date',
                        label: '完成时间'
                    },
                    {
                        id: 'netManage' + i,
                        type: 'multiselect',
                        label: '数据制作',
                        dataValueField: "ID",
                        dataTextField: "NAME",
                        column: 12,
                        label_sm: 6,
                        label_md: 3

                    },
                    {
                        id: 'isAssignPerson' + i,
                        type: 'select',
                        label: '数据制作是否指定到人',
                        options: [{NAME: '是', VALUE: '是'}, {NAME: '否', VALUE: '否'}],
                        defaultValue: {value: '否'},
                        placeholder: '--请选择是否指定到人--',
                        column: 12,
                        label_sm: 6,
                        label_md: 3
                    },
                    {
                        id: 'DATA_4Name' + i,
                        type: 'text',
                        required: true,
                        label: '数据专业人员选择',
                        hidden: true,
                        column: 12,
                        label_sm: 6,
                        label_md: 3
                    },
                    {
                        id: 'TRANS_3Name' + i,
                        type: 'text',
                        required: true,
                        label: '传输专业人员选择',
                        hidden: true,
                        column: 12,
                        label_sm: 6,
                        label_md: 3
                    },
                    {
                        id: 'EXCHANGE_5Name' + i,
                        type: 'text',
                        required: true,
                        label: '交换专业人员选择',
                        hidden: true,
                        column: 12,
                        label_sm: 6,
                        label_md: 3
                    },
                    {
                        id: 'IP_15Name' + i,
                        type: 'text',
                        required: true,
                        label: 'IP地址人员选择',
                        hidden: true,
                        column: 12,
                        label_sm: 6,
                        label_md: 3
                    },
                    {
                        id: 'OTHER_11Name' + i,
                        type: 'text',
                        required: true,
                        label: '其它专业人员选择',
                        hidden: true,
                        column: 12,
                        label_sm: 6,
                        label_md: 3
                    },
                    {
                        id: 'resAllocate' + i,
                        type: 'select',
                        label: '是否转资源分配',
                        options: [{NAME: '是', VALUE: '是'}, {NAME: '否', VALUE: '否'}],
                        defaultValue: {value: '是'},
                        placeholder: '--请选择是否转资源分配--',
                        column: 12,
                        label_sm: 6,
                        label_md: 3
                    },
                    {
                        id: 'specialty' + i,
                        type: 'multiselect',
                        label: '资源分配',
                        required: true,
                        dataValueField: "ID",
                        dataTextField: "NAME",
                        column: 12,
                        label_sm: 6,
                        label_md: 3
                    },
                    {
                        id: 'isResAss' + i,
                        type: 'select',
                        label: '资源是否分配到人',
                        options: [{NAME: '是', VALUE: '是'}, {NAME: '否', VALUE: '否'}],
                        defaultValue: {value: '否'},
                        placeholder: '--请选择是否资源分配到人--',
                        column: 12,
                        label_sm: 6,
                        label_md: 3
                    },
                    {
                        id: 'TRANS_3Person' + i,
                        label: '传输专业人员选择',
                        type: 'text',
                        required: true,
                        hidden: true,
                        column: 12,
                        label_sm: 6,
                        label_md: 3
                    },
                    {
                        id: 'DATA_4Person' + i,
                        label: '数据专业人员选择',
                        type: 'text',
                        required: true,
                        hidden: true,
                        column: 12,
                        label_sm: 6,
                        label_md: 3
                    },
                    {
                        id: 'EXCHANGE_5Person' + i,
                        label: '交换专业人员选择',
                        type: 'text',
                        required: true,
                        hidden: true,
                        column: 12,
                        label_sm: 6,
                        label_md: 3
                    },
                    {
                        id: 'OPTICAL_2Person' + i,
                        label: '光纤专业人员选择',
                        type: 'text',
                        required: true,
                        hidden: true,
                        column: 12,
                        label_sm: 6,
                        label_md: 3
                    },
                    {
                        id: 'IP_15Person' + i,
                        label: 'IP地址人员选择',
                        type: 'text',
                        required: true,
                        hidden: true,
                        column: 12,
                        label_sm: 6,
                        label_md: 3
                    },
                    {
                        id: 'OTHER_11Person' + i,
                        label: '其它专业人员选择',
                        type: 'text',
                        required: true,
                        hidden: true,
                        column: 12,
                        label_sm: 6,
                        label_md: 3
                    },
                    {
                        id: 'resMakeAllocate' + i,
                        type: 'select',
                        label: '是否资源施工',
                        options: [{NAME: '是', VALUE: '是'}, {NAME: '否', VALUE: '否'}],
                        defaultValue: {value: '是'},
                        placeholder: '--请选择是否资源施工--',
                        column: 12,
                        label_sm: 6,
                        label_md: 3
                    },
                    {
                        id: 'resMake' + i,
                        type: 'multiselect',
                        label: '资源施工',
                        //required: true,
                        dataValueField: "ID",
                        dataTextField: "NAME",
                        column: 12,
                        label_sm: 6,
                        label_md: 3
                    },
                    {
                        id: 'isResMake' + i,
                        type: 'select',
                        label: '资源施工是否分配到人',
                        options: [{NAME: '是', VALUE: '是'}, {NAME: '否', VALUE: '否'}],
                        defaultValue: {value: '否'},
                        placeholder: '--请选择是否指定到人--',
                        column: 12,
                        label_sm: 6,
                        label_md: 3
                    },
                    {
                        id: 'TRANS_3Make' + i,
                        label: '传输专业人员选择',
                        type: 'text',
                        required: true,
                        hidden: true,
                        column: 12,
                        label_sm: 6,
                        label_md: 3
                    },
                    {
                        id: 'DATA_4Make' + i,
                        label: '数据专业人员选择',
                        type: 'text',
                        required: true,
                        hidden: true,
                        column: 12,
                        label_sm: 6,
                        label_md: 6
                    },
                    {
                        id: 'EXCHANGE_5Make' + i,
                        label: '交换专业人员选择',
                        type: 'text',
                        required: true,
                        hidden: true,
                        column: 12,
                        label_sm: 6,
                        label_md: 3
                    },
                    {
                        id: 'OPTICAL_2Make' + i,
                        label: '光纤专业人员选择',
                        type: 'text',
                        required: true,
                        hidden: true,
                        column: 12,
                        label_sm: 6,
                        label_md: 6
                    },
                    {
                        id: 'IP_15Make' + i,
                        label: 'IP地址人员选择',
                        type: 'text',
                        required: true,
                        hidden: true,
                        column: 12,
                        label_sm: 6,
                        label_md: 3
                    },
                    {
                        id: 'OTHER_11Make' + i,
                        label: '其它专业人员选择',
                        type: 'text',
                        required: true,
                        hidden: true,
                        column: 12,
                        label_sm: 6,
                        label_md: 3
                    },
                    {
                        id: 'isComplatePerson' + i,
                        type: 'select',
                        label: '完工汇总是否指定到人',
                        options: [{NAME: '是', VALUE: '是'}, {NAME: '否', VALUE: '否'}],
                        defaultValue: {value: '是'},
                        placeholder: '--请选择完工汇总是否指定到人--',
                        column: 12,
                        label_sm: 6,
                        label_md: 3
                    },
                    {
                        id: 'complatePerson' + i,
                        label: '完工汇总人员选择',
                        type: 'text',
                        required: true,
                        column: 12,
                        label_sm: 6,
                        label_md: 3
                    },
                ];

                this.setView(id, new FormView({
                    config: {
                        elements: config_elements,
                        column: 1,
                        callback: {
                            onChange: function (obj) {
                                if (obj.indexOf("netManage") != -1) {
                                    that.setSendOrg();
                                }
                                //处理是否资源分配联动
                                if (obj.indexOf("resAllocate") != -1) {
                                    var value = this.getValueText(obj).value;
                                    if (value == "是") {
                                        $("#top_node_specialty" + i).css('display', 'block');
                                        $("#top_node_isResAss" + i).css('display', 'block');
                                        //获取资源分配是否指定到人
                                        var isResAss = this.getValueText("isResAss" + i).value;
                                        if (isResAss == "是"){
                                            //获取资源分配选择的专业，根据选择的专业进行展示相关专业的指定人员选择
                                            var specialtyInfo = $("#specialty" + i).val();
                                            if (specialtyInfo != null){
                                                for (var m = 0; m < specialtyInfo.length; m++){
                                                    $("#top_node_" + specialtyInfo[m] + "Person"  + i).css('display', 'block');
                                                }
                                            }else{
                                                $("#top_node_TRANS_3Person" + i).css('display', 'none');
                                                $("#top_node_DATA_4Person" + i).css('display', 'none');
                                                $("#top_node_EXCHANGE_5Person" + i).css('display', 'none');
                                                $("#top_node_OPTICAL_2Person" + i).css('display', 'none');
                                                $("#top_node_IP_15Person" + i).css('display', 'none');
                                                $("#top_node_OTHER_11Person" + i).css('display', 'none');
                                            }
                                        }else{
                                            $("#top_node_TRANS_3Person" + i).css('display', 'none');
                                            $("#top_node_DATA_4Person" + i).css('display', 'none');
                                            $("#top_node_EXCHANGE_5Person" + i).css('display', 'none');
                                            $("#top_node_OPTICAL_2Person" + i).css('display', 'none');
                                            $("#top_node_IP_15Person" + i).css('display', 'none');
                                            $("#top_node_OTHER_11Person" + i).css('display', 'none');
                                        }
                                    } else {
                                        $("#top_node_specialty" + i).css('display', 'none');
                                        $("#top_node_isResAss" + i).css('display', 'none');
                                        $("#top_node_TRANS_3Person" + i).css('display', 'none');
                                        $("#top_node_DATA_4Person" + i).css('display', 'none');
                                        $("#top_node_EXCHANGE_5Person" + i).css('display', 'none');
                                        $("#top_node_OPTICAL_2Person" + i).css('display', 'none');
                                        $("#top_node_IP_15Person" + i).css('display', 'none');
                                        $("#top_node_OTHER_11Person" + i).css('display', 'none');
                                    }
                                }
                                //处理是否资源施工联动
                                if (obj.indexOf("resMakeAllocate") != -1) {
                                    var value = this.getValueText(obj).value;
                                    if (value == "是") {
                                        $("#top_node_resMake" + i).css('display', 'block');
                                        $("#top_node_isResMake" + i).css('display', 'block');
                                        //获取资源分配是否指定到人
                                        var isResMake = this.getValueText("isResMake" + i).value;
                                        if (isResMake == "是"){
                                            //获取资源分配选择的专业，根据选择的专业进行展示相关专业的指定人员选择
                                            var resMakeInfo = $("#resMake" + i).val();
                                            if (resMakeInfo != null){
                                                for (var m = 0; m < resMakeInfo.length; m++){
                                                    $("#top_node_" + resMakeInfo[m] + "Make"  + i).css('display', 'block');
                                                }
                                            }else{
                                                $("#top_node_TRANS_3Make" + i).css('display', 'none');
                                                $("#top_node_DATA_4Make" + i).css('display', 'none');
                                                $("#top_node_EXCHANGE_5Make" + i).css('display', 'none');
                                                $("#top_node_OPTICAL_2Make" + i).css('display', 'none');
                                                $("#top_node_IP_15Make" + i).css('display', 'none');
                                                $("#top_node_OTHER_11Make" + i).css('display', 'none');
                                            }
                                        }else{
                                            $("#top_node_TRANS_3Make" + i).css('display', 'none');
                                            $("#top_node_DATA_4Make" + i).css('display', 'none');
                                            $("#top_node_EXCHANGE_5Make" + i).css('display', 'none');
                                            $("#top_node_OPTICAL_2Make" + i).css('display', 'none');
                                            $("#top_node_IP_15Person" + i).css('display', 'none');
                                            $("#top_node_OTHER_11Make" + i).css('display', 'none');
                                        }
                                    } else {
                                        $("#top_node_resMake" + i).css('display', 'none');
                                        $("#top_node_isResMake" + i).css('display', 'none');
                                        $("#top_node_TRANS_3Make" + i).css('display', 'none');
                                        $("#top_node_DATA_4Make" + i).css('display', 'none');
                                        $("#top_node_EXCHANGE_5Make" + i).css('display', 'none');
                                        $("#top_node_OPTICAL_2Make" + i).css('display', 'none');
                                        $("#top_node_IP_15Make" + i).css('display', 'none');
                                        $("#top_node_OTHER_11Make" + i).css('display', 'none');
                                    }
                                }
                                //处理资源分配是否指定到人
                                if (obj.indexOf("isResAss") != -1){
                                    var value = this.getValueText(obj).value;
                                    if (value == "是") {
                                        var specialtyInfo = $("#specialty" + i).val();
                                        if (specialtyInfo != null){
                                            for (var m = 0; m < specialtyInfo.length; m++){
                                                $("#top_node_" + specialtyInfo[m] + "Person"  + i).css('display', 'block');
                                            }
                                        }else{
                                            $("#top_node_TRANS_3Person" + i).css('display', 'none');
                                            $("#top_node_DATA_4Person" + i).css('display', 'none');
                                            $("#top_node_EXCHANGE_5Person" + i).css('display', 'none');
                                            $("#top_node_OPTICAL_2Person" + i).css('display', 'none');
                                            $("#top_node_IP_15Person" + i).css('display', 'none');
                                            $("#top_node_OTHER_11Person" + i).css('display', 'none');
                                        }
                                    } else {
                                        $("#top_node_TRANS_3Person" + i).css('display', 'none');
                                        $("#top_node_DATA_4Person" + i).css('display', 'none');
                                        $("#top_node_EXCHANGE_5Person" + i).css('display', 'none');
                                        $("#top_node_OPTICAL_2Person" + i).css('display', 'none');
                                        $("#top_node_IP_15Person" + i).css('display', 'none');
                                        $("#top_node_OTHER_11Person" + i).css('display', 'none');
                                    }
                                }
                                //处理资源施工是否指定到人
                                if (obj.indexOf("isResMake") != -1){
                                    var value = this.getValueText(obj).value;
                                    if (value == "是") {
                                        var resMakeInfo = $("#resMake" + i).val();
                                        if (resMakeInfo != null){
                                            for (var m = 0; m < resMakeInfo.length; m++){
                                                $("#top_node_" + resMakeInfo[m] + "Make"  + i).css('display', 'block');
                                            }
                                        }else{
                                            $("#top_node_TRANS_3Make" + i).css('display', 'none');
                                            $("#top_node_DATA_4Make" + i).css('display', 'none');
                                            $("#top_node_EXCHANGE_5Make" + i).css('display', 'none');
                                            $("#top_node_OPTICAL_2Make" + i).css('display', 'none');
                                            $("#top_node_IP_15Make" + i).css('display', 'none');
                                            $("#top_node_OTHER_11Make" + i).css('display', 'none');
                                        }
                                    } else {
                                        $("#top_node_TRANS_3Make" + i).css('display', 'none');
                                        $("#top_node_DATA_4Make" + i).css('display', 'none');
                                        $("#top_node_EXCHANGE_5Make" + i).css('display', 'none');
                                        $("#top_node_OPTICAL_2Make" + i).css('display', 'none');
                                        $("#top_node_IP_15Make" + i).css('display', 'none');
                                        $("#top_node_OTHER_11Make" + i).css('display', 'none');
                                    }
                                }

                                //处理资源分配联动
                                if (obj.indexOf("specialty") != -1){
                                    //获取资源分配是否指定到人
                                    var isResAss = $('#isResAss' + i).val();
                                    if (isResAss == '是'){
                                        //先将所有专业指定人隐藏
                                        $("#top_node_TRANS_3Person" + i).css('display', 'none');
                                        $("#top_node_DATA_4Person" + i).css('display', 'none');
                                        $("#top_node_EXCHANGE_5Person" + i).css('display', 'none');
                                        $("#top_node_OPTICAL_2Person" + i).css('display', 'none');
                                        $("#top_node_IP_15Person" + i).css('display', 'none');
                                        $("#top_node_OTHER_11Person" + i).css('display', 'none');
                                        //获取资源分配专业信息
                                        var specialtyInfo = $("#specialty" + i).val();
                                        if (specialtyInfo != null){
                                            for (var m = 0; m < specialtyInfo.length; m++){
                                                $("#top_node_" + specialtyInfo[m] + "Person"  + i).css('display', 'block');
                                            }
                                        }else{
                                            $("#top_node_TRANS_3Person" + i).css('display', 'none');
                                            $("#top_node_DATA_4Person" + i).css('display', 'none');
                                            $("#top_node_EXCHANGE_5Person" + i).css('display', 'none');
                                            $("#top_node_OPTICAL_2Person" + i).css('display', 'none');
                                            $("#top_node_IP_15Person" + i).css('display', 'none');
                                            $("#top_node_OTHER_11Person" + i).css('display', 'none');
                                        }
                                    }
                                }
                                //处理资源施工联动
                                if (obj.indexOf("resMake") != -1){
                                    //获取资源分配是否指定到人
                                    var isResMake = $('#isResMake' + i).val();
                                    if (isResMake == '是'){
                                        //先将所有专业指定人隐藏
                                        $("#top_node_TRANS_3Make" + i).css('display', 'none');
                                        $("#top_node_DATA_4Make" + i).css('display', 'none');
                                        $("#top_node_EXCHANGE_5Make" + i).css('display', 'none');
                                        $("#top_node_OPTICAL_2Make" + i).css('display', 'none');
                                        $("#top_node_IP_15Make" + i).css('display', 'none');
                                        $("#top_node_OTHER_11Make" + i).css('display', 'none');
                                        //获取资源分配专业信息
                                        var resMakeInfo = $("#resMake" + i).val();
                                        if (resMakeInfo != null){
                                            for (var m = 0; m < resMakeInfo.length; m++){
                                                $("#top_node_" + resMakeInfo[m] + "Make"  + i).css('display', 'block');
                                            }
                                        }else{
                                            $("#top_node_TRANS_3Make" + i).css('display', 'none');
                                            $("#top_node_DATA_4Make" + i).css('display', 'none');
                                            $("#top_node_EXCHANGE_5Make" + i).css('display', 'none');
                                            $("#top_node_OPTICAL_2Make" + i).css('display', 'none');
                                            $("#top_node_IP_15Make" + i).css('display', 'none');
                                            $("#top_node_OTHER_11Make" + i).css('display', 'none');
                                        }
                                    }
                                }
                                //处理数据制作是否指定到人联动
                                if (obj.indexOf("isAssignPerson") != -1){
                                    var value = this.getValueText(obj).value;
                                    if (value == "是") {
                                        var netManageInfo = $('#netManage' + i).val();
                                        if (netManageInfo != null){
                                            for (var m = 0; m < netManageInfo.length; m++){
                                                $("#top_node_" + netManageInfo[m] + "Name" + i).css('display', 'block');
                                            }
                                        }else{
                                            $("#top_node_DATA_4Name" + i).css('display', 'none');
                                            $("#top_node_TRANS_3Name" + i).css('display', 'none');
                                            $("#top_node_EXCHANGE_5Name" + i).css('display', 'none');
                                            $("#top_node_IP_15Name" + i).css('display', 'none');
                                            $("#top_node_OTHER_11Name" + i).css('display', 'none');
                                        }
                                    } else {
                                        $("#top_node_DATA_4Name" + i).css('display', 'none');
                                        $("#top_node_TRANS_3Name" + i).css('display', 'none');
                                        $("#top_node_EXCHANGE_5Name" + i).css('display', 'none');
                                        $("#top_node_IP_15Name" + i).css('display', 'none');
                                        $("#top_node_OTHER_11Name" + i).css('display', 'none');
                                    }
                                }
                                //处理数据制作联动
                                if (obj.indexOf("netManage") != -1){
                                    var isAssignPerson = $('#isAssignPerson' + i).val();
                                    if (isAssignPerson == "是") {
                                        //先将所有专业指定人隐藏
                                        $("#top_node_DATA_4Name" + i).css('display', 'none');
                                        $("#top_node_TRANS_3Name" + i).css('display', 'none');
                                        $("#top_node_EXCHANGE_5Name" + i).css('display', 'none');
                                        $("#top_node_IP_15Name" + i).css('display', 'none');
                                        $("#top_node_OTHER_11Name" + i).css('display', 'none');
                                        var netManageInfo = $('#netManage' + i).val();
                                        if (netManageInfo != null){
                                            for (var m = 0; m < netManageInfo.length; m++){
                                                $("#top_node_" + netManageInfo[m] + "Name" + i).css('display', 'block');
                                            }
                                        }else{
                                            $("#top_node_DATA_4Name" + i).css('display', 'none');
                                            $("#top_node_TRANS_3Name" + i).css('display', 'none');
                                            $("#top_node_EXCHANGE_5Name" + i).css('display', 'none');
                                            $("#top_node_IP_15Name" + i).css('display', 'none');
                                            $("#top_node_OTHER_11Name" + i).css('display', 'none');
                                        }
                                    }
                                }
                                //完工汇总是否指定到人联动
                                if (obj.indexOf("isComplatePerson") != -1){
                                    var value = this.getValueText(obj).value;
                                    if (value == "是") {
                                        $("#top_node_complatePerson" + i).css('display', 'block');
                                    } else {
                                        $("#top_node_complatePerson" + i).css('display', 'none');
                                    }
                                }
                            }
                        }
                    }
                }));

                this.renderViews(id);
                this.getView(id).on("viewRenderAfter", function () {
                    var html = "<div class='col-sm-12 col-md-12 col-lg-12' id='toBdw"+i+"'>\n" +
                        "    <div class='form-group'>\n" +
                        "        <label class='col-md-3 col-sm-3 control-label' title='是否派发本地网' style=\"width: 12.5%;\">是否派发本地网</label>\n" +
                        "        <div class='col-md-9 col-sm-9' style=\"width: 87.5%;margin-top:3px;\">\n" +
                        "             <input type='radio' name='ynRadio"+i+"' class='ynRadio"+i+"' id='toBdwY"+ i+"' value='是' data-rule='checked'\n" +
                        "                       checked='true'>是\n" +
                        "             <input type='radio' name='ynRadio"+i+"' class='ynRadio"+i+"' id='toBdwN"+i+"' value='否' style='margin-left:10px;'>否\n" +
                        "        </div>\n" +
                        "    </div>\n" +
                        "</div>";
                    $("#top_node_srvOrdId" + i).after(html);

                    $(".ynRadio" + i).click(function () {
                        var val = $("input:radio[name='ynRadio"+i+"']:checked").val();
                        if(val == '是'){
                            $("#top_node_masterRegionName"+ i).css('display','block');
                            $("#top_node_masterReqTime"+ i).css('display','block');
                            $("#top_node_assistRegionName"+ i).css('display','block');
                            $("#top_node_assistReqTime"+ i).css('display','block');
                            that.setSendOrg();
                            $("#masterRegionName" + i).popedit('enable'); // 补单可能禁用主调局选择，重新启用
                            that.getView("#orgForm" + i).enable("masterReqTime" + i);
                        }else {
                            $("#top_node_masterRegionName"+ i).css('display','none');
                            $("#top_node_masterReqTime"+ i).css('display','none');
                            $("#top_node_assistRegionName"+ i).css('display','none');
                            $("#top_node_assistReqTime"+ i).css('display','none');
                            that.setSendOrg();
                        }
                    });

                    var siht = this;
                    var obj = new Object();
                    obj.proId = "23";
                    obj.type = "SPECIALTY_TYPE";
                    var specialtyOP = operOrderAction.querySpecNetMag(obj).responseJSON.data;
                    siht.initDropItem("specialty" + i, specialtyOP ? specialtyOP : []);

                    obj.type = "SPECIALTY_TYPE";
                    var netmanageOp = operOrderAction.querySpecNetMag(obj).responseJSON.data;
                    siht.initDropItem("netManage" + i, netmanageOp ? netmanageOp : []);
                    $("#srvOrdId" + i).val(circuitInfo[i].SRV_ORD_ID);

                    obj.type = "NETMANAGE_TYPE";//资源施工
                    var resMakeOp = operOrderAction.querySpecNetMag(obj).responseJSON.data;
                    siht.initDropItem("resMake" + i, resMakeOp ? resMakeOp : []);

                    $("#top_node_masterRegionName"+i +" span").click(function () {
                        $("#masterRegion" + i).val('');
                    });
                    $("#top_node_assistRegionName"+i +" span").click(function () {
                        if(replenish){
                            $("#assistRegion" + i).val(oldOrgInfo[i].ASSIST_REGION);
                            $("#assistRegionName" + i).val(oldOrgInfo[i].ASSIST_REGION_NAME);
                        }else{
                            $("#assistRegion" + i).val('');
                            $("#assistReqTime" + i).val('');
                            $("#top_node_assistReqTime" + i + " .form-group").removeClass('required');
                        }
                    });

                    $("#top_node_masterRegionName" + i + " .res_form_content").addClass('input-group');
                    $("#top_node_masterRegionName" + i + " .res_form_content").css('padding', '0 10px');
                    $("#top_node_assistRegionName" + i + " .res_form_content").addClass('input-group');
                    $("#top_node_assistRegionName" + i + " .res_form_content").css('padding', '0 10px');
                    $("#top_node_DATA_4Name" + i + " .res_form_content").addClass('input-group');
                    $("#top_node_DATA_4Name" + i + " .res_form_content").css('padding', '0 10px');
                    $("#top_node_TRANS_3Name" + i + " .res_form_content").addClass('input-group');
                    $("#top_node_TRANS_3Name" + i + " .res_form_content").css('padding', '0 10px');
                    $("#top_node_EXCHANGE_5Name" + i + " .res_form_content").addClass('input-group');
                    $("#top_node_EXCHANGE_5Name" + i + " .res_form_content").css('padding', '0 10px');
                    $("#top_node_IP_15Name" + i + " .res_form_content").addClass('input-group');
                    $("#top_node_IP_15Name" + i + " .res_form_content").css('padding', '0 10px');
                    $("#top_node_OTHER_11Name" + i + " .res_form_content").addClass('input-group');
                    $("#top_node_OTHER_11Name" + i + " .res_form_content").css('padding', '0 10px');

                    $("#top_node_TRANS_3Person" + i + " .res_form_content").addClass('input-group');
                    $("#top_node_TRANS_3Person" + i + " .res_form_content").css('padding', '0 10px');
                    $("#top_node_DATA_4Person" + i + " .res_form_content").addClass('input-group');
                    $("#top_node_DATA_4Person" + i + " .res_form_content").css('padding', '0 10px');
                    $("#top_node_EXCHANGE_5Person" + i + " .res_form_content").addClass('input-group');
                    $("#top_node_EXCHANGE_5Person" + i + " .res_form_content").css('padding', '0 10px');
                    $("#top_node_OPTICAL_2Person" + i + " .res_form_content").addClass('input-group');
                    $("#top_node_OPTICAL_2Person" + i + " .res_form_content").css('padding', '0 10px');
                    $("#top_node_IP_15Person" + i + " .res_form_content").addClass('input-group');
                    $("#top_node_IP_15Person" + i + " .res_form_content").css('padding', '0 10px');
                    $("#top_node_OTHER_11Person" + i + " .res_form_content").addClass('input-group');
                    $("#top_node_OTHER_11Person" + i + " .res_form_content").css('padding', '0 10px');

                    $("#top_node_TRANS_3Make" + i + " .res_form_content").addClass('input-group');
                    $("#top_node_TRANS_3Make" + i + " .res_form_content").css('padding', '0 10px');
                    $("#top_node_DATA_4Make" + i + " .res_form_content").addClass('input-group');
                    $("#top_node_DATA_4Make" + i + " .res_form_content").css('padding', '0 10px');
                    $("#top_node_EXCHANGE_5Make" + i + " .res_form_content").addClass('input-group');
                    $("#top_node_EXCHANGE_5Make" + i + " .res_form_content").css('padding', '0 10px');
                    $("#top_node_OPTICAL_2Make" + i + " .res_form_content").addClass('input-group');
                    $("#top_node_OPTICAL_2Make" + i + " .res_form_content").css('padding', '0 10px');
                    $("#top_node_IP_15Make" + i + " .res_form_content").addClass('input-group');
                    $("#top_node_IP_15Make" + i + " .res_form_content").css('padding', '0 10px');
                    $("#top_node_OTHER_11Make" + i + " .res_form_content").addClass('input-group');
                    $("#top_node_OTHER_11Make" + i + " .res_form_content").css('padding', '0 10px');

                    $("#top_node_complatePerson" + i + " .res_form_content").addClass('input-group');
                    $("#top_node_complatePerson" + i + " .res_form_content").css('padding', '0 10px');

                    //低分辨率 全变一行了，没办法，手动设置吧
                    $("#top_node_masterRegionName"+i).css('width','50%');
                    $("#top_node_masterReqTime"+i).css('width','50%');
                    $("#top_node_assistRegionName"+i).css('width','50%');
                    $("#top_node_assistReqTime"+i).css('width','50%');
                    $("#top_node_netManage"+i).css('width','50%');
                    $("#top_node_resAllocate"+i).css('width','50%');
                    $("#top_node_specialty"+i).css('width','50%');
                    $("#top_node_DATA_4Name"+i).css('width','50%');
                    $("#top_node_TRANS_3Name"+i).css('width','50%');
                    $("#top_node_EXCHANGE_5Name"+i).css('width','50%');
                    $("#top_node_IP_15Name"+i).css('width','50%');
                    $("#top_node_OTHER_11Name"+i).css('width','50%');

                    $("#top_node_TRANS_3Person"+i).css('width','50%');
                    $("#top_node_DATA_4Person"+i).css('width','50%');
                    $("#top_node_EXCHANGE_5Person"+i).css('width','50%');
                    $("#top_node_OPTICAL_2Person"+i).css('width','50%');
                    $("#top_node_IP_15Person"+i).css('width','50%');
                    $("#top_node_OTHER_11Person"+i).css('width','50%');

                    $("#top_node_TRANS_3Make"+i).css('width','50%');
                    $("#top_node_DATA_4Make"+i).css('width','50%');
                    $("#top_node_EXCHANGE_5Make"+i).css('width','50%');
                    $("#top_node_OPTICAL_2Make"+i).css('width','50%');
                    $("#top_node_IP_15Make"+i).css('width','50%');
                    $("#top_node_OTHER_11Make"+i).css('width','50%');
                    $("#top_node_complatePerson"+i).css('width','50%');

                    $("#top_node_isAssignPerson"+i).css('width','50%');
                    $("#top_node_isResAss"+i).css('width','50%');
                    $("#top_node_resMakeAllocate"+i).css('width','50%');
                    $("#top_node_resMake"+i).css('width','50%');
                    $("#top_node_isResMake"+i).css('width','50%');
                    $("#top_node_isComplatePerson"+i).css('width','50%');
                    $("#top_node_complatePerson"+i).css('width','50%');
                    that.initRegion(i, batchSaveFlag);
                    //初始化指定人员选择树
                    // that.initAssignPerson(i, batchSaveFlag);

                });
            },
            refreshAssistRegion: function(index){
                var idArr = new Array();
                var nameArr = new Array();
                var masterid = $("#masterRegion" + index).val();
                var masterName = $("#masterRegionName" + index).val();
                var assistId = $("#assistRegion" + index).val();
                var assistName = $("#assistRegionName" + index).val();
                if (assistId) {
                    var assistIdArr = assistId.split(",");
                    var assistNameArr = assistName.split(",");
                    for(var i in assistIdArr){
                        if(assistIdArr[i] != masterid) {
                            idArr.push(assistIdArr[i]);
                        }
                    }
                    for(var j in assistNameArr) {
                        if(assistNameArr[j] != masterName) {
                            nameArr.push(assistNameArr[j]);
                        }
                    }
                }
                if (idArr) {
                    var ids = idArr.filter(function (element, index, self) {
                        return self.indexOf(element) === index;
                    });
                    $("#assistRegion" + index).val(ids.join(','));
                }
                if (nameArr) {
                    var names = nameArr.filter(function (element, index, self) {
                        return self.indexOf(element) === index;
                    });
                    $("#assistRegionName" + index).val(names.join(','));
                }
            },
            initRegion: function (i, batchSaveFlag) {
                var me = this;
                //主调局
                $("#masterRegionName" + i).popedit({
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
                                orderId: me.options.parentOption.orderId,
                                key: "MASTER",
                                circuitInfo : oldOrgInfo ? [oldOrgInfo[i]] : "" //电路信息,这里为了子页面复用封装为array类型
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
                                    $("#masterRegion" + i).val(orgIds);
                                    $("#masterRegionName" + i).val(orgNames);
                                    me.setSendOrg();
                                    me.refreshAssistRegion(i);
                                }, function (e) {
                                    console.log('关闭了', e);
                                });
                            }
                        };
                        var popup = fish.popupView(options);
                    }
                });
                //辅调局
                $("#assistRegionName" + i).popedit({
                    showClearIcon:false, //不显示x按钮----好像没啥作用，下面手动销毁了
                    initialData: {
                        'name': '--请选择辅调局--',
                        'value': ''
                    },
                    open: function (e) {
                        var masterRegion = $("#masterRegion" + i).val();
                        var areaPro = {};
                        var _this = $(this);
                        var key = _this.get(0).id;
                        var _array = new Array;
                        var _value = $("#assistRegion" + i).val();
                        if (_value != null) {
                            _array = _value.split(",");
                        }
                        areaPro[key] = _array;
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
                                nodeValues: areaPro[key],
                                masterRegion:masterRegion,
                                oldRegions:replenish&&disableAssist?[oldOrgInfo[i]]:'', //补单，主流程在数据制作环节原辅调不可删；封装成数组，方便子页面复用
                                circuitInfos : oldOrgInfo ? [oldOrgInfo[i]] : "" //电路信息
                            },
                            callback: function (popup, view) {
                                popup.result.then(function (res) {
                                    var orgNames = replenish&&disableAssist?oldOrgInfo[i].ASSIST_REGION_NAME:'';
                                    var orgIds = replenish&&disableAssist?oldOrgInfo[i].ASSIST_REGION:'';
                                    var nodeArray = new Array;
                                    res.forEach(function (val, i) {
                                        //
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
                                    $("#assistRegion" + i).val(orgIds);
                                    $("#assistRegionName" + i).val(orgNames);
                                    if($("#assistRegion" + i).val() != ''){
                                        $("#top_node_assistReqTime"+i+" .form-group").addClass('required');
                                    }else{
                                        $("#top_node_assistReqTime"+i+" .form-group").removeClass('required');
                                    }
                                    me.setSendOrg();
                                }, function (e) {
                                    console.log('关闭了', e);
                                });
                            }
                        };
                        var popup = fish.popupView(options);
                    }
                });
                $("#assistRegionName" + i).clearinput('destroy'); //手动销毁小叉叉
                //数据专业数据制作指定人
                $("#DATA_4Name" + i).popedit({
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
                //传输专业数据制作指定人
                $("#TRANS_3Name" + i).popedit({
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
                //交换专业的数据制作指定人
                $("#EXCHANGE_5Name" + i).popedit({
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
                //IP地址数据制作指定人
                $("#IP_15Name" + i).popedit({
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
                //其它专业
                $("#OTHER_11Name" + i).popedit({
                    initialData: {
                        'name': '--请选其它专业的数据制作指定人--',
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
                //传输专业资源分配指定人
                $("#TRANS_3Person" + i).popedit({
                    initialData: {
                        'name': '--请选择传输专业资源分配的指定人--',
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
                $("#DATA_4Person" + i).popedit({
                    initialData: {
                        'name': '--请选择数据专业资源分配的指定人--',
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
                //交换专业资源分配指定人
                $("#EXCHANGE_5Person" + i).popedit({
                    initialData: {
                        'name': '--请选择交换专业资源分配的指定人--',
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
                //光纤专业资源分配指定人
                $("#OPTICAL_2Person" + i).popedit({
                    initialData: {
                        'name': '--请选择光纤专业资源分配的指定人--',
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
                //IP地址资源分配指定人
                $("#IP_15Person" + i).popedit({
                    initialData: {
                        'name': '--请选择IP地址资源分配的指定人--',
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
                //其它专业资源分配指定人
                $("#OTHER_11Person" + i).popedit({
                    initialData: {
                        'name': '--请选择其它专业资源分配的指定人--',
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

                //资源施工
                //传输专业资源分配指定人
                $("#TRANS_3Make" + i).popedit({
                    initialData: {
                        'name': '--请选择传输专业资源施工的指定人--',
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
                $("#DATA_4Make" + i).popedit({
                    initialData: {
                        'name': '--请选择数据专业资源施工的指定人--',
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
                //交换专业资源分配指定人
                $("#EXCHANGE_5Make" + i).popedit({
                    initialData: {
                        'name': '--请选择交换专业资源施工的指定人--',
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
                //光纤专业资源分配指定人
                $("#OPTICAL_2Make" + i).popedit({
                    initialData: {
                        'name': '--请选择光纤专业资源施工的指定人--',
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
                //IP地址资源分配指定人
                $("#IP_15Make" + i).popedit({
                    initialData: {
                        'name': '--请选择IP地址资源施工的指定人--',
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
                //其它专业资源分配指定人
                $("#OTHER_11Make" + i).popedit({
                    initialData: {
                        'name': '--请选择其它专业资源施工的指定人--',
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


                //完工汇总指定人
                $("#complatePerson" + i).popedit({
                    initialData: {
                        'name': '--请选择指定的人--',
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
                var activeType = me.options.parentOption.activeType;
                $.each(circuitInfo,function(i,val){
                    if (val.SERVICE_ID == '10000008' || activeType == '102'){ //如果是mv10000008
                        $("#top_node_netManage"+i+" .form-group").removeClass('required');
                    }else {
                        $("#top_node_netManage"+i+" .form-group").addClass('required');
                    }
                });
                me.intDispOrgInfo(i, batchSaveFlag);
            },
            initDispDetailForm: function () {
                var that = this;
                var config_elements = [
                    {
                        id: 'dispatchId',
                        type: 'text',
                        label: '调度单id',
                        hidden: true
                    },
                    {
                        id: 'dispatchOrderNo',
                        type: 'text',
                        label: '调度单编号',
                        disable: true
                    },
                    {
                        id: 'useGroupNo',
                        type: 'select',
                        label: '是否沿用集团调单编号',
                        options: [{NAME: '是', VALUE: '是'}, {NAME: '否', VALUE: '否'}],
                        defaultValue: {value: '是'},
                        placeholder: '--请选择是否沿用集团调单编号--'
                    },
                    {
                        id: 'dispatchGrade',
                        type: 'select',
                        label: '调度单等级',
                        options: [{NAME: '蓝色', VALUE: '蓝色'}, {NAME: '红色', VALUE: '红色'}, {NAME: '黄色', VALUE: '黄色'}],
                        defaultValue: {value: '蓝色'},
                        placeholder: '--请选择调度单等级--'
                    },
                    {
                        id: 'dispatchUrgency',
                        type: 'select',
                        label: '调度单缓急',
                        options: [{NAME: '正常', VALUE: '正常'}, {NAME: '加急', VALUE: '加急'}],
                        defaultValue: {value: '正常'},
                        placeholder: '--请选择调度单缓急--'
                    },
                    {
                        id: 'staffId',
                        type: 'text',
                        label: '处理人',
                        hidden: true
                    },
                    {
                        id: 'staffName',
                        type: 'text',
                        label: '处理人姓名',
                        disable: true
                    },
                    {
                        id: 'staffTel',
                        type: 'text',
                        label: '处理人电话',
                        disable: true
                    },
                    {
                        id: 'staffOrg',
                        type: 'text',
                        label: '处理人部门',
                        disable: true
                    },
                    {
                        id: 'issuer',
                        type: 'text',
                        label: '签发人',
                        disable: true
                    },
                    {
                        id: 'dispatchTitle',
                        type: 'text',
                        label: '标题',
                        required: true
                    },
                    {
                        id: 'dispatchSendOrgName',
                        type: 'text',
                        label: '发送单位',
                        required: true,
                        disable: true
                    },
                    {
                        id: 'dispatchSendOrg',
                        type: 'text',
                        label: '发送单位',
                        hidden: true
                    },
                    {
                        id: 'dispatchCopyOrg',
                        type: 'text',
                        label: '抄送单位',
                        hidden: true
                    },
                    {
                        id: 'dispatchCopyOrgName',
                        type: 'text',
                        label: '抄送单位'
                    },
                    {
                        id: 'dispatchText',
                        type: 'textarea',
                        label: '调度单正文',
                        required: true,
                        column: 12,
                        label_sm: 3,
                        label_md: 3,
                        content_sm: 9,
                        content_md: 9
                    },
                    {
                        id: 'dispatchRemark',
                        type: 'textarea',
                        label: '调单说明',
                        required: false,
                        column: 12,
                        label_sm: 3,
                        label_md: 3,
                        content_sm: 9,
                        content_md: 9
                    }
                ];
                this.setView("#dispDetail", new FormView({
                    config: {
                        elements: config_elements,
                        column: 1,
                        callback: {
                            onChange: function (obj) {
                                var param={};
                                param.cstOrdId = me.options.cstOrdId;
                                var value = this.getValueText(obj).value;
                                if (value == '是') {
                                    param.dispatchSource = 'onedry';
                                    var onedryDisInfo = operOrderAction.queryDispatchOrder(param).responseJSON.data;
                                    if (onedryDisInfo.success && onedryDisInfo.dispatchOrder.length > 0){
                                        actionType = 'update';
                                        me.setDispOrdValue(onedryDisInfo);
                                    }
                                }else if(value == '否'){
                                    //查询是否有二干调单信息
                                    param.dispatchSource = 'second';
                                    var secondDisInfo = operOrderAction.queryDispatchOrder(param).responseJSON.data;
                                    if (secondDisInfo.success && secondDisInfo.dispatchOrder.length > 0) {
                                        actionType = 'update';
                                        me.setDispOrdValue(secondDisInfo);
                                    }else{
                                        //如果没有二干调单信息，就查询客户订单表中关联的是否有调单编号
                                        actionType = 'add';
                                        var resMap = operOrderAction.querySecondDisNoByCstOrdId(param).responseJSON.data;
                                        if (resMap.success && resMap.secondDisNo != "") {
                                            $("#dispatchOrderNo").val(resMap.secondDisNo);
                                        }else{
                                            //如果客户订单表中没有关联的调单编号，取根据规则生成
                                            //获取调单编号
                                            me.getDispatchNumber();
                                        }
                                        //根据规则生成调单标题
                                        me.getDispatchTitle();
                                        me.refreshDispatchText();
                                    }
                                }
                            }
                        }
                    }
                }));
                this.getView("#dispDetail").on("viewRenderAfter", function () {
                    //抄送单位
                    $("#dispatchCopyOrgName").popedit({
                        initialData: {
                            'name': '--请选择抄送单位--',
                            'value': ''
                        },
                        open: function (e) {
                            var _this = $(this);
                            var _array = new Array();
                            var _value = $("#dispatchCopyOrg").val();
                            if (_value != null) {
                                _array = _value.split(",");
                            }
                            var options = {
                                url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/transferView',
                                height: 450,
                                width: 500,
                                modal: false,
                                draggable: false,
                                autoResizable: true,
                                viewOption: {
                                    flag: "transferStaff",
                                    valueArr: _array,
                                    nodeValues: _value,
                                    currentAreaId : userInfo.areaId,
                                    currentOrgId : userInfo.orgId,
                                    currentUserId : userInfo.userId,
                                    type: "schedule",
                                    multiselect: true
                                },
                                callback: function (popup, view) {
                                    popup.result.then(function (res) {
                                        var orgNames = '';
                                        var orgIds = '';
                                        var objType = '';
                                        var ids = new Array();
                                        var names = new Array();
                                        res.forEach(function (val, i) {
                                            if (i == 0) {
                                                orgNames = val.name;
                                            }
                                            orgIds = val.id;
                                            objType = val.objType;
                                            ids.push(val.id);
                                            names.push(val.name);
                                        });
                                        // _this.popedit('setValue', {name: orgNames, value: orgIds, objType: objType});
                                        $("#dispatchCopyOrg").val(ids.join(","));
                                        $("#dispatchCopyOrgName").val(names.join(","));
                                    }, function (e) {
                                        console.log('关闭了', e);
                                    });
                                }
                            };
                            var popup = fish.popupView(options);
                        }

                    });

                    $("#top_node_dispatchSendOrgName span").click(function () {
                        $("#dispatchCopyOrg").val('');
                    });

                    //低分辨率 全变一行了，没办法，手动设置吧
                    $("#top_node_dispatchOrderNo").css('width','50%');
                    $("#top_node_useGroupNo").css('width','50%');
                    $("#top_node_dispatchGrade").css('width','50%');
                    $("#top_node_dispatchUrgency").css('width','50%');
                    $("#top_node_staffName").css('width','50%');
                    $("#top_node_staffTel").css('width','50%');
                    $("#top_node_staffOrg").css('width','50%');
                    $("#top_node_issuer").css('width', '50%');
                });
            },
            initdispOrdInfo: function () {
                var that = this;
                var oldTitle = '';
                var oldText = '';
                var obj = new Object();
                actionType = 'add';
                obj.cstOrdId = that.options.cstOrdId;
                if(replenish){
                    $("#top_node_useGroupNo").css('display', 'none');
                }else {
                    if (oneDry) {
                        $("#top_node_useGroupNo").css('display', 'block');
                    } else {
                        $("#top_node_useGroupNo").css('display', 'none');
                    }
                }

                if(replenish){ //补单
                    //先查询有没有二干调单
                    obj.dispatchSource = 'second'; //二干调单
                    actionType = 'update';
                    var secondDisInfo = operOrderAction.queryDispatchOrder(obj).responseJSON.data;
                    if (secondDisInfo.success && secondDisInfo.dispatchOrder.length > 0) {
                        obj.dispatchOrderId = secondDisInfo.dispatchOrder[0].DISPATCH_ORDER_ID;
                        this.setDispOrdValue(secondDisInfo);
                    }else{
                        //如果没有就查询一干调单进行回填
                        obj.dispatchSource = 'onedry';
                        var onedryDisInfo = operOrderAction.queryDispatchOrder(obj).responseJSON.data;
                        if (onedryDisInfo.success && onedryDisInfo.dispatchOrder.length > 0){
                            obj.dispatchOrderId = onedryDisInfo.dispatchOrder[0].DISPATCH_ORDER_ID;
                            this.setDispOrdValue(onedryDisInfo);
                        }
                    }
                    //对调单部门信息进行处理
                    obj.state = '10A';
                    var ret = operOrderAction.queryDispatchDept(obj).responseJSON.data;
                    if (ret.success && ret.dispatchDept.length > 0) {
                        oldOrgInfo = ret.dispatchDept;
                    }
                }
                else {
                    //如果是一干来单则回填一干调单信息
                    if(oneDry){
                        obj.dispatchSource = 'second';
                        //查询一干来单关联的有没有二干调单
                        var secondDisInfo = operOrderAction.queryDispatchOrder(obj).responseJSON.data;
                        if (secondDisInfo.success && secondDisInfo.dispatchOrder.length > 0) {
                            actionType = 'update';
                            obj.dispatchOrderId = secondDisInfo.dispatchOrder[0].DISPATCH_ORDER_ID;
                            this.setDispOrdValue(secondDisInfo);

                        }else{
                            //如果没有就查询一干调单信息进行回填
                            obj.dispatchSource = 'onedry';
                            var onedryDisInfo = operOrderAction.queryDispatchOrder(obj).responseJSON.data;
                            if (onedryDisInfo.success && onedryDisInfo.dispatchOrder.length > 0){
                                obj.dispatchOrderId = onedryDisInfo.dispatchOrder[0].DISPATCH_ORDER_ID;
                                this.setDispOrdValue(onedryDisInfo);
                            }
                        }
                        //派单部门信息进行回填
                        var ret = operOrderAction.queryDispatchDept(obj).responseJSON.data;//派单信息
                        if (ret.success && ret.dispatchDept.length > 0) {
                            oldOrgInfo = ret.dispatchDept;
                        }
                    }else {
                        //不是一干来单查询是否有二干调单信息
                        obj.dispatchSource = 'second';
                        var secondDisInfo = operOrderAction.queryDispatchOrder(obj).responseJSON.data;
                        if (secondDisInfo.success && secondDisInfo.dispatchOrder.length > 0) {
                            actionType = 'update';
                            obj.dispatchOrderId = secondDisInfo.dispatchOrder[0].DISPATCH_ORDER_ID;
                            this.setDispOrdValue(secondDisInfo);
                        }else{
                            actionType = 'add';
                            //如果没有二干调单信息，就查询客户订单表中关联的是否有调单编号
                            var resMap = operOrderAction.querySecondDisNoByCstOrdId(obj).responseJSON.data;
                            if (resMap.success && resMap.secondDisNo != "") {
                                $("#dispatchOrderNo").val(resMap.secondDisNo);
                            }else{
                                //如果客户订单表中没有关联的调单编号，取根据规则生成
                                //获取调单编号
                                this.getDispatchNumber();
                            }
                            //根据规则生成调单标题
                            this.getDispatchTitle();
                            this.refreshDispatchText();
                        }
                        //派单部门信息进行回填
                        var ret = operOrderAction.queryDispatchDept(obj).responseJSON.data;//派单信息
                        if (ret.success && ret.dispatchDept.length > 0) {
                            oldOrgInfo = ret.dispatchDept;
                        }
                    }
                }
                $("#staffName").val(userInfo.userName);
                $("#staffTel").val(userInfo.userPhone);
                $("#staffOrg").val(userInfo.orgName);
                that.resize();
                //签发人
                var areaId = userInfo.areaId;
                var result = operOrderAction.queryIssuer(areaId).responseJSON.data;
                this.$("#issuer").val(result.ISSUER_NAME);
            },
            intDispOrgInfo: function (index, batchSaveFlag) {
                if (oldOrgInfo != null && oldOrgInfo.length > 0) {
                    var sendOrgArr = new Array();
                    var sendOrgIdArr = new Array();
                    for (var i = 0; i < oldOrgInfo.length; i++) {
                        if (circuitInfo[index].SRV_ORD_ID == oldOrgInfo[i].SRV_ORD_ID) {
                            var temp = oldOrgInfo[i];
                            if(temp.TO_BDW != "否"){
                                $("input:radio[name='ynRadio"+index+"'][value='是']").attr("checked",true);
                                $("#top_node_masterRegionName"+ index).css('display','block');
                                $("#top_node_masterReqTime"+ index).css('display','block');
                                $("#top_node_assistRegionName"+ index).css('display','block');
                                $("#top_node_assistReqTime"+ index).css('display','block');
                            }else{
                                $("input:radio[name='ynRadio"+index+"'][value='否']").attr("checked",true);
                                $("#top_node_masterRegionName"+ index).css('display','none');
                                $("#top_node_masterReqTime"+ index).css('display','none');
                                $("#top_node_assistRegionName"+ index).css('display','none');
                                $("#top_node_assistReqTime"+ index).css('display','none');
                            }
                            $("#masterRegion" + index).val(temp.MASTER_REGION);
                            $("#masterRegionName" + index).val(temp.MASTER_REGION_NAME);
                            $("#masterReqTime" + index).datetimepicker("value", temp.MASTER_REQ_TIME);
                            $("#assistRegion" + index).val(temp.ASSIST_REGION);
                            $("#assistRegionName" + index).val(temp.ASSIST_REGION_NAME);
                            $("#assistReqTime" + index).datetimepicker("value", temp.ASSIST_REQ_TIME);
                            /**
                             * 主调区域定单如果有正在执行中，则不能修改区域，也不能修改是否下发本地网
                             */
                            temp.areaId = temp.MASTER_REGION;
                            var ifModifyFlag = operOrderAction.ifModifyMainArea(temp).responseJSON.data;
                            if (!ifModifyFlag) {
                                $("#masterRegionName" + index).popedit('disable'); // 主调局不可修改
                                $("#toBdw"+ index).find("input:radio").attr("disabled", "disabled");
                            }
                            /**
                             * 辅调区域定单如果有正在执行中，则是否下发本地网不能修改
                             */
                            if(temp.ASSIST_REGION != "" && temp.ASSIST_REGION != null){
                                for (var l = 0; l < temp.ASSIST_REGION.split(",").length ; l++) {
                                    temp.areaId = temp.ASSIST_REGION.split(",")[l];
                                    var ifModifyFlag = operOrderAction.ifModifyMainArea(temp).responseJSON.data;
                                    if (!ifModifyFlag) {
                                        $("#toBdw"+ index).find("input:radio").attr("disabled", "disabled");
                                        break;
                                    }
                                }
                            }
                            //专业数据制作专业回显
                            this.getView("#orgForm" + index).initValue("netManage" + index, {value: temp.NETMANAGE.split(",")});
                            var value = "否";
                            value = temp.RES_ALLOCATE;
                            //是否转资源分配回显
                            this.getView("#orgForm" + index).initValue("resAllocate" + index, {value: value});
                            if (value == "是") {
                                //资源分配专业回显
                                $("#top_node_specialty" + index).css('display', 'block');
                                this.getView("#orgForm" + index).initValue("specialty" + index, {value: temp.SPECIALTY.split(",")});
                            } else {
                                $("#top_node_specialty" + index).css('display', 'none');
                            }
                            //是否转资源施工回显
                            this.getView("#orgForm" + index).initValue("resMakeAllocate" + index, {value: value});
                            if (value == "是") {
                                //资源分配专业回显
                                $("#top_node_resMake" + index).css('display', 'block');
                                this.getView("#orgForm" + index).initValue("resMake" + index, {value: temp.RESCONSTRUCTION.split(",")});
                            } else {
                                $("#top_node_resMake" + index).css('display', 'none');
                            }
                            //数据制作是否指定到人回显
                            this.getView("#orgForm" + index).initValue("isAssignPerson" + index, {value: temp.ASS_MKDATA_PERSON});
                            if (temp.ASS_MKDATA_PERSON == '是'){
                                //处理数据制作专业指定人信息回显
                                var mkDataInfo = temp.NETMANAGE;
                                if (mkDataInfo != null && mkDataInfo != ''){
                                    var mkDataInfoArr = mkDataInfo.split(",");
                                    var person = eval("("+temp.MKDATA_PERSON+")");
                                    var personId = eval("("+temp.MKDATA_PERSON_ID+")");
                                    for (var n = 0; n < mkDataInfoArr.length; n ++){
                                        var key = mkDataInfoArr[n];
                                        $("#top_node_" + key + "Name" + index).css('display', 'block');
                                        $("#"+ key + "Name" + index).popedit('setValue', {name: person[key], value: personId[key]});
                                    }
                                }
                            }
                            //资源分配是否指定到人回显
                            this.getView("#orgForm" + index).initValue("isResAss" + index, {value: temp.IS_ASSIGN_PERSON});
                            if (temp.IS_ASSIGN_PERSON == '是'){
                                //资源分配指定人信息回显
                                var specialtyInfo = temp.SPECIALTY;
                                if (specialtyInfo != null && specialtyInfo != ''){
                                    var specialtyInfoArr = specialtyInfo.split(",");
                                    var person = eval("("+temp.RES_ASS_PERSION+")");
                                    var personId = eval("("+temp.RES_ASS_PERSION_ID+")");
                                    for (var n = 0; n < specialtyInfoArr.length; n ++){
                                        var key = specialtyInfoArr[n];
                                        $("#top_node_" + key + "Person" + index).css('display', 'block');
                                        $("#"+ key + "Person" + index).popedit('setValue', {name: person[key], value: personId[key]});
                                    }
                                }
                            }
                            //资源施工是否指定到人回显
                            this.getView("#orgForm" + index).initValue("isResMake" + index, {value: temp.IS_RES_MAKE});
                            if (temp.IS_RES_MAKE == '是'){
                                //资源施工指定人信息回显IS_RES_MAKE
                                var resMakeInfo = temp.RESCONSTRUCTION;
                                if (resMakeInfo != null && resMakeInfo != ''){
                                    var resMakeInfoArr = resMakeInfo.split(",");
                                    var person = eval("("+temp.RES_MAKE_PERSION+")");
                                    var personId = eval("("+temp.RES_MAKE_PERSION_ID+")");
                                    for (var n = 0; n < resMakeInfoArr.length; n ++){
                                        var key = resMakeInfoArr[n];
                                        $("#top_node_" + key + "Make" + index).css('display', 'block');
                                        $("#"+ key + "Make" + index).popedit('setValue', {name: person[key], value: personId[key]});
                                    }
                                }
                            }
                            //完工汇总指定到人回显
                            this.getView("#orgForm" + index).initValue("isComplatePerson" + index, {value: temp.IS_COMPLATE_PERSON});
                            if (temp.IS_COMPLATE_PERSON == '是'){
                                $("#complatePerson" + index).popedit('setValue', {name: temp.COMPLATE_PERSON, value: temp.COMPLATE_PERSON_ID});
                            }else{
                                $("#complatePerson" + index).val('');
                            }
                            //发送单位回显
                            if (sendOrgArr.indexOf(temp.MASTER_REGION_NAME) == -1){
                                sendOrgArr.push(temp.MASTER_REGION_NAME);
                                sendOrgIdArr.push(temp.MASTER_REGION);
                            }
                            if (sendOrgArr.indexOf(temp.ASSIST_REGION_NAME) == -1){
                                sendOrgArr.push(temp.ASSIST_REGION_NAME);
                                sendOrgIdArr.push(temp.ASSIST_REGION);
                            }
                            if (sendOrgArr.length > 0 ){
                                $('#dispatchSendOrgName' ).val(sendOrgArr.join(','));
                                $('#dispatchSendOrg' ).val(sendOrgIdArr.join(','));
                            }
                            $('#isResAss' + index).val(temp.IS_ASSIGN_PERSON);
                            $('#isResMake' + index).val(temp.IS_RESMAKE_PERSON);

                            //控制专业是否可以删除修改新增
                            var specialtyArr = [];
                            var netManageArr = [];
                            var orderId = circuitInfo[index].ORDER_ID + '';
                            var childOrders = operOrderAction.qryChildOrder(orderId + '').responseJSON.data;
                            if (childOrders.length > 0) { //执行中的子流程
                                for (var j = 0; j < childOrders.length; j++) {
                                    var code = childOrders[j].PARENT_ORDER_CODE;
                                    if(code == 'SPECIALTY') {
                                        specialtyArr.push(childOrders[j].SPECIALTY);
                                    } else if(code == 'NETMANAGE'){
                                        netManageArr.push(childOrders[j].SPECIALTY);
                                    }
                                }
                                //有正在执行中的资源分配子流程，不可修改是否转资源分配
                                if(specialtyArr.length > 0){
                                    this.getView("#orgForm" + index).disable("resAllocate" + index);
                                }
                                this.disableMultiSel(index, 'specialty', specialtyArr); //子流程的专业不能删
                                this.disableMultiSel(index, 'netManage', netManageArr); //子流程的数据制作不能删
                            }
                            //补单
                            if(replenish) {
                                //二干调度环节
                                var secScheArr = ['SECONDARY_SCHEDULE','SECONDARY_SCHEDULE_2'];
                                //二干资源分配环节
                                var secResArr = ['SEC_SOURCE_DISPATCH_2', 'SEC_SOURCE_DISPATCH'];
                                //待数据制作与本地调度环节
                                var dataLocArr = ['TO_DATA_CREATE_AND_SCHEDULE', 'TO_DATA_CREATE_AND_SCHEDULE_2'];
                                if(secScheArr.indexOf(circuitInfo[index].MAIN_TACHE_CODE) > -1){ //二干调度环节
                                    //不用操作
                                }else{
                                    this.getView("#orgForm" + index).disable("resAllocate" + index);//资源分配不能修改
                                    if(dataLocArr.indexOf(circuitInfo[index].MAIN_TACHE_CODE) > -1){ //待数据制作与本地调度环节
                                        $("#masterRegionName" + index).popedit('disable'); // 主调局不可选
                                        this.getView("#orgForm" + index).disable("masterReqTime" + index);
                                        disableAssist = true; // 辅调已有不能删
                                        if(temp.ASSIST_REGION != null && temp.ASSIST_REGION != ''){
                                            this.getView("#orgForm" + index).disable("assistReqTime" + index);
                                        }
                                        var input = $("#toBdw"+ index).find("input:radio"); //是否派发本地网不可选
                                        if(temp.TO_BDW == '是') {
                                            input.attr("disabled", "disabled");
                                        }
                                        this.getView("#orgForm" + index).disable("specialty" + index);
                                        this.$("#specialty" + index + "_multi").addClass("combobox-readonly disabled");
                                    }else if(secResArr.indexOf(circuitInfo[index].MAIN_TACHE_CODE) > -1){ //二干资源分配

                                    }else{
                                        //如果已经过了待数据制作与本地调度的电路所有不能操作
                                        //这里非二干调度，非待数据制作与本地调度，非二干资源分配环节，上面已经有过滤了
                                        this.getView("#orgForm" + index).disable();
                                    }
                                }

                            }

                        }
                    }
                } else {
                    var aReqTime = circuitInfo[index].A_REQ_TIME;
                    var zReqTime = circuitInfo[index].Z_REQ_TIME;
                    var masterReqTime = '';
                    var assistReqTime = '';
                    if (aReqTime != null && aReqTime != '') {
                        masterReqTime = aReqTime;
                    }
                    if (zReqTime != null && zReqTime != '') {
                        assistReqTime = aReqTime;
                    }
                    $("#masterReqTime" + index).val(masterReqTime);
                    $("#assistReqTime" + index).val(assistReqTime);
                    this.setSendOrg();
                }
                if($("#assistRegion" + index).val() != ''){
                    $("#top_node_assistReqTime"+index+" .form-group").addClass('required');
                }else{
                    $("#top_node_assistReqTime"+index+" .form-group").removeClass('required');
                }
                //判断是否为拆机单，如果为拆机单，第一次起草调单时，派发单位不会回填到调单信息中，
                //在主调局信息回填完成后，将派发单位回填到调单信息中
                if (this.options.parentOption.activeType == '102'){
                    me.setSendOrg();
                }
                //判断是否为批量处理主辅调信息，如果是批量处理，在回显祝福调信息后，将派发单位回填到调单信息中
                if (batchSaveFlag == 'batchSave'){
                    me.setSendOrg();
                }
            },
            disableMultiSel: function(index, id, disableArr){ //多选框中不允许修改某些选项
                if(disableArr!=null) {
                    var lis = $('#' + id + index + '_multi').find('li');
                    var sel = $('#' + id + index).find('option');
                    var disableOption;
                    var arr = [];
                    for (var m = 0; m < disableArr.length; m++) {
                        for (var n = 0; n < sel.length; n++) {
                            if (disableArr[m] == sel[n].value) {
                                arr.push(sel[n].innerText); //中文，不去查表了，直接HTML取
                            }
                        }
                    }
                    disableOption = arr.join(',');
                    for (var i = 0; i < lis.length - 1; i++) { //最后一个是空的，-1
                        var val = $(lis[i]).children('.overflow-ellipsis')[0].innerText;
                        if (disableOption.indexOf(val) > -1) {
                            $(lis[i]).find('button').remove(); //去掉删除按钮
                        }
                    }
                }
            },
            setDispOrdValue: function (data) {
                var that = this;
                var dispatchOrder = data.dispatchOrder[0];
                $("#dispatchId").val(dispatchOrder.DISPATCH_ORDER_ID);
                $("#dispatchOrderNo").val(dispatchOrder.DISPATCH_ORDER_NO);
                if (oneDry) {
                    if (dispatchOrder.DISPATCH_ORDER_NO.indexOf("中国联通网调字") > -1) {
                        this.getView("#dispDetail").initValue('useGroupNo', {value: '是'});
                    } else {
                        this.getView("#dispDetail").initValue('useGroupNo', {value: '否'});
                    }
                }
                this.getView("#dispDetail").initValue("dispatchGrade", {value: dispatchOrder.DISPATCH_GRADE});
                this.getView("#dispDetail").initValue("dispatchUrgency", {value: dispatchOrder.DISPATCH_URGENCY});
                $("#dispatchTitle").val(dispatchOrder.DISPATCH_TITLE);
                $("#dispatchSendOrg").val(dispatchOrder.DISPATCH_SEND_ORG);
                $("#dispatchSendOrgName").val(dispatchOrder.DISPATCH_SEND_ORG_NAME);
                $("#dispatchCopyOrg").val(dispatchOrder.DISPATCH_COPY_ORG);
                $("#dispatchCopyOrgName").val(dispatchOrder.DISPATCH_COPY_ORG_NAME);
                $("#dispatchText").val(dispatchOrder.DISPATCH_TEXT);
                $("#dispatchRemark").val(dispatchOrder.REMARK);

                orderDetailsAction.qryDispatchAttachForDraftSchedule(dispatchOrder.DISPATCH_ORDER_ID.toString(), function (data) {
                    for (var i = 0; i < data.length; i++) {
                        var fileObj = {};
                        var obj = data[i];
                        fileObj.attachInfoId = obj.ATTACH_INFO_ID;
                        fileObj.fileId = obj.FILE_ID+"."+obj.FILE_TYPE;
                        fileObj.filePath = obj.FILE_PATH;
                        fileObj.fileName = obj.FILE_NAME;
                        fileObj.fileSize = obj.FILE_SIZE;
                        fileObj.fileType = obj.FILE_TYPE;

                        that.getView("#fileGrid").addRowData(fileObj);
                        that.attachs.push(fileObj.attachInfoId);
                        $('.inline-remove').off('click').on('click', function (e) {
                            var rowid = $(this).closest("tr.jqgrow").attr("id");
                            var data = that.getView("#fileGrid").getRowData(rowid);
                            that.getView("#fileGrid").delRow(rowid);
                            if(that.attachs.indexOf(data.attachInfoId) > -1){
                                that.delAttach.push(data.attachInfoId);
                            }
                        });
                        $('.inline-download').off('click').on('click', function (e) {
                            var rowid = $(this).closest("tr.jqgrow").attr("id");
                            var data = that.getView("#fileGrid").getRowData(rowid);
                            if(data.fileId){
                                var param = new Object();
                                param.fileName =data.fileName;
                                param.filePath = data.filePath;
                                param.fileId = data.fileId;
                                orderDetailsAction.downFile("localScheduleLT/orderDetails/fileDownload.spr",param);
                            }
                        });
                    }
                });
            },
            //将获取调单编号和调单内容的方法拆分开
            //获取调单编号
            getDispatchNumber:function(){
                var param = {};
                param.cstOrdId = this.options.cstOrdId;
                operOrderAction.getDispatchNumber(param, function(res){
                    if (res.success){
                        var date = new Date();
                        dispatchOrderNo = res.sign + '[' + date.getFullYear() + ']' + res.message + '号';
                        $('#dispatchOrderNo').val(dispatchOrderNo);
                        //然后将调单编号填到客户订单表中
                        param.dispatchOrderNo = dispatchOrderNo;
                        //将调单编号根据cstOrdId回填到客户订单表中
                        operOrderAction.linkDisOrdNoToCstOrd(param).responseJSON.data;
                    }
                });
            },
            //获取拼接调单标题的信息
            getDispatchTitle:function(){
                var param = {};
                param.cstOrdId = this.options.cstOrdId;
                operOrderAction.getDispatchTitle(param, function(res){
                    if (res.success){
                        var title = "关于【";
                        if(res.custName!=undefined && res.custName != null && res.custName != '') {
                            title += res.custName + "】【";
                        }
                        title += res.operateType + "】【" + N + "条】【" + res.productType + "】的通知";
                        $('#dispatchTitle').val(title);
                    }
                });
            },
            setSendOrg: function () {
                var arr = new Array();
                var arrIds = new Array();
                for (var i = 0; i < N; i++) {
                    var netManage = $("#netManage" + i).val();
                    if (netManage) {
                        for (var j = 0; j < netManage.length; j++) {
                            var sel = $("#netManage" + i).find('option');
                            for (var k = 0; k < sel.length; k++) {
                                if (netManage[j] == sel[k].value) {
                                    arr.push(sel[k].innerText);
                                    arrIds.push(netManage[j]);
                                }
                            }
                        }
                    }
                    var val = $("input:radio[name='ynRadio" + i + "']:checked").val();
                    if (val == '是') {
                        var masterRegionName = $("#masterRegionName" + i).val();
                        var masterRegion = $("#masterRegion" + i).val();
                        if (masterRegion) {
                            arr.push(masterRegionName);
                            arrIds.push(masterRegion);
                        }
                        var assistRegion = $("#assistRegion" + i).val();
                        var assistRegionName = $("#assistRegionName" + i).val();
                        if (assistRegion) {
                            var assistRegionArr = assistRegion.split(",");
                            var assistRegionNameArr = assistRegionName.split(",");
                            for (var k in assistRegionArr) {
                                arrIds.push(assistRegionArr[k]);
                            }
                            for (var k in assistRegionNameArr) {
                                arr.push(assistRegionNameArr[k]);
                            }
                        }
                    }
                }
                if (arr) {
                    var r = arr.filter(function (element, index, self) {
                        return self.indexOf(element) === index;
                    });
                    $("#dispatchSendOrgName").val(r.join(','));
                }
                if (arrIds) {
                    var rIds = arrIds.filter(function (element, index, self) {
                        return self.indexOf(element) === index;
                    });
                    $("#dispatchSendOrg").val(rIds.join(','));
                }
            },
            refreshDispatchText: function () {
                var me = this;
                var cstOrdId = me.options.cstOrdId + '';
                var param = {};
                param.cstOrdId = cstOrdId;
                var orderText = '';
                for (i = 0; i < circuitInfo.length; i++) {
                    orderText = orderText + (i + 1) + '、电路编号：' + circuitInfo[i].CIRCUITCODE + '\n业务号码：' + circuitInfo[i].SERIAL_NUMBER + '\n业务订单号：' + circuitInfo[i].TRADE_ID + '\nA端城市：' + circuitInfo[i].A_CITY +
                        '\nZ端城市：' + circuitInfo[i].Z_CITY + '\n';
                }
                orderText = orderText + '由主调局负责对传输电路进行端到端BER测试，相关分公司配合。\n' +
                    '传输质量符合要求后，由相关分公司按电路调令要求时限进行反馈，并通知本公司业务受理部门。测试过程中如有问题，请及时反馈网管中心。'
                $('#dispatchText').val(orderText);
            },
            initFileUpdate: function () {
                var that = this;
                var opt = {
                    colModel: [
                        {name: 'attachInfoId', hidden: true},
                        {name: 'fileId', hidden: true},
                        {name: 'fileName', label: '文件名称', width: 160, sortable: false},
                        {name: 'fileSize', label: '大小', width: 40},
                        {name: 'fileType', label: '类型', width: 40, sortable: false},
                        {
                            name: 'action', label: '操作', width: 100, formatter: 'actions',
                            formatoptions: {
                                editbutton: false,
                                delbutton: false,
                                inlineButtonAdd: function (rowdata) {
                                    return [{ //可以给actions类型添加多个icon图标,事件自行控制
                                        id: "jRemoveButton", //每个图标的id规则为id+"_"+rowid
                                        className: "inline-remove",//每个图标的class
                                        icon: "glyphicon glyphicon-remove-circle",//图标的样子
                                        title: "删除"//鼠标移上去显示的内容
                                    }]
                                }
                            }
                        },
                        {
                            name: 'action2', label: '操作', width: 100, formatter: 'actions',
                            formatoptions: {
                                editbutton: false,
                                delbutton: false,
                                inlineButtonAdd: function (rowdata) {
                                    return [{ //可以给actions类型添加多个icon图标,事件自行控制
                                        id: "jDownloadButton", //每个图标的id规则为id+"_"+rowid
                                        className: "inline-download",//每个图标的class
                                        icon: "glyphicon glyphicon-download",//图标的样子
                                        title: "下载"//鼠标移上去显示的内容
                                    }]
                                }
                            }
                        }
                    ],
                    width: '100%'
                };
                this.setView("#fileGrid", new GridView({
                    config: {
                        grid: opt
                    }
                }));

                var me = this;
                me.$('#selectFiles').fileupload({
                    dataType: 'json',
                    autoUpload: false,
                    add: function (e, data) {
                        var fileObj = {};
                        var obj = data.files[0];
                        fileObj.fileName = obj.name.split(".")[0];
                        fileObj.fileSize = (obj.size / 1024.0).toFixed(2) + "KB";
                        fileObj.fileType = obj.name.split(".")[obj.name.split(".").length - 1];
                        if ((obj.size / 1024.0).toFixed(2) >= 20 * 1024) {
                            fish.warn('上传文件不能超过20M！');
                            return;
                        }
                        me.getView("#fileGrid").addRowData(fileObj);
                        if (FILES === null) {
                            FILES = data;
                        } else {
                            FILES.files.push(obj);
                        }
                        $('.inline-remove').off('click').on('click', function (e) {
                            var rowid = $(this).closest("tr.jqgrow").attr("id");
                            var data = me.getView("#fileGrid").getRowData(rowid);
                            me.getView("#fileGrid").delRow(rowid);
                            if(data.fileId != null){

                            }else{
                                var i;
                                $.each(FILES.files, function (index, file) {
                                    if (file.name === data.fileName + "." + data.fileType) {
                                        i = index;
                                    }
                                });
                                FILES.files.splice(i, 1);
                            }

                        });
                    },
                    always: function (e, data) {
                        me.$("#draftScheduleDiv").unblockUI().data('blockui-content', false);
                        if (data.result.success) {
                            fish.toast('success', data.result.message);
                            $("#cancelThisBtn").click(); //只关闭当前页面
                            if(replenish){
                                me.popup.close();
                            }
                        } else {
                            fish.toast('error', data.result.message);
                        }
                    },
                });
            },
            fileUpdate: function (params) {
                FILES.url = URL + "/localScheduleLT/FlieUpdateController/uploadFiles.spr";
                FILES.formData = {
                    params: JSON.stringify(params)
                };
                FILES.submit();
            },
            resize: function () {
                $('#top_node_dispatchTitle').css('width', '100%');
                $('#top_node_dispatchTitle label').css('width', '12.5%');
                $('#top_node_dispatchTitle .res_form_content').css('width', '87.5%');
                $('#top_node_dispatchSendOrgName').css('width', '100%');
                $('#top_node_dispatchSendOrgName label').css('width', '12.5%');
                $('#top_node_dispatchSendOrgName .res_form_content').css('width', '87.5%');
                $('#top_node_dispatchCopyOrgName').css('width', '100%');
                $('#top_node_dispatchCopyOrgName label').css('width', '12.5%');
                $('#top_node_dispatchText ').css('width', '100%');
                $('#top_node_dispatchText label').css('width', '12.5%');
                $('#top_node_dispatchText textarea').css('height', '200px');
                $('#top_node_dispatchText .clear-white').css('width', '87.5%');
                //add by wang.gang2 调单说明
                $('#top_node_dispatchRemark').css('width', '100%');
                $('#top_node_dispatchRemark label').css('width', '12.5%');
                $('#top_node_dispatchRemark textarea').css('height', '100px');
                $('#top_node_dispatchRemark .clear-white').css('width', '87.5%');

                $("#top_node_dispatchCopyOrgName .res_form_content").addClass('input-group');
                $("#top_node_dispatchCopyOrgName .res_form_content").css('padding', '0 10px');
                $("#top_node_dispatchCopyOrgName .res_form_content").css('width', '87.5%');

                $("#fileUpdateDiv table").css('width', '100%');
            },
            submit: function () {
                var message = '保存调单中...';
                if(replenish){
                    message = '补单中...';
                }
                this.$("#draftScheduleDiv").blockUI({message: message}).data('blockui-content', true);
                var that = this;
                var obj = new Object();
                var dispOrgList = new Array();
                var srvOrdIds = new Array();
                var orderIds = new Array();
                for (var i = 0; i < N; i++) {
                    var temp = new Object();
                    temp.srvOrdId = circuitInfo[i].SRV_ORD_ID;
                    temp.cstOrdId = this.options.cstOrdId;
                    var toBdw = $("input:radio[name='ynRadio"+i+"']:checked").val();
                    temp.toBdw = toBdw;
                    if(toBdw == "否"){
                        temp.masterRegion = '';
                        temp.masterRegionName = '';
                        temp.masterRegion = '';
                        temp.assistRegionName = '';
                        temp.masterReqTime = '';
                        temp.assistReqTime = '';
                    }else {
                        temp.masterRegion = $("#masterRegion" + i).val();
                        temp.masterRegionName = $("#masterRegionName" + i).val();
                        if (temp.masterRegionName == '') {
                            temp.masterRegion = '';
                        }
                        if (temp.masterRegion == '') {
                            that.$("#draftScheduleDiv").unblockUI().data('blockui-content', false);
                            ngc.info("请选择电路" + (i + 1) + " 的主调局！");
                            return false;
                        }
                        temp.assistRegion = $("#assistRegion" + i).val();
                        temp.assistRegionName = $("#assistRegionName" + i).val();
                        if (temp.assistRegionName == '') {
                            temp.assistRegion = '';
                        }
                        temp.masterReqTime = $("#masterReqTime" + i).val();
                        if (temp.masterReqTime == '') {
                            that.$("#draftScheduleDiv").unblockUI().data('blockui-content', false);
                            ngc.info("请选择电路" + (i + 1) + " 的主调局完成时间！");
                            return false;
                        }
                        temp.assistReqTime = $("#assistReqTime" + i).val();
                        if (temp.assistRegion != '' && (temp.assistReqTime == '')) {
                            that.$("#draftScheduleDiv").unblockUI().data('blockui-content', false);
                            ngc.info("请选择电路" + (i + 1) + " 的辅调局完成时间！");
                            return false;
                        }
                    }
                    //数据制作是否指定到人
                    var isAssPerMkData = $('#isAssignPerson' + i).val();
                    if (isAssPerMkData == '是'){
                        //获取数据制作选择的专业
                        var netManageInfo = $("#netManage" + i).val();
                        if (netManageInfo == null){
                            that.$("#draftScheduleDiv").unblockUI().data('blockui-content', false);
                            ngc.info("请先选择电路" + (i + 1) + " 的数据制作！");
                            return false;
                        }else{
                            for (var m = 0; m < netManageInfo.length; m++){
                                var name = $("#" + netManageInfo[m] + "Name" + i).popedit('getValue');
                                if (name == null || name == '' || name.value == null || name.value == ''){
                                    that.$("#draftScheduleDiv").unblockUI().data('blockui-content', false);
                                    ngc.info("请完成电路" + (i + 1) + " 的数据制作指定人信息！");
                                    return false;
                                }
                            }
                        }
                    }
                    temp.resAllocate = $("#resAllocate" + i).val();
                    temp.resMakeAllocate = $("#resMakeAllocate" + i).val();
                    //完工汇总是否指定到人
                    var isComplatePerson = $('#isComplatePerson' + i).val();
                    if (isComplatePerson == '是'){
                        var complatePerson = $('#complatePerson' + i).popedit('getValue');
                        if (complatePerson == null || complatePerson == '' || complatePerson.value == null || complatePerson.value == ''){
                            that.$("#draftScheduleDiv").unblockUI().data('blockui-content', false);
                            ngc.info("请选择电路" + (i + 1) + " 的完工汇总指定人！");
                            return false;
                        }
                    }
                    //是否转资源分配
                    if (temp.resAllocate == "是") {
                        temp.specialty = $("#specialty" + i).val() != null ? $("#specialty" + i).val().join(',') : '';
                        if (temp.specialty == '') {
                            that.$("#draftScheduleDiv").unblockUI().data('blockui-content', false);
                            ngc.info("请选择电路" + (i + 1) + " 的资源分配！");
                            return false;
                        }
                        //资源分配是否指定到人
                        var isResAssign = $('#isResAss' + i).val();
                        if (isResAssign == '是'){
                            //获取资源分配信息
                            var specialtyInfo = $("#specialty" + i).val();
                            if(specialtyInfo != null){
                                for (var m = 0; m < specialtyInfo.length; m++){
                                    var person = $("#" + specialtyInfo[m] + "Person" + i).popedit('getValue');
                                    if (person == null || person == '' || person.value == null || person.value == ''){
                                        that.$("#draftScheduleDiv").unblockUI().data('blockui-content', false);
                                        ngc.info("请完成电路" + (i + 1) + " 的资源分配指定人信息！");
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                    //是否转资源施工
                    if (temp.resMakeAllocate == "是") {
                        temp.resMake = $("#resMake" + i).val() != null ? $("#resMake" + i).val().join(',') : '';
                        if (temp.resMake == '') {
                            that.$("#draftScheduleDiv").unblockUI().data('blockui-content', false);
                            ngc.info("请选择电路" + (i + 1) + " 的资源施工！");
                            return false;
                        }
                        //资源施工是否指定到人
                        var isResMake = $('#isResMake' + i).val();
                        if (isResMake == '是'){
                            //获取资源分配信息
                            var resMakeInfo = $("#resMake" + i).val();
                            if(resMakeInfo != null){
                                for (var m = 0; m < resMakeInfo.length; m++){
                                    var person = $("#" + resMakeInfo[m] + "Make" + i).popedit('getValue');
                                    if (person == null || person == '' || person.value == null || person.value == ''){
                                        that.$("#draftScheduleDiv").unblockUI().data('blockui-content', false);
                                        ngc.info("请完成电路" + (i + 1) + " 的资源施工指定人信息！");
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                    temp.netManage = $("#netManage" + i).val() != null ? $("#netManage" + i).val().join(',') : '';
                    temp.resMake = $("#resMake" + i).val() != null ? $("#resMake" + i).val().join(',') : '';
                    if((temp.netManage == '')){
                        var flagNum = -1;
                        if (!(circuitInfo[i].SERVICE_ID == '10000008' || this.options.parentOption.activeType == '102')){ //如果非mv10000008数据制作必填
                            flagNum = i;
                        }
                        if (flagNum != -1){
                            that.$("#draftScheduleDiv").unblockUI().data('blockui-content', false);
                            ngc.info("请选择电路" + (flagNum + 1) + " 的数据制作！");
                            return false;
                        }
                    }
                    //封装数据制作 资源分配 资源施工
                    //是否数据制作指定到人
                    if (isAssPerMkData == '是'){
                        temp.isAssPerMkData = isAssPerMkData;
                        //封装各专业对应的指定人
                        var netManageInfo = $("#netManage" + i).val();
                        if (netManageInfo != null){
                            var mkDataPerson = {};
                            var mkDataPersonId = {};
                            for (var m = 0; m < netManageInfo.length; m++){
                                var key = netManageInfo[m];
                                mkDataPerson[key] = $("#" + key + "Name" + i).val();
                                mkDataPersonId[key] = $("#" + key + "Name" + i).popedit('getValue').value;
                            }
                            temp.mkDataPerson = JSON.stringify(mkDataPerson);
                            temp.mkDataPersonId = JSON.stringify(mkDataPersonId);
                        }
                    } else {
                        temp.isAssPerMkData = isAssPerMkData;
                        temp.mkDataPerson = '';
                        temp.mkDataPersonId = '';
                    }
                    //是否转资源分配
                    if (temp.resAllocate == '是'){
                        var isResAssign = $('#isResAss' + i).val();
                        if (isResAssign == '是'){
                            temp.isResAssign = isResAssign;
                            //封装不同专业资源分配指定人信息
                            var specialtyInfo = $("#specialty" + i).val();
                            if (specialtyInfo != null){
                                var resAssPerson = {};
                                var resAssPersonId = {};
                                for (var m = 0; m < specialtyInfo.length; m++){
                                    var key = specialtyInfo[m];
                                    resAssPerson[key] = $("#" + key + "Person" + i).val();
                                    resAssPersonId[key] = $("#" + key + "Person" + i).popedit('getValue').value;
                                }
                                temp.resAssPerson = JSON.stringify(resAssPerson);
                                temp.resAssPersonId = JSON.stringify(resAssPersonId);
                            }
                        }else{
                            temp.isResAssign = isResAssign;
                            temp.resAssPerson = '';
                            temp.resAssPersonId = '';
                        }
                    }else{
                        temp.isResAssign = '';
                        temp.resAssPerson = '';
                        temp.resAssPersonId = '';
                    }
                    //是否转资源施工
                    if (temp.resMakeAllocate == '是'){
                        var isResMake = $('#isResMake' + i).val();
                        if (isResMake == '是'){
                            temp.isResMake = isResMake;
                            //封装不同专业资源施工指定人信息
                            var resMakeInfo = $("#resMake" + i).val();
                            if (resMakeInfo != null){
                                var resMakePerson = {};
                                var resMakePersonId = {};
                                for (var m = 0; m < resMakeInfo.length; m++){
                                    var key = resMakeInfo[m];
                                    resMakePerson[key] = $("#" + key + "Make" + i).val();
                                    resMakePersonId[key] = $("#" + key + "Make" + i).popedit('getValue').value;
                                }
                                temp.resMakePerson = JSON.stringify(resMakePerson);
                                temp.resMakePersonId = JSON.stringify(resMakePersonId);
                            }
                        }else{
                            temp.isResMake = isResMake;
                            temp.resMakePerson = '';
                            temp.resMakePersonId = '';
                        }
                    }else{
                        temp.isResMake = '';
                        temp.resMakePerson = '';
                        temp.resMakePersonId = '';
                    }

                    //完工汇总是否指定到人
                    if (isComplatePerson == '是'){
                        temp.isComplatePerson = isComplatePerson;
                        temp.complatePerson = $('#complatePerson' + i).val();
                        temp.complatePersonId = $("#complatePerson" + i).popedit('getValue').value;
                    }else{
                        temp.isComplatePerson = isComplatePerson;
                        temp.complatePerson = '';
                        temp.complatePersonId = '';
                    }
                    dispOrgList.push(temp);
                    srvOrdIds.push(circuitInfo[i].SRV_ORD_ID);
                    orderIds.push(circuitInfo[i].ORDER_ID);
                }
                if (dispOrgList.length < 1) {
                    that.$("#draftScheduleDiv").unblockUI().data('blockui-content', false);
                    ngc.info("请至少选择一条电路信息填写！");
                    return false;
                }
                var dispatchOrderData = this.getView("#dispDetail").getValues();
                if (!dispatchOrderData.dispatchTitle) {
                    that.$("#draftScheduleDiv").unblockUI().data('blockui-content', false);
                    ngc.info("请填写标题！");
                    return false;
                }
                if (!dispatchOrderData.dispatchText) {
                    that.$("#draftScheduleDiv").unblockUI().data('blockui-content', false);
                    ngc.info("请填写调单正文");
                    return false;
                }
                dispatchOrderData.dispatchOrderId = $("#dispatchId").val();
                dispatchOrderData.dispatchSendOrg = $("#dispatchSendOrg").val();
                dispatchOrderData.dispatchSendOrgName = $("#dispatchSendOrgName").val();
                dispatchOrderData.dispatchCopyOrgName = $("#dispatchCopyOrgName").val();
                if(dispatchOrderData.dispatchCopyOrgName != undefined && dispatchOrderData.dispatchCopyOrgName != ''
                    && dispatchOrderData.dispatchCopyOrgName != '--请选择抄送单位--'){
                    dispatchOrderData.dispatchCopyOrg = $("#dispatchCopyOrg").val();
                }else{
                    dispatchOrderData.dispatchCopyOrgName = '';
                    dispatchOrderData.dispatchCopyOrg = '';
                }
                dispatchOrderData.srvOrdId = srvOrdIds.join(',');
                dispatchOrderData.cstOrdId = this.options.cstOrdId;
                dispatchOrderData.dispOrgList = dispOrgList;
                dispatchOrderData.dispatchSource = 'second';
                obj.woId = this.options.woId;
                obj.cstOrdId = this.options.cstOrdId;
                obj.srvOrdId = srvOrdIds.join(',');
                obj.orderIds = orderIds.join(',');
                obj.circuitData = circuitInfo;
                obj.dispatchOrderData = dispatchOrderData;
                obj.action = "dispatch";
                obj.actionType = actionType;
                obj.onedry = oneDry;
                obj.replenish = replenish;
                obj.priDispOrdId = priDispOrdId;
                obj.delAttach = that.delAttach.join(',');
                var woIds = this.options.woIds;
                var woOrderIds = new Array();
                if(woIds != '' && woIds != undefined){
                    var splitWoIds = woIds.split(",");
                    $.each(splitWoIds,function(index,obj){
                        woOrderIds.push(obj);
                    });
                }
                obj.woOrderIds = woOrderIds;
                if (FILES == null || FILES.files.length < 1) {
                    operOrderAction.saveDispatchOrder(obj,function (ret) {
                        that.$("#draftScheduleDiv").unblockUI().data('blockui-content', false);
                        if (ret.success) {
                            fish.toast('success', ret.message);
                            //调单信息保存成功后，将dispatchOrderNo清空
                            dispatchOrderNo = "";
                            if(replenish){
                                that.popup.close();
                            } else {
                                $("#cancelThisBtn").click(); //只关闭当前页面
                            }
                        } else {
                            fish.toast('error', ret.message);
                        }
                    });
                } else {
                    this.fileUpdate(obj);
                }
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
            batchSave : function(){
                var me = this;
                fish.popupView({
                    url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/batchDispatchInfoView',
                    height: "100%",
                    width: "80%",
                    viewOption: {
                        orderId : me.options.parentOption.orderId,
                        oldOrgInfo : oldOrgInfo ? oldOrgInfo : "",
                        replenish : replenish,
                        disableAssist: disableAssist,
                        onedry : oneDry,
                        cstOrdId : me.options.parentOption.cstOrdId,
                        tacheId : me.options.parentOption.tacheId,
                        specialtyCode : me.options.parentOption.specialtyCode,
                        woState : me.options.parentOption.woStateCir,
                        woIds : me.options.parentOption.woIds,
                        reginonId : me.options.parentOption.reginonId,
                        dealUserId : me.options.parentOption.dealUserId,
                        compUserId : me.options.parentOption.compUserId,
                        dispType : me.options.parentOption.dispType,
                        staffId : me.options.parentOption.staffId,
                        dispObjTyeValue : me.options.parentOption.dispObjTyeValue,
                        dispObjTye : me.options.parentOption.dispObjTye,
                        orderIdSelect : me.options.parentOption.orderIdSelect,
                    },
                    callback: function (popup, view) {
                        popup.result.then(function (e) {
                            debugger;
                            //批量处理完成后将主辅调信息填充到页面
                            //先将加载的调单部门信息删除
                            $('#dispOrg').empty();
                            var param = {};
                            param.cstOrdId = me.options.cstOrdId;
                            var ret = operOrderAction.queryDispatchDept(param).responseJSON.data;//派单信息
                            if (ret.success && ret.dispatchDept.length > 0) {
                                oldOrgInfo = ret.dispatchDept;
                            }
                            //获取grid的数据量
                            var checkData = me.getView("#dispGrid").getRowData();
                            me.initOrgForm(checkData.length, 'batchSave');
                        }, function (e) {
                            console.log('关闭了', e);
                        });
                    }
                });
            },
        });
    });