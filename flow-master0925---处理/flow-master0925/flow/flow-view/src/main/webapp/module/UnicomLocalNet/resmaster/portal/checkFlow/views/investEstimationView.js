define(['text!module/UnicomLocalNet/resmaster/portal/checkFlow/templates/investEstimationView.html',
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/operOrderAction',
    'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/orderDetailsAction',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/operOrderView.css'
], function(operOrderView,operOrderAction,orderStandbyAction,orderDetailsAction,i18n,css) {
    var SpecialFlag ;
    var srvOrdIds;
    var userInfo;
    var sysResource;
    var meOper;
    return fish.View.extend({
        template: fish.compile(operOrderView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #submitBtn': 'submit',
            'click #checkSaveBtn': 'saveCheckInfo',
        },
        initialize: function() {
            this.render();
            URL = "";
            FILES = null;
            this.isSubmit = false;
            this.delFiles = new Array();
        },

        //渲染页面
        render: function() {
            this.$el.html(this.template(this.i18nData));
        },
        afterRender: function() {
            meOper = this;
            SpecialFlag='-1';
            URL=this.getRootPath();
            var me = this;
            userInfo = orderStandbyAction.queryStaffInfo().responseJSON.data;
            var tacheId = me.options.tacheId;
            var orderId = me.options.orderId + '';
            var woId=me.options.woId +'';
            var psId = me.options.psId;
            var srvOrdId = me.options.srvOrdId + '';
            var serviceId = me.options.serviceId;
            var resources = me.options.resources;
            var srvBelong = new Object();
            srvBelong.srvOrdId = srvOrdId;
            sysResource=operOrderAction.qrySrvOrderBelongSys(srvBelong).responseJSON.data;
            srvOrdIds = me.options.srvOrdId;
            //初始化电路信息
            this.circuitInfo();
            this.initFish();
            //初始化附件表格
            this.initFileUpdate(woId);
            // 资源是否满足
            $("input[name$='RES_SATISFY']").combobox({
                placeholder: '--请选择资源是否满足--',
                dataTextField: 'name',
                dataValueField: 'value',
                dataSource: [
                    {name: '满足', value: '0'},
                    {name: '不满足', value: '1'}
                ]
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
        // 初始化电路信息表
        circuitInfo : function() {
            var me = this;
            var tacheId = me.options.tacheId;
            var btnFlag = me.options.btnFlag;
            var psId = me.options.psId;
            $("#circuitGrid").grid({
                colModel: me.initGridInfo(),
                width: 516,
                multiselect: true,
                shrinkToFit: false,
                pageData: me.qrycircuitInfo(),
                onSelectRow: function (e, rowid, state, checked) {//选中行事件
                    var data = $("#circuitGrid").grid("getSelection");
                    var srvOrdId = data.SRV_ORD_ID + '';
                },
                onSelectAll: function (e, status){ //全选事件
                },
                onCellSelect: function (e, rowid, iCol, cellcontent) { // 选中单元格事件
                    var data = $("#circuitGrid").grid("getRowData",rowid);
                    //当iCol为0时，选中的是复选框而不是行数据，则不触发数据回显事件

                    if(me.qryCheckFlag()){
                        if(data.CHECK_STATE == '已保存' || tacheId == "500001150" || tacheId == "500001151"){
                            //回显核查反馈信息
                            me.initCheckInfo(data)
                        }else {
                            $('#A_CONSTRUCT_SCHEME').val('');
                            $('#A_RES_SATISFY').combobox('clear');
                            $('#A_ACCESS_ROOM').val('');
                            $('#A_INVESTMENT_AMOUNT').val('');
                            $('#A_CONSTRUCT_PERIOD').val('');
                            $('#Z_CONSTRUCT_SCHEME').val('');
                            $('#Z_ACCESS_ROOM').val('');
                            $('#Z_RES_SATISFY').combobox('clear');
                            $('#Z_INVESTMENT_AMOUNT').val('');
                            $('#Z_CONSTRUCT_PERIOD').val('');
                            $("input[name$='ACCESS_ROOM']").popedit("clear");
                            me.initCheckInfo(data);
                        }
                    }

                },
                gridComplete: function () {
                }
            });
            // 设置表格高度
            $("#circuitGrid").grid("setGridHeight", 150);
            // 冻结表格复选框一列
            $("#circuitGrid").grid('setFrozenColumns', 1);
            $("input[name$='A_ACCESS_ROOM']").popedit({
                open:function(e) {
                    var _this = $(this);
                    var param = new Object();
                    var province ;
                    var value ;
                    param.srvOrdId = srvOrdIds;
                    param.type = 'A';
                    var param2 =  new Object();
                    operOrderAction.qryProvinceName(param,function (date1) {
                        province = date1.ATTR_VALUE;
                        param2.province = province;
                        if(province != null ){
                            operOrderAction.qryProvinceValue(param2,function (date2) {
                                value = date2.ID;
                                var options = {
                                    url: 'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/views/resourceSelectHcView',
                                    height: '80%',
                                    width: '80%',
                                    modal: false,
                                    draggable: true,
                                    resizable:true,
                                    autoResizable: true,
                                    viewOption: {
                                        flag : "org",
                                        province : province,
                                        value : value
                                    },
                                    callback: function (popup, view) {
                                        popup.result.then(function (data) {
                                            _this.popedit('setValue', {name:data.MACROOM_NAME, value:data.MACROOM_NAME});
                                        }, function (e) {
                                            console.log('关闭了', e);
                                        });
                                    }
                                };
                                var popup = fish.popupView(options);
                            });
                        }
                    });
                }
            });
            $("input[name$='Z_ACCESS_ROOM']").popedit({
                open:function(e) {
                    var _this = $(this);
                    var param = new Object();
                    var province ;
                    var value ;
                    param.srvOrdId = srvOrdIds;
                    param.type = 'Z';
                    var param2 =  new Object();
                    operOrderAction.qryProvinceName(param,function (date1) {
                        province = date1.ATTR_VALUE;
                        param2.province = province;
                        if(province != null ){
                            operOrderAction.qryProvinceValue(param2,function (date2) {
                                value = date2.ID;
                                //var city = $("#A_belong_city").popedit("getValue");
                                var options = {
                                    url: 'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/views/resourceSelectHcView',
                                    height: '80%',
                                    width: '80%',
                                    modal: false,
                                    draggable: true,
                                    resizable:true,
                                    autoResizable: true,
                                    viewOption: {
                                        flag : "org",
                                        province : province,
                                        value : value
                                    },
                                    callback: function (popup, view) {
                                        popup.result.then(function (data) {
                                            _this.popedit('setValue', {name:data.MACROOM_NAME, value:data.MACROOM_NAME});
                                            //$('input[name="serialNumber"]').val(res.prodInstId);
                                        }, function (e) {
                                            console.log('关闭了', e);
                                        });
                                    }
                                };
                                var popup = fish.popupView(options);
                            });
                        }

                    });

                }
            });
        },
        initGridInfo:function(){
            var tacheId = this.options.tacheId;
            var psId = this.options.psId;
            // 核查调度不选专业、核查流程(非核查调度环节)
            return [
                {name: 'CHECK_STATE', label: '反馈信息', width: 95, sortable: false,
                    formatter: function(cellval, opts, rwdat, _act) {
                        if(cellval == '已保存'){
                            return '<div class="btn-group">' +
                                '<button type="button" class="btn btn-link js-delete" style="color: #6DCC4A">'+cellval+'</button>' +
                                '</div>';
                        }else{
                            return '<div class="btn-group btn btn-link" style="color: #FF5858">'+cellval +
                                '</div>';
                        }
                    }},
                {name: 'CIRCUITCODE', label: '电路编号', width: 100, sortable: false },
                {name: 'TRADE_ID', label: '业务订单号', width: 100 },
                {name: 'ORDER_ID', label: '流程订单号', width: 100 , hidden: true },
                {name: 'SERIAL_NUMBER', label: '业务号码', width: 100 , sortable: false },
                {name: 'AREGIONNAME', label: 'A端所属区域', width: 110, sortable: false},
                {name: 'ZREGIONNAME', label: 'Z端所属区域', width: 110, sortable: false},
                {name: 'A_INSTALLED_ADD', label: 'A端装机地址', width: 100 , sortable: false },
                {name: 'Z_INSTALLED_ADD', label: 'Z端装机地址', width: 100 , sortable: false }
            ]
        },

        initFish:function () {
            var me = this;
            var tacheId = me.options.tacheId; //"500001144";
            var psId = me.options.psId;
            var woState = me.options.woState;
            var orderId = me.options.orderId;
            var woId = me.options.woId;
            var buttonState = me.options.buttonState;
            var btnFlag = me.options.btnFlag;
            var srvOrdId = me.options.srvOrdId + '';
            var serviceId = me.options.serviceId;
            var resources = me.options.resources;
            var formValue = $('#orderOper-form').form('value');

            if(btnFlag == "submit"){
                $("#operOrderViewTitle").text("工单提交");
                if(psId == '1000211' && tacheId != "500001144"){ // 核查流程
                    if(sysResource.SYSTEM_RESOURCE!="second-schedule-lt"){
                        $("#feedBackADiv").show();
                        $("#feedBackZDiv").show();
                    }
                    $("#checkSaveBtnDiv").show();
                }
                if(tacheId == "500001145" || tacheId == "500001146" || tacheId == "500001147" || tacheId == "500001148"
                    || tacheId == "500001149" || tacheId == "510101020"){//专业核查
                    $("#remarkGoRoll").addClass("requireds");
                }
            }
        },

        //初始化附件表格
        initFileUpdate : function(param) {
            var me = this;
            $("#fileGrid").grid({
                colModel: [
                    {name: 'attachInfoId', label: '附件ID', hidden: true},
                    {name: 'fileName', label: '文件名称', width: 160, sortable: false },
                    {name: 'fileSize', label: '大小', width: 50},
                    {name: 'fileType', label: '类型', width: 40 , sortable: false },
                    {name: 'action', label: '操作', width: 100, formatter: 'actions',
                        formatoptions: {
                            editbutton: false,
                            delbutton: function (rowdata) {
                                if (rowdata.attachInfoId) {
                                    me.delFiles.push(rowdata.attachInfoId);
                                }
                                return true;
                            }
                        }

                    }
                ],
                width:516,
                create: function () {//控件初始化完成触发的事件
                    operOrderAction.getAnnex(param,function (data) {
                        if (data.length != 0) {
                            for (i = 0; i < data.length; i++) {
                                var fileObj = {};
                                var map = data[i];
                                for (var k in map) {  //通过定义一个局部变量k遍历获取到了map中所有的key值
                                    var docList = map[k]; //获取到了key所对应的value的值！
                                    if (k == "ATTACH_INFO_ID") {
                                        fileObj.attachInfoId = docList;
                                    }
                                    if (k == "FILE_SIZE") {
                                        fileObj.fileSize = docList + "KB";
                                    }
                                    if (k == "FILE_TYPE") {
                                        fileObj.fileType = docList;
                                    }
                                    if (k == "FILE_NAME") {
                                        fileObj.fileName = docList.split(".")[0];
                                    }
                                }
                                $("#fileGrid").grid("addRowData", fileObj);
                            }
                        }
                    });
                }

            });
            var me = this;
            me.$('#selectFiles').fileupload({
                dataType: 'json',
                autoUpload: false,
                add: function(e, data) {
                    var fileObj = {};
                    var obj = data.files[0];
                    fileObj.attachInfoId = '';
                    var site = obj.name.lastIndexOf(".");
                    fileObj.fileName = obj.name.substring(0, site);
                    fileObj.fileSize = (obj.size / 1024.0).toFixed(2) + "KB";
                    fileObj.fileType = obj.name.substring(site + 1, obj.name.length);
                    if ((obj.size / 1024.0).toFixed(2) >= 20*1024 ){
                        fish.warn('上传文件不能超过20M！');
                        return;
                    }
                    $("#fileGrid").grid("addRowData", fileObj);
                    if (FILES === null) {
                        FILES = data;
                    } else {
                        FILES.files.push(obj);
                    }
                },
                always: function (e, data) {
                    me.isSubmit = false;
                    if (data.result.success) {
                        $.unblockUI();
                        fish.toast('success', data.result.message);
                        me.popup.close();
                    }else {
                        $.unblockUI();
                        fish.toast('error', data.result.message);
                    }
                },
            });
        },
        // 查询是否出现核查反馈页面
        qryCheckFlag:function(){
            var me = this;
            var tacheId = me.options.tacheId;
            var psId = me.options.psId;
            var formValue = $('#orderOper-form').form('value');
            var checkFlag = true;
            return checkFlag;
        },
        // 初始化核查反馈页面并查询数据
        initCheckInfo : function (param) {
            var me = this;
            var checkFlag = me.qryCheckFlag();
            var tacheId = me.options.tacheId;
            if(checkFlag) {
                var feedBackSign = param.FEEDBACKSIGN;
                // 核查汇总、投资估算、核查调度
                if (tacheId == "500001150" || tacheId == "500001151" || tacheId == "500001144") {
                    feedBackSign = "ALL";
                }
                //如果是二干发往本地的单子是否派发专业选择否只展示填写一端反馈信息
                if(tacheId == "500001151" && sysResource.SYSTEM_RESOURCE=="second-schedule-lt"){
                    feedBackSign = "Z";
                }
                // 如果是互联网专线产品，只展示一端
                var serviceId = param.SERVICE_ID;
                if (serviceId=="10000011"){
                    feedBackSign = "Z";
                }
                if (feedBackSign == "A") {
                    if (me.options.btnFlag != "resConfig" && me.options.btnFlag != "trans") {
                        $("#feedBackADiv").show();
                    }
                    $("#feedBackZDiv").hide();
                } else if (feedBackSign == "Z") {
                    $("#feedBackADiv").hide();
                    if (me.options.btnFlag != "resConfig" && me.options.btnFlag != "trans") {
                        $("#feedBackZDiv").show();
                    }
                } else if (feedBackSign == "ALL") {
                    if (me.options.btnFlag != "resConfig" && me.options.btnFlag != "trans") {
                        $("#feedBackADiv").show();
                        $("#feedBackZDiv").show();
                    }
                }
                $("#A_INVESTMENT_AMOUNT").spinner({
                    max: 100,
                    min: 0,
                });
                $("#A_CONSTRUCT_PERIOD").spinner({
                    max: 100,
                    min: 0,
                });
                $("#Z_INVESTMENT_AMOUNT").spinner({
                    max: 100,
                    min: 0,
                });
                $("#Z_CONSTRUCT_PERIOD").spinner({
                    max: 100,
                    min: 0,
                });
                //           }
                if (feedBackSign == "A" || feedBackSign == "Z") {
                    if (me.options.btnFlag != "resConfig") {
                        $("input[name$='RES_SATISFY']").each(function () {
                            $(this).parent().prev().html("<span style='color: red'>*</span>资源是否满足:");
                        });
                        $("input[name$='CONSTRUCT_SCHEME']").each(function () {
                            $(this).parent().prev().html("<span style='color: red'>*</span>接入建设方案、资源情况:");
                        });
                        $("#A_CONSTRUCT_SCHEME").parent().prev().html("<span style='color: red'>*</span>接入建设方案、资源情况:");
                        $("#Z_CONSTRUCT_SCHEME").parent().prev().html("<span style='color: red'>*</span>接入建设方案、资源情况:");
                        $("input[name$='ACCESS_ROOM']").each(function () {
                            $(this).parent().parent().prev().html("局端接入机房:");
                        });
                        $("input[name$='INVESTMENT_AMOUNT']").spinner({
                            max: 100,
                            min: 0,
                        });
                        $("input[name$='CONSTRUCT_PERIOD']").spinner({
                            max: 100,
                            min: 0,
                        });
                        $("#amountA").hide();
                        $("#amount").show();
                        $("#amountZ").hide();
                        $("#amountLabel").show();
                        $("#periodA").hide();
                        $("#period").show();
                        $("#periodZ").hide();
                        $("#periodLabel").show();
                        $("#resProvideZ").hide();
                        $("#resProvide").show();
                        $("#resAccessZ").hide();
                        $("#resAccess").show();
                        $("#equipReadyZ").hide();
                        $("#equipReady").show();
                    }
                } else if (feedBackSign == "ALL") {
                    if (me.options.btnFlag != "resConfig") {
                        $("#A_RES_SATISFY").parent().prev().html("<span style='color: red'>*</span>A端资源是否满足:");
                        $("#Z_RES_SATISFY").parent().prev().html("<span style='color: red'>*</span>Z端资源是否满足:");
                        $("#A_CONSTRUCT_SCHEME").parent().prev().html("A端接入建设方案、资源情况:");
                        $("#Z_CONSTRUCT_SCHEME").parent().prev().html("Z端接入建设方案、资源情况:");
                        $("#A_ACCESS_ROOM").parent().parent().prev().html("A端局端接入机房:");
                        $("#Z_ACCESS_ROOM").parent().parent().prev().html("Z端局端接入机房:");
                        $("#amount").hide();
                        $("#amountA").show();
                        $("#amountLabel").hide();
                        $("#amountZ").show();
                        $("#period").hide();
                        $("#periodA").show();
                        $("#periodLabel").hide();
                        $("#periodZ").show();
                        $("#amountA").show();
                        $("#amountA").show();
                        $("#resProvideZ").show();
                        $("#resProvide").hide();
                        $("#resAccessZ").show();
                        $("#resAccess").hide();
                        $("#equipReadyZ").show();
                        $("#equipReady").hide();

                        $("input[name$='INVESTMENT_AMOUNT']").spinner({
                            max: 100,
                            min: 0,
                        });
                        $("input[name$='CONSTRUCT_PERIOD']").spinner({
                            max: 100,
                            min: 0,
                        });
                    }
                }
                if (tacheId == "500001150" || tacheId == "500001151") {
                    $("input[name$='CONSTRUCT_SCHEME']").each(function () {
                        $(this).attr('readonly', 'readonly');
                    });
                    $("#A_CONSTRUCT_SCHEME").attr('readonly', 'readonly');
                    $("#Z_CONSTRUCT_SCHEME").attr('readonly', 'readonly');
                    $("#A_ACCESS_ROOM").attr('disabled', true);
                    $("#Z_ACCESS_ROOM").attr('disabled', true);
                    $("input[name$='RES_SATISFY']").each(function(){
                        $(this).combobox('disable');
                    });
                }
                me.queryCheckInfo(param);
            }
        },
        // 查询核查反馈信息 参数：WO_ID,SRV_ORD_ID
        queryCheckInfo : function (param) {
            var me = this;
            var tacheId = me.options.tacheId;
            var params = {};
            params.tacheId = tacheId;
            params.woId =param.WO_ID;
            params.srvOrdId = param.SRV_ORD_ID;
            params.orderId = param.ORDER_ID;
            params.sysResource=sysResource.SYSTEM_RESOURCE;
            params.RELATE_INFO_ID=param.RELATE_INFO_ID;
            operOrderAction.queryCheckInfo(params,function (res) {
                var arrayData = new Map();
                arrayData = res.data;
                for(var key in arrayData){
                    var value = arrayData[key];
                    if(key=="A_RES_SATISFY" || key=="Z_RES_SATISFY"){
                        $("#"+key).combobox('value',value);
                    }else{
                        $("#"+key).val(value);
                    }
                }
            });
        },
        // 查询电路信息
        qrycircuitInfo : function(){
            var me = this;
            var psIds = '1000248,1000249'; //子流程
            var psId = me.options.psId;
            var specialtyCode = me.options.specialtyCode;
            var regionId = me.options.regionId;
            var dispObjTyeValue = me.options.dispObjTyeValue;
            var dispObjTye = me.options.dispObjTye;
            var param = {};
            param.cstOrdId = me.options.cstOrdId +'';
            param.orderId = me.options.orderId +'';
            param.woIds = me.options.woIds +'';
            param.srvOrdId = me.options.srvOrdId + '';
            param.woState = me.options.woState +'';
            param.tacheId = me.options.tacheId +'';
            param.dealUserId = userInfo.userId +'';
            param.dispObjTyeValue = me.options.dispObjTyeValue +'';
            param.dispObjTye = me.options.dispObjTye +'';
            param.btnFlag = me.options.btnFlag +'';
            param.newCreateResource = "1";// 是否新建资源：否
            if (me.qryCheckFlag()){
                param.woIds = me.options.woId;
                param.specialtyCode = specialtyCode;
                param.regionId = regionId;
                operOrderAction.qrySrvOrdListCheck(param,function (res) {
                    if(res.flag == 1){
                        $("#circuitGrid").grid("reloadData", res.data);
                        //      me.qryCircuitAreaInfo(res.data[0]);
                        me.initCheckInfo(res.data[0]);
                    }else {
                        fish.toast('error', res.message);
                    }
                });
            }else {
                operOrderAction.qrySrvOrdList(param,function (res) {
                    if(res.flag == 1){
                        $("#circuitGrid").grid("reloadData", res.data);
                        me.qryCircuitAreaInfo(res.data[0]);
                        if(me.qryCheckFlag()){
                            me.initCheckInfo(res.data[0]);
                        }
                    }else {
                        fish.toast('error', res.message);
                    }
                });
            }
            $(window).trigger("resize");
            //(specialtyCode != null && specialtyCode != null)
        },

        // 提交工单
        submit:function () {
            var me = this;
            var psId = me.options.psId;
            var tacheId = me.options.tacheId;
            var buttonState = me.options.buttonState;
            var formValue = $('#orderOper-form').form('value');
            var btnFlag = me.options.btnFlag;
            var params = new Object();
            var circuitData = $("#circuitGrid").grid("getCheckRows");
            var fileData = $("#fileGrid").grid("getRowData");
            var delFilesStr = "";
            for (var j = 0; j < this.delFiles.length; j++) {
                var temp = false;
                for (var i = 0; i < fileData.length; i++) {
                    if (this.delFiles[j] == fileData[i].attachInfoId) {
                        temp = true;
                        break;
                    }
                }
                if (!temp) {
                    delFilesStr += this.delFiles[j] + ",";
                }
            }
            if (delFilesStr) {
                params.delFiles = delFilesStr.substring(0, delFilesStr.length - 1);
            }
            if (FILES) {
                var vernier = 0;
                var fileLength = FILES.files.length;
                for (var index = 0; index < fileLength; index++) {
                    var file = FILES.files[index - vernier];
                    var isRemove = true;
                    for (var i = 0; i < fileData.length; i++) {
                        var data = fileData[i];
                        if (file.name == data.fileName + "." + data.fileType) {
                            isRemove = false;
                        }
                    }
                    if (isRemove) {
                        FILES.files.splice(index - vernier, 1);
                        vernier++;
                    }
                }
            }
            if (circuitData.length == 0) {
                fish.warn('请至少选择一个电路信息！');
                return;
            }
            //判断有没有追单
            var flag = -1;
            var message = '';
            $.each(circuitData,function(i,val){
                var orderId = val.ORDER_ID;
                var cstOrdId = val.CST_ORD_ID;
                var ifFlag = orderDetailsAction.queryIfTrack(orderId + '', cstOrdId + '').responseJSON.data;
                if (ifFlag){
                    flag = i;
                    message = '存在追单未处理的电路，不能提交，请重新勾选!';
                    return false;
                }
            });
            if (flag > -1) {
                fish.error({title:'提示',message:message});
                return;
            }
            params.circuitData = circuitData; //订单信息
            //modefy by wanggang2  异常单资源配置的时候这来字段空了 报错 不敢直接删掉
            if(sysResource!= null && sysResource!=''){
                params.sysResFullCom = sysResource.SYSTEM_RESOURCE; //系统来源
                params.resFullCom = sysResource.RESOURCES; //数据来源
            }
            if(btnFlag == "submit"){//回单
                var operAttrs = new Object();//线条参数
                var tacheOperInfo = new Object();//环节操作数据信息
                var actionFlag = "complateWo";
                if(me.qryCheckFlag()){
                    var circuitNum = -1;
                    $.each(circuitData,function(i,val){
                        if (val.CHECK_STATE != '已保存') {
                            circuitNum = 0;
                        }
                    });
                    if (circuitNum != -1 && tacheId != "500001150"){
                        fish.warn('选择了未保存的电路，请选择已保存的电路进行派发！');
                        return;
                    }
                }

                //资源核查，说明必填 huangxingfei 2019-04-23
                if (tacheId == "500001145" || tacheId == "500001146" || tacheId == "500001147" || tacheId == "500001148"
                    || tacheId == "500001149" || tacheId == "510101020") {
                    if(formValue.remark == null){
                        fish.error({title:'提示',message:'说明不能为空'});
                        return;
                    }
                }
                //流程线条参数
                if (tacheId == "500001151"){//投资估算
                    operAttrs.ifFinish = 1;
                    operAttrs.ifCollect = 0;
                }
                tacheOperInfo.remark = formValue.remark;
                params.operAttrsVal = operAttrs;
                params.tacheOperInfo = tacheOperInfo;
                params.actionFlag = actionFlag;
                params.action = "submit";
                $.blockUI({message: '派单中...'});
                if (FILES === null) {
                    me.submitOrder(params);
                }else {
                    me.fileUpdate(params); //上传附件并回单
                }
            }
        },
        submitOrder : function(params){
            var me = this;
            if (!me.isSubmit) {
                me.isSubmit = true;
                params.newCreateResource = "1";
                operOrderAction.submitOrder(params, function (res) {
                    me.isSubmit = false;
                    if (res.success) {
                        $.unblockUI();
                        fish.toast('success', res.message);
                        me.popup.close();
                    } else {
                        $.unblockUI();
                        fish.toast('error', res.message);
                    }
                });
            } else {
                fish.toast('error', "请勿重复提交！");
            }
        },

        // 保存核查反馈信息
        saveCheckInfo: function(){
            var me = this;
            var tacheId = me.options.tacheId;
            var data = $("#circuitGrid").grid("getCheckRows");
            var formValue = $('#orderOper-form').form('value');
            if (data.length >= 1) {
                var param = {};
                param.circuitData = data;
                param.formValue = formValue;
                param.sysResource=sysResource.SYSTEM_RESOURCE;
                // 校验核查数据
                var checkA = $('#A_INVESTMENT_AMOUNT').isValid();
                var checkZ = $('#Z_INVESTMENT_AMOUNT').isValid();
                var tFlag = (tacheId == "500001145"||tacheId == "500001147"||tacheId == "500001146"||tacheId == "500001148"
                    ||tacheId == "500001149"||tacheId == "510101020")&&sysResource.SYSTEM_RESOURCE=="second-schedule-lt";//二干来单专业核查环节
                if(!tFlag){
                    if(!checkA){
                        fish.warn({title:'提示',message:'不能为空或格式不正确！请修改!'})
                        return;
                    }
                }
                if(!checkZ){
                    fish.warn({title:'提示',message:'不能为空或格式不正确！请修改!'})
                    return;
                }
                if(me.validCheckInfo(param)){
                    operOrderAction.saveCheckInfo(param,function(res){
                        if (res.success){
                            //核查信息保存完成后，重新刷新电路信息表格数据
                            me.circuitInfo();
                            fish.toast('success', res.message);
                        } else{
                            fish.toast('error', res.message);
                        }
                    });
                }
            }else{
                fish.toast('error', "请至少勾选一条电路信息!");
            }

        },
        validCheckInfo : function(param){
            var data = param.circuitData;
            var formValue = param.formValue;
            var me = this;
            var tacheId = me.options.tacheId;
            // 核查汇总500001150、投资估算500001151$
            var isValidSign = !(tacheId == "500001150" || tacheId == "500001151");
            var feedBackSign = data[0].FEEDBACKSIGN;
            if((feedBackSign=="A" || feedBackSign=="ALL") && '10000011'!=me.options.serviceId){
                var A_INVESTMENT_AMOUNT = formValue.A_INVESTMENT_AMOUNT;
                var A_CONSTRUCT_PERIOD = formValue.A_CONSTRUCT_PERIOD;
                var A_CONSTRUCT_SCHEME = formValue.A_CONSTRUCT_SCHEME;
                var ResSatisfyA = $('#A_RES_SATISFY').val();//满足-不满足
                if(isValidSign && A_CONSTRUCT_SCHEME==null && ResSatisfyA == 1){
                    fish.warn('A端接入建设方案、资源情况不能为空！');
                    return false;
                }
                if(ResSatisfyA==""){
                    fish.warn('A端资源是否满足不能为空！');
                    return false;
                }
            }
            if(feedBackSign=="Z" || feedBackSign=="ALL"){
                var Z_INVESTMENT_AMOUNT = formValue.Z_INVESTMENT_AMOUNT;
                var Z_CONSTRUCT_PERIOD = formValue.Z_CONSTRUCT_PERIOD;
                var Z_CONSTRUCT_SCHEME = formValue.Z_CONSTRUCT_SCHEME;
                var ResSatisfyZ = $('#Z_RES_SATISFY').val();//满足-不满足
                if(isValidSign && Z_CONSTRUCT_SCHEME==null && ResSatisfyZ == 1){
                    if('10000011'== me.options.serviceId){
                        fish.warn('接入建设方案、资源情况不能为空！');
                        return false;
                    }else{
                        fish.warn('Z端接入建设方案、资源情况不能为空！');
                        return false;
                    }
                }
                if(ResSatisfyZ==""){
                    if('10000011' == me.options.serviceId){
                        fish.warn('资源是否满足不能为空！');
                        return false;
                    }else{
                        fish.warn('Z端资源是否满足不能为空！');
                        return false;
                    }
                }
            }
            return true;
        },
        fileUpdate :function (params){
            FILES.url = URL+"/localScheduleLT/FlieUpdateController/uploadFiles.spr";
            FILES.formData = {
                params : JSON.stringify(params)
            };
            if (!this.isSubmit) {
                this.isSubmit = true;
                FILES.submit();
            } else {
                fish.toast('error', "请勿重复提交！");
            }
        }
    });
});