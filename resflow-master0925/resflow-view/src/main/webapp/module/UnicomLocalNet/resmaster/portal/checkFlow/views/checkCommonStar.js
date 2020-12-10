define([
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/operOrderAction',
    'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
], function (operOrderAction, orderStandbyAction) {
    let areaPro = {};
    let meOpera, orderId;
    let me;
    let fields = {
        'COLLECT_RES': '',
        'COLLECT_MONEY': 'workDayRep;',
        'COLLECT_DAY': 'moneyRep',
        'COLLECT_DESC': '',
    }
    return {
        initMe:function(meOperaP) {
            me = this;
            meOpera = meOperaP;
            orderId = meOpera.options.orderId;

        },
        getRootPath: function () {
            //获取当前网址，如： http://localhost:8083/uimcardprj/share/meun.jsp
            let curWwwPath = window.document.location.href;
            //获取主机地址之后的目录，如： uimcardprj/share/meun.jsp
            let pathName = window.document.location.pathname;
            let pos = curWwwPath.indexOf(pathName);
            //获取主机地址，如： http://localhost:8083
            let localhostPaht = curWwwPath.substring(0, pos);
            //获取带"/"的项目名，如：/uimcardprj
            let projectName = pathName.substring(0, pathName.substr(1).indexOf('/') + 1);
            return (localhostPaht + projectName);
        },
        //是否需要核查
        initDivShow:function() {
            me.localShow();
            $("input[name = 'resRadio']").bind('click', function () {
                let selectSpecialFlag = $("input[name='resRadio']:checked").val();
                if (selectSpecialFlag == '0') {
                    $("#specialDiv").show(); //选择专业派发区域
                    $("#specialSaveBtnDiv").show(); //保存按钮
                    $("#localScheduleDiv").show(); //选择是否派发专业及本地网核查-是，保存按钮
                    $("#feedBackZDiv").hide(); //反馈信息
                    $("#checkSaveBtnDiv").hide(); //选择是否派发专业及本地网核查-否，反馈信息保存按钮
                } else {
                    $("#specialDiv").hide();
                    $("#specialSaveBtnDiv").hide();
                    $("#localScheduleDiv").hide();
                    $("#feedBackZDiv").show();
                    $("#checkSaveBtnDiv").show();
                }
                meOpera.circuitInfo();
                me.changeRequire2();
                SpecialFlag = selectSpecialFlag;
                me.localShow();
            });
        },
        // 显示本地核查
        localShow:function(){
            let selectSpecialFlag = $("input[name='resRadio']:checked").val();
            if (selectSpecialFlag  == '0' && meOpera.options.psId != "10101322" ) {
                $(".LOCAL").show();
            } else {
                $(".LOCAL").hide();
            }
        },
        //初始化专业选择框
        initCheck:function() {
            $("input[name$='Popedit']").popedit({
                initialData: {
                    'name': '请选择派发区域！',
                    'value': ''
                },
                open: function (e) {
                    let _this = $(this);
                    let key = _this.get(0).id;
                    let _array = new Array;
                    if (key == 'localSchedulePopedit') {
                        let _value = $('#localSchedulePopedit').popedit('getValue');
                        if (_value != null) {
                            _value = _value.value + '';
                            _array = _value.split(",");
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
                            flag: "org",
                            orderId: orderId,
                            key: key,
                            nodeValues: areaPro[key]
                        },
                        callback: function (popup, view) {
                            popup.result.then(function (res) {
                                let orgNames = '';
                                let orgIds = '';
                                let nodeArray = new Array;
                                res.forEach(function (val, i) {
                                    let nodeSin = new Object();
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
                    let popup = fish.popupView(options);
                }

            });
            //核查选专业
            $("input[name$='Special']").bind('click', function () {
                $("input[name$='Special']").each(function () {
                    if ($(this).is(":checked") == true) {
                        // $(this).parent().parent().next().find("input").attr("disabled", false);
                        $("#localSchedulePopedit").popedit('enable'); //禁用控件
                    } else {
                        //$(this).parent().parent().next().find("input").attr("disabled", true);
                        $("#localSchedulePopedit").popedit('disable'); //启用控件
                    }
                })
            });
        },
        initShow:function() {
            let res = $('#COLLECT_RES').combobox("value");
            if (res == "0") {
                // 不满足
                $(".Z_DAY").show();
                // $(".Z_AMOUNT").addClass("requires");
            } else {
                // 满足
                $(".Z_DAY").hide();
                // $(".Z_AMOUNT").removeClass("requires");
                $("#COLLECT_MONEY").val('');
            }
        },
        initCheckComboBox:function() {
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
                switch (selectId) {
                    case "COLLECT_RES":
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
                }
                if (dataSource == null) {
                    orderStandbyAction.queryItemType(param, function (data) {
                        if (data.length == 0) {
                            data =  [
                                {name: '满足', value: '1'},
                                {name: '不满足', value: '0'}
                            ]
                            // fish.toast("warn", "未配置枚举类" + param.codeType);
                            console.error("未配置枚举类" + param.codeType)
                        } else {
                            console.error("枚举类" + selectId +"枚举值" +param.codeType);
                            console.error(data);
                        }
                        dataMap[end] = data;
                        idArray.push(end);
                        me.initBox(placeHolder, selectId , data);
                    });
                } else {
                    me.initBox(placeHolder, selectId , dataSource);
                }
            }
        },
        initBox : function (holder, selectId, data) {
            let element = $("#" + selectId);
            element.combobox({
                placeholder: holder,
                dataTextField: 'name',
                dataValueField: 'value',
                dataSource: data
            });

            // 绑定change事件
            element.on('combobox:change', function(ele) {
                me.initShow();
                me.changeRequire2();
            })
        },
        changeRequire2:function() {
            let selectSpecialFlag = $("input[name='resRadio']:checked").val();
            let hideKeyArray = [];
     //       if (selectSpecialFlag == 0) {
                // 派发专业:是，所有核查字段不校验
                for (let field in fields) {
                    hideKeyArray.push(field);
                }
        /*    } else {
                // 派发专业:否
                let resHave = $('#COLLECT_RES').combobox('value');
                if(resHave === "1") {
                    hideKeyArray.push("COLLECT_DAY");
                }
                hideKeyArray.push("COLLECT_DESC");
                hideKeyArray.push("COLLECT_MONEY");
            }*/


            let rules={};
            for (let key in fields) {
                if (hideKeyArray.indexOf(key) !== -1) {
                    rules[key] = "";
                } else {
                    rules[key] = fields[key];
                }
                let rule = rules[key];
              /*  if (rule.indexOf("required") != -1) {
                    // 增加必填样式
                    $("." + key).addClass("requires");
                } else {
                    // 移除必填样式
                    $("." + key).removeClass("requires");
                }*/
            }

            // 重置所有校验
            console.log("校验规则");
            console.log(rules);

      //      meOpera.$orderOperaFrom.validator("setField", rules);

        },

        initValidate:function() {
            meOpera.$orderOperaFrom.validator({
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
        }
    }
});
