define([
        'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/action/localOrderSelectAction',
        'text!module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/templates/taskDetailView.html',
        'i18n!module/UnicomLocalNet/resmaster/portal/local/i18n/unicomLocalOrderView.i18n',
        'css!module/UnicomLocalNet/resmaster/portal/local/css/unicomLocalOrderView.css'],
    function(localOrderSelectAction,feedBackInfoView,i18n,css) {

        var localConfigNum = 10;
        var localConfigPage = 1;
        var productType ;

        return fish.View.extend({
            template: fish.compile(feedBackInfoView),
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
                document.getElementById("titleValue").innerText = selrow.titleValue;
                document.getElementById("TASKNAME").innerText = selrow.TASKNAME;
                document.getElementById("ORGNAME").innerText = selrow.ORGNAME;
                document.getElementById("USERJOBNAME").innerText = selrow.USERJOBNAME;
                document.getElementById("USERNAME").innerText = selrow.USERNAME;
                document.getElementById("TACHENAME").innerText = selrow.TACHENAME;
                document.getElementById("ORDERSTATE").innerText = selrow.ORDERSTATE;
                document.getElementById("TRACKCONTENT").innerText = selrow.TRACKCONTENT;
                document.getElementById("DEAL_DATE").innerText = selrow.DEAL_DATE;
                document.getElementById("CREATE_DATE").innerText = selrow.CREATE_DATE;
                document.getElementById("STATE_DATE").innerText = selrow.STATE_DATE;
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