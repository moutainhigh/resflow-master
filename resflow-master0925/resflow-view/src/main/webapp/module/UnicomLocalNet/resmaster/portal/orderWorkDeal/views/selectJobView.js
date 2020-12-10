/**
 * 多选择树JS
 */
define(['module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/operOrderAction',
    'text!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/templates/selectJobView.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n'
], function(operOrderAction,selectJobView,i18n) {
    return fish.View.extend({
        template: fish.compile(selectJobView),
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
            this.initFishData();
        },

        initFishData : function(){
            var me = this;
            var type = me.options.type;
            var jobDate = {};
            jobDate =    {
                check: {
                    enable: true,
                    chkStyle: "checkbox",
                    chkboxType:  { "Y" : "ps", "N" : "ps" }
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                }
            };
            var params = {};
            params.workName = type;
            operOrderAction.qryJob(params,function (data) {
                jobDate.fNodes = data;
                $("#treeDemo").tree(jobDate);
                var nodesValues = me.options.nodeValues;
                if(nodesValues!=undefined &&nodesValues!= ''){
                    fish.each(nodesValues, function (nodeObj) {
                            var parentNodeTh = $("#treeDemo").tree('getNodeByParam', 'id', nodeObj.pId);
                            var nodeTh = $("#treeDemo").tree('getNodeByParam', 'id', nodeObj.id,parentNodeTh);
                            if (nodeTh != null) {
                                $("#treeDemo").tree('checkNode',nodeTh,true);
                            }else if (parentNodeTh != null) {
                                $("#treeDemo").tree('checkNode',parentNodeTh,true);
                            }
                        }
                    );
                }
            });
        },

        submit:function () {
            var me =this;
            //获取选中的节点
            var nodes = $("#treeDemo").tree("getCheckedNodes", true);
            me.popup.close(nodes);
        },
    });
})