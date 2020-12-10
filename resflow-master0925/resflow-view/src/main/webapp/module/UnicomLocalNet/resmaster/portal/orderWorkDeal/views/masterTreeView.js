/**
 * 多选择树JS
 */
define(['module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/operOrderAction',
    'text!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/templates/masterTreeView.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n'
], function(operOrderAction,masterTree,i18n) {
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
            var params = new Object();
            params.flag = "";
            if(this.options){
                params.flag = this.options.flag;
                params.orderId = this.options.orderId;
                params.key = this.options.key;
            }
            var meT = this;
            // $("#treeDemo").blockUI({message: "加载中"}).data('blockui-content', true);
            $('#treeDemo').blockUI();
            operOrderAction.qryDepart(params,function (data) {
                $('#treeDemo').unblockUI();
                options.fNodes = data;
                $("#treeDemo").tree(options);
                var nodesValues = meT.options.nodeValues;
                if(nodesValues!=undefined
                    &&nodesValues!= ''){
                    //
                    fish.each(nodesValues, function (nodeObj) {
                            //
                            var nodeTh = $("#treeDemo").tree('getNodeByParam', 'id', nodeObj);
                            // console.log(valueObj);
                            $("#treeDemo").tree('checkNode',nodeTh,true);

                        }

                    );
                }
            });

            $("#treeDemo").on("tree:onclick",function(e, treeNode){
                console.log("tree:onclick defined on event " + treeNode.name);
                var circuitInfos = meT.options.circuitInfo;
                if (circuitInfos != "" && circuitInfos != null) {
                    for (var i=0; i < circuitInfos.length; i++){
                        var circuitInfo = circuitInfos[i];
                        circuitInfo.areaId = treeNode.id;
                        var ifModifyFlag = operOrderAction.ifModifyMainArea(circuitInfo).responseJSON.data;
                        if (!ifModifyFlag) {
                            fish.warn('该区域作为辅调有本地网执行中的单子，请重新选择！');
                        }
                    }
                }
            })

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
            if(this.options.type == "schedule"){ //调单
                if (nodes[0].pId == '' || nodes[0].pId == null) {
                    fish.info("不能选择省分公司，请重新选择！");
                    return;
                }
            }
            var circuitInfos = me.options.circuitInfo;
            if (circuitInfos != "" && circuitInfos != null) {
                for(var i=0; i < circuitInfos.length; i++){
                    var circuitInfo = circuitInfos[i];
                    circuitInfo.areaId = nodes[0].id;
                    var ifModifyFlag = operOrderAction.ifModifyMainArea(circuitInfo).responseJSON.data;
                    if (!ifModifyFlag) {
                        fish.warn('该区域作为辅调有本地网执行中的单子，请重新选择！');
                        return;
                    }
                }
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