/**
 * 多选择树JS
 */
define(['module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/action/treeAction',
    'text!module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/templates/provienceTreeView.html',
    'i18n!module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/i18n/acceptArea.i18n'
], function(treeAction,operTreeOrder,i18n) {
    return fish.View.extend({
        template: fish.compile(operTreeOrder),
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
                    enable: true,
                    chkStyle: "radio",
                    chkboxType:  { "Y" : "ps", "N" : "ps" }
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                }
            };
            var params = new Object();
            params.flag = "";
            if(this.options){
                params.flag = this.options.flag;
                params.orderId = this.options.orderId;
                params.key = this.options.key;
            }
            var meT = this;
            treeAction.queryCityTree(params,function (data) {
                options.fNodes = data;
                // debugger;
                $("#treeDemo").tree(options);
                var nodesValues = meT.options.nodeValues;
                if(nodesValues!=undefined
                    &&nodesValues!= ''){
                    // debugger;
                    fish.each(nodesValues, function (nodeObj) {
                        // debugger;
                        var nodeTh = $("#treeDemo").tree('getNodeByParam', 'id', nodeObj.id);
                        // console.log(valueObj);
                        $("#treeDemo").tree('checkNode',nodeTh,true);

                        }

                    );
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
            if(this.options.flag=="transferStaff"){
                nodes = $("#treeDemo").tree("getSelectedNodes");
            } else {
                nodes = $("#treeDemo").tree("getCheckedNodes", true);
            }

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