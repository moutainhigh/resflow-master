define(['module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/operOrderAction',
    'text!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/templates/disableView.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/operOrderView.css'
], function(operOrderAction,disableView,i18n,css) {
    return fish.View.extend({
        template: fish.compile(disableView),
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
            URL=this.getRootPath();
            var me = this;
            var tacheId = me.options.tacheId;
            var orderId = me.options.orderId + '';
            var psId = me.options.psId;
            var srvOrdId = me.options.srvOrdId + '';
            var btnFlag = me.options.btnFlag;
            if(btnFlag == "disableOrder"){
                $("#remarkDiv").show();
                $("#remarkGoRoll").addClass("requireds");
            }
        },

        getRootPath:function (){
            //获取当前网址，如： http://localhost:8083/uimcardprj/share/meun.jsp
            var curWwwPath=window.document.location.href;
            //获取主机地址之后的目录，如： uimcardprj/share/meun.jsp
            var pathName=window.document.location.pathname;
            var pos=curWwwPath.indexOf(pathName);
            //获取主机地址，如： http://localhost:8083
            var localhostPaht=curWwwPath.substring(0,pos);
            //获取带"/"的项目名，如：/uimcardprj
            var projectName=pathName.substring(0,pathName.substr(1).indexOf('/')+1);
            return (localhostPaht+projectName);
        },

        submit:function () {
            var me = this;
            var psId = me.options.psId;
            var tacheId = me.options.tacheId;
            var woId = me.options.woId;
            var btnFlag = me.options.btnFlag;
            var orderId = me.options.orderId;
            var formValue = $('#orderOper-form').form('value');
            var params = new Object();
            if(btnFlag == "disableOrder"){
                params.remark = formValue.remark;
                params.tacheId = tacheId;
                params.woId = woId;
                params.orderId = orderId;
                params.action = "disableOrder";
                if(params.remark == null){
                    fish.error({title:'提示',message:'说明不能为空'});
                    return;
                }
                $("#orderOper-form").blockUI({message: '处理中...'}).data('blockui-content', true);
                operOrderAction.disableOrder(params,function (res) {
                    if (res.success){
                        fish.toast('success', res.message);
                    }else {
                        fish.toast('error', res.message);
                    }
                    $("#orderOper-form").unblockUI().data('blockui-content', false);
                    me.popup.close();
                });
            }
        },
    });
});