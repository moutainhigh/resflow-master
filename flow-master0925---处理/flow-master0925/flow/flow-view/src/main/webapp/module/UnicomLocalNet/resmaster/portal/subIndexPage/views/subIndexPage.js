define(["text!module/UnicomLocalNet/resmaster/portal/subIndexPage/templates/subIndexPage.html",
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        "module/component/views/TabView",
        'module/UnicomLocalNet/resmaster/portal/subIndexPage/action/subIndexPageAction',
        'module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/action/affairDispatcherAction',
        'module/UnicomLocalNet/resmaster/portal/local/action/unicomLocalOrderAction',
        'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
        "css!module/UnicomLocalNet/resmaster/portal/subIndexPage/css/subIndexPage.css"],
    function (subIndexPage, portalViewi18n, TabView, subIndexPageAction, affairDispatcherAction, unicomLocalOrderAction, orderStandbyAction, css) {

        return fish.View.extend({
            template: fish.compile(subIndexPage),
            i18nData: fish.extend({}, portalViewi18n),
            events: {
                'click #yw-link': 'ywLink',
                'click #yw-cgx-li': 'bdywLink',
                'click #yw-ztd-li': 'bdywLink',
                'click #yw-ywc-li': 'bdywLink',
                'click #yw-bmdqs-li': 'ywLink',
                'click #yw-gwdqs-li': 'ywLink',
                'click #yw-grdqs-li': 'ywLink',
                'click #yw-clz-li': 'ywLink',
                'click #yw-clywc-li': 'ywLink',
                'click #yw-ycd-li': 'ywLink',
                'click #sw-link': 'swLink',
                'click #sw-mysw-li': 'swLink',
                'click #sw-cgx-li': 'swLink',
                'click #sw-sh-li': 'swLink',
                'click #sw-cl-li': 'swLink',
                'click #sw-qr-li': 'swLink',
                'click #sw-ls-li': 'swLink',
                'click #sw-cs-li': 'swLink',
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
                this.initUserInfo();
                this.initYWOrderSum();
                this.initSWOrderSum();
                //初始化表格
                this.initorderDealGrid();
                this.initLineBar();
                this.initParBar();
                this.setSameHeight();
            },
            // 初始化用户信息
            initUserInfo: function () {
                subIndexPageAction.queryStaffInfo(null, function (data) {
                    if (data) {
                        $("#userNameSpan").html(data.userRealName);
                        $("#orgNameSpan").html(data.orgName);
                    }
                });
            },
            // 初始化业务调单数量
            initYWOrderSum: function () {
                this.queryLocalOtherCount();
                this.standbyOrderCountGroup();
            },
            standbyOrderCountGroup: function () {
                var queryObj = {};
                queryObj.pageIndex = '1';
                queryObj.pageSize = '10';
                orderStandbyAction.queryAllStandbyOrderCount(queryObj, function (datas) {
                    if (datas.messages = "success") {
                        $('#deptStandny').html(datas.dataLength);
                        $('#jobStandby').text(datas.jobStandby);
                        $('#staffStandby').text(datas.staffStandby);
                        $('#dealOrder').text(datas.dealOrder);
                        $('#dispConfirm').text(datas.dispConfirm);
                        $('#abnormalOrder').text(datas.abnormalCount);
                    } else {
                        fish.toast("error", "获取数据失败");
                    }
                });
            },
            //查询草稿单、申请单数量
            queryLocalOtherCount: function () {
                var queryObject = new Object();
                queryObject.isLocalUnicom = 'localBuild';
                queryObject.draftOrderState = '10C';
                queryObject.allOrderState = '';
                queryObject.completeOrderState = '10F';
                queryObject.submitedOrderState = '10N';
                unicomLocalOrderAction.queryLocalApplyOrderCount(queryObject, function (data) {
                    if (data.message == "success") {
                        $('#draftspan').text(data.draftOrderCount);
                        $('#completespan').text(data.completeOrderCount);
                        $('#submitspan').text(data.submitedOrderCount);
                    } else {
                        fish.toast("warn", "获取数据失败");
                    }
                });
            },
            // 初始化事务调单数量
            initSWOrderSum: function () {
                var queryObj = new Object();
                var obj = {};
                obj.isReject = "false";
                queryObj.pageIndex = '1';
                queryObj.pageSize = '10';
                queryObj.parameters = obj;
                affairDispatcherAction.countVariousAffairOrder(queryObj, function (data) {
                    if (data.code == 'SUCCESS') {
                        var obj = data.counts;
                        $("#fqAmount").html(obj.fqOrder);
                        $("#cgAmount").html(obj.cgOrder);
                        $("#shAmount").html(obj.shOrder);
                        $("#clAmount").html(obj.clOrder);
                        $("#qrAmount").html(obj.qrOrder);
                        $("#lsAmount").html(obj.lsOrder);
                        $("#tzAmount").html(obj.tzOrder);
                    }
                });
            },
            //工单待办
            ywLink: function (data) {
                var data = {
                    title: "工单待办",
                    id: "a11",
                    hash: "javascript:;",
                    "toTab": $("#"+ data.currentTarget.id).attr("value"),
                    "icon": "glyphicon glyphicon-list-alt",
                    "url": "UnicomLocalNetStandby.html?navType=no"
                };
                this.openTabView(data);
            },
            //事务调单
            swLink: function (data) {
                var data = {
                    title: "事务调单",
                    id: "a26",
                    hash: "javascript:;",
                    "toTab": $("#"+ data.currentTarget.id).attr("value"),
                    "icon": "glyphicon glyphicon-paperclip",
                    "url": "gomAffairDispatcherIndex.html?navType=no"
                };
                this.openTabView(data);
            },
            //本地业务申请
            bdywLink: function (data) {
                var data = {
                    title: "本地业务申请",
                    id: "a20",
                    hash: "javascript:;",
                    "toTab": $("#"+ data.currentTarget.id).attr("value"),
                    "icon": "glyphicon glyphicon-edit",
                    "url": "unicomLocalOrderIndex.html?navType=no"
                };
                this.openTabView(data);
            },
            openTabView: function (data) {
                var pView = parent.window.contentTabView;
                var id = data.id;
                var title = data.title;
                var url = data.url;
                var param = data.toTab;
                var context = ngc.getContext();
                var origin = window.location.origin;
                var _url = origin + context + "/" + url;
                pView.openFrameView(id, title, _url, true, true, param, false);
                /* 目前点击跳转到相应页面，对应页面如果没打开，tab可以对应固定，如果已经是打开的，就不能固定了
                先这样，后面优化
                var _rst = pView._openTab(id, title, _url, true, true);
                if (_rst){
                    $("#" + id).contents().find("#" + param).click();
                    $('#' + pView.getTabViewObj(id).options._src).click();
                    pView.getViews(pView.getTabViewObj(id)).value()[0];
                    pView.getViews(pView.getTabViewObj(id).__manager__.selector).value()[0];
                }*/
            },
            initorderDealGrid: function() {
                var me = this;
                var queryOrderList = $.proxy(this.queryOrderList,this); //函数作用域改变
                $("#need_dealt_grid").grid({
                    colModel: [
                        //默认展示字段
                        {name: 'SRV_ORD_ID', label: '序号', width: 60, align: 'center'},
                        {name: 'CST_ORD_ID', label: '调单编码', width: 200, align: 'left'},
                        {name: 'ORDER_ID', label: '调单类型', width: 120, align: 'left'},
                        {name: 'PS_ID', label: '调单标题', width: 240, align: 'left'},
                        {name: 'ORDER_IDS', label: '环节', width: 120, align: 'left'},
                        {name: 'CUST_NAME_CHINESE', label: '环节状态', width: 140, align: 'left'},
                        {name: 'SUBSCRIBE_ID', label: '处理时间', width: 300, align: 'left'}
                    ],
                    datatype: "json",
                    autowidth: true,
                    rowNum:10,
                    rowList: [10,15,20,50,100,200,500,1000],
                    pager: true,
                    curPageSort: true,
                    recordtext:"{0}-{1} 共{2}条",
                    pgtext: " 第{0}页/共{1}页",
                    rowtext: "每页{0}条",
                    gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                    multiselect: true,
                    shrinkToFit: false,
                    autoResizable: true,
                    // showColumnsFeature: true, //允许用户自定义列展示设置
                    cached: true, //把用户自定义的列展示设置缓存在本地
                    pageData: queryOrderList,
                    onDblClickRow: function (e, rowid, iRow, iCol) {//双击行事件
                        me.orderDetailView(e, rowid, iRow, iCol);
                    },
                    gridComplete: function () {
                        $('.gotext').html("<span>跳转至<input class=\"ui-pagination-input\"></span>");
                    }
                });
                this.resize();
            },
            //柱状图
            initLineBar: function () {
                subIndexPageAction.queryMonthWorkChart(null, function (data) {
                    if (data.code == 'success') {
                        var charData = data.chartData;
                        // 基于准备好的dom，初始化echarts实例
                        var myChart = echarts.init(document.getElementById('lineBar'));
                        var option = {
                            title: {
                                subtext: '工单数(个）'
                            },
                            color: ['#2DB6F4'],
                            tooltip: {
                                trigger: 'axis',
                                axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                                    type: ''        // 默认为直线，可选为：'line' | 'shadow'
                                }
                            },
                            grid: {
                                top: '40px',
                                left: '3%',
                                right: '4%',
                                bottom: '3%',
                                containLabel: true
                            },
                            xAxis: [
                                {
                                    type: 'category',
                                    data: ['一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月'],
                                    axisTick: {
                                        alignWithLabel: false
                                    }
                                }
                            ],
                            yAxis: [
                                {
                                    type: 'value'
                                }
                            ],
                            series: [
                                {
                                    name: '流程环节处理工单总数',
                                    type: 'bar',
                                    barWidth: '16px',//柱宽度
                                    data: charData,
                                    itemStyle: {//圆角
                                        emphasis: {
                                            barBorderRadius: 7
                                        },
                                        normal: {
                                            barBorderRadius: 7
                                        }
                                    }
                                }
                            ]
                        };
                        // 使用刚指定的配置项和数据显示图表。
                        myChart.setOption(option);
                    }
                });
            },
           //页面保持左右div高度一致add by cwy
            setSameHeight:function(){
                var h1 = $("#panel-body-left").height();
                var h2 = $("#panel-body-right").height();
                var mh = Math.max( h1, h2);
                $("#panel-body-left").height(mh);
                $("#panel-body-right").height(mh);
            },
            //饼图
            initParBar: function () {
                subIndexPageAction.queryWorkOrderDistributeChart(null, function (data) {
                    if (data.code == 'success') {
                        var charData = data.chartData;
                        var formateData = new Array();
                        formateData = [
                            {name: '正常', value: 0},
                            {name: '超时', value: 0},
                            {name: '预警', value: 0}
                        ];
                        for (var i = 0; i < charData.length; i++) {
                            var obj = {};
                            formateData[0] = obj;
                            if (charData[i].LIMIT_STATE == '正常单') {
                                obj.name = '正常';
                                obj.value = charData[i].COUNTS;
                                formateData[0] = obj;
                            }
                            if (charData[i].LIMIT_STATE == '超时单') {
                                obj.name = '超时';
                                obj.value = charData[i].COUNTS;
                                formateData[1] = obj;
                            }
                            if (charData[i].LIMIT_STATE == '预警单') {
                                obj.name = '预警';
                                obj.value = charData[i].COUNTS;
                                formateData[2] = obj;
                            }
                        }
                        // 基于准备好的dom，初始化echarts实例
                        var myChart = echarts.init(document.getElementById('myParBar'));
                        var option = {
                            title: {
                                subtext: '单位(%）'
                            },
                            color: ['#56CE99', '#2DB6F4', '#FB8C5C'],
                            tooltip: {
                                trigger: 'item',
                                formatter: "{a} <br/>{b}: {c} ({d}%)"
                            },
                            legend: {
                                orient: 'vertical',
                                y: 'bottom',
                                x: 'right',
                                icon: "circle",
                                data: ['正常', '超时', '预警']
                            },
                            series: [
                                {
                                    name: '单据状态',
                                    type: 'pie',
                                    radius: ['50%', '70%'],
                                    avoidLabelOverlap: false,
                                    label: {
                                        normal: {
                                            show: false,
                                            position: 'center'
                                        },
                                        emphasis: {
                                            show: true,
                                            textStyle: {
                                                fontSize: '24',
                                                fontWeight: ''
                                            }
                                        }
                                    },
                                    labelLine: {
                                        normal: {
                                            show: false
                                        }
                                    },
                                    data: formateData
                                }
                            ]
                        };
                        // 使用刚指定的配置项和数据显示图表。
                        myChart.setOption(option);
                    }
                });
            },
            resize: function() {
                $("#need_dealt_grid").grid("setGridHeight", 327);
            }
        }); //fish.View.extend END
    }); //ALL END