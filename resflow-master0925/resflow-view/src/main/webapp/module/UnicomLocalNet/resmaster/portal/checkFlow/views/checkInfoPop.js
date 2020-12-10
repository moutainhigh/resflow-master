define([
    'text!module/UnicomLocalNet/resmaster/portal/checkFlow/templates/checkInfoPop.html',
    'module/UnicomLocalNet/resmaster/portal/orderTacheDealView/action/operOrderAction',
    'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
    'module/UnicomLocalNet/resmaster/portal/checkFlow/views/checkCommon',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderTacheDealView/styles/operOrderView.css'
], function(operOrderView, operOrderAction,orderStandbyAction, checkCommon,i18n,css) {
    let srvOrdIds;
    let opera;
    let fields = {
        'A_BOARD_READY': 'required;',
        'A_BOARD_PERIOD': 'required;workDayRep;',
        'A_BOARD_AMOUNT': 'required;moneyRep',
        'A_BOARD_TYPE': 'required;',
        'A_TRANS_READY': 'required;',
        'A_TRANS_PERIOD': 'required;workDayRep;',
        'A_TRANS_AMOUNT': 'required;moneyRep',
        'A_TRANS_TYPE': 'required;',
        'A_OPTICAL_READY': 'required;',
        'A_OPTICAL_PERIOD': 'required;workDayRep;',
        'A_OPTICAL_AMOUNT': 'required;moneyRep',
        'A_MUNICIPAL_APPROVAL': 'required;',
        'A_APPROVAL_PERIOD': 'required;workDayRep;',
        'A_CONSTRUCT_PERIOD_STAND': 'required;',
        'A_PROJECT_AMOUNT': 'required;moneyRep',
        'A_PROJECT_OVERVIEW': 'required;',
        'A_RES_DESC': 'required;',
        'A_PROPERTY_REDLINE': 'required;',
        'A_PROPERTY_DESC': 'required;',
        'A_RES_EXPLORER': 'required;',
        'A_RES_EXPLOR_CONTACT': 'required;',
        'A_UNABLE_RELOVE': 'required;',

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
        'Z_PROJECT_AMOUNT': 'required;moneyRep',
        'Z_PROJECT_OVERVIEW': 'required;',
        'Z_RES_DESC': 'required;',
        'Z_PROPERTY_REDLINE': 'required;',
        'Z_PROPERTY_DESC': 'required;',
        'Z_RES_EXPLORER': 'required;',
        'Z_RES_EXPLOR_CONTACT': 'required;',
        'Z_UNABLE_RELOVE': 'required;',
        'Z_ACCESS_CIR_TYPE': 'required;',
        'Z_OTHER_ACE_CIR_TYPE': 'required;',
    }
    return fish.View.extend({
        template: fish.compile(operOrderView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #saveCheckInfoLocal': 'saveCheckInfoLocal',
        },

        initialize: function() {
            this.render();
            URL = "";
            FILES = null; //附件
            userInfo = orderStandbyAction.queryStaffInfo().responseJSON.data; //用户信息
        },

        //渲染页面
        render: function() {
            this.$el.html(this.template(this.i18nData));
        },

        //初始化fish组件
        afterRender: function(options) {
            URL=checkCommon.getRootPath();
            opera = this;
            opera.$orderOperaFrom = opera.$("#checkInfo");
            opera.initShow();
            opera.initCheckComboBox();
            opera.bindChangeData();
        },
        initShow: function (type) {
            if (type == null) {
                opera.baseInitShow("A");
                opera.baseInitShow("Z");
            } else {
                opera.baseInitShow(type);
            }

        },
        baseInitShow(type) {
            opera.$("." + type + "_DIV").hide();
            opera.$("." + type + "_ALL").show();
            let Select = opera.$("#" + type + "_RES_PROVIDE_STAND").combobox('value');
            switch (Select) {
                case "1":
                    //现有资源-资源完全具备 不需要O侧反馈其他的信息
                    break;
                case "2":
                    //、现有资源-客户端联通光缆已接入且客户机房的联通设备具备但需要新增设备板卡
                    opera.$("." + type + "_BOARD").show();
                    break;
                case "3":
                    opera.$("." + type + "_TRANS").show();
                    break;
                case "4":
                    opera.$("." + type + "_TRANS").show();
                    opera.$("." + type + "_OPTICAL").show();
                    opera.$("." + type + "_PROJECT_DESC").show();
                    opera.$("." + type + "_APPROVAL").show();
                    break;
                case "5":
                    //需做到客户端机房光缆管道接入工程（含租用）及在客户机房新建联通设备工程
                    opera.$("." + type + "_TRANS").show();
                    opera.$("." + type + "_OPTICAL").show();
                    opera.$("." + type + "_PROJECT_DESC").show();
                    opera.$("." + type + "_APPROVAL").show();
                    break;
                case "6":
                    opera.$("." + type + "_PROJECT").show();
                    opera.$("." + type + "_PROJECT_DESC").show();
                    break;
                case "7":
                    opera.$("." + type + "_ALL").hide();
                    opera.$("." + type + "_RELOVE").show();
                    break;
                default:
                    break;
            }
        },
        initCheckComboBox() {
            let menus = opera.$(".menu");
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
                switch (selectId) {
                    case "A_RES_HAVE":
                    case "Z_RES_HAVE":
                        param.codeType = "RES_HAVE";
                        placeHolder = "--资源是否具备--";
                        break;
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
                        placeHolder = "--是否需要客户机房条件--";
                        break;
                    case "A_MUNICIPAL_APPROVAL":
                    case "Z_MUNICIPAL_APPROVAL":
                        param.codeType = "MUNICIPAL_APPROVAL";
                        placeHolder = "--是否需要市政报批--";
                        break;
                }
                if (dataSource == null) {
                    let data = opera.queryItemType(param).responseJSON.data;
                    if (data.length == 0) {
                        data =  [
                            {name: '满足', value: '1'},
                            {name: '不满足', value: '0'}
                        ]
                        // fish.toast("warn", "未配置枚举类" + param.codeType);
                        console.error("未配置枚举类" + param.codeType)
                    } else {
                        console.error("枚举类" + selectId + "枚举值" + param.codeType);
                        console.log(data);
                    }
                    dataMap[end] = data;
                    idArray.push(end);
                    opera.initBox(placeHolder, selectId , data);
                } else {
                    opera.initBox(placeHolder, selectId , dataSource);
                }
            }
            opera.initCheckInfoData();
            $.unblockUI();
        },
        //单据类型下拉
        queryItemType:function (jsonObject){
            return  ngc.callServerFunction("com.zres.project.localnet.portal.dict.service.UnicomSysDictServiceIntf","querySysDict", jsonObject);
        },
        initBox : function (holder, selectId, data) {
            let element = opera.$("#" + selectId);
            element.combobox({
                placeholder: holder,
                dataTextField: 'name',
                dataValueField: 'value',
                dataSource: data,
            });
            if (selectId.indexOf('RES_HAVE') == -1) {
                element.on('combobox:change', function (ele) {
                    let type = selectId.split("_")[0];
                    // 资源提供方式
                    if (selectId.indexOf('RES_PROVIDE_STAND') != -1) {
                        // 显示隐藏
                        opera.initShow(type);
                        opera.initDefault(type, selectId);
                    }
                    opera.changeRequire2(selectId);
                    opera.collectData(ele);
                })
            }
        },
        initDefault (type, selectId) {
            let approval ='', redLine='2',  cusRoom = '2';
            let res = $("#" + selectId).combobox("value");
            switch (res) {
                case "1":
                    //现有资源-资源完全具备 不需要O侧反馈其他的信息
                case "2":
                    //、现有资源-客户端联通光缆已接入且客户机房的联通设备具备但需要新增设备板卡
                case "3":
                    break;
                case "4":
                    approval = '2';
                    break;
                case "5":
                    //需做到客户端机房光缆管道接入工程（含租用）及在客户机房新建联通设备工程
                    approval = '2';
                    break;
                case "6":
                    break;
                case "7":
                    // 无法解决
                    redLine =''
                    cusRoom ='';
                    break;
                default:
                    break;
            }

            opera.$("." + type + "_INPUT").val('');//清空已填的值
            // 恢复选项默认值
            opera.$("#" + type + "_BOARD_READY").combobox('value', '');
            opera.$("#" + type + "_TRANS_READY").combobox('value', '');
            opera.$("#" + type + "_TRANS_TYPE").combobox('value', '');
            opera.$("#" + type + "_OPTICAL_READY").combobox('value', '');

            opera.$("#" + type + "_PROPERTY_REDLINE").combobox('value', redLine);
            opera.$("#" + type + "_MUNICIPAL_APPROVAL").combobox('value', approval);
            opera.$("#" + type + "_CUST_ROOM").combobox('value', cusRoom);
        },
        changeRequire2(selectId) {
            let hideKeyArray = [];
            let type = selectId.split("_")[0];

            // 隐藏的模块
            let hideDiv = opera.$("." + type + "_DIV:hidden");
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
            let board = opera.$("#" + type + "_BOARD_READY").combobox('value');
            console.log("板卡" + board);
            if (board != "2") {
                let boardKey = type + "_BOARD_PERIOD";
                hideKeyArray.push(boardKey);
            }
            // 传输备货
            let trans = opera.$("#" + type + "_TRANS_READY").combobox("value");
            console.log("传输备货" + trans);
            if (trans != "2") {
                let transKey = type + "_TRANS_PERIOD";
                hideKeyArray.push(transKey);
            }
            // 光缆备货
            let optical = opera.$("#" + type + "_OPTICAL_READY").combobox("value");
            console.log("光缆备货" + optical);
            if (optical!= "2") {
                let opticalKey = type + "_OPTICAL_PERIOD";
                hideKeyArray.push(opticalKey);
            }
            // 物业红线
            let redLine = opera.$("#" + type + "_PROPERTY_REDLINE").combobox("value");
            console.log("物业红线" + redLine);
            if (redLine == "2") {
                let redLineKey = type + "_PROPERTY_DESC";
                hideKeyArray.push(redLineKey);
            }
            //市政报批
            let approval = opera.$("#" + type + "_MUNICIPAL_APPROVAL").combobox("value");
            console.log("市政报批" + approval);
            if (approval == "2") {
                let approvalKey = type + "_APPROVAL_PERIOD";
                hideKeyArray.push(approvalKey);
            }

            // 资源无法解决
            let resValue = opera.$("#" + type + "_RES_PROVIDE_STAND").combobox('value');
            console.log("堪察人" + resValue);
            if (resValue == "7") {
                let resKey = type + "_RES_EXPLORER";
                hideKeyArray.push(resKey);
            }
            let rules={};
            for (let key in fields) {
                if (hideKeyArray.indexOf(key) != -1) {
                    rules[key] = "";
                } else if (key.indexOf(type + "_") == -1) {
                    continue;
                } else  {
                    rules[key] = fields[key];
                }
                let label = opera.$("#" + key).parent().prev();
                let rule = rules[key];
                if (rule.indexOf("required") != -1) {
                    // 增加必填样式
                    opera.$("." + key).addClass("requires");
                } else {
                    // 移除必填样式
                    opera.$("." + key).removeClass("requires");
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

            opera.$orderOperaFrom.validator("setField", rules);
        },
        // 初始化汇总反馈信息
        initCheckInfoData() {
            let checkInfo = opera.options;
            console.error("页面打开, 传递的核查信息");
            console.log(checkInfo);
            for(let key in checkInfo){
                let value = checkInfo[key];
                if (key.indexOf("_RES_PROVIDE_STAND")  != -1
                    || key.indexOf("_BOARD_READY") != -1 || key.indexOf("_TRANS_READY")  != -1
                    || key.indexOf("_TRANS_TYPE")  != -1 || key.indexOf("_OPTICAL_READY")  != -1
                    || key.indexOf("_MUNICIPAL_APPROVAL")  != -1  || key.indexOf("_PROPERTY_REDLINE")  != -1
                    || key.indexOf("_RES_HAVE")  != -1
                ) {
                    opera.$("#" + key).combobox('value',value);
                }
            }
            for(let key in checkInfo){
                let value = checkInfo[key];
                if (key.indexOf("_RES_PROVIDE_STAND")  != -1
                    || key.indexOf("_BOARD_READY") != -1 || key.indexOf("_TRANS_READY")  != -1
                    || key.indexOf("_TRANS_TYPE")  != -1 || key.indexOf("_OPTICAL_READY")  != -1
                    || key.indexOf("_MUNICIPAL_APPROVAL")  != -1  || key.indexOf("_PROPERTY_REDLINE")  != -1
                    || key.indexOf("_RES_HAVE")  != -1
                ) {}else{
                    opera.$("#"+key).val(value);
                }
            }
        },
        bindChangeData () {
            $(".CHANGE_DATA").on("change", function(ele) {
                opera.collectData(ele);
            })
        },
        // 汇总A Z端
        collectData(ele) {
            let type = ele.target.getAttribute("id").split("_")[0];

            // 资源是否具备
            let res = opera.$("#" + type + "_RES_PROVIDE_STAND").combobox("value")==1?1:0;
            opera.$("#"+type + "_RES_HAVE").combobox("value", res);

            // 总金额（”设备金额”、“客户接入工程概算造价”、“工程造价概算”总和）
            let board = opera.$("#"+type+"_BOARD_AMOUNT").val();
            board = board==""?0:board;
            let trans = opera.$("#"+type+"_TRANS_AMOUNT").val();
            trans = trans==""?0:trans;
            let optical = opera.$("#"+type+"_OPTICAL_AMOUNT").val();
            optical = optical==""?0:optical;
            let project = opera.$("#"+type+"_PROJECT_AMOUNT").val();
            project = project==""?0:project;
            let amount = [board,trans,optical,project];

            let allAmount = eval(amount.join("+"));
            opera.$("#"+type+"_TOTAL_AMOUNT").val(allAmount);

            // 工作日(“采购工期”、“传输采购工期”、“光缆及配件采购工期”、“建设工期”4项中取最大值)
            let boardPeriod = opera.$("#"+type+"_BOARD_PERIOD").val();
            boardPeriod = boardPeriod==""?0:boardPeriod;
            let transPeriod = opera.$("#"+type+"_TRANS_PERIOD").val();
            transPeriod = transPeriod==""?0:transPeriod;
            let opticalPeriod = opera.$("#"+type+"_OPTICAL_PERIOD").val();
            opticalPeriod = opticalPeriod==""?0:opticalPeriod;
            let projectPeriod = opera.$("#"+type+"_CONSTRUCT_PERIOD_STAND").val();
            let period = [boardPeriod, transPeriod, opticalPeriod, projectPeriod];

            let maxPeriod = Math.max.apply(null, period)
            opera.$("#"+type+"_LONGEST_PERIOD").val(maxPeriod);
        },
        saveCheckInfoLocal: function () {
            let form = opera.$("#checkInfo");
            console.log(checkInfo);
            if (form.isValid()) {
                let checkInfo = form.form("value");
                this.popup.close(checkInfo);
            }
        }
    });
});