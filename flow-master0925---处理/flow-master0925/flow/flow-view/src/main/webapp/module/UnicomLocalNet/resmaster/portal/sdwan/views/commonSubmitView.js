define(['text!module/UnicomLocalNet/resmaster/portal/sdwan/templates/commonSubmitView.html',
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        'module/UnicomLocalNet/resmaster/portal/sdwan/action/sdwanAction',
        'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/orderDetailsAction',
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/operOrderAction',
        "css!module/UnicomLocalNet/resmaster/portal/orderLocalStandby/styles/localStandby.css"
], function(commonSubmitView,portalViewi18n,sdwanAction,orderDetailsAction,operOrderAction,css) {
    var userInfo;
    return fish.View.extend({
        template: fish.compile(commonSubmitView),
        i18nData: fish.extend({}, portalViewi18n),
        events: {
            'click #submitBtn': 'submit',
        },

        initialize: function () {
            this.render();
            userInfo = sdwanAction.queryStaffInfo().responseJSON.data; //用户信息
        },
        //渲染页面
        render: function () {
            this.$el.html(this.template(this.i18nData));
        },
        //初始化fish组件
        afterRender: function () {
            var me = this;
            var tacheId = me.options.tacheId;
            var btnFlag = me.options.btnFlag;
            this.circuitInfo();
            if(btnFlag == "rollBackOrder"){
                $("#commonSubmitViewTitle").text("退单处理");
            }else if(tacheId=="1551002669" && btnFlag == "submit"){//运维派单
                $("#tranStaffDiv").show();
                $("#tranStaff").addClass("requireds");
            } else{
                $("#remarkGoRoll").addClass("requireds");
            }

            this.staffInfo();
        },
        //初始化派发人员选择框
        staffInfo: function () {
            $("#tranStaffPopedit").popedit({
                open: function (e) {
                    var _this = $(this);
                    var options = {
                        url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/transferView',
                        height: 460,
                        width: 700,
                        modal: false,
                        draggable: false,
                        autoResizable: true,
                        viewOption: {
                            flag: "transferStaff",
                            currentAreaId: userInfo.areaId,
                            currentOrgId: userInfo.orgId,
                            currentUserId: userInfo.userId
                        },
                        callback: function (popup, view) {
                            popup.result.then(function (res) {
                                var orgNames = '';
                                var orgIds = '';
                                var objType = '';
                                var phone = '';
                                //    var orgIds = new Array();
                                res.forEach(function (val, i) {
                                    if (i == 0) {
                                        orgNames = val.name;
                                    }
                                    orgIds = val.id;
                                    objType = val.objType;
                                    phone = val.phone;
                                })
                                _this.popedit('setValue', {
                                    name: orgNames,
                                    value: orgIds,
                                    objType: objType,
                                    phone: phone
                                });

                            }, function (e) {
                                console.log('关闭了', e);
                            });
                        }
                    };
                    var popup = fish.popupView(options);

                }
            });

        },
        // 初始化电路信息表
        circuitInfo: function () {
            var me = this;
            $("#circuitGrid").grid({
                colModel: me.initGridInfo(),
                width: 516,
                multiselect: true,
                shrinkToFit: false,
                pageData: me.qrycircuitInfo()

            /*    gridComplete: function () {
                    if (userInfo.ifSelect == '1') {
                        if (tacheId == '500001155' || tacheId == '500001157') {
                            if (btnFlag == 'submit') {
                                $("#circuitGrid").grid("showCol", 'DF_ORDER_CONFIG');
                            }
                        }
                    }
                }*/
                // }
            });
            // 设置表格高度
            $("#circuitGrid").grid("setGridHeight", 150);
        },
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
            operOrderAction.qrySrvOrdList(param,function (res) {
                if(res.flag == 1){
                    $("#circuitGrid").grid("reloadData", res.data);
                }else {
                    fish.toast('error', res.message);
                }
            });
            $(window).trigger("resize");
        },

        initGridInfo:function(){
            // debugger
            return [
                {name: 'CIRCUITCODE', label: '电路编号', width: 95, sortable: false },
                {name: 'TRADE_ID', label: '业务订单号', width: 100 },
                {name: 'ORDER_ID', label: '流程订单号', width: 100 , hidden: true },
                {name: 'SERIAL_NUMBER', label: '业务号码', width: 100 , sortable: false },
                {name: 'AREGIONNAME', label: 'A端所属区域', width: 110, sortable: false},
                {name: 'ZREGIONNAME', label: 'Z端所属区域', width: 110, sortable: false},
                {name: 'A_INSTALLED_ADD', label: 'A端装机地址', width: 100 , sortable: false },
                {name: 'Z_INSTALLED_ADD', label: 'Z端装机地址', width: 100 , sortable: false }
            ]
        },

        //提交
        submit: function () {
            var me = this;
            var tacheId = me.options.tacheId;
            var btnFlag = me.options.btnFlag;
            var params = new Object();
            if(tacheId=="1551002669" && btnFlag == "submit"){
                // 1551002669 运维派单
                var obj = $("#tranStaffPopedit").popedit('getValue');
                if (obj == null) {
                    fish.error({title: '提示', message: '派发对象不能为空！请选择。。。'});
                    return;
                } else{
                    params.objId = obj.value;
                    params.objType = obj.objType;
                }
            }
            //
            var formData = $('#orderOper-form').form('value');
            var psId = me.options.psId;
            var buttonState = me.options.buttonState;

            var circuitData = $("#circuitGrid").grid("getCheckRows");
            if (circuitData.length >= 1) {
             //   params.woId = me.options.woId;
            //    params.orderId = me.options.orderId;
                params.tacheId = me.options.tacheId;
                params.psId = me.options.psId;
                params.remark = formData.remark;
                params.btnFlag = me.options.btnFlag;

                params.circuitData = circuitData; //订单信息

                $("#orderOper-form").blockUI({message: '派单中...'}).data('blockui-content', true);
                sdwanAction.submitOrder(params, function (res) {
                    if (res.success) {
                        $("#orderOper-form").unblockUI().data('blockui-content', false);
                        fish.toast('success', res.message);
                        me.popup.close();
                    } else {
                        $("#orderOper-form").unblockUI().data('blockui-content', false);
                        fish.toast('error', res.message);
                    }
                });
            }else{
                fish.toast('error', "请至少勾选一条电路信息!");
                return;
            }

        },

    });
})

