/**
 * 多选择树JS
 */
define(['module/UnicomLocalNet/resmaster/portal/cloudNetworkFlow/action/woOrderCloudAction',
    'text!module/UnicomLocalNet/resmaster/portal/cloudNetworkFlow/templates/cloudAreaTreeView.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n'
], function(woOrderCloudAction,masterTree,i18n) {
    return fish.View.extend({
        template: fish.compile(masterTree),
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
            this.qryData();
        },

        qryData : function(){
            var options = new Object();
            options =    {
                check: {
                    /*enable: true,
                    chkStyle: "radio",*/
                    chkboxType:  { "Y" : "ps", "N" : "ps" }
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                }
            };
            var meT = this;
            var params = new Object();
            params.deptId = meT.options.deptId;
            $("#treeDemo").blockUI({message: "加载中"}).data('blockui-content', true);
            woOrderCloudAction.qryCompanyAreaId(params,function (res) {
                if (res.success) {
                    options.fNodes = res.areaIdList;
                    $("#treeDemo").tree(options);
                    var nodesValues = meT.options.nodeValues;
                    if(nodesValues != undefined && nodesValues != ''){
                        fish.each(nodesValues, function (nodeObj) {
                                var nodeTh = $("#treeDemo").tree('getNodeByParam', 'id', nodeObj);
                                $("#treeDemo").tree('checkNode',nodeTh,true);
                            }
                        );
                    }
                }else {
                    fish.toast('error', res.message);
                }
            });

            function onCheck(e, treeNode) {
                console.log("onClick defined in options. " + treeNode.name);
            }
        },
        submit:function () {
            var me =this;
            //获取选中的节点
            var nodes;
            /*if(this.options.flag=="otherMaster"){
                nodes = $("#treeDemo").tree("getSelectedNodes");
            } else {
                nodes = $("#treeDemo").tree("getCheckedNodes", true);
            }*/
            nodes = $("#treeDemo").tree("getSelectedNodes", true);
            me.popup.close(nodes);
        },
        clearCheckedOldNodes:function () {
            var nodes = $("#treeDemo").tree("getChangeCheckedNodes");
            for (var i = 0, l = nodes.length; i < l; i++) {
                nodes[i].checkedOld = nodes[i].checked;
            }
        }


    });
})