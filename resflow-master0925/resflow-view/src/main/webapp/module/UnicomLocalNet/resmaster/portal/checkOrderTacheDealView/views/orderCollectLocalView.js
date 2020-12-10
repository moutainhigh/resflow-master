define(['module/UnicomLocalNet/resmaster/portal/orderTacheDealView/action/operOrderAction',
    'text!module/UnicomLocalNet/resmaster/portal/checkOrderTacheDealView/templates/orderCollectLocalView.html',
    'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderTacheDealView/styles/operOrderView.css'
], function(operOrderAction,operOrderView,orderStandbyAction,i18n,css) {
    var srvOrdIds,tacheId;
    return fish.View.extend({
        template: fish.compile(operOrderView),
        i18nData: fish.extend({}, i18n),
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
        afterRender: function() {
            this.initCombobox();
            this.initPateData();
        },
        submit:function () {
            var me = this;
            var formValue = $('#localOrderOper-form').form('value');
            var checkNull = $('#localOrderOper-form').isValid();
            if(!checkNull){
                fish.warn({title:'提示',message:'不能为空或格式不正确！请修改!'})
                return;
            }
            me.popup.close(formValue);
        },
        initCombobox:function () {
            $("#SUB_A_RES_SATISFY").combobox({
                placeholder: '--请选择资源是否满足--',
                dataTextField: 'name',
                dataValueField: 'value',
                dataSource: [
                    {name: '满足', value: '0'},
                    {name: '不满足', value: '1'}
                ]
            });
            $("#SUB_Z_RES_SATISFY").combobox({
                placeholder: '--请选择资源是否满足--',
                dataTextField: 'name',
                dataValueField: 'value',
                dataSource: [
                    {name: '满足', value: '0'},
                    {name: '不满足', value: '1'}
                ]
            });
        },
        initSpinner:function () {
            $("#L_INVESTMENT_AMOUNT").spinner({
                max: 100,
                min: 0,
            });
            $("#L_CONSTRUCT_PERIOD").spinner({
                max: 100,
                min: 0,
            });
        },
        initPateData:function () {
            var me = this;
            var CONSTRUCT_SCHEME = me.options.CONSTRUCT_SCHEME;
            var arr=new Array();
            arr=CONSTRUCT_SCHEME.split(":");
            var A_CONSTRUCT_SCHEME = arr[1];
            var Z_CONSTRUCT_SCHEME = arr[3];
            $('#SUB_A_RES_SATISFY').combobox('value',me.options.A_RES_SATISFY);
            $('#SUB_A_CONSTRUCT_SCHEME').val(A_CONSTRUCT_SCHEME);
            $('#SUB_A_INVESTMENT_AMOUNT').val(me.options.A_INVESTMENT_AMOUNT);
            $('#SUB_A_CONSTRUCT_PERIOD').val(me.options.A_CONSTRUCT_PERIOD);

            $('#SUB_Z_RES_SATISFY').combobox('value',me.options.Z_RES_SATISFY);
            $('#SUB_Z_CONSTRUCT_SCHEME').val(Z_CONSTRUCT_SCHEME);
            $('#SUB_Z_INVESTMENT_AMOUNT').val(me.options.Z_INVESTMENT_AMOUNT);
            $('#SUB_Z_CONSTRUCT_PERIOD').val(me.options.Z_CONSTRUCT_PERIOD);
        }
    });
});