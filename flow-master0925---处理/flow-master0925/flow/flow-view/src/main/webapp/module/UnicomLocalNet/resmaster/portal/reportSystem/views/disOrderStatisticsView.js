define(["text!module/UnicomLocalNet/resmaster/portal/reportSystem/templates/disOrderStatisticsView.html",
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        'module/UnicomLocalNet/resmaster/portal/reportSystem/action/reportSystemAction.js',
        "css!module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/styles/fqAffair.css"],
    function (template, portalViewi18n, reportAction, css) {
        return ngc.View.extend({
            template: ngc.compile(template),
            i18nData: ngc.extend({}, portalViewi18n),
            events: {
                'click #disOrderDetailStatistics-queryBtn': 'query',//查询
                'click #disOrderDetailStatistics-exportBtn': 'exportExcel',//导出
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
                $('#disOrderDetailStatistics-beginDate,#disOrderDetailStatistics-endDate').datetimepicker({
                    buttonIcon: '',
                    viewType: "date",
                    todayBtn: true
                });

                //初始化下拉框
                this.initCombobox();
                //初始化表格
                this.initDisDetailGrid();
                // 增加input清除功能
                $("#disOrderDetailStatistics-form input").clearinput();
                // 调整窗口大小
                $(window).trigger("resize");
            },
            query:function(){
                this.querydisOrderDetailStatisticsList();
            },
            initCombobox:function(){
                $('#isSupplement').combobox({
                    placeholder: '--请选择是否资源补录--',
                    dataTextField: 'name',
                    dataValueField: 'value',
                    dataSource: [
                        {name: '是', value: '1'},
                        {name: '否', value: '0'}
                    ]
                });
                reportAction.queryEnum({productCode:"product_code"},function (data) {
                    $('#disOrderDetailStatistics-productType').combobox({
                        placeholder: '--请选择产品类型--',
                        dataTextField: 'text',
                        dataValueField: 'value',
                        dataSource: data
                    });
                });
                reportAction.queryEnum({productCode:"operate_type"},function (data) {
                    $('#disOrderDetailStatistics-operateType').combobox({
                        placeholder: '--请选择动作类型--',
                        dataTextField: 'text',
                        dataValueField: 'value',
                        dataSource: data
                    });
                });
            },
            //初始化表格
            initDisDetailGrid: function () {
                var querydisOrderDetailStatisticsList = $.proxy(this.querydisOrderDetailStatisticsList, this);
                $("#disOrderDetailStatistics-grid").grid({
                    datatype: "json",
                    colModel: [
                        //默认展示字段
                        {name: 'SRV_ORD_ID', label: '业务定单信息ID', width: 120, align: 'center',hidden:true , sortable: false},
                        {name: 'SUBSCRIBE_ID', label: '客户订单号', width: 150, align: 'left', sortable: false},
                        {name: 'APPLY_ORD_ID', label: '申请单编号', width: 200, align: 'left', sortable: false },
                        {name: 'APPLY_ORD_NAME', label: '申请单标题', width: 200, align: 'left', sortable: false },
                        {name: 'DISPATCH_ORDER_NO', label: '调度单编号', width: 200, align: 'left', sortable: false},
                        {name: 'ISSUPPLEMENT', label: '是否资源补录', width: 160, align: 'left'},
                        {name: 'SERIAL_NUMBER', label: '业务号码', width: 120, align: 'left', sortable: false},
                        {name: 'CIRCUITCODE', label: '电路编号', width: 150, align:'left', sortable: false},
                        {name: 'SERVICETYPE', label: '产品类型', width: 100, align:'left', sortable: false},
                        {name: 'OPERTYPE', label: '动作类型', width: 100, align:'left', sortable: false},
                        {name: 'TACHE_NAME', label: '当前环节', width: 120, align:'left', sortable: false},
                        {name: 'CREATE_DATE', label: '创建时间', width: 160, align:'left', sortable: false},
                        {name: 'USER_REAL_NAME', label: '当前处理人', width: 160, align:'left', sortable: false},
                        //{name: 'ORG_NAME', label: '处理人部门', width: 160, align:'left', sortable: false}
                        {name: 'ORG_NAME', label: '处理人所属分公司', width: 160, align:'left', sortable: false},
                        {name: 'REPORTTIME', label: '报竣时间', width: 160, align:'left', sortable: false},
                        {name: 'A_INSTALLED_ADD', label: 'A端装机地址', width: 160, align:'left', sortable: false},
                        {name: 'Z_INSTALLED_ADD', label: 'Z端装机地址', width: 160, align:'left', sortable: false},
                        {name: 'A_CITY', label: 'A端所属地市', width: 160, align:'left', sortable: false},
                        {name: 'Z_CITY', label: 'Z端所属地市', width: 160, align:'left', sortable: false},
                        {name: 'A_REQ_FIN_DATE', label: 'A端要求完成时间', width: 160, align:'left', sortable: false},
                        {name: 'Z_REQ_FIN_DATE', label: 'Z端要求完成时间', width: 160, align:'left', sortable: false},
                        {name: 'REQ_FIN_DATE', label: '全程要求完成', width: 160, align:'left', sortable: false},


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
                    shrinkToFit: false,
                    showColumnsFeature: true,
                    pageData: querydisOrderDetailStatisticsList,
                    height: 380
                });
                this.resize();
            },
            resize: function () {
                $("#disOrderDetailStatistics-grid").grid("resize", true);
                var frameHeight = document.documentElement.scrollHeight;
                $("#disOrderDetailStatistics-grid").grid("setGridHeight", frameHeight - 150);
            },
            //查询
            querydisOrderDetailStatisticsList: function (page, rowNum, isReject) {
                debugger;
                var param = {};
                param.operateType = $("#disOrderDetailStatistics-operateType").val();
                param.productCode = $("#disOrderDetailStatistics-productType").val();
                param.isSupplement = $("#isSupplement").val();
                param.endDate = $("#disOrderDetailStatistics-endDate").val();
                param.beginDate = $("#disOrderDetailStatistics-beginDate").val();
                if (page == undefined){
                    param.pageIndex = 1;
                }else{
                    param.pageIndex = page;
                }
                if (rowNum == undefined){
                    param.pageSize = 10;
                }else{
                    param.pageSize = rowNum;
                }
                if (param.beginDate && param.endDate && param.beginDate > param.endDate) {
                    fish.warn("开始时间必须小于结束时间！");
                    return;
                }
                $("#disOrderDetailStatistics-grid").blockUI({message: '加载中'}).data('blockui-content', true);
                reportAction.queryDisOrderList(param, function (res) {
                    if (res.success){
                        $("#disOrderDetailStatistics-grid").grid("reloadData", res.data);
                    }
                    $("#disOrderDetailStatistics-grid").unblockUI().data('blockui-content', false);
                });
            },

            //导出
            exportExcel: function () {
                var param = {};
                param.operateType = $("#disOrderDetailStatistics-operateType").val();
                param.productCode = $("#disOrderDetailStatistics-productType").val();
                param.isSupplement = $("#isSupplement").val();
                param.endDate = $("#disOrderDetailStatistics-endDate").val();
                param.beginDate = $("#disOrderDetailStatistics-beginDate").val();
                param.pageIndex = 1;
                param.pageSize = 10000;
                if (param.beginDate && param.endDate && param.beginDate > param.endDate) {
                    fish.warn("开始时间必须小于结束时间！");
                    return;
                }
                reportAction.export(this.getRootPath() + '/localScheduleLT/ReportController/exportDisOrderDetailData.spr', param);
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