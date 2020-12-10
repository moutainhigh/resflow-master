define(['text!module/UnicomLocalNet/resmaster/portal/checkFlow/templates/checkTotalViewSingle.html',
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/operOrderAction',
    'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/orderDetailsAction',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/operOrderView.css'
], function(operOrderView,operOrderAction,orderStandbyAction,orderDetailsAction,i18n,css) {

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
    };
    return fish.View.extend({
        template: fish.compile(operOrderView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #submitBtn': 'submit',
            'click #checkSaveBtn': 'saveCheckInfo'
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
            meOpera = this;
            URL=this.getRootPath();
            userInfo = orderStandbyAction.queryStaffInfo().responseJSON.data;
            let tacheId = meOpera.options.tacheId;
            let orderId = meOpera.options.orderId + '';
            meOpera.woId=meOpera.options.woId +'';
            let psId = meOpera.options.psId;
            let srvOrdId = meOpera.options.srvOrdId + '';
            let serviceId = meOpera.options.serviceId;
            let resources = meOpera.options.resources;
            let srvBelong = {};

            srvBelong.srvOrdId = srvOrdId;
            sysResource = operOrderAction.qrySrvOrderBelongSys(srvBelong).responseJSON.data;
            console.log(meOpera.options);
            console.log("=============传入页面参数=============");
            srvOrdIds = meOpera.options.srvOrdId;
            //初始化电路信息
            this.circuitInfo();
            this.initFish();
            this.initFileCheckTotal(meOpera.woId);
            this.initCollect();
            // 初始化校验规则
            this.initCheckComboBox();
            meOpera.$orderOperaFrom = this.$("#orderOper-form");
            this.initValidate();
            // 初始化显示隐藏
            this.initShow();
            this.bindChange();
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
            let me = this;
            let tacheId = me.options.tacheId;
            let btnFlag = me.options.btnFlag;
            let psId = me.options.psId;
            $("#circuitGrid").grid({
                colModel: me.initGridInfo(),
                width: 516,
                multiselect: true,
                shrinkToFit: false,
                pageData: me.qrycircuitInfo(),
                onSelectRow: function (e, rowid, state, checked) {//选中行事件
                    let data = $("#circuitGrid").grid("getSelection");
                    let srvOrdId = data.SRV_ORD_ID + '';
                },
                onSelectAll: function (e, status){ //全选事件
                },
                onCellSelect: function (e, rowid, iCol, cellcontent) { // 选中单元格事件
                    let data = $("#circuitGrid").grid("getRowData",rowid);
                    //当iCol为0时，选中的是复选框而不是行数据，则不触发数据回显事件
                    if(data.CHECK_STATE != '已保存' && tacheId != "500001150" && tacheId != "500001151"){

                    }
                    //回显核查反馈信息
                    me.initCheckInfo(data, 1);
                },
                gridComplete: function () {
                }
            });
            // 设置表格高度
            $("#circuitGrid").grid("setGridHeight", 150);
            // 冻结表格复选框一列
            $("#circuitGrid").grid('setFrozenColumns', 1);

        },
        initGridInfo:function(){
            let tacheId = this.options.tacheId;
            let psId = this.options.psId;
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
            let me = this;
            let tacheId = me.options.tacheId; //"500001144";
            let psId = me.options.psId;
            let woState = me.options.woState;
            let orderId = me.options.orderId;
            let woId = me.options.woId;
            let buttonState = me.options.buttonState;
            let btnFlag = me.options.btnFlag;
            let srvOrdId = me.options.srvOrdId + '';
            let serviceId = me.options.serviceId;
            let resources = me.options.resources;
            let formValue = $('#orderOper-form').form('value');

            if(btnFlag == "submit"){
                $("#remarkGoRoll").addClass("requireds");
                //  $("#operOrderViewTitle").text("工单提交");
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
        // 初始化核查反馈页面并查询数据
        initCheckInfo : function (param) {
            let me = this;
            let checkFlag = true;
            let tacheId = me.options.tacheId;
            if(checkFlag) {
                this.feedBackSign = "Z";
                me.queryCheckInfo(param);
            }
        },
        queryCheckInfo : function (param) {
            let me = this;
            let queryParam={};
            queryParam.woId=param.WO_ID;
            queryParam.orderId=param.ORDER_ID;
            queryParam.tacheId=param.TACHE_ID;
            orderDetailsAction.queryCheckInfoBack(queryParam,function (res) {
                let dataArray = res.data;
                // 资源提供方式
                for(let key in dataArray){
                    let value = dataArray[key];
                    if (key.indexOf("_RES_PROVIDE_STAND")  != -1) {
                        $("#" + key).combobox('value',value);
                    }
                }
                for(let key in dataArray){
                    let value = dataArray[key];
                    if (key.indexOf("_BOARD_READY") != -1 || key.indexOf("_TRANS_READY")  != -1
                        || key.indexOf("_TRANS_TYPE")  != -1 || key.indexOf("_OPTICAL_READY")  != -1
                        || key.indexOf("_MUNICIPAL_APPROVAL")  != -1  || key.indexOf("_PROPERTY_REDLINE")  != -1
                        || key.indexOf("_RES_HAVE")  != -1
                        || key.indexOf( "_ACCESS_CIR_TYPE") != -1
                    ) {
                        $("#" + key).combobox('value',value);
                    } else {
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
        baseInitShow: function (type) {
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
        initValidate: function () {
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
        initCheckComboBox: function () {
            let menus = $(".menu");
            let idArray = [];
            let dataMap = {};
            for(let i =0;i< menus.length;i++) {
                let menu = menus[i];
                let selectId = menu.id;
                let param = {};
                let start = selectId.split("_")[0];
                let end = selectId.split(start + "_")[1];
                let dataSource = dataMap[end];
                let placeHolder;
                if (dataSource == null) {
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
                        case "A_CUST_ROOM":
                        case "Z_CUST_ROOM":
                            param.codeType = "CUST_ROOM";
                            placeHolder = "--是否需要客户机房条件--";
                            break;
                        case "A_RES_HAVE":
                        case "Z_RES_HAVE":
                            param.codeType = "RES_HAVE";
                            placeHolder = "--资源是否具备--";
                            break;
                        case "Z_ACCESS_CIR_TYPE":
                            param.codeType = "ACCESS_CIR_TYPE";
                            placeHolder = "--请选择接入电路类型--";
                            break;
                    }
                    orderStandbyAction.queryItemType(param, function (data) {
                        if (selectId.indexOf("_RES_HAVE") != -1) {
                            debugger;
                            if (data == null) {
                                data = [{"name":"是","value":"1"}, {"name":"否","value":"0"}];
                            }
                        }
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
                meOpera.collectData(ele);
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
        changeRequire2: function (selectId) {
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
                if (rule.indexOf("required") != -1) {
                    // 增加必填样式
                    $("." + key).addClass("requires");
                } else {
                    // 移除必填样式
                    $("." + key).removeClass("requires");
                }
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

            meOpera.$orderOperaFrom.validator("setField", rules);

        },
        // 查询电路信息
        qrycircuitInfo : function(){
            let me = this;
            let psIds = '1000248,1000249'; //子流程
            let psId = me.options.psId;
            let specialtyCode = me.options.specialtyCode;
            let regionId = me.options.regionId;
            let dispObjTyeValue = me.options.dispObjTyeValue;
            let dispObjTye = me.options.dispObjTye;
            let param = {};
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
            param.woIds = me.options.woId;
            param.specialtyCode = specialtyCode;
            param.regionId = regionId;
            operOrderAction.qrySrvOrdListCheck(param,function (res) {
                if(res.flag == 1){
                    $("#circuitGrid").grid("reloadData", res.data);
                    me.initCheckInfo(res.data[0],2);
                }else {
                    fish.toast('error', res.message);
                }
            });
            $(window).trigger("resize");
        },

        // 提交工单
        submit:function () {
            let form = meOpera.$orderOperaFrom;
            form.validator("setField", {"remark": "required;length[10~, true]"});

            let me = this;
            let psId = me.options.psId;
            let tacheId = me.options.tacheId;
            let buttonState = me.options.buttonState;
            let formValue = $('#orderOper-form').form('value');
            let btnFlag = me.options.btnFlag;
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

                if(formValue.remark == null){
                    fish.error({title:'提示',message:'说明不能为空'});
                    return;
                }
                //流程线条参数
                if (tacheId == "500001150"){//核查汇总
                    let estimateFlag = $("input[name='estimateRadio']:checked").val();
                    if (estimateFlag == '0') {
                        operAttrs.IsNeedInvestment = 0;
                    }else if(estimateFlag == '1') {
                        operAttrs.IsNeedInvestment = 1;
                        operAttrs.ifCollect = 1;
                        operAttrs.isSendEngineering = 0;
                    }
                }else if (tacheId == "500001151"){//投资估算
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
            let me = this;
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
            let me = this;
            let data = $("#circuitGrid").grid("getCheckRows");
            if (data.length >= 1) {
                let form = meOpera.$orderOperaFrom;
                form.validator("setField", {"remark":""});
                if (!form.isValid()) {
                    // 校验未通过
                    fish.toast('warn', "请完整填写正确信息");
                    return;
                }
                let param = {};
                param.circuitData = data;
                param.formValue = form.form('value');
                param.formValue.OTHER_ACE_CIR_TYPE = param.formValue.Z_OTHER_ACE_CIR_TYPE;
                param.formValue.UPLINK_NODE_PORT = param.formValue.Z_UPLINK_NODE_PORT;
                param.formValue.ACCESS_CIR_TYPE = param.formValue.Z_ACCESS_CIR_TYPE;

                param.formValue.Z_RES_HAVE = $("#Z_RES_HAVE").combobox("value");
                param.formValue.Z_TOTAL_AMOUNT = $("#Z_TOTAL_AMOUNT").val();
                param.formValue.Z_LONGEST_PERIOD = $("#Z_LONGEST_PERIOD").val();
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
        initCollect: function () {
            // $("#A_RES_HAVE").combobox()
        },
        //初始化附件表格
        initFileCheckTotal : function(param) {
            let me = this;
            $("#fileGrid").grid({
                colModel: [
                    {name: 'attachInfoId', label: '附件ID', hidden: true},
                    {name: 'fileName', label: '文件名称', width: 160, sortable: false },
                    {name: 'fileSize', label: '大小', width: 50},
                    {name: 'fileType', label: '类型', width: 40 , sortable: false },
                    {name: 'action', label: '操作', width: 100, formatter: 'actions', formatoptions: {
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
            me.$('#selectFiles').fileupload({
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
                    me.isSubmit = false;
                    if (data.result.success) {
                        $.unblockUI();
                        fish.toast('success', data.result.message);
                        me.popup.close();
                    }else {
                        $.unblockUI();
                        fish.toast('error', data.result.message);
                    }
                }
            });
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
        // 绑定数据变化
        bindChange: function () {
            $(".CHANGE_DATA").on("change", function(ele) {
                meOpera.collectData(ele);
            })
        },
        collectData: function (ele) {
            let type = ele.target.getAttribute("id").split("_")[0];

            // 资源是否具备
            let res = $("#" + type + "_RES_PROVIDE_STAND").combobox("value")==1?1:0;
            $("#"+type + "_RES_HAVE").combobox("value", res);

            // 总金额（”设备金额”、“客户接入工程概算造价”、“工程造价概算”总和）
            let board = $("#"+type+"_BOARD_AMOUNT").val();
            board = board==""?0:board;
            let trans = $("#"+type+"_TRANS_AMOUNT").val();
            trans = trans==""?0:trans;
            let optical = $("#"+type+"_OPTICAL_AMOUNT").val();
            optical = optical==""?0:optical;
            let project = $("#"+type+"_PROJECT_AMOUNT").val();
            project = project==""?0:project;
            let amount = [board,trans,optical,project];

            let allAmount = eval(amount.join("+"));
            $("#"+type+"_TOTAL_AMOUNT").val(allAmount);

            // 工作日(“采购工期”、“传输采购工期”、“光缆及配件采购工期”、“建设工期”4项中取最大值)
            let boardPeriod = $("#"+type+"_BOARD_PERIOD").val();
            boardPeriod = boardPeriod==""?0:boardPeriod;
            let transPeriod = $("#"+type+"_TRANS_PERIOD").val();
            transPeriod = transPeriod==""?0:transPeriod;
            let opticalPeriod = $("#"+type+"_OPTICAL_PERIOD").val();
            opticalPeriod = opticalPeriod==""?0:opticalPeriod;
            let projectPeriod = $("#"+type+"_CONSTRUCT_PERIOD_STAND").val();
            let period = [boardPeriod, transPeriod, opticalPeriod, projectPeriod];
            let maxPeriod = Math.max.apply(null, period);
            $("#"+type+"_LONGEST_PERIOD").val(maxPeriod);
        }
    });
});