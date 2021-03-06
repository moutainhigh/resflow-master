define(["text!module/UnicomLocalNet/resmaster/portal/reportSystem/templates/finishTimeRateStatisticsView.html",
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        'module/UnicomLocalNet/resmaster/portal/reportSystem/action/reportSystemAction.js',
        'module/UnicomLocalNet/resmaster/portal/reportSystem/action/cQreportSystemAction.js',
        "css!module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/styles/fqAffair.css"],
    function (template, portalViewi18n, reportAction, cQreportAction, css) {
        var titleList=[];//表头
        return ngc.View.extend({
            template: ngc.compile(template),
            i18nData: ngc.extend({}, portalViewi18n),
            events: {
                'click #finishTimeRare-queryBtn': 'query',//查询
                'click #finishTimeRare-exportBtn': 'exportExcel',//导出
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
                $('#finishTimeRare-beginDate,#finishTimeRare-endDate').datetimepicker({
                    orientation:{y:'bottom'},
                    buttonIcon: '',
                    viewType: "date",
                    todayBtn: true
                });

                //初始化下拉框
                this.initMultiselect();
                //初始化表格
                // this.queryData();
                // 增加input清除功能
                $("#finishTimeRare-form input").clearinput();
                // 调整窗口大小
                $(window).trigger("resize");

            },
            query: function () {
                this.queryData();
            },

            initMultiselect: function () {
                reportAction.queryEnum({productCode: "product_code"}, function (data) {
                    $('#finishTimeRare-productType').multiselect({
                        dataTextField: 'text',
                        dataValueField: 'value',
                        dataSource: data
                    });
                });
                reportAction.queryEnum({productCode: "operate_type"}, function (data) {
                    $('#finishTimeRare-operateType').multiselect({
                        dataTextField: 'text',
                        dataValueField: 'value',
                        dataSource: data
                    });
                });

                cQreportAction.getLocalNetworkTache(function (data) {
                    $('#finishTimeRare-tacheName').multiselect({
                        dataTextField: 'text',
                        dataValueField: 'value',
                        dataSource: data
                    });
                });

                cQreportAction.getCityInfo(function (data) {
                    $('#finishTimeRare-dcStaffOrg,#finishTimeRare-handelDep,#finishTimeRare-orgName').multiselect({
                        dataTextField: 'text',
                        dataValueField: 'text',
                        dataSource: data
                    });

                });

                $('#finishTimeRare-resources').multiselect({
                    dataTextField:'value',
                    dataValueField:'id',
                    dataSource:  [
                        { id: "本地客户电路", value: '本地客户电路'},
                        { id: "本地局内电路", value: '本地局内电路'},
                        { id: "二干客户电路", value: '二干客户电路'},
                        { id: "二干局内电路", value: '二干局内电路'},
                        { id: "一干调度（客户电路、局内电路）", value: '一干调度（客户电路、局内电路）'}
                    ]
                });


            },
            //查询数据
            queryData: function () {
                var me = this;
                var params = {
                    endDate: $("#finishTimeRare-endDate").val() || ''
                    , beginDate: $("#finishTimeRare-beginDate").val() || ''
                    , resources: $("#finishTimeRare-resources").multiselect('value') || []
                    , tacheCodes: $("#finishTimeRare-tacheName").multiselect('value') || []
                    , productType: $("#finishTimeRare-productType").multiselect('value') || []
                    , operateType: $("#finishTimeRare-operateType").multiselect('value') || []
                    , dcStaffOrg: $("#finishTimeRare-dcStaffOrg").multiselect('value') || []
                    , orgName: $("#finishTimeRare-orgName").multiselect('value') || []
                    , handelDep: $("#finishTimeRare-handelDep").multiselect('value') || []
                    , tacheDuration: $("#finishTimeRare-tacheDuration").val() || ''
                };
                //console.log("queryData",params);
                $("#finishTimeRare-grid-div").blockUI({message: '加载中'}).data('blockui-content', true);
                cQreportAction.queryFinishTimeRateStatistics(params, function (data) {
                    //console.log("data",data);
                    $("#finishTimeRare-grid-div").unblockUI().data('blockui-content', false);
                    /*  if(data.title.size>0){*/
                     titleList = [];
                    $.each(data.title, function (key, values) {
                        var titleData = {
                            name: key,
                            label: values,
                            width: 180,
                            align: 'left',
                            sortable: false,
                            formatter: me.formatNum
                        };
                        if(key.indexOf("及时率") != -1 ){
                            titleData.formatter=me.formatPresent;
                        }
                        titleList.push(titleData);
                    });
                    data.colList = titleList;
                    me.initGrid(data);

                    /* }else {
                         ngc.warn("无符合条件的记录");
                     }*/
                });
            },
            //初始化表格
            initGrid: function (data) {
                var me = this;
                $("#finishTimeRare-grid").grid({
                    colModel: data.colList,
                    gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                    autoResizable: true,
                    data: data.rowData,
                    height: 280,
                    autowidth: false,
                    shrinkToFit: false,
                    showColumnsFeature: true

                });
                this.resize();
            },
            //格式化数字
            formatNum: function (value) {
                if (typeof (value) == "undefined") {
                    //value="<font  color='red'>0</font>";
                    value = 0;
                }
                return value;
            },
            //格式化几率
            formatPresent: function (value) {
                if (typeof (value) == "undefined") {
                    //value="<font  color='red'>0</font>";
                    value = '0%';
                }
                return value;
            },

            resize: function () {
                $("#finishTimeRare-grid").grid("resize", true);
                var frameHeight = document.documentElement.scrollHeight;
                $("#finishTimeRare-grid").grid("setGridHeight", frameHeight - 230);
            },

            //导出
            exportExcel: function () {
                var me = this;
                var params={
                    title:titleList,
                    data:$("#finishTimeRare-grid").grid('getRowData'),
                    endDate: $("#finishTimeRare-endDate").val() || '',
                    beginDate: $("#finishTimeRare-beginDate").val() || ''
                };
                console.log("ex",params);
                reportAction.export(me.getRootPath() + '/localScheduleLT/CqReportController/exportFinishTimeRareData.spr', params);
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