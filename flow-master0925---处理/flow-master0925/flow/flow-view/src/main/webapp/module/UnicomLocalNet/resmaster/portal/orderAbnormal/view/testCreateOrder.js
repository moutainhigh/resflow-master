define([
    'text!module/UnicomLocalNet/resmaster/portal/orderAbnormal/template/testCreateOrder.html',
    'module/UnicomLocalNet/resmaster/portal/orderAbnormal/action/orderAppendAction',
    'module/component/views/FormView'
], function (Template, orderAppendAction, FormView) {
    return ngc.View.extend({
        template: ngc.compile(Template),
        events: {
            'click #testCreateOrder': 'testCreateOrder'
        },
        initialize: function () {
            this.render();
        },
        render: function () {
            this.$el.html(this.template());
            // this.setView("#condition-form", new FormView({
            //     config: {
            //         elements: {},
            //         column: 0
            //     }
            // }));
            // this.getView("#condition-form").on("viewRenderAfter", function () {
            //     // that.getView("#woBaseInfo").disableForm();
            // });
        },
        afterRender: function () {
        },
        testCreateOrder: function () {
            ngc.progress('loading');
            orderAppendAction.testExceptionFlowChange({},function () {
                ngc.progress();
            });
        }
    });
});