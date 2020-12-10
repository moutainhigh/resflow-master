define(['text!module/UnicomLocalNet/resmaster/portal/resourceInitiate/templates/resSubmitView.html',
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        'module/UnicomLocalNet/resmaster/portal/resourceInitiate/action/resourceInitiateAction',
        "css!module/UnicomLocalNet/resmaster/portal/orderLocalStandby/styles/localStandby.css"
], function(resSubmitView,portalViewi18n,resourceInitiateAction,css) {
    return fish.View.extend({
        template: fish.compile(resSubmitView),
        i18nData: fish.extend({}, portalViewi18n),
        events: {
            'click #submitBtn': 'submit',
        },

        initialize: function() {
            this.render();
        },

        //渲染页面
        render: function() {
            this.$el.html(this.template(this.i18nData));
        },

        //初始化fish组件
        afterRender: function() {},

        //提交
        submit:function () {
            var me = this;
            var formData = $('#orderOper-form').form('value');
            var params = new Object();
            params.woId = me.options.woId;
            params.orderId = me.options.orderId;
            params.tacheId = me.options.tacheId;
            params.remark = formData.remark;
            $("#orderOper-form").blockUI({message: '派单中...'}).data('blockui-content', true);
            resourceInitiateAction.submitOrder(params,function (res) {
                if(res.success){
                    $("#orderOper-form").unblockUI().data('blockui-content', false);
                    fish.toast('success', res.message);
                    me.popup.close();
                }else {
                    $("#orderOper-form").unblockUI().data('blockui-content', false);
                    fish.toast('error', res.message);
                }
            });

        }
    });
});
