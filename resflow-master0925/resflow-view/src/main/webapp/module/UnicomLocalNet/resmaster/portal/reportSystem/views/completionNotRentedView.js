define(["text!module/UnicomLocalNet/resmaster/portal/reportSystem/templates/completionNotRentedView.html",
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        'module/UnicomLocalNet/resmaster/portal/reportSystem/action/reportSystemAction.js',
        "css!module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/styles/fqAffair.css"],
    function (template, portalViewi18n, reportAction, css) {
        var localStandyNum = 10;                  //页面展示条数
        var localStandyPage = 1;                  //页数
        return ngc.View.extend({
            template: ngc.compile(template),
            i18nData: ngc.extend({}, portalViewi18n),
            events: {
                'click #completionNotRented-queryBtn': 'query',//查询
                'click #completionNotRented-exportBtn': 'exportExcel',//导出
            },
            initialize: function () {
                this.render();

            },
            render: function () {           //渲染页面
                this.$el.html(this.template(this.i18nData));
            },
            //初始化fish组件
            afterRender: function () {
                //初始化时间插件
                $('#completionNotRented-beginDate,#completionNotRented-endDate').datetimepicker({
                    orientation:{y:'bottom'},
                    buttonIcon: '',
                    viewType: "date",
                    todayBtn: true
                });

                //初始化下拉框
                this.initCombobox();
                //初始化表格
                this.initGrid();
                //查询数据
               this.queryCompletionNotRentedList(1);
                // 增加input清除功能
                $("#completionNotRented-form input").clearinput();
                // 调整窗口大小
                $(window).trigger("resize");
            },
            query:function(){
                this.queryCompletionNotRentedList(1,undefined,undefined);
            },
            initCombobox:function(){

            },
            //初始化表格
            initGrid: function () {
                var queryCompletionNotRentedList = $.proxy(this.queryCompletionNotRentedList, this);
                $("#completionNotRented-grid").grid({
                    datatype: "json",
                    colModel: [
                        //默认展示字段
                        {name: 'applyOrdId', label: '业务定单信息ID', width: 120, align: 'center',hidden:true , sortable: false},
                        {name: 'custName', label: '客户名称', width: 120, align: 'center',  sortable: false},
                        {name: 'subscribeId', label: '客户订单号', width: 150, align: 'left', sortable: false},
                        {name: 'tradeId', label: '业务订单号', width: 200, align: 'left', sortable: false },
                        {name: 'serialNumber', label: '业务号码(计费ID)', width: 200, align: 'left', sortable: false },
                        {name: 'applyOrdName', label: '申请单标题', width: 200, align: 'left', sortable: false},
                        {name: 'dispatchOrderNo', label: '调单号', width: 120, align: 'left', sortable: false},
                        {name: 'circuitCode', label: '电路编号', width: 150, align:'left', sortable: false},
                        {name: 'handelDEep', label: '受理部门', width: 100, align:'left', sortable: false},
                        {name: 'reportTime', label: '报竣时间', width: 160, align:'left', sortable: false}
                    ],
                    autowidth: true,
                    rowNum: 10,
                    rowList: [10, 15, 20, 50, 100, 200, 500],
                    pager: true,
                    recordtext: "{0}-{1} 共{2}条",
                    pgtext: " 第{0}页/共{1}页",
                    rowtext: "每页{0}条",
                    gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                    autoResizable: true,
                    //showColumnsFeature: true, //允许用户自定义列展示设置
                    //cached: true, //把用户自定义的列展示设置缓存在本地
                    pageData: queryCompletionNotRentedList,
                    height: 380
                });
                this.resize();
            },
            resize: function () {
                $("#completionNotRented-grid").grid("resize", true);
                var frameHeight = document.documentElement.scrollHeight;
                $("#completionNotRented-grid").grid("setGridHeight", frameHeight - 135);
            },
            //查询
            queryCompletionNotRentedList: function (page, rowNum, isReject) {
                debugger;
                var me = this;
                var queryObj={};
                page = (page != '' && page != undefined) ? page : localStandyPage;
                debugger;
                rowNum = (rowNum != '' && rowNum != undefined) ? rowNum : localStandyNum;
                fish.store.set('completionNotRented-grid-rowNum', rowNum); //记录用户选择的每页记录数
                queryObj.parameters ={
                    custName:$("#completionNotRented-custName").val()
                    ,circuitCode:$("#completionNotRented-circuitCode").val()
                    ,endDate:$("#completionNotRented-endDate").val()
                    ,beginDate:$("#completionNotRented-beginDate").val()
                };
                queryObj.pageIndex = page + '';
                queryObj.pageSize = rowNum + '';
                if (queryObj.parameters.beginDate && queryObj.parameters.endDate && queryObj.parameters.beginDate > queryObj.parameters.endDate) {
                    fish.warn("开始时间必须小于结束时间！");
                    return;
                }
                console.log("queryData",queryObj);
                $("#completionNotRented-grid").blockUI({message: '加载中'}).data('blockui-content', true);
                reportAction.queryCompletionNotRented(queryObj, function (data) {
                    console.log("data",data);
                    $("#completionNotRented-grid").unblockUI().data('blockui-content', false);
                    var gridData = {
                        "rows": data.data,
                        "page": data.pageIndex,
                        "records": data.dataLength,
                        "rowNum": data.rowNum,
                        "total": data.total
                    };
                    $("#completionNotRented-grid").grid("reloadData", gridData);
                });
            },

            //导出
            exportExcel: function () {
                var me = this;
                var data ={
                    custName:$("#completionNotRented-custName").val()
                    ,circuitCode:$("#completionNotRented-circuitCode").val()
                    ,endDate:$("#completionNotRented-endDate").val()
                    ,beginDate:$("#completionNotRented-beginDate").val()
                };
                if (data.beginDate && data.endDate && data.beginDate >data.endDate) {
                    fish.warn("开始时间必须小于结束时间！");
                    return;
                }
                reportAction.export(me.getRootPath() + '/localScheduleLT/ReportController/exportCompletionNotRentedData.spr', data);
            },
            // 获取根目录
            getRootPath: function () {
                // 获取当前网址，如： http://localhost:8083/uimcardprj/share/meun.jsp
                var curWwwPath = window.document.location.href;
                // 获取主机地址之后的目录，如： uimcardprj/share/meun.jsp
                var pathName = window.document.location.pathname;
                var pos = curWwwPath.indexOf(pathName);
                // 获取主机地址，如： http://localhost:8083
                var localhostPaht = curWwwPath.substring(0, pos);
                // 获取带"/"的项目名，如：/uimcardprj
                var projectName = pathName.substring(0, pathName.substr(1).indexOf('/') + 1);
                return (localhostPaht + projectName);
            },

        })
    });