define(["text!module/UnicomLocalNet/resmaster/portal/reportSystem/templates/resAllocateUnStorageListView.html",
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        'module/UnicomLocalNet/resmaster/portal/reportSystem/action/reportSystemAction.js',
        'module/UnicomLocalNet/resmaster/portal/reportSystem/action/cQreportSystemAction.js',
        "css!module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/styles/fqAffair.css"],
    function (template, portalViewi18n, reportAction, cQreportAction,css) {
        var localStandyNum = 10;                  //页面展示条数
        var localStandyPage = 1;                  //页数
        return ngc.View.extend({
            template: ngc.compile(template),
            i18nData: ngc.extend({}, portalViewi18n),
            events: {
                'click #resAllocateUnStorageList-queryBtn': 'query',//查询
                'click #resAllocateUnStorageList-exportBtn': 'exportExcel',//导出
                'click #resAllocateUnStorageList-initStaBtn': 'initStatistics',//生成汇总报表
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
                $('#resAllocateUnStorageList-beginDate,#resAllocateUnStorageList-endDate,#apply-beginDate,#apply-endDate').datetimepicker({
                    orientation:{y:'bottom'},
                    buttonIcon: '',
                    viewType: "date",
                    todayBtn: true
                });

                //初始化下拉框
                this.initMultiselect();
                //初始化表格
                this.initGrid();
                //查询数据
               // this.queryResAllocateUnStorageList();
                // 增加input清除功能
                $("#resAllocateUnStorageList-form input").clearinput();
                // 调整窗口大小
                $(window).trigger("resize");

            },
            query:function(){
                this.queryResAllocateUnStorageList(1,undefined,undefined);
            },
            initMultiselect:function(){

                cQreportAction.getSpecialtyType(function (data) {
                    $('#resAllocateUnStorageList-specialtyType').multiselect({
                        dataTextField: 'text',
                        dataValueField: 'text',
                        dataSource: data
                    });
                });

                cQreportAction.getCityInfo(function (data) {
                    $('#resAllocateUnStorageList-fpStaffOrg').multiselect({
                        dataTextField: 'text',
                        dataValueField: 'text',
                        dataSource: data
                    });

                });

                $('#resAllocateUnStorageList-resources').multiselect({
                    dataTextField:'id',
                    dataValueField:'value',
                    dataSource:  [
                        {id: '政企中台', value: 'jike'},
                        {id: '一干调度', value: 'onedry'},
                        {id: '二干调度', value: 'secondary'},
                        {id: '本地调度', value: 'localBuild'},
                        {id: '云网协同', value: 'cloudNetwork'}
                    ]
                });


            },
            //初始化表格
            initGrid: function () {
                var queryResAllocateUnStorageList = $.proxy(this.queryResAllocateUnStorageList, this);
                $("#resAllocateUnStorageList-grid").grid({
                    datatype: "json",
                    colModel: [
                        //默认展示字段
                        {name: 'RESOURCES_NAME', label: '单据来源',width: 150, align: 'center', sortable: false},
                        {name: 'HANDLE_DEP', label: '发起单位（分公司）',width: 180, align: 'center', sortable: false},
                        {name: 'APPLY_ORD_ID', label: '申请单号',width: 180, align: 'center', sortable: false},
                        {name: 'CREATE_DATE', label: '申请单创建时间',width: 150, align: 'center', sortable: false},
                        {name: 'APPLY_ORD_NAME', label: '申请单标题',width: 150, align: 'center', sortable: false},
                        {name: 'CUST_NAME_CHINESE', label: '客户名称',width: 150, align: 'center', sortable: false},
                        {name: 'PRODUCT_NAME', label: '产品类型',width: 150, align: 'center', sortable: false},
                        {name: 'SERIAL_NUMBER', label: '业务号码',width: 150, align: 'center', sortable: false},
                        {name: 'ATTR_VALUE', label: '电路编号',width: 150, align: 'center', sortable: false},
                        {name: 'OPERATE_NAME', label: '动作类型',width: 150, align: 'center', sortable: false},
                        {name: 'DISPATCH_ORDER_NO', label: '调度单号',width: 150, align: 'center', sortable: false},
                        {name: 'SUBSCRIBE_ID', label: '客户订单号',width: 150, align: 'center', sortable: false},
                        {name: 'UN_SAVE_SPECIALTY', label: '资源分配未入库专业',width: 150, align: 'center', sortable: false},
                        {name: 'FP_ORG', label: '资源分配处理人员所属区域',width: 150, align: 'center', sortable: false},
                        {name: 'FP_STAFF', label: '资源分配提交人员/角色',width: 150, align: 'center', sortable: false},
                        {name: 'IS_FINISH', label: '是否报竣',width: 150, align: 'center', sortable: false},
                        {name: 'TRADE_ID', label: '业务订单号',width: 150, align: 'center', sortable: false},
                    ],
                    rowNum: 10,
                    rowList: [10, 15, 20, 50, 100, 200, 500],
                    pager: true,
                    recordtext: "{0}-{1} 共{2}条",
                    pgtext: " 第{0}页/共{1}页",
                    rowtext: "每页{0}条",
                    gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                    autoResizable: true,
                    pageData: queryResAllocateUnStorageList,
                    height: 280,
                    autowidth: false,
                    shrinkToFit: false,
                    showColumnsFeature: true
                });
                this.resize();
            },
            resize: function () {
                $("#resAllocateUnStorageList-grid").grid("resize", true);
                var frameHeight = document.documentElement.scrollHeight;
                $("#resAllocateUnStorageList-grid").grid("setGridHeight", frameHeight - 230);
            },
            //查询
            queryResAllocateUnStorageList: function (page, rowNum, isReject) {
                var queryObj={};
                page = (page != '' && page != undefined) ? page : localStandyPage;
                rowNum = (rowNum != '' && rowNum != undefined) ? rowNum : localStandyNum;
                fish.store.set('resAllocateUnStorageList-grid-rowNum', rowNum); //记录用户选择的每页记录数
                queryObj.parameters ={
                    endDate:$("#resAllocateUnStorageList-endDate").val()||''
                    ,beginDate:$("#resAllocateUnStorageList-beginDate").val()||''
                    ,applyEndDate:$("#apply-endDate").val()||''
                    ,applyBeginDate:$("#apply-beginDate").val()||''
                    ,resources:$("#resAllocateUnStorageList-resources").multiselect('value')||[]
                    ,specialtyType:$("#resAllocateUnStorageList-specialtyType").multiselect('value')||[]
                    ,fpStaffOrg:$("#resAllocateUnStorageList-fpStaffOrg").multiselect('value')||[]
                };
                queryObj.pageIndex = page + '';
                queryObj.pageSize = rowNum + '';
                if (queryObj.parameters.beginDate && queryObj.parameters.endDate && queryObj.parameters.beginDate > queryObj.parameters.endDate) {
                    fish.warn("开始时间必须小于结束时间！");
                    return;
                }

                if (queryObj.parameters.applyBeginDate && queryObj.parameters.applyEndDate && queryObj.parameters.applyBeginDate > queryObj.parameters.applyEndDate) {
                    fish.warn("开始时间必须小于结束时间！");
                    return;
                }
                console.log("queryData",queryObj);
                $("#resAllocateUnStorageList-grid").blockUI({message: '加载中'}).data('blockui-content', true);
                cQreportAction.queryResAllocateUnStorageList(queryObj, function (data) {
                    console.log("data",data);
                    $("#resAllocateUnStorageList-grid").unblockUI().data('blockui-content', false);
                    var gridData = {
                        "rows": data.data,
                        "page": data.pageIndex,
                        "records": data.dataLength,
                        "rowNum": data.rowNum,
                        "total": data.total
                    };
                    $("#resAllocateUnStorageList-grid").grid("reloadData", gridData);
                });
            },

            //导出
            exportExcel: function () {
                var me = this;
                var data ={
                    endDate:$("#resAllocateUnStorageList-endDate").val()||''
                    ,beginDate:$("#resAllocateUnStorageList-beginDate").val()||''
                    ,applyEndDate:$("#apply-endDate").val()||''
                    ,applyBeginDate:$("#apply-beginDate").val()||''
                    ,resources:$("#resAllocateUnStorageList-resources").multiselect('value')||[]
                    ,specialtyType:$("#resAllocateUnStorageList-specialtyType").multiselect('value')||[]
                    ,fpStaffOrg:$("#resAllocateUnStorageList-fpStaffOrg").multiselect('value')||[]

                };
                if (data.beginDate && data.endDate && data.beginDate >data.endDate) {
                    fish.warn("开始时间必须小于结束时间！");
                    return;
                }
                reportAction.export(me.getRootPath() + '/localScheduleLT/CqReportController/exportResAllocateUnStorageData.spr', data);
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

            //生成汇总报表
            initStatistics:function () {
                var me = this;
                var  parameters ={
                    endDate:$("#resAllocateUnStorageList-endDate").val()||''
                    ,beginDate:$("#resAllocateUnStorageList-beginDate").val()||''
                    ,applyEndDate:$("#apply-endDate").val()||''
                    ,applyBeginDate:$("#apply-beginDate").val()||''
                    ,resources:$("#resAllocateUnStorageList-resources").multiselect('value')||[]
                    ,specialtyType:$("#resAllocateUnStorageList-specialtyType").multiselect('value')||[]
                    ,fpStaffOrg:$("#resAllocateUnStorageList-fpStaffOrg").multiselect('value')||[]
                };
              //  var id = data.id;
                //如果是表单一级菜单不打开

              /*  me.getView("#tabContent").openFrameView("a34",
                    "測試", "resAllocateUnStorageListStatisticIndex.html?navType=no",true,true,null,false);*/
                var pop = ngc.openView({
                    url: 'module/UnicomLocalNet/resmaster/portal/reportSystem/views/resAllocateUnStorageStatisticsView',
                    width: "100%",
                    height: "100%",
                    canClose:true,
                    title: "资源分配未入库汇总汇总",
                    viewOption: {
                        data: parameters
                        ,type:'readonly'
                    },
                    callback: function (popup, view) {
                        popup.result.then(function (e) {
                        }, function (e) {
                            console.log('关闭了', e);
                        });
                    }
                });
            },

        })
    });