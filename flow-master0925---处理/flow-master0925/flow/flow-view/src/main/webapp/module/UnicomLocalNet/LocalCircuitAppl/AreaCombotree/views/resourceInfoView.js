define([
        'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/action/localOrderSelectAction',
        'text!module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/templates/resourceInfoView.html',
        'i18n!module/UnicomLocalNet/resmaster/portal/local/i18n/unicomLocalOrderView.i18n',
        'css!module/UnicomLocalNet/resmaster/portal/local/css/unicomLocalOrderView.css'],
    function(localOrderSelectAction,resourceInfoView,i18n,css) {

        var localConfigNum = 10;
        var localConfigPage = 1;
        var productType ;

        return fish.View.extend({
            template: fish.compile(resourceInfoView),
            i18nData: fish.extend({}, i18n),
            events: {
                "click #close" : 'close',

            },
            initialize: function() {
                this.render();
            },
            //渲染页面
            render: function() {
                this.$el.html(this.template(this.i18nData));
                return this;
            },

            //初始化fish组件
            afterRender: function() {

                var selrow = this.options.selrow;
                document.getElementById("RESTYPE").innerText = selrow.RESTYPE;
                document.getElementById("RESNAME").innerText = selrow.RESNAME;
                document.getElementById("BEFORE_ROUTE").innerText = selrow.BEFORE_ROUTE;
                document.getElementById("AFTER_ROUTE").innerText = selrow.AFTER_ROUTE;
                //document.getElementById("ALL_ROUTE").innerText = selrow.ALL_ROUTE;
            },

            close: function() {
                this.popup.close();
            },









            //浏览器窗口大小改变事件
            resize: function() {
                // $("#Config-draftGri").grid("resize",true);
               // $("#Config-draftGri").grid("setGridHeight", 327);
            }

        }); //fish.View.extend END
    }); //ALL END