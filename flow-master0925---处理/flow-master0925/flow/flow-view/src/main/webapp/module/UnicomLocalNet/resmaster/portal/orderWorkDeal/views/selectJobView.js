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
            'click #searchBtn': 'querysearchData',
        },
        initialize: function() {
            flag = '';
            selectType = '';  //选择类型  人员 岗位
            nodesValues = {};
            this.render();

        },
        //渲染页面
        render: function() {
            this.$el.html(this.template(this.i18nData));
        },
        //初始化fish组件
        afterRender: function() {
            var me = this;
            selectType = me.options.selectType;
            me.initFishData();
            var selectTypeName = '';
            if (selectType == 'user'){
                selectTypeName = '人员';
            } if (selectType == 'job'){
                selectTypeName = '岗位';
            }
            $("#searchType").val(selectTypeName);
            me.initsearchDataGrid();//初始化搜索
            me.initSelectType();//初始化资源树
        },

        initSelectType: function(){
            var me = this;
            //组织树、搜索
            $("input[name = 'selectTypeRadio']").bind('click',function(){
                var newResourceFlag = $("input[name='selectTypeRadio']:checked").val();
                if ('tree' == newResourceFlag) { //组织树
                    $("#treeDiv").show();
                    $("#findButton").hide();
                    $("#findDiv").hide();
                }else if('find' == newResourceFlag) { //搜索框
                    $("#findButton").show();
                    $("#findDiv").show();
                    $("#treeDiv").hide();
                }
            });
        },

        initFishData : function(){
            var me = this;
            var jobDate = {};
            var type = me.options.type;
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
            params.searchFlag = false;
            operOrderAction.qryJob(params,function (data) {
                jobDate.fNodes = data;
                $("#treeDemo").tree(jobDate);
                nodesValues = me.options.nodeValues;
                if (type == 'dataMakeUser') {
                    flag = "dataMake";
                    if(nodesValues.userNodeArrayMake!=undefined &&nodesValues.userNodeArrayMake!= ''){
                        me.eachData(nodesValues.userNodeArrayMake);
                    }
                } else if (type == 'resConstructUser') {
                    flag = "resConstruct";
                    if (nodesValues.userNodeArrayCon!=undefined &&nodesValues.userNodeArrayCon!= ''){
                        me.eachData(nodesValues.userNodeArrayCon);
                    }
                }else {
                    me.eachData(nodesValues);
                }
            });
        },

        eachData : function (nodesValue) {
            fish.each(nodesValue, function (nodeObj) {
                    var parentNodeTh = $("#treeDemo").tree('getNodeByParam', 'id', nodeObj.pId);
                    var nodeTh = $("#treeDemo").tree('getNodeByParam', 'id', nodeObj.id,parentNodeTh);
                    if (nodeTh != null) {
                        $("#treeDemo").tree('checkNode',nodeTh,true);
                    }else if (parentNodeTh != null) {
                        $("#treeDemo").tree('checkNode',parentNodeTh,true);
                    }
                }
            );
        },

        initGridInfo : function () {
            if (selectType == 'user'){
                return [
                    {name: 'id', label: '人员Id', hidden: true},
                    {name: 'name', label: '人员', width: 150},
                    {name: 'jobName', label: '所属岗位', width: 295}
                ]
            } if (selectType == 'job'){
                return [
                    {name: 'id', label: '岗位Id', hidden: true},
                    {name: 'name', label: '岗位', width: 455}
                ]
            }
        },

        initsearchDataGrid: function() {
            var me = this;
            $("#searchDataGrid").grid({
                colModel: me.initGridInfo(),
                multiselect: true,
                autowidth: true,
                height:293,
                gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                shrinkToFit: false,
                autoResizable: true,
                showColumnsFeature: false, //允许用户自定义列展示设置
                cached: true, //把用户自定义的列展示设置缓存在本地
                pageData: me.querysearchData()
            });

        },
        querysearchData: function() { //岗位、人员
            var me = this;
            var paramsMap = new Object();
            var searchDataName = $("#searchData").val();//人员、岗位 搜索内容
            var selectTypeFlag = selectType;
            paramsMap.searchDataName = searchDataName; //搜索内容
            paramsMap.selectTypeFlag = selectTypeFlag; //查询类型  人员 岗位
            paramsMap.workName = me.options.type;
            paramsMap.searchFlag = true;
            //调用后台方法
            $("#searchDataGrid").blockUI({message: '加载中'}).data('blockui-content', true);
            operOrderAction.qryJob(paramsMap,function (data) {
                $("#searchDataGrid").grid("reloadData", data);
                $("#searchDataGrid").unblockUI({message: '加载中'}).data('blockui-content', false);
            });

        },

        submit:function () {
            var me =this;
            var nodes = null;
            var newResourceFlag = $("input[name='selectTypeRadio']:checked").val();
            if ('tree' == newResourceFlag) { //组织树
                //获取选中的节点
                nodes = $("#treeDemo").tree("getCheckedNodes", true);
            }else if('find' == newResourceFlag) { //搜索框
                nodes = $("#searchDataGrid").grid("getCheckRows");
            }
            if (flag == "dataMake") {
                nodesValues.userNodeArrayMake = nodes;
            }else if (flag == "resConstruct") {
                nodesValues.userNodeArrayCon = nodes;
            }else {
                nodesValues = nodes;
            }
            me.popup.close(nodesValues);
        },
    });
})