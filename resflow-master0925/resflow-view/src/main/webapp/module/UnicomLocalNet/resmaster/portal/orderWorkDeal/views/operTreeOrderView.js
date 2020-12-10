/**
 * 多选择树JS
 */
define(['module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/operOrderAction',
    'text!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/templates/operTreeOrderView.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n'
], function(operOrderAction,operTreeOrder,i18n) {
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
                    chkStyle: "checkbox",
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
            operOrderAction.qryDepart(params,function (data) {
                options.fNodes = data;
                $("#treeDemo").tree(options);
                var nodesValues = meT.options.nodeValues;
                if(nodesValues!=undefined &&nodesValues!= ''){
                    //回显之前选择的
                    fish.each(nodesValues, function (nodeObj) {
                        var nodeTh = $("#treeDemo").tree('getNodeByParam', 'id', nodeObj);
                            $("#treeDemo").tree('checkNode',nodeTh,true);
                        }
                    );
                }
                //补单不修改之前配置
                var oldRegions = meT.options.oldRegions;
                if(oldRegions != undefined && oldRegions != '') {
                    for (var i=0; i<oldRegions.length; i++){
                        var oldRegion = oldRegions[i].ASSIST_REGION.split(',');
                        fish.each(oldRegion, function (obj) {
                            var nodeTh = $("#treeDemo").tree('getNodeByParam', 'id', obj);
                            $("#treeDemo").tree('checkNode', nodeTh, true);
                            $("#treeDemo").tree('setChkDisabled', nodeTh, true);
                        });
                    }
                    // var oldRegion = meT.options.oldRegion.split(',');

                }
            });

            //起草调单 主调选择了辅调不能再选
            $("#treeDemo").on('tree:oncheck', function(e,treeNode) {
                var masterRegion = meT.options.masterRegion;
                if(masterRegion == treeNode.id) {
                    fish.warn("主调局已选择 "+treeNode.name + "，请勿重复选择！");
                    $("#treeDemo").tree('checkNode', treeNode, false);
                    return;
                }
                var circuitInfos = meT.options.circuitInfo;
                if (circuitInfos != "" && circuitInfos != null) {
                    for (var i=0; i< circuitInfos.length; i++){
                        var circuitInfo = circuitInfos[i];
                        circuitInfo.areaId = treeNode.id;
                        var ifModifyFlag = operOrderAction.ifModifyMainArea(circuitInfo).responseJSON.data;
                        if (!ifModifyFlag) {
                            fish.warn('该区域有正在执行中的本地网单子，请勿取消！');
                            $("#treeDemo").tree('checkNode', treeNode, true);
                        }
                    }
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