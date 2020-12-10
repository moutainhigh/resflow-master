define(["text!module/UnicomLocalNet/resmaster/portal/reportSystem/templates/undoneTacheStatisticView.html",
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
                'click #undoneTacheSta-queryBtn': 'query',//查询
                'click #undoneTacheSta-exportBtn': 'exportExcel',//导出
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
                $('#undoneTacheSta-beginDate,#undoneTacheSta-endDate').datetimepicker({
                    orientation:{y:'bottom'},
                    buttonIcon: '',
                    viewType: "date",
                    todayBtn: true
                });

                //初始化下拉框
                this.initMultiselect();
                //回显数据
                this.initData();





                // 增加input清除功能
                $("#undoneTacheSta-form input").clearinput();
                // 调整窗口大小
                $(window).trigger("resize");

            },
            query: function () {
                this.queryData();
            },
            //回写查询数据
            initData:function(){
                var me = this;
                console.log("options",me.options);
                if (me.options.type == 'readonly') {
                    setTimeout(function(){
                        var data = me.options.data;
                        $("#undoneTacheSta-endDate").val(data.endDate);
                        $("#undoneTacheSta-beginDate").val(data.beginDate);
                        $("#undoneTacheSta-resources").multiselect('value', data.resources);
                        $("#undoneTacheSta-tacheName").multiselect('value', data.tacheCodes);
                        $("#undoneTacheSta-productType").multiselect('value', data.productType);
                        $("#undoneTacheSta-operateType").multiselect('value', data.operateType);
                        $("#undoneTacheSta-dcStaffOrg").multiselect('value', data.dcStaffOrg);
                        $("#undoneTacheSta-orgName").multiselect('value', data.orgName);
                        $("#undoneTacheSta-handelDep").multiselect('value', data.handelDep);
                        $("#undoneTacheSta-tacheDuration").val(data.tacheDuration);
                        me.queryData();
                    }, 1500);
                }
            },
            initMultiselect: function () {
                reportAction.queryEnum({productCode: "product_code"}, function (data) {
                    $('#undoneTacheSta-productType').multiselect({
                        dataTextField: 'text',
                        dataValueField: 'value',
                        dataSource: data
                    });
                });
                reportAction.queryEnum({productCode: "operate_type"}, function (data) {
                    $('#undoneTacheSta-operateType').multiselect({
                        dataTextField: 'text',
                        dataValueField: 'value',
                        dataSource: data
                    });
                });

                cQreportAction.getLocalNetworkTache(function (data) {
                    $('#undoneTacheSta-tacheName').multiselect({
                        dataTextField: 'text',
                        dataValueField: 'value',
                        dataSource: data
                    });
                });

                cQreportAction.getCityInfo(function (data) {
                    $('#undoneTacheSta-dcStaffOrg,#undoneTacheSta-handelDep,#undoneTacheSta-orgName').multiselect({
                        dataTextField: 'text',
                        dataValueField: 'text',
                        dataSource: data
                    });

                });

                $('#undoneTacheSta-resources').multiselect({
                    dataTextField: 'value',
                    dataValueField: 'id',
                    dataSource:  [
                        {value: '政企中台', id: 'jike'},
                        {value: '一干调度', id: 'onedry'},
                        {value: '二干调度', id: 'secondary'},

                        {value: '云网协同', id: 'cloudNetwork'}
                    ]
                });

                $('#undoneTacheSta-tacheDuration').spinner({
                    step: 0.5,
                    min: 0
                });
            },
            //查询数据
            queryData: function () {
                var me = this;
                var params = {
                    endDate: $("#undoneTacheSta-endDate").val() || ''
                    , beginDate: $("#undoneTacheSta-beginDate").val() || ''
                    , resources: $("#undoneTacheSta-resources").multiselect('value') || []
                    , tacheCodes: $("#undoneTacheSta-tacheName").multiselect('value') || []
                    , productType: $("#undoneTacheSta-productType").multiselect('value') || []
                    , operateType: $("#undoneTacheSta-operateType").multiselect('value') || []
                    , dcStaffOrg: $("#undoneTacheSta-dcStaffOrg").multiselect('value') || []
                    , orgName: $("#undoneTacheSta-orgName").multiselect('value') || []
                    , handelDep: $("#undoneTacheSta-handelDep").multiselect('value') || []
                    , tacheDuration: $("#undoneTacheSta-tacheDuration").val() || ''
                };
                //console.log("queryData",params);
                $("#undoneTacheSta-grid-div").blockUI({message: '加载中'}).data('blockui-content', true);
                cQreportAction.queryUndoneTacheStatistics(params, function (data) {
                    //console.log("data",data);
                    $("#undoneTacheSta-grid-div").unblockUI().data('blockui-content', false);
                    /*  if(data.title.size>0){*/
                     titleList = [{
                        name: "orgName",
                        label: "部门名称",
                        width: 150,
                        align: 'left',
                        sortable: false
                    }];
                    $.each(data.title, function (key, values) {
                        var titleData = {
                            name: key,
                            label: values,
                            width: 150,
                            align: 'left',
                            sortable: false,
                            formatter: me.formatNum
                        };
                        titleList.push(titleData);
                    });
                    titleList.push({
                        name: "countNum",
                        label: "<font  color='red'>合计</font>",
                        width: 100,
                        align: 'left',
                        sortable: false
                    });

                    data.colList = titleList;
                    me.initGrid(data);
                    me.addCountRow();
                    /* }else {
                         ngc.warn("无符合条件的记录");
                     }*/
                });
            },
            //初始化表格
            initGrid: function (data) {
                var me = this;
                $("#undoneTacheSta-grid").grid({
                    colModel: data.colList,
                    gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                    autoResizable: true,
                    data: data.rowData,
                    height: 280,
                    autowidth: false,
                    shrinkToFit: false,
                    showColumnsFeature: true,
                    afterInsertRow: function (e, rowid, data) {
                        //计算行总数
                        var count = 0;
                        $.each(data, function (key, value) {
                            if (key != 'orgName' && key != 'countNum' && key != '_id_') {
                                // console.log(key,value);
                                count += parseInt(value);
                            }
                        });
                        $("#undoneTacheSta-grid").grid('setCell', rowid, 'countNum', count, {color: 'red'});
                        //空行处理
                        if (data.orgName == "") {
                            $("#undoneTacheSta-grid").grid('setCell', rowid, 'orgName', "无所属分公司", {color: 'red'});
                        }
                        //列总计样式
                        if (data.orgName == "合计") {
                            $("#undoneTacheSta-grid").grid('setCell', rowid, 'orgName',"合计", {color: 'red'});
                        }
                    }
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

            //总计行数据处理
            addCountRow: function () {
                var countRow = {orgName: "合计"};
                $($("#undoneTacheSta-grid").grid('getRowData')).each(function () {
                    $.each(this, function (key, value) {
                        if(key != 'orgName' && key != '_id_'){
                            debugger;
                            console.log(key,value);
                            if(typeof(countRow[key]) == "undefined"){
                                countRow[key]=parseInt(value);
                            }else {
                                countRow[key]+=parseInt(value);
                            }
                        }
                    });
                });
                console.log("countRow",countRow);
                $("#undoneTacheSta-grid").grid('addRowData',countRow);
            },
            resize: function () {
                $("#undoneTacheSta-grid").grid("resize", true);
                var frameHeight = document.documentElement.scrollHeight;
                $("#undoneTacheSta-grid").grid("setGridHeight", frameHeight - 230);
            },

            //导出
            exportExcel: function () {
                var me = this;
                var data = {
                    endDate: $("#undoneTacheSta-endDate").val() || ''
                    , beginDate: $("#undoneTacheSta-beginDate").val() || ''
                    , resources: $("#undoneTacheSta-resources").multiselect('value') || []
                    , tacheCodes: $("#undoneTacheSta-tacheName").multiselect('value') || []
                    , productType: $("#undoneTacheSta-productType").multiselect('value') || []
                    , operateType: $("#undoneTacheSta-operateType").multiselect('value') || []
                    , dcStaffOrg: $("#undoneTacheSta-dcStaffOrg").multiselect('value') || []
                    , orgName: $("#undoneTacheSta-orgName").multiselect('value') || []
                    , handelDep: $("#undoneTacheSta-handelDep").multiselect('value') || []
                    , tacheDuration: $("#undoneTacheSta-tacheDuration").val() || ''
                };

                if (data.beginDate && data.endDate && data.beginDate > data.endDate) {
                    fish.warn("开始时间必须小于结束时间！");
                    return;
                }
                var params={
                    queryData:data,
                    title:titleList,
                    data:$("#undoneTacheSta-grid").grid('getRowData'),
                    endDate: $("#undoneTacheSta-endDate").val() || '',
                    beginDate: $("#undoneTacheSta-beginDate").val() || ''
                };
                reportAction.export(me.getRootPath() + '/localScheduleLT/CqReportController/exportUndoneTacheStaData.spr', params);
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