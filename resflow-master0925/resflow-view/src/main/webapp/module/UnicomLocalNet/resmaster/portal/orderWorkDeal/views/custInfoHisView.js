define([
    'text!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/templates/custInfoHisView.html',

    'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/orderDetailsAction',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/orderDetailsView.css'
], function (custInfoHisView, orderStandbyAction, orderDetailsAction, i18n) {
    var userInfo = orderStandbyAction.queryStaffInfo().responseJSON.data;
    var chgType = '';
    var chgVersion = '';
    var lessVersions = []; //还未确认的低版本追单
    return fish.View.extend({
        template: fish.compile(custInfoHisView),
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
            this.queryChgLog();

            if (chgVersion > 1) {
                var verParam = {
                    chgType: chgType,
                    srcCstOrderId: this.options.cstOrdId,
                    lessVerion: chgVersion
                };
                var ret = orderAppendAction.getPrivVersionState(verParam).responseJSON.data;
                if (ret && ret.length > 0) {
                    lessVersions = ret;

                } else {
                    lessVersions = [];
                }
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
            param.cstOrdId = cstOrdId;
            var ret = orderDetailsAction.queryRenameLogByCstOrdId(param).responseJSON.data;
            if (ret.success) {
                var customerList = ret.customerList;
                this.getCstInfo(customerList);
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

        getTowColsHtml: function (data, version) { //一行显示两个字段
            var html = "";
            html += "<div> 版本--" + version + " :</div>";
            html += "<table style='width: 100%; text-align: center;' border='1'>\n" +
                "   <tr style='height:30px; background:lightgrey;'>\n" +
                "      <td class='col-md-2'>属性名</td>\n" +
                "      <td class='col-md-2'>过户前</td>\n" +
                "      <td class='col-md-2'>过户后</td>\n" +
                "      <td class='col-md-2'>属性名</td>\n" +
                "      <td class='col-md-2'>过户前</td>\n" +
                "      <td class='col-md-2'>过户后</td>\n" +
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
            html += "<div> 版本--" + version + ":</div>";
            html += "<table style='width: 100%; text-align: center;' border='1'>\n" +
                "   <tr style='height:30px; background:lightgrey;'>\n" +
                "      <td class='col-md-2'>属性名</td>\n" +
                "      <td class='col-md-5'>过户前</td>\n" +
                "      <td class='col-md-5'>过户后</td>\n" +
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
                    srvordId: srvOrdIds,
                    orderId: '',
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