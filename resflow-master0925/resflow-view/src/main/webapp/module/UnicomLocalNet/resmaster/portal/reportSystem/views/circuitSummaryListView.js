define(["text!module/UnicomLocalNet/resmaster/portal/reportSystem/templates/circuitSummaryListView.html",
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        'module/UnicomLocalNet/resmaster/portal/reportSystem/action/reportSystemAction.js',
        "css!module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/styles/fqAffair.css"],
    function (template, portalViewi18n, reportAction, cQreportAction,css) {
        var localStandyNum = 10;                  //页面展示条数
        var localStandyPage = 1;                  //页数
        return ngc.View.extend({
            template: ngc.compile(template),
            i18nData: ngc.extend({}, portalViewi18n),
            events: {
                'click #circuitSummaryList-queryBtn': 'query',//查询
                'click #circuitSummaryList-exportBtn': 'exportExcel',//导出
            },
            initialize: function () {
                this.render();

            },
            render: function () {           //渲染页面
                this.$el.html(this.template(this.i18nData));
            },
            //初始化fish组件
            afterRender: function () {

                //初始化下拉框
                this.initMultiselect();
                //初始化表格
                this.initGrid();
                $('#circuitSummaryList-startDate,#circuitSummaryList-endDate').datetimepicker({
                    orientation: {y: 'bottom'}
                });

                //查询数据
                // this.queryCircuitSummaryList(1,undefined,undefined);
                // 增加input清除功能
                $("#circuitSummaryList-form input").clearinput();
                // 调整窗口大小
                $(window).trigger("resize");

            },
            query:function(){
                this.queryCircuitSummaryList(1,undefined,undefined);
            },
            initMultiselect:function(){

                $('#circuitSummaryList-productType').multiselect({
                    dataTextField: 'text',
                    dataValueField: 'value',
                    dataSource:  [
                        { text: "数字电路", value: '10000001'},
                        { text: "以太网专线", value: '10000002'},
                        { text: "MPLS-VPN", value: '10000008'},
                        { text: "互联网专线(DIA)", value: '10000011'},
                        { text: "语音中继电路", value: '20181211001'},
                        { text: "裸光纤", value: '20181221002'},
                        { text: "基础数据(ATM)", value: '20181221003'},
                        { text: "基础数据(FR)", value: '20181221004'},
                        { text: "基础数据(DDN)", value: '20181221005'}
                    ]
                });

                $('#circuitSummaryList-actType').combobox({
                    placeholder: '--请选择动作类型--',
                    dataTextField: 'text',
                    dataValueField: 'value',
                    dataSource:  [
                        { text: "新开", value: '101'},
                        { text: "拆机", value: '102'},
                        { text: "变更", value: '103'},
                        { text: "停机", value: '104'},
                        { text: "复机", value: '105'},
                        { text: "移机", value: '106'},
                    ]
                });

                //电路状态
                $('#circuitSummaryList-oprState').multiselect({
                    dataTextField: 'text',
                    dataValueField: 'value',
                    dataSource:  [
                        { text: "占用", value: '占用'},
                        { text: "释放", value: '释放'}
                    ]
                });
                //租用范围
                $('#circuitSummaryList-cirLeaseRange').multiselect({
                    dataTextField:'value',
                    dataValueField:'id',
                    dataSource:  [
                        { id: "本地区内", value: '本地区内'},
                        { id: "本地区间", value: '本地区间'},
                        { id: "国内长途", value: '国内长途'},
                        { id: "国际电路", value: '国际电路'},
                        { id: "省内长途", value: '省内长途'},
                        { id: "省际长途", value: '省际长途'}
                    ]
                });

            },
            //初始化表格
            initGrid: function () {
                var queryCircuitSummaryList = $.proxy(this.queryCircuitSummaryList, this);
                $("#circuitSummaryList-grid").grid({
                    datatype: "json",
                    colModel: [
                        //默认展示字段
                        {name: 'CIRCUIT_NO', label: '电路编号',width: 150, align: 'center', sortable: false},
                        {name: 'PRODUCT_NAME', label: '产品类型',width: 150, align: 'center', sortable: false},
                        {name: 'OPERATE_NAME', label: '业务动作',width: 150, align: 'center', sortable: false},
                        {name: 'ROUTE_INFO', label: '详细路由',width: 150, align: 'center', sortable: false},
                        {name: 'CUST_NAME_CHINESE', label: '客户名称',width: 150, align: 'center', sortable: false},
                        {name: 'SERIAL_NUMBER', label: '业务号码',width: 150, align: 'center', sortable: false},
                        {name: 'KT_DIS_NO', label: '开通调度单号',width: 150, align: 'center', sortable: false},
                        {name: 'KT_TIME', label: '开通时间',width: 150, align: 'center', sortable: false},
                        {name: 'OPRSTATE', label: '电路状态',width: 150, align: 'center', sortable: false},
                        {name: 'speedName', label: '带宽速率',width: 150, align: 'center', sortable: false},
                        {name: 'slaFlagName', label: 'SLA标识',width: 150, align: 'center', sortable: false},
                        {name: 'slaServOpenName', label: 'SLA业务开通',width: 150, align: 'center', sortable: false},
                        {name: 'slaNetQuAssName', label: 'SLA网络质量保证',width: 150, align: 'center', sortable: false},
                        /*{name: 'slaNetQuAssName', label: 'SLA网络质量保证',width: 150, align: 'center', sortable: false},*/
                        {name: 'slaSaleServName', label: 'SLA售后服务',width: 150, align: 'center', sortable: false},
                        {name: 'CUST_INDUSTRY', label: '客户行业',width: 150, align: 'center', sortable: false},
                        {name: 'BG_DIS_NO', label: '调整调单编号',width: 150, align: 'center', sortable: false},
                        {name: 'BG_TIME', label: '调整时间',width: 150, align: 'center', sortable: false},
                        {name: 'STOP_DIS_NO', label: '关闭调单编号',width: 150, align: 'center', sortable: false},
                        {name: 'STOP_TIME', label: '关闭时间',width: 150, align: 'center', sortable: false},
                        {name: 'TRADE_ID', label: '业务订单号',width: 150, align: 'center', sortable: false},
                        {name: 'QZ_TIME', label: '实际开通时间',width: 150, align: 'center', sortable: false},
                        {name: 'cirLeaseRangeName', label: '电路租用范围',width: 150, align: 'center', sortable: false},
                        {name: 'custManager', label: '客户经理',width: 150, align: 'center', sortable: false},
                        {name: 'custManaPhone', label: '客户经理联系电话',width: 150, align: 'center', sortable: false},
                        {name: 'A_contact_man', label: 'A端联系人',width: 150, align: 'center', sortable: false},
                        {name: 'A_contact_tel', label: 'A端联系电话',width: 150, align: 'center', sortable: false},
                        {name: 'A_installed_add', label: 'A端装机地址',width: 150, align: 'center', sortable: false},
                        {name: 'A_belong_region', label: 'A端归属分公司',width: 150, align: 'center', sortable: false},
                        {name: 'Z_contact_man', label: 'Z端联系人',width: 150, align: 'center', sortable: false},
                        {name: 'Z_contact_tel', label: 'Z端联系电话',width: 150, align: 'center', sortable: false},
                        {name: 'Z_installed_add', label: 'Z端装机地址',width: 150, align: 'center', sortable: false},
                        {name: 'Z_belong_region', label: 'Z端归属分公司',width: 150, align: 'center', sortable: false},
                        {name: 'oldCircuitCode', label: '原电路编号',width: 150, align: 'center', sortable: false},
                        {name: 'cir_remark', label: '备注',width: 150, align: 'center', sortable: false},
                    ],
                    rowNum: 10,
                    rowList: [10, 15, 20, 50, 100, 200, 500],
                    pager: true,
                    recordtext: "{0}-{1} 共{2}条",
                    pgtext: " 第{0}页/共{1}页",
                    rowtext: "每页{0}条",
                    gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                    autoResizable: true,
                    pageData: queryCircuitSummaryList,
                    height: 280,
                    autowidth: false,
                    shrinkToFit: false,
                    showColumnsFeature: true
                });
                this.resize();
            },
            resize: function () {
                $("#circuitSummaryList-grid").grid("resize", true);
                var frameHeight = document.documentElement.scrollHeight;
                $("#circuitSummaryList-grid").grid("setGridHeight", frameHeight - 230);
            },
            //查询
            queryCircuitSummaryList: function (page, rowNum, isReject) {
                var queryObj={};
                var startDate = $("#circuitSummaryList-startDate").val();//开通时间
                var endDate = $("#circuitSummaryList-endDate").val();//

                if (startDate!="" && endDate!="" && startDate >= endDate){
                    fish.warn("开始时间不能大于完成时间！");
                    return;
                } else if(startDate != '' && endDate == '') {
                    fish.warn("请选择时间段！");
                    return;
                }else {
                    page = (page != '' && page != undefined) ? page : localStandyPage;
                    rowNum = (rowNum != '' && rowNum != undefined) ? rowNum : localStandyNum;
                    fish.store.set('circuitSummaryList-grid-rowNum', rowNum); //记录用户选择的每页记录数
                    queryObj.parameters = {
                        circuitNo: $("#circuitSummaryList-circuitNo").val(),
                        serialNumber: $("#circuitSummaryList-serialNumber").val(),
                        custName: $("#circuitSummaryList-custNameChinese").val(),
                        productType: $("#circuitSummaryList-productType").multiselect('value') || [],
                        oprState: $("#circuitSummaryList-oprState").multiselect('value') || [],
                        cirLeaseRangeName: $("#circuitSummaryList-cirLeaseRange").multiselect('value') || [],
                        startDate: $("#circuitSummaryList-startDate").val(),
                        endDate: $("#circuitSummaryList-endDate").val(),
                        actType: $("#circuitSummaryList-actType").val(),
                    };
                    queryObj.pageIndex = page + '';
                    queryObj.pageSize = rowNum + '';
                    console.log("queryData", queryObj);
                    $("#circuitSummaryList-grid").blockUI({message: '加载中'}).data('blockui-content', true);
                    reportAction.queryCircuitSummaryList(queryObj, function (data) {
                        console.log("data", data);
                        $("#circuitSummaryList-grid").unblockUI().data('blockui-content', false);
                        var gridData = {
                            "rows": data.data,
                            "page": data.pageIndex,
                            "records": data.dataLength,
                            "rowNum": data.rowNum,
                            "total": data.total
                        };
                        $("#circuitSummaryList-grid").grid("reloadData", gridData);
                    });
                }
            },

            //导出
            exportExcel: function () {
                var me = this;
                var startDate = $("#circuitSummaryList-startDate").val();//开通时间
                var endDate = $("#circuitSummaryList-endDate").val();//

                if (startDate!="" && endDate!="" && startDate >= endDate){
                    fish.warn("开始时间不能大于完成时间！");
                    return;
                } else if(startDate != '' && endDate == '') {
                    fish.warn("请选择时间段！");
                    return;
                }else {
                    var data = {
                        circuitNo: $("#circuitSummaryList-circuitNo").val(),
                        serialNumber: $("#circuitSummaryList-serialNumber").val(),
                        custName: $("#circuitSummaryList-custNameChinese").val(),
                        productType: $("#circuitSummaryList-productType").multiselect('value') || [],
                        oprState: $("#circuitSummaryList-oprState").multiselect('value') || [],
                        cirLeaseRangeName: $("#circuitSummaryList-cirLeaseRange").multiselect('value') || [],
                        startDate: $("#circuitSummaryList-startDate").val(),
                        endDate: $("#circuitSummaryList-endDate").val(),
                        actType: $("#circuitSummaryList-actType").val(),
                    };
                    reportAction.export(me.getRootPath() + '/localScheduleLT/ReportController/exportCircuitSummaryData.spr', data);
                }
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