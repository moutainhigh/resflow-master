define(["text!module/UnicomLocalNet/resmaster/portal/reportSystem/templates/businessNetworkVerificationView.html",
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
                'click #busNetWorkVer-queryBtn': 'query',//查询
                'click #busNetWorkVer-exportBtn': 'exportExcel',//导出
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
                $('#busNetWorkVer-beginDate,#busNetWorkVer-endDate').datetimepicker({
                    orientation:{y:'bottom'},
                    buttonIcon: '',
                    viewType: "date",
                    todayBtn: true
                });

                //初始化下拉框
                this.initCombobox();
                //初始化表格
                this.initDisDetailGrid();
                //查询数据
                // this.queryBusNetWorkVerList();
                // 增加input清除功能
                $("#busNetWorkVer-form input").clearinput();
                // 调整窗口大小
                $(window).trigger("resize");

            },
            query:function(){
                this.queryBusNetWorkVerList(1,undefined,undefined);
            },
            initCombobox:function(){
                reportAction.queryEnum({productCode:"product_code"},function (data) {
                    $('#busNetWorkVer-productType').combobox({
                        placeholder: '--请选择产品类型--',
                        dataTextField: 'text',
                        dataValueField: 'value',
                        dataSource: data
                    });
                });
                reportAction.queryEnum({productCode:"operate_type"},function (data) {
                    $('#busNetWorkVer-operateType').combobox({
                        placeholder: '--请选择动作类型--',
                        dataTextField: 'text',
                        dataValueField: 'value',
                        dataSource: data
                    });
                });
            },
            //初始化表格
            initDisDetailGrid: function () {
                var me = this;
                var queryBusNetWorkVerList = $.proxy(this.queryBusNetWorkVerList, this);
                $("#busNetWorkVer-grid").grid({
                    datatype: "json",
                    colModel: [
                        //默认展示字段
                        {name: 'SRV_ORD_ID', label: '业务定单信息ID', hidden:true , sortable: false},
                        {name: 'custName', label: '客户名称',width: 150, align: 'center', sortable: false},
                        {name: 'subscribeId', label: '客户订单号',width: 150, align: 'center', sortable: false},
                        {name: 'applyOrdId', label: '申请单编号',width: 150, align: 'center', sortable: false},
                        {name: 'tradeId', label: '业务单编号',width: 150, align: 'center', sortable: false},
                        {name: 'serialNumber', label: '业务号码',width: 150, align: 'center', sortable: false},
                        {name: 'itemType', label: '单据类型',width: 150, align: 'center', sortable: false, formatter: me.formatItemType},
                        {name: 'productType', label: '产品类型',width: 150, align: 'center', sortable: false},
                        {name: 'operateType', label: '动作类型',width: 150, align: 'center', sortable: false},
                        {name: 'circuitCode', label: '电路编号',width: 150, align: 'center', sortable: false},
                        {name: 'reportTime', label: '报竣时间',width: 150, align: 'center', sortable: false},
                        {name: 'startTime', label: '起租/止租时间',width: 150, align: 'center', sortable: false},
                        {name: 'routInfoList', label: '详细路由',width: 150, align: 'center', sortable: false},
                        {name: 'disOrderNoList', label: '电路历史调单编号',width: 150, align: 'center', sortable: false},
                        {name: 'speedName',label: '带宽速率',width: 150, align: 'center', sortable: false},
                        {name: 'custManager',label: '客户经理',width: 150, align: 'center', sortable: false},
                        {name: 'custManaPhone',label: '客户经理联系电话',width: 150, align: 'center', sortable: false},
                        {name: 'cir_remark', label: '电路备注',width: 150, align: 'center', sortable: false},
                        {name: 'A_belong_province', label: 'A端归属省',width: 150, align: 'center', sortable: false},
                        {name: 'A_belong_city', label: 'A端归属地市',width: 150, align: 'center', sortable: false},
                        {name: 'A_belong_region', label: 'A端归属分公司',width: 150, align: 'center', sortable: false},
                        {name: 'A_installed_add', label: 'A端装机地址',width: 150, align: 'center', sortable: false},
                        {name: 'A_contact_man', label: 'A端联系人',width: 150, align: 'center', sortable: false},
                        {name: 'A_contact_tel', label: 'A端联系电话',width: 150, align: 'center', sortable: false},
                        {name: 'Z_belong_province', label: 'Z端归属省',width: 150, align: 'center', sortable: false},
                        {name: 'Z_belong_city', label: 'Z端归属地市',width: 150, align: 'center', sortable: false},
                        {name: 'Z_belong_region', label: 'Z端归属分公司',width: 150, align: 'center', sortable: false},
                        {name: 'Z_installed_add', label: 'Z端装机地址',width: 150, align: 'center', sortable: false},
                        {name: 'Z_contact_man', label: 'Z端联系人',width: 150, align: 'center', sortable: false},
                        {name: 'Z_contact_tel', label: 'Z端联系电话',width: 150, align: 'center', sortable: false}
                    ],
                    rowNum: 10,
                    rowList: [10, 15, 20, 50, 100, 200, 500],
                    pager: true,
                    recordtext: "{0}-{1} 共{2}条",
                    pgtext: " 第{0}页/共{1}页",
                    rowtext: "每页{0}条",
                    gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                    autoResizable: true,
                    pageData: queryBusNetWorkVerList,
                    height: 380,
                    autowidth: false,
                    shrinkToFit: false,
                    showColumnsFeature: true
                });
                this.resize();
            },
            resize: function () {
                $("#busNetWorkVer-grid").grid("resize", true);
                var frameHeight = document.documentElement.scrollHeight;
                $("#busNetWorkVer-grid").grid("setGridHeight", frameHeight - 150);
            },
            //查询
            queryBusNetWorkVerList: function (page, rowNum, isReject) {
                debugger;
                var me = this;
                var queryObj={};
                page = (page != '' && page != undefined) ? page : localStandyPage;
                debugger;
                rowNum = (rowNum != '' && rowNum != undefined) ? rowNum : localStandyNum;
                fish.store.set('busNetWorkVer-grid-rowNum', rowNum); //记录用户选择的每页记录数
                queryObj.parameters ={
                    operateType:$("#busNetWorkVer-operateType").combobox('value') || ''
                    ,productCode:$("#busNetWorkVer-productType").combobox('value') || ''
                    ,itemType:$("#busNetWorkVer-itemType").combobox('value') || ''
                    ,endDate:$("#busNetWorkVer-endDate").val()
                    ,beginDate:$("#busNetWorkVer-beginDate").val()
                    ,circuitCode:$("#busNetWorkVer-circuitCode").val()
                    ,custName:$("#busNetWorkVer-custName").val()
                    ,serialNumber:$("#busNetWorkVer-serialNumber").val()
                };
                queryObj.pageIndex = page + '';
                queryObj.pageSize = rowNum + '';
                if (queryObj.parameters.beginDate && queryObj.parameters.endDate && queryObj.parameters.beginDate > queryObj.parameters.endDate) {
                    fish.warn("开始时间必须小于结束时间！");
                    return;
                }
                console.log("queryData",queryObj);
                $("#busNetWorkVer-grid").blockUI({message: '加载中'}).data('blockui-content', true);
                reportAction.businessNetworkVerification(queryObj, function (data) {
                    console.log("data",data);
                    $("#busNetWorkVer-grid").unblockUI().data('blockui-content', false);
                    var gridData = {
                        "rows": data.data,
                        "page": data.pageIndex,
                        "records": data.dataLength,
                        "rowNum": data.rowNum,
                        "total": data.total
                    };
                    $("#busNetWorkVer-grid").grid("reloadData", gridData);
                });
            },

            //导出
            exportExcel: function () {
                var me = this;
                var data ={
                    operateType:$("#busNetWorkVer-operateType").combobox('value') || ''
                    ,productCode:$("#busNetWorkVer-productType").combobox('value') || ''
                    ,itemType:$("#busNetWorkVer-itemType").combobox('value') || ''
                    ,endDate:$("#busNetWorkVer-endDate").val()
                    ,beginDate:$("#busNetWorkVer-beginDate").val()
                    ,circuitCode:$("#busNetWorkVer-circuitCode").val()
                    ,custName:$("#busNetWorkVer-custName").val()
                    ,serialNumber:$("#busNetWorkVer-serialNumber").val()
                };
                if (data.beginDate && data.endDate && data.beginDate >data.endDate) {
                    fish.warn("开始时间必须小于结束时间！");
                    return;
                }
                reportAction.export(me.getRootPath() + '/localScheduleLT/ReportController/exportBusNetVerData.spr', data);
            },
            //单据类型转换
            formatItemType: function (value) {
                var itemTypeMap = {
                    '101':'开通单',
                    '102':'核查单'
                   /* '104':'追单',
                    '108':'加急',
                    '109':'延期',
                    '110':'挂起',
                    '111':'解挂',
                    '112':'撤单',
                    '114':'全程调测退单'*/
                };
                return itemTypeMap[value];
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