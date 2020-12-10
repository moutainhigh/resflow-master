define(['text!module/UnicomLocalNet/resmaster/portal/checkFlow/templates/checkDispatchViewSingle.html',
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/operOrderAction',
    'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/orderDetailsAction',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/operOrderView.css'
], function(operOrderView,operOrderAction,orderStandbyAction,orderDetailsAction,i18n,css) {
    let SpecialFlag ;
    let srvOrdIds;
    let userInfo;
    let sysResource;
    let meOpera;
    let fields = {
        'Z_BOARD_READY': 'required;',
        'Z_BOARD_PERIOD': 'required;workDayRep;',
        'Z_BOARD_AMOUNT': 'required;moneyRep',
        'Z_BOARD_TYPE': 'required;',
        'Z_TRANS_READY': 'required;',
        'Z_TRANS_PERIOD': 'required;workDayRep;',
        'Z_TRANS_AMOUNT': 'required;moneyRep',
        'Z_TRANS_TYPE': 'required;',
        'Z_OPTICAL_READY': 'required;',
        'Z_OPTICAL_PERIOD': 'required;workDayRep;',
        'Z_OPTICAL_AMOUNT': 'required;moneyRep',
        'Z_MUNICIPAL_APPROVAL': 'required;',
        'Z_APPROVAL_PERIOD': 'required;workDayRep;',
        'Z_CONSTRUCT_PERIOD_STAND': 'required;',
        'Z_PROJECT_AMOUNT': 'moneyRep',
        'Z_PROJECT_OVERVIEW': 'required;',
        'Z_RES_DESC': 'required;',
        'Z_PROPERTY_REDLINE': 'required;',
        'Z_PROPERTY_DESC': 'required;',
        'Z_RES_EXPLORER': 'required;',
        'Z_RES_EXPLOR_CONTACT': 'required;',
        'Z_UNABLE_RELOVE': 'required;',
        'Z_ACCESS_CIR_TYPE': 'required;',
        'Z_OTHER_ACE_CIR_TYPE': 'required;'
    }
    return fish.View.extend({
        template: fish.compile(operOrderView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #submitBtn': 'submit',
            'click #specialSaveBtn': 'saveSpecialConfig',
            'click #checkSaveBtn': 'saveCheckInfo'
        },
        initialize: function() {
            this.render();
            URL = "";
            FILES = null;
            areaPro = {};
            this.isSubmit = false;
            this.delFiles = new Array();
        },

        //渲染页面
        render: function() {
            this.$el.html(this.template(this.i18nData));
        },
        afterRender: function() {
            SpecialFlag='-1';
            URL=this.getRootPath();

            userInfo = orderStandbyAction.queryStaffInfo().responseJSON.data;

            meOpera  = this;
            let woId=meOpera.options.woId +'';
            let srvBelong = {};
            srvBelong.srvOrdId = meOpera.options.srvOrdId + '';
            sysResource=operOrderAction.qrySrvOrderBelongSys(srvBelong).responseJSON.data;

            srvOrdIds = meOpera.options.srvOrdId;
            //初始化电路信息
            this.circuitInfo();
            this.initFish();
            //初始化附件表格
            this.initFileUpdate(woId);
            // 初始化专业选择框
            this.initProfessional();
            // 初始化校验规则
            this.initCheckComboBox();
            meOpera.$orderOperaFrom = this.$("#orderOper-form");
        //    this.initValidate();
            //this.initTerminateNameShow();
            // 初始化显示隐藏
            this.initShow();
        },
        getRootPath:function (){
            //获取当前网址，如： http://localhost:8083/uimcardprj/share/meun.jsp
            let curWwwPath=window.document.location.href;
            //获取主机地址之后的目录，如： uimcardprj/share/meun.jsp
            let pathName=window.document.location.pathname;
            let pos=curWwwPath.indexOf(pathName);
            //获取主机地址，如： http://localhost:8083
            let localhostPaht=curWwwPath.substring(0,pos);
            //获取带"/"的项目名，如：/uimcardprj
            let projectName=pathName.substring(0,pathName.substr(1).indexOf('/')+1);
            return (localhostPaht+projectName);
        },
        // 初始化电路信息表
        circuitInfo : function() {

            let tacheId = meOpera.options.tacheId;
            let btnFlag = meOpera.options.btnFlag;
            let psId = meOpera.options.psId;
            $("#circuitGrid").grid({
                colModel: meOpera.initGridInfo(),
                width: 516,
                height:150,
                multiselect: true,
                shrinkToFit: false,
                pageData: meOpera.qryCircuitInfo(),
                onSelectRow: function (e, rowid, state, checked) {//选中行事件
                    let data = $("#circuitGrid").grid("getSelection");
                    let srvOrdId = data.SRV_ORD_ID + '';
                    if(data.STATE == '已配置' && !(meOpera.qryCheckFlag())){
                        //回显专业配置信息
                        meOpera.qryCircuitAreaInfo(data)
                        // meOpera.queryPropertyConfig(data);
                    }else if (data.STATE == '未配置'){
                        if (tacheId == "500001144") {//核查调度
                            //置灰所有核查专业
                            $("input[name$='Special']").each(function(){
                                $(this).removeAttr("checked");
                                $(this).parent().parent().next().find("input").attr("disabled",true);
                                $(this).parent().parent().next().find("input").popedit('setValue',{name:'请选择派发区域！',value:''});
                            });
                        }
                    }
                },
                onSelectAll: function (e, status){ //全选事件
                },
                onCellSelect: function (e, rowid, iCol, cellcontent) { // 选中单元格事件
                    let data = $("#circuitGrid").grid("getRowData",rowid);
                    //当iCol为0时，选中的是复选框而不是行数据，则不触发数据回显事件

                    if(meOpera.qryCheckFlag()){
                        if(data.CHECK_STATE == '已保存' || tacheId == "500001150" || tacheId == "500001151"){
                            //回显核查反馈信息
                            meOpera.initCheckInfo(data)
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
                            meOpera.initCheckInfo(data);
                        }
                        if(tacheId == "500001150"&&sysResource.SYSTEM_RESOURCE!="second-schedule-lt"&&data.CHECK_STATE == '未保存'){
                            //本地核查单，资源提供情况，接入情况，备货情况清空
                            $('#Z_RES_PROVIDE').combobox('clear');
                            $('#Z_RES_ACCESS').combobox('clear');
                            $('#Z_EQUIP_READY').combobox('clear');
                            $('#A_RES_PROVIDE').combobox('clear');
                            $('#A_RES_ACCESS').combobox('clear');
                            $('#A_EQUIP_READY').combobox('clear');
                        }
                    }

                },
                gridComplete: function () {
                }
            });
            // // 设置表格高度
            // $("#circuitGrid").grid("setGridHeight", 150);
            // // 冻结表格复选框一列
            // $("#circuitGrid").grid('setFrozenColumns', 1);
            $("input[name$='A_ACCESS_ROOM']").popedit({
                open:function(e) {
                    let _this = $(this);
                    let param = new Object();
                    let province ;
                    let value ;
                    param.srvOrdId = srvOrdIds;
                    param.type = 'A';
                    let param2 =  new Object();
                    operOrderAction.qryProvinceName(param,function (date1) {
                        province = date1.ATTR_VALUE;
                        param2.province = province;
                        if(province != null ){
                            operOrderAction.qryProvinceValue(param2,function (date2) {
                                value = date2.ID;
                                let options = {
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
                                let popup = fish.popupView(options);
                            });
                        }
                    });
                }
            });
            $("input[name$='Z_ACCESS_ROOM']").popedit({
                open:function(e) {
                    let _this = $(this);
                    let param = new Object();
                    let province ;
                    let value ;
                    param.srvOrdId = srvOrdIds;
                    param.type = 'Z';
                    let param2 =  new Object();
                    operOrderAction.qryProvinceName(param,function (date1) {
                        province = date1.ATTR_VALUE;
                        param2.province = province;
                        if(province != null ){
                            operOrderAction.qryProvinceValue(param2,function (date2) {
                                value = date2.ID;
                                //let city = $("#A_belong_city").popedit("getValue");
                                let options = {
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
                                let popup = fish.popupView(options);
                            });
                        }

                    });

                }
            });
        },
        initGridInfo:function(){
            let tacheId = this.options.tacheId;
            let psId = this.options.psId;
            let selectSpecialFlag = $("input[name='resRadio']:checked").val();
            if(tacheId == '500001144' && selectSpecialFlag=='0') { //核查调度(选专业)
                return [
                    {name: 'STATE', label: '专业配置', width: 95, sortable: false,
                        formatter: function(cellval, opts, rwdat, _act) {
                            if(cellval == '已配置'){
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
            }else {
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
            }
        },

        initFish:function () {

            let orderId = meOpera.options.orderId + '';
            //是否选择专业
            $("input[name='resRadio']").bind('click',function(){
                let selectSpecialFlag = $("input[name='resRadio']:checked").val();
                if (selectSpecialFlag == '0') {
                    $("#specialDiv").show();
                    $("#specialDiv :input").show();
                    $("#specialSaveBtnDiv").show();
                    $("#feedBackADiv").hide();
                    $("#feedBackZDiv").hide();
                    $("#checkSaveBtnDiv").hide();
                }else if(selectSpecialFlag == '1') {
                    $("#specialDiv").hide();
                    $("#specialDiv :input").hide();
                    $("#specialSaveBtnDiv").hide();
                    $("#feedBackADiv").show();
                    $("#feedBackZDiv").show();
                    $("#checkSaveBtnDiv").show();
                }
                meOpera.circuitInfo();
                SpecialFlag = selectSpecialFlag;
            });
            operOrderAction.qryAllSpecialCheckWoOrder(orderId,function (res) {
                if (res.length > 0) {
                    $("input[name='resRadio']").each(function(){
                        $(this).prop("disabled",true);
                    });
                }
            })


            let tacheId = meOpera.options.tacheId; //"500001144";
            let psId = meOpera.options.psId;
            let buttonState = meOpera.options.buttonState;
            let btnFlag = meOpera.options.btnFlag;

            if(btnFlag == "submit"){
                $("#remarkGoRoll").addClass("requireds");
                $("#operOrderViewTitle").text("工单提交");
                if(psId == '1000211' && tacheId != "500001144"){ // 核查流程
                    if(sysResource.SYSTEM_RESOURCE!="second-schedule-lt"){
                        $("#feedBackADiv").show();
                        $("#feedBackZDiv").show();
                    }
                    $("#checkSaveBtnDiv").show();
                }
                if (tacheId == "500001144"){//核查调度
                    if(buttonState != "dispConfirm"){
                        $("#selectSpecialDiv").show();
                    }
                    $("#specialDiv").show();
                    $('#specialSaveBtnDiv').show();
                }
                if(tacheId == "500001145" || tacheId == "500001146" || tacheId == "500001147" || tacheId == "500001148"
                    || tacheId == "500001149" || tacheId == "510101020"){//专业核查
                    $("#remarkGoRoll").addClass("requireds");
                }
                else if (tacheId == "500001150"){//核查汇总
                    if(sysResource.SYSTEM_RESOURCE=="second-schedule-lt"){
                        $("#checkSaveBtnDiv").hide();
                    }
                    $("#estimateDiv").show();
                }
            }

        },

        //初始化附件表格
        initFileUpdate : function(param) {

            $("#fileGrid, #A_fileGrid, #Z_fileGrid").grid({
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
                                    meOpera.delFiles.push(rowdata.attachInfoId);
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
                                let fileObj = {};
                                let map = data[i];
                                for (let k in map) {  //通过定义一个局部变量k遍历获取到了map中所有的key值
                                    let docList = map[k]; //获取到了key所对应的value的值！
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
            meOpera.$('#selectFiles').fileupload({
                dataType: 'json',
                autoUpload: false,
                add: function(e, data) {
                    let fileObj = {};
                    let obj = data.files[0];
                    fileObj.attachInfoId = '';
                    let site = obj.name.lastIndexOf(".");
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
                    meOpera.isSubmit = false;
                    if (data.result.success) {
                        $.unblockUI();
                        fish.toast('success', data.result.message);
                        meOpera.popup.close();
                    }else {
                        $.unblockUI();
                        fish.toast('error', data.result.message);
                    }
                },
            });
        },
        // 查询是否出现核查反馈页面
        qryCheckFlag:function(){

            let tacheId = meOpera.options.tacheId;
            let psId = meOpera.options.psId;
            let formValue = $('#orderOper-form').form('value');
            let checkFlag = (psId == "1000211" && tacheId != "500001144") || (tacheId == "500001144" && formValue.resRadio == 1);
            //    let checkFlag = true;
            return checkFlag;
        },
        // 初始化核查反馈页面并查询数据
        initCheckInfo : function (param) {
            let checkFlag = meOpera.qryCheckFlag();
            let tacheId = meOpera.options.tacheId;
            if(checkFlag) {
                this.feedbackSign = "Z";
            }
            meOpera.queryCheckInfo(param);
        },
        queryCheckInfo : function (param) {
            let me = this;
            let queryParam={};
            queryParam.woId=param.WO_ID;
            queryParam.orderId=param.ORDER_ID;
            queryParam.tacheId=param.TACHE_ID;
            orderDetailsAction.queryCheckInfoBack(queryParam,function (res) {
                let dataArray = res.data;
                console.error("已保存的核查");
                console.error(dataArray);
                for(let key in dataArray){
                    let value = dataArray[key];
                    if (key.indexOf("_RES_PROVIDE_STAND")  != -1
                        || key.indexOf("_BOARD_READY") != -1 || key.indexOf("_TRANS_READY")  != -1
                        || key.indexOf("_TRANS_TYPE")  != -1 || key.indexOf("_OPTICAL_READY")  != -1
                        || key.indexOf("_MUNICIPAL_APPROVAL")  != -1  || key.indexOf("_PROPERTY_REDLINE")  != -1
                        || key.indexOf( "_ACCESS_CIR_TYPE") != -1
                    ) {
                        $("#" + key).combobox('value',value);
                    } else{
                        $("#"+key).val(value);
                    }
                }
            });
        },
        // 控制显示隐藏
        initShow: function (type) {
            if (type == null) {
                meOpera.baseInitShow("A");
                meOpera.baseInitShow("Z");
            } else {
                meOpera.baseInitShow(type);
            }

        },
        baseInitShow:function(type) {
            $("." + type + "_DIV").hide();
            $("." + type + "_ALL").show();
            $("." + type + "_OTHER_ACE").hide();
            if (meOpera.options.serviceId == "10000011") {
                // 只有互联网显示
                $("." + type + "_CIR_TYPE").show();
            } else {
                $("." + type + "_CIR_TYPE").hide();
            }
            let Select = $("#" + type + "_RES_PROVIDE_STAND").combobox('value');
            switch (Select) {
                case "1":
                    //现有资源-资源完全具备 不需要O侧反馈其他的信息
                    break;
                case "2":
                    //、现有资源-客户端联通光缆已接入且客户机房的联通设备具备但需要新增设备板卡
                    $("." + type + "_BOARD").show();
                    break;
                case "3":
                    $("." + type + "_TRANS").show();
                    break;
                case "4":
                    $("." + type + "_TRANS").show();
                    $("." + type + "_OPTICAL").show();
                    $("." + type + "_PROJECT_DESC").show();
                    $("." + type + "_APPROVAL").show();
                    break;
                case "5":
                    //需做到客户端机房光缆管道接入工程（含租用）及在客户机房新建联通设备工程
                    $("." + type + "_TRANS").show();
                    $("." + type + "_OPTICAL").show();
                    $("." + type + "_PROJECT_DESC").show();
                    $("." + type + "_APPROVAL").show();
                    break;
                case "6":
                    $("." + type + "_PROJECT").show();
                    $("." + type + "_PROJECT_DESC").show();
                    break;
                case "7":
                    $("." + type + "_ALL").hide();
                    $("." + type + "_RELOVE").show();
                    $("." + type + "_CIR_TYPE").show();
                    break;
                default:
                    break;
            }
        },
        initValidate:function() {
            this.$orderOperaFrom.validator({
                stopOnError: false ,
                focusInvalid: true ,
                ignore: ':hidden :disabled, #specialDiv',
                timely: true,
                messages: {
                },
                rules : {
                    workDayRep:  function(element, param, field) {
                        return   /^\d+$/.test(element.value) || '请输入正整数';
                    },
                    moneyRep:  function(element, param, field) {

                        return  /^[0-9]+(.[0-9]{1,3})?$/.test(element.value) || '请输入正确金额, 小数点后最多三位小数';
                    }
                },
                fields:  fields
            });
        },
        initCheckComboBox:function() {
            let menus = $(".menu");
            let idArray = [];
            let dataMap = {};
            console.log("===============所有下拉框==================");
            console.log(menus);
            for(let i =0;i< menus.length;i++) {
                let menu = menus[i];
                let selectId = menu.id;
                let param = {};
                let start = selectId.split("_")[0];
                let end = selectId.split(start + "_")[1];
                let dataSource = dataMap[end];
                let placeHolder;
                // 未获取该类型的数据
                switch (selectId) {
                    case "A_RES_PROVIDE_STAND":
                    case "Z_RES_PROVIDE_STAND":
                        param.codeType = "RES_PROVIDE_STAND";
                        placeHolder = "--请选择资源提供方式--";
                        break;
                    case "A_BOARD_READY":
                    case "Z_BOARD_READY":
                        param.codeType = "BOARD_READY";
                        placeHolder = "--请选择备货情况--";
                        break;
                    case "A_TRANS_READY":
                    case "Z_TRANS_READY":
                        param.codeType = "TRANS_READY";
                        placeHolder = "--请选择传输设备备货情况--";
                        break;
                    case "A_OPTICAL_READY":
                    case "Z_OPTICAL_READY":
                        param.codeType = "OPTICAL_READY";
                        placeHolder = "--请选择资源提供方式--";
                        break;
                    case "A_PROPERTY_REDLINE":
                    case "Z_PROPERTY_REDLINE":
                        param.codeType = "PROPERTY_REDLINE";
                        placeHolder = "--是否涉及物业问题--";
                        break
                    case "A_TRANS_TYPE":
                    case "Z_TRANS_TYPE":
                        param.codeType = "TRANS_TYPE";
                        placeHolder = "--请选择传输设备类型--";
                        break;
                    case "A_CUST_ROOM":
                    case "Z_CUST_ROOM":
                        param.codeType = "CUST_ROOM";
                        placeHolder = "--请选择客户机房条件--";
                        break;
                    case "A_MUNICIPAL_APPROVAL":
                    case "Z_MUNICIPAL_APPROVAL":
                        param.codeType = "MUNICIPAL_APPROVAL";
                        placeHolder = "--是否需要市政报批--";
                        break;
                    case "Z_ACCESS_CIR_TYPE":
                        param.codeType = "ACCESS_CIR_TYPE";
                        placeHolder = "--请选择接入电路类型--";
                        break;
                }
                if (dataSource == null) {
                    orderStandbyAction.queryItemType(param, function (data) {
                        console.log("======================" + selectId);
                        console.log(data);
                        dataMap[end] = data;
                        idArray.push(end);
                        meOpera.initBox(placeHolder, selectId , data);
                    });
                } else {
                    meOpera.initBox(placeHolder, selectId , dataSource);
                }
            }
        },
        initBox : function (holder, selectId, data) {
            let element = $("#" + selectId);
            element.combobox({
                placeholder: holder,
                dataTextField: 'name',
                dataValueField: 'value',
                dataSource: data,
            });

            // 绑定change事件
            element.on('combobox:change', function(ele) {
                let type = selectId.split("_")[0];
                let value = ele.target.value;

                // 校验规则
                let ruleValue = {};
                // 资源提供方式
                if (selectId.indexOf('RES_PROVIDE_STAND') != -1) {
                    // 显示隐藏
                    meOpera.initShow(type);

                    $("."+ type + "_INPUT").val('');//清空已填的值
                    // 恢复选项默认值
                    $("#"+ type + "_BOARD_READY").combobox('value', '');
                    $("#"+ type + "_TRANS_READY").combobox('value', '');
                    $("#"+ type + "_TRANS_TYPE").combobox('value', '');
                    $("#"+ type + "_OPTICAL_READY").combobox('value', '');
                    $("#"+ type + "_ACCESS_CIR_TYPE").combobox('value', '');

                    // 默认否
                    $("#"+ type + "_PROPERTY_REDLINE").combobox('value', '2');
                    $("#"+ type + "_MUNICIPAL_APPROVAL").combobox('value', '2');
                    $("#"+ type + "_CUST_ROOM").combobox('value', '2');
                }
                meOpera.changeRequire2(selectId);
            })

            // 初始化默认值
            let defaultValue;
            if (selectId.indexOf("RES_PROVIDE_STAND") != -1) {

            } else {
                if (selectId.indexOf("PROPERTY_REDLINE") != -1) {
                    // 物业红线区域内施工是否涉及物业问题	（回复：是、否、不清楚，默认为否）
                    defaultValue = 2;
                } else {
                    defaultValue = data[0].value;
                }
                element.combobox('value', defaultValue);
            }
        },
        changeRequire2:function(selectId) {
            let type = selectId.split("_")[0];

            let hideKeyArray = [];
            // 电路设备类型
            let accessCirType = $("#"+ type +"_ACCESS_CIR_TYPE").combobox("value");
            if (accessCirType == '08') {
                // 其他设备类型
                $("."+type+"_OTHER_ACE").show();
            } else {
                $("."+type+"_OTHER_ACE").hide();
                $("#"+type+"_OTHER_ACE_CIR_TYPE").val("");
            }
            // 隐藏的模块
            let hideDiv = $("." + type + "_DIV:hidden");
            console.log(hideDiv);
            hideDiv.each(function(ele){
                // 如果隐藏 则子元素取消校验
                let hideInput = $(hideDiv[ele].children).find("." + type + "_INPUT");
                hideInput.each(function(element){
                    let hideKey = $(this).attr("id");
                    hideKeyArray.push(hideKey);
                });
            });

            // 板卡备货
            let board = $("#" + type + "_BOARD_READY").combobox('value');
            console.log("板卡" + board);
            if (board != "2") {
                let boardKey = type + "_BOARD_PERIOD";
                hideKeyArray.push(boardKey);
            }
            // 传输备货
            let trans = $("#" + type + "_TRANS_READY").combobox("value");
            console.log("传输备货" + trans);
            if (trans != "2") {
                let transKey = type + "_TRANS_PERIOD";
                hideKeyArray.push(transKey);
            }
            // 光缆备货
            let optical = $("#" + type + "_OPTICAL_READY").combobox("value");
            console.log("光缆备货" + optical);
            if (optical!= "2") {
                let opticalKey = type + "_OPTICAL_PERIOD";
                hideKeyArray.push(opticalKey);
            }
            // 物业红线
            let redLine = $("#" + type + "_PROPERTY_REDLINE").combobox("value");
            console.log("物业红线" + redLine);
            if (redLine == "2") {
                let redLineKey = type + "_PROPERTY_DESC";
                hideKeyArray.push(redLineKey);
            }
            //市政报批
            let approval = $("#" + type + "_MUNICIPAL_APPROVAL").combobox("value");
            console.log("市政报批" + approval);
            if (approval == "2") {
                let approvalKey = type + "_APPROVAL_PERIOD";
                hideKeyArray.push(approvalKey);
            }

            // 堪察人
            let resValue = $("#" + type + "_RES_PROVIDE_STAND").combobox('value');
            console.log("堪察人" + resValue);
            if (resValue == "7") {
                let resKey = type + "_RES_EXPLORER";
                hideKeyArray.push(resKey);
            }

            let rules={};
            for (let key in fields) {
                if (key.indexOf(type + "_") == -1) {
                    continue;
                }
                if (hideKeyArray.indexOf(key) != -1) {
                    rules[key] = "";
                } else {
                    rules[key] = fields[key];
                }
                let label = $("#" + key).parent().prev();
                let rule = rules[key];
               /* if (rule.indexOf("required") != -1) {
                    // 增加必填样式
                    $("." + key).addClass("requires");
                } else {
                    // 移除必填样式
                    $("." + key).removeClass("requires");
                }*/
            }

            // 传输设备类型
            let otherTransValue = $("#" + type + "_TRANS_TYPE").combobox("value");
            if (otherTransValue == '5') {
                // 其他设备类型
                $("." + type + "_OTHER_TRANS").show();
            } else {
                $("." + type + "Z_OTHER_TRANS").hide();
                $("#" + type + "_OTHER_TYPE").val("");
            }

            // 重置所有校验
            console.log("校验规则");
            console.log(rules);

   //         meOpera.$orderOperaFrom.validator("setField", rules);

        },
        // 查询电路信息
        qryCircuitInfo : function(){

            // let psIds = '1000248,1000249'; //子流程
            // let psId = meOpera.options.psId;
            let specialtyCode = meOpera.options.specialtyCode;
            let regionId = meOpera.options.regionId;
            // let dispObjTyeValue = meOpera.options.dispObjTyeValue;
            // let dispObjTye = meOpera.options.dispObjTye;
            let param = {};
            param.cstOrdId = meOpera.options.cstOrdId +'';
            param.orderId = meOpera.options.orderId +'';
            param.woIds = meOpera.options.woIds +'';
            param.srvOrdId = meOpera.options.srvOrdId + '';
            param.woState = meOpera.options.woState +'';
            param.tacheId = meOpera.options.tacheId +'';
            param.dealUserId = userInfo.userId +'';
            param.dispObjTyeValue = meOpera.options.dispObjTyeValue +'';
            param.dispObjTye = meOpera.options.dispObjTye +'';
            param.btnFlag = meOpera.options.btnFlag +'';
            param.newCreateResource = "1"; //是否新建资源：否
            if (meOpera.qryCheckFlag()){
                param.woIds = meOpera.options.woId;
                param.specialtyCode = specialtyCode;
                param.regionId = regionId;
                operOrderAction.qrySrvOrdListCheck(param,function (res) {
                    if(res.flag == 1){
                        $("#circuitGrid").grid("reloadData", res.data);
                        meOpera.initCheckInfo(res.data[0]);
                    }else {
                        fish.toast('error', res.message);
                    }
                });
            }else {
                operOrderAction.qrySrvOrdList(param,function (res) {
                    if(res.flag == 1){
                        $("#circuitGrid").grid("reloadData", res.data);
                        meOpera.qryCircuitAreaInfo(res.data[0]);
                        if(meOpera.qryCheckFlag()){
                            meOpera.initCheckInfo(res.data[0]);
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
            let form = meOpera.$orderOperaFrom

            let psId = meOpera.options.psId;
            let tacheId = meOpera.options.tacheId;
            let buttonState = meOpera.options.buttonState;
            let formValue = $('#orderOper-form').form('value');
            if (formValue.remark == null) {
                fish.toast("warn", "说明不能为空");
                return;
            }
            let btnFlag = meOpera.options.btnFlag;
            let params = new Object();
            let circuitData = $("#circuitGrid").grid("getCheckRows");
            let fileData = $("#fileGrid").grid("getRowData");
            let delFilesStr = "";
            for (let j = 0; j < this.delFiles.length; j++) {
                let temp = false;
                for (let i = 0; i < fileData.length; i++) {
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
                let vernier = 0;
                let fileLength = FILES.files.length;
                for (let index = 0; index < fileLength; index++) {
                    let file = FILES.files[index - vernier];
                    let isRemove = true;
                    for (let i = 0; i < fileData.length; i++) {
                        let data = fileData[i];
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
            let flag = -1;
            let message = '';
            $.each(circuitData,function(i,val){
                let orderId = val.ORDER_ID;
                let cstOrdId = val.CST_ORD_ID;
                let ifFlag = orderDetailsAction.queryIfTrack(orderId + '', cstOrdId + '').responseJSON.data;
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
                let operAttrs = new Object();//线条参数
                let tacheOperInfo = new Object();//环节操作数据信息
                let actionFlag = "complateWo";
                if(meOpera.qryCheckFlag()){
                    let circuitNum = -1;
                    $.each(circuitData,function(i,val){
                        if (val.CHECK_STATE != '已保存') {
                            circuitNum = 0;
                        }
                    });
                    if (circuitNum != -1 && tacheId != "500001150"){
                        fish.warn('选择了未保存的电路，请选择已保存的电路进行派发！');
                        return;
                    }
                    if (circuitNum != -1 && sysResource.SYSTEM_RESOURCE!="second-schedule-lt"&&tacheId == "500001150"){
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
                if (tacheId == "500001150"){//核查汇总
                    let estimateFlag = $("input[name='estimateRadio']:checked").val();
                    if (estimateFlag == '0') {
                        operAttrs.IsNeedInvestment = 0;
                    }else if(estimateFlag == '1') {
                        operAttrs.IsNeedInvestment = 1;
                        operAttrs.ifCollect = 1;
                    }
                }else if (tacheId == "500001151"){//投资估算
                    operAttrs.ifFinish = 1;
                    operAttrs.ifCollect = 0;
                }else if (tacheId == "500001144"){//核查调度
                    if (formValue.resRadio == 1){
                        operAttrs.isOutsideCheck = 1;
                        operAttrs.isDataCheck = 1;
                        operAttrs.isTransCheck = 1;
                        operAttrs.isAccessCheck = 1;
                        operAttrs.isOtherCheck = 1;
                        operAttrs.isChangeCheck = 1;
                        operAttrs.isThoughCheckTatal = 0;
                    }
                }
                if(buttonState == "dispConfirm" && tacheId == "500001144"){ //核查调度补单
                    params.checkAddOrder = true;
                }
                tacheOperInfo.remark = formValue.remark;
                params.operAttrsVal = operAttrs;
                params.tacheOperInfo = tacheOperInfo;
                params.actionFlag = actionFlag;
                params.action = "submit";
                $.blockUI({message: '派单中...'});
                if (FILES === null) {
                    meOpera.submitOrder(params);
                }else {
                    meOpera.fileUpdate(params); //上传附件并回单
                }
            }
        },
        submitOrder : function(params){

            if (!meOpera.isSubmit) {
                meOpera.isSubmit = true;
                params.newCreateResource = "1";
                operOrderAction.submitOrder(params, function (res) {
                    meOpera.isSubmit = false;
                    if (res.success) {
                        $.unblockUI();
                        fish.toast('success', res.message);
                        meOpera.popup.close();
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
            let me = this;
            let data = $("#circuitGrid").grid("getCheckRows");
            if (data.length >= 1) {
                let form = meOpera.$orderOperaFrom;
                form.validator("setField", {"remark":""});
                /*if (!form.isValid()) {
                    // 校验未通过
                    fish.toast('warn', "请完整填写正确信息");
                    return;
                }*/
                let param = {};
                param.circuitData = data;
                param.formValue = form.form('value');
                param.formValue.OTHER_ACE_CIR_TYPE = param.formValue.Z_OTHER_ACE_CIR_TYPE;
                param.formValue.ACCESS_CIR_TYPE = param.formValue.Z_ACCESS_CIR_TYPE;
                console.error("提交的信息");
                console.error(param);
                operOrderAction.saveCheckInfo(param,function(res){
                    if (res.success){
                        //核查信息保存完成后，重新刷新电路信息表格数据
                        me.circuitInfo();
                        fish.toast('success', res.message);
                    } else{
                        fish.toast('error', res.message);
                    }
                });
            }else{
                fish.toast('error', "请至少勾选一条电路信息!");
            }
        },
        //核查调度环节保存核查专业
        saveSpecialConfig:function(){
            let data = $("#circuitGrid").grid("getCheckRows");
            let formValue = $('#orderOper-form').form('value');
            if(data.length>0){
                let param = {};
                param.dataInfo = data;
                param.masterValue = '';
                let childFlowSpecialMap = meOpera.getAreaSpecialCheck(formValue);
                if(childFlowSpecialMap != false){

                    let specialtyConfig = {};
                    let specialtyConfigName = {};
                    specialtyConfig = childFlowSpecialMap.checkAreaSpecialArea;
                    specialtyConfigName = childFlowSpecialMap.checkAreaSpecialAreaName;
                    param.specialtyConfig = specialtyConfig;
                    param.specialtyConfigName = specialtyConfigName;
                    param.flowSpecialData = childFlowSpecialMap;
                    param.newCreateResource = "1"; //是否新建资源：否
                    operOrderAction.saveSpecialtyConfigInfo(param,function(res){
                        if (res.success){
                            //专业配置保存完成后，重新刷新电路信息表格数据
                            meOpera.circuitInfo();
                            fish.toast('success', res.message);
                        } else{
                            fish.toast('error', res.message);
                        }
                    });
                }
            }else{
                fish.toast('error', "请至少勾选一条电路信息，并配置专业!");
            }
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
        },
        //核查调度环节选择区域和专业
        getAreaSpecialCheck : function(params){
            let checkAreaSpecialMap = new Object();
            if (params.resRadio == 0) {
                if ($("input[name$='Special']").is(":checked")) {
                    checkAreaSpecialMap.isOutsideCheck = params.outsideSpecial == "outside" ? 0 : 1;
                    checkAreaSpecialMap.isDataCheck = params.dataSpecial == "data" ? 0 : 1;
                    checkAreaSpecialMap.isTransCheck = params.transSpecial == "trans" ? 0 : 1;
                    checkAreaSpecialMap.isAccessCheck = params.accessSpecial == "access" ? 0 : 1;
                    checkAreaSpecialMap.isOtherCheck = params.otherSpecial == "other" ? 0 : 1;
                    checkAreaSpecialMap.isChangeCheck = params.changeSpecial == "change" ? 0 : 1;
                    let checkAreaSpecialArea = new Object();
                    let checkAreaSpecialAreaName = new Object();
                    if(checkAreaSpecialMap.isOutsideCheck == 0){
                        if (params.outsidePopedit != "请选择派发区域！" && params.outsidePopedit != undefined) {
                            checkAreaSpecialArea.outsideOrg = params.outsidePopedit;
                            if ($('#outsidePopedit').popedit('getValue') != null) {
                                checkAreaSpecialAreaName.outsideOrg = $('#outsidePopedit').popedit('getValue').name;
                            }
                        } else {
                            fish.warn('请选择外线专业对应的派发区域！');
                            return false;
                        }
                    }
                    if(checkAreaSpecialMap.isDataCheck == 0){
                        if (params.dataPopedit != "请选择派发区域！" && params.dataPopedit != undefined) {
                            checkAreaSpecialArea.dataOrg = params.dataPopedit;
                            if ($('#dataPopedit').popedit('getValue') != null){
                                checkAreaSpecialAreaName.dataOrg = $('#dataPopedit').popedit('getValue').name;
                            }
                        } else {
                            fish.warn('请选择数据专业对应的派发区域！');
                            return false;
                        }
                    }
                    if(checkAreaSpecialMap.isTransCheck == 0){
                        if (params.transPopedit != "请选择派发区域！" && params.transPopedit != undefined) {
                            checkAreaSpecialArea.transOrg = params.transPopedit;
                            if ($('#transPopedit').popedit('getValue') != null){
                                checkAreaSpecialAreaName.transOrg = $('#transPopedit').popedit('getValue').name;
                            }
                        } else {
                            fish.warn('请选择传输专业对应的派发区域！');
                            return false;
                        }
                    }
                    if(checkAreaSpecialMap.isAccessCheck == 0){
                        if (params.accessPopedit != "请选择派发区域！" && params.accessPopedit != undefined) {
                            checkAreaSpecialArea.accessOrg = params.accessPopedit;
                            if ($('#accessPopedit').popedit('getValue') != null){
                                checkAreaSpecialAreaName.accessOrg = $('#accessPopedit').popedit('getValue').name;
                            }
                        } else {
                            fish.warn('请选择接入专业对应的派发区域！');
                            return false;
                        }
                    }
                    if(checkAreaSpecialMap.isOtherCheck == 0){
                        if (params.otherPopedit != "请选择派发区域！" && params.otherPopedit != undefined) {
                            checkAreaSpecialArea.otherOrg = params.otherPopedit;
                            if ($('#otherPopedit').popedit('getValue') != null){
                                checkAreaSpecialAreaName.otherOrg = $('#otherPopedit').popedit('getValue').name;
                            }
                        } else {
                            fish.warn('请选择其他专业对应的派发区域！');
                            return false;
                        }
                    }
                    if(checkAreaSpecialMap.isChangeCheck == 0){
                        if (params.changePopedit != "请选择派发区域！" && params.changePopedit != undefined) {
                            checkAreaSpecialArea.changeOrg = params.changePopedit;
                            if ($('#changePopedit').popedit('getValue') != null){
                                checkAreaSpecialAreaName.changeOrg = $('#changePopedit').popedit('getValue').name;
                            }
                        } else {
                            fish.warn('请选择交换专业对应的派发区域！');
                            return false;
                        }
                    }
                    checkAreaSpecialMap.isThoughCheckTatal = 1;
                    checkAreaSpecialMap.checkAreaSpecialArea = checkAreaSpecialArea;
                    checkAreaSpecialMap.checkAreaSpecialAreaName = checkAreaSpecialAreaName;
                }else {
                    fish.warn('最少选择一个专业！');
                    return false;
                }
            } /*else if (params.resRadio == 1){
                checkAreaSpecialMap.isOutsideCheck = 1;
                checkAreaSpecialMap.isDataCheck = 1;
                checkAreaSpecialMap.isTransCheck = 1;
                checkAreaSpecialMap.isAccessCheck = 1;
                checkAreaSpecialMap.isOtherCheck = 1;
                checkAreaSpecialMap.isChangeCheck = 1;
                checkAreaSpecialMap.isThoughCheckTatal = 0;
            }*/
            return checkAreaSpecialMap;
        },
        //回显电路对应的专业配置信息
        queryPropertyConfig:function(data){

            let woState = meOpera.options.woState;
            let param = {};
            param.srvOrdId = data.SRV_ORD_ID;
            param.cstOrdId = data.CST_ORD_ID;
            param.orderId = data.ORDER_ID;
            param.activeType = data.ACTIVE_TYPE;
            let tacheId = this.options.tacheId;
            param.newCreateResource = "1";
            operOrderAction.queryPropertyConfig(param,function(res){
                if (res.success){
                    if (res.message != '102,104,105') {
                        if(tacheId == '500001144'){ //核查调度环
                            //每次回显前置灰所有核查专业
                            $("input[name$='Special']").each(function(){
                                $(this).removeAttr("checked");
                                $(this).parent().parent().next().find("input").attr("disabled",true);
                                $(this).parent().parent().next().find("input").popedit('setValue',{name:'请选择派发区域！',value:''});
                            });
                            if (res.configInfo != null) {
                                // $("input[name=masterSelectRadio][value= "+res.keyNote+"]").attr("checked",true);
                                if (res.configInfo.outsideOrg != undefined) {
                                    $("input[name=outsideSpecial]").prop("checked", true);
                                    $("input[name=outsidePopedit]").removeAttr("disabled");
                                    $("#outsidePopedit").popedit('setValue', {name: res.configInfoName.outsideOrg, value: res.configInfo.outsideOrg});
                                }
                                if (res.configInfo.dataOrg != undefined) {
                                    $("input[name=dataSpecial]").prop("checked", true);
                                    $("input[name=dataPopedit]").removeAttr("disabled");
                                    $("#dataPopedit").popedit('setValue', {name: res.configInfoName.dataOrg, value: res.configInfo.dataOrg});
                                }
                                if (res.configInfo.changeOrg != undefined) {
                                    $("input[name=changeSpecial]").prop("checked", true);
                                    $("input[name=changePopedit]").removeAttr("disabled");
                                    $("#changePopedit").popedit('setValue', {name: res.configInfoName.changeOrg, value: res.configInfo.changeOrg});
                                }
                                if (res.configInfo.transOrg != undefined) {
                                    $("input[name=transSpecial]").prop("checked", true);
                                    $("input[name=transPopedit]").removeAttr("disabled");
                                    $("#transPopedit").popedit('setValue', {name: res.configInfoName.transOrg, value: res.configInfo.transOrg});
                                }
                                if (res.configInfo.accessOrg != undefined) {
                                    $("input[name=accessSpecial]").prop("checked", true);
                                    $("input[name=accessPopedit]").removeAttr("disabled");
                                    $("#accessPopedit").popedit('setValue', {name: res.configInfoName.accessOrg, value: res.configInfo.accessOrg});
                                }
                                if (res.configInfo.otherOrg != undefined) {
                                    $("input[name=otherSpecial]").prop("checked", true);
                                    $("input[name=otherPopedit]").removeAttr("disabled");
                                    $("#otherPopedit").popedit('setValue', {name: res.configInfoName.otherOrg, value: res.configInfo.otherOrg});
                                }
                            }
                        }
                    }
                } else{
                    fish.toast('error', res.message);
                }
            });
        },
        qryCircuitAreaInfo : function(param) {
            let srvOrdId = param.SRV_ORD_ID + '';
            console.log("www"+srvOrdId)

            let formValue = $('#orderOper-form').form('value');
            operOrderAction.qryCircuitAreaInfo(srvOrdId,function (res) {
                //将回显方法写入到主调局方法里，是为了避免回显主调局数据后，主调局数据会再次被初始化覆盖
                if (param.STATE =='已配置' || '102,104,105'.indexOf(param.ACTIVE_TYPE) != -1) {//拆机停机复机都回显上个动作的配置信息
                    meOpera.queryPropertyConfig(param)
                }
            });
        },
        initProfessional:function () {
            //初始化专业选择框
            let tacheId =  meOpera.options.tacheId;
            $("input[name$='Popedit']").popedit({
                initialData: {
                    'name': '请选择派发区域！',
                    'value': ''
                },
                open:function(e) {
                    let _this = $(this);
                    let key=_this.get(0).id;
                    let _array = new Array;
                    if(tacheId == '500001144'){ //核查调度
                        if(key=='outsidePopedit'){
                            let _value = $('#outsidePopedit').popedit('getValue');
                            if(_value != null){
                                _value = _value.value+'';
                                _array = _value.split(",");
                            }
                        }else if(key=='dataPopedit'){
                            let _value = $('#dataPopedit').popedit('getValue');
                            if(_value != null){
                                _value = _value.value+'';
                                _array = _value.split(",");
                            }
                        }else if(key=='transPopedit'){
                            let _value = $('#transPopedit').popedit('getValue');
                            if(_value != null){
                                _value = _value.value+'';
                                _array = _value.split(",");
                            }
                        }else if(key=='changePopedit'){
                            let _value = $('#changePopedit').popedit('getValue');
                            if(_value != null){
                                _value = _value.value+'';
                                _array = _value.split(",");
                            }
                        }else if(key=='accessPopedit'){
                            let _value = $('#accessPopedit').popedit('getValue');
                            if(_value != null){
                                _value = _value.value+'';
                                _array = _value.split(",");
                            }
                        }else if(key=='otherPopedit'){
                            let _value = $('#otherPopedit').popedit('getValue');
                            if(_value != null){
                                _value = _value.value+'';
                                _array = _value.split(",");
                            }
                        }
                    }
                    areaPro[key] = _array;
                    let options = {
                        url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/operTreeOrderView',
                        height: 350,
                        width: 400,
                        modal: false,
                        draggable: false,
                        autoResizable: true,
                        viewOption: {
                            flag : "org",
                            orderId : meOpera.options.orderId,
                            key : key,
                            nodeValues :  areaPro[key]
                        },
                        callback: function (popup, view) {
                            popup.result.then(function (res) {
                                let orgNames = '';
                                let orgIds = '';
                                let nodeArray = new Array;
                                res.forEach(function(val,i){
                                    let nodeSin = new Object();
                                    nodeSin.id = val.id;
                                    nodeArray.push(nodeSin);
                                    if(!val.isParent){
                                        if(orgIds==''){
                                            orgNames = val.name ;
                                            orgIds = val.id;
                                        } else {
                                            orgNames = orgNames + ',' + val.name ;
                                            orgIds = orgIds + ',' + val.id;
                                        }
                                    }
                                })
                                _this.popedit('setValue', {name:orgNames, value:orgIds});
                            }, function (e) {
                                console.log('关闭了', e);
                            });
                        }
                    };
                    let popup = fish.popupView(options);
                }

            });
            //核查选专业
            $("input[name$='Special']").bind('click',function(){
                $("input[name$='Special']").each(function(){
                    if($(this).is(":checked") == true){
                        $(this).parent().parent().next().find("input").attr("disabled",false);
                    }else{
                        $(this).parent().parent().next().find("input").attr("disabled",true);
                    }
                })
            });
        }
    });
});