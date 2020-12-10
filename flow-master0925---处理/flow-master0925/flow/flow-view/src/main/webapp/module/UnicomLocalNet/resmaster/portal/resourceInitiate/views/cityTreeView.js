/**
 * 多选择树JS
 */
define(['module/UnicomLocalNet/resmaster/portal/resourceInitiate/action/resourceInitiateAction',
    'text!module/UnicomLocalNet/resmaster/portal/resourceInitiate/templates/cityTreeView.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n'
], function(resourceInitiateAction,cityTreeView,i18n) {
    return fish.View.extend({
        template: fish.compile(cityTreeView),
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
            this.initCheckTree();
        },
        //初始化城市选择树
        initCheckTree : function(){
            var me = this;
            $("#cityTreeView").blockUI({
                message: '加载中'
            }).data('blockui-content', true);
            resourceInitiateAction.queryDeptInfo(function(res){
                if (res.success){
                    var options =    {
                        check: {
                            enable: true,
                            chkStyle: "checkbox",
                            chkboxType:  { "Y" : "ps", "N" : "ps" }
                        },
                        data: {
                            simpleData: {
                                enable: true
                            }
                        },
                        fNodes:res.data,
                    };
                    $('#cityTree').tree(options);
                    //回显数据
                    if (me.options.cityTreeData){
                        var cityDataArr = (me.options.cityTreeData +'').split(",");
                        fish.each(cityDataArr, function (nodeObj) {
                                var nodeTh = $("#cityTree").tree('getNodeByParam', 'id', nodeObj);
                                $("#cityTree").tree('checkNode',nodeTh,true);
                            }
                        );
                    }
                }else{
                    fish.info(res.msg);
                }
                $("#cityTreeView").unblockUI().data('blockui-content', false);
            });
        },
        submit:function () {
            //获取选中的节点
            var nodes = $("#cityTree").tree("getCheckedNodes", true);
            this.popup.close(nodes);
        }
    });
})