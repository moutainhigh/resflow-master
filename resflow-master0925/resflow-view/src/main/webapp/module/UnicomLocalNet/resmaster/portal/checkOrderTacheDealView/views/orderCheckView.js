define(['text!module/UnicomLocalNet/resmaster/portal/checkOrderTacheDealView/templates/orderCheckView.html',
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/operOrderAction',
    'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/operOrderView.css'
], function (operOrderView, operOrderAction, orderStandbyAction, i18n, css) {
    var dispatchNum = '-1';
    var SpecialFlag;
    var srvOrdIds;
    var userInfo;

    return fish.View.extend({
        template: fish.compile(operOrderView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #submitBtn': 'submit',
            'click #specialSaveBtn': 'saveSpecialConfig',
            'click #checkSaveBtn': 'saveCheckInfo',
        },

        initialize: function () {
            this.render();
            URL = "";
            FILES = null;
            areaPro = {};
            jobData = {};//岗位
            userData = {};//人员
            psIdFlow = {};
        },

        //渲染页面
        render: function () {
            this.$el.html(this.template(this.i18nData));
        },

        //初始化fish组件
        afterRender: function () {
            SpecialFlag = '-1';
            URL = this.getRootPath();
            var me = this;
            userInfo = orderStandbyAction.queryStaffInfo().responseJSON.data;
            var tacheId = me.options.tacheId;
            var orderId = me.options.orderId + '';
            var woId = me.options.woId + '';
            var psId = me.options.psId;
            var srvOrdId = me.options.srvOrdId + '';
            var psIds = '1000248,1000249';
            if (psIds.indexOf(psId) != -1) {
                psIdFlow = operOrderAction.getMainFlowPsId(orderId).responseJSON.data;
            }
            srvOrdIds = me.options.srvOrdId;
            this.initFish();
            //初始化附件表格
            this.initFileUpdate(woId);
            //初始化电路信息
            this.circuitInfo();

            //是否需要核查
            $("input[name = 'resRadio']").bind('click',function(){
                var selectSpecialFlag = $("input[name='resRadio']:checked").val();
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
                me.circuitInfo();
                SpecialFlag = selectSpecialFlag;
            });
            operOrderAction.qryAllSpecialCheckWoOrder(orderId,function (res) {
                if (res.length > 0) {
                    $("input[name='resRadio']").each(function(){
                        $(this).prop("disabled",true);
                    });
                }
            })

            var me = this;
            //初始化专业选择框
            $("input[name$='Popedit']").popedit({
                initialData: {
                    'name': '请选择派发区域！',
                    'value': ''
                },
                open: function (e) {
                    var _this = $(this);
                    var key = _this.get(0).id;
                    var _array = new Array;
                    if (key == 'localSchedulePopedit') {
                        var _value = $('#localSchedulePopedit').popedit('getValue');
                        if (_value != null) {
                            _value = _value.value + '';
                            _array = _value.split(",");
                        }
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
                            key: key,
                            nodeValues: areaPro[key]
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
        initFish: function () {
            var me = this;
            var tacheId = me.options.tacheId; //"510101052";
            var psIds = '1000248,1000249';
            var psId = me.options.psId;
            var woState = me.options.woState;
            var orderId = me.options.orderId;
            var woId = me.options.woId;
            var buttonState = me.options.buttonState;
            var btnFlag = me.options.btnFlag;
            var srvOrdId = me.options.srvOrdId + '';
            var formValue = $('#orderOper-form').form('value');
            if (btnFlag == "submit") {
                //父流程环节
                if (tacheId == "510101052") { //核查调度
                    if (buttonState != "dispConfirm") {
                        $("#selectSpecialDiv").show();
                    }
                    $("#specialDiv").show();
                    $("#localScheduleDiv").show();
                    $('#specialSaveBtnDiv').show();
                }
            }
            $("input[name$='RES_SATISFY']").combobox({
                placeholder: '--请选择资源是否满足--',
                dataTextField: 'name',
                dataValueField: 'value',
                dataSource: [
                    {name: '满足', value: '0'},
                    {name: '不满足', value: '1'}
                ]
            });
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
        submit: function () {
            var me = this;
            var psId = me.options.psId;
            var tacheId = me.options.tacheId;
            var buttonState = me.options.buttonState;
            var formValue = $('#orderOper-form').form('value');
            var btnFlag = me.options.btnFlag;
            var serviceId =  me.options.serviceId;
            var params = new Object();
            var circuitData = $("#circuitGrid").grid("getCheckRows");
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
                var ifFlag = operOrderAction.queryIfTrack(orderId + '', cstOrdId + '').responseJSON.data;
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
            if (btnFlag == "submit") {//回单
                var operAttrs = new Object();//线条参数
                var tacheOperInfo = new Object();//环节操作数据信息
                var actionFlag = "complateWo";
                // 资源配置按钮是否隐藏状态
                if ((tacheId == "510101052" && formValue.resRadio == 0)) {
                    //电路调度  核查调度
                    var circuitNum = -1;
                    $.each(circuitData, function (i, val) {
                        if (val.STATE != '已配置') {
                            circuitNum = 0;
                        }
                    });
                    if (circuitNum != -1 ){
                        fish.warn('选择了未配置的电路，请选择已配置的电路进行派发！');
                        return;
                    }
                }
                if ((tacheId == "510101052" && formValue.resRadio == 1)) {
                    //电路调度  核查调度
                    var circuitNum = -1;
                    $.each(circuitData, function (i, val) {
                        if (val.CHECK_STATE != '已保存') {
                            circuitNum = 0;
                        }
                    });
                    if (circuitNum != -1 ){
                        fish.warn('选择了未保存的电路，请选择已保存的电路进行派发！');
                        return;
                    }
                }
                //流程线条参数
                if (tacheId == "510101052") {//核查调度
                    if (formValue.resRadio == 1) {
                        operAttrs.isDataCheck = 1; //是否数据核查：否
                        operAttrs.isTransCheck = 1;  //是否传输核查：否
                        operAttrs.isOtherCheck = 1;  //是否其他专业核查：否
                        operAttrs.isChangeCheck = 1;  //是否交换核查：否
                        operAttrs.isLocalCheck = 1;  //是否本地网核查：否
                        operAttrs.Is_Return = 0; //是否回退：否
                        operAttrs.is_transfer_profe_Local='N'; //是否核查： 否
                    }
                }

                if (buttonState == "dispConfirm" && tacheId == "510101052") { //核查调度补单
                    params.checkAddOrder = true;
                }
                tacheOperInfo.remark = formValue.remark;
                params.operAttrsVal = operAttrs;
                params.tacheOperInfo = tacheOperInfo;
                params.actionFlag = actionFlag;
                params.action = "submit";
                params.serviceId=serviceId;
                $("#orderOper-form").blockUI({message: '派单中...'}).data('blockui-content', true);
                if (FILES === null) {
                    me.submitOrder(params);
                } else {
                    me.fileUpdate(params); //上传附件并回单
                }
            }
        },
        submitOrder: function (params) {
            var me = this;
            operOrderAction.submitOrder(params, function (res) {
                if (res.success) {
                    $("#orderOper-form").unblockUI().data('blockui-content', false);
                    fish.toast('success', res.message);
                    me.popup.close();
                } else {
                    fish.toast('error', res.message);
                }
            });
        },

        //核查调度环节选择区域和专业
        getAreaSpecialCheck: function (params) {
            var checkAreaSpecialMap = new Object();
            if (params.resRadio == 0) {
                if ($("input[name$='Special']").is(":checked")) {
                    checkAreaSpecialMap.isDataCheck = params.dataSpecial == "data" ? 0 : 1;
                    checkAreaSpecialMap.isTransCheck = params.transSpecial == "trans" ? 0 : 1;
                    checkAreaSpecialMap.isOtherCheck = params.otherSpecial == "other" ? 0 : 1;
                    checkAreaSpecialMap.isChangeCheck = params.changeSpecial == "change" ? 0 : 1;
                    checkAreaSpecialMap.isLocalCheck = params.localScheduleSpecial == "localSchedule" ? 0 : 1;
                    var checkAreaSpecialArea = new Object();
                    var checkAreaSpecialAreaName = new Object();
                    if (checkAreaSpecialMap.isLocalCheck == 0) {
                        if (params.localSchedulePopedit != "请选择派发区域！" && params.localSchedulePopedit != undefined) {
                            checkAreaSpecialArea.localScheduleOrg = params.localSchedulePopedit;
                            if ($('#localSchedulePopedit').popedit('getValue') != null) {
                                checkAreaSpecialAreaName.localScheduleOrg = $('#localSchedulePopedit').popedit('getValue').name;
                            }
                        } else {
                            fish.warn('请选择本地网的派发区域！');
                            return false;
                        }
                    }
                    if (checkAreaSpecialMap.isDataCheck == 0) {
                        checkAreaSpecialArea.isDataCheck = 0;
                    }
                    if (checkAreaSpecialMap.isTransCheck == 0) {
                        checkAreaSpecialArea.isTransCheck = 0;
                    }
                    if (checkAreaSpecialMap.isChangeCheck == 0) {
                        checkAreaSpecialArea.isChangeCheck = 0;
                    }
                    if (checkAreaSpecialMap.isOtherCheck == 0) {
                        checkAreaSpecialArea.isOtherCheck = 0;
                    }
                    checkAreaSpecialMap.is_transfer_profe_Local = 'Y'; //是否流转到专业及本地核查Y(是);N(否);
                    checkAreaSpecialMap.Is_Return = 0 //是否回退申请单 1(是);0(否);
                    checkAreaSpecialMap.checkAreaSpecialArea = checkAreaSpecialArea;
                    checkAreaSpecialMap.checkAreaSpecialAreaName = checkAreaSpecialAreaName;
                } else {
                    fish.warn('请至少选择一个核查对象！');
                    return false;
                }
            } else if (params.resRadio == 1) {
                checkAreaSpecialMap.isDataCheck = 1;
                checkAreaSpecialMap.isTransCheck = 1;
                checkAreaSpecialMap.isOtherCheck = 1;
                checkAreaSpecialMap.isChangeCheck = 1;
                checkAreaSpecialMap.isLocalCheck = 1;
                checkAreaSpecialMap.is_transfer_profe_Local = 'N'; //是否流转到专业及本地核查Y(是);N(否);
                checkAreaSpecialMap.Is_Return = 0 //是否回退申请单 1(是);0(否);
            }
            return checkAreaSpecialMap;
        },

        circuitInfo: function () {
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
                    if (tacheId != "500001155" && tacheId != "500001157") {
                        if (data.STATE == '已配置' && !(me.qryCheckFlag())) {
                            //回显专业配置信息
                            me.qryCircuitAreaInfo(data)
                        } else if (data.STATE == '未配置') {
                            if (tacheId == "510101052") {//核查调度
                                //置灰所有核查专业
                                $("input[name$='Special']").each(function () {
                                    $(this).removeAttr("checked");
                                   // $(this).parent().parent().next().find("input").attr("disabled", true);
                                    $(this).parent().parent().next().find("input").popedit('setValue', {
                                        name: '请选择派发区域！',
                                        value: ''
                                    });
                                    $("#localSchedulePopedit").popedit('disable'); //禁用控件
                                });
                            }
                        }
                    }

                },
                onSelectAll: function (e, status) { //全选事件

                },
                onCellSelect: function (e, rowid, iCol, cellcontent) {
                    // debugger;
                    var data = $("#circuitGrid").grid("getRowData", rowid);
                    if (me.qryCheckFlag()) {
                        if (data.CHECK_STATE == '已保存' || tacheId == "510101052") {
                            //回显核查反馈信息
                            me.initCheckInfo(data)
                        } else {
                            $('#A_CONSTRUCT_SCHEME').val('');
                            // $('#A_RES_SATISFY').val('');
                            $('#A_RES_SATISFY').combobox('clear');
                            $('#A_ACCESS_ROOM').val('');
                            $('#A_INVESTMENT_AMOUNT').val('');
                            $('#A_CONSTRUCT_PERIOD').val('');
                            $('#Z_CONSTRUCT_SCHEME').val('');
                            // $('#Z_RES_SATISFY').val('');
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
                    if (userInfo.areaId == '350002000000000042766427') { //如果是海南用户
                        if (tacheId == '500001155' || tacheId == '500001157') {
                            if (btnFlag == 'submit') {
                                $("#circuitGrid").grid("showCol", 'DF_ORDER_CONFIG');
                            }
                        }
                    }
                }
                // }
            });
            // 设置表格高度
            $("#circuitGrid").grid("setGridHeight", 150);
            // 冻结表格复选框一列
            $("#circuitGrid").grid('setFrozenColumns', 1);

        },
        qrycircuitInfo: function () {
            var me = this;
            var psIds = '1000248,1000249'; //子流程
            var psId = me.options.psId;
            var specialtyCode = me.options.specialtyCode;
            var regionId = me.options.regionId;
            var dispObjTyeValue = me.options.dispObjTyeValue;
            var dispObjTye = me.options.dispObjTye;
            var param = {};
            param.cstOrdId = me.options.cstOrdId + '';
            param.woState = me.options.woState + '';
            param.tacheId = me.options.tacheId + '';
            param.dealUserId = userInfo.userId + '';
            param.dispObjTyeValue = me.options.dispObjTyeValue + '';
            param.dispObjTye = me.options.dispObjTye + '';
            param.btnFlag = me.options.btnFlag + '';
            //     var  = me.options.btnFlag;//
            if (psIds.indexOf(psId) != -1) {
                param.specialtyCode = specialtyCode;
                param.regionId = regionId;
                operOrderAction.qrySrvOrdChildList(param, function (res) {
                    if (res.flag == 1) {
                        $("#circuitGrid").grid("reloadData", res.data);
                        me.qryCircuitAreaInfo(res.data[0]);
                        me.initResourceConstructFn(res.data[0]);
                    } else {
                        fish.toast('warn', res.message);
                    }
                });
            } else if (me.qryCheckFlag()) {
                param.specialtyCode = specialtyCode;
                param.regionId = regionId;
                operOrderAction.qrySrvOrdListCheck(param, function (res) {
                    if (res.flag == 1) {
                        $("#circuitGrid").grid("reloadData", res.data);
                        //      me.qryCircuitAreaInfo(res.data[0]);
                        me.initCheckInfo(res.data[0]);
                    } else {
                        fish.toast('warn', res.message);
                    }
                });
            } else {
                operOrderAction.qrySrvOrdList(param, function (res) {
                    if (res.flag == 1) {
                        $("#circuitGrid").grid("reloadData", res.data);
                        me.qryCircuitAreaInfo(res.data[0]);
                        if (me.qryCheckFlag()) {
                            me.initCheckInfo(res.data[0]);
                        }

                    } else {
                        fish.toast('warn', res.message);
                    }
                });
            }
            //(specialtyCode != null && specialtyCode != null)
        },

        validCheckInfo: function (param) {
            var data = param.circuitData;
            var formValue = param.formValue;
            var me = this;
            var tacheId = me.options.tacheId;
            // 核查汇总500001150、投资估算500001151
            var isValidSign = !(tacheId == "500001150" || tacheId == "500001151");
            var feedBackSign = data[0].FEEDBACKSIGN;

            if (feedBackSign == "A" || feedBackSign == "ALL") {
                var A_INVESTMENT_AMOUNT = formValue.A_INVESTMENT_AMOUNT;
                var A_CONSTRUCT_PERIOD = formValue.A_CONSTRUCT_PERIOD;
                var A_CONSTRUCT_SCHEME = formValue.A_CONSTRUCT_SCHEME;
                var ResSatisfyA = $('#A_RES_SATISFY').val();//满足-不满足
                if (isValidSign && A_CONSTRUCT_SCHEME == null && ResSatisfyA == 1) {
                    fish.warn('A端接入建设方案、资源情况不能为空！');
                    return false;
                }
                if (ResSatisfyA == "") {
                    fish.warn('A端资源是否满足不能为空！');
                    return false;
                }

            }
            if (feedBackSign == "Z" || feedBackSign == "ALL") {
                var Z_INVESTMENT_AMOUNT = formValue.Z_INVESTMENT_AMOUNT;
                var Z_CONSTRUCT_PERIOD = formValue.Z_CONSTRUCT_PERIOD;
                var Z_CONSTRUCT_SCHEME = formValue.Z_CONSTRUCT_SCHEME;
                var ResSatisfyZ = $('#Z_RES_SATISFY').val();//满足-不满足
                if (isValidSign && Z_CONSTRUCT_SCHEME == null && ResSatisfyZ == 1) {
                    fish.warn('Z端接入建设方案、资源情况不能为空！');
                    return false;
                }
                if (ResSatisfyZ == "") {
                    fish.warn('Z端资源是否满足不能为空！');
                    return false;
                }

            }
            //取消校验
            $('#orderOper-form').validator('setIgnoreField','remark,transSpecial,changeSpecial,dataSpecial,otherSpecial,localScheduleSpecial',true);
            //如果资源满足则投资金额和建设工期非必填
            if($('#A_RES_SATISFY').val()==0){
                $('#orderOper-form').validator('setIgnoreField','A_INVESTMENT_AMOUNT,A_CONSTRUCT_PERIOD',true);
            }
            if($('#Z_RES_SATISFY').val()==0){
                $('#orderOper-form').validator('setIgnoreField','Z_INVESTMENT_AMOUNT,Z_CONSTRUCT_PERIOD',true);
            }
            if(!$('#orderOper-form').isValid()){
                fish.warn("请按正确格式填写");
                return false;
            }
            //添加校验
            $('#orderOper-form').validator('setIgnoreField','remark,transSpecial,changeSpecial,dataSpecial,otherSpecial,localScheduleSpecial',false);
            if($('#A_RES_SATISFY').val()==0){
                $('#orderOper-form').validator('setIgnoreField','A_INVESTMENT_AMOUNT,A_CONSTRUCT_PERIOD',false);
            }
            if($('#Z_RES_SATISFY').val()==0){
                $('#orderOper-form').validator('setIgnoreField','Z_INVESTMENT_AMOUNT,Z_CONSTRUCT_PERIOD',false);
            }
            return true;
        },
        // 查询是否出现核查反馈页面
        qryCheckFlag: function () {
            var me = this;
            var tacheId = me.options.tacheId;
            var psId = me.options.psId;
            var formValue = $('#orderOper-form').form('value');
            var checkFlag = (psId == "10101042" && tacheId != "510101052") || (tacheId == "510101052" && formValue.resRadio == 1);
            return checkFlag;
        },
        qryCircuitAreaInfo: function (param) {
            var srvOrdId = param.SRV_ORD_ID + '';
            console.log("www" + srvOrdId)
            var me = this;
            var formValue = $('#orderOper-form').form('value');
            operOrderAction.qryCircuitAreaInfo(srvOrdId, function (res) {
                var divStr = "";
                var otherStr = "";
                if (res.PORT == 'A') {
                    if (res.AREAID != res.AREGIONID) {
                        divStr = '<label class="radio-inline" id="AREGIONNAME">'
                            + '<input type="radio" name="masterSelectRadio" id="AREGIONNAME" value="' + res.AREGIONID + '">'
                            + res.AREGIONNAME + '</label>';
                    }
                    divStr += '<label class="radio-inline" id="AREANAME">'
                        + '<input type="radio" name="masterSelectRadio" id="AREANAME" checked="true" value="' + res.AREAID + '">'
                        + res.AREANAME + '</label>';
                } else if (res.PORT == 'Z') {
                    if (res.AREAID != res.ZREGIONID) {
                        divStr = '<label class="radio-inline" id="ZREGIONNAME">'
                            + '<input type="radio" name="masterSelectRadio" id="ZREGIONNAME" value="' + res.ZREGIONID + '">'
                            + res.ZREGIONNAME + '</label>';
                    }
                    divStr += '<label class="radio-inline" id="AREANAME">'
                        + '<input type="radio" name="masterSelectRadio" id="AREANAME" checked="true" value="' + res.AREAID + '">'
                        + res.AREANAME + '</label>';
                } else if (res.PORT == 'ALL') {
                    if (res.AREAID != res.AREGIONID) {
                        divStr = '<label class="radio-inline" id="AREGIONNAME">'
                            + '<input type="radio" name="masterSelectRadio" id="AREGIONNAME" value="' + res.AREGIONID + '">'
                            + res.AREGIONNAME + '</label>';
                    }
                    if (res.AREAID != res.ZREGIONID) {
                        divStr += '<label class="radio-inline" id="ZREGIONNAME">'
                            + '<input type="radio" name="masterSelectRadio" id="ZREGIONNAME" value="' + res.ZREGIONID + '">'
                            + res.ZREGIONNAME + '</label>';
                    }
                    divStr += '<label class="radio-inline" id="AREANAME">'
                        + '<input type="radio" name="masterSelectRadio" id="AREANAME" checked="true" value="' + res.AREAID + '">'
                        + res.AREANAME + '</label>';
                } else {
                    divStr = '<label class="radio-inline" id="AREANAME">'
                        + '<input type="radio" name="masterSelectRadio" id="AREANAME" checked="true" value="' + res.AREAID + '">'
                        + res.AREANAME + '</label>';
                }

                //$("#masterDiv").replaceWith(divStr);
                $("#masterSelectAZProtDiv").html("");
                $("#masterSelectAZProtDiv").append(divStr);
                // //将回显方法写入到主调局方法里，是为了避免回显主调局数据后，主调局数据会再次被初始化覆盖
                if (param.STATE == '已配置' || param.ACTIVE_TYPE == '102') {
                    me.queryPropertyConfig(param)
                } else {
                    $("#otherMaster").attr("disabled", true);
                    $("#otherMaster").popedit('setValue', {name: '请选择区域！', value: ''});
                }
            });
        },
        initFileUpdate: function (param) {
            $("#fileGrid").grid({
                colModel: [
                    {name: 'fileName', label: '文件名称', width: 160, sortable: false},
                    {name: 'fileSize', label: '大小', width: 50},
                    {name: 'fileType', label: '类型', width: 40, sortable: false},
                    {
                        name: 'action', label: '操作', width: 100, formatter: 'actions',
                        formatoptions: {
                            editbutton: false,
                            delbutton: true
                        }
                    }
                ],
                width: 516,
                create: function () {//控件初始化完成触发的事件
                   /* operOrderAction.getAnnex(param, function (data) {
                        if (data.length != 0) {
                            for (i = 0; i < data.length; i++) {
                                var fileObj = {};
                                var map = data[i];
                                for (var k in map) {  //通过定义一个局部变量k遍历获取到了map中所有的key值
                                    var docList = map[k]; //获取到了key所对应的value的值！
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
                    });*/
                }

            });
            var me = this;
            me.$('#selectFiles').fileupload({
                dataType: 'json',
                autoUpload: false,
                /*maxFileSize: 20000000,*/
                add: function (e, data) {
                    var fileObj = {};
                    var obj = data.files[0];
                    fileObj.fileName = obj.name.split(".")[0];
                    fileObj.fileSize = (obj.size / 1024.0).toFixed(2) + "KB";
                    fileObj.fileType = obj.name.split(".")[1];
                    if ((obj.size / 1024.0).toFixed(2) >= 20 * 1024) {
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
                    if (data.result.success) {
                        $("#orderOper-form").unblockUI().data('blockui-content', false);
                        fish.toast('success', data.result.message);
                        me.popup.close();
                    } else {
                        $("#orderOper-form").unblockUI().data('blockui-content', false);
                        fish.toast('warn', data.result.message);
                    }
                },
            });
        },
        fileUpdate: function (params) {
            FILES.url = URL + "/localScheduleLT/FlieUpdateController/uploadFiles.spr";
            FILES.formData = {
                params: JSON.stringify(params)
            }
            FILES.submit();
        },


        // 初始化核查反馈页面并查询数据
        initCheckInfo : function (param) {
            var me = this;
            var checkFlag = me.qryCheckFlag();
            var tacheId = me.options.tacheId;
            me.queryCheckInfo(param);
        },
        // 查询核查反馈信息 参数：WO_ID,SRV_ORD_ID
        queryCheckInfo : function (param) {
            var me = this;
            var tacheId = me.options.tacheId;
            var params = {};
            //       if(tacheId=='500001150' || tacheId=='500001151'){
            params.tacheId = tacheId;
            params.woId =param.WO_ID;
            params.srvOrdId = param.SRV_ORD_ID;
            params.orderId = param.ORDER_ID;
            operOrderAction.queryCheckInfo(params,function (res) {
                var arrayData = new Map();
                /* if (tacheId=='500001150' || tacheId=='500001151'){
                     $("#A_ACCESS_ROOM").val(res.data.A_ACCESS_ROOM);
                     $("#Z_ACCESS_ROOM").val(res.data.Z_ACCESS_ROOM)
                 }*/
                arrayData = res.data;
                for(var key in arrayData){
                    var value = arrayData[key];
                    if(key=="Z_RES_SATISFY"){
                        $("#"+key).combobox('value',value);
                    }else{
                        $("#"+key).val(value);
                    }
                }
            });
            //      }
        },
        //回显电路对应的专业配置信息
        queryPropertyConfig: function (data) {
            var param = {};
            param.srvOrdId = data.SRV_ORD_ID;
            param.orderId = data.ORDER_ID;
            param.cstOrdId = data.CST_ORD_ID;
            param.activeType = data.ACTIVE_TYPE;
            var tacheId = this.options.tacheId;
            operOrderAction.queryPropertyConfig(param, function (res) {
                if (res.success) {
                    if (res.message != '102') {
                        if (tacheId == '510101052') { //核查调度环
                            //每次回显前置灰所有核查专业
                            $("input[name$='Special']").each(function () {
                                $(this).removeAttr("checked");
                                //$(this).parent().parent().next().find("input").attr("disabled", true);
                                $(this).parent().parent().next().find("input").popedit('setValue', {
                                    name: '请选择派发区域！',
                                    value: ''
                                });
                                $("#localSchedulePopedit").popedit('disable'); //禁用控件
                            });
                            if (res.configInfo != null) {
                                if (res.configInfo.isDataCheck == 0) {
                                    $("input[name=dataSpecial]").prop("checked", true);
                                }
                                if (res.configInfo.isChangeCheck == 0) {
                                    $("input[name=changeSpecial]").prop("checked", true);
                                }
                                if (res.configInfo.isTransCheck == 0) {
                                    $("input[name=transSpecial]").prop("checked", true);
                                }
                                if (res.configInfo.isOtherCheck == 0) {
                                    $("input[name=otherSpecial]").prop("checked", true);
                                }
                                if (res.configInfo.localScheduleOrg != undefined) {
                                    $("input[name=localScheduleSpecial]").prop("checked", true);
                                    $("#localSchedulePopedit").popedit('enable');
                                    //$("input[name=localSchedulePopedit]").removeAttr("disabled");
                                    $("#localSchedulePopedit").popedit('setValue', {
                                        name: res.configInfoName.localScheduleOrg,
                                        value: res.configInfo.localScheduleOrg
                                    });
                                }
                            }
                        }
                    }
                } else {
                    fish.toast('warn', res.message);
                }
            });
        },
        //初始化已保存的岗位
        initSelectSave: function (orderId) {
            var param = {};
            param.orderId = orderId;
            operOrderAction.qryDispObjByOrderId(param, function (resData) {
                // debugger
                if (resData != null) {
                    var nodeArray = new Array;
                    var userNodeArray = new Array;
                    resData.forEach(function (val, i) {
                        var obj = '';
                        var user = '';
                        if (val.JOBOBJ != '' && val.JOBOBJ != undefined) {
                            obj = val.JOBOBJ;
                            obj.forEach(function (data, j) {
                                var nodeSin = new Object();
                                nodeSin = data;
                                nodeArray.push(nodeSin);
                            });
                        }
                        if (val.USEROBJ != '' && val.USEROBJ != undefined) {
                            user = val.USEROBJ;
                        }
                        if (val.TACHE_ID == '500001158') { //数据制作
                            $("#dataMake").popedit('setValue', {name: val.JOBNAME, value: obj});
                            $("#dataMakeUser").popedit('setValue', {name: val.USERNAME, value: user});//选择人员文本框赋值
                            userNodeArray = val.USEROBJ;
                        } else if (val.TACHE_ID == '500001159') { //资源施工
                            $("#resConstruct").popedit('setValue', {name: val.JOBNAME, value: obj});
                        } else if (val.TACHE_ID == '500001156') { //外线施工
                            $("#outside").popedit('setValue', {name: val.JOBNAME, value: obj});
                        }
                    })
                    jobData = nodeArray;
                    userData = userNodeArray;
                }
            });

        },
        // 保存核查反馈信息
        saveCheckInfo: function(){
            var me = this;
            var data = $("#circuitGrid").grid("getCheckRows");
            var formValue = $('#orderOper-form').form('value');
            if (data.length >= 1) {
                var param = {};
                param.circuitData = data;
                param.formValue = formValue;
                // 校验核查数据
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
                fish.toast('warn', "请至少勾选一条电路信息!");
            }

        },
        //核查调度环节保存核查专业
        saveSpecialConfig: function () {
            var me = this;
            var data = $("#circuitGrid").grid("getCheckRows");
            var formValue = $('#orderOper-form').form('value');
            if (data.length > 0) {
                var param = {};
                param.dataInfo = data;
                param.masterValue = '';
                var childFlowSpecialMap = me.getAreaSpecialCheck(formValue);
                if (childFlowSpecialMap != false) {
                    var specialtyConfig = {};
                    var specialtyConfigName = {};
                    specialtyConfig = childFlowSpecialMap.checkAreaSpecialArea;
                    specialtyConfigName = childFlowSpecialMap.checkAreaSpecialAreaName;
                    param.specialtyConfig = specialtyConfig;
                    param.specialtyConfigName = specialtyConfigName;
                    param.flowSpecialData = childFlowSpecialMap;
                    operOrderAction.saveSpecialtyConfigInfo(param, function (res) {
                        if (res.success) {
                            //专业配置保存完成后，重新刷新电路信息表格数据
                            me.circuitInfo();
                            fish.toast('success', res.message);
                        } else {
                            fish.toast('error', res.message);
                        }
                    });
                }
            } else {
                fish.toast('warn', "请至少勾选一条电路信息，并配置专业!");
            }
        },
        initGridInfo: function () {
            var tacheId = this.options.tacheId;
            var psId = this.options.psId;
            // 资源配置按钮是否隐藏状态
            var selectSpecialFlag = $("input[name='resRadio']:checked").val();
            if ((tacheId == '510101052' && selectSpecialFlag == '0')) { //核查调度(选专业)
                return [
                    {
                        name: 'STATE', label: '专业配置', width: 95, sortable: false,
                        formatter: function (cellval, opts, rwdat, _act) {
                            if (cellval == '已配置') {
                                return '<div class="btn-group">' +
                                    '<button type="button" class="btn btn-link js-delete" style="color: #6DCC4A">' + cellval + '</button>' +
                                    '</div>';
                            } else {
                                return '<div class="btn-group btn btn-link" style="color: #FF5858">' + cellval +
                                    '</div>';
                            }
                        }
                    },
                    {name: 'CIRCUITCODE', label: '电路编号', width: 100, sortable: false},
                    {name: 'TRADE_ID', label: '业务订单号', width: 100},
                    {name: 'ORDER_ID', label: '流程订单号', width: 100, hidden: true},
                    {name: 'SERIAL_NUMBER', label: '业务号码', width: 100, sortable: false},
                    {name: 'AREGIONNAME', label: 'A端所属区域', width: 100, sortable: false},
                    {name: 'ZREGIONNAME', label: 'Z端所属区域', width: 100, sortable: false},
                    {name: 'A_INSTALLED_ADD', label: 'A端装机地址', width: 100, sortable: false},
                    {name: 'Z_INSTALLED_ADD', label: 'Z端装机地址', width: 100, sortable: false}
                ]
            }
            else if(tacheId == '510101052'&& selectSpecialFlag=='1'){
                // 核查调度不选专业
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
                    {name: 'AREGIONNAME', label: 'A端所属区域', width: 100 , sortable: false },
                    {name: 'ZREGIONNAME', label: 'Z端所属区域', width: 100 , sortable: false },
                    {name: 'A_INSTALLED_ADD', label: 'A端装机地址', width: 100 , sortable: false },
                    {name: 'Z_INSTALLED_ADD', label: 'Z端装机地址', width: 100 , sortable: false }
                ]
            }
        },
    });
});