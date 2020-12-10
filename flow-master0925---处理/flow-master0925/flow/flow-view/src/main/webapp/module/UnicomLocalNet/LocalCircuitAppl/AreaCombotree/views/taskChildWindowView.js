/**
 * 任务表格行双击弹窗事件create by caomm at 16/05/2019
 */
define([
        'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/action/localOrderSelectAction',
        'text!module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/templates/taskChildWindowView.html',
        'i18n!module/UnicomLocalNet/resmaster/portal/local/i18n/unicomLocalOrderView.i18n',
        'css!module/UnicomLocalNet/resmaster/portal/local/css/unicomLocalOrderView.css'],
    function(localOrderSelectAction,taskChildWindowView,i18n,css) {

        return fish.View.extend({
            template: fish.compile(taskChildWindowView),
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

                var gridData = this.options.gridData;
                document.getElementById("TASKNAME").innerText = gridData.TACHENAME;
                document.getElementById("ORGNAME").innerText = gridData.ORGNAME;
                document.getElementById("USERJOBNAME").innerText = gridData.USERJOBNAME;
                document.getElementById("USERNAME").innerText = gridData.USERNAME;
                document.getElementById("TACHENAME").innerText = gridData.TACHENAME;
                document.getElementById("ORDERSTATE").innerText = gridData.ORDERSTATE;
                document.getElementById("DEAL_DATE").innerText = gridData.DEAL_DATE;
                document.getElementById("CREATE_DATE").innerText = gridData.CREATE_DATE;
                document.getElementById("STATE_DATE").innerText = gridData.STATE_DATE;
                document.getElementById("MINUTE").innerText = gridData.MINUTE;
                document.getElementById("EXCEEDTIME").innerText = gridData.EXCEEDTIME;
                document.getElementById("ALARM_DATE").innerText = gridData.ALARM_DATE;
                document.getElementById("EXCEEDTYPE").innerText = gridData.EXCEEDTYPE;
                document.getElementById("TRACKCONTENT").innerText = gridData.TRACKCONTENT;
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