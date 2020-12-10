/**
 * 多选择树JS
 */
define(['module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/operOrderAction',
    'text!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/templates/selectStaffView.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n'
], function(operOrderAction,transferView,i18n) {
    var IDMark_Switch = "_switch",
        IDMark_Icon = "_ico",
        IDMark_Span = "_span",
        IDMark_Input = "_input",
        IDMark_Check = "_check",
        IDMark_Edit = "_edit",
        IDMark_Remove = "_remove",
        IDMark_Ul = "_ul",
        IDMark_A = "_a";
    var meT;

    return fish.View.extend({
        template: fish.compile(transferView),
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
            meT = this;
        },

        initFishData : function(){
            var me = this;
            var type = me.options.type;
            var options = {
                view: {
                    txtSelectedEnable: true,
                },
                check: {
                    chkboxType:  { "Y" : "ps", "N" : "ps" }
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    onCollapse: function onCollapse(event, treeNode){
                        // console.log("[onCollapse ]:" + treeNode.name);
                    },
                    onExpand: function onExpand(event, treeNode) {
                        // console.log("[onExpand ]:" + treeNode.name);
                        if(!treeNode.isParent){
                            return;
                        }
                        var tid = treeNode.tId;
                        if(treeNode.children != undefined
                            && treeNode.children != null){
                            if(treeNode.children.length == 0) {
                                operOrderAction.getStaffInfoDeptListUnit(treeNode, function (data) {
                                    var nodes = $("#transferObjTree").tree('getNodeByTId', tid);
                                    $("#transferObjTree").tree('removeChildNodes', nodes);//先清空子节点
                                    $("#transferObjTree").tree('addNodes', nodes, data);
                                });
                            }
                        }else{
                            operOrderAction.getStaffInfoDeptListUnit(treeNode,function (data) {
                                var nodes = $("#transferObjTree").tree('getNodeByTId',tid);
                                $("#transferObjTree").tree('removeChildNodes',nodes);//先清空子节点
                                $("#transferObjTree").tree('addNodes',nodes,data);
                            });
                        }

                    },
                    /*                    onDblClick: function(e, treeNode) {
                                            var tid = treeNode.tId;
                                            //获取选中的节点
                                            var nodesS = $("#transferObjTree").tree('getNodeByTId',tid);
                                            if(nodesS == undefined
                                                && nodesS == null){
                                                fish.info("请选择一个部门或人员！");
                                                return;
                                            }
                                            var nodes = new Array;
                                            nodes[0] = nodesS;
                                            if (nodes[0].pId == '' || nodes[0].pId == null) {
                                                fish.info("不能选择省分公司，请重新选择！");
                                                return;
                                            }
                                            meT.popup.close(nodes);
                                       },*/


                }
            };
            var params = {};
            params.flag = this.options.flag;
            operOrderAction.qryDepartParent(params,function (data) {
                // debugger
                options.fNodes = data;
                $("#transferObjTree").tree(options);

            });
        },
        submit:function () {
            debugger
            var me =this;
            var type = me.options.type;
            //获取选中的节点
            var nodes = $("#transferObjTree").tree("getSelectedNodes");
            if(type == "schedule"){ //调度
                if (nodes.length == 0) {
                    fish.info("请选择一个人员！");
                    return;
                }else if(nodes[0].isParent){
                    fish.info("只能选择人员，请重新选择！");
                    return;
                }
            }else {
                if (nodes[0].pId == '' || nodes[0].pId == null) {
                    fish.info("不能选择省分公司，请重新选择！");
                    return;
                }
                if (nodes.length == 0) {
                    fish.info("请选择一个部门或人员！");
                    return;
                }
            }
            me.popup.close(nodes);
        },


    });
})