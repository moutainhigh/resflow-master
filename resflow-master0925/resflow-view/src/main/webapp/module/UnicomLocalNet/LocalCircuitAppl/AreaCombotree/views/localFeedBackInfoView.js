define([
        'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/action/localOrderSelectAction',
        'text!module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/templates/localFeedBackInfoView.html',
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
                debugger;
                var selrow = this.options.selrow;
                document.getElementById("USER_REAL_NAME").innerText = selrow.USER_REAL_NAME;
                document.getElementById("AREA").innerText = selrow.AREAID;
                document.getElementById("CONSTRUCT_SCHEME").innerText = selrow.CONSTRUCT_SCHEME;
                document.getElementById("ACCESS_ROOM").innerText = selrow.ACCESS_ROOM;
                document.getElementById("INVESTMENT_AMOUNT").innerText = selrow.INVESTMENT_AMOUNT;
                document.getElementById("CONSTRUCT_PERIOD").innerText = selrow.CONSTRUCT_PERIOD;
                document.getElementById("RES_SATISFY").innerText = selrow.RES_SATISFY;
                document.getElementById("RES_PROVIDE_STAND_NAME").innerText = selrow.RES_PROVIDE_STAND_NAME;
                document.getElementById("BOARD_READY_NAME").innerText = selrow.BOARD_READY_NAME;
                document.getElementById("TRANS_READY_NAME").innerText = selrow.TRANS_READY_NAME;
                document.getElementById("OPTICAL_READY_NAME").innerText = selrow.OPTICAL_READY_NAME;
                document.getElementById("BOARD_PERIOD").innerText = selrow.BOARD_PERIOD;

                document.getElementById("BOARD_AMOUNT").innerText = selrow.BOARD_AMOUNT;
                document.getElementById("BOARD_TYPE").innerText = selrow.BOARD_TYPE;
                document.getElementById("BOARD_MODEL").innerText = selrow.BOARD_MODEL;
                document.getElementById("TRANS_PERIOD").innerText = selrow.TRANS_PERIOD;
                document.getElementById("TRANS_AMOUNT").innerText = selrow.TRANS_AMOUNT;

                document.getElementById("TRANS_TYPE_NAME").innerText = selrow.TRANS_TYPE_NAME;
                document.getElementById("OTHER_TYPE").innerText = selrow.OTHER_TYPE;
                document.getElementById("TRANS_MODEL").innerText = selrow.TRANS_MODEL;
                document.getElementById("OPTICAL_PERIOD").innerText = selrow.OPTICAL_PERIOD;
                document.getElementById("OPTICAL_AMOUNT").innerText = selrow.OPTICAL_AMOUNT;

                document.getElementById("CONSTRUCT_PERIOD_STAND").innerText = selrow.CONSTRUCT_PERIOD_STAND;
                document.getElementById("PROJECT_AMOUNT").innerText = selrow.PROJECT_AMOUNT;
                document.getElementById("PROJECT_OVERVIEW").innerText = selrow.PROJECT_OVERVIEW;


                document.getElementById("APPROVAL_PERIOD").innerText = selrow.APPROVAL_PERIOD;
                document.getElementById("MUNICIPAL_APPROVAL_NAME").innerText = selrow.MUNICIPAL_APPROVAL_NAME;
                document.getElementById("APPROVAL_PERIOD").innerText = selrow.APPROVAL_PERIOD;

                document.getElementById("RES_DESC").innerText = selrow.RES_DESC;
                document.getElementById("PROPERTY_REDLINE_NAME").innerText = selrow.PROPERTY_REDLINE_NAME;
                document.getElementById("PROPERTY_DESC").innerText = selrow.PROPERTY_DESC;
                document.getElementById("CUST_ROOM_NAME").innerText = selrow.CUST_ROOM_NAME;
                document.getElementById("ACCESS_PROJECT_SCHEME").innerText = selrow.ACCESS_PROJECT_SCHEME;

                document.getElementById("RES_EXPLORER").innerText = selrow.RES_EXPLORER;
                document.getElementById("RES_EXPLOR_CONTACT").innerText = selrow.RES_EXPLOR_CONTACT;
                document.getElementById("RES_HAVE_NAME").innerText = selrow.RES_HAVE_NAME;
                document.getElementById("TOTAL_AMOUNT").innerText = selrow.TOTAL_AMOUNT;
                document.getElementById("LONGEST_PERIOD").innerText = selrow.LONGEST_PERIOD;
                document.getElementById("UNABLE_RELOVE").innerText = selrow.UNABLE_RELOVE;

                document.getElementById("ACCESS_CIR_TYPE_NAME").innerText = selrow.ACCESS_CIR_TYPE_NAME;
                document.getElementById("OTHER_ACE_CIR_TYPE").innerText = selrow.OTHER_ACE_CIR_TYPE;
                document.getElementById("UPLINK_NODE_PORT").innerText = selrow.UPLINK_NODE_PORT;
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