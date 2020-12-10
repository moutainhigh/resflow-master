define([
    'text!module/UnicomLocalNet/resmaster/portal/orderAbnormal/template/orderAppendTemplate.html',
    'module/UnicomLocalNet/resmaster/portal/orderAbnormal/action/orderAppendAction',
    'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/orderDetailsAction',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/orderDetailsView.css'
], function (orderAppendTemplate, orderAppendAction, orderStandbyAction, orderDetailsAction, i18n) {
    var userInfo = orderStandbyAction.queryStaffInfo().responseJSON.data;
    var chgType = '';
    var chgVersion = '';
    var lessVersions = []; //还未确认的低版本追单
    return fish.View.extend({
        template: fish.compile(orderAppendTemplate),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #appendConfirm': 'appendConfirm',
            'click #appendReject': 'appendReject',
            'click #resModify': 'resModify',
            'click #compOrder': 'compOrder',
            'click #oldOrderInfo': 'oldOrderInfo'
        },
        initialize: function () {
            this.render();
        },
        render: function () {
            this.$el.html(this.template(this.i18nData));
        },
        afterRender: function () {

            chgType = this.options.chgType;
            chgVersion = this.options.chgVersion;
            if (chgVersion > 1) {
                var verParam = {
                    chgType: chgType,
                    srcCstOrderId: this.options.cstOrdId,
                    lessVerion: chgVersion
                };
                var ret = orderAppendAction.getPrivVersionState(verParam).responseJSON.data;
                if (ret && ret.length > 0) {
                    lessVersions = ret;
                }
            }else {
                lessVersions = [];
            }

            if ('104' === chgType) {
                this.initDocumentation();
                this.$("#title").html('追单详情');
            }
            else if ('108' === chgType) {
                this.$("#title").html('加急详情');
                this.$("#accordion").css('display', 'none');
                this.$("#timeInfoDiv").css('display', 'block');
            }
            else if ('109' === chgType) {
                this.$("#title").html('延期详情');
                this.$("#accordion").css('display', 'none');
                this.$("#timeInfoDiv").css('display', 'block');
            }
            this.queryChgLog();
            this.initBtn();
            if("onedry" == this.options.RESOURCES){
                $('#appendReject').hide()
            }
        },
        initBtn: function () {
            if ('show' === this.options.viewType) {
                this.$("#btnHDiv").css('display', 'none');
                return;
            }

            //调度环节
            var scueduleArry = ['SECONDARY_SCHEDULE', 'SECONDARY_SCHEDULE_2', 'CHECK_SCHEDULING',
                'CIRCUIT_DISPATCH', 'CHECK_DISPATCH'];

            //资源分配环节
            var srcDispArry = ['FIBER_RES_ALLOCATE', 'RES_ALLOCATE', 'SEC_SOURCE_DISPATCH_2',
                'SEC_SOURCE_DISPATCH', 'SEC_SOURCE_DISPATCH_CLD'];


            var tacheCode = this.options.tacheCode;
            var levelId = this.options.levelId;
            if ('01' === levelId && scueduleArry.indexOf(tacheCode) > -1) {  //二干调度环节
                $("#appendConfirm").show();
                $("#appendReject").show();
                $("#compOrder").hide();

            }

            else {
                $("#appendConfirm").hide();
                $("#appendReject").hide();
            }
            if (srcDispArry.indexOf(tacheCode) > -1) { //资源分配
                $("#resModify").show();
            } else {
                $("#resModify").hide();
            }
        },
        initDocumentation: function () {
            this.$("#accordion").css('display', 'block');
            this.$("#timeInfoDiv").css('display', 'none');
            this.$("#accordion").accordion({
                active: 0,
                heightStyle: "content",
                multiple: "true",
                collapsible: "false"
            });
        },
        queryChgLog: function () {
            var param = {};
            var cstOrdId = this.options.cstOrdId + '';
            var chgType = this.options.chgType;
            var version = this.options.version;
            param.cstOrdId = cstOrdId;
            param.chgType = chgType;
            param.version = version;
            var ret = orderAppendAction.qryOrdChgLogByCstOrdId(param).responseJSON.data;
            if (ret.success) {
                var customerList = ret.customerList;
                var dispatchList = ret.dispatchList;
                var prodList = ret.prodList;
                var changeTimeList = ret.changeTimeList;
                if ('104' === chgType) { //追单
                    this.getCstInfo(customerList);
                    this.getDispOrderInfo(dispatchList);
                    this.getCircuitInfo(prodList);
                } else if ('108' === chgType || '109' === chgType) { //加急
                    this.getChangegTimeInfo(changeTimeList);
                }
            } else {

            }
        },
        getChangegTimeInfo: function (data) { //变更时间信息
            if (data && data.length > 0) {
                this.$("#noTimeInfo").css('display', 'none');
                var html = "";
                var version1 = "";
                var count = 1;
                for (var i = 0; i < data.length; i++) {
                    var version2 = data[i].CHG_VERSION;
                    if (version1 !== version2) {
                        count = 1;
                    } else {
                        count++;
                    }
                    var circuitNo = (data[i].CIRCUIT_CODE == null || data[i].CIRCUIT_CODE == '') ? '' : '—' + data[i].CIRCUIT_CODE;
                    html += this.getTowColsHtml(data[i].CHANGE_MESSAGE, "电路 " + count + circuitNo + '—版本 ' + version2);
                    version1 = version2;
                }
                this.$("#timeInfo").append(html);
            }
        },
        getCstInfo: function (data) {
            if (data && data.length > 0) {
                this.$("#noCstInfo").css('display', 'none');
                var html = "";
                for (var i = 0; i < data.length; i++) {
                    html += this.getTowColsHtml(data[i].CHANGE_MESSAGE, data[i].CHG_VERSION);
                }
                this.$("#cstInfo").append(html);
            }

        },
        getDispOrderInfo: function (data) {
            if (data && data.length > 0) {
                this.$("#noDispOrderInfo").css('display', 'none');
                var html = "";
                for (var i = 0; i < data.length; i++) {
                    html += this.getOneColHtml(data[i].CHANGE_MESSAGE, data[i].CHG_VERSION);
                }
                this.$("#dispOrderInfo").append(html);
            }
        },
        getCircuitInfo: function (data) {
            if (data && data.length > 0) {
                this.$("#noCircuitInfo").css('display', 'none');
                var html = "";
                var version1 = "";
                var count = 1;
                for (var i = 0; i < data.length; i++) {
                    var version2 = data[i].CHG_VERSION;
                    if (version1 !== version2) {
                        count = 1;
                    } else {
                        count++;
                    }
                    var circuitNo = (data[i].CIRCUIT_CODE == null || data[i].CIRCUIT_CODE == '') ? '' : '—' + data[i].CIRCUIT_CODE;
                    html += this.getTowColsHtml(data[i].CHANGE_MESSAGE,   "电路 " + count + circuitNo + '-版本 ' + version2);
                    version1 = version2;
                }
                this.$("#circuitInfo").append(html);
            }
        },
        getTowColsHtml: function (data, version) { //一行显示两个字段
            var html = "";
            html += "<div>" + version + " :</div>";
            html += "<table style='width: 100%; text-align: center;' border='1'>\n" +
                "   <tr style='height:30px; background:lightgrey;'>\n" +
                "      <td class='col-md-2'>属性名</td>\n" +
                "      <td class='col-md-2'>变更前</td>\n" +
                "      <td class='col-md-2'>变更后</td>\n" +
                "      <td class='col-md-2'>属性名</td>\n" +
                "      <td class='col-md-2'>变更前</td>\n" +
                "      <td class='col-md-2'>变更后</td>\n" +
                "</tr>\n";
            for (var i = 0; i < data.length; i++) {
                var temp = data[i];
                if (i % 2 == 0) {
                    html += "<tr style='height:30px;'>\n" +
                        "      <td class='col-md-2'>" + temp.key + "</td>\n" +
                        "      <td class='col-md-2'>" + temp.oldValue + "</td>\n" +
                        "      <td class='col-md-2'>" + temp.newValue + "</td>\n";
                } else {
                    html += "<td class='col-md-2'>" + temp.key + "</td>\n" +
                        "    <td class='col-md-2'>" + temp.oldValue + "</td>\n" +
                        "    <td class='col-md-2'>" + temp.newValue + "</td>\n" +
                        "</tr>\n";
                }
            }
            if (i % 2 == 1) {
                html += "<td class='col-md-2'></td>\n" +
                    "    <td class='col-md-2'></td>\n" +
                    "    <td class='col-md-2'></td>\n" +
                    "</tr>\n";
            }
            html += "</table>";
            return html;
        },
        getOneColHtml: function (data, version) { //一行显示一个字段
            var html = "";
            html += "<div>" + version + ":</div>";
            html += "<table style='width: 100%; text-align: center;' border='1'>\n" +
                "   <tr style='height:30px; background:lightgrey;'>\n" +
                "      <td class='col-md-2'>属性名</td>\n" +
                "      <td class='col-md-5'>变更前</td>\n" +
                "      <td class='col-md-5'>变更后</td>\n" +
                "</tr>\n";
            for (var i = 0; i < data.length; i++) {
                var temp = data[i];
                html += "<tr style='height:30px;'>\n" +
                    "      <td class='col-md-2'>" + temp.key + "</td>\n" +
                    "      <td class='col-md-5'>" + temp.oldValue + "</td>\n" +
                    "      <td class='col-md-5'>" + temp.newValue + "</td>\n" +
                    "</tr>\n";
            }
            html += "</table>";
            return html;
        },
        appendConfirm: function () { //追单确认
            var that = this;
            if (lessVersions.length > 0) {
                var applyOrdId = this.options.applyOrdId;
                var message = applyOrdId + "还有版本 " + lessVersions.join(',') + " 未确认，请先处理低版本异常单";
                fish.error(message);
                return;
            }
            var popFile = fish.popupView({
                url: 'module/UnicomLocalNet/resmaster/portal/orderAbnormal/view/orderConfirmView',
                width: "55%",
                height: "56%",
                title: "异常单确认",
                viewOption: {
                    // queryObj: this.queryObj,
                    woId : this.options.woId,
                    tacheId : this.options.tacheId,
                    cstOrdId : this.options.cstOrdId,
                    orderIds : this.options.orderIds,
                    chgType : this.options.chgType,
                    userName : this.options.userInfo.userName,
                    userId : this.options.userInfo.userId,
                    orgId : this.options.userInfo.orgId,

                    // selArrrow: selarrrow,
                    // girdCollMdel: girdCollMdel,
                    URl: this.URl,
                    // staffIdColl: this.userInfo.userId

                },
                callback: function (popup, view) {
                    popup.result.then(function (e) {
                        if(e =='success'){
                            var dto = that.genCompWoParam();
                            ngc.progress("提交中...");
                            orderAppendAction.exceptionFlowSure(dto, function (ret) {
                            ngc.progress();
                            if (ret.success) {
                                fish.toast('warn', '追单确认成功');
                                that.popup.close();
                            } else {
                                fish.toast('warn', ret.message);
                            }
                        });
                        }
                    }, function (e) {
                        console.log('关闭了', e);
                    });
                }
            });

        },
        appendReject: function () { //追单驳回
            var that = this;
            if (lessVersions.length > 0) {
                var applyOrdId = this.options.applyOrdId;
                var message = applyOrdId + "还有版本 " + lessVersions.join(',') + " 未确认，请先处理低版本异常单";
                fish.error(message);
                return;
            }
            var obj = this.genCompWoParam();
            ngc.progress("追单驳回...");
            orderAppendAction.appendReject(obj, function (ret) {
                ngc.progress();
                if (ret.success) {
                    fish.toast('warn', '追单驳回成功');
                    that.popup.close();
                } else {
                    fish.toast('warn', ret.message);
                }
            });

        },

        genCompWoParam:function() {
            var dto = {};
            var woIds = [];
            var _woIds = this.options.woIds;
            if (_woIds) {
                woIds = _woIds.split(",");
            }
            var orderIds = [];
            var _orderIds = this.options.orderIds;
            if (_orderIds) {
                orderIds = _orderIds.split(',');
            }
            dto.woIds = woIds;
            dto.orderIds = orderIds;
            dto.cstOrdId = this.options.cstOrdId;
            dto.staffId = userInfo.userId;
            dto.chgType = this.options.chgType;
            dto.serviceId = this.options.serviceId;
            return dto;
        },
        resModify: function () { //资源修改。 这个按钮已经控制只有资源配置环节才可以打开
            //先查出必要的参数
            var dto = {};
            dto.level = this.options.levelId;
            //二干资源分配环节
            var srcDispArry = ['SEC_SOURCE_DISPATCH_2', 'SEC_SOURCE_DISPATCH'];
            var tacheCode = this.options.tacheCode;
            dto.tacheCode = (srcDispArry.indexOf(tacheCode) > -1) ? 'SEC_SOURCE_DISPATCH_CLD' : tacheCode;

            dto.dealUser = userInfo.userId;
            dto.orderId = this.options.orderIds;
            console.log('资源修改页面入参' + dto);
            var param = orderAppendAction.getResUrlParam(dto).responseJSON.data;



            var pop = fish.popupView({
                url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/operOrderView',
                width: 700,
                height: 350,
                position:{at: "center"},
                title: "资源修改",
                viewOption: {
                    psId : param.PS_ID,
                    tacheId : param.TACHE_ID,
                    woState : param.WO_STATE,
                    orderId : param.ORDER_ID,
                    woId : param.WO_ID,
                    srvOrdId : param.SRV_ORD_ID,
                    cstOrdId : param.CST_ORD_ID,
                    specialtyCode : param.SPECIALTY_CODE,
                    regionId : param.REGION_ID,
                    buttonState : '',
                    dispObjTyeValue: param.DISP_OBJ_ID,
                    dispObjTye: param.DISP_OBJ_TYE,
                    btnFlag : "resConfig"
                },
                callback: function (popup, view) {
                    popup.result.then(function (e) {
                        me.popup.close();
                    }, function (e) {
                        console.log('关闭了', e);
                    });
                }
            });

        },
        compOrder: function () { //提交
            var that = this;
            var param = this.genCompWoParam();
            // ngc.progress("加载中...");
            if (lessVersions.length > 0) {
                var applyOrdId = this.options.applyOrdId;
                var message = applyOrdId + "还有版本 " + lessVersions.join(',') + " 未确认，请先处理低版本异常单";
                fish.error(message);
                return;
            }
            var popFile = fish.popupView({
                url: 'module/UnicomLocalNet/resmaster/portal/orderAbnormal/view/orderConfirmView',
                width: "55%",
                height: "56%",
                title: "异常单确认",
                viewOption: {
                    // queryObj: this.queryObj,
                    woId : this.options.woId,
                    tacheId : this.options.tacheId,
                    cstOrdId : this.options.cstOrdId,
                    orderIds : this.options.orderIds,
                    chgType : this.options.chgType,
                    userName : this.options.userInfo.userName,
                    userId : this.options.userInfo.userId,
                    orgId : this.options.userInfo.orgId,
                    URl: this.URl,
                },
                callback: function (popup, view) {
                    popup.result.then(function (e) {
                        if(e =='success'){
                            var dto = that.genCompWoParam();
                            ngc.progress("提交中...");
                            orderAppendAction.compWo(param, function (ret) {
                                ngc.progress();
                                if ('SUCCESS' === ret.result) {
                                    fish.toast('info', '提交成功');
                                    that.popup.close();
                                }
                                else if ('UNCONFIRM' === ret.result) {
                                    fish.toast('info', '请先进行追单确认或者驳回改追单');
                                }
                                else {
                                    fish.toast('warn', ret.message);
                                }
                            });
                        }
                    }, function (e) {
                        console.log('关闭了', e);
                    });
                }
            });



        },
        oldOrderInfo: function () { // 原定单信息
            var cstOrdId = this.options.cstOrdId + '';
            var srvOrdIds = '';
            var ret = orderAppendAction.qrySrvOrdIds(cstOrdId).responseJSON.data;
            if (ret.success) {
                srvOrdIds = ret.srvOrdIds;
            } else {
                fish.toast('warn', res.message);
                return;
            }
            var pop = fish.popupView({
                url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/orderDetailsView',
                width: "100%",
                height: "100%",
                title: "工单详情",
                modal: false,
                viewOption: {
                    srvOrdId: srvOrdIds,
                    orderId: '',
                    userInfo:userInfo,
                    cstOrdId: this.options.cstOrdId //客户订单id
                },
                callback: function (popup, view) {
                    popup.result.then(function (e) {
                    }, function (e) {
                        console.log('关闭了', e);
                    });
                }
            })
        }

    });
});